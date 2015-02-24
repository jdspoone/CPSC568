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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import casa.TransientAgent;
import casa.ontology.owl2.OWLOntology;
import casa.ui.BufferedAgentUI;

import java.text.ParseException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class OntologyFilterTest {

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
		while(!agent.isInitialized())
		{
			Thread.sleep(500);
		}
		OWLOntology ontology1= (OWLOntology) ((OWLOntology)agent.getOntology()).getOntology("Myontology");
		agent.setOntology(ontology1);
		
		agent.assert_("(hasfur)");
		agent.assert_("(hasfur mammal)");
	  agent.assert_("(hasfur mammal mammal)");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		agent.exit();
	}
	
	public void setParameters()
	{
	//	OntologyFilter.found = false;
		//OntologyFilter.queryResult = null;
	}

	@Test
	public final void test() {
		try {
			assertNull(agent.query("(hasfur animal goldenretriver)"));
			assertNotNull(agent.query("(and (hasfur dog) (hasfur))"));
			//setParameters();
			assertNotNull(agent.query("(hasfur)"));
			//setParameters();
			assertNotNull(agent.query("(hastailhairs)"));
			//setParameters();
		
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	

}
