package casa.auction;
import casa.Act;
import casa.ML;
import casa.MLMessage;
import casa.PerformDescriptor;
import casa.Status;
import casa.TransientAgent;
import casa.abcl.CasaLispOperator;
import casa.abcl.ParamsMap;
import casa.agentCom.URLDescriptor;
import casa.auction.ui.AuctionAgentInternalFrame;
import casa.auction.ui.AuctionPanel;
import casa.event.MessageEventDescriptor;
import casa.event.MessageObserverEvent;
import casa.exceptions.URLDescriptorException;
import casa.transaction.AbstractTransactionAgent;
import casa.transaction.Inventory;
import casa.transaction.Product;
import casa.transaction.TransactionAgent;
import casa.ui.AgentUI;
import casa.util.CASAUtil;
import casa.util.Pair;

import java.awt.Container;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import org.armedbear.lisp.Environment;

/**
 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 *
 */
public abstract class AuctionAgent extends TransactionAgent {

	public final static String AUCTION_STARTING = "auction_starting";
	public final static String CRY = "cry";
	public final static String I_HEAR = "i_hear";
	public final static String MAKE_AUCTION_CD = "make_auction_cd";
	public final static String AUCTION_IS_OVER = "auction_is_over";
	
	public final static int AUCTION_CD_URL = 8700;
	public final static int AUCTION_DELAY = 5000;
	
	
	//Generally, depending on the order in which events are fired, the auctioneer will
	//join the CD after all the bidders.  Start the auction only after the auctioneer 
	//has joined.
	public MessageEventDescriptor auctioneerJoinedCDEvent = 
		new MessageEventDescriptor(
				this, ML.EVENT_MESSAGE_SENT, 
				new Pair[]{
						new Pair<String, Object>(ML.PERFORMATIVE, ML.AGREE),
						new Pair<String, Object>(
								ML.ACT, new Act(
										new ArrayList<String>(
												Arrays.asList(
														ML.PROPOSE, ML.DISCHARGE, ML.PERFORM, ML.GET_MEMBERS))))});
	
	MessageObserverEvent auctionAnnouncement = 
		new MessageObserverEvent(true, this, this.auctioneerJoinedCDEvent){
		@Override
		public void fireEvent () {
			MLMessage msg = getMessage();
			startAuction(msg);
		}
		
	};
	
	/**
	 * @param params
	 * @param ui
	 * @throws Exception
	 */
	public AuctionAgent(ParamsMap params, AgentUI ui) throws Exception {
		super(params, ui);
		auctionAnnouncement.start();
	}

  @Override
  protected casa.ui.TransientAgentInternalFrame makeDefaultInternalFrame(TransientAgent agent,
      String title, Container aFrame) {
  	return new AuctionAgentInternalFrame((AuctionAgent)agent, title, aFrame);
  }
	
	/* (non-Javadoc)
	 * @see casa.Agent#init()
	 */
	@Override
	public void initializeAfterRegistered(boolean registered) {
		super.initializeAfterRegistered(registered);
	}

	@Override
	public void updateActionText(String s) {
  	getAuctionPanel().updateActionText(s);
	}

	@Override
	public void updateUI() {
		if (getAuctionPanel() != null)
			getAuctionPanel().updateAuctionDetails();
	}

	/**
	 * Manage the items currently on the block 
	 */
	private Hashtable<URLDescriptor, Lot> lots = new Hashtable<URLDescriptor, Lot>();
	
	public Hashtable<URLDescriptor, Lot> getLots(){
		return this.lots;
	}
	
	public void addLot(Lot lot){
		this.lots.put(lot.getAuctionCD(), lot);
	}
	
	public void removeLot(Lot lot){
		this.lots.remove(lot.getAuctionCD());
	}
	
	/**
	 * Manage items currently being bid upon
	 */
	private Hashtable<URLDescriptor, Lot> bids = new Hashtable<URLDescriptor, Lot>();

	public Hashtable<URLDescriptor, Lot> getBids(){
		return this.bids;
	}
	
	public void addBid(Lot lot){
		this.bids.put(lot.getAuctionCD(), lot);
	}
	
	public void removeBid(Lot lot){
		this.bids.remove(lot.getAuctionCD());
	}
	
	/**
	 * The frame that displays the agent's inventory and desired products
	 */
	private AuctionPanel auctionPanel;
	
	/**
	 * Return auctionFrame
	 * @return auctionFrame
	 */
	public AuctionPanel getAuctionPanel(){
		return this.auctionPanel;
	}
	
	/**
	 * Set auctionFrame
	 */
	public void setAuctionPanel(AuctionPanel panel){
		this.auctionPanel = panel;
	}
	
	
	/**
	 * 
	 */
	public abstract void startAuction(MLMessage msg);
	
	//////////////////////////////////////////////////////////////////////////////
  // AUCTION_IS_OVER ///////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

	/**
	 * Perform offer_to_sell (SERVER)
	 * 
	 * @param msg
	 * @return PerformDescriptor
	 */
	public PerformDescriptor consider_auction_is_over(MLMessage msg) {
		PerformDescriptor ret = new PerformDescriptor();
		in("AuctionAgent.consider_auction_is_over");
		
		//Get the bid
		Lot lot;
		try {
			lot = (Lot)CASAUtil.unserialize(msg.getParameter(ML.CONTENT), msg.getParameter(ML.LANGUAGE));
		} catch (ParseException e) {
			updateActionText("I could not understand the auction_is_over message. Whatever could this mean?");
			e.printStackTrace();
			return new PerformDescriptor(new Status(-1, "I could not understand the auction_is_over message. Whatever could this mean?"));
		}

		doWithdrawCD(lot.getAuctionCD(), false);
		
		out("AuctionAgent.consider_auction_is_over");
		return ret;
	}
	
  //////////////////////////////////////////////////////////////////////////////
  // AUCTION_STARTING //////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  
	/**
	 * Does this agent want the item up for sale?
	 * 
	 * @param msg The incoming message
	 * @return The result of the processing; the status part will influence the return
	 */
	public PerformDescriptor consider_auction_starting(MLMessage msg) {
		in("AuctionAgent.consider_announce_auction");
		PerformDescriptor ret = new PerformDescriptor();
		
		//What's on the block?
		Lot lot;
		try {
			lot = (Lot)CASAUtil.unserialize(msg.getParameter(ML.CONTENT), msg.getParameter(ML.LANGUAGE));
		} catch (ParseException e) {
			updateActionText("Whoa! Something went wrong...");
			e.printStackTrace();
			return new PerformDescriptor(new Status(-1, "Could not unserialize product in the message's content field"));
		}

		//The auctioneer sends this message to itself so that it knows what
		//item is on the block and what CD the auction is to take place in. 
		URLDescriptor sender; 
		try {
			sender = msg.getSender();
		} catch (URLDescriptorException e) {
			updateActionText("Could not unserialize sender.");
			e.printStackTrace();
			return ret;
		}
		if (sender.equals(getURL())){
			ret.setStatus(doJoinCD(lot.getAuctionCD()));
			return ret;
		}
		
		
		updateActionText("\nOh boy! An auction!\n-------------------");
		
		//Does this agent want the item up for auction?
		Inventory desiredProducts = getDesiredProducts();

		if (desiredProducts.contains(lot.getName())){
			updateActionText(lot.getName() + "? I want it!");
			Status stat = doJoinCD(lot.getAuctionCD());
			if (stat.getStatusValue()==0){
				addBid(lot);
				updateActionText("Let's get this party started.");
			}
			else if (stat.getStatusValue()==2)
				updateActionText("Lots going on today.");
			else
				updateActionText("Hey! Let me in!");
		} else {
			updateActionText(lot.getName() + "? No thanks.");
		}
		
		out("AuctionAgent.consider_announce_auction");
		return ret;
	}
	
	
	//////////////////////////////////////////////////////////////////////////////
  // GET_MEMBERS ///////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
	
	/**
	 * If this agent is the only one left in the CD, no one wants the item on the block
	 * 
	 * @param msg The incoming message
	 * @return The result of the processing; the status part will influence the return
	 */
	public PerformDescriptor release_get_members (MLMessage msg) {
		PerformDescriptor ret = super.release_get_members(msg);
    in ("TransientAgent.release_get_members");
    
    URLDescriptor cd;
    try {
			cd = msg.getSender();
		} catch (URLDescriptorException e) {
			e.printStackTrace();
			return new PerformDescriptor(-1, "Could not unserialize CD URL");
		}
    
		//Is the agent alone?  Auction is over.
    Vector<URLDescriptor> members = getMembers(cd);
    if (members.size() == 1 
    		&& members.get(0).equals(getURL())
    		&& getLots().get(cd).auctionHasStarted()){
    	updateActionText("Hmmmm... maybe I should rethink my asking price.");
    	doWithdrawCD(cd, false);
    }
    
    out ("TransientAgent.release_get_members");
    return ret;
	}
	
  //////////////////////////////////////////////////////////////////////////////
  // MAKE_AUCTION_CD ///////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  
	/**
	 * Release when an CD in which to conduct an auction has been created
	 * 
	 * @param msg The incoming message
	 * @return The result of the processing; the status part will influence the return
	 */
	public PerformDescriptor release_make_auction_cd(MLMessage msg) {
		PerformDescriptor ret = super.release_execute(msg);
		in("AuctionAgent.release_make_auction_cd");

		out("AuctionAgent.release_make_auction_cd");
		return ret;
	}
	
  //////////////////////////////////////////////////////////////////////////////
  // OFFER_TO_SELL /////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  

	/**
	 * Conclude the offer to sell on the auctioneer's end
	 * 
	 * @param msg The incoming message
	 * @return The result of the processing; the status part will influence the return
	 */
	public PerformDescriptor conclude_offer_to_sell(MLMessage msg) {
		PerformDescriptor ret = super.conclude_offer_to_sell(msg);
		in("AuctionAgent.conclude_offer_to_sell");

		//Get the lot
		Lot lot;
		try {
			lot = (Lot)CASAUtil.unserialize(msg.getParameter(ML.CONTENT), msg.getParameter(ML.LANGUAGE));
		} catch (ParseException e) {
			e.printStackTrace();
			return new PerformDescriptor(new Status(-1, "Could not unserialize lot in the message's content field"));
		}
		doWithdrawCD(lot.getAuctionCD(), false);
		removeLot(lot);
		
		out("AuctionAgent.conclude_offer_to_sell");
		return ret;
	}
 
	/** 
	 * Release the offer to sell on the bidder's end
	 * 
	 * @param msg The incoming message
	 * @return The result of the processing; the status part will influence the return
	 */
	public PerformDescriptor release_offer_to_sell(MLMessage msg) {
		PerformDescriptor ret = super.release_offer_to_sell(msg);
		in("AuctionAgent.release_offer_to_sell");

		//Get the lot
		Lot lot;
		try {
			lot = (Lot)CASAUtil.unserialize(msg.getParameter(ML.CONTENT), msg.getParameter(ML.LANGUAGE));
		} catch (ParseException e) {
			e.printStackTrace();
			return new PerformDescriptor(new Status(-1, "Could not unserialize lot in the message's content field"));
		}
		doWithdrawCD(lot.getAuctionCD(), false);
		removeBid(lot);
//		lot.setForSale(false);
//		getInventory().addProduct(lot);
		
//		updateUI();
		out("AuctionAgent.release_offer_to_sell");
		return ret;
	}
	
	/**
   * Lisp operator: liquidate-inventory<br>
   * 
   * Put all sellable items up for auction 
   */
  @SuppressWarnings("unused")
  private static final CasaLispOperator LIQUIDATE_INVENTORY =
  	new CasaLispOperator("LIQUIDATE-INVENTORY", "\"!Put all sellable items up for auction.\""
  			, AbstractTransactionAgent.class, new Object() { }.getClass().getEnclosingClass())
  {
  	@Override
  	public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
  		
  		((AuctionAgent)agent).updateActionText("Liquidating inventory...");
  		
  		Inventory inventory = ((AuctionAgent)agent).getInventory().getSellables();
  		
//  		if (rows.length > 0){
//  			
//  			for (int i : rows){
  				//Get the details for the selected item
//  				String name = (String)inventoryJTable.getModel().getValueAt(i, 0);
//  				Integer price = (Integer)inventoryJTable.getModel().getValueAt(i, 1);
  		if (inventory.getProducts().size()==0){		
  			((AuctionAgent)agent).updateActionText("You have nothing to auction.");
  			return new Status(0, agent.getAgentName() + " no items to auction in inventory.");
  		}
  		
  		for (Product product : inventory.getProducts()){
  			//Create a CD through which to conduct the auction
  			int testPort = AuctionAgent.AUCTION_CD_URL;
  			for (URLDescriptor url : ((AuctionAgent)agent).getLots().keySet()){
  				if (url.getPort() > testPort)
  					testPort = url.getPort();
  			}
  			testPort++;
  				
  			int cdPort = CASAUtil.getOpenPort(testPort);
  			URLDescriptor cdURL;
				try {
					cdURL = URLDescriptor.make(cdPort);
				} catch (URLDescriptorException e1) {
					return new Status(-3, agent.println("error", "AuctionAgent.LIQIDATE_INVENTORY: ", e1));
				}
  			Lot lot = new Lot (product, cdURL);
  			((AuctionAgent)agent).addLot(lot);
  			
  			MLMessage msg = ((TransientAgent)agent).getNewMessage(ML.REQUEST, AuctionAgent.MAKE_AUCTION_CD, agent.getURL());
  			msg.setParameter (ML.LANGUAGE, String.class.getName());
  			msg.setParameter (ML.CONTENT, "(agent.new-agent \"casa.auction.AuctionCD\" \"AuctionCD_" + lot.getName() +"_\" " + cdPort + " :PROCESS \"CURRENT\" :NOWAIT :ACK false :LACPORT 9000 :MARKUP \"KQML\" :ONTOLOGYENGINE \"casa.ontology.v2.CASAOntology\" :ONTOLOGYFILE \"/ontology.lisp\" :PERSISTENT false :PRIORITY 0 :ROOT \"/casa/\" :SECURITY \"none\" :STRATEGY \"sc3\" :TRACE 10 :TRACEFILE  :TRACETAGS \"warning,msg,msgHandling,commitments,policies9,-lisp,-info\")" );
  			((TransientAgent)agent).sendMessage(msg);

  			//Give the open port a chance to be claimed
  			try {
  				Thread.sleep((long)AuctionAgent.AUCTION_DELAY);
  			} catch (InterruptedException e) {
  				e.printStackTrace();
  			}
  		}
  		return new Status(0, agent.getAgentName() + " inventory liquidation has begun.");
  	}
  };
	
}