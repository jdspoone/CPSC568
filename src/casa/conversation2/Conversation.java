/**
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. 
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  
 * The  Knowledge Science Group makes no representations about the suitability
 * of  this software for any purpose.  It is provided "as is" without express
 * or implied warranty.</p>
 * <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
package casa.conversation2;

import casa.AbstractProcess;
import casa.CASAProcess;
import casa.ML;
import casa.MLMessage;
import casa.PerformDescriptor;
import casa.Status;
import casa.StatusObject;
import casa.TransientAgent;
import casa.abcl.CasaLispOperator;
import casa.abcl.LispException;
import casa.abcl.ParamsMap;
import casa.event.Event;
import casa.event.MessageEvent;
import casa.event.NonRecurringEvent;
import casa.exceptions.IllegalOperationException;
import casa.interfaces.PolicyAgentInterface;
import casa.policy.AbstractPolicy;
import casa.policy.Policy;
import casa.policy.PolicyContainer;
import casa.ui.AgentUI;
import casa.util.CASAUtil;
import casa.util.Pair;
import casa.util.PairComparable;
import casa.util.Runnable1;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

import org.armedbear.lisp.Cons;
import org.armedbear.lisp.Environment;
import org.armedbear.lisp.JavaObject;
import org.armedbear.lisp.LispObject;
import org.armedbear.lisp.SimpleString;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class Conversation extends BoundSymbolObject implements Comparable<Conversation> {
	
//	PerfActTransformation transformation = null;
	
	TransientAgent agent = null; // a null agent signifies this is a template conversation
	
	/* Used by TransientAgent: provides a fair lock (the lock used by synchronize is 
	 * not a fair lock) to be used to serialize events handled by this Conversation. 
	 */
	public ReentrantLock lock = new ReentrantLock(true);

	/**
	 * The list of all policies for the Conversation
	 */
	private PolicyContainer policies;
	
	private Action successAction;
	private Action failureAction;
	
	/**
	 * All conversations known in the current process -- not necessarily supported by any agent in the process.
	 */
	public static  Conversations knownConversations = new Conversations();
			
  /**
	 * @param conv the name of the conversation to return.
	 * @return Either the conversation if it's in {@link #knownConversations}, or <code>null</code> if the named <em>conv</em> can't be found. 
	 */
	public static Conversation findConversation(String conv) {return knownConversations.get(conv);}
	/**
	 * @return The list of all conversations in {@link #knownConversations}.
	 */
	public static Collection<Conversation> getKnownConversations() {return knownConversations.values();}
	
	private String id = null;
	public String getId() {return id;}
	
	@Override
	public void setName(String newName) {
		if (!newName.equals(getName()) && isTemplate()) {
			String oldName = name;
			name = newName;
			knownConversations.rename(oldName, newName);
		}
	}

	/**
	 * Conversation state
	 */
	public void setState(String state){
		bindVar("STATE",state);
	}

	protected void deleteSelf() {
		if (getParent() == null) { //only queue this Conversation for deletion if it's a top-level
			final Conversation conv2delete = this;
			agent.queueEvent(new NonRecurringEvent(ML.EVENT_EXECUTABLE, agent) {
				@Override
				public void fireEvent() {
					super.fireEvent();
					((AbstractProcess)agent).removeConversation(conv2delete);
				}
			});
		}
	}
	
  /* (non-Javadoc)
   * This method just adds a debugging trace
   * @see casa.conversation2.BoundSymbolObject#getVar(java.lang.String)
   */
  @Override
	public Object getVar(String sym) throws IllegalOperationException {
  	if (agent!=null && agent.isLoggingTag("boundSymbols")) {
  		agent.println("boundSymbols", "Conversation "+getName()+": Accessing "+symbols.reportSymbol(sym));
  	}
  	return super.getVar(sym);
  }

  /**
   * This method adds two functionalities to {@link BoundSymbolObject#bindVar(String, Object)}:
   * <ul>
   * <li> if <em>sym</em> is "STATE" and <em>val</em> is "terminated", queue this conversation for deletion.
   * <li> adds a debugging trace.
   * </ul>
   * @param sym the symbol to bind
   * @param val the object to bind to <em>sym</em>
   * @see casa.conversation2.BoundSymbolObject#bindVar(java.lang.String, java.lang.Object)
   */
  @Override
	public void bindVar(String sym, Object val) {
  	
  	// debugging trace code
  	StringBuilder b = null; 
  	if (agent!= null && agent.isLoggingTag("boundSymbols")) { //won't trace for a template conversation
  		b = new StringBuilder();
  		b.append("Updated symbol \"").append(sym).append("\" with value '").append(val).append("' from/to:\n")
  				.append(symbols.reportSymbol(sym)).append('\n');
  	}
  	
    // do the actual binding
  	super.bindVar(sym, val);
    
    // If we just changed the state to "terminated", queue this conversations for deletion
		if ("STATE".equals(sym) && "terminated".equals(val)) {
			deleteSelf();
		}

  	// debugging trace code
  	if (agent!= null && agent.isLoggingTag("boundSymbols")) {
			b.append(symbols.reportSymbol(sym));
  		agent.println("boundSymbols", "Conversation "+getName()+": "+b.toString());
  	}
  }

  /* (non-Javadoc)
   * This method just adds a debugging trace
   * @see casa.conversation2.BoundSymbolObject#bindVarExpression(java.lang.String, org.armedbear.lisp.LispObject)
   */
  @Override
	public void bindVarExpression(String sym, LispObject exp) {
  	super.bindVarExpression(sym, exp);
  	if (agent!=null && agent.isLoggingTag("boundSymbols")) {
  		agent.println("boundSymbols", "Conversation "+getName()+": Assigned symbol expression: "+symbols.reportSymbol(sym));
  	}
  }
  
  /* (non-Javadoc)
   * This method just adds a debugging trace
   * @see casa.conversation2.BoundSymbolObject#bindVarTo(java.lang.String, java.lang.String)
   */
  @Override
	public void bindVarTo(String sym, String parentSym) throws IllegalOperationException {
  	symbols.bindTo(sym, parentSym);
  	if (agent!=null && agent.isLoggingTag("boundSymbols")) {
  		agent.println("boundSymbols", "Conversation "+getName()+": bound symbol \""+sym+"\" to parent symbol \""+parentSym+"\": "+symbols.reportSymbol(sym));
  	}
  }
  
	public String getState() {
		try {
			return (String)getVar("STATE");
		} catch (IllegalOperationException e) {
			CASAUtil.log("error", "Can't find STATE binding in Conversation '"+this.getId()+"', returning 'unknown'", e, true);
			return "<unknown>";
		}
	}
	
	/**
	 * Creates a top-level conversation template.
	 * @param name
	 */
	public Conversation(String name) {
		super(name);
		policies = new PolicyContainer(agent==null?CASAProcess.getInstance().getStrategy():agent.getStrategy(), name);
  	knownConversations.put(name, this);
  	
  	//Initialize state, otherwise Lisp Conversation function throws exception - dsb
  	setState(null);
	}

  /**
   * Creates a child conversation template.
   * @param name
   * @param parent
   */
  public Conversation(String name, Conversation parent) {
  	super(name);
		policies = new PolicyContainer(agent==null?CASAProcess.getInstance().getStrategy():agent.getStrategy(), name);
  	setParent(parent);
  }
  
  /**
   * Constructor for a conversation instance. This constructor is protected because it should only be 
   * called from {@link #instantiate(TransientAgent, MessageEvent, Object, Object)}, which is the correct way
   * to instantiate a conversation <em>instance</em> from a conversation <em>template</em>.
   * 
   * @param name				the name of the lisp function defining the type of conversation we want
   * @param ownerAgent	the name of the registered agent that wants to have this conversation
   * @param message			the constructed KQML message containing the relevant data
   * @param bindings		string object pairs (Ex. ,['to',URL] and ['from',URL] information)
   * @throws IllegalOperationException
   */
  protected Conversation(String name, TransientAgent ownerAgent, MLMessage message, Pair<String,Object>... bindings) throws IllegalOperationException {
  	super(name);
  	assert name!=null;
  	assert ownerAgent!=null;
  	//MessageEvent event = new MessageEvent(ML.EVENT_MESSAGE_SENT, ownerAgent, message);
  	Conversation template = Conversation.findConversation(name);
  	if (template==null)
  		throw new IllegalOperationException("Converstion.constructor(): Can't find a Conversation template with name '"+name+"'");
  	try {
			template.copyTo(this);
		} catch (CloneNotSupportedException e) {
  		throw new IllegalOperationException("Converstion.constructor(): Unexpected CloneNotSupportedException");
		}
  	id = message.getParameter(ML.CONVERSATION_ID);
  	if (id==null) {
  		id = ownerAgent.getUniqueRequestID();
  		message.setParameter(ML.CONVERSATION_ID, id);
  	}
  	instantiateHelper(ownerAgent, new MessageEvent(ML.EVENT_MESSAGE_SENT,ownerAgent,message), template);
  	if (bindings!=null) {
  		for (Pair<String,Object> p: bindings) {
  			bindVar(p.getFirst(), p.getSecond());
  		}
  	}
  	ownerAgent.addConversation(id, this);
  	agent.sendMessage(message);
  }
  
  
  
  public Conversation instantiate(TransientAgent ownerAgent, MessageEvent event, Object overRideSuccessHandler, Object overRideFailureHandler) throws IllegalOperationException {
  	assert ownerAgent!=null;
  	assert getParent()==null: "Can't instantiate a child conversation";
  	if (overRideSuccessHandler!=null) {
  		if (!(overRideSuccessHandler instanceof String)) {
  			if (!(overRideSuccessHandler instanceof Runnable1<?,?>))
  				throw new IllegalOperationException("Parameter overRideSuccessHandler must be either a Runnable1<PerformDescriptor, MLMessage> or a String (representing Lisp code).");
  		}
  	}
  	if (overRideFailureHandler!=null) {
  		if (!(overRideFailureHandler instanceof String)) {
  			if (!(overRideFailureHandler instanceof Runnable1<?,?>))
  				throw new IllegalOperationException("Parameter overRideFailureHandler must be either a Runnable1<PerformDescriptor, MLMessage> or a String (representing Lisp code).");
  		}
  	}
  	
  	Conversation ret = null;

  	//check to see if there already exists a conversation for this case
		String conversationID = event.getMessage().getParameter(ML.CONVERSATION_ID);
		List<Conversation> existingConvs = ownerAgent.getConversation(conversationID);
		if (existingConvs!=null) {
		  for (Conversation c: existingConvs) {
		  	if (c.getName().equals(getName())) {
		  		c.agent.println("warning8", "(AGENT.INSTANTIATE_CONVERSATION "+getName()+" ...) called for an already existing instantiated conversation with the same id, \""+conversationID
		  				+"\". This may be normal if the conversation had self-started by sending it's own message.  Returning existing conversation.");
		  		ret = c;
		  	}
		  }
		}
  	
		if (ret==null) {
			try {
				ret = (Conversation)clone();
			} catch (CloneNotSupportedException e) {
				throw new IllegalOperationException("Conversation.instantiate(): Unexpected CloneNotSupportedException", e);
			}
		}
  	
  	ret.instantiateHelper(ownerAgent, event, this);
  	
		ownerAgent.addConversation(conversationID, ret);

  	ownerAgent.println("conversations","Conversation instantiated: "+ret.getName()+", id="+ret.getId());
  	
  	// policies are applied here, immediately after conversation instantiation, to allow initiation policies to take effect in the context of the both the conversation and the initiating message event.
  	try {
			StatusObject<List<Object>> stat = ret.applyPolicies(event);
			
		} catch (Throwable e) {
			throw new IllegalOperationException("Unexpected exception in Conversation.instantiate() applyPolicies",e);
		}
  	return ret;
  }
  
  @Override
  protected BoundSymbolObject copyTo(BoundSymbolObject x) throws CloneNotSupportedException {
  	super.copyTo(x);
  	if (x instanceof Conversation) {
  		Conversation c = (Conversation)x;
  		//c.agent = agent; // the agent souldn't be copied
  		//c.id = id; // the id shouldn't be copied
  		c.policies = policies;
  	}
  	return x;
  }
  
  /**
   * <ul>
   * <li> fix up all the children (that have already been cloned by {@link BoundSymbolObject#clone()})
   * <li> evaluates symbols
   * <li> sets the state to "init"
   * </ul>
   * @param ownerAgent
   * @param event
   * @param template
   * @throws IllegalOperationException
   */
  private void instantiateHelper(TransientAgent ownerAgent, MessageEvent event, Conversation template) throws IllegalOperationException {
  	agent = ownerAgent;
  	id = event.getMessage().getParameter(ML.CONVERSATION_ID);

  	// fix up all the children (that have already been cloned by BoundSymbolObject.clone()
  	if (template!=null) {
  		Iterator<Conversation> i = getChildConversations().iterator();
  		for (Conversation templateChild: template.getChildConversations()) {
  			assert templateChild.isTemplate();
  			Conversation myChild = i.next();
  			myChild.instantiateHelper(ownerAgent, event, templateChild);
  		}
  	}

  	// evaluate symbols
   	TreeMap<String,LispObject> map = new TreeMap<String,LispObject>();
   	map.put("EVENT", new JavaObject(event));
   	map.put("CONVERSATION", new JavaObject(this));
   	
   	//Testing... this should inform the policies contained herein of their parent conversation - dsb
   	bindVar("CONVERSATION", new JavaObject(this));
   	
  	symbols.evaluate(ownerAgent, null, map);
  	
   	setState("init");

//   	// clone, and fix up the policies (mappings, source files)
//  	policies = template.policies.clone();
//  	Map<String, LispObject> symMap = null;
//  	for (AbstractPolicy p: policies) {
//  		try {
//  		  symMap = getMap();
//				p.setMapping(symMap);
//			} catch (IllegalOperationException e) {
//				agent.println("error", "Conversation.instantiateHelper(): some symbols not instantiated.", e);
//			}
//  		p.setSourceFile((p.getSourceFile()==null?"":(p.getSourceFile()+", ")) + "Instantiated from "+p.getName());
//  	}
//  	if (policies.size()>0 && agent!= null && agent.isLoggingTag("boundSymbols")) { //won't trace for a template conversation
//	  	StringBuilder b = new StringBuilder();
//	  	b.append("Conversation ").append(getName()).append(": ").append("Committed Lisp symbols to policies ("+policies.size()+"):\n");
//	  	for (String key: symMap.keySet()) {
//			  b.append("  ").append(symbols.reportSymbol(key)).append('\n');
//	  	}
//	 		agent.println("boundSymbols", b.toString());
//	 	}				
  	
  }
    
	protected void setParent(Conversation parent) {
		super.setParent(parent);
	}
  
  @Override
	protected Conversation getParent() {
  	return (Conversation)super.getParent();
  }

	@Override
  protected void addChild(BoundSymbolObject child) {
		assert child instanceof Conversation;
		super.addChild(child);
	}

//	@Override
//  protected void addChild(BoundSymbolObject child, boolean hasParent) {
//		assert child instanceof Conversation;
//		super.addChild(child, hasParent);
//	}
	
	@Override
//	protected Conversation getChild(String childName) {
	public Conversation getChild(String childName) {
		return (Conversation)super.getChild(childName);
	}
	
	public Collection<Conversation> getChildConversations() {
		Vector<Conversation> ret = new Vector<Conversation>(); 
		for (BoundSymbolObject o: getChildren()) {
			if (o instanceof Conversation) ret.add((Conversation)o);
		}
		return ret;
	}
	
	/**
	 * @return true iff this conversation is a template conversation (ie: <em>agent</em>==null).
	 */
	public boolean isTemplate() {return agent==null;}

	public void addPolicy(AbstractPolicy policy) {
		policies.add(policy);
//		if (transformation!=null) {
//			policy.setTransformation(transformation);
//			if (policy.getAntecedent()!=null)
//			  policy.getAntecedent().getPattern().setTransformation(transformation);
//		}
	}
	/**adds a conversation as a sub conversation to this one
	 * 
	 * @param conv	the conversation to add as a child of this conversation
	 */
	public void addSubConversation(Conversation conv) {
		addChild(conv);
	}
	
//	public void addSubConversation(Conversation conv, boolean hasParent) {
//		addChild(conv, hasParent);
//	}
	
	/**
	 * Note that there is not "isApplicable(Event)" method in this class.  This is because
	 * this is an expensive operation that throws away a lot of effort that could be 
	 * used by {@link #applyPolicies(Event)}, so the information should be cached
	 * as in the following pattern: 
	 * <code><pre>
	 * ...
	 * Vector &lt AbstractPolicy > applicable = conv.{@link #getApplicablePolicies(Event) getApplicablePolicies(event)};
	 * if (applicable!=null) result = conv.{@link #applyPolicies(Event, Vector) applyPolicies(event,applicable)};
	 * ...
	 * @param event
	 * @return a Vector (in no particular order) or all the policies applicable to
	 * the <em>event</em>, including any in any sub-conversations (children).  
	 */
	public synchronized Vector<Pair<Conversation,AbstractPolicy>> getApplicablePolicies(PolicyAgentInterface agent, Event event) {
		assert this.agent==null || agent==this.agent;
		Vector<Pair<Conversation,AbstractPolicy>> ret = new Vector<Pair<Conversation,AbstractPolicy>>();
		agent.println("policies", "Conversation "+getName()+"("+getId()+" in state '"+getState()+"') searching for policies matching event: "+event);
		for (BoundSymbolObject childConv: children.values()) {
			Vector<Pair<Conversation,AbstractPolicy>> temp = ((Conversation)childConv).getApplicablePolicies(agent,event);
			if (temp!=null) ret.addAll(temp);
		}
		Map<String, LispObject> map =new TreeMap<String, LispObject>();
		map.put("conversation", new JavaObject(this));
		map.put("agent", new JavaObject(agent));
		map.put("event", new JavaObject(event));
		map.putAll(getSymbolMap());
		for (AbstractPolicy p: policies.findApplicable(agent, event, map)) 
			ret.add(new PairComparable<Conversation,AbstractPolicy>(this,p));
		agent.println("policies", "Conversation "+getName()+"("+getId()+") found "+ret.size()+" policies matching event: "+event.toString());
		if (ret.size()==0) ret = null;
		return ret;
	}
	
	/** getter method for the conversation's policies
	 * 
	 * @return	the policies that define the conversation's behaviour
	 */
	public PolicyContainer getPolicies() {
		return policies;
	}
	
	/**
	 * ***Warning: It's not very efficient to call this method.  In general, callers will
	 * be checking to see if policies are applicable first which is expensive, so the 
	 * caller should cache in information by doing it this way:
	 * <code><pre>
	 * ...
	 * Vector &lt AbstractPolicy > applicable = conv.{@link #getApplicablePolicies(Event) getApplicablePolicies(event)};
	 * if (applicable!=null) result = {@link #applyPolicies(Event, Vector) applyPolicies(event,applicable)};
	 * ...
	 * </pre></code>
	 * @param event
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("static-access") // the static access in the return statement needs to be there to assure we call the right static getApplicablePolicies() method.
	protected StatusObject<List<Object>> applyPolicies(Event event) throws Exception {
		assert agent!=null: "applyPolicies() cannot be called on a template (non-instantiated) Conversation.";
		Vector<Pair<Conversation,AbstractPolicy>> ps = getApplicablePolicies(agent, event);
		//Vector<Pair<Conversation,AbstractPolicy>> conv2polMap = ps==null ? null : new Vector<Pair<Conversation,AbstractPolicy>>(ps.size());
		Map<String, LispObject> map =new TreeMap<String, LispObject>();
		if (ps!=null) {
			map.put("agent", new JavaObject(agent));
			map.put("event", new JavaObject(event));
			map.put("conversation", new JavaObject(this));
		}
		StatusObject<List<Object>> ret = policies.applyPolicies(agent, ps, event, map); 
		return ret;
	}
	
	/**
	 * @return the symbol-to-value map for this conversations
	 */
	public Map<String, LispObject> getSymbolMap() {
		 try {
			return symbols.getMap();
		} catch (IllegalOperationException e) {
			agent.println("error", "Conversations.getSymbolMap: Unexpected exception", e);
		}
		return new TreeMap<String,LispObject>();
	}
	
//	/**
//	 * @param event The event to which the policy list should be applied.  They must all be applicable to this event.
//	 * The events will be sorted and filtered before applying.
//	 * @param policyList
//	 * @return
//	 * @throws Exception
//	 */
//	protected StatusObject<List<Object>> applyPolicies(Event event, Vector<AbstractPolicy> policyList) throws Exception {
//		return policies.applyPolicies(agent, policyList, event);
//	}

	@Override
	public String toString() {
		return toString(0, false);
	}
	
	public String toString(int indent, boolean brief) {
		StringBuilder b = new StringBuilder();
		CASAUtil.pad(b,indent)
		.append("(conversation \"")
		.append(getName())
		.append("\" ; ");
		if (agent==null) b.append("template\n");
		else {
			b.append("instantiated\n");
			CASAUtil.pad(b,indent+2);
			b.append("; agent=").append(agent.getAgentName()).append('\n');
			CASAUtil.pad(b,indent+2);
			b.append("; id=").append(id).append('\n');
			CASAUtil.pad(b,indent+2);
			b.append("; state=").append(getState()).append('\n');
		}		
		CASAUtil.pad(b,indent+2);
		if (getParent()==null) b.append("; root conversation");
		else {
			b.append("; parent conversation=").append(getParent().getName());
			if (getParent().getId()!=null) 
				b.append(", id=").append(getParent().getId());
		}
		b.append("\n");
		
		CASAUtil.pad(b, indent+2)
		.append("(list\n");
		
		// child conversations
		CASAUtil.pad(b, indent+4);
		Collection<Conversation> col = getChildConversations();
		if (col.size()==0)
			b.append("; no embedded conversations\n");
		else {
		  b.append("; embedded conversations\n");
		  for (Conversation c: getChildConversations()) {
			  b.append(c.toString(indent+4, brief));
		  }
		}
		
		// policies
		CASAUtil.pad(b, indent+4);
		if (policies.size()==0) {
			b.append("; no attached policies\n");
		}
		else {
			if (brief) {
				b.append("; ").append(policies.size()).append(" policies (not listed)\n");
			}
			else {
		    b.append("; policies\n");
		    for (AbstractPolicy p: policies) {
		  	  b.append(p.toString(indent+4));
		    }
			}
		}
		
		CASAUtil.pad(b, indent+2)
		.append(")\n");
		
//		if (transformation!=null)
//			CASAUtil.pad(b, indent+2).append(":transformation ").append(transformation.toString()).append('\n');

		CASAUtil.pad(b, indent+2).append(":class ").append(this.getClass().getName()).append('\n');
		
		// :bind-var
		String buf = symbols.toStringBound(indent+14);
		if (buf.length()>0) {
			CASAUtil.pad(b, indent+2)
			.append(":bind-var '(\n")
			.append(buf);
			CASAUtil.pad(b, indent+13)
			.append(")\n");
		}
		
		// :bind-var-to
		buf = symbols.toStringBoundTo(indent+17);
		if (buf.length()>0) {
			CASAUtil.pad(b, indent+2)
			.append(":bind-var-to '(\n")
			.append(buf);
			CASAUtil.pad(b, indent+16)
			.append(")\n");
		}
		
		// :bind-state
		buf = symbols.toStringBoundValue(indent+16, "STATE");
		if (buf.length()>0) {
			CASAUtil.pad(b, indent+2)
			.append(":bind-state '(\n")
			.append(buf);
			CASAUtil.pad(b, indent+15)
			.append(")\n");
		}
		
		CASAUtil.pad(b, indent+2)
		.append(")\n");
		return b.toString();
	}
	
	@Override
	public int compareTo(Conversation o) {
		int ret = getName().compareTo(o.getName());
		if (ret!=0) return ret;
		if (getId()!=null) ret = getId().compareTo(o.getId());
		return ret;
	}

	/**
	 * Desperate attempt to get the gc to clear objects when we think this one is dead.
	 */
	public void destroy() {
		super.destroy();
		agent = null;
		lock = null;
		policies.destroy();
		policies = null;
	}
	
	/**
	 * (Conversation ...)
	 */
  @SuppressWarnings("unused")
	private static final CasaLispOperator CONVERSATION =
    new CasaLispOperator("CONVERSATION", "\"!Declares a conversation.\" "
								+"NAME \"@java.lang.String\" \"!The name of the conversation.\" "
    						+"BINDINGS \"@org.armedbear.lisp.Cons\" \"!A Cons list of Lisp functions describing sub-conversations or policies.\" "
    						+"&KEY BIND-VAR \"@org.armedbear.lisp.Cons\" \"!A Cons list of pairs of symbol/values pairs (themselves Cons lists) that will be bound in the context of the conversation.  The expressions are evaluated at the time the conversation is created.\" "
    						+"BIND-VAR-TO \"@org.armedbear.lisp.Cons\" \"!A Cons list of triples of symbol/childConversation/childSymbol (themselves Cons lists) that will be bound in the context of the conversation.\" "
    						+"BIND-STATE \"@org.armedbear.lisp.Cons\" \"!A Cons list of triples of state/childConversation/childState (themselves Cons lists) that will be bound in the context of the conversation.\" "
    						+"CLASS \"@java.lang.String\" \"!The specific subclass of a Conversation.\" "
//    						+"TRANSFORMATION \"@casa.PerfActTransformation\" \"!Transformation object.\" "
    						, TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
    {
      @Override
			public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
       	Status stat = new Status();
      	
      	Vector<BoundConversationlet> bindings = new Vector<BoundConversationlet>();
      	
      	String name = (String)params.getJavaObject("NAME");
      	
      	Conversation conv;
      	String className = (String)params.getJavaObject("CLASS");
      	if (className==null)
      	  conv = new Conversation(name);
      	else {
      		try {
						Class<? extends Conversation> cls = (Class<? extends Conversation>)Class.forName(className);
						Constructor<? extends Conversation> cons = (Constructor<? extends Conversation>)cls.getConstructor(String.class);
						conv = cons.newInstance(name);
					} catch (Throwable e) {
						agent.println("error", "Failed to create Conversation \""+name+"\" of type \""+className+"\"", e);
						return null;
					}
      	}
      	
  			conv.setSourceFile((String)params.getJavaObject("__SOURCE_"));

      	
//      	PerfActTransformation trans = (PerfActTransformation)params.getJavaObject("TRANSFORMATION");
//      	if (trans!=null) {
//      		conv.transformation = trans;
//      	}
      	
  			for (Cons cons = (Cons)params.getLispObject("BINDINGS"); cons!=null && cons!=org.armedbear.lisp.Lisp.NIL; cons=(cons.cdr==org.armedbear.lisp.Lisp.NIL?null:(Cons)cons.cdr)) {
  				LispObject car = cons.car();

  				if (car instanceof JavaObject) {
  					JavaObject javaObj = (JavaObject)car;
  					Object obj = javaObj.getObject();
  					if (obj instanceof Policy) {
  						AbstractPolicy policy = (AbstractPolicy)obj;
  						conv.addPolicy(policy);
  					}
  					else if (obj instanceof Conversation) {
  						Conversation subConv = (Conversation)obj;
  						conv.addSubConversation(subConv);
  					}
  					else {
//    					return new Status(-1098,"CONVERSATION: unexpected type in BINDINGS list: "+car.writeToString());
    					agent.println("warning", "CONVERSATION "+name+": unexpected type (object "+obj+") in BINDINGS list: "+car.writeToString());
  					}
  				}
  				else {
//  					return new Status(-1,"CONVERSATION: unexpected type in BINDINGS list: "+car.writeToString());
  					if (car!=org.armedbear.lisp.Lisp.NIL)
  					  agent.println("warning", "CONVERSATION "+name+": unexpected non-JavaObject type (object "+car+") in BINDINGS list: "+car.writeToString());
  				}
  				
  			}
  			
  			//BIND-VAR
  			for (Cons cons = (Cons)params.getLispObject("BIND-VAR"); cons!=null && cons!=org.armedbear.lisp.Lisp.NIL; cons=(cons.cdr==org.armedbear.lisp.Lisp.NIL?null:(Cons)cons.cdr)) {
  				LispObject car = cons.car();
  				
  				if (car instanceof Cons) {
  					LispObject sym = ((Cons)car).car;
  					if (sym instanceof SimpleString) {
  						LispObject val = (((Cons)car).cdr).car();
  						conv.bindVarExpression(sym.getStringValue(),val);
  					}
  					else 
  					  throw new LispException("CONVERSATION "+name+" :BIND_VAR: unexpected non-string type (object "+sym+") in BIND-VAR list: "+sym.writeToString());
  				}
  				else 
					  throw new LispException("CONVERSATION "+name+" :BIND_VAR: unexpected non-dotted-list-pair type (object "+car+") in BIND-VAR list: "+car.writeToString());
  			}

  			//BIND-VAR-TO
  			for (Cons cons = (Cons)params.getLispObject("BIND-VAR-TO"); cons!=null && cons!=org.armedbear.lisp.Lisp.NIL; cons=(cons.cdr==org.armedbear.lisp.Lisp.NIL?null:(Cons)cons.cdr)) {
  				LispObject car = cons.car();
  				
  				if (car instanceof Cons && ((Cons)car).length()==3) {
  					LispObject sym = ((Cons)car).car;
  					if (sym instanceof SimpleString) {
  						if (((Cons)car).cdr instanceof Cons) {
  							LispObject cdr = ((Cons)car).cdr;
  							LispObject childName = ((Cons)cdr).car;
  							if (childName instanceof SimpleString) {
  	  						if (((Cons)cdr).cdr instanceof Cons) {
  	  							LispObject childSym = ((Cons)((Cons)cdr).cdr).car;
  	  							if (childSym instanceof SimpleString) {
  	  								try {
												conv.bindVarTo(sym.getStringValue(), childName.getStringValue(), childSym.getStringValue());
											} catch (IllegalOperationException e) {
												throw new LispException("CONVERSATION "+name+" :BIND-VAR-TO: Illegal Operation Exception for args: "+sym.writeToString()+" "+childName.writeToString()+" "+childSym.writeToString()+": "+e.toString());
											}
  	  							}
  	  	  					else throw new LispException("CONVERSATION "+name+" :BIND-VAR-TO: unexpected non-string type (object "+childSym+" of type "+childSym.getClass()+") in 3rd element (child symbol) of BIND-VAR-TO list: "+childSym.writeToString());
  	  						}
	  	  					else throw new LispException("CONVERSATION "+name+" :BIND-VAR-TO: unexpected *non-string type (object "+childName+" of type "+childName.getClass()+") in 2nd element (child-name) of BIND-VAR-TO list: "+childName.writeToString());
  							}
  	  					else throw new LispException("CONVERSATION "+name+" :BIND-VAR-TO: unexpected non-string type (object "+childName+" of type "+childName.getClass()+") in 2nd element (child-name) of BIND-VAR-TO list: "+childName.writeToString());
  						}
  					}
  					else 
  					  throw new LispException("CONVERSATION "+name+" :BIND-VAR-TO: unexpected non-string type (object "+sym+" of type "+sym.getClass()+") in 1st element (local symbol) of BIND-VAR-TO list: "+sym.writeToString());
  				}
  				else 
					  throw new LispException("CONVERSATION "+name+" :BIND-VAR-TO: unexpected non-triple-list type (object "+car+") in BIND-VAR-TO list: "+car.writeToString());
  			}

  			//BIND-STATE
  			for (Cons cons = (Cons)params.getLispObject("BIND-STATE"); cons!=null && cons!=org.armedbear.lisp.Lisp.NIL; cons=(cons.cdr==org.armedbear.lisp.Lisp.NIL?null:(Cons)cons.cdr)) {
  				LispObject car = cons.car();
  				
  				if (car instanceof Cons) {
  					LispObject state = ((Cons)car).car;
  					if (state instanceof SimpleString) {
  						if (((Cons)car).cdr instanceof Cons) {
  							LispObject cdr = ((Cons)car).cdr;
  							LispObject childName = ((Cons)cdr).car;
  							if (childName instanceof SimpleString) {
  	  						if (((Cons)cdr).cdr instanceof Cons) {
  	  							LispObject childState = ((Cons)((Cons)cdr).cdr).car;
  	  							if (childState instanceof SimpleString) {
  	  								try {
  	  									Conversation childConv = conv.getChild(childName.getStringValue());
  	  									if (childConv!=null) {
  	  										childConv.bindValue("STATE", childState.getStringValue(), state.getStringValue());
  	  									}
											} catch (IllegalOperationException e) {
												throw new LispException("CONVERSATION "+name+" :BIND-STATE: Illegal Operation Exception for args: "+state.writeToString()+" "+childName.writeToString()+" "+childState.writeToString()+": "+e.toString());
											}
  	  							}
  	  	  					else throw new LispException("CONVERSATION "+name+" :BIND-STATE: unexpected non-string type (object "+childState+") in BIND-STATE list: "+childState.writeToString());
  	  						}
	  	  					else throw new LispException("CONVERSATION "+name+" :BIND-STATE: unexpected non-string type (object "+childName+") in BIND-STATE list: "+childName.writeToString());
  							}
  	  					else throw new LispException("CONVERSATION "+name+" :BIND-STATE: unexpected non-string type (object "+state+") in BIND-STATE list: "+state.writeToString());
  						}
  					}
  					else 
  					  throw new LispException("CONVERSATION "+name+" :BIND-STATE: unexpected non-string type (object "+state+") in BIND-STATE list: "+state.writeToString());
  				}
  				else 
					  throw new LispException("CONVERSATION "+name+" :BIND-STATE: unexpected non-triple-list type (object "+car+") in BIND-STATE list: "+car.writeToString());
  			}

//  			agent.println(null,conv.toString());
  			
      	return new StatusObject<Conversation>(conv);
      }
  };
  
  /**
   * Lisp operator: (conversation.set-state)<br>
   * Sets the state of the current specified
   */
  @SuppressWarnings("unused")
	private static final CasaLispOperator CONVERSATION__SET_STATE =
    new CasaLispOperator("CONVERSATION.SET-STATE", "\"!Sets the state of the current conversation.\" "
    		+"STATE \"@java.lang.String\" \"!The new state.\" "
    		, TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
  	{
  	  @Override 
  	  public Status execute (TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
  	  	String state = (String)params.getJavaObject("STATE");
  	  	
  	    Conversation conv = null;
				try {
					//Is state supposed to map to a conversation?  It currently maps to a string
					//See conversation.get-state - dsb
					conv = (Conversation)casa.abcl.Lisp.lookupAsJavaObject(env, "CONVERSATION");
				} catch (ClassCastException e) {
    	  	return new Status(-1, "(CONVERSATION.SET-STATE \""+state+"\"): Conversation found in context is not of type Conversation.");
				}
				if (conv == null) 
    	  	return new Status(-2, "(CONVERSATION.SET-STATE \""+state+"\"): No Conversation found in context.");

  	  	if (state != null){
  	  		conv.setState(state);
  	  		return new Status(0);
  	  	}
  	  	return new Status(-3, "(CONVERSATION.SET-STATE NIL): Can't set state to NIL.");
  	  }
  	};
	
    /**
     * Lisp operator: (conversation.set-state)<br>
     * Sets the state of the current specified
     */
    @SuppressWarnings("unused")
  	private static final CasaLispOperator CONVERSATION__SET_ACTION =
      new CasaLispOperator("CONVERSATION.SET-ACTION", "\"!Sets one or more actions of the current conversation.\" "
      		+"&KEY SUCCESS \"@java.lang.String\" \"!The success action.\" "
      		+"FAILURE \"@java.lang.String\" \"!The failure action.\" "
      		, TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
    	{
    	  @Override 
    	  public Status execute (TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
    	    Conversation conv = null;
  				try {
  					//Is state supposed to map to a conversation?  It currently maps to a string
  					//See conversation.get-state - dsb
  					conv = (Conversation)casa.abcl.Lisp.lookupAsJavaObject(env, "CONVERSATION");
  				} catch (ClassCastException e) {
      	  	return new Status(-1, "(CONVERSATION.SET-ACTION ...): Conversation found in context is not of type Conversation.");
  				}
  				if (conv == null) 
      	  	return new Status(-2, "(CONVERSATION.SET-ACTION ...): No Conversation found in context.");

    	  	String success = (String)params.getJavaObject("SUCCESS");
    	  	if (success!=null) {
    	  		conv.successAction = new LispAction(conv, success);
    	  	}
    	  
    	  	String failure = (String)params.getJavaObject("FAILURE");
    	  	if (failure!=null) {
    	  		conv.failureAction = new LispAction(conv, failure);
    	  	}
    	  
    	  	return new Status(0);
    	  }
    	};
  	
      /**
       * Lisp operator: (conversation.set-state)<br>
       * Sets the state of the current specified
       */
      @SuppressWarnings("unused")
    	private static final CasaLispOperator CONVERSATION__EXECUTE_ACTION =
        new CasaLispOperator("CONVERSATION.EXECUTE-ACTION", "\"!Executes ONE of the current conversation's actions.\" "
        		+"EVENT \"@casa.event.Event\" \"!The event in scope,\" "
        		+"&KEY SUCCESS \"@java.lang.Boolean\" \"!The success action.\" "
        		+"FAILURE \"@java.lang.Boolean\" \"!The failure action.\" "
        		, TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
      	{
      	  @Override 
      	  public Status execute (TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
      	    Conversation conv = null;
    				try {
    					//Is state supposed to map to a conversation?  It currently maps to a string
    					//See conversation.get-state - dsb
    					conv = (Conversation)casa.abcl.Lisp.lookupAsJavaObject(env, "CONVERSATION");
    				} catch (ClassCastException e) {
        	  	return new Status(-1, "(CONVERSATION.SET-ACTION ...): Conversation found in context is not of type Conversation.");
    				}
    				if (conv == null) 
        	  	return new Status(-2, "(CONVERSATION.SET-ACTION ...): No Conversation found in context.");

      	  	boolean success = (Boolean)params.getJavaObject("SUCCESS");
      	  	boolean failure = (Boolean)params.getJavaObject("FAILURE");
      	  	
      	  	if (count(success, failure)!=1) {
      	  		throw new LispException("(CONVERSATION.EXECUTE-ACTION ...) must contain exactly one true key.");
      	  	}
      	  	
      	  	Event event = (Event)params.getJavaObject("EVENT");
      	  	if (event==null) {
      	  		throw new LispException("(CONVERSATION.EXECUTE-ACTION ...): EVENT parameter must contain a casa.event.Event object.");
      	  	}
      	  	
      	  	if (success)
      	  		return conv.successAction.execute(event);
      	  	if (failure)
      	  		return conv.failureAction.execute(event);
      	  
      	  	return new Status(0);
      	  }
      	};
      	
    private static int count(Boolean... b) {
    	int count = 0;
    	for (boolean bool: b) {
    		if (bool)
    			count++;
    	}
    	return count;
    }
    	
    String sourceFile = null;
    	
  	protected void setSourceFile(String sourceFile) {
	    this.sourceFile = sourceFile; 
    }
  	
  	public String getSourceFile() {
  		return sourceFile;
  	}

  	/**
     * Lisp operator: (conversation.get-state)<br>
     * Gets the state of the conversation specified
     */
    @SuppressWarnings("unused")
  	private static final CasaLispOperator CONVERSATION__GET_STATE =
      new CasaLispOperator("CONVERSATION.GET-STATE", "\"!Gets the state of the current conversation.\" "
      		, TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
    	{
    	  @Override 
    	  public Status execute (TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
    	  	
    	  	Conversation conv = (Conversation)casa.abcl.Lisp.lookupAsJavaObject(env, "CONVERSATION");
    	  	String state = conv.getState();
    	  	
    	  	if (state != null){
    	  		return new StatusObject<String>(state);
    	  	}
    	  	return new Status(-1, "(CONVERSATION.GET-STATE): No Conversation found in context.");
    	  }
    	};
	
}
