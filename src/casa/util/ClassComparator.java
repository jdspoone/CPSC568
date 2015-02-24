package casa.util;

import java.util.Comparator;

public class ClassComparator implements Comparator<Class<?>> {
	/**
	 * Compares the two given classes, returning a negative integer, zero or a
	 * positive integer if the first class object is "less than", equal to, or
	 * "greater than" the second class object. Currently,
	 * {@link Class#toString()} is used to retrieve the string representation of
	 * each class object which are then compared with
	 * {@link String#compareTo(String)}.
	 * 
	 * @param firstClass The first class to compare.
	 * @param secondClass The second class to compare.
	 * @return A negative integer, zero or a positive integer if
	 *         {@code firtClass} is "less than", equal to, or "greater than"
	 *         {@code secondClass}.
	 */
	public int compare (Class<?> firstClass, Class<?> secondClass) {
		return firstClass.toString ().compareTo (secondClass.toString ());
	}
}