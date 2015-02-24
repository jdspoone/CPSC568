package casa.policy.sc3;

import casa.MLMessage;
import casa.interfaces.PolicyAgentInterface;
import casa.socialcommitments.operators.SocialCommitmentOperator;

public interface Rule {

	SocialCommitmentOperator process (MLMessage message, PolicyAgentInterface agent);

}
