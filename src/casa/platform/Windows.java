package casa.platform;

import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
public class Windows extends Generic {
	static {
		new Windows();
	}

	/**
	 * private because singleton
	 */
	protected Windows() {
	}

	/**
	 * @param command
	 * @return A command string appropriate to the O/S (On Windows, one must backslash quotes).
	 */
	@Override
	public String prepareCmdString2(String command) {
		return command.replace("\"", "\\\"");
	}

	/**
	 * @param command
	 * @return A command string appropriate to the O/S (On Windows, one must backslash quotes).
	 */
	@Override
	public List<String> prepareCmdList2(List<String> command) {
		List<String> ret = new LinkedList<String>();
		for (String s: command)
			ret.add(s.replace("\"", "\\\""));
		return ret;
	}


	/**
	 * Indicates whether current platform is OS X
	 * 
	 * @return whether current platform is OS X
	 */
	public static final boolean isWindows() {
		return WINDOWS;
	}
	
}
