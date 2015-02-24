package casa.socialcommitments.operators;

import casa.Status;
import casa.interfaces.PolicyAgentInterface;
import casa.socialcommitments.SocialCommitmentsStore;

public abstract class SocialCommitmentOperator implements Comparable<SocialCommitmentOperator> {
	
	public abstract Status executeOperator (SocialCommitmentsStore store, PolicyAgentInterface agent);
	
	@Override
	public abstract String toString ();
	
	@Override
	public int compareTo(SocialCommitmentOperator o) {
		return Integer.signum(o.hashCode()-this.hashCode());
	}

}
