/**
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. 
 * Permission to use, copy, modify, distribute and sell this software and its 
 * documentation for any purpose is hereby granted without fee, provided that the 
 * above copyright notice appear in all copies and that both that copyright notice 
 * and this permission notice appear in supporting documentation.  The  Knowledge 
 * Science Group makes no representations about the suitability of  this software 
 * for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
package casa.testAgents;

import casa.Agent;
import casa.ML;
import casa.MLMessage;
import casa.PerformDescriptor;
import casa.Status;
import casa.abcl.ParamsMap;
import casa.agentCom.URLDescriptor;
import casa.event.RecurringTimeEvent;
import casa.exceptions.URLDescriptorException;
import casa.ui.AgentUI;

import java.sql.Time;

/**
 * This agent informs other agents of the time at intervals
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
public class ObnoxiousTimeInformAgent extends Agent {

	/**
	 * @param params
	 * @param ui
	 * @throws Exception
	 */
	public ObnoxiousTimeInformAgent(ParamsMap params, AgentUI ui)
			throws Exception {
		super(params, ui);
	}

	@Override
	public void initializeAfterRegistered(boolean registered) {
		super.initializeAfterRegistered(registered);
		try {
			ontology.addType("keepTellingTime", ML.ACT);
		} catch (Exception e) {
			println("error","Failed to add ACT to ontology",e);
		}
	}

  //************ Server methods for an REQUEST keepTellingTime conversation **************

	/**
	 * Process an incoming <em>request</em> message for a <em>keepTellingTime</em> act-type request conversation.
	 * This method should return a 
	 * {@link casa.PerformDescriptor PerformDescriptor}.{@link casa.PerformDescriptor#getStatusValue() getStatusValue()}
	 * of 0 or positive to make the reply an AGREE, or a negative value to indicate some failure
	 * (and return REFUSE).  It may influence the reply by calling 
	 * {@link java.util.TreeMap#put(Object, Object) PerformDescriptor.put(messageKey,value)} on the
	 * return {@link casa.PerformDescriptor PerformDescriptor}.  For example, 
	 * ret.put({@link casa.ML#PERFORMATIVE},{@link casa.ML#NOT_UNDERSTOOD} will cause the
	 * reply performative to be NOT UNDERSTOOD instead of AGREE.  The default return 
	 * message will be an AGREE.  This method is optional -- use this method if you want to 
	 * possibly refuse the request.
	 * @param msg The incoming message
	 * @return The result of the processing; the status part will influence the return
	 *  message (if any), and the key/value part will be overlayed on the return message 
	 *  (if any)
	 * @since 2010-01-16
	 */
	public PerformDescriptor consider_keepTellingTime(MLMessage msg) {
		//TODO auto generated method for consider keepTellingTime for a request server conversation 
		in("ObnoxiousTimeInformAgent.consider_keepTellingTime");
		PerformDescriptor ret = null;
		out("ObnoxiousTimeInformAgent.consider_keepTellingTime");
		return ret;
	}

	/**
	 * Perform the action for an incoming <em>request</em> message for a <em>keepTellingTime</em> act-type request conversation.
	 * This method should return a 
	 * {@link casa.PerformDescriptor PerformDescriptor}.{@link casa.PerformDescriptor#getStatusValue() getStatusValue()}
	 * of 0 or positive to make the reply a SUCCESS, or a negative value to indicate FAILURE.  It may influence
	 * the reply by calling 
	 * {@link java.util.TreeMap#put(Object, Object) PerformDescriptor.put(messageKey,value)} on the
	 * return {@link casa.PerformDescriptor PerformDescriptor}.  For example, 
	 * ret.put({@link casa.ML#CONTENT},"content"} will fill the content field with "content"
	 * in the message to the client.  The default return 
	 * message will be an SUCCESS.  This method is required.
	 * @param msg The outgoing AGREE message
	 * @return The result of the processing; the status part will influence the return
	 *  message (if any), and the key/value part will be overlayed on the return message 
	 *  (if any)
	 * @since 2010-01-16
	 */
	public PerformDescriptor perform_keepTellingTime(MLMessage msg) {
		//TODO auto generated method for perform keepTellingTime for a request server conversation 
		in("ObnoxiousTimeInformAgent.perform_keepTellingTime");
		final URLDescriptor url;
		try {
			url = msg.getSender();
		} catch (URLDescriptorException e1) {
			return new PerformDescriptor(new Status(-1,"Cannot interpret Sender field '"+msg.getParameter(ML.SENDER)+"'"));
		}
		new RecurringTimeEvent("keepTellingTime", this, 60000, 60000) {
			@Override
			public void fireEvent() {
				super.fireEvent();
				sendMessage(ML.INFORM, 
						        "keepTellingTime", 
						        url, 
						        ML.CONTENT, new Time(System.currentTimeMillis()).toString());
			}
		}.start();
		PerformDescriptor ret = new PerformDescriptor();
		ret.put(ML.CONTENT, new Time(System.currentTimeMillis()).toString());
		out("ObnoxiousTimeInformAgent.perform_keepTellingTime");
		return ret;
	}

	/**
	 * Perform the action for an incoming <em>accept-proposal</em> or <em>reject-proposal</em> message
	 * for a <em>keepTellingTime</em> act-type request conversation.
	 * This method should return a 
	 * {@link casa.PerformDescriptor PerformDescriptor}.{@link casa.PerformDescriptor#getStatusValue() getStatusValue()}
	 * of 0 or positive to indicate SUCCESS, or a negative value to indicate FAILURE.
	 * This method is optional -- use it when you want to process a rejection in some
	 * special way.
	 * @param msg The incoming message
	 * @return The result of the processing; the status part will influence the return
	 *  message (if any), and the key/value part will be overlayed on the return message 
	 *  (if any)
	 * @since 2010-01-16
	 */
	public PerformDescriptor conclude_keepTellingTime(MLMessage msg) {
		//TODO auto generated method for conclude keepTellingTime for a request server conversation 
		in("ObnoxiousTimeInformAgent.conclude_keepTellingTime");
		PerformDescriptor ret = null;
		out("ObnoxiousTimeInformAgent.conclude_keepTellingTime");
		return ret;
	}

	/**
	 * Perform the action for an incoming <em>cancel</em> message
	 * for a <em>keepTellingTime</em> act-type request conversation.
	 * This method should return a 
	 * {@link casa.PerformDescriptor PerformDescriptor}.{@link casa.PerformDescriptor#getStatusValue() getStatusValue()}
	 * of 0 or positive to indicate SUCCESS, or a negative value to indicate FAILURE.
	 * This method is optional -- use it when you want to process a cancelation in some
	 * special way.
	 * @param msg The incoming cancel message
	 * @return The result of the processing; the status part will influence the return
	 *  message (if any), and the key/value part will be overlayed on the return message 
	 *  (if any)
	 * @since 2010-01-16
	 */
	public PerformDescriptor release_keepTellingTime(MLMessage msg) {
		//TODO auto generated method for release keepTellingTime for a request server conversation 
		in("ObnoxiousTimeInformAgent.release_keepTellingTime");
		PerformDescriptor ret = null;
		out("ObnoxiousTimeInformAgent.release_keepTellingTime");
		return ret;
	}


}
