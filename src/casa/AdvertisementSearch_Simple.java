package casa;

import casa.interfaces.AdvertisementSearchInterface;
import casa.util.PropertyException;

/**
 * An <code>AdvertisementSearch_Simple</code> object is an implimentation of the <code>AdvertisementSearchInterface</code> interface. It is used to search for a particular advertisement as defined by an <code>AdvertisementDescriptor</code>. The search is defined by a single string which must be of the following form: <pre> searchString ::= element { &quot;&amp;&amp;&quot; element } element      ::= property { &quot;=&quot; value } </pre> The match() method will return true iff each of the listed properties is in the AdvertisementDescriptor, and if there's a corresponding value part, the value is equal (using a string comparison). <p> Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation. The Knowledge Science Group makes no representations about the suitability of this software for any purpose. It is provided "as is" without express or implied warranty. </p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer </a>
 * @version 0.9
 */

public final class AdvertisementSearch_Simple implements
    AdvertisementSearchInterface {
  class Pair {
    String key;
		String value;

    public Pair (String k, String v) {
      key = k;
      value = v;
    }
  }

  /**
	 */
  private String searchString = "";
  /**
	 */
  private Pair[] table = null;

  public AdvertisementSearch_Simple () {
    this.searchString = "";
  }

  public AdvertisementSearch_Simple (String newSearchString) {
    if (newSearchString == null) {
      this.searchString = "";
    } else {
      this.searchString = newSearchString;
    }
    init (searchString);
  }

  /**
	 * @return
	 */
  public String getSearchString () {
    return searchString;
  }

  /**
	 * @param newSearchString
	 */
  public void setSearchString (String newSearchString) {
    if (newSearchString == null) {
      this.searchString = "";
    } else {
      this.searchString = newSearchString;
    }
    init (searchString);
  }

  public boolean match (AdvertisementDescriptor advertisement) {
    // parse string into tree
    // check given advertisementDescriptor
    for (int i = 0, end = table.length; i < end; i++) {
      String key = table[i].key;
      if (!advertisement.hasProperty (key))
        return false;
      String val = table[i].value;
      try {
        if (!advertisement.getStringProperty (key).equals (val)) {
          return false;
        }
      } catch (PropertyException ex) {
        return false;
      }
    }

    return true;
  }

  private void init (String s) {
    String[] a = s.split ("&&");
    table = new Pair[a.length];
    for (int i = 0, end = a.length; i < end; i++) {
      String[] b = a[i].split ("=");
      table[i] = new Pair (b[0].trim (), b.length < 2 ? null : b[1].trim ());
    }
  }
}