package casa.ui;

import java.util.Iterator;
import java.util.Vector;

import javax.swing.DefaultListModel;

import casa.ML;
import casa.MLMessage;
import casa.testAgents.ChatMessage;

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

public class ChatMessageListModel extends DefaultListModel {
	private static final long	serialVersionUID	= -8712224977709341153L;

	public ChatMessageListModel() {
	}

	/**
	 * Retrieves the object stored at a provided index. If the object stored
	 * is a {@link MLMessage} then instead returns the contents of that message.
	 */
	public Object getElementAt(int index) {
		Object tempObject = super.elementAt(index);
		if (tempObject instanceof MLMessage) {
			MLMessage tempMessage = (MLMessage) tempObject;

			return tempMessage.getParameter(ML.CONTENT);
		} else if (tempObject instanceof ChatMessage) {
			return tempObject;
		}

		return tempObject;
	}

	/**
	 * Copies the contents of a vector into the model. Listeners
	 * will receive change, add, or remove events as necessary
	 * @param v the vector to copy
	 */
	public void setVector(Vector<?> v) {
		if (v==null) 
			return;
		//be careful about editing this, events need to be fired correctly
		
		//update elements at same index; only fire event if actually changed
		for(int i=0; i<v.size() && i<this.size(); i++)
			if(v.get(i) != this.get(i))
				super.set(i, v.get(i));
		//remove elements if needed
		if(v.size() < this.size())
			super.removeRange(v.size(), this.size()-1);
		//add new elements if needed
		for(int i=this.size(); i<v.size(); i++)
			super.addElement(v.get(i));
	}

	public void addMessage(MLMessage newMessage) {
		super.addElement(newMessage);
	}
}