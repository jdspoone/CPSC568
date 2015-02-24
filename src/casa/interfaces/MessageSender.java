package casa.interfaces;

import casa.MLMessage;
import casa.Status;


/**
 * The interface for objects which are able to send IPMessage messages.
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
 */
public interface MessageSender
{
  /**
   * This method will be envoked when a message is to be sent, <code>message</code>
   * contains destination information about the message.
   *
   * @param message the message to send
   * @see IPMessage
   * @return The <em>Status</em> describing the status of the opporation:
   * <li>0 if the message was sent without errors,</li>
   * <li>-3 if there was an exception thrown during the message sending
   * process, or</li>
   * <li>negative if the send failed for some other reason.</li>
   */
  public Status sendMessage(MLMessage message);
}

