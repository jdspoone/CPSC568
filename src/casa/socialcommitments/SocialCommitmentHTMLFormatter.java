package casa.socialcommitments;

import javax.swing.JList;

/**
 * <code>SocialCommitmentHTMLFormatter</code> is a class which can be used to
 * format {@link SocialCommitment} objects using HTML tages. This formatter is
 * intended to be used when social commitments are in {@link JList}s. The text
 * describing the social commitment results from a call to
 * {@link SocialCommitment#toString()}. The text is formatted as plain black
 * text with the following modifications:
 * <ul>
 * <li>If the commitment has been fulfilled or canceled, the entire commitment
 * is formatted with strike-through (using the <code>&lt;strike&gt;</code>
 * tag).
 * <li>If the commitment has been violated, the font colour is changed to red.
 * <li>If the commitment has ended (fulfilled, canceled or violated), it is
 * faded in proportion to the amount of time that has passed since the
 * commitment ended. The speed of the fading is controlled by a parameter.
 * </ul>
 * 
 * @author Jason Heard
 * @version 0.9
 */
public class SocialCommitmentHTMLFormatter {

  /**
   * Formats a {@link SocialCommitment} object using HTML tags. A description of
   * the formatting can be found in this classes description.
   * 
   * @param socialCommitment The social commitment to format.
   * @param fadeTime The time in milliseconds that it takes a commitment to fade
   *            out when it ends (is fulfilled, canceled, or violated).
   * @return A string containing HTML code which will print the commitment.
   */
  public static String formatCommitment (SocialCommitment socialCommitment,
      long fadeTime) {
    StringBuffer display = new StringBuffer ();

    display.append ("<html>");

    if (!socialCommitment.flagSet (SocialCommitmentStatusFlags.NOTFULFILLED)) {
      display.append ("<strike>");
    }

    display.append ("<font color=");
    display.append (fadeColor (socialCommitment, fadeTime));
    display.append (">");

    display.append (socialCommitment.toString ());

    display.append ("</font>");
    if (!socialCommitment.flagSet (SocialCommitmentStatusFlags.NOTFULFILLED)) {
      display.append ("</strike>");
    }
    display.append ("</html>");

    return display.toString ();
  }

  /**
   * @param fadeTime
   * @param socialCommitment
   */
  private static String fadeColor (SocialCommitment socialCommitment,
      long fadeTime) {
    long fade = 0;
    StringBuffer colorString = new StringBuffer ();

    if (socialCommitment.flagSet (SocialCommitmentStatusFlags.ENDED)) {
      fade = (System.currentTimeMillis () - socialCommitment.getEndTime ())
          * 255 / fadeTime;
      if (fade > 255) {
        fade = 255;
      }
    }
    if (fade == 0) {
      colorString.append ("BLACK");
    } else {
      colorString.append ("\"#");
      String hex;
      if (fade < 16) {
        hex = "0" + Long.toHexString (fade).toUpperCase ();
      } else {
        hex = Long.toHexString (fade).toUpperCase ();
      }
      if (!socialCommitment.flagSet (SocialCommitmentStatusFlags.NOTBROKEN)) {
        colorString.append ("FF");
      } else {
        colorString.append (hex);
      }
      colorString.append (hex);
      colorString.append (hex);
      colorString.append ("\"");
    }

    return colorString.toString ();
  }
}