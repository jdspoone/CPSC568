package casa;

import casa.abcl.ParamsMap;
import casa.agentCom.URLDescriptor;
import casa.exceptions.URLDescriptorException;
import casa.ui.AgentUI;
import casa.ui.MasVisLoader;
import casa.ui.MasvisInternalFrame;
import casa.ui.TransientAgentInternalFrame;

import java.awt.Container;
import java.util.Observable;

/**
 * <p>
 * Copyright: Copyright 2003-2014, Knowledge Science Group, University of
 * Calgary. Permission to use, copy, modify, distribute and sell this software
 * and its documentation for any purpose is hereby granted without fee, provided
 * that the above copyright notice appear in all copies and that both that
 * copyright notice and this permission notice appear in supporting
 * documentation. The Knowledge Science Group makes no representations about the
 * suitability of this software for any purpose. It is provided "as is" without
 * express or implied warranty.
 * </p>
 * 
 * TODO: write documentation, clean up this class, use MasvisLoader here
 * 
 * @author <a href="mailto:rkyee@ucalgary.ca">Ryan Yee</a>
 * 
 */
public class MasvisAgent extends Agent {
	MasVisLoader	masVisLoader;

	public MasvisAgent(ParamsMap params, AgentUI ui) throws Exception {
		super(params, ui);
		in("MasvisAgent");
		
//		addConversationSupported(ML.REGISTER_INSTANCE, RegisterInstanceMasvisAgentRequestComposite.class, true);
		
		out("MasvisAgent");
	}

	@Override
	protected void initializeThread(ParamsMap params, AgentUI ui) {
		super.initializeThread(params, ui);
		masVisLoader = new MasVisLoader();
	}
	
	@Override
	protected void initializePolicies(ParamsMap params, AgentUI ui) {
		super.initializePolicies(params, ui);
		
//		try {
//			readPolicyFile("register_instance" + File.separator + "MasvisAgent" + File.separator + "RegisterInstanceMasvisAgentPolicies.lisp");
//		} catch (Exception e) {
//			println("error", getAgentName() + ": Cannot read new policy file: " + e.toString());
//		}
	}
	
	boolean receivedFirstMessage = false;
	
	/**
	 * TODO: rkyee: this doesn't grab all the events correctly because add_observer is itself
	 * an event. All events are dequeued and an appropriate social commitment is added. When
	 * the social commitment for the add_observer request is processed then the this agent is
	 * added as an observer. However, this is too late because the notification for events
	 * happens after the dequeue BUT before social commitments are processed! This means that
	 * events that are queued right after the add_observer requests are "invisible".
	 */
	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);
		
		if(arg instanceof ObserverNotification){
			ObserverNotification notification = (ObserverNotification)arg;
			if(isA(notification.getType(), ML.EVENT_REGISTER_INSTANCE) || isA(notification.getType(), ML.EVENT_REGISTER_INSTANCE_LOCAL) || isA(notification.getType(), ML.EVENT_REGISTER_INSTANCE_REMOTE)){
				//URLDescriptor newInstanceUrl = (URLDescriptor)notification.getObject();
				MLMessage message = (MLMessage)notification.getObject();
				URLDescriptor newInstanceUrl;
				try {
					newInstanceUrl = message.getSender();
					doAddObserver(newInstanceUrl, ML.EVENT_MESSAGE_EVENT, ML.EVENT_REGISTER_INSTANCE, ML.EVENT_REGISTER_INSTANCE_LOCAL, ML.EVENT_REGISTER_INSTANCE_REMOTE); //rkyee: remove this filter when checking add_observer...
				} catch (URLDescriptorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if(notification.getObject() instanceof MLMessage){
				if(!receivedFirstMessage){
					receivedFirstMessage = true;
					masVisLoader.readFile(LAC.ProcessInfo.process.getTrace().getTraceFile());
				}
				
				masVisLoader.addMessage((MLMessage)notification.getObject());
			}
		}
	}
	
	
	@Override
	protected TransientAgentInternalFrame makeDefaultInternalFrame(TransientAgent agent, String title, Container aFrame) {
		return new MasvisInternalFrame(this, "Masvis", aFrame, masVisLoader.getJFrame());
	}
	
//	@SuppressWarnings("unused")
//	private static final CasaLispOperator MAKE_REGISTER_INSTANCE_MASVIS_AGENT_REQUEST_CONVERSATION =
//	  	new CasaLispOperator("MAKE-REGISTER-INSTANCE-MASVIS-AGENT-REQUEST-CONVERSATION", "\"!Instantiates a register instance conversation.\""
//	  			, MasvisAgent.class)
//	  {
//	  	@Override
//	  	public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui) {
//	  		MLMessage msg = getMsgForThread();
//
//	  		agent.println("info", agent.getAgentName() + ": trying to create RegisterInstanceMasvisAgentRequestComposite conversation");
//	  		RegisterInstanceMasvisAgentRequestComposite conv = new RegisterInstanceMasvisAgentRequestComposite(agent, msg.getConversationID());
//
//	  		if (conv != null){ 
//	  			conv.run();
//	  			return new StatusObject<RegisterInstanceMasvisAgentRequestComposite>(0, conv);
//	  		}
//			  return new Status(-1, "Could not create conversation in MasvisAgent.");
//	  	}
//	  };
}
