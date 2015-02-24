package casa.socialcommitments.operators;

import casa.Act;
import casa.ML;
import casa.MLMessage;
import casa.Status;
import casa.agentCom.URLDescriptor;
import casa.event.Event;
import casa.event.MessageEvent;
import casa.interfaces.PolicyAgentInterface;
import casa.socialcommitments.SocialCommitment;
import casa.socialcommitments.SocialCommitmentsStore;
import casa.util.InstanceCounter;

import java.util.Collection;

/**
 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
public class CancelSocialCommitment extends SocialCommitmentOperator {
	
	/**
	 */
	private URLDescriptor debtor;
	/**
	 */
	private URLDescriptor creditor;
	private String performative;
	/**
	 */
	private Act act;
	/**
	 */
	private Event event;

	public CancelSocialCommitment (URLDescriptor debtor, URLDescriptor creditor,
			String performative, Act act, Event event) {
		super ();
		this.debtor = debtor;
		this.creditor = creditor;
		this.performative = performative;
		this.act = act;
		this.event = event;
		InstanceCounter.add(this);
	}

	public Status executeOperator (SocialCommitmentsStore store, PolicyAgentInterface agent) {
		Status ret = new Status (0);
		
		int count = 0;
		int total = 0;

		// clean up the values we want to match
		String perfX = performative==null ? ML.TOP : performative;
		Act actX = act==null ? new Act(ML.TOP) : act;
		
		String cIDX  = getMessage()!=null ? getMessage().getParameter(ML.CONVERSATION_ID) : "";
		if (cIDX==null) {
			cIDX="";
		}

		// get outstanding commitments with the given debtor and creditor
		Collection<SocialCommitment> c = store.getCommitments(debtor,creditor,SocialCommitment.MASK_OUTSTANDING);

		for (SocialCommitment sc: c) {
			// count the number of commitments we look at
			total++;
			
			// get and clean up the values in the SC we will be comparing
			String perfSC = sc.getPerformative(); if (perfSC==null) perfSC = /*ML.PERFORMATIVE*/ML.TOP;
			Act actSC = sc.getAct(); if (actSC==null) actSC = new Act(ML.TOP);
			String cIDSC = sc.getMessage().getParameter(ML.CONVERSATION_ID); if (cIDSC==null) cIDSC="";
			
			// To cancel the commitment, the conversation IDs must match, performative and act that we are
			// fulfilling must be sub-type of those in the commitment.
			if (cIDX.equals (cIDSC) &&
				agent.isA (perfX, perfSC) &&
				agent.isAAct (actX, actSC)) {
				
				if (agent.isLoggingTag ("commitments2")) {
					agent.println ("commitments2", "Operator cancelling commitment '" + sc.toString () + "'");
				}
				sc.designateCanceled ();
				count++;
			}
		}

		if (ret.getStatusValue()==0) {
			ret.setExplanation("Applied to "+count+" of "+total+" social commitments");
		}
		
		return ret;
	}
	
  /**
	 * Retrieves the message in the event, if there is one; else null.
	 * @return  The message in the event.
	 */
  protected MLMessage getMessage() {
    return (event instanceof MessageEvent)? ((MessageEvent)event).getMessage() : null;
  }

	@Override
	public String toString () {
	    StringBuffer buf = new StringBuffer ();
	    buf.append ("Cancel: D: ");
	    buf.append (debtor.getShortestName ());

	    buf.append (", C: ");
	    buf.append (creditor.getShortestName ());

	    buf.append (", (");
	    buf.append (performative == null ? "*" : performative);
	    buf.append ("/");
	    buf.append (act == null ? "*" : act.toString ());
	    buf.append (")");

	    return buf.toString ();
	}
}
