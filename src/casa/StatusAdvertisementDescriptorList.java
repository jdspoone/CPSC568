package casa;

import casa.agentCom.URLDescriptor;

import java.util.Iterator;
import java.util.Vector;

/**
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public class StatusAdvertisementDescriptorList extends Status {
  /**
	 */
  private Vector advertisements;

  public StatusAdvertisementDescriptorList () {
    advertisements = null;
  }

  public StatusAdvertisementDescriptorList (int status, Vector advertisements) {
    super (status);
    this.advertisements = advertisements;
  }

  public StatusAdvertisementDescriptorList (int status, String explanation,
                                            Vector advertisements) {
    super (status, explanation);
    this.advertisements = advertisements;
  }

  /**
	 * @param advertisements
	 */
  public void setAdvertisements (Vector advertisements) {
    this.advertisements = advertisements;
  }

  /**
	 * @return
	 */
  public Vector getAdvertisements () {
    return advertisements;
  }

  public String toString_extension () {
    if (advertisements == null) {
      return " ";
    } else {
      StringBuffer buffer = new StringBuffer ();

      for (Iterator i = advertisements.iterator (); i.hasNext (); ) {
        buffer.append (ML.BLANK);
        buffer.append (TokenParser.makeFit (i.next ().toString ()));
      }

      return buffer.toString ();
    }
  }

  public void fromString_extension (TokenParser parser) throws Exception {
    Vector vector = new Vector ();
    String str;

    for (str = parser.getNextToken (); str != null && !str.equals (")");
               str = parser.getNextToken ()) {
      vector.add (URLDescriptor.make (str));
    }

    if (str.equals (")"))
      parser.putback ();

    setAdvertisements (vector);
  }
}