package casa.socialcommitments;

import casa.ML;
import casa.PerformDescriptor;
import casa.Status;
import casa.TransientAgent;
import casa.conversation2.Conversation;
import casa.event.MessageEvent;
import casa.exceptions.IllegalOperationException;
import casa.interfaces.PolicyAgentInterface;
import casa.util.InstanceCounter;

import java.util.List;
import java.util.TreeMap;

import org.armedbear.lisp.Cons;
import org.armedbear.lisp.Environment;
import org.armedbear.lisp.JavaObject;
import org.armedbear.lisp.LispObject;
import org.armedbear.lisp.Symbol;

/**
 * <code>Action</code> is used to encapsulate the action portion of some commitment.
 * @author  Jason Heard
 * @version 0.9
 */
public class Action implements Comparable<Action> {

	protected String name = null;

	/**
	 * The social commitment that "owns" this Action (if there is one). This is usually set by the SocialCommitment during its instantiation.
	 */
	protected SocialCommitment ownerSC = null;
	
	protected Cons lispCode = null;

	protected Action (String name) {
		this.name = name == null ? "null" : name;
		InstanceCounter.add(this);
	}

	public Action (Cons cons) {
		this("lisp-action");
		lispCode = cons;
	}

	public PerformDescriptor perform (PolicyAgentInterface agent) {
		if (lispCode==null) return new PerformDescriptor(new Status(-1,"Action: No lisp code given"));
		TreeMap<String,LispObject> map = new TreeMap<String,LispObject>();
		map.put("EVENT",  new JavaObject(ownerSC.getEvent())); //new MessageEvent(ML.EVENT_MESSAGE_RECEIVED, agent, ownerSC.getMessage())));
		map.put("COMMITMENT", new JavaObject(ownerSC));
		String cid = ownerSC.getOwnerConversationID();
		if (cid!=null) {
			List<Conversation> convs = ((TransientAgent)agent).getConversation(cid);
			if (convs!=null && convs.size()>0) {
				// TODO not sure what to do about the case of more than one conversation.  rck.
				map.put("CONVERSATION", new JavaObject(convs.get(0)));
			}
		}
		Status stat = agent.abclEval(lispCode, map); 
		if (stat instanceof PerformDescriptor) 
			return (PerformDescriptor)stat;
		return new PerformDescriptor(stat);
	}

	/**
	 * @return
	 */
	public String getName () {
		return name;
	}

	public void setOwner (SocialCommitment owner) {
		ownerSC = owner;
	}

	public SocialCommitment getOwner () {
		return ownerSC;
	}

	/**
	 * equals does...
	 * 
	 * TODO Finish documenting the equals method.
	 * 
	 * @param action
	 * @return
	 */
	public boolean equals (Action action) {
		if (action == this) {
			return true;
		}

		return action.name.equals (this.name);
	}

	/**
	 * equals does...
	 * 
	 * TODO Finish documenting the equals method.
	 * 
	 * @param action
	 * @return
	 */
	@Override
	public boolean equals (Object object) {
		if (object == this) {
			return true;
		} else if (object instanceof Action) {
			return equals ((Action) object);
		}

		return false;
	}

	@Override
	public int hashCode () {
		return name.hashCode ();
	}

	/**
	 * Compares this action to another action. The comparison is is designed to
	 * give the actions an ordering. The name field of the two actions are
	 * compared, determining the ordering of the two actions:
	 * 
	 * @param action The <code>Action</code> to be compared.
	 * @return The value <code>0</code> if the argument is an action equal to
	 *         this action; a value less than <code>0</code> if the argument
	 *         is an action that comes after this action; and a value greater
	 *         than <code>0</code> if the argument is an action that comes
	 *         before this action.
	 */
	@Override
	public int compareTo (Action action) {
		assert action!=null;
		if (action == this) {
			return 0;
		}
		return this.name.compareTo (action.name);
	}

	/**
	 * Returns a pretty string version of this action. Currently, this is the
	 * name of the action, or {@code "*"} if this action is {@code null}.
	 * 
	 * @return The name of the action, or {@code "*"} if this action is
	 *         {@code null}.
	 */
	public String displayString () {
		StringBuffer display = new StringBuffer ();

		if (name == null) {
			display.append ("*");
		} else {
			display.append (name);
		}
		if (lispCode!=null)
			display.append('=').append(lispCode.writeToString());
		return display.toString ();
	}

	@Override
	public String toString () {
		return name+":"+lispCode.writeToString();
	}
}