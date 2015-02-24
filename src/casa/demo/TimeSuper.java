package casa.demo;

import casa.ML;
import casa.MLMessage;
import casa.Status;
import casa.TokenParser;
import casa.TransientAgent;
import casa.abcl.ParamsMap;
import casa.agentCom.URLDescriptor;
import casa.exceptions.URLDescriptorException;
import casa.ui.AgentUI;
import casa.util.Trace;

import java.util.ArrayList;
import java.util.Collections;

public class TimeSuper extends TransientAgent {

	public static final String ACT_GET_TIME = "get.time";

	public TimeSuper (ParamsMap params, AgentUI ui) throws Exception {
		super (params, ui);
	}

	@Override
	protected void initializeThread (ParamsMap params, AgentUI ui) {
		in ("TestAgent.initializeRun");
		super.initializeThread (params, ui);

		try {
			ontology.addType (ACT_GET_TIME, ML.PERFORM);
		} catch (Exception e) {
			Trace.log("error", "TimerSuper.initializeThread()", e);
		} 
		
		options.defaultTimeout = 3600000;
	}

	@Override
	public Status sendMessage (MLMessage msg) {
		println ("time", "Message sent:\n" + messagePrint (msg));
		return super.sendMessage (msg);
	}

	private String messagePrint (MLMessage msg) {
		StringBuffer buffer = new StringBuffer ();

		buffer.append ("( ").append (msg.getParameter (ML.PERFORMATIVE));

		ArrayList<String> list = Collections.list (msg.parameters ());

		addParameter (msg, buffer, list, ML.ACT);
		addParameter (msg, buffer, list, ML.SENDER);
		addParameter (msg, buffer, list, ML.RECEIVER);
		addParameter (msg, buffer, list, ML.LANGUAGE);
		addParameter (msg, buffer, list, ML.CONTENT);
//		addParameter (msg, buffer, list, ML.TIMEOUT);

		buffer.append ("\n)");

		return buffer.toString ();
	}

	private void addParameter (MLMessage msg, StringBuffer buffer,
			ArrayList<String> list, String key) {
		if (list.contains (key)) {
			String value = msg.getParameter (key);
			if (key.equals (ML.REPLY_BY)) {
				long timeout = Long.parseLong (value);
				if (timeout != ML.TIMEOUT_NEVER) {
					value = MLMessage.longToTextDate (timeout);
				} else {
					value = "Never";
				}
			} else if (key.equals (ML.RECEIVER) || key.equals (ML.SENDER)) {
				try {
					URLDescriptor temp = URLDescriptor.make(value);
					value = temp.getFile();
				} catch (URLDescriptorException e) {
					e.printStackTrace();
				}
			}
			
			buffer.append ("\n :");
			buffer.append (key);
			buffer.append (" ");
			buffer.append (TokenParser.makeFit (value));
		}
	}
}
