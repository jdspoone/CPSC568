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
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
  */
package casa;

/**
 * Since we can't trust that AWT or some other thread is spinning and keeping
 * the process alive when we exit, we should explicitly kill the process if we
 * can't find any CASA agent threads or a main program (assumed to be a thread
 * named "main"). We can do this by calling ExitChecker.startExitCheck()
 * somewhere in the program initialization. This will start the ExitChecker
 * thread, which will periodically wake up (every 2 seconds) to check the
 * existance of CASA agent threads or a main thread, and then call
 * System.exit(0) if it can't find any.<b> {@link ExitChecker#startExitCheck()}
 * can also be called at any time again to do a check immediately.
 * 
 * @author kremer
 */
public class ExitChecker extends Thread {

	/**
	 */
	static private ExitChecker exitChecker = null;

	/**
	 * Start up the ExitChecker thread, or, if it's already running, interrupt
	 * it to perform a check immediately.
	 */
	static void startExitCheck () {
		if (exitChecker == null) {
			exitChecker = new ExitChecker ();
			exitChecker.start ();
		} else {
			exitChecker.interrupt ();
		}
	}

	/**
	 * Hide the constructor (it can only be accessed indirectly by a call to
	 * {link ExitChecker#startExitCheck()}).
	 */
	private ExitChecker () {
		super ("CASAExitChecker");
	}

	/**
	 * Wakes up every 2 seconds to check for an CASA agent threads or a main
	 * thread. If it doesn't find any, calls System.exit().
	 * 
	 * @see java.lang.Thread#run()
	 * @author kremer
	 */
	@Override
	public void run () {
		ThreadGroup group = Thread.currentThread ().getThreadGroup ();
		// We may not be in the top-most thread group, so ensure that we are - JPH
		ThreadGroup parent = group.getParent ();
		while (parent != null) {
			group = parent;
			parent = group.getParent ();
		}
		while (true) {
			Thread[] threads = new Thread[group.activeCount () * 2];
			boolean foundAgents = false;
			int count = group.enumerate (threads, true);
			// Ensure that we get all threads, and that count is not more than the size of threads - JPH
			if (count > threads.length) {
				threads = new Thread[count];
				count = group.enumerate (threads, true);
				count = count > threads.length ? threads.length : count;
			}
			for (int i = count; i > 0;) {
				Thread t = threads[--i];
				if (t instanceof AbstractProcess || t.getName ().equals ("main")) {
					foundAgents = true;
					break;
				}
			}
			try {
				if (foundAgents)
					sleep (2000);
				else {
					System.exit (0);
				}
			} catch (InterruptedException e) {}
		}
	}
}
