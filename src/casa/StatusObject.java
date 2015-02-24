package casa;

import casa.util.CASAUtil;

/**
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * Extends {@link Status} to include a type-parameterized return value.
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

/**a class that extends the functionality of Status to include a generic object
 * 
 * @author <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 * @param <T>		the generice type so that any object can have a status
 */
public class StatusObject<T> extends Status {
  /**the object of whome's status we are concerned with
	 */
  private T object;

  /**
   * the default constructor of StatusObject
   */
  public StatusObject() {
    super();

    this.object = null;
  }

  /**a constructor that includes additional information
   * 
   * @param status				the integer status we want to set to
   * @param explanation		the explanation of the status
   */
  public StatusObject(int status, String explanation) {
    super(status, explanation);

    this.object = null;
  }

  /**a constructor that includes additional information
   * 
   * @param status		the integer status we want to set to
   * @param obj				the object who's status we are concerned with
   */
  public StatusObject(int status, T obj) {
    super(status);

    this.object = obj;
  }

  /**a constructor that includes additional information
   * 
   * @param status				the integer status we want to set to
   * @param explanation		the explanation of the status
   * @param obj						the object who's status we are concerned with
   */
  public StatusObject(int status, String explanation, T obj) {
    super(status, explanation);

    this.object = obj;
  }

  /**a constructor that includes additional information
   * 
   * @param obj		the object who's status we are concerned with
   */
  public StatusObject(T obj) {
    super();

    this.object = obj;
  }

  /**a constructor for a status object of type TokenParser
   * 
   * @param p							the token parser to supply
   * @throws Exception		see TokenParser for information
   */
  public StatusObject(TokenParser p) throws Exception {
    this();
    fromString(p);
  }

  /**a function that returns the generic object of this status object (might be null if not set)
	 * @return				the generic object
	 */
  public T getObject () {
    return object;
  }

  /**a function that sets the generic object of this status object
	 * @param obj			the generic object to set to
	 */
  public void setObject (T obj) {
    this.object = obj;
  }
  /**a function to check if the StatusOBject's generic object is an instance of the testClass
   * 
   * @param testClass		the testClass to see if the generic object is an instance there-of
   * @return	a boolean
   */
  public boolean containsType (Class<?> testClass) {
    return testClass.isInstance (object);
  }

  @Override
	public String toString_extension () {
    String str = CASAUtil.serialize(object);
    return CASAUtil.toQuotedString (str);
  }

  @SuppressWarnings("unchecked")
	@Override
	public void fromString_extension (TokenParser parser) throws Exception {
    String str = parser.getNextToken ();
    object = (T) CASAUtil.unserialize(str, null);
  }
}
