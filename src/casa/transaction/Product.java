/**
 * 
 */
package casa.transaction;

import casa.util.CASAUtil;

/**
 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 *
 */
public class Product {
	
	public Product(){
		this.name = "";
		this.price = 0;
		this.forSale = true;
	}
	
	public Product(String name, Integer price){
		this.name = name;
		this.price = price;
		this.forSale = true;
	}
	
	public Product(String name, Integer price, boolean isForSale){
		this.name = name;
		this.price = price;
		this.forSale = isForSale;
	}
	
	//Name
	protected String name;
	
	public String getName(){ 
		return name; 
	}
	
	public void setName(String name){ 
		this.name=name; 
	}
	
	// Price
	protected Integer price;
	
	public Integer getPrice(){
		return price;
	}
	
	public void setPrice(Integer price){
		this.price = price; 
	}

	// For Sale?
	private boolean forSale;
	
	public boolean isForSale(){
		return this.forSale;
	}
	
	public void setForSale(boolean sale){
		this.forSale = sale;
	}

	//Used to prevent purchasing more than one (identical) producat at a time 
	boolean acquiring = false;
	
	public boolean isAcquiring(){
		return this.acquiring;
	}
	
	public void setAcquiring(boolean a){
		this.acquiring = a;
	}
	
	/**
	 * Used for the purpose of serialization.  The str value may contain a string
	 * with spaces of any length.  The price of the product descriptor must appear
	 * at the end of str.
	 * @param str
	 */
	public void fromString(String str){
//		this = (Product)CASAUtil.unserialize(str);
		String[] strArray = str.trim().split(" ");
		for(int i = 0; i < strArray.length-1; i++)
			this.name = this.name.concat(strArray[i]) + " ";
		this.name = this.name.trim();
		this.price = Integer.valueOf(strArray[strArray.length-1]);
		//What to do with this? dsb
		this.forSale = true;
	}
	
	public String toString(){
		return this.name + " " + this.price;
//		return "("+Product.class.getName() + ") \"" + this.name + " " + this.price + "\"";
	}
	
}
