/**
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. 
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  
 * The  Knowledge Science Group makes no representations about the suitability
 * of  this software for any purpose.  It is provided "as is" without express
 * or implied warranty.</p>
 * <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
package casa;

import casa.abcl.ParamsMap;
import casa.agentCom.URLDescriptor;
import casa.exceptions.IllegalOperationException;
import casa.ui.AgentUI;
import casa.ui.CustomIcons;
import casa.ui.ProcessWindow;
import casa.util.AgentLookUpTable;
import casa.util.Trace;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.armedbear.lisp.JavaObject;
import org.armedbear.lisp.SimpleString;
import org.ksg.casa.CASA;


/**
 * A singleton class representing a process where one or more agents execute.
 * This may be used to store common agent data, such as the Strategy and
 * policies shared by all the agents in the process.
 * 
 * On startup, the CASAProcess initializes the following variables:
 * <table border=1 rules="COLS">
 * <tr bgcolor="orange">
 * <th>TERM</th>
 * <th>INITIALIZATION</th>
 * <th>GETTER</th>
 * <th>SETTER</th>
 * </tr>
 * <tr valign="TOP">
 * <td>strategy</td>
 * <td>from env var "CASAStrategy"<br>
 * or default "sc3"</td>
 * <td>{@link #getStrategy()}</td>
 * <td>{@link #setStrategy(String)}</td>
 * </tr>
 * <tr valign="TOP">
 * <td></td>
 * <td></td>
 * <td></td>
 * <td></td>
 * </tr>
 * <tr valign="TOP">
 * <td></td>
 * <td></td>
 * <td></td>
 * <td></td>
 * </tr>
 * <tr valign="TOP">
 * <td></td>
 * <td></td>
 * <td></td>
 * <td></td>
 * </tr>
 * </table>
 * 
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * 
 */
public class CASAProcess extends Agent {
	
	static CASAProcess instance = null;

	private CASAProcess() throws Exception {
		super(getParamsMap(), null);
	}
	
	/**
	 * @param args
	 * @deprecated Use {@link casa.CASA#main(String[])} instead.
	 */
	public static void main(String[] args) {
		casa.CASA.main(args);
	}

	static private ParamsMap getParamsMap() {
		ParamsMap params = new ParamsMap();
		params.put("PORT", new Integer(-9010), new JavaObject(new Integer(-9010)),
				false);
		String name = "CASAProcess" + System.currentTimeMillis();
		params.put("NAME", name, new SimpleString(name), false);
		params.put("LACPORT", new Integer(-1), new JavaObject(new Integer(-1)),
				false);
		int traceSpec = 10; // AbstractProcess.TRACE_TO_FILE|(trace?AbstractProcess.TRACE_ON:AbstractProcess.TRACE_OFF);
		params.put("TRACE", new Integer(traceSpec), new JavaObject(new Integer(
				traceSpec)), false);
		params.put("TRACETAGS", "error,warning,info,msg", new SimpleString(
				"error,warning,info,msg"), false);
		// params.put("STRATEGY", "sc3", new SimpleString("sc3"), false);
		return params;
	}
	
	/* (non-Javadoc)
	 * @see casa.TransientAgent#initializeThread(casa.abcl.ParamsMap, casa.ui.AgentUI)
	 */
	@Override
	protected void initializeThread(ParamsMap params, AgentUI ui) {
		super.initializeThread(params, ui);
		ProcessInfo.process = this;
		ProcessInfo.processPort = getPort();
	}

	public CASAProcess(ParamsMap params, AgentUI ui) throws Exception {
		super(params, ui);
		if (instance != null)
			throw new IllegalOperationException(
					"Only one CASAProcess Agent is allowed in a process");
		instance = this;
	}

	static public CASAProcess getInstance() {
		if (instance == null)
			try {
				instance = new CASAProcess();
			} catch (Exception e) {
				Trace.log("error", "Unexpected failure creating a CASAProcess object.", e);
			}
		return instance;
	}
	
  /**
   * Creates  the directory <em>filePath</em> and all sub directories needed.  If <em>filePath</em>
   * ends in "/", then the terminal directory will be created (it's interpreted as a 
   * directory path).  However, if it doesn't end in "/" then we assume the last entry
   * is a filename, and it's not created.
   *
   * @throws IOException If one of the directories could not be created.
   */
  private static void createDirectories (String filePath) throws IOException {
    try {
      int nextSpot = filePath.indexOf ('/');
      if (nextSpot == 0) {
        nextSpot = filePath.indexOf ('/', nextSpot + 1); //if this is a top-level directory, start in the right place

      } while (nextSpot != -1) {
        File tempFile = new File (filePath.substring (0, nextSpot));
        if (!tempFile.isDirectory ()) {
          if (!tempFile.mkdir ()) {
            throw new IOException ("Could not create directory: " +
                                   tempFile.getAbsolutePath ());
          }
        }
        nextSpot = filePath.indexOf ('/', nextSpot + 1);
      }

      //check to see if we should create the default types as directories
      int rootIndex = filePath.lastIndexOf ('/');
      String root = filePath.substring (0, rootIndex);
      File tempFile = new File (root + "/casa");
      if (!tempFile.isDirectory ()) {
        tempFile.mkdir ();
        tempFile = new File (root + "/casa/CooperationDomain");
        tempFile.mkdir ();
        tempFile = new File (root + "/casa/ChatAgent");
        tempFile.mkdir ();
      }
    } catch (IOException ex) {
      throw ex;
    }
  }

  /**
   * Finds an appropriate root directory (given that we aren't otherwise told)
   * by looking for the preference "rootDirectory" or using ~/.casa/.  This method
   * also CREATES that directory if it's not already existing, including the entire path.
   * @return The directory, which always ends in a "/".
   * @see org.ksg.casa.CASA#getPreference(String, String, int)
   * @see #createDirectories(String)
   */
  static public String getRootDirectoryDefault() {
  	String casaRootDirectory;
  	casaRootDirectory = CASA.getPreference("rootDirectory", (String)null, 0);
  	if (casaRootDirectory!=null) { // we have a directory, attempt to create it
  		if (casaRootDirectory.charAt (casaRootDirectory.length () - 1) != '/') {
  			casaRootDirectory += "/";
  		}
  		try {
  			createDirectories(casaRootDirectory);
  		} catch (IOException e) {
  			Trace.log("error", "Could not create directory "+casaRootDirectory+":", e);
  			casaRootDirectory = null;
  		}
  	}
  	if (casaRootDirectory == null || casaRootDirectory.length () == 0) {
  		casaRootDirectory = System.getProperty("user.home"); 
  		if (casaRootDirectory.charAt (casaRootDirectory.length () - 1) != '/')
  			casaRootDirectory += "/";
  		casaRootDirectory += ".casa/persistent/";
  	} 
  	if (casaRootDirectory.charAt (casaRootDirectory.length () - 1) != '/') {
  		casaRootDirectory += "/";
  	}
  	return casaRootDirectory;
  }
  
  /**
   * Returns a complete file spec string corresponding to the URL.  An agent
   * uses this file to store it's persistent data (in a CASAFile).
   * @param url to compute the file spec from
   * @return a complete file spec string corresponding to the URL
   */
  static public String staticUrl2file (URLDescriptor url) {
    if (url == null) {
      return null;
    }
    String path = url.getPath ();
    String ret = staticPath2file (path);
    return ret;
  }

  /**
   * Returns a complete file spec string corresponding to an agent path name (that is,
   * the agent list and agent name (eg. "casa/ChatAgent/Rob")).  An agent
   * uses this file to store it's persistent data (in a CASAFile).
   * @param path path to compute the file spec from
   * @return a complete file spec string corresponding to the URL
   */
  static public String staticPath2file (String path) {
    String file = getRootDirectoryDefault() + path;
    int dot = file.lastIndexOf (".casa");
    if (dot != file.length () - 5) {
      file += ".casa";
    }
    return file;
  }



	String strategy = null;

	@Override
	public String getStrategy() {
		if (strategy == null) {
			strategy = System.getenv("CASAStrategy");
			if (strategy == null)
				strategy = "sc3";
		}
		return strategy;
	}

	public void setStrategy(String strategy) {
		this.strategy = strategy;
	}

	protected static ProcessWindow lacWindow = null;

	@Override
	public AgentUI makeDefaultGUI(String[] args) {
		return new ProcessWindow(this, args);
	}

	@Override
	public void putUI(AgentUI ui) {
		super.putUI(ui);
		ProcessInfo.lacUI = ui;
		if (ProcessWindow.class.isAssignableFrom(ui.getClass())) {
			lacWindow = (ProcessWindow) ui;
			ProcessInfo.desktop = lacWindow;
			lacWindow.setVisible(true);
		} else {
			lacWindow = null;
			ProcessInfo.desktop = null;
		}
	}

	/**
	 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
	 */
	public static class ProcessInfo {
		
		/**
		 * Marks the process as a daemon, meaning that no agent in this process should
		 * perform any gui calls, or should not have an interface.
		 */
		public static boolean daemon = false;
		/**
		 */
		public static ProcessWindow desktop = null;

		/**
		 */
		public static AgentUI lacUI = null;

		/** @todo have this port info. set dynamic */
		public static int lacPort = 0;

		public static int processPort = 0;

		/**
		 */
		public static CASAProcess process = null;

		public static LAC lac = null;
		
	  public static Set<URLDescriptor> synonymsURLs = new TreeSet<URLDescriptor>();
	}

	public ProcessOptions getProcessOptions() {
		return super.getOptions();
	}

	@Override
	public void exit() {
		class Exiter implements Runnable {
			CASAProcess agent;

			public Exiter(CASAProcess agent) {
				this.agent = agent;
			}

			@Override
			public void run() {
				exit2(agent);
			}
	  };
	  
	  if (CASAProcess.ProcessInfo.daemon) {
	  	exit2(this);
	  }
	  else {
	    javax.swing.SwingUtilities.invokeLater(new Exiter(this));
	  }
	}


	protected void exit2(CASAProcess agent) {
		if (agent==null) {
			super.exit();
			return;
		}
		URLDescriptor myURL = getURL();
		for (URLDescriptor url : AgentLookUpTable.keySet()) {
			if (!url.equals(myURL))
				sendMessage(ML.INFORM, ML.EXIT, url);
		}
		int proc = 1;
		for (int i = 20; i > 0 && proc > 0; i--) { // wait around for up to 10
																							 // seconds
			synchronized (this) {
				try {
					this.wait(500);
				} catch (InterruptedException e) {
					println("warning",
							"CASAProcess interruped waiting for other agents to exit");
				}
			}
			proc = 0;
			for (URLDescriptor url : AgentLookUpTable.keySet()) {
				if (!url.equals(myURL)) {
					proc++;
				}
			}
		}
		int closeOption = JOptionPane.OK_OPTION;
		if (!CASAProcess.ProcessInfo.daemon && proc > 0) {
			StringBuilder sb = new StringBuilder();
			for (URLDescriptor url : AgentLookUpTable.keySet()) {
				if (!url.equals(myURL)) {
					sb.append("  ").append(url.getFile()).append("\n");
				}
			}
			StringBuilder sb3 = new StringBuilder();
			if (sb.length() > 0)
				sb3.append("Some agents running in this process have not exited:\n")
						.append(sb);
			sb3.append("Do you want to force termination?");
			ImageIcon i = CustomIcons.REAL_INNER_ICON;
			String title = "CASAProcess Closing -- Terminating this process...";
			closeOption = JOptionPane.showConfirmDialog(null, sb3.toString(),
					title, JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, i);

		}
		if (closeOption == JOptionPane.OK_OPTION)
		  super.exit();
	}
	

	/** 
	 * Adds "CASA: " in front of {@link TransientAgent#getDefaultBanner()}'s banner.
	 * @see casa.TransientAgent#getDefaultBanner()
	 */
	@Override
	public String getDefaultBanner() {
		return "CASA: "+super.getDefaultBanner();
	}



	/**
	 * 
	 * @param receiverURL
	 * @param content
	 * @return
	 */
	public Status informAgent_GUIOperationRequest(URLDescriptor receiverURL, String content) {
	  MLMessage message = getNewMessage (ML.INFORM,
	                                     ML.GUI_ACTION_REQUEST,
	                                     receiverURL);
	
	  message.setParameter (ML.LANGUAGE, String.class.getName ());
	  message.setParameter (ML.CONTENT, content);
	
	  return sendMessage (message);
	}


  /**
   * @return true iff the agent is processing messages not addressed to it.
   */
  @Override
	public boolean isObserveMessages(){
    return true;
  }
  
	
}
