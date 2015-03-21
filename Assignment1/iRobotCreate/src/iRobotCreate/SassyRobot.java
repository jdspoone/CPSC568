package iRobotCreate;

/* For invaluable instructions on how to run this in Eclipse, please refer to page 8
 * of the Casa IRobot User Manual on the iRobot section of the CASA website. Use
 * the modified wallMeasure.lisp script that should HOPEFULLY be accompanying this push
 */

import iRobotCreate.iRobotCommands.Sensor;
import casa.ML;
import casa.agentCom.URLDescriptor;
import casa.abcl.ParamsMap;
import casa.conversation2.SubscribeClientConversation;
import casa.event.TimeEvent;
import casa.exceptions.IllegalOperationException;
import casa.ui.AgentUI;
import iRobotCreate.IRobotState;
import jade.semantics.lang.sl.grammar.Term;

public class SassyRobot extends StateBasedController { //extending StateBased Controller
													   //is needed for setState and getState
													   //This will be very useful!

	public SassyRobot(ParamsMap params, AgentUI ui) throws Exception {
		super(params, ui);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initializeAfterRegistered(boolean registered) {
		super.initializeAfterRegistered(registered);
		
		/* For what will go here, please refer to the Controller class in iRobotCreate.
		   We need to subscribe our controller to the sensor readings we care about. Controller
		   has one for bumps and wheel drops already. These act like observers, of a sort.
		   After the controller gets a notification, it fires an appropriate method. We have
		   different states we'll be in, but we can use getCurrentState() that comes with
		   StateBasedController and call the handleEvent method that each IRobotState
		   needs to declare. For an example of what I'm talking about, look at the commented out 
		   onBumpsAndWheelDrop method included in LineFollower.
		*/
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
		
		setState(goForTheRecord);
	}
	
	

	/*This state now bounces around the room until it gets both left and right bumpers activated.
	  Then it backs up and goes into the aligning1 state.
	*/
	IRobotState goForTheRecord = new IRobotState("goForTheRecord") {
		@Override
		public void enterState() {
			//memorize this form because you can be darned sure we're going to be using this a lot.
			sendMessage(ML.REQUEST, ML.EXECUTE, server, ML.LANGUAGE, "lisp", ML.CONTENT, "(progn () (irobot.mode 2) (irobot.drive 150))");
		}
	
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
							sendMessage(ML.REQUEST, ML.EXECUTE, server, ML.LANGUAGE, "lisp", ML.CONTENT, "(progn () (irobot.drive 0 :flush T) (irobot.moveby -20))");
							setState(align1);
					}
					
					if (deg > 0) {
						sendMessage(ML.REQUEST, ML.EXECUTE, server 
								,ML.LANGUAGE, "lisp"
								,ML.CONTENT, "(progn () (irobot.drive 0) (irobot.moveby -50) (irobot.rotate-deg "+deg+") (irobot.drive 100))"
							  );
					}
					
					break;
				default:
				    break;
				}
			}
		};
		
		@Override
		protected void onBumpsAndWheelDrops(int val) {
			getCurrentState().handleEvent(Sensor.BumpsAndWheelDrops, (short)val);
		}
		
		protected void onWall(int val) {
			getCurrentState().handleEvent(Sensor.Wall, (short)val);
			
		}
	

	
	// Skeleton for begin state
	IRobotState begin = new IRobotState("begin") {
		public void handleEvent(Sensor sensor, short shortness) {
		}
	};
	
	
	// Skeleton for findWall state
	IRobotState find = new IRobotState("find") {
		public void handleEvent(Sensor sensor, short shortness) {
		}
	};
	
	
	// This is the first pass for the align1 state.
	// What it does is spin in place until it no longer detects the wall.
	// Then it adjusts back a set angle to hopefully be aligned with the wall.
	// Unfortunately due to the nature of messages being passed, there's no
	// guarantee of how long it will take until Mr. Robot actually goes ahead
	// and stops turning when the wall sensor reads 0.
	// Regardless of this, the method is kinda janky and easy to break to begin with.
	// Hopefully better ideas will hit me. Perhaps using the wall sensor and finding out
	// the "sweet spot" through trial and error.
	IRobotState align1 = new IRobotState("align1") {
		
		boolean wallSeen = false;
		public void enterState() {
			sendMessage(ML.REQUEST, ML.EXECUTE, server, ML.LANGUAGE, "lisp", ML.CONTENT, "(progn () (irobot.drive 0 :flush T) (irobot.drive 15 1))");
		}
		
		public void handleEvent(Sensor sensor, short shortness) {
			switch(sensor) {
			case Wall:
				switch (shortness) {
				case 0:
					if (wallSeen) {
						sendMessage(ML.REQUEST, ML.EXECUTE, server, ML.LANGUAGE, "lisp", ML.CONTENT, "(progn () (irobot.drive 0 :flush T) (irobot.rotate-deg -38))");
						setState(end);
					}
				case 1:
					wallSeen = true;
				}
			default:
				break;
			}
		
		}
	};
	
	
	// Skeleton for first wall traversal state
	IRobotState traverse1 = new IRobotState("traverse1") {
		public void handleEvent(Sensor sensor, short shortness) {
		}
	};
	
	
	// Skeleton for second align state
	IRobotState align2 = new IRobotState("align2") {
		public void handleEvent(Sensor sensor, short shortness) {
		}
	};
	
	
	// Skeleton for second wall traversal state
	IRobotState traverse2 = new IRobotState("traverse2") {
		public void handleEvent(Sensor sensor, short shortness) {
		}
	};

	
	// Skeleton for end state
	IRobotState end = new IRobotState("end") {
		public void handleEvent(Sensor sensor, short shortness) {
		}
	};

}
