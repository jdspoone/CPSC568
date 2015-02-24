
package casa.testAgents;

import casa.Agent;
import casa.CasaPersistent;
import casa.CooperationDomain;
import casa.ML;
import casa.MLMessage;
import casa.PerformDescriptor;
import casa.Status;
import casa.StatusMLMessageList;
import casa.TransientAgent;
import casa.abcl.ParamsMap;
import casa.agentCom.URLDescriptor;
import casa.exceptions.IPSocketException;
import casa.exceptions.URLDescriptorException;
import casa.interfaces.ChatAgentInterface;
import casa.ui.AgentUI;
import casa.ui.ChatAgentWindow;
import casa.ui.ChatTextInterface;
import casa.util.PropertyException;

import java.awt.Container;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * <code>ChatAgent</code> is a simple subclass of <code>Agent</code> designed to be used with a GUI as a chat or message system.  Each user has their own <code>ChatAgent</code> and each cooperation domain is treated as a separate "chat room".  A history of messages is stored, each message represented by a <code>ChatMessage</code>.  The public functions of this class are outlined in the interface <code>ChatAgentInterface</code>. <br> In addition to the messages defined in  {@link Agent} , this agent can originate the following messages types: <table border="1" bgcolor="gold" cellpadding="3"> <tr><th> <a href="doc-files/performatives.gif">performative</a> </th> <th>  {@link TransientAgent#makeDefaultActs()  act}  </th> <th> <em> see </em> </th> <th> <em> reply handler </em></th> </tr> <tr><td>  </td> <td>  </td> <td>  </td> <td>  </td> </tr> <tr><td>      </td> <td>      </td> <td>      </td> <td>  -   </td> </tr> </table> <br> In addition to the messages defined in  {@link Agent} , this agent responds to the following messages types: <table border="1" bgcolor="orange" cellpadding="3"> <tr><th> <a href="doc-files/performatives.gif">performative</a> </th> <th>  {@link TransientAgent#makeDefaultActs()  act}  </th> <th> <em> see </em> </th> </tr> <tr><td>  </td> <td>  </td> <td>  </td> </tr> </table> <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @version 0.9
 * @see ChatMessage
 * @see ChatAgentInterface
 * @author  Jason Heard,<a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */

public class ChatAgent extends Agent implements ChatAgentInterface {
  /**
   * A <code>Hashtable</code> linking the <code>URLDescriptor</code> of a
   * cooperation domain to a <code>Vector</code> of the message history
   * (<code>ChatMessage</code>s) of that cooperation domain.
   *
   * @see ChatMessage
   */
  private Hashtable<URLDescriptor,Vector<ChatMessage>> cooperationDomainHistories = new Hashtable<URLDescriptor,Vector<ChatMessage>>();

  public void putCooperationDomainHistory(URLDescriptor url, Vector<ChatMessage> msg){
  	this.cooperationDomainHistories.put(url, msg);
  }
  public void removeCooperationDomainHistory(URLDescriptor url){
  	this.cooperationDomainHistories.remove(url);
  }
//  public void removeJoinedCooperationDomain(URLDescriptor url, ChatMessage msg){
//  	this.cooperationDomainHistories.removeElement(url);
//  }
  
  /**
	 */
  protected URLDescriptor currentCD=null;

  /**
   * A boolean value that determines if the agent will retrieve the history of
   * a cooperation domain whenever it joins it.
   */
  @CasaPersistent
  private boolean obtainHistory = false;
  
  public boolean canObtainHistory() {
  	return this.obtainHistory;
  }

  private boolean setObtainHistory = false;

//  /**
//   * set to true when the ChatAgent constructor completes.
//   */
//	@Deprecated
//  private boolean chatAgentConstructorComplete = false;

  //private ChatAgentWindow chatAgentUI;

  /**
   * Creates a new <code>ChatAgent</code> on the given port, with the given
   * name, registered with the LAC on the given port.  The value of
   * <code>obtainHistory</code> determines whether this agent will retrieve the
   * history of the cooperation domains it joins.
   *
   * @param port The port number to be used for communication with other
   * agents.  See AbstractProcess.
   * @param name The name of this Agent.
   * @param obtainHistory Determines whether this agent will retrieve the
   * history of whatever cooperation domains it joins.
   * @param lacPort The port number of the LAC that the agent should register
   * with.
   * @param persistent Determines whether this agent is persistent or not.
   * @throws IPSocketException If an Agent attempts to bind to an IPSocket
   * (port) that doesn't exist or is in use.
   */
  public ChatAgent (ParamsMap params, AgentUI ui) throws Exception {
    super (params, ui);

    in("ChatAgent.ChatAgent");

    if (!params.isDefaulted("OBTAINHISTORY")) {
      try {
        this.obtainHistory = (Boolean)params.getJavaObject("OBTAINHISTORY",Boolean.class);
        setObtainHistory = true;
      } catch (Exception e) {
        // this won't happen since this area is only reached if the variable is
        // defined.
      }
    }

//    addConversationSupported(ML.JOIN_CD, JoinCDChatAgentRequestComposite.class, true);
//    addConversationSupported(ML.WITHDRAW_CD, WithdrawCDChatAgentRequestComposite.class, true);
    
    out("ChatAgent.ChatAgent");
  }

//  /**
//   * Called by the constructor to initialize the run time command line interface.
//   * Subclasses should override this method, but be sure to call super.initRTCommandInterface()
//   * to include these definitions.<br>
//   * First calls super to do it's definitions, then defines the following commands:
//   * <li>broadcast: Broadcast a text message to other chat members</li>
//   */
//  @Override
//  protected void initRTCommandInterface() {
//    in("ChatAgent.initRTCommandInterface");
//    super.initRTCommandInterface();
//    try {
//      commandInterpreter.put(
//          ML.INFORM+" "+ML.CHAT_MESSAGE+" | message(type=string; required; help=\"A text message to broadcast to the other chat members\") " +
//                      "CD(type=string; help=\"The CD to broadcast the message (to the CD's members); defaults to the last one used in this command.\") " +
//                      "?(help=\"Broadcast a text message to other chat members\")",
//          new Command() {
//            @Override
//			public Status execute(String line, Map<String, String> params, AgentUI ui) {
//              Status status;
//              String b = (String)params.get("CD");
//              try {
//                URLDescriptor cd = b!=null?URLDescriptor.make(b):currentCD;
//                if (cd!=null) currentCD = cd;
//                if (currentCD==null) {
//                  return new Status(-7,"Please supply a CD on the command line; no current CD known");
//                }
//                status = doSendChatMessage(currentCD, (String) params.get("message"));
//              }
//              catch (URLDescriptorException ex) {
//                return new Status(-6,"Bad CD URL given: "+ex.toString());
//              }
//              return status;
//            }
//          });
//
//    }
//    catch (ParameterParserException ex) {
//      println("error","Unexepected exception when executing commandInterpreter.put()'s",ex);
//    }
//    out("ChatAgent.initRTCommandInterface");
//  }

	/**
   * Returns a default <em>AgentUI</em> as a window.<br>
   * Subclasses should override ChatAgent.makeDefaultTextInterface() to
   * change this behaviour in subclasses<br>
   * ChatAgent.makeDefaultTextInterface() may return null if no default interface
   * can be built or is not desired.<br>
   * @param args The command line arguments
   * @return some default interface the conforms to <em>AgentUI</em> interface.
   */
  @Override
  protected AgentUI makeDefaultTextInterface(String[] args) {
    return new ChatTextInterface(this, args);
  }

  /**
	 * Sets whether this <code>ChatAgent</code> should obtain the history of any cooperation domain it joins.
	 * @param persistent  Whether this <code>Agent</code>'s data should be  persistent between instances.
	 */
  public synchronized void setObtainHistory (boolean obtainHistory) {
    in("ChatAgent.setObtainHistory");
    this.obtainHistory = obtainHistory;
    setBooleanProperty ("obtainHistory", obtainHistory);
    out("ChatAgent.setObtainHistory");
  }

  /**
   * Returns whether this <code>ChatAgent</code> will obtain the history of any
   * cooperation domain it joins.
   *
   * @return <code>true</code> if this <code>ChatAgent</code> will obtain the
   * history of any cooperation domain it joins; <code>false</code> otherwise.
   */
  public boolean willObtainHistory () {
    return obtainHistory;
  }

  /**
   * Retrieves the message history for the given cooperation domain.
   *
   * @param cd The cooperation domain for which we are retrieving the history.
   * @return A <code>Vector</code> of <code>ChatMessage</code>s that have been
   * received from the given cooperation domain.
   */
  public Vector<ChatMessage> getHistory (URLDescriptor cd) {
    return cooperationDomainHistories.get (cd);
  }

  /**
   * Handler for a message with the performative <em>inform</em>, and the act
   * <em>chat.message</em>.  Parses the the incoming <em>message</em>
   * and then calls chatMessage() if the incoming message is appropriately
   * formed.  Does not send a reply message.
   *
   * @param message The incoming message to be handled.
   * @return The <code>Status</code> of 0, indicating that the message was
   * handled.
   */
  public PerformDescriptor accept_chat_message (MLMessage message) {
    in("ChatAgent.handleChatMessage");

    // Read the relevant info from the message
    String language = message.getParameter (ML.LANGUAGE);
    String sender = message.getParameter (ML.SENDER);
    String cdString = message.getParameter (ML.CD);

    // Interpret the fields of the message
    if ((language == null) || (!language.equals (String.class.getName ()))) {
      if (isLoggingTag("warning")) println("warning", "Language does not match: " + language +
                                           ", attempting to decipher anyways.");
    }

    URLDescriptor cd = null;
    try {
      cd = URLDescriptor.make (cdString==null?sender:cdString);

      chatMessage (cd, message);
    } catch (URLDescriptorException ex) {
      if (isLoggingTag("warning")) println("warning", "Could not retrieve originating CD from message: " + sender);
    }

    out("ChatAgent.handleChatMessage");
    return new PerformDescriptor ();
  }

  /**
   * Performs operations necessary when a chat message has been received.  The
   * message is stored in the history corresponding to the cooperation domain
   * that sent the message.  The observers are notified with a
   * ObservableState.STATE_CHAT_MESSAGE_RECIEVED message.
   *
   * @param cd The cooperation domain that the message was sent through.
   * @param message The chat message that was received.
   */
  public void chatMessage (URLDescriptor cd, MLMessage message) {
    in("ChatAgent.chatMessage");
    Vector<ChatMessage> tempHistory = cooperationDomainHistories.get (cd);

    if (tempHistory == null) {
      tempHistory = new Vector<ChatMessage> ();
      cooperationDomainHistories.put (cd, tempHistory);
    }

    tempHistory.add (new ChatMessage (message, cd));
    //notifyObservers (new casa.State(ObservableEvent.STATE_CHAT_MESSAGE_RECIEVED));
    notifyObservers (ML.EVENT_CHAT_MESSAGE_RECEIVED, null);
    out("ChatAgent.chatMessage");
  }


  /**
   * Sends the given chat message through the given cooperation domain to the
   * given agent.
   *
   * @param cd The cooperation domain that will forward the message.
   * @param to The agent that will receive the message.
   * @param whisper Constructs a <em>whisper</em> message if <code>true</code>;
   *          constructs a <em>directed</em> message if <code>false</code>.
   * @param message The message to send to the given agent.
   * @return The <code>Status</code> of the message sending operation, 0 for
   * success; negative otherwise.
   * @throws Exception If an exception is thrown while sending the message.
   */
  public Status doSendChatMessage (URLDescriptor cd, URLDescriptor to,
                                   boolean whisper, String message) {
    in("ChatAgent.doSendChatMessage");
    
    MLMessage msg = getNewMessage (ML.NOTIFY, ML.CHAT_MESSAGE, cd);
    msg.setParameter (ML.LANGUAGE, String.class.getName ());
    msg.setParameter (ML.CONTENT, message);
    
    MLMessage holder = CooperationDomain.constructCDProxyMessage (msg, getURL (), cd, to, whisper);

    Status ret = sendMessage (holder);
    out("ChatAgent.doSendChatMessage");
    return ret;
  }

  /**
   * Sends the given chat message through the given cooperation domain to the
   * given agents.
   *
   * @param cd The cooperation domain that will forward the message.
   * @param to A <code>Vector</code> containing the <code>URLDescriptor</code>s
   * of the agents that will receive the message.
   * @param whisper Constructs a <em>whisper</em> message if <code>true</code>;
   *          constructs a <em>directed</em> message if <code>false</code>.
   * @param message The message to send to the given agents.
   * @return The <code>Status</code> of the message sending operation, 0 for
   * success; negative otherwise.
   */
  public Status doSendChatMessage (URLDescriptor cd, List<URLDescriptor> to,
                                   boolean whisper, String message) {
    in("ChatAgent.doSendChatMessage");

    MLMessage msg = getNewMessage (ML.NOTIFY, ML.CHAT_MESSAGE, cd);
    msg.setParameter (ML.LANGUAGE, String.class.getName ());
    msg.setParameter (ML.CONTENT, message);
    
    MLMessage holder = CooperationDomain.constructCDProxyMessage (msg, getURL (), cd, to, whisper);

    Status ret = sendMessage (holder);
    out("ChatAgent.doSendChatMessage");
    return ret;
  }

  /**
   * Sends the given chat message through the given cooperation domain to all
   * current members of that cooperation domain.
   *
   * @param cd The cooperation domain that will forward the message.
   * @param message The message to send to the given agent.
   * @return The <code>Status</code> of the message sending operation, 0 for
   * success; negative otherwise.
   * @throws Exception If an exception is thrown while sending the message.
   */
  public Status doSendChatMessage (URLDescriptor cd, String message) {
    in("ChatAgent.doSendChatMessage");

    MLMessage msg = getNewMessage (ML.NOTIFY, ML.CHAT_MESSAGE, cd);
    msg.setParameter (ML.LANGUAGE, String.class.getName ());
    msg.setParameter (ML.CONTENT, message);
    
    MLMessage holder = CooperationDomain.constructCDProxyMessage (msg, getURL (), cd);

    Status ret = sendMessage (holder);
    out("ChatAgent.doSendChatMessage");
    return ret;
  }

  /**
   * This method is called when a cd.withdraw reply message is received in
   * response to a previously sent cd.withdraw request message.  This function
   * extends the superclass version of the function, returning whatever the
   * superclass version returns. When it is called, the agent has successfully
   * withdrawn from a cooperation domain.  The history for the given
   * cooperation domain is then removed from cooperationDomainHistories.
   *
   * @param msg The incoming message to be handled.
   * @return The <code>Status</code> returned by the superclass function.
   */
  @Override
  public PerformDescriptor release_withdraw_cd(MLMessage msg) {
    in("ChatAgent.release_withdraw_cd");
    if (isA(msg.getParameter(ML.PERFORMATIVE),ML.REPLY)) {
//      String content = msg.getParameter (ML.CONTENT);
      URLDescriptor cd = null;
      try {
        cd = msg.getFrom ();

        cooperationDomainHistories.remove (cd);
        //notifyObservers (new casa.State(ObservableEvent.STATE_CHAT_MESSAGE_RECIEVED));
        notifyObservers (ML.EVENT_CHAT_MESSAGE_RECEIVED, null);
      } catch (URLDescriptorException ex) {
      }
    }

    PerformDescriptor ret = super.release_withdraw_cd (msg);
    out("ChatAgent.release_withdraw_cd");
    return ret;
  }

  /**
   * This method is called when a cd.join reply message is received in response
   * to a previously sent cd.join request message.  This function extends the
   * superclass version of the function, returning whatever the superclass
   * version returns.  When it is called, the agent has successfully joined a
   * cooperation domain.  This function requests that the cooperation domain
   * notify the agent of new members, requests the current members, and,
   * depending on the value of obtainHistory, retrieves the history for the
   * cooperation domain.
   *
   * @param msg The incoming message to be handled.
   * @return The <code>Status</code> returned by the superclass function.
   */
  @Override
  public PerformDescriptor release_join_cd(MLMessage msg) {
    in("ChatAgent.handleReply_joinCD");
    PerformDescriptor result = super.release_join_cd (msg);

    try {
      final URLDescriptor cd = msg.getFrom ();

      // get history
      if (obtainHistory) {
        defer (new Runnable () {
          public void run () {
            StatusMLMessageList history = doCDGetHistory_sync (cd);
            Vector<ChatMessage> historyVector = new Vector<ChatMessage> ();

            // sort through history, ignoring messages that aren't PERFORMATIVE=inform and ACT=chat.message
            MLMessage tempMessage;
            for (Iterator<?> i = history.getMessages ().iterator (); i.hasNext (); ) {
              tempMessage = (MLMessage) i.next ();
              if (ML.INFORM.equals (tempMessage.getParameter (ML.PERFORMATIVE)) &&
                  ML.CHAT_MESSAGE.equals (tempMessage.
                  getParameter (ML.ACT))) {

                historyVector.add (new ChatMessage (tempMessage, cd));
              }
            }

            cooperationDomainHistories.put (cd, historyVector);
            //notifyObservers (new casa.State (ObservableEvent.
                                        //STATE_CHAT_MESSAGE_RECIEVED));
            notifyObservers (ML.EVENT_CHAT_MESSAGE_RECEIVED, null);
          }
        });
      }
      else {
        cooperationDomainHistories.put (cd, new Vector<ChatMessage>());
      }

    } catch (URLDescriptorException ex) {}

    out("ChatAgent.dischargeJoinCD");
    return result;
  }

  /**
   * Overrides Agent.init().  During the constructor, the
   * Agent registers with the LAC, but has to wait for the LAC to reply before initializing
   * the file and properties.  This init() method is called at that time to
   * initialize the file and properties.<br>
   * This version of init() first calls super.init(), then tries to join CDs based
   * on the persistent property "joinedCDs".
   */
   @Override
	public void initializeAfterRegistered(boolean registered) {
    in("ChatAgent.init");
    super.initializeAfterRegistered(registered);

		//Load critical CD-related policy
    //
    //This really should go somewhere else... but where?
//		try {
//			readPolicyFile("join_cd" + File.separator + "ChatAgent" + File.separator + "JoinCDChatAgentPolicies.lisp");
//			readPolicyFile("withdraw_cd" + File.separator + "ChatAgent" + File.separator + "WithdrawCDChatAgentPolicies.lisp");
//		} catch (Exception e) {
//			println("error", getAgentName() + ": Cannot read new policy file: " + e.toString());
//		}
    
    if (isPersistent()) {
      if (setObtainHistory) setBooleanProperty ("obtainHistory", obtainHistory);
      else {
        try {
          obtainHistory = getBooleanProperty ("obtainHistory");
        } catch (PropertyException e) {}
      }

      String cds;
      try {
        cds = getStringProperty("joinedCDs");
      }
      catch (PropertyException ex) {
        return;
      }
      for (int i = 1; i < cds.length() - 1; i++) {
        String cd = null;
        try {
          cd = cds.substring(i, i = cds.indexOf('`', i + 1));
        }
        catch (Exception ex2) {
        }
        try {
          URLDescriptor cdurl = URLDescriptor.make(cd);
          Status tempStatus = doJoinCD(cdurl);
          if (tempStatus.getStatusValue() != 0) {
            if (isLoggingTag("warning")) println("warning", "ChatAgent.init: Error encountered attempting to re-join saved CD '" + cd +
                                                 "': " + tempStatus.toString());
          }
        }
        catch (Exception ex1) {
          if (isLoggingTag("warning")) println("warning", "ChatAgent.init: Can't re-join saved CD '" + cd +
                                               "': " + ex1.toString());
        }
      }
    }

    out("ChatAgent.init");
  }


  /**
   * Cleans up. Saves the CDs that this agent is joined to in the persistent
   * property 'joinedCDs', then calls super.pendingFinishRun().
   */
  @Override
  protected void pendingFinishRun() {
    in("ChatAgent.pendingFinishRun");
    Set<URLDescriptor> joinedCooperationDomains = getJoinedCooperationDomains();
    URLDescriptor tempCD = null;
    String cds = "`";
    for (Iterator<?> i = joinedCooperationDomains.iterator(); i.hasNext(); ) {
      try {
				tempCD = URLDescriptor.make((URLDescriptor)i.next());
			} catch (URLDescriptorException e) {
				println("error", "ChatAgent.pendingFinishRun()", e);
			}
      tempCD.unresolve();
      cds += tempCD.toString(getURL()) + "`";
    }
    setStringProperty("joinedCDs", cds);
    setBooleanProperty("obtainHistory",willObtainHistory());

    super.pendingFinishRun();
    out("ChatAgent.pendingFinishRun");
  }

  /**
   * Returns a default <em>AgentUI</em>.
   * @param args The command line arguments
   * @param userName The name of the user
   * @return some default interface the conforms to <em>AgentUI</em> interface.
   */
//  @Override
//  protected AgentUI makeDefaultGUI(String[] args, String userName) {
//    in("ChatAgent.makeDefaultInterface");
//    try {
//      Container frame = null;
//      if (LAC.LACinfo.desktop != null)
//        frame = new JInternalFrame ();
//      else
//        frame = new JFrame ();
//      chatAgentUI = new ChatAgentWindow (this, frame);
//      if (LAC.LACinfo.desktop != null)
//        LAC.LACinfo.desktop.addAgentWindow (chatAgentUI);
//      chatAgentUI.setSize (500, 500);
//      chatAgentUI.setName(this.getName());
//      chatAgentUI.setOpaque (true);
//      chatAgentUI.pack ();
//      chatAgentUI.show ();
//    } catch (Exception ex) {
//      println ("warning",
//          "ChatAgent.startUp: Unexpected exception trying to start the window",
//          ex);
//    }
//    out("ChatAgent.makeDefaultInterface");
//    return chatAgentUI;
//  }

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
  	return new ChatAgentWindow((ChatAgent)agent, aFrame);
  }


  /**
   * @param receiverURL
   * @param content
   * @return
   * @deprecated
   */
  @Deprecated
  public Status informAgent_GUIOperationRequest (URLDescriptor receiverURL, String content) {
    MLMessage message = getNewMessage (ML.INFORM,
                                       ML.GUI_ACTION_REQUEST,
                                       receiverURL);

    message.setParameter (ML.LANGUAGE, String.class.getName ());
    message.setParameter (ML.CONTENT, content);

    return sendMessage (message);
  }

  protected PerformDescriptor accept_time (MLMessage message) {
    try {
      String CDString = message.getParameter(ML.CD);
      nonMemberMessage(message,
                       URLDescriptor.make(CDString!=null?CDString:message.getParameter(ML.SENDER)));
    } catch (URLDescriptorException ex) {
      return new PerformDescriptor (new Status (Status.EXCEPTION_CAUGHT, "Could not interpret CD", ex));
    }
    return new PerformDescriptor ();
  }

  protected void nonMemberMessage (MLMessage message, URLDescriptor sender) {
    Vector<ChatMessage> h = cooperationDomainHistories.get(sender);
    if (h==null)
      println(null,"\n!!! message from "+sender.getPath()+" at "+sender.getHostString()+":"+message.getParameter(ML.CONTENT)+"!!!\n");
    else
      chatMessage(sender,message);
  }

//	/**
//   * Lisp operator: make-join-cd-chat-agent-request-conversation<br>
//   * 
//   * Instantiates a join_cd conversation
//   * 
//   * @author  <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
//   */
//  @SuppressWarnings("unused")
//  private static final CasaLispOperator MAKE_JOIN_CD_CHAT_AGENT_REQUEST_CONVERSATION =
//  	new CasaLispOperator("MAKE-JOIN-CD-CHAT-AGENT-REQUEST-CONVERSATION", "\"!Instantiates a client-side join_cd conversation.\""
//  			, TransientAgent.class)
//  {
//  	@Override
//  	public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui) {
//  		MLMessage msg = getMsgForThread();
//
//  		agent.println("info", agent.getAgentName() + ": trying to create JoinCDChatAgentRequestComposite conversation");
//  		JoinCDChatAgentRequestComposite conv = new JoinCDChatAgentRequestComposite(agent, msg.getConversationID());
//
//  		if (conv != null){ 
//  			return new StatusObject<JoinCDChatAgentRequestComposite>(0, conv);
//  		}
//		  return new Status(-1, "Could not create conversation in ChatAgent.");
//  	}
//  };

//	/**
//   * Lisp operator: make-withdraw-cd-chat-agent-request-conversation<br>
//   * 
//   * Instantiates a withdraw_cd conversation
//   * 
//   * @author  <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
//   */
//  @SuppressWarnings("unused")
//  private static final CasaLispOperator MAKE_WITHDRAW_CD_CHAT_AGENT_REQUEST_CONVERSATION =
//  	new CasaLispOperator("MAKE-WITHDRAW-CD-CHAT-AGENT-REQUEST-CONVERSATION", "\"!Instantiates a client-side withdraw_cd conversation.\""
//  			, TransientAgent.class)
//  {
//  	@Override
//  	public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui) {
//  		MLMessage msg = getMsgForThread();
//
//  		agent.println("info", agent.getAgentName() + ": trying to create WithdrawCDChatAgentRequestComposite conversation");
//  		WithdrawCDChatAgentRequestComposite conv = new WithdrawCDChatAgentRequestComposite(agent, msg.getConversationID());
//
//  		if (conv != null){ 
//  			return new StatusObject<WithdrawCDChatAgentRequestComposite>(0, conv);
//  		}
//		  return new Status(-1, "Could not create conversation in ChatAgent.");
//  	}
//  };
  
}
