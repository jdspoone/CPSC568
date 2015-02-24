package casa.socialcommitments.operators;

import casa.Act;
import casa.MLMessage;
import casa.Status;
import casa.agentCom.URLDescriptor;
import casa.event.Event;
import casa.event.MessageEvent;
import casa.interfaces.PolicyAgentInterface;
import casa.socialcommitments.Action;
import casa.socialcommitments.SocialCommitment;
import casa.socialcommitments.SocialCommitmentsStore;
import casa.util.InstanceCounter;

import java.lang.reflect.Constructor;

/**
 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
public class AddSocialCommitment extends SocialCommitmentOperator {
	
	/**
	 */
	protected URLDescriptor debtor;
	/**
	 */
	protected URLDescriptor creditor;
	protected String performative;
	/**
	 */
	protected Act act;
	/**
	 */
	protected Event event;
	protected Class<? extends Action> actionClass;
	protected Object actionData;
	protected boolean shared;
	protected boolean persistent;
	/**
	 */
	protected Condition condition;
	/**
	 */
	protected Event[] events;
	
	protected boolean notDebtor = false;

	public AddSocialCommitment (URLDescriptor newDebtor, URLDescriptor newCreditor,
			String newPerformative, Act newAct, Event newEvent,
			Class<? extends Action> newActionClass, Object newActionData, boolean newShared, boolean newPersistent, Condition newCondition, Event[] newEvents) {

		debtor = newDebtor;
		creditor = newCreditor;
		performative = newPerformative;
		act = newAct;
		event = newEvent;
		actionClass = newActionClass;
		actionData = newActionData;
		shared = newShared;
		persistent = newPersistent;
		condition = newCondition;
		events = newEvents;
		InstanceCounter.add(this);
	}

	@Override
	public final Status executeOperator (SocialCommitmentsStore store, PolicyAgentInterface agent) {
		Status ret = new Status (0);
		SocialCommitment sc = createCommitment (store, agent);
		if (condition==null || condition.booleanValue(agent)) {
  		
  		if (sc != null) {
  			store.addCommitment (sc);
  			if (agent.isLoggingTag ("commitments2")) {
  				agent.println ("commitments2", "Operator adding commitment '" + sc.toString () + "'");
  			}
  		} else {
  			ret.setStatusValue (-50);
  			if (agent.isLoggingTag("commitments7")) 
  				agent.println ("commitments7", "Operator NOT adding commitment '" + sc.toString () + "'\n  due to failure to create Social Commitment.");
  		}
		}
		else if (agent.isLoggingTag("commitments7")) {
			agent.println ("commitments7", "Operator NOT adding commitment '" + sc.toString () + "'\n  due to failed condition: "+condition);
		}
		return ret;
	}

	protected SocialCommitment createCommitment (SocialCommitmentsStore store, PolicyAgentInterface agent) {
		SocialCommitment sc = null;
		
		if (notDebtor) {
			if (shared) {
				sc = new SocialCommitment (agent, debtor,creditor,performative,act,event,null,events);				
			}
		} else {
			Action action = createAction (agent);
			sc = new SocialCommitment (agent, debtor,creditor,performative,act,event,action,events);
			sc.setShared (shared);
		}
		
//		if (recurring)
//			sc.setRecurring(true);
		if (sc != null)
			sc.setPersistent(persistent);
		
		return sc;
	}

	protected Action createAction (PolicyAgentInterface agent) {
		Action action = null;
		
		if (actionData != null) {
			return createActionWithData (agent);
		}
		
		if (action == null && actionClass!=null) {
			try {
				Constructor<? extends Action> actionConstructor = actionClass.getConstructor ();

				action = actionConstructor.newInstance ();
			} catch (Throwable e) {
				agent.println ("error", "Unable to create Action object needed for the creation of a social commitment.",e);
				action = null;
			}
		}
		return action;
	}
	
	protected Action createActionWithData (PolicyAgentInterface agent) {
		Action action = null;
		try {
			Constructor<? /*extends Action*/>[] actionConstructors = actionClass.getConstructors ();
			Constructor<? /*extends Action*/> pickedActionConstructor = null;
			
			for (Constructor<? /*extends Action*/> c : actionConstructors) {
				Class<?>[] params = c.getParameterTypes ();
				if (params.length == 1 && params[0].isAssignableFrom (actionData.getClass ())) {
					pickedActionConstructor = c;
				}
			}
			
			if (pickedActionConstructor != null) {
				action = (Action)pickedActionConstructor.newInstance (actionData);
			}
		} catch (Throwable e) {
			agent.println ("error", "Unable to create paramterized Action object needed for the creation of a social commitment.",e);
			action = null;
		} 
		return action;
	}
	
	@Override
	public String toString () {
	    StringBuffer buf = new StringBuffer ();
	    buf.append ("Add: D: ");
	    buf.append (debtor.getShortestName ());

	    buf.append (", C: ");
	    buf.append (creditor.getShortestName ());

	    buf.append (", (");
	    buf.append (performative == null ? "*" : performative);
	    buf.append ("/");
	    buf.append (act == null ? "*" : act.toString ());
	    buf.append (")");

	    buf.append (", action-class: ");
	    buf.append (actionClass == null ? "null" : actionClass.getName ());

	    buf.append (shared ? ", shared" : ", not-shared");
	    buf.append (condition == null ? "" : " (conditional)");
	    buf.append (events == null ? "" : " (extra events)");

	    return buf.toString ();
	}

	/**
	 * @return
	 */
	public URLDescriptor getDebtor () {
		return debtor;
	}

	/**
	 * @return
	 */
	public URLDescriptor getCreditor () {
		return creditor;
	}

	/**
	 * @return
	 */
	public String getPerformative () {
		return performative;
	}

	/**
	 * @return
	 */
	public Act getAct () {
		return act;
	}

	/**
	 * @return
	 */
	public MLMessage getMessage () {
	  return (event instanceof MessageEvent) ? ((MessageEvent)event).getMessage() : null;
	}

	/**
	 * @return
	 */
	public Event getEvent () {
		return event;
	}

	/**
	 * @return
	 */
	public Class<? extends Action> getActionClass () {
		return actionClass;
	}

	public Object getActionData () {
		return actionData;
	}

	/**
	 * @return
	 */
	public boolean isShared () {
		return shared;
	}

	/**
	 * @return
	 */
	public boolean isNotDebtor () {
		return notDebtor;
	}

	/**
	 * @param newNotDebtor
	 */
	public void setNotDebtor (boolean newNotDebtor) {
		notDebtor = newNotDebtor;
	}

	/**
	 * Returns true if all of this object's protected variables are the same
	 * (except condition and events)
	 * 
	 * @param o
	 * @return boolean
	 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
	 */
	public boolean equals(SocialCommitmentOperator o) {
		if (o instanceof AddSocialCommitment){
			if (this.debtor.equals(((AddSocialCommitment)o).getDebtor())
					&& this.creditor.equals(((AddSocialCommitment)o).getCreditor())
					&& this.performative.equals(((AddSocialCommitment)o).getPerformative())
					&& this.act.equals(((AddSocialCommitment)o).getAct())
					&& this.event.equals(((AddSocialCommitment)o).getEvent())
					&& this.shared == ((AddSocialCommitment)o).isShared()){

//				if (this.actionClass.equals(((AddSocialCommitment)o).getActionClass())
//				&& this.actionData.equals(((AddSocialCommitment)o).getActionData())
				
				if (this.actionClass != null 
						&& ((AddSocialCommitment)o).getActionClass() != null){
					if (!this.actionClass.equals(((AddSocialCommitment)o).getActionClass())){
						return false;
					}
				} else{
					if (this.actionClass != (((AddSocialCommitment)o).getActionClass())){
						return false;
					}
				}
				
				//How do I compare cons lists? dsb
				if (this.actionData != null
						&& ((AddSocialCommitment)o).getActionData() != null){
//					if (!this.actionData.equals(((AddSocialCommitment)o).getActionData())){
//						return false;
//					}
				} else {
					if (this.actionData != (((AddSocialCommitment)o).getActionData())){
						return false;
					}
				}
				
				return true;
			}
		}
		return false;
	}
}