package casa.util;

import java.util.Iterator;

/**
 * Simple implementation of java.util.Iterator that automatically throws an
 * exception if remove is attempted.
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
 * @author <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public class ReadOnlyIterator implements Iterator {
  private Iterator iterator;

  public ReadOnlyIterator (Iterator anIterator) {
    if (anIterator == null)
      throw new IllegalArgumentException (
          "ReadOnlyIterator needs a non-null Iterator for construction");
    iterator = anIterator;
  }

  /**
   * uses the underlying iterators' method
   *
   * @return true if the iteration has more elements.
   */
  public boolean hasNext () {
    return iterator.hasNext ();
  }

  /**
   * uses the underlying iterators' method
   *
   * @return the next element in the interation.
   */
  public Object next () {
    return iterator.next ();
  }

  /**
   * this is an unsupported operation, and as such an
   * UnsupportedOperationException will be thrown if this method is called.
   */
  public void remove () {
    throw (new UnsupportedOperationException ());
  }
}