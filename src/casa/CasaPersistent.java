/**
 * <p>
 * Title: CASA Agent Infrastructure
 * </p>
 * <p>
 * Description:
 * </p>
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
 * <p>
 * Company: Knowledge Science Group, University of Calgary
 * </p>
 * 
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
package casa;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares that the field of an agent should be preserved between activations.
 * An agent is persistent if it inherits from the {@link casa.Agent} class and
 * sets the attribute <code>persistent</code> to true (which can be done either
 * in the agent code of via the command line). The agent stores the information
 * in a file according the the LAC's setup. All you have to do to make at
 * attribute persistent is to mark it's declaration with the
 * <code>&#64;CasaPersistent</code> annotation. For example:
 * 
 * <pre>
 * // a simple persistent boolean attribute that will be stored in the properties under "myFlag" 
 * &#64;CasaPersistent
 * boolean myFlag;
 * 
 * //a persistent object that will be stored in the properties under "options.x" 
 * //and "options.y" because it has at least one &#64;CasaPersistent attribute itself. 
 * public class Options {
 *   &#64;CasaPersistent int x = 4;
 *   &#64;CasaPersistent double y = 7.5;
 * }
 * 
 * &#64;CasaPersistent
 * Options options;
 * //a persistent object that will be stored in the properties under "stuff" in 
 * //the standard CASA serial format (because none of it's properties are marked
 * //<code>&#64;CasaPersistent</code>).  Note that the class Stuff MUST have a
 * //<code>toString()</code> method and a corresponding constructor that takes a
 * //single string.
 * public class Stuff {
 *   int x = 4;
 *   double y = 7.5;
 *   public Stuff() {...}
 *   public Stuff(String persistData) {...}
 *   &#64;Override
 *   public toString() {...}
 * }
 * 
 * &#64;CasaPersistent
 * Stuff stuff = new Stuff();
 * </pre>
 * 
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD })
public @interface CasaPersistent {
}
