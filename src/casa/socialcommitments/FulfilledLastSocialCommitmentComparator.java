package casa.socialcommitments;


/**
 * This is a simple comparator that sorts social commitments so that the
 * commitments that have ended are last and furthermore that commitments that
 * have ended because they are fulfilled are last among those that have ended.
 * 
 * @author Jason Heard
 * @version 0.9
 */
public class FulfilledLastSocialCommitmentComparator extends
    SocialCommitmentComparator {

  /*
   * (non-Javadoc)
   * 
   * @see casa.socialcommitments.SocialCommitmentComparator#compare(casa.socialcommitments.SocialCommitment,
   *      casa.socialcommitments.SocialCommitment)
   */
  @Override
  public int compare (SocialCommitment commitment1, SocialCommitment commitment2) {
  	if (commitment1.flagSet (SocialCommitmentStatusFlags.ENDED)) {
  		if (commitment2.flagSet (SocialCommitmentStatusFlags.ENDED)) {
  			if (!commitment1.flagSet (SocialCommitmentStatusFlags.NOTFULFILLED)) {
  				if (commitment2.flagSet (SocialCommitmentStatusFlags.NOTFULFILLED)) {
  					return 1;
  				}
  			} else {
  				if (! commitment2.flagSet (SocialCommitmentStatusFlags.NOTFULFILLED)) {
  					return -1;
  				}
  			}
  		} else {
  			return 1;
  		}
  	} else {
  		if (commitment2.flagSet (SocialCommitmentStatusFlags.ENDED)) {
  			return -1;
  		}
  	}

  	return (int) (commitment1.getSerialNumber () - commitment2.getSerialNumber ());
  }
}