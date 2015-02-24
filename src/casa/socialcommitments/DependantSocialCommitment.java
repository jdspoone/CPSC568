package casa.socialcommitments;

import casa.Act;
import casa.ML;
import casa.ObserverNotification;
import casa.PerformDescriptor;
import casa.agentCom.URLDescriptor;
import casa.event.Event;
import casa.event.TriggerEvent;
import casa.interfaces.PolicyAgentInterface;

import java.util.Observable;
import java.util.Observer;

/**
 * <p> Title: CASA Agent Infrastructure </p> <p> Description: </p> <p> Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation. The Knowledge Science Group makes no representations about the suitability of this software for any purpose. It is provided "as is" without express or implied warranty. </p> <p> Company: Knowledge Science Group, University of Calgary </p> A social commitment that is dependent upon another social commitment (the 'guard') to 'fire' (be executed). This is implemented by the observer pattern, where this commitment observes the SCObservable commitment, which will call this commitment's update() method, which will set it's READY status to "true". (SCDependent commitments are initialized with their READY status to false.)
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public class DependantSocialCommitment extends SocialCommitment implements Observer /*SocialCommitmentObserver*/ {

  /**
	 * The agent we're observing
	 */
  private SocialCommitment guard;

  /**
	 * The status returned by the observed agent, guard
	 */
  private PerformDescriptor subjectStatus = null;

  /**
	 */
  private TriggerEvent guardEvent;

  /**
   * 
   * @param guard
   * @param newDebtor
   * @param newCreditor
   * @param newPerformative
   * @param newAct
   * @param msg
   * @param action
   */
  public DependantSocialCommitment (PolicyAgentInterface agent, SocialCommitment guard,
      URLDescriptor newDebtor, URLDescriptor newCreditor,
      String newPerformative, Act newAct, Event event, Action action) {
    super (agent, newDebtor, newCreditor, newPerformative, newAct, event, action,
        new TriggerEvent (ML.EVENT_SC_PERFORM_ACTION,guard==null?false:guard.flagSet (SocialCommitmentStatusFlags.RECURRING), agent));
    guardEvent = (TriggerEvent) getEventOfType (ML.EVENT_SC_PERFORM_ACTION);
    setGuard (guard);
  }

  public DependantSocialCommitment (PolicyAgentInterface agent, SocialCommitment guard,
      URLDescriptor newDebtor, URLDescriptor newCreditor,
      String newPerformative, Act newAct, Event event, Action action,
      Event... events) {
    super (agent, newDebtor, newCreditor, newPerformative, newAct, event, action,
        enbiggen (new TriggerEvent (ML.EVENT_SC_PERFORM_ACTION,guard==null?false:guard.flagSet (SocialCommitmentStatusFlags.RECURRING), agent), events));
    guardEvent = (TriggerEvent) getEventOfType (ML.EVENT_SC_PERFORM_ACTION);
    setGuard (guard);
  }

  private static Event[] enbiggen (Event e, Event... events) {
	  Event[] newEvents;
	  if (events != null) {
		  newEvents = new Event[events.length + 1];
		  System.arraycopy (events, 0, newEvents, 0, events.length);
		  newEvents[events.length] = e;
	  } else {
		  newEvents = new Event[1];
		  newEvents[0] = e;
	  }
	  return newEvents;		  
  }

//  /**
//   * Called by the observable commitment when it's execute() method is called.
//   * 
//   * @param observable
//   * @param stat
//   */
//  public void update (SocialCommitment observable, PerformDescriptor stat) {
//    if (guard != null && guard.equals (observable)) {
//      subjectStatus = stat;
//      guardEvent.trigger ();
//    }
//  }

	@Override
	public void update(Observable o, Object arg) {
		ObserverNotification note = (ObserverNotification)arg; 
		String type = note.getType();
    if (guard != null && guard.equals (o) && ML.EVENT_SC_PERFORM_ACTION.equals(type)) {
      subjectStatus = (PerformDescriptor)note.getObject();
      guardEvent.trigger ();
    }
    else {
		  super.update(o, arg);
    }
	}

  @Override
  protected StringBuffer attributesString () {
    StringBuffer display = super.attributesString ();
    display.append (" depends-on=").append (
        guard != null ? Long.toString (guard.getSerialNumber ()) : "none");
    return display;
  }

  /**
   * @return the value of the execution of the Action of the guard commitment,
   *         or null if the guard commitment's Action has not been executed.
   */
  public PerformDescriptor getGuardStatus () {
    return subjectStatus;
  }

  /**
	 * Sets the guard and sets this commitment's READY state to false (awaiting the guard's execution) if the 
	 * guard is non-null, READY goes to true otherwise.
	 * @param newGuard  The guard
	 */
  public void setGuard (SocialCommitment newGuard) {
    this.guard = newGuard;
    if (guard == null) {
      guardEvent.trigger ();
    } else {
      guard.addObserver (this);
    }
  }
 
  /**
   * Called whenever the SocialCommitment enters a terminal state (FILFILLED, CANCELED, or VIOLATED).
   * Subclasses should override this method to clean up as appropriate.
   */
  @Override
  protected void enterTerminalState() {
  	super.enterTerminalState();
    if (guard!=null) {
    	guard.deleteObserver(this);
    }
  }

}