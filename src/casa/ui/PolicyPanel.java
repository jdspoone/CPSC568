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
package casa.ui;

import casa.TransientAgent;
import casa.policy.Policy;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * A tab panel to display an agent's Policies (regular, always-apply, and/or last-resort). 
 * Options (regular/always-apply/last-resort; detailed/brief) are selected with a popup menu.
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
@SuppressWarnings("serial")
public class PolicyPanel extends JPanel {

	protected TransientAgent agent;
	protected JTextPane textPane;
	protected Container frame;
	private JPopupMenu popup;
	private JCheckBoxMenuItem standardMenuItem;
	private JCheckBoxMenuItem alwaysApplyMenuItem;
	private JCheckBoxMenuItem lastResortMenuItem;
	private JCheckBoxMenuItem detailedMenuItem;
	private JMenuItem refreshMenuItem;
	/** used to specify weather the window should display standard policies */
	boolean standard = true;
	/** used to specify weather the window should display always-apply policies */
	boolean alwaysApply = true;
	/** used to specify weather the window should display last-resort policies */
	boolean lastResort = true;
	/** used to set the detail level of the conversation listing */
	boolean detailed = false;


	/**
	 */
	public PolicyPanel(TransientAgent agent, Container frame) {
		textPane = new JTextPane();
		this.agent = agent;
		this.frame = frame;
		setName("Policies");

		setLayout(new BorderLayout());
		// final String prompt = new String("$-");

		//final TracePanel tracePanel = new TracePanel();
		textPane.setMinimumSize(new Dimension(10, 4));
		textPane.setAutoscrolls(true);
		textPane.setFocusable(true);
		//tracePanel.addTrace(agent);

		JScrollPane msgScrolltemp = new JScrollPane(textPane);

		add( msgScrolltemp, BorderLayout.CENTER);
		setMinimumSize(new Dimension(20, 24));

		makePopupMenu(textPane);
		textPane.addAncestorListener(new AncestorListener(){
			@Override public void ancestorAdded ( AncestorEvent event ) {
				if (event.getComponent().isValid())
					reset();
			}
			@Override public void ancestorRemoved ( AncestorEvent event ) {
				textPane.setDocument(textPane.getEditorKit().createDefaultDocument());
				}
			@Override public void ancestorMoved ( AncestorEvent event ) {}
		});

		textPane.setContentType("text/plain");
	}

	protected void reset() {
		StringBuilder buf = new StringBuilder();
		if (standard) {
			buf.append(";STANDARD POLICIES: \n")
			.append(policiesToString(agent.getPolicies()));
		}
		if (alwaysApply) {
			buf.append(";ALWAYS-APPLY POLICIES: \n")
			.append(policiesToString(agent.getAlwaysApplyPolicies()));
		}
		if (lastResort) {
			buf.append(";LAST-RESORT POLICIES: \n")
			.append(policiesToString(agent.getLastResortPolicies()));
		}
		Document doc = textPane.getEditorKit().createDefaultDocument();
		try {
			doc.insertString(0, buf.toString(), null);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		textPane.setDocument(doc);
	}
	
	private String policiesToString(Policy[] policies) {
		StringBuilder buf = new StringBuilder();
		if (policies!=null) {
			for (Policy p: policies) {
				if (detailed) {
					buf.append(p.toString(2)).append('\n');
				}
				else {
					buf.append("  ").append(p.getName(false)).append(":  ").append(p.getDoc()).append('\n');
				}
			}
		}
		if (buf.length()==0)
			buf.append("  *** No policies to display. ***\n");
		return buf.toString();
	}

	protected void makePopupMenu(JEditorPane pane) {
		//Add listener to components that can bring up popup menus.
		PopupListener popupListener = new PopupListener();
		pane.addMouseListener(popupListener);
		//Create the popup menu.
		popup = new JPopupMenu();
		
		refreshMenuItem = new JMenuItem("refresh");
		refreshMenuItem.addItemListener(popupListener);
		popup.add(refreshMenuItem);

		popup.addSeparator();
		
		standardMenuItem = new JCheckBoxMenuItem("show standard policies", standard);
		standardMenuItem.addItemListener(popupListener);
		popup.add(standardMenuItem);
		alwaysApplyMenuItem = new JCheckBoxMenuItem("show always-apply policies", !standard);
		alwaysApplyMenuItem.addItemListener(popupListener);
		popup.add(alwaysApplyMenuItem);
		lastResortMenuItem = new JCheckBoxMenuItem("show last-resort policies", !standard);
		lastResortMenuItem.addItemListener(popupListener);
		popup.add(lastResortMenuItem);

		popup.addSeparator();
		
		detailedMenuItem = new JCheckBoxMenuItem("detailed", detailed);
		detailedMenuItem.addItemListener(popupListener);
		popup.add(detailedMenuItem);

	}
	class PopupListener extends MouseAdapter implements ItemListener, ActionListener {
		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popup.show(e.getComponent(),
						e.getX(), e.getY());
			}
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			Object source = e.getItemSelectable();
			if (source == standardMenuItem) {
				standard = standardMenuItem.isSelected();
				reset();
			} 
			else if (source == alwaysApplyMenuItem) {
				alwaysApply = alwaysApplyMenuItem.isSelected();
				reset();
			} 
			else if (source == lastResortMenuItem) {
				lastResort = lastResortMenuItem.isSelected();
				reset();
			} 
			else if (source == detailedMenuItem) {
				detailed = detailedMenuItem.isSelected();
				reset();
			} 

		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if (source == refreshMenuItem) {
				reset();
			} 
		}

	}

}
