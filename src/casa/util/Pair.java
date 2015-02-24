package casa.util;

import org.armedbear.lisp.LispObject;

/**a class for associating pairs of objects
 *
 * @param <T>		//the first object's type
 * @param <S>		//the second object's type
 */
public class Pair<T, S> {
	/**
	 * the first object of the pair
	 */
	private T first;
	/**
	 * the second object of the pair
	 */
	private S second;
	/**a constructor for this class
	 * 
	 * @param newFirst		the first object (could be null)
	 * @param newSecond		the second object (could be null)
	 */
	public Pair (T newFirst, S newSecond) {
		super ();
		first = newFirst;
		second = newSecond;
	}
	/** a constructor that copy's another pair
	 * 
	 * @param x		the other pair to copy
	 */
	public Pair(Pair<T,S> x) {
		this(x.first, x.second);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object arg0) {
		if (!(arg0 instanceof Pair<?,?>))
			return false;
		if (   (first ==null ? ((Pair<?,?>)arg0).first ==null : first .equals(((Pair<?,?>)arg0).first))
				&& (second==null ? ((Pair<?,?>)arg0).second==null : second.equals(((Pair<?,?>)arg0).second)))
			return true;
		return false;
	}

	/**
	 * @return Returns the first.
	 */
	public T getFirst () {
		return first;
	}

	/**
	 * @param first The first to set.
	 */
	public void setFirst (T first) {
		this.first = first;
	}

	/**
	 * @return Returns the second.
	 */
	public S getSecond () {
		return second;
	}

	/**
	 * @param next The second to set.
	 */
	public void setSecond (S next) {
		this.second = next;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "("+(getFirst()==null?"null":getFirst().toString())+" "+(getSecond()==null?"null":getSecond().toString())+")";
	}

}
