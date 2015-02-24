/**
 * 
 */
package casa.auction.sealedbid;

import casa.Act;
import casa.ML;
import casa.MLMessage;
import casa.PerformDescriptor;
import casa.Status;
import casa.abcl.ParamsMap;
import casa.agentCom.URLDescriptor;
import casa.auction.AuctionAgent;
import casa.auction.Lot;
import casa.event.Event;
import casa.event.MessageEvent;
import casa.event.MessageEventDescriptor;
import casa.event.TimeEvent;
import casa.exceptions.URLDescriptorException;
import casa.transaction.Inventory;
import casa.ui.AgentUI;
import casa.util.CASAUtil;
import casa.util.Pair;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

/**
 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 *
 */
public class SealedBidAuctionAgent extends AuctionAgent {

	public final static int TIME_OUT = 5000;
	protected static boolean AUCTION_IS_OVER = false;
	
	//Used to determine event relevance
	protected MessageEventDescriptor agreeCryEvent = 
		new MessageEventDescriptor(
				this, ML.EVENT_MESSAGE_RECEIVED, 
				new Pair[]{
						new Pair<String, Object>(ML.PERFORMATIVE, ML.AGREE),
						new Pair<String, Object>(
								ML.ACT, new Act(
										new ArrayList<String>(
												Arrays.asList(
														ML.PROPOSE, CRY))))});
	
	protected URLDescriptor winningBidder;
	
	/**
	 * @param params
	 * @param ui
	 * @throws Exception
	 */
	public SealedBidAuctionAgent(ParamsMap params, AgentUI ui) throws Exception {
		super(params, ui);
	}
	
	/**
	 * Inherited from the abstract AuctionAgent class
	 * 
	 * @param msg
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
		updateActionText("Now accepting sealed bids for " + lot.getName());

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
				winningBidder = getMarketProfile().getHighestAgent(lot);
				
				//Did the winningBidder meet or exceed the auctioneer's reserve price?
				if (winningBidder == null || 
						getMarketProfile().getProfile().get(winningBidder).getProduct(lot.getName()).getPrice() < lot.getPrice()){
					updateActionText("No one could meet the reserve price of $" + lot.getPrice());
				}
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
	
	/**
	 * 
	 */
	public void handleEvent (Event event) {
		in("SealedBidAuctionAgent.handleEvent");

		//Is this the right kind of event?
		MLMessage msg;
		Status stat = agreeCryEvent.isApplicable(this, event);
		if (stat.getStatusValue()==0 && !AUCTION_IS_OVER){
			msg = ((MessageEvent)event).getMessage();
		} else {
			super.handleEvent(event);
			return;
		}
		
		//Who made the bid
		URLDescriptor bidder;
		String bidderName; 
		try {
			bidder = msg.getSender();
			bidderName = msg.getParameter(ML.SENDER).split("/")[msg.getParameter(ML.SENDER).split("/").length-1].split("\\?")[0];
		} catch (URLDescriptorException e2) {
			e2.printStackTrace();
			updateActionText("Who the heck sent that bid?");
			return;
		}
		
		//Has this bidder already submitted a bid (because its event is being requeued)?
		if (getMarketProfile().getProfile().containsKey(bidder)){ 
//				&& getMarketProfile().getProfile().get(bidder).getProducts().contains(incomingBid)){
			queueEvent(event);
			return;
		}
		
		//Get the bid
		Lot incomingBid;
		try {
			incomingBid = (Lot)CASAUtil.unserialize(msg.getParameter(ML.CONTENT), msg.getParameter(ML.LANGUAGE));
		} catch (ParseException e) {
			updateActionText("I could not understand the bid from " + bidderName);
			e.printStackTrace();
			return;
		}
		
		updateActionText(bidderName + " will pay $" + incomingBid.getPrice());
		
		//Save the bid for later...
//		Lot lot = getLots().get(incomingBid.getAuctionCD());
		getMarketProfile().add(bidder, incomingBid);
		
		//Requeue the event
		queueEvent(event);
		
		out("SealedBidAuctionAgent.agreeCry");
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
		in("SealedBidAuctionAgent.consider_offer_to_sell");

		//Is there a winning bidder?
		if (this.winningBidder == null){
			ret.setStatus(-1, "No one would meet the reserve price.");
			return ret;
		}
		
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

		//If the msg is not from the winningBidder, ignore
		if (!bidder.equals(this.winningBidder)){
			ret.setStatus(-1, "I'm afraid another agent out-bid you.");
			return ret;
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

		out("SealedBidAuctionAgent.consider_offer_to_sell");
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
		in("SealedBidAuctionAgent.consider_offer_to_sell");

		String sender = msg.getParameter(ML.SENDER).split("/")[msg.getParameter(ML.SENDER).split("/").length-1].split("\\?")[0];		
		
		//Get lot
		Lot lot;
		try {
			lot = (Lot)CASAUtil.unserialize(msg.getParameter(ML.CONTENT), msg.getParameter(ML.LANGUAGE));
		} catch (ParseException e) {
			updateActionText("Couldn't unserialize bid");
			ret.setStatus(new Status(-1, "Couldn't unserialize bid"));
			e.printStackTrace();
			return ret;
		}

		updateActionText(sender + " is selling " + lot.getName() + "?");

		
		//Get products the agent wants to purchase
		Inventory desiredProducts = getDesiredProducts();

		if (desiredProducts.getProducts().isEmpty()){
			updateActionText("No thanks, " + sender + ", I don't need anything");
			ret.put(ML.PERFORMATIVE, ML.REJECT_PROPOSAL);
			ret.put(ML.LANGUAGE, String.class.getName());
			ret.put(ML.CONTENT, "No thanks, I don't need anything");
			ret.setStatus(new Status(-1, "Don't need it."));
			return ret;
		}
		
		//Does the agent want the product being offered?  
		if (!desiredProducts.contains(lot.getName())){
			updateActionText("No thanks, I don't want " + lot.getName());
			ret.put(ML.PERFORMATIVE, ML.REJECT_PROPOSAL);
			ret.put(ML.LANGUAGE, String.class.getName());
			ret.put(ML.CONTENT, "No thanks, "+ sender + ", I don't want " + lot.getName());
			ret.setStatus(new Status(-1, "Don't want it."));
			return ret;
		}

		//Is the agent currently purchasing a product by the same name?  
		if (desiredProducts.getProduct(lot.getName()).isAcquiring()){
			updateActionText("Thanks for the offer, " + sender + ", but I'm in the process of buying " + lot.getName());
			ret.put(ML.PERFORMATIVE, ML.REJECT_PROPOSAL);
			ret.put(ML.LANGUAGE, String.class.getName());
			ret.put(ML.CONTENT, "Thanks for the offer, " + sender + ", but I'm in the process of buying " + lot.getName());
			ret.setStatus(new Status(-1, "Thanks for the offer, " + sender + ", but I'm in the process of buying " + lot.getName()));
			return ret;
		}
		

		//Does the buyer have any money?
		if (getBankRoll().getBalance() <= 0){
			updateActionText("D'oh! I don't have any money for " + lot.getName() + ", " + sender);
			ret.put(ML.PERFORMATIVE, ML.REJECT_PROPOSAL);
			ret.put(ML.LANGUAGE, String.class.getName());
			ret.put(ML.CONTENT, "D'oh! I don't have any money for " + lot.getName());
			ret.setStatus(new Status(-1, "No money!"));
			return ret;
		} 
		
		//Make an offer
		Integer offer = desiredProducts.getProduct(lot.getName()).getPrice();
		if (offer > getBankRoll().getBalance())
			offer = getBankRoll().getBalance();
		lot.setPrice(offer);
		
		desiredProducts.getProduct(lot.getName()).setAcquiring(true);
		updateActionText("I'll pay $" + offer + " for " + lot.getName());
		ret.put(ML.LANGUAGE, Lot.class.getName());
		ret.put(ML.CONTENT, CASAUtil.serialize(lot));
		ret.setStatus(new Status(0));
		
		out("SealedBidAuctionAgent.consider_offer_to_sell");
		return ret;
	}

}
