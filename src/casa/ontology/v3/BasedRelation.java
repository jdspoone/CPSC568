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

/**
 * Describes are relation based (or backed by) another relation.  Generally,  these types of relations constitute a decorator pattern.
 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
public abstract class BasedRelation extends ConcreteRelation {

	/**
	 */
	ConcreteRelation base;
	boolean assignable;

  Constraint domConstraint = null, ranConstraint = null;
	/**
	 * @param name
	 * @param base
	 * @param assignable
	 * @throws IllegalOperationException 
	 */
	public BasedRelation(String name, Ontology ontology, ConcreteRelation base, boolean assignable, Constraint domainConstraint, Constraint rangeConstraint) throws IllegalOperationException {
		super(name, ontology);
		this.base = base;
		this.assignable = assignable;
		domConstraint = domainConstraint;
		ranConstraint = rangeConstraint;
	}
	
	@Override
	public boolean isBasedOn(Relation other) {
		if (base==null) return false;
		if (base==other) return true;
		if (base instanceof BasedRelation) return ((BasedRelation)base).isBasedOn(other);
		return false;
	}

  /* (non-Javadoc)
   * @see casa.ontology.Relation#add(casa.ontology.Type, casa.ontology.Type)
   */
  @Override
	public boolean add(Type domain, Type range)
  		throws IllegalOperationException {
		if (domConstraint!=null && !domConstraint.validate(domain, domain, range))
			throw new IllegalOperationException("PrimitiveRelation "+getName()+": Domain violates type constraint: "+domConstraint+".");
		if (ranConstraint!=null && !ranConstraint.validate(range, domain, range))
			throw new IllegalOperationException("PrimitiveRelation "+getName()+": Range violates type constraint: "+ranConstraint+".");
  	return base.add(domain, range);
  }
  
  /* (non-Javadoc)
   * @see casa.ontology.Relation#remove(casa.ontology.Type, casa.ontology.Type)
   */
  @Override
	public boolean remove(Type subject, Type object)
  		throws IllegalOperationException {
  	return base.remove(subject, object);
  }

  /* (non-Javadoc)
   * @see casa.ontology.Relation#relatedFrom(casa.ontology.Type)
   */
//  public Set<Type> relatedFrom(Type range) {
//    return base.relatedFrom(range);
//  }


  //	public Set<Type> relatedTo(Type subject) {
//		return base.relatedTo(subject);
//	}
//
	/**
	 * @return the name of the <em>visible</em> relation that this relation is based on (backed by).
	 */
	protected String getBaseName() {
		return (base instanceof BasedRelation) 
		               ? (base.isVisible() ? base.getName() : ((BasedRelation)base).getBaseName()) 
		               : base.getName();
	}
	
	/* (non-Javadoc)
	 * @see casa.ontology.Relation#isAssignable()
	 */
	/**
	 * @return
	 */
	@Override
	public boolean isAssignable() {
		return assignable;
	}

	/* (non-Javadoc)
	 * @see casa.ontology.Relation#setVisible(boolean)
	 */
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
	   .append("\" ");
	  if (getBaseName().charAt(0)!='.')
	  	b.append(":base ")
	     .append(getBaseName())
	     .append(' ');
	  b.append(toStringOptions());
	  if (assignable) b.append(":assignable");
	  if (domConstraint!=null)
	    b.append(" :domain-constraint ")
	     .append(domConstraint.toString());
	  if (ranConstraint!=null)
	  	b.append(" :range-constraint ")
	     .append(ranConstraint.toString());
	  b.append(')');
		return b.toString();
	}

	/**
	 * @return the result of {@link #base}.{@link ConcreteRelation#getUses()}
	 * @see casa.ontology.v3.ConcreteRelation#getUses()
	 */
	@Override
	public ConcreteRelation getUses() {
		return base.getUses();
	}	

	/* (non-Javadoc)
	 * @see casa.ontology.v3.ConcreteRelation#toStringComment()
	 */
	@Override
	public String toStringComment(Ontology relativeToOntology) {
		return name.getRelativeName(relativeToOntology)+"("+getClass().getSimpleName()+")"
				   +", "+base.toStringComment(relativeToOntology);
	}

}
