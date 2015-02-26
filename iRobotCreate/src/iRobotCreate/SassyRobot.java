package iRobotCreate;

/* For invaluable instructions on how to run this in Eclipse, please refer to page 8
 * of the Casa IRobot User Manual on the iRobot section of the CASA website. Use
 * the modified wallMeasure.lisp script that should HOPEFULLY be accompanying this push
 */

import iRobotCreate.iRobotCommands.Sensor;
import casa.ML;
import casa.abcl.ParamsMap;
import casa.ui.AgentUI;
import iRobotCreate.IRobotState;

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
		
		setState(goForTheRecord);
	}
	
	/*This state just spins around in place fairly slowly. Not very interesting but this is just
	 * to show what can be done.
	*/
	IRobotState goForTheRecord = new IRobotState("goForTheRecord") {
		@Override
		public void enterState() {
			//memorize this form because you can be darned sure we're going to be using this a lot.
			sendMessage(ML.REQUEST, ML.EXECUTE, server, ML.LANGUAGE, "lisp", ML.CONTENT, "(progn () (irobot.mode 2) (irobot.drive 30 -1))");
		}
		public void handleEvent(Sensor sensor, short shortness) {
			//Here's where we tell specifically what the agent to do in a given state
			//when a sensor reading arrives
		}
	};
	
	
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
	
	
	// Skeleton for first align state
	IRobotState align1 = new IRobotState("align1") {
		public void handleEvent(Sensor sensor, short shortness) {
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
