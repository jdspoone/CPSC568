/**
 * 
 */
package casa.transaction;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Vector;

/**
 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 *
 */
public class Inventory {
	
	Vector<Product> inventory = new Vector<Product>(); 
	
	public Inventory(){
	}
	
	public Vector<Product> getProducts(){
		return this.inventory;
	}
	
	public Object[][] getCollectionArray(){
		
		Vector<Integer> indices = new Vector<Integer>();
		for (int i=0; i<this.inventory.size(); i++)
			if (!this.inventory.get(i).isForSale())
				indices.add(i);
		
		Object[][] array = new Object[indices.size()][2];
		
		for (int i=0; i<indices.size(); i++){
			array[i][0] = this.inventory.get(indices.get(i)).getName();
			array[i][1] = this.inventory.get(indices.get(i)).getPrice();
		}
		return array;
	}
	
	public Object[][] getSellablesArray(){
		Vector<Integer> indices = new Vector<Integer>();
		for (int i=0; i<this.inventory.size(); i++)
			if (this.inventory.get(i).isForSale())
				indices.add(i);
		
		Object[][] array = new Object[indices.size()][2];
		
		for (int i=0; i<indices.size(); i++){
			array[i][0] = this.inventory.get(indices.get(i)).getName();
			array[i][1] = this.inventory.get(indices.get(i)).getPrice();
		}
		return array;
	}
	
	/**
	 * Retrieve inventory items that are for sale
	 * @return inventory
	 */
	public Inventory getSellables(){
		Inventory i = new Inventory();
		for (Product p : this.inventory){
			if (p.isForSale())
				i.addProduct(p);
		}
		return i;
	}

	/**
	 * Retrieve inventory items that are not for sale
	 * @return inventory
	 */
	public Inventory getCollectables(){
		Inventory i = new Inventory();
		for (Product p : this.inventory){
			if (!p.isForSale())
				i.addProduct(p);
		}
		return i;
	}

	
	public void setProducts(Vector<Product> products){
		this.inventory = products;
	}
	
	public void addProduct(Product product){
		this.inventory.add(product);
	}
	
	public void update(Product product, int index){
		this.inventory.get(index).setPrice(product.getPrice());
//		this.inventory.remove(index);
//		this.inventory.add(index, product);
	}

	/**
	 * Updates all instances of products that share the same name as the parameter passed
	 * @param product
	 */
	public void update(Product product){
		for (Product p : getProducts()){
			if (p.getName().equals(product.getName())){
				int index = inventory.indexOf(p);
				this.inventory.get(index).setPrice(product.getPrice());
			}
		}
	}
	
	/**
	 * Removes the first instance of this product (by name only) from the inventory Vector
	 * @param product
	 */
	public void removeProduct(Product product){
		for (Product p : this.inventory){
			if (p.getName().equals(product.getName())){// && p.getPrice().equals(product.getPrice())){
				this.inventory.remove(p);
				break;
			}
		}
	}
	
	/**
	 * Retrieves the first instance of this product from the inventory Vector.  This method
	 * requires an exact match.
	 * @param product
	 * @return product
	 */
	public Product getProduct(Product product){
		for (Product p : this.inventory){
			if (p.getName().equals(product.getName()) && p.getPrice().equals(product.getPrice())){
				return p;
			}
		}
		return null;
	}

	/**
	 * Retrieves the first instance of this product from the inventory Vector.  This method
	 * only requires a name match.
	 * @param productName
	 * @return product
	 */
	public Product getProduct(String productName){
		for (Product p : this.inventory){
			if (p.getName().equals(productName)){
				return p;
			}
		}
		return null;
	}
	
	/**
	 * Is the product contained in this inventory?
	 * @param productName
	 * @return boolean
	 */
	public boolean contains(String productName){
		for (Product p : this.inventory){
			if (p.getName().equals(productName)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Retrieves a tree map of all the items in this agent's inventory.  If 
	 * duplicate products exist, only the product with the lowest value is listed.
	 * @return map
	 */
	public synchronized TreeMap<String, Integer> getMap(){
		TreeMap<String, Integer> map = new TreeMap<String, Integer>();
		for (Product p : this.inventory){
			if (map.containsKey(p.getName())){
				if(map.get(p.getName()) > p.getPrice())
					map.put(p.getName(), p.getPrice());
			}
			else
				map.put(p.getName(), p.getPrice());
		}
		return map;
	}
	
}
