/**
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. 
 * Permission to use, copy, modify, distribute and sell this software and its 
 * documentation for any purpose is hereby granted without fee, provided that the 
 * above copyright notice appear in all copies and that both that copyright notice 
 * and this permission notice appear in supporting documentation.  The  Knowledge 
 * Science Group makes no representations about the suitability of  this software 
 * for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
package casa.abcl;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.armedbear.lisp.Condition;
import org.armedbear.lisp.ControlTransfer;
import org.armedbear.lisp.LispObject;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class LispException extends ControlTransfer {
	
	Condition condition = null;
	
	public LispException(String message) {
		condition = new Condition(message);
	}

	public LispException(String message, Throwable ex) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintWriter pw = new PrintWriter(out);
    for (Throwable th = ex; th!=null; th = th.getCause()) {
    	if (th!=ex) pw.println("Caused by:");
      ex.printStackTrace(pw);
    }
    pw.flush();
		condition = new Condition(message+"\n"+out.toString());
	}

	public LispException(Condition condition) {
		this.condition = condition;
	}

	/* (non-Javadoc)
	 * @see org.armedbear.lisp.ControlTransfer#getCondition()
	 */
	@Override
	public LispObject getCondition() {
		// TODO Auto-generated method stub
		return condition;
	}

}
