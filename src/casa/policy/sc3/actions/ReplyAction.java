package casa.policy.sc3.actions;

import casa.ML;
import casa.MLMessage;
import casa.PerformDescriptor;
import casa.interfaces.PolicyAgentInterface;
import casa.socialcommitments.Action;
import casa.socialcommitments.DependantSocialCommitment;

public class ReplyAction extends Action {
    public ReplyAction() {
      super("reply-action");
    }

    @Override
    public PerformDescriptor perform(PolicyAgentInterface agent) {
      MLMessage inMsg = ownerSC.getMessage();
      MLMessage prop = MLMessage.constructReplyTo(inMsg,agent.getUniqueRequestID(),agent.getURL());
      PerformDescriptor pd = ((DependantSocialCommitment) ownerSC).getGuardStatus();
      prop.setParameter(ML.PERFORMATIVE,getReturnPerformative(agent,inMsg.getParameter(ML.PERFORMATIVE),pd==null||pd.getStatusValue()>=0));
//      Act act = prop.getAct();
//      prop.setParameter(ML.ACT,(act==null?new Act(ML.REQUEST):act.push(ML.REQUEST)).toString());
      prop.setParameter(ML.ACT,ownerSC.getAct().toString());
//      prop.setParameter(ML.REPLY_WITH,agent.getUniqueRequestID());
      if (pd!=null) {
        //prop.setParameter(ML.CONTENT, CASAUtil.serialize(pd.getStatus()));
        prop.setParameters(pd, ML.REPLY, null, agent);
      }
      return new PerformDescriptor(agent.sendMessage(prop));
    }
    
    /**
     * Determines the correct return message performative:
     * <table>
     * <tr><th><code>requestPerform</code></th><th>affirmative </th><th>negative</th></tr>
     * <tr><td>{@link ML#REQUEST}  </td><td>{@link ML#AGREE}   </td><td>{@link ML#REFUSE}</td></tr>
     * <tr><td>{@link ML#SUBSCRIBE}</td><td>{@link ML#CONTRACT}</td><td>{@link ML#REFUSE}</td></tr>
     * </table>
     * @param affirmative set to true to get the normal affirmative reply; false to get the normal negative reply.
     * @param requestPerformative the performative of the original request
     * @return as in the table above.
     */
    private static String getReturnPerformative(PolicyAgentInterface agent, String requestPerformative, boolean affirmative) {
    	return affirmative ? ML.AGREE : ML.REFUSE;
//    	  agent.isA(requestPerformative,ML.SUBSCRIBE)
//    	    ?(affirmative ?ML.CONTRACT :ML.REFUSE) 
//    	    :(affirmative ?ML.AGREE    :ML.REFUSE);
    }
}