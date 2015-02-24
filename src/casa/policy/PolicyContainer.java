package casa.policy;

import casa.CommitmentProcessor;
import casa.Status;
import casa.StatusObject;
import casa.conversation2.Conversation;
import casa.event.Event;
import casa.event.EventDescriptor;
import casa.interfaces.PolicyAgentInterface;
import casa.socialcommitments.SocialCommitment;
import casa.socialcommitments.SocialCommitmentsStore;
import casa.socialcommitments.operators.AddSocialCommitment;
import casa.socialcommitments.operators.SocialCommitmentOperator;
import casa.socialcommitments.ui.SocialCommitmentJList;
import casa.util.CASAUtil;
import casa.util.InstanceCounter;
import casa.util.Pair;
import casa.util.PairComparable;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.swing.JComponent;

import org.armedbear.lisp.Cons;
import org.armedbear.lisp.JavaObject;
import org.armedbear.lisp.LispObject;

/**
 * <p>Title: CASA Agent Infrastructure</p> <p>Description: </p> <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The  Knowledge Science Group makes no representations about the suitability of  this software for any purpose.  It is provided "as is" without express or implied warranty.</p> <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public class PolicyContainer extends ConcurrentSkipListSet<AbstractPolicy> implements Comparable<PolicyContainer> { //TreeSet<AbstractPolicy> {
	private static final long serialVersionUID = 1597482566941025440L;
	/**
	 * @param type TODO
	 */

  public PolicyContainer(String type, String name) {
    super();
    setType(type);
    this.name = name;
    InstanceCounter.add(this); 
  }
  
	public Collection<AbstractPolicy> getPolicies() {
  	return this;
  }
  
  public void setType(String type) {
  	this.type = type;
  }

  /**
   * Overrides the host add method enforce all members to be Policies and
   * to REPLACE a newly added policy instead of just ignoring it.
   * @param policy: an Object that must be a Policy
   * @return true if the object was not already in the set.
   */
  @Override
  public boolean add(AbstractPolicy policy) {
  	if (policy==null) return false;
 		remove(policy);
    return super.add(policy);
  }

  public int compareMethod(Object o1, Object o2) {
  	return (((PairComparable<Conversation,AbstractPolicy>)o1).getSecond()).compareTo(((PairComparable<Conversation,AbstractPolicy>)o2).getSecond());
  }

  public void addPolicy(AbstractPolicy p) {
    add(p);
  }

  public void addPolicies(AbstractPolicy p[]) {
    for (int i=0, max=p.length; i<max; i++) add(p[i]);
  }

  public void addPolicies(Collection<AbstractPolicy> p) {
    for (AbstractPolicy i: p) add(i);
  }

 /**
   * Apply all the appropriate policies to <em>event</em> after filtering and sorting.
   * @param agent the agent that is the context (is going to execute) the policies.
   * @param applicablePolicies the policies to apply.
   * @param event the event that the policies are applied to.
   * @return the composite status of the application of <em>applicablePolicies</em>. 
   * @throws Exception if one of the policy applications 
   * @see AbstractPolicy#apply(PolicyAgentInterface, Status, Vector, Event, Object)
   * @see #filterPolicies(PolicyAgentInterface, Vector)
   * @see #sortPolicies(PolicyAgentInterface, Vector)
   * @throws Exception if an exception occurs in the application, the process is interrupted and the exception is thrown.
   */
  public StatusObject<List<Object>> applyPolicies(PolicyAgentInterface agent, 
  		AbstractCollection<Pair<Conversation, AbstractPolicy>> applicablePolicies, 
  		Event event, 
  		Map<String, 
  		LispObject> map) throws Exception {
    if (applicablePolicies==null) 
    	return new StatusObject<List<Object>>(138,"Applicable policies == null");
    
  	if (agent.isLoggingTag("policies"))
      agent.println("policies1","+++++++++++++ PolicyContainer.applyPolicies() start +++++++++++++++++++++++++++++++++++++++++++++");
    StatusObject<List<Object>> ret = null;
    
    if (agent.isLoggingTag("policies4"))
      agent.println("policies4", "Agent " + agent.getName() + " has "+applicablePolicies.size()+" applicable policies (before filtering):\n" + toString(applicablePolicies)); //debugging

    // Filtering (below) requires EventDescriptors in order to determine precedence; but 
    // some of the EventDescriptors may be unevaluated.  The following fixes this.
    updatePolicies(agent, applicablePolicies, map);
    
    //filter policies -- if two or more policies apply, and have the same name,
    //choose only the one with the most specific act.
    filterPolicies(agent, applicablePolicies);

    if (agent.isLoggingTag("policies4"))
      agent.println("policies4", "Agent " + agent.getName() + " has "+applicablePolicies.size()+" applicable policies (after filtering):\n" + toString(applicablePolicies)); //debugging

    //sort policies -- sort by partial order of Performatives then acts (top first).
    applicablePolicies = sortPolicies(agent, applicablePolicies);
   
    if (agent.isLoggingTag("policies1"))
      agent.println("policies1", "Agent " + agent.getName() + " has "+applicablePolicies.size()+" applicable policies (after sorting):\n" + toString(applicablePolicies)); //debugging

   
    for (Pair<Conversation,AbstractPolicy> p : applicablePolicies) {
      if (agent.isLoggingTag("policies")) {
   		  agent.println("policies","Executing policy '"+p.getSecond().getName()+"' (type="+p.getSecond().getClass().getName()+")");
      }
      //p.setContext(applicable);
      StatusObject<List<Object>> temp=null;
      if (p.getFirst()==null)
      	map.remove("conversation");
      else {
      	Conversation c = p.getFirst();
        map.put("conversation", new JavaObject(c));
        map.putAll(c.getSymbolMap());
      }
      try {
      	Vector<AbstractPolicy> policiesOnly = new Vector<AbstractPolicy>(applicablePolicies.size());
      	for (Pair<Conversation,AbstractPolicy> a: applicablePolicies) {
      		policiesOnly.add(a.getSecond());
      	}
      	temp = p.getSecond().apply(agent, ret, policiesOnly, event, map, null);
      } catch (Throwable e) {
      	agent.println("error", "PolicyContainer: Failed to apply policy "+p.getSecond().getName(),e);
      }
      if (agent.isLoggingTag("policies")) {
      	if (temp!=null && temp.getStatusValue()<0)
      		agent.println("policies","**************policy "+p.getSecond().getName()+" failed: "+temp);
      	else
          agent.println("policies4","Finished executing policy '" + p.getSecond().getName() + "'");
      }
      ret = combineStatus(ret,temp);
    }
  
    conclude(agent, ret);
//    Status temp = mhInstance.conclude (agent,ret);
        
    if (ret==null) ret = new StatusObject<List<Object>>(138,"No policies applicable");
    if (agent.isLoggingTag("policies"))
      agent.println("policies1","---------------- PolicyContainer.applyPolicies() returning Status: "+ret.getStatusValue()); //debugging
    
    return ret;
  }
  
  /**
   * Evaluates the policy antecedents so filtering can determine all the antecedent EventDescriptors.
   * @param agent
   * @param applicablePolicies
   * @param map
   */
  void updatePolicies(PolicyAgentInterface agent, AbstractCollection<Pair<Conversation,AbstractPolicy>> applicablePolicies, Map<String, LispObject> map) {
    for (Pair<Conversation,AbstractPolicy> pair: applicablePolicies) {
  	  AbstractPolicy ap = pair.getSecond();
  	  ap.updateAntecedent(agent, map);
    }
  }

  /** 
	 * This is called after all the social commitments have been applied (and have
	 * created social commitment operators.  So this method needs to apply all social
	 * commitment operators to actual instantiate and delete the social commitments
	 * as specified by the operators.<br>
	 * In addition, this method applies several constraints:
	 * <ul>
	 * <li> don't add a social commitment that duplicates an existing social commitment
	 * <li> set "no debtor" for any SC for which the agent is not the deptor
	 * </ul>
	 * @see casa.policy.ApplicablePoliciesList#conclude(casa.interfaces.PolicyAgentInterface, casa.StatusObject)
	 */
	static public Status conclude (PolicyAgentInterface agent, StatusObject<List<Object>> stat) {
		if (agent.isLoggingTag ("policies4"))
			agent.println ("policies4", "Processing and executing operators...");
		
		///////////////////////////////////////////////////////////////////
		//List<SocialCommitmentOperator> ops = operators; // < old form; v new form
		List<SocialCommitmentOperator> ops = new LinkedList<SocialCommitmentOperator>();
		if (stat!=null && stat.getObject()!=null) {
		  for (Object o: stat.getObject()) {
			  if (o instanceof SocialCommitmentOperator) ops.add((SocialCommitmentOperator)o);
			  //else agent.println("warning", "MHSCInstance.conclude: Found non-SocialCommitmentOperator object in consequent results: "+o+" of type "+o.getClass());
		  }
		}
		///////////////////////////////////////////////////////////////////
		
	  //Remove duplicate Add operators
		List<SocialCommitmentOperator> tempOps = new LinkedList<SocialCommitmentOperator>();
		boolean add;
		for (SocialCommitmentOperator i : ops){
			add = true;
			if (i instanceof AddSocialCommitment){
				for (SocialCommitmentOperator j : tempOps)
					if (((AddSocialCommitment)i).equals(j)){
						add = false;
						break;
					}
			}
			if (add)
				tempOps.add(i);
		}
		ops = tempOps;

		// process all of the operators
		for (SocialCommitmentOperator op : ops) {
			boolean skip = false;
			
			if (agent.isLoggingTag ("commitments6"))
				agent.println ("commitments6", "    Working on operator: '" + op.toString () + "'");

			if (op instanceof AddSocialCommitment) {
				AddSocialCommitment addOp = (AddSocialCommitment) op;

				//Internalized conversations require similar commitments to be formed.  The client-side will
				//typically (always?) have a null Action.  This commitment should not be skipped.  Similarly,
				//multiple non-null Actions are potentially formed on the server side.  These should be
				//skipped. A similar problem exists for the operator's action data.  See below - dsb
				
				// do not execute add operator for the same social commitment for the same message
				for (SocialCommitment sc : agent.getSCStore().getCommitments (addOp.getDebtor (), addOp.getCreditor (), SocialCommitment.MASK_ALL)) {
					if (sc.getAct ().equals (addOp.getAct ())
						&& sc.getPerformative ().equals (addOp.getPerformative ())
						&& sc.getMessage ().equals (addOp.getMessage ())
						&& !sc.getDebtor().equals(addOp.getCreditor())		//For internalized conversations. See above
						&& addOp.getActionClass()==null
						&& addOp.getActionData()==null){
						if (agent.isLoggingTag ("commitments6"))
							agent.println ("commitments6", "        Skipping; operator would create duplicate social commitment");
						skip = true;
						break;			
					}
				}

				// set not debtor on all operators where the debtor is not us
				if (! agent.getURL ().equals (addOp.getDebtor ())) {
					if (agent.isLoggingTag ("commitments8"))
						agent.println ("commitments8", "        Marking operator not debtor");
					addOp.setNotDebtor (true);
				}
				
			}
		
			if (! skip) {
				// execute the operator
				if (agent.isLoggingTag ("commitments6"))
					agent.println ("commitments6", "        Executing operator...");
				try {
					op.executeOperator (agent.getSCStore(), agent);
				} catch (RuntimeException e) {
					agent.println ("error", "PolicyContainer.conclude(): Got an unexpected exception executing operator: "+op, e);
				}
			}
		}
		
		agent.bump();
		
		if (agent.isLoggingTag ("policies4"))
			agent.println ("policies4", "Finished processing and executing operators");

		return null;
	}

  /**
   * Combines two Status objects.  Subclasses may override for
   * different behaviour.
   * @param s1
   * @param s2
   * @return an appropriately combined (or chosen) Status
   */
  protected static StatusObject<List<Object>> combineStatus(StatusObject<List<Object>> s1, StatusObject<List<Object>> s2) {
    if (s1==null) return s2;
    if (s2==null) return s1;
    StatusObject<List<Object>> ret = 
    	new StatusObject<List<Object>>(s1.getStatusValue()<s2.getStatusValue()?s1.getStatusValue()     :s2.getStatusValue(),
    						                     s1.getExplanation()==null    ?s2.getExplanation():(s2.getExplanation()==null?s1.getExplanation():(s1.getExplanation()+'\n'+s2.getExplanation())));
    //TODO: if left this way, the list returned doubles up on s1's object - dsb
    //LinkedList<Object> list = new LinkedList<Object>(s1.getObject());
    LinkedList<Object> list = new LinkedList<Object>();
    if (s1.getObject()!=null) list.addAll(s1.getObject());
    if (s2.getObject()!=null) list.addAll(s2.getObject());
    ret.setObject(list);
    return ret;
  }
  
  public Vector<AbstractPolicy> findApplicable(PolicyAgentInterface agent, Event event, Map<String, LispObject> map) {
    Vector<AbstractPolicy> v = new Vector<AbstractPolicy>();

  	if (agent.isLoggingTag("policies9")) //debugging 
  		agent.println("policies9","PolicyContainer "+getType()+"/"+getName()+" searching polcies for event "+event.getID()+":\n"+event+"\nmap: "+map);
    for (AbstractPolicy policy : this) {
    	Status stat = policy.isApplicable(agent, event, map); 
    	if (stat.getStatusValue()==0) { // 0 is "true"
        v.add(policy);
      }
    	if (agent.isLoggingTag("policies9")) //debugging 
    		agent.println("policies9","PolicyContainer "+getType()+(stat.getStatusValue()==0?" SELECTED":" ignoring")
    				+" policy "+policy.getName()+" ("+stat.getExplanation()+")");
    }
    return v;
  }

  protected Vector<Pair<Conversation,AbstractPolicy>> sortPolicies(PolicyAgentInterface agent, AbstractCollection<Pair<Conversation,AbstractPolicy>> applicable) {
  	Vector<Pair<Conversation,AbstractPolicy>> v = new Vector<Pair<Conversation,AbstractPolicy>>(applicable);
    Vector<Pair<Conversation,AbstractPolicy>> ret = new Vector<Pair<Conversation,AbstractPolicy>>();
    if (v.size()<2) return v;
    while (v.size()>1) {
      int end = v.size();
      int smallest_i = 0;
      Pair<Conversation,AbstractPolicy> smallest = v.elementAt(0);
      for (int i = 1; i < end; i++) {
      	Pair<Conversation,AbstractPolicy> pi = v.elementAt(i);
        if (compareMethod(smallest, pi) > 0) {
          smallest = pi;
          smallest_i = i;
        }
      }
      ret.add(smallest);
      v.removeElementAt(smallest_i);
    }
    ret.add(v.elementAt(0));
    return ret;
  }

  protected void filterPolicies(PolicyAgentInterface agent, AbstractCollection<Pair<Conversation,AbstractPolicy>> applicablePolicies) {
  	Vector<Pair<Conversation,AbstractPolicy>> applicable = new Vector<Pair<Conversation,AbstractPolicy>>(applicablePolicies);
  	top:
  		for (int i = applicable.size()-1; i>=0; i--) {
  			Pair<Conversation,AbstractPolicy> pi = applicable.elementAt(i);
  			for (int j = i-1; j>=0; j--) {
  				Pair<Conversation,AbstractPolicy> pj = applicable.elementAt(j);
  				EventDescriptor eventI = pi.getSecond().getAntecedent();
  				EventDescriptor eventJ = pj.getSecond().getAntecedent();
  				if (eventI!=null && eventJ!=null  // filter only if... both have an eventDescriptor
  						&& eventI.getType().equals(eventJ.getType()) //... the event types are equal
  						&& pi.getFirst()==pj.getFirst() //... the conversations are the same
  						&& eventI.getAct()!=null && eventJ.getAct()!=null) { //... both have an Act field in the eventDescriptor
  					if (eventI.overrides(eventJ)) {
  						applicable.remove(j);
  						continue top;
  					}
  					if (eventJ.overrides(eventI)) {
  						applicable.remove(i);
  						continue top;
  					}
  				}
  			}
  		}
  	applicablePolicies.clear();
  	applicablePolicies.addAll(applicable);
  }

  @Override
  public String toString() {
    return toString (this,2);
  }

  public static <T extends AbstractPolicy> String toString(Vector<T> v) {
  	return toString(v,2);
  }
  
  public String toString(int indent) {
    return toString (this, indent);
  }

  public <T extends AbstractPolicy> String toString(AbstractCollection<Pair<Conversation,T>> v) {
  	Vector<AbstractPolicy> policiesOnly = new Vector<AbstractPolicy>(v.size());
  	for (Pair<Conversation,T> p: v) {
  		policiesOnly.add(p.getSecond());
  	}
  	return toString(policiesOnly);
  }
  
  public static <T extends AbstractPolicy> String toString(Collection<T> v, int indent) {
    StringBuilder buf = new StringBuilder();
    for (T p : v) {
      buf.append("\n");
    	CASAUtil.pad(buf, indent)
      .append(p.getName(true));
    }
    buf.append("\n");
    return buf.toString();
  }
  
	public boolean hasPolicyGUI () {
		return true;
	}

	public JComponent getPolicyGUI (SocialCommitmentsStore store) {
		return SocialCommitmentJList.getListPanel (store);
	}

	private String type = null;
	public String getType() {
		if (type==null) {
		  String packag = this.getClass().getPackage().getName();
		  type = packag.substring(packag.lastIndexOf('.')+1);
		}
		return type;
	}

	private String name = null;
	public String getName() {
		if (name==null) {
			name = "null";
		}
		return name;
	}

	@Override
	public PolicyContainer clone() {
		PolicyContainer ret = (PolicyContainer)super.clone();
		ret.clear();
		for (AbstractPolicy p: this) {
			ret.add(p.clone());
		}
		return ret;
	}
	
	/**
	 * Desperate attempt to get the gc to clear objects when we think this one is dead.
	 */
	public void destroy() {
		
	}

	@Override
	public int compareTo(PolicyContainer o) {
		return Integer.signum(o.hashCode()-this.hashCode());
	}

}