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
import casa.ontology.OntologyEntity;
import casa.ontology.Relation;
import casa.ontology.Type;

import java.util.Set;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public abstract class ConcreteRelation implements Relation {

	protected Name name;
	private boolean mark = false;
	boolean visible=false;

	ConcreteRelation(String name, Ontology ontology) throws IllegalOperationException {
		this.name = new Name(ontology, name);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(OntologyEntity o) {
		// TODO Auto-generated method stub
		return getName().compareTo(o.getName());
	}

	/* (non-Javadoc)
	 * @see casa.ontology.Relation#getName()
	 */
	/**
	 * @return
	 */
	@Override
	public String getName() {
		return name.toString();
	}

	/* (non-Javadoc)
	 * @see casa.ontology.OntologyEntity#getOntology()
	 */
	@Override
	public Ontology getOntology() {
		return name.getOntology();
	}
	
	public void setName(String name) throws IllegalOperationException {
		if (visible)
			throw new IllegalOperationException("Cannot reset the name of visible relation "+this.name+" to "+name+" because it is already visible.");
		this.name = new Name(this.name.getOntology(), name);
	}
	
	public abstract boolean isBasedOn(Relation other);
	
	public String getRelativeName(Ontology ont) {
		return name.getRelativeName(ont);
	}
	
	/**
	 * Determines if <em>domain</em> is related to <em>range</em> through this 
	 * Relation.  This is done by calling {@link #relatedToX(Type, Type)} and returning
	 * FALSE if it returns null, and TRUE otherwise. 
	 * @param domain
	 * @param range
	 * @return true iff <em>domain</em> is related to <em>range</em> through this 
	 * Relation. 
	 * @see casa.ontology.Relation#relatedTo(casa.ontology.Type, casa.ontology.Type)
	 */
	@Override
	public final boolean relatedTo(Type domain, Type range) {
		return relatedToX(domain, range)==null?false:true;
	}
	
	/**
	 * Determines if <em>domain</em> is related to <em>range</em> through this 
	 * Relation.  This is done by calling {@link #relatedTo(Type, Type)} and returning
	 * FALSE if it returns null, and TRUE otherwise. 
	 * @param domain
	 * @param range
	 * @return the found most-specific Maplet if <em>domain</em> is related to <em>range</em> through this 
	 * Relation, null otherwise.
	 */
	public final Maplet relatedToX(Type domain, Type range) {
		return relatedTo(domain, range, this);
	}
	
	protected abstract Maplet relatedTo(Type domain, Type range, ConcreteRelation topOfStack);


	/* (non-Javadoc)
	 * @see casa.ontology.Relation#relatedTo(casa.ontology.Type)
	 */
	@Override
	public final Set<Type> relatedTo(Type domain) {
		// TODO Auto-generated method stub
		return relatedTo(domain, this);
	}

	public abstract Set<Type> relatedTo(Type domain, ConcreteRelation topOfStack);

	/**
	 * @return
	 */
	@Override
	public boolean isMark() {
		return mark;
	}

	/**
	 * @param mark
	 */
	@Override
	public void setMark(boolean mark) {
		this.mark = mark;
	}
	
	/* (non-Javadoc)
	 * @see casa.ontology.Relation#hasProperty(casa.ontology.Relation.Property)
	 */
	@Override
	public boolean hasProperty(Property property) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see casa.ontology.Relation#isVisible()
	 */
	@Override
	public boolean isVisible() {
		return visible;
	}
	
	/**
	 * @param set
	 * @return
	 */
	@Override
	public boolean setVisible(boolean set) {
		boolean ret = visible;
		visible = set;
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see casa.ontology.Relation#toStringOptions()
	 */
	public String toStringOptions() {
		return "";
	}
	
	/**
	 * @return the Relation that this relation uses as its internal "equivalence" relation.
	 */
	public abstract ConcreteRelation getUses();
	
	/**
	 * A comment string appropriate for the relation (not inclding the prefix ";").
	 * Specifically the this should be a list in the form:<pre>
	 * <em>name</em>(<em>class-name</em>), <em>name</em>(<em>class-name</em>)...</pre>
	 * describing the base and ordered decorators for this relation.
	 * @param relativeToOntology TODO
	 * @return A string as described above. 
	 */
	public abstract String toStringComment(Ontology relativeToOntology);
	
}
