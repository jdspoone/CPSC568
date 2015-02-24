package casa.actions.rf3;

public class Pair<T,S> {
	public T first;
	public S second;

	public Pair (T newFirst, S newSecond) {
		first  = newFirst;
		second = newSecond;
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
