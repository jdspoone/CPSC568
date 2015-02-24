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
 * WaitingObserverAdapter.java
 * Created on 25 october 2007
 * Author : Carole Adam
 */
package jade.semantics.kbase.observers;


/* ** 
 * modification 26 October 2007: timeout method
 */

import jade.core.Agent;
import jade.semantics.behaviours.PrimitiveActionBehaviour;
import jade.semantics.behaviours.SemanticBehaviour;
import jade.semantics.interpreter.Finder;
import jade.semantics.kbase.KBase;
import jade.semantics.kbase.QueryResult;
import jade.semantics.lang.sl.grammar.Formula;

import java.util.Date;

/**
 * Observer that waits for a given formula before resuming a given behaviour.
 * used in class PrimitiveActionBehaviour, method action 
 * @author Carole Adam
 * @version 0.9
 */
public class WaitingObserver extends ObserverAdapter {

	/**
     * The formula that represents the current behaviour
     * that was paused in PrimitiveActionBehaviour.action
     */
	PrimitiveActionBehaviour behaviour;
	
	/**
	 * The agent who is running this behaviour
	 */
	Agent myAgent;
	
	/*********************************************************************/
    /**                         CONSTRUCTOR                             **/
    /*********************************************************************/
	
	/**
     * Creates a new WaitingObserverAdapter
     * @param kbase The belief base owning this observer 
     * @param observedFormula the formula to observe
     * @param pausedBehaviour the pausedBehaviour to resume when the formula is observed
     * @param agent The agent owning the KBase on which this observer applies
     */
	public WaitingObserver(KBase kbase,Formula observedFormula,
									PrimitiveActionBehaviour pausedBehaviour,
									Agent agent) {
		super(kbase,observedFormula);
		behaviour = pausedBehaviour;
		myAgent = agent;
	}
	
	/**
     * Creates a new WaitingObserverAdapter
     * @param kbase The belief base owning this observer 
     * @param observedFormula the formula to observe
     * @param pausedBehaviour the pausedBehaviour to resume when the formula is observed
     * @param agent The agent owning the KBase on which this observer applies
     * @param delay The timeout for waiting for the observed formula
     */
	public WaitingObserver(KBase kbase,Formula observedFormula,
			PrimitiveActionBehaviour pausedBehaviour,
			Agent agent,long delay) {
		
		super(kbase,observedFormula,delay);
		behaviour = pausedBehaviour;
		myAgent = agent;
	}
	
	/**
     * Creates a new WaitingObserverAdapter
     * @param kbase The belief base owning this observer 
     * @param observedFormula the formula to observe
     * @param pausedBehaviour the pausedBehaviour to resume when the formula is observed
     * @param agent The agent owning the KBase on which this observer applies
     * @param wakeUp The Date for stopping waiting for the observed formula
     */
	public WaitingObserver(KBase kbase,Formula observedFormula,
			PrimitiveActionBehaviour pausedBehaviour,
			Agent agent,Date wakeUp) {
		super(kbase,observedFormula,wakeUp);
		behaviour = pausedBehaviour;
		myAgent = agent;
	}
	
	/***************
	 * GET METHODS *
	 ***************/
	
	/**
     * Returns the paused behavior
     * @return Returns the pausedBehaviour
     */
	PrimitiveActionBehaviour getPausedBehaviour() {
		return behaviour;
	}
	
	/**
	 * Method invoked when the observer is triggered.
     * Resumes the paused behaviour in a successful state
	 * and removes the observer from the KBase.
	 * Also removes the awakening behavior that was added
	 * if a timeout or wakeUpDate was given.
     * @param result list of MatchResults which represents the last queried value of the observed formula.
     */
	@Override
	public void action(QueryResult result) {
		// success of paused behavior
		myAgent.addBehaviour(behaviour.root());
		behaviour.state = SemanticBehaviour.SUCCESS;
		// one shot: remove observer once invoked once
		myKBase.removeObserver(new Finder() {
			@Override
			public boolean identify(Object object) {
				return WaitingObserver.this == object;
            }
		});
		// remove wakerBehaviour after success of main behavior 
		timeoutDisabled = true;
	}

	/** Method triggered at the end of the timeout
	 *  or when the wakeUpDate is reached.
	 *  Resumes the pausedBehaviour in an execution failure state.
	 *  This triggers the interpretation of the corresponding 
	 *  annotation of the agent's current plan.
	 */
	@Override
	public void timeout() {
		if (!timeoutDisabled) {
			myAgent.addBehaviour(behaviour.root());
			behaviour.state = SemanticBehaviour.EXECUTION_FAILURE;
		}
	}
	
}//end class
