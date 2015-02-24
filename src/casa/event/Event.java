package casa.event;

import casa.Status;
import casa.TransientAgent;
import casa.abcl.CasaLispOperator;
import casa.abcl.ParamsMap;
import casa.exceptions.IllegalOperationException;
import casa.interfaces.Describable;
import casa.ui.AgentUI;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.armedbear.lisp.Environment;

/**
 * The Event interface describes an interrupt style event. Each event must have access to a thread to monitor or wait for the event to occur. When it does, it informs all observers of the event.<p> All events have an type, which may be used by the event observers to distinguish between multiple events it is observing.<p> If an observer wishes to be informed of the occurrence of the event, it must implement the  {@link EventObserver}  interface. The observer object must also be passed on to  {@link #addEventObserver(EventObserver)} . If the observer is added before  {@link #start()}  is called, it is guaranteed to be informed of the event's occurrence. If not, there is no guarantee. When the event occurs, {@link EventObserver#notifyEventOccurred(String,Event,Object)}  is called for all registered observers.
 * @author  Jason Heard
 * @version 0.9
 */

public interface Event extends Describable {
	
  /**
   * This method adds an {@link Observer} to the observer list. All
   * observers should be notified via
   * {@link Observer#update(Observable, Object)} when this event
   * occurs.
   * 
   * @param observer The {@link EventObserver} to add to the observer list.
   */
	public void addObserver(Observer o);
	
	public void deleteObserver(Observer o);

//  /**
//   * This method adds an {@link EventObserver} to the observer list. All
//   * observers should be notified via
//   * {@link EventObserver#notifyEventOccurred(String, Event, Object)} when this event
//   * occurs.
//   * 
//   * @param observer The {@link EventObserver} to add to the observer list.
//   */
//  public void addEventObserver (EventObserver observer);
//
//  /**
//   * This method removes an {@link EventObserver} from the observer list.
//   * 
//   * @param observer The {@link EventObserver} to remove from the observer list.
//   */
//  public void removeEventObserver (EventObserver observer);
  
  /**
   * @return The number of observers observing this event
   */
  public int countObservers();

  /**
   * This method returns the type of this event.
   * 
   * @return The type of this event.
   */
  public String getEventType ();

  /**
   * Determines if this event is recurring. A recurring event can occur multiple
   * times. Returns <code>true</code> if the event is recurring;
   * <code>false</code> otherwise.
   * 
   * @return <code>true</code> if the event is recurring; <code>false</code>
   *         otherwise.
   */
  public boolean isRecurring ();

  /**
   * Indicates that notification of the observers can now occur and queues the event
   * to the agent's event queue. Before this is
   * called, this event should never call any of its observers
   * {@link EventObserver#notifyEventOccurred(String, Event, Object)} method.
   */
  public void start ();
  
  /**
   * This method calls {@link EventObserver#notifyEventOccurred(String, Event, Object)} for
   * all observers that have been registered via
   * {@link #addEventObserver(EventObserver)}.
   */
  
  public void fireEvent ();
  
  /**
   * The default priority given to any message without <em>priority</em> field,
   * or with a mangles <em>priority</em> field.
   */
  public static final int DEFAULT_PRIORITY = 0;
  public static final int HIGHEST_PRIORITY = Integer.MAX_VALUE;
  
  /**
   * Indicates the priority of the event
   * @return the priority of this event
   */
  public int getPriority();
  
  /**
   * Sets the priority of the event
   * @return the parameter, which is the new priority
   */
  public int setPriority(int priority);
  
  /**
	 * Used by the EventQueue to set the time this event was queued
	 * @return
	 */
  public long getQueueTime();
  
  /**
	 * Used by the EventQueue to sort events
	 * @param  time
	 */
  public void setQueueTime(long time);
  
	/**
	 * conversationID getter
	 */
	public String getOwnerConversationID();
	
	/**
	 * conversationID setter
	 * @param id
	 */
	public void setOwnerConversationID(String id) throws IllegalOperationException;
	
	public int getID();	
	
	/**
	 * Add the key/value pairs in <em>list </em> to the parameters associated with this event.
	 * @param list key/value pairs
	 */
  public void setParameters (Map<String, String> list);

  /**
	 * Add the key/value pair to the parameters associated with this event.
	 * @param key the key name.
	 * @param value the value associated with the key.
	 */
  public void setParameter(String key, String value);
  
  /**
   * @param key the key name
   * @return The value associated with <em>key</em> or null if there is not such key.
   */
  public String getParameter(String key);
  
  /**
   * Lisp operator: (FIRE-EVENT)<br>
   * Fire the current event.
   */
  @SuppressWarnings("unused")
	public static final CasaLispOperator FIRE_EVENT =
    new CasaLispOperator("FIRE-EVENT", "\"!Fire the current event.\"", TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
    {
      @SuppressWarnings("unchecked")
			@Override
			public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
      	//Event event = TransientAgent.getEventForThread();
      	Event event = (Event)casa.abcl.Lisp.lookupAsJavaObject(env, "event");
      	event.fireEvent();
      	return new Status(0);
      }
    };
  

  
}