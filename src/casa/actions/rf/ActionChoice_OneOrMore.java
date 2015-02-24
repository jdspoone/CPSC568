package casa.actions.rf;
import java.util.Collection;
import java.util.Iterator;

public class ActionChoice_OneOrMore<T extends Action> extends AbstractActionChoice<T> {

	public ActionChoice_OneOrMore(Collection<T> theActions) {
		super( theActions );
	}
	@Override
	public Iterator<T> iterator() {
		return new AbstractChoiceIterator() {		
			@Override
			protected int getChoiceSize() {
				int    available = size();
				int    chosen    = available > 0 ? RANDOM.nextInt( available )+1 : 0;
				// post-conditions
				assert available >= chosen            : String.format("Choice+ (we can't choose more than available): size:%d chosen:%d", available, chosen );
				if (available > 0) assert chosen >  0 : String.format("Choice+ (if available we should choose one+): size:%d chosen:%d", available, chosen );
				else			   assert chosen == 0 : String.format("Choice+ (we can't choose when none available): size:%d chosen:%d", available, chosen );
				//
				return chosen;
			}
		};
	}
}
