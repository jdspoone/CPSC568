package casa.ui;

import casa.LAC;
import casa.RunDescriptor;
import casa.Status;
import casa.util.Trace;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;


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
 * @author <a href="mailto:ayala@cpsc.ucalgary.ca">Gabriel Becerra</a>
 */

public class AgentTypeDialog extends JDialog {
  //
  // Java - UI Objects
  //
  private JFrame frame;
  private JPanel dialogPanel = new JPanel();
  private BorderLayout borderLayout1 = new BorderLayout();

  //
  // Varaibles
  //
  private String className  = new String();

  //private Hashtable cdHashtable = new Hashtable();

  //
  // CASA Objects
  //
  //private RunDescriptor runDescr;
  private LAC lac;
  ButtonGroup typeGroup = new ButtonGroup();
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  JTextField specField = new JTextField();
  JRadioButton _internal = new JRadioButton();
  JRadioButton _javaClass = new JRadioButton();
  JRadioButton cmdLine = new JRadioButton();
  GridBagLayout gridBagLayout3 = new GridBagLayout();
  JTextField agentTypeTextField = new JTextField();
  JLabel agentTypeLabel1 = new JLabel();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  JTextArea jTextArea1 = new JTextArea();
  JPanel jPanel1 = new JPanel();
  GridBagLayout gridBagLayout4 = new GridBagLayout();
  JButton cancelButton = new JButton();
  JButton saveButton = new JButton();
  JButton deletejButton = new JButton();
  JCheckBox authorizedCheckBox = new JCheckBox();
  JComboBox agentTypeCombo = new JComboBox();
  JLabel commandLabel = new JLabel();

  /**
   * Constructs a Dialog according to the agent that is about to be created
   * @param frame is this Dialog's parent
   * @param title is this Dialog's title
   * @param modal is a boolean (true) that forces the user to work with this Dialog only
   * @param rightFrame is a "pointer" to the LACDesktop
   * @param userNameORTitle is the user's username or a specific title for the new agent
   * @param agentPort is the port that the user wants to enable for this agent
   * @param lacPort is the LAC's current port
   * @param agentInstance is used to determine what type of agent we are creating
   */
  public AgentTypeDialog(JFrame frame, LAC lac, String title, boolean modal, String className) {
    super(frame, title, modal);

    this.frame           = frame;
    this.lac             = lac;
    //this.runDescr        = rd;
    this.className       = className;

    try {
      jbInit();

      //use one or the other the agentTypeCombo or agentTypeTextField
      boolean comboEnabled = (className==null);
      agentTypeCombo.setEnabled(comboEnabled);
      agentTypeCombo.setFocusable(comboEnabled);
      agentTypeCombo.setVisible(comboEnabled);
      agentTypeTextField.setEnabled(false);
      agentTypeTextField.setFocusable(false);
      agentTypeTextField.setVisible(!comboEnabled);

      setVisible(true);
      dialogPanel.setOpaque(true);
      pack();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Sets all the elements and listeners in this dialog
   * @throws Exception
   */
  private void jbInit() throws Exception {

    specField.setMinimumSize(new Dimension(300, 20));
    specField.setPreferredSize(new Dimension(300, 20));
    _internal.setText("Internal");
    _internal.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        //String s = specField.getText();
        //if (s==null || s.length()==0)
          specField.setText("+A%type% -n%name% -p%port% -L%lacPort%");
      }
    });
    _javaClass.setText("Java class");
    _javaClass.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        //String s = specField.getText();
        //if (s==null || s.length()==0)
          specField.setText("/A%type% -n%name% -p%port% -L%lacPort%");
      }
    });
    cmdLine.setText("Command line");
    cmdLine.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        //String s = specField.getText();
        //if (s==null || s.length()==0)
          specField.setText("casa /A%type% -n%name% -p%port% -L%lacPort%");
      }
    });

    agentTypeTextField.setMinimumSize(new Dimension(230, 20));
    agentTypeTextField.setPreferredSize(new Dimension(230, 20));
    agentTypeTextField.setText(className);
    updateFromRunDescriptor();
    agentTypeCombo.addFocusListener(new java.awt.event.FocusListener() {
      public void focusLost(FocusEvent e) {
        updateFromRunDescriptor();
      }
      public void focusGained(FocusEvent e) {}
    });
    agentTypeCombo.setEditable(true);
    agentTypeCombo.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        updateFromRunDescriptor();
      }
    });
    Vector v = lac.getRegisteredAgentsVector();
    for (int i = 0, last=v.size(); i<last; i++) {
      agentTypeCombo.addItem((String)v.get(i));
    }
    if (className!=null) agentTypeCombo.setSelectedItem(className);
    agentTypeCombo.validate();
    agentTypeLabel1.setToolTipText("");
    agentTypeLabel1.setText("Agent Type/Path:");

    dialogPanel.setMinimumSize(new Dimension(400, 300));
    dialogPanel.setPreferredSize(new Dimension(400, 300));
    jTextArea1.setBackground(UIManager.getColor("Button.background"));
    jTextArea1.setEditable(false);
    jTextArea1.setText("%port%    The port given in the run time command for the agent to " +
    "listen at.\n%lacPort% The LAC port given in the run time command for " +
    "the agent to register to.\n%path%    The path of the the agent.  This " +
    "is the concatonations of the %dir% and %file%\n%file%    The filename " +
    "for the agent.  This represents an individual agent name.  Same as " +
    "%name%.\n%name%    The name for the agent.  This represents an individual " +
    "agent name.  Same as %file%.\n%dir%     The directory path, relative " +
    "to the LAC\'s root, for the agent.  Same as %type% only with backslashes.\n%type%    The " +
    "type path specifying the agent type.  Same as %dir% only with dots.");
    jTextArea1.setLineWrap(true);
    jTextArea1.setWrapStyleWord(true);
    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelButtonActionPerformed(e);
      }
    });
    saveButton.setText("Save Type");
    saveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveButtonActionPerformed(e);
      }
    });
    deletejButton.setVerifyInputWhenFocusTarget(true);
    deletejButton.setText("Delete Type");
    deletejButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        deleteButtonActionPerformed(e);
      }
    });
    authorizedCheckBox.setText("Authorized");
    commandLabel.setText("Command: ");
    typeGroup.add(_javaClass);
    typeGroup.add(cmdLine);
    typeGroup.add(_internal);

    dialogPanel.setLayout(gridBagLayout3);
    dialogPanel.add(agentTypeLabel1,      new GridBagConstraints(0, 0, 1, 2, 0.0, 0.0
            ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(10, 5, 0, 0), 0, 0));
    dialogPanel.add(agentTypeTextField,             new GridBagConstraints(1, 0, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 5), 0, 0));
    dialogPanel.add(agentTypeCombo,               new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(3, 5, 0, 5), 291, 0));
    dialogPanel.add(_javaClass,                  new GridBagConstraints(0, 1, 1, 2, 0.0, 0.0
            ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE, new Insets(20, 0, 0, 0), 0, 0));
    dialogPanel.add(cmdLine,                  new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.SOUTH, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    dialogPanel.add(_internal,                    new GridBagConstraints(2, 1, 1, 2, 0.0, 0.0
            ,GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(20, 0, 0, 0), 0, 0));
    dialogPanel.add(commandLabel,  new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    dialogPanel.add(specField,                      new GridBagConstraints(1, 3, 2, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 2));
    dialogPanel.add(jTextArea1,             new GridBagConstraints(0, 4, 4, 1, 0.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 5, 0, 5), 0, 0));

    dialogPanel.add(authorizedCheckBox,          new GridBagConstraints(0, 5, 3, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 0, 0), 0, 0));

    dialogPanel.add(jPanel1,                new GridBagConstraints(0, 6, 3, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 0, 5), 0, 0));
    jPanel1.setLayout(gridBagLayout4);
    jPanel1.add(cancelButton,        new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
    jPanel1.add(saveButton,       new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
    jPanel1.add(deletejButton,      new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
    this.getContentPane().add(dialogPanel, BorderLayout.SOUTH);




    // Placing Grid in the centre

    Dimension screenSize = Toolkit.getDefaultToolkit ().getScreenSize ();
    Dimension dialogSize = this.getPreferredSize ();
    setLocation (screenSize.width / 2 - (dialogSize.width / 2),
                 screenSize.height / 2 - (dialogSize.height / 2));

    setSize(new Dimension(595, 350));
    setResizable(true);
  }

  private void updateFromRunDescriptor() {
    RunDescriptor rd = null;
    className = agentTypeTextField.getText();
    if (className != null && className.length()>0)
      rd = lac.getRunDescriptor(className);
    else {
      className = (String)agentTypeCombo.getSelectedItem();
      if (className != null && className.length()>0)
        rd = lac.getRunDescriptor(className);
    }
    authorizedCheckBox.setSelected(rd==null || rd.isAuthorized());
    switch (rd != null ? rd.getType() : RunDescriptor.JavaClass) {
      case RunDescriptor.CommandLine:
        specField.setText(rd != null ? rd.getCommandLine() : "casa /A%type% -n%name% -p%port% -L%lacPort%");
        cmdLine.setSelected(true);
        break;
      case RunDescriptor.JavaClass:
        specField.setText(rd != null ? rd.getJavaClass() : "/A%type% -n%name% -p%port% -L%lacPort%");
        _javaClass.setSelected(true);
        break;
      case RunDescriptor.Internal:
        specField.setText(rd != null ? rd.getInternal() : "+A%type% -n%name% -p%port% -L%lacPort%");
        _internal.setSelected(true);
        break;
      case RunDescriptor.None:
      default:
        specField.setText("");
        cmdLine.setSelected(false);
        _javaClass.setSelected(false);
        _internal.setSelected(false);
    }
  }

  private void saveButtonActionPerformed( ActionEvent e ) {
    RunDescriptor rd = new RunDescriptor();
    String line = specField.getText();
    if      (cmdLine   .isSelected()) rd.setCommandLine(line);
    else if (_javaClass.isSelected()) rd.setJavaClass(line);
    else if (_internal .isSelected()) rd.setInternal(line);
    rd.setAuthorized(authorizedCheckBox.isSelected());
    String type = agentTypeTextField.getText();
    if (type==null || type.length()==0) type = (String)agentTypeCombo.getSelectedItem();
    Status stat = lac.registerAgentType(type,rd);
    int statInt = stat.getStatusValue();
    if (statInt!=0) Trace.log("error", stat.getExplanation()+": "+(statInt<0?"Error":"Warning")+" saving "+agentTypeTextField.getText());
    closeDialog();
  }

  private void deleteButtonActionPerformed( ActionEvent e ) {
    Status stat = lac.unregisterAgentType(agentTypeTextField.getText());
    int statInt = stat.getStatusValue();
    if (statInt!=0) Trace.log("error", stat.getExplanation()+": "+(statInt<0?"Error":"Warning")+" deleting "+agentTypeTextField.getText());
    agentTypeTextField.setText("");
    updateFromRunDescriptor();
    closeDialog();
  }

  private void cancelButtonActionPerformed(ActionEvent e) {
    closeDialog();
  }

  private void closeDialog () {
    this.dispose();
    if (frame!=null) frame.repaint();
  }

}
