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
import casa.ontology.Type;

import java.util.Set;


/**
 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
public class SymmetricRelation extends BasedRelation {
	
	/**
	 */
	InverseRelation inverseRelation;
	
	/**
	 * @param name
	 * @throws IllegalOperationException 
	 */
	public SymmetricRelation(String name, Ontology ontology, ConcreteRelation base, boolean assignable, Constraint domConstraint, Constraint ranConstraint) throws IllegalOperationException {
		super(name, ontology, base, assignable, domConstraint, ranConstraint);
		inverseRelation = new InverseRelation("inverseFor"+name, ontology, base, false, domConstraint, ranConstraint, this);
		inverseRelation.setVisible(false);
	}

	/* (non-Javadoc)
	 * @see casa.ontology.PrimitiveRelation#relatedTo(casa.ontology.Type, casa.ontology.Type)
	 */
	@Override
	protected Maplet relatedTo(Type subject, Type object, ConcreteRelation topOfStack) {
		Maplet forward = base.relatedTo(subject, object, topOfStack);
		Maplet backward = base.relatedTo(object, subject, topOfStack);
		if (forward!=null) return forward;
		if (backward!=null) return backward;
		return null;
	}

	@Override
	public Set<Type> relatedTo(Type domain, ConcreteRelation topOfStack) {
		Set<Type> ret = base.relatedTo(domain, topOfStack);
		//ret.addAll(base.relatedFrom(domain));
		//if (ret==null)
		//	return null;
		Set<Type> related = inverseRelation.relatedTo(domain, topOfStack);
		if (related!=null) {
			if (ret!=null) 
				ret.addAll(related);
			else
				ret = related;
		}
		return ret;
	}

	@Override
	public boolean hasProperty(Property property) {
		return property.equals(Property.SYMMETRIC) ? true : base.hasProperty(property);
	}
	
	@Override
	public String toStringOptions() {
		return base.toStringOptions()+":symmetric ";
	}

}
