/**
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. 
 * Permission to use, copy, modify, distribute and sell this software and its 
 * documentation for any purpose is hereby granted without fee, provided that the 
 * above copyright notice appear in all copies and that both that copyright notice 
 * and this permission notice appear in supporting documentation.  The  Knowledge 
 * Science Group makes no representations about the suitability of  this software 
 * for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
package casa.ontology.v3;

import casa.exceptions.IllegalOperationException;
import casa.ontology.Constraint;
import casa.ontology.Ontology;
import casa.ontology.Relation;
import casa.ontology.Type;

import java.util.Set;

/**
 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
public class PrimitiveRelation extends ConcreteRelation {
	
	protected Constraint domConstraint = null, ranConstraint = null;
	
	PrimitiveRelation(String name, Ontology ontology) throws IllegalOperationException {
		super(name, ontology);
	}

	PrimitiveRelation(String name, Ontology ontology, Constraint domainConstraint, Constraint rangeConstraint) throws IllegalOperationException {
		super(name, ontology);
		domConstraint = domainConstraint;
		ranConstraint = rangeConstraint;
	}

  @Override
	protected Maplet relatedTo(Type domain, Type range, ConcreteRelation topOfStack) {
		BaseType d = (BaseType)domain, r = (BaseType)range;
		if (d.relatedTo(this,r))
			return new Maplet(d,r);
		return null;
	}

	/* (non-Javadoc)
	 * @see casa.ontology.Relation#put(casa.ontology.Type, casa.ontology.Type)
	 */
	@Override
	public boolean add(Type domain, Type range) throws IllegalOperationException {
		if (domConstraint!=null && !domConstraint.validate(domain, domain, range))
			throw new IllegalOperationException("PrimitiveRelation "+getName()+": Domain violates type constraint: "+domConstraint+".");
		if (ranConstraint!=null && !ranConstraint.validate(range, domain, range))
			throw new IllegalOperationException("PrimitiveRelation "+getName()+": Range violates type constraint: "+ranConstraint+".");
		BaseType d = (BaseType)domain, r = (BaseType)range;
		return d.insertRelationTo(this, r);
	}

	/* (non-Javadoc)
	 * @see casa.ontology.Relation#remove(casa.ontology.Type, casa.ontology.Type)
	 */
	@Override
	public boolean remove(Type domain, Type range) throws IllegalOperationException {
	  BaseType d = (BaseType)domain, r = (BaseType)range;
		return d.removeRelationTo(this, r);
	}

	/* (non-Javadoc)
	 * @see casa.ontology.Relation#relatedTo(casa.ontology.Type)
	 */
	@Override
	public Set<Type> relatedTo(Type domain, ConcreteRelation topOfStack) {
		BaseType d = (BaseType)domain;
		Set<Type> ret=d.relatedTo(this);
		return ret;
	}
	
	
	@Override
	public boolean isAssignable() {
		return true;
	}

	@Override
	public boolean hasProperty(Property property) {
		return false;
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
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
	  StringBuffer b = new StringBuffer();
	  b.append("(declRelation \"")
	   .append(name)
	   .append("\" ")
	   .append(toStringOptions());
	  if (domConstraint!=null)
	    b.append(" :domain-constraint ")
	     .append(domConstraint.toString());
	  if (ranConstraint!=null)
	    b.append(" :range-constraint ")
	     .append(ranConstraint.toString());
	  b.append(')');
		return b.toString();
	}

	@Override
	public boolean isBasedOn(Relation other) {
		return false;
	}

	/**
	 * @return null
	 * @see casa.ontology.v3.ConcreteRelation#getUses()
	 */
	@Override
	public ConcreteRelation getUses() {
		return null;
	}

	/* (non-Javadoc)
	 * @see casa.ontology.v3.ConcreteRelation#toStringComment()
	 */
	@Override
	public String toStringComment(Ontology relativeToOntology) {
		// TODO Auto-generated method stub
		return name.getRelativeName(relativeToOntology)+"("+getClass().getSimpleName()+")";
	}

}
