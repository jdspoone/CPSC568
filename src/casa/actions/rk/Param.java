package casa.actions.rk;

/**
 * A description of a single parameter, containing it's name, type, and default value;
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
public class Param {
	public Param(String name, Class<?> type, Object defaultValue) {
		this.name = name;
		this.type = type;
		defValue = defaultValue;
	}
	public Param(String name, Class<?> type) {
		this(name, type, null);
	}
	String name;
	Class<?> type;
	Object defValue;
}