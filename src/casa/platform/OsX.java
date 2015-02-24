package casa.platform;

import casa.ui.AbstractInternalFrame;
import casa.util.CASAUtil;

import java.awt.Image;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.ksg.casa.CASA;

import com.apple.mrj.MRJAboutHandler;

/**
 * <code>OsX</code> provides a means to handle the AppleEvent for quit. Add a new <code>OsX.QuitHandler</code> using <code>OsX.addQuitHandler</code> to handle the event. <p> Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation. The Knowledge Science Group makes no representations about the suitability of this software for any purpose. It is provided "as is" without express or implied warranty. </p> Created on June 2, 2009
 * @author  <a href="mailto:rkyee@ucalgary.ca">Ryan Yee</a>
 */
public class OsX extends Generic implements InvocationHandler {
	private Object				applicationInstance;			// this is non-null on OS X
	private Method				addApplicationListenerMethod;
	private Class<?>			applicationListenerClass;
	private Vector<QuitHandler>	quitHandlers;
	
	static {
		new OsX();
	}

	/**
	 * private because singleton
	 */
	protected OsX() {

		if (isMacOsX()) {
			quitHandlers = new Vector<QuitHandler>();

			try {
				applicationInstance = Class.forName(
						"com.apple.eawt.Application").getConstructor()
						.newInstance();
				applicationListenerClass = Class
						.forName("com.apple.eawt.ApplicationListener");
				addApplicationListenerMethod = applicationInstance.getClass()
						.getDeclaredMethod("addApplicationListener",
								applicationListenerClass);
				Object applicationAdapterProxy = Proxy.newProxyInstance(
						getClass().getClassLoader(),
						new Class<?>[] { applicationListenerClass }, this);
				addApplicationListenerMethod.invoke(applicationInstance,
						applicationAdapterProxy);
			} catch (Throwable e) {
				CASAUtil.log("error", "OsX.init(): Failed to install quit handler.", e, true);
			} 
			
			try {
				Class<?> app = Class.forName("com.apple.mrj.MRJApplicationUtils");
				Method method = app.getMethod("registerAboutHandler", Class.forName("com.apple.mrj.MRJAboutHandler"));
				MRJAboutHandler handler = new MRJAboutHandler(){
					@Override
					public void handleAbout() {
			      JOptionPane.getRootFrame().removeAll();
			      URL url = ClassLoader.getSystemResource("images/customGraphics/casa64.png");
			      Icon icon = new ImageIcon(url);
			      JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), CASA.getBuildInfo(), 
			      		"About CASA", JOptionPane.INFORMATION_MESSAGE, icon);
					}};
				method.invoke(null, handler);
			} catch (Throwable e) {
				CASAUtil.log("error", "OsX.init(): Failed to install MRJ about handler.", e, true);
			}
		}
	}

	/**
	 * Required for a proxy object. Checks to see if the quit method is being
	 * called and calls the quit handlers.
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		if (method.getName().compareTo("handleQuit") == 0)
			for (QuitHandler qh : quitHandlers) {
				try {
					qh.handleQuit();
				} catch (Throwable e) {
				}
			}
		return null;
	}
	
	@Override
	public Image setDocIconImage2(Image img) {
		try {
  	  com.apple.eawt.Application app = com.apple.eawt.Application.getApplication();
  	  app.setDockIconImage(img);
  	  return img;
		}
		catch (Throwable e) {
			return null;
		}
	}



	// STATICS

//	private static boolean	MAC_OS_X	= (System.getProperty("os.name")
//												.toLowerCase()
//												.startsWith("mac os x"));
//
//	private static OsX		singleton	= new OsX();

	/**
	 * Indicates whether current platform is OS X
	 * 
	 * @return whether current platform is OS X
	 */
	public static final boolean isMacOsX() {
		return MAC_OS_X;
	}
	
	/**
	 * Adds a quit event handler
	 * 
	 * @param quitHandler to add
	 */
	@Override
	protected QuitHandler addQuitHandler2(QuitHandler quitHandler) {
		if (MAC_OS_X) {
			((OsX)singleton).quitHandlers.add(quitHandler);
			return quitHandler;
		}
		return null;
	}

}
