package casa.actions.rf3;
import java.util.Collection;
import java.util.Iterator;

public class ActionChoice_One<T extends Action> extends AbstractAngelicChoice<T> {

	public ActionChoice_One(Collection<T> theActions) {
		super( "Choice1", theActions );
	}

		@Override
		protected int getChoiceSize() {
			int    available = size();
			int    chosen    = available > 0 ? 1 : 0;
			// post-conditions
			if (available > 0) assert chosen == 1 : String.format("Choice1 (if available we must choose one): size:%d chosen:%d", available, chosen );
			else			   assert chosen == 0 : String.format("Choice1 (we can't choose when none available): size:%d chosen:%d", available, chosen );
			//
			return chosen;
		}

}
