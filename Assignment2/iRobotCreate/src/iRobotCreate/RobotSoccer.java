package iRobotCreate;


import iRobotCreate.iRobotCommands.Sensor;
import iRobotCreate.simulator.CameraSimulation;
import iRobotCreate.simulator.Environment;
import jade.semantics.lang.sl.grammar.Term;

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
	
	// Variable for this robot's target goal. True: top, false: bottom.
	private Boolean whichGoal = true; // Default value of true for testing purposes
	
	// Variable for one-on-one soccer (as opposed to a full two-on-two game)
	private Boolean isSinglePlayer;
	
	// Variable for whether this robot should consider itself an attacker (=0) or defender (=1)
	private static int playerNumber = 0;
	private int myNumber;
	
	// Variables for positions of entities in the environment
	Position selfPosition;
	Position puckPosition;
	Position goalPosition;
	Position secondGoalPosition;
	
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
			
			// Default to attacker (ie. goal-scorer) behaviour.
			// In the case of a double game, the second robot should use defender (ie. goal-guarding) behaviour.
			( ( RobotSoccer ) agent ).myNumber = playerNumber;
				
			// Switch behaviour for the second robot.
			if ( !( ( RobotSoccer ) agent ).isSinglePlayer ) {
				if ( playerNumber == 0 )
					playerNumber++;
				else
					playerNumber = 0;
			}
			
			// Initialized parameters successfully. Begin playing a game of soccer.
			( ( RobotSoccer ) agent ).isStarted = true;
			
			if ( ( ( RobotSoccer ) agent ).myNumber == 0 )
				( ( RobotSoccer ) agent ).setState( ( ( RobotSoccer ) agent ).firstAlignState );
			else if ( ( ( RobotSoccer ) agent ).myNumber == 1 )
				( ( RobotSoccer ) agent ).setState( ( ( RobotSoccer ) agent ).secondAlignState );
			
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
	  			, "INSTREAM", "alice.in"
//	  			, "INSTREAM", "/dev/tty.ElementSerial-ElementSe"
	  			, "OUTSTREAM", "alice.out"
//	  			, "OUTSTREAM", "/dev/tty.ElementSerial-ElementSe"
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
		
		// State which aligns the robot with the goal
		registerState( secondAlignState );
		
		// State which moves the robot to a point just above or below the puck
		registerState( firstTraversalState );
		
		// State which moves the robot to the goal
		registerState( secondTraversalState );
		
		// State which aligns the robot to and then causes it to push the puck
		registerState( pushBallState );
		
		// State which moves the robot within the bounds of the goal
		registerState( patrolState );
				
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
		
		Position(Position pos, Vec3 vec) {
			x = pos.x + (int)vec.x;
			y = pos.y + (int)vec.y;
			a = pos.a;
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
	
	// Vector in 3 dimension (the third dimension is useful to make cross product)
	class Vec3 {
		
		// Default constructor
		public double x, y, z;
		Vec3(double x1, double y1, double z1) {
			x = x1;
			y = y1;
			z = z1;
		}
		
		// Constructor given an angle in degrees
		Vec3(int angle) {
			try {
				x = Math.cos(angle * Math.PI / 180);
				y = Math.sin(angle * Math.PI / 180);
				z = 0;
			} catch (Throwable e) {
				System.out.println("Invalid angle");
			}
		}
		
		// Constructor given 2 positions
		Vec3(Position pos1, Position pos2) {
			x = pos1.x - pos2.x;
			y = pos1.y - pos2.y;
			z = 0;
		}
		
		// Scale the vector by a given value
		public void scale(double s) {
			x = x * s;
			y = y * s;
			z = z * s;
		}
		
		public double dot(Vec3 v1)
		{
			return x*v1.x + y*v1.y + z*v1.z ;
		}
		
		public Vec3 cross(Vec3 v1)
		{
			double vx,vy,vz ;
			double x1 = v1.x, y1= v1.y, z1 = v1.z ;
			
			vx = y*z1-z*y1;
			vy = z*x1-x*z1;
			vz = x*y1-x1*y;
						
			return new Vec3(vx,vy,vz);
		}
		
		public double length()
		{
			return Math.sqrt(x*x + y*y + z*z) ; 
		}
		
		public void normalize() {
			double l = length();
			x = x / l;
			y = y / l;
			z = z / l;
		}
		
		@Override
		public String toString() {
			return "( " + x + ", " + y + ")";
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
//			MLMessage reply1 = sendRequestAndWait(ML.REQUEST, "get-color-position", URLDescriptor.make("136.159.7.26", "9001"), ML.CONTENT, shape+","+color);
			Thread.sleep(1000);
			if (reply1!=null && isA(reply1.getParameter(ML.PERFORMATIVE),ML.PROPOSE)) {
				p1 = new Position((String)reply1.getParameter(ML.CONTENT));
			}
			
			MLMessage reply2 = sendRequestAndWait(ML.REQUEST, "get-color-position", URLDescriptor.make(cameraPort), ML.CONTENT, shape+","+color);
//			MLMessage reply2 = sendRequestAndWait(ML.REQUEST, "get-color-position", URLDescriptor.make("136.159.7.26", "9001"), ML.CONTENT, shape+","+color);
			Thread.sleep(1000);
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
//					MLMessage reply = sendRequestAndWait(ML.REQUEST, "get-color-position", URLDescriptor.make("136.159.7.26", "9001"), ML.CONTENT, shape+","+color);
					Thread.sleep(1000);
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
					
					// Set the goal position
					int yGoal = whichGoal ? 0 : 1382;
					goalPosition = new Position("goal," + 1152 + "," + yGoal + "," + 0);
										
					// Set robot in "waiting" state, ready for command input.
					System.out.println(getURL().getFile()+" enter state start thread ended.");	
					setState( firstAlignState);
//					setState( secondAlignState);

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
						// Determine the initial positions of the robot and the puck
						selfPosition = getSelfPosition();
						puckPosition = getPuck();
																		
						// Determine the vector from the goal to the ball, normalize and scale it by the distance we want
						Vec3 displacement = new Vec3(puckPosition, goalPosition);
						displacement.normalize();
						displacement.scale(250);
												
						// Determine the point to which we want to move
						Position strikePosition = new Position(puckPosition, displacement);
									
						// Proceed as if the ball will never be inbetween ourselves and the point we want to move to
						
						// Determine the unit vector corresponding to the angle of the robot
						Vec3 robotDirectionVector = new Vec3(selfPosition.a);
									
						// Determine the vector between the robot and the strike position
						Vec3 robotStrikeVector = new Vec3(strikePosition, selfPosition);
						robotStrikeVector.normalize();
												
						// Get the angle between the 2 vectors
						double angle = angleBetween(robotDirectionVector, robotStrikeVector);
																	
						// Now, rotate the robot by the angle we just calculated
						tellRobot("(progn () (irobot.drive 0) (irobot.rotate-deg " + (int)angle + "))");
						
						// Make sure we sleep for the duration of the turn
						Thread.sleep(7000);
						
						// Now try to actually get there
						intendedPosition = strikePosition;	// We need this var to tell us where we're headed.
						setState( firstTraversalState );
						
					} catch (Throwable e) {
						println("error", "RobotSoccer.enterState() [state=optionalBackup]: Unexpected error in state thread", e);
						errors.add( "RobotSoccer.enterState() [state=optionalBackup]: " + e );
				
					}
				}
			}).start();

		}
		
		//Although it may not seem necessary at first, we need to handle bumps in this
		//state if the robot goes into the wall when trying to adjust for intersecting
		//with the puck at the beginning of the state.
		@Override
		public void handleEvent(Sensor sensor, final short reading) {
			switch (sensor) {
			case BumpsAndWheelDrops:
				
				/* In case you're curious why a switch statement is here, remember that
				 * a subscription reports any changes in a sensor reading, which also 
				 * encompasses a reading going to 0.
				 */
				
				switch (reading & 3) {
					case 0: //no bumps
						break;
					case 1: //right bump
					case 2: //left bump	
					case 3: //both bumps
						
						//back the robot up, and let's try realigning. Clearly something
						//went wrong in order for us to hit a wall.
						tellRobot("(progn () (irobot.drive 0) (irobot.moveby -50))");
						setState(firstAlignState);
						break;
					default:
						break;
				}
			default:
				break;
			}
		}
	};

	/**
	 * Alignment state for the second, partner robot. 
	 * The robot rotates to align itself with the center of its own goal.
	 * Proceeding into the secondTraversalState causes the robot to actually move toward this goal to guard it.
	 */
	IRobotState secondAlignState = new IRobotState("secondAlign") {
		
		@Override
		public void enterState() {
			
			makeSubthread( new Runnable() {
				@Override
				public void run() {
					try {
						// Determine the initial position of the robot
						selfPosition = getSelfPosition();
						
						// Set its own goal position
						int yGoal = (!whichGoal) ? 0 : 1382;
						
						// We don't want to travel directly into the goal, but patrol in front of it.
						// Adjust for one robot-radius away from the goal.
						if (yGoal == 0)
							yGoal = yGoal + (int) iRobotCommands.chassisRadius + 15;
						else
							yGoal = yGoal - (int) iRobotCommands.chassisRadius - 15;

						secondGoalPosition = new Position("goal," + 1152 + "," + yGoal + "," + 0);
																		
						// Determine the unit vector corresponding to the angle of the robot
						Vec3 robotDirectionVector = new Vec3(selfPosition.a);
									
						// Determine the vector between the robot and the goal position
						Vec3 robotGoalVector = new Vec3(secondGoalPosition, selfPosition);
						robotGoalVector.normalize();
												
						// Get the angle between the 2 vectors
						double angle = angleBetween(robotDirectionVector, robotGoalVector);
																	
						// Now, rotate the robot by the angle we just calculated
						tellRobot("(progn () (irobot.drive 0) (irobot.rotate-deg " + (int)angle + "))");
						
						// Make sure we sleep for the duration of the turn
						Thread.sleep(7000);
						
						// Now try to actually get there
						intendedPosition = secondGoalPosition;
						setState( secondTraversalState );
						
					} catch (Throwable e) {
						println("error", "RobotSoccer.enterState() [state=secondAlign]: Unexpected error in state thread", e);
						errors.add( "RobotSoccer.enterState() [state=secondAlign]: " + e );
				
					}
				}
			}).start();

		}
		
		//Although it may not seem necessary at first, we need to handle bumps in this
		//state if the robot goes into the wall when trying to adjust for intersecting
		//with the puck at the beginning of the state.
		@Override
		public void handleEvent(Sensor sensor, final short reading) {
			switch (sensor) {
			case BumpsAndWheelDrops:
				
				/* In case you're curious why a switch statement is here, remember that
				 * a subscription reports any changes in a sensor reading, which also 
				 * encompasses a reading going to 0.
				 */
				
				switch (reading & 3) {
					case 0: //no bumps
						break;
					case 1: //right bump
					case 2: //left bump	
					case 3: //both bumps
						
						//back the robot up, and let's try realigning. Clearly something
						//went wrong in order for us to hit a wall.
						tellRobot("(progn () (irobot.drive 0) (irobot.moveby -50))");
						setState(secondAlignState);
						break;
					default:
						break;
				}
			default:
				break;
			}
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
		private final int traversalSpeed = 100;
		
		// Time interval for polling the camera (in milliseconds)
		private final double cameraUpdateInterval = 500;
		private double timeInterval = 500;
		
		private boolean traversalFailed;
		
		@Override
		public void enterState() {
			
			makeSubthread( new Runnable() {
				@Override
				public void run() {
					try {

						traversalFailed = false;
						
						initialSelfPosition = getSelfPosition();
						initialPuckPosition = getPuck();
						
						intendedDistance = distance( selfPosition.x, selfPosition.y, intendedPosition.x, intendedPosition.y );						
						
						double newDistance;

						// Attempt to travel toward the intended position behind the ball.
						// Break up the traversal into intervals.
						// Thus, we check the camera for our updated position; travel for one interval; and, if necessary, repeat the process until we reach our intended position.
						do {							
							// Calculate the distance remaining to the intended position behind the ball
							newDistance = distance( selfPosition.x, selfPosition.y, intendedPosition.x, intendedPosition.y );
							( (AbstractInternalFrame) getUI() ).getCommandPanel().print( "distance to go: " + newDistance );
							
							// If we're close enough, stop.
							if ( newDistance <= allowedDeviation )
								break;
							
							// If we seem to be way off-course, break and try firstAlignState again
							if ( Math.abs( newDistance ) > Math.abs( intendedDistance ) ) {
								traversalFailed = true;
								break;
							}
							
							// Determine how far we have to travel, either 150 or the remaining distance, whicher is less
							int driveDistance = (newDistance <= 150) ? (int)newDistance : 150;
							
							// Drive forward by that distance
							tellRobot("(progn () (irobot.drive 0) (irobot.moveby " + driveDistance + "))");
							
							// Wait a fixed duration which will definitely be long enough
							Thread.sleep(5000);
							
							// Poll camera for updated positions
							selfPosition = getSelfPosition();
							puckPosition = getPuck();
							
							( (AbstractInternalFrame) getUI() ).getCommandPanel().print( "distance of ball from orig pos: " + distance( initialPuckPosition.x, initialPuckPosition.y, puckPosition.x, puckPosition.y ) );
							
							// If the ball moves, break and try firstAlignState again
							if ( Math.abs( distance( initialPuckPosition.x, initialPuckPosition.y, puckPosition.x, puckPosition.y ) ) > allowedDeviation ) {
								traversalFailed = true;
								break;
							}
 						}
						while ( newDistance > allowedDeviation );
						
						// Clear robot command queue.
						tellRobot("(irobot.drive 0 :flush T)");
							
						// If all goes well, we are now positioned behind the ball.
						// Enter pushBall state, in which the robot aligns itself behind the ball and begins pushing it toward the goal
						if ( !traversalFailed )
							setState( pushBallState );
											
					} catch (Throwable e) {
						println("error", "RobotSoccer.enterState() [state=firstTraversal]: Unexpected error in state thread", e);
						errors.add( "RobotSoccer.enterState() [state=firstTraversal]: " + e );
					}
				
				}
			}).start();

		}
		
		@Override
		public void handleEvent(Sensor sensor, final short reading) {
			switch (sensor) {
			case BumpsAndWheelDrops:
				
				/* In case you're curious why a switch statement is here, remember that
				 * a subscription reports any changes in a sensor reading, which also 
				 * encompasses a reading going to 0.
				 */
				
				switch (reading & 3) {
					case 0: //no bumps
						break;
					case 1: //right bump
					case 2: //left bump	
					case 3: //both bumps
						
						//back the robot up, and let's try realigning. Clearly something
						//went wrong in order for us to hit a wall.
						tellRobot("(progn () (irobot.drive 0) (irobot.moveby -50))");
						traversalFailed = true;
						setState(firstAlignState);
						break;
					default:
						break;
				}
			default:
				break;
			}
		}
	};
	
	/**
	 * Traversal state for the second, partner robot.
	 * After calculating a goal position in secondAlignState, move the robot to
	 * (approximately) that position in front of its own goal.
	 */
	IRobotState secondTraversalState = new IRobotState( "secondTraversal" ) {
		
		// Constants for robot travelling speed and margin of error
		private final int allowedDeviation = 150;
		private final int traversalSpeed = 100;
		
		// Time interval for polling the camera (in milliseconds)
		private final double cameraUpdateInterval = 500;
		private double timeInterval = 500;
		
		private boolean errorOccurred;
		
		@Override
		public void enterState() {
			
			makeSubthread( new Runnable() {
				@Override
				public void run() {
					try {

						double newDistance;
						errorOccurred = false;

						// Attempt to travel toward the intended position in front of the goal.
						// Break up the traversal into intervals, approximately the same as those of camera updates.
						// Thus, we check the camera for our updated position; travel for one interval; and, if necessary, repeat the process until we reach our intended position.
						do {
							// Poll camera for updated position
							selfPosition = getSelfPosition();
							
							// Calculate the distance remaining to the intended goal-guarding position
							newDistance = distance( selfPosition.x, selfPosition.y, intendedPosition.x, intendedPosition.y );
							( (AbstractInternalFrame) getUI() ).getCommandPanel().print( "distance to go: " + newDistance );
							
							// If we're close enough, stop in front of the goal.
							if ( newDistance <= allowedDeviation )
								break;
							
							// Calculate a time to travel. This will usually be the same as a camera update interval,
							// unless the distance remaining is very small.
							timeInterval = Math.min( ( newDistance / traversalSpeed ), cameraUpdateInterval );
							
							// Calculate the distance we can travel in that interval
							int intervalDistance = Math.min( (int)newDistance, (int)timeInterval * traversalSpeed );

							// Drive forward for the duration of the calculated time.
							tellRobot("(progn () (irobot.drive 0) (irobot.moveby " + intervalDistance + "))");	
							CASAUtil.sleepIgnoringInterrupts( (long)timeInterval+2000, null );
 						}
						while ( newDistance > allowedDeviation );
						
						if ( !errorOccurred ) {
							// Clear robot command queue.
							tellRobot("(irobot.drive 0 :flush T)");
							
							// If all goes well, we are now in front of our own goal, ready to start patrolling.
							// Enter patrolling state, in which the robot moves back and forth in front of the goal.
							setState( patrolState );
						}
																
					} catch (Throwable e) {
						println("error", "RobotSoccer.enterState() [state=secondTraversal]: Unexpected error in state thread", e);
						errors.add( "RobotSoccer.enterState() [state=secondTraversal]: " + e );
					}

				}
			}).start();

		}
		
		@Override
		public void handleEvent(Sensor sensor, final short reading) {
			switch (sensor) {
			case Overcurrents:
				// If we get overcurrents, back up and try aligning to the goal again.
				if ( reading > 0 ) {
					errorOccurred = true;
					tellRobot("(progn () (irobot.drive 0) (irobot.moveby -50))");	
					setState(secondAlignState);
					break;
				}
				
			case BumpsAndWheelDrops:
				
				/* In case you're curious why a switch statement is here, remember that
				 * a subscription reports any changes in a sensor reading, which also 
				 * encompasses a reading going to 0.
				 */
				
				switch (reading & 3) {
					case 0: //no bumps
						break;
					case 1: //right bump
					case 2: //left bump	
					case 3: //both bumps
						errorOccurred = true;
						//back the robot up, and let's try realigning. Clearly something
						//went wrong in order for us to hit a wall.
						tellRobot("(progn () (irobot.drive 0) (irobot.moveby -50))");
						setState(secondAlignState);
						break;
					default:
						break;
				}
			default:
				break;
			}
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
																		
						// Unit vector defined by selfPosition.a
						Vec3 self = new Vec3(Math.cos(selfPosition.a * Math.PI / 180),Math.sin(selfPosition.a * Math.PI / 180),0);
						
						// Vector between goal and self Position
						Vec3 self_goal = new Vec3(goalPosition.x-selfPosition.x,goalPosition.y-selfPosition.y,0);
						
						// The angle between the self vector and self_goal vector
						double angle_pos_goal;
						
						// We calculate the direction of the angle
						// Since the base is not well oriented, the orientation are inversed
						// If the cross product is negative the angle is positive 
						if (self.cross(self_goal).z < 0)
							angle_pos_goal = Math.acos(self.dot(self_goal)/self_goal.length()) ;
						else
							angle_pos_goal = -Math.acos(self.dot(self_goal)/self_goal.length()) ;
						
						// Rotate the robot and wait for a fixed durtation
						tellRobot("(progn () (irobot.drive 0) (irobot.rotate-deg " + (int)(angle_pos_goal*180/Math.PI) + "))");
						Thread.sleep(7000);
						
						// Calculate the distance between the robot and goal
						double remainingDistance = distance(selfPosition.x, selfPosition.y, goalPosition.x, goalPosition.y);
						
						// While there is still distance to go...
						while (remainingDistance > allowedDeviation) {
																				
							// Determine how far we have to travel, either 150 or the remaining distance, which is less.
							int driveDistance = (remainingDistance < 150) ? (int)remainingDistance : 150;
							
							// Drive forward by the previously calculated distance
							tellRobot("(progn () (irobot.drive 0) (irobot.moveby " + driveDistance + "))");
							
							// Wait a fixed duration which will definitely be long enough
							Thread.sleep(5000);
							
							// Poll the camera for the location of ourself and the puck
							selfPosition = getSelfPosition();
							puckPosition = getPuck();
							
							// Update the remaining distance after polling the camera
							remainingDistance = distance(selfPosition.x, selfPosition.y, goalPosition.x, goalPosition.y);
							
							// Ensure the ball is where we expect it to be, if not enter first align state
							if (false)
								setState(firstAlignState);
						}
												
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
	 * Patrolling state for the second, partner robot.
	 * After the robot has left secondTraversalState, it should be positioned in front of its own goal.
	 * In patrolState, the robot then "patrols" in front of the goal, first moving to the right edge of
	 * its goal, and then moving back to the left edge. This state loops back on itself so that the
	 * robot patrols continuously until explicitly stopped by the user.
	 */
	IRobotState patrolState = new IRobotState( "patrol" ) {
		
		// Endpoints of patrolling area
		private final int GOAL_LEFT_EDGE = (2304/ 2) - (int)Math.floor(2304 / 3) / 2;
		private final int GOAL_RIGHT_EDGE = GOAL_LEFT_EDGE + (int)Math.floor(2304 / 3);
		
		// Constants for robot travelling speed and margin of error
		private final int allowedDeviation = 100;
		private final int traversalSpeed = 100;
				
		// Time interval for polling the camera (in milliseconds)
		private final double cameraUpdateInterval = 500;
		private double timeInterval = 500;
		
		@Override
		public void enterState() {
			
			makeSubthread( new Runnable() {
				@Override
				public void run() {
					try {
						
						// Grab current position of the robot
						selfPosition = getSelfPosition();
						
						// Rotate to face right (i.e. a=0 degrees)
						tellRobot("(progn () (irobot.drive 0) (irobot.rotate-deg " + (int)(selfPosition.a) + "))");

						// Make sure we sleep for the duration of the turn
						Thread.sleep(7000);
						
						// Drive until endpoint reached
						double patrolDistance;
						// Attempt to travel toward the intended position at the right edge of the goal.
						// Break up the traversal into intervals, approximately the same as those of camera updates.
						// Thus, we check the camera for our updated position; travel for one interval; and, if necessary, repeat the process until we reach our intended position.
						do {
							// Poll camera for updated position
							selfPosition = getSelfPosition();

							// Calculate remaining distance to travel
							patrolDistance = GOAL_RIGHT_EDGE - selfPosition.x;
							( (AbstractInternalFrame) getUI() ).getCommandPanel().print( "distance to go: " + patrolDistance +"\n my x: " + selfPosition.x + "\n edge: " + GOAL_RIGHT_EDGE);
							
							// If we happen to overshoot the end of the goal, or we're close enough, stop rightward patrol.
							if ( patrolDistance <= allowedDeviation )
								break;
							
							// Calculate a time to travel. This will usually be the same as a camera update interval,
							// unless the distance remaining is very small.
							timeInterval = Math.min( ( patrolDistance / traversalSpeed ), cameraUpdateInterval );
							
							// Calculate the distance we can travel in that interval
							int intervalDistance = Math.min( (int)patrolDistance, (int)timeInterval * traversalSpeed );

							// Drive forward for the duration of the calculated time.
							tellRobot("(progn () (irobot.drive 0) (irobot.moveby " + intervalDistance + "))");	
							CASAUtil.sleepIgnoringInterrupts( (long)timeInterval+2000, null );
 						}
						while ( patrolDistance > allowedDeviation );
						
						// Clear robot command queue.
						tellRobot("(irobot.drive 0 :flush T :emergency T)");
						
						// Rotate to face left (i.e. a=180 degrees)
						tellRobot("(progn () (irobot.drive 0) (irobot.rotate-deg " + (int)(180) + "))");

						// Make sure we sleep for the duration of the turn
						Thread.sleep(7000);
						
						// Drive until endpoint reached
						// Attempt to travel toward the intended position at the left edge of the goal.
						// Break up the traversal into intervals, approximately the same as those of camera updates.
						// Thus, we check the camera for our updated position; travel for one interval; and, if necessary, repeat the process until we reach our intended position.
						do {
							// Poll camera for updated position
							selfPosition = getSelfPosition();

							// Calculate remaining distance to travel
							patrolDistance = selfPosition.x - GOAL_LEFT_EDGE;
							( (AbstractInternalFrame) getUI() ).getCommandPanel().print( "distance to go: " + patrolDistance +"\n my x: " + selfPosition.x + "\n edge: " + GOAL_LEFT_EDGE);
							
							// If we happen to overshoot the end of the goal, or we're close enough, stop rightward patrol.
							if ( patrolDistance <= allowedDeviation )
								break;
							
							// Calculate a time to travel. This will usually be the same as a camera update interval,
							// unless the distance remaining is very small.
							timeInterval = Math.min( ( patrolDistance / traversalSpeed ), cameraUpdateInterval );
							
							// Calculate the distance we can travel in that interval
							int intervalDistance = Math.min( (int)patrolDistance, (int)timeInterval * traversalSpeed );

							// Drive forward for the duration of the calculated time.
							tellRobot("(progn () (irobot.drive 0) (irobot.moveby " + intervalDistance + "))");	
							CASAUtil.sleepIgnoringInterrupts( (long)timeInterval+2000, null );
 						}
						while ( patrolDistance > allowedDeviation );
						
						// Clear the robot command queue.
						tellRobot("(irobot.drive 0 :flush T :emergency T)");
						
						// Reset state so we do it all over again!
						setState( patrolState );
											
					} catch (Throwable e) {
						println("error", "RobotSoccer.enterState() [state=patrol]: Unexpected error in state thread", e);
						errors.add( "RobotSoccer.enterState() [state=patrol]: " + e );
					}

				}
			}).start();

		}
		
		@Override
		public void handleEvent(Sensor sensor, final short reading) {
			switch (sensor) {
			case Overcurrents:
				// If we get overcurrents, back up and try aligning to the goal again.
				if ( reading > 0 ) {
					tellRobot("(progn () (irobot.drive 0) (irobot.moveby -50))");	
					setState(secondAlignState);
					break;
				}
				
			case BumpsAndWheelDrops:
				
				/* In case you're curious why a switch statement is here, remember that
				 * a subscription reports any changes in a sensor reading, which also 
				 * encompasses a reading going to 0.
				 */
				
				switch (reading & 3) {
					case 0: //no bumps
						break;
					case 1: //right bump
					case 2: //left bump	
					case 3: //both bumps
						//back the robot up, and let's try realigning. Clearly something
						//went wrong in order for us to hit a wall.
						tellRobot("(progn () (irobot.drive 0) (irobot.moveby -50))");
						setState(secondAlignState);
						break;
					default:
						break;
				}
			default:
				break;
			}
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
	 * Utility method that will be called once a bump or wheel drop event is encountered.
	 * Override of method in the Controller class.
	 */
	
	@Override
	protected void onBumpsAndWheelDrops(int val) {
		getCurrentState().handleEvent( Sensor.BumpsAndWheelDrops, (short) val );
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
	 * This method takes the coordinates of the robot, the puck and another point, and determines whether
	 * the line connecting the robot and the point intersect the puck, taking radius into account
	 */
	public boolean intersects(Position robotPosition, Position strikePosition, Position puckPosition) {
		
		// These are just guesses at the moment
		double robotRadius = 100;
		double puckRadius = 50;
		
		// Determine the line connected the robot and the point
		double slope = (double)(robotPosition.y - strikePosition.y) / (double)(robotPosition.x - strikePosition.y);
		double intercept = (double)robotPosition.y - (slope * (double)robotPosition.x);

		// Determine the points of the line corresponding to the puck's x and y coordinates
		int x = (int)((puckPosition.y - intercept)/slope);
		int y = (int)((slope * puckPosition.x) + intercept);
				
		// Determine the distance between these points and the puck
		double d1 = distance(x, puckPosition.y, puckPosition.x, puckPosition.y);
		double d2 = distance(puckPosition.x, y, puckPosition.x, puckPosition.y);
		
		// If the distance is less than the combined length of the robot and the puck, return true
		if (d1 < ((robotRadius * 2) + (puckRadius * 2)))
			return true;
		
		if (d2 < ((robotRadius * 2) + (puckRadius * 2)))
			return true;

		// Otherwise, false
		return false;
	}
		
	
	/**
	 * This method takes 2 vectors, and return the angle between then in degrees
	 */
	public double angleBetween(Vec3 vec1, Vec3 vec2) {
		
		double angle = Math.acos(vec1.dot(vec2)/vec2.length());
		Vec3 cross = vec1.cross(vec2);
		
		// If the cross product is greater than 0, negate the angle
		if (cross.z > 0)
			angle =-angle;
		
		return angle * 180 / Math.PI;
	}
	
	
	/**
	 * This method takes an angle in degrees, and returns the smallest equivalent angle
	 */
	int minimizeAngle(int a) {
		int angle = a;
		if (angle > 180)
			angle = angle - 360;
		
		return angle;
	}
}
