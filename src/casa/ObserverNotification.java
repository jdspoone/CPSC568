package casa;

import casa.agentCom.URLDescriptor;
import casa.interfaces.ProcessInterface;
import casa.util.CASAUtil;

import java.text.ParseException;

/**
 * <p>Title: EventNotification</p>  <p>Description: This class is used to notify subscribers 
 * of an ObservableEvent that a change has taken place. </p> 
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary.
 * Permission to use, copy, modify, distribute and sell this software and its documentation
 *  for any purpose is hereby granted without fee, provided that the above copyright notice 
 *  appear in all copies and that both that copyright notice and this permission notice appear 
 *  in supporting documentation.  The Knowledge Science Group makes no representations about 
 *  the suitability of this software for any purpose.  It is provided "as is" without express 
 *  or implied warranty.</p> <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author  <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 * @version 0.9
 * @see  EventNotificationURL
 */

public class ObserverNotification {

	/**
	 */
	private ProcessInterface agent;
	private String eventType;
	private Object obj;
	private URLDescriptor agentUrl;
		  
	/**
	 * @return the agentUrl
	 */
	public URLDescriptor getAgentUrl() {
		return agentUrl;
	}

	public ObserverNotification(ProcessInterface agent, String eventType, Object obj){
		this.agent = agent;
		this.agentUrl = agent.getURL();
		this.eventType = eventType;
		this.obj = obj;
	}
		  
	public String getType() { return eventType; }

	/**
	 * @return
	 */
	public ProcessInterface getAgent() { return agent; }

	public Object getObject() { return obj; }
	
	/**
	 * The agent is NOT serialized because it is typically a heavy class.
	 * Also, it makes little sense to transmit a data structure that heavy
	 * over the network.
	 */
	@Override
	public String toString() {
		return CASAUtil.serialize(agentUrl, eventType, obj);
	}
	
	public ObserverNotification(String aString){
		try {
			Object[] arr = CASAUtil.unserializeArray(aString, null);
			this.agentUrl = (URLDescriptor)arr[0];
			this.eventType = (String)arr[1];
			this.obj = arr[2];
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
