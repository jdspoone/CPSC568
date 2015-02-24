package casa.event;

import casa.AbstractProcess;
import casa.interfaces.PolicyAgentInterface;

import java.util.Date;

/**
 * The NonRecurringEvent abstract class represents an event that does not recur.
 * It defines {@link #isRecurring()} to always return <code>false</code>.
 * 
 * @author Jason Heard
 * @version 0.9
 */

public class NonRecurringEvent extends AbstractEvent {
  /**
   * Creates a new NonRecurringEvent class with the specified type.
   * @param newType the type of the event
   * @param agent the agent that the event will be queued to
   */
  public NonRecurringEvent (String newType, PolicyAgentInterface agent) {
    super (newType, agent);
  }

  /**
   * Determines if this event is recurring. Since this is a non-recurring event,
   * this always returns <code>false</code>.
   * 
   * @return <code>false</code>, always.
   */
  public final boolean isRecurring () {
    return false;
  }

  public void start () {
    agent.queueEvent(this);
  }

}
