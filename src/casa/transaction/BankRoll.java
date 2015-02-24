/**
 * 
 */
package casa.transaction;

/**
 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 *
 */
public class BankRoll {

	public BankRoll(){
		this.money = 0;
	}
	
	public BankRoll(Integer money){
		this.money = money;
	}
	
	/**
	 * The amount of money this BankRoll contains
	 */
	private Integer money;
	
	/**
	 * Set this agent's bank roll
	 * @param money
	 */
	public void setBalance(Integer money){
		this.money = money;
	}
	
	/**
	 * Retrieve this agent's bank roll
	 * @return money
	 */
	public Integer getBalance(){
		return this.money;
	}
	
	/**
	 * Add money to bank roll
	 * @param newMoney
	 */
	public void deposit(Integer newMoney){
		this.money += newMoney;
	}
	
	/**
	 * Subtract money from bank roll
	 * @param newMoney
	 */
	public void withdraw(Integer money){
		this.money -= money;
	}
	
}
