/* <p>Title: CASA Agent Infrastructure</p>
 * <p>Copyright: Copyright (c) 2003-2014, Knowledge Science Group, University of Calgary. 
 */
package casa.extensions;

import casa.AbstractProcess;
import casa.Status;
import casa.TransientAgent;
import casa.abcl.Lisp;
import casa.ui.AbstractInternalFrame;
import casa.util.Trace;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.InvalidParameterException;

import javax.swing.JMenuItem;

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
 * @version 0.9
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class LispScriptExtension extends Extension {

	/** If the attribute is "true", then the script is run without an agent at startup and will not appear in the menu. */
	public static final String ATTR_ONCE     = "once";
	
	/** If this attribute is "true", then the script is run for each agent that matches its {@link Extension#ATTR_AGENTTYPE} at agent startup. */
	public static final String ATTR_ONCE_PER_AGENT     = "oncePerAgent";

	/**
	 * @param d
	 */
	public LispScriptExtension(ExtensionDescriptor d) {
		super(d);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Executes the lisp file:
	 * <ul>
	 * <li> If ATTR_ONCE is set and the frame and agent are null.
	 * <li> If ATTR_ONCE_PER_AGENT is set and the frame is null, but the agent is not.
	 * <li> If neither ATTR_ONCE nor ATTR_ONCE_PER_AGENT is set and both the frame and agent are not null.
	 * </ul> 
	 * @see casa.extensions.Extension#load(casa.ui.AbstractInternalFrame, casa.AbstractProcess)
	 */
	@Override
	void load(final AbstractInternalFrame frame, final AbstractProcess agent) {
		if ((Boolean)descriptor.get(ATTR_ONCE)) {
			if (frame==null && agent==null) {
				Status stat = Lisp.abclEval(null, null, null, "(load \""+descriptor.getSourceFile()+"\")", (agent!=null&&(agent instanceof TransientAgent)?((TransientAgent)agent).getUI():null));
			}
		}
		else if ((Boolean)descriptor.get(ATTR_ONCE_PER_AGENT)) {
			if (frame==null && agent!=null) {
				Status stat = Lisp.abclEval((TransientAgent)agent, null, null, "(load \""+descriptor.getSourceFile()+"\")", (agent!=null&&(agent instanceof TransientAgent)?((TransientAgent)agent).getUI():null));
			}
		}
		else {
			if (agent!=null && frame!=null 
					&& ((Class<?>)descriptor.get(ATTR_AGENTTYPE)).isAssignableFrom(agent.getClass())
					&& ((Class<?>)descriptor.get(ATTR_FRAMETYPE)).isAssignableFrom(frame.getClass())) {
				JMenuItem menuItem = new JMenuItem((String)descriptor.get(ATTR_EXTENSIONNAME));
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							((TransientAgent)agent).makeSubthread(new Runnable() {
								@Override
								public void run() {
									Status stat = Lisp.abclEval((TransientAgent)agent, null, null, "(load \""+descriptor.getSourceFile()+"\")", (agent!=null&&(agent instanceof TransientAgent)?((TransientAgent)agent).getUI():null));
									frame.println(stat.toString());
								}
							}).start();
						} catch (Throwable e1) {
							frame.println(Trace.log("error", "Exception during execution of "+descriptor.getSourceFile(), e1));;
						}
					}
				});
				frame.addScript(menuItem);
			}
		}
	}
	
	/**
	 * <ul>
	 * <li>defaults ATTR_EXTENSIONNAME to the souce file name
	 * <li>calls the parent version
	 * <li>ensures at ATTR_ONCE and ATTR_ONCE_PER_AGENT are not both true (throws if they are both true).
	 * </ul>
	 * @see casa.extensions.Extension#validate()
	 */
	@Override
	int validate() throws InvalidParameterException {

		//Make the extensionName the file name if it's not already set.
		validateString(ATTR_EXTENSIONNAME, descriptor.getSourceFile().getName());
		
		int ret = super.validate();
		
		//once and oncePerAgent
		validateBool(ATTR_ONCE, false);
		validateBool(ATTR_ONCE_PER_AGENT, false);
		if (((Boolean)descriptor.get(ATTR_ONCE)) && ((Boolean)descriptor.get(ATTR_ONCE_PER_AGENT)))
			throw new InvalidParameterException("Both attributes "+ATTR_ONCE+" and "+ATTR_ONCE_PER_AGENT+" cannot be selected at the same time.");

		return ret;
	}
	
}
