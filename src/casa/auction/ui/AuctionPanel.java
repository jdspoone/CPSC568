/**
 * 
 */
package casa.auction.ui;

import casa.Act;
import casa.ML;
import casa.MLMessage;
import casa.TransientAgent;
import casa.agentCom.URLDescriptor;
import casa.auction.AuctionAgent;
import casa.auction.Lot;
import casa.event.MessageEventDescriptor;
import casa.event.MessageObserverEvent;
import casa.event.TimeEvent;
import casa.exceptions.URLDescriptorException;
import casa.interfaces.TransientAgentInterface;
import casa.transaction.AbstractTransactionAgent;
import casa.transaction.Inventory;
import casa.transaction.Product;
import casa.transaction.TransactionAgent;
import casa.util.CASAUtil;
import casa.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 *
 */
public class AuctionPanel extends javax.swing.JPanel {

	private static final long serialVersionUID = 9210688787186317010L;
	private TransientAgentInterface agent;

	private boolean buyButtonPressed = false;
	
	/**
	 * The products this agent is interested in buying or selling
	 */
	private Inventory productsOfInterest;
	
	public Inventory getProductsOfInterest(){
		return this.productsOfInterest;
	}
	
	/**
	 * Constructor
	 * @param agent
	 */
	public AuctionPanel(TransientAgentInterface agent) {
		super();
		this.agent = agent;
		initComponents();
		
		//Wait until the auction CD has been successfully created before proceeding
		MessageEventDescriptor makeAuctionCDEvent = 
			new MessageEventDescriptor(
					agent, ML.EVENT_MESSAGE_RECEIVED, 
					new Pair[]{
							new Pair<String, Object>(ML.PERFORMATIVE, ML.AGREE),
							new Pair<String, Object>(
									ML.ACT, new Act(
											new ArrayList<String>(
													Arrays.asList(
															ML.PROPOSE, ML.DISCHARGE, ML.PERFORM, AuctionAgent.MAKE_AUCTION_CD))))});

		MessageObserverEvent makeAuctionCD = 
			new MessageObserverEvent(true, agent, makeAuctionCDEvent){
			@Override
			public void fireEvent () {
				//Find agents registered so that they may be invited to the auction
				((TransientAgent)agent).sendMessage(ML.REQUEST, ML.GET_AGENTS_RUNNING, ((TransientAgent)agent).getLACURL());
			}
		};
		makeAuctionCD.start();

		//Wait until the list of running agents has been received before inviting agents to the auction
		MessageEventDescriptor getRunningAgentsEvent = 
			new MessageEventDescriptor(
					agent, ML.EVENT_MESSAGE_SENT, 
					new Pair[]{
							new Pair<String, Object>(ML.PERFORMATIVE, ML.AGREE),
							new Pair<String, Object>(
									ML.ACT, new Act(
											new ArrayList<String>(
													Arrays.asList(
															ML.PROPOSE, ML.DISCHARGE, ML.PERFORM, ML.GET_AGENTS_RUNNING))))});
		
		MessageObserverEvent getRunningAgents = 
			new MessageObserverEvent(true, agent, getRunningAgentsEvent){
			@Override
			public void fireEvent () {

				//Agent wants to make a regular purchase... no auction
				if (buyButtonPressed){
					if (((TransactionAgent)agent).getRunningAgents() != null){
						for (String s : ((TransactionAgent)agent).getRunningAgents().keySet()){
							
							//This agent doesn't send a request to itself or the LAC
							if (!((TransactionAgent)agent).getRunningAgents().get(s).equals(((TransientAgent)agent).getURL()) &&  
									!((TransactionAgent)agent).getRunningAgents().get(s).equals(((TransientAgent)agent).getLACURL())){
								
								((TransientAgent)agent).sendMessage(ML.REQUEST, AbstractTransactionAgent.INVENTORY_LIST, 
										((TransactionAgent)agent).getRunningAgents().get(s));
							}
						}
					}
					
					TimeEvent analyzeMarketProfile = new TimeEvent(ML.EVENT_DEFERRED_EXECUTION, agent, System.currentTimeMillis()+AbstractTransactionAgent.TIME_OUT){
						@Override
						public void fireEvent () {
							URLDescriptor bestURL = null;
							for (Product product : productsOfInterest.getProducts()){
								bestURL = ((TransactionAgent)agent).getMarketProfile().getLowestAgent(product);
								if (bestURL==null)
									updateActionText("No one has " + product.getName() + " in stock");
								else{
									Inventory bestInventory = ((TransactionAgent)agent).getMarketProfile().getAgentInventory(bestURL);
									Product bestProduct = bestInventory.getProduct(product.getName());
									String agentName = bestURL.toString().split("/")[bestURL.toString().split("/").length-1].split("\\?")[0];
									updateActionText(agentName + " has the lowest price for " + product.getName() + " ($" + bestProduct.getPrice() +")");
									//Is the price right?
									if (bestProduct.getPrice() <= product.getPrice() && 
											((AbstractTransactionAgent)agent).getBankRoll().getBalance() >= bestProduct.getPrice())
										((TransientAgent)agent).sendMessage(ML.REQUEST, AbstractTransactionAgent.OFFER_TO_BUY, bestURL,
												ML.LANGUAGE, Product.class.getName(),
												ML.CONTENT, CASAUtil.serialize(bestProduct));
									//Does this agent have enough money?
									else if (((AbstractTransactionAgent)agent).getBankRoll().getBalance() < bestProduct.getPrice())
										updateActionText("I don't have enough money for " + product.getName());
									else
										updateActionText("I'm only willing to pay $" + 
												product.getPrice() + " for " + product.getName());
								}
							}
							buyButtonPressed = false;
						}
					};
					analyzeMarketProfile.start();
				} 
				//Else there's an auction
				else {
					Lot lot = null;
				
					//Get the first available lot (one whose auction hasn't started) and start the auction
					Hashtable<URLDescriptor, Lot> lots = ((AuctionAgent)agent).getLots();
					for (URLDescriptor url : lots.keySet()){
						if (!lots.get(url).auctionHasStarted()){
							lot = lots.get(url);
							lot.startAuction();
							break;
						}
					}
					
					updateActionText("\nWho wants to buy " + lot.getName()// + " for $" + lot.getPrice() 
							+ "?\n-------------------------------------------------------------");
					
					//Announce the auction
					if (((TransactionAgent)agent).getRunningAgents() != null){
						for (String s : ((TransactionAgent)agent).getRunningAgents().keySet()){
							
							//The auctioneer sends this message to itself last
							if (!((TransactionAgent)agent).getRunningAgents().get(s).equals(((TransientAgent)agent).getURL()) &&  
									!((TransactionAgent)agent).getRunningAgents().get(s).equals(((TransientAgent)agent).getLACURL())){
								
								((TransientAgent)agent).sendMessage(
										ML.INFORM, AuctionAgent.AUCTION_STARTING, ((TransactionAgent)agent).getRunningAgents().get(s),
										ML.LANGUAGE, Lot.class.getName(),
										ML.CONTENT, CASAUtil.serialize(lot));
							}
							
						}
						//The auctioneer sends the auction announcement to itself.  Bidding for the
						//lot contained in the content field begins upon receipt.
						((TransientAgent)agent).sendMessage(
								ML.INFORM, AuctionAgent.AUCTION_STARTING, ((TransactionAgent)agent).getURL(),
								ML.LANGUAGE, Lot.class.getName(),
								ML.CONTENT, CASAUtil.serialize(lot));
					}
				}
			}
		};
		getRunningAgents.start();

//		//Generally, depending on the order in which events are fired, the auctioneer will
//		//join the CD after all the bidders.  Start the auction only after the auctioneer 
//		//has joined.
//		MessageEventDescriptor auctioneerJoinedCDEvent = 
//			new MessageEventDescriptor(
//					agent, ML.EVENT_MESSAGE_SENT, 
//					new Pair[]{
//							new Pair<String, Object>(ML.PERFORMATIVE, ML.AGREE),
//							new Pair<String, Object>(
//									ML.ACT, new Act(
//											new ArrayList<String>(
//													Arrays.asList(
//															ML.PROPOSE, ML.DISCHARGE, ML.PERFORM, ML.GET_MEMBERS))))});
//		
//		MessageObserverEvent auctionAnnouncement = 
//			new MessageObserverEvent(true, agent, auctioneerJoinedCDEvent){
//			@Override
//			public void fireEvent () {
//				MLMessage msg = getMessage();
//				
//				URLDescriptor cd; 
//				try {
//					cd = msg.getReceiver();
//				} catch (URLDescriptorException e) {
//					updateActionText("Could not unserialize receiver.");
//					e.printStackTrace();
//					return;
//				}
//				
//				//Get the lot 
//				Lot lot = ((AuctionAgent)agent).getLots().get(cd);
//				
//				//If true, this agent is not the auctioneer
//				if (lot == null)
//					return;
//
//
//				
//				Vector<URLDescriptor> members = 
//					((TransientAgent)agent).getMembers(lot.getAuctionCD());
//
//				//TODO: do something about this... dsb
//				if (members==null){
////					updateActionText("Try again for " + lot.getName() + " for $" + lot.getPrice() + ". Nobody's here.");
//					return;
//				}
//				
//				//Start the bidding
//				updateActionText("The bidding for " + lot.getName() + " starts at $" + lot.getPrice());
//				
//
//				
//				for (URLDescriptor m : members){
//					if (!m.equals(agent.getURL())){
//						MLMessage outMsg = ((TransientAgent)agent).getNewMessage(ML.PROPOSE, AuctionAgent.CRY, m);
//						outMsg.setParameter (ML.LANGUAGE, Lot.class.getName());
//						outMsg.setParameter (ML.CONTENT, CASAUtil.serialize(lot));
//						((TransientAgent)agent).sendMessage(outMsg);
//					}
//				}
//			}	
//		};
//		auctionAnnouncement.start();
	}
	
	private class AuctionTableModel extends javax.swing.table.DefaultTableModel implements TableModelListener{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -8782127443164759957L;
		private Inventory inventory;
		
		public AuctionTableModel(Object[][] data, String[] header, Inventory inventory) {
			super(data, header);
			addTableModelListener(this);
			this.inventory = inventory;
		}
	
		
		Class[] types = new Class [] {
				java.lang.String.class, java.lang.Integer.class
		};
		boolean[] canEdit = new boolean [] {
				false, true
		};

		public Class getColumnClass(int columnIndex) {
			return types [columnIndex];
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return canEdit [columnIndex];
		}

    public void tableChanged(TableModelEvent e) {
      int row = e.getFirstRow();
      int column = e.getColumn();
      TableModel model = (TableModel)e.getSource();
//      String columnName = model.getColumnName(column);
      String name = (String)model.getValueAt(row, 0);
      Integer price = (Integer)model.getValueAt(row, column);
//      ((AbstractTransactionAgent)agent).getInventory().update(new Product(name, price), row);
      inventory.update(new Product(name, price), row);
    }
	}
	
  /** 
   * This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. 
   */
	private void initComponents() {

		englishAuctionJPanel = new javax.swing.JPanel();
    actionJPanel = new javax.swing.JPanel();
    actionJScrollPane = new javax.swing.JScrollPane();
    actionJTextArea = new javax.swing.JTextArea();
    detailsJPanel = new javax.swing.JPanel();
    desiredProductsJScrollPane = new javax.swing.JScrollPane();
    desiredProductsJTable = new javax.swing.JTable();
    inventoryJScrollPane = new javax.swing.JScrollPane();
    inventoryJTable = new javax.swing.JTable();
    collectionJScrollPane = new javax.swing.JScrollPane();
    collectionJTable = new javax.swing.JTable();
    bankJSpinner = new javax.swing.JSpinner();
    bankJLabel = new javax.swing.JLabel();
    buyJButton = new javax.swing.JButton();
    auctionJButton = new javax.swing.JButton();

    actionJPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Action"));

    actionJTextArea.setColumns(20);
    actionJTextArea.setRows(5);
    actionJScrollPane.setViewportView(actionJTextArea);

    javax.swing.GroupLayout actionJPanelLayout = new javax.swing.GroupLayout(actionJPanel);
    actionJPanel.setLayout(actionJPanelLayout);
    actionJPanelLayout.setHorizontalGroup(
        actionJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(actionJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE)
    );
    actionJPanelLayout.setVerticalGroup(
        actionJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(actionJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
    );

    detailsJPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Details"));

    desiredProductsJTable.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {
            {null, null},
            {null, null},
            {null, null}
        },
        new String [] {
            "Desired Product", "P. Value"
        }
    ) {
        Class[] types = new Class [] {
            java.lang.String.class, java.lang.Integer.class
        };

        public Class getColumnClass(int columnIndex) {
            return types [columnIndex];
        }
    });
    desiredProductsJScrollPane.setViewportView(desiredProductsJTable);

    inventoryJTable.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {
            {null, null},
            {null, null},
            {null, null}
        },
        new String [] {
            "Inventory", "Price"
        }
    ) {
        Class[] types = new Class [] {
            java.lang.String.class, java.lang.Integer.class
        };
        boolean[] canEdit = new boolean [] {
            false, true
        };

        public Class getColumnClass(int columnIndex) {
            return types [columnIndex];
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return canEdit [columnIndex];
        }
    });
    inventoryJScrollPane.setViewportView(inventoryJTable);

    collectionJTable.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {
            {null, null},
            {null, null},
            {null, null}
        },
        new String [] {
            "Collectable", "Value"
        }
    ) {
        Class[] types = new Class [] {
            java.lang.String.class, java.lang.Integer.class
        };
        boolean[] canEdit = new boolean [] {
            false, true
        };

        public Class getColumnClass(int columnIndex) {
            return types [columnIndex];
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return canEdit [columnIndex];
        }
    });
    collectionJScrollPane.setViewportView(collectionJTable);

    bankJSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
        public void stateChanged(javax.swing.event.ChangeEvent evt) {
            bankJSpinnerStateChanged(evt);
        }
    });

    bankJLabel.setText("Bank roll: $");

    buyJButton.setText("Buy");
    buyJButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            buyJButtonActionPerformed(evt);
        }
    });

    auctionJButton.setText("Auction");
    auctionJButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            auctionJButtonActionPerformed(evt);
        }
    });

    javax.swing.GroupLayout detailsJPanelLayout = new javax.swing.GroupLayout(detailsJPanel);
    detailsJPanel.setLayout(detailsJPanelLayout);
    detailsJPanelLayout.setHorizontalGroup(
        detailsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(detailsJPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(detailsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(detailsJPanelLayout.createSequentialGroup()
                    .addComponent(collectionJScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(bankJLabel)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(bankJSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(detailsJPanelLayout.createSequentialGroup()
                    .addGroup(detailsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(buyJButton)
                        .addComponent(desiredProductsJScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(detailsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(inventoryJScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(auctionJButton))))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    detailsJPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {auctionJButton, buyJButton});

    detailsJPanelLayout.setVerticalGroup(
        detailsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(detailsJPanelLayout.createSequentialGroup()
            .addGroup(detailsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(collectionJScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(detailsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bankJLabel)
                    .addComponent(bankJSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(detailsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(detailsJPanelLayout.createSequentialGroup()
                    .addComponent(inventoryJScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(auctionJButton))
                .addGroup(detailsJPanelLayout.createSequentialGroup()
                    .addComponent(desiredProductsJScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(buyJButton))))
    );

    javax.swing.GroupLayout englishAuctionJPanelLayout = new javax.swing.GroupLayout(englishAuctionJPanel);
    englishAuctionJPanel.setLayout(englishAuctionJPanelLayout);
    englishAuctionJPanelLayout.setHorizontalGroup(
        englishAuctionJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(englishAuctionJPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(englishAuctionJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(actionJPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(detailsJPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap())
    );
    englishAuctionJPanelLayout.setVerticalGroup(
        englishAuctionJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(englishAuctionJPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(detailsJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(actionJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addContainerGap())
    );

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(englishAuctionJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(englishAuctionJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    );
	}

	private void bankJSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {
		((AbstractTransactionAgent)agent).getBankRoll().setBalance((Integer)bankJSpinner.getValue());
	}
	
  private void buyJButtonActionPerformed(java.awt.event.ActionEvent evt) {
		int[] rows = desiredProductsJTable.getSelectedRows();
	
		if (rows.length > 0){
			buyButtonPressed = true;
		
			//Update this agent's productsOfInterest
			Inventory temp = new Inventory();
			for(int i = 0; i<rows.length; i++){
				//Get the details for the selected item
				String name = (String)desiredProductsJTable.getModel().getValueAt(rows[i], 0);
				Integer price = (Integer)desiredProductsJTable.getModel().getValueAt(rows[i], 1);
				temp.addProduct(new Product(name, price));
			}
			productsOfInterest = temp;

			((TransactionAgent)agent).resetMarketProfile();
			((TransientAgent)agent).sendMessage(ML.REQUEST, ML.GET_AGENTS_RUNNING, 
					((TransientAgent)agent).getLACURL());
		} else {
			updateActionText("You didn't select the product(s) you wish to buy.");
		}
  }
	
	/**
	 * Offer the items selected in inventoryJTable up for auction.  This method
	 * depends on the getRunningAgents MessageObserverEvent defined in the
	 * constructor class.  
	 * @param evt
	 */
	private void auctionJButtonActionPerformed(java.awt.event.ActionEvent evt) {
		
		int[] rows = inventoryJTable.getSelectedRows();
//		((AbstractTransactionAgent)agent).getBankRoll().setBalance((Integer)bankJSpinner.getValue());
		
		if (rows.length > 0){
	
			//Go through the table in reverse to avoid array indexing errors
			for(int i = rows.length-1; i>=0; i--){
				//Get the details for the selected item
				String name = (String)inventoryJTable.getModel().getValueAt(rows[i], 0);
				Integer price = (Integer)inventoryJTable.getModel().getValueAt(rows[i], 1);
				
				//Create a CD through which to conduct the auction
				int testPort = AuctionAgent.AUCTION_CD_URL;
				for (URLDescriptor url : ((AuctionAgent)agent).getLots().keySet()){
					if (url.getPort() > testPort)
						testPort = url.getPort();
				}
				testPort++;
				
//				int cdPort = CASAUtil.getOpenPort(AuctionAgent.AUCTION_CD_URL+i);
				int cdPort = CASAUtil.getOpenPort(testPort);
				URLDescriptor cdURL;
				try {
					cdURL = URLDescriptor.make(cdPort);
				} catch (URLDescriptorException e1) {
					agent.println("error", "AuctionPanel.auctionJButtonActionPerformed()", e1);
					continue;
				}
				Lot lot = new Lot (new Product(name, price), cdURL);
				((AuctionAgent)agent).addLot(lot);
			
				MLMessage msg = ((TransientAgent)agent).getNewMessage(ML.REQUEST, AuctionAgent.MAKE_AUCTION_CD, agent.getURL());
				msg.setParameter (ML.LANGUAGE, String.class.getName());
				msg.setParameter (ML.CONTENT, "(agent.new-agent \"casa.auction.AuctionCD\" \"AuctionCD_" + lot.getName() + "_\" " + cdPort + " :PROCESS \"CURRENT\" :NOWAIT :ACK false :LACPORT 9000 :MARKUP \"KQML\" :ONTOLOGYENGINE \"casa.ontology.v2.CASAOntology\" :ONTOLOGYFILE \"/ontology.lisp\" :PERSISTENT false :PRIORITY 0 :ROOT \"/casa/\" :SECURITY \"none\" :STRATEGY \"sc3\" :TRACE 10 :TRACEFILE  :TRACETAGS \"warning,msg,msgHandling,commitments,policies9,-lisp,-info\")" );
				((TransientAgent)agent).sendMessage(msg);

				//Give the open port a chance to be claimed
				try {
					Thread.sleep((long)AuctionAgent.AUCTION_DELAY);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else {
			updateActionText("You haven't selected any items to auction."); 
		}
	}
	
  /**
   * Add new info to the action scroll pane
   * @param s
   */
  public void updateActionText(String s){
  	//This sometimes throws a java.lang.Error: "Interrupted attempt to aquire[sic] write lock"
  	try {
  		actionJTextArea.append(s + "\n");
  		actionJTextArea.setCaretPosition(actionJTextArea.getText().length());
  	} catch (Throwable thr) {
  		agent.println("warning", s);
  		thr.printStackTrace();
  	}
  }
	
  /**
   * Allows the caller to set component details
   */
  public void updateAuctionDetails(){

  	Object[][] desires = ((AbstractTransactionAgent)agent).getDesiredProducts().getSellablesArray();
  	desiredProductsJTable.setModel(new AuctionTableModel(desires, new String[]{"Desired Products", "P. Value"}, ((AbstractTransactionAgent)agent).getDesiredProducts()));
  	desiredProductsJTable.getColumnModel().getColumn(1).setPreferredWidth(25);
  	
  	Object[][] sellables = ((AbstractTransactionAgent)agent).getInventory().getSellablesArray();
  	inventoryJTable.setModel(new AuctionTableModel(sellables, new String[]{"Inventory", "Price"}, ((AbstractTransactionAgent)agent).getInventory()));
  	inventoryJTable.getColumnModel().getColumn(1).setPreferredWidth(25);
  	
  	Object[][] collection = ((AbstractTransactionAgent)agent).getInventory().getCollectionArray();
  	collectionJTable.setModel(new AuctionTableModel(collection, new String[]{"Collectable", "Value"}, ((AbstractTransactionAgent)agent).getInventory()));
  	collectionJTable.getColumnModel().getColumn(1).setPreferredWidth(25);
  	
  	//Update bankroll
  	bankJSpinner.setValue(((AbstractTransactionAgent)agent).getBankRoll().getBalance());
  	
  }
  
  // Variables declaration - do not modify
  private javax.swing.JPanel actionJPanel;
  private javax.swing.JScrollPane actionJScrollPane;
  private javax.swing.JTextArea actionJTextArea;
  private javax.swing.JButton auctionJButton;
  private javax.swing.JLabel bankJLabel;
  private javax.swing.JSpinner bankJSpinner;
  private javax.swing.JButton buyJButton;
  private javax.swing.JScrollPane collectionJScrollPane;
  private javax.swing.JTable collectionJTable;
  private javax.swing.JScrollPane desiredProductsJScrollPane;
  private javax.swing.JTable desiredProductsJTable;
  private javax.swing.JPanel detailsJPanel;
  private javax.swing.JPanel englishAuctionJPanel;
  private javax.swing.JScrollPane inventoryJScrollPane;
  private javax.swing.JTable inventoryJTable;
  // End of variables declaration
	
}
