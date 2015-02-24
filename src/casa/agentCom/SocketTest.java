package casa.agentCom;

import static org.junit.Assert.assertTrue;

import casa.ML;
import casa.MLMessage;
import casa.TransientAgent;
import casa.exceptions.URLDescriptorException;
import casa.util.CASAUtil;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit test for to see if agents can communiate between processes.
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
public class SocketTest {
	TransientAgent local;
	int remotePort=6010, localPort=6011;
	URLDescriptor remoteURL;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		remoteURL = URLDescriptor.make(":"+remotePort);
  	CASAUtil.startAnAgent(TransientAgent.class, "remote", remotePort, null
  			, "PROCESS", "INDEPENDENT"
  			, "TRACE", "10"
  			, "TRACETAGS", "sockets,warning,msg,msgHandling,kb9,eventloop,-info,commitments,-policies9,-lisp,-eventqueue9,-conversations"
  			);
  	local = CASAUtil.startAnAgent(TransientAgent.class, "local", localPort, null
  			, "PROCESS", "CURRENT"
  			, "TRACE", "10"
  			, "TRACETAGS", "sockets,warning,msg,msgHandling,kb9,eventloop,-info,commitments,-policies9,-lisp,-eventqueue9,-conversations"
  			);
  	CASAUtil.sleepIgnoringInterrupts(3000, null);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		local.sendMessage(MLMessage.getNewMLMessage(ML.PERFORMATIVE, ML.REQUEST
				, ML.ACT, ML.EXECUTE
				, ML.RECEIVER, ":"+remotePort
				, ML.LANGUAGE, "Lisp"
				, ML.CONTENT, "(agent.exit)"));
		local.sendMessage(MLMessage.getNewMLMessage(ML.PERFORMATIVE, ML.REQUEST
				, ML.ACT, ML.EXECUTE
				, ML.RECEIVER, ":1024"
				, ML.LANGUAGE, "Lisp"
				, ML.CONTENT, "(agent.exit)"));
  	CASAUtil.sleepIgnoringInterrupts(2000, null);
  	local.exit();
	}

	@Test
	public final void testPing() throws URLDescriptorException {
		URLDescriptor testRemote = local.doPing_sync(remoteURL, 2000);
		assertTrue(testRemote!=null);
		assertTrue("result url="+testRemote+", local url="+remoteURL, testRemote.equals(remoteURL));
	}

}
