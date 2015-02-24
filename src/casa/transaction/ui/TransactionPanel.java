/**
 * 
 */
package casa.transaction.ui;

import casa.Act;
import casa.ML;
import casa.TransientAgent;
import casa.agentCom.URLDescriptor;
import casa.event.MessageEventDescriptor;
import casa.event.MessageObserverEvent;
import casa.event.TimeEvent;
import casa.interfaces.TransientAgentInterface;
import casa.transaction.AbstractTransactionAgent;
import casa.transaction.Inventory;
import casa.transaction.Product;
import casa.transaction.TransactionAgent;
import casa.util.CASAUtil;
import casa.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;


/**
 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 *
 */
public class TransactionPanel extends javax.swing.JPanel {

	private static final long serialVersionUID = -869837877933179218L;
	private TransientAgentInterface agent;
	
	private boolean buyButtonPressed, sellButtonPressed = false;
	
	/**
	 * The products this agent is interested in buying or selling
	 */
	private Inventory productsOfInterest;
	
	public Inventory getProductsOfInterest(){
		return this.productsOfInterest;
	}
	
	/**
	 * SellerPanel constructor
	 * @param agent
	 */
	public TransactionPanel(TransientAgentInterface agent){
		super();
		this.agent = agent;
		initComponents();
		initEvents();
	}

	private void initEvents(){
	//Wait until the list of running agents has been received before asking for inventory/desires
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
				
				String action;
				if (buyButtonPressed)
					action = AbstractTransactionAgent.INVENTORY_LIST;
				else if (sellButtonPressed)
					action = AbstractTransactionAgent.WANTS_LIST;
				else
					return;
				
				if (((TransactionAgent)agent).getRunningAgents() != null){
					for (String s : ((TransactionAgent)agent).getRunningAgents().keySet()){
						
						//This agent doesn't send a request to itself or the LAC
						if (!((TransactionAgent)agent).getRunningAgents().get(s).equals(((TransientAgent)agent).getURL()) &&  
								!((TransactionAgent)agent).getRunningAgents().get(s).equals(((TransientAgent)agent).getLACURL())){
							
							((TransientAgent)agent).sendMessage(ML.REQUEST, action, 
									((TransactionAgent)agent).getRunningAgents().get(s));
						}
					}
				}
				
				TimeEvent analyzeMarketProfile = new TimeEvent(ML.EVENT_DEFERRED_EXECUTION, agent, System.currentTimeMillis()+AbstractTransactionAgent.TIME_OUT){
					@Override
					public void fireEvent () {
						URLDescriptor bestURL = null;
						for (Product product : productsOfInterest.getProducts()){
							if (buyButtonPressed){
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
											((AbstractTransactionAgent)agent).getBankRoll().getBalance() >= bestProduct.getPrice()){
										((AbstractTransactionAgent)agent).getInventory().update(bestProduct);
										((TransientAgent)agent).sendMessage(ML.REQUEST, AbstractTransactionAgent.OFFER_TO_BUY, bestURL,
												ML.LANGUAGE, Product.class.getName(),
												ML.CONTENT, CASAUtil.serialize(bestProduct));
									}
									//Does this agent have enough money?
									else if (((AbstractTransactionAgent)agent).getBankRoll().getBalance() < bestProduct.getPrice())
										updateActionText("I don't have enough money for " + product.getName());
									else
										updateActionText("I'm only willing to pay $" + 
												product.getPrice() + " for " + product.getName());
								}
							}
							else if (sellButtonPressed){
								bestURL = ((TransactionAgent)agent).getMarketProfile().getHighestAgent(product);
								if (bestURL==null)
									updateActionText("No one wants " + product.getName());
								else {
									Inventory bestInventory = ((TransactionAgent)agent).getMarketProfile().getAgentInventory(bestURL);
									Product bestProduct = bestInventory.getProduct(product.getName());

									String agentName = bestURL.toString().split("/")[bestURL.toString().split("/").length-1].split("\\?")[0];
									updateActionText(agentName + " will offer the most for " + product.getName() + "($" + bestProduct.getPrice() + ")");
									if (bestProduct.getPrice() >= product.getPrice()) {
										((AbstractTransactionAgent)agent).getInventory().update(bestProduct);
										((TransientAgent)agent).sendMessage(ML.PROPOSE, AbstractTransactionAgent.OFFER_TO_SELL, bestURL,
												ML.LANGUAGE, Product.class.getName(),
												ML.CONTENT, CASAUtil.serialize(bestProduct));
									}
									else
										updateActionText("I won't sell " + product.getName() + 
												" for less than $" + product.getPrice());
								}
							}
						}
						sellButtonPressed = false;
						buyButtonPressed = false;
					}
				};
				analyzeMarketProfile.start();
			}
		};
		getRunningAgents.start();
	}
	
	private class TransactionTableModel extends javax.swing.table.DefaultTableModel implements TableModelListener{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -8782127443164759957L;
		private Inventory inventory;
		
		public TransactionTableModel(Object[][] data, String[] header, Inventory inventory) {
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
	
	private void initComponents() {

		detailsJPanel = new javax.swing.JPanel();
    desiredProductJScrollPane = new javax.swing.JScrollPane();
    desiredProductsJTable = new javax.swing.JTable();
    inventoryJScrollPane = new javax.swing.JScrollPane();
    inventoryJTable = new javax.swing.JTable();
    collectablesJScrollPane = new javax.swing.JScrollPane();
    collectablesJTable = new javax.swing.JTable();
    bankJSpinner = new javax.swing.JSpinner();
    bankJLabel = new javax.swing.JLabel();
    buyJButton = new javax.swing.JButton();
    sellJButton = new javax.swing.JButton();
    actionJPanel = new javax.swing.JPanel();
    actionJScrollPane = new javax.swing.JScrollPane();
    actionJTextArea = new javax.swing.JTextArea();

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
    desiredProductJScrollPane.setViewportView(desiredProductsJTable);

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

    collectablesJTable.setModel(new javax.swing.table.DefaultTableModel(
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
    collectablesJScrollPane.setViewportView(collectablesJTable);

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

    sellJButton.setText("Sell");
    sellJButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            sellJButtonActionPerformed(evt);
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
                    .addComponent(collectablesJScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(bankJLabel)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(bankJSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(detailsJPanelLayout.createSequentialGroup()
                    .addGroup(detailsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(buyJButton)
                        .addComponent(desiredProductJScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(detailsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(inventoryJScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(sellJButton))))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    detailsJPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {buyJButton, sellJButton});

    detailsJPanelLayout.setVerticalGroup(
        detailsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(detailsJPanelLayout.createSequentialGroup()
            .addGroup(detailsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(collectablesJScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(detailsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bankJLabel)
                    .addComponent(bankJSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(detailsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(detailsJPanelLayout.createSequentialGroup()
                    .addComponent(inventoryJScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(sellJButton))
                .addGroup(detailsJPanelLayout.createSequentialGroup()
                    .addComponent(desiredProductJScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(buyJButton))))
    );

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
        .addComponent(actionJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE)
    );

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(actionJPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(detailsJPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap())
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(detailsJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(actionJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addContainerGap())
    );
	}
  
	private void bankJSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {
		((AbstractTransactionAgent)agent).getBankRoll().setBalance((Integer)bankJSpinner.getValue());
	}
	
	/**
	 * Find agents registered so that this agent may request their inventory lists
	 * @param evt
	 */
  private void buyJButtonActionPerformed(java.awt.event.ActionEvent evt) {
		int[] rows = desiredProductsJTable.getSelectedRows();
//		((AbstractTransactionAgent)agent).getBankRoll().setBalance((Integer)bankJSpinner.getValue());
		
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
   * Find agents registered so that this agent may request their desires lists
   * @param evt
   */
  private void sellJButtonActionPerformed(java.awt.event.ActionEvent evt) {
		int[] rows = inventoryJTable.getSelectedRows();
		
		if (rows.length > 0){
			sellButtonPressed = true;
			
			//Update this agent's productsOfInterest
			Inventory temp = new Inventory();
			for(int i = 0; i<rows.length; i++){
				//Get the details for the selected item
				String name = (String)inventoryJTable.getModel().getValueAt(rows[i], 0);
				Integer price = (Integer)inventoryJTable.getModel().getValueAt(rows[i], 1);
				temp.addProduct(new Product(name, price));
			}
			productsOfInterest = temp;
			
			((TransactionAgent)agent).resetMarketProfile();  	
			((TransientAgent)agent).sendMessage(ML.REQUEST, ML.GET_AGENTS_RUNNING, 
					((TransientAgent)agent).getLACURL());
		} else {
			updateActionText("You didn't select the product(s) you wish to sell.");
		}
  }
	
  /**
   * Allows the caller to set component details
   */
  public void updateTransactionDetails(){
   	Object[][] desires = ((AbstractTransactionAgent)agent).getDesiredProducts().getSellablesArray();
  	desiredProductsJTable.setModel(new TransactionTableModel(desires, new String[]{"Desired Products", "P. Value"}, ((AbstractTransactionAgent)agent).getDesiredProducts()));
  	desiredProductsJTable.getColumnModel().getColumn(1).setPreferredWidth(25);
  	
  	Object[][] sellables = ((AbstractTransactionAgent)agent).getInventory().getSellablesArray();
  	inventoryJTable.setModel(new TransactionTableModel(sellables, new String[]{"Inventory", "Price"}, ((AbstractTransactionAgent)agent).getInventory()));
  	inventoryJTable.getColumnModel().getColumn(1).setPreferredWidth(25);
  	
  	Object[][] collection = ((AbstractTransactionAgent)agent).getInventory().getCollectionArray();
  	collectablesJTable.setModel(new TransactionTableModel(collection, new String[]{"Collectable", "Value"}, ((AbstractTransactionAgent)agent).getInventory()));
  	collectablesJTable.getColumnModel().getColumn(1).setPreferredWidth(25);
  	
  	//Update bankroll
  	bankJSpinner.setValue(((AbstractTransactionAgent)agent).getBankRoll().getBalance());
  }
	
  /**
   * Add new info to the action scroll pane
   * @param s
   */
  public void updateActionText(String s){
  	actionJTextArea.append(s + "\n");
  	actionJTextArea.setCaretPosition(actionJTextArea.getText().length());
  }
  
  // Variables declaration - do not modify
  private javax.swing.JPanel actionJPanel;
  private javax.swing.JScrollPane actionJScrollPane;
  private javax.swing.JTextArea actionJTextArea;
  private javax.swing.JLabel bankJLabel;
  private javax.swing.JSpinner bankJSpinner;
  private javax.swing.JButton buyJButton;
  private javax.swing.JScrollPane collectablesJScrollPane;
  private javax.swing.JTable collectablesJTable;
  private javax.swing.JScrollPane desiredProductJScrollPane;
  private javax.swing.JTable desiredProductsJTable;
  private javax.swing.JPanel detailsJPanel;
  private javax.swing.JScrollPane inventoryJScrollPane;
  private javax.swing.JTable inventoryJTable;
  private javax.swing.JButton sellJButton;
  // End of variables declaration
	
}
