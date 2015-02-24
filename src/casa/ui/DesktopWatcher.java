package casa.ui;

import java.awt.BorderLayout;
import java.awt.font.TextLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.event.MouseInputAdapter;

/**
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 * @author  <a href="mailto:ayala@cpsc.ucalgary.ca">Gabriel Becerra</a>
 * @author  <a href="mailto:sedachv@cpsc.ucalgary.ca">Vladimir Sedach</a>
 */
public class DesktopWatcher extends JPanel {
  //
  // Variables and Constants
  //
  protected static final Color UNSELECTED_COLOR = new Color(180, 210, 250); // light blue
  protected static final Color SELECTED_COLOR   = new Color(100, 140, 255); // darker blue
  protected static final Color BACKGROUND_COLOR = new Color(255, 255, 255); // !!! This is hard-coded to be the same color as that of the desktop (currently blue)
  protected static final Color GRAYOUT_COLOR = new Color(0.25f, 0.25f, 0.25f, 0.5f); // transparent light grey color
  protected static final Color ICON_COLOR = Color.gray;
  protected static final Color TITLE_COLOR = Color.black;

  protected float  windowScale;           // the aspectRatio (width/height) of the desktop
	protected float aspectRatio;
  protected int    originX;
	protected int originY;
	protected int desktopWatcherWidth;
	protected int desktopWatcherHeight;
	protected int mouseLastX;
	protected int mouseLastY;
  
  protected boolean mouseLastValid = false; // if true, mouseLast coordinates should be read again by mouseDragged

  // the desktop being summarized
  /**
	 */
  protected MDIDesktopPane localDesktop;
  // if the desktop is embedded 
  private JScrollPane localDesktopScroller;

  /**
   * Contructor. Uses a BorderLayout.
   * @param desktop copy of the JDesktopPane declared in LACDesktop
   */
  public DesktopWatcher(MDIDesktopPane desktop, JScrollPane scroller) {
    localDesktop = desktop;
    //setOpaque(true);
    setToolTipText("Keeps track of the Desktop");
    this.setBorder(BorderFactory.createLineBorder(Color.blue));

    setLayout(new BorderLayout());
    
    // the following code forces a re-paint of the DesktopWatcher whenever the LACDesktop is scrolled
    if (scroller != null) {
    	localDesktopScroller = scroller;
    	// Dummy class to watch for scrolling events and updating the DesktopWatcher
    	class DesktopWatcherScrollbarListener implements AdjustmentListener {
    		public void adjustmentValueChanged(java.awt.event.AdjustmentEvent e) {
    			repaint();
    		}
    	}
    	DesktopWatcherScrollbarListener listener = new DesktopWatcherScrollbarListener();
    	localDesktopScroller.getHorizontalScrollBar().addAdjustmentListener(listener);
    	localDesktopScroller.getVerticalScrollBar().addAdjustmentListener(listener);
    } 

    // The following code defines what the DesktopWatcher does on mouse input
    MouseInputAdapter ma = new MouseInputAdapter() {
      public void mouseEntered(MouseEvent e) {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
      public void mouseExited(MouseEvent e) {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
       
      // Scroll the localDesktop around on right-click drag
      public void mouseDragged(MouseEvent e) {
      	if (!mouseLastValid) {
      		mouseLastX = e.getX();
      		mouseLastY = e.getY();
      		mouseLastValid = true;
      	}
      	int mouse_moved_x = e.getX() - mouseLastX;
      	int mouse_moved_y = e.getY() - mouseLastY;
      	mouseLastX = e.getX();
      	mouseLastY = e.getY();
      	      		
      	// In case of right-button drag and presence of scrollers, scroll
      	if (((e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) == MouseEvent.BUTTON3_DOWN_MASK) &&
      			localDesktopScroller != null) {
      		JScrollBar horiz = localDesktopScroller.getHorizontalScrollBar();
      		JScrollBar vert = localDesktopScroller.getVerticalScrollBar();
      		horiz.setValueIsAdjusting(true);
      		vert.setValueIsAdjusting(true);
      		horiz.setValue(horiz.getValue() + watcherToDesktopX(mouse_moved_x));
      		vert.setValue(vert.getValue() + watcherToDesktopY(mouse_moved_y));
      		horiz.setValueIsAdjusting(false);
      		vert.setValueIsAdjusting(false);
      	}
      	// in case of left-button drag, move the selected window.
      	else if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK) {
      		JInternalFrame selectedFrame = localDesktop.getSelectedFrame();
      		int newX = selectedFrame.getX() + watcherToDesktopX(mouse_moved_x);
      		int newY = selectedFrame.getY() + watcherToDesktopY(mouse_moved_y);
      		selectedFrame.setLocation(newX, newY);
      		localDesktop.setSize(localDesktop.getWidth(), localDesktop.getHeight());
      		repaint();
      	}
      }
      
      // set window focus on left-click
      public void mousePressed(MouseEvent e) {
      	if (e.getButton() == MouseEvent.BUTTON1) {
      		int desktopX = watcherToDesktopX(e.getX() + originX);
      		int desktopY = watcherToDesktopY(e.getY() - originY);
      		
      		desktopSelectFrame(desktopX, desktopY);
      		repaint();
      	}
      }
      
      // resets the valid switch for mouseLast coordinates after the mouse button is released (after being dragged)
      public void mouseReleased(MouseEvent e) {
      	mouseLastValid = false;
      }
    };
    addMouseListener(ma);
    addMouseMotionListener(ma);
  }
  
  /**
   * Selects the frame in the localDesktop at the specified x and y localDesktop coordinates
   */
  private void desktopSelectFrame(int x, int y) {
  	JInternalFrame newselection = null;
  	  	
  	// see if there is a frame at those coordinates
  	JInternalFrame[] frames = localDesktop.getAllFrames();
  	for(int i = 0; i < frames.length; i++) {
  		if ((x >= frames[i].getX() && x <= (frames[i].getWidth() + frames[i].getX())) &&
  		    (y >= frames[i].getY() && y <= (frames[i].getHeight() + frames[i].getY()))) {
  			newselection = frames[i];
  			break;
  		}
  	}
  	
  	// if there is a frame, unselect current frame if possible, select newselection
  	if (newselection!=null) {
  		try {
  			if (localDesktop!=null && localDesktop.getSelectedFrame()!=null)
  			  localDesktop.getSelectedFrame().setSelected(false);
  			newselection.setSelected(true);
  		}
  		catch (java.beans.PropertyVetoException e) { };
  	}
  }

  /**
   * Maps a desktop X coordinate onto the corresponding DesktopWatcher X coordinate. 
   * NOTE: does not add originX offset.
  */
  private int desktopToWatcherX(double desktop_x) {
  	return (int)(desktop_x * windowScale);
  }
  
  /**
   * Maps a desktop Y coordinate onto the corresponding DesktopWatcher Y coordinate. 
   * NOTE: does not add originY offset.
  */
  private int desktopToWatcherY(double desktop_y) {
  	return (int)(desktop_y * windowScale);
  }
  
  /**
   * Maps a DesktopWatcher X coordinate onto the corresponding desktop X coordinate.
   * NOTE: does not subtract originX offset.
   */
  private int watcherToDesktopX(int watcher_x) {
  	return (int)((float)watcher_x / windowScale);
  }
  
  /**
   * Maps a DesktopWatcher Y coordinate onto the corresponding desktop Y coordinate.
   * NOTE: does not subtract originY offset.
   */
  private int watcherToDesktopY(int watcher_y) {
  	return (int)((float)watcher_y / windowScale);
  }
  
  /**
   * Sets up coordinate system and transformations for the DesktopWatcher (including aspect ratio, etc.)
   *
   */
  private void setupCoordinateSystem() {
    // Obtaining the current dimensions of the DesktopWatcher
    desktopWatcherHeight = getHeight();
    desktopWatcherWidth  = getWidth();  
        
    // set up aspect ratio
    float newWidth, newHeight;
    aspectRatio = (float)localDesktop.getWidth() / (float)localDesktop.getHeight();
    // assume desktopWatcher will be wide enough to accomodate it
    newHeight = (float)desktopWatcherHeight;
    newWidth = aspectRatio * newHeight;
    // check to see if the assumption holds
    if (newWidth > (float)desktopWatcherWidth) {
    	// the assumption didn't hold, so now have to re-size the height
    	newWidth = (float)desktopWatcherWidth;
    	newHeight = newWidth / aspectRatio;
    }
  
    // set up scale of DesktopWatcher
    windowScale = newWidth / (float)localDesktop.getWidth();
            
    // set up origin points
    originX = (desktopWatcherWidth - (int)newWidth) / 2;
    originY = (desktopWatcherHeight - (int)newHeight) / 2;
    // set up width and height
    desktopWatcherHeight = (int)newHeight;
    desktopWatcherWidth  = (int)newWidth;
  }
  
  
  /**
   * Paints the desktop watcher onto Graphics g
   * @param g
   */
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    
    setupCoordinateSystem();
    
    // fill unused space with background container color
    g.setColor(this.getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());

    // fill up the panel with the desired background
    g.setColor(BACKGROUND_COLOR);
    g.fillRect(originX,originY,desktopWatcherWidth,desktopWatcherHeight);

    // Retrieving the components contained in the LACDesktop
    Component[] components = localDesktop.getComponents();
    // Retrieving only the JInternalFrames contained in the LACDesktop
    JInternalFrame[] array = localDesktop.getAllFrames();

    //
    // Drawing "thumbnails" of each JInternalFrame in the DesktopWatcher
    //
    String frameName = new String();
    float titleY, titleX;
    for (int i = components.length-1; i > -1; i--) {
      if (components[i].isVisible()) {
        if (components[i] instanceof JInternalFrame && ((JInternalFrame) components[i]).isSelected()) {
            g.setColor(SELECTED_COLOR);
        }
        else if (array[i].isIcon()) {
        	g.setColor (ICON_COLOR);
        }
        else {
        	g.setColor(UNSELECTED_COLOR);
        }
          
        // Fills up the representation of the JInternalFrame with selected/unselected color
        g.fill3DRect(
            desktopToWatcherX(components[i].getX()) + originX,
            desktopToWatcherY(components[i].getY()) + originY,
            desktopToWatcherX(components[i].getWidth()) - 1,
            desktopToWatcherY(components[i].getHeight()) - 1, true);
        

        // Necessary to use array instead of components in order to avoid a "cast exception".
        // If the JInternalFrame is minimized, then DO NOT attempt to draw any title
        if (! array[i].isIcon() ) {
        	// Set up frame title
          frameName = ((JInternalFrame) components[i]).getName ();
          TextLayout title = new TextLayout(frameName, g.getFont(), ((Graphics2D) g).getFontRenderContext());
          // Set title color
          g.setColor (TITLE_COLOR);
          // Draw the FrameName's at the centre of the rectangle
          titleX = (float)(desktopToWatcherX(components[i].getWidth()) - title.getBounds().getWidth()) / 2;
          titleY = (float)(desktopToWatcherY(components[i].getHeight()) + title.getBounds().getHeight()) / 2;
          title.draw((Graphics2D) g, 
          		     (float)desktopToWatcherX(components[i].getX()) + titleX + originX,
		             (float)desktopToWatcherY(components[i].getY())+ titleY + originY);
        }
      }
    }
    
    // grey out areas not currently seen
    Rectangle area = localDesktop.getVisibleRect();
    int visibleX, visibleWidth, visibleY, visibleHeight;
    visibleX = desktopToWatcherX(area.getX()) + originX;
    visibleWidth = desktopToWatcherX(area.getWidth());
    visibleY = desktopToWatcherY(area.getY()) + originY;
    visibleHeight = desktopToWatcherY(area.getHeight());
    g.setColor(GRAYOUT_COLOR);
    g.fillRect(originX, originY, visibleX - originX, desktopWatcherHeight);
    g.fillRect(visibleWidth + visibleX, originY, desktopWatcherWidth - visibleWidth - visibleX + originX, desktopWatcherHeight);
    g.fillRect(visibleX, originY, visibleWidth, visibleY - originY);
    g.fillRect(visibleX, visibleY + visibleHeight, visibleWidth, desktopWatcherHeight - visibleY - visibleHeight + originY);
  }
}