package casa;

import casa.CASAProcess.ProcessInfo;
import casa.abcl.CasaLispOperator;
import casa.abcl.LispException;
import casa.abcl.ParamsMap;
import casa.agentCom.URLDescriptor;
import casa.conversation2.Conversation;
import casa.event.AbstractEvent;
import casa.event.Event;
import casa.event.EventObserverEvent;
import casa.event.MessageEvent;
import casa.event.MessageEventDescriptor;
import casa.exceptions.IPSocketException;
import casa.exceptions.IllegalOperationException;
import casa.exceptions.URLDescriptorException;
import casa.extensions.ExtensionLoader;
import casa.interfaces.Describable;
import casa.interfaces.TransientAgentInterface;
import casa.jade.CasaKB;
import casa.jade.SingleNumValueDefinition;
import casa.jade.SingleValueDefinition;
import casa.ontology.Ontology;
import casa.ontology.owl2.OWLOntology;
import casa.policy.AbstractPolicy;
import casa.policy.Policy;
import casa.policy.PolicyContainer;
import casa.policy.sc3.ConcreteCommitmentProcessor;
import casa.socialcommitments.SocialCommitment;
import casa.socialcommitments.SocialCommitmentStatusFlags;
import casa.socialcommitments.SocialCommitmentsStore;
import casa.ui.AbstractInternalFrame;
import casa.ui.AgentUI;
import casa.ui.BufferedAgentUI;
import casa.ui.TextInterface;
import casa.ui.TransientAgentInternalFrame;
import casa.util.AgentLookUpTable;
import casa.util.AnnotationUtil;
import casa.util.CASAUtil;
import casa.util.InstanceCounter;
import casa.util.JarLoader;
import casa.util.Pair;
import casa.util.PairComparable;
import casa.util.Runnable1;
import casa.util.RunnableWithParameter;
import casa.util.Trace;

import jade.core.AID;
import jade.semantics.interpreter.Tools;
import jade.semantics.kbase.QueryResult;
import jade.semantics.kbase.filters.std.assertion.AllIREFilter;
import jade.semantics.lang.sl.grammar.BelieveNode;
import jade.semantics.lang.sl.grammar.Content;
import jade.semantics.lang.sl.grammar.ContentExpression;
import jade.semantics.lang.sl.grammar.DateTimeConstantNode;
import jade.semantics.lang.sl.grammar.EqualsNode;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.FormulaContentExpressionNode;
import jade.semantics.lang.sl.grammar.IdentifyingExpression;
import jade.semantics.lang.sl.grammar.IntegerConstant;
import jade.semantics.lang.sl.grammar.ListOfContentExpression;
import jade.semantics.lang.sl.grammar.ListOfTerm;
import jade.semantics.lang.sl.grammar.ModalLogicFormula;
import jade.semantics.lang.sl.grammar.Node;
import jade.semantics.lang.sl.grammar.NotNode;
import jade.semantics.lang.sl.grammar.PredicateNode;
import jade.semantics.lang.sl.grammar.RealConstantNode;
import jade.semantics.lang.sl.grammar.StringConstant;
import jade.semantics.lang.sl.grammar.SymbolNode;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.grammar.TermSetNode;
import jade.semantics.lang.sl.grammar.VariableNode;
import jade.semantics.lang.sl.parser.SLParser;
import jade.semantics.lang.sl.tools.MatchResult;
import jade.semantics.lang.sl.tools.SL;

import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;

import org.armedbear.lisp.Condition;
import org.armedbear.lisp.Cons;
import org.armedbear.lisp.ControlTransfer;
import org.armedbear.lisp.DoubleFloat;
import org.armedbear.lisp.Environment;
import org.armedbear.lisp.Fixnum;
import org.armedbear.lisp.JavaObject;
import org.armedbear.lisp.Lisp;
import org.armedbear.lisp.LispInteger;
import org.armedbear.lisp.LispObject;
import org.armedbear.lisp.SimpleString;
import org.armedbear.lisp.Symbol;
import org.ksg.casa.CASA;

// OLD VERSION JAVADOC:
//* This agent can handle the following messages types peer-to-peer:  
//* <table border="1" bgcolor="gold" cellpadding="3"> 
//* <tr bgcolor="tan"> <th colspan="2">Message Type</th> <th>            To Send</th> <th colspan="5"><a href=doc-files/conversations.html>Handler Methods</a></th> </tr> 
//* <tr bgcolor="tan"> <th><a href="doc-files/performatives.gif">performative</a></th> <th>act</th> <th><em>do method</em></th> <th> {@link ML#CONSIDER  consider} </th> <th> {@link ML#VERIFY  verify} </th> <th> {@link ML#PERFORM  perform} / {@link ML#MONITOR  monitor} </th> <th> {@link ML#RELEASE  release} / {@link ML#ACCEPT  accept} </th> <th> {@link ML#CONCLUDE  conclude} </th> </tr> 
//* <tr> <td> {@link ML#REQUEST  request} </td> <td> {@link ML#PING  ping} </td> <td> {@link #doPing(URLDescriptor,long)  doPing} ,  {@link #doPing_sync(URLDescriptor,long)  doPing_sync} </td> <td> {@link #consider_ping(MLMessage)  consider_ping} </td> <td>(<code>consider</code> shortcuts)</td> <td>(<code>consider</code> shortcuts)</td> <td> {@link #release_ping(MLMessage)  release_ping} </td> <td> {@link #concludeDefault(Act,MLMessage)  DEFAULT} </td> </tr> 
//* <tr> <td> {@link ML#REQUEST  request}  /  {@link ML#INFORM  inform} </td> <td> {@link ML#EXECUTE  execute} </td> <td>-</td> <td> {@link #consider_execute(MLMessage)  consider_execute} </td> <td> {@link #verify_execute(MLMessage)  verify_execute} </td> <td> {@link #perform_execute(MLMessage)  perform_execute} </td> <td> {@link #release_execute(MLMessage)  release_execute} </td> <td> {@link #conclude_execute(MLMessage)  conclude_execute} </td> </tr> 
//* <tr> <td> {@link ML#REQUEST  request} </td> <td> {@link ML#GET_NAME  get_name} </td> <td>-</td> <td> {@link #considerDefault(Act,MLMessage)  DEFAULT} </td> <td> {@link #verifyDefault(MLMessage)  DEFAULT} </td> <td> {@link #perform_get_name(MLMessage)  perform_get_name} </td> <td> {@link #releaseDefault(Act,MLMessage)  DEFAULT} </td> <td> {@link #concludeDefault(Act,MLMessage)  DEFAULT} </td> </tr> 
//* <tr> <td> {@link ML#REQUEST  request} </td> <td> {@link ML#GET_ONTOLOGY  get_onology} </td> <td> {@link #doGetOnology(URLDescriptor,String,String)  doGetOntology} </td> <td> {@link #considerDefault(Act,MLMessage)  DEFAULT} </td> <td> {@link #verifyDefault(MLMessage)  DEFAULT} </td> <td> {@link #perform_get_ontology(MLMessage)  perform_get_ontology} </td> <td> {@link #release_get_ontology(MLMessage)  release_get_ontology} </td> <td> {@link #concludeDefault(Act,MLMessage)  DEFAULT} </td> </tr> 
//* <tr> <td> {@link ML#REQUEST  request}  /  {@link ML#INFORM  inform} </td> <td> {@link ML#INVITE_CD  invite_cd} </td> <td> {@link #doInviteToCD(URLDescriptor,URLDescriptor,boolean)  doInviteToCD}  </td> <td> {@link #consider_invite_to_cd(MLMessage)  consider_invite_to_cd} </td> <td> {@link #verifyDefault(MLMessage)  DEFAULT} </td> <td> {@link #perform_invite_to_cd(MLMessage)  perform_invite_to_cd} </td> <td> {@link #releaseDefault(Act,MLMessage)  DEFAULT} </td> <td> {@link #concludeDefault(Act,MLMessage)  DEFAULT} </td> </tr>
//* <tr> <td colspan=8>&nbsp;&nbsp;&nbsp;&nbsp;&uarr; Good example of using  {@link #DEFER_ACTION}  to handle a service that is dependent on something else (in this case a request to another agent)</td> </tr> <tr> <td> {@link ML#REQUEST  request}  /  {@link ML#INFORM  inform} </td> <td> {@link ML#NEW_COMMAND  new_MenuItem} </td> <td>-</td> <td> {@link #consider_new_MenuItem(MLMessage)  consider_new_MenuItem} </td> <td>(<code>consider</code> shortcuts)</td> <td>(<code>consider</code> shortcuts)</td> <td> {@link #releaseDefault(Act,MLMessage)  DEFAULT} </td> <td> {@link #concludeDefault(Act,MLMessage)  DEFAULT} </td> </tr> 
//* <tr> <td> {@link ML#REQUEST  request}  /  {@link ML#INFORM  inform} </td> <td> {@link ML#FORWARD_MESSAGE  forward_message} </td> <td>-</td> <td> {@link #accept_forward_message(MLMessage)  consider_forward_message} <br>needs fixing</td> <td> {@link #verifyDefault(MLMessage)  DEFAULT} </td> <td> {@link #performDefault(Act,MLMessage)  DEFAULT} </td> <td> {@link #releaseDefault(Act,MLMessage)  DEFAULT} </td> <td> {@link #concludeDefault(Act,MLMessage)  DEFAULT} </td> </tr> 
//* <tr> <td> {@link ML#REQUEST  request}  /  {@link ML#INFORM  inform} </td> <td> {@link ML#METHOD_CALL  method_call} </td> <td>-</td> <td> {@link #considerDefault(Act,MLMessage)  DEFAULT} </td> <td> {@link #verifyDefault(MLMessage)  DEFAULT} </td> <td> {@link #perform_method_call(MLMessage)  perform_proxy} </td> <td> {@link #releaseDefault(Act,MLMessage)  DEFAULT} </td> <td> {@link #concludeDefault(Act,MLMessage)  DEFAULT} </td> </tr> 
//* <!--- <tr> <td> {@link ML#REQUEST  request}  /  {@link ML#INFORM  inform} </td> <td> {@link ML#xx} </td> <td> {@link  #do do} </td> <td> {@link #considerDefault(Act,MLMessage)  DEFAULT} </td> <td> {@link #verifyDefault(MLMessage)  DEFAULT} </td> <td> {@link #performDefault(Act,MLMessage)  DEFAULT} </td> <td> {@link #releaseDefault(Act,MLMessage)  DEFAULT} </td> <td> {@link #concludeDefault(Act,MLMessage)  DEFAULT} </td> </tr> </!--->
//*  </table> <br> 
//*  This agent can handle the following message types as a client: 
//*  <table border="1" bgcolor="gold" cellpadding="3"> 
//*  <tr bgcolor="tan"> <th colspan="2">Message Type</th> <th>            To Send</th> <th colspan="2"><a href=doc-files/conversations.html>Handler Methods</a></th> </tr> 
//*  <tr bgcolor="tan"> <th><a href="doc-files/performatives.gif">performative</a></th> <th>act</th> <th><em>do method</em></th> <th> {@link ML#VERIFY  verify} </th> <th> {@link ML#RELEASE  release} / {@link ML#ACCEPT  accept} </th> </tr> 
//*  <tr> <td colspan="5" bgcolor="darkorange">LAC intereaction</td> </tr> 
//*  <tr> <td> {@link ML#REQUEST  request}  /  {@link ML#INFORM  inform} </td> <td> {@link ML#REGISTER_INSTANCE  register_instance} </td> <td> {@link #doRegisterAgentInstance(int)  doRegisterAgentInstance} </td> <td> {@link #verifyDefault(MLMessage)  DEFAULT} </td> <td> {@link #release_register_instance(MLMessage)  release_register_instance} </td> </tr> 
//*  <tr> <td> {@link ML#REQUEST  request}  /  {@link ML#INFORM  inform} </td> <td> {@link ML#UNREGISTER_INSTANCE  unregister_instance} </td> <td> {@link #doUnregisterAgentInstance(boolean)  doUnregisterAgentInstance} </td> <td> {@link #verifyDefault(MLMessage)  DEFAULT} </td> <td> {@link #release_unregister_instance(MLMessage)  release_unregister_instance} </td> </tr> 
//*  <tr> <td> {@link ML#REQUEST  request}  /  {@link ML#INFORM  inform} </td> <td> {@link ML#REGISTER_AGENTTYPE  register_agentType} </td> <td> {@link #doRegisterAgentType(int)  doRegisterAgentType()} ,  {@link #doRegisterAgentType(URLDescriptor,String,RunDescriptor)  doRegisterAgentType(,)} </td> <td> {@link #verifyDefault(MLMessage)  DEFAULT} </td> <td> {@link #release_register_agentType(MLMessage)  release_register_agentType} </td> </tr> 
//*  <tr> <td> {@link ML#REQUEST  request}  /  {@link ML#INFORM  inform} </td> <td> {@link ML#UNREGISTER_AGENTTYPE  unregister_agentType} </td> <td> {@link #doUnregisterAgentType(int)  doUnregisterAgentType} </td> <td> {@link #verifyDefault(MLMessage)  DEFAULT} </td> <td> {@link #release_unregister_agentType(MLMessage)  release_unregister_agentType} </td> </tr> 
//*  <tr> <td> {@link ML#REQUEST  request} </td> <td> {@link ML#RESOLVE_URL  resolve_url} </td> <td> {@link #doResolveURL_sync(URLDescriptor,long)} </td> <td>-</td> </tr> 
//*  <tr> <td> {@link ML#REQUEST  request} </td> <td> {@link ML#FIND_INSTANCE  find_instance} </td> <td> {@link #doFindInstances_sync(String)  doFindInstances_sync} </td> <td> {@link #verifyDefault(MLMessage)  DEFAULT} </td> <td> {@link #releaseDefault(Act,MLMessage)  DEFAULT} </td> </tr> <tr> <td colspan="5" bgcolor="darkorange">CD intereaction</td> </tr> 
//*  <tr> <td> {@link ML#REQUEST  request}  /  {@link ML#INFORM  inform} </td> <td> {@link ML#JOIN_CD  join_cd} </td> <td> {@link #doJoinCD(URLDescriptor)  doJoinCD} </td> <td> {@link #verifyDefault(MLMessage)  DEFAULT} </td> <td> {@link #release_join_cd(MLMessage)  release_join_cd} </td> </tr> 
//*  <tr> <td> {@link ML#REQUEST  request}  /  {@link ML#INFORM  inform} </td> <td> {@link ML#WITHDRAW_CD  withdraw_cd} </td> <td> {@link #doWithdrawCD(URLDescriptor,boolean)  doWithdrawCD} </td> <td> {@link #verifyDefault(MLMessage)  DEFAULT} </td> <td> {@link #release_withdraw_cd(MLMessage)  release_withdraw_cd} </td> </tr> 
//*  <tr> <td> {@link ML#REQUEST  request} </td> <td> {@link ML#GET_MEMBERS  get_members} </td> <td> {@link #doCDGetMembers(URLDescriptor)  doCDGetMembers} </td> <td> {@link #verifyDefault(MLMessage)  DEFAULT} </td> <td> {@link #release_get_members(MLMessage)  release_get_members} </td> </tr> 
//*  <tr> <td> {@link ML#REQUEST  request} </td> <td> {@link ML#GET_DATA  get_data} </td> <td colspan=2><em>not implemented</em></td> </tr> 
//*  <tr> <td> {@link ML#REQUEST  request} </td> <td> {@link ML#GET_HISTORY  get_history} </td> <td> {@link #doCDGetHistory_sync(URLDescriptor)  doCDGetHistory_sync} </td> <td colspan=2><em>not implemented</em></td> </tr> 
//*  <tr> <td> {@link ML#REQUEST  request}  /  {@link ML#INFORM  inform} </td> <td> {@link ML#PUT_DATA  put_data} </td> <td> {@link #doCDPutData(URLDescriptor,DataStorageDescriptor)  doCDPutData} </td> <td> {@link #verifyDefault(MLMessage)  DEFAULT} </td> <td> {@link #handleReply_CDPutData(MLMessage)} </td> </tr> 
//*  <tr> <td> {@link ML#SUBSCRIBE  subscribe} </td> <td> {@link ML#OBSERVE_MEMBERSHIP  obeserve_membership} </td> <td> {@link #doCDSubscribeMembership(URLDescriptor,boolean)  doCDObserveMembership} </td> <td> {@link #verifyDefault(MLMessage)  DEFAULT} </td> <td> {@link TransientAgent#accept_membership_change(MLMessage)} <br> {@link #releaseDefault(Act,MLMessage)  RELEASE DEFAULT} <br> {@link #concludeDefault(Act,MLMessage)  CONCLUDE DEFAULT} </td> </tr> 
//*  <tr> <td colspan="5" bgcolor="darkorange">YP intereaction</td> </tr> 
//*  <tr> <td> {@link ML#REQUEST  request}  /  {@link ML#INFORM  inform} </td> <td></td> <td colspan="3"> {@link #doAdvertise(URLDescriptor,ServiceDescriptor)} <em>not implemented</em></td> </tr> 
//*  <tr> <td> {@link ML#REQUEST  request}  /  {@link ML#INFORM  inform} </td> <td></td> <td colspan="3"> {@link #doUnadvertise(URLDescriptor,ServiceDescriptor)} <em>not implemented</em></td> </tr> 
//*  <tr> <td> {@link ML#REQUEST  request} </td> <td></td> <td colspan="3"> {@link #doSearchYP(URLDescriptor,ServiceDescriptor)} <em>not implemented</em></td> </tr> <tr> <!---> 
//*  <tr> <td> {@link ML#REQUEST  request}  /  {@link ML#INFORM  inform} </td> <td> {@link ML#xx} </td> <td> {@link  #do do} </td> <td> {@link #verifyDefault(MLMessage)  DEFAULT} </td> <td> {@link #releaseDefault(Act,MLMessage)  DEFAULT} </td> </tr> 
//*  </!---> </table> <br> This class also implements some handy utility methods for message handling: <table border="1" cellpadding="3"> 
//*  <tr> <td> {@link #sendMessage(String,String,URLDescriptor,String[])  sendMessage()} </td> <td>Construct and send and message using an array of key/value pairs.</td> </tr> 
//*  <tr> <td> {@link #sendRequestAndWait(String,String,URLDescriptor,String[])  sendMessage_sync()} </td> <td>Construct and send and message using an array of key/value pairs, wait for the reply to return.</td> </tr> 
//*  <tr> <td> {@link #verifyMessage(MLMessage,String[])  verifyMessage()} </td> <td>Verify the fields of a message using an array of key/value pairs, and retrieves the content value as an object for <a href="doc-files/contentLanguages.html">"casa." languages </a> (class names). </td> </tr> 
//*  <tr> <td> {@link #handleErrorReplies(MLMessage,String)  handleErrorReplies()} </td> <td>Returns an appropriate  {@link Status} object for performative-subtypes of <em>reply</em> and <em>nack</em> (error replies).</td> </tr> 
//*  </table> <p> 
//*  <table border="1" bgcolor="RED" cellpadding="3"> 
//*  <tr> <th><b>NOTE </b></th> </tr> 
//*  <tr> <td bgcolor="white">Every change to a constructor for a TransientAgent or ProxyAgent <b>should </b> be reflected in the {@link #initializeProxy(ParamsMap, AgentUI)}  method. <br>
//*  These changes should be done when setting the values of <code>Class[] proxyFields</code><br> or <code>Object[] proxyFields</code>. Otherwise a serious debugging session will have to <br> be carried on!</td> </tr> 
//*  </table> </p> <p> 

/**
 * <p>
 * Base abstract class for all classes to exhibit Agent behavior. Contains basic
 * methods for registering/unregistering Agents with a LAC or YellowPages, as
 * well as the runnable functions to implement the perspectives necessary to
 * accomplish these tasks.
 * </p>
 * 
 * <p>
 * The most important function of Transient agent is handling events coming out
 * of the even queue through the {@link #handleEvent(Event)} method (which
 * overrides {@link AbstractProcess#handleEvent(Event)}). Equally important is
 * the implementation of the {@link #processCommitments()} which is called by
 * {@link AbstractProcess} when there is nothing in the event queue to process
 * (and there's time to do other things).
 * </p>
 * 
 * <p>
 * The major methods that subclass might consider overriding for various
 * functions are as follows. There are others that you can override to deal with
 * activity you want to occur during idle periods or periodically - see the
 * top-level documentation of {@link casa.AbstractProcess} .
 * </p>
 * 
 * <p>
 * The following table lists some of the methods a subclass may want to override
 * (be sure to call the super method). See {@link AbstractProcess} for a others.
 * </p>
 * <table border="1" cellpadding="3">
 * <tr bgcolor="gold">
 * <th>Method</th>
 * <th>Thread</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td> {@link #initializeThread(ParamsMap, AgentUI)}</td>
 * <td>agent</td>
 * <td>called just before the message loop begins; the agent is not fully
 * initialized at this point -- it's not yet registered with the LAC. You can
 * override this method to deal with qualifiers specific to your agent. See
 * {@link casa.AbstractProcess#initializeThread(ParamsMap, AgentUI)} and the
 * top-level documentation of {@link casa.AbstractProcess} for more detail.
 * <tr>
 * <td> {@link #initializeAfterRegistered(boolean)}</td>
 * <td>agent</td>
 * <td>called just after the agent is registered with the LAC -- the agent is
 * now fully initialized.</td>
 * </tr>
 * </table>
 * </p>
 * 
 * <p>
 * This class gets its conversational definitions from a lisp definition in <a
 * href="doc-files/dataFiles/casa.TransientAgent.init.lisp"
 * type="text/plain">/dataFiles/casa.TransientAgent.init.lisp</a>, and it's
 * ontology definition from <a
 * href="doc-files/dataFiles/casa.TransientAgent.ont.lisp"
 * type="text/html">/dataFiles/casa.TransientAgent.ont.lisp</a>.
 * </p>
 * 
 * <p>
 * All subclasses of TransientAgent <em>must</em> statically call
 * {@link #createCasaLispOperators} to create automatically
 * {@link casa.abcl.CasaLispOperator}s for methods annotated with
 * {@link casa.LispAccessible}.
 * </P>
 * 
 * 
 * 
 * 
 * Copyright: Copyright 2003-2014, Knowledge Science Group, University of
 * Calgary. Permission to use, copy, modify, distribute and sell this software
 * and its documentation for any purpose is hereby granted without fee, provided
 * that the above copyright notice appear in all copies and that both that
 * copyright notice and this permission notice appear in supporting
 * documentation. The Knowledge Science Group makes no representations about the
 * suitability of this software for any purpose. It is provided "as is" without
 * express or implied warranty. </p>
 * 
 * @author <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer </a>
 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 * @version 0.9
 * @see AbstractProcess
 */
// TODO: rkyee: update documentation to include request add_observer
public class TransientAgent extends AbstractProcess implements
		TransientAgentInterface, Observer {
	
	static org.armedbear.lisp.Interpreter lisp = casa.abcl.Lisp.getInterpreter();

	// From AbstractProcess///////////////////////////////
	// ///////////////////////////////////////////////////
	/**
	 * A <code>Vector</code> of all of the cooperation domains (
	 * <code>URLDescriptor</code>s) that this agent has successfully joined.
	 * 
	 */
	private ConcurrentSkipListSet<URLDescriptor> joinedCooperationDomains = new ConcurrentSkipListSet<URLDescriptor>();

	public void addJoinedCooperationDomain(URLDescriptor url) {
		this.joinedCooperationDomains.add(url);
	}

	public void removeJoinedCooperationDomain(URLDescriptor url) {
		this.joinedCooperationDomains.remove(url);
	}

	public Set<URLDescriptor> getJoinedCooperationDomain() {
		return this.joinedCooperationDomains;
	}

	/**
	 * A <code>TreeMap</code> of all of the cooperation domains (
	 * <code>URLDescriptor</code>s) that this agent is "watching" to the
	 * conversation IDs that "remember" the subscription. In other words, all of
	 * the cooperation domains that we have requested to learn about new members.
	 */
	// private Vector<URLDescriptor> watchedCooperationDomains = new
	// Vector<Pair<URLDescriptor,String>> ();
	private TreeMap<URLDescriptor, String> watchedCooperationDomains = new TreeMap<URLDescriptor, String>();

	/**
	 * A <code>Hashtable</code> linking the <code>URLDescriptor</code> of a
	 * cooperation domain to a <code>Vector</code> of the participants (
	 * <code>URLDescriptor</code>s) of that cooperation domain.
	 */
	private Hashtable<URLDescriptor, Vector<URLDescriptor>> cooperationDomainMembers = new Hashtable<URLDescriptor, Vector<URLDescriptor>>();

	/**
	 * The message handling factory
	 * 
	 */
	// protected casa.policy.MHPolicyFactory factory;

	/**
	 * The IdleProcessor who's doIdle() method is called in doIdle(); created by
	 * the MHFactory factory.
	 * 
	 */
	private CommitmentProcessor commitmentProcessor;

	/**
	 * Although there may be several observing UIs, this one is the first
	 * registered and will be considered the primary one which the agent may use.
	 */
	protected AgentUI primaryUI = null;
	public AgentUI getPrimaryUI() {return primaryUI;}

	public static CasaKB cacheKB = null;

	protected Vector<AgentUI> UIs = new Vector<AgentUI>();

	// List of the ontology argument descriptors
	protected static List<OntologyFilterArgument> ListOfArgs = new ArrayList<OntologyFilterArgument>();

	/**
	 * strategy object chain for dealing with 'reactive' or 'socialcommitments'
	 * protocol agents.
	 * 
	 */
	protected casa.policy.PolicyContainer policies;

	public void putPolicy(AbstractPolicy p) {
		policies.add(p);
	}

	public Policy[] getPolicies() {
		return policies.toArray(new Policy[policies.size()]);
	}

	protected casa.policy.PolicyContainer policiesAlwaysApply;

	public void putPolicyAlwaysApply(AbstractPolicy p) {
		policiesAlwaysApply.add(p);
	}

	public Policy[] getAlwaysApplyPolicies() {
		return policiesAlwaysApply.toArray(new Policy[policiesAlwaysApply.size()]);
	}

	protected casa.policy.PolicyContainer policiesLastResort;

	public void putPolicyLastResort(AbstractPolicy p) {
		if (policiesLastResort == null) {
			println("error",
					"TransientAgent.putPolicyLastResort(): policiesLastResort is null");
		} else
			policiesLastResort.add(p);
	}

	public Policy[] getLastResortPolicies() {
		return policiesLastResort.toArray(new Policy[policiesLastResort == null ? 0
				: policiesLastResort.size()]);
	}

	// Conversation structures and accessors revisited -- rck
	// //////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////

	// protected ConcurrentSkipListMap<String, LinkedList<Conversation>>
	// conversation2s
	// = new ConcurrentSkipListMap<String, LinkedList<Conversation>>();
	private Conversations conversations = new Conversations();

	/**
	 * This class assures that conversations in the list of conversations will
	 * eventually be released to free memory.
	 */
	class Conversations extends
			ConcurrentSkipListMap<String, LinkedList<Conversation>> {
		private static final long serialVersionUID = 1L;

		/**
		 * Removes the conversation, removing the specific conversation, and the
		 * entire entry under that conversation id if this is the last one under the
		 * same ID.
		 * 
		 * @param conv
		 *          the conversation to remove
		 * @return the parameter Conversation if it was successfully removed, null
		 *         otherwise.
		 * @see casa.AbstractProcess#removeConversation(casa.conversation2.Conversation)
		 */
		public Conversation remove(Conversation conv) {
			LinkedList<Conversation> convs = get(conv.getId());
			if (convs == null)
				return null;
			// attempt to remove it
			if (!convs.remove(conv))
				return null;
			// at this point we've removed it, so check if we can remove the whole
			// entry
			if (convs.size() == 0)
				super.remove(conv.getId());
			return conv;
		}

		static final long purgeInterval = 30000;

		long purgeDueTime = System.currentTimeMillis() + purgeInterval;

		/**
		 * Delete any conversations in state "terminated"
		 */
		public int purge() {
			// don't purge too often
			if (System.currentTimeMillis() < purgeDueTime)
				return 0;
			purgeDueTime = System.currentTimeMillis() + purgeInterval;
			int count = 0;
			int countAll = 0;
			boolean removed = true;
			while (removed) {
				countAll = 0;
				removed = false;
				restart: for (String key : keySet()) {
					LinkedList<Conversation> ll = get(key);
					for (Conversation c : ll) {
						countAll++;
						if ("terminated".equals(c.getState())) {
							removeConversation(c);
							c.destroy();
							removed = true;
							count++;
							break restart;
						}
					}
				}
			}
			if (count > 0)
				println("info", "Purged " + count + " terminated converstions leaving "
						+ countAll + " total. (known conversations: "
						+ Conversation.getKnownConversations().size() + ")");
			return count;
		}

		public void add(String convID, Conversation conv) {
			assert !conv.isTemplate();
			purge(); // clean
			if (containsKey(convID)) {
				get(convID).add(conv);
				println("conversations",
						"Added " + getAgentName() + ": " + conv.toString());
				println("warning5", "Added a subsequent conversation with id '"
						+ convID
						+ "'.  This could be normal if the agent is talking to himself...");
			} else {
				LinkedList<Conversation> cList = new LinkedList<Conversation>();
				cList.add(conv);
				put(convID, cList);
				println("conversations",
						"Updated " + getAgentName() + ": " + conv.toString());
			}
		}

	}; // class Conversations

	@Override
	public Conversation removeConversation(Conversation conv) {
		return conversations.remove(conv);
	}

	@Override
	@LispAccessible(help = "Returns T iff the agent has a conversation with a specific conversationID; returns NIL otherwise.", arguments = { @LispAccessible.Argument(name = "convID", help = "The conversation ID to look up.") })
	public boolean hasConversation(String convID) {
		conversations.purge();
		return conversations.containsKey(convID);
	}

	@Override
	public LinkedList<Conversation> getConversation(String convID) {
		conversations.purge();
		return conversations.get(convID);
	}

	public ConcurrentSkipListMap<String, LinkedList<Conversation>> getConversations() {
		conversations.purge();
		return conversations;
	}

	@Override
	public void addConversation(String convID, Conversation conv) {
		assert !conv.isTemplate();
		conversations.purge();
		conversations.add(convID, conv);
	}

	// //Supported conversations REVISITED////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////
	// private ConcurrentSkipListMap<String, Vector<BoundConversationlet>>
	// supportedConversations = new ConcurrentSkipListMap<String,
	// Vector<BoundConversationlet>>();
	// public boolean supportsConversation(String action){ return
	// supportedConversations.containsKey(action);}
	// public Vector<BoundConversationlet> getSupportedConversation(String action)
	// {return supportedConversations.get(action);}
	// public Vector<BoundConversationlet> removeSupportedConversation(String
	// action) {return supportedConversations.remove(action);}

	/**
	 * This function does not necessarily add a supported conversation to the
	 * TreeMap. If the key passed already exists, the composites are merged. This
	 * is necessary to support internalized conversations.
	 * 
	 * @param action
	 *          The act associated with this conv
	 * @param conv
	 *          The conversation to add
	 * @param overwrite
	 *          if true, overwrite the entry, else extend the entry with the new
	 *          conversation
	 */
	// public void supportConversation(String action, Vector<BoundConversationlet>
	// conv, boolean overwrite){
	// if (supportedConversations.containsKey(action) && !overwrite){
	// supportedConversations.get(action).addAll(conv);
	// } else {
	// Vector<BoundConversationlet> v = new Vector<BoundConversationlet>();
	// v.addAll(conv);
	// supportedConversations.put(action, v);
	// }
	// }
	/**
	 * 
	 * @param action
	 * @param conv
	 */
	// public void supportConversation(String action, Vector<BoundConversationlet>
	// conv){
	// supportConversation(action, conv, false);
	// }

	// //Supported conversations//////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////
	// private ConcurrentSkipListMap<String, Vector<Class<? extends
	// CompositeConversation>>> conversationsSupported = new
	// ConcurrentSkipListMap<String, Vector<Class<? extends
	// CompositeConversation>>>();
	// public boolean hasConversationSupported(String action){ return
	// conversationsSupported.containsKey(action);}
	// public Vector<Class<? extends CompositeConversation>>
	// getConversationSupported(String action) {return
	// conversationsSupported.get(action);}
	// public Vector<Class<? extends CompositeConversation>>
	// removeConversationSupported(String action) {return
	// conversationsSupported.remove(action);}

	/**
	 * This function does not necessarily add a supported conversation to the
	 * TreeMap. If the key passed already exists, the composites are merged. This
	 * is necessary to support internalized conversations.
	 * 
	 * @param action
	 *          The act associated with this conv
	 * @param conv
	 *          The conversation to add
	 * @param overwrite
	 *          if true, overwrite the entry, else extend the entry with the new
	 *          conversation
	 */
	// public void addConversationSupported(String action, Class<? extends
	// CompositeConversation> conv, boolean overwrite){
	// if (conversationsSupported.containsKey(action) && !overwrite){
	// conversationsSupported.get(action).add(conv);
	// } else {
	// Vector<Class<? extends CompositeConversation>> v = new Vector<Class<?
	// extends CompositeConversation>>();
	// v.add(conv);
	// conversationsSupported.put(action, v);
	// }
	// }
	/**
	 * 
	 * @param action
	 * @param conv
	 */
	// public void addConversationSupported(String action, Class<? extends
	// CompositeConversation> conv){
	// addConversationSupported(action, conv, false);
	// }

	// Store and retrieve data returned from conversations////////////////////////
	// ///////////////////////////////////////////////////////////////////////////
	protected ConcurrentSkipListMap<String, Object> conversationData = new ConcurrentSkipListMap<String, Object>();

	public void saveReturnedData(String act, Object o) {
		conversationData.put(act, o);
		println("conversations", "Saved " + o.toString() + " from " + act
				+ " conversation");
	}

	public Object retrieveReturnedData(String act) {
		return conversationData.get(act);
	}

	// For Proxy//////////////////////////////////////
	// ///////////////////////////////////////////////
	/**
	 */
	protected AgentProxy proxy = null;

	protected boolean usingProxy = false;

	private boolean usingSecureProxy = false;

	private byte[] key;

	private static int KEY_LENGTH = 20;

	private long uniqueNumber = 0L;

	private int realPort;

	private int proxyPort;

	public boolean isUsingProxy() {
		return usingProxy;
	}

	public AgentProxy getProxy() {
		return proxy;
	}

	// ///////////////////////////////////////////////
	// ///////////////////////////////////////////////
	/**
	 */
	public URLDescriptor CDinvitee = null;

	/**
	 */
	public URLDescriptor CDInvitedTo = null;

	// ///////////////////////////////////////////////
	// ///////////////////////////////////////////////

	/**
	 */
	// protected RTCommandInterpreter commandInterpreter = new
	// RTCommandInterpreter ();

	private static int calculatePortAccountingForProxies(ParamsMap params,
			AgentUI ui) throws Exception {
		int port = 0;
		port = (Integer) params.getJavaObject("PORT", Integer.class);
		Class<?>[] proxies = getProxies(params, ui);
		if (proxies != null && proxies.length > 0) {
			if (port < 0)
				port = port - proxies.length;
			else
				port = -port - proxies.length;
		}
		return port;
	}

	@SuppressWarnings("unchecked")
	private static Class<AgentProxy>[] getProxies(ParamsMap params, AgentUI ui)
			throws Exception {
		String proxiesString = (String) params.getJavaObject("PROXIES",
				String.class);
		if (proxiesString == null)
			return null;
		String[] proxyStrings = proxiesString.split(";");
		Vector<Class<AgentProxy>> v = new Vector<Class<AgentProxy>>();
		for (String proxyString : proxyStrings) {
			Class<AgentProxy> cls = (Class<AgentProxy>) Class.forName(proxyString);
			if (!AgentProxy.class.isAssignableFrom(cls))
				throw new java.lang.ClassCastException(
						"Proxies must be a subclass of AgentProxy: " + proxyString);
			v.add(cls);
		}
		return (Class<AgentProxy>[]) v.toArray();
	}

	/**
	 * Calls the parent constructor
	 * {@link AbstractProcess#AbstractProcess(ParamsMap, AgentUI, int)} with the
	 * <em>port</em> (2nd) argument modified: iff proxies are specified in
	 * <em>quals</em> then the port is decremented to make room for the proxies.
	 * Thus, the top proxy can be allocated the target port.
	 * 
	 * @see AbstractProcess#AbstractProcess(ParamsMap, AgentUI, int)
	 * @param params
	 * @param ui
	 * @throws Exception
	 */
	public TransientAgent(ParamsMap params, AgentUI ui) throws Exception {
		super(params, ui, calculatePortAccountingForProxies(params, ui));
		in("TransientAgent.TransientAgent");
		if (options == null)
			options = makeOptions();
		out("TransientAgent.TransientAgent");
	}

	// @Override
	// protected void initializeConstructor(ParamsMap params, AgentUI ui, int
	// port) throws Exception {
	// super.initializeConstructor(params, ui, port);
	// }

	/**
	 * attempts to read <em>fileNameOnly</em> from the following places (in this
	 * order):
	 * <ol>
	 * <li>the directory specified in the system property "casa.home"
	 * <li>the directory specified in the system property "user.home",
	 * subdirectory "casa/dataFiles"
	 * <li>the current working directory
	 * <li>as a resource included in the casa distribution
	 * </ol>
	 * 
	 * @param fileNameOnly
	 * @param reportFailure
	 *          In the event that {@link #findFileResourcePath(String)} fails,
	 *          then if this is true throw a detailed exception, otherwise just
	 *          return null.
	 * @throws IllegalOperationException
	 * @throws Exception
	 */
	private String loadFileResource(String fileNameOnly, boolean reportFailure)
			throws IOException, IllegalOperationException {
		// locate the path for the resource
		String path = findFileResourcePath(fileNameOnly);

		if (path == null) {
			if (!reportFailure)
				return null;
			StringBuilder buf = new StringBuilder(
					"Can't find resource initialization file: ")
					.append(fileNameOnly)
					.append(
							"\n- Please put the file in one of the following search locations:");
			for (String loc : getDefFileSystemLocations()) {
				buf.append("\n    ").append(loc);
			}
			throw new IOException(buf.toString());
		}

		// Accounts for Windows' path separators, which are also escape characters
		if (File.separatorChar == '\\') {
			path = path.replaceAll("\\\\", "\\\\\\\\");
			fileNameOnly = fileNameOnly.replaceAll("\\\\", "\\\\\\\\");
			fileNameOnly = fileNameOnly.replaceAll("/", "\\\\\\\\");
		}

		// String dir = path.substring(0, path.lastIndexOf(fileNameOnly));
		String loadPath = path.substring(0,
				path.lastIndexOf(File.separatorChar) + 1);

		println("info", "Loading file " + path);
		// org.armedbear.lisp.Load.load(path);
		// abclEval("(setq *CASA-LOAD-PATH* \""+loadPath+"\")", null);
		// Status stat = abclEval("(load \""+path+"\")", null);
		// abclEval("(makunbound '*CASA-LOAD-PATH*)", null);

		// Status stat =
		// abclEval("(prog2 (setq *CASA-LOAD-PATH* \""+loadPath+"\") (load \""+path+"\") (makunbound '*CASA-LOAD-PATH*))",
		// null);

		Status stat = casa.abcl.Lisp.abclEval(this, null, null,
				"(prog2 (setq *CASA-LOAD-PATH* \"" + loadPath + "\") (load \"" + path
						+ "\") (makunbound '*CASA-LOAD-PATH*))", null);

		if (stat.getStatusValue() >= 0) {
			println("info", "Loaded file " + path);
		} else {
			println("error",
					"Failed to load file " + path + ": " + stat.getExplanation());
		}
		return path;
	}

	// public String loadFileResource(String fileNameOnly) throws IOException {
	// String path = findFileResourcePath(fileNameOnly);
	// if (path != null) {
	// //Accounts for Windows' path separators, which are also escape characters
	// if (File.separatorChar == '\\')
	// path = path.replaceAll("\\\\", "\\\\\\\\");
	// TreeMap<String, LispObject> map = new TreeMap<String, LispObject>();
	// String dir = path.substring(0, path.lastIndexOf(fileNameOnly));
	// map.put("*CASA-LOAD-PATH*", new SimpleString(dir));
	// println("loading "+path);
	// abclEval("(load \""+path+"\")",map);
	// }
	// return path;
	// }

	public static String findFileResourcePath(String fileNameOnly) throws IOException {
		return findFileResourcePath(fileNameOnly, getDefFileSystemLocations());
	}
	/**
	 * Attempts to locate the file resource <em>fileNameOnly</em> from the
	 * following places (in this order):
	 * <ol>
	 * <li>each of the paths specified by {@link #getDefFileSystemLocations()} (be
	 * careful of caching here -- call {@link #resetDefFileSystemLocations()} if
	 * you change system properties "casa.home" or "user.home").
	 * <li>as a resource included in the casa distribution associated with class
	 * CASAOntology
	 * <li>as a system resource (may be included in the casa distribution)
	 * </ol>
	 * 
	 * @param fileNameOnly
	 * @throws IOException
	 * @return The fully-qualified file pathname or null if the resource can't be
	 *         found
	 */
	public static String findFileResourcePath(String fileNameOnly, String[] dirs) throws IOException {
		// String fileName = File.separator + fileNameOnly;
		// String path;
		File file;

		// look in the directories specified by getDefFileSystemLocations()
		for (String path : dirs) {
			file = new java.io.File(path, fileNameOnly);
			if (file.canRead())
				return file.getCanonicalPath();
		}

		java.net.URL url;

		// look for the resource associated with the class CASAOntology
		// java.net.URL url = Ontology.class.getResource(fileNameOnly);
		// URI uri = null;
		// if (url!=null) {
		// try {
		// // this seems weird, but deals with paths with spaces in them
		// file = new java.io.File(uri = new java.net.URI(url.toString()));
		// if (file.canRead()) return file.getCanonicalPath();
		// } catch (Throwable e) {
		// DEBUG.PRINT("TransientAgent.findFileResourcePath("+fileNameOnly+") ["+uri+"]",
		// e);
		// } //do nothing, just continue
		// }

		// //look for it as a system resource
		// url = ClassLoader.getSystemResource("/"+fileNameOnly);
		// if (url!=null) {
		// try {
		// // this seems weird, but deals with paths with spaces in them
		// file = new java.io.File(uri = new java.net.URI(url.toString()));
		// if (file.canRead()) return file.getCanonicalPath();
		// } catch (Throwable e) {
		// DEBUG.PRINT("TransientAgent.findFileResourcePath("+fileNameOnly+") ["+uri+"]",
		// e);
		// }
		// }

		// look for it as a system resource
		url = ClassLoader.getSystemResource(fileNameOnly);
		if (url != null) {
			return url.toString();
			// try {
			// // this seems weird, but deals with paths with spaces in them
			// file = new java.io.File(uri = new
			// java.net.URI(url.toString().replace("jar:file:", "jar:")));
			// if (file.canRead()) return file.getCanonicalPath();
			// } catch (Throwable e) {
			// DEBUG.PRINT("TransientAgent.findFileResourcePath("+fileNameOnly+") [uri="+uri+" url="+url+"]",
			// e);
			// }
		}

		return null;
	}

	/**
	 * To be used only by {@link #getDefFileSystemLocations()} and
	 * {@link #resetDefFileSystemLocations()}.
	 */
	private static String[] defFileSystemLocations = null;

	/**
	 * Returns an ordered array of directories to look in when locating system
	 * resources:
	 * <ol>
	 * <li>the directory specified in the system property "casa.home" (if
	 * "casa.home" is defined)
	 * <li>the directory specified in the system property "casa.home",
	 * subdirectroy "dataFiles" (if "casa.home" is defined)
	 * <li>the directory specified in the system property "user.home",
	 * subdirectory "casa/dataFiles"
	 * <li>the current working directory (".")
	 * </ol>
	 * This method is used by {@link #findFileResourcePath(String)}, which uses
	 * these directories as well as project and system resource paths to find a
	 * file. If you are looking for a file, you should probably use
	 * {@link #findFileResourcePath(String)}, but you may find this method useful
	 * to report an error in find that resource. <br>
	 * Caution: this list is cached for efficiency; if you change the system
	 * properties "casa.home" or "user.home", you'll want to call
	 * {@link #resetDefFileSystemLocations()}.
	 * 
	 * @return An ordered array of the directories to search for system files
	 */
	public static String[] getDefFileSystemLocations() {

		// return the cache if we can
		if (defFileSystemLocations != null)
			return defFileSystemLocations;
		else {
			Vector<String> ret = new Vector<String>();

			String casaHome = System.getProperty("casa.home");
			if (casaHome != null) {
				ret.add(casaHome); // look in system property "casa.home"
				ret.add(casaHome + File.separator + "dataFiles"); // look in system
																													// property
																													// "casa.home" +
																													// /dataFiles/
			}
			ret.add(System.getProperty("user.home") + File.separator + ".casa");
			ret.add(System.getProperty("user.home") + File.separator + ".casa"
					+ File.separator + "dataFiles"); // look in system property
																					 // "user.home" + /casa/dataFiles/
			ret.add("."); // look in the current working directory
			ret.add("." + File.separator + "dataFiles"); // look in the current working directory /dataFiles

			defFileSystemLocations = ret.toArray(new String[0]); // update the cache

			return defFileSystemLocations;
		}
	}

	/**
	 * reset the cache for the default resource locations to look in. You'd want
	 * to do that if you've changed the system properties "casa.home" or
	 * "user.home".
	 */
	private void resetDefFileSystemLocations() {
		defFileSystemLocations = null;
	}

	/**
	 * Called by handleReply_registerAgentInstance(). During the constructor, the
	 * Agent registers with the LAC, but has to wait for the LAC to reply before
	 * initializing the file and properties. The init() method is called at that
	 * time to initialize. <br>
	 * This version of init() checks to see if the agent already has an ontology,
	 * and if it doesn't, it requests the ontology from the LAC by calling
	 * {@link #doGetOnology(URLDescriptor, String, String)}.<br>
	 * Subclasses should override as appropriate.
	 * 
	 * @param registered
	 *          Will be set to true if the agent has sucessfully registered with
	 *          the LAC; false otherwise
	 * 
	 */
	public void initializeAfterRegistered(boolean registered) {
		in("TransientAgent.init");
		assert isAgentThread() : "initializeAfterRegistered() should only be executing in the agent thread.";
		String file = null;
		try {
			setBanner(null);

			// run any script files, starting with the least specific
			Vector<String> candidateInitFiles = new Vector<String>();
			candidateInitFiles.add(getURL().getFile()); // the name of this individual
																									// agent
			for (Class<? extends Object> cls = this.getClass(); cls != null; cls = cls
					.getSuperclass()) {
				candidateInitFiles.add(cls.getName()); // the name of this agent's class
																							 // and it's superclasses
			}
			for (int i = candidateInitFiles.size() - 1; i >= 0; i--) {
				file = candidateInitFiles.get(i) + ".lisp";
				try {
					loadFileResource(file, false);
				} catch (Exception e) {
					println("error", "Unexpected exception opening " + file, e);
				}
			}
		} catch (Throwable e) {
			println("error", "TransientAgent.initializeAfterRegistered(" + registered
					+ "): Unexpected error"
					+ (file == null ? "" : " loading file " + file), e);
		}
	}

	/**
	 * Subclasses may override this method to initialize before the message loop
	 * is called, after the constructor is finished but before control is returned
	 * to the thread that called the constructor. Called once, just before
	 * messageBufferLoop(). This method is safe as it called from the thread of
	 * the agent (not the constructor). <br>
	 * This method does several tasks:
	 * <ul>
	 * <li>initializes any proxies using
	 * {@link #initializeProxy(ParamsMap, AgentUI)}
	 * <li>initializes the conversation protocol type using
	 * {@link #initializePolicies(ParamsMap, AgentUI)}
	 * <li>sets the Acknowledge protocol (ack/nack) on or off depending on the
	 * qualifier setting
	 * <li>
	 * </ul>
	 */
	@Override
	protected void initializeThread(ParamsMap params, AgentUI ui) {
		in("TransientAgent.initializeRun");
		super.initializeThread(params, ui);

		initializePolicies(params, ui);

		// Will attempt to load ontology v3 from <agent_name>.ont.lisp or
		// <agent_class_name>.ont.lisp
		getOntology();
		// // if the above failed, read in the ontology from ontology.lisp as a
		// process-wide shared ontology
		// if (ontology==null) {
		// try {
		// loadFileResource("ontology.lisp", false);
		// } catch (Exception e1) {
		// println ("error",
		// "TransientAgent.initialize: Can't read file ontology.lisp", e1);
		// }
		// }

		// load agent init files
		Vector<String> candidateInitFiles = new Vector<String>();
		candidateInitFiles.add(getURL().getFile()); // the name of this individual
																								// agent
		for (Class<? extends Object> cls = this.getClass(); cls != null; cls = cls
				.getSuperclass()) {
			candidateInitFiles.add(cls.getName()); // the name of this agent's class
																						 // and it's superclasses
		}
		Vector<String> reversed = new Vector<String>();
		for (int i = candidateInitFiles.size() - 1; i >= 0; i--)
			reversed.add(candidateInitFiles.elementAt(i));
		for (String file : reversed) {
			try {
				loadFileResource(file + ".init.lisp",
						trace.isLoggingTag("info5") ? true : false);
			} catch (IOException e) {
				println("info5",
						"Can't find file " + file + ".init.lisp: " + e.toString());
			} catch (IllegalOperationException e) {
				println("info5",
						"Can't find file " + file + ".init.lisp: " + e.toString());
			} catch (Throwable e) {
				println("error", "Unexpected exception opening " + file + ".init.lisp",
						e);
			}
		}

		/*
		 * If an agent requests to be protected, we then initialize the respective
		 * ProxyAgent
		 */
		try {
			initializeProxy(params, ui);
		} catch (Exception e) {
			Trace.log("error", "TransientAgent.intializeRun: agent " + getName()
					+ " failed initializeProxy()", e);
		}

		// if agent is protected, so not add itself to the look-up table.
		if (!usingProxy)
			AgentLookUpTable.put(this.getURL(), this);

		initJADE();

		setBanner(null);

		int LACPort;
		try {
			LACPort = (Integer) params.getJavaObject("LACPORT", Integer.class);
		} catch (Throwable e) {
			LACPort = CASA.getPreference("LACdefaultport", 9000, 0);
		}
		final int finalLACPort = LACPort;
		defer(new Runnable() { // This code must be deferred through the event queue
													 // to avoid calling initializeAfterRegestered()
													 // before initializeThread() is done.
			@Override
			public void run() {
				if (finalLACPort > 0) { // the command line (or default) specifies an
																// LAC, so register to the LAC

					Status tempStatus = doRegisterAgentInstance(finalLACPort);
					if (tempStatus.getStatusValue() != 0) { // the registration failed
																									// from the get-go, so call
																									// initializeAfterRegistered(false)
																									// anyway
						println("warning",
								"TransientAgent.constructor: Cannot register agent", tempStatus);
						callInitializeAfterRegistered(false);
					}
				} else { // the command line specified NOT to register to a LAC, so call
								 // initializeAfterRegistered(false) anyway
					callInitializeAfterRegistered(false);
				}
			}
		});

		startPrimaryInterface();
		ExtensionLoader.loadType(null, null, this); // executes any agent-global extensions on this agent.

		out("TransientAgent.initializeRun");
	}

	/**
	 * Utility method to be sure to call
	 * {@link #initializeAfterRegistered(boolean)} from the agent's thread.
	 * 
	 * @param isRegistered
	 *          used to tell {@link #initializeAfterRegistered(boolean)} if the
	 *          agent is actually registered or not.
	 */
	private void callInitializeAfterRegistered(final boolean isRegistered) {
		if (isAgentThread())
			initializeAfterRegistered(isRegistered);
		else {
			defer(new Runnable() {
				@Override
				public void run() {
					initializeAfterRegistered(isRegistered);
				}
			});
		}
	}

	/**
	 * This method creates and loads the policies for this agent. Children should
	 * override and extends this method if they implement custom events. If you
	 * load a policy in another initialize* method then there is no guarantee that
	 * this agent has not handled any events. The {@link #commitmentProcessor} is
	 * also initialized here.
	 * 
	 * @param params
	 * @param ui
	 *          the UI that represents this agent
	 */
	protected void initializePolicies(ParamsMap params, AgentUI ui) {
		// read in the policies
		String strategy;
		try {
			strategy = (String) params.getJavaObject("STRATEGY", String.class);
		} catch (Exception e1) {
			strategy = "sc3";
		}
		if (strategy == null)
			strategy = "sc3";
		updateConversationProtocolType(strategy);
		setCommitmentProcessor(new ConcreteCommitmentProcessor(this,
				new SocialCommitmentsStore(this)));
	}

	protected void setCommitmentProcessor(CommitmentProcessor cp) {
		commitmentProcessor = cp;
	}

	protected CommitmentProcessor getCommitmentProcessor() {
		return commitmentProcessor;
	}

	/**
	 * Used by Conversations to track their respective SocialCommitments
	 * 
	 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
	 */
	public Collection<SocialCommitment> getUnfulfilledSocialCommitments(String id) {
		return commitmentProcessor.getStore().getUnfulfilledCommitments(id);
	}

	/**
	 * Retrieve all unfulfilled SocialCommitments
	 * 
	 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
	 */
	public Collection<SocialCommitment> getUnfulfilledSocialCommitments() {
		return commitmentProcessor.getStore().getUnfulfilledCommitments();
	}

	@Override
	public SocialCommitmentsStore getSCStore() {
		return commitmentProcessor.getStore();
	}

	/**
	 * Initializes (or re-initializes) the conversation protocol by looking in the
	 * package "casa.policy<em>strat</em> (where <em>strat</em> is the parameter)
	 * for a class called MHConcretePolicyFactory (which better be a subclass of
	 * {@link casa.policy.PolicyContainer} ). It instantiates casa.policy.
	 * <em>strat</em>.MHConcretePolicyContainer and uses it as its policy factory.<br>
	 * 
	 * @param strat
	 *          the strategy package to use
	 */
	@Deprecated
	protected void updateConversationProtocolType(String strat) {

		if (policies != null) {
			if (policies.getType().equals(strat)) {
				println("warning", "policies " + policies.getType()
						+ " being replaced by itself, aborting policy update",
						new Exception());
				return;
			}
			println("warning", "policies " + policies.getType()
					+ " being replaced by policies " + strat, new Exception());
		}
		// try {
		// Class<? extends PolicyContainer> containerClass = Class.forName
		// (className).asSubclass (PolicyContainer.class);
		// Object params[] = {/*this*/};
		// Class<?> paramTypes[] = {/*PolicyAgentInterface.class*/};
		// policies = containerClass.getConstructor(paramTypes).newInstance
		// (params);
		// policiesAlwaysApply =
		// containerClass.getConstructor(paramTypes).newInstance (params);
		// policiesLastResort =
		// containerClass.getConstructor(paramTypes).newInstance (params);
		//
		// } catch (Throwable ex) {
		// println("warning","Cannot construct object of type "+className+"; using casa.policy.PolicyContainer(\""+strat+"\"): "+ex.toString()/*,ex*/);
		policies = new casa.policy.PolicyContainer(strat,"global");
		policiesAlwaysApply = new casa.policy.PolicyContainer(strat,"alwaysApply");
		policiesLastResort = new casa.policy.PolicyContainer(strat,"lastResort");
		;
		// }

		notifyObservers(ML.EVENT_STRATEGY_CHANGED, null);
	}

	@Override
	public String getStrategy() {
		return policies == null ? "unknown" : policies.getType();
	}

	/**
	 * Returns a GUI that can be used to interact with the stategy of this agent.
	 * Will return a <code>null</code> value if hasStrategyGUI() returns
	 * <code>false</code>.
	 * 
	 * @return A <code>JPanel</code> that is the GUI responsible for interacting
	 *         with this agent's strategy.
	 */
	@Override
	public JComponent getStrategyGUI() {
		return policies == null ? null : policies.getPolicyGUI(getSCStore());
	}

	/**
	 * Used to determine if the agent's strategy has a GUI.
	 * 
	 * @return Whether the strategy has a GUI.
	 */
	@Override
	public boolean hasStrategyGUI() {
		return policies == null ? false : policies.hasPolicyGUI();
	}

	/**
	 * Returns the default (null) knowledge base. Specific implementations should
	 * override this method if an actual KnowledgeBase is to be used.
	 * 
	 * @return
	 */
	@Override
	public CasaKB getKnowledgeBase() {
		return kBase;
	}

	public CasaKB getCachedKnowledgeBase() {
		return cacheKB;
	}

	/**
	 * This method retrieves a method from <code>this</code> even if it is
	 * protected. This means that the method may not be callable. If getMethod()
	 * is used, only public methods are returned.
	 * 
	 * @param object
	 *          The object who's method is to be obtained (only used for it's
	 *          class).
	 * @param methodName
	 *          The name of the method to return.
	 * @param parameters
	 *          The parameters that must exist in the returned method.
	 * 
	 * @return A Method object with the required parameters, or <code>null</code>
	 *         if it does not exist.
	 */
	private Method getMethodRobustly(Object object, String methodName,
			Class<?>... parameters) {
		Class<?> clazz = object.getClass();
		Method method = null;
		do {
			try {
				method = clazz.getDeclaredMethod(methodName, parameters);
			} catch (Exception e) {
				method = null;
			}
			clazz = clazz.getSuperclass();
		} while (clazz != null && method == null);

		return method;
	}

	Map<String, Method> methodMap = new TreeMap<String, Method>();

	/**
	 * Calls a method on <code>this</code> with the specified name, passing it the
	 * given MLMesssage, and returning a PerformDescriptor. Throws
	 * NoSuchMethodException if no such method exists.<br>
	 * In the event that the method is not addressed to this agent, we don't call
	 * the method, but we attempt to find a method with the same name concatonated
	 * with "_evesdrop"; if we can't find such a method, then call
	 * {@link #evesdrop(MLMessage)} as a catch-all.
	 * 
	 * @param message
	 *          The message to pass.
	 * @param theMethodName
	 *          The name of the method to call.
	 * @return The PerformDescriptor that the method returns with the details of
	 *         the actual method called in the key 'methodCalled'.
	 * @throws NoSuchMethodException
	 *           If any exception occurs during the call.
	 */
	private PerformDescriptor callHandlerMethod(MLMessage message,
			String methodName) throws NoSuchMethodException {
		PerformDescriptor pd = null;
		Method method = null;
		Object object = this; // by default, we want to call the method on this
													// agent, but if we find the method in a conversation,
													// we need to redirect...

		// if the message is not addressed to me, then form a version of the method
		// that ends in "_evesdrop"
		String theMethodName = new String(methodName);
		boolean evesdrop = false;
		try {
			if (!isThisMyAddress(message.getReceiver())) {
				theMethodName += "_evesdrop";
				evesdrop = true;
			}
		} catch (URLDescriptorException e2) {
			e2.printStackTrace();
			evesdrop = true;
		}

		// try to find a method that matches the method name in this agent
		if (method == null) {
			method = findMethod(this, theMethodName);
		}

		// if we're evesdropping and we can't find a matching method, then call the
		// catchall method evesdrop(MLMessage);
		if (evesdrop && method == null) {
			return evesdrop(message);
		}

		// finally, attempt to to call the method...
		Throwable throwable = null;
		try {
			if (method != null) {
				if (method.getReturnType().isAssignableFrom(PerformDescriptor.class)) {
					Object o = method.invoke(object, message);
					if (o == null || o instanceof PerformDescriptor) {
						pd = (PerformDescriptor) o;
						if (pd == null)
							pd = new PerformDescriptor();
						pd.put("methodCalled", method.toGenericString());// method.getReturnType().getName()+" "+object.getClass().getName()+"."+method.getName()+"(MLMessage)");
					}
				} else {
					throw new NoSuchMethodException("Method " + theMethodName
							+ " returns a " + method.getReturnType().getName()
							+ " instead of a PerformDescriptor type");
				}
			} else {
				throw new NoSuchMethodException("Method " + theMethodName
						+ " not found");
			}
		} catch (SecurityException e) {
			throwable = e;
		} catch (IllegalArgumentException e) {
			throwable = e;
		} catch (IllegalAccessException e) {
			throwable = e;
		} catch (InvocationTargetException e) {
			println("error", method.toString() + " threw exeception:", e.getCause());
			throwable = e.getCause();
		}
		if (throwable != null) {
			Throwable e = new NoSuchMethodException("Cause: " + throwable.toString());
			try {
				e.initCause(throwable);
				throw e;
			} catch (Throwable e1) {
				println("error",
						"TransientAgent.callHandlerMethod: Cannot init cause for Exception in method: "
								+ theMethodName, e1);
			}
		}
		return pd;
	}

	/**
	 * Find a method by first looking in the cache, and if it isn't there by
	 * deferring to {@link #getMethodRobustly(Object, String, Class...)} to find
	 * it in <em>object</em> using reflection.
	 * 
	 * @param object
	 *          the Object to look for the method in
	 * @param methodName
	 *          the name of the method to return
	 * @return the Method found, or null if not found.
	 */
	private Method findMethod(Object object, String methodName) {
		Method method;
		if (methodMap.containsKey(methodName)) { // is this method in the cache?
			method = methodMap.get(methodName); // retrieve the method from the cache
		} else {
			method = getMethodRobustly(object, methodName, MLMessage.class);
			if (method != null)
				methodMap.put(methodName, method); // we've found the method, record it
																					 // in the cache
		}
		return method;
	}

	/**
	 * This is the catch-all method called when
	 * {@link ProcessOptions#observeMessages} is set true and the handler method
	 * (the regular method concatonated with "_evesdrop") is not found. Subclasses
	 * should override this to implement the desired behaviour. This implemenation
	 * does nothing but return a {@link PerformDescriptor} with a
	 * {@link #DROP_ACTION} Status, which indicates no action should be taken.
	 * 
	 * @param msg
	 *          An incomming message that is not addressed to this agent.
	 * @return null
	 */
	@Override
	public PerformDescriptor evesdrop(MLMessage msg) {
		return new PerformDescriptor(new Status(DROP_ACTION));
	}

	/**
	 * This method is called (directly or indirectly) in response to a
	 * {@link ML#REQUEST request}-type message. This implementation uses the
	 * {@link sun.reflect} package to look for a method with the signature
	 * consider_*(MLMessage) (where * is the name of the performative in the
	 * message) and calls that method if it exists, otherwise it calls
	 * {@link #considerDefault(Act, MLMessage)}. If the message is just an
	 * {@link ML#INFORM inform} type and the called method returns a performative
	 * in it's return of a subtype of {@link ML#NEGATIVE_REPLY negative-reply}
	 * then a {@link ML#REPLY_REQUEST reply} message is constructed and returned
	 * to the sender (even though {@link ML#INFORM inform} messages usually don't
	 * get a reply).<br>
	 * Be sure to look at the <em>see</em> section for more detail on possible
	 * returns.
	 * 
	 * @param act
	 *          the Act object of the message (should be the same as that
	 *          described in the {@link ML#ACT act} field of <em>message</em>).
	 * @param message
	 *          the {@link ML#REQUEST request}-type performative message that
	 *          percipitated the call to the method
	 * @return the result as described above.
	 * @author kremer
	 * @see casa.interfaces.PolicyAgentInterface#consider(casa.Act,
	 *      casa.MLMessage)
	 */
	@Override
	public PerformDescriptor dispatchMsgHandlerMethod(String consider, Act act,
			MLMessage message) {
		in("TransientAgent.dispatchMsgHandlerMethod");

		if (act == null)
			act = message.getAct();

		String methodName = consider + "_" + act.toStringSimplify(-1);

		PerformDescriptor pd = null;

		try {
			pd = callHandlerMethod(message, methodName);
			println(
					"policies",
					"TransientAgent.consider: Called "
							+ ((pd == null) ? "(<:TransientAgent)." + methodName + "()" : pd
									.get("methodCalled")) + " to handle message "
							+ message.getParameter(ML.PERFORMATIVE) + "/"
							+ message.getParameter(ML.ACT));
			// TODO In the case of a handler that doesn't normally respond (such as
			// INFORM) that returns a bad status, we need to force a response here...
		} catch (NoSuchMethodException e) {
			try {
				pd = callHandlerMethod(message, consider + "Default");
				println(
						"policies",
						"TransientAgent.consider: Called (<:TransientAgent)." + consider
								+ "Default() (instead of not-found ." + methodName
								+ "()) to handle message "
								+ message.getParameter(ML.PERFORMATIVE) + "/"
								+ message.getParameter(ML.ACT) + " due to", e);
				// if (e.getCause()!=null) println("policies","",e.getCause());
			} catch (NoSuchMethodException e1) {
				pd = new PerformDescriptor(new Status(-1));
				pd.put(ML.PERFORMATIVE, ML.NOT_UNDERSTOOD);
				pd.put(
						ML.CONTENT,
						println("error",
								"TransientAgent.dispathchMsgHandlerMethod: Could not find method \""
										+ methodName + "\" and could not find a default, \""
										+ consider + "Default\" for it either", e1));
			}
		}

		out("TransientAgent.dispathcMsgHandlerMethod");
		return pd;
	}

	/**
	 * The default code for responding to an {@link ML#REQUEST request} message or
	 * a {@link ML#PROPOSE_DISCHARGE} message. Subclasses may override this method
	 * to change the default behavior. In this case, if this is a
	 * {@link ML#REQUEST}-type message, we check to make sure there is a
	 * perform_*(MLMessage) handler; if there is, this method sortcuts to omit the
	 * {@link ML#AGREE agree} message; otherwise it constructs a return indicating
	 * a {@link ML#NOT_UNDERSTOOD not-understood} message should be returned
	 * (since we have no handler for it).<br>
	 * On the other hand, if this is only an {@link ML#INFORM inform}-type
	 * message, and we got here, the agent obviously doesn't have a handler for
	 * it, so a message is constructed and sent back indicating
	 * {@link ML#NOT_UNDERSTOOD not-understood}. <br>
	 * To shortcut (as above), the return PerformDescriptor is altered by
	 * inserting a {@link ML#PERFORMATIVE}={@link ML#SUCCESS}, inserting a
	 * {@link ML#ACT}={@link ML#DISCHARGE}|<original-ACT>, and calling
	 * perform_*(MLMessage) to fill in the rest.
	 * 
	 * @param message
	 *          the message that precipitated this action
	 * @return null, but normally the description containing a {@link Status}
	 *         object and any changes to the default return message (a null return
	 *         indicates "success" and no changes).
	 */
	protected PerformDescriptor considerDefault(MLMessage message) {
		in("TransientAgent.considerDefault");
		Act act = message.getAct();
		PerformDescriptor ret = null;
		String methodName = null;
		Method method = null;
		boolean forceReply = false;
		String performative = message.getParameter(ML.PERFORMATIVE);
		// SUBSCRIBE
		if (isA(performative, ML.SUBSCRIBE)) {
			methodName = "monitor_" + act.toStringSimplify(-1);
			method = getMethodRobustly(this, methodName, MLMessage.class);
		}
		// REQUEST
		else if (isA(performative, ML.REQUEST)) {
			methodName = "perform_" + act.toStringSimplify(-1);
			method = getMethodRobustly(this, methodName, MLMessage.class);
		}
		// PROPOSE_DISCHARGE
		// else if (isA(performative,ML.PROPOSE_DISCHARGE)) {
		// methodName = "release_" + act.toStringSimplify(-1);
		// method = getMethodRobustly(methodName, MLMessage.class);
		// }
		// INFORM: if this method is called, we don't have a handler
		else if (isA(performative, ML.INFORM)) {
			methodName = "accept_" + act.toStringSimplify(-1);
			method = getMethodRobustly(this, methodName, MLMessage.class);
			// forceReply = true;
		}

		if (method == null) { // there is no perform method, so we don't handle this
													// message: reply "not understood"
			if (message.isBroadcast()) { // if this is broadcast-type message we can
																	 // safely just ignore it
				ret = new PerformDescriptor(new Status(DROP_ACTION, null));
			} else {
				String warning = "TransientAgent.considerDefault: Agent "
						+ getAgentName() + " has no handler (looking for method "
						+ methodName + "(MLMessage)) for messages of type "
						+ message.getParameter(ML.PERFORMATIVE) + "/"
						+ message.getParameter(ML.ACT);
				ret = new PerformDescriptor(new Status(-55, warning));
				ret.put(ML.PERFORMATIVE, ML.NOT_UNDERSTOOD);
				// ret.put(ML.ACT,
				// ((Act)act.clone()).push(message.getParameter(ML.PERFORMATIVE)).toString());//this
				// is done by MLMessage.consturctReplyTo()
				ret.put(ML.CONTENT, warning);
				ret.put(ML.LANGUAGE, "text");
			}
		}
		if (forceReply && !message.isBroadcast()) { // this is an INFORM with no
																								// handler, so reply
																								// (negatively) anyway.
			MLMessage replyMsg = MLMessage.constructReplyTo(message,
					getUniqueRequestID(), getURL());
			replyMsg.setParameters(ret);
			sendMessage(replyMsg);
		}
		out("TransientAgent.considerDefault");
		return ret;
	}

	/**
	 * The default code for responding to an non-{@link ML#AGREE request}
	 * {@link ML#REPLY_REQUEST reply} message. Subclasses may override this method
	 * to change the default behavior. In this case, it's just returns null, which
	 * indicates doing nothing.
	 * 
	 * @param message
	 *          the message that precipitated this action
	 * @return <code>null</code> always
	 */
	protected PerformDescriptor verifyDefault(MLMessage message) {
		in("TransientAgent.verifyDefault");
		out("TransientAgent.verifyDefault");
		return null;
	}

	/**
	 * The default code for responding to an <b>outging</b> {@link ML#AGREE agree}
	 * message. Subclasses may override this method to change the default
	 * behavior. In this case, since this method does not know how to do anything,
	 * the return is set up with the status of -55 and the fields are built with
	 * the performative {@link ML#FAILURE failure} and a proper explanation.
	 * 
	 * @param act
	 *          The act of the message.
	 * @param message
	 *          the message the percipitated this action
	 * 
	 * @return the description containing a {@link Status} object and any changes
	 *         to the default return message (a null return indicates "success"
	 *         and no changes).
	 */
	protected PerformDescriptor performDefault(MLMessage message) {
		in("TransientAgent.performDefault");
		Act act = message.getAct();
		String methodName = "perform_" + act.toStringSimplify(-1);
		String warning = "TransientAgent.performDefault: Agent " + getAgentName()
				+ " has no handler (looking for method " + methodName
				+ "(MLMessage)) for messages of type "
				+ message.getParameter(ML.PERFORMATIVE) + "/"
				+ message.getParameter(ML.ACT);
		PerformDescriptor ret = new PerformDescriptor(new Status(-55, warning));
		ret.put(ML.PERFORMATIVE, ML.FAILURE);
		ret.put(
				ML.ACT,
				(new Act(message.getParameter(ML.ACT))).push(
						message.getParameter(ML.PERFORMATIVE)).toString());
		ret.put(ML.CONTENT, warning);
		ret.put(ML.LANGUAGE, "text");
		out("TransientAgent.performDefault");
		return ret;
	}

	/**
	 * The default code for responding to an <b>outging</b> {@link ML#CONTRACT
	 * contract} message. Subclasses may override this method to change the
	 * default behavior. In this case, since this method does not know how to do
	 * anything, the return is set up with the status of -55 and the fields are
	 * built with the performative {@link ML#FAILURE failure} and a proper
	 * explanation.
	 * 
	 * @param message
	 *          the message the percipitated this action
	 * @return the description containing a {@link Status} object and any changes
	 *         to the default return message (a null return indicates "success"
	 *         and no changes).
	 */
	protected PerformDescriptor monitorDefault(MLMessage message) {
		in("TransientAgent.monitorDefault");
		Act act = message.getAct();
		String methodName = "monitor_" + act.toStringSimplify(-1);
		String warning = "TransientAgent.monitorDefault: Agent " + getAgentName()
				+ " has no handler (looking for method " + methodName
				+ "(MLMessage)) for messages of type "
				+ message.getParameter(ML.PERFORMATIVE) + "/"
				+ message.getParameter(ML.ACT);
		PerformDescriptor ret = new PerformDescriptor(new Status(-55, warning));
		ret.put(ML.PERFORMATIVE, ML.FAILURE);
		ret.put(
				ML.ACT,
				(new Act(message.getParameter(ML.ACT))).push(
						message.getParameter(ML.PERFORMATIVE)).toString());
		ret.put(ML.CONTENT, warning);
		ret.put(ML.LANGUAGE, "text");
		out("TransientAgent.monitorDefault");
		return ret;
	}

	/**
	 * The default code for responding to an <b>outging</b> {@link ML#AGREE agree}
	 * message. Subclasses may override this method to change the default
	 * behavior. In this case, since this method does not know how to do anything,
	 * the return is set up with the status of -55 and the fields are built with
	 * the performative {@link ML#FAILURE failure} and a proper explanation.
	 * 
	 * @param message
	 *          the message the percipitated this action
	 * @return the description containing a {@link Status} object and any changes
	 *         to the default return message (a null return indicates "success"
	 *         and no changes).
	 */
	protected PerformDescriptor acceptDefault(MLMessage message) {
		in("TransientAgent.acceptDefault");
		Act act = message.getAct();
		String methodName = "accept_" + act.toStringSimplify(-1);
		String warning = "TransientAgent.acceptDefault: Agent " + getAgentName()
				+ " has no handler (looking for method " + methodName
				+ "(MLMessage)) for messages of type "
				+ message.getParameter(ML.PERFORMATIVE) + "/"
				+ message.getParameter(ML.ACT);
		PerformDescriptor ret = new PerformDescriptor(new Status(-55, warning));
		ret.put(ML.PERFORMATIVE, ML.NOTIFY_FAILURE);
		ret.put(
				ML.ACT,
				(new Act(message.getParameter(ML.ACT))).push(
						message.getParameter(ML.PERFORMATIVE)).toString());
		ret.put(ML.CONTENT, warning);
		ret.put(ML.LANGUAGE, "text");
		out("TransientAgent.acceptDefault");
		return ret;
	}

	// dsb
	private ArrayList<Event> subscribeEvents = new ArrayList<Event>();

	/**
	 * 
	 * @param event
	 * @return
	 * @author kremer
	 * @see casa.interfaces.PolicyAgentInterface#getSubscribeEvents(casa.MLMessage)
	 */
	@Override
	public Event[] getSubscribeEvents(Event event) {
		// no base-level subscriptions yet
		// return null;
		return subscribeEvents.toArray(new Event[subscribeEvents.size()]);
	}

	public Event getSubscribeEvents(String conversationID) {
		for (Event e : subscribeEvents) {
			if (((AbstractEvent) e).getOwnerConversationID().equals(conversationID))
				return e; // Can multiple events have the same ID? dsb
		}
		return null;
	}

	/**
	 * @param event
	 * @return
	 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
	 */
	public void addSubscribeEvents(Event event) {
		subscribeEvents.add(event);
	}

	/**
	 * @param event
	 * @return
	 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
	 */
	public void removeSubscribeEvents(Event event) {
		subscribeEvents.remove(event);
	}

	/**
	 * The default code for responding to a {@link ML#PROPOSE_DISCHARGE propose}
	 * message. Subclasses may override this method to change the default
	 * behavior. In this case, it just returns null, which indicates doing
	 * nothing.
	 * 
	 * @param message
	 *          the message that precipitated this action
	 * @return <code>null</code> always
	 */
	protected PerformDescriptor releaseDefault(MLMessage message) {
		in("TransientAgent.releaseDefault");
		out("TransientAgent.releaseDefault");
		return null;
	}

	protected PerformDescriptor evaluateDefault(Act act, MLMessage message) {
		in("TransientAgent.evaluateDefault");
		out("TransientAgent.evaluateDefault");
		return null;
	}

	/**
	 * The default code for responding to a {@link ML#REPLY_PROPOSAL
	 * reply-proposal} message. Subclasses may override this method to change the
	 * default behavior. In this case, it just returns null, which indicates doing
	 * nothing.
	 * 
	 * @param message
	 *          the message that precipitated this action
	 * @return <code>null</code> always
	 */
	protected PerformDescriptor concludeDefault(MLMessage message) {
		in("TransientAgent.concludeDefault");
		out("TransientAgent.concludeDefault");
		return null;
	}

	/**
	 * The default code for dealing with a {@link ML#PROXY proxy} message.
	 * Subclasses may override this method to change the default behavior. In this
	 * case, it just returns a StatusObject<Pair<List<URLDescriptor>, MLMessage>>
	 * object (limited to one element), which indicates that the message will be
	 * forwarded to one agent only.
	 * 
	 * @param message
	 *          the message that precipitated this action
	 * @return <code>null</code> always
	 */
	protected PerformDescriptor assembleDefault(MLMessage message) {
		String content = message.getParameter(ML.CONTENT);

		Pair<URLDescriptor, MLMessage> basicInfo = MLMessage
				.extractBasicProxyInformation(content);

		if (basicInfo == null) {
			String m = "Could not interpret content field in message.";
			if (this.isLoggingTag("warning")) {
				println("warning", m + "(in TransientAgent.assembleDefault)");
			}
			return new PerformDescriptor(new Status(Status.EXCEPTION_CAUGHT, m));
		} else {
			List<URLDescriptor> list = new ArrayList<URLDescriptor>();
			list.add(basicInfo.getFirst());
			return new PerformDescriptor(
					new StatusObject<Pair<List<URLDescriptor>, MLMessage>>(
							new Pair<List<URLDescriptor>, MLMessage>(list,
									basicInfo.getSecond())));
		}

	}

	// /**
	// * Called by the constructor to initialize the run time command line
	// * interface. Subclasses should override this method, but be sure to call
	// * super.initRTCommandInterface() to include these definitions. <br>
	// * Defines the following commands:
	// * <li>debugging: Turn debugging on or off.
	// * <li>exit: Request this agent to exit.
	// * <li>join: Join a Cooperation Domain.
	// * <li>options: Show or set options.
	// * <li>register: Regisiter this agent to a LAC, given the LAC's port number.
	// * <li>send: Send a message to another agent.
	// * <li>unregister: Unregister this agent from the LAC.
	// * <li>withdraw: Withdraw from a Cooperation Domain.
	// */
	// protected void initRTCommandInterface () {
	// in ("TransientAgent.initRTCommandInterface");
	// commandInterpreter.execute ("! helpstyle=html", null);
	// final TransientAgent This = this;
	// final char EOF = (char)-1;
	//
	// //This is a hidden command that will be used to do a "lazy load" of the
	// options command --
	// //"options" takes awhile to initialize, so it's only loaded if its needed.
	// It will
	// //also be used to dynamically execute any lisp commands.
	// //An empty command parameter indicates a "help" ("?") listing was called.
	// addRTCommandInterfaceItem(".dynamic |"
	// +"command(type=string; restOfLine)", new Command()
	// {@Override public Status execute (String line, Map<String,String> params,
	// AgentUI ui){
	// String c = params.get("command");
	// return initRTCommandInterfaceLazy(c, ui);}});
	//
	// addRTCommandInterfaceItem
	// ("read file | file(type=string; required; valuerequired; help=\"file name to read from\") "
	// + "?(help=\"Read lines from a file and execute each line.\"; "
	// + "category=\"read|file\")", new Command()
	// {@Override public Status execute (String line, Map<String,String> params,
	// AgentUI ui)
	// {
	// Status ret = new Status(0);
	// String fileName = params.get("file");
	// boolean agressive = commandInterpreter.setAgressive(false);
	// try {
	// BufferedReader in = new BufferedReader(new FileReader(fileName));
	// while (in.ready()) {
	// String buf = in.readLine();
	// ui.println(buf);
	// ret = This.executeCommand(buf, ui);
	// if (ret.getStatus()<0) {
	// ui.println("Error "+ret.getStatus()+" returned from command '"+buf+"'");
	// if (ret.getExplanation()!=null)
	// ui.println("   "+ret.getExplanation());
	// }
	// }
	// } catch (FileNotFoundException e) {
	// ui.println("File not found: "+fileName);
	// } catch (IOException e) {
	// ui.println(e.toString());
	// }
	// ui.print(Character.toString(EOF));
	// commandInterpreter.setAgressive(agressive);
	// return ret;
	// }});
	//
	// addRTCommandInterfaceItem (
	// "request register.instance | LAC(type=int; default=9000; valuerequired; help=\"LAC port to register to\") "
	// +
	// "?(help=\"Regisiter this agent to a LAC, given the LAC's port number.\"; "
	// + "category=\"request|register.instance\")", new Command () {
	// @Override public Status execute (String line, Map<String,String> params,
	// AgentUI ui) {
	// String p = params.get ("LAC");
	// return doRegisterAgentInstance (Integer.parseInt (p));
	// }
	// });
	//
	// addRTCommandInterfaceItem (
	// "request unregister.instance | LAC(type=int; help=\"LAC port to register from (optional)\") "
	// + "?(help=\"Unregister this agent from the LAC.\"; "
	// + "category=\"request|unregister.instance\")",
	// new Command () {
	// @Override public Status execute (String line, Map<String,String> params,
	// AgentUI ui) {
	// return doUnregisterAgentInstance (true);
	// }
	// });
	//
	// addRTCommandInterfaceItem (
	// "request withdraw.cd | CD(type=string; required; valuerequired; help=\"The URL of the Cooperation Domain to withdraw from.  Should be one that was previously successfully joined.\") "
	// +
	// "request(type=boolean; default=true; help=\"send as request or as an inform? (otherwise a request)\") "
	// + "?(help=\"Withdraw from a Cooperation Domain.\"; "
	// + "category=\"request|withdraw.cd\")", new Command () {
	// @Override public Status execute (String line, Map<String,String> params,
	// AgentUI ui) {
	// String p = params.get ("CD");
	// URLDescriptor cd;
	// try {
	// cd = URLDescriptor.make (p);
	// } catch (URLDescriptorException ex) {
	// return new Status (-4, "Bad CD url descriptor '" + p
	// + "' -- check syntax");
	// }
	// String b = params.get ("request");
	// return doWithdrawCD (cd, b == null ? true : Boolean
	// .getBoolean (b));
	// }
	// });
	//
	// addRTCommandInterfaceItem (
	// "request execute | "
	// +
	// "to(type=string; required; valuerequired; help=\"The URL of the remote agent to execute the command\") "
	// +
	// "command(type=string; required; valuerequired; help=\"The command to be executed by the remote agent\") "
	// + "?(help=\"Send a command to another agent to have it execute it.\"; "
	// + "category=\"request|execute\")", new Command() {
	// @Override public Status execute (String line, Map<String,String> params,
	// AgentUI ui){
	// return
	// commandInterpreter.execute("send request execute "+params.get("to")+" content=\""+params.get("command")+"\"",ui);}});
	//
	// addRTCommandInterfaceItem (
	// "show outstandingrequests | "
	// + "?(help=\"List all outstanding requests.\"; "
	// + "category=\"show|outstandingrequsts\")", new Command() {
	// @Override public Status execute (String line, Map<String,String> params,
	// AgentUI ui){
	// StringBuffer buf = new StringBuffer();
	// for (MLMessage msg: outstandingRequests) {
	// buf.append('\n')
	// .append(msg.getParameter(ML.PERFORMATIVE)).append('/')
	// .append(msg.getAct()).append(": ")
	// .append(msg.getParameter(ML.REPLY_WITH));
	// }
	// ui.print(buf.toString()+'\n'+EOF);
	// return new Status(0,buf.toString());}});
	//
	// out ("TransientAgent.initRTCommandInterface");
	// }

	// /**
	// * This method is used to load "slow loading" commands lazily (when they are
	// needed). "When they
	// * are needed" is when the command interpreter can't find a command, or when
	// * the top-level help is first called.
	// * @param command If the command interpreter can't find
	// * a command this method is called with the command string that can't be
	// interpreted.
	// * If the command interpreter is running top-level help for the first time,
	// * the command parameter will be null.
	// * @param ui an AgentUI object to use for any generqted user IO
	// * @return This method should return a Status object with a status of 0 or
	// more if
	// * it succeeds in loading a command (which signals the command interpreter
	// to
	// * retry the command), or -1 if it fails to load an appropriate command (or
	// commands)
	// * to signal the command interpreter to take the command as an error.
	// */
	// protected Status initRTCommandInterfaceLazy(String command, AgentUI ui){
	// // dynamic load any slow commands (in particular the options command
	// if (command==null) {
	// return new Status(0);
	// }
	// // execute a lisp command
	// if (command!=null && command.length()>=2 && command.charAt(0)=='(') {
	// return abclEval(command,null, ui);
	// }
	//
	// return new Status(-1);
	// }

	// /**
	// * Normally called from an agent's {@link #initRTCommandInterface()} method
	// to add
	// * a new command to the interface. This method merely calls
	// * {@link RTCommandInterpreter#put(String, Command)}, but catches the
	// * {@link ParameterParserException} to print an error message if it does.
	// * @param spec The command specification string (first parameter of {@link
	// RTCommandInterpreter#put(String, Command)})
	// * @param command The command implementation (second parameter of {@link
	// RTCommandInterpreter#put(String, Command)})
	// */
	// public void addRTCommandInterfaceItem(String spec, Command command) {
	// in ("TransientAgent.addRTCommandInterfaceItem");
	// try {
	// commandInterpreter.put (spec, command);
	// } catch (ParameterParserException ex) {
	// println ("error",
	// "Unexepected exception when executing commandInterpreter.put() for command \""+spec.substring(0,spec.indexOf('|'))+"\":\n",
	// ex);
	// }
	// out ("TransientAgent.addRTCommandInterfaceItem");
	// }

	/**
	 * Called by handleReply_registerAgentInstance(). During the constructor, the
	 * Agent registers with the LAC, but has to wait for the LAC to reply before
	 * initializing the file and properties. The initU() method is called at that
	 * time to initialize the file and properties and this initUI() is called
	 * after that. This version of initUI() does nothing. Subclasses should
	 * override as appropriate.
	 */
	@Deprecated
	public void initUI() {
		in("TransientAgent.initUI");
		out("TransientAgent.initUI");
	}

	/**
	 * Used to execute a command string at runtime. It relies of the
	 * {@link #abclEval(String, Map, AgentUI)} method to do the actual work.
	 * 
	 * @param command
	 *          a command string
	 * @return a {@link Status}object with the result of the attempted execution.
	 */
	@Override
	public Status executeCommand(String command, AgentUI ui) {
		in("TransientAgent.execute");
		Status stat = // commandInterpreter.execute (command, ui);
		abclEval(command, null, ui);
		out("TransientAgent.execute");
		return stat;
	}

	// /**
	// * Returns an Iterator that iterates over all the commands the agent
	// supports.
	// *
	// * @return an Iterator that iterates over all the commands the agent
	// supports.
	// */
	// public RTCommandInterpreter.RTCommandInterpreterIterator
	// getCommandInterpreterIterator () {
	// return commandInterpreter.iterator ();
	// }
	//
	// /**
	// * Returns the agent's command interpreter.
	// * @return a reference to the agent's command interpreter.
	// */
	// public RTCommandInterpreter getCommandInterpreter () {
	// return commandInterpreter;
	// }

	/**
	 * Returns a default <em>AgentUI</em> (in this case, a
	 * TransientAgentInternalFrame instance or a TextInterface instance --
	 * depending on the <em>GUI</em> argument). This will normally be called only
	 * if the the -i (interface) qualifier does not appear on the command line. <br>
	 * You should not normally override TransientAgent.makeDefaultInterface() to
	 * change this behaviour in subclasses; instead override makeDefaultGUI() or
	 * makeDefaultTextInterface(). <br>
	 * TransientAgent.makeDefaultInterface() may return null if no default
	 * interface is desired. <br>
	 * 
	 * @param args
	 *          The command line arguments
	 * @param userName
	 *          The name of the user
	 * @param GUI
	 *          set to true if you want a window interface (made by
	 *          makeDefaultGUI()); false if you want to text interface (made by
	 *          makeDefaultTextInterface())
	 * @return some default interface the conforms to <em>AgentUI</em> interface.
	 */
	@Override
	public AgentUI makeDefaultInterface(String[] args, boolean GUI) {
		AgentUI ui;
		if (GUI && !GraphicsEnvironment.isHeadless())
			ui = makeDefaultGUI(args);
		else
			ui = makeDefaultTextInterface(args);
		putUI(ui);
		return ui;
	}

	/**
	 * Returns a default <em>AgentUI</em> as a window. <br>
	 * Subclasses should override TransientAgent.makeDefaultGUI() to change this
	 * behaviour in subclasses <br>
	 * TransientAgent.makeDefaultInterface() may return null if no default
	 * interface can be built or is not desired. <br>
	 * Note that you can elimintate the (annoying) center panel in the default
	 * TransientAgentInternalFrame class by calling ctrPanel.setVisible(false) in
	 * your subclass.
	 * 
	 * @param args
	 *          The command line arguments
	 * @return some default interface the conforms to <em>AgentUI</em> interface.
	 */
	protected AgentUI makeDefaultGUI(String[] args) {
		in("TransientAgent.makeDefaultInterface");
		casa.ui.TransientAgentInternalFrame f = null;
		try {
			Container frame = null;

			/*
			 * check for an exception in the event of LAC==null when this agent isn't
			 * in the LAC's process
			 */
			boolean doInternal;
			try {
				doInternal = (LAC.ProcessInfo.desktop != null);
			} catch (Throwable e) {
				doInternal = false;
			}

			if (doInternal)
				frame = new JInternalFrame();
			else
				frame = new JFrame();
			f = makeDefaultInternalFrame(this, getName(), frame);
			if (doInternal)
				LAC.ProcessInfo.desktop.addAgentWindow(f);
			f.setName(getName());
			// f.setOpaque (true);
			f.pack();
			f.show();
			f.toFront();
			// f.setIcon (true);
		} catch (Exception ex) {
			println(
					"warning",
					"TransientAgent.startUp: Unexpected exception trying to start the window",
					ex);
		}
		notifyObservers(ML.EVENT_BANNER_CHANGED, getBanner());
		out("TransientAgent.makeDefaultInterface");
		return f;
	}

	/**
	 * Create the default internal Frame (usually) with tabs for this agent type.
	 * 
	 * @param agent
	 *          the owner agent
	 * @param title
	 *          the title of the window
	 * @param aFrame
	 *          the owner frame in which this window is to be embedded
	 * @return the frame
	 */
	protected casa.ui.TransientAgentInternalFrame makeDefaultInternalFrame(
			TransientAgent agent, String title, Container aFrame) {
		casa.ui.TransientAgentInternalFrame frame = new TransientAgentInternalFrame(
				agent, title, aFrame);
		notifyObservers(ML.EVENT_BANNER_CHANGED, getBanner());
		return frame;
	}

	/**
	 * Returns a default <em>AgentUI</em> as a window. <br>
	 * Subclasses should override TransientAgent.makeDefaultTextInterface() to
	 * change this behaviour in subclasses <br>
	 * TransientAgent.makeDefaultTextInterface() may return null if no default
	 * interface can be built or is not desired. <br>
	 * 
	 * @param args
	 *          The command line arguments
	 * @return some default interface the conforms to <em>AgentUI</em> interface.
	 */
	protected AgentUI makeDefaultTextInterface(String[] args) {
		AgentUI ui = new TextInterface(this, args, true);
		return ui;
	}

	/**
	 * A creating process should call this to let the agent know of any user
	 * interface it has attached to the agent. Note that this <em>could</em> be
	 * called more than once, which may mean the agent has more than one user
	 * interface. The default behaviour is to just record the <em>most recent</em>
	 * UI notified as the {@link #primaryUI}, and call
	 * {@link #notifyObservers(String, Object)
	 * notifyObservers(ML.EVENT_BANNER_CHANGED,getBanner())} since the interface
	 * could work via the <em>Observer</em> interface and various <em>set*()</em>
	 * and <em>get*()</em> calls on the agent.
	 * 
	 * @param ui
	 *          the user interface to be attached to the agent.
	 */
	@Override
	public void putUI(AgentUI ui) {
		if (primaryUI == null)
			primaryUI = ui;
		UIs.add(ui);
	}

	public AgentUI getUI() {
		return primaryUI;
	}

	public AgentUI[] getUIs() {
		AgentUI ret[] = new AgentUI[UIs.size()];
		return UIs.toArray(ret);
	}

	// TODO fix proxy instantiation
	private void initializeProxy(ParamsMap params, AgentUI ui) throws Exception {
		in("TransientAgent.initializeProxy");
		Class<? extends AgentProxy>[] proxies = getProxies(params, ui);
		if (proxies != null && proxies.length > 0) {
			Class<? extends AgentProxy> proxyClass = proxies[0];

			// we are using the proxy
			usingProxy = true;

			// find our port for the proxy's information
			URLDescriptor url = getURL();
			this.realPort = url.getPort();

			if (SecureProxy.class.isAssignableFrom(proxyClass)) {
				usingSecureProxy = true;

				// find key
				Random pool = null;
				try {
					pool = SecureRandom.getInstance("SHA1PRNG");
				} catch (NoSuchAlgorithmException e) {
					if (isLoggingTag("warning"))
						println("warning",
								"Falling back on lower quality random data for key.");
					pool = new Random();
				}
				key = new byte[KEY_LENGTH];
				pool.nextBytes(key);
				pool = null;

				// get the constructor for the proxy
				Constructor<?> proxyConstructor = null;
				try {
					Class<?>[] proxyFields = { ParamsMap.class, AgentUI.class };
					proxyConstructor = proxyClass.getConstructor(proxyFields);
				} catch (SecurityException ex) {
					// bad class, probably should throw an exception here...
					out("TransientAgent.initializeProxy");
					throw new IPSocketException();
				} catch (NoSuchMethodException ex) {
					// bad class, probably should throw an exception here...
					if (isLoggingTag("warning"))
						println("warning",
								"No such a method exception #1\n" + ex.toString());
					out("TransientAgent.initializeProxy");
					throw new IPSocketException();
				}

				try {
					// proxy will attempt to use the port given for the agent
					ParamsMap p = new ParamsMap(params);
					p.put("REALPORT", realPort, new JavaObject(realPort), false);
					p.put("KEY", key, new JavaObject(key), false);
					Object[] proxyFields = { p, ui };
					proxy = (AgentProxy) proxyConstructor.newInstance(proxyFields);
				} catch (InvocationTargetException ex1) {
					// only "good" exception, it is from the constructor iteself and
					// should therefore be an IPSocketException
					if (ex1.getTargetException() instanceof IPSocketException) {
						out("TransientAgent.initializeProxy");
						throw (IPSocketException) ex1.getTargetException();
					}
				} catch (IllegalArgumentException ex1) {
					// bad class, probably should throw an exception here...
					out("TransientAgent.initializeProxy");
					throw new IPSocketException();
				} catch (IllegalAccessException ex1) {
					// bad class, probably should throw an exception here...
					out("TransientAgent.initializeProxy");
					throw new IPSocketException();
				} catch (InstantiationException ex1) {
					// bad class, probably should throw an exception here...
					out("TransientAgent.initializeProxy");
					throw new IPSocketException();
				}
			} else {
				// get the constructor for the proxy
				Constructor<?> proxyConstructor = null;
				try {
					Class<?>[] proxyFields = { ParamsMap.class, AgentUI.class };
					proxyConstructor = proxyClass.getConstructor(proxyFields);
				} catch (SecurityException ex) {
					// bad class, probably should throw an exception here...
					out("TransientAgent.initializeProxy");
					throw new IPSocketException();
				} catch (NoSuchMethodException ex) {
					// bad class, probably should throw an exception here...
					if (isLoggingTag("warning"))
						println("warning",
								"No such a method exception #2\n" + ex.toString());
					out("TransientAgent.initializeProxy");
					throw new IPSocketException();
				}

				try {
					// proxy will attempt to use the port given for the agent
					ParamsMap p = new ParamsMap(params);
					p.put("REALPORT", realPort, new JavaObject(realPort), false);
					Object[] proxyFields = { p, ui };
					proxy = (AgentProxy) proxyConstructor.newInstance(proxyFields);
				} catch (InvocationTargetException ex1) {
					// only "good" exception, it is from the constructor iteself and
					// should therefore be an IPSocketException
					if (ex1.getTargetException() instanceof IPSocketException) {
						out("TransientAgent.initializeProxy");
						throw (IPSocketException) ex1.getTargetException();
					}
				} catch (IllegalArgumentException ex1) {
					// bad class, probably should throw an exception here...
					out("TransientAgent.initializeProxy");
					throw new IPSocketException();
				} catch (IllegalAccessException ex1) {
					// bad class, probably should throw an exception here...
					out("TransientAgent.initializeProxy");
					throw new IPSocketException();
				} catch (InstantiationException ex1) {
					// bad class, probably should throw an exception here...
					out("TransientAgent.initializeProxy");
					throw new IPSocketException();
				}
			}

			// retrieve the port that the proxy has settled on and
			// set our reported port to the port of the proxy
			this.proxyPort = proxy.getPort();
			url.setPort(this.proxyPort);
		}
		out("TransientAgent.initializeProxy --> made it to the end");
	}

	@Override
	protected SocialCommitment getChosenCommitment() {
		if (commitmentProcessor == null) {
			println("commitments", "TransientAgent.getChosenCommitment(): called with a null commitmentProcessor object.");
			return null;
		}
		return commitmentProcessor.choose();
	}

	@Override
	public boolean hasActiveCommitments() {
		if (commitmentProcessor == null) {
			return false;
		}
		return commitmentProcessor.hasActiveCommitments();
	}

	@Override
	protected SocialCommitment processCommitment(SocialCommitment sc) {
		if (commitmentProcessor == null) {
			println(
					"commitments",
					"TransientAgent.processCommitment(): called with a null commitmentProcessor object.");
			return null;
		}
		return commitmentProcessor.processCommitment(sc);
	}

	/**
	 * If the parent classes {@link AbstractProcess#processCommitments()} doesn't
	 * do anything (returns false), and there is a {@link #commitmentProcessor}
	 * with outstanding commitments, calls the {@link #commitmentProcessor
	 * #processCommitments()} method.
	 * 
	 * @return the commitment a commitment was processed; null otherwise
	 */
	@Override
	protected SocialCommitment processCommitments() {
		SocialCommitment ret = super.processCommitments();
		if (ret != null)
			return ret;

		if (commitmentProcessor == null) {
			println(
					"commitments",
					"TransientAgent.processCommitments(): called with a null commitmentProcessor object.");
			return null;
		}

		int count = commitmentProcessor.getStore().count(
				SocialCommitment.MASK_OUTSTANDING);
		println(
				"commitments",
				"Calling commitmentProcessor with "
						+ count
						+ " outstanding commitments:\n"
						+ commitmentProcessor.getStore().displayString(
								SocialCommitment.MASK_OUTSTANDING));
		return commitmentProcessor.processCommitments();
	}

	@Override
	protected boolean isEventQueueReady() {
		if (commitmentProcessor == null)
			return super.isEventQueueReady();

		final SocialCommitment sc = commitmentProcessor.peek();
		final MLMessage scMessage = sc != null ? sc.getMessage() : null;
		final Event event = eventQueuePeek();
		final boolean scPreempts = scMessage != null
				&& (event == null || scMessage.hasPriority()
						&& scMessage.getPriority() >= event.getPriority());

		return !scPreempts && super.isEventQueueReady();
	}

	/**
	 * Overrides {@link AbstractProcess#handleEvent(Event)} by:
	 * <ol>
	 * <li>if this is a message event and the CONVERSATION-ID matches one of the
	 * current conversations listed in {@link #conversations}, pass the event to
	 * that conversation and collect the policies.
	 * <li>if there were no policies from the match conversations (above), then
	 * collect the ordinary global policies that match the event.
	 * <li>add in all the "always apply" policies from the global policies (if
	 * they apply to the event).
	 * <li>if {@link AbstractProcess#options#threadedEvents} is true, execute the
	 * policies in their own thread, otherwise, execute them in this (the agent's)
	 * thread. In either case, polices are executed by calling
	 * {@link #processPolicies(AbstractProcess, Event, AbstractCollection, Conversation, String, Map)}.
	 * </ol>
	 * If there were no non-ghost policies applied then the event's {@link
	 * Event.fireEvent()} method is called.</p>
	 * <p>
	 * Unsupported conversations should be handled by agent-global policies listed
	 * in {@link #policies}. Supported conversations are created by policies in
	 * {@link #policies}, and continued by the policies in the current
	 * conversations listed in {@link #conversations}. Note that default behaviour
	 * for conversation-specific policies can be specified by policies in the
	 * agent-global policies due the fact that policy filtering allows all
	 * subtypes of <em>performative</em>s, but only the most specific <em>act</em>
	 * .
	 * </p>
	 * 
	 * @param event
	 *          the event to handle
	 * @return the status from the above tests
	 * @see casa.AbstractProcess#handleEvent(Event)
	 * @see TransientAgent#processPolicies(AbstractProcess, Event,
	 *      AbstractCollection, Conversation, String, Map)
	 */
	@Override
	public void handleEvent(Event event) {
		in("TransientAgent.handleEvent");

		assert isAgentThread() : "TransientAgent.handleEvent should only be run in the agent thread";

		println("eventqueue", "Handling event:\n  " + event);

		// find any matching conversations (from the conversationsID in a event)
		LinkedList<Conversation> convs = null;
		String convID = event.getOwnerConversationID();
		if (convID != null) {
			convs = conversations.get(convID);
		}

		final AbstractCollection<Pair<Conversation, AbstractPolicy>> collectedPolicies = new Vector<Pair<Conversation, AbstractPolicy>>();
		String conversationsDoc = ""; // used for debugging
		// final Vector<Conversation> convsToLock = new Vector<Conversation>();

		synchronized (collectedPolicies) {

			// if we have some conversations, collect the applicable policies for
			// (hopefully) one of them
			Conversation conversation = null;
			if (convs != null) {
				for (Conversation conv : convs) {
					Vector<Pair<Conversation, AbstractPolicy>> ps = conv
							.getApplicablePolicies(this, event);
					if (ps != null) {
						if (conversation != null)
							println(
									"error",
									"TransientAgent.handEvent(): Unexpected application of more than one conversation ("
											+ convID + ")\n" + conversation + "\n  --AND--\n" + conv);
						else {
							conversation = conv;
							collectedPolicies.addAll(ps);
							conversationsDoc += ((conversationsDoc.length() > 0 ? ", "
									: "conversation(s) ") + conv.getName() + "(" + conv.getId() + ")");
							// convsToLock.add(conv);
						}
					}
				}
			}

			Map<String, LispObject> map = new TreeMap<String, LispObject>();
			if (conversation != null)
				map.put("conversation", new JavaObject(conversation));
			map.put("agent", new JavaObject(this));
			map.put("event", new JavaObject(event));

			// if there were no applicable policies in the conversations (or there
			// were no conversations),
			// then get the globally applicable ones.
			if (collectedPolicies.size() == 0) {
				if (convs != null)
					println(
							"warning",
							"Event has a conversation ID matching "
									+ conversationsDoc
									+ ", but no applicable policies found.  Applying global policies:\n  "
									+ event.toString());
				Vector<AbstractPolicy> ps = policies.findApplicable(this, event, map);
				collectedPolicies.addAll(pols2convs2pol(ps, null));
				conversationsDoc = "global";
				if (collectedPolicies.size() == 0) {
					if (policiesLastResort != null) {
						if (convs != null)
							println("warning",
									"Event has no applicable policies found.  Applying last-resort policies:\n  "
											+ event.toString());
						ps = policiesLastResort.findApplicable(this, event, map);
						collectedPolicies.addAll(pols2convs2pol(ps, null));
						conversationsDoc = "global, last resort";
					}
				}
			}

			// add in any "always apply" policies we have
			Vector<AbstractPolicy> alwaysApply = policiesAlwaysApply.findApplicable(
					this, event, map);
			if (alwaysApply != null)
				collectedPolicies.addAll(pols2convs2pol(alwaysApply, null));

			// if (collectedPolicies.size()>0) {
			if (options.threadedEvents) {
				String threadName = getName() + " " + event.getEventType();
				MLMessage msg = (event instanceof MessageEvent) ? ((MessageEvent) event)
						.getMessage() : null;
				if (msg != null) {
					threadName += " " + msg.getParameter(ML.PERFORMATIVE) + "/"
							+ msg.getParameter(ML.ACT);
				}
				threadName += "-" + uniqueThreadName++;

				class PolicyProcessingThread extends Subthread {
					String conversationsDoc;

					Conversation conversation;

					AbstractProcess creator;

					Event event;

					Map<String, LispObject> map;

					AbstractCollection<Pair<Conversation, AbstractPolicy>> collectedPolicies;

					PolicyProcessingThread(
							String name,
							AbstractProcess creator,
							Event event,
							AbstractCollection<Pair<Conversation, AbstractPolicy>> collectedPolicies,
							Conversation conv, String convDoc, Map<String, LispObject> map) {
						super(creator,null,name);
						this.creator = creator;
						this.event = event;
						this.conversation = conv;
						this.conversationsDoc = convDoc;
						this.collectedPolicies = collectedPolicies;
						this.map = map;
					}

					@Override
					public void run() {
						processPolicies(creator, event, collectedPolicies, conversation,
								conversationsDoc, map);
						creator.interrupt();
					}
				}

				PolicyProcessingThread policyProcessingThread = new PolicyProcessingThread(
						threadName, this, event, collectedPolicies, conversation,
						conversationsDoc, map);
				policyProcessingThread.start();
			} else {
				processPolicies(this, event, collectedPolicies, conversation,
						conversationsDoc, map);
				getAgent().interrupt();
			}
			// }

		} // synchronized (collectedPolicies)
		out("TransientAgent.handleEvent");
	}

	/**
	 * <ol>
	 * <li>Applies the policies using
	 * {@link PolicyContainer#applyPolicies(casa.interfaces.PolicyAgentInterface, AbstractCollection, Event, Map)}.
	 * <li>if the application of the policies returns a status of 138 or it
	 * returns a {@link StatusObject} with a null object or an empty list, the
	 * event is considered to be "not handled". In this case, if the event is not
	 * a subtype of {@link ML#EVENT_DEFERRED_EXECUTION} or
	 * {@link ML#EVENT_SC_PERFORM_ACTION} then log it in the agent's log file.
	 * <li>fire the event ({@link Event#fireEvent()}).
	 * <li>call {@link AbstractEvent#delete()}, if applicable, to give the event a
	 * chance to clean up.
	 * <li>interupt the agent's thread to insure that it isn't sleeping when we
	 * return (we could be executing in our own thread here).
	 * </ol>
	 * 
	 * @param creator
	 *          The calling agent
	 * @param event
	 *          The event being processed by the polices
	 * @param collectedPolicies
	 *          The list of collected applicable policies
	 * @param conversation
	 *          The conversation iin the context of which this even is being
	 *          processed (or null if none)
	 * @param conversationsDoc
	 *          Any doc string associated with the conversation
	 * @param map
	 *          A key/value map to form additional context for the policy
	 *          execution.
	 */
	private void processPolicies(AbstractProcess creator, Event event,
			AbstractCollection<Pair<Conversation, AbstractPolicy>> collectedPolicies,
			Conversation conversation, String conversationsDoc,
			Map<String, LispObject> map) {
		assert creator != null;
		assert event != null;
		assert collectedPolicies != null;

		Status ret = null;
		boolean handled = false;
		try {
			if (collectedPolicies.size() > 0) {
				// Finally, we can execute all the policies collected above
				println("policies", "Applying " + conversationsDoc + " policies ("
						+ collectedPolicies.size() + ")...");
				if (conversation != null)
					conversation.lock.lock();
				try {
					ret = policies.applyPolicies(creator, collectedPolicies, event, map);
				} catch (Throwable e) {
					ret = new Status(-54, println("error",
							"policies.applyPolicies returned Exception", e));
				} finally {
					if (conversation != null)
						conversation.lock.unlock();
				}

				// determine if the policies handled the event
				handled = true;
				if (ret == null || ret.getStatusValue() == 138)
					handled = false;
				else if (ret instanceof StatusObject<?>) {
					Object statObj = ((StatusObject<?>) ret).getObject();
					if (statObj == null
							|| ((statObj instanceof List<?>) && ((List<?>) statObj).size() == 0)) {
						handled = false;
					}
				}
				assert ret != null;
			} // if (collectedPolicies.size()>0)

			// deal with unhandled non-executable events.
			if (!isA(event.getEventType(), ML.EVENT_EXECUTABLE)
					&& !isA(event.getEventType(), ML.EVENT_SC_PERFORM_ACTION)) {
				boolean hasAllGhosts = true;
				for (Pair<Conversation, AbstractPolicy> p : collectedPolicies) {
					if (!p.getSecond().isGhost())
						hasAllGhosts = false;
				}
				int observerCount = event.countObservers();

				// log errors
				if (hasAllGhosts) { // There were no applicable policies
					if (observerCount == 0)
						println(
								"warning",
								"Non-executable Event (with no observers) has no applicable non-ghost policies:\n  "
										+ event + "\n It's fireEvent() method will be called...");
					else
						println(
								"warning3",
								"Non-executale Event (with "
										+ observerCount
										+ " observers, so they may handle it) has no applicable non-ghost policies:\n  "
										+ event.toString()
										+ "\n It's fireEvent() method will be called.");
				} else if (!handled) {
					// should we log a suspicious event unhandled by policies?
					if (observerCount == 0)
						println("error",
								"Non-executable Event (with no observers) not handled by ("
										+ collectedPolicies.size() + ") policies (return='" + ret
										+ "'):\n  " + event
										+ "\n It's fireEvent() method will be called.");
					else
						println("warning3", "Non-executable Event (with " + observerCount
								+ " observers, so they may handle it) not handled by ("
								+ collectedPolicies.size() + ") policies (return='" + ret
								+ "'):\n  " + event
								+ "\n It's fireEvent() method will be called.");
				}

				// message not handled? last-ditch effort is to call unhandledMessage().
				if ((!handled || hasAllGhosts)
						&& event instanceof MessageEvent
						&& (isA(event.getEventType(), ML.EVENT_MESSAGE_OBSERVED) || isA(
								event.getEventType(), ML.EVENT_MESSAGE_OBSERVED))) {
					unhandledMessage(((MessageEvent) event).getMessage());
				}
			} // if (!isA(event.getEventType(),ML.EVENT_EXECUTABLE))

			// fire the event and give the event a ping to delete itself if applicable
			event.fireEvent();
			if (event instanceof AbstractEvent)
				((AbstractEvent) event).delete();

		} catch (Throwable e) {
			creator.println("error", "Unexpected exception in eventHandling thread, "
					+ this.getName(), e);
		} finally {
			// after processing in a separate thread (which this method may be), make
			// sure
			// we try to process an SCs that may have been created, rather than just
			// waiting for
			// an interrupt from the event queue.
			if (Thread.currentThread() != this)
				creator.interrupt();
		}

	}

	private Vector<PairComparable<Conversation, AbstractPolicy>> pols2convs2pol(
			Vector<AbstractPolicy> ps, Conversation conv) {
		Vector<PairComparable<Conversation, AbstractPolicy>> ret = new Vector<PairComparable<Conversation, AbstractPolicy>>(
				ps.size());
		for (AbstractPolicy p : ps)
			ret.add(new PairComparable(conv, p));
		return ret;
	}

	/**
	 * This method is to be overridden by any subclasses that wish to be informed
	 * whenever a message is actually sent out.
	 * 
	 * @param message
	 */
	@Override
	protected void notifySendingMessage(MLMessage message) {
		if (commitmentProcessor != null)
			commitmentProcessor.notifyMessageSent(message);
		// handleEvent(new MessageEvent(ML.EVENT_MESSAGE_SENT, this, message));
		Event event = new MessageEvent(ML.EVENT_MESSAGE_SENT, this, message);
		// event.setPriority(100);
		queueEvent(event);
	}

	/**
	 * Called when the LAC informs the agent that it is closing. This
	 * implementation does nothing but return success (Status(0)). Subclasses
	 * should override.
	 * 
	 * @return Status(0)
	 */
	protected Status handleLACClosing() {
		return new Status(0);
	}

	/**
	 * Refresh agent behaviour from the options object. Subclasses should override
	 * as appropriate.
	 */
	@Override
	public void realizeAgentBehaviourFromOptions() {
		in("updateFromOptions");
		super.realizeAgentBehaviourFromOptions();
		// updateConversationProtocolType (options.strategy);
		out("updateFromOptions");
	}

	/**
	 * Responds to a request from another agent to get this agent's name.
	 * 
	 * @param msg
	 *          the message that percipitated this call
	 * @return a successful PerformDescrptor containing the agent's name in the
	 *         {@link ML#CONTENT content} field
	 */
	protected PerformDescriptor perform_get_name(MLMessage msg) {
		in("TransientAgent.perform_get_name");
		PerformDescriptor pd = new PerformDescriptor();
		pd.put(ML.CONTENT, getAgentName());
		pd.put(ML.LANGUAGE, "text");
		out("TransientAgent.perform_get_name");
		return pd;
	}

	/**
	 * The code for responding to an {@link ML#INFORM inform} message. Subclasses
	 * may override this method to change the behavior. In this case, it calls
	 * {@link #consider_new_MenuItem(MLMessage)} which handles both the
	 * {@link ML#INFORM inform}- and the {@link ML#REQUEST request}-type messages.
	 * 
	 * @param message
	 *          the message the percipitated this action
	 * @return the description containing a {@link Status} object and any changes
	 *         to the default return message (a null return indicates "success"
	 *         and no changes).
	 */
	protected PerformDescriptor accept_new_MenuItem(MLMessage message) {
		in("TransientAgent.accept_new_MenuItem");
		PerformDescriptor ret = consider_new_MenuItem(message);
		out("TransientAgent.accept_new_MenuItem");
		return ret;
	}

	/**
	 * TODO: rkyee: figure out why EVENT_WITHDRAW_CD is never invoked
	 * 
	 * @param notification
	 *          the event to consider for join/exit agent
	 */
	protected void processObserverNotification(
			final ObserverNotification notification) {
		if (ML.EVENT_JOIN_CD.equals(notification.getType())) {
			Vector<URLDescriptor> members = cooperationDomainMembers.get(notification
					.getAgentUrl());
			if (members == null) {
				members = new Vector<URLDescriptor>();
				cooperationDomainMembers.put(notification.getAgentUrl(), members);
			}
			if (!members.contains(notification.getObject()))
				members.add((URLDescriptor) notification.getObject());
		} else if (ML.EVENT_WITHDRAW_CD.equals(notification.getType())) {
			Vector<URLDescriptor> members = cooperationDomainMembers.get(notification
					.getAgentUrl());
			if (members == null) {
				members = new Vector<URLDescriptor>();
				cooperationDomainMembers.put(notification.getAgentUrl(), members);
			}
			if (members.contains(notification.getObject()))
				members.remove(notification.getObject());
		}
	}

	protected PerformDescriptor accept_update(MLMessage message) {
		PerformDescriptor ret = new PerformDescriptor();

		try {
			Object content = message.getParameter(ML.CONTENT, Object.class);
			if (content instanceof ObserverNotification) {
				processObserverNotification((ObserverNotification) content);
			}
		} catch (ParseException e) {
			println("error", "TransientAgent.accept_update(): ", e);
		}

		return ret;
	}

	/**
	 * The code for responding to an {@link ML#INFORM inform} or
	 * {@link ML#REQUEST request} message. Subclasses may override this method to
	 * change the behavior. In this case, adds the new command described in the
	 * {@link ML#CONTENT content} field to it's list of commands. However, if this
	 * message is a {@link ML#REQUEST request} performative, then it shortcuts the
	 * normal request/agree/propoase-discharge/reply-proposal-discharge sequence
	 * by immediatly sending a {@link ML#PROPOSE_DISCHARGE propose}/
	 * {@link ML#DISCHARGE discharge} (skipping the {@link ML#AGREE agree} step).
	 * 
	 * @param message
	 *          the message the percipitated this action
	 * @return the description containing a {@link Status} object and any changes
	 *         to the default return message (a null return indicates "success"
	 *         and no changes).
	 */
	protected PerformDescriptor consider_new_MenuItem(MLMessage message) {
		in("TransientAgent.consider_new_MenuItem");
		PerformDescriptor ret;
		try {
			// TODO consider_new_menuItem() needs updating to handle new Lisp
			// interpreter -- rck
			throw new Exception(
					"consider_new_menuItem() needs updating to handle new Lisp interpreter.");
			// Object content[] = (Object[])
			// CASAUtil.unserialize(message.getParameter(ML.CONTENT));
			// if (content.length != 2)
			// throw new ParseException("Expected content of type String[2]", 0);
			// String spec = (String) (content[0]);
			// String remoteCmd = (String) (content[1]);
			// Object o = commandInterpreter.put(spec, new AgentCommandCommand(this,
			// remoteCmd));
			// //notifyObservers(new
			// casa.State(ObservableEvent.STATE_CHANGED_COMMANDS));
			// notifyObservers(ML.EVENT_CHANGED_COMMANDS, null);
			// ret = new PerformDescriptor(new Status(0, "New command "
			// + (o == null ? "added to" : "replaced in") + " command specs"));
			// if (isAPerformative(message,ML.REQUEST)) {
			// ret.put(ML.PERFORMATIVE,ML.SUCCESS);
			// ret.put(ML.ACT,message.getAct().push(ML.DISCHARGE).toString());
			// }
		} catch (Exception ex) {
			println("warning", "Exception during respondToNewCommand", ex);
			ret = new PerformDescriptor(new Status(-9, ex.toString()));
			ret.put(ML.PERFORMATIVE, ML.FAILURE);
			ret.put(ML.LANGUAGE, Status.class.getName());
			ret.put(ML.CONTENT, ret.toString());
			if (isAPerformative(message, ML.REQUEST)) {
				ret.put(ML.ACT, message.getAct().push(ML.DISCHARGE).toString());
			}
		}
		out("TransientAgent.consider_new_MenuItem");
		return ret;
	}

	@Override
	public boolean authorizeMessage(MLMessage msg) {
		in("TransientAgent.authorizeMessage");
		boolean result = authorizedByProxy(msg);
		out("TransientAgent.authorizeMessage");
		return result;
	}

	protected boolean authorizedByProxy(MLMessage msg) {
		in("TransientAgent.authorizedByProxy");
		boolean authorizedByProxy = false;
		if (usingSecureProxy) {
			long messageUniqueNumber = SecureProxy.verifySignature(msg, key);
			if (messageUniqueNumber > uniqueNumber) {
				uniqueNumber = messageUniqueNumber;
				msg.removeParameter(ML.SIGNATURE);
				authorizedByProxy = true;
			}
		} else {
			authorizedByProxy = true;
		}
		out("TransientAgent.authorizedByProxy");
		return authorizedByProxy;
	}

	/**
	 * Adds withdrawing from CDs ({@link #pendingFinishRun_withdrawFromAllCDs()})
	 * and unregistering with the LAC (
	 * {@link #pendingFinishRun_unregisterAgentInstance()}) the the superclass's
	 * actions.
	 * 
	 * @see AbstractProcess#pendingFinishRun()
	 */
	@Override
	protected void pendingFinishRun() {
		in("TransientAgent.pendingFinishRun");
		super.pendingFinishRun();

		// SECURITY COMMENT
		//
		// VotingSupervisor and VoterAgent should overide
		// pendingFinishRun_withdrawFromAllCDs() and do nothing, to prevent them
		// from trying to withdraw from CDs upon exit.

		pendingFinishRun_withdrawFromAllCDs();

		pendingFinishRun_unregisterAgentInstance();

		out("TransientAgent.pendingFinishRun");
	}

	/**
	 * Unsubscribes from an any watched CDs ({@link #watchedCooperationDomains})
	 * by calling {@link #doCDSubscribeMembership(URLDescriptor, boolean)
	 * doCDSubscribeMembership(URLDescriptor, false)} and withdraws from any
	 * joined CDs ({@link #joinedCooperationDomains}) by calling
	 * {@link #doWithdrawCD(URLDescriptor, boolean) doWithdrawCD(URLDescriptor,
	 * false)}.
	 */
	protected void pendingFinishRun_withdrawFromAllCDs() {
		for (URLDescriptor cd : watchedCooperationDomains.keySet()) {
			Status tempStatus = doCDSubscribeMembership(cd, false);
			if (tempStatus.getStatusValue() != 0) {
				println(
						"warning",
						"TransientAgent.pendingFinishRun_withdrawFromAllCDs: Cannot unregister (unattach from observing CD membership)",
						tempStatus);
			}
		}

		for (URLDescriptor tempCD : joinedCooperationDomains) {
			Status tempStatus = doWithdrawCD(tempCD, false);
			if (tempStatus.getStatusValue() != 0) {
				println(
						"warning",
						"TransientAgent.pendingFinishRun_withdrawFromAllCDs: Cannot unregister (withdraw from CD)",
						tempStatus);
			}
		}
	}

	/**
	 * If this agent is registered with the lack ({@link #isRegistered()}) then
	 * unregister it by calling {@link #doUnregisterAgentInstance(boolean)
	 * doUnregisterAgentInstance(false)}.
	 */
	protected void pendingFinishRun_unregisterAgentInstance() {
		in("TransientAgent.pendingFinishRun_unregisterAgentInstance");

		if (isRegistered()) {
			Status tempStatus = doUnregisterAgentInstance(false);

			if (tempStatus.getStatusValue() != 0) {
				println(
						"warning",
						"TransientAgent.pendingFinishRun_unregisterAgentInstance: Cannot unregister (unregister this instance with the LAC)",
						tempStatus);
			}
		}

		out("TransientAgent.pendingFinishRun_unregisterAgentInstance");
	}

	/**
	 * Attempts to resolve the url of the reciever field of a message. It does
	 * this by a call to doResolveURL().
	 * 
	 * @param msg
	 *          the message to be updated.
	 * @return Status(0) if msg was successfully updated, Status(-ve) otherwise.
	 */
	@Override
	protected Status resolveConnectException(MLMessage msg,
			final Runnable1<String, Status> cmd) {
		in("TransientAgent.resolveConnectException");
		URLDescriptor url = null;
		try {
			url = URLDescriptor.make(msg.getParameter(ML.RECEIVER));
		} catch (URLDescriptorException ex1) {
			out("TransientAgent.resolveConnectException");
			return new Status(-8,
					"TransientAgent.resolveConnectException: found mangled receiver field");
		}
		Status stat = null;
		if (isAgentThread()) {
			final URLDescriptor thisURL = url;
			this.makeSubthread(new Runnable() {
				@Override
				public void run() {
					doResolveURL(thisURL, 6000, cmd);
				}
			}).start();
			stat = new Status(1);
		}
		else {
			stat = doResolveURL(url, 6000, cmd);
		}
		out("TransientAgent.resolveConnectException");
		return stat;
	}

	/**
	 * Sends a the request to the appropriate LAC to resolve the URL.
	 * 
	 * @param url
	 *          The URL to be resolved. Note that this URLDescriptor object will
	 *          be updated to reflect the resolution.
	 * @param timeout
	 *          milliseconds to wait before returning if something goes wrong
	 * @param cmd
	 *          A {@link Command} object to execute when the LAC returns with an
	 *          successful or failed resolution
	 * @return Status(0) if url is successfully updated, Status(-ve) if not
	 */
	public Status doResolveURL(final URLDescriptor url, final long timeout,
			final Runnable1<String, Status> cmd) {

		// If there is no lac given, assume it's at port 9000
		if (!url.hasLACport()) {
			url.setDataValue("lac", "9000");
		}

		if (url.isResolvable()) {
			final URLDescriptor lac;
			try {
				lac = URLDescriptor.make(url.getHost(), url.getLACport());
			} catch (URLDescriptorException e) {
				return new Status(-3, println("error", "TransientAgent.doResolveURL()", e));
			}
//			final URLDescriptor lac = URLDescriptor.make(url);
//			lac.setPort(url.getLACport());
//			lac.setPath("");
			MLMessage req = getNewMessage(ML.REQUEST, ML.RESOLVE_URL, lac);
			req.setParameter(ML.SENDER, getURL().toString(lac));
			req.setParameter(ML.LANGUAGE, URLDescriptor.class.getName());
			req.setParameter(ML.CONTENT, url.toString(lac));
			req.setParameter(ML.REPLY_BY,
					Long.toString(System.currentTimeMillis() + timeout));
			req.setParameter(ML.REPLY_WITH, getUniqueRequestID());

			if (!isAgentThread()) {
				StatusObject<MLMessage> replyStat = sendRequestAndWait(req, timeout);
				MLMessage reply = replyStat.getObject();

				if (reply != null
						&& reply.getParameter(ML.PERFORMATIVE).equals(ML.PROPOSE)) {
					return cmd.run(reply.getParameter(ML.CONTENT));
				} else {
					return new Status(-19,
							"TransientAgent.doResolveURL(): Attempt to resolve URL " + url
									+ " through LAC " + lac + " failed with returned message: \n"
									+ (reply == null ? "(" + replyStat.getExplanation() + ")"
											: reply.toString(true))
									+ CASAUtil.getStack(null));
				}
			} else {
				final MLMessage reqCopy = req;
				new Thread(new Runnable() {
					@Override
					public void run() {
						StatusObject<MLMessage> replyStat = sendRequestAndWait(reqCopy,
								timeout);
						MLMessage reply = replyStat.getObject();

						if (reply != null
								&& reply.getParameter(ML.PERFORMATIVE).equals(ML.PROPOSE)) {
							cmd.run(reply.getParameter(ML.CONTENT));
						} else {
							println("error",
									"TransientAgent.doResolveURL(): Attempt to resolve URL "
											+ url
											+ " through LAC "
											+ lac
											+ " failed with returned message: \n"
											+ (reply == null ? "(" + replyStat.getExplanation() + ")"
													: reply.toString(true))
											+ CASAUtil.getStack(null));
						}
					}
				}).start();
				return new Status(
						15,
						"TransientAgent.doResolveURL(): Spawned new thread to request resolved URL for "
								+ url
								+ " through LAC "
								+ lac
								+ ".  Service will be performed in that thread.  If status is required, please make this request in a thread that is not an agent's main thread.");
			}

			// deferedExecs.put(req.getParameter(ML.REPLY_WITH),cmd);
			// ret = sendMessage(req);
		} else
			return new Status(-1, "Unable to resolve URL " + url.toString()
					+ " (isResolvable() returned false)");
	}

	// from AbstractProcess///////////////////////////////////////
	// ///////////////////////////////////////////////////////////
	// accessors for testing
	protected boolean verifyCD(URLDescriptor url) {
		in("TransientAgent.verifyCD");
		if (joinedCooperationDomains.contains(url)) {
			out("TransientAgent.verifyCD");
			return true;
		} else {
			out("TransientAgent.verifyCD");
			return false;
		}
	}

	/**
	 * @return
	 */
	@Override
	public Set<URLDescriptor> getJoinedCooperationDomains() {
		return joinedCooperationDomains;
	}

	@Override
	public void removeCooperationDomains(URLDescriptor url) {
		joinedCooperationDomains.remove(url);
	}

	@Override
	public Vector<URLDescriptor> getMembers(URLDescriptor cd) {
		return cooperationDomainMembers.get(cd);
	}

	public Vector<URLDescriptor> addMembers(URLDescriptor cd,
			Vector<URLDescriptor> urls) {
		return cooperationDomainMembers.put(cd, urls);
	}

	/**
	 * Returns the URL of the LAC the agent is registered to.
	 * 
	 * @return URLDescriptor of the LAC the agent is registered to. Null if it
	 *         isn't registered.
	 */
	@Override
	public URLDescriptor getLACURL() {
		URLDescriptor url = getURL();
		if (url.getLACport() == 0) {
			out("TransientAgent.getLACURL");
			return null;
		}
		try {
			url = URLDescriptor.make(url.getHost(), url.getLACport());
		} catch (URLDescriptorException e) {
			println("error", "TransientAgent.getLACURL()", e);
			return null;
		}
		return url;
	}

	// /////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////

	/*
	 * AbstractProcess methods
	 */

	// Registration as agent
	/**
	 * If the Agent is registered to a LAC returns true. Otherwise returns false.
	 * 
	 * @return true if the Agent is registered to a LAC, false if it is not.
	 */
	@Override
	public boolean isRegistered() {
		return getURL().hasLACport();
	}

	/**
	 * determines if this process can be stopped. A subclass may override if there
	 * are more constraints on stopping. This implementation checks returns the
	 * conjunct of {@link casa.interfaces.ProcessInterface#isStoppable()} and
	 * {@link #commitmentProcessor}.{@link MessageProcessor#isStoppable()}.
	 * 
	 * @return true if the process is stoppable, false otherwise.
	 * @see casa.interfaces.ProcessInterface#isStoppable()
	 */
	@Override
	public boolean isStoppable() {
		return super.isStoppable()
				&& (commitmentProcessor != null ? commitmentProcessor.isStoppable()
						: true);
	}

	// /////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////
	// OBLIGATIONS FROM
	// AgentInterface//////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////

	// ///////////////////////////////////////////////////////////////////////////
	// Ping///////////////////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * Pings another agent. Attempts to ping the agent corresponding to the
	 * {@link URLDescriptor} <em>agent</em> by sending a message of the format:
	 * <table border="1" bgcolor="gold" cellpadding="3">
	 * <tr>
	 * <td><a href="doc-files/performatives.gif">performative</a></td>
	 * <td>request</td>
	 * </tr>
	 * <tr>
	 * <td>{@link ML#ACT act}</td>
	 * <td>{@link ML#PING ping}</td>
	 * </tr>
	 * </table>
	 * Note that his method <b>only </b> sends the message; it relies on
	 * {@link #release_ping(MLMessage)} to do the actual processing of the
	 * returned reply.
	 * 
	 * @param agent
	 *          URLDescriptor of the agent to ping
	 * @param timeout
	 *          maxiumum time (in milliseconds) to wait for the ping response
	 * @return a Status object with value 0 for success, or describing the failure
	 *         otherwise.
	 * @see casa.interfaces.TransientAgentInterface#doPing(casa.channels.URLDescriptor,
	 *      long)
	 */
	@Override
	public Status doPing(URLDescriptor agent, long timeout) {
		in("TransientAgent.doPing");
		String m[] = { ML.REPLY_BY,
				Long.toString(System.currentTimeMillis() + timeout) };
		Status ret = sendMessage(ML.REQUEST, ML.PING, agent, m);
		out("TransientAgent.doPing");
		return ret;
	}

	/**
	 * Pings another agent synchronously. Attempts to ping the agent corresponding
	 * to the {@link URLDescriptor}<em>agent</em> by sending a REQUEST message for
	 * the agent's URL, and returning that URL.
	 * 
	 * @param agent
	 *          URLDescriptor of the agent to ping
	 * @param timeout
	 *          maxiumum time (in milliseconds) to wait for the ping response
	 * @return The agent's self-described URL or null if there was no response
	 *         within the timeout period.
	 * @see casa.interfaces.TransientAgentInterface#doPing_sync(casa.channels.URLDescriptor,
	 *      long)
	 */
	@Override
	public URLDescriptor doPing_sync(URLDescriptor agent, long timeout) {
		in("TransientAgent.doPing_sync");
		MLMessage ask = null;
		ask = MLMessage.getNewMLMessage(ML.PERFORMATIVE, ML.REQUEST, ML.ACT,
				ML.EXECUTE, ML.CONTENT, "(agent.get-url)", ML.RECEIVER,
				agent.toString());
		StatusObject<MLMessage> stat = sendRequestAndWait(ask, timeout);
		MLMessage reply = stat.getObject();
		URLDescriptor ret = null;
		if (reply != null) {
			try {
				ret = reply.getURLParameter(ML.SENDER);
			} catch (Exception e) {
				println("error", "TransientAgent.doPing_sync", e);
			}
		}
		out("TransientAgent.doPing_sync");
		return ret;
	}

	/**
	 * Responds to a received ping request message by constructing a complimentary
	 * ping reply message and returning it to the sender.
	 * 
	 * @param m
	 *          The MLMessage received.
	 * @return a Status object reflecting the success or failure of the operation
	 */
	protected PerformDescriptor release_ping(MLMessage m) {
		in("TransientAgent.release_ping");
		String sender = m.getParameter(m.getFromString());
		if (isLoggingTag(ML.PING))
			println(ML.PING,
					"Recieved ping from " + sender + ": " + m.getParameter(ML.CONTENT));
		out("TransientAgent.release_ping");
		return new PerformDescriptor();
	}

	// ************ Client methods for an REQUEST getAgentsRunning conversation
	// **************

	// ************ Client methods for an REQUEST getAgentsRegistered conversation
	// **************

	/**
	 * Responds to a received forward.message inform message by passing on the
	 * message encapsulated in the content part of the message.
	 * 
	 * @param m
	 *          The MLMessage received.
	 * @return a Status object reflecting the success or failure of the operation
	 *         TODO fix this -- it doesn't work
	 */
	protected PerformDescriptor accept_forward_message(MLMessage m) {
		in("TransientAgent.consider_forward_message");
		PerformDescriptor ret = null;
		String buf = m.getParameter(ML.CONTENT);
		try {
			MLMessage msg = MLMessage.fromString(buf);
			ret = new PerformDescriptor(sendMessage(msg));
			if (ret.getStatus().getStatusValue() != 0) {
				println("warning",
						"TransientAgent.consider_forward_message: failed to sendMessage\n"
								+ ret.toString());
			}
		} catch (Exception ex) {
			ret = new PerformDescriptor(
					new Status(
							-6,
							"TransientAgent.consider_forward_message: found malformed message in content field of envelope; message unformarded: "
									+ buf));
			if (isLoggingTag("warning"))
				println("warning", ret.getStatus().getExplanation());
		}
		out("TransientAgent.consider_forward_message");
		return ret;
	}

	/**
	 * Responds to a received {@link ML#INFORM inform}/ {@link ML#LAC_CLOSING
	 * LAC_closing} inform message by passing on the message encapsulated in the
	 * content part of the message.
	 * 
	 * @param m
	 *          The MLMessage received.
	 * @return a Status object reflecting the success or failure of the operation
	 */
	protected PerformDescriptor accept_LAC_closing(MLMessage m) {
		in("TransientAgent.accept_LAC_closing");
		PerformDescriptor ret = new PerformDescriptor(handleLACClosing());
		if (LAC.ProcessInfo.process != null) {
			exit();
		}
		out("TransientAgent.accept_LAC_closing");
		return ret;
	}

	protected Object perform_query_ref(String item) {
		if ("url".equalsIgnoreCase(item))
			return getURL().toString();
		if ("current-conversation-ids".equalsIgnoreCase(item))
			return getConversations().keySet().toArray();
		return null;
	}

	/**
	 * Responds to a query-ref message by interpreting the act field, attempting
	 * to execute it, and returning a message with the result.
	 * 
	 * @param m
	 *          The MLMessage received.
	 * @return a Status object reflecting the success or failure of the operation
	 */
	public PerformDescriptor perform_query_ref(MLMessage m) {
		String item;
		PerformDescriptor result = new PerformDescriptor();
		if (ML.FIPA_SL.equalsIgnoreCase(m.getParameter(ML.LANGUAGE))) {
			item = m.getParameter(ML.CONTENT);
			try {
				String res = queryRef_toString(item);
				result.put(ML.LANGUAGE, ML.FIPA_SL);
				result.put(ML.CONTENT, res);// CASAUtil.toQuotedString(res));
			} catch (ParseException e) {
				result = new PerformDescriptor(-1, "Bad parse of expression: '" + item	+ "'");
				result.put(ML.PERFORMATIVE, ML.FAILURE);
				result.put(ML.LANGUAGE, "text");
			}
		} else {
			Act act = m.getAct();
			item = act.get(act.size() - 1);
			Object content = perform_query_ref(item);
			if (content != null) {
				result.put(ML.LANGUAGE, "casa.*");
				result.put(ML.CONTENT, CASAUtil.serialize(content));
			} else {
				result.put(ML.PERFORMATIVE, ML.FAILURE);
				result.put(ML.CONTENT, "Don't know item '" + item + "'.");
				result.put(ML.LANGUAGE, "text");
				result.setStatus(1, "Don't know item '" + item + "'.");
			}
		}
		println("query", "query for " + item + " returned '" + result + "'.");
		return result;
	}

	/**
	 * Responds to a query-ref message by interpreting the act field, attempting
	 * to execute it, and returning a message with the result.
	 * 
	 * @param m
	 *          The MLMessage received.
	 * @return a Status object reflecting the success or failure of the operation
	 */
	public PerformDescriptor consider_query_ref(MLMessage m) {
		PerformDescriptor result = null;
//		boolean shortcutting = true;
		String contentString = m.getParameter(ML.CONTENT);
		Act act = m.getAct();
		boolean success = false;
		if (ML.FIPA_SL.equalsIgnoreCase(m.getParameter(ML.LANGUAGE))) {
			try {
				String res = queryRef_toString(contentString);
//				result.put(key, value)
				success = true;
//				result = new PerformDescriptor(DROP_ACTION);
//				MLMessage msg = MLMessage.constructReplyTo(m, getUniqueRequestID(),
//						null);
//				msg.setParameter(ML.PERFORMATIVE, ML.INFORM_REF);
//				msg.setParameter(ML.ACT, "discharge|perform|TOP"); // msg.removeParameter(ML.ACT);
//																													 // //msg.setParameter(ML.ACT,
//																													 // act.toString());
//				msg.setParameter(ML.LANGUAGE, ML.FIPA_SL);
//				msg.setParameter(ML.CONTENT, res);// CASAUtil.toQuotedString(res));
//				msg.setParameter(ML.CONVERSATION_ID, getUniqueRequestID());
//				sendMessage(msg);
			} catch (ParseException e) {
				String message = "Bad parse of expression: '" + contentString + "'";
				result = new PerformDescriptor(-1, message);
				result.put(ML.CONTENT, println("error", message, e));
				result.put(ML.LANGUAGE, "text");
			}
		} 
		else {
			String actAtom = act.get(act.size() - 1);
			Object content = perform_query_ref(actAtom);
			if (content == null) {
				boolean retUnknown = true;
				if (retUnknown) {
					result = new PerformDescriptor(DROP_ACTION);
					MLMessage msg = MLMessage.constructReplyTo(m, getUniqueRequestID(),
							null);
					msg.setParameter(ML.PERFORMATIVE, ML.NOT_UNDERSTOOD);
					msg.setParameter(ML.ACT, m.getParameter(ML.PERFORMATIVE) + "|"
							+ act.toString());
					msg.setParameter(ML.LANGUAGE, "text");
					msg.setParameter(ML.CONTENT, "Don't know item '" + actAtom + "'.");
					sendMessage(msg);
				} else {
					result = new PerformDescriptor(-1, "Don't know item '" + actAtom
							+ "'.");
				}
			} else {
				result = new PerformDescriptor(DROP_ACTION);
				MLMessage msg = MLMessage.constructReplyTo(m, getUniqueRequestID(),
						null);
				msg.setParameter(ML.PERFORMATIVE, ML.INFORM_REF);
				msg.setParameter(ML.ACT, actAtom);
				msg.setParameter(ML.LANGUAGE, "casa.*");
				msg.setParameter(ML.CONTENT, CASAUtil.serialize(content));
				sendMessage(msg);
			}
		}
//		if (shortcutting) {
//		}
		return result;
	}

	static private Set<String> privateTerms = null;

	public boolean isTrusted(URLDescriptor url, Node node) {
		if (node == null)
			return false;

		// don't allow any predicates that are in the privateTerms list
		if (privateTerms == null) {
			privateTerms = new HashSet<String>();
			privateTerms.add("agent-name");
			privateTerms.add("url");
		}
		if (node instanceof NotNode) {
			NotNode t = (NotNode) node;
			Node[] children = t.children();
			node = children[0];
		}
		if (node instanceof PredicateNode) {
			PredicateNode t = (PredicateNode) node;
			Node[] children = t.children();
			node = children[0];
		}
		if (node instanceof SymbolNode) {
			SymbolNode t = (SymbolNode) node;
			if (privateTerms.contains(t.lx_value())) {
				return false;
			}
		}

		// VERY trusting. All agents are trusted
		return true;
	}

	public Node parseJADENode(String node) {
		SLParser parser = SLParser.getParser();
		try {
			return parser.parseTerm(node, true);
		} catch (jade.semantics.lang.sl.parser.ParseException e) {
		}
		try {
			return parser.parseContent(node, true);
		} catch (jade.semantics.lang.sl.parser.ParseException e) {
		}
		try {
			return parser.parseFormula(node, true);
		} catch (jade.semantics.lang.sl.parser.ParseException e) {
			println(
					"error",
					"TransientAgent.parseJADENode(\""
							+ node
							+ "\"): parameter can't be interpreted as a Term, Content, or Formula",
					e);
		}
		return null;
	}

	public boolean isTrusted(URLDescriptor url, String node) {
		return isTrusted(url, parseJADENode(node));
	}

	// /**
	// * Responds to a query-ref message by interpreting the act
	// * field, attempting to execute it, and returning a message with the result.
	// *
	// * @param m The MLMessage received.
	// * @return a Status object reflecting the success or failure of the
	// operation
	// */
	// public PerformDescriptor accept_query_ref (MLMessage m) {
	// }

	/**
	 * Retrieves the response to an earlier query-ref message by interpreting the
	 * act field and content field.
	 * 
	 * @param m
	 *          The MLMessage received.
	 * @return a Status object reflecting the success or failure of the operation
	 */
	public PerformDescriptor release_query_ref(MLMessage m) {
		PerformDescriptor result = new PerformDescriptor();
		if (ML.FIPA_SL.equalsIgnoreCase(m.getParameter(ML.LANGUAGE))) {
			try {
				URLDescriptor sender = m.getSender();
				Content cont = (Content) m.getContent();
				ListOfContentExpression contentList = cont.as_expressions();
				// IdentifyingExpression contNode =
				// (IdentifyingExpression)contentList.getFirst().children()[0];
				// Formula formula = (Formula)contNode.as_formula();
				FormulaContentExpressionNode contNode = (FormulaContentExpressionNode) contentList
						.getFirst();
				Formula formula = (Formula) contNode.getElement();
				assert formula instanceof EqualsNode;
				EqualsNode eqNode = (EqualsNode) formula;
				Node[] children = eqNode.children();
				assert children.length == 2;
				IdentifyingExpression idExp = null;
				TermSetNode termSet = null;
				for (Node c : children) {
					if (c instanceof IdentifyingExpression)
						idExp = (IdentifyingExpression) c;
					else if (c instanceof TermSetNode)
						termSet = (TermSetNode) c;
				}
				assert idExp != null;
				assert termSet != null;
				children = idExp.children();
				assert children.length == 2;
				VariableNode var = null;
				PredicateNode pred = null;
				for (Node c : children) {
					if (c instanceof VariableNode)
						var = (VariableNode) c;
					else if (c instanceof PredicateNode)
						pred = (PredicateNode) c;
				}
				for (Term t : (Collection<Term>) termSet.as_terms().asACollection()) {
					// For some reason, the following isn't working, so we do it as
					// strings. :(
					// Term theTerm = idExp.instantiate(var.lx_name(), t);
					// Should be the above, but...
					String instantiatedFormulaString = pred.toString().replaceAll(
							"\\" + var.toString(), t.toString());
					Formula instantiatedFormula = SLParser.getParser().parseFormula(
							instantiatedFormulaString);
					if (isTrusted(sender, instantiatedFormula)) {
						kBase.assertFormula(instantiatedFormula);
						println("kb", "Asserting from " + sender.getFile() + ": "
								+ instantiatedFormulaString);
						if (isLoggingTag("kb9"))
							println("kb9",
									"KB (" + kBase.getAgentName() + "): " + kBase.toStringFacts());
					} else {
						println("kb", "Not Asserting " + m.getParameter(ML.PERFORMATIVE)
								+ " from " + sender.getFile() + ": "
								+ instantiatedFormulaString);
					}
					ModalLogicFormula bFormula = new BelieveNode();
					bFormula.as_agent(SL.term(sender.toStringAgentIdentifier(false)));
					bFormula.as_formula(instantiatedFormula);
					kBase.assertFormula(bFormula);
					println("kb", "Asserting from " + sender.getFile() + ": " + bFormula);
					if (isLoggingTag("kb9"))
						println("kb9",
								"KB (" + kBase.getAgentName() + "): " + kBase.toStringFacts());
					// String res = queryRef_toString(item);
					// result.put(ML.LANGUAGE, ML.FIPA_SL);
					// result.put(ML.CONTENT, CASAUtil.toQuotedString(res));
				}
			} catch (Exception e) {
				result = new PerformDescriptor(-1, println(
						"error",
						"TransientAgent.accept_query_ref() failed on content '"
								+ m.getParameter(ML.CONTENT) + "'", e));
				result.put(ML.LANGUAGE, "text");
			}
		} else {
			Act act = m.getAct();
			String item = act.get(act.size() - 1);
			Object content = perform_query_ref(item);
			if (content != null) {
				result.put(ML.LANGUAGE, "casa.*");
				result.put(ML.CONTENT, CASAUtil.serialize(content));
			} else {
				result.put(ML.PERFORMATIVE, ML.FAILURE);
				result.put(ML.CONTENT, "Don't know item '" + item + "'.");
				result.put(ML.LANGUAGE, "text");
				result.setStatus(1, "Don't know item '" + item + "'.");
			}
		}
		// println("query for "+item+" returned '"+result+"'.");
		return result;
	}

	/**
	 * Sends a <b>synchronous</b> query ref the <em>url</em> and returns the
	 * reply.
	 * 
	 * @param url
	 *          The URL to send teh query-ref message to.
	 * @param expression
	 *          The expression to query
	 * @param timeout
	 *          The max time (ms) to wait for a reponse.
	 * @return The reply to the query.
	 * @throws Throwable
	 *           if the query reply is not the expected inform-ref.
	 */
	public Collection<Term> queryRef(URLDescriptor url, String expression,
			long timeout) throws Throwable {
		MLMessage message = MLMessage.getNewMLMessage(ML.PERFORMATIVE,
				ML.QUERY_REF, ML.RECEIVER, url.toString(), ML.LANGUAGE, ML.FIPA_SL,
				ML.CONTENT, expression);
		StatusObject<MLMessage> stat = sendRequestAndWait(message, timeout);
		MLMessage reply = stat.getObject();
		if (reply == null)
			return null;
		if (ML.INFORM_REF.equals(reply.getParameter(ML.PERFORMATIVE))
				&& ML.FIPA_SL.equalsIgnoreCase(reply.getParameter(ML.LANGUAGE))) {

			Content cont = (Content) reply.getContent();
			return getTermsFromContent(cont);
		}
		throw new Exception(
				"TransientAgent.queryRef(): expected a INFORM-REF message back with language field FIPA-SL, but got:\n"
						+ reply);
	}

	public static Collection<Term> getTermsFromContent(Content cont) {
		// Content cont = (Content)reply.getContent();
		ListOfContentExpression contentList = cont.as_expressions();
		ContentExpression cexp = contentList.first();
		Node[] children = cexp.children();
		Node[] grandchildren = children[0].children();
		TermSetNode set = (TermSetNode) grandchildren[1];
		return (Collection<Term>) set.as_terms().asACollection();
	}

	public Collection<AID> queryRefAIDs(URLDescriptor url, String expression,
			long timeout) throws Throwable {
		Collection<Term> terms = queryRef(url, expression, timeout);
		Vector<AID> ret = new Vector<AID>();
		for (Term term : terms) {
			AID aid = Tools.term2AID(term);
			String addresses[] = aid.getAddressesArray();
			if (addresses == null || addresses.length == 0) {
				URLDescriptor url2 = AgentLookUpTable.findByName(aid.getName());
				if (url2 != null) {
					aid.addAddresses(url2.toString());
				}
			}
			ret.add(aid);
		}
		return ret;
	}

	public Collection<URLDescriptor> queryRefURLs(URLDescriptor url,
			String expression, long timeout) throws Throwable {
		Collection<AID> aids = queryRefAIDs(url, expression, timeout);
		Vector<URLDescriptor> ret = new Vector<URLDescriptor>();
		for (AID aid : aids) {
			String addresses[] = aid.getAddressesArray();
			if (addresses == null || addresses.length == 0) {
				println("error",
						"TransientAgent.queryRefURLs(): can't find URL for AID " + aid);
				ret.add(URLDescriptor.make(aid.getName()));
			} else {
				ret.add(URLDescriptor.make(addresses[0]));
			}
		}
		return ret;
	}

	protected boolean perform_query_if(String item)
			throws IllegalOperationException {
		if ("registerd".equalsIgnoreCase(item))
			return isRegistered();
		if ("using-proxy".equalsIgnoreCase(item))
			return isUsingProxy();
		throw new IllegalOperationException("TransientAgent.perform_query_if("
				+ item + "): Unknown term.");
	}

	/**
	 * Responds to a query-ref message by interpreting the act field, attempting
	 * to execute it, and returning a message with the result.
	 * 
	 * @param m
	 *          The MLMessage received.
	 * @return a Status object reflecting the success or failure of the operation
	 */
	public PerformDescriptor perform_query_if(MLMessage m) {
		String item;
		PerformDescriptor result = new PerformDescriptor();
		if (ML.FIPA_SL.equalsIgnoreCase(m.getParameter(ML.LANGUAGE))) {
			item = m.getParameter(ML.CONTENT);
			try {
				String res = query_toString(item);
				result.put(ML.LANGUAGE, ML.FIPA_SL);
				result.put(ML.CONTENT, res);// CASAUtil.toQuotedString(res));
			} catch (ParseException e) {
				result = new PerformDescriptor(-1, "Bad parse of expression: '" + item
						+ "'");
				result.put(ML.LANGUAGE, "text");
			}
		} else {
			Act act = m.getAct();
			item = act.get(act.size() - 1);
			boolean res;
			try {
				res = perform_query_if(item);
				result.put(ML.LANGUAGE, "casa.*");
				result.put(ML.CONTENT, CASAUtil.serialize(res));
			} catch (IllegalOperationException e) {
				result.put(ML.PERFORMATIVE, ML.FAILURE);
				result.put(ML.CONTENT, "Don't know item '" + item + "'.");
				result.put(ML.LANGUAGE, "text");
				result.setStatus(1, "Don't know item '" + item + "'.");
			}
		}
		return result;
	}

	/**
	 * Responds to a query-ref message by interpreting the act field, attempting
	 * to execute it, and returning a message with the result.
	 * 
	 * @param m
	 *          The MLMessage received.
	 * @return a Status object reflecting the success or failure of the operation
	 */
	public PerformDescriptor consider_query_if(MLMessage m) {
		PerformDescriptor result = null;
		boolean shortcutting = true;
		String contentString = m.getParameter(ML.CONTENT);
		if (shortcutting) {
			Act act = m.getAct();
			if (ML.FIPA_SL.equalsIgnoreCase(m.getParameter(ML.LANGUAGE))) {
				try {
					String res = query_toString(contentString);
					result = new PerformDescriptor(DROP_ACTION);
					MLMessage msg = MLMessage.constructReplyTo(m, getUniqueRequestID(),
							null);
					msg.setParameter(ML.PERFORMATIVE, ML.INFORM_IF);
					msg.setParameter(ML.ACT, "discharge|perform|TOP"); // msg.removeParameter(ML.ACT);
																														 // //msg.setParameter(ML.ACT,
																														 // act.toString());
					msg.setParameter(ML.LANGUAGE, ML.FIPA_SL);
					msg.setParameter(ML.CONTENT, res);
					sendMessage(msg);
				} catch (ParseException e) {
					String message = "Bad parse of expression: '" + contentString + "'";
					result = new PerformDescriptor(-1, message);
					result.put(ML.CONTENT, println("error", message, e));
					result.put(ML.LANGUAGE, "text");
				}
			} else {
				String actAtom = act.get(act.size() - 1);
				boolean res;
				try {
					res = perform_query_if(actAtom);
					result = new PerformDescriptor(DROP_ACTION);
					MLMessage msg = MLMessage.constructReplyTo(m, getUniqueRequestID(),
							null);
					msg.setParameter(ML.PERFORMATIVE, ML.INFORM_IF);
					msg.setParameter(ML.ACT, actAtom);
					msg.setParameter(ML.LANGUAGE, "casa.*");
					msg.setParameter(ML.CONTENT, CASAUtil.serialize(res));
					sendMessage(msg);
				} catch (IllegalOperationException e) {
					boolean retUnknown = true;
					if (retUnknown) {
						result = new PerformDescriptor(DROP_ACTION);
						MLMessage msg = MLMessage.constructReplyTo(m, getUniqueRequestID(),
								null);
						msg.setParameter(ML.PERFORMATIVE, ML.NOT_UNDERSTOOD);
						msg.setParameter(ML.ACT, m.getParameter(ML.PERFORMATIVE) + "|"
								+ act.toString());
						msg.setParameter(ML.LANGUAGE, "text");
						msg.setParameter(ML.CONTENT, "Don't know item '" + actAtom + "'.");
						sendMessage(msg);
					} else {
						result = new PerformDescriptor(-1, "Don't know item '" + actAtom
								+ "'.");
					}
				}
			}
		}
		return result;
	}

	/**
	 * Retrieves the response to an earlier query-ref message by interpreting the
	 * act field and content field.
	 * 
	 * @param m
	 *          The MLMessage received.
	 * @return a Status object reflecting the success or failure of the operation
	 */
	public PerformDescriptor release_query_if(MLMessage m) {
		PerformDescriptor result = new PerformDescriptor();
		if (ML.FIPA_SL.equalsIgnoreCase(m.getParameter(ML.LANGUAGE))) {
			try {
				Node node = (Node) m.getContent();
				if (node instanceof Formula) {
					Formula formula = (Formula) node;
					URLDescriptor sender = m.getSender();
					if (isTrusted(sender, formula)) {
						kBase.assertFormula(formula);
						println("kb", "Asserting from " + sender.getFile() + " (trusted): "
								+ m.getParameter(ML.CONTENT));
						if (isLoggingTag("kb9"))
							println("kb9",
									"KB (" + kBase.getAgentName() + "): " + kBase.toStringFacts());
					} else {
						println(
								"kb",
								"Not Asserting " + m.getParameter(ML.PERFORMATIVE) + " from "
										+ sender.getFile() + " (not trusted): "
										+ m.getParameter(ML.CONTENT));
					}
					ModalLogicFormula bFormula = new BelieveNode();
					bFormula.as_agent(SL.term(sender.toStringAgentIdentifier(false)));
					bFormula.as_formula(formula);
					kBase.assertFormula(bFormula);
					println("kb", "Asserting from " + sender.getFile() + ": " + bFormula);
					if (isLoggingTag("kb9"))
						println("kb9",
								"KB (" + kBase.getAgentName() + "): " + kBase.toStringFacts());
					// String res = queryRef_toString(item);
					// result.put(ML.LANGUAGE, ML.FIPA_SL);
					// result.put(ML.CONTENT, CASAUtil.toQuotedString(res));
				}
			} catch (Throwable e) {
				result = new PerformDescriptor(-1, println(
						"error",
						"TransientAgent.accept_query_if() failed on content '"
								+ m.getParameter(ML.CONTENT) + "'", e));
				result.put(ML.LANGUAGE, "text");
			}
		} else { // language casa.*
			Act act = m.getAct();
			String item = act.get(act.size() - 1);
			// ??
		}
		// println("query for "+item+" returned '"+result+"'.");
		return result;
	}

	public boolean queryIf(URLDescriptor url, String expression, long timeout)
			throws Throwable {
		MLMessage message = MLMessage.getNewMLMessage(ML.PERFORMATIVE, ML.QUERY_IF,
				ML.RECEIVER, url.toString(), ML.LANGUAGE, ML.FIPA_SL, ML.CONTENT,
				expression, ML.REPLY_WITH, getUniqueRequestID());

		MessageEventDescriptor[] messageDescriptors = null;
		try {
			MLMessage transformedMessage = (MLMessage) transform(message);
			messageDescriptors = new MessageEventDescriptor[] {
					new MessageEventDescriptor(this, ML.EVENT_MESSAGE_RECEIVED,
							ML.PERFORMATIVE, ML.INFORM_IF, ML.ACT, new Act(
									"discharge|perform|TOP"), ML.SENDER, URLDescriptor.make(
									transformedMessage.getParameter(ML.RECEIVER)),
							ML.IN_REPLY_TO, message.getParameter(ML.REPLY_WITH)),
					new MessageEventDescriptor(this, ML.EVENT_MESSAGE_RECEIVED,
							ML.PERFORMATIVE, ML.FAILURE, ML.ACT, new Act("discharge|perform|"
									+ transformedMessage.getParameter(ML.ACT)), ML.SENDER,
							URLDescriptor.make(transformedMessage.getParameter(ML.RECEIVER)),
							ML.IN_REPLY_TO, message.getParameter(ML.REPLY_WITH)),
					new MessageEventDescriptor(this, ML.EVENT_MESSAGE_RECEIVED,
							ML.PERFORMATIVE, ML.REFUSE, ML.ACT, new Act(
									transformedMessage.getParameter(ML.PERFORMATIVE) + "|"
											+ transformedMessage.getParameter(ML.ACT)), ML.SENDER,
							URLDescriptor.make(transformedMessage.getParameter(ML.RECEIVER)),
							ML.IN_REPLY_TO, message.getParameter(ML.REPLY_WITH)),
					new MessageEventDescriptor(this, ML.EVENT_MESSAGE_RECEIVED,
							ML.PERFORMATIVE, ML.NOT_UNDERSTOOD, ML.ACT, new Act(
									transformedMessage.getParameter(ML.PERFORMATIVE) + "|"
											+ transformedMessage.getParameter(ML.ACT)), ML.SENDER,
							URLDescriptor.make(transformedMessage.getParameter(ML.RECEIVER)),
							ML.IN_REPLY_TO, message.getParameter(ML.REPLY_WITH)),
					new MessageEventDescriptor(this, ML.EVENT_MESSAGE_RECEIVED,
							ML.PERFORMATIVE, transformedMessage.getAct().elementAt(0),
							ML.ACT, transformedMessage.getAct().pop(), ML.SENDER,
							URLDescriptor.make(transformedMessage.getParameter(ML.RECEIVER)),
							ML.IN_REPLY_TO, message.getParameter(ML.REPLY_WITH)) };
		} catch (Throwable/* URLDescriptorException */e1) {
			println("error",
					"AbstractProcess.sendRequestAndWait(): Bad URL in RECEIVER field.",
					e1);
			assert false;
		}

		StatusObject<MLMessage> stat = sendRequestAndWait(message, timeout,
				messageDescriptors);
		MLMessage reply = stat.getObject();
		if (reply != null && stat.getStatusValue() >= 0) {
			if (ML.INFORM_IF.equals(reply.getParameter(ML.PERFORMATIVE))
					&& ML.FIPA_SL.equalsIgnoreCase(reply.getParameter(ML.LANGUAGE))) {
				Object obj = reply.getContent();
				if (obj instanceof NotNode)
					return false;
				else
					return true;
			}
		}
		throw new Exception(
				"TransientAgent.queryIf(): expected a INFORM-IF message back with language field FIPA-SL, but got:\n"
						+ reply);
	}

	// proxy request
	// (reactive)////////////////////////////////////////////////////
	/**
	 * Responds to a received proxy request message by interpreting the content
	 * field, attempting to execute it, and returning a message with the result.
	 * 
	 * @param m
	 *          The MLMessage received.
	 * @return a Status object reflecting the success or failure of the operation
	 */
	protected PerformDescriptor perform_method_call(MLMessage m) {
		in("TransientAgent.perform_proxy");
		String content = m.getParameter("content");
		int pos = CASAUtil.scanFor(content, 0, "(");
		String methodString = content.substring(0, pos).trim();
		Object ret = null;
		PerformDescriptor pd = new PerformDescriptor();

		// MLMessage reply = MLMessage.constructReplyTo (m, getUniqueRequestID (),
		// getURL ());
		try {
			Object[] params = CASAUtil.unserializeArray(content, pos + 1, null);
			Class<?>[] paramTypes = CASAUtil.objectsToClasses(params);
			Class<?> cls = this.getClass();
			Method method = cls.getMethod(methodString, paramTypes);
			ret = method.invoke(this, params);
			content = CASAUtil.serialize(ret);
			pd.put(ML.LANGUAGE, "casa." + ret.getClass().getName());
			pd.put(ML.CONTENT, content);
		} catch (Exception e) {
			String warning = "Could not interpret and invoke content of message";
			if (isLoggingTag("warning"))
				println("warning", "TransientAgent.perform_proxy: " + warning + ":\n"
						+ m.toString(true), e);
			pd.put(ML.PERFORM, ML.FAILURE);
			pd.put(ML.LANGUAGE, "casa.Status");
			pd.put(ML.CONTENT,
					CASAUtil.serialize(new Status(-59, warning + ": " + e.toString())));
		}
		out("TransientAgent.perform_proxy");
		return pd;
	}

	@Override
	public Status doRegisterAgentInstance(int lacPort) {
		RunnableWithParameter<MLMessage> success = new RunnableWithParameter<MLMessage>() {
			@Override
			public void run(MLMessage msg) {
				Object o = null;
				try {
					o = CASAUtil.unserialize(msg.getParameter(ML.CONTENT), StatusURLandFile.class.getCanonicalName());
				} catch (ParseException ex) {
					out("TransientAgent.release_register_instance returned with exception");
					String expl = "TransientAgent.release_register_instance: exception unserializing content field:\n  "
							+ msg.getParameter(ML.CONTENT);
					Trace.log("error", expl, ex);
					println("error", expl, ex);
					callInitializeAfterRegistered(false);
					return;
				}

				if (o instanceof StatusURLandFile) {
					StatusURLandFile status = (StatusURLandFile) o;
					if (status.getStatusValue() != 0) {
						if (isLoggingTag("warning"))
							println("warning",
									"TransientAgent.release_register_instance: got bad status in message:\n"
											+ msg.toString(true));
						out("TransientAgent.release_register_instance");
					}
					setURL(status.getURL());
//					setFile(status.getFile());
					try {
						setRegisteredWithLAC(msg.getURLParameter(ML.SENDER));
					} catch (Throwable e) {
						println(
								"error",
								"TransientAgent.release_register_instance(): Unexpected exception parsing sender from :SENDER field: "
										+ msg.getParameter(ML.SENDER), e);
					}
					callInitializeAfterRegistered(true);
					initUI();
					if (usingProxy) {
						if (proxy == null) {
							println("warning", "TransientAgent.release_register_instance: "
									+ "UNEXPECTED CONDITION: usingProxy==true && proxy==null");
						} else
							proxy.setLACPort(getURL().getLACport()); // ... and the proxy
					}
					notifyObservers(ML.EVENT_REGISTER_INSTANCE, null);
				} else {
					out("TransientAgent.release_register_instance returned with exception");
					String expl = "TransientAgent.release_register_instance: expected StatusURLandFile object in content, but got:\n"
							+ msg.getParameter(ML.CONTENT);
					Trace.log("error", expl);
					println("error", expl);
					callInitializeAfterRegistered(false);
					return;
				}

			}
		};

		RunnableWithParameter<MLMessage> failure = new RunnableWithParameter<MLMessage>() {
			@Override
			public void run(MLMessage msg) {
				if (msg == null) {
					println(
							"error",
							"TransientAgent failed to register with LAC: request timed out or failed (null return)");
				} else {
					println("error",
							"TransientAgent failed to register with LAC: return message:\n"
									+ msg.toString(true));
				}
				callInitializeAfterRegistered(false);
			}
		};

		return doRegisterAgentInstance(lacPort, success, failure);

	}

	// ///////////////////////////////////////////////////////////////////////////
	// LAC communication/status /////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////

	// RegisterAgentIntance (proactive)///////////////////////////////////////////
	/**
	 * Registers this Agent with the LAC. That is, tells the LAC that this Agent
	 * is up and running. It sends the LAC (whose port is the parameter
	 * <em>port</em> a message of the format:
	 * <table border="1" bgcolor="gold" * cellpadding="3">
	 * <tr>
	 * <td><a href="doc-files/performatives.gif">performative</a></td>
	 * <td>request</td>
	 * </tr>
	 * <tr>
	 * <td>{@link ML#ACT act}</td>
	 * <td>register.instance</td>
	 * </tr>
	 * <tr>
	 * <td>language</td>
	 * <td><a href="doc-files/contentLanguages.html#casa.agentCom.URLDescriptor">casa.
	 * URLDescriptor </a></td>
	 * </tr>
	 * <tr>
	 * <td>content</td>
	 * <td><em>a {@link URLDescriptor} object for this
	 *              agent followed by either </em> true <em> or </em> false
	 * <em> indicating
	 *              whether this agent is in the local process of the LAC or not</em>
	 * </td>
	 * </tr>
	 * </table>
	 * Note that his method <b>only </b> sends the message; it relies on
	 * {@link #release_register_instance(MLMessage)}to do the actual processing of
	 * the returned reply.
	 * 
	 * @param lacPort
	 *          URLDescriptor for the LAC to register with. Since an agent can
	 *          only register with a LAC on the same host, the LAC address is
	 *          calculated using <em>port</em> and this agent's URL.
	 * @return a Status object describing success or failure of sending the
	 *         <em>request</em> message (see
	 *         {@link #sendMessage(String, String, URLDescriptor, String[])
	 *         sendMessage()}) or a Status object of value 5 if the agent is
	 *         already registered.
	 */
	@Override
	public Status doRegisterAgentInstance(int lacPort,
			final RunnableWithParameter<MLMessage> successAction,
			final RunnableWithParameter<MLMessage> failureAction) {
		in("TransientAgent.doRegisterAgentInstance");
		final long timeout = 10000;

		// check to see that we are not already registered
		if (getURL().getLACport() != 0) {
			String err = "TransientAgent.doRegisterAgentInstance: Already registered";
			println("warning", err);
			out("TransientAgent.doRegisterAgentInstance");
			return new Status(5, err);
		}

		// construct the LAC's URL from our URL
		if (lacPort<=0)
		  lacPort = getURL().getLACport();
		if (lacPort==0) 
			lacPort = LAC.ProcessInfo.lacPort;
		URLDescriptor lacURL;
		try {
			lacURL = URLDescriptor.make(getURL().getHost(), lacPort);
		} catch (URLDescriptorException e) {
			return new Status(-3, println("error", "TransientAgent.doRegisterAgentInstance()", e));
		}
//		URLDescriptor lacURL = getURL();
//		lacURL.setPort(lacPort <= 0 ? LAC.ProcessInfo.lacPort/* 9000 */: lacPort);
//		lacURL.setPath("");

		final MLMessage msg = getNewMessage(ML.REQUEST, ML.REGISTER_INSTANCE,
				lacURL, ML.PRIORITY, "10", ML.REPLY_BY, Long.toString(timeout));
		msg.setContent(new Object[] { getURL(),
				new Boolean(LAC.ProcessInfo.process != null) },
				MLMessage.Languages.CASA);

		// Status ret;

		// ret = sendMessage(msg);
		// notifyObservers(ML.EVENT_REGISTER_INSTANCE, msg);

		// start a thread to wait on the reply
		makeSubthread(new Runnable() {
			@Override
			public void run() {
				StatusObject<MLMessage> stat = sendRequestAndWait(msg, timeout);
				if (stat != null && stat.getStatusValue() == 0) {
					if (successAction != null) {
						successAction.setParam(stat.getObject());
						successAction.run();
					}
					notifyObservers(ML.EVENT_REGISTER_INSTANCE, msg);
				} else {
					if (failureAction != null) {
						failureAction.setParam(stat.getObject());
						failureAction.run();
					}
					// notifyObservers(ML.EVENT_?, msg);
				}
			}
		}).start();

		out("TransientAgent.doRegisterAgentInstance");
		// return ret;
		return new Status(
				0,
				"One of the actions will be executed when the request completes (if it's not null).");

	}

	/**
	 */
	private URLDescriptor registeredWithLAC = null;

	/**
	 * @param r The URL to set {@link #registeredWithLAC} to (null means "not registered").
	 */
	private void setRegisteredWithLAC(URLDescriptor r) {
		registeredWithLAC = r;
	}

	/**
	 * @return Returns true if the agent has finished registering with the LAC.
	 */
	public boolean isRegisteredWithLAC() {
		return registeredWithLAC != null;
	}

	// public PerformDescriptor release_register_instance(MLMessage msg) {
	// in ("TransientAgent.release_register_instance");
	// PerformDescriptor ret = null;
	// Object o = null;
	// try {
	// o = CASAUtil.unserialize(msg.getParameter(ML.CONTENT));
	// }
	// catch (ParseException ex) {
	// out ("TransientAgent.release_register_instance returned with exception");
	// String expl =
	// "TransientAgent.release_register_instance: exception unserializing content field:\n  "+msg.getParameter(ML.CONTENT);
	// ret = new PerformDescriptor(new Status(-56,expl,ex));
	// DEBUG.DISPLAY_ERROR(expl, getAgentName(), ex);
	// println("error",expl,ex);
	// return ret;
	// }
	//
	// if (o instanceof StatusURLandFile) {
	// StatusURLandFile status = (StatusURLandFile)o;
	// if (status.getStatusValue () != 0) {
	// if (isLoggingTag("warning")) println (
	// "warning",
	// "TransientAgent.release_register_instance: got bad status in message:\n"
	// + msg.toString (true));
	// out ("TransientAgent.release_register_instance");
	// ret = new PerformDescriptor(status);
	// }
	// setURL (status.getURL ());
	// setFile (status.getFile ());
	// try {
	// setRegisteredWithLAC(msg.getURLParameter(ML.SENDER));
	// } catch (Throwable e) {
	// println("error",
	// "TransientAgent.release_register_instance(): Unexpected exception parsing sender from :SENDER field: "+msg.getParameter(ML.SENDER),
	// e);
	// }
	// initializeAfterRegistered (true);
	// initUI ();
	// if (usingProxy) {
	// if (proxy == null) {
	// println ("warning",
	// "TransientAgent.release_register_instance: "
	// + "UNEXPECTED CONDITION: usingProxy==true && proxy==null");
	// } else
	// proxy.setLACPort (getURL ().getLACport ()); //... and the proxy
	// }
	// notifyObservers (ML.EVENT_REGISTER_INSTANCE, null);
	// }
	// else {
	// out ("TransientAgent.release_register_instance returned with exception");
	// String expl =
	// "TransientAgent.release_register_instance: expected StatusURLandFile object in content, but got:\n"+msg.getParameter(ML.CONTENT);
	// ret = new PerformDescriptor(new Status(-59,expl));
	// DEBUG.DISPLAY_ERROR(expl, getAgentName());
	// println("error",expl);
	// return ret;
	// }
	// out ("TransientAgent.release_register_instance");
	// return ret==null?new PerformDescriptor(new Status (0,
	// "registered with LAC " + msg.getFromString ()
	// + " as URL " + getURL ().toString ())):ret;
	// }

//	/**
//	 * Template method to handle the <em>file</em> part of the return from the
//	 * register.instance message returned from the LAC. The LAC will return a
//	 * status, a URL, and a filename, but this is not a persistent class, so it
//	 * has no use for the filename, and this method merely dicards it.
//	 * 
//	 * @param fileName
//	 */
//	public void setFile(String fileName) {
//	}

	// RegisterAgentType (proactive) /////////////////////////////////////////////
	// /**
	// * Registers this Agent with the LAC. That is, it tells the LAC of it's
	// * existence, it's description, and how to run it if requested by some
	// outside
	// * agent.
	// *
	// * @param LACport the port number of the LAC to register with
	// * @return a Status object describing success or failure
	// */
	// public Status doRegisterAgentType (int LACport) {
	// in ("TransientAgent.doRegisterAgentType");
	// RunDescriptor rd = new RunDescriptor ();
	// rd.setInternal (getClass ().toString ().substring (6).replace ('/', '.')
	// + " 0:int" + " %name%:java.lang.String" + " %lacPort%:int");
	// URLDescriptor lacURL = getURL ();
	// lacURL.setPort (LACport <= 0 ? 9000 : LACport);
	// lacURL.setPath ("");
	// Status stat = doRegisterAgentType (lacURL, getURL ().getDirectory (), rd);
	// out ("TransientAgent.doRegisterAgentType");
	// return stat;
	// }
	//
	// public Status doRegisterAgentType (URLDescriptor lacURL, String typePath,
	// RunDescriptor rd) {
	// in ("TransientAgent.doRegisterAgentType");
	// MLMessage message = getNewMessage (ML.REQUEST, ML.REGISTER_AGENTTYPE,
	// lacURL);
	//
	// message.setParameter (ML.LANGUAGE, "CASA.RunDescriptor");
	// message.setParameter (ML.CONTENT, rd.toString () + " " + typePath);
	//
	// Status stat = sendMessage (message);
	// out ("TransientAgent.doRegisterAgentType");
	// return stat;
	// }
	//
	// public PerformDescriptor release_register_agentType (MLMessage msg) {
	// in ("TransientAgent.release_register_agentType");
	// String content = msg.getParameter (ML.CONTENT);
	// Status status = new Status ();
	// PerformDescriptor pd = null;
	// try {
	// content = msg.getParameter (ML.CONTENT);
	// Object c = CASAUtil.unserialize(content);
	// status = (c instanceof Status)?(Status)c:null;
	// status.fromString (new TokenParser (content));
	// if (status==null || status.getStatus () < 0) {
	// if (isLoggingTag("warning")) println ("warning",
	// "TransientAgent.release_register_agentType: got bad status in message:\n"
	// + msg.toString (true));
	// pd = new PerformDescriptor(status==null?new
	// Status(-57,"unexpected content type"):status);
	// pd.put(ML.PERFORM, ML.REJECT_PROPOSAL); //being obstenent
	// pd.put(ML.LANGUAGE, "text");
	// pd.put(ML.CONTENT, CASAUtil.serialize(pd.getStatus()));
	// }
	// else {
	// //notifyObservers (new casa.State (ObservableEvent.STATE_REGISTER_TYPE));
	// notifyObservers (ML.EVENT_REGISTER_TYPE, null);
	// }
	// } catch (Exception ex) {
	// String warning =
	// "TransientAgent.release_register_agentType: mangled or missing content in messasge";
	// println ("warning", warning+": \n"+ msg.toString (true));
	// pd = new PerformDescriptor(new Status(-58,warning));
	// pd.put(ML.PERFORM, ML.REJECT_PROPOSAL); //being obstenent
	// pd.put(ML.LANGUAGE, "casa.Status");
	// pd.put(ML.CONTENT, CASAUtil.serialize(pd.getStatus()));
	// return pd;
	// }
	// out ("TransientAgent.release_register_agentType");
	// return pd;
	// }

	// UnregisterAgentInstance (proactive)////////////////////////////////////////
	/**
	 * Unregisters this Agent from the LAC. That is, tells the LAC that this Agent
	 * is no longer running.
	 * 
	 * @param request
	 *          <code>true</code> if the message will be a request;
	 *          <code>false</code> if it is to be an inform.
	 * @return a Status object describing success or failure
	 */
	@Override
	public Status doUnregisterAgentInstance(boolean request) {
		in("TransientAgent.doUnregisterAgentInstance");
		int lacPort = getURL().getLACport();
		if (lacPort==0)
			lacPort = LAC.ProcessInfo.lacPort;
		URLDescriptor lacURL;
		try {
			lacURL = URLDescriptor.make(getURL().getHost(),lacPort);
		} catch (URLDescriptorException e) {
			return new Status(-3, println("error", "TransientAgent.doUnregisterAgentInstance()", e));
		}
//		lacURL.setPort(getURL().getLACport());
//		lacURL.setPath("");

		MLMessage message = getNewMessage(ML.REQUEST, ML.UNREGISTER_INSTANCE,
				lacURL);

		message.setParameter(ML.LANGUAGE, URLDescriptor.class.getName());
		message.setParameter(ML.CONTENT, getURL().toString(lacURL));
		getURL().setLACport(0);
		registeredWithLAC = null;
		setBanner(null);
		if (request) {
			out("TransientAgent.doUnregisterAgentInstance");
			return sendMessage(message);
		} else {
			message.setParameter(ML.PERFORMATIVE, ML.INFORM);
			out("TransientAgent.doUnregisterAgentInstance");
			return sendMessage(message);
		}
	}

	/**
	 * If this messsage is from this agent's LAC (valid :sender field, I'm registered, {@link #getLACURL()}.equals :sender field),
	 * then unregister with the LAC by:
	 * <ul>
	 * <li> setting {@link #setRegisteredWithLAC(URLDescriptor) setRegisteredWithLAC(false)}
	 * <li> setting this agent's URL's LACport to 0
	 * <li> setting the proxy's LACPort to 0
	 * <li> notifying observers {@link ML#EVENT_UNREGISTER_INSTANCE}
	 * <li> Setting {@link #setBanner(String) setBanner(null)}
	 * </ul>
	 * @param msg The incoming message
	 * @return A 0-status PerformDescriptor, or if the validity check fail, null. 
	 */
	public PerformDescriptor release_unregister_instance(MLMessage msg) {
		URLDescriptor sender;
	
		//Sanity checks
		try { // Valid sender?
			sender = msg.getSender();
		} catch (URLDescriptorException e) {
			println("warning", "Recieved inform/exit message with an invalid :sender: "+msg.getParameter(ML.SENDER));
			return null; //not our LAC, so return without doing anything.
		}
		URLDescriptor lacURL = getLACURL();
		if (lacURL==null) { // Are we registered?
			println("warning", "Recieved inform/exit message from agent "+sender+", but I'm not registered with any LAC");
			return null; //not our LAC, so return without doing anything.
		}
		if (!lacURL.equals(sender)) { // Is the sender my LAC?
			println("warning", "Recieved inform/exit message from not-my-LAC: "+sender);
			return null; //not our LAC, so return without doing anything.
		}
		
		//Actual action
		setRegisteredWithLAC(null); // mark as not registered anymore
		getURL().setLACport(0); // we've unregistered with the lac, so clear the
		// LAC port from the URL
		if (usingProxy)
			proxy.setLACPort(getURL().getLACport()); // ... and the proxy
		notifyObservers(ML.EVENT_UNREGISTER_INSTANCE, null);
		setBanner(null);
		return new PerformDescriptor(new Status(0, "unregistered from LAC "
				+ msg.getFromString() + ". Updated URL is " + getURL().toString()));
	}

	// UnregisterAgentType (proactive)
	// ////////////////////////////////////////////
	/**
	 * Unregisters this Agent with the LAC. That is, it tells the LAC to erase all
	 * memory of its existence, it's description, and how to run it.
	 * 
	 * @param LACport
	 *          the port number of the LAC to unregister with
	 * @return a Status object describing success or failure
	 */
	@Override
	public Status doUnregisterAgentType(int LACport) {
		in("TransientAgent.doUnregisterAgentType");
		int lacPort = getURL().getLACport();
		if (lacPort==0)
			lacPort = LAC.ProcessInfo.lacPort;
		URLDescriptor lacURL;
		try {
			lacURL = URLDescriptor.make(getURL().getHost(),lacPort);
		} catch (URLDescriptorException e) {
			return new Status(-3, println("error", "TransientAgent.doUnregisterAgentType()", e));
		}
//		URLDescriptor lacURL = getURL();
//		lacURL.setPort(LACport <= 0 ? 9000 : LACport);
//		lacURL.setPath("");

		MLMessage message = getNewMessage(ML.REQUEST, ML.UNREGISTER_AGENTTYPE,
				lacURL);
		message.setParameter(ML.LANGUAGE, "CASA.path");
		message.setParameter(ML.CONTENT, getClass().toString().substring(6)
				.replace('.', '/'));

		Status stat = sendMessage(message);
		out("TransientAgent.doUnregisterAgentType");
		return stat;
	}

	public PerformDescriptor release_unregister_agentType(MLMessage msg) {
		in("TransientAgent.release_unregister_agentType");
		String content = msg.getParameter(ML.CONTENT);
		PerformDescriptor pd = null;
		if (isA(msg.getParameter(ML.PERFORMATIVE), ML.AFFIRMATIVE_REPLY)) {
			try {
				content = msg.getParameter(ML.CONTENT);
				Status status = new Status(content);
				if (status.getStatusValue() < 0) {
					String warning = "TransientAgent.release_unregister_agentType: got bad status ("
							+ status.toString() + ") in message";
					if (isLoggingTag("warning"))
						println("warning", warning + ":\n" + msg.toString(true));
					pd = new PerformDescriptor(new Status(-44, warning));
					pd.put(ML.LANGUAGE, "casa.Status");
					pd.put(ML.CONTENT, CASAUtil.serialize(pd.getStatus()));
				} else {
					// notifyObservers (new casa.State
					// (ObservableEvent.STATE_REGISTER_TYPE));
					notifyObservers(ML.EVENT_REGISTER_TYPE, null);
				}
			} catch (Exception ex) {
				String warning = "TransientAgent.handleReply_unregisterAgentType: mangled or missing content in messasge";
				if (isLoggingTag("warning"))
					println("warning", warning + ": \n" + msg.toString(true), ex);
				out("TransientAgent.handleReply_unregisterAgentType");
				pd = new PerformDescriptor(new Status(-12, warning));
				pd.put(ML.LANGUAGE, "casa.Status");
				pd.put(ML.CONTENT, CASAUtil.serialize(pd.getStatus()));
			}
		}
		out("TransientAgent.release_unregister_agentType");
		return pd;
	}

	// //ResolveURL (proactive)
	// ////////////////////////////////////////////////////
	//
	// /**
	// * Sends a the request to the appropriate LAC to resolve the URL.
	// *
	// * @param url The URL to be resolved. Note that this URLDescriptor object
	// will
	// * be updated to reflect the resolution.
	// * @param timeout milliseconds to wait before returning if something goes
	// * wrong
	// * @param cmd A {@link Command} object to execute when the LAC returns with
	// an
	// * successful or failed resolution
	// * @return Status(0) if url is successfully updated, Status(-ve) if not
	// */
	// public Status doResolveURL(URLDescriptor url, long timeout, /*Command*/
	// Runnable1<String,Status> cmd) {
	// in ("TransientAgent.doResolveURL");
	//
	// Status ret;
	//
	// // If there is no lac given, assume it's at port 9000
	// if (!url.hasLACport()) {
	// url.setDataValue("lac","9000");
	// }
	//
	// if (url.isResolvable ()) {
	// URLDescriptor lac = URLDescriptor.make (url);
	// lac.setPort (url.getLACport ());
	// lac.setPath ("");
	// MLMessage req = getNewMessage (ML.REQUEST, ML.RESOLVE_URL, lac);
	// req.setParameter (ML.SENDER, getURL ().toString (lac));
	// req.setParameter (ML.LANGUAGE, URLDescriptor.class.getName ());
	// req.setParameter (ML.CONTENT, url.toString (lac));
	// req.setParameter (ML.REPLY_BY,
	// Long.toString (System.currentTimeMillis() + timeout));
	// req.setParameter (ML.REPLY_WITH, getUniqueRequestID ());
	//
	// //deferedExecs.put(req.getParameter(ML.REPLY_WITH),cmd);
	//
	// //ret = sendMessage(req);
	// StatusObject<MLMessage> stat = sendRequestAndWait(req, 5000);
	// ret = stat;
	// MLMessage msg = stat.getObject();
	// if (msg!=null) {
	// // A success message
	// if (isA(msg.getParameter(ML.PERFORMATIVE), ML.PROPOSE)) {
	// cmd.run(msg.getParameter(ML.CONTENT));
	// }
	// // A failure of some sort
	// else {
	// ret = new Status(-2, println("warning",
	// "TransientAgent.doResolveURL(): failed to resolve url "+url+"; received message: "+msg.toString(true)));
	// }
	// }
	//
	// }
	// else
	// ret = new Status(-1,"Unable to resolve URL "+url.toString());
	// out ("TransientAgent.doResolveURL");
	// return ret;
	// }
	//
	// /**
	// * This is a synchronous action that spawns a new subagent to actually send
	// * the request to the appropriate LAC to resolve the URL.
	// *
	// * @param url The URL to be resolved. Note that this URLDescriptor object
	// will
	// * be updated to reflect the resolution.
	// * @param timeout milliseconds to wait before returning if something goes
	// * wrong
	// * @return Status(0) if url is successfully updated, Status(-ve) if not
	// * @deprecated
	// */
	// @Deprecated
	// public Status doResolveURL_sync (URLDescriptor url, long timeout) {
	// in ("TransientAgent.doResolveURL_sync");
	//
	// // If there is no lac given, assume it's at port 9000
	// if (!url.hasLACport()) {
	// url.setDataValue("lac","9000");
	// }
	//
	// if (url.isResolvable ()) {
	// URLDescriptor lac = URLDescriptor.make (url);
	// lac.setPort (url.getLACport ());
	// lac.setPath ("");
	// MLMessage req = getNewMessage (ML.REQUEST, ML.RESOLVE_URL, lac);
	// req.setParameter (ML.SENDER, getURL ().toString (lac));
	// req.setParameter (ML.LANGUAGE, URLDescriptor.class.getName ());
	// req.setParameter (ML.CONTENT, url.toString (lac));
	// req.setParameter (ML.REPLY_BY,
	// Long.toString (System.currentTimeMillis() + timeout));
	// req.setParameter (ML.REPLY_WITH, getUniqueRequestID ());
	// MLMessage reply = null;
	//
	// //reply = sendMessage_sync(req);
	// StatusObject<MLMessage> stat = sendRequestAndWait(req, 3000);
	// reply = stat.getObject();
	// /**
	// * @todo What can we check for in the reply to see if there has been an
	// * error?
	// */
	// if (isA(reply.getParameter (ML.PERFORMATIVE),ML.SUCCESS)) {
	// try {
	// URLDescriptor newUrl = URLDescriptor.fromString (new TokenParser (
	// reply.getParameter (ML.CONTENT)));
	// url.copy (newUrl);
	// out ("TransientAgent.doResolveURL_sync");
	// return new Status (0);
	// } catch (URLDescriptorException ex2) {
	// out ("TransientAgent.doResolveURL_sync");
	// return new Status (
	// -4,
	// "TransientAgent.doResolveURL_sync: resolve-url request returned mangled content");
	// }
	// }
	// else {
	// println("error","TransientAgent.doResolveURL_sync: got unexpected reply from LAC:\n"+reply.toString(true));
	// }
	// }
	// out ("TransientAgent.doResolveURL_sync");
	// return new Status (-1,
	// "TransientAgent.doResolveURL_sync: URL is unresolvable");
	// }

	/**
	 */
	private StatusURLDescriptorList instancesFound = null;

	/**
	 * Used by consider_*(), verify_*(), perform_*(), release_*(), conclude_*(),
	 * etc-type methods to signal that the action is not yet ready to be executed,
	 * and that the method should be called again later.
	 */
	static final public int DEFER_ACTION = 8765;

	/**
	 * Used by consider_*(), verify_*(), perform_*(), release_*(), conclude_*(),
	 * etc-type methods to signal that the action should be discarded -- ie: no
	 * action should be taken and the message should be allowed by timeout when
	 * the REPLY-BY expires.
	 */
	static final public int DROP_ACTION = 8766;

	@Override
	public StatusURLDescriptorList getInstancesFound() {
		return this.instancesFound;
	}

	@Override
	public Status requestInstances(String pattern) {
		int lacPort = getURL().getLACport();
		if (lacPort==0)
			lacPort = LAC.ProcessInfo.lacPort;
		URLDescriptor lacURL;
		try {
			lacURL = URLDescriptor.make(getURL().getHost(),lacPort);
		} catch (URLDescriptorException e) {
			return new Status(-3, println("error", "TransientAgent.requestInstances()", e));
		}
//		URLDescriptor lacURL = getURL();
//		lacURL.setPort(getURL().getLACport());
//		lacURL.setPath("");

		String m[] = { ML.LANGUAGE, java.util.regex.Pattern.class.getName(),
				ML.CONTENT, pattern };

		return sendMessage(ML.REQUEST, ML.FIND_INSTANCE, lacURL, m);
	}

	protected Status setInstancesFound(MLMessage message) {
		String language = message.getParameter(ML.LANGUAGE);
		String content = message.getParameter(ML.CONTENT);
		StatusURLDescriptorList status;

		if (language.equals(StatusURLDescriptorList.class.getName())) {
			status = new StatusURLDescriptorList();
			try {
				status.fromString(new TokenParser(content));
			} catch (Exception ex) {
				out("TransientAgent.getInstancesFound");
				status = new StatusURLDescriptorList(-3,
						"TransientAgent.setInstancesFound: content could not be parsed",
						null);
				return status;
			}
		} else {
			out("TransientAgent.getInstancesFound");
			status = new StatusURLDescriptorList(
					-3,
					"TransientAgent.getInstancesFound: language does not equal 'StatusURLDescriptorList.class.getName ()'",
					null);
			return status;
		}

		this.instancesFound = status;

		return status;
	}

	/**
	 * Attempts to retrieve the list of registered agents that have a path
	 * matching the given regex.
	 * 
	 * @param pattern
	 *          The {@link java.util.regex.Pattern} of the agents to be found.
	 * @return A Vector of agent's URLDescriptors
	 */
	@Override
	public StatusURLDescriptorList doFindInstances_sync(String pattern) {
		in("TransientAgent.doFindInstances_sync");
		URLDescriptor lacURL;
		try {
			lacURL = URLDescriptor.make(getURL().getHost(), getURL().getLACport());
		} catch (URLDescriptorException e) {
			return new StatusURLDescriptorList(-3, println("error", "TransientAgent.doFindIntsances_sync()", e), null);
		}
//		URLDescriptor lacURL = getURL();
//		lacURL.setPort(getURL().getLACport());
//		lacURL.setPath("");

		MLMessage message = getNewMessage(ML.REQUEST, ML.FIND_INSTANCE, lacURL);

		message.setParameter(ML.LANGUAGE, java.util.regex.Pattern.class.getName());
		message.setParameter(ML.CONTENT, pattern);

		MLMessage reply;
		// reply = sendRequest_sync (message);
		StatusObject<MLMessage> stat = sendRequestAndWait(message, 3000);
		reply = stat.getObject();

		if (reply == null) {
			String m = "TransientAgent.handleReply_unregisterAgent: unexpectedly recieved null message";
			println("warning", m);
			out("TransientAgent.doFindInstances_sync");
			return new StatusURLDescriptorList(-1, m, null);
		}
		String content = reply.getParameter(ML.CONTENT);
		String language = reply.getParameter(ML.LANGUAGE);

		if (isA(reply.getParameter(ML.PERFORMATIVE), ML.REPLY)) {
			StatusURLDescriptorList status;
			if (language.equals(StatusURLDescriptorList.class.getName())) {
				status = new StatusURLDescriptorList();
				try {
					status.fromString(new TokenParser(content));
				} catch (Exception ex) {
					out("TransientAgent.doFindInstances_sync");
					return new StatusURLDescriptorList(
							-3,
							"TransientAgent.doFindInstances_sync: content could not be parsed",
							null);
				}
			} else {
				out("TransientAgent.doFindInstances");
				return new StatusURLDescriptorList(
						-3,
						"TransientAgent.doFindInstances_sync: language does not equal 'StatusURLDescriptorList.class.getName ()'",
						null);
			}

			if (status.getStatusValue() != 0) {
				if (isLoggingTag("warning"))
					println("warning",
							"TransientAgent.doFindInstances_sync: got bad status in message:\n"
									+ reply.toString(true));
			}

			// notifyObservers (new casa.State
			// (ObservableEvent.STATE_FIND_INSTANCES));
			notifyObservers(ML.EVENT_FIND_INSTANCES, null);
			out("TransientAgent.doFindInstances_sync");
			return status;
		} else {
			Status tempStatus = handleErrorReplies(reply,
					"Request to retrieve history from CD");
			out("TransientAgent.doFindInstances_sync");
			return new StatusURLDescriptorList(tempStatus.getStatusValue(),
					tempStatus.getExplanation(), null);
		}
	}

	// ///////////////////////////////////////////////////////////////////////////
	// CDS communication/status /////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////
	/** TODO implement this */
	/**
	 * Retrieves the list of URLDescriptor's from a LAC.
	 * 
	 * @param lac
	 *          The LAC to ask for it's list of CDs
	 * @return A list of CDs
	 */
	@Override
	public Vector<URLDescriptor> doGetCooperationDomains(URLDescriptor lac) {
		/* new */
		println("warning", "Agent.getCoopertationDomains() not implemented.");
		return null;
	}

	// ///////////////////////////////////////////////////////////////////////////
	// CD communication/status //////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////
	/**
	 * Join a cooperation domain. Attempts to join the cooperation domain
	 * corresponding to the {@link URLDescriptor}<em>cdURL</em> by sending a
	 * message of the format:
	 * <table border="1" bgcolor="gold" cellpadding="3">
	 * <tr>
	 * <td><a href="doc-files/performatives.gif">performative</a></td>
	 * <td>request</td>
	 * </tr>
	 * <tr>
	 * <td>{@link ML#ACT act}</td>
	 * <td>join.cd</td>
	 * </tr>
	 * <tr>
	 * <td>language</td>
	 * <td><a href="doc-files/contentLanguages.html#casa.agentCom.URLDescriptor">casa.
	 * URLDescriptor </a></td>
	 * </tr>
	 * <tr>
	 * <td>content</td>
	 * <td><em>a {@link URLDescriptor} object for this agent</em></td>
	 * </tr>
	 * </table>
	 * Note that his method <b>only </b> sends the message; it relies on
	 * {@link #release_join_cd(MLMessage)}to do the actual processing of the
	 * returned reply and do the functionality of registering the join.
	 * 
	 * @param cdURL
	 *          URLDescriptor of the cooperation domain to join
	 * @return a Status object with value 0 for success, 2 for "already joined",
	 *         or describing the failure otherwise.
	 */
	@Override
	public Status doJoinCD(URLDescriptor cdURL) {
		in("TransientAgent.doJoinCD");

		for (URLDescriptor url : joinedCooperationDomains) {
			if (url.equals(cdURL)) {
				out("TransientAgent.doJoinCD");
				return new Status(2, "Agent " + getAgentName()
						+ " is already a member of CD " + cdURL);
			}
		}

		String m[] = { ML.LANGUAGE, URLDescriptor.class.getName(), ML.CONTENT,
				getURL().toString(cdURL) };
		Status ret = sendMessage(ML.REQUEST, ML.JOIN_CD, cdURL, m);
		out("TransientAgent.doJoinCD");
		return ret;
	}

	/**
	 * Handles join-cd replies from a CD. The expected format is:
	 * <table * border="1" bgcolor="orange" cellpadding="3">
	 * <tr>
	 * <td><a href="doc-files/performatives.gif">performative</a></td>
	 * <td>reply</td>
	 * </tr>
	 * <tr>
	 * <td>{@link ML#ACT act}</td>
	 * <td>join.cd</td>
	 * </tr>
	 * <tr>
	 * <td>language</td>
	 * <td><a href="doc-files/contentLanguages.html#casa.Status">casa.Status </a></td>
	 * </tr>
	 * <tr>
	 * <td>content</td>
	 * <td><em>a {@link Status} object</em></td>
	 * </tr>
	 * </table>
	 * or standard error replies. Assuming <em>msg</em> is not an error reply or
	 * is somehow malformed or unrecogized, (in which case this method notifies
	 * observers with STATE_JOIN_CD_FAILED, and returns a negative Status
	 * according to {@link #verifyMessage(MLMessage,String[])}), this method
	 * behaves according to the Status's value in the content field:
	 * <ul>
	 * <li>0: Adds this CD to the list of joined CDs, and notifies observers with
	 * STAT_JOIN_CD, and returns Status(0)
	 * <li>1: Updates this CD to the list of joined CDs, and notifies observers
	 * with STAT_JOIN_CD_REPEATED, and returns Status(0)
	 * <li>[otherwise]: Notifies observers with STATE_JOIN_CD_FAILED, and returns
	 * a Status object indicating the error
	 * </ul>
	 * If the return status is not 0, all errors are logged via
	 * {@link #println(String,String) println()}with the "warning" tag.
	 * 
	 * @param msg
	 *          the incoming reply message
	 * @return the Status object as described above. 0 for success.
	 */
	public PerformDescriptor release_join_cd(MLMessage msg) {
		in("TransientAgent.release_join_cd");

		Status ret;

		// interpret the FROM field
		URLDescriptor from = null;
		try {
			from = msg.getFrom();
		} catch (URLDescriptorException ex) {
			String m2 = "TransientAgent.release_join_cd: Found mangled FROM or SENDER field in message";
			if (isLoggingTag("warning"))
				println("warning", m2 + ":\n" + msg.toString(true));
			ret = new Status(-12, m2);
			// notifyObservers (new casa.State
			// (ObservableEvent.STATE_JOIN_CD_FAILED));
			notifyObservers(ML.EVENT_JOIN_CD_FAILED, null);
			out("TransientAgent.release_join_cd");
			return new PerformDescriptor(ret);
		}

		// check the success status
		if (isA(msg.getParameter(ML.PERFORMATIVE), ML.PROPOSE)) {
			// unmarshal the status from the CONTENT field
			Status status = null;
			try {
				String content = msg.getParameter(ML.CONTENT);
				status = (Status) CASAUtil.unserialize(content, Status.class.getCanonicalName());
			} catch (Exception ex1) {
				status = new Status(
						-1,
						"TransientAgent.release_join_cd: cannot unmarshal Status object from CONTENT field",
						ex1);
			}

			// do the bookkeeping and notify observers appropiately
			if (status.getStatusValue() == 0) {
				joinedCooperationDomains.add(from);
				// notifyObservers (new casa.State (ObservableEvent.STATE_JOIN_CD));
				notifyObservers(ML.EVENT_JOIN_CD, null);
				ret = new Status(0);
			} else if (status.getStatusValue() == 1) {
				// It said we had already joined, we should probably ensure that we
				// don't have it in our list under a similar but different URL.
				joinedCooperationDomains.remove(from);
				joinedCooperationDomains.add(from);
				// notifyObservers (new casa.State
				// (ObservableEvent.STATE_JOIN_CD_REPEATED));
				notifyObservers(ML.EVENT_JOIN_CD_REPEATED, null);
				ret = new Status(0);
			} else {
				if (isLoggingTag("warning"))
					println("warning",
							"TransientAgent.release_join_cd: got bad status in message:\n"
									+ msg.toString(true));
				// notifyObservers (new casa.State
				// (ObservableEvent.STATE_JOIN_CD_FAILED));
				notifyObservers(ML.EVENT_JOIN_CD_FAILED, null);
				ret = status;
			}
			if (ret.getStatusValue() == 0 && from != null) {
				// Ask to be updated when new members are added. Do this first, so that
				// if there is an update in the middle of processing the full list below
				// we will still get it.
				doCDSubscribeMembership(from, true);
				// get member list
				doCDGetMembers(from);
			}
		} else
			ret = null;

		out("TransientAgent.release_join_cd");
		return new PerformDescriptor(ret);
	}

	/**
	 * Withdraw from a cooperation domain. Attempts to withdraw from the
	 * cooperation domain corresponding to the URLdesciptor <em>cd</em>, and
	 * notifies observers if successful.
	 * <p>
	 * If this agent is already in the cooperation domain, observers are notified
	 * of this.
	 * 
	 * @param cdURL
	 *          URLDescriptor of the cooperation domain to withdraw from
	 * @param request
	 *          <code>true</code> if the message will be a request;
	 *          <code>false</code> if it is to be an inform.
	 * @return a Status object describing success or failure
	 */
	@Override
	public Status doWithdrawCD(URLDescriptor cdURL, boolean request) {
		in("TransientAgent.doWithdrawCD");

//		doCDSubscribeMembership(cdURL, false);

		MLMessage message = getNewMessage(request ? ML.REQUEST : ML.INFORM,
				ML.WITHDRAW_CD, cdURL);

		message.setParameter(ML.LANGUAGE, URLDescriptor.class.getName());
		message.setParameter(ML.CONTENT, getURL().toString(cdURL));

		Status ret = sendMessage(message);

		if (!request) {
			joinedCooperationDomains.remove(cdURL);
			cooperationDomainMembers.remove(cdURL);
			notifyObservers(ML.EVENT_WITHDRAW_CD, cdURL);
		}
		out("TransientAgent.doWithdrawCD");
		return ret;
	}

	/**
	 * 
	 * @param msg
	 * @return a PerformDescriptor
	 */

	public PerformDescriptor release_withdraw_cd(MLMessage msg) {
		in("TransientAgent.release_withdraw_cd");
		Status status;
		try {
			// unmarshal the status from the CONTENT field
			status = (Status) CASAUtil.unserialize(msg.getParameter(ML.CONTENT), Status.class.getCanonicalName());

			if (status.getStatusValue() == 0) {
				URLDescriptor cd = msg.getFrom();
				joinedCooperationDomains.remove(cd);
				cooperationDomainMembers.remove(cd);
				// notifyObservers (new casa.State (ObservableEvent.STATE_WITHDRAW_CD));
				notifyObservers(ML.EVENT_WITHDRAW_CD, null);
			} else if (status.getStatusValue() == 1) {
				// It said that we weren't in it's list.
				joinedCooperationDomains.remove(msg.getFrom());
				// notifyObservers (new casa.State (ObservableEvent.STATE_WITHDRAW_CD));
				notifyObservers(ML.EVENT_WITHDRAW_CD, null);
			} else {
				if (isLoggingTag("warning"))
					println("warning",
							"TransientAgent.release_withdraw_cd: got bad status in message:\n"
									+ msg.toString(true));
				// out ("TransientAgent.release_withdraw_cd");
			}
		} catch (Exception ex) {
			String m = "TransientAgent.release_withdraw_cd: mangled or missing CONTENT OR FROM field in messasge: \n"
					+ msg.toString(true);
			println("warning", m, ex);
			status = new Status(-1, m, ex);
			out("TransientAgent.release_withdraw_cd");
		}

		out("TransientAgent.release_withdraw_cd");
		return new PerformDescriptor(status);
	}

	//
	// Get/Put data from a cooperation domain
	//

	/**
	 * Attempts to store data with a cooperation domain. This Agent must be a
	 * member of (successfully joined) the cooperation domain. Sends a message of
	 * format:
	 * <table border="1" bgcolor="gold" cellpadding="3">
	 * <tr>
	 * <td><a href="doc-files/performatives.gif">performative</a></td>
	 * <td>request</td>
	 * </tr>
	 * <tr>
	 * <td>{@link ML#ACT act}</td>
	 * <td>put.data</td>
	 * </tr>
	 * <tr>
	 * <td>language</td>
	 * <td><a
	 * href="doc-files/contentLanguages.html#casa.DataStorageDescriptor">casa
	 * .DataStorageDescriptor </a></td>
	 * </tr>
	 * <tr>
	 * <td>content</td>
	 * <td><em>a {@link DataStorageDescriptor} object</em></td>
	 * </tr>
	 * </table>
	 * 
	 * @param dsd
	 *          The data to store
	 * @param cdURL
	 *          The URLDescriptor of the cooperation domain.
	 * @return the Status of the {@link AbstractProcess#sendMessage(MLMessage)}
	 *         call.
	 */
	@Override
	public Status doCDPutData(URLDescriptor cdURL, DataStorageDescriptor dsd) {
		in("TransientAgent.doCDPutData");
		MLMessage message = getNewMessage(ML.REQUEST, ML.PUT_DATA, cdURL);

		message.setParameter(ML.LANGUAGE, DataStorageDescriptor.class.getName());
		message.setParameter(ML.CONTENT, dsd.toString());

		Status stat = sendMessage(message);
		out("TransientAgent.doCDPutData");
		return stat;
	}

	/**
	 * Retrieves the history list from a cooperation domain. Sends a message of
	 * format:
	 * <table border="1" bgcolor="gold" cellpadding="3">
	 * <tr>
	 * <td><a href="doc-files/performatives.gif">performative</a></td>
	 * <td>request</td>
	 * </tr>
	 * <tr>
	 * <td>{@link ML#ACT act}</td>
	 * <td>get.history</td>
	 * </tr>
	 * </table>
	 * It handles replies of the format:
	 * <table border="1" * bgcolor="orange" cellpadding="3">
	 * <tr>
	 * <td><a href="doc-files/performatives.gif">performative</a></td>
	 * <td>reply</td>
	 * </tr>
	 * <tr>
	 * <td>{@link ML#ACT act}</td>
	 * <td>get.history</td>
	 * </tr>
	 * <tr>
	 * <td>language</td>
	 * <td><a href="doc-files/contentLanguages.html#StatusMLMessageList">casa.
	 * StatusMLMessageList </a></td>
	 * </tr>
	 * <tr>
	 * <td>content</td>
	 * <td><em>a {@link StatusMLMessageList} object</em></td>
	 * </tr>
	 * </table>
	 * or standard error replies.
	 * 
	 * @param cdURL
	 *          The URLDescriptor of the CD from which to retrieve the history
	 *          list
	 * @return A StatusMLMessageList containing a Vector of messages forming the
	 *         history list
	 */
	@Override
	public StatusMLMessageList doCDGetHistory_sync(URLDescriptor cdURL) {
		in("TransientAgent.doCDGetHistory");
		MLMessage message = getNewMessage(ML.REQUEST, ML.GET_HISTORY, cdURL);

		MLMessage reply;
		// reply = sendMessage_sync (message);
		StatusObject<MLMessage> stat = sendRequestAndWait(message, 3000);
		reply = stat.getObject();

		if (reply == null) {
			String m = "TransientAgent.doCDGetHistory: unexpectedly recieved null message";
			println("warning", m);
			out("TransientAgent.doCDGetHistory");
			return new StatusMLMessageList(-1,
					"TransientAgent.doCDGetHistory: unexpectedly recieved null message",
					null);
		}
		String content = reply.getParameter(ML.CONTENT);
		String language = reply.getParameter(ML.LANGUAGE);

		if (isA(reply.getParameter(ML.PERFORMATIVE), ML.REPLY)) {
			StatusMLMessageList status;
			if (language.equals(StatusMLMessageList.class.getName())) {
				status = new StatusMLMessageList();
				try {
					status.fromString(new TokenParser(content));
				} catch (Exception ex) {
					out("TransientAgent.doCDGetHistory");
					return new StatusMLMessageList(-3,
							"TransientAgent.doCDGetHistory: content could not be parsed",
							null);
				}
			} else {
				out("TransientAgent.doCDGetHistory");
				return new StatusMLMessageList(
						-3,
						"TransientAgent.doCDGetHistory: language does not equal 'StatusMLMessageList.class.getName ()'",
						null);
			}

			if (status.getStatusValue() != 0) {
				if (isLoggingTag("warning"))
					println("warning",
							"TransientAgent.doCDGetHistory: got bad status in message:\n"
									+ reply.toString(true));
			}

			// notifyObservers (new casa.State
			// (ObservableEvent.STATE_GET_HISTORY_CD));
			notifyObservers(ML.EVENT_GET_HISTORY_CD, null);
			out("TransientAgent.doCDGetHistory");
			return status;
		} else {
			Status tempStatus = handleErrorReplies(reply,
					"Request to retrieve history from CD");
			out("TransientAgent.doCDGetHistory");
			return new StatusMLMessageList(tempStatus.getStatusValue(),
					tempStatus.getExplanation(), null);
		}
	}

	/**
	 * Sends a message to retrieve the list of participants in a the specified
	 * coopertation domain. Sends a message of format:
	 * <table border="1" * bgcolor="gold" cellpadding="3">
	 * <tr>
	 * <td><a href="doc-files/performatives.gif">performative</a></td>
	 * <td>request</td>
	 * </tr>
	 * <tr>
	 * <td>{@link ML#ACT act}</td>
	 * <td>get.members</td>
	 * </tr>
	 * </table>
	 * The corresponding handler is {@link #release_get_members(MLMessage)}.
	 * 
	 * @param cdURL
	 *          The URLDescriptor of the CD.
	 * @return A Status object from the sendMessage.
	 */
	@Override
	public Status doCDGetMembers(URLDescriptor cdURL) {
		in("TransientAgent.doCDGetMembers");
		MLMessage message = getNewMessage(ML.REQUEST, ML.GET_MEMBERS, cdURL);
		Status stat = sendMessage(message);
		out("TransientAgent.doCDGetMembers");
		return stat;
	}

	/**
	 * Handles get.members replies from a CD. The expected format is:
	 * <table * border="1" bgcolor="orange" cellpadding="3">
	 * <tr>
	 * <td><a href="doc-files/performatives.gif">performative</a></td>
	 * <td>reply</td>
	 * </tr>
	 * <tr>
	 * <td>{@link ML#ACT act}</td>
	 * <td>get.members</td>
	 * </tr>
	 * <tr>
	 * <td>language</td>
	 * <td><a
	 * href="doc-files/contentLanguages.html#casa.StatusURLDescriptorList">casa
	 * .StatusURLDescriptorList </a></td>
	 * </tr>
	 * <tr>
	 * <td>content</td>
	 * <td><em>a {@link StatusURLDescriptorList} object</em></td>
	 * </tr>
	 * </table>
	 * or standard error replies. If the message is successfully interpreted,
	 * calls {@link #cooperationDomainMembers} with the list from the contents.
	 * 
	 * @param msg
	 *          the incoming get.members request message
	 * @return a status describing the success of failure of the operation
	 */
	@SuppressWarnings("unchecked")
	public PerformDescriptor release_get_members(MLMessage msg) {
		in("TransientAgent.release_get_members");
		String content = msg.getParameter(ML.CONTENT);
		// String language = msg.getParameter (ML.LANGUAGE);
		// String performative = msg.getParameter(ML.PERFORMATIVE);

		// if (isA(performative,ML.NACK)) {
		// String m = "TransientAgent.release_get_members: failed: "+content;
		// println("warning",m);
		// out ("TransientAgent.release_get_members");
		// return new PerformDescriptor(new Status(0,m)); //status=0 because we
		// don't want to disconfirm.
		// }
		//

		try {
			Object[] objs = CASAUtil.unserializeArray(content, URLDescriptor.class.getCanonicalName());
			if (objs != null) {
				Vector<URLDescriptor> members = new Vector<URLDescriptor>();
				int i = 0;
				for (Object o : objs) {
					members.add((URLDescriptor) o);
				}
				cooperationDomainMembers.put(msg.getFrom(), members);
			} else {
				cooperationDomainMembers
						.put(msg.getFrom(), new Vector<URLDescriptor>());
			}
			notifyObservers(ML.EVENT_GET_CD_PARTICIPANTS, null);
			return new PerformDescriptor();
		} catch (Throwable e) {
			PerformDescriptor ret = new PerformDescriptor(new Status(-1, println(
					"error", "TransientAgent.release_get_members failed", e)));
			ret.put(ML.LANGUAGE, "text");
			return ret;
		}
	}

	// //************ Client methods for an REQUEST resolve_url conversation
	// **************
	//
	// /**
	// * Process an incoming <em>propose-discharge</em> or <em>cancel</em> message
	// for a <em>resolve_url</em>
	// * act-type request conversation.
	// * Note that the propose-discharge form could be {@link casa.ML#SUCCESS} or
	// {@link casa.ML#FAILURE}. You
	// * should use this method the process the either the client's or server's
	// response.
	// * A client should be the only role getting the propose-discharge form.
	// Either could
	// * recieve a cancel.
	// * This method is required.
	// * @param msg The incoming message
	// * @return The result of the processing; the status part will influence the
	// return
	// * reply-propose-discharge message (-ve will cause a {@link
	// casa.ML#REJECT_PROPOSAL},
	// * otherwise the reply will be {@link casa.ML#ACCEPT_PROPOSAL}), and the
	// key/value
	// * part will be overlayed on the return message.
	// * @author kremer
	// * @since 23-Feb-2007
	// */
	// public PerformDescriptor release_resolve_url(MLMessage msg) {
	// in("TransientAgent.release_resolve_url");
	// PerformDescriptor ret = null;
	// // The other party CANCELS (unexpectedly)
	// if (isA(msg.getParameter(ML.PERFORMATIVE), ML.CANCEL)) {
	//
	// }
	// // A success message
	// else if (isA(msg.getParameter(ML.PERFORMATIVE), ML.AFFIRMATIVE_REPLY)) { //
	// this is an OUTGOING release
	// for (String key: deferedExecs.keySet()) {
	// if (key.equals(msg.getParameter(ML.IN_REPLY_TO))) {
	// deferedExecs.get(key).execute(msg.getParameter(ML.CONTENT),null);
	// }
	// }
	// }
	// // A failure of some sort
	// else {
	//
	// }
	// out("TransientAgent.release_resolve_url");
	// return ret;
	// }
	// TreeMap<String,Command> deferedExecs = new TreeMap<String,Command>();

	/**
	 * Sends a message to retrieve information about the performatives in the
	 * performative type library of another agent. Sends a message of format:
	 * <table border="1" bgcolor="gold" cellpadding="3">
	 * <tr>
	 * <td><a href="doc-files/performatives.gif">performative</a></td>
	 * <td>request</td>
	 * </tr>
	 * <tr>
	 * <td>{@link ML#ACT act}</td>
	 * <td>get.performatives</td>
	 * </tr>
	 * <tr>
	 * <td>language</td>
	 * <td><a href=doc-files/contentLanguages.html#identifier>identifier</a></td>
	 * <td>may be missing if <em>performativeName</em> is null</td>
	 * </tr>
	 * <tr>
	 * <td>content</td>
	 * <td><em>performativeName</em>|<em>null</em></td>
	 * <td>blank or missing requests all performative information</td>
	 * </tr>
	 * </table>
	 * The coorespoinding handler is {@link #release_get_ontology(MLMessage)}.
	 * 
	 * @param url
	 *          The URLDescriptor of an agent to request performative information
	 *          from
	 * @param GET_PERFORMATIVESorGET_ACTS
	 *          either the act name GET_PERFORMATIVES or GET_ACTS
	 * @param typeName
	 *          the name of the performative, or null to request a complete
	 *          description of <em>url</em>'s performatives
	 * @return A Status object from the sendMessage.
	 */
	@Override
	public Status doGetOnology(URLDescriptor url,
			String GET_PERFORMATIVESorGET_ACTS, String typeName) {
		in("TransientAgent.doGetTypes");
		MLMessage message = getNewMessage(ML.REQUEST, ML.GET_ONTOLOGY, url);
		message.setParameter(ML.LANGUAGE, "identifier");
		if (typeName != null) {
			message.setParameter(ML.CONTENT, typeName);
		}
		Status stat = sendMessage(message);
		out("TransientAgent.doGetTypes");
		return stat;
	}

	/**
	 * The code for responding to an outgoing {@link ML#AGREE agree} message.
	 * Subclasses may override this method to change the behavior. In this case,
	 * if the {@link ML#CONTENT content} field of <em>messsage</em> is empty, it
	 * sets the return {@link ML#CONTENT content} field to a description of this
	 * agent's {@link Ontology}, {@link #ontology}. If the {@link ML#CONTENT
	 * content} field of <em>messsage</em> contains a type name that this agent
	 * knows, it sets the return {@link ML#CONTENT content} field to a description
	 * of this agent's concept of that type.
	 * 
	 * @param message
	 *          the message the percipitated this action
	 * @return the description containing a {@link PerformDescriptor} object and
	 *         any changes to the default return message (a null return indicates
	 *         "success" and no changes).
	 */
	protected PerformDescriptor perform_get_ontology(MLMessage message) {
		in("TransientAgent.perform_get_ontology");
		PerformDescriptor pd = new PerformDescriptor();
		Ontology typeHierarchy = ontology;
		String[] m = { ML.PERFORMATIVE, ML.REQUEST, ML.ACT, ML.GET_ONTOLOGY,
				ML.LANGUAGE, "identifier", ML.CONTENT, null };
		StatusObject<Object> stat = verifyMessage(message, m);

		// message is OK so far, but we need to check content types and retrieve the
		// objects
		if (stat.getStatusValue() == 0 || stat.getStatusValue() == -1) { // success
			String typeName = (stat.getStatusValue() == 0) ? message
					.getParameter(ML.CONTENT) : null; // null means 'get all'
			String content;
			try {
				content = (typeName == null) ? typeHierarchy.toString() : typeHierarchy
						.describe(typeName);
				pd.put(ML.CONTENT, content);
				pd.put(ML.LANGUAGE, "casa.TypeHierarchy");
			} catch (IllegalOperationException e) {
				Status s = new Status(-6, "Unknown Type: " + e);
				pd.put(ML.PERFORMATIVE, ML.NOT_UNDERSTOOD);
				pd.put(ML.LANGUAGE, "casa.*." + Status.class.getName());
				pd.put(ML.CONTENT, CASAUtil.serialize(s));
			}
		} else { // message is wrong
			if (stat.getStatusValue() > 0)
				stat.setStatusValue(-stat.getStatusValue());
			pd.put(ML.PERFORMATIVE, ML.NOT_UNDERSTOOD);
			pd.put(ML.LANGUAGE, "casa.*." + Status.class.getName());
			pd.put(ML.CONTENT, CASAUtil.serialize(stat));
		}
		out("TransientAgent.perform_get_ontology");

		return pd;
	}

	/**
	 * The code for responding to an {@link ML#REQUEST request} message.
	 * Subclasses may override this method to change the behavior.
	 * 
	 * @param message
	 *          the message the percipitated this action
	 * @return the description containing a {@link Status} object and any changes
	 *         to the default return message (a null return indicates "success"
	 *         and no changes).
	 */
	protected PerformDescriptor consider_ping(MLMessage message) {
		in("TransientAgent.consider_ping");
		PerformDescriptor result = new PerformDescriptor(new Status(0,
				"Yes, I'm here."));
		result.put(ML.CONTENT, "Yes, I'm here!");
		result.put(ML.PERFORMATIVE, ML.SUCCESS);
		result.put(ML.ACT, message.getAct().push(ML.DISCHARGE).toString());
		out("TransientAgent.consider_ping");
		return result;
	}

	/**
	 * The code for responding to an {@link ML#REQUEST request} message.
	 * Subclasses may override this method to change the behavior. In this case,
	 * this method checks to see if the command is an <em>exit</em> command, and
	 * marks the return to send a {@link ML#REFUSE refuse} message back. Otherwise
	 * it returns null, which indicates sending an {@link ML#AGREE agree} message.
	 * 
	 * @param message
	 *          the message the percipitated this action
	 * @return null, but normally the description containing a {@link Status}
	 *         object and any changes to the default return message (a null return
	 *         indicates "success" and no changes).
	 */
	public PerformDescriptor consider_execute(MLMessage message) {
		in("TransientAgent.consider_execute");
		PerformDescriptor pd = null;
		String command = message.getParameter("content");
		if (command != null) {
			command = command.trim();
			if (command.length() >= 4 && command.substring(0, 4).equals("exit")) {
				pd = new PerformDescriptor(new Status(-1,
						"Won't exit on a remote command"));
				pd.put(ML.PERFORMATIVE, ML.REFUSE);
			}
		}
		out("TransientAgent.consider_execute");
		return pd;
	}

	// /**
	// * The code for responding to an non-{@link ML#AGREE request} {@link
	// ML#REPLY_REQUEST reply} message.
	// * Subclasses may override this method to change the behavior.
	// * In this case, it's just returns null, which indicates doing nothing.
	// * @param message the message the percipitated this action
	// * @return null
	// */
	// protected PerformDescriptor verify_execute (MLMessage message) {
	// in ("TransientAgent.verify_execute");
	// out ("TransientAgent.verify_execute");
	// return null;
	// }

	/**
	 * Determines if we should execute the contents of a request/execute incomming
	 * message. This implementation returns:
	 * <ol>
	 * <li>false if {@link {@link AbstractProcess#options
	 * #ExecuteRequest_paranoid} is true.
	 * <li>true if {@link {@link AbstractProcess#options#ExecuteRequest_trusting}
	 * is true.
	 * <li>true if the message seems to be from the local machine.
	 * <li>false otherwise.
	 * </ol>
	 * 
	 * @param message
	 * @return
	 */
	@LispAccessible(arguments = @LispAccessible.Argument(name = "MESSAGE", help = "The message to decide to execute or not."))
	public Boolean shouldDoExecuteRequest(MLMessage message) {
		assert isA(message.getParameter(ML.PERFORMATIVE), ML.REQUEST) /*&& isA(message.getAct(), ML.EXECUTE)*/;
		if (options.ExecuteRequest_paranoid)
			return false;
		if (options.ExecuteRequest_trusting)
			return true;
		try {
			URLDescriptor from = message.getURLParameter(ML.SENDER);
			if (from.getHost().isSiteLocalAddress())
				return true;
		} catch (Throwable e) {
			println("error", "TransientAgent.shouldDoExecuteRequest()", e);
		}
		return false;
	}

	/**
	 * The code for responding to an outgoing {@link ML#AGREE agree} message.
	 * Subclasses may override this method to change the behavior. In this case,
	 * it executes the command in the {@link ML#CONTENT content} field.
	 * 
	 * @param message
	 *          the message the precipitated this action
	 * @return the description containing a {@link Status} object and any changes
	 *         to the default return message (a null return indicates "success"
	 *         and no changes).
	 */
	public PerformDescriptor perform_execute(MLMessage message) {
		String content = message.getParameter(ML.CONTENT);

		// if the content is quoted, strip the quotes
		while (content != null && content.length() > 0 && content.charAt(0) == '"'
				&& content.charAt(content.length() - 1) == '"') {
			try {
				content = CASAUtil.fromQuotedString(content);
			} catch (ParseException e) {
				return new PerformDescriptor(new Status(-1,
						"Malformed content field: \"" + content
								+ "\".  Expected a legal run-time command"));
			}
		}

		PerformDescriptor ret = new PerformDescriptor();
		try {
			BufferedAgentUI ui = new BufferedAgentUI();
			Status execResult = abclEval(content, null, ui);

			// send back FAILURE, SUCCESS or PROPOSE depending on a status value of
			// <0, 0 or >0.
			if (execResult.getStatusValue() < 0) { // FAILURE return
				ret.put(ML.PERFORMATIVE, ML.FAILURE);
				ret.put(ML.CONTENT,
						CASAUtil.serialize(content, execResult, ui.result()));
				ret.put(ML.LANGUAGE, "casa.*");
			} else { // successful PROPOSE return
				ret.put(ML.PERFORMATIVE, ML.PROPOSE);
				ret.put(ML.CONTENT,
						CASAUtil.serialize(content, execResult, ui.result()));
				ret.put(ML.LANGUAGE, "casa.*");
			}
		} catch (Throwable e) {
			ret = new PerformDescriptor(-4, println(
					"error",
					"Failed to execute command from agent "
							+ message.getParameter(ML.SENDER) + ": " + content, e));
			ret.put(ML.PERFORMATIVE, ML.FAILURE);
			ret.put(ML.CONTENT, CASAUtil.serialize(ret));
			ret.put(ML.LANGUAGE, "casa.*");
		}
		out("TransientAgent.perform_execute");
		return ret;
	}

	/**
	 * The code for responding to an {@link ML#PROPOSE_DISCHARGE propose}/
	 * {@link ML#DISCHARGE}|... message. Subclasses may override this method to
	 * change the behavior. In this case, this method notifies observers with a
	 * STATE_POST_STRING.
	 * 
	 * @param message
	 *          the message the precipitated this action
	 * @return a descriptor indicating success or failure at unmarshalling the
	 *         message.
	 */
	public PerformDescriptor release_execute(MLMessage message) {
		String lang = message.getParameter(ML.LANGUAGE);
		String performative = message.getParameter(ML.PERFORMATIVE);
		String content = message.getParameter(ML.CONTENT);

		String problem = "array";
		try {
			Object o = CASAUtil.unserializeArray(content, null);
			Object[] objects = (Object[]) o;
			problem = "originalCommand String";
			String originalCommand = (String) objects[0];
			problem = "Status object";
			Status stat = (Status) objects[1];
			problem = "text String";
			String text = (String) objects[2];
			problem = "object from Status";
			Object object = (stat instanceof StatusObject<?>) ? ((StatusObject<?>) stat)
					.getObject() : null;

			// boolean html =
			// (commandInterpreter.getStyle()==RTCommandInterpreter.STYLE_HTML);
			boolean html = false;

			if (stat != null && stat.getStatusValue() >= 0) {
				notifyObservers(ML.EVENT_POST_STRING, "\n"
						+ (html ? "<font color=blue><b>" : "") + "Command result from "
						+ message.getFromString() + ":" + (html ? "</b>" : "") + "\n"
						+ stat + "\n" + text + (html ? "</font>\n" : "\n"));
			} else {
				if (isLoggingTag("warning"))
					println("warning",
							"TransientAgent.release_execute: got bad status in message:\n"
									+ message.toString(true));
				notifyObservers(ML.EVENT_POST_STRING, "\n+"
						+ (html ? "<font color=red><b>" : "")
						+ "Failed command result from " + message.getFromString() + ":"
						+ (html ? "</b>" : "") + "\n" + stat + "\n" + text
						+ (html ? "</font>\n" : "\n"));
			}

			return new PerformDescriptor();
		} catch (Throwable e) {
			PerformDescriptor ret = new PerformDescriptor(
					new Status(
							-747,
							println(
									"error",
									"TransientAgent.release_execute: cannot unmarshal '"
											+ problem
											+ "' from CONTENT field \n  \""
											+ content
											+ "\"\n  -- expecting a tuple [originalCommand:String status:Status textOutput:String]"
											+ problem, e)));
			ret.put(ML.LANGUAGE, "text");
			return ret;
		}
	}

	// ************ Server methods for an INFORM execute conversation
	// **************

	// ////////////////////////////////////////////////////////////////////////////
	// GET_AGENTS_REGISTERED /////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////
	/**
	 * Process an incoming <em>propose-discharge</em> or <em>cancel</em> message
	 * for a <em>get_agents_registered</em> act-type request conversation. Note
	 * that the propose-discharge form could be {@link casa.ML#SUCCESS} or
	 * {@link casa.ML#FAILURE}. You should use this method the process the either
	 * the client's or server's response. A client should be the only role getting
	 * the propose-discharge form. Either could receive a cancel. This method is
	 * required.
	 * 
	 * @param msg
	 *          The incoming message
	 * @return The result of the processing; the status part will influence the
	 *         return reply-propose-discharge message (-ve will cause a
	 *         {@link casa.ML#REJECT_PROPOSAL}, otherwise the reply will be
	 *         {@link casa.ML#ACCEPT_PROPOSAL}), and the key/value part will be
	 *         overlaid on the return message.
	 * @since 11-Mar-09
	 * @updated 1-Jun-10
	 */
	public PerformDescriptor release_get_agents_registered(MLMessage msg) {
		in("TransientAgent.release_get_agents_registered");

		PerformDescriptor ret = new PerformDescriptor();

		// The other party CANCELS (unexpectedly)
		if (isA(msg.getParameter(ML.PERFORMATIVE), ML.CANCEL)) {
			ret.setStatus(new Status(-1, "request/get_agents_registered canceled"));
		}

		// A success message
		else if (isA(msg.getParameter(ML.PERFORMATIVE), ML.AFFIRMATIVE_REPLY)) { // this
																																						 // is
																																						 // an
																																						 // OUTGOING
																																						 // release
			ret.setStatus(new Status(0));
			ret.put(ML.CONTENT, msg.getParameter(ML.CONTENT));
		}

		// A failure of some sort
		else {
			ret.setStatus(new Status(-1, "request/get_agents_registered failed"));
		}

		out("release_get_agents_registered");
		return ret;
	}

	// ////////////////////////////////////////////////////////////////////////////
	// GET_AGENTS_RUNNING ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////

	/**
	 * Process an incoming <em>propose-discharge</em> or <em>cancel</em> message
	 * for a <em>getAgentsRunning</em> act-type request conversation. Note that
	 * the propose-discharge form could be {@link casa.ML#SUCCESS} or
	 * {@link casa.ML#FAILURE}. You should use this method the process the either
	 * the client's or server's response. A client should be the only role getting
	 * the propose-discharge form. Either could receive a cancel. This method is
	 * required.
	 * 
	 * @param msg
	 *          The incoming message
	 * @return The result of the processing; the status part will influence the
	 *         return reply-propose-discharge message (-ve will cause a
	 *         {@link casa.ML#REJECT_PROPOSAL}, otherwise the reply will be
	 *         {@link casa.ML#ACCEPT_PROPOSAL}), and the key/value part will be
	 *         overlaid on the return message.
	 * @since 11-Mar-09
	 * @updated 1-Jun-10
	 */
	public PerformDescriptor release_get_agents_running(MLMessage msg) {
		in("TransientAgent.release_get_agents_running");
		PerformDescriptor ret = new PerformDescriptor();

		// Success!
		if (isA(msg.getParameter(ML.PERFORMATIVE), ML.PROPOSE)) {
			ret = new PerformDescriptor(new Status(0));
			try {
				saveReturnedData(ML.GET_AGENTS_RUNNING,
						(Hashtable<String, URLDescriptor>) CASAUtil.unserialize(msg
								.getParameter(ML.CONTENT), URLDescriptor.class.getCanonicalName()));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		// A failure of some sort
		else {
			ret.setStatus(new Status(-1, "request/get_agents_running failed"));
		}
		out("TransientAgent.release_get_agents_running");
		return ret;
	}

	/**
	 * An empty placeholder tof the resolve_url conversation. Subclasses may
	 * override if necessary, but the functionality of the propose/discharge
	 * message is handed by the observer in a
	 * {@link #sendRequestAndWait(MLMessage, long, MessageEventDescriptor...)}
	 * call when {@link #sendMessage(MLMessage)} tries to resove an unresolved
	 * URL.
	 * 
	 * @param msg
	 *          The message
	 */
	public PerformDescriptor release_resolve_url(MLMessage msg) {
		return null;
	}

	/**
	 * Handles get.performative replies from another agent. The expected format
	 * is:
	 * <table border="1" bgcolor="orange" cellpadding="3">
	 * <tr>
	 * <td><a href="doc-files/performatives.gif">performative</a></td>
	 * <td>reply</td>
	 * </tr>
	 * <tr>
	 * <td>{@link ML#ACT act}</td>
	 * <td>get.performatives</td>
	 * </tr>
	 * <tr>
	 * <td>language</td>
	 * <td><a href="doc-files/contentLanguages.html#casa.TypeHierarachy">casa.
	 * TypeHierarchy </a></td>
	 * </tr>
	 * <tr>
	 * <td>content</td>
	 * <td>
	 * <em>a description of the requested performative(s) conforming to <a href="doc-files/contentLanguages.html#casa.TypeHierarachy">casa.TypeHierarchy</a> </em>
	 * </td>
	 * </tr>
	 * </table>
	 * or standard error replies. If the message is successfully interpreted,
	 * updates the local performatives type hierarchy with a call to
	 * {@link #template_handleNewOntologyInfo(MLMessage, String)}
	 * 
	 * @param msg
	 *          the incoming get.members request message
	 * @return a status describing the success or failure of the operation
	 */
	@Override
	public PerformDescriptor release_get_ontology(MLMessage msg) {
		in("TransientAgent.release_get_types");
		String content = msg.getParameter(ML.CONTENT);
		String language = msg.getParameter(ML.LANGUAGE);
		PerformDescriptor pd = null;

		if (!(language == null || language.equals("casa.TypeHierarchy"))) {
			out("TransientAgent.release_get_types");
			pd = new PerformDescriptor(
					new Status(
							-11,
							"TransientAgent.release_get_types: language does not equal 'casa.TypeHierarchy'"));
			pd.put(ML.PERFORMATIVE, ML.NOT_UNDERSTOOD);
			pd.put(ML.LANGUAGE, "casa.*");
			pd.put(ML.CONTENT, CASAUtil.serialize(pd.getStatus()));
		} else {
			// call the template method to handle the info
			template_handleNewOntologyInfo(msg, content);
		}

		out("TransientAgent.release_get_types");
		return pd;
	}

	/**
	 * Template method called by handleReply_GetTypes() to do whatever the agent
	 * wants to do with the new type information. This implemenation adds it to
	 * the agent's local performative type hierarchy.
	 * 
	 * @param msg
	 *          the message (it's already been processed to get <em>types</em>.
	 *          Only provided here to disabiguate if necessary
	 * @param types
	 *          The type information that resulted from the above request/reply.
	 */
	protected Status template_handleNewOntologyInfo(MLMessage msg, String types) {
		in("TransientAgent.template_handleNewPerformativeInfo");
		Status ret = putExtendedOntology(types);
		out("TransientAgent.template_handleNewPerformativeInfo");
		return ret;
	}

	/**
	 * Returns the String (persistent) representation of the performatives
	 * hiearchy for this agent. This is compatable with the String constructor of
	 * TypeHiearchy.
	 * 
	 * @return the String (persistent) representation of the performatives
	 *         hiearchy for this agent.
	 */
	@Override
	public String getSerializedOntology() {
		in("TransientAgent.getSerializedOntology");
		String ret = ontology.toString();
		out("TransientAgent.getSerializedOntology");
		return ret;
	}

	static public final String defaultOntologyClassName = "casa.ontology.owl2.OWLOntology"; // "casa.ontology.v3.CASAOntology";
	// static public final Class<? extends Ontology> defaultOntologyClass =
	// casa.ontology.owl2.OWLOntology.class;
	// //casa.ontology.v3.CASAOntology.class;

	String ontologyEngine = "none";

	@LispAccessible
	public String getOntologyEngine() {
		if (ontologyEngine.equals("none")) {
			Ontology ont = getOntology();
			if (ont!=null)
				return ont.getClass().getCanonicalName();
		}
		return ontologyEngine;
	}

	protected Method ontologyEngineLoadMethod = null;

	private Method getOntologyEngineLoadMethod() {
		if (ontologyEngineLoadMethod == null) {
			ontologyEngineLoadMethod = getMethod("ONTOLOGYENGINE",
					"getOntology", new Class[] { String.class }, defaultOntologyClassName,
					Ontology.class, Ontology.class);
			if (ontologyEngineLoadMethod!=null)
				ontologyEngine = ontologyEngineLoadMethod.getDeclaringClass().getCanonicalName();
		}
		return ontologyEngineLoadMethod;
	}

	protected Method ontologyEngineCreateMethod = null;

	private Method getOntologyEngineCreateMethod() {
		if (ontologyEngineCreateMethod == null) {
			ontologyEngineCreateMethod = getMethod("ONTOLOGYENGINE",
					"makeOntology", new Class[] { String.class, Ontology[].class },
					defaultOntologyClassName, Ontology.class, Ontology.class);
			if (ontologyEngineCreateMethod != null)
				ontologyEngine = ontologyEngineCreateMethod.getDeclaringClass()
				.getCanonicalName();
		}
		return ontologyEngineCreateMethod;
	}

	private Method getMethod(String agentParamName, String methodName,
			Class[] params, String defaultTargetClassName, Class<?> baseClass,
			Class<?> returnType) {
		Method method = null;

		// load the target class based on the initParams
		String targetClass = (String) initParams.getJavaObject(agentParamName);
		if (targetClass == null)
			targetClass = defaultTargetClassName;
		Class<?> cls;

		// attempt to get the method, methodName, from the target class
		try {
			cls = (Class<?>) Class.forName(targetClass);
			if (baseClass != null && !baseClass.isAssignableFrom(cls)) {
				throw new Exception("The class specified by \"" + targetClass
						+ "\" of type " + cls.getCanonicalName() + " is not a subclass of "
						+ baseClass.getCanonicalName());
			}
			method = cls.getMethod(methodName, params);
			if (!returnType.isAssignableFrom(method.getReturnType()))
				throw new Exception("The return type of method \"" + methodName
						+ "\" is type " + method.getReturnType().getCanonicalName()
						+ " is not a subclass of " + returnType.getCanonicalName());
		} catch (Throwable e) { // if we failed, try again with the DEFAULT target
														// class
			println("warning", "Failed to load ontology engine '" + targetClass
					+ "', loading engine '" + defaultTargetClassName + "' instead.");
			try {
				cls = Class.forName(defaultTargetClassName);
				method = cls.getMethod(methodName, params);
			} catch (Throwable e1) {
				println("error", "Unexpected exception", e1);
				method = null;
			}
		}
		return method;
	}

	/**
	 * Returns the agent's current ontology. If the agent doesn't currently have
	 * an ontology, attemps to load it using files with the following names and
	 * extension ".ont.lisp" or ".owl" in the following order, stopping after the ontology
	 * is loaded:
	 * <ol>
	 * <li>the name of this individual agent
	 * <li>the name of this agent's class
	 * <li>the name of this agent's superclasses from most specific to most
	 * general
	 * </ol>
	 * 
	 * @return Either the current ontology, or null if the agent doesn't have one
	 *         and it can't be loaded.
	 */
	@Override
	@LispAccessible(help = "Return the agent's current default ontology")
	public Ontology getOntology() {
		ParamsMap initParams = getInitParams();
		if (ontology != null) {
			return ontology;
		}

		Method loadMethod = getOntologyEngineLoadMethod();

		String myOntName = getURL().getFile();
		// Ontology myOnt = null;
		try {
			ontology = (Ontology) loadMethod.invoke(null, myOntName);
			if (ontology != null) {
				println("info", "Loaded ontology " + myOntName);
				return ontology; // since the agent's actually specific ontology is loaded,
												 // there's no need to do more.
			}
		} catch (Throwable e1) {
		}
		if (ontology == null) { // however, if we need to CREATE the agent's
														// specific ontology, then we should look for a super
														// ontology for it.
			Method createMethod = getOntologyEngineCreateMethod();
			try {
				ontology = (Ontology) createMethod.invoke(null, myOntName, new Ontology[] {});
				assert ontology != null;
			} catch (Throwable e) {
				println("error", "Could neither load nor create ontolgy " + myOntName, e);
				return null;
			}
		}

		Ontology importOnt = getDefaultImportOntology();
		if (importOnt != null)
			try {
				ontology.addSuperOntologies(importOnt);
				println("info",
						"Created agent's private ontology, and imported ontology "
								+ importOnt.getName());
			} catch (IllegalOperationException e) {
				println("error",
						"Failed to import known ontology " + importOnt.getName()
								+ " to agent's private ontology.", e);
			}
		else {
			println(
					"error",
					"Created agent's private ontology, but failed to find any specified ontology to import.");
		}

		return ontology;
	}

	private Ontology getDefaultImportOntology() {
		Method loadMethod = getOntologyEngineLoadMethod();
		// gather a list of candidate ontologies to import into the agent's
		// ontology. eg: TransientAgent, AbstractProcess, Thread, Object.
		Vector<String> candidateOntologies = new Vector<String>();
		String ontFile = (String) initParams.getJavaObject("ONTOLOGYFILE");
		if (ontFile == null) {
			// candidateOntologies.add(getURL().getFile());
			for (Class<? extends Object> cls2 = this.getClass(); cls2 != null; cls2 = cls2
					.getSuperclass()) {
				candidateOntologies.add(cls2.getName());
			}
		} else {
			candidateOntologies.add(ontFile);
		}

		// attempt to import the earliest candidate ontology
		Ontology importOnt = null;
		Vector<Throwable> exceptions = new Vector<Throwable>();
		for (String name : candidateOntologies) {
			// ontology = casa.ontology.v3.CASAOntology.getOntology(name);
			try {
				importOnt = (Ontology) loadMethod.invoke(null, name);
			} catch (Throwable e) {
				exceptions.add(e);
			}
			if (importOnt != null) {
				return importOnt;
			}
		}

		int i = 1;
		for (Throwable e: exceptions) {
			println("error", "Can't load any default ontology, (attempt "+i+++")", e);
		}

		// if we failed by using a specified engine and file, then recurse using the
		// defaults only as a last-ditch effort.
		if (importOnt == null
				&& (initParams.containsKey("ONTOLOGYFILE") || initParams
						.containsKey("ONTOLOGYENGINE"))) {
			initParams.remove("ONTOLOGYFILE");
			initParams.remove("ONTOLOGYENGINE");
			return getDefaultImportOntology();
		}

		String suffix = getOntologyFileSuffix();
		if (importOnt == null) {
			// can't use println() because it will recursively call this method again
			StringBuilder b = new StringBuilder(
					"Can't load ontology from one of any of the following files:");
			for (String name : candidateOntologies)
				b.append("\n  ").append(name).append(suffix);
			b.append("\nin any of the following directories:");
			for (String name : getDefFileSystemLocations())
				b.append("\n  ").append(name);
			b.append("\n  or from the system class loader resources.");
			Trace.log("error", b.toString());
		}
		return importOnt;
	}

	public void setOntology(Ontology ont) {
		in("TransientAgent.getOntology");
		ontology = ont;
		out("TransientAgent.getOntology");
		// if (ontology==null) ontology = new CASAOntology(this);
	}
	
	public String getOntologyFileSuffix() {
		Ontology ont = getOntology();
		if (ont!=null) {
			return ont.getDefaultFileExtension();
		}
		return ".?";
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * casa.interfaces.TransientAgentInterface#putExtendedOntology(java.lang.String
	 * )
	 */
	@Override
	@Deprecated
	public Status putExtendedOntology(String spec) {
		in("TransientAgent.putExtendedPerformativeHierarchy");

		Status stat;
		try {
			stat = new Status(0, "Added " + ontology.add(spec) + " types");
		} catch (Exception e) {
			stat = new Status(-10, "Failed to add to ontology", e);
		}
		if (stat.getStatusValue() == 0) {
			// notifyObservers (new casa.State (
			// ObservableEvent.STATE_INSERTED_PERFORMATIVES));
			notifyObservers(ML.EVENT_INSERTED_PERFORMATIVES, null);
		} else {
			if (isLoggingTag("warning"))
				println("warning", "TransientAgent.putExtendedPerformativeHierarchy: "
						+ stat.getExplanation());
			// notifyObservers (new casa.State (
			// ObservableEvent.STATE_FAILED_PERFORMATIVES_UPDATE));
			notifyObservers(ML.EVENT_FAILED_PERFORMATIVES_UPDATE, null);
		}
		out("TransientAgent.putExtendedPerformativeHierarchy");
		return stat;
	}

	/**
	 * Replace this agent's type performative hierarchy with the persistent
	 * TypeHierarchy data in <em>types</em>. If <em>types</em> is not well-formed,
	 * a negative status is returned and the original performative hiearchy is
	 * left unchanged.
	 * 
	 * @param types
	 *          A String constaining the persisent for of a TypeHierarchy
	 * @return a Status object indicating:
	 *         <ul>
	 *         <li>0: success
	 *         <li>-1: Attempted insertion of duplicate type node into
	 *         performatives type hiearchy
	 *         <li>-2: Malformed content field: Parent node not found in
	 *         performatives type heirarchy
	 *         <li>-3: Malformed content field: Parse exception
	 *         </ul>
	 */
	@Override
	@Deprecated
	public Status putReplacementOntology(String types) {
		in("TransientAgent.putReplacementPerformativeHierarchy");
		Ontology oldTypes = ontology;
		ontology = null;
		Status stat = putExtendedOntology(types);
		if (stat.getStatusValue() != 0)
			ontology = oldTypes;
		out("TransientAgent.putReplacementPerformativeHierarchy");
		return stat;
	}

	/**
	 * Sends a subscribe message to start/stop the observation of membership in a
	 * cooperation domain. Sends a message of format:
	 * <table * border="1" bgcolor="gold" cellpadding="3">
	 * <tr>
	 * <td><a href="doc-files/performatives.gif">performative</a></td>
	 * <td>subscribe</td>
	 * </tr>
	 * <tr>
	 * <td>{@link ML#ACT act}</td>
	 * <td>observe.membership</td>
	 * </tr>
	 * <tr>
	 * <td>timeout</td>
	 * <td>0</td>
	 * </tr>
	 * <tr>
	 * <td>sense</td>
	 * <td>positive | negative</td>
	 * </tr>
	 * <tr>
	 * <td>language</td>
	 * <td><a href="doc-files/contentLanguages.html#casa.agentCom.URLDescriptor">casa.
	 * URLDescriptor </a></td>
	 * </tr>
	 * <tr>
	 * <td>content</td>
	 * <td><em>a {@link URLDescriptor} object</em></td>
	 * </tr>
	 * </table>
	 * The corresponding handler is {@link #accept_membership_change(MLMessage)}.
	 * 
	 * @param cdURL
	 *          URLDescriptor of the cooperation domain to observe the membership
	 *          of
	 * @param sense
	 *          true to set observation; false to cancel
	 * @return a Status object describing success or failure of the sendMessage
	 */
	public Status doCDSubscribeMembership(URLDescriptor cdURL, boolean sense) {
		in("TransientAgent.doCDObserveMembership");
		MLMessage message = getNewMessage(sense ? ML.SUBSCRIBE : ML.CANCEL,
				(sense ? "" : (ML.SUBSCRIBE + "|")) + ML.CHANGE_MEMBERSHIP, cdURL);

		String test = URLDescriptor.class.getName();
		message.setParameter(ML.LANGUAGE, URLDescriptor.class.getName());
		message.setParameter(ML.CONTENT, getURL().toString(cdURL));
		message.setParameter(ML.REPLY_BY, Long.toString(Long.MAX_VALUE));

		if (sense) {
			watchedCooperationDomains.remove(cdURL);
			watchedCooperationDomains.put(cdURL,
					message.getParameter(ML.CONVERSATION_ID));
		} else {
			String wcd = watchedCooperationDomains.get(cdURL);
			if (wcd != null && wcd.length() > 0)
				message.setParameter(ML.CONVERSATION_ID, wcd);
			watchedCooperationDomains.remove(cdURL);
		}

		Status stat = sendMessage(message);

		out("TransientAgent.doCDObserveMembership");
		return stat;
	}

	/**
	 * Called when a CD this agent is a member of is in the process of exiting:
	 * the behaviour is to withdraw from that CD.
	 * 
	 * @param msg
	 *          The INFORM message that the CD sent
	 * @return null (which indicates "success"
	 */
	protected PerformDescriptor accept_deleteCD(MLMessage msg) {
		in("TransientAgent.accept_deleteCD");
		try {
			doWithdrawCD(msg.getFrom(), true);
		} catch (URLDescriptorException e) {
		}
		out("TransientAgent.accept_deleteCD");
		return null;
	}

	// ************ Server methods for an INFORM exit conversation **************

	/**
	 * Process an incoming <em>inform</em> message for a <em>exit</em> act. This
	 * method should return a {@link casa.PerformDescriptor PerformDescriptor}.
	 * {@link casa.PerformDescriptor#getStatusValue() getStatusValue()} of 0 or
	 * positive for success, or a negative value to indicate failure. It may
	 * influence any return value by calling
	 * {@link java.util.TreeMap#put(Object, Object)
	 * PerformDescriptor.put(messageKey,value)} on the return
	 * {@link casa.PerformDescriptor PerformDescriptor}.
	 * 
	 * @param msg
	 *          The incoming message
	 * @return The result of the processing; the status part will influence the
	 *         return message (if any), and the key/value part will be overlayed
	 *         on the return message (if any)
	 * @author kremer
	 * @since Dec 5, 2008
	 */
	public PerformDescriptor accept_exit(MLMessage msg) {
		in("TransientAgent.accept_exit");
		URLDescriptor exitingURL;
		try {
			exitingURL = msg.getURLParameter(ML.SENDER);
		} catch (Throwable e) {
			println("error",
					"TransientAgent.accept_exit(): Unexpected error parsing :SENDER field: "
							+ msg.getParameter(ML.SENDER), e);
			return null;
		}

		boolean shouldUnregister = false;
		boolean shouldExit = false;

		// is the LAC exiting?
		if (exitingURL.equals(registeredWithLAC)) { // LAC is exiting, so I should
																								// unregister or worse...
			shouldUnregister = true;
		}

		// is this process exiting?
		if (ProcessInfo.process!=null && exitingURL.equals(ProcessInfo.process.getURL())) {
			shouldUnregister = true;
			shouldExit = true;
		}

		if (shouldUnregister) {
			release_unregister_instance(msg);
			doUnregisterAgentInstance(true);
		}
		if (shouldExit) {
			exit();
		}

		// is it a CD we've joined exiting?
		for (URLDescriptor url : joinedCooperationDomains) {
			if (exitingURL.equals(url)) {
				doWithdrawCD(url, false);
				break;
			}
		}

		// is it a CD we're observing exiting?
		for (URLDescriptor url : watchedCooperationDomains.keySet()) {
			if (exitingURL.equals(url)) {
				doCDSubscribeMembership(url, false);
				break;
			}
		}

		out("TransientAgent.accept_exit");
		return null;
	}

	// ///////////////////////////////////////////////////////////////////////////
	// YP communication/status //////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////

	// TODO implement this
	/**
	 * This method is called when this agent wishes to advertises a service. The
	 * service added is advertised on a the YellowPages at the urlDescriptor yp.
	 * Adds the URLDescriptor and ServiceDescriptor pair to the
	 * bufferOfAdvertisements Hashmap. If the entry is already present in the
	 * bufferOfAdvertisements, the function simply returns.
	 * 
	 * @param yp
	 *          URLDescriptor of the agent advertising the service
	 * @param service
	 *          ServiceDescriptor of the service being advertised
	 * @return A Status object describing success or failure
	 */
	@Override
	public Status doAdvertise(URLDescriptor yp, ServiceDescriptor service) {
		println("warning", "Agent.advertise() not implemented.");
		return new Status(-1);
	}

	// TODO implement this
	/**
	 * This method is called when this agent wishes to withdraw a previously
	 * advertised service from a yellow pages service.
	 * 
	 * @param yp
	 *          URLDescriptor of the agent advertising the service
	 * @param service
	 *          ServiceDescriptor of the service being withdrawn
	 * @return A Status object describing success or failure
	 */
	@Override
	public Status doUnadvertise(URLDescriptor yp, ServiceDescriptor service) {
		// public void advertise (Status status, URLDescriptor yp, ServiceDescriptor
		// service);
		println("warning", "Agent.unadvertise() not implemented.");
		return new Status(-1);
	}

	// TODO implement this
	/**
	 * Attempts to search a yellow pages service for a agents matching a
	 * particular service descriptor.
	 * 
	 * @param yp
	 *          The URLDescriptor of the YP to search
	 * @param service
	 *          A (perhaps partial) ServiceDescriptor description of a service
	 *          that the YP should match its advertisements against
	 * @return A Vector of URLdescritors of matching services (this might be
	 *         expanded to be ServiceDescriptor/URLDescriptor pairs)
	 */
	@Override
	public StatusURLDescriptorList doSearchYP(URLDescriptor yp,
			ServiceDescriptor service) {
		// public void search (Status status, URLDescriptor yp, Vector urls);
		println("warning", "Agent.searchYP() not implemented.");
		return null;
	}

	// ///////////////////////////////////////////////////////////////////////////
	// other agent communication/status /////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////
	/**
	 * Requests an agent to join a particular cooperation domain.
	 * 
	 * @param agent
	 *          Agent this agent is requesting the join the CD.
	 * @param cd
	 *          The CD the agent should join.
	 * @return A Status object describing success or failure
	 */
	@Override
	public Status doInviteToCD(URLDescriptor agent, URLDescriptor cd,
			boolean sense) {
		in("TransientAgent.doInviteToCD");
		MLMessage message = getNewMessage(ML.REQUEST, ML.INVITE_CD, agent);

		message.setParameter(ML.LANGUAGE, URLDescriptor.class.getName());
		message.setParameter(ML.CONTENT, cd.toString(agent));

		if (sense) {
			message.setParameter(ML.SENSE, ML.POSITIVE);
		} else {
			message.setParameter(ML.SENSE, ML.NEGATIVE);
		}

		Status stat = sendMessage(message);
		out("TransientAgent.doInviteToCD");
		return stat;
	}

	/**
	 * The code for responding to an <b>outgoing</b> {@link ML#AGREE agree}
	 * message. Subclasses may override this method to change the behavior. In
	 * this case, the {@link #consider_invite_to_cd(MLMessage)} method should have
	 * already started the process of joining the CD, so this method only has to
	 * wait around until the CD is jointed as reflected in
	 * {@link #joinedCooperationDomains}. If this method is called before the CD
	 * in the {@link ML#CONTENT content} field appears in
	 * {@link #joinedCooperationDomains}, then it just returns a status of
	 * {@link #DEFER_ACTION} which causes nothing to happen but this method is
	 * called again later. However, if we're within 400ms of the timeout in the
	 * message, this method will set the appropate elements of the return to cause
	 * a {@link ML#FAILURE failure} reply.
	 * 
	 * @param message
	 *          the message the percipitated this action
	 * @return the description containing a {@link Status} object and any changes
	 *         to the default return message (a null return indicates "success"
	 *         and no changes).
	 */
	protected PerformDescriptor perform_invite_to_cd(MLMessage message) {
		in("TransientAgent.perform_invite_to_cd");
		PerformDescriptor ret = null;
		// Read the relevant info from the message
		String content = message.getParameter(ML.CONTENT);
		Long timeout = message.getTimeout();

		// Interpret the fields of the message
		try {
			URLDescriptor cd = null;
			cd = URLDescriptor.make(content);
			for (URLDescriptor url : joinedCooperationDomains) {
				if (url.equals(cd)) { // success!
					out("TransientAgent.perform_invite_to_cd");
					return null;
				}
			}
			if ((System.currentTimeMillis() + 400) > timeout) {
				ret = new PerformDescriptor(new Status(-789,
						"Join failed within the specified time"));
			} else { // haven't joined *yet*, so defer and get called again later...
				ret = new PerformDescriptor(new Status(DEFER_ACTION));
			}
		} catch (URLDescriptorException e) {
			String x = "TransientAgent.perform_invite_to_cd: Could not interpret data object URL in content field";
			println("warning", x, e);
			ret = new PerformDescriptor(new Status(-56, x + ": " + e));
		}
		if (ret != null) {
			ret.put(ML.PERFORMATIVE, ML.FAILURE);
			ret.put(ML.LANGUAGE, Status.class.getName());
			ret.put(ML.CONTENT, ret.getStatus().toString());
		}
		out("TransientAgent.perform_invite_to_cd");
		return ret;
	}

	/**
	 * The code for responding to an {@link ML#REQUEST request} message.
	 * Subclasses may override this method to change the behavior. In this case,
	 * it calls {@link #doJoinCD(URLDescriptor)} with the URL in the
	 * {@link ML#CONTENT content} field. If the send message done by
	 * {@link #doJoinCD(URLDescriptor)} goes well, it causes an {@link ML#AGREE
	 * agree} reply by returning null. Otherwise, it causes either a
	 * {@link ML#NOT_UNDERSTOOD not-understood} or a {@link ML#REFUSE} (already
	 * joined) reply by setting the appropriate elements in the return.
	 * 
	 * @param message
	 *          the message the percipitated this action
	 * @return the description containing a {@link Status} object and any changes
	 *         to the default return message (a null return indicates "success"
	 *         and no changes).
	 */
	protected PerformDescriptor consider_invite_to_cd(MLMessage message) {
		in("TransientAgent.consider_invite_to_cd");
		PerformDescriptor ret = null;
		// Read the relevant info from the message
		String content = message.getParameter(ML.CONTENT);

		// Interpret the fields of the message
		try {
			URLDescriptor cd = null;
			cd = URLDescriptor.make(content);
			Status result = doJoinCD(cd);
			if (result.getStatusValue() != 0) {
				ret = new PerformDescriptor(result);
			} else { // success!
				// notifyObservers (new casa.State (ObservableEvent.STATE_INVITE_CD));
				notifyObservers(ML.EVENT_INVITE_CD, null);
			}
		} catch (URLDescriptorException e) {
			String x = "TransientAgent.consider_invite_to_cd: Could not interpret data object URL in content field";
			println("warning", x, e);
			ret = new PerformDescriptor(new Status(-56, x + ": " + e));
		}
		if (ret != null) {
			ret.put(ML.PERFORMATIVE, ret.getStatusValue() == 2 ? ML.REFUSE
					: ML.NOT_UNDERSTOOD);
			ret.put(ML.LANGUAGE, Status.class.getName());
			ret.put(ML.CONTENT, ret.getStatus().toString());
		}
		out("TransientAgent.consider_invite_to_cd");
		return ret;
	}
	
	public PerformDescriptor release_FIPAStyle(MLMessage message) {
		System.out.println("release called");
		return null;
	}
	
	public PerformDescriptor perform_FIPAStyle(MLMessage message) {
		System.out.println("perform called");
		return null;
	}
	

	// ///////////////////////////////////////////////////////////////////////////
	// UTILITY METHODS //////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * Deal with the various error subtypes of reply by constructing an
	 * appropriate Status (with explanation String). Errors are not logged. The
	 * messages handled those with performatives:
	 * <ul>
	 * <li>timeout (return Status(-102))
	 * <li>failure (return Status(-103))
	 * <li>not-understood (return Status(-104))
	 * <li>refuse (return Status(-105))
	 * </ul>
	 * Any messages not falling into the above categories results in return Status
	 * with value -110.
	 * 
	 * @param msg
	 *          the MLMessage to interpret
	 * @param requestType
	 *          a string to prefix to the error message in the returned Status
	 *          (usually an <em>act</em>)
	 * @return a Status with a negative error code and an informative String
	 */

	protected Status handleErrorReplies(MLMessage msg, String requestType) {
		in("TransientAgent.handleErrorReplies");
		int stat;
		String reason;
		String perf = msg.getParameter(ML.PERFORMATIVE);
		if (perf.equals(ML.REPLY_BY)) {
			stat = -102;
			reason = "timed out";
		} else if (perf.equals("failure")) {
			stat = -103;
			reason = "returned failue: " + msg.getParameter(ML.CONTENT);
		} else if (perf.equals("not-understood")) {
			stat = -104;
			reason = "resulted in message not understood by recipient";
		} else if (perf.equals("refuse")) {
			stat = -105;
			reason = "refused by recipient";
		} else {
			stat = -110;
			reason = "resulted in unknown message:\n" + msg.toString(true);
		}
		out("TransientAgent.handleErrorReplies");
		return new Status(stat, requestType + " " + reason);
	}

	/**
	 * Verifies that a message comforms to specification in <em>list</em>. Even
	 * indexes are keys and odd indexes are values. A key/value pair of X/null
	 * indicates the key must be there, but the value can be anything. A key/value
	 * pair of X/Y indicates key must be there and the value must be Y.<br>
	 * In addition, if the language field and content fields are in <em>list</em>
	 * and the language field starts with "casa.", this method will try to
	 * interpret the content fild as a CASA obect using the reflexive classes.
	 * This is <b>only </b> attempted if the language field starts with "casa.".
	 * It will try to instantiate an object from the content field by:
	 * <ol>
	 * <li>calling {@link CASAUtil#unserialize(String, String)}(the most general way of
	 * passing an object (polymorphic [subtypes]) objects can be passed). But it's
	 * possible the object is not the object expected by the caller.
	 * <li>calling the class's String constructor
	 * <li>calling the class's empty construtor and:
	 * <ol>
	 * <li>calling the object's fromString(String) method
	 * <li>calling the object's fromString( {@link TokenParser}) method
	 * </ol>
	 * </ol>
	 * The "casa." language is interpeted specially. If the language specification
	 * is of the form:
	 * <ul>
	 * <li><b>casa.[casa-class-identifier] </b>: an attempt is made to instantiate
	 * object as above
	 * <li><b>casa.* </b>: an attempt is made to intantiate with
	 * CASAUtil.unserialize, and no check is made
	 * <li><b>casa.*.[complete-class-identifier] </b>: an attempt is made to
	 * instantiate with CASAUtil.unserialize, and -4 is returned if the object is
	 * not assignment compatable with [complete-class-identifier].
	 * </ul>
	 * 
	 * @param msg
	 *          a MLMessage to examine
	 * @param list
	 *          an array of key/values alternating.
	 * @return A StatusObject of
	 *         <ul>
	 *         <li>(0,[content field val],[content object]) if the message passes
	 *         <li>(1,[content field val],null) if an attempt to interpret the
	 *         contents field failed
	 *         <li>(-1,[explanation],null) if missing key
	 *         <li>(-2,[explanation],null) if wrong value
	 *         <li>(-3,[explanation],null) if the parameter <em>msg</em> is null
	 *         <li>(-4,[explanation],[content object]) if there was an
	 *         instantiable object in the content, but it wasn't assignment
	 *         compatable with the casa.*.[class] language specificaion.
	 *         <li>(-5,[explanation],null) if the performative field has the wrong
	 *         value
	 *         <li>(-6,[explanation],null) if the act field has the wrong value
	 *         <li>([less than -100],[explanation],null) if an error message of
	 *         some sort (see {@link #handleErrorReplies(MLMessage,String)})
	 *         </ul>
	 */
	public StatusObject<Object> verifyMessage(MLMessage msg, String[] list) {
		StatusObject<Object> ret = null;
		String performative = null;
		String act = "";
		String language = null;
		String content = null;

		// null message?
		if (msg == null)
			ret = new StatusObject<Object>(-3, "Unexpected null reply received");

		else {

			// we have a message, check it against the list
			for (int i = 0, len = list.length; i < len; i += 2) {
				String key = list[i];
				if (key == null) {
					println("error", "MLMessage(String[]): Null keys are not allowed");
					continue;
				}
				if ((i + 1) > len) {
					println("error",
							"MLMessage(String[]) requires an even number of elements (key/value pairs)");
					break;
				}
				String val = list[i + 1];
				String mval = msg.getParameter(key);
				if (key.equals(ML.PERFORMATIVE)) {
					performative = mval;
					if (!isAPerformative(mval, val)) {
						ret = new StatusObject<Object>(-5, "Expected " + key
								+ " field with value of a performative-subtype of '"
								+ (val == null ? "null" : val) + "', found value of '"
								+ (mval == null ? "null" : mval) + "'.");
						break;
					}
					continue;
				}
				if (key.equals(ML.ACT)) {
					act = mval;
					Act temp = new Act(act);
					temp = new Act(temp.lastElement());
					if (!isAAct(temp, new Act(val))) {
						ret = new StatusObject<Object>(-6, "Expected " + key
								+ " field with value of a act-subtype of '"
								+ (val == null ? "null" : val) + "', found value of '"
								+ (mval == null ? "null" : mval) + "'.");
						break;
					}
					continue;
				}
				if (key.equals(ML.LANGUAGE)) {
					language = mval;
					if (val.equals("casa.*") && mval != null && mval.length() >= 6
							&& mval.substring(0, 6).equals("casa.*")) // casa.* matches
																												// casa.*<anything>"
						continue;
				}
				if (key.equals(ML.CONTENT))
					content = mval;
				if (mval == null) {
					ret = new StatusObject<Object>(-1, "Expected " + key
							+ " field in message.");
					break;
				}
				if (val != null && !val.equals(mval)) {
					ret = new StatusObject<Object>(-2, "Expected " + key
							+ " field with value '" + val + "', found value of '"
							+ (mval == null ? "null" : mval) + "'.");
					break;
				}
			} // for

			// if we found a problem, try to interpret it as an error reply
			if (ret != null || (isAPerformative(performative, "nack"))) {
				Status stat = handleErrorReplies(msg, act);
				if (stat.getStatusValue() != -110)
					ret = new StatusObject<Object>(stat.getStatusValue(),
							stat.getExplanation());
			}

			// no problems: try to interpret the content field as a "casa." object
			else {
				Object obj = null;
				int stat_n = 0;
				if (language != null && content != null
						&& language.substring(0, 5).equals("casa.")) {
					// try to instantiate and object from the content field
					try { // CASA serialization
						obj = CASAUtil.unserialize(content, language);
						if (obj instanceof String && !content.startsWith("\"")) { // we got a string that wasn't explicitly surrounded in quotes
							throw new Exception(); // this is just to execute the exception code below for another attempt
						}
						if (language.equals("casa.*")) {
						} else if (language.substring(0, 7).equals("casa.*.")) { // check it
																																		 // only if
																																		 // it's got
																																		 // a
																																		 // subtype
																																		 // specification
							if (!Class.forName(language.substring(7)).isInstance(obj)) {
								content = "instantiable content did not match language '"
										+ language + "' in language specification";
								stat_n = -4;
							}
						} else {
							if (!Class.forName(language).isInstance(obj)) {
								content = "instantiable content did not match language for casa language specification";
								stat_n = -4;
							}
						}
					} catch (Exception ex3) { // not a "serialize()" object
						obj = CASAUtil.interpretString(content, language);
						if (obj==null) {
							stat_n = 1;
							content = "mangled or missing content in messasge";
							String m2 = "TransientAgent.verifyMessage: mangled or missing .content in messasge:\n"
									+ msg.toString(true);
							println("warning", m2);
						}
					}
				}
				ret = new StatusObject<Object>(stat_n, content, obj);
			}
		} // else

		// finally, clean up and return
		if (ret.getStatusValue() < 0)
			if (isLoggingTag("warning"))
				println("warning", "TransientAgent.verifyMessage: failed message: "
						+ ret.toString() + ((msg == null) ? "" : "\n" + msg.toString(true)));
		return ret;
	}

	private AgentUI createNewInterface(TransientAgentInterface agent,
			String agentInterfaceName) {
		AgentUI agentUI = null;
		Class<?> arg_type2[] = { casa.TransientAgent.class, String[].class };
		Object m_args2[] = { agent, /* args */null };
		try {
			Class<?> cls = Class.forName(agentInterfaceName);
			// Method method = cls.getMethod("main", arg_type);
			Constructor<?> cons = cls.getConstructor(arg_type2);
			// Object agentInterface = method.invoke(null, m_args);
			Object agentInterface = cons.newInstance(m_args2);
			if (AgentUI.class.isAssignableFrom(agentInterface.getClass()))
				agentUI = (AgentUI) agentInterface;
		} catch (Exception ex1) { // failed to invoke specified interface
			String msg = "CASAComamndLine.createNewInterface(): cannot create interface '"
					+ agentInterfaceName + "'";
			println("error", msg, ex1);
			System.out.println(msg);
		}
		return agentUI;
	}
	
	// @SuppressWarnings("unused")
	// private class SendCommand extends Command{
	// @Override public Status execute(String line, Map<String, String> params,
	// AgentUI ui) {
	// MLMessage msg = MLMessage.getNewMLMessage();
	// //msg.setParameter(ML.FROM,getURL().toString());
	// msg.setParameter(ML.RECEIVER,getURL().toString());
	// Set<String> keys = params.keySet();
	// if (keys==null) return new Status(-3,"no parameters");
	// for (Iterator<String> i = keys.iterator() ; i.hasNext(); ) {
	// String key = i.next();
	// String val = params.get(key);
	// if (key.equals(ML.REPLY_BY)) {
	// long now = System.currentTimeMillis();
	// try {
	// val = String.valueOf (now + Long.parseLong (val));
	// } catch (NumberFormatException ex) {}
	// }
	// msg.setParameter (key, val);
	// }
	// return sendMessage (msg);
	// }
	// }

	/**
	 * Used to record all outgoing {@link ML#REQUEST requests} to match against
	 * incomming {@link ML#REPLY_REQUEST reply-requests} (and then remove them).
	 * This allows us to detect an unrequested reply.
	 */
	private TreeSet<MLMessage> outstandingRequests = new TreeSet<MLMessage>(
			new Comparator<MLMessage>() {
				@Override
				public int compare(MLMessage m1, MLMessage m2) {
					String r1 = m1.getParameter(ML.REPLY_WITH);
					String r2 = m2.getParameter(ML.REPLY_WITH);
					return r1 == null ? -1 : r1.compareTo(r2);
				}
			});

	/**
	 * Inserts <em>msg</em> input this list of outstanding requests.<br>
	 * <b>Precondition</b>: <em>msg</em> needs to be a subtype of
	 * {@link ML#REQUEST} or {@link ML#ACCEPT_PROPOSAL}. In addition, this method
	 * will add a reply_with field to the message if it isn't there already (since
	 * outstanding request checking depends on it).
	 * 
	 * @param msg
	 */
	private void addRequest(MLMessage msg) {
		assert (isA(msg.getParameter(ML.PERFORMATIVE), ML.PETITION)
		/* || isA(msg.getParameter(ML.PERFORMATIVE),ML.ACCEPT_PROPOSAL) */);// asserts
																																				// don't
																																				// seem
																																				// to
																																				// work
		if (msg != null && (isA(msg.getParameter(ML.PERFORMATIVE), ML.PETITION)
		/* || isA(msg.getParameter(ML.PERFORMATIVE),ML.ACCEPT_PROPOSAL) */)) {
			// make sure we have a reply_with embedded in the message
			if (msg.getParameter(ML.REPLY_WITH) == null)
				msg.setParameter(ML.REPLY_WITH, getUniqueRequestID());
			outstandingRequests.add(msg);
			println("outstandingRequests",
					"Added outstanding request: " + msg.toString());
		}
	}

	/**
	 * Finds a request matching (in the {@link ML#IN_REPLY_TO} and
	 * {@link ML#REPLY_WITH} fields), deletes the request from the internal list
	 * of requests and returns the matching message. <b>Precondition</b>:
	 * <em>msg</em> needs to be a subtype of {@link ML#REPLY}.
	 * 
	 * @param replyRequestMsg
	 * @return The deleted request message or null if a match can't be found.
	 */
	@SuppressWarnings("unused")
	private MLMessage matchRequestAndDelete(MLMessage replyRequestMsg) {
		assert (isA(replyRequestMsg.getParameter(ML.PERFORMATIVE), ML.REPLY)
		/*
		 * ||
		 * isA(replyRequestMsg.getParameter(ML.PERFORMATIVE),ML.PROPOSE_DISCHARGE)
		 */);
		MLMessage msgToDelete = matchRequest(replyRequestMsg);
		if (msgToDelete != null) {
			// TODO fix this hack for finding whether this should be deleted or not
			// (it shouldn't if this was an outgoing broadcaste
			if (!msgToDelete.isBroadcast()) {
				outstandingRequests.remove(msgToDelete);
				println("outstandingRequests", "Deleted outstanding request: "
						+ msgToDelete.toString());
			}
		}
		return msgToDelete;
	}

	/**
	 * Finds a request matching (in the {@link ML#IN_REPLY_TO} and
	 * {@link ML#REPLY_WITH} fields) and returns the matching message.
	 * <b>Precondition</b>: <em>msg</em> needs to be a subtype of {@link ML#REPLY}
	 * .
	 * 
	 * @param replyRequestMsg
	 * @return The deleted request message or null if a match can't be found.
	 */
	private MLMessage matchRequest(MLMessage replyRequestMsg) {
		assert (isA(replyRequestMsg.getParameter(ML.PERFORMATIVE), ML.REPLY)
		/*
		 * ||
		 * isA(replyRequestMsg.getParameter(ML.PERFORMATIVE),ML.PROPOSE_DISCHARGE)
		 */);// asserts don't seem to work
		if (replyRequestMsg != null
				&& (isA(replyRequestMsg.getParameter(ML.PERFORMATIVE), ML.REPLY)
				/*
				 * ||
				 * isA(replyRequestMsg.getParameter(ML.PERFORMATIVE),ML.PROPOSE_DISCHARGE
				 * )
				 */)) {
			for (MLMessage msg : outstandingRequests) {
				if (msg != null) {
					String replyWith = msg.getParameter(ML.REPLY_WITH);
					if (replyWith != null
							&& replyWith.equals(replyRequestMsg.getParameter(ML.IN_REPLY_TO))) {
						return msg;
					}
				}
			}
		}
		return null;
	}

	private String banner = null;

	/** Can be used by UI windows to get a title text */
	public String getBanner() {
		return banner;
	}

	/**
	 * Sets a banner string that may be used by UI windows, etc.
	 * 
	 * @param banner
	 *          A banner string; if null, then it will be the result of
	 *          {@link #getDefaultBanner()}.
	 */
	public void setBanner(String banner) {
		if (banner == null)
			this.banner = getDefaultBanner();
		else
			this.banner = banner;
		notifyObservers(ML.EVENT_BANNER_CHANGED, this.banner);
	}

	/**
	 * @return A default banner of the form
	 *         "[class] Agent [name]@[port] ([strategy]) [flags]..." where [flags]
	 *         include "[not registered]" and "[no CASAProcess]".
	 */
	public String getDefaultBanner() {
		URLDescriptor url = getURL();
		String b = this.getClass().getSimpleName() + " " + url.getFile() + "@"
				+ url.getPort() + " (" + getStrategy() + ")";
		if (!isRegistered())
			b += " [not registered]";
		if (ProcessInfo.process == null)
			b += " [no CASAProcess]";
		return b;
	}

	/****************************************************************************
	 **************************************************************************** 
	 ************************* LISP OPERATORS, ETC. *****************************
	 **************************************************************************** 
	 ****************************************************************************/

	private class abclEvalInAgentThread implements Runnable {
		public Status result = null;

		public boolean completed = false;

		private String c;

		private AgentUI ui;

		private Map<String, LispObject> bindings;

		private Thread thisThread;

		public abclEvalInAgentThread(Thread thisThread, final String c,
				Map<String, LispObject> newEnvBindings, final AgentUI ui) {
			this.c = c;
			this.ui = ui;
			this.bindings = newEnvBindings;
			// this.thisThread = thisThread;
		}

		@Override
		public void run() {
			result = abclEval(c, bindings, ui);
			completed = true;
			// thisThread.interrupt();
		}
	};

	/**
	 * Can be used to store Symbols in the lisp environment specifically for this
	 * agent.
	 */
	Environment lispEnvironment = new Environment(null, new Symbol("agent"),
			new JavaObject(this));

	/**
	 * Executes lisp code and returns a result. This method sets the following
	 * thread-bound variables, which may be made use of from any Lisp code that is
	 * executed as a result of this method:
	 * <ul>
	 * <li>agentForThread: set to point to the current agent (class
	 * {@link casa.TransientAgent}).
	 * <li>uiForThread: set to point to the ui parameter object (class
	 * {@link casa.ui.AgentUI}).
	 * <li>objectsForThread: set to point to a Map of the pairs passed in on the
	 * namesAndObjets parameter (class {@link java.util.Map}) with parameterized
	 * type String and Object). An important object listed in this map is the
	 * "event" object which is used by {@link #getEventForThread()} and
	 * {@link #getMsgForThread()} to get the current event and message objects
	 * respectively.
	 * </ul>
	 * 
	 * @param c
	 *          The lisp code
	 * @param ui
	 *          The lisp code may or may not write to or read from this ui object
	 * @return A Status object that may in fact be a {@link StatusObject}
	 *         containing an appropriate result object.
	 */
	@Override
	public Status abclEval(String c, Map<String, LispObject> newEnvBindings,
			AgentUI ui) {
		boolean lispExecutionOnlyInAgentThread = false;

		if (lispExecutionOnlyInAgentThread) {
			// Lisp evaluation can only occur in the context of an agent thread
			// because the casaLispOperators
			// may refer to the thread-local variables. So if this isn't running in
			// the agent's thread,
			// halt this thread while it's queued to the agent's thread (with an
			// appropriate timeout).
			String agentName = getURL() == null ? "unknown" : getURL().getFile();
			final Thread thisThread = Thread.currentThread();
			if (!thisThread.getName().equals(agentName)) {
				if (isInitialized()) {
					// ui.println("Deferring execution to agent thread. Cause: "+(isInitializationComplete()?(thisThread.getName().equals(agentName)?"Unknown":"Not running in agent thread"):"Still in initialization phase"));
					// @SuppressWarnings("unused")
					// Status ret;
					abclEvalInAgentThread runnable = new abclEvalInAgentThread(
							thisThread, c, newEnvBindings, ui);
					defer(runnable);
					long timeout = System.currentTimeMillis() + 3000;
					while (System.currentTimeMillis() < timeout) {
						try { // in this case we, want WANT the interrupt exception to
									// occur, it it didn't the agent timed out.
							sleep(timeout - System.currentTimeMillis());
						} catch (InterruptedException e) {
							if (runnable.completed)
								return runnable.result;
						}
					}
					return new Status(-12, "TransientAgent.abclEval(\"" + c
							+ "\" ...): Deferred to agent thread, but agent timed out.");
				}
			}

		}
		return casa.abcl.Lisp
				.abclEval(this, lispEnvironment, newEnvBindings, c, ui);
	}

	@Override
	public Status abclEval(String c, Map<String, LispObject> lispBindings) {
		BufferedAgentUI ui = new BufferedAgentUI();
		Status ret = abclEval(c, lispBindings, ui);
		String result = ui.result();
		if (result != null && result.length() > 0) {
			ret.setExplanation(ret.getExplanation() + "\n-- contents:\n" + result);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see casa.interfaces.PolicyAgentInterface#abclEval(org.armedbear.lisp.Cons,
	 * java.lang.Object[])
	 */
	@Override
	public Status abclEval(org.armedbear.lisp.Cons cons,
			Map<String, LispObject> lispBindings) {
		String c;
		try {
			c = cons.writeToString();
		} catch (ControlTransfer e) {
			return new Status(-56, println("error",
					"Can't convert Cons object to String for evaluation", e));
		}
		return abclEval(c, lispBindings);
	}

	/**
	 * @param unmarked
	 *          a set of SocialCommitments to choose one from
	 * @return A social commitment to work on next from the parameter collection;
	 *         or null to leave the selection to the caller
	 */
	@Override
	public SocialCommitment chooseSC(
			Collection<SocialCommitment> SocialCommitments) {
		return null;
	}

	/**
	 * Should be used only by {@link #abclEval(String, Map<String,LispObject>,
	 * AgentUI, Object...)} allow Lisp operator access to thread-local data
	 * through {@link #getUIForThread()}.
	 */
	private static final ThreadLocal<AgentUI> uiForThread = new ThreadLocal<AgentUI>();

	/**
	 * Used by Lisp operators defined as subclasses of {@link CasaLispOperator} to
	 * access thread-local data. The thread-local values are set by {@link
	 * #abclEval(String, Map<String,LispObject>, AgentUI, Object...)}, so all
	 * these calls must be called (directly or indirectly) though that method.
	 * 
	 * @return
	 */
	public static AgentUI getUIForThread() {
		return uiForThread.get();
	}

	/**
	 * Should be used only by {@link #abclEval(String, Map<String,LispObject>,
	 * AgentUI, Object...)} to allow Lisp operator access to thread-local data
	 * through {@link #getUIForThread()}.
	 */
	private static final ThreadLocal<TransientAgent> agentForThread = new ThreadLocal<TransientAgent>();

	/**
	 * Used by Lisp operators defined as subclasses of {@link CasaLispOperator} to
	 * access thread-local data. The thread-local values are set by {@link
	 * #abclEval(String, Map<String,LispObject>, AgentUI, Object...)}, so all
	 * these calls must be called (directly or indirectly) though that method.
	 * 
	 * @return
	 */
	public static TransientAgent getAgentForThread() {
		return agentForThread.get();
	}

	// /**
	// * Should be used only by {@link #abclEval(String, AgentUI, Object...)}
	// allow Lisp operator
	// * access to thread-local data through {@link #getUIForThread()}.
	// */
	// private static final ThreadLocal<MLMessage> msgForThread = new
	// ThreadLocal<MLMessage> ();

	/**
	 * Used by Lisp operators defined as subclasses of {@link CasaLispOperator} to
	 * access thread-local data. The thread-local values are set by {@link
	 * #abclEval(String, Map<String,LispObject>, AgentUI, Object...)}, so all
	 * these calls must be called (directly or indirectly) though that method.
	 * 
	 * @return
	 */
	@Deprecated
	public static MLMessage getMsgForThread() {
		// Event event = getEventForThread();
		Stack<LispObject> stack = casa.abcl.Lisp.envForThread.get().get("EVENT");
		if (stack == null || stack.isEmpty())
			return null;
		Event event = (Event) (((JavaObject) (stack.peek())).getObject());
		if (event == null || !(event instanceof MessageEvent))
			return null;
		MessageEvent mevent = (MessageEvent) event;
		return mevent.getMessage();
	}

	/**
	 * Used by Lisp operators defined as subclasses of {@link CasaLispOperator} to
	 * access thread-local data. The thread-local values are set by {@link
	 * #abclEval(String, Map<String,LispObject>, AgentUI, Object...)}, so all
	 * these calls must be called (directly or indirectly) though that method.
	 * 
	 * @return
	 */
	public static Event getEventForThread() {
		Map<String, Object> map = getObjectsForThread();
		Object obj = map.get("event");
		if (obj == null || !(obj instanceof Event))
			return null;
		return (Event) obj;
	}

	/**
	 * Should be used only by {@link #abclEval(String, Map<String,LispObject>,
	 * AgentUI, Object...)} allow Lisp operator access to thread-local data
	 * through {@link #getUIForThread()}.
	 */
	private static final ThreadLocal<Map<String, Object>> objectsForThread = new ThreadLocal<Map<String, Object>>();

	/**
	 * Used by Lisp operators defined as subclasses of {@link CasaLispOperator} to
	 * access thread-local data. The thread-local values are set by {@link
	 * #abclEval(String, Map<String,LispObject>, AgentUI, Object...)}, so all
	 * these calls must be called (directly or indirectly) though that method.
	 * 
	 * @return
	 */
	public static Map<String, Object> getObjectsForThread() {
		return objectsForThread.get();
	}

	static {
		createCasaLispOperators(TransientAgent.class);
		casa.abcl.Lisp.loadClass("casa.platform.Generic");
	}

	/**
	 * Scan self for {@link casa.LispAccessible} methods. All subclasses of
	 * {@link TransientAgent} must statically call this method to create
	 * operators.
	 * 
	 * @param aClass
	 *          the class to scan
	 */
	public static void createCasaLispOperators(
			Class<? extends TransientAgent> aClass) {
		for (Method method : aClass.getMethods()) {
			try {
				CasaLispOperator.makeCasaLispOperator(aClass, method);
			} catch (Throwable e) {
				Trace.log("error", 
						"TransientAgent.createCasaLispOperators(" + aClass.toString()
								+ ") [method=" + method.getName() + "]", e);
			}
		}
	}

	/**
	 * Lisp operator: (AGENT__GET-URL)<br>
	 * Returns the agent's url as a string
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__GET_URL = new CasaLispOperator(
			"AGENT.GET-URL",
			"\"!Returns the agent's URL as a string.\" "
					+ "&KEY "
					+ "OBJECT \"@java.lang.Boolean\" \"!Indicates that the URL should be returned as a URLDescriptor object. (optional)\" ",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			if (params.containsKey("OBJECT")
					&& params.getJavaObject("OBJECT") instanceof Boolean
					&& ((Boolean) params.getJavaObject("OBJECT")).booleanValue()) {
				return new StatusObject<URLDescriptor>(0, agent.getURL());
			} else {
				String url = agent.getURL().toString();
				// ui.println(url);
				return new StatusObject<LispObject>(0, new SimpleString(url));
			}
		}
	};

	/**
	 * Lisp operator: (AGENT.GET-POLICIES)<br>
	 * Returns the agent's policies formated as a string
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__GET_POLICIES = new CasaLispOperator(
			"AGENT.GET-POLICIES",
			"\"!Returns the agent's policies as a string.\" "
					+ "&KEY ID \"@java.lang.Integer\" \"!retrieve only the policy with this ID.\" "
					+ "(VERBOSE T) \"@java.lang.Boolean\" \"!set to Nil to get only the names of the policies.\" ",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			StringBuilder buf = new StringBuilder();
			int id = -1;
			if (params.containsKey("ID")) {
				id = (Integer) params.getJavaObject("ID");
			}
			boolean verbose = (Boolean) params.getJavaObject("VERBOSE");
			int count = 0;
			buf.append("; POLICY CONTAINTER ").append(agent.policies.getType())
					.append(":\n");
			for (AbstractPolicy p : agent.policies) {
				if (id == -1 || p.getID() == id) {
					buf.append(verbose ? p.toString() : p.getName()).append("\n  ");
					count++;
				}
			}
			StringBuilder buf2 = new StringBuilder();
			if (agent.policiesAlwaysApply != null) {
				for (AbstractPolicy p : agent.policiesAlwaysApply) {
					if (id == -1 || p.getID() == id) {
						buf2.append(verbose ? p.toString() : p.getName()).append("\n  ");
						count++;
					}
				}
				if (buf2.length() > 0) {
					buf.append("; ALWAYS-APPLY POLICIES ")
							.append(agent.policiesAlwaysApply.getType()).append(":\n")
							.append(buf2);
				}
			}
			buf2 = new StringBuilder();
			if (agent.policiesLastResort != null) {
				for (AbstractPolicy p : agent.policiesLastResort) {
					if (id == -1 || p.getID() == id) {
						buf2.append(verbose ? p.toString() : p.getName()).append("\n  ");
						count++;
					}
				}
				if (buf2.length() > 0) {
					buf.append("; LAST-RESORT POLICIES ")
							.append(agent.policiesLastResort.getType()).append(":\n")
							.append(buf2);
				}
			}
			String ret = buf.toString();
			String msg = "Found " + count + " policies.";
			ui.println(msg);
			return new StatusObject<String>(0, msg, ret);
		}
	};

	/**
	 * Lisp operator: (AGENT.DELETE-POLICY)<br>
	 * Deletes a policy if it has one.
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__DELETE_POLICY = new CasaLispOperator(
			"AGENT.DELETE-POLICY",
			"\"!Returns the agent's policies as a string.\" "
					+ "NAME \"@java.lang.String\" \"!The name of the policy to be deleted.\"",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			String name = (String) params.getJavaObject("NAME");
			int count = 0;
			int nameLength = name.length();
			top: for (AbstractPolicy p : agent.policies) {
				String thatName = p.getName();
				try {
					if (thatName.charAt(nameLength) == '-') {
						for (int i = thatName.length() - 1; i > nameLength; i--) {
							char c = thatName.charAt(i);
							if (!(c >= '0' && c <= '9')) {
								continue top;
							}
						}
					} else {
						continue top;
					}
				} catch (Exception e) {
					continue top;
				}
				if (name.equalsIgnoreCase(p.getName().substring(0, nameLength))) {
					agent.policies.remove(p);
					agent.println("policies", "Policy " + name
							+ " deleted by (AGENT.DELETE_POLICY ...)");
					count++;
				}
			}
			if (count == 0)
				agent.println("warning", "(AGENT.DELETE_POLICY " + name
						+ ") deleted 0 policies.");
			return new Status(count == 0 ? 0 : 1, "Deleted " + count + " policies.");
		}
	};

	/**
	 * Lisp operator: (GET-URL)<br>
	 * Returns the agent's url as a string
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__GET_AGENT = new CasaLispOperator(
			"AGENT.GET-AGENT", "\"!Returns the agent as a java object.\" ",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			return new StatusObject<JavaObject>(0, new JavaObject(agent));
		}
	};

	/**
	 * Lisp operator: (GET-THREAD)<br>
	 * Returns the agent's url as a string
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator GET_THREAD = new CasaLispOperator(
			"GET-THREAD", "\"!Returns the current tread name as a string.\" ",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			return new StatusObject<LispObject>(0, new SimpleString(Thread
					.currentThread().getName()));
		}
	};

	// /**
	// * Lisp operator: (GET_ONTOLOGY)<br>
	// * Returns the agent's ontology as a string. This string is executable lisp
	// code.
	// */
	// @SuppressWarnings("unused")
	// private static final CasaLispOperator GET_ONTOLOGY =
	// new CasaLispOperator("GET-ONTOLOGY",
	// "\"!Returns the casa agent's ontology as a string.\" ",
	// TransientAgent.class)
	// {
	// @Override
	// public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
	// Environment env) {
	// String ont = agent.ontology.toString();
	// //ui.print(ont+EOF);
	// return new StatusObject<String>(0,"success",ont);
	// }
	// };

	/**
	 * Lisp operator: (GET-SYSTEM {property-name})<br>
	 * Either return the string value of a system property, or display all system
	 * properties (returning NIL).<br>
	 * Lambda List: &OPTIONAL (PROPERTY "all")<br>
	 * PROPERTY [optional def=all] string name of the system property to show (or
	 * 'all' to display and return NIL)
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator GET_SYSTEM = new CasaLispOperator(
			"GET-SYSTEM",
			"\"!Either return the string value of a system property, or display all system properties (returning NIL).\" "
					+ "&OPTIONAL (PROPERTY \"all\") \"!string name of the system property to show (or 'all' to display and return NIL)\"",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			int ret = 0;
			String key = (String) params.getJavaObject("PROPERTY");
			if ("all".equalsIgnoreCase(key)) {
				StringBuilder buf = new StringBuilder();
				Properties temp = System.getProperties();
				TreeSet<Object> keys = new TreeSet<Object>(temp.keySet()); // This has
																																	 // the side
																																	 // effect of
																																	 // sorting
																																	 // the list
				for (Object k : keys) {
					buf.append('\n').append((String) k).append(" = ").append(temp.get(k));
				}
				buf.append('\n').append(org.armedbear.lisp.Lisp.EOF);
				ui.print(buf.toString());
				return new Status(0);
			} else {
				String val = System.getProperty(key, "no such system property: " + key);
				// make a valant attempt to recover from a case-typo...
				if (val != null
						&& "no such".equals(val.substring(0, Math.min(7, val.length())))) {
					val = System.getProperty(key.toLowerCase(),
							"no such system property: " + key);
					if (val != null
							&& "no such".equals(val.substring(0, Math.min(7, val.length()))))
						return new Status(-1, val);
					else
						ret = 1;
				}
				return new StatusObject<String>(ret, val, val);
			}
		}
	};

	/**
	 * Lisp operator: (SET-SYSTEM {property-name})<br>
	 * Sets the string value of a system property\.<br>
	 * Lambda List: &OPTIONAL (PROPERTY "all")<br>
	 * PROPERTY [optional def=all] string name of the system property to show (or
	 * 'all' to display and return NIL)
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator SET_SYSTEM = new CasaLispOperator(
			"SET-SYSTEM", "\"!Set the string value of a system property.\" "
					+ "PROPERTY \"!string name of the system property to change\" "
					+ "VALUE \"!the new value of the property\"", TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			int ret = 0;
			String retText = "";
			String key = (String) params.getJavaObject("PROPERTY");
			String value = (String) params.getJavaObject("VALUE");
			String oldValue = System.getProperty(key, "[empty]");
			if (value != null
					&& "[empty]".equals(value.substring(0, Math.min(7, value.length())))) {
				String otherValue = System.getProperty(key.toLowerCase(), "[empty]");
				if (otherValue != null
						&& "[empty]".equals(otherValue.substring(0,
								Math.min(7, otherValue.length())))) {
					retText = "\nNo such property \"" + key
							+ "\" exits -- do you mean existant property \""
							+ key.toLowerCase() + "\"?\n";
					ui.print(retText);
					ret = 1; // warning
				}
			}
			retText = retText + "\"" + key + "\" changed from \""
					+ System.setProperty(key, value) + "\" to \""
					+ System.getProperty(key) + "\"";
			ui.println(retText);
			return new Status(ret, retText);
		}
	};

	/**
	 * Lisp operator: (Find {property-name})<br>
	 * Sets the string value of a system property\.<br>
	 * Lambda List: &OPTIONAL (PROPERTY "all")<br>
	 * PROPERTY [optional def=all] string name of the system property to show (or
	 * 'all' to display and return NIL)
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__FIND_FILE_RESOURCE_PATH = new CasaLispOperator(
			"AGENT.FIND-FILE-RESOURCE-PATH",
			"\"!Searchs for and returns a filename in system-specific directories.\" "
					+ "&OPTIONAL FILENAMEONLY \"!string name file to find\"",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			String fileNameOnly = (String) params.getJavaObject("FILENAMEONLY");
			if (fileNameOnly == null) {
				for (String path : agent.getDefFileSystemLocations()) {
					ui.println(path);
				}
				return new Status(0);
			} else {
				try {
					String ret = agent.findFileResourcePath(fileNameOnly);
					return ret == null ? new Status(1) : new StatusObject<String>(0,
							"success", ret);
				} catch (IOException e) {
					return new Status(-1, agent.println("error",
							"Unexpected exception in finding resource file \"" + fileNameOnly
									+ "\"", e));
				}
			}
		}
	};

	/**
	 * Lisp operator: (Find {property-name})<br>
	 * Sets the string value of a system property\.<br>
	 * Lambda List: &OPTIONAL (PROPERTY "all")<br>
	 * PROPERTY [optional def=all] string name of the system property to show (or
	 * 'all' to display and return NIL)
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__RESET_DEF_FILE_SYSTEM_LOCATIONS = new CasaLispOperator(
			"AGENT.RESET-DEF-FILE-SYSTEM-LOCATIONS",
			"\"!resets the default file sysetm locations from system properties casa.home and user.home.\"",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			agent.resetDefFileSystemLocations();
			return new Status(0);
		}
	};

	/**
	 * Lisp operator: (GET-AGENTS-RUNNING)<br>
	 * Sends a message to the LAC requesting the running agents. The output will
	 * be printed on agents log (see .release_getAgentsRunning()).
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__GET_AGENTS_RUNNING = new CasaLispOperator(
			"AGENT.GET-AGENTS-RUNNING",
			"\"!Sends a message to the LAC requesting the running agents. The output will be printed on agents log (see .release_getAgentsRunning()).\" "
					+ "&OPTIONAL (PROPERTY \"all\") \"!string name of the system property to show (or 'all' to display and return NIL)\"",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			return agent.sendMessage(ML.REQUEST, ML.GET_AGENTS_RUNNING,
					agent.getLACURL());// URLDescriptor.make(9000));
		}
	};

	/**
	 * Lisp operator: (GET-AGENTS-REGISTERED)<br>
	 * Sends a message to the LAC requesting the registered agents. The output
	 * will be printed on agents log (see .release_getAgentsRunning()).
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__GET_AGENTS_REGISTERED = new CasaLispOperator(
			"AGENT.GET-AGENTS-REGISTERED",
			"\"!Sends a message to the LAC requesting the registered agents. The output will be printed on agents log (see .release_getAgentsRunning()).\" "
					+ "&OPTIONAL (PROPERTY \"all\") \"!string name of the system property to show (or 'all' to display and return NIL)\"",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			return agent.sendMessage(ML.REQUEST, ML.GET_AGENTS_REGISTERED,
					agent.getLACURL());// URLDescriptor.make(9000));
		}
	};

	public static String optionsToString(String prefix, Object obj) { // note: the
																																		// obj might
																																		// be null,
																																		// so we
																																		// need to
																																		// pass in
																																		// the class
																																		// separately
		StringBuilder buf = new StringBuilder();
		for (Field f : AnnotationUtil.getAnnotatedFields(obj.getClass(),
				CasaOption.class)) {
			try {
				if (f.getAnnotation(CasaOption.class).recurse()) {
					if (f.get(obj) != null) {
						buf.append(optionsToString((prefix == null ? "" : (prefix + "."))
								+ f.getName(), f.get(obj)));
					}
				} else {
					buf.append((prefix == null ? "" : (prefix + ".")) + f.getName())
							.append(": ").append(f.get(obj)).append(" (").append(f.getType())
							.append(")\n");
				}
			} catch (IllegalArgumentException e) {
				buf.append("***Illegal argument exception***\n");
			} catch (IllegalAccessException e) {
				buf.append("***Illegal access exception***\n");
			}
		}
		return buf.toString();
	}

	/**
	 * Attempts to get the field by first trying to access it through a getter of
	 * the form "get"+name(value) (or "is"+name(value) if the type of the field is
	 * boolean) where the name is the same name as the field, but the first letter
	 * is capitolized. If the getter is not found or fails, this method attempts
	 * to get the field value directly. If that fails, an exception is thrown.
	 * 
	 * @param object
	 *          The object containing the field (and value)
	 * @param field
	 *          The field that is assumed to be defined in the object
	 * @return the value of the field, but this could have been accessed through
	 *         the getter method if it exists
	 * @throws IllegalArgumentException
	 *           This actually should never be thrown
	 * @throws IllegalAccessException
	 *           If the field in object is unaccessable by the agent (having
	 *           failed the setter method)
	 */
	public static Object getField(Object object, Field field)
			throws IllegalArgumentException, IllegalAccessException {
		Class<?> clazz = object.getClass();
		String fName = field.getName();
		String getterPrefix = clazz == Boolean.class ? "is" : "get";
		String getterName = getterPrefix + (Character.toUpperCase(fName.charAt(0)))
				+ (fName.length() > 1 ? fName.substring(1) : "");
		Object result;
		try {
			Method method = clazz.getMethod(getterName, (Class<?>[]) null);
			if (method == null)
				field.get(object);
			result = method.invoke(object, (Object[]) null);
		} catch (Exception e) {
			result = field.get(object);
		}
		return result;
	}

	/**
	 * Attempts to set the field by first trying to access it through a setter of
	 * the form "set"+name(value) where the name is the same name as the field,
	 * but the first letter is capitolized. The argument type is the same as the
	 * type of value. If the setter is not found or fails, this method attempts to
	 * set the field value directly. If that fails, an exception is thrown. If the
	 * method succeeds, it calls {@link #getField(Object, Field)} to return a
	 * value.
	 * 
	 * @param object
	 *          The object containing the field (and value)
	 * @param field
	 *          The field that is assumed to be defined in the object
	 * @param value
	 *          The value to be assigned to the field
	 * @return the value of the field as assigned, but could be access through a
	 *         getter method as per {@link #getField(Object, Field)}
	 * @throws IllegalArgumentException
	 *           If the value type cannot be applied to the field
	 * @throws IllegalAccessException
	 *           If the field in object is unaccessable by the agent (having
	 *           failed the setter method)
	 */
	public static Object setField(Object object, Field field, Object value)
			throws IllegalArgumentException, IllegalAccessException {
		Class<?> clazz = object.getClass();
		String fName = field.getName();
		String setterName = "set" + (Character.toUpperCase(fName.charAt(0)))
				+ (fName.length() > 1 ? fName.substring(1) : "");
		// Object result;
		try {
			Method method = clazz.getMethod(setterName,
					new Class<?>[] { value.getClass() });
			if (method == null)
				field.get(object);
			method.invoke(object, new Object[] { value });
		} catch (Exception e) {
			field.set(object, value);
		}
		return getField(object, field);
	}

	/**
	 * Lisp operator: (OPTIONS ...)<br>
	 * Show or set options. No keys lists all options. A key with no value returns
	 * it's value. A key with a value sets the value.
	 * 
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__OPTIONS = new CasaLispOperator(
			"AGENT.OPTIONS",
			"\"!Show or set options. No keys lists all options. A key with no value returns it's value.  A key with a value sets the value.\""
					+ " &KEY &ALLOW-OTHER-KEYS", TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) throws ControlTransfer {
			Object ret = org.armedbear.lisp.Lisp.NIL;
			try {
				String key = null;
				for (String k : params.keySet()) {
					if (!(k.length() > 1 && k.startsWith("__"))) {
						if (key != null) {
							throw new ControlTransfer("Only one key parameter allowed") {
								private static final long serialVersionUID = -8380885115313083429L;

								@Override
								public LispObject getCondition() {
									return new Condition("Only one key parameter allowed");
								}
							};
						}
						key = k;
					}
				}
				if (key == null) {
					ui.print(TransientAgent.optionsToString(null, agent));
				} else {
					Pair<Object, Field> field = casa.util.AnnotationUtil
							.getAnnotatedField(agent, CasaOption.class, key);
					if (field == null)
						return new Status(-89, "(options ...): can't find field " + key);
					Object val = params.getJavaObject(key);
					if (!params.isDefaulted(key)) {
						ret = setField(field.getFirst(), field.getSecond(), val); // uses
																																			// setters
																																			// if
																																			// available
					} else {
						ret = getField(field.getFirst(), field.getSecond()); // uses getters
																																 // if available
					}
				}
			} catch (Throwable e) {
				return new StatusObject<Object>(-1, e.getMessage(),
						new org.armedbear.lisp.LispError(e.toString()));
			}

			if (ret == null)
				ret = org.armedbear.lisp.Lisp.NIL;
			else if (ret instanceof String)
				ret = new SimpleString((String) ret);
			else if (ret instanceof Integer)
				ret = org.armedbear.lisp.LispInteger.getInstance(((Integer) ret)
						.intValue());
			else if (ret instanceof Long)
				ret = org.armedbear.lisp.LispInteger.getInstance(((Long) ret)
						.longValue());
			else if (ret instanceof Boolean)
				ret = ((Boolean) ret).booleanValue() ? org.armedbear.lisp.Lisp.T
						: org.armedbear.lisp.Lisp.NIL;
			else if (ret == org.armedbear.lisp.Lisp.NIL)
				;
			else
				ret = new org.armedbear.lisp.JavaObject(ret);

			return new StatusObject<Object>(0, ret);
		}
	};

	/**
	 */
	@SuppressWarnings("unused")
	private static final NewAgentLispCommand AGENT__NEW_AGENT = new NewAgentLispCommand(
			"AGENT.NEW-AGENT");

	/**
	 * Class to implement the (OPTIONS ...) lisp command.
	 * 
	 * @see OPTIONS
	 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
	 * 
	 */
	protected static class NewAgentLispCommand extends CasaLispOperator {
		public NewAgentLispCommand(String name) {
			super(name, getCommandSpec(), TransientAgent.class, new Object() { }.getClass().getEnclosingClass());
		}

		public static String getCommandSpec() {
			return "\"!Command that creates an agent\" "
					+ "TYPE \"!The type (Java class) of agent to be created\" "
					+ "NAME \"!Name of the agent\" "
					+ "&OPTIONAL "
					+ "(PORT 0) \"@java.lang.Integer\" \"!Port of the agent; 0: will choose; -ve: indicates to 'hunt' (more than 0 proxies will affect the actual port number)\" "
					+ "&KEY "
					+ "(PROCESS \"CURRENT\") \"!Specifies the process in which to run the agent. LAC, CURRENT, or INDEPENDENT.\" "
					+ "(LACPORT 9000) \"@java.lang.Integer\" \"!Specifies the port of the LAC the agent registers with (-1 indicates not to register)\" "
					+ "SHORTCUTTING \"!Set shortcutting in message protocols on or off\" "
					+ "(ACK NIL) \"@java.lang.Boolean\" \"!Turn acknowledge (ack) requirement on or off\" "
					// +
					// "(MARKUP \"KQML\") \"!Specify the markuplanguage for inter-agent messages (effects the entire process globally). KQML or XML\" "
					+ "(PERSISTENT T) \"@java.lang.Boolean\" \"!Turn persistent saving of agent data on or off (default to on)\" "
					+ "(ROOT \"/casa/\") \"!Root directory for the casa files (LAC only)\" "
					+ "(TRACE 0) \"@java.lang.Integer\" \"!Turn set the trace flags.  Bits are (1=off,2=on,4=monitor,8=toFile)\" "
					// +
					// "TRACEFILE \"@java.lang.Boolean\" \"!Turn file tracing on or off (only effective if trace=true)\" "
					// +
					// "TRACEWINDOW \"@java.lang.Boolean\" \"!Turn trace window on or off (only effective if trace=true)\" "
					+ "(TRACETAGS \"error\") \"!A list of trace tags(identifiers) to add|remove.  Remove a tag by preceding it with a'-'.  Current valid tags:calls,msg,msgHandling,warning,info,policies,commitments.\" "
					+ "INTERFACE \"!A fully-qualified java class name of the interface for the agent to use.  Defaults to an appropriate window interface. The special [name] 'text' yeilds a default text interface; 'none' specifies the agent should have no interface.\" "
					+ "GUARD \"@java.lang.Boolean\" \"!Turn the guard (secure) on or off for the agent\" "
					+ "PROXIES \"@java.lang.String\" \"!Add proxies to the agent (a semi-colon separated list of fully-qualified class names)\" "
					+ "(STRATEGY \"sc3\") \"!Choose a strategy. sc, reactive, BDI, or sc3.\" "
					+ "(SECURITY \"none\") \"!Choose security package. Currently, 'casa.security' or 'none'\" "
					+ "(ONTOLOGYENGINE \"casa.ontology.owl2.OWLOntology\") \"!The Java class for the ontology engine (currently, either casa.ontology.v3.CASAOntology or casa.util.TypeHierarchy or casa.ontology.owl2.OWLOntology)\" "
					+ "ONTOLOGYFILE \"!The file from which the ontology engine should read inialization data\" "
					+ "ONTOLOGYFILTER \"@java.lang.String\" \"!The ontology filter\" "
					+ "NOWAIT \"!Don't wait for the agent to start before returning (it doesn't matter what the value is, if :nowait is present it doesn't wait)\" "
					+ "FIPA-URLS \"@java.lang.Boolean\" \"!true: (agent-idenifier :name \\\"fred\\\" ...); false: casa://10.0.1.12/casa/TransientAgent/fred:5400.\" "
					+ "&ALLOW-OTHER-KEYS";
		}

		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) throws ControlTransfer {
			Throwable reason = null;
			String process = (String) params.getJavaObject("PROCESS");
			String name = (String) params.getJavaObject("NAME");
			String Filter = (String) params.getJavaObject("ONTOLOGYFILTER");
			//***CURRENT or THIS or PROCESS 
			if (process == null || process.equalsIgnoreCase("CURRENT") || process.equalsIgnoreCase("THIS") || process.equalsIgnoreCase("PROCESS")) {
				return makeAgentInThisProc(params, ui);
			//***INDEPENDENT or NEW
			} else if (process.equalsIgnoreCase("INDEPENDENT") || process.equalsIgnoreCase("NEW")) {
				return makeAgentInNewProc(agent, params);
			//***LAC
			} else if (process.equalsIgnoreCase("LAC")) {
				// choose the lac to execute this agent in by: 1) the lacPort specified
				// for the agent; 2) this agent's LAC; 3) default to 9000
				int lacPort = params.containsKey("lacPort") ? (Integer) params
						.getJavaObject("lacPort") : agent.getURL().getLACport();
				if (lacPort == 0)
					lacPort = 9000;
				try {
					URLDescriptor url = URLDescriptor.make(lacPort);
					return makeAgentInAnotherProc(agent, params, ui, env, url);
				} catch (URLDescriptorException e) {
					reason = e;
				}
			} else {
				//***PORT
				try {
					int port = Integer.parseInt(process);
					try {
						URLDescriptor url = URLDescriptor.make(port);
						return makeAgentInAnotherProc(agent, params, ui, env, url);
					} catch (URLDescriptorException e) {
						reason = e;
					}
				} catch (NumberFormatException e) {
					//***URLDESCRIPTOR
					try {
						URLDescriptor url = URLDescriptor.make(process);
						return makeAgentInAnotherProc(agent, params, ui, env, url);
					} catch (URLDescriptorException e1) {
						reason = e1;
					}
				}
			}
			return new Status(-44, "Bad PROCESS argument: " 
					+ agent.println("error", process
							+ ". Must be one of 'current' (or 'this' or 'process'), 'independent' (or 'new'), 'LAC', a valid port number, or a valid URLDescriptor.",
								reason));
		}

		private Status makeAgentInAnotherProc(TransientAgent agent, ParamsMap params,
				AgentUI ui, Environment env, URLDescriptor url) {
			params.put("PROCESS", new Pair<Object, LispObject>("CURRENT",
					new SimpleString("CURRENT")), false);
			StringBuilder command = new StringBuilder();
			command.append("(agent.new-agent \"")
					.append(params.getJavaObject("TYPE").toString()).append("\" \"")
					.append(params.getJavaObject("NAME").toString()).append("\" :PORT ")
					.append(params.getJavaObject("PORT").toString())
					.append(" :PROCESS \"CURRENT\"").append(" :NOWAIT T");

			// TODO: Does it matter that the new-agent lisp command will say to open
			// the old ontology.lisp file here? dsb
			for (String key : params.keySet()) {
				if (!key.equalsIgnoreCase("TYPE") && !key.equalsIgnoreCase("NAME")
						&& !key.equalsIgnoreCase("PORT")
						&& !key.equalsIgnoreCase("PROCESS")) {
					command.append(" :").append(key).append(' ');
					Object val = params.getJavaObject(key);
					if (val != null) {
						if (val instanceof String)
							command.append('"').append((String) val).append('"');
						else
							command.append(val.toString());
					}
				}
			}
			command.append(")");

			String commandString = String.format("(agent.tell \"%s\" \"%s\")",
					url.toString(),
					CASAUtil.escape(command.toString(), "\""));
			return agent.abclEval(commandString, null, ui);
		}

		private Status makeAgentInNewProc(final TransientAgent agent,
				ParamsMap params) {
			ParamsMap newParams = new ParamsMap(params);
			newParams.put("PROCESS", "CURRENT", new SimpleString("CURRENT"), false);
			List<String> cmdList = new Vector<String>(newParams.size() + 6);
			cmdList.add("java");
			cmdList.add("-classpath");
			cmdList.add(System.getProperty("java.class.path")
					+ JarLoader.getLoadedJarsAsString());
			cmdList.add("CASA");
			cmdList.add("(agent.new-agent");
			cmdList.add("\"" + newParams.getJavaObject("TYPE").toString() + "\"");
			cmdList.add("\"" + newParams.getJavaObject("NAME").toString() + "\" :PORT ");
			cmdList.add(newParams.getJavaObject("PORT").toString());
			// cmdList.add(":NOWAIT");
			cmdList.add(":KILLONFAILURE");
			cmdList.add("T");
			for (String key : newParams.keySet()) {
				if (!key.equalsIgnoreCase("TYPE") && !key.equalsIgnoreCase("NAME")
						&& !key.equalsIgnoreCase("PORT")) {
					cmdList.add(":" + key);
					Object val = newParams.getJavaObject(key);
					if (val != null) {
						if (val instanceof String)
							cmdList.add("\"" + ((String) val) + "\"");
						else
							cmdList.add(newParams.getJavaObject(key).toString());
					}
				}
			}
			cmdList.add(")");

			// make a string copy of cmdList for tracing purposes
			StringBuffer cmd = new StringBuffer();
			for (String p : cmdList) {
				cmd.append(p).append(' ');
			}

			Process proc = null;
			try {
				// proc = Runtime.getRuntime().exec(cmd);
				ProcessBuilder pb = new ProcessBuilder(casa.platform.Generic.prepareCmdList(cmdList));
				pb.redirectErrorStream(true);
				proc = pb.start();
			} catch (IOException ex) {
				// DEBUG.PRINT("Cannot spawn new process: " + ex.toString());
				String ret = readFromProc(proc);
				return new Status(-1, "Cannot spawn new process command line:\n   "
						+ cmd + "\n\nOutput:\n" + ret + "\n" + ex);
			}
			for (int i = 4; i > 0; i--) { // wait around a little while to see if the
				// process returns failure...
				try {
					String out = readFromProc(proc);
					int stat = proc.exitValue(); // will throw if the process is still
					// running
					if (stat != 0) { // assuming a successful exit is OK
						return new Status(-2, "Process returned status "
								+ Integer.toString(stat) + ":\n    " + cmd + "\n\nOutput:\n"
								+ out + "\n");
					}
					i = 0;
					break;
				} catch (IllegalThreadStateException ex2) { // process is still running
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
					}
				}
			}

			// Fire up a new thread to monitor the process for 25 seconds to see if it
			// exits in that time; report if it does
			final Process proc2 = proc;
			final String cmd2 = cmd.toString();
			// new Thread(params.getJavaObject("name")+"_monitor") { @Override public
			// void run() {
			if (agent!=null) {
				agent.makeSubthread(new Runnable() {
					@Override
					public void run() {
						int stat = -9999;
						// wait arond for 25 seconds to see if the process keeps running
						String out = "";
						for (int i = 4; i > 0; i--) {
							out += readFromProc(proc2);
							try {
								stat = proc2.exitValue(); // will throw if the process is still
								// running
								break;
							} catch (IllegalThreadStateException ex) {
								synchronized (this) {
									try {
										wait(5000);
									} catch (InterruptedException e) {
									}
								}
							}
						}
						if (stat != -9999)
							agent.println(null, "Spawned process exited with status " + stat + "\n  "
									+ cmd2 + "\n\nOutput:\n" + out);
					}
				}).start();
			}

			return new Status(0, "Agent '" + params.getJavaObject("name")
					+ "' spawned in process '" + proc + "':\n   " + cmd);
		}

		private String readFromProc(Process proc) {
			StringBuilder sb = new StringBuilder();
			InputStream s = proc.getInputStream();
			int len;
			try {
				while ((len = s.available()) > 0) {
					byte[] buf = new byte[len];
					s.read(buf);
					sb.append(buf);
					// DEBUG.PRINT(new String(buf)+"\n");
				}
			} catch (IOException e) {
			}
			return sb.toString();
		}

		private Status makeAgentInThisProc(ParamsMap params, AgentUI ui) {
			String val;
			// Attempt to start the agent.
			TransientAgentInterface agent = null;
			String className = (String) params.getJavaObject("TYPE");
			try {
				Class<? extends TransientAgent> agentClass = Class.forName(className)
						.asSubclass(TransientAgent.class);
				// Class<?>[] agentNestedClasses;
				// Class<? extends TransientAgent> currentClass = agentClass;
				Constructor<? extends TransientAgent> agentConstructor = agentClass
						.getConstructor(new Class[] { ParamsMap.class, AgentUI.class });
				agent = agentConstructor.newInstance(params, ui);
			} catch (InvocationTargetException e) {
				String msg = "CASAcommandLine.runAgent: Exception when invoking new class '"
						+ className + "':" + e.toString() + ":\n" + e.getCause();
				Trace.log("error", msg, e);
				if (e.getCause() != null) {
					return new Status(-50, "", e.getCause());
				} else {
					return new Status(-51, "", e);
				}
			} catch (Throwable e) {
				String msg = "CASAcommandLine.runAgent: Cannot invoke new class '"
						+ className + "'";
				CASAUtil.log("error", msg, e, true);
				return new Status(-52, "", e);
			}

			if (agent instanceof AbstractProcess) {
				if (agent.hasOpenPort()) {
					AbstractProcess t = (AbstractProcess) agent;
					interrupted(); // clear the interrupt status
					t.start();
					if (!params.containsKey("NOWAIT")) {
						final long timeout = 15000;
						long timeoutTime = System.currentTimeMillis() + timeout;
						synchronized (agent) {
							t.waitingForAgentToStart = true;
							while (System.currentTimeMillis() < timeoutTime
									&& !t.isInitialized()) {
								try {
									t.wait(3000); // wait for the agent to start if we did not get
																// the signal while starting the UI --
																// hopefully, it should interrupt us before the
																// time is up...
								} catch (InterruptedException e) {
								}
							}
							t.waitingForAgentToStart = false;
						}
						if (t.isInitialized()) {
							if (ui!=null)
								ui.println("**********************Agent " + agent.getName()
									+ " successfully started in "
									+ (System.currentTimeMillis() - (timeoutTime - timeout))
									+ "ms.");
						}
						else {
							if (ui!=null)
								ui.println("***********************Agent " + agent.getName()
									+ " has not signalled that it's reading its event queue in "
									+ (System.currentTimeMillis() - (timeoutTime - timeout))
									+ " ms.");
							if (params.containsKey("KILLONFAILURE")) {
								System.out
										.println("***********************Subprocess terminated due to "
												+ agent.getName() + " startup failure.");
								Runtime.getRuntime().exit(-5);
							}
						}
					}
				} else {
					String warning = "CASAcommandLine.runAgent: agent "
							+ agent.getAgentName() + " failed to initialize listener";
					Trace.log("error", warning);
					return new Status(+53, warning);
				}
			} else {
				Trace
						.log(
								"error",
								"CASAcommandLine.runAgent: specified className did not return a TransientAgent subtype");
				throw new ClassCastException();
			}
			return new StatusObject<TransientAgentInterface>(0, agent);

		}

	}
	
	/**
	 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
	 */
	private class UIStarter implements Runnable {
		/**
		 */
		TransientAgentInterface agent;

		String agentInterfaceName;

		public UIStarter(TransientAgentInterface agent, String agentInterfaceName) {
			super();
			this.agent = agent;
			this.agentInterfaceName = agentInterfaceName;
		}

		@Override
		public void run() {
			assert java.awt.EventQueue.isDispatchThread();
			int waitcount = 0;
			long startTime = System.currentTimeMillis();


			// Build the interface for the agent, either from the command
			// line specification according to the agent's default
			boolean noInterface = agentInterfaceName != null
					&& agentInterfaceName.equalsIgnoreCase("none");
			if (agent != null && !noInterface) {
				// wait until the agent is actually initialized -- otherwise, we get
				// null pointer exceptions
				final int waitTime = 15000;
				long till = System.currentTimeMillis() + waitTime;
				while (!agent.isInitialized() && System.currentTimeMillis() < till) {
					try {
						sleep(200);
					} catch (InterruptedException e) {
					}
				}
				if (!agent.isInitialized()) {
					Trace.log("error", "Agent " + agent.getName() + " not initialized in "
							+ (waitTime / 1000) + " seconds: UI aborted.");
					return;
				}

				// now we can go ahead and initialize the interface
				if (!ProcessInfo.daemon) {
					boolean GUI = true;
					AgentUI agentUI = null;
					if (agentInterfaceName != null) { // interface specified on
						// command line...
						if (agentInterfaceName.equalsIgnoreCase("text"))
							GUI = false;
						else {
							agentUI = ((TransientAgent) agent).createNewInterface(agent,
									agentInterfaceName);
						}
					}

					// attempt the default interface if no interface specified
					// on the command line or if the specified interface failed.
					if (agentUI == null) {
						try {
							agentUI = agent.makeDefaultInterface(/* args */null, GUI);
						} catch (Exception ex3) {
							Trace.log("error", 
									"Failed to start up default interface for agent", ex3);
						}
					}
					if (agentUI != null) {
						agent.putUI(agentUI);
					}

				}
			}
		}

	}

	private void startPrimaryInterface() {
		String agentInterfaceName = (String) initParams.getJavaObject("INTERFACE");
		UIStarter uiStarter = new UIStarter(this, agentInterfaceName);
		AbstractInternalFrame.runInEventDispatchThread(uiStarter);
	}

	/**
	 * Lisp operator: (TELL agent-url ...)<br>
	 * Sends a REQUEST/EXECUTE message to the given agent-url.
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__TELL = new CasaLispOperator(
			"AGENT.TELL",
			"\"!Sends a REQUEST/EXECUTE message with the specified command.\" "
					+ "AGENT \"@java.lang.String\" \"!The URL of the agent to execute the command.\" "
					+ "COMMAND \"@java.lang.String\" \"!The command to execute.\"",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			try {
				URLDescriptor url = URLDescriptor.make(
						(String) params.getJavaObject("AGENT")); // parse the URL of the
																										 // agent we are sending to
				String content = (String) params.getJavaObject("COMMAND"); // parse the
																																	 // message
																																	 // CONTENT
				Status ret = agent.sendMessage(ML.REQUEST, ML.EXECUTE, url, ML.CONTENT,
						content); // send the message
				if (ret.getStatusValue() != 0) { // if the returned sent status is not 0
					ui.println("(AGENT.TELL " + url + " " + content
							+ ") returned non-zero status: " + ret.getStatusValue() + ", "
							+ ret.getExplanation()); // print error message
					if (ret instanceof StatusObject) { // if ret is indeed a StatusObject
						Object obj = ((StatusObject<?>) ret).getObject();
						ui.println(obj==null?"null":obj.toString()); // print description
					}
				}
				return ret; // return the result of the send
			} catch (URLDescriptorException e) {
				return new Status(-6, "Bad URL: " + e.toString()); // indicate that a
																													 // bad URL was given
			}
		}
	};

	/**
	 * Lisp operator: (AGENT.ASYNC command)<br>
	 * Sends a EXECUTE message to agent-name.
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__ASYNC = new CasaLispOperator(
			"AGENT.ASYNC",
			"\"!Executes command in a separate thread.\" "
					+ "COMMAND \"!The command to execute as either a String or a quoted Cons.\"",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(final TransientAgent agent, ParamsMap params,
				AgentUI ui, Environment env) {
			Object obj = params.getJavaObject("COMMAND");
			final String command;
			if (obj instanceof Cons)
				command = ((Cons) obj).writeToString();
			else if (obj instanceof String)
				command = (String) obj;
			else
				throw new LispException(
						toString()
								+ ": parameter COMMAND must be either a legal lisp command as either a String or a Cons object.");

			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					agent.abclEval(command, null);
				}
			};
			agent.makeSubthread(runnable).start();
			return new Status(0);
		}
	};

	/**
	 * Lisp operator: (AGENT.REPLY MLMessage {PerformDescriptor} :[{*}key] [value]
	 * ...)
	 * <p>
	 * 
	 * A reply message is sent in response to the {@link MLMessage} in the first
	 * argument. This reply is constructured as follows:
	 * <ol>
	 * <li>The first-argument {@link MLMessage} has
	 * {@link MLMessage#constructReplyTo(MLMessage, String, URLDescriptor)}
	 * applied to it to produce a legitimate {@link ML#AGREE}-type reply message.
	 * <li>If second-argument {@link PerformDescriptor} exists, its key/value
	 * pairs are overlayed the key/value pairs in the reply message (replacing or
	 * adding to them).
	 * <li>For all the keys and values in this command,
	 * <ol>
	 * <li>If the key is prefixed by a star (eg: ":*PERFORMATIVE agree") then it
	 * will only fill the key in PD if there is no such key in PD.
	 * <li>Otherwise, for the :PERFORMATIVE and :ACT keys (only): if the value in
	 * the key subsumes the value in PD then the PD key is left; otherwise the PD
	 * key is replaced and a warning is issued to the agent's log.
	 * <li>Otherwise the key value replaces the value in PD.
	 * </ol>
	 * </ol>
	 * 
	 * Sends the designated response to the sender of the TO-MESSAGE, returning
	 * the result of the send.
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__REPLY = new CasaLispOperator(
			"AGENT.REPLY",
			"\"!Sends a message to another agent constructed from the argument message, with fields being replaced from the PerformDescriptor and any other keys/value pairs given.\" "
					+ "TO-MESSAGE \"@casa.MLMessage\" \"!The message to which the reply is being made\" "
					+ "&OPTIONAL PERFORM-DESCRIPTOR \"!Normally either the result of a call to the agent's 'consider' method or a legal reply performative string or NIL (taken as 'agree'.\" "
					+ "&ALLOW-OTHER-KEYS ", TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(final TransientAgent agent, final ParamsMap params,
				final AgentUI ui, final Environment env) {

			MLMessage inMsg = (MLMessage) params.getJavaObject("TO-MESSAGE");
			Object temp = params.getJavaObject("PERFORM-DESCRIPTOR");
			String performative = null;
			PerformDescriptor pd = null;
			if (temp != null) {
				if ((temp instanceof Boolean) && ((Boolean) temp) == false) {
				} 
				else if (((temp instanceof String) && agent.isA(((String) temp),
						ML.PERFORMATIVE))) {
					performative = (String) temp;
				} 
				else if (temp instanceof PerformDescriptor) {
					pd = (PerformDescriptor) temp;
				} 
				else
					throw new LispException(
							"AGENT.REPLY: parameter PERFORM-DESCRIPTOR must be either a CASA PerformDescriptor or null (got '"
									+ temp + "')");
			}

			// build a default reply message
			MLMessage reply = MLMessage.constructReplyTo(inMsg,
					agent.getUniqueRequestID(), agent.getURL());

			// The above constructReplyTo always makes it an AGREE performative -- we
			// should change this to a REFUSE if
			// the status is negative, but we want the default to override if it's
			// there (this will be overridden
			// by either the PERFORM-DESCRIPTOR parameter or the key parameters.
			if (!params.containsKey("*PERFORMATIVE")) {
				if (performative != null)
					reply.setParameter(ML.PERFORMATIVE, performative);
				else
					reply.setParameter(ML.PERFORMATIVE,
							(pd == null || pd.getStatusValue() >= 0) ? ML.AGREE : ML.REFUSE);
			}

			// Set the key/value pairs from the first parameter PerformDescriptor
			if (pd != null) {

				// handle a request by the agent (though the PD status) for DROP_ACTION
				// or DEFER_ACTION
				int status = pd.getStatusValue();
				if (status == DROP_ACTION) {
					return new Status(
							DROP_ACTION,
							"(agent.reply...): Action dropped due to the PerformDescriptor status value (persumably at the request of the agent)");
				}
				if (status == DEFER_ACTION) {
					final CasaLispOperator This = this;
					agent.defer(new Runnable() {
						@Override
						public void run() {
							This.execute(agent, params, ui, env);
						}
					}, 1000);
					return new Status(
							DEFER_ACTION,
							"(agent.reply...): Action deferred due to the PerformDescriptor status value (persumably at the request of the agent)");
				}
			}

			// replace keys from the command line CONDITIONALLY
			ParamsMap pm = new ParamsMap(params);
			pm.remove("PD");
			pm.remove("TO-MESSAGE");
			pm.remove("PERFORM-DESCRIPTOR");
			PerformDescriptor overlay = new PerformDescriptor(pm);
			if (pd==null) {
				reply.setParameters(overlay);
			}
			else {
				PerformDescriptor overlayed = overlay.overlay(pd, agent);
				reply.setParameters(overlayed);
			}

//			/*
//			 * With the propose/discharge reply specifically, the inMsg from which
//			 * the reply is being constructed is actually the server's initial
//			 * agree/request. As such, any reply made to the server's outgoing
//			 * agree/request message results in a reply directed toward itself. This
//			 * is appropriate when conversations are internalized, but not when two
//			 * agents are engaged in conversation... the reply must be directed
//			 * toward the other agent. The following tests the inMsg to see if this
//			 * conversation is indeed internalized, and if not, it sets the
//			 * appropriate receiver - dsb TODO The anticident of the following IF
//			 * should never be true for received messages, so the setting of the
//			 * receiver in the reply should be unreachable... -rck
//			 */
//			try {
//				if (!agent.getURL().equals(inMsg.getReceiver())) {
//					reply.setParameter(ML.RECEIVER, inMsg.getReceiver().toString());
//				}
//			} catch (URLDescriptorException e) {
//				agent.println("error", "Unexpected exception", e);
//			}

			if (reply.getParameter(ML.PERFORMATIVE) == null) {
				throw new LispException(
						"AGENT.REPLY: Failed to set the PERFORMATIVE field of the constructed reply message:/n"
								+ reply.toString(true));
			}
			return agent.sendMessage(reply);
		}
	};

	/**
	 * Lisp operator: (SEND agent-url ...)<br>
	 * Sends a EXECUTE message to agent-name.
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__SEND = new CasaLispOperator(
			"AGENT.SEND",
			"\"!Sends a message to another agent.\" "
					+ "MESSAGE \"@casa.MLMessage\" \"!The message to send\" "
					+ "&KEY "
					+ "PROXY \"!Send the message through the indicated proxy (optional)\" ",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			MLMessage msg = (MLMessage) params.getJavaObject("MESSAGE");

			if (params.containsKey("PROXY")) {
				String proxy = params.getJavaObject("PROXY").toString();
				URLDescriptor proxyURL = null;
				try {
					proxyURL = URLDescriptor.make(proxy);
				} catch (URLDescriptorException e) {
					return new Status(-6, "Bad Proxy URL: " + e.toString());
				}
				try {
					msg = MLMessage.constructProxyMessage(msg, msg.getFrom(), proxyURL,
							msg.getParameter(ML.RECEIVER));
				} catch (URLDescriptorException e) {
					return new Status(-6, "Bad URL: " + e.toString());
				}
			}
			return agent.sendMessage(msg);
		}
	};

	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__MESSAGE = new CasaLispOperator(
			"AGENT.MESSAGE",
			"\"!Create a new message object.\" "
					+ "PERFORMATIVE \"!Any performative subtype of 'inform' or 'request'\" "
					+ "ACT \"!An act name\" "
					+ "RECEIVER \"!The URL of the destination agent\" "
					+ "&KEY "
					+ "CONTENT \"!The content part of the message (optional)\" "
					+ "LANGUAGE \"!The language in which the content field is written (optional)\" "
					+ "LANGUAGE-VERSION \"!The version of the language in which the content field is written (optional)\" "
					+ "REPLY-BY \"@java.lang.Integer\" \"!Milliseconds to wait before giving up on a reply (optional) Default=options.timeout\" "
					+ "&ALLOW-OTHER-KEYS ", TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			MLMessage msg = MLMessage.getNewMLMessage();
			msg.setParameter(ML.FROM, agent.getURL().toString());
			msg.setParameter(ML.RECEIVER, agent.getURL().toString());
			Set<String> keys = params.keySet();
			if (keys == null)
				return new Status(-3, "no parameters");
			for (String key : keys) {
				if (key.length() > 1 && key.charAt(0) == '_' && key.charAt(1) == '_')
					continue;
				Object param = params.getJavaObject(key);
				if (param != null) {
					String val = param.toString();
					//if the ACT is empty or null, that means we should just leave the ACT out altogether.
					if (key.equals("ACT") && (param==null || ((String)param).length()==0)) {
						continue;
					}
					if (key.equals("REPLY-BY")) {
						long now = System.currentTimeMillis();
						try {
							val = String.valueOf(now + Long.parseLong(val));
						} catch (NumberFormatException ex) {
						}
					}

					else if (key.toUpperCase().equals("PD")) {
						// Object obj = CASAUtil.unserialize(val);
						Object obj = params.getJavaObject(key.toUpperCase());
						if (obj instanceof PerformDescriptor) {
							PerformDescriptor pd = (PerformDescriptor) obj;
							// Some major (probably incorrect) assumptions follow - dsb
							Status status = pd.getStatus();
							if (status instanceof StatusObject<?>)
								msg.setParameter(ML.CONTENT,
										CASAUtil.serialize(((StatusObject<?>) status).getObject()));
							else
								msg.setParameter(ML.CONTENT, CASAUtil.serialize(status));
							for (String pdKey : pd.keySet()) {
								// Give precendence to user-set message parameters for now - dsb
								if (!keys.contains(pdKey)) {
									if (pd.get(pdKey) instanceof String)
										msg.setParameter(pdKey, pd.get(pdKey));
									else
										msg.setParameter(pdKey, CASAUtil.serialize(pd.get(pdKey)));
								}
							}
						}
					}
					msg.setParameter(key.toLowerCase(), val);
				}
			}
			// msg
			return new StatusObject<MLMessage>(0, msg);
		}
	};

	/**
	 * Lisp operator: (AGENT.JOIN CD-url)<br>
	 * Attempt to join the cooperation domain specified by the parameter URL.
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__JOIN = new CasaLispOperator(
			"AGENT.JOIN",
			"\"!Attempt to join the cooperation domain specified by the parameter URL.\" "
					+ "CD-URL \"!The URL of the cooperation domain to join.\" ",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			try {
				return agent.doJoinCD(URLDescriptor.make((String) params
						.getJavaObject("CD-URL")));
			} catch (URLDescriptorException e) {
				return new Status(-6, "Bad URL: " + e.toString());
			}
		}
	};

	/**
	 * Lisp operator: (GETUSEACKPROTOCOL)<br>
	 * Return T if the agent is using the ACK parotocol, otherwise return Nil.
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__GETUSEACKPROTOCOL = new CasaLispOperator(
			"AGENT.GETUSEACKPROTOCOL",
			"\"!Return T if the agent is using the ACK parotocol, otherwise return Nil.\" ",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			return new StatusObject<Boolean>(0, agent.getUseAckProtocol());
		}
	};

	/**
	 * Lisp operator: (EVENT.GET-MSG-OBJ)<br>
	 * Return the message object
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__GET_MSG_OBJ = new CasaLispOperator(
			"EVENT.GET-MSG-OBJ",
			"\"!Return the value of the specified message tag, Nil if the tag is missing and throws an exception if the MSG isn't in scope.\" ",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) throws ControlTransfer {
			Event event = (Event) casa.abcl.Lisp.lookupAsJavaObject(env, "event");
			if (event == null || !(event instanceof MessageEvent))
				throw new LispException(
						"Lisp Opertator EVEMT.GET-MSG-OBJ: no message event in scope");
			MessageEvent mevent = (MessageEvent) event;
			MLMessage msg = mevent.getMessage();

			if (msg == null)
				throw new LispException(
						"Lisp Opertator EVENT.GET-MSG-OBJ: no message in scope");

			return new StatusObject<Object>(0, msg);
		}
	};

	/**
	 * Lisp operator: (EVENT.GET-MSG)<br>
	 * Return the value.
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator EVENT__GET_MSG = new CasaLispOperator(
			"EVENT.GET-MSG",
			"\"!Return the value of the specified message tag, Nil if the tag is missing and throws an exception if the MSG isn't in scope.\" "
					+ "&OPTIONAL TAG \"@java.lang.String\" \"!The tag for which to return the value.\" "
					+ "&KEY UNSERIALIZE \"!If included, an attempt to unserialize the value will be made.\" ",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) throws ControlTransfer {
			// MLMessage msg = getMsgForThread();
			Event event = (Event) casa.abcl.Lisp.lookupAsJavaObject(env, "event");
			if (event == null || !(event instanceof MessageEvent))
				// throw new
				// LispException("Lisp Opertator EVENT.GET-MSG: no message event in scope");
				return new StatusObject<MLMessage>(-1,
						"Lisp Opertator EVENT.GET-MSG: no message event in scope", null);
			MessageEvent mevent = (MessageEvent) event;
			MLMessage msg = mevent.getMessage();

			if (msg == null)
				// throw new
				// LispException("Lisp Opertator EVENT.GET-MSG: no message in scope");
				return new StatusObject<MLMessage>(-2,
						"Lisp Opertator EVENT.GET-MSG: no message in scope", null);

			if (params.containsKey("TAG")) { // we are returning a value from INSIDE
																			 // the message
				String tag = ((String) params.getJavaObject("TAG")).toLowerCase();
				Object value = msg.getParameter(tag);
				if (params.containsKey("UNSERIALIZE")
						&& params.getLispObject("UNSERIALIZE") != org.armedbear.lisp.Lisp.NIL
						&& value instanceof String) {
					try {
						value = CASAUtil.unserialize((String) value, null);
					} catch (ParseException e) {
						// Do nothing -- assume this isn't a serialized field after all
					}
				}
				if (value instanceof String)
					return new StatusObject<SimpleString>(0, new SimpleString(
							(String) value));
				return new StatusObject<Object>(0, value);
			} else { // we are returning the message itself
				return new StatusObject<MLMessage>(0, msg);
			}
		}
	};

	/**
	 * Lisp operator: (EVENT.GET)<br>
	 * Return the event object.
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator EVENT__GET = new CasaLispOperator(
			"EVENT.GET", "\"!Return the Event object.\" ", TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) throws ControlTransfer {
			Event event = (Event) casa.abcl.Lisp.lookupAsJavaObject(env, "event");
			if (event == null)
				return new StatusObject<MLMessage>(-1,
						"Lisp Opertator EVENT.GET: no event in scope", null);
			return new StatusObject<Event>(0, event);
		}
	};

	/**
	 * Lisp operator: (EVENT.GET-OWNER-CONVERSATION-ID)<br>
	 * Return the event's owner conversation ID, if it has one
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator EVENT__GET_OWNER_CONVERSATION_ID = new CasaLispOperator(
			"EVENT.GET-OWNER-CONVERSATION-ID",
			"\"!Return the Event's owner conversation ID if it has one.\" ",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) throws ControlTransfer {
			AbstractEvent event = (AbstractEvent) casa.abcl.Lisp.lookupAsJavaObject(
					env, "event");
			if (event == null)
				return new StatusObject<MLMessage>(-1,
						"Lisp Opertator EVENT.GET: no event in scope", null);
			return new StatusObject<String>(0, event.getOwnerConversationID(),
					event.getOwnerConversationID());
		}
	};

	/**
	 * Lisp operator: (GET-OBJECT NAME)<br>
	 * Return the object associated with the NAME parameter, if there isn't such
	 * an object, throw an exception.
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private static final CasaLispOperator GET_OBJECT = new CasaLispOperator(
			"GET-OBJECT",
			"\"!(DEPRECATED) Return the value of the specified message tag, Nil if the tag is missing and throws an exception if the MSG isn't in scope.\" "
					+ "NAME \"@java.lang.String\" \"!The key the object to return.\" ",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) throws ControlTransfer {
			Map<String, Object> map = getObjectsForThread();
			String name = ((String) params.getJavaObject("NAME")).toLowerCase();
			if (map == null)
				throw new LispException(
						"Lisp Opertator GET-OBJECT: no objects in scope");
			Object value = map.get(name);
			if (value == null)
				throw new LispException("Lisp Opertator GET-OBJECT: no object named \""
						+ name + "\" in scope");
			if (value instanceof String)
				return new StatusObject<SimpleString>(0, new SimpleString(
						(String) value));
			return new StatusObject<Object>(0, value);
		}
	};

	/**
	 * Lisp operator: (PAUSE)<br>
	 * Pause the agent just after processing a message.
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__PRINTLN = new CasaLispOperator(
			"AGENT.PRINTLN",
			"\"!Log the message to the agent's reporting mechanism.\" "
					+ "TAG \"!The tag for conditional logging. 'error' or NIL will always print.\" "
					+ "&REST MESSAGE \"!Objects to be converted to type String, concatonated, and used as the log message.\" ",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			Object obj = params.getJavaObject("TAG");
			String tag = (obj instanceof String) ? (String) obj : null;
			Cons rest = (Cons) params.getLispObject("MESSAGE");
			StringBuilder b = new StringBuilder();
			for (; rest != null && rest != org.armedbear.lisp.Lisp.NIL; rest = (rest.cdr instanceof Cons ? (Cons) rest.cdr
					: null)) {
				if (rest.car instanceof JavaObject)
					b.append(((JavaObject) rest.car).getObject().toString());
				else {
					b.append(rest.car.toString());
				}
			}
			return new StatusObject<String>(0, null, agent.println(tag, b.toString()));
		}
	};

	/**
	 * Lisp operator: (PAUSE)<br>
	 * Pause the agent just after processing a message.
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__PAUSE = new CasaLispOperator(
			"AGENT.PAUSE", "\"!Pause the agent just after processing a message.\" ",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			agent.setPause(true); /* ui.print(Character.toString(EOF)); */
			return new Status(0, "pausing...");
		}
	};

	/**
	 * Lisp operator: (RESUME)<br>
	 * Resume the agent from a pause state.
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__RESUME = new CasaLispOperator(
			"AGENT.RESUME", "\"!Resume the agent from a pause state.\" ",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			agent.setPause(false); /* ui.print(Character.toString(EOF)); */
			return new Status(0, "resuming...");
		}
	};

	/**
	 * Lisp operator: (STEP)<br>
	 * Resume the agent from a pause state until just after processing the next
	 * message.
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__STEP = new CasaLispOperator(
			"AGENT.STEP",
			"\"!Resume the agent from a pause state until just after processing the next message.\" ",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			agent.step(); /* ui.print(Character.toString(EOF)); */
			return new Status(0, "stepping...");
		}
	};

	// /**
	// * Lisp operator: (SHOW-CONVERSATIONS)<br>
	// * Show the agent's current conversations.
	// */
	// @SuppressWarnings("unused")
	// private static final CasaLispOperator AGENT__SHOW_CONVERSATIONS =
	// new CasaLispOperator("AGENT.SHOW-CONVERSATIONS",
	// "\"!Show the agent's current conversations.\" ", TransientAgent.class,
	// "SC")
	// {
	// @Override public Status execute (TransientAgent agent, ParamsMap params,
	// AgentUI ui, Environment env){
	// String convs = agent.conversations.toString();
	// //ui.print(convs);
	//
	// //Conversations that may currently be executing
	// for (String c : agent.conversations.keySet()){
	// //
	// ui.print("----RUNNING----------------------------------------------------");
	// ui.print("\n" + c + ":  " + agent.conversations.get(c).toString());
	//
	// for (ConversationInterface ci :
	// agent.conversations.get(c).getConversationlets()){
	// casa.conversation.ConversationInterface.State state = ci.getState();
	// casa.conversation.ConversationInterface.State prevState =
	// ci.getPreviousState();
	// ui.print(//"    " + ci.getRole() +
	// "\n    " + state +
	// " <- " + prevState +
	// " :   " + ci.toString());
	// }
	// }
	//
	// // //Archived conversations
	// // for (String c : agent.archivedConversations.keySet()){
	// //
	// ui.print("----ARCHIVED---------------------------------------------------");
	// // ui.print(c + "\t" + agent.archivedConversations.get(c).toString());
	// //
	// // for (ConversationInterface ci :
	// agent.archivedConversations.get(c).getConversationlets()){
	// // ui.print(//"    " + ci.getRole() +
	// // "    " + ci.getState() +
	// // " <- " + ci.getPreviousState() +
	// // "    " + ci.toString());
	// // }
	// // }
	//
	// return new Status(0 ,convs);
	// }
	// };

	/**
	 * Lisp operator: (SHOW-SUPPORTED-CONVERSATIONS)<br>
	 * Show the conversations the agent supports.
	 */
	// @Deprecated
	// @SuppressWarnings("unused")
	// private static final CasaLispOperator SHOW_SUPPORTED_CONVERSATIONS =
	// new CasaLispOperator("SHOW-SUPPORTED-CONVERSATIONS",
	// "\"!(DEPRECATED) Show the conversations the agent supports.\" "
	// , TransientAgent.class, "SSC")
	// {
	// @Override public Status execute (TransientAgent agent, ParamsMap params,
	// AgentUI ui, Environment env){
	// String convs = agent.supportedConversations.toString();
	//
	// //Conversations that may currently be executing
	// for (String c : agent.supportedConversations.keySet()){
	// ui.print("\n" + c + "\n");// + ":  " +
	// agent.supportedConversations.get(c).toString());
	//
	// for (BoundConversationlet b : agent.supportedConversations.get(c)){
	// ui.print("  " + b.getConversationlet().toString() + "\n");
	// }
	// }
	// return new Status(0 ,convs);
	// }
	// };

	/**
	 * Lisp operator: (NEW-INTERFACE)<br>
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__NEW_INTERFACE = new CasaLispOperator(
			"AGENT.NEW-INTERFACE",
			"\"!Adds a new interface to this agent.\" "
					+ "CLASSNAME \"@java.lang.String\" \"!The fully-qualified java class name of the new interface.\" ",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			AgentUI u = agent.createNewInterface(agent,
					(String) params.getJavaObject("CLASSNAME"));
			if (u != null)
				agent.putUI(u);
			return new Status(u == null ? -1 : 0);
		}
	};

	/**
	 * Lisp operator: (SHOW-EVENTQUEUE)<br>
	 * List the current event queue.
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__SHOW_EVENTQUEUE = new CasaLispOperator(
			"AGENT.SHOW-EVENTQUEUE", "\"!List the current event queue.\" "
			// +"&KEY ALL \"@java.lang.Boolean\" \"!List active events not on the queue\""
			, TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			StringBuilder s = new StringBuilder();
			// if (params.containsKey("ALL")) {
			// Set<AbstractEvent> events = AbstractEvent.getEvents(agent);
			// for (AbstractEvent e: events) {
			// s.append(e.toString()).append('\n');
			// }
			// }
			// else {
			String p = agent.getEventQueue();
			s.append(p);
			// }
			ui.println(s.toString());
			return new Status(0, s.toString());
		}
	};

	/**
	 * Lisp operator: (EXIT)<br>
	 * List the current event queue.
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__EXIT = new CasaLispOperator(
			"AGENT.EXIT",
			"\"!Request this agent to exit.  Note that this is only a request: the agent may refuse to exit or delay exiting.  For example, it may finish up pending requests before exiting..\" ",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			agent.exit();
			// ui.print(Character.toString(EOF));
			return new Status(0, "Agent exiting");
		}
	};

//	/**
//	 * Lisp operator: (AGENT.SOCKETS)<br>
//	 * List the current event queue.
//	 */
//	@SuppressWarnings("unused")
//	private static final CasaLispOperator AGENT__SOCKETS = new CasaLispOperator(
//			"AGENT.SOCKETS",
//			"\"!List the current sockets the agent has going.\" ",
//			TransientAgent.class) {
//		@Override
//		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
//				Environment env) {
//			ui.println(agent.getSockets().toString());
//			return new Status(0);
//		}
//	};
//
	// /**
	// * Lisp operator: has-conversation-in-state<br>
	// *
	// * Tests to see if the specified conversation exists.
	// *
	// * @author <a href=mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
	// */
	//
	// @SuppressWarnings("unused")
	// @Deprecated
	// private static final CasaLispOperator HAS_CONVERSATION_IN_STATE =
	// new CasaLispOperator("HAS-CONVERSATION-IN-STATE",
	// "\"!(DEPRECATED) Tests to see if the specified conversation exists.\""
	// + "CONV_ID \"@java.lang.String\" \"!Unique numerical string identifier.\""
	// +
	// "ROLE \"@java.lang.String\" \"!The agent's role in this conversation (e.g. client, server...).\""
	// +
	// "CURRENT_STATE \"@java.lang.String\" \"!The current state of the conversation.\"",
	// TransientAgent.class)
	// {
	// @Override
	// public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
	// Environment env) {
	//
	// //If this is a reply, then make sure there is a matching request
	// String convID = ((String)params.getJavaObject("CONV_ID"));
	// if (agent.hasConversation(convID)){
	// return new StatusObject<Boolean>(0, true);
	// }
	// //Historically, the numerical error returned was -658. A false return
	// value, however,
	// //does not mean failed execution (thus no negative return value).
	// return new StatusObject<Boolean>(0, false);
	// }
	// };

	/**
	 * Lisp operator: make-conversation-id<br>
	 * 
	 * Returns a unique String conversation identifier
	 * 
	 * @author <a href=mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
	 */

	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__MAKE_CONVERSATION_ID = new CasaLispOperator(
			"AGENT.MAKE-CONVERSATION-ID",
			"\"!Returns a new, unique conversation ID string.\"",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			return new StatusObject<LispObject>(0, new SimpleString(
					agent.getUniqueRequestID()));
		}
	};

	/**
	 * Lisp operator: (COMPARETO)<br>
	 * List the current event queue.
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator COMPARETO = new CasaLispOperator(
			"COMPARETO",
			"\"!Calls the java compareto() operator on the 1st parameter with the second parameter as an argument.\" "
					+ "p1 p2", TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@SuppressWarnings("unchecked")
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			Object p1 = params.getJavaObject("p1");
			Object p2 = params.getJavaObject("p2");
			if (p1 instanceof Comparable)
				return new StatusObject<Integer>(0, ((Comparable) p1).compareTo(p2));
			return new Status(-1, "p1 is not a Comparable");
		}
	};

	// /**
	// * Lisp operator: start-conversation<br>
	// *
	// * Add a CompositeConversation object to the agent's list of conversations.
	// *
	// * @author <a href=mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
	// */
	//
	// @SuppressWarnings("unused")
	// private static final CasaLispOperator AGENT__START_CONVERSATION =
	// new CasaLispOperator("AGENT.START-CONVERSATION",
	// "\"!Adds a Conversation object to the agent's conversation TreeSet.\""
	// + "ACT \"@java.lang.String\" \"!String description of the act.\""
	// + "CONV_ID \"@java.lang.String\" \"!Unique numerical string identifier.\""
	// , TransientAgent.class)
	// {
	// @Override
	// public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
	// Environment env) {
	// String act = ((String)params.getJavaObject("ACT"));
	// String convID = ((String)params.getJavaObject("CONV_ID"));
	//
	// //This function gets called with every petition and inform, so make sure
	// the
	// //conversation hasn't already started.
	// if (!agent.hasConversation2(convID)){
	//
	// //Is the conversation for the act supported?
	// if(agent.hasConversationSupported(act)){
	//
	// // System.out.println("-1-" + agent.getAgentName() + " supports " + act );
	//
	// Vector<Class<? extends CompositeConversation>> conversations =
	// agent.getConversationSupported(act);
	//
	// //Instantiate Conversations
	// for (Class<? extends CompositeConversation> conv : conversations){
	// CompositeConversation c;
	// try {
	// c = (CompositeConversation)conv.getConstructor(
	// TransientAgent.class, String.class, Environment.class).newInstance(agent,
	// convID, env);
	// } catch (Throwable e) {
	// return new Status(-1, agent.println("error",
	// "Cannot instantiate conversation", e));
	// }
	//
	// //If there was more than one conversation returned, take the
	// //conversationlets and add them to the agent's existing conversation
	// if (!agent.hasConversation2(convID)){
	// agent.addConversation2(convID, c);
	// } else {
	// CompositeConversation existing = agent.getConversation(convID);
	// for (SocialCommitment sc : c.getConversations().keySet())
	// existing.add(sc, c.getConversations().get(sc));
	// agent.updateConversation(existing);
	// }
	// }
	// return new Status(0, agent.println("conversation", act +
	// " conversation started: " + convID));
	// }
	//
	// //The accompanying 'if' is checking if there is a CompositeConversation
	// //subclass available to direct this conversation. If not, check the
	// //policy-file-initialized TreeMap and create a new composite with the
	// //conversationlets provided
	// else if (agent.supportsConversation(act)){
	// // System.out.println("-2-" + agent.getAgentName() + " supports " + act );
	// CompositeConversation c =
	// new CompositeConversation(
	// agent, convID, env, agent.getSupportedConversation(act));
	// agent.addCompositeConversation(convID, c);
	// return new Status(0, agent.println("conversation", act +
	// " conversation started with new method: " + convID));
	// }
	//
	// //Even though the conversation is not supported, a Conversation object
	// //has to be instantiated to avoid the "unsolicited agree problem".
	// //Once the old internal consideration method is permanently deprecated,
	// //this can be removed.
	// agent.addCompositeConversation(convID, new CompositeConversation(agent,
	// convID, env));
	//
	// return new Status(1, agent.println("warning", act +
	// " conversation is not supported, creating a new, generic conversation"));
	// }
	//
	// return new Status(0, agent.println("warning",
	// "Conversation already started: " + convID));
	// }
	// };

	@Override
	public void update(Observable o, Object arg) {
		if (arg instanceof ObserverNotification) {
			processObserverNotification((ObserverNotification) arg);
		}
	}

	/**
	 * Creates a new add_observer request. This agent is requesting the observee
	 * to inform this agent of certain event.
	 * 
	 * @param observableAgentUrl
	 *          the agent to observe
	 * @param types
	 *          the event types
	 * @return whether the message was sent successfully
	 */
	public Status doAddObserver(URLDescriptor observableAgentUrl, String... types) {
		MLMessage message = getNewMessage(ML.REQUEST, ML.ADD_OBSERVER,
				observableAgentUrl);
		StringBuffer buf = new StringBuffer();
		for (String line : types) {
			buf.append(line);
			buf.append('\n');
		}
		message.setParameter(ML.CONTENT, buf.toString());
		message.setPriority(Event.HIGHEST_PRIORITY);

		return sendMessage(message);
	}

	/**
	 * Lisp operator: (SHOW-CONVERSATIONS)<br>
	 * Show the agent's current conversations.
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__SHOW_CONVERSATIONS = new CasaLispOperator(
			"AGENT.SHOW-CONVERSATIONS",
			"\"!Show the conversations -- supported, current, all, or a specific one.\" "
					+ "&OPTIONAL NAME \"@java.lang.String\" \"!If present shows the named template, otherwise all are shown.\" "
					+ "&KEY "
					// +"SUPPORTED \"!Show the conversations supported by the the current agent; ignores the NAME parameter.\" "
					+ "CURRENT \"!Show the current conversations for the current agent; ignores the NAME parameter.\" "
					+ "(VERBOSE 1) \"@java.lang.Integer\" \"!0=only names; 1=state with no policies; 2=all\" ",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass(), "SCONV") {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			int verbose = (Integer) params.getJavaObject("VERBOSE");
			boolean current = params.containsKey("CURRENT");
			String name = (String) params.getJavaObject("NAME");

			agent.conversations.purge();

			if (current || name == null) {
				Pair<Integer, String> ret = agent.getConversationsReport(current,
						verbose);
				ui.println(ret.getSecond());
				return new StatusObject<Integer>(0, ret.getFirst());
			}

			int count = 0;
			Conversation conv = Conversation.findConversation(name);
			if (conv == null)
				ui.println("No such conversation");
			else {
				if (verbose == 0)
					ui.println(conv.getName()
							+ (conv.getId() == null ? "" : (", id:" + conv.getId()
									+ ", state:" + conv.getState())));
				else
					ui.println(conv.toString(0, verbose == 1));
				count++;
			}
			return new StatusObject<Integer>(0, count);
		}
	};

	/**
	 * Report on this agent's conversations.
	 * 
	 * @param current
	 *          Set to true for the current conversations, false for known
	 *          (template) conversations.
	 * @param verbose
	 *          0=brief, 1=without-policies, 2=full-detail
	 * @return A string containing a multi-line report on the current or known
	 *         conversations.
	 */
	public Pair<Integer, String> getConversationsReport(boolean current,
			int verbose) {
		StringBuilder buf = new StringBuilder();
		int count = 0;
		if (current) {
			ConcurrentSkipListMap<String, LinkedList<Conversation>> convs = getConversations();
			for (String name : convs.keySet()) {
				if (verbose > 0) {
					for (Conversation c : convs.get(name)) {
						buf.append(c.toString(0, verbose == 1)).append('\n');
						count++;
					}
				} else {
					buf.append(name);
					for (Conversation c : convs.get(name)) {
						buf.append("\n  ").append(c.getName()).append(" (")
								.append(c.getState()).append(")");
						count++;
					}
					buf.append("\n");
				}
			}
			if (buf.length() == 0)
				buf.append("No current conversations to display.");
		} else {
			for (Conversation c : Conversation.getKnownConversations()) {
				if (verbose > 0) {
					buf.append(c.toString(0, verbose == 1)).append('\n');
				} else {
					buf.append(c.getName()).append('\n');
				}
				count++;
			}
			if (buf.length() == 0)
				buf.append("No known conversations to display.");
		}
		return new Pair<Integer, String>(count, buf.toString());
	}

	/**
	 * Lisp operator: (SHOW-COMMITMENTS)<br>
	 * Show the agent's current social commitments.
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__SHOW_COMMITMENTS = new CasaLispOperator(
			"AGENT.SHOW-COMMITMENTS",
			"\"!Show the social commitments.\" "
					+ "&KEY "
					+ "CURRENT \"!Show only the commitments that are not filfilled, violated, etc.\" "
					+ "VIOLATED \"!Show only the commitments that are violated.\" ",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass(), "SCOM") {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			int count = 0;
			boolean current = params.containsKey("CURRENT");
			boolean violated = params.containsKey("VIOLATED");

			if (agent.commitmentProcessor == null)
				return new Status(0, "No commitmentProcessor active.");
			Collection<SocialCommitment> commitments = agent.commitmentProcessor
					.getStore().getAllCommitments();
			for (SocialCommitment sc : commitments) {
				if (!current || !sc.flagSet(SocialCommitmentStatusFlags.ENDED)) {
					ui.println(sc.toString());
					count++;
				}
			}

			return new StatusObject<Integer>(0, count);
		}
	};

//	/**
//	 * Lisp operator: (SHOW-SOCKETS)<br>
//	 * Show the agent's current open sockets.
//	 */
//	@SuppressWarnings("unused")
//	private static final CasaLispOperator AGENT__SHOW_SOCKETS = new CasaLispOperator(
//			"AGENT.SHOW-SOCKETS",
//			"\"!Show the current open sockets.\" "
//					+ "&KEY ",
//			TransientAgent.class, "SOCKETS") {
//		@Override
//		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
//				Environment env) {
//			int count = 0;
//			Sockets sockets = agent.getSockets();
//			if (ui==null) {
//				return new StatusObject<String>(0, "Success", sockets.toString());
//			}
//			else {
//				ui.println(sockets.toString());
//			}
//			return new Status(0);
//		}
//	};

	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__INSTANTIATE_CONVERSATION = new CasaLispOperator(
			"AGENT.INSTANTIATE-CONVERSATION",
			"\"!Instantiates a conversation from a template conversation.\" "
					+ "NAME \"@java.lang.String\" \"!The name of the conversation template.\" "
					+ "EVENT \"@casa.event.MessageEvent\" \"!The initial message event from which to instantiate the conversation.\" ",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			String convName = (String) params.getJavaObject("NAME");
			MessageEvent event = (MessageEvent) params.getJavaObject("EVENT");
			String conversationID = event.getMessage().getParameter(ML.CONVERSATION_ID);

			Conversation template = Conversation.findConversation(convName);
			if (template == null)
				throw new LispException("(agent.instantiate-conversation " + convName
						+ " ...): " + convName
						+ " does not resolve to a conversation template.");
			Conversation conv = null;
			try {
				conv = template.instantiate(agent, event, null, null);
			} catch (IllegalOperationException e) {
				throw new LispException("(agent.instantiate-conversation " + convName
						+ " ...): Cannot instantiate conversation.", e);
			}
			if (conv == null) {
				throw new LispException("(agent.instantiate-conversation " + convName
						+ " ...): " + convName
						+ " could not be instantiated (returned null).");
			}
			return new StatusObject<Conversation>(0, conv);
		}
	};

	/**
	 * Lisp operator: (AGENT.ISA)<br>
	 * Add a policy to the agent's global policy repetoire
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__ISA = new CasaLispOperator(
			"AGENT.ISA",
			"\"!Determines if the child act is a descendent of the given ancestor.\" "
					+ "CHILD \"@java.lang.String\" \"!The child act.\" "
					+ "ANCESTOR \"@java.lang.String\" \"!The ancestor act.\" ",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			String child = (String) params.getJavaObject("CHILD");
			String ancestor = (String) params.getJavaObject("ANCESTOR");
			Boolean isA = agent.isA(child, ancestor);
			return new StatusObject<Boolean>(0, isA);
		}
	};

	/**
	 * Lisp operator: (SERIALIZE object)<br>
	 * Return the casa serialization string for the object
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator SERIALIZE = new CasaLispOperator(
			"SERIALIZE",
			"\"!Returns the casa serialization string for the object.\" "
					+ "OBJECT \"!The object to serialize.\" ", TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			Object obj = params.getJavaObject("OBJECT");
			return new StatusObject<String>(0, "", CASAUtil.serialize(obj));
		}
	};

	/**
	 * Lisp operator: (PUT-POLICY)<br>
	 * Add a policy to the agent's global policy repetoire
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__PUT_POLICY = new CasaLispOperator(
			"AGENT.PUT-POLICY",
			"\"!Insert a policy into the agent's global policy repetoire.\" "
					+ "POLICY \"@casa.policy.Policy\" \"!The policy to be added.\" "
					+ "&KEY "
					+ "ALWAYS-APPLY \"@java.lang.Boolean\" \"!Apply this policy to every conversation, if set.\" "
					+ "LAST-RESORT \"@java.lang.Boolean\" \"!Apply this policy only if no other non-last-resort policies are applicable, if set.\" ",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			try {
				Object policy = params.getJavaObject("POLICY");
				Boolean alwaysApply = params.containsKey("ALWAYS-APPLY");
				Boolean lastResort = params.containsKey("LAST-RESORT");
				if (alwaysApply && lastResort)
					return new Status(-2,
							"agent.put-policy: cannot set both ALWAYS-APPLY and LAST-RESORT keys");

				if (policy instanceof AbstractPolicy) {
					if (alwaysApply)
						agent.putPolicyAlwaysApply((AbstractPolicy) policy);
					else if (lastResort)
						agent.putPolicyLastResort((AbstractPolicy) policy);
					else
						agent.putPolicy((AbstractPolicy) policy);
					return new Status(0);
				}
				return new Status(-1,
						"agent.put-policy was expecting a parameter of type Policy, not "
								+ policy.getClass());
			} catch (Exception e) {
				agent.println("error", "(agent.put-policy ...)", e);
				return new Status(-2, "(agent.put-policy ...): ", e);
			}
		}
	};

	/**
	 * Lisp operator: (AGENT.CREATE-EVENT-OBSERVER-EVENT)<br>
	 * Create an EventObserverEvent
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__CREATE_EVENT_OBSERVER_EVENT = new CasaLispOperator(
			"AGENT.CREATE-EVENT-OBSERVER-EVENT",
			"\"!Creates an EventObserverEvent to manage subscriptions.\" "
					+ "EVENT-TYPE \"@java.lang.String\" \"!The type of this event.\" "
					+ "WATCHED-EVENT-TYPES \"@org.armedbear.lisp.LispObject\" \"!The types of events to watch.\" "
					// +"WATCHED-EVENT-TYPE \"@java.lang.String\" \"!The type of the event to watch.\" "
					+ "&KEY CONVERSATION-ID \"@java.lang.String\" \"!An (optional) conversation ID if this event is to be associated with a particular conversation.\" ",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			String convID = (String) params.getJavaObject("CONVERSATION-ID");
			// String watchedEvents =
			// (String)params.getJavaObject("WATCHED-EVENT-TYPE");
			ArrayList<String> watchedEvents = new ArrayList<String>();
			LispObject watchedObj = params.getLispObject("WATCHED-EVENT-TYPES");
			String eventType = (String) params.getJavaObject("EVENT-TYPE");
			if (eventType == null || !agent.isA(eventType, ML.EVENT)) {
				throw new LispException("(agent.create-event-observer-event "
						+ eventType + " ... ): 1st parameter must be a subtype of EVENT.");
			}

			// Turn the watchedObj into a String[]
			if (watchedObj instanceof org.armedbear.lisp.Cons) {
				while (watchedObj != null && watchedObj != org.armedbear.lisp.Lisp.NIL) {
					LispObject event = watchedObj.car();
					watchedEvents.add(event.getStringValue());
					watchedObj = watchedObj.cdr();
				}
			}

			// EventObserverEvent event = new
			// EventObserverEvent((String)params.getJavaObject("EVENT-TYPE"), true,
			// agent, (String)params.getJavaObject("WATCHED-EVENT-TYPE"));
			EventObserverEvent event = new EventObserverEvent(eventType, true, agent,
					(String[]) watchedEvents.toArray(new String[watchedEvents.size()]));
			if (convID != null) {
				try {
					event.setOwnerConversationID(convID);
				} catch (IllegalOperationException e) {
					agent
							.println(
									"error",
									"(agent.create-event-observer-event ...) encountered unexpected exception",
									e);
				}
			}

			event.start();

			// Temporary hack. This function does not necessarily have to be
			// called in response to a subscribe - dsb
			agent.addSubscribeEvents(event);

			return new Status(0);
		}
	};

	/**
	 * Lisp operator: (AGENT.STOP-EVENT-OBSERVER-EVENT)<br>
	 * Stop an existing EventObserverEvent
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__STOP_EVENT_OBSERVER_EVENT = new CasaLispOperator(
			"AGENT.STOP-EVENT-OBSERVER-EVENT",
			"\"!Stop the event with the matching owner conversation ID.\" "
					+ "CONVERSATION-ID \"@java.lang.String\" \"!The event's owner conversation ID.\" ",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			String convID = (String) params.getJavaObject("CONVERSATION-ID"); // get
																																				// the
																																				// id of
																																				// the
																																				// conversation
			AbstractEvent event = (AbstractEvent) agent.getSubscribeEvents(convID); // get
																																							// the
																																							// subscribed
																																							// event
																																							// associated
																																							// with
																																							// this
																																							// conversation
			if (event == null) // if the event is null
				return new Status(-2, "Matching event not found."); // throw error
			if (event instanceof EventObserverEvent) { // if event is of type
																								 // EventObserveEvent
				agent.removeSubscribeEvents(event); // remove the subscribed event from
																						// the transient agent's list of
																						// subscribed events
				((EventObserverEvent) event).cancel(); // cancel the event
				((EventObserverEvent) event).delete(); // delete the event
				return new Status(0); // return successful status
			}
			return new Status(-1, event.toString()
					+ " is not of type EventObserverEvent"); // otherwise throw error
		}
	};

	/**
	 * Lisp operator: (AGENT.SEND-REQUEST-AND-WAIT)<br>
	 * Create an EventObserverEvent
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__SEND_REQUEST_AND_WAIT = new CasaLispOperator(
			"AGENT.SEND-REQUEST-AND-WAIT",
			"\"!Sends a request-type message and waits for the response.\" "
					+ "MESSAGE \"@casa.MLMessage\" \"!The message to send.\" "
					+ "&KEY (TIMEOUT 2000) \"@java.lang.Integer\" \"!The time in milliseconds to wait.\" ",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass(), "REQUESTW") {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			MLMessage msg = (MLMessage) params.getJavaObject("MESSAGE");
			long timeout = (Integer) params.getJavaObject("TIMEOUT");
			return agent.sendRequestAndWait(msg, timeout);
		}
	};

	/**
	 * Lisp operator: (AGENT.SEND-QUERY-AND-WAIT)<br>
	 * Create an EventObserverEvent
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__SEND_QUERY_AND_WAIT = new CasaLispOperator(
			"AGENT.SEND-QUERY-AND-WAIT",
			"\"!Sends a request-type message and waits for the response.\" "
					+ "MESSAGE \"@casa.MLMessage\" \"!The message to send.\" "
					+ "&KEY (TIMEOUT 2000) \"@java.lang.Integer\" \"!The time in milliseconds to wait.\" ",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass(), "QUERYW") {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			MLMessage msg = (MLMessage) params.getJavaObject("MESSAGE");
			long timeout = (Integer) params.getJavaObject("TIMEOUT");
			return agent.sendQueryAndWait(msg, timeout);
		}
	};

	/**
	 * Lisp operator: (AGENT.PING)<br>
	 * Ping another agent given by URL, waiting up to TIMEOUT milliseconds for the
	 * response if it doesn't come sooner.
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__PING = new CasaLispOperator(
			"AGENT.PING",
			"\"!Pings another agent and returns it's URL or NIL if the ping failed.\" "
					+ "URL \"@java.lang.String\" \"!The URL of the agent to ping.\" "
					+ "&KEY (TIMEOUT 2000) \"@java.lang.Integer\" \"!The time in milliseconds to wait.\" ",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass(), "PING") {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			String urlString = (String) params.getJavaObject("URL");
			URLDescriptor url;
			try {
				url = URLDescriptor.make(urlString);
			} catch (URLDescriptorException e) {
				return new Status(-2, "Invalid URL descriptor");
			}
			long timeout = (Integer) params.getJavaObject("TIMEOUT");
			Long startTime = System.currentTimeMillis();
			URLDescriptor retUrl = agent.doPing_sync(url, timeout);
			long delta = System.currentTimeMillis()-startTime;
			if (retUrl == null) {
				if (ui!=null)
					ui.println("Ping timed out in "+(delta)+" ms.");
				return new StatusObject<LispObject>(0, Lisp.NIL);
				}
			else {
				if (ui!=null)
					ui.println("Ping return in "+(delta)+" ms.");
				return new StatusObject<LispInteger>(0, LispInteger.getInstance(delta));
			}
		}
	};

	/**
	 * Lisp operator: (AGENT.LOAD-FILE-RESOURCE)<br>
	 * Ping another agent given by URL, waiting up to TIMEOUT milliseconds for the
	 * response if it doesn't come sooner.
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__LOAD_FILE_RESOURCE = new CasaLispOperator(
			"AGENT.LOAD-FILE-RESOURCE",
			"\"!Locates and loads a file, returning the path it was loaded from or NIL if it failed.\" "
					+ "FILE \"@java.lang.String\" \"!The file name (not the path) to load.\" ",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass(), "LOAD-FILE-RESOURCE") {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			String ret = null;
			try {
				// String file = (String)params.getJavaObject("FILE");
				//
				// //The old DOS-backslash-escape-character problem
				// if (File.separatorChar == '\\'){
				// file = file.replaceAll("\\\\", "\\\\\\\\");
				// file = file.replaceAll("/", "\\\\\\\\");
				// }
				//
				// ret = agent.loadFileResource(file, false);

				ret = agent.loadFileResource((String) params.getJavaObject("FILE"),
						false);
			} catch (Exception e) {
				return new Status(-1, "Error loading file resource from init script "
						+ e);
			}
			return new StatusObject<String>(ret);
		}
	};

	/**
	 * Lisp operator: (AGENT.GET-CLASS-NAME)<br>
	 * Return the class name of the agent.
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__GET_CLASS_NAME = new CasaLispOperator(
			"AGENT.GET-CLASS-NAME",
			"\"!Returns the agent class name as a string.\" ", TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			return new StatusObject<String>(agent.getClass().getName());
		}
	};

	/**
	 * Lisp operator: (AGENT.TRANSFORM)<br>
	 * Returns the transformed object, or the argument argument if no
	 * transformation is applicable
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__TRANSFORM = new CasaLispOperator(
			"AGENT.TRANSFORM",
			"\"!Returns the transformed object, or the argument argument if no transformation is applicable.\" "
					+ " DESCRIBABLE \"@casa.interfaces.Describable\" \"!The object to transform.\"",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			return new StatusObject<Describable>(agent.transform((Describable) params
					.getJavaObject("DESCRIBABLE")));
		}
	};

	/**
	 * Lisp operator: (AGENT.TRANSFORM-STRING)<br>
	 * Returns the transformed string, or the argument string if no transformation
	 * is applicable
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__TRANSFORM_STRING = new CasaLispOperator(
			"AGENT.TRANSFORM-STRING",
			"\"!Returns the transformed string, or the argument string if no transformation is applicable.\" "
					+ " STRING \"@java.lang.String\" \"!The string to transform in id|id|... form.\"",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			return new StatusObject<String>(0, null, agent.transform((String) params
					.getJavaObject("STRING")));
		}
	};

	// ********************************************************************************************************************
	// ********************************************************************************************************************
	// ***************************************************** JADE Stuff
	// ***************************************************
	// ********************************************************************************************************************
	// ********************************************************************************************************************

	// protected ContentManager contentMgr = new ContentManager();
	public CasaKB kBase = null;

	protected void initJADE() {
		try {
			kBase = new CasaKB(getURL().getFile(), this);
			kBase.addFiltersDefinition(new SingleValueDefinition("url"));
			assert_("(= (iota ?x (url ?x)) " + getURL().toString() + ")");
			kBase.addKBAssertFilter(new AllIREFilter());
		} catch (Exception e) {
			assert false;
			println("error", "TransientAgent.initJADE(): ", e);
		}
		try {
			cacheKB = new CasaKB(getURL().getFile(), this);
			cacheKB.addFiltersDefinition(new SingleValueDefinition("url"));
			assert_("(= (iota ?x (url ?x)) " + getURL().toString() + ")");
			cacheKB.addKBAssertFilter(new AllIREFilter());
		} catch (Exception e) {
			assert false;
			println("error", "TransientAgent.initJADE(): ", e);
		}
	}

	public Formula assert_(String formula) throws ParseException {
		try {
			Formula form = SLParser.getParser().parseFormula(formula, true);// SL.formula(formula);
			kBase.assertFormula(form);
			return form;
		} catch (Throwable e) {
			ParseException ex = new ParseException(
					"TransientAgent.assert_(): malformed term '" + formula + "'", 0);
			ex.initCause(e);
			throw ex;
		}
	}

	public Formula assert_(String formula, CasaKB cacheKB) throws ParseException {
		try {
			Formula form = SLParser.getParser().parseFormula(formula, true);// SL.formula(formula);
			cacheKB.assertFormula(form);
			return form;
		} catch (Throwable e) {
			ParseException ex = new ParseException(
					"TransientAgent.assert_(): malformed term '" + formula + "'", 0);
			ex.initCause(e);
			throw ex;
		}
	}

	@SuppressWarnings("unused")
	private static final CasaLispOperator ASSERT = new CasaLispOperator(
			"KB.ASSERT", "\"!Asserts the formula into the KB.\" "
					+ " FORMULA \"@java.lang.String\" \"!The formula.\"",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			String formString = (String) params.getJavaObject("FORMULA");
			try {
				Formula form = agent.assert_(formString);
				// String ret = agent.kBase.toString();
				return new StatusObject<Formula>(0, "", form);
			} catch (ParseException e) {
				return new Status(-1, "(ASSERT \"" + formString
						+ "\"): Bad Parse of expression.", e);
			}
		}
	};

	@SuppressWarnings("unused")
	private static final CasaLispOperator KB_SHOW = new CasaLispOperator(
			"KB.SHOW",
			"\"!Dispalys the formulas from the KB.\" "
					+ "&OPTIONAL PATTERN \"@java.lang.String\" \"!If specified, only expressions containing PATTERN will be displayed.\" "
					+ "&KEY REGEX \"!Set to non-NIL to have PATTERN interpreted as a regular expression occuring in the expression displayed.\" "
					+ "STRICT \"!Set to non-NIL to have PATTERN interpreted as a regular expression that must match the entire expression to be displayed.\" "
					+ "FACTS \"!Set to non-NIL to return only a list of facts in the KB.\" "
					+ "QUERIES \"!Set to non-NIL to return only a list of query filters on the KB.\" "
					+ "ASSERTS \"!Set to non-NIL to return only a list of assert filters on the KB.\" "
					,TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			String pattern = (String) params.getJavaObject("PATTERN");
			boolean regex = params.getJavaObject("REGEX") != null;
			boolean strict = params.getJavaObject("STRICT") != null;
			boolean facts = params.getJavaObject("FACTS") != null;
			boolean queries = params.getJavaObject("QUERIES") != null;
			boolean asserts = params.getJavaObject("ASSERTS") != null;
			int count = 0;
//			count += printKBexpressions(ui, pattern, agent.kBase.toStrings(), regex,
//					strict);
//			TreeMap<String, CasaKB> others = agent.kBase.getOtherAgentKBs();
//			if (others != null) {
//				for (String otherName : others.keySet()) {
//					ui.println("**** Other Agent \"" + otherName + "\":");
//					count += printKBexpressions(ui, pattern, others.get(otherName)
//							.toStrings(), regex, strict);
//				}
//			}
			if (!facts && !queries && !asserts) {
				count += printKBexpressions(ui, pattern, agent.kBase.toString(), regex, strict);
			}
			else {
				if (facts) {
					count += printKBexpressions(ui, pattern, agent.kBase.toStringFacts(), regex, strict);
				}
				if (queries) {
					count += printKBexpressions(ui, pattern, agent.kBase.toStringQueryFilters(), regex, strict);
				}
				if (asserts) {
					count += printKBexpressions(ui, pattern, agent.kBase.toStringAssertFilters(), regex, strict);
				}
			}
			return new StatusObject<Integer>(0,count);
		}
	};
	
	

	private static int printKBexpressions(AgentUI ui, String pattern,
			String text, boolean regex, boolean strict) {
		if (regex && !strict) {
			pattern = ".*" + pattern + ".*";
		}
		int count = 0;
		String items[] = text.split("\\n");
		for (String s: items) {
			if (s!=null && s.trim().length()!=0) {
				// print only if this isn't a header and it matches the optional pattern
				// (no pattern means print)
				if (s != null 
						&& s.trim().length()!=0
						&& (pattern == null || (regex ? s.matches(pattern) : s.contains(pattern)))) {
					ui.println(s);
					count++;
				}
			}
		}
		return count;
	}

	private static final CasaLispOperator KB_DEFINE_ONTOLOGY_FILTER = new CasaLispOperator(
			"KB.define-ont-filter",
			"\"!Defining the ontology filter.\" "
					+ "PATTERN \"@java.lang.String\" \"!PATTERN of the FILTER\" "
					+ "&REST ARGUMENTS \"!Set to non-NIL to have PATTERN interpreted as a regular expression occuring in the expression displayed.\" ",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			// Object obj = params.getJavaObject("TAG");
			// String tag = (obj instanceof String)?(String)obj:null;

			String pattern = (String) params.getJavaObject("PATTERN");
			Cons rest = (Cons) params.getLispObject("ARGUMENTS");
			StringBuilder b = new StringBuilder();

			for (; rest != null && rest != org.armedbear.lisp.Lisp.NIL; rest = (rest.cdr instanceof Cons ? (Cons) rest.cdr
					: null)) {
				if (rest.car instanceof OntologyFilterArgument) {

					b.append(((OntologyFilterArgument) rest.car).toString());
					ListOfArgs.add(((OntologyFilterArgument) rest.car));
					// ListOfArgs.get(0).getName();

				} else {
					b.append(rest.car.toString());
					ListOfArgs.add(((OntologyFilterArgument) rest.car));
				}
			}
			return new StatusObject<String>(0, null, agent.println("", b.toString()));
		}
	};

	private static final CasaLispOperator KB_ARG_DESC = new CasaLispOperator(
			"KB.arg-desc", "\"!Description of arguments.\" "
					+ "VARIABLE \"@java.lang.String\" \"!PATTERN of the Argument\" "
					+ "&KEY SUBSUMPTION ", TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {

			String name = (String) params.getJavaObject("VARIABLE");
			String subsumption = (String) params.getJavaObject("SUBSUMPTION");
			OntologyFilterArgument o = new OntologyFilterArgument();
			o.setName(name.substring(1));
			o.setSubsumption(subsumption);
			return new StatusObject<OntologyFilterArgument>(0, null, o);
		}
	};

	public QueryResult query(String formula) throws ParseException {
		try {
			Formula exp = SL.formula(formula);
			QueryResult result = kBase.query(exp, new jade.util.leap.ArrayList());
			return result;
		} catch (Throwable e) {
			ParseException ex = new ParseException(
					"TransientAgent.query(): malformed term '" + formula + "'", 0);
			ex.initCause(e);
			throw ex;
		}
	}

	public boolean querywithFilter(String formula, TransientAgent a)
			throws ParseException {
		try {
			Formula exp = SL.formula(formula);
			QueryResult result = kBase.query(exp, new jade.util.leap.ArrayList());

			if (result == null) {
				result = cacheKB.query(exp, new jade.util.leap.ArrayList());
				if (result == null) {
					boolean rest = searchOntology1(a, null, null, null, exp.toString());
					if (rest == true) {
						return true;
					} else {
						return false;
					}
				} else
					return true;

			} else {
				return true;
			}

		} catch (Throwable e) {
			ParseException ex = new ParseException(
					"TransientAgent.query(): malformed term '" + formula + "'", 0);
			ex.initCause(e);
			throw ex;
		}
	}

	public String query_toString(String formula) throws ParseException {
		try {
			Formula exp = SL.formula(formula);
			QueryResult result = kBase.query(exp);
			StringBuilder b = new StringBuilder();
			if (result == null) {
				b.append("(not ").append(formula).append(')');
			} else {
				int size = result.size();
				if (size == 0)
					b.append(formula);
				else {
					if (size > 1)
						b.append("(set \n");
					for (int i = 0; i < size; i++)
						b.append(i > 0 ? "\n     " : "     ").append(
								SL.instantiate(exp, result.getResult(i)).toString());
					if (size > 1)
						b.append(")");
				}
			}
			return b.toString();
		} catch (Throwable e) {
			ParseException ex = new ParseException(
					"TransientAgent.query_toString(): malformed term '" + formula + "'",
					0);
			ex.initCause(e);
			throw ex;
		}
	}

	@SuppressWarnings("unused")
	private static final CasaLispOperator KB__QUERY_IF = new CasaLispOperator(
			"KB.QUERY-IF",
			"\"!Queries the formula from the KB returning a Nil (if the formula doesn't exist), T (if it does), or a list of string (if it does and it contains meta variables).\" "
					+ " FORMULA \"@java.lang.String\" \"!The formula.\""
					+ " &KEY"
					+ " REPLY-EXP \"!Return a string containing an expression appropriate for a reply instead of the default.\""
					+ " BOOLEAN \"!Return either T or NIL instead of the default.\""
					+ " TO \"@java.lang.String\" \"!Send the request to another agent and return a boolean.\"",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			String formString = (String) params.getJavaObject("FORMULA");
			try {
				if (params.getJavaObject("TO") != null) {
					try {
						String s = (String) params.getJavaObject("TO");
						URLDescriptor url = URLDescriptor.make(s);
						return new StatusObject<Boolean>(0, agent.queryIf(url, formString,
								3000));
					} catch (Throwable e) {
						throw new LispException("(query-if :to ...):", e);
					}
				} else if (params.containsKey("BOOLEAN")
						&& !params.isDefaulted("BOOLEAN")
						&& params.getJavaObject("BOOLEAN") != org.armedbear.lisp.Lisp.NIL) {
					QueryResult result = agent.query(formString);
					return new StatusObject<LispObject>(0,
							result == null ? org.armedbear.lisp.Lisp.NIL
									: org.armedbear.lisp.Lisp.T);
				} else if (params.containsKey("REPLY-EXP")
						&& !params.isDefaulted("REPLY-EXP")
						&& params.getJavaObject("REPLY-EXP") != org.armedbear.lisp.Lisp.NIL) {
					String result = agent.query_toString(formString);
					return new StatusObject<String>(0, "Success",
							result == null ? "((not (" + formString + ")))" : result);
				} else {
					QueryResult result = agent.query(formString);
//					if (result == null) {
//						String[] s = agent.banner.split(" ");
//						String[] s1 = s[1].split("@");
//						for (int i = 0; i < list.size(); i++) {
//
//							List<String> row = list.get(i);
//							if (row.get(0).matches(s1[0])) {
//								if (row.get(1).matches("YES")) {
//									boolean rest = searchOntology(agent, params, ui, env,
//											formString);
//									if (rest == false) {
//										return new StatusObject<jade.util.leap.ArrayList>(0,
//												result == null ? null : result.getResults());
//									} else {
//										return new StatusObject<LispObject>(0,
//												org.armedbear.lisp.Lisp.T);
//									}
//								} else {
//									return new StatusObject<jade.util.leap.ArrayList>(0,
//											result == null ? null : result.getResults());
//								}
//							}
//
//						}
//
//					}
					// return new Status(0);
					org.armedbear.lisp.Cons cons = null, cons2 = null;
					if (result == null)
						return new StatusObject<LispObject>(0, Lisp.NIL);
					jade.util.leap.ArrayList resultsList = result.getResults();
					Object[] results = resultsList.toArray();
					for (Object o : results) {
						MatchResult res = (MatchResult) o;
						if (cons == null)
							cons = cons2 = new Cons(new SimpleString(res.toString()));
						else {
							cons2.cdr = new Cons(new SimpleString(res.toString()));
							cons2 = (Cons) cons2.cdr;
						}
					}
					if (cons == null) {
						return new StatusObject<LispObject>(0, Lisp.T);
					}
					return (new StatusObject<Cons>(0, cons));
				}
			} catch (ParseException e) {
				return new Status(-1, "(QUERY ...): Bad Parse of expression '"
						+ formString + "': " + e.getMessage());
			}
		}
	};

	/**************************************************************************************************
	 * This filter is actually trying to find some relations present between types
	 * in the ontology. These relations could be used to give back the query
	 * results to the agent.
	 **************************************************************************************************/
	/*
	 * public static boolean searchOntology(TransientAgent agent, ParamsMap
	 * params, AgentUI ui, Environment env,String expr) { String
	 * updatedExpr=expr.substring(1, expr.length()-1);
	 * 
	 * //Find all the identifiers in your query expression String[] identifiers =
	 * updatedExpr.split(" "); //Get the CASAOntology CASAOntology ont =
	 * (CASAOntology) agent.getOntology(); for(String i : identifiers) {
	 * 
	 * List<String> myList = new ArrayList<String>(); try { //Check if identifier
	 * is a type in the tBox if(ont.isType(i)) { //Retrieve all the types which
	 * are isa related to the identifier Set<Type> set =
	 * ont.relatedTo(CASAOntology.ISA, i); Object[] a=set.toArray(); for(Object k:
	 * a) { if(k.toString().contains("declMaplet")) { String[]
	 * rest=k.toString().split(" "); int count = 0;
	 * 
	 * for(String j: rest) {
	 * 
	 * if(count==rest.length-1) { String[] ancestors=j.split("\\."); int count1 =
	 * 0; for(String l : ancestors) {
	 * 
	 * if(count1==ancestors.length-1) { ui.println(l.substring(0, l.length()-1));
	 * myList.add(l.substring(0, l.length()-1)); } count1++; }
	 * 
	 * } count++; } } } for(String m : myList) { String newExpr=expr.replace(i,
	 * m); QueryResult result; try { //requery the Knowledege Base with replaced
	 * identifier result = agent.query(newExpr); if(result==null) {
	 * //ui.println("NIL"); } else { //Return True when the new expression found
	 * in the knowledge base return true; } } catch (ParseException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); }
	 * 
	 * }
	 * 
	 * }
	 * 
	 * } catch (IllegalOperationException e1) { // TODO Auto-generated catch block
	 * e1.printStackTrace(); }
	 * 
	 * 
	 * } return false;
	 * 
	 * }
	 */

	// This filter is for the owl ontology
	/*
	 * public static boolean searchOntology(TransientAgent agent, ParamsMap
	 * params, AgentUI ui, Environment env,String expr) { String
	 * updatedExpr=expr.substring(1, expr.length()-1);
	 * 
	 * //Find all the identifiers in your query expression String[] identifiers =
	 * updatedExpr.split(" "); //Get the OWLOntology OWLOntology ont =
	 * (OWLOntology) agent.getOntology(); for(String i : identifiers) {
	 * 
	 * //List<String> myList = new ArrayList<String>(); try { //Check if
	 * identifier is a type in the tBox
	 * 
	 * if(ont.isType(i)) { //Retrieve all the types which are isa related to the
	 * identifier Set<String> set = ont.isa(i); String[] array = set.toArray(new
	 * String[0]); //loop through all the isa related identifiers for(String m
	 * :array) { String newExpr=expr.replace(i, m); QueryResult result; try {
	 * //requery the Knowledege Base with replaced identifier result =
	 * agent.query(newExpr); if(result==null) { //ui.println("NIL"); } else {
	 * //Return True when the new expression found in the knowledge base return
	 * true; } } catch (ParseException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); }
	 * 
	 * }
	 * 
	 * }
	 * 
	 * } catch (IllegalOperationException e1) { // TODO Auto-generated catch block
	 * e1.printStackTrace(); }
	 * 
	 * 
	 * } return false; for(String m : myList) { String newExpr=expr.replace(i, m);
	 * QueryResult result; try { //requery the Knowledege Base with replaced
	 * identifier result = agent.query(newExpr); if(result==null) {
	 * //ui.println("NIL"); } else { //Return True when the new expression found
	 * in the knowledge base return true; } } catch (ParseException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); }
	 * 
	 * } }
	 */
	public static Set<String> recursiveParent(TransientAgent a, String x,
			Set<String> SI) {
		if (x.equals("Thing")) {
			return SI;
		} else {
			SI.add(x);
			Set<String> parents;
			try {

				parents = a.getOntology().isParent(x);

				for (String p : parents) {
					recursiveParent(a, p, SI);
				}

			} catch (IllegalOperationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return SI;
	}

	public static boolean searchOntology1(TransientAgent agent, ParamsMap params,
			AgentUI ui, Environment env, String expr) {
		String updatedExpr = expr.substring(1, expr.length() - 1);
		List<Set<String>> s = new ArrayList<Set<String>>();
		String[] identifiers = updatedExpr.split(" ");
		OWLOntology ont = (OWLOntology) agent.getOntology();
		int count = 0;
		QueryResult result;
		for (String i : identifiers) {
			try {
				if (ont.isType(i)) {
					Set<String> A = new HashSet<String>();
					A = recursiveParent(agent, i, A);
					s.add(A);
				}
			} catch (IllegalOperationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Set<String> result1 = recursivePermutations(s);
		result1.remove(updatedExpr);
		Iterator iterator = result1.iterator();
		while (iterator.hasNext()) {
			String val = "(" + (String) iterator.next() + ")";
			try {
				result = agent.query(val);
				if (result == null) {
				} else {
					// Return True when the new expression found in the knowledge base
					count++;
					// agent.assert_(expr,cacheKB);
					return true;
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

	public static Set<String> recursivePermutations(List<Set<String>> sets) {
		if (sets.size() == 1) {
			Set<String> si = sets.get(0);
			return si;
		}
		if (sets.size() == 2) {
			return getPermutations(sets.get(0), sets.get(1));
		} else {
			return getPermutations(sets.get(0),
					recursivePermutations(sets.subList(1, sets.size())));
		}
	}

	private static Set<String> getPermutations(Set<String> a, Set<String> b) {
		Set<String> si = new HashSet<String>();
		for (String i : a) {
			for (String j : b) {
				String x = i + " " + j;
				si.add(x);
			}
		}
		return si;
	}

	// Filter for generic ontology

	public static boolean searchOntology(TransientAgent agent, ParamsMap params,
			AgentUI ui, Environment env, String expr) {
		String updatedExpr = expr.substring(1, expr.length() - 1);

		// Find all the identifiers in your query expression
		int querySize = ListOfArgs.size();
		String[] identifiers = updatedExpr.split(" ");
		// Get the CASAOntology
		OWLOntology ont = (OWLOntology) agent.getOntology();
		int count = 0;
		QueryResult result;
		String j = "";
		if (identifiers.length > 1) {
			j = identifiers[1];
		}

		for (String i : identifiers) {

			List<String> myList = new ArrayList<String>();
			try {
				// Check if identifier is a type in the tBox
				if (querySize == 0) {
					if (ont.isType(i)) {

						Set<String> set = ont.isa(i);

						String[] array = set.toArray(new String[0]);

						// loop through all the isa related identifiers
						for (String m : array) {
							String newExpr = "";
							if (!m.equals("Thing")) {
								newExpr = expr.replace(i, m);
								try {
									result = agent.query(newExpr);
									if (result == null) {
										// ui.println("NIL");
									} else {
										// Return True when the new expression found in the
										// knowledge base
										count++;
										return true;
									}
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}

							try {
								// requery the Knowledege Base with replaced identifier
								if (identifiers.length > 1) {
									if (ont.isType(j)) {

										Set<String> set1 = ont.isa(j);
										String[] array1 = set1.toArray(new String[0]);
										for (String n : array1) {
											if (!n.equals("Thing")) {

												if (newExpr.length() > 1) {
													newExpr = newExpr.replace(j, n);
													result = agent.query(newExpr);
													newExpr = newExpr.replace(n, j);
													if (result == null) {
														// ui.println("NIL");
													} else {
														// Return True when the new expression found in the
														// knowledge base
														count++;
														return true;
													}
												} else {
													newExpr = expr.replace(j, n);
													result = agent.query(newExpr);
													newExpr = "";
													if (result == null) {
														// ui.println("NIL");
													} else {
														// Return True when the new expression found in the
														// knowledge base
														count++;
														return true;
													}
												}

											}
										}
									}
								}
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					}
				} else {
					if (ListOfArgs.get(count).getSubsumption().equals("=")) {
						String newExpr = expr.replace(i, i);
						try {
							result = agent.query(newExpr);
							if (result == null) {
								// ui.println("NIL");
							} else {
								// Return True when the new expression found in the knowledge
								// base
								count++;
								return true;
							}
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
					if (ListOfArgs.get(count).getSubsumption().equals("<=")) {

						if (ont.isType(i)) {

							Set<String> set = ont.isa(i);
							String[] array = set.toArray(new String[0]);

							// loop through all the isa related identifiers
							for (String m : array) {
								String newExpr = "";
								if (!m.equals("Thing")) {
									newExpr = expr.replace(i, m);
								}

								try {
									// requery the Knowledege Base with replaced identifier
									if (identifiers.length > 1) {
										if (ont.isType(j)) {
											Set<String> set1 = ont.isa(j);
											String[] array1 = set1.toArray(new String[0]);
											for (String n : array1) {
												if (!n.equals("Thing")) {
													newExpr = newExpr.replace(j, n);
													result = agent.query(newExpr);
													if (result == null) {
														// ui.println("NIL");
													} else {
														// Return True when the new expression found in the
														// knowledge base
														count++;
														return true;
													}
												}
											}
										}
									}
									result = agent.query(newExpr);
									if (result == null) {
										// ui.println("NIL");
									} else {
										// Return True when the new expression found in the
										// knowledge base
										count++;
										return true;
									}
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}
						}
					}
				}
				count++;
			} catch (IllegalOperationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return false;

	}

	public ListOfTerm queryRef(String formula) throws ParseException {
		try {
			IdentifyingExpression exp = (IdentifyingExpression) SL.term(formula);
			ListOfTerm result = kBase.queryRef(exp);
			return result;
		} catch (Throwable e) {
			ParseException ex = new ParseException(
					"TransientAgent.queryRef(): malformed referential expression '"
							+ formula
							+ "' (expecting an 'all', 'any', 'iota', or 'some' expression).",
					0);
			ex.initCause(e);
			throw ex;
		}
	}

	@LispAccessible(name = "KB.QUERY-REF", help = "Return the answer to to JSL-like query-ref.", arguments = @LispAccessible.Argument(name = "FORMULA", help = "A JSL-like identifying expression: iota, any, all, some."))
	public SimpleString query_ref(String formula) {
		try {
			return new SimpleString(queryRef_toString(formula));
		} catch (Throwable e) {
			throw new LispException(e.toString());
		}
	}

	@LispAccessible(name = "KB.GET-VALUE", help = "Return the (unique) value of an attribute in an (attribute value) pair.", arguments = @LispAccessible.Argument(name = "ATTRIBUTE", help = "An attribute name in an (attribute value) pair."))
	public LispObject kb_get_value(String attrName) {
		Term term = null;
		try {
			List<Term> list = queryRef_toTermList("(some ?x (" + attrName + " ?x))");
			term = list.get(0);
			if (term instanceof IntegerConstant) {
				IntegerConstant cons = (IntegerConstant) term;
				return Fixnum.getInstance(cons.lx_value());
			}
			if (term instanceof DateTimeConstantNode) {
				DateTimeConstantNode cons = (DateTimeConstantNode) term;
				return Fixnum.getInstance(cons.lx_value().getTime());
			}
			if (term instanceof RealConstantNode) {
				RealConstantNode cons = (RealConstantNode) term;
				return DoubleFloat.getInstance(cons.lx_value());
			}
			if (term instanceof StringConstant) {
				StringConstant cons = (StringConstant) term;
				return new SimpleString(cons.stringValue());
			}
		} catch (Throwable e) {
			throw new LispException(e.toString());
		}
		throw new LispException("Unrecognized type for value '" + term + "'.");
	}

	public String queryRef_toString(String formula) throws ParseException {
		// try {
		// IdentifyingExpression exp = (IdentifyingExpression)SL.term(formula);
		// ListOfTerm result = kBase.queryRef(exp);
		// StringBuilder b = new StringBuilder("((= ");
		// b.append(formula);
		// b.append("\n  (set");
		// if (result!=null) {
		// for (Object o: result.asAList()) {
		// Term t = (Term)o;
		// b.append("\n       ").append(t.toString());
		// }
		// }
		// b.append("       )))");
		// return b.toString();
		// }
		// catch (Throwable e) {
		// ParseException ex = new
		// ParseException("TransientAgent.queryRef_toString(): malformed referential expression '"+formula+"' (expecting an 'all', 'any', 'iota', or 'some' expression).",
		// 0);
		// ex.initCause(e);
		// throw ex;
		// }
		StringBuilder b = new StringBuilder("((= ");
		b.append(formula);
		b.append("\n  (set");
		for (Term t : queryRef_toTermList(formula)) {
			b.append("\n       ").append(t.toString());
		}
		b.append("       )))");
		return b.toString();
	}

	public List<Term> queryRef_toTermList(String formula) throws ParseException {
		try {
			IdentifyingExpression exp = (IdentifyingExpression) SL.term(formula);
			ListOfTerm result = kBase.queryRef(exp);
			List<Term> list = new LinkedList<Term>();
			if (result != null) {
				for (Object o : result.asAList()) {
					Term t = (Term) o;
					list.add(t);
				}
			}
			return list;
		} catch (Throwable e) {
			ParseException ex = new ParseException(
					"TransientAgent.queryRef_toTermList(): malformed referential expression '"
							+ formula
							+ "' (expecting an 'all', 'any', 'iota', or 'some' expression).",
					0);
			ex.initCause(e);
			throw ex;
		}
	}

	public List<String> queryRef_toStringList(String formula)
			throws ParseException {
		List<String> list = new LinkedList<String>();
		for (Term t : queryRef_toTermList(formula)) {
			list.add(t.toString());
		}
		return list;
	}

	public List<Integer> queryRef_toIntegerList(String formula)
			throws ParseException {
		List<Integer> list = new LinkedList<Integer>();
		for (Term t : queryRef_toTermList(formula)) {
			try {
				list.add(new Integer(t.toString()));
			} catch (NumberFormatException e) {
				ParseException ex = new ParseException(
						"TransientAgent.queryRef_toIntegerList(): results cannot all be converted to Integer.",
						0);
				ex.initCause(e);
				throw ex;
			}
		}
		return list;
	}

	@SuppressWarnings("unused")
	private static final CasaLispOperator QUERYREF = new CasaLispOperator(
			"QUERYREF",
			"\"!Queries the formula for the reference from the KB returning a ListOfTerm structure.\" "
					+ " FORMULA \"@java.lang.String\" \"!The formula.\""
					+ " &KEY REPLY-EXP \"!Return an expression appropriate for a reply instead of a ListOfTerm structure\""
					+ " TO \"@java.lang.String\" \"!Send the request to another agent and return a Collection<Term>.\""
					+ " REPLY-LIST \"!Return a list of the individual results as Strings.\"",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass(), "QUERY-REF") {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			String formString = (String) params.getJavaObject("FORMULA");
			try {
				if (params.getJavaObject("TO") != null) {
					try {
						String s = (String) params.getJavaObject("TO");
						URLDescriptor url = URLDescriptor.make(s);
						Collection<Term> list = agent.queryRef(url, formString, 3000);
						return new StatusObject<Collection<Term>>(0, list);
					} catch (Throwable e) {
						throw new LispException("(queryref :to ...):", e);
					}
				}
				boolean replyExp = !params.isDefaulted("REPLY-EXP")
						&& params.getJavaObject("REPLY-EXP") != null;
				boolean replyList = !params.isDefaulted("REPLY-LIST")
						&& params.getJavaObject("REPLY-LIST") != null;
				if (replyExp && replyList) {
					throw new LispException(
							"(QUERYREF ...): Keys REPLY-EXP and REPLY-LIST are incompatible.");
				}
				if (replyExp) {
					String result = agent.queryRef_toString(formString);
					return new StatusObject<String>(result == null ? 1 : 0,
							result == null ? "failure" : "success", result == null ? null
									: result);
				} else if (replyList) {
					ListOfTerm result = agent.queryRef(formString);
					if (result != null) {
						List<String> list = new LinkedList<String>();
						for (Iterator<Term> i = result.iterator(); i.hasNext();) {
							Term t = i.next();
							list.add(t.toString());
						}
						return new StatusObject<List<String>>(0, "success", list);
					} else {
						return new Status(1, "failure");
					}
				} else {
					ListOfTerm result = agent.queryRef(formString);
					return new StatusObject<List<?>>(result == null ? 1 : 0,
							result == null ? "failure" : "success", result == null ? null
									: result.asAList());
				}
			} catch (ParseException e) {
				return new Status(-1, "(QUERYREF \"" + formString + "\"): "
						+ e.toString());
			}
		}
	};

	@SuppressWarnings("unused")
	private static final CasaLispOperator ADD__SINGLE__NUM__VALUE__KBFILTER = new CasaLispOperator(
			"ADD-SINGLE-NUM-VALUE-KBFILTER",
			"\"!Adds a single-value int filter to the KB.\" "
					+ " PREDICATE \"@java.lang.String\" \"!The name of the predicate to filter.\"",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			String pred = (String) params.getJavaObject("PREDICATE");
			agent.kBase.addFiltersDefinition(new SingleNumValueDefinition(pred));
			return new Status(0);
		}
	};

	@SuppressWarnings("unused")
	private static final CasaLispOperator CALL_GC = new CasaLispOperator(
			"CALL-GC", "\"!Requests the Garbage Collector to clean up.\" ",
			TransientAgent.class, new Object() { }.getClass().getEnclosingClass()) {
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui,
				Environment env) {
			System.gc();
			return new StatusObject<String>(0, "Success", InstanceCounter.getReport());
		}
	};

}
