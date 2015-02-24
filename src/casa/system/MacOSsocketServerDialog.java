package casa.system;

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
import javax.swing.JTextArea;
import java.awt.Component;
import javax.swing.JButton;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.ELProperty;

//import sun.tools.tree.ThisExpression;
import sun.tools.*;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.UIManager;

class MacOSsocketServerDialog extends JDialog {
	
	private BindingGroup m_bindingGroup;

	private JPanel m_contentPane;

	private casa.system.MacOSsocketServerInstaller macOSsocketServerInstaller = new casa.system.MacOSsocketServerInstaller();

	private JTextField userJTextField;

	private JTextField groupJTextField;

	private JTextField portJTextField;

	private JTextField workingDirectoryJTextField;
	private JTextArea txtrEnterTheDetails;
	private JPanel panel;
	private JButton btnEnable;
	private JButton btnDisable;
	private JLabel lblKeepAlive;
	private JCheckBox keepAliveJCheckBox;
	
	public static String getJarFor(Class cls) {
		String ret = cls.getProtectionDomain().getCodeSource().getLocation().getPath();
		try {
			ret = URLDecoder.decode(ret,"UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		return ret;
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		String osName = System.getProperty("os.name");
		if (!osName.contains("Mac OS X")) {
			String osArch = System.getProperty("os.arch");
			String osVersion = System.getProperty("os.version");
			String msg = "MacOSsocketServerInstaller only works on Mac OS X\nCurrent system is: os.name="+osName+"; os.arch="+osArch+"; os.version="+osVersion;
			System.out.println(msg);
			return;
		}
		String user = System.getProperty("user.name");
		if ("root".equals(user)) {
			String defaultUser=null;
			for (String arg:args) {
				if (arg.length()>6 && arg.startsWith("-user="))
					defaultUser=arg.substring(6);
			}
			System.out.println("user="+defaultUser);
			try {
				MacOSsocketServerDialog dialog = new MacOSsocketServerDialog();
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dialog.macOSsocketServerInstaller.setDefaultUser(defaultUser); 
				dialog.initDataBindings(); //reinitialize them to work with the change in defaultUser.
				dialog.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			String jarPath = getJarFor(MacOSsocketServerDialog.class);
			String propPath = getJarFor(org.jdesktop.beansbinding.Property.class);
			String saxPath = getJarFor(org.apache.xerces.parsers.SAXParser.class);
			String command = "java -cp "
					+jarPath+":"
					+saxPath+":"
					+propPath+" casa.system.MacOSsocketServerDialog -user="+System.getProperty("user.name");
			System.out.println("sudo'ing: "+command);
			Sudo sudo = new Sudo();
			sudo.sudo(command);
		}

	}

	/**
	 * Create the dialog.
	 */
	public MacOSsocketServerDialog() {
		setBounds(100, 100, 450, 285);
		m_contentPane = new JPanel();
		m_contentPane.setAlignmentX(Component.RIGHT_ALIGNMENT);
		setContentPane(m_contentPane);
		//
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 124, 304, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 1.0, 1.0E-4 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0E-4 };
		m_contentPane.setLayout(gridBagLayout);
		
		txtrEnterTheDetails = new JTextArea();
		txtrEnterTheDetails.setMargin(new Insets(5, 3, 3, 0));
		txtrEnterTheDetails.setBackground(UIManager.getColor("InternalFrame.background"));
		txtrEnterTheDetails.setText("Enter the details for the daemon LAC");
		GridBagConstraints gbc_txtrEnterTheDetails = new GridBagConstraints();
		gbc_txtrEnterTheDetails.insets = new Insets(0, 0, 5, 0);
		gbc_txtrEnterTheDetails.gridwidth = 2;
		gbc_txtrEnterTheDetails.fill = GridBagConstraints.BOTH;
		gbc_txtrEnterTheDetails.gridx = 0;
		gbc_txtrEnterTheDetails.gridy = 0;
		m_contentPane.add(txtrEnterTheDetails, gbc_txtrEnterTheDetails);

		JLabel userLabel = new JLabel("User:");
		userLabel.setToolTipText("The user name under which the deamon should run.  Note that you should probably avoid using 'root' and pick some user that is not quite so vulnerable if the port should be compromised.");
		userLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		GridBagConstraints labelGbc_0 = new GridBagConstraints();
		labelGbc_0.anchor = GridBagConstraints.EAST;
		labelGbc_0.insets = new Insets(5, 5, 5, 5);
		labelGbc_0.gridx = 0;
		labelGbc_0.gridy = 1;
		m_contentPane.add(userLabel, labelGbc_0);

		userJTextField = new JTextField();
		GridBagConstraints componentGbc_0 = new GridBagConstraints();
		componentGbc_0.insets = new Insets(5, 0, 5, 0);
		componentGbc_0.fill = GridBagConstraints.HORIZONTAL;
		componentGbc_0.gridx = 1;
		componentGbc_0.gridy = 1;
		m_contentPane.add(userJTextField, componentGbc_0);

		JLabel groupLabel = new JLabel("Group:");
		groupLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		GridBagConstraints labelGbc_1 = new GridBagConstraints();
		labelGbc_1.anchor = GridBagConstraints.EAST;
		labelGbc_1.insets = new Insets(5, 5, 5, 5);
		labelGbc_1.gridx = 0;
		labelGbc_1.gridy = 2;
		m_contentPane.add(groupLabel, labelGbc_1);

		groupJTextField = new JTextField();
		GridBagConstraints componentGbc_1 = new GridBagConstraints();
		componentGbc_1.insets = new Insets(5, 0, 5, 0);
		componentGbc_1.fill = GridBagConstraints.HORIZONTAL;
		componentGbc_1.gridx = 1;
		componentGbc_1.gridy = 2;
		m_contentPane.add(groupJTextField, componentGbc_1);

		JLabel portLabel = new JLabel("Port:");
		portLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		GridBagConstraints labelGbc_2 = new GridBagConstraints();
		labelGbc_2.anchor = GridBagConstraints.EAST;
		labelGbc_2.insets = new Insets(5, 5, 5, 5);
		labelGbc_2.gridx = 0;
		labelGbc_2.gridy = 3;
		m_contentPane.add(portLabel, labelGbc_2);

		portJTextField = new JTextField();
		GridBagConstraints componentGbc_2 = new GridBagConstraints();
		componentGbc_2.insets = new Insets(5, 0, 5, 0);
		componentGbc_2.fill = GridBagConstraints.HORIZONTAL;
		componentGbc_2.gridx = 1;
		componentGbc_2.gridy = 3;
		m_contentPane.add(portJTextField, componentGbc_2);
		
		lblKeepAlive = new JLabel("Keep Alive");
		GridBagConstraints gbc_lblRestart = new GridBagConstraints();
		gbc_lblRestart.anchor = GridBagConstraints.EAST;
		gbc_lblRestart.insets = new Insets(0, 0, 5, 5);
		gbc_lblRestart.gridx = 0;
		gbc_lblRestart.gridy = 4;
		m_contentPane.add(lblKeepAlive, gbc_lblRestart);
		
		keepAliveJCheckBox = new JCheckBox("\n");
		GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
		gbc_chckbxNewCheckBox.anchor = GridBagConstraints.WEST;
		gbc_chckbxNewCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxNewCheckBox.gridx = 1;
		gbc_chckbxNewCheckBox.gridy = 4;
		m_contentPane.add(keepAliveJCheckBox, gbc_chckbxNewCheckBox);

		JLabel workingDirectoryLabel = new JLabel("WorkingDirectory:");
		workingDirectoryLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		GridBagConstraints labelGbc_4 = new GridBagConstraints();
		labelGbc_4.anchor = GridBagConstraints.EAST;
		labelGbc_4.insets = new Insets(5, 5, 5, 5);
		labelGbc_4.gridx = 0;
		labelGbc_4.gridy = 5;
		m_contentPane.add(workingDirectoryLabel, labelGbc_4);

		workingDirectoryJTextField = new JTextField();
		GridBagConstraints componentGbc_4 = new GridBagConstraints();
		componentGbc_4.insets = new Insets(5, 0, 5, 0);
		componentGbc_4.fill = GridBagConstraints.HORIZONTAL;
		componentGbc_4.gridx = 1;
		componentGbc_4.gridy = 5;
		m_contentPane.add(workingDirectoryJTextField, componentGbc_4);
		
		panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridwidth = 2;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 6;
		m_contentPane.add(panel, gbc_panel);
		
		btnDisable = new JButton("Disable");
		btnDisable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				macOSsocketServerInstaller.disable();
				dispose();
			}
		});
		btnDisable.setActionCommand("");
		panel.add(btnDisable);
		
		btnEnable = new JButton("Enable");
		btnEnable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				macOSsocketServerInstaller.enable();
				dispose();
			}
		});
		btnEnable.setActionCommand("");
		panel.add(btnEnable);

		if (macOSsocketServerInstaller != null) {
			m_bindingGroup = initDataBindings();
		}
	}

	public casa.system.MacOSsocketServerInstaller getMacOSsocketServerInstaller() {
		return macOSsocketServerInstaller;
	}

	public void setMacOSsocketServerInstaller(
			casa.system.MacOSsocketServerInstaller newMacOSsocketServerInstaller) {
		setMacOSsocketServerInstaller(newMacOSsocketServerInstaller, true);
	}

	public void setMacOSsocketServerInstaller(
			casa.system.MacOSsocketServerInstaller newMacOSsocketServerInstaller,
			boolean update) {
		macOSsocketServerInstaller = newMacOSsocketServerInstaller;
		if (update) {
			if (m_bindingGroup != null) {
				m_bindingGroup.unbind();
				m_bindingGroup = null;
			}
			if (macOSsocketServerInstaller != null) {
				m_bindingGroup = initDataBindings();
			}
		}
	}
	protected BindingGroup initDataBindings() {
		BeanProperty<MacOSsocketServerInstaller, String> userProperty = BeanProperty.create("user");
		BeanProperty<JTextField, String> textProperty = BeanProperty.create("text");
		AutoBinding<MacOSsocketServerInstaller, String, JTextField, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, macOSsocketServerInstaller, userProperty, userJTextField, textProperty);
		autoBinding.bind();
		//
		BeanProperty<MacOSsocketServerInstaller, String> groupProperty = BeanProperty.create("group");
		BeanProperty<JTextField, String> textProperty_1 = BeanProperty.create("text");
		AutoBinding<MacOSsocketServerInstaller, String, JTextField, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, macOSsocketServerInstaller, groupProperty, groupJTextField, textProperty_1);
		autoBinding_1.bind();
		//
		BeanProperty<MacOSsocketServerInstaller, Integer> portProperty = BeanProperty.create("port");
		BeanProperty<JTextField, String> textProperty_2 = BeanProperty.create("text");
		AutoBinding<MacOSsocketServerInstaller, Integer, JTextField, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, macOSsocketServerInstaller, portProperty, portJTextField, textProperty_2);
		autoBinding_2.bind();
		//
		BeanProperty<MacOSsocketServerInstaller, String> workingDirectoryProperty = BeanProperty.create("workingDirectory");
		BeanProperty<JTextField, String> textProperty_3 = BeanProperty.create("text");
		AutoBinding<MacOSsocketServerInstaller, String, JTextField, String> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, macOSsocketServerInstaller, workingDirectoryProperty, workingDirectoryJTextField, textProperty_3);
		autoBinding_4.bind();
		//
		BeanProperty<MacOSsocketServerInstaller, Boolean> macOSsocketServerInstallerBeanProperty = BeanProperty.create("keepAlive");
		BeanProperty<JCheckBox, Boolean> selectedProperty = BeanProperty.create("selected");
		AutoBinding<MacOSsocketServerInstaller, Boolean, JCheckBox, Boolean> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, macOSsocketServerInstaller, macOSsocketServerInstallerBeanProperty, keepAliveJCheckBox, selectedProperty);
		autoBinding_3.bind();
		//
		BeanProperty<MacOSsocketServerInstaller, String> macOSsocketServerInstallerBeanProperty_1 = BeanProperty.create("dialogMessage");
		BeanProperty<JTextArea, String> jTextAreaBeanProperty = BeanProperty.create("text");
		AutoBinding<MacOSsocketServerInstaller, String, JTextArea, String> autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ, macOSsocketServerInstaller, macOSsocketServerInstallerBeanProperty_1, txtrEnterTheDetails, jTextAreaBeanProperty);
		autoBinding_6.bind();
		//
		BeanProperty<MacOSsocketServerInstaller, Boolean> macOSsocketServerInstallerBeanProperty_2 = BeanProperty.create("plistExists");
		BeanProperty<JButton, Boolean> jButtonBeanProperty = BeanProperty.create("enabled");
		AutoBinding<MacOSsocketServerInstaller, Boolean, JButton, Boolean> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ, macOSsocketServerInstaller, macOSsocketServerInstallerBeanProperty_2, btnDisable, jButtonBeanProperty);
		autoBinding_5.bind();
		//
		BeanProperty<MacOSsocketServerInstaller, String> macOSsocketServerInstallerBeanProperty_3 = BeanProperty.create("enableString");
		BeanProperty<JButton, String> jButtonBeanProperty_1 = BeanProperty.create("text");
		AutoBinding<MacOSsocketServerInstaller, String, JButton, String> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ, macOSsocketServerInstaller, macOSsocketServerInstallerBeanProperty_3, btnEnable, jButtonBeanProperty_1);
		autoBinding_7.bind();
		//
		BindingGroup bindingGroup = new BindingGroup();
		//
		bindingGroup.addBinding(autoBinding);
		bindingGroup.addBinding(autoBinding_1);
		bindingGroup.addBinding(autoBinding_2);
		bindingGroup.addBinding(autoBinding_4);
		bindingGroup.addBinding(autoBinding_3);
		bindingGroup.addBinding(autoBinding_6);
		bindingGroup.addBinding(autoBinding_5);
		bindingGroup.addBinding(autoBinding_7);
		return bindingGroup;
	}
}
