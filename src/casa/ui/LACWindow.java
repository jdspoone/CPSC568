package casa.ui;

import casa.CASAProcess.ProcessInfo;
import casa.CasaObservableObject;
import casa.LACOptions;
import casa.ML;
import casa.MLMessage;
import casa.ObserverNotification;
import casa.Status;
import casa.TransientAgent;
import casa.interfaces.PolicyAgentInterface;
import casa.interfaces.ProcessInterface;
import casa.util.Trace;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowListener;
import java.io.OutputStream;
import java.net.URL;
import java.util.Observable;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
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
public class LACWindow extends /*AbstractInternalFrame*/ProcessWindow implements WindowListener, AgentUI, ActionListener /*Observer*/ {
  private casa.LAC       lac;
  private CasaAgentTree agentTree;

	/**
   * Constrcutor
   * @param lac
   */
  public LACWindow(casa.LAC lac, String[] args) {
  	super(lac, lac.getBanner(), args);

    this.lac = lac;
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
  @Override
	protected void init() {
    ProcessInfo.process = this.lac;
    super.init();
//    desktopFrame = new LACDesktop();
//
//    // Split direction, leftFrame, rightFrame
//    lacWindow = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, lacLeftFrame, desktopFrame);
//    lacWindow.setContinuousLayout(true);
//    lacWindow.setDividerLocation(215);
//    lacWindow.setOneTouchExpandable(true);
//
//
//    getContentPane().add(lacWindow);
//
//     //read in the window size to the LAC's persistent options, using appropriate defaults
//    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//    String cn = getClass().getName()+".";	    
//    Boolean isIcon      = false;                                                  try {isIcon = lac.getBooleanProperty(cn+"isIcon");} catch (PropertyException e) {}
//    Boolean isMaximized = false;                                                  try {isMaximized = lac.getBooleanProperty(cn+"isMaximized");} catch (PropertyException e) {}
//    int xPos = 0;                                                                 try {xPos  = lac.getIntegerProperty(cn+"xPos");}  catch (PropertyException e) {}
//    int yPos = 0;                                                                 try {yPos  = lac.getIntegerProperty(cn+"yPos");}  catch (PropertyException e) {}
//    int xSize = (screenSize.width  > 1400) ? 1340 : screenSize.width  - xPos * 2; try {xSize = lac.getIntegerProperty(cn+"xSize");} catch (PropertyException e) {}
//    int ySize = (screenSize.height >  900) ? 840  : screenSize.height - yPos * 2; try {ySize = lac.getIntegerProperty(cn+"ySize");} catch (PropertyException e) {}
//    jFrame.setBounds( xPos, yPos, xSize, ySize);
//    if (isIcon) jFrame.setExtendedState(Frame.ICONIFIED);
//    else if (isMaximized) jFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
//    
//    OsX.addQuitHandler(new OsX.QuitHandler(){
//		@Override
//		public void handleQuit() {
//			closeWindow();
//		}
//    });
  }
  
  @Override
	JTabbedPane makeTreeTabPane() {
  	JTabbedPane pane;
		try {
			pane = super.makeTreeTabPane();
	  	agentTree = new CasaAgentTree(/*(LACOptions)casaProcess.getOptions()*/null, desktopFrame.desktop);
	  	agentTree.setLayout (new GridLayout (1,1));
	    pane.addTab("All Agents", agentTree);
		} catch (Throwable e) {
			agent.println("error", "LACWindow.makeTreeTabPane(): failed to create tree pane.", e);
			pane = new JTabbedPane();
		}
    return pane;
  }
  
  @Override
  protected JTabbedPane makeTabPane() {
  	JTabbedPane pane = super.makeTabPane();
  	
  	return pane;
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
        lac.executeCommand("debugging true", this);
        lac.executeCommand("pause", this);
        break;
      case buttonSTEP:
        pausedLabel.setText("PAUSED  Errors:" + Trace.getErrors() + "  Warnings:" + Trace.getWarnings());
        lac.executeCommand("step", this);
        break;
      case buttonRESUME:
        pausedLabel.setVisible(false);
        pauseButton.setEnabled(true);
        stepButton.setEnabled(false);
        resumeButton.setEnabled(false);
        lac.executeCommand("debugging false", this);
        lac.executeCommand("resume", this);
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

  @Override
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



  @Override
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
   * method that sets the LACDesktopManager - Links it with the DesktopWatcher
   */
  private void setDesktopWatcher() {
  	this.desktopManagerObject = new MDIDesktopManager(desktopFrame.desktop, desktopFrame.scroller);
    desktopFrame.desktop.setDesktopManager(this.desktopManagerObject);
  }


  @Override
  protected JMenu makeAgentMenu() {
  	JMenu ret = super.makeAgentMenu();
  	menuItemShowInactive = new JCheckBoxMenuItem("Show Inactive Agents",
        agentTree==null?false:agentTree.getShowInactive());
    menuItemShowInactive.setActionCommand(String.valueOf(menuSET_SHOW_INACTIVE));
    menuItemShowInactive.addActionListener(new ActionListener() {
      @Override
			public void actionPerformed(ActionEvent e) {
        agentTree.setShowInactive(menuItemShowInactive.getState());
        agentTree.refresh();
      }});
    ret.insert(menuItemShowInactive,0);

    ret.insertSeparator(1);

    menuEditTypes = new JMenuItem("Edit Agent Types.");
    menuEditTypes.setMnemonic(KeyEvent.VK_T);
    menuEditTypes.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,InputEvent.CTRL_MASK));
    menuEditTypes.setActionCommand(String.valueOf(menuEDITTYPES));
    menuEditTypes.addActionListener(new ActionListener() {
      @Override
			public void actionPerformed(ActionEvent e) {
        new AgentTypeDialog(/*LAC.LACinfo.desktop*/jFrame,lac,"Agent Type Editor",true,null);
      }});
    int n = ret.getItemCount();
    ret.insert(menuEditTypes,n-3);
    
    return ret;
  }
  
  private void eventAgentMatch(ActionEvent e, @SuppressWarnings("unused") MLMessage message) {
    String command = e.getActionCommand();
       lac.informAgent_GUIOperationRequest (this.guiObserver.get(command), command);
  }

  /**
   * @return TypeEditDialog
   */
  @Override
	protected TypeEditDialog makeTypeEditDialog() {
    String title = "Edit ontology for agent "+lac.getAgentName();
    return new TypeEditDialog(/*LAC.LACinfo.desktop*/jFrame,
                              title,
                              false, lac);
  }

  /**
   * Check consistency between the outcome of the options dialog box and any
   * Corresponding menu items and the various panes.  Fix them.
   */
  @Override
	protected void updateOptions() {
  	super.updateOptions();
    agentTree.setShowInactive(((LACOptions)lac.getProcessOptions()).showInactiveAgents);
    menuItemShowInactive.setSelected(((LACOptions)lac.getProcessOptions()).showInactiveAgents);
    agentTree.setCreateProxyWindows(((LACOptions)lac.getProcessOptions()).createProxyWindows);
  }

  /**
   * Observer Interface.
   * Uses {@link ObserverNotification} and
   * {@link ML}.
   */
  	@Override
  	public void updateEventHandler(final Observable observable, String event, final Object obj) {
  		super.updateEventHandler(observable, event, obj);
  		if (event.equals(ML.EVENT_EXITED)){
  			if (agent.equals(obj)) {
  				closeInternalFrame ();        		
  			}
  		}
  	}
  
  	@Override
  	public void updateHandler(final Observable observable, final Object obj) {
  		super.updateHandler(observable, obj);
  		ObserverNotification state = null;
  		if (obj instanceof ObserverNotification) { 
  			state = ((ObserverNotification) obj);
  		}
  		final String event = (state != null) ? state.getType() : null;
  		if (event == null) return; //No NullPointerExceptions
  		if (event.equals(ML.EVENT_REGISTER_INSTANCE_LOCAL)){
  			if (agentTree!=null) agentTree.update(observable,obj);
  		}
  		else if (event.equals(ML.EVENT_REGISTER_INSTANCE_REMOTE)){
  			if (agentTree!=null) agentTree.update(observable,obj);
  		}
  		else if (event.equals(ML.EVENT_UNREGISTER_INSTANCE)){
  			if (agentTree!=null) agentTree.update(observable,obj);
  			ProcessInterface pi = ((CasaObservableObject)observable).getAgent();
  			removeFrame(findFrameForAgent(pi instanceof PolicyAgentInterface ? (TransientAgent)pi : null));
  		}
  	}
  

  long closingWaitTime = 0;
  boolean closeInProgress = false;

  
}
