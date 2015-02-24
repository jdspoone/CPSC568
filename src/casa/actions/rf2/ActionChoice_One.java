package casa.actions.rf2;
import java.util.Collection;
import java.util.Iterator;

public class ActionChoice_One extends AbstractActionChoice {

	public ActionChoice_One(Collection<Action> theActions) {
		super( theActions );
	}
	@Override
	public Iterator<Action> iterator() {
		return new AbstractChoiceIterator() {		
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
		};
	}
}
