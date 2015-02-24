package casa.ontology.v3;

import casa.exceptions.IllegalOperationException;
import casa.ontology.Ontology;
import casa.ontology.Relation;
import casa.util.CASAUtil;

import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

/**
 * Helper class for Ontology; not to be used by any other class other than Ontolgy. Stores the names and links them to another via Vectors of parent and child links. <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p> <p>Company: Knowledge Science Group</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
public class SimpleType extends BaseType {
	
	/**
	 * Constructor: makes a new node in the hierarchy with name <em>typeName</em>,
	 * attaching the pointers to and from the nodes in <em>parents</em>.
	 * @param typeName the name of the type
	 * @param parents must be a Vector of Strings
	 * @param ontology the ontology to which this belongs
	 */
	protected SimpleType(String typeName, Vector<BaseType> parents, CASAOntology ontology) throws IllegalOperationException {
		this(typeName, ontology);
		assert ontology!=null;
		if (parents!=null) {
			Relation isA = ontology.getRelation("isa");
			insertRelationTo(isA, parents);
		}
	}
	
	public void setIndividual() {
		individual = true;
	}
	
	/**
	 * Constructor: makes a new Type with name <em>typeName</em>.
	 * @param typeName the name of the type
	 * @param ontology the ontology to which this belongs
	 * @throws IllegalOperationException 
	 */
	protected SimpleType(String typeName, Ontology ontology) throws IllegalOperationException {
		super(typeName, ontology);
	}
	
	private boolean individual = false;

	@Override
	public boolean isIndividual() {
		return individual;
	}
	
}
