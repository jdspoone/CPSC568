/* <p>Title: CASA Agent Infrastructure</p>
 * <p>Copyright: Copyright (c) 2003-2014, Knowledge Science Group, University of Calgary. 
 */
package casa.extensions;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;

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
 * A simple and generic description if a CASA extension (plug-in). 
 * @version 0.9
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class ExtensionDescriptor implements Comparable<ExtensionDescriptor> {
	/** the type (code, tab, or Lisp-Script) of the extension. */
	String type;
	
	/** the source file were the exension resides in, */
	File sourceFile;
	
	/**
	 * The attributes of the extension.  All keys are stored as lower case for case-insensitivity.
	 */
	Map<String, Object> attributes = new TreeMap<String, Object>();
	
	/** The index of the extension (largely irrelivant -- suffixed number used in the names of jar sections). */
	Integer index;
	
	/**
	 * Defers to {@link #ExtensionDescriptor(String, File, Integer, Map, String)} by transforming the forth parameter.
	 * @param type The type of the extension (code, tab, Lisp_Script).
	 * @param sourceFile The file in which the extension resides.
	 * @param index The index of the extension (largely irrelivant -- suffixed number used in the names of jar sections).
	 * @param attrs The attributes of the extension.
	 * @param defaultMain The main class named in the main section of the manifest of a jar file. May be null.
	 */
	public ExtensionDescriptor(String type, File sourceFile, Integer index, Attributes attrs, String defaultMain) {
		this(type, sourceFile, index, attributes2Map(attrs), defaultMain);
	}
	
	/**
	 * Builds a new ExtensionDescriptor.
	 * @param type The type of the extension (code, tab, Lisp_Script).
	 * @param sourceFile The file in which the extension resides.
	 * @param index The index of the extension (largely irrelivant -- suffixed number used in the names of jar sections).
	 * @param attrs The attributes of the extension.
	 * @param defaultMain The main class named in the main section of the manifest of a jar file. May be null.
	 */
	public ExtensionDescriptor(String type, File sourceFile, Integer index, Map<String, Object> attrs, String defaultMain) {
		assert type!=null && type.length()>0;
		assert attrs!=null;
		this.type = type;
		this.sourceFile = sourceFile;
		this.index = index;
		//attributes are saved with all keys converted to lower case.
		if (defaultMain!=null)
			put("Default-Main", defaultMain);
		for (String key: attrs.keySet()) {
			Object val = attrs.get(key);
			put(key, val);
		}
	}
	
	private static Map<String, Object> attributes2Map(Attributes attrs) {
		TreeMap<String,Object> ret = new TreeMap<String, Object>();
		for (Entry<Object,Object> attr: attrs.entrySet()) {
			ret.put(((Name)attr.getKey()).toString(), attr.getValue());
		}
		return ret;
	}
	
	/**
	 * Adds a new attribute <em>key</em> with value <em>o</em>.  Not that the key will be converted
	 * to lower case before saving.
	 * @param key The key to the newly added key/value pair.
	 * @param o The value to the newly added key/value pair.
	 */
	void put(String key, Object o) {
		key = key.toLowerCase();
		attributes.put(key, o);
	}
	
	/**
	 * Removes a key/value pair from the attribues list.  Not that the key is NOT case-senstive.
	 * @param key The key of the key/value pair in the attributes list.
	 */
	void remove(String key) {
		key = key.toLowerCase();
		attributes.remove(key);
	}
	
	/**
	 * Retrieves the value for <em>key</em> in the attribute list or null if there is not key in
	 * the attribute list.  Not that the key is NOT case-senstive.
	 * @param key The key of the key/value pair in the attributes list.
	 * @return The value for <em>key</em>, or null if the key is not in the attribute list.
	 */
	public Object get(String key) {
		return attributes.get(key.toLowerCase());
	}
	
	/**
	 * @return the source file for this extension as per the constructor.
	 */
	public File getSourceFile() {
		return sourceFile;
	}
	
	/**
	 * @return the type (code, tab, Lisp-Script) for this extension as per the constructor.
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * @return the index for this extension as per the constructor.
	 */
	public int getIndex() {
		return index;
	}
	
	/**
	 * @return the string representation of the object in Lisp notation:<br>
	 * (extension-descriptor [type] {:[key] [value]}* {:sourcefile [filename]}) 
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder("(extension-descriptor ");
		b.append(makeValueString(type));
		for (Entry<String,Object> attr: attributes.entrySet()) {
			b.append(" :").append(attr.getKey()).append(' ').append(makeValueString(attr.getValue()));
		}
		if (sourceFile!=null)
			b.append(" :sourcefile \"").append(sourceFile).append('"');
		b.append(")");
		return b.toString();
	}

	private String makeValueString(Object val) {
		if (val instanceof Boolean) 
			return ((Boolean)val)?"T":"nil";
		if (val.getClass().isPrimitive())
			return val.toString();
		return "\""+val+"\"";
	}
	
	/**
	 * Compares on the basis of 
	 * <ol>
	 * <li>type
	 * <li>sourceFilename (existing or not existing and both existing)
	 * <li>index
	 * </ol>
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ExtensionDescriptor o) {
		//type
		int ret = type.compareTo(o.type);
		if (ret!=0) 
			return ret;
		
		//sourceFile
		if (sourceFile==null && o.sourceFile!=null)
			return -1;
		if (sourceFile!=null && o.sourceFile==null)
			return 1;
		if (sourceFile!=null && o.sourceFile!=null) {
		  ret = sourceFile.compareTo(o.sourceFile);
		  if (ret!=0)
		  	return ret;
		}
		
		//index
		if (index==null && o.index!=null)
			return -1;
		if (index!=null && o.index==null)
			return 1;
		if (index!=null && o.index!=null) {
		  ret = index.compareTo(o.index);
		  if (ret!=0)
		  	return ret;
		}
		
		return 0;
	}

}
