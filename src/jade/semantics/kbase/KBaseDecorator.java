/******************************************************************
Copyright 2003-2014, 
version 2.1 of the License. 
This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.
*****************************************************************/

/*
* created on 21 mars 07 by Thierry Martinez
*/

package jade.semantics.kbase;

/**
 * Modified 29 January 2008 by Carole Adam
 * to implement modifications of toStrings 
 * from the main branch of JSA (author Vincent Louis)
 */

import jade.semantics.interpreter.Finder;
import jade.semantics.kbase.observers.Observer;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.IdentifyingExpression;
import jade.semantics.lang.sl.grammar.ListOfTerm;
import jade.semantics.lang.sl.grammar.Term;
import jade.util.leap.ArrayList;

public class KBaseDecorator implements KBase {
	
	/**
	 * The decorated KBase
	 */
	protected KBase decorated = null;
	
	/**
	 * Build a KBase decorator
	 * @param decorated the decorated KBase
	 */
	protected KBaseDecorator(KBase decorated) {
		this.decorated = decorated;
		setWrappingKBase(this);
	}
	
	/**
	 * Return the decorated kbase
	 * @return the decorated kbase
	 */
	public KBase getDecorated() {
		return decorated;
	}

	/* (non-Javadoc)
	 * @see jade.semantics.kbase.KBase#addClosedPredicate(jade.semantics.lang.sl.grammar.Formula)
	 */
	public void addClosedPredicate(Formula pattern) {
		decorated.addClosedPredicate(pattern);
	}

	/* (non-Javadoc)
	 * @see jade.semantics.kbase.KBase#addObserver(jade.semantics.kbase.observers.Observer)
	 */
	public void addObserver(Observer o) {
		decorated.addObserver(o);
	}

	/* (non-Javadoc)
	 * @see jade.semantics.kbase.KBase#assertFormula(jade.semantics.lang.sl.grammar.Formula)
	 */
	public void assertFormula(Formula formula) {
		decorated.assertFormula(formula);
	}

	/* (non-Javadoc)
	 * @see jade.semantics.kbase.KBase#getAgentName()
	 */
	public Term getAgentName() {
		return decorated.getAgentName();
	}

	/* (non-Javadoc)
	 * @see jade.semantics.kbase.KBase#getWrappingKBase()
	 */
	public KBase getWrappingKBase() {
		return decorated.getWrappingKBase();
	}

	/* (non-Javadoc)
	 * @see jade.semantics.kbase.KBase#isClosed(jade.semantics.lang.sl.grammar.Formula, jade.semantics.kbase.QueryResult)
	 */
	public boolean isClosed(Formula pattern, QueryResult values) {
		return decorated.isClosed(pattern, values);
	}

	/* (non-Javadoc)
	 * @see jade.semantics.kbase.KBase#query(jade.semantics.lang.sl.grammar.Formula)
	 */
	public QueryResult query(Formula formula) {
		return decorated.query(formula);
	}

	/* (non-Javadoc)
	 * @see jade.semantics.kbase.KBase#query(jade.semantics.lang.sl.grammar.Formula, jade.util.leap.ArrayList)
	 */
	public QueryResult query(Formula formula, ArrayList reasons) {
		return decorated.query(formula, reasons);
	}
	
	/* (non-Javadoc)
	 * @see jade.semantics.kbase.KBase#queryRef(jade.semantics.lang.sl.grammar.IdentifyingExpression)
	 */
	public ListOfTerm queryRef(IdentifyingExpression expression) {
		return decorated.queryRef(expression);
	}
	
	/* (non-Javadoc)
	 * @see jade.semantics.kbase.KBase#queryRef(jade.semantics.lang.sl.grammar.IdentifyingExpression, jade.util.leap.ArrayList)
	 */
	public Term queryRef(IdentifyingExpression expression, ArrayList falsityReasons) {
		return decorated.queryRef(expression, falsityReasons);
	}
	
	/* (non-Javadoc)
	 * @see jade.semantics.kbase.KBase#queryRefTerm(jade.semantics.lang.sl.grammar.IdentifyingExpression)
	 */
	public Term queryRefSingleTerm(IdentifyingExpression expression) {
		return decorated.queryRefSingleTerm(expression);
	}
	
	/* (non-Javadoc)
	 * @see jade.semantics.kbase.KBase#eval(jade.semantics.lang.sl.grammar.Term)
	 */
	public Term eval(Term expression) {
		return decorated.eval(expression);
	}
	
	/* (non-Javadoc)
	 * @see jade.semantics.kbase.KBase#removeClosedPredicate(jade.semantics.interpreter.Finder)
	 */
	public void removeClosedPredicate(Finder finder) {
		decorated.removeClosedPredicate(finder);
	}

	/* (non-Javadoc)
	 * @see jade.semantics.kbase.KBase#removeFormula(jade.semantics.interpreter.Finder)
	 */
	public void removeFormula(Finder finder) {
		decorated.removeFormula(finder);
	}

	/* (non-Javadoc)
	 * @see jade.semantics.kbase.KBase#removeObserver(jade.semantics.interpreter.Finder)
	 */
	public void removeObserver(Finder finder) {
		decorated.removeObserver(finder);
	}

	/* (non-Javadoc)
	 * @see jade.semantics.kbase.KBase#removeObserver(jade.semantics.kbase.observers.Observer)
	 */
	public void removeObserver(Observer obs) {
		decorated.removeObserver(obs);
	}

	/* (non-Javadoc)
	 * @see jade.semantics.kbase.KBase#retractFormula(jade.semantics.lang.sl.grammar.Formula)
	 */
	public void retractFormula(Formula formula) {
		decorated.retractFormula(formula);
	}

	/* (non-Javadoc)
	 * @see jade.semantics.kbase.KBase#setAgentName(jade.semantics.lang.sl.grammar.Term)
	 */
	public void setAgentName(Term agent) {
		decorated.setAgentName(agent);
	}

	/* (non-Javadoc)
	 * @see jade.semantics.kbase.KBase#setWrappingKBase(jade.semantics.kbase.KBase)
	 */
	public void setWrappingKBase(KBase kbase) {
		decorated.setWrappingKBase(kbase);
	}

	/* (non-Javadoc)
	 * @see jade.semantics.kbase.KBase#toStrings()
	 */
	public ArrayList toStrings() {
		return decorated.toStrings();
	}

	/* (non-Javadoc)
	 * @see jade.semantics.kbase.KBase#updateObservers(jade.semantics.lang.sl.grammar.Formula)
	 */
	public void updateObservers(Formula formula) {
		decorated.updateObservers(formula);
	}

}

