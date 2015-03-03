package iRobotCreate;

import java.text.ParseException;
import java.util.Observable;

import jade.semantics.lang.sl.grammar.Term;
import casa.LispAccessible;
import casa.ML;
import casa.MLMessage;
import casa.ObserverNotification;
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
import iRobotCreate.iRobotCommands.Sensor;
import iRobotCreate.simulator.Environment;

/**
 * WallMeasurer class, as a subtype of StateBasedController, is used as a controller for a wall-measuring iRobotCreate agent.
 * This controller implements the Command console commands (report), (reset), and (measure). (report) prints current state, measurement data, and any errors/warnings.
 * (reset) returns the robot to an idling state, awaiting commands. (measure) begins a measurement task, in which the robot measures the length (in cm) of some wall indicated
 * by a VirtualWall emitter. It does so by driving forward until bumping into a wall. If the robot does not bump the wall head-on, it resumes wandering until it does hit a wall head-on.
 * It then aligns itself parallel to the wall and traverses this and the following walls, turning corners. Wall measurements are continuous and reset on turning. Once the virtual wall is 
 * detected, and the measurement of this wall completed, the robot celebrates with a victory song.
 * The robot has an internal time limit of 10 minutes from startup for its measurement task; at this point it plays a sad tune, but may continue its measurement.
 *  
 * @author Joel Nielsen
 *         Hugo Richard
 *         Jeffrey Spooner
 *         Sara Williamson
 */
public class WallMeasurer extends StateBasedController {
	
	// Status variables:
	private long wallMeasurement = 0; // length of wall measured (in mm)
	public boolean isVictory = false; // whether or not virtual wall has been measured
	public boolean foundVirtualWall = false; // whether or not the current wall is the virtual wall
	public boolean isFirstWall = true; // whether or not the current wall is the first wall; as we may start our traversal at pretty much any point along this wall, our measurement data is garbage. Ignore it.
	
	public boolean isAlmostVictory = false ; 	// True if the robot is at the beginning of the virtual wall and has only
												//to measure it
	
	// Store errors encountered to display on "report" command
	private java.util.LinkedList<String> errors = new java.util.LinkedList<String>();
	
	static {
		  createCasaLispOperators( WallMeasurer.class );
	  }
	
	/**
	 * Command line lisp command to begin measurement task.
	 * On executing this function, the robot enters its "wandering" state, proceeding toward the nearest wall. It will then traverse the walls until it
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
		isAlmostVictory = false ;
		
		// Flush current command queue
		tellRobot( "(iRobot.drive 0 :flush T :emergency T)" );
		
		// Enter first wall-finding state
		setState( wandering );
		
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
			( (AbstractInternalFrame) getUI() ).getCommandPanel().print( "\nWall measurement: " + getMeasurement() + " cm \nStatus: complete");
		else if ( wallMeasurement > 0 && foundVirtualWall )
			( (AbstractInternalFrame) getUI() ).getCommandPanel().print( "\nWall measurement: " + getMeasurement() + " cm \nStatus: in progress; virtual wall known" );
		else if ( wallMeasurement > 0 )
			( (AbstractInternalFrame) getUI() ).getCommandPanel().print( "\nWall measurement: " + getMeasurement() + " cm \nStatus: in progress; virtual wall unknown" );
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
		isAlmostVictory = false ;
		
		// Flush current command queue
		tellRobot( "(iRobot.drive 0 :flush T :emergency T)" );
		
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
		registerState( traversal1 );
		registerState( victoryState );
		
		registerState( align1State );
		registerState( align1 );
		registerState( align2 );
		registerState( wandering );
		registerState( traversalMeasure );
	}
	
	/**
	 * Initialize the controller agent. Subscribe to the robot proxy for updates regarding
	 * any sensors we care about - namely, Distance, Wall, WallSignal, and VirtualWall.
	 * Finally, commence interaction by entering the start state.
	 */
	@Override
	public void initializeAfterRegistered( boolean registered ) {
		super.initializeAfterRegistered( registered );
				
		/*
		 * Subscribe to the robot proxy.
		 * This results in the appropriate handleEvent() case being triggered in the current controller state
		 * whenever a sensor value is updated.
		 * 
		 * This method of subscribing to the robot proxy is heavily based on code by Rob Kremer; see iRobotCreate.Controller for his original code.
		 */
		try {
			
			// Subscribe to alerts for WallSignal updates
			@SuppressWarnings("unused")
			SubscribeClientConversation convWallSignal = new SubscribeClientConversation(
					"--subscription-request", 
					this, server, 
					"(all ?x (WallSignal ?x))", null)
							{
				
								@Override
								protected void update(URLDescriptor agentB, Term term) {
									if (term==null)
										return;
									String intString = term.toString();
									int val = Integer.parseInt(intString);
									onWallSignal( val );
								}
								
							};
							
			// Subscribe to alerts for Angle updates [Deprecated?]
			@SuppressWarnings("unused")
			SubscribeClientConversation convAngle = new SubscribeClientConversation(
					"--subscription-request", 
					this, server, 
					"(all ?x (Angle ?x))", null)
							{
								/*@Override
								protected void update(URLDescriptor agentB, Term term) {
									if (term==null)
										return;
									String intString = term.toString();
									int val = Integer.parseInt(intString);
								}*/
							};
							
			// Subscribe to alerts for Wall updates
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
							
			// Subscribe to alerts for Distance updates
			@SuppressWarnings("unused")
			SubscribeClientConversation convDistance = new SubscribeClientConversation(
					"--subscription-request", 
					this, server, 
					"(all ?x (Distance ?x))", null)
							{
				
								@Override
								protected void update(URLDescriptor agentB, Term exp) {
									if (exp==null)
										return;
									String intString = exp.toString();
									int val = Integer.parseInt(intString);
									onDistance(val);
								}
								
							};
							
			// Subscribe to alerts for VirtualWall updates
			@SuppressWarnings("unused")
			SubscribeClientConversation convVirtualWall = new SubscribeClientConversation(
					"--subscription-request", 
					this, server, 
					"(all ?x (VirtualWall ?x))", null)
			{
				
				@Override
				protected void update(URLDescriptor agentB, Term exp) {
					if (exp==null)
						return;
					String intString = exp.toString();
					int val = Integer.parseInt(intString);
					onVirtualWall( val );
				}
				
			};
			
			// Subscribe to alerts for distanceAcc updates
			@SuppressWarnings("unused")
			SubscribeClientConversation convDistanceAcc = new SubscribeClientConversation(
					"--subscription-request", 
					this, server, 
					"(all ?x (distanceAcc ?x))", null)
			{
				
				@Override
				protected void update(URLDescriptor agentB, Term exp) {
					if (exp==null)
						return;
					String intString = exp.toString();
					int val = Integer.parseInt(intString);
					onDistanceAcc( val );
				}
				
			};
							
							
		} catch (IllegalOperationException e) {
			e.printStackTrace();
		}
		
		// Start initializing the robot for its measurement task.
		setState( startState );
		
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
			println("error", "WallMeasurer.tellRobot", e);
			errors.add( "WallMeasurer.tellRobot: " + e );
		}
	}
	
	/**
	 * Modified update method to check for sensor readings.
	 * Use this in case other forms of update (ie. SubscriptionClientConversation) are borked.
	 * The controller examines the content of incoming inform-ref messages, and, upon notification
	 * from one of the sensors of interest, it will trigger an event handler for that sensor update.
	 */
	/* [Deprecated]
	public void update( Observable o, Object arg ) {
        // Check that the event arg is a proper notification and not garbage
        if ( arg instanceof ObserverNotification ) {
         
            ObserverNotification aNotification = ( ObserverNotification ) arg;
             
            // Check that the notification comes from the observed subject
            if ( aNotification.getAgent() == this ) {
             
                // Check if we were notified of a message being received by the subject
                if ( aNotification.getType().equals( ML.EVENT_MESSAGE_RECEIVED ) ) {
                     
                    MLMessage message = ( MLMessage ) aNotification.getObject();
                    
                    try {
                    	// Only bother with messages that might be sensor updates
						if ( message.getParameter( ML.PERFORMATIVE ) == ML.INFORM_REF ) {
							System.out.println("Message: " + message.getContent() );
							
							// WallSignal update
							if ( message.getParameter( ML.CONTENT ).startsWith( "((WallSignal" ) )
								getCurrentState().handleEvent( iRobotCommands.Sensor.WallSignal, (short) Integer.parseInt( message.getParameter( ML.CONTENT ).substring( 13, message.getParameter( ML.CONTENT ).length() - 2 ) ) );
							
							// Angle update
							else if ( message.getParameter( ML.CONTENT ).startsWith( "((Angle " ) )
								getCurrentState().handleEvent( iRobotCommands.Sensor.Angle, (short) Integer.parseInt( message.getParameter( ML.CONTENT ).substring( 8, message.getParameter( ML.CONTENT ).length() - 2 ) ) );
							
							else if ( message.getParameter( ML.CONTENT ).startsWith( "((VirtualWall " ) ) {
								// Debugging: display state
								( (AbstractInternalFrame) getUI() ).getCommandPanel().print( "VirtualWall: " + message.getContent() );
								getCurrentState().handleEvent( iRobotCommands.Sensor.Angle, (short) Integer.parseInt( message.getParameter( ML.CONTENT ).substring( 14, message.getParameter( ML.CONTENT ).length() - 2 ) ) );
								
							}
						}
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                }
            }
        }
    }*/
	
	//************ STATES ***************************
	//***********************************************

	/**
	 * Initialize the robot proxy: Load songs and play a short song to indicate the robot is online; 
	 * reset state variables as necessary; and fire a time-out message scheduled for ten minutes in the future.
	 * 
	 * After startup, the robot should be in "waiting" state, awaiting commands from the WallMeasurer's Command console.
	 */
	IRobotState startState = new IRobotState( "start" ) {
	
		@Override
		public void enterState() {
			
			makeSubthread( new Runnable() {
				@Override
				public void run() {
					try {

						// Load short songs for startup, victory, and time-out.
						// Credits to http://air.imag.fr/images/1/1b/ImperialMarch.pde.txt
						tellRobot( "(iRobot.execute \"140 1 9 69 32 69 32 69 32 65 22 72 10 69 32 65 22 72 10 69 64\")" );
						tellRobot( "(iRobot.execute \"140 2 9 76 32 76 32 76 32 77 22 68 10 69 32 65 22 72 10 69 64\")" );
						tellRobot( "(iRobot.execute \"140 3 4 57 32 57 32 57 32 53 64\")" );
						
						// Since my machine is slow, give it time to catch up...
						CASAUtil.sleepIgnoringInterrupts( 10000, null );
						
						System.out.println(getURL().getFile()+" enter state start thread started.");
						
						// Command the robot to sing its startup song. Wait (approximately) for this message to go through, and the song to begin.
						tellRobot( "(iRobot.execute \"141 1\")" );
						CASAUtil.sleepIgnoringInterrupts( 10000, null );
						
						
						// Fire a one-time TimeEvent scheduled for 10 minutes in the future
						try {
							
							TimeEvent timeout = new TimeEvent( "event", getAgent(), System.currentTimeMillis() + 10 * 60 * 1000 ) {

								/**
								 * When timeout occurs, indicate this by turning the power LED red and playing a pathetic song.
								 */
								@Override
								public void fireEvent() {
							 		super.fireEvent();
							 		
							 		// When (if) timeout occurs, and the robot has not yet succeeded in measuring the virtual wall,
							 		// play a sad song on the robot; turn power LED red
							 		if ( !isVictory ) {
							 			
										// Turn power LED red.
										tellRobot( "(iRobot.LED 255 255)" );
										
										// Sing a pretty sad song.
										tellRobot( "(iRobot.execute \"141 3\")" );
										
							 		}
							 	}
							
							};
						
							timeout.start();
							
						} catch ( Throwable e ) {
							println("error", "WallMeasurer.enterState() [state=start]: Unexpected error in state thread", e);
							errors.add( "WallMeasurer.enterState() [state=start]: " + e );
						}
						
					} catch (Throwable e) {
						println("error", "WallMeasurer.enterState() [state=start]: Unexpected error in state thread", e);
						errors.add( "WallMeasurer.enterState() [state=start]: " + e );
					}

					System.out.println(getURL().getFile()+" enter state start thread ended.");	
					
					// Set robot in "waiting" state, ready for command input.
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
						
						// Ensure robot is active and ready
						tellRobot( "(iRobot.mode 2)" );
						
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
			// Not needed
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
							isFirstWall = true; // Align to traverse this new wall. Since it is the first wall, we might be anywhere along its length, so measurement will be incomplete.
												// Set this variable so we don't send off any incomplete reports or victory flags.
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
							setState(traversal1);
						}
					case 1:
						wallSeen = true;
					}
				default:
					break;
				}
			}
		};
			
	// First traversal state
	IRobotState traversal1 = new IRobotState("traversal1") {
			
		private final int allowedDeviation = 25;
		private final int correctionAngle = 3;
			
		private int initialWallSignal = 0;
		private int initialWallDistanceAcc = 0;
			
		public void enterState() {
			initialWallDistanceAcc = 0;
			wallMeasurement = 0;

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
						
					switch (reading & 3) {
						case 0:
							break;
						case 1:
						case 2:
							// This should never happen now...
							tellRobot( "(progn () (irobot.drive 0 :flush T) (irobot.moveby -20) (irobot.rotate-deg 7) (irobot.drive 30))" );
							break;
						case 3: // Hit a corner
								
//							wallMeasurement = 0 ;
							// If this is not the first wall we hit (ie. we have completed a full measurement of this wall)
							// and this wall is marked by the virtual wall signal, congratulations! Measurement task is complete.
							if ( !isFirstWall && foundVirtualWall )
								setState( victoryState );
							
							// Otherwise, turn the corner; align to the new wall
							isFirstWall = false; // We can traverse this new wall fully, corner to corner
							tellRobot("(irobot.moveby -20)");
							setState(align1);
								
							break;
						default:
							break;
					}
							
				/*case Distance:					
							
					// Update the length of the wall we're currently measuring
					wallMeasurement += (int)reading;
					break;*/
					
				case Unused1: // DistanceAcc update
					
					// Update length of wall we're currently traversing
					if ( initialWallDistanceAcc == 0 )
						initialWallDistanceAcc = (int) reading;
					else
						wallMeasurement = (int) reading - initialWallDistanceAcc;
					break;
													
				case WallSignal:
												
					// If we haven't set our initial wall signal
					int signal = (int)reading;
					if (initialWallSignal == 0) {
								
						// And the current reading is greater than 0
						if (signal > 0) {
							System.out.println("Setting inital wall signal to " + (int)reading);
							initialWallSignal = signal;
						}
					}
							
					// Otherwise, ensure we are not deviating from that inital wall reading too much
					else {
								
						// If the given reading is outside of the allowed deviation, adjust course
						if (signal > (initialWallSignal + allowedDeviation) || signal < (initialWallSignal - allowedDeviation)) {
							int correctionFactor = signal > initialWallSignal ? correctionAngle : -correctionAngle;
							System.out.println("Adjusting course by " + correctionFactor);
							tellRobot( "(irobot.rotate-deg " + correctionFactor + ")" );
							tellRobot( "(irobot.drive 30)" );
						}
					}
							
					break;
						
				case VirtualWall:
					
					// Debugging: display state
					( (AbstractInternalFrame) getUI() ).getCommandPanel().print( "VirtualWall: " + reading );
					
					foundVirtualWall = true;
					
					
				default:
					break;
			}
		}
	};

		
	/* This state is supposed to replace the traversal state.
	 * The robot goes along the wall.
	 * If it sees the VirtualWall, it goes into the opposit direction
	 * and notice he now knows where the virtual wall is.
	 *  The next time the robot hits a corner it will be at the beginning of the Virtual 
	 *  Wall it has to measure which is almost a victory. 
	 *  When the robot hits a second corner it means it has reached the other side of the wall
	 *  and the measurement is done
	 *  
	 *  This still part needs improvement
	 */
	IRobotState traversalState2 = new IRobotState("traversalState") {
		
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
				
				case VirtualWall:
					switch (reading){
					case 0:
						break;
					case 1:
						foundVirtualWall = true;
						setState(traversalMeasure);
					}
					
				case BumpsAndWheelDrops:
					
					switch (reading & 3) {
					case 0:
						break;
					case 1:
					case 2:
						tellRobot( "(progn () (irobot.drive 0 :flush T) (irobot.moveby -20) (irobot.rotate-deg 7) (irobot.drive 30))" );
						break;
					case 3:
						wallMeasurement = 0 ;
						//just tell the robot to turn 90 degrees. Align doesn't work well with
						//the corner turn because it requires being really close to the wall,
						//which is not guaranteed by the time the robot gets close to the end.
						tellRobot( "(progn () (irobot.drive 0 :flush T) (irobot.moveby -20) (irobot.rotate-deg 90) (irobot.drive 30))");
						if (isAlmostVictory = true)
						{
							setState(victoryState);
						}
						if (foundVirtualWall){
							isAlmostVictory = true;
						}
						
						break;
					default:
						break;
					}
					
				case Distance:					
					
					// Update the length of the wall we're currently measuring
					wallMeasurement += (int)reading;
					break;
					
				case Wall:
					
					if (reading == 0) {
						tellRobot( "(irobot.drive 0 :flush T)");
						setState(align2);
					}
					
				default:
					break;
			}
			
		}
	};

	IRobotState traversalMeasure = new IRobotState("traversalMeasure") {

		public void enterState() {
			tellRobot( "(progn () (irobot.drive 0 :flush T) (irobot.moveby -20) (irobot.rotate-deg 180) (irobot.drive 30))");
		}
		
		@Override
		public void handleEvent(Sensor sensor, short reading) {
			switch(sensor) {
			case Wall:
				if (reading == 0) {
					tellRobot("(irobot.drive 0 :flush T");
					setState(traversalState2);
				}
			default:
				break;
			}
			
		}
		
	};
	
	IRobotState align2 = new IRobotState("align2") {

		public void enterState() {
			tellRobot("(irobot.drive 5 -1)");
		}
		
		@Override
		public void handleEvent(Sensor sensor, short reading) {
			switch(sensor) {
			case Wall:
				if (reading == 1) {
					tellRobot("(irobot.drive 0 :flush T");
					setState(traversal1);
				}
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
						( (AbstractInternalFrame) getUI() ).getCommandPanel().print( "VICTORY!!\nWall Measured: " + getMeasurement() + " cm" );
						
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
	 * Sara's version - currently dysfunctional. needs fixing and re-testing.
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
						tellRobot( "(iRobot.drive 10 1)" );
						
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
			case WallSignal: 
				System.out.println("Wall signal received: " + reading);
				if ( calibrating && reading > maxWallSignal ) {
					maxWallSignal = reading;
					angleOfMaxWallSignal = currentAngle + angleOfMaxWallSignal;
				}
				break;
			case Angle: 
				System.out.println("Angle received: " + reading);
				if ( calibrating )
					currentAngle = reading;
			}
		}
	};


	/** This gets called once the controller is notified of a bump or a wheel drop.
	 * This is an override of a method in the Controller class. 
	 * @param int, the updated BumpsAndWheelDrops reading
	 */	
	@Override
	protected void onBumpsAndWheelDrops(int val) {
		getCurrentState().handleEvent( Sensor.BumpsAndWheelDrops, (short) val );
	}
	
	/** This gets called once the controller receives notification of a change in the
	 * value of wall sensor.
	 * @param int, the updated Wall reading
	 */
	protected void onWall(int val) {
		getCurrentState().handleEvent( Sensor.Wall, (short) val );
	}


	/**
	 * This gets called once the controller is notified of a Distance sensor value update.
	 * @param val int, the updated Distance reading
	 */
	protected void onDistance(int val) {
		getCurrentState().handleEvent( Sensor.Distance, (short) val );
	}
	
	/**
	 * This gets called once the controller is notified of an accumulated distance update.
	 * Note that we make use of sensor Unused1 here, since distanceAcc is a software tally and not
	 * an actual sensor on the robot.
	 * @param val int, the updated accumulated distance
	 */
	protected void onDistanceAcc(int val) {
		getCurrentState().handleEvent( Sensor.Unused1, (short) val );
	}
	
	/**
	 * This gets called once the controller is notified of a VirtualWall sensor value update.
	 * @param val int, the updated VirtualWall reading
	 */
	protected void onVirtualWall(int val) {
		getCurrentState().handleEvent( Sensor.VirtualWall, (short) val );
	}
	
	/**
	 * This gets called once the controller is notified of a WallSignal sensor value update.
	 * @param val int, the updated WallSignal reading
	 */
	protected void onWallSignal(int val) {
		getCurrentState().handleEvent( Sensor.WallSignal, (short) val );
	}

	/**
	 * Return the current measurement value, in cm, for the current wall.
	 * 
	 * @return long Measurement thus far of the wall the robot is currently traversing
	 */
	public long getMeasurement() {
		return wallMeasurement / 10;
	}

}
