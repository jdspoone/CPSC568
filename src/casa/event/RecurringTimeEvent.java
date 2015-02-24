package casa.event;


import casa.interfaces.PolicyAgentInterface;

import java.util.Date;
import java.util.TimerTask;

/**
 * RecurringTimeEvent is a recurring event that occurs first at a specified time and then repeats indefinitely 
 * with a fixed delay between occurrences. The first time and the time between occurrences are given to the constructor 
 * and do not change over the lifetime of the event. The first time is given in the same format 
 * as   {@link System#currentTimeMillis()}  , and the time between occurences is given in milliseconds. If the time that 
 * the event occurs is before   {@link #start()}   is called, the event occurs as soon as it is called, and will reoccur 
 * repeatedly in order to "catch-up". See  {@link java.util.Timer#scheduleAtFixedRate(java.util.TimerTask,Date,long)}  
 * for a description of this.
 * 
 * @author   Jason Heard
 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 * @version 0.9
 * @see   System.currentTimeMillis()
 */
public class RecurringTimeEvent extends RecurringEvent {
  /**
   * The first time this event should occur. The time is given in the same
   * format as {@link System#currentTimeMillis()}.
   */
  private long firstTime;

  /**
   * <code>long</code> indicating the number of milliseconds between
   * occurrences of this event.
   */
  private long timeBetweenOcurrences;

  /**
   * Recorded TimerTask needed if this TimeEvent is canceled.  Only
   * instantiated after {@link #start()} is executed.
   */
  private EventTask task = null;
  
  /**
   * Creates a new RecurringTimeEvent that first occurs at the given time and
   * repeats at the given rate.
   * 
   * @param newType The type of the new event.
   * @param agent The agent that this event will be queued to
   * @param newFirstTime The time when this event should first occur.
   * @param newTimeBetweenOccurrences The time in milliseconds between this
   *          event's occurrences.
   */
  public RecurringTimeEvent (String newType, PolicyAgentInterface agent, long newFirstTime,
      long newTimeBetweenOccurrences) {
    super (newType, agent);
    if (newFirstTime<System.currentTimeMillis()) {
      this.firstTime = System.currentTimeMillis();
      agent.println("warning", "RecurringTimeEvent.<constructor>: firstTime is specified as earlier than present,  Events will start immediately: "+this);
    }
    else {
    	this.firstTime = newFirstTime;
    }
    this.timeBetweenOcurrences = newTimeBetweenOccurrences;
  }

  /**
   * Starts the timer so that this event can occur. This uses
   * {@link java.util.Timer#scheduleAtFixedRate(TimerTask, Date, long)} to
   * schedule the event.
   */
  public void start () {
  	start(true);
//  	task = new EventTask (this,agent,true);
//    TimeEvent.TIMER.scheduleAtFixedRate (task, new Date (
//        firstTime), timeBetweenOcurrences);
  }
  
  /**
   * Starts the timer so that this event can occur. This uses
   * {@link java.util.Timer#scheduleAtFixedRate(TimerTask, Date, long)} to
   * schedule the event.
   */
  public void start (boolean queueOnlyIfNotAlreadyQueued) {
  	task = new EventTask (this,agent,queueOnlyIfNotAlreadyQueued);
    TimeEvent.TIMER.scheduleAtFixedRate (task, new Date (
        firstTime), timeBetweenOcurrences);
  }
  
  /**
   * Cancels the timer task so it will not get queued for execution
   */
  public void cancel() {
  	if (task!=null) task.cancel();
    agent.println("eventqueue", "RecurringTimeEvent for "+task+" canceled: "+this);
    delete();
  }
  
  
}