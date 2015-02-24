/**
 * 
 */
package casa.transaction;

import casa.agentCom.URLDescriptor;

import java.util.HashMap;

/**
 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 *
 */
public class MarketProfile {

	private HashMap<URLDescriptor,Inventory> profile; 
	
	public MarketProfile(){
		this.profile = new HashMap<URLDescriptor,Inventory>();
	}
	
	public HashMap<URLDescriptor,Inventory> getProfile(){
		return this.profile;
	}
	
	public void add(URLDescriptor url, Product product){
		if (this.profile.containsKey(url))
			this.profile.get(url).addProduct(product);
		else{
			this.profile.put(url, new Inventory());
			this.profile.get(url).addProduct(product);
		}
	}

	public void remove(URLDescriptor url){
		if (this.profile.containsKey(url))
			this.profile.remove(url);
	}
	
	/**
	 * Return the URL of the agent that will pay the highest price for the product
	 * @param product
	 * @return highestURL
	 */
	public URLDescriptor getHighestAgent(Product product){
		URLDescriptor highestURL = null;
		for (URLDescriptor url : this.profile.keySet()){
			if (this.profile.get(url).contains(product.getName())){
				if (highestURL==null)
					highestURL = url;
				else{
					if (this.profile.get(highestURL).getProduct(product.getName()).getPrice() <
							this.profile.get(url).getProduct(product.getName()).getPrice())
						highestURL = url;
				}
			}
		}
		return highestURL;
	}
	
	/**
	 * Return the URL of the agent that is offering the product for the lowest price
	 * @param product
	 * @return lowestURL
	 */
	public URLDescriptor getLowestAgent(Product product){
		URLDescriptor lowestURL = null;
		for (URLDescriptor url : this.profile.keySet()){
			if (this.profile.get(url).contains(product.getName())){
				if (lowestURL==null)
					lowestURL = url;
				else{
					if (this.profile.get(lowestURL).getProduct(product.getName()).getPrice() >
							this.profile.get(url).getProduct(product.getName()).getPrice())
						lowestURL = url;
				}
			}
		}
		return lowestURL;
	}

	/**
	 * Retrieve the inventory of the interesting agent
	 * @param url
	 * @return
	 */
	public Inventory getAgentInventory(URLDescriptor url){
		return this.profile.get(url);
	}
	
}
