package casa.interfaces;

import casa.Status;
import casa.StatusMLMessageList;
import casa.agentCom.URLDescriptor;

import java.util.Vector;

/**
 * <code>CooperationDomainInterface</code> is an extension of
 * <code>AgentInterface</code> that acts as a meeting room for other agents.
 * It adds methods for all the incoming requests that a cooperation domain must
 * handle as well as constants used in communication with a cooperation domain.
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
 * @version 0.9
 */

public interface CooperationDomainInterface extends AgentInterface {

  /**
   * Adds the specified agent to the list of current members and notifies any
   * agents that are observing membership that a new member has joined the
   * cooperation domain.
   *
   * @param joiner The agent who is joining the cooperation domain.
   * @return The <code>Status</code> of the join:
   * <li>0 if it was successful with no warnings or errors, or</li>
   * <li>1 if the agent was already a member and its URL is updated in the
   * member list.</li>
   */
  public Status join (URLDescriptor joiner);

  /**
   * Removes the specified agent from the list of current members and notifies
   * any agents that are observing membership that a member has withdrawn from
   * the cooperation domain.
   *
   * @param member The agent (presumably a member) who is withdrawing from the
   * cooperation domain.
   * @return The <code>Status</code> of the withdrawal:
   * <li>0 if it was successful with no warnings or errors, or</li>
   * <li>1 if the agent was not already a member.</li>
   */
  public Status withdraw (URLDescriptor member);

  /**
   * Retrieves a membership list for the cooperation domain.  Returns a
   * list of <code>URLDescriptor</code>s (encapsulated in a
   * <code>StatusURLDescriptorList</code>) that are members of the cooperation
   * domain.
   *
   * @return A <code>Vector</code> of <code>URLDescriptor</code>s (encapsulated
   * in a <code>StatusURLDescriptorList</code>) that are members of the
   * cooperation domain.  The status will be:
   * <li>0 indicating the operation was successful.</li>
   */
  public Vector getMembers ();

  /**
   * Retrieves the entire history for the cooperation domain.  Returns a
   * list of <code>MLMessage</code>s (encapsulated in a
   * <code>StatusMLMessageList</code>) that have been sent within the
   * cooperation domain.
   *
   * @return A <code>Vector</code> of <code>MLMessage</code>s (encapsulated
   * in a <code>StatusMLMessageList</code>) that have been sent within the
   * cooperation domain.  The status will be:
   * <li>0 if the operation was successful, or</li>
   * <li>1 if there were errors loading the history.</li>
   */
  public StatusMLMessageList getEntireHistory ();

  /**
   * Retrieves the history for a given agent in the cooperation domain.
   * Returns a list of <code>MLMessage</code>s (encapsulated in a
   * <code>StatusMLMessageList</code>) that have been sent within the
   * cooperation domain to the given agent or to all agents.
   *
   * @param member The member for whom we are retrieving the history.
   * @return A <code>Vector</code> of <code>MLMessage</code>s (encapsulated
   * in a <code>StatusMLMessageList</code>) that have been sent within the
   * cooperation domain to the given agent or to all agents.  The status will
   * be:
   * <li>0 if the operation was successful, or</li>
   * <li>1 if there were errors loading the history, or parsing the
   * messages.</li>
   */
  public StatusMLMessageList getHistory (URLDescriptor member);
}