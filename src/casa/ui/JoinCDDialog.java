package casa.ui;

import casa.CooperationDomain;
import casa.LAC;
import casa.Status;
import casa.abcl.ParamsMap;
import casa.agentCom.URLDescriptor;
import casa.interfaces.TransientAgentInterface;
import casa.util.Trace;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.List;
import java.util.TimerTask;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.armedbear.lisp.JavaObject;
import org.armedbear.lisp.SimpleString;

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
 * @author <a href="mailto:ayala@cpsc.ucalgary.ca">Gabriel Becerra</a>
 * @author Jason Heard
 */

public class JoinCDDialog extends JDialog {
	private TransientAgentInterface agent;

	private Hashtable<String,URLDescriptor> cdHashtable = new Hashtable<String,URLDescriptor> ();

	private int joinMode = EXISTING_LOCAL;

	private static final int EXISTING_LOCAL = 0;

	private static final int EXISTING_REMOTE = 1;

	private static final int NEW_LOCAL = 2;

	private boolean ipLast = true;

	private String savedURLorNameText = "";

	private String savedPortText = "";

	private JPanel dialogPanel = new JPanel();

	private JPanel panelRadioButtons = new JPanel();

	private JPanel panelPanelRadioButtons = new JPanel();

	private GridBagLayout gridBagLayout1 = new GridBagLayout();

	private JPanel panelLocalCDs = new JPanel();

	private JPanel panelIPOrNameAndPort = new JPanel();

	private ButtonGroup buttonGroupJoinType = new ButtonGroup();

	private JRadioButton radioButtonCreateJoinLocal = new JRadioButton();

	private JRadioButton radioButtonJoinRemote = new JRadioButton();

	private JRadioButton radioButtonJoinLocal = new JRadioButton();

	private JScrollPane scrollListLocalCDs = new JScrollPane();

	private JList listLocalCDs = new JList();

	private JTextField textFieldURLorName = new JTextField();

	private JTextField textFieldPort = new JTextField();

	private GridBagLayout gridBagLayout2 = new GridBagLayout();

	private JLabel labelIPOrName = new JLabel();

	private JLabel labelPort = new JLabel();

	private GridLayout gridLayout1 = new GridLayout();

	private GridBagLayout gridBagLayout3 = new GridBagLayout();

	private JPanel panelButtons = new JPanel();

	private JButton buttonCancel = new JButton();

	private JButton buttonJoinOrCreate = new JButton();

	private TitledBorder titledBorder1;

	public JoinCDDialog(Frame frame, boolean modal,
			TransientAgentInterface agent) {
		super(frame, "Join CD", modal);

		this.agent = agent;

		try {
			jbInit();
			pack();
			// Center of the screen, better than insane far upper left.
			setLocation((int) (Toolkit.getDefaultToolkit().getScreenSize()
					.getWidth() - this.getSize().getWidth()) / 2,
					(int) (Toolkit.getDefaultToolkit().getScreenSize()
							.getHeight() - this.getSize().getHeight()) / 2);
			setVisible(true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		radioButtonJoinLocal.setSelected(true);
	}

	private void jbInit() throws Exception {
		dialogPanel.setLayout(gridBagLayout1);

		// Create radio button panel
		panelRadioButtons.setLayout(gridBagLayout3);

		// titledBorder1 = new
		// TitledBorder(BorderFactory.createEtchedBorder(Color.white,new
		// Color(148, 145, 140)),"Cooperation Domain\n To Join");
		titledBorder1 = new TitledBorder("Cooperation Domain To Join");
		panelRadioButtons.setBorder(titledBorder1);

		radioButtonJoinLocal.setText("Existing Local");
		radioButtonJoinLocal.setMnemonic('L');
		radioButtonJoinLocal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				radioButtonJoinLocal_actionPerformed(e);
			}
		});
		buttonGroupJoinType.add(radioButtonJoinLocal);
		panelRadioButtons.add(radioButtonJoinLocal, new GridBagConstraints(0,
				0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		radioButtonJoinRemote.setText("Existing Remote");
		radioButtonJoinRemote.setMnemonic('R');
		radioButtonJoinRemote.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				radioButtonJoinRemote_actionPerformed(e);
			}
		});
		buttonGroupJoinType.add(radioButtonJoinRemote);
		panelRadioButtons.add(radioButtonJoinRemote, new GridBagConstraints(0,
				1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		radioButtonCreateJoinLocal.setText("New Local");
		radioButtonCreateJoinLocal.setMnemonic('C');
		radioButtonCreateJoinLocal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				radioButtonCreateJoinLocal_actionPerformed(e);
			}
		});
		buttonGroupJoinType.add(radioButtonCreateJoinLocal);
		panelRadioButtons.add(radioButtonCreateJoinLocal,
				new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));

		panelPanelRadioButtons.add(panelRadioButtons, null);

		dialogPanel.add(panelPanelRadioButtons, new GridBagConstraints(0, 0, 1,
				1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		// Ensure that top panel has minimum width, must be done after it has
		// been filled
		Dimension tempDimension = panelPanelRadioButtons.getMinimumSize();
		if (tempDimension.getWidth() < 200) {
			tempDimension.setSize(200, tempDimension.getHeight());
		}
		panelPanelRadioButtons.setMinimumSize(tempDimension);
		tempDimension = panelPanelRadioButtons.getPreferredSize();
		if (tempDimension.getWidth() < 200) {
			tempDimension.setSize(200, tempDimension.getHeight());
		}
		panelPanelRadioButtons.setPreferredSize(tempDimension);

		// Create list panel
		panelLocalCDs.setLayout(gridLayout1);

		listLocalCDs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listLocalCDs.addListSelectionListener(new ListSelectionListenerImpl() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				listLocalCDs_valueChanged(e);
			}
		});
		listLocalCDs.setEnabled(false);
		propagateListLocalCDs();

		scrollListLocalCDs.getViewport().setView(listLocalCDs);
		panelLocalCDs.add(scrollListLocalCDs, null);

		dialogPanel.add(panelLocalCDs, new GridBagConstraints(0, 1, 1, 1, 1.0,
				1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));

		// Ensure that list panel has minimum height, must be done after it has
		// been filled
		tempDimension = panelLocalCDs.getMinimumSize();
		if (tempDimension.getHeight() < 150) {
			tempDimension.setSize(tempDimension.getWidth(), 150);
		}
		panelLocalCDs.setMinimumSize(tempDimension);
		tempDimension = panelLocalCDs.getPreferredSize();
		if (tempDimension.getHeight() < 150) {
			tempDimension.setSize(tempDimension.getWidth(), 150);
		}
		panelLocalCDs.setPreferredSize(tempDimension);

		// Create IP (Name) and Port text field panel
		panelIPOrNameAndPort.setLayout(gridBagLayout2);

		labelIPOrName.setHorizontalAlignment(SwingConstants.RIGHT);
		labelIPOrName.setLabelFor(textFieldURLorName);
		labelIPOrName.setText("IP:");
		panelIPOrNameAndPort.add(labelIPOrName, new GridBagConstraints(0, 0, 1,
				1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 40, 0));

		textFieldURLorName.addCaretListener(new CaretListenerImpl() {
			@Override
			public void caretUpdate(CaretEvent e) {
				textFieldIPOrName_caretUpdate(e);
			}
		});
		panelIPOrNameAndPort.add(textFieldURLorName, new GridBagConstraints(1,
				0, 1, 1, 1.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		labelPort.setHorizontalAlignment(SwingConstants.RIGHT);
		labelPort.setLabelFor(textFieldPort);
		labelPort.setText("Port:");
		panelIPOrNameAndPort.add(labelPort, new GridBagConstraints(0, 1, 1, 1,
				0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 40, 0));

		textFieldPort.setDocument(JTextFieldFilter.NUMERIC_WITH_NEGATIVE);
		textFieldPort.addCaretListener(new CaretListenerImpl() {
		    @Override
			public void caretUpdate(CaretEvent e) {
				textFieldPort_caretUpdate(e);
			}
		});
		panelIPOrNameAndPort.add(textFieldPort, new GridBagConstraints(1, 1, 1,
				1, 1.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		// dialogPanel.add(panelIPOrNameAndPort, new GridBagConstraints(0, 2, 1,
		// 1, 1.0, 0.0
		// ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new
		// Insets(0, 0, 0, 0), 0, 0));

		// Create button panel
		buttonJoinOrCreate.setText("Join");
		getRootPane().setDefaultButton(buttonJoinOrCreate);
		buttonJoinOrCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				buttonJoinOrCreate_actionPerformed(e);
			}
		});
		panelButtons.add(buttonJoinOrCreate, null);

		buttonCancel.setText("Cancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				buttonCancel_actionPerformed(e);
			}
		});
		panelButtons.add(buttonCancel, null);

		dialogPanel.add(panelButtons, new GridBagConstraints(0, 3, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

		// put whole thing into dialog main panel
		getContentPane().add(dialogPanel);

		// Dimension screenSize = Toolkit.getDefaultToolkit ().getScreenSize ();
		// Dimension dialogSize = this.getPreferredSize ();
		// setLocation (screenSize.width / 2 - (dialogSize.width / 2),
		// screenSize.height / 2 - (dialogSize.height / 2));

		radioButtonJoinLocal.setSelected(true);
	}

	/**
	 * Retrieves the currents active CDs and puts them in the JList.
	 */
	private void propagateListLocalCDs() {
		Vector<String> cdVector = new Vector<String> ();
		Vector<URLDescriptor> allRunningAgents = null;
		if (LAC.ProcessInfo.lac != null) {
			allRunningAgents = LAC.ProcessInfo.lac.getRunningAgentDescriptors();

			if (allRunningAgents == null) {
				Trace.log("error", "Cannot get list of running agents from LAC");
				return;
			}

			// get only Cooperation Domains
			Pattern pattern = Pattern.compile("CooperationDomain/");

			for (URLDescriptor item : allRunningAgents) {
				// get only Cooperation Domains
				Matcher matcher = pattern
						.matcher(item.toString(agent.getURL()));

				if (matcher.find()) {
					String coopDomainName = item.getFile();

					cdHashtable.put(coopDomainName, item);
					cdVector.add(coopDomainName);
				}
			}

			listLocalCDs.setListData(cdVector);
			listLocalCDs.setEnabled(true);
		} else {

			AbstractInternalFrame.runInEventDispatchThread(new Runnable() {
				public void run() {
				    List<URLDescriptor> cdList = null;
				    Vector<String> cdVector = new Vector<String> ();
					cdList = agent.doFindInstances_sync(
							".*CooperationDomain.*|.*ControllableSociety.*")
							.getURLs();
					if (cdList != null) {
					  
					    for (URLDescriptor item : cdList) {
							cdHashtable.put(item.getFile(), item);
							cdVector.add(item.getFile());
						}

						listLocalCDs.setListData(cdVector);
						listLocalCDs.setEnabled(true);
					}
				}
			});
		}
	}

	private void radioButtonJoinLocal_actionPerformed(ActionEvent e) {
		// Set correct join mode
		joinMode = EXISTING_LOCAL;

		// Set button to join
		buttonJoinOrCreate.setText("Join");
		// getRootPane().setDefaultButton(buttonJoinOrCreate);

		// remove text fields
		dialogPanel.remove(panelIPOrNameAndPort);

		// re-add list
		dialogPanel.remove(panelLocalCDs);
		dialogPanel.add(panelLocalCDs, new GridBagConstraints(0, 1, 1, 1, 1.0,
				1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));

		// resize window
		pack();
	}

	private void radioButtonJoinRemote_actionPerformed(ActionEvent e) {
		// Set correct join mode
		joinMode = EXISTING_REMOTE;

		// Set button to join
		buttonJoinOrCreate.setText("Join");
		// getRootPane().setDefaultButton(buttonJoinOrCreate);

		// remove list
		dialogPanel.remove(panelLocalCDs);

		// re-add text fields
		dialogPanel.remove(panelIPOrNameAndPort);
		dialogPanel.add(panelIPOrNameAndPort, new GridBagConstraints(0, 2, 1,
				1, 1.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		// set IPOrName to IP
		labelIPOrName.setText("URL:");

		// save and load text field
		String tempNameText = textFieldURLorName.getText();
		String tempPortText = textFieldPort.getText();
		textFieldURLorName.setText("");
		textFieldPort.setText("");

		// resize window while text fields are empty (to be consistent)
		pack();

		if (!ipLast) {
			textFieldURLorName.setText(savedURLorNameText);
			textFieldPort.setVisible(false); // textFieldPort.setText(savedPortText);
			labelPort.setVisible(false);
			savedURLorNameText = tempNameText;
			savedPortText = tempPortText;
			ipLast = true;
		} else {
			textFieldURLorName.setText(tempNameText);
			textFieldPort.setVisible(false); // textFieldPort.setText(tempPortText);
			labelPort.setVisible(false);
		}

		// our text change may change the enabled state of the button
		updateButtonJoinOrCreateEnabled();
	}

	private void radioButtonCreateJoinLocal_actionPerformed(ActionEvent e) {
		// Set correct join mode
		joinMode = NEW_LOCAL;

		// Set button to join
		buttonJoinOrCreate.setText("Create");

		// remove list
		dialogPanel.remove(panelLocalCDs);

		// re-add text fields
		dialogPanel.remove(panelIPOrNameAndPort);
		dialogPanel.add(panelIPOrNameAndPort, new GridBagConstraints(0, 2, 1,
				1, 1.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		// set IPOrName to Name
		labelIPOrName.setText("Name:");

		// save and load text field
		textFieldPort.setVisible(true);
		labelPort.setVisible(true);
		String tempIPText = textFieldURLorName.getText();
		String tempPortText = textFieldPort.getText();
		textFieldURLorName.setText("");
		textFieldPort.setText("");

		// resize window while text fields are empty (to be consistent)
		pack();

		if (ipLast) {
			textFieldURLorName.setText(savedURLorNameText);
			textFieldPort.setText(savedPortText);
			savedURLorNameText = tempIPText;
			savedPortText = tempPortText;
			ipLast = false;
		} else {
			textFieldURLorName.setText(tempIPText);
			textFieldPort.setText(tempPortText);
		}

		// our text change may change the enabled state of the button
		updateButtonJoinOrCreateEnabled();
	}

	private void listLocalCDs_valueChanged(ListSelectionEvent e) {
		updateButtonJoinOrCreateEnabled();
	}

	private void textFieldIPOrName_caretUpdate(CaretEvent e) {
		updateButtonJoinOrCreateEnabled();
	}

	private void textFieldPort_caretUpdate(CaretEvent e) {
		updateButtonJoinOrCreateEnabled();
	}

	private void updateButtonJoinOrCreateEnabled() {
		if (joinMode == EXISTING_LOCAL) {
			buttonJoinOrCreate.setEnabled(listLocalCDs.getSelectedIndex() > -1);
		} else if (joinMode == NEW_LOCAL) {
			if (textFieldURLorName.getText().length() > 0
					&& textFieldPort.getText().length() > 0) {
				buttonJoinOrCreate.setEnabled(true);
				getRootPane().setDefaultButton(buttonJoinOrCreate);
			} else {
				buttonJoinOrCreate.setEnabled(false);
			}
		} else if (joinMode == EXISTING_REMOTE) {
			if (textFieldURLorName.getText().length() > 0) {
				buttonJoinOrCreate.setEnabled(true);
				getRootPane().setDefaultButton(buttonJoinOrCreate);
			} else {
				buttonJoinOrCreate.setEnabled(false);
			}
		}
	}

	private void buttonJoinOrCreate_actionPerformed(ActionEvent e) {
		if (joinMode == EXISTING_LOCAL) {
			URLDescriptor cdSelected;

			if (listLocalCDs.getSelectedIndex() > -1) {
				String cdStr = (String) listLocalCDs.getSelectedValue();
				cdSelected = (URLDescriptor) cdHashtable.get(cdStr);
				Status status = agent.doJoinCD(cdSelected);
				if (status.getStatusValue() != 0)
					Trace.log("error", "Bad status ("
							+ Integer.toString(status.getStatusValue())
							+ ") from join: " + status.getExplanation());
			}
		} else if (joinMode == EXISTING_REMOTE) {
			try {
				// URLDescriptor cdToJoin = new URLDescriptor
				// (textFieldURLorName.getText(), textFieldPort.getText());
				URLDescriptor cdToJoin = URLDescriptor.make(textFieldURLorName.getText());
				Status status = agent.doJoinCD(cdToJoin);
				if (status.getStatusValue() != 0)
					Trace.log("error", "Bad status ("
							+ Integer.toString(status.getStatusValue())
							+ ") from join: " + status.getExplanation());
			} catch (Exception ex) {
				Trace.log("error", "Exception during join: " + ex.toString());
			}
		} else if (joinMode == NEW_LOCAL) {
			String givenName = textFieldURLorName.getText();
			String newcdPortString = textFieldPort.getText();
			// boolean persistent = setPersistentCheck.isSelected ();
			// boolean secured = setSecuredCheck.isSelected ();
			int lacPort = agent.getURL().getLACport();
			int newAgentPortInt = -4000;

			try {
				if (newcdPortString.length() != 0)
					newAgentPortInt = Integer.parseInt(newcdPortString);
			} catch (NumberFormatException ex) {
			}

			CooperationDomain temp = null;
			try {
				ParamsMap params = new ParamsMap();
				params.put("TYPE", "casa.CooperationDomain", new SimpleString("casa.CooperationDomain"), false);
				params.put("NAME", givenName, new SimpleString(givenName), false);
				params.put("PORT", newAgentPortInt, new JavaObject(newAgentPortInt), false);
				params.put("LACPORT", lacPort, new JavaObject(lacPort), false);
				temp = new CooperationDomain(params, new BufferedAgentUI());
			} catch (Exception e1) {
				Trace.log("error", "Could not start up CooperationDomain: "
						+ e1.toString());
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
			}

			Status status = agent.doJoinCD(temp.getURL());
			if (status.getStatusValue() != 0)
				Trace.log("error", "Bad status ("
						+ Integer.toString(status.getStatusValue())
						+ ") from join: " + status.getExplanation());
		}

		// close when we are done, unless an error occured above, in which case
		// return () should have been called
		dispose();
	}

	private void buttonCancel_actionPerformed(ActionEvent e) {
		// close dialog and do nothing...
		dispose();
	}

	protected class ListSelectionListenerImpl implements ListSelectionListener {
		public ListSelectionListenerImpl() {
		}

		public void valueChanged(ListSelectionEvent e) {
		}
	}

	protected class CaretListenerImpl implements CaretListener {
		public CaretListenerImpl() {
		}

		public void caretUpdate(CaretEvent e) {
		}
	}

	private List<URLDescriptor> cdListNew = null;

	private class TimeIt extends TimerTask {
	    @Override
		public void run() {
			cdListNew = agent.getInstancesFound().getURLs();
			// if (cdListNew != null)
			// notify();
		}
	}
}