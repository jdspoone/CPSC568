import jade.semantics.lang.sl.grammar.Term;
import casa.LispAccessible;
import casa.ML;
import casa.abcl.ParamsMap;
import casa.agentCom.URLDescriptor;
import casa.conversation2.SubscribeClientConversation;
import casa.event.TimeEvent;
import casa.exceptions.IllegalOperationException;
import casa.ui.AgentUI;
import casa.util.CASAUtil;
import casa.util.Trace;
import iRobotCreate.iRobotCommands.Sensor;
import iRobotCreate.simulator.Environment;
import iRobotCreate.*;

public class WallMeasurer extends StateBasedController {
	
	// Status variables: length of wall measured (in cm); whether or not virtual wall has been measured; whether or not virtual wall has been detected
	private long wallMeasurement = 0;
	public boolean isVictory = false;
	public boolean foundVirtualWall = false;
	
	// Store errors encountered to display on "report" command
	private java.util.LinkedList<String> errors = new java.util.LinkedList<String>();
	
	static {
		  createCasaLispOperators( WallMeasurer.class );
	  }
	
	/**
	 * Command line lisp command to begin measurement task.
	 * On executing this function, the robot enters its "findWall" state, proceeding toward the nearest wall. It will then traverse the walls until it
	 * detects the virtual wall, and then measures that wall.
	 * 
	 * @return Status 0 if successful
	 */
	@LispAccessible( name = "measure", help = "Begin measurement task. Robot proceeds toward a wall and traverses all walls until it finds the virtual wall; it then measures this wall." )
	public Status measure() {

		// Reset wall measurement, victory conditions prior to new measurement
		wallMeasurement = 0;
		isVictory = false;
		foundVirtualWall = false;
		
		// Enter first wall-finding state
		//setState( findWall );
		
		// Success
		return new Status( 0 );
	}

	/**
	 * Command line lisp command to report on the robot's progress.
	 * Prints current state information ("waiting", "complete", "starting up", "in progress"), current wall measurements, and any errors thus far.
	 * 
	 * @return Status 0 if successful
	 */
	@LispAccessible( name = "report", help = "Prints state information and robot's current state on the controller's command tab." )
	public Status report() {

		String state = "";

		// Check current state
		if ( getCurrentState().equals( waitingState ) )
			state = "waiting";
		else if ( getCurrentState().equals( victoryState ) )
			state = "complete";
		else if ( getCurrentState().equals( startState ) )
			state = "starting up";
		else
			state = "in progress"; // in some search state

		// Print state information to controller's command panel
		( (AbstractInternalFrame) getUI() ).getCommandPanel().print( "Current state: " + state );

		// Check current measurement results; print to command panel
		if ( isVictory )
			( (AbstractInternalFrame) getUI() ).getCommandPanel().print( "\nWall measurement: " + wallMeasurement + " cm \nStatus: complete");
		else if ( wallMeasurement > 0 && foundVirtualWall )
			( (AbstractInternalFrame) getUI() ).getCommandPanel().print( "\nWall measurement: " + wallMeasurement + " cm \nStatus: in progress; virtual wall known)" );
		else if ( wallMeasurement > 0 )
			( (AbstractInternalFrame) getUI() ).getCommandPanel().print( "\nWall measurement: " + wallMeasurement + " cm \nStatus: in progress; virtual wall unknown)" );
		else
			( (AbstractInternalFrame) getUI() ).getCommandPanel().print( "\nWall measurement: unknown" );
		
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
	 * Command line lisp command to reset the robot's state.
	 * Puts the robot in "waiting" state and resets all measurement variables.
	 * To resume, execute (measure).
	 * 
	 * @return Status 0 if successful
	 */
	@LispAccessible( name = "reset", help = "Reset all measurements. Robot enters waiting state, awaiting command-line instructions." )
	public Status reset() {

		// Reset wall measurement, victory conditions
		wallMeasurement = 0;
		isVictory = false;
		foundVirtualWall = false;
		
		// Enter waiting state
		setState( waitingState );
		
		// Success
		return new Status( 0 );
	}

	/**
	   * Start an {@link iRobotCreate} robot at 7778.<br>
	   * Wait for it to be initialized.<br>
	   * Start a {@link WallMeasurer} controller at 7777.<br>
	   * @param args
	   * @author Rob Kremer
	   */
	  public static void main(String[] args) {
	  	iRobotCreate robot = (iRobotCreate)CASAUtil.startAnAgent(iRobotCreate.class, "robot", 7778, null
	  			, "PROCESS", "CURRENT"
	  			, "INSTREAM", "robot.in" //"/dev/tty.iRobot9"
	  			, "OUTSTREAM", "robot.out" //"/dev/tty.iRobot9"
	  			, "TRACE", "10"
	  			, "TRACETAGS", "iRobot9,warning,msg,msgHandling,kb9,eventloop,-info,commitments,-policies9,-lisp,-eventqueue9,-conversations"
	  			);

	  	if (robot==null) {
	  		Trace.log("error", "Cannot start an iRobotCreate agent.");
	  	}
	  	else {
	  		while (!robot.isInitialized())
	  			CASAUtil.sleepIgnoringInterrupts(1000, null);
	  		
	  		if (Environment.exits()) { // we are operating a simulator
	  			try {
	  				Environment env = Environment.getInstance();
	  				env.abclEval("(iRobot-env.new-bounds 4000 4000)", null); 
	  				env.abclEval("(iRobot-env.new \"line1\" \"Line2D\"  500  500 2000 2000 :paint T :corporeal NIL :color #x8888FF)", null);
	  				env.abclEval("(iRobot-env.new \"line2\" \"Line2D\" 2000 2000 1000 3000 :paint T :corporeal NIL :color #x8888FF)", null);
	  			} catch (Exception e) {
	  				System.out.println(robot.println("error", "Environment failed", e));
	  			}
	  		}

	  		WallMeasurer controller = (WallMeasurer)CASAUtil.startAnAgent(WallMeasurer.class, "controller", 7777, null
	  				, "PROCESS", "CURRENT"
	  				, "CONTROLS", ":7778"
	  				, "TRACE", "10"
	  				, "TRACETAGS", "iRobot9,warning,msg,msgHandling,kb9,eventloop,-info,commitments,-policies9,-lisp,-eventqueue9,-conversations"
	  				);
	  		
	  		if (controller==null) {
	  			Trace.log("error", "Cannot create WallMeasurer agent.");
	  		}
	  	}
	  }
	
	
	/**
	 * Initialize a new WallMeasurer controller. Register all possible states.
	 * 
	 * @param params The standard map of parameters to set up the robot.  This is a simple map of (String) keys to values. The
	 * key "CONTROLS" is required and must specify the URL of an {@link iRobotCreate} agent to control.
	 * @param ui
	 * @throws Exception
	 */
	public WallMeasurer( ParamsMap params, AgentUI ui ) throws Exception {
		super( params, ui );

		// Register all valid states for a WallMeasurer agent
		registerState( startState );
		registerState( waitingState );
		registerState( victoryState );
		
		registerState( align1State );
	}
	
	/**
	 * Initialize the controller.
	 * Enter start state.
	 */
	@Override
	public void initializeAfterRegistered( boolean registered ) {
		super.initializeAfterRegistered( registered );
		
		/*
		 * Supposedly this is a way to get sensor readings, but I can't seem to get this to work.
		 */
		try {
			
			@SuppressWarnings("unused")
			SubscribeClientConversation convWall = new SubscribeClientConversation(
					"--subscription-request", 
					this, getServer(), 
					"(all ?x (WallSignal ?x))", null)
							{
								@Override
								public void update(URLDescriptor agentB, Term term) {
									if (term==null || demoOn())
										return;
									String intString = term.toString();
									int val = Integer.parseInt(intString);
									onWallUpdate(val);
								}
							};
							
			@SuppressWarnings("unused")
			SubscribeClientConversation convAngle = new SubscribeClientConversation(
					"--subscription-request", 
					this, getServer(), 
					"(all ?x (angleAcc ?x))", null)
							{
								@Override
								public void update(URLDescriptor agentB, Term term) {
									if (term==null || demoOn())
										return;
									String intString = term.toString();
									int val = Integer.parseInt(intString);
									onAngleAccUpdate(val);
								}
							};
							
		} catch (IllegalOperationException e) {
			e.printStackTrace();
		}
		
		setState( startState );
	}
	
	public void onWallUpdate(int val) {
		getCurrentState().handleEvent( iRobotCommands.Sensor.WallSignal, (short) val ); 
	}
	
	public void onAngleAccUpdate(int val) {
		getCurrentState().handleEvent( iRobotCommands.Sensor.Angle, (short) val ); 
	}
	
	/**
	 * Utility function for sending a command to the robot agent.
	 * @param command A lisp command, enclosed in a string, to send the robot agent
	 */
	protected void tellRobot( String command ) {
		try {
			sendMessage(ML.REQUEST, ML.EXECUTE, getServer()
					, ML.LANGUAGE, "Lisp"
					, ML.CONTENT, command
					);
			} catch (Throwable e) {
			println("error", "WallMeasurer.tellRobot", e);
			errors.add( "WallMeasurer.tellRobot: " + e );
		}
	}
	
	//************ STATES ***************************
	//***********************************************

	/**
	 * Start up the robot proxy. Play a short song to indicate robot is online.
	 * Reset variables as necessary.
	 * Fire a time-out message for after 10 minutes have elapsed.
	 */
	IRobotState startState = new IRobotState( "start" ) {
	
		@Override
		public void enterState() {
			
			makeSubthread( new Runnable() {
				@Override
				public void run() {
					try {
						// Load a cheery tune. Credits to http://air.imag.fr/images/1/1b/ImperialMarch.pde.txt
						tellRobot( "(iRobot.execute \"140 1 9 69 32 69 32 69 32 65 22 72 10 69 32 65 22 72 10 69 64\")" );
						tellRobot( "(iRobot.execute \"140 2 9 76 32 76 32 76 32 77 22 68 10 69 32 65 22 72 10 69 64\")" );
						
						// Load a sad tune.
						tellRobot( "(iRobot.execute \"140 3 4 57 32 57 32 57 32 53 64\")" );
						
						// Since my machine is slow, give it time to catch up...
						CASAUtil.sleepIgnoringInterrupts(10000, null);
						
						System.out.println(getURL().getFile()+" enter state start thread started.");
						
						// Sing a pretty song.
						tellRobot( "(iRobot.execute \"141 1\")" );
						CASAUtil.sleepIgnoringInterrupts(10000, null);
						
						
						// Fire time event for 10 minutes in the future
						try {
							TimeEvent timeout = new TimeEvent( "event", getAgent(), System.currentTimeMillis() + 10 * 60 * 1000 ) {
							 	@Override
								public void fireEvent() {
							 		super.fireEvent();
							 		
							 		// When (if) timeout occurs, and the victory conditions have not been met,
							 		// play a sad song on the robot; turn power LED red
							 		if ( !isVictory ) {
							 			System.out.println( "Oops, timeout occurred..." );
							 			
										// Turn power LED red.
										tellRobot( "(iRobot.LED 255 255)" );
										
										// Sing a pretty sad song.
										tellRobot( "(iRobot.execute \"141 3\")" );
										
							 		}
							 	}
							
							};
							timeout.start();
							
						} catch ( Throwable e ) {
							println( "error", "WallMeasurer.start", e );
							errors.add( "WallMeasurer.start: " + e );
						}
						
					} catch (Throwable e) {
						println("error", "WallMeasurer.enterState() [state=start]: Unexpected error in state thread", e);
						errors.add( "WallMeasurer.enterState() [state=start]: " + e );
					}
					System.out.println(getURL().getFile()+" enter state start thread ended.");	
					
					setState( waitingState );
				}
			}).start();
		}
		
		@Override
		public void handleEvent(Sensor sensor, final short reading) {
			// Not needed
		}
	};
	
	/**
	 * Idle state. Wait for command to begin measurement.
	 */
	IRobotState waitingState = new IRobotState( "waiting" ) {
		
		@Override
		public void enterState() {
			
			makeSubthread( new Runnable() {
				@Override
				public void run() {
					try {
						System.out.println(getURL().getFile()+" enter state waiting thread started.");
						
						// Testing...
						tellRobot( "(iRobot.drive 100)" );
						
					} catch (Throwable e) {
						println("error", "WallMeasurer.enterState() [state=waiting]: Unexpected error in state thread", e);
						errors.add( "WallMeasurer.enterState() [state=waiting]: " + e );
					}
					System.out.println(getURL().getFile()+" enter state waiting thread ended.");
				}
			}).start();
		}
		
		@Override
		public void handleEvent(Sensor sensor, final short reading) {
			// Testing...
			// When the robot hits a wall, begin aligning state
			switch (sensor) {
			case BumpsAndWheelDrops:
				if (reading!=0) {
					setState( align1State );
				}
				break;
			}
		}
	};

	/**
	 * Victory state entered once the virtual wall has been fully measured.
	 * The controller's command panel is updated with the results of the measurement;
	 * the robot plays a jubilant song; and the robot powers down.
	 */
	IRobotState victoryState = new IRobotState( "victory" ) {
		
		@Override
		public void enterState() {
			
			makeSubthread( new Runnable() {
				@Override
				public void run() {
					try {
						// Allow time to catch up...
						CASAUtil.sleepIgnoringInterrupts(10000, null);
						
						System.out.println(getURL().getFile()+" enter state victory thread started.");
						
						// Woot! We measured the wall!
						isVictory = true;
						
						// Display results to controller's Command console
						( (AbstractInternalFrame) getUI() ).getCommandPanel().print( "VICTORY!!\nWall Measured: " + wallMeasurement + " cm" );
						
						// Play victory song
						tellRobot( "(iRobot.execute \"141 1\")" );
						CASAUtil.sleepIgnoringInterrupts( 4000, null );
						tellRobot( "(iRobot.execute \"141 2\")" );
						
						
						// Power down
						tellRobot( "(iRobot.mode 0)" );
						
					} catch (Throwable e) {
						println("error", "WallMeasurer.enterState() [state=victory]: Unexpected error in state thread", e);
						errors.add( "WallMeasurer.enterState() [state=victory]: " + e );
					}
					System.out.println(getURL().getFile()+" enter state victory thread ended.");
				}
			}).start();
		}
		
		@Override
		public void handleEvent(Sensor sensor, final short reading) {
			// Not needed
		}
	};
	
	/**
	 * Aligning state entered the first time the robot bumps up against a wall.
	 * Sara's version - needs testing.
	 */
	IRobotState align1State = new IRobotState( "align1" ) {
		
		@Override
		public void enterState() {
			
			makeSubthread( new Runnable() {
				@Override
				public void run() {
					try {
						System.out.println(getURL().getFile()+" enter state align1 thread started.");
						
						// Stop moving forward
						tellRobot( "(irobot.drive 0 :emergency T)" );
						
						// Back up slightly
						tellRobot( "(iRobot.drive -10)" );
						CASAUtil.sleepIgnoringInterrupts( 1000, null );
						tellRobot( "iRobot.drive 0 :emergency T)" );
						
						// Rotate full circle, recording WallSignal readings.
						calibrating = true;
						tellRobot( "(iRobot.reset-angle-acc)" );
						maxWallSignal = 0;
						angleOfMaxWallSignal = 0;
						System.out.println("Begin calibration...");
						tellRobot( "(iRobot.rotate-deg 360)" );
						
						// Allow time to catch up...
						CASAUtil.sleepIgnoringInterrupts(10000, null);
						
						// Align so strongest WallSignal achieved
						calibrating = false;

						System.out.println("Done calibration...ideal angle: " + angleOfMaxWallSignal );
						tellRobot( "(irobot.rotate-deg " + angleOfMaxWallSignal + ")" );

						// Should be currently at max WallSignal
						angleOfMaxWallSignal = 0;
						
						// set state to traverse wall (temp victory)
						setState( victoryState );
						
					} catch (Throwable e) {
						println("error", "WallMeasurer.enterState() [state=align1]: Unexpected error in state thread", e);
						errors.add( "WallMeasurer.enterState() [state=align1]: " + e );
					}
					System.out.println(getURL().getFile()+" enter state align1 thread ended.");
				}
			}).start();
		}
		
		public boolean calibrating = false;
		public short maxWallSignal = 0;
		public int angleOfMaxWallSignal = 0;
		public int currentAngle = 0;
		
		@Override
		public void handleEvent(Sensor sensor, final short reading) {
			switch (sensor) {
			case WallSignal: // Can't seem to ever trigger this
				System.out.println("Wall signal received: " + reading);
				if ( calibrating && reading > maxWallSignal ) {
					maxWallSignal = reading;
					angleOfMaxWallSignal = currentAngle + angleOfMaxWallSignal;
				}
				break;
			case Angle: // Or this
				System.out.println("Angle received: " + reading);
				if ( calibrating )
					currentAngle = reading;
			}
		}
	};


}
