package casa.socialcommitments;

/**
 * <code>OldSocialCommitmentFilter</code> is a ...
 * TODO Add description to JavaDoc file header.
 *
 * @author Jason Heard
 * @version 0.9
 */
public class OldSocialCommitmentFilter extends SocialCommitmentFilter {
  private long ageLimit;
  
  public OldSocialCommitmentFilter (long newAgeLimit) {
    ageLimit = newAgeLimit;
  }
  
  /* (non-Javadoc)
   * @see casa.socialcommitments.SocialCommitmentFilter#keepSocialCommitment(casa.socialcommitments.SocialCommitment, long)
   */
  @Override
  public boolean keepSocialCommitment (SocialCommitment socialCommitment, long now) {
    if ((now - socialCommitment.getEndTime ()) >= ageLimit) {
      return false;
    }
    
    return true;
  }
}
