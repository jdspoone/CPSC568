package casa.actions.rf3;

import casa.Status;
import casa.exceptions.IllegalOperationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/** 
 * A composite action is a {@link Collection} of zero or more actions.  It is enabled
 * if all of it's contained actions are enabled. 
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 * @param <T> The parameter class must extend Action.
 */
public class CompositeAction<T extends Action> extends AbstractAction implements Collection<T> {
	private Collection<T> actions;
	
	public CompositeAction(String theName, T... theActions) {
		this( theName, new ArrayList<T>( Arrays.asList( theActions )) );
	}
	public CompositeAction(T... theActions) {
		this( new ArrayList<T>( Arrays.asList( theActions )) );
	}
	public CompositeAction(Collection<T> theActions) {
		this( "CompositeAction", theActions );
	}
	public CompositeAction(String theName, Collection<T> theActions) {
		super( theName );
		actions = theActions;
	}
	@Override
	public final boolean isEnabled() {
		for (Action action : this) {
			if (!action.isEnabled()) {
				return false;
			}
		}
		return true;
	}
	@Override
	public Status run(Object... theParameters) throws IllegalOperationException {
		for (Action action : this) {
			assert action != null;
			Status result = action.execute( theParameters );
			if (result.getStatusValue() != Status.SUCCESS) {
				return result;
			}
		}
		return new Status(Status.SUCCESS);
	}
	@Override
	public Iterator<T> iterator() {
		return actions.iterator();
	}
	@Override
	public boolean add(T theAction) {
		return actions.add(theAction);
	}
	@Override
	public boolean addAll(Collection<? extends T> theActions) {
		return actions.addAll(theActions);
	}
	@Override
	public void clear() {
		actions.clear();
	}
	@Override
	public boolean contains(Object theObject) {
		return actions.contains(theObject);
	}
	@Override
	public boolean containsAll(Collection<?> theObjects) {
		return actions.containsAll(theObjects);
	}
	@Override
	public boolean isEmpty() {
		return actions.isEmpty();
	}
	@Override
	public boolean remove(Object theObject) {
		return actions.remove(theObject);
	}
	@Override
	public boolean removeAll(Collection<?> theObjects) {
		return actions.removeAll(theObjects);
	}
	@Override
	public boolean retainAll(Collection<?> theObjects) {
		return actions.retainAll(theObjects);
	}
	@Override
	public int size() {
		int    size = actions.size();
		assert size > -1;
		return size;
	}
	@Override
	public Object[] toArray() {
		return actions.toArray();
	}
	@SuppressWarnings("hiding")
	@Override
	public <T> T[] toArray(T[] theObjects) {
		return actions.toArray(theObjects);
	}
	@Override
	public int compareTo(Action theAction) {
		int result = super.compareTo(theAction);
		if (result == 0) {
			if (theAction instanceof CompositeAction) {
				@SuppressWarnings("unchecked")
				CompositeAction<T> theComposite = (CompositeAction<T>) theAction;
				List<T>            one          = new ArrayList<T>(              actions );
				List<T>            two          = new ArrayList<T>( theComposite.actions );
				Collections.sort( one );
				Collections.sort( two );
				for (int i = 0; result == 0 && i < one.size(); i++) {
					result = one.get(i).compareTo( two.get(i) );
				}
			}
			else
				throw new IllegalArgumentException();
		}
		return result;
	}
}
