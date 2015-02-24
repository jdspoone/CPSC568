package casa;

import casa.abcl.ParamsMap;
import casa.event.Event;
import casa.event.MessageEvent;
import casa.interfaces.AdvertisementSearchInterface;
import casa.interfaces.YellowPagesAgentInterface;
import casa.ui.AgentUI;
import casa.util.CASAUtil;

import java.text.ParseException;
import java.util.Iterator;
import java.util.Vector;

/**
 * <code>YellowPagesAgent</code> is an extension of <code>Agent</code> that
 * acts as an advertisement service for other agents.<br><br>
 *
 * Yellow pages accept requests to advertise and to remove an existing
 * advertisement.<br><br>
 *
 * Yellow pages store their advertisement list between sessions if they are set
 * persistent.<br><br>
 *
 * Yellow pages accept requests to search for a given advertisement in their
 * advertisement list.<br><br>
 *
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
 *
 * @author Jason Heard
 */

public class YellowPagesAgent extends Agent implements
    YellowPagesAgentInterface {

  private Vector<AdvertisementDescriptor> advertisements = new Vector<AdvertisementDescriptor> ();

  public YellowPagesAgent (ParamsMap params, AgentUI ui) throws Exception {
    super (params, ui);
  }

  /**
   * Handles the dispatching of all incoming messages to corresponding
   * functions, usually based upon the value of the "act" and "performative"
   * parameters of the message.  It first calls the superclass version of the
   * function which returns a <code>Status</code> object.  If the parent
   * returns a status of 0 then the message has already been handled without a
   * problem.  If it returned a negative status, the message was handled, but
   * with some errors.  So, if the status is positive, the message has not been
   * handled by the super class and an attempt is made to handle the message.
   *
   * This agent handles messages with the following "performative"s and "act"s:
   * <table cellpadding=6>
   * <tr><th align=left>performative</th><th align=left>act</th><th align=left>Action</th></tr>
   * <tr><td>request</td><td>advertise</td><td>calls respondToJoin (msg)</td></tr>
   * <tr><td>request</td><td>remove.advertisement</td><td>calls respondToWithdraw (msg)</td></tr>
   * <tr><td>request</td><td>search</td><td>calls respondToGetMembers (msg)</td></tr>
   * </table>
   * @param event The incoming message to be handled.
   * @return The <code>Status</code> describing whether the message was
   * handled:
   * <li>0 if the message was handled,</li>
   * <li>-121 if the message was a reply to a request that this agent didn't
   * send,</li>
   * <li>positive if the message was not handled, or</li>
   * <li>negative if the message failed and no further processing is
   * necesary.</li>
   */
  @Override
//  public Status handleEvent(Event event) {
  public void handleEvent(Event event) {
  	super.handleEvent(event);
//    Status status = super.handleEvent(event);
//    if (event instanceof MessageEvent) {
//    	MLMessage msg = ((MessageEvent)event).getMessage();
//      if (status.getStatusValue() > 0) { //super didn't handle it
//        String performative = msg.getParameter(ML.PERFORMATIVE),
//                        act = msg.getParameter(ML.ACT);
//        if (performative.equals(ML.REQUEST)) {
//          if (act.equals(ML.ADVERTISE)) {
//            status = respondToAdvertise(msg);
//          }
//          if (act.equals(ML.REMOVE_ADVERTISEMENT)) {
//            status = respondToRemoveAdvertisement(msg);
//          }
//          if (act.equals(ML.SEARCH)) {
//            status = respondToSearch(msg);
//          }
//        }
//      }
//    }
//    return status;
  }

  protected Status respondToAdvertise (MLMessage message) {
    // Read the relevant info from the message
    String content = message.getParameter (ML.CONTENT);
    String language = message.getParameter (ML.LANGUAGE);

    // Construct a template reply
    MLMessage reply = MLMessage.constructReplyTo (message, getUniqueRequestID(), getURL());

    // Interpret the fields of the message
    AdvertisementDescriptor descriptor = null;
    if (language.equals (AdvertisementDescriptor.class.getName ())) {
      try {
        descriptor = new AdvertisementDescriptor (content);
        Status result = advertise (descriptor);
        reply.setParameter (ML.LANGUAGE, Status.class.getName ());
        reply.setParameter (ML.CONTENT, result.toString ());
      } catch (ParseException e) {
        reply.setParameter (ML.PERFORMATIVE, ML.FAILURE);
        reply.setParameter (ML.LANGUAGE, String.class.getName ());
        reply.setParameter (ML.CONTENT,
            "YellowPagesAgent.respondToAdvertise:  Could not interpret content field: " +
            e.toString ());
        println ("error",
            "YellowPagesAgent.respondToAdvertise:  Could not interpret content field: " +
            e.toString ());
      }
    } else {
      reply.setParameter (ML.PERFORMATIVE, "failure");
      reply.setParameter (ML.LANGUAGE, String.class.getName ());
      reply.setParameter (ML.CONTENT,
                          "YellowPagesAgent.respondToAdvertise:  Could not interpret content field: Language does not equal 'AdvertisementDescriptor.class.getName ()'");
      println("error","YellowPagesAgent.respondToAdvertise:  Could not interpret content field: Language does not equal 'AdvertisementDescriptor.class.getName ()'");
    }

    Status status = sendMessage (reply);
    return status;
  }

  /**
   * Adds the given advertisement to the list of current advertisements.
   *
   * @param advertisement The advertisement that should be added to the list of
   * current advertisements.
   * @return The <code>Status</code> of the advertise action:
   * <li>0 if it was successful with no warnings or errors, or</li>
   * <li>1 if the advertisement was previously in the list of current
   * advertisements.</li>
   */
  public Status advertise (AdvertisementDescriptor advertisement) {
    boolean repeatAdvertisement = false;

    repeatAdvertisement = advertisements.remove (advertisement);

    advertisements.add (advertisement);

    //notifyObservers (new casa.State (ObservableEvent.STATE_ADVERTISEMENT_ADDED));
    notifyObservers (ML.EVENT_ADVERTISEMENT_ADDED, null);
    
    if (repeatAdvertisement) {
      return new Status (1, "Advertisement already existed in advertisement list, updating if necesary.");
    } else {
      return new Status (0);
    }
  }

  protected Status respondToRemoveAdvertisement (MLMessage message) {
    // Read the relevant info from the message
    String content = message.getParameter (ML.CONTENT);
    String language = message.getParameter (ML.LANGUAGE);

    // Construct a template reply
    MLMessage reply = MLMessage.constructReplyTo (message, getUniqueRequestID(), getURL());

    // Interpret the fields of the message
    AdvertisementDescriptor descriptor = null;
    if (language.equals (AdvertisementDescriptor.class.getName ())) {
      try {
        descriptor = new AdvertisementDescriptor (content);
        Status result = removeAdvertisement (descriptor);
        reply.setParameter (ML.LANGUAGE, Status.class.getName ());
        reply.setParameter (ML.CONTENT, result.toString ());
      } catch (ParseException e) {
        reply.setParameter (ML.PERFORMATIVE, ML.FAILURE);
        reply.setParameter (ML.LANGUAGE, String.class.getName ());
        reply.setParameter (ML.CONTENT,
                            "YellowPagesAgent.respondToRemoveAdvertisement:  Could not interpret content field: " +
                            e.toString ());
        println("error","YellowPagesAgent.respondToRemoveAdvertisement:  Could not interpret content field: " +
                     e.toString ());
      }
    } else {
      reply.setParameter (ML.PERFORMATIVE, "failure");
      reply.setParameter (ML.LANGUAGE, String.class.getName ());
      reply.setParameter (ML.CONTENT,
                          "YellowPagesAgent.respondToRemoveAdvertisement:  Could not interpret content field: Language does not equal 'AdvertisementDescriptor.class.getName ()'");
      println("error","YellowPagesAgent.respondToRemoveAdvertisement:  Could not interpret content field: Language does not equal 'AdvertisementDescriptor.class.getName ()'");
    }

    Status status = sendMessage (reply);
    return status;
  }

  /**
   * Removes the given advertisement from the list of current advertisements.
   *
   * @param advertisement The advertisement that should be removed from the
   * list of current advertisements.
   * @return The <code>Status</code> of the remove advertisement action:
   * <li>0 if it was successful with no warnings or errors, or</li>
   * <li>1 if the advertisement was not previously in the list of current
   * advertisements.</li>
   */
  public Status removeAdvertisement (AdvertisementDescriptor advertisement) {
    boolean advertisementFound = false;

    advertisementFound = advertisements.remove (advertisement);

    if (advertisementFound) {
      //notifyObservers (new casa.State(ObservableEvent.STATE_ADVERTISEMENT_REMOVED));
    	notifyObservers(ML.EVENT_ADVERTISEMENT_REMOVED, null);

      return new Status (0);
    }

    return new Status (1, "Advertisement not found: " + advertisement.toString ());
  }

  protected Status respondToSearch (MLMessage message) {
    // Read the relevant info from the message
    String content = message.getParameter (ML.CONTENT);
    String language = message.getParameter (ML.LANGUAGE);

    // Construct a template reply
    MLMessage reply = MLMessage.constructReplyTo (message, getUniqueRequestID(), getURL());

    // Interpret the fields of the message
    AdvertisementSearchInterface descriptor = null;
    if (language.equals (AdvertisementSearchInterface.class.getName ())) {
      try {
        descriptor = (AdvertisementSearchInterface) CASAUtil.unserialize (
            content, null);
        StatusAdvertisementDescriptorList result = search (descriptor);
        reply.setParameter (ML.LANGUAGE,
                            StatusAdvertisementDescriptorList.class.getName ());
        reply.setParameter (ML.CONTENT, result.toString ());
      } catch (ParseException ex1) {
        reply.setParameter (ML.PERFORMATIVE, ML.FAILURE);
        reply.setParameter (ML.LANGUAGE, String.class.getName ());
        String err = "YellowPagesAgent.respondToSearch:  Could not unserialize the content field: '"+content+"'";
        reply.setParameter (ML.CONTENT,err);
        println("error", err);
      }
    } else {
      reply.setParameter (ML.PERFORMATIVE, "failure");
      reply.setParameter (ML.LANGUAGE, String.class.getName ());
      String err = "YellowPagesAgent.respondToSearch:  Could not interpret content field: Language does not equal 'AdvertisementSearchInterface.class.getName ()'";
      reply.setParameter (ML.CONTENT,err);
      println("error", err);
    }

    Status status = sendMessage (reply);
    return status;
  }

  /**
   * Retreives a list of advertisements that match the specified search
   * parameters.  The search parameters are encapsulated in a
   * <code>AdvertisementSearchInterface</code> object.  Returns a list of
   * <code>AdvertisementDescriptor</code>s (encapsulated in a
   * <code>StatusAdvertisementDescriptorList</code>) that match the specified
   * search parameters.
   *
   * @param searchParameters An object that will determine which of the
   * advertisements is a match.
   * @return A <code>Vector</code> of <code>AdvertisementDescriptor</code>s
   * (encapsulated in a <code>StatusAdvertisementDescriptorList</code>) that
   * match the specified search parameters.  The status will be:
   * <li>0 indicating the operation was successful.</li>
   */
  public StatusAdvertisementDescriptorList search (AdvertisementSearchInterface
      searchParameters) {
    Vector<AdvertisementDescriptor> tempVector = new Vector<AdvertisementDescriptor> ();

    AdvertisementDescriptor tempAdvertisement = null;
    for (Iterator<AdvertisementDescriptor> i = advertisements.iterator(); i.hasNext(); ){
      tempAdvertisement = i.next();

      if (searchParameters.match(tempAdvertisement)) {
        tempVector.add(tempAdvertisement);
      }
    }

    return new StatusAdvertisementDescriptorList (0, tempVector);
  }
}