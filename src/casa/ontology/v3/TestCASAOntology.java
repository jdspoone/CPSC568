/**
      " * <p>Title: CASA Agent Infrastructure</p>
      " * <p>Description: </p>
      " * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. 
      " * Permission to use, copy, modify, distribute and sell this software and its
      " * documentation for any purpose is hereby granted without fee, provided that
      " * the above copyright notice appear in all copies and that both that copyright
      " * notice and this permission notice appear in supporting documentation.  
      " * The  Knowledge Science Group makes no representations about the suitability
      " * of  this software for any purpose.  It is provided "as is" without express
      " * or implied warranty.</p>
      " * <p>Company: Knowledge Science Group, University of Calgary</p>
      " * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
      " * @version 0.9
      " */
package casa.ontology.v3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import casa.Status;
import casa.StatusObject;
import casa.exceptions.IllegalOperationException;
import casa.ontology.Ontology;
import casa.ontology.Relation;
import casa.ontology.Type;

import java.util.Set;

import org.armedbear.lisp.Lisp;
import org.armedbear.lisp.Symbol;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
      " * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
      " *
      " */
public class TestCASAOntology {

	static private CASAOntology primitiveOntology;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		primitiveOntology = CASAOntology.getOntology(CASAOntology.PRIMITIVE_ONTOLOGY);
		assertNotNull("Can't find ontology: "+CASAOntology.PRIMITIVE_ONTOLOGY, primitiveOntology);
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

	/**
	 * Test method for {@link casa.ontology.v3.CASAOntology#getName()}.
	 */
	@Test
	public final void testGetName() {
		assertTrue(CASAOntology.PRIMITIVE_ONTOLOGY.equals(primitiveOntology.getName()));
	}

	/**
	 * Test method for {@link casa.ontology.v3.CASAOntology#getOntology(java.lang.String)}.
	 */
	@Test
	public final void testGetOntology() {
		assertEquals(CASAOntology.primitiveOntology, CASAOntology.getOntology(CASAOntology.PRIMITIVE_ONTOLOGY));
	}

	/**
	 * Test method for {@link casa.ontology.v3.CASAOntology#getRelation(java.lang.String)}.
	 */
	@Test
	public final void testGetRelation() {
		try {
			assertNotNull(primitiveOntology.getRelation(CASAOntology.ISAPARENT));
			assertNotNull(primitiveOntology.getRelation(CASAOntology.ISAANCESTOR));
			assertNotNull(primitiveOntology.getRelation(CASAOntology.ISA));
			assertNotNull(primitiveOntology.getRelation(CASAOntology.ISACHILD));
			assertNotNull(primitiveOntology.getRelation(CASAOntology.ISADESCENDANT));
			assertNotNull(primitiveOntology.getRelation(CASAOntology.ISEQUAL));
			assertNotNull(primitiveOntology.getRelation(CASAOntology.PROPERINSTANCEOF));
			assertNotNull(primitiveOntology.getRelation(CASAOntology.INSTANCEOF));
		} catch (IllegalOperationException e) {
			fail(e.toString());
		}
	}

	/**
	 * Test method for {@link casa.ontology.v3.CASAOntology#getType(java.lang.String)}.
	 */
	@Test
	public final void testGetType() {
		try {
			assertNotNull(primitiveOntology.getType(CASAOntology.TOP));
		} catch (IllegalOperationException e) {
			fail(e.toString());
		}
	}

	/**
	 * Test method for {@link casa.ontology.v3.CASAOntology#isa(java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testIsa() {
		try {
			assertTrue(primitiveOntology.isa(CASAOntology.TOP, CASAOntology.TOP));
		} catch (IllegalOperationException e) {
			fail(e.toString());
		}
	}

	/**
	 * Test method for {@link casa.ontology.v3.CASAOntology#relatedTo(casa.ontology.Relation, String, String)}.
	 */
	@Test
	public final void testRelatedToRelationTypeType() {
		try {
			assertTrue(primitiveOntology.relatedTo(primitiveOntology.getRelation(CASAOntology.ISA), primitiveOntology.getType(CASAOntology.TOP), primitiveOntology.getType(CASAOntology.TOP)));
		} catch (IllegalOperationException e) {
			fail(e.toString());
		}
	}

	/**
	 * Test method for {@link casa.ontology.v3.CASAOntology#relatedTo(casa.ontology.Relation, String, String)}.
	 */
	@Test
	public final void testRelatedToRelationStringString() {
		try {
			assertTrue(primitiveOntology.relatedTo(primitiveOntology.getRelation(CASAOntology.ISA), CASAOntology.TOP, CASAOntology.TOP));
		} catch (IllegalOperationException e) {
			fail(e.toString());
		}
	}

	/**
	 * Test method for {@link casa.ontology.v3.CASAOntology#relatedTo(java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testRelatedToStringStringString() {
		try {
			assertTrue(primitiveOntology.relatedTo(CASAOntology.ISA, CASAOntology.TOP, CASAOntology.TOP));
		} catch (IllegalOperationException e) {
			fail(e.toString());
		}
	}

	public CASAOntology bioOnt() throws IllegalOperationException {
		CASAOntology myOnt=null;
		myOnt = CASAOntology.getOntology("bioOnt");
		if (myOnt==null) {
			assertNotNull(myOnt = CASAOntology.makeOntology2("bioOnt"));
			myOnt.addType("biological", CASAOntology.TOP);
			myOnt.addType("animal", "biological");
			myOnt.addType("plant", "biological");
			myOnt.addType("mammal", "animal");
			myOnt.addType("cat", "mammal");
			myOnt.addType("dog", "mammal");
			myOnt.addType("goldenRetriever", "dog");
			myOnt.addType("boxer", "dog");
			myOnt.addType("chihuahua", "dog");
			myOnt.addType("siamese", "cat");
			myOnt.addType("lion", "cat");
			myOnt.declType("k9");
			myOnt.declMaplet(CASAOntology.ISEQUAL, "k9", "dog");
			myOnt.declType("doggy");
			myOnt.declMaplet(CASAOntology.ISEQUAL, "k9", "doggy");
		}
		return myOnt;
	}

	/**
	 * Test method for loading the little bio ontology.
	 */
	@Test
	public final void testIsaLoadBioOntology()  throws IllegalOperationException {
		Ontology myOnt = bioOnt();
		assertNotNull(myOnt);
	}

	/**
	 * Test method for loading the little size ontology, which is sub-ontology of the bio ontology.
	 */
	@Test
	public final void testIsaLoadSizeOntology()  throws IllegalOperationException {
		Ontology myOnt = sizeOnt();
		assertNotNull(myOnt);
	}

	/**
	 * Test method for general tests on a isa hierarchy about biology.
	 */
	@Test
	public final void testIsaHierarchy()  throws IllegalOperationException {
		Ontology myOnt = bioOnt();
		assertTrue(myOnt.isa("goldenRetriever", "dog"));
		assertTrue(myOnt.isa("goldenRetriever", "mammal"));
		assertTrue(myOnt.isa("goldenRetriever", "animal"));
		assertTrue(myOnt.isa("goldenRetriever", "biological"));
		assertTrue(myOnt.isa("goldenRetriever", CASAOntology.TOP));
		assertFalse(myOnt.isa("goldenRetriever", "cat"));
		assertFalse(myOnt.isa("goldenRetriever", "plant"));
		assertFalse(myOnt.isa("dog", "goldenRetriever"));
		assertFalse(myOnt.isa("mammal", "goldenRetriever"));
	}

	/**
	 * Test method for general tests on isEqual on the hierarchy about biology.
	 */
	@Test
	public final void testIsEqual()  throws IllegalOperationException {
		Ontology myOnt = bioOnt();
		assertTrue(myOnt.relatedTo(CASAOntology.ISEQUAL, "k9", "dog"));
		assertTrue(myOnt.relatedTo(CASAOntology.ISEQUAL, "dog", "k9"));
		assertTrue(myOnt.relatedTo(CASAOntology.ISEQUAL, "doggy", "dog"));
		assertTrue(myOnt.relatedTo(CASAOntology.ISEQUAL, "dog", "dog"));
		assertTrue(myOnt.relatedTo(CASAOntology.ISEQUAL, "doggy", "doggy"));
		assertFalse(myOnt.relatedTo(CASAOntology.ISEQUAL, "dog", "cat"));
		assertFalse(myOnt.relatedTo(CASAOntology.ISEQUAL, "k9", "cat"));
	}

	/**
	 * Test method for general tests on a isa with isEqual on the hierarchy about biology.
	 */
	@Test
	public final void testIsEqualOverIsa()  throws IllegalOperationException {
		Ontology myOnt = bioOnt();
		assertTrue(myOnt.isa("k9", "mammal"));
		assertTrue(myOnt.isa("goldenRetriever", "k9"));
		assertTrue(myOnt.isa("goldenRetriever", "doggy"));
		assertFalse(myOnt.isa("goldenRetriever", "cat"));
	}

	/**
	 * Test method for general tests on a type hierarchy about biology on the relatedTo(Relation, String) method.
	 */
	@Test
	public final void testRelatedToString()  throws IllegalOperationException {
		CASAOntology myOnt = bioOnt();
		Set<String> set = myOnt.relatedTo(CASAOntology.ISEQUAL, "dog");
		assertEquals(3, set.size());
	}

	public Ontology sizeOnt() throws IllegalOperationException {
		CASAOntology myOnt=null;
		myOnt = CASAOntology.getOntology("sizeOnt");
		if (myOnt==null) {
			if (CASAOntology.getOntology("bioOnt")==null) bioOnt();
			assertNotNull(myOnt = CASAOntology.makeOntology2("sizeOnt", "bioOnt"));
			myOnt.declRelation("bigger", null, new Relation.Property[]{Relation.Property.TRANSITIVE, Relation.Property.ASYMMETRIC, Relation.Property.USES}, null, null, CASAOntology.isa);
			myOnt.declMaplet("bigger", "dog", "cat");
			myOnt.declMaplet("bigger", "goldenRetriever", "boxer");
			myOnt.declMaplet("bigger", "lion", "dog");
			myOnt.declMaplet("bigger", "cat", "chihuahua");
		}
		return myOnt;
	}

	/**
	 * Test method for general tests on the size ontology with the bigger relation.
	 */
	@Test//(timeout=10000)
	public final void testBiggerRelation() throws IllegalOperationException {
		Ontology myOnt = sizeOnt();
		System.out.println(CASAOntology.getOntology("bioOnt"));
		System.out.println(CASAOntology.getOntology("sizeOnt"));
		assertTrue(myOnt.relatedTo("bigger", "dog", "cat"));
		assertFalse(myOnt.relatedTo("bigger", "cat", "dog"));
		assertTrue(myOnt.relatedTo("bigger", "goldenRetriever", "boxer"));
		assertTrue(myOnt.relatedTo("bigger", "goldenRetriever", "cat"));
		assertTrue(myOnt.relatedTo("bigger", "goldenRetriever", "siamese"));
		assertTrue(myOnt.relatedTo("bigger", "lion", "dog"));
		assertTrue(myOnt.relatedTo("bigger", "lion", "goldenRetriever"));
		assertFalse(myOnt.relatedTo("bigger", "animal", "mammal"));
		assertFalse("Asymmeric test", myOnt.relatedTo("bigger", "dog", "lion"));
		assertTrue(myOnt.relatedTo("bigger", "cat", "chihuahua"));
		assertFalse(myOnt.relatedTo("bigger", "chihuahua", "siamese"));
		assertTrue(myOnt.relatedTo("bigger", "siamese", "chihuahua"));
		assertFalse(myOnt.relatedTo("bigger", "chihuahua", "dog"));
	  //interestingly, (bigger dog chihuahua) is true even though chihuahua<:dog 
		//because (bigger dog cat) and (bigger cat chihuahua) and bigger is transitive 
		//It's saying "a chihuahua is smaller than the average (typical) dog."
		assertTrue(myOnt.relatedTo("bigger", "dog", "chihuahua"));
	}
	
	/* *********************************************************************
	 * Lisp tests
	 ***********************************************************************/
	
	/**
	 * Test method for loading the little bio ontology.
	 */
	@Test
	public final void testIsaLoadBioOntologyLisp()  throws IllegalOperationException {
		casa.abcl.Lisp.abclEval(null, null, null, 
			"(declOntology \"bioOntLisp\" \n" +
      "  '() ;super ontologies (the search path is bioOnt, primitiveOntology) \n" +
      "  '( ;ontology declarations \n" +
      "    ; No visible relations defined (of 0 total relations). \n" +
      "    ; 12 types defined: \n" +
      "    (declType \"animal\") \n" +
      "    (declType \"biological\") \n" +
      "    (declType \"boxer\") \n" +
      "    (declType \"cat\") \n" +
      "    (declType \"dog\") \n" +
      "    (declType \"doggy\") \n" +
      "    (declType \"goldenRetriever\") \n" +
      "    (declType \"k9\") \n" +
      "    (declType \"lion\") \n" +
      "    (declType \"mammal\") \n" +
      "    (declType \"plant\") \n" +
      "    (declType \"siamese\") \n" +
      "    ; 10 relational maplets defined: \n" +
      "    (declMaplet primitiveOntology.isa-parent biological primitiveOntology.TOP) \n" +
      "    (declMaplet primitiveOntology.isa-parent plant biological) \n" +
      "    (declMaplet primitiveOntology.isa-parent animal biological) \n" +
      "    (declMaplet primitiveOntology.isa-parent mammal animal) \n" +
      "    (declMaplet primitiveOntology.isa-parent cat mammal) \n" +
      "    (declMaplet primitiveOntology.isa-parent dog mammal) \n" +
      "    (declMaplet primitiveOntology.isa-parent goldenRetriever dog) \n" +
      "    (declMaplet primitiveOntology.isa-parent lion cat) \n" +
      "    (declMaplet primitiveOntology.isa-parent siamese cat) \n" +
      "    (declMaplet primitiveOntology.isa-parent boxer dog) \n" +
      "    ; No objects in superontologies have relations in this ontology. \n" +
      "  ) \n" +
      ") \n"
      , null);
  		casa.abcl.Lisp.abclEval(null, null, null, 
      "(declOntology \"sizeOntLisp\" \n" +
      "  '(bioOntLisp) ;super ontologies (the search path is sizeOnt, bioOnt, primitiveOntology) \n" +
      "  '( ;ontology declarations \n" +
      "    ; 1 visible relations defined (of 1 total relations). \n" +
      "    (declRelation \"bigger\" :uses \"isa\" :transitive :asymmetric :assignable) \n" +
      "    ; No types defined. \n" +
      "    ; No relational maplets defined. \n" +
      "    ; 3 objects in superontologies have relations in this ontology. \n" +
      "    (declMaplet bigger dog cat) \n" +
      "    (declMaplet bigger goldenRetriever boxer) \n" +
      "    (declMaplet bigger lion dog) \n" +
      "  ) \n" +
      ") "
			, null);
	}

	/**
	 * Test method for general tests on the size ontology with the bigger relation with Lisp.
	 */
	@Test//(timeout=10000)
	public final void testBiggerRelationLisp() throws IllegalOperationException {
//		CASAOntology myOnt = sizeOnt(); // this being absent depends on the above test being first.
		System.out.println(CASAOntology.getOntology("bioOntLisp"));
		System.out.println(CASAOntology.getOntology("sizeOntLisp"));
		assertTrue(checkBooleanRelLisp("bigger dog cat"));
		assertFalse(checkBooleanRelLisp("bigger cat dog"));
		assertTrue(checkBooleanRelLisp("bigger goldenRetriever boxer"));
		assertTrue(checkBooleanRelLisp("bigger goldenRetriever cat"));
		assertTrue(checkBooleanRelLisp("bigger goldenRetriever siamese"));
		assertTrue(checkBooleanRelLisp("bigger lion dog"));
		assertFalse("Asymmetry test", checkBooleanRelLisp("bigger dog lion"));
		assertTrue(checkBooleanRelLisp("bigger lion goldenRetriever"));
		assertFalse(checkBooleanRelLisp("bigger animal mammal"));
		}
	
	private boolean checkBooleanRelLisp(String s) {
		Status stat = casa.abcl.Lisp.abclEval(null, null, null, "("+s+" :ontology \"sizeOntLisp\")", null); 
		assertTrue(stat instanceof StatusObject);
		Object obj = ((StatusObject<?>)stat).getObject();
		assertTrue(obj instanceof Symbol);
		return ((Symbol)obj)!=Lisp.NIL;
	}


}
