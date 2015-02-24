package casa;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import casa.exceptions.MLMessageFormatException;
import casa.util.CASAUtil;

/**
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>, Jason Heard
 * @version 0.9
 */

public class StatusMLMessageList extends Status {
  /**
	 */
  private List<MLMessage> messages;

  /**
   *
   */
  public StatusMLMessageList () {
    messages = null;
  }

  /**
   *
   * @param status
   * @param messages
   */
  public StatusMLMessageList (int status, List<MLMessage> messages) {
    super (status);
    this.messages = messages;
  }

  /**
   *
   * @param status
   * @param explanation
   * @param messages
   */
  public StatusMLMessageList (int status, String explanation, List<MLMessage> messages) {
    super (status, explanation);
    this.messages = messages;
  }

  /**
	 * @param  messages
	 */
  public void setMessages (List<MLMessage> messages) {
    this.messages = messages;
  }

  /**
	 * @return
	 */
  public List<MLMessage> getMessages () {
    return messages;
  }

  /**
   *
   * @return
   */
  @Override
public String toString_extension () {
    if (messages == null) {
      return " ";
    } else {
      StringBuffer buffer = new StringBuffer ();

      for (Iterator i = messages.iterator (); i.hasNext (); ) {
        buffer.append (ML.BLANK);
        buffer.append (CASAUtil.toQuotedString (i.next ().toString ()));
      }

      return buffer.toString ();
    }
  }

  /**
   *
   * @param parser
   * @throws Exception
   */
  @Override
public void fromString_extension (TokenParser parser) throws Exception {
	List<MLMessage> tempVector = new ArrayList<MLMessage> ();
    String tempString;
    MLMessage tempMessage;

    for (tempString = parser.getNextToken ();
         tempString != null && !tempString.equals (")");
         tempString = parser.getNextToken ()) {

      try {
        tempMessage = MLMessage.getNewMLMessageType (MLMessage.getMarkupLanguage ());
        tempMessage.fromStringLocal (tempString);
        tempVector.add (tempMessage);
      } catch (MLMessageFormatException ex1) {
        try {
          tempMessage = MLMessage.getNewMLMessage();
          tempMessage.fromStringLocal (tempString);
          tempVector.add (tempMessage);
        } catch (MLMessageFormatException ex2) {
          throw new ParseException ("Cannot parse current MLMessage", 0);
        }
      }
    }

    if (tempString.equals (")")) {
      parser.putback ();
    }

    setMessages (tempVector);
  }
}