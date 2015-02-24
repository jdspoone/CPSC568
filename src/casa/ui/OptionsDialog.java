package casa.ui;

import casa.CasaOption;
import casa.ProcessOptions;
import casa.util.AnnotationUtil;
import casa.util.Pair;
import casa.util.PairComparable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.border.TitledBorder;

import org.ksg.casa.CASA;

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
 */

public class OptionsDialog extends JDialog {
	/**
	 * Used by serialization... use Eclipse to generate a new ID if you change
	 * the structure.
	 */
	private static final long	serialVersionUID	= 1L;
	JPanel						jPanel3				= new JPanel();
	JButton						buttonOK			= new JButton();
	JButton						buttonCancel		= new JButton();
	boolean						updated				= false;
	Vector<ObjectFieldCache>    optionsValueCache	= new Vector<ObjectFieldCache>();
	ProcessOptions				options;
	JComponent					lastOptionComponent = null;

	public OptionsDialog() throws HeadlessException {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public OptionsDialog(Frame owner, String title, boolean modal,
			ProcessOptions options) throws HeadlessException {
		super(owner, title, modal);
		this.options = options;
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		setCasaOptions(options, title);
	}

	public boolean display() {
		updated = false;
		pack();
		setVisible(true);
		return updated;
	}
	
	private final void groupify(Map<String, ArrayList<JComponent>> groups, Set<JComponent> rootComponents, ObjectFieldCache cache){
		CasaOption annotation = AnnotationUtil.getAnnotation(cache.field, CasaOption.class);
		if(annotation != null && !annotation.group().equals(CasaOption.NONE)){
			String[] hiearchy = annotation.group().split("/");
			StringBuffer path = new StringBuffer();
			if(!rootComponents.contains(hiearchy[0] + "/")){
				JPanel panel = new JPanel();
				panel.setBorder(new TitledBorder(hiearchy[0]));
			}
			for(String hier : hiearchy){
				path.append(hier);
				path.append("/");
				
				
			}
		}
	}
	
	/**
	 * Generates a panel based on a collection of
	 * {@link casa.ui.ObjectFieldCache}s.
	 * 
	 * @param options
	 *            the {@link casa.ui.ObjectFieldCache}s to panelize
	 * @return a panel representation of the {@link casa.ui.ObjectFieldCache}s
	 */
	private static final JPanel generatePanelFromOptions(Collection<ObjectFieldCache> options){
		ArrayList<Pair<JComponent, JComponent>> rows = new ArrayList<Pair<JComponent,JComponent>>();
		for(ObjectFieldCache cache : options) {
			JComponent label = cache.getGuiLabel();
			if (label instanceof JLabel && "Trace tags".equalsIgnoreCase(((JLabel)label).getText())) {
				String help = label.getToolTipText();
				String tags = CASA.getPreference("knownTraceTags", "", 0);
				help = help.replaceFirst("__", tags);
				label.setToolTipText(help);
				cache.getGuiValue().setToolTipText(help);
			}
			rows.add(new Pair<JComponent, JComponent>(cache.getGuiLabel(), cache.getGuiValue()));
		}
		
		return generatePanelFromComponentPairs(rows);
	}
	
	/**
	 * Generates a Nx2 panel GUI for a collection of pairs of
	 * {@link javax.swing.JComponent}s. Pairs that have a single member (where
	 * one of the members is null) will span both columns.
	 * 
	 * @param rows
	 *            the collection of pair of {@link javax.swing.JComponent}s to
	 *            generate a panel for
	 * @return a panel GUI containing the rows of {@link javax.swing.JComponent}s
	 */
	private static final JPanel generatePanelFromComponentPairs(Collection<Pair<JComponent, JComponent>> rows){
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		SequentialGroup horizontalColumns = layout.createSequentialGroup();
		SequentialGroup vertical = layout.createSequentialGroup();
		
		ParallelGroup labelColumn = layout.createParallelGroup(GroupLayout.Alignment.TRAILING);
		ParallelGroup valueColumn = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		ParallelGroup spanColumn = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		
		for(Pair<JComponent, JComponent> pair: rows){
			if(pair.getSecond() != null && !JCheckBox.class.isInstance(pair.getSecond())){
				labelColumn.addComponent(pair.getFirst());
				valueColumn.addComponent(pair.getSecond());
				vertical.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(pair.getFirst())
						.addComponent(pair.getSecond()));
			}else if(pair.getSecond() == null){
				spanColumn.addComponent(pair.getFirst());
				vertical.addComponent(pair.getFirst());
			}else{
				spanColumn.addComponent(pair.getSecond());
				vertical.addComponent(pair.getSecond());
			}
		}
		horizontalColumns.addGroup(labelColumn).addGroup(valueColumn);
		
		layout.setHorizontalGroup(layout.createParallelGroup().addGroup(horizontalColumns).addGroup(spanColumn));
		layout.setVerticalGroup(vertical);
		
		panel.setLayout(layout);
		
		return panel;
	}
	
	/**
	 * Given a list of {@link casa.ui.ObjectFieldCache} objects this method will
	 * generate a grouped panel GUI. (i.e objects marked with a group via
	 * {@link casa.CasaOption} will be grouped).
	 * 
	 * @param options
	 *            the collection of {@link casa.ui.ObjectFieldCache} objects to
	 *            generate a GUI for.
	 * @return a grouped GUI of the collection of
	 *         {@link casa.ui.ObjectFieldCache} objects
	 */
	private static final JPanel generateGroupedPanel(Collection<ObjectFieldCache> options){
		ArrayList<Pair<JComponent, JComponent>> pairs = new ArrayList<Pair<JComponent,JComponent>>();
		Iterator<ObjectFieldCache> it = options.iterator();
		
		while(it.hasNext()){
			ObjectFieldCache cache = it.next();
			CasaOption annotation = AnnotationUtil.getAnnotation(cache.getField(), CasaOption.class);
			//parse this group
			if(annotation != null && !annotation.group().equals(CasaOption.NONE)){
				ArrayList<ObjectFieldCache> group = new ArrayList<ObjectFieldCache>();
				group.add(cache);
				String groupName = annotation.group();
				while(it.hasNext()){
					cache = it.next();
					annotation = AnnotationUtil.getAnnotation(cache.getField(), CasaOption.class);
					if(annotation != null && annotation.group().equals(groupName)){
						group.add(cache);
					}else{
						JPanel panel = generatePanelFromOptions(group);
						panel.setBorder(new TitledBorder(groupName));
						pairs.add(new Pair<JComponent, JComponent>(panel,null));
						
						//parse next group
						if(annotation != null){
							groupName = annotation.group();
							group = new ArrayList<ObjectFieldCache>();
							group.add(cache);
						}else{ // add this single pair
							pairs.add(new Pair<JComponent, JComponent>(cache.getGuiLabel(), cache.getGuiValue()));
							break;
						}
					}
				}
				
				//add last group
				JPanel panel = generatePanelFromOptions(group);
				panel.setBorder(new TitledBorder(groupName));
				pairs.add(new Pair<JComponent, JComponent>(panel,null));
			}else
				pairs.add(new Pair<JComponent, JComponent>(cache.getGuiLabel(), cache.getGuiValue()));
		}
		
		return generatePanelFromComponentPairs(pairs);
	}

	/**
	 * Uses introspection to generate a panel based on an options object. A
	 * field will have a UI generated if it has been annotated with
	 * &#64;CasaOption.
	 * 
	 * @param object
	 *            the object to introspect for &#64;CasaOption annotated fields
	 * @param panelTitle
	 *            is the name to use in the tab
	 */
	public void setCasaOptions(Object object, String panelTitle) {
		optionsValueCache.clear();
		HashMap<String, ArrayList<JComponent>> groups = new HashMap<String, ArrayList<JComponent>>();
		HashSet<JComponent> rootComponents = new HashSet<JComponent>();
		
		for(Field field : AnnotationUtil.getAnnotatedFields(object.getClass(), CasaOption.class)){
			try {
				ObjectFieldCache cache = new ObjectFieldCache(optionsValueCache, object, field, field.get(object));
				optionsValueCache.add(cache);
				groupify(groups, rootComponents, cache);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Collections.sort(optionsValueCache, new Comparator<ObjectFieldCache>() {
			public int compare(ObjectFieldCache o1, ObjectFieldCache o2) {
				final String name1 = o1.getFieldName();
				final String name2 = o2.getFieldName();
				final CasaOption annotation1 = AnnotationUtil.getAnnotation(o1.getField(), CasaOption.class);
				final CasaOption annotation2 = AnnotationUtil.getAnnotation(o2.getField(), CasaOption.class);
				final String group1 = annotation1 != null ? annotation1.group() : CasaOption.NONE;
				final String group2 = annotation2 != null ? annotation2.group() : CasaOption.NONE;
				final int order1 = annotation1 != null ? annotation1.groupOrder() : 0;
				final int order2 = annotation2 != null ? annotation2.groupOrder() : 0;
				final int c1 = group1.compareTo(group2);
				final int c2 = name1.compareTo(name2);
				final int c3 = order1 - order2;
				return c1 != 0 ? c1 : (c3 == 0 ? c2 : c3);
			}
		});
		
		JScrollPane gui = new JScrollPane(generateGroupedPanel(optionsValueCache));
		if(lastOptionComponent!=null)
			this.getContentPane().remove(lastOptionComponent);
		this.getContentPane().add(gui, BorderLayout.CENTER);
		lastOptionComponent = gui;
	}

	private void jbInit() throws Exception {
		buttonOK.addActionListener(new OptionsDialog_buttonOK_actionAdapter(
				this));
		buttonOK.setMaximumSize(new Dimension(51, 26));
		buttonCancel
				.addActionListener(new OptionsDialog_buttonCancel_actionAdapter(
						this));
		this.getContentPane().setBackground(SystemColor.control);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this
				.setTitle("Options for "
						+ ((options == null) ? "unknown" : options.getAgent()
								.getName()));
		buttonOK.setText("OK");
		buttonCancel.setText("Cancel");
		jPanel3.add(buttonOK, null);
		jPanel3.add(buttonCancel, null);
		this.getContentPane().add(jPanel3, BorderLayout.SOUTH);
	}

	void buttonOK_actionPerformed(ActionEvent e) {
		boolean passed = false;
		// Try to save data
		for(ObjectFieldCache cache : optionsValueCache)
			try {
				cache.saveData();
				passed = true;
			} catch (IllegalArgumentException e1) {
				JOptionPane.showMessageDialog(this, String.format("Field: %s\nDoesn't recognize value: %s\nExpected: %s", cache.getField().getName(), cache.getGuiValueAsString(), cache.getField().getType()));
			} catch (IllegalAccessException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				JOptionPane.showMessageDialog(this, String.format("Field: %s\nDoesn't recognize value: %s\nExpected: %s\nHint:\n\t%s", cache.getField().getName(), cache.getGuiValueAsString(), cache.getField().getType(), e1.getCause().getMessage()));
			}
			
		// If they all saved... then run post save methods
		for(ObjectFieldCache cache : optionsValueCache)
			cache.postSaveData();
		
		if(passed){
			updated = true;
			dispose();
		}
	}

	void buttonCancel_actionPerformed(ActionEvent e) {
		dispose();
	}
}

class OptionsDialog_buttonOK_actionAdapter implements
		java.awt.event.ActionListener {
	OptionsDialog	adaptee;

	OptionsDialog_buttonOK_actionAdapter(OptionsDialog adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.buttonOK_actionPerformed(e);
	}
}

class OptionsDialog_buttonCancel_actionAdapter implements
		java.awt.event.ActionListener {
	OptionsDialog	adaptee;

	OptionsDialog_buttonCancel_actionAdapter(OptionsDialog adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.buttonCancel_actionPerformed(e);
	}
}
