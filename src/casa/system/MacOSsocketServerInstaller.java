package casa.system;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Vector;

import javax.swing.JDialog;

import plistreader.AbstractReader;
import plistreader.AbstractWriter;
import plistreader.PlistFactory;
import plistreader.PlistProperties;
import plistreader.PlistReaderException;

class MacOSsocketServerInstaller {
	
	final static String plistFileName = "org.ksg.casa.plist";
	final static String plistPathName = "/Library/LaunchDaemons/"+plistFileName;
	
	final static String LABEL = "Label";
	final static String PROGRAMARGUMENTS = "ProgramArguments";
	final static String RUNATLOAD = "RunAtLoad";
	final static String SERVICEDESCRIPTION = "ServiceDescription";
	final static String USERNAME = "UserName";
	final static String GROUPNAME = "GroupName";
	final static String WORKINGDIRECTORY = "WorkingDirectory"; 
	final static String ROOTDIRECTORY = "RootDirectory"; 
	final static String STANDARDOUTPATH = "StandardOutPath";
	final static String STANDARDERRORPATH = "StandardErrorPath";
	final static String KEEPALIVE = "KeepAlive";
	
	//these variables are all bound to the dialog window
	public String user;
	public String group;
	public String workingDirectory;
	public int port=9000;
	public boolean keepAlive = true;
	public boolean plistExists = false;
	public String enableString = "Enable";
	/**
	 * @return the enableString
	 */
	public String getEnableString() {
		return enableString;
	}

	public String dialogMessage = "";


	private PlistProperties plistProp = null;
	private File plistFile = null;

	/**
	 * @return the plistExists
	 */
	public boolean isPlistExists() {
		return plistExists;
	}

	/**
	 * @return the dialogMessage
	 */
	public String getDialogMessage() {
		return dialogMessage;
	}

	MacOSsocketServerInstaller() {
		String osName = System.getProperty("os.name");
		if (!osName.contains("Mac OS X")) {
			String osArch = System.getProperty("os.arch");
			String osVersion = System.getProperty("os.version");
			String msg = "MacOSsocketServerInstaller only works on Mac OS X\nCurrent system is: os.name="+osName+"; os.arch="+osArch+"; os.version="+osVersion;
			System.out.println(msg);
			return;
			}
		plistProp = getPlistProp(); 
	}
	
	public PlistProperties getPlistProp() {
		plistFile = new File(plistPathName);
		PlistProperties props=null;
		if (plistFile.exists()) { 
			plistExists = true;
			enableString = "Update";
			dialogMessage = "Plist exists";
			AbstractReader reader = (new PlistFactory()).createReader();
			try {
				props = reader.parse(plistFile);
				user = (String)props.getProperty(USERNAME, "xxx");
				String serviceDesc = (String)props.getProperty(SERVICEDESCRIPTION, "@ 9000");
				String portString = serviceDesc.substring(serviceDesc.indexOf('@')+1);
				port = Integer.parseInt(portString.trim());
				workingDirectory = (String)props.getProperty(WORKINGDIRECTORY, "~"+user+"/.casa");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				dialogMessage = e.toString();
			}
			
		}
		else {
			plistExists = false;
			enableString = "Enable";
			dialogMessage = "Plist is absent";
		}
		
		if (props==null) {
			props = new PlistProperties();
		}
		return mergePlistWithVars(props, true);
	}
	/**
	 * 
	 * @param props
	 * @param init indicates the plist should take priority over the variables (only on initialization usually).
	 * @return
	 */
	protected PlistProperties mergePlistWithVars(PlistProperties props, boolean init) {
		try {
			if (user==null)
				user = (String)props.getProperty(USERNAME,	null);
			if (user == null) {
				user = System.getProperty("user.name");
				props.setProperty(USERNAME, user);
			}
			if (user != null) {
				props.setProperty(USERNAME, user);
			}

			if (group==null)
				group = (String)props.getProperty(GROUPNAME,	null);
			if (group != null) {
				props.setProperty(GROUPNAME, group);
			}

			props.setProperty(LABEL, plistFileName);
			
			String jar = casa.system.MacOSsocketServerDialog.getJarFor(this.getClass());
			String[] command = new String[]{
					"/usr/bin/java",
					"-jar",
					jar,
					"-LAC",
					Integer.toString(port),
					"-daemon",
					"-killonlowmemory"
			};
			Vector array = new Vector<String>(5);
			for (String c:command) array.add(c);
			props.setProperty(PROGRAMARGUMENTS, array);

			props.setProperty(RUNATLOAD, true);

			if (init) {
				Object obj = props.getProperty(KEEPALIVE);
				if (obj instanceof Boolean) {
					keepAlive = (Boolean)obj;
				}
			}
			props.setProperty(KEEPALIVE, keepAlive);

			props.setProperty(SERVICEDESCRIPTION, "CASA LAC @ "+port);

			props.setProperty(ROOTDIRECTORY, "/");

			if (workingDirectory==null)
				workingDirectory = (String)props.getProperty(WORKINGDIRECTORY,	null);
			if (workingDirectory==null) 
				workingDirectory = getWorkingDirectory();
			if (workingDirectory!=null) { 
			  props.setProperty(WORKINGDIRECTORY, workingDirectory);
			  props.setProperty(STANDARDOUTPATH, workingDirectory+"/daemonLAC.log");
			  props.setProperty(STANDARDERRORPATH, workingDirectory+"/daemonLAC.log");
			}

		} catch (PlistReaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return props;
	}

	public void enable() {
		mergePlistWithVars(plistProp, false);

		System.out.println("Enabled:");
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(os);
		
		plistProp.list(ps);
		System.out.println(os.toString());
		
		AbstractWriter writer = (new PlistFactory()).createWriter();
		try {
			writer.write(plistFile, plistProp);
		} catch (PlistReaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			FileInputStream in = new FileInputStream(plistFile);
			byte[] b = new byte[in.available()];
			in.read(b);
			in.close();
			System.out.println(plistFile.getCanonicalPath()+":\n"+new String(b));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			Runtime.getRuntime().exec("launchctl load "+plistPathName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void disable() {
		File plist = new File(plistPathName);
		if (plist.exists()) { //delete the plist file (requires sudo)
			plist.delete();
		}
		try {
			Runtime.getRuntime().exec("launchctl unload "+plistPathName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Disabled.");
	}
	
	/**
	 * @return the user
	 */
	public String getUser() {
		if (user==null) 
			user = System.getProperty("user.name");
		return user;
	}
	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}
	/**
	 * @return the group
	 */
	public String getGroup() {
		return group;
	}
	/**
	 * @param group the group to set
	 */
	public void setGroup(String group) {
		this.group = group;
	}
	/**
	 * @return the workingDirectory
	 */
	public String getWorkingDirectory() {
		if (workingDirectory==null)
			workingDirectory = buildWorkingDirectory();
		return workingDirectory;
	}
	public String buildWorkingDirectory() {
		workingDirectory = "/Users/"+getUser()+"/.casa";
	  return workingDirectory;
	}
	/**
	 * @param workingDirectory the workingDirectory to set
	 */
	public void setWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}
	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the keepAlive
	 */
	public boolean isKeepAlive() {
		return keepAlive;
	}

	/**
	 * @param keepAlive the keepAlive to set
	 */
	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}
	
	/**
	 * Test.
	 */
	public static void main(String[] args) {
		MacOSsocketServerDialog.main(args);
	}

  public void setDefaultUser(String user) {
  	this.user=user;
  	workingDirectory = buildWorkingDirectory();
  	mergePlistWithVars(plistProp, true);
  }
	
}
