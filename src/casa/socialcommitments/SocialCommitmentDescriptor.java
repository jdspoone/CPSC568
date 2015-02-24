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
package casa.socialcommitments;

import casa.Act;
import casa.ML;
import casa.MLMessage;
import casa.Status;
import casa.StatusObject;
import casa.TransientAgent;
import casa.abcl.CasaLispOperator;
import casa.abcl.ParamsMap;
import casa.event.Event;
import casa.event.EventDescriptor;
import casa.event.MessageEvent;
import casa.event.MessageEventDescriptor;
import casa.interfaces.Descriptor;
import casa.interfaces.PolicyAgentInterface;
import casa.ui.AgentUI;
import casa.util.EventPattern;
import casa.util.InstanceCounter;
import casa.util.PairComparable;

import java.util.TreeMap;
import java.util.Vector;

import org.armedbear.lisp.Environment;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class SocialCommitmentDescriptor implements Descriptor<SocialCommitment>, Comparable<SocialCommitmentDescriptor> {

	protected EventPattern pattern = new EventPattern();
//	public String performative;
//	public Act act;

	/**
	 * Create a MessageEventDescriptor for <em>agent</em> of type <em>eventType</em> (must be 
	 * subtype of messageEvent in the agent's ontology) with the constrains on message 
	 * matching listed in <em>pairs</em> (key/value pairs in the message).
	 * @param agent
	 * @param eventType
	 * @param pairs
	 */
	@SuppressWarnings("unchecked")
	public SocialCommitmentDescriptor(PolicyAgentInterface agent, TreeMap<String,Object> pairs) {
		for (String key: pairs.keySet()) {
			Object val = pairs.get(key);
			if (ML.ACT.equals(key) && !(val instanceof Act)) throw new IllegalArgumentException("SocialCommitmentDescriptor: ACT is not of type Act");
			pattern.put(key, val);
		}
		InstanceCounter.add(this);
	}

	/* (non-Javadoc)
	 * @see casa.interfaces.Descriptor#isApplicable(casa.interfaces.PolicyAgentInterface, java.lang.Object, java.lang.Object)
	 */
	@Override
	public Status isApplicable(PolicyAgentInterface agent,
			SocialCommitment sc) {
		return pattern.matches(agent, sc);

//		Event event = sc.getEventOfType("MessageEvent");
//		if (event instanceof MessageEvent)
//			return pattern.matches(agent, (MessageEvent)event);
//		return false;

//		MLMessage message = sc.getMessage();
//		return pattern.matches(agent, message);
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("(SCDESCRIPTOR ")
		.append(pattern.toString())
		.append(")");
		return b.toString();
	}

	/**
   * Lisp operator: (SCDESCRIPTOR performative act)<br>
   * Create a SocialCommitmentDescriptor object.
   */
  @SuppressWarnings("unused")
	private static final CasaLispOperator SCDESCRIPTOR =
    new CasaLispOperator("SCDESCRIPTOR", "\"!Return a new SocialCommitmentDescriptor that matches fields according the the keys and values.\" "
								+ "&KEY PERFORMATIVE \"@java.lang.String\" \"!The performative.\" "
								+ "ACT \"@casa.Act\" \"!The act.\" "
    						+ "&ALLOW-OTHER-KEYS", TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
    {
      @SuppressWarnings("unchecked")
			@Override
			public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
      	TreeMap<String, Object> pairs = new TreeMap<String,Object>();
      	String eventType = ML.EVENT_MESSAGE_EVENT;
      	for (String key: params.keySet()) {
      		if (!(key.startsWith("__")))
      		  pairs.put(key,params.getJavaObject(key));
      	}
      	SocialCommitmentDescriptor ret = new SocialCommitmentDescriptor(agent, pairs);
//      	ret.performative = (String)pairs.get("PERFORMATIVE");
//      	ret.act = (Act)pairs.get("ACT");
      	return new StatusObject<SocialCommitmentDescriptor>(
      	             0, 
                     ret);
      }
    };
    
	@Override
	public int compareTo(SocialCommitmentDescriptor o) {
		return Integer.signum(o.hashCode()-this.hashCode());
	}
	 
}
