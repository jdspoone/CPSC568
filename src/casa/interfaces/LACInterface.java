package casa.interfaces;

import casa.RunDescriptor;
import casa.Status;
import casa.StatusURLandFile;
import casa.agentCom.URLDescriptor;

/**
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

public interface LACInterface extends TransientAgentInterface {

  /**
   * Registers an instance of an agent with the LAC.  Note that this means
   * to register a <em>running</em> agent, and the registration does not
   * persist beyond a single execution of the LAC.  All agents should register
   * with a LAC as soon as they start up, and should unregister as upon
   * exit.  Registration with the LAC allows other agents to resolve an
   * unresolved URL.
   * @param newURL the URL of the registering agent
   * @return a StatusURLDescriptor returning the resolved URL of the agent.
   * Status.getStatus() values are:
   * <ul>
   * <li> 0 Successfully registered
   * <li> 1 Agent was already registered
   * </ul>
   * @see unregiserAgentInstance(URLDescriptor)
   */
  public StatusURLandFile registerAgentInstance(URLDescriptor newURL);

  /**
   * Registers the type an agent with the LAC.  Note that this means
   * to register persistently agent, along with a description of how the LAC
   * may run the agent if is called for when the agent is not running.
   * Registration of agent types persists beyond a single execution of the
   * LAC.
   * @param path the pathname to register as the type of the agent.  e.g. The
   * type of a "vanilla" agent is "casa/agent/"; a CP agent may be
   * "casa/cp/agent/".
   * @param r A RunDescriptor object that describes how the LAC should run
   * the agent if it should be needed.
   * @return a StatusURLDescriptor returning the resolved URL of the agent.
   * Status.getStatus() values are:
   * <ul>
   * <li> 0 Successfully registered
   * <li> 1 Agent was already registered
   * </ul>
   * @see unregiserAgentType(String), RunDescriptor
   */
  public Status registerAgentType(String path, RunDescriptor r);

  /**
   * De-registers an instance of an agent with the LAC. All agents should register
   * with a LAC as soon as they start up, and should unregister as upon
   * exit.
   * @param newURL the URL of the registered agent
   * @return a Status returning the success/failure of the operation.
   * Status.getStatus() values are:
   * <ul>
   * <li> 0 Successfully registered
   * <li> 1 Agent was not registered
   * </ul>
   * @see regiserAgentInstance(URLDescriptor)
   */
  public Status unregisterAgentInstance(URLDescriptor newURL);

  /**
  * De-registers an an agent type with the LAC.
  * @param path the path previously registered as the agent's type
  * @return a Status returning the success/failure of the operation.
  * Status.getStatus() values are:
  * <ul>
  * <li> 0 Successfully registered
  * <li> 1 Agent type was not registered
  * </ul>
  * @see regiserAgentType(String)
  */
  public Status unregisterAgentType(String path);

  /**
   * Resolved an unresolved URL.  That is, tries to find a <em>port</em>
   * that the agent is connected to, and adds this information to the URL.
   * If the LAC can't find such an agent, but it has it's agent type
   * registered, then it will attempt to create an instance of the agent
   * and return the new agent's port number in the URL.
   * @param url the unresolved URL, it will be modified to a resolved
   * URL if possible
   * @return a Status returning the success/failure of the operation.
   * Status.getStatus() values are:
   * <ul>
   * <li> 0 Successfully resolved a running agent
   * <li> 1 Agent wasn't running, but created a new instance successfully,
   * and resolved
   * <li> -1 Agent is neither running, nor it's type registered
   * <li> -2 Agent is registered but failed to run the agent (details in
   * Status.getExplanation())
   * </ul>
   */
  public Status resolveURL(URLDescriptor url);

}
