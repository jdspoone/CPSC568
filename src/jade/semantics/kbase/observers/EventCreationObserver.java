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
 * EventCreationObserver.java
 * Created on 9 mars 2005
 * Author : Vincent Pautret
 */
package jade.semantics.kbase.observers;

import jade.semantics.interpreter.Finder;
import jade.semantics.interpreter.SemanticInterpreterBehaviour;
import jade.semantics.interpreter.SemanticRepresentation;
import jade.semantics.interpreter.Tools;
import jade.semantics.kbase.KBase;
import jade.semantics.kbase.QueryResult;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.tools.SL;

/**
 * Observer that triggers a subscribe event. 
 * @author Vincent Pautret - France Telecom
 * @version 0.9
 * @version 0.9
 * @version 0.9
 */
public class EventCreationObserver extends ObserverAdapter {

	private final boolean DEBUG = false;
	
	/**
	 * The formula that represents the subscribed internal event
	 */
	private SemanticRepresentation subscribedEvent;

	/**
	 * The interpreter to submit the subscribed event when triggered.
	 */
	private SemanticInterpreterBehaviour interpreter;

	/**
	 * Indicates if the observer should be done only one time or not
	 */
	private boolean isOneShot;

	/*********************************************************************/
	/**                         CONSTRUCTOR                             **/
	/*********************************************************************/
	/**
	 * Creates a new Observer
	 * @param agent The agent that has this observer on its belief base 
	 * @param observedFormula the formula to observe
	 * @param subscribedEvent the event to trigger (SemanticRepresentation)
	 * @param interpreter the interpreter to submit the subscribed event
	 * @param isOneShot should the observer be done only one time or not
	 */
	public EventCreationObserver(KBase kbase, 
			Formula observedFormula, 
			SemanticRepresentation subscribedEvent,
			SemanticInterpreterBehaviour interpreter,
			boolean isOneShot) {
		super(kbase, observedFormula);
		this.interpreter = interpreter;
		this.subscribedEvent = subscribedEvent;
		this.isOneShot = isOneShot;
	} // End of EventCreationObserver

	/**
	 * Creates a new Observer
	 * @param agent The agent that has this observer on its belief base 
	 * @param observedFormula the formula to observe
	 * @param subscribedEvent the event to trigger (Formula)
	 * @param interpreter the interpreter to submit the subscribed event
	 * @param isOneShot should the observer be done only one time or not
	 */
	public EventCreationObserver(KBase kbase, 
			Formula observedFormula, 
			Formula subscribedEvent, 
			SemanticInterpreterBehaviour interpreter,
			boolean isOneShot) {
		this(kbase, 
				observedFormula, 
				new SemanticRepresentation(subscribedEvent),
				interpreter, 
				isOneShot);
	}

	/**
	 * Creates a new Observer
	 * @param agent The agent that has this observer on its belief base 
	 * @param observedFormula the formula to observe
	 * @param subscribedEvent the event to trigger
	 * @param interpreter the interpreter to submit the subscribed event
	 */
	public EventCreationObserver(KBase kbase, 
			Formula observedFormula, 
			Formula subscribedEvent,
			SemanticInterpreterBehaviour interpreter) {
		this(kbase, observedFormula, subscribedEvent, interpreter, false);
	}

	/** The same constructors with a timeout (Long) **/

	/**
	 * Creates a new Observer with a timeout
	 * @param agent The agent that has this observer on its belief base 
	 * @param observedFormula the formula to observe
	 * @param subscribedEvent the event to trigger (SemanticRepresentation)
	 * @param interpreter the interpreter to submit the subscribed event
	 * @param isOneShot should the observer be done only one time or not
	 * @param timeout the delay (Long) before triggering the timeout method
	 */
	public EventCreationObserver(KBase kbase, 
			Formula observedFormula, 
			SemanticRepresentation subscribedEvent,
			SemanticInterpreterBehaviour interpreter,
			boolean isOneShot,
			Long timeout) {
		super(kbase, observedFormula, timeout);
		this.interpreter = interpreter;
		this.subscribedEvent = subscribedEvent;
		this.isOneShot = isOneShot;
	} // End of EventCreationObserver

	/**
	 * Creates a new Observer
	 * @param agent The agent that has this observer on its belief base 
	 * @param observedFormula the formula to observe
	 * @param subscribedEvent the event to trigger (Formula)
	 * @param interpreter the interpreter to submit the subscribed event
	 * @param isOneShot should the observer be done only one time or not
	 * @param timeout the delay (Long) before triggering the timeout method
	 */
	public EventCreationObserver(KBase kbase, 
			Formula observedFormula, 
			Formula subscribedEvent, 
			SemanticInterpreterBehaviour interpreter,
			boolean isOneShot,
			Long timeout) {
		this(kbase, 
				observedFormula, 
				new SemanticRepresentation(subscribedEvent),
				interpreter, 
				isOneShot,
				timeout);
	}

	/**
	 * Creates a new Observer
	 * @param agent The agent that has this observer on its belief base 
	 * @param observedFormula the formula to observe
	 * @param subscribedEvent the event to trigger
	 * @param interpreter the interpreter to submit the subscribed event
	 * @param timeout the delay (Long) before triggering the timeout method
	 */
	public EventCreationObserver(KBase kbase, 
			Formula observedFormula, 
			Formula subscribedEvent,
			SemanticInterpreterBehaviour interpreter,
			Long timeout) {
		this(kbase, observedFormula, subscribedEvent, 
				interpreter, false, timeout);
	}

	/** Same constructors with a wakeUpDate (Date) **/

	/**
	 * Creates a new Observer with a timeout
	 * @param agent The agent that has this observer on its belief base 
	 * @param observedFormula the formula to observe
	 * @param subscribedEvent the event to trigger (SemanticRepresentation)
	 * @param interpreter the interpreter to submit the subscribed event
	 * @param isOneShot should the observer be done only one time or not
	 * @param wakeUpDate the date (java.util.Date) when to trigger the timeout method
	 */
	public EventCreationObserver(KBase kbase, 
			Formula observedFormula, 
			SemanticRepresentation subscribedEvent,
			SemanticInterpreterBehaviour interpreter,
			boolean isOneShot,
			java.util.Date wakeUpDate) {
		super(kbase, observedFormula, wakeUpDate);
		this.interpreter = interpreter;
		this.subscribedEvent = subscribedEvent;
		this.isOneShot = isOneShot;
	} // End of EventCreationObserver

	/**
	 * Creates a new Observer
	 * @param agent The agent that has this observer on its belief base 
	 * @param observedFormula the formula to observe
	 * @param subscribedEvent the event to trigger (Formula)
	 * @param interpreter the interpreter to submit the subscribed event
	 * @param isOneShot should the observer be done only one time or not
	 * @param wakeUpDate the date (java.util.Date) when to trigger the timeout method
	 */
	public EventCreationObserver(KBase kbase, 
			Formula observedFormula, 
			Formula subscribedEvent, 
			SemanticInterpreterBehaviour interpreter,
			boolean isOneShot,
			java.util.Date wakeUpDate) {
		this(kbase, 
				observedFormula, 
				new SemanticRepresentation(subscribedEvent),
				interpreter, 
				isOneShot,
				wakeUpDate);
	}

	/**
	 * Creates a new Observer
	 * @param agent The agent that has this observer on its belief base 
	 * @param observedFormula the formula to observe
	 * @param subscribedEvent the event to trigger
	 * @param interpreter the interpreter to submit the subscribed event
	 * @param wakeUpDate the date (java.util.Date) when to trigger the timeout method
	 */
	public EventCreationObserver(KBase kbase, 
			Formula observedFormula, 
			Formula subscribedEvent,
			SemanticInterpreterBehaviour interpreter,
			java.util.Date wakeUpDate) {
		this(kbase, observedFormula, subscribedEvent, 
				interpreter, false, wakeUpDate);
	}

	/***************
	 * GET METHODS *
	 ***************/

	/**
	 * Returns the subscribedEvent.
	 * @return Returns the subscribedEvent.
	 */
	public Formula getSubscribedEvent() {
		return subscribedEvent.getSLRepresentation();
	} 


	/************************
	 * METHODS
	 ************************/

	/**
	 * Interprets the subscribed event and removes the suitable 
	 * observer if it should be done only one time (isOneShot = true).
	 * @param value list of MatchResults which represents the last 
	 * queried value of the observed formula.
	 */
	@Override
	public void action(QueryResult value) {
		// impedes the timeout method
		disableTimeout();
		// action !
		try {
			Tools.printTraceMessage("action of "+this+" on "+value,DEBUG);
			if (value != null) {
				if (value.size() >= 1) {
					// scan the different possible instantiations of subscribedEvent
					for (int j=0;j<value.size();j++) {
//						for (int i =0; i < value.getResult(j).size(); i++) {
//							subscribedEvent.setSLRepresentation(
//									subscribedEvent.getSLRepresentation().instantiate(
//											((MetaTermReferenceNode)value.getResult(j).get(i)).lx_name(),
//											((MetaTermReferenceNode)value.getResult(j).get(i)).sm_value()));
//						}
						Formula instantiatedEvent = subscribedEvent.getSLRepresentation();
						instantiatedEvent = (Formula)SL.instantiate(instantiatedEvent,value.getResult(j));
						// interpret each instantiation of subscribedEvent	
						Tools.printTraceMessage("observer interprets "+instantiatedEvent,DEBUG);
						if (myKBase.query(instantiatedEvent) == null) {
							Tools.printTraceMessage("--> ok",DEBUG);
							interpreter.interpret(new SemanticRepresentation(instantiatedEvent), false);							
						}
						else {
							Tools.printTraceMessage("--> not ok",DEBUG);
						}
					} 
				}
				else {
					// if value = Query.KNOWN, interpret subscribedEvent (already fully instantiated)
					Tools.printTraceMessage("observer interprets "+subscribedEvent,DEBUG);
					interpreter.interpret(new SemanticRepresentation(subscribedEvent), false);
				}


				if (isOneShot) {
					Tools.printTraceMessage("!!! remove observer on "+getObservedFormula(),DEBUG);
					myKBase.removeObserver(new Finder() {
						@Override
						public boolean identify(Object object) {
							return EventCreationObserver.this == object;
						}
					});
					disableTimeout();
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 

	@Override
	/**
	 * This method is invoked at the end of the timeout.
	 * It removes the observer from the KBase.
	 */
	public void timeout() {
		myKBase.removeObserver(new Finder() {
			@Override
			public boolean identify(Object object) {
				return EventCreationObserver.this == object;
			}
		});
	}

} 
