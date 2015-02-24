package casa;

import casa.util.CASAUtil;
import casa.util.Trace;

import java.text.ParseException;

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
 * An object combining a status integer and an explanation {@link String}.  The integer
 * should be 0 for success, +ive for warning, and -ve for failure.
 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
public class Status implements Cloneable
{
   private int code = 0;
   /**the explanation for the Status
    * 
	  */
  private String explanation = "";

   // success status
   public final static int SUCCESS = 0;

   // warning status is always positive
   public final static int UNKNOWN_WARNING = 1;

   // error status is always negative
   public final static int UNKNOWN_ERROR = -1;
   public final static int UNEXEPCTED_EXCEPTION_CAUGHT = -2;
   public final static int EXCEPTION_CAUGHT = -3;
   public final static int TIMEOUT = -10;
   public final static int UNKNOWN_REPLY = -100;
   public final static int BAD_REPLY_TO_FIELD = -101;
   public final static int BAD_CONTENT_FIELD = -102;

   /**a function to convert the variable representation of a status in code to a string
    * 
    * @return		the string representation of the status variable
    */
   public String codeToString() {
     String body = "unknown error code";
     switch (code) {
       case UNKNOWN_ERROR:                   body = "UNKNOWN_ERROR"; break;
       case SUCCESS:                         body = "SUCCESS"; break;
       case UNKNOWN_WARNING:                 body = "UNKNOWN_WARNING"; break;
       case UNEXEPCTED_EXCEPTION_CAUGHT:     body = "UNEXEPCTED_EXCEPTION_CAUGHT"; break;
       case TIMEOUT:                         body = "TIMEOUT"; break;
       case UNKNOWN_REPLY:                   body = "UNKNOWN_REPLY"; break;
       case BAD_REPLY_TO_FIELD:              body = "BAD_REPLY_TO_FIELD"; break;
       case BAD_CONTENT_FIELD:               body = "BAD_CONTENT_FIELD"; break;
       //case X: body = "x"; break;
     }
     String pre = (code < 0)? "E: ":
                  (code > 0)? "W: ":
                              "S: ";																			//set prepend string to an error, warning or success
     String tail = (explanation==null) ? "."
                                       : (": '" + explanation + "'.");		//set append string to ": 'explanation'." or to "." if explanation is empty
     String ret = pre + body + " (" + String.valueOf(code) + ")" + tail;	//create the return string

     return ret;
   }

   /**
    * the default constructor
    */
   public Status() {
   }
   /**a constructor that takes an integer representation of the current status
    * 
    * @param status		the status to set to
    */
   public Status( int status ) {
      this.code = status;
   }

   /**a constructor that takes the integer representation of the current status with a String explanation
    * 
    * @param status				the status to set to
    * @param explanation	the explanation for the status
    */
   public Status( int status, String explanation ) {
      this.code = status;
      this. explanation = explanation;
   }

  /**
   * Copy constructor
   * @param s the constructor to be copied
  */
  public Status( Status s ) {
  	if (s==null) return;
    this.code = s.getStatusValue();
    this.explanation = s.getExplanation();
  }
   
  @Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public Status( String s ) throws Exception {
     fromString(new TokenParser(s),this);
   }

   public Status( int status, String explanation, Throwable ex ) {
     this(status, explanation + ":\n" + Trace.getStackTraceString(ex));
   }

   public void setStatusValue( int status ) {
     this.code = status;
     this.explanation = "";
   }

   public Status setStatus( int status, String explanation ) {
     this.code = status;
     this. explanation = explanation;
     return this;
   }

   public int getStatusValue() {
      return code;
   }

   /**
	 * @param explanation
	 */
  public void setExplanation( String explanation ) {
     this. explanation = explanation;
   }

   /**
	 * @return
	 */
  public String getExplanation() {
     return (explanation==null || explanation.length()==0)
         ? ""
         : explanation;
   }

   /**
    * other methods
    * @returns the string representation of the Object argument --> status.
    */
   /*
   public String toString() {
      return String.valueOf( code );
   }

   public static void fromString( TokenParser parser,
                                  Status      status ) throws ParseException {
      try {
         status.setStatus( Integer.parseInt( parser.getNextToken() ));
      }
      catch( NumberFormatException e ) {
         status.setStatus(Status.BAD_CONTENT_FIELD, "Expected an integer");
         System.out.println(status.codeToString());
         e.printStackTrace();
         throw new ParseException("Expected an Integer",0);
      }
  }

  public static Status fromString( TokenParser parser ) throws ParseException {
    Status status = new Status();
    fromString( parser, status );
    return status;
  }
  */
  /**
  * other methods
  * @returns the string representation of the Object argument --> status.
  */
  @Override
  public String toString() {
    return "( "
        + String.valueOf( code )
        + ((explanation==null || explanation.length()==0)
           ?" \"\""
           :(" "+CASAUtil.toQuotedString(explanation)))
        + " " + toString_extension()
        + " )";
  }

  /**
   * To be overridden by subclasses to write out any extended data. This method
   * is called just be just before Status.toString() writes out the closing
   * ')'.
   * @return
   */
  protected String toString_extension() {
    return "";
  }

  public static void fromString(TokenParser parser,
                                 Status status) throws Exception {
    status.fromString(parser);
  }

  public void fromString(TokenParser parser) throws Exception {
    String token = parser.getNextToken();
    if (!token.equals("("))
      throw new ParseException("expected '('", 0);
    try {
      setStatusValue(Integer.parseInt(parser.getNextToken()));
    }
    catch (NumberFormatException e) {
      setStatus(Status.BAD_CONTENT_FIELD, "Expected an integer");
      System.out.println(codeToString());
      e.printStackTrace();
      throw new ParseException("expected an integer", 1);
    }
    token = parser.getNextToken();
    setExplanation (token);
    fromString_extension(parser);
    //if (!parser.getNextToken().equals(")")) throw new ParseException("Expected ')'",2);
  }

  /**
   * To be overridden by subclasses to read any additional information from a string.
   * This method is called just before Status.fromString() would read the closing
   * ')'.  This implementation does nothing.
   * @param parser
   * @param status
   * @throws ParseException
   */
  protected void fromString_extension(TokenParser parser) throws Exception {
   }

  /*
   public static Status fromString(TokenParser parser) throws ParseException {
    Status status = new Status();
    fromString(parser, status);
    return status;
  }
  */

  public static Status fromString2(String in) throws Exception {
    Status status = new Status();
    TokenParser parser = new TokenParser(in);
    fromString(parser, status);
    return status;
  }

  public static void main(String[] args) {
    Status status = new Status(5,"test \"1\" 2 3 ");
    String s = CASAUtil.serialize(status);
    System.out.println(s);
    System.out.println(CASAUtil.unescape(s,"\""));
    System.out.println(CASAUtil.unescape(CASAUtil.unescape(s,"\""),"\""));
    try {
      Status s2 = (Status) CASAUtil.unserialize(s, null);
      System.out.println(s2.toString());
    }
    catch (ParseException ex) {
    	CASAUtil.log("error", "Status.main()", ex, true);
    }
  }
}
