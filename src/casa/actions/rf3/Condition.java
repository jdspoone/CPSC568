package casa.actions.rf3;

public interface Condition extends Comparable<Condition> {
	static final Condition TRUE = new AbstractCondition() {
		@Override
		public boolean isEnabled() {
			return true;
		}		
	};
	boolean isEnabled();
}
