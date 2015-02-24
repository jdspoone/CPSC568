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

import java.util.Iterator;

/**
 * A sequence of actions, all of which to be executed in order.  Method {@link #execute()} stops when
 * any actions fails and that status is returned; if it succeeds in all executions but some are
 * warnings (+ve) then the FIRST warning status is returned (but all are executed).
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
public class Seq extends CompositeAction {

	/**
	 * @param actions The actions to be executed when {@link #execute()} is called.
	 */
	public Seq(Action... actions) {
		super(actions);
	}

	@Override
	public Status execute(Object... paramValues) throws IllegalOperationException, ClassCastException {
		if (paramValues!=null && paramValues.length>0)
			throw new IllegalOperationException("Action seq cannot take any arguments");
		Status stat = new Status(0);
		for (Action action: this) {
			stat = action.execute();
			if (stat.getStatusValue()<0) {
				return stat;
			}
		}
		return stat;
	}

	/**
	 * Iterates all of the actions in order.
	 */
	@Override
	public Iterator<Action> iterator() {
		return actions.iterator();
	}
}
