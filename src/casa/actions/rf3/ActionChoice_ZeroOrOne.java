package casa.actions.rf3;
import java.util.Collection;
import java.util.Iterator;

public class ActionChoice_ZeroOrOne<T extends Action> extends AbstractAngelicChoice<T> {

	public ActionChoice_ZeroOrOne(Collection<T> theActions) {
		super( "ChoiceOpt", theActions );
	}

	@Override
	protected int getChoiceSize() {
		int    available = size();
		int    chosen    = available > 0 ? RANDOM.nextInt( 2 ) : 0;
		// post-conditions
		assert available >= chosen                           : String.format("ChoiceOpt (we can't choose more than available): size:%d chosen:%d", available, chosen );
		if (available > 0) assert chosen == 0 || chosen == 1 : String.format("ChoiceOpt (if available we must choose zero or one): size:%d chosen:%d", available, chosen );
		else			   assert chosen == 0                : String.format("ChoiceOpt (we can't choose when none available): size:%d chosen:%d", available, chosen );
		//
		return chosen;
	}
}
