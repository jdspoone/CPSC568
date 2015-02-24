package casa.agentCom;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Set of URLs.
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
 * 
 * @version 0.9
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */

public class URLset {
  TreeSet s = new TreeSet();
  boolean marked = false;

  public URLset() {
  }

  /**
	 * @param v
	 * @return
	 */
  public boolean setMarked(boolean v) {
    boolean temp = marked;
    marked = v;
    return temp;
  }

  public boolean marked() {return marked;}

  public URLDescriptor getUniqueElement() {
    Iterator i = s.iterator();
    if (i.hasNext()) return (URLDescriptor)i.next();
    return null;
  }

  /**
   * Returns the number of elements in this set (its cardinality).  If this
   * set contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
   * <tt>Integer.MAX_VALUE</tt>.
   *
   * @return the number of elements in this set (its cardinality).
   */
  public int size() {return s.size();}

  /**
   * Returns <tt>true</tt> if this set contains no elements.
   *
   * @return <tt>true</tt> if this set contains no elements.
   */
  public boolean isEmpty() {return s.isEmpty();}

  /**
   * Returns <tt>true</tt> if this set contains the specified element.  More
   * formally, returns <tt>true</tt> if and only if this set contains an
   * element <code>e</code> such that <code>(o==null ? e==null :
   * o.equals(e))</code>.
   *
   * @param o element whose presence in this set is to be tested.
   * @return <tt>true</tt> if this set contains the specified element.
   * @throws ClassCastException if the type of the specified element
   * 	       is incompatible with this set (optional).
   * @throws NullPointerException if the specified element is null and this
   *         set does not support null elements (optional).
   */
  public boolean contains(URLDescriptor o) {return s.contains(o);}

  /**
   * Returns an iterator over the elements in this set.  The elements are
   * returned in no particular order (unless this set is an instance of some
   * class that provides a guarantee).
   *
   * @return an iterator over the elements in this set.
   */
  public Iterator iterator() {return s.iterator();}

  /**
   * Returns an array containing all of the elements in this set.
   * Obeys the general contract of the <tt>Collection.toArray</tt> method.
   *
   * @return an array containing all of the elements in this set.
   */
  public URLDescriptor[] toArray() {return (URLDescriptor[])(s.toArray());}



  // Modification Operations

  /**
   * Adds the specified element to this set if it is not already present
   * (optional operation).  More formally, adds the specified element,
   * <code>o</code>, to this set if this set contains no element
   * <code>e</code> such that <code>(o==null ? e==null :
   * o.equals(e))</code>.  If this set already contains the specified
   * element, the call leaves this set unchanged and returns <tt>false</tt>.
   * In combination with the restriction on constructors, this ensures that
   * sets never contain duplicate elements.<p>
   *
   * The stipulation above does not imply that sets must accept all
   * elements; sets may refuse to add any particular element, including
   * <tt>null</tt>, and throwing an exception, as described in the
   * specification for <tt>Collection.add</tt>.  Individual set
   * implementations should clearly document any restrictions on the the
   * elements that they may contain.
   *
   * @param o element to be added to this set.
   * @return <tt>true</tt> if this set did not already contain the specified
   *         element.
   *
   * @throws UnsupportedOperationException if the <tt>add</tt> method is not
   * 	       supported by this set.
   * @throws ClassCastException if the class of the specified element
   * 	       prevents it from being added to this set.
   * @throws NullPointerException if the specified element is null and this
   *         set does not support null elements.
   * @throws IllegalArgumentException if some aspect of the specified element
   *         prevents it from being added to this set.
   */
  public boolean add(URLDescriptor o) {return s.add(o);}


  /**
   * Removes the specified element from this set if it is present (optional
   * operation).  More formally, removes an element <code>e</code> such that
   * <code>(o==null ?  e==null : o.equals(e))</code>, if the set contains
   * such an element.  Returns <tt>true</tt> if the set contained the
   * specified element (or equivalently, if the set changed as a result of
   * the call).  (The set will not contain the specified element once the
   * call returns.)
   *
   * @param o object to be removed from this set, if present.
   * @return true if the set contained the specified element.
   * @throws ClassCastException if the type of the specified element
   * 	       is incompatible with this set (optional).
   * @throws NullPointerException if the specified element is null and this
   *         set does not support null elements (optional).
   * @throws UnsupportedOperationException if the <tt>remove</tt> method is
   *         not supported by this set.
   */
  public boolean remove(URLDescriptor o) {return s.remove(o);}


  // Bulk Operations


  /**
   * Removes all of the elements from this set (optional operation).
   * This set will be empty after this call returns (unless it throws an
   * exception).
   *
   * @throws UnsupportedOperationException if the <tt>clear</tt> method
   * 		  is not supported by this set.
   */
  public void clear() {s.clear();}


  // Comparison and hashing

  /**
   * Compares the specified object with this set for equality.  Returns
   * <tt>true</tt> if the specified object is also a set, the two sets
   * have the same size, and every member of the specified set is
   * contained in this set (or equivalently, every member of this set is
   * contained in the specified set).  This definition ensures that the
   * equals method works properly across different implementations of the
   * set interface.
   *
   * @param o Object to be compared for equality with this set.
   * @return <tt>true</tt> if the specified Object is equal to this set.
   */
  public boolean equals(Object o) {return s.equals(o);}

  /**
   *
   * Returns the hash code value for this set.  The hash code of a set is
   * defined to be the sum of the hash codes of the elements in the set,
   * where the hashcode of a <tt>null</tt> element is defined to be zero.
   * This ensures that <code>s1.equals(s2)</code> implies that
   * <code>s1.hashCode()==s2.hashCode()</code> for any two sets
   * <code>s1</code> and <code>s2</code>, as required by the general
   * contract of the <tt>Object.hashCode</tt> method.
   *
   * @return the hash code value for this set.
   * @see Object#hashCode()
   * @see Object#equals(Object)
   * @see Set#equals(Object)
   */
  public int hashCode() {return s.hashCode();}

}