package casa.socialcommitments.ui;

import casa.socialcommitments.SocialCommitmentComparator;
import casa.socialcommitments.SocialCommitmentsStore;
import casa.ui.AbstractFadingListModel;

import java.awt.Dimension;

import javax.swing.*;

import casa.ui.RefreshTimerJList;

/**
 * <code>SocialCommitmentJList</code> is a JList that extends RefreshTimerJList for pretty-printing and fading. <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p> TODO Add description to JavaDoc file header. TODO Add filter delegate method.
 * @author  Jason Heard
 * @author  <a href="mailto:sedachv@cpsc.ucalgary.ca">Vladimir Sedach</a>
 * @version 0.9
 */
public class SocialCommitmentJList extends RefreshTimerJList {
  /**
	 */
  private final SocialCommitmentListModel sclm;

  public SocialCommitmentJList (SocialCommitmentListModel model, int refreshDelay) {
  	super((AbstractFadingListModel)model, refreshDelay);
    sclm = model;
  }


  /**
   * setCommitmentComparator does... TODO Finish documenting the
   * setCommitmentComparator method.
   * 
   * @param comparator
   */
  public void setCommitmentComparator (
      SocialCommitmentComparator commitmentComparator) {
    sclm.setComparator (commitmentComparator);
  }

  public static JComponent getListPanel (SocialCommitmentsStore store) {
    SocialCommitmentJList socialCommitmentList = new SocialCommitmentJList(new SocialCommitmentListModel(store), 500);
    JScrollPane scrollSCList = new JScrollPane (socialCommitmentList);
    scrollSCList.setPreferredSize (new Dimension (245, 100));

    return scrollSCList;
  }
}