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
package casa;

import static org.junit.Assert.*;

import casa.exceptions.IllegalOperationException;

import org.junit.Test;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */

public class Test1 {
	
	@Test
	public void messages() throws IllegalOperationException {
		MLMessage msg = MLMessage.getNewMLMessage(ML.KQML, ML.PERFORMATIVE, ML.INFORM);
		assertTrue(msg instanceof KQMLMessage);
		msg = MLMessage.getNewMLMessage(ML.XML, ML.PERFORMATIVE, ML.INFORM);
		assertTrue(msg instanceof XMLMessage);
		try {
			msg = null;
			msg = MLMessage.getNewMLMessage("XXX", ML.PERFORMATIVE, ML.INFORM);
			assertTrue("A false success was returned for target XXX.\nMessage: '"+msg+"'.\nknownSubclasses: "+MLMessage.knownSubclasses, msg==null);
		} catch (Exception e) {
			System.out.println("Exeption expected: "+e);
			System.out.println(MLMessage.knownSubclasses.toString());
		}
	}

}
