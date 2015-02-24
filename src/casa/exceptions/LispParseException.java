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
package casa.exceptions;

import java.text.ParseException;

/**
 * A subclass of {@link java.text.ParseException} who's {@link #toString()}
 * is a little more informative.
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class LispParseException extends ParseException {

	/**
	 * @param s
	 * @param errorOffset
	 */
	public LispParseException(String s, int errorOffset) {
		super(s, errorOffset);
	}

	/**
	 * @return the super {@link java.text.ParseException} with a next line of describing 
	 * the line line number of the error.
	 * @see java.lang.Throwable#toString()
	 */
	@Override
	public String toString() {
		return super.toString()+(getErrorOffset()==-1?"":("\n  - at line "+getErrorOffset()));
	}

}
