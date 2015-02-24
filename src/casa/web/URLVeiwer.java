/**
 */
package casa.web;

import casa.util.CASAUtil;

import java.io.IOException;

/**
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: </p>
 * 
 * A simple, static class to display a URL in the system browser.<p>
 *
 * Under Unix, the system browser is hard-coded to be 'netscape'.
 * Netscape must be in your PATH for this to work.  This has been
 * tested with the following platforms: AIX, HP-UX and Solaris.<p>
 *
 * Under Windows, this will bring up the default browser under windows,
 * usually either Netscape or Microsoft IE.  The default browser is
 * determined by the OS.  This has been tested under Windows 95/98/NT.<p>
 *
 * Examples:<br>
 * BrowserControl.displayURL("http://www.javaworld.com")<br>
 * BrowserControl.displayURL("file://c:\\docs\\index.html")<br>
 * BrowserContorl.displayURL("file:///user/joe/index.html");<p>
 * 
 * Note - you must include the url type -- either "http://" or
 * "file://".<p>
 * 
 * This class is adapted from Steven Spencer, JavaWorld.com, 01/01/99 
 * and Ryan Stevens ("Follow-up tips").<p>
 * 
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. 
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  
 * The  Knowledge Science Group makes no representations about the suitability
 * of  this software for any purpose.  It is provided "as is" without express
 * or implied warranty.</p>
 * <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
public class URLVeiwer
{
	/**
	 * Display a file in the system browser.  If you want to display a
	 * file, you must include the absolute path name.
	 *
	 * @param url the file's url (the url must start with either "http://" or
	 * "file://").
	 */
	public static void displayURL(String url)
	{
		String cmd = null;
		Process p;
		try
		{
			switch (getPlatform()) {
			case WINDOWS: 
				// cmd = 'rundll32 url.dll,FileProtocolHandler http://...'
				cmd = WIN_PATH + " " + WIN_FLAG + " " + url;
				p = Runtime.getRuntime().exec(cmd);
				break;
			case UNIX:
				for (String path: UNIX_PATH) {
					try
					{
						// Under Unix, Netscape has to be running for the "-remote"
						// command to work.  So, we try sending the command and
						// check for an exit value.  If the exit command is 0,
						// it worked, otherwise we need to start the browser.
						// cmd = 'netscape -remote openURL(<url>)'
						cmd = path + " " + UNIX_FLAG + "(" + url + ")";
						p = Runtime.getRuntime().exec(cmd);
						// wait for exit code -- if it's 0, command worked,
						// otherwise we need to start the browser up.
						int exitCode = p.waitFor();
						if (exitCode == 0)
							break;
						// Command failed, start up the browser
						// cmd = 'netscape http://www.javaworld.com'
						cmd = path + " "  + url;
						p = Runtime.getRuntime().exec(cmd);
						//setup to interrupt ourself in 2 seconds if the browser works.
						final Thread thisThread = Thread.currentThread(); 
						new Thread(new Runnable(){@Override public void run() {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {} 
							thisThread.interrupt();}}).start();
						try {
						exitCode = p.waitFor();
						if (exitCode == 0)
							break;
						}
						catch(InterruptedException x) { // we assume the browser is still running an displaying the URL
							break;
						}
					}
					catch(InterruptedException x)
					{
						System.err.println("Error bringing up browser, cmd='" +
								cmd + "'");
						System.err.println("Caught: " + x);
						continue;
					}
					catch(Throwable x) {
						// couldn't exec browser
						System.err.println("Could not invoke browser, command=" + cmd);
						System.err.println("Caught: " + x);
						continue;
					}
					System.err.println("Failed to bring up or invoke browser "+path);
				}
				break;
			case MAC:
				try {
					CASAUtil.callMethod(Class.forName("com.apple.mrj.MRJFileUtils"), "openURL", null, new Class[]{String.class}, new Object[]{url});
				} catch (Throwable ex) {}
				break;
			}
		}
		catch(Throwable x)
		{
			// couldn't exec browser
			System.err.println("Could not invoke browser, command=" + cmd);
			System.err.println("Caught: " + x);
		}
	}

	public static PLATFORM getPlatform() {
		String os = System.getProperty("os.name");
//		System.out.println("OS = "+os);
		if ( os != null && os.startsWith(WIN_ID))
			return PLATFORM.WINDOWS;
		else if ( os != null && os.startsWith(MAC_ID))
			return PLATFORM.MAC;
		else
			return PLATFORM.UNIX;
	}
	/**
	 * Simple example.
	 */
	public static void main(String[] args)
	{
		displayURL("http://casa.cpsc.ucalgary.ca/doc/CasaUserManual.pdf");
	}
	// Used to identify the windows platform.
	private static final String WIN_ID = "Windows";
	private static final String MAC_ID = "Mac";
	private static enum PLATFORM { WINDOWS, MAC, UNIX };
	// The default system browser under windows.
	private static final String WIN_PATH = "rundll32";
	// The flag to display a url.
	private static final String WIN_FLAG = "url.dll,FileProtocolHandler";
	// The default browser under unix.
	private static final String[] UNIX_PATH = new String[]{"netscape", "firefox"};
	// The flag to display a url.
	private static final String UNIX_FLAG = "-remote openURL";
}
