package casa.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
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
 * @author <a href="mailto:ayala@cpsc.ucalgary.ca">Gabriel Becerra</a>
 */
public class CasaSplashWindow extends JWindow {
  protected int min = 0;
  protected int max = 15;
  protected int counter = 0;
  protected JProgressBar jpb;

  public CasaSplashWindow (String filename, int waitTime) {
    super();

    //
    // Label containing the CASA drawing
    //
    JLabel l = new JLabel (new ImageIcon (filename));
    getContentPane ().add (l, BorderLayout.CENTER);

    //
    // Setting up the JprogressBar... --- TODO: sync. it with the processes running
    //
    UIManager.put("ProgressBar.selectionBackground", Color.BLACK);
    UIManager.put("ProgressBar.selectionForeground", Color.WHITE);
    UIManager.put("ProgressBar.foreground", new Color(200, 87, 13));

    jpb = new JProgressBar();
    jpb.setMinimum(min);
    jpb.setMaximum(max);
    jpb.setStringPainted(true);
    jpb.setString("Loading...");

    // setting the pogress bar to be intederminate since we don't know how many
    // seconds it takes to load CASA, yet.
    UIManager.put("ProgressBar.repaintInterval", new Integer(250));
    // specifying how many milliseconds each animation cycle takes
    UIManager.put("ProgressBar.cycleTime", new Integer(10000));
    jpb.setIndeterminate(true);

    getContentPane().add(jpb, BorderLayout.SOUTH);

    pack ();
    Dimension screenSize = Toolkit.getDefaultToolkit ().getScreenSize ();
    Dimension labelSize  = l.getPreferredSize ();
    setLocation (screenSize.width / 2 - (labelSize.width / 2),
                 screenSize.height / 2 - (labelSize.height / 2));

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
          counter = min;
          while (counter <= max) {
            Runnable runMe = new Runnable () {
              public void run () {
                jpb.setValue (counter);
              }
            };
            SwingUtilities.invokeLater(runMe);
            counter ++;
            Thread.sleep(300);
          }

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