package casa.policy.sc3.actions;

import casa.PerformDescriptor;
import casa.interfaces.PolicyAgentInterface;
import casa.socialcommitments.Action;

public class MonitorAction extends Action {
    public MonitorAction () {
      super("monitor-action");
    }

    @Override
    public PerformDescriptor perform(PolicyAgentInterface agent) {
    	//return agent.monitor(ownerSC.getAct(), ownerSC.getMessage());
    	PerformDescriptor pd = null;
    	
  		//Does the conversation object have a relevant method?
//  		pd = ((TransientAgent)agent).getConversation(
//  				ownerSC.getMessage().getConversationID()).dispatchMsgHandlerMethod(
//  						"monitor", ownerSC.getAct(), ownerSC.getMessage());
    	
  		if (pd.getStatusValue() == -1)
  			pd = agent.dispatchMsgHandlerMethod("monitor", ownerSC.getAct(), ownerSC.getMessage()); 
  		
    	return pd;
    	//return agent.dispatchMsgHandlerMethod("monitor", ownerSC.getAct(), ownerSC.getMessage());
    }
  }