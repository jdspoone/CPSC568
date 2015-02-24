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
import casa.ontology.Constraint;
import casa.ontology.Ontology;
import casa.ontology.Type;

import java.util.Set;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class MirrorRelation extends BasedRelation {

	/**
	 * A relation that just mirrors it's base and nothing more except check constraints.
	 * @param name
	 * @param ontology
	 * @param base
	 * @param assignable
	 * @param domainConstraint
	 * @param rangeConstraint
	 * @throws IllegalOperationException
	 */
	public MirrorRelation(String name, Ontology ontology, ConcreteRelation base,
			boolean assignable, Constraint domainConstraint,
			Constraint rangeConstraint) throws IllegalOperationException {
		super(name, ontology, base, assignable, domainConstraint, rangeConstraint);
		assert base!=null;
	}

	/* (non-Javadoc)
	 * @see casa.ontology.Relation#relatedTo(casa.ontology.Type, casa.ontology.Type)
	 */
	@Override
	protected Maplet relatedTo(Type domain, Type range, ConcreteRelation topOfStack) {
		if (!domConstraint.validate(domain, domain, range)) return null;
		if (!ranConstraint.validate(range, domain, range)) return null;
		return base.relatedTo(domain, range, topOfStack);
	}

	/* (non-Javadoc)
	 * @see casa.ontology.Relation#relatedTo(casa.ontology.Type)
	 */
	@Override
	public Set<Type> relatedTo(Type domain, ConcreteRelation topOfStack) {
		if (!domConstraint.validate(domain, domain)) return null;
		return base.relatedTo(domain, topOfStack);
	}

	/* (non-Javadoc)
	 * @see casa.ontology.Relation#hasProperty(casa.ontology.Relation.Property)
	 */
	@Override
	public boolean hasProperty(Property property) {
		return base.hasProperty(property);
	}

}
