package casa.actions.rf2;

import java.util.Collection;
import java.util.Iterator;

public class CompositeAction implements Action, Collection<Action> {
	private Collection<Action> actions;
	
	public CompositeAction(Collection<Action> theActions) {
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
	public Iterator<Action> iterator() {
		return actions.iterator();
	}
	@Override
	public boolean add(Action theAction) {
		return actions.add(theAction);
	}
	@Override
	public boolean addAll(Collection<? extends Action> theActions) {
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
	public <Action> Action[] toArray(Action[] theObjects) {
		return actions.toArray(theObjects);
	}
}
