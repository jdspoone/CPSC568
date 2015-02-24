package casa.event;

import casa.ML;
import casa.interfaces.PolicyAgentInterface;

/**
 * The RecurringEvent abstract class represents an event that does recur. It
 * defines {@link #isRecurring()} to always return <code>true</code>.
 * 
 * @author Jason Heard
 * @version 0.9
 */

public abstract class RecurringEvent extends AbstractEvent {
  /**
   * Creates a new NonRecurringEvent class with the specified type.
   * @param newType the type of the event
   * @param agent the agent that this event will be queued to
   */
  public RecurringEvent (String newType, PolicyAgentInterface agent) {
    super (newType, agent);
  }

  /**
   * Determines if this event is recurring. Since this is a recurring event,
   * this always returns <code>true</code>.
   * 
   * @return <code>true</code>, always.
   */
  public final boolean isRecurring () {
    return true;
  }
  
  private boolean stop = false;
  
  /**
   * Signals that this event should actually be deleted and not repeated after the next call to {@link #delete()}.
   */
  public void cancel() {
  	stop = true;
  }

	/**
	 * Doesn't delete the event unless {@link cancel()} has been previously called
	 * @see casa.event.AbstractEvent#delete()
	 */
	@Override
	public void delete() {
		if (stop)
		  super.delete();
	}

	/* (non-Javadoc)
	 * @see casa.event.Event#start()
	 */
	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}
  
  @Override
	public String toString() {
  	StringBuilder b = new StringBuilder();
  	b.append(super.toString());
  	if (stop) b.append(", cancelled");
  	return b.toString();
  }

  
}
