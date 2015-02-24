package casa.policy.sc3.actions;

import casa.MLMessage;
import casa.PerformDescriptor;
import casa.TransientAgent;
import casa.interfaces.PolicyAgentInterface;
import casa.socialcommitments.Action;

/**
 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 */

public class ConsiderAction extends Action {

	public ConsiderAction () {
		super ("consider-action");
	}

	@Override
	public PerformDescriptor perform (PolicyAgentInterface agent) {
  		PerformDescriptor pd = null;
  		
  		String considerType = getOwner().getPerformative();
  		MLMessage message = getOwner().getMessage();

    	String methodName = considerType + "_" + message.getAct().toStringSimplify(-1);

    	
    	
  		//Does the conversation object have a relevant method?
			//
  		//Note: the true boolean value passed to dispatchMsgHandlerMethod means the method 
			//in question is "contextual". That is, an underscored methodName is not used here. 
//  		if (((TransientAgent)agent).getConversation(message.getConversationID()) != null)
//  			pd = ((TransientAgent)agent).getConversation(
//  					message.getConversationID()).dispatchMsgHandlerMethod(
//  							considerType, message.getAct(), message, true);
  		//If no relevant method found, refer to TransientAgent (for now)
  		if (pd == null || pd.getStatusValue() == -1)
  			pd = ((TransientAgent)agent).dispatchMsgHandlerMethod(considerType, message.getAct(), message);
  		
  		return pd;
	}
}