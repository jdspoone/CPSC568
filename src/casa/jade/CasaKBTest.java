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
package casa.jade;

import static org.junit.Assert.assertTrue;

import casa.Status;
import casa.TransientAgent;
import casa.ui.BufferedAgentUI;

import jade.semantics.lang.sl.grammar.IdentifyingExpression;
import jade.semantics.lang.sl.grammar.ListOfTerm;
import jade.semantics.lang.sl.tools.SL;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class CasaKBTest {

	TransientAgent agent;

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
  	BufferedAgentUI ui = new BufferedAgentUI();
		agent =  casa.CASA.startAgent(ui);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		agent.exit();
	}

	@Test
	public final void test() {
		//print the facts in the KB
		BufferedAgentUI ui = new BufferedAgentUI();
		Status stat = agent.abclEval("(kb.show :facts T)", null, ui);
		System.out.println("BEFORE:\n"+ui.result());

		agent.kBase.assertFormula(SL.formula("("+"capable "+ "police" + " (agent-identifier :name " + agent.getURL().getFile() + ")"+")"));

		//print the facts in the KB
		ui = new BufferedAgentUI();
		stat = agent.abclEval("(kb.show :facts T)", null, ui);
		System.out.println("AFTER:\n"+ui.result());

		IdentifyingExpression node = (IdentifyingExpression)SL.term("(some ?x (capable police ?x))");
		assertTrue(node.toString().equals("(some ?x (capable police ?x))"));
		ListOfTerm list = agent.kBase.queryRef(node);
		System.out.println("!!!! " + list);
		assertTrue(list.toString().contains("(agent-identifier :name "+agent.getURL().getFile()+")"));
		
	}

}
