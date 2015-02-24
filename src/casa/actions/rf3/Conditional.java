package casa.actions.rf3;

import casa.Status;
import casa.exceptions.IllegalOperationException;

/**
 * An action similar to a <em>switch</em> statement.  It is constructed with a vector of {@link Condition}/{@link Action}
 * pairs.  When executed, these pairs are searched (in order) for the first one in which both the {@link Condition}
 * and the {@link Action}'s {@link Action#isEnabled()} method is satisfied, and executes that {@link Action}.
 * @author <a href="http://www.pcs.cnu.edu/~flores/">Roberto Flores</a>
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
public class Conditional extends AbstractAction {
	private Pair<Condition,Action>[] pairs;
	
	/**
	 * Defers to {@link #Conditional(String, Pair...)} with the first parameter "Conditional".
	 * @param thePairs The Condition/action pairs for each of the clauses.
	 */
	public Conditional(Pair<Condition,Action>... thePairs) {
		this( "Conditional", thePairs );
	}
	
	/**
	 * @see {@link AbstractAction}
	 * @param theName The name of this Action.
	 * @param thePairs The pairs as described in {@link AbstractAction}.
	 */
	public Conditional(String theName, Pair<Condition,Action>... thePairs) {
		super( theName );
		pairs = thePairs;
	}
	
	/**
	 * Finds the first pair in which the Condition is satisfied AND the preconditions of the Action 
	 * are satisfied, and calls {@link #execute(Object...)} on that Action with this method's parameters,
	 * returning the result.  If none of the pairs can be
	 * executed, a 0-status is returned. 
	 */
	@Override
	protected Status run(Object... theParameters) throws IllegalOperationException {
		for (Pair<Condition,Action> pair : pairs) {
			if ((pair.first==null || pair.first.isEnabled()) && pair.second.isEnabled()) {
				return pair.second.execute( theParameters );
			}
		}
		return new Status(Status.SUCCESS);
	}
	
	//TODO this class should override getPreconditions() and isEnabled() to properly reflect its semantics. -- rck
}
