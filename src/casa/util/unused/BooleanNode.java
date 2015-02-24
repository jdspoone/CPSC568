package casa.util.unused;

import casa.util.PropertiesMap;

/**
 * A <code>BooleanNode</code> object is a node in a
 * <code>LogicalPropositionTree</code> that represents a <var>boolean</var>.
 * It can evaluate to <code>true</code> or <code>false</code> if given the
 * context of a <code>PropertiesMap</code>.
 *
 * A <var>boolean</var> the value <code>true</code> or the value
 * <code>false</code>.
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
public abstract interface BooleanNode {
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
  public boolean evaluate (PropertiesMap map) throws LogicalPropositionTreeException;
}