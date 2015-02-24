/* <p>Title: CASA Agent Infrastructure</p>
 * <p>Copyright: Copyright (c) 2003-2014, Knowledge Science Group, University of Calgary. 
 */
package casa.extensions;

import casa.AbstractProcess;
import casa.abcl.Lisp;
import casa.ui.AbstractInternalFrame;
import casa.util.JarLoader;
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
 * Defines a java-code jar extension.  The class specified by the descriptor's {@link #ATTR_MAINCLASS} is simply loaded into memory.
 * Attributes in jar manifest file:
 * <ul>
 * <li>{@link Extension#ATTR_MAINCLASS} - {@value Extension#ATTR_MAINCLASS}.  Defaults to the value Main-Class in the manifest main section.
 * <li>{@link Extension#ATTR_EXTENSIONNAME} - {@value Extension#ATTR_EXTENSIONNAME}. Defaults to name (only) of the value of {@link Extension#ATTR_MAINCLASS}. 
 * </ul>
 * @version 0.9
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class CodeExtension extends Extension {

	/** This attribute contains the fully-qualified class name of the class containing the main(String[]) method implementing this extension. */
	public static final String ATTR_MAINCLASS     = "Main-Class";
	
	/**
	 * Loads the class specified by the descriptor's {@link #ATTR_MAINCLASS}. Specifically,
	 * Loads the class specified in by {@link #descriptor}.get({@link #ATTR_MAINCLASS}) by
	 * adding the source file to the classpath.  If the {@link Extension#descriptor descriptor} contains the key
	 * {@link Extension#ATTR_AUTOLOAD} then the class {@link #ATTR_MAINCLASS} will be actually
	 * loaded into memory, causing any static initialization to occur for that class.
	 * @param d
	 */
	public CodeExtension(ExtensionDescriptor d) {
		super(d);
		loadIf();
	}
	
	/**
	 * Loads the class specified in by {@link #descriptor}.get({@link #ATTR_MAINCLASS}) by
	 * adding the source file to the classpath.  If the {@link Extension#descriptor descriptor} contains the key
	 * {@link Extension#ATTR_AUTOLOAD} then the class {@link #ATTR_MAINCLASS} will be actually
	 * loaded into memory, causing any static initialization to occur for that class.
	 * This method is called from the constructor so subclasses can override this behaviour.
	 */
	protected void loadIf() {
		try {
			JarLoader.addFile(descriptor.getSourceFile());
		} catch (Throwable e) {
			Trace.log("error", "Can't load class "+(String)descriptor.get(ATTR_MAINCLASS), e);
		}
		if ((Boolean)descriptor.get(ATTR_AUTOLOAD)) {
			Lisp.loadClass(ATTR_MAINCLASS);
		}
	}
	
	/**
	 * <ul>
	 * <li> insures the {@link Extension#ATTR_MAINCLASS} exists by defaulting to the valie of Main-Class in the main section.  If we cannot
	 * default in this way an exception is thrown.
	 * <li> ensures that there is an extension name.  The default is the class name (not fully-qualified) of the class containing the main(String[]) method. 
	 * <li> calls the super's method ({@link Extension#validate()).
	 * </ul>
	 */
	@Override
	int validate() throws InvalidParameterException {
		//Main-Class
		int ret = validateString(ATTR_MAINCLASS, descriptor.get("Default-Main")==null?null:descriptor.get("Default-Main").toString());
		if (descriptor.get(ATTR_MAINCLASS)==null) {
			throw new InvalidParameterException(Trace.log("error", "Attribute 'Main-Class' is missing in jar '"+descriptor.getSourceFile()+"', manifest section '"+descriptor.getIndex()+"'.  Aborting jar processing."));
		}

		//Extension-Name
		int r = validateString(ATTR_EXTENSIONNAME, getNameFromMainClass());
		if (ret==0)
			ret = r;
		
		//autoload
		r = validateBool(ATTR_AUTOLOAD, true);
		if (ret==0)
			ret = r;
		
		r = super.validate();
		if (ret==0)
			ret = r;

		return ret;
	}

	/**
	 * This method is a no-op because CodeExtensions are automatically loaded at instantiation.
	 */
	@Override
	void load(final AbstractInternalFrame frame, final AbstractProcess agent) {
	}
	
	/**
	 * Utility method to obtain the non-fully-qualified class name from the value of the #ATTR_MAINCLASS attribute.
	 * @return The name of the class.
	 */
	protected String getNameFromMainClass() {
		Object o = descriptor.get(ATTR_MAINCLASS);
		if (o!=null) {
			String path = descriptor.get(ATTR_MAINCLASS).toString();
			String p[] = path.split("\\.");
			return p[p.length-1];
		}
		else {
			int index = descriptor.getIndex();
			return descriptor.getType()+(index<0?"":Integer.toString(index));
		}
	}
	
}
