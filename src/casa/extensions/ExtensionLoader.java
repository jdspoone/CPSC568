/* <p>Title: CASA Agent Infrastructure</p>
 * <p>Copyright: Copyright (c) 2003-2014, Knowledge Science Group, University of Calgary. 
 */
package casa.extensions;

import casa.AbstractProcess;
import casa.ui.AbstractInternalFrame;
import casa.util.CASAUtil;
import casa.util.Pair;
import casa.util.Trace;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
 * This class is responsible for discovering all extensions at application startup time, and then
 * acting as a repository for extension object at run time.
 * @version 0.9
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class ExtensionLoader {
	
	/** the set of all known extension objects. */
	private Set<Extension> extensions = new TreeSet<Extension>();
	
	/** The singleton ExtensionLoader object. */
	private static ExtensionLoader instance = new ExtensionLoader();
	
	/**
	 * Access to the singleton object.
	 * @return The singleton ExtnsionLoader object.
	 */
	static ExtensionLoader get() {
		return instance;
	}
	
	/**
	 * This constructor is private. The singleton ExtensionLoader object is constructed during static initialization.
	 */
	private ExtensionLoader() {
		processCandidateFiles();
	}
	
	/**
	 * At application initialization, search out all extension files (files ending in ".lisp" or ".jar") residing 
	 * in ~/.casa/extensions or the /extensions/ directory in the application jar file.  These files are then read
	 * to create one or more extension object form each of the files.  At the end creating all the extension objects,
	 * each extension object is given the opportunity to initialize globally by calling its 
	 * {@link Extension#load(AbstractInternalFrame, AbstractProcess)} method with both parameters set to null.
	 * @return The list of all files processed.
	 */
	private File[] processCandidateFiles() {
		//from the file system
		Vector<File> files = new Vector<File>();
		File dir = new File(System.getProperty("user.home") + File.separator + ".casa"+ File.separator + "extensions");
		if (dir!=null && dir.isDirectory()) {
			for (File file: getFilesFromDir(dir, null)) {
				String name = file.getName();
				if (name.endsWith(".jar")) {
					processJarFile(file);
					files.add(file);
				}
				else if (name.endsWith(".lisp")) {
						processLispFile(file, null);
						files.add(file);
				}
			}
		}

		//from the CASA jar file (lisp only at this point)
		CodeSource src = getClass().getProtectionDomain().getCodeSource();
		if (src != null) {
			URL jar = src.getLocation();
			Trace.log("extensions", "jar="+jar);	
			if (jar.toString().endsWith(".jar")) {
				try {
					ZipInputStream zip = new ZipInputStream(jar.openStream());
					while(true) {
						ZipEntry e = zip.getNextEntry();
						if (e == null)
							break;
						String name = e.getName();
						if (name.startsWith("extensions/")) {
							Trace.log("extensions", "Found extension "+name);
							String zipName = e.getName();
							String path = "jar:file:"+jar.getPath()+"!/"+zipName;
							Trace.log("extensions2", "path="+path);
							File zipFile = new File(path);
							if (path.endsWith(".lisp")) {
								File file = new File(path);
								processLispFile(file, zip);
								files.add(file);
							}
							files.add(zipFile);
						}
					}
				} catch (Throwable e) {
					Trace.log("error", "Failed to find resources in application jar file", e);
				}
			}
			else { //this is for situation where were are running from Eclipse
				try {
					dir = new File(jar.toURI()+"/extensions");
					if (dir!=null && dir.isDirectory()) {
						for (File file: getFilesFromDir(dir, null)) 
							files.add(file);
					}
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		//Give all the extensions a chance to execute globally if they want
		for (Extension e: extensions) {
			e.load(null, null);
		}
		
		//Accumulate all the files to return a list of them.
		File ret[] = new File[files.size()];
		int i = 0;
		for (File f: files) {
			ret[i++] = f;
		}
		return ret;
	}

	/**
	 * Returns a list of all files in the directory <em>dir</em> with the file extension <em>fileExtension</em>.
	 * @param dir The directory to search.
	 * @param fileExtension The file extension. If it does not have a prefix ".", then one is added before searching.
	 * @return
	 */
	private File[] getFilesFromDir(File dir, String fileExtension) {
		try {
			if (dir.isDirectory()) {
				if (fileExtension!=null && !fileExtension.startsWith("."))
					fileExtension = "."+fileExtension;
				final String fx = fileExtension;
				File[] files = dir.listFiles(new FileFilter() {
					@Override
					public boolean accept(File arg0) {
						return fx==null?true:arg0.getName().toLowerCase().endsWith(fx);
					}
				});
				return files;
			}
		}
		catch (Throwable e) {
			Trace.log("error", "ExtensionLoader.<init>(): unexpected exception in looking for jars in "+dir, e);
		}
		return new File[]{};
	}

	/**
	 * Simple test of this class. Merely prints out the result of the {@link #toString()} method on the singleton instance.
	 * @param args Ignored.
	 */
	public static void main(String args[]) {
  	System.out.println(instance);
  }
  
	/**
	 * Reads a lisp file to determine the attributes it should have as an Extension.  Attributes in the lisp file 
	 * are on lines starting with a semicolon (the Lisp comment character) and immediately followed by a colon (";:").
	 * The first whitespace-delimited token after the ";:" is taken as an attribute name (key), and the remainder of
	 * the line is taken as the key's value.<p>
	 * The parameters seem redundant, but in the case of reading within a jar, the caller may keep the stream open and
	 * pass it to this method in the second (<em>stream</em>) parameter.  If the second parameter is null, a file stream
	 * will be created from the first (<em>file</em>) parameter.
	 * <br>The created Extension object is added to {@link #extensions}.
	 * @param file The file to read.
	 * @param stream The steam for the <em>file</em>.  May be null.
	 */
	private void processLispFile(File file, InputStream stream) {
		boolean shouldClose = false;
		Map<String, Object> attrs = new TreeMap<String, Object>();
		if (stream==null)
			try {
				stream = new FileInputStream(file);
				shouldClose = true;
			} catch (FileNotFoundException e1) {
				Trace.log("error", "ExtensionLoader.processLispFile(): Could not open file "+file, e1);
				return;
			}
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(stream));
			while (reader.ready()) {
				String line = reader.readLine().trim();
				if (line.startsWith(";:")) {
					line = line.substring(2).trim();
					int pos = CASAUtil.scanForWhiteSpace(line, 0);
					if (pos>0) {
						String key = line.substring(0,pos);
						String val = line.substring(pos+1).trim();
						attrs.put(key, val);
					}
				}
			}
			ExtensionDescriptor extDesc = new ExtensionDescriptor(Extension.TYPE_LISPSCRIPT, file, -1, attrs, null);
			LispScriptExtension ext = new LispScriptExtension(extDesc);
			ext.validate();
			extensions.add(ext);
			if (reader!=null && shouldClose) {
				reader.close();
			}
		} catch (FileNotFoundException e) {
			Trace.log("error", "ExtensionLoader.processLispFile(): Could not open file "+stream, e);
		} catch (IOException e) {
			Trace.log("error", "ExtensionLoader.processLispFile(): Unexpected  "+stream, e);
		}
	}

	/**
	 * Processes a jar file for one or more Extension objects.  Extension objects are defined in a Jar 
	 * file in the manifest file under separate sections for each Extension object.  The section name
	 * should begin with "tab" or "code" and may be followed by a string of digits.  (The digits are only
	 * used to make the section names unique.)  The attributes of for the ExtensionDescriptors are specified
	 * in the obvious way: as attributes in the manifest.  
	 * <br>Any Extension object found and instantiated are
	 * added to {@link #extensions}.
	 * @param file
	 */
	private void processJarFile(File file) {
		JarFile jarFile;
		try {
			jarFile = new JarFile(file);
		} catch (IOException e) {
			Trace.log("error", "ExtensionLoader.processFile(): Cannot open jar file: "+file, e);
			return;
		}
		Manifest manifest;
		try {
			manifest = jarFile.getManifest();
		} catch (IOException e) {
			Trace.log("error", "ExtensionLoader.processFile(): Jar file "+file+" is missing or has illegal manifest", e);
			return;
		}

		Map<String, Attributes> entries = manifest.getEntries();

		//if there are no entries (sections) in the manifest, this must be a plain extension (like an agent) that can be called dynamically
		if (entries.size()==0) {
			try {
				casa.util.JarLoader.addFile(file);
				ExtensionDescriptor d = new ExtensionDescriptor("code", file, -1, manifest.getMainAttributes(), null);
				extensions.add(new CodeExtension(d));
			} catch (IOException e) {
				Trace.log("error", "ExtensionLoader.processFile(): Failed to load plain jar file "+file, e);
			}
		}
		else {
			//process the entries (sections) of the manifest.
			for (String entryName: entries.keySet()) {
				Pair<String, Integer> type = parseEntryName(entryName); //separate the prefix (alph-num not ending in a digit) from the suffix (digits).
				if (type.getFirst()==null || type.getFirst().length()<1) {
					Trace.log("error", "ExtensionLoader.processFile(): Cannot process-non alpha extension description '"+entryName+"' in file "+file);
					continue;
				}
				Attributes mainAttrs = manifest.getMainAttributes();
				String defaultMain = mainAttrs.getValue(CodeExtension.ATTR_MAINCLASS);
				Attributes attrs = manifest.getAttributes(entryName);
				ExtensionDescriptor extDesc = new ExtensionDescriptor(type.getFirst(), file, type.getSecond(), attrs, defaultMain);

				//determine a subclass (if available) of Extension specific to this extension type, and generate a new object from it.
				Class<Extension> cls = getClassForType(extDesc.getType());
				Extension ext;
				try {
					Constructor<Extension> cons = cls.getConstructor(ExtensionDescriptor.class);
					{
						try {
							ext = cons.newInstance(extDesc);
						} catch (Exception e) {
							Trace.log("error", "ExtensionLoader.processFile(): Cannot create new instance of class "+cls.getCanonicalName()+" for '"+entryName+"' in file "+file, e);
							continue;
						}
					}
				} catch (Exception e) {
					Trace.log("error", "ExtensionLoader.processFile(): Cannot find constructor for class "+cls.getCanonicalName()+" for '"+entryName+"' in file "+file, e);
					continue;
				}

				//allow the new object of <:Extension to validate itself 
				ext.validate();
				//store the extension object
				extensions.add(ext);
			}
		}
  }
  
	/**
	 * 
	 * @param type The type (usually "code", "tab", or "LispScript").
	 * @return Returns a class by the name of "casa.extensions.<em>type</em>Extension" if it can
	 * be found.  Otherwise returns the {@link Extension} class.
	 * <p>The type is automatically converted to first-letter-capitalized and any dashes converted to
	 * camel case.  
	 */
  private Class<Extension> getClassForType(String type) {
  	assert type!=null;
  	assert type.length()>0;
  	
		try {
			String className = capitalize(type);
			return (Class<Extension>) Class.forName("casa.extensions."+className+"Extension");
		} catch (ClassNotFoundException e) {
			return Extension.class;
		}
  }
  
  /**
   * Calls {@link Extension#load(AbstractInternalFrame, AbstractProcess)} on every know extension that matches
   * the <em>type</em> with the parameters <em>frame</em> and <em>agent</em>.  This method is called at process
   * startup (with <em>type</em>==null, <em>frame</em>==null and <em>agent</em>==null), at agent initialization
   * (with <em>type</em>==null, <em>frame</em>==null and <em>agent</em>==the started-up agent), and may be called
   * from places like GUI menus (with <em>frame</em>!=null and <em>agent</em>!=null).  It is up to the individual
   * Extension types to distinguish these situations and act (or not act) accordingly.
   * @param type The type of the extension: {@value Extension#TYPE_CODE}, {@value Extension#TYPE_TAB}, or {@value Extension#load(AbstractInternalFrame, AbstractProcess)}; may be null to indicate all (wildcard). 
   * @param frame The agent GUI element this is applied to.
   * @param agent The agent this is applied to.
   */
	public static void loadType(String type, AbstractInternalFrame frame, AbstractProcess agent) {
		for (Extension e: instance.extensions) {
			if (type==null || e.descriptor.getType().equalsIgnoreCase(type)) {
				try {
					e.load(frame, agent);
				}
				catch (Exception ex) {
					Trace.log("error", "ExtensionLoader: Cannot use method init(AbstractINternalFrame, AbstractProcess) for extension type '"+type+"' for '"+e.descriptor.get("Extension-Name")+"' in file "+e.descriptor.getSourceFile(), ex);
				}
			}
		}
	}

  /**
   * @param s
   * @return <em>s</em> transformed to first-letter-capitalized and replacing every dash with camel case.
   */
	private String capitalize(String s) {
    //capitalize if necessary
  	if (Character.isLowerCase(s.charAt(0))) { 
  		s = s.substring(0,1).toUpperCase() + s.substring(1);
  	}
  	
  	//replace dashes with camel case
  	for (int index=s.indexOf('-'); index!=-1; index=s.indexOf('-')) { 
  		if (s.length()>index+1) //doesn't end in a dash
  			s = s.substring(0,index) + s.substring(index+1,index+2).toUpperCase() + s.substring(index+2);
  		else
  			s = s.substring(0,index);
  	}
  	return s;
  }
  
  /**
   * Returns s with any digits at the end removed.  May return s itself if there are no suffix digits.  If there are no
   * non-digits in s, then null is returned. 
   * @param s
   * @return s with any digits at the end removed. 
   */
  private Pair<String, Integer> parseEntryName(String s) {
  	boolean hasIndex = false;
  	for (int i=s.length()-1; i>=0; i--) 
  		if (Character.isDigit(s.charAt(i))) 
  			hasIndex = true;
  		else {
  			String type = s.substring(0, i);
  			Integer index = hasIndex? Integer.parseInt(s.substring(i+1, s.length())) : null;
  			return new Pair<String, Integer>(type, index);
  		}
  	return null;
  }
  
  /**
   * @return The lisp representation of the known extensions: "(list {(extension ...)}* )"
   * @see java.lang.Object#toString()
   */
	@Override
	public String toString() {
		if (extensions.size()==0)
			return "nil";
		StringBuilder b = new StringBuilder("(list");
		for (Extension e: extensions) {
			b.append("\n  ").append(e);
		}
		b.append("\n  )");
		return b.toString();
	}
	
	ExtensionDescriptor[] getAllExtensionDescriptors() {
		ExtensionDescriptor ret[] = new ExtensionDescriptor[extensions.size()];
		int i = 0;
		for (Extension e: extensions) {
			ret[i++] = e.descriptor;
		}
		return ret;
	}
}
