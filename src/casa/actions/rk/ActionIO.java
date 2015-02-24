package casa.actions.rk;

public abstract class ActionIO {
	public String toString(Action action) {
		if (action instanceof SimpleAction)
		  return toString((SimpleAction)action);
		if (action instanceof CompositeAction)
		  return toString((CompositeAction)action);
		return "; ActionIO.toString(Action): type "+getClass().getSimpleName()+" not known.\n";
	}
	public abstract String toString(CompositeAction action);
	public abstract String toString(SimpleAction action);
	public abstract String toInstanceString(CompositeAction action);
	public abstract String toInstanceString(SimpleAction action);
	public abstract Action fromString(String serialized);
	public abstract Action fromInstanceString(String serialized);
	public abstract String toString(Param p);
}