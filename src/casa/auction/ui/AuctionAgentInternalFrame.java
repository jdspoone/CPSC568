/**
 * 
 */
package casa.auction.ui;

import casa.TransientAgent;
import casa.auction.AuctionAgent;
import casa.ui.TransientAgentInternalFrame;

import java.awt.Container;

import javax.swing.JTabbedPane;

/**
 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 *
 */
public class AuctionAgentInternalFrame extends TransientAgentInternalFrame {

	static int DEFAULT_X = 240, DEFAULT_Y = 0;
	
	/**
	 * @param agent
	 * @param title
	 * @param aFrame
	 */
	public AuctionAgentInternalFrame(TransientAgent agent,
			String title, Container aFrame) {
		super(agent, title, aFrame);
		frame.setLocation(DEFAULT_X+=22, DEFAULT_Y+=22);		
	}


	
//	@Override
	protected JTabbedPane	makeTabPane() {
		JTabbedPane pane = super.makeTabPane();
	
    AuctionPanel auction = new AuctionPanel(agent);
		
    //TODO I think the update method is occasionally throwing exceptions
    ((AuctionAgent)agent).setAuctionPanel(auction);
//		auction.updateAuctionDetails();
    
		addTab("Auction", auction, true);
    return pane;
	}
	
}
