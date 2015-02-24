/**
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: </p>
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
package casa.ui;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Observable;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class StandardOutAgentUI implements AgentUI {
		
	public String ask(String prompt, String help, int type, String _default) {
		return null;
	}

	public void print(String txt) {
		System.out.print(txt);
	}

	public void println(String txt) {
		System.out.println(txt);
	}

	public void update(Observable o, Object arg) {}

	/**
	 * This doesn't do anything in a text GUI.
	 */
	public void start() {
	}

	public boolean takesHTML() {return false;}

	@Override
	public OutputStream getOutStream() {
		return System.out;
	}
  
}
	
