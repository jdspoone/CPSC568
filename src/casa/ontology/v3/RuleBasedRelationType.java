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

import casa.ontology.Relation;
import casa.ontology.Type;

import java.util.Set;

/**
 * Implement this interface in a {@link Type} to cause Primitive Relations to consult
 * the type when determining direct relationships between types.
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public interface RuleBasedRelationType {
  /**
   * @param relation
   * @return the set of types that are related to this type for this particular relation (the set in the range).
   */
  public Set<Type> extendedRelation(Relation relation);
  /**
   * @param relation
   * @param other
   * @return true iff this type related to the other type for the particular relation (the other is in the range).
   */
  public boolean extendedRelation(Relation relation, Type other);
  
  public boolean overrides(Relation relation);
}
