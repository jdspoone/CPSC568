package casa;

import casa.exceptions.KQMLMessageFormatException;

/**
 * This class is responsible for parsing strings into KQML messages in the form of KQMLmessage objects. <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 * @see  KQMLmessage
 */

public class KQMLParser implements KQML
{
  /**
	 */
  private TokenParser parser = new TokenParser();

  /**
   * Constructs a new parser that is ready parse new strings. To set a
   * string to parse use the reset method.
   */
  public KQMLParser() {
    this( "" );
  }

  /**
   * Constructs a new parser with the string specified. This string will be parsed
   * when the getKQMLmessage method is invoked.
   *
   * @param string string to parse as a KQML message
   */
  public KQMLParser( String string ) {
    reset( string );
  }

  /**
   * Resets the parser and sets the about-to-be-parsed string to the one specified.
   *
   * @param string string to parse as a KQML message
   */
  public void reset( String string ) {
    parser.reset( string );
  }

  /**
   * Parses the previously specified string
   *
   * @return  fully parsed KQML message if the string specified could be parsed
   *          as a KQML message.
   * @throws  MLMessageFormatException  when the string does not appear to be a
   *                                    KQML message and cannot be parsed as such.
   */
  public KQMLMessage getKQMLMessage() throws KQMLMessageFormatException {
    KQMLMessage m = new KQMLMessage();
    String p;

    getToken( String.valueOf(OPEN) );
    m.setParameter(ML.PERFORMATIVE, getToken( null ).substring(1) );

    while (!(p = getToken()).equals( String.valueOf(CLOSE) ) )
             m.setParameter( p, getToken( null ) );

    return m;

  }


  /**
   * Parses the previously specified string for the purpose of validation as a
   * KQML message.
   *
   * @throws  MLMessageFormatException  when the string does not appear to be a
   *                                      KQML message and cannot be parsed as such.
   */
  public void parse() throws KQMLMessageFormatException {
    KQMLMessage m = new KQMLMessage();
    String      p;

    getToken( String.valueOf(OPEN) );
    m.setParameter(ML.PERFORMATIVE, getToken( null ).substring(1) );

    while (!(p = getToken()).equals( String.valueOf(CLOSE) ) )
             m.setParameter( p, getTokenAcceptingParenGroups( null ) );
  }


  /**
   * Retrieves the next token from the internal <code>TokenParser</code> object
   * and throws an exception if none exists. This method is used primarily to
   * fetch perspectives and parameters' value after that parameter has been
   * identified. If the value is null, as in no perspective or no value was
   * found, then the string is considered to be mal-formed. <p>
   *
   * @param compare if not <code>null</code> the token retrieved is compared this value, if it
                    does not match an exception is thrown. This can be used to automatically throw
                    an exception when a string is expected.
                    If <code>null</code> an exception is thrown only if the end of message marker 'CLOSE'
                    is found.
   * @return the next token (value of the current parameter)
   * @throws MLMessageFormatException no parameter value was found where there should
   *                                    have been some value. Poorly formatted string?
   * @see KQML#CLOSE
   */
  protected String getToken( String compare ) throws KQMLMessageFormatException {
    String  s = parser.getNextToken();
    return checkToken(s, compare);
  }
  
  protected String getTokenAcceptingParenGroups( String compare ) throws KQMLMessageFormatException {
    String  s = parser.getNextTokenAcceptingParenGroups();
    return checkToken(s, compare);
  }
  
  private String checkToken(String token, String compare) throws KQMLMessageFormatException {
    boolean e = true;

    if (token != null)
        if (compare == null) e =  token.equals( String.valueOf( CLOSE ) );
        else                 e = !token.equals( compare );

    if (e) {
    	String reason;
    	if (token==null) reason = "no token found when expecting '"+compare+"'";
    	else if (compare==null) reason = "unexpected close paranthesis found '"+token+"'";
    	else reason = "expecting '"+compare+"' but found '"+token+"'";
    	KQMLMessageFormatException ex = new KQMLMessageFormatException("Error in the formatting of the KQMLmessage (reason: "+reason+"): "+parser.source);
    	throw ex;
    }
    else   return token; 	
  }
  
  /**
   * Retrieves the next token from the internal <code>TokenParser</code> object
   * and throws an exception if none exists. This is used to fetch the next
   * parameter from the target string. If the token is null, as in no parameter
   * was found, then the string is considered to be mal-formed since this method
   * should only be called when other parameters are expected in the string. <p>
   *
   * @return the next token (value of the current parameter)
   * @throws MLMessageFormatException no parameter value was found where there should
   *                                    have been some value. Poorly formatted string?
   */
  protected String getToken() throws KQMLMessageFormatException {
    String s = parser.getNextToken();
    if (s != null) return s;
    else           throw new KQMLMessageFormatException();
  }

}
