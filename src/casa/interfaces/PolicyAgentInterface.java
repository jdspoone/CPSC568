package casa.interfaces;

import casa.Act;
import casa.MLMessage;
import casa.PerformDescriptor;
import casa.ProcessOptions;
import casa.Status;
import casa.agentCom.URLDescriptor;
import casa.event.Event;
import casa.jade.CasaKB;
import casa.ontology.Ontology;
import casa.socialcommitments.SocialCommitment;
import casa.socialcommitments.SocialCommitmentsStore;
import casa.ui.AgentUI;
import casa.util.Trace;

import java.util.Collection;
import java.util.Map;
import java.util.Observer;

import org.armedbear.lisp.LispObject;


/**
 * This interface defines the functions necessary for an object to implement to
 * use the policy objects.
 * 
 * @author Jason Heard
 * @version 0.9
 */
public interface PolicyAgentInterface extends Observer {
  public boolean isAAct (Act a1, Act a2);

  public Status abclEval(String c, Map<String,LispObject> newEnvBindings, AgentUI ui);
  public Status abclEval(String c, Map<String, LispObject> lispBindings);
  public Status abclEval(org.armedbear.lisp.Cons cons, Map<String, LispObject> lispBindings);

  public casa.ProcessOptions getOptions();

  public String getUniqueRequestID ();

  public boolean isLoggingTag (String tag);

  public boolean isA (String s1, String s2);

  public boolean isA (Act s1, String s2);

  public boolean isA (String s1, Act s2);

  public boolean isA (Act s1, Act s2);

  public String getName ();

  public boolean isAPerformative (String p1, String p2);

  public boolean isAPerformative (MLMessage msg, String p2);

  public Ontology getOntology();
  
  public SocialCommitmentsStore getSCStore();

 	/**
   * Debugging or error method: Uses the {@link Trace} object to log the string
   * if appropriate (i.e.: the traceTAg matches a tag that's turned on in the
   * Trace object.
   * 
   * @param traceTag The <em>txt</em> String will be logged if the traceTag
   *          matches a tag that's turned on in the Trace object.
   * @param txt The String to be logged
   */
  public String println (String traceTag, String txt);

  /**
   * Same as {@link #println(String, String)} but appends ex.toString() and
   * prints a stack trace after.
   * 
   * @param traceTag The <em>txt</em> String will be logged if the traceTag
   *          matches a tag that's turned on in the Trace object.
   * @param txt The String to be logged
   * @param ex An Exception object
   * @return The string that was printed
   */
  public String println (String traceTag, String txt, Throwable ex);

  /**
   * Same as {@link #println(String, String)} but appends
   * tempStatus.getExplanation().
   * 
   * @param traceTag The <em>txt</em> String will be logged if the traceTag
   *          matches a tag that's turned on in the Trace object.
   * @param txt The String to be logged
   * @param tempStatus A Status object
   * @return The string that was printed
   */
  public String println (String traceTag, String txt, Status tempStatus);

  /**
   * Returns the URL of the current process
   * 
   * @return the <em>URLDescriptor</em> of the process
   */
  public URLDescriptor getURL ();

  /**
   * Sends the <em>message</em> to the the <em>message.receiver</em> or, if
   * <em>message.receiver</em> is missing or empty, to <em>message.to</em>.
   * 
   * @param message <em>MLMessage</em> of the message being sent, which
   *          contains the info about the recipient in the <em>receiver</em>
   *          or <em>to</em> fields.
   * @return The <em>Status</em> describing the status of the operation:
   *         <li>0 if the message was sent without errors,</li>
   *         <li>-3 if there was an exception thrown during the message sending
   *         process (in this case, the returned object will be a
   *         <em>StatusObject</em> containing an <em>Exception</em> object).</li>
   */
  public Status sendMessage (MLMessage message);

  /**
   * Retrieves whether this agent will use the the Ack (acknowledge) protocol.
   * That is, that this process will add "ML.REQUEST_ACK" to outgoing messages
   * and re-send if the message does not get acknowledged within the reply-by
   * period.
   * 
   * @return true iff the agent uses the Ack protocol
   */
  public boolean getUseAckProtocol ();

  public PerformDescriptor dispatchMsgHandlerMethod (String consider, Act act, MLMessage message);

//  /**
//   * Called to allow an agent to customize its response to a
//   * {@link ML#REQUEST request} message. Agents may customize the result
//   * (usually returning a message) by modifying the return
//   * {@link PerformDescriptor}: 
//   * <li> If {@link PerformDescriptor#getStatus()}.{@link Status#setStatus(int)}
//   * (or the constructor) is set to {@link TransientAgent#DEFER_ACTION} the
//   * action is seen as 'not ready' and will be called again later until some
//   * other status is returned.</li>
//   * <li> If {@link PerformDescriptor#getStatus()}.{@link Status#setStatus(int)}
//   * (or the constructor) is set to {@link TransientAgent#DROP_ACTION} the
//   * action should be ignored and allowed to timeout when REPLY-BY expires.</li>
//   * <li> The
//   * {@link java.util.TreeMap#add(K, V) put()} method may be used to modify any
//   * of the fields (described in {@link ML}) in the message. This allows you to
//   * return an error message, etc.</li>
//   * 
//   * @param act the Act object of the message (should be the same as that
//   *          described in the {@link ML#ACT act} field of <em>message</em>).
//   * @param message the {@link ML#REQUEST request}-type performative message
//   *          that precipitated the call to the method
//   * @return the result as described above.
//   */
//  public PerformDescriptor consider (Act act, MLMessage message);
//
//  /**
//   * Called to allow an agent to customize its response to a
//   * {@link ML#REPLY_REQUEST reply} message. Agents may customize the result
//   * (usually returning a message) by modifying the return
//   * {@link PerformDescriptor}: 
//   * <li> If {@link PerformDescriptor#getStatus()}.{@link Status#setStatus(int)}
//   * (or the constructor) is set to {@link TransientAgent#DEFER_ACTION} the
//   * action is seen as 'not ready' and will be called again later until some
//   * other status is returned.</li> 
//   * <li> If {@link PerformDescriptor#getStatus()}.{@link Status#setStatus(int)}
//   * (or the constructor) is set to {@link TransientAgent#DROP_ACTION} the
//   * action should be ignored and allowed to timeout when REPLY-BY expires.</li>
//   * <li> The
//   * {@link java.util.TreeMap#add(K, V) put()} method may be used to modify any
//   * of the fields (described in {@link ML}) in the message. This allows you to
//   * return an error message, etc.</li>
//   * 
//   * @param act the Act object of the message (should be the same as that
//   *          described in the {@link ML#ACT act} field of <em>message</em>).
//   * @param message the {@link ML#REPLY_REQUEST reply}-type performative
//   *          message that precipitated the call to the method
//   * @return the result as described above.
//   */
//  public PerformDescriptor verify (Act act, MLMessage message);
//
//  /**
//   * Called to allow an agent to customize its response to an <b>outgoing</b>
//   * {@link ML#REQUEST request} message. Agents may customize the result
//   * (usually returning a message) by modifying the return
//   * {@link PerformDescriptor}: 
//   * <li> If {@link PerformDescriptor#getStatus()}.{@link Status#setStatus(int)}
//   * (or the constructor) is set to {@link TransientAgent#DEFER_ACTION} the
//   * action is seen as 'not ready' and will be called again later until some
//   * other status is returned.</li> 
//   * <li> If {@link PerformDescriptor#getStatus()}.{@link Status#setStatus(int)}
//   * (or the constructor) is set to {@link TransientAgent#DROP_ACTION} the
//   * action should be ignored and allowed to timeout when REPLY-BY expires.</li>
//   * <li> The
//   * {@link java.util.TreeMap#add(K, V) put()} method may be used to modify any
//   * of the fields (described in {@link ML}) in the message. This allows you to
//   * return an error message, etc.</li>
//   * 
//   * @param act the Act object of the message (should be the same as that
//   *          described in the {@link ML#ACT act} field of <em>message</em>).
//   * @param message the {@link ML#REQUEST request}-type performative message
//   *          that precipitated the call to the method
//   * @return the result as described above.
//   */
//  public PerformDescriptor perform (Act act, MLMessage message);
//
//  /**
//   * Called to allow an agent to customize its response to a
//   * {@link ML#REQUEST query_if} message. Agents may customize the result
//   * (usually returning a message) by modifying the return
//   * {@link PerformDescriptor}: 
//   * <li> If {@link PerformDescriptor#getStatus()}.{@link Status#setStatus(int)}
//   * (or the constructor) is set to {@link TransientAgent#DEFER_ACTION} the
//   * action is seen as 'not ready' and will be called again later until some
//   * other status is returned.</li> 
//   * <li> If {@link PerformDescriptor#getStatus()}.{@link Status#setStatus(int)}
//   * (or the constructor) is set to {@link TransientAgent#DROP_ACTION} the
//   * action should be ignored and allowed to timeout when REPLY-BY expires.</li>
//   * <li> The
//   * {@link java.util.TreeMap#add(K, V) put()} method may be used to modify any
//   * of the fields (described in {@link ML}) in the message. This allows you to
//   * return an error message, etc.</li>
//   * 
//   * @param act the Act object of the message (should be the same as that
//   *          described in the {@link ML#ACT act} field of <em>message</em>).
//   * @param message the {@link ML#QUERY_IF request}-type performative message
//   *          that precipitated the call to the method
//   * @return the result as described above.
//   */
//  public PerformDescriptor evaluate (Act act, MLMessage message);
//  
//  /**
//   * Called to allow an agent to customize its response to an <b>outgoing</b>
//   * {@link ML#SUBSCRIBE request} message. Agents may customize the result
//   * (usually returning a message) by modifying the return
//   * {@link PerformDescriptor}: 
//   * <li> If {@link PerformDescriptor#getStatus()}.{@link Status#setStatus(int)}
//   * (or the constructor) is set to {@link TransientAgent#DEFER_ACTION} the
//   * action is seen as 'not ready' and will be called again later until some
//   * other status is returned.</li> 
//   * <li> If {@link PerformDescriptor#getStatus()}.{@link Status#setStatus(int)}
//   * (or the constructor) is set to {@link TransientAgent#DROP_ACTION} the
//   * action should be ignored and allowed to timeout when REPLY-BY expires.</li>
//   * <li> The
//   * {@link java.util.TreeMap#add(K, V) put()} method may be used to modify any
//   * of the fields (described in {@link ML}) in the message. This allows you to
//   * return an error message, etc.</li>
//   * 
//   * @param act the Act object of the message (should be the same as that
//   *          described in the {@link ML#ACT act} field of <em>message</em>).
//   * @param message the {@link ML#SUBSCRIBE request}-type performative message
//   *          that precipitated the call to the method
//   * @return the result as described above.
//   */
//  public PerformDescriptor monitor (Act act, MLMessage message);
//
//  /**
//   * Called to allow an agent to customize its response to a
//   * {@link ML#INFORM inform} message. Agents may customize the result (usually
//   * returning a message) by modifying the return {@link PerformDescriptor}:
//   * <li> If {@link PerformDescriptor#getStatus()}.{@link Status#setStatus(int)}
//   * (or the constructor) is set to {@link TransientAgent#DEFER_ACTION} the
//   * action is seen as 'not ready' and will be called again later until some
//   * other status is returned.</li>
//   * <li> If {@link PerformDescriptor#getStatus()}.{@link Status#setStatus(int)}
//   * (or the constructor) is set to {@link TransientAgent#DROP_ACTION} the
//   * action should be ignored and allowed to timeout when REPLY-BY expires.</li>
//   * <li> The {@link java.util.TreeMap#add(K, V) put()} method may be used to modify any
//   * of the fields (described in {@link ML}) in the message. This allows you to
//   * return an error message, etc.</li>
//   * 
//   * @param act the Act object of the message (should be the same as that
//   *          described in the {@link ML#ACT act} field of <em>message</em>).
//   * @param message the {@link ML#INFORM inform}-type performative message that
//   *          precipitated the call to the method
//   * @return the result as described above.
//   */
//  public PerformDescriptor accept (Act act, MLMessage message);
//
//  /**
//   * Called to allow an agent to customize its response to a
//   * {@link ML#PROPOSE_DISCHARGE request}/{@link TransientAgentInterface#ML.DISCHARGE}
//   * message. Agents may customize the result (usually returning a message) by
//   * modifying the return {@link PerformDescriptor}: 
//   * <li> If
//   * {@link PerformDescriptor#getStatus()}.{@link Status#setStatus(int)} (or
//   * the constructor) is set to {@link TransientAgent#DEFER_ACTION} the action
//   * is seen as 'not ready' and will be called again later until some other
//   * status is returned.</li>
//   * <li> If {@link PerformDescriptor#getStatus()}.{@link Status#setStatus(int)}
//   * (or the constructor) is set to {@link TransientAgent#DROP_ACTION} the
//   * action should be ignored and allowed to timeout when REPLY-BY expires.</li>
//   * <li> The {@link java.util.TreeMap#add(K, V) put()} method may be used to modify any
//   * of the fields (described in {@link ML}) in the message. This allows you to
//   * return an error message, etc.</li>
//   * 
//   * @param act the Act object of the message (should be the same as that
//   *          described in the {@link ML#ACT act} field of <em>message</em>).
//   * @param message the {@link ML#PROPOSE_DISCHARGE request}/{@link TransientAgentInterface#ML.DISCHARGE}-type
//   *          performative/act message that precipitated the call to the method
//   * @return the result as described above.
//   */
//  public PerformDescriptor release (Act act, MLMessage message);
//
//  /**
//   * Called to allow an agent to customize its response to a
//   * {@link ML#REPLY_PROPOSAL reply-proposal} message. Agents may customize the
//   * result (usually returning a message) by modifying the return
//   * {@link PerformDescriptor}: <li> If {@link PerformDescriptor#getStatus()}.{@link Status#setStatus(int)}
//   * (or the constructor) is set to {@link TransientAgent#DEFER_ACTION} the
//   * action is seen as 'not ready' and will be called again later until some
//   * other status is returned.</li> 
//   * <li> If {@link PerformDescriptor#getStatus()}.{@link Status#setStatus(int)}
//   * (or the constructor) is set to {@link TransientAgent#DROP_ACTION} the
//   * action should be ignored and allowed to timeout when REPLY-BY expires.</li>
//   * <li> The
//   * {@link java.util.TreeMap#add(K, V) put()} method may be used to modify any
//   * of the fields (described in {@link ML}) in the message. This allows you to
//   * return an error message, etc.</li>
//   * 
//   * @param act the Act object of the message (should be the same as that
//   *          described in the {@link ML#ACT act} field of <em>message</em>).
//   * @param message the {@link ML#REPLY_PROPOSAL reply-proposal}-type
//   *          performative message that precipitated the call to the method
//   * @return the result as described above.
//   */
//  public PerformDescriptor conclude (Act act, MLMessage message);
//
//  /**
//   * Called to allow an agent to determine its response to a
//   * {@link ML#PROXY proxy} message. Agents determine which agent to forward the
//   * encoded message to by modifying the return {@link PerformDescriptor}. In
//   * order for the message to be forwarded, a {@link PerformDescriptorHolder}
//   * must be returned containing a {@link Pair} object containing first a
//   * {@link List} of {@link URLDescriptor}s and then a {@link MLMessage}.
//   * 
//   * @param act the Act object of the message (should be the same as that
//   *          described in the {@link ML#ACT act} field of <em>message</em>).
//   * @param message the {@link ML#PROXY proxy}-type performative message that
//   *          Precipitated the call to the method
//   * @return the result as described above.
//   */
//  public PerformDescriptor assemble (Act act, MLMessage message);

  /*
   * Inserts <em>msg</em> input this list of outstanding requests.<br>
   * <b>Precondition</b>: <em>msg</em> needs to be a sub-type of
   * {@link ML#REQUEST}.
   * 
   * @param msg
   */
  //public void addRequest (MLMessage msg);

  public Event[] getSubscribeEvents (Event event);

  /**
   * Can be used to defer a section of code for later execution during the
   * agent's idle time.
   * 
   * @param x some Runnable to execute
   */
  public void defer (Runnable runnable);
  
  public CasaKB getKnowledgeBase();
  
	/**
	 * This is the catch-all method called when {@link ProcessOptions#observeMessages} 
	 * is set true and the handler method (the regular method concatenated with
	 * "_evesdrop") is not found.  Subclasses should override this to implement
	 * the desired behaviour.  This implementation does nothing but return a
	 * {@link PerformDescriptor} with a {@link #DROP_ACTION} Status, which 
	 * indicates no action should be taken.
	 * @param msg An incoming message that is not addressed to this agent.
	 * @return null
	 */
	public PerformDescriptor evesdrop(MLMessage msg);
	
	/**
	 * Put the Event on the agent's event queue.
	 * @param event The event to queue.
	 */
	public void queueEvent(Event event);

	/**
	 * Put the Event on the agent's event queue 
	 * iff the event is not already on the queue.
	 * @param event The event to queue.
	 */
	public void queueEventIf(Event event);

	
	/**
	 * Remove the event from the agent's event queue
	 * @param event The event to dequeue.
	 */
	public void dequeueEvent(Event event);
	
//	public abstract RTCommandInterpreter.RTCommandInterpreterIterator getCommandInterpreterIterator();

	public abstract Status executeCommand(String command, AgentUI ui);

	/**
	 * @param unmarked a set of SocialCommitments to choose one from
	 * @return A social commitment to work on next from the parameter collection; or null to leave the selection to the caller
	 */
	public SocialCommitment chooseSC(Collection<SocialCommitment> SocialCommitments);

  public void addTransformation(Transformation trans);
  public Transformation getTransformationFor(Describable d);
  public Describable transform(Describable d) ;
  public Describable revTransform(Describable d);
  public String transform(String d);
  public String revTransform(String d);
	public void bump();

	public void notifyObservers(String string, Object policy);


}