/* <p>Title: CASA Agent Infrastructure</p>
 * <p>Copyright: Copyright (c) 2003-2014, Knowledge Science Group, University of Calgary. 
 */
package casa.extensions;

import casa.AbstractProcess;
import casa.ui.AbstractInternalFrame;
import casa.util.Trace;

import java.security.InvalidParameterException;

/**
 *
 * <p><b>Title:</b> CASA Agent Infrastructure</p>
 * <p><b>Copyright: Copyright (c) 2003-2014, Knowledge Science Group, University of Calgary.</b> 
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  
 * The  Knowledge Science Group makes no representations about the suitability
 * of  this software for any purpose.  It is provided "as is" without express
 * or implied warranty.</p>
 * <p><b>Company:</b> Knowledge Science Group, Department of Computer Science, University of Calgary</p>
 * This class is the top-level class for various kinds of CASA extensions.
 * @version 0.9
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public abstract class Extension implements Comparable<Extension> {
	/** This attribute contains a Class object of the most general agent class for which this extension object is applicable. */
	public static final String ATTR_AGENTTYPE     = "agenttype";
	
	/** The frame must be a object of this type (or it's subtypes) */
	public static final String ATTR_FRAMETYPE = "frameType";

	/** This attribute is true iff it this extension should be loaded automatically at startup. */
	public static final String ATTR_AUTOLOAD      = "autoload";
	
	/** This attribute contains the name that will be used to refer to this extension in menus, etc. */
	public static final String ATTR_EXTENSIONNAME = "Extension-Name";
	
	/** This attribute contains a short doc string to be used in dialogs, mouse-overs, etc */
	public static final String ATTR_DOC           = "doc";
	
	/** The type name for a pure java code (from a jar) extension */
	public static final String TYPE_CODE          = "code";
	
	/** The type name for a java code extension that will act as a tab pane in a {@link AbstractProcessWindow}. */ 
	public static final String TYPE_TAB           = "tab";
	
	/** The type name for Lisp script extension; the file will be executed when the Tools|LispScript|[ATTR_EXTENSIONNAME] menu item is selected. */
	public static final String TYPE_LISPSCRIPT    = "Lisp-Script";
	
	/** The object that contains a lot of the detail about this extension, including the ATTR_* attributes. */
	protected ExtensionDescriptor descriptor;
	
	/**
	 * Every Extension object must have a descriptor. 
	 * @param d The descriptor for this extension.
	 */
	public Extension(ExtensionDescriptor d) {
		descriptor = d;
	}
	
	/**
	 * Validate the descriptor with respect to this extension.<p>
	 * This method:
	 * <ul>
	 * <li> checks that the <b>agentType</b> field is a legitimate agent path and replaces
	 * the value with the actual class object.  Default: {@link casa.AbstractProcess}.
	 * <li> checks that the <b>frameType</b> field is a legitimate frame path and replaces
	 * the value with the actual class object.  Default: {@link casa.AbstractInternalFrame}.
	 * <li> checks that the <b>autoload</b> field is either "true" or "false" and replaces it with a boolean.  The default is "false".
	 * <li> ensures that there is an extension name.  The default is the class name (not fully-qualified) of the class containing the main(String[]) method. 
	 * </ul>
	 * @return 0 if all is good, some other integer if there were problems.
	 * @throws InvalidParameterException if a fatal error happens.
	 */
	int validate() throws InvalidParameterException {
		int ret = 0;
		
		//agentType
		ret = validateClass(ATTR_AGENTTYPE, AbstractProcess.class);
		
		//Extension-Name
		int r = validateString(ATTR_EXTENSIONNAME, descriptor.getSourceFile().getName());
		if (ret==0)
			ret = r;
		
		//autoLoad
		r = validateBool(ATTR_AUTOLOAD, false);
		if (ret==0)
			ret = r;
		
		// frameType
		r = validateClass(ATTR_FRAMETYPE, AbstractInternalFrame.class);
		if (ret == 0)
			ret = r;

		return ret;
	}
	
	/**
	 * Utility method to validate a String attribute in the {@link #descriptor} 
	 * @param key The key in the descriptor to validate.
	 * @param defaultString The default to assign to the key is it's value is missing.
	 * @return 0 for "OK" or "fixed it", 1 for "unexpected type, fixed it".
	 */
	protected int validateString(String key, String defaultString) {
		Object o = descriptor.get(key);
		if (o==null) {
			descriptor.put(key, defaultString);
			return 0;
		}
		if (!(o instanceof String)) {
			Trace.log("warning", "Invalid object type '"+o+"' in the '"+key+"' attribute in jar '"+descriptor.getSourceFile()+"', manifest section '"+descriptor.getIndex()+"'.  Defaulting to "+o.toString()+".");
			descriptor.put(key, o.toString());
			return 1;
		}
		return 0;
	}

	/**
	 * Utility method to validate a boolean attribute in the {@link #descriptor} 
	 * @param key The key in the descriptor to validate.
	 * @param defaultBool The default to assign to the key is it's value is missing or otherwise invalid.
	 * @return 0 for "OK" or "fixed it", 1 for "unexpected type type or value, fixed it (to default)".
	 */
	protected int validateBool(String key, Boolean defaultBool) {
		Object o = descriptor.get(key);
		if (o==null) {
			descriptor.put(key, defaultBool);
			return 0;
		}
		if (!(o instanceof Boolean)) {
			if (o instanceof String) {
				if ("true".equalsIgnoreCase((String)o))
					descriptor.put(key, true);
				else if ("false".equalsIgnoreCase((String)o))
					descriptor.put(key, false);
				else {
					Trace.log("warning", "Invalid boolean identifier '"+o+"' in the '"+key+"' attribute in jar '"+descriptor.getSourceFile()+"', manifest section '"+descriptor.getIndex()+"'.  Defaulting to "+defaultBool+".");
					descriptor.put(key, defaultBool);
					return 1;
				}
			}
			else {
				Trace.log("warning", "Invalid object type '"+o+"' in the '"+key+"' attribute in jar '"+descriptor.getSourceFile()+"', manifest section '"+descriptor.getIndex()+"'.  Defaulting to "+defaultBool+".");
				descriptor.put(key, defaultBool);
				return 1;
			}
		}
		return 0;
	}

	/**
	 * Utility method to validate a class attribute in the {@link #descriptor} 
	 * @param key The key in the descriptor to validate.
	 * @param defaultBool The default to assign to the key is it's value is missing or otherwise invalid.
	 * @return 0 for "OK" or "fixed it", 1 for "unexpected type type or value, fixed it (to default)".
	 */
	protected int validateClass(String key, Class defaultClass) {
		Object o = descriptor.get(key);
		if (o==null) {
			if (defaultClass!=null) {
				descriptor.put(key, defaultClass);
				return 0;
			}
		}
		else if (!(o instanceof Class)) {
			if (o instanceof String) {
				try {
					Class<?> cls = Class.forName((String)o);
					descriptor.put(key, cls);
					return 0;
				} catch (Exception e) {
					Trace.log("warning", "Invalid class name '"+o+"' in the '"+key+"' attribute in jar '"+descriptor.getSourceFile()+"', manifest section '"+descriptor.getIndex()+(defaultClass==null?"'. Ignoring it.":(". Using default: "+defaultClass)), e);
					if (defaultClass==null)
						descriptor.remove(key);
					else 
						if (defaultClass!=null) {
							descriptor.put(key, defaultClass);
							return 0;
						}
					return 1;
				}
			}
			else {
				Trace.log("warning", "Invalid object type '"+o+"' in the '"+key+"' attribute in jar '"+descriptor.getSourceFile()+"', manifest section '"+descriptor.getIndex()+(defaultClass==null?"'. Ignoring it.":(". Using default: "+defaultClass)));
				if (defaultClass==null)
					descriptor.remove(key);
				else {
					if (defaultClass!=null) {
						descriptor.put(key, defaultClass);
						return 0;
					}
				}
				return 1;
			}
		}
		return 0;
	}
	
	/**
	 * @return this object as Lisp code "(extension [className] (descriptor ...))"
	 */
	public String toString () {
		StringBuilder b = new StringBuilder("(extension \"");
		b.append(getClass().getSimpleName()).append("\" ").append(descriptor.toString()).append(")");
		return b.toString();
	}

	/**
	 * Defers to {@link ExtensionDescriptor#compareTo(ExtensionDescriptor)} using {@link #descriptor}
	 */
	@Override
	public int compareTo(Extension o) {
		return descriptor.compareTo(o.descriptor);
	}

	/**
	 * Initialize this extension.
	 * @param frame The frame this extension should attach itself to. (May be null).
	 * @param agent The agent this extension should associate itself with. (May be null).
	 */
	abstract void load(final AbstractInternalFrame frame, final AbstractProcess agent);
	
}
