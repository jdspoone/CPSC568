package casa.socialcommitments;

import casa.util.InstanceCounter;

/**
 * <code>SocialCommitmentFilter</code> is a ... TODO Add description to
 * JavaDoc file header.
 * 
 * @author Jason Heard
 * @version 0.9
 */
public abstract class SocialCommitmentFilter {
  public boolean keepSocialCommitment (SocialCommitment socialCommitment) {
    return keepSocialCommitment(socialCommitment, System.currentTimeMillis ());
  }
  
  public abstract boolean keepSocialCommitment (SocialCommitment socialCommitment,
      long now);
}