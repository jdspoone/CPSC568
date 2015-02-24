package casa.ui;

import javax.swing.AbstractListModel;

/**
 * 
 * The AbstractFadingListModel class is an abstract class that defines several methods
 * and variables that are used to implement a ListModel that can return objects that
 * can change with time.
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
 * @author <a href="mailto:sedachv@cpsc.ucalgary.ca">Vladimir Sedach</a>
 * 
 * @version 0.9
 */
public abstract class AbstractFadingListModel extends AbstractListModel {
	// How long (in milliseconds) it takes for a list member to completely fade out
	public static final long FADE_TIME = 120000;
	
	/*
	 * Called to signal that the specified member of the list has been removed.
	 * This method is not obligated to remove that member right away, but may
	 * mark it for later removal and start to fade out it's visual representation.
	 */
	public abstract void removeMember(Object member);
	
	/*
	 * Called to add the specified member to the list.
	 */
	public abstract void addMember(Object member);
	
	/*
	 * This method is called regularly by a timer, so any code for time-related changes
	 * should be placed here.
	 */
	public abstract void refreshList();

}
