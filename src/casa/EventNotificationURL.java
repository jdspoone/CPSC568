/**
 * 
 */
package casa;

import casa.agentCom.URLDescriptor;
import casa.interfaces.ProcessInterface;
import casa.util.CASAUtil;

import java.text.ParseException;

/**
 * <p>Title: EventNotificationURL</p>  <p>Description: Supersedes deprecated StateURL </p> <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p> <p>Company: Knowledge Science Group, University of Calgary</p>	
 * @author  <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 * @version 0.9
 * @see  ObserverNotification
 */


public class EventNotificationURL extends ObserverNotification {

	/**
	 */
	private URLDescriptor url;
	
	public EventNotificationURL(ProcessInterface agent, String eventType, Object obj, URLDescriptor url) {
		super(agent, eventType, obj);
	    this.url=url;
	}
	
	public EventNotificationURL(ProcessInterface agent, String eventType, Object obj, String s) {
		super(agent, eventType, obj);
	    int i = CASAUtil.scanFor(s, 0, ",");
	    try {
	    	url = (URLDescriptor) CASAUtil.unserialize (s, i, null);
	    } catch (ParseException ex) {
	    	CASAUtil.log("error", "StateURL.StateURL(String): Unexpected exception parsing input", ex, true);
	    }
	}
	
	public URLDescriptor getURL() {return url;}
	
	@Override
	public String toString() {return super.toString() + ", " + CASAUtil.serialize(url);}
}
