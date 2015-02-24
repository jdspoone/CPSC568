package casa;

import java.util.Enumeration;

import casa.exceptions.MLMessageFormatException;


/**
 * Title:        CASA Description:  Interface for all messages that use a markup language. <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author   <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 * @author   <a href="laf@cpsc.ucalgary.ca">Chad La Fournie</a>
 */
public interface MLMessageInterface {

  public void reset();

  public void setParameter( String parameter, String value );

  public String getParameter( String key );

  /**
	 * Retrieves the current performative for this message.
	 * @return   The performative of this message.
	 * @deprecated   use getParameter(ML.PERFORMATIVE) instead
	 */
  @Deprecated
  public void setPerformative( String performative );

  /**
	 * Sets the performative of this message. Since a message can have only one performative this method over-writes the performative if it had previously been set.
	 * @param performative    perfomative to assign to this message
	 * @deprecated   use SetParameter(ML.PERFORMATIVE,String) instead
	 */
  @Deprecated
  public String getPerformative();

  public Enumeration parameters();

  public String toString();

  public void fromString( String source ) throws MLMessageFormatException;


  // accessor methods - necessary because of the slight difference between these parameters for XML/KQML.

  public String getSENDER();

  public String getRECEIVER();

  public String getFROM();

  public String getTO();

  public String getREPLY_WITH();

  public String getIN_REPLY_TO();

  public String getLANGUAGE();

  public String getCONTENT();

}