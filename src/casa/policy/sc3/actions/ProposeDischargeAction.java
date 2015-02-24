package casa.policy.sc3.actions;

import casa.ML;
import casa.MLMessage;
import casa.PerformDescriptor;
import casa.interfaces.PolicyAgentInterface;
import casa.socialcommitments.Action;
import casa.socialcommitments.DependantSocialCommitment;
import casa.util.CASAUtil;

public class ProposeDischargeAction extends Action {
	
  // TODO Figure out how to deal with the reversal despite needing constructor without parameters
  public ProposeDischargeAction (/*boolean reverseMsgDirection*/) {
    super("proposeDischarge-action");
    }

  @Override
  public PerformDescriptor perform(PolicyAgentInterface agent) {
    MLMessage prop=null;
    prop = ownerSC.getMessage().clone();
    boolean rev = false;
    if (agent.isA(prop.getAct().peek(),ML.PROPOSE)) rev = true;
    if (rev) {
    	prop.reverseDirection();
    	prop.setParameter(ML.IN_REPLY_TO,prop.getParameter(ML.REPLY_WITH));
    	prop.removeParameter(ML.REPLY_WITH);
    }
    PerformDescriptor pd = ((DependantSocialCommitment) ownerSC).getGuardStatus();
    prop.setParameter(ML.PERFORMATIVE,(pd==null||pd.getStatusValue()>=0)?ML.SUCCESS:ML.FAILURE);
    //Act a = prop.getAct();
    //assert(agent.isAAct(a,new Act(ML.REQUEST)));
    String actString = ((ownerSC.getAct()==null)?null:(ownerSC.getAct().toString()));
    prop.setParameter(ML.ACT,actString);
    //prop.setParameter(ML.IN_REPLY_TO,prop.getParameter(ML.REPLY_WITH));
    prop.setParameter(ML.REPLY_WITH,agent.getUniqueRequestID());
    if (pd!=null) {
      prop.setParameter(ML.CONTENT, CASAUtil.serialize(pd.getStatus()));
      try {
        prop.setParameters(pd, ML.PROPOSE, /*ML.DISCHARGE*/prop.getParameter(ML.ACT), agent);
      }
      catch (IllegalArgumentException ex) {
        agent.println("error","Bad update on message",ex);
      }
    }
    return new PerformDescriptor(agent.sendMessage(prop));
  }

}