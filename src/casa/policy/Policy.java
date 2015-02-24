/**
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. 
 * Permission to use, copy, modify, distribute and sell this software and its 
 * documentation for any purpose is hereby granted without fee, provided that the 
 * above copyright notice appear in all copies and that both that copyright notice 
 * and this permission notice appear in supporting documentation.  The  Knowledge 
 * Science Group makes no representations about the suitability of  this software 
 * for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
package casa.policy;

import casa.Act;
import casa.ML;
import casa.Status;
import casa.StatusObject;
import casa.TransientAgent;
import casa.abcl.CasaLispOperator;
import casa.abcl.LispException;
import casa.abcl.ParamsMap;
import casa.agentCom.URLDescriptor;
import casa.event.Event;
import casa.event.EventDescriptor;
import casa.event.MessageEvent;
import casa.exceptions.LispParseException;
import casa.interfaces.PolicyAgentInterface;
import casa.policy.sc3.PolicyConsequent;
import casa.policy.sc3.PolicyConsequentInterface;
import casa.socialcommitments.Action;
import casa.socialcommitments.SocialCommitmentDescriptor;
import casa.socialcommitments.operators.AddDependentSocialCommitment;
import casa.socialcommitments.operators.AddSocialCommitment;
import casa.socialcommitments.operators.CancelSocialCommitment;
import casa.socialcommitments.operators.Condition;
import casa.socialcommitments.operators.FulfilSocialCommitment;
import casa.socialcommitments.operators.SocialCommitmentOperator;
import casa.ui.AgentUI;
import casa.ui.BufferedAgentUI;
import casa.util.CASAUtil;
import casa.util.InstanceCounter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.armedbear.lisp.Cons;
import org.armedbear.lisp.ControlTransfer;
import org.armedbear.lisp.Environment;
import org.armedbear.lisp.JavaObject;
import org.armedbear.lisp.LispObject;
import org.armedbear.lisp.Symbol;

/**
 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @author  <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 */
public class Policy implements AbstractPolicy {

	private static int idSequence = 0;
	private int id;
	String doc;
	private String sourceFile=null;
	public void setSourceFile(String s) {sourceFile=s;}
	public String getSourceFile() {return sourceFile;}
	private String name="";
	public void setName(String name) {this.name=name;}

	/**
	 */
	private EventDescriptor antecedent = null;
	public void setAntecedent(EventDescriptor antecedent) {
		this.antecedent = antecedent;
	}
	public void setAntecedent(String antecedent) {
		EventDescriptor eventDescriptor = null;
		Cons eventDescriptorCons = null;
		Status stat = casa.abcl.Lisp.abclEval(null, null, null, "'"+antecedent, null);
		if (stat instanceof StatusObject<?>) {
			Object obj = ((StatusObject<?>)stat).getObject();
			if (obj instanceof EventDescriptor) {
				eventDescriptor = (EventDescriptor)obj;
			}
			else if (obj instanceof Cons) {
				eventDescriptorCons = (Cons)obj;
			}
		}		
		if (eventDescriptor!=null) {
			setAntecedent(eventDescriptor);
		} else { 
			setUnevaluatedAntecedent(eventDescriptorCons);
		}
	}
	public EventDescriptor getAntecedent() {
		return antecedent;
	}

	private Cons unevaluatedAntecedent = null;
  protected void setUnevaluatedAntecedent(Cons eventDescriptorCons) {
		this.unevaluatedAntecedent = eventDescriptorCons;
	}
	public Cons getUnevaluatedAntecedent() {
		return unevaluatedAntecedent;
	}

	private Cons precondition = null;
	public Cons getPrecondition() {
		return precondition;
	}
	public void setPrecondition(Cons precondition) {
		this.precondition = precondition;
	}
	public void setPrecondition(String precondition) throws IllegalArgumentException {
		setPrecondition(toCons(precondition));
	}
	
	private Cons toCons(String precondition) throws IllegalArgumentException {
		Status stat = casa.abcl.Lisp.abclEval(null, null, null, "'"+precondition, null);
		if (stat instanceof StatusObject<?>) {
			Object obj = ((StatusObject<?>)stat).getObject();
			if (obj instanceof Cons) {
				return (Cons)obj;
			}
		}
		throw new IllegalArgumentException("Policy.toCons(\""+precondition+"\"): Cannot interpret arg expression as a Cons list.");
	}

	private Cons postcondition = null;
	public Cons getPostcondition() {
		return postcondition;
	}
	public void setPostcondition(Cons postcondition) {
		this.postcondition = postcondition;
	}
	public void setPostcondition(String postcondition) {
		setPostcondition(toCons(postcondition));
	}

	private List<PolicyConsequentInterface> consequents = new LinkedList<PolicyConsequentInterface>();
	/**allows for the setting of consequents using a list of PolicyConsequentInterface(s)
	 * 
	 * @param consequents	the list of consequents as a list of PolicyConsequentInterface(s)
	 */
	public void setConsequents(List<PolicyConsequentInterface> consequents) {
		this.consequents = consequents;
	}
	/**allows for setting consequents using a list of Strings
	 * 
	 * @param consequents		the list of consequents as strings
	 */
	public void setConsequentsByStrings(List<String> consequents) {
		List<PolicyConsequentInterface> cons = new LinkedList<PolicyConsequentInterface>();
		for(String s : consequents){
			cons.add(new PolicyConsequent(toCons(s)));
		}
		setConsequents(cons);
	}
	/**get method for this policy's consequents
	 * 
	 * @return the list of consequents
	 */
	public List<PolicyConsequentInterface> getConsequents(){
		return consequents;
	}
	
	/**
	 * @see #isGhost()
	 */
	private boolean ghost = false;
	
	/**
	 * A ghost policy is a policy that shouldn't be counted as being applied.  For example, 
	 * the global policy that blindly tries to filfill a social commitment to send that message
	 * on receiving or sending that message is a ghost; it doesn't do much (and usually doesn't
	 * do anything at all), and would want to consider it the message unhandled if only
	 * this policy applied.  
	 * @return true if this policy is a ghost.
	 */
	public boolean isGhost() {
		return ghost;
	}

	/**
	 * Declares this policy a ghost.  It isn't by default.
	 * @see #isGhost()
	 */
	public void setGhost() {
		ghost = true;
	}
	
//	private PerfActTransformation transformation = null;
//	public void setTransformation(PerfActTransformation trans) {
//		transformation = trans;
//	}
//	public Transformation getTransformation() {
//		return transformation;
//	}
	

//  /**
//   * Constructs a new Policy object
//   * @param type the type (or category) of the policy
//   * @param name the type of the individual policy
//   */
//  public Policy(String type, String name) {
//    id = idSequence++;
//    this.type = type;
//  }

	/**
	 * Constructs a new Policy
	 * @param type the type (or category) of the policy
	 * @param name the type of the individual policy
	 */
	public Policy() {
		id = idSequence++;
//    this.type = type;
		InstanceCounter.add(this);
	}
	
	@Override
	public Policy clone() {
		Policy ret;
		try {
			ret = (Policy)super.clone();
		} catch (CloneNotSupportedException e) {
			assert false: "got an unexpected CloneNotSupportedException in Policy.clone()";
		  throw new NullPointerException("got an unexpected CloneNotSupportedException in Policy.clone()");
		}
		return ret;
	}

	/**
	 *
	 * @param longVersion
	 * @return
	 * @author kremer
	 */
	public String getName(boolean longVersion) {
		StringBuilder s = new StringBuilder(name);
		if (longVersion) {
			s.append('-').append((antecedent==null)?"LispExpression":antecedent.getName()).append('-').append(getID());
			if (doc!=null) {
			  s.append(" (").append(doc).append(')');
			}
			if (sourceFile!=null) {
			  s.append(" [defined in ").append(sourceFile).append(']');
			}
		}
		return s.toString();
	}
  
	/**
	 * @return
	 * @author  kremer
	 */
	public String getName() {return getName(false);}

	/**
	 * @see casa.policy.AbstractMessagePolicy#getID()
	 *
	 * @return
	 * @author kremer
	 */

	/**
	 * @return the documentation string for this object if there is one, otherwise null
	 */
	public String getDoc() {
		return doc;
	}

	/**
	 * @param doc set the documenation string for this object
	 */
	protected void setDoc(String doc) {
		this.doc = doc;
	}

	public int getID() {return id;}

	/**
	 * @see java.lang.Object#toString()
	 * @return form: (policy [EventDescriptor] ('( [PolicyConsequent]... )))
	 */
	@Override
	public String toString() {
		return toString(0);
	}
  
	/**
	 * @see java.lang.Object#toString()
	 * @return form: (policy [EventDescriptor] ('( [PolicyConsequent]... )))
	 */
	public String toString(int indent) {
		StringBuilder buf = new StringBuilder();
		CASAUtil.pad(buf, indent)
		.append("(POLICY ; id=")
		.append(getID())
		.append(" name=")
		.append(getName())
		.append("\n");
		CASAUtil.pad(buf, indent+2);
		if (unevaluatedAntecedent!=null)
			buf.append('`')
			.append(unevaluatedAntecedent.writeToString());
//		  .append(ConsToStringWithoutInterp(unevaluatedAntecedent));
		else
		  buf.append(antecedent);
		buf.append("\n");
		CASAUtil.pad(buf, indent+2)
		.append("`(\n");
		for (PolicyConsequentInterface c: consequents) {
			CASAUtil.pad(buf, indent+4)
			.append(c.toString())
			.append('\n');
		}
		CASAUtil.pad(buf, indent+2).append(")\n");
		if (doc!=null || sourceFile!=null)
			CASAUtil.pad(buf, indent+2)
			.append("\"")
			.append((doc==null?"":doc)+(sourceFile==null?"":" (defined in "+sourceFile+")"))
			.append("\"\n");
		if (precondition!=null)
			CASAUtil.pad(buf, indent+2).append(":PRECONDITION '").append(precondition.writeToString()).append('\n');
		if (name!=null)
			CASAUtil.pad(buf, indent+2).append(":NAME '").append(name).append('\n');
		if (ghost)
			CASAUtil.pad(buf, indent+2).append(":GHOST");
//		if (transformation!=null)
//			CASAUtil.pad(buf, indent+2).append(":TRANSFORMATION ").append(transformation.toString()).append('\n');
		CASAUtil.pad(buf, indent).append(")\n");
		return buf.toString();
	}
	
	public String toStringUML(){
		String s = null;
		return s;
	}
	
	
	static private String ConsToStringWithoutInterp(Cons cons) {
		StringBuilder b = new StringBuilder();
		b.append('(');
		while (cons!=null) {
			LispObject car;
			if (cons.cdr instanceof Cons) { // continuing list
				car = cons.car;
				cons = (Cons)cons.cdr;
			}
			else if (cons.cdr == org.armedbear.lisp.Lisp.NIL) { // the normal termination case
				car = cons.car;
				cons = null;
			}
			else { // the dotted case
				b.append(" . ");
				car = cons.cdr;
				cons = null;
			}
			
			if (car!=null) {
				if (car instanceof Symbol) {
					Symbol sym = (Symbol)car;
					if (sym.isConstant() && sym.isSpecialVariable())
						b.append(':');
					b.append(sym.getName());
				}
				else if (car instanceof Cons) 
					b.append(ConsToStringWithoutInterp((Cons)car));
				else
					b.append(car.writeToString());
			}
			if (cons!=null)
				b.append(' ');
		}
		b.append(')');
		return b.toString();
	}
  

  
	@Override
	public int compareTo(AbstractPolicy o) {
		return getID()-o.getID();
	}	

	/**
	 * @return  the type
	 */
	public String getType() {
		return antecedent.getType();
	}
	
//	private Map<String, LispObject> symbolMap = null;
//	public void setMapping(Map<String, LispObject> map){
//		symbolMap = map;
//		}
//	public Map<String, LispObject> getMapping() {
//		if (symbolMap==null) symbolMap = new TreeMap<String,LispObject>();
//		return symbolMap;
//		}

	public final StatusObject<List<Object>> apply(PolicyAgentInterface agent, Status status, Vector<AbstractPolicy> policyContext, Event event, Map<String, LispObject> map, Object eventInstanceInfo) throws Exception, Throwable {
		assert map!=null;
		StatusObject<List<Object>> stat;
//		if (isApplicable(agent,event,map).getStatusValue()==0) { // 0 is "true"
			if (agent.isLoggingTag("policies6")) agent.println("policies6", "Applying policy \""+getName()+"\" with symbol map "+map);
	    map.put("event", new JavaObject(event));
			stat = executeConcequent(agent,status,policyContext,event,eventInstanceInfo,map);
			agent.notifyObservers(ML.EVENT_POLICY_APPLIED,new casa.util.Pair<Policy,Event>(this,event));
//		}
//		else
//			stat = new StatusObject<List<Object>>(-12,"Policy "+getName()+" not applicble to event \""+event+"\".");
		return stat;
	}
  
	/**
	 * Determines if this policy is applicable to the <em>event</em>.  It is considered applicable if:
	 * <ol>
	 * <li> If this policy has an antecedent {@link EventDescriptor}, which returns false for 
	 *      this event, the event is considered NOT applicable and we return false.
	 * <li> Otherwise, if this policy has an unevaluated antecedent {@link Cons}, it is evaluated.  
	 *      If it returns a {@link EventDescriptor}, this is evaluated as in (1), otherwise the 
	 *      Cons is evaluated as if it were a lisp boolean expression.  If the expression returns
	 *      NIL, the event is considered NOT applicable and we return false.
	 * <li> Otherwise, if this there is a postcondition, it is evaluated as a boolean expression
	 *      and this value is returned.
	 * <li> Otherwise, return true.
	 * </ol>
	 * @param agent The agent in who's context the policy is to be executed in
	 * @param event The event
	 * @param map The mapping of symbols to be added to the lisp envirnonment
	 * @see casa.policy.AbstractPolicy#isApplicable(casa.interfaces.PolicyAgentInterface, casa.event.Event, java.util.Map)
	 */
	public Status isApplicable(PolicyAgentInterface agent, Event event, Map<String, LispObject> map) {
		assert map!=null;

		//		//Account for bound symbols in antecedent - dsb
		//		if ((antecedent != null) && (antecedent instanceof MessageEventDescriptor)){
		////			Map<String, LispObject> map = getMapping();
		//			EventPattern pattern = ((MessageEventDescriptor)antecedent).getPattern();
		//			for (String p : pattern.keySet()){
		//				String ptrn = pattern.get(p).toString().toLowerCase();
		//				if (map.keySet().contains(ptrn)){
		//					URLDescriptor u = (URLDescriptor)((JavaObject)map.get(ptrn)).getObject();
		//					pattern.put(p, u);
		//				}
		//			}
		//			((MessageEventDescriptor)antecedent).setPattern(pattern);
		//		}

		Object obj = updateAntecedent(agent, map);
		if (antecedent!=null) {
			
			Status stat = antecedent.isApplicable(agent, event); 
			if (stat.getStatusValue()!=0)
				return new Status(-1,(unevaluatedAntecedent==null?"Compile-time":"Run-time")+" EventDescriptor in antecedent failed ("+stat.getExplanation()+")");
		}
		else if (unevaluatedAntecedent!=null) {
//			Object obj = evalExpression(agent, unevaluatedAntecedent, map);
//			if (obj instanceof EventDescriptor) {
//				if (!((EventDescriptor)obj).isApplicable(agent, event)) {
//					return false;
//				}
//			}
//			else {
				if (obj instanceof Boolean && !((Boolean)obj)) 
					return new Status(-2,"Boolean run-time antecedent evaluated to false.");
				else if (obj == org.armedbear.lisp.Lisp.NIL)
					return new Status(-3,"Object run-time antecedent evalated to NIL");
//			}
		}

		if (precondition == null) 
			return new Status(0,"Antecedent evaluated to true with no precondition.");

		boolean ret = evalBooleanExpression(agent, precondition, map);
		if (ret)
			return new Status(0,"Antecedent and precondition evaluated to true.");
		else
		  return new Status(-4,"Antecedent evaluated to true but precondition evaluated to false.");
	}
	
	/**
	 * if there is an unevaluated antecedent, and the evaluation is a {@link EventDescriptor}
	 * then place this {@link EventDescriptor} in the antecedent.  The return
	 * value is not particularly meaningful, but it can be used to avoid performing
	 * the evaluation multiple times as it is guaranteed to be the result of an
	 * evaluation of the unevaluated antecedent if there is one.
	 * @param agent
	 * @param map symbols to be placed in the lisp environment during any evaluation.
	 * @return null if there was no unevaluated antecedent or the object returned 
	 * from the unevaluated antecedent if there was one.
	 */
	@Override
	public Object updateAntecedent(PolicyAgentInterface agent, Map<String, LispObject> map) {
		Object obj = null;
		if (unevaluatedAntecedent!=null) {
			obj = evalExpression(agent, unevaluatedAntecedent, map);
			if (obj instanceof EventDescriptor) {
				antecedent = (EventDescriptor)obj;
//				if (transformation!=null) 
//					antecedent.getPattern().setTransformation(transformation);
			}
		}
		return obj;
	}
	
	private Object evalExpression(PolicyAgentInterface agent, Cons exp, Map<String, LispObject> map) {
		Status stat = agent.abclEval(exp, map);
		if (stat instanceof StatusObject<?>) {
			Object obj = ((StatusObject<?>)stat).getObject();
			if (obj instanceof JavaObject) {
				obj = ((JavaObject)obj).getObject();
			}
			return obj;
		}
		return (stat.getStatusValue()>=0);
	}
	
	private boolean evalBooleanExpression(PolicyAgentInterface agent, Cons exp, Map<String, LispObject> map) {
		Object obj = evalExpression(agent, exp, map);
		if (obj instanceof Boolean) return (Boolean)obj;
		return obj != org.armedbear.lisp.Lisp.NIL;
		//agent.println("warning","Policy.isApplicable: Precondition returned " + guard + " \""+precondition.writeToString()+"\"");
	}

	protected StatusObject<List<Object>> executeConcequent(
			PolicyAgentInterface agent, 
			Status status, 
			Vector<AbstractPolicy> policyContext, 
			Event event, 
			Object eventInstanceInfo,
			Map<String, LispObject> map) throws Exception, ControlTransfer {
		
		if (map==null) map = new TreeMap<String, LispObject>(); 
		map.put("event", new JavaObject(event));
		List<Object> objs= new LinkedList<Object>();
		AgentUI ui = new BufferedAgentUI();
		int n = 0;
		for (PolicyConsequentInterface consequent: consequents) {
			n++;
			if (agent.isLoggingTag("policies5")) agent.println("policies5", "Executing policy concequent "+n+": "+consequent);
			try {
				Object obj = consequent.process(agent, ui, map);
			  if (obj!=null) objs.add(obj);
			}
			catch (Exception e) {
				agent.println("error", "Policy "+getName()+" failed executing consequent "+n+"\n   "+consequent, e);
			}
		}
		return new StatusObject<List<Object>>(0,objs);
	}

	/**
	 * Lisp operator: (POLICY event-descriptor '(action ...))<br>
	 * Construct a Policy object and add it to the agent's policy store.
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator POLICY =
		new CasaLispOperator("POLICY", "\"!Return a new Policy object.\" "
    						+ "EVENT-DESCRIPTOR \"!The antecedent - a description of the event; either an EventDescriptor or a Cons that will evaluate to an EventDescriptor.\" "
    						+ "ACTIONS \"@org.armedbear.lisp.Cons\" \"!The result - a Cons list of actions to take if the event happens.\" "
    						+ "&OPTIONAL DOC \"@java.lang.String\" \"!The documentation string for the policy.\" "
    						+ "&KEY "
    						+ "PRECONDITION \"!A (backquoted) boolean expression acting as a guard on this policy.\" "
    						+ "POSTCONDITION \"!A (backquoted) boolean expression for the post condition of this policy (currently not used).\" "
    						+ "NAME \"@java.lang.String\" \"!The name to be used as the short name of this policy.\" "
    						+ "GHOST \"@java.lang.Boolean\" \"!Sets this policy to be a ghost (not counted as a 'real' policy application).\" "
//    						+ "TRANSFORMATION \"@casa.PerfActTransformation\" \"!Transformation object.\" "
    						,TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
	{
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
      
			//get the event descriptor
			Object obj = params.getJavaObject("EVENT-DESCRIPTOR");
			EventDescriptor eventDescriptor = null;
			Cons eventDescriptorCons = null;
			if (obj instanceof EventDescriptor) {
				eventDescriptor = (EventDescriptor)obj;
			}
			else if (obj instanceof Cons) {
				eventDescriptorCons = (Cons)obj;
			}
			else 
				throw new LispException("POLICY: expecting an EventDescriptor as the first parameter, but got \""+obj+"\""); 
      	
			//get the cons list of actions to do
			LispObject lispObj = params.getLispObject("ACTIONS");
			if (!(lispObj instanceof Cons))
				throw new LispException("POLICY: expecting a list of consequents (such as SC operators), but got \""+lispObj+"\"");
			
			Cons cons = (Cons)lispObj;
			List<PolicyConsequentInterface> consequents = new LinkedList<PolicyConsequentInterface>();
			
			while (cons!=null && cons!=org.armedbear.lisp.Lisp.NIL) {
				LispObject car = cons.car();
				LispObject cdr = cons.cdr();
				if (!(cdr==null || cdr==org.armedbear.lisp.Lisp.NIL || cdr instanceof Cons)) {
					throw new LispException("POLICY: expecting an element of the list of SC operators, but got \""+cdr+"\"");
				}
				if (!(car==null || car==org.armedbear.lisp.Lisp.NIL)) {
					if (!(car instanceof Cons)) {
						try {
							String s = car.writeToString();
							throw new LispException("POLICY: expecting a Cons list describing a consequent, got: "+s);
						} catch (ControlTransfer e) {
							throw new LispException("POLICY: expecting a Cons list describing a consequent.",e);
						}
					}
					consequents.add(new PolicyConsequent((Cons)car));
				}
				cons = (cdr==null||cdr==org.armedbear.lisp.Lisp.NIL)?null:(Cons)cdr;
			}
      	
			lispObj = params.getLispObject("PRECONDITION");
			Cons precondition = null;
			if (!(lispObj == null || lispObj==org.armedbear.lisp.Lisp.NIL)) {
				if (!(lispObj instanceof Cons))
					throw new LispException("POLICY: expecting boolean lisp expression for a PRECONDITION, but got \""+lispObj+"\"");
				precondition = (Cons)lispObj;
			}
      	
			lispObj = params.getLispObject("POSTCONDITION");
			Cons postcondition = null;
			if (!(lispObj == null || lispObj==org.armedbear.lisp.Lisp.NIL)) {
				if (!(lispObj instanceof Cons))
					throw new LispException("POLICY: expecting boolean lisp expression for a POSTCONDITION, but got \""+lispObj+"\"");
				postcondition = (Cons)lispObj;
			}
			
			String name = (String)params.getJavaObject("NAME");
			if (name==null) {
				if (eventDescriptor!=null)
					name = eventDescriptor.getType();
				else 
					name = "LispExpression";
			}
			
			Boolean ghost = (Boolean)params.getJavaObject("GHOST");
			if (ghost==null) ghost = false;
				
			//if we got here, we didn't run into any errors parsing -- we can construct a new policy
			Policy policy = new Policy();
			if (eventDescriptor!=null)
				policy.setAntecedent(eventDescriptor);
			else 
				policy.setUnevaluatedAntecedent(eventDescriptorCons);
			policy.setConsequents(consequents);
			policy.setPrecondition(precondition);
			policy.setPostcondition(postcondition);
			policy.setName(name);
		  if (ghost)
		  	policy.setGhost();
			Object doc = params.getJavaObject("DOC");
			if (doc!=null && doc instanceof String) {
				policy.doc = (String)doc;
			}
			policy.setSourceFile((String)params.getJavaObject("__SOURCE_"));
			return new StatusObject<Policy>(0,policy);
		}
	};
	
	/**
	 * Lisp operator: (POLICY event-descriptor '(action ...))<br>
	 * Construct a Policy object and add it to the agent's policy store.
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator CONSEQUENT_CLASS =
		new CasaLispOperator("CONSEQUENT-CLASS", "\"!Return a new object that is a subclass of Concequent.\" "+
    						"CLASS-NAME \"!The fully-qualified name if a subclass of Rule.\" "
							, TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
    	{
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
			try {
				PolicyConsequentInterface rule = Class.forName ((String)params.getJavaObject("CLASS-NAME")).asSubclass (PolicyConsequentInterface.class).getConstructor((Class[])null).newInstance((Object[])null);
				return new StatusObject<Object>(rule.process(agent,ui,null));
				//return new StatusObject<PolicyConsequentInterface>(0,rule);
  				} catch (Throwable e) {
  					return new Status(-1,agent.println("error", "(RuleClass <class>) must have parameter of fully-qualified-java-class-name, but got '"+params.getJavaObject("CLASS-NAME")+"'", e));
  				}
			}
    	};
    
    static class SCOpData {
    	URLDescriptor debtor,
    	              creditor;
    	String        performative;
    	Act           act;
    	Class<? extends Action> actionClass = null;
  		Cons          actionData = null;
  		SocialCommitmentDescriptor  dependsOn;
  		boolean       shared;
  		boolean				persistent;
  		boolean       getEvents;
  		Event[]       events = null;
  		Event         event;
    }
    
    static private SCOpData getSCOpData(String op, TransientAgent agent, ParamsMap params, Environment env) throws ControlTransfer, Exception {
    	SCOpData data = new SCOpData();
    	
    	//get the message from the lisp environment
    	//data.message = TransientAgent.getMsgForThread();
  	  Event event = (Event)casa.abcl.Lisp.lookupAsJavaObject(env, "event");
  	  if (event==null || !(event instanceof MessageEvent)) 
  	  	throw new LispException("Policy.getSCOpData: no message event in scope");
  	  data.event = event;
    	
    	// get the values from the lisp command
  		data.debtor = (URLDescriptor)params.getJavaObject("DEBTOR",URLDescriptor.class);
  		data.creditor = (URLDescriptor)params.getJavaObject("CREDITOR",URLDescriptor.class);
  		data.performative = (String)params.getJavaObject("PERFORMATIVE",String.class);
  		if (!agent.isA(data.performative,ML.ACTION)) 
  			throw new Exception("("+op+" ... :Performative <id> ...) must have a :Performative parameter that evaluates to an action type, got '"+data.performative+"'");
  		data.act = (Act)params.getJavaObject("ACT",Act.class);
  		
  		String actionClass = null;
  		try {
  			actionClass = (String)params.getJavaObject("ACTION-CLASS",String.class);
  			if (actionClass!=null) 
  				data.actionClass = Class.forName(actionClass).asSubclass(Action.class);
  		} catch (Exception e) {
  			throw new Exception("("+op+" ... :ACTION-CLASS <string> :ACTION <string> ...): ACTION-CLASS parameter values must specify a subclass of class Action, got \""+actionClass+"\"");
		  }
  		data.actionData = (Cons)params.getLispObject("ACTION");
  	  if (data.actionData != null && data.actionClass == null) {
  				data.actionClass = Action.class;
  		}
  		
  		data.dependsOn = (SocialCommitmentDescriptor)params.getJavaObject("DEPENDS-ON",SocialCommitmentDescriptor.class);
  		data.shared = params.containsKey("SHARED");
  		data.persistent = params.containsKey("PERSISTENT");
  		data.getEvents = params.containsKey("GETEVENTS");
  		if (data.getEvents) {
  			data.events = agent.getSubscribeEvents (data.event);
  		}
  		return data;
    }
      	
		/**
     * The kinds of Social Commitment Operators that can be specified
  	 */
    public enum OpType {
    	/**
  		 */
  		ADD, 
  		/**
  		 */
  		ADDIF, 
  		/**
  		 */
  		FULFIL, 
  		/**
  		 */
  		CANCEL
  	};
  	
    /**
     * Lisp operator: (ADD :DEBTOR d :Creditor c :PERFORMATIVE p :ACT ...)<br>
     * Construct a social commitment ADD operator.
     */
    @SuppressWarnings("unused")
    private static final CasaLispOperator SC__ADD =
    	new CasaLispOperator("SC.ADD", "\"!Return a new ADD operator.\" "+
    			"&KEY DEBTOR \"@casa.agentCom.URLDescriptor\" \"!The debtor.\" "+
    			"CREDITOR \"@casa.agentCom.URLDescriptor\" \"!The creditor.\" "+
    			"PERFORMATIVE \"!The performative.\" "+
    			"ACT \"@casa.Act\" \"!The act.\" "+
    			"ACTION-CLASS \"@java.lang.String\" \"!The action class, which needs to be a subclass of Action.\" "+
    			"ACTION \"@org.armedbear.lisp.Cons\" \"!The action data, which needs to be a cons list.\" "+
    			"DEPENDS-ON \"@casa.socialcommitments.SocialCommitmentDescriptor\" \"!The social commitment that this one depends on.\" "+
    			"SHARED \"!The commitment is a shared commitment.\" "+
    			"PERSISTENT \"!The commitment is persistent and can only be removed with a CANCEL.\" "+
    			"GETEVENTS \"!We should retrieve the events from the agent.\" "
    			, TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
    {
    	@Override
    	public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
    		try {
    			SCOpData data = getSCOpData("ADD", agent, params, env);
    			SocialCommitmentOperator SCop =
    				(data.dependsOn == null)
    					? new AddSocialCommitment (data.debtor, data.creditor, data.performative, data.act, data.event, data.actionClass, data.actionData, data.shared, data.persistent, null, data.events)
    					: new AddDependentSocialCommitment (data.dependsOn, data.debtor, data.creditor, data.performative, data.act, data.event, data.actionClass, data.actionData, data.shared, data.persistent, null, data.events);
    					return new StatusObject<SocialCommitmentOperator>(SCop);
    		} catch (Throwable e) {
    			throw new LispException(agent.println("error", "SC.ADD social commitment operator failed",e));
    		}
    	}
    };
    
//    /**
//     * Lisp operator: (ADD :DEBTOR d :Creditor c :PERFORMATIVE p :ACT ...)<br>
//     * Construct a social commitment ADD operator.
//     */
//    @SuppressWarnings("unused")
//    @Deprecated
//    private static final CasaLispOperator SC__ADDIF = 
//    	new CasaLispOperator("SC.ADDIF", "\"!Return a new ADDIF operator.\" "+
//    			"CONDITION \"@org.armedbear.lisp.Cons\" \"!The boolean expression indicating whether this operator is instantiated or not (evaluated at policy application time).\" "+
//    			"&KEY DEBTOR \"@casa.agentCom.URLDescriptor\" \"!The debtor.\" "+
//    			"CREDITOR \"@casa.agentCom.URLDescriptor\" \"!The creditor.\" "+
//    			"PERFORMATIVE \"!The performative.\" "+
//    			"ACT \"@casa.Act\" \"!The act.\" "+
//    			"ACTION-CLASS \"@java.lang.String\" \"!The acton class, which needs to be a subclass of Action.\" "+
//    			//"ACTION \"@java.lang.String\" \"!The acton class, which needs to be a subclass of Action.\" "+
//    			"ACTION \"@org.armedbear.lisp.Cons\" \"!The action data, which needs to be a cons list.\" "+
//    			"DEPENDS-ON \"@casa.socialcommitments.SocialCommitmentDescriptor\" \"!The social commitment that this one depends on.\" "+
//    			"SHARED \"!The commitment is a shared commitment.\" "+
//    			"GETEVENTS \"!We should retrieve the events from the agent.\" "
//    			, TransientAgent.class)
//    {
//    	@Override
//    	public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
//    		try {
//    			Condition addifCondition = null;
//    			Cons cons = (Cons)params.getJavaObject("CONDITION", Cons.class);
//    			addifCondition = getAddifParam1(cons);
//
//    			SCOpData data = getSCOpData("ADDIF", agent, params, env);
//
//    			SocialCommitmentOperator SCop =
//    				(data.dependsOn == null)
//    				? new AddSocialCommitment (data.debtor, data.creditor, data.performative, data.act, data.message, data.actionClass, data.actionData, data.shared, addifCondition, data.events)
//    			: new AddDependentSocialCommitment (data.dependsOn, data.debtor, data.creditor, data.performative, data.act, data.message, data.actionClass, data.actionData, data.shared, addifCondition, data.events);
//
//    				return new StatusObject<SocialCommitmentOperator>(SCop);
//    		} catch (Throwable e) {
//    			return new Status(-1, agent.println("error", "ADDIF social commitment operator failed",e));
//    		} 
//    	}
//    };

  	/**
  	 * @param cons
  	 * @return
  	 * @throws LispParseException
  	 */
  	private static Condition getAddifParam1(final Cons cons) throws LispParseException {
  		if (cons==null) throw new LispParseException("(Addif <condition> ...) must have a 1st parameter and it must be a boolean expression, got a null",0);
  		Condition ret = new Condition(){public boolean booleanValue(PolicyAgentInterface agent) {
  			StatusObject<Object> stat = null;
  		    try {
  		    	stat = (StatusObject<Object>)agent.abclEval(cons, null);
      			return !(stat.getObject()==null || stat.getObject().equals(org.armedbear.lisp.Lisp.NIL));
      		} catch (Throwable e) {
      			agent.println("error", "(Addif "+cons+" ...) must have a 1st parameter and it must be a boolean expression, got '"+(stat==null?"null":stat.getObject())+"'\n  ******* Social commitment not added *******",e);
      			return false;
      		}}};
  		return ret;
  	}

  	/**
  	 * Lisp operator: (FULFIL :DEBTOR d :Creditor c :PERFORMATIVE p :ACT a)<br>
  	 * Construct a social commitment FULFIL operator.
  	 */
  	private static final CasaLispOperator SC__FULFIL =
  		new CasaLispOperator("SC.FULFIL", "\"!Return a new FULFIL operator.\" "+
  				"&KEY DEBTOR \"@casa.agentCom.URLDescriptor\" \"!The debtor.\" "+
  				"CREDITOR \"@casa.agentCom.URLDescriptor\" \"!The creditor.\" "+
  				"PERFORMATIVE \"@java.lang.String\" \"!The performative.\" "+
  				"ACT \"@casa.Act\" \"!The act.\" "
  				, TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
  	{
  		@Override
  		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
  			try {
  				SCOpData data = getSCOpData("FULFIL", agent, params, env);
  				SocialCommitmentOperator SCop = 
  					new FulfilSocialCommitment (data.debtor, data.creditor, data.performative, data.act, data.event);
  				return new StatusObject<SocialCommitmentOperator>(SCop);
  			} catch (Throwable e) {
  				return new Status(-1, agent.println("error", "FULFIL social commitment operator failed",e));
  			} 
  		}
  	};

  	/**
  	 * Lisp operator: (CANCEL :DEBTOR d :Creditor c :PERFORMATIVE p :ACT a)<br>
  	 * Construct a social commitment CANCEL operator.
  	 */
  	private static final CasaLispOperator SC__CANCEL =
  		new CasaLispOperator("SC.CANCEL", "\"!Return a new CANCEL operator.\" "+
  				"&KEY DEBTOR \"@casa.agentCom.URLDescriptor\" \"!The debtor.\" "+
  				"CREDITOR \"@casa.agentCom.URLDescriptor\" \"!The creditor.\" "+
  				"PERFORMATIVE \"!The performative.\" "+
  				"ACT \"@casa.Act\" \"!The act.\" "
  				, TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
  	{
  		@Override
  		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
  			try {
  				SCOpData data = getSCOpData("CANCEL", agent, params, env);
  				SocialCommitmentOperator SCop = 
  					new CancelSocialCommitment (data.debtor, data.creditor, data.performative, data.act, data.event);
  				return new StatusObject<SocialCommitmentOperator>(SCop);
  			} catch (Throwable e) {
  				return new Status(-1, agent.println("error", "CANCEL social commitment operator failed",e));
  			} 
  		}
  	};

}