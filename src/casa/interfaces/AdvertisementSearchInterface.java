package casa.interfaces;

import casa.AdvertisementDescriptor;

/**
 * <code>AdvertisementSearchInterface</code> is an interface used to search a
 * <code>YellowPagesAgentInterface</code>.  It has only one function,
 * <code>match()</code>, that is used to determine whether a specific
 * <code>AdvertisementDescriptor</code> is matched by this search.
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
 *
 * @author Jason Heard
 */

public interface AdvertisementSearchInterface {
  /**
   * Determines whether the given advertisement is matched by this search.
   *
   * @param advertisement The advertisement that should be judged.
   * @return <code>true</code> if the specified advertisement is matched by
   * this search; <code>false</code> otherwise.
   */
  public boolean match (AdvertisementDescriptor advertisement);
}