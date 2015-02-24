package casa.event;

import casa.interfaces.PolicyAgentInterface;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * TimeEvent is a non-recurring event that occurs at a specified time. This time is given to the 
 * constructor and does not change over the lifetime of the event. The time is given in the same 
 * format as  {@link System#currentTimeMillis()}. If the time that the event occurs is before 
 * {@link #start()}   is called, the event occurs as soon as it is called.
 * 
 * @author   Jason Heard
 * @version 0.9
 * @see   System.currentTimeMillis()
 */

public class TimeEvent extends NonRecurringEvent {
  /**
   * This is a shared {@link Timer} object that is used by all TimeEvents to
   * schedule the activations of events. By sharing the timer object, all timer
   * events use only a single thread.
   */
  protected static final Timer TIMER = new Timer(false);

  /**
   * The time the event occurs. The time is given in the same format as
   * {@link System#currentTimeMillis()}.
   */
  private final long time;
  
  /**
   * Recorded TimerTask needed if this TimeEvent is canceled.  Only
   * instantiated after {@link #start()} is executed.
   */
  private EventTask task = null;

  /**
   * Creates a new TimeEvent that occurs at the given time.
   * 
   * @param newType The type of the new event.
   * @param agent The agent that this event will be queued to.
   * @param newTime The time when this event should occur.
   */
  public TimeEvent (String newType, PolicyAgentInterface agent, long newTime) {
    super (newType, agent);

    time = newTime;
  }

  /**
   * Starts the timer so that this event can occur. This uses
   * {@link java.util.Timer#schedule(TimerTask, Date)} to schedule the event.
   */
  @Override
	public void start() {
  	task = new EventTask (this,agent){
  	  @Override
  	  public void run () {
  	  	this.cancel(); //Guarantees that the event will never run again
  	  	super.run();
  	  }
  	};
    TIMER.schedule (task, new Date (time));
    agent.println("eventqueue", "Scheduling time event "+id+" for "+time+": "+this);
  }
  
  /**
   * Cancels the timer task so it will not get queued for execution
   */
  public void cancel() {
  	if (task!=null) task.cancel();
    agent.println("eventqueue", "Time event "+id+" for "+time+" canceled: "+this);
    delete();
  }
}