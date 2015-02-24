package casa.socialcommitments;

/**
 * This is a simple comparator that sorts social commitments based on their
 * serial number, oldest first.
 * 
 * @author Jason Heard
 * @version 0.9
 */
public class SerialNumberSocialCommitmentComparator extends
    SocialCommitmentComparator {

  /*
   * (non-Javadoc)
   * 
   * @see casa.socialcommitments.SocialCommitmentComparator#compare(casa.socialcommitments.SocialCommitment,
   *      casa.socialcommitments.SocialCommitment)
   */
  public int compare (SocialCommitment commitment1, SocialCommitment commitment2) {
    return (int) (commitment1.getSerialNumber () - commitment2
        .getSerialNumber ());
  }
}