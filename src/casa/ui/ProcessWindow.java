package casa.ui;

import casa.CASAProcess.ProcessInfo;
import casa.CasaObservableObject;
import casa.ML;
import casa.ObserverNotification;
import casa.Status;
import casa.TransientAgent;
import casa.interfaces.PolicyAgentInterface;
import casa.interfaces.ProcessInterface;
import casa.platform.Generic;
import casa.system.SocketServerDialog;
import casa.util.CASAUtil;
import casa.util.PropertyException;
import casa.util.Trace;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Observable;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

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
 * @author <a href="mailto:sedachv@cpsc.ucalgary.ca">Vladimir Sedach</a>
 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 */
public class ProcessWindow extends /*JFrame*/AbstractInternalFrame implements WindowListener, AgentUI, ActionListener /*Observer*/ {
  //
  // JAVA Swing Objects
  //
  protected JSplitPane  lacWindow;
  protected JSplitPane  lacLeftFrame;
  protected JCheckBoxMenuItem menuItemShowInactive;
  //private JCheckBoxMenuItem menuCreateProxyWindows;
  protected JMenuItem menuSystemClearTags;
  protected JMenuItem menuSystemCASAPreferences;
  protected JMenuItem menuSystemLACDaemon;

    protected JMenuItem menuWindow_showDebugBar;
    protected JMenuItem menuWindow_hideDebugBar;

    protected JLabel pausedLabel = new JLabel();
  protected JToolBar debugBar = new JToolBar();
  protected JButton pauseButton = new JButton();
  protected JButton resumeButton = new JButton();
  protected JButton stepButton = new JButton();
  //
  // CASA Objects
  //
  protected LACDesktop     desktopFrame;
  protected casa.CASAProcess       casaProcess;
//  private CasaAgentTree agentTree;
  protected MDIDesktopManager desktopManagerObject;

  //
  // Variables and Constants
  //
  protected String userNameORTitle  = new String();
  protected String args[];
  protected int lacPort = 9000;

  public boolean protection;
  protected Vector<TransientAgentInternalFrame> currentFrames = new Vector<TransientAgentInternalFrame>();
  
//  /**
//	 * Constrcutor
//	 * @param lac
//	 */
//	public LACWindow(casa.LAC lac, String[] args, String userName) {
//		this(lac, args);
//	}

  /**
   * 
   * @param casaProcess
   * @param args
   * @wbp.parser.constructor
   */
  public ProcessWindow(casa.CASAProcess casaProcess, String[] args) {
  	this(casaProcess, casaProcess.getBanner(), args); 
  }

  public ProcessWindow(casa.CASAProcess casaProcess, String name, String[] args) {
  	super(casaProcess, name, new JFrame());

  	this.casaProcess = casaProcess;
  	this.args = args;
  	this.userNameORTitle = casaProcess.getURL().getUser();

  	final ProcessWindow This = this;
  	runInEventDispatchThread(new Runnable() {@Override
  		public void run () {
  		commandPanel = makeCommandPanel();
  		init();
  		setJMenuBar(makeMenuBar());

  		jFrame.addWindowListener(This);

  		if (CustomIcons.FRAME_ICON.getImage()!=null)
  			jFrame.setIconImage(CustomIcons.FRAME_ICON.getImage());

  		setVisible(true);
  	}},true);

  	this.casaProcess.addObserver(this);

  }

  /**
   * Handles the creation of the LACWindow internal components such as the JSplitPane which
   * contains two main splits:
   * <ul>
   * <li>A LeftFrame that is composed of the LAC's tree and JTabbedPane</li>
   * <ol>
   *     <li>Viewer: DesktopViewer</li>
   *     <li>LACInfo: Provides information about this LAC</li>
   * </ol>
   * <li>A RightFrame composed if the desktop environment</li>
   * <li>A menu that hanldes:</li>
   * <ol>
   *     <li>The Creation of new ChatAgents</li>
   *     <li>Navigating through each one of the JInternalFrames (i.e. cascade or bring to front)</li>
   * </ol>
   * </ul>
   *
   * See inner comments for more specific actions
   */
  protected void init() {
    ProcessInfo.process = this.casaProcess;

    desktopFrame = new LACDesktop();

    // Provides a JTabbedPane, which contains two tabs: DesktopViewer and LACDescription
    //JTabbedPane pane = setTabPanel();


    // Setting the LACLeftFrame up.   It consists of an inner JSplitPane that contains the
    // LACTreePane as the TopComponent and the WindowWatcher as the BottomComponent
    setDesktopWatcher();
    lacLeftFrame = new JSplitPane(JSplitPane.VERTICAL_SPLIT, makeTreeTabPane(), desktopManagerObject.ww);
    lacLeftFrame.setContinuousLayout(true);
    lacLeftFrame.setDividerLocation(550);
    lacLeftFrame.setOneTouchExpandable(true);

    // Split direction, leftFrame, rightFrame
    lacWindow = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, lacLeftFrame, makeTabPane()/*rightFrame*/);
    lacWindow.setContinuousLayout(true);
    lacWindow.setDividerLocation(215);
    lacWindow.setOneTouchExpandable(true);

    setDebugBar();


    getContentPane().add(lacWindow);

     //read in the window size to the LAC's persistent options, using appropriate defaults
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    String cn = getClass().getName()+".";	    
    Boolean isIcon      = false;                                                  try {isIcon = casaProcess.getBooleanProperty(cn+"isIcon");} catch (PropertyException e) {}
    Boolean isMaximized = false;                                                  try {isMaximized = casaProcess.getBooleanProperty(cn+"isMaximized");} catch (PropertyException e) {}
    int xPos = 0;                                                                 try {xPos  = casaProcess.getIntegerProperty(cn+"xPos");}  catch (PropertyException e) {}
    int yPos = 0;                                                                 try {yPos  = casaProcess.getIntegerProperty(cn+"yPos");}  catch (PropertyException e) {}
    int xSize = (screenSize.width  > 1400) ? 1340 : screenSize.width  - xPos * 2; try {xSize = casaProcess.getIntegerProperty(cn+"xSize");} catch (PropertyException e) {}
    int ySize = (screenSize.height >  900) ? 840  : screenSize.height - yPos * 2; try {ySize = casaProcess.getIntegerProperty(cn+"ySize");} catch (PropertyException e) {}
    jFrame.setBounds( xPos, yPos, xSize, ySize);
    if (isIcon) jFrame.setExtendedState(Frame.ICONIFIED);
    else if (isMaximized) jFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
    
//    Generic.addQuitHandler(new Generic.QuitHandler(){
//    	@Override
//    	public void handleQuit() {
//    		closeWindow();
//    	}
//    });
  }
  
  JTabbedPane makeTreeTabPane() {
    return new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
  }
  
	@Override
  protected JTabbedPane makeTabPane() {
		JTabbedPane tabPane = super.makeTabPane();
    insertTabAfter("Desktop", desktopFrame, "Command", null);
    try {
    	Class<?> kinstance = Class.forName("karta.KartaInstance");
    	Object karta = kinstance.newInstance();
    	java.lang.reflect.Method getUIPanel = kinstance.getMethod("getUIPanel", new Class[] {});
    	addTab("Graphs", (JPanel)getUIPanel.invoke(karta, (Object)new Class[] {}), true);
    }
    catch (Throwable e) {
    	// error trying to load karta, ignore it
    }
    //addTab("Command", commandPanel);
		
		return tabPane;
	}

  
  @Override
	public boolean takesHTML() {return commandPanel.takesHTML();}

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
    /*    case buttonEXECUTE:
        String c = commandTextField.getText();
        if (c != null && c.length() > 0) {
          Status status = lac.execute(c, this);
          commandTextField.setText("");
          printStatus(status, c);
        break;
        */
      case buttonPAUSE:
        stepButton.setEnabled(true);
        pauseButton.setEnabled(false);
        resumeButton.setEnabled(true);
        pausedLabel.setVisible(true);
        casaProcess.executeCommand("debugging true", this);
        casaProcess.executeCommand("pause", this);
        break;
      case buttonSTEP:
        pausedLabel.setText("PAUSED  Errors:" + Trace.getErrors() + "  Warnings:" + Trace.getWarnings());
        casaProcess.executeCommand("step", this);
        break;
      case buttonRESUME:
        pausedLabel.setVisible(false);
        pauseButton.setEnabled(true);
        stepButton.setEnabled(false);
        resumeButton.setEnabled(false);
        casaProcess.executeCommand("debugging false", this);
        casaProcess.executeCommand("resume", this);
        break;
      case menuSHOW_DEBUG_BAR:
        menuWindow_showDebugBar.setEnabled(false);
        menuWindow_hideDebugBar.setEnabled(true);
        lacWindow.remove(getTabPane());
        JSplitPane main = new JSplitPane(JSplitPane.VERTICAL_SPLIT, getTabPane(), debugBar);
        main.setDividerLocation(740);
        lacWindow.setRightComponent(main);
        lacWindow.validate();
        break;
      case menuHIDE_DEBUG_BAR:
        menuWindow_showDebugBar.setEnabled(true);
        menuWindow_hideDebugBar.setEnabled(false);
        lacWindow.remove(lacWindow.getRightComponent());
        lacWindow.setRightComponent(getTabPane());
        lacWindow.validate();
        break;

    }
  }

  protected void printStatus (Status status, String commandString) {
  	commandPanel.printStatus(status, commandString);
  }


  @Override
	public void print (String txt) {
  	commandPanel.print(txt);
  }

	/**
	 * Returns an output stream that can be used to write to the interface
	 */
	@Override
	public OutputStream getOutStream() {
		return commandPanel.getOutStream();
	}



  protected void setDebugBar(){
      URL pause = TransientAgentInternalFrame.class.getResource("/images/toolbarButtonGraphics/media/Pause16.gif");
      URL resume = TransientAgentInternalFrame.class.getResource("/images/toolbarButtonGraphics/media/Play16.gif");
      URL step = TransientAgentInternalFrame.class.getResource("/images/toolbarButtonGraphics/media/StepForward16.gif");
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

  /**
   * Handles the creation of the JTabbedPane and its components:
   * <ol>
   *     <li>DestopWatcher</li>
   *     <li>LACInfo</li>
   * </ol>
   * @return JTabbedPane object
   */
	@Override
	protected JPanel setInfoPanel () {
    Vector<String> lacVector = new Vector<String>();
    JPanel lacInfoPanel = new JPanel();
    int counter = setInfoPanel(lacInfoPanel, lacVector);
//    JList lacInfoList;
//    JScrollPane infoLACScroller;

    try {
			File file = new File(casaProcess.getCASAFilename());
			if (file.exists()) {
			  lacVector.add(counter++, "");
			  lacVector.add(counter++, "Last modified: " + CASAUtil.getDateAsString(file.lastModified()));
			}
		} catch (FileNotFoundException e) {
			lacVector.add(counter++, "");
			lacVector.add(counter++, "CASA persistence file not available.");
		}

//    lacInfoList = new JList(lacVector);
//    infoLACScroller = new JScrollPane(lacInfoList);
//
//    lacInfoPanel.setLayout(new GridLayout(1, 1));
//    lacInfoPanel.add(infoLACScroller, BorderLayout.CENTER);
//    lacInfoPanel.setBorder(BorderFactory.createLineBorder(Color.blue));
    lacInfoPanel.validate();
    return lacInfoPanel;
  }



  /**
   * method that sets the LACDesktopManager - Links it with the DesktopWatcher
   */
  private void setDesktopWatcher() {
  	this.desktopManagerObject = new MDIDesktopManager(desktopFrame.desktop, desktopFrame.scroller);
    desktopFrame.desktop.setDesktopManager(this.desktopManagerObject);
  }

  protected JMenu createSystemMenu() {
    //System Menu
    JMenu menuSystem = new JMenu("System");
    menuSystem.setMnemonic(KeyEvent.VK_S);

    menuSystemClearTags = new JMenuItem("Clear known trace tags");
    menuSystemClearTags.setMnemonic(KeyEvent.VK_C);
    //menuSystemClearTags.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,InputEvent.CTRL_MASK));
    menuSystemClearTags.setActionCommand(String.valueOf(menuSYSTEM_CLEAR_TAGS_TYPES));
    menuSystemClearTags.addActionListener(new ActionListener() {
    @Override
		public void actionPerformed(ActionEvent e) {
      Trace.clearKnownTags();
    }});
    menuSystem.add(menuSystemClearTags);
    
    menuSystemCASAPreferences = new JMenuItem("Modify CASA preferences...");
    menuSystemCASAPreferences.setMnemonic(KeyEvent.VK_C);
    //menuSystemCASAPreferences.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,InputEvent.CTRL_MASK));
    menuSystemCASAPreferences.setActionCommand(String.valueOf(menuSYSTEM_Preferences));
    menuSystemCASAPreferences.addActionListener(new ActionListener() {
    @Override
		public void actionPerformed(ActionEvent e) {
      org.ksg.casa.CASAPreferencesDialog.main(new String[]{});
    }});
    menuSystem.add(menuSystemCASAPreferences);
    
    if (SocketServerDialog.isOSSupported(System.getProperty("os.name"))) {
    	menuSystemLACDaemon = new JMenuItem("Enable or Disable LAC system daemon...");
    	menuSystemLACDaemon.setMnemonic(KeyEvent.VK_C);
    	//menuSystemLACDaemon.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,InputEvent.CTRL_MASK));
    	menuSystemLACDaemon.setActionCommand(String.valueOf(menuSYSTEM_LAC_DAEMON_TYPES));
    	menuSystemLACDaemon.addActionListener(new ActionListener() {
    		@Override
    		public void actionPerformed(ActionEvent e) {
    			casa.system.SocketServerDialog.main(new String[]{});
    		}});
    	menuSystem.add(menuSystemLACDaemon);
    }
    return menuSystem;
  }
  	
  /**
   * Method used to create a menuBar for the Main Frame.
   * @return menuBar
   */
  @Override
	protected JMenuBar makeMenuBar() {
  	JMenuBar menuBar = super.makeMenuBar();

    insertMenuBarAfter(createSystemMenu(),"Agent Commands");

    // Adding a menu that handles the "navigational" actions in the Desktop
    menuWindow_showDebugBar = new JMenuItem("Show debug toolbar");
    menuWindow_showDebugBar.setMnemonic(KeyEvent.VK_D);
    menuWindow_showDebugBar.setAccelerator(KeyStroke.getKeyStroke(
        KeyEvent.VK_D, InputEvent.CTRL_MASK));
    menuWindow_showDebugBar.setActionCommand(String.valueOf(menuSHOW_DEBUG_BAR));
    menuWindow_showDebugBar.addActionListener(this);

    menuWindow_hideDebugBar = new JMenuItem("Hide debug toolbar");
    menuWindow_hideDebugBar.setMnemonic(KeyEvent.VK_E);
    menuWindow_hideDebugBar.setAccelerator(KeyStroke.getKeyStroke(
        KeyEvent.VK_E, InputEvent.CTRL_MASK));
    menuWindow_hideDebugBar.setActionCommand(String.valueOf(menuHIDE_DEBUG_BAR));
    menuWindow_hideDebugBar.addActionListener(this);
    menuWindow_hideDebugBar.setEnabled(false);

    Vector<JMenuItem> extraItems = new Vector<JMenuItem>();
    extraItems.add(menuWindow_showDebugBar);
    extraItems.add(menuWindow_hideDebugBar);
//    insertMenuBarAfter(new LACWindowMenu(desktopFrame.desktop, extraItems), "System");
    replaceMenuBar(menuWindow = new LACWindowMenu(this, desktopFrame.desktop, extraItems));
    return menuBar;
  }


  /**
   * <p>Title: Observer Interface
   * <p>Description: adapted from the previous update(). The structure 
   * belongs to the author of the previous version.  Update() now uses {@link ObserverNotification} and
   * {@link ML}.</p>
   * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
   * @version 0.9
   */
  @Override
  public void updateEventHandler(final Observable observable, String event, final Object obj) {
  	super.updateEventHandler(observable, event, obj);
  	if (event == null) return; //No NullPointerExceptions
  	if (event.equals(ML.EVENT_BANNER_CHANGED)) {
  		setTitle((String)obj);
  	}
  	if (event.equals(ML.EVENT_REGISTER_INSTANCE_LOCAL)){
  		//  					if (agentTree!=null) agentTree.update(observable,argument);
  	}
  	else if (event.equals(ML.EVENT_REGISTER_INSTANCE_REMOTE)){
  		//  					if (agentTree!=null) agentTree.update(observable,argument);
  	}
  	else if (event.equals(ML.EVENT_UNREGISTER_INSTANCE)){
  		//  					if (agentTree!=null) agentTree.update(observable,argument);
  		ProcessInterface pi = ((CasaObservableObject)observable).getAgent();
  		removeFrame(findFrameForAgent(pi instanceof PolicyAgentInterface ? (TransientAgent)pi : null));
  	}
  	else if (event.equals(ML.EVENT_EXITED)){
  		if (agent.equals(obj)) {
  			closeInternalFrame ();        		
  		}
  	}
  }
  
	@Override
	public void updateHandler(Observable observable, Object obj) {
		super.updateHandler(observable, obj);
  	if (commandPanel!=null) 
    	commandPanel.update(observable, obj);
	}
  
  /**
   * Search for and return a frame in the agent view frame that represents
   * the <em>agent</em>.
   * @param agent find the frame for this agent
   * @return the frame that represents the <em>agent</em> or null if no such frame exists
   */
  public TransientAgentInternalFrame findFrameForAgent(PolicyAgentInterface agent) {
		if (currentFrames != null && agent != null) {
			for (TransientAgentInternalFrame frame : currentFrames) {
				if (frame.agent.equals(agent))
					return frame;
			}
		}
		return null;
	}
  
  /**
   * Remove a frame by removing it and calling dispose()
   * on the frame.
   * @param frame the frame to be destroyed (may be null, in which case this method does nothing).
   * @return true if the frame was removed.
   */
  public boolean removeFrame(TransientAgentInternalFrame frame) {
  	boolean ret = false;
  	if (frame!=null) {
  	  ret = currentFrames.remove(frame);
  	  frame.dispose();
  	}
		return ret;
	}
  
  //
  // WindowListener Section
  //
  protected boolean isWindowClosable() { return true; }
  @Override
	public void windowClosing    (WindowEvent e) {
    closeWindow();
  }
  @Override
	public void windowDeactivated(WindowEvent e) {}
  @Override
	public void windowDeiconified(WindowEvent e) {}
  @Override
	public void windowActivated  (WindowEvent e) {}
  @Override
	public void windowIconified  (WindowEvent e) {}
  @Override
	public void windowOpened     (WindowEvent e) {}
  @Override
	public void windowClosed     (WindowEvent e) {}

  long closingWaitTime = 0;
  boolean closeInProgress = false;

  /**
   * Adaptor method.
   */
  @Override
  public void doDefaultCloseAction () {
  	closeWindow();	
  }
  
  /**
   * When the user either exits the system from the File -> Exit <code>JMenuItem</code> or from the
   * default closing button of this <code>JFrame</code>, the following event will occur:
   * <ol>
   *     <li> Prompt the user if the exit event should really happen</li>
   *     <li> If the user confirms the terminate event, the <code>LAC</code> will trigger a set of
   *          messages to all its registered agents.  These messages inform that the <code>LACWindow</code>
   *          is closing.  The action that the agents will take is unregister.instance. </li>
   * </ol>
   *
   */
  @Override
  protected void closeWindow() {
  	if (closeInProgress)
  		return;
  	closeInProgress = true;

  	//write out the window size to the LAC's persistent options
  	Rectangle r = jFrame.getBounds();
  	String cn = getClass().getName()+".";
  	casaProcess.setBooleanProperty(cn+"isIcon",jFrame.getExtendedState()==Frame.ICONIFIED);
  	casaProcess.setBooleanProperty(cn+"isMaximized",jFrame.getExtendedState()==Frame.MAXIMIZED_BOTH);
  	casaProcess.setIntegerProperty(cn+"xPos",r.x);
  	casaProcess.setIntegerProperty(cn+"yPos",r.y);
  	casaProcess.setIntegerProperty(cn+"xSize",r.width);
  	casaProcess.setIntegerProperty(cn+"ySize",r.height);

  	int closeOption = 0;
  	ImageIcon i = CustomIcons.REAL_INNER_ICON;
  	String message = "Are you sure you want CASA to Terminate?";
  	String title   = "Terminating all agents in this processes...";
  	closeOption = JOptionPane.showConfirmDialog(null, message, title,
  			JOptionPane.OK_CANCEL_OPTION,
  			JOptionPane.QUESTION_MESSAGE, i);
  	if (closeOption == JOptionPane.OK_OPTION) {
  		if (isWindowClosable ()) {
  			casaProcess.exit();
  		}
  	} else if (closeOption == JOptionPane.CANCEL_OPTION) {
  		this.setVisible(true);
  		jFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
  		closeInProgress = false;
  	} else {
  		this.setVisible(true);
  		jFrame.setDefaultCloseOperation (JFrame.DO_NOTHING_ON_CLOSE);
  		closeInProgress = false;
  	}
  }

  protected void saveWindowInfo() {
  	casa.io.CASAFile windowInfoFile = new casa.io.CASAFile("window_info.casa");
  	casa.io.CASAFilePropertiesMap windowInfoMap = new casa.io.CASAFilePropertiesMap(windowInfoFile);
  	String name;
  	JInternalFrame[] frames = desktopFrame.desktop.getAllFrames();
  	for (int x = 0; x < frames.length; x++)
  	{
  		name = frames[x].getName();
  		windowInfoMap.setInteger(name + ".xPos", frames[x].getX());
  		windowInfoMap.setInteger(name + ".yPos", frames[x].getY());
  		windowInfoMap.setInteger(name + ".xSize", frames[x].getHeight());
  		windowInfoMap.setInteger(name + ".ySize", frames[x].getWidth());
  		windowInfoMap.writeProperties();
  	}
  }
  
  public void addAgentWindow(TransientAgentInternalFrame w) {
    desktopFrame.desktop.add(w.frame);
    currentFrames.add(w);
  }


  @Override
	public void println(String txt) {
  	if (commandPanel!=null) 
  		commandPanel.println(txt);
  }

  @Override
	public String ask(String prompt, String help, int type, String _default) {
    return commandPanel.ask(prompt, help, type, _default);
  }
  
	@Override
	public void start() {
		commandPanel.start();
	}

	public JFrame getJFrame() {
		return jFrame;
	}
 
}
