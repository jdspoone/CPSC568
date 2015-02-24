package casa.ui;

import casa.Agent;
import casa.interfaces.AgentInterface;

import java.awt.Container;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

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
 * @author <a href="mailto:rkyee@ucalgary.ca">Ryan Yee</a>
 * 
 */
public class MasvisInternalFrame extends AgentInternalFrame {

	JFrame	frame;

	public MasvisInternalFrame(Agent agent, String title, Container aFrame, final JFrame content) {
		super(agent, title, aFrame);
		addTab("Masvis", content.getRootPane(), true);
		
//		contentPanel.setSelectedTab("Masvis");
		setSelectedTab("Masvis");
		
	}
}
