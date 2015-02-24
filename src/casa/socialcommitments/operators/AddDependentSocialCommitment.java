package casa.socialcommitments.operators;

import casa.Act;
import casa.agentCom.URLDescriptor;
import casa.event.Event;
import casa.interfaces.PolicyAgentInterface;
import casa.socialcommitments.Action;
import casa.socialcommitments.DependantSocialCommitment;
import casa.socialcommitments.SocialCommitment;
import casa.socialcommitments.SocialCommitmentDescriptor;
import casa.socialcommitments.SocialCommitmentsStore;

import java.util.Collection;

/**
 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
public class AddDependentSocialCommitment extends AddSocialCommitment {
	
	protected SocialCommitmentDescriptor scDescriptor;
	
	public AddDependentSocialCommitment (SocialCommitmentDescriptor scDescriptor, URLDescriptor debtor, URLDescriptor creditor,
			String performative, Act act, Event event,
			Class<? extends Action> actionClass, Object actionData, boolean shared, boolean persistent, Condition condition, Event[] events) {
		super (debtor, creditor, performative, act, event,
				actionClass, actionData, shared, persistent, condition, events);
		
		this.scDescriptor = scDescriptor;
	}

	@Override
	protected SocialCommitment createCommitment (SocialCommitmentsStore store, PolicyAgentInterface agent) {
		SocialCommitment sc = null;
		
		if (notDebtor) {
			if (shared) {
				sc = new SocialCommitment (agent, debtor,creditor,performative,act,event,null,events);				
			}
		} else {
			Action action = createAction (agent);
			SocialCommitment guard = findGuard (store, agent);
			sc = new DependantSocialCommitment (agent, guard, debtor,creditor,performative,act,event,action,events);
			sc.setShared (shared);
		}
		return sc;
	}

	protected SocialCommitment findGuard (SocialCommitmentsStore store, PolicyAgentInterface agent) {
		Collection<SocialCommitment> c = store.getCommitments(debtor,creditor,SocialCommitment.MASK_OUTSTANDING);

//		// clean up the values we want to match
//		String perfX = dependentPerformative==null ? ML.TOP : dependentPerformative;
//		Act actX = dependentAct==null ? new Act(ML.TOP) : dependentAct;

		SocialCommitment guard = null;

		for (SocialCommitment sc: c) {
//			// get and clean up the values in the SC we will be comparing
//			String perfSC = sc.getPerformative(); if (perfSC==null) perfSC = /*ML.PERFORMATIVE*/ML.TOP;
//			Act actSC = sc.getAct(); if (actSC==null) actSC = new Act(ML.TOP);

			if (event.equals (sc.getEvent ()) &&
					scDescriptor.isApplicable(agent, sc).getStatusValue()==0
//					agent.isA (perfSC, perfX) &&
//					agent.isAAct (actSC, actX)
					) {

				guard = sc;
			}
		}
		
		return guard;
	}

	@Override
	public String toString () {
		StringBuffer buf = new StringBuffer (super.toString ());
		
	    buf.append (", depends on (");
	    buf.append(scDescriptor.toString());
	    buf.append (")");

		return buf.toString ();
	}
}