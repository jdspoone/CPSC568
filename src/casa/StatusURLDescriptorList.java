package casa;

import casa.agentCom.URLDescriptor;
import casa.exceptions.URLDescriptorException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
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
 * @todo add doc
 */
public class StatusURLDescriptorList extends Status {
  private List<URLDescriptor> urls;

  /**
   * Default constructor
   */
  public StatusURLDescriptorList () {
    urls = null;
  }

  /**
   *
   * @param status contains either a -1 when nothing is connected/registered.
   *                                  0 otherwise.
   *
   * @param urls contains the full address of the LAC.
   */
  public StatusURLDescriptorList (int status,
                                  List<URLDescriptor> urls) {
    super (status);
    this.urls = urls;
  }

  public StatusURLDescriptorList (int status, String explanation,
                                  List<URLDescriptor> urls) {
    super (status, explanation);
    this.urls = urls;
  }

  /**
   * @param urls contains the full address (IPAddress and port #) of the LAC
   */
  public void setURLs (List<URLDescriptor> urls) {
    this.urls = urls;
  }

  /**
   * @return full address of the LAC
   */
  public List<URLDescriptor> getURLs () {
    return urls;
  }

  /**
   * other methods
   *
   * @return the buffer with the full addresses of the LAC and the CDs (including
   *         their names, i.e. testCD or any one you choose to join).
   */
  public String toString_extension () {
    if (urls == null) {
      return " ";
    } else {
      StringBuffer buffer = new StringBuffer ();

      for (URLDescriptor u : urls) {
        buffer.append (ML.BLANK);
        buffer.append (TokenParser.makeFit (u.toString ()));
      }

      return buffer.toString ();
    }
  }

  /**
   *
   */
  public void fromString_extension (TokenParser parser) throws Exception {
    try {
      List<URLDescriptor> vector = new ArrayList<URLDescriptor> ();
      String str;

      for (str = parser.getNextToken (); str != null && !str.equals (")");
          str = parser.getNextToken ()) {
        vector.add (URLDescriptor.make (str));
      }
      if (str.equals (")"))
        parser.putback ();

      setURLs (vector);
    } catch (URLDescriptorException e) {
      e.printStackTrace ();
      throw new ParseException ("Cannot parse URLDescriptor", 0);
    }
  }

  // testing only
  public static void main (String args[]) {
    StatusURLDescriptorList s = new StatusURLDescriptorList ( -1, null);
    URLDescriptor temp = null;
    URLDescriptor temp2 = null;
    try {
      temp = URLDescriptor.make ("127.0.0.1", "9005");
      temp2 = URLDescriptor.make ("127.0.0.1", "9006");
    } catch (URLDescriptorException ex) {
      ex.printStackTrace ();
    }

    ArrayList<URLDescriptor> v = new ArrayList<URLDescriptor> ();
    v.add (temp);
    v.add (temp2);

    String testString = (0 + ML.NULL + temp.toString (null) + ML.NULL +
                         temp2.toString (null));

    s = new StatusURLDescriptorList (0, v);

    System.out.println (s.toString ());
  }
}