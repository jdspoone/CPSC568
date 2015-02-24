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
package casa.event;

import casa.ML;
import casa.Status;
import casa.StatusObject;
import casa.TransientAgent;
import casa.abcl.CasaLispOperator;
import casa.abcl.ParamsMap;
import casa.interfaces.PolicyAgentInterface;
import casa.ui.AgentUI;
import casa.util.Pair;
import casa.util.PairComparable;

import org.armedbear.lisp.Environment;

/**
 * @author      <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
public class MessageEventDescriptor extends EventDescriptor {

	/**
	 * Create a MessageEventDescriptor for <em>agent</em> of type <em>eventType</em> (must be 
	 * subtype of messageEvent in the agent's ontology) with the constrains on message 
	 * matching listed in <em>pairs</em> (key/value pairs in the message).
	 * @param agent
	 * @param eventType
	 * @param pairs
	 */
	public MessageEventDescriptor(PolicyAgentInterface agent, String eventType, Pair<String,? extends Object>[] pairs) {
		super(agent, eventType, pairs);
		if (!agent.isA(eventType, "event_messageEvent")) throw new IllegalArgumentException("eventType parameter '"+eventType+"' must be a subtype of \"event_messageEvent\"");
	}

  public MessageEventDescriptor(PolicyAgentInterface agent, String type, Object... pairs) {
  	this(agent, type, makePairs(pairs));
  }
  
	/**
	 * @return The name of the lisp function that will create an instance of this type
	 */
	@Override
	protected String getLispFunctionName() {return "MSGEVENT-DESCRIPTOR";}
	
  /**
	 * Determines if this EventDescriptor should override (replace, takes precedence over) 
	 * the other one such that the other one's associated policy doesn't get executed at
	 * all.  This is not the same as being more general or specific: two EventDescriptors
	 * may be in a sub/super type relationship and both be executable.
	 * @see EventDescriptor#overrides(EventDescriptor)
	 * @param other The EventDescriptor to compare to
	 * @return Returns true iff <em>other</em> is another MessageEventDescriptor, and
	 * both performatives are identical, and the <em>act</em>
	 * of this one is more specialized than the <em>act</em> of the other one.
	 */
	@Override
	public boolean overrides(EventDescriptor other) {
		if (other instanceof MessageEventDescriptor) {
			MessageEventDescriptor o = (MessageEventDescriptor)other;
			if (getPerformative()!=null && o.getPerformative()!=null 
					&& getPerformative().equals(o.getPerformative())) {
				// if "this" isa "that", and they aren't equal
				if (agent.isAAct(getAct(),o.getAct()) && !(agent.isAAct(o.getAct(),getAct()))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
   * Lisp operator: (MSGEVENT-DESCRIPTOR performative act)<br>
   * Create a MessageEventDescriptor object.
   */
  @SuppressWarnings("unused")
	private static final CasaLispOperator MSGEVENT_DESCRIPTOR =
    new EventDescriptorLispOperator("MSGEVENT-DESCRIPTOR", "\"!Return a new MessageEventDescriptor that matches fields according the the keys and values.\" "+
    						"&OPTIONAL EVENT-TYPE \"@java.lang.String\" \"!The specific subtype of EVENT_MESSAGE_EVENT of to be matched\" "+
    						"&ALLOW-OTHER-KEYS", TransientAgent.class)
    {
      @SuppressWarnings("unchecked")
			@Override
			public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
      	try {
	        StatusObject<EventDescriptor> ret = (StatusObject<EventDescriptor>)super.execute(agent, params, ui, env);
	        return new StatusObject<MessageEventDescriptor>(0, (MessageEventDescriptor)ret.getObject());
        } catch (Throwable e) {
	        e.printStackTrace();
	        return null;
        }
      }
      
    	/**
    	 * @return The most general type name this class will accept
    	 */
    	@Override
			protected String getMGT() {return ML.EVENT_MESSAGE_EVENT;}
    	
      @Override
    	protected MessageEventDescriptor makeNew(PolicyAgentInterface agent, String type, Pair<String,Object>[] pairs) {
      	return new MessageEventDescriptor(agent, type, pairs);
      }
      
    	@Override
    	protected String getLispFunctionName() {return "MSGEVENT-DESCRIPTOR";}
    };
  


}
