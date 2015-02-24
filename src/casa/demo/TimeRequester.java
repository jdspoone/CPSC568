package casa.demo;

import casa.ML;
import casa.MLMessage;
import casa.PerformDescriptor;
import casa.abcl.ParamsMap;
import casa.ui.AgentUI;

public class TimeRequester extends TimeSuper {
	public TimeRequester (ParamsMap params, AgentUI ui) throws Exception {
		super (params, ui);
	}

	public PerformDescriptor release_getTime (MLMessage message) {
		in ("TimeRequester.dischargeGetTime");
		String contents = message.getParameter (ML.CONTENT);

		if (isLoggingTag ("time2")) {
			println ("time2", "Got time: " + contents);
		}

		out ("TimeRequester.dischargeGetTime");
		return null; //new PerformDescriptor (new Status (0));
	}

//	@Override
//	protected void initRTCommandInterface () {
//		in ("TimeRequester.initRTCommandInterface");
//		super.initRTCommandInterface ();
//		try {
//			commandInterpreter
//					.put (
//							"time | "
//									+ "?(help=\"Request time from another agent.\")",
//							new Command () {
//								@Override
//								public Status execute (String line, Map<String,String> params,
//										AgentUI ui) {
//									String port = "3000";
//									if (params.containsKey("port")) {
//										port = (String) params.get ("port");
//									}
//									try {
//										URLDescriptor other = new URLDescriptor ("casa://localhost:" + port + "/casa/demo/TimeGiver/Bob");
//										return sendMessage(ML.REQUEST, ACT_GET_TIME, other, new String[] {});
//									} catch (URLDescriptorException e) {
//										return new Status (-1);
//									}
//								}
//							});
//		} catch (ParameterParserException ex) {
//			println (
//					"error",
//					"Unexepected exception when executing commandInterpreter.put()'s",
//					ex);
//			System.out.println ("ERROR!!");
//			ex.printStackTrace(System.out);
//		}
//		out ("TimeRequester.initRTCommandInterface");
//	}
}