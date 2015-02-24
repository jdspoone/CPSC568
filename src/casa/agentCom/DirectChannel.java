package casa.agentCom;

import casa.AbstractProcess;
import casa.ML;
import casa.MLMessage;
import casa.Status;
import casa.event.MessageEvent;

/**
 * A "Channel" that writes directly to the "remote" agent's event queue.  This
 * only works for agents in the same process.
* <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. 
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  
 * The  Knowledge Science Group makes no representations about the suitability
 * of  this software for any purpose.  It is provided "as is" without express
 * or implied warranty.</p>
 * <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
public class DirectChannel implements Channel {
	
	AbstractProcess remoteAgent;
	
	public DirectChannel(AbstractProcess remoteAgent) {
		assert remoteAgent!=null;
		this.remoteAgent = remoteAgent;
	}

	/* (non-Javadoc)
	 * @see casa.channels.Channel#sendMessage(casa.MLMessage)
	 */
	@Override
	public Status sendMessage(AbstractProcess sender, MLMessage msg) {
    if (sender!=null && sender.isLoggingTag("msg")) {
    	sender.println("msg", "Sending message (local call):\n" + msg.toString(true)); //debug
    }
		remoteAgent.queueEvent(new MessageEvent(ML.EVENT_MESSAGE_RECEIVED, remoteAgent, msg));
		return new Status(0);
	}
	
	@Override
	public String toString() {
		return "direct->"+remoteAgent.getAgentName()+(remoteAgent.isAlive()?"":"(dead agent)");
	}
}
