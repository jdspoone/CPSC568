package casa.system;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JPasswordField;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import javax.swing.JTextArea;
import java.awt.Component;
import java.awt.Color;
import javax.swing.UIManager;
import java.awt.Rectangle;
import org.eclipse.wb.swing.FocusTraversalOnArray;
import javax.swing.ImageIcon;

public class PasswordDialog extends JDialog {

	private BindingGroup m_bindingGroup;

	private JPanel m_contentPane;

	private casa.system.Sudo sudo = new casa.system.Sudo();

	private JPasswordField passwordJTextField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			PasswordDialog dialog = new PasswordDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String command = null;
	private JButton btnNewButton;
	private JButton btnCancel;
	private JTextArea txtrX;
	public PasswordDialog(String command) {
		this();
		this.command = command;
	}
	/**
	 * Create the dialog.
	 */
	public PasswordDialog() {
		setBounds(100, 100, 450, 150);
		m_contentPane = new JPanel();
		setContentPane(m_contentPane);
		m_contentPane.setLayout(null);

		JLabel passwordLabel = new JLabel("Password:");
		passwordLabel.setBounds(16, 69, 63, 16);
		m_contentPane.add(passwordLabel);

		passwordJTextField = new JPasswordField();
		passwordJTextField.setBounds(84, 63, 361, 28);
		passwordJTextField.setFocusCycleRoot(true);
		m_contentPane.add(passwordJTextField);
		
		btnNewButton = new JButton("OK");
		btnNewButton.setBounds(142, 92, 75, 29);
		m_contentPane.add(btnNewButton);
		getRootPane().setDefaultButton(btnNewButton);
		
		btnCancel = new JButton("Cancel");
		btnCancel.setBounds(229, 92, 86, 29);
		m_contentPane.add(btnCancel);
		
		txtrX = new JTextArea();
		txtrX.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		txtrX.setEditable(false);
		txtrX.setBounds(84, 6, 360, 51);
		m_contentPane.add(txtrX);
		txtrX.setBackground(UIManager.getColor("InternalFrame.background"));
		txtrX.setAlignmentX(Component.LEFT_ALIGNMENT);
		txtrX.setText("Enter the admin password for sudo operation:");
		
		JLabel lblNewLabel = new JLabel("New label");
		lblNewLabel.setIcon(new ImageIcon(PasswordDialog.class.getResource("/images/customGraphics/casa64.png")));
		lblNewLabel.setBounds(9, 6, 70, 64);
		m_contentPane.add(lblNewLabel);
		m_contentPane.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{passwordJTextField, btnNewButton, btnCancel}));
		setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{passwordJTextField, m_contentPane, btnNewButton, btnCancel}));
		btnCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				dispose();
			}
		});
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (sudo.sudo2(command)>=0)
					dispose();
			}
		});

		if (sudo != null) {
			m_bindingGroup = initDataBindings();
		}
	}

	public casa.system.Sudo getSudo() {
		return sudo;
	}

	public void setSudo(
			casa.system.Sudo newSudo) {
		setSudo(newSudo, true);
	}

	public void setSudo(
			casa.system.Sudo newSudo,
			boolean update) {
		sudo = newSudo;
		if (update) {
			if (m_bindingGroup != null) {
				m_bindingGroup.unbind();
				m_bindingGroup = null;
			}
			if (sudo != null) {
				m_bindingGroup = initDataBindings();
			}
		}
	}
	protected BindingGroup initDataBindings() {
		BeanProperty<Sudo, String> sudoBeanProperty = BeanProperty.create("password");
		BeanProperty<JPasswordField, String> jPasswordFieldBeanProperty = BeanProperty.create("text");
		AutoBinding<Sudo, String, JPasswordField, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, sudo, sudoBeanProperty, passwordJTextField, jPasswordFieldBeanProperty);
		autoBinding.bind();
		//
		BeanProperty<Sudo, String> sudoBeanProperty_1 = BeanProperty.create("passwordMessage");
		BeanProperty<JTextArea, String> jTextAreaBeanProperty = BeanProperty.create("text");
		AutoBinding<Sudo, String, JTextArea, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, sudo, sudoBeanProperty_1, txtrX, jTextAreaBeanProperty);
		autoBinding_1.bind();
		//
		BindingGroup bindingGroup = new BindingGroup();
		//
		bindingGroup.addBinding(autoBinding);
		bindingGroup.addBinding(autoBinding_1);
		return bindingGroup;
	}
}
