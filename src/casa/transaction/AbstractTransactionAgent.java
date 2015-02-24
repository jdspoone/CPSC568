/**
 * 
 */
package casa.transaction;

import casa.Agent;
import casa.ML;
import casa.MLMessage;
import casa.PerformDescriptor;
import casa.Status;
import casa.TransientAgent;
import casa.abcl.CasaLispOperator;
import casa.abcl.ParamsMap;
import casa.ui.AgentUI;
import casa.util.CASAUtil;

import java.text.ParseException;
import java.util.Hashtable;
import java.util.TreeMap;
import java.util.Vector;

import org.armedbear.lisp.Cons;
import org.armedbear.lisp.Environment;
import org.armedbear.lisp.Fixnum;
import org.armedbear.lisp.LispObject;
import org.armedbear.lisp.SimpleString;

/**
 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 *
 */
public abstract class AbstractTransactionAgent extends Agent {

	/**
	 * Conversation constants
	 */
	public final static String OFFER_TO_BUY = "offer_to_buy";
	public final static String OFFER_TO_SELL = "offer_to_sell";
	public final static String INVENTORY_LIST = "inventory_list";
	public final static String WANTS_LIST = "wants_list";
	
	public final static long TIME_OUT = 3000;
	

	/**
	 * @param params
	 * @param ui
	 * @throws Exception
	 */
	public AbstractTransactionAgent(ParamsMap params, AgentUI ui)
			throws Exception {
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
	 * Different types of transaction agents will require different types of
	 * user interfaces.  Update interfaces here.
	 */
	public abstract void updateUI();
	public abstract void updateActionText(String s);
	
	/**
	 * The money this agent has available
	 */
	private BankRoll bankRoll = new BankRoll();
	
	/**
	 * Set this agent's bank roll
	 * @param money
	 */
	public void setBankRoll(Integer money){
		this.bankRoll.setBalance(money);
	}

	public BankRoll getBankRoll(){
		return this.bankRoll;
	}
	
	/**
	 * Products this agent wishes to possess
	 */
	private Inventory desiredProducts = new Inventory();
	
	
	/**
	 * Retrieve the inventory 
	 * 
	 * @return inventory
	 */
	public Inventory getDesiredProducts(){
		return this.desiredProducts;
	}
	
	/**
	 * Products this agent physically possesses and may want to sell
	 */
	private Inventory inventory = new Inventory();
	
	
	/**
	 * Retrieve the inventory 
	 * 
	 * @return inventory
	 */
	public Inventory getInventory(){
		return this.inventory;
	}
	

	/////////////////////////////////////////////////////////////////////////
	// OFFER_TO_BUY
	/////////////////////////////////////////////////////////////////////////
	
	/**
	 * Verify offer to buy (CLIENT)
	 * 
	 * @param msg
	 * @return PerformDescriptor 
	 */
	public PerformDescriptor verify_offer_to_buy(MLMessage msg) {
		in("AbstractTransactionAgent.verify_offer_to_buy");
		
		PerformDescriptor ret = new PerformDescriptor();
		
		String sender = msg.getParameter(ML.SENDER).split("/")[msg.getParameter(ML.SENDER).split("/").length-1].split("\\?")[0];

		/**
		 * Did the seller agent agree to the proposal? 
		 */
		if(!isA(msg.getParameter(ML.PERFORMATIVE), ML.AGREE)){
//			((AbstractTransactionAgent)agent).updateActionText("That's too bad, " + sender);
			ret.setStatus(0, "Seller did not agree to the proposed sale.");
			return ret;
		}
		
		/**
		 * Retrieve the product originally requested
		 */
		Product product = null;
		try {
			product = (Product)CASAUtil.unserialize(msg.getParameter(ML.CONTENT), msg.getParameter(ML.LANGUAGE));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		if (product == null){
			updateActionText(
				"Something's wrong.  I don't think you understood what I'm looking for, " + sender);
			ret.setStatus(0, ML.NOT_UNDERSTOOD);
			return ret;
		}

		//All is well.
		updateActionText(
				"I'm really excited about " + product.getName() 
				+ " for $" + product.getPrice() + ", " + sender);
	
		out("AbstractTransactionAgent.verify_offer_to_buy");
		return null;
	}
	
	/**
	 * Release offer to buy (CLIENT)
	 * 
	 * @param msg
	 * @return PerformDescriptor
	 */
	public PerformDescriptor release_offer_to_buy(MLMessage msg) {
		in("AbstractTransactionAgent.release_offer_to_buy");
		
		PerformDescriptor ret = new PerformDescriptor();
//		Hashtable<String, Integer> desiredProducts = getDesiredProducts();
		Inventory desiredProducts = getDesiredProducts();
		
		String sender = msg.getParameter(ML.SENDER).split("/")[msg.getParameter(ML.SENDER).split("/").length-1].split("\\?")[0];
		
		Product product = null;
		try {
			product = (Product)CASAUtil.unserialize(msg.getParameter(ML.CONTENT), msg.getParameter(ML.LANGUAGE));
		} catch (ParseException e) {
			updateActionText(
					"Sorry, I don't understand, " + sender + ".\n");
			ret.setStatus(0, ML.FAILURE);
			e.printStackTrace();
			return ret;
		}
		
		/**
		 * Remove requested item from desiredProducts list and subtract 
		 * value from bankroll) 
		 */
		desiredProducts.removeProduct(product);
		bankRoll.withdraw(product.getPrice());
		
		/**
		 * Add item to inventory 
		 */
		product.setForSale(false);
		inventory.addProduct(product);
				
		updateActionText("Thank you, " + sender + ", for " + product.getName() + ".\n");
		
		updateUI();
				
		ret.put(ML.LANGUAGE, Product.class.getName());
		ret.put(ML.CONTENT, CASAUtil.serialize(product));

		out("AbstractTransactionAgent.release_offer_to_buy");
		return ret;
	}
	
	
	/**
	 * Conclude offer to buy (SERVER) 	
	 * 
	 * @param msg
	 * @return PerformDescriptor
	 */
	public PerformDescriptor conclude_offer_to_buy(MLMessage msg) {
		in("AbstractTransactionAgent.conclude_offer_to_buy");
		
		String sender = msg.getParameter(ML.SENDER).split("/")[msg.getParameter(ML.SENDER).split("/").length-1].split("\\?")[0];
		
		//If the transaction was successful, remove the product from the seller's inventory
		
		Product product = null;
		try {
			if (isA(msg.getParameter(ML.PERFORMATIVE), ML.AGREE)){
				product = (Product)CASAUtil.unserialize(msg.getParameter(ML.CONTENT), msg.getParameter(ML.LANGUAGE));

				bankRoll.deposit(product.getPrice());
				inventory.removeProduct(product);

				updateActionText("You're welcome, " + sender);
				updateUI();

				return null;
			}
			//Else, something went wrong... put the product back in inventory		
			else {
				inventory.addProduct(product);
				updateActionText(
						"What just happened there, " + sender + "?\n");	
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	
		out("AbstractTransactionAgent.conclude_offer_to_buy");
		return null;
	}
	
	/**
	 * Perform offer to buy (SERVER)
	 * 
	 * @param msg
	 * @return
	 */
	public PerformDescriptor perform_offer_to_buy(MLMessage msg) {
		in("AbstractTransactionAgent.perform_offer_to_buy");

		PerformDescriptor ret = new PerformDescriptor();
		
		String buyer = msg.getParameter(ML.RECEIVER).split("/")[msg.getParameter(ML.RECEIVER).split("/").length-1].split("\\?")[0];
	
		/**
		 * Retrieve the requested product
		 */
		Product product = null;
		try {
			product = (Product)CASAUtil.unserialize(msg.getParameter(ML.CONTENT), msg.getParameter(ML.LANGUAGE));
		} catch (ParseException e) {
			e.printStackTrace();
			updateActionText("Sorry, "+ buyer + ", I could not unserialize the requested product.");
			ret.setStatus(-1, "Sorry, "+ buyer + ", I could not unserialize the requested product.");
			return ret;
		}
		
		if (product == null){
			updateActionText(
				"Sorry, "+ buyer + ", what are you looking for?");
			ret.put(ML.PERFORMATIVE, ML.REFUSE);
			ret.setStatus(0, "Sorry, "+ buyer + ", what are you looking for?");
			return ret;
		}

		/**
		 * Make sure the product is still in stock
		 */
		if (!inventory.contains(product.getName())){
			updateActionText(
					"Sorry, " + buyer + ", I do not have " + product.getName() + " in stock.");
			ret.put(ML.PERFORMATIVE, ML.REFUSE);
			ret.setStatus(0, "Sorry, " + buyer + ", I do not have " + product.getName() + " in stock.");
			return ret;
		}

		/**
		 * Confirm the price
		 */
		if (inventory.getProduct(product.getName()).getPrice() > product.getPrice()){
			updateActionText("I sell " + product.getName() + 
					" for $" + inventory.getProduct(product.getName()).getPrice() + ", " + buyer);
			ret.put(ML.PERFORMATIVE, ML.REFUSE);
			ret.setStatus(-1, "I sell " + product.getName() + " for $" + inventory.getProduct(product.getName()).getPrice() + "," + buyer);
			return ret;
		}
		//The buyer offered more than the asking price.  Update inventory with 
		//increased value
		else if (inventory.getProduct(product.getName()).getPrice() < product.getPrice()){
			inventory.update(product);
		}

		updateActionText(
				"Alright, " + buyer + ".  One " + product.getName() + " for $" + product.getPrice() + " coming up!\n");
				
		ret.put(ML.LANGUAGE, Product.class.getName());
		ret.put(ML.CONTENT, CASAUtil.serialize(product));

//		//Else, something went wrong
//		updateActionText(
//				"Whoops! I do not have " + product.getName() + " after all, " + sender + ".\n");
//		ret.setStatus(0, ML.FAILURE);
	
		out("AbstractTransactionAgent.perform_offer_to_buy");

		return ret;
	}
	
	/////////////////////////////////////////////////////////////////////////
	// OFFER_TO_SELL
	/////////////////////////////////////////////////////////////////////////


	/**
	 * Consider offer_to_sell (CLIENT)
	 * 
	 * @param msg
	 * @return PerformDescriptor
	 */
	public PerformDescriptor consider_offer_to_sell(MLMessage msg) {
		in("AbstractTransactionAgent.consider_offer_to_sell");

		PerformDescriptor ret = new PerformDescriptor();
		
		String sender = msg.getParameter(ML.SENDER).split("/")[msg.getParameter(ML.SENDER).split("/").length-1].split("\\?")[0];
		
		//Get product descriptor
		Product product = null;
		try {
			product = (Product)CASAUtil.unserialize(msg.getParameter(ML.CONTENT), msg.getParameter(ML.LANGUAGE));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		//Did something go wrong with the message content?
		if (product == null){
			ret.setStatus(new Status(-1, "No product specified"));
			return ret;
		}

		updateActionText(
				sender + " is selling "
				+ product.getName() + " for $" + product.getPrice() + "?");

		
		//Get products the agent wants to purchase
		Inventory desiredProducts = getDesiredProducts();

		if (desiredProducts.getProducts().isEmpty()){
			updateActionText(
					"No thanks, " + sender + ", I don't need anything");
			ret.put(ML.PERFORMATIVE, ML.REJECT_PROPOSAL);
			ret.put(ML.LANGUAGE, String.class.getName());
			ret.put(ML.CONTENT, "No thanks, I don't need anything");
			ret.setStatus(new Status(-1, "Don't need it."));
			return ret;
		}
		
		//Does the agent want the product being offered?  
		if (!desiredProducts.contains(product.getName())){
			updateActionText(
					"No thanks, I don't want " + product.getName());
			ret.put(ML.PERFORMATIVE, ML.REJECT_PROPOSAL);
			ret.put(ML.LANGUAGE, String.class.getName());
			ret.put(ML.CONTENT, "No thanks, "+ sender + ", I don't want " + product.getName());
			ret.setStatus(new Status(-1, "Don't want it."));
			return ret;
		}

		//Is the agent currently purchasing a product by the same name?  
		if (desiredProducts.getProduct(product.getName()).isAcquiring()){
			updateActionText(
					"Thanks for the offer, " + sender + ", but I'm in the process of buying " + product.getName());
			ret.put(ML.PERFORMATIVE, ML.REJECT_PROPOSAL);
			ret.put(ML.LANGUAGE, String.class.getName());
			ret.put(ML.CONTENT, "Thanks for the offer, " + sender + ", but I'm in the process of buying " + product.getName());
			ret.setStatus(new Status(-1, "Thanks for the offer, " + sender + ", but I'm in the process of buying " + product.getName()));
			return ret;
		}
		
		//Is the price right?
		if (desiredProducts.getProduct(product.getName()).getPrice() < product.getPrice()){
			updateActionText(
					"That's too expensive for " + product.getName());
			ret.put(ML.PERFORMATIVE, ML.REJECT_PROPOSAL);
			ret.put(ML.LANGUAGE, String.class.getName());
			ret.put(ML.CONTENT, sender + ", that's too expensive for " + product.getName());
			ret.setStatus(new Status(-1, "Too expensive."));
			return ret;
		}

		//Does the buyer have enough money?
		if (bankRoll.getBalance() < product.getPrice()){
			updateActionText(
					"D'oh! I don't have enough money for " + product.getName() + ", " + sender);
			ret.put(ML.PERFORMATIVE, ML.REJECT_PROPOSAL);
			ret.put(ML.LANGUAGE, String.class.getName());
			ret.put(ML.CONTENT, "D'oh! I don't have enough money for " + product.getName());
			ret.setStatus(new Status(-1, "Not enough money."));
			return ret;
		} 
		
		desiredProducts.getProduct(product.getName()).setAcquiring(true);
		updateActionText(
				"Sure, " + sender + ", I'll buy " + product.getName() + " for $" + product.getPrice());
		ret.setStatus(new Status(0));
		
		out("AbstractTransactionAgent.consider_offer_to_sell");
		
		return ret;
	}
	
	/**
	 * Consider offer to sell (CLIENT)
	 * 
	 * @param msg
	 * @return
	 */
	public PerformDescriptor release_offer_to_sell(MLMessage msg) {
		in("AbstractTransactionAgent.release_offer_to_sell");
		
		PerformDescriptor ret = new PerformDescriptor();
		
		String sender = msg.getParameter(ML.SENDER).split("/")[msg.getParameter(ML.SENDER).split("/").length-1].split("\\?")[0];
		
		Product product = null;
		try {
			product = (Product)CASAUtil.unserialize(msg.getParameter(ML.CONTENT), msg.getParameter(ML.LANGUAGE));
			bankRoll.withdraw(product.getPrice());
			product.setForSale(false); //The agent bought it presumably because it wants to keep it
			product.setAcquiring(false);
			inventory.addProduct(product);
			getDesiredProducts().removeProduct(product);
			updateActionText(
					"Thank you for " + product.getName() + ", " + sender + ".\n");
		} catch (ParseException e) {
			updateActionText(
					"Sorry, I don't understand, " + sender + ".\n");
			ret.setStatus(0, ML.FAILURE);
			e.printStackTrace();
			return ret;
		}
		
		ret.setStatus(0, "Purchase received");
		ret.put(ML.LANGUAGE, Product.class.getName());
		ret.put(ML.CONTENT, CASAUtil.serialize(product));
		
		updateUI();
				
		out("AbstractTransactionAgent.release_offer_to_sell");
		return ret;
	}
	
	/**
	 * Perform offer to sell (SERVER)
	 * 
	 * @param msg
	 * @return PerformDescriptor	
	 */
	public PerformDescriptor perform_offer_to_sell(MLMessage msg) {
		in("AbstractTransactionAgent.perform_offer_to_sell");
		
		PerformDescriptor ret = new PerformDescriptor();
		
//		TreeMap<String, Integer> inventoryMap = inventory.getMap();
		Product product = null;
		
		String sender = msg.getParameter(ML.SENDER).split("/")[msg.getParameter(ML.SENDER).split("/").length-1].split("\\?")[0];
		
		/*
		 * Consider
		 */
		
		//Did the buyer agent agree to the proposal?
		if(!isA(msg.getParameter(ML.PERFORMATIVE), ML.AGREE)){
			updateActionText(
					"That's too bad, " + sender);
			//((TransactionAgent)agent).getSellerPanel().updateActionText("That's too bad\n");
			ret.put(ML.PERFORMATIVE, ML.FAILURE);
			ret.setStatus(0, "Buyer did not agree to the proposed sale.");
			return ret;
		}
		
		//Make sure the seller can access its inventory
		if (inventory == null){
			updateActionText(
					"Sorry, " + sender + ", I can't access my inventory");
			ret.put(ML.PERFORMATIVE, ML.FAILURE);
			ret.setStatus(0, "Sorry, " + sender + ", I can't access my inventory");
			return ret;
		}
		
		//Retrieve the product offered
		try {
			product = (Product)CASAUtil.unserialize(msg.getParameter(ML.CONTENT), msg.getParameter(ML.LANGUAGE));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		if (product == null){
			updateActionText(
				"Sorry, what are you looking for, " + sender + "?");
			ret.put(ML.PERFORMATIVE, ML.FAILURE);
			ret.setStatus(0, "Sorry, what are you looking for, " + sender + "?");
			return ret;
		}
		
		//Retrieve the product first offered
//		try {
//			product = (Product)CASAUtil.unserialize(msg.getParameter(ML.CONTENT));
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
		
		//Make sure the product is still in stock
//		if (!inventoryMap.keySet().contains(product.getName())){
		if (!inventory.contains(product.getName())){
			updateActionText(
					"Sorry, " + sender + ", I no longer have " + product.getName() + " in stock.");
			ret.put(ML.PERFORMATIVE, ML.FAILURE);
			ret.setStatus(0, "Sorry, " + sender + ", I no longer have " + product.getName() + " in stock.");
			return ret;
		}
		
		/**
		 * Hand over the product 
		 */
		for(Product p : inventory.getProducts()){
			if(p.getName().equals(product.getName()) 
					&& p.getPrice() <= product.getPrice()){
				
				//Remove so that the same product doesn't get sold twice
				inventory.removeProduct(product);
				updateUI();

				updateActionText(
						"Alright, " + sender + ", One " + product.getName() + " for $"
						+ product.getPrice() + " coming up!");
				
				ret.put(ML.LANGUAGE, Product.class.getName());
				ret.put(ML.CONTENT, CASAUtil.serialize(p));
				return ret;
			}
		}

		//Else, something went wrong
		updateActionText(
				"Whoops! I do not have " + product.getName() 
				+ " after all, " + sender + ".");
		ret.put(ML.PERFORMATIVE, ML.FAILURE);
		ret.setStatus(0, "Whoops! I do not have " + product.getName() 
				+ " after all, " + sender + ".");
	
		
		out("AbstractTransactionAgent.perform_offer_to_sell");
		return ret;
	}
	
	/**
	 * Conclude offer to sell (SERVER)
	 * 
	 * @param msg
	 * @return
	 */
	public PerformDescriptor conclude_offer_to_sell(MLMessage msg) {
		in("AbstractTransactionAgent.conclude_offer_to_sell");
		
		PerformDescriptor ret = new PerformDescriptor();
//		Vector<Product> inventory = ((AbstractTransactionAgent)agent).getInventory();
		

		String sender = msg.getParameter(ML.SENDER).split("/")[msg.getParameter(ML.SENDER).split("/").length-1].split("\\?")[0];
		
		Product product;
		try {
			product = (Product)CASAUtil.unserialize(msg.getParameter(ML.CONTENT), msg.getParameter(ML.LANGUAGE));

			inventory.removeProduct(product);
			bankRoll.deposit(product.getPrice());
//			addDesiredProduct(product.getName(), product.getPrice());

			updateActionText("You're welcome, " + sender + ".\n");
			
			ret.put(ML.LANGUAGE, Product.class.getName());
			ret.put(ML.CONTENT, CASAUtil.serialize(product));
			
		} catch (ParseException e) {
//			inventory.addProduct(product);
			updateActionText(
					"What just happened there, " + sender + "?\n");	
			e.printStackTrace();
			return new PerformDescriptor(new Status(-1, "What just happened there?"));
		}

		updateUI();
		
		out("AbstractTransactionAgent.conclude_offer_to_sell");
		return ret;
	}
	
	/////////////////////////////////////////////////////////////////////////
	// INVENTORY_LIST
	/////////////////////////////////////////////////////////////////////////
	
	/**
	 * Release inventory list (CLIENT)
	 * 
	 * @param msg
	 * @return PerformDescriptor
	 */
	public PerformDescriptor release_inventory_list(MLMessage msg) {
		in("AbstractTransactionAgent.release_inventory_list");
		
//		try {
//			saveReturnedData(TransactionAgent.INVENTORY_LIST, 
//					(Hashtable<String, URLDescriptor>)CASAUtil.unserialize(
//							msg.getParameter(ML.CONTENT)));
//			
//		} catch (ParseException e) {
//			e.printStackTrace();
//			return new PerformDescriptor(new Status(0,ML.NOT_UNDERSTOOD));
//		} 

		out("AbstractTransactionAgent.release_inventory_list");
		return null;
	}
	
	/**
	 * Perform inventory list (SERVER)
	 * @param msg
	 * @return
	 */
	public PerformDescriptor perform_inventory_list(MLMessage msg) {
		in("AbstractTransactionAgent.perform_inventory_list");
		PerformDescriptor ret = new PerformDescriptor();
		
		Inventory tempInventory = new Inventory();
		for (Product p : this.inventory.getProducts())
			if (p.isForSale())
				tempInventory.addProduct(p);
		
		ret.put(ML.LANGUAGE, Hashtable.class.getName());
		ret.put(ML.CONTENT, CASAUtil.serialize(tempInventory.getMap()));

		out("AbstractTransactionAgent.perform_inventory_list");
		return ret;
	}

	/////////////////////////////////////////////////////////////////////////
	// WANTS_LIST
	/////////////////////////////////////////////////////////////////////////

	public PerformDescriptor release_wants_list(MLMessage msg) {
		in("AbstractTransactionAgent.release_wants_list");
		
//		try {
//			saveReturnedData(TransactionAgent.WANTS_LIST, 
//					(Hashtable<String, Integer>)CASAUtil.unserialize(
//							msg.getParameter(ML.CONTENT)));
//			
//		} catch (ParseException e) {
//			e.printStackTrace();
//			return new PerformDescriptor(new Status(0,ML.NOT_UNDERSTOOD));
//		}

		out("AbstractTransactionAgent.release_wants_list");
		return null;
	}
	
	/**
	 * Perform wants list (SERVER)
	 * 
	 * @param msg
	 * @return PerformDescriptor
	 */
	public PerformDescriptor perform_wants_list(MLMessage msg) {
		in("AbstractTransactionAgent.perform_wants_list");
		PerformDescriptor ret = new PerformDescriptor();
		
		ret.put(ML.LANGUAGE, Hashtable.class.getName());
		ret.put(ML.CONTENT, CASAUtil.serialize(this.desiredProducts.getMap()));
		
		out("AbstractTransactionAgent.perform_wants_list");
		return ret;
	}
	
	
	
	/////////////////////////////////////////////////////////////////////////
	// LISP OPERATORS
	/////////////////////////////////////////////////////////////////////////
	
	/**
   * Lisp operator: initialize-desired-products<br>
   * 
   * Allows the user to set desired products and bank roll
   */
  @SuppressWarnings("unused")
  private static final CasaLispOperator INITIALIZE_DESIRED_PRODUCTS =
  	new CasaLispOperator("INITIALIZE-DESIRED-PRODUCTS", "\"!Initializes the products this agent desires and their perceived values.\""
  			+ "BANKROLL \"@java.lang.Integer\" \"!The maximum amount of money this agent is able to spend\" "
  			+ "PRODUCTS \"@org.armedbear.lisp.Cons\" \"!Products this agent may wish to purchase entered as individual a cons list of product/value list pairs.\" "
  			, AbstractTransactionAgent.class, new Object() { }.getClass().getEnclosingClass())
  {
  	@Override
  	public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
 
  		((AbstractTransactionAgent)agent).setBankRoll((Integer)params.getJavaObject("BANKROLL"));
  		
			for (Cons productCons = (Cons)params.getLispObject("PRODUCTS"); productCons!=null && productCons!=org.armedbear.lisp.Lisp.NIL; productCons=(productCons.cdr==org.armedbear.lisp.Lisp.NIL?null:(Cons)productCons.cdr)) {
				LispObject car = productCons.car();

				Vector<Product> desiredProducts = new Vector<Product>();
  		
				//Turn the product cons into ProductDescriptors
				if (car instanceof Cons){
					if (car.car() instanceof SimpleString && car.cdr().car() instanceof Fixnum){
					((AbstractTransactionAgent)agent).getDesiredProducts().addProduct(
							new Product(car.car().getStringValue(), car.cdr().car().intValue()));// getStringValue()));
					} else {
						return new Status(-1, "Could not initialize desired products in " 
								+ agent.getAgentName() 
								+ ".  The product list consists of bracketed product/value pairs.");
					}
				} else {
					return new Status(-1, "Could not initialize desired products in " 
							+ agent.getAgentName() 
							+ ".  The product list consists of bracketed product/value pairs.");
				}
			}
	
  		//Update user interface
  		((AbstractTransactionAgent)agent).updateUI();
  		
		  return new Status(0, agent.getAgentName() + " initialized with user-set values.");
  	}
  };
	
	/**
   * Lisp operator: initialize-inventory<br>
   * 
   * Allows the user to set the lots, reserves, openings, products, and start times 
   */
  @SuppressWarnings("unused")
  private static final CasaLispOperator INITIALIZE_INVENTORY =
  	new CasaLispOperator("INITIALIZE-INVENTORY", "\"!Initializes the desired product and its perceived value.\""
  			+ "PRODUCTS \"@org.armedbear.lisp.Cons\" \"!Products this agent may wish to purchase entered as individual a cons list of product/value list pairs.\" "
//  			+ "&REST ITEMS \"@org.armedbear.lisp.LispObject\" \"!Products this agent offers for sale entered as a cons list of product/value list pairs.\" "
  			, AbstractTransactionAgent.class, new Object() { }.getClass().getEnclosingClass())
  {
  	@Override
  	public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {

			for (Cons productCons = (Cons)params.getLispObject("PRODUCTS"); productCons!=null && productCons!=org.armedbear.lisp.Lisp.NIL; productCons=(productCons.cdr==org.armedbear.lisp.Lisp.NIL?null:(Cons)productCons.cdr)) {
				LispObject car = productCons.car();

				//Turn the product cons into ProductDescriptors
				if (car instanceof Cons){
					if (car.car() instanceof SimpleString && car.cdr().car() instanceof Fixnum){
					((AbstractTransactionAgent)agent).getInventory().addProduct(
							new Product(car.car().getStringValue(), car.cdr().car().intValue()));
					} else {
						return new Status(-1, "Could not initialize inventory in " 
								+ agent.getAgentName() 
								+ ".  The product list consists of bracketed product/value pairs.");
					}
				} else {
					return new Status(-1, "Could not initialize inventory in " 
							+ agent.getAgentName() 
							+ ".  The product list consists of bracketed product/value pairs.");
				}
			}
  		
  		//Update user interface
  		((AbstractTransactionAgent)agent).updateUI();
  		
		  return new Status(0, agent.getAgentName() + " initialized seller inventory.");
  	}
  };
  
}
