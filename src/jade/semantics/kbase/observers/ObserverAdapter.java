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
 * ObserverAdapter.java
 * Created on 11 mars 2005
 * Author : Vincent Pautret
 */
package jade.semantics.kbase.observers;



import jade.semantics.interpreter.Tools;
import jade.semantics.kbase.KBase;
import jade.semantics.kbase.QueryResult;
import jade.semantics.lang.sl.grammar.Formula;

import java.util.Date;


/**
 * Base observer that implements the <code>Observer</code> interface.
 * @author Thierry Martinez - France Telecom
 * @version 0.9
 * @version 0.9
 */

/******************
 *  CONSTRUCTORS  *
 ******************/

public abstract class ObserverAdapter extends Observer {

	private static final boolean DEBUG = false;
	
    /**
     * Creates a new Observer
     * @param formula the formula to observe
     */
    public ObserverAdapter(KBase kbase, Formula formula) {
		super(kbase, formula);
    }
    
    /**
     * Creates a new Observer with timeout
     * @param formula the formula to observe
     * @param timeout the (Long) value of the delay before stopping to observe the formula
     */
    public ObserverAdapter(KBase kbase, Formula formula, long timeout) {
		super(kbase, formula,timeout);
    }
    
    /**
     * Creates a new Observer
     * @param formula the formula to observe
     * @param wakeupdate the date when to stop observing the formula
     */
    public ObserverAdapter(KBase kbase, Formula formula, Date wakeupdate) {
		super(kbase, formula,wakeupdate);
    }

    /***************
     * GET METHODS *
     ***************/
    
	/**
	 * @return the formula passed to the constructor.
	 */
	public Formula getObservedFormula() {
		return (Formula)observedFormulas.get(DIRECTLY_OBSERVED);
	}

	
	/*************
	 *  METHODS  *
	 *************/
	
    /**
     * Should be overridden. It is triggered when
     * the observer state changes.
     * @param value is the last value queried of the observed formula.
     */
    public abstract void action(QueryResult value);
    
	@Override
	// Call this method once with null parameter to force the 
	// initialisation of the observer after adding it to the KBase.
	// The initialisation is automatic only for observers added to 
	// KBase during its setup.
	public boolean update(Formula formula) {
		Tools.printTraceMessage("observer update on "+formula,DEBUG);
		boolean change = super.update(formula);
		Tools.printTraceMessage(" --> change = "+change,DEBUG);
		if ( change ) {
			action(lastQueryResultOnDirectlyObserved);
		}
		return change;
	}
}
