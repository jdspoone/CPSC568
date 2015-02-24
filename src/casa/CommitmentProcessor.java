package casa;

import casa.interfaces.PolicyAgentInterface;
import casa.socialcommitments.SocialCommitment;
import casa.socialcommitments.SocialCommitmentsStore;
import casa.util.InstanceCounter;

/**
 * <p>Title: CASA Agent Infrastructure</p> <p>Description: </p> <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The  Knowledge Science Group makes no representations about the suitability of  this software for any purpose.  It is provided "as is" without express or implied warranty.</p> <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public abstract class CommitmentProcessor {

  /**
	 * a non-null "owner" agent that must be past in the constructor
	 */
  protected PolicyAgentInterface agent;

  public CommitmentProcessor(PolicyAgentInterface agent) {
    assert agent!=null : "MessageProcessor constructor must take a non-null agent";
    this.agent = agent;
  	InstanceCounter.add(this);
  }

  /**
   *
   * @param remoteAgent
   * @return true if it did something; otherwise false
   */
  public abstract SocialCommitment processCommitments();
  
  public abstract SocialCommitmentsStore getStore();
  
  public abstract SocialCommitment peek();

  /**
   * This method is to be overridden by any subclasses that wish to be informed
   * whenever a message is actually sent out.  This verion does nothing.
   * @param message
   */
  public void notifyMessageSent(MLMessage message) {}

  /**
   * determines if this process can be stopped.  A subclass may override if there
   * are more constraints on stopping.  This implementation checks to see that
   * event queue is empty and there are no (non-standing-request) requests
   * with replies pending.
   * @return true if the process is stoppable, false otherwise.
   */
  public abstract boolean isStoppable();

	public abstract boolean hasActiveCommitments();
	
	protected abstract SocialCommitment choose();

	public abstract SocialCommitment processCommitment(SocialCommitment sc);
}