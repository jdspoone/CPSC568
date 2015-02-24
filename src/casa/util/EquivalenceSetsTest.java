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
package casa.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class EquivalenceSetsTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		test = new EquivalenceSets<String>();
	}

	/** the object used for testing */
	static EquivalenceSets<String> test;
	
	@Test
	public void test1() throws Exception {
		test.put("dog");
		test.setEqual("dog", "k9");
		assertTrue(test.equal("dog", "dog"));
		assertTrue(test.equal("dog", "k9"));
		assertTrue(test.equal("k9", "dog"));
		assertFalse(test.equal("dog", "x"));
		assertFalse(test.equal("x", "dog"));

		test.setEqual("cat", "feline");
		assertFalse(test.equal("dog", "cat"));
		assertFalse(test.equal("cat", "dog"));
		assertFalse(test.equal("dog", "feline"));
		assertFalse(test.equal("feline", "dog"));
		
		test.setEqual("k9", "doggie");
		assertTrue(test.equal("dog", "doggie"));
		assertTrue(test.equal("k9", "doggie"));
		
		String[] eqClass = test.getEqClass("dog");
		for (String s: eqClass) System.out.println(s);
		assertEquals(3, eqClass.length);
		
		eqClass = test.getEqClass("cat");
		assertEquals(2, eqClass.length);
	}

}
