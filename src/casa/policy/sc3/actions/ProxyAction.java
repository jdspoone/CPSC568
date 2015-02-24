package casa.policy.sc3.actions;

import casa.ML;
import casa.MLMessage;
import casa.PerformDescriptor;
import casa.Status;
import casa.StatusObject;
import casa.agentCom.URLDescriptor;
import casa.interfaces.PolicyAgentInterface;
import casa.socialcommitments.Action;
import casa.socialcommitments.DependantSocialCommitment;
import casa.util.Pair;
import casa.util.PairComparable;

import java.util.List;

public class ProxyAction extends Action {
    public ProxyAction() {
      super("forward-action");
    }

    @SuppressWarnings("unchecked")
    @Override
    public PerformDescriptor perform(PolicyAgentInterface agent) {
      Status stat = new Status(0);
      PerformDescriptor pd = ((DependantSocialCommitment) ownerSC).getGuardStatus();
      boolean noForwardees = false;
      boolean someErrors = false;
      
      if (pd!=null && pd.getStatusValue() == 0 && pd.getStatus () instanceof StatusObject) {
        
        StatusObject<Pair<List<URLDescriptor>, MLMessage>> stat2 = 
          (StatusObject<Pair<List<URLDescriptor>, MLMessage>>) pd.getStatus ();
        
        Pair<List<URLDescriptor>, MLMessage> info = null;
        if (stat2.getObject () instanceof PairComparable) {
          info = stat2.getObject ();

          if (! (info.getFirst () instanceof List &&
              info.getSecond () instanceof MLMessage)) {
            noForwardees = true;
          }
        } else {
          noForwardees = true;
        }

        if (!noForwardees) {
          for (URLDescriptor recipient : info.getFirst ()) {
        	MLMessage message = info.getSecond ().clone ();
        	
            message.setParameter (ML.RECEIVER, recipient.toString (agent.getURL ()));

            stat = agent.sendMessage (message);
            if (stat.getStatusValue() != 0) {
              someErrors = true;
            }
          }
        }
      }
      
      if (noForwardees) {
        // TODO - no forwardees, should/how do we NACK?
      }
      if (someErrors) {
        // TODO - handle errors better (combine them?)
      }
      return new PerformDescriptor(stat);
    }
  }