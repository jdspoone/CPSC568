package casa.socialcommitments;

/**
 * <code>SocialCommitmentStoreObserver</code> is a ...
 * 
 * TODO Add description to JavaDoc file header.
 *
 * @author Jason Heard
 * @version 0.9
 */
public interface SocialCommitmentStoreObserver {

	void removeMember (SocialCommitment socialCommitment);

	void addMember (SocialCommitment socialCommitment);

}
