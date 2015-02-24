/**
 * 
 */
package casa.auction.english;

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
import casa.transaction.Inventory;
import casa.transaction.Product;
import casa.ui.AgentUI;
import casa.util.CASAUtil;

import java.text.ParseException;
import java.util.Vector;

/**
 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 *
 */
public class EnglishAuctionAgent extends AuctionAgent {

	private final static int BID_DELAY = 1000;
	private final static int BID_INCREMENT = 1;
	
	private MLMessage currentBidMessage = null;
	
	//Used to determine event relevance
//	private MessageEventDescriptor agreeCryEvent = 
//		new MessageEventDescriptor(
//				this, ML.EVENT_MESSAGE_RECEIVED, 
//				new Pair[]{
//						new Pair<String, Object>(ML.PERFORMATIVE, ML.AGREE),
//						new Pair<String, Object>(
//								ML.ACT, new Act(
//										new ArrayList<String>(
//												Arrays.asList(
//														ML.PROPOSE, CRY))))});
	
	RecurringTimeEvent reconsiderCurrentBid = null; 
//		new RecurringTimeEvent(ML.EVENT, this, System.currentTimeMillis(), BID_DELAY){
//		@Override
//		public void fireEvent () {
////			System.out.print("Fired");
//			if (currentBidMessage != null){
////				System.out.print(": message processed");
//				perform_offer_to_sell(currentBidMessage);
//			}
////			System.out.println();
//		}
//	};
	
	/**
	 * @param params
	 * @param ui
	 * @throws Exception
	 */
	public EnglishAuctionAgent(ParamsMap params, AgentUI ui) throws Exception {
		super(params, ui);
	}

	/* (non-Javadoc)
	 * @see casa.Agent#init()
	 */
	@Override
	public void initializeAfterRegistered(boolean registered) {
		super.initializeAfterRegistered(registered);
	}

	/**
	 * 
	 */
//	public void handleEvent (Event event) {
//		in("EnglishAuctionAgent.handleEvent");
//
//		//Is this the right kind of event?
//		MLMessage msg;
//		Status stat = agreeCryEvent.isApplicable(this, event);
//		if (stat.getStatusValue()==0){
//			msg = ((MessageEvent)event).getMessage();
//		} else {
//			super.handleEvent(event);
//			return;
//		}
//		
//		//Who made the bid
//		URLDescriptor bidder;
//		String bidderName; 
//		try {
//			bidder = msg.getSender();
//			bidderName = msg.getParameter(ML.SENDER).split("/")[msg.getParameter(ML.SENDER).split("/").length-1].split("\\?")[0];
//		} catch (URLDescriptorException e2) {
//			e2.printStackTrace();
//			updateActionText("Who the heck sent that bid?");
//			return;
//		}
//		
//		//Get the bid
//		Lot incomingBid;
//		try {
//			incomingBid = (Lot)CASAUtil.unserialize(msg.getParameter(ML.CONTENT));
//		} catch (ParseException e) {
//			updateActionText("I could not understand the bid from " + bidderName);
//			e.printStackTrace();
//			return;
//		}
//		
//		//Is it a bid worth considering?
//		Lot lot = getLots().get(incomingBid.getAuctionCD());
//
//		if (incomingBid.getPrice() > lot.getPrice() || lot.getPreviousBidder()==null){
//			incomingBid.setCurrentBidder(bidder);
//			lot.update(incomingBid);
//			queueEvent(event);
//			
//			//Make the announcement
//			//This tends to throw a concurrent modification exception when too much 
//			//is going on (hence the cloning)
//			Vector<URLDescriptor> members = 
//				(Vector<URLDescriptor>)getMembers(incomingBid.getAuctionCD()).clone();
//			if (members != null){
//				updateActionText("I hear $" + incomingBid.getPrice() + " from " + bidderName);
//				
//				for (URLDescriptor url : members){
////				for (URLDescriptor url : getMembers(incomingBid.getAuctionCD())){
//					//The auctioneer doesn't send a message to itself
//					if (!url.equals(getURL()) 
//							&& !url.equals(getLACURL())
//							&& !url.equals(incomingBid.getAuctionCD())){
//						sendMessage(ML.INFORM, I_HEAR, url,
//								ML.LANGUAGE, Lot.class.getName(),
//								ML.CONTENT, CASAUtil.serialize(incomingBid));
//					}
//				}
//			}
//
//			//Call for new bids
////			members = getMembers(incomingBid.getAuctionCD());
//			members = (Vector<URLDescriptor>)getMembers(incomingBid.getAuctionCD()).clone();
//			if (members != null){
//				//Raise the price of the product (but don't commit price change to lot yet)
//				incomingBid.setPrice(incomingBid.getPrice() + BID_INCREMENT);
//				updateActionText("Do I hear $" + incomingBid.getPrice() + "?");
//				for (URLDescriptor url : members){
////				for (URLDescriptor url : getMembers(incomingBid.getAuctionCD())){
//					//The auctioneer doesn't send a message to itself
//					if (!url.equals(getURL()) 
//							&& !url.equals(getLACURL())
//							&& !url.equals(incomingBid.getAuctionCD())){
//						sendMessage(ML.PROPOSE, CRY, url,
//								ML.LANGUAGE, Product.class.getName(),
//								ML.CONTENT, CASAUtil.serialize(incomingBid));
//					}
//				}
//			}
//		}	else if (!bidder.equals(lot.getCurrentBidder())){
//				super.handleEvent(event);
//				return;
//		}
//		
//		//If a fresh bid has not been received, initiate goingGoingGone sequence
//		if (!lot.biddingIsLocked()){
//			if (bidder.equals(lot.getCurrentBidder())){
//				switch (lot.goingGoingGone){
//					case Lot.CALLING_FOR_BIDS:
//						lot.goingGoingGone = Lot.GOING_ONCE;
//						break;
//					case Lot.GOING_ONCE:
//						lot.goingGoingGone = Lot.GOING_TWICE;
//						updateActionText("Going once...");
//						break;
//					case Lot.GOING_TWICE:
//						lot.goingGoingGone = Lot.GONE;
//						updateActionText("Going twice...");
//						break;
//					case Lot.GONE:
//						lot.goingGoingGone = Lot.AUCTION_IS_OVER;
//						updateActionText("Gone!");
//						break;
//				}
//				
//				if (lot.goingGoingGone == Lot.AUCTION_IS_OVER)
//					super.handleEvent(event);					
//				else
//					queueEvent(event);
//				
//			} else {
//				lot.goingGoingGone = Lot.CALLING_FOR_BIDS;
//				super.handleEvent(event);
//			}
//		} else {
//			lot.goingGoingGone = Lot.CALLING_FOR_BIDS;
//			lot.unlockBidding();
//		}
//		
//		out("EnglishAuctionAgent.handleEvent");
//	}
	
  //////////////////////////////////////////////////////////////////////////////
  // I_HEAR ////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  
	/**
	 * Consider the current high bid. (Client)
	 * 
	 * @param msg The incoming message
	 * @return The result of the processing; the status part will influence the return
	 */
	public PerformDescriptor consider_i_hear(MLMessage msg) {
		in("EnglishAuctionAgent.consider_i_hear");
		PerformDescriptor ret = new PerformDescriptor();
		
		Lot lot;
		try {
			lot = (Lot)CASAUtil.unserialize(msg.getParameter(ML.CONTENT), msg.getParameter(ML.LANGUAGE));
		} catch (ParseException e) {
			updateActionText("Couldn't unserialize highest bidder");
			ret.setStatus(new Status(-1, "Couldn't unserialize highest bidder"));
			e.printStackTrace();
			return ret;
		}
		
		URLDescriptor highestBidder = lot.getCurrentBidder();
		if (getURL().equals(highestBidder))
			lot.setHighestBidder(true);
		else
			lot.setHighestBidder(false);
		addBid(lot);
		
		out("EnglishAuctionAgent.consider_i_hear");
		return ret;
	}
	
  //////////////////////////////////////////////////////////////////////////////
  // OFFER_TO_SELL /////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

	/**
	 * The EnglishAuctionAgent needs to process multiple incoming agree messages to
	 * its initiating cry
	 * 
	 * @param msg The incoming message
	 */
//	public void consider_agree_cry(MLMessage msg){
////		PerformDescriptor ret = new PerformDescriptor();
//		in("EnglishAuctionAgent.consider_agree_cry");
//		
//		//Who made the bid
//		URLDescriptor bidder;
//		String bidderName; 
//		try {
//			bidder = msg.getSender();
//			bidderName = msg.getParameter(ML.SENDER).split("/")[msg.getParameter(ML.SENDER).split("/").length-1].split("\\?")[0];
//		} catch (URLDescriptorException e2) {
//			e2.printStackTrace();
//			updateActionText("Who the heck sent that bid?");
//			return;
//		}
//		
//		//Get the bid
//		Lot incomingBid;
//		try {
//			incomingBid = (Lot)CASAUtil.unserialize(msg.getParameter(ML.CONTENT));
//		} catch (ParseException e) {
//			updateActionText("I could not understand the bid from " + bidderName);
//			e.printStackTrace();
//			return;
//		}
//		
//		//Is it a bid worth considering?
//		Lot lot = getLots().get(incomingBid.getAuctionCD());
//
//		if (incomingBid.getPrice() > lot.getPrice() || lot.getPreviousBidder()==null){
//			incomingBid.setCurrentBidder(bidder);
//			lot.update(incomingBid);
//			queueEvent(new MessageEvent(ML.EVENT_MESSAGE_EVENT, this, msg));
//			
//			//Make the announcement
//			//This tends to throw a concurrent modification exception when too much 
//			//is going on (hence the cloning)
//			Vector<URLDescriptor> members = 
//				(Vector<URLDescriptor>)getMembers(incomingBid.getAuctionCD()).clone();
//			if (members != null){
//				updateActionText("I hear $" + incomingBid.getPrice() + " from " + bidderName);
//				
//				for (URLDescriptor url : members){
////				for (URLDescriptor url : getMembers(incomingBid.getAuctionCD())){
//					//The auctioneer doesn't send a message to itself
//					if (!url.equals(getURL()) 
//							&& !url.equals(getLACURL())
//							&& !url.equals(incomingBid.getAuctionCD())){
//						sendMessage(ML.INFORM, I_HEAR, url,
//								ML.LANGUAGE, Lot.class.getName(),
//								ML.CONTENT, CASAUtil.serialize(incomingBid));
//					}
//				}
//			}
//
//			//Call for new bids
////			members = getMembers(incomingBid.getAuctionCD());
//			members = (Vector<URLDescriptor>)getMembers(incomingBid.getAuctionCD()).clone();
//			if (members != null){
//				//Raise the price of the product (but don't commit price change to lot yet)
//				incomingBid.setPrice(incomingBid.getPrice() + BID_INCREMENT);
//				updateActionText("Do I hear $" + incomingBid.getPrice() + "?");
//				for (URLDescriptor url : members){
////				for (URLDescriptor url : getMembers(incomingBid.getAuctionCD())){
//					//The auctioneer doesn't send a message to itself
//					if (!url.equals(getURL()) 
//							&& !url.equals(getLACURL())
//							&& !url.equals(incomingBid.getAuctionCD())){
//						sendMessage(ML.PROPOSE, CRY, url,
//								ML.LANGUAGE, Product.class.getName(),
//								ML.CONTENT, CASAUtil.serialize(incomingBid));
//					}
//				}
//			}
//		}	else if (!bidder.equals(lot.getCurrentBidder())){
//				super.handleEvent(new MessageEvent(ML.EVENT_MESSAGE_EVENT, this, msg));
//				return;
//		}
//		
//		//If a fresh bid has not been received, initiate goingGoingGone sequence
//		if (!lot.biddingIsLocked()){
//			if (bidder.equals(lot.getCurrentBidder())){
//				switch (lot.goingGoingGone){
//					case Lot.CALLING_FOR_BIDS:
//						lot.goingGoingGone = Lot.GOING_ONCE;
//						break;
//					case Lot.GOING_ONCE:
//						lot.goingGoingGone = Lot.GOING_TWICE;
//						updateActionText("Going once...");
//						break;
//					case Lot.GOING_TWICE:
//						lot.goingGoingGone = Lot.GONE;
//						updateActionText("Going twice...");
//						break;
//					case Lot.GONE:
//						lot.goingGoingGone = Lot.AUCTION_IS_OVER;
//						updateActionText("Gone!");
//						break;
//				}
//				
//				if (lot.goingGoingGone == Lot.AUCTION_IS_OVER)
//					super.handleEvent(new MessageEvent(ML.EVENT_MESSAGE_EVENT, this, msg));					
//				else
//					queueEvent(new MessageEvent(ML.EVENT_MESSAGE_EVENT, this, msg));
//				
//			} else {
//				lot.goingGoingGone = Lot.CALLING_FOR_BIDS;
//				super.handleEvent(new MessageEvent(ML.EVENT_MESSAGE_EVENT, this, msg));
//			}
//		} else {
//			lot.goingGoingGone = Lot.CALLING_FOR_BIDS;
//			lot.unlockBidding();
//		}
//		
//		out("EnglishAuctionAgent.consider_agree_cry");
//		return;
//	}
	
	/**
	 * Perform offer_to_sell (SERVER)
	 * 
	 * @param msg
	 * @return PerformDescriptor
	 */
	
	public PerformDescriptor perform_offer_to_sell(MLMessage msg) {
		PerformDescriptor ret = new PerformDescriptor();
		in("EnglishAuctionAgent.perform_offer_to_sell");
		
//		if (!reconsiderCurrentBid.hasFired()){
//			reconsiderCurrentBid.start();
//		}
		if (reconsiderCurrentBid == null){
			reconsiderCurrentBid = new RecurringTimeEvent(ML.EVENT, this, System.currentTimeMillis(), BID_DELAY){
				@Override
				public void fireEvent () {
					if (currentBidMessage != null){
						perform_offer_to_sell(currentBidMessage);
					}
				}
			};
			reconsiderCurrentBid.start();
		}
			
		//Who made the bid?
		URLDescriptor bidder;
		String bidderName; 
		try {
			bidder = msg.getSender();
			bidderName = msg.getParameter(ML.SENDER).split("/")[msg.getParameter(ML.SENDER).split("/").length-1].split("\\?")[0];
		} catch (URLDescriptorException e2) {
			e2.printStackTrace();
			updateActionText("Who the heck sent that bid?");
			return new PerformDescriptor(new Status(Status.UNKNOWN_ERROR, "Who the heck sent that bid?"));
		}
		
		//Get the bid
		Lot incomingBid;
		try {
			incomingBid = (Lot)CASAUtil.unserialize(msg.getParameter(ML.CONTENT), msg.getParameter(ML.LANGUAGE));
		} catch (ParseException e) {
			updateActionText("I could not understand the bid from " + bidderName);
			e.printStackTrace();
			return new PerformDescriptor(new Status(Status.UNKNOWN_ERROR, "I could not understand the bid from " + bidderName));
		}
		
		//Is the bid worth considering?
		Lot lot = getLots().get(incomingBid.getAuctionCD());
		if (incomingBid.getPrice() > lot.getPrice() || lot.getPreviousBidder()==null){
			lot.goingGoingGone = Lot.CALLING_FOR_BIDS;
			incomingBid.setCurrentBidder(bidder);
			lot.update(incomingBid);
			
			//Cancel the previous best bid
			if (currentBidMessage != null){
				MLMessage cancelMsg = MLMessage.constructReplyTo(currentBidMessage,getUniqueRequestID(),getURL());
				cancelMsg.setParameter(ML.PERFORMATIVE, ML.FAILURE);
				cancelMsg.setParameter(ML.ACT,  ML.DISCHARGE + "|" + ML.PERFORM + "|" + AuctionAgent.CRY);
				cancelMsg.setParameter(ML.LANGUAGE, String.class.getName());
				cancelMsg.setParameter(ML.CONTENT, "A higher bid has been received (first chance)");
				sendMessage(cancelMsg);
			}
			
			//queueEvent(new MessageEvent(ML.EVENT_MESSAGE_EVENT, this, msg));
			currentBidMessage = msg;
			
			//Make the announcement
			//This tends to throw a concurrent modification exception when too much 
			//is going on (hence the cloning)
			Vector<URLDescriptor> members = 
				(Vector<URLDescriptor>)getMembers(incomingBid.getAuctionCD()).clone();
			if (members != null){
				updateActionText("I hear $" + incomingBid.getPrice() + " from " + bidderName);
				
				for (URLDescriptor url : members){
					//The auctioneer doesn't send a message to itself
					if (!url.equals(getURL()) 
							&& !url.equals(getLACURL())
							&& !url.equals(incomingBid.getAuctionCD())){
						sendMessage(ML.INFORM, I_HEAR, url,
								ML.LANGUAGE, Lot.class.getName(),
								ML.CONTENT, CASAUtil.serialize(incomingBid));
					}
				}
			}

			//Call for new bids
			members = (Vector<URLDescriptor>)getMembers(incomingBid.getAuctionCD()).clone();
			if (members != null){
				//Raise the price of the product (but don't commit price change to lot yet)
				incomingBid.setPrice(incomingBid.getPrice() + BID_INCREMENT);
				updateActionText("Do I hear $" + incomingBid.getPrice() + "?");
				for (URLDescriptor url : members){
					//The auctioneer doesn't send a message to itself
					if (!url.equals(getURL()) 
							&& !url.equals(getLACURL())
							&& !url.equals(incomingBid.getAuctionCD())){
						sendMessage(ML.PROPOSE, CRY, url,
								ML.LANGUAGE, Product.class.getName(),
								ML.CONTENT, CASAUtil.serialize(incomingBid));
					}
				}
			}
			return new PerformDescriptor(new Status(DROP_ACTION));
		}	
//		else if (!bidder.equals(lot.getCurrentBidder())){
//				super.handleEvent(new MessageEvent(ML.EVENT_MESSAGE_EVENT, this, msg));
//				return;
//		}
		
		//If a fresh bid has not been received, initiate goingGoingGone sequence
		if (bidder.equals(lot.getCurrentBidder())){
			switch (lot.goingGoingGone){
				case Lot.CALLING_FOR_BIDS:
					lot.goingGoingGone = Lot.GOING_ONCE;
					break;
				case Lot.GOING_ONCE:
					lot.goingGoingGone = Lot.GOING_TWICE;
					updateActionText("Going once...");
					break;
				case Lot.GOING_TWICE:
					lot.goingGoingGone = Lot.GONE;
					updateActionText("Going twice...");
					break;
				case Lot.GONE:
					lot.goingGoingGone = Lot.AUCTION_IS_OVER;
					updateActionText("Gone!");
					break;
			}
			currentBidMessage = msg;
				
			if (lot.goingGoingGone != Lot.AUCTION_IS_OVER)
				return new PerformDescriptor(new Status(DROP_ACTION));
				
		} else {
			lot.goingGoingGone = Lot.CALLING_FOR_BIDS;
		}
		
		//These tests must be made, because there is no assurance that low bids will
		//be refused in time
		if (lot.goingGoingGone == Lot.AUCTION_IS_OVER
				&& lot.getCurrentBidder().equals(bidder)
				&& lot.getPrice().equals(incomingBid.getPrice())){
			
			currentBidMessage = null;
			reconsiderCurrentBid.cancel();
			reconsiderCurrentBid.delete();
			reconsiderCurrentBid = null;
			
			ret = super.perform_offer_to_sell(msg);
			if (ret.getStatusValue()==Status.SUCCESS){
				ret.put(ML.LANGUAGE, Lot.class.getName());
				ret.put(ML.CONTENT, CASAUtil.serialize(incomingBid));
				ret.setStatus(Status.SUCCESS, incomingBid.getName() + " for $" + incomingBid.getPrice());

				MLMessage proposeDischargeMsg = MLMessage.constructReplyTo(msg,getUniqueRequestID(),getURL());
				proposeDischargeMsg.setParameter(ML.PERFORMATIVE, ML.PROPOSE);
				proposeDischargeMsg.setParameter(ML.ACT, ML.DISCHARGE + "|" + ML.PERFORM + "|" + AuctionAgent.CRY);
				proposeDischargeMsg.setParameter(ML.LANGUAGE, Lot.class.getName());
				proposeDischargeMsg.setParameter(ML.CONTENT, CASAUtil.serialize(incomingBid));
				sendMessage(proposeDischargeMsg);
				
				//Remove product from inventory.  The super method doesn't do this
				//because the agent's inventory is not updated to reflect bid, and therefore,
				//the new price of the item previously on the block
				Inventory inventory = getInventory();
				Product tempProduct = new Product(lot.getName(), lot.getStartingBid());
				inventory.removeProduct(tempProduct);
			}
		} else {
			ret.setStatus(Status.UNKNOWN_ERROR, "A higher bid has been received (last chance)");
//			ret.put(ML.PERFORMATIVE, ML.CANCEL);
		}
		
		out("EnglishAuctionAgent.perform_offer_to_sell");
		return ret;
	}
	
//	public PerformDescriptor perform_offer_to_sell(MLMessage msg) {
//		PerformDescriptor ret = new PerformDescriptor();
//		in("EnglishAuctionAgent.perform_offer_to_sell");
//		
//		//Who made the bid
//		URLDescriptor bidder;
//		String bidderName;
//		try {
//			bidder = (URLDescriptor)msg.getSender();
//			bidderName = msg.getParameter(ML.SENDER).split("/")[msg.getParameter(ML.SENDER).split("/").length-1].split("\\?")[0];
//		} catch (URLDescriptorException e1) {
//			e1.printStackTrace();
//			return new PerformDescriptor(new Status(Status.UNKNOWN_ERROR, "Could not retrieve sender's URL"));
//		}
//		
//		//Get the bid
//		Lot bid;
//		try {
//			bid = (Lot)CASAUtil.unserialize(msg.getParameter(ML.CONTENT));
//		} catch (ParseException e) {
//			updateActionText("I could not understand the bid from " + bidderName);
//			e.printStackTrace();
//			return new PerformDescriptor(new Status(Status.UNKNOWN_ERROR, "I could not understand the bid from " + bidderName));
//		}
//
//	
//		//These tests must be made, because there is no assurance that low bids will
//		//be refused in time
//		Lot lot = getLots().get(bid.getAuctionCD());
//		if (lot.goingGoingGone == Lot.AUCTION_IS_OVER
//				&& lot.getCurrentBidder().equals(bidder)
//				&& lot.getPrice().equals(bid.getPrice())){
//			ret = super.perform_offer_to_sell(msg);
//			if (ret.getStatusValue()==0){
//				ret.put(ML.LANGUAGE, Lot.class.getName());
//				ret.put(ML.CONTENT, CASAUtil.serialize(bid));
//				ret.setStatus(Status.SUCCESS, bid.getName() + " for $" + bid.getPrice());
//
//				//Remove product from inventory.  The super method doesn't do this
//				//because the agent's inventory is not updated to reflect bid, and therefore,
//				//the new price of the item previously on the block
//				Inventory inventory = getInventory();
//				Product tempProduct = new Product(lot.getName(), lot.getStartingBid());
//				inventory.removeProduct(tempProduct);
//			}
//		} else {
//			ret.setStatus(Status.UNKNOWN_ERROR, "A higher bid has been received");
//		}
//		
//		out("EnglishAuctionAgent.perform_offer_to_sell");
//		return ret;
//	}
	
	/**
	 * Consider offer_to_sell (CLIENT)
	 * 
	 * @param msg
	 * @return PerformDescriptor
	 */
	public PerformDescriptor consider_offer_to_sell(MLMessage msg) {
		PerformDescriptor ret = new PerformDescriptor();
		in("EnglishAuctionAgent.consider_offer_to_sell");

		Lot lot;
		try {
			lot = (Lot)CASAUtil.unserialize(msg.getParameter(ML.CONTENT), msg.getParameter(ML.LANGUAGE));
		} catch (ParseException e) {
			updateActionText("Couldn't unserialize bid");
			ret.setStatus(new Status(-1, "Couldn't unserialize bid"));
			e.printStackTrace();
			return ret;
		}
		
		//This has to be set to false, otherwise bidders won't participate in the auction
		getDesiredProducts().getProduct(lot.getName()).setAcquiring(false);
		
		//Sometimes the CD lags in removing a member from its roster. If that's
		//the case, ignore the proposal
		if (!getJoinedCooperationDomains().contains(lot.getAuctionCD())){
			updateActionText("I don't want that, stop bugging me.");
			ret.setStatus(new Status(-1, "I don't want that, stop bugging me."));
			return ret;
		}
		
		if (getBids().get(lot.getAuctionCD()).isHighestBidder()){
			updateActionText("Woohoo! I'm the highest bidder");
			ret.put(ML.PERFORMATIVE, ML.REFUSE);
			ret.put(ML.LANGUAGE, String.class.getName());
			ret.put(ML.CONTENT, "I already have the highest bid. ");
			return ret;
		}

		ret = super.consider_offer_to_sell(msg);
		ret.put(ML.LANGUAGE, Lot.class.getName());
		ret.put(ML.CONTENT, CASAUtil.serialize(lot));
		
		if (ret.getStatusValue() != 0){
			doWithdrawCD(lot.getAuctionCD(), false);
			removeBid(lot);
			updateActionText("I'm outta here...");
		}
		
		out("EnglishAuctionAgent.consider_offer_to_sell");
		return ret;
	}

	/**
	 * Inherited from the abstract AuctionAgent class
	 */
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

		Vector<URLDescriptor> members = getMembers(lot.getAuctionCD());

		//TODO: do something about this... dsb
		if (members==null){
			//				updateActionText("Try again for " + lot.getName() + " for $" + lot.getPrice() + ". Nobody's here.");
			return;
		}

		//Start the bidding
		updateActionText("The bidding for " + lot.getName() + " starts at $" + lot.getPrice());

		for (URLDescriptor m : members){
			if (!m.equals(getURL())){
				MLMessage outMsg = getNewMessage(ML.PROPOSE, AuctionAgent.CRY, m);
				outMsg.setParameter (ML.LANGUAGE, Lot.class.getName());
				outMsg.setParameter (ML.CONTENT, CASAUtil.serialize(lot));
				sendMessage(outMsg);
			}
		}
	}	

}
