/** 
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary.
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  The
 * Knowledge Science Group makes no representations about the suitability of
 * this software for any purpose.  It is provided "as is" without express or
 * implied warranty.</p>
 */
package casa.ui;

import casa.CooperationDomain;
import casa.agentCom.URLDescriptor;

import java.util.Vector;

/**
 * Listmodel for the cooperation domain members list.
 * Used mainly to make for pretty member expiry effects (fade, strikeout, etc.)
 * 
 * Created on May 5, 2005
 * @author <a href="mailto:sedachv@cpsc.ucalgary.ca">Vladimir Sedach</a>
 * 
 * @version 0.9
 */
public class CooperationDomainListModel extends AbstractFadingListModel {
	Vector<URLDescriptor> membersList;
	
	public CooperationDomainListModel (Vector<URLDescriptor> members) {
		super();
		membersList = members!=null?members:new Vector<URLDescriptor>();
	}
	
	public CooperationDomainListModel () {
		super();
		membersList = new Vector<URLDescriptor>();
	}
	
	// return the nicely printed colorized string
	public Object getElementAt(final int i) {
		URLDescriptor agent = membersList.get(i);
		
		// some code to format strings according to age
		// shamelessly stolen from SocialCommitment.java		
		StringBuffer display = new StringBuffer ();
		
		// the following code determines the color of the text
		int red, green, blue;
		red = agent.getMark() * 255 / CooperationDomain.PING_CHANCES;
		// the above detail should be moved into a common freshness protocol
		green = 0; blue = 0;
		if (agent.isWithdrawn()) {
			float fade = (float)(System.currentTimeMillis() - agent.timeOfWithdrawal()) / FADE_TIME;
			red += (int)(fade * (255 - red));
			green = (int)(fade * 255);
			if (green > 255) green = 255;
			blue = (int)(fade * 255);
			if (blue > 255) blue = 255;
		}
		if (red > 255) red = 255;
		
		// this section of the code outputs the actual HTML
		display.append ("<html>");
		if (agent.isWithdrawn()) {
			display.append("<strike>");
		}
		// print font color
		display.append ("<font color=\"#");
		display.append(((red < 16) ? "0" : "") + Integer.toHexString(red).toUpperCase());
		display.append(((green < 16) ? "0" : "") + Integer.toHexString(green).toUpperCase());
		display.append(((blue < 16) ? "0" : "") + Integer.toHexString(blue).toUpperCase());
		display.append ("\">");
		// print the actual URLDescriptor
		display.append (agent.toString());
		// print closing tags
		display.append ("</font>");
		if (agent.isWithdrawn())
			display.append("</strike>");
		display.append ("</html>");
		return display.toString ();
	}
	
	public int getSize() {
		return membersList.size();
	}
	
	@Override
	public void removeMember(Object agentURL) {
		assert (agentURL instanceof URLDescriptor);
		// note that equality for URLDescriptors means they are pointing to the same agent, 
		// not that they share any other properties 
		int indexOf = membersList.indexOf(agentURL);
		if (indexOf != -1) { // if the agentURL is actually in there
			URLDescriptor agent = membersList.get(indexOf);
			// mark the agent for later removal
			agent.withdraw();
		}
	}
	
	@Override
	public void addMember(Object agent) {
		assert (agent instanceof URLDescriptor);
		membersList.add((URLDescriptor)agent);
		int end = membersList.size()-1;
		fireIntervalAdded(this, end, end);
	}
	
	/**
	 * This method is called repeatedly on intervals to implement fading of 
	 * agents that have not reponded to pings (are inactive).
	 * @see casa.ui.AbstractFadingListModel#refreshList()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void refreshList () {
	    long now = System.currentTimeMillis ();

	    // here, want to remove members that have finished fading, and start fading those agents that have been removed
	    Vector<URLDescriptor> oldList = (Vector<URLDescriptor>)membersList.clone();
	    for (URLDescriptor member: oldList) {
	      // if the agent has sufficiently decomposed, remove it
	      if (member.isWithdrawn() && (now - member.timeOfWithdrawal() > FADE_TIME)) {
	      	int interval = membersList.indexOf(member);
	      	membersList.remove(member);
	      	fireIntervalRemoved (this, interval, interval);
	      }
	    }
	    // get the thing to fade some more
	    fireContentsChanged (this, 0, membersList.size () - 1);
	 }	
}
