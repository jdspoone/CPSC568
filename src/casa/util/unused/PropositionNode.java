package casa.util.unused;

import casa.util.PropertiesMap;

import java.util.regex.Pattern;

/**
 * A <code>PropositionNode</code> object is a node in a
 * <code>LogicalPropositionTree</code> that represents a
 * <var>proposition</var>.  It can evaluate to <code>true</code> or
 * <code>false</code> if given the context of a <code>PropertiesMap</code>.
 *
 * A <var>proposition</var> must be in one of the following forms, where
 * <var>c</var> and <var>d</var> represent a <var>literal string</var>, a
 * <var>literal number</var>, or a <var>variable</var>:
 * <ul>
 * <li><var>c</var> <code>==</code> <var>d</var><br>
 * evaluates to <code>true</code> if <var>c</var> and <var>d</var> are
 * identical; <code>false</code> otherwise.</li>
 * <li><var>c</var> <code>!=</code> <var>d</var><br>
 * evaluates to <code>false</code> if <var>c</var> and <var>d</var> are
 * identical; <code>true</code> otherwise.</li>
 * <li><var>c</var> <code>&lt;</code> <var>d</var><br>
 * evaluates to <code>true</code> if <var>c</var> and <var>d</var> are both a
 * <var>number</var> and <var>c</var> is less than <var>d</var> or if
 * <var>c</var> and <var>d</var> are both <var>string</var>s and <var>c</var>
 * is lexigraphically before <var>d</var>; <code>false</code> otherwise.</li>
 * <li><var>c</var> <code>&lt;=</code> <var>d</var><br>
 * evaluates to <var>c</var> <code>&lt;</code> <var>d</var> <code>||</code>
 * <var>c</var> <code>==</code> <var>d</var>.</li>
 * <li><var>c</var> <code>&gt;</code> <var>d</var><br>
 * evaluates to <code>true</code> if <var>c</var> and <var>d</var> are both a
 * <var>number</var> and <var>c</var> is greater than <var>d</var> or if
 * <var>c</var> and <var>d</var> are both <var>string</var>s and <var>c</var>
 * is lexigraphically after <var>d</var>; <code>false</code> otherwise.</li>
 * <li><var>c</var> <code>&gt;=</code> <var>d</var><br>
 * evaluates to <var>c</var> <code>&gt;</code> <var>d</var> <code>||</code>
 * <var>c</var> <code>==</code> <var>d</var>.</li>
 * <li><var>c</var> <code>=re</code> <var>d</var><br>
 * evaluates to <code>true</code> if <var>c</var> is a <var>string</var> that
 * matches the <var>regular expression</var> <var>d</var>; <code>false</code>
 * otherwise.</li>
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
public class PropositionNode implements BooleanNode {
  public static final String EQUALS_OPERATOR = "==";
  public static final String NOT_EQUAL_OPERATOR = "!=";
  public static final String LESS_THAN_OPERATOR = "<";
  public static final String LESS_THAN_OR_EQUAL_OPERATOR = "<=";
  public static final String GREATER_THAN_OPERATOR = ">";
  public static final String GREATER_THAN_OR_EQUAL_OPERATOR = ">=";
  public static final String EQUALS_REGULAR_EXPRESSION_OPERATOR = "=re";

  private DataNode c = null;
  private DataNode d = null;
  private String operator = null;
  private Pattern pattern = null;

  public PropositionNode() {
  }

  public PropositionNode (DataNode newC, DataNode newD, String newOperator) throws
      LogicalPropositionTreeException {
    this.c = newC;
    this.d = newD;
    setOperator (newOperator);
  }

  public DataNode getC () {
    return c;
  }

  public void setC (DataNode newC) {
    this.c = newC;
  }

  public DataNode getD () {
    return d;
  }

  public void setD (DataNode newD) {
    this.d = newD;
    pattern = null;
  }

  public String getOperator () {
    return operator;
  }

  public void setOperator (String newOperator) throws
      LogicalPropositionTreeException {
    if (operator.equals (EQUALS_OPERATOR) ||
        operator.equals (NOT_EQUAL_OPERATOR) ||
        operator.equals (LESS_THAN_OPERATOR) ||
        operator.equals (LESS_THAN_OR_EQUAL_OPERATOR) ||
        operator.equals (GREATER_THAN_OPERATOR) ||
        operator.equals (GREATER_THAN_OR_EQUAL_OPERATOR) ||
        operator.equals (EQUALS_REGULAR_EXPRESSION_OPERATOR)) {
      this.operator = newOperator;
    } else {
      throw new LogicalPropositionTreeException ("Operator is not valid: " + newOperator + "!");
    }
  }

  /**
   * Returns the truth value of this node with the given context, either
   * <code>true</code> or <code>false</code>.
   *
   * @param map The context within which we are evaluating the truth value.
   * Defines the type and value of all properties in the tree during the
   * evaluation.
   * @return The truth value of this node with the given context, either
   * <code>true</code> or <code>false</code>.
   * @throws LogicalPropositionTreeException If there was a problem determining
   * the truth value.  Usually this happens because a property was referenced
   * as the wrong type or did not exist.
   */
  public boolean evaluate (PropertiesMap map) throws LogicalPropositionTreeException {
    if (operator == null) {
      throw new LogicalPropositionTreeException ("Operator is not set!");
    }

    if (c == null) {
      if (d == null) {
        throw new LogicalPropositionTreeException ("c and d are null!");
      } else {
        throw new LogicalPropositionTreeException ("c is null!");
      }
    } else if (d == null) {
      throw new LogicalPropositionTreeException ("d is null!");
    }

    if (operator.equals (EQUALS_OPERATOR)) {
      if (c.isANumber(map) && d.isANumber(map)) {
        if (c.getNumber (map).compareTo (d.getNumber (map)) == 0) {
          return true;
        }
      }

      if (c.isAString(map) && d.isAString(map)) {
        if (c.getString (map).equals (d.getString (map))) {
          return true;
        }
      }

      return false;
    } else if (operator.equals (NOT_EQUAL_OPERATOR)) {
      if (c.isANumber(map) && d.isANumber(map)) {
        if (c.getNumber (map).compareTo (d.getNumber (map)) == 0) {
          return false;
        }
      }

      if (c.isAString(map) && d.isAString(map)) {
        if (c.getString (map).equals (d.getString (map))) {
          return false;
        }
      }

      return true;
    } else if (operator.equals (LESS_THAN_OPERATOR)) {
      if (c.isANumber(map) && d.isANumber(map)) {
        if (c.getNumber (map).compareTo (d.getNumber (map)) < 0) {
          return true;
        }
      }

      if (c.isAString(map) && d.isAString(map)) {
        if (c.getString (map).compareTo (d.getString (map)) < 0) {
          return true;
        }
      }

      return false;
    } else if (operator.equals (LESS_THAN_OR_EQUAL_OPERATOR)) {
      if (c.isANumber(map) && d.isANumber(map)) {
        if (c.getNumber (map).compareTo (d.getNumber (map)) <= 0) {
          return true;
        }
      }

      if (c.isAString(map) && d.isAString(map)) {
        if (c.getString (map).compareTo (d.getString (map)) <= 0) {
          return true;
        }
      }

      return false;
    } else if (operator.equals (GREATER_THAN_OPERATOR)) {
      if (c.isANumber(map) && d.isANumber(map)) {
        if (c.getNumber (map).compareTo (d.getNumber (map)) > 0) {
          return true;
        }
      }

      if (c.isAString(map) && d.isAString(map)) {
        if (c.getString (map).compareTo (d.getString (map)) > 0) {
          return true;
        }
      }

      return false;
    } else if (operator.equals (GREATER_THAN_OR_EQUAL_OPERATOR)) {
      if (c.isANumber(map) && d.isANumber(map)) {
        if (c.getNumber (map).compareTo (d.getNumber (map)) >= 0) {
          return true;
        }
      }

      if (c.isAString(map) && d.isAString(map)) {
        if (c.getString (map).compareTo (d.getString (map)) >= 0) {
          return true;
        }
      }

      return false;
    } else if (operator.equals (EQUALS_REGULAR_EXPRESSION_OPERATOR)) {
      if (c.isAString (map) && d.isAString (map)) {
        if (pattern == null) {
          pattern = Pattern.compile (d.getString (map));
        }

        if (pattern.matcher (c.getString (map)).matches ()) {
          return true;
        }
      }

      return false;
    }

    throw new LogicalPropositionTreeException ("Operator '" + operator + "' is not valid!");
  }
}