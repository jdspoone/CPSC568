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
public class InverseRelation extends BasedRelation {
	
	/**
	 * @param name
	 * @param owner TODO
	 * @throws IllegalOperationException 
	 */
	public InverseRelation(String name, Ontology ontology, ConcreteRelation base, boolean assignable, Constraint domConstraint, Constraint ranConstraint, Relation owner) throws IllegalOperationException {
		super(name, ontology, base, assignable, domConstraint, ranConstraint);
		//if (!(base instanceof PrimitiveRelation)) throw new IllegalOperationException("Inverse relations must be based directly on primitive relations");
		this.base = base;
//		this.owner = owner;
	}

	/* (non-Javadoc)
	 * @see casa.ontology.PrimitiveRelation#relatedTo(casa.ontology.Type, casa.ontology.Type)
	 */
	@Override
	protected Maplet relatedTo(Type domain, Type range, ConcreteRelation topOfStack) {
		return base.relatedTo(range, domain, topOfStack);
	}

	@Override
	public Set<Type> relatedTo(Type domain, ConcreteRelation topOfStack) {
		BaseType d = (BaseType)domain;
		Set<Type> ret = d.relatedFrom(base);
		Relation rel = base;
		while (ret==null && rel instanceof BasedRelation) {
			rel = ((BasedRelation)rel).base;
			ret = d.relatedFrom(rel);
		}
		return ret;
	}

  /* (non-Javadoc)
   * @see casa.ontology.Relation#add(casa.ontology.Type, casa.ontology.Type)
   */
  @Override
	public boolean add(Type subject, Type object)
  		throws IllegalOperationException {
		if (!(base instanceof PrimitiveRelation)) throw new IllegalOperationException("Relation "+getName()+" is not assignable");
  	return ((PrimitiveRelation)base).add(object, subject);
  }
    
  @Override
	public boolean remove(Type subject, Type object)
  		throws IllegalOperationException {
		if (!(base instanceof PrimitiveRelation)) throw new IllegalOperationException("Relation "+getName()+" is not assignable");
  	return ((PrimitiveRelation)base).remove(object, subject);
  }

	@Override
	public boolean hasProperty(Property property) {
		return property.equals(Property.INVERSE) ? true : base.hasProperty(property);
	}
	
	@Override
	public String toStringOptions() {
		return base.toStringOptions()+":inverse ";
	}

}
