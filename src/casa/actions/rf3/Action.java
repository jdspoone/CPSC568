package casa.actions.rf3;
import casa.Status;
import casa.StatusObject;
import casa.exceptions.IllegalOperationException;

import java.util.Collection;

public interface Action extends Comparable<Action> {
	
	//******general contract******
	/**
	 * Execute the action, but ONLY if the preconditions are satisfied.
	 * @param theParameters
	 * @return 0: success, +ive: warning, -ive: failure; and possibly explanation text. If the
	 * execution returns a more complex result this method may return a {@link StatusObject}
	 * containing that result.
	 * @throws IllegalOperationException 
	 * @see Status
	 */
	public Status  execute(Object... theParameters) throws IllegalOperationException; //TODO should this throw a IllegalOperationException if the precondition is not satisfied? -rck

	/**
	 * Used in {@link #compareTo(Action)} and {@link #equals(Object)}.
	 * @return the name of this action.  
	 */
	public String  getName();
	
	/**
	 * @return true iff the preconditions are satisfied.
	 */
	public boolean isEnabled();

	//******preconditions******
	/**
	 * @return the preconditions for this Action.
	 */
	public Collection<PreCondition> getPreConditions();
	
	/**
	 * Adds Conditions to this Action's set of preconditions.
	 * @param theConditions
	 */
	public void addPreConditions(Collection<PreCondition> theConditions);

	/**
	 * Adds the Condition to this Acitons's set of preconditions.
	 * @param theCondition
	 */
	public void addPreCondition(PreCondition theCondition);

	/**
	 * Removes the Condition from this action, if it in the set of preconditions.
	 * @param theCondition
	 */
	public void removePreCondition(PreCondition theCondition);
	
	/**
	 * Removes all preconditions from this action.
	 */
	public void clearPreConditions();

	//******postconditions******
	/**
	 * @return the postconditions for this Action.
	 */
	public Collection<PostCondition> getPostConditions();
	
	/**
	 * Adds Conditions to this Action's set of postconditions.
	 * @param theConditions
	 */
	public void addPostConditions(Collection<PostCondition> theConditions);

	/**
	 * Adds the Condition to this Acitons's set of postconditions.
	 * @param theCondition
	 */
	public void addPostCondition(PostCondition theCondition);

	/**
	 * Removes the Condition from this action, if it in the set of preconditions.
	 * @param theCondition
	 */
	public void removePostCondition(PostCondition theCondition);
	
	/**
	 * Removes all postconditions from this action.
	 */
	public void clearPostConditions();
}
