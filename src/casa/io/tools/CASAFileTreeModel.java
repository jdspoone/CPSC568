package casa.io.tools;

import java.io.IOException;
import java.util.Vector;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import casa.io.CASAFile;
import casa.io.CASAFileIndexEntry;
import casa.io.CASAIOException;

/**
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 * @author  Jason Heard
 */

public class CASAFileTreeModel implements TreeModel {
  private Vector treeModelListeners = new Vector ();
  /**
	 */
  private CASAFile file;

  public CASAFileTreeModel () {
    this.file = null;
  }

  public CASAFileTreeModel (CASAFile file) throws CASAIOException {
    if (!file.isCASAFile ()) {
      this.file = null;
      throw new CASAIOException ("Not a valid CASA file, cannot create tree.");
    }

    this.file = file;
    fireTreeStructureChanged ();
  }

  /**
	 * @param file
	 * @throws CASAIOException
	 */
  public void setFile (CASAFile file) throws CASAIOException {
    if (!file.isCASAFile ()) {
      throw new CASAIOException ("Not a valid CASA file, cannot create tree.");
    }

    this.file = file;
    fireTreeStructureChanged ();
  }

  /**
	 * @return
	 */
  public CASAFile getFile () {
    return file;
  }

  /**
   * Returns the root of the tree.
   */
  public Object getRoot () {
    if (file == null) {
      return new String ("");
    }
    return file;
  }

  /**
   * Returns the child of parent at index index in the parent's child array.
   */
  public Object getChild (Object parent, int index) {
    if (CASAFile.class.isInstance (parent)) {
      CASAFile tempFile = (CASAFile) parent;

      if (index == 0) {
        return "Full Path: \"" + tempFile.getAbsolutePath () + "\"";
      } else if (index == 1) {
        return "Size: " + tempFile.length ();
      } else if (index == 2) {
        Vector tempList = null;
        Vector newList = new Vector ();
        try {
          tempList = tempFile.getIndex ().getEntries ();
        } catch (IOException ex) {
          return null;
        }

        for (int i = 0; i < tempList.size (); i++) {
          newList.add (new CASAFileTreeIndexEntry ((CASAFileIndexEntry)
              tempList.get (i), tempFile));
        }

        return new CASAFileTreeNodeList (newList);
      }
    } else if (Vector.class.isInstance (parent)) {
      Vector tempList = (Vector) parent;
      if (index >= tempList.size ()) {
        return null;
      } else {
        return tempList.get (index);
      }
    } else if (CASAFileTreeIndexEntry.class.isInstance (parent)) {
      CASAFileTreeIndexEntry tempEntry = (CASAFileTreeIndexEntry) parent;
      return tempEntry.get (index);
    } else if (CASAFileTreeData.class.isInstance (parent)) {
      CASAFileTreeData tempData = (CASAFileTreeData) parent;
      return tempData.get (index);
    } else if (CASAFileTreeProperty.class.isInstance (parent)) {
      CASAFileTreeProperty tempProp = (CASAFileTreeProperty) parent;
      return tempProp.getValue ();
    }

    // No Children
    return null;
  }

  /**
   * Returns the number of children of parent.
   */
  public int getChildCount (Object parent) {
    if (CASAFile.class.isInstance (parent)) {
      return 3;
    } else if (Vector.class.isInstance (parent)) {
      Vector tempList = (Vector) parent;
      return tempList.size ();
    } else if (CASAFileTreeIndexEntry.class.isInstance (parent)) {
      CASAFileTreeIndexEntry tempEntry = (CASAFileTreeIndexEntry) parent;
      return tempEntry.size ();
    } else if (CASAFileTreeData.class.isInstance (parent)) {
      CASAFileTreeData tempData = (CASAFileTreeData) parent;
      return tempData.size ();
    } else if (CASAFileTreeProperty.class.isInstance (parent)) {
      return 1;
    }

    // No Children
    return 0;
  }

  /**
   * Returns true if node is a leaf.
   */
  public boolean isLeaf (Object node) {
    return!(CASAFile.class.isInstance (node) ||
            Vector.class.isInstance (node) ||
            CASAFileTreeIndexEntry.class.isInstance (node) ||
            CASAFileTreeData.class.isInstance (node) ||
            CASAFileTreeProperty.class.isInstance (node));
  }

  /**
   * Messaged when the user has altered the value for the item identified by
   * path to newValue.  Should not used by this model yet.
   */
  public void valueForPathChanged (TreePath path, Object newValue) {
    ; // nothing ... for now
  }

  /**
   * Returns the index of child in parent.
   */
  public int getIndexOfChild (Object parent, Object child) {
    if (parent == null || child == null) {
      return -1;
    }
    if (CASAFile.class.isInstance (parent)) {
      if (String.class.isInstance (child)) {
        String tempStr = (String) child;
        if (tempStr.charAt(0) == 'F') {
          return 0;
        } else if (tempStr.charAt(0) == 'S') {
          return 1;
        }
      } else if (CASAFileTreeNodeList.class.isInstance (child)) {
        return 2;
      }
    } else if (Vector.class.isInstance (parent)) {
      Vector tempList = (Vector) parent;
      tempList.indexOf (child);
    } else if (CASAFileTreeIndexEntry.class.isInstance (parent)) {
      CASAFileTreeIndexEntry tempEntry = (CASAFileTreeIndexEntry) parent;
      return tempEntry.indexOf (child);
    } else if (CASAFileTreeData.class.isInstance (parent)) {
      CASAFileTreeData tempData = (CASAFileTreeData) parent;
      return tempData.indexOf (child);
    } else if (CASAFileTreeProperty.class.isInstance (parent)) {
      return 0;
    }

    // No Children
    return -1;
  }

  /**
   * Adds a listener for the TreeModelEvent posted after the tree changes.
   */
  public void addTreeModelListener (TreeModelListener l) {
    treeModelListeners.addElement (l);
  }

  /**
   * Removes a listener previously added with addTreeModelListener().
   */
  public void removeTreeModelListener (TreeModelListener l) {
    treeModelListeners.removeElement (l);
  }

  /**
   * This event is fired when the entire tree has been changed.
   */
  protected void fireTreeStructureChanged () {
    int len = treeModelListeners.size ();
    TreeModelEvent e = new TreeModelEvent (this, new TreePath (file));

    for (int i = 0; i < len; i++) {
      ((TreeModelListener) treeModelListeners.elementAt (i)).
          treeStructureChanged (e);
    }
  }

  /**
   * This event is fired when only a portion of the tree is modified.
   */
  protected void fireTreeStructureChanged (TreePath path) {
    int len = treeModelListeners.size ();
    TreeModelEvent e = new TreeModelEvent (this, path);

    for (int i = 0; i < len; i++) {
      ((TreeModelListener) treeModelListeners.elementAt (i)).
          treeStructureChanged (e);
    }
  }
}
