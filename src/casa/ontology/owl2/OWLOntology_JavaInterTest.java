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
package casa.ontology.owl2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import casa.exceptions.IllegalOperationException;
import casa.ontology.Ontology;

import java.util.Set;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.io.StringDocumentTarget;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class OWLOntology_JavaInterTest {
	
	static OWLOntology ontology;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Ontology o = OWLOntology.getOntology("http://casa.cpsc.ucalgary.ca/ontologies/casa.owl");
		assertTrue(o instanceof Ontology);
		ontology = (OWLOntology)o;
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
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testIsConsistent() {
		assertTrue("Ontology is inconsistent", ontology.isConsistent());
	}

	@Test
	public final void testDescribeEvent() throws IllegalOperationException {
		String buf = ontology.describe("event");
		System.out.println("\n>>> Ontology in input format:\n"+buf+"\n<<<\n");
		assertTrue(buf.length()>0);
		assertTrue(buf.contains("Ontology(<http://casa.cpsc.ucalgary.ca/ontologies/events.owl>"));
	}

	@Test
	public final void testDescribeEvent_Manchester() throws IllegalOperationException {
		String buf = ontology.describe("event", new ManchesterOWLSyntaxOntologyFormat());
		System.out.println("\n>>> Ontology in Manchester format:\n"+buf+"\n<<<\n");
		assertTrue(buf.length()>0);
		assertTrue(buf.contains("Ontology: <http://casa.cpsc.ucalgary.ca/ontologies/events.owl>"));
	}

	@Test
	public final void testDescribeAction() throws IllegalOperationException {
		String buf = ontology.describe("action");
		System.out.println("\n>>> Ontology in input format:\n"+buf+"\n<<<\n");
		assertTrue(buf.length()>0);
		assertTrue(buf.contains("Ontology(<http://casa.cpsc.ucalgary.ca/ontologies/actions.owl>"));
	}

	@Test
	public final void testDescribeAction_Manchester() throws IllegalOperationException {
		String buf = ontology.describe("action", new ManchesterOWLSyntaxOntologyFormat());
		System.out.println("\n>>> Ontology in Manchester format:\n"+buf+"\n<<<\n");
		assertTrue(buf.length()>0);
		assertTrue(buf.contains("Ontology: <http://casa.cpsc.ucalgary.ca/ontologies/actions.owl>"));
	}

	@Test
	public final void testDescribeCasa() throws IllegalOperationException {
		StringDocumentTarget documentTarget = new StringDocumentTarget();
		//OWLOntologyFormat outputFormat = new ManchesterOWLSyntaxOntologyFormat();
//		if(originalFormat.isPrefixOWLOntologyFormat()) {
//			((PrefixOWLOntologyFormat)outputFormat).copyPrefixesFrom(originalFormat.asPrefixOWLOntologyFormat());
//		}
		try {
			OWLOntology.getOntologyManager().saveOntology(ontology, documentTarget);
		} catch (OWLOntologyStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String buf = documentTarget.toString();
		System.out.println("\n>>> Ontology in input format:\n"+buf+"\n<<<\n");
		assertTrue(buf.length()>0);
		assertTrue(buf.contains("Ontology(<http://casa.cpsc.ucalgary.ca/ontologies/casa.owl>"));
	}

	@Test
	public final void testDescribeCasa_Manchester() throws IllegalOperationException {
		StringDocumentTarget documentTarget = new StringDocumentTarget();
		OWLOntologyFormat outputFormat = new ManchesterOWLSyntaxOntologyFormat();
//		if(originalFormat.isPrefixOWLOntologyFormat()) {
//			((PrefixOWLOntologyFormat)outputFormat).copyPrefixesFrom(originalFormat.asPrefixOWLOntologyFormat());
//		}
		try {
			OWLOntology.getOntologyManager().saveOntology(ontology, outputFormat, documentTarget);
		} catch (OWLOntologyStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String buf = documentTarget.toString();
		System.out.println("\n>>> Ontology in Manchester format:\n"+buf+"\n<<<\n");
		assertTrue(buf.length()>0);
		assertTrue(buf.contains("Ontology: <http://casa.cpsc.ucalgary.ca/ontologies/casa.owl>"));
	}

	@Test
	public final void testFindEntity() throws IllegalOperationException {
		OWLEntity entity = ontology.findEntity("action");
		assertNotNull(entity);
		entity = ontology.findEntity("xxxyyyzzz");
		assertNull(entity);
	}

	@Test
	public final void testFindIRI() throws IllegalOperationException {
		IRI iri = ontology.findIRI("action");
		assertNotNull(iri);
		iri = ontology.findIRI("xxxyyyzzz");
		assertNull(iri);
	}

	@Test
	public final void testFindOntologyOfSimpleName() throws IllegalOperationException {
		Ontology ont = ontology.findOntologyOfSimpleName("event");
		assertNotNull(ont);
		IRI iri = IRI.create("http://casa.cpsc.ucalgary.ca/ontologies/events.owl");
		Ontology actionOnt = (Ontology)OWLOntology.getOntologyManager().getOntology(iri);
		assertNotNull(actionOnt);
		assertEquals(ont, actionOnt);
		iri = IRI.create("http://casa.cpsc.ucalgary.ca/ontologies/actions.owl");
		actionOnt = (Ontology)OWLOntology.getOntologyManager().getOntology(iri);
		assertNotNull(actionOnt);
		assertFalse(ont.equals(actionOnt));
	}
	
	@Test
	public final void testisObject() throws IllegalOperationException {
		assertTrue(ontology.isObject("action"));
		assertFalse(ontology.isObject("xxxyyyzzz"));
	}

	@Test
	public final void testisType() throws IllegalOperationException {
		assertTrue(ontology.isType("action"));
		assertFalse(ontology.isType("xxxyyyzzz"));
	}

	@Test
	public final void testisIndividual() throws IllegalOperationException {
		assertFalse(ontology.isIndividual("action"));
		assertFalse(ontology.isIndividual("xxxyyyzzz"));
		
		assertFalse(ontology.isIndividual("George"));
		ontology.addIndividual("George", "inform");
		assertTrue(ontology.isIndividual("George"));
		ontology.removeIndividual("George");
		assertFalse(ontology.isIndividual("George"));
	}

	@Test
	public final void testIsa2() throws IllegalOperationException {
		assertTrue(ontology.isa("performative", "action"));
		assertTrue(ontology.isa("action", "event"));
		assertTrue(ontology.isa("performative", "event"));
		assertFalse(ontology.isa("action", "performative"));
		assertFalse(ontology.isa("event", "action"));
		
		ontology.addIndividual("George", "inform");
		assertTrue(ontology.isa("George", "inform"));
		assertFalse(ontology.isa("George", "request"));
		ontology.removeIndividual("George");
	}

	@Test
	public final void testIsa1() throws IllegalOperationException {
		Set<String> set = ontology.isa("performative"); 
		System.out.println("testIsa1(): "+set);
		assertTrue(set.size()>2);
		assertTrue(set.contains("Thing"));
		assertTrue(set.contains("action"));
		assertTrue(set.contains("event"));
	}

	@Test
	public final void testRelatedTo() throws IllegalOperationException {
		assertTrue(ontology.relatedTo("FIPA-follows",  "reply",   "request"));
		assertFalse(ontology.relatedTo("FIPA-follows", "request", "reply"));
		assertTrue(ontology.relatedTo("FIPA-preceeds", "request", "reply"));

		assertTrue(ontology.relatedTo("FIPA-follows",  "agree",   "request"));
		assertFalse(ontology.relatedTo("FIPA-follows", "request", "agree"));
		assertTrue(ontology.relatedTo("FIPA-preceeds", "request", "agree"));
  }
	
	@Test
	public final void testChildren() throws IllegalOperationException {
		assertFalse(ontology.isChild("performative").contains("reply"));
		assertTrue (ontology.isChild("performative").contains("inform"));
		assertTrue (ontology.isDescendant("performative").contains("reply"));
	}

	@Test
	public final void testAddIndividual() throws IllegalOperationException {
		ontology.addIndividual("Fred", "inform");
		OWLEntity ent = ontology.findIndividualInClosureBySimpleName("Fred");
		assertNotNull(ent);
		OWLNamedIndividual ind = (OWLNamedIndividual)ent;
		assertTrue(ontology.getReasoner().isDefined(ind));
		assertTrue(ontology.instanceOf("Fred", "inform"));
		assertTrue(ontology.instanceOf("Fred", "event"));
		assertFalse(ontology.instanceOf("Fred", "reply"));
		assertTrue(ontology.isa("Fred", "inform")); // assumes loose interpretation of isa
		assertTrue(ontology.isa("Fred", "event"));  // "
		assertFalse(ontology.isa("Fred", "reply"));
		
		ontology.removeIndividual("Fred");
		assertFalse(ontology.getReasoner().isDefined(ind));
	}

	@Test
	public final void testAddIndividual2() throws IllegalOperationException {
		ontology.addIndividual("Fred", "inform", "ack");
		OWLEntity ent = ontology.findIndividualInClosureBySimpleName("Fred");
		assertNotNull(ent);
		OWLNamedIndividual ind = (OWLNamedIndividual)ent;
		assertTrue(ontology.getReasoner().isDefined(ind));
		assertTrue(ontology.instanceOf("Fred", "inform"));
		assertTrue(ontology.instanceOf("Fred", "ack"));
		assertTrue(ontology.instanceOf("Fred", "event"));
		assertFalse(ontology.instanceOf("Fred", "reply"));
		assertTrue(ontology.isa("Fred", "inform")); // assumes loose interpretation of isa
		assertTrue(ontology.isa("Fred", "ack"));    // "
		assertTrue(ontology.isa("Fred", "event"));  // "
		assertTrue(ontology.isa("Fred", "inform")); // assumes loose interpretation of isa
		assertFalse(ontology.isa("Fred", "reply"));
		
		ontology.removeIndividual("Fred");
		assertFalse(ontology.getReasoner().isDefined(ind));
	}

	@Test
	public final void testAddType() throws IllegalOperationException {
		ontology.addType("Fred", "inform", "ack");
		OWLEntity ent = ontology.findClassInClosureBySimpleName("Fred");
		assertNotNull(ent);
		OWLClass cls = (OWLClass)ent;
		assertTrue(ontology.getReasoner().isDefined(cls));
		assertTrue(ontology.isa("Fred", "inform"));
		assertTrue(ontology.isa("Fred", "event"));
		assertFalse(ontology.isa("Fred", "reply"));
		assertFalse(ontology.instanceOf("Fred", "inform"));
		assertFalse(ontology.instanceOf("Fred", "event"));
		assertFalse(ontology.instanceOf("Fred", "reply"));
		
		ontology.removeType("Fred");
		assertFalse(ontology.getReasoner().isDefined(cls));
	}

	@Test
	public final void testAddType2() throws IllegalOperationException {
		ontology.addType("Fred", "inform", "ack");
		OWLEntity ent = ontology.findClassInClosureBySimpleName("Fred");
		assertNotNull(ent);
		OWLClass cls = (OWLClass)ent;
		assertTrue(ontology.getReasoner().isDefined(cls));
		assertTrue(ontology.isa("Fred", "inform"));
		assertTrue(ontology.isa("Fred", "ack"));
		assertTrue(ontology.isa("Fred", "event"));
		assertFalse(ontology.isa("Fred", "reply"));
		assertFalse(ontology.instanceOf("Fred", "inform"));
		assertFalse(ontology.instanceOf("Fred", "ack"));
		assertFalse(ontology.instanceOf("Fred", "event"));
		assertFalse(ontology.instanceOf("Fred", "reply"));
		
		ontology.removeType("Fred");
		assertFalse(ontology.getReasoner().isDefined(cls));
	}
	
	@Test
	public final void testAddRelation_ind_ind() throws IllegalOperationException {
		ontology.addIndividual("Alice", "request");
		ontology.addIndividual("Bob", "reply");
		ontology.addRelation("bigger", "Bob", "Alice");
		assertTrue (ontology.relatedTo("bigger", "Bob", "Alice"));
		assertFalse(ontology.relatedTo("bigger", "Alice", "Bob"));
		assertFalse(ontology.relatedTo("bigger", "Alice", "Alice"));
		assertFalse(ontology.relatedTo("bigger", "Bob", "Bob"));
		ontology.removeIndividual("Alice");
		ontology.removeIndividual("Bob");
		ontology.removeEntity("bigger", OWLObjectProperty.class);
		assertFalse(ontology.isIndividual("Alice"));
		assertFalse(ontology.isIndividual("Bob"));
	}

	@Test
	public final void testAddRelation_cls_cls() throws IllegalOperationException {
		ontology.addRelation("bigger", "request", "reply");
		assertFalse(ontology.relatedTo("bigger", "reply", "request"));
		assertTrue (ontology.relatedTo("bigger", "request", "reply"));
		assertFalse(ontology.relatedTo("bigger", "request", "request"));
		assertFalse(ontology.relatedTo("bigger", "reply", "reply"));
		assertTrue(ontology.isIndividual("request"));
		assertTrue(ontology.isIndividual("reply"));
		ontology.removeIndividual("request");
		ontology.removeIndividual("reply");
		ontology.removeEntity("bigger", OWLObjectProperty.class);
//		assertFalse(ontology.isIndividual("request"));
//		assertFalse(ontology.isIndividual("reply"));
	}

}
