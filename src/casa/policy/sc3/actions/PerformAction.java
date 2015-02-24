package casa.policy.sc3.actions;

import casa.Act;
import casa.ML;
import casa.MLMessage;
import casa.PerformDescriptor;
import casa.TransientAgent;
import casa.interfaces.PolicyAgentInterface;
import casa.socialcommitments.Action;

public class PerformAction extends Action {
  public PerformAction () {
    super("perform-action");
  }

  @Override
  public PerformDescriptor perform(PolicyAgentInterface agent) {
  	PerformDescriptor pd = null;
  	//return agent.perform(ownerSC.getAct(), ownerSC.getMessage());
  	Act act = ownerSC.getAct();
  	MLMessage message = ownerSC.getMessage();
  	
  	
  	
		//Does the conversation object have a relevant method?
  	//
  	//Note: the true boolean value passed to dispatchMsgHandlerMethod means the method 
		//in question is "contextual". That is, and underscored methodName is not used here. 
//		if (((TransientAgent)agent).getConversation(message.getConversationID()) != null)
//			pd = ((TransientAgent)agent).getConversation(
//					message.getConversationID()).dispatchMsgHandlerMethod(
//							ML.PERFORM, act, message, true);

		//If no relevant method found, refer to TransientAgent (for now)
		//(Yikes! What a mess!)
		if (pd == null || pd.getStatusValue() == -1)
			pd = ((TransientAgent)agent).dispatchMsgHandlerMethod(
					ML.PERFORM, act, message);

  	
  	
		//Does the conversation object have a relevant method?
		/*pd = ((TransientAgent)agent).getConversation(
				message.getConversationID()).dispatchMsgHandlerMethod(
						"perform", act, message);*/
  	
		if (pd.getStatusValue() == -1)
			pd = agent.dispatchMsgHandlerMethod("perform", act, message);
		
  	return pd;
  	//return agent.dispatchMsgHandlerMethod("perform", act, msg);
  }
}