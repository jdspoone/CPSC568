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

import casa.ontology.Ontology;
import casa.ontology.Relation;
import casa.ontology.Type;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class TypeListType extends BaseType implements RuleBasedRelationType {
	
	List<BaseType> list; 

	/**
	 * @param list
	 */
	public TypeListType(List<BaseType> list) {
		this.list = new LinkedList<BaseType>(list);
	}

	@Override
	public String getName() {
		StringBuffer b = new StringBuffer();
		b.append('(');
		boolean first=true;
		for (Type t: list) {
			if (first) first=false; 
			else b.append(' ');
			b.append(t.getName());
		}
		b.append(')');
		return b.toString();
	}
	
	public String getRelativeName(Ontology ont) {
		StringBuffer b = new StringBuffer();
		b.append('(');
		boolean first=true;
		for (Type t: list) {
			if (first) first=false; 
			else b.append(' ');
			b.append(((BaseType)t).getRelativeName(ont));
		}
		b.append(')');
		return b.toString();
	}
	


	@Override
	public Ontology getOntology() {
		Ontology ont = (Ontology)list.get(0).getOntology();
		return ont;
	}
	
  /**
   * @param relation
   * @return the set of types that are related to this type for this particular relation (the set in the range).
   */
  @Override
	public Set<Type> extendedRelation(Relation relation) {
  	TreeSet<Type> ret = new TreeSet<Type>();
  	if (!"isa-parent".equals(relation.getName())) 
  		return ret;
  	
  	if (list.size()==0) return ret;
  	LinkedList<BaseType> prefix = new LinkedList<BaseType>(); // make an empty prefix
  	ret.add(new TypeListType(prefix)); // add "top"
  	extendedRelation(relation,ret,prefix,0);
  	return ret;
  }
  
  /**
   * @param relation the relation we're working on (should be already veted as appropriate by {@link #extendedRelation(Relation)})
   * @param ret the accumulated return list of related elements
   * @param prefix the prefix (base) of the elements we should add (may be empty)
   * @param index the index we are working on here
   */
  private void extendedRelation(Relation relation, Set<Type> ret, List<BaseType> prefix, int index) {
  	assert prefix.size() == index;
  	if (index>=list.size()) 
  		return;
  	LinkedList<BaseType> temp = new LinkedList<BaseType>(prefix);
  	
  	//add add possibilities with the type at index in the index position
   	temp.add(list.get(index));
 		ret.add(new TypeListType(temp));
 		extendedRelation(relation, ret, temp, index+1);
 		
  	//add add possibilities with type type at index's parent in the index position
 		temp.removeLast();
 		for (Type i: relation.relatedTo(list.get(index))) {
   	  assert i instanceof BaseType; 
 			temp.add((BaseType)i);
 		  ret.add(new TypeListType(temp));
 		  extendedRelation(relation, ret, temp, index+1);
 		}
  }

  /**
   * @param relation
   * @param other
   * @return true iff this type related to the other type for the particular relation (the other is in the range).
   */
  public boolean extendedRelation(Relation relation, Type other) {
  	if (!"isa-parent".equals(relation.getName())) return false;
  	if (!(other instanceof TypeListType)) return false;
  	TypeListType range = (TypeListType)other;
  	int rangeSize = range.list.size();
  	if (list.size()<rangeSize) return false;
  	int i = 0;
  	for (Type t: list) {
  		if (i>=rangeSize) return true;
  		Type o = range.list.get(i++);
  		if (!relation.relatedTo(t, o)) return false; 
  	}
  	return true;
  }
  
  public boolean overrides(Relation relation) {
  	return "isa".equals(relation.getName());
  }

	@Override
	public String getUnqualifiedName() {
		// TODO Auto-generated method stub
		return getName();
	}
	
	private boolean individual = false;

	@Override
	public boolean isIndividual() {
		return individual;
	}
  
}
