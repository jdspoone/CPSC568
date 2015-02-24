package casa.util.unused;

import casa.util.PropertiesMap;

/**
 * A <code>LogicalNode</code> object is a node in a
 * <code>LogicalPropositionTree</code> that represents a <var>logical
 * statement</var>.  It can evaluate to <code>true</code> or <code>false</code>
 * if given the context of a <code>PropertiesMap</code>.
 *
 * <li>A <var>logical statement</var> must be in one of the following
 * forms, where <var>a</var> and <var>b</var> represent a
 * <var>proposition</var>, a <var>logical statement</var> or a
 * <var>boolean</var>:</li>
 * <ul>
 * <li><var>a</var> <code>||</code> <var>b</var><br>
 * evaluates to <code>false</code> if <var>a</var> and <var>b</var> both
 * evaluate to <code>false</code>; <code>true</code> otherwise.</li>
 * <li><var>a</var> <code>&&</code> <var>b</var><br>
 * evaluates to <code>true</code> if <var>a</var> and <var>b</var> both
 * evaluate to <code>true</code>; <code>false</code> otherwise.</li>
 * <li><code>!</code> <var>b</var><br>
 * evaluates to <code>true</code> if <var>b</var> evaluates to
 * <code>false</code>; <code>false</code> otherwise.</li>
 * <li><var>a</var> <code>-&gt;</code> <var>b</var><br>
 * evaluates to <code>false</code> if <var>a</var> evaluates to
 * <code>true</code> and <var>b</var> evaluates to <code>false</code>;
 * <code>true</code> otherwise.</li>
 * <li><var>a</var> <code>&lt;-</code> <var>b</var><br>
 * evaluates to <code>false</code> if <var>a</var> evaluates to
 * <code>false</code> and <var>b</var> evaluates to <code>true</code>;
 * <code>true</code> otherwise.</li>
 * <li><var>a</var> <code>&lt;&gt;</code> <var>b</var><br>
 * evaluates to <code>true</code> if <var>a</var> and <var>b</var> both
 * evaluate to the same truth value; <code>false</code> otherwise.</li>
 * </ul>
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
public class LogicalNode implements BooleanNode {
  public static final String OR_OPERATOR = "||";
  public static final String AND_OPERATOR = "&&";
  public static final String NOT_OPERATOR = "!";
  public static final String RIGHT_OPERATOR = "->";
  public static final String LEFT_OPERATOR = "<-";
  public static final String UNARY_OPERATOR = "<>";

  private BooleanNode a = null;
  private BooleanNode b = null;
  private String operator = null;

  public LogicalNode() {
  }

  public LogicalNode (BooleanNode newA, BooleanNode newB, String newOperator) throws
      LogicalPropositionTreeException {
    this.a = newA;
    this.b = newB;
    setOperator (newOperator);
  }

  public BooleanNode getA () {
    return a;
  }

  public void setA (BooleanNode newA) {
    this.a = newA;
  }

  public BooleanNode getB () {
    return b;
  }

  public void setB (BooleanNode newB) {
    this.b = newB;
  }

  public String getOperator () {
    return operator;
  }

  public void setOperator (String newOperator) throws
      LogicalPropositionTreeException {
    if (operator.equals (OR_OPERATOR) || operator.equals (AND_OPERATOR) ||
        operator.equals (NOT_OPERATOR) || operator.equals (RIGHT_OPERATOR) ||
        operator.equals (LEFT_OPERATOR) || operator.equals (UNARY_OPERATOR)) {
      this.operator = newOperator;
    } else {
      throw new LogicalPropositionTreeException ("Operator is not valid: " + newOperator + "!");
    }
  }

  /**
   * Returns the truth value of this node with the given context, either
   * <code>true</code> or <code>false</code>.
   *
   *
   * @param map The context within which we are evaluating the truth value.
   * Defines the type and value of all properties in the tree during the
   * evaluation.
   * @return The truth value of this node with the given context, either
   * <code>true</code> or <code>false</code>.
   * @throws LogicalPropositionTreeException If there was a problem determining
   * the truth value.  This can happen if <var>a</var> or <var>b</var> is
   * <code>null</code>, if <code>evaluate()</code> throws an exception for
   * <var>a</var> or <var>b</var>, <code>operator</code> is <code>null</code>,
   * or <code>operator</code> is set to an invalid operator.
   */
  public boolean evaluate (PropertiesMap map) throws LogicalPropositionTreeException {
    if (operator == null) {
      throw new LogicalPropositionTreeException ("Operator is not set!");
    }

    if (operator.equals (NOT_OPERATOR)) {
      if (b == null) {
        throw new LogicalPropositionTreeException ("b is null!");
      }
      if (!b.evaluate (map)) {
        return true;
      }
      return false;
    }

    if (a == null) {
      if (b == null) {
        throw new LogicalPropositionTreeException ("a and b are null!");
      } else {
        throw new LogicalPropositionTreeException ("a is null!");
      }
    } else if (b == null) {
      throw new LogicalPropositionTreeException ("b is null!");
    }

    if (operator.equals (OR_OPERATOR)) {
      if (a.evaluate (map)) {
        return true;
      } else if (b.evaluate (map)) {
        return true;
      }
      return false;
    } else if (operator.equals (AND_OPERATOR)) {
      if (!a.evaluate (map)) {
        return false;
      } else if (!b.evaluate (map)) {
        return false;
      }
      return true;
    } else if (operator.equals (RIGHT_OPERATOR)) {
      if (a.evaluate (map) && !b.evaluate (map)) {
        return false;
      }
      return true;
    } else if (operator.equals (LEFT_OPERATOR)) {
      if (!a.evaluate (map) && b.evaluate (map)) {
        return false;
      }
      return true;
    } else if (operator.equals (UNARY_OPERATOR)) {
      if (a.evaluate (map) == b.evaluate (map)) {
        return true;
      }
      return false;
    }

    throw new LogicalPropositionTreeException ("Operator '" + operator + "' is not valid!");
  }
}