package casa.event;

import casa.AbstractProcess;
import casa.interfaces.PolicyAgentInterface;
import casa.util.InstanceCounter;

import java.util.TimerTask;

/**
 * EventTask is a subclass of  {@link TimerTask}  that calls 
 * {@link AbstractEvent#fireEvent()}  when the task is run via the {@link #run()}  method.
 * @author  Jason Heard
 * @version 0.9
 */
public class EventTask extends TimerTask implements Comparable<EventTask> {
  /**
	 * The event that is related to this task.
	 */
  private AbstractEvent event;
  
  /**
	 */
  private PolicyAgentInterface agent;
  
  private boolean conditional = false;

  /**
   * Creates a new EventTask object associated with the specified event.
   * 
   * @param newEvent The {@link AbstractEvent} that this task is associated
   *          with.
   * @param agent The agent to queue this event to
   */
  public EventTask (AbstractEvent newEvent, PolicyAgentInterface agent) {
    event = newEvent;
    this.agent = agent;
  	InstanceCounter.add(this);
  }

	/**
   * Creates a new EventTask object associated with the specified event.
   * 
   * @param newEvent The {@link AbstractEvent} that this task is associated
   *          with.
   * @param agent The agent to queue this event to
   * @param conditional If conditional is true, then don't queue events if the event is already on the queue
   */
  public EventTask (AbstractEvent newEvent, PolicyAgentInterface agent, boolean conditional) {
    this(newEvent,agent);
    this.conditional = conditional;
  }

  /**
   * This method calls {@link AbstractEvent#fireEvent()} on the associated
   * event.
   */
  @Override
  public void run () {
    //event.fireEvent ();
  	if (conditional) 
  		agent.queueEventIf(event);
  	else
  		agent.queueEvent(event);
  }
  
  @Override
  public int compareTo(EventTask o) {
  	return Integer.signum(o.hashCode()-this.hashCode());
  }
 
}