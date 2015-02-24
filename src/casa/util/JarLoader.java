/**
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. 
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  
 * The  Knowledge Science Group makes no representations about the suitability
 * of  this software for any purpose.  It is provided "as is" without express
 * or implied warranty.</p>
 * <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
package casa.util;
import casa.Status;
import casa.TransientAgent;
import casa.abcl.CasaLispOperator;
import casa.abcl.LispException;
import casa.abcl.ParamsMap;
import casa.ui.AgentUI;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Vector;

import org.armedbear.lisp.Environment;

/**
 * Useful class for dynamically changing the classpath, adding classes during runtime. <p>
 * @author unknown
 */
public class JarLoader {
	/**
	 * Parameters of the method to add an URL to the System classes. 
	 */
	private static final Class<?>[] parameters = new Class[]{URL.class};
	
	private static Vector<String> loadedJars = new Vector<String>();
	public static Vector<String> getLoadedJars() {
		return loadedJars;
	}
	/**
	 * @return the loaded jars in in the form:</br>
	 * "" if there are no loaded jars,<br>
	 * ":[jar1]:[jar2]..." otherwise.
	 */
	public static String getLoadedJarsAsString() {
		StringBuilder b = new StringBuilder();
		if (loadedJars.size()==0) return "";
		for (String jar: loadedJars) {
			b.append(':').append(jar);
		}
		return b.toString();
	}

	/**
	 * Adds a file to the classpath.
	 * @param s a String pointing to the file
	 * @throws IOException
	 * @return the URL added (as a String)
	 */
	public static String addFile(String s) throws IOException {
		loadedJars.add(s); //TODO this needs to be made a lot more robust!!!
		File f = new File(s);
		return addFile(f);
	}//end method

	/**
	 * Adds a file to the classpath.
	 * @param f the file to be added
	 * @throws IOException
	 * @return the URL added (as a String)
	 */
	public static String addFile(File f) throws IOException {
		return addURL(f.toURI().toURL());
	}//end method

	/**
	 * Adds the content pointed by the URL to the classpath.
	 * @param u the URL pointing to the content to be added
	 * @return the URL added (as a String)
	 * @throws IOException
	 */
	public static String addURL(URL u) throws IOException {
		if (loadedJars.contains(u.toString()))
			return u.toString();
		loadedJars.add(u.toString());
		URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
		Class<?> sysclass = URLClassLoader.class;
		try {
			Method method = sysclass.getDeclaredMethod("addURL",parameters);
			method.setAccessible(true);
			method.invoke(sysloader,new Object[]{ u }); 
		} catch (Throwable t) {
			t.printStackTrace();
			throw new IOException("Error, could not add URL to system classloader");
		}//end try catch
		return u.toString();
	}//end method

	@SuppressWarnings("unused")
	private static final CasaLispOperator LOAD_JAR =
		new CasaLispOperator("LOAD-JAR", "\"!Loads a jar into the classpath.\" "
				+" JARNAME \"@java.lang.String\" \"!The fully-qualified pathname of jar file (including the .jar extension).\""
				, TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
	{
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
			String jarpath = null;
 			try {
 				jarpath = (String)params.getJavaObject("JARNAME");
				addFile(jarpath);
			} catch (IOException e) {
				agent.println("error", "(LOAD-JAR [jarpath])", e);
				throw new LispException("Could not load jar \""+jarpath+"\": "+e.toString());
//				return new Status (-1, "Could not load jar \""+jarpath+"\": "+e.toString());
			}
 			return new Status(0);
		}
	};


}
