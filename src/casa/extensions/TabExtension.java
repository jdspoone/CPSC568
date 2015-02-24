package casa.extensions;

import casa.AbstractProcess;
import casa.TransientAgent;
import casa.ui.AbstractInternalFrame;
import casa.util.JarLoader;
import casa.util.Trace;

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

/**
 * 
 * <p>
 * <b>Title:</b> CASA Agent Infrastructure
 * </p>
 * <p>
 * <b>Copyright: Copyright (c) 2003-2014, Knowledge Science Group, University of
 * Calgary.</b> Permission to use, copy, modify, distribute and sell this
 * software and its documentation for any purpose is hereby granted without fee,
 * provided that the above copyright notice appear in all copies and that both
 * that copyright notice and this permission notice appear in supporting
 * documentation. The Knowledge Science Group makes no representations about the
 * suitability of this software for any purpose. It is provided "as is" without
 * express or implied warranty.
 * </p>
 * <p>
 * <b>Company:</b> Knowledge Science Group, Department of Computer Science,
 * University of Calgary
 * </p>
 * 
 * An extension that loads itself as a GUI tab pane into a
 * {@link AbstractInternalFrame} frame. TabExtension classes/objects are never
 * loaded/instantiated until the tab is displayed (made visible) to save on
 * memory. TabExtensions are always placed in the menu "Tools|Tabs|[tabName]",
 * but they are only actually shown as a [tabName] tab if the specified by the
 * descriptor's {@link #AUTOLOAD} attribute is true (or the menu item is
 * selected, of course). Attributes in jar manifest file:
 * <ul>
 * <li>{@link Extension#ATTR_MAINCLASS} - {@value Extension#ATTR_MAINCLASS}.
 * Defaults to the value Main-Class in the manifest main section.
 * <li>{@link Extension#ATTR_EXTENSIONNAME} -
 * {@value Extension#ATTR_EXTENSIONNAME}. Defaults to name (only) of the value
 * of {@link Extension#ATTR_MAINCLASS}.
 * <li>{@link #ATTR_TABNAME} - {@value #ATTR_TABNAME}. The text on the tab or
 * the tab pane. Defaults to ATTR_EXTENSIONNAME.
 * <li>{@link #ATTR_AUTOLOAD} - {@value #ATTR_AUTOLOAD}. Will display the tab
 * automatically (initially) iff this is true. Defaults false.
 * <li>{@link #ATTR_AGENTTYPE} - {@value #ATTR_AGENTTYPE}. Any agent we are
 * applying this extension to must be a subtype of this. Defaults to
 * {@link TransientAgent}.
 * </ul>
 * 
 * @version 0.9
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * 
 */
public class TabExtension extends CodeExtension {

	/** The text on the tab itself */
	public static final String ATTR_TABNAME = "tabName";

	/**
	 * Constructs a new tab extension.
	 * 
	 * @param d
	 */
	public TabExtension(ExtensionDescriptor d) {
		super(d);
	}

	/**
	 * Validate the descriptor with respect to this extension.
	 * <p>
	 * This method:
	 * <ul>
	 * <li>checks that the <b>agentType</b> field is a legitimate agent path and
	 * replaces the value with the actual class object.
	 * <li>checks that the <b>tab name</b> field is set and defaults it to the
	 * extension name if it isn't set.
	 * </ul>
	 * 
	 * @return 0 if all is good, some other integer if there were problems.
	 * @throws InvalidParameterException
	 *           if a fatal error happens.
	 */
	@Override
	int validate() throws InvalidParameterException {

		// special case for Tabs
		if (descriptor.get(ATTR_EXTENSIONNAME) == null
				&& descriptor.get(ATTR_TABNAME) != null)
			descriptor.put(ATTR_EXTENSIONNAME, descriptor.get(ATTR_TABNAME));
		if (descriptor.get(ATTR_TABNAME) == null
				&& descriptor.get(ATTR_EXTENSIONNAME) != null)
			descriptor.put(ATTR_TABNAME, descriptor.get(ATTR_EXTENSIONNAME));

		//autoload
		int ret = validateBool(ATTR_AUTOLOAD, false);
		
		//super.validate
		int r = super.validate();
		if (ret==0)
			ret = r;

		// tabName
		r = validateString(ATTR_TABNAME, getNameFromMainClass());
		if (ret == 0)
			ret = r;
		
		return ret;
	}

	/**
	 * If this extension is applicable to the <em>agent</em> (according to
	 * {@link Extension#ATTR_AGENTTYPE ATTR_AGENTTYPE}) and <em>frame</em>
	 * (according to {@link Extension#ATTR_FRAMETYPE ATTR_FRAMETYPE}),
	 * instantiates a tab pane in the
	 * <em>agent</em>'s <em>frame</em> with the name specified by the descriptor's
	 * {@link #ATTR_TABNAME}. The class specified by the descriptor's
	 * {@link #ATTR_MAINCLASS} will ONLY be loaded when the tab pane is made
	 * visible, and at that time an instance of the class will be instantiated
	 * using it's ({@link TransientAgent}, {@link AbstractInternalFrame})
	 * constructor with the arguments <em>agent</em> and <em>frame</em>.<p>
	 * If {@link Extension#ATTR_AUTOLOAD AUTOLOAD} is true, the tab
	 * will actually show up on the frame, but if {@link Extension#ATTR_AUTOLOAD AUTOLOAD}
	 * is not true, then the use can display it by choosing the menu item Tools|Tabs|[tabName].
	 * 
	 * @param frame
	 *          the frame that will contain this tab pane.
	 * @param agent
	 *          the agent that "owns" the frame and tab pane.
	 */
	@Override
	void load(final AbstractInternalFrame frame, final AbstractProcess agent) {
		if (frame==null || agent==null) //don't process globals of any sort.
			return;
		
		//validate the agent
		Class<?> cls = null;
		Object obj = descriptor.get(ATTR_AGENTTYPE);
		if (obj instanceof Class<?>) {
			cls = (Class<?>) obj;
		}
		else {
			Trace.log("error", "Expected attribute "+ATTR_AGENTTYPE+" to be a class object, but got "+obj);
			return;
		}
		if (!cls.isAssignableFrom(agent.getClass())) // check if this extension is
																								 // applicable to the agent.
			return;

		//validate the frame
		cls = null;
		obj = descriptor.get(ATTR_FRAMETYPE);
		if (obj instanceof Class<?>) {
			cls = (Class<?>) obj;
		}
		else {
			Trace.log("error", "Expected attribute "+ATTR_FRAMETYPE+" to be a class object, but got "+obj);
			return;
		}
		if (!cls.isAssignableFrom(frame.getClass())) // check if this extension is
																								 // applicable to the frame.
			return;
		
		final String tabName = (String) descriptor.get(ATTR_TABNAME);
		if (frame.getTab(tabName) == null) {
			final JTextArea component = new JTextArea("Please wait while the "
					+ descriptor.get(ATTR_EXTENSIONNAME) + " plugin initializes...");
			JScrollPane scroll = new JScrollPane(component,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			ComponentListener listener = new ComponentListener() {
				@Override
				public void componentShown(ComponentEvent e) {
					try {
						Class<?> constructorTypes[] = new Class[] { TransientAgent.class,
								AbstractInternalFrame.class };
						Object constructorObjects[] = new Object[] { agent, frame };

						Object o = loadFromJar(constructorTypes, constructorObjects);
						if (o instanceof Component) {
							Component newComponent = (JComponent) o;
							frame.replaceTabComponent(tabName, newComponent,
									descriptor.get(ATTR_DOC).toString());
							frame.setSelectedTab(tabName);
						} else {
							throw new ClassCastException(
									"Tab plugins must specify a subclass of JComponent, but found type "
											+ o.getClass().getCanonicalName());
						}
					} catch (Throwable e1) {
						component.setText(Trace
								.log("error", "Could not load plugin", e1, 0));
					}
				}

				@Override
				public void componentResized(ComponentEvent e) {
				}

				@Override
				public void componentMoved(ComponentEvent e) {
				}

				@Override
				public void componentHidden(ComponentEvent e) {
				}
			};
			scroll.addComponentListener(listener);
			component.addComponentListener(listener);
			frame.addTab(tabName, scroll, (Boolean) descriptor.get(ATTR_AUTOLOAD));
		}
	}

	/**
	 * This method is overridden as a no-op because tabs are not loaded until they
	 * are actually displayed.
	 */
	@Override
	protected void loadIf() {
	}

	/**
	 * Loads the jar (specified by the descriptor's {@link #ATTR_MAINCLASS}) and
	 * constructs a new instance of it using the {@link #constructorTypes} and
	 * {@link #constructorObjects} parameters.
	 * 
	 * @param constructorTypes
	 * @param constructorObjects
	 * @return The object loaded from the jar.
	 * @throws IOException
	 * @throws ClassCastException
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	protected Object loadFromJar(Class<?>[] constructorTypes,
			Object[] constructorObjects) throws IOException, ClassCastException,
			ClassNotFoundException, SecurityException, NoSuchMethodException,
			IllegalArgumentException, InstantiationException, IllegalAccessException,
			InvocationTargetException {
		JarLoader.addFile(descriptor.getSourceFile());
		String className = (String) descriptor.get(ATTR_MAINCLASS);
		Class<?> cls = Class.forName(className);
		Constructor<?> constructor = cls.getConstructor(constructorTypes);
		Object o = constructor.newInstance(constructorObjects);
		return o;
	}
}
