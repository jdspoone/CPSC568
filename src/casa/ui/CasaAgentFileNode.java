package casa.ui;

import casa.LAC;
import casa.agentCom.URLDescriptor;
import casa.util.CasaErrorMessage;

import java.io.File;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * <p>Title: CASA</p>
 * <p>Description: Constructs a Tree that is to be included in the LACTreePanel under the
 * "agent instances</p>
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
 * @author Gabriel Becerra
 */
class CasaAgentFileNode {
  //
  // Objects
  //
  protected File casaFile;
  protected boolean isActive;

  /**
   * Constructor
   * @param casaFile is the starting path for retrieving all the agents
   * @param isActive true if the agent is active (registered instance), false otherwise
   */
  public CasaAgentFileNode(File casaFile, boolean isActive) {
    this.casaFile = casaFile;
    this.isActive = isActive;
  }

  public File getFile() {
    return casaFile;
  }

  public String toString() {
    String ret = casaFile.getName().length() > 0 ? casaFile.getName() :
        casaFile.getPath();
    int end = ret.length();
    if (end<5) return ret;
    if (ret.substring(end-5,end).equals(".casa")) end -= 5;
    ret = ret.substring(0,end);
    return ret;
  }

  public boolean expand(DefaultMutableTreeNode parent, DefaultTreeModel model, boolean showInactive) {
    if (!parent.getAllowsChildren())
      return false;

    //DEBUG.PRINT("Expanding: "+getFile().getName());
    parent.removeAllChildren(); // clear children to refresh

    Vector v = getAllChildren(parent, showInactive);
    if (v!=null) {
      //Insert the ordered CASAAgentFileNodes into the parent node
      for (int i = 0, sz = v.size (); i < sz; i++) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) v.elementAt (i);
        CasaAgentFileNode nd = (CasaAgentFileNode) node.getUserObject ();
        node.setAllowsChildren (nd.getFile ().isDirectory ());
        parent.add (node);
      }
      if (model != null)
        model.reload (parent);
    }
    return true;
  }

  public DefaultMutableTreeNode verify(DefaultMutableTreeNode parent, DefaultTreeModel model, JTree tree, boolean showInactive) {
    if (!parent.getAllowsChildren()) {
      return null;
    }

    Vector expanded = new Vector();
    verify_body(parent,model,tree,showInactive,expanded);
    //DEBUG.PRINT("expanded="+expanded.toString());

    parent.removeAllChildren();
    expand(parent,model,showInactive);

    //get all the expanded paths refreshed.
    for (Iterator i = expanded.iterator(); i.hasNext(); ) {
      Vector v = (Vector)i.next();
      DefaultMutableTreeNode node = find(parent,v,1);
      if (node!=null) {
        TreePath p = new TreePath(model.getPathToRoot(node));
        tree.expandPath(p);
        //DEBUG.PRINT(v.toString());
      }
    }
    return parent;
  }

  private DefaultMutableTreeNode verify_body(DefaultMutableTreeNode parent, DefaultTreeModel model, JTree tree, boolean showInactive, Vector expanded) {
    if (!parent.getAllowsChildren())
      return null;

    //collect all the expanded nodes in the (visual) tree into 'expanded'
    for (Enumeration i=parent.children(); i.hasMoreElements(); ) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) i.nextElement();
      CasaAgentFileNode o = (CasaAgentFileNode)node.getUserObject();
      if (node.getChildCount()!=0 && o.getFile().exists()) {
        TreePath path = new TreePath(model.getPathToRoot(node));
        if (tree.isExpanded(path)) {
          int count = path.getPathCount();
          Vector v = new Vector(count);
          for (int j=0; j<count; j++) {
            v.insertElementAt(((CasaAgentFileNode)((DefaultMutableTreeNode)path.getPathComponent(j)).getUserObject()).getFile().getName(),j);
          }
          expanded.add(v);
        }
        verify_body(node,model,tree,showInactive,expanded);
      }
    }
    return parent;
  }

  private DefaultMutableTreeNode find(DefaultMutableTreeNode parent, Vector path, int level) {
      String target = (String)path.elementAt(level);
      for (Enumeration c = parent.children(); c.hasMoreElements();) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)c.nextElement();
        String name = ((CasaAgentFileNode)node.getUserObject()).getFile().getName();
        if (name.equals(target)) {
          if (level==(path.size()-1)) return node;
          else                        return find(node,path,level+1);
        }
      }
      return null;
  }

  private Vector getAllChildren(DefaultMutableTreeNode parent, boolean showInactive) {
    File files[] = listFiles();
    if (files == null)
      return null;

    Vector v = new Vector();

    //Collect and sort directories and casa files (if 'showActive' is on) from this node's directory
    for (int k = 0, sz = files.length; k < sz; k++) {
      File f = files[k];

      //skip the simple casa files if we don't want to show the inactive agents
      if (f.isDirectory() || (showInactive && isCasaFile(f))) {
        CasaAgentFileNode newNode = new CasaAgentFileNode(f, false);
        DefaultMutableTreeNode newNd = new DefaultMutableTreeNode(newNode);
        insert(newNd, v);
      }
    }

    //Collect and sort RUNNING agents int the vector
    Vector urls = null;
    try {
      urls = LAC.ProcessInfo.lac.getRunningAgentDescriptors();
      for (int i = urls.size() - 1; i >= 0; i--) {
        String path = ( (URLDescriptor) urls.elementAt(i)).getPath();
        String filepath = LAC.ProcessInfo.lac.path2file(path);
        File fn = new File(filepath);
        String s1 = fn.getParent(), s2 = casaFile.getPath();
        if (s1.equals(s2)) {
          CasaAgentFileNode newNode = new CasaAgentFileNode(fn, true);
          DefaultMutableTreeNode newNd = new DefaultMutableTreeNode(newNode);
          insert(newNd, v);
        }
      }
    }
    catch (Exception ex) {
    }
    return v;
  }


  private boolean isCasaFile(File f) {
    String fn = f.getName();
    int len = fn.length();
    return len>5 ? fn.substring(len-5,len).equals(".casa") : false;
  }

  private void insert(DefaultMutableTreeNode newNode, Vector v) {
    // Insertion sort (alphabetical order)
    boolean isAdded = false;
    for (int i=0, sz=v.size(); i < sz; i++) {
      CasaAgentFileNode n = (CasaAgentFileNode) ((DefaultMutableTreeNode)v.elementAt(i)).getUserObject();
      int comp = ((CasaAgentFileNode)((DefaultMutableTreeNode)newNode).getUserObject()).compareTo(n);
      if (comp<=0) {
        if (comp==0) v.setElementAt(newNode,i);
        else         v.insertElementAt(newNode, i);
        isAdded = true;
        break;
      }
    }
    if (!isAdded)
      v.addElement(newNode);
  }

  public int compareTo (CasaAgentFileNode toCompare) {
    return casaFile.getName().compareToIgnoreCase(toCompare.casaFile.getName());
  }

  public File[] listFiles() {
    if (!casaFile.isDirectory())
      return null;
    try {
      return casaFile.listFiles ();
    } catch (Exception ex) {
      new CasaErrorMessage ("Error", "Error reading directory " + casaFile.getAbsolutePath());
      return null;
    }
  }
}
