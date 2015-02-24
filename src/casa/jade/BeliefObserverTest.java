package casa.jade;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import casa.TransientAgent;
import casa.ui.BufferedAgentUI;
import casa.util.Runnable1;

import jade.semantics.lang.sl.parser.ParseException;
import jade.semantics.lang.sl.parser.SLParser;
import jade.semantics.lang.sl.tools.SL;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
//import sun.awt.image.IntegerComponentRaster;

public class BeliefObserverTest {
	
	TransientAgent agent;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
  	BufferedAgentUI ui = new BufferedAgentUI();
		agent =  casa.CASA.startAgent(ui);
	}

	@After
	public void tearDown() throws Exception {
		agent.exit();
	}
	
  Long theLong = new Long(0);
  boolean called = false;
	
	@Test
	public final void test() throws ParseException, java.text.ParseException {
		BeliefObserver bo = casa.jade.BeliefObserver.onValueChange(agent.kBase, "x", null, new Runnable1<Object, Object>(){
			@Override
			public Object run(Object parameter1) {
				called = true;
				System.out.println("x="+parameter1+" theInt="+theLong);
				theLong = (Long)parameter1;
				return null;
			}});

		agent.kBase.addClosedPredicate(SL.formula("(x ??x)"));
		agent.kBase.assertFormula(SLParser.getParser().parseFormula("(x 1)"));
		assertTrue(called);
		assertTrue(theLong.longValue()==1);

		called = false;
		agent.kBase.assertFormula(SLParser.getParser().parseFormula("(x 2)"));
		assertTrue(called);
		assertTrue(theLong.longValue()==2);
		
		called = false;
		agent.kBase.assertFormula(SLParser.getParser().parseFormula("(x 3)"));
		assertTrue(called);
		assertTrue(theLong.longValue()==3);
		
		agent.kBase.removeObserver(bo);
		
		called = false;
		agent.kBase.assertFormula(SLParser.getParser().parseFormula("(x 4)"));
		assertFalse(called);
		assertTrue(theLong.longValue()==3);
		
		
	}

}
