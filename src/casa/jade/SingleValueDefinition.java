/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
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
 * SingleNumValueDefinition.java
 * Created on 15 d�c. 2004
 * Author : louisvi
 */
package casa.jade;

import jade.semantics.kbase.filters.FilterKBase;
import jade.semantics.kbase.filters.FiltersDefinition;
import jade.semantics.kbase.filters.KBAssertFilterAdapter;
import jade.semantics.kbase.filters.KBQueryFilterAdapter;
import jade.semantics.lang.sl.grammar.Constant;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.IdentifyingExpression;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.grammar.TrueNode;
import jade.semantics.lang.sl.tools.MatchResult;
import jade.semantics.lang.sl.tools.SL;
import jade.util.leap.Set;

/**
 * General class that defines single value predicate. 
 * @author Vincent Louis - France Telecom
 * @version 0.9
 */
public class SingleValueDefinition extends FiltersDefinition {

	/**
	 * Patterns used to manage the defined predicate
	 */
	private Formula VALUE_X_PATTERN;
	private Formula NOT_VALUE_X_PATTERN;
	private IdentifyingExpression IOTA_VALUE;

	/**
	 * Removes from the base all the belief about this kind of predicate
	 * @param kbase the base to clean
	 */
	protected void cleanKBase(FilterKBase kbase) {
		kbase.retractFormula(NOT_VALUE_X_PATTERN);
		kbase.retractFormula(VALUE_X_PATTERN);
	} // End of cleanKBase/1

	/**
	 * Constructor
	 * @param name name of the predicate
	 */
	public SingleValueDefinition(String name) 
	{        
		VALUE_X_PATTERN = SL.formula("("+name+" ??X)");

		NOT_VALUE_X_PATTERN = SL.formula("(not ("+name+" ??X))");

		IOTA_VALUE = (IdentifyingExpression)SL.term("(iota ?y (B ??agent ("+name+" ?y)))");

		// ASSERT FILTERS
		// --------------
		defineFilter(new KBAssertFilterAdapter("(B ??agent " + VALUE_X_PATTERN + ")") {
			//If the predicate is already in the base, does nothing, otherwise
			//cleans the base of all knowledge related to this predicate
			@Override
			public Formula doApply(Formula formula, MatchResult match) {
				if ((myKBase.query(formula) != null)) {
					return new TrueNode();
				}
				cleanKBase(myKBase);
				return formula;
			}
		});

		// QUERY FILTERS
		// -------------


	} // End of SingleValueDefinition

} // End of SingleValueDefinition