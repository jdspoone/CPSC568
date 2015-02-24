package casa.util;

/**a class that enables pairs with comparable parameters
 *
 * @param <T>		the first generic comparable object type of the pair
 * @param <S>		the second generic comparable object type of the pair
 */
public class PairComparable <T extends Comparable<T>, S extends Comparable<S>> extends Pair<T,S> implements Comparable<PairComparable<T,S>> {
	/**
	 * the first object of this pair
	 */
	private T first;
	/**
	 * the second object of this pair
	 */
	private S second;

	/**the constructor requiring a parameter for both pairs (could be null)
	 * 
	 * @param newFirst		the first parameter
	 * @param newSecond		the second parameter
	 */
	public PairComparable (T newFirst, S newSecond) {
		super (newFirst, newSecond);
	}

	@Override
	public int compareTo(PairComparable<T, S> o) {
		int c = (first.compareTo(o.first));
		if (c==0) {
				return second.compareTo(o.second);
		}
		return c;
	}

}
