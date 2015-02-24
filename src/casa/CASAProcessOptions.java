package casa;

import casa.CASAProcess.ProcessInfo;
import casa.agentCom.URLDescriptor;
import casa.exceptions.URLDescriptorException;
import casa.io.CASAFilePropertiesMap;

import java.util.TreeSet;

import org.ksg.casa.CASA;

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

public class CASAProcessOptions extends AgentOptions {
	@CasaPersistent
	@CasaOption(
			labelText = "Incomming synonym URLs", 
			helpText = "This is a semi-colon separated lists of n.n.n.n:port TCP specs that to be considered synonyms for the agent's.", 
			validationMethod = "validateSynonymUrls",
	    postSaveMethod = "synonymURLsPost")
	public static String	synonymURLs				= CASA.getPreference("synonymURLs", "", CASA.USER);

	public void validateSynonymUrls(String obj) {
		String s[] = obj.split(";");
		for (String u: s) {
			u = u.trim();
			if (u.length()>0) {
				validateUrl(u);
			}
		}
		saveSynonymURLsToAgent();
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

	public void synonymURLsPost() {
		CASA.putPreference("synonymURLs", synonymURLs, CASA.USER);
	}
	
	private void saveSynonymURLsToAgent() {
		String s[] = synonymURLs.split(";");
		TreeSet<URLDescriptor> set = new TreeSet<URLDescriptor>();
		for (String u: s) {
			u = u.trim();
			if (u.length()>0) {
				try {
					set.add(URLDescriptor.make(u));
				} catch (URLDescriptorException e) {
					getAgent().println("error", "CASAProcessOptions.saveSynnymURLs()", e);
				}
			}
		}
		ProcessInfo.synonymsURLs = set;
	}

	public CASAProcessOptions(AbstractProcess agent) {
		super(agent);
		saveSynonymURLsToAgent();
	}

	@Override
	public void write(CASAFilePropertiesMap m) {
		if (m!=null) {
		super.write(m);
		}
	}

	@Override
	public void read(CASAFilePropertiesMap m) {
		super.read(m);
	}
}