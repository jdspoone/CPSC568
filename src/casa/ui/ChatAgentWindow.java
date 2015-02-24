package casa.ui;

import casa.ML;
import casa.agentCom.URLDescriptor;
import casa.interfaces.ChatAgentInterface;
import casa.testAgents.ChatAgent;
import casa.testAgents.ChatMessage;
import casa.util.CASAUtil;
import casa.util.Tristate;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Observable;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * This class creates a window which is the main user interface to an Agent.
 *<p>
 * Use: java AgentWindow -pPORT# -n"NAME" -lLAC#, where PORT# is the desired
 *      port for the agent to use, NAME is the desired name for the agent, and
 *      LAC# is an optional parameter for the location (port) of a LAC to log
 *      on to.
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary.
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  The
 * Knowledge Science Group makes no representations about the suitability of
 * this software for any purpose.  It is provided "as is" without express or
 * implied warranty.</p>
 *
 * @author <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @author Eunice Lim
 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 * @version 0.9
 */
public class ChatAgentWindow
extends AgentInternalFrame {

	protected JCheckBoxMenuItem menuCD_setRetrieveHistory;
	protected JMenuItem menuCD_MembersSelectAll;
	protected JMenuItem menuCD_MembersSelectNone;

	private static final int buttonSEND = agentFrame_LAST_EVENT + 1;
	private static final int buttonSENDDIRECTED = agentFrame_LAST_EVENT + 2;
	private static final int buttonSENDWHISPER = agentFrame_LAST_EVENT + 3;
	private static final int listSCROLLED = agentFrame_LAST_EVENT + 4;
	private static final int menuSETRETRIEVEHISTORY = agentFrame_LAST_EVENT + 5;
	private static final int menuCDMEMBERSSELECTALL = agentFrame_LAST_EVENT + 6;
	private static final int menuCDMEMBERSSELECTNONE = agentFrame_LAST_EVENT + 7;
	protected static final int chatAgentFrame_LAST_EVENT = agentFrame_LAST_EVENT + 10;

	private JMenuItem menuAddFriend;

	protected JList listMessages;
	protected ChatMessageListModel listMessagesModel;

	protected JButton buttonSendBroadcast;
	protected JButton buttonSendDirected;
	protected JButton buttonSendWhisper;

	protected JTextField textMessage;
	
	static int DEFAULT_X = 240, DEFAULT_Y = 150;

	//private Hashtable guiObserver = new Hashtable ();
	//private ChatAgent agent;

	/**
	 * Constructor
	 *
	 * @param agent Agent which will be interfaced by this window
	 */
	public ChatAgentWindow(ChatAgent agent, Container aFrame) {
		super(agent, "Chat Agent: ", aFrame);
		this.agent = (ChatAgent) agent;
		pack();
		frame.setLocation(DEFAULT_X+=22, DEFAULT_Y+=22);
	}

	/** @todo uncomment if need to use the designer... have to fix proxies (Stack) */
	//  public ChatAgentWindow() throws Exception {
	//    this(new ChatAgent(7777, "DesignerDummy", new Tristate(), 9000,
	//                       new Tristate(), 0, null), new JInternalFrame());
	//  }

	public ChatAgentInterface getChatAgent() {
		return (ChatAgentInterface) getTransientAgent();
	}

	@Override
	protected JTabbedPane makeTabPane() {
		JTabbedPane pane = super.makeTabPane();
		setSelectedTab("CD");
		return pane;
	}


	/**
	 * Makes and returns a message panel that looks like:
	 *  +------------------------------------------------------------+
	 *  | Messages Received                                          |
	 *  | +--------------------------------------------------------+ |
	 *  | |                     listMessages                     |^| |
	 *  | |                         in                           | | |
	 *  | |                    scrollCDmessages                  |v| |
	 *  | +--------------------------------------------------------+ |
	 *  |         +---------------+ +---------+ +--------+ +-------+ |
	 *  | Message |  textMessage  | |Broadcast| |Directed| |Whisper| |
	 *  |         +---------------+ +---------+ +--------+ +-------+ |
	 *  +------------------------------------------------------------+
	 * All components added to vectors will be an index i.
	 * 
	 * TODO: rkyee: is this i parameter even used? I've updated the MVC pattern
	 * to actually glue the model and view together... but it looks like
	 * they aren't being used at all. This i parameter makes things needlessly
	 * complex and hard to read. 
	 * @param i
	 * @return
	 */
	protected JPanel makeCtrPanel() {
		buttonSendBroadcast = new JButton();
		buttonSendDirected = new JButton();
		buttonSendWhisper = new JButton();
		listMessagesModel = null;
		//ctrPanel = new Vector();
		listMessages = new JList();
		textMessage = new JTextField();

		JPanel panelChatAgent = new JPanel();
		panelChatAgent.setPreferredSize(new Dimension(500, 200));
		panelChatAgent.setLayout(new BorderLayout());
		JPanel panelCDmessages = new JPanel();
		panelChatAgent.add(panelCDmessages, BorderLayout.CENTER);
		panelCDmessages.setLayout(new BorderLayout());
		JLabel labelCDmessages = new JLabel();
		panelCDmessages.add(labelCDmessages, BorderLayout.NORTH);
		JScrollPane scrollCDmessages = new JScrollPane();
		panelCDmessages.add(scrollCDmessages, BorderLayout.CENTER);

		JPanel panelCDsend = new JPanel();
		panelCDsend.setLayout(new BoxLayout(panelCDsend, BoxLayout.X_AXIS));
		panelChatAgent.add(panelCDsend, BorderLayout.SOUTH);
		//panelCDsend.setLayout(new BorderLayout());
		JLabel labelCDsend = new JLabel();
		panelCDsend.add(labelCDsend);

		textMessage.add(new JTextField());

		panelCDsend.add(textMessage);
		//JPanel panelSendButtons = new JPanel();
		//panelCDsend.add(panelSendButtons, BorderLayout.EAST);
		//panelSendButtons.setLayout(new BorderLayout());

		if(buttonSendBroadcast == null){
			buttonSendBroadcast = new JButton();
		}

		class MapButton implements FocusListener{
			JButton button;
			public MapButton(JButton button) {
				this.button = button;
			}

			public void focusLost(FocusEvent e) {
				getRootPane().setDefaultButton(null);
			}

			public void focusGained(FocusEvent e) {
				getRootPane().setDefaultButton(button);
			}
		}
		textMessage.addFocusListener(new MapButton(buttonSendBroadcast));
		textMessage.setMinimumSize(new Dimension(50, 20));
		textMessage.setPreferredSize(new Dimension(100, 20));


		panelCDsend.add(buttonSendBroadcast, BorderLayout.WEST);
		panelCDsend.add(buttonSendDirected, BorderLayout.CENTER);
		panelCDsend.add(buttonSendWhisper, BorderLayout.EAST);

		if(listMessages == null){
			listMessages = new JList();
		}
		labelCDmessages.setText("Messages Received");
		labelCDmessages.setLabelFor(listMessages);

		labelCDsend.setMaximumSize(new Dimension(60, 19));
		labelCDsend.setMinimumSize(new Dimension(10, 19));
		labelCDsend.setText(" Message");
		labelCDsend.setLabelFor(textMessage);


		scrollCDmessages.setPreferredSize(new Dimension(500, 200));
		//listMessages[i] = new JList();
		scrollCDmessages.getViewport().setView(listMessages);

		buttonSendBroadcast.setText("Broadcast");
		buttonSendBroadcast.setMaximumSize(new Dimension(93, 26));
		buttonSendBroadcast.setMinimumSize(new Dimension(10, 26));
		buttonSendBroadcast.setActionCommand(String.valueOf(buttonSEND));
		buttonSendBroadcast.addActionListener(this);
		buttonSendBroadcast.setEnabled(false);

		buttonSendDirected.setText("Directed");
		buttonSendDirected.setMaximumSize(new Dimension(82, 26));
		buttonSendDirected.setMinimumSize(new Dimension(10, 26));
		buttonSendDirected.setActionCommand(String.valueOf(buttonSENDDIRECTED));
		buttonSendDirected.addActionListener(this);
		buttonSendDirected.setEnabled(false);

		buttonSendWhisper.setText("Whisper");
		buttonSendWhisper.setMaximumSize(new Dimension(82, 26));
		buttonSendWhisper.setMinimumSize(new Dimension(10, 26));
		buttonSendWhisper.setActionCommand(String.valueOf(buttonSENDWHISPER));
		buttonSendWhisper.addActionListener(this);
		buttonSendWhisper.setEnabled(false);

		listMessages.setToolTipText("Messages from the selected CD");
		if(listMessagesModel == null){
			listMessagesModel = newChatMessageModelWithAutoScrolling(listMessages);
		}
		
		panelChatAgent.validate();

		textMessage.setToolTipText("Send message to the selected CD");
		textMessage.setEnabled(false);
		return panelChatAgent;
	}

	/**
	 * Creates a new model for a given JList. This model will automatically
	 * scroll as necessary.
	 * @param list the list to bind the model to
	 * @return a {@link ChatMessageListModel} for the provided list
	 */
	private final ChatMessageListModel newChatMessageModelWithAutoScrolling(final JList list){
		ChatMessageListModel model = new ChatMessageListModel();
		model.addListDataListener(new ListDataListener() {

			public void intervalRemoved(ListDataEvent e) {
			}

			public void intervalAdded(ListDataEvent e) {
				java.awt.EventQueue.invokeLater(new Runnable() {
					public void run() {
						JViewport viewport = (JViewport) list.getParent();
						//TODO: this fudge may be platform specific (pixels?)
						final int fudge = 17;
						if(viewport.getViewPosition().y >= viewport.getViewSize().height - viewport.getViewRect().height - fudge)
							list.ensureIndexIsVisible(list.getModel().getSize()-1);
					}
				});
			}

			public void contentsChanged(ListDataEvent e) {
			}
		});
		list.setModel(model);
		return model;
	}


	JMenuBar menuBar;
	@Override
	protected JMenuBar makeMenuBar() {
		menuBar = super.makeMenuBar();

		menuCD_setRetrieveHistory = new JCheckBoxMenuItem(
				"Retrieve History On Join", getChatAgent().willObtainHistory());
		menuCD_setRetrieveHistory.setMnemonic(KeyEvent.VK_R);
		//    menuCD_setRetrieveHistory.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,InputEvent.CTRL_MASK));
		menuCD_setRetrieveHistory.setEnabled(true);
		menuCD_setRetrieveHistory.setActionCommand(String.valueOf(
				menuSETRETRIEVEHISTORY));
		menuCD_setRetrieveHistory.addActionListener(this);
		menuCD.add(menuCD_setRetrieveHistory);

		//extend the CD menu bar item
		menuCD.addSeparator();


		menuCD_MembersSelectAll = new JMenuItem("Select all members");
		menuCD_MembersSelectAll.setMnemonic(KeyEvent.VK_S);
		menuCD_MembersSelectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				InputEvent.CTRL_MASK));
		menuCD_MembersSelectAll.setEnabled(false);
		menuCD_MembersSelectAll.setActionCommand(String.valueOf(
				menuCDMEMBERSSELECTALL));
		menuCD_MembersSelectAll.addActionListener(this);
		menuCD.add(menuCD_MembersSelectAll);

		menuCD_MembersSelectNone = new JMenuItem("De-select all members");
		menuCD_MembersSelectNone.setMnemonic(KeyEvent.VK_D);
		menuCD_MembersSelectNone.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.
				VK_S, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
		menuCD_MembersSelectNone.setEnabled(false);
		menuCD_MembersSelectNone.setActionCommand(String.valueOf(
				menuCDMEMBERSSELECTNONE));
		menuCD_MembersSelectNone.addActionListener(this);
		menuCD.add(menuCD_MembersSelectNone);

		// Tools Menu - Allows the user to add chat friends to a list
		JMenu menuTool = new JMenu("Tools");
		menuTool.setMnemonic(KeyEvent.VK_T);
		menuAddFriend = new JMenuItem("ChatAgent Interaction");
		menuAddFriend.setEnabled(true);
		menuAddFriend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addChatFriends();
			}
		});
		menuTool.add(menuAddFriend);

		menuTool.addSeparator();

		JMenuItem securityItem = new JMenuItem("Set Access Rights");
		securityItem.setEnabled(true);
		securityItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setAccessRights();
			}
		});
		menuTool.add(securityItem);

		menuBar.add(menuTool, menuBar.getComponentCount()-1);
		return menuBar;
	}


	/**
	 *  +------------------------------------------------------------+
	 *  | +---------------------------+----------------------------+ |
	 *  | |   Cooperation Domains     |       CD Participants      | |
	 *  | |---------------------------|----------------------------| |
	 *  | |     listCDs             |^|         listMembers      |^| | makeCDpanel
	 *  | |       in                | |             in           | | |
	 *  | |   scrollCDlist          | |    ScrollCDparticipants  | | |
	 *  | |                         |v|                          |v| |
	 *  | +---------------------------+----------------------------+ |
	 *  +------------------------------------------------------------+
	 *  | Messages Received                                          |
	 *  | +--------------------------------------------------------+ |
	 *  | |                     listMessages                     |^| |
	 *  | |                         in                           | | |
	 *  | |                    scrollCDmessages                  |v| |
	 *  | +--------------------------------------------------------+ |makeCtrpanel
	 *  |         +---------------+ +---------+ +--------+ +-------+ |
	 *  | Message |  textMessage  | |Broadcast| |Directed| |Whisper| |
	 *  |         +---------------+ +---------+ +--------+ +-------+ |
	 *  +------------------------------------------------------------+

	 * @return
	 */
	@Override
	protected JPanel makeCDPanel () {
		//protected JSplitPane makeMessagesPane(int i){
		final JPanel dummyPanel = new JPanel();
		final JPanel cd = super.makeCDPanel();
		AbstractInternalFrame.runInEventDispatchThread(new Runnable() {
			public void run() {
				JSplitPane r = new JSplitPane(JSplitPane.VERTICAL_SPLIT, cd, makeCtrPanel());
				r.setDividerSize(1);
				r.setResizeWeight(0.5);

				//dummyPanel = new JPanel ();
				dummyPanel.setLayout (new GridLayout (1, 1));
				dummyPanel.add (r);
				dummyPanel.setPreferredSize(new Dimension(300, 300));
			}},true);
		return dummyPanel;
	}

	private void setAccessRights() {

	}

	public Dimension getPreferredSize() {
		return new Dimension(500, 500);
	}

	/**
	 * Called when a message is to be sent.
	 * Gets the message and calls a function to send the message.
	 * whisper determines if the message is to be whispered or directed.
	 *
	 * @param whisper
	 */
	private void sendMessage(Tristate whisper) {
		String message;
		message = textMessage.getText();

		// ensure message is not empty
		if (message.length() > 0) {
			URLDescriptor cd = (URLDescriptor) listCDs.getSelectedValue();

			// ensure a cd is selected
			if (cd != null) {
				if (whisper.equals(Tristate.FALSE)) {
					getChatAgent().doSendChatMessage(cd, message);
					textMessage.setText("");
				}

				else {
					if (listCDMembers != null) {
						if (listCDMembers.getSelectedValues().length == 0) {
							//              getChatAgent().doSendChatMessage (cd, message);
							if (whisper.equals(Tristate.TRUE)) {
								JOptionPane.showMessageDialog(
										frame,
										"Cannot send whisper without selecting a member or members to whisper to.",
										"Must Select Member(s)",
										JOptionPane.ERROR_MESSAGE);
							}
							else {
								JOptionPane.showMessageDialog(
										frame,
										"Cannot send whisper without selecting a member or members to send the directed message to.",
										"Must Select Member(s)",
										JOptionPane.ERROR_MESSAGE);
							}
						}
						else if (listCDMembers.getSelectedValues().length == 1) {
							getChatAgent().doSendChatMessage(cd,
									(URLDescriptor) listCDMembers.getSelectedValue(),
									whisper.equals(Tristate.TRUE),
									message);
							// clear the text box after the message is sent
							textMessage.setText("");
						}
					}

					else {
						Vector<URLDescriptor> tos = new Vector<URLDescriptor>();
						for (Object url : listCDMembers.getSelectedValues())
							if (url instanceof URLDescriptor) tos.add((URLDescriptor)url);
						getChatAgent().doSendChatMessage(cd, tos,
								whisper.equals(Tristate.TRUE),
								message);
						// clear the text box after the message is sent
						textMessage.setText("");
					}
				}
			}
			else {
				JOptionPane.showMessageDialog(frame,
						"Please select a CD to send message to.",
						"No CD Selected",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		else {
			JOptionPane.showMessageDialog(frame,
					"Please type your message in the input line labeled \"Message\".",
					"No Message", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Allows the user to add ChatFriends to a list... this will simplify the fact that the user
	 * has to type in the IP address at all times.  The main idea is that a ChatAgent now has a
	 * History File where it keeps track of the new "Freind" property, which is used to store
	 * chatting friends.
	 */
	private void addChatFriends() {
		final int SHOW_ADD = 1;
		new ChatFriendInteractionDialog(getFrame(), "Chat Agent Interaction", true,
				getChatAgent(), SHOW_ADD);
	}

	/**
	 * implementation of the ActionListener interface
	 * @param e --> Object implemented as a listener.  It interacts with
	 *              getActionCommand() & intValue().  The value returned is
	 *              assigned to the variable: command <-- used in the switch
	 *              in order to set the activity of the Window, i.e. closeWindow()
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		int command = noAction;
		try {
			command = Integer.valueOf(e.getActionCommand()).intValue();
		}
		catch (Exception ex) {
			command = noAction;
		}

		switch (command) {
		case listSCROLLED:
			CDSelectionChanged();
			break;
		case buttonSEND:
			sendMessage(Tristate.FALSE);
			break;
		case buttonSENDDIRECTED:
			sendMessage(Tristate.UNDEFINED);
			break;
		case buttonSENDWHISPER:
			sendMessage(Tristate.TRUE);
			break;
		case menuSETRETRIEVEHISTORY:
			getChatAgent().setObtainHistory(menuCD_setRetrieveHistory.getState());
			break;
		case menuCDMEMBERSSELECTNONE:
			listCDMembers.clearSelection();
			break;
		case menuCDMEMBERSSELECTALL:
			listCDMembers.setSelectionInterval(0, listCDMembers.getModel().getSize());
			break;
		default:
			super.actionPerformed(e);
		}
	}

	/**
	 * Observer interface
	 */

  /**
   * Observer will be notified when the agent's state is STATE_CLOSE_PORT. In
   * this case, the agent will be unregistered from the LAC and widthdrawn from
   * the CDs it is participating.
   * @param observable
   * @param argument
   */
  @Override
	public void updateEventHandler(Observable observable, String event, Object argObject) {
      /* JOIN CD or WITHDRAW CD *********************************************/
		// note that most of the functionality here is already done by TransientAgentFrame,
		if (event.equals(ML.EVENT_JOIN_CD) || event.equals(ML.EVENT_WITHDRAW_CD)){
			buttonSendBroadcast.setEnabled(false);
			buttonSendDirected.setEnabled(false);
			buttonSendWhisper.setEnabled(false);
			textMessage.setEnabled(false);
			textMessage.setText("");
			CDSelectionChanged();
		}

		/*RECEIVE_MESSAGE_CD (from menu) ************************************/
		else if(event.equals(ML.EVENT_CHAT_MESSAGE_RECEIVED)){
			/** @todo better? */
			updateMessageList();
		}
		/*INVITE_CD**********************************************************/
		else if(event.equals(ML.EVENT_INVITE_CD)){
			invitationCDReceived();
		}
		/*GET_CD_PARTICIPANTS************************************************/
		else if(event.equals(ML.EVENT_GET_CD_PARTICIPANTS) || 
				event.equals(ML.EVENT_CD_NEW_MEMBER) || //TODO: this event is never fired 
				event.equals(ML.EVENT_REGISTER_INSTANCE)){
			updateMemberList();	
		}
		
		super.updateEventHandler(observable, event, argObject);
  }
	
	@Override
	protected JPanel setInfoPanel() {
		JPanel infoPanel = new JPanel();
		Vector<String> agentVector = new Vector<String>();
		int counter = setInfoPanel(infoPanel, agentVector);

		agentVector.add(counter++, "");
		agentVector.add(counter++, "");
		String createdate = ((ChatAgent)agent).getCreateDate();
		if (createdate != null)
			agentVector.add(counter++, "Created: " + createdate);

		try {
			File file = new File(((ChatAgent)agent).getCASAFilename());
			if (file.exists()) {
				agentVector.add(counter++, "Last modified: " + CASAUtil.getDateAsString(file.lastModified()));
			}
		} catch (Throwable e) {
		}
		infoPanel.validate();
		return infoPanel;
	}

	/**
	 * Updates window if a different CD is selected. 2 main updates: updates the
	 * list of messages received by this agent using the new CD, and retreives
	 * the list of participants on the newly selected CD.
	 */
	@Override
	protected void CDSelectionChanged() {
		super.CDSelectionChanged();

		runInEventDispatchThread(new Runnable() {
			public void run() {
				URLDescriptor temp = null;
				try {
					temp = (URLDescriptor) listCDs.getSelectedValue();
				}
				catch (ArrayIndexOutOfBoundsException ex) {}
				final URLDescriptor cd = temp;

				// if a CD is selected
				boolean isSelected = (cd != null);
				buttonSendBroadcast.setEnabled(isSelected);
				buttonSendDirected.setEnabled(isSelected);
				buttonSendWhisper.setEnabled(isSelected);
				textMessage.setEnabled(isSelected);

				menuCD_MembersSelectAll.setEnabled(isSelected);
				menuCD_MembersSelectNone.setEnabled(isSelected);
				if (isSelected) {
					getRootPane().setDefaultButton(buttonSendBroadcast);
				}

				updateMessageList();
			}
		},true);

	}

	/**
	 * Updates the message models; for auto-scrolling, find the handler in
	 * {@link #makeCtrPanel(int)}. 
	 */
	private void updateMessageList() {
		if (listCDs.getSelectedValue() != null){
			Vector<ChatMessage> messages = getChatAgent().getHistory((URLDescriptor)listCDs.getSelectedValue());
			listMessagesModel.setVector(messages);
		}
	}

	@Override
	protected void updateMemberList() {
		super.updateMemberList();

		Vector<URLDescriptor> memberList = null; //new Vector ();
		if(listCDs != null) {
			URLDescriptor cd = (URLDescriptor) listCDs.getSelectedValue();

			// if a CD is selected
			boolean isSelected = ( (JList) listCDs).getSelectedIndex() > -1;
			if (isSelected) {
				memberList = getChatAgent().getMembers(cd);
			}

			// update the member list in the window
			boolean members = memberList != null;
			menuCD_MembersSelectAll.setEnabled(members);
			menuCD_MembersSelectNone.setEnabled(members);
		}

	}

	private void invitationCDReceived() {
		//    AgentInvitedCDDialog d = new AgentInvitedCDDialog ( /*AgentWindow.this, */null,
		//        /*true, */this.agent);
		//    d.show ();
	}

	/**
	 * overridden implementation of WindowListener interface method from AbstractWindow
	 * this simply calls System.exit(0)
	 *
	 * @param e <-- see above explanation.
	 */
	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	protected void inviteToCD() {
		final int SHOW_INVITE = 2;
		new ChatFriendInteractionDialog(getFrame(), "Chat Agent Interaction", true,
				getChatAgent(), SHOW_INVITE);
	}

	@Override
	public void internalFrameActivated(InternalFrameEvent e) {
		super.internalFrameActivated(e);
		getRootPane().setDefaultButton( buttonSendBroadcast);
	}
}
