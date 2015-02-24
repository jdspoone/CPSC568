package casa.system;

public class SocketServerDialog {
	
	private SocketServerDialog() { //non-instantiatable constructor;
		
	}

	public static void main(String[] args) {
		String osName = System.getProperty("os.name");
		if (osName.contains("OS X")) { 
			MacOSsocketServerDialog.main(args);
			return;
		}
			String osArch = System.getProperty("os.arch");
			String osVersion = System.getProperty("os.version");
			String msg = "Socket Server not supported on current system:\n  os.name="+osName+"; os.arch="+osArch+"; os.version="+osVersion;
			System.out.println(msg);
			return;
	}
  
	public static boolean isOSSupported(String OSname) {
		if (OSname.contains("OS X"))
			return true;
		return false;
	}
}
