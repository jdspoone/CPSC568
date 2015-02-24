package casa.testAgents;

import casa.ML;
import casa.MLMessage;
import casa.TokenParser;
import casa.agentCom.URLDescriptor;
import casa.exceptions.URLDescriptorException;

import java.util.Iterator;
import java.util.Vector;

/**
 * <code>ChatMessage</code> is a simple class that is used to store one chat message that has been recieved by a <code>ChatAgent</code>.  It is usually used in a <code>Vector</code> containing several messages, and the <code>toString()</code> function is designed to be used in <code>JList</code>s and other visual interfaces. <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @version 0.9
 * @see ChatAgent
 * @author  Jason Heard,<a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */

public class ChatMessage {
  /**
	 * Stores the actual text of the chat message.
	 */
  private String message;

  /**
	 * Stores the <code>URLDescriptor</code> of the sender of the message.
	 */
  private URLDescriptor sender;

  /**
	 * Determines whether this message was a "directed" message.  It is <code>true</code> if the message was directed to one or more specified agents; <code>false</code> otherwise.
	 */
  private boolean directed;

  /**
	 * Determines whether this message was a "whisper".  It is <code>true</code> if the message was sent exclusivly to one or more specified agents; <code>false</code> otherwise.  This can only be <code>true</code> if the message is "directed".
	 */
  private boolean whisper;

  /**
	 * Stores a <code>Vector</code> containing the recipients (<code>URLDescriptor</code>s) of the message.  This is <code>null</code> if the message is not "directed".
	 */
  private Vector<URLDescriptor> recipients;

  /**
	 * Stores the <code>URLDescriptor</code> of the cooperation domain that this message was sent through.
	 */
  private URLDescriptor cd;

  /**
   * Creates a new <code>ChatMessage</code> that is not a "directed" message or
   * a "whisper" with the given message text, sender, and cooperation domain.
   *
   * @param message The actual text of the chat message.
   * @param sender The <code>URLDescriptor</code> of the sender of the message.
   * @param cd The <code>URLDescriptor</code> of the cooperation domain that
   * this message was sent through.
   */
  public ChatMessage (String message, URLDescriptor sender, URLDescriptor cd) {
    this.message = message;
    this.sender = sender;
    this.directed = false;
    this.whisper = false;
    this.recipients = null;
    this.cd = cd;
  }

  /**
   * Creates a new <code>ChatMessage</code> from the given
   * <code>MLMessage</code> and the given cooperation domain.  The details of
   * the message are arll retrieved from the <code>MLMessage</code>.
   *
   * @param message An <code>MLMessage</code> containing both the actual text
   * of the message, the sender of the message, whether the message is a
   * "directed" message or a "whisper", and who the message was to if it was
   * "directed".
   * @param cd The <code>URLDescriptor</code> of the cooperation domain that
   * this message was sent through.
   */
  public ChatMessage (MLMessage message, URLDescriptor cd) {
    this.message = message.getParameter (ML.CONTENT);

    try {
      this.sender = message.getFrom ();
    } catch (URLDescriptorException ex) {
      this.sender = null;
    }

    String toString = message.getParameter (ML.RECIPIENTS);

    if (toString == null || toString.equals ("") || toString.equals ("*")) {
      this.directed = false;
      this.whisper = false;
      this.recipients = null;
    } else {
      this.directed = true;
      this.recipients = new Vector<URLDescriptor> ();

      TokenParser parser = new TokenParser(toString);
      URLDescriptor tempRecipient = null;

      if (parser.getNextToken ().equals ("+")) {
        // "Directed Speach", must remove the "+", so the URLs can be parsed.
        this.whisper = false;
      } else {
        // "Whisper"
        this.whisper = true;
        // Put back the first member that the message is to.
        parser.putback ();
      }

      try {
        tempRecipient = URLDescriptor.fromString(parser);
      }
      catch (URLDescriptorException ex1) {
      }

      while (tempRecipient != null) {
        this.recipients.add(tempRecipient);

        tempRecipient = null;
        try {
          tempRecipient = URLDescriptor.fromString(parser);
        }
        catch (URLDescriptorException ex1) {
        }
      }
    }

    this.cd = cd;
  }

  /**
	 * Retrieves the actual text of the chat message.
	 * @return  The actual text of the chat message.
	 */
  public String getMessage () {
    return message;
  }

  /**
	 * Sets the actual text of the chat message.
	 * @param message  The actual text of the chat message.
	 */
  public void setMessage (String message) {
    this.message = message;
  }

  /**
	 * Retrieves the sender of the chat message as a <code>URLDescriptor</code>.
	 * @return  The sender of the chat message as a <code>URLDescriptor</code>.
	 */
  public URLDescriptor getSender () {
    return sender;
  }

  /**
	 * Sets the sender of the chat message as a <code>URLDescriptor</code>.
	 * @param sender  The sender of the chat message as a <code>URLDescriptor</code>.
	 */
  public void setSender (URLDescriptor sender) {
    this.sender = sender;
  }

  /**
	 * Retrieves whether the message is "directed".  Returns <code>true</code> if the message was directed to one or more specified agents; <code>false</code> otherwise.
	 * @return  <code>true</code> if the message was directed to one or more  specified agents; <code>false</code> otherwise.
	 */
  public boolean isDirected () {
    return directed;
  }

  /**
	 * Retrieves whether the message is a "whisper".  Returns <code>true</code> if the message was sent exclusivly to one or more specified agents; <code>false</code> otherwise.
	 * @return  <code>true</code> if the message was sent exclusivly to one or  more specified agents; <code>false</code> otherwise.
	 */
  public boolean isWhisper () {
    return whisper;
  }

  /**
   * Sets the message so that it is "directed" with the specified recipients.
   *
   * @param recipients A <code>Vector</code> containing the recipients
   * (<code>URLDescriptor</code>s) of the message.
   */
  public void setDirected (Vector<URLDescriptor> recipients) {
    this.directed = true;
    this.recipients = recipients;
  }

  /**
   * Sets the message so that it is not "directed".  In addition,
   * the message is set so that is is not a "whisper" and recipients is set to
   * <code>null</code>.
   */
  public void unsetDirected () {
    this.directed = false;
    this.whisper = false;
    this.recipients = null;
  }

  /**
   * Sets the message so that it is a "whisper" with the specified exclusive
   * recipients.  In addition, the message is set so that it is "directed".
   *
   * @param recipients A <code>Vector</code> containing the exclusive
   * recipients (<code>URLDescriptor</code>s) of the message.
   */
  public void setWhisper (Vector<URLDescriptor> recipients) {
    this.directed = true;
    this.whisper = true;
    this.recipients = recipients;
  }

  /**
   * Sets the message so that it is not a "whisper".
   */
  public void unsetWhisper () {
    this.whisper = false;
  }

  /**
	 * Retrieves the list of recipients of the message, or <code>null</code> if the message is not "directed".
	 * @return  A <code>Vector</code> containing the recipients  (<code>URLDescriptor</code>s) of the message, or <code>null</code> if  the message is not "directed".
	 */
  public Vector getRecipients () {
    return recipients;
  }

  /**
	 * Sets the list of recipients of the message.  If the message is not "directed", nothing will be changed.
	 * @param recipients  A <code>Vector</code> containing the recipients  (<code>URLDescriptor</code>s) of the message.
	 */
  public void setRecipients (Vector<URLDescriptor> recipients) {
    if (this.directed) {
      this.recipients = recipients;
    }
  }

  /**
   * Retrieves the cooperation domain of the chat message.
   *
   * @return The cooperation domain of the chat message.
   */
  public URLDescriptor getCD () {
    return cd;
  }

  /**
   * Sets the cooperation domain of the chat message.
   *
   * @param cd The cooperation domain of the chat message.
   */
  public void setCD (URLDescriptor cd) {
    this.cd = cd;
  }

  /**
   * Returns the chat message as a string suitable to use in a user interface.
   * This string can be formatted in several ways, depending on whether the
   * sender and recipient or recipients are known or not, whether the message
   * is directed or a whisper, and how many recipients there are:
   * <li>if the message is not directed and the sender is known:
   * "<em>sender</em>:&nbsp;<em>message</em>",</li>
   * <li>if the message is not directed and the sender is not known:
   * "&lt;unknown&nbsp;sender&gt;:&nbsp;<em>message</em>",</li>
   * <li>if the message is directed, the sender is known, and the recipient or
   * recipients are unknown:
   * "<em>sender</em>->&lt;unknown&gt;:&nbsp;<em>message</em>",</li>
   * <li>if the message is directed, the sender is not known, and the recipient
   * or recipients are unknown:
   * "&lt;unknown&nbsp;sender&gt;->&lt;unknown&gt;:&nbsp;<em>message</em>",</li>
   * <li>if the message is directed, the sender is known, and there is one known
   * recipient:
   * "<em>sender</em>-><em>recipient</em>:&nbsp;<em>message</em>",</li>
   * <li>if the message is directed, the sender is not known, and there is one
   * known recipient:
   * "&lt;unknown&nbsp;sender&gt;-><em>recipient</em>:&nbsp;<em>message</em>",</li>
   * <li>if the message is directed, the sender is known, and there are more than
   * one known recipient:
   * "<em>sender</em>->(<em>recipient</em>,&nbsp;<em>recipient</em>,&nbsp;...):&nbsp;<em>message</em>",</li>
   * <li>if the message is directed, the sender is not known, and there are more
   * than one known recipient:
   * "&lt;unknown&nbsp;sender&gt;->(<em>recipient</em>,&nbsp;<em>recipient</em>,&nbsp;...):&nbsp;<em>message</em>",</li>
   * <li>if the message is a whisper, the sender is known, and the recipient or
   * recipients are unknown:
   * "<em>sender</em>->&lt;unknown&gt;:&nbsp;(*&nbsp;<em>message</em>&nbsp;*)",</li>
   * <li>if the message is a whisper, the sender is not known, and the recipient
   * or recipients are unknown:
   * "&lt;unknown&nbsp;sender&gt;->&lt;unknown&gt;:&nbsp;(*&nbsp;<em>message</em>&nbsp;*)",</li>
   * <li>if the message is a whisper, the sender is known, and there is one known
   * recipient:
   * "<em>sender</em>-><em>recipient</em>:&nbsp;(*&nbsp;<em>message</em>&nbsp;*)",</li>
   * <li>if the message is a whisper, the sender is not known, and there is one
   * known recipient:
   * "&lt;unknown&nbsp;sender&gt;-><em>recipient</em>:&nbsp;(*&nbsp;<em>message</em>&nbsp;*)",</li>
   * <li>if the message is a whisper, the sender is known, and there are more than
   * one known recipient:
   * "<em>sender</em>->(<em>recipient</em>,&nbsp;<em>recipient</em>,&nbsp;...):&nbsp;(*&nbsp;<em>message</em>&nbsp;*)",</li>
   * <li>if the message is a whisper, the sender is not known, and there are more
   * than one known recipient:
   * "&lt;unknown&nbsp;sender&gt;->(<em>recipient</em>,&nbsp;<em>recipient</em>,&nbsp;...):&nbsp;(*&nbsp;<em>message</em>&nbsp;*)",</li>
   *
   * @return The chat message as a string suitable to use in a user interface.
   */
  @Override
public String toString () {
    StringBuffer output = new StringBuffer ("<HTML>");
    if (sender == null) {
      output.append ("<unknown sender>");
    }
    else {
      output.append (sender.getFile ());
    }

    if (directed) {
      output.append ("->");

      if (recipients == null || recipients.size () == 0) {
        output.append ("<unknown>");
      } else {
        URLDescriptor recipient = null;
        if (recipients.size () == 1) {
            recipient = recipients.get (0);

            output.append (recipient.getFile ());
        } else {
          output.append('(');

          for (Iterator i = recipients.iterator (); i.hasNext (); ) {
            recipient = (URLDescriptor) i.next ();

            output.append (recipient.getFile ());

            if (i.hasNext ()) {
              output.append (", ");
            }
          }
          output.append(')');
        }
      }
    }

    output.append (": ");
    if (whisper) {
      output.append ("<FONT COLOR=\"#CCCCCC\">");
    }
    output.append (message);
    if (whisper) {
      output.append ("</FONT>");
    }

    output.append ("</HTML>");
    
    return output.toString ();
  }
}