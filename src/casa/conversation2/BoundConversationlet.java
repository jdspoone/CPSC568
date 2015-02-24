/**
 * 
 */
package casa.conversation2;

import casa.policy.SCdescriptor;

/**
 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 *
 */
public class BoundConversationlet {

//	public enum Debtor {SENDER, RECEIVER};
	
	public final static String SENDER = "sender";
	public final static String RECEIVER = "receiver";
	
	private SCdescriptor desc;
	private Class<? extends ConversationInterface> conversationlet;
	private String debtor;
	
	
	/**
	 * Pair the description of a social commitment with a conversationlet.  This
	 * class is used to construct a CompositeConversation when the composite is 
	 * specified in the agent's init file.
	 */
	public BoundConversationlet(SCdescriptor desc, 
			Class<? extends ConversationInterface> conversationlet, String debtor) {
		this.desc = desc;
		this.conversationlet = conversationlet;
		this.debtor = debtor.toLowerCase();

	}

	public SCdescriptor getSCDescriptor(){
		return this.desc;
	}
	
	public Class<? extends ConversationInterface> getConversationlet(){
		return this.conversationlet;
	}
	
	public String getDebtor(){
		return this.debtor;
	}
}
