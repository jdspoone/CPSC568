/**
 * <p>
 * Title: CASA Agent Infrastructure
 * </p>
 * <p>
 * Description:
 * </p>
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
 * <p>
 * Company: Knowledge Science Group, University of Calgary
 * </p>
 * 
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
package casa;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Direct or indirect fields in agent classes marked with &#64;CasaOption are
 * available for viewing and modification by the casa lisp operator (options
 * ...). Most objects saved are of primitive types, but more complex structures
 * can be modified by using "&#64;CasaOptions(recurse=true)" which will cause
 * (options ...) to recursively look into the structure for fields marked with
 * &#64;CasaOption.
 * 
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CasaOption {
	boolean recurse() default false;

	/**
	 * Fields can be grouped into categories. By default, a field will have no
	 * group.
	 * 
	 * @return the group this field is part of
	 */
	String group() default NONE;

	/**
	 * The name of the method that returns valid options. This method must
	 * return a list of strings.
	 * 
	 * @return name of the method that returns valid options
	 */
	String optionsMethod() default NONE;

	/**
	 * The name of the method that indicates whether this current option is
	 * enabled or disabled. This method must accept a collection of
	 * ObjectFieldCache objects, and return a boolean.
	 * 
	 * TODO: this may not be sufficient. Needs to run on actionListener and
	 * update GUI elements.
	 * 
	 * @return name of the method that indicates whether this current option is
	 *         enabled or disabled
	 */
	String enabledMethod() default NONE;

	/**
	 * The name of a method that will return an action listener this field's
	 * corresponding GUI. This method must take a Collection of ObjectFieldCache
	 * objects as a parameter.
	 * 
	 * @return The name of a method that will return an action listener this
	 *         field's corresponding GUI.
	 */
	String actionListenerMethod() default NONE;

	/**
	 * The name of the method that will be called after a successful save.
	 * 
	 * @return name of the method that will be called after a successful save.
	 */
	String postSaveMethod() default NONE;

	/**
	 * @return The pretty name to display in the options dialog.
	 */
	String labelText() default NONE;

	/**
	 * @return the text to display as help (e.g. in a tool tip box).
	 */
	String helpText() default NONE;

	/**
	 * The name of the method for field validation. This method must take an
	 * object as an argument and return void. An exception must be thrown on bad
	 * input; the message of the exception should describe the error (e.g.
	 * throw new IllegalArgumentException("Expected an integer")).
	 * 
	 * @return the name of the method for field validation.
	 */
	String validationMethod() default NONE;
	
	/**
	 * @return the relative order to sort the GUI widget for this field in a
	 * group.
	 */
	int groupOrder() default 0;

	/**
	 * Indicates that the default behaviour is to be used (none).
	 */
	public static String	NONE	= "";
}
