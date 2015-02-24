package casa.ui;

import casa.CASAProcess;
import casa.ML;
import casa.MLMessage;
import casa.ObserverNotification;
import casa.Status;
import casa.TransientAgent;
import casa.agentCom.URLDescriptor;
import casa.exceptions.URLDescriptorException;
import casa.extensions.Extension;
import casa.extensions.ExtensionLoader;
import casa.extensions.ExtensionsDialog;
import casa.platform.Generic;
import casa.util.CASAUtil;
import casa.util.Trace;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.EventListener;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.ksg.casa.CASA;

/**
 * This class implements the common functionality needed by windows that act
 * as interfaces for CASA agents.  These windows must act as either a JFrame or
 * a JInternalFrame (which have no common superclass (except Container).  So this
 * class must act as an Adaptor (Gamme, 1995) for both classes.<br>
 *
 * The idea behind CASA agent interfaces is that the agents themselves are "unaware"
 * of the interface, but rather, the interfaces work through the Observer
 * pattern to track asynchronous activities of the agent, and make calls to the agent to
 * affect changes in the agent.
 *
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
public abstract class AbstractInternalFrame extends ObservingAgentUI
  implements AgentUI, InternalFrameListener, WindowListener {

  //protected static final int CHAT_AGENT = 0;
  protected static final int noAction = 0;
  protected static final int menuNEW_MENU_ITEM = 1;
  protected static final int menuSET_SHOW_INACTIVE = 2;
  protected static final int menuSET_CREATE_PROXY_WINDOWS = 3;
  protected static final int menuSETDEBUG = 4;
  protected static final int menuSHOWTRACE = 5;
  protected static final int menuOPTIONS = 6;
  protected static final int menuEDITTYPES = 7;
  protected static final int menuEDIT_PERFORMATIVE_TYPES = 8;
  protected static final int menuEDIT_ACT_TYPES = 9;
 // protected static final int buttonEXECUTE = 10;
  protected static final int menuINFO = 11;
  protected static final int buttonPAUSE = 12;
  protected static final int buttonRESUME = 13;
  protected static final int buttonSTEP = 14;
  protected static final int menuSHOW_DEBUG_BAR = 15;
  protected static final int menuHIDE_DEBUG_BAR = 16;
  protected static final int menuSYSTEM_CLEAR_TAGS_TYPES = 17;
  protected static final int menuSYSTEM_LAC_DAEMON_TYPES = 18;
  protected static final int menuSYSTEM_Preferences = 19;
  protected static final int menuUserManual = 20;
  protected static final int menuHowTo = 21;
  protected static final int menuJavaDoc = 22;
  protected static final int menuHomePage = 23;
  protected static final int menuHelpLisp = 24;
  protected static final int menuHelpOWL2 = 25;
  protected static final int menuHelpJade = 26;
  
  protected JMenuItem menuShowTrace;
  protected JMenuItem menuOptions;
  protected JMenuItem menuEditTypes;
  protected JMenuItem menuAgent_editPerformatives;
  protected JMenuItem menuAgent_editActs;
  
  protected JMenuItem menuCD_Invite;
  protected JMenuItem menuCD_Withdraw;
  
  protected JMenu menuWindow;
  protected JMenu menuTools;
  protected JMenu menuScripts;

  protected JMenu menuHelp;
  protected JMenuItem menuHelp_info;
  protected JMenuItem menuHelp_umanual;
  protected JMenuItem menuHelp_htmanual;
  protected JMenuItem menuHelp_javadoc;
  protected JMenuItem menuHelp_homepage;
  protected JMenuItem menuHelp_helpLisp;
  protected JMenuItem menuHelp_helpOWL2;
  protected JMenuItem menuHelp_helpJade;
  
  protected JMenu menuAgent;
  protected JMenu menuAgentCommands;
  
  /**
   * The list of CDs in the {@link #cdsPanel}.
   */
  protected JList listCDs = new JList();
  /**
   * The list of member agents in the CD that has focus (selected) in {@link #listCDs}; part of {@link #cdsPanel}.
   */
  protected JList listCDMembers;
  


  
  /**
   * The menu bar
   */
	private JMenuBar menuBar;
	
	/**
   * The frame used by this class.  Is the same as JInternalFrame or JFrame (the other
   * will be null).
   * @see java.awt.JFrame
   * @see javax.swing.JInternalFrame
   */
  protected Container frame = null;
  /**
   * Set to either the same value as frame or null, it will be the same as frame
   * only if this is an independent window.
   */
  protected JFrame jFrame = null;
  /**
   * Set to either the same value as frame or null, it will be the same as frame
   * only if this is a JInternalFrame, usually a frame within the LAC window.
   */
  protected JInternalFrame jInternalFrame = null;

  /**
   * The agent that this window is attached to.
   */
  protected TransientAgent process;
  
  protected Hashtable<String, URLDescriptor> guiObserver = new Hashtable<String, URLDescriptor> ();
  
	protected CommandPanel commandPanel;
	
	private JTabbedPane tabPane;
	protected JTabbedPane getTabPane() {return tabPane;}
	private TreeMap<String, Component> knownTabs = new TreeMap<String, Component>();

	/**
	 * Adds a new script to the script menu.
	 * @param title The title of the script (used in the menu).
	 * @param component The component in the tab body.
	 * @param visible TODO
	 */
	public void addScript(JMenuItem menuItem) {
		menuScripts.add(menuItem);
	}
	
	/**
	 * Adds a new tab to the tab pane.
	 * @param title The title in the tab.
	 * @param component The component in the tab body.
	 * @param visible TODO
	 */
	public void addTab(String title, Component component, boolean visible) {
		knownTabs.put(title, component);
		if (visible) {
			if (getTab(title)==null)
				tabPane.add(title, component);
		}
		else
			if (getTabIndexOf(title)!=-1)
				removeTab(title);
		refreshTabMenu();
	}
//	/**
//	 * Removes the tab that contains a particular component.
//	 * @param component The component to remove.
//	 */
//	public void removeTab(Component component) {
//		tabPane.remove(component);
//	}
	/**
	 * Removes the tab with the label <em>name</em>
	 * @param name The label name to remove.
	 */
	public void removeTab(String name) {
		int index = tabPane.indexOfTab(name);
		if (index > 0)
			tabPane.remove(index);
	}

	public void insertTabAfter(String title, Component component, String name, String toolTipText) {
  	int i = 0;
  	if (name==null) { 
  		i = 0; // first position
  	}
  	else {
  		i = getTabIndexOf(name);
  		if (i>=0) 
  			i++; // the position after the found name
  		else
  		  i = tabPane.getTabCount(); // last
  	}
  	insertTab(title, component, i, toolTipText);
	}

	public void insertTab(String title, Component component, int location, String toolTipText) {
  	int count = tabPane.getTabCount();
  	if (location>count)
  		location = count;
  	if (location<0) 
  		location = 0;
    tabPane.insertTab(title, null, component, toolTipText, location);
	}

	public void replaceTabComponent(String title, Component component, String toolTipText) {
		int index = getTabIndexOf(title);
		if (index<0) {
			index = tabPane.getTabCount();
			insertTab(title, component, index, null);
		}
		else {
			tabPane.setComponentAt(index, component);
		}
		if (toolTipText!=null)
			tabPane.setToolTipTextAt(index, toolTipText);
		tabPane.invalidate();
	}

	private int getTabIndexOf(String name) {
		return tabPane.indexOfTab(name);
	}
	/**
	 * Returns the Component associated with the tab with label <em>name</em>
	 * @param name The name to search for.
	 * @return The Component with <em>name</em> or null if the <em>name</em> is not found.
	 */
	public Component getTab(String name) {
		try {
			return tabPane.getComponent(getTabIndexOf(name));
		} catch (Throwable e) {
			return null;
		}
	}
	/**
	 * Set the tab with the label <em>name</em> as the selected tab.
	 * @param name The name to select.
	 * @return
	 */
	public boolean setSelectedTab(String name) {
		int i = tabPane.indexOfTab(name);
		if (i>=0) { 
			tabPane.setSelectedIndex(i);
			return true;
		} 
		return false;
	}

	protected JTabbedPane makeTabPane() {
		if (tabPane==null) {
			tabPane = new JTabbedPane();
			boolean visible = tabPane.isVisible();
			tabPane.setVisible(false);
			commandPanel = makeCommandPanel();
			addTab( "Command", commandPanel, true);
			addTab( "CD", makeCDPanel(), true);
			updateStrategyPanel();
			addTab("Conversations", new ConversationPanel(agent, tabPane), true);
			addTab("Policies", new PolicyPanel(agent, tabPane), true);
			tabPane.setVisible(visible);
		}
		try {
			//			TabJar.lookupJars(agent, this);
			ExtensionLoader.loadType("tab", this, agent);
		} catch (Exception e) {
			agent.println("error", "TabJar.lookupJars() failed.", e);
		}
		return tabPane;
	}
	
  /**
   * @param title The title of the tab Component to search for.
   * @return the tabbed Component under the <em>title</em> tab.
   */
	public Component getTabComponent(String title) {
		for (int i=tabPane.getTabCount()-1; i>=0; i--) {
			if (title.equals(tabPane.getTitleAt(i))) {
				return tabPane.getComponentAt(i);
			}
		}
		return null;
	}
	
	static boolean appIconSet = false;
	private static String buildTime = null;

  /**
   * Constructor
   * Sets the process and the title of this window, and determines if this
   * is a JFrame, or JInternalFrame -type window.
   *
   * @param theProcess AbstractProcess set as this object process
   * @param title String to be set as this windows title
   * @param aFrame Container of type either JInternalFrame or JFrame to use for this window
   */
  public AbstractInternalFrame (final TransientAgent theProcess, final String title, Container aFrame) {
  	super(theProcess);
  	
  	validate();
  	frame = aFrame;
  	if (aFrame.getClass ().isAssignableFrom (JInternalFrame.class)) {
  		jInternalFrame = (JInternalFrame) aFrame;
  	} else {
  		jFrame = (JFrame) aFrame;
  	}
  	
  	final AbstractInternalFrame This = this;
  	
    listCDMembers = getListCDMembers();
    
  	runInEventDispatchThread(new Runnable() {@Override
		public void run () {
  		getContentPane().setLayout(new BorderLayout());

  		setTitle (title);
  		setMaximizable (true);
  		setIconifiable (true);
  		setClosable (true);
  		setResizable (true);
  		//if (CustomIcons.FRAME_ICON!=null) 
  			//setFrameIcon(CustomIcons.FRAME_ICON);
  		
  		process = theProcess;
  		//java.awt.EventQueue.invokeLater(new Runnable () {public void run () {
  		theProcess.addObserver (This);
  		//}});

  		// Added in order to comply with the internal frame events instead of window events
  		//addInternalFrameListener (this);
  		addFrameListener (This);

    	String osName = System.getProperty("os.name");
//    	if (!appIconSet) {
    	java.net.URL url = ClassLoader.getSystemResource("images/customGraphics/casa.png");
    	Toolkit kit = Toolkit.getDefaultToolkit();
    	Image img = kit.getImage(url);
    	if (osName.contains("OS X")) {
    		Generic.setDocIconImage(img);
    	}
    	else {
    		List<Image> imgs = new LinkedList<Image>();
    		imgs.add(img);
    		getFrame().setIconImages(imgs);
    		getFrame().setIconImage(img);
    	}
    	appIconSet = true;
    	//    	}
    	
    	Generic.addQuitHandler(new Generic.QuitHandler(){
      	@Override
      	public void handleQuit() {
      		closeWindow();
      	}
      });
  	}},true);
  }
  
  protected void closeWindow() {
  	validate();
    if (jInternalFrame == null) {
      agent.exit();
    }
  }

  /**
   * Factory Method to create a generic Agent menu bar. Subclasses may override
   * to customize the menu. Used in the constructor of a window.
   *
   * @return a generic Agent menu bar
   */
  protected JMenuBar makeMenuBar () {
    menuBar = new JMenuBar();

		//set the system property to make the Aqua look-and-feel use a top-of-screen menu bar
		System.setProperty("apple.laf.useSceenMenuBar", "true");

    menuAgent = makeAgentMenu();
		menuBar.add(menuAgent);

    menuAgentCommands = new JMenu("Agent Commands");
    menuBar.add(menuAgentCommands);
    
    menuTools = makeToolsMenu();
    menuBar.add(menuTools);
    
    menuWindow = makeWindowMenu();
    menuBar.add(menuWindow);
    
    menuHelp = makeHelpMenu();
    menuBar.add(menuHelp);
    
    return menuBar;
  }
  
  boolean avoidRecursion1 = false;
  protected void refreshTabMenu() {
  	if (avoidRecursion1)
  		return;
  	try {
  		avoidRecursion1 = true;
  		if (menuTools==null)
  			return;
  		JMenu menu = new JMenu("Tabs");
  		for (String s: knownTabs.keySet()) {
  			final String fs = s;
  			JCheckBoxMenuItem item = new JCheckBoxMenuItem(s);
  			item.addItemListener(new ItemListener() {
  				@Override
  				public void itemStateChanged(ItemEvent e) {
  					addTab(fs, knownTabs.get(fs), e.getStateChange()==ItemEvent.SELECTED);
  				}
  			});
  			menu.add(item);
  			item.setSelected(getTabIndexOf(fs)!=-1);
  		}
  		int count = menuTools.getMenuComponentCount();
  		int index = -1;
  		for (int i=count-1; i>=0; i--) {
  			Component c = menuTools.getMenuComponent(i);
  			if (c instanceof JMenu) {
  				if ("Tabs".equals(((JMenu)c).getText())) {
  					index = i;
  					break;
  				}
  			}
  		}
  		if (index>=0) {
  			menuTools.remove(index);
  			menuTools.insert(menu, index);
  		}
  		else {
  			menuTools.insert(menu, 0);
  		}
  	}
  	catch (Throwable e) {
  		e.printStackTrace();
  		agent.println("error", "AbstractInternalFrame.refreshTabMenu()", e);
  	}
  	finally {
  		avoidRecursion1 = false;
  	}
  }

  protected JMenu makeWindowMenu() {
  	menuWindow = new JMenu("Window");
  	menuWindow.setMnemonic(KeyEvent.VK_W);
  	return menuWindow;
  }

  protected JMenu makeToolsMenu() {
    /**
     * Tools menu
     */
  	menuTools = new JMenu("Tools");
  	menuTools.setMnemonic(KeyEvent.VK_T);
    
		menuScripts = new JMenu("Scripts");
		menuTools.add(menuScripts);
		ExtensionLoader.loadType(Extension.TYPE_LISPSCRIPT, this, agent);	
		
		menuTools.addSeparator();

		JMenuItem item = new JMenuItem("Extensions");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					ExtensionsDialog dialog = new ExtensionsDialog();
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setVisible(true);
				} catch (Exception ex) {
					agent.println("error", "Cannot bring up Extensions Dialog", ex);
				}
			}
		});
		menuTools.add(item);
		
		item = new JMenuItem("Install Extension...");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
			    JFileChooser chooser = new JFileChooser();
			    FileNameExtensionFilter filter = new FileNameExtensionFilter(
			        "Jars or Lisp scripts", "jar", "lisp");
			    chooser.setFileFilter(filter);
			    int returnVal = chooser.showOpenDialog(null);
			    if(returnVal == JFileChooser.APPROVE_OPTION) {
			    	File theFile = chooser.getSelectedFile();
			    	String home = System.getProperty("user.home");
			    	File target = new File(home+"/.casa/extensions/"+theFile.getName());
						java.nio.file.Files.copy(theFile.toPath(), target.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
						ExtensionLoader.main(new String[]{});
			    }
				} catch (Exception ex) {
					agent.println("error", "Cannot bring up Extensions Dialog", ex);
				}
			}
		});
		menuTools.add(item);
		

    Runnable r = new Runnable() {
			@Override
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						refreshTabMenu();
					}
				});
			}
		};
    
    agent.defer(r, 1000); //give the interface a chance to finish then build the window menu based on the registered tabs.
   
    return menuTools;
  }
  
  protected JMenu makeHelpMenu() {
  	/*Help Menu*/
  	JMenu menuHelp = new JMenu("Help");
  	menuHelp.setMnemonic(KeyEvent.VK_H);

  	menuHelp_info = new JMenuItem("About");
  	menuHelp_info.setMnemonic(KeyEvent.VK_I);
//  	menuHelp_info.setAccelerator(KeyStroke.getKeyStroke(
//  			KeyEvent.VK_I, InputEvent.CTRL_MASK));
  	menuHelp_info.setActionCommand(String.valueOf(menuINFO));
    menuHelp_info.addActionListener(new ActionListener() {
    @Override
		public void actionPerformed(ActionEvent e) {
      JOptionPane.getRootFrame().removeAll();
      JOptionPane.showMessageDialog(getRootPane(), setInfoPanel(), "Info", JOptionPane.INFORMATION_MESSAGE);
    }});
  	menuHelp.add(menuHelp_info);

  	menuHelp.addSeparator();

  	menuHelp_javadoc = new JMenuItem("CASA Home Page");
  	menuHelp_javadoc.setMnemonic(KeyEvent.VK_P);
//  	menuHelp_javadoc.setAccelerator(KeyStroke.getKeyStroke(
//  			KeyEvent.VK_P, InputEvent.CTRL_MASK));
  	menuHelp_javadoc.setActionCommand(String.valueOf(menuHomePage));
    menuHelp_javadoc.addActionListener(new ActionListener() {
      @Override
  		public void actionPerformed(ActionEvent e) {
      	casa.web.URLVeiwer.displayURL("http://casa.cpsc.ucalgary.ca/index.html");
      }});
  	menuHelp.add(menuHelp_javadoc);

  	menuHelp_umanual = new JMenuItem("User Manual");
  	menuHelp_umanual.setMnemonic(KeyEvent.VK_U);
//  	menuHelp_umanual.setAccelerator(KeyStroke.getKeyStroke(
//  			KeyEvent.VK_U, InputEvent.CTRL_MASK));
  	menuHelp_umanual.setActionCommand(String.valueOf(menuUserManual));
    menuHelp_umanual.addActionListener(new ActionListener() {
      @Override
  		public void actionPerformed(ActionEvent e) {
      	casa.web.URLVeiwer.displayURL("http://casa.cpsc.ucalgary.ca/doc/CasaUserManual.pdf");
      }});
  	menuHelp.add(menuHelp_umanual);

  	menuHelp_htmanual = new JMenuItem("How-To Manual");
  	menuHelp_htmanual.setMnemonic(KeyEvent.VK_H);
//  	menuHelp_htmanual.setAccelerator(KeyStroke.getKeyStroke(
//  			KeyEvent.VK_H, InputEvent.CTRL_MASK));
  	menuHelp_htmanual.setActionCommand(String.valueOf(menuHowTo));
    menuHelp_htmanual.addActionListener(new ActionListener() {
      @Override
  		public void actionPerformed(ActionEvent e) {
      	casa.web.URLVeiwer.displayURL("http://casa.cpsc.ucalgary.ca/doc/CasaHowTo.pdf");
      }});
  	menuHelp.add(menuHelp_htmanual);

  	menuHelp_homepage = new JMenuItem("Java Doc");
  	menuHelp_homepage.setMnemonic(KeyEvent.VK_J);
//  	menuHelp_homepage.setAccelerator(KeyStroke.getKeyStroke(
//  			KeyEvent.VK_H, InputEvent.CTRL_MASK));
  	menuHelp_homepage.setActionCommand(String.valueOf(menuJavaDoc));
    menuHelp_homepage.addActionListener(new ActionListener() {
      @Override
  		public void actionPerformed(ActionEvent e) {
      	casa.web.URLVeiwer.displayURL("http://casa.cpsc.ucalgary.ca/javadoc/index.html");
      }});
  	menuHelp.add(menuHelp_homepage);
  	
  	menuHelp.addSeparator();

  	menuHelp_helpLisp = new JMenuItem("Common Lisp");
  	menuHelp_helpLisp.setMnemonic(KeyEvent.VK_J);
//  	menuHelp_helpLisp.setAccelerator(KeyStroke.getKeyStroke(
//  			KeyEvent.VK_H, InputEvent.CTRL_MASK));
  	menuHelp_helpLisp.setActionCommand(String.valueOf(menuHelpLisp));
    menuHelp_helpLisp.addActionListener(new ActionListener() {
      @Override
  		public void actionPerformed(ActionEvent e) {
      	casa.web.URLVeiwer.displayURL("http://www.cs.cmu.edu/Groups/AI/html/cltl/cltl2.html");
      }});
  	menuHelp.add(menuHelp_helpLisp);

  	menuHelp_helpOWL2 = new JMenuItem("OWL2");
  	menuHelp_helpOWL2.setMnemonic(KeyEvent.VK_J);
//  	menuHelp_helpOWL2.setAccelerator(KeyStroke.getKeyStroke(
//  			KeyEvent.VK_H, InputEvent.CTRL_MASK));
  	menuHelp_helpOWL2.setActionCommand(String.valueOf(menuHelpOWL2));
    menuHelp_helpOWL2.addActionListener(new ActionListener() {
      @Override
  		public void actionPerformed(ActionEvent e) {
      	casa.web.URLVeiwer.displayURL("http://www.w3.org/TR/owl2-primer/");
      }});
  	menuHelp.add(menuHelp_helpOWL2);

  	menuHelp_helpJade = new JMenuItem("Jade Semantic Extension");
  	menuHelp_helpJade.setMnemonic(KeyEvent.VK_J);
//  	menuHelp_helpJade.setAccelerator(KeyStroke.getKeyStroke(
//  			KeyEvent.VK_H, InputEvent.CTRL_MASK));
  	menuHelp_helpJade.setActionCommand(String.valueOf(menuHelpJade));
    menuHelp_helpJade.addActionListener(new ActionListener() {
      @Override
  		public void actionPerformed(ActionEvent e) {
      	casa.web.URLVeiwer.displayURL("http://jade.tilab.com/doc/tutorials/SemanticsProgrammerGuide.pdf");
      }});
  	menuHelp.add(menuHelp_helpJade);

  	return menuHelp;
  }

  
  /**
   * Inserts a new menu.
   * @param menu The menu to insert.
   * @param location The location at which to isert the new menu (<=0 = first; >n = last). 
   */
  public void insertMenuBar(JMenu menu, int location) {
  	int count = menuBar.getComponentCount();
  	if (location>count)
  		location = count;
  	if (location<0) 
  		location = 0;
    menuBar.add(menu, location);
  }
  
  /**
   * Inserts a new menu in the agent menu bar
   * @param menu The menu to insert
   * @param name The name AFTER which to insert the new menu; if null it
   * will be first, if not found it will be "last" (but before Help, etc).
   */
  public void insertMenuBarAfter(JMenu menu, String name) {
  	int i = 0;
  	if (name==null) { 
  		i = 0; // first position
  	}
  	else {
  		i = getMenuBarIndexOf(name);
  		if (i>=0) 
  			i++; // the position after the found name
  		else
  		  i = menuBar.getComponentCount(); // last
  	}
  	insertMenuBar(menu, i);
  }
  
  /**
   * Inserts a new menu in the agent menu bar
   * @param menu The menu to insert
   * @param name The name BEFORE which to insert the new menu; ; if null it
   * will be first, if not found it will be "last" (but before Help, etc).
   */
  public void insertMenuBarBefore(JMenu menu, String name) {
  	int i = 0;
  	if (name==null) { 
  		i = 0; // first position
  	}
  	else {
  		i = getMenuBarIndexOf(name);
  		if (i<0) 
  		  i = menuBar.getComponentCount(); // last
  	}
  	insertMenuBar(menu, i);
  }
  
  /**
   * Replace the menu with the same name.  If the name doesn't exist, the
   * new menu is added at the end.
   * @param menu The menu to replace the old one with.
   */
  public void replaceMenuBar(JMenu menu) {
  	String text = menu.getText();
  	assert text!=null;
  	int i = getMenuBarIndexOf(text);
  	if (i>=0) {
  		menuBar.remove(i);
  		menuBar.add(menu,i);
  	}
  	else
  		menuBar.add(menu,menuBar.getComponentCount()-1);
  }

  /**
   * @param name The name to find.
   * @return The index if the menu named <em>name</em>.
   */
	private int getMenuBarIndexOf(String name) {
  	int i = 0;
  	for (Component c: menuBar.getComponents()) {
  		if (c instanceof JMenu) {
  			JMenu m = (JMenu)c;
  			String n = m.getText();
    		if (n.equalsIgnoreCase(name)) 
    			return i;
  		} 
  		i++;
  	}
  	return -1;
  }

	/**
	 * @param name The menu label to find
	 * @return The menu with the label <em>name</em>.
	 */
	public JMenu getMenuBarMenu(String name) {
  	for (Component c: menuBar.getComponents()) {
  		if (c instanceof JMenu) {
  			JMenu m = (JMenu)c;
  			String n = m.getText();
    		if (n.equalsIgnoreCase(name)) 
    			return m;
  		} 
  	}
  	return null;
  }


  protected JMenu makeAgentMenu() {
  	String name = agent.getClass().getCanonicalName();
  	if (name.indexOf('.')>=0)
  	  name = name.substring(name.lastIndexOf('.')+1);
    JMenu menu = new JMenu(name);
    menu.setMnemonic(KeyEvent.VK_A);

    menuShowTrace = new JMenuItem("Show trace window");
    menuShowTrace.setMnemonic(KeyEvent.VK_T);
    menuShowTrace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,InputEvent.CTRL_MASK));
    menuShowTrace.setActionCommand(String.valueOf(menuSHOWTRACE));
    menuShowTrace.addActionListener(new ActionListener() {
      @Override
			public void actionPerformed(ActionEvent e) {
      	AbstractInternalFrame.runInEventDispatchThread (new Runnable () {
          @Override
					public void run () {
              agent.startTraceMonitor ();
          }
        });
       }});
    menu.add(menuShowTrace);

    menuOptions = new JMenuItem("Options...");
    menuOptions.setMnemonic(KeyEvent.VK_O);
    menuOptions.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,InputEvent.CTRL_MASK));
    menuOptions.setActionCommand(String.valueOf(menuOPTIONS));
    menuOptions.addActionListener(new ActionListener() {
      @Override
			public void actionPerformed(ActionEvent e) {
        OptionsDialog d = new OptionsDialog(jFrame,"Options for agent: "+agent.getAgentName (),true,agent.getOptions());
        if (d.display()) 
        	updateOptions();
      }});
    menu.add(menuOptions);

    menuAgent_editPerformatives = new JMenuItem("Edit ontology...");
    menuAgent_editPerformatives.setMnemonic(KeyEvent.VK_P);
    menuAgent_editPerformatives.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,InputEvent.CTRL_MASK));
    menuAgent_editPerformatives.setActionCommand(String.valueOf(menuEDIT_PERFORMATIVE_TYPES));
    menuAgent_editPerformatives.addActionListener(new ActionListener() {
    @Override
		public void actionPerformed(ActionEvent e) {
      TypeEditDialog d = makeTypeEditDialog();
      d.display();
    }});
    menu.add(menuAgent_editPerformatives);

    menuAgent_editActs = new JMenuItem("Edit act types...");
    menuAgent_editActs.setMnemonic(KeyEvent.VK_A);
    menuAgent_editActs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,InputEvent.CTRL_MASK));
    menuAgent_editActs.setActionCommand(String.valueOf(menuEDIT_ACT_TYPES));
    menuAgent_editActs.addActionListener(new ActionListener() {
    @Override
		public void actionPerformed(ActionEvent e) {
      TypeEditDialog d = makeTypeEditDialog();
      d.display();
    }});
    menu.add(menuAgent_editActs);

    menu.addSeparator();

    // File|Exit
    JMenuItem menuItem = new JMenuItem("Exit");
    menuItem.setMnemonic(KeyEvent.VK_X);
    menuItem.addActionListener(new ActionListener() {
      @Override
			public void actionPerformed(ActionEvent e) {
        doDefaultCloseAction();
      }
    });
    menu.add(menuItem);

    return menu;
  }
  
  /**
   * Makes and returns the CD panel that looks like:
   *  +----------------------------------------------+
   *  | +-------------------+----------------------+ |
   *  | |Cooperation Domains|CD Participants       | |
   *  | |-------------------|----------------------| |
   *  | |     listCDs     |^|     listCDMembers  |^| |
   *  | |       in        | |         in         | | |
   *  | |   scrollCDlist  | |ScrollCDparticipants| | |
   *  | |                 |v|                    |v| |
   *  | +-------------------+----------------------+ |
   *  +----------------------------------------------+
   * All components placed in vectors will be at index i.
   *
   * @return
   */
  protected JPanel makeCDPanel () {
  	final JPanel dummyPanel = new JPanel();
  	AbstractInternalFrame.runInEventDispatchThread(new Runnable() {
			@Override
			public void run() {
        JPanel panelCDparticipants = new JPanel ();
        JPanel panelCDlist = new JPanel ();
        JSplitPane panelCDtop = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT,
            panelCDlist, panelCDparticipants);
        panelCDtop.setResizeWeight (0.5);
        panelCDtop.setDividerSize (4);
    
        panelCDlist.setLayout (new BorderLayout ());
        JLabel labelCDlist = new JLabel ();
        panelCDlist.add (labelCDlist, BorderLayout.NORTH);
        JScrollPane scrollCDlist = new JScrollPane ();
        panelCDlist.add (scrollCDlist, BorderLayout.CENTER);
    
        panelCDparticipants.setLayout (new BorderLayout ());
        JLabel labelCDparticipants = new JLabel ();
        panelCDparticipants.add (labelCDparticipants, BorderLayout.NORTH);
        JScrollPane scrollCDparticipants = new JScrollPane ();
        panelCDparticipants.add (scrollCDparticipants, BorderLayout.CENTER);
          ListModel model = listCDs.getModel();
          int size = model.getSize();
          URLDescriptor[] urls = new URLDescriptor[size];
          for (int i = 0; i<size; i++) {
          	urls[i] = (URLDescriptor)model.getElementAt(i);
           }
          listCDs.setListData(urls);
          listCDs.setSelectedIndices(listCDs.getSelectedIndices());
    
        scrollCDlist.getViewport ().setView (listCDs);
        scrollCDlist.setPreferredSize (new Dimension (245, 100));
    
        // Sean says that this removeAll() is a hack! It removes the CellRendererPane component in this panel,
        // which magically appeared one day.
        listCDMembers.removeAll();
        
          scrollCDparticipants.getViewport().setView(listCDMembers);
          scrollCDparticipants.setPreferredSize(new Dimension(245, 100));
    
          labelCDlist.setText(" Cooperation Domains");
          labelCDlist.setLabelFor(listCDs);
          labelCDlist.setPreferredSize(new Dimension(100, 19));
    
          labelCDparticipants.setText(" CD Participants");
          labelCDparticipants.setLabelFor(listCDMembers);
          labelCDparticipants.setPreferredSize(new Dimension(65, 19));
    
          listCDs.addListSelectionListener(new ListSelectionListener() {
						@Override
						public void valueChanged(ListSelectionEvent arg0) {
					    if (!arg0.getValueIsAdjusting ()) {
				        if (listCDs != null) {
				          if ( listCDs.getSelectedIndex() > -1) {
				            CDSelectionChanged();
				          }
				        }
				    }
						}
					});
          listCDs.setToolTipText("Subscribed CDs");
          listCDs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
          listCDMembers.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
          listCDMembers.setToolTipText("Select participants to receive message");
    
          CDMembersMouseHook(listCDMembers);
          
        Set<URLDescriptor> list = agent.getJoinedCooperationDomains ();
        listCDs.setListData (list.toArray());
        if (menuCD_Invite != null) {
          menuCD_Invite.setEnabled (list != null && !list.isEmpty ());
        }
        //    CDSelectionChanged ();
    
        //dummyPanel = new JPanel ();
        dummyPanel.setLayout (new GridLayout (1, 1));
        dummyPanel.add (panelCDtop);
        dummyPanel.setPreferredSize(new Dimension(500, 100));
		}},true);
    return dummyPanel;
  }
  
  /**
   * Updates the listCDMembers JLists and enables or disables the menuCD_Withdrawn menu item
   * according to the parameter.
   * <br>
   * NOTE: THIS METHOD SHOULD BE CALLED ONLY IN AWT'S EDT THREAD (invokeLater()).
   * @param memberList A Vector of URLDescriptors.
   */
  protected void updateCDmemberJList (final Vector<URLDescriptor> memberList) {
  	// update the member list in the window
  	if (listCDMembers != null) {
  		try {
  			listCDMembers.setListData(memberList);
  		}
  		catch (Throwable ex) {
  			Trace.log("error", "TransientAgentInternalFrame.CDSelectionChanged: Bad JList.setListData()", ex);
  		}
  	}
  }

  /**
   * Mouse hook used by makeCDPanel in case we want to add context sensitive menus 
   * or other flashy garbage to the list of CD members.
   */
  protected void CDMembersMouseHook (final JList membertemp) {};

  /**
   * Updates window if a different CD is selected. 2 main updates: updates the
   * list of messages received by this agent using the new CD, and retrieves the
   * list of participants on the newly selected CD.
   */
  protected void CDSelectionChanged () {
  	runInEventDispatchThread(new Runnable() {
  		@Override
			public void run() {
  			URLDescriptor temp = null;
  				if ( (listCDs != null) && listCDs.getModel().getSize() > 0) {
  					temp = (URLDescriptor) listCDs.getSelectedValue();
  				}
  			if (temp==null)
    			updateCDmemberJList(new Vector<URLDescriptor>());
  			else {
  			  Vector<URLDescriptor> members = agent.getMembers(temp);
  			  updateCDmemberJList(members==null
  					? new Vector<URLDescriptor>()
  					: new Vector<URLDescriptor>(agent.getMembers(temp)));

  			}
  		}
  	},true);
  }


  protected void updateCDListFromAgent() {
    runInEventDispatchThread(new Runnable () {
      @Override
			public void run () {
        try {
      	
          Set<URLDescriptor> list = agent.getJoinedCooperationDomains();
          if (listCDs != null) {
          	int selected = listCDs.getSelectedIndex();
          	if (selected<0) selected = 0;
          	listCDs.setListData(list.toArray());
          	listCDs.setSelectedIndex(selected);
          }
 
        	if (menuCD_Withdraw != null)
        		menuCD_Withdraw.setEnabled(list!=null && list.size()>0);
          if (menuCD_Invite != null) {
            menuCD_Invite.setEnabled(list != null && !list.isEmpty());
          }
          CDSelectionChanged();
        }
        catch (Throwable ex) {
          Trace.log("error", "AbstractInternalFrame.updateCDListFromAgent()", ex);
        }
      }
    });

  }
  
  protected JList getListCDMembers() {
  	return new JList();
  }

  protected void updateMemberList() {
  	if (listCDMembers==null) return; //no point if there's no interface element
  	if (listCDs == null) return; // no point if there's no interface element

  	URLDescriptor cd = (URLDescriptor) listCDs.getSelectedValue();

  	// if a CD is selected
  	if (cd!=null) {
  		Vector<URLDescriptor> memberList = agent.getMembers(cd);
  		// update the member list in the window
  		listCDMembers.setListData(memberList==null?new Vector<URLDescriptor>():memberList);
  		listCDMembers.revalidate();
  		listCDMembers.repaint();
  	}
  }
  
  protected void withdrawCD () { 
  	runInEventDispatchThread(new Runnable() {@Override
		public void run() {
  		if (listCDs == null) return;
  		URLDescriptor cd = (URLDescriptor) listCDs.getSelectedValue();

  		final URLDescriptor cd2 = cd;

  		if (cd != null) {
  			URLDescriptor url=null;
				try {
					url = URLDescriptor.make(cd2);
				} catch (URLDescriptorException e) {
					agent.println("error", "AbstractInternalFrame.withdrawCD()", e);
				}
  			Status tempStatus = (url==null?null:agent.doWithdrawCD(url, true));
  			if (tempStatus==null || tempStatus.getStatusValue() != 0) {
  				JOptionPane.showMessageDialog(frame,
  						"There was an error sending the message:\n"
  						+ (tempStatus==null?("Failed to resolve CD url: "+cd2):tempStatus.getExplanation()), "Withdraw Failed",
  						JOptionPane.ERROR_MESSAGE);
  			}
  			else{
  				agent.removeCooperationDomains(cd2);
  				listCDs.clearSelection();
  				CDSelectionChanged ();           	
  			} 	
  		}
  		else {
  			JOptionPane.showMessageDialog(frame,
  					"Please select a CD from which to withdraw.", "No CD Selected",
  					JOptionPane.ERROR_MESSAGE);
  		}
  	}});

  }

  /**
   * Updates the commitment panels, to display the strategyGUI of the agent
   * if it has one. If it does not, the commitments tab will be removed.
   */
  protected void updateStrategyPanel () {
	  try {
			removeTab("Commitments");
		} catch (Exception e) {}
	  
	  if ( agent.hasStrategyGUI() ) {
		  addTab( "Commitments", 
		  		agent.getStrategyGUI(), true
		  );
	  }
  }


  
  protected void updateOptions() {
	  agent.realizeAgentBehaviourFromOptions();
  }
  
  /**
   * This method sets a new <code>JMenu</code> and <code>JMenuItem</code> based on the information
   * submitted by the agent that "observes" a GUI.   The information should be formated as follows in
   * the respective message <code>content</code>: menuTitle|menuItemTitle
   *
   * @param message MLMessage
   */
	public void createRequestedMenuItem (final MLMessage message) {
    String content = message.getParameter (ML.CONTENT);
    StringTokenizer tk = new StringTokenizer (content, "|");

    String menuTitle = tk.nextToken ();
    String menuItemTitle = tk.nextToken ();
    try {
      // matching event with agent
      this.guiObserver.put(menuItemTitle, URLDescriptor.make(message.getParameter(ML.SENDER)));
    } catch (URLDescriptorException ex) {}

    JMenu menu = getMenuBarMenu(menuTitle);
    if (menu == null) {
      menu = new JMenu (menuTitle);
      insertMenuBarAfter(menu, "Agent Commands");
    }
    else {
      replaceMenuBar(menu);
    }

    JMenuItem menuItem = new JMenuItem (menuItemTitle);
    menuItem.addActionListener (new ActionListener () {
      @Override
			public void actionPerformed (ActionEvent e) {
        eventAgentMatch(e, message);
      }});
    menu.add (menuItem);

  }


  private void eventAgentMatch(ActionEvent e, MLMessage message) {
    String command = e.getActionCommand();
    if (agent instanceof CASAProcess)
      ((CASAProcess)agent).informAgent_GUIOperationRequest (this.guiObserver.get(command), command);
  }

  protected TypeEditDialog makeTypeEditDialog () {
    String title = "Edit ontology for agent " + agent.getAgentName ();
    return new TypeEditDialog (getFrame (), title, false, agent);
  }

  /**
   * Adaptor method for jInternalFrame.addInternalFrameListener(InternalFrameListener)
   * and jFrame.addWindowListener (WindowListener).
   * @param listener
   */
  public void addFrameListener (EventListener listener) {
  	validate();
    if (jInternalFrame != null) {
      jInternalFrame.addInternalFrameListener ((InternalFrameListener) listener);
    } else {
      jFrame.addWindowListener ((WindowListener) listener);
    }
  }

  /**
   * Adaptor method: when isInternalFrameClosable() returns true, this method ensures that observers are notified
   * and that the window is disposed off.
   */
  protected void closeInternalFrame () {
  	validate();
  	System.gc(); //This is here to help InstanceCounter deliver a better report.
    if (isInternalFrameClosable ()) {
      process.deleteObserver (this);
      dispose ();
    }
  }
  
  /**
   * Adaptor method.
   */
  public void dispose () {
  	validate();
    if (jInternalFrame != null) {
      jInternalFrame.dispose ();
    } else {
      jFrame.dispose ();
    }
  }

  /**
   * Adaptor method.
   */
  public void doDefaultCloseAction () {
  	validate();
    if (jInternalFrame != null) {
      jInternalFrame.doDefaultCloseAction ();
    } else {
      /** @todo JFrame imp? */
    }
  }

  /**
   * Adaptor method.
   */
  public Container getContentPane () {
  	validate();
    if (jInternalFrame != null) {
      return jInternalFrame.getContentPane ();
    } else {
      return jFrame.getContentPane ();
    }
  }

  /**
   * Adaptor method.
   */
  public void setContentPane (Container pain) {
  	validate();
    if (jInternalFrame != null) {
      jInternalFrame.setContentPane (pain);
    } else {
      jFrame.setContentPane (pain);
    }
  }

  /**
   * Adaptor method.
   */
  public JInternalFrame.JDesktopIcon getDesktopIcon () {
  	validate();
    if (jInternalFrame != null) {
      return jInternalFrame.getDesktopIcon ();
    } else {
      return null;
    }
  }

  /**
   * Returns the outermost frame for this window. If this window is embedded in
   * a {@link LACWindow} it will return the LAC's frame ({@link casa.LAC.ProcessInfo#desktop LACinfo.desktop}).
   * Otherwise it returns this windows outer frame, {@link #jFrame jFrame}.
   * @return
   */
  protected Frame getFrame () {
  	validate();
  	if (jFrame != null)
  		return jFrame;
//  	Component comp = jInternalFrame;
//  	while (comp.getParent()!=null)
//  		comp = comp.getParent();
//  	if (comp instanceof Frame)
//  		return (Frame)comp;
//  	return null; //hopefully, we'll never get here...
  	return CASAProcess.ProcessInfo.desktop.getJFrame();
  }

  /**
   * Adaptor method that returns the current height of the window, taking
   * into account whether the window is iconified or not.
   * @return the height
   */
  public int getHeight () {
  	validate();
    if (jInternalFrame != null) {
      if (isIcon ())
        return getDesktopIcon ().getHeight ();
      else
        return jInternalFrame.getHeight ();
    } else {
      /** @todo Is there a special JFrame call to set an icon location? */
      return jFrame.getHeight ();
    }
  }

  /**
   * Adaptor method.
   * @return the method of the same name for either JFrame or JInternalFrame, as appropriate.
   */
  public JLayeredPane getLayeredPane () {
  	validate();
    if (jInternalFrame != null) {
      return jInternalFrame.getLayeredPane ();
    } else {
      return jFrame.getLayeredPane ();
    }
  }

  /**
   * Adaptor method.
   * @return the method of the same name for either JFrame or JInternalFrame, as appropriate.
   */
  public JRootPane getRootPane () {
  	validate();
    if (jInternalFrame != null) {
      return jInternalFrame.getRootPane ();
    } else {
      return jFrame.getRootPane ();
    }
  }

  /**
   * Adaptor method.
   * @return the method of the same name for either JFrame or JInternalFrame, as appropriate.
   */
  public int getWidth () {
  	validate();
    if (jInternalFrame != null) {
      if (isIcon ())
        return getDesktopIcon ().getWidth ();
      else
        return jInternalFrame.getWidth ();
    } else {
      /** @todo Is there a special JFrame call to set an icon width? */
      return jFrame.getWidth ();
    }
  }

  /**
   * Implementation of InternalFrameListener interface
   */

  /**
   * stub implementation of method from InternalFrameListener interface
   * as the abstract window is it's own internalframe event listener
   * <br>sub classes should override this as needed
   */
  @Override
	public void internalFrameActivated (InternalFrameEvent e) {
  }

  /**
   * stub implementation of method from InternalFrameListener interface
   * as the abstract window is it's own internalframe event listener
   * <br>sub classes should override this as needed
   */
  @Override
	public void internalFrameClosed (InternalFrameEvent e) {
  }

  /**
   * stub implementation of method from InternalFrameListener interface
   * as the abstract window is it's own internalframe event listener
   * <br>sub classes should override this as needed
   */
  @Override
	public void internalFrameClosing (InternalFrameEvent e) {
    closeInternalFrame ();
  }

  /**
   * stub implementation of method from InternalFrameListener interface
   * as the abstract window is it's own internalframe event listener
   * <br>sub classes should override this as needed
   */
  @Override
	public void internalFrameDeactivated (InternalFrameEvent e) {
  	validate();
  }

  /**
   * stub implementation of method from InternalFrameListener interface
   * as the abstract window is it's own internalframe event listener
   * <br>sub classes should override this as needed
   */
  @Override
	public void internalFrameDeiconified (InternalFrameEvent e) {
  	validate();
  }

  /**
   * stub implementation of method from InternalFrameListener interface
   * as the abstract window is it's own internalframe event listener
   * <br>sub classes should override this as needed
   */
  @Override
	public void internalFrameIconified (InternalFrameEvent e) {
  	validate();
    getLayeredPane ().setLayer (this.getDesktopIcon (),
                                JLayeredPane.PALETTE_LAYER.intValue (), 0);
  }

  /**
   * stub implementation of method from InternalFrameListener interface
   * as the abstract window is it's own internalframe event listener
   * <br>sub classes should override this as needed
   */
  @Override
	public void internalFrameOpened (InternalFrameEvent e) {
  }

  /**
   * Adaptor method.
   * @return the method of the same name for either JFrame or JInternalFrame, as appropriate.
   */
  public boolean isIcon () {
    if (jInternalFrame != null) {
      return jInternalFrame.isIcon ();
    } else {
      return ((jFrame.getExtendedState () & JFrame.ICONIFIED) > 0);
    }
  }

  /**
   * Adaptor method.
   * @return true if the frame is maximized
   */
  public boolean isMaximized() {
	  if (jInternalFrame != null) {
		  return jInternalFrame.isMaximum();
	  } else {
		  return (jFrame.getExtendedState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH;
	  }
  }
  
  /**
   * Adaptor method.
   * @return true if the frame is maximized
   */
  public boolean isClosed() {
	  if (jInternalFrame != null) {
		  return jInternalFrame.isClosed();
	  } else {
	  	return !jFrame.isDisplayable();
	  }
  }
  
  public void setVisible(boolean visibility){
  	validate();
	  if(jInternalFrame != null)
		  jInternalFrame.setVisible(visibility);
	  else
		  jFrame.setVisible(visibility);
  }
  
  /**
   * This method is called from closeInternalFrame() to check if InternalFrame is currently in
   * a closable state.  This implementation always returns true (the InternalFrame is
   * always in a closable state).  Sub classes should override this as needed
   * to perform context specific checking for closable state.
   *
   * @return TRUE --> see above explanation
   */
  protected boolean isInternalFrameClosable () {
    return true;
  }

  /**
   * Adaptor method.
   */
  public void pack () {
  	validate();
    if (jInternalFrame != null) {
      jInternalFrame.pack ();
    } else {
      jFrame.pack ();
    }
  }

  /**
   * Adaptor method.
   */
  public void setClosable (boolean b) {
  	validate();
    if (jInternalFrame != null) {
      jInternalFrame.setClosable (b);
    } else {
      /** @todo JFrame imp */
    }
  }

  /**
   * Adaptor method.
   */
  public void setFrameIcon (ImageIcon icon) {
  	validate();
    if (jInternalFrame != null) {
      jInternalFrame.setFrameIcon (icon);
    } else {
      jFrame.setIconImage (icon.getImage ());
    }
  }

  /**
   * Adaptor method.
   */
  public void setIcon (boolean b) {
  	validate();
    if (jInternalFrame != null) {
      try {
        jInternalFrame.setIcon (b);
      } catch (PropertyVetoException ex) {
      }
    } else {
      jFrame.setExtendedState (b ? JFrame.ICONIFIED : JFrame.NORMAL);
    }
  }

  /**
   * Adaptor method.
   */
  public void setMaximized (boolean b) {
  	validate();
    if (jInternalFrame != null) {
      try {
        jInternalFrame.setMaximum(b);
      } catch (PropertyVetoException ex) {
      }
    } else {
      jFrame.setExtendedState (b ? JFrame.MAXIMIZED_BOTH : JFrame.NORMAL);
    }
  }

  /**
   * Adaptor method.
   */
  public void setIconifiable (boolean b) {
  	validate();
    if (jInternalFrame != null) {
      jInternalFrame.setIconifiable (b);
    } else {
      /** @todo JFrame imp */
    }
  }

  /**
   * Adaptor method.
   */
  public void setJMenuBar (JMenuBar menuBar) {
  	validate();
    if (jInternalFrame != null) {
      jInternalFrame.setJMenuBar (menuBar);
    } else {
      jFrame.setJMenuBar (menuBar);
    }
  }

  /**
   * Adaptor method.
   */
  public void setLocation (Point p) {
  	validate();
    if (jInternalFrame != null) {
      if (isIcon ())
        getDesktopIcon ().setLocation (p);
      else
        jInternalFrame.setLocation (p);
    } else {
      /** @todo Is there a special JFrame call to set an icon location? */
      jFrame.setLocation (p);
    }
  }

  /**
   * Calls {@link #runInEventDispatchThread(Runnable, boolean) runInEventDispatchThread(r,false)}.
   * @param r The Runnable to run in the EDT
   */
  public static void runInEventDispatchThread(Runnable r) {
  	runInEventDispatchThread(r,false);
  }

  /**
   * Guarantees to run r.run() in the AWT dispatch Thread (EDT) by checking 
   * {@link java.awt.EventQueue#isDispatchThread()} and if it returns true, will
   * simply call r.run(), otherwise, it will call either 
   * {@link java.awt.EventQueue#invokeLater(Runnable)} if <em>wait</em> is false
   * or {@link java.awt.EventQueue#invokeAndWait(Runnable)} if <em>wait</em> is true
   * @param r The Runnable to run on the EDT
   * @param wait indicates whether to wait on the call or place on the EDT's queue
   */
  public static void runInEventDispatchThread(final Runnable r, final boolean wait) {
  	if (java.awt.EventQueue.isDispatchThread()) 
  		r.run();
  	else {
  		if (!wait) {
  			//The call to invokeLater() might be interrupted if not called from a clear new thread.
  			new Thread(new Runnable() {
  				@Override
  				public void run() {
  					java.awt.EventQueue.invokeLater(r);
  				}}).start();
  		}
  		else {
  			try {
  				java.awt.EventQueue.invokeAndWait(r);
  			} catch (InterruptedException e) {
  			} catch (InvocationTargetException e) {
  				Trace.log("error", "AbstractInternalFram.runInEventDispatchThread()", e);
  			}
  		}
  	}

  }
  
  /**
   * This method is called whenever the {@link #update(Observable, Object)}
   * gets an Object that's a {@link ObserverNotification}.  The {@link ObserverNotification}
   * is split into the components, and that forms the arguments to this method.
   * Note that if you need to you the raw info from {@link #update(Observable, Object)},
   * then you should put your code in {@link #updateHandler(Observable, Object)}
   * which is unconditionally called with the raw parameters.
   * You can expect this method to always be executed in the AWT Event thread.
   * @param observable The Observable that triggered this update call
   * @param event The event
   * @param argObject The parameter to the event
   */
  public void updateEventHandler(Observable observable, String event, Object argObject) {
  	validate();
    if (event.equals(ML.EVENT_BANNER_CHANGED)){
    	setTitle((String)argObject);
    }
  }
  
  //***************************************************************************
  // "Generic" methods for JFrame/JInternalFrame
  //***************************************************************************

   /**
    * Adaptor method.
    */
   public void setMaximizable(boolean b) {
   	validate();
     if (jInternalFrame != null) {
       jInternalFrame.setMaximizable(b);
     }
     else {
       /** @todo JFrame imp */
     }
   }

 /**
  * Adaptor method.
  */
  public void setName (String name) {
  	validate();
    frame.setName (name);
  }

  /**
   * Adaptor method.
   */
  public void setOpaque (boolean b) {
  	validate();
    if (jInternalFrame != null) {
      jInternalFrame.setOpaque (b);
    } else {
      /** @todo JFrame imp? */
    }
  }

  /**
   * Adaptor method.
   */
  public void setResizable (boolean b) {
  	validate();
    if (jInternalFrame != null) {
      jInternalFrame.setResizable (b);
    } else {
      /** @todo JFrame imp */
    }
  }

  /**
   * Adaptor method.
   */
  public void setSelected (boolean b) {
  	validate();
    if (jInternalFrame != null) {
      try {
        jInternalFrame.setSelected (b);
      } catch (PropertyVetoException ex) {
      }
    } else {
      /** @todo JFrame imp? */
    }
  }

  /**
   * Adaptor method.
   */
  public void setSize (int x, int y) {
  	validate();
    if (jInternalFrame != null) {
      if (isIcon ())
        getDesktopIcon ().setSize (x, y);
      else
        jInternalFrame.setSize (x, y);
    } else {
      /** @todo Is there a special JFrame call to set an icon location? */
      jFrame.setSize (x, y);
    }
  }

  /**
   * Adaptor method.
   */
  public void setTitle (String title) {
  	validate();
    if (jInternalFrame != null) {
      jInternalFrame.setTitle (title);
    } else {
      jFrame.setTitle (title);
    }
  }
  
	public String getTitle() {
  	validate();
		if (jInternalFrame != null) {
			return jInternalFrame.getTitle();
		} else {
			return jFrame.getTitle();
		}
	}

  /**
   * Adaptor method.
   */
  public void show () {
  	validate();
    if (jInternalFrame != null) {
      jInternalFrame.setVisible (true);
    } else {
      jFrame.setVisible (true);
    }
  }

  /**
   * Adaptor method.
   */
  public void toFront () {
  	validate();
    if (jInternalFrame != null) {
      jInternalFrame.toFront ();
      jInternalFrame.requestFocus();
    } else {
      jFrame.toFront ();
      jFrame.requestFocus();
    }
  }

  /**
   * Implementation of WindowListener interface
   */

  /**
   * stub implementation of method from WindowListener interface
   * as the abstract window is it's own internalframe event listener
   * <br>sub classes should override this as needed
   */
  @Override
	public void windowActivated (WindowEvent e) {
  	validate();
  }

  /**
   * stub implementation of method from WindowListener interface
   * as the abstract window is it's own internalframe event listener
   * <br>sub classes should override this as needed
   */
  @Override
	public void windowClosed (WindowEvent e) {
  	validate();
  }

  /**
   * stub implementation of method from WindowListener interface
   * as the abstract window is it's own internalframe event listener
   * <br>sub classes should override this as needed
   */
  @Override
	public void windowClosing (WindowEvent e) {
  	validate();
    closeInternalFrame ();
  }

  /**
   * stub implementation of method from WindowListener interface
   * as the abstract window is it's own internalframe event listener
   * <br>sub classes should override this as needed
   */
  @Override
	public void windowDeactivated (WindowEvent e) {
  	validate();
  }

  /**
   * stub implementation of method from WindowListener interface
   * as the abstract window is it's own internalframe event listener
   * <br>sub classes should override this as needed
   */
  @Override
	public void windowDeiconified (WindowEvent e) {
  	validate();
  }

  /**
   * stub implementation of method from WindowListener interface
   * as the abstract window is it's own internalframe event listener
   * <br>sub classes should override this as needed
   */
  @Override
	public void windowIconified (WindowEvent e) {
  	validate();
  }

  /**
   * stub implementation of method from WindowListener interface
   * as the abstract window is it's own internalframe event listener
   * <br>sub classes should override this as needed
   */
  @Override
	public void windowOpened (WindowEvent e) {
  	validate();
  }

	/**
	 * As an Observer, this methed will be called  when the an event happens with the
	 * agent.  Most AWT events need to be handled in the AWT Event threat, so we will
	 * queue the code into the AWT Event thread.  When we re-execute in the AWT Event thread,
	 * we will call {@link #updateHandler(Observable, Object)} to handle the event, and if
   * the Object argument is of type {@link ObserverNotification},
	 * we also call {@link #updateEventHandler(Observable, String, Object)}.
	 *
	 * @param observable
	 * @param argument
	 */
	@Override
	public final void update(final Observable observable, final Object argument) {
		if (!java.awt.EventQueue.isDispatchThread()) {
			java.awt.EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {update(observable, argument);}
		    });
			return;
		}
	  try { //updates shouldn't return an exception, so prevent this from
	        //happening no matter what
	    ObserverNotification state = null;
	    if (argument instanceof ObserverNotification) { 
	    	state = ((ObserverNotification) argument);
	    }

	    updateHandler(observable, argument);
	
	    final String event = (state != null) ? state.getType() : null;
    	if (event == null) return;  //No NullPointerExceptions
	    
	    final Object obj = state.getObject();
	    updateEventHandler(observable, event, obj);
	    
	  } catch (Throwable e) {
	  	if (agent!=null)
	  	  agent.println("error", "AbstractInternalFrame.update: unexpected exception.", e);
	  	else
	  		CASAUtil.log("error", "TransientAgentInternalFrame.update: unexpected exception.", e, true);
	  }
	
	}
	
  /**
   * This method is called from {@link #update(Observable, Object)} but
   * this method to always be executed in the AWT Event thread.  You should 
   * use this instead of {@link #update(Observable, Object)} (which is final).
   * @param observable The Observable that triggered this update call
   * @param obj The parameter object
   */
	public void updateHandler(Observable observable, Object obj) {}
	
	protected boolean validate() {
		if (SwingUtilities.isEventDispatchThread()) {
			return true;
		}
		String methodDescription = "unknown method";
		String traceString ="unknown trace";
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		if (trace!=null) {
			int traceLength = trace.length;
			if (traceLength>2) {
				methodDescription = trace[2].toString();
				if (traceLength>3) {
					StringBuilder b = new StringBuilder();
					for (int i=3; i<traceLength; i++) {
						b.append("    ").append(trace[i].toString()).append('\n');
					}
				  traceString = b.toString();
				}
			}
		}
		agent.println("warning", "Swing method called outside of dispatch thread: "+methodDescription+"\n  --trace:\n"+traceString);
		return false;
	}

	/**
	 * Returns a command panel that looks like this:
	 *  +--------------------------------------------+
	 *  | +----------------------------------------+ |
	 *  | |               msgArea                |^| |
	 *  | |                 in                   | | |
	 *  | |             msgScroller              |v| |
	 *  | +----------------------------------------+ |
	 *  |         +--------------------+ +---------+ | \
	 *  | Command:|  commandTextField  | | Execute | |  makeCommandPanel()
	 *  |         +--------------------+ +---------+ | /
	 *  +--------------------------------------------+
	 * This panel will be at index i of all vectors.
	 * @param i
	 * @return a command panel
	 */
	protected CommandPanel makeCommandPanel() {
		commandPanel = new CommandPanel(agent, frame);
		return commandPanel;
	}
	
	/**
	 * Displays information about the agent in infoPanel.
	 * Calls the other setInfoPanel.
	 * This method may be overridden.
	 *
	 * @param infoPanel
	 */
	protected JPanel setInfoPanel() {
    JPanel info = new JPanel();
	  Vector<String> agentVector = new Vector<String>();
	  setInfoPanel(info, agentVector);
	  return info;
	}
	/**
	 * Displays information about the agent in infoPanel.
	 * This information will be put into agentVector.
	 *
	 * @param infoPanel
	 * @param agentVector
	 * @return the number of lines added agentVector
	 */
	protected int setInfoPanel(JPanel infoPanel, Vector<String> agentVector) {
	   JScrollPane infoScroller;
	   JList agentInfoList;
	   int counter = 0;
	   agentVector.add(counter++, "User: " + agent.getURL().getUser());
	   agentVector.add(counter++, "Host: " + agent.getURL().getHostString());
	   agentVector.add(counter++, "Port: " + agent.getURL().getPort());
	   agentVector.add(counter++, "Path: " + agent.getURL().getPath());
	   agentVector.add(counter++,
	                   "Full Path: " + agent.getURL().getFullAddress(agent.getURL()));
	   agentVector.add(counter++, "_________________________");
	   for (String s: CASA.getBuildInfo().split("\n"))
	  	 agentVector.add(counter++, s);
	   agentInfoList = new JList(agentVector);
	   infoScroller = new JScrollPane(agentInfoList);
	
	   infoPanel.setLayout(new GridLayout(1, 1));
	   infoPanel.add(infoScroller, BorderLayout.CENTER);
	   infoPanel.setBorder(BorderFactory.createLineBorder(Color.blue));
	   return counter;
	 }
	/**
	 * @return the command-line panel for the window
	 */
	public CommandPanel getCommandPanel() {
		return commandPanel;
	}
	public static String getBuildTime() {
		 //use the cached value if available
		 if (buildTime!=null)
			 return buildTime;
	
		 String ret = null;
		 try {
			 URL jarURL = AbstractInternalFrame.class.getResource("/casa/ui/AbstractInternalFrame.class");
			 JarURLConnection jurlConn = (JarURLConnection)jarURL.openConnection();
			 Manifest mf = jurlConn.getManifest();
			 Attributes attr = mf.getMainAttributes();
			 ret = attr.getValue("casa-build-time");
		 } catch (Throwable e) {
			 try {
				URL jarURL = AbstractInternalFrame.class.getResource("/casa/ui/AbstractInternalFrame.class");
				 URLConnection urlConn = jarURL.openConnection();
				 ret = new Date(urlConn.getLastModified()).toString();
			} catch (IOException e1) {
				ret = null;
			}
		 }
	
		 if (ret==null || ret.length()==0)
			 ret = System.getProperty("casa.build.time");
	
		 if (ret==null || ret.length()==0)
			 ret = "unknown";
	
		 buildTime = ret; //cache the value
		 return ret;
	 }


}