package casa;

import casa.abcl.ParamsMap;
import casa.agentCom.URLDescriptor;
import casa.event.Event;
import casa.event.MessageEvent;
import casa.exceptions.IPSocketException;
import casa.exceptions.MLMessageFormatException;
import casa.ui.AgentUI;

/**
 * <code>CooperationDomainObserver</code> is a subclass of <code>Agent</code> with the sole purpose of observing all messages sent in a <code>CooperationDomain</code>, it only sends messages to its members after having recieved a corresponding message, so observing its recieved messages should be enough to observe its activity. <p> Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation. The Knowledge Science Group makes no representations about the suitability of this software for any purpose. It is provided "as is" without express or implied warranty. </p>
 * @see Agent
 * @author  Jason Heard
 * @version 0.9
 */
public class CooperationDomainObserver extends Agent {
	/**
	 * A <code>Vector</code> of all of the cooperation domains ( <code>URLDescriptor</code>s) that this agent is "watching". In other words, all of the cooperation domains that we have requested to receive all forwarded messages.
	 */
	private URLDescriptor watchedCooperationDomain;

	/**
	 * Creates a new <code>CooperationDomainObserver</code> object.
	 * 
	 * TODO Fill in specific information for constructor.
	 * 
	 * @param quals
	 * @throws IPSocketException
	 */
	public CooperationDomainObserver (ParamsMap params, AgentUI ui)
			throws Exception {
		super (params, ui);
	}

	@Override
	protected void initializeThread (ParamsMap params, AgentUI ui) {
		in ("CooperationDomainObserver.initializeRun");
		super.initializeThread (params, ui);

		if (!params.isDefaulted("CDTOOBSERVE")) {
			try {
				URLDescriptor url = (URLDescriptor)params.getJavaObject("CDTOOBSERVE",URLDescriptor.class);
				doCDObserveMessages (url, true);
			} catch (Exception e) {
				println("error","CooperationDomainObserver:initializeThread: Key :CDTOOBSERVE no URL value",e);
			}
		}
		out ("CooperationDomainObserver.initializeRun");
	}

//	/**
//	 * Called by the constructor to initialize the run time command line
//	 * interface. Subclasses should override this method, but be sure to call
//	 * super.initRTCommandInterface() to include these definitions. <br>
//	 * First calls super to do it's definitions, then defines the following
//	 * commands:
//	 * <li>broadcast: Broadcast a text message to other chat members</li>
//	 */
//	@Override
//	protected void initRTCommandInterface () {
//		in ("CooperationDomainObserver.initRTCommandInterface");
//		super.initRTCommandInterface ();
//		try {
//			commandInterpreter
//					.put (
//							"observe | "
//									+ "CD(type=string; required; help=\"The cooperation domain to observe.\") "
//									+ "start(type=boolean; help=\"true if you want to start observing, false if you want to stop.\") "
//									+ "?(help=\"Observe all messages that a cooperation domain forwards\")",
//							new Command () {
//								@SuppressWarnings("unchecked")
//								@Override
//								public Status execute (String line, Map params,
//										AgentUI ui) {
//									if (watchedCooperationDomain != null) {
//										return new Status (-1,
//												"Already watching a cooperation domain.");
//									}
//									Status status;
//									String b = (String) params.get ("CD");
//									Boolean start = (Boolean) params
//											.get ("start");
//									try {
//										URLDescriptor cd = new URLDescriptor (b);
//										status = doCDObserveMessages (cd,
//												start == null ? true : start
//														.booleanValue ());
//									} catch (URLDescriptorException ex) {
//										return new Status (-6,
//												"Bad cooperation domain URL given: "
//														+ ex.toString ());
//									}
//									return status;
//								}
//							});
//
//		} catch (ParameterParserException ex) {
//			println (
//					"error",
//					"Unexepected exception when executing commandInterpreter.put()'s",
//					ex);
//		}
//		out ("CooperationDomainObserver.initRTCommandInterface");
//	}

	@Override
	public void handleEvent (Event event) {
	    in("ChatAgent.handleMessage");
	    Status ret = null;

	    if (event instanceof MessageEvent) {
  	    MLMessage message = ((MessageEvent)event).getMessage();
  	    String performative = message.getParameter (ML.PERFORMATIVE);
  	    Act act = message.getAct();
  
  	    if (performative.equals (ML.NOTIFY)) {
  	      if (isAAct(act,new Act(ML.MESSAGE_FORWARDED))) {
  	        ret = handleReplies_observeMessages(message).getStatus();
  	      }
  	    }
	    }

	    super.handleEvent (event);
	    out("ChatAgent.handleMessage");
	}

	/**
	 * Sends a subscribe message to start/stop the observation of forwarded
	 * messages in a cooperation domain. Sends a message of format: <table
	 * border="1" bgcolor="gold" cellpadding="3">
	 * <tr>
	 * <td><a href="doc-files/performatives.gif">performative</a></td>
	 * <td>subscribe</td>
	 * </tr>
	 * <tr>
	 * <td>{@link TransientAgent#makeDefaultActs() act}</td>
	 * <td>observe.messages</td>
	 * </tr>
	 * <tr>
	 * <td>reply-by</td>
	 * <td>0</td>
	 * </tr>
	 * <tr>
	 * <td>sense</td>
	 * <td>positive | negative</td>
	 * </tr>
	 * <tr>
	 * <td>language</td>
	 * <td><a
	 * href="doc-files/contentLanguages.html#casa.agentCom.URLDescriptor">casa.agentCom.URLDescriptor
	 * </a></td>
	 * </tr>
	 * <tr>
	 * <td>content</td>
	 * <td><em>a {@link URLDescriptor} object</em></td>
	 * </tr>
	 * </table> The coorespoinding handler is
	 * {@link #handleReplies_observeMessages(MLMessage)}.
	 * 
	 * @param cdURL URLDescriptor of the cooperation domain to observe the
	 *            messages of
	 * @param sense true to set observation; false to cancel
	 * @return a Status object describing success or failure of the sendMessage
	 */
	public Status doCDObserveMessages (URLDescriptor cdURL, boolean sense) {
		in ("CooperationDomainObserver.doCDObserveMessages");
		if (watchedCooperationDomain != null) {
			return new Status (-1, "Already watching a cooperation domain.");
		}
		MLMessage message = getNewMessage (ML.SUBSCRIBE, ML.OBSERVE_MESSAGES,
				cdURL);

		message.setParameter (ML.LANGUAGE, URLDescriptor.class.getName ());
		message.setParameter (ML.CONTENT, getURL ().toString (cdURL));
		message.setParameter (ML.REPLY_BY, Long.toString (ML.TIMEOUT_NEVER));

		if (sense) {
			message.setParameter (ML.SENSE, ML.POSITIVE);
			watchedCooperationDomain = cdURL;
		} else {
			message.setParameter (ML.SENSE, ML.NEGATIVE);
			watchedCooperationDomain = null;
		}

		Status stat;
		if (sense) {
			stat = sendMessage (message);
		} else {
			stat = sendMessage (message);
		}

		out ("CooperationDomainObserver.doCDObserveMessages");
		return stat;
	}

	/**
	 * Handles observe.messages subscribe messages. The handled messages are of
	 * the form: <table border="1" bgcolor="orange" cellpadding="3">
	 * <tr>
	 * <td><a href="doc-files/performatives.gif">performative</a></td>
	 * <td>notify</td>
	 * </tr>
	 * <tr>
	 * <td>{@link TransientAgent#makeDefaultActs() act}</td>
	 * <td>message.forwarded</td>
	 * </tr>
	 * <tr>
	 * <td>language</td>
	 * <td><a
	 * href="doc-files/contentLanguages.html#casa.MLMessage">casa.MLMessage </a>
	 * </td>
	 * </tr>
	 * <tr>
	 * <td>content</td>
	 * <td><em>a {@link MLMessage} object</em></td>
	 * </tr>
	 * </table> or standard error replies. If the message is successfully
	 * interpreted, observeMessage is called with the included message.
	 * 
	 * @param msg the incoming message
	 * @return A Status describing the success of the operation
	 */
	public PerformDescriptor handleReplies_observeMessages (MLMessage message) {
		in ("CooperationDomainObserver.handleReplies_observeMessages");
		Status result = null;

		// check the message
		String[] m = { ML.PERFORMATIVE, ML.INFORM, ML.ACT,
				ML.MESSAGE_FORWARDED };
		StatusObject<Object> stat = verifyMessage (message, m);

		if (stat.getStatusValue () == 0) {
			String content = null;
			try {
				// here we need to manually interpret the message in the
				// contents.
				content = message.getParameter (ML.CONTENT);
				MLMessage wrappedMessage = MLMessage
						.fromString (content);

				observeMessage (wrappedMessage);
			} catch (MLMessageFormatException e) {
				println ("observe", "catch");
				result = new Status (
						-6,
						"CooperationDomainObserver.handleReplys_observeMessages: found malformed message in content field of envelope; message ignored: "
								+ content);
			}
		} else {
			// message isn't right
			println ("observe", "else");
			result = new Status (-1, "Malformed " + ML.MESSAGE_FORWARDED + " "
					+ ML.INFORM + ".");
		}

		out ("CooperationDomainObserver.handleReplies_observeMessages");
		return new PerformDescriptor (result);
	}

	protected void observeMessage (MLMessage message) {
		in ("CooperationDomainObserver.observeMessage");
		if (isLoggingTag ("observe")) {
			println ("observe", message.toString (true));
		}
		out ("CooperationDomainObserver.observeMessage");
	}

	@Override
	protected void pendingFinishRun () {
		in ("CooperationDomainObserver.pendingFinishRun");
		super.pendingFinishRun ();


		if (watchedCooperationDomain != null) {
			Status tempStatus = doCDObserveMessages (
					watchedCooperationDomain, false);
			if (tempStatus.getStatusValue () != 0) {
				if (isLoggingTag ("warning"))
					println (
							"warning",
							"CooperationDomainObserver.finishRun: Cannot unregister (unattach from observing CD messages)",
							tempStatus);
			}
		}
		out ("CooperationDomainObserver.pendingFinishRun");
	}

	/**
	 * @return  Returns the watchedCooperationDomains.
	 */
	public URLDescriptor getWatchedCooperationDomain () {
		return watchedCooperationDomain;
	}

}