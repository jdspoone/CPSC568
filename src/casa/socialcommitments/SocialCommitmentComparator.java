package casa.socialcommitments;

import casa.util.InstanceCounter;

import java.util.Comparator;

/**
 * <code>SocialCommitmentComparator</code> is an abstract class designed to
 * simplify the process of creating comparators for social commitments.
 * 
 * @author Jason Heard
 * @version 0.9
 */
public abstract class SocialCommitmentComparator implements Comparator<SocialCommitment> {
  /**
   * Compares the two given social commitments. The comparison is is designed to
   * give the commitments an ordering.
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
  public abstract int compare (SocialCommitment commitment1,
      SocialCommitment commitment2);

  /**
   * Determines whether this social commitment comparator is equal to some
   * object.
   * 
   * @param object The object to compare to this.
   * @return <code>true</code> if the object is of the same class as this
   *         object.
   */
  @Override
  public final boolean equals (Object object) {
    return this.getClass ().equals (object);
  }
}