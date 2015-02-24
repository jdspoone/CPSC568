/**
 * 
 */
package casa.auction.dutch;

import casa.ML;
import casa.MLMessage;
import casa.PerformDescriptor;
import casa.Status;
import casa.abcl.ParamsMap;
import casa.agentCom.URLDescriptor;
import casa.auction.AuctionAgent;
import casa.auction.Lot;
import casa.event.RecurringTimeEvent;
import casa.exceptions.URLDescriptorException;
import casa.interfaces.PolicyAgentInterface;
import casa.ui.AgentUI;
import casa.util.CASAUtil;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Vector;

/**
 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 *
 */
public class DutchAuctionAgent extends AuctionAgent {

	private final static int BID_DELAY = 3000;
	private final static int BID_DECREMENT = 1;
	private final static int START_HIGH_BID = 50;

	
	private class RecurringCryEvent extends RecurringTimeEvent{
		
		public URLDescriptor auctionCD;
		
		public RecurringCryEvent (String newType, PolicyAgentInterface agent, 
				long newFirstTime,long newTimeBetweenOccurrences, URLDescriptor auctionCD){	
			super(newType, agent, newFirstTime, newTimeBetweenOccurrences);
			
			this.auctionCD = auctionCD;
			
		}
	}  
	
//	RecurringCryEvent cryNewPrice;
	HashMap<URLDescriptor, RecurringCryEvent> cryNewPriceEvents = new HashMap<URLDescriptor, RecurringCryEvent>();
	
	/**
	 * @param params
	 * @param ui
	 * @throws Exception
	 */
	public DutchAuctionAgent(ParamsMap params, AgentUI ui) throws Exception {
		super(params, ui);
	}

	/* (non-Javadoc)
	 * @see casa.Agent#init()
	 */
	@Override
	public void initializeAfterRegistered(boolean registered) {
		super.initializeAfterRegistered(registered);
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
		in("DutchAuctionAgent.consider_offer_to_sell");
//		cryNewPrice.cancel();
//		cryNewPrice.delete();
		
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

		cryNewPriceEvents.get(bid.getAuctionCD()).cancel();

		//Make sure the inventory price reflects the bid's price
		getInventory().update(bid);
		
		ret = super.perform_offer_to_sell(msg);
		if (ret.getStatusValue()==0){
			ret.put(ML.LANGUAGE, Lot.class.getName());
			ret.put(ML.CONTENT, CASAUtil.serialize(bid));
			ret.setStatus(0, bid.getName() + " for $" + bid.getPrice());
		} else {
			ret.setStatus(-1, "Something weird happened. I cannot sell you the requested item.");
		}

		//Let everyone know that the auction is over
		Vector<URLDescriptor> members = getMembers(bid.getAuctionCD());
		for (URLDescriptor m : members){
			MLMessage outMsg = getNewMessage(ML.INFORM, AuctionAgent.AUCTION_IS_OVER, m);
			outMsg.setParameter (ML.LANGUAGE, Lot.class.getName());
			outMsg.setParameter (ML.CONTENT, CASAUtil.serialize(bid));
			sendMessage(outMsg);
		}
			
		out("DutchAuctionAgent.consider_offer_to_sell");
		return ret;
	}
	
	/**
	 * Consider offer_to_sell (CLIENT)
	 * 
	 * @param msg
	 * @return PerformDescriptor
	 */
	public PerformDescriptor consider_offer_to_sell(MLMessage msg) {
		PerformDescriptor ret = new PerformDescriptor();
		in("DutchAuctionAgent.consider_offer_to_sell");

		Lot lot;
		try {
			lot = (Lot)CASAUtil.unserialize(msg.getParameter(ML.CONTENT), msg.getParameter(ML.LANGUAGE));
		} catch (ParseException e) {
			updateActionText("Couldn't unserialize bid");
			ret.setStatus(new Status(-1, "Couldn't unserialize bid"));
			e.printStackTrace();
			return ret;
		}
		
		//This has to be set to false, otherwise bidders won't raise their bids if necessary
		getDesiredProducts().getProduct(lot.getName()).setAcquiring(false);
		
		//Sometimes the CD lags in removing a member from its roster. If that's
		//the case, ignore the proposal
		if (!getJoinedCooperationDomains().contains(lot.getAuctionCD())){
			updateActionText("I don't want that, stop bugging me.");
			ret.setStatus(new Status(-1, "I don't want that, stop bugging me."));
			return ret;
		}
		
		ret = super.consider_offer_to_sell(msg);
		ret.put(ML.LANGUAGE, Lot.class.getName());
		ret.put(ML.CONTENT, CASAUtil.serialize(lot));
		
		out("DutchAuctionAgent.consider_offer_to_sell");
		return ret;
	}

	@Override
	public void startAuction(MLMessage msg) {
		URLDescriptor cd; 
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

		//Start the bidding high, as this is a Dutch Auction
		lot.setPrice(lot.getPrice()+START_HIGH_BID);
		
		Vector<URLDescriptor> members = getMembers(lot.getAuctionCD());

		//TODO: do something about this... dsb
		if (members==null){
			return;
		}

		//Start the bidding
		updateActionText("The bidding for " + lot.getName() + " starts at $" + lot.getPrice());

		cryNewPriceEvents.put(cd, new RecurringCryEvent( ML.EVENT_DEFERRED_EXECUTION, this, System.currentTimeMillis()+BID_DELAY, BID_DELAY, cd){
			@Override
			public void fireEvent () {

				Lot lot = ((AuctionAgent)agent).getLots().get(auctionCD);
				
				//The lot's starting bid is serving the purpose of a reserve bid here
				if (lot.getPrice() >= lot.getStartingBid()){
					updateActionText(lot.getName() + " for $" + lot.getPrice() + "?");
					
					Vector<URLDescriptor> members = getMembers(lot.getAuctionCD());
					
					for (URLDescriptor m : members){
						if (!m.equals(agent.getURL())){
							MLMessage outMsg = getNewMessage(ML.PROPOSE, AuctionAgent.CRY, m);
							outMsg.setParameter (ML.LANGUAGE, Lot.class.getName());
							outMsg.setParameter (ML.CONTENT, CASAUtil.serialize(lot));
							sendMessage(outMsg);
						}
					}
				
					//Reduce price for next round
					lot.setPrice(lot.getPrice()-BID_DECREMENT);
					((AuctionAgent)agent).addLot(lot);
				}
				else {
					//End the auction (TODO: send a message to CD members)
					updateActionText("Maybe I should rethink my reserve price...");
					
					Vector<URLDescriptor> members = getMembers(lot.getAuctionCD());
					
					for (URLDescriptor m : members){
						MLMessage outMsg = getNewMessage(ML.INFORM, AuctionAgent.AUCTION_IS_OVER, m);
						outMsg.setParameter (ML.LANGUAGE, Lot.class.getName());
						outMsg.setParameter (ML.CONTENT, CASAUtil.serialize(lot));
						sendMessage(outMsg);
					}
//					doWithdrawCD(lot.getAuctionCD(), false);
					this.cancel();
				}
				
			}
		});
		cryNewPriceEvents.get(cd).start();
	}	
	
}
