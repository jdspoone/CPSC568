/**
 * 
 */
package casa.transaction.ui;

import casa.TransientAgent;
import casa.auction.AuctionAgent;
import casa.auction.ui.AuctionPanel;
import casa.interfaces.TransientAgentInterface;
import casa.transaction.TransactionAgent;
import casa.ui.TransientAgentInternalFrame;

import java.awt.Container;
import javax.swing.JTabbedPane;

/**
 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 *
 */
public class TransactionAgentInternalFrame extends TransientAgentInternalFrame {

	static int DEFAULT_X = 240, DEFAULT_Y = 0;

	/**
	 * @param agent
	 * @param title
	 * @param aFrame
	 */
	public TransactionAgentInternalFrame(TransactionAgent agent,
			String title, Container aFrame) {
		super(agent, title, aFrame);
		frame.setLocation(DEFAULT_X+=22, DEFAULT_Y+=22);
	}

	
	@Override
	protected JTabbedPane makeTabPane() {
		JTabbedPane pane = super.makeTabPane();

    TransactionPanel transaction = new TransactionPanel(agent);

    ((TransactionAgent)agent).setTransactionPanel(transaction);
    
		addTab("Buy/Sell", transaction, true);
    return pane;
	}
	
}
