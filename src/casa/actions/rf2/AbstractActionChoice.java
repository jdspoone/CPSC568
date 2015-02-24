package casa.actions.rf2;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;


public abstract class AbstractActionChoice extends CompositeAction {
	protected static final Random RANDOM = new Random();

	protected AbstractActionChoice(Collection<Action> theActions) {
		super(theActions);
	}

	protected abstract class AbstractChoiceIterator implements Iterator<Action> {
		private Iterator<Action> values; 

		protected AbstractChoiceIterator() {
			values = getChoiceValues();
		}
		@Override
		public boolean hasNext() {
			return values.hasNext();
		}
		@Override
		public Action next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			return values.next();
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}		

		protected abstract int getChoiceSize();
		
		private Iterator<Action> getChoiceValues() {
			// how many values will get chosen?
			int size   = size();
			int chosen = getChoiceSize();
			// (randomly) get as many unique indices as values chosen (then sort them)
			List<Integer> indices = new ArrayList<Integer>();
			while (indices.size() < chosen) {
				int index = RANDOM.nextInt( size );
				if (!indices.contains( index )) {
					indices.add( index );
				}
			}
			Collections.sort( indices );
			// collect values at chosen indices
			Iterator<Action> iterator = AbstractActionChoice.super.iterator();
			List<Action>     result   = new ArrayList<Action>();
			int         current  = -1;
			for (int next : indices) {
				Action action = null;
				while (current < next) {
					current++;
					action = iterator.next();
				}
				result.add( action );
			}
			// return an iterator to collected values
			return result.iterator();
		}
	}
}
