package casa.ui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

/**
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 * @author  <a href="mailto:ayala@cpsc.ucalgary.ca">Gabriel Becerra</a>
 * @author  <a href="mailto:sedachv@cpsc.ucalgary.ca">Vladimir Sedach</a>
 */
public class LACDesktop extends MDIDesktopPane {
  //
  // CASA object
  //
  /**
	 */
  protected MDIDesktopPane desktop;

  //
  // Swing Objects
  //
  public JScrollPane scroller = new JScrollPane();
  public JViewport   viewport;

  /**
   *
   */
  public LACDesktop() {
    //Add a pane for the desktop
    desktop = new MDIDesktopPane();
    desktop.setBorder(BorderFactory.createLineBorder(Color.blue));
    //desktop.setOpaque(true);



    scroller.getViewport().add(desktop);
    //scroller.getViewport().setOpaque(true);
    setLayout(new BorderLayout());
    add(scroller, BorderLayout.CENTER);

  }

}

