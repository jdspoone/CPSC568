package casa;

import casa.exceptions.IllegalOperationException;
import casa.exceptions.KQMLMessageFormatException;
import casa.exceptions.MLMessageFormatException;
import casa.util.CASAUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
public class KQMLMessage  extends     MLMessage
                          implements  KQML
{
  // refers to this message's performative
  // private String    performative; //now stored in the parameters Hashtable
  // holds all of the extra parameters this message contains
  // private Hashtable parameters; //moved up to MLMessage

  /**
	 */
  private KQMLParser parser;

  /**
   * Constructs an empty KQML message, it has no performative and no parameters.
   */
  public KQMLMessage() {
    parser = null;
  }

  public KQMLMessage(String... list) {
    super(list);
    parser = null;
  }

  /**
   * Resets the message. Clears the performative and removes all parameters
   * that may have previously been defined.
   */
  @Override
	public void reset() {
    super.reset();
    //performative = "";
    //parameters.clear();
  }

  /**
   * Sets a parameter in the message.
   * Override of the parent (MLMessage) method.  This version stips any preceding
   * ":" from the parameter key before storing it.<br>
   * If the parameter has not been defined it
   * is added to the parameter list with the value specified. If the parameter
   * has been previously defined it is updated to reflect the value specified.
   *
   * @param parameter name of the parameter to set/add
   * @param value     string value to associate with the parameter
   */
  @Override
	public void setParameter( String parameter,
                            String value ) {
    super.setParameter((parameter.charAt(0)==':')?parameter.substring(1):parameter, value );
  }


  /**
   * Parses the string input using KQML standards and initializes the all KQMLMessage fields.
   *
   * @param source message used to initialize parameters in XMLMessage.
   *
   * @throws  KQMLMessageFormatException  when the string does not appear to be a
   *                                      KQML message and cannot be parsed as such.
   *
   * @throws MLMessageFormatException when This exception is thrown when an
   *         MLMessage is too poorly formatted to be parsed.
   */
  @Override
	public void fromStringLocal(String source) throws MLMessageFormatException {
//    parser = new KQMLParser(source);
//
//    String p;
//    parser.getToken(String.valueOf(OPEN));
//    setParameter(ML.PERFORMATIVE, parser.getToken(null).substring(0/*1*/));
//
//    while (! (p = parser.getToken()).equals(String.valueOf(CLOSE))) {
//      setParameter(p, parser.getTokenAcceptingParenGroups(null));
//    }
    // find the opening PAREN
    int forward = CASAUtil.scanFor(source, 0, "(");
    int back = forward;
//    back = forward+1; 
//    forward = CASAUtil.scanFor(source, back, " \n\r\f\t"); // scan for white space
    String key = ML.PERFORMATIVE;
    
    while (true) {
    	
    	// find a ":"
    	do { //scan for a ":" with white space in front of it
      	back = forward + 1;
      	forward = CASAUtil.scanFor(source, back, " \n\r\f\t)"); // scan for white space or ")"
    		if (forward<0)
    			throw new MLMessageFormatException("Expected ':' or ')' after \""+source.substring(back)+"\" at position "+back+".");
      	if (source.charAt(forward)!=')')
      		forward = CASAUtil.scanFor(source, forward, ":)");
    		if (forward<0)
    			throw new MLMessageFormatException("Expected ':' or ')' after \""+source.substring(back)+"\" at position "+back+".");
    		if (source.charAt(forward)==')')
    			break;
    	} while (!Character.isWhitespace(source.charAt(forward-1)));
    	
    	String value = source.substring(back,forward).trim();
    	setParameter(key, value);
    	if (source.charAt(forward)==')')
    		break;

    	back = forward + 1;
    	forward = CASAUtil.scanFor(source, back, " \n\r\f\t"); // scan for white space
    	key = source.substring(back, forward).trim();
    	back = forward;
    }

  } // fromString

  /**
   * This method can be used to generate the completely well-formed string equivalent
   * of the message specified. The message will include the performative and
   * any parameters using proper KQML syntax.
   *
   * @returns well formed KQML message corresponding to object's current state
   */
  @Override
	public String toString(boolean prettyPrint) {
    StringBuilder buffer = new StringBuilder();

    buffer.append( OPEN );
    buffer.append( BLANK );
    buffer.append( getParameter(ML.PERFORMATIVE) );
    buffer.append( BLANK );

    for(Enumeration<String> e = getSortedParameterList().elements(); e.hasMoreElements(); ) {
        String key   = e.nextElement(),
               value = getParameter( key );
        
        if (prettyPrint)
        	value = prettyfy(key, value);
        
        if (prettyPrint) buffer.append("\n  ");
        buffer.append( ":"+key   );
        buffer.append( BLANK );
        buffer.append( makeFit(value) ); //TokenParser.makeFit( value ) );
        buffer.append( BLANK );
    }
    if (prettyPrint) buffer.append("\n");
    buffer.append( CLOSE );

    return buffer.toString();
  }
  
  private String makeFit(String s) {
  	if (s.startsWith(":")) 
  		return CASAUtil.toQuotedString(s);
  	return s;
  }


//  /**
//   * Parses the string input.
//   * Throws MLMessageFormatException if for any reason the string input could not be parsed.
//   *
//   * @param source the String to parse as a KQML message.
//   *
//   * @throws  MLMessageFormatException  when the string does not appear to be a
//   *                                    KQML message and cannot be parsed as such.
//   */
//  public void parseMessage( String source ) throws MLMessageFormatException
//  {
//    KQMLParser parser = new KQMLParser( source );
//    parser.parse();
//  	
//
//  }// parseMessage


  // Main
  public static void main(String args[]) {

  	String sources[] = {"( registerAgent :receiver LAC :sender agent_a :reply-with conversationID :content \"String contents\" )"
  			, "( registerAgent :receiver LAC :sender agent_a :reply-with conversationID :content String contents )"
  			, "(registerAgent :receiver LAC :sender agent_a :reply-with conversationID :content ((String) contents) )"
  			, "( registerAgent :receiver LAC :sender agent_a :reply-with conversationID :content ((String) contents))"
  			, "( registerAgent :receiver LAC :sender agent_a :reply-with conversationID :content ((String) contents \"HI\"))"
  			, "( registerAgent :receiver LAC :sender agent_a :reply-with conversationID :content \"((String) contents \\\"HI\\\")\")"
  			, "( registerAgent :receiver LAC :sender agent_a :reply-with conversationID :content \"((String) contents \\\"HI\\\"\")"
  			, "( registerAgent :receiver :9000 :sender agent_a :reply-with conversationID :content \"((String) contents \\\"HI\\\"\")"
  			, "( registerAgent :receiver casa://casa/TransientAgent:9000 :sender agent_a :reply-with conversationID :content \"((String) contents \\\"HI\\\"\")"
  			, "( registerAgent :receiver LAC :sender agent_a :reply-with conversationID :content ((String) contents)" //should fail
  			, "( registerAgent :receiver LAC :sender agent_a :reply-with conversationID :content \"String contents )" //should fail
  	};

  	for (String source: sources) {
  		KQMLMessage m = new KQMLMessage();

  		try {
  			m.fromStringLocal( source );
  		}
  		catch (MLMessageFormatException e) {
  			System.out.println(e.getMessage());
  			e.printStackTrace();
  		}

  		System.out.println( "m.toString = " + m.toString() );
  	}
  /*
    KQMLMessage m = new KQMLMessage();
                m.setPerformative( REGISTER_AGENT );
                m.setParameter   ( SENDER,     "agent_a");
                m.setParameter   ( RECEIVER,   "LAC");
                m.setParameter   ( REPLY_WITH, "conversationID");
                m.setParameter   ( CONTENT,    "Este es un \"ejemplo\" de un string.");

    String      s = m.toString();
    System.out.println( s );

    KQMLparser  p = new KQMLparser( s );
    try {
      m = p.getKQMLMessage();
      System.out.println( m.toString() );

      for (Enumeration e=m.parameters(); e.hasMoreElements();) {
        String key   = (String) e.nextElement(),
               value = (String) m.getParameter( key );

        System.out.println( key + " " + value );
      }
    }
    catch (MLMessageFormatException e) {
      System.out.println( s + "is not a KQML message." );
    }
  */
  }

}
