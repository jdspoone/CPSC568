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

import casa.ML;
import casa.interfaces.PolicyAgentInterface;

/**
 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
public class TriggerEvent extends AbstractEvent {
  private boolean triggered = false;
  private boolean started = false;
  private final boolean recurring;

  public TriggerEvent (String eventType, boolean recurring, PolicyAgentInterface agent) {
    super (eventType, agent);
    this.recurring = recurring;
  }

  /* (non-Javadoc)
   * @see casa.event.Event#isRecurring()
   */
  /**
	 * @return
	 */
  public boolean isRecurring () {
    return recurring;
  }

  public void trigger () {
    triggered = true;
    if (started) {
      //fireEvent ();
    	agent.queueEvent(this);
    }
  }
  
  public boolean hasTriggered() {
  	return triggered;
  }

  public boolean hasStarted() {
  	return started;
  }
  
  public boolean hasBeenQueued() {
  	return triggered && started;
  }

  public void start () {
    started = true;
    if (triggered) {
      //fireEvent ();
    	agent.queueEvent(this);
    }
  }
  
  private boolean stop = false;
  
  /**
   * Signals that this event should actually be deleted and not repeated after the next call to {@link #delete()}.
   */
  public void cancel() {
  	stop = true;
  	started = false;
  }

  /**
   * Overrides {@link AbstractEvent#fireEvent()} by checking to see if this event has 
   * been {@link #cancel()}'ed.  If it has not been cancelled, {@link AbstractEvent#fireEvent()}
   * is called.
   */
	@Override
	public void fireEvent() {
		if (!stop)
		  super.fireEvent();
	}

	/**
	 * Deletes the event from the internal list of all Events.
	 * But if this is a {@link #recurring} event, it doesn't delete the event unless 
	 * {@link #cancel()} has been previously called.
	 * @see casa.event.AbstractEvent#delete()
	 */
	@Override
	public void delete() {
		if (!recurring || stop)
		  super.delete();
	}

}