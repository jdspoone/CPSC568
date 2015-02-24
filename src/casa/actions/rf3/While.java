/**
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. 
 * Permission to use, copy, modify, distribute and sell this software and its 
 * documentation for any purpose is hereby granted without fee, provided that the 
 * above copyright notice appear in all copies and that both that copyright notice 
 * and this permission notice appear in supporting documentation.  The  Knowledge 
 * Science Group makes no representations about the suitability of  this software 
 * for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
package casa.actions.rf3;

import casa.Status;
import casa.exceptions.IllegalOperationException;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class While extends Loop {
	
	Condition loopCondition;

	/**
	 * @param body
	 */
	public While(Condition loopCondition, Action body) {
		this (null, loopCondition, body);
	}

	/**
	 * @param theName
	 * @param body
	 */
	public While(String theName, Condition loopCondition, Action body) {
		super(theName, body);
		assert loopCondition != null;
		this.loopCondition  = loopCondition;
	}

	/* (non-Javadoc)
	 * @see casa.actions.rf3.AbstractAction#run(java.lang.Object[])
	 */
	@Override
	protected final Status run(Object... theParameters) throws IllegalOperationException {
		assert theParameters.length==0;
		while (loopCondition.isEnabled()) 
			body.execute(theParameters);
		return null;
	}

}
