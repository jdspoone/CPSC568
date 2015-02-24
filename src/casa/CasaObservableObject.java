package casa;

import casa.agentCom.URLDescriptor;
import casa.interfaces.ProcessInterface;
import casa.util.AgentLookUpTable;
import casa.util.CASAUtil;
import casa.util.InstanceCounter;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Observer;
import java.util.TreeMap;

/**
 * <p>
 * Copyright: Copyright 2003-2014, Knowledge Science Group, University of
 * Calgary. Permission to use, copy, modify, distribute and sell this software
 * and its documentation for any purpose is hereby granted without fee, provided
 * that the above copyright notice appear in all copies and that both that
 * copyright notice and this permission notice appear in supporting
 * documentation. The Knowledge Science Group makes no representations about the
 * suitability of this software for any purpose. It is provided "as is" without
 * express or implied warranty.
 * </p>
 * 
 * @author <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 * @author <a href="mailto:sedachv@cpsc.ucalgary.ca">Vladimir Sedach</a>
 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 */
public class CasaObservableObject extends java.util.Observable implements CasaObservable, Comparable<CasaObservableObject> {

	/**
	 * The url or the owner agent
	 */
	private final URLDescriptor		url;

	/**
	 * The "owner" agent
	 */
	private final AbstractProcess	agent;

	class ObserverComparator implements Comparator<Observer> {
		@Override
		public int compare(Observer o1, Observer o2) {
			if (o1==o2)
				return 0;
			return (o1.hashCode()<o2.hashCode())?-1:1;
		}
	}

	/**
	 * Observers on other machines or other process that we will notify via the
	 * casa messaging system. This TreeMap has the URLs of remote observers as
	 * keys, the key value is either null (meaning "send me all notifications")
	 * or a LinkedList of all the types (in the casa ontology) for which the
	 * observer wants to receive notifications. Any notification that is a a
	 * subtype of the registered type will also be sent.
	 */
	private TreeMap<URLDescriptor, String[]>	remoteObservers	= new TreeMap<URLDescriptor, String[]>();

	/**
	 * Observers that we will notify via just the same as
	 * {@link java.util.Observable} would. However, this is TreeMap of observers
	 * as keys, the key value is either null (meaning
	 * "send me all notifications") or a LinkedList of all the types (in the
	 * casa ontology) for which the observer wants to receive notifications. Any
	 * notification that is a a subtype of the registered type will also be
	 * sent.
	 */
	private TreeMap<Observer, String[]>			localObservers	= new TreeMap<Observer, String[]>(new ObserverComparator());

	/**
	 * @param url
	 *            The URL of the "owner" agent
	 * @param agent
	 *            The "owner" agent
	 */
	public CasaObservableObject(URLDescriptor url, AbstractProcess agent) {
		this.url = url;
		this.agent = agent;
		InstanceCounter.add(this);
	}

	/**
	 * @return The URL of the "owner" agent
	 */
	public URLDescriptor getURL() {
		return url;
	}

	/**
	 * @return The "owner" agent
	 */
	public ProcessInterface getAgent() {
		return agent;
	}

	/**
	 * Calls {@link #notifyObservers(String, Object)} with the first parameter
	 * as ML.TOP, and the second parameter as null.
	 * 
	 * @see java.util.Observable#clearChanged()
	 * @see java.util.Observable#hasChanged()
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 * @see #notifyObservers(String, Object)
	 */
	@Override
	public void notifyObservers() {
		notifyObserversWithTop(null);
	}

	/**
	 * Calls {@link #notifyObservers(String, Object)} with the first parameter
	 * as ML.TOP, and the second parameter as <em>arg</em>.
	 * 
	 * @param arg any object.
	 * @see java.util.Observable#clearChanged()
	 * @see java.util.Observable#hasChanged()
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 * @see #notifyObservers(String, Object)
	 */
	@Override
	public void notifyObservers(Object arg) {
		notifyObserversWithTop(arg);
	}

	/**
	 * Calls {@link #notifyObservers(String, Object)} with the first parameter
	 * as ML.TOP, and the second parameter as the argument, <em>arg</em>.
	 * 
	 * @param arg
	 *            any object.
	 * @see java.util.Observable#clearChanged()
	 * @see java.util.Observable#hasChanged()
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 * @see #notifyObservers(String, Object)
	 */
	@Override
	public void notifyObserversWithTop(Object arg) {
		notifyObservers(ML.TOP, arg);
	}

	/**
	 * Calls {@link #notifyObservers(String, Object)} with the first parameter
	 * as the argument <em>notifyType</em> and the second argument as null.
	 * 
	 * @param notifyType
	 *            The type (classification) of this notification; must be a type
	 *            for the ontology.
	 * @see java.util.Observable#clearChanged()
	 * @see java.util.Observable#hasChanged()
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 * @see #notifyObservers(String, Object)
	 */
	@Override
	public void notifyObserversWithNoArg(String notifyType) {
		notifyObservers(notifyType, null);
	}

	/**
	 * Notify all observers ({@link #localObservers local} and
	 * {@link #remoteObservers remote}) in the same manner as
	 * {@link java.util.Observable#notifyObservers(Object)}. However, filter the
	 * notifications based on what notifications observer wants to receive
	 * (based on when the observer registered). Any notification that is a
	 * subtype of the registered type will also be sent.
	 * <p>
	 * This method will always construct a {@link ObserverNotification} object
	 * as the "object" argument of the
	 * {@link java.util.Observer#update(java.util.Observable, Object)} call.
	 * Note that the Observable argument will NOT be the agent, but will be this
	 * CasaObservable object (one can get the agent from the
	 * {@link ObserverNotification} object.
	 * 
	 * @param notifyType
	 *            The type (classification) of this notification; must be a type
	 *            for the ontology.
	 * @param arg
	 *            An argument to pass to the observer in the update() method in
	 *            the ObserverNotification object.
	 */
	@SuppressWarnings("unchecked")
	public void notifyObservers(String notifyType, Object arg) {
		setChanged(); // ensure that setChanged() is called (an easy mistake)

		//assert agent.isA(notifyType, "TOP") : "CasaObservable.notifyObservers: eventType isn't found in the ontology: " + notifyType;

		/*
		 * a temporary buffer, used as a snapshot of the state of current
		 * Observers.
		 */
		TreeMap<Observer, String[]> localClone;
		TreeMap<URLDescriptor, String[]> remoteClone;

		if (!hasChanged())
			return;

		synchronized (this) {
			/*
			 * We don't want the Observer doing callbacks into arbitrary code
			 * while holding its own Monitor. The code where we extract each
			 * Observable from the Vector and store the state of the Observer
			 * needs synchronization, but notifying observers does not (should
			 * not). The worst result of any potential race-condition here is
			 * that: 1) a newly-added Observer will miss a notification in
			 * progress 2) a recently unregistered Observer will be wrongly
			 * notified when it doesn't care
			 */
			clearChanged();

			localClone = (TreeMap<Observer, String[]>) localObservers.clone();
			remoteClone = (TreeMap<URLDescriptor, String[]>) remoteObservers.clone();
		}

		ObserverNotification notification = new ObserverNotification(agent, notifyType, arg);

		// notify local observers
		for (Observer observer : localClone.keySet()) {
			String[] observingTypes = localClone.get(observer);
			if (observingTypes == null)
				observer.update(this, notification);
			else {
				for (String type : observingTypes) {
					if (agent.isA(notifyType, type)) {
						observer.update(this, notification);
						break;
					}
				}
			}
		}

		// notify remote observer
		for (URLDescriptor observer : remoteClone.keySet()) {
			boolean send = false;
			String[] observingTypes = remoteClone.get(observer);
			if (observingTypes == null)
				send = true;
			else {
				for (String type : observingTypes) {
					if (agent.isA(notifyType, type)) {
						send = true;
						break;
					}
				}
			}
			if (send) {
				((TransientAgent) agent).sendMessage(ML.INFORM, ML.UPDATE, observer, ML.CONTENT, CASAUtil.serialize(notification));
			}
		}

	}
	
	/**
	 * Merges two event lists. A null list represents the maximum matching list.
	 * @param a the first list to merge
	 * @param b the second list to merge
	 * @return a new list that will match both events from list a or b.
	 */
	private final String[] merge(String[] a, String[] b){
		if(a==null || b==null)
			return null;
		
		HashSet<String> ret = new HashSet<String>();
		for(String s : a)
			ret.add(s);
		for(String s : b)
			ret.add(s);
		
		return ret.toArray(new String[0]);
	}

	/**
	 * Description: add an observer along with the type of event it wishes to be
	 * notified about. If the agent is actually a local agent then it will instead be notified
	 * locally instead of via a message.
	 * <p>
	 * TODO: rkyee: Be careful with this! You can cause an infinite inform loop
	 * 
	 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
	 * @version 0.9
	 */
	@Override
	public void addObserver(URLDescriptor observer, String... notifyTypes) {
		if (observer == null)
			throw new NullPointerException();
		assert !observer.equals(agent.getURL());
		if(notifyTypes.length == 0)
			notifyTypes = null;
		
		if(AgentLookUpTable.containsKey(observer) && AgentLookUpTable.get(observer) instanceof Observer) {
			agent.println("observer", "Adding URL observer "+observer+" as LOCAL observer.");
			addObserver((Observer)AgentLookUpTable.get(observer),notifyTypes);
		}
		else if (!remoteObservers.containsKey(observer)) {
			agent.println("observer", "Adding URL observer "+observer+" to remote observers.");
			remoteObservers.put(observer, notifyTypes);
		}
		else {
			agent.println("observer", "Updating URL observer "+observer+" in remote observers.");
			remoteObservers.put(observer, merge(remoteObservers.get(observer),notifyTypes));
		}
	}

	@Override
	public void deleteObserver(URLDescriptor observer) {
		if(AgentLookUpTable.containsKey(observer) && AgentLookUpTable.get(observer) instanceof Observer) {
			agent.println("observer", "Deleting URL observer "+observer+" as LOCAL observers.");
			localObservers.remove(observer);
		}
		else {
			agent.println("observer", "Deleting URL observer "+observer+" in remote observers.");
			remoteObservers.remove(observer);
		}
	}

	/**
	 * Adds an observer to the set of observers for this object, provided that
	 * it is not the same as some observer already in the set. The order in
	 * which notifications will be delivered to multiple observers is not
	 * specified. See the class comment.
	 * 
	 * @param o
	 *            an observer to be added.
	 * @throws NullPointerException
	 *             if the parameter o is null.
	 */
	@Override
	public synchronized void addObserver(Observer o) {
		assert o!=this && o!=agent;
		this.addObserver(o, (String[]) null);
	}

	/**
	 * Adds an observer to the set of observers for this object, provided that
	 * it is not the same as some observer already in the set. The order in
	 * which notifications will be delivered to multiple observers is not
	 * specified. See the class comment.
	 * 
	 * @param o
	 *            an observer to be added.
	 * @throws NullPointerException
	 *             if the parameter o is null.
	 */
	@Override
	public synchronized void addObserver(Observer o, String... notifyTypes) {
		assert o!=this && o!=agent;																					//ensure that the Observer is not this object or this object's agent
		if (o == null)																											//if the observer is null
			throw new NullPointerException();																		//throw null pointer exception
		if(!localObservers.containsKey(o)) {																//if the observer is not already in the set of localObservers
			agent.println("observer", "Adding local observer of type "+o.getClass().getName()+", id "+o.hashCode()+" to local observers.");
			localObservers.put(o, notifyTypes);																	//add the observer along with the type of events to notify it of
		}
		else {																															//otherwise
			agent.println("observer", "Updating local observer of type "+o.getClass().getName()+", id "+o.hashCode()+" in local observers.");
			localObservers.put(o, merge(localObservers.get(o),notifyTypes));		//merge the events to notify this event of
		}
	}

	/**
	 * Deletes an observer from the set of observers of this object. Passing
	 * <CODE>null</CODE> to this method will have no effect.
	 * 
	 * @param o the observer to be deleted.
	 */
	@Override
	public synchronized void deleteObserver(Observer o) {
		if (localObservers.containsKey(o)) {
			agent.println("observer", "Deleting local observer of type "+o.getClass().getName()+", id "+o.hashCode()+" from local observers.");
			localObservers.remove(o);															//remove the observer from the list of this object's local observers
		}
		else if (agent.isLoggingTag("observer")) { //warning, the observer wasn't in the list
			StringBuilder buf = new StringBuilder();
			for (Observer local:localObservers.keySet()) {
				buf.append("\n\t").append(local.toString());
			}
			agent.println("observer", "Attempt to delete local observer of type "+o.getClass().getName()+", id "+o.hashCode()+" when it isn't already an Observer.\n  "+o+"\nCurrent observers are:"
						+buf.toString()+"\nStack trace:", (Throwable)null);
		}
	}

	/**
	 * Clears the observer list so that this object no longer has any observers.
	 */
	@Override
	public synchronized void deleteObservers() {
		agent.println("observer", "Deleting all observers");
		localObservers.clear();
		remoteObservers.clear();
	}

	/**
	 * Returns the number of observers of this <tt>Observable</tt> object.
	 * 
	 * @return the number of observers of this object.
	 */
	@Override
	public synchronized int countObservers() {
		return localObservers.size() + remoteObservers.size();
	}

	/**
	 * Returns the number of local observers of this <tt>Observable</tt> object.
	 * 
	 * @return the number of observers of this object.
	 */
	public synchronized int countLocalObservers() {
		return localObservers.size();
	}

	/**
	 * Returns the number of remote observers of this <tt>Observable</tt>
	 * object.
	 * 
	 * @return the number of observers of this object.
	 */
	public synchronized int countRemoteObservers() {
		return remoteObservers.size();
	}

	@Override
	public int compareTo(CasaObservableObject o) {
		return Integer.signum(o.hashCode()-this.hashCode());
	}

}
