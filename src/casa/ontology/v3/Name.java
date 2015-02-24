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
package casa.ontology.v3;

import casa.exceptions.IllegalOperationException;
import casa.ontology.Ontology;

/**
 * A name object that contains both a name and the ontology it resides in.
 * This is intended to be used in the form [ontology-name]:[name], where 
 * the object named may be recognized with or with the ontology-name qualification. 
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
class Name implements Comparable<Name> { // NOTE: this class has scope limited to the package only
	private Ontology ontology;
	private String name;
	
	/**
	 * String for of the separator character that separates and ontology for the name proper.
	 */
	static final String SEPARATOR_STRING = "\\.";
	/**
	 * char for of the separator character that separates and ontology for the name proper.
	 */
	static final char SEPARATOR_CHAR = '.';
	
	/**
	 * A name for of the separator character that separates and ontology for the name proper.
	 */
	static final String SEPARATOR_NAME = "dot";
	
	Name(String name) throws IllegalOperationException {
		//name = name.toUpperCase();
		if (name==null || name.length()<=0) throw new IllegalOperationException("Ontology Type and Relation names must not be null and must not be emptly: "+name);
		int sepIndex = name.lastIndexOf(SEPARATOR_CHAR);
		if (sepIndex>=0) {
			if (sepIndex==0) {
				ontology = null;
			}
			else {
				String ontName = name.substring(0, sepIndex);
				ontology = CASAOntology.getOntology(ontName);
			  if (ontology==null) throw new IllegalOperationException("Ontology '"+ontName+"' doesn't exist for name '"+name+"'");
			}
		  if (sepIndex>=name.length())
		  	throw new IllegalOperationException("Ontology Type and Relation names must not be emptly: "+name);
		  this.name = name.substring(sepIndex+1);
		}
		else {
			this.name = name;
			ontology = null;
		}
	}

	Name(Ontology ontology, String name) throws IllegalOperationException {
		//name = name.toUpperCase();
		if (name==null || name.length()<=0) 
			throw new IllegalOperationException("Ontology Type and Relation names must not be null and must not be emptly: "+name);
		if (name.indexOf(SEPARATOR_CHAR) >= 0) 
			throw new IllegalOperationException("Ontology Type and Relation unqualified names may not contain "+SEPARATOR_NAME+"s: "+name);
  	this.name = name;
		this.ontology = ontology;
	}

	/**
	 * @return the ontology object that contains this object.
	 */
	public Ontology getOntology() {
		return ontology;
	}

	/**
	 * @return the name (without the ontology spec) part of this Name object.
	 * @see #toString()
	 */
	public String getName() {
		return name;
	}
	
	public String getRelativeName(Ontology ont) {
		if (ontology==ont)
			return getName();
		else
			return toString();
	}
	
	/**
	 * @return the fully qualified name of this Name object: [ontology-name].[name]. For example,
	 * "primitiveOntology.isa".
	 */
	public String toString() {
		return ontology==null ? name : ontology.getName()+SEPARATOR_CHAR+name;
	}

	@Override
	public int compareTo(Name o) {
		if (ontology==null && o.ontology!=null) return -1;
		if (ontology!=null && o.ontology==null) return 1;
		if (!ontology.getName().equals(o.ontology.getName())) return ontology.getName().compareToIgnoreCase(o.ontology.getName());
		return name.compareTo(o.name);
	}

}
