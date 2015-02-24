package casa.ui;

import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.Timer;

/**
 * 
 * A JList that periodically refreshes it's ListModel according to the protocol
 * defined in AbstractFadingListModel. 
 * 
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary.
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  The
 * Knowledge Science Group makes no representations about the suitability of
 * this software for any purpose.  It is provided "as is" without express or
 * implied warranty.</p>
 * 
 * Created on May 11, 2005
 * 
 * @version 0.9
 * 
 * @author <a href="mailto:sedachv@cpsc.ucalgary.ca">Vladimir Sedach</a>
 *
 */
public class RefreshTimerJList extends JList {
	private final Timer listRefreshTimer;

	public RefreshTimerJList (AbstractFadingListModel model, int refreshDelay) {
		super.setModel(model);
	    setEnabled (false);
	    
	    final AbstractFadingListModel listModel = model;
	    final JList This = this;
	    
	    ActionListener taskPerformer = new ActionListener () {
		      public void actionPerformed (ActionEvent event) {
		      	if (This.isShowing()) { //no sense updating if the window isn't showing (visible)
		          listModel.refreshList ();
		      	}
		      }
		    };
		    listRefreshTimer = new Timer (refreshDelay, taskPerformer);
		    listRefreshTimer.start ();
	}
	
	  /**
	   * Disallow model changes.
	   */
	  @Override
	  public void setModel (ListModel model) {
	    throw new RuntimeException ("List model may not be changed.");
	    // If you want to change this, talk to Jason first!
	  }

	  /**
	   * Stops the refresh timer.
	   */
	  @Override
	protected void finalize () throws Throwable {
	    listRefreshTimer.stop ();
	    super.finalize ();
	  }

}
