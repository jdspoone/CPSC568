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
import casa.conversation2.Conversation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * A tab panel to display Conversations (current or known). 
 * Options (current/known; detailed/brief) are selected with a popup menu.
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
@SuppressWarnings("serial")
public class ConversationPanel extends JPanel {

	protected TransientAgent agent;
	protected JTextPane textPane;
	protected Container frame;
	private JPopupMenu popup;
	private JCheckBoxMenuItem currentConversationsMenuItem;
	private JCheckBoxMenuItem knownConversationsMenuItem;
	private JCheckBoxMenuItem detailedConversationsMenuItem;
	/** used to specify weather the window should display current or known conversations */
	boolean current = true;
	/** used to set the detail level of the conversation listing */
	boolean detailed = false;
	private JMenuItem refreshMenuItem;


	/**
	 */
	public ConversationPanel(TransientAgent agent, Container frame) {
		textPane = new JTextPane();
		this.agent = agent;
		this.frame = frame;
		setName("Conversations");

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
		Document doc = textPane.getEditorKit().createDefaultDocument();
		try {
			doc.insertString(0, agent.getConversationsReport(current, detailed?2:0).getSecond(), null);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		textPane.setDocument(doc);
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
		
		currentConversationsMenuItem = new JCheckBoxMenuItem("current conversations", current);
		currentConversationsMenuItem.addItemListener(popupListener);
		popup.add(currentConversationsMenuItem);
		knownConversationsMenuItem = new JCheckBoxMenuItem("known conversations", !current);
		knownConversationsMenuItem.addItemListener(popupListener);
		popup.add(knownConversationsMenuItem);

		popup.addSeparator();
		
		detailedConversationsMenuItem = new JCheckBoxMenuItem("detailed", detailed);
		detailedConversationsMenuItem.addItemListener(popupListener);
		popup.add(detailedConversationsMenuItem);

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
			if (source == currentConversationsMenuItem) {
				boolean on = current = currentConversationsMenuItem.isSelected();
				knownConversationsMenuItem.setSelected(!on);
				reset();
			} 
			else if (source == knownConversationsMenuItem) {
				boolean on = knownConversationsMenuItem.isSelected();
				current = !on;
				currentConversationsMenuItem.setSelected(!on);
				reset();
			} 
			else if (source == detailedConversationsMenuItem) {
				detailed = detailedConversationsMenuItem.isSelected();
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
