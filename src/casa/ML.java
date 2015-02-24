package casa;

/**
 * Title:        CASA
 * Description:  Interface for markup languages <code>KQML</code> and <code>XML</code>.
 *               Contains the list of performatives, as well as constants used to indicate which
 *               of the two markup languages is to be used.
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
 * @author <a href="mailto:laf@cpsc.ucalgary.ca">Chad La Fournie</a>
 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 */
public interface ML {
  // Constants used to indicate a markup language
  /**
   * String used to indicate the XML markup language.
   */
  public final static String XML = "XML";
  /**
   * String used to indicate the KQML markup language.
   */
  public final static String KQML = "KQML";


  // Constants used for message parameters
  /**
   * Tag that encases the performative and is type of the parent of all performatives.
   */
  public final static String PERFORMATIVE = "performative";
  /**
   * Tag that encases the act and is the type of the parent of ML.PERFORMATIVE and speech-act-type things.
   */
  public final static String ACT = "act";
  /**
   * Tag indicating the sense of the message.
   */
  public final static String SENSE = "sense";
  /**
   * Tag that indicates the sender.
   */
  public final static String SENDER = "sender";
  /**
   * Tag indicating the receiver when there is only one.
   */
  public final static String RECEIVER = "receiver";
  /**
   * Tag indicating who the message is from; this tag is only instantiated during forwarding
    */
  public final static String FROM = "from";
//  /**
//   * Tag indicating who the message is to.
//   * @deprecated
//   */
//  public final static String TO = "to";
  /**
   * Tag indicating which CD forwarded this message.
   */
  public final static String CD = "cd";
  /**
   * Tag indicating the recipients of this forwarded message.
   */
  public final static String RECIPIENTS = "recipients";
  /**
   * Tag that determines when this message times out.
   */
  public final static String REPLY_BY = "reply-by";
  /**
   * Long value that indicates there is no timeout.
   */
  public final static long TIMEOUT_NEVER = Long.MAX_VALUE;
  /**
   * Tag indicating the value to reply with.
   */
  public final static String REPLY_WITH = "reply-with";
  /**
   * Tag indicating the value to reply with.
   */
  public final static String REPLY_TO = "reply-to";
  /**
   * Tag indicating some value that is being replied with, this should be the
   * same as the REPLY_WITH value of the message that this message is replying
   * to.
   */
  public final static String IN_REPLY_TO = "in-reply-to";
  /**
   * Tag that, if present, indicates that an acknowledgment is needed.
   */
  public final static String REQUEST_ACK = "request-ack";
  /**
   * Tag that encases the agent.
   */
  public final static String AGENT = "agent_";
  /**
   * Tag that encases the actor.
   */
  public final static String ACTOR = "actor";
  /**
   * Tag that encases the conversion ID of the message.
   */
  public final static String CONVERSATION_ID = "conversation-id";
  /**
   * Tag indicating the language/protocol used in the content part of the message.
   */
  public final static String LANGUAGE = "language";
  public final static String FIPA_SL = "FIPA-SL";
  /**
   * Tag indicating the language/protocol used in the content part of the message.
   */
  public final static String LANGUAGE_VERSION = "language-version";
  /**
   * Tag indicating the ontology used in the content part of the message.
   */
  public final static String ONTOLOGY = "ontology";
  /**
   * Tag indicating the ontology used in the content part of the message.
   */
  public final static String ONTOLOGY_VERSION = "ontology-version";
  /**
   * Tag indicating the content of the message.
   */
  public final static String CONTENT = "content";
  /**
   * Tag that holds the digital signature of the message.
   */
  public final static String SIGNATURE = "signature";
  /**
   * Tag that holds the method used to produce the digital signature
   */
  public final static String SIGNATURE_METHOD = "signature-method";
  /**
   * Tag that holds the name of the encryption algorithm.  This algorithm will then be used to
   * decrypt/encrypt the content of the message.
   */
  public final static String ENCRYPTION_ALGORITHM = "encryption-algorithm";
  /**
   * Tag that holds the processing priority of the message.
   */
  public final static String PRIORITY = "priority";

  // Constants used in various parameters.
  /**
   * Performative used when an agent is acknowledging the receipt of a message.
   */
  public final static String ACK = "ack";
  /**
   * Performative used when an agent negatively acknowledging the receipt of a message.
   */
  public final static String NACK = "nack";
  /**
   * Performative used when an agent is informing another agent about
   * something.
   */
  public final static String INFORM = "inform";
  /**
   * Performative used when an agent is requesting that another agent do
   * something (the superclass of request and propose).
   */
  public final static String PETITION = "petition";
  /**
   * Performative used when an agent is replying to a petition of another agent.
   */
  public final static String REPLY = "reply";
  /**
   * Performative used when an agent is requesting that another agent do
   * something for it.
   */
  public final static String REQUEST = "request";
  /**
   * Performative used when an agent is requesting that another agent do
   * something whenever an event occurs.
   */
  public final static String SUBSCRIBE = "subscribe";
  /**
   * Performative used when an agent is replying to a request of another agent.
   */
  //public final static String REPLY_REQUEST = "reply-request";
  /**
   * Performative used when an agent is replying to a subscribe.
   */
  //public final static String NOTIFY = "notify";
  /**
   * Performative used when an agent wants to cancel notification on a subscribe
   */
  public final static String CANCEL = "cancel";
  /**
   * Performative used when an agent has is proposing to do something for another agent
   */
  public final static String PROPOSE = "propose";
//  public final static String PROPOSE_DISCHARGE = "propose-discharge";
  /**
   * Performative used when an agent is accepting a previous proposal
   */
  public final static String ACCEPT_PROPOSAL = "accept-proposal";
  /**
   * Performative used when an agent is rejecting a previous proposal
   */
  public final static String REJECT_PROPOSAL = "reject-proposal";
  /**
   * Performative used when an agent is proposing to discharge a commitment, successfully completed
   * Use the former parent, {@link #PROPOSE} instead (You are, actually, as now
   * FAILURE == PROPOSE == "propose").
   */
  @Deprecated
  public final static String SUCCESS = PROPOSE;
  //public final static String SUCCESS = "success";
  /**
   * Performative used to have the receiver forward the embedded message to other agent(s) as
   * though it was directly sent from the sender (FIPA).
   */
  public final static String PROPAGATE = "propagate";
  /**
   * Performative used to have the receiver forward the embedded message to other agent(s) as
   * though it was directly sent from the sender (FIPA).
   */
  public final static String PROXY = "proxy";
  /**
   * Performative used when an agent has successfully completed a request
   */
  @Deprecated
  public final static String DONE = "done";
  /**
   * Performative used when an agent has failed to complete a request.
   */
  public final static String FAILURE = "failure";

  /**
   * Performative used when an agent is confirming ...
   */
  public final static String CONFIRM = "confirm";
  /**
   * Performative used when an agent is confirming that another
   * (debtor) agent has failed to fulfill a request on the agent's (creditor's) behalf.
   * Does NOT release the debtor from the commitment.
   */
  public final static String DISCONFIRM = "disconfirm";
  /**
   * Performative used when an agent is indicating that it does not understand
   * a message from another agent.
   */
  public final static String NOT_UNDERSTOOD = "not-understood";
  /**
   * Performative used when an agent is indicating that a request from another
   * agent was refused.
   */
  public final static String REFUSE = "refuse";
  /**
   * Performative used when an agent is indicating that a request from another
   * agent was accepted.
   */
  public final static String AGREE = "agree";
  /**
   * Performative used to indicate that a request persists (supertype of subscribe and request-whenever)
   */
  public final static String PERSISTENT_ACTION = "request-persistent";
  /**
   * Performative used to indicate that a persistent request is accepted (like {@link ML#AGREE agree})
   */
  public final static String CONTRACT = "contract";

  /**
   * The top (T) of all type in the type lattice.
   */
  public final static String TOP = "top";

  /**
   * The parent type to and ML.ACT and as well as physical acts.
   */
  public final static String ACTION = "action";

  /**
   * Performative used when .
   */
  public final static String REQUEST_WHENEVER = "request-whenever";

  /**
   * Performative used when .
   */
  public final static String REQUEST_WHEN = "request-when";

  /**
   * Performative used when .
   */
  public final static String NOTIFY_COMPLETE = "notify-complete";

  /**
   * Performative used when .
   */
  public final static String NOTIFY_FAILURE = "notify-failure";

  /**
   * Performative used when .
   */
  public final static String AFFIRMATIVE_REPLY = "affirmative-reply";

  /**
   * Performative used when .
   */
  public final static String NEGATIVE_REPLY = "negative-reply";

  /**
   * Performative used when .
   */
  public final static String INFORM_IF  = "inform-if";

  /**
   * Performative used when .
   */
  public final static String INFORM_REF = "inform-ref";

  /**
   * Performative used when .
   */
  public final static String QUERY_IF = "query-if";

  /**
   * Performative used when .
   */
  public final static String QUERY_REF = "query-ref";

  /**
   * Performative used when .
   */
  public final static String QUERY_IF_REPLY = "query-if-reply";

  /**
   * Performative used when .
   */
  public final static String QUERY_REF_REPLY = "query-ref-reply";

  /**
   * Performative used when .
   */
  public final static String CFP = "cfp";

//  /**
//   * Performative used when .
//   */
//  public final static String REPLY_PROPOSAL = "reply-proposal";

  /**
   * Sense used when a message is in a negative sense.
   */
  public final static String NEGATIVE = "negative";
  /**
   * Sense used when a message is in a positive sense.
   */
  public final static String POSITIVE = "positive";

  /**
   * Language used to indicate a standard CASA message.
   */
  public final static String CAG_CASA = "CAG-CASA";
  /**
   * Language used to indicate a CASA data message.
   */
  public final static String DATA_CASA = "DATA-CASA";


  // Constants used in message creation and parsing.
  /**
   * Corresponds to the string "null".  Required since cannot parse an actual null.
   */
  public final static String NULL = "null";
  /**
   * Corresponds to an ASCII space character.
   */
  public final static char BLANK = ' ';
  /**
   * Used to indicate negation according to FIPA specification.
   */
  public final static String NOT = "not";
  
  
  // *** event actions ***
  public final static String
  EVENT = "event";
	public final static String EVENT_RECURRING = "event_recurring";
	public final static String EVENT_SC_EVENT = "event_SCEvent";
	public final static String EVENT_SC_PERFORM_ACTION = "event_SCPerformAction";
	public final static String EVENT_SC_START = "event_SCStart";
	public final static String EVENT_SC_STOP = "event_SCStop";
	public final static String EVENT_SC_VIOLATION = "event_SCViolation";
	public final static String EVENT_MESSAGE_EVENT = "event_messageEvent";
	public final static String EVENT_MESSAGE_INCOMING = "event_messageIncoming";
	public final static String EVENT_MESSAGE_OUTGOING = "event_messageOutgoing";
	public final static String EVENT_MESSAGE_OBSERVED = "event_messageObserved";
	public final static String EVENT_MESSAGE_SENT = "event_messageSent";
	public final static String EVENT_MESSAGE_RECEIVED = "event_messageReceived";
	public final static String EVENT_MESSAGE_IGNORED = "event_messageIgnored";
	public final static String EVENT_MESSAGE_PARTICIPANT = "event_messageParticipant";
	public final static String EVENT_EXECUTABLE = "event_executable";
	public final static String EVENT_DEFERRED_EXECUTION = "event_deferedExecution";
	public final static String EVENT_DEFERRED_EXECUTION_DELAYED = "event_deferedExecutionDelayed";
	public final static String EVENT_RECURRINGEXECUTABLE = "event_recurringExecutable";
	public final static String /****** States from ObservableEvent ******/
  EVENT_EXITED = "event_exited";
	public final static String EVENT_EXITING = "event_exiting";
	public final static String /* Message Events */
  EVENT_MESSAGE_SEND_FAILED = "event_messageSendFailed";
	public final static String EVENT_CHANGED_COMMANDS = "event_changedCommands";
	public final static String EVENT_INSERTED_PERFORMATIVES = "event_insertedPerformatives";
	public final static String EVENT_FAILED_PERFORMATIVES_UPDATE = "event_failedPerformativesUpdate";
	public final static String EVENT_STRATEGY_CHANGED = "event_strategyChanged";
	public final static String EVENT_POST_STRING = "event_postString";
	public final static String EVENT_CHAT_MESSAGE_RECEIVED = "event_chatMessageReceived";
	public final static String /* Advertisement Events */
  EVENT_ADVERTISEMENT_EVENT = "event_advertisementEvent";
	public final static String EVENT_ADVERTISEMENT_ADDED = "event_AdvertisementAdded";
	public final static String EVENT_ADVERTISEMENT_REMOVED = "event_AdvertisementRemoved";
	public final static String /* Cooperation Domain Events */
  EVENT_CD_EVENT = "event_CDEvent";
	public final static String EVENT_WITHDRAW_CD = "event_withdrawCD";
	public final static String EVENT_JOIN_CD = "event_joinCD";
	public final static String EVENT_JOIN_CD_FAILED = "event_joinCDFailed";
	public final static String EVENT_GET_HISTORY_CD = "event_getHistoryCD";
	public final static String EVENT_JOIN_CD_REPEATED = "event_joinCDRepeated";
	public final static String EVENT_PARTICIPANT_CD = "event_participantCD";
	public final static String EVENT_INVITE_CD = "event_inviteCD";
	public final static String EVENT_GET_CD_PARTICIPANTS = "event_getCDParticipants";
	public final static String EVENT_UPDATE_URL_CD = "event_updateURLCD";
	public final static String EVENT_PUT_DATA_CD = "event_putDataCD";
	public final static String EVENT_GET_DATA_CD = "event_getDataCD";
	public final static String EVENT_CD_NEW_MEMBER = "event_CDNewMember";
	public final static String /* LAC Events */
  EVENT_LAC_EVENT = "event_LACEvent";
	public final static String EVENT_CLOSE_PORT = "event_closePort";
	public final static String EVENT_REGISTER_INSTANCE = "event_registerInstance";
	public final static String EVENT_REGISTER_INSTANCE_LOCAL = "event_registerInstanceLocal";
	public final static String EVENT_REGISTER_INSTANCE_REMOTE = "event_registerInstanceRemote";
	public final static String EVENT_UNREGISTER_INSTANCE = "event_unregisterInstance";
	public final static String EVENT_FIND_INSTANCES = "event_findInstances";
	public final static String EVENT_REGISTER_TYPE = "event_registerType";
	public final static String EVENT_UNREGISTER_TYPE = "event_unregisterType";
	public final static String EVENT_BANNER_CHANGED = "event_bannerChanged";
	public static final String EVENT_POLICY_APPLIED = "event_policyApplied";
  
  // *** other actions ***
  public final static String 
  CONSIDER = "consider";
  public final static String ADD_OBSERVER = "add_observer";
	public final static String MONITOR = "monitor";
	public final static String NOTIFY = "notify";
	public final static String VERIFY = "verify";
	public final static String EVALUATE = "evaluate";
	public final static String RELEASE = "release";
	public final static String CONCLUDE = "conclude";
	public final static String ACCEPT = "accept";
	public final static String DISCHARGE = "discharge";
	public final static String ASSEMBLE = "assemble";
	public final static String GET = "get";
	public final static String PING = "ping";
	public final static String GET_INFO = "get-info";
	public final static String GET_YELLOW_PAGES = "getYellowPages";
	public final static String GET_AGENTS = "getAgents";
	public final static String GET_AGENTS_RUNNING = "get_agents_running";
	public final static String GET_AGENTS_REGISTERED = "get_agents_registered";
	public final static String GET_CDS = "getCDs";
	public final static String RESOLVE_URL = "resolve_url";
	public final static String GET_DATA = "get_data";
	public final static String GET_HISTORY = "get_history";
	public final static String GET_MEMBERS = "get_members";
	public final static String GET_NAME = "get_name";
	public final static String SEARCH = "search";
	public final static String FIND_INSTANCE = "find_instance";
	public final static String GET_ONTOLOGY = "get_ontology";
	public final static String GET_ACTS = "get_acts";
	public final static String DESTROY = "destroy";
	public final static String DELETE_CD = "deleteCD";
	public final static String DELETE_INFO = "delete-info";
	public final static String DELETE_DATA_CD = "deleteDataCD";
	public final static String DELETE_HISTORY_CD = "deleteHistoryCD";
	public final static String UNREGISTER = "unregister";
	public final static String UNADVERTISE = "unadvertise";
	public final static String UNREGISTER_INSTANCE = "unregister_instance";
	public final static String UNREGISTER_AGENTTYPE = "unregister_agentType";
	public final static String CHANGE_MEMBERSHIP = "membership_change";
	public final static String WITHDRAW_CD = "withdraw_cd";
	public final static String REMOVE_ADVERTISEMENT = "remove_advertisement";
	public final static String CREATE = "create";
	public final static String CREATE_CD = "createCD";
	public final static String SAVE_INFO = "save-info";
	public final static String PUT_DATA = "put_data";
	public final static String REGISTER = "register";
	public final static String REGISTER_YELLOWPAGES = "registerYellowPages";
	public final static String ADVERTISE = "advertise";
	public final static String REGISTER_INSTANCE = "register_instance";
	public final static String REGISTER_AGENTTYPE = "register_agentType";
	public final static String JOIN_CD = "join_cd";
	public final static String OBSERVE = "observe";
	public final static String OBSERVE_MESSAGES = "observe_messages";
	public final static String OBSERVE_MEMBERSHIP = "observe_membership";
	public final static String MESSAGE_FORWARDED = "message_forwarded";
	public final static String RUN = "run";
	public final static String RUN_AGENT = "run_agent";
	public final static String PERFORM = "perform";
	public final static String PHYSICAL_ACT = "physical_act";
	public final static String INVITE_CD = "invite_to_cd";
	public final static String UPDATE = "update";
	public final static String FORWARD_MESSAGE = "forward_message";
	public final static String EXECUTE = "execute";
	public final static String SECURITY_AUTHENTICATE = "security_authenticate";
	public final static String SECURITY_VOTE = "security_vote";
	public final static String SECURITY_CHECK_ACCESS = "security_checkAccess";
	public final static String SECURITY_AUTHORIZE = "security_authorize";
	public final static String SECURITY_AGENT_USERS_LIST = "security_agent_users_list";
	public final static String REQUEST_AKA_MAC_LIST = "request_aka_mac_list";
	public final static String REQUEST_USER_LIST = "request_user_list";
	public final static String SPECIFIC_AKA_LIST = "specific_aka_list";
	public final static String INFORM_USERS_LIST = "inform_users_list";
	public final static String LAC_CLOSING = "LAC_closing";
	public final static String EXIT = "exit";
	public final static String NEW_COMMAND = "new_MenuItem";
	public final static String GUI_ACTION_REQUEST = "gui_action_request";
	public final static String TIME_MESSAGE = "time_message";
	public final static String CHAT_MESSAGE = "chat_message";
	public final static String METHOD_CALL = "method_call";

	public final static String ENTITY = "entity";
	public final static String DEBTOR = "debtor";
	public final static String CREDITOR = "creditor";
}