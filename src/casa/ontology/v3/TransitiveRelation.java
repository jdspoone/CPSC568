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
import java.util.TreeSet;
import java.util.Vector;


/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class TransitiveRelation extends BasedRelation {
	
	/**
	 * @param name
	 * @param base
	 * @param assignable
	 * @throws IllegalOperationException 
	 */
	public TransitiveRelation(String name, Ontology ontology, ConcreteRelation base, boolean assignable, Constraint domConstraint, Constraint ranConstraint) throws IllegalOperationException {
		super(name, ontology, base, assignable, domConstraint, ranConstraint);
	}

	/* (non-Javadoc)
	 * @see casa.ontology.PrimitiveRelation#relatedTo(casa.ontology.Type, casa.ontology.Type)
	 */
	@Override
	protected Maplet relatedTo(Type subject, Type object, ConcreteRelation topOfStack) {
		Vector<Maplet> recursion = new Vector<Maplet>();
	  return relatedTo2(subject, object, recursion, topOfStack);
	}
	
	private Maplet relatedTo2(Type subject, Type object, Vector<Maplet> recursion, ConcreteRelation topOfStack) {
		Maplet ret = base.relatedTo(subject, object, topOfStack);
		if (ret!=null) return ret;
		Set<Type> related = base.relatedTo(subject, topOfStack);
		if (related!=null) {
			for (Type type: related) {
				Maplet pair = new Maplet((BaseType)type, (BaseType)object); 
				if (!recursion.contains(pair)) {
					recursion.add(pair);
				  if (relatedTo2(type, object, recursion, topOfStack)!=null) {
					  return new Maplet((BaseType)type, (BaseType)object);
				  }
				}
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see casa.ontology.Relation#relatedTo(casa.ontology.Type)
	 */
	@Override
	public Set<Type> relatedTo(Type subject, ConcreteRelation topOfStack) {
		Set<Type> ret = relatedTo2(subject, new TreeSet<Type>(), topOfStack); 
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see casa.ontology.Relation#relatedTo(casa.ontology.Type)
	 */
	private Set<Type> relatedTo2(Type subject, TreeSet<Type> recursion, ConcreteRelation topOfStack) {
//		System.out.println(name+".relatedTo("+subject.getName()+")");
		TreeSet<Type> ret = new TreeSet<Type>();
		Set<Type> related = base.relatedTo(subject, topOfStack);
		if (related!=null) {
			for (Type type: related) {
//				if (!ret.contains(type)) {
				if (!recursion.contains(type)) {
					recursion.add(type);
				  ret.add(type);
					ret.addAll(relatedTo2(type, recursion, topOfStack));
				}
			}
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see casa.ontology.Relation#hasProperty(casa.ontology.Relation.Property)
	 */
	@Override
	public boolean hasProperty(Property property) {
		return property.equals(Property.TRANSITIVE) ? true : base.hasProperty(property);
	}
	
	/* (non-Javadoc)
	 * @see casa.ontology.v3.BasedRelation#toStringOptions()
	 */
	@Override
	public String toStringOptions() {
		return base.toStringOptions()+":transitive ";
	}

}
