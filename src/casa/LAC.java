package casa;

import casa.abcl.ParamsMap;
import casa.agentCom.URLDescriptor;
import casa.exceptions.URLDescriptorException;
import casa.interfaces.LACInterface;
import casa.io.CASAFile;
import casa.io.CASAFileLACKnownUsersMap;
import casa.io.CASAFilePropertiesMap;
import casa.ui.AgentUI;
import casa.ui.CustomIcons;
import casa.ui.LACWindow;
import casa.util.AgentLookUpTable;
import casa.util.CASAUtil;
import casa.util.PropertyException;
import casa.util.Trace;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * Local Area Coordinator (LAC).  A CASA service agent that serves as a hub for other 
 * agents on the same machine.  Normally there is only one LAC per machine, but there 
 * can be more than one (e.g.: one for "production" and another for "experimental" 
 * purposes).  The default port for a LAC is 9000. <br>Agents will normally register 
 * with a LAC.  Note that there are <em>two</em> kinds of registration: 
 * <ul> <li><b>Type Registration</b> where an agent registers its type 
 * (which is specified as a path), and a description of how to run the agent should 
 * it be requested, and the agent is not running (specified as a RunDescriptor). 
 * Type Registrations persist between invocations of the LAC, and must be specifically 
 * deleted (via <em>unregisterType()</em>). 
 * <li><b>Instance Registration</b> where an agent registers that it is running as 
 * an individual of its type at a particular port.  Normally, agents will do an 
 * instance registration when they first start, and will do a 
 * <em>unregisterInstance()</em> when they exit.  Instance registrations do not 
 * persist beyond the lifetime of the LAC process (i.e.: if the LAC process is 
 * ended, the instance registrations are forgotten). 
 * </ul> 
 * One of the other services the LAC offers is to resolve URLs for the agents it 
 * controls.  URLs may be resolved for individuals, whether persistent or transient, 
 * and for individuals whose type has been registered (in which case the LAC will 
 * start a new instance).  Obviously, this can only be done if the individual agent 
 * has registered, or it's type has been registered. <br> In addition to the messages 
 * defined in  {@link TransientAgent} , this agent can originate the following 
 * messages types: 
 * <table border="1" bgcolor="gold" cellpadding="3"> 
 * <tr><th> <a href="doc-files/performatives.gif">performative</a> </th> <th>  {@link TransientAgent#makeDefaultActs()  act}  </th> <th> <em> see </em> </th> <th> <em> reply handler </em></th> </tr> 
 * <tr><td> inform </td> <td> LAC.closing </td> <td>  {@link #informTerminateProcess()}  </td> <td> - </td> </tr> 
 * </table> 
 * <br> In addition to the messages defined in  {@link TransientAgent} , this agent responds 
 * to the following messages types: 
 * <table border="1" bgcolor="orange" cellpadding="3"> 
 * <tr><th> <a href="doc-files/performatives.gif">performative</a> </th> <th>  {@link TransientAgent#makeDefaultActs()  act}  </th> <th> <em> see </em> </th> </tr> 
 * <tr><td> request </td> <td> register.instance </td> <td>  {@link #respondToRegisterAgentInstance(MLMessage)}  </td> </tr> 
 * <tr><td> request </td> <td> register.agentType </td> <td>  {@link #respondToRegisterAgentType(MLMessage)}  </td> </tr> 
 * <tr><td> request </td> <td> unregister.instance </td> <td>  {@link #respondToUnregisterAgentInstance(MLMessage)}  </td> </tr> 
 * <tr><td> request </td> <td> unregister.agentType </td> <td>  {@link #respondToUnregisterAgentType(MLMessage)}  </td> </tr> 
 * <tr><td> request </td> <td> resolve.url </td> <td>  {@link #respondToResolveURL(MLMessage)}  </td> </tr> 
 * <tr><td> request </td> <td> find.instance </td> <td>  {@link #respondToFindInstances(MLMessage)}  </td> </tr> 
 * <tr><td> request </td> <td> run.agent </td> <td>  {@link #respondToRunAgent(MLMessage)}  </td> </tr> 
 * <tr><td> inform </td> <td> register.instance </td> <td>  {@link #handleUnregisterAgentInstance(MLMessage)}  </td> </tr> 
 * </table> 
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * 
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @author  <a href="mailto:sedachv@cpsc.ucalgary.ca">Vladimir Sedach</a>
 * @author  <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 * @version 0.9
 */

public class LAC extends CASAProcess implements LACInterface {

	/**
   * Don't confuse "runningAgents" with "registeredAgents".  The former contains agents
   * registered with the LAC.  The latter appears to contain "types" of agents known to the
   * LAC, though I'm not entirely certain of this (dsbidulo 2010-6-1)
   * 
   * elements are: <pre>forall x:URLDescriptor.(x.getPath(), x)</pre>
   */
  protected ConcurrentSkipListMap<String,URLDescriptor> runningAgents = new ConcurrentSkipListMap<String,URLDescriptor> ();
  /**
   * Don't confuse "runningAgents" with "registeredAgents".  The former contains agents
   * registered with the LAC.  The latter appears to contain "types" of agents known to the
   * LAC, though I'm not entirely certain of this (dsbidulo 2010-6-1)
   * 
   * elements are: <pre>forall x:URLDescriptor. (String path, RunDescriptor)</pre>
   */
  protected Hashtable<String,RunDescriptor> registeredAgents = new Hashtable<String,RunDescriptor> ();
  /**
   * The directory used as the root of all information about casa agents: where
   * all the .casa files are stored.  This may be different for each LAC on a
   * system (although, typically, there is only one LAC per system).  It is set
   * by (in priority order):
   * <ol>
   * <le>-r Command line argument to the LAC
   * <le>or defaults to "/casa/"
   * </ol>
   */
  protected String rootDirectory = null;
  /**
	 */
  CASAFile casaFile = null;
  /**
	 * The CASAFilePropertiesMap associated with this LAC for persistence (LAC is not a (persistent) Agent, so it needs to implement it's own persistence)
	 */
  CASAFilePropertiesMap properties = null;
  
  //
  // Security related objects
  //
  /**
	 */
  CASAFileLACKnownUsersMap knownUserMap = null;

  Hashtable<String,String> validUsers = new Hashtable<String,String> (); // holds a list of known users by the LAC

  private static int cdIndex = 0;
  private static int otherIndex = 0;

  public Vector<URLDescriptor> removedVector = new Vector<URLDescriptor> ();

  /**
	 */
//  private static LACWindow lacWindow = null;
  /**
	 */

  public LAC (ParamsMap params, AgentUI ui) throws Exception {
    super (params, ui);
    in ("LAC.LAC");
    
//    addConversationSupported(ML.GET_AGENTS_REGISTERED, GetAgentsRegisteredServerRequestComposite.class);
//    addConversationSupported(ML.GET_AGENTS_RUNNING, GetAgentsRunningServerRequestComposite.class);
//    addConversationSupported(ML.REGISTER_INSTANCE, RegisterInstanceServerRequestComposite.class);
    
    out ("LAC.LAC");
  }
  
  @Override
  protected ProcessOptions makeOptions () {
    in ("LAC.makeOptions");
    ProcessOptions ret = new LACOptions (this);
    out ("LAC.makeOptions");
    return ret;
  }

  /**
   * Returns the root directory for this LAC.
   * @return the root directory for this LAC
   */
  public String getRoot () {
  	if (rootDirectory==null) {
  		rootDirectory = getRootDirectoryDefault();
  	}
    return rootDirectory;
  }

  /**
   * Returns a complete file spec string corresponding to the URL.  An agent
   * uses this file to store it's persistent data (in a CASAFile).
   * @param url to compute the file spec from
   * @return a complete file spec string corresponding to the URL
   */
  public String url2file (URLDescriptor url) {
    in ("LAC.url2file");
    if (url == null) {
      return null;
    }
    String path = url.getPath ();
    String ret = path2file (path);
    out ("LAC.url2file");
    return ret;
  }

  /**
   * Returns a complete file spec string corresponding to an agent path name (that is,
   * the agent list and agent name (eg. "casa/ChatAgent/Rob")).  An agent
   * uses this file to store it's persistent data (in a CASAFile).
   * @param path path to compute the file spec from
   * @return a complete file spec string corresponding to the URL
   */
  public String path2file (String path) {
    in ("LAC.path2file");
    String file = getRoot () + path;
    int dot = file.lastIndexOf (".casa");
    if (dot != file.length () - 5) {
      file += ".casa";
    }
    out ("LAC.path2file");
    return file;
  }

  /**
   * Returns a complete a path name (that is,
       * the agent list and agent name (eg. "casa/ChatAgent/Rob")) from a file name.
   * This is with the root removed and the ".casa" extension removed.  An agent
   * uses this file to store it's persistent data (in a CASAFile).  An agent
   * is identified by it's path name.
   * @param file to compute the file spec from
   * @return url a complete file spec string corresponding to the URL
   */
  public String file2path (String file) {
    in ("LAC.file2path");
    String path = null;
    try {
      path = file.substring (getRoot ().length () + 1,
                             file.lastIndexOf (".casa"));
    } catch (Exception ex) {}
    out ("LAC.file2path");
    return path;
  }

  /**
   * Returns the filename of the <code>CASAFile</code> that will be used by
   * this LAC to store its properties and data either temporarily or
   * persistently.
   *
   * @return The filename of the <code>CASAFile</code> that will be used by
   * this agent to store its properties and data.
   */
  @Override
	public String getCASAFilename () {
    return getRoot() + getURL ().getFile () + ".casa";
  }

  /**
   * Creates all sub directories needed to write the <code>CASAFile</code> for
   * this agent.
   *
   * @throws IOException If one of the directories could not be created.
   */
  private void createDirectories () throws IOException {
    in ("LAC.createDirectories");
    try {
      String filePath = getCASAFilename ();
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
      String root = rootIndex>0?filePath.substring (0, rootIndex):filePath;
      File tempFile = new File (root + "/casa");
      if (!tempFile.isDirectory ()) {
        tempFile.mkdir ();
        tempFile = new File (root + "/casa/CooperationDomain");
        tempFile.mkdir ();
        tempFile = new File (root + "/casa/ChatAgent");
        tempFile.mkdir ();
      }
    } catch (IOException ex) {
      out ("LAC.file2path");
      throw ex;
    }
    out ("LAC.file2path");
  }

  /**
   * This initializes both the <code>CASAFile</code> that will be used to store
   * the data, and the properties object.
   */
  private synchronized void initializeFile () {
    in ("LAC.initializeFile");
    try {
      createDirectories ();
    } catch (IOException e) {
    	Trace.log("error", "CASA LAC: The CASA directories were not created, all write operations are now guaranteed to fail.");
    }
    casaFile = new CASAFile (getCASAFilename ());
    properties = new CASAFilePropertiesMap (casaFile);
    knownUserMap = new CASAFileLACKnownUsersMap (casaFile);
  }
  
  /**
   * This method is safe as it called from the thread of the agent (not the constructor)
   */
  @Override
  protected void initializeThread (ParamsMap params, AgentUI ui) {
    in ("LAC.initailizeRun");
    super.initializeThread (params, ui);

    //Load critical agent-specific policies
//		try {
//			readPolicyFile("register_instance" + File.separator + "RegisterInstanceServerPolicies.lisp");
//		} catch (Exception e) {
//			println("error", getAgentName() + ": Cannot read new policy file: " + e.toString());
//		}
    
    int port;
		try {
			port = (Integer)params.getJavaObject("PORT",Integer.class);
		} catch (Exception e) {
			port = 9000;
		}
    if (port <= 0) {
      port = 9000;
      if (isLoggingTag("warning"))
        println("warning","LAC.initializeRun: found port value of " +
                Integer.toString (port) + ".  Resetting port to 9000.");
    }

    String agentName = null;
		try {
			agentName = (String)params.getJavaObject("NAME",String.class);
		} catch (Exception e) {
	  }
    if (agentName == null || agentName.length () == 0) {
      agentName = "CASA-LAC";
      if (isLoggingTag("warning"))
        println("warning","LAC.initializeRun: found agent name null or blank.  Resetting name to 'CASA-LAC'.");
    }

    //handle getting the root of the CASA directory structure
    String casaRootDirectory = null;
		try {
			casaRootDirectory = (String)params.getJavaObject("ROOT",String.class);
		} catch (Exception e) {
		}
    if (casaRootDirectory == null || casaRootDirectory.length () == 0) {
    	casaRootDirectory = getRootDirectoryDefault();
//      casaRootDirectory = System.getProperty("user.home");
//      if (casaRootDirectory.charAt (casaRootDirectory.length () - 1) != '/')
//        casaRootDirectory += "/";
//      casaRootDirectory += ".casa/";
    }
    if (casaRootDirectory.charAt (casaRootDirectory.length () - 1) != '/') {
      casaRootDirectory += "/";
    }

    initializeFile ();

    properties.setInteger ("port", getURL ().getPort ());

    ProcessInfo.lacPort = getPort ();
    ProcessInfo.process = this;

    //add default agent types to registered agents
    String[] cl = {"casa.CooperationDomain", "casa.YellowPagesAgent",
                  "casa.ChatAgent"};
    for (int i = 0, end = cl.length; i < end; i++) {
      RunDescriptor rd = new RunDescriptor ();
      rd.setJavaClass ("-A" + cl[i] + " -n%name% -p%port% -L%lacPort%");
      rd.setAuthorized (true);
      registeredAgents.put (cl[i], rd);
    }

//    //recover the persistent settings
//    //save the setting that might have been set specifically by the command line
//    boolean tracing = options.tracing;
//    boolean traceMonitor = options.traceMonitor;
//    boolean traceToFile = options.traceToFile;
//    String traceTags = options.traceTags;
//    String strategy  = options.strategy;

    //read the options that persisted from last run
//    options.read (properties);
    CASAUtil.readPersistentFromProperties(null,this, properties);

    //restore the command line setting (override persistent settings) as necessary
//    if ((options.traceMask & (TRACE_OFF | TRACE_ON)) != 0) {
//      options.tracing = tracing;
//      //-setTracing(options.tracing);
//    }
//    if ((options.traceMask & TRACE_MONITOR) != 0) {
//      options.traceMonitor = traceMonitor;
//    }
//    if ((options.traceMask & TRACE_TO_FILE) != 0) {
//      options.traceToFile = traceToFile;
//    }
//    if (traceTags!=null) options.traceTags=traceTags;
//    if (strategy !=null) options.strategy =strategy;
    resetRuntimeOptionsFromCommandLine();
    
    //TOD do we need this?
    realizeAgentBehaviourFromOptions ();


    //recover registered agents
    Hashtable<?,?> ra = null;
    String ras=null;
    try {
    	ras=properties.getString ("registeredAgents");
    	if (ras!=null) {
    		ra = (Hashtable<?,?>) CASAUtil.unserialize (ras, URLDescriptor.class.getCanonicalName());
    		if (ra != null) {
    			Set<?> keys = ra.keySet ();
    			for (Iterator<?> i = keys.iterator (); i.hasNext (); ) {
    				String key = (String) i.next ();
    				RunDescriptor val = (RunDescriptor) ra.get (key);
    				registeredAgents.put (key, val);
    			}
    		}
    	}
    } catch (PropertyException ex) {} catch (ParseException ex) { //if the property is non-existent, just leave registeredAgents empty
      if (isLoggingTag("warning"))
        println("warning","LAC.initializeRun: " + ex.toString () + " at position " +
                Integer.toString (ex.getErrorOffset ())+":\n"+ras);
    } catch (Exception ex) {
      if (isLoggingTag("warning"))
        println("warning",
                "LAC contructor: unexpected exception reading registeredAgents", ex);
    }

    //set the look and feel
    try {
      String lf = properties.getString ("options.lookAndFeel");
      if (lf != null) {
        UIManager.setLookAndFeel (lf);
      }
    } catch (Exception ex) {}
   
    out ("LAC.initailizeRun");
  }

  @Override
  protected void pendingFinishRun () {
    in ("LAC.pendingFinishRun");
    if (properties!=null)
    	options.write (properties);
    super.pendingFinishRun ();
    out ("LAC.pendingFinishRun");
  }

// If a LAC joined a CD, it should certainly withdraw from it.  --rck
//  /* (non-Javadoc)
//   * @see casa.TransientAgent#pendingFinishRun_withdrawFromAllCDs()
//   */
//  @Override
//  protected void pendingFinishRun_withdrawFromAllCDs () {
//    in ("LAC.pendingFinishRun_withdrawFromAllCDs");
//    // LAC shouldn't do withdraws...
//    out ("LAC.pendingFinishRun_withdrawFromAllCDs");
//  }

  /* (non-Javadoc)
   * @see casa.TransientAgent#pendingFinishRun_unregisterAgentInstance()
   */
  @Override
  protected void pendingFinishRun_unregisterAgentInstance () {
    in ("LAC.pendingFinishRun_unregisterAgentInstance");
    // LAC shouldn't unregister, since it shouldn't have registered with another
    // LAC.
    out ("LAC.pendingFinishRun_unregisterAgentInstance");
  }
  
//  @Override
//  public void exit() {
//  	class Exiter implements Runnable {
//  		LAC agent;
//  		public Exiter(LAC agent) {this.agent = agent;}
//  		@Override public void run() {
//  			exit2(agent);
//  		}
//  	};
//  	
//  	javax.swing.SwingUtilities.invokeLater(new Exiter(this));
//  }

  @Override
  protected void exit2(CASAProcess agent) {
  	if (agent==null) {
  		super.exit2(null);
  		return;
  	}
		URLDescriptor myURL = getURL();
		for (URLDescriptor url: runningAgents.values()) {
			if (!url.equals(myURL))
				sendMessage(ML.INFORM, ML.EXIT, url);
		}
		int proc = 1, anotherProc = 1;
		for (int i=20; i>0 && (proc>0 || anotherProc>0); i--) { //wait around for up to 10 seconds
			synchronized (this) {
				try {
					this.wait(500);
				} catch (InterruptedException e) {
					println("warning","LAC interruped waiting for other agents to exit");
				}
			}
			proc = 0;
			anotherProc = 0;
			for (URLDescriptor url: runningAgents.values()) {
				if (!url.equals(myURL)) {
					if (AgentLookUpTable.containsKey(url))
						proc++;
					else
						anotherProc++;
				}
			}
		}
		int closeOption = JOptionPane.OK_OPTION;
		if ((!CASAProcess.ProcessInfo.daemon) && (proc>0 || anotherProc>0)) {
			StringBuilder sb = new StringBuilder();
			StringBuilder sb2 = new StringBuilder();
			for (URLDescriptor url: runningAgents.values()) {
				if (!url.equals(myURL)) {
					if (AgentLookUpTable.containsKey(url))
						sb.append("  ").append(url.getFile()).append("\n");
					else
						sb2.append("  ").append(url.getFile()).append("\n");
				}
			}
			StringBuilder sb3 = new StringBuilder();
			if (sb.length()>0)
				sb3.append("Some agents running in this process have not exited:\n").append(sb);
			if (sb2.length()>0)
				sb3.append("Some agents running in other processes have not unregistered:\n").append(sb2);
			sb3.append("Do you want to force termination?");
			ImageIcon i = CustomIcons.REAL_INNER_ICON;
			String title = "LAC Closing -- Terminating this process...";
			closeOption = JOptionPane.showConfirmDialog(null, sb3.toString(), 
					title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, i);

		}
		if (closeOption==JOptionPane.OK_OPTION)
  	  super.exit2(null);
  }

  

  //////////////////////////////////////////////////////////////////////////////
  // REGISTER INSTANCE /////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

	private void initRunningAgents() {
    if (runningAgents==null) runningAgents = new ConcurrentSkipListMap<String,URLDescriptor>();
  }

  /**
   * Registers an agent instance according to
   * {@link LACInterface#registerAgentInstance(URLDescriptor)} .
   * @param newURL
   * @return A StatusURLandFile object
   */
  @Override
	public StatusURLandFile registerAgentInstance (URLDescriptor newURL) {
    in ("LAC.registerAgentInstance");
    newURL.setLACport (getURL ().getPort ());
    newURL.setIndirect (!newURL.equals (getURL()) && ((LACOptions) options).indirect);
    if (((LACOptions) options).alias!=null && ((LACOptions) options).alias.length()!=0) {
      newURL.setDataValue("alias", ( (LACOptions) options).alias);
    }
    if (runningAgents==null) initRunningAgents();//this method might be called before construction is complete.
    if (runningAgents.containsKey (newURL.getPath ())) {
      out ("LAC.registerAgentInstance");
      return new StatusURLandFile (1,
                                   runningAgents.get (newURL.getPath ()),
                                   url2file (newURL),
                                   "Agent is already registered");
    } else {
      runningAgents.put (newURL.getPath (), newURL);

      String foundStr = newURL.getPath ();

      Matcher matcher = Pattern.compile ("/security/").matcher (foundStr);
      if (!matcher.find ()) {
        addToListofKnownUsers (newURL);
      }

      out ("LAC.registerAgentInstance");
      return new StatusURLandFile (0, newURL, url2file (newURL), "Success");
    }
  }

  
  Hashtable<?,?> tempUserTable = new Hashtable<Object, Object>();
  int userCounter = 0;
  /**
   * TODO set the userCounter variable in order to have knowledge of how many times does this user
   * sign in to the system
   * @param newURL the users' URLDescriptor
   */
  private void addToListofKnownUsers (URLDescriptor newURL) {
    if (knownUserMap==null) return; //this method may be called part way through the constructor
    String usersFromFile = new String ();
    try {
      usersFromFile = knownUserMap.getString ("LAC_KnownUsers");
    } catch (PropertyException ex) {}

    // writes to the node when the node is empty...
    if (usersFromFile.length () == 0) {
      validUsers.put (newURL.getPath (),
                      newURL + " _userCounter " + userCounter);
      knownUserMap.setString ("LAC_KnownUsers", CASAUtil.serialize (validUsers));
    } else { // when the node is not empty, try to match the user to the users in the list
      try {
        tempUserTable = CASAUtil.unserializeHashtable (usersFromFile, null);
      } catch (ParseException ex1) {}

      String tempPath = newURL.getPath ();
      String indexInUrl = newURL.toString (getURL ());

      if (tempUserTable.containsKey (tempPath)) {
        Matcher indexMatcher = Pattern.compile ("\\s+_userCounter\\s+(\\d+)").
                               matcher (indexInUrl);
        if (indexMatcher.find ()) {
//          System.err.println ("userCounter for " + tempPath + " is: " +
//                              userCounter);
          userCounter = Integer.parseInt (indexMatcher.group (1));
          userCounter++;
        }
        validUsers.put (newURL.getPath (),
                        newURL + " _userCounter " + userCounter);
        knownUserMap.setString ("LAC_KnownUsers",
                                CASAUtil.serialize (validUsers));
      } else { // if there is no match in the users' list, then add the new user
        validUsers.put (newURL.getPath (),
                        newURL + " _userCounter " + userCounter);
        knownUserMap.setString ("LAC_KnownUsers",
                                CASAUtil.serialize (validUsers));
      }
    }
  }

//  protected PerformDescriptor consider_register_instance(MLMessage msg) {
//    in("LAC.consider_register_instance");
//    out("LAC.consider_register_instance");
//    return null;
//  }
  
  public PerformDescriptor perform_register_instance(MLMessage msg) {
    in("LAC.perform_register_instance");
    Status ret = null;
    URLDescriptor url = null;

    try {	
//      Object obj = CASAUtil.unserialize(msg.getParameter(ML.CONTENT));
      Object obj = msg.getContent();
      if (obj instanceof Object[]) {
        Object[] array = (Object[]) obj;
        if (array.length == 2 && array[0] instanceof URLDescriptor &&
            array[1] instanceof Boolean) {
          url = (URLDescriptor) array[0];
          ret = registerAgentInstance(url);//, thisProcess);
        }
      }
    }
    catch (Exception ex) {
      ret = new Status(-34,"LAC.perform_register_instance: Expected array [ URLDescriptor, Boolean ] in content \""+msg.getParameter(ML.CONTENT)+"\"",ex);
    }

    if (ret == null)
      ret = new Status(-33,"LAC.perform_register_instance: Expected array [ URLDescriptor, Boolean ] in content \""+msg.getParameter(ML.CONTENT)+"\"");

    PerformDescriptor r = new PerformDescriptor(ret); 
    out("LAC.perform_register_instance");
    return r;
  }


  //////////////////////////////////////////////////////////////////////////////
  // REGISTER TYPE /////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  /**
   * Registers an agent type according to
   * LACInterface.registerAgentType(String RunDescriptor).
   * @param path
   * @param r
   * @return a status
   */
  @Override
	public Status registerAgentType (String path, RunDescriptor r) {
    in ("LAC.respondToRegisterAgentType");
    if (path != null && path.length () > 0) {

      String type = new String (path);

      //store the type and RunDescriptor in registeredAgents
      type = type.replace ('/', '.');

      if (type.charAt (type.length () - 1) == '.') {
        type = type.substring (0, type.length () - 1);

      }
      Object o = registeredAgents.put (type, r);
      properties.setString ("registeredAgents",
                            CASAUtil.serialize (registeredAgents));

      // create a corresponding directory for this type
      String p = path.replace ('.', '/');
      p = rootDirectory + p;
      File f = new File (p);
      if (!f.exists ()) {
        if (!f.mkdirs ()) {
        	Trace.log("error", "Unable to create directory for type: " + path);
        }
      }

      //notifyObservers (new casa.State (ObservableEvent.STATE_REGISTER_TYPE));
      notifyObservers (ML.EVENT_REGISTER_TYPE, null);
      out ("LAC.respondToRegisterAgentType");
      return new Status (o == null ? 0 : 1,
                         (o == null ? "New type added into " : "Replaced type in") +
                         " agent type registry");
    }
    out ("LAC.respondToRegisterAgentType");
    return new Status ( -1,
                       "Bad parameters: registerAgentType(String \"" + path +
                       "\", RunDescriptor " + (r == null ? "null" : r.toString ()) +
                       ")");
  }

  /**
   * Looks up a RunDesciptor, given a type-path
   * @param path A path of the form x/y/z
   * @return The corresponding RunDescriptor; null if there is not entry for the path
   */
  public RunDescriptor getRunDescriptor (String path) {
    in ("LAC.getRunDescriptor");

    String type = new String (path);
    type = type.replace ('/', '.');

    if (type.length () > 1 && type.charAt (type.length () - 1) == '.') {
      type = type.substring (0, type.length () - 1);

    }
    out ("LAC.getRunDescriptor");
    return registeredAgents.get (type);
  }

  /**
   * Handler for a <em>register.agentType</em> message.  Parses the the
   * Incoming <em>message</em> and then calls registerAgentType().
   * @param message The incoming message
   * @return
   */
  protected PerformDescriptor perform_RegisterAgentType (MLMessage message) {
    in ("LAC.respondToRegisterAgentType");

    //read the relevant info from the message
    String content = message.getParameter (ML.CONTENT);

    PerformDescriptor ret = new PerformDescriptor();
    
    //interpret the fields of the message
    try {
      RunDescriptor descriptor = new RunDescriptor ();
      int pos = descriptor.parse (content, 0);
      String path = content.substring (pos).trim ();
      Status result = registerAgentType (path, descriptor);
      ret.put(ML.CONTENT, result.toString ());
      ret.put(ML.LANGUAGE, StatusURLDescriptor.class.getName ());
    } catch (Exception e) {
      println("warning","(in LAC.respondToRegisterAgentType)",e);
      ret.put(ML.PERFORMATIVE, ML.FAILURE);
      ret.put(ML.LANGUAGE, String.class.getName ());
      ret.put(ML.CONTENT, "Could not interpret URL in from/sender or RunDescriptor in content field:" +
                          e.toString ());
    }

    out ("LAC.respondToRegisterAgentType");
    return ret;
  }

  //////////////////////////////////////////////////////////////////////////////
  // UNREGISTER INSTANCE /////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  /**
   * Unregisters an agent instance according to LACInterface.unregisterAgentInstance(URLDescriptor).
   * @param newURL
   * @return a status
   */
  @Override
	public Status unregisterAgentInstance (URLDescriptor newURL) {
    in ("LAC.unregisterAgentInstance");

    if (!runningAgents.containsKey (newURL.getPath ())) {
      out ("LAC.unregisterAgentInstance");
      return new Status ( -1,
                         "Agent '" + newURL.getPath () + "' does not exist in database");
    } else {
      removedVector.add (0, newURL);
      runningAgents.remove (newURL.getPath ());
      //notifyObservers (new casa.State (ObservableEvent.STATE_UNREGISTER_INSTANCE));
      notifyObservers(ML.EVENT_UNREGISTER_INSTANCE, null);

      out ("LAC.unregisterAgentInstance");
      return new Status (0);
    }
  }

  /**
   * Handler for an <em>unregister.instance</em> message that has a
   * <em>request</em> performative.  Parses the the incoming <em>message</em>
   * and then calls unregisterAgentInstance().  Sends a reply.
   *
   * @param message The incoming message
   * @return
   */
  protected PerformDescriptor perform_UnregisterAgentInstance (MLMessage message) {
    in ("LAC.respondToUnregisterAgentInstance");

    //read the relevant info from the message
    String content = message.getParameter (ML.CONTENT);

    PerformDescriptor ret = new PerformDescriptor();
    
    //interpret the fields of the message
    try {
      URLDescriptor descriptor = URLDescriptor.make (content);
      Status result = unregisterAgentInstance (descriptor);
      ret.put(ML.CONTENT, result.toString ());
      ret.put(ML.LANGUAGE, Status.class.getName ());
    } catch (URLDescriptorException e) {
      println("warning","(in LAC.respondToUnregisterAgentInstance)",e);
      ret.put(ML.PERFORMATIVE, "failure");
      ret.put(ML.LANGUAGE, String.class.getName ());
      ret.put(ML.CONTENT,
          "Could not interpret URL in from/sender or content field: " +
                          e.toString ());
    }
    out ("LAC.respondToUnregisterAgentInstance");
    return ret;
  }

  /**
   * Handler for an <em>unregister.instance</em> message that has an
   * <em>inform</em> performative.  Parses the the incoming <em>message</em>
   * and then calls unregisterAgentInstance().  Does not send a reply.
   *
   * @param message The incoming message
   * @return
   */
  protected PerformDescriptor accept_UnregisterAgentInstance (MLMessage message) {
    in ("LAC.handleUnregisterAgentInstance");
    if (isLoggingTag("msgHandling")) println("msgHandling","Recieved an inform: " + ML.UNREGISTER_INSTANCE);

    //read the relevant info from the message
    String content = message.getParameter (ML.CONTENT);

    //interpret the fields of the message
    try {
      URLDescriptor descriptor = URLDescriptor.make (content);
      unregisterAgentInstance (descriptor);
    } catch (URLDescriptorException e) {
      println("warning","(in LAC.handleUnregisterAgentInstance)",e);
    }

    out ("LAC.handleUnregisterAgentInstance");
    return new PerformDescriptor();
  }

  //////////////////////////////////////////////////////////////////////////////
  // UNREGISTER TYPE /////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  /**
   * Unregisters an agent instance according to
   * LACInterface.unregisterAgentType(String).
   * @param path
   * @return a status
   */
  @Override
	public Status unregisterAgentType (String path) {
    in ("LAC.unregisterAgentType");
    String type = new String (path);

    //remove the type and RunDescriptor from registeredAgents
    type = type.replace ('/', '.');
    if (type.charAt (type.length () - 1) == '.') {
      type = type.substring (0, type.length () - 1);

    }
    Object o = registeredAgents.remove (type);
    properties.setString ("registeredAgents",
                          CASAUtil.serialize (registeredAgents));

    // Remove the corresponding directory for this type (only if there is no individuals).
    // Also remove any parent directories if they don't have any individuals, and they're not registered types.
    String p = path.replace ('.', '/');
    p = rootDirectory + p;
    File f = new File (p);
    while (true) {
      if (!f.exists ()) {
        break;
      }
      if (!f.delete ()) {
        break;
      }
      int i = p.lastIndexOf ('/'); //index of the last dir in path
      if (i > 1) {
        p = p.substring (0, i - 1); //remove teh last dir in path
      } else {
        break;
      }
      if (getRunDescriptor (p) != null) {
        break; //if it's registered don't delete
      }
    }

    //notifyObservers (new casa.State (ObservableEvent.STATE_UNREGISTER_TYPE));
    notifyObservers (ML.EVENT_UNREGISTER_TYPE, null);

    out ("LAC.unregisterAgentType");
    return new Status (o == null ? 1 : 0,
                       o == null ? "Could not find " + type + " in database; not removed." :
                       "success");
  }

  /**
   * Handler for an <em>unregister.agentType</em> message.  Parses the the
   * Incoming <em>message</em> and then calls unregisterAgentType().
   * @param message The incoming message
   * @return
   */
  protected PerformDescriptor perform_UnregisterAgentType (MLMessage message) {
    in ("LAC.respondToUnregisterAgentType");
    //read the relevant info from the message
    String content = message.getParameter (ML.CONTENT);

    PerformDescriptor ret = new PerformDescriptor();
    
    //interpret the fields of the message
    try {
      Status result = unregisterAgentType (content);
      ret.put(ML.CONTENT, result.toString ());
      ret.put(ML.LANGUAGE, Status.class.getName ());
    } catch (Exception e) {
      println("warning","(in LAC.handleUnregisterAgentInstance)",e);
      ret.put(ML.PERFORMATIVE, "failure");
      ret.put(ML.LANGUAGE, String.class.getName ());
      ret.put(ML.CONTENT,
                          "Could not interpret path in content field '" +
                          content + "':" + e.toString ());
    }

    out ("LAC.respondToUnregisterAgentType");
    return ret;
  }

  //////////////////////////////////////////////////////////////////////////////
  // GET_AGENTS_REGISTERED /////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

	/**
	 * Perform the action for an incoming <em>request</em> message for a <em>getAgentsRegistered</em> act-type request conversation.
	 * This method should return a 
	 * {@link casa.PerformDescriptor PerformDescriptor}.{@link casa.PerformDescriptor#getStatusValue() getStatusValue()}
	 * of 0 or positive to make the reply a SUCCESS, or a negative value to indicate FAILURE.  It may influence
	 * the reply by calling 
	 * {@link java.util.TreeMap#put(Object, Object) PerformDescriptor.put(messageKey,value)} on the
	 * return {@link casa.PerformDescriptor PerformDescriptor}.  For example, 
	 * ret.put({@link casa.ML#CONTENT},"content"} will fill the content field with "content"
	 * in the message to the client.  The default return 
	 * message will be an SUCCESS.  This method is required.
	 * @param msg The outgoing AGREE message
	 * @return The result of the processing; the status part will influence the return
	 *  message (if any), and the key/value part will be overlaid on the return message 
	 *  (if any)
	 * @since 11-Mar-09
	 * @updated 1-Jun-10
	 */
	public PerformDescriptor perform_get_agents_registered(MLMessage msg) {
		in("LAC.perform_get_agents_registered");
		
		PerformDescriptor ret = new PerformDescriptor(new Status(0));
		StringBuffer b = new StringBuffer();
		
		for (String url: (getAgentsRegistered().keySet())) 
			b.append(url).append('\n');
		
		ret.put(ML.CONTENT, b.toString());//return the urls
		ret.put(ML.LANGUAGE, "text");
		
		out("LAC.perform_get_agents_registered");
		return ret;	
	}

  //////////////////////////////////////////////////////////////////////////////
  // GET_AGENTS_RUNNING ////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
 
	/**
	 * Perform the action for an incoming <em>request</em> message for a 
	 * <em>get_agents_running</em> act-type request conversation.
	 * 
	 * This method should return a 
	 * {@link casa.PerformDescriptor PerformDescriptor}.{@link casa.PerformDescriptor#getStatusValue() getStatusValue()}
	 * of 0 or positive to make the reply a SUCCESS, or a negative value to indicate FAILURE.  It may influence
	 * the reply by calling 
	 * {@link java.util.TreeMap#put(Object, Object) PerformDescriptor.put(messageKey,value)} on the
	 * return {@link casa.PerformDescriptor PerformDescriptor}.  For example, 
	 * ret.put({@link casa.ML#CONTENT},"content"} will fill the content field with "content"
	 * in the message to the client.  The default return 
	 * message will be an SUCCESS.  This method is required.

	 * @param msg The outgoing AGREE message
	 * @return The result of the processing; the status part will influence the return
	 *  message (if any), and the key/value part will be overlaid on the return message 
	 *  (if any)
	 * @since 11-Mar-09
	 * @updated 1-Jun-10
	 */
	public PerformDescriptor perform_get_agents_running(MLMessage msg) {
		in("LAC.perform_get_agents_running");
		
		PerformDescriptor ret = new PerformDescriptor(new Status(0));
		ret.put(ML.CONTENT, CASAUtil.serialize(getAgentsRunning()));
		
		out("LAC.perform_get_agents_running");
		return ret;	
	}
  
  //////////////////////////////////////////////////////////////////////////////
  // FIND INSTANCES ////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  public StatusURLDescriptorList findInstances (String pattern) {
    in ("LAC.findInstances");
    Pattern p = Pattern.compile (pattern);
    Vector<URLDescriptor> result = new Vector<URLDescriptor> ();

    for (String currentPath: runningAgents.keySet()) {
      if (p.matcher(currentPath).matches ()) {
        result.add (runningAgents.get (currentPath));
      }
    }

    //notifyObservers (new casa.State (ObservableEvent.STATE_FIND_INSTANCES));
    notifyObservers (ML.EVENT_FIND_INSTANCES, null);
    StatusURLDescriptorList ret = new StatusURLDescriptorList (0, result);

    out ("LAC.findInstances");
    return ret;
  }

  // NOTE: return type has been changed from Status to StatusURLDescriptorList (Gabriel - July 21)
  protected PerformDescriptor perform_FindInstances (MLMessage message) {
    in ("LAC.respondToFindInstances");

    //read the relevant info from the message
    String content = message.getParameter (ML.CONTENT),
        language = message.getParameter (ML.LANGUAGE);

    PerformDescriptor ret = new PerformDescriptor();
    
    ret.put(ML.PERFORMATIVE, ML.SUCCESS);

    StatusURLDescriptorList result = null;
    //interpret the fields of the message
    try {
      if (language.equals (java.util.regex.Pattern.class.getName ())) {
        result = findInstances (content);
      } else {
        result = new StatusURLDescriptorList ( -1,
                                              "Improper language in request: " +
                                              language, null);
      }
      ret.put(ML.CONTENT, result.toString ());
      ret.put(ML.LANGUAGE, StatusURLDescriptorList.class.getName ());
    } catch (Exception e) {
      println("warning","(in LAC.respondToFindInstances)",e);
      ret.put(ML.PERFORMATIVE, "failure");
      ret.put(ML.LANGUAGE, String.class.getName ());
      ret.put(ML.CONTENT, "Could not interpret path in content field:" +
                          e.toString ());
    }

    out ("LAC.respondToFindInstances");
    return ret;
  }

  //////////////////////////////////////////////////////////////////////////////
  // RESOLVE URL ///////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  /**
   * Attempts to resolve a URL by the following method:
   * <ul>
       * <li> checks for matching registered agent instances and updates <em>url</em>
   * and returns if a match is found.
   * <li> checks for matching registered agent types
   * <ul>
   * <li> if a match is found, attempts to instantiate an individual, updating
   * <em>url</em> if it succeeds, or returning a fail status if it fails
   * <li> if a match is not found return with failure status without updating
   * <em>url</em>
   * </ul>
   * </ul>
   * @param url the unresolved URL, which will be updated to a resolved URL if successfull.
   * @return the success/failure of the operation:
   * <ul>
   * <li> 0 Success
   * <li> -1 Agent is neither runnig nor registered
   * <li> -2 Failed to run the agent
   * </ul>
   */
  @Override
	public Status resolveURL (URLDescriptor url) {

    //First, check to see if that agent is already running
  	String path = url.getPath();
    URLDescriptor x = runningAgents.get (path);
    if (x==null) { //check for the "only a name without a type" condition
    	for (String r: runningAgents.keySet()) {
    		int lastSlash = r.lastIndexOf("/");
    		if (lastSlash>0 && lastSlash+1<r.length()) {
    			String name = r.substring(lastSlash+1);
    			if (name.equals(path)) {
    				x = runningAgents.get(r);
    				break;
    			}
    		}
    	}
    }
    if (x != null) {
      url.copy (x);
      url.setLACport (getPort ());

      return new StatusObject<URLDescriptor> (0, url);
    }

    //next, check to see if this agent is registered
    RunDescriptor rd = registeredAgents.get (url.getDirectory ());
    if (x==null) { //check for the "only a name without a type" condition
    	for (String r: registeredAgents.keySet()) {
    		int lastSlash = r.lastIndexOf("/");
    		if (lastSlash>0 && lastSlash+1<r.length()) {
    			String name = r.substring(lastSlash+1);
    			if (name.equals(path)) {
    				rd = registeredAgents.get(r);
    				break;
    			}
    		}
    	}
    }

    if (rd != null) {
      int newPort = 0;
      try {
        newPort = rd.run ( -(getPort () + 1), getPort (), url.getPath (), this);
      } catch (Exception ex) {
        return new Status ( -2, "LAC.resolveURL: run failed: " + ex.toString ());
      }

      if (newPort > 0) {
//        url.setPort (newPort);
//        url.setHost (getURL ().getHost());
        try {
					return new StatusObject<URLDescriptor> (1, URLDescriptor.make(getURL().getHost(), newPort));
				} catch (URLDescriptorException e) {
					return new Status(-3, println("error", "LAC.resolveURL()", e));
				}
      }
    }
    return new Status ( -1,
        "LAC.resolveURL: Agent is neither running nor registered");
  }

  /**
   * Handler for a <em>resolve-url</em> message.  Parses the the
   * Incoming <em>message</em> and then calls resolveURL().
   * @param message The incoming message
   * @return
   */
  public PerformDescriptor perform_resolve_url (MLMessage message) {
    in ("LAC.perform_resolve_url");
    //read the relevant info from the message
    String content = message.getParameter (ML.CONTENT);

    PerformDescriptor ret = new PerformDescriptor();

    //interpret the fields of the message
    URLDescriptor url = null;
    try {
      URLDescriptor senderUrl = message.getFrom ();
      url = URLDescriptor.make (content);
      Status result = resolveURL (url);
      if (result.getStatusValue () < 0) {
        throw new Exception (result.getExplanation ());
      }
      ret.put(ML.CONTENT, ((StatusObject<URLDescriptor>)result).getObject().toString (senderUrl));
      ret.put(ML.LANGUAGE, URLDescriptor.class.getName ());
    } catch (Exception e) {
      println("warning","(in LAC.performResolveURL)",e);
      ret.put(ML.PERFORMATIVE, ML.FAILURE);
      ret.put(ML.LANGUAGE, String.class.getName ());
      String m = println("warning", "LAC.perform_resolve_url(): Could not interpret URL "+url+" in content of sender field in message, or failed resolution:", e);
      ret.put(ML.CONTENT, m);
      ret.setStatus(new Status(-5,m));
    }

    out ("LAC.perform_resolve_url");
    return ret;
  }

//  /**
//   * Overrides TransientAgent's version.
//   * Attempts to resolve the url of the receiver field of a message.
//   * It does this by calling resolveURL() if the message's receiver is
//   * local to the LAC, otherwise by calling do_ResolveURL_sync().
//   * @return Status(0) if msg was successfully updated, Status(-ve) otherwise.
//   */
//  @Override
//protected Status resolveConnectException (MLMessage msg, /*Command*/Runnable1<String,Status> cmd) {
//    in ("TransientAgent.resolveConnectException");
//    URLDescriptor url = null;
//    try {
//      url = URLDescriptor.make (msg.getParameter (ML.RECEIVER));
//    } catch (URLDescriptorException ex1) {
//      out ("TransientAgent.resolveConnectException");
//      return new Status ( -8,
//          "TransientAgent.resolveConnectException: found mangled receiver field");
//    }
//    Status stat =
//        (url.getHost ().equals (getURL ().getHost ()) &&
//         url.getLACport () == getURL ().getPort ()) //local
//        ? resolveURL (url)
//        : doResolveURL (url, 3000, cmd);
//    if (stat.getStatusValue () == 0) {
//      msg.setParameter (ML.RECEIVER, url.toString (getURL ()));
//    }
//    out ("TransientAgent.resolveConnectException");
//    return stat;
//  }


  protected PerformDescriptor perform_run_agent (MLMessage message) {
    in ("LAC.perform_run_agent");
    Status status;

    //read the relevant info from the message
    String content = message.getParameter (ML.CONTENT),
         language = message.getParameter (ML.LANGUAGE);


    if (language.equals ("casa.agentCom.URLDescriptor")) {
      try { //interpret the content as a URL
        status = runAgent (URLDescriptor.make (content));
      } catch (URLDescriptorException ex1) {
        status = new Status(-5,"LAC.perform_run_agent: Could not interpret content field URL",ex1);
      }
    }
    //if it's not a URL in the content, try it as a CASA command...
    else if (language.equals ("CASA Command Line Language")) {
      try {
        CASACommandLine2.main (content.split("\\s"));
        status = new Status (0, "Executed as a CASA command line.");
      } catch (Exception ex2) {
        status = new Status ( -3, "LAC.perform_run_agent: Could not interpret content CASA command line '"
                                   + content + "'",ex2);
      }
    }
    //don't recognize the language...
    else {
      status = new Status ( -4, "LAC.perform_run_agent: Unrecognized language '"
                                + language + "': Expected either 'CASA Command Line Language' or"
                                +" 'casa.agentCom.URLDescriptor'");
    }
    out ("LAC.perform_run_agent");
    return new PerformDescriptor(status);
  }

  /**
   * Handler for an <em>run.agent</em> message that has a
   * <em>request</em> performative.  Handles either a URL or a command in the
   * content part.
   * <table border=2 borderColor="blue">
   * <tr><th>CONTENT</th>
   *     <th>REGISTERED?</th>
   *     <th>RETURN PERFORATIVE</th>
   *     <th>RETURNS</th>
   * </tr>
   * <tr><td>URLDescriptor</td>
   *     <td>type registered</td>
   *     <td>reply or error</td>
   *     <td>Status(0,"success") or Status(-1,explanation)</td>
   * </tr>
   * <tr><td>URLDescriptor</td>
   *     <td>type not registered</td>
   *     <td>reply</td>
   *     <td>Status(1,"Found unregistered agent type in URL, tryied as a Java class")</td>
   * </tr>
   * <tr><td>anything else</td>
   *     <td>--</td>
   *     <td>reply</td>
   *     <td>Status(2,"Could not interpret content URL, tried as a CASA command line.")</td>
   * </tr>
   * </table>
   *
   * @param message The incoming message
   * @return the status of sendMessage(reply) (not that Status returned to the requester)
   */

  /**
   * Run an agent based on the parameter URL.
   * @param url
   * @return 0 for success; -1 for failure; 1 for unregistered: tried as a Java class (can't determine success)
   */
  public Status runAgent (URLDescriptor url) {
    in ("LAC.runAgent");

    String type = url.getDirectory ();

    //check to see if we have the agent type registered....
    if (registeredAgents.containsKey (type)) { //it's registered, fetch the run descriptor and execuite it
      RunDescriptor d = registeredAgents.get (type);

      try {
        d.run (url.getPort (), url.getLACport (), url.getPath (), this);

        out ("LAC.runAgent");
        return new Status (0, "success");
      } catch (Exception ex) {
        out ("LAC.runAgent");
        return new Status ( -1, ex.toString ());
      }
    } else { //it's an URL, but it's not registered.  Try it as as a CASA Java class
      String com = "+A" + url.getDirectory () + " -p" + url.getPort () + " -L" +
                   url.getLACport () + " -n" + url.getFile ();

      try {
        CASACommandLine2.main (com.split("\\s"));
      } catch (Exception ex) {
        out ("LAC.runAgent");
        return new Status ( -2, ex.toString ());
      }
      out ("LAC.runAgent");
      return new Status (1,
          "Found unregistered agent type in URL, tried as a Java class");
    }
  }

  //
  // Accessor Methods
  //

  public Vector<String> getAgentsRunningVector (URLDescriptor relativeTo) {
    in ("LAC.getAgentsRunningVector");

    Vector<String> runningAgentsVector = new Vector<String> ();
//    URLDescriptor descriptor = null;
//
//    Enumeration<?> index = runningAgents.keys ();
//    while (index.hasMoreElements ()) {
//      String item = (String) index.nextElement ();
//      descriptor = runningAgents.get (item);
    for (URLDescriptor descriptor: runningAgents.values()) {
      String agentPath = descriptor.getFullAddress (relativeTo);
      runningAgentsVector.add (agentPath);
    }

    out ("LAC.getAgentsRunningVector");
    return runningAgentsVector;
  }

  public Vector<URLDescriptor> getRunningAgentDescriptors () {
    in ("LAC.getRunningAgentDescriptors");

    Vector<URLDescriptor> runningDescriptors = new Vector<URLDescriptor> ();
//    URLDescriptor item = null;
//
//    Enumeration<?> e = runningAgents.elements ();
//    while (e.hasMoreElements ()) {
//      item = (URLDescriptor) e.nextElement ();
    for (URLDescriptor item: runningAgents.values()) {
      runningDescriptors.add (item);
    }

    out ("LAC.getRunningAgentDescriptors");
    return runningDescriptors;
  }

  public Vector<?> getRegisteredAgentsVector () {
    in ("LAC.getRegisteredAgentsVector");

    Vector<String> descriptorVector = new Vector<String> ();

    Enumeration<?> index = registeredAgents.keys ();
    while (index.hasMoreElements ()) {
      String item = (String) index.nextElement ();
      descriptorVector.add (item);
    }

    out ("LAC.getRegisteredAgentsVector");
    return descriptorVector;
  }

  /**
	 * @see casa.TransientAgent#consider_new_MenuItem(casa.MLMessage)
	 * Calls {@link #lacWindow}.{@link LACWindow#createRequestedMenuItem(MLMessage)}
	 * and then the parent implementation.
	 * @param message
	 * @return a {@link PerformDescriptor}
	 * @author kremer
	 */
	@Override
	public PerformDescriptor consider_new_MenuItem(MLMessage message) {
		// TODO Auto-generated method stub
    lacWindow.createRequestedMenuItem(message);
		return super.consider_new_MenuItem(message);
	}
	/**
   * Once the PriorityQueue has been created, this method iterates through the PriorityQueue and sends
   * a message informing each agent (running under this process) that the LAC is closing.
   *
   * @return a boolean indicating whether all the registered agents were informed about
   * a closing event or not.
   */
  public boolean informTerminateProcess () {
    in ("LAC.informTerminateProcess");
    
    LinkedList<URLDescriptor> priorityQueue = new LinkedList<URLDescriptor> ();
    priorityQueue = createPriorityQueue (priorityQueue);
    
    boolean terminatedAllProcesses = false;

    for(URLDescriptor agentToInform: priorityQueue){
    	MLMessage message = getNewMessage (ML.INFORM, ML.LAC_CLOSING,agentToInform);
        message.setParameter (ML.LANGUAGE, URLDescriptor.class.getName ());

        sendMessage (message);

        try {
          Thread.sleep (800);
        } catch (InterruptedException ex) {}

        if (priorityQueue.getLast ().equals (agentToInform)) {
          terminatedAllProcesses = true;
        }
      }

    out ("LAC.informTerminateProcess");
    return priorityQueue.isEmpty() || terminatedAllProcesses;
  }

  /**
   * Since there is a specific order to follow during the event LAC.LAC_CLOSING, this function organizes
   * all the URLDescriptors of the running agents (under this process) in a PriorityQueue.
   * The resulting PriorityQueue will look something like (from low to high priority):
   *
   * <ol>
   *     <li>Security</li>
   *     <li>CooperationDomain</li>
   *     <li>Any Other agent type (YP, ChatAgent)</li>
   * </ol>
   *
   * @param lList is a LinkedList that is going to work as a PriorityQueue
   * @return a priority queue
   */
  protected LinkedList<URLDescriptor> createPriorityQueue (LinkedList<URLDescriptor> lList) {
    in ("LAC.createPriorityQueue");
    Matcher securityMatcher = null;
    Matcher cdMatcher = null;
//    URLDescriptor agentToInform = null;
//
//    Enumeration<?> runningIndex = runningAgents.elements ();
//    while (runningIndex.hasMoreElements ()) {
//      agentToInform = (URLDescriptor) runningIndex.nextElement ();
    for (URLDescriptor agentToInform: runningAgents.values()) {

      securityMatcher = Pattern.compile ("security").matcher (agentToInform.
          getDirectory ());
      cdMatcher = Pattern.compile ("CooperationDomain").matcher (agentToInform.
          getDirectory ());

      if (cdMatcher.find ()) {
        if (otherIndex == 0) {
          lList.add (agentToInform);
          cdIndex++;
        } else {
          lList.add ((otherIndex + cdIndex), agentToInform);
          cdIndex++;
        }
      } else if (securityMatcher.find ()) {
        lList.addLast (agentToInform);
      } else {
        lList.addFirst (agentToInform);
        otherIndex++;
      }
    }

    out ("LAC.createPriorityQueue");
    return lList;
  }

  public void deleteFile (String path) {
    in ("LAC.deleteFile");

    String file = path2file (path);
    int len = path.length ();

    if (!path.substring (len - 5, len).equals (".casa")) {
      file = file.substring (0, file.length () - 5);

    }
    len = file.length ();
    File f = new File (file);

    if (file.substring (len - 5, len).equals (".casa") || f.isDirectory ()) {
      try {
        if (!f.delete ()) {
        	Trace.log("error", "Cannot delete file '" + file + "': " +
                         (f.isDirectory () ? "Type has persistent or active agents?" :
                          "Unknown reason"));
        }
      } catch (Exception ex) {
      	Trace.log("error", "Cannot delete '" + file, ex);
      }
    } else {
    	Trace.log("error", "Cannot delete: not a casa file");
    }

    out ("LAC.deleteFile");
  }
  
  /**
   * getAgentsRegistered()
   * 
   * @return Hashtable<String,RunDescriptor>
   */
  public Hashtable<String,RunDescriptor> getAgentsRegistered(){
  	return registeredAgents;
  }
  
  /**
   * getAgentsRunning()
   * 
   * @return Hashtable<String,RunDescriptor>
   */
  public ConcurrentSkipListMap<String,URLDescriptor> getAgentsRunning() {
  	return runningAgents;
  }
  
  //************ Server methods for an REQUEST getAgentsRunning conversation **************

	/**
	 * Perform the action for an incoming <em>request</em> message for a <em>getAgentsRunning</em> act-type request conversation.
	 * This method should return a 
	 * {@link casa.PerformDescriptor PerformDescriptor}.{@link casa.PerformDescriptor#getStatusValue() getStatusValue()}
	 * of 0 or positive to make the reply a SUCCESS, or a negative value to indicate FAILURE.  It may influence
	 * the reply by calling 
	 * {@link java.util.TreeMap#put(Object, Object) PerformDescriptor.put(messageKey,value)} on the
	 * return {@link casa.PerformDescriptor PerformDescriptor}.  For example, 
	 * ret.put({@link casa.ML#CONTENT},"content"} will fill the content field with "content"
	 * in the message to the client.  The default return 
	 * message will be an SUCCESS.  This method is required.
	 * @param msg The outgoing AGREE message
	 * @return The result of the processing; the status part will influence the return
	 *  message (if any), and the key/value part will be overlayed on the return message 
	 *  (if any)
	 * @since 11-Mar-09
	 */
//	public PerformDescriptor perform_getAgentsRunning(MLMessage msg) {
//		in("LAC.perform_getAgentsRunning");
//		PerformDescriptor ret = new PerformDescriptor(new Status(0));
//		StringBuffer b = new StringBuffer();
//		for (URLDescriptor url: runningAgents.values()) b.append(url.toString()).append('\n');
//		ret.put(ML.CONTENT, b.toString());//return the urls
//		ret.put(ML.LANGUAGE, "text");
//		out("LAC.perform_getAgentsRunning");
//		return ret;
//	}

  //************ Server methods for an REQUEST getAgentsRegistered conversation **************

//	/**
//	 * Perform the action for an incoming <em>request</em> message for a <em>getAgentsRegistered</em> act-type request conversation.
//	 * This method should return a 
//	 * {@link casa.PerformDescriptor PerformDescriptor}.{@link casa.PerformDescriptor#getStatusValue() getStatusValue()}
//	 * of 0 or positive to make the reply a SUCCESS, or a negative value to indicate FAILURE.  It may influence
//	 * the reply by calling 
//	 * {@link java.util.TreeMap#put(Object, Object) PerformDescriptor.put(messageKey,value)} on the
//	 * return {@link casa.PerformDescriptor PerformDescriptor}.  For example, 
//	 * ret.put({@link casa.ML#CONTENT},"content"} will fill the content field with "content"
//	 * in the message to the client.  The default return 
//	 * message will be an SUCCESS.  This method is required.
//	 * @param msg The outgoing AGREE message
//	 * @return The result of the processing; the status part will influence the return
//	 *  message (if any), and the key/value part will be overlayed on the return message 
//	 *  (if any)
//	 * @since 11-Mar-09
//	 */
//	public PerformDescriptor perform_getAgentsRegistered(MLMessage msg) {
//		//TODO auto generated method for perform getAgentsRegistered for a request server conversation 
//		in("LAC.perform_getAgentsRegistered");
//		PerformDescriptor ret = new PerformDescriptor(new Status(0));
//		StringBuffer b = new StringBuffer();
//		for (String url: registeredAgents.keySet()) b.append(url).append('\n');
//		ret.put(ML.CONTENT, b.toString());//return the urls
//		ret.put(ML.LANGUAGE, "text");
//		out("LAC.perform_getAgentsRegistered");
//		return ret;
//	}



	
	@Override
	public AgentUI makeDefaultGUI(String[] args) {
		return new LACWindow(this, args);
	}

  @Override
	public void putUI(AgentUI ui) {
  	super.putUI(ui);
		ProcessInfo.lacUI = ui;
		if (LACWindow.class.isAssignableFrom(ui.getClass())) {
			lacWindow = (LACWindow) ui;
			ProcessInfo.desktop = lacWindow;
			lacWindow.setVisible(true);
		} else {
			lacWindow = null;
			ProcessInfo.desktop = null;
		}
	}

}