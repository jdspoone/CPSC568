package casa.ui;

import javax.swing.text.*;

/**
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
 */
public class JTextFieldFilter extends PlainDocument {
  private static final int LOWERCASE_INT = 0;
  private static final int UPPERCASE_INT = 1;
  private static final int ALPHA_INT = 2;
  private static final int NUMERIC_INT = 3;
  private static final int FLOAT_INT = 4;
  private static final int ALPHA_NUMERIC_INT = 5;

  private static final String LOWERCASE_STRING = "abcdefghijklmnopqrstuvwxyz";
  private static final String UPPERCASE_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String ALPHA_STRING = LOWERCASE_STRING + UPPERCASE_STRING;
  private static final String NUMERIC_STRING = "0123456789";
  private static final String FLOAT_STRING = NUMERIC_STRING + ".";
  private static final String ALPHA_NUMERIC_STRING = ALPHA_STRING + NUMERIC_STRING;

  public static final JTextFieldFilter LOWERCASE = new JTextFieldFilter (LOWERCASE_INT);
  public static final JTextFieldFilter UPPERCASE = new JTextFieldFilter (UPPERCASE_INT);
  public static final JTextFieldFilter ALPHA = new JTextFieldFilter (ALPHA_INT);
  public static final JTextFieldFilter NUMERIC = new JTextFieldFilter (NUMERIC_INT);
  public static final JTextFieldFilter NUMERIC_WITH_NEGATIVE = new JTextFieldFilter (NUMERIC_INT, true);
  public static final JTextFieldFilter FLOAT = new JTextFieldFilter (FLOAT_INT);
  public static final JTextFieldFilter FLOAT_WITH_NEGATIVE = new JTextFieldFilter (FLOAT_INT, true);
  public static final JTextFieldFilter ALPHA_NUMERIC = new JTextFieldFilter (ALPHA_NUMERIC_INT);

  protected int filterMode;
  protected String acceptedChars;
  protected boolean negativeAccepted = false;

  private JTextFieldFilter (int newFilterMode) {
    this.filterMode = newFilterMode;
    switch (filterMode) {
      case LOWERCASE_INT:
        acceptedChars = LOWERCASE_STRING;
        break;
      case UPPERCASE_INT:
        acceptedChars = UPPERCASE_STRING;
        break;
      case ALPHA_INT:
        acceptedChars = ALPHA_STRING;
        break;
      case NUMERIC_INT:
        acceptedChars = NUMERIC_STRING;
        break;
      case FLOAT_INT:
        acceptedChars = FLOAT_STRING;
        break;
      case ALPHA_NUMERIC_INT:
        acceptedChars = ALPHA_NUMERIC_STRING;
        break;
    }
  }

  private JTextFieldFilter (int newFilterMode, boolean newNegativeAccepted) {
    this.filterMode = newFilterMode;
    switch (filterMode) {
      case LOWERCASE_INT:
        acceptedChars = LOWERCASE_STRING;
        break;
      case UPPERCASE_INT:
        acceptedChars = UPPERCASE_STRING;
        break;
      case ALPHA_INT:
        acceptedChars = ALPHA_STRING;
        break;
      case NUMERIC_INT:
        acceptedChars = NUMERIC_STRING;
        break;
      case FLOAT_INT:
        acceptedChars = FLOAT_STRING;
        break;
      case ALPHA_NUMERIC_INT:
        acceptedChars = ALPHA_NUMERIC_STRING;
        break;
    }

    setNegativeAccepted(newNegativeAccepted);
  }

  public void setNegativeAccepted (boolean newNegativeAccepted) {
    if (filterMode == NUMERIC_INT || filterMode == FLOAT_INT) {
      this.negativeAccepted = newNegativeAccepted;
      switch (filterMode) {
        case NUMERIC_INT:
          if (negativeAccepted) {
            acceptedChars = NUMERIC_STRING + "-";
          } else {
            acceptedChars = NUMERIC_STRING;
          }
          break;
        case FLOAT_INT:
          if (negativeAccepted) {
            acceptedChars = FLOAT_STRING + "-";
          } else {
            acceptedChars = FLOAT_STRING;
          }
          break;
      }
    }
  }

  public void insertString (int offset, String str, AttributeSet attr) throws
      BadLocationException {
    if (str == null)
      return;

    // Fix the case if it is required.
    if (filterMode == UPPERCASE_INT)
      str = str.toUpperCase ();
    else if (filterMode == LOWERCASE_INT)
      str = str.toLowerCase ();

    // Ensure that all of the characters are accepted, otherwise do not insert
    // the text.
    for (int i = 0; i < str.length (); i++) {
      if (acceptedChars.indexOf (String.valueOf (str.charAt (i))) == -1)
        return;
    }

    // If we are attempting to insert a second ',', do not insert the text.
    if (filterMode == FLOAT_INT) {
      if (str.indexOf ('.') != -1) {
        if (getText (0, getLength ()).indexOf ('.') != -1) {
          return;
        }
      }
    }

    // If the negative isn't at the beginning, do not insert the text.
    if (negativeAccepted && str.indexOf ('-') != -1) {
      if (str.indexOf ('-') != 0 || offset != 0) {
        return;
      }
    }

    super.insertString (offset, str, attr);
  }
}