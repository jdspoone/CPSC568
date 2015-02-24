package casa;

import casa.io.CASAFilePropertiesMap;
import casa.ui.ObjectFieldCache;
import casa.util.PropertyException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

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
 * @see TransientAgent
 * @author <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public class AgentOptions extends ProcessOptions {
	@CasaPersistent
	@CasaOption (
			labelText = "Persistent",
			helpText = "check maintain the agent\'s state for the next invocation",
			actionListenerMethod = "getPersistentActionListener",
			group = "Persistence",
			groupOrder = -1)
	public boolean	persistent;
	@CasaPersistent
	@CasaOption (
			labelText = "Persistent history",
			helpText = "history will only persist beyond the current invocation if \"Persistent\" is checked and history is being recorded.",
			enabledMethod = "isPersistent",
			group = "Persistence")
	public boolean	persistentHistory;
	@CasaPersistent
	@CasaOption (
			labelText = "Persistent Ontology",
			helpText = "ontology will only persist beyond the current invocation if \"Persistent\" is checked and history is being recorded.",
			enabledMethod="isPersistent",
			group = "Persistence")
	public boolean	persistentOntology;

	public AgentOptions(AbstractProcess agent) {
		super(agent);
	}

	@Override
	public void write(CASAFilePropertiesMap m) {
		if (m!=null) {
		super.write(m);
		m.setBoolean("options.persistent", persistent);
		m.setBoolean("options.persistentHistory", persistentHistory);
		m.setBoolean("options.persistentOntology", persistentOntology);
		}
	}

	@Override
	public void read(CASAFilePropertiesMap m) {
		super.read(m);
		try {
			persistent = m.getBoolean("options.persistent");
		} catch (PropertyException ex3) {
		}
		try {
			persistentHistory = m.getBoolean("options.persistentHistory");
		} catch (PropertyException ex3) {
		}
		try {
			persistentOntology = m.getBoolean("options.persistentOntology");
		} catch (PropertyException ex3) {
		}
	}
	
	public boolean isPersistent(){
		return persistent;
	}
	
	public ActionListener getPersistentActionListener(final Collection<ObjectFieldCache> cache){
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean enabled = false;
				for(ObjectFieldCache cash : cache){
					if(cash.getFieldName().equals("persistent"))
						enabled = (Boolean) cash.getGuiValueAsNative();
				}
				for(ObjectFieldCache cash : cache){
					if(cash.getFieldName().equals("persistentHistory") || cash.getFieldName().equals("persistentOntology"))
						cash.getGuiValue().setEnabled(enabled);
				}				
			}
		};
	}
}
