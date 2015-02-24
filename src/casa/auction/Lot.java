/**
 * 
 */
package casa.auction;

import casa.agentCom.URLDescriptor;
import casa.transaction.Product;
import casa.util.CASAUtil;

import java.text.ParseException;

/**
 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 *
 */
public class Lot extends Product{

	public final static int CALLING_FOR_BIDS = 0;
	public final static int GOING_ONCE = 1;
	public final static int GOING_TWICE = 2;
	public final static int GONE = 3;
	public final static int AUCTION_IS_OVER = 4;
	public int goingGoingGone = CALLING_FOR_BIDS;
	
	public Lot(){
		this.name = null;
		this.price = 0;
		this.startingBid = 0;
		this.auctionCD = null;
		this.isLocked = false;
		this.auctionHasStarted = false;
	}
	
	public Lot(Product product, URLDescriptor url){
		this.name = product.getName();
		this.price = product.getPrice();
		this.startingBid = price;
		this.auctionCD = url;
		this.isLocked = false;
		this.auctionHasStarted = false;
	}
	
	public Lot(String name, Integer price, URLDescriptor url){
		this.name = name;
		this.price = price;
		this.startingBid = price;
		this.auctionCD = url;
		this.isLocked = false;
		this.auctionHasStarted = false;
	}

	public Lot(String name, Integer price, URLDescriptor url, Integer startingBid, 
			URLDescriptor currentBidder, URLDescriptor previousBidder){
		this.name = name;
		this.price = price;
		this.auctionCD = url;
		this.startingBid = startingBid;
		this.currentBidder = currentBidder;
		this.previousBidder = previousBidder;
		this.isLocked = false;
		//This constructor is used primarily for serialization.  If the lot has been
		//serialized or unserialized, then the auction has started and this value 
		//must be true.
//		this.auctionHasStarted = true;
	}
	
	public void update(Lot lot){
		setCurrentBidder(lot.getCurrentBidder());
		setPrice(lot.getPrice());
	}
	

	//auction CD
	private URLDescriptor auctionCD;
	
	public URLDescriptor getAuctionCD(){
		return this.auctionCD;
	}

	//isLocked
	private boolean isLocked;
	
	public boolean biddingIsLocked(){
		return this.isLocked;
	}
	
	public void setBiddingLock(boolean lock){
		this.isLocked = lock;
	}
	
	public void lockBidding(){
		this.isLocked = true;
	}
	
	public void unlockBidding(){
		this.isLocked = false;
	}
	
	/**
	 * auctionStarted
	 */
	private boolean auctionHasStarted;
	
	public boolean auctionHasStarted(){
		return this.auctionHasStarted;
	}
	
	public void startAuction(){
		this.auctionHasStarted = true;
	}
	
	/**
	 * For use by bidding agents with multiple bids on different products, potentially
	 */
	private boolean highestBidder = false;
	
	public boolean isHighestBidder(){
		return this.highestBidder;
	}
	
	public void setHighestBidder(boolean highest){
		this.highestBidder = highest;
	}
	
	/**
	 * Keep track of the best bid so far.
	 */
	private Integer startingBid = 0;
	private URLDescriptor currentBidder = null;
	private URLDescriptor previousBidder = null;
	
	
	public void setCurrentBidder(URLDescriptor bidder){
		lockBidding();
		this.previousBidder = this.previousBidder==null?bidder:this.currentBidder;
		this.currentBidder = bidder;
	}
	
	public Integer getStartingBid(){
		return this.startingBid;
	}
	
	public URLDescriptor getCurrentBidder(){
		return this.currentBidder;
	}

	public URLDescriptor getPreviousBidder(){
		return this.previousBidder;
	}
	
	/**
	 * Used for the purpose of serialization. 
	 * @param str
	 */
	public void fromString(String str){
		String[] strArray = str.trim().split(",");
		try {
			this.name = (String)CASAUtil.unserialize(strArray[0], null);
			this.price = (Integer)CASAUtil.unserialize(strArray[1], null);
			this.auctionCD = (URLDescriptor) CASAUtil.unserialize(strArray[2], URLDescriptor.class.getCanonicalName());
			this.startingBid = (Integer)CASAUtil.unserialize(strArray[3], null);
			this.currentBidder = (URLDescriptor) CASAUtil.unserialize(strArray[4], URLDescriptor.class.getCanonicalName());
			this.previousBidder = (URLDescriptor) CASAUtil.unserialize(strArray[5], URLDescriptor.class.getCanonicalName());
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public String toString(){
		return CASAUtil.serialize(this.name) + ","
			+ CASAUtil.serialize(this.price) + ","
			+ CASAUtil.serialize(this.auctionCD) + ","
			+ CASAUtil.serialize(this.startingBid) + ","
			+ CASAUtil.serialize(this.currentBidder) + ","
			+ CASAUtil.serialize(this.previousBidder);
	}
}

