package casa.exceptions;

     /*
      * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary.
      * Permission to use, copy, modify, distribute and sell this software and its
      * documentation for any purpose is hereby granted without fee, provided that
      * the above copyright notice appear in all copies and that both that copyright
      * notice and this permission notice appear in supporting documentation.  The
      * Knowledge Science Group makes no representations about the suitability of
      * this software for any purpose.  It is provided "as is" without express or
      * implied warranty.</p>
      *
      * @author <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
      * @version 0.9
     */
/**
 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
public class URLDescriptorException extends Exception {
 	private static final long serialVersionUID = 1L;

	public static final int UNKNOWN_ERROR =  0;

	public static final int HOST_ERROR = -1;

	public static final int PORT_ERROR = -2;

	public static final int NULL_ERROR = -3;

	public static final int AMBIGUOUS_ERROR = -4;

	protected int error;
  private String explanation = null;

  public URLDescriptorException() {
    this.error = UNKNOWN_ERROR;
  }

  public URLDescriptorException( int error ) {
    this.error = error;
  }

  public URLDescriptorException( String explanation ) {
    this.error = UNKNOWN_ERROR;
    this.explanation = explanation;
  }

  public URLDescriptorException( int error, String explanation ) {
    this.error = error;
    this.explanation = explanation;
  }

  /**
	 * @return
	 */
  public int getError() {
    return error;
  }

  public String toString() {
    return super.toString() + (explanation==null ? "" : ": "+explanation);
  }
}
