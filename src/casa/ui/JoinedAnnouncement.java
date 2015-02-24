package casa.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

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
 * @author Gabriel Becerra
 */

public class JoinedAnnouncement extends JWindow {
  public JoinedAnnouncement (int waitTime, String username, String name) {
    super ();

    JTextField nt   = new JTextField();
    JTextField text = new JTextField();
    JTextField st   = new JTextField();

    nt.setBackground(Color.LIGHT_GRAY);
    nt.setForeground(Color.BLACK);
    text.setBackground(Color.orange);
    text.setForeground(Color.BLACK);
    st.setBackground(Color.LIGHT_GRAY);
    st.setForeground(Color.BLACK);

    setFont(text.getFont());
    text.setText(username + " has joined " + name);
    getContentPane().add(nt, BorderLayout.NORTH);
    getContentPane().add(text, BorderLayout.CENTER);
    getContentPane().add(st, BorderLayout.SOUTH);

    pack ();
    Dimension screenSize =
        Toolkit.getDefaultToolkit ().getScreenSize ();
    setLocation (screenSize.width - 200,
                 screenSize.height - 100);

    final int pause = waitTime;
    final Runnable closerRunner = new Runnable () {
      public void run () {
        setVisible (false);
        dispose ();
      }
    };
    Runnable waitRunner = new Runnable () {
      public void run () {
        try {
          Thread.sleep (pause);
          SwingUtilities.invokeAndWait (closerRunner);
        } catch (Exception e) {
          e.printStackTrace ();
        }
      }
    };
    setVisible (true);
    Thread splashThread = new Thread (waitRunner, "SplashThread");
    splashThread.start ();
  }
}