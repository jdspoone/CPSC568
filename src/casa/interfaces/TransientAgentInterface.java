package casa.interfaces;

import casa.Act;
import casa.DataStorageDescriptor;
import casa.MLMessage;
import casa.PerformDescriptor;
import casa.ProcessOptions;
import casa.ServiceDescriptor;
import casa.Status;
import casa.StatusMLMessageList;
import casa.StatusURLDescriptorList;
import casa.TransientAgent;
import casa.agentCom.URLDescriptor;
import casa.ontology.Ontology;
import casa.ui.AgentUI;
import casa.util.RunnableWithParameter;

import java.util.Set;
import java.util.Vector;

import javax.swing.JComponent;


/**
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @author  <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 * @version 0.9
 */
public interface TransientAgentInterface extends ProcessInterface, PolicyAgentInterface {

  /////////////////////////////////////////////////////////////////////////////
  // LAC communication/status /////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////
  /**
   * Registers with the LAC that this Agent is running.  That is, tells the LAC
   * that this Agent is up and running.  Also tells the LAC its URL.  When
   * the conversation with the LAC ends, it executes
   * <em>sucessAction()</em> or <em>failureAction</em> as appropriate (but this
   * will happend sometime AFTER the method returns).
   * @param lac  URLDescriptor for the LAC to register with.
   * @return     a Status object describing success or failure
   */
  public Status doRegisterAgentInstance(int port, final RunnableWithParameter<MLMessage> successAction, final RunnableWithParameter<MLMessage> failureAction);
	public Status doRegisterAgentInstance(int lacPort);
  /**
   * Registers this Agent with the LAC.  That is, tells the LAC that this Agent is
   * up and running.
   * @param lac  URLDescriptor for the LAC to register with.
   * @return     a Status object describing success or failure
   */
//  public Status doRegisterAgentType(int port);

  /**
   * Unregisters this Agent from the LAC.  This should always be done before
   * the agent exits.
   * @param request <code>true</code> if the message will be a request;
   * <code>false</code> if it is to be an inform.
   * @return     a Status object describing success or failure
   */
  public Status doUnregisterAgentInstance(boolean request);

  /**
   * Unregisters this Agent type from the LAC's permanent registry.
   * @return     a Status object describing success or failure
   */
  public Status doUnregisterAgentType(int port);

  public StatusURLDescriptorList doFindInstances_sync (String pattern);

  /**
   * If the Agent is registered to a LAC returns true. Otherwise returns false.
   * @return true  if the Agent is registered to a LAC, false if it is not.
   */
  public boolean isRegistered();

  public boolean isInitialized();

  public Set<URLDescriptor> getJoinedCooperationDomains ();

  public Vector<URLDescriptor> getMembers (URLDescriptor cd);
  
  public void removeCooperationDomains(URLDescriptor url);
  
  public StatusURLDescriptorList getInstancesFound();
  
  public Status requestInstances(String pattern);

  /**
   * Returns the URL of the LAC the agent is registered to.
   * @return URLDescriptor of the LAC the agent is registered to.  Null if it isn't registered.
   */
  public URLDescriptor getLACURL();

  /////////////////////////////////////////////////////////////////////////////
  // CDS communication/status /////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////
  /**
   * Retrieves the list of URLDescriptor's from a cooperation domain server.
   * @param cds
   * @return
   * @throws CDSexception
   */
  public Vector /*URLDescriptor*/ doGetCooperationDomains(URLDescriptor cds);

  /////////////////////////////////////////////////////////////////////////////
  // CD communication/status //////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////
  /**
   * Join a cooperation domain.
   * Attempts to join the cooperation domain corresponding to the URLdesciptor
   * <em>cd</em>, and notifies observers if successful.<p>
   * If this agent is already in the cooperation domain, observers are notified of this.
   *
   * @param cd   URLDescriptor of the cooperation domain to join
   * @param cds  URLDescriptor of the cooperation domain server to be set as the server for the
   *             URLdesciptor cd
   * @return     a Status object describing success or failure
   */
  public Status doJoinCD(URLDescriptor cd);

  /**
   * Withdraw from a cooperation domain.
   * Attempts to join the cooperation domain corresponding to the URLdesciptor
   * <em>cd</em>, and notifies observers if successful.<p>
   * If this agent is already in the cooperation domain, observers are notified of this.
   *
   * @param cd   URLDescriptor of the cooperation domain to join
   * @param request <code>true</code> if the message will be a request;
   * <code>false</code> if it is to be an inform.
   * @return     a Status object describing success or failure
   */
  public Status doWithdrawCD (URLDescriptor cd, boolean request);

  /**
   * Attempts to store data with a Cooperation Domain.  This Agent must be
   * a member (successfully joined) the cooperation(??).
   * @param dsd The data descriptor of the data to be stored
   * @param cd  The URLDescriptor of the cooperation domain.
   * @throws CDDataException If the data cannot be successfully stored with the CD.
   */
  public Status doCDPutData(URLDescriptor cdURL, DataStorageDescriptor dsd);

  /**
   * Retrieves the history list from a cooperation domain.  Note that this method
   * is usually implemented in terms of the method {@see getDataCD}.
   * @param url The URLDescriptor of the CD from which to retrieve the history list
   * @return    A Vector of messages forming the history list
   * @throws CDDataException If the retrieval fails
   */
  public StatusMLMessageList doCDGetHistory_sync(URLDescriptor cdURL);

  /**
   * Attempts to retrieve the list of participants in a the specified cooperation domain.
   * @param cd The URLDescriptor of the CD.
   * @return A Vector of URLDescriptor's of agents currently participating in the CD.
   * @throws CDException If the retrieval fails.
   */
  public Status doCDGetMembers(URLDescriptor cd);

  /////////////////////////////////////////////////////////////////////////////
  // YP communication/status //////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////
  /**
   * This method is called when this agent wishes to advertises a service. The service added is advertised on
   * a the YellowPages at the urlDescriptor yp.
   * Adds the URLDescriptor and ServiceDescriptor pair to the bufferOfAdvertisements Hashmap. If the
   * entry is already present in the bufferOfAdvertisements, the function simply returns.
   *
   * @param yp      URLDescriptor of the agent advertising the service
   * @param service ServiceDescriptor of the service being advertised
   * @return        A Status object describing success or failure
   */
  public Status doAdvertise(URLDescriptor yp, ServiceDescriptor service);
  //public void advertise (Status status, URLDescriptor yp, ServiceDescriptor service);

  /**
   * This method is called when this agent wishes to withdraw a previously
   * advertised service from a yellow pages service.
   *
   * @param yp      URLDescriptor of the agent advertising the service
   * @param service ServiceDescriptor of the service being withdrawn
   * @return        A Status object describing success or failure
   */
  public Status doUnadvertise(URLDescriptor yp, ServiceDescriptor service);
  //public void advertise (Status status, URLDescriptor yp, ServiceDescriptor service);

  /**
   * Attempts to search a yellow pages service for a agents matching a particular
   * service descriptor.
   * @param yp          The URLDescriptor of the YP to search
   * @param service     A (perhaps partial) ServiceDescriptor description of a
   *                    service that the YP should match its advertisements against
   * @return            A Vector of URLdescritors of matching services (this might
   *                    be expanded to be ServiceDescriptor/URLDescriptor pairs)
   * @throws YPexception If the retrieval fails.
   */
  public StatusURLDescriptorList doSearchYP(URLDescriptor yp, ServiceDescriptor service);
  //public void search (Status status, URLDescriptor yp, Vector urls);



  /////////////////////////////////////////////////////////////////////////////
  // other agent communication/status /////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////

  /**
   * Returns a default <em>AgentUI</em>.  This will normally be called only if the the
   * -i (interface) qualifier does not appear on the command line.
   * @param args The command line arguments
   * @param userName The name of the user
   * @return some default interface the conforms to <em>AgentUI</em> interface.
   */
  public AgentUI makeDefaultInterface(String[] args, boolean GUI);

  /**
   * A creating process should call this to let the agent know of any user interface
   * it has attached to the agent.  Note that this <em>could</em> be called more
   * than once, which may mean the agent has more than one user interface.  The
   * default behaviour is just to ignore it, since the interface could work
   * via the <em>Observer</em> interface and various <em>set*()<em> and <em>get*()</em>
   * calls on the agent.
   * @param ui the user interface to be attached to the agent.
   */
  public void putUI(AgentUI ui);

  /**
   * Requests an agent to join a particular cooperation domain.
   * @param agent Agent this agent is requesting the join the CD.
   * @param cd    The CD the agent should join.
   * @return      A Status object describing success or failure
   */
  public Status doInviteToCD (URLDescriptor agent, URLDescriptor cd, boolean sense);

  /**
   * Pings another agent.
   * Attempts to ping the agent corresponding to the {@link URLDesciptor}
   * <em>agent</em> by sending a message of the format:
   * <table border="1" bgcolor="gold" cellpadding="3">
   * <tr><td><a href="doc-files/performatives.gif">performative</a> </td><td>request</td></tr>
   * <tr><td>{@link TransientAgent#makeDefaultActs() act}          </td><td>ping</td></tr>
   * </table>
   * Note that his method <b>only</b> sends the message; it relies on
   * {@link #handleReply_ping(MLMessage)} to do the actual processing of the
   * returned reply.
   *
   * @param agent      URLDescriptor of the agent to ping
   * @param timeout    maxiumum time (in milliseconds) to wait for the ping
   *                   response
   * @return           a Status object with value 0 for success, or describing
   *                   the failure otherwise.
   * @throws Exception if there is an error sending the message
   */
  public Status doPing (URLDescriptor agent, long timeout);

  /**
   * Pings another agent synchronously.
   * Attempts to ping the agent corresponding to the {@link URLDesciptor}
   * <em>agent</em> by sending a message of the format:
   * <table border="1" bgcolor="gold" cellpadding="3">
   * <tr><td><a href="doc-files/performatives.gif">performative</a> </td><td>request</td></tr>
   * <tr><td>{@link TransientAgent#makeDefaultActs() act}          </td><td>ping</td></tr>
   * </table>
   * The reply is not verified in any way, but generally should have the
   * format:
   * <table border="1" bgcolor="orange" cellpadding="3">
   * <tr><td><a href="doc-files/performatives.gif">performative</a> </td><td>reply</td></tr>
   * <tr><td>{@link TransientAgent#makeDefaultActs() act}          </td><td>ping</td></tr>
   * </table>
   *
   * @param agent      URLDescriptor of the agent to ping
   * @param timeout    maxiumum time (in milliseconds) to wait for the ping
   *                   response
   * @return           the MLMessage that was recieved in reply to the ping
   * @throws Exception if there is an error sending the message
   */
  public URLDescriptor doPing_sync (URLDescriptor agent, long timeout);

  /**
	 */
  public ProcessOptions getOptions();

  /**
	 * @param options
	 */
  public void setOptions(ProcessOptions options);

  public void updateOptions();

  /**
   * Sends a message to retrieve information about the performatives in the
   * performative type library of another agent.
   * Sends a message of format:
   * <table border="1" bgcolor="gold" cellpadding="3">
   * <tr><td><a href="doc-files/performatives.gif">performative</a> </td><td>request</td></tr>
   * <tr><td>{@link TransientAgent#makeDefaultActs() act}          </td><td>get.performatives</td></tr>
   * <tr><td>language                                        </td><td>{@link doc-files/contentLanguages.html#identifier identifier}</td><td>may be missing if <em>performativeName</em> is null</td></tr>
   * <tr><td>content                                         </td><td><em>performativeName</em> | <em>null</em></td><td>blank or missing requests all performative information</td></tr>
   * </table>
   * The corresponding handler is {@link #handleReply_GetPerformatives(MLMessage)}.
   * @param url The URLDescriptor of an agent to request performative information from
   * @param GET_PERFORMATIVESorGET_ACTS either the act name GET_PERFORMATIVES or GET_ACTS
   * @param typeName the name of the performative, or null to request a complete description of <em>url</em>'s performatives
   * @return A Status object from the sendMessage.
   */
  public Status doGetOnology(URLDescriptor url,
                           String GET_PERFORMATIVESorGET_ACTS, String typeName);

  /**
   * Handles get.performative replies from another agent.  The expected format is:
   * <table border="1" bgcolor="orange" cellpadding="3">
   * <tr><td><a href="doc-files/performatives.gif">performative</a> </td><td>reply</td></tr>
   * <tr><td>{@link TransientAgent#makeDefaultActs() act}          </td><td>get.performatives</td></tr>
   * <tr><td>language     </td><td><a href="doc-files/contentLanguages.html#casa.TypeHierarachy">casa.TypeHierarchy</a></td></tr>
   * <tr><td>content      </td><td><em>a description of the requested performative(s) conforming to <a href="doc-files/contentLanguages.html#casa.TypeHierarachy">casa.TypeHierarchy</a> </em></td></tr>
   * </table>
   * or standard error replies.  If the message is successfully interpreted,
   * updates the local performatives type hierarchy with a call to {@link AbstractProcess.??? } ??
   *
   * @param msg the incoming get.members request message
   * @return a status describing the success or failure of the operation
   */
  public PerformDescriptor release_get_ontology(MLMessage msg);

  /**
   * Returns the String (persistent) representation of the performatives hierarchy
   * for this agent.  This is compatible with the String constructor of TypeHiearchy.
   * @return the String (persistent) representation of the performatives hierarchy
   * for this agent.
   */
  public String getSerializedOntology();
  
  /**
   * Returns the performatives hierarchy for this agent. 
   */
  public Ontology getOntology();

  /**
   * Add the persistent TypeHierarchy data in <em>perfHierarchy</em> to this
   * agent's performative hierarchy.
   * @param perfHierarchy A String containing the persistent for of a TypeHierarchy
   * @return a Status object indicating:
   * <ul>
   * <li> 0: success
   * <li> -1: Attempted insertion of duplicate type node into performatives type hierarchy
   * <li> -2: Malformed content field: Parent node not found in performatives type hierarchy
   * <li> -3: Malformed content field: Parse exception
   * <li> -4: Type information is incompatible with existing performatives hierarchy
   * </ul>
   */
  public Status putExtendedOntology(String perfHierarchy);


  /**
   * Replace this agent's type performative hierarchy with the persistent
   * TypeHierarchy data in <em>types</em>.  If <em>types</em> is not well-formed,
   * a negative status is returned and the original performative hierarchy is
   * left unchanged.
   * @param types A String containing the persistent for of a TypeHierarchy
   * @return a Status object indicating:
   * <ul>
   * <li> 0: success
   * <li> -1: Attempted insertion of duplicate type node into performatives type hierarchy
   * <li> -2: Malformed content field: Parent node not found in performatives type hierarchy
   * <li> -3: Malformed content field: Parse exception
   * </ul>
   */
  public Status putReplacementOntology(String types);

  /**
   * Used to execute a command string at runtime.
   * @param command a command string
   * @param ui an {@link casa.ui.AgentUI} object (may be null).
   * @return a {@link Status} object with the result of the attempted execution.
   */
  public Status executeCommand(String command, AgentUI ui);

//  /**
//   * Returns an Iterator that iterates over all the commands the agent supports.
//   * @return an Iterator that iterates over all the commands the agent supports.
//   */
//  public RTCommandInterpreter.RTCommandInterpreterIterator getCommandInterpreterIterator();

  /**
   * Returns a GUI that can be used to interact with the strategy of this agent.
   * Will return a <code>null</code> value if hasStrategyGUI() returns
   * <code>false</code>.
   *
   * @return A <code>JPanel</code> that is the GUI responsible for interacting
   *         with this agent's strategy.
   */
  public JComponent getStrategyGUI ();

  /**
   * Used to determine if the agent's strategy has a GUI.
   *
   * @return Whether the strategy has a GUI.
   */
  public boolean hasStrategyGUI ();
  
  public boolean isAAct (Act a1, Act a2);

	//TODO should this really go here?
  /**
   * Get a conversation object from the agent's TreeMap of conversations
   * 
   * @param convID
   * @return the <code>Conversation</code> object associated with the <code>convID</code>
   */
  //public GenericConversation getConversation(String convID);
}