package iRobotCreate;

import java.util.Observable;

import jade.semantics.lang.sl.grammar.Term;
import casa.LispAccessible;
import casa.ML;
import casa.ObserverNotification;
import casa.abcl.ParamsMap;
import casa.agentCom.URLDescriptor;
import casa.conversation2.SubscribeClientConversation;
import casa.event.TimeEvent;
import casa.exceptions.IllegalOperationException;
import casa.jade.BeliefObserver;
import casa.ui.AgentUI;
import casa.ui.AbstractInternalFrame;
import casa.util.CASAUtil;
import casa.util.Trace;
import casa.Status;
import iRobotCreate.iRobotCommands.Sensor;
import iRobotCreate.simulator.Environment;
import iRobotCreate.*;

public class WallMeasurer extends StateBasedController {
	
	private iRobotCreate robot;
	
	private long wallMeasurement = 0;
	public boolean isVictory = false;
	public boolean foundVirtualWall = false;
	
	// Store errors encountered to display on "report" command
	private java.util.LinkedList<String> errors = new java.util.LinkedList<String>();
	
	static {
		  createCasaLispOperators( WallMeasurer.class );
	  }
	
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
	   * Start a {@link LineFollower} controller at 7777.<br>
	   * @param args
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
	  				env.abclEval("(iRobot-env.new-bounds 2000 2000)", null); 
	  				env.abclEval("(iRobot-env.new \"vWall field\"  \"Rectangle2D\"  1000  500 300 1000 :vwall T :corporeal NIL :color #xF4FFF4)", null);
	  				env.abclEval("(iRobot-env.new \"vWall emitter\"  \"Rectangle2D\"  1000  1050  50 100 :corporeal T :color #xFF00FE)", null);
	  				env.abclEval("(iRobot-env.new \"p1\"  \"Line2D\"  500  1500 1500  1500 :paint T :corporeal NIL :color #x202020)", null);
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
	  		
	  		controller.robot = robot;
	  	}
	  }
	  
	
	private int wallLength = 0;
	
	/**
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
		registerState( traversalState );
		registerState( victoryState );
		registerState( align1 );
		registerState( wandering );
	}
	
	@Override
	public void initializeAfterRegistered( boolean registered ) {
		super.initializeAfterRegistered( registered );
		
		setState( startState );
		try {
			@SuppressWarnings("unused")
			SubscribeClientConversation convWall = new SubscribeClientConversation(
					"--subscription-request", 
					this, server, 
					"(all ?x (Wall ?x))", null)
							{
								@Override
								protected void update(URLDescriptor agentB, Term exp) {
									if (exp==null)
										return;
									String intString = exp.toString();
									int val = Integer.parseInt(intString);
									onWall(val);
								}
							};
							
							
		} catch (IllegalOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		private boolean commandSent = false;
	
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
							TimeEvent timeout = new TimeEvent( "TimeEvent", getAgent(), System.currentTimeMillis() + 10 * 60 * 1000 ) {
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
					
					setState( wandering );
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
						tellRobot( "(iRobot.drive 10 10)" );
						
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
			switch (sensor) {
			case Overcurrents:
			case BumpsAndWheelDrops:
			}
		}
	};
	
	/*
	 * This state is what sends the robot faffing about whatever space it's been put into.
	 * Left and right bumps by themselves make the robot back up a bit and adjust its angle
	 * to go elsewhere. Once we arrive at a wall head on enough to activate both the left
	 * and right bumpers, the robot backs up a bit and goes into the first alignment state.
	 */
	
	IRobotState wandering = new IRobotState("wandering") {
		@Override
		public void enterState() {
			tellRobot("(progn () (irobot.drive 500))");
		}
		@Override
		public void handleEvent(Sensor sensor, short shortness) {
			switch (sensor) {
				case BumpsAndWheelDrops:
					int deg = 0;
					switch (shortness & 3) {
						case 0: //no bumps
							deg = 0;
							break;
						case 1: //right bump
							deg = 30;
							break;
						case 2: //left bump
							deg = -75;
							break;
						case 3: //both bumps
							tellRobot("(progn () (irobot.drive 0) (irobot.moveby -20))");
							setState(align1);
					}
					
					//if we have a degree to adjust by that's greater than 0, let's go ahead
					//and adjust the robot and keep going. The check is necessary otherwise
					//this event is going to kick in again once the bump sensor starts reading 0.
					
					if (deg != 0) {
						tellRobot("(progn () (irobot.drive 0 :flush T) (irobot.moveby -50) (irobot.rotate-deg "+deg+") (irobot.drive 500))");
					}
					break;
				default:
				    break;
				}
			}
		};
		
		/* This state aligns the robot to be parallel with the wall (hopefully!) after the robot
		 * makes contact with the wall relatively head on. The basic concept is that it rotates in place
		 * until the binary wall sensor switches to 0. After that, it rotates back a set angle
		 * to align itself.
		 * 
		 * The major snag with this method is that due to the queuing delays inherit in passing messages
		 * between the controller and the robot agent, there's no guarantee of the robot stopping in a 
		 * consistent position. Having the robot turn as slowly as possible mitigates this somewhat. 
		 */
		
		private final int rotateBack = -27; //degrees to rotate back
		IRobotState align1 = new IRobotState("align1") {
			
			
			boolean wallSeen = false; // this may not be strictly necessary
			public void enterState() {
				tellRobot("(irobot.drive 15 1))"); //let's turn slowly in place
			}
			
			public void handleEvent(Sensor sensor, short shortness) {
				switch(sensor) {
				case Wall:
					switch (shortness) {
					case 0:
						if (wallSeen) {
							tellRobot("(progn () (irobot.drive 0 :flush T) (irobot.rotate-deg "+rotateBack+"))");
							setState(traversalState);
						}
					case 1:
						wallSeen = true;
					}
				default:
					break;
				}
			
			}
		};
			
	/**
	 * First traversal state
	 */
	IRobotState traversalState = new IRobotState("traversalState") {
		
		public void enterState() {
			// We're not concerned with measuring the wall we are traversing, begin moving forward.
			tellRobot( "(irobot.drive 30)" );
		}
		
		public void handleEvent(Sensor sensor, short reading) {
			
			switch (sensor) {
			
				// At the moment, let's treat overcurrent and bumps/wheeldrops the same way
				case Overcurrents:
					
					// We want to ignore sensor readings of zero 
					if (reading == 0)
						break;
					
				case BumpsAndWheelDrops:
					
					
					if ((reading & 3) == 0)
						break;					
					
					// Stop the robot
					tellRobot( "(irobot.drive 0)" );
					
					// Back up a little bit
					tellRobot( "(irobot.moveby -20)" );
					
					// Transition to align2
//					setState(alignmentState;
					
					break;
										
				default:
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
	
	/* This gets called once the controller is notified of a bump or a wheel drop.
	   This is an override of a method in the Controller class. */
	
	@Override
	protected void onBumpsAndWheelDrops(int val) {
		getCurrentState().handleEvent(Sensor.BumpsAndWheelDrops, (short)val);
	}
	
	/* This gets called once the controller receives notification of a change in the
	 * value of wall sensor.
	 */
	
	protected void onWall(int val) {
		getCurrentState().handleEvent(Sensor.Wall, (short)val);
	}

}
