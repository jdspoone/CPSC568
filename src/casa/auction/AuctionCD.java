/**
 * 
 */
package casa.auction;

import casa.CooperationDomain;
import casa.MLMessage;
import casa.PerformDescriptor;
import casa.Status;
import casa.abcl.ParamsMap;
import casa.agentCom.URLDescriptor;
import casa.ui.AgentUI;

/**
 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 *
 */
public class AuctionCD extends CooperationDomain {

	
//	private boolean auctionStarted = false;
	
	 /**
   * Creates a new <code>AuctionCD</code>.
   *
   * @param params
   * @param ui
   * @throws Exception If the agent creation fails for any reason.
   */
  public AuctionCD(ParamsMap params, AgentUI ui) throws Exception {
    super(params, ui);
    in("AuctionCD.AuctionCD");
    out("AuctionCD.AuctionCD");
  }
	
  
  
	public Status withdraw(URLDescriptor member) {
		Status result = super.withdraw(member);
		in("AuctionCD.withdraw");

		if(members.size()==0)
			exit();
		
		out("AuctionCD.withdraw");
		return result;
	}

	protected PerformDescriptor conclude_membership_change(MLMessage message) {
		PerformDescriptor result = super.conclude_membership_change(message);
		in("AuctionCD.conclude_membership_change");
		
		if (membershipSubscriptions.size()==0)
			exit();
		
		out("AuctionCD.conclude_membership_change");
		return result;
	}
  
}
