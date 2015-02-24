package casa.util.unused;

import casa.util.PropertiesMap;

import java.math.BigDecimal;

/**
 * A <code>NumberLiteralNode</code> object is a node in a
 * <code>LogicalPropositionTree</code> that represents a <var>literal
 * number</var>.  It can evaluate to a number.
 *
 * A <var>literal number</var> is string in the form
 * <var>"-"?[0-9]*"."?[0-9]*</var> that evaluates to a <var>number</var>.<br>
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
public class NumberLiteralNode implements DataNode {
  /**
   * The numerical value of this node.
   */
  BigDecimal number;

  /**
   * Creates a new <code>NumberLiteralNode</code> with thes numerical value
   * represented by the given string.
   *
   * @param newNumber The numerical value of the new node in a string
   * representation.
   */
  public NumberLiteralNode (String newNumber) {
    this.number = new BigDecimal (newNumber);
  }

  /**
   * Creates a new <code>NumberLiteralNode</code> with the given
   * <code>BigDecimal</code> numerical value.
   *
   * @param newNumber The numerical value of the new node represented by a
   * <code>BigDecimal</code> object.
   */
  public NumberLiteralNode (BigDecimal newNumber) {
    this.number = newNumber;
  }

  /**
   * Creates a new <code>NumberLiteralNode</code> with the given
   * <code>double</code> numerical value.
   *
   * @param newNumber The numerical value of the new node represented by a
   * <code>double</code>.
   */
  public NumberLiteralNode (double newNumber) {
    this.number = new BigDecimal (newNumber);
  }

  /**
   * Creates a new <code>NumberLiteralNode</code> with the given
   * <code>long</code> numerical value.
   *
   * @param newNumber The numerical value of the new node represented by a
   * <code>long</code>.
   */
  public NumberLiteralNode (long newNumber) {
    this.number = new BigDecimal (newNumber);
  }

  /**
   * Determines whether this node can evaluate to a number with the given
   * context.  Returns <code>true</code> always.
   *
   * @param map The context within which we are evaluating the truth value.
   * Defines the type and value of all properties in the tree during the
   * evaluation.
   * @return <code>true</code> always.
   */
  public boolean isANumber (PropertiesMap map) {
    return true;
  }

  /**
   * Determines whether this node can evaluate to a string with the given
   * context.  Returns <code>false</code> always.
   *
   * @param map The context within which we are evaluating the truth value.
   * Defines the type and value of all properties in the tree during the
   * evaluation.
   * @return <code>false</code> always.
   */
  public boolean isAString (PropertiesMap map) {
    return false;
  }

  /**
   * Retrieves this node's numerical value.  Returns a <code>BigDecimal</code>
   * containing the numerical value of this node.
   *
   * @param map The context within which we are evaluating the string value.
   * Does not affect the result of this call since this node has a constant
   * value.
   * @return A <code>BigDecimal</code> containing the numerical value of this
   * node.
   */
  public BigDecimal getNumber (PropertiesMap map) {
    return number;
  }

  /**
   * Retrieves this node's string value.  Since this node has only a numerical
   * value, this function always throws an exception.
   *
   * @param map The context within which we are evaluating the string value.
   * Does not affect the result of this call since this node has a constant
   * value.
   * @return Nothing, since this function always throws an exception.
   * @throws LogicalPropositionTreeException Since this node has only a
   * numerical value.
   */
  public String getString (PropertiesMap map) throws LogicalPropositionTreeException {
    throw new LogicalPropositionTreeException ("This node is a NumberLiteralNode, it doesn't implement getString()!");
  }
}