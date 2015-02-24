package casa.ui;

import casa.util.Trace;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;


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

class LongInputVerifier extends InputVerifier {
  long min;
	long max;
  public LongInputVerifier(long min, long max) {
    this.min = min; this.max = max;
  }
  public boolean verify (JComponent input) {
    try {
      JTextField tf = (JTextField) input;
      long x = Long.parseLong (tf.getText ());
    } catch (NumberFormatException ex) {
    	Trace.log("error", "Input must be an integer between "+Long.toString(min)+" and "+Long.toString(max)+".");
      return false;
    }
    return true;
  }
}
