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
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class ReflexiveRelation extends BasedRelation {
	
	/**
	 * @param name
	 * @throws IllegalOperationException 
	 */
	public ReflexiveRelation(String name, Ontology ontology, ConcreteRelation base, boolean assignable, Constraint domConstraint, Constraint ranConstraint) throws IllegalOperationException {
		super(name, ontology, base, assignable, domConstraint, ranConstraint);
	}

	/* (non-Javadoc)
	 * @see casa.ontology.PrimitiveRelation#relatedTo(casa.ontology.Type, casa.ontology.Type)
	 */
	@Override
	protected Maplet relatedTo(Type subject, Type object, ConcreteRelation topOfStack) {
		return subject.equals(object) ? new Maplet((BaseType)subject, (BaseType)object) : base.relatedTo(subject, object, topOfStack);
	}

	@Override
	public Set<Type> relatedTo(Type domain, ConcreteRelation topOfStack) {
		Set<Type> ret = base.relatedTo(domain, topOfStack);
		ret.add(domain);
		return ret;
	}

	@Override
	public boolean hasProperty(Property property) {
		return property.equals(Property.REFLEXIVE) ? true : base.hasProperty(property);
	}
	
	@Override
	public String toStringOptions() {
		return base.toStringOptions()+":reflexive ";
	}

}
