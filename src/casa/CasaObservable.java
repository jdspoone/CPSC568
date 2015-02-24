package casa;

import casa.agentCom.URLDescriptor;

import java.util.Observer;

/**
 * A {@link CasaObservable} represents an object that can be "observed". Typically, 
 * an observed object will notify its observers when certain events occur. These
 * events can be filtered to notify an observer for only a specific subset of
 * events.
 * 
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
 * @author <a href="mailto:rkyee@ucalgary.ca">Ryan Yee</a>
 * @see java.util.Observer
 * @see casa.CasaObservableObject
 */
public interface CasaObservable {
	public void addObserver(Observer o, String... notifyTypes);

	public void addObserver(URLDescriptor observer, String... notifyTypes);

	public int countObservers();

	public void deleteObserver(Observer o);

	public void deleteObserver(URLDescriptor observer);

	public void deleteObservers();

	public boolean hasChanged();

	public void notifyObservers();

  public void notifyObservers(String eventType, Object argument);
  
	/**
	 * TOP is the notification type
	 * @param arg the argument to use
	 */
	public void notifyObserversWithTop(Object arg);

	/**
	 * Notify with no arguments
	 * @param notifyType the notification type to use
	 */
	public void notifyObserversWithNoArg(String notifyType);
}
