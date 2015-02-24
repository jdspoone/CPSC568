/**
 * 
 */
package casa.auction.dutch.ui;

import casa.TransientAgent;
import casa.interfaces.TransientAgentInterface;
import casa.transaction.TransactionAgent;
import casa.ui.TransientAgentInternalFrame;

import java.awt.Container;
import javax.swing.JTabbedPane;

/**
 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 *
 */
public class DutchAuctionAgentInternalFrame extends TransientAgentInternalFrame {

	/**
	 * @param agent
	 * @param title
	 * @param aFrame
	 */
	public DutchAuctionAgentInternalFrame(TransactionAgent agent,
			String title, Container aFrame) {
		super(agent, title, aFrame);
	}

	
//	@Override
//	protected void addPanels (Container contentPane) {
	
//		SellerPanel seller = new SellerPanel(agent);
//		BuyerPanel buyer = new BuyerPanel(agent);
		
//		//TODO I think the update methods are occasionally throwing exceptions
//		((TransactionAgent)agent).setSellerPanel(seller);
//		seller.updateSellerDetails();
//		((TransactionAgent)agent).setBuyerPanel(buyer);
//		buyer.updateBuyerDetails();
//		
////    contentPanel.add( "Inventory", ((TransactionAgent)agent).getSellerPanel());
//		tabPane.add("Inventory", seller);
////    contentPanel.add( "Desired Products", ((TransactionAgent)agent).getBuyerPanel());
//		tabPane.add("Desired Products", buyer);

//		EnglishAuctioneerWindow auctioneer = new EnglishAuctioneerWindow(agent);
//		
//    super.addPanels(contentPane);
//	}
	
}
