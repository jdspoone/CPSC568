package casa.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.util.Vector;

/**
 * <p>Title: CASA</p> <p>Description: Provides a list of the JInternalFrames and the ability to select it. When the user selects the ChackBox, the JInternalFrame will go to the front. In addition, allows the user to cascade or tile the JInternalFrames. </p> <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 * @author  <a href="mailto:ayala@cpsc.ucalgary.ca">Gabriel Becerra</a>
 */
public class LACWindowMenu extends JMenu {
  //
  // JAVA - UI Objects
  //
  /**
	 */
  private MDIDesktopPane desktop;
  private JMenuItem cascade = new JMenuItem ("Cascade");
  private JMenuItem tile    = new JMenuItem ("Tile");
  private Vector<JMenuItem> extraItems;
  private ProcessWindow parent;

  @SuppressWarnings("unchecked")
	public LACWindowMenu (ProcessWindow parent, MDIDesktopPane desktop, Vector<JMenuItem> extraItems) {
  	this.parent = parent;
    this.desktop = desktop;
    this.extraItems = (Vector<JMenuItem>)extraItems.clone();
    buildNavigateMenu();
  }
  

  /* (non-Javadoc)
	 * @see javax.swing.JMenu#add(java.awt.Component, int)
	 */
	@Override
	synchronized public Component add(Component c, int index) {
		extraItems.insertElementAt((JMenuItem)c, index);
		return super.add(c, index);
	}

  /* (non-Javadoc)
	 * @see javax.swing.JMenu#add(java.awt.Component, int)
	 */
	@Override
	synchronized public JMenuItem insert(JMenuItem c, int index) {
		String label = c.getText();
		if (label!=null) {
			for (JMenuItem m: extraItems) {
				if (label.equals(m.getText())) {
					extraItems.remove(m);
					break;
				}
			}
		}
		if (index>=extraItems.size()) {
			extraItems.add((JMenuItem)c);
			return super.add(c);
		}
		else {
			extraItems.insertElementAt((JMenuItem)c, index);
			return super.insert(c, index);
		}
	}

  /* (non-Javadoc)
	 * @see javax.swing.JMenu#add(java.awt.Component, int)
	 */
	@Override
	synchronized public void remove(int index) {
		if (index < extraItems.size()) {
			extraItems.removeElementAt(index);
			super.remove(index);
		}
	}

	private void buildNavigateMenu () {
    //
    // Navigate Menu Section
    //
    this.setText    ("Window" );
    this.setMnemonic(KeyEvent.VK_W);

    cascade.setMnemonic(KeyEvent.VK_C);
    cascade.addActionListener (new ActionListener () {
      public void actionPerformed (ActionEvent ae) {
        LACWindowMenu.this.desktop.cascadeFrames ();
      }
    });

    tile.setMnemonic(KeyEvent.VK_T);
    tile.addActionListener (new ActionListener () {
      public void actionPerformed (ActionEvent ae) {
        LACWindowMenu.this.desktop.tileFrames ();
      }
    });

    addMenuListener (new MenuListener () {
      public void menuCanceled (MenuEvent e) {}

      public void menuDeselected (MenuEvent e) {
        removeAll();
      }

      public void menuSelected (MenuEvent e) {
        buildChildMenus ();
      }
    });
  }

  /* Sets up the children menus depending on the current desktop state */
  synchronized private void buildChildMenus () {
    int i;
    JInternalFrame[] array = desktop.getAllFrames ();

    add (cascade);
    add (tile);

    if((extraItems != null) && (extraItems.size() > 0)){
      addSeparator();
      for(int j = 0; j < extraItems.size(); j++){
//          if(extraItems.get(j) instanceof JMenuItem){
            add((JMenuItem)extraItems.get(j));
//          }
      }
    }

    if (array.length > 0)
      addSeparator ();
    cascade.setEnabled (array.length > 0);
    tile.setEnabled (array.length > 0);
    
    parent.refreshTabMenu();

    for (i = 0; i < array.length; i++) {
      ChildMenuItem menu = new ChildMenuItem (array[i]);
      menu.setState (i == 0);
      menu.addActionListener (new ActionListener () {
        public void actionPerformed (ActionEvent ae) {
          JInternalFrame frame = ((ChildMenuItem) ae.getSource ()).getFrame ();
          frame.moveToFront ();
          try {
            frame.setSelected (true);
          } catch (PropertyVetoException e) {
            e.printStackTrace ();
          }
        }
      });
      menu.setIcon (array[i].getFrameIcon ());
      add (menu);
    }
  }

  /* This JCheckBoxMenuItem descendant is used to track the child frame that corresponds
     to a given menu. */
  /**
	 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
	 */
  public class ChildMenuItem extends JCheckBoxMenuItem {
    private JInternalFrame frame;

    public ChildMenuItem (JInternalFrame frame) {
      super (frame.getTitle ());
      this.frame = frame;
    }

    /**
		 * @return
		 */
    public JInternalFrame getFrame () {
      return frame;
    }
  }
}