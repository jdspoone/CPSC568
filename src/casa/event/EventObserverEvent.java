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
import casa.interfaces.PolicyAgentInterface;

import java.util.Observable;
import java.util.Observer;

/**
 * Defines an Event that will be triggered when the owner agent receives a message 
 * matching the <em>patternPairs</em> given in this constructor.  Don't forget to call 
 * the    {@link #start()}   method to enable the trigger. 
 *  
 * @author    <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
public class EventObserverEvent extends TriggerEvent implements Observer {

	/**
	 * These are the events we will observe.
	 * Note that, although one would think we needn't save these since we add ourselves to the agent's
	 * observers with a filter on our watched events, it's still possible that another call generalizes
	 * this filter, and we end up getting events we handn't specified. 
	 */
	String[] watchedEvents = null;
	
	/**
	 * Construct a new Event that will be triggered when the owner agent receives a message matching
	 * the <em>patternPairs</em> given in this constructor.  Don't forget to call the {@link #start()}
	 * method to enable the trigger.  
	 * @param recurring set to true to make this a recurring event that doesn't disappear after one activation (see {@link Event#isRecurring()})
	 * @param agent The agent that owns this Event and will be watched for the matching received message
	 * @param watchedEvents When any of these events occurs, we will call {@link #trigger()}
	 * @see #update(Observable, Object) 
	 */
	public EventObserverEvent(String eventType, boolean recurring, PolicyAgentInterface agent, String... watchedEvents) {
		super(eventType, recurring, agent);
		((AbstractProcess)agent).addObserver(this, watchedEvents);
		this.watchedEvents = watchedEvents;
	}

//	public EventDescriptor getDescriptor(){
//		return this.descriptor;
//	}
	
	/** 
	 * Override the {@link java.util.Observer#update(java.util.Observable, java.lang.Object) update()} template
	 * method to call the {@link #trigger()} method iff the update was for a 
	 * event in watchedEvents
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, Object arg) {
		if (arg instanceof casa.ObserverNotification){
			if (watchedEvents==null) {
				trigger();
				return;
			}
			String eventType = ((casa.ObserverNotification) arg).getType();
			for (String e: watchedEvents) {
				if (e.equals(eventType)) {
					trigger();
					if (!isRecurring())
						((AbstractProcess)agent).deleteObserver(this);
					return;
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see casa.event.TriggerEvent#cancel()
	 */
	@Override
	public void cancel() {
		((AbstractProcess)agent).deleteObserver(this);
		super.cancel();
	}

	/* (non-Javadoc)
	 * @see casa.event.TriggerEvent#fireEvent()
	 */
	@Override
	public void fireEvent() {
		((AbstractProcess)agent).deleteObserver(this);
		super.fireEvent();
	}

	/* (non-Javadoc)
	 * @see casa.event.TriggerEvent#delete()
	 */
	@Override
	public void delete() {
		((AbstractProcess)agent).deleteObserver(this);
		super.delete();
	}


	
}
