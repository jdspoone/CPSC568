package iRobotCreate;


import iRobotCreate.iRobotCommands.Sensor;
import iRobotCreate.simulator.CameraSimulation;
import iRobotCreate.simulator.Environment;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;

import casa.LispAccessible;
import casa.ML;
import casa.MLMessage;
import casa.Status;
import casa.TransientAgent;
import casa.abcl.CasaLispOperator;
import casa.abcl.ParamsMap;
import casa.agentCom.URLDescriptor;
import casa.conversation2.SubscribeClientConversation;
import casa.exceptions.IllegalOperationException;
import casa.ui.AbstractInternalFrame;
import casa.ui.AgentUI;
import casa.util.CASAUtil;
import casa.util.Trace;
/**
 * Blah blah blah...
 *   
 * @author Joel Nielsen
 *         Hugo Richard
 *         Jeffrey Spooner
 *         Sara Williamson
 */
public class RobotSoccer extends StateBasedController {
	
	// Boolean for start or stop state
	private boolean isStarted = false;
	
	// Port numbers 
	private final static int cameraPort = 8995;
	private final int robotPort = 9100;
	
	// Variables for game parameters: the colours of this robot, its partners, and its opponents, and the colour of the ball. 
	private String myColour = "purple";
	private String partnerColour;
	private String opponent1Colour;
	private String opponent2Colour;
	private String ballColour = "red";
	
	// Variable for this robot's target goal. True: y=0, false: y=700.
	private Boolean whichGoal;
	
	// Variable for one-on-one soccer (as opposed to a full two-on-two game)
	private Boolean isSinglePlayer;
	
	// Variables for positions of entities in the environment
	Position selfPosition;
	Position puckPosition;
	
	Position intendedPosition;
	double intendedDistance;
	
	// Variables tracking goals scored
	private int playerGoals = 0;
	private int opponentGoals = 0;
	
	public boolean debugMode = false;		// Display debug messages for state on Command console
	
	// Store errors encountered to display on "report" command
	private java.util.LinkedList<String> errors = new java.util.LinkedList<String>();
	
	static { createCasaLispOperators( RobotSoccer.class ); }
	
	
	/**
	 * Lisp accessible command to make the agent begin playing robot soccer.
	 * Once this command has been given once with all parameters, it may be repeated with no parameters to re-use the last settings.
	 * This command is mapped onto pushing the "play" button on the robot.
	 * 
	 * @return Status 0 if successful
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator ROBOTSOCCER_START =
		new casa.abcl.CasaLispOperator("start", "\"!Begin playing soccer by attempting to move the ball into the opposing goal.\" "
				+"&KEY MY \"@java.lang.String\" \"!This robot's color.\" "
				+"PARTNER \"@java.lang.String\" \"!This robot's partner's color.\" "
				+"OP1 \"@java.lang.String\" \"!Opponent 1's color.\" "
				+"OP2 \"@java.lang.String\" \"!Opponent 2's color.\" "
				+"GOALZERO \"@java.lang.Boolean\" \"!Which goal to score on (NIL=y=700, other=y=0).\" "
				, iRobotCreate.class, iRobotCreate.class)
	{
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, org.armedbear.lisp.Environment lispEnv) {
			
			// If a game is in progress, stop it
			if ( ( ( RobotSoccer ) agent ).isStarted )
				ROBOTSOCCER_STOP.execute( ( ( RobotSoccer ) agent ), new ParamsMap(), ( ( RobotSoccer ) agent ).getUI(), null );
			
			// Flush current command queue
			( ( RobotSoccer ) agent ).tellRobot( "(iRobot.drive 0 :flush T :emergency T)" );
			
			// If no parameters are given, use last settings;
			// otherwise, re-set them from input values.
			if ( !params.isEmpty() ) {
			
				( ( RobotSoccer ) agent ).myColour = null;
				if ( params.containsKey( "MY" ) )
					( ( RobotSoccer ) agent ).myColour = (String) params.getJavaObject( "MY" );
	
				( ( RobotSoccer ) agent ).partnerColour = null;
				if ( params.containsKey( "PARTNER" ) )
					( ( RobotSoccer ) agent ).partnerColour = (String) params.getJavaObject( "PARTNER" );
				
				( ( RobotSoccer ) agent ).opponent1Colour = null;
				if ( params.containsKey( "OP1" ) )
					( ( RobotSoccer ) agent ).opponent1Colour = (String) params.getJavaObject( "OP1" );
				
				( ( RobotSoccer ) agent ).opponent2Colour = null;
				if ( params.containsKey( "OP2" ) )
					( ( RobotSoccer ) agent ).opponent2Colour = (String) params.getJavaObject( "OP2" );
				
				( ( RobotSoccer ) agent ).whichGoal = null;
				if ( params.containsKey( "GOALZERO" ) ) 
					( ( RobotSoccer ) agent ).whichGoal = (Boolean) params.getJavaObject( "GOALZERO" );
				
				( ( RobotSoccer ) agent ).isSinglePlayer = null;
				// If a partner and a second opponent are not defined, assume a single player match.
				if ( ( ( RobotSoccer ) agent ).partnerColour == null
						&& ( ( RobotSoccer ) agent ).opponent2Colour == null ) {
							( ( RobotSoccer ) agent ).isSinglePlayer = new Boolean( Boolean.TRUE );
				}
				
				// If both a partner and a second opponent are defined, assume a double match.
				else if ( ( ( RobotSoccer ) agent ).partnerColour != null
						&& ( ( RobotSoccer ) agent ).opponent2Colour != null ) {
							( ( RobotSoccer ) agent ).isSinglePlayer = new Boolean( Boolean.FALSE );
				}
			}
			
			// All parameters must be set at least once, and then they are saved for future runs. If some are not initialized, starting a game fails.
			if ( ( ( RobotSoccer ) agent ).myColour == null
					|| ( ( RobotSoccer ) agent ).opponent1Colour == null
					|| ( ( RobotSoccer ) agent ).whichGoal == null 
					|| ( ( RobotSoccer ) agent ).isSinglePlayer == null ) {
						( (AbstractInternalFrame) ( ( RobotSoccer ) agent ).getUI() ).getCommandPanel().print( "Start failed: parameter(s) uninitialized." );
						return new Status(0);
			}
			
			// Initialized parameters successfully. Begin playing a game of soccer.
			( ( RobotSoccer ) agent ).isStarted = true;
			( ( RobotSoccer ) agent ).setState( ( ( RobotSoccer ) agent ).firstAlignState );
			
			return new Status(0);
		}
	};
	
	/**
	 * Lisp accessible command to report the agent's state.
	 * Prints current state information, current score, and any errors thus far.
	 * 
	 * @return Status 0 if successful
	 */
	@LispAccessible( name = "report", help = "Prints state information and robot's current state on the controller's command tab." )
	public Status report() {

		// Print state information to controller's command panel
		( (AbstractInternalFrame) getUI() ).getCommandPanel().print( "Current state: " + getCurrentState().getName() );
		
		// Print available results
		( (AbstractInternalFrame) getUI() ).getCommandPanel().print( "Current score:\n   Player Goals: " + playerGoals + "\n   Opponent Goals: " + opponentGoals );
		
		// Report any errors encountered
		( (AbstractInternalFrame) getUI() ).getCommandPanel().print( "\nErrors and warnings logged: " );
		int index = 0;
		while ( index < errors.size() ) {
			( (AbstractInternalFrame) getUI() ).getCommandPanel().print( "\n" + errors.get( index ) );
			index++;
		}
		
		// Success
		return new Status( 0 );
	}
	
	
	/**
	 * Lisp accessible command to reset the agent's state.
	 * Puts the robot in "waiting" state and resets all relevant state variables.
	 * This command is mapped to pushing "play" on the robot.
	 * 
	 * @return Status 0 if successful
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator ROBOTSOCCER_STOP =
		new casa.abcl.CasaLispOperator("stop", "\"!Reset the robot, clear results, and go into waiting state.\" "
				, iRobotCreate.class, iRobotCreate.class)
	{
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, org.armedbear.lisp.Environment lispEnv) {
			
			// Flush current command queue
			( ( RobotSoccer ) agent ).tellRobot( "(iRobot.drive 0 :flush T :emergency T)" );
						
			// Clear results
			( ( RobotSoccer ) agent ).playerGoals = 0;
			( ( RobotSoccer ) agent ).opponentGoals = 0;
			
			// Reset successfully
			( ( RobotSoccer ) agent ).isStarted = false;
			
			// Enter waiting state
			( ( RobotSoccer ) agent ).setState( ( ( RobotSoccer ) agent ).waitingState );
			
			return new Status(0);
		}
	};
	
	/**
	 * Command line lisp command to set or unset debug mode.
	 * In this state, state changes are printed to the Command console.
	 * 
	 * @return Status 0 if successful
	 */
	@LispAccessible( name = "debug", help = "Toggle debug mode, which displays state changes in the Command console." )
	public Status debug() {

		if ( debugMode )
			debugMode = false;
		else
			debugMode = true;
		
		// Success
		return new Status( 0 );
	}
	
	
	/**
	 * Print debug messages on state change, if in debug mode.
	 */
	@Override
	public void setState(IRobotState s) {
		super.setState( s );
		
		if ( debugMode )
			( (AbstractInternalFrame) getUI() ).getCommandPanel().print( s.getName() );
	}

	
	/**
	   * Start an {@link iRobotCreate} robot at 9100.<br>
	   * Wait for it to be initialized.<br>
	   * Start a {@link RobotSoccer} controller at 9200.<br>
	   * @param args
	   * @author Rob Kremer
	   */
	  public static void main(String[] args) {
	  	iRobotCreate alice = (iRobotCreate)CASAUtil.startAnAgent(iRobotCreate.class, "Alice", 9100, null
	  			, "PROCESS", "CURRENT"
	  			, "INSTREAM", "alice.in" //"/dev/tty.iRobot9"
	  			, "OUTSTREAM", "alice.out" //"/dev/tty.iRobot9"
	  			, "TRACE", "10"
	  			, "TRACETAGS", "iRobot9,warning,msg,msgHandling,kb9,eventloop,-info,commitments,-policies9,-lisp,-eventqueue9,-conversations"
	  			);
	  	
	  	if (alice==null) {
	  		Trace.log("error", "Cannot start an iRobotCreate agent.");
	  	}
	  	else {
	  		while (!alice.isInitialized())
	  			CASAUtil.sleepIgnoringInterrupts(1000, null);
	  		
	  		if (Environment.exits()) { // we are operating a simulator
	  			try {
	  				Environment env = Environment.getInstance();
	  				env.abclEval("(iRobot-env.new-bounds 2304 1382)", null); 
	  				env.abclEval("(iRobot-env.new \"goal0\" \"Rectangle2D\" (/ 2304 2) 25 (floor (/ 2304 3)) 50 :paint T :corporeal NIL :color #x8888FF)", null);
	  				env.abclEval("(iRobot-env.new \"goal1\" \"Rectangle2D\" (/ 2304 2) (- 1384 25) (floor (/ 2304 3)) 50 :paint T :corporeal NIL :color #xFFFF88)", null);
	  				env.abclEval("(iRobot-env.puck :name \"puck\")", null);
	  				env.abclEval("(iRobot-env.set \"puck\" :labeled NIL)",null);
	  				env.abclEval("(iRobot-env.circle \"puck\" :color-name \"red\")",null);
	  				env.abclEval("(iRobot-env.triangle \"Alice\" :name \"red-tri\" :color-name \"purple\")",null);
	  			} catch (Exception e) {
	  				System.out.println(alice.println("error", "Environment failed", e));
	  			}
	  			
	  			try {
	  				@SuppressWarnings("unused")
					CameraSimulation cam = (CameraSimulation) CASAUtil.startAnAgent(
	  						CameraSimulation.class, "camera" ,cameraPort,null,
	  						"LAC","9000",
	  						"PROCESS","CURRENT",
	  						"TRACE","trace-code",
	  						"traceTags", "trace-tags",
	  						"scale","(/ 1280.0 2304)");
	  					  
	  		  				
	  			} catch (Exception e) {
	  				System.out.println(alice.println("error", "Camera failed", e));
	  			}
	  		}

	  		RobotSoccer controllerOfAlice = (RobotSoccer)CASAUtil.startAnAgent(RobotSoccer.class, "controllerOfAlice", 9200, null
	  				, "PROCESS", "CURRENT"
	  				, "CONTROLS", ":9100"
	  				, "TRACE", "10"
	  				, "TRACETAGS", "iRobot9,warning,msg,msgHandling,kb9,eventloop,-info,commitments,-policies9,-lisp,-eventqueue9,-conversations"
	  				);
	  		
	  		if (controllerOfAlice==null) {
	  			Trace.log("error", "Cannot create RobotSoccer agent.");
	  		}
	  	}
	  }
	
	
	/**
	 * Initialize a new RobotSoccer controller. Register all possible states.
	 * 
	 * @param params The standard map of parameters to set up the robot.  This is a simple map of (String) keys to values. The
	 * key "CONTROLS" is required and must specify the URL of an {@link iRobotCreate} agent to control.
	 * @param ui
	 * @throws Exception
	 */
	public RobotSoccer( ParamsMap params, AgentUI ui ) throws Exception {
		super( params, ui );
		
		// parse through the parameter keys to see if we should be controlling
		// a different color
		
		if (params.containsKey("COLOR")) 
			myColour = (String)params.getJavaObject("COLOR");
		if (params.containsKey("BALL-COLOR"))
			ballColour = (String)params.getJavaObject("BALL-COLOR");
				

		// Register all valid states for a RobotSoccer agent: a short description is provided here
		// A longer description is available where the states are implemented.
		
		//Starting state
		registerState( startState );
		
		// State entered when the command wait is typed
		registerState( waitingState );
		
		// State which aligns the robot with a point just above or below the puck
		registerState( firstAlignState );
		
		// State which moves the robot to a point just above or below the puck
		registerState( firstTraversalState );
		
		// State which aligns the robot to and then causes it to push the puck
		registerState( pushBallState );
				
		// When the agent scores a goal, we enter the victory state
		registerState( victoryState );
	}
	
	
	/**
	 * Initialize the controller agent. Subscribe to the robot proxy for updates regarding
	 * any sensors we care about - namely, Distance, Wall, WallSignal, and VirtualWall.
	 * Finally, commence interaction by entering the start state.
	 */
	@Override
	public void initializeAfterRegistered( boolean registered ) {
		super.initializeAfterRegistered( registered );
						
		// Subscribe to alerts for VirtualWall updates
		try {
					@SuppressWarnings("unused")
					SubscribeClientConversation convButtonPress = new SubscribeClientConversation(
							"--subscription-request", 
							this, server, 
							"(all ?x (Buttons ?x))", null)
					{
						
						@Override
						protected void update(URLDescriptor agentB, jade.semantics.lang.sl.grammar.Term exp) {
							if (exp==null)
								return;
							String intString = exp.toString();
							int val = Integer.parseInt(intString);
							onButtonPress( val );
						}
						
					};
		} catch (IllegalOperationException e) {
			e.printStackTrace();
		}
		
		// Start initializing the robot to play soccer.
		setState( startState );
	}
	
	
	/**
	 * This embedded class implements the the notion of a position within the world
	 * of robot soccer. It consists of an x-coordinate, a y-coordinate, and an angle. 
	 * All of these are retrievable from the public variables x, y, and a.
	 * 
	 * Taken from BallPusher in the iRobotCreate package.
	 * 
	 * @author Rob Kremer
	 * 
	 */
	class Position {
		public int x, y, a;
		Position(String parsable) throws NumberFormatException, IllegalArgumentException {
			String content[] = parsable.split(",");
			if (content.length!=4) 
				throw new IllegalArgumentException("RobotSoccer.Position("+parsable+"): Expected a comma-separted list of length 4.");
			x = Integer.parseInt(content[1]);
			y = Integer.parseInt(content[2]);
			a = Integer.parseInt(content[3]);
		}
		
		Position(int x1,int y1,int a1)
		{
			x = x1;
			y = y1;
			a = a1;
					
		}
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Position))
				return false;
			Position p = (Position)obj;
			return x==p.x && y==p.y && a==p.a;
		}
		
		 @Override
		 public String toString(){
			 return "("+x+","+y+","+a+")";
		 }
		 
		 @Override
		 public int hashCode() {
			return x*3+y*5+a*7;
			 
		 }
	}
	
	
	/**
	 * This method polls the Camera to get the x-coordinate, y-coordinate, and angle
	 * of the desired shape of the desired color.
	 * 
	 * Taken from BallPusher in the iRobotCreate package.
	 * 
	 * Whenever we ask the camera something, errors can occur
	 * we ask it twice just to make sure the camera gives the same answer
	 * (the camera is not likely to produce the exact same error twice)
	 * 
	 * if the camera is wrong, we ask the same question a certain number of time and we take
	 * the answer that came up the most often.
	 * 
	 * 
	 * @param shape - the shape you wish to know the position for
	 * @param color - the color of the shape you wish to know the position for
	 * @author Rob Kremer
	 * @return - the position of the desired object
	 */
	
	protected Position askCamera(String shape, String color) {
		try {
			
			int nb_try = 10;
			
			Position p1 = null,p2=null;
			Position[] positions = new Position[nb_try];
			
			MLMessage reply1 = sendRequestAndWait(ML.REQUEST, "get-color-position", URLDescriptor.make(cameraPort), ML.CONTENT, shape+","+color);
			if (reply1!=null && isA(reply1.getParameter(ML.PERFORMATIVE),ML.PROPOSE)) {
				p1 = new Position((String)reply1.getParameter(ML.CONTENT));
			}
			
			MLMessage reply2 = sendRequestAndWait(ML.REQUEST, "get-color-position", URLDescriptor.make(cameraPort), ML.CONTENT, shape+","+color);
			if (reply2!=null && isA(reply2.getParameter(ML.PERFORMATIVE),ML.PROPOSE)) {
				p2 = new Position((String)reply2.getParameter(ML.CONTENT));
			}
			
			if (p1.equals(p2))
			{
				return p1;
			}
			else
			{
				for (int i=0;i<nb_try;i++)
				{
					MLMessage reply = sendRequestAndWait(ML.REQUEST, "get-color-position", URLDescriptor.make(cameraPort), ML.CONTENT, shape+","+color);
					if (reply!=null && isA(reply.getParameter(ML.PERFORMATIVE),ML.PROPOSE)) {
						positions[i] = new Position((String)reply.getParameter(ML.CONTENT));
					}
				}
				
				return max_occur(positions);
			}
			
		} catch (Throwable e) {
			println("error", "RobotSoccer.askCamera", e);
		}
		return null;
	}
	
	
	private Position max_occur(Position[] positions) {
		
		HashMap<Position,Integer> hm = new HashMap<Position,Integer>();

	    int value;
		for (int i=0;i<positions.length;i++)
		{
			
			value = 0;
			if (hm.containsKey(positions[i]))
			{
				value = hm.get(positions[i]);
			}
			
			hm.put(positions[i], value+1);
		}
		
		int maximum = 0;
		Position position_star = null;
		for(Entry<Position, Integer> entry : hm.entrySet()) {
			if (entry.getValue()>maximum)
			{
				maximum = entry.getValue();
				position_star = entry.getKey();
			}
		}
		
		return position_star;
	}


	/**
	 * Utility method which calls the askCamera method to find the position
	 * of the puck directly.
	 * 
	 * @return a Position object containing location and angle of puck.
	 */
	protected Position getPuck() {
		return askCamera("circle",ballColour);
	}
	
	
	/**
	 * Utility method which calls the askCamera method to find the position
	 * of the robot directly.
	 * 
	 * @return a Position object containing location and angle of self.
	 */
	protected Position getSelfPosition() {
		return askCamera("triangle",myColour);
	}
	
	
	/**
	 * Utility function for sending a command to the robot agent.
	 * 
	 * This method is based on Rob Kremer's code; see CliffCalibratingController.sendCommand() for his original code.
	 * 
	 * @param command A lisp command, enclosed in a string, to send the robot agent
	 */
	protected void tellRobot( String command ) {
		try {
			
			sendMessage( ML.REQUEST, ML.EXECUTE, getServer()
					, ML.LANGUAGE, "Lisp"
					, ML.CONTENT, command
					);
			
			} catch (Throwable e) {
			println("error", "RobotSoccer.tellRobot", e);
			errors.add( "RobotSoccer.tellRobot: " + e );
		}
	}
	
	
	//************ STATES ***************************
	//***********************************************

	
	/**
	 * Initialize the robot proxy: Load songs and play a short song to indicate the robot is online; 
	 * reset state variables as necessary.
	 * 
	 * After startup, the robot should be in "waiting" state, awaiting commands from the SoccerRobot's Command console.
	 */
	IRobotState startState = new IRobotState( "start" ) {
	
		@Override
		public void enterState() {
			
			System.out.print("");
			
			makeSubthread( new Runnable() {
				@Override
				public void run() {
					try {
						// Load short songs for startup, victory.
						// Credits to http://air.imag.fr/images/1/1b/ImperialMarch.pde.txt
						tellRobot( "(iRobot.execute \"140 1 9 69 32 69 32 69 32 65 22 72 10 69 32 65 22 72 10 69 64\")" );
						tellRobot( "(iRobot.execute \"140 2 9 76 32 76 32 76 32 77 22 68 10 69 32 65 22 72 10 69 64\")" );
						tellRobot( "(iRobot.execute \"140 3 4 57 32 57 32 57 32 53 64\")" );
						
						// Since my machine is slow, give it time to catch up...
						CASAUtil.sleepIgnoringInterrupts( 5000, null );
						
						System.out.println(getURL().getFile()+" enter state start thread started.");
						
						// Turn power LED on.
						tellRobot( "(iRobot.LED 127 255)" );
						
						// Command the robot to sing its startup song. Wait (approximately) for this message to go through, and the song to begin.
						tellRobot( "(iRobot.execute \"141 1\")" );
						CASAUtil.sleepIgnoringInterrupts( 5000, null );
						
						// Turn power LED off.
						tellRobot( "(iRobot.LED 0 0)" );
												
					} catch (Throwable e) {
						println("error", "RobotSoccer.enterState() [state=start]: Unexpected error in state thread", e);
						errors.add( "RobotSoccer.enterState() [state=start]: " + e );
					}

					CASAUtil.sleepIgnoringInterrupts( 5000, null );
					
					System.out.println(getURL().getFile()+" enter state start thread ended.");	
					
					// Set robot in "waiting" state, ready for command input.
					setState( firstAlignState);
				}
				
			}).start();
			
		}
		
		@Override
		public void handleEvent(Sensor sensor, final short reading) {
			// Not needed
		}
		
	};
	
		
	/**
	 * Idle state. Wait for command to begin playing soccer.
	 */
	IRobotState waitingState = new IRobotState( "waiting" ) {
		
		@Override
		public void enterState() {
			
			makeSubthread( new Runnable() {
				@Override
				public void run() {
					try {
						
						System.out.println(getURL().getFile()+" enter state waiting thread started.");
						
						// Ensure robot is active and ready in safe mode.
						tellRobot( "(iRobot.mode 2)" );
						
					} catch (Throwable e) {
						println("error", "RobotSoccer.enterState() [state=waiting]: Unexpected error in state thread", e);
						errors.add( "RobotSoccer.enterState() [state=waiting]: " + e );
					}
					
					System.out.println(getURL().getFile()+" enter state waiting thread ended.");

				}
			}).start();

		}
		
		@Override
		public void handleEvent(Sensor sensor, final short reading) {
			// Not needed
		}
	};
	
	
	/**
	 * This state will align the robot with a point beyond the puck on the line connecting the puck and the center of the goal
	 */
	IRobotState firstAlignState = new IRobotState("firstAlign") {
		
		@Override
		public void enterState() {
			
			makeSubthread( new Runnable() {
				@Override
				public void run() {
					try {
						
						// Poll the location of the robot and of the ball
						selfPosition = getSelfPosition(); 
						puckPosition = getPuck();
						int xGoalCoord = 1152;
						int yGoalCoord = 0; // At the moment, let's just assume we're always going for the top goal...
																
						// Calculate the line connecting the puck and the goal
						double slope = (double)(puckPosition.y - yGoalCoord) / (double)(puckPosition.x - xGoalCoord);
						double intercept = (double)puckPosition.y - (slope * (double)puckPosition.x);
												
						// Find a point further along that line, which is where we want to move to
						int offset = puckPosition.y > yGoalCoord ? 250 : -250;
						int yFurther = puckPosition.y + offset;
						int xFurther = (int)((yFurther - intercept) / slope);
								
						// Check if the the puck lies on the path the robot will take to reach the new point 
						if (intersects(selfPosition.x, selfPosition.y, xFurther, yFurther, puckPosition.x, puckPosition.y)) {
											
							// There will be a collision if we do not adjust course, so first we need to rotate the robot
							// Begin with the angle which will return the robot to angle 0
							int angle = selfPosition.a;
							
							// Depending on whether the robot is above or below the puck, add or remove 90 degrees
							if (selfPosition.y >= xGoalCoord)
								angle += 90;
							else
								angle -= 90;
							
							// Ensure that the angle remains within the range [0,359)
							angle = angle % 360;
							
							// Make the turn as small as possible
							if (angle > 180)
								angle -= 360;
							
							// Now calculcate the distance we want to move
							int distance = Math.abs(selfPosition.y - puckPosition.y) + 250;
							
							// Rotate and move the robot by the calculated angle and distance
							tellRobot("(progn () (irobot.drive 0) (irobot.rotate-deg " + angle + ") (irobot.moveby " + distance + "))");
							
							// Wait a sufficiently long time, and update our position.
							Thread.sleep(15000);
							selfPosition = getSelfPosition(); 

						}
																		
						// Create yet another point on the same line as the robot and the direction its facing
						int xImaginary = selfPosition.x + (int)(100.0 * Math.cos(selfPosition.a * Math.PI / 180));
						int yImaginary = selfPosition.y + (int)(100.0 * Math.sin(selfPosition.a * Math.PI / 180));
												
						/*
						 * Calculate the distances between:
						 * 	i - the robot and the imaginary point
						 * 	j - the imaginary point and the point behind the ball
						 * 	k - the point behind the ball and the robot 
						 */
						double i = distance(selfPosition.x, selfPosition.y, xImaginary, yImaginary);
						double j = distance(xImaginary, yImaginary, xFurther, yFurther);
						double k = distance(xFurther, yFurther, selfPosition.x, selfPosition.y);
												
						// Using the law of cosines, determine the angle opposite j.
						// This is the smallest angle we can use to rotate the robot so its facing the correct direction
						double angle = angle(j, i, k) * 180 / Math.PI;
										
						// But now we need to know if we rotate by that angle, the negation of that angle
						// Firstly, get the angle corresponding to the slope of the line connecting the robot and the point
						int a1 = (int)(Math.acos((selfPosition.x - xFurther) / k) * 180 / Math.PI);
						
						// Since acos only goes from 0 to 180, we need to do a little extra to ensure we get the correct angle
						if (selfPosition.y < yFurther)
							a1 = 360 - a1;
						
						// The second angle is just the opposite of the first
						double a2 = (a1 + 180) % 360;
												
						// Now, if the robot is below the point we want to get to
						if (selfPosition.y > yFurther) {
							
							// Then we negate the angle by which we turn if the robot's angle is in the range (a1, a2)
							if (selfPosition.a > a1 && selfPosition.a < a2) {
								angle = angle * -1;
							}
						}
						// Otherwise, the robot is above the point
						else {
							
							// So we negate the angle by which we turn if the robot's angle is outside the range (a1, a2)
							if (selfPosition.a > a1 || selfPosition.a < a2) {
								angle = angle * -1;
							}
						}
						
						// Now, rotate the robot by the angle we just calculated
						tellRobot("(progn () (irobot.drive 0) (irobot.rotate-deg " + (int)angle + "))");
						
						intendedPosition = new Position("intended," + xFurther + "," + yFurther + "," + "0");
						intendedDistance = k;
						// Now try to actually get there
						setState( firstTraversalState );
						
					} catch (Throwable e) {
						println("error", "RobotSoccer.enterState() [state=optionalBackup]: Unexpected error in state thread", e);
						errors.add( "RobotSoccer.enterState() [state=optionalBackup]: " + e );
					}
				}
			}).start();

		}
		
		@Override
		public void handleEvent(Sensor sensor, final short reading) {
			// Not needed
		}
	};
		
	/**
	 * Traversal state: after calculating a position in firstAlignState, move the robot to
	 * (approximately) that position.
	 */
	IRobotState firstTraversalState = new IRobotState( "firstTraversal" ) {
		
		private Position initialSelfPosition;
		private Position initialPuckPosition;
		
		// Constants for robot travelling speed and margin of error
		private final int allowedDeviation = 25;
		private final int traversalSpeed = 50;
		
		// Time interval for polling the camera (in milliseconds)
		private final double cameraUpdateInterval = 500;
		private double timeInterval = 500;
		
		@Override
		public void enterState() {
			
			makeSubthread( new Runnable() {
				@Override
				public void run() {
					try {
						
						System.out.println(getURL().getFile()+" enter state first traversal thread started.");
						
						// Grab current positions of the robot and puck
						initialSelfPosition = selfPosition;
						initialPuckPosition = puckPosition;
																
						// Wait for the robot to traverse the distance in small increments, polling the camera to check we're on course
						while ( Math.abs( intendedDistance ) > allowedDeviation ) {
						
							// Calculate time interval for polling the camera (in milliseconds)
							// Currently, take the min of t=(distance-to-travel)/(speed) and t=camera-update-interval
							timeInterval = Math.min( ( 1000 * intendedDistance / traversalSpeed ), cameraUpdateInterval );
							
							System.out.println("distance to travel: " + intendedDistance + "\npolling time interval: " + timeInterval );
															
							// Travel forward for the duration of a time interval
							tellRobot( "(progn () (irobot.drive " + traversalSpeed + ") (irobot.execute 155 " + timeInterval / 100 + "))" );

							CASAUtil.sleepIgnoringInterrupts( (long)timeInterval, null );
							tellRobot("(irobot.drive 0)");
							
							// Poll camera for updated positions
							selfPosition = getSelfPosition();
							puckPosition = getPuck();
							
							System.out.println( "distance of ball from orig pos: " + distance( initialPuckPosition.x, initialPuckPosition.y, puckPosition.x, puckPosition.y ) );
							// If the ball moves, break and try firstAlignState again
							if ( Math.abs( distance( initialPuckPosition.x, initialPuckPosition.y, puckPosition.x, puckPosition.y ) ) > allowedDeviation )
								break;
							
							// Recalculate distance to go
							double newDistance = distance( selfPosition.x, selfPosition.y, intendedPosition.x, intendedPosition.y );
							System.out.println( "distance to go: " + newDistance );
							
							// If we seem to be way off-course, break and try firstAlignState again
							if ( Math.abs( newDistance ) > Math.abs( intendedDistance ) )
								break;
							
							// If the robot has stopped moving entirely, poke it again
//							else if ( Math.abs( (int)newDistance ) == Math.abs( (int)intendedDistance ) )
								//tellRobot( "(irobot.drive 0 :flush T)" );
							
							// TODO: If there's an obstacle, do ???
							
							intendedDistance = newDistance;
						}
						
						// Just in case the robot hasn't completed its traversal for some reason, stop it now.
						tellRobot( "(irobot.drive 0 :flush T :emergency T)" );
						
						if ( Math.abs( intendedDistance ) > allowedDeviation ) {
							// Traversal failed...
							setState( firstAlignState );
						}
											
						// If we end up approx. where we want to be, excellent! enter a pushBallState, where we align to and then push the ball.
						else {
							System.out.println("ready to push the ball!!");
							setState( pushBallState );
						}
						
					} catch (Throwable e) {
						println("error", "RobotSoccer.enterState() [state=waiting]: Unexpected error in state thread", e);
						errors.add( "RobotSoccer.enterState() [state=waiting]: " + e );
					}
				
					System.out.println(getURL().getFile()+" enter state first traversal thread ended.");

				}
			}).start();

		}
		
		@Override
		public void handleEvent(Sensor sensor, final short reading) {
			// Not needed
		}
	};
	
	/**
	 * Traversal state: after calculating a position in firstAlignState, move the robot to
	 * (approximately) that position.
	 */
	IRobotState pushBallState = new IRobotState( "pushBall" ) {
		
		private Position initialSelfPosition;
		private Position initialPuckPosition;
		
		// Constants for robot travelling speed and margin of error
		private final int allowedDeviation = 25;
		private final int traversalSpeed = 50;
		
		// Time interval for polling the camera (in milliseconds)
		private double timeInterval = 500;
		
		@Override
		public void enterState() {
			
			makeSubthread( new Runnable() {
				@Override
				public void run() {
					try {
						
						System.out.println(getURL().getFile()+" enter state pushball thread started.");
						
						// Grab current positions of the robot and puck
						selfPosition = getSelfPosition();
						puckPosition = getPuck();
						
						int xGoalCoord = 1152;
						int yGoalCoord = 0; // At the moment, let's just assume we're always going for the top goal...
						
						// Rotate to face goal
						
						// Push ball until goal scored
						
						// Goal scored? If yes, enter victoryState
						
						// If ball is no longer in front of the robot, go back to firstAlignState
						
						
					} catch (Throwable e) {
						println("error", "RobotSoccer.enterState() [state=waiting]: Unexpected error in state thread", e);
						errors.add( "RobotSoccer.enterState() [state=waiting]: " + e );
					}
				
					System.out.println(getURL().getFile()+" enter state pushball thread ended.");

				}
			}).start();

		}
		
		@Override
		public void handleEvent(Sensor sensor, final short reading) {
			// Not needed
		}
	};
	
	/**
	 * Victory state entered once the agent has scored a goal.
	 * The robot plays a jubilant song; and the robot powers down.
	 */
	IRobotState victoryState = new IRobotState( "victory" ) {
		
		@Override
		public void enterState() {
								
			// Display results to controller's Command console
			( (AbstractInternalFrame) getUI() ).getCommandPanel().print( "VICTORY!" );
				
			// Play victory song
			tellRobot( "(iRobot.execute \"141 1\")" );
			CASAUtil.sleepIgnoringInterrupts( 5000, null ); // Wait for song to finish
			tellRobot( "(iRobot.execute \"141 2\")" );
						
			// Power down
			tellRobot( "(iRobot.mode 0)" );			
		}
		
		@Override
		public void handleEvent(Sensor sensor, final short reading) {
			// Not needed
		}
	};
	
	
	/**
	 * Utility method. On pressing the "play" button on the robot,
	 * start or stop a game of soccer, as appropriate.
	 */
	public void onButtonPress(int val) {
		if ( val == 1 ) {
			if ( isStarted )
				ROBOTSOCCER_STOP.execute( this, new ParamsMap(), this.getUI(), null );
			else
				ROBOTSOCCER_START.execute( this, new ParamsMap(), this.getUI(), null );
		}
	}
	
	
	/**
	 * This method takes 2 Position objects, and returns the distance between them
	 * 
	 * @param Some Position a
	 * @param Some Position b
	 * @return The distance between a and b
	 */
	public double distance(int ax, int ay, int bx, int by) {
		
		double xDiff = (double)(ax - bx);
		double yDiff = (double)(ay - by);
		
		return Math.sqrt(Math.abs((xDiff * xDiff) + (yDiff * yDiff)));
	}

	
	/**
	 * This method takes the 3 sides of a triangle, with the longest side as the first argument, and returns the angle in radians opposite the longest side
	 * 
	 * @param a - The longest side of the triangle
	 * @param b - Another side of the triangle 
	 * @param c - Another side of the triangle
	 * @return The angle in radians opposite the longest side of the triangle
	 */
	public double angle(double a, double b, double c) {
		
		assert(a >= b && a >= c);
		
		double cosAngle = (Math.pow(b, 2) + Math.pow(c, 2) - Math.pow(a, 2)) / (2.0 * b * c);
		
		assert(cosAngle >= -1.0 && cosAngle <= 1.0);
		
		return Math.acos(cosAngle);
	}
	
	
	/**
	 * This method takes the coordinates of the robot, the puck and another point, and determines whether
	 * the line connecting the robot and the point intersect the puck, taking radius into account
	 */
	public boolean intersects(int xRobot, int yRobot, int xPoint, int yPoint, int xPuck, int yPuck) {
		
		// These are just guesses at the moment
		double robotRadius = 100;
		double puckRadius = 50;
		
		// Determine the line connected the robot and the point
		double slope = (double)(yRobot - yPoint) / (double)(xRobot - xPoint);
		double intercept = (double)yRobot - (slope * (double)xRobot);

		// Determine the points of the line corresponding to the puck's x and y coordinates
		int x = (int)((yPuck - intercept)/slope);
		int y = (int)((slope * xPuck) + intercept);
				
		// Determine the distance between these points and the puck
		double d1 = distance(x, yPuck, xPuck, yPuck);
		double d2 = distance(xPuck, y, xPuck, yPuck);
		
		// If the distance is less than the combined length of the robot and the puck, return true
		if (d1 < ((robotRadius * 2) + (puckRadius * 2)))
			return true;
		
		if (d2 < ((robotRadius * 2) + (puckRadius * 2)))
			return true;

		// Otherwise, false
		return false;
	}
}