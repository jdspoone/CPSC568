package casa;

import casa.interfaces.AdvertisementSearchInterface;

/**
 * An <code>AdvertisementSearch</code> object is an implimentation of the <code>AdvertisementSearchInterface</code> interface.  It is used to search for a particular advertisement as defined by an <code>AdvertisementDescriptor</code>.  The search is defined by a single string which must match the following specifications. <ul> <li>The string must be a <var>proposition</var> or a <var>logical statement</var>.</li> <li>A <var>proposition</var> must be in one of the following forms:</li> <ul> <li><var>c</var> <code>==</code> <var>d</var><br> evaluates to <code>true</code> if <var>c</var> and <var>d</var> are identical; <code>false</code> otherwise.</li> <li><var>c</var> <code>&lt;</code> <var>d</var><br> evaluates to <code>true</code> if <var>c</var> and <var>d</var> are both a <var>number</var> and <var>c</var> is less than <var>d</var> or if <var>c</var> and <var>d</var> are both <var>string</var>s and <var>c</var> is lexigraphically before <var>d</var>; <code>false</code> otherwise.</li> <li><var>c</var> <code>&lt;=</code> <var>d</var><br> evaluates to <var>c</var> <code>&lt;</code> <var>d</var> <code>||</code> <var>c</var> <code>==</code> <var>d</var>.</li> <li><var>c</var> <code>&gt;</code> <var>d</var><br> evaluates to <code>true</code> if <var>c</var> and <var>d</var> are both a <var>number</var> and <var>c</var> is greater than <var>d</var> or if <var>c</var> and <var>d</var> are both <var>string</var>s and <var>c</var> is lexigraphically after <var>d</var>; <code>false</code> otherwise.</li> <li><var>c</var> <code>&gt;=</code> <var>d</var><br> evaluates to <var>c</var> <code>&gt;</code> <var>d</var> <code>||</code> <var>c</var> <code>==</code> <var>d</var>.</li> <li><var>c</var> <code>==</code> <var>d</var><br> evaluates to <code>true</code> if <var>c</var> and <var>d</var> are not identical; <code>false</code> otherwise.</li> <li><var>c</var> <code>=re</code> <var>d</var><br> evaluates to <code>true</code> if <var>c</var> is a <var>string</var> that matches the <var>regular expression</var> <var>d</var>; <code>false</code> otherwise.</li> </ul> <li><var>c</var> and <var>d</var> must be a <var>literal string</var>, <var>literal number</var>, or a <var>variable</var>.</li> <li>A <var>logical statement</var> must be in one of the following forms:</li> <ul> <li><var>a</var> <code>||</code> <var>b</var><br> evaluates to <code>false</code> if <var>a</var> and <var>b</var> both evaluate to <code>false</code>; <code>true</code> otherwise.</li> <li><var>a</var> <code>&&</code> <var>b</var><br> evaluates to <code>true</code> if <var>a</var> and <var>b</var> both evaluate to <code>true</code>; <code>false</code> otherwise.</li> <li><code>!</code> <var>b</var><br> evaluates to <code>true</code> if <var>b</var> evaluates to <code>false</code>; <code>false</code> otherwise.</li> <li><var>a</var> <code>-&gt;</code> <var>b</var><br> evaluates to <code>false</code> if <var>a</var> evaluates to <code>true</code> and <var>b</var> evaluates to <code>false</code>; <code>true</code> otherwise.</li> <li><var>a</var> <code>&lt;-</code> <var>b</var><br> evaluates to <code>false</code> if <var>a</var> evaluates to <code>false</code> and <var>b</var> evaluates to <code>true</code>; <code>true</code> otherwise.</li> <li><var>a</var> <code>&lt;&gt;</code> <var>b</var><br> evaluates to <code>true</code> if <var>a</var> and <var>b</var> both evaluate to the same truth value; <code>false</code> otherwise.</li> </ul> <li><var>a</var> and <var>b</var> must be a <var>proposition</var> or a <var>logical statement</var>.</li> <li>A <var>literal string</var> is string in the form <code>"</code><var>.*</var><code>"</code> that evaluates to the <var>string</var> contained in the quotes (<code>"</code>s).</li> <li>A <var>literal number</var> is string in the form <var>"-"?[0-9]*"."?[0-9]*</var> that evaluates to a <var>number</var>.</li> <li>A <var>variable</var> is a stored value from the advertisement that evaluates to a <var>string</var> or a <var>number</var>.</li> <li>A <var>string</var> is a character string.</li> <li>A <var>number</var> is either a integer or floating point number.</li> </ul> <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @see TransientAgent
 * @author  Jason Heard
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>, Jason Heard
 * @version 0.9
 */

public final class AdvertisementSearch implements AdvertisementSearchInterface {
  /**
	 */
  private String searchString = "";
  private String treeString = null;

  public AdvertisementSearch () {
    this.searchString = "";
  }

  public AdvertisementSearch (String newSearchString) {
    if (newSearchString == null) {
      this.searchString = "";
    } else {
      this.searchString = newSearchString;
    }
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
  }

  public boolean match (AdvertisementDescriptor advertisement) {
    // parse string into tree
    // check given advertisementDescriptor

    return false;
  }
}