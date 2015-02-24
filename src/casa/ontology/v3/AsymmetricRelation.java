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
public class AsymmetricRelation extends BasedRelation {
	
	/**
	 */
	InverseRelation inverseRelation;
	
	/**
	 * @param name
	 * @throws IllegalOperationException 
	 */
	public AsymmetricRelation(String name, Ontology ontology, ConcreteRelation base, boolean assignable, Constraint domConstraint, Constraint ranConstraint) throws IllegalOperationException {
		super(name, ontology, base, assignable, domConstraint, ranConstraint);
		inverseRelation = new InverseRelation("inverseFor"+name, ontology, base, false, domConstraint, ranConstraint, this);
		inverseRelation.setVisible(false);
	}

	/**
	 * If this relation is holds in both forward and backward direction then 
	 * we must disambiguate using the USES relation if there is one. It's possible
	 * that the USES relation can't disambiguate, and then there must be an
	 * error, which case we return null (if our base says 
	 * @see casa.ontology.v3.ConcreteRelation#relatedTo(casa.ontology.Type, casa.ontology.Type, casa.ontology.v3.ConcreteRelation)
	 */
	@Override
	protected Maplet relatedTo(Type subject, Type object, ConcreteRelation topOfStack) {
		Maplet forward = base.relatedTo(subject, object, topOfStack);
		if (forward==null) return null; // for efficiency

		//avoid recursing indefinitely in checking the reverse relation
		Maplet backward;
//		if (topOfStack == this) {
			backward = base.relatedTo(object, subject, topOfStack);
//		}
//		else {
//			backward = topOfStack.relatedTo(object, subject, this);
//		}
		if (backward==null) return forward;
		
		backward = backward.getInverted();

		ConcreteRelation uses = topOfStack.getUses(); 
		if (uses!=null) {
			try {
				int comp = forward.compare(uses, backward);
				if (comp<=0) return forward;
			} catch (IllegalOperationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	

	@Override
	public Set<Type> relatedTo(Type domain, ConcreteRelation topOfStack) {
		Set<Type> ret = base.relatedTo(domain, topOfStack);
		Set<Type> backRelations = inverseRelation.relatedTo(domain, topOfStack);
		if (ret!=null && backRelations!=null) 
			ret.removeAll(backRelations);
		return ret;
	}

	@Override
	public boolean hasProperty(Property property) {
		return property.equals(Property.ASYMMETRIC) ? true : base.hasProperty(property);
	}
	
	@Override
	public String toStringOptions() {
		return base.toStringOptions()+":asymmetric ";
	}

}
