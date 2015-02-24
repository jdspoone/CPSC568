package casa.io.tools;


import javax.swing.JTree;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import casa.io.CASAFile;
import casa.io.CASAIOException;

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
 * @author Jason Heard
 */

public class CASAFileTree extends JTree {
  public CASAFileTree () {
    super (new CASAFileTreeModel ());
    initializeTree ();
  }

  public CASAFileTree (CASAFile file) throws CASAIOException {
    super (new CASAFileTreeModel ());
    initializeTree ();

    setFile (file);
  }

  private void initializeTree () {
    putClientProperty ("JTree.lineStyle", "Angled");
    setEnabled (false);
    super.getSelectionModel ().setSelectionMode (TreeSelectionModel.SINGLE_TREE_SELECTION);
    super.setCellRenderer (new CASAFileTreeCellRenderer ());
  }

  public void setFile (CASAFile file) throws CASAIOException {
    CASAFileTreeModel model = (CASAFileTreeModel) getModel ();
    model.setFile (file);
    setEnabled (true);
  }

  public CASAFile getFile () {
    CASAFileTreeModel model = (CASAFileTreeModel) getModel ();
    return model.getFile ();
  }

  public void fileChanged () {
    CASAFileTreeModel model = (CASAFileTreeModel) getModel ();
    model.fireTreeStructureChanged ();
  }
}

class CASAFileTreeSelectionModel extends DefaultTreeSelectionModel {
  public void addSelectionPaths (TreePath[] paths) {
    /**@todo Override this javax.swing.tree.DefaultTreeSelectionModel method*/
    super.setSelectionPath (paths[0]);
  }
  public void addSelectionPath (TreePath path) {
    super.setSelectionPath (path);
  }
  public void setSelectionPaths (TreePath[] paths) {
    super.setSelectionPath (paths[0]);
  }
}