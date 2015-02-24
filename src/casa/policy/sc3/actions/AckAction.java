package casa.policy.sc3.actions;

import casa.ML;
import casa.MLMessage;
import casa.PerformDescriptor;
import casa.interfaces.PolicyAgentInterface;
import casa.socialcommitments.Action;
import casa.socialcommitments.DependantSocialCommitment;

public class AckAction extends Action {
    public AckAction() {
      super("ack-action");
    }

    @Override
    public PerformDescriptor perform(PolicyAgentInterface agent) {
      MLMessage ack = MLMessage.constructReplyTo(ownerSC.getMessage(), agent.getUniqueRequestID(),agent.getURL());
      PerformDescriptor pd = ((DependantSocialCommitment) ownerSC).getGuardStatus();
      ack.setParameter(ML.PERFORMATIVE,(pd==null||pd.getStatusValue()>=0)?ML.ACK:ML.NACK);
      ack.setParameter(ML.ACT,ownerSC.getAct().toString());
      ack.setParameters(pd,ML.ACK,null,agent);
      return new PerformDescriptor(agent.sendMessage(ack));
    }
  }