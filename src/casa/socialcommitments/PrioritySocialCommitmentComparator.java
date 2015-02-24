package casa.socialcommitments;

/**
 * <code>PrioritySocialCommitmentComparator</code> is a comparator that can be
 * used to compare social commitments. It sorts the commitments based on:
 * 
 * <ol>
 * <li>The social commitments' priorities (highest priority first).</li>
 * <li>The social commitments' fulfillment time.</li>
 * <li>The social commitments' actions.</li>
 * <li>The social commitments' creation time.</li>
 * <li>The social commitments' debtors.</li>
 * <li>The social commitments' creditors.</li>
 * </ol>
 * 
 * @author Jason Heard
 * @version 0.9
 */
public class PrioritySocialCommitmentComparator extends
    SocialCommitmentComparator {

  /**
   * Compares the two given social commitments. The comparison is is designed to
   * give the commitments an ordering. The fields of the two commitments are
   * compared in the following order, with the first difference determining the
   * ordering of the two commitments:
   * 
   * <ol>
   * <li>The social commitments are sorted by their priorities (with the
   * highest priority first).</li>
   * <li>Unfulfilled social commitments come before fulfilled commitments.
   * </li>
   * <li>Fulfilled social commitments are sorted by their fulfilment time.
   * </li>
   * <li>The social commitments are sorted by their actions.</li>
   * <li>The social commitments are sorted by their creation time.</li>
   * <li>The social commitments are sorted by their debtors.</li>
   * <li>The social commitments are sorted by their creditors.</li>
   * </ol>
   * 
   * If the two social commitments are equal in all of the above comparisons,
   * they are equal.
   * 
   * @param commitment1 The first <code>SocialCommitment</code> to be
   *          compared.
   * @param commitment2 The second <code>SocialCommitment</code> to be
   *          compared.
   * @return The value <code>0</code> if the two social commitments are equal;
   *         a value less than <code>0</code> if the first commitment comes
   *         before the second commitment; and a value greater than
   *         <code>0</code> if the second commitment comes before the first
   *         commitment.
   */
  @Override
  public int compare (SocialCommitment commitment1, SocialCommitment commitment2) {
    if (commitment1 == commitment2) {
      return 0;
    }

    if (commitment1.getPriority () != commitment2.getPriority ()) {
      return commitment1.getPriority () - commitment2.getPriority ();
    } else if (commitment1.getEndTime () != commitment2
        .getEndTime ()) {
      return (int) (commitment2.getEndTime () - commitment1
          .getEndTime ());
    } else if (commitment1.getCreatedTime () != commitment2.getCreatedTime ()) {
      return (int) (commitment2.getCreatedTime () - commitment1
          .getCreatedTime ());
    } else if (!commitment2.getDebtor ().equals (commitment1.getDebtor ())) {
      return commitment2.getDebtor ().compareTo (commitment1.getDebtor ());
    } else if (!commitment2.getCreditor ().equals (commitment1.getCreditor ())) {
      return commitment2.getCreditor ().compareTo (commitment1.getCreditor ());
    }
    return 0;
  }
}