package casa.ui;

import casa.ML;
import casa.MLMessage;
import casa.ObserverNotification;
import casa.TransientAgent;
import casa.agentCom.URLDescriptor;
import casa.exceptions.URLDescriptorException;

/**
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The  Knowledge Science Group makes no representations about the suitability of  this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public class ChatTextInterface extends TextInterface {

  public ChatTextInterface(TransientAgent a, String[] args, String user) {
		this(a, args);
	}

	public ChatTextInterface(TransientAgent a, String[] args) {
    super(a, args, true);
  }

  /**
   * <p>Description: adapted from the previous handleEvent(). The structure is credited 
   * to the author of the previous version.  HandleEvent() now uses {@link ObserverNotification}</p>
   * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
   * @version 0.9
   */
	@Override
	//protected void handleEvent(int event, Object obj)
	protected void handleEvent(String event, Object obj) {
		super.handleEvent(event, obj);
		MLMessage msg;
		//TODO Delete when ready
		//if (event == ObservableEvent.STATE_MESSAGE_RECEIVED && (obj instanceof MLMessage) 
		//	&& (msg=(MLMessage)obj).getParameter("act").equals("chat.message")) {
		if (event == null) return; //No NullPointerExceptions
		if ((event.equals(ML.EVENT_MESSAGE_RECEIVED)) && 
				(obj instanceof MLMessage) &&
				(msg=(MLMessage)obj).getParameter("act").equals("chat.message")){
			String from = msg.getParameter("from");
			try {
				URLDescriptor url = URLDescriptor.make(from);
				from = url.getFile();
			}
			catch (URLDescriptorException ex) {
			}
			println(from + ": " + msg.getParameter("content"));
		}
	}
}
