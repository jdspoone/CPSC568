
package casa;
import casa.abcl.ParamsMap;
import casa.agentCom.URLDescriptor;
import casa.event.RecurringTimeEvent;
import casa.exceptions.URLDescriptorException;
import casa.interfaces.CooperationDomainInterface;
import casa.ui.AgentUI;
import casa.ui.CooperationDomainWindow;
import casa.util.CASAUtil;
import casa.util.Pair;
import casa.util.PropertyException;

import jade.semantics.lang.sl.tools.SL;

import java.awt.Container;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


//import com.sun.j3d.utils.behaviors.vp.WandViewBehavior.ScaleListener2D;

/**
 * Acts as a meeting room for other agents.  It's primarily responsible for forwarding 
 * messages between agents. <br> In addition to the messages defined in  
 * {@link Agent} , this agent can originate the following messages types: 
 * <table border="1" bgcolor="gold" cellpadding="3"> 
 * 	<tr><th><a href="doc-files/performatives.gif">performative</a> </th>
 * 			<th> {@link TransientAgent#makeDefaultActs()  act}        </th>
 * 			<th><em>see</em>     </th>
 * 			<th><em>reply handler</em></th></tr> 
 * 	<tr><td>notify      </td>
 * 			<td>join.cd        </td>
 * 			<td> {@link #notifyJoin(URLDescriptor)} </td> 
 * 			<td>-</td></tr> 
 * 	<tr><td>notify      </td>
 * 			<td>withdraw.cd    </td>
 * 			<td> {@link #notifyWithdraw(URLDescriptor)} </td> 
 * 			<td>-</td></tr> 
 * </table> </ br> 
 * In addition to the messages defined in  {@link Agent} , this agent responds to the 
 * following messages types: 
 * <table border="1" bgcolor="orange" cellpadding="3"> 
 * 	<tr><th><a href="doc-files/performatives.gif">performative</a> </th>
 * 			<th> {@link TransientAgent#makeDefaultActs()  act}                </th>
 * 			<th><em>see</em>     </th></tr> 
 * 	<tr><td>request      </td>
 * 			<td>join.cd             </td>
 * 			<td> {@link #respondToJoin(MLMessage)} </td></tr> 
 * 	<tr><td>inform       </td>
 * 			<td>withdraw.cd         </td>
 * 			<td> {@link #handleWithdraw(MLMessage)} </td></tr> 
 * 	<tr><td>request      </td>
 * 			<td>withdraw.cd         </td>
 * 			<td> {@link #respondToWithdraw(MLMessage)} </td></tr> 
 * 	<tr><td>request      </td>
 * 			<td>get.members         </td>
 * 			<td> {@link #respondToGetMembers(MLMessage)} </td></tr> 
 * 	<tr><td>request      </td>
 * 			<td>observe.membership  </td>
 * 			<td> {@link #respondToObserveMembership(MLMessage)} </td></tr> 
 * 	<tr><td>request      </td>
 * 			<td>get.history         </td>
 * 			<td> {@link #perform_get_history(MLMessage)} </td></tr> 
 * 	<tr><td>request      </td>
 * 			<td>get.data            </td>
 * 			<td> {@link #perform_get_data(MLMessage)} </td></tr> 
 * 	<tr><td>request      </td>
 * 			<td>put.data            </td>
 * 			<td> {@link #perform_put_data(MLMessage)} </td></tr> 
 * </table> </ br> 
 * The CooperationDomain acts as a message forwarding agent and can receive messages 
 * not intended for processing by itself.  The following table describes the behaviour 
 * of the CooperationDomain when it receives a message: 
 * <table border="1" cellpadding="3"> 
 * 	<tr><td></td> 
 * 			<td colspan="2" bgcolor="#cccccc">"to CD"<br> A message to this CD</td> 
 * 			<td colspan="2">"ambiguous"<br> A message that <i>could</i> be to this CD</td> 
 * 			<td colspan="2" bgcolor="#cccccc">"whisper"<br> A message to one or more CD members</td> 
 * 			<td colspan="2">"public"<br> A broadcast message</td> 
 * 			<td colspan="2" bgcolor="#cccccc">"directed"<br> A message to one or more CD members, but cc'ed to all</td> </tr> 
 * 	<tr bgcolor="yellow" align="center"> 
 * 			<td>fields</td>   
 * 			<td>rec'd message</td>  
 * 			<td>send message</td>    
 * 			<td>rec'd message</td>  
 * 			<td>send message(s)</td>    
 * 			<td>rec'd message</td>   
 * 			<td>send message(s)</td>   
 * 			<td>rec'd message</td>   
 * 			<td>send message(s)</td>
 * 			<td>rec'd message</td>
 * 			<td>send message(s)</td> </tr> 
 * 	<tr><td>:sender</td> 
 * 			<td bgcolor="#cccccc">s</td> 
 * 			<td rowspan="4" bgcolor="#cccccc">none</td> 
 * 			<td>s</td> 
 * 			<td>this-CD</td> 
 * 			<td bgcolor="#cccccc">s</td> 
 * 			<td bgcolor="#cccccc">this-CD</td> 
 * 			<td>s</td> 
 * 			<td>this-CD</td> 
 * 			<td valign="top" bgcolor="#cccccc">s</td> 
 * 			<td valign="top" bgcolor="#cccccc">this-CD</td> </tr> 
 * 	<tr><td>:receiver</td> 
 * 			<td bgcolor="#cccccc">this-CD</td> 
 * 			<td>this-CD</td> 
 * 			<td><nobr><i>forall </i>member<sub>i</sub></nobr></td> 
 * 			<td bgcolor="#cccccc">this-CD</td> 
 * 			<td bgcolor="#cccccc"><nobr><i>forall </i>t<sub>i</sub></nobr></td> 
 * 			<td>this-CD</td> <td><nobr><i>forall </i>member<sub>i</sub></nobr></td>
 * 			<td valign="top" bgcolor="#cccccc">this-CD</td> 
 * 			<td valign="top" bgcolor="#cccccc"><nobr><i>forall </i>member<sub>i</sub></nobr></td> </tr> 
 * 	<tr><td>:from</td> <td bgcolor="#cccccc">f</td> 
 * 			<td>f</td> 
 * 			<td>f?f:s</td> 
 * 			<td bgcolor="#cccccc">f</td> 
 * 			<td bgcolor="#cccccc">f?f:s</td> 
 * 			<td>f</td> 
 * 			<td>f?f:s</td>
 * 			<td valign="top" bgcolor="#cccccc">f</td> 
 * 			<td valign="top" bgcolor="#cccccc">f?f:s</td> </tr> 
 * 	<tr><td>:to</td> 
 * 			<td bgcolor="#cccccc">this-CD</td> 
 * 			<td>(empty)</td> 
 * 			<td>*</td> 
 * 			<td bgcolor="#cccccc"><nobr>{t<sub>i </sub>}+ </nobr></td> 
 * 			<td bgcolor="#cccccc"><nobr>t<sub>target</sub> {t<sub>i</sub>-t<sub>target</sub>}*</nobr></td> 
 * 			<td>*</td> 
 * 			<td>*</td>
 * 			<td valign="top" bgcolor="#cccccc"><nobr>"+" {t<sub>i</sub>}+</nobr></td> 
 * 			<td valign="top" bgcolor="#cccccc"><nobr>"+" t<sub>target</sub> {t<sub>i</sub>-t<sub>target</sub>}*</nobr></td> </tr> 
 * 	<tr><td>notes</td> 
 * 			<td colspan="2" bgcolor="#cccccc">attempt to interpret and service the message</td> 
 * 			<td colspan="2">Attempt to interpret and service the message.<br> If the CD can't, then forward it to all</td> 
 * 			<td colspan="2" bgcolor="#cccccc">t may be a list of recipients.<br>The sent message's "to" list always has the recipient first</td> 
 * 			<td colspan="2">explicit "forward to all"</td> 
 * 			<td valign="top" rowspan="1" colspan="2" bgcolor="#cccccc">eg: Received message: ":to + <i>url url url</i>".<br>Sent message: ":to the-receiver other-urls-in-the-TO-list"</td> </tr> 
 * </table> </ br> 
 * In addition, this class has the following properties: 
 * <ul>	<li> The cooperation domain also maintains a history of all messages it forwards. 
 * 		 	<li> A cooperation domain will default to being persistent if someone saves data or modifies attributes, but will not persist otherwise.  However, if someone explicitly sets the cooperation domain to be non-persistent, then the cooperation domain will be non-persistent, and all data will be lost when the last member leaves. 
 * 			<li> A cooperation domain terminates (exits) automatically when all members leave. (<em>NOT TRUE, CURRENTLY DISABLED</em>) 
 * 			<li> A cooperation domain will periodically ping its members.  If a member agent does not answer their pings for 3 consecutive times, they are automatically removed from membership. 
 * 			<li> Members may send the standing request message <em>observe.membership</em> to the cooperation domain, and the cooperation domain will send the inform message <em>notify.join</em> whenever a member joins the cooperation domain and <em>notify.withdraw</em> whenever a member leaves the cooperation domain. 
 * </ul> 
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission 
 * to use, copy, modify, distribute and sell this software and its documentation for 
 * any purpose is hereby granted without fee, provided that the above copyright notice 
 * appear in all copies and that both that copyright notice and this permission notice 
 * appear in supporting documentation.  The Knowledge Science Group makes no 
 * representations about the suitability of this software for any purpose.  
 * It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @author  Jason Heard
 * @author  <a href="mailto:sedachv@cpsc.ucalgary.ca">Vladimir Sedach</a>
 * @author  <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 * @version 0.9
 */

public class CooperationDomain
    extends Agent
    implements
    CooperationDomainInterface {
  /**
   * The name of the data object in which we store the cooperation domain
   * message history.
   */
  protected static String HISTORY_NODE = "_*history";

  /**
	 * A <code>Vector</code> of <code>URLDescriptor</code>s of the members of this cooperation domain.
	 */
  protected Vector<URLDescriptor> members = new Vector<URLDescriptor>();
  
//  private HashSet<URLDescriptor> membershipSubscriptions = new HashSet<URLDescriptor>(); 
  protected Hashtable<URLDescriptor, String> membershipSubscriptions = new Hashtable<URLDescriptor, String>();

  /**
   * The mesage notification list.  A <code>Hashtable</code> linking the
   * <code>URLDescriptor</code> of an agent that is observing the messages of
   * this cooperation domain to a <code>String</code> of the
   * <code>ML.REPLY_WITH</code> parameter that should be used when notifying
   * that agent of sent messages.
   */
  private Hashtable<URLDescriptor,String> messageObservers = new Hashtable<URLDescriptor,String>();

  private RecurringTimeEvent pingMemebersEvent = null;
  
  /* (non-Javadoc)
	 * @see casa.AbstractProcess#finishRun()
	 */
	@Override
	protected void finishRun() {
		super.finishRun();
		if (pingMemebersEvent!=null)
			pingMemebersEvent.cancel();
	}

	/**
   * Creates a new <code>CooperationDomain</code>.
   *
   * @param params
   * @param ui
   * @throws Exception If the agent creation fails for any reason.
   */
  public CooperationDomain(ParamsMap params, AgentUI ui) throws Exception {
    super(params, ui);
    in("CooperationDomain.CooperationDomain");
    out("CooperationDomain.CooperationDomain");
  }

  @Override
  public void initializeAfterRegistered(boolean registered) {
  	in("CooperationDomain.init");
  	super.initializeAfterRegistered(registered);
  	setCDCreator(this.getURL().getUser());
  	if (kBase!=null) {
  		kBase.addClosedPredicate(SL.formula("(CDmember ??x ??y)"));
  	}
  	pingMemebersEvent = new RecurringTimeEvent(ML.EVENT_RECURRINGEXECUTABLE, this, System.currentTimeMillis()+300000, 300000)
  	{
  		@Override
  		public void fireEvent() {
  			super.fireEvent();
  			//do this no more than every 5 minutes (or whatever PING_INTERVAL is)
  			try {
  				for (int i = members.size() - 1; i >= 0; i--) {
  					URLDescriptor member = members.elementAt(i);
  					if (member.getMark() > PING_CHANCES) {
  						withdraw(member);
  						println("warning",
  								"CooperationDomain.messageBufferLoopPeriodic: removing '" +
  										member.toString(getURL()) + "'");

  					}
  					else {
  						member.mark();
  						Status tempStatus = doPing(member, PING_WAIT_TIME);
  						if (tempStatus.getStatusValue() != 0) {
  							println("warning",
  									"CooperationDomain.messageBufferLoopPeriodic: got exception when pinging '"
  											+ member.toString(getURL()) + "': " +
  											tempStatus.getExplanation());

  						}
  					}
  				} 	
  			}
  			catch (NullPointerException ex1) {
  				println ("error", "Exception in CooperationDomain.messageBufferLoopPeriodic: " +
  						ex1.getCause().toString());
  			}
  		} 
  	};

  	out("CooperationDomain.init");
  }

  /**
   * Sets the creator (username) of the cooperation domain to the given value.
   * This allows the security system (in this case the authorization) to
   * retrieve the user that has "unlimited" access to the cooperation domain.
   * Returns a <code>String</code> value representing the username of the
   * creator of the cooperation domain.  This person/agent has full access
   * rights in the cooperation domain.   This means that this person/agent can
   * modify other users' access rights.  That is, limit or upgrade the level of
   * cooperation and collaboration that a person/agent has in the specific
   * cooperation domain.
   *
   * @param cdCreator A <code>String</code> value representing the username of
   * the creator of the cooperation domain.
   */
  private void setCDCreator(String cdCreator) {
    in("CooperationDomain.setCDCreator");
    setStringProperty("CD_Creator", cdCreator);
    out("CooperationDomain.setCDCreator");
  }

  @Override
  protected PerformDescriptor assembleDefault (MLMessage message) {
    in("CooperationDomain.assembleDefault");
    StatusObject<Pair<List<URLDescriptor>, MLMessage>> stat = 
      new StatusObject<Pair<List<URLDescriptor>,MLMessage>> ();
    PerformDescriptor ret = new PerformDescriptor (stat);
    String content = message.getParameter (ML.CONTENT);
    Pair<List<URLDescriptor>, MLMessage> proxyInfo = extractCDProxyInformation (content);
    stat.setObject (proxyInfo);
    
    if (proxyInfo == null) {
      String m = "Could not interpret content field in message.";
      if (this.isLoggingTag ("warning")) {
        println ("warning", m + "(in CooperationDomain.assembleDefault)");
      }
      ret.setStatus (new Status (Status.EXCEPTION_CAUGHT, m));
    }
    else {
    	proxyInfo.getSecond ().setParameter(ML.CD, getURL().toString());
    }
    out("CooperationDomain.assembleDefault");
    return ret;
  }

  /**
   * Using the <em>content</em> parameter, parse out the members to forward
   * a message to.  The content parameter should contain a serialized vector
   * of the address string and a message to forward.  The address string is of
   * 3 possible syntaxes:
   * <ol>
   * <li> "*" - broadcast
   * <li> "+" { url }* - directed (addressed, but public broadcast)
   * <li> { url }* - whisper (private)
   * </ol>
   * No non-members (as specified by the <em>members</em> parameter) are ever
   * included in the the returned members-to-forward list, which will
   * contain all the members in case 1 and 2, but only the targeted agents
   * in case 3.  The returned message will be the same as the message in the 
   * <em>content</em> but will have a new RECIPIENTS field containing the
   * intented recipients in case 2 and 3, or "*" in case 1.  Note that the
   * RECIPIENTS field is NOT the same as the returns list of urls. <br>
   * If, after all this, there are no list or urls to return, an error 
   * message is displayed.
   * @param content
   * @return
   */
  @SuppressWarnings("unchecked")
	public Pair<List<URLDescriptor>,MLMessage> extractCDProxyInformation (String content) {    
    Pair<String, MLMessage> basicInfo = MLMessage.extractProxyInformation (content);
    
    if (basicInfo == null) {
      return null;
    }

    List<URLDescriptor> list;
    String recipients = basicInfo.getFirst ();

    /*********** *-SYNTAX ************************************/
    if (recipients.equals("*")) {
      // Do we care if we forward to ourself?
      list = (List<URLDescriptor>)members.clone();
    /*********************************************************/
    } else {
      List<URLDescriptor> tempList = new ArrayList<URLDescriptor> (); 
      TokenParser parser = new TokenParser(recipients);
      boolean directedSpeach;

      // check for whisper mode (+syntax)
      if (parser.getNextToken().equals("+")) {
        directedSpeach = true;
      }
      else {
        directedSpeach = false;
        parser.putback();
      }

      // scan through a space-separated list of URLs and put only those that are members into 'tempList'
      URLDescriptor tempMember;
      do {
        try {
          tempMember = URLDescriptor.fromString(parser);
        }
        catch (URLDescriptorException ex1) {
          tempMember = null;
        }
        
        if (tempMember!=null) {
        	if (members.contains(tempMember)) {
        		tempList.add(tempMember);
        	} else {
        		println("warning","Attempt to forward to non-member "+tempMember);
        	}
        }
      } while (tempMember != null);

      //
      StringBuffer toString;
      Iterator<URLDescriptor> i = tempList.iterator();
      if (!i.hasNext()) println("warning","there are no CD members specified in the addressees list: \""+content+"\"");
      else {
	      	tempMember = i.next();
	      //make the list of recipients
	      toString = new StringBuffer();
	      if (directedSpeach) {
	        toString.append("+ ");
	      }
	      toString.append(tempMember.toString());
	
	      while (i.hasNext()) {
	        tempMember = i.next();
	
	        toString.append(' ').append(tempMember.toString());
	      }
	      recipients = toString.toString ();
      }

      if (directedSpeach) {
        list = (List<URLDescriptor>)members.clone();
      } else {
        list = tempList;
      }
    }

    //update the message to include a recipients description in the RECIPIENTS field
    basicInfo.getSecond ().setParameter(ML.RECIPIENTS, recipients);

    return new Pair<List<URLDescriptor>, MLMessage> (list, basicInfo.getSecond());
  }
  
  /**
   * Constructs a new proxy message which instructs the cooperation domain to
   * forward the encapsulated directed or whisper message. A <em>whisper</em>
   * message is forwarded only to the given agent, while a <em>directed</em>
   * message is forwarded to all agents, but marked as directed towards the
   * given agent.
   * 
   * @param message The message to encapsulate within the proxy message.
   * @param sender The sender of the proxy message.
   * @param cd The cooperation domain which will recieve the proxy message and
   *          forward the encapsulated message.
   * @param to A {@link List} containing the {@link URLDescriptor}s of the
   *          agents that should recieve the encapsulated message.
   * @param whisper Constructs a <em>whisper</em> message if <code>true</code>;
   *          constructs a <em>directed</em> message if <code>false</code>.
   * @return A new proxy message.
   * @see MLMessage#constructProxyMessage(MLMessage, URLDescriptor, URLDescriptor, String)
   */
  public static MLMessage constructCDProxyMessage (MLMessage message, URLDescriptor sender, URLDescriptor cd, URLDescriptor to, boolean whisper) {
    sender.pushViaAtEnd(cd);
    
    String toString;
    if (whisper) {
      toString = to.toString ();
    } else {
      toString = "+ " + to.toString ();
    }

    MLMessage tempMessage = MLMessage.constructProxyMessage (message, sender, cd, toString);

    return tempMessage;
  }

  /**
   * Constructs a new proxy message which instructs the cooperation domain to
   * forward the encapsulated directed or whisper message. A <em>whisper</em>
   * message is forwarded only to the given agents, while a <em>directed</em>
   * message is forwarded to all agents, but marked as directed towards the
   * given agents.
   * 
   * @param message The message to encapsulate within the proxy message.
   * @param sender The sender of the proxy message.
   * @param cd The cooperation domain which will recieve the proxy message and
   *          forward the encapsulated message.
   * @param to A {@link List} containing the {@link URLDescriptor}s of the
   *          agents that should recieve the encapsulated message.
   * @param whisper Constructs a <em>whisper</em> message if <code>true</code>;
   *          constructs a <em>directed</em> message if <code>false</code>.
   * @return A new proxy message.
   * @see MLMessage#constructProxyMessage(MLMessage, URLDescriptor, URLDescriptor, String)
   */
  public static MLMessage constructCDProxyMessage (MLMessage message, URLDescriptor sender, URLDescriptor cd, List<URLDescriptor> to, boolean whisper) {
    sender.pushViaAtEnd(cd);

    StringBuffer tempTo = new StringBuffer ();

    if (! whisper) {
      tempTo.append ('+');
    }
    for (Iterator<?> i = to.iterator (); i.hasNext (); ) {
      tempTo.append (ML.BLANK);
      tempTo.append (i.next ().toString ());
    }

    MLMessage tempMessage = MLMessage.constructProxyMessage (message, sender, cd, tempTo.toString ());

    return tempMessage;
  }

  /**
   * Constructs a new proxy message which instructs the cooperation domain to
   * forward the encapsulated message to all current members of that cooperation
   * domain.
   * 
   * @param message The message to encapsulate within the proxy message.
   * @param sender The sender of the proxy message.
   * @param cd The cooperation domain which will recieve the proxy message and
   *          forward the encapsulated message.
   * @return A new proxy message.
   * @see MLMessage#constructProxyMessage(MLMessage, URLDescriptor, URLDescriptor, String)
   */
  public static MLMessage constructCDProxyMessage (MLMessage message, URLDescriptor sender, URLDescriptor cd) {
    sender.pushViaAtEnd (cd);

    MLMessage tempMessage =  MLMessage.constructProxyMessage (message, sender, cd, "*");

    return tempMessage;
  }

  /**
   * Take care of forwarding messages.  This would be straight forward,
   * except that we also need to construct and send INFORM/MESSAGE/FORWARDED
   * messages to any observers we might have.
   * TODO Finish documenting the sendForwardedMessage method.
   * <p>
   * Deprecated in favour of {@link #consider_proxy_message(MLMessage)}
   *
   * @param message the {@link MLMessage message} to forward
   * @return the status returned from a call to {@link AbstractProcess#sendMessage(MLMessage)}
   * @see #consider_proxy_message(MLMessage)
   */
  @SuppressWarnings("unused")
  @Deprecated
  private Status sendForwardedMessage (MLMessage message) {
    in("CooperationDomain.sendForwardedMessage");
    // Most importantly, actually send the message.
//    ignoreMessages = true;
	  message.setParameter("ignore-processing-this-message","forwarding message");
    Status result = sendMessage(message);
//    ignoreMessages = false;

    // Then send it to the message observer(s).
    if (messageObservers.size() > 0) {
      Enumeration<?> e = messageObservers.keys();
      URLDescriptor receiver = (URLDescriptor) e.nextElement();

      String m[] = {
          ML.LANGUAGE, MLMessage.class.getName(),
          ML.CONTENT, message.toString ()};
      MLMessage messageEnvelope = getNewMessage(ML.INFORM,
                                        ML.MESSAGE_FORWARDED,
                                        receiver, m);

      String replyWith = messageObservers.get(receiver);
      messageEnvelope.setParameter(ML.REPLY_WITH, replyWith);
      messageEnvelope.setParameter(ML.IN_REPLY_TO, replyWith);
      Status status = sendMessage(messageEnvelope);
      if (status.getStatusValue() != 0 && isLoggingTag("warning")) {
        println("warning", "Got exception when sending a notification to '" +
                receiver.toString(getURL()) + "'", status);
      }

      while (e.hasMoreElements()) {
        receiver = (URLDescriptor) e.nextElement();

        replyWith = messageObservers.get(receiver);
        messageEnvelope.setParameter(ML.RECEIVER, receiver.toString(getURL()));
        messageEnvelope.setParameter(ML.REPLY_WITH, replyWith);
        messageEnvelope.setParameter(ML.IN_REPLY_TO, replyWith);
        status = sendMessage(messageEnvelope);
        if (status.getStatusValue() != 0 && isLoggingTag("warning")) {
          println("warning", "Got exception when sending a notification to '" +
                  receiver.toString(getURL()) + "'", status);
        }
      }
    }

    out("CooperationDomain.sendForwardedMessage");
    return result;
  }
  
//  /**
//	 * This variable is set true in sendForwardedMessage before it sends
//	 * messages so that the CD doesn't try to process them with commitments.
//	 * TODO this won't work with concurrent threads.
//	 */
//  private boolean ignoreMessages = false;

  /* (non-Javadoc)
   * @see casa.TransientAgent#notifySendingMessage(casa.MLMessage)
   */
  @Override
  protected void notifySendingMessage (MLMessage message) {
	  if (message.getParameter("ignore-processing-this-message")==null) {
		  super.notifySendingMessage (message);
	  }
	  else {
	  	message.removeParameter("ignore-processing-this-message");
	  }
  }
  
  /**
   * Stores the given message in this cooperation domain's history node.
   *
   * @param message The message to store in the hostory node.
   */
  protected synchronized void archiveMessage(MLMessage message) {
    in("CooperationDomain.archiveMessage");
    OutputStream outStream = null;
    try {
      outStream = getDataObjectOutputStream(HISTORY_NODE, true);

      outStream.write(ML.BLANK);

      outStream.write(CASAUtil.toQuotedString(message.toString()).getBytes());
    }
    catch (IOException ex) {
    }

    try {
      outStream.close();
    }
    catch (Exception ex) {
    }
    out("CooperationDomain.archiveMessage");
  }

  /**
   * Public method for forwarding proxy messages (I think) 
   * 
   * @param message
   * @return the PerformDescriptor containing the Status of the {@link CooperationDomain#sendForwardMessage(MLMessage)} call
   *
   * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
	 */
  public PerformDescriptor consider_proxy_message(MLMessage message){
  	in("CooperationDomain.consider_proxy_message");
  	
  	String content = message.getParameter(ML.CONTENT);
  	Pair<List<URLDescriptor>,MLMessage> urlsAndMessage = extractCDProxyInformation(content);
  	List<URLDescriptor> urls = urlsAndMessage.getFirst();
  	MLMessage forward = urlsAndMessage.getSecond();
  	
  	Status status = new Status();
  	for (URLDescriptor url : urls){
  		MLMessage m = forward.clone();
  		m.setParameter(ML.RECEIVER, url.toString());
//  		status = sendForwardedMessage(forward);	
		  status = sendMessage(m);	
  	}
  	
  	out("CooperationDomain.consider_proxy_message");
  	return new PerformDescriptor(status);
  }
  
  
  /**
   * Responds to a join.cd request.  The format of the incoming message is:
   * <table border="1" bgcolor="gold" cellpadding="3">
   * <tr><td>{@link AbstractProcess#makeDefaultOntology() performative} </td><td>request</td></tr>
   * <tr><td>{@link TransientAgent#makeDefaultActs() act}          </td><td>join.cd</td></tr>
   * <tr><td>language     </td><td><a href="doc-files/contentLanguages.html#casa.agentCom.URLDescriptor">casa.agentCom.URLDescriptor</a></td></tr>
   * <tr><td>content      </td><td><em>a {@link URLDescriptor} object for the agent that wants to join</em></td></tr>
   * </table>
   * and responds with:
   * <table border="1" bgcolor="orange" cellpadding="3">
   * <tr bgcolor="darkorange"><th></th><th>success or warning</th><th>failure</th></tr>
   * <tr><td>{@link AbstractProcess#makeDefaultOntology() performative} </td><td>reply</td>          <td>failure | refuse</td></tr>
   * <tr><td>{@link TransientAgent#makeDefaultActs() act}          </td><td>join.cd</td>        <td>join.cd</td></tr>
   * <tr><td>language     </td><td><a href="doc-files/contentLanguages.html#casa.Status">casa.Status</a></td>    <td><a href="doc-files/contentLanguages.html#casa.Status">casa.Status</a></td></tr>
   * <tr><td>content      </td><td><em>a {@link Status} object</em></td><td><em>a {@link Status} object</em></td></tr>
   * </table>
   * The <em>failure</em>message is sent if the message is malformed; the <em>refuse</em>
   * message is sent if {@link #join(URLDescriptor)} returned a negative Status (the
   * requester agent was not joined to the domain).
   * @param message an incoming join.cd request message
   * @return the status of the {@link AbstractProcess#sendMessage(MLMessage)} call for the reply message
   */
  public PerformDescriptor perform_join_cd(MLMessage message) {
    URLDescriptor descriptor = null;
    PerformDescriptor ret = new PerformDescriptor();

    //check the message
    String[] m = {
        ML.LANGUAGE, URLDescriptor.class.getName(),
        ML.CONTENT, null};
    StatusObject<Object> stat = verifyMessage(message, m);

    if (stat.getStatusValue() == 0) {
      //at this point we know we have a valid content
      descriptor = (URLDescriptor) stat.getObject();
    }
    else { //message isn't right
      //check the message again for the weaker semantics
      String[] m2 = {
          ML.PERFORMATIVE, ML.AGREE,
          ML.ACT, ML.JOIN_CD};
      StatusObject<Object> stat2 = verifyMessage(message, m2);

      if (stat2.getStatusValue() == 0) {
        try {
          descriptor = message.getFrom ();
        }
        catch (URLDescriptorException ex) {
        }
      }
    }

    if (descriptor==null) { //message (with or without the language/content fields) isn't right
      	println("error", "CooperationDomain.preform_join_cd(): Can't interpret content URLDescriptor, returning NOTUNDERSTOOD: "+stat.getObject());
        PerformDescriptor ret2 = new PerformDescriptor(-1);
        ret2.put(ML.LANGUAGE, "text");
        ret2.put(ML.CONTENT, "Expected a valid URLDescritpor in the CONTENT field, but got: "+stat.getObject());
        return ret2;
    }
    else { // was a good message (with or without the language/content fields)
      Status result = join(descriptor);
      ret.setStatus(result);
      if (result.getStatusValue() < 0) {
        ret.put(ML.PERFORMATIVE, ML.FAILURE);//suggest an alternative performative
      }
      ret.put(ML.CONTENT, CASAUtil.serialize(result));
      ret.put(ML.LANGUAGE,"casa.Status");
    }

    return ret;
  }


	/**
	 * Adds the specified agent to the list of current members and notifies any
	 * agents that are observing membership that a new member has joined the
	 * cooperation domain.
	 * 
	 * @param joiner
	 *            The agent who is joining the cooperation domain.
	 * @return The <code>Status</code> of the join: <li>0 if it was successful
	 *         with no warnings or errors, or</li> <li>1 if the agent was
	 *         already a member and its URL is updated in the member list.</li>
	 */
	@Override
	public Status join(URLDescriptor joiner) {
		in("CooperationDomain.join");

		// Tries to remove the joiner if it's in the CD already, then re-adds it
		boolean alreadyMember = members.remove(joiner);
		URLDescriptor joinerURL;
		try {
			joinerURL = URLDescriptor.make(joiner);
		} catch (URLDescriptorException e) {
			return new Status(-3, println("error", "CooperationDomain.join()", e));
		}
		members.add(joinerURL);

		if (!alreadyMember) {
			notifyObservers(ML.EVENT_JOIN_CD, joinerURL);
			if (kBase!=null) {
				kBase.assertFormula(SL.formula("(CDmember "+getURL().toStringAgentIdentifier(true)+" "+joiner.toStringAgentIdentifier(false)+")"));
			}

			out("CooperationDomain.join");
			return new Status(0);
		} else { // if the joiner was already in the CD, update its URL
			notifyObservers(ML.EVENT_UPDATE_URL_CD, joinerURL);

			out("CooperationDomain.join");
			return new Status(1, "Already joined, updating URL");
		}
	}

  /**
   * Responds to a withdraw.cd request.  The format of the incoming message is:
   * <table border="1" bgcolor="gold" cellpadding="3">
   * <tr><td><a href="doc-files/performatives.gif">performative</a> </td><td>request</td></tr>
   * <tr><td>{@link TransientAgent#makeDefaultActs() act}          </td><td>withdraw.cd</td></tr>
   * <tr><td>language     </td><td><a href="doc-files/contentLanguages.html#casa.agentCom.URLDescriptor">casa.agentCom.URLDescriptor</a></td></tr>
   * <tr><td>content      </td><td><em>a {@link URLDescriptor} object for the agent that wants to withdraw</em></td></tr>
   * </table>
   * and responds with:
   * <table border="1" bgcolor="orange" cellpadding="3">
   * <tr bgcolor="darkorange"><th></th><th>success or warning</th><th>failure</th></tr>
   * <tr><td><a href="doc-files/performatives.gif">performative</a> </td><td>reply</td>          <td>failure | refuse</td></tr>
   * <tr><td>{@link TransientAgent#makeDefaultActs() act}          </td><td>join.cd</td>        <td>join.cd</td></tr>
   * <tr><td>language     </td><td><a href="doc-files/contentLanguages.html#casa.Status">casa.Status</a></td>    <td><a href="doc-files/contentLanguages.html#casa.Status">casa.Status</a></td></tr>
   * <tr><td>content      </td><td><em>a {@link Status} object</em></td><td><em>a {@link Status} object</em></td></tr>
   * </table>
   * The <em>failure</em>message is sent if the message is malformed; the <em>refuse</em>
   * message is sent if {@link #withdraw(URLDescriptor)} returned a negative Status (the
   * requester agent was not joined to the domain).
   * @param message an incoming withdraw.cd request message
   * @return the status of the {@link AbstractProcess#sendMessage(MLMessage)} call for the reply message
   */
  public PerformDescriptor perform_withdraw_cd(MLMessage message) {
    in("CooperationDomain.perform_withdraw_cd");
    //check the message
    String[] m = {
        ML.ACT, ML.WITHDRAW_CD,
        ML.LANGUAGE, URLDescriptor.class.getName(),
        ML.CONTENT, null};
    StatusObject<Object> stat = verifyMessage(message, m);
    Status result;

    if (stat.getStatusValue() != 0) { //message isn't right
      if (stat.getStatusValue() > 0) stat.setStatusValue( -stat.getStatusValue());
      result = stat;
      println("warning", "Invalid message in CooperationDomain.performWithdraw: " + stat.toString());
    }
    else {
      //at this point we know we have a valid content
      URLDescriptor descriptor = (URLDescriptor) stat.getObject();
      if (descriptor==null) {
      	println("error", "CooperationDomain.preform_withdraw_cd(): Can't interpret content URLDescriptor, returning NOTUNDERSTOOD: "+stat.getObject());
        PerformDescriptor ret = new PerformDescriptor(-1);
        ret.put(ML.LANGUAGE, "text");
        ret.put(ML.CONTENT, "Expected a valid URLDescritpor in the CONTENT field, but got: "+stat.getObject());
        return ret;
      }
      result = withdraw(descriptor);
      if (result.getStatusValue() < 0) {
        println("warning", "Bad result in CooperationDomain.handleWithdraw: " + result.toString());
      }
    }

    PerformDescriptor ret = new PerformDescriptor(result);
    ret.put(ML.LANGUAGE, result.getClass().toString());
    ret.put(ML.CONTENT, CASAUtil.serialize(result));
    
    out("CooperationDomain.perform_withdraw_cd");
    return ret;
  }

//  protected PerformDescriptor accept_withdraw_cd(MLMessage message) {
//    in("CooperationDomain.accept_withdraw_cd");
//    PerformDescriptor ret = perform_withdraw_cd (message);
//    out("CooperationDomain.accept_withdraw_cd");
//    return ret;
//  }
  
  /**
   * Override of method to cleanup after an {@link #AbstractProcess.exit()} has
   * been called, but before the message loop exits and the agent is about to
   * terminate.
   */
  @Override
  protected void pendingFinishRun() {
    in("CooperationDomain.pendingFinishRun");
    super.pendingFinishRun();
    
    //propose to cancel all membership monitors
    //TODO: rkyee: does anyone respond to this propose?
    for (URLDescriptor id: membershipSubscriptions.keySet()) {
    	sendMessage(ML.REFUSE,
    			        ML.SUBSCRIBE+"|"+ML.CHANGE_MEMBERSHIP,
    			        id,
    			        ML.CONTENT, "Cooperation Domain exiting",
    			        ML.CONVERSATION_ID, membershipSubscriptions.get(id));
    }
    
    //inform all members that the CD is terminating
    for (URLDescriptor member: members) {
    	sendMessage(ML.INFORM,
	                ML.EXIT,
	                member,
	                ML.LANGUAGE, "text",
	                ML.CONTENT, "Cooperation Domain exiting");
    }
    
    //wait for all members to terminate
    for (int i=10; i>0 && !members.isEmpty(); i--) {
      try {
        sleep(1000);
      }
      catch (InterruptedException ex) {}
    }
    if (!members.isEmpty()) println("error","Cooperation Domain exiting with "+ members.size()+ " members not withdrawn");
    out("CooperationDomain.pendingFinishRun");
  }

  /**
   * Removes the specified agent from the list of current members and notifies
   * any agents that are observing membership that a member has withdrawn from
   * the cooperation domain.
   *
   * @param member The agent (presumably a member) who is withdrawing from the
   * cooperation domain.
   * @return The <code>Status</code> of the withdrawl:
   * <li>0 if it was successful with no warnings or errors, or</li>
   * <li>1 if the agent was not already a member.</li>
   */
  @Override
	public Status withdraw(URLDescriptor member) {
  	assert member!=null;
  	
    boolean wasMember = false;

    wasMember = members.remove(member);

		if (kBase!=null) {
			//kBase.assertFormula(SL.formula("(not (CDmember "+getURL().toStringAgentIdentifier(true) +" "+member.toStringAgentIdentifier(false)+"))"));
			kBase.retractFormula(SL.formula("(CDmember "+getURL().toStringAgentIdentifier(true) +" "+member.toStringAgentIdentifier(false)+")"));
		}

    if (wasMember) {
      notifyObservers(ML.EVENT_WITHDRAW_CD, member);
      
//      if (members.size () == 0) {
//        // no one is registered, shut down
//        println ("warning", "All members have withdrawn, shutting down.");
//        exit ();
//        //notifyObservers(ObservableState.STATE_KILL_CD_FRAME);
//      }
      return new Status(0);
    }
    return new Status(1, "Member not found: " + member.toString(getURL()));
  }

  public PerformDescriptor perform_get_members(MLMessage message) {
    in("CooperationDomain.perform_get_members");
    PerformDescriptor status = new PerformDescriptor();

    //Do we really need all this? dsb
    //check the message
//    String[] m = {
//        ML.PERFORMATIVE, ML.AGREE,
//        ML.ACT, ML.GET_MEMBERS};
//    StatusObject<Object> stat = verifyMessage(message, m);
//    if (stat.getStatus() != 0) { //message isn't right -- might be shortcutted
//	    m = new String[]{
//	        ML.PERFORMATIVE, ML.REQUEST,
//	        ML.ACT, ML.GET_MEMBERS};
//	    stat = verifyMessage(message, m);
//    }
//
//    if (stat.getStatus() != 0) { //message isn't right
//      if (stat.getStatus() > 0) {
//        stat.setStatus( -stat.getStatus());
//      }
//      status.put(ML.PERFORMATIVE, ML.FAILURE);
//      status.put(ML.LANGUAGE, Status.class.getName());
//      status.put(ML.CONTENT, stat.toString());
//      status.setStatus(stat);
//    }
//    else {
      //at this point we know we have a valid content
      Vector<?> result = getMembers();
      result.toArray();
      
      status.put(ML.LANGUAGE, "casa.*");
      status.put(ML.CONTENT, CASAUtil.serialize(getMembers().toArray()));
//      status.setStatus(new StatusObject<String>(0, null, CASAUtil.serialize(result)));
//      status.setStatus(new StatusObject<Vector<?>>(0, result));
//      status.put(ML.CONTENT, CASAUtil.serialize(result)); //Why commented? dsb
//    }

    out("CooperationDomain.perform_get_members");
    return status;
  }

  /**
	 * Retreives a membership list for the cooperation domain.  Returns a Vector of <code>URLDescriptor</code>s that are members of the cooperation domain.
	 * @return  A <code>Vector</code> of <code>URLDescriptor</code>s (encapsulated  in a <code>StatusURLDescriptorList</code>) that are members of the  cooperation domain.  The status will be:  <li>0 indicating the operation was successful.</li>
	 */
  @Override
	public Vector<URLDescriptor> getMembers() {
    in("CooperationDomain.getMembers");
    Vector<URLDescriptor> v = new Vector<URLDescriptor>();
    for (URLDescriptor url: members) {
      v.add(url);
    }
    out("CooperationDomain.getMembers");
    return v;
  }
  

	public PerformDescriptor evaluate_membership_change(MLMessage message) {
		in("CooperationDomain.evaluate_membership_change");
		PerformDescriptor result = new PerformDescriptor();

		String conversationID = message.getParameter(ML.CONVERSATION_ID);
		if (conversationID == null) {
			conversationID = getUniqueRequestID();
			result.put(ML.CONVERSATION_ID, conversationID);
		}
		try {
			URLDescriptor requester = message.getFrom();
//			membershipSubscriptions.add(requester);
			membershipSubscriptions.put(requester, conversationID);
			addObserver(requester, ML.EVENT_JOIN_CD, ML.EVENT_WITHDRAW_CD);
		} catch (URLDescriptorException e) {
			String warning = "CooperationDomain.evaluate_membership_change: Bad SENDER or FROM field in message";
			result.setStatus(new Status(-124, warning));
			println("error", warning);
		}
		out("CooperationDomain.evaluate_membership_change");
		return result;
	}

	protected PerformDescriptor release_membership_change(MLMessage message) {
		in("CooperationDomain.release_membership_change");
		PerformDescriptor result = new PerformDescriptor();
		
		URLDescriptor sender;
		try {
			sender = message.getSender();
		} catch (URLDescriptorException e) {
			sender = null;
			e.printStackTrace();
		}

		membershipSubscriptions.remove(sender);

		out("CooperationDomain.release_membership_change");
		return result;
	}

	protected PerformDescriptor conclude_membership_change(MLMessage message) {
		in("CooperationDomain.conclude_membership_change");
		PerformDescriptor result = new PerformDescriptor();

		String conversationID = message.getParameter(ML.CONVERSATION_ID);
		if (conversationID != null)
			membershipSubscriptions.remove(conversationID);
		
		out("CooperationDomain.conclude_membership_change");
		return result;
	}

  /**
   * Responds to a observe.messages subscribe message by setting up
   * (or cancelling) notify messages whenever a message is sent within the
   * cooperation domain.  The handled messages are of the format:
   * <table border="1" bgcolor="gold" cellpadding="3">
   * <tr><td><a href="doc-files/performatives.gif">performative</a> </td><td>subscribe</td></tr>
   * <tr><td>{@link TransientAgent#makeDefaultActs() act}          </td><td>observe.messages</td></tr>
   * <tr><td>reply-by      </td><td>0</td></tr>
   * <tr><td>sense        </td><td>positive | negative</td></tr>
   * <tr><td>language     </td><td><a href="doc-files/contentLanguages.html#casa.agentCom.URLDescriptor">casa.agentCom.URLDescriptor</a></td></tr>
   * <tr><td>content      </td><td><em>a {@link URLDescriptor} object</em></td></tr>
   * </table>
   * The nofify messages will be of the format:
   * <table border="1" bgcolor="orange" cellpadding="3">
   * <tr><td><a href="doc-files/performatives.gif">performative</a> </td><td>notify</td></tr>
   * <tr><td>{@link TransientAgent#makeDefaultActs() act}          </td><td>message.forwarded</td></tr>
   * <tr><td>language     </td><td><a href="doc-files/contentLanguages.html#casa.agentCom.URLDescriptor">casa.agentCom.URLDescriptor</a></td></tr>
   * <tr><td>content      </td><td><em>a {@link URLDescriptor} object</em></td></tr>
   * </table>
   * @param message an incoming observe.membership subscribe message
   * @return a {@link Status} object with a value of 0
   * @todo Change this to be like membership change.
   */
  @Deprecated
  protected PerformDescriptor perform_observe_messages(MLMessage message) {
    in("CooperationDomain.perform_observe_messages");
    Status result = null;

    //check the message
    String[] m = {
//        ML.PERFORMATIVE, ML.SUBSCRIBE,
        ML.ACT, ML.OBSERVE_MESSAGES,
        ML.SENSE, null,
        ML.LANGUAGE, URLDescriptor.class.getName(),
        ML.CONTENT, null};
    StatusObject<Object> stat = verifyMessage(message, m);

    if (stat.getStatusValue() != 0) { //message isn't right
      result = new Status (-1, "Malformed observe.messages subscribe.");
    } else {
      //at this point we know we have a valid content
      String replyWith = message.getParameter(ML.REPLY_WITH);
      String sense = message.getParameter(ML.SENSE);

      URLDescriptor member = (URLDescriptor) stat.getObject();

      observeMessages(member, replyWith, sense.equals(ML.POSITIVE));
    }

    out("CooperationDomain.perform_observe_messages");
    return new PerformDescriptor (result == null ? new Status(0) : result);
  }
  
  /**
   * Adds or removes the given agent to/from the message notification list.
   *
   * @param agent The agent (presumably not a member) who is requesting to be
   * added or removed from the list of agents that would like to be notified of
   * every message forwarded by the cooperation domain.
   * @param replyWith This is the REPLY_WITH parameter of the original request
   * that should be used in all of the notifications in reply to that message.
   * This may be <code>null</code> if addIfTrue is <code>false</code>.
   * @param addIfTrue This should be <code>true</code> if the agent is would
   * like to be added to the list; <code>false</code> otherwise.
   * @return The <code>Status</code> of the operation:
   * <li>0 if it was successful with no warnings or errors, or</li>
   * <li>1 if the agent was not already a member.</li>
   */
  public Status observeMessages(URLDescriptor agent, String replyWith,
                                  boolean addIfTrue) {
    in("CooperationDomain.observeMessages");
    if (addIfTrue) {
      messageObservers.put(agent, replyWith);
    }
    else {
      messageObservers.remove(agent);
    }

    out("CooperationDomain.observeMessages");
    return new Status(0);
  }

  /**
   * Responds to a get.history request using {@link #getHistory(URLDescriptor)}
   * to reply.  It handles request messages of the form:
   * <table border="1" bgcolor="gold" cellpadding="3">
   * <tr><td><a href="doc-files/performatives.gif">performative</a> </td><td>request</td></tr>
   * <tr><td>{@link TransientAgent#makeDefaultActs() act}          </td><td>get.history</td></tr>
   * </table>
   * It replies with a message of the format:
   * <table border="1" bgcolor="orange" cellpadding="3">
   * <tr bgcolor="darkorange"><th></th><th>success or warning</th><th>failure</th></tr>
   * <tr><td><a href="doc-files/performatives.gif">performative</a> </td><td>reply</td><td>failure</td></tr>
   * <tr><td>{@link TransientAgent#makeDefaultActs() act}          </td><td>get.history</td><td>get.history</td></tr>
   * <tr><td>language     </td><td><a href="doc-files/contentLanguages.html#StatusMLMessageList">casa.StatusMLMessageList</a></td><td>text</td></tr>
   * <tr><td>content      </td><td><em>a {@link StatusMLMessageList} object</em></td><td><em>explanitory text</em></td></tr>
   * </table>
   * @param message the incoming get.history request message
   * @return the Status of the sendMessage used to send the reply or a Status of value -1 if the sendMessage thows and exception.
   */
  protected PerformDescriptor perform_get_history(MLMessage message) {
    in("CooperationDomain.respondToGetHistory");
    PerformDescriptor status = new PerformDescriptor();

    URLDescriptor senderURL = null;
    try {
        senderURL = message.getFrom ();
    	
    	StatusMLMessageList result = getHistory(senderURL);
    	
    	status.setStatus(result);
        status.put(ML.LANGUAGE, StatusMLMessageList.class.getName());
        status.put(ML.CONTENT, result.toString());
    }
    catch (URLDescriptorException e) {
    	println("warning", "Exception in CooperationDomain.notifyWithdraw", e);
    	status.put(ML.PERFORMATIVE, ML.FAILURE);
    	status.put(ML.LANGUAGE, String.class.getName());
    	status.put(ML.CONTENT,
    			"Could not interpret URL in content field: " +
    			e.toString());
    }

    out("CooperationDomain.respondToGetHistory");
    return status;
  }

  /**
   * Retreives the history for a given agent in the cooperation domain.
   * Returns a list of <code>MLMessage</code>s (encapsulated in a
   * <code>StatusMLMessageList</code>) that have been sent within the
   * cooperation domain to the given agent or to all agents.
   *
   * @param member The member for whom we are retreiving the history.
   * @return A <code>Vector</code> of <code>MLMessage</code>s (encapsulated
   * in a <code>StatusMLMessageList</code>) that have been sent within the
   * cooperation domain to the given agent or to all agents.  The status will
   * be:
   * <li>0 if the operation was successful, or</li>
   * <li>1 if there were errors loading the history, or parsing the
   * messages.</li>
   */
  @Override
	public StatusMLMessageList getHistory(URLDescriptor member) {
    in("CooperationDomain.getHistory");
    //notifyObservers(new casa.State(ObservableEvent.STATE_GET_HISTORY_CD));
    notifyObservers(ML.EVENT_GET_HISTORY_CD, null);

    StatusMLMessageList statusList = getEntireHistory();
    List<MLMessage> tempVector = statusList.getMessages();
    if (tempVector == null) {
      return new StatusMLMessageList(statusList.getStatusValue(),
                                     statusList.getExplanation(), null);
    }

    List<MLMessage> agentVector = new ArrayList<MLMessage>();
    MLMessage message;

    if (tempVector != null) {
      for (Iterator<?> i = tempVector.iterator(); i.hasNext(); ) {
        message = (MLMessage) i.next();

        // for each message
        String to = message.getParameter(ML.RECEIVER);
        if (to == null || to.equals("") || to.equals("*")) {
          // message is to everyone
          // TODO Fix me!
          if (message.getParameter(ML.REPLY_TO) == null) {
            message.setParameter(ML.REPLY_TO, message.getParameter(ML.SENDER));
          }
          message.setParameter(ML.SENDER, this.getURL().toString(member));
          message.setParameter(ML.RECEIVER, member.toString(getURL()));
          message.setParameter(ML.RECIPIENTS, "*");

          agentVector.add(message);
        }
        else {
          // message is to a specific set of agents, are we one?
          TokenParser parser = new TokenParser(to);
          URLDescriptor tempAgent = null;

          boolean directedSpeach = true;
          if (parser.getNextToken().equals("+")) {
            directedSpeach = true;
          }
          else {
            directedSpeach = false;
            parser.putback();
          }

          try {
            tempAgent = URLDescriptor.fromString(parser);
          }
          catch (URLDescriptorException ex1) {
          }

          while (tempAgent != null) {
            if (tempAgent.equals(member)) {
              if (message.getParameter(ML.REPLY_TO) == null) {
                message.setParameter(ML.REPLY_TO, message.getParameter(ML.SENDER));
              }
              message.setParameter(ML.SENDER, this.getURL().toString(member));
              /** @todo redo to with guy in front... */
              message.setParameter(ML.RECEIVER, member.toString(getURL()));

              agentVector.add(message);

              directedSpeach = false;
              break;
            }

            tempAgent = null;
            try {
              tempAgent = URLDescriptor.fromString(parser);
            }
            catch (URLDescriptorException ex1) {
            }
          }

          if (directedSpeach) {
            if (message.getParameter(ML.REPLY_TO) == null) {
              message.setParameter(ML.REPLY_TO, message.getParameter(ML.SENDER));
            }
            message.setParameter(ML.SENDER, this.getURL().toString(member));
            message.setParameter(ML.RECEIVER, member.toString(this.getURL()));

            agentVector.add(message);
          }
        }
      }
    }

    if (statusList.getStatusValue() != 0) {
      out("CooperationDomain.getHistory");
      return new StatusMLMessageList(statusList.getStatusValue(),
                                     statusList.getExplanation(),
                                     agentVector);
    }
    else {
      out("CooperationDomain.getHistory");
      return new StatusMLMessageList(0, agentVector);
    }
  }

  /**
   * Retreives the entire history for the cooperation domain.  Returns a
   * list of <code>MLMessage</code>s (encapsulated in a
   * <code>StatusMLMessageList</code>) that have been sent within the
   * cooperation domain.
   *
   * @return A <code>Vector</code> of <code>MLMessage</code>s (encapsulated
   * in a <code>StatusMLMessageList</code>) that have been sent within the
   * cooperation domain.  The status will be:
   * <li>0 if the operation was successful, or</li>
   * <li>1 if there were errors loading the history.</li>
   */
  @Override
	public StatusMLMessageList getEntireHistory() {
    in("CooperationDomain.getEntireHistory");
    StatusString history = getDataObject(HISTORY_NODE);

    if (history.getStatusValue() != 0) {
      return new StatusMLMessageList(1, "Unable to retrieve history", null);
    }
    TokenParser historyParser = new TokenParser(history.getData());

    List<MLMessage> tempVector = new ArrayList<MLMessage>();
    String tempString;
    MLMessage tempMessage;

    for (tempString = historyParser.getNextToken(); tempString != null;
         tempString = historyParser.getNextToken()) {

      try {
        tempMessage = MLMessage.getNewMLMessage();
        tempMessage.fromStringLocal(tempString);
        tempVector.add(tempMessage);
      }
      catch (Exception ex1) {
        try {
          tempMessage = MLMessage.getNewMLMessage();
          tempMessage.fromStringLocal(tempString);
          tempVector.add(tempMessage);
        }
        catch (Exception ex2) {
          out("CooperationDomain.getEntireHistory");
          return new StatusMLMessageList(1, "Cannot parse current MLMessage",
                                         tempVector);
        }
      }
    }

    out("CooperationDomain.getEntireHistory");
    return new StatusMLMessageList(0, tempVector);
  }

  /**
   * Responds to an incoming put.data request message by sending a reply
   * giving the status of the operation.  The handled message if of the form:
   * <table border="1" bgcolor="gold" cellpadding="3">
   * <tr><td><a href="doc-files/performatives.gif">performative</a> </td><td>request</td></tr>
   * <tr><td>{@link TransientAgent#makeDefaultActs() act}          </td><td>put.data</td></tr>
   * <tr><td>language     </td><td><a href="doc-files/contentLanguages.html#casa.DataStorageDescriptor">casa.DataStorageDescriptor</a></td></tr>
   * <tr><td>content      </td><td><em>a {@link DataStorageDescriptor} object</em></td></tr>
   * </table>
   * The reply message is of the form:
   * <table border="1" bgcolor="orange" cellpadding="3">
   * <tr bgcolor="darkorange"><th></th><th>success or warning</th><th>failure</th></tr>
   * <tr><td><a href="doc-files/performatives.gif">performative</a> </td><td>reply              </td><td>failure | refuse</td></tr>
   * <tr><td>{@link TransientAgent#makeDefaultActs() act}          </td><td>put.data           </td><td>put.data</td></tr>
   * <tr><td>language     </td><td><a href="doc-files/contentLanguages.html#casa.Status">casa.Status</a>  </td><td><a href="doc-files/contentLanguages.html#casa.Status">casa.Status</a></td></tr>
   * <tr><td>content      </td><td><em>a {@link Status} object</em></td><td><em>a {@link Status} object</em></td></tr>
   * </table>
   * The <em>failure</em>message is sent if the message is malformed; the <em>refuse</em>
   * message is sent if the agent is trying to write to a reserved data node.
   * @param message an incoming put.data request message
   * @return the status of the {@link AbstractProcess#sendMessage(MLMessage)} call for the reply message
   */
  protected PerformDescriptor perform_put_data(MLMessage message) {
    in("CooperationDomain.respondToPutData");
    PerformDescriptor status = new PerformDescriptor();

    //check the message
    String[] m = {
        ML.LANGUAGE, DataStorageDescriptor.class.getName(),
        ML.CONTENT, null};
    StatusObject<Object> stat = verifyMessage(message, m);

    if (stat.getStatusValue() != 0) { //message isn't right
      if (stat.getStatusValue() > 0) {
        stat.setStatusValue( -stat.getStatusValue());
      }

      // negative status will result in failure message
      status.setStatus(stat);
    }
    else {
      //at this point we know we have a valid content
      Status result;
      DataStorageDescriptor dsd = (DataStorageDescriptor) stat.getObject();
      if (dsd.getDataObjectName().equals(HISTORY_NODE)) {
        result = new Status( -1,
                            "The specified data object name cannot be written to, it is reserved: " +
                            HISTORY_NODE);
      }
      else {
        result = putDataObject(dsd);
        //notifyObservers(new casa.State(ObservableEvent.STATE_PUT_DATA_CD));
        notifyObservers(ML.EVENT_PUT_DATA_CD, null);
      }

      status.setStatus(result);
      status.put(ML.LANGUAGE, Status.class.getName());
      status.put(ML.CONTENT, result.toString());
    }

    out("CooperationDomain.respondToPutData");
    return status;
  }

  /**
   * Responds to an incoming get.data request message by sending a reply
   * giving the status of the operation and the requested data.  The handled
   * message if of the form:
   * <table border="1" bgcolor="gold" cellpadding="3">
   * <tr><td><a href="doc-files/performatives.gif">performative</a> </td><td>request</td></tr>
   * <tr><td>{@link TransientAgent#makeDefaultActs() act}          </td><td>get.data</td></tr>
   * <tr><td>language     </td><td><a href="doc-files/contentLanguages.html#identifier">identifier</a></td></tr>
   * <tr><td>content      </td><td><em>named-data name</em></td></tr>
   * </table>
   * It sends a mesage of the format:
   * <table border="1" bgcolor="orange" cellpadding="3">
   * <tr bgcolor="darkorange"><th></th><th>success or warning</th><th>failure</th></tr>
   * <tr><td><a href="doc-files/performatives.gif">performative</a> </td><td>reply              </td><td>failure</td></tr>
   * <tr><td>{@link TransientAgent#makeDefaultActs() act}          </td><td>get.data           </td><td>get.data</td></tr>
   * <tr><td>language     </td><td><a href="doc-files/contentLanguages.html#casa.StatusString">casa.StatusString</a>  </td><td><a href="doc-files/contentLanguages.html#casa.Status">casa.Status</a></td></tr>
   * <tr><td>content      </td><td><em>a {@link StatusString} object</em></td><td><em>a {@link Status} object</em></td></tr>
   * </table>
   * The <em>failure</em>message is sent if the message is malformed.
   * @param message an incoming get.data request message
   * @return the status of the {@link AbstractProcess#sendMessage(MLMessage)} call for the reply message
   */
  protected PerformDescriptor perform_get_data(MLMessage message) {
    in("CooperationDomain.respondToGetData");
    PerformDescriptor status = new PerformDescriptor();

    //check the message
    String[] m = {
        ML.LANGUAGE, "identifier",
        ML.CONTENT, null};
    StatusObject<Object> stat = verifyMessage(message, m);

    if (stat.getStatusValue() != 0) { //message isn't right
      if (stat.getStatusValue() > 0) {
        stat.setStatusValue( -stat.getStatusValue());
      }
      
      // negative status will result in failure message
      status.setStatus(stat);
    }
    else {
      //at this point we know we have a valid content
      String dataObjectName = message.getParameter(ML.CONTENT).trim();
      StatusString result = getDataObject(dataObjectName);
      //notifyObservers(new casa.State(ObservableEvent.STATE_GET_DATA_CD));
      notifyObservers(ML.EVENT_GET_DATA_CD, null);

      status.setStatus(result);
      status.put(ML.LANGUAGE, StatusString.class.getName());
      status.put(ML.CONTENT, result.toString());
    }

    out("CooperationDomain.respondToGetData");
    return status;
  }

  /**
   * The interval that determines how often this agent will attempt to ping its
   * members.
   *
   * @see messageBufferLoopPeriodic()
   */
  final static private long PING_INTERVAL = 300000; // 5 minutes

  /**
   * The ping timeout value used when this agent's members are pinged.
   *
   * @see messageBufferLoopPeriodic()
   */
  final static private long PING_WAIT_TIME = 15000;

  /**
   * The number of times a member agent's ping must timeout before it is
   * withdrawn from this cooperation domain. This is public because it
   * is also used by CooperationDomainListModel for fading visual effects.
   *
   * @see messageBufferLoopPeriodic()
   */
  final static public int PING_CHANCES = 3;

  /**
   * The next moment in time that all of our members will be pinged.  This is
   * initially set to the current time plus {@link #PING_INTERVAL}.
   *
   * @see messageBufferLoopPeriodic()
   */
  private long pingTime = (System.currentTimeMillis() + PING_INTERVAL);


  /**
   * This method is called when a ping reply message is received in response to a
   * previously sent ping request message.  We override this method to clear
   * the respoinder's marks (if the ping'ee doesn't respond so many times in a row,
   * it will be removed from the list of members.
   *
   * @param msg the incomming reply message
   * @return    a Status object as described above. 0 if the message was not an
   *            error or malformed, or a negative Status according to
   *            {@link verifyMessage(MLMessage,String[])}
   */
   @Override
  public PerformDescriptor release_ping(MLMessage msg) {
    in("CooperationDomain.DischargePing");
    super.release_ping(msg);
    String responderString = msg.getParameter(ML.SENDER);
    URLDescriptor responder = null;
    try {
      responder = URLDescriptor.make(responderString);
      int i = members.indexOf(responder);
      if (i >= 0) {
         (members.elementAt(i)).resetMark();
      }
    }
    catch (URLDescriptorException ex) {
      if (isLoggingTag("warning")) println("warning",
              "CooperationDomain.handleReply_ping: Could not interpret '" +
              responderString + "'", ex);
    }
    out("CooperationDomain.DischargePing");
    return new PerformDescriptor();
  }

  /**
   * Retreives the creator (username) of the cooperation domain.
   * This allows the security system (in this case the authorization) to
   * retrieve the user that has "unlimited" access to the cooperation domain.
   * Returns a <code>String</code> value representing the username of the
   * creator of the cooperation domain.  This person/agent has full access
   * rights in the cooperation domain.   This means that this person/agent can
   * modify other users' access rights.  That is, limit or upgrade the level of
   * cooperation and collaboration that a person/agent has in the specific
   * cooperation domain.
   *
   * @returns a <code>String</code> value representing the username of the
   * creator of the cooperation domain.
   */
  public String getCDCreator() {
    in("CooperationDomain.getCDCreator");
    String cdCreator = new String();
    try {
      cdCreator = getStringProperty("CD_Creator");
    }
    catch (PropertyException ex) {
    }

    out("CooperationDomain.getCDCreator");
    return cdCreator;
  }

  /**
   * Create the default internal Frame (usually) with tabs for this agent type. 
   * @param agent the owner agent
   * @param title the title of the window
   * @param aFrame the owner frame in which this window is to be embedded
   * @return the frame
   */
  @Override
  protected casa.ui.TransientAgentInternalFrame makeDefaultInternalFrame(TransientAgent agent,
      String title, Container aFrame) {
  	return new CooperationDomainWindow ((CooperationDomain)agent, title, aFrame);
  }
 
}