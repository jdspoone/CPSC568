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
package casa.platform;

import casa.util.CASAUtil;

import java.awt.Image;
import java.lang.reflect.Constructor;
import java.util.List;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class Generic {

	/**
	 * 
	 */
	public Generic() {
		
	}

	protected static boolean	MAC_OS_X	= (System.getProperty("os.name")
			.toLowerCase()
			.startsWith("mac os x"));
	protected static boolean WINDOWS = (System.getProperty("os.name")
			.toLowerCase()
			.contains("win"));

  protected static Generic singleton	= getSingleton();
  
  private static Generic getSingleton() {
  	String typeName;
  	if (MAC_OS_X)
  		typeName = "OsX";
  	else if (WINDOWS)
  		typeName = "Windows";
  	else
  		typeName = "Generic";
  	
  	Generic ret;
		try {
			@SuppressWarnings("unchecked")
			Class<? extends Generic> type = (Class<? extends Generic>) Class.forName("casa.platform."+typeName);
			Constructor<? extends Generic> cons = type.getDeclaredConstructor((Class<?>[])null);
			ret = cons.newInstance((Object[])null);
			return ret;
		} catch (Throwable e) {
			CASAUtil.log("error", "casa.platforms.Generic.getSinglton: failed to instantiate OS-specific class for OS "+typeName, e, false);
		}   	
		return new Generic();
  }
  
	/**
	 * Adds a quit event handler
	 * 
	 * @param quitHandler to add
	 */
	public static QuitHandler addQuitHandler(QuitHandler quitHandler) {
		return singleton.addQuitHandler2(quitHandler);
	}
	
	protected QuitHandler addQuitHandler2(QuitHandler quitHandler) {
		return null;
	}

	// INTERFACES

	/**
	 * <code>OsX.QuitHandler.HandleQuit</code> is invoked if the interface
	 * has been added via <code>OsX.addQuitHandler</code>.
	 */
	public interface QuitHandler {
		/**
		 * Method invoked on the AppleEvent "quit" (when this
		 * <code>QuitHandler</code> has been added as a quit handler). 
		 */
		public void handleQuit();
	}
	
	static public Image setDocIconImage(Image img) {
		return singleton.setDocIconImage2(img);
	}

	public Image setDocIconImage2(Image img) {
		return null;
	}
	
	/**
	 * @param command
	 * @return A command string appropriate to the O/S (On Windows, one must backslash quotes).
	 */
	static public String prepareCmdString(String command) {
		return singleton.prepareCmdString2(command);
	}

	public String prepareCmdString2(String command) {
		return command;
	}

	/**
	 * @param command
	 * @return A command List appropriate to the O/S (On Windows, one must backslash quotes).
	 */
	static public List<String> prepareCmdList(List<String> command) {
		return singleton.prepareCmdList2(command);
	}

	public List<String> prepareCmdList2(List<String> command) {
		return command;
	}
}
