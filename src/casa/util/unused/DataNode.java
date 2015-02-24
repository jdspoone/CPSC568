package casa.util.unused;

import casa.util.PropertiesMap;

import java.math.BigDecimal;

/**
 * A <code>DataNode</code> object is a node in a
 * <code>LogicalPropositionTree</code> that represents a <var>string</var> or a
 * <var>number</var>.  It can possibly evaluate to a number or a string if
 * given the context of a <code>PropertiesMap</code>.
 *
 * A <var>string</var> is a character string.<br>
 * A <var>number</var> is either a integer or floating point number.
 *
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary.
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  The
 * Knowledge Science Group makes no representations about the suitability of
 * this software for any purpose.  It is provided "as is" without express or
 * implied warranty.</p>
 *
 * @author Jason Heard
 * @version 0.9
 * @deprecated (rck) The classes BooleanNode, DataNode, LogicalNode, LogicalPropoisitonTree,
 * LogicalPropositionTreeException, LogicalPropositionTreeNode, NumberLiteralNode,
 * PropositionNode, and StringLiteralNode taken together appear to be unused in CASA.
 */

@Deprecated
public interface DataNode {
  /**
   * Determines whether this node can evaluate to a number with the given
   * context.  Returns <code>true</code> if a call to <code>getNumber()</code>
   * with the same context won't throw an exception; <code>false</code>
   * otherwise.
   *
   * @param map The context within which we are evaluating the truth value.
   * Defines the type and value of all properties in the tree during the
   * evaluation.
   * @return <code>true</code> if a call to <code>getNumber()</code> with the
   * same context won't throw an exception; <code>false</code> otherwise.
   */
  public boolean isANumber (PropertiesMap map);

  /**
   * Determines whether this node can evaluate to a string with the given
   * context.  Returns <code>true</code> if a call to <code>getString()</code>
   * with the same context won't throw an exception; <code>false</code>
   * otherwise.
   *
   * @param map The context within which we are evaluating the truth value.
   * Defines the type and value of all properties in the tree during the
   * evaluation.
   * @return <code>true</code> if a call to <code>getString()</code> with the
   * same context won't throw an exception; <code>false</code> otherwise.
   */
  public boolean isAString (PropertiesMap map);

  /**
   * Retrieves this node's numerical value.  Returns a <code>BigDecimal</code>
   * containing the numerical value of this node with the given context.
   *
   * @param map The context within which we are evaluating the numerical value.
   * Defines the type and value of all properties in the tree during the
   * evaluation.
   * @return A <code>BigDecimal</code> containing the numerical value of this
   * node with the given context.
   * @throws LogicalPropositionTreeException If there was a problem determining
   * whether this node represents a number.  Usually this happens because a
   * property was referenced as the wrong type or did not exist.
   */
  public BigDecimal getNumber (PropertiesMap map) throws LogicalPropositionTreeException;

  /**
   * Retrieves this node's string value.  Returns a <code>String</code>
   * that is the string value of this node with the given context.
   *
   * @param map The context within which we are evaluating the string value.
   * Defines the type and value of all properties in the tree during the
   * evaluation.
   * @return A <code>String</code> that is the string value of this node with
   * the given context.
   * @throws LogicalPropositionTreeException If there was a problem determining
   * whether this node represents a number.  Usually this happens because a
   * property was referenced as the wrong type or did not exist.
   */
  public String getString (PropertiesMap map) throws LogicalPropositionTreeException;
}