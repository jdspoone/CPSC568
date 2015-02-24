package casa;

import casa.abcl.ParamsMap;
import casa.event.Event;
import casa.event.MessageEvent;
import casa.ui.BufferedAgentUI;
import casa.util.CASAUtil;

import org.armedbear.lisp.JavaObject;
import org.armedbear.lisp.SimpleString;

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

public class AgentRemoteProxy extends TransientAgent {

  private static int portToTry = 7850;
  
  private static ParamsMap makeParams(String namePrefix) {
  	ParamsMap ret = new ParamsMap();
  	String name = namePrefix+"$rproxy";
  	ret.put("NAME", name, new SimpleString(name), false);
  	ret.put("PORT", -portToTry, new JavaObject(-portToTry), false);
  	ret.put("TYPE", "casa.AgentRemoteProxy", new SimpleString("casa.AgentRemoteProxy"), false);
  	return ret;
  }

  public AgentRemoteProxy(String namePrefix) throws Exception {
    super(makeParams(namePrefix), new BufferedAgentUI());
    portToTry=getPort()+1;
  }

  /**
   * Overrides AbstractProcess.handleMessage() by first calling (and returning
   * the status of ) super.handleMessage(), which takes care of all replies.
   * The message wasn't a reply then it handles the following list of "acts"
   * for messages with request performatives:
   * <table>
   * <tr><th> act        </th><th> Action                </th></tr>
   * <tr><td> update     </td><td> call update() on all observers</td></tr>
   * </table>
   * @param event
   * @return
   */
  @Override
//  public Status handleEvent(Event event) {
  public void handleEvent(Event event) {
  	super.handleEvent (event);
//  	Status status = super.handleEvent (event);
//  	if (status.getStatusValue () == 121)
//  		return status; //special handling for synchronous calls
//  	if (status.getStatusValue () == 0)
//  		return status; // the message must have been a reply successfully handled by the parent class
//  	if (status.getStatusValue () < 0)
//  		return status; //the message must either mismatched reply or a reply that was unsuccessfully handled by the parent class

  	//at this point, we know we aren't dealing with a reply

  	if (event instanceof MessageEvent) {
  		MLMessage msg = ((MessageEvent)event).getMessage();
  		if (isA(msg.getParameter(ML.ACT), ML.UPDATE)) {
  			try {
  				String content = msg.getParameter (ML.CONTENT);
  				Object o       = CASAUtil.unserialize (content, msg.getParameter (ML.LANGUAGE));

  				//notifyObservers (o);
  				//What kind of event is this?
  				notifyObservers (ML.EVENT_MESSAGE_EVENT, o);
  			} catch (Exception ex) {
  				println("error", "AgentRemoteProxy.handleMessage(update): Could not interpret content of message:\n"
  						+ msg.toString(true),ex);
  			}
//  			return new Status(0);
  		}
  	}

//  	return new Status (1);
  }

}
