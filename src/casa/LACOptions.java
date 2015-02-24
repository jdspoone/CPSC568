package casa;

import casa.agentCom.URLDescriptor;
import casa.exceptions.URLDescriptorException;
import casa.io.CASAFilePropertiesMap;
import casa.util.CASAUtil;
import casa.util.PropertyException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.UIManager;

/**
 * <p>
 * Copyright: Copyright 2003-2014, Knowledge Science Group, University of
 * Calgary. Permission to use, copy, modify, distribute and sell this software
 * and its documentation for any purpose is hereby granted without fee, provided
 * that the above copyright notice appear in all copies and that both that
 * copyright notice and this permission notice appear in supporting
 * documentation. The Knowledge Science Group makes no representations about the
 * suitability of this software for any purpose. It is provided "as is" without
 * express or implied warranty.
 * </p>
 * 
 * @author <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public class LACOptions extends CASAProcessOptions {
	@CasaPersistent
	@CasaOption(
			labelText = "Alias URL", 
			helpText = "URL to send remote IP message through (only host and port are used). For firewall tunneling.  Leave this blank if no firewall.", 
			validationMethod = "validateUrl")
	public String	alias				= null;
	@CasaPersistent
	@CasaOption(
			labelText = "indirect (route nonlocal  messages through LAC for firewall tunnelling, etc.)")
	public boolean	indirect			= false;
	@CasaPersistent
	@CasaOption(
			labelText = "Create proxy windows for external agents", 
			helpText = "Create proxy (shadow) windows for agents running in a different process (but still within the Area)")
	public boolean	createProxyWindows	= false;
	@CasaPersistent
	@CasaOption(
			labelText = "Show inactive agents")
	public boolean	showInactiveAgents	= true;
	@CasaPersistent
	@CasaOption(
			labelText = "Look and Feel", 
			optionsMethod = "lookAndFeels", 
			postSaveMethod = "lookAndFeelPost")
	public String	lookAndFeel			= null;

	public static Collection<String> lookAndFeels() {
		Set<String> lafs = new HashSet<String>();
		for (UIManager.LookAndFeelInfo installedLookAndFeel : UIManager
				.getInstalledLookAndFeels())
			lafs.add(installedLookAndFeel.getClassName());
		return lafs;
	}

	public void lookAndFeelPost() {
		try {
			UIManager.setLookAndFeel(lookAndFeel);
		} catch (Exception e) {
			CASAUtil.log("error", "LACOptions.lookAndFeelPost()", e, true);
		}
	}

	public void validateUrl(String obj) {
		if (String.class.isInstance(obj)) {
			String s = (String)obj;
			try {
				if(s!=null && s.length()>0)
					URLDescriptor.make(s);
			} catch (URLDescriptorException ex) {
				throw new IllegalArgumentException(String.format("Received %s; Excpected a valid URL in the form 'hostIPAddress:Port', e.g. 68.147.201.116:9000",s));
			}
		} else
			throw new IllegalArgumentException(String.format("Received: %s; Expected a string", obj));
	}

	public LACOptions(AbstractProcess agent) {
		super(agent);
	}

	public void write(CASAFilePropertiesMap m) {
		if (m!=null) {
			super.write(m);
			if (alias == null || alias.length() == 0)
				m.removeProperty("options.alias");
			else
				m.setString("options.alias", alias);
			m.setBoolean("options.indirect", indirect);
			m.setBoolean("options.createProxyWindows", createProxyWindows);
			m.setBoolean("options.showInactiveAgents", showInactiveAgents);
			m.setString("options.lookAndFeel", lookAndFeel);
		}
	}

	public void read(CASAFilePropertiesMap m) {
		super.read(m);
		try {
			alias = m.getString("options.alias");
		} catch (PropertyException ex3) {
		}
		try {
			indirect = m.getBoolean("options.indirect");
		} catch (PropertyException ex3) {
		}
		try {
			createProxyWindows = m.getBoolean("options.createProxyWindows");
		} catch (PropertyException ex3) {
		}
		try {
			showInactiveAgents = m.getBoolean("options.showInactiveAgents");
		} catch (PropertyException ex3) {
		}
		try {
			lookAndFeel = m.getString("options.lookAndFeel");
		} catch (PropertyException ex3) {
		}
	}
}