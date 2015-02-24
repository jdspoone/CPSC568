package casa.jade;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import casa.TransientAgent;
import casa.ontology.Ontology;
import casa.ontology.owl2.OWLOntology;
import casa.ui.BufferedAgentUI;

import java.text.ParseException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class IfFilterTest {

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
		while(!agent.isInitialized())
		{
			Thread.sleep(500);
		}
		Ontology ontology1= (OWLOntology) OWLOntology.getOntology("Myontology");
		agent.setOntology(ontology1);
		
		agent.assert_("x");
		agent.assert_("(hasfur)");
		agent.assert_("(hasfur mammal)");
	  agent.assert_("(hasfur mammal mammal)");
	}

	@After
	public void tearDown() throws Exception {
		agent.exit();
	}

	@Test
	public final void test() {
		try {
			assertNotNull(agent.query("(hasfur)"));
			assertNotNull(agent.query("x"));
			assertNull   (agent.query("(x)"));
			assertNull   (agent.query("(! (hasfur))"));
			assertNull   (agent.query("(if (hasfur) (x) (hasfur))"));
			assertNull   (agent.query("(if x (x) (hasfur))"));
			assertNotNull(agent.query("(if (hasnofur) (x) (hasfur))"));
			assertNull   (agent.query("(if (hasfur mammal) (x) (hasfur))"));
			assertNotNull(agent.query("(if (! (hasfur)) (x) (!(x)))"));
		} catch (ParseException e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

}
