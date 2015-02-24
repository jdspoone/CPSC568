package casa;

import casa.abcl.CasaLispOperator;
import casa.abcl.ParamsMap;
import casa.agentCom.DirectChannel;
import casa.agentCom.SocketServerInterface;
import casa.agentCom.SocketServerTCPIP;
import casa.agentCom.URLDescriptor;
import casa.conversation2.Conversation;
import casa.event.Event;
import casa.event.MessageEvent;
import casa.event.MessageEventDescriptor;
import casa.event.MessageObserverEvent;
import casa.event.NonRecurringEvent;
import casa.event.TimeEvent;
import casa.exceptions.IPSocketException;
import casa.exceptions.IllegalOperationException;
import casa.exceptions.URLDescriptorException;
import casa.interfaces.Describable;
import casa.interfaces.PolicyAgentInterface;
import casa.interfaces.ProcessInterface;
import casa.interfaces.SecurityFilterInterface;
import casa.interfaces.Transformation;
import casa.ontology.Ontology;
import casa.socialcommitments.SocialCommitment;
import casa.ui.AgentUI;
import casa.util.CASAUtil;
import casa.util.InstanceCounter;
import casa.util.Runnable1;
import casa.util.Trace;
import casa.util.TraceInterface;
import casa.util.TraceMonitor;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentSkipListSet;

import org.armedbear.lisp.Cons;
import org.armedbear.lisp.Environment;
import org.armedbear.lisp.LispObject;
import org.armedbear.lisp.SimpleString;

/**
 * <p>This class contains the basic methods for any process. It's primary responsibility is sending and receiving
 * messages.  Messages ({@link MLMessage}) are sent directly using either local calls (if the other agent happens
 * to be in the same process) or TCP/IP.  Messages (and other {@link Event}s) are received by removing them an
 * {@link EventQueue}, which is populated by the AbstractProcess's sister thread, a {@link SocketServer}. 
 * One of ABstractProcess's most important functions is to monitor the event queue in an <em>event loop</em>, removing items (such
 * as messages) for processing (via {@link #processEvent(Event)} or, if there is nothing in the event queue, 
 * calling {@link #processCommitments()}. (If there's nothing left to do, it just sleeps.) </p>
 *
 * <p>The class makes use of several important methods as part of the
 * event loop (which listens for incoming messages and other events):</p>
 * <table border="1">
 * <tr bgcolor="yellow">
 * <th>message loop status</th>               <th>method called</th>            <th>function</th>
 * </tr>
 * <tr>
 * <td rowspan=2>not started</td>             <td>{@link #initializeConstructor(ParamsMap, AgentUI, int)}</td>
 *                                            <td>pre-loop init, called once only, during the constructor, and <em>in the thread of the caller</em>, not the agent's thread.</td>
 * </tr>
 * <tr>
 * 	                                          <td>{@link #initializeThread(ParamsMap, AgentUI)}</td>
 *                                            <td>pre-loop init, called once only, before theevent loop starts, and <em> in the agent's main thread</em>, not the caller's thread. 
 *                                                This is called before the agent is registered (see {@link TransientAgent#initializeAfterRegistered(boolean)} for the method called after the agent is registered with the {@link LAC}.</td>
 * </tr>
 * <tr>
 * <td rowspan=3>running</td>                 <td>{@link #eventBufferLoopBody()}</td>
 *                                            <td>implements the body of the loop, called repeatedly.  The default version will call doIdle() if there's no messages available</td>
 * </tr>
 * <tr>
 *                                            <td>{@link #pendingFinishRun()}</td>
 *                                            <td>called when an exit() has been called, called only once</td>
 * </tr>
 * <tr>
 * <td>finished</td>                          <td>{@link #finishRun()}</td>
 *                                            <td>called after the message loop has exited, called only once</td>
 * </tr>
 * </table>
 * <p>When overriding these methods, be sure to call the <b>super</b> version
 * to avoid crippling the functionality of the process.</p>
 * <p>In addition, there are several important virtual methods having to do with
 * message handling.  All of these methods should return a {@link Status} with a value of:</p>
 * <ul>
 * <li>negative: to indicate failed handling of the message (no more processing necessary)
 * <li>0: to indicate successful handling of the message (no more processing necessary)
 * <li>1: the message was not handled or not recognized (continue to attempt processing normally)
 * <li>greater than 1: the message processing failed or was somehow not completely successful but we should continue to attempt processing normally)
 * </ul>
 * <table border="1">
 * <tr bgcolor="yellow">
 * <th>method</th>               <th>comment</th>
 * </tr>
 * <tr>
 *   <td>{@link #handleEvent(Event)}</td>
 *   <td>Normal handling of incoming messages.  See {@link TransientAgent#handleEvent(Event)}.</td>
 * </tr>
 * <tr>
 *   <td>{@link #resolveConnectException(MLMessage)}</td>
 *   <td>This method is called when a <em>send</em> has failed due to a
 *       ConnectionException, which might have been caused by an unresolved URL.
 *       The subclass can resolve the URL (as
 *       {@link TransientAgent#resolveConnectException(MLMessage)} does) and
 *       return 0 to cause the send to be retried (once).  AbstractProcess's
 *       default is to do nothing and return -1.</td>
 * </tr>
 * <tr>
 *   <td>{@link #unhandledMessage(MLMessage)}</td>
 *   <td>This method is called as a last-ditch effort to handle the message,
 *       after all other normal processing has not handled it (as opposed to
 *       failing).  AbstractProcess's default is to reply with a <em>
 *       not-understood</em> message.  This method has a <em>void</em>
 *       return</td>
 * </tr>
 * </table>
 * <p>When overriding these methods, be sure to call the <b>super</b> version
 * so that superclasses may handle any relevant messages that your class does not.</p>
 * 
 * <p>Other important messages are:
 * <ul>
 * <li> {@link #sendMessage(MLMessage)}
 * <li> {@link #sendMessage(String, String, URLDescriptor, String...)}
 * <li> {@link #sendRequestAndWait(MLMessage, long, MessageEventDescriptor...)}
 * <li> {@link #sendRequestAndWait(String, String, URLDescriptor, String[])}
 * </ul>
 * </p>
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
 * @author <a href="laf@cpsc.ucalgary.ca">Chad La Fournie</a>
 * @version 0.9
 *
 * @see TransientAgent
 * @see ProcessInterface
 */
public abstract class AbstractProcess
    extends    Thread
    implements ProcessInterface, PolicyAgentInterface, CasaObservable, Comparable<AbstractProcess>, TraceInterface {
	
	static {
		loadClass("casa.CASAProcess");
	}
	
  public static final void loadClass(String className)
  {
    try
      {
        Class.forName(className);
      }
    catch (ClassNotFoundException e)
      {
        e.printStackTrace();
      }
  }
  /**
   * This class should actually multiply inherit from Thread and CasaObservable,
   * but this is Java, so it can't:  Instead we're forced to hold this reference
   * to an ObservableState object.  Initialized in the constructor.
   */
  private CasaObservableObject      observerDelegate;
  /** The URL of this object */
  private URLDescriptor        url;
  /**
   * The Listener thread that will queue incoming messages to the event queue 
   * (see {@link #eventQueue}). 
   */
  private SocketServerInterface socketServer;
  /**
   * 
   * @return the agent's socket server.
   */
  public SocketServerInterface getSocketServer() { return socketServer; }
  /**
   * The agent's event queue.
   */
  EventQueue eventQueue;
  /**
   * A number to be used to generate unique <em>reply-with</em> fields in
   * messages.  To be used ONLY by {@link #getUniqueRequestID()}.
   */
  @CasaPersistent
  private int reqID=0;
  /** Set by {@link #exit()} to signal the agent to exit. */
  private boolean exit;
  /** Set when the agent is fully existed and not longer accepting any messages */
  private boolean exited = false;
  /** @return true iff the agent is fully existed and not longer accepting any messages */
  public  boolean existed() {return exited;}
  /** True if this agent is to keep looking for a port if it fails to open
   * the port suggested in the constructor.  If huntForPort is false, this agent
   * should fail if it can't open it's suggested port.
   */
  protected boolean huntForPort = false;
  /** The normal minimum range of agent lister port numbers */
  private final static int minPort =  2000;
  /** The normal maximum range of agent lister port numbers */
  private final static int maxPort = 10000;
  /**
   * Agents use this static as a place to start when searching for ports.  The
   * number is normally incremented (circularly) so agents can heuristically
   * avoid tripping over one another in searching for ports.
   */
  private static int portCounter   = minPort;
  /** True whenever the message listener port is open */
  private boolean portOpen = false;
  /** the Trace object that handles debugging traces and messages */
  protected final Trace trace; // = new Trace(this,null);
  /** Constant as a bit mask to signal the trace mode is undefined */
  public final static int TRACE_UNDEFINED = 0;
  /** Constant as a bit mask to signal tracing is off */
  public final static int TRACE_OFF       = 1;
  /** Constant as a bit mask to signal tracing is on */
  public final static int TRACE_ON        = 2;
  /** Constant as a bit mask to signal tracing to a monitor window */
  public final static int TRACE_MONITOR   = 4;
  /** Constant as a bit mask to signal tracing to a log file */
  public final static int TRACE_TO_FILE   = 8;

  /**
   * Used to make a trace parameter integer that matches the current options.  Normally
   * used for an agent spawning sub-agents that have the same trace attributes as the agent.
   * @return A trace parameter int that match the current options.
   */
  protected final int makeTraceParam() {
    in("AbstractProcess.makeTraceParam");
    int ret = (options.tracing?TRACE_ON:TRACE_OFF) | (options.traceMonitor?TRACE_MONITOR:0) | (options.traceToFile?TRACE_TO_FILE:0);
    ret |= (TRACE_ON|TRACE_OFF|TRACE_MONITOR|TRACE_TO_FILE)<<16; //set the mask part to "all specifically set"
    out("AbstractProcess.makeTraceParam");
    return ret;
  }

  /** The user-setable and potential persistent options for this object type.
   * This field must have public access to allow OPTIONS processing to get at it.
   */
  @CasaPersistent @CasaOption(recurse=true)
  public ProcessOptions options = null;

  /** The performative type hierarchy for this agent */
  protected Ontology ontology = null;
  protected static Ontology ontologyShared = null;
  
  /** The security filter used to encode/decode outgoing/incoming messages */
  protected SecurityFilterInterface securityFilter = null;
  
  /** used only to signal the starting thread that this agent is up and running */
  private final Thread starter;
  
  ConcurrentSkipListSet<Transformation> transformations = new ConcurrentSkipListSet<Transformation>();
  @Override
	public void addTransformation(Transformation trans) {
  	transformations.add(trans);
  }
  @Override
	public Transformation getTransformationFor(Describable d) {
  	for (Transformation t: transformations)
  		if (t.isApplicable(d)) return t;
  	return null;
  }
  @Override
	public Describable transform(Describable d) {
  	for (Transformation t: transformations) {
  		Describable ret = t.transform(d);
  		if (ret!=d) return ret;
  	}
  	return d;
  }
  @Override
	public Describable revTransform(Describable d) {
  	for (Transformation t: transformations) {
  		Describable ret = t.revTransform(d);
  		if (ret!=d) return ret;
  	}
  	return d;
  }
  @Override
	public String transform(String d) {
  	for (Transformation t: transformations) {
  		String ret = t.transform(d);
  		if (ret!=d) return ret;
  	}
  	return d;
  }
  @Override
	public String revTransform(String d) {
  	for (Transformation t: transformations) {
  		String ret = t.revTransform(d);
  		if (ret!=d) return ret;
  	}
  	return d;
  }

  /**
   * Constructor.  Most of the work of the constructor is done by the
   * {@link #initializeConstructor(ParamsMap, AgentUI, int) initialize()} method.
   * @param params The parameters for creating this agent
   * @param ui The UI to use to report any problems
   * @param port The port number this process will use for communication with other objects.  A -ve value indicates the agent should search starting with this port; +ve means it should fail if it can't open this port; 0 means it should choose it's own port.
   * @throws IPSocketException when trying to bind to an IPSocket (port)
   *         that is either in use or invalid.  See the <em>port</em> param above.
   */
  public AbstractProcess(ParamsMap params, AgentUI ui, int port)
  		throws IPSocketException {
  	super(new AgentThreadGroup((String)params.getJavaObject("NAME")), null, (String)params.getJavaObject("NAME"));
  	
    String name = (String)params.getJavaObject("NAME");

    trace = new Trace(this, name);

  	((AgentThreadGroup)getThreadGroup()).setAgent(this);
  	starter = Thread.currentThread();

  	options = makeOptions();
    
    if (port==0) 
    	port = -(portCounter++);
    if (portCounter>=maxPort) 
    	portCounter=minPort;
    if (port<0) {
    	port = -port; 
    	huntForPort = true; 
    } //if we're passed a -ve port value, then it means we should hunt for a port (see run()).

    url = initializeURL(port, name);
    params.put("PORT",new Integer(port),new org.armedbear.lisp.SingleFloat(port),false);
    
  	try {
  		initializeConstructor(params, ui, port);
  	} catch (IPSocketException e) {
			String m = println("error", "AbstractProcess.constructor: Socket exception", e);
			if (url!=null)
				url.setChannel(null);
			if (ui!=null)
				ui.println(m);
			throw e;
  	} catch (Throwable e) {
 			String m = println("error", "AbstractProcess.constructor: Unexpected exception", e);
			if (ui!=null)
				ui.println(m);
  	}
  	InstanceCounter.add(this);
  }
  
  private boolean initializationComplete = false;
  /**
   * @return true iff the agent has completed {@link #initializeThread(ParamsMap, AgentUI)} and in running its main thread.
   */
  public final boolean isInitialized() {return initializationComplete;}
  

  protected ParamsMap initParams=null;
  /**
   * @return The parameters used that were passed to the constructor as a map from key to java/lisp object pairs
   */
  public ParamsMap getInitParams() {return initParams;}
  
  private AgentUI initUI=null;

  /**
   * Initializes all local variables and starts up the listener thread (to listen
   * for and queue up any incoming messages).  This method is called from the constructor
   * and executes in the constructor caller's thread, not the agent's thread.
   *
   * @param params The parameters for initializing this agent
   * @param ui
   * @param port int this process will use for communication with other objects.  A -ve value indicates the agent should search starting with this port; +ve means it should fail if it can't open this port; 0 means it should choose it's own port.
   * @throws IPSocketException
   * @throws Exception 
   */
  protected void initializeConstructor(ParamsMap params, AgentUI ui, int port) throws Exception {

    observerDelegate = new CasaObservableObject(this.url, this);
    
    //try to start up, which means we are going to start the SocketServer thread,
    //and then wait for the run method to signal (through 'started') that the
    //SocketServer has successfully started, so that we can signal failure
    //(through throwing IPSocketException) to the creator of this AbstractProcess.
    initParams = params;
    initUI = ui;
    if (params.containsKey("FIPA-URLS"))
    	options.fipa_urls = params.getJavaObject("FIPA-URLS", Boolean.class);
    startSocketServer(); // this will create the socket server thread, which will start itself; won't return until it's started (we hope)
    long t = System.currentTimeMillis()+2000;
    while (System.currentTimeMillis()<t && (socketServer==null || !socketServer.isAlive())) {
      try {
				sleep(100);
			} catch (InterruptedException  e) {}
    }
    if (socketServer!=null && socketServer.isAlive()) {
    	portOpen = true;
      url.setPort(socketServer.getLocalPort());
      params.put("PORT", new Integer(url.getPort()), new org.armedbear.lisp.SingleFloat(url.getPort()) , false);
    }
    else {
    	throw new IPSocketException("Agent '"+getName()+"' failed startup");
    }
  }

  /**
   * @return true iff this is the agent's thread, that is, the thread that reads events off the event queue.
   */
  public boolean isAgentThread() {
 	  return Thread.currentThread()==this;
  }
  
  protected void resetRuntimeOptionsFromCommandLine() {
  	ParamsMap params = initParams;
  	Object obj = params.getJavaObject("TRACE");
  	int trace = 10; //default
    if (obj!=null) {
    	if (obj instanceof String)
    	  trace = Integer.parseInt((String)obj);
    	else if (obj instanceof Integer)
    		trace = (Integer)obj;
    	else 
    		println("error", "AbstractProcess.resetRuntimeOptionsFromCommandLine(): Cannot interpret TRACE option from \""+obj+"\"");
    }
    String traceTokens = (String)params.getJavaObject("TRACETAGS");

    if (trace!=TRACE_UNDEFINED) {
      options.tracing      = (trace & TRACE_ON)!=0;
      if ((trace & TRACE_OFF)!=0  && (trace & TRACE_ON)==0) options.tracing = false; //trace_on takes precedence if a conflict
      options.traceMonitor = (trace&TRACE_MONITOR)!=0;
      options.traceToFile =  (trace&TRACE_TO_FILE)!=0;
//      options.traceMask = (trace >>> 16);
    }
    if (traceTokens!=null && !traceTokens.equals("")) options.traceTags = new String(traceTokens);
    resetTraceOptions();
    //set the ack protocol on or off if necessary
    if (!params.isDefaulted("ACK"))
			try {
				setUseAckProtocol ((Boolean)params.getJavaObject("ACK", Boolean.class));
			} catch (Exception e) {
			}
  }
  
  /**
   * Synchronize the trace options with what's actually happening: if {@link #options}
   * says we're monitoring, start the monitor; if they say to log to a file, make
   * sure the file is open for logging.
   */
  protected void resetTraceOptions() {
    if (options.tracing && options.traceMonitor) startTraceMonitor();
    if (options.tracing && options.traceToFile) {
      try {
        this.trace.setLogToFile(true);
      }
      catch (IOException ex1) {
        println("error", "Cannot start trace on "+getURL().getFile()+".log: ",ex1);
      }
    }
    this.trace.addTraceTags(options.traceTags);
    options.traceTags = trace.getTraceTags(); //fixes options.traceTags if there was any -<tag> parts in traceTags
    trace.setHistoryMaxBytes(options.traceHistoryMaxSize);
  }

  /**
   * Factory method to make a new options object.  Subclasses should override
   * this method if they need a specialized options object.
   * @return a new ProcessOptions object
   */
  protected ProcessOptions makeOptions() {
    in("AbsractProcess.makeOptions");
    ProcessOptions ret = new ProcessOptions(this);
    out("AbsractProcess.makeOptions");
    return ret;
  }

  /**
   * Abstract classes for managing agent conversations.  Implemented in TransientAgent.
   * 
   * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
   */
//  	public abstract boolean hasConversation(String convID);
//  	public abstract CompositeConversation getConversation(String convID);
//  	public abstract CompositeConversation removeConversation(String convID);
//  	public abstract void addCompositeConversation(String convID, CompositeConversation conv);
  	public abstract boolean hasConversation(String convID);
  	public abstract LinkedList<Conversation> getConversation(String convID);
//  	public abstract Conversation getConversation2(String convID);
  	public abstract Conversation removeConversation(Conversation conv);
//  	public abstract Conversation removeConversation2(String convID);
  	public abstract void addConversation(String convID, Conversation conv);

//  	/**
//   * Abstract classes for managing agent conversations.  Implemented in TransientAgent.
//   * 
//   * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
//   */
//  	public abstract boolean hasConversation(String convID);
//  	public abstract CompositeConversation getConversation(String convID);
//  	public abstract CompositeConversation removeConversation(String convID);
//  	public abstract void addCompositeConversation(String convID, CompositeConversation conv);
  
  
  /**
   * Determines if performative field of message <em>m</em> is a subtype of (or the same
   * type as the performative named by the String <em>performative</em>.
   * @param m the message under question
   * @param ancestor the String name of the performative that's expected to be a super type
   * @return true if <em>m.performative</em> is a subtype of <em>ancestor</em>,
   * false otherwise
   */
  @Override
	public boolean isAPerformative (MLMessage m, String ancestor) {
    in("AbstractProcess.isAPerformative(MLMessage m, String ancestor="+ancestor+")");
    boolean ret = isAPerformative(m.getParameter("performative"), ancestor);
    out("AbstractProcess.isAPerformative(MLMessage m, String ancestor="+ancestor+") -> "+Boolean.toString(ret));
    return ret;
  }

  /**
   * Determines if <em>child</em> is a subtype of (or the same
   * type as the performative named by the String <em>ancestor</em>.
   * @param child the name of the proposed child type
   * @param ancestor the name of the proposed ancestor of the child
   * @return if <em>child</em> is a subtype of <em>ancestor</em>,
   * false otherwise
   */
  @Override
	public boolean isAPerformative (String child, String ancestor) {
    in("AbstractProcess.isAPerformative(String child="+child+", String ancestor="+ancestor+")");
    if (ontology==null) ontology = getOntology();
    if (child==null)    child    = ML.PERFORMATIVE; //take null as 'performative' (relative top)
    if (ancestor==null) ancestor = ML.PERFORMATIVE; //take null as 'performative' (relative top)
    boolean ret;
		try {
			ret = ontology.isa(ancestor,ML.PERFORMATIVE) && ontology.isa(child,ancestor);
		} catch (IllegalOperationException e) {
			//println("error","Abstractprocess.isAPerformative: Unknown ontology types '"+child+"' or '"+ancestor+"'",e);
			ret = false;
		}
    out("AbstractProcess.isAPerformative(String child="+child+", String ancestor="+ancestor+") -> "+Boolean.toString(ret));
    return ret;
  }

  /**
   * Determines if <em>child</em> is a subtype of (or the same
   * type as) <em>ancestor</em>.  If either 
   * @param child the name of the proposed child type; null is taken as "top"
   * @param ancestor the name of the proposed ancestor of the child; null is taken as "top"
   * @return if <em>child</em> is a subtype of <em>ancestor</em>, or if the at
   * least one of them is not in the ontology, then return equal(child,ancestor).
   */
  @Override
	public boolean isA (String child, String ancestor) {
    in("AbstractProcess.isA(String child="+child+", String ancestor="+ancestor+")");
    if (ontology==null) ontology = getOntology();
    if (child==null)    child    = "top";
    if (ancestor==null) ancestor = "top";
    boolean ret;
		try {
			ret = ontology.isa(child,ancestor);
		} catch (IllegalOperationException e) {
			println("warning","Abstractprocess.isA("+child+","+ancestor+") failed: "+e.toString()+". Using String.equals operator instead.");
			ret = child.equals(ancestor);
		}
    out("AbstractProcess.isA(String child="+child+", String ancestor="+ancestor+") -> "+Boolean.toString(ret));
    return ret;
  }
  
  /**
   * Determines if <em>child</em> is a subtype of (or the same type as the act
   * named by the String <em>ancestor</em>.  A subtype of a composite
   * Act is: forall x,y:Act . x isa y = (forall i:int | i<|y| . x[i] isa y[i]).
   *
   * @param child the name of the proposed child type
   * @param ancestor the name of the proposed ancestor of the child
   * @return if <em>child</em> is a subtype of <em>ancestor</em>, false
   *         otherwise
   */
  @Override
	public boolean isA(Act child, Act ancestor) {
  	return isAAct(child, ancestor);
  }

  /**
   * Determines if <em>child</em> is a subtype of (or the same type as the act
   * named by the String <em>ancestor</em>.  A subtype of a composite
   * Act is: forall x,y:Act . x isa y = (forall i:int | i<|y| . x[i] isa y[i]).
   *
   * @param child the name of the proposed child type
   * @param ancestor the name of the proposed ancestor of the child
   * @return if <em>child</em> is a subtype of <em>ancestor</em>, false
   *         otherwise
   */
  @Override
	public boolean isA(String child, Act ancestor) {
  	return isA(new Act(child), ancestor);
  }

  /**
   * Determines if <em>child</em> is a subtype of (or the same type as the act
   * named by the String <em>ancestor</em>.  A subtype of a composite
   * Act is: forall x,y:Act . x isa y = (forall i:int | i<|y| . x[i] isa y[i]).
   *
   * @param child the name of the proposed child type
   * @param ancestor the name of the proposed ancestor of the child
   * @return if <em>child</em> is a subtype of <em>ancestor</em>, false
   *         otherwise
   */
  @Override
	public boolean isA(Act child, String ancestor) {
  	return isA(child, new Act(ancestor));
  }

  /**
   * Determines if <em>child</em> is a subtype of (or the same type as the act
   * named by the String <em>ancestor</em>.  A subtype of a composite
   * Act is: forall x,y:Act . x isa y = (forall i:int | i<|y| . x[i] isa y[i]).
   *
   * @param child the name of the proposed child type
   * @param ancestor the name of the proposed ancestor of the child
   * @return if <em>child</em> is a subtype of <em>ancestor</em>, false
   *         otherwise
   */
  @Override
	public boolean isAAct (Act child, Act ancestor) {
    in("TransientAgent.isAAct(Act child=" + ((child==null)?"null":child.toString()) + ", Act ancestor="+ ((ancestor==null)?"null":ancestor.toString()) + ")");
    if (ontology==null) ontology = getOntology();
    assert ontology != null:"TransientAgent.isAAct relies on ontology!=null";
    if (child   ==null) child    = new Act(ML.TOP); //take null as "top"
    if (ancestor==null) ancestor = new Act(ML.TOP); //take null as "top"
    boolean ret = true;
    if (child.size() < ancestor.size())
      ret = false;
    else {
      for (int i = 0, end = ancestor.size(); i < end; i++) {
        if (!isA((String)child.elementAt(i),(String)ancestor.elementAt(i))) {
          ret = false;
          break;
        }
      }
    }
    out("TransientAgent.isAAct(Act child=" + ((child==null)?"null":child.toString()) + ", Act ancestor="+ ((ancestor==null)?"null":ancestor.toString()) + ") -> "+Boolean.toString(ret));
    return ret;
  }
  
  public boolean isAAct(String child, String ancestor) {
  	Act ch = new Act(child);
  	Act anc = new Act(ancestor);
  	return isAAct(ch, anc);
  }

  /**
   * Initialize the URL for this object.  If <em>name</em> contains '.'s or '/'s,
   * then it's assumed to contain the complete path (and any '.'s are replaced
   * with '/'s).  If <em>name</em> doesn't contain any '.'s or '/'s, then the path
   * part of the object string is inferred from it's class name.  The username
   * part is taken from the <em>user.name</em> system property.
   * @param port The port that this object will open on (if -ve it's changed to its absolute value)
   * @param name The name of the agent. Can be either with a path or not (in
   * which case, the class name us used to infer a path)
   * @return the URLDescriptor.make
   */
  protected URLDescriptor initializeURL(int port, String name) {
    String n = name.replace('.','/');
    int p = n.indexOf('/');
    if (p<0) n = getClass().toString().substring(6).replace('.','/')+"/"+name;
    URLDescriptor ret;
		try {
			ret = URLDescriptor.make(System.getProperty("user.name")
			                         , null
			                         , port<0 ? -port : port
			                         , n
			                         , null);
		} catch (URLDescriptorException e) {
			println("error", "Unexpected error in AbstractProcess.initailizeURL()", e);
			ret = null;
		}
		
		// set the channel unique URL for this process to be a DirectChannel 
		ret.setChannel(new DirectChannel(this));
		
    return ret;
  }

  /**
   * Debugging method: should be the first line of every method in this class and
   * in every subclass.  It uses {@link Trace trace}.in() to log the invocation
   * of the method.
   * @param methodName The string name of the method in the form "[className].[methodName]"
   */
  public void in(String methodName) {
    //if (trace!=null && (options==null || options.tracing) && trace.traceTagsContains("calls")) trace.in(methodName);
  }

  /**
  * Debugging method: should be the last line of every method (and every exiting
  * branch) in this class and
  * in every subclass.  It uses {@link Trace trace}.out() to log the exiting
  * of the method.
  * @param methodName The string name of the method in the form "[className].[methodName]"
  */
  public void out(String methodName) {
    //if (trace!=null && (options==null || options.tracing) && trace.traceTagsContains("calls")) trace.out(methodName);
  }

  /**
   * Returns a copy of the URL of the current process
   * @return the <em>URLDescriptor</em> of the process
   */
  @Override
	public URLDescriptor getURL() {
    try {
			return url==null?null:URLDescriptor.make(url);
		} catch (URLDescriptorException e) {
			println("error", "Unexpected error in calling AbstractProcess.getURL() for the first time", e);
			return null;
		}
  }

  /**
   * Returns the name of the current process
   * @return the name of this agent
   */
  @Override
	public String getAgentName() {
    return url==null?"<unknown>":url.getFile();
  }

  /**
   * Returns the strategy that this agent uses to interpret messages.
   * @return an <em>int</em> port number
   */
  @Override
	public abstract String getStrategy();

  /**
   * Sets the URL of for current process
   * @param url a reference to URLDescriptor that will become this agent's URL
   */
  //Was protected.  Changed to public so that conversation objects can access this method
  public void setURL(URLDescriptor url) {
    this.url = url;
  }

  /**
   * Retrieves whether this agent will use the the Ack (acknowledge) protocol.
   * That is, that this process will add ML.REQUEST_ACK to outgoing messages and
   * resend if the message does not get acknowledged within the timeout period.
   * This changes to always return false when the agent is exitting, because at
   * that point, acks will probably fail.
   *
   * @return true iff the agent uses the Ack protocol and isn't exitting
   */
  @Override
	public boolean getUseAckProtocol() {return options.useAckProtocol && ! exit;}

  /**
   * Sets whether this agent will use the the Ack (acknowledge) protocol.
   * That is, that this process will add ML.REQUEST_ACK to outgoing messages and
   * resend if the message does not get acknowledged within the reply-by period.
   * @param b true to set the protocol, false to not use the protocol.
   * @return true iff the agent used the Ack protocol (before the current setting);
   */
  @Override
	public boolean setUseAckProtocol(boolean b) {
    boolean ret=options.useAckProtocol;
    options.useAckProtocol=b;
    return ret;
  }

  /**
   * Returns a reference to this object's {@link ProcessOptions} object.
   * @return an reference to this object's {@link ProcessOptions} object.
   */
  @Override
	public ProcessOptions getOptions() {return options;}

  /**
   * Sets this object's ProcessOptions object.
   * @param options a reference to a ProcessOptions object will become this agent's
   *        options object
   */
  @Override
	public void setOptions(ProcessOptions options) {this.options = options;}

  /**
   * Save the options to a persistent database.  Since this is non-persistent
   * class, there's nothing to do.  Subclasses should override as appropriate.
   */
  @Override
	public void updateOptions() {}

  /**
   * Refresh agent behaviour from the options object.  Subclasses should override as appropriate.
   */
  @Override
	public void realizeAgentBehaviourFromOptions() {
    in("updateFromOptions");
    setTracing(options.tracing);

    if (trace != null)
      trace.setTraceTags(options.traceTags);

    if (options.traceMonitor)
      startTraceMonitor();

    try {
      if (trace!=null) trace.setLogToFile(options.traceToFile && options.tracing);
    }
    catch (IOException ex1) {
      println("warning","Agent: Cannot set trace to log to file",ex1);
    }

    setUsePriority(options.usePriority);
    
    if (options.security_package!=null) resetSecurityPackage(options.security_package);

    out("updateFromOptions");
  }

  /**
   * Determines whether the given message should be handled by receiveMessage,
   * or if it should be thrown out.  This method is called from
   * receiveMessage().  It should be overridden in subclasses to add actual
   * behaviour.  The overriding method should normally call and return
   * super.authorizeMessage() if it doesn't want to filter incoming messages.
   *
   * @param msg contains the message to authorize
   * @return <code>true</code> if the message is authorized to be processed;
   * <code>false</code> otherwise
   */
  public boolean authorizeMessage (MLMessage msg) {
    in ("AbstractProcess.authorizeMessage");
    out ("AbstractProcess.authorizeMessage");
    return true;
  }

  //
  // PROACTIVE methods
  //
  /**corrects message fields like message timeout if bad
   * 
   * @param message						the message to correct
   * @throws URLDescriptorException		thrown if the receiver URL in the message is bad
   */
  public void fixupMessage(MLMessage message) throws URLDescriptorException {
		//set REQUESTACK in the message if appropriate
		if (getUseAckProtocol()) message.setParameter(ML.REQUEST_ACK,"true");

		//adjust TIMEOUT except for the case of a REQUEST_WHENEVER/SUBSCRIBE message that's
		//supposed to say in effect forever (TIMEOUT==ML.TIMEOUT_NEVER)
		long timeout = 0L;
		String sto = message.getParameter(ML.REPLY_BY);
		try {
			timeout = Long.parseLong(sto);
		}
		catch (Throwable ex) {}
		boolean isSubscribe = isAPerformative(message, ML.SUBSCRIBE);
		if (isSubscribe && timeout == 0L) {
			message.setParameter(ML.REPLY_BY, Long.toString(ML.TIMEOUT_NEVER));
		}
		else {
			long now = System.currentTimeMillis();
			if (timeout - now < options.minTimeout) {
				long t = now + options.minTimeout;
				String time = Long.toString(t);
				if (timeout!=0 && isLoggingTag("warning3")) { //print the warning only if timeout was filled in (and too old), and warnings are on
					println("warning3", "Message timeout adjusted to '"+MLMessage.longToTextDate(t)
							+"' from '"+MLMessage.longToTextDate(timeout)+"' for "+message.getParameter("performative")+" message");
				}
				message.setParameter(ML.REPLY_BY, time);
			}
		}

    //determine the recipient URL (toURL)
		URLDescriptor toURL;
    try {
      toURL = URLDescriptor.make (message.getParameter (ML.RECEIVER));
    } catch (URLDescriptorException ex1) {
      String m = "Cannot parse valid RECEIVER from message:\n" +
                 message.toString (true) + "\n  -- " + ex1.toString ();
      Trace.log("warning", m);
      println ("warning", m);
      out ("AbsractProcess.sendMessage_primitive(MLMessage)");
      throw (ex1);
    }
    
    message.setParameter(ML.RECEIVER, toURL.toString());

		//ensure the FROM and SENDER fields are filled in
		message.setParameter(ML.SENDER,getURL().toString());

		//ensure the REPLY_WITH field is filled in
		if (message.getParameter(ML.REPLY_WITH)==null) message.setParameter(ML.REPLY_WITH,getUniqueRequestID());

		//ensure the CONVERSATION_ID field is filled in
		if (message.getParameter(ML.CONVERSATION_ID)==null) message.setParameter(ML.CONVERSATION_ID,getUniqueRequestID());    

  }

  /**
   * Sends the <em>message</em> to the the <em>message.receiver</em> or, if
   * <em>message.receiver</em> is missing or empty, to <em>message.to</em>.
   * Will adjust the following message fields:
   * <ul>
   * <li>sets <em>message.requestAck</em> if the Ack protocol is on
   * ({@link #getUseAckProtocol()} returns true).
   * <li>adjust the value of <em>message.reply-by</em> to be the current time plus
   * {@link #options}.{@link ProcessOptions#minTimeout minTimeout} if it is missing or
   * or malformed or less than current time plus options.minTimeout (the exception
   * is it is a <em>subscribe</em> message and <em>message.timeout</em>==0,
   * which means "no timeout on the standing request").
   * <li><em>message.sender</em> is always reset to be the URL of this agent
   * </ul>
   * If send fails on a ConnectionException the first time, the subclass will
   * be given a chance to resolve the problem by calling the "abstract" method
   * {@link #resolveConnectException(MLMessage)}.  Normally, this will be because
   * of an unresolved recipient URL.  If resolveConnectException() is able to
   * resolve the URL and returns 0, the send will be tried one more time.
   *
   * @param message   <em>MLMessage</em> of the message being sent, which
   * contains the info about the recipient in the <em>receiver</em> or
   * <em>to</em> fields.
   * @return The <em>Status</em> describing the status of the operation:
   * <li>0 if the message was sent without errors, or</li>
   * <li>-3 if there was an exception thrown during the message sending process
   * (in this case, the returned object will be a <em>StatusObject</em>
   * containing an <em>Exception</em> object).</li>
   */
  @Override
  public Status sendMessage(MLMessage message) {
  	Status result;
  	try {
  		fixupMessage(message);
  		sendMessage_primitive (message);
  		result = new Status (0,message.getParameter(ML.PERFORMATIVE)+" "
  				+message.getParameter(ML.ACT)+" message dispatched to "
  				+message.getParameter(ML.RECEIVER));
  		notifyObservers(ML.EVENT_MESSAGE_SENT, message);
  	} catch (Exception ex) {
  		result = new StatusObject<Exception> (Status.EXCEPTION_CAUGHT,
  				println ("error", "Exception ("+ex.toString()+") caught when sending message\n"+message.toString(true), ex));
  		notifyObservers(ML.EVENT_MESSAGE_SEND_FAILED, result);
  	} 
  	return result;
  }

  /**
   * Sends the <em>message</em> to the the <em>message.receiver</em> or, if
   * <em>message.receiver</em> is missing or empty, to <em>message.to</em>.
   * Will adjust the following message fields:
   * <ul>
   * <li><em>sender</em> is always reset to be the URL of this agent
   * </ul>
   * If send fails on a ConnectionException the first time, the subclass will
   * be given a chance to resolve the problem by calling the "abstract" method
   * {@link #resolveConnectException(MLMessage)}.  Normally, this will be because
   * of an unresolved recipient URL.  If resolveConnectException() is able to
   * resolve the URL and returns 0, the send will be tried one more time.
   *
   * @param message   <em>MLMessage</em> of the message being sent, which
   * contains the info about the recipient in the <em>receiver</em> or
   * <em>to</em> fields.
   * @param shouldResolve Uses only by recursive calls: Do not use.
   * @return a <em>Status</em> object describing the success (0) or failure (-ve) of the operation
   * @throws URLDescriptorException 
   * @throws IOException 
   */
  final Status sendMessage_primitive(MLMessage message, boolean... shouldResolve) throws URLDescriptorException, IOException {
    final boolean resolve = shouldResolve.length<1 || shouldResolve[0]==true;
    URLDescriptor toURL;

    MLMessage wrapper = null;

    //determine the recipient URL (toURL)
    try {
      toURL = URLDescriptor.make (message.getParameter (ML.RECEIVER));
    } catch (URLDescriptorException ex1) {
      String m = "Cannot parse valid RECEIVER from message:\n" +
                 message.toString (true) + "\n  -- " + ex1.toString ();
      println ("warning", m);
      out ("AbsractProcess.sendMessage_primitive(MLMessage)");
      throw (ex1);
    }
    
    message.setParameter(ML.RECEIVER, toURL.toString());

    message.setParameter (ML.SENDER, getURL ().toString (toURL));

    // Check for indirection through another agent
    while (toURL.containsVia(getURL())) //don't send to self (directly or indirectly)
      toURL.popViaAtEnd(); //	this circumvents any loops that contain self in the via's
    if (toURL.hasDataValue ("via")) {
      URLDescriptor viaURL = URLDescriptor.make (toURL.getDataValue ("via"));
    	wrapper = MLMessage.constructBasicProxyMessage (message, getURL (), viaURL, toURL);
     	if (isLoggingTag ("msg")) {
     		println("msg",  "Sending message (via another agent):\n" + message.toString(true));//debug
     	}
     	notifySendingMessage (message);
      return sendMessage (wrapper);
    }

    // Check for indirection through the LAC
    if (toURL.getIndirect() && !toURL.local(getURL())) {
  		int lacPort = toURL.getLACport();
  		if (lacPort==0)
  			lacPort = LAC.ProcessInfo.lacPort;
  		URLDescriptor lacURL = URLDescriptor.make(getURL().getHost(),lacPort);
      toURL.setIndirect (false);
      wrapper = MLMessage.constructBasicProxyMessage (message, getURL (), lacURL, toURL);
      if (isLoggingTag ("msg")) {
        println("msg",  "Sending message (via receiver's LAC):\n" + message.toString(true));//debug
      }
      notifySendingMessage (message);
      return sendMessage (wrapper);
    }
    
    //determine whether to send the message as process-local call, or via TCP/IP
    //a local-to-process URL should ALWAYS have a channel since the LocalChannel is always created in it's constructor.
//    Channel channel = toURL.getChannel();
//    if (channel == null) {
//    	final AbstractProcess This = this;
//    	final URLDescriptor ThisToURL = toURL;
//    	final MLMessage ThisMessage = message;
//    	class MyRunnable implements Runnable {
//    		public boolean done = false;
//    		public IOException exception = null;
//				@Override
//				public void run() {
//					try {
//						Channel channel2 = new TCPChannelGlobal(This, ThisToURL, socketServer);
//			    	ThisToURL.setChannel(channel2);
//			    	channel2.sendMessage(This, ThisMessage);
//					} catch (IOException e) {
//						This.println("error", "AbstractProcess.sendMessage_primitive(): Unexpected exception creating new TCPChannel", e);
//						exception = e;
//					}
//					done = true;
//				}
//    	};
//    	// we need to run the code in MyRunnable.run() in a thread that isn't going to be interrupted 
//    	// (it will fail if interrupted). But we also need to sync with it when it's done.
//    	MyRunnable r = new MyRunnable();
//    	Thread t = makeSubthread(r);
//    	t.start();
//    	while (!r.done) {
//    		try {
//    			t.join();
//    		} catch (InterruptedException e) {}
//    	}
//    	if (r.exception != null)
//    		throw(r.exception);
//
//    	
//    }
//    else {
//      channel.sendMessage(this, message);
//    }
    Status ret;
			ret = toURL.sendMessage(this, message);
	    notifySendingMessage(message);
    
    //TODO get rid of AgentLoopupTable
//    AbstractProcess localAgent = options.processLocalMessageShortcutting ? (AbstractProcess) AgentLookUpTable.get (URLDescriptor.make (toURL)) : null;
//    if (resolve) { //do this only on the first iteration
//      MLMessage temp = null;//debug
//      if (isLoggingTag("msg")) {
//        temp = message.clone();
//      }
//      notifySendingMessage(message);
//      if (isLoggingTag("msg")) {
//        if (temp!=null && !message.equals(temp))//debug
//          println("msg", "Subclass revised message; sending revised message ("+(localAgent!=null?"local call":"TCP/IP")+"):\n" + message.toString(true));//debug
//      }
//      if (securityFilter!=null) {
//      	message = securityFilter.processMessage(message,this,false);
//        if (isLoggingTag("msg")) println("msg", "Sending message (after signing/encryption):\n" + message.toString(true));
//      }
//    }
//    if (localAgent != null) { //**********send the message using a process-local call
//      sendMessage_primitive (message, localAgent);
//      pauseCheck();
//
//    } else { //************send the message using TCP/IP
//      //send the message
//      try {
//        //calculate the host IP address and port
//        URLDescriptor url = toURL;
//        InetAddress host = null;
//        int port = -1;
//        //if this is an aliased URL and it's not in the local network, replace the host and port with the alias
//        String alias = url.getDataValue("alias");
//        if (alias!=null && alias.length()>0 && !url.local(getURL())) {
//          try {
//            URLDescriptor aliasURL = URLDescriptor.make(alias);
//            host = aliasURL.getHost();
//            port = aliasURL.getPort();
//          }
//          catch (URLDescriptorException ex4) {
//            println("error","Bad alias value in URL: '"+url.toString()+"'");
//          }
//        }
//        if (host==null && port==-1) {
//        	host = url.getHost();
//        	port = url.getPort();
//        }
//
//        //finally, send it...
//        sendMessage_primitive ((wrapper == null) ? message : wrapper, host, port);
//        pauseCheck();
//      } catch (SocketException ex2) { //(ConnectException ex2) {
//      	if (resolve && !toURL.isResolved()) { //do this only on the first iteration
//      		//give a subclass a chance to resolve a ConnectionException -- this will be done by TransientAgent 
//      		//by sending a asking the LAC.  We will build a Command (Runnable1) to be executed upon the LAC successfully
//      		//returning the resolved URL (may be in this thread or another one).
//      		try {
//      			final MLMessage m2 = message.clone();
//      			if (ex2 instanceof ConnectException ||
//      					(resolveConnectException (message, new Runnable1<String,Status>() {
//      						@Override public Status run(String s) {
//      							m2.setParameter(ML.RECEIVER,s);
//      							try {
//      								return sendMessage_primitive(m2,false);
//      							} catch (IOException e) {
//      								return new StatusObject<Exception>(-1,"AbstractProcess.sendMessage_primitive: "+e.toString(),e);
//      							} catch (URLDescriptorException e1) {
//      								return new StatusObject<Exception>(-2,"AbstractProcess.sendMessage_primitive: "+e1.toString(),e1);
//      							} 
//      						}
//      					})).getStatusValue () < 0) {
//      				throw (ex2);
//      			}
//      			else
//      				message.setParameter ("resolved-url", null);
//      		} catch (IOException ex3) {
//      			throw (ex3);
//      		}
//      	} else {
//      		throw (ex2);
//      	}
//      } catch (IOException ex2) {
//      	throw (ex2);
//      }
//    }

    return ret;
  }

  /**
   * Utility method to construct and send a message asynchronously using
   * {@link AbstractProcess#sendMessage(MLMessage)}.
   *
   * @param performative the performative field of the message
   * @param act the act field
   * @param reciever the URL of the message reciever
   * @param list an array of key/value pairs (keys are even, values are odd).
   *          Key may <b>not </b> be null, but values may be.
   * @return the Status returned from
   *         {@link AbstractProcess#sendMessage(MLMessage)}
   */
  public Status sendMessage (String performative, String act,
      URLDescriptor reciever, String... list) {
    in ("AbstractProcess.sendMessage");
    MLMessage message = getNewMessage (performative, act, reciever, list);			//create a message out of the command
    Status stat = sendMessage (message);											//send the message
    out ("AbstractProcess.sendMessage");
    return stat;																	//return the status of the send
  }

  /**
   * Utility method to construct and send a syncronous message using
   * {@link AbstractProcess#sendRequestAndWait(MLMessage, long, MessageEventDescriptor...)}.
   *
   * @param performative the performative field of the message
   * @param act          the act field
   * @param reciever     the reciever field
   * @param list         an array of key/value pairs (keys are even, values are
   *                     odd). Key may <b>not</b> be null, but values may be.
   * @return             the reply message
   * @throws RuntimeException   if the <em>list</em> parameter is malformed
   */
  public MLMessage sendRequestAndWait(String performative, String act, URLDescriptor reciever, String... list) {
    in("AbstractProcess.sendMessage_sync");
    MLMessage message = getNewMessage (performative,
                                       act,
                                       reciever,
                                       list);
    //MLMessage ret = sendMessage_sync(message);
    StatusObject<MLMessage> stat = sendRequestAndWait(message, 5000);
    MLMessage ret = stat.getObject();
    out("AbstractProcess.sendMessage_sync");
    return ret;
  }

  /**
   * This method is to be overridden by any subclasses that wish to be informed
   * whenever a message is actually sent out.  This version does nothing.
   * @param message
   * @throws Exception 
   */
  protected void notifySendingMessage(MLMessage message) {
  }

//  /**
//   * This method "sends" the message to local agents via object messaging instead of
//   * sending it via TCP/IP messaging.   Network overhead is reduced in this manner.
//   *
//   * @param message Message to be sent
//   * @param abs AbstractProcess that is about to receive the message
//   * @return a status of 0
//   */
//  private final Status sendMessage_primitive (MLMessage message, AbstractProcess abs) {
//    if (isLoggingTag("msg")) {
//      println("msg", "Sending message (local call):\n" + message.toString(true)); //debug
//    }
//    if (abs.isLoggingTag("msg")) {
//      abs.println("msg", "Receiving message (local call):\n" + message.toString(true)); //debug
//    }
//    MessageEvent mEvent = new MessageEvent(ML.EVENT_MESSAGE_RECEIVED,abs,message);				//create a message event for the queue
//    abs.eventQueue.putItem(mEvent);																//add the event to the queue
//    return new Status (0);
//  }
  
//  private Sockets sockets = new Sockets(this);
//  protected Sockets getSockets() {return sockets;}
  
//  /**
//   * Overloading the sendMessage_primitive method.   Why?  When we are in AgentProxy.forwardMessage()
//   * we need to send a message without modifying the fields in the message.   If we DO modify the fields
//   * in the message the signatures (for each message) will not match at all.
//   * In order to keep order in the code, we decided to overload this method since writing to a socket
//   * should not happen at any other level than here (AbstractProcess).
//   *
//   * @param message the message to send
//   * @param ip represents the host
//   * @param port to be used in the message
//   * @return a status indicating the success of the send operation
//   * @throws IOException
//   */
//  protected final Status sendMessage_primitive(MLMessage message, InetAddress ip, int port) throws IOException {
//  	Socket socket = null;
//  	String convID = message.getConversationID();
//  	URLDescriptor recipient;
//		try {
//			recipient = message.getReceiver();
//		} catch (URLDescriptorException e) {
//  		throw new IOException("Message has bad URL in RECEIVER field: "+message.getParameter(ML.RECEIVER ), e);
//		}
//  	SocketIn socketIn = getSockets().getSocket(convID, recipient);
//  	try {
//  		if (socketIn==null) {
//  			socket = new Socket(ip, port);
//  			socketIn = new SocketIn(socket, eventQueue, this);
//  			getSockets().putSocket(convID, recipient, socketIn);
//  		}
//			socketIn.send(message);
//  	}
//  	catch (SocketException ex2) { 
//  		throw (ex2);
//  	}
//  	catch (IOException ex2) {
//  		String m =
//  				"AbstractProcess.sendMessage: Unexpected I/O exception when sending message: '" +
//  						message.toString(true) + "\n  " + ex2.toString();
//  		println("error", m);
//  		throw (ex2);
//  	}
//  	catch (Throwable ex2) {
//  		String m =
//  				"AbstractProcess.sendMessage: Unexpected exception when sending message: '" +
//  						message.toString(true) + "\n  " + ex2.toString();
//  		println("error", m);
//  		throw new IOException(ex2);
//  	}
////  	finally {
////  		if (socket != null) {
////  			socket.close();
////  		}
////  	}
//  	return new Status(0);
//  }
  
  /**
   * This method may be overridden by a subclass to handle a ConnectionException
   * during a call to sendMessage(MLMessage).  This method is guaranteed to be
   * called at most only once per sent message.  If this method returns a status of value
   * 0, the send will be attempted exactly once more.<br>
   * This implementation does nothing, and returns -1.
   * @param msg the MLMessage the generated a ConnectionException on send.
   * @return Status(-1)
   */
  protected Status resolveConnectException(MLMessage msg, Runnable1<String, Status> cmd) 
  {return new Status(-1);}

//  private MLMessage syncBuf = null;
//  private Thread syncBufWaitThread = null;

  /**
   * Sends a REQUEST message, <em>message</em>, waiting up to <em>timeout</em> milliseconds
   * for the request transaction to complete, and returns as StatusObject which contains 
   * the final message, if it completed.  If the transaction times out either by 
   * a timeout event, or the <em>timeout</em> expiring the message will be null.
   * Otherwise the message could be a PROPOSE, FAILURE, REFUSE, or NOT_UNDERSOOD.<p> 
   * If the messageDescriptor's are left out, this method will generate descriptors
   * for PROPOSE/DISCHARGE|PERFORM|x, FAILURE/DISCHARGE|PERFORM|x, REFUSE/REQUEST|x,
   * and NOT_UNDERSOOD/REQUEST|x.
   * @param message The message to send, which should be a properly-addressed REQUEST message.
   * @param timeout the time in milliseconds to wait if no event occurs
   * @param messageDescriptors describing all the possible events to watch for the message, defaults to typical request protocol.
   * @return a status of 0 is if a non-"negative-reply" message is returned, +ve if a "negative-reply" message is return,
   * or -ve if no message was returned (a timeout).
   * @throws UnsupportedOperationException if the message is somehow mangled
   */
  protected StatusObject<MLMessage> sendRequestAndWait(MLMessage message, long timeout, MessageEventDescriptor... messageDescriptors) throws UnsupportedOperationException {
  	try {
			fixupMessage(message);
		} catch (URLDescriptorException e2) {
	  	return new StatusObject<MLMessage>(-2, "Bad URL in RECEIVER field", null);
		}

  	if (messageDescriptors==null || messageDescriptors.length==0) {
    	MLMessage transformedMessage = (MLMessage) transform(message);
  		try {
  			messageDescriptors = new MessageEventDescriptor[]{
  					new MessageEventDescriptor(this, 
  							ML.EVENT_MESSAGE_RECEIVED, 
  							ML.PERFORMATIVE, ML.PROPOSE,
  							ML.ACT, new Act("discharge|perform|"+transformedMessage.getParameter(ML.ACT)),
  							ML.SENDER, URLDescriptor.make(transformedMessage.getParameter(ML.RECEIVER)),
  							ML.IN_REPLY_TO, message.getParameter(ML.REPLY_WITH)
  					),
  					new MessageEventDescriptor(this, 
  							ML.EVENT_MESSAGE_RECEIVED, 
  							ML.PERFORMATIVE, ML.FAILURE,
  							ML.ACT, new Act("discharge|perform|"+transformedMessage.getParameter(ML.ACT)),
  							ML.SENDER, URLDescriptor.make(transformedMessage.getParameter(ML.RECEIVER)),
  							ML.IN_REPLY_TO, message.getParameter(ML.REPLY_WITH)
  					),
  					new MessageEventDescriptor(this, 
  							ML.EVENT_MESSAGE_RECEIVED, 
  							ML.PERFORMATIVE, ML.REFUSE,
  							ML.ACT, new Act(transformedMessage.getParameter(ML.PERFORMATIVE)+"|"+transformedMessage.getParameter(ML.ACT)),
  							ML.SENDER, URLDescriptor.make(transformedMessage.getParameter(ML.RECEIVER)),
  							ML.IN_REPLY_TO, message.getParameter(ML.REPLY_WITH)
  					),
  					new MessageEventDescriptor(this, 
  							ML.EVENT_MESSAGE_RECEIVED, 
  							ML.PERFORMATIVE, ML.NOT_UNDERSTOOD,
  							ML.ACT, new Act(transformedMessage.getParameter(ML.PERFORMATIVE)+"|"+transformedMessage.getParameter(ML.ACT)),
  							ML.SENDER, URLDescriptor.make(transformedMessage.getParameter(ML.RECEIVER)),
  							ML.IN_REPLY_TO, message.getParameter(ML.REPLY_WITH)
  					),
  					new MessageEventDescriptor(this, 
  							ML.EVENT_MESSAGE_RECEIVED, 
  							ML.PERFORMATIVE, transformedMessage.getAct().elementAt(0),
  							ML.ACT, transformedMessage.getAct().pop(),
  							ML.SENDER, URLDescriptor.make(transformedMessage.getParameter(ML.RECEIVER)),
  							ML.IN_REPLY_TO, message.getParameter(ML.REPLY_WITH)
  					)
  			};
  		} catch (Throwable/*URLDescriptorException*/ e1) {
  			//assert false;
  			return new StatusObject<MLMessage>(-1, println("error", "AbstractProcess.sendRequestAndWait(): Bad URL in RECEIVER field or see stack trace.", e1), null);
  		}
  	}
  	return sendMessageAndWait(message, timeout, messageDescriptors);
  }
  
  /**
   * Sends a QUERY (QUERY-REF or QUERY-IF) message, <em>message</em>, waiting up to <em>timeout</em> milliseconds
   * for the query transaction to complete, and returns as StatusObject which contains 
   * the final message, if it completed.  If the transaction times out either by 
   * a timeout event, or the <em>timeout</em> expiring the message will be null.
   * Otherwise the message could be a INFORM-IF/INFORM-REF, FAILURE, REFUSE, or NOT_UNDERSOOD (QUERY-IF-REPLYs or QUERY-REF-REPLYs).<p> 
   * If the messageDescriptor's are left out, this method will generate descriptors
   * for QUERY-IF-REPLYs or QUERY-REF-REPLYs respectively (depending on the message performative).
   * @param message The message to send, which should be a properly-addressed REQUEST message.
   * @param timeout the time in milliseconds to wait if no event occurs
   * @param messageDescriptors describing all the possible events to watch for the message, defaults to typical query protocol.
   * @return a status of 0 is if a non-"negative-reply" message is returned, +ve if a "negative-reply" message is return,
   * or -ve if no message was returned (a timeout).
   * @throws UnsupportedOperationException if the message is somehow mangled
   */
  protected StatusObject<MLMessage> sendQueryAndWait(MLMessage message, long timeout, MessageEventDescriptor... messageDescriptors) throws UnsupportedOperationException {
  	try {
			fixupMessage(message);
		} catch (URLDescriptorException e2) {
	  	return new StatusObject<MLMessage>(-2, "Bad URL in RECEIVER field", null);
		}
  	
  	String performative = message.getParameter(ML.PERFORMATIVE);
  	String returnPerformative;
  	if (isA(performative, ML.QUERY_IF))
  		returnPerformative = ML.QUERY_IF_REPLY;
  	else if (isA(performative, ML.QUERY_REF))
    	returnPerformative = ML.QUERY_REF_REPLY;
  	else 
  		throw new UnsupportedOperationException("only message performatives QUERY-IF and QUERY-REF subtypes supported.");

  	if (messageDescriptors==null || messageDescriptors.length==0) {
    	MLMessage transformedMessage = (MLMessage) transform(message);
  		try {
  			messageDescriptors = new MessageEventDescriptor[]{
  					new MessageEventDescriptor(this, 
  							ML.EVENT_MESSAGE_RECEIVED, 
  							ML.PERFORMATIVE, returnPerformative,
  							ML.SENDER, URLDescriptor.make(transformedMessage.getParameter(ML.RECEIVER)),
  							ML.IN_REPLY_TO, message.getParameter(ML.REPLY_WITH)
  					)
  			};
  		} catch (Throwable/*URLDescriptorException*/ e1) {
  			//assert false;
  			return new StatusObject<MLMessage>(-1, println("error", "AbstractProcess.sendRequestAndWait(): Bad URL in RECEIVER field or see stack trace.", e1), null);
  		}
  	}
  	return sendMessageAndWait(message, timeout, messageDescriptors);
  }
  
  /**
   * Sends a message, <em>message</em>, waiting up to <em>timeout</em> milliseconds
   * for the request transaction to complete, and returns as StatusObject which contains 
   * the final message, if it completed.  If the transaction times out either by 
   * a timeout event, or the <em>timeout</em> expiring the message will be null.
   * @param message The message to send, which should be a properly-addressed REQUEST message.
   * @param timeout the time in milliseconds to wait if no event occurs
   * @param messageDescriptors describing all the possible events to watch for the message.
   * @return a status of 0 is if a non-"negative-reply" message is returned, +ve if a "negative-reply" message is return,
   * or -ve if no message was returned (a timeout).
   * @throws UnsupportedOperationException if the message is somehow mangled or you don't supply the messageDescriptors
   */
  protected StatusObject<MLMessage> sendMessageAndWait(MLMessage message, long timeout, MessageEventDescriptor... messageDescriptors) throws UnsupportedOperationException {
   	if (isAgentThread()) 
  		throw new UnsupportedOperationException("AbstractProcess.sendMessageAndWait() cannot be called from the agent's event-processing thread.");
  	if (messageDescriptors==null || messageDescriptors.length==0) {
  		throw new UnsupportedOperationException("The 3rd parameter, messageDescriptors cannot be null or of zero length.");
  	}
  	
  	try {
			fixupMessage(message);
		} catch (URLDescriptorException e2) {
	  	return new StatusObject<MLMessage>(-2, "Bad URL in RECEIVER field", null);
		}
  	
  	final Thread thread = Thread.currentThread();

  	MessageObserverEvent[] events = new MessageObserverEvent[messageDescriptors.length];
  	int i=0;
  	class AbstractProcess_MsgObserverEvent extends MessageObserverEvent {
			public AbstractProcess_MsgObserverEvent(boolean recurring,
					PolicyAgentInterface agent, MessageEventDescriptor descriptor) {
				super(recurring, agent, descriptor);
			}
			@Override
			public void fireEvent() {
				thread.interrupt();
				super.fireEvent();
			}
  	}
  	for (MessageEventDescriptor desc: messageDescriptors) {
  		events[i++] =
  				new AbstractProcess_MsgObserverEvent(false, this, desc);
  	}

  	for (MessageObserverEvent e: events) {
  		e.start();
  	}
  	
  	// wait around for one of the events to occur
  	boolean timedOut = true; // assume the worst
  	synchronized (thread){
  		long timeoutTime = System.currentTimeMillis()+timeout;
  		try {
  			Status stat = sendMessage(message);
  			if (stat.getStatusValue()>=0)
  				thread.wait(timeout);
  			else 
  				return new StatusObject<MLMessage>(stat.getStatusValue(), stat.getExplanation(), null);
  		} catch (InterruptedException e) {
  			if (someTriggered(events))
  				timedOut = false;
  			else {
  				while (!someTriggered(events) && System.currentTimeMillis()<timeoutTime) {
  					long wait = timeoutTime-System.currentTimeMillis();
  					if (wait<=0) wait = 1;
  					try {
  						thread.wait(wait);
  					} catch (InterruptedException e1) {
  						if (someTriggered(events)) {
  							timedOut = false;
  							break;
  						}
  					}
  				}
  			}
  		}
  	}

  	MLMessage reply = null;
  	String retPerformative = "unknown";

  	int fired = -1;
  	int retVal = 1;
  	for (MessageObserverEvent e: events) {
  		if (e.hasTriggered()) {
  			if (fired>=0) {
  				println("warning","AbstractProcess.sendRequestAndWait: more than one wait event fired: "+e.getID()+" (selected "+fired+")");
  			}
  			if (fired<0) fired = e.getID();
  			MLMessage temp = e.getMessage();
  			if (temp!=null) { 
  				retPerformative = temp.getParameter(ML.PERFORMATIVE);
  				if (reply!=null)
  					println("warning","AbstractProcess.sendRequestAndWait: more than one wait event fired has a message: "+e.getID()+" (selected "+fired+")");
  				if (reply==null || !isA(retPerformative, ML.NEGATIVE_REPLY)) {
  					reply = temp;
  					fired = e.getID();
  					if (!isA(retPerformative, ML.NEGATIVE_REPLY));
  						retVal = 0;
  				}
  			}
  		}
  		else {
			  e.cancel();
  		}
  	}

  	StatusObject<MLMessage> ret = new StatusObject<MLMessage>(
  			timedOut?-1:retVal, 
  			timedOut?"Timed out":(retVal==0?"Success":retPerformative), 
  			reply);
  	return ret;
  }
  
  private boolean someTriggered(MessageObserverEvent[] events) {
  	for (MessageObserverEvent e: events) {
  		if (e.hasTriggered())
  			return true;
  	}
  	return false;
  }
  
  /**
   * This method is called from {@link #processEvent(Event)}.  It should be overridden in
   * subclasses to add actual behaviour.  The overriding method should normally
   * call and return super.handleMessage() if it doesn't handle the message.
   * An overriding method should return:
   * <li> 0   if the message is handled</li>
   * <li> +ve if the message was not handled, but should continue to attempt to process</li>
   * <li> -ve if the message failed and any further processing should be aborted</li>
   * @param event The recieved message
   * @return Status(1), indicating the message was not handled
   */
  protected abstract void handleEvent(Event event);

  /** Used only by {@link #startSocketServer()} and {@link SocketServerTCPIP#run()} to synchronize the start of the socket server. */
  volatile boolean waitingForSocketServerToStart = false;

  /**
   * Start the SocketServer and initialize the eventQueue
   * @throws IPSocketException
   */
  protected void startSocketServer() throws IPSocketException {
  	eventQueue = new EventQueue(this);
  	SocketServerTCPIP[] global = SocketServerTCPIP.get();
  	int port = url.getPort();

  	//takes care of the port==0 and we-already-have-the-port-open cases.
  	for (SocketServerTCPIP s: global) {
  		if (port==0 || s.getLocalPort()==port) {
  			socketServer = s;
  			return;
  		}
  	}
  	
  	//if we didn't find the exact port (above) and we are to hunt, then use the first port in the list.
  	if (huntForPort && global.length>0) {
  		socketServer = global[0];
  	}
  	
  	//at this point we are going to open a new port because we didn't find an acceptable open one above.
  	if ((huntForPort&&(port>0))) port = -port;
  	synchronized(eventQueue) {
  		interrupted(); // clear any interrupts queued
  	  waitingForSocketServerToStart = true;
  		socketServer = new SocketServerTCPIP (port);
  		try {
  			eventQueue.wait(3000);
  		} catch (InterruptedException e) {
  			// expect the server to interrupt us during this time...;
  		}
  		if (socketServer!=null)
  			url.setPort(socketServer.getLocalPort());
  	  waitingForSocketServerToStart = false;
  	}
	  interrupted(); // clear any interrupts queued
  }
  
  /**
   * Queues an event to the agent's event queue
   * @param event the event to queue
   */
  @Override
	public void queueEvent(Event event) {
  	eventQueue.putItem(event);
  	if (isLoggingTag("msg") && event instanceof MessageEvent && isA(event.getEventType(), ML.EVENT_MESSAGE_RECEIVED)) {
  		println("msg", "Queued received message ("+event.getEventType()+", id="+event.getID()+"):\n"+((MessageEvent)event).getMessage().toString(true));
  	}
  	this.interrupt();
  }

	/**
	 * Put the Event on the agent's event queue 
	 * iff the event is not already on the queue.
	 * @param event The event to queue.
	 */
	@Override
	public void queueEventIf(Event event){
		if (!eventQueue.contains(event)) queueEvent(event);
		else println("eventqueue","Event not queued because it's already on the queue: "+event);
	}
	
	/**
	 * Remove the event from the agent's event queue
	 * iff the event is already in the queue
	 * @param event The event to dequeue.
	 */
	@Override
	public void dequeueEvent(Event event){
		if (eventQueue.contains(event))
			eventQueue.remove(event);
	}
	
  /** Used only by {@link #makeAgentInThisProc(ParamsMap, AgentUI)} and {@link AbstractProcess#run()} to synchronize the start of the socket server. */
  volatile public boolean waitingForAgentToStart = false;
  
  /**
   * This method starts the message handling loop
   * by calling {@link #eventBufferLoop()}, which actually does the loop.
   * If <em>messageBufferLoop()</em> returns, run() signals <em>socketServer</em>
   * to exit and sends a inform/exit message to <em>self</em> so that <em>socketServer</em>
   * will wake up to exit.
   */
  @Override
  final public void run() {
    if (socketServer==null) {
    	Trace.log("error", getAgentName().toString()+": Cannot start agent due to listener startup failure.");
    	return;
    }
    try {
      initializeThread(initParams, initUI);
    }
    catch (Throwable ex) {
    	Trace.log("error", getAgentName().toString()+": Unexpected throwable during initializeThread()",ex);
    }
    initializationComplete = true;
    synchronized (this) {
      if (starter!=null && waitingForAgentToStart) starter.interrupt();
    }
    
    try {
    eventBufferLoop();
    }
    catch (Throwable ex) {
    	Trace.log("error", getAgentName().toString()+": Unexpected throwable during eventBufferLoop()", ex);
    }

    try {
    finishRun();
    }
    catch (Throwable ex) {
    	Trace.log("error", getAgentName().toString()+": Unexpected throwable during finishRun()", ex);
    }
  
    //kill the message listener thread by telling it to exit, then send a message to get it out of the wait state
    if (socketServer!= null) 
    	socketServer.exit();
    MLMessage msg = MLMessage.getNewMLMessage();
    msg.setParameter(ML.PERFORMATIVE,ML.INFORM);
    msg.setParameter(ML.ACT,ML.EXIT);
    URLDescriptor newURL = getURL();
    newURL.setIndirect(false);
    msg.setParameter(ML.RECEIVER,newURL.toString(getURL()));
    msg.setParameter(ML.SENDER,getURL().toString(getURL()));
    sendMessage(msg);
    
    //while we've "sent the message", it isn't ever going to arrive because the 
    //socketServer is dead now, so we have to put it on the queue here.
    eventQueue.putItem(new MessageEvent(ML.EVENT_MESSAGE_EVENT,this,msg)); 
  
    //finally, close the port so that another agent can use it
    closePort ();
    
//    if (ProcessInfo.process==null && ProcessInfo.lac==null) {
//    	println("Detected singleton process: calling exit(0).");
//    	Runtime.getRuntime().exit(0);
//    } else {
      ExitChecker.startExitCheck();
//    }
  } // run
  
  /**
   * determines if this process can be stopped.  A subclass may override if there
   * are more constraints on stopping.  This implementation checks to see that that
   * {@link #eventQueue} is empty and there are no (non-standing-request) requests
   * with replies pending.
   * @return true if the process is stoppable, false otherwise.
   */
  @Override
	public boolean isStoppable() {
    return eventQueue.isEmpty();
  }
  
  /**
   * Tells the process to exit.  Note that the process may not exit right away;
   * generally, it must check to see that {@link #isStoppable()} is true before
   * it can actually exit (terminate {@link #eventBufferLoop()}).  In the event this
   * method is called more than once, it will only call {@link #pendingFinishRun()}
   * the first time.
   */
  @Override
	public void exit() {
    in("AbsractProcess.exit");
    if (!exit) {
      exit = true;
      println("info","exit(): Signaled to exit");
      pendingFinishRun ();
    } else
      if (isLoggingTag("warning")) println("warning","Agent '"+getName()+"' received DUPLICATE exit() request");
    this.interrupt(); // The agent might be sleeping when exit becomes true, so we need to wake it up.
    out("AbsractProcess.exit");
  }
  
  /**
   * determines if this process is in the process of exiting.  This returns the value of the exit field.
   * @return true if the process is exiting, false otherwise.
   */
  @Override
	public boolean isExiting() {
    return exit;
  }
  
  /**
   * Subclasses may override this method to initialize before the message loop is called,
   * after the constructor is finished but before control is returned to the thread
   * that called the constructor.  Called once, just before messageBufferLoop().
   * This implementation initializes tracing.
   * This method is safe as it is called from the thread of the agent (not the constructor)
   * @param quals
   */
  protected void initializeThread(ParamsMap params, AgentUI ui) {
  	in("AbsractProcess.initializeRun");
  	try {
  		this.trace.setName(getURL().getFile());
  		resetRuntimeOptionsFromCommandLine();
  		resetSecurityPackage((String)params.getJavaObject("SECURITY"));
  	} catch (Throwable e) {
  		println("error", "TransientAgent.initializeThread(): Unexpected error", e);
  	}
  	out("AbsractProcess.initializeRun");
  }
  
  /**
   * Subclasses may override this method to cleanup after an {@link #exit()} has been
   * called, but before the
   * message loop exits and the agent is about to terminate.  Called once, just
   * before the message loop terminates.
   * This implementation does a <code>notifyObservers(state.STATE_EXITING)</code>.
   */
  protected void pendingFinishRun() {
    in("AbsractProcess.pendingFinishRun");
    notifyObservers(ML.EVENT_EXITING, this);
    out("AbsractProcess.pendingFinishRun");
  }
  
  /**
   * Subclasses may override this method to cleanup after the
   * message loop exits and the agent is about to terminate.  Called once, just
   * before the agent terminates.
   * This implementation does nothing but call
   * <code>notifyObservers(state.STATE_EXITED)</code>.
   */
  protected void finishRun() {
    in("AbsractProcess.finishRun");
    //notifyObservers(new casa.State(ObservableEvent.STATE_EXITED,this));
    exited = true;
    notifyObservers(ML.EVENT_EXITED, this);
    if (socketServer!=null) {
      socketServer.exit();
      socketServer.interrupt();
      socketServer = null;
    }
    out("AbsractProcess.finishRun");
  }
  
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
    if (socketServer!=null) {
      socketServer.exit();
      socketServer.interrupt();
      socketServer = null;
    }
	}

	/**
   * The basic loop where this process checks for incoming messages on it's
   * socket port.  This may be overridden by a subclass (but be careful about
   * it!).  This implementation mearly loops, calling {@link #eventBufferLoopBody()}
   * until after {@link #exit()} has been called and {@link #isStoppable()}
   * becomes true.
   */
  private final void eventBufferLoop() {
		in("AbsractProcess.messageBufferLoop");
		boolean printedWarning = false;

		while (!(exit && (isStoppable() || exitWaitCounter>20))) {

	  	if (exit)
	  		exitWaitCounter++;
	  	else
	  		exitWaitCounter=0;

			// print a warning message if we can't exit, but only once in a row
			if (exit) {
				if (!printedWarning)
					println("warning", "Attempting to exit, waiting on: "
							+ (eventQueue.isEmpty() ? "unknown"
									: "event queue to be empty"));
				printedWarning = true;
			} else
				printedWarning = false;

			try {
				eventBufferLoopBody();
			} catch (OutOfMemoryError e) {
				try {
					println("error", "OutOfMemoryError. Agent exiting abruptly.", e);
					InstanceCounter.report();
				} catch (Throwable e1) {
					// probably got here due to the memory error, so nothing we can do. :(
				}
				break;
			} catch (Throwable e) {
				String s = "AbstractProcess.messageBuffLoop of agent " + getName()
						+ ": Unexpected exception in AbstractProcess.eventBufferLoopBody()";
				println("error", s, e);
			}

		} // while
		out("AbsractProcess.messageBufferLoop");
	}
  
  /**
   * Can be used to defer a section of code on to the agent's event queue so that the code
   * will execute in the agent's main thread.
   * After it is done execution, it will interrupt the calling thread.
   * @param x some Runnable to execute
   * @param milliseconds the number of milliseconds (minimum) to wait before executing.
   */
  public void defer(final Runnable x, long milliseconds) {
  	final Thread callingThread = Thread.currentThread();
  	if (milliseconds<=0) 
      new NonRecurringEvent(ML.EVENT_DEFERRED_EXECUTION,this)
  	    {@Override public void fireEvent() {
  	    	super.fireEvent();
  	    	x.run();
  	    	callingThread.interrupt();
  	    	}
  	    }.start();
  	else
      new TimeEvent(ML.EVENT_DEFERRED_EXECUTION_DELAYED,this,System.currentTimeMillis()+milliseconds)
      	{@Override public void fireEvent() {
      		super.fireEvent();
      		x.run();
  	    	callingThread.interrupt();
      		}
      	}.start();
  }
  
  /**
   * Can be used to defer a section of code on to the agent's event queue so that the code
   * will execute in the agent's main thread.
   * After it is done execution, it will interrupt the calling thread.
   * @param x some Runnable to execute
   */
  @Override
	public void defer(Runnable x) {
  	defer(x,0);
  }
  
  /**
   * Subclasses will normally override this.  It will be called whenever the agent is
   * waiting around for an an event from an empty event queue.  This method mearly returns
   * false. 
   * @return true if it did something; false if it didn't. 
   */ 
  protected SocialCommitment processCommitments() {
  	return null;
  }
  
  /**
   * Determines if the event queue has an event ready to process.
   * Subclasses should override this method if they would like to
   * pre-empt event dequeing in favour of processing a social
   * commitment.
   * @return
   */
  protected boolean isEventQueueReady(){
	  return !eventQueue.isEmpty();
  }
  
  protected Event eventQueuePeek(){
	  return eventQueue.peek();
  }
  
  /** used by {@link #eventBufferLoopBody()} and {@link EventQueue#notifyNewItem(Event)} to notify of events being placed in the queue */
  volatile boolean waitingForEvent = false;
  
  int exitWaitCounter = 0;
  
  /**
   * This method is called by {@link #eventBufferLoop()} and may be overridden
   * by a subclass as the actual code to dequeue messages off the queue.  However,
   * the subclass method is best off by calling
   * super.{@link #eventBufferLoopBody()} as part of it's implementation.<br>
   * This implementation will
   * <ol>
	 * <li> If there is an event in the event queue, process it ({@link #processCommitments()}) and exit.
	 * <li> Otherwise if there are ready commitments, process one ({@link #processCommitments()}) and exit.
	 * <li> Otherwise if there is still nothing in the event queue, wait to be interrupted (unless we are in {@link #exit} state, then wait 500ms}).
	 * </ol>
   */
  protected void eventBufferLoopBody() {
  	assert isAgentThread();

  	println("eventloop9", "AbstractProcess.eventBufferLoopBody(): Event loop running...");
  	
  	boolean eventQueueProcessed = isEventQueueReady(); 
  	if (eventQueueProcessed) {
  		Event event = eventQueue.getItem(); 
  		if (isLoggingTag("eventloop"))
  		  println("eventloop", "AbstractProcess.eventBufferLoopBody(): Processing event: "+event);
  		processEvent(event);
  		pauseCheck();
  		return;
  	}

  	SocialCommitment commitment = getChosenCommitment();
  	if (commitment!=null) {
  		if (isLoggingTag("eventloop"))
  			println("eventloop", "AbstractProcess.eventBufferLoopBody(): Processing commitment: "+commitment);
  		processCommitment(commitment);
  		pauseCheck();
  		return;
  	}

  	if (!eventQueueProcessed && commitment==null) { 
  		synchronized (this) {
  			waitingForEvent = true;
  			try {
  				println("eventloop5", "AbstractProcess.eventBufferLoopBody(): Event loop in waiting state.");
  				this.wait(exit?500:heartbeat);
  			}
  			catch (InterruptedException e) { //we EXPECT the interrupt when something is queued to the event queue, etc.
  				if (isLoggingTag("eventloop5")) 
  					println("eventloop5", "AbstactProcess.eventBufferLoopBody(): interrupted in event loop wait: " + e.toString());
  				return;
  			}
  			finally {
  			  waitingForEvent = false;
  			}
  		}
  		//if we got to this point the headbeat has expired without an interrupt.  We should
  		//check to see if we have anything to do outstanding, which there SHOULDN'T be: this should
  		//be considered an error condition.
  		println("eventloop5", "AbstactProcess.eventBufferLoopBody(): heartbeat caused iteration of event loop.");
  		if (isEventQueueReady()) {
  			println("error", "AbstractProcess.eventBufferLoopBody(): Unexpectedly found an event in the event queue in an uninterrupted heartbeat.");
  		}
  		if (hasActiveCommitments()) {
  			println("error", "AbstractProcess.eventBufferLoopBody(): Found active commitments during an uninterupted heartbeat.");
  		}
  	}
  }
  
  /**
   * The interval (in milliseconds) that the agent should sleep if it has nothing else to do.  The agent
   * will "wake up" every interval to check to see if there is something in the event queue or any
   * social commitments to process.<br>  
   * Set this value to 0 to have the agent only wake up when it 
   * is interrupted (which is is whenever something is queued to the event queue, etc.). 
   */
  long heartbeat = 0; 
  
//	static String[] myHostAddresses = CASAUtil.getMyAddresses();
  
	public boolean isThisMyAddress(URLDescriptor theURL) {
		return theURL.equals(getURL());
//  	URLDescriptor tempURL = URLDescriptor.make(getURL());
//    for (String a: myHostAddresses) {
//    	try {
//				tempURL.setHost(a);
//			} catch (URLDescriptorException e) {
//				println("error", "AbstractProcess.isThisMyAddress():", e);
//			}
//    	if (theURL.equals(tempURL))
//    		return true;
//    }
//    return false;
  }
  
  /**
   * This method is a pre-processing step before the event from the event queue is passed
   * passed on to {@link #handleEvent(Event)} for actual processing event.  In particular,
   * message events require special processing:
   * <ol>
   * <li> the message is decoded if has been encoded for security
   * <li> message events are dropped if {@link #isObserveMessages()} is not true and the
   * message is not addressed to this agent.
   * <li> message events are dropped if they are not authorized according to {@link #authorizeMessage(MLMessage)}
   * <li> if this message is an exit message it's just dropped (part of how a casa agent ends)
   * <li> {@link #notifyObservers()} is called with parameters {@link ML#EVENT_MESSAGE_RECEIVED} and the message itself
   * </ol>
   * So this method then calls {@link #handleEvent(Event)} and if that returns 
   * @param event	the event on the queue being processed
   */
  private void processEvent(final Event event) {
    if (event instanceof MessageEvent) 
    	notifyObservers(event.getEventType(), ((MessageEvent)event).getMessage());
    else 
    	notifyObservers(event.getEventType(), event);
    
    // special processing for message events
  	if (event instanceof MessageEvent && isA(event.getEventType(), ML.EVENT_MESSAGE_INCOMING)) {
    	MessageEvent mevent = (MessageEvent)event;
    	
    	//decode the message
    	if (securityFilter!=null) {
    		mevent.applySecurityFilter(securityFilter);
    	}
      
    	MLMessage message = mevent.getMessage();	//extract the message parameter
    	if (isLoggingTag("msg"))
    		println("msg", " (processing incomming message "+event.getEventType()+", ID "+event.getID()+"):\n" + message.toString(true));

      // only process messages if they're addressed to me, or if we are observing all...
      boolean isAddressedToMe = false;
			try {
				isAddressedToMe = isThisMyAddress(message.getReceiver());
			} catch (URLDescriptorException e) {
				println("error", "AbstactProcess.processEvent()", e);
	      notifyObservers(ML.EVENT_MESSAGE_IGNORED, message);
				return;
			}

      if (!isObserveMessages() && !isAddressedToMe) {
      	println("msg","Ignoring message "+message.getParameter(ML.PERFORMATIVE)+"/"+message.getParameter(ML.ACT)+" from "+message.getParameter(ML.SENDER));
	      notifyObservers(ML.EVENT_MESSAGE_IGNORED, message);
      	return;
      }
      
      //Check for authorization
      boolean authorizedMessage = authorizeMessage (message);
      if (!authorizedMessage) {
        // message not authenticated
        if (isLoggingTag("warning"))
          println ("warning", "Unauthorized message ignored:\n" + message.toString (true));
      	notifyObservers(ML.EVENT_MESSAGE_IGNORED, message);
        return;
      } 

      //if this is an exit message, it means the process is exiting.  Just dump the message and return success
      if (isAddressedToMe && ML.EXIT.equals(message.getParameter("act")) && exit) {
      	notifyObservers(ML.EVENT_MESSAGE_IGNORED, message);
        return;
      }

  	}
    
		handleEvent(event);
  }
  
  static protected int uniqueThreadName = 0;

  	
  /**
   * Checks for the pause state and wait()'s until interuped if paused.  The interupt
   * will normally be called by a setPause(false) or a step().
   */
    private void pauseCheck() {
      if (Thread.currentThread()==this) {
        if (pause) {
          println("info", "Paused.");
          try {
            synchronized (this) {
              wait();
            }
          }
          catch (InterruptedException ex1) {
            println("info", "Resumed.");
          }
        }
      }
    }
  
    /**
     * Set to true to pause the agent at the next message send or recieve
    */
    private boolean pause = false;
  
    /**
     * Determines if the agent is currently in the pause state.
     * @see #setPause(boolean)
     * @return true iff the agent is currently in pause state (but could be active
     * pending a processing the next message).
     */
    public boolean isPaused() {
      return pause;
    }
  
    /**
     * Set or clear the agent pausing immediately after processing the next message send
     * or recieve.  The agent can be resumed by passing <em>false</em> as the argument
     * or by calling <em>step()</em>.
     * @param val
     */
    public void setPause(boolean val) {
      if (val && !pause) {
        pause = true;
      }
      else if (!val && pause) {
        pause = false;
        this.interrupt();
      }
    }
  
    /**
     * If the agent is is the pause state, activates (resumes) the agent until processing the
     * next sent or recieved message.
     */
    public void step() {
      if (pause) this.interrupt();
    }
  
  /**
   * Called periodically from {@link #eventBufferLoopBody()}.  Subclasses may
   * override to add fucntionality.  This method does nothing.
   */
  @Deprecated
  protected void eventBufferLoopPeriodic () {
  }
  
  /**
   * Subclasses may override this method to override default processing of
   * unhandled messages.  The default behaviour is just to log the unhandled
   * message.
   * @param message the unhandled message.
   */
  protected void unhandledMessage(MLMessage message) {
    if (isLoggingTag("warning")) println("warning", "Message received but not handled:\n" +
                message.toString(true));
//    deleted rck 2011/11/30 - this functionality is now handled by a last-resort policy.
//    MLMessage reply = MLMessage.constructReplyTo(message, getUniqueRequestID(),getURL());
//    reply.setParameter(ML.PERFORMATIVE, ML.NOT_UNDERSTOOD);
//    Status tempStatus = sendMessage(reply);
//    if (tempStatus.getStatusValue() != 0) {
//      DEBUG.PRINT(tempStatus.getExplanation());
//      println("msg", tempStatus.getExplanation());
//    }
  }
  
  /**
   * Returns a unique string that can be conveniently used in the <em>reply-with</em> and
   * <em>in-reply-to</em> fields of a message.
   * @return a unique <em>String</em>
   */
  @Override
	public synchronized String getUniqueRequestID() {
    return getURL().toString(null) + "--" + Integer.toString(reqID++);
  }
  
  /**
   * Returns the port that this agent is listening on.
   * @return an <em>int</em> port number
   */
  @Override
	public int getPort() {
    return url.getPort();
  }
  
  /**
   * Method to close the module's open port and return it to the system.
   */
  @Override
	public void closePort () {
    in("AbsractProcess.closePort");
    if (socketServer!=null)
    	socketServer.closePort ();
    portOpen = false;
    //notifyObservers(new casa.State(ObservableEvent.STATE_CLOSE_PORT));
    url.setChannel(null);
    notifyObservers(ML.EVENT_CLOSE_PORT, null);
    out("AbsractProcess.closePort");
  }
  
  /**
   * Tests if this process has any open ports.
   * @return <code>true</code> if and only if the process has an open port;
   * <code>false</code> otherwise.
   */
  @Override
	public boolean hasOpenPort() {
    return portOpen;
  }
  
  /**
   * Part of the <em>Observable</em> interface from the observer pattern (Gamma et al.).
   * Call to add an observer to this Observable's list of Observers.  Observers
   * will be notified by calling their <em>update()</em> method when this object.
   * changes.
   * @param observer the Observer object to add.
   */
  @Override
	public synchronized void addObserver(Observer observer) {
    observerDelegate.addObserver(observer);
//    if (trace!=null)
//    	trace.addLocalObserver(observer);
  }
  
  /**
   * Extension to the <em>Observable</em> interface from the observer pattern (Gamma et al.).
   * Call to add a remote agent observer to this Observable's list of remote Observers.  Observers
   * will be notified by send them an <em>update()</em> message when this object
   * changes.
   * @param observer the Observer object to add.
   */
  @Override
	public synchronized void addObserver(URLDescriptor observer) {
    observerDelegate.addObserver(observer);
  }
  
  /**
   * Part of the <em>Observable</em> interface from the observer pattern (Gamma et al.).
   * Call to remove an observer from this object's list of observers.
   * @param observer the Observer object to delete from the list of observers
   */
  @Override
	public synchronized void deleteObserver(Observer observer) {
    observerDelegate.deleteObserver(observer);
  }
  
  /**
   * Extension to the <em>Observable</em> interface from the observer pattern (Gamma et al.).
   * Call to remove a remote observer from this object's list of remote observers.
   * @param observer the URL of the observer to delete from the list of observers
   */
  @Override
	public synchronized void deleteObserver(URLDescriptor observer) {
    observerDelegate.deleteObserver(observer);
  }
  
	
  @Override
	public void notifyObservers(String eventType, Object argument){
	  try {
		observerDelegate.notifyObservers(eventType, argument);
	  } catch (Exception e) {
	  	e.printStackTrace();
	  }
  }
  
  @Override
	public boolean equals(Object other) {
  	if (other instanceof ProcessInterface) {
  		return getURL().equals(((ProcessInterface)other).getURL());
  	}
  	else return false;
  }
  
  /**
   * Creates a basic message with the given performative, act, and reciever.
   * Then it automatically adds sender, reply-with, and reply-by parameters to
   * the message.
   *
   * @param performative The performative of the new message.
   * @param act The act of the new message.
   * @param reciever The reciever of the new message.
   * @return The new message with the given performative, act, and reciever and
   * correct sender, reply-with, and reply-by fields.
   */
  @Override
	public MLMessage getNewMessage (String performative, String act,
                                  URLDescriptor reciever) {
    in("AbsractProcess.getNewMessage");
    String id = getUniqueRequestID();
    String m[];
    if (act==null)
    					m = new String[]
    					   {ML.PERFORMATIVE,    performative,
                  ML.SENDER,          getURL().toString(reciever),
                  ML.RECEIVER,        reciever.toString(getURL()),
                  ML.REPLY_WITH,      id,
                  ML.CONVERSATION_ID, id,
                  ML.REPLY_BY,        Long.toString(System.currentTimeMillis() + options.defaultTimeout)
                  };
    else
    					m = new String[]
    					   {ML.PERFORMATIVE,    performative,
                  ML.ACT,             act,
                  ML.SENDER,          getURL().toString(reciever),
                  ML.RECEIVER,        reciever.toString(getURL()),
                  ML.REPLY_WITH,      id,
                  ML.CONVERSATION_ID, id,
                  ML.REPLY_BY,        Long.toString(System.currentTimeMillis() + options.defaultTimeout)
                  };
    MLMessage message = null;
    try {
      message = MLMessage.getNewMLMessageType(MLMessage.getMarkupLanguage(), m);
    }
    catch (Exception ex) {
      println("warning","getNewMessage: unexpected exception",ex);
    }
    out("AbsractProcess.getNewMessage");
    return message;
  }
  
  /**
   * Creates a basic message with the given performative, act, and reciever.
   * Then it automatically adds sender, reply-with, and reply-by parameters to
   * the message.
   *
   * @param performative The performative of the new message.
   * @param act The act of the new message.
   * @param reciever The reciever of the new message.
   * @param list an array of strings: odd indicis are taken as keys and even as values
   * @return The new message with the given performative, act, and reciever and
   * correct sender, reply-with, and reply-by fields.
   */
  public MLMessage getNewMessage (String performative, String act,
                                  URLDescriptor reciever, String... list) {
    in("AbsractProcess.getNewMessage");
    MLMessage message = getNewMessage(performative, act, reciever);
    message.setParameters(list);
    out("AbsractProcess.getNewMessage");
    return message;
  }

  /**
   * Turn tracing on / off.
   * @param doTrace true to turn tracing on, false to turn tracing off
   */
  @Override
	public void setTracing(boolean doTrace){
    if(options.tracing != doTrace){
      options.tracing = doTrace;
//      if(options.tracing && (trace == null)) trace = new Trace(this,getURL().getFile(),0,Trace.LONG_TIMESTAMP_FORMAT);
      try {
        trace.setLogToFile(true);
        resetTraceOptions();
      }
      catch (IOException ex1) {
      	Trace.log("error", "Cannot start trace on "+getURL().getFile()+".log", ex1);
      }
    }
  }

  /**
   * Is tracing on?
   * @return true iff tracing is on
   */
  @Override
	public boolean isTracing(){ return options.tracing;}

//  /**
//   * replace / set the current Trace object.  If the new trace is null, then tracing is
//   * automatically turned off.
//   * @param newTrace a reference to a new Trace object or null
//   */
//  public void setTrace(Trace newTrace){
//    if(newTrace == null) options.tracing = false; // automatically turn tracing off if no trace set...
//    trace = newTrace;
//  }

 /**
  * get the associated {@link Trace} object.  If isTracing() == true, then this is guaranteed
  * non-null, otherwise, this may return null.
  * @return a reference to this agent's Trace object
  */
  @Override
	public Trace getTrace(){ return trace;}

  /**
   * Returns true if the process is logging tag, tag is "error," or tag is null
   * @param tag The tag in question of being traced or not.
   */
  @Override
	public boolean isLoggingTag(String tag) {
    return (tag==null || tag.equals("error") || trace.isLoggingTag(tag));
  }

  /**
   * Set the use of message priorities.
   * @param newVal true to use message queue priorities; false to use FIFO.
   */
  public void setUsePriority(boolean newVal){
    options.usePriority = newVal;
    eventQueue.setUsePriority(newVal);
  }

 /**
  * Get the state of using message queue priorities.
  * @return true if using message queue priorities; false if using FIFO
  */
  public boolean getUsePriority() { 
  	return eventQueue.getUsePriority();
  }

  /**
   * Set whether the agent will ignore or process messages that are not
   * addressed to it (broadcaste messages never ignored).
   * @param newVal true to have the agent process messages not addressed to it.
   */
  public void setObserveMessages(boolean newVal){
    options.observeMessages = newVal;
  }

  /**
   * @return true iff the agent is processing messages not addressed to it.
   */
  public boolean isObserveMessages(){
    return options.observeMessages;
  }

  /**
   * Display the trace monitor window.  It will load with any history if trace is
   * already on.  If trace is off, it will be turned on.
   */
  @Override
	public void startTraceMonitor() {
    if (!isTracing()) setTracing(true);
    TraceMonitor m = new TraceMonitor(getURL().getFile(),true,false);
    m.update(observerDelegate,trace.getHistory());
//    trace.addLocalObserver(m);
    addObserver(m, "event_trace");
  }

  protected void resetSecurityPackage(String pack) {
  	if (pack==null || "none".equalsIgnoreCase(pack) || pack.length()==0) 
  		securityFilter = null;
  	else 
	  	try {
				Class<? extends SecurityFilterInterface> filterClass = Class.forName(pack+".SecurityFilter").asSubclass (SecurityFilterInterface.class);
				Constructor<? extends SecurityFilterInterface> constructor = filterClass.getConstructor(new Class[]{});
				securityFilter = constructor.newInstance(new Object[]{});
			} catch (Exception e) {
				println(null,"AbstractProcess.resetSecurityPackage", e);
			}

		if (securityFilter!=null) {
			securityFilter.setSecurityLevel(getOptions().security_level);
      securityFilter.requireIncomingSigning(getOptions().security_requireIncommingSigning);
      securityFilter.setDefSignatureAlgorithm(getOptions ().security_defSignatureAlgorithm);
		}
  }
  
  protected String getEventQueue() {
  	return eventQueue.toString();
  }

	@Override
	public int countObservers() {
		return observerDelegate.countObservers();
	}

	@Override
	public void deleteObservers() {
		observerDelegate.deleteObservers();
	}

	@Override
	public boolean hasChanged() {
		return observerDelegate.hasChanged();
	}

	@Override
	public void notifyObservers() {
		observerDelegate.notifyObservers();
	}

	@Override
	public void notifyObserversWithTop(Object arg) {
		observerDelegate.notifyObserversWithTop(arg);
	}

	@Override
	public void addObserver(Observer o, String... notifyTypes) {
		observerDelegate.addObserver(o, notifyTypes);
	}

	@Override
	public void addObserver(URLDescriptor observer, String... notifyTypes) {
		observerDelegate.addObserver(observer, notifyTypes);
	}

	@Override
	public void notifyObserversWithNoArg(String notifyType) {
		observerDelegate.notifyObserversWithNoArg(notifyType);		
	}
	
	/** wakes up the agent if it is sleeping waiting for an event to happen */
	@Override
	public synchronized void bump() {
		if (waitingForEvent) 
			interrupt();
	}
	
	//standard observer
	@Override
	public void update(Observable o, Object arg) {
		bump();
	}
	
	public class Subthread extends Thread {
		private AbstractProcess agent = null;
//		Subthread(AbstractProcess agent, ThreadGroup threadGroup, Runnable runnable, String name) {
//			super(threadGroup, runnable, name);
//		}
		protected Subthread(AbstractProcess agent, Runnable runnable, String name) {
			super(agent.getThreadGroup(), runnable, name);
			assert agent!=null;
			assert !getAgentName().equals(name);
			this.agent = agent;
		}
		public AbstractProcess getAgent() {
			return agent;
		}
	}
	
  /**
   * Creates a new thread in the same {@link ThreadGroup} as this agent, with the agent
   * specified.  Don't forget to call start() on the thread.
   * @param runnable The runnable to execute.
   * @param name The name of he new Thread.
   * @return the new thread
   */
  public Thread makeSubthread(Runnable runnable, String name) {
  	return new Subthread(this, runnable, ((Thread)this).getName()+"-subthread"+(subthreadID++)+(name==null?"":("-"+name)));
  }
  
	private long subthreadID = 0;
	
  /**
   * Creates a new thread in the same {@link ThreadGroup} as this agent, with the agent
   * specified.  Don't forget to call start() on the thread.
   * @param runnable The runnable to execute.
   * @return the new thread
   */
	public Thread makeSubthread(Runnable runnable) {
  	return new Subthread(this, runnable, ((Thread)this).getName()+"-subthread"+(subthreadID++));
  }
  
  static public AbstractProcess getAgent() {
  	Thread thisThread = Thread.currentThread();
  	if (thisThread instanceof Subthread)
  		return ((Subthread)thisThread).getAgent();
  	if (thisThread instanceof AbstractProcess)
  		return (AbstractProcess)thisThread;
  	else
  		return CASAUtil.getAbstractProcessInScope();
  	}

  /**
   * Lisp operator: (SHOW-EVENTQUEUE)<br>
   * Show the agent's events on the event queue.
   */
	@SuppressWarnings("unused")
	private static final CasaLispOperator AGENT__SHOW_EVENT_QUEUE =
		new CasaLispOperator("AGENT.SHOW-EVENT-QUEUE", "\"!Show the events on the event queue, returning a count.\" "
				, TransientAgent.class, new Object() { }.getClass().getEnclosingClass(), "SEQ")
	{
		@Override public Status execute (TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
			int count = 0;
			for (Event e: agent.eventQueue) {
				ui.println(e.toString());
				count++;
			}
			
			return new StatusObject<Integer>(0,count);
		}
	};
	
	 /**
   * Lisp operator: (GET-HOST-NAMES)<br>
   * Show the host names for this computer.
   */
	@SuppressWarnings("unused")
	private static final CasaLispOperator GET_HOST_NAMES =
		new CasaLispOperator("GET-HOST-NAMES", "\"!Show the host names for this computer.\" "
				, TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
	{
		@Override public Status execute (TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
			String names[] = CASAUtil.getMyHostNames();
			LispObject cons = org.armedbear.lisp.Lisp.NIL;
			for (String s: names) {
				cons = new Cons(new SimpleString(s), cons);
			}
			return new StatusObject<LispObject>(0,cons);
		}
	};
	
	 /**
  * Lisp operator: (GET-INETADDRESSES)<br>
  * Show the host names for this computer.
  */
	@SuppressWarnings("unused")
	private static final CasaLispOperator GET_INETADDRESSES =
		new CasaLispOperator("GET-INETADDRESSES", "\"!Show all the InetAddresses for this computer.\" "
				, TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
	{
		@Override public Status execute (TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
			InetAddress addrs[] = CASAUtil.getMyInetAddresses();
			LispObject cons = org.armedbear.lisp.Lisp.NIL;
			for (InetAddress a: addrs) {
				cons = new Cons(new SimpleString(a.toString()), cons);
			}
			return new StatusObject<LispObject>(0,cons);
		}
	};
	
	@Override
	public int compareTo(AbstractProcess o) {
		return Integer.signum(o.hashCode()-this.hashCode());
	}
	
	protected abstract SocialCommitment getChosenCommitment();
	
	protected abstract boolean hasActiveCommitments();
	
	protected abstract SocialCommitment processCommitment(SocialCommitment sc);

	@Override
	public int setTraceTags(String tags) {
		return trace==null ? 0 : trace.setTraceTags(tags);
	}

	@Override
	public int removeTraceTags(String tags) {
		return trace==null ? 0 : trace.removeTraceTags(tags);
	}

	@Override
	public String getTraceTags() {
		return trace==null ? null : trace.getTraceTags();
	}

	@Override
	public int addTraceTags(String tags) {
		return trace==null ? 0 : trace.addTraceTags(tags);
	}

	@Override
	public String println(String traceTag, String txt, Throwable ex, int flags) {
		if (options!=null && !options.tracing)
			return txt;
		if (trace!=null)
			return trace.println(traceTag, txt, ex, flags);
		else 
			return Trace.log(traceTag, txt, ex, flags);
	}

  /**
	 * Debugging or error method: Uses the {@link Trace} object to log the
	 * string if appropriate (ie: the traceTAg matches a tag that's turned on
	 * in the Trace object.
	 * @param traceTag The <em>txt</em> String will be logged if the traceTag matches a tag that's turned on in the Trace object.
	 * @param txt The String to be logged
	 * @return The string actually printed to the log (without the time stamp) or null if nothing is printed because of the tracetag
	 */
	@Override
	public String println(String traceTag, String txt) {
		if (options!=null && !options.tracing)
			return txt;
		if (trace!=null)
			return trace.println(traceTag, txt);
		else 
			return Trace.log(traceTag, txt);
	}
  
  /**
	 * Same as {@link #println(String, String)} but appends ex.toString() and prints
	 * a stack trace after.
	 * @param traceTag The <em>txt</em> String will be logged if the traceTag matches a tag that's turned on in the Trace object.
	 * @param txt The String to be logged
	 * @param ex An Exception object
	 * @return The string actually printed to the log (without the time stamp) or null if nothing is printed because of the tracetag
	 */
	@Override
	public String println(String traceTag, String txt, Throwable ex) {
		if (options!=null && !options.tracing)
			return txt;
		if (trace!=null)
			return trace.println(traceTag, txt, ex);
		else 
			return Trace.log(traceTag, txt, ex);
	}

  /**
	 * Same as {@link #println(String, String)} but appends tempStatus.getExplanation().
	 * @param traceTag The <em>txt</em> String will be logged if the traceTag matches a tag that's turned on in the Trace object.
	 * @param txt The String to be logged
	 * @param tempStatus A Status object
	 * @return The string actually printed to the log (without the time stamp) or null if nothing is printed because of the tracetag
	 */
	@Override
	@Deprecated
	public String println(String traceTag, String txt, Status tempStatus) {
	  if (trace!=null) {
	    if (isLoggingTag(traceTag))
	      return println(traceTag, txt + ":\n  (" + String.valueOf(tempStatus.getStatusValue()) + ")" + tempStatus.getExplanation());
	  }
	  return null;
	}

	@Override
	public String println(String traceTag, String txt, int flags) {
		if (options!=null && !options.tracing)
			return txt;
		if (trace!=null)
			return trace.println(traceTag, txt, flags);
		else 
			return Trace.log(traceTag, txt, flags);
	}

	@Override
	public void setAllTraceTags() {
		if (trace!=null)
			trace.setAllTraceTags();;
	}

	@Override
	public void clearAllTraceTags() {
		if (trace!=null)
			trace.clearAllTraceTags();;
	}

}