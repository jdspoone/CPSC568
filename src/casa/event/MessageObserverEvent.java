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
package casa.event;

import casa.AbstractProcess;
import casa.ML;
import casa.MLMessage;
import casa.Status;
import casa.interfaces.PolicyAgentInterface;

import java.util.Observable;
import java.util.Observer;

/**
 * Defines an Event that will be triggered when the owner agent receives a message 
 * matching the <em>patternPairs</em> given in this constructor.  Don't forget to call 
 * the  {@link #start()} method to enable the trigger.  
 * 
 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
public class MessageObserverEvent extends TriggerEvent implements Observer {

//	TreeMap<String,String> pattern = new TreeMap<String,String>();
	
	/**
	 */
	protected MLMessage message = null;
	/**
	 * @return the message that triggered this event (will be null before it's triggered).
	 */
	public MLMessage getMessage() {return message;}
	
	MessageEventDescriptor[] descriptor = null;
	
	boolean observing = true;
	
	/**
	 * Construct a new Event that will be triggered when the owner agent receives a message matching
	 * the <em>patterhPairs</em> given in this constructor.  Don't forget to call the {@link #start()}
	 * method to enable the trigger.  
	 * @param recurring set to true to make this a recurring event that doesn't disappear after one activation (see {@link Event#isRecurring()})
	 * @param agent The agent that owns this Event and will be watched for the matching received message
	 * @param patternPairs A even-cardinality list interpreted as key/value pairs that will be used to match on the received message
	 * @see #update(Observable, Object) 
	 */
	public MessageObserverEvent(boolean recurring, PolicyAgentInterface agent, MessageEventDescriptor... descriptor) {
		super(ML.EVENT_SC_PERFORM_ACTION, recurring, agent);
		// TODO might want to restrict the types to observer, based on the descriptors...
		((AbstractProcess)agent).addObserver(this);
		this.descriptor = descriptor;
	}

	/** 
	 * Override the {@link java.util.Observer#update(java.util.Observable, java.lang.Object) update()} template
	 * method to call the {@link #trigger()} method iff the update was for a 
	 * {@link ML#EVENT_MESSAGE_RECEIVED} event and the received message
	 * <em>matches</em> the pattern given in the 
	 * {@link #MessageObserverEvent(boolean, PolicyAgentInterface, String...) constructor}.
	 * By <em>matches</em> we mean that each key in the pattern exists in the message
	 * and the value of the key in the pattern subsumes the value of the key
	 * in the message. 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, Object arg) {
		if (arg instanceof casa.ObserverNotification){
			casa.ObserverNotification event = (casa.ObserverNotification)arg;
			if (event.getObject() instanceof MLMessage && agent.isA(event.getType(),ML.EVENT_MESSAGE_RECEIVED)) {
				message = (MLMessage)event.getObject();
				String eventType = event.getType();

				//Duplicate the message event and use descriptor.isApplicable
				Event temp = new MessageEvent(eventType, agent, message);
				if (descriptor != null) {
					for (MessageEventDescriptor d: descriptor) {
						Status stat = d.isApplicable(agent, temp);
						if (stat.getStatusValue()==0) { 
							trigger();
							agent.println("eventqueue5", "MessageObserverEvent.update: '"+d+"' triggered");
							if (!isRecurring())
								delete(); // ((AbstractProcess)agent).deleteObserver(this);
							break;
						}
						else
							agent.println("eventqueue5", "MessageObserverEvent.update: '"+d+"': "+stat);
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see casa.event.TriggerEvent#cancel()
	 */
	@Override
	public void cancel() {
		if (observing) 
			((AbstractProcess)agent).deleteObserver(this);
		observing = false;
		super.cancel();
	}

	/* (non-Javadoc)
	 * @see casa.event.TriggerEvent#fireEvent()
	 */
	@Override
	public void fireEvent() {
		if (observing) 
			((AbstractProcess)agent).deleteObserver(this);
		observing = false;
		super.fireEvent();
	}

	/* (non-Javadoc)
	 * @see casa.event.TriggerEvent#delete()
	 */
	@Override
	public void delete() {
		if (observing) 
			((AbstractProcess)agent).deleteObserver(this);
		observing = false;
		super.delete();
	}

}
