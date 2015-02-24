package casa.util;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 *
 * <p>Title: CASA</p>
 * <p>Description: Creates JOptionPane messages inorder to communicate the user that
 * an error has occured... Include in the message the correct steps to avoid such problems</p>
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary.
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  The
 * Knowledge Science Group makes no representations about the suitability of
 * this software for any purpose.  It is provided "as is" without express or
 * implied warranty.</p>
 *
 * @author <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 * @author Gabriel Becerra
 */
public class CasaErrorMessage extends JOptionPane {
  //
  // Elements needed in order to create a CasaErrorMessage
  //
  private ImageIcon icon        = new ImageIcon ("/images/customGraphics/real_innericon.png");
  private String    title       = new String();
  private String    message     = new String();
  private int       messageType = JOptionPane.ERROR_MESSAGE;

  /**
   *
   * @param title is the Error Type
   * @param message is the explanation of the error and the "correct" steps that are required
   * in order to avoid such problems (if possible)
   */
  public CasaErrorMessage(String title, String message) {
    this.title = title;
    this.message = message;

    buildMessage();
  }

  /**
   * Constructs the JOptionPaneDialog...
   */
  private void buildMessage() {
    JOptionPane.showMessageDialog (null, message, title, messageType, icon);
  }
}