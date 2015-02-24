/**
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. 
 * Permission to use, copy, modify, distribute and sell this software and its 
 * documentation for any purpose is hereby granted without fee, provided that the 
 * above copyright notice appear in all copies and that both that copyright notice 
 * and this permission notice appear in supporting documentation.  The  Knowledge 
 * Science Group makes no representations about the suitability of  this software 
 * for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
package casa.system;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.swing.JDialog;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class Sudo {
	// bound to the password window
	public String password;

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	private PasswordDialog passwordDialog = null;
	private String passwordMessage = "Please enter admin password to validate operation:";
	/**
	 * @return the passwordMessage
	 */
	public String getPasswordMessage() {
		return passwordMessage;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	public void sudo(String command) {
		if (password==null) {
		try {
			passwordDialog = new PasswordDialog(command);
			passwordDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			passwordDialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		}
		else {
			sudo2(command);
		}
	}

	int sudo2(String command) {
		if (password==null) {
			System.out.println("null password");
			return -1;
		}
		if (passwordDialog!=null) {
		  passwordDialog.dispose();
		  passwordDialog = null;
		}
		int ret = -1;
		try {
			//File curDir = new File(".");
			File tempFile = File.createTempFile("prompter", "sh");//, curDir);
			FileOutputStream f = new FileOutputStream(tempFile);
			tempFile.setReadable(true, false);
			tempFile.setExecutable(true, false);
			String pFileContent = "#!/bin/sh\necho "+password+"\n";
			f.write(pFileContent.getBytes());
			f.close();
			ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", "sudo -A "+command);
			Map<String, String> env = pb.environment();
			env.put("SUDO_ASKPASS", tempFile.getAbsolutePath());
			pb.redirectErrorStream(true);
			Process process = pb.start();
			InputStream processOutput = process.getInputStream();
			//wait for the subprocess to complete
			for (boolean interrupted=true; interrupted; ) {
				interrupted = false;
				try {
					ret = process.waitFor();
				} catch (InterruptedException e) {
					interrupted = true;
				}
			}
			int avail = processOutput.available();
			byte[] b = new byte[avail];
			processOutput.read(b);
			String processOutString = new String(b);
			System.out.println("Execution returned: "+processOutString);
			if (!tempFile.delete())
				System.out.println("Password file didn't get erased!!!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		passwordMessage = "Command failed: "+command+".";
		return ret;
	}


}
