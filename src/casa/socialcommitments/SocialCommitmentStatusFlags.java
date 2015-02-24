package casa.socialcommitments;

/**
 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
public enum SocialCommitmentStatusFlags {
  /**
	 */
  STARTED("started"),  /**
	 */
  ENDED("ended"), /**
	 */
  NOTBROKEN("not-broken"),
  /**
	 */
  NOTFULFILLED("not-fulfilled"), /**
	 */
  READY("ready"), /**
	 */
  SHARED("shared"),
  /**
	 */
  DEBTOR("debtor"), /**
	 */
  NOTDEBTOR("not-debtor"), /**
	 */
  MARKED("marked"),
  /**
	 */
  RECURRING("recurring"), /**
	 */
  PERSISTENT("persistent"), /**
	 */
  HAS_ACTION("has-action"), /**
	 */
  OBLIGATED("obligated");
  
  private String name;

  private SocialCommitmentStatusFlags (String newName) {
    name = newName;
  }

  @Override
  public String toString () {
    return name;
  }
}