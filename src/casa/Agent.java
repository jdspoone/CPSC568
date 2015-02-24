package casa;

import casa.abcl.ParamsMap;
import casa.interfaces.AgentInterface;
import casa.io.CASAFile;
import casa.io.CASAFilePropertiesMap;
import casa.io.CASAInputStream;
import casa.io.CASAOutputStream;
import casa.ui.AgentInternalFrame;
import casa.ui.AgentUI;
import casa.ui.TransientAgentInternalFrame;
import casa.util.AgentLookUpTable;
import casa.util.CASAUtil;
import casa.util.PropertyException;
import casa.util.Tristate;

import java.awt.Container;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * <code>Agent</code> is an extension of <code>TransientAgent</code> that adds the possibility 
 * to store properties and data. These can be stored persistently or can be temporary for each
 * instance. <p> 
 * <b>WARNING!</b> You cannot do anything with the CASAFile data file (or the properties) within
 * the constructor. If you need to do this, override {@link #initializeThread(ParamsMap, AgentUI)}
 * and put such initialization in that method. 
 * <p> Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission
 * to use, copy, modify, distribute and sell this software and its documentation for any purpose 
 * is hereby granted without fee, provided that the above copyright notice appear in all copies 
 * and that both that copyright notice and this permission notice appear in supporting 
 * documentation. The Knowledge Science Group makes no representations about the suitability 
 * of this software for any purpose. It is provided "as is" without express or implied warranty. </p>
 * @see TransientAgent
 * @author  Jason Heard, <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public class Agent extends TransientAgent implements AgentInterface {
	
	/**
	 * Stores all of the data corresponding to the current <code>Agent</code>. If this agent is not persistent, this file will be deleted when the agent is shut down.
	 */
	@CasaPersistent
	private CASAFile dataFile;

	/**
	 * The fully qualified filename of the file that the agent stores persistent
	 * date in. See dataFile.
	 */
	private String dataFileName;

	/**
	 * Stores the properties for this agent. The properties are stored in the  {@link #dataFile}  .
	 */
	protected CASAFilePropertiesMap properties;

	/**
	 * Determines whether this agent's data will be persistent:
	 * <code>true</code> if this agent's data will be persistent between
	 * instances; <code>false</code> otherwise. By default the value is
	 * <code>false</code>.
	 * 
	 */
	@CasaPersistent
	private Tristate persistent = Tristate.UNDEFINED;

	/**
	 * Used by the constructors to note whether persistence has been explicitly
	 * set or not.
	 */
	// private boolean setPersistent = false;
	/**
	 * Set to true when the constructor completes. This does not mean that the
	 * file and properties have been set yet.
	 */
	protected boolean agentConstructorComplete = false;

	/**
	 * Standard constructor
	 * @param params
	 * @param ui
	 * @throws Exception
	 */
	public Agent(ParamsMap params, AgentUI ui) throws Exception {
		super(params, ui);
		agentConstructorComplete = true;
	}
	
	public static void main(String[] args) {
		System.out.println(System.getProperty("user.home") + "/.casa/persistent/");	
	}

	/**
	 * Determines if the agent has been fully initialized: registered with the
	 * LAC and initialized its data file and properties.
	 * 
	 * @return true iff initialization is complete.
	 */
	@Override
	public boolean ready() {
		in("Agent.ready");
		boolean ret = (!isPersistent() || dataFile != null) && isInitialized();
		out("Agent.ready: " + (ret ? "true" : "false"));
		return ret;
	}

  /**
   * Uses the {@link #properties} object to read in all of this agent's attributes tagged with 
   * the @{@link CasaPersistent} annotation from persistent store.
   */
  public void readPersistent() {
  	Status stat = CASAUtil.readPersistentFromProperties("", this, properties);
  	if (stat.getStatusValue()!=0) 
  		println(stat.getStatusValue()<0?"error":"warning", "Agent.getPersistent(): "+stat.getExplanation());
  }
  
  /**
   * Called by handleReply_registerAgentInstance. During the constructor, the
   * Agent registers with the LAC, but has to wait for the LAC to reply before
   * initializing the file and properties. This init() method is called at
   * that time to initialize the file and properties.<br>
   * This version handles persistence details, and calls the super method
   * ({@link TransientAgent#initializeAfterRegistered(boolean)}) <em>after</em> processing the persistent data.
   * @see TransientAgent#initializeAfterRegistered(boolean)
   */
  @Override
  public void initializeAfterRegistered(boolean registered) {
  	in("Agent.init");
  	try {
  		//NOTE: the super method is called at the END of this method
  		Object obj = getInitParams().getJavaObject("PERSISTENT");
  		boolean persists = false;
  		if (obj !=null) { 
  			if (obj instanceof Boolean)  
  				persists = (Boolean)obj;
  			if (obj instanceof String && !(((String)obj).equalsIgnoreCase("FALSE") || ((String)obj).equalsIgnoreCase("NIL") || ((String)obj).equalsIgnoreCase("NO") || ((String)obj).equalsIgnoreCase("NULL")))  
  				persists = true;
  		}
  		setPersistent(persists);
  		if (persists) {
  			try {
  				initializeFile();

  				// load remaining persistent data if applicable
  				// read the options data structure and other attributes that persisted from last run
  				readPersistent();

  				// restore the command line setting (override persistent settings)
  				// as necessary
  				resetRuntimeOptionsFromCommandLine();

  				// The options object should now be restored, so make sure it's parameters are realized in the agent 
  				realizeAgentBehaviourFromOptions();

  				// read in _*ontology node
  				if (((AgentOptions) options).persistentOntology) {
  					String node = "_*ontology";
  					try {
  						long len = dataFile.getNodeLength(node);
  						if (len > 0) {
  							byte[] buf = new byte[(int) len];
  							dataFile.read(buf, node);
  							String sbuf = new String(buf);
  							// TODO this needs to work with the v3 ontology
  							ontology.add(sbuf);
  						}
  					} catch (Exception ex3) {
  						println(
  								"warning",
  								"Agent.init: Can't read from persistent file or can't update performatives type hierarchy",
  								ex3);
  					}
  				}


  				properties.setInteger("port", getPort());

  				try {
  					properties.getString("CreateDate");
  				} catch (PropertyException ex4) {
  					Calendar calendar = Calendar.getInstance();
  					String format = "yyyy/MM/dd";
  					SimpleDateFormat dateformat = new SimpleDateFormat(format);
  					properties.setString("CreateDate", dateformat.format(calendar.getTime()));
  				}

  			} catch (Exception ex) {
  				println("warning", "Agent.init(): Failed to read persisent data", ex);
  			}
  		}
  		else { // we are not persistent
  			properties = null;
  		}
  	} catch (Throwable e) {
  		println("error", "Agent.initializeAfterRegistered("+registered+"): Unexpected error", e);
  	}

  	//call the super version of this method
  	super.initializeAfterRegistered(registered);
  	out("Agent.init");
  }

	/**
	 * Refresh agent behaviour from the options object. Subclasses should
	 * override as appropriate.
	 */
	@Override
	public void realizeAgentBehaviourFromOptions() {
		in("updateFromOptions");
		super.realizeAgentBehaviourFromOptions();
		out("updateFromOptions");
	}

	/**
	 * Returns the filename of the <code>CASAFile</code> that will be used by
	 * this agent to store its properties and data either temporarily or
	 * persistently.
	 * 
	 * @return The filename of the <code>CASAFile</code> that will be used by
	 *         this agent to store its properties and data.
	 * @throws FileNotFoundException 
	 */
	@Override
	public String getCASAFilename() throws FileNotFoundException {
		if (dataFileName!=null)
			return dataFileName;
//		if (isRegistered()) { //something is seriously wrong
//			throw new FileNotFoundException("Unable to resolve CASAFile persistence file");
//		}
//		else { // we did not register, or failed to register, try getting the dataFileName from preferences
			return CASAProcess.staticUrl2file(getURL());
//		}
	}
	
	public String getCreateDate(){
		String create;
		if (properties==null) return null;
		try{
			create = properties.getString("CreateDate");
		}
		catch(Throwable e){
			create = null;
		}
		return create;
	}

	/**
	 * Creates all sub directories needed to write the <code>CASAFile</code>
	 * for this agent.
	 * 
	 * @throws IOException
	 *             If one of the directories could not be created.
	 */
	private void createDirectories() throws IOException {
		in("Agent.createDirectories");
		String filePath = getCASAFilename();
		int nextSpot = filePath.indexOf('/');
		int start = 0;
		if (filePath.length() > 2 && filePath.charAt(1) == ':') { // Windows/DOS
																	// special
																	// case
			start = 2;
		}
		if (nextSpot == start) {
			nextSpot = filePath.indexOf('/', nextSpot + 1); // if this is a
															// top-level
															// directory, start
															// in the right
															// place

		}
		while (nextSpot != -1) {
			File tempFile = new File(filePath.substring(0, nextSpot));
			if (!tempFile.isDirectory()) {
				if (!tempFile.mkdir()) {
					out("Agent.createDirectories");
					throw new IOException("Could not create directory: "
							+ tempFile.getAbsolutePath());
				}
			}

			nextSpot = filePath.indexOf('/', nextSpot + 1);
		}
		out("Agent.createDirectories");
	}

	/**
	 * This initializes both the <code>CASAFile</code> that will be used to
	 * store the data, and the properties object.
	 */
	private synchronized void initializeFile() throws IOException {
		in("Agent.initializeFile");
//		try {
			createDirectories();
//		} catch (IOException e) {
//			// The directories were not created, all write operations are now
//			// guaranteed to fail.
//			println("warning", "Failed to create directories for casa file", e);
//		}
		dataFile = new CASAFile(getCASAFilename());
		properties = new CASAFilePropertiesMap(dataFile);
		out("Agent.initializeFile");
	}

	/**
	 * Sets whether this <code>Agent</code>'s data should be persistent
	 * between instances.
	 * 
	 * @param persistent
	 *            Whether this <code>Agent</code>'s data should be persistent
	 *            between instances.
	 */
	@Override
	public synchronized void setPersistent(boolean persistent) {
		in("Agent.setPersistent");
		((AgentOptions) options).persistent = persistent;
		options.write(properties);
		out("Agent.setPersistent");
	}

	public AgentOptions getAgentOptions() {
		return (AgentOptions) options;
	}
	
	/**
	 * Returns whether this agent's data will be persistent between instances.
	 * 
	 * @return <code>true</code> if this agent's data will be persistent
	 *         between instances; <code>false</code> otherwise.
	 */
	@Override
	public boolean isPersistent() {
		return ((AgentOptions) options).isPersistent();
	}

	/**
	 * Destroys the specified data object, deleting the corresponding node in
	 * the <code>CASAFile</code>. A boolean value is returned as to whether
	 * the data object existed in the first place.
	 * 
	 * @param dataObjectName
	 *            The name of the data object that we wish to destroy.
	 * @return <code>true</code> if the data object existed and was
	 *         successfully destroyed; <code>false</code> otherwise.
	 * @throws IOException
	 *             If there is a problem removing the node from the
	 *             <code>CASAFile</code>, such as a non-existent file.
	 */
	@Override
	public synchronized boolean destroyDataObject(String dataObjectName)
			throws IOException {
		in("Agent.destroyDataObject");
		boolean ret = dataFile.deleteNode(dataObjectName);
		out("Agent.destroyDataObject");
		return ret;
	}

	/**
	 * Stores some data for this agent based on the specified
	 * <code>DataStorageDescriptor</code>. The <code>Status</code> is
	 * returned indicating the success of the operation.
	 * 
	 * @param dsd
	 *            The <code>DataStorageDescriptor</code> that specifies what
	 *            data is to be written as well as to which dataobject it should
	 *            be written.
	 * @return A <code>Status</code> object indicating the result of the
	 *         attempt.
	 */
	@Override
	public Status putDataObject(DataStorageDescriptor dsd) {
		in("Agent.putDataObject");
		Status stat = putDataObject(dsd.getData(), dsd.getDataObjectName(), dsd
				.willAppend());
		out("Agent.putDataObject");
		return stat;
	}

	/**
	 * Stores a <code>DataDescriptor</code> for this agent to the specified
	 * data object. The <code>Status</code> is returned indicating the success
	 * of the operation.
	 * 
	 * @param data
	 *            The <code>DataDescriptor</code> to be stored.
	 * @param dataObjectName
	 *            The name of the data object that will be written to or written
	 *            over.
	 * @param append
	 *            Sets whether the data object should be appended to, or
	 *            overwritten.
	 * @return A <code>Status</code> object indicating the result of the
	 *         attempt.
	 */
	@Override
	public synchronized Status putDataObject(String data,
			String dataObjectName, boolean append) {
		in("Agent.putDataObject");
		try {
			if (append) {
				dataFile.appendNode(dataObjectName, data.getBytes());
			} else {
				dataFile.putNode(dataObjectName, data.getBytes());
			}
		} catch (IOException e) {
			println("warning", "Agent.putDataObject", e);
			out("Agent.putDataObject");
			return new Status(1, e.getMessage());
		}

		out("Agent.putDataObject");
		return new Status(0);
	}

	/**
	 * Returns an <code>OutputStream</code> that can be used to write to the
	 * specified data object.
	 * 
	 * @param dataObjectName
	 *            The name of the data object that the <code>OutputStream</code>
	 *            will write to.
	 * @param append
	 *            Sets whether the data object will be appended to, or
	 *            overwritten by the <code>OutputStream</code>.
	 * @return An <code>OutputStream</code> that can be used to write to the
	 *         specified data object.
	 * @throws IOException
	 *             If there is a problem creating the <code>OutputStream</code>
	 *             for the specified data object.
	 */
	@Override
	public OutputStream getDataObjectOutputStream(String dataObjectName,
			boolean append) throws IOException {
		in("Agent.putDataObjectOutputStream");
		OutputStream ret;
		if (append) {
			ret = new CASAOutputStream(dataObjectName, CASAFile.MODE_APPEND,
					dataFile);
		} else {
			ret = new CASAOutputStream(dataObjectName, CASAFile.MODE_OVERWRITE,
					dataFile);
		}
		out("Agent.putDataObjectOutputStream");
		return ret;
	}

	/**
	 * Returns an <code>InputStream</code> that can be used to read from the
	 * specified data object.
	 * 
	 * @param dataObjectName
	 *            The name of the data object that the <code>InputStream</code>
	 *            will read from.
	 * @return An <code>InputStream</code> that can be used to read from the
	 *         specified data object.
	 * @throws IOException
	 *             If there is a problem creating the <code>InputStream</code>
	 *             for the specified data object.
	 */
	@Override
	public InputStream getDataObjectInputStream(String dataObjectName)
			throws IOException {
		in("Agent.putDataObjectInputStream");
		InputStream ret = new CASAInputStream(dataObjectName, dataFile);
		out("Agent.putDataObjectInputStream");
		return ret;
	}

	/**
	 * Retreives a <code>StatusDataDescriptor</code> for this agent from the
	 * specified data object. The <code>StatusDataDescriptor</code> contains
	 * the status of the attempt and the data of the specified data object.
	 * 
	 * @param dataObjectName
	 *            The name of the data object that will be read from.
	 * @return A <code>StatusDataDescriptor</code> containing the status of
	 *         the attempt and the data of the specified data object.
	 */
	@Override
	public StatusString getDataObject(String dataObjectName) {
		in("Agent.getDataObject");
		String data = null;
		if (dataFile==null) return new StatusString(-1,"Not a persistent agent");

		try {
			data = new String(dataFile.getNode(dataObjectName));
		} catch (Exception e) {
			println("warning", "Agent.getDataObject", e);
			out("Agent.getDataObject");
			return new StatusString(1, data);
		}

		out("Agent.getDataObject");
		return new StatusString(0, data);
	}

	/**
	 * Stores a boolean property. If there was a property with the same name
	 * previously, it is overwritten, even if the type is different.
	 * 
	 * @param name
	 *            The name of the property to store.
	 * @param value
	 *            The value of the property that matches the name given.
	 */
	@Override
	public synchronized void setBooleanProperty(String name, boolean value) {
		if (properties != null)
			properties.setBoolean(name, value);
	}

	/**
	 * Retrieves the boolean property, returning it to the user.
	 * 
	 * @param name
	 *            The name of the property to retrieve.
	 * @return The value of the property that matches the name given.
	 * @throws PropertyException
	 *             If the given property is not a boolean property.
	 */
	@Override
	public boolean getBooleanProperty(String name) throws PropertyException {
		if (properties == null) {
			throw new PropertyException("not a persistent agent");
		}
		return properties.getBoolean(name);
	}

	/**
	 * Stores a String property. If there was a property with the same name
	 * previously, it is overwritten, even if the type is different.
	 * 
	 * @param name
	 *            The name of the property to store.
	 * @param value
	 *            The value of the property that matches the name given.
	 */
	@Override
	public synchronized void setStringProperty(String name, String value) {
		if (properties != null)
			properties.setString(name, value);
	}

	/**
	 * Retrieves the String property, returning it to the user.
	 * 
	 * @param name
	 *            The name of the property to retrieve.
	 * @return The value of the property that matches the name given.
	 * @throws PropertyException
	 *             If the given property is not a String property.
	 */
	@Override
	public String getStringProperty(String name) throws PropertyException {
		if (properties == null) {
			throw new PropertyException("not a persistent agent");
		}
		return properties.getString(name);
	}

	/**
	 * Stores an integer property. If there was a property with the same name
	 * previously, it is overwritten, even if the type is different.
	 * 
	 * @param name
	 *            The name of the property to store.
	 * @param value
	 *            The value of the property that matches the name given.
	 */
	@Override
	public synchronized void setIntegerProperty(String name, int value) {
		if (properties != null)
			properties.setInteger(name, value);
	}

	/**
	 * Retrieves the integer property, returning it to the user.
	 * 
	 * @param name
	 *            The name of the property to retrieve.
	 * @return The value of the property that matches the name given.
	 * @throws PropertyException
	 *             If the given property is not an integer property.
	 */
	@Override
	public int getIntegerProperty(String name) throws PropertyException {
		if (properties == null) {
			throw new PropertyException("not a persistent agent");
		}
		return properties.getInteger(name);
	}

	/**
	 * Stores a long integer property. If there was a property with the same
	 * name previously, it is overwritten, even if the type is different.
	 * 
	 * @param name
	 *            The name of the property to store.
	 * @param value
	 *            The value of the property that matches the name given.
	 */
	@Override
	public synchronized void setLongProperty(String name, long value) {
		if (properties != null)
			properties.setLong(name, value);
	}

	/**
	 * Retrieves the long integer property, returning it to the user.
	 * 
	 * @param name
	 *            The name of the property to retrieve.
	 * @return The value of the property that matches the name given.
	 * @throws PropertyException
	 *             If the given property is not a long integer property.
	 */
	@Override
	public long getLongProperty(String name) throws PropertyException {
		if (properties == null) {
			throw new PropertyException("not a persistent agent");
		}
		return properties.getLong(name);
	}

	/**
	 * Stores a floating point property. If there was a property with the same
	 * name previously, it is overwritten, even if the type is different.
	 * 
	 * @param name
	 *            The name of the property to store.
	 * @param value
	 *            The value of the property that matches the name given.
	 */
	@Override
	public synchronized void setFloatProperty(String name, float value) {
		if (properties != null)
			properties.setFloat(name, value);
	}

	/**
	 * Retrieves the floating point property, returning it to the user.
	 * 
	 * @param name
	 *            The name of the property to retrieve.
	 * @return The value of the property that matches the name given.
	 * @throws PropertyException
	 *             If the given property is not a floating point property.
	 */
	@Override
	public float getFloatProperty(String name) throws PropertyException {
		if (properties == null) {
			throw new PropertyException("not a persistent agent");
		}
		return properties.getFloat(name);
	}

	/**
	 * Stores a double precision floating point property. If there was a
	 * property with the same name previously, it is overwritten, even if the
	 * type is different.
	 * 
	 * @param name
	 *            The name of the property to store.
	 * @param value
	 *            The value of the property that matches the name given.
	 */
	@Override
	public synchronized void setDoubleProperty(String name, double value) {
		if (properties != null)
			properties.setDouble(name, value);
	}

	/**
	 * Retrieves the double precision floating point property, returning it to
	 * the user.
	 * 
	 * @param name
	 *            The name of the property to retrieve.
	 * @return The value of the property that matches the name given.
	 * @throws PropertyException
	 *             If the given property is not a double precision floating
	 *             point property.
	 */
	@Override
	public double getDoubleProperty(String name) throws PropertyException {
		if (properties == null) {
			throw new PropertyException("not a persistent agent");
		}
		return properties.getDouble(name);
	}

	/**
	 * Returns whether the specified property is contained in the properties.
	 * 
	 * @param propertyName
	 *            The name of the property that we are checking for.
	 * @return <code>true</code> if there exists a property with the specified
	 *         name; <code>false</code> otherwise.
	 */
	@Override
	public boolean hasProperty(String propertyName) {
		return properties == null ? false : properties
				.hasProperty(propertyName);
	}

	/**
	 * Removes the property from the properties.
	 * 
	 * @param propertyName
	 *            The name of the property to be removed.
	 */
	@Override
	public void removeProperty(String propertyName) {
		if (properties != null)
			properties.removeProperty(propertyName);
	}

	/**
	 * Save the options.
	 */
	@Override
	public void updateOptions() {
		in("Agent.updateOptions");
		if (properties != null) {
			boolean keep = properties.getKeepFileUpdated();
			properties.setKeepFileUpdated(false);
			options.write(properties);
			properties.setKeepFileUpdated(keep);
		}
		out("Agent.updateOptions");
	}

	/**
	 * This is called when the agent is shutting down. If the agents is set as
	 * persistent: it writes out the properties, _*performatives, and _*acts
	 * nodes to the persistent file. If the agent is <b>not</b> set as
	 * persistent, it removes the <code>CASAFile</code>.
	 */
	@Override
	protected void pendingFinishRun() {
		super.pendingFinishRun();
		if (!usingProxy) {
			AgentLookUpTable.remove(this.getURL());
		} else {
			// protectedAgentVector.remove (this.getURL());
		}

		if (isPersistent()) {

			// write out the _*properties node
			//options.write(properties);
			try {
				if (properties==null) initializeFile();
				writePersistent();
				properties.writeProperties();

				// write out the _*performatives node
				if (dataFile!=null) {
					if (((AgentOptions) options).persistentOntology) {
						try {
							dataFile.putNode("_*ontology", ontology.toString()
									.getBytes());
						} catch (IOException ex) {
							println(null,
							"Agent.pendingFinishRun: could not write _*performatives node to CASAfile.");
						}
					} else {
						try {
							dataFile.deleteNode("_*ontology");
						} catch (IOException ex1) {
						}
					}
				}
			} catch (IOException e) {
				println("error", "Failed to write persistent data", e);
			}


		}

		else {
			// Use deleteOnExit because another thread may still be reading this file.
			// AWTEventQueue appears to have some delayed runnables which still need to
			// read from this file.
			if (dataFile != null)
				dataFile.deleteOnExit();
		}
	}

	/**
	 * Overrides <code>Object.finalize()</code> in order to perform a final
	 * clean up to the resources used by an <code>Agent</code>. Called by the
	 * garbage collector on an <code>Agent's</code> object when garbage
	 * collection determines that there are no more references to such object.
	 * 
	 * @throws Throwable
	 *             May be thrown by super.finalize().
	 */
	@Override
	protected void finalize() throws java.lang.Throwable {
		in("Agent.finalize");
		super.finalize();

		if (!isPersistent()) {
			if (dataFile!=null) dataFile.delete();
		}
		out("Agent.finalize");
	}

//	/**
//	 * Override of the "virtual" method in TransientAgent (which does nothing).
//	 * {@link TransientAgent#release_register_instance(MLMessage)} will
//	 * call this method to set the filename.
//	 * 
//	 * @param filename
//	 *            a filename to use as the persistent file
//	 */
//	@Override
//	public void setFile(String filename) {
//		dataFileName = filename;
//	}
//
	@Override
	protected ProcessOptions makeOptions() {
		return new AgentOptions(this);
	}

  @Override
	public AgentOptions getOptions () {
    return (AgentOptions) super.getOptions ();
  }

  @Override
	public void setOptions(ProcessOptions options) {
    if (options instanceof AgentOptions) {
    	super.setOptions (options);
		} else {
			println("error","Agent.setOptions() should have arg type of AgentOptions, but got was type "+options.getClass());
		}
  }
  
  /**
   * Uses the {@link #properties} object to write out all of this agent's attributes tagged with 
   * the @{@link CasaPersistent} annotation to persistent store.
   */
  public void writePersistent() {
  	Status stat = CASAUtil.writePersistentToProperties("", this, properties);
  	if (stat.getStatusValue()!=0) 
  		println(stat.getStatusValue()<0?"error":"warning", "Agent.writePersistent(): "+stat.getExplanation());
  }

  /* (non-Javadoc)
	 * @see casa.TransientAgent#makeDefaultGUI(java.lang.String[], java.lang.String)
	 */
	@Override
	protected AgentUI makeDefaultGUI(String[] args) {
		AgentUI ret = super.makeDefaultGUI(args);
		if (ret instanceof AgentInternalFrame) {
			((AgentInternalFrame)ret).resetFromPersistentData();
		}
		return ret;
	}

	/**
   * Create the default internal Frame (usually) with tabs for this agent type. 
   * @param agent the owner agent
   * @param title the title of the window
   * @param aFrame the owner frame in which this window is to be embedded
   * @return the frame
   */
	@Override
	protected TransientAgentInternalFrame makeDefaultInternalFrame(TransientAgent agent, String title, Container aFrame) {
	 	return new AgentInternalFrame((Agent)agent, title, aFrame);
	}
	
} // class Agent

