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
package casa.ontology;

import casa.exceptions.IllegalOperationException;
import casa.ontology.Relation.Property;

import java.util.Set;

/**
 * Relations may be stacked, decorator pattern fashion, with a "primitive relation" (which is one that actually contains maplets) at the bottom of the stack.  This strategy can "automatically" keep relationships between elements updated when information changes.  There are constraints in doing so though: <pre> inverse must be based directly on a primitive relation (symmetric<:inverse) == (symmetric<:primitive(inverse)) primitive /     | /      | inverse    | \      | \--U--| symmetric <pre> 
 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
public interface Relation extends OntologyEntity {
	
	/**
	 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
	 */
	public enum Property {
	INVERSE {
		public boolean assignable() {return true;} 
	},
	TRANSITIVE{
	},
	SYMMETRIC{
	}, 
	ASYMMETRIC{
	}, 
	REFLEXIVE{
	}, 
	ASSIGNABLE{
		public boolean assignable() {return true;} 
	},
	USES() {
		public boolean assignable() {return true;} 
	};
	public boolean assignable() {return false;}
	};
	
	/**
	 * Determines if an element is related to another element.  In terms 
	 * of description logics, whether <code>domain</code> has a role of
	 * type <code>range</code>.
	 * @param domain the thing that is a the "source" of the relation
	 * @param range the thing that is the "destination" of the relation
	 * @return true iff <code>domain</code> is related to <code>range</code>
	 */
	public boolean relatedTo(Type domain, Type range);
	
	/**
	 * Returns all the types that this domain is related to.  In terms 
	 * of description logics, whether <code>domain</code> has a role of
	 * type <code>range</code>.
	 * @param domain the thing that is a the "source" of the relation
	 * @return The set of ranges that are related to this type, or an empty set
	 * if the domain has no such relationship.
	 */
	public Set<Type> relatedTo(Type domain);
		
//	/**
//	 * Returns all the types that are related to this type.  In terms 
//	 * of description logics, whether <code>domain</code> has a role of
//	 * type <code>range</code>.
//	 * @param domain the thing that is a the "source" of the relation
//	 * @return The set of ranges that are related to this type, or an empty set
//	 * if the domain has no such relationship.
//	 */
//	public Set<Type> relatedFrom(Type range);
		
	/**
	 * Puts the domain/range pair into the relation.  In terms 
	 * of description logics, whether <code>domain</code> has a role of
	 * type <code>range</code>.
	 * @param domain the thing that is a the "source" of the relation
	 * @param range the thing that is the "destination" of the relation
	 * @return true if the relation did not already contain the specified pair.
	 */
	public boolean add(Type domain, Type range) throws IllegalOperationException;
	
	/**
	 * Removes domain/range pair from the relation.  In terms 
	 * of description logics, whether <code>domain</code> has a role of
	 * type <code>range</code>.
	 * @param domain the thing that is a the "source" of the relation
	 * @param range the thing that is the "destination" of the relation
	 * @return true if the relation contained the specified pair.
	 */
	public boolean remove(Type domain, Type range) throws IllegalOperationException;
	
	/**
	 * Some relations are based on other relations, and only base relations can have
	 * maplets declared as members.  For example, the relation ancestor is based on
	 * the relation parent (ancestor being the transitive form of parent); one can 
	 * assign the maplet (parent james jill), but one cannot assign the maplet
	 * (ancestor james jill) because it's ambiguous whether james is a parent of jill
	 * or not.
	 * @return true iff it is possible to assignment maplets directly to the relation
	 */
	public boolean isAssignable();
	
	/**
	 * Convenience method to check if a method is marked.  See  {@link #setMark(boolean)} .
	 * @return  true iff the relation has been marked.
	 */
	public boolean isMark();
	
	/**
	 * Convenience method to set or clear a mark in the relation when iterating relations.
	 * @param mark  value to set the mark to.
	 */

	public void setMark(boolean mark);

	/**
	 * @param property
	 * @return
	 */
	public boolean hasProperty(Property property);

	/**
	 * @return  true if the relations is visible, false if it's not.
	 * @see  #setVisible(boolean)
	 */
	public boolean isVisible();

	/**
	 * Some relations are not declared, but are created in some implementations in the process of creating other relations.  It is sometimes convenient to "hide" these relations from the user and not have these show up in the persistent form (see  {@link #toString()} ).  Use setVisible() to set the visibility  attribute as read by  {@link #isVisible()} .
	 * @param set  set to true to make this relation visible, false to make it invisible.
	 * @return  the previous value of visibility.
	 */
	public boolean setVisible(boolean set);
	
}
