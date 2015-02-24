/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
JSA - JADE Semantics Add-on is a framework to develop cognitive
agents in compliance with the FIPA-ACL formal specifications.

Copyright 2003-2014, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/
/*
 * Observer.java
 * Created on 21 d�cembre 2006
 * Author : Thierry Martinez
 */
package jade.semantics.kbase.observers;


import jade.semantics.interpreter.Tools;
import jade.semantics.kbase.KBase;
import jade.semantics.kbase.QueryResult;
import jade.semantics.lang.sl.grammar.BelieveNode;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.NotNode;
import jade.semantics.lang.sl.grammar.Term;
import jade.util.leap.ArrayList;

import java.util.Date;

/**
 * Defines a KBase observer. 
 * Such an observer provides a means to observe several
 * formulas, possibly asserted in the KBase, and to
 * react if their values change.
 * Do not forget to initialise new observers if they
 * are added to the KBase after its initial setup
 * @author Thierry Martinez - France Telecom
 * @version 0.9
 * @version 0.9
 */
public abstract class Observer {
	
	private static final boolean DEBUG = false;
	
	static final int DIRECTLY_OBSERVED = 0;
	
	/**
	 * Stores the formulas to be observed. 
	 * Among these formulas, the first one is the one directly 
	 * observed, while the other ones are the associated pattern 
	 * that could also trigger this observer. 
	 */
	ArrayList observedFormulas = new ArrayList();
	
	/**
	 * Store the last queried value of the DIRECTLY_OBSERVED formula.
	 */
	QueryResult lastQueryResultOnDirectlyObserved = null;
	
	/**
	 * The KBase the observer belongs to.
	 */
	KBase myKBase = null;
	
	/**
	 * true if the observer value has been queried at least one time.
	 */
	boolean updatedOnce = false;
	
	/** 
	 * Timeout value to avoid to wait endlessly.
	 * At the end of the timeout, triggers an exit behaviour.
	 */
	long timeout=-1;
	
	/** 
	 * Instead of a timeout value, one can use a wake up date.
	 * When this date is reached, the same exit behaviour is triggered.
	 */
	Date wakeUpDate=null;

	/** 
	 * The waker behaviour triggered at the end of the timeout
	 * or when the wake up date is reached.
	 * It is only necessary to know it inside the class in order to 
	 * remove it from the behaviour queue when it becomes unnecessary.
	 */
	Thread awakeningThread;
	
	/**
	 * A boolean to check if the timeout method should be invoked
	 * (to avoid invoking it after the success of the behaviour).
	 * 
	 * Warning : when the observer is suppressed, this boolean must
	 * also be set to true to avoid triggering the timeout method.
	 */
	boolean timeoutDisabled = false;
	
	/*********************************************
	 * CONSTRUCTOR
	 *********************************************/
	
	/**
	 * Build an Observer for the KBase given as an argument. 
	 * 
	 * Warning: if no timeout is given, no awakeningBehaviour is programmed
	 * so this observer could observe endlessly...
	 * 
	 * @param kbase the KBase the observer belongs to. It should not be null.
	 */
	public Observer(KBase kbase, Formula formula) {
		myKBase = kbase;
		addFormula(formula);
		// timeout and wakeUpDate are not modified
		// they keep their default value (-1 and null)
		// so that no awakeningBehaviour is programmed.
	}
	
	/** Builds an Observer for the kbase given as an argument.
	 * This observer expires at the end of the given timeout.
	 * @param kbase the kbase the observer belongs to. It should not be null.
	 * @param timeOut the value of the timeout. It should be positive.
	 */
	public Observer(KBase kbase, Formula formula, long timeOut) {
		this(kbase,formula);
		this.timeout = timeOut;
	}
	
	/** Builds an Observer for the kbase given as an argument.
	 * This observer expires at the end of the given timeout
	 * @param kbase the kbase the observer belongs to. It should not be null.
	 * @param wakeupDate the date of wake up. It should not be null.
	 */
	public Observer(KBase kbase, Formula formula, Date wakeupDate) {
		this(kbase,formula);
		this.wakeUpDate = wakeupDate;
	}
	

	/***************
	 * GET METHODS *
	 ***************/
	
	/**
	 * This method returns all observed formulas
	 * @return an array of all observed formulas
	 */
	public Formula[] getObservedFormulas()
	{
		Formula[] result = new Formula[observedFormulas.size()];
		for (int i=0; i<result.length; i++) {
			result[i] = (Formula)observedFormulas.get(i);
		}
		return result;
	}
	
	/**
	 * @return the value of the timeout (java.lang.Long) for this observer.
	 */
	public Long getTimeOut() {
		return timeout;
	}
	
	/**
	 * @return the wakeUpDate (java.util.Date) for this observer.
	 */
	public Date getWakeUpDate() {
		return wakeUpDate;
	}
	
	/**
	 * @return the KBase to which this observer belongs to
	 */
	public KBase getMyKBase() {
		return myKBase;
	}
	
	/****************
	 *  SET METHODS *
	 ****************/
	
	/**
     * Add a formula to observe. Notice that observe f means observe f and (not f).
     * @param the formula to observe.
     */
    public void addFormula(Formula formula) {
    	Term myself = myKBase.getAgentName();
    	formula = formula.instantiate("myself", myself);
    	Formula f = new BelieveNode(myself, formula).getSimplifiedFormula();
		if ( ! observedFormulas.contains(f) ) {
			observedFormulas.add(f);
		}
		Formula notF = new BelieveNode(myself, new NotNode(formula)).getSimplifiedFormula();
		if ( ! observedFormulas.contains(notF) ) {
			observedFormulas.add(notF);
		}
    }
    
    
    /****************************
     * TIMEOUT MANAGING METHODS *
     * **************************/
    
    /**
     * Local method called during the initialisation of the observer (method update).
     * It sets up an awakening thread that runs the timeout method at the end
     * of the delay (timeout or time to wakeUpDate)
     * FIXME : use JADE behaviour instead of JAVA thread
     */
	private void setupAwakeningThread() {		
		awakeningThread = new Thread() {
			@Override
			public void run() {
				try {
					// FIXME : should check if timout is still enabled before calling the timeout method
					// (otherwise it is necessary to perform this check in each children timeout method)
					if (timeout>0) {
						sleep(timeout);
						timeout();
					}
					else if (wakeUpDate != null) {
						Long delay = wakeUpDate.getTime()-System.currentTimeMillis();
						sleep(delay);
						timeout();
					}
				}
				catch(InterruptedException e) {
					e.printStackTrace();
				}
			}// end run
		}; //end new thread
		awakeningThread.start();
	}// end setup
	
	/** 
	 * Timeout method called by the awakening behaviour that is
	 * triggered at the end of the timeout or at the wakeUpDate.
	 * By default it does nothing, should be overridden in subclasses 
	 * when necessary. 
	 */
	public void timeout() {
		// To be overridden in subclasses needing a timeout behaviour
	}
	
	
	/**
	 * Method to disable the timeout method.
	 * Useful to prevent the timeout method to be executed when the observer
	 * has been removed or after the watched formula has been observed (this
	 * could happen since the timeout method is invoked by a thread, independently
	 * from the Observer itself).
	 * 
	 * FIXME this method should be automatically invoked each time an observer is 
	 * removed from the KBase: modify the KBase class and subclasses ??
	 * For now: be careful and do not forget to disable timeout when removing an observer.
	 */
	public void disableTimeout() {
		timeoutDisabled = true;
	}
	
	/**
	 * Method used in the timeout method of subclasses to know if it should
	 * be executed (if the timeout is enabled) or not.
	 * @return a boolean indicating if the timeout is still enabled
	 */
	public boolean isEnabledTimeout() {
		return !timeoutDisabled;
	}

	
	/*************
	 *  METHODS  *
	 *************/
	
	/**
	 * This method is called to update the observer according to
	 * the change that has just occurred on the given formula.
	 * Do not forget to call update(null) to initialise the observer once 
	 * added in the KBase (unless it is added in the setupKBase method: in 
	 * this case the initialisation is automatic).
	 * 
	 * @param formula the formula which has just changed (newly asserted or removed) 
	 *        or null to force the update of the observer.
	 * @return true if one of the observed formulas is concerned by this recent change.
	 */
	public boolean update(Formula formula) { 
		boolean hasChanged = false;
		Tools.printTraceMessage("Observer.update("+formula+")", DEBUG);
		Tools.printTraceMessage("observer="+this+" and belongs to class "+this.getClass(), DEBUG);
		if ( updatedOnce ) {
			Tools.printTraceMessage("CASE 1 : updated once", DEBUG);
			boolean isConcerned = (formula == null); // If formula is null, the observer is necessarily concerned by the update operation
			if (!isConcerned) {
				Tools.printTraceMessage(" - SUBCASE 1.1 : not concerned", DEBUG);
				formula = new BelieveNode(myKBase.getAgentName(), formula).getSimplifiedFormula();
		    	for (int i=0; !isConcerned && i<observedFormulas.size(); i++) {
					Formula f = (Formula)observedFormulas.get(i);
					Tools.printTraceMessage(" - observed formula("+i+")="+f, DEBUG);
					isConcerned = ( f.match(formula) != null );
					Tools.printTraceMessage(" - -> match="+isConcerned, DEBUG);
				}
			}
			if ( isConcerned ) {
				Tools.printTraceMessage(" - SUBCASE 1.2 : concerned", DEBUG);
				QueryResult val = myKBase.query((Formula)observedFormulas.get(DIRECTLY_OBSERVED));
				Tools.printTraceMessage(" - query directly observed formula ("+observedFormulas.get(DIRECTLY_OBSERVED)+") returns val = "+val, DEBUG);
				Tools.printTraceMessage(" - while lastQRonDirectlyObserved = "+lastQueryResultOnDirectlyObserved, DEBUG);
				hasChanged = lastQueryResultOnDirectlyObserved==null?(val!=null):!lastQueryResultOnDirectlyObserved.equals(val);
				lastQueryResultOnDirectlyObserved = val;
				Tools.printTraceMessage(" - -> hasChanged = "+hasChanged, DEBUG);
			}		
		}
		else {
			Tools.printTraceMessage("CASE 2 : first update", DEBUG);
			// never updated: initialisation of the observer
			// sets up the waker thread triggered at the end of timeout
			if ((timeout > 0) || (wakeUpDate != null)) {
				setupAwakeningThread();
			}
			if ( myKBase != null ) {
				lastQueryResultOnDirectlyObserved = myKBase.query((Formula)observedFormulas.get(DIRECTLY_OBSERVED));
				Tools.printTraceMessage(" - directly observed formula = "+observedFormulas.get(DIRECTLY_OBSERVED), DEBUG);
				Tools.printTraceMessage(" -> compute lastQRonDirectlyObserved = "+lastQueryResultOnDirectlyObserved, DEBUG);
				updatedOnce = true;
				return false;
			}
		}
		return hasChanged;
	}
	
	@Override
	public String toString() {
		String result = "Observer("+observedFormulas.get(DIRECTLY_OBSERVED)+") {\n";
		for (int i=0; i<observedFormulas.size(); i++) {
			result += "\t"+observedFormulas.get(i)+",\n";
		}
		result+= "}";
		return result;
	}
	
	
//	/****************************************************************
//	 **** INNER CLASS                                            ****
//	 **** the waker behavior triggered at the end of the timeout ****
//	 ****************************************************************/
//
//	private class ObserverWakerBehaviour extends WakerBehaviour {
//		
//		
//		// standard WakerBehaviour constructor
//		ObserverWakerBehaviour(Agent arg0, Date arg1) {
//			super(arg0, arg1);
//		}
//
//		// standard WakerBehaviour constructor
//		ObserverWakerBehaviour(Agent arg0, long arg1) {
//			super(arg0, arg1);
//		}
//
//		// overriding onWake to remove the Observer from the agent's table 
//		// and notify the Observer it is timed out
//		protected void onWake() {
//			// remove the observer from the agent's table
//			myKBase.removeObserver(new Finder() {
//                public boolean identify(Object object) {
//                    return Observer.this == object;
//                }
//            });
//			Observer.this.timeout();
//		}//end onWake
//	}// end inner class
	
}

