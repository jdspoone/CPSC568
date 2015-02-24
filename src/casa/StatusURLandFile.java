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
public class StatusURLandFile extends StatusURLDescriptor
{
   /**
	 */
  private String file;

   public StatusURLandFile() {
     file = null;
   }

   public StatusURLandFile( int           status,
                            URLDescriptor url,
                            String        file ) {
      super( status, url );
      this.file = file;
   }

   public StatusURLandFile (String s) throws Exception {
     super(s);
   }

   public StatusURLandFile( int           status,
                            URLDescriptor url,
                            String        file,
                            String        comment ) {
      super( status, url, comment );
      this.file = file;
   }

   /**
	 * @param file
	 */
  public void setFile( String file) {
      this.file = file;
   }
   /**
	 * @return
	 */
  public String getFile() {
      return file;
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
      return            super.toString_extension() + " \"" + file +"\"";
   }

  public void fromString_extension( TokenParser parser ) throws Exception {
    super.fromString_extension(parser);
    file = parser.getNextToken();
  }
}
