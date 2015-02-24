/**
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. 
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  
 * The  Knowledge Science Group makes no representations about the suitability
 * of  this software for any purpose.  It is provided "as is" without express
 * or implied warranty.</p>
 * <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
package casa.actions.rk;

import casa.Status;
import casa.exceptions.IllegalOperationException;

/**
 * An abstract action.
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
public abstract class Action implements Comparable<Action> {
	
	static private ActionIO defaultIO = new ActionIOLisp();
	
	private ActionIO IO = defaultIO;
	
	/**
	 * @return the iO
	 */
	public ActionIO getIO() {
		return IO;
	}

	/**
	 * @param iO the iO to set
	 */
	public void setIO(ActionIO iO) {
		IO = iO;
	}

	/**
	 * @return the defaultIO
	 */
	public static ActionIO getDefaultIO() {
		return defaultIO;
	}

	/**
	 * @param defaultIO the defaultIO to set
	 */
	public static void setDefaultIO(ActionIO defaultIO) {
		Action.defaultIO = defaultIO;
	}

	/**
	 * Perform the action.  For subclasses representing simple actions, this
	 * is just executing some code, but for composite actions this be recursively
	 * executing some selection of actions.
	 * @return A status or subclass or {@link casa.Status}; the value part of the 
	 * status should be -ve to indicate failure, 0 to indicate success, ad +ve to
	 * indicate a warning. 
	 */
	abstract public Status execute(Object... paramValues) throws IllegalOperationException, ClassCastException;
	
	/**
	 * The name of the action should generally be an entry in the ontology subsumed
	 * by the <em>action</em> type.  Caution should also be observed in picking the 
	 * name so that it does not conflict with reserved words in Lisp or XML, etc.
	 * @return the name of the action.
	 */
	abstract public String getName();
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Action arg0) {
		return getName().compareTo(arg0.getName());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Action)) 
			return false;
		if (obj.getClass().equals(getClass()))
			return false;
		return compareTo((Action)obj)==0;
	};

	/**
	 * The contract of this method is that it will return a String which can
	 * be used as the parameter to {@link #fromStringLocal(String)} to create an
	 * object identical to this one.
	 * @return a String representation of this object.
	 */
	@Override
	public String toString() {
		return IO.toString(this);
	}
	
}
