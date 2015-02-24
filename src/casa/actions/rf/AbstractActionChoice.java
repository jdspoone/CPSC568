package casa.actions.rf;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;


public abstract class AbstractActionChoice<T extends Action> extends CompositeAction<T> {
	protected static final Random RANDOM = new Random();

	protected AbstractActionChoice(Collection<T> theActions) {
		super(theActions);
	}

	protected abstract class AbstractChoiceIterator implements Iterator<T> {
		private Iterator<T> values; 

		protected AbstractChoiceIterator() {
			values = getChoiceValues();
		}
		@Override
		public boolean hasNext() {
			return values.hasNext();
		}
		@Override
		public T next() {
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
		
		private Iterator<T> getChoiceValues() {
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
			Iterator<T> iterator = AbstractActionChoice.super.iterator();
			List<T>     result   = new ArrayList<T>();
			int         current  = -1;
			for (int next : indices) {
				T action = null;
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
