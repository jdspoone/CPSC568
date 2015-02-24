package casa.interfaces;

import casa.CasaObservable;
import casa.MLMessage;
import casa.ProcessOptions;
import casa.Status;
import casa.agentCom.URLDescriptor;
import casa.util.Trace;
import casa.util.TraceInterface;

import java.util.Observer;

/**
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public interface ProcessInterface extends MessageSender, Runnable, AgentPrintInterface, CasaObservable {
  /**
   * Returns the URL of the current process
   * @return the <em>URLDescriptor</em> of the process
   */
  public URLDescriptor getURL();

  /**
   * Returns the name of the current process
   * @return the name of this agent
   */
  public String getAgentName();

  /**
   * Returns the port that this agent is listening on.
   * @return an <em>int</em> port number
   */
  public int getPort();

  /**
   * Returns the strategy that this agent uses to interpret messages.
   * @return an <em>int</em> port number
   */
  public String getStrategy();

  /**
   * Tests if this process has any open ports.
   * @return <code>true</code> if and only if the process has an open port;
   * <code>false</code> otherwise.
   */
  public boolean hasOpenPort();
  /**
   * Method to close the module's open port and return it to the system.
   */
  public void closePort ();

  /**
   * determines if this process can be stopped.  A subclass may override if there
   * are more constraints on stopping.
   * @return true if the process is stoppable, false otherwise.
   */
  public boolean isStoppable();
  /**
   * Tells the process to exit.  Note that the process may not exit right away;
   * generally, it must check to see that {@link #isStoppable()} is true before
   * it can actually exit.
   */
  public void exit ();
  /**
   * determines if this process is in the process of exiting.  This returns the value of the exit field.
   * @return true if the process is exiting, false otherwise.
   */
  public boolean isExiting();
  
  /**
   * Part of the <em>Observable</em> interface from the observer pattern (Gamma et al.).
   * Call to add an observer to this Observable's list of Observers.  Observers
   * will be notified by calling thier <em>update()</em> method when this object
   * changes.
   * @param observer the Observer object to add.
   */
  public void addObserver(Observer observer);
  /**
   * Extension to the <em>Observable</em> interface from the observer pattern (Gamma et al.).
   * Call to add a remote agent observer to this Observable's list of remote Observers.  Observers
   * will be notified by send them an <em>update()</em> message when this object
   * changes.
   * @param observer the Observer object to add.
   */
  public void addObserver(URLDescriptor observer);
  /**
   * Part of the <em>Observable</em> interface from the observer pattern (Gamma et al.).
   * Call to remove an observer from this object's list of observers.
   * @param observer
   */
  public void deleteObserver(Observer observer);
  /**
   * Extension to the <em>Observable</em> interface from the observer pattern (Gamma et al.).
   * Call to remove a remote observer from this object's list of remote observers.
   * @param observer
   */
  public void deleteObserver(URLDescriptor observer);
  /**
   * Sends the <em>message</em> to the the <em>message.receiver</em> or, if
   * <em>message.receiver</em> is missing or empty, to <em>message.to</em>.
   *
   * @param message   <em>MLMessage</em> of the message being sent, which
   * contains the info about the recipient in the <em>receiver</em> or
   * <em>to</em> fields.
   * @return The <em>Status</em> describing the status of the opporation:
   * <li>0 if the message was sent without errors,</li>
   * <li>-3 if there was an exception thrown during the message sending process
   * (in this case, the returned object will be a <em>StatusObject</em>
   * containing an <em>Exception</em> object).</li>
   */
  public Status sendMessage(MLMessage message);


  /**
   * Creates a basic message with the given performative, act, and receiver.
   * Then it automatically adds sender, reply-with, and reply-by parameters to
   * the message.
   *
   * @param performative The performative of the new message.
   * @param act The act of the new message.
   * @param reciever The receiver of the new message.
   * @return The new message with the given performative, act, and receiver and
   * correct sender, reply-with, and reply-by fields.
   */
  public MLMessage getNewMessage (String performative, String act,
                                  URLDescriptor reciever);

  /**
	 * Returns a reference to this object's  {@link ProcessOptions}  object.
	 * @return  an reference to this object's  {@link ProcessOptions}  object.
	 */
  public ProcessOptions getOptions();
  /**
	 * Sets this object's ProcessOptions object.
	 * @param  options
	 */
  public void setOptions(ProcessOptions options);
  /**
   * Save the options to a persistent database.  Subclasses should override as appropriate.
   */
  public void updateOptions();
  /**
   * Refresh agent behaviour from the options object.  Subclasses should override as appropriate.
   */
  public void realizeAgentBehaviourFromOptions();
  /**
	 * Retrieves whether this agent will use the the Ack (acknowledge) protocol. That is, that this process will add "ML.REQUEST_ACK" to outgoing messages and resend if the message does not get acknowledged within the reply-by period.
	 * @return  true iff the agent uses the Ack protocol
	 */
  public boolean getUseAckProtocol();

  /**
	 * Sets whether this agent will use the the Ack (acknowledge) protocol. That is, that this process will add "ML.REQUEST_ACK" to outgoing messages and resend if the message does not get acknowledged within the reply-by period.
	 * @param b  true to set the protocol, false to not use the protocol.
	 * @return  true iff the agent used the Ack protocol (before the current setting);
	 */
  public boolean setUseAckProtocol(boolean b);

  /**
	 * is tracing on?
	 */
  public boolean isTracing();

//  /**
//	 * replace / set the current  {@link Trace}  object.  If the new trace is null, then tracing is automatically turned off.
//	 */
//  public void setTrace(Trace newTrace);

  /**
	 * get the associated  {@link Trace}  object.  If isTracing() == true, then this is guaranteed non-null, otherwise, this may return null.
	 */
  public TraceInterface getTrace();

  /**
	 * turn tracing on / off
	 */
  public void setTracing(boolean doTrace);

  /**
       * Display the trace monitor window.  It will load with any history if trace is
   * already on.  If trace is off, it will be turned on.
   */
  public void startTraceMonitor();

  /**
   * Debugging or error method: Uses the {@link Trace} object to log the
   * string if appropriate (ie: the traceTAg matches a tag that's turned on
   * in the Trace object.
   * @param traceTag The <em>txt</em> String will be logged if the traceTag maches a tag that's turned on in the Trace object.
   * @param txt The String to be logged
   */
  public String println(String traceTag, String txt);

  /**
       * Same as {@link #println(String, String)} but appends ex.toString() and prints
   * a stack trace after.
   * @param traceTag The <em>txt</em> String will be logged if the traceTag maches a tag that's turned on in the Trace object.
   * @param txt The String to be logged
   * @param ex An Exception object
   */
  public String println(String traceTag, String txt, Throwable ex);

  /**
   * Same as {@link #println(String, String)} but appends tempStatus.getExplanation().
   * @param traceTag The <em>txt</em> String will be logged if the traceTag maches a tag that's turned on in the Trace object.
   * @param txt The String to be logged
   * @param tempStatus A Status object
   */
  public String println(String traceTag, String txt, Status tempStatus);


  public String getUniqueRequestID ();
  public boolean isLoggingTag (String tag);
  public boolean isA (String s1, String s2);
  public String getName ();
  public boolean isAPerformative (String p1, String p2);
}

