package iRobotCreate;


import jade.semantics.lang.sl.grammar.Term;
import casa.LispAccessible;
import casa.ML;
import casa.MLMessage;
import casa.abcl.ParamsMap;
import casa.agentCom.URLDescriptor;
import casa.conversation2.SubscribeClientConversation;
import casa.event.TimeEvent;
import casa.exceptions.IllegalOperationException;
import casa.ui.AgentUI;
import casa.ui.AbstractInternalFrame;
import casa.util.CASAUtil;
import casa.util.Trace;
import casa.Status;
import iRobotCreate.IRobotState;
import iRobotCreate.BallPusher.Position;
import iRobotCreate.iRobotCommands.Sensor;
import iRobotCreate.simulator.CameraSimulation;
import iRobotCreate.simulator.Environment;

/**
 * Blah blah blah...
 *   
 * @author Joel Nielsen
 *         Hugo Richard
 *         Jeffrey Spooner
 *         Sara Williamson
 */
public class RobotSoccer extends StateBasedController {
	
	// Port numbers 
	private final static int cameraPort = 8995;
	private final int robotPort = 9100;
	
	private String myColour = "purple";
	private String ballColour = "red";
	
	// Variables tracking goals scored
	private int playerGoals = 0;
	private int opponentGoals = 0;
	
	public boolean debugMode = false;		// Display debug messages for state on Command console
	
	// Store errors encountered to display on "report" command
	private java.util.LinkedList<String> errors = new java.util.LinkedList<String>();
	
	static { createCasaLispOperators( RobotSoccer.class ); }
	
	
	/**
	 * Lis accessible command to make the agent begin playing robot soccer.
	 * 
	 * NOTE - the name of this method should actually be start, but there appears to be a naming conflict here...
	 * 
	 * @return Status 0 if successful
	 */
	@LispAccessible( name = "begin", help = "Begin playing soccer." )
	public Status begin() {

		// Flush current command queue
		tellRobot( "(iRobot.drive 0 :flush T :emergency T)" );
		
		// Enter first wall-finding state
		setState( testState );
		
		// Success
		return new Status( 0 );
	}

	
	/**
	 * Lisp accessible command to report the agent's state.
	 * Prints current state information, current score, and any errors thus far.
	 * 
	 * @return Status 0 if successful
	 */
	@LispAccessible( name = "report", help = "Prints state information and robot's current state on the controller's command tab." )
	public Status report() {

		// Print state information to controller's command panel
		( (AbstractInternalFrame) getUI() ).getCommandPanel().print( "Current state: " + this.getState().name() );
		
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
	 * Lisp accesible command to reset the agent's state.
	 * Puts the robot in "waiting" state and resets all relevant state variables.
	 * 
	 * @return Status 0 if successful
	 */
	@LispAccessible( name = "reset", help = "Reset all state. Robot enters waiting state, awaiting command-line instructions." )
	public Status reset() {

		// Flush current command queue
		tellRobot( "(iRobot.drive 0 :flush T :emergency T)" );
		
		// Enter waiting state
		setState( waitingState );
		
		// Success
		return new Status( 0 );
	}
	
	
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
			( (AbstractInternalFrame) getUI() ).getCommandPanel().print( "Some debugging information...");
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
		
		//testState exists solely to test out robot functionality until the start LispOperator is implemented effectively
		registerState( testState );
				
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
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Position))
				return false;
			Position p = (Position)obj;
			return x==p.x && y==p.y && a==p.a;
		}
	}
	
	
	/**
	 * This method polls the Camera to get the x-coordinate, y-coordinate, and angle
	 * of the desired shape of the desired color.
	 * 
	 * Taken from BallPusher in the iRobotCreate package.
	 * 
	 * @param shape - the shape you wish to know the position for
	 * @param color - the color of the shape you wish to know the position for
	 * @author Rob Kremer
	 * @return - the position of the desired object
	 */
	protected Position askCamera(String shape, String color) {
		try {
			MLMessage reply = sendRequestAndWait(ML.REQUEST, "get-color-position", URLDescriptor.make(cameraPort), ML.CONTENT, shape+","+color);
			if (reply!=null && isA(reply.getParameter(ML.PERFORMATIVE),ML.PROPOSE)) {
				return new Position((String)reply.getParameter(ML.CONTENT));
			}
		} catch (Throwable e) {
			println("error", "RobotSoccer.askCamera", e);
		}
		return null;
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

					System.out.println(getURL().getFile()+" enter state start thread ended.");	
					
					// Set robot in "waiting" state, ready for command input.
					//setState( waitingState );
					setState( testState );
				}
				
			}).start();
			
		}
		
		@Override
		public void handleEvent(Sensor sensor, final short reading) {
			// Not needed
		}
		
	};
	
	
	//testState exists solely to test out robot functionality until the start LispOperator
	//is implemented effectively. Don't hesitate to delete this little playground once it's served
	//its purpose.
	IRobotState testState = new IRobotState ("test") {
		@Override
		public void enterState() {
			
			//Testing of retrieval of info from camera. Go ahead and delete
			//if it serves your purposes, you won't hurt my feelings.
			
			Position puckPosition = getPuck();
			Position selfPosition = getSelfPosition();
			System.out.println("Puck: " + puckPosition.x + " " + puckPosition.y + " " + puckPosition.a);
			System.out.println("Self: " + selfPosition.x + " " + selfPosition.y + " " + selfPosition.a);
		}
		
		@Override
		public void handleEvent(Sensor sensor, final short reading) {
			
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
}