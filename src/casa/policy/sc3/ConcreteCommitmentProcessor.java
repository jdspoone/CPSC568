package casa.policy.sc3;

import casa.MLMessage;
import casa.PerformDescriptor;
import casa.TransientAgent;
import casa.interfaces.PolicyAgentInterface;
import casa.socialcommitments.SocialCommitment;
import casa.socialcommitments.SocialCommitmentStatusFlags;
import casa.socialcommitments.SocialCommitmentsStore;
import casa.util.InstanceCounter;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * Title: CASA Agent Infrastructure
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright 2003-2014, Knowledge Science Group, University of
 * Calgary. Permission to use, copy, modify, distribute and sell this software
 * and its documentation for any purpose is hereby granted without fee, provided
 * that the above copyright notice appear in all copies and that both that
 * copyright notice and this permission notice appear in supporting
 * documentation. The Knowledge Science Group makes no representations about the
 * suitability of this software for any purpose. It is provided "as is" without
 * express or implied warranty.
 * </p>
 * <p>
 * Company: Knowledge Science Group, University of Calgary
 * </p>
 * 
 * @author <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public class ConcreteCommitmentProcessor extends casa.CommitmentProcessor implements Comparable<ConcreteCommitmentProcessor> {

	/**
	 */
	protected SocialCommitmentsStore	store;
	/**
	 * Use for debugging. Records the last number of transactions we saw in the
	 * store so we can tell if anything potentially interesting has happened.
	 */
	protected long					StoreTransactionCount	= -1;		// an
																		// unlikely
																		// number
	/**
	 * Remember expired and fulfilled commitments for an hour
	 */
	protected final static long		expiredMemoryTime		= 360000;

	public ConcreteCommitmentProcessor(PolicyAgentInterface agent, SocialCommitmentsStore store) {
		super(agent);
		this.store = store;
		store.addObserver(agent);
		InstanceCounter.add(this);
	}

	/**
	 * If there are active social commitments in the store, choose one, and
	 * execute it and return true. If there are no active social commitments
	 * return false. A social commitment is chosen by calling
	 * {@link #choose(Collection)}.
	 * 
	 * @return true if it did something; otherwise false
	 */
	@Override
	public SocialCommitment processCommitments() {
		// >>>>>>debugging
		long tc = store.getTransactionCount();
		boolean printCommitments = false; // for debugging trace only
		if (tc != StoreTransactionCount) {
			if (agent.isLoggingTag("commitments"))
				if (agent.isLoggingTag("commitments2"))
					agent.println("commitments2", "before processCommitments() Agent " + agent.getName() + " has " + store.count(SocialCommitment.MASK_OUTSTANDING)
							+ " commitements:\n" + store.displayString(SocialCommitment.MASK_OUTSTANDING));
			StoreTransactionCount = tc;
			printCommitments = true;
		}
		// <<<<<debugging

		SocialCommitment chosen = choose();

		// execute the chosen commitment, if there was one
		processCommitment (chosen);

		// >>>>>>debugging
		if (tc != StoreTransactionCount || printCommitments) {
			if (agent.isLoggingTag("commitments"))
				agent.println("commitments", "After processCommitments() agent " + agent.getName() + " has " + store.count(SocialCommitment.MASK_OUTSTANDING)
						+ " commitements:\n" + store.displayString(SocialCommitment.MASK_OUTSTANDING));
			StoreTransactionCount = tc;
			printCommitments = true;
		}
		// >>>>>>debugging

		return chosen;
	}
	
	@Override
	public SocialCommitment processCommitment(SocialCommitment sc) {
		if (sc != null) {
			if (agent.isLoggingTag("commitments1"))
				agent.println("commitments1", "Agent " + agent.getName() + " executing commitment: " + sc.toString());// debugging
			PerformDescriptor status = perform(sc, agent);
			if (agent.isLoggingTag("commitments2"))
				agent.println("commitments2", "    -- returned result: " + status.toString());// debugging
		} 
		return sc;
	}
	
	@Override
	public boolean hasActiveCommitments() {
		if (getActiveCommitments().size()==0)
			return false;
		return chooseWithoutMarking()!=null;
	}
	
	protected List<SocialCommitment> getActiveCommitments(){
		// get the active social commitments
		// get all the commitments except for the not Effective yet ones
		List<SocialCommitment> coll;// = store.getCommitments(agent.getURL(), null, EnumSet.complementOf(EnumSet.of(SocialCommitmentStatusFlags.ENDED)));

		// if the SC has ended (fulfilled or violated) and exceeded the
		// memory-time threshold, delete it.
//		long now = System.currentTimeMillis();
//		long threshhold = now + expiredMemoryTime;
//		for (SocialCommitment sc : coll) {
//			if (sc.getEndTime() > threshhold) {
//				store.removeCommitment(sc);
//			}
//		}

		// if we got here, we dealt with getting rid of long-expired or long
		// fulfilled commitments,
		// and we deal with only active (started, ready, not-broken, not-fulfilled, has-action)
		// commitments
		coll = store.getCommitments(agent.getURL(), null, SocialCommitment.MASK_EXECUTABLE);
		
		return coll;
	}

	/**
	 * @return a single commitment from the active commitments in the store.
	 * @see #getActiveCommitments()
	 * @see #choose(List)
	 */
	@Override
	protected SocialCommitment choose() {
		List<SocialCommitment> commitments = getActiveCommitments();
		if (agent.isLoggingTag("commitments5")) {
		  agent.println("commitments5", "SocialCommitment.choose() called with "+commitments.size()+" active commitments of "+store.count(null)+" total commitments.");
		}
		if (commitments==null) 
			return null;
		int n = commitments.size();
		switch (n) {
		case 0:
			return null;
		case 1:
			return commitments.get(0);
		default:
			SocialCommitment chosen = choose(commitments);
			assert chosen != null;
			return chosen;
		}
	}

	/**
	 * Choose a commitment to focus on by first calling the owner agent's
	 * {@link TransientAgent#chooseSC(Collection)} method; if that method
	 * returns null (indicating it doesn't want to make the decision) then
	 * choose the first commitment that has a priority higher or equal to and
	 * other commitment. But if there is a more specific commitment than this
	 * one choose that one instead. Care is taken to mark previously chosen
	 * commitments so that if this method is called repeatedly with the same
	 * argument collection, it will not keep choosing the same commitment, but
	 * will iterate through the collection. In the event all the commitments
	 * have been marked, it unmarks them all, and proceeds as usual.
	 * 
	 * @param coll
	 *            The collection of commitments to pick from
	 * @return The chosen commitment
	 */
	protected SocialCommitment choose(List<SocialCommitment> coll) {
		final SocialCommitment chosen = chooseWithoutMarking(coll);
		
		// mark this as chosen once so we don't keep trying the same one over
		// and over.
		if (chosen != null)
			chosen.setMarked(true);
		
		return chosen;
	}
	
	protected SocialCommitment chooseWithoutMarking(){
		return chooseWithoutMarking(getActiveCommitments());
	}
	
	protected SocialCommitment chooseWithoutMarking(List<SocialCommitment> coll){
		if (coll == null || coll.isEmpty())
			return null;
		
		class SCComparitor implements Comparator<SocialCommitment> {

			@Override
			public int compare(SocialCommitment o1, SocialCommitment o2) {
				if(o1.getPriority() > o2.getPriority()){
					return -1;
				}else if (o1.getPriority() == o2.getPriority()){
					if (o1.getCreatedTime() < o2.getCreatedTime()) 
						return -1;
					else if (o1.getCreatedTime() == o2.getCreatedTime()) 
						return 0;
					else 
						return 1;
				}else
					return 1;
			}
		}
//		Collections.sort(coll, new SCComparitor());

		// build the collection 'unmarked' that has only unmarked SCs in it
		List<SocialCommitment> unmarked = new LinkedList<SocialCommitment>();
		for (SocialCommitment sc : coll)
			if (!sc.flagSet(SocialCommitmentStatusFlags.MARKED))
				unmarked.add(sc);

		// if all the SCs in coll were marked, then unmarked is empty: so unmark
		// them all
		if (unmarked.isEmpty()) {
			for (SocialCommitment sc : coll)
				sc.setMarked(false);
			unmarked.addAll(coll);
		}

		// give the agent a chance to choose
		SocialCommitment chosen = agent.chooseSC(unmarked); // agent can just return null to forego the choice
		if (chosen != null) {
			chosen.setMarked(true);
			return chosen;
		}

		// find the highest priority SC in unmarked SCs
		Collections.sort(unmarked, new SCComparitor());
		chosen = unmarked.get(0);

		// check for any more specific SCs
		chosen = findMoreSpecific(chosen, unmarked);
		
		assert chosen!=null;
		
		return chosen;
	}

	/**
	 * @param chosen
	 *            the current candidate commitment
	 * @param coll
	 *            the list of other possible candidate commitments to consider
	 * @return Finds the most specific commitment available (by subsumption)
	 */
	protected SocialCommitment findMoreSpecific(SocialCommitment chosen, Collection<SocialCommitment> coll) {
		boolean foundSubsumption;
		do {
			foundSubsumption = false;
			for (SocialCommitment sc : coll) {
				if (isSubsumedBy(sc, chosen)) {
					chosen = sc;
					foundSubsumption = true;
					break;
				}
			}
		} while (foundSubsumption);
		return chosen;
	}

	/**
	 * @param c1 the candidate child commitment
	 * @param c2 the candidate parent commitment
	 * @return whether c1 is more specific than c2.
	 */
	private boolean isSubsumedBy(SocialCommitment c1, SocialCommitment c2) {
		// Naively, the following expression is the return (see comment below)...
		boolean ret = c1 != c2 
				&& agent.isAAct(c1.getAct(), c2.getAct())
				&& agent.isAPerformative(c1.getPerformative(), c2.getPerformative());
		//  ...but if these are message commitments, the messages must be the same for them to be comparable.
		if (ret && c2.getMessage()!=null && c1.getMessage()!=null) {
			MLMessage m1 = c1.getMessage();
			MLMessage m2 = c2.getMessage();
			ret = m2.equals(m1);
		}
		// ...but if the two commitments are the same, we need to distinguish them
		// or risk violating the the partial ordering constraint on subsumption
		// relations, and possibly causing an infinite recursion somewhere...
		if (ret 
				&& c1.getAct().equals(c2.getAct())
				&& c1.getPerformative().equals(c2.getPerformative())) {
			ret = c1.getSerialNumber() < c2.getSerialNumber();
		}
		return ret;
	}

	protected PerformDescriptor perform(SocialCommitment commitment, PolicyAgentInterface agent) {
		// Are there SCs that subsume our commitment?
		// If so, we should just remove those commitments to save time and
		// bandwidth.
		// TreeSet subsumes = getSubsuming(agent, commitment);

		// do the action, but don't remove the commitment (we have to agree on
		// that with the creditor)
		// Action action = commitment.getAction();
		// Status stat = action!=null ? action.perform(agent) : new
		// Status(0,"null action");
		PerformDescriptor stat = commitment.execute(agent);
		if (stat == null)
			stat = new PerformDescriptor();
		int statusValue = stat.getStatusValue();
		if (statusValue != 0 && statusValue != TransientAgent.DEFER_ACTION) {
			String tag = statusValue > 0 ? "warning" : "error";
			if (agent.isLoggingTag(tag))
				agent.println(tag, "Commitment (\"" + commitment.toString() + "\") failed: " + stat.getStatus().getExplanation());
		}
		return stat;
	}

	@Override
	public String toString() {
		String ret = super.toString();
		ret += store.toString();
		return ret;
	}

	@Override
	public boolean isStoppable() {
		return !store.hasCommitment(SocialCommitment.MASK_OBLIGATED);
	}

	@Override
	public SocialCommitment peek() {
		return chooseWithoutMarking();
	}

	/**
	 * Return the social commitment store.
	 * @see casa.CommitmentProcessor#getStore()
	 * @return the social commitment store
	 */
	@Override
	public SocialCommitmentsStore getStore() {
		return store;
	}

	@Override
	public int compareTo(ConcreteCommitmentProcessor o) {
		return Integer.signum(o.hashCode()-this.hashCode());
	}

}
