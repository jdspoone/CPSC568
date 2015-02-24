package casa;

import casa.agentCom.URLDescriptor;


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
public class StatusURLDescriptor extends Status
{
   /**
	 */
  private URLDescriptor url;

   public StatusURLDescriptor() {
   }

   public StatusURLDescriptor( int           status,
                               URLDescriptor url ) {
      super( status );
      this.url = url;
   }

   public StatusURLDescriptor( int           status,
                               URLDescriptor url,
                               String        comment ) {
      super( status, comment );
      this.url = url;
   }

   public StatusURLDescriptor(String s) throws Exception {
     super(s);
   }

   public void setURL( URLDescriptor url) {
      this.url = url;
   }
   public URLDescriptor getURL() {
      return url;
   }
   /**
    * other methods
    * stringBuffer buffer --> Initializes a new String object with the contents
    *                         of the StringBuffer argument.
    *
    * @return buffer.toString() --> Converts the StringBuffer to a String.  It
    *                               contains either a null (when not registered);
    *                               or the full address (of an Agent, for example)
    *                               ie 136.159.14.232:12534
    */
   public String toString_extension() {
      return            url.toString(null);
   }

  public void fromString_extension( TokenParser parser ) throws Exception {
    url = URLDescriptor.fromString( parser );
  }
}
