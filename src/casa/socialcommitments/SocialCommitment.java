package casa.socialcommitments;

import casa.AbstractProcess;
import casa.Act;
import casa.ML;
import casa.MLMessage;
import casa.ObserverNotification;
import casa.PerformDescriptor;
import casa.Status;
import casa.StatusObject;
import casa.TransientAgent;
import casa.abcl.CasaLispOperator;
import casa.abcl.ParamsMap;
import casa.agentCom.URLDescriptor;
import casa.event.AbstractEvent;
import casa.event.Event;
import casa.event.MessageEvent;
import casa.event.TimeEvent;
import casa.interfaces.Describable;
import casa.interfaces.PolicyAgentInterface;
import casa.ui.AgentUI;
import casa.util.InstanceCounter;
import casa.util.Trace;

import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.TreeMap;

import org.armedbear.lisp.Cons;
import org.armedbear.lisp.Environment;

/**
 * <code>SocialCommitment</code> is a class to track a social commitment. 
 * The following chart shows the possible states and their transitions. <p> 
 * <img src="doc-files/social-commitment-states.png"> <p>
 *  The state machine follows the following rules: 
 *  <ul> 
 *  <li> The state machine starts in the  {@link CommitmentState#CREATED created} state. 
 *  <li> The state machine ends in one of the {@link CommitmentState#FULFILLED  fulfilled},
 *       {@link CommitmentState#CANCELED  canceled}, or 
 *       {@link CommitmentState#VIOLATED  violated}  states. 
 *  <li> If the start event occurs and the current state is 
 *       {@link CommitmentState#CREATED  created}, then the current state is 
 *       set to {@link CommitmentState#STARTED  started}. 
 *  <li> If the perform action event occurs and the current state is 
 *       {@link CommitmentState#STARTED  started}, then the current state is 
 *       set to {@link CommitmentState#PERFORM_ACTION  perform-action}. 
 *  <li> If the state becomes  {@link CommitmentState#STARTED  started}, 
 *       and the perform action event has previously occurred, then the 
 *       current state is set to  
 *       {@link CommitmentState#PERFORM_ACTION  perform-action}. 
 *  <li> If the stop event occurs and the current state is 
 *       {@link CommitmentState#STARTED  started}  or 
 *       {@link CommitmentState#PERFORM_ACTION  perform-action}, then 
 *       the current state is set to  {@link CommitmentState#FULFILLED  fulfilled}. 
 *  <li> If the state becomes  {@link CommitmentState#STARTED  started}, 
 *       and the stop event has previously occurred, then the current state is set
 *       to {@link CommitmentState#FULFILLED  fulfilled} . 
 *  <li> If  {@link #execute(PolicyAgentInterface)}  is called and the current 
 *       state is {@link CommitmentState#PERFORM_ACTION  perform-action}, then
 *       the current state is set to  
 *       {@link CommitmentState#READY_FULFILLED  ready-fulfilled} . 
 *  <li> If  {@link #designateFulfilled()}  is called and the current state is not 
 *       an end state, then the current state is set to 
 *       {@link CommitmentState#FULFILLED  fulfilled}. 
 *  <li> If  {@link #designateCanceled()}  is called and the current state is not 
 *       an end state, then the current state is set to 
 *       {@link CommitmentState#CANCELED  canceled}. 
 *  <li> If  {@link #designateStarted()}  is called and the current state is not 
 *       an end state, then the current state is set to 
 *       {@link CommitmentState#STARTED  started}. 
 *  <li> If the violation event occurs and the current state is not an end state, 
 *       then the current state is set to  {@link CommitmentState#VIOLATED  violated}. 
 *  <li> If the social commitment does not have a start event or perform action event, 
 *       then those events are considered to have occurred. <li> If the start event 
 *       or the perform action event occur before they have an effect, then the fact 
 *       that they have occurred is recorded. 
 *  <li> If the social commitment does not have a stop event or a violation event, 
 *       then those event never occur.
 * </ul>
 * @author  Jason Heard, Rob Kremer
 * @version 0.9
 */
public class SocialCommitment extends Observable implements Observer, Comparable<SocialCommitment>, Describable {
	/**
	 * The agent that "owns" this SocialCommitment.  Used primarily for reporting back state changes.
	 */
	private PolicyAgentInterface agent;
	
  /**
	 * The debtor of this commitment. This is the agent who owes the debtor to perform the action of this commitment.
	 */
  private URLDescriptor debtor;

  /**
	 * The creditor of this commitment. This is the agent who is owed the action of this commitment.
	 */
  private URLDescriptor creditor;

  /**
   * The performative of this commitment. This represents the type of action
   * that this commitment requires the debtor to perform.
   */
  private String performative;

  /**
	 * The act of this commitment. This is typically the same as the act of the message that caused the creation of this commitment.
	 */
  private Act act;

  /**
	 * The event that caused the creation of this social commitment.
	 */
  private Event event;

  /**
	 * The action that should fulfil this commitment.
	 */
  private Action action;
  
  /**
   * Supports the {@link Describable} interface
   */
  private TreeMap<String,String> params = new TreeMap<String,String>();

  /**
   * The time that this social commitment was created. It is stored in the
   * format given by {@link System#currentTimeMillis()}. It cannot change over
   * the lifetime of a social commitment.
   */
  private final long createdTime;

  /**
   * The time when this commitment ended (was fulfilled, canceled or violated).
   * It is stored in the format given by {@link System#currentTimeMillis()}. A
   * value of 0 indicates that this social commitment has not ended.
   */
  private long endTime;

  /**
   * A map which contains the events associated with this commitment. The keys
   * in the map indicate the type of event, and the values are the events
   * themselves. The event types are strings, which are part of the CASAOntology.
   * The event type can be one of:
   * <ul>
   * <li>{@link casa.ML#EVENT_SC_START start event}
   * <li>{@link casa.ML#EVENT_SC_PERFORM_ACTION perform action event}
   * <li>{@link casa.ML#EVENT_SC_STOP stop event}
   * <li>{@link casa.ML#EVENT_SC_VIOLATION violation event}
   * </ul>
   */
  private Map<String, Event> events;

  /**
   * The priority of this commitment. Lower numbers are less urgent, and the
   * default priority is 10.
   */
  private int priority;

  /**
   * This is a unique serial number for each social commitment. It does not
   * affect equals, because then it would be impossible to create two
   * commitments that are equal to one another.
   */
  private final long serialNumber;

  /**
   * This is the next serial number to be used.
   */
  private static long nextSerialNumber = 1;

  /**
	 * The current state of the commitment. See the class description for a description of the states and the transitions between them.
	 */
  private CommitmentState currentState;

  /**
   * A set of status flags for this social commitment. The set contains a subset
   * of the {@link SocialCommitmentStatusFlags} enumeration. For each flag, if
   * it is contained within this set, it is considered to be set to {@code true}.
   * If it is not in this set, it is considered to be {@link false}.
   */
  private EnumSet<SocialCommitmentStatusFlags> statusFlags;
  
//  /**
//	 * A set of {@linkplain SocialCommitmentObserver observers} which should be
//	 * informed when this social commitment is executed.
//	 */
//  private Collection</*SocialCommitmentObserver*/SocialCommitment> observers = new Vector</*SocialCommitmentObserver*/SocialCommitment> ();

  /**
	 * The ID of the Conversation in which the Social Commitment was formed
	 * 
	 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
	 */
  private String ownerConversationID = null;
  
  public String getOwnerConversationID(){
  	return this.ownerConversationID;
  }
  
  /**
   * The time from current that the SocialCommitment will go Violated if not otherwise specified.
   */
  final static long DEFAULT_TIMEOUT = 60000;
  
  /**
   * Constructs a new social commitment from the given information.  The new social commitment is set up by performing the following operations:
   * <ol>
   * <li>The parameters which are given are saved in the appropriate fields.
   * <li>The {@linkplain #createdTime creation time} is set to {@link System#currentTimeMillis()}.
   * <li>The {@linkplain #priority} is set to the priority parameter of the given message if it exists.  If it does not exist, it is set to the default value of 10.
   * <li>The {@linkplain #serialNumber serial number} is set to {@linkplain #nextSerialNumber the next serial number}, which is incremented.
   * <li>Several static status flags are set based on the given information or their default values.
   * <li>The given action and events are notified of their new master.
   * <li>The state is set to {@link CommitmentState#CREATED created} and then {@link #updateStateAtStartup()} is called.
   * <li>All of the events are {@linkplain Event#start() started}.
   * </ol>  
   * 
   * @param newDebtor The debtor of the new social commitment.
   * @param newCreditor The creditor of the new social commitment.
   * @param newPerformative The performative of the new social commitment.
   * @param newAct The act of the new social commitment.
   * @param msg The message which caused the creation of the new social commitment.
   * @param newAction The action which should fulfill the new social commitment.
   * @param newEvents The set of events associated with the new social commitment.
   */
  public SocialCommitment (PolicyAgentInterface agent, URLDescriptor newDebtor, URLDescriptor newCreditor,
      String newPerformative, Act newAct, Event event, Action newAction,
      Event... newEvents) {

  	this.agent = agent;
  	debtor = newDebtor;
    creditor = newCreditor;
    performative = newPerformative;
    act = newAct;
    this.event = event;
    params.put(ML.PERFORMATIVE, performative);
    params.put(ML.ACT, act.toString());
    params.put("debtor", debtor.toString());
    params.put("creditor", creditor.toString());
    
    
    createdTime = System.currentTimeMillis ();
    endTime = 0;
    priority = (getMessage() != null) ? getMessage().getIntParameter (ML.PRIORITY, 10) : 10;
    serialNumber = nextSerialNumber++;
    statusFlags = EnumSet.of (SocialCommitmentStatusFlags.SHARED);

    //ownerConversationID = ((TransientAgent)agent).getConversation(
    	//	msg.getConversationID()).getRunningConversationID();
    if (getMessage()!=null) 
    	ownerConversationID = getMessage().getConversationID();

    action = newAction;
    if (action != null) {
      action.setOwner (this);
      statusFlags.add (SocialCommitmentStatusFlags.HAS_ACTION);
    }
    
    // setting our local copy of events
    events = new TreeMap<String, Event> ();
    if (newEvents != null) {
      for (Event e : newEvents) {
        events.put (e.getEventType (), e);
      }
    }
 
    // set all static attributes stemming from message, action and events
    if (action != null) {
      statusFlags.add (SocialCommitmentStatusFlags.HAS_ACTION);
    }
    if (getMessage() == null || ! getMessage().isBroadcast()) {
      statusFlags.add(SocialCommitmentStatusFlags.OBLIGATED);
    }
    Event tempEvent = events.get (ML.EVENT_SC_PERFORM_ACTION);
    if (tempEvent != null && tempEvent.isRecurring ()) {
      statusFlags.add (SocialCommitmentStatusFlags.RECURRING);
    }
//    if (agent.isA(performative, ML.PERSISTENT_ACTION)) { //dsb
//    	statusFlags.add (SocialCommitmentStatusFlags.PERSISTENT);
//    }
    
//  	MLMessage msg = getMessage();
//    if (!hasEventOfType (ML.EVENT_SC_VIOLATION) 
//    		&& msg != null 
//    		&& !flagSet(SocialCommitmentStatusFlags.RECURRING)
//    		&& msg.getTimeout()!=0) {
//    	TimeEvent timeEvent = new TimeEvent (ML.EVENT_SC_VIOLATION, agent, msg.getTimeout ());
//      timeEvent.setParameter(ML.CONVERSATION_ID, msg.getParameter(ML.CONVERSATION_ID));
//      timeEvent.setParameter(ML.SENDER,          msg.getParameter(ML.SENDER));
//      timeEvent.setParameter(ML.RECEIVER,        msg.getParameter(ML.RECEIVER));
//      timeEvent.setParameter(ML.PERFORMATIVE,    msg.getParameter(ML.PERFORMATIVE));
//      timeEvent.setParameter(ML.ACT,             msg.getParameter(ML.ACT));
//      events.put (ML.EVENT_SC_VIOLATION, timeEvent);
//    }
    if (!flagSet(SocialCommitmentStatusFlags.RECURRING)) {
    	TimeEvent timeEvent;
    	MLMessage msg = getMessage();
    	if (msg != null && msg.getTimeout()!=0) {
    		long timeout = msg.getTimeout();
    		if (timeout==Long.MAX_VALUE || timeout < System.currentTimeMillis()) 
    			timeout = System.currentTimeMillis() + DEFAULT_TIMEOUT; 
    		timeEvent = new TimeEvent (ML.EVENT_SC_VIOLATION, agent, timeout);
    		timeEvent.setParameter(ML.CONVERSATION_ID, msg.getParameter(ML.CONVERSATION_ID));
    		timeEvent.setParameter(ML.SENDER,          msg.getParameter(ML.SENDER));
    		timeEvent.setParameter(ML.RECEIVER,        msg.getParameter(ML.RECEIVER));
    		timeEvent.setParameter(ML.PERFORMATIVE,    msg.getParameter(ML.PERFORMATIVE));
    		timeEvent.setParameter(ML.ACT,             msg.getParameter(ML.ACT));
    	}
    	else {
    		timeEvent = new TimeEvent (ML.EVENT_SC_VIOLATION, agent, System.currentTimeMillis() + DEFAULT_TIMEOUT);
    	}
  		timeEvent.setParameter(ML.DEBTOR, debtor.toString());
  		timeEvent.setParameter(ML.CREDITOR, creditor.toString());
  		events.put (ML.EVENT_SC_VIOLATION, timeEvent);
    }

    for (Event e : events.values ()) {
      //e.addEventObserver (this);
      e.addObserver(this);
    }

    currentState = CommitmentState.CREATED;
    agent.println("commitments", "Commitment CREATED: "+toString());
    updateStateAtStartup ();

    // Events must be started before they will work.
    for (Event e : events.values ()) {
      e.start ();
    }
    
    InstanceCounter.add(this);
  }

  /**
   * Constructs a new social commitment from the given information. This is a
   * convenience constructor for when no events are given for the new social
   * commitment. This constructor calls
   * {@link #SocialCommitment(URLDescriptor, URLDescriptor, String, Act, MLMessage, Action, Event...)}
   * with a {@code null} final argument.
   * 
   * @param agent 
   * @param newDebtor The debtor of the new social commitment.
   * @param newCreditor The creditor of the new social commitment.
   * @param newPerformative The performative of the new social commitment.
   * @param newAct The act of the new social commitment.
   * @param msg The message which caused the creation of the new social
   *            commitment.
   * @param newAction The action which should fulfill the new social commitment.
   */

  public SocialCommitment (PolicyAgentInterface agent, URLDescriptor newDebtor, URLDescriptor newCreditor,
      String newPerformative, Act newAct, Event event, Action newAction) {
    this (agent, newDebtor, newCreditor, newPerformative, newAct, event, newAction,
        (Event[]) null);
  }

  /**
	 * Retrieves the debtor of this commitment. This is the agent who owes the debtor to perform the action of this commitment.
	 * @return  The debtor of this commitment.
	 */
  public URLDescriptor getDebtor () {
    return debtor;
  }

  /**
	 * Retrieves the creditor of this commitment. This is the agent who is owed the action of this commitment.
	 * @return  The creditor of this commitment.
	 */
  public URLDescriptor getCreditor () {
    return creditor;
  }

  /**
	 * Retrieves the performative of this commitment. This represents the type of action that this commitment requires the debtor to perform.
	 * @return  the performative of this commitment.
	 */
  public String getPerformative () {
    return performative;
  }

  /**
	 * Retrieves the act of this commitment. This is typically the same as the act of the message that caused the creation of this commitment.
	 * @return  The act of this commitment.
	 */
  public Act getAct () {
    return act;
  }

  /**
	 * Retrieves the action of this commitment. 
	 * @return  The action of this commitment.
	 */
  public Action getAction () {
    return action;
  }

  /**
	 * Retrieves the Event that caused the creation of this social commitment.
	 * @return  The Event that caused the creation of this social commitment.
	 */
  public Event getEvent () {
    return event;
  }

  /**
	 * Retrieves the message in the event that caused the creation of this social commitment, if there is one; else null.
	 * @return  The message in the event that caused the creation of this social commitment.
	 */
  public MLMessage getMessage () {
    return (event instanceof MessageEvent)? ((MessageEvent)event).getMessage() : null;
  }

  /**
	 * Retrieves the time that this social commitment was created. It is stored in the format given by  {@link System#currentTimeMillis()} . It cannot change over the lifetime of a social commitment.
	 * @return  The time that this social commitment was created.
	 */
  public long getCreatedTime () {
    return createdTime;
  }
  
  /**
	 * Returns the time this commitment ended (was fulfilled, canceled or expired). It is stored in the format given by  {@link System#currentTimeMillis()} . MAX_VALUE if it has not ended.
	 * @return  The time this commitment was fulfilled, canceled or expired, or  Long.MAX_VALUE if it has not ended.
	 */
  public long getEndTime () {
    if (flagSet (SocialCommitmentStatusFlags.ENDED)) {
      return endTime;
    }

    return Long.MAX_VALUE;
  }
  
  /**
   * @return The current state of this SocialCommitment
   * @see CommitmentState
   */
  public CommitmentState getState() {
  	return currentState;
  }

  /**
	 * Retrieves the priority of this commitment. Lower numbers are less urgent, and the default priority is 10.
	 * @return  The priority of this commitment.
	 */
  public int getPriority () {
    return priority;
  }

  /**
	 * Sets the priority of this commitment. Lower numbers are less urgent, and the default priority is 10.
	 * @param newPriority  The new value of priority.
	 */
  public void setPriority (int newPriority) {
    this.priority = newPriority;
  }

  /**
	 * Retrieves the unique serial number for this social commitment. It does not affect equals, because then it would be impossible to create two commitments that are equal to one another.
	 * @return  The unique serial number for this social commitment.
	 */
  public long getSerialNumber () {
    return serialNumber;
  }
  
  /**
   * Determines if this social commitment has an event of the indicated type.
   * 
   * @param type A string describing the type of event that the calling method
   *            is curious about.
   * @return {@code true} if this commitment has an event of the
   *         specified type; {@code false} otherwise.
   */
  protected boolean hasEventOfType (String type) {
    return events.containsKey (type);
  }

  /**
   * Retrieves the event of the indicated type, if any.
   * 
   * @param type A string describing the type of event that should be retrieved.
   * @return An event of the type specified, or <code>null</code> if this
   *         commitment does not have an event of the specified type.
   */
  protected Event getEventOfType (String type) {
    return events.get (type);
  }

  /**
   * Executes this social commitment by performing its associated action. This
   * is done in several steps:
   * <ol>
   * <li>{@link #updateStatePerformingAction()} is called to move the state
   * machine forward.
   * <li>{@link #action}.{@link Action#perform(PolicyAgentInterface) perform(PolicyAgentInterface)}
   * is called to actually perform the action associated with this social
   * commitment. If there is no associated action, a perform descriptor with a
   * status of 0 is returned.
   * <li>If the status of the perform descriptor returned by the execution of
   * the action has a status of {@link TransientAgent#DEFER_ACTION}, then the
   * social commitment is restarted by calling {@link #designateStarted()}.
   * <li>If the status of the perform descriptor returned by the execution of
   * the action has a status of {@link TransientAgent#DROP_ACTION}, then the
   * social commitment is fulfilled by calling {@link #designateFulfilled()}.
   * <li>If the status of the perform descriptor returned by the execution of
   * the action does not have a status of {@link TransientAgent#DEFER_ACTION}
   * or {@link TransientAgent#DROP_ACTION}, then all observers are notified
   * by calling {@link #notifyObservers(PerformDescriptor)} with the status
   * that will be returned by this method.
   * <li>If the {@link SocialCommitmentStatusFlags.SHARED SHARED} flag is not
   * set to {@code true}, then the social commitment is fulfilled by calling
   * {@link #designateFulfilled()}.
   * <li>Finally, the perform descriptor returned by the execution of the
   * action is returned.
   * </ol>
   * 
   * @param agent The agent in which will provide the context for the
   *            execution of this social commitment.
   * @return The status returned by {@link #action}.{@link Action#perform(PolicyAgentInterface) perform(PolicyAgentInterface)}
   *         if it is not {@code null}.
   */
  public PerformDescriptor execute (PolicyAgentInterface agent) {
  	PerformDescriptor status;
  	try {
  		updateStatePerformingAction ();
  		if (action == null) {
  			status = new PerformDescriptor (new Status (0, "null action"));
  		}
  		else {
  			status = action.perform (agent);
  		}

  		if (status == null)
  			status = new PerformDescriptor ();

  		if (status.getStatusValue () == TransientAgent.DEFER_ACTION) {
  			if (!designateStarted ()) {
  				agent.println("warning","Cannot restart violated, fulfilled, or cancelled commitment "+this.getSerialNumber());
  			}
  		} else if  (status.getStatusValue () == TransientAgent.DROP_ACTION) {
  			//if the agent chooses to ignore the action, we can pretend it fulfilled.
  			if (flagSet (SocialCommitmentStatusFlags.OBLIGATED)) 
  				agent.println("warning","Dropping obligitory commitment "+this.getSerialNumber());
  			designateFulfilled ();
  		} else if (status.getStatusValue() < 0) {
  			agent.println("error", "Commitment consequent failed: "+toString());
  			designateCanceled();
  		} else {
  			if (! flagSet (SocialCommitmentStatusFlags.SHARED)) {
  				// must "self-fulfill" if there's no-one to agree on this commitment's
  				// fulfillment
  				designateFulfilled ();
  			}

  			notifyObservers (status);
  		}
  	} catch (Throwable e) {
  		status = new PerformDescriptor(-5, agent.println("error", "SocialCommitment.execute(): (serial#="+getSerialNumber()+") Exception occurred", e));
  	}

  	return status;
  }

  /**
   * Moves forward in the state machine from the state of {@link CommitmentState#CREATED created}
   * when the commitment is first created. This happens if no event of type
   * start is found, and continues if no event of type perform action are found.
   */
  private void updateStateAtStartup () {
  	String msg = "";
    if (currentState == CommitmentState.CREATED) {
      if (!hasEventOfType (ML.EVENT_SC_START)) {
        currentState = CommitmentState.STARTED;
        msg = "Commitment STARTED: ";
      }
    }

    if (currentState == CommitmentState.STARTED) {
      if (!hasEventOfType (ML.EVENT_SC_PERFORM_ACTION)) {
        currentState = CommitmentState.PERFORM_ACTION;
        msg += "Commitment PERFORM_ACTION: ";
      }
    }

    updateStatusWord ();
    if (msg.length()>0)
    	agent.println("commitments", msg+toString());
  }

  /**
   * Moves forward in the state machine from the state of
   * {@link CommitmentState#PERFORM_ACTION perform-action} when the commitment
   * is executed. This is called from {@link #execute(PolicyAgentInterface)}.
   */
  private void updateStatePerformingAction () {
    // if not, something is wrong...
    if (currentState == CommitmentState.PERFORM_ACTION) {
    	String msg;
      if (flagSet (SocialCommitmentStatusFlags.RECURRING)) {
        currentState = CommitmentState.STARTED;
        msg = "Commitment STARTED: ";
      } else {
        currentState = CommitmentState.READY_FULFILLED;
        msg = "Commitment READY_FULFILLED (performing): ";
      }

      updateStateAtStartup ();
      agent.println("commitments", msg+toString());
    }
    else {
    	agent.println("error", "SocialCommitment.updateStatePerformingAction(): Erroniously entered this method in state "+currentState);
    }
  }

  /**
   * Attempts to reset the social commitment by changing the state of this
   * commitment to {@link CommitmentState#STARTED started}. This will only
   * occur if the commitment has not already ended (been fulfilled, canceled or
   * violated).
   * 
   * @return {@code true} if this commitment has been successfully
   *         restarted (had its state set to
   *         {@link CommitmentState#STARTED started}); {@code false}
   *         otherwise.
   */
  public boolean designateStarted () {
    if (! flagSet (SocialCommitmentStatusFlags.ENDED)) {
      currentState = CommitmentState.STARTED; // reset
      
      // updateState will move the state machine forward to PERFORM_ACTION if no start event exists.
      updateStateAtStartup ();
      agent.println("commitments", "Commitment STARTED: "+toString());
      return true;
    }
    
    return false;
  }

  /**
   * Attempts to change the state of this commitment to
   * {@link CommitmentState#FULFILLED fulfilled}. This will only occur if the
   * commitment has not already ended (been fulfilled, canceled or violated).
   * This also has the effect of setting the {@link #endTime} to
   * {@link System#currentTimeMillis()}.
   * 
   * @return {@code true} if the state of this commitment has been
   *         successfully set to {@link CommitmentState#FULFILLED fulfilled};
   *         {@code false} otherwise.
   */
  public boolean designateFulfilled () {
    if (! flagSet (SocialCommitmentStatusFlags.ENDED)) {
      currentState = CommitmentState.FULFILLED;
      this.endTime = System.currentTimeMillis ();

      updateStatusWord ();
      retractTimeoutEvent();
      agent.println("commitments", "Commitment FULFILLED: "+toString());
      return true;
    }

    return false;
  }
  
  protected void retractTimeoutEvent() {
  	Event event = events.get(ML.EVENT_SC_VIOLATION);
  	if (event==null) return;
  	if (event instanceof TimeEvent)
  	  ((TimeEvent)event).cancel();
  }

  /**
   * Attempts to change the state of this commitment to
   * {@link CommitmentState#CANCELED canceled}. This will only occur if the
   * commitment has not already ended (been fulfilled, canceled or violated). If
   * successful, this also has the effect of setting the {@link #endTime} to
   * {@link System#currentTimeMillis()}.
   * 
   * @return {@code true} if the state of this commitment has been
   *         successfully set to {@link CommitmentState#CANCELED canceled};
   *         {@code false} otherwise.
   */
  public boolean designateCanceled () {
    if (! flagSet (SocialCommitmentStatusFlags.ENDED)) {
      currentState = CommitmentState.CANCELED;
      this.endTime = System.currentTimeMillis ();

      updateStatusWord ();
      retractTimeoutEvent();
      agent.println("commitments", "Commitment CANCELED: "+toString());
      return true;
    }
    
    return false;
  }

  /**
   * Determines if the given object is equal to this social commitment. This is
   * true if the given object is this social commitment, or if the given object
   * is a social commitment and the {@link #equals(SocialCommitment)} method
   * returns {@code true} for that commitment. Otherwise, the object is not
   * considered to be equal to this object.
   * 
   * @param object The object to compare to {@code this} for equality.
   * @return {@code true} if the given object is equal to this social
   *         commitment; {@code false} otherwise.
   */
  @Override
  public boolean equals (Object object) {
  	if (object==null)
  		return false;
    if (object == this) {
      return true;
    } else if (object instanceof SocialCommitment) {
      return equals ((SocialCommitment) object);
    }

    return false;
  }

  /**
   * Determines if the given social commitment is equal to this social
   * commitment. The commitments are considered equal if each of the following fields in both social commitents are equal:
   * <ul>
   * <li>{@link #debtor}
   * <li>{@link #creditor}
   * <li>{@link #action}
   * <li>{@link #act}
   * <li>{@link #performative}
   * <li>{@link #priority}
   * </ul>
   * 
   * @param commitment The commitment to compare to {@code this} for equality.
   * @return {@code true} if the given social commitment is equal to this social
   *         commitment; {@code false} otherwise.
   */
  private boolean equals (SocialCommitment commitment) {
  	if (commitment==null) 
  		return false;
  	return compareTo(commitment)==0;
//    if (commitment == this) {
//      return true;
//    }
//
//    return commitment.debtor.equals (this.debtor)
//        && commitment.creditor.equals (this.creditor)
//        && (commitment.action == this.action || (commitment.action != null
//            && this.action != null && commitment.action.equals (this.action)))
//        && commitment.act.equals(this.act)
//        && commitment.performative.equals(this.performative)
//        && commitment.priority == this.priority;
  }
 

  /**
   * Compare the given social commitment to this social commitment. This is done
   * by comparing identical fields in both social commitments. The fields are
   * compared in the following order. The first instance of inequality is
   * returned.
   * <ol>
   * <li>{@link #debtor}
   * <li>{@link #creditor}
   * <li>{@link #action}
   * <li>{@link #act}
   * <li>{@link #performative}
   * </ol>
   * 
   * @param commitment The commitment to compare to {@code this} for equality.
   * @return The value {@code 0} if the two social commitments are equal; a
   *         value less than {@code 0} if this commitment comes before the given
   *         commitment; and a value greater than {@code 0} if the given
   *         commitment comes before this commitment.
   */
  @Override
	public int compareTo (SocialCommitment commitment) {
  	assert commitment!=null;
  	
  	assert debtor!=null;
  	assert creditor!=null;
  	assert act!=null;
  	assert performative!=null;
  	assert commitment.debtor!=null;
  	assert commitment.creditor!=null;
  	assert commitment.act!=null;
  	assert commitment.performative!=null;

//  	return Integer.signum(commitment.hashCode()-this.hashCode());
	 
  	int ret;
  	ret = this.debtor.compareTo(commitment.debtor);
  	if (ret!=0) return ret;
  	ret = this.creditor.compareTo(commitment.creditor);
  	if (ret!=0) return ret;
  	if (commitment.action != null && this.action != null) 
  		ret = this.action.compareTo (commitment.action);
  	if (ret!=0) return ret;
  	ret = this.act.toString ().compareTo (commitment.act.toString ());
  	if (ret!=0) return ret;
  	ret = this.performative.compareTo (commitment.performative);
  	if (ret!=0) return ret;
  	if (this.ownerConversationID != null) {
  		ret = this.ownerConversationID.compareTo (commitment.ownerConversationID);
  		if (ret!=0) return ret;
  	}
  	else if (commitment.ownerConversationID!=null)
  		return -1;
  	//    if (commitment.priority != this.priority) return commitment.priority < this.priority?-1:1;
  	//    if (commitment.createdTime != this.createdTime) return commitment.createdTime<this.createdTime?-1:1;
  	//    if (commitment.endTime != this.endTime) return commitment.endTime < this.endTime?-1:1;
  	return 0;
  }

  /**
   * Returns a hashing of this social commitment.  The hash code is based on the following fields:
   * <ul>
   * <li>{@link #debtor}
   * <li>{@link #creditor}
   * <li>{@link #action}
   * <li>{@link #act}
   * <li>{@link #performative}
   * </ul>
   * 
   * @return A hashing code of this social commitment.
   */
  @Override
  public int hashCode () {
  	if (debtor==null) Trace.log("error", "SocialCommitment.hashCode(): found null debtor");
  	if (creditor==null) Trace.log("error", "SocialCommitment.hashCode(): found null creditor");
  	if (act==null) Trace.log("error", "SocialCommitment.hashCode(): found null act");
  	if (performative==null) Trace.log("error", "SocialCommitment.hashCode(): found null performative");
  	assert debtor!=null;
  	assert creditor!=null;
  	assert act!=null;
  	assert performative!=null;
    int ret = (debtor==null?0:debtor.hashCode ()) 
    		+ (creditor==null?0:creditor.hashCode ()) 
    		+ (action==null?0:action.hashCode ())
        + (act==null?0:act.hashCode())
        + (performative==null?0:performative.hashCode());
    return ret;
  }

  /**
   * Returns a string describing this social commitment. This string is not
   * complete enough to recreate the social commitment, but is instead intended
   * to be displayed in an interface.  The string contains simplified versions of the following fields:
   * <ul>
   * <li>{@link #debtor}
   * <li>{@link #creditor}
   * <li>{@link #performative}
   * <li>{@link #act}
   * <li>{@link #action}
   * </ul>
   * In addition, it incorporates the string produced by {@link #attributesString()}.
   * 
   * @return A string describing this social commitment.
   */
  @Override
  public String toString () {
  	return toString(true);
//    StringBuffer buf = new StringBuffer ();
//    
//    buf.append (serialNumber);
//
//    buf.append (": D: ");
//    buf.append (debtor.getShortestName ());
//
//    buf.append (", C: ");
//    buf.append (creditor.getShortestName ());
//
//    buf.append (", (");
//    buf.append (performative == null ? "*" : performative);
//    buf.append ("/");
//    buf.append (act == null ? "*" : act.toString ());
//    buf.append (")");
//
//    buf.append (", action: ");
//    buf.append (action == null ? "null" : action.displayString ());
//
//    buf.append (", priority: ");
//    buf.append (getPriority());
//    
//    if (events!=null && events.size()>0) {
//      buf.append (", events: ");
//      for (String e: events.keySet())
//      	buf.append(e).append(',');
//    } else
//    	buf.append(", no events");
//    
//    buf.append (" [");
//    buf.append (attributesString ());
//    buf.append (" ]");
//
//    return buf.toString ();
  }

  /**
   * Returns a string describing this social commitment. This string is not
   * complete enough to recreate the social commitment, but is instead intended
   * to be displayed in an interface.  The string contains simplified versions of the following fields:
   * <ul>
   * <li>{@link #debtor}
   * <li>{@link #creditor}
   * <li>{@link #performative}
   * <li>{@link #act}
   * <li>{@link #action}
   * </ul>
   * In addition, it incorporates the string produced by {@link #attributesString()}.
   * @param prettyPrint Return a more human-readable string.
   * @return A string describing this social commitment.
   */
  public String toString (boolean prettyPrint) {
  	StringBuffer buf = new StringBuffer ();
  	String brk = prettyPrint?"\n  ":" "; 
  	buf.append("(socialcommitment");
  	if (prettyPrint) {
  		buf.append(brk) 
  		.append("; serial#=").append(serialNumber)
  		.append(", state=").append(currentState)
  		.append(", flags=").append(statusFlags)
  		.append(", createTime=").append(new Date(createdTime))
  		.append(", endTime=").append(endTime==0?"unspecified":(endTime==Long.MAX_VALUE?"infinite":new Date(endTime)));
  		if (countObservers()>0) 
  			buf.append(brk).append("; observers=").append(countObservers());
  		if (event!=null)
  			buf.append(brk).append("; creatingEvent=").append(event);
  		if (params!=null && params.size()>0) 
  			buf.append(brk).append("; params=").append(params);
  	}
  	buf.append(brk)  
  	.append(":agent ").append(agent==null?"NIL":agent.toString()).append(brk)
  	.append(":debtor ").append(debtor.getShortestName()).append(brk)
  	.append(":creditor ").append(creditor.getShortestName()).append(brk)
  	.append(":performative ").append(performative==null?"NIL":performative).append(brk)
  	.append(":act ").append(act==null?"NIL":act.toString()).append(brk)
  	.append(":priority ").append(getPriority()).append(brk)
  	.append(":events ");
  	if (events!=null && events.size()>0) {
  		buf.append ("(");
  		for (String e: events.keySet())
  			buf.append(e).append("(id=").append(events.get(e).getID()).append(") ");
  		buf.append (")");
  	} 
  	else
  		buf.append("NIL");
  	buf.append(brk)
  	.append(":action ").append(action==null?"NIL":action.toString()).append(brk)
  	.append(')').append(brk);

  	return buf.toString ();
  }

  /**
   * Creates a {@link StringBuffer} object and fills it with some attributes of
   * this social commitment. This method should be overridden in subclasses to
   * give more information.
   * 
   * @return A string buffer filled with descriptions of some attributes of this
   *         social commitment.
   */
  protected StringBuffer attributesString () {
    StringBuffer display = new StringBuffer ();

    display.append (" ")
           .append (currentState.toString ())
           .append ("(")
           .append (statusFlags.toString())
           .append (")");
    if (meetsMask (MASK_EXECUTABLE)) {
      display.append (" executable");
    }
    
//    if (observers.size()>0) {
//    	boolean first = true;
//    	display.append (" observers=");
//    	for (SocialCommitment/*Observer*/ i : observers) {
//    		if (i instanceof SocialCommitment) {
//    			SocialCommitment sc = (SocialCommitment) i;
//    			if (! first)
//    				display.append(",");
//    			display.append(sc.getSerialNumber());
//    		}
//    	}
//    }
    int obs = super.countObservers();
    if (obs>0)
    	display.append(" observers:"+obs);
    
    return display;
  }

  /**
	 * The <code>CommitmentState</code> enumeration is used to describe the set of possible states that a social commitment may be in.
	 * @author  Jason Heard
	 * @version 0.9
	 */
  public static enum CommitmentState {
  	/**
  	 */
  	CREATED("created"), 
  	/**
  	 */
  	STARTED("started"), 
  	/**
  	 */
  	VIOLATED("violated"), 
  	/**
  	 */
  	PERFORM_ACTION("perform-action"), 
  	/**
  	 */
  	READY_FULFILLED("ready-fulfilled"), 
  	/**
  	 */
  	FULFILLED("fulfilled"), 
  	/**
  	 */
  	CANCELED("cancelled");

    /**
     * The name of this social commitment state.
     */
    private String name;

    private CommitmentState (String newName) {
      name = newName;
    }

    /**
     * Returns the name of this social commitment state.
     * 
     * #return The name of this social commitment state.
     */
    @Override
    public String toString () {
      return name;
    }
  }

  /**
   * Used to indicate that the perform action event has occurred, but had no
   * effect because it fired before the start event.
   */
  private boolean performActionEventOccurred = false;
  /**
   * Used to indicate that the stop event has occurred, but had no effect because it fired before the agent
   */
  private boolean stopEventOccurred = false;

  /**
   * TODO update this doc
   * Updates the state of this commitment based on the event that has occurred.
   * See the class documentation for a description of the state machine and its
   * transitions.
   * 
   * @param type A string representing the type of event that has occurred.
   * @param event The event that {@link AbstractEvent#fireEvent() fired}.
   * @param instanceInfo An object that details the event occurrence.
   * @see casa.event.EventObserver#notifyEventOccurred(java.lang.String,
   *      casa.event.Event, java.lang.Object)
   */
// void notifyEventOccurred (String type, Event event, Object instanceInfo) {
	@Override
	public void update(Observable o, Object arg) {
		ObserverNotification note = (ObserverNotification)arg; 
		String type = note.getType();
		
		
  	assert(type!=null);

  	if (currentState == CommitmentState.FULFILLED
  			|| currentState == CommitmentState.VIOLATED
  			|| currentState == CommitmentState.CANCELED) {
  		return;
  	}

  	String msg = null;
  	if (ML.EVENT_SC_PERFORM_ACTION.equals(type)) {
  		if (currentState == CommitmentState.STARTED) {
  			currentState = CommitmentState.PERFORM_ACTION;
  	    msg = "Commitment PERFORM_ACTION: ";
  		} else if (currentState == CommitmentState.CREATED) {
  			performActionEventOccurred = true;
  		}
  	}

  	else if (ML.EVENT_SC_START.equals(type)) {
  		if (currentState == CommitmentState.CREATED) {
  			currentState = CommitmentState.STARTED;
  	    agent.println("commitments", "Commitment STARTED: "+toString());
  			if (performActionEventOccurred) {
  				currentState = CommitmentState.PERFORM_ACTION;
  		    msg = "Commitment PERFORM_ACTION: ";
  			}
  			if (stopEventOccurred) {
  				currentState = CommitmentState.READY_FULFILLED;
  		    msg = "Commitment READY_FULFILLED (started): ";
  			}
  		}
  	}

  	else if (ML.EVENT_SC_STOP.equals(type)) {
  		if (currentState == CommitmentState.STARTED
  				|| currentState == CommitmentState.PERFORM_ACTION) {
  			currentState = CommitmentState.READY_FULFILLED;
  	    msg = "Commitment READY_FULFILLED (stopeped): ";
  		} else if (currentState == CommitmentState.CREATED) {
  			stopEventOccurred = true;
  		}
  	}

  	else if (ML.EVENT_SC_VIOLATION.equals(type)) {
  		currentState = CommitmentState.VIOLATED;
      msg = "Commitment VIOLATED: ";
  		endTime = System.currentTimeMillis ();
  	}

    updateStateAtStartup ();
    
    if (msg!=null)
    	agent.println("commitments", msg+toString());
  }

  //// Status flags stuff

  /**
   * An empty set of status flags. If this mask is passed into
   * {@link #meetsMask(Set)}, it will always return {@code true}.
   */
  public static final Set<SocialCommitmentStatusFlags> MASK_ALL = EnumSet.noneOf (SocialCommitmentStatusFlags.class);
  
  /**
   * The set of status flags that, if set, indicate that a social commitment is
   * executable.
   */
  public static final Set<SocialCommitmentStatusFlags> MASK_EXECUTABLE = EnumSet.of (SocialCommitmentStatusFlags.STARTED, SocialCommitmentStatusFlags.NOTBROKEN, SocialCommitmentStatusFlags.NOTFULFILLED, SocialCommitmentStatusFlags.READY,SocialCommitmentStatusFlags.HAS_ACTION);
  /**
   * The set of status flags that, if set, indicate that a social commitment is
   * outstanding.
   */
  public static final Set<SocialCommitmentStatusFlags> MASK_OUTSTANDING = EnumSet.of (SocialCommitmentStatusFlags.NOTBROKEN, SocialCommitmentStatusFlags.NOTFULFILLED);
  /**
   * The set of status flags that, if set, indicate that a social commitment
   * represents an obligation for the debtor of that social commitment.
   */
  public static final Set<SocialCommitmentStatusFlags> MASK_OBLIGATED = EnumSet.of (SocialCommitmentStatusFlags.STARTED, SocialCommitmentStatusFlags.NOTBROKEN, SocialCommitmentStatusFlags.NOTFULFILLED, SocialCommitmentStatusFlags.OBLIGATED);

  /**
   * The set of status flags which do not change when the state of a social
   * commitment changes. This set includes:
   * <ul>
   * <li>{@link SocialCommitmentStatusFlags#DEBTOR}
   * <li>{@link SocialCommitmentStatusFlags#NOTDEBTOR}
   * <li>{@link SocialCommitmentStatusFlags#SHARED}
   * <li>{@link SocialCommitmentStatusFlags#HAS_ACTION}
   * <li>{@link SocialCommitmentStatusFlags#RECURRING}
   * <li>{@link SocialCommitmentStatusFlags#PERSISTENT}
   * <li>{@link SocialCommitmentStatusFlags#OBLIGATED}
   * <li>{@link SocialCommitmentStatusFlags#MARKED}
   * </ul>
   */
  private static Set<SocialCommitmentStatusFlags> STATIC_STATUS_FLAGS = EnumSet.of (SocialCommitmentStatusFlags.DEBTOR,SocialCommitmentStatusFlags.NOTDEBTOR,SocialCommitmentStatusFlags.SHARED,SocialCommitmentStatusFlags.HAS_ACTION,SocialCommitmentStatusFlags.RECURRING,SocialCommitmentStatusFlags.PERSISTENT,SocialCommitmentStatusFlags.OBLIGATED,SocialCommitmentStatusFlags.MARKED);

  /**
   * Sets the {@link SocialCommitmentStatusFlags#DEBTOR DEBTOR} flag to be the
   * same as the given value.
   * 
   * @param isDebtor The new value for the
   *            {@link SocialCommitmentStatusFlags#DEBTOR DEBTOR} flag.
   */
  public void setDebtor (boolean isDebtor) {
    if (isDebtor) {
      statusFlags.add (SocialCommitmentStatusFlags.DEBTOR);
    } else {
      statusFlags.remove (SocialCommitmentStatusFlags.DEBTOR);
    }
  }

  /**
   * Sets the {@link SocialCommitmentStatusFlags#NOTDEBTOR NOTDEBTOR} flag to be
   * the same as the given value.
   * 
   * @param isDebtor The new value for the
   *            {@link SocialCommitmentStatusFlags#NOTDEBTOR NOTDEBTOR} flag.
   */
  public void setNotDebtor (boolean isNotDebtor) {
    if (isNotDebtor) {
      statusFlags.add (SocialCommitmentStatusFlags.NOTDEBTOR);
    } else {
      statusFlags.remove (SocialCommitmentStatusFlags.NOTDEBTOR);      
    }
  }

  /**
   * Sets the {@link SocialCommitmentStatusFlags#SHARED SHARED} flag to be the
   * same as the given value.
   * 
   * @param isDebtor The new value for the
   *            {@link SocialCommitmentStatusFlags#SHARED SHARED} flag.
   */
  public void setShared (boolean isShared) {
    if (isShared) {
      statusFlags.add (SocialCommitmentStatusFlags.SHARED);
    } else {
      statusFlags.remove (SocialCommitmentStatusFlags.SHARED);      
    }
  }

  /**
   * Sets the {@link SocialCommitmentStatusFlags#RECURRING RECURRING} flag to be the
   * same as the given value.
   * 
   * @param isDebtor The new value for the
   *            {@link SocialCommitmentStatusFlags#RECURRING RECURRING} flag.
   *            
   * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
   */
//  public void setRecurring (boolean isRecurring) {
//    if (isRecurring) {
//      statusFlags.add (SocialCommitmentStatusFlags.RECURRING);
//    } else {
//    	if (statusFlags.contains(SocialCommitmentStatusFlags.RECURRING))
//    		statusFlags.remove (SocialCommitmentStatusFlags.RECURRING);      
//    }
//  }

  /**
   * Sets the {@link SocialCommitmentStatusFlags#PERSISTENT PERSISTENT} flag to be the
   * same as the given value.
   * 
   * @param isDebtor The new value for the
   *            {@link SocialCommitmentStatusFlags#PERSISTENT PERSISTENT} flag.
   *            
   * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
   */
  public void setPersistent (boolean isPersistent) {
    if (isPersistent) {
      statusFlags.add (SocialCommitmentStatusFlags.PERSISTENT);
    } else {
    	statusFlags.remove (SocialCommitmentStatusFlags.PERSISTENT);      
    }
  }
  
  /**
   * Sets the {@link SocialCommitmentStatusFlags#MARKED MARKED} flag to be the
   * same as the given value.
   * 
   * @param isDebtor The new value for the
   *            {@link SocialCommitmentStatusFlags#MARKED MARKED} flag.
   */
  public void setMarked (boolean marked) {
    if (marked) {
      statusFlags.add (SocialCommitmentStatusFlags.MARKED);
    } else {
      statusFlags.remove (SocialCommitmentStatusFlags.MARKED);      
    }
  }

  /**
   * Retrieves this social commitment's set of status flags.
   * 
   * @return This social commitment's set of status flags.
   */
  public Set<SocialCommitmentStatusFlags> getFlags () {
    return Collections.unmodifiableSet (statusFlags);
  }

  /**
   * Determines if this social commitment has the given status flag set to
   * {@code true}.
   * 
   * @param flagToCheck The status flag which is to be checked in this social
   *            commitment.
   * @return {@code true} if this social commitment has the given status flag
   *         set to {@code true}; {@code false} otherwise.
   */
  public boolean flagSet (SocialCommitmentStatusFlags flagToCheck) {
    return statusFlags.contains (flagToCheck);
  }

  /**
   * Determines if this social commitment has all of the status flags given in
   * the mask set to {@code true}.
   * 
   * @param mask The set of status flags which are to be checked in this social
   *            commitment.
   * @return {@code true} if this social commitment has all of the status flags
   *         given in the mask set to {@code true}; {@code false} otherwise.
   */
  public boolean meetsMask (Set<SocialCommitmentStatusFlags> mask) {
    return statusFlags.containsAll (mask);
  }

  /**
   * Updates the {@link #statusFlags status flags} based on the current state of
   * the social commitment. This is done by copying the current status flags,
   * retaining only the status flags in {@link #STATIC_STATUS_FLAGS}, and then
   * setting all of the remaining flags conditionally based on the current state
   * of the commitment.
   */
  private void updateStatusWord () {
    EnumSet<SocialCommitmentStatusFlags> tempStatusFlags = EnumSet.copyOf (statusFlags);
    tempStatusFlags.retainAll (STATIC_STATUS_FLAGS);
    tempStatusFlags.add (SocialCommitmentStatusFlags.NOTBROKEN);
    tempStatusFlags.add (SocialCommitmentStatusFlags.NOTFULFILLED);
    tempStatusFlags.add (SocialCommitmentStatusFlags.STARTED);

    switch (currentState) {
      case CREATED:
        tempStatusFlags.remove (SocialCommitmentStatusFlags.STARTED);
      break;
      case PERFORM_ACTION:
        tempStatusFlags.add (SocialCommitmentStatusFlags.READY);
      break;
      case FULFILLED:
        tempStatusFlags.add (SocialCommitmentStatusFlags.ENDED);
        tempStatusFlags.remove (SocialCommitmentStatusFlags.NOTFULFILLED);
        enterTerminalState();
      break;
      case CANCELED:
        tempStatusFlags.add (SocialCommitmentStatusFlags.ENDED);
        tempStatusFlags.remove (SocialCommitmentStatusFlags.NOTFULFILLED);
        enterTerminalState();
      break;
      case VIOLATED:
        tempStatusFlags.add (SocialCommitmentStatusFlags.ENDED);
        tempStatusFlags.remove (SocialCommitmentStatusFlags.NOTBROKEN);
        enterTerminalState();
      break;
    }
    
    //SIDE-EFFECT: if the status word has changed, interrupt the agent
    if (!statusFlags.equals(tempStatusFlags)) {
    	agent.bump();
    }
    
    statusFlags = tempStatusFlags;
  }
  
  /**
   * Called whenever the SocialCommitment enters a terminal state (FILFILLED, CANCELED, or VIOLATED).
   * Subclasses should override this method to clean up as appropriate.
   */
  protected void enterTerminalState() {
    for (Event e : events.values ()) {
      e.deleteObserver(this);
    }
  }

  /**
   * TODO Document the addObserver method. 
   *
   * @param sco
   */
  protected void addObserver (/*SocialCommitmentObserver*/SocialCommitment sco) {
//	  observers.add(sco);
	  super.addObserver(sco);
  }

  /**
   * TODO Document the notifyObservers method. 
   *
   * @param stat
   */
  void notifyObservers (PerformDescriptor stat) {
//    for (SocialCommitmentObserver observer : observers) {
//      observer.update(this,stat);
//    }
  	setChanged();
    super.notifyObservers(new ObserverNotification((AbstractProcess)agent, ML.EVENT_SC_PERFORM_ACTION, stat));
  }

	@Override
	public String getParameter(String key) {
	  return params.get(key);
	}

	@Override
	public Set<String> keySet() {
		return params.keySet();
	}

	@Override
	public void setParameter(String parameter, String value) {
		params.put(parameter, value);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public SocialCommitment clone() {
		try {
			SocialCommitment ret = (SocialCommitment)super.clone();
			ret.params = (TreeMap<String,String>)params.clone();
			return ret;
		} catch (CloneNotSupportedException e) {
		}
		return null;
	}
	
  @SuppressWarnings("unused")
  private static final CasaLispOperator SOCIALCOMMITMENT =
    new CasaLispOperator("SOCIALCOMMITMENT", "\"!Create and return a new social commitment.\" "
    						+"&KEY "
    						+ "(DEBTOR NIL) \"@casa.agentCom.URLDescriptor\" \"!The deptor\""
    						+ "(CREDITOR NIL) \"@casa.agentCom.URLDescriptor\" \"!The creditor\""
    						+ "(PERFORMATIVE NIL) \"@java.lang.String\" \"!The performative of this commitment\""
    						+ "(ACT NIL) \"@casa.Act\" \"!The act of this commitment\""
    						+ "(EVENT NIL) \"@casa.event.Event\" \"!The originating event of this commitemnt\""
    						+ "(ACTION NIL) \"@casa.socialcommitments.Action\" \"!The aciton that this commitment embodies\""
    						+ "(EVENTS NIL) \"@org.armedbear.lisp.Cons\" \"The events associated with this commitment (may be mil)!\""
    						, TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
    {
      @Override
			public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
      	URLDescriptor debtor = (URLDescriptor)params.getJavaObject("DEBTOR");
      	URLDescriptor creditor = (URLDescriptor)params.getJavaObject("CREDITOR");
        String performative = (String)params.getJavaObject("PERFORMATIVE"); 
        Act act = (Act)params.getJavaObject("ACT"); 
        Event event = (Event)params.getJavaObject("EVENT"); 
        Action action = (Action)params.getJavaObject("ACTION");
        Cons eventsCons = (org.armedbear.lisp.Cons)params.getJavaObject("EVENTS");
        Event[] events = (Event[]) eventsCons.copyToArray();
      	return new StatusObject<SocialCommitment>(new SocialCommitment (agent, debtor, creditor, performative, act, event, action, events));
    }
  };


}