package casa.socialcommitments;

import casa.Act;
import casa.agentCom.URLDescriptor;
import casa.event.Event;
import casa.interfaces.PolicyAgentInterface;
import casa.ui.AbstractFadingListModel;
import casa.util.CASAUtil;
import casa.util.InstanceCounter;
import casa.util.PairComparable;
import casa.util.Trace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * <code>SocialCommitmentStore</code> is a ... TODO Add description to JavaDoc file header.
 * @author  Jason Heard
 * @version 0.9
 */
public class SocialCommitmentsStore extends Observable implements Comparable<SocialCommitmentsStore> {
	
	class SCs extends ConcurrentSkipListSet<SocialCommitment> {
		private static final long serialVersionUID = 9147760729271186069L;
	}
	
	class InnerMap extends ConcurrentSkipListMap<URLDescriptor, SCs> implements Comparable<InnerMap>{
		private static final long serialVersionUID = -8534915954242128433L;
		@Override
		public int compareTo(InnerMap arg0) {
			for (Iterator<URLDescriptor> me = keySet().iterator(), you = arg0.keySet().iterator(); me.hasNext() && you.hasNext();  ) {
				int ret = me.next().compareTo(you.next());
				if (ret!=0)
					return ret;
			}
			int meSize = keySet().size();
			int youSize = arg0.keySet().size();
			if (meSize<youSize)
				return -1;
			if (meSize>youSize)
				return 1;
			return 0;
		}
	}
	
	class OuterMap extends ConcurrentSkipListMap<URLDescriptor, InnerMap> {
		private static final long serialVersionUID = 8781676121181900124L;
	}
	
  /**
   * Stores all of the commitments. It is a ConcurrentSkipListMap from
   * <code>URLDescriptors</code> (debtor) to <code>ConcurrentSkipListMap</code>. Each inner
   * <code>ConcurrentSkipListMap</code> is from <code>URLDescriptors</code> (creditor) to
   * <code>Vectors</code>. Each <code>Vector</code> contains
   * <code>SocialCommitments</code>.
   */
  private OuterMap allCommitments = new OuterMap ();

  /**
   * Redundant storage of all commitments. Used in cases where all commitments
   * need to be recalled.
   */
  private SCs commitmentsVector = new SCs();

  protected long transactionCount = 0;

  /**
   * Creates a new <code>SocialCommitmentStore</code> object.
   *
   * TODO Fill in specific information for constructor.
   */
  public SocialCommitmentsStore (final PolicyAgentInterface owner) {
  	addObserver(owner);
  	InstanceCounter.add(this);
  }
  
	private synchronized InnerMap getInnerMap (URLDescriptor debtor) {
    InnerMap innerMap = allCommitments.get (debtor);

    // these two checks and corrections are here to handle the unresolved/resolved URL cases
//    if (innerMap == null) { //it could be we have an entry with a *path* hash, and now the port is resolved (this happens with the LAC when we're doing a register-instance)
//    	URLDescriptor portlessURL = new URLDescriptor(debtor);
//    	portlessURL.setPort(0);
//    	innerMap = allCommitments.get (portlessURL);
//    	if (innerMap != null) { //swap out the old map and in with the new
//    		allCommitments.put(debtor, innerMap);
//    		allCommitments.remove(portlessURL);
//    	}
//    }
//    if (innerMap == null) { //on the other hand, we could have re-positioned the innerMap (as above), and the agent is asking for it with an unresolved URL
//    	for (URLDescriptor key : allCommitments.keySet()) {
//    		URLDescriptor portlessURL = new URLDescriptor(key);
//      	portlessURL.setPort(0);
//      	if (portlessURL.equals(debtor)) {
//      		innerMap = allCommitments.get(key);
//      	}
//    	}
//    }
    
    if (innerMap == null) {
      innerMap = new InnerMap ();
      allCommitments.put (debtor, innerMap);
    }

    return innerMap;
  }

  private SCs getVector (URLDescriptor debtor, URLDescriptor creditor,
      boolean create) {
    InnerMap innerMap = getInnerMap (debtor);
    SCs commitments = null;
    if (creditor == null) {
      commitments = getVector (debtor);
    } else {
      commitments = innerMap.get (creditor);
      
      // these two checks and corrections are here to handle the unresolved/resolved URL cases
//      if (commitments==null) { //it could be we have an entry with a *path* hash, and now the port is resolved (this happens with the LAC when we're doing a register-instance)
//      	URLDescriptor portlessURL = new URLDescriptor(creditor);
//      	portlessURL.setPort(0);
//      	commitments = innerMap.get (portlessURL);
//      	if (commitments != null) { //swap out the old map and in with the new
//      		innerMap.put(creditor, commitments);
//      	  innerMap.remove(portlessURL);
//      	}
//      }
//      if (innerMap == null) { //on the other hand, we could have re-positioned the commitments (as above), and the agent is asking for it with an unresolved URL
//      	for (URLDescriptor key : innerMap.keySet()) {
//      		URLDescriptor portlessURL = new URLDescriptor(key);
//        	portlessURL.setPort(0);
//        	if (portlessURL.equals(creditor)) {
//        		commitments = innerMap.get(key);
//        	}
//      	}
//      }
      
    }
    if (create && commitments == null) {
      commitments = new SCs ();
      innerMap.put (creditor, commitments);
    }

    return commitments;
  }

  private SCs getVector (URLDescriptor debtor) {
    InnerMap innerMap = getInnerMap (debtor);

    Set<URLDescriptor> keys = innerMap.keySet ();
    if (keys.isEmpty())
      return null;
    SCs commitments = new SCs ();
    for (URLDescriptor key: keys) {
      commitments.addAll (innerMap.get (key));
    }
    return commitments;
  }

  private SCs getVector (SocialCommitment socialCommitment, boolean create) {
    return getVector (socialCommitment.getDebtor (), socialCommitment
        .getCreditor (), create);
  }

  public synchronized void addCommitment (SocialCommitment socialCommitment) {
  	SCs commitments = getVector (socialCommitment, true);

    commitments.add (socialCommitment);

    commitmentsVector.add (socialCommitment);
    transactionCount++;
    
    purgeCheck();
    
    setChanged();
    notifyObservers(new Operation(Operator.ADD, socialCommitment));
  }
  
  protected void purgeCheck() {
    if (System.currentTimeMillis()>purgeDueTime /*|| Runtime.getRuntime().freeMemory()<10000*/) {
    	int count = allCommitments.size();
    	int purged = purge();
    	Trace.log("info","Purged "+purged+" Social Commitments of "+count);
    }
  }

  /**
   * This adds a commitment with the specified arguments. It uses
   * addCommitment(SocialCommitment) to limit the number of modifying functions.
   *
   * @param debtor
   * @param creditor
   * @param msg
   * @param action
   */
  public void addCommitment (PolicyAgentInterface agent, URLDescriptor debtor, URLDescriptor creditor,
      String performative, Act act, Event event, Action action) {
  	
    SocialCommitment newSC = new SocialCommitment (agent, debtor, creditor,
        performative, act, event, action);
    addCommitment (newSC);
  }

  public List<SocialCommitment> getCommitments (URLDescriptor debtor, URLDescriptor creditor) {
  	return getCommitments (debtor, creditor, SocialCommitment.MASK_ALL);
  }

  public List<SocialCommitment> getCommitments (URLDescriptor debtor,
      URLDescriptor creditor, Set<SocialCommitmentStatusFlags> mask) {
  	SCs tempVector = getVector (debtor, creditor, false);
  	SCs v = new SCs();
    if (tempVector==null) return new LinkedList<SocialCommitment>(v);
    for (SocialCommitment sc : tempVector) {
      if (sc.meetsMask (mask)) {
        v.add (sc);
      }
    }
    return new LinkedList<SocialCommitment>(v);
  }

  public synchronized void removeCommitment (SocialCommitment socialCommitment) {
  	SCs commitments = getVector (socialCommitment, false);

    if (commitments != null) {
      commitments.remove (socialCommitment);
    }
    else
    	Trace.log("error", "SocialCommitmentStore.removeCommitment(): Cannot remove commitment: "+ socialCommitment);

    if (!commitmentsVector.remove (socialCommitment))
    	Trace.log("error", "SocialCommitmentStore.removeCommitment(): Cannot remove commitment: "+ socialCommitment);
    
    transactionCount++;
    
    purgeCheck();

  }

  /**
   * This removes a commitment formed from the parameters. This uses
   * removeCommitment, to limit the number of functions that actually change
   * things.
   *
   * @param debtor
   * @param creditor
   * @param performative
   * @param act
   * @param msg
   * @param action
   */
  public void removeCommitment (PolicyAgentInterface agent, URLDescriptor debtor, URLDescriptor creditor,
      String performative, Act act, Event event, Action action) {
    SocialCommitment socialCommitment = new SocialCommitment (agent, debtor, creditor,
        performative, act, event, action);
    removeCommitment (socialCommitment);
  }

  /**
	 * @return
	 */
  public Collection<SocialCommitment> getAllCommitments () {
    return Collections.unmodifiableCollection (commitmentsVector);
  }
  
  /**
	 * @return unfulfilled
	 */
  public Collection<SocialCommitment> getUnfulfilledCommitments(String id){
  	SCs unfulfilled = new SCs();
  	
  	for (SocialCommitment sc : commitmentsVector)
  		if (sc.meetsMask(SocialCommitment.MASK_OUTSTANDING) && 
  				sc.getOwnerConversationID().equals(id))
  			unfulfilled.add(sc);
  	return unfulfilled;
  }

  /**
	 * Retrieves all unfulfilled social commitments, as opposed to just ones
	 * associated with a particular conversation ID
	 * 
	 * @return unfulfilled

	 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
	 */
  public Collection<SocialCommitment> getUnfulfilledCommitments(){
  	Collection<SocialCommitment> unfulfilled = new ArrayList<SocialCommitment>();
  	
  	for (SocialCommitment sc : commitmentsVector)
  		if (sc.meetsMask(SocialCommitment.MASK_OUTSTANDING))
  			unfulfilled.add(sc);
  	return unfulfilled;
  }
  
//  public void addObserver (SocialCommitmentStoreObserver observer) {
////    if (!observers.contains (observer)) {
//      observers.add (observer);
////    }
//  }
//
//  public void removeObserver (SocialCommitmentStoreObserver observer) {
////    observers.remove (observer);
//  }
//
//  /**
//   * notifyCommitmentRemoved does...
//   *
//   * TODO Finish documenting the notifyCommitmentRemoved method.
//   *
//   * @param socialCommitment
//   */
//  private void notifyCommitmentRemoved (SocialCommitment socialCommitment) {
//    for (SocialCommitmentStoreObserver sclm : observers) {
//      sclm.removeMember (socialCommitment);
//    }
//  }
//
//  /**
//   * notifyCommitmentAdded does...
//   *
//   * TODO Finish documenting the notifyCommitmentAdded method.
//   *
//   * @param socialCommitment
//   */
//  private void notifyCommitmentAdded (SocialCommitment socialCommitment) {
//    for (SocialCommitmentStoreObserver sclm : observers) {
//      sclm.addMember (socialCommitment);
//    }
//  }
  
  public enum Operator {ADD, REMOVE};
  public class Operation extends PairComparable<Operator, SocialCommitment> {
  	Operation(Operator op, SocialCommitment sc) {super(op, sc);}
  }

  @Override
  public synchronized String toString () {
    StringBuffer buf = new StringBuffer ();
    for (SocialCommitment sc : getAllCommitments()) {
      buf.append ("  \n").append (CASAUtil.serialize (sc)).append (';');
    }
    return buf.toString ();
  }

  public synchronized String displayString(Set<SocialCommitmentStatusFlags> mask) {
    StringBuffer buf = new StringBuffer();
    for (SocialCommitment sc : getAllCommitments()) {
      if (sc.meetsMask (mask))
        buf.append("  ").append(sc.toString ()).append(";\n");
    }
    return buf.toString();
  }

  /**
	 * @return
	 */
  public long getTransactionCount () {
    return transactionCount;
  }

  public synchronized int count(Set<SocialCommitmentStatusFlags> mask) {
    int count=0;
    for (SocialCommitment sc : getAllCommitments()) {
    	if (mask==null) 
    	  count++;
    	else {
        if (sc.meetsMask (mask)) count++;
    	}
    }
    return count;
  }

  public synchronized boolean hasCommitment(Set<SocialCommitmentStatusFlags> mask) {
    // Don't use getAllCommitments for performance reasons
    for (SocialCommitment sc : commitmentsVector) {
      if (sc.meetsMask (mask))
          return true;
    }
    return false;
  }
  
  final static private long purgeInterval = 60000;
  private long purgeDueTime = System.currentTimeMillis()+purgeInterval;
  
  public synchronized int purge() {
  	purgeDueTime = System.currentTimeMillis()+purgeInterval;
  	int count = 0;
  	final long expiredTime = System.currentTimeMillis() - (AbstractFadingListModel.FADE_TIME+1000);
  	boolean removedOuter = true;
  	Vector<Long> removedSCs = new Vector<Long>();
  	Vector<Long> cantRemove = new Vector<Long>();
  	while (removedOuter) {
  		removedOuter = false;
  		outerLoop:
  			for (InnerMap outer: allCommitments.values()) {
  				if (outer.isEmpty()) {
  					allCommitments.values().remove(outer);
						Trace.log("commitments9", "SocialCommitmentStore.purge(): Removed group (from outer) ");
  					removedOuter = true;
  					break outerLoop;
  				}
  				boolean removedInner = true;
  				while (removedInner) {
  					removedInner = false;
  					centreLoop:
  						for (SCs inner: outer.values()) {
  							if (inner.isEmpty()) {
  								outer.values().remove(inner);
  								Trace.log("commitments9", "SocialCommitmentStore.purge(): Removed group (from inner) ");
  								removedInner = true;
  								break centreLoop;
  							}
  							boolean removed = true;
  							while (removed) {
  								removed = false;
  								innerLoop:
  									for (SocialCommitment sc: inner) {
  										long endTime = sc.getEndTime(); 
  										if (endTime!=0 && endTime<expiredTime) {
  											boolean r1 = inner.remove(sc); 
  											if (!r1) {
  												Trace.log("error", "SocialCommitmentStore.purge(): Can't remove (from inner) "+sc);
  												cantRemove.add(sc.getSerialNumber());
  											}
  											else { 
  												Trace.log("commitments8", "SocialCommitmentStore.purge(): Removed (from inner) SN="+sc.getSerialNumber());
  												removedSCs.add(sc.getSerialNumber());
    											removed = true;
  											}
  											boolean r2 = commitmentsVector.remove(sc);
  											if (!r2) {
  												Trace.log("error", "SocialCommitmentStore.purge(): Can't remove (from vector) "+sc);
  											}
  											if (removed) {
  												count++;
  												setChanged();
  												notifyObservers(new Operation(Operator.REMOVE, sc));
  												break innerLoop;
  											}
  										} //if
  										else {
  											Trace.log("commitments9", "SocialCommitmentsStore.purge(): Keeping "+sc);
  										}
  									} //for 
  							} //while
  						} // for
  				} //while
  			} //for
  	} //while
		Trace.log("commitments", "SocialCommitmentStore.purge(): Summary: Removed "+count+" SCs "+removedSCs+", \n\tnow "+commitmentsVector.size()+" SCs left."+(cantRemove.size()==0?"":(" Failed to remove "+cantRemove+".")));
  	return count;
  }

	@Override
	public int compareTo(SocialCommitmentsStore o) {
		return Integer.signum(o.hashCode()-this.hashCode());
	}



}