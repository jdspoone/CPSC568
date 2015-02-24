package org.ksg.casa;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import javax.swing.JSeparator;
import javax.swing.JScrollPane;
import javax.swing.BoxLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.SpringLayout;
import java.awt.Component;
import javax.swing.Box;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.ScrollPaneConstants;
import java.awt.GridLayout;

public class CASAPreferencesDialog extends JDialog {
	
  public static void main(String[] args) {
		try {
			CASAPreferencesDialog dialog = new CASAPreferencesDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	private BindingGroup m_bindingGroup;

	private CASA cASA = new CASA();

	private JTextField rootJTextField;

	private JTextField LACdefaultportJTextField;

	private JTextField putPreferenceIfAbsentJTextField;

	private JTextField routerJTextField;

	private JCheckBox dieOnLACExitDefaultJCheckBox;

	public CASAPreferencesDialog(org.ksg.casa.CASA newCASA) {
		this();
		setCASA(newCASA);
	}

	public CASAPreferencesDialog() {
		getContentPane().setBounds(new Rectangle(400, 200, 300, 400));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{415, 0};
		gridBagLayout.rowHeights = new int[]{254, 39, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
																						
																						JScrollPane scrollPane_1 = new JScrollPane();
																						scrollPane_1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
																						GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
																						gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
																						gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
																						gbc_scrollPane_1.gridx = 0;
																						gbc_scrollPane_1.gridy = 0;
																						getContentPane().add(scrollPane_1, gbc_scrollPane_1);
																						
																						JPanel panel = new JPanel();
																						scrollPane_1.setViewportView(panel);
																								GridBagLayout gbl_panel = new GridBagLayout();
																								gbl_panel.columnWidths = new int[]{198, 198, 0};
																								gbl_panel.rowHeights = new int[]{38, 38, 38, 38, 38, 0};
																								gbl_panel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
																								gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
																								panel.setLayout(gbl_panel);
																										
																												JLabel rootLabel = new JLabel("Root:");
																												GridBagConstraints gbc_rootLabel = new GridBagConstraints();
																												gbc_rootLabel.anchor = GridBagConstraints.EAST;
																												gbc_rootLabel.fill = GridBagConstraints.VERTICAL;
																												gbc_rootLabel.insets = new Insets(0, 0, 5, 5);
																												gbc_rootLabel.gridx = 0;
																												gbc_rootLabel.gridy = 0;
																												panel.add(rootLabel, gbc_rootLabel);
																										
																												rootJTextField = new JTextField();
																												GridBagConstraints gbc_rootJTextField = new GridBagConstraints();
																												gbc_rootJTextField.fill = GridBagConstraints.BOTH;
																												gbc_rootJTextField.insets = new Insets(0, 0, 5, 0);
																												gbc_rootJTextField.gridx = 1;
																												gbc_rootJTextField.gridy = 0;
																												panel.add(rootJTextField, gbc_rootJTextField);
																										
																												JLabel LACdefaultportLabel = new JLabel("LACdefaultport:");
																												GridBagConstraints gbc_LACdefaultportLabel = new GridBagConstraints();
																												gbc_LACdefaultportLabel.anchor = GridBagConstraints.EAST;
																												gbc_LACdefaultportLabel.fill = GridBagConstraints.VERTICAL;
																												gbc_LACdefaultportLabel.insets = new Insets(0, 0, 5, 5);
																												gbc_LACdefaultportLabel.gridx = 0;
																												gbc_LACdefaultportLabel.gridy = 1;
																												panel.add(LACdefaultportLabel, gbc_LACdefaultportLabel);
																								
																										LACdefaultportJTextField = new JTextField();
																										GridBagConstraints gbc_LACdefaultportJTextField = new GridBagConstraints();
																										gbc_LACdefaultportJTextField.fill = GridBagConstraints.BOTH;
																										gbc_LACdefaultportJTextField.insets = new Insets(0, 0, 5, 0);
																										gbc_LACdefaultportJTextField.gridx = 1;
																										gbc_LACdefaultportJTextField.gridy = 1;
																										panel.add(LACdefaultportJTextField, gbc_LACdefaultportJTextField);
																												
																														JLabel putPreferenceIfAbsentLabel = new JLabel("PutPreferenceIfAbsent:");
																														GridBagConstraints gbc_putPreferenceIfAbsentLabel = new GridBagConstraints();
																														gbc_putPreferenceIfAbsentLabel.anchor = GridBagConstraints.EAST;
																														gbc_putPreferenceIfAbsentLabel.fill = GridBagConstraints.VERTICAL;
																														gbc_putPreferenceIfAbsentLabel.insets = new Insets(0, 0, 5, 5);
																														gbc_putPreferenceIfAbsentLabel.gridx = 0;
																														gbc_putPreferenceIfAbsentLabel.gridy = 2;
																														panel.add(putPreferenceIfAbsentLabel, gbc_putPreferenceIfAbsentLabel);
																										
																												putPreferenceIfAbsentJTextField = new JTextField();
																												GridBagConstraints gbc_putPreferenceIfAbsentJTextField = new GridBagConstraints();
																												gbc_putPreferenceIfAbsentJTextField.fill = GridBagConstraints.BOTH;
																												gbc_putPreferenceIfAbsentJTextField.insets = new Insets(0, 0, 5, 0);
																												gbc_putPreferenceIfAbsentJTextField.gridx = 1;
																												gbc_putPreferenceIfAbsentJTextField.gridy = 2;
																												panel.add(putPreferenceIfAbsentJTextField, gbc_putPreferenceIfAbsentJTextField);
																										
																												JLabel routerLabel = new JLabel("Router:");
																												GridBagConstraints gbc_routerLabel = new GridBagConstraints();
																												gbc_routerLabel.anchor = GridBagConstraints.EAST;
																												gbc_routerLabel.fill = GridBagConstraints.VERTICAL;
																												gbc_routerLabel.insets = new Insets(0, 0, 5, 5);
																												gbc_routerLabel.gridx = 0;
																												gbc_routerLabel.gridy = 3;
																												panel.add(routerLabel, gbc_routerLabel);
																										
																												routerJTextField = new JTextField();
																												GridBagConstraints gbc_routerJTextField = new GridBagConstraints();
																												gbc_routerJTextField.fill = GridBagConstraints.BOTH;
																												gbc_routerJTextField.insets = new Insets(0, 0, 5, 0);
																												gbc_routerJTextField.gridx = 1;
																												gbc_routerJTextField.gridy = 3;
																												panel.add(routerJTextField, gbc_routerJTextField);
																								
																										JLabel dieOnLACExitDefaultLabel = new JLabel("DieOnLACExitDefault:");
																										GridBagConstraints gbc_dieOnLACExitDefaultLabel = new GridBagConstraints();
																										gbc_dieOnLACExitDefaultLabel.anchor = GridBagConstraints.EAST;
																										gbc_dieOnLACExitDefaultLabel.insets = new Insets(0, 0, 0, 5);
																										gbc_dieOnLACExitDefaultLabel.gridx = 0;
																										gbc_dieOnLACExitDefaultLabel.gridy = 4;
																										panel.add(dieOnLACExitDefaultLabel, gbc_dieOnLACExitDefaultLabel);
																												
																														dieOnLACExitDefaultJCheckBox = new JCheckBox();
																														GridBagConstraints gbc_dieOnLACExitDefaultJCheckBox = new GridBagConstraints();
																														gbc_dieOnLACExitDefaultJCheckBox.fill = GridBagConstraints.HORIZONTAL;
																														gbc_dieOnLACExitDefaultJCheckBox.gridx = 1;
																														gbc_dieOnLACExitDefaultJCheckBox.gridy = 4;
																														panel.add(dieOnLACExitDefaultJCheckBox, gbc_dieOnLACExitDefaultJCheckBox);
																						
																						JPanel panel_1 = new JPanel();
																						panel_1.setSize(new Dimension(0, 20));
																						GridBagConstraints gbc_panel_1 = new GridBagConstraints();
																						gbc_panel_1.fill = GridBagConstraints.HORIZONTAL;
																						gbc_panel_1.gridx = 0;
																						gbc_panel_1.gridy = 1;
																						getContentPane().add(panel_1, gbc_panel_1);
																						
																						JButton btnNewButton = new JButton("Cancel");
																						btnNewButton.addActionListener(new ActionListener() {
																							public void actionPerformed(ActionEvent arg0) {
																								dispose();
																							}
																						});
																						panel_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
																						
																						JLabel lblNewLabel = new JLabel("");
																						panel_1.add(lblNewLabel);
																						lblNewLabel.setIcon(new ImageIcon(CASAPreferencesDialog.class.getResource("/images/customGraphics/casa32.png")));
																						panel_1.add(btnNewButton);
																						
																						JButton btnNewButton_1 = new JButton("Save");
																						btnNewButton_1.addActionListener(new ActionListener() {
																							public void actionPerformed(ActionEvent e) {
																								setCASA(cASA,false);
																								System.out.println("Saving...");
																								dispose();
																							}
																						});
																						panel_1.add(btnNewButton_1);

		if (cASA != null) {
			m_bindingGroup = initDataBindings();
		}
		
		setBounds(200, 300, 415, 271);
	}

	protected BindingGroup initDataBindings() {
		BeanProperty<org.ksg.casa.CASA, java.lang.String> rootProperty = BeanProperty
				.create("root");
		BeanProperty<javax.swing.JTextField, java.lang.String> textProperty = BeanProperty
				.create("text");
		AutoBinding<org.ksg.casa.CASA, java.lang.String, javax.swing.JTextField, java.lang.String> autoBinding = Bindings
				.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE,
						cASA, rootProperty, rootJTextField, textProperty);
		autoBinding.bind();
		//
		BeanProperty<org.ksg.casa.CASA, java.lang.Integer> lACdefaultportProperty = BeanProperty
				.create("LACdefaultport");
		BeanProperty<javax.swing.JTextField, java.lang.String> textProperty_1 = BeanProperty
				.create("text");
		AutoBinding<org.ksg.casa.CASA, java.lang.Integer, javax.swing.JTextField, java.lang.String> autoBinding_1 = Bindings
				.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE,
						cASA, lACdefaultportProperty, LACdefaultportJTextField,
						textProperty_1);
		autoBinding_1.bind();
		//
		BeanProperty<org.ksg.casa.CASA, java.lang.String> putPreferenceIfAbsentProperty = BeanProperty
				.create("putPreferenceIfAbsent");
		BeanProperty<javax.swing.JTextField, java.lang.String> textProperty_2 = BeanProperty
				.create("text");
		AutoBinding<org.ksg.casa.CASA, java.lang.String, javax.swing.JTextField, java.lang.String> autoBinding_2 = Bindings
				.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE,
						cASA, putPreferenceIfAbsentProperty,
						putPreferenceIfAbsentJTextField, textProperty_2);
		autoBinding_2.bind();
		//
		BeanProperty<org.ksg.casa.CASA, java.lang.String> routerProperty = BeanProperty
				.create("router");
		BeanProperty<javax.swing.JTextField, java.lang.String> textProperty_3 = BeanProperty
				.create("text");
		AutoBinding<org.ksg.casa.CASA, java.lang.String, javax.swing.JTextField, java.lang.String> autoBinding_3 = Bindings
				.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE,
						cASA, routerProperty, routerJTextField,
						textProperty_3);
		autoBinding_3.bind();
		//
		BeanProperty<org.ksg.casa.CASA, java.lang.Boolean> dieOnLACExitDefaultProperty = BeanProperty
				.create("dieOnLACExitDefault");
		BeanProperty<javax.swing.JCheckBox, java.lang.Boolean> selectedProperty = BeanProperty
				.create("selected");
		AutoBinding<org.ksg.casa.CASA, java.lang.Boolean, javax.swing.JCheckBox, java.lang.Boolean> autoBinding_4 = Bindings
				.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE,
						cASA, dieOnLACExitDefaultProperty,
						dieOnLACExitDefaultJCheckBox, selectedProperty);
		autoBinding_4.bind();
		//
		BindingGroup bindingGroup = new BindingGroup();
		bindingGroup.addBinding(autoBinding);
		bindingGroup.addBinding(autoBinding_1);
		bindingGroup.addBinding(autoBinding_2);
		bindingGroup.addBinding(autoBinding_3);
		bindingGroup.addBinding(autoBinding_4);
		//
		return bindingGroup;
	}

	public org.ksg.casa.CASA getCASA() {
		return cASA;
	}

	public void setCASA(org.ksg.casa.CASA newCASA) {
		setCASA(newCASA, true);
	}

	public void setCASA(
			org.ksg.casa.CASA newCASA, boolean update) {
		cASA = newCASA;
		if (update) {
			if (m_bindingGroup != null) {
				m_bindingGroup.unbind();
				m_bindingGroup = null;
			}
			if (cASA != null) {
				m_bindingGroup = initDataBindings();
			}
		}
	}
}
