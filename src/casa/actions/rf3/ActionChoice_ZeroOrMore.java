package casa.actions.rf3;
import java.util.Collection;
import java.util.Iterator;

public class ActionChoice_ZeroOrMore<T extends Action> extends AbstractAngelicChoice<T> {

	public ActionChoice_ZeroOrMore(Collection<T> theActions) {
		super( "Choice*", theActions );
	}

	@Override
	protected int getChoiceSize() {
		int    available = size();
		int    chosen    = available > 0 ? RANDOM.nextInt( available+1 ) : 0;
		// post-conditions
		assert available >= chosen            : String.format("Choice* (we can't choose more than available): size:%d chosen:%d", available, chosen );
		if (available > 0) assert chosen >= 0 : String.format("Choice* (if available we should choose zero+): size:%d chosen:%d", available, chosen );
		else			   assert chosen == 0 : String.format("Choice* (we can't choose when none available): size:%d chosen:%d", available, chosen );
		//
		return chosen;
	}
}
