/**
 * 
 */
package casa.transaction;

import casa.ML;
import casa.MLMessage;
import casa.PerformDescriptor;
import casa.Status;
import casa.TransientAgent;
import casa.abcl.ParamsMap;
import casa.agentCom.URLDescriptor;
import casa.exceptions.URLDescriptorException;
import casa.transaction.ui.TransactionAgentInternalFrame;
import casa.transaction.ui.TransactionPanel;
import casa.ui.AgentUI;
import casa.util.CASAUtil;

import java.awt.Container;
import java.text.ParseException;
import java.util.Hashtable;

/**
 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 *
 */
public class TransactionAgent extends AbstractTransactionAgent{

	/**
	 * Used to track inventory/desires and to determine the best price the market
	 * has to offer.
	 */
	private MarketProfile marketProfile = new MarketProfile();

	public MarketProfile getMarketProfile(){
		return this.marketProfile;
	}
	
	public void resetMarketProfile(){
		this.marketProfile = new MarketProfile();
	}

	/**
	 * All agents currently registered with the LAC
	 */
	protected Hashtable<String, URLDescriptor> runningAgents;
	
	public Hashtable<String, URLDescriptor> getRunningAgents(){
		return this.runningAgents;
	}
	
	private TransactionPanel transactionPanel;

	/**
	 * Return buyerPanel
	 * @return buyerPanel
	 */
	public TransactionPanel getTransactionPanel(){
		return this.transactionPanel;
	}

	/**	
	 * Set buyerPanel
	 */
	public void setTransactionPanel(TransactionPanel panel){
		this.transactionPanel = panel;
	}
	
	/**
	 * @param params
	 * @param ui
	 * @throws Exception
	 */
	public TransactionAgent(ParamsMap params, AgentUI ui) throws Exception {
		super(params, ui);
	}

  /**
   * Create the buyer's internal frame
   * @param agent 
   * @param title 
   * @param aFrame the owner frame in which this window is to be embedded
   * @return the frame
   */
  @Override
  protected casa.ui.TransientAgentInternalFrame makeDefaultInternalFrame(TransientAgent agent,
      String title, Container aFrame) {
  	return new TransactionAgentInternalFrame((TransactionAgent)agent, title, aFrame);
  }
	
  /**
   * Update whatever user interfaces this agent may employ.
   */
  public void updateUI(){
		if (getTransactionPanel() != null)
			getTransactionPanel().updateTransactionDetails();
  }
  
  public void updateActionText(String s){
  	getTransactionPanel().updateActionText(s);
  }
  
  //////////////////////////////////////////////////////////////////////////////
  // GET_AGENTS_RUNNING ////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  
	/**
	 * Used to find out what agents are present.  
	 * 
	 * @param msg The incoming message
	 * @return The result of the processing; the status part will influence the return
	 */
	public PerformDescriptor release_get_agents_running(MLMessage msg) {
//		PerformDescriptor ret = super.release_get_agents_running(msg);
		in("TransactionAgent.release_get_agents_running");
		PerformDescriptor ret = new PerformDescriptor();
		
		//Save the running agents
		try {
			this.runningAgents = 
				(Hashtable<String, URLDescriptor>)CASAUtil.unserialize(msg.getParameter(ML.CONTENT), msg.getParameter(ML.LANGUAGE));
			ret.setStatus(new Status(0));
			ret.put(ML.CONTENT, msg.getParameter(ML.CONTENT));
		} catch (ParseException e) {
			ret.setStatus(new Status(-1, e.toString()));
			e.printStackTrace();
			updateActionText("Could not retrieve running agents from the LAC");
			return ret;
		}
		
		out("TransactionAgent.release_get_agents_running");
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
  @Override
	public PerformDescriptor release_inventory_list(MLMessage msg) {
		in("TransactionAgent.release_inventory_list");
		
		try {
			Hashtable<String, Integer> wants = 
				(Hashtable<String, Integer>)CASAUtil.unserialize(msg.getParameter(ML.CONTENT), msg.getParameter(ML.LANGUAGE));
			URLDescriptor sender = msg.getSender(); 
			String senderName = msg.getParameter(ML.SENDER).split("/")[msg.getParameter(ML.SENDER).split("/").length-1].split("\\?")[0];
			if (wants != null)
				updateActionText(senderName + " has:\n-----------------");
				for(String s : wants.keySet()){
					updateActionText(s + " $" + wants.get(s));
					marketProfile.add(sender, new Product(s, wants.get(s)));
				}
			updateActionText("");
		} catch (ParseException e) {
			e.printStackTrace();
			return new PerformDescriptor(new Status(0,ML.NOT_UNDERSTOOD));
		} catch (URLDescriptorException e) {
			e.printStackTrace();
			return new PerformDescriptor(-1, "Could not unserialize sender.");
		}
		
		out("TransactionAgent.release_inventory_list");
		return null;
	}
  
	/////////////////////////////////////////////////////////////////////////
	// WANTS_LIST
	/////////////////////////////////////////////////////////////////////////

  @Override
	public PerformDescriptor release_wants_list(MLMessage msg) {
		in("TransactionAgent.release_wants_list");
		PerformDescriptor ret = new PerformDescriptor();
		try {
			Hashtable<String, Integer> wants = 
				(Hashtable<String, Integer>)CASAUtil.unserialize(msg.getParameter(ML.CONTENT), msg.getParameter(ML.LANGUAGE));
			URLDescriptor sender = msg.getSender(); 
			String senderName = msg.getParameter(ML.SENDER).split("/")[msg.getParameter(ML.SENDER).split("/").length-1].split("\\?")[0];
			if (wants != null)
				updateActionText(senderName + " wants:\n-----------------");
				for(String s : wants.keySet()){
					updateActionText(s + " $" + wants.get(s));
					marketProfile.add(sender, new Product(s, wants.get(s)));
				}
			updateActionText("");
			ret.setStatus(new Status(0, ML.AGREE));
		} catch (ParseException e) {
			e.printStackTrace();
			ret.setStatus(new Status(-1, ML.NOT_UNDERSTOOD));
		} catch (URLDescriptorException e) {
			e.printStackTrace();
			return new PerformDescriptor(-1, "Could not unserialize sender.");
		}

		out("TransactionAgent.release_wants_list");
		return ret;
	}
  
}
