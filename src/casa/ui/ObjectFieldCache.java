package casa.ui;

import casa.CasaOption;
import casa.util.AnnotationUtil;

import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * ObjectFieldCache is responsible for keeping track of the options object,
 * the field, the original value, and the GUI elements. 
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
 * @author <a href="mailto:rkyee@ucalgary.ca">Ryan Yee</a>
 */
public class ObjectFieldCache{
	Object object;
	Field field;
	Object value;
	JComponent guiLabel;
	JComponent guiValue;
	private Collection<ObjectFieldCache>	parent; 
	
	/**
	 * Constructs a new ObjectFieldCache given its enclosing collection object,
	 * the field's containing object, the field, and the field's value.
	 * @param parent this object's containing Collection object
	 * @param object the object containing the field
	 * @param field the field to track
	 * @param value the field's value
	 */
	public ObjectFieldCache(Collection<ObjectFieldCache> parent, Object object, Field field, Object value){
		this.parent = parent;
		this.object = object;
		this.field = field;
		this.value = value;
		try {
			final CasaOption annotation = AnnotationUtil.getAnnotation(field, CasaOption.class);
			
			guiLabel = getLabelComponent(annotation);
			guiValue = getValueComponent(annotation);
			
			//set tool tip if necessary
			if (!annotation.helpText().equals(CasaOption.NONE)){
				guiLabel.setToolTipText(annotation.helpText());
				guiValue.setToolTipText(annotation.helpText());
			}
			
			//figure out if element is enabled
			if (!annotation.enabledMethod().equals(CasaOption.NONE)) {
				Method enabledMethod = object.getClass().getMethod(annotation.enabledMethod());
				guiLabel.setEnabled((Boolean) enabledMethod.invoke(object));
				guiValue.setEnabled((Boolean) enabledMethod.invoke(object));
			}
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Generates a label based on the field's type
	 * @return a label based on the field's type
	 */
	private JComponent getLabelComponent(final CasaOption annotation) {
		final JComponent ret;
		
		if(!annotation.labelText().equals(CasaOption.NONE))
			ret = new JLabel(annotation.labelText());
		else
			ret = new JLabel(field.getName());
		
		return ret;
	}

	public void setValue(Object value){
		this.value = value;
	}
	
	public void setValue(boolean b){
		this.value = b;
	}
	
	public void setValue(int i){
		this.value = i;
	}
	
	public void setLong(long l){
		this.value = l;
	}
	
	/**
	 * Flushes the currently stored value into the field. If the field is annotated with
	 * a CasaOption then an attempt is made to run its validationMethod before saving.
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public void saveData() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		final Class<?> type = field.getType();
		final CasaOption annotation = AnnotationUtil.getAnnotation(field,CasaOption.class);
		
		if(annotation!=null && !annotation.validationMethod().equals(CasaOption.NONE)){
			try {
				Method validationMethod = object.getClass().getMethod(annotation.validationMethod(), getGuiValueAsNative().getClass());
				validationMethod.invoke(object, getGuiValueAsNative());
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(type == String.class || type == int.class || type == long.class){
			final String text = getGuiValueAsString();
			if(type == String.class)
				setValue(text);
			else if(type == int.class)
				setValue(Integer.parseInt(text));
			else //long.class
				setValue(Long.parseLong(text));
		}else if(type == boolean.class){
			final boolean b = ((JCheckBox)guiValue).isSelected();
			setValue(b);
		}else
			throw new UnsupportedOperationException("Unknown type");
		
		field.set(object, value);
	}
	
	/**
	 * Invokes the postSaveMethod (if present) on a CasaOption annotated field
	 */
	public void postSaveData() {
		final CasaOption annotation = AnnotationUtil.getAnnotation(field, CasaOption.class);
		if(annotation != null && !annotation.postSaveMethod().equals(CasaOption.NONE)){
			try {
				Method postSaveMethod = object.getClass().getMethod(annotation.postSaveMethod());
				postSaveMethod.invoke(object);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Returns a debugger friendly string (field=value)
	 */
	@Override
	public String toString() {
		return String.format("%s=%s", field, value);
	}

	/**
	 * Generates a GUI component based on the type of the value.
	 * @return a GUI component based on the type of the value.
	 */
	@SuppressWarnings("unchecked")
	private JComponent getValueComponent(final CasaOption annotation) {
		try {
			Object fieldValue = field.get(object);
			final Class<?> type = field.getType();
			final JComponent ret;
			final ActionListener listener;
			
			//optionally create ActionListener
			if(!annotation.actionListenerMethod().equals(CasaOption.NONE)){
				Method actionListenerMethod = object.getClass().getMethod(annotation.actionListenerMethod(), Collection.class);
				listener = (ActionListener) actionListenerMethod.invoke(object, parent);
			}else
				listener = null;
			
			//figure out the correct GUI representation
			if (!annotation.optionsMethod().equals(CasaOption.NONE)) {
				Method optionsMethod = object.getClass().getMethod(annotation.optionsMethod());
				Collection<String> options = (Collection<String>) optionsMethod.invoke(object);
				final JComboBox comboBox = new JComboBox();
				for (String option : options)
					comboBox.addItem(option);
				if (options.contains(value))
					comboBox.setSelectedItem(value);
				comboBox.addActionListener(listener);
				ret = comboBox;
			}else if (type == String.class || type == int.class || type == long.class) {
				JTextField textField = new JTextField(fieldValue != null ? fieldValue.toString() : "");
				ret = textField;
			} else if (type == boolean.class) {
				final String label = !annotation.labelText().equals(CasaOption.NONE) ? annotation.labelText() : field.getName();
				final JCheckBox check = new JCheckBox(label);
				check.setSelected(field.getBoolean(object));
				check.addActionListener(listener);
				ret = check;
			} else
				ret = new JLabel(String.format(
						"Undefined type: %s with Value: %s", type,
						fieldValue != null ? fieldValue.toString() : "null"));
			

			
			
			
			return ret;
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Interprets the UI value of the guiValue as a string
	 * @return the UI value of the guiValue as a string
	 */
	public String getGuiValueAsString(){
		Object ret = getGuiValueAsNative();
		return ret != null ? ret.toString() : null;
	}
	
	/**
	 * Gets the UI value for guiValue
	 * @return the UI value for guiValue
	 */
	public Object getGuiValueAsNative(){
		if(JTextField.class.isInstance(guiValue))
			return ((JTextField)guiValue).getText();
		else if(JCheckBox.class.isInstance(guiValue))
			return ((JCheckBox)guiValue).isSelected(); 
		else if(JComboBox.class.isInstance(guiValue))
			return ((JComboBox)guiValue).getSelectedItem();
		return null;
	}

	/**
	 * @return the guiLabel
	 */
	public JComponent getGuiLabel() {
		return guiLabel;
	}

	/**
	 * @return the guiValue
	 */
	public JComponent getGuiValue() {
		return guiValue;
	}

	/**
	 * @return the field
	 */
	public Field getField() {
		return field;
	}
	
	/**
	 * @return the name of the field
	 */
	public String getFieldName() {
		return field.getName();
	}
}