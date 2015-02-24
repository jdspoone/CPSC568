package casa;

import casa.abcl.ParamsMap;
import casa.agentCom.URLDescriptor;
import casa.event.Event;
import casa.event.MessageEvent;
import casa.exceptions.IPSocketException;
import casa.interfaces.TransientAgentInterface;
import casa.ui.AgentUI;
import casa.util.Trace;

import java.io.IOException;

import org.armedbear.lisp.SimpleString;

/**
 * <code>AgentProxy</code> is a simple subclass of <code>TransientAgent</code> with the sole purpose of acting as a proxy between an agent and all other agents. All messages that it recieves are first filtered to determine if the message is to the proxy (based on the <code>ML.RECEIVER</code> parameter). If so, the message is processed by the super class as a regular agent. Otherwise, the message is passed along to the proxied agent by <code>forwardMessage()</code>. <p> Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation. The Knowledge Science Group makes no representations about the suitability of this software for any purpose. It is provided "as is" without express or implied warranty. </p>
 * @see TransientAgent
 * @author  Jason Heard, <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer  </a>
 * @version 0.9
 */

public class AgentProxy extends TransientAgent implements
    TransientAgentInterface {
  /**
   * The port of the agent this proxy is protecting.
   */
  private int agentPort;
  
  private static ParamsMap adjustAgentName(ParamsMap params) throws Exception {
  	String name = (String)params.getJavaObject("NAME",String.class);
  	ParamsMap ret = new ParamsMap(params);
  	ret.put("NAME", name, new SimpleString(name), false);
  	return ret;
  }
  
  /**
   * Creates a new <code>AgentProxy</code> that forwards messages to the given
   * agent and uses the given port.
   *
   * @param agentName The name of the agent the new proxy is protecting.
   * @param proxyPort The port that the new proxy should use for communications.
   * @param agentPort The port that the agent uses for communications.
   * @throws IPSocketException If an Agent attempts to bind to an IPSocket
   *           (port) that doesn't exist or is in use.
   */
  public AgentProxy (ParamsMap params, AgentUI ui) throws Exception {
    super (adjustAgentName(params), ui);
    in ("AgentProxy.AgentProxy");

    this.agentPort = (Integer)params.getJavaObject("AGENTPORT", Integer.class);
    URLDescriptor.make (agentPort);
    out ("AgentProxy.AgentProxy");
  }
  
  /* (non-Javadoc)
   * @see casa.TransientAgent#pendingFinishRun_withdrawFromAllCDs()
   */
  @Override
protected void pendingFinishRun_withdrawFromAllCDs () {
    in ("AgentProxy.pendingFinishRun_withdrawFromAllCDs");
    // AgentProxy shouldn't do withdraws...
    out ("AgentProxy.pendingFinishRun_withdrawFromAllCDs");
  }

  /**
   * Handles the processing and dispatching of all incoming messages. For each
   * incoming message is checked to see if it was sent to the proxy directly. If
   * so, the return value of <code>super.handleMessage()</code> is returned.
   * If the message was not sent to the proxy directly, the message is passed to
   * <code>handleForwardMessage()</code>, which is responsible for forwarding
   * the message to the proxied agent, and its return value is returned.
   *
   * @param event The incoming message to be handled.
   * @return The <code>Status</code> describing how the message was handled:
   *         <li>0 if the message was either handled by the proxy or forwarded
   *         to the protected agent,</li>
   *         <li>-10 if the message was not directed to the proxy and was not
   *         forwarded for any reason.</li>
   *         <li>-121 if the message was directed to the proxy and was a reply
   *         to a request that the proxy didn't send,</li>
   *         <li>positive if the message was directed to the proxy and was not
   *         handled, or</li>
   *         <li>negative if the message was directed to the proxy and failed
   *         and no further processing is necesary.</li>
   */
  @Override
//  public Status handleEvent (Event event) {
  public void handleEvent (Event event) {
    in ("AgentProxy.handleMessage");
    Status ret = new Status (1);

    if (event instanceof MessageEvent) {
    	MLMessage msg = ((MessageEvent)event).getMessage();
      if (msg.getParameter (ML.RECEIVER).indexOf (getURL ().getPath ()) > -1) {
      	super.handleEvent (event);
//        ret = super.handleEvent (event);
  
      } else if (msg.getParameter (ML.RECEIVER).indexOf (getURL ().getPath ()) == -1) {
        ret = handleForwardMessage (msg);
      }
    }

    out ("AgentProxy.handleMessage");
//    return ret;
  }

  /**
   * Handles all messages that should be forwarded to the proxied agent.
   *
   * @param message The incoming message to be forwarded to the proxied agent.
   * @return The <code>Status</code> describing if the message was forwarded:
   *         <li>0 if the message was forwarded to the proxied agent, or</li>
   *         <li>-10 if the message was not forwarded for any reason.</li>
   */
  protected Status handleForwardMessage (MLMessage message) {
    in ("AgentProxy.handleForwardMessage");
    try {
      forwardMessage (message);
    } catch (Exception ex) {
    	Trace.log("error", "Error sending message: " + ex.toString ());
    }
    out ("AgentProxy.handleForwardMessage");
    return new Status (0);
  }

  /**
   * Sends the given message to the protected agent at the port given by
   * <code>agentPort</code>. The protected agent is always assumed to be on
   * the same host as the proxy.
   *
   * @param message The message to forward to the protected agent.
   * @throws IOException If there was an error in the process of sending the
   *           message to the protected agent.
   */
  protected void forwardMessage (MLMessage message) throws Exception {
    in ("AgentProxy.forwardMessage");

    try {
      /*
       * To avoid writing directly to sockets at this level, make use of the
       * overloaded method from AbstractProcess
       */
      sendMessage_primitive (message);
    } catch (Exception ex2) {
      if (isLoggingTag("warning"))
        println ("warning",
                 "AgentProxy.forwardMessage: Unexpected I/O exception when sending message: '"
                 + message.toString () + "': " + ex2.toString ());
      out ("AgentProxy.forwardMessage");
      throw (ex2);
 		}
    out ("AgentProxy.forwardMessage");
  }

  /**
   * Sets the LAC port that this agent should use when it needs to resolve local
   * hosts.
   *
   * @param port The LAC port that this agent should use when it needs to
   *          resolve local hosts.
   */
  public void setLACPort (int port) {
    in ("AgentProxy.setLACPort");
    getURL ().setLACport (port);
    out ("AgentProxy.setLACPort");
  }

}