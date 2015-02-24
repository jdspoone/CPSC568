
package casa.ontology.owl2;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import casa.TransientAgent;
import casa.jade.CasaKB;
import casa.ontology.Ontology;
import casa.ui.BufferedAgentUI;

import java.text.ParseException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class OWLOntologyWithKB_JavaTest {
	
	static OWLOntology ontology;
	static public final String defaultOntologyClassName = "casa.ontology.owl2.OWLOntology";
	static public final Class<? extends Ontology> defaultOntologyClass = casa.ontology.owl2.OWLOntology.class; //casa.ontology.v3.CASAOntology.class;
  
	static TransientAgent agent;
	static CasaKB KB ;
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		BufferedAgentUI ui = new BufferedAgentUI();
		agent =  casa.CASA.startAgent(ui);
		while(!agent.isInitialized())
		{
			Thread.sleep(500);
		}
		KB=agent.getKnowledgeBase();
		OWLOntology ontology1= (OWLOntology) OWLOntology.getOntology("Myontology");
		agent.setOntology(ontology1);

		try {
			agent.assert_("(hasfur)");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		try {
			agent.assert_("(hasfur mammal)");
		} catch (ParseException e) {
			e.printStackTrace();
		}

		try {
			agent.assert_("(hasfur mammal mammal)");
		} catch (ParseException e) {
			e.printStackTrace();
		}

}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ontology = null;
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	
	//	ParamsMap initParams = AbstractProcess.getInitParams();
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		agent.exit();
	}
	
	@Test
	public final void testzeroargument1() {
		try {
			assertTrue(agent.query("(hasskin)") != null);
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

	@Test
	public final void testzeroargument2() {
					try {
				assertTrue(agent.query("(hasfur)") != null);
			} catch (ParseException e) {

				e.printStackTrace();
			}
		}
	
	
	@Test
	public final void testzeroargument3() {
					try {
						assertFalse(agent.query("(hashair)") != null);
			} catch (ParseException e) {

				e.printStackTrace();
			}
		}
	
	
	@Test
	public final void testzeroargument4() {
					try {
					assertFalse(agent.query("(hastailhairs)") != null);
			} catch (ParseException e) {

				e.printStackTrace();
			}
	}		
	
	@Test
	public final void testfirstargument1() {
		try {
		
			assertFalse(agent.query("(hasfur animal)") != null);
			} catch (ParseException e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public final void testfirstargument1x() {
		try {
		
			assertFalse(agent.query("(hasfur animal)") != null);
			} catch (ParseException e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public final void testfirstargument2() {
			try {
				assertTrue(agent.query("(hasfur mammal)") != null);
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
	}
	
	
	@Test
	public final void testfirstargument3() {
			try {
				assertTrue(agent.query("(hasfur dog)") != null);
				
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
	}
	
	@Test
	public final void testfirstargument4() {
			try {
				assertTrue(agent.query("(hasfur goldenretriver)") != null);
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
	}
	
	@Test
	public final void testfirstargument5() {
			try {
				assertFalse(agent.query("(hasskin animal)") != null);
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
	}
	
	@Test
	public final void testfirstargument6() {
			try {
				assertTrue(agent.query("(hasskin mammal)") != null);
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
	}
	
	@Test
	public final void testfirstargument7() {
			try {
				assertTrue(agent.query("(hasskin dog)") != null);
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
	}
	
	@Test
	public final void testfirstargument8() {
			try {
				assertTrue(agent.query("(hasskin goldenretriver)") != null);
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
	}
	
	@Test
	public final void testfirstargument9() {
			try {
				assertFalse(agent.query("(hashair animal)") != null);
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
	}
	
	@Test
	public final void testfirstargument10() {
			try {
				assertFalse(agent.query("(hashair mammal)") != null);
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
	}
	
	@Test
	public final void testfirstargument11() {
			try {
				assertFalse(agent.query("(hashair dog)") != null);
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
	}
	
	@Test
	public final void testfirstargument12() {
			try {
				assertFalse(agent.query("(hashair goldenretriver)") != null);
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
	}
	
	@Test
	public final void testfirstargument13() {
			try {
				assertFalse(agent.query("(hastailhairs animal)") != null);
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
	}
	
	@Test
	public final void testfirstargument14() {
			try {
				assertFalse(agent.query("(hastailhairs mammal)") != null);
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
	}
	
	@Test
	public final void testfirstargument15() {
			try {
				assertFalse(agent.query("(hastailhairs dog)") != null);
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
	}
	
	@Test
	public final void testfirstargument16() {
			try {
				assertFalse(agent.query("(hastailhairs goldenretriver)") != null);
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
	}
	
 @Test
	public final void testsecondargument1() {
		try {
			assertFalse(agent.query("(hasfur mammal animal)") != null);
			} catch (ParseException e) {
			e.printStackTrace();
		}
		
	}

 @Test
	public final void testsecondargument1x() {
		try {
			assertFalse(agent.query("(hasfur mammal animal)") != null);
			} catch (ParseException e) {
			e.printStackTrace();
		}
		
	}

 
 @Test
	public final void testsecondargument2() {
			
			try {
				assertTrue(agent.query("(hasfur mammal mammal)") != null);
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument3() {
			
			try {
				assertTrue(agent.query("(hasfur mammal dog)") != null);
				
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument4() {
			
			try {
				assertTrue(agent.query("(hasfur mammal goldenretriver)") != null);
				
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument5() {
			
			try {
				assertFalse(agent.query("(hasfur animal animal)") != null);
				
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument6() {
			
			try {
				assertFalse(agent.query("(hasfur animal mammal)") != null);
			
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument7() {
			
			try {
				assertFalse(agent.query("(hasfur animal dog)") != null);
				
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument8() {
			
			try {
				assertFalse(agent.query("(hasfur animal goldenretriver)") != null);
				
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument9() {
			
			try {
				assertFalse(agent.query("(hasfur dog animal)") != null);
				
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument10() {
			
			try {
				assertTrue(agent.query("(hasfur dog mammal)") != null);
				} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument11() {
			
			try {
				assertTrue(agent.query("(hasfur dog dog)") != null);
				} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument12() {
			
			try {
				assertTrue(agent.query("(hasfur dog goldenretriver)") != null);
				} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument13() {
			
			try {
				assertFalse(agent.query("(hasfur goldenretriver animal)") != null);
				} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument14() {
			
			try {
				assertTrue(agent.query("(hasfur goldenretriver mammal)") != null);
				} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument15() {
			
			try {
				assertTrue(agent.query("(hasfur goldenretriver dog)") != null);
				} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument16() {
			
			try {
				assertTrue(agent.query("(hasfur goldenretriver goldenretriver)") != null);
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
	
 @Test
	public final void testsecondargument17() {
			
			try {
				assertFalse(agent.query("(hasskin mammal animal)") != null);
				} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument18() {
			
			try {
				assertTrue(agent.query("(hasskin mammal mammal)") != null);
					} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument19() {
			
			try {
				assertTrue(agent.query("(hasskin mammal dog)") != null);
				} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument20() {
			
			try {
				assertTrue(agent.query("(hasskin mammal goldenretriver)") != null);
				} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument21() {
			
			try {
				assertFalse(agent.query("(hasskin animal animal)") != null);
				} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument22() {
			
			try {
				assertFalse(agent.query("(hasskin animal mammal)") != null);
				} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument23() {
			
			try {
				assertFalse(agent.query("(hasskin animal dog)") != null);
				} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument24() {
			
			try {
				assertFalse(agent.query("(hasskin animal goldenretriver)") != null);
				} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument25() {
			
			try {
				assertFalse(agent.query("(hasskin dog animal)") != null);
				} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument26() {
			
			try {
				assertTrue(agent.query("(hasskin dog mammal)") != null);
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument27() {
			
			try {
				assertTrue(agent.query("(hasskin dog dog)") != null);
					} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument28() {
			
			try {
				assertTrue(agent.query("(hasskin dog goldenretriver)") != null);
					} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument29() {
			
			try {
				assertFalse(agent.query("(hasskin goldenretriver animal)") != null);
					} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument30() {
			
			try {
				assertTrue(agent.query("(hasskin goldenretriver mammal)") != null);
				} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument31() {
			
			try {
				assertTrue(agent.query("(hasskin goldenretriver dog)") != null);
				} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument32() {
			
			try {
				assertTrue(agent.query("(hasskin goldenretriver goldenretriver)") != null);
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument33() {
			
			try {
				assertFalse(agent.query("(hashair mammal animal)") != null);
				} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument34() {
			
			try {
				assertFalse(agent.query("(hashair mammal mammal)") != null);
					} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument35() {
			
			try {
				assertFalse(agent.query("(hashair mammal dog)") != null);
				} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument36() {
			
			try {
				assertFalse(agent.query("(hashair mammal goldenretriver)") != null);
				} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument37() {
			
			try {
				assertFalse(agent.query("(hashair animal animal)") != null);
				} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument38() {
			
			try {
				assertFalse(agent.query("(hashair animal mammal)") != null);
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument39() {
			
			try {
				assertFalse(agent.query("(hashair animal dog)") != null);
				} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument40() {
			
			try {
				assertFalse(agent.query("(hashair animal goldenretriver)") != null);
				} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument41() {
			
			try {
				assertFalse(agent.query("(hashair dog animal)") != null);
				} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument42() {
			
			try {
				assertFalse(agent.query("(hashair dog mammal)") != null);
				} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument43() {
			
			try {
				assertFalse(agent.query("(hashair dog dog)") != null);
					} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument44() {
			
			try {
				assertFalse(agent.query("(hashair dog goldenretriver)") != null);
				} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument45() {
			
			try {
				assertFalse(agent.query("(hashair goldenretriver animal)") != null);
				} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument46() {
			
			try {
				assertFalse(agent.query("(hashair goldenretriver mammal)") != null);
				} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument47() {
			
			try {
				assertFalse(agent.query("(hashair goldenretriver dog)") != null);
					} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument48() {
			
			try {
				assertFalse(agent.query("(hashair goldenretriver goldenretriver)") != null);
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument49() {
			
			try {
				assertFalse(agent.query("(hastailhairs mammal animal)") != null);
					} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument50() {
			
			try {
				assertFalse(agent.query("(hastailhairs mammal mammal)") != null);
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument51() {
			
			try {
				assertFalse(agent.query("(hastailhairs mammal dog)") != null);
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument52() {
			
			try {
				assertFalse(agent.query("(hastailhairs mammal goldenretriver)") != null);
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument53() {
			
			try {
				assertFalse(agent.query("(hastailhairs animal animal)") != null);
				} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument54() {
			
			try {
				assertFalse(agent.query("(hastailhairs animal mammal)") != null);
					} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument55() {
			
			try {
				assertFalse(agent.query("(hastailhairs animal dog)") != null);
					} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument56() {
			
			try {
				assertFalse(agent.query("(hastailhairs animal goldenretriver)") != null);
					} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument57() {
			
			try {
				assertFalse(agent.query("(hastailhairs dog animal)") != null);
				} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument58() {
			
			try {
				assertFalse(agent.query("(hastailhairs dog mammal)") != null);
					} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument59() {
			
			try {
				assertFalse(agent.query("(hastailhairs dog dog)") != null);
					} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument60() {
			
			try {
				assertFalse(agent.query("(hastailhairs dog goldenretriver)") != null);
				} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument61() {
			
			try {
				assertFalse(agent.query("(hastailhairs goldenretriver animal)") != null);
					
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument62() {
			
			try {
				assertFalse(agent.query("(hastailhairs goldenretriver mammal)") != null);
						
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument63() {
			
			try {
				assertFalse(agent.query("(hastailhairs goldenretriver dog)") != null);
			
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
 
 @Test
	public final void testsecondargument64() {
			
			try {
				assertFalse(agent.query("(hastailhairs goldenretriver goldenretriver)") != null);
			} catch (ParseException e) {

				e.printStackTrace();
			}
			
		
	}
}
