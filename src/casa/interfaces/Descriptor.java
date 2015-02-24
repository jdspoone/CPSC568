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
package casa.interfaces;

import casa.Status;
import casa.event.Event;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public interface Descriptor<T> {

	/**
	 * Returns true if descriptor matches the <em>instance</em> in the context of 
	 * the <em>agent</em>.  The <em>instanceInfo</em> is used to pass any additional
	 * info (such as a message object) for additional context.
	 * @param agent the agent that forms the context of evaluation.
	 * @param instance the object to match against.
	 * @return true if this descriptor matches the instance in the context of the agent and instanceInfo.
	 */
	public Status isApplicable(PolicyAgentInterface agent, T instance);

}
