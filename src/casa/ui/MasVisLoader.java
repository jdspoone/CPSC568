package casa.ui;

import casa.MLMessage;
import casa.ObserverNotification;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;

/**
 * MASVIS dynamic loader. Responsible for dynamically loading MASVIS visualization module and providing an abstraction to the MASVIS methods.
 * @author  <a href="mailto:rkyee@ucalgary.ca">Ryan Yee</a>
 */
public class MasVisLoader implements Observer {
	private Object logTree;
	private Method	addMessage;
	private Method  readFile;
	private Constructor<?>	pvizConstructor;
	private JFrame gui;
	
	/**
	 * Dynamically loads MASVIS classes.
	 */
	public MasVisLoader(){
		try {
			Class<?> logTreeClass = Class.forName("casalogparser.LogTree");
			Constructor<?> logTreeConstructor = logTreeClass.getConstructor();
			addMessage = logTreeClass.getDeclaredMethod("addMessage", Object.class);
			readFile = logTreeClass.getDeclaredMethod("readFile", File.class);
			logTree = logTreeConstructor.newInstance();
			
			Class<?> pVisualizationJFrameClass = Class.forName("casalogparser.PVisualizationJFrame");
			pvizConstructor = pVisualizationJFrameClass.getConstructor(logTreeClass);
			gui = (JFrame) pvizConstructor.newInstance(logTree);
//			gui.getRootPane().setWindowDecorationStyle(windowDecorationStyle)
//			gui.setDefaultLookAndFeelDecorated(true);
		}  catch (Throwable e) {
			e.printStackTrace();
			String error = "Unexpected failure loading MASVIS Libraries. Did you include MASVIS.jar in the classpath?";
			System.out.println(error);
			JOptionPane.showMessageDialog(null, error);
		}
 
	}
	
	public JFrame getJFrame(){
		return gui;
	}
	
	/**
	 * Gets the rootpane of the MASVIS JFrame GUI. Expected usage is as content for a tab.
	 * @return  non-null value if GUI is available
	 */
	public JRootPane getGui(){
		return hasGui() ? gui.getRootPane() : null;
	}
	
	/**
	 * Indicates whether a GUI is available.
	 * 
	 * @return whether a GUI is available
	 */
	public boolean hasGui(){
		return gui!=null;
	}
	
	/**
	 * Add a message to the MASVIS GUI.
	 * @param msg to add
	 */
	public void addMessage(MLMessage msg){
		try {
			if(logTree != null)
				addMessage.invoke(logTree, msg);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public void readFile(File f){
		try {
			if (logTree != null)
				readFile.invoke(logTree, f);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public void update(Observable o, Object arg) {
		System.out.println(arg);
		if(arg instanceof ObserverNotification){
			final ObserverNotification notification = (ObserverNotification) arg;
			if(notification.getObject() instanceof MLMessage){
				final MLMessage message = (MLMessage)notification.getObject();
				addMessage(message);
			}
		}
	}
}
