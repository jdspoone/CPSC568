package casa.ui;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary.
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  The
 * Knowledge Science Group makes no representations about the suitability of
 * this software for any purpose.  It is provided "as is" without express or
 * implied warranty.</p>
 *
 * @author <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

class CasaNodeRenderer
    extends DefaultTreeCellRenderer {
  ImageIcon runningAgentIcon;

  public CasaNodeRenderer() {
    runningAgentIcon = CustomIcons.FRAME_ICON;
  }

  @Override
  public Component getTreeCellRendererComponent(
      JTree tree,
      Object value,
      boolean sel,
      boolean expanded,
      boolean leaf,
      int row,
      boolean hasFocus) {

    super.getTreeCellRendererComponent(
        tree, value, sel,
        expanded, leaf, row,
        hasFocus);
    if (leaf && isRunning(value))
      setIcon(runningAgentIcon);

    return this;
  }

  private boolean isRunning(Object x) {
    CasaAgentFileNode n = null;
    try {
      DefaultMutableTreeNode d = (DefaultMutableTreeNode) x;
      n = (CasaAgentFileNode)d.getUserObject();
    }
    catch (Exception ex) {
      return false;
    }
    return n.isActive;
  }
}
