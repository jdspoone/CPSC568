package casa.actions.rf3;


import casa.Status;
import casa.exceptions.IllegalOperationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public abstract class AbstractAction implements Action {
	/** The set of preconditions for this Action */
	private Collection<PreCondition>  pre;
	
	/** The set of postconditions for this Action */
	private Collection<PostCondition> post;
	
	/** The name of the action */
	private String                    name;
	
	/**
	 * Defers to {@link #AbstractAction(String)} where the parameter is "Action".
	 */
	public AbstractAction() {
		this( "Action" );
	}
	
	/**
	 * Initializes the internal data structures.
	 * @param theName The name of this Action.
	 */
	public AbstractAction(String theName) {
		name = theName;
		pre  = new HashSet<PreCondition>();
		post = new HashSet<PostCondition>();
	}
	
	//*********** PRECONDITIONS to satisfy the Action contract *****************
	@Override
	public final void addPreCondition(PreCondition theCondition) {
		pre.add( theCondition );
	}
	@Override
	public final void addPreConditions(Collection<PreCondition> theConditions) {
		pre.addAll( theConditions );
	}
	@Override
	public final void removePreCondition(PreCondition theCondition) {
		pre.remove( theCondition );
	}
	@Override
	public final Collection<PreCondition> getPreConditions() {
		return new ArrayList<PreCondition>( pre );
	}
	@Override
	public final void clearPreConditions() {
		pre.clear();
	}
	
	//*********** POSTCONDITIONS to satisfy the Action contract *****************
	@Override
	public final void addPostCondition(PostCondition theCondition) {
		post.add( theCondition );
	}
	@Override
	public final void addPostConditions(Collection<PostCondition> theConditions) {
		post.addAll( theConditions );
	}
	@Override
	public final void removePostCondition(PostCondition theCondition) {
		post.remove( theCondition );
	}
	@Override
	public final Collection<PostCondition> getPostConditions() {
		return new ArrayList<PostCondition>( post );
	}
	@Override
	public final void clearPostConditions() {
		post.clear();
	}

	//*********** OTHER METHODS to satisfy the Action contract *****************
	
	/**
	 * Compares based on:
	 * <ol>
	 * <li> the {@link String#compareTo(String)} of {@link #getName()}
	 * <li> the compareTo() of the sorted members of the preconditions
	 * <li> the compareTo() of the sorted members of the postconditions
	 * </ol> 
	 * @param Action the other action
	 */
	@Override
	public int compareTo(Action theAction) {
		String mine   =           getClass().getName();
		String other  = theAction.getClass().getName();
		int    result = mine.compareTo( other ); 
		if (result == 0) {
			Collection<PreCondition> thePre = theAction.getPreConditions();
			result = pre.size() - thePre.size();
			if (result == 0) {
				List<PreCondition> one = new ArrayList<PreCondition>( pre );
				List<PreCondition> two = new ArrayList<PreCondition>( thePre );
				Collections.sort( one );
				Collections.sort( two );
				for (int i = 0; result == 0 && i < one.size(); i++) {
					result = one.get(i).compareTo( two.get(i) );
				}
			}
			if (result == 0) {
				Collection<PostCondition> thePost = theAction.getPostConditions();
				result = post.size() - thePost.size();
				if (result == 0) {
					List<PostCondition> one = new ArrayList<PostCondition>( post );
					List<PostCondition> two = new ArrayList<PostCondition>( thePost );
					Collections.sort( one );
					Collections.sort( two );
					for (int i = 0; result == 0 && i < one.size(); i++) {
						result = one.get(i).compareTo( two.get(i) );
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * @return True iff {@link #compareTo(Action)} returns 0.
	 */
	@Override
	public final boolean equals(Object theAction) {
		if (theAction instanceof Action) {
			return compareTo( (Action)theAction ) == 0;
		}
		return false;
	}
	
	/**
	 * @return The name of this Action as per the constructors, {@link #AbstractAction()} and {@link #AbstractAction(String)}.
	 */
	@Override
	public final String getName() {
		return name;
	}
	
	/**
	 * @return True iff all of the preconditions are true.
	 */
	@Override 
	public boolean isEnabled() {
		for (PreCondition pre : getPreConditions()) {
			if (!pre.isEnabled()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * If {@link #isEnabled()} returns true, then calls {@link #run(Object...)} with the parameters of this method.
	 * Otherwise throws an exception.
	 * @param theParameters The parameters to pass to {@link #run(Object...)}.
	 * @return The {@link Status} returned by the call to {@link #run(Object...)}.
	 * @throws IllegalOperationException if {@link #isEnabled()} returns false.
	 */
	@Override
	public final Status execute(Object... theParameters) throws IllegalOperationException {
		if (isEnabled()) {
			return run( theParameters );
		}
		else {
			//TODO what to do? failing | waiting (can there be a runtime choice)?
			throw new IllegalOperationException("AbstractAction.execute() ("+getName()+"): execute() called when preconditions do not hold. ");
		}
	}
	
	/**
	 * This is the actual action of the action.  It should only be called by {@link #execute(Object...)}.
	 * @param theParameters Any parameters required by the specific override of this method.
	 * @return Any {@link Status} object, according the the specific override of this method.
	 * @throws IllegalOperationException 
	 */
	protected abstract Status run(Object... theParameters) throws IllegalOperationException;
}
