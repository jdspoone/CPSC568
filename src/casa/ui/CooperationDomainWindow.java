package casa.ui;

import casa.CooperationDomain;
import casa.ML;
import casa.agentCom.URLDescriptor;
import casa.util.CASAUtil;
import casa.util.Trace;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Observable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * 
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
 * @author Jason Heard
 * @author Gabriel Becerra
 * @author Eunice Lim
 * @author <a href="mailto:sedachv@cpsc.ucalgary.ca">Vladimir Sedach</a>
 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
 */
public class CooperationDomainWindow extends AgentInternalFrame {

	static int DEFAULT_X = -22, DEFAULT_Y = -22;

  /**
   * The list of member agents in the CD that has focus (selected) in {@link #listCDs}; part of {@link #cdsPanel}.
   */
  private JList members;
  
  @Override
	public void updateEventHandler(Observable observable, String event, Object argObject) {
			if (event.equals(ML.EVENT_JOIN_CD)){
				announceMyEntrance();				
			}
			//PURPOSEFULLY NO ELSE-IF HERE
			if (event.equals(ML.EVENT_JOIN_CD) || event.equals(ML.EVENT_JOIN_CD_REPEATED)){
				if (argObject != null)
					joinedMembersList((URLDescriptor) argObject);
			}
			else if (event.equals(ML.EVENT_UPDATE_URL_CD)){
				if (argObject != null) {
					withdrewMembersList((URLDescriptor) argObject);
					joinedMembersList((URLDescriptor) argObject);
				}
			}
			else if (event.equals(ML.EVENT_PARTICIPANT_CD)){
				//TODO Pass... no catch-all 'else'.  Is this condition needed?
			}
			else if (event.equals(ML.EVENT_WITHDRAW_CD)){
				if (argObject != null)
					withdrewMembersList((URLDescriptor) argObject);
			}
			super.updateEventHandler(observable, event, argObject);
  }

	// private JButton buttonClose = new JButton ();
	//
	// CASA Objects
	//
	//private CooperationDomain cd;

	//
	// Varaibles
	//

	// private BorderLayout layoutTop = new BorderLayout ();
	// private BorderLayout layoutURL = new BorderLayout ();
	private JTextField fieldURL;

	// private JMenuBar menuBar = new JMenuBar ();
	// private JMenu menuCD = new JMenu ();
	// private JMenuItem menuCDClose = new JMenuItem ();

	//
	// JAVA - UI
	//
	// menu section
	private JCheckBoxMenuItem menuCDPersistent = new JCheckBoxMenuItem();

	private JCheckBoxMenuItem menuCDPersistentHistory = new JCheckBoxMenuItem();

	protected JMenuItem menuCDViewHistory;

	// buttons
	// private JPanel panelButtons = new JPanel ();

	// members' components
	// private JPanel panelMembers;

	// top panel
	// private JPanel panelTop = new JPanel ();

	// URL components
	// private JPanel panelURL = new JPanel ();

	/**
	 * Constructor
	 * 
	 * @param theCD Cooperation Domain object
	 */
	public CooperationDomainWindow(CooperationDomain theCD, String title,
			Container aFrame) {
		super(theCD, /*"CooperationDomain"*/title, aFrame);
		setMaximizable(true);

		try {
			pack();
		} catch (Exception e) {
			e.printStackTrace();
		}

		updateMenuPrefs();
		frame.setLocation(DEFAULT_X+=22, DEFAULT_Y+=22);
	}

	CooperationDomain getCD() {
		return (CooperationDomain)agent;
	}
	
	private void announceMyEntrance() {
		int waitTime = 5000;
		String coopDomainName = agent.getName();
		try {
			String usernameToParse = getCD().getMembers().lastElement().toString();

			// split the string in order to obtain only the username
			Pattern cdPattern = Pattern.compile("casa://");
			Matcher cdMatcherStart = cdPattern.matcher(usernameToParse);
			Pattern cdPattern2 = Pattern.compile("@");
			Matcher cdMatcherEnd = cdPattern2.matcher(usernameToParse);

			if (cdMatcherStart.find() && cdMatcherEnd.find()) {
				String userName = usernameToParse.substring(cdMatcherStart.end(),
						cdMatcherEnd.start());
				new JoinedAnnouncement(waitTime, userName, coopDomainName);
			} else
				new JoinedAnnouncement(waitTime, "Unrecognized", coopDomainName);
		} catch (Throwable e) {
			new JoinedAnnouncement(waitTime, "Unrecognized", coopDomainName);
		}
	}

	protected void iconifyFrame() {
		this.setIcon(true);
	}

	@Override
	protected void CDSelectionChanged() {
		runInEventDispatchThread(new Runnable() {
			@Override
			public void run() {
				Vector<URLDescriptor> members = getCD().getMembers();
				updateCDmemberJList(members);
			}
		});
	}

	/**
	 * Overides the method in TransientAgentInternalFrame. Makes and returns a
	 * CD panel that looks like: 
	 * +-----------------------------------+
	 * | Members:                        
	 * | +-------------------------------+ | 
	 * | | listMembers                 |^| |
	 * | | in                          | | | 
	 * | | scrollMembers               |v| | 
	 * | +-------------------------------+ |
	 * +-----------------------------------+
	 * All components placed in vectors
	 * will be at index i.
	 * 
	 * @return
	 */
	@Override
	protected JPanel makeCDPanel() {
		members = getListCDMembers();

		JPanel panelMembers = new JPanel();
		panelMembers.setLayout(new BorderLayout());
		panelMembers
				.setBorder(new TitledBorder(new EtchedBorder(), "Members:"));
		JScrollPane scrollMembers = new JScrollPane();
		panelMembers.add(scrollMembers, BorderLayout.CENTER);

		scrollMembers.getViewport().setView(members);
		scrollMembers.setPreferredSize(new Dimension(245, 100));
		
//		members.addListSelectionListener(new ListSelectionListener() {
//			@Override
//			public void valueChanged(ListSelectionEvent arg0) {
//		    if (!arg0.getValueIsAdjusting ()) {
//	        if (listCDs != null) {
//	          if ( listCDs.getSelectedIndex() > -1) {
//	            CDSelectionChanged();
//	          }
//	        }
//	    }
//			}
//		});
		members
				.setToolTipText("Currently registered agents on this Cooperation Domain.");
		members.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		members.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		return panelMembers;
	}

  @Override
	protected JList getListCDMembers() {
  	return new RefreshTimerJList(
				new CooperationDomainListModel(getCD().getMembers()), 500);
  }

	@Override
	protected JMenuBar makeMenuBar() {
		JMenuBar menuBar = super.makeMenuBar();
		menuAgent.setText("Cooperation Domain");

		menuCDViewHistory = new JMenuItem();
		menuCDViewHistory.setText("View History...");
		menuCDViewHistory.setMnemonic('v');
		menuCDViewHistory.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				menuCDViewHistory_actionPerformed(e);
			}
		});
		int count = menuAgent.getItemCount();
		menuAgent.insert(menuCDViewHistory, count - 2);
		menuAgent.insertSeparator(count - 2);

		menuBar.remove(menuCD);

		/* @todo Persistent History option */

		return menuBar;
	}

	@Override
	protected JTabbedPane makeTabPane() {
		JTabbedPane pane = super.makeTabPane();
		setSelectedTab("CD");
		return pane;
	}
	
	@Override
	protected JPanel setInfoPanel() {
		JPanel infoPanel = new JPanel();
		Vector<String> agentVector = new Vector<String>();
		int counter = setInfoPanel(infoPanel, agentVector);

		agentVector.add(counter++, "");
		agentVector.add(counter++, "");
		String createdate = getCD().getCreateDate();
		if (createdate != null)
			agentVector.add(counter++, "Created: " + createdate);

		try {
			File file = new File(getCD().getCASAFilename());
			if (file.exists()) {
				agentVector.add(counter++, "Last modified: " + CASAUtil.getDateAsString(file.lastModified()));
			}
		} catch (Throwable e) {
		}
		infoPanel.validate();
		return infoPanel;
	}

	void menuCDpersistent_actionPerformed(ActionEvent e) {
		boolean persistent = menuCDPersistent.isSelected();
		getCD().setPersistent(persistent);
		menuCDPersistentHistory.setEnabled(persistent);
		if (!persistent) {
			menuCDPersistentHistory.setSelected(false);
		}
	}

	void menuCDpersistentHistory_actionPerformed(ActionEvent e) {
		getCD().setPersistent(menuCDPersistentHistory.isSelected());
	}

	void menuCDViewHistory_actionPerformed(ActionEvent e) {
		Trace.log("error", "menuCDViewHistory_actionPerformed() not implemented");
	}

	protected void updateFieldURL() {
		fieldURL.setText(getCD().getURL().getFullAddress(
				getCD().getURL()));
	}

	private CooperationDomainListModel getMembersListModel() {
		RefreshTimerJList list = (RefreshTimerJList) members;
		return (CooperationDomainListModel) list.getModel();
	}

	private void joinedMembersList(URLDescriptor joinerURL) {
		getMembersListModel().addMember(joinerURL);
		CDSelectionChanged();
	}

	private void withdrewMembersList(URLDescriptor withdrawnURL) {
		getMembersListModel().removeMember(withdrawnURL);
		CDSelectionChanged();
	}

	private void updateMenuPrefs() {
		menuCDPersistent.setSelected(getCD().isPersistent());
		if (menuCDPersistent.isSelected()) {
			menuCDPersistentHistory.setEnabled(true);
			menuCDPersistentHistory.setSelected(getCD().isPersistent());
		} else {
			menuCDPersistentHistory.setEnabled(false);
			menuCDPersistentHistory.setSelected(false);
		}
	}

//	@Override
//	protected void updateCDmemberJList (Vector<URLDescriptor> memberList) {
//		super.updateCDmemberJList(memberList);
//		// Don't do anything!
//	}
	
  /**
   * Updates the listCDMembers JLists and enables or disables the menuCD_Withdrawn menu item
   * according to the parameter.
   * <br>
   * NOTE: THIS METHOD SHOULD BE CALLED ONLY IN AWT'S EDT THREAD (invokeLater()).
   * @param memberList A Vector of URLDescriptors.
   */
	@Override
  protected void updateCDmemberJList (final Vector<URLDescriptor> memberList) {
  	//final URLDescriptor cd = temp;
  	//final Vector<URLDescriptor> memberList = new Vector<URLDescriptor>(agent.getMembers(cd));

  	// update the member list in the window
  	if (members != null) {
  		try {
  			//listCDMembers.setListData(memberList);
  			members.removeSelectionInterval(0, members.getModel().getSize());
  			CooperationDomainListModel model = ((CooperationDomainListModel)members.getModel());
  			model.membersList = memberList;
  			model.refreshList();
  			
  		}
  		catch (Throwable ex) {
  			Trace.log("error", "TransientAgentInternalFrame.CDSelectionChanged: Bad JList.setListData()", ex);
  		}
  	}
  }


}
