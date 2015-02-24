package org.ksg.casa;

import static org.junit.Assert.assertEquals;

import casa.ui.AbstractInternalFrame;
import casa.util.CASAUtil;
import casa.util.Trace;

import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This class is teh 
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class CASA {
	
	static {
		casa.abcl.Lisp.loadClass("casa.abcl.Lisp");
	}
	
	static class SilentPreferences { //needed for Windows :(
		Preferences prefs;
		public SilentPreferences(Preferences prefs) {
			this.prefs = prefs;
		}

		public void sync() throws BackingStoreException {
			casa.util.SysoutToBitBucket bb = new casa.util.SysoutToBitBucket();
			try {
				prefs.sync();
			}
			finally {
				bb.finish();
			}
		}

		public void put(String key, String value) {
			casa.util.SysoutToBitBucket bb = new casa.util.SysoutToBitBucket();
			try {
				prefs.put(key, value);
			}
			finally {
				bb.finish();
			}
		}

		public String get(String key, String def) {
			casa.util.SysoutToBitBucket bb = new casa.util.SysoutToBitBucket();
			String ret;
			try {
				ret = prefs.get(key, def); 
			}
			finally {
				bb.finish();
			}
			return ret;
		}

		public void remove(String key) {
			casa.util.SysoutToBitBucket bb = new casa.util.SysoutToBitBucket();
			try {
				prefs.remove(key);
			}
			finally {
				bb.finish();
			}
		}

		public Preferences node(String pathName) {
			casa.util.SysoutToBitBucket bb = new casa.util.SysoutToBitBucket();
			Preferences ret;
			try {
				ret = prefs.node(pathName);
			}
			finally {
				bb.finish();
			}
			return ret;
		}

		public boolean nodeExists(String pathName) throws BackingStoreException {
			casa.util.SysoutToBitBucket bb = new casa.util.SysoutToBitBucket();
			boolean ret;
			try {
				ret = prefs.nodeExists(pathName);
			}
			finally {
				bb.finish();
			}
			return ret;
		}

		public void removeNode() throws BackingStoreException {
			casa.util.SysoutToBitBucket bb = new casa.util.SysoutToBitBucket();
			try {
				prefs.removeNode();
			}
			finally {
				bb.finish();
			}
		}

		public String toString() {
			return prefs.toString();
		}
	}

	private static final SilentPreferences userPreferences;
	private static final SilentPreferences systemPreferences;
	
	static {
		casa.util.SysoutToBitBucket bb = new casa.util.SysoutToBitBucket();
		try {
			userPreferences = new SilentPreferences(Preferences.userNodeForPackage(CASA.class));
			systemPreferences = new SilentPreferences(Preferences.systemNodeForPackage(CASA.class));
		}
		finally {
			bb.finish();
		}
	}

  public CASA() { //dummy constructor for JUnit
  }
  
  public static int ENVIRONMENT=0x1, USER=0x2, SYSTEM=0x4; //, SAVE=0x8, REMOVE=0x10;
  /**
   * Set this to false if you want to attempt to write appropriate data to the System preferences,
   * otherwise (if true) this will not bother to attempt to write to System preferences.  Most
   * systems will no longer let a non-root user to write to System preferences. 
   */
  private static boolean systemPrefsFailureLogged = true; 
  
  static {
  	writeDefaultPrefs();
  }
  
  /**
   * Write out the default preferences to both the user and system preferences.
   * Note that on some systems this might not work, for example in Mac OS/X Lion
   * one cannot write the system prefs unless you are running under the root
   * account.
   */
  public static void writeDefaultPrefs() {
  	putPreferenceIfAbsent("LACdefaultport", 9000, USER|SYSTEM);
  	putPreferenceIfAbsent("knownTraceTags", "error,warning", USER|SYSTEM);
  	putPreferenceIfAbsent("synonymURLs", "", USER);
  }
  
  public static boolean isSystemPrefsWorking(boolean verbose) {
    try {
			CASA.flush(SYSTEM);
		} catch (IllegalAccessError e) {
			if (verbose)
				CASAUtil.log("error", "CASA.isSystemPrefsWorking", e, true);
			return false;
		}
    return true;
  }
  
  /**
   * Reads preferences from one of:
   * <ol>
   * <li> System environment variables
   * <li> System user preferences
   * <li> System system preferences
   * </ol>
   * in that order. Detailed control is specified by the function integer, which is the disjunct of the
   * flags {@link #ENVIRONMENT}, {@link #USER}, {@link #SYSTEM}, {@link #SAVE}, and {@link #REMOVE}.  The first 
   * three flags mean we should attempt to read from that source (in that order).  The {@link #SAVE}
   * flag indicates that, if we don't find the value in that source (or some error occurs), we
   * should save the given <em>defaultValue</em> to that source (but only the first one, in order). However,
   * this method will never overwrite a non-default value in the preferences store (although it can be
   * used to remove an preference).
   * Note that you cannot save a value to an environment variable, only to preferences. {@link #REMOVE}
   * behaves similarly, but removes the value from that source.  
   * <p>There are some special flag words: 
   * <ul>
   * <li> 0 is taken the same as ENVIRONMENT|USER|SYSTEM (try all sources and don't save).
   * <li> SAVE (only) will save to the user preferences.
   * <li> REMOVE (only) will remove the user preference.
   * </ul>
   * If {@link #SAVE} and {@link #REMOVE} are used together, {@link #SAVE} will override.
   * Note that to remove or save specifically either a user or system preference you should use only
   * USER|REMOVE or only SYSTEM|REMOVE (or SAVE, as appropriate) -- this method works on only the
   * first priority element found. <p>
   * In some systems, the system preferences may not work, in which case, this method
   * silently falls back to using the user preferences instead (although, this fact was probably
   * logged already by {@link #writeDefaultPrefs()}.
   * @param prefName The name of the preference variable
   * @param defaultValue The default value
   * @param operation The flag word, which should be and disjuction (|) of one or more of
   * {@link #ENVIRONMENT}, {@link #USER}, {@link #SYSTEM}, {@link #SAVE} {@link #REMOVE}. 
   * @return The value found, or the <em>defaultValue</em> if a value is not found.
   */
  public static String getPreference(String prefName, String defaultValue, int operation) {
  	operation &= ENVIRONMENT|USER|SYSTEM; //get rid of any extra bits
  	if (operation==0) 
  		operation = ENVIRONMENT|USER|SYSTEM; //default to looking everywhere
  	
  	// check for environment variable 
  	if ((operation&ENVIRONMENT)!=0) {
  	  String envVar = System.getenv("CASA_"+prefName);
  	  if (envVar!=null) {
  	  	return envVar;
  	  }
  	}
  	//check for a user preference
    if ((operation&USER)!=0) {
    	String ret=userPreferences.get(prefName, defaultValue);
    	if (ret!=defaultValue) return ret;
    }
    //check for a system preference
    if ((operation&SYSTEM)!=0) {
    	String ret=systemPreferences.get(prefName, defaultValue);
      if (ret!=defaultValue) return ret;
    }    
    return defaultValue; // we should actually never get to this line.
  }

  public static Map<String,String> getPreference(String prefName, Map<String,String> defaultValue, int operation) {
  	assert prefName!=null;
  	operation &= ENVIRONMENT|USER|SYSTEM; //get rid of any extra bits
  	if (operation==0) 
  		operation = ENVIRONMENT|USER|SYSTEM; //default to looking everywhere

  	// check for environment variable 
  	if ((operation&ENVIRONMENT)!=0) {
  		String envVar = System.getenv("CASA_"+prefName);
  		if (envVar!=null) {
				Map<String,String> ret = new TreeMap<String,String>();
  			String[] pairs = envVar.split(":");
  			for (String pair: pairs) {
  				String[] tokens = pair.split("=",2);
  				ret.put(tokens[0], tokens.length>1?tokens[1]:null);
  			}
  			return ret;
  		}
  	}
  	Preferences map = null;
  	//check for a user preference
  	if ((operation&USER)!=0) {
  		try {
				if (userPreferences.nodeExists(prefName)) {
					map=userPreferences.node(prefName);
				}
			} catch (BackingStoreException e) {
				Trace.log("error", "CASA.getPreference()", e);
			}
  	}
  	//check for a system preference
  	if (map==null && (operation&SYSTEM)!=0) {
  		try {
				if (systemPreferences.nodeExists(prefName)) {
					map=userPreferences.node(prefName);
				}
			} catch (BackingStoreException e) {
				Trace.log("error", "CASA.getPreference()", e);
			}
  	}
  	if (map!=null) {
  		try {
				Map<String,String> ret = new TreeMap<String,String>();
				for (String key: map.keys()) {
					ret.put(key, map.get(key, null));
				}
				return ret;
			} catch (BackingStoreException e) {
				Trace.log("error", "CASA.getPreference()", e);
			}
  	}
  	return defaultValue; // we should actually never get to this line.
  }

  public static void putPreference(String prefName, int defaultValue, int operation) {
  	putPreference(prefName, Integer.toString(defaultValue), operation);
  }

  public static void putPreference(String prefName, boolean defaultValue, int operation) {
  	putPreference(prefName, Boolean.toString(defaultValue), operation);
  }

  public static void putPreference(String prefName, String defaultValue, int operation) {
  	boolean systemWorking = false;
  	if (!systemPrefsFailureLogged)
    	systemWorking = isSystemPrefsWorking(false);
  		if (!systemWorking && (operation&SYSTEM)!=0) {
  		Trace.logToSysout("warning", "System preferences are not saving persistently, using user preferences instead.  This is a known problem with OS/X Lion (unless you are root), but may also be a system configuation problem.", null, 0);
  		systemPrefsFailureLogged = true;
  	}

  	operation &= USER|SYSTEM; //get rid of any extra bits
  	if (operation==0) 
  		operation = USER; //default to USER only
  	
  	//check for a user preference
    if ((operation&USER)!=0) {
    	userPreferences.put(prefName, defaultValue);
    }
    //check for a system preference
    if ((operation&SYSTEM)!=0) {
    	systemPreferences.put(prefName, defaultValue);
    }    
  }
  
  public static void putPreference(String prefName, Map<String,?> defaultValue, int operation) {
  	assert defaultValue!=null;
  	assert prefName!=null;
  	boolean systemWorking = false;
  	if (!systemPrefsFailureLogged)
    	systemWorking = isSystemPrefsWorking(false);
  		if (!systemWorking && (operation&SYSTEM)!=0) {
  		Trace.log("warning", "System preferences are not saving persistently, using user preferences instead.  This is a known problem with OS/X Lion (unless you are root), but may also be a system configuation problem.");
  		systemPrefsFailureLogged = true;
  	}

  	operation &= USER|SYSTEM; //get rid of any extra bits
  	if (operation==0) 
  		operation = USER; //default to USER only

  	Preferences map = null;
  	//check for a user preference
    if ((operation&USER)!=0) {
    	map = userPreferences.node(prefName);
//    	userPreferences.put(prefName, defaultValue);
    }
    //check for a system preference
    if ((operation&SYSTEM)!=0) {
    	map = systemPreferences.node(prefName);
//    	systemPreferences.put(prefName, defaultValue);
    }    
    assert map!=null;
    for (String key: defaultValue.keySet()) {
    	map.put(key, defaultValue.get(key).toString());
    }
  }
  
  public static void putPreferenceIfAbsent(String prefName, int defaultValue, int operation) {
  	putPreferenceIfAbsent(prefName, Integer.toString(defaultValue), operation);
  }

  public static void putPreferenceIfAbsent(String prefName, String defaultValue, int operation) {
  	boolean systemWorking = isSystemPrefsWorking(false);
  	if (!systemPrefsFailureLogged && !systemWorking && (operation&SYSTEM)!=0) {
  		Trace.logToSysout("warning", "System preferences are not saving persistently, using user preferences instead.  This is a known problem with OS/X Lion (unless you are root), but may also be a system configuation problem.", null, 0);
  		systemPrefsFailureLogged = true;
  	}

  	operation &= USER|SYSTEM; //get rid of any extra bits
  	if (operation==0) 
  		operation = USER; //default to USER only
  	
  	//check for a user preference
    if ((operation&USER)!=0) {
    	if (!isPrefPresent(userPreferences, prefName))
    	  userPreferences.put(prefName, defaultValue);
    }
    //check for a system preference
    if ((operation&SYSTEM)!=0) {
    	if (!isPrefPresent(systemPreferences, prefName))
    	  systemPreferences.put(prefName, defaultValue);
    }    
  }
  
  private static boolean isPrefPresent(SilentPreferences p, String name) {
  	if (!"1".equals(p.get(name, "1"))) return true;
  	if (!"2".equals(p.get(name, "2"))) return true;
  	return false;
  }
  
  public static void removePreference(String prefName, int operation) {
  	operation &= USER|SYSTEM; //get rid of any extra bits
  	if (operation==0) 
  		operation = USER|SYSTEM; //default to looking everywhere, but not saving
  	
  	//check for a user preference
    if ((operation&USER)!=0) {
    	userPreferences.remove(prefName);
    }
    //check for a system preference
    if ((operation&SYSTEM)!=0) {
    	systemPreferences.remove(prefName);
    }    
  }
  
  /**
   * Specialized int version of {@link #getPreference(String, String, int)}.
   * @param prefName The name of the preference variable
   * @param defaultValue The default value
   * @param function The flag word, which should be and disjuction (|) of one or more of
   * {@link #ENVIRONMENT}, {@link #USER}, {@link #SYSTEM}, {@link #SAVE} {@link #REMOVE}. 
   * @return The value found, or the <em>defaultValue</em> if a value is not found.
   * @see #getPreference(String, String, int)
   */
  public static int getPreference(String prefName, int defaultValue, int function) {
  	return Integer.parseInt(getPreference(prefName, Integer.toString(defaultValue), function));
  }

  /**
   * Specialized boolean version of {@link #getPreference(String, String, int)}.
   * @param prefName The name of the preference variable
   * @param defaultValue The default value
   * @param function The flag word, which should be and disjuction (|) of one or more of
   * {@link #ENVIRONMENT}, {@link #USER}, {@link #SYSTEM}, {@link #SAVE} {@link #REMOVE}. 
   * @return The value found, or the <em>defaultValue</em> if a value is not found.
   * @see #getPreference(String, String, int)
   */
  public static boolean getPreference(String prefName, boolean defaultValue, int function) {
  	return Boolean.parseBoolean(getPreference(prefName, Boolean.toString(defaultValue), function));
  }

  /**
   * Specialized long version of {@link #getPreference(String, String, int)}.
   * @param prefName The name of the preference variable
   * @param defaultValue The default value
   * @param function The flag word, which should be and disjuction (|) of one or more of
   * {@link #ENVIRONMENT}, {@link #USER}, {@link #SYSTEM}, {@link #SAVE} {@link #REMOVE}. 
   * @return The value found, or the <em>defaultValue</em> if a value is not found.
   * @see #getPreference(String, String, int)
   */
  public static long getPreference(String prefName, long defaultValue, int function) {
  	return Long.parseLong(getPreference(prefName, Long.toString(defaultValue), function));
  }

  @Override
	protected void finalize() throws Throwable {
    flush(0);
  	super.finalize();
	}

  /**
   * Flushes user and/or system preferences to make them persistent.
   * @param operation if the USER bit is set flushes user preferences, if the SYSTEM
   * bit is set flushes system preferences, if neither or both are set then both are flushed.
   */
  public static void flush(int operation) throws IllegalAccessError {
  	if ((operation&(USER|SYSTEM))==0) operation = (USER|SYSTEM); 
  	if ((operation&USER)!=0) {
  		try {
  			userPreferences.sync();
  		} catch (Throwable e1) {
  			IllegalAccessError e = new IllegalAccessError("Can't write user preferences: "+e1);
  			e.fillInStackTrace();
  			throw e;
  		}
  	}
  	if ((operation&SYSTEM)!=0) {
  		try {
  			systemPreferences.sync();
  		} catch (Throwable e1) {
  			IllegalAccessError e = new IllegalAccessError("Can't write system preferences: "+e1);
  			e.fillInStackTrace();
  			throw e;
  		}
  	}
  }
  /* *************JUnit tests **********************************************/
  
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		removePreference("test", USER);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		removePreference("test", USER);
	}

  @Test
  public final void testPrefs() {
  	assertEquals(9, getPreference("test", 9, 0));
  	assertEquals(8, getPreference("test", 8, USER));
  	putPreferenceIfAbsent("test", 8, USER);
  	assertEquals(8, getPreference("test", 7, USER));
  	putPreferenceIfAbsent("test", 12, USER);
  	assertEquals(8, getPreference("test", 12, USER));
  	assertEquals(10, getPreference("test", 10, ENVIRONMENT));
  	assertEquals(11, getPreference("test", 11, ENVIRONMENT|SYSTEM));
  }
  
  /* ****************************************************************************************
   *  Accessors for CASAPreferencesDialog
   ******************************************************************************************/
  private static String getPref(String name, String default_) {
  	return getPreference(name, default_, USER|SYSTEM);
  }
  private static int getPref(String name, int default_) {
  	return getPreference(name, default_, USER|SYSTEM);
  }
  private static boolean getPref(String name, boolean default_) {
  	return getPreference(name, default_, USER|SYSTEM);
  }
  
  private static void setPref(String name, String default_) {
  	putPreference(name, default_, USER|SYSTEM);
  }
  private static void setPref(String name, int default_) {
  	putPreference(name, default_, USER|SYSTEM);
  }
  private static void setPref(String name, boolean default_) {
  	putPreference(name, default_, USER|SYSTEM);
  }
  
	/* dummy declarations for WindowBuilder */
//  public int LACdefaultport = 9000;// USER|SYSTEM
//  public String putPreferenceIfAbsent = "error,warning"; //SYSTEM
//  public String synonymURLs = ""; // USER
//  public String root = "";
//  public boolean dieOnLACExitDefault = false;

	/**
	 * @return getPref("the lACdefaultport
	 */
	public static int getLACdefaultport() {
		return getPref("LACdefaultport", 9000);
	}

	/**
	 * @param lACdefaultport the lACdefaultport to set
	 */
	public static void setLACdefaultport(int lACdefaultport) {
		setPref("LACdefaultport", lACdefaultport);
	}

	/**
	 * @return getPref("the putPreferenceIfAbsent
	 */
	public static String getPutPreferenceIfAbsent() {
		return getPref("putPreferenceIfAbsent", "error,warning");
	}

	/**
	 * @param putPreferenceIfAbsent the putPreferenceIfAbsent to set
	 */
	public static void setPutPreferenceIfAbsent(String putPreferenceIfAbsent) {
		setPref("PreferenceIfAbsent", putPreferenceIfAbsent);
	}

	/**
	 * @return getPref("the synonymURLs
	 */
	public static String getRouter() {
		return getPref("router", "");
	}

	/**
	 * @param synonymURLs the synonymURLs to set
	 */
	public static void setRouter
	(String router) {
		setPref("router", router);
	}

	/**
	 * @return getPref("the root
	 */
	public static String getRoot() {
		return getPref("root", System.getProperty("user.name")+"/.casa");
	}

	/**
	 * @param root the root to set
	 */
	public static void setRoot(String root) {
		setPref("root", root);
	}

	/**
	 * @return getPref("the dieOnLACExitDefault
	 */
	public static boolean isDieOnLACExitDefault() {
		return getPref("dieOnLACExitDefault", false);
	}

	/**
	 * @param dieOnLACExitDefault the dieOnLACExitDefault to set
	 */
	public static void setDieOnLACExitDefault(boolean dieOnLACExitDefault) {
		setPref("dieOnLACExitDefault", dieOnLACExitDefault);
	}

  static public String getBuildInfo() {
  	StringBuilder b = new StringBuilder("CASA (Collaborative Agent System Archicture)\n");
  	b.append("Knowledge Science Group, Department of Computer Science\n")
  	 .append("University of Calgary\n")
  	 .append("Version:    0.9\n")
  	 .append("Build time: ").append(AbstractInternalFrame.getBuildTime()).append('\n')
  	 .append("Process ID: ").append(CASAUtil.getPID()).append('\n')
  	 .append("CASA makes use of the following open source projects:\n")
  	 .append("  Lisp interpreter: Armed Bear Common Lisp, http://abcl.org\n")
  	 .append("  Knowledge base: JADE, http://jade.tilab.com, and its Semantic Extension, http://www.francetelecom.com)\n")
  	 .append("  XML Message format: JDOM, http://www.jdom.org; and SAX, http://www.saxproject.org\n")
  	 .append("  OWL2 ontology engine: OWL API, The University of Manchester, http://owlapi.sourceforge.net\n")
  	 .append("  plistreader: Gie Spaepen, University of Antwerp\n");
  	return b.toString();
  }
}
