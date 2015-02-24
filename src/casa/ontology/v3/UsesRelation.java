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
import casa.util.Pair;
import casa.util.PairComparable;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
public class UsesRelation extends BasedRelation {
	
	ConcreteRelation uses = null;
	
	/**
	 * @param name
	 * @param owner TODO
	 * @throws IllegalOperationException 
	 */
	public UsesRelation(String name, Ontology ontology, ConcreteRelation base, ConcreteRelation uses, boolean assignable, Constraint domConstraint, Constraint ranConstraint, Relation owner) throws IllegalOperationException {
		super(name/*+"("+uses.getName().replace(':', '-')+")"*/, ontology, base, assignable, domConstraint, ranConstraint);
		//if (!(base instanceof PrimitiveRelation)) throw new IllegalOperationException("Inverse relations must be based directly on primitive relations");
		this.base = base;
		this.uses = uses;
		assert this.uses!=null;
		assert this.base!=null;
//		this.owner = owner;
	}

	/* (non-Javadoc)
	 * @see casa.ontology.PrimitiveRelation#relatedTo(casa.ontology.Type, casa.ontology.Type)
	 */
	@Override
	protected Maplet relatedTo(Type domain, Type range, ConcreteRelation topOfStack) {
		BaseType d = (BaseType)domain, r = (BaseType)range;
//		if (base.relatedTo(d, r, topOfStack))
//			return true;
		
		//deal with equality of terms
		
		//collect the domain and range related terms
		assert uses!=null;
		Set<Type> d_equalSet = uses.relatedTo(d);
		Set<Type> r_equalSet = uses.relatedTo(r);
		if (d_equalSet==null || d_equalSet.isEmpty()) {
			d_equalSet = new TreeSet<Type>();
			d_equalSet.add(domain);
		}
		if (r_equalSet==null || r_equalSet.isEmpty()) {
			r_equalSet = new TreeSet<Type>();
			r_equalSet.add(range);
		}
		
		//cross the domain and range sets in to a list of ordered pairs sorted by domain and range (in that order)
		class Comp implements Comparator<Pair<BaseType,BaseType>> {
			@Override
			public int compare(Pair<BaseType, BaseType> o1, Pair<BaseType, BaseType> o2) {
				if (o1.getFirst().compareTo(o2.getFirst())!=0) {
					return uses.relatedTo(o1.getFirst(), o2.getFirst())?-1:1;
				}
				else { //"firsts" are equal
					if (o1.getSecond().compareTo(o2.getSecond())==0)
						return 0;
					return uses.relatedTo(o1.getSecond(), o2.getSecond())?-1:1;
					}
				}
			}
			
		@SuppressWarnings("serial")
		TreeSet<Maplet> pairs = new TreeSet<Maplet>(new Comp()) {
			@Override
			public String toString() {
				StringBuffer b = new StringBuffer();
				b.append("{["+uses.getName()+"]\n");
				for (Pair<BaseType,BaseType> p: this) {
					b.append(p.getFirst().getName()).append(" -> ").append(p.getSecond().getName()).append("\n");
				}
				b.append("}");
				return b.toString();
			}
		};
		for (Type dtype: d_equalSet) {
			d = (BaseType)dtype;
			for (Type rtype: r_equalSet) {
				r = (BaseType)rtype;
				pairs.add(new Maplet(d,r));
			}
		}
//		System.out.println(pairs.toString());
		
// new method
		//TODO The pairs method could be improved upon by filtering...
		for (Maplet p: pairs) {
			if (topOfStack == this) {
				if (base.relatedTo(p.getFirst(), p.getSecond(), topOfStack)!=null) return p;
			}
			else {
				if (topOfStack.relatedTo(p.getFirst(), p.getSecond(), this)!=null) return p;
			}
		}

// old method
//		for (Type dtype: d_equalSet) {
//			d = (BaseType)dtype;
//			for (Type rtype: r_equalSet) {
//				r = (BaseType)rtype;
//				if (d.relatedTo(topOfStack==this?base:topOfStack, r))
//					return true;
//				// evaluate the equal set from topOfStack, but make sure we don't recurse indefinitely
//				if (topOfStack == this) {
//				  if (base.relatedTo(d, r, topOfStack)) return true;
//				}
//				else {
//				  if (topOfStack.relatedTo(d, r, this)) return true;
//				}
//			}
//		}
		return null;
	}

	@Override
	public Set<Type> relatedTo(Type domain, ConcreteRelation topOfStack) {
		Set<Type> ret = new TreeSet<Type>();
		Set<Type> eqSet = ((BaseType)domain).relatedTo(uses);
		if (eqSet!=null && !eqSet.isEmpty()) {
			for (Type t: eqSet) {
				Set<Type> s;
				// evaluate the equal set from topOfStack, but make sure we don't recurse indefinitely
				if (topOfStack == this) {
				  s = base.relatedTo(t, topOfStack);
				}
				else {
				  s = topOfStack.relatedTo(t, this);
				}
				if (s!=null) 
					ret.addAll(s);
				ret.add(t); // enforce reflexivity
			}
		  // enforce reflexivity
			Set<Type> related = base.relatedTo(domain, topOfStack); 
			if (related!=null) {
			  ret.addAll(related); 
			}
			return ret;
		}
		else {
			return base.relatedTo(domain, topOfStack);
		}
	}

  /* (non-Javadoc)
   * @see casa.ontology.Relation#add(casa.ontology.Type, casa.ontology.Type)
   */
  @Override
	public boolean add(Type subject, Type object) throws IllegalOperationException {
		if (/*!(base instanceof PrimitiveRelation) ||*/ !base.isAssignable()) throw new IllegalOperationException("Relation "+getName()+" is not assignable");
  	return ((PrimitiveRelation)base).add(subject, object);
  }
    
  @Override
	public boolean remove(Type subject, Type object)
  		throws IllegalOperationException {
		if (!(base instanceof PrimitiveRelation)) throw new IllegalOperationException("Relation "+getName()+" is not assignable");
  	return ((PrimitiveRelation)base).remove(object, subject);
  }

	@Override
	public boolean hasProperty(Property property) {
		return property.equals(Property.USES) ? true : base.hasProperty(property);
	}
	
	@Override
	public String toStringOptions() {
		return base.toStringOptions()+":uses \""+uses.getName()+"\" ";
	}

	/**
	 * @return this
	 * @see casa.ontology.v3.BasedRelation#getUses()
	 */
	@Override
	public ConcreteRelation getUses() {
		return uses;
	}

	/* (non-Javadoc)
	 * @see casa.ontology.v3.ConcreteRelation#toStringComment()
	 */
	@Override
	public String toStringComment(Ontology relativeToOntology) {
		return name.getRelativeName(relativeToOntology)+"("+getClass().getSimpleName()
				   +" "+uses.name.getRelativeName(relativeToOntology)+")"
				   +", "+base.toStringComment(relativeToOntology);
	}

}
