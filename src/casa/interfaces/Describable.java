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
package casa.interfaces;

import java.util.Set;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public interface Describable extends Cloneable {
	/**
	 * Retrieves a parameter of this object.  If the parameter is not found, returns null.
	 *
	 * @param key String indication which parameter to retrieve.
	 *
	 * @returns The parameter specified, or null if the parameter is not found.
	 */
	public String getParameter (String key);


	/**
	 * Sets a parameter in the object. If the parameter has not been defined then
	 * there is a fault in the document - likely it was not parsed prior to calling
	 * setParameter, so the document was not instantiated.
	 * If the parameter has been previously defined it is updated to reflect the
	 * value specified.
	 *
	 * @param parameter name of the parameter to set/add
	 * @param value     string value to associate with the parameter
	 */
	public void setParameter (String parameter, String value);
	
  /**
   * @return the set of all the keys for this object
   */
  public Set<String> keySet();


	public Describable clone();

}
