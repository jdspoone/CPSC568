package casa.ui;




import casa.ML;
import casa.ObserverNotification;
import casa.Status;
import casa.agentCom.URLDescriptor;
import casa.exceptions.URLDescriptorException;
import casa.interfaces.TransientAgentInterface;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.border.TitledBorder;


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

public class TypeEditDialog extends JDialog implements Observer {

	private static final long	serialVersionUID	= -8971702083112425436L;
	boolean updated = false;
  //public static final int DO_PERFS = 1;
  //public static final int DO_ACTS = 2;
  //int hierarchyType = DO_PERFS;
  TransientAgentInterface agent = null;

  GridBagLayout gridBagLayout1 = new GridBagLayout();
  JScrollPane jScrollPane1 = new JScrollPane();
  JTextField statusBar = new JTextField();
  JPanel jPanel1 = new JPanel();
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  JButton DoneButton = new JButton();
  JButton updateTypeButton = new JButton();
  JButton replaceTypeButton = new JButton();
  JTextField otherAgentTextField = new JTextField();
  JLabel otherAgentLabel = new JLabel();
  JButton askButton = new JButton();
  JTextField askTypeTextField = new JTextField();
  JLabel AskTypeLabel = new JLabel();
  JPanel jPanel2 = new JPanel();
  TitledBorder titledBorder1;
  GridBagLayout gridBagLayout3 = new GridBagLayout();
  JEditorPane jEditorPane1 = new JEditorPane();

  public TypeEditDialog() throws HeadlessException {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  public TypeEditDialog(Frame owner, String title, boolean modal, TransientAgentInterface agent) throws HeadlessException {
    super(owner, title, modal);
    this. agent = agent;
    try {
      jbInit ();
    } catch (Exception ex) {
      ex.printStackTrace ();
    }
  }

  private void jbInit() throws Exception {
    titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(149, 142, 130)),"Ask another agent");
    this.getContentPane().setLayout(gridBagLayout1);
    jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    jScrollPane1.setViewport(new JViewport());
    jScrollPane1.setViewportBorder(BorderFactory.createLineBorder(Color.black));
    jScrollPane1.setBorder(BorderFactory.createRaisedBevelBorder());
    jScrollPane1.setToolTipText("");
    jScrollPane1.setVerifyInputWhenFocusTarget(true);
    this.setModal(false);
    this.addWindowListener(new TypeEditDialog_this_windowAdapter(this));
    statusBar.setBackground(SystemColor.menu);
    statusBar.setText("");
    jPanel1.setLayout(gridBagLayout2);
    DoneButton.setActionCommand("done");
    DoneButton.setText("Done");
    DoneButton.addMouseListener(new TypeEditDialog_DoneButton_mouseAdapter(this));
    jPanel1.setBorder(null);
    jPanel1.setDoubleBuffered(true);
    updateTypeButton.setActionCommand("updateType");
    updateTypeButton.setText("Update");
    updateTypeButton.addMouseListener(new TypeEditDialog_updateTypeButton_mouseAdapter(this));
    replaceTypeButton.setActionCommand("replaceType");
    replaceTypeButton.setText("Replace");
    replaceTypeButton.addMouseListener(new TypeEditDialog_replaceTypeButton_mouseAdapter(this));
    otherAgentTextField.setMinimumSize(new Dimension(40, 18));
    otherAgentTextField.setToolTipText("eg: 136.159.1.10:6400");
    otherAgentTextField.setText("other agent");
    otherAgentTextField.addActionListener(new TypeEditDialog_otherAgentTextField_actionAdapter(this));
    otherAgentLabel.setText("IP:port: ");
    askButton.setText("Ask");
    askButton.addMouseListener(new TypeEditDialog_askButton_mouseAdapter(this));
    askTypeTextField.setMinimumSize(new Dimension(60, 18));
    askTypeTextField.setToolTipText("if left blank, all types are requested");
    askTypeTextField.setText("ask type");
    AskTypeLabel.setVerifyInputWhenFocusTarget(true);
    AskTypeLabel.setText("Type: ");
    jPanel2.setBorder(titledBorder1);
    jPanel2.setLayout(gridBagLayout3);
    jEditorPane1.setContentType("text/plain");
    jEditorPane1.setText(agent.getSerializedOntology());
    jEditorPane1.setMargin(new Insets(3, 3, 3, 3));
    jEditorPane1.setPreferredSize(new Dimension(400, 100));
    jEditorPane1.setMinimumSize(new Dimension(200, 100));
    jEditorPane1.setDebugGraphicsOptions(0);
    jEditorPane1.setBorder(BorderFactory.createLineBorder(Color.black));
    jPanel1.add(replaceTypeButton,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 10, 0, 10), 0, 0));
    jPanel1.add(updateTypeButton,   new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 10, 0, 10), 0, 0));
    jPanel1.add(DoneButton,         new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 10, 0, 10), 0, 0));
    this.getContentPane().add(statusBar,                    new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    this.getContentPane().add(jPanel2,          new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 5, 2, 5), 23, -1));
    jPanel2.add(otherAgentLabel,    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 3, 0, 0), 0, 0));
    jPanel2.add(otherAgentTextField,  new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 4, 0), 0, 0));
    jPanel2.add(AskTypeLabel,   new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
    jPanel2.add(askTypeTextField,    new GridBagConstraints(3, 0, 1, 1, 0.5, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    jPanel2.add(askButton,    new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
    this.getContentPane().add(jScrollPane1, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 0, 5), 400, 200));
    jScrollPane1.getViewport().add(jEditorPane1, null);
    this.getContentPane().add(jPanel1, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
  }

  public boolean display() {
    updated = false;
    pack ();
    setVisible (true);
    return updated;
  }

  private String getTypesFromAgent() {
    return agent.getSerializedOntology();
  }

  private void updateStatusBar(String msg, boolean error) {
    statusBar.setBackground(error?SystemColor.YELLOW:SystemColor.menu);
    statusBar.setText(msg==null?"":msg);
    if (error) Toolkit.getDefaultToolkit().beep();
 }

  void updateTypeButton_mouseReleased(MouseEvent e) {
    Status stat = agent.putExtendedOntology(jEditorPane1.getText());
    if (stat.getStatusValue()==0) {
      updateStatusBar("Herarchy updated", false);
      jEditorPane1.setText(getTypesFromAgent());
    } else {
      updateStatusBar(stat.getExplanation(),true);
    }
  }

  void DoneButton_mouseReleased(MouseEvent e) {
    String editData = jEditorPane1.getText();
    String agentData = getTypesFromAgent();
    boolean changed = !editData.equals(agentData);
    int userAnswer = JOptionPane.YES_OPTION;
    if (changed) {
      String m = "Data in the edit buffer has changed.  Do you want to quite anyway?";
      updateStatusBar(m,true);
      userAnswer = JOptionPane.showConfirmDialog(this
                                    ,m
                                    ,this.getTitle()
                                    ,JOptionPane.YES_NO_OPTION
                                    ,JOptionPane.INFORMATION_MESSAGE);
      updateStatusBar("",false);
    }
    if (userAnswer==JOptionPane.YES_OPTION) this.dispose();
  }

  void replaceTypeButton_mouseReleased(MouseEvent e) {
    Status stat = agent.putReplacementOntology(jEditorPane1.getText());
    if (stat.getStatusValue()==0) {
      updateStatusBar("Herarchy updated", false);
      jEditorPane1.setText(getTypesFromAgent());
    } else {
      updateStatusBar(stat.getExplanation(),true);
    }
  }

  void otherAgentTextField_actionPerformed(ActionEvent e) {

  }

  void askButton_mouseReleased(MouseEvent e) {
    URLDescriptor url;
    try {
      url = URLDescriptor.make(otherAgentTextField.getText());
    }
    catch (URLDescriptorException ex) {
      updateStatusBar("Bad IP address:port syntax: "+ex.toString(), true);
      return;
    }
    agent.addObserver(this);

    Status tempStatus = agent.doGetOnology (url,
                                          ML.GET_ONTOLOGY,
                                          askTypeTextField.getText ());

    if (tempStatus.getStatusValue () != 0) {
      updateStatusBar("Failed to send message to the other agent: " + tempStatus.getExplanation (),true);
      return;
    }

    updateStatusBar("Waiting for the other agent to reply...",false);
  }

  /**
   * <p>Description: adapted from the previous update(). The structure and most comments
   * belong to the author of the previous version.  Update() now uses 
   * {@link ObserverNotification}</p>
   * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
   * @version 0.9
   */
  public void update(Observable o, Object arg) {
	  try {
		  if (ML.EVENT_INSERTED_PERFORMATIVES.equals(((ObserverNotification)arg).getType())){
			  getTypesFromAgent();
			  updateStatusBar("Update from remote agent completed successfully.", false);
			  Toolkit.getDefaultToolkit().beep();
		  }
		  else if(ML.EVENT_FAILED_PERFORMATIVES_UPDATE.equals(((ObserverNotification)arg).getType())){
			  updateStatusBar("Update from remote agent failed.", true);
		  }
		  agent.deleteObserver(this);
	  }
	  catch (Exception ex) {
		  ex.printStackTrace();
	  }
  }

  void this_windowClosed(WindowEvent e) {
    agent.deleteObserver(this);
  }
}

class TypeEditDialog_updateTypeButton_mouseAdapter extends java.awt.event.MouseAdapter {
  TypeEditDialog adaptee;

  TypeEditDialog_updateTypeButton_mouseAdapter(TypeEditDialog adaptee) {
    this.adaptee = adaptee;
  }
  public void mouseReleased(MouseEvent e) {
    adaptee.updateTypeButton_mouseReleased(e);
  }
}

class TypeEditDialog_DoneButton_mouseAdapter extends java.awt.event.MouseAdapter {
  TypeEditDialog adaptee;

  TypeEditDialog_DoneButton_mouseAdapter(TypeEditDialog adaptee) {
    this.adaptee = adaptee;
  }
  public void mouseReleased(MouseEvent e) {
    adaptee.DoneButton_mouseReleased(e);
  }
}

class TypeEditDialog_replaceTypeButton_mouseAdapter extends java.awt.event.MouseAdapter {
  TypeEditDialog adaptee;

  TypeEditDialog_replaceTypeButton_mouseAdapter(TypeEditDialog adaptee) {
    this.adaptee = adaptee;
  }
  public void mouseReleased(MouseEvent e) {
    adaptee.replaceTypeButton_mouseReleased(e);
  }
}

class TypeEditDialog_otherAgentTextField_actionAdapter implements java.awt.event.ActionListener {
  TypeEditDialog adaptee;

  TypeEditDialog_otherAgentTextField_actionAdapter(TypeEditDialog adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.otherAgentTextField_actionPerformed(e);
  }
}

class TypeEditDialog_askButton_mouseAdapter extends java.awt.event.MouseAdapter {
  TypeEditDialog adaptee;

  TypeEditDialog_askButton_mouseAdapter(TypeEditDialog adaptee) {
    this.adaptee = adaptee;
  }
  public void mouseReleased(MouseEvent e) {
    adaptee.askButton_mouseReleased(e);
  }
}

class TypeEditDialog_this_windowAdapter extends java.awt.event.WindowAdapter {
  TypeEditDialog adaptee;

  TypeEditDialog_this_windowAdapter(TypeEditDialog adaptee) {
    this.adaptee = adaptee;
  }
  public void windowClosed(WindowEvent e) {
    adaptee.this_windowClosed(e);
  }
}