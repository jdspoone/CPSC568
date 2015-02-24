/**
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. 
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  
 * The  Knowledge Science Group makes no representations about the suitability
 * of  this software for any purpose.  It is provided "as is" without express
 * or implied warranty.</p>
 * <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
package casa.actions.rk;

import casa.Status;
import casa.exceptions.IllegalOperationException;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class ActionChoice_One extends CompositeAction {

	/**
	 * @param actions
	 */
	public ActionChoice_One(Action... actions) {
		super(actions);
	}

	private static final Random random = new Random();

	@Override
	public Iterator<Action> iterator() {
		return new Choice_OneIterator();
	}
	
	private class Choice_OneIterator implements Iterator<Action> {		
		private boolean chosen;
		public Choice_OneIterator() {
			chosen = size() == 0;
		}
		@Override
		public boolean hasNext() {
			return !chosen;
		}
		@Override
		public Action next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			int              size     = size();
			int              index    = random.nextInt( size ); 
			Iterator<Action> iterator = actions.iterator();
			Action           result   = null;
			while (index-- > -1) {
				result = iterator.next();
			}
			chosen = true;
			return result;
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}		
	}

}
