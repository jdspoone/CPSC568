package casa.io.tools;

import casa.io.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreePath;

/**
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 * @author  Jason Heard
 */

public class CASAFileInspector extends JFrame {
  JPanel panelTop = new JPanel ();
  JMenuBar menuBar = new JMenuBar ();
  JPanel panelFileTree = new JPanel ();
  GridLayout layoutTop = new GridLayout ();
  JScrollPane scrollFileTree = new JScrollPane ();
  /**
	 */
  CASAFileTree fileTree = new CASAFileTree ();
  JMenu menuFile = new JMenu ();
  JMenuItem menuFileQuit = new JMenuItem ();
  JMenuItem menuFileOpen = new JMenuItem ();
  GridLayout layoutFileTree = new GridLayout ();
  JMenuItem menuNodeExport = new JMenuItem ();
  JMenuItem menuNodeImport = new JMenuItem ();
  JMenu menuNode = new JMenu ();
  JMenuItem menuNodeDelete = new JMenuItem ();

  private JFileChooser fileChooserOpen = new JFileChooser ();
  private JFileChooser fileChooserExImport = new JFileChooser ();
  /**
	 */
  private CASAFile currentFile;
  private String selectedNode;

  public static void main (String[] args) {
    CASAFileInspector test = null;
    try {
      test = new CASAFileInspector ();
    } catch (HeadlessException e) {
      e.printStackTrace ();
    }

    if (args.length > 0) {
      test.setFile (new CASAFile (args[0]));
    }
  }

  public void setFile (CASAFile file) {
    try {
      fileTree.setFile (file);
      this.setTitle ("CASAFile Inspector - " + file.getAbsolutePath ());
    } catch (CASAIOException ex) {
      JOptionPane.showMessageDialog (this, ex.getMessage () + "\n" +
                                     file.getAbsolutePath (),
                                     "Error Opening File",
                                     JOptionPane.ERROR_MESSAGE);
    }

    currentFile = fileTree.getFile ();
    if (currentFile == null) {
      menuNodeImport.setEnabled (false);
    } else {
      menuNodeImport.setEnabled (true);
    }
  }

  private void exceptionError (String action, Exception exception) {
    JOptionPane.showMessageDialog (this,
                                   action + ":\n" + exception.getMessage (),
                                   "Error", JOptionPane.ERROR_MESSAGE);
  }

  private void exportNodeToFile (CASAFile inputFile, String node,
                                 File outputFile) {
    CASAInputStream inputStream = null;
    try {
      inputStream = new CASAInputStream (node, inputFile);
    } catch (IOException ex) {
      exceptionError ("Error opening input node", ex);
      return;
    }
    FileOutputStream outputStream = null;
    try {
      outputStream = new FileOutputStream (outputFile, false);
    } catch (FileNotFoundException ex) {
      exceptionError ("Error opening output file", ex);
      return;
    }

    // copying part
    byte[] buffer = new byte[1024];
    int bytesRead = -1;
    int bytesCopied = 0;
    try {
      bytesRead = inputStream.read (buffer);

      while (bytesRead != -1) {
        outputStream.write (buffer, 0, bytesRead);
        bytesCopied += bytesRead;
        bytesRead = inputStream.read (buffer);
      }
    } catch (IOException ex) {
      exceptionError ("Error copying data", ex);
      return;
    }

    try {
      outputStream.close ();
    } catch (IOException ex) {
    }
    try {
      inputStream.close ();
    } catch (IOException ex) {
    }
  }

  private void importNodeFromFile (File inputFile, CASAFile outputFile,
                                   String node) {
    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream (inputFile);
    } catch (FileNotFoundException ex) {
      exceptionError ("Error opening input file", ex);
      return;
    }
    CASAOutputStream outputStream = null;
    try {
      outputStream = new CASAOutputStream (node, CASAFile.MODE_OVERWRITE,
                                           outputFile);
    } catch (IOException ex) {
      exceptionError ("Error opening output node", ex);
      return;
    }

    // copying part
    byte[] buffer = new byte[1024];
    int bytesRead = -1;
    int bytesCopied = 0;
    try {
      bytesRead = inputStream.read (buffer);

      while (bytesRead != -1) {
        outputStream.write (buffer, 0, bytesRead);
        bytesCopied += bytesRead;
        bytesRead = inputStream.read (buffer);
      }
    } catch (IOException ex) {
      exceptionError ("Error copying data", ex);
    }

    fileTree.fileChanged ();

    try {
      outputStream.close ();
    } catch (IOException ex) {
    }
    try {
      inputStream.close ();
    } catch (IOException ex) {
    }
  }

  public CASAFileInspector () throws HeadlessException {
    try {
      jbInit ();
    } catch (Exception e) {
      e.printStackTrace ();
    }

    this.fileChooserOpen.setAcceptAllFileFilterUsed (false);
    this.fileChooserOpen.setFileFilter (new CASAFileFilter ());
    this.fileChooserOpen.setCurrentDirectory (new File ("."));
    this.fileChooserExImport.setAcceptAllFileFilterUsed (true);
    this.fileChooserExImport.setCurrentDirectory (new File ("."));
    setSelectedNode (null);

    this.pack ();
    this.setSize (600, 500);
    this.setVisible (true);
  }

  private void jbInit () throws Exception {
    panelTop.setLayout (layoutTop);
    layoutTop.setColumns (1);
    menuFile.setMnemonic ('F');
    menuFile.setText ("File");
    menuFileQuit.setMnemonic ('Q');
    menuFileQuit.setText ("Quit");
    menuFileQuit.addActionListener (new
        CASAFileInspector_menuFileQuit_actionAdapter (this));
    menuFileOpen.setMnemonic ('O');
    menuFileOpen.setText ("Open...");
    menuFileOpen.addActionListener (new
        CASAFileInspector_menuFileOpen_actionAdapter (this));
    panelFileTree.setLayout (layoutFileTree);
    layoutFileTree.setColumns (1);
    this.setDefaultCloseOperation (DISPOSE_ON_CLOSE);
    this.setLocale (java.util.Locale.getDefault ());
    this.setJMenuBar (menuBar);
    this.setState (Frame.NORMAL);
    this.setTitle ("CASAFile Inspector");
    menuNodeExport.setMnemonic ('E');
    menuNodeExport.setText ("Export To File...");
    menuNodeExport.addActionListener (new
        CASAFileInspector_menuNodeExport_actionAdapter (this));
    menuNodeImport.setMnemonic ('I');
    menuNodeImport.setText ("Import From File...");
    menuNodeImport.addActionListener (new
        CASAFileInspector_menuNodeImport_actionAdapter (this));
    menuNode.setMnemonic ('N');
    menuNode.setText ("Node");
    menuNodeDelete.setMnemonic ('D');
    menuNodeDelete.setText ("Delete...");
    menuNodeDelete.addActionListener (new
        CASAFileInspector_menuNodeDelete_actionAdapter (this));
    fileTree.addTreeSelectionListener (new
        CASAFileInspector_fileTree_treeSelectionAdapter (this));
    this.getContentPane ().add (panelTop, BorderLayout.CENTER);
    panelTop.add (panelFileTree, null);
    panelFileTree.add (scrollFileTree, null);
    scrollFileTree.getViewport ().add (fileTree, null);
    menuBar.add (menuFile);
    menuBar.add (menuNode);
    menuFile.add (menuFileOpen);
    menuFile.addSeparator ();
    menuFile.add (menuFileQuit);
    menuNode.add (menuNodeImport);
    menuNode.add (menuNodeExport);
    menuNode.addSeparator ();
    menuNode.add (menuNodeDelete);
  }

  /**
	 * @param selectedNode
	 */
  private void setSelectedNode (String selectedNode) {
    this.selectedNode = selectedNode;
    if (selectedNode == null) {
      menuNodeDelete.setEnabled (false);
      menuNodeExport.setEnabled (false);
    } else {
      menuNodeDelete.setEnabled (true);
      menuNodeExport.setEnabled (true);
    }
  }

  void menuFileQuit_actionPerformed (ActionEvent e) {
    this.setVisible (false);
    this.dispose ();
  }

  void menuFileOpen_actionPerformed (ActionEvent e) {
    int returnValue = fileChooserOpen.showOpenDialog (this);

    if (returnValue == JFileChooser.APPROVE_OPTION) {
      File file = fileChooserOpen.getSelectedFile ();
      setFile (new CASAFile (file.getAbsolutePath ()));
    }
  }

  void fileTree_valueChanged (TreeSelectionEvent e) {
    TreePath path = fileTree.getSelectionPath ();
    if (path != null && path.getPathCount () >= 3) {
      Object tempObject = path.getPathComponent (2);
      if (CASAFileTreeIndexEntry.class.isInstance (tempObject)) {
        CASAFileTreeIndexEntry tempEntry = (CASAFileTreeIndexEntry) tempObject;
        setSelectedNode (tempEntry.getName ());
      } else {
        setSelectedNode (null);
      }
    } else {
      setSelectedNode (null);
    }
  }

  void menuNodeDelete_actionPerformed (ActionEvent ev) {
    int returnValue = JOptionPane.showConfirmDialog (this,
        "Are you sure you want to delete the node '" + selectedNode + "'?",
        "Delete Node?",
        JOptionPane.YES_NO_OPTION);

    if (returnValue == JOptionPane.YES_OPTION) {
      try {
        currentFile.deleteNode (selectedNode);

        fileTree.fileChanged ();
      } catch (IOException ex) {
        exceptionError ("Error deleting node", ex);
      }
    }
  }

  void menuNodeExport_actionPerformed (ActionEvent e) {
    fileChooserExImport.setApproveButtonText ("Export");
    fileChooserExImport.setApproveButtonMnemonic ('E');
    fileChooserExImport.setDialogTitle ("Export to What File?");
    int returnValue = fileChooserExImport.showDialog (this, null);

    if (returnValue == JFileChooser.APPROVE_OPTION) {
      File file = fileChooserExImport.getSelectedFile ();
      if (file.exists ()) {
        returnValue = JOptionPane.showConfirmDialog (this,
            "Are you sure you want to overwrite the file:\n'" +
            file.getAbsolutePath () + "'?",
            "Overwrite File?",
            JOptionPane.YES_NO_OPTION);
        if (returnValue == JOptionPane.YES_OPTION) {
          exportNodeToFile (currentFile, selectedNode, file);
        }
      } else {
        exportNodeToFile (currentFile, selectedNode, file);
      }
    }
  }

  void menuNodeImport_actionPerformed (ActionEvent e) {
    fileChooserExImport.setApproveButtonText ("Import");
    fileChooserExImport.setApproveButtonMnemonic ('I');
    fileChooserExImport.setDialogTitle ("Import from What File?");
    fileChooserExImport.setDialogType (JFileChooser.OPEN_DIALOG);
    int returnValue = fileChooserExImport.showDialog (this, null);

    if (returnValue == JFileChooser.APPROVE_OPTION) {
      File file = fileChooserExImport.getSelectedFile ();
      if (!file.exists ()) {
        JOptionPane.showMessageDialog (this,
                                       "File does not exist:\n" +
                                       file.getAbsolutePath (),
                                       "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      String node = JOptionPane.showInputDialog (this,
          "To what node should the file (" + file.getAbsolutePath () +
                                                 ") be written?",
                                                 "Choose a Node",
                                                 JOptionPane.QUESTION_MESSAGE);
      if (node != null) {
        Object testNode = null;
        try {
          testNode = currentFile.getIndex ().getEntry (node);
        } catch (IOException ex) {
        }
        if (testNode != null) {
          returnValue = JOptionPane.showConfirmDialog (this,
              "Are you sure you want to overwrite the node:\n'" + node + "'?",
              "Overwrite Node?",
              JOptionPane.YES_NO_OPTION);
          if (returnValue == JOptionPane.YES_OPTION) {
            importNodeFromFile (file, currentFile, node);
          }
        } else {
          importNodeFromFile (file, currentFile, node);
        }
      }
    }
  }
}

/**
 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
class CASAFileInspector_menuFileQuit_actionAdapter implements java.awt.event.
    ActionListener {
  /**
	 */
  CASAFileInspector adaptee;

  CASAFileInspector_menuFileQuit_actionAdapter (CASAFileInspector adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed (ActionEvent e) {
    adaptee.menuFileQuit_actionPerformed (e);
  }
}

/**
 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
class CASAFileInspector_menuFileOpen_actionAdapter implements java.awt.event.
    ActionListener {
  /**
	 */
  CASAFileInspector adaptee;

  CASAFileInspector_menuFileOpen_actionAdapter (CASAFileInspector adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed (ActionEvent e) {
    adaptee.menuFileOpen_actionPerformed (e);
  }
}

/**
 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
class CASAFileInspector_fileTree_treeSelectionAdapter implements javax.swing.
    event.TreeSelectionListener {
  /**
	 */
  CASAFileInspector adaptee;

  CASAFileInspector_fileTree_treeSelectionAdapter (CASAFileInspector adaptee) {
    this.adaptee = adaptee;
  }

  public void valueChanged (TreeSelectionEvent e) {
    adaptee.fileTree_valueChanged (e);
  }
}

/**
 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
class CASAFileInspector_menuNodeDelete_actionAdapter implements java.awt.event.
    ActionListener {
  /**
	 */
  CASAFileInspector adaptee;

  CASAFileInspector_menuNodeDelete_actionAdapter (CASAFileInspector adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed (ActionEvent e) {
    adaptee.menuNodeDelete_actionPerformed (e);
  }
}

/**
 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
class CASAFileInspector_menuNodeExport_actionAdapter implements java.awt.event.
    ActionListener {
  /**
	 */
  CASAFileInspector adaptee;

  CASAFileInspector_menuNodeExport_actionAdapter (CASAFileInspector adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed (ActionEvent e) {
    adaptee.menuNodeExport_actionPerformed (e);
  }
}

/**
 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
class CASAFileInspector_menuNodeImport_actionAdapter implements java.awt.event.
    ActionListener {
  /**
	 */
  CASAFileInspector adaptee;

  CASAFileInspector_menuNodeImport_actionAdapter (CASAFileInspector adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed (ActionEvent e) {
    adaptee.menuNodeImport_actionPerformed (e);
  }
}