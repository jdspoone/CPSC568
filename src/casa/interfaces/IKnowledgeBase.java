package casa.interfaces;

import casa.exceptions.ConflictingFactException;
import casa.exceptions.DuplicateFactException;
import casa.exceptions.UnparseableFactException;
import casa.util.Tristate;

@Deprecated
public interface IKnowledgeBase {

	public abstract void addBelief(String factString) throws DuplicateFactException, ConflictingFactException, UnparseableFactException;

	public abstract void addDesire(String factString) throws DuplicateFactException, ConflictingFactException, UnparseableFactException;

	public abstract void addIntention(String factString) throws DuplicateFactException, ConflictingFactException, UnparseableFactException;

	public abstract Tristate verifyBelief(String factString) throws UnparseableFactException;

	public abstract Tristate verifyDesire(String factString) throws UnparseableFactException;

	public abstract Tristate verifyIntention(String factString) throws UnparseableFactException;

}