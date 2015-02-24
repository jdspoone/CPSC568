package casa.util.unused;

import casa.util.PropertiesMap;

import java.util.*;

/**
 * A <code>LogicalPropositionTree</code> object is a boolean logical evaluator
 * with support for evaluating propositions into boolean values and evaluating
 * complex boolean expressions.  The language that it supports is below.  Every
 * expression is defined by a string.
 *
 * <ul>
 * <li>The string must be a <var>proposition</var>, a <var>logical
 * statement</var> or a <var>boolean</var>.</li>
 * <li>A <var>proposition</var> must be in one of the following forms:</li>
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
 * <li><var>c</var> and <var>d</var> must be a <var>literal string</var>, a
 * <var>literal number</var>, or a <var>variable</var>.</li>
 * <li>A <var>logical statement</var> must be in one of the following
 * forms:</li>
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
 * <li><var>a</var> and <var>b</var> must be a <var>proposition</var>, a
 * <var>logical statement</var> or a <var>boolean</var>.</li>
 * <li>A <var>literal string</var> is string in the form
 * <code>"</code><var>.*</var><code>"</code> that evaluates to the
 * <var>string</var> contained in the quotes (<code>"</code>s).</li>
 * <li>A <var>literal number</var> is string in the form
 * <var>"-"?[0-9]*"."?[0-9]*</var> that evaluates to a <var>number</var>.</li>
 * <li>A <var>variable</var> is a stored value from the given
 * <code>PropertiesMap</code> that evaluates to a <var>boolean</var>, a
 * <var>string</var> or a <var>number</var>.</li>
 * <li>A <var>boolean</var> the value <code>true</code> or the value
 * <code>false</code>.</li>
 * <li>A <var>string</var> is a character string.</li>
 * <li>A <var>number</var> is either a integer or floating point number.</li>
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
 * @see BooleanNode
 * @see LogicalNode
 * @see PropositionNode
 * @see DataNode
 * @see PropertyNode
 * @see StringLiteralNode
 * @see NumberLiteralNode
 * @see LogicalPropositionTreeException
 * @author Jason Heard
 * @version 0.9
 * @deprecated (rck) The classes BooleanNode, DataNode, LogicalNode, LogicalPropoisitonTree,
 * LogicalPropositionTreeException, LogicalPropositionTreeNode, NumberLiteralNode,
 * PropositionNode, and StringLiteralNode taken together appear to be unused in CASA.
 */

@Deprecated
public class LogicalPropositionTree {
  private BooleanNode root = null;
  private String treeString = null;

  public LogicalPropositionTree (String newTreeString) {
    if (this.treeString != newTreeString) {
      this.root = null;
    }

    this.treeString = newTreeString;
  }

  public String getTreeString () {
    return treeString;
  }

  public void setTreeString (String newTreeString) {
    if (this.treeString != newTreeString) {
      this.root = null;
    }

    this.treeString = newTreeString;
  }

  /**
   * Returns the truth value of the tree with the given context, either
   * <code>true</code> or <code>false</code>.
   *
   * @param map The context within which we are evaluating the truth value.
   * Defines the type and value of all properties in the tree during the
   * evaluation.
   * @return The truth value of the tree with the given context, either
   * <code>true</code> or <code>false</code>.
   * @throws LogicalPropositionTreeException If there was a problem determining
   * the truth value.  Usually because a property was referenced as the wrong
   * type or did not exist.
   */
  public boolean evaluate (PropertiesMap map) {
    if (root == null) {
      buildTree ();
    }

    try {
      return root.evaluate (map);
    } catch (Exception ex) {
      return false;
    }
  }

  private void buildTree () {
    StringTokenizer ted = new StringTokenizer (treeString);
    String token;

    while (ted.hasMoreTokens()) {
      token = ted.nextToken ();

      // use token
//      if (getTokenValue (token) > rootValue) {
        // recurse deeper
//      } else {
        // put on top

//      }
    }
  }
}