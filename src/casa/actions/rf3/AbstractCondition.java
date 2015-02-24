package casa.actions.rf3;

public abstract class AbstractCondition implements Condition {
	@Override
	public int compareTo(Condition theCondition) {
		String mine   =              getClass().getName();
		String other  = theCondition.getClass().getName();
		return mine.compareTo( other ); 
	}
}
