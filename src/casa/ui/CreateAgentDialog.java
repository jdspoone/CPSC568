package casa.ui;

import casa.LAC;
import casa.RunDescriptor;
import casa.util.Trace;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;


/**
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 * @author  <a href="mailto:ayala@cpsc.ucalgary.ca">Gabriel Becerra</a>
 */

public class CreateAgentDialog extends JDialog {
  //
  // Java - UI Objects
  //
  private JFrame frame;
  private JPanel dialogPanel = new JPanel();
  private BorderLayout borderLayout1 = new BorderLayout();

  //private JCheckBox  retrieveBox        = new JCheckBox();
  //private JCheckBox  joinCDCheckBox     = new JCheckBox();
  //private JComboBox  coopDomainComboBox = new JComboBox();
  private JTextField agentPortTextField = new JTextField();
  private JTextField lacPortTextField   = new JTextField();
  private JLabel     agentPortLabel     = new JLabel();
  private JLabel     lacPortLabel       = new JLabel();
  //private JLabel     coopDomainBoxLabel = new JLabel();

  //
  // Varaibles
  //
  private String newAgentPortString = new String();
  private String newAgentNameString = new String();
  private String newAdditionalParams= new String();
  private String newLacPortString   = new String();
  private String userNameORTitle    = new String();
  //private String coopToJoin         = new String();
  private String className  = new String();
  //private int newAgentPortInt = 0;
  //private int newLacPortInt   = 0;
  private int lacPort         = 0;
  private int agentPort       = 0;
  private boolean obtainHistory;

  //private Hashtable cdHashtable = new Hashtable();

  //
  // CASA Objects
  //
  //private casa.agentCom.URLDescriptor chosenCD;
  //private LACDesktop rightFrame;
  /**
	 */
  private LAC lac;
  JTextField AgentNameTextField = new JTextField();
  JLabel AgentNameLabel1 = new JLabel();
  JTextField additionalParamsTextField = new JTextField();
  JLabel additionalParamsLabel = new JLabel();
//  ButtonGroup typeGroup = new ButtonGroup();
  JTextField agentTypeTextField = new JTextField();
  JLabel agentTypeLabel1 = new JLabel();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  JButton createButton = new JButton();
  JTextField specField = new JTextField();
//  JRadioButton _internal = new JRadioButton();
//  JRadioButton _javaClass = new JRadioButton();
  JButton cancelButton = new JButton();
//  JRadioButton cmdLine = new JRadioButton();
  GridBagLayout gridBagLayout3 = new GridBagLayout();
  private static int defaultPort = 7100;

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
  public CreateAgentDialog(JFrame frame, LAC lac2, String title, boolean modal,
                           String userNameORTitle, int agentPort, int lacPort,
                           String className, String additionalParams) {
    super(frame, title, modal);

    this.frame           = frame;
    this.lac             = lac2;
    //this.rightFrame      = rightFrame;
    this.userNameORTitle = userNameORTitle;
    this.agentPort       = agentPort!=0?agentPort:-defaultPort++;
    this.lacPort         = lacPort>0?lacPort:lac2.getPort();
    this.className       = className;

    try {
      jbInit();
      agentTypeTextField.setEnabled(this.className==null);
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
    additionalParamsTextField.setMinimumSize(new Dimension(230, 20));
    additionalParamsTextField.setPreferredSize(new Dimension(230, 20));
    lacPortTextField.setMinimumSize(new Dimension(230, 20));
    lacPortTextField.setPreferredSize(new Dimension(230, 20));
    agentPortTextField.setMinimumSize(new Dimension(230, 20));
    agentPortTextField.setPreferredSize(new Dimension(230, 20));
    AgentNameTextField.setMinimumSize(new Dimension(230, 20));
    AgentNameTextField.setPreferredSize(new Dimension(230, 20));
    agentTypeTextField.setMinimumSize(new Dimension(230, 20));
    agentTypeTextField.setPreferredSize(new Dimension(230, 20));
    dialogPanel.setLayout(gridBagLayout3);

    JPanel parametersPanel = new JPanel();

    parametersPanel.setBorder (new TitledBorder (new EtchedBorder (),
                                                 className + " Creation Parameters"));
    parametersPanel.setMinimumSize(new Dimension(265, 300));
    parametersPanel.setPreferredSize(new Dimension(265, 300));
    parametersPanel.setLayout(gridBagLayout1);

    String agentTempPort = Integer.toString(agentPort);
    String lacTempPort   = Integer.toString(lacPort);

    agentPortLabel.setText("Agent Port");
    agentPortTextField.setEditable(true);
    agentPortTextField.setText(Integer.toString(agentPort));
    agentPortTextField.setToolTipText("Enter a port to use or leave unchanged");

    lacPortLabel.setText("LAC Port");
    lacPortTextField.setEditable(true);
    lacPortTextField.setText(Integer.toString(lacPort));
    lacPortTextField.setToolTipText("LAC\'s port");



    AgentNameTextField.setText(userNameORTitle);
    AgentNameLabel1.setText("Agent Name");
    additionalParamsTextField.setText("");
    additionalParamsLabel.setText("other parms");
    additionalParamsTextField.setText(newAdditionalParams);
    agentTypeTextField.setText(className);
    agentTypeLabel1.setText("Agent Type/Path");
    RunDescriptor rd = lac.getRunDescriptor(className);
    if (rd!=null) {
//      switch (rd.getType()) {
//        case RunDescriptor.CommandLine:
          specField.setText(rd.getCommandLine());
//          cmdLine.setSelected(true);
//          break;
//        case RunDescriptor.JavaClass:
//          specField.setText(rd.getJavaClass());
//          _javaClass.setSelected(true);
//          break;
//        case RunDescriptor.Internal:
//          specField.setText(rd.getInternal());
//          _internal.setSelected(true);
//          break;
//        case RunDescriptor.None:
//        default:
//          specField.setText("");
//      }
//      specField.setEnabled(true);
//      cmdLine.setEnabled(true);
//      _javaClass.setEnabled(true);
//      _internal.setEnabled(true);
//    }
//    else {
      specField.setEnabled(true);
//      cmdLine.setEnabled(true);
//      _javaClass.setEnabled(true);
//      _internal.setEnabled(true);
    }

    createButton.setText("Create");
    createButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        createButtonActionPerformed(e);
      }
    });
    specField.setMinimumSize(new Dimension(300, 20));
    specField.setPreferredSize(new Dimension(300, 20));
//    _internal.setText("Internal");
    final String defaultCommand = "(agent.new-agent \"%type%\" \"%name%\" %port% :LACPORT %lacPort% "
  		+":process \"CURRENT\" "
  		+":ack nil :markup \"KQML\" :trace 10 :debug \"ON\" "
  		+":trace :trace-tags \"warning,msg,msgHandling,commitments9,policies9,lisp\")";
    specField.setText(defaultCommand);

//    _internal.addActionListener(new java.awt.event.ActionListener() {
//      public void actionPerformed(ActionEvent e) {
//        String s = specField.getText();
//        if (s==null || s.length()==0) specField.setText(defaultCommand);
//      }
//    });
//    _javaClass.setText("Java class");
//    _javaClass.addActionListener(new java.awt.event.ActionListener() {
//      public void actionPerformed(ActionEvent e) {
//        String s = specField.getText();
//        if (s==null || s.length()==0) specField.setText(defaultCommand);
//      }
//    });
//    cmdLine.setText("Command line");
//    cmdLine.addActionListener(new java.awt.event.ActionListener() {
//      public void actionPerformed(ActionEvent e) {
//        String s = specField.getText();
//        if (s==null || s.length()==0) specField.setText(defaultCommand);
//      }
//    });
    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelButtonActionPerformed(e);
      }
    });
    parametersPanel.add(agentTypeLabel1,          new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 2), 0, 0));
    parametersPanel.add(agentTypeTextField,                  new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 160, 5));

    parametersPanel.add(AgentNameLabel1,          new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 2), 0, 0));
    parametersPanel.add(AgentNameTextField,              new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 160, 5));

    parametersPanel.add(agentPortLabel,          new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 2), 0, 0));
    parametersPanel.add(agentPortTextField,          new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 160, 5));

    parametersPanel.add(lacPortLabel,          new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 2), 0, 0));
    parametersPanel.add(lacPortTextField,          new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 160, 5));

    parametersPanel.add(additionalParamsLabel,           new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 2), 0, 0));
    parametersPanel.add(additionalParamsTextField,          new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 160, 5));

    dialogPanel.add(parametersPanel,    new GridBagConstraints(0, 0, 3, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 0, 5), 35, 0));

//    dialogPanel.add(_javaClass,     new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
//            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(20, 0, 0, 0), 0, 0));
//    dialogPanel.add(cmdLine,     new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
//            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(20, 0, 0, 0), 0, 0));
//    dialogPanel.add(_internal,       new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
//            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(20, 0, 0, 0), 0, 0));

    dialogPanel.add(specField,     new GridBagConstraints(0, 2, 3, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));

    dialogPanel.add(createButton,    new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(20, 0, 20, 0), 0, 0));
    dialogPanel.add(cancelButton,    new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(20, 0, 20, 0), 0, 0));



    parametersPanel.setOpaque(true);

    // Placing Grid in the centre
    getContentPane().add(dialogPanel, BorderLayout.CENTER);

    Dimension screenSize = Toolkit.getDefaultToolkit ().getScreenSize ();
    Dimension dialogSize = this.getPreferredSize ();
    setLocation (screenSize.width / 2 - (dialogSize.width / 2),
                 screenSize.height / 2 - (dialogSize.height / 2));

    this.setSize(300, 340);
    this.setResizable(false);
//    typeGroup.add(_internal);
//    typeGroup.add(cmdLine);
//    typeGroup.add(_javaClass);
  }

  private void createButtonActionPerformed( ActionEvent e ) {
    newAgentPortString = agentPortTextField.getText();
    newLacPortString   = lacPortTextField.getText();
    newAgentNameString = AgentNameTextField.getText();
    newAdditionalParams= additionalParamsTextField.getText();

    try {
      if (newAgentPortString.length() != 0)
        agentPort = Integer.parseInt (newAgentPortString);
    } catch (NumberFormatException ex) {}

    try {
      if (newLacPortString.length() != 0)
        lacPort = Integer.parseInt (newLacPortString);
    } catch (NumberFormatException ex1) {}

    createAgentInstance();
    //cdHashtable.clear();

    closeDialog();
  }

  /**
   * Creates an instance of the agent based on the user's choice in the LACMenu
   */
  private void createAgentInstance() {
    RunDescriptor rd = new RunDescriptor();
    String s = specField.getText();
//    if (cmdLine.isSelected()) 
    	rd.setCommandLine(s);
//    else if (_internal.isSelected()) 
//    	rd.setInternal(s);
//    else if (_javaClass.isSelected()) 
//    	rd.setJavaClass(s);
//    else 
//    	return;
    String p = null;
    if (newAgentNameString.indexOf('/')>=0 || newAgentNameString.indexOf('.')>=0) {
      p = newAgentNameString.replace('.', '/');
    }
    else {p = new String(className);
      p = p.replace('.', '/');
      if (p.charAt(p.length() - 1) != '/')
        p += '/';
      p += newAgentNameString;
    }
    try {
      rd.run(agentPort, lacPort, p, lac);
    }
    catch (Exception ex) {
    	Trace.log("error", "Could not run agent "+newAgentNameString+": "+ex.toString());
      ex.printStackTrace();
    }
    /*
    if (newAdditionalParams != "") {
      String command =
          "+A" + className
          + " -p" + newAgentPortInt
          + " -L" + newLacPortInt
          + " -n" + newAgentNameString
          + " " + newAdditionalParams;
      CASACommandLine.main(command);
    }
    else {
      URLDescriptor url = new URLDescriptor(lac.getURL().getUser(), lac.getURL().getHost(), newAgentPortInt, className+newAgentNameString);
      url.setLACport(newLacPortInt);
      lac.runAgent(url);
    }
    */
  }
//----------------------------------------------------------------------------------------
//  public void createExternalAgentInstance(String s, int agentPort, int lacPort){
//	  RunDescriptor rd = new RunDescriptor();
////	    if (cmdLine.isSelected()) 
//	    	rd.setCommandLine(s);
////	    else if (_internal.isSelected()) 
////	    	rd.setInternal(s);
////	    else if (_javaClass.isSelected()) 
////	    	rd.setJavaClass(s);
////	    else return;
//	    String p = null;
//	    if (newAgentNameString.indexOf('/')>=0 || newAgentNameString.indexOf('.')>=0) {
//	      p = newAgentNameString.replace('.', '/');
//	    }
//	    else {p = new String(className);
//	      p = p.replace('.', '/');
//	      if (p.charAt(p.length() - 1) != '/')
//	        p += '/';
//	      p += newAgentNameString;
//	    }
//	    try {
//	      rd.run(agentPort, lacPort, p);
//	    }
//	    catch (Exception ex) {
//	      DEBUG.DISPLAY("Could not run agent "+newAgentNameString+": "+ex.toString(),"Create agent error",true);
//	      ex.printStackTrace();
//	    }
//  }
  //---------------------------------------------------------------------------------------
  private void cancelButtonActionPerformed(ActionEvent e) {
    closeDialog();
  }

  private void closeDialog () {
    this.dispose();
    frame.repaint();
    //rightFrame.repaint();
  }

  void cmdLine_actionPerformed(ActionEvent e) {
    String s = specField.getText();
    if (s==null || s.length()== 0) specField.setText(
    		"(agent.new-agent \"%type%\" \"%name%\" %port% :LACPORT %lacPort% "
    		+":process \"CURRENT\" :markup \"KQML\" :trace trace-code "
    		+":trace-tags \"warning,msg,msgHandling,commitments9,policies9,lisp\")");
  }
}

