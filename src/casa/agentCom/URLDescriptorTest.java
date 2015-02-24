package casa.agentCom;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import casa.exceptions.AmbiguousURLException;
import casa.exceptions.URLDescriptorException;
import casa.util.CASAUtil;

import java.text.ParseException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit test to check {@link URLDescriptor} contract.
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
public class URLDescriptorTest {

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
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testSerialize() {
		try {
			URLDescriptor u7000 = URLDescriptor.make("168.137.0.10:7000/Alice");
			String s = CASAUtil.serialize(u7000);
			try {
				Object o = CASAUtil.unserialize(s, URLDescriptor.class.getCanonicalName());
				assertTrue(u7000.equals(o));
				assertTrue(u7000==o);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} catch (URLDescriptorException e) {
			e.printStackTrace();
		}
	}

	@Test
	public final void testRemoteVsLocal() {
		try {
			URLDescriptor remote = URLDescriptor.make("168.137.0.10:7000/Alice"); //remote
			assertTrue("Alice".equals(remote.getFile())); //file
			assertTrue(7000 == remote.getPort()); //port
			assertTrue("168.137.0.10".equals(remote.getHostString())); //host
			
			URLDescriptor remote_2 = URLDescriptor.make("/Alice"); //should find the remote Alice
			assertTrue("Alice".equals(remote_2.getFile())); // is Alice by file
			assertTrue(remote_2.equals(remote)); // is Alice by equals()
			assertTrue(remote_2==remote); // is remote Alice by identity
			
			URLDescriptor remote_2a = URLDescriptor.make(":7000/Alice"); // should find remote Alice
			assertTrue(remote_2a==remote); // identity with remote Alice
			
			URLDescriptor local = URLDescriptor.make("localhost:7000"); // Another (local) Alice   
			assertFalse(local.equals(remote)); // not equals() to remote Alice
			assertTrue(remote!=local); // not identity with remote Alice

			URLDescriptor.setDefaultAmbiguousURLtoLocal(true);
			URLDescriptor local_2 = URLDescriptor.make(":7000"); // should find the local 7000
			assertTrue(local_2==local); // identity with local Alice

			URLDescriptor.setDefaultAmbiguousURLtoLocal(false);
			try {
				URLDescriptor.make(":7000"); // should throw ambiguousURL
				fail("Expected 'AmbiguousURLException' not thrown");
			} catch (AmbiguousURLException e) {}
			
			URLDescriptor.setDefaultAmbiguousURLtoLocal(true);

			URLDescriptor remote_3 = URLDescriptor.make("/Alice"); // should find the remote Alice
			assertTrue(remote==remote_3); // identity with remote Alice

			URLDescriptor remote_4 = URLDescriptor.make("168.137.0.10/Alice"); //should find the remote Alice
			assertTrue(remote_4==remote); // identity with the remote Alice
			
			URLDescriptor.make("localhost:7000/Alice"); // should name the local 7000 to Alice
			URLDescriptor local_3 = URLDescriptor.make("/Alice"); // should find the LOCAL Alice
			assertTrue(local_3==local);
			
			URLDescriptor.setDefaultAmbiguousURLtoLocal(false);
			try {
				URLDescriptor.make("/Alice"); // should throw ambiguousURL
				fail("Expected 'AmbiguousURLException' not thrown");
			} catch (AmbiguousURLException e) {}

		} catch (URLDescriptorException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public final void testURLCompare() throws URLDescriptorException {
		assertTrue(equalURLs("casa://kremer@68.147.227.36:6701/casa/XXX/Chatty1", "casa://kremer@68.147.227.36:6701/casa/XXX/Chatty1?lac=9000"));
		assertTrue(equalURLs("casa://kremer@68.147.227.36:6701"                       , "casa://kremer@68.147.227.36:6701/casa/XXX/Chatty1?lac=9000"));
		assertTrue(equalURLs("68.147.227.36:6701"                                     , "casa://kremer@68.147.227.36:6701/casa/XXX/Chatty1?lac=9000"));
		assertTrue(equalURLs(":6701"                                                  , "casa://kremer@68.147.227.36:6701/casa/XXX/Chatty1?lac=9000"));
		assertTrue(equalURLs("10.0.1.7:6701"                                          , "casa://kremer@68.147.227.36:6701/casa/XXX/Chatty1?lac=9000"));
		assertTrue(equalURLs("Rex.Local:6701"                                         , "casa://kremer@68.147.227.36:6701/casa/XXX/Chatty1?lac=9000"));
		assertTrue(equalURLs("/Chatty1"                                               , "casa://kremer@68.147.227.36:6701/casa/XXX/Chatty1?lac=9000"));
		assertTrue(equalURLs("/casa/XXX/Chatty1"                                , "casa://kremer@68.147.227.36:6701/casa/XXX/Chatty1?lac=9000"));
		try {
			assertTrue(equalURLs("/casa/XXX/"                                       , "casa://kremer@68.147.227.36:6701/casa/XXX/Chatty1?lac=9000"));
			fail("Expected AmbiguousURLException.");
		} catch (AmbiguousURLException e) {System.out.println(e);}
	}
	
  public final boolean equalURLs(String one, String two) throws URLDescriptorException {
  	return equalURLs(one, two, true);
  }
  
  public final boolean equalURLs(String one, String two, boolean shouldBe) throws URLDescriptorException {
		if ((URLDescriptor.make(one).compareTo(URLDescriptor.make(two))!=0)) {
			if (shouldBe)
				System.out.println(one+".compareTo("+two+") --> false");
			return false;
		}
		if ((URLDescriptor.make(two).compareTo(URLDescriptor.make(one))!=0)) {
			if (shouldBe)
				System.out.println(two+".compareTo("+one+") --> false");
			return false;
		}
		if (!(URLDescriptor.make(one).equals(URLDescriptor.make(two)))) {
			if (shouldBe)
				System.out.println(one+".equals("+two+") --> false");
			return false;
		}
		if (!(URLDescriptor.make(two).equals(URLDescriptor.make(one)))) {
			if (shouldBe)
				System.out.println(two+".equals("+one+") --> false");
			return false;
		}
		if (!(URLDescriptor.make(one).equals(two))) {
			if (shouldBe)
				System.out.println(one+".equals("+two+"[as String]) --> false");
			return false;
		}
		if (!(URLDescriptor.make(two).equals(one))) {
			if (shouldBe)
				System.out.println(two+".equals("+one+"[as String]) --> false");
			return false;
		}
		return true;
  }

}
