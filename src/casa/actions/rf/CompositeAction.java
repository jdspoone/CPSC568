package casa.actions.rf;

import java.util.Collection;
import java.util.Iterator;

public class CompositeAction<T extends Action> implements Action, Collection<T> {
	private Collection<T> actions;
	
	public CompositeAction(Collection<T> theActions) {
		actions = theActions;
	}
	@Override
	public void execute() {
		for (Action action : this) {
			assert action != null;
			action.execute();
		}
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
}
