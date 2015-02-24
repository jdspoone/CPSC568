package casa.ui;

import casa.EventNotificationURL;
import casa.LAC;
import casa.LACOptions;
import casa.ML;
import casa.ObserverNotification;
import casa.agentCom.URLDescriptor;
import casa.interfaces.TransientAgentInterface;
import casa.io.CASAFile;
import casa.io.CASAFilePropertiesMap;
import casa.util.CASAUtil;
import casa.util.Trace;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.EventListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 * @author  Gabriel Becerra
 */

public class CasaAgentTree extends JPanel implements Observer {
  //
  // Tree
  //
  protected JTree tree;
  protected DefaultTreeModel model;
  protected File dir;
  //private boolean showInactive=true;
  /**
	 */
  LACOptions options;
  //private boolean createProxyWindows=false;
  protected DefaultMutableTreeNode treetop;

  //Popup menus
  protected JPopupMenu  activePopUp;
  protected JPopupMenu  inactivePopUp;
  protected JPopupMenu  typePopUp;
  protected AbstractAction expandOrCollapseNodeAction;
  protected TreePath clickedPath = null;

  /**
	 */
  private MDIDesktopPane localDesktop;
  private int portToTry = 6100;

  /**
   * Creates a JTree with nodes representing the agents
   */
  public CasaAgentTree(LACOptions optionsRef, MDIDesktopPane localDesktop) {
    this.localDesktop = localDesktop;
    //this.showInactive = showInactive;
    options = optionsRef;
    this.dir = new File("casa");
    DefaultMutableTreeNode node;

    makePopups();

    if (LAC.ProcessInfo.lac==null) {
      treetop = new DefaultMutableTreeNode("<error...>");
      model = new DefaultTreeModel (treetop);
      tree = new JTree (model);
    }
    else {
    String root = LAC.ProcessInfo.lac.getRoot();
    while (root==null) {
      try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
      root = LAC.ProcessInfo.lac.getRoot();
    }
    CasaAgentFileNode cn = new CasaAgentFileNode(new File(root),false);
    treetop = new DefaultMutableTreeNode(cn);

    model = null;
    // Creates a treenode that holds file info
    cn.expand(treetop,model,getShowInactive());

    // Tree with top node as the root
    model = new DefaultTreeModel (treetop);
    model.setAsksAllowsChildren(true);
    tree = new JTree (model);

    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.putClientProperty("JTree.lineStyle", "Angled");
    tree.addTreeExpansionListener(new DirExpansionListener());
    tree.addTreeSelectionListener(new DirSelectionListener());
    tree.setShowsRootHandles(true);
    tree.setEditable(false);
    tree.setRootVisible(false);
    tree.setCellRenderer(new CasaNodeRenderer());
    tree.setExpandsSelectedPaths(true);
    tree.addMouseListener (new PopupTrigger ());

    JScrollPane s = new JScrollPane();
    s.getViewport().add(tree);
    s.setBorder (BorderFactory.createLineBorder (Color.blue));
    add (s);
    }
  }

  private void makePopups() {
    JMenuItem temp;

    //typePopup*****************************************************************
    typePopUp = new JPopupMenu("Type");
    //Expand/Collapse
    expandOrCollapseNodeAction = new AbstractAction("Expand/Collapse") {
      public void actionPerformed(ActionEvent event) {
        if (clickedPath == null) return;
        if (tree.isExpanded(clickedPath)) tree.collapsePath(clickedPath);
        else                              tree.expandPath(clickedPath);
      }};
    typePopUp.add(expandOrCollapseNodeAction);

    //typePopUp.addSeparator();

    //new...
    typePopUp.add(new AbstractAction("new agent...") {
      public void actionPerformed(ActionEvent event) {
        if (clickedPath == null)
          return;
        String path = pathToString(clickedPath);
        String className = path.replaceAll("/",".");
        new CreateAgentDialog (LAC.ProcessInfo.desktop.getJFrame(), LAC.ProcessInfo.lac, "Create New "+className+" Agent", true,
                               LAC.ProcessInfo.lac.getURL().getUser()+"$"+(path.substring(path.lastIndexOf('/')+1,path.length())),
                               portToTry++, LAC.ProcessInfo.lacPort, className, "");
        ((CasaAgentFileNode)treetop.getUserObject()).verify(treetop, model, tree, getShowInactive());
      }
    });

    //edit
    typePopUp.add(new AbstractAction("edit type...") {
      public void actionPerformed(ActionEvent event) {
        if (clickedPath == null)
          return;
        String path = pathToString(clickedPath);
        new AgentTypeDialog(LAC.ProcessInfo.desktop.getJFrame(),LAC.ProcessInfo.lac,"Edit type for "+path,true,path);
        ((CasaAgentFileNode)treetop.getUserObject()).verify(treetop, model, tree, getShowInactive());
      }
    });

    //delete
    typePopUp.add(new AbstractAction("delete") {
      public void actionPerformed(ActionEvent event) {
        if (clickedPath == null)
          return;
        String path = pathToString(clickedPath);
        //LAC.LACinfo.lac.deleteFile(path);
        LAC.ProcessInfo.lac.unregisterAgentType(path);
        ((CasaAgentFileNode)treetop.getUserObject()).verify(treetop, model, tree, getShowInactive());
      }
    });

    //activePopup**************************************************************
    activePopUp = new JPopupMenu("Active agent");
    //bring to front
    activePopUp.add(new AbstractAction("bring to front") {
      public void actionPerformed(ActionEvent event) {
        if (clickedPath == null)
          return;
        FrameAndAgent fa = findFrame(clickedPath);
        AbstractInternalFrame frameTemp = fa!=null?fa.frame:null;
        if (frameTemp != null) {
//          if (! frameTemp.isVisible()) frameTemp.setVisible(true);
          if (frameTemp.isIcon ()) {
              frameTemp.setIcon (false);
          }
          else frameTemp.toFront();
            frameTemp.setSelected(true);
        }
      }
    });

    //kill
    activePopUp.add(new AbstractAction("kill") {
      public void actionPerformed(ActionEvent event) {
        if (clickedPath == null)
          return;
        FrameAndAgent fa = findFrame(clickedPath);
        if (fa!=null) {
          TransientAgentInterface agent = fa.agent;
          if (agent != null) {
            agent.exit ();
          }
        }
      }
    });

    //inspect persistent data
    activePopUp.add(new InspectDataAction("inspect data"));

    //inactivePopup************************************************************
    inactivePopUp = new JPopupMenu("Inactive agent");
    //invoke...
    inactivePopUp.add(new AbstractAction("invoke...") {
      public void actionPerformed(ActionEvent event) {
        if (clickedPath == null)
          return;
        String path = pathToString(clickedPath);
        String agentName = path.substring(path.lastIndexOf('/')+1,path.lastIndexOf(".casa"));
        String className = path.substring(0,path.lastIndexOf(agentName+".casa")-1).replaceAll("/",".");
        int port = 0;
        try { //try to determine the orignal port.
            String file = LAC.ProcessInfo.lac.path2file(path);
            CASAFile f = new CASAFile (file);
            CASAFilePropertiesMap m = new CASAFilePropertiesMap (f);
            port = m.getInteger ("port");
          } catch (Exception ex) {}
        new CreateAgentDialog (LAC.ProcessInfo.desktop.getJFrame(), LAC.ProcessInfo.lac, "Invoke Agent "+agentName, true,
                               agentName, port==0?portToTry++:port, LAC.ProcessInfo.lacPort, className, "");
        ((CasaAgentFileNode)treetop.getUserObject()).verify(treetop, model, tree, getShowInactive());
      }
    });

    //delete
    inactivePopUp.add(new AbstractAction("delete") {
      public void actionPerformed(ActionEvent event) {
        if (clickedPath == null)
          return;
        FrameAndAgent a = findFrame(clickedPath);
        if (a!=null) {
        	Trace.log("error", "Cannot delete active agent '"+a.agent.getAgentName()+"'");
        }
        else {
          String path = pathToString(clickedPath);
          LAC.ProcessInfo.lac.deleteFile(path);
          ((CasaAgentFileNode)treetop.getUserObject()).verify(treetop, model, tree, getShowInactive());
        }
      }
    });

    //inspect persistent data
    inactivePopUp.add(new InspectDataAction("inspect data"));
  }

  public class InspectDataAction extends AbstractAction {
    public InspectDataAction(String label) {
      super(label);
    }
    public void actionPerformed(ActionEvent event) {
      if (clickedPath == null)
        return;
      String path = pathToString(clickedPath);
      final String file = LAC.ProcessInfo.lac.path2file(path);
      try {
        Runtime.getRuntime ().exec ("casaw /i" + file);
      } catch (IOException ex) {
      	AbstractInternalFrame.runInEventDispatchThread(new Runnable () {
            public void run () {
              casa.io.tools.CASAFileInspector.main(new String[] {file});
            }
          });
      }
    }
  } //class InspectDataAction

  public void setDesktopObject (MDIDesktopPane foreignDesktop) {
    this.localDesktop = foreignDesktop;
  }
  private MDIDesktopPane getDesktopObject () {
    return this.localDesktop;
  }

  // gets the treenode at the end of the path
  DefaultMutableTreeNode getTreeNode (TreePath path) {
    return (DefaultMutableTreeNode) path.getLastPathComponent();
  }

  // gets the file node from a treenode
  CasaAgentFileNode getFileNode (DefaultMutableTreeNode node) {
    if (node == null)
      return null;
    Object obj = node.getUserObject();
    if (obj instanceof CasaAgentFileNode) {
      return (CasaAgentFileNode) obj;
    }
    else
      return null;
  }

  /**
   *
   * <p>Title: CASA</p>
   * <p>Description: Making sure that the expansion is threaded and updating the tree model
   * only occurs within the event dispatching thread</p>
   * <p>Copyright: Copyright 2003-2014, University of Calgary</p>
   * @author Gabriel Becerra
   * @version 0.9
   */
  class DirExpansionListener implements TreeExpansionListener {
    public void treeExpanded (TreeExpansionEvent event) {
      final DefaultMutableTreeNode node = getTreeNode(event.getPath());
      final CasaAgentFileNode casaNode = getFileNode(node);
      if (casaNode != null) casaNode.expand(node,model,getShowInactive());
    }

    public void treeCollapsed (TreeExpansionEvent event) {
      final DefaultMutableTreeNode node = getTreeNode(event.getPath());
      final CasaAgentFileNode casaNode = getFileNode(node);
//      DEBUG.PRINT("Tree collapsed: "+casaNode.getFile().getName());
    }
  }

  class DirSelectionListener implements TreeSelectionListener {
    public void valueChanged (TreeSelectionEvent event) {
      DefaultMutableTreeNode node = getTreeNode (event.getPath());
      CasaAgentFileNode casaNode = getFileNode(node);
    }
  }

  public void refresh() {
    ((CasaAgentFileNode) treetop.getUserObject ()).verify (treetop, model,
        tree, getShowInactive());
  }

 /**
  * <p>Title: update</p>
  * <p>Description: Creates new interface windows for remote agents</p>
  * <p>Copyright: Copyright 2009</p>
  * <p>Company: Knowledge Science Group, University of Calgary</p>
  * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
  * @version 0.9
  */
  
  public void update (Observable observable, final Object argument) {
    String event = (argument instanceof ObserverNotification) ? 
    		((ObserverNotification)argument).getType(): null;
    		
	if (event == null) return; //No NullPointerExceptions
    if (event.equals(ML.EVENT_REGISTER_INSTANCE_REMOTE)){

    	if (options!=null && options.createProxyWindows) {
            String cls = observable.getClass().getName();

            if (argument instanceof EventNotificationURL) {
              final URLDescriptor remoteURL = ((EventNotificationURL) argument).getURL ();
              if (event.equals(ML.EVENT_REGISTER_INSTANCE_REMOTE)) {
                javax.swing.SwingUtilities.invokeLater (new Runnable () {
                  public void run () {
                    try {
                      AgentRemoteProxyWindow w = new AgentRemoteProxyWindow (remoteURL);
                      LAC.ProcessInfo.desktop.addAgentWindow (w);
                      w.setSize (500, 500);
                      w.setName ("Chat Agent");
                      w.setOpaque (true);
                      w.pack ();
                      w.show ();
                    } catch (Exception ex) {
                    	Trace.log("error", "CasaAgentTree.update(" + remoteURL.getPath () +
                                   "): Could not start remote proxy window", ex);
                    }
                  }
                });
              }
            }
          }
    }
    
    if (event.equals(ML.EVENT_REGISTER_INSTANCE_REMOTE)||
    		event.equals(ML.EVENT_REGISTER_INSTANCE_LOCAL) ||
    		event.equals(ML.EVENT_UNREGISTER_INSTANCE))
    	refresh();

    /** @todo adding AgentTypes into the Tree */
  }

  public boolean setShowInactive(boolean showInactive) {
    //boolean ret = this.showInactive;
    //this.showInactive = showInactive;
    boolean ret = options==null?false:options.showInactiveAgents;
    if (options!=null) 
    	options.showInactiveAgents = showInactive;
    refresh();
    return ret;
  }

  public boolean getShowInactive() {
    //return showInactive;
    return options==null?false:options.showInactiveAgents;
  }

  public boolean setCreateProxyWindows(boolean create) {
    //boolean ret = this.createProxyWindows;
    //this.createProxyWindows = create;
    boolean ret = options==null?false:options.createProxyWindows;
    if (options!=null)
    	options.createProxyWindows = create;
    return ret;
  }

  public boolean getCreateProxyWindows() {
    return options==null?false:options.createProxyWindows;
  }

  /**
	 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
	 */
  protected class FrameAndAgent {
    public FrameAndAgent(TransientAgentInternalFrame frame, TransientAgentInterface agent, String path) {
      this.agent = agent;
      this.frame = frame;
      this.path  = path;
    }
    /**
		 */
    public TransientAgentInterface agent;
    /**
		 */
    public TransientAgentInternalFrame frame;
    public String path;
  }

  //build a name string from path
  static protected String pathToString(TreePath path) {
    String name = new String ();
    for (int i = 1, sz = path.getPathCount (); i < sz; i++) {
      name += (i == 1 ? "" : "/")
            + ((CasaAgentFileNode)((DefaultMutableTreeNode)path.getPathComponent(i)).getUserObject()).getFile().getName ();
    }
    return name;
  }

  protected FrameAndAgent findFrame(String pathname) {
    //search for the frame who's agent path matches the name
    JInternalFrame[] componentsArray = getDesktopObject ().getAllFrames ();
    for (int i=componentsArray.length-1; i>=0; i--) {
      try {
        EventListener el[] = componentsArray[i].getListeners(InternalFrameListener.class);
        TransientAgentInterface agent = ( (TransientAgentInternalFrame)el[0]).getTransientAgent();
        URLDescriptor url = agent.getURL();
        if ((url.getPath()/*+".casa"*/).equals(pathname)) {
          return new FrameAndAgent((TransientAgentInternalFrame)el[0],agent,pathname);
        }
      }
      catch (Exception ex1) {
      	CASAUtil.log("error", "AccessControlAgentWindow.findFrame: unexpected exception", ex1, true);
      }
    }
    Trace.log("error", "AccessControlAgentWindow.findFrame: did not find frame");
    return null;
  }

  protected FrameAndAgent findFrame(TreePath path) {
    String name = pathToString(path);
    name = name.substring(0,name.lastIndexOf(".casa"));
    return findFrame(name);
  }
  
//
// Allows for a "dynamic" popup menu
//
class PopupTrigger extends MouseAdapter {
	
	protected void doPopup(MouseEvent event) {
    if (event.isPopupTrigger ()) {
      int x = event.getX ();
      int y = event.getY ();

      clickedPath = tree.getPathForLocation (x, y);
      if (clickedPath != null) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)clickedPath.getLastPathComponent();
        if (node.getAllowsChildren()) { //a type (directory)********************
          if (tree.isExpanded(clickedPath))
            expandOrCollapseNodeAction.putValue(Action.NAME, "Collapse");
          else
            expandOrCollapseNodeAction.putValue(Action.NAME, "Expand");
          typePopUp.show(tree, x, y);
        } else {
          CasaAgentFileNode cf = (CasaAgentFileNode)node.getUserObject();
          if (cf.isActive) { //an active agent *********************************
            activePopUp.show(tree,x,y);
          } else { //an inactive agent *****************************************
            inactivePopUp.show(tree,x,y);
          }
        }
      }
    }

	}
  @Override
	public void mouseReleased (MouseEvent event) {
  	doPopup(event);
  }
  @Override
	public void mousePressed (MouseEvent event) {
  	doPopup(event);
  }
  @Override
	public void mouseClicked (MouseEvent event) {
    if (event.getClickCount () == 2) {
      int x = event.getX ();
      int y = event.getY ();

      TreePath path = tree.getPathForLocation (x, y);
      if (path != null) {
        //bringUIToFront(path);
      }
    }
  }
}


}

