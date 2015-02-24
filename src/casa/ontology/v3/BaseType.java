package casa.ontology.v3;

import casa.exceptions.IllegalOperationException;
import casa.ontology.Ontology;
import casa.ontology.OntologyEntity;
import casa.ontology.Relation;
import casa.ontology.Type;

import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

/**
 * Helper class for Ontology; not to be used by any other class other than Ontology. 
 * A BaseType stores all the relations it participates in through a {@link BaseType.Role}, 
 * which is the 
 *  
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. 
 * Permission to use, copy, modify, distribute and sell this software and its 
 * documentation for any purpose is hereby granted without fee, provided that 
 * the above copyright notice appear in all copies and that both that copyright 
 * notice and this permission notice appear in supporting documentation.  The 
 * Knowledge Science Group makes no representations about the suitability of this 
 * software for any purpose.  It is provided "as is" without express or implied 
 * warranty.</p>
 * <p>Company: Knowledge Science Group</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
abstract class BaseType implements Type {
	
	/**
	 * Responsible to store a {@link Relation} ({@link #rel}) together with:
	 * <ul>
	 * <li>{@link #inDomain}: a list of other types in which this type is in relation in the domain position
	 * <li>{@link #inRange}: a list of other types in which this type is in relation in the range position
	 * </ul> 
	 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
	 */
	protected class Role {
		Role(Relation rel) {
			this.rel = rel;
		}
		/** The relation through which this Type is participating with another type. */
		Relation rel;
		/** a list of other types in which this type is in relation in the domain position. */
		public TreeSet<Type> inDomain=new TreeSet<Type>();
		/** a list of other types in which this type is in relation in the range position. */
		public TreeSet<Type> inRange=new TreeSet<Type>();
	}
	
	TreeMap<String, Role> roles = new TreeMap<String, Role>();
	
	/**
	 * Used by TypeHierarchy to help in visiting the nodes
	 */
	protected int mark;
	/**
	 * Name of the type.
	 */
	private Name name;
	
	BaseType(String name, Ontology ont) throws IllegalOperationException {
		this.name = new Name(ont, new Name(name).getName());
	}
	
	BaseType() {}
	
	/**
	 * Primitive method to add a relation to link this Type through relation to another
	 * Type.  This type will the domain, and other <em>rolefiller</em> type will be
	 * the range.  The back link will automatically be created using {@link #insertRelationFrom(Relation, BaseType)}.
	 * @param relation the relation to add or extend (depending if it exists or not)
	 * @param x the child to add
	 */
	protected boolean insertRelationTo(Relation relation, BaseType roleFiller) {
		Role role = roles.get(relation.getName());
		if (role==null) {
			role = new BaseType.Role(relation);
			roles.put(relation.getName(), role);
		}
		roleFiller.insertRelationFrom(relation, this);
		return role.inDomain.add(roleFiller);
	}
	
	/**
	 * @return
	 */
	public String getName() {
		return name.toString();
	}
	
	public String getRelativeName(Ontology ont) {
		return name.getRelativeName(ont);
	}
	
	@Override
	public Ontology getOntology() {
		return name.getOntology();
	}
	
	/**
	 * @return
	 */
	public String getUnqualifiedName() {
		return name.getName();
	}
	

	/**
	 * @param relation
	 * @param roleFiller
	 * @throws IllegalOperationException
	 */
	protected void insertRelationTo(Relation relation, Vector<BaseType> roleFiller) {
		if (roleFiller!=null)
			for (BaseType p: roleFiller) {
				insertRelationTo(relation, p);
			}
	}
	
	/**
	 * Primitive method to add a link to the Type oo the range end of relation.
	 * Users of the ontology
	 * should use only {@link #insertRelationFrom(Relation, BaseType)} or 
	 * {@link #insertRelationTo(Relation, Vector)}
	 * @param relation the relation to add or extend (dependint if it exists or not)
	 * @param x the child to add
	 */
	private void insertRelationFrom(Relation relation, BaseType x) {
		Role role = roles.get(relation.getName());
		if (role==null) {
			role = new BaseType.Role(relation);
			roles.put(relation.getName(), role);
		}
		role.inRange.add(x);
		}
	
	/**
	 * Primitive method to remove a relation link from this Type through <em>relation</em> to 
	 * <em>rolefiller</em>.  This type will the domain, and other <em>rolefiller</em> type will be
	 * the range.  The back link will automatically be deleted using {@link #insertRelationFrom(Relation, BaseType)}.
	 * The the role (relation) is now empty, delete it to clean up.
	 * @param relation the relation to add or extend (depending if it exists or not)
	 * @param x the child to add
	 * @return true if the relationship was removed or false if the relationship wasn't there or couldn't be removed
	 */
	protected boolean removeRelationTo(Relation relation, BaseType roleFiller) {
		Role role = roles.get(relation.getName());
		if (role==null) return false;
		boolean ret = role.inDomain.remove(roleFiller);
		if (role.inDomain.isEmpty() && role.inRange.isEmpty()) roles.remove(relation.getName()); 
		if (ret) {
			role = roleFiller.roles.get(relation.getName());
			assert role!=null: "Something is seriously inconsistent here";
			role.inRange.remove(this);
			if (role.inDomain.isEmpty() && role.inRange.isEmpty()) roleFiller.roles.remove(relation.getName()); 
		}
		return ret;
	}
	
	/**
	 * Traverses the hierarchy setting each node's <em>mark</em> element to the
	 * number of parents of the node.
	 */
	public void resetMark() {
		Role isa = roles.get("ISA-PARENT"); 
		if (isa==null) {
			mark = 0;
			return;
		}
		mark = isa.inDomain.size();
		for (Type t: isa.inRange)
			t.resetMark();
	}
	
	/**
	 * @return a reference to the set of types that this type is related to (by "relation", ie:
	 * this type is in the domain and the others are in the range) or null if this 
	 * type has no such role (note that the return set could also be empty if the role 
	 * exists but there are no fillers).
	 */
	protected Set<Type> relatedTo(String relation) {
		Role r = roles.get(relation);
		return r==null?null:r.inDomain;
		}
	
	/**
	 * @return a reference to the set of types that have a relation to this type (by "relation", ie:
	 * this type is in the range and the others are in domain) or null if this 
	 * type has no such role between any type.
	 */
	protected Set<Type> relatedFrom(String relation) {
		Role r = roles.get(relation);
		return r==null?null:r.inRange;
	}
	
	/**
	 * @return a reference to the set of types that this type is related to (by "relation", ie:
	 * this type is in the domain and the others are in the range) or null if this 
	 * type has no such role (note that the return set could also be empty if the role 
	 * exists but there are no fillers).
	 */
	protected Set<Type> relatedTo(Relation relation) {
		Role r = roles.get(relation.getName());
		return r==null?null:r.inDomain;
		}
	
	/**
	 * @return true if this Type is related to <em>range</em> (by <em>relation</em>, ie:
	 * this type is in the domain and the other is in the range) or null if this 
	 * type has no such role or the role doesn't contain <em>range</em>.
	 */
	protected boolean relatedTo(Relation relation, BaseType range) {
		Set<Type> r = relatedTo(relation);
		if (r==null) return false;
		return r.contains(range);
		}
	
	/**
	 * @return a reference to the set of types that have a relation to this type (by "relation", ie:
	 * this type is in the range and the others are in domain) or null if this 
	 * type has no such role between any type.
	 */
	protected Set<Type> relatedFrom(Relation relation) {
		Role r = roles.get(relation.getName());
		return r==null?null:r.inRange;
	}
	
	/**
	 * @return true if this Type is on the range side of relation <em>rlations</em>
	 * with <em>domain</em> on the domain side; or null if this 
	 * type has no such role or the role doesn't contain <em>domain</em>.
	 */
	protected boolean relatedFrom(Relation relation, BaseType domain) {
		Set<Type> r = relatedFrom(relation);
		if (r==null) return false;
		return r.contains(domain);
		}
	
	/**
	 * Returns the set of roles (relation names) that this Type is in the domain for.
	 * @return  a reference to the <em>parents</em> Vector.
	 */
	protected Set<String> getRoles() {
		Set<String> ret = new TreeSet<String>(roles.keySet());
		for (String name: roles.keySet()) {
			Role r = roles.get(name);
			if (r.inDomain.size()==0) ret.remove(name);
		}
		return ret;
	}
		
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/* (non-Javadoc)
	 * @see casa.ontology.v3.Type#toString()
	 */
	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append(toStringParent()).append(toStringNonIsa());
		return b.toString();
	}

  /**
   * @return A string of the form "(declType isa-parent [thisType] [rangetype]...)"
   * describing all the non-isa relations for this type, or if there is no isa relation
   * the string is "(declType [thisType)".
   */
  protected String toStringParent () {
  	StringBuffer b = new StringBuffer();
  	
  	Role map = roles.get(CASAOntology.isaParent.getName());
  	boolean empty = map==null || map.inDomain==null || map.inDomain.size()==0; 
  	if (empty || isIndividual()) {
  	  b.append(isIndividual()?"(declIndividual \"":"(declType \"")
  	  .append(getName())
  	  .append('"');
  	}
  	if (!empty) {
  	  b.append("(declMaplet isa-parent \"")
  	  .append(getName())
  	  .append('"')
  		.append(' ');
  	  if (map.inDomain.size()>1) b.append('(');
  	  for (Type t: map.inDomain) b.append(t.getName()).append(' ');
  	  b.setLength(b.length()-1);
  	  if (map.inDomain.size()>1) b.append(')');
  	}
  	b.append(')');
  	return b.toString();
  }
	
  /**
   * @return A string of the form "(settbpx [role] [thisType] [rangetype]...) [newline] ..."
   * describing all the non-isa relations for this type.
   */
  protected String toStringNonIsa() {
    StringBuffer b = new StringBuffer();
  	for (String r: roles.keySet()) {
  		if (CASAOntology.isaParent.getName().equalsIgnoreCase(r)) continue;
  		Role role = roles.get(r);
  		if (!role.rel.isAssignable()) continue;
  		if (role.inDomain==null || role.inDomain.size()==0) continue;
  	  b.append("\n  (declMaplet ")
  	  .append(r)
  	  .append(' ')
  	  .append(getName())
  		.append(' ');
  	  if (role.inDomain.size()>1) b.append('(');
  	  for (Type t: role.inDomain) b.append(t.getName()).append(' ');
  	  b.setLength(b.length()-1);
  	  if (role.inDomain.size()>1) b.append(')');
  		b.append(')');
  	}
  	return b.toString();
  }
  
	/**
	 * Recursive method to print to a String a node and all it's children in the persistent
	 * format in the an order such that no node definition comes before the
	 * definition of all it's ancestors.  <b>A call to <code>resetMark()</code>
	 * must precede the call to this method in order for it to function correctly.
	 * @param x the node to print to the String
	 * @return a String representing the persistent format of the node and all it's
	 * children
	 */
	protected String toStringWithIsaChildren() {
		String ret = "";
		if (!CASAOntology.TOP.equals(getName())) ret = toStringParent() + "\n";
		else ret = "(decltype \"TOP\")\n";
		Set<Type> children = relatedFrom("ISA-PARENT");
		if (children!=null) {
  		for (Type c: children) {
  			BaseType child = (BaseType)c;
  			if ((--child.mark)==0) { //don't output a child unless this is the deepest parent (prevents duplicate and pre-mature output)
  				ret += child.toStringWithIsaChildren();
  			}
  		}
		}
		return ret;
	}
	

  
	/**
	 * Used by TreeMap as a sorting comparitor.  Compares on the basis of
	 * the <em>name</em>.
	 * @param x Assumedly another Node object, but could be a String
	 * @return -1 if <em>name</em> is less than <em>x.name</em>;
	 *          0 if <em>name</em> is equal to <em>x.name</em>;
	 *          1 if <em>name</em> is greater than <em>x.name</em>
	 */
//	public int compareTo(Object x) {
//		try                  { return compareTo((TypeNode) x);  }
//		catch (Exception ex) { return compareTo((String)x); }
//	}
	
	/**
	 * Used by TreeMap as a sorting comparitor.  Compares on the basis of
	 * the <em>name</em>.
	 * @param x A String to be compared to <em>name</em>.
	 * @return -1 if <em>name</em> is less than <em>x.name</em>;
	 *          0 if <em>name</em> is equal to <em>x.name</em>;
	 *          1 if <em>name</em> is greater than <em>x.name</em>
	 */
	public int compareTo(String x) {return getName().compareTo(x);}
	
	/**
	 * Used by TreeMap as a sorting comparitor.  Compares on the basis of
	 * the <em>name</em>.
	 * @param x Another Node object to be compared with
	 * @return -1 if <em>name</em> is less than <em>x.name</em>;
	 *          0 if <em>name</em> is equal to <em>x.name</em>;
	 *          1 if <em>name</em> is greater than <em>x.name</em>
	 */
	public int compareTo(OntologyEntity x) {return getName().compareTo(x.getName());}
	
	/**
	 * Determines if this Node is equal to <em>x</em>, where "equal" means that
	 * the <em>name</em>s are the same, and the name of each of the parents is
	 * the same.  This is non-recursive.
	 * @param x A Node to be compared with
	 * @return true iff this and <em>x</em> are equal.
	 */
	public boolean equals(Type x) {
		return (compareTo(x)==0)
		//&& (new TreeSet<Type>(parents)).equals(new TreeSet<Type>(x.parents))
		;
	}
	
//	/**
//	 * Returns a String set to the persistent representation of this node
//	 * object.  Form is: <br>
//	 * <em>name</em> &lt; {<em>parent.name</em>}* ;
//	 * <br>
//	 * @return the persistent representation of this node object.
//	 */
//	@Override
//	public String toString() {
//		String ret = CASAUtil.toQuotedString(name) + " <";
//		for (int i=0,end = parents.size(); i<end; i++) {
//			ret += " " + CASAUtil.toQuotedString(parents.elementAt(i).name);
//		}
//		ret += ";";
//		return ret;
//	}
}
