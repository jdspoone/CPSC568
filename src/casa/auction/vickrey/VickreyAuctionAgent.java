/**
 * 
 */
package casa.auction.vickrey;

import casa.ML;
import casa.MLMessage;
import casa.PerformDescriptor;
import casa.Status;
import casa.abcl.ParamsMap;
import casa.agentCom.URLDescriptor;
import casa.auction.AuctionAgent;
import casa.auction.Lot;
import casa.auction.sealedbid.SealedBidAuctionAgent;
import casa.event.TimeEvent;
import casa.exceptions.URLDescriptorException;
import casa.ui.AgentUI;
import casa.util.CASAUtil;

import java.text.ParseException;
import java.util.Vector;

/**
 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 *
 */
public class VickreyAuctionAgent extends SealedBidAuctionAgent {

	/**
	 * @param params
	 * @param ui
	 * @throws Exception
	 */
	public VickreyAuctionAgent(ParamsMap params, AgentUI ui) throws Exception {
		super(params, ui);
	}

	/**
	 * Override the SealedBidAuction agent's implementation of the method from the inherited abstract AuctionAgent's class
	 */
	@Override
	public void startAuction(MLMessage msg){
		AUCTION_IS_OVER = false;
		winningBidder = null;
		resetMarketProfile();

		final URLDescriptor cd; 
		try {
			cd = msg.getReceiver();
		} catch (URLDescriptorException e) {
			updateActionText("Could not unserialize receiver.");
			e.printStackTrace();
			return;
		}

		//Get the lot 
		Lot lot = getLots().get(cd);

		//If true, this agent is not the auctioneer
		if (lot == null)
			return;

		//The price is the agent's reserved price
		lot.setPrice(lot.getPrice());
		
		Vector<URLDescriptor> members = getMembers(lot.getAuctionCD());

		//TODO: do something about this... dsb
		if (members==null){
			return;
		}

		//Start the bidding
		updateActionText("Now accepting Vickrey bids for " + lot.getName());

		for (URLDescriptor m : members){
			if (!m.equals(getURL())){
				MLMessage outMsg = getNewMessage(ML.PROPOSE, AuctionAgent.CRY, m);
				outMsg.setParameter (ML.LANGUAGE, Lot.class.getName());
				outMsg.setParameter (ML.CONTENT, CASAUtil.serialize(lot));
				sendMessage(outMsg);
			}
		}
		
		//Set a time limit in which to accept bids
		new TimeEvent(ML.EVENT_DEFERRED_EXECUTION, this, System.currentTimeMillis()+TIME_OUT){
			@Override
			public void fireEvent(){
				Lot lot = ((AuctionAgent)agent).getLots().get(cd);
				Integer reservePrice = lot.getPrice();
				winningBidder = getMarketProfile().getHighestAgent(lot);
				getMarketProfile().remove(winningBidder);
				
				//Set the price to what the runner up bid, if there is a runner up bid
				URLDescriptor runnerUpBidder = getMarketProfile().getHighestAgent(lot);
				if (runnerUpBidder != null){
					lot.setPrice(getMarketProfile().getProfile().get(runnerUpBidder).getProduct(lot.getName()).getPrice());
					((AuctionAgent)agent).addLot(lot);
					
					if (reservePrice > lot.getPrice()){
						winningBidder = null;
					}
					
					//Did the winningBidder meet or exceed the auctioneer's reserve price?
					if (winningBidder == null || 
							getMarketProfile().getProfile().get(runnerUpBidder).getProduct(lot.getName()).getPrice() < reservePrice){
						updateActionText("No one could meet the reserve price of $" + lot.getPrice());
					}
				} 

				//Did anyone meet or exceed the reserve price?
				if (winningBidder == null && runnerUpBidder == null)
					updateActionText("No one could meet the reserve price of $" + lot.getPrice());

				
				AUCTION_IS_OVER = true;
				
				//Inform all agents that the auction is over
				Vector<URLDescriptor> members = getMembers(lot.getAuctionCD());
				for (URLDescriptor m : members){
					MLMessage outMsg = getNewMessage(ML.INFORM, AuctionAgent.AUCTION_IS_OVER, m);
					outMsg.setParameter (ML.LANGUAGE, Lot.class.getName());
					outMsg.setParameter (ML.CONTENT, CASAUtil.serialize(lot));
					sendMessage(outMsg);
				}
			}
		}.start();
	}
	
  //////////////////////////////////////////////////////////////////////////////
  // OFFER_TO_SELL /////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

	/**
	 * Perform offer_to_sell (SERVER)
	 * 
	 * @param msg
	 * @return PerformDescriptor
	 */
	public PerformDescriptor perform_offer_to_sell(MLMessage msg) {
		PerformDescriptor ret = new PerformDescriptor();
		in("VickreyAuctionAgent.perform_offer_to_sell");
		
		//Who made the bid
		URLDescriptor bidder;
		String bidderName;
		try {
			bidder = (URLDescriptor)msg.getSender();
			bidderName = msg.getParameter(ML.SENDER).split("/")[msg.getParameter(ML.SENDER).split("/").length-1].split("\\?")[0];
		} catch (URLDescriptorException e1) {
			e1.printStackTrace();
			return new PerformDescriptor(new Status(-1, "Could not retrieve sender's URL"));
		}

		//Get the bid
		Lot bid;
		try {
			bid = (Lot)CASAUtil.unserialize(msg.getParameter(ML.CONTENT), msg.getParameter(ML.LANGUAGE));
		} catch (ParseException e) {
			updateActionText("I could not understand the bid from " + bidderName);
			e.printStackTrace();
			return new PerformDescriptor(new Status(-1, "I could not understand the bid from " + bidderName));
		}
		
		//Make sure the inventory price reflects the runner up bid's price
//		Lot lot = getLots().get(bid.getAuctionCD());
//		getInventory().update(lot);
		bid = getLots().get(bid.getAuctionCD());
		msg.setParameter(ML.CONTENT, CASAUtil.serialize(bid));
		
		ret = super.perform_offer_to_sell(msg);

		out("VickreyAuctionAgent.perform_offer_to_sell");
		return ret;
	}
	
}
