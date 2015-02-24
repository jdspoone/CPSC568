package casa.ui;

import casa.AgentRemoteProxyHandler;
import casa.ML;
import casa.TransientAgent;
import casa.agentCom.URLDescriptor;
import casa.interfaces.TransientAgentInterface;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Observable;

import javax.swing.JInternalFrame;

/**
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public class AgentRemoteProxyWindow extends TransientAgentInternalFrame {
  /**
	 */
  final AgentRemoteProxyWindow _this;

  public AgentRemoteProxyWindow (URLDescriptor remoteAgent) throws Exception {
    super (
        (TransientAgent) Proxy.getProxyClass
        (TransientAgentInterface.class.getClassLoader (), new Class[] { TransientAgent.class})
        .getConstructor (new Class[] {InvocationHandler.class})
        .newInstance (new Object[] {new AgentRemoteProxyHandler (remoteAgent)})
        , remoteAgent.getPath () + "$rproxy", new JInternalFrame ()
        );
    _this = this; //used by the AgentRemoteProxyUpdate nested class
  }

  @Override
	public void updateEventHandler(Observable observable, String event, Object argObject) {
    	if (ML.EVENT_EXITING.equals(event))
    		_this.closeInternalFrame ();
    	else
    		super.updateEventHandler(observable, event, argObject);
    }
  }