/**
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The  Knowledge Science Group makes no representations about the suitability of  this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
package casa.event;

import casa.ML;
import casa.MLMessage;
import casa.exceptions.IllegalOperationException;
import casa.interfaces.PolicyAgentInterface;
import casa.interfaces.SecurityFilterInterface;

import java.util.Map;

/**
 * @author  kremer
 */
public class MessageEvent extends NonRecurringEvent {
	
	/**
	 */
	private MLMessage message;

	/**
	 * Create a new MessageEvent
	 * @param newType The type, which must be a subtype (in the ontology) of {@link ML#EVENT_MESSAGE_EVENT}.
	 * @param agent The agent to which this event will be queued.
	 * @param msg The message.
	 */
	public MessageEvent(String newType, PolicyAgentInterface agent, MLMessage msg) {
		super(newType, agent);
		try {
			super.setOwnerConversationID(msg.getConversationID());
		} catch (IllegalOperationException e) {
			//This shouldn't ever happen
		}
		assert agent.isA(newType, ML.EVENT_MESSAGE_EVENT): "A message event must have type String of \""+ML.EVENT_MESSAGE_EVENT+"\", but got \""+newType+"\"";
		setMessage(msg);
	}

	/**
	 * @return  the message
	 */
	public MLMessage getMessage() {
		return message;
	}

	/**
	 * @param message  the message to set
	 */
	public void setMessage(MLMessage message) {
		this.message = message.clone();
		getPriority();
	}

	@Override
	public int getPriority() {
    String sPrio = message.getParameter(ML.PRIORITY);
    if (sPrio!=null) {
      try {
        priority = Integer.parseInt(sPrio);
      }
      catch (NumberFormatException ex) {
      }
    }
    return priority;
	}
	
	@Override
	public int setPriority(int priority) {
		message.setParameter(ML.PRIORITY, Integer.toString(priority));
		return this.priority = priority;
	}
	
	public void applySecurityFilter(SecurityFilterInterface securityFilter) {
  	if (securityFilter!=null) message = securityFilter.processMessage(message,agent,true);
	}

	/* (non-Javadoc)
	 * @see casa.event.AbstractEvent#fireEvent()
	 */
	@Override
	public void fireEvent() {
		super.fireEvent();
	}

	@Override
	/**
	 * It's illegal to call this method on a MessageEvent because the conversation ID is embedded in the embedded message.
	 */
	public void setOwnerConversationID(String id) throws IllegalOperationException {
		throw new IllegalOperationException("Cannot set call setOwnerConversationID() on a MessageEvent.");
	}
	
	@Override
	public String getParameter(String key) {
		String ret = message.getParameter(key);
		if (ret==null)
			ret = super.getParameter(key);
		return ret;
	}

	@Override
	public void setParameter(String key, String value) {
		message.setParameter(key, value);
	}

	@Override
	public void setParameters(Map<String, String> list) {
		message.setParameters(list);
	}

	@Override
	public String toString() {
  	StringBuilder b = new StringBuilder();
  	b.append(super.toString())
  	.append(", (").append(message.getParameter(ML.PERFORMATIVE))
  	.append(" :act ").append(message.getParameter(ML.ACT))
  	.append(" :receiver ").append(message.getParameter(ML.RECEIVER))
  	.append(" :sender ").append(message.getParameter(ML.SENDER))
  	.append("...)");
  	return b.toString();
  }
	
	@Override
	public MessageEvent clone() {
		MessageEvent ret = (MessageEvent)super.clone();
		ret.message = message.clone();
		return ret;
	}

}
