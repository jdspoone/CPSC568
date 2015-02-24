package casa.util.unused;

/**
 * A <code>LogicalPropositionTreeException</code> object is an exception used
 * by the <code>LogicalPropositionTree</code> set of classes.
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
 * @see LogicalPropositionTree
 * @see BooleanNode
 * @see LogicalNode
 * @see PropositionNode
 * @see DataNode
 * @see PropertyNode
 * @see StringLiteralNode
 * @see NumberLiteralNode
 * @author Jason Heard
 * @version 0.9
 * @deprecated (rck) The classes BooleanNode, DataNode, LogicalNode, LogicalPropoisitonTree,
 * LogicalPropositionTreeException, LogicalPropositionTreeNode, NumberLiteralNode,
 * PropositionNode, and StringLiteralNode taken together appear to be unused in CASA.
 */

@Deprecated
public class LogicalPropositionTreeException extends Exception {
  public LogicalPropositionTreeException() {
    super ();
  }

  public LogicalPropositionTreeException (String message) {
    super (message);
  }

  public LogicalPropositionTreeException (String message, Throwable cause) {
    super (message, cause);
  }

  public LogicalPropositionTreeException (Throwable cause) {
    super (cause);
  }
}