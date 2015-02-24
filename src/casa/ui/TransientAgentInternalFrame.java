package casa.ui;

import casa.ML;
import casa.Status;
import casa.TransientAgent;
import casa.interfaces.TransientAgentInterface;
import casa.util.Trace;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.OutputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.Observable;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

/**
 * This class acts as a default windows interface for agents. The window looks
 * like this:
 *
 * <pre>
 *   +-------------------------------------------------------------+
 *   | Title Bar                                                   |
 *   +-------------------------------------------------------------+
 *   | Menu Bar                                makeJMenuBar()      |
 *   +-------------------------------------------------------------+
 *   | Agent URL Panel                    makeAgentURLPanel()      |
 *   +-------------------------------------------------------------+
 *   |  ______________   ________________   _____________________  |
 *   | /     CD       | /  Commitments   | /    Command          | |
 *   | |makeCDPanel() | |agent.          | |makeCommandTabPanel()| |
 *   | |              | |getStrategyGUI()| |                     | |
 *   | +---------------------------------------------------------+ |
 *   | |                                                         | |
 *   | |                                                         | |
 *   | |                                                         | |
 *   | +---------------------------------------------------------+ |
 *   +-------------------------------------------------------------+
 *
 * </pre>
 *
 * Subclasses may override any of the make*() factory methods to yield
 * appropriate behaviour.
 *
 * To add a tab, simply override the {@link #addPanels(Container, int)} method,
 * and if necessary the unsplit method to remove any additional panels
 * created.
 *
 * <p>
 * Copyright: Copyright 2003-2014, Knowledge Science Group, University of
 * Calgary. Permission to use, copy, modify, distribute and sell this software
 * and its documentation for any purpose is hereby granted without fee, provided
 * that the above copyright notice appear in all copies and that both that
 * copyright notice and this permission notice appear in supporting
 * documentation. The Knowledge Science Group makes no representations about the
 * suitability of this software for any purpose. It is provided "as is" without
 * express or implied warranty.
 * </p>
 *
 * @author <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer </a>
 * @version 0.9
 * @author <a href="ayala@cpsc.ucalgary.ca">Gabriel Becerra </a>
 * @author Eunice Lim
 * @author <a href="mailto:sedachv@cpsc.ucalgary.ca">Vladimir Sedach</a>
 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 */
public class TransientAgentInternalFrame extends AbstractInternalFrame
    implements ActionListener {

  protected JCheckBoxMenuItem menuAgent_setDebug;
  protected JMenuItem menuAgent_showTrace;
  protected JCheckBoxMenuItem menuAgent_showMessageArea;
  protected JMenuItem menuAgent_options;
  protected JMenuItem menuAgent_editOntology;
//  protected JMenuItem menuAgent_editOntologyGraphical;
  //  protected JMenuItem menuAgent_editActs;
  protected JMenuItem menuAgent_Exit;

  protected JMenu menuLAC;
  protected JMenuItem menuLAC_RegisterInstance;
  protected JMenuItem menuLAC_UnregisterInstance;

  protected JMenu menuYP;

  protected JMenu menuCD;
  protected JMenuItem menuCD_Join;

  protected JMenuItem menuWindow_showDebugBar;
  protected JMenuItem menuWindow_hideDebugBar;



  /**********************************************************
  /************ Tab panels in the interface ***************** 
  /**********************************************************
  /**
   * The pane that contains the various tab panels. It controls 
   * the splitting and unsplitting of panes.
   */
  protected final JPanel/*Tabs*/ contentPanel = new JPanel/*Tabs*/(new BorderLayout());
  
  
  
  /* We are using the Tabs class to deal with this 
  protected Vector<JTabbedPane> tabs = new Vector<JTabbedPane>();
  */
  
  protected JToolBar debugBar = new JToolBar();
  protected JButton pauseButton = new JButton();
  protected JButton resumeButton = new JButton();
  protected JButton stepButton = new JButton();

  JLabel pausedLabel = new JLabel("PAUSED");


  protected LinkedList<JSplitPane> splitPanes;

  //
  // Variables
  //
  protected boolean closingPort = false;

  //
  // Constants
  //
  protected static final int noAction = 0;
  protected static final int menuREGISTER = 1;
  protected static final int menuUNREGISTER = 2;
  protected static final int menuINVITECD = 3;
  protected static final int menuGETYELLOWPAGES = 4;
  protected static final int menuCLEARDEBUG = 7;
  protected static final int menuEXIT = 8;
  protected static final int menuJOINCD = 9;
  protected static final int menuWITHDRAWCD = 15;
  protected static final int menuSET_ACK_PROTOCOL = 16;
  protected static final int menuOPTIONS = 17;
  protected static final int menuSHOWTRACE = 18;
  protected static final int menuEDIT_ONTOLOGY = 19;
  //protected static final int menuEDIT_ML.TYPES = 20;
  protected static final int menuSHOW_MESSAGE_AREA = 21;
  protected static final int menuHIDE_MESSAGE_AREA = 22;
  protected static final int buttonEXECUTE = 23;

  protected static final int menuINFO = 27;
  protected static final int buttonPAUSE = 28;
  protected static final int buttonRESUME = 29;
  protected static final int buttonSTEP = 30;
  protected static final int menuSHOW_DEBUG_BAR = 31;
  protected static final int menuHIDE_DEBUG_BAR = 32;
  protected static final int menuEDIT_ONTOLOGY_GRAPHICAL = 33;
  protected static final int menuADD_NEW_TAB = 34;
  protected static final int menuUserManual = 35;
  protected static final int menuFIRST_DYNAMIC_EVENT = 40;
  protected static final int menuLAST_DYNAMIC_EVENT = 300;
  protected static final int transientAgentFrame_LAST_EVENT = 300;

  protected static final boolean VERTICAL = true;
  protected static final boolean HORIZONTAL = false;

//  protected MenuItems menuItems;
  

	static int DEFAULT_X = 0, DEFAULT_Y = 200;

	/**
   * Constructor: builds a TransientAgentInternalFrame by calling
   * {@link #makeJMenuBar makeJMenuBar}and {@link #addPanels}.
   *
   * @param agent the agent for which this an interface
   * @param title The title of the window
   * @param aFrame Either a JFrame or a JInternal Frame object to use for the
   *          frame.
   */
  public TransientAgentInternalFrame(TransientAgent agent,
      String title, Container aFrame) {
    super (agent, title, aFrame);
    assert agent!=null : "TransientAgentInternalFrame.constructor: agent parameter must not be null";
    runInEventDispatchThread(new Runnable() {@Override
		public void run () {
      setJMenuBar (makeMenuBar ());
      contentPanel.add(makeTabPane());
      setDebugBar();    
      getContentPane().add(contentPanel, BorderLayout.CENTER);
      menuWindow_showDebugBar.setEnabled(true);
      menuWindow_hideDebugBar.setEnabled(false);
      getContentPane().validate();
//      mapCommandsToMenu ();
      setOpaque(false);
      frame.setLocation(DEFAULT_X+=22, DEFAULT_Y+=22);
      
    }},true);
  }

  @Override
	public boolean takesHTML() {return commandPanel.takesHTML();}

//  protected void mapCommandsToMenu () {
//    menuItems = new MenuItems (this, menuAgentCommands);
//    menuAgentCommands.removeAll ();
//    RTCommandInterpreter.RTCommandInterpreterIterator i = agent
//        .getCommandInterpreterIterator ();
//    for (; i.hasNext ();) {
//      RTCommandInterpreter.ParamsCommandPair p = i.nextPair ();
//      if (p.name().charAt(0)=='.') continue;
//      String category = p.getCategory ();
//      if (category == null) {
//        category = p.name ();
//        category = category.replaceAll ("\\s+", "|");
//        if (category.charAt (0) == '?')
//          category = null;
//        else if (category.charAt (0) == '!')
//          category = null;
//        else if (category.indexOf ("|!") >= 0)
//          category = null;
//        else if (category.indexOf ("|?") >= 0)
//          category = null;
//      }
//      if (category != null) {
//        menuItems.put (category, p);
//      }
//    }
//    menuAgentCommands.invalidate ();
//  }

  /**
   * Adaptor method: when isInternalFrameClosable() returns true, this method 
   * calls the agent's exit() method to shut down the agent.
   */
  @Override
	protected void closeInternalFrame () {
  	super.closeInternalFrame();
    if (isInternalFrameClosable ()) {
      if (!agent.isExiting()) agent.exit ();
    }
  }

  /**
   * Factory Method to create a generic Agent menu bar. Subclasses may override
   * to customize the menu. Used in the constructor of a window.
   *
   * @return a generic Agent menu bar
   */
  @Override
	protected JMenuBar makeMenuBar () {
    JMenuBar menuBar = super.makeMenuBar();

    //
    // LAC menu
    //
    menuLAC = new JMenu("LAC");
    menuLAC.setMnemonic(KeyEvent.VK_L);

    menuLAC_RegisterInstance = new JMenuItem("Register...");
    menuLAC_RegisterInstance.setMnemonic(KeyEvent.VK_R);
    menuLAC_RegisterInstance.setActionCommand(String.valueOf(menuREGISTER));
    menuLAC_RegisterInstance.addActionListener(this);
    menuLAC.add(menuLAC_RegisterInstance);

    menuLAC_UnregisterInstance = new JMenuItem("Unregister...");
    menuLAC_UnregisterInstance.setMnemonic(KeyEvent.VK_U);
    menuLAC_UnregisterInstance.setActionCommand(String
                                                .valueOf(menuUNREGISTER));
    menuLAC_UnregisterInstance.addActionListener(this);
    menuLAC.add(menuLAC_UnregisterInstance);

    // set the register and unregister menu items to their correct check status
    java.awt.EventQueue.invokeLater(new Runnable() {
      @Override
			public void run() {
        boolean reg = agent.isRegistered();
        menuLAC_RegisterInstance.setEnabled(!reg);
        menuLAC_UnregisterInstance.setEnabled(reg);
      }
    });

    insertMenuBarAfter(menuLAC, "Agent Commands");

    //
    // YP menu
    //
    menuYP = new JMenu("Yellow Pages");
    menuYP.setMnemonic(KeyEvent.VK_Y);
    menuYP.setEnabled(false);

    JMenuItem menuYPGetLocations = new JMenuItem("Get locations...");
    menuYPGetLocations.setActionCommand(String.valueOf(menuGETYELLOWPAGES));
    menuYPGetLocations.addActionListener(this);
    menuYP.add(menuYPGetLocations);

    insertMenuBarAfter(menuYP, "LAC");

    /**
     * Cooperation Domains Menu
     */
    menuCD = new JMenu("Cooperation Domains");
    menuCD.setMnemonic(KeyEvent.VK_C);

    menuCD_Join = new JMenuItem("Join...");
    menuCD_Join.setMnemonic(KeyEvent.VK_J);
    menuCD_Join.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J,
        InputEvent.CTRL_MASK));
    menuCD_Join.setEnabled(true);
    menuCD_Join.setActionCommand(String.valueOf(menuJOINCD));
    menuCD_Join.addActionListener(this);
    menuCD.add(menuCD_Join);

    menuCD_Withdraw = new JMenuItem("Withdraw from selected");
    menuCD_Withdraw.setMnemonic(KeyEvent.VK_W);
    menuCD_Withdraw.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
        InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
    menuCD_Withdraw.setEnabled(false);
    menuCD_Withdraw.setActionCommand(String.valueOf(menuWITHDRAWCD));
    menuCD_Withdraw.addActionListener(this);
    menuCD.add(menuCD_Withdraw);

    menuCD_Invite = new JMenuItem("Send invitation...");
    menuCD_Invite.setMnemonic(KeyEvent.VK_I);
    menuCD_Invite.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,
        InputEvent.CTRL_MASK));
    menuCD_Invite.setEnabled(false);
    menuCD_Invite.setActionCommand(String.valueOf(menuINVITECD));
    menuCD_Invite.addActionListener(this);
    menuCD.add(menuCD_Invite);

    insertMenuBarAfter(menuCD, "Yellow Pages");

    /**
     * Window menu
     */
    menuWindow_showDebugBar = new JMenuItem("Show debug toolbar");
    menuWindow_showDebugBar.setMnemonic(KeyEvent.VK_D);
    menuWindow_showDebugBar.setAccelerator(KeyStroke.getKeyStroke(
        KeyEvent.VK_D, InputEvent.CTRL_MASK));
    menuWindow_showDebugBar.setActionCommand(String.valueOf(menuSHOW_DEBUG_BAR));
    menuWindow_showDebugBar.addActionListener(this);
    menuWindow.add(menuWindow_showDebugBar);

    menuWindow_hideDebugBar = new JMenuItem("Hide debug toolbar");
    menuWindow_hideDebugBar.setMnemonic(KeyEvent.VK_E);
    menuWindow_hideDebugBar.setAccelerator(KeyStroke.getKeyStroke(
        KeyEvent.VK_E, InputEvent.CTRL_MASK));
    menuWindow_hideDebugBar.setActionCommand(String.valueOf(menuHIDE_DEBUG_BAR));
    menuWindow_hideDebugBar.addActionListener(this);
    menuWindow.add(menuWindow_hideDebugBar);

    insertMenuBarAfter(menuWindow, "Cooperation Domains");


    return menuBar;
  }
  
  
  /**
   * implementation of the ActionListener interface
   *
   * @param e --> Object implemented as a listener. It interacts with
   *          getActionCommand() & intValue(). The value returned is assigned to
   *          the variable: command <-- used in the switch in order to set the
   *          activity of the Window, i.e. closeWindow()
   */
  @Override
	public void actionPerformed (ActionEvent e) {
    int command = noAction;
    try {
      command = Integer.valueOf(e.getActionCommand()).intValue();
    }
    catch (Exception ex) {
      command = noAction;
    }

    switch (command) {
      case menuREGISTER:
        registerAgent();
        break;
      case menuUNREGISTER:
        unregisterAgent();
        break;
      case menuGETYELLOWPAGES:
        //        agent.runGetYellowPages ();
        break;
      case buttonPAUSE:
        stepButton.setEnabled(true);
        pauseButton.setEnabled(false);
        resumeButton.setEnabled(true);
        pausedLabel.setVisible(true);
        agent.executeCommand("debugging true", this);
        agent.executeCommand("pause", this);
        break;
      case buttonSTEP:
        pausedLabel.setText("PAUSED  Errors:" + Trace.getErrors() + "  Warnings:" + Trace.getWarnings());
        agent.executeCommand("step", this);
        break;
      case buttonRESUME:
        pausedLabel.setVisible(false);
        pauseButton.setEnabled(true);
        stepButton.setEnabled(false);
        resumeButton.setEnabled(false);
        agent.executeCommand("debugging false", this);
        agent.executeCommand("resume", this);
        break;
      case menuEDIT_ONTOLOGY: {
        TypeEditDialog d = makeTypeEditDialog();
        d.display();
      }
      break;
      case menuINVITECD:
        inviteToCD();
        break;
      case menuJOINCD:
        joinCD();
        break;
      case menuWITHDRAWCD:
        withdrawCD();
        break;
      case menuSHOW_DEBUG_BAR:
        JPanel main = new JPanel(new BorderLayout());
        main.add(contentPanel, BorderLayout.CENTER);
        main.add(debugBar, BorderLayout.SOUTH);
        getContentPane().removeAll();
        getContentPane().add(main, BorderLayout.CENTER);
        menuWindow_showDebugBar.setEnabled(false);
        menuWindow_hideDebugBar.setEnabled(true);
        getContentPane().validate();
        break;
      case menuHIDE_DEBUG_BAR:
        getContentPane().removeAll();
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        menuWindow_showDebugBar.setEnabled(true);
        menuWindow_hideDebugBar.setEnabled(false);
        getContentPane().validate();
        break;
      default:
//        if (command >= menuFIRST_DYNAMIC_EVENT
//            && command <= menuLAST_DYNAMIC_EVENT) {
//          Status stat = menuItems.execute(command);
//          printStatus(stat, "(menu selection)");
//        }
//        else
      	Trace.log("error", 
              "AgentWindow().actionPerformed() - unexpected action command: "
              + command);
        break;
    }
  }

  protected void printStatus (Status status, String commandString) {
  	commandPanel.printStatus(status, commandString);
  }


  protected void setDebugBar(){
    URL pause = TransientAgentInternalFrame.class.getResource("/images/toolbarButtonGraphics/media/Pause10.gif");
    URL resume = TransientAgentInternalFrame.class.getResource("/images/toolbarButtonGraphics/media/Play10.gif");
    URL step = TransientAgentInternalFrame.class.getResource("/images/toolbarButtonGraphics/media/StepForward10.gif");
    if(pause != null){
      pauseButton.setIcon(new ImageIcon(pause));
    }
    if(resume != null){
      resumeButton.setIcon(new ImageIcon(resume));
    }
    if(step != null){
      stepButton.setIcon(new ImageIcon(step));
    }
    stepButton.setEnabled(false);
    pauseButton.setEnabled(true);
    resumeButton.setEnabled(false);

    pauseButton.setActionCommand(String.valueOf(buttonPAUSE));
    pauseButton.addActionListener(this);
    stepButton.setActionCommand(String.valueOf(buttonSTEP));
    stepButton.addActionListener(this);
    resumeButton.setActionCommand(String.valueOf(buttonRESUME));
    resumeButton.addActionListener(this);

    JPanel buttons = new JPanel();
    buttons.add(pauseButton);
    buttons.add(stepButton);
    buttons.add(resumeButton);

    pausedLabel.setText("PAUSED  Errors:" + Trace.getErrors() +"  Warnings:" + Trace.getWarnings());
    pausedLabel.setVisible(false);
    debugBar.setLayout(new BorderLayout());
    debugBar.add(buttons, BorderLayout.WEST);
    debugBar.add(pausedLabel, BorderLayout.EAST);
    debugBar.validate();
  }

  protected void inviteToCD () {
  	Trace.log("error", "TransientAgentInteralFrame.inviteToCD() not implemented");
  }

  /**
   * If the user chooses to join a CD, a dialog will pop up offering three
   * possibilities of joining a CD: local, create and Join a local CD, or
   * provide the IP:Port of a non-local CD.
   */
  private void joinCD () {
    new JoinCDDialog (getFrame (), true, agent);
  }

  private void registerAgent () {
  	runInEventDispatchThread(new Runnable() { @Override
		public void run () {
  		int choice = JOptionPane.showOptionDialog (frame, getRegistrationPanel (),
  				"Register Agent", JOptionPane.OK_CANCEL_OPTION,
  				JOptionPane.PLAIN_MESSAGE, null, null, null);

  		if (choice == JOptionPane.OK_OPTION) {
  			Status tempStatus = agent.doRegisterAgentInstance (Integer
  					.parseInt (port.getText ()));
  			if (tempStatus.getStatusValue () != 0) {
  				JOptionPane.showMessageDialog (frame,
  						"There was an error sending the message:\n"
  						+ tempStatus.getExplanation (), "Register Instance Failed",
  						JOptionPane.ERROR_MESSAGE);
  			}
  		}
  	}});
  }

  private JPanel registrationPanel = null;
  private JTextField port = null;

  private JPanel getRegistrationPanel () {
    if (registrationPanel == null) {
      registrationPanel = new JPanel (new GridBagLayout ());
      port = new JTextField (20);
      port.setToolTipText ("LAC's port number");

      GridBagConstraints gbc = new GridBagConstraints ();
      gbc.weightx = 0;
      gbc.weighty = 0;
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.anchor = GridBagConstraints.EAST;
      gbc.fill = GridBagConstraints.NONE;

      registrationPanel.add (new JLabel ("Port:"), gbc);
      gbc.gridx++;
      gbc.anchor = GridBagConstraints.CENTER;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.weightx = 1;
      registrationPanel.add (port, gbc);
    }
    return registrationPanel;
  }

  private void unregisterAgent () {
    runInEventDispatchThread(new Runnable () {
      @Override
			public void run () {
        String msg = "Ok to unregister from LAC at port"
            + agent.getURL ().getLACport () + "?";
        int choice = JOptionPane.showConfirmDialog (frame, msg,
            "Unregister Agent", JOptionPane.OK_CANCEL_OPTION);
        if (choice == JOptionPane.OK_OPTION) {
          Status tempStatus = agent.doUnregisterAgentInstance (true);
          if (tempStatus.getStatusValue () != 0) {
            JOptionPane.showMessageDialog (frame,
                "There was an error sending the message:\n"
                    + tempStatus.getExplanation (),
                "Unregister Instance Failed", JOptionPane.ERROR_MESSAGE);
          }
        }
      }
    });
  }


  @Override
	public void updateHandler(Observable observable, Object obj) {
		super.updateHandler(observable, obj);
  	if (commandPanel!=null) 
    	commandPanel.update(observable, obj);
	}
  
  /**
   * @param event
   * @param argObject
   */
  @Override
	public void updateEventHandler(Observable observable, String event, Object argObject) {
  	super.updateEventHandler(observable, event, argObject);

  	/* CLOSE_PORT******************************************************* */
    if (event.equals(ML.EVENT_CLOSE_PORT)){
    	if (closingPort) {
    		if (!agent.hasOpenPort ()) {
    			/** @todo update the URL in the interface to be unresolved */
            }
    	}
    }
    /* JOIN CD or WITHDRAW CD******************************************** */
    else if (event.equals(ML.EVENT_JOIN_CD) ||
    		event.equals(ML.EVENT_JOIN_CD_REPEATED) ||
    		event.equals(ML.EVENT_WITHDRAW_CD)){
    	updateCDListFromAgent();	
    }
    /* REGISTER********************************************************** */
    else if (event.equals(ML.EVENT_REGISTER_INSTANCE) ||
    		event.equals(ML.EVENT_UNREGISTER_INSTANCE)){
    	runInEventDispatchThread(new Runnable () {
    		@Override
				public void run () {
    			boolean registered = false;
    			registered = agent.isRegistered ();

    			if (menuLAC_RegisterInstance != null)
    				menuLAC_RegisterInstance.setEnabled (!registered);
    			if (menuLAC_UnregisterInstance != null)
    				menuLAC_UnregisterInstance.setEnabled(registered);
            	}
          });
    }
    /**
     * @todo THIS IMPLEMENATION IS COMPLETELY WRONG!!! it deletes any
     *       selected CD, not the one that said it was exiting!!!
     */
    else if (event.equals(ML.EVENT_EXITING)){
    	if (agent.equals(argObject)) {
    		//closeInternalFrame (); //done on ML.EVENT_EXITED        		
    	}
    	else { //must be a CD that's exiting
    		updateCDListFromAgent();
    	}	
    }
    else if (event.equals(ML.EVENT_EXITED)){
    	if (agent.equals(argObject)) {
    		closeInternalFrame ();        		
    	}
    	else { //must be a CD that's exiting
    		//updateCDListFromAgent(); //done on ML.EVENT_EXITING
    	}	
    }
    else if (event.equals(ML.EVENT_CHANGED_COMMANDS)){
//    	runInEventDispatchThread (new Runnable () {
//            public void run () {
//            	mapCommandsToMenu ();
//            }
//    	});
    }
    else if (event.equals(ML.EVENT_STRATEGY_CHANGED)){
    	runInEventDispatchThread(new Runnable () {
            @Override
						public void run () {
            	updateStrategyPanel ();
            }
        });	
    }
    else if (event.equals(ML.EVENT_CD_NEW_MEMBER) || event.equals(ML.EVENT_GET_CD_PARTICIPANTS)){ 
     	runInEventDispatchThread(new Runnable () {
        @Override
				public void run () {
          updateMemberList();
        }
    });	
    }
}


  /**
   * These variables are a work around so that we may access them in anonymous classes
   * particularly that found in print(String,boolean).
   */
  public void print (String tempTxt, boolean tempNormal, final String color) {
  	commandPanel.print(tempTxt, tempNormal, color);
  }

  @Override
	public void print(String txt){
    commandPanel.print(txt);
  }


  @Override
	public void println (String txt) {
    commandPanel.println(txt);
  }

  @Override
	public String ask (String prompt, String help, int type, String _default) {
  	return commandPanel.ask(prompt, help, type, _default);
  }
  
	/**
	 * Returns an output stream that can be used to write to the interface
	 */
	@Override
	public OutputStream getOutStream() {
		return commandPanel.getOutStream();
	}
  
  public TransientAgentInterface getTransientAgent () {
    return agent;
  }

//  class MenuItems {
//    TreeMap<Integer,MenuRecord> map = new TreeMap<Integer,MenuRecord> ();
//    TransientAgentInternalFrame owner;
//    JMenu menu;
//
//    public MenuItems (TransientAgentInternalFrame owner, JMenu menu) {
//      this.owner = owner;
//      this.menu = menu;
//    }
//
//    public int put (String category, RTCommandInterpreter.ParamsCommandPair p) {
//      int i;
//      for (i = menuFIRST_DYNAMIC_EVENT; map.get (new Integer (i)) != null; i++)
//        ;//find an empty record
//      map.put (new Integer (i), new MenuRecord (i, category, p.name ()));
//      String cat[] = category.split ("\\|");
//      for (int j = cat.length - 1; j >= 0; j--)
//        cat[j] = cat[j].trim ();
//      put (menu, cat, 0, p, i);
//      return i;
//    }
//
//    private void put (JMenu menu, String[] cat, int i,
//        RTCommandInterpreter.ParamsCommandPair p, int event) {
//      JMenuItem m = null;
//      for (int k = 0, end = menu.getItemCount (); k < end; k++) {
//        try {
//          m = menu.getItem (k);
//          if (m.getText ().equals (cat[i])) {
//            if (i < cat.length - 1) { // follow down the menu hierarchy
//              try {
//                JMenu submenu = (JMenu) m;
//                put (submenu, cat, i + 1, p, event);
//                return;
//              } catch (Exception ex1) {//this is an item, not a menu: replace
//                // with a submenu
//                menu.remove (k);
//                /** @todo remove the replaced item from menuItems */
//                JMenu submenu = new JMenu (cat[i]);
//                menu.add (submenu);
//                put (submenu, cat, i + 1, p, event);
//                return;
//              }
//            } else { // a replace
//              menu.remove (k);
//              put (menu, cat[i], event);
//              /** @todo remove the replaced item from menuItems */
//              return;
//            }
//          }
//        } catch (Exception ex) {
//          continue;
//        }
//      }
//
//      if (i < cat.length - 1) { // create a new menu in the menu hierarchy
//        JMenu item = new JMenu (cat[i]);
//        menu.add (item);
//        put (item, cat, i + 1, p, event);
//        return;
//      } else { // add a new item
//        put (menu, cat[i], event);
//        return;
//      }
//    }
//
//    private void put (JMenu m, String title, int event) {
//      JMenuItem item = new JMenuItem (title);
//      item.setActionCommand (String.valueOf (event));
//      item.addActionListener (owner);
//      m.add (item);
//    }
//
//    public Status execute (int i) {
//      MenuRecord r = map.get (new Integer (i));
//      if (r != null)
//        return agent.executeCommand (r.commandString, owner);
//      return null;
//    }
//
//    class MenuRecord {
//      String category;
//      String commandString;
//      int id;
//
//      MenuRecord (int id, String category, String commandString) {
//        this.id = id;
//        this.category = category;
//        this.commandString = commandString;
//      }
//    }
//  }
//
  	/**
  	 * Starts displaying the UI
  	 */
  	@Override
		public void start() {
  		commandPanel.start();
	}
  


}