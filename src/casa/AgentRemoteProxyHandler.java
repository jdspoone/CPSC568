package casa;

import casa.agentCom.URLDescriptor;
import casa.util.CASAUtil;
import casa.util.Trace;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Observer;

/**
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public class AgentRemoteProxyHandler implements InvocationHandler {
  /**
	 */
  private URLDescriptor target;
  /**
	 */
  private TransientAgent agent;

  public AgentRemoteProxyHandler(URLDescriptor target) throws Exception {
    this.target = target;
    agent = new AgentRemoteProxy(target.getFile());
  }

  @Override
public void finalize() {
    if (agent!=null) agent.exit();
  }

  public Object invoke(Object  proxy, Method method, Object[] args) {
    //DEBUG.DISPLAY("sending: "+contents);

    //"shortcut" or "cached" methods: don't actually call the remote
    if (method.getName().equals("getURL"))  return target;
    if (method.getName().equals("getName")) return target.getFile();

    //sneaky modified methods: change the method before passing it to the remote
    if (method.getName().equals("addObserver") &&
        args.length==1 &&
        Observer.class.isAssignableFrom(args[0].getClass())) {
      agent.addObserver((Observer)args[0]);
      args[0] = agent.getURL();
      //note: explictly no return here: we want to send the altered message on the the remote agent
    }

    //construct the message
    MLMessage msg = agent.getNewMessage("request",ML.METHOD_CALL,target);
    String contents = method.getName() + " ( " + CASAUtil.serialize(args) + " )";
    msg.setParameter("content", contents);

    //send message
    MLMessage reply = null;
    try {
      try {
        //reply = agent.sendMessage_sync (msg);
        StatusObject<MLMessage> stat = agent.sendRequestAndWait(msg, 3000);
        reply = stat.getObject();
      } catch (Exception ex2) {
      }
    } catch (Exception ex) {
    	Trace.log("error", "AgentRemoteProxyHandler.invoke: Failed to send message:\n"+msg.toString(true), ex);
      return null;
    }
    if (reply!=null) {
      if (reply.getParameter("performative").equals("reply")) {
        try {
          String rcontent = reply.getParameter ("content");
          Object o = CASAUtil.unserialize(rcontent, reply.getParameter(ML.LANGUAGE));
          return o;
        } catch (Exception ex1) {
        	Trace.log("error", "AgentRemoteProxyHandler.invoke: Failed to translate object in content field of message:\n"+msg.toString(true), ex1);
        }
      }
      else {
        Trace.log("error", "AgentRemoteProxyHandler.invoke: received unexpected message:\n" + reply.toString (true));
      }
    }
    return null;
  }
}
