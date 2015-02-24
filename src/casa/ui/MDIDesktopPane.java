package casa.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.beans.PropertyVetoException;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

/**
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 * @author  Gerald Nunn
 * @author  <a href="mailto:ayala@cpsc.ucalgary.ca">Gabriel Becerra</a>
 */
public class MDIDesktopPane extends JDesktopPane {
  private static int FRAME_OFFSET = 20;
  /**
	 */
  private MDIDesktopManager manager;

  public MDIDesktopPane () {
    manager = new MDIDesktopManager (this);
    setDesktopManager (manager);
    putClientProperty("JDesktopPane.dragMode", "outline");
  }

  @Override
  public void setBounds (int x, int y, int w, int h) {
    super.setBounds (x, y, w, h);
    checkDesktopSize ();
  }

  public Component add (JInternalFrame frame) {
    JInternalFrame[] array = getAllFrames ();
    Point p;
    int w;
    int h;

    Component retval = super.add (frame);
    checkDesktopSize ();
    if (array.length > 0) {
      p = array[0].getLocation ();
      p.x = p.x + FRAME_OFFSET;
      p.y = p.y + FRAME_OFFSET;
    } else {
      p = new Point (0, 0);
    }
    frame.setLocation (p.x, p.y);
    if (frame.isResizable ()) {
      w = getWidth () - (getWidth () / 3);
      h = getHeight () - (getHeight () / 3);
      if (w < frame.getMinimumSize ().getWidth ())
        w = (int) frame.getMinimumSize ().getWidth ();
      if (h < frame.getMinimumSize ().getHeight ())
        h = (int) frame.getMinimumSize ().getHeight ();
      frame.setSize (w, h);
    }
    moveToFront (frame);
    frame.setVisible (true);
    try {
      frame.setSelected (true);
    } catch (PropertyVetoException e) {
      frame.toBack ();
    }
    return retval;
  }

  @Override
  public void remove (Component c) {
    super.remove (c);
    checkDesktopSize ();
  }

  /**
   * Cascades all internal frames keeping their original size
   *
   */
  public void cascadeFrames () {
    int x = 0;
    int y = 0;
    JInternalFrame[] allFrames = getAllFrames ();
    manager.setNormalSize ();

    for (int i = allFrames.length - 1; i >= 0; i--) {
      int frameH = allFrames[i].getHeight();
      int frameW = allFrames[i].getWidth();
      allFrames[i].setSize(frameW, frameH);
      allFrames[i].setLocation(x, y);
      x += FRAME_OFFSET;
      y += FRAME_OFFSET;
    }
  }

  /**
   * Tile all internal frames
   */
  public void tileFrames () {
    java.awt.Component allFrames[] = getAllFrames ();
    manager.setNormalSize ();
    int frameHeight = getBounds ().height / allFrames.length;
    int y = 0;
    for (int i = 0; i < allFrames.length; i++) {
      allFrames[i].setSize (getBounds ().width, frameHeight);
      allFrames[i].setLocation (0, y);
      y = y + frameHeight;
    }
  }

  /**
   * Sets all component size properties ( maximum, minimum, preferred)
   * to the given dimension.
   */
  public void setAllSize (Dimension d) {
    setMinimumSize (d);
    setMaximumSize (d);
    setPreferredSize (d);
  }

  /**
   * Sets all component size properties ( maximum, minimum, preferred)
   * to the given width and height.
   */
  public void setAllSize (int width, int height) {
    setAllSize (new Dimension (width, height));
  }

  private void checkDesktopSize () {
    if (getParent () != null && isVisible ()) {
    	javax.swing.SwingUtilities.invokeLater(new Runnable () {public void run () {manager.resizeDesktop ();}});
    }
  }

  public void repaintWindowWatcher() {
  	manager.repaintWindowWatcher();
  }
}

///**
// * Private class used to replace the standard DesktopManager for JDesktopPane. Used to provide scrollbar functionality.
// */
//class MDIDesktopManager extends DefaultDesktopManager {
//  /**
//	 */
//  private MDIDesktopPane desktop;
//  /**
//	 */
//  protected DesktopWatcher ww;
//
//  public void repaintWindowWatcher() {
//  	ww.repaint();
//  }
//
//  public MDIDesktopManager (MDIDesktopPane desktop) {
//    this.desktop = desktop;
//    ww = new DesktopWatcher(desktop, null);
//  }
//
//  public MDIDesktopManager (MDIDesktopPane desktop, JScrollPane scroller) {
//  	 this.desktop = desktop;
//     ww = new DesktopWatcher(desktop, scroller);
//  }
//
//  @Override
//  public void endResizingFrame (JComponent f) {
//    super.endResizingFrame (f);
//    repaintWindowWatcher();
//  }
//
//  @Override
//  public void endDraggingFrame (JComponent f) {
//	super.endDraggingFrame (f);
//    resizeDesktop ();	
//    repaintWindowWatcher();
//  }
//
//  public void setNormalSize () {
//    JScrollPane scrollPane = getScrollPane ();
//    int x = 0;
//    int y = 0;
//    Insets scrollInsets = getScrollPaneInsets ();
//
//    if (scrollPane != null) {
//      Dimension d = scrollPane.getVisibleRect ().getSize ();
//      if (scrollPane.getBorder () != null) {
//        d.setSize (d.getWidth () - scrollInsets.left - scrollInsets.right,
//                   d.getHeight () - scrollInsets.top - scrollInsets.bottom);
//      }
//
//      d.setSize (d.getWidth () - 20, d.getHeight () - 20);
//      desktop.setAllSize (x, y);
//      scrollPane.invalidate ();
//      scrollPane.validate ();
//    }
//  }
//
//  private Insets getScrollPaneInsets () {
//    JScrollPane scrollPane = getScrollPane ();
//    if (scrollPane == null || scrollPane.getBorder()==null)
//      return new Insets (0, 0, 0, 0);
//    else
//      return scrollPane.getBorder ().getBorderInsets (scrollPane);
//  }
//
//  private JScrollPane getScrollPane () {
//    if (desktop.getParent ()instanceof JViewport) {
//      JViewport viewPort = (JViewport) desktop.getParent ();
//      if (viewPort.getParent ()instanceof JScrollPane)
//        return (JScrollPane) viewPort.getParent ();
//    }
//    return null;
//  }
//
//  protected void resizeDesktop () {
//    int x = 0;
//    int y = 0;
//    int x1 = desktop.getWidth();
//    int y1 = desktop.getHeight();
//
//    JScrollPane scrollPane = getScrollPane();
//    Insets scrollInsets = getScrollPaneInsets();
//
//    if (scrollPane != null) {
//      JInternalFrame allFrames[] = desktop.getAllFrames();
//      int rightEdge, bottomEdge, leftEdge, topEdge;
//
//      for (int i = allFrames.length - 1; i >= 0; i--) { //find the right-most and bottom-most edges
//        if (!allFrames[i].isIcon()) {
//          leftEdge = allFrames[i].getX();
//          topEdge = allFrames[i].getY();
//          rightEdge = allFrames[i].getX() + allFrames[i].getWidth();
//          bottomEdge = allFrames[i].getY() + allFrames[i].getHeight();
//          x = (rightEdge > x) ? rightEdge : x;
//          y = (bottomEdge > y) ? bottomEdge : y;
//          x1 = (leftEdge < x1) ? leftEdge : x1;
//          y1 = (topEdge < y1) ? topEdge : y1;
//        }
//      }
//      Dimension d = scrollPane.getVisibleRect().getSize();
//      if (scrollPane.getBorder() != null) {
//        d.setSize(d.getWidth() - scrollInsets.left - scrollInsets.right,
//                  d.getHeight() - scrollInsets.top - scrollInsets.bottom);
//      }
//
//      if (x <= d.getWidth())
//        x = ( (int) d.getWidth()) - 20;
//      if (y <= d.getHeight())
//        y = ( (int) d.getHeight()) - 20;
//
//        //calculating space for scrolls
//      if (y1 != 0 && x1 != 0) {
//        
//        int viewableWidth = scrollPane.getWidth() - scrollInsets.left 
//		- scrollInsets.right;
//        if(scrollPane.getVerticalScrollBar().isVisible())
//        	viewableWidth -= scrollPane.getVerticalScrollBar().getWidth();
//        if(((x - x1) <= viewableWidth)&&(x1 > 0))
//        	x1 = 0;
//        
//        int viewableHeight = scrollPane.getHeight() - scrollInsets.top 
//		- scrollInsets.bottom;
//        if(scrollPane.getHorizontalScrollBar().isVisible())
//        	viewableHeight -= scrollPane.getHorizontalScrollBar().getHeight();
//        if(((y - y1) <= viewableHeight)&&(y1 > 0))
//        	y1 = 0;
//        
//        int last = allFrames.length;      
//        for (int i = 0; i < last; i++) {
//          if (!allFrames[i].isIcon()) {
//            int px = allFrames[i].getX() - x1;
//            int py = allFrames[i].getY() - y1;
//            allFrames[i].setLocation(new Point(px, py));
//          } 
//        }
//  
//        if(desktop.getParent().getParent() instanceof JScrollPane){
//          JScrollPane scroll = (JScrollPane)desktop.getParent().getParent();
//          scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getValue() - y1);
//          scroll.getHorizontalScrollBar().setValue(scroll.getHorizontalScrollBar().getValue() - x1);
//        }
//      }  
//      int viewableWidth = scrollPane.getWidth() - scrollInsets.left 
//		- scrollInsets.right;
//      if(scrollPane.getVerticalScrollBar().isVisible())
//      	viewableWidth -= scrollPane.getVerticalScrollBar().getWidth();
//      
//      int viewableHeight = scrollPane.getHeight() - scrollInsets.top 
//		- scrollInsets.bottom;
//      if(scrollPane.getHorizontalScrollBar().isVisible())
//      	viewableHeight -= scrollPane.getHorizontalScrollBar().getHeight();
//      
//      desktop.setAllSize(Math.max(x - x1, viewableWidth),
//    		  Math.max(y - y1, viewableHeight));
//      scrollPane.invalidate();
//      scrollPane.validate();
//          
//      //sets the icon on top and places them at the bottom of the view
//      int iconizedFrames = 0;
//      int visiblebar = 0;
//      
//      if(scrollPane.getHorizontalScrollBar().isVisible())
//    		visiblebar = scrollPane.getHorizontalScrollBar().getHeight();
//      
//      for (int i = 0; i < allFrames.length; i++)
//				try { // problems could occur when closing the window
//					if(!allFrames[i].isIcon())
//						allFrames[i].toBack();
//					else{
//						if(allFrames[i].getDesktopIcon().getSize().height>31 )
//							allFrames[i].getDesktopIcon().setSize(160,31);
//						
//						
//						int px = iconizedFrames*allFrames[i].getDesktopIcon().getWidth()
//								+ scrollPane.getViewport().getViewRect().x;
//						int py = scrollPane.getHeight() - scrollInsets.bottom 
//								- allFrames[i].getDesktopIcon().getHeight()
//								- visiblebar
//								+ scrollPane.getViewport().getViewRect().y;
//						
//						allFrames[i].getDesktopIcon().setLocation(px,py);
//						iconizedFrames++;
//					}
//				} catch (Exception e) { // do nothing
//				}
// 
//    }
//  }
//  public void repaintIcons(){
//	  int iconizedFrames = 0;
//      int visiblebar = 0;
//      JScrollPane scrollPane = getScrollPane();
//      Insets scrollInsets = getScrollPaneInsets();
//      JInternalFrame allFrames[] = desktop.getAllFrames();
//      if(scrollPane.getHorizontalScrollBar().isVisible())
//    		visiblebar = scrollPane.getHorizontalScrollBar().getHeight();
//      
//      for (int i = 0; i < allFrames.length; i++)
//  	    if(!allFrames[i].isIcon())
//  	    	allFrames[i].toBack();
//  	    else{
//  	    	if(allFrames[i].getDesktopIcon().getSize().height>31 )
//  	    		allFrames[i].getDesktopIcon().setSize(160,31);
//  	    	
//  	    	
//  	    	int px = iconizedFrames*allFrames[i].getDesktopIcon().getWidth()
//  	    			+ scrollPane.getViewport().getViewRect().x;
//  	    	int py = scrollPane.getHeight() - scrollInsets.bottom 
//  	    			- allFrames[i].getDesktopIcon().getHeight()
//  	    			- visiblebar
//  	    			+ scrollPane.getViewport().getViewRect().y;
//  	    	
//  	    	allFrames[i].getDesktopIcon().setLocation(px,py);
//  	    	iconizedFrames++;
//  	    }  
//  }
//  //
//  // More overridden methods... making of this a "complete" DesktopManager
//  //
//  public DesktopWatcher getWindowWatcher() { return ww; }
//
//  @Override
//  public void activateFrame(JInternalFrame f) {
//  	if (f!=null && f.getParent()!=null) {
//  		try { //the activateFrame() call sometimes generates a null pointer exception for some reason
//  			super.activateFrame(f);  
//  		} catch (Throwable e) { 
//  		}
//  	}
//  	repaintWindowWatcher();
//  }
//  @Override
//  public void beginDraggingFrame(JComponent f) {
//	super.beginDraggingFrame(f);
//    repaintWindowWatcher();
//  }
//  @Override
//  public void beginResizingFrame(JComponent f, int direction) {
//    super.beginResizingFrame(f,direction);
//    repaintWindowWatcher();
//  }
//  @Override
//  public void closeFrame(JInternalFrame f) {
//    super.closeFrame(f);
//    repaintWindowWatcher();
//  }
//  @Override
//  public void deactivateFrame(JInternalFrame f) {
//     super.deactivateFrame(f);
//    repaintWindowWatcher();
//  }
//  @Override
//  public void deiconifyFrame(JInternalFrame f) {
//    super.deiconifyFrame(f);
//    repaintWindowWatcher();
//  }
//  @Override
//  public void dragFrame(JComponent f, int newX, int newY) {
//    f.setLocation(newX, newY);
//    repaintIcons();						
//    repaintWindowWatcher();
//  }
//  @Override
//  public void iconifyFrame(JInternalFrame f) {
//    super.iconifyFrame(f);
//    repaintWindowWatcher();
//  }
//  @Override
//  public void maximizeFrame(JInternalFrame f) {
//	
//	Dimension d = new Dimension();
//  	Point p = new Point();
//	if(!f.isIcon()) {
//    	d = desktop.getParent().getSize();
//    	p = ((JViewport) desktop.getParent()).getViewPosition();
//    } else {
//        Container c = f.getDesktopIcon().getParent();
//        if(c == null)
//            return;
//        d = c.getSize();
//      //  Rectangle r = c.getBounds();
//        try { f.setIcon(false); } catch (PropertyVetoException e2) { }
//    }
//    f.setNormalBounds(f.getBounds());
//
//    setBoundsForFrame(f, p.x, p.y, d.width, d.height);
//    try { f.setSelected(true); } catch (PropertyVetoException e2) { }
//    removeIconFor(f);
//    repaintWindowWatcher();
//    resizeDesktop();
//  }
//  @Override
//  public void minimizeFrame(JInternalFrame f) {
//	super.minimizeFrame(f);
//    resizeDesktop();
//    repaintWindowWatcher();
//  }
//  @Override
//  public void openFrame(JInternalFrame f) {
//    super.openFrame(f);
//    repaintWindowWatcher();
//  }
//  @Override
//  public void resizeFrame(JComponent f,
//   int newX, int newY, int newWidth, int newHeight) {
//    f.setBounds(newX, newY, newWidth, newHeight);
//    repaintWindowWatcher();
//  }
//  @Override
//  public void setBoundsForFrame(JComponent f,
//   int newX, int newY, int newWidth, int newHeight) {
//    f.setBounds(newX, newY, newWidth, newHeight);
//    repaintWindowWatcher();
//  }
//}