/**
 * <p>
 * Copyright: Copyright 2003-2014, Knowledge Science Group, University of
 * Calgary. Permission to use, copy, modify, distribute and sell this software
 * and its documentation for any purpose is hereby granted without fee, provided
 * that the above copyright notice appear in all copies and that both that
 * copyright notice and this permission notice appear in supporting
 * documentation. The Knowledge Science Group makes no representations about the
 * suitability of this software for any purpose. It is provided "as is" without
 * express or implied warranty.
 * </p>
 */
package casa.socialcommitments.ui;

import casa.socialcommitments.*;

import jade.util.leap.LinkedList;

import java.util.Collections;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import casa.ui.AbstractFadingListModel;
import casa.util.Trace;

/**
 * <code>SocialCommitmentListModel</code> is an implementation of AbstractFadingListModel that is used to fade out the list of commitments in a pretty way.
 * @author  Jason Heard
 * @author  <a href="mailto:sedachv@cpsc.ucalgary.ca">Vladimir Sedach</a>
 * @version 0.9
 */
public class SocialCommitmentListModel extends AbstractFadingListModel
		implements /*SocialCommitmentStoreObserver*/Observer {
	private static final long serialVersionUID = 7933945544874608242L;
	/**
	 */
	SocialCommitmentsStore commitmentStore;
	Vector<SocialCommitment> commitmentList;
	/**
	 */
	SocialCommitmentComparator comparator;
	/**
	 */
	SocialCommitmentFilter filter;

	/**
	 * Creates a new <code>SocialCommitmentList</code> object.
	 * 
	 * TODO Fill in specific information for constructor.
	 * 
	 * 
	 */
	public SocialCommitmentListModel (SocialCommitmentsStore newCommitmentStore) {
		super ();

		commitmentStore = newCommitmentStore;
		commitmentList = new Vector<SocialCommitment>();
		//TODO comparator really isn't used; remove it when you have time to check.  rck.
		comparator = new FulfilledLastSocialCommitmentComparator ();
		filter = new OldSocialCommitmentFilter (FADE_TIME);

		updateList ();
		commitmentStore.addObserver (this);
	}

	/**
	 * Updates the commiment list used by the list model based on the social
	 * commitment store and the current comparator.
	 */
	private synchronized void updateList () {
		int end = commitmentList.size ();
		commitmentList.clear ();
		if (end > 0) {
			fireIntervalRemoved (this, 0, end - 1);
		}

		long now = System.currentTimeMillis ();
		SocialCommitment tempCommitment;
		Iterator i = commitmentStore.getAllCommitments ().iterator ();
		while (i.hasNext ()) {
			tempCommitment = (SocialCommitment) i.next ();
			if (filter.keepSocialCommitment (tempCommitment, now)) {
				commitmentList.add (tempCommitment);
			}
		}
		Collections.sort (commitmentList, comparator);

		fireIntervalAdded (this, 0, commitmentList.size () - 1);
	}

	/**
	 * This changes the comparator used to sort the list.
	 * @param comparator  The comparator to set.
	 */
	public void setComparator (SocialCommitmentComparator commitmentComparator) {
		this.comparator = commitmentComparator;

		Collections.sort (commitmentList, commitmentComparator);

		fireContentsChanged (this, 0, commitmentList.size () - 1);
	}

	@Override
	public Object getElementAt (int index) {
		SocialCommitment socialCommitment = commitmentList.get(index);

		return SocialCommitmentHTMLFormatter.formatCommitment (socialCommitment, FADE_TIME);
	}

	@Override
	public int getSize () {
		return commitmentList.size ();
	}

	public synchronized void removeMember (SocialCommitment socialCommitment) {
		int index = Collections.binarySearch (commitmentList, socialCommitment,
				comparator);

		if (index >= 0) {
			commitmentList.remove (index);
			fireIntervalRemoved (this, index, index);
			Trace.log("commitments9", "SocialCommitmentListModel.removeMember(): Removed: "+socialCommitment);
		}
		else
			Trace.log("error", "SocialCommitmentListModel.removeMember(): Failed to remove: "+socialCommitment);
	}

	/**
	 * commitmentRemoved does...
	 * 
	 * TODO Finish documenting the commitmentRemoved method.
	 * 
	 * @param socialCommitment
	 */
	@Override
	public void removeMember (Object socialCommitment) {
		assert (socialCommitment instanceof SocialCommitment);
		removeMember ((SocialCommitment) socialCommitment);
	}

	public synchronized void addMember (SocialCommitment socialCommitment) {
		if (filter.keepSocialCommitment (socialCommitment)) {
			int index = Collections.binarySearch (commitmentList,
					socialCommitment, comparator);

			if (index < 0) {
				index = - (index + 1);
				commitmentList.add (index, socialCommitment);
				fireIntervalAdded (this, index, index);
			}
		}
	}
	
	

	@Override
	public void update(Observable o, Object arg) {
		if (arg instanceof SocialCommitmentsStore.Operation) {
			SocialCommitmentsStore.Operation op = (SocialCommitmentsStore.Operation)arg;
			if (op.getFirst()==SocialCommitmentsStore.Operator.ADD) 
				addMember(op.getSecond());
			else if (op.getFirst()==SocialCommitmentsStore.Operator.REMOVE) 
				purge(); //removeMember(op.getSecond());
		}
		
	}
	
  final static private long purgeInterval = 60000;
  private long purgeDueTime = System.currentTimeMillis()+purgeInterval;
  
	private void purge() {
		if (System.currentTimeMillis()<purgeDueTime) 
			return;
		
  	purgeDueTime = System.currentTimeMillis()+purgeInterval;

  	final long expiredTime = System.currentTimeMillis() - (AbstractFadingListModel.FADE_TIME+1000); //add a second to the fade time to be on the safe side
  	
		//gather the expired element indexes in target. This needs to be in largest-to-smallest order.
		Vector<Integer> targets = new Vector<Integer>();
		Vector<Long> deletedIDs = new Vector<Long>();
		Vector<Long> keptIDs = new Vector<Long>();
		for (int i=commitmentList.size()-1; i>=0; i--) {
			SocialCommitment sc = commitmentList.elementAt(i);
			long endTime = sc.getEndTime();
			if (endTime!=0 && endTime<expiredTime) {
				targets.add(i);
				deletedIDs.add(sc.getSerialNumber());
			}
			else {
				keptIDs.add(sc.getSerialNumber());
			}
		}
		
		// remove each of the expired indexes from the list (back to front)
		for (int i: targets) {
			commitmentList.remove(i);
		}
		Trace.log("commitments", "SocialCommitmentListModel.purge(): Removed "+targets.size()+" SCs "+deletedIDs+"; "+commitmentList.size()+" remaining "+keptIDs+".");
	}

	/**
	 * commitmentAdded does...
	 * 
	 * TODO Finish documenting the commitmentAdded method.
	 * 
	 * @param socialCommitment
	 */
	@Override
	public void addMember (Object socialCommitment) {
		assert (socialCommitment instanceof SocialCommitment);
		addMember ((SocialCommitment) socialCommitment);
	}

	@Override
	public synchronized void refreshList () {
		long now = System.currentTimeMillis ();

		@SuppressWarnings("unchecked")
		Vector<SocialCommitment> oldCList = (Vector<SocialCommitment>)commitmentList.clone();
		for (SocialCommitment sc : oldCList) {
			if (!filter.keepSocialCommitment(sc, now)) {
				int interval = commitmentList.indexOf(sc);
				commitmentList.remove(sc);
				fireIntervalRemoved (this, interval, interval);
			}
		}

		Collections.sort (commitmentList, comparator);
		fireContentsChanged (this, 0, commitmentList.size() - 1);
	}
}