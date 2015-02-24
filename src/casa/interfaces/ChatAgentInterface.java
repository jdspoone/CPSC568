package casa.interfaces;

import casa.MLMessage;
import casa.Status;
import casa.agentCom.URLDescriptor;
import casa.testAgents.ChatMessage;

import java.util.List;
import java.util.Vector;

/**
 * <code>ChatAgentInterface</code> is an extension of
 * <code>AgentInterface</code> designed to be used with a GUI as a chat or
 * message system.  It adds methods to send chat messages, store chat messages
 * that are received, and retrieve the history that has been stored.
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
 * @author Jason Heard
 */

public interface ChatAgentInterface extends AgentInterface {
  /**
   * Sets whether this <code>ChatAgent</code> should obtain the history of any
   * cooperation domain it joins.
   *
   * @param persistent Whether this <code>Agent</code>'s data should be
   * persistent between instances.
   */
  public void setObtainHistory (boolean obtainHistory);

  /**
   * Returns whether this <code>ChatAgent</code> will obtain the history of any
   * cooperation domain it joins.
   *
   * @return <code>true</code> if this <code>ChatAgent</code> will obtain the
   * history of any cooperation domain it joins; <code>false</code> otherwise.
   */
  public boolean willObtainHistory ();

  /**
   * Retrieves the message history for the given cooperation domain.
   *
   * @param cd The cooperation domain for which we are retrieving the history.
   * @return A <code>Vector</code> of <code>ChatMessage</code>s that have been
   * received from the given cooperation domain.
   */
  public Vector<ChatMessage> getHistory (URLDescriptor cd);

  //public Vector getNonMemberHistory ();

  /**
   * Performs operations necessary when a chat message has been received.  The
   * message is stored in the history corresponding to the cooperation domain
   * that sent the message.  The observers are notified with a
   * ObservableState.STATE_CHAT_MESSAGE_RECIEVED message.
   *
   * @param cd The cooperation domain that the message was sent through.
   * @param message The chat message that was received.
   */
  public void chatMessage (URLDescriptor cd, MLMessage message);

  /**
   * Sends the given chat message through the given cooperation domain to the
   * given agent.
   *
   * @param cd The cooperation domain that will forward the message.
   * @param to The agent that will receive the message.
   * @param message The message to send to the given agent.
   * @return The <code>Status</code> of the message sending operation, 0 for
   * success; negative otherwise.
   * @throws Exception If an exception is thrown while sending the message.
   */
  public Status doSendChatMessage (URLDescriptor cd, URLDescriptor to,
                                   boolean whisper, String message);

  /**
   * Sends the given chat message through the given cooperation domain to the
   * given agents.
   *
   * @param cd The cooperation domain that will forward the message.
   * @param to A <code>Vector</code> containing the <code>URLDescriptor</code>s
   * of the agents that will receive the message.
   * @param message The message to send to the given agents.
   * @return The <code>Status</code> of the message sending operation, 0 for
   * success; negative otherwise.
   * @throws Exception If an exception is thrown while sending the message.
   */
  public Status doSendChatMessage (URLDescriptor cd, List<URLDescriptor> to, boolean whisper, String message);

  /**
   * Sends the given chat message through the given cooperation domain to all
   * current members of that cooperation domain.
   *
   * @param cd The cooperation domain that will forward the message.
   * @param message The message to send to the given agent.
   * @return The <code>Status</code> of the message sending operation, 0 for
   * success; negative otherwise.
   * @throws Exception If an exception is thrown while sending the message.
   */
  public Status doSendChatMessage (URLDescriptor cd, String message);
}
