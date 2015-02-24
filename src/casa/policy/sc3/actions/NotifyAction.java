package casa.policy.sc3.actions;

import casa.Act;
import casa.ML;
import casa.MLMessage;
import casa.PerformDescriptor;
import casa.interfaces.PolicyAgentInterface;
import casa.socialcommitments.Action;
import casa.socialcommitments.DependantSocialCommitment;
import casa.util.CASAUtil;

public  class NotifyAction extends Action {
  public NotifyAction() {
    super("notify-action");
  }

  @Override
  public PerformDescriptor perform(PolicyAgentInterface agent) {
    MLMessage prop=null;
    prop = ownerSC.getMessage().clone();
    PerformDescriptor pd = ((DependantSocialCommitment) ownerSC).getGuardStatus();
    if (pd==null) {
      ownerSC.designateFulfilled();
    } else {
      prop.setParameter(ML.PERFORMATIVE,ML.NOTIFY);
      Act a = prop.getAct();
      assert(agent.isAAct(a,new Act(ML.SUBSCRIBE)));
      prop.setParameter(ML.ACT,a==null?null:a.pop().toString());
      prop.setParameter(ML.REPLY_WITH,agent.getUniqueRequestID());
      prop.setParameter(ML.CONTENT, CASAUtil.serialize(pd.getStatus()));
      prop.setParameters(pd, ML.INFORM, null, agent);
    }
    return new PerformDescriptor(agent.sendMessage(prop));
  }
}