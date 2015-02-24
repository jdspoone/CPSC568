package casa.event;

import casa.AbstractDescribable;
import casa.AbstractProcess;
import casa.ML;
import casa.ObserverNotification;
import casa.exceptions.IllegalOperationException;
import casa.interfaces.PolicyAgentInterface;
import casa.util.InstanceCounter;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * AbstractEvent is an abstract class which implements 
 * {@link Event#addEventObserver(EventObserver)} , 
 * {@link Event#removeEventObserver(EventObserver)} , 
 * {@link Event#getEventType()}. 
 * In addition, it provides the method  {@link #fireEvent()}  which calls 
 * {@link EventObserver#notifyEventOccurred(String,Event,Object)}  
 * for all listeners. To create an  {@link Event} , create a subclass of AbstractEvent 
 * and implement {@link Event#isRecurring()}  and  {@link Event#start()} . A good 
 * example of this can be seen in  {@link TimeEvent} .
 * 
 * @author  Jason Heard
 * @version 0.9
 * @see Event
 * @see TimeEvent  
 */

public abstract class AbstractEvent extends Observable implements Event, Comparable<AbstractEvent> {
	
//	static private final boolean recordingEvents = false;
//	static private AbstractSet<AbstractEvent> allEvents = new ConcurrentSkipListSet<AbstractEvent>();
//	static public Set<AbstractEvent> getEvents(PolicyAgentInterface agent) {
//		TreeSet<AbstractEvent> ret = new TreeSet<AbstractEvent>();
//		for (AbstractEvent e: allEvents) {
//			if (agent.equals(e.agent)) ret.add(e);
//		}
//		return ret;
//	}
//	static private void addEvent(AbstractEvent e) {
//		if (recordingEvents)
//		  allEvents.add(e);
//	}
//	static private void removeEvent(AbstractEvent e) {
//		if (recordingEvents)
//		  allEvents.remove(e);
//	}
	
	/**
   * Creates a new AbstractEvent class with the specified type.
   * @param newType The type (from the agent ontology) of the new event.
   * @param agent The agent that will receive the event.
   */
  public AbstractEvent (String newType, PolicyAgentInterface agent) {
//    observers = new ArrayList<EventObserver> ();
    type = newType;
    this.agent = agent;
//    addEvent(this);
    if (agent.isLoggingTag("eventqueue3")) {
    	StringBuffer b = new StringBuffer();
    	b.append(getClass().toString().substring(6)).append('-').append(id).append('(').append(type).append("), ")
    	.append(agent.getName());
    	if (isRecurring()) b.append(", recurring");
//    	if (observers.size()!=0) b.append(", observers:").append(observers.size());
      agent.println("eventqueue3", "Instantiated event: "+b.toString());
    }
  	InstanceCounter.add(this);
  }
  
  /* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(AbstractEvent o) {
		int ret = agent.getURL().toString().compareTo(o.agent.getURL().toString());
		if (ret!=0) return ret;
		ret = type.compareTo(o.type);
		if (ret!=0) return ret;
		return id-o.id;
	}

	/**
	 * Removes this element from the {@link #allEvents}.
	 */
	public void delete() {
//  	removeEvent(this);
    agent.println("eventqueue3", "Deleted event: "+toString());  	
  }

//  /**
//	 * @see casa.event.Event#countObservers()
//	 */
//	@Override
//	public int countObservers() {
//		return observers==null?0:observers.size();
//	}

//	/**
//   * The list of observers as an {@link ArrayList}.
//   */
//  private ArrayList<EventObserver> observers;

  /**
   * The type of this event.
   */
  private String type;
  
  /**
   * The time the event was placed on the {@link casa.EventQueue}
   */
  long queueTime = 0;
  
  static private int uniqueID = 0;
  
  int priority = DEFAULT_PRIORITY;
  
  /**
   * The unique ID of this event.  This is unique to this process.
   */
  int id = uniqueID++;
  
  /**
	 * The agent that this event is to be queued to when it's  {@link #start()} ed.
	 */
  protected PolicyAgentInterface agent;

//  public final void addEventObserver (EventObserver observer) {
//    observers.add (observer);
//  }
//
//  public final void removeEventObserver (EventObserver observer) {
//    observers.remove (observer);
//  }

  @Override
	public final String getEventType () {
    return type;
  }

  /**
   * Initialized to false and set to true when {@link AbstractEvent#fireEvent()}
   * is called.
   */
  protected boolean fired = false;
  
  /**
   * Subclasses should be sure to either call {@link AbstractEvent#fireEvent()},
   * or specifically set {@link #fired} to true if they don't call {@link AbstractEvent#fireEvent()}.
   * @return true iff this event has had its {@link #fireEvent()} method called.
   */
  public boolean hasFired() {return fired;}
  
  /**
	 * This method calls
	 * {@link EventObserver#notifyEventOccurred(String, Event, Object)} for all
	 * observers that have been registered via {@link #addObserver(Observer)}.
	 * This method should only be
	 * called by the agent. To indicate that an event has occurred, the event
	 * should be enqueued in the agent's queue using
	 * {@link PolicyAgentInterface#queueEvent(Event)}.
	 */
  @Override
	public void fireEvent () {
  	//    for (EventObserver observer : observers) {
  	//      observer.notifyEventOccurred (type, this, null);
  	//    }
  	fired = true;
  	setChanged();
  	notifyObservers(new ObserverNotification((AbstractProcess)agent, type, this));
  	delete();
  }
  
	/* (non-Javadoc)
	 * @see casa.event.Event#getPriority()
	 */
	@Override
	public int getPriority() {
    return priority;
	}

	/* (non-Javadoc)
	 * @see casa.event.Event#setPriority()
	 */
	@Override
	public int setPriority(int priority) {
    return this.priority = priority;
	}

  /* (non-Javadoc)
	 * @see casa.event.Event#getQueueTime()
	 */
	/**
	 * @return the time the event was queued
	 */
	@Override
	public long getQueueTime() {
		return queueTime;
	}

	/* (non-Javadoc)
	 * @see casa.event.Event#setQueueTime(long)
	 */
	/**
	 * @param time
	 */
	@Override
	public void setQueueTime(long time) {
		queueTime = time;
	}

	/**
	 * conversationID getter
	 */
	@Override
	public String getOwnerConversationID(){
		return params.get(ML.CONVERSATION_ID);
	}
	
	/**
	 * conversationID setter
	 * @param id
	 */
	@Override
	public void setOwnerConversationID(String id) throws IllegalOperationException {
		params.put(ML.CONVERSATION_ID, id);
	}
	
	/**
	 * @return the unique ID of this Event
	 */
	@Override
	public int getID() {return id;}
	
  /**
   * A map of key/value pairs to be used by users to help identify this event
   */
	private TreeMap<String,String> params = new TreeMap<String,String>();
  
	/**
	 * Add the key/value pairs in <em>list </em> to the parameters associated with this event.
	 * @param list key/value pairs
	 */
  @Override
	public void setParameters (Map<String, String> list) {
    if (list==null) return;
  	params.putAll(list);
  }

  /**
	 * Add the key/value pair to the parameters associated with this event.
	 * @param key the key name.
	 * @param value the value associated with the key.
	 */
  @Override
	public void setParameter(String key, String value) {
  	params.put(key, value);
  }
  
  /**
   * @param key the key name
   * @return The value associated with <em>key</em> or null if there is not such key.
   */
  @Override
	public String getParameter(String key) {
  	return params.get(key);
  }
  
  /**
   * @return the set of all the keys for this Message
   */
  @Override
	public Set<String> keySet() {
  	return params.keySet();
  }
  
	@SuppressWarnings("unchecked")
	@Override
	public AbstractEvent clone() {
		try {
			AbstractEvent ret = (AbstractEvent)super.clone();
			ret.params = (TreeMap<String,String>)params.clone();
			InstanceCounter.add(ret);
			return ret;
		} catch (CloneNotSupportedException e) {
		}
		return null;
	}


  @Override
	public String toString() {
  	StringBuffer b = new StringBuffer();
  	b.append(getClass().toString().substring(6)).append('-').append(id).append('(').append(type).append("), ")
  	.append(agent.getName());
  	if (isRecurring()) b.append(", recurring");
  	if (queueTime!=0) b.append(", Qtime:").append(queueTime);
  	b.append(", ").append(countObservers()).append(" observers");
  	if (getPriority()!=0) b.append(", priority: ").append(getPriority());
  	if (params.size()>0) for (String key: params.keySet()) b.append(", ").append(key).append('=').append(params.get(key));
  	else b.append(", no params");
  	return b.toString();
  }
}