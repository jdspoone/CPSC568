package casa.util;

/**
 * A <code>PropertyException</code> object is an exception used by the
 * <code>PropertyMap</code> set of classes.
 *
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary.
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  The
 * Knowledge Science Group makes no representations about the suitability of
 * this software for any purpose.  It is provided "as is" without express or
 * implied warranty.</p>
 *
 * @see PropertiesMap
 * @see PropertiesMapXML
 * @see Property
 * @see BooleanProperty
 * @see StringProperty
 * @see IntegerProperty
 * @see LongProperty
 * @see FloatProperty
 * @see DoubleProperty
 *
 * @author Jason Heard
 * @author <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public class PropertyException extends Exception {

  public PropertyException() {
    super ();
  }

  public PropertyException (String message) {
    super (message);
  }

  public PropertyException (String message, Throwable cause) {
    super (message, cause);
  }

  public PropertyException (Throwable cause) {
    super (cause);
  }
}