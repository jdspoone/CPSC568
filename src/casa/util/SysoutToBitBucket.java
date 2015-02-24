package casa.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Redirects sysout and syserr to an internal buffer so it does not appear on the usual 
 * channel.  To retrieve the output, use {@link #getOutput()}.  To restore the original
 * sysout and syserr, use {@link #finish()}.
 *
 * <p><b>Title:</b> CASA Agent Infrastructure</p>
 * <p><b>Copyright: Copyright (c) 2003-2014, Knowledge Science Group, University of Calgary.</b> 
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  
 * The  Knowledge Science Group makes no representations about the suitability
 * of  this software for any purpose.  It is provided "as is" without express
 * or implied warranty.</p>
 * <p><b>Company:</b> Knowledge Science Group, Department of Computer Science, University of Calgary</p>
 * @version 0.9
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class SysoutToBitBucket {
	private ByteArrayOutputStream out;
	private PrintStream ps;
	private PrintStream oldSysout, oldSyserr;

	/**
	 * Redirects sysout and syserr to an internal buffer so it does not appear on the usual channel.
	 */
	public SysoutToBitBucket() {
		out = new ByteArrayOutputStream();
		ps = new PrintStream(out, true);
		oldSysout = System.out;
		oldSyserr = System.err;
		System.setOut(ps);
		System.setErr(ps);
	}
	
	/**
	 * @return all the output (sysout and syserr) from the program from the time this object
	 * was created.
	 */
	public String getOutput() {
		return out.toString();
	}
	
	/**
	 * Restores the sysout and syserr that was in place when this object was constructed.
	 * @return all the output (sysout and syserr) from the program from the time this object
	 * was created.
	 */
	public String finish() {
		System.setOut(oldSysout);
		System.setErr(oldSyserr);
		return out.toString();
	}
}
