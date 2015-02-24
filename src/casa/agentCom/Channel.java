package casa.agentCom;

import casa.AbstractProcess;
import casa.MLMessage;
import casa.Status;

import java.nio.channels.ClosedChannelException;

/**
 * Abstract Channel to be associated with a URLDescriptor.  Concrete channels
 * are objects that might use TCP/IP or direct calls.
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
public interface Channel {
	/**
	 * Send a message to the to the agent specified in the RECEIVER field of the <em>msg</em>.
	 * The message is NOT checked for validity or anything else other than the URL
	 * in the RECEIVER field.  
	 * @param sender The sender agent, primarily used to write log information to.
	 * @param msg The message to send.
	 * @throws ClosedChannelException if the channel has been closed at the other end or is otherwise disrupted.
	 */
	public Status sendMessage(AbstractProcess sender, MLMessage msg) throws ClosedChannelException;
}
