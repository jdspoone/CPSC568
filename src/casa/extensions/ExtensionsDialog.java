package casa.extensions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class ExtensionsDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private final JList extensionList;
	private JTable attributesTable;
	private JPanel extensionDetails;
	private ExtensionDescriptor allDescriptors[];
	private JTextField textField_Type;
	private JTextField textField_File;
	private JTextField textField_Index;
	private AbstractTableModel tableModel;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ExtensionsDialog dialog = new ExtensionsDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public ExtensionsDialog() {
		setTitle("CASA Extensions");
		allDescriptors = ExtensionLoader.get().getAllExtensionDescriptors();
		setBounds(100, 100, 450, 395);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{450, 0};
		gridBagLayout.rowHeights = new int[]{330, 39, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		GridBagConstraints gbc_contentPanel = new GridBagConstraints();
		gbc_contentPanel.weightx = 0.5;
		gbc_contentPanel.gridheight = 2;
		gbc_contentPanel.weighty = 1.0;
		gbc_contentPanel.anchor = GridBagConstraints.NORTH;
		gbc_contentPanel.fill = GridBagConstraints.BOTH;
		gbc_contentPanel.insets = new Insets(0, 0, 5, 0);
		gbc_contentPanel.gridx = 0;
		gbc_contentPanel.gridy = 0;
		JSplitPane splitPane = new JSplitPane();
		getContentPane().add(splitPane, gbc_contentPanel);
		splitPane.setDividerSize(3);
		splitPane.setPreferredSize(new Dimension(244, 320));
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		{
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setMinimumSize(new Dimension(23, 40));
			scrollPane.setPreferredSize(new Dimension(4, 60));
			splitPane.setLeftComponent(scrollPane);
			{
				extensionList = new JList(new AbstractListModel() {
					private static final long serialVersionUID = 1L;
					@Override
					public int getSize() {
						return allDescriptors.length;
					}
					@Override
					public Object getElementAt(int index) {
						ExtensionDescriptor ed = allDescriptors[index]; 
						String ret = (String)ed.get("Extension-Name");
						if (ed.get("doc")!=null)
							ret += " - "+ed.get("doc");
						return ret; 
					}
				});
				extensionList.setToolTipText("The extensions installed in this CASA system.  Select one to display the details below.");
				extensionList.addListSelectionListener(new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						int index = extensionList.getSelectedIndex();
						if (index<0) {
							getTextField_Type().setText("");
							getTextField_File().setText("");
							getTextField_Index().setText("");
						}
						else {
							ExtensionDescriptor ed = allDescriptors[index];
							getTextField_Type().setText(ed.getType());
							getTextField_File().setText(ed.getSourceFile().toString());
							getTextField_Index().setText(Integer.toString(ed.getIndex()));
							tableModel.fireTableDataChanged();
						}
					}
				});
				extensionList.setPreferredSize(new Dimension(0, 60));

				scrollPane.setViewportView(extensionList);
			}
		}
		extensionDetails = new JPanel();
		extensionDetails.setBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(128, 128, 128)));
		extensionDetails.setPreferredSize(new Dimension(0, 0));
		splitPane.setRightComponent(extensionDetails);
		extensionDetails.setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			extensionDetails.add(panel, BorderLayout.NORTH);
			panel.setLayout(new FormLayout(new ColumnSpec[] {
					FormFactory.RELATED_GAP_COLSPEC,
					FormFactory.DEFAULT_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("default:grow"),},
					new RowSpec[] {
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,}));
			{
				JLabel lblType = new JLabel("Type:");
				lblType.setHorizontalAlignment(SwingConstants.TRAILING);
				panel.add(lblType, "2, 2, right, default");
			}
			{
				textField_Type = new JTextField();
				textField_Type.setEditable(false);
				textField_Type.setToolTipText("The type of the extension. The type specifies how the extension is handed based on the key/value pairs below.");
				panel.add(textField_Type, "4, 2, fill, default");
				textField_Type.setColumns(10);
			}
			{
				JLabel lblSourceFile = new JLabel("Source file:");
				lblSourceFile.setHorizontalAlignment(SwingConstants.TRAILING);
				panel.add(lblSourceFile, "2, 4, right, default");
			}
			{
				textField_File = new JTextField();
				textField_File.setEditable(false);
				textField_File.setToolTipText("The file (usually a .jar file) where this extension resides.");
				panel.add(textField_File, "4, 4, fill, default");
				textField_File.setColumns(10);
			}
			{
				JLabel lblIndex = new JLabel("Index:");
				lblIndex.setHorizontalAlignment(SwingConstants.TRAILING);
				panel.add(lblIndex, "2, 6, right, default");
			}
			{
				textField_Index = new JTextField();
				textField_Index.setEditable(false);
				textField_Index.setToolTipText("The index of the extension in the souce file. Only used to distinquish like entries.");
				panel.add(textField_Index, "4, 6, fill, default");
				textField_Index.setColumns(10);
			}
		}
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setToolTipText("The key/value pairs that dictate how the extension is handled.  The source of this information is the manifest file in the jar file.");
		extensionDetails.add(scrollPane);
		{
			tableModel = new AbstractTableModel() {

				private static final long serialVersionUID = 1L;

				@Override
				public boolean isCellEditable(int rowIndex, int columnIndex) {
					return false;
				}

				@Override
				public Object getValueAt(int rowIndex, int columnIndex) {
					int index = extensionList.getSelectedIndex();
					if (index==-1)
						return null;
					ExtensionDescriptor desc = allDescriptors[index];
					Set<Entry<String, Object>> entries = desc.attributes.entrySet();
					int i = 0;
					for (Entry<String, Object> entry: entries) {
						if (rowIndex==i++) {
							return columnIndex==0?entry.getKey():entry.getValue();
						}
					}
					 fireTableCellUpdated(rowIndex, columnIndex);
					 return null;
				}

				@Override
				public int getRowCount() {
					int index = extensionList.getSelectedIndex();
					if (index==-1)
						return 0;
					return allDescriptors[index].attributes.size();
				}

				@Override
				public String getColumnName(int columnIndex) {
					return columnIndex==0?"Key":"Value";
				}

				@Override
				public int getColumnCount() {
					return 2;
				}

				@Override
				public Class<?> getColumnClass(int columnIndex) {
					return String.class;
				}

			};

			attributesTable = new JTable(tableModel);

		}
		attributesTable.setToolTipText("The key/value pairs that dictate how the extension is handled.  The source of this information is the manifest file in the jar file.");
		extensionList.setSelectedIndex(0);
		attributesTable.invalidate();
		scrollPane.setViewportView(attributesTable);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setMaximumSize(new Dimension(32767, 30));
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			GridBagConstraints gbc_buttonPane = new GridBagConstraints();
			gbc_buttonPane.fill = GridBagConstraints.HORIZONTAL;
			gbc_buttonPane.anchor = GridBagConstraints.SOUTH;
			gbc_buttonPane.gridx = 0;
			gbc_buttonPane.gridy = 2;
			getContentPane().add(buttonPane, gbc_buttonPane);
			{
				JButton okButton = new JButton("Dismiss");
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
	}

	protected JPanel getExtensionDetails() {
		return extensionDetails;
	}
	protected JTable getAttributesTable() {
		return attributesTable;
	}
	protected JTextField getTextField_Index() {
		return textField_Index;
	}
	protected JTextField getTextField_File() {
		return textField_File;
	}
	protected JTextField getTextField_Type() {
		return textField_Type;
	}
}
