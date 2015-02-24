package casa.ui;

import casa.AbstractProcess;
import casa.Agent;
import casa.TransientAgent;
import casa.interfaces.AgentInterface;
import casa.util.PropertyException;

import java.awt.Container;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.JDialog;

/**
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary.
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  The
 * Knowledge Science Group makes no representations about the suitability of
 * this software for any purpose.  It is provided "as is" without express or
 * implied warranty.</p>
 *
 * @author <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public class AgentInternalFrame extends TransientAgentInternalFrame {

	/**
	 * Don't immediately do start... because of "flicker" from live update
	 * @see casa.ui.TransientAgentInternalFrame#start()
	 * @see superStart()
	 */
	@Override
	public void start() {
		// don't do start yet...
	}

	private void superStart() {
		super.start();
	}

protected static final int agentFrame_LAST_EVENT = transientAgentFrame_LAST_EVENT + 10;

  public AgentInternalFrame (TransientAgent agent, String title, Container aFrame) {
    super (agent, title, aFrame);
  }

  public void resetFromPersistentData() {
    final String cn = getClass().getName()+".";
    //runInEventDispatchThread(new ResetFromPersistentData(cn));  
    agent.makeSubthread(new ResetFromPersistentData(cn),agent.getAgentName()+"-AgentInternalFrame-init").start();  
  }
  
  private class ResetFromPersistentData implements Runnable {
  	String cn;
  	public ResetFromPersistentData(String className) {
  		this.cn = className;
  	}
  	public void run () {
  		if (process instanceof Agent) {
  			AgentInterface agent = getAgent();
  			long timeout = System.currentTimeMillis() + 8000;
  			JDialog win = null;
  			while (System.currentTimeMillis()<timeout && (!agent.ready() || !agent.isRegistered()) && ((Agent)process).getState()!=Thread.State.TERMINATED){
//  				try {
//  					Thread.sleep(500);
//  					if (System.currentTimeMillis()>timeout && win==null) {
//  						//DEBUG.DISPLAY_ERROR("Agent "+agent.getAgentName()+" failed to initialize properly in 5 seconds; continuing using defaults");
//	  				    try {
//	  				    	win = new JDialog((Frame)null,"Waiting for agent "+agent.getAgentName()+" to initialize",false);
//	  				    	win.setVisible(true);
//	  				    }
//	  				    catch (HeadlessException ex) {
//	  				    }
//  					}
//  				} catch (InterruptedException e) {}
  			}
  			
  			if (win!=null)
  				win.dispose();

			int x = getInt(agent, cn+"xPos", -1);
  			int y = getInt(agent, cn+"yPos", -1);
  			final int width = getInt(agent, cn+"xSize", 0);
  			final int height = getInt(agent, cn+"ySize", 0);
  			boolean isIcon = getBool(agent, cn+"isIcon", false);
  			boolean isMaximized = getBool(agent, cn+"isMaximized", false);

  			Point p = new Point(x, y);
  			
			if (x != -1 && y != -1)
				setLocation(p);
						
			if (width != 0 && height != 0){
				//TODO: this is a hack; shouldn't need to do this according to API
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						setSize(width, height);
					}
				});
			}
			
			if (isMaximized){
				//TODO: this is a hack; shouldn't need to do this according to API
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						setMaximized(true);
					}
				});
			}
			
			if (isIcon){
				setIcon(true);
			}
			
			superStart();				
  		}
  	}
  	private boolean getBool(AgentInterface agent, String name, boolean def) {
  		try {
  			return agent.isPersistent()?agent.getBooleanProperty(name):def;
  		} catch (PropertyException e) {
  			agent.println("warning","Could not read "+name+" information for " + process.getAgentName() + ". Using default: "+def);
  		} catch (ClassCastException e) {
  			agent.println("error", "Could not read "+name+" information.");
  		}
  		return def;
  	}
  	private int getInt(AgentInterface agent, String name, int def) {
  		try {
  			return agent.isPersistent()?agent.getIntegerProperty(name):def;
  		} catch (PropertyException e) { 
  			agent.println("warning","Could not read "+name+" information for " + process.getAgentName() + ". Using default: "+def);
  		} catch (ClassCastException e) {
  			agent.println("error", "Could not read "+name+" information.");
  		}
  		return def;
  	}
  	private String getString(AgentInterface agent, String name, String def) {
  		try {
  			return agent.isPersistent()?agent.getStringProperty(name):def;
  		} catch (PropertyException e) {
  			agent.println("warning","Could not read "+name+" information for " + process.getAgentName() + ". Using default: \""+def+'"');
  		} catch (ClassCastException e) {
  			agent.println("error", "Could not read "+name+" information.");
  		}
  		return def;
  	}
  } // class ResetFromPersistentData
  
  public AgentInterface getAgent () {
    return (AgentInterface) getTransientAgent ();
  }

  public Rectangle getBounds() {
    Rectangle r;
    if (jInternalFrame != null) {
      r = jInternalFrame.getBounds();
    } else {
      r = jFrame.getBounds();
    }
    return r;
  }

  /**
   * Adaptor method: when isInternalFrameClosable() returns true, this method ensures that observers are notified
   * and that the window is disposed off.
   */
  @Override
  protected void closeInternalFrame () {
   updateAgent();
   super.closeInternalFrame();
  }
  
  protected void updateAgent() {
    Rectangle r = getBounds();
    AgentInterface a = getAgent();
    String cn = getClass().getName()+".";
    a.setBooleanProperty(cn+"isIcon",isIcon());
    a.setBooleanProperty(cn+"isMaximized",isMaximized());
    a.setIntegerProperty(cn+"xPos",r.x);
    a.setIntegerProperty(cn+"yPos",r.y);
    a.setIntegerProperty(cn+"xSize",r.width);
    a.setIntegerProperty(cn+"ySize",r.height);
  }

  @Override
  public void actionPerformed (ActionEvent e) {
    int command = noAction;
    try {
      command = Integer.valueOf (e.getActionCommand ()).intValue ();
    } catch (Exception ex) {
      command = noAction;
    }

    switch (command) {
      default:
        super.actionPerformed (e);
    }
  }
}
