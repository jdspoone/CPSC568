package casa.util.unused;

import casa.util.PropertiesMap;

import java.math.BigDecimal;

/**
 * A <code>NumberLiteralNode</code> object is a node in a
 * <code>LogicalPropositionTree</code> that represents a <var>literal
 * number</var>.  It can evaluate to a number.
 *
 * A <var>literal string</var> is string in the form
 * <code>"</code><var>.*</var><code>"</code> that evaluates to the
 * <var>string</var> contained in the quotes (<code>"</code>s).<br>
 * A <var>string</var> is a character string.
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
public class StringLiteralNode implements DataNode {
  /**
   * The string value of this node.
   */
  private String string;

  /**
   * Creates a new <code>StringLiteralNode</code> with the given string value.
   *
   * @param newString The string value of the new node.
   */
  public StringLiteralNode (String newString) {
    this.string = newString;
  }

  /**
   * Determines whether this node can evaluate to a number with the given
   * context.  Returns <code>false</code> always.
   *
   * @param map The context within which we are evaluating the truth value.
   * Defines the type and value of all properties in the tree during the
   * evaluation.
   * @return <code>false</code> always.
   */
  public boolean isANumber (PropertiesMap map) {
    return false;
  }

  /**
   * Determines whether this node can evaluate to a string with the given
   * context.  Returns <code>true</code> always.
   *
   * @param map The context within which we are evaluating the truth value.
   * Defines the type and value of all properties in the tree during the
   * evaluation.
   * @return <code>true</code> always.
   */
  public boolean isAString (PropertiesMap map) {
    return true;
  }

  /**
   * Retrieves this node's numerical value.  Since this node has only a string
   * value, this function always throws an exception.
   *
   * @param map The context within which we are evaluating the numerical value.
   * Does not affect the result of this call since this node has a constant
   * value.
   * @return Nothing, since this function always throws an exception.
   * @throws LogicalPropositionTreeException Since this node has only a
   * string value.
   */
  public BigDecimal getNumber (PropertiesMap map) throws LogicalPropositionTreeException {
    throw new LogicalPropositionTreeException ("This node is a StringLiteralNode, it doesn't implement getNumber()!");
  }

  /**
   * Retrieves this node's numerical value.  Returns a <code>String</code>
   * containing the string value of this node.
   *
   * @param map The context within which we are evaluating the string value.
   * Does not affect the result of this call since this node has a constant
   * value.
   * @return A <code>String</code> containing the string value of this
   * node.
   */
  public String getString (PropertiesMap map) {
    return string;
  }
}