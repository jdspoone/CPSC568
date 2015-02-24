package casa.ui;

import casa.agentCom.URLDescriptor;
import casa.exceptions.URLDescriptorException;
import casa.interfaces.ChatAgentInterface;
import casa.util.CASAUtil;
import casa.util.PropertyException;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 * @author  <a href="mailto:ayala@cpsc.ucalgary.ca">Gabriel Becerra</a>
 */

@SuppressWarnings("serial")
public class ChatFriendInteractionDialog
    extends JDialog {
  /**
	 */
  ChatAgentInterface agent;

  Hashtable<String, String> friendsTable = new Hashtable<String, String>();
  Hashtable<String, URLDescriptor> cdHashtable = new Hashtable<String, URLDescriptor>();

  int showTab = 0;
  int numberOfJoinedCD = 0;

  JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

  JPanel dialogPanel = new JPanel();
  BorderLayout borderLayout1 = new BorderLayout();
  JTabbedPane friendTab = new JTabbedPane();
  JPanel addPanel = new JPanel();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  JLabel userNameLabel = new JLabel();
  JLabel ipLabel = new JLabel();
  JTextField ipField = new JTextField();
  JTextField userNameField = new JTextField();
  JButton addButton = new JButton();
  JButton cancelButton = new JButton();
  JPanel inviteMainPanel = new JPanel();
  JPanel friendsListPanel = new JPanel();
  JPanel buttonPanel = new JPanel();
  JList friendsList = new JList();
  JList cdList = new JList();
  JButton inviteButton = new JButton();
  JButton cancelInviteButton = new JButton();
  GridLayout gridLayout1 = new GridLayout();
  GridLayout gridLayout2 = new GridLayout(2, 1);
  GridBagLayout gridBagLayout2 = new GridBagLayout();

  public ChatFriendInteractionDialog(Frame frame, String title, boolean modal,
                                     ChatAgentInterface agent, int showTab) {
    super(frame, title, modal);

    this.agent = agent;
    this.showTab = showTab;
    unserializeFriends(this.agent);

    try {
      jbInit();
      setSize(300, 250);
      setVisible(true);
      setResizable(false);
      pack();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    dialogPanel.setLayout(borderLayout1);
    friendTab.setBackground(Color.lightGray);
    addPanel.setLayout(gridBagLayout1);
    userNameLabel.setText("username");
    ipLabel.setText("IP:Port");
    ipField.setText("");
    userNameField.setText("");
    addButton.setText("Add Friend");
    cancelButton.setText("Cancel");
    inviteMainPanel.setLayout(gridLayout2);
    inviteButton.setText("Invite");
    cancelInviteButton.setText("Cancel");
    friendsList.setMaximumSize(new Dimension(0, 0));
    friendsList.setMinimumSize(new Dimension(0, 0));
    friendsList.setToolTipText("Choose a friend to invite");
    friendsListPanel.setLayout(gridLayout1);
    buttonPanel.setLayout(gridBagLayout2);
    buttonPanel.setMaximumSize(new Dimension(32767, 32767));
    buttonPanel.setMinimumSize(new Dimension(0, 0));
    buttonPanel.setPreferredSize(new Dimension(0, 0));
    gridLayout2.setRows(2);
    dialogPanel.setMinimumSize(new Dimension(300, 200));
    getContentPane().add(dialogPanel);
    dialogPanel.add(friendTab, BorderLayout.CENTER);

    inviteButton.setEnabled(false);
    inviteButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        inviteFriend();
      }
    });
    cancelInviteButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dispose();
      }
    });
    addButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addFriends();
      }
    });
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dispose();
      }
    });
    friendsList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        enableInviteButton(); /** @todo Change this method's name */
      }
    });
    cdList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        enableInviteButton();
      }
    });
    friendTab.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent evt) {
        JTabbedPane pane = (JTabbedPane) evt.getSource();
        // Get current tab
        int sel = pane.getSelectedIndex();
        if (sel == 1) {
          showFriends();
        }
      }
    });

    //
    // Adding Friends set up section
    //
    addPanel.add(userNameLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(0, 0, 5, 0), 0, 0));
    addPanel.add(ipLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                                                 , GridBagConstraints.CENTER,
                                                 GridBagConstraints.BOTH,
                                                 new Insets(0, 0, 0, 0), -1, 0));
    addPanel.add(ipField, new GridBagConstraints(1, 2, 1, 2, 0.0, 0.0
                                                 , GridBagConstraints.CENTER,
                                                 GridBagConstraints.BOTH,
                                                 new Insets(0, 0, 20, 0), 165,
                                                 0));
    addPanel.add(userNameField, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(0, 0, 5, 0), 165, 0));
    addPanel.add(addButton, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(0, 0, 0, 10), 0, 0));
    addPanel.add(cancelButton, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(0, 10, 0, 0), 0, 0));

    //
    // Invite Friends set up section
    //
    buttonPanel.add(cancelInviteButton,
                    new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
                                           , GridBagConstraints.CENTER,
                                           GridBagConstraints.BOTH,
                                           new Insets(0, 10, 0, 1), 60, 0));
    buttonPanel.add(inviteButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(0, 0, 0, 10), 60, 0));
    inviteMainPanel.add(friendsListPanel, null);
    friendsListPanel.add(split, null);
    inviteMainPanel.add(buttonPanel, null);

    inviteMainPanel.setBorder(new TitledBorder(new EtchedBorder(),
        "Chat Friends & CDs - Choose one of each"));

    if (showTab == 2) {
      friendTab.add(inviteMainPanel, "Invite");
      friendTab.add(addPanel, "Add Friends");
      showFriends();
    } else {
      friendTab.add(addPanel, "Add Friends");
      friendTab.add(inviteMainPanel, "Invite");
    }

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension dialogSize = this.getPreferredSize();
    setLocation(screenSize.width / 2 - (dialogSize.width / 2),
                screenSize.height / 2 - (dialogSize.height / 2));
  }

  private void unserializeFriends(ChatAgentInterface agent) {
    try {
      friendsTable = (Hashtable<String, String>) CASAUtil.unserialize
          (agent.getStringProperty("ChatFriends"), null);
    } catch (ParseException ex) {
    } catch (PropertyException ex) {}
  }

  private void addFriends() {
    String name = userNameField.getText();
    String ip = ipField.getText();

    if (name.length() != 0 || ip.length() != 0) {
      friendsTable.put(ip, name);
      agent.setStringProperty("ChatFriends", CASAUtil.serialize(friendsTable));
    }
  }

  private void showFriends() {
    Vector<String> friends = new Vector<String>();
    Vector<String> vectorList = new Vector<String>();

    Set<URLDescriptor> cdVector = agent.getJoinedCooperationDomains();

    Enumeration<String> index = friendsTable.keys();
    while (index.hasMoreElements()) {
      String item = index.nextElement().toString();
      String name = friendsTable.get(item).toString();
      friends.add(name);
    }
    friendsList.setListData(friends);

    JScrollPane sc1 = new JScrollPane();
    sc1.getViewport().setView(friendsList);
    split.setLeftComponent(sc1);

    numberOfJoinedCD = cdVector.size();
    for (URLDescriptor item: cdVector) {

      // split the string in order to obtain only the CD name
      Pattern pattern = Pattern.compile("CooperationDomain/");
      Matcher matcher = pattern.matcher(item.toString(agent.getURL()));
      Pattern pattern2 = Pattern.compile("#");
      Matcher matcher2 = pattern2.matcher(item.toString(agent.getURL()));

      if (matcher.find() && matcher2.find()) {
        String coopDomainName = item.toString(agent.getURL()).substring(matcher.end(), matcher2.start());
        vectorList.add(coopDomainName);

        cdHashtable.put(coopDomainName, item);
      }
    }
    cdList.setListData(vectorList);

    JScrollPane sc2 = new JScrollPane();
    sc2.getViewport().setView(cdList);
    split.setRightComponent(sc2);

    split.setContinuousLayout(true);
    split.setDividerLocation(0.5);
    split.setOneTouchExpandable(true);
  }

  /**
   * Enables the invite button iff a friend and a CD are selected
   */
  private void enableInviteButton() {
    if (friendsList.getSelectedIndex() > -1 && cdList.getSelectedIndex() > -1) {
      inviteButton.setEnabled(true);
    }
    else {
      inviteButton.setEnabled(false);
    }
  }

  private void inviteFriend() {
    String invitee = friendsList.getSelectedValue().toString();
    Object cdSelected = cdList.getSelectedValue();
    URLDescriptor friendToInvite = null;
    URLDescriptor cdToUse = null;

    Enumeration<String> e = friendsTable.keys();
    while (e.hasMoreElements()) {
      String item = e.nextElement().toString();
      String name = friendsTable.get(item).toString();

      if (name == invitee) {
        String patternStr = ":";
        String[] fields = item.split(patternStr);

        String ip = fields[0];
        String port = fields[1];

        try {
          friendToInvite = URLDescriptor.make(ip, port);
        }
        catch (URLDescriptorException ex) {}
        cdToUse = cdHashtable.get(cdSelected);

        agent.doInviteToCD(friendToInvite, cdToUse, true);
      }
    }
  }
}
