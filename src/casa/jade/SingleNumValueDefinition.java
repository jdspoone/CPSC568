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
 * General class that defines single value predicat. 
 * @author Vincent Louis - France Telecom
 * @version 0.9
 */
public class SingleNumValueDefinition extends FiltersDefinition {

	/**
	 * Patterns used to manage the defined predicat
	 */
	Formula VALUE_X_PATTERN;
	Formula NOT_VALUE_X_PATTERN;
	Formula VALUE_GT_X_PATTERN;
	Formula NOT_VALUE_GT_X_PATTERN;
	IdentifyingExpression IOTA_VALUE;
	IdentifyingExpression IOTA_VALUE_GT;
	IdentifyingExpression IOTA_VALUE_NOT_GT;

	/**
	 * Removes from the base all the belief about this kind of predicat
	 * @param kbase the base to clean
	 */
	protected void cleanKBase(FilterKBase kbase) {
		kbase.retractFormula(NOT_VALUE_X_PATTERN);
		kbase.retractFormula(VALUE_X_PATTERN);
		kbase.retractFormula(VALUE_GT_X_PATTERN);
		kbase.retractFormula(NOT_VALUE_GT_X_PATTERN);
	} // End of cleanKBase/1

	/**
	 * Constructor
	 * @param name name of the predicat 
	 */
	public SingleNumValueDefinition(String name) 
	{        
		VALUE_X_PATTERN = SL.formula("("+name+" ??X)");

		NOT_VALUE_X_PATTERN = SL.formula("(not ("+name+" ??X))");

		VALUE_GT_X_PATTERN = SL.formula("("+name+"_gt ??X)");

		NOT_VALUE_GT_X_PATTERN = SL.formula("(not ("+name+"_gt ??X))");

		IOTA_VALUE = (IdentifyingExpression)SL.term("(iota ?y (B ??agent ("+name+" ?y)))");

		IOTA_VALUE_GT = (IdentifyingExpression)SL.term("(iota ?y (B ??agent ("+name+"_gt ?y)))");

		IOTA_VALUE_NOT_GT = (IdentifyingExpression)SL.term("(iota ?y (B ??agent (not ("+name+"_gt ?y))))");

		// ASSERT FILTERS
		// --------------
		// These filters are used to let only one information about this 
		//predicat in the base.
		defineFilter(new KBAssertFilterAdapter("(B ??agent " + VALUE_X_PATTERN + ")") {
			//If the predicat is already in the base, does nothing, otherwise
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

		defineFilter(new KBAssertFilterAdapter("(B ??agent " + VALUE_GT_X_PATTERN + ")") {
			@Override
			public Formula doApply(Formula formula, MatchResult match) {
				//If the predicat is already in the base, does nothing, otherwise
				//cleans the base of all knowledge related to this predicate
				if ((myKBase.query(formula) != null)) {
					return new TrueNode();
				}
				cleanKBase(myKBase);
				return formula;
			}
		});

		defineFilter(new KBAssertFilterAdapter("(B ??agent " + NOT_VALUE_GT_X_PATTERN + ")") {
			@Override
			public Formula doApply(Formula formula, MatchResult match) {
				//If the predicat is already in the base, does nothing, otherwise
				//cleans the base of all knowledge related to this predicate
				if ((myKBase.query(formula) != null)) {
					return new TrueNode();
				}
				cleanKBase(myKBase);
				return formula;
			}
		});

		// QUERY FILTERS
		// -------------

		defineFilter(new KBQueryFilterAdapter("(B ??agent " + VALUE_GT_X_PATTERN + ")") {
			// Compare the sought value with that known to give the answer. 
			@Override
			public MatchResult doApply(Formula formula, MatchResult applyResult) {
				MatchResult queryResult = null;
				try {
					if (applyResult.getTerm("X") instanceof Constant) {
						Long queriedValue = ((Constant)applyResult.getTerm("X")).intValue();
						Term queryRefResult = myKBase.queryRefSingleTerm((IdentifyingExpression)
								SL.instantiate(IOTA_VALUE, "agent", applyResult.getTerm("agent")));
						if (queryRefResult != null) {
							if ( ((Constant)queryRefResult).intValue().longValue() > queriedValue.longValue() ) {
								queryResult = new MatchResult();
							}
						}
						else {
							queryRefResult = myKBase.queryRefSingleTerm((IdentifyingExpression)
									SL.instantiate(IOTA_VALUE_GT, "agent", applyResult.getTerm("agent")));
							if (queryRefResult != null) {
								if (((Constant)queryRefResult).intValue().longValue() >= queriedValue.longValue() ) {
									queryResult = new MatchResult();
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return queryResult;
			}

			@Override
			public boolean getObserverTriggerPatterns(Formula formula, Set set) {
				try {
					MatchResult applyResult = SL.match(pattern, formula);
					if (applyResult != null && applyResult.getTerm("X") instanceof Constant) {
						set.add(VALUE_X_PATTERN);
						set.add(VALUE_GT_X_PATTERN);
						set.add(NOT_VALUE_GT_X_PATTERN);
						return false;
					}
				}catch (SL.WrongTypeException wte) {
					wte.printStackTrace();
				}
				return true;
			}
		});

		defineFilter(new KBQueryFilterAdapter("(B ??agent " + NOT_VALUE_GT_X_PATTERN + ")") {
			// Compare the sought value with that known to give the answer. 
			@Override
			public MatchResult doApply(Formula formula, MatchResult applyResult) {
				MatchResult queryResult = null;
				try {
					if (applyResult.getTerm("X") instanceof Constant) {
						Long queriedValue = ((Constant)applyResult.getTerm("X")).intValue();
						Term queryRefResult = myKBase.queryRefSingleTerm((IdentifyingExpression)
								SL.instantiate(IOTA_VALUE, "agent", applyResult.getTerm("agent")));
						if (queryRefResult != null) {
							if ( ((Constant)queryRefResult).intValue().longValue() < queriedValue.longValue() ) {
								queryResult = new MatchResult();
							}
						}
						else {
							queryRefResult = myKBase.queryRefSingleTerm((IdentifyingExpression)
									SL.instantiate(IOTA_VALUE_NOT_GT, "agent", applyResult.getTerm("agent")));
							if (queryRefResult != null) {
								if (((Constant)queryRefResult).intValue().longValue() <= queriedValue.longValue() ) {
									queryResult = new MatchResult();
								}
							}
						}
					}
				} catch(Exception e) {e.printStackTrace();}
				return queryResult;
			}
			@Override
			public boolean getObserverTriggerPatterns(Formula formula, Set set) {
				try {
					MatchResult applyResult = SL.match(pattern, formula);
					if (applyResult != null && applyResult.getTerm("X") instanceof Constant) {
						set.add(NOT_VALUE_GT_X_PATTERN);
						set.add(VALUE_X_PATTERN);
						set.add(VALUE_GT_X_PATTERN);
						return false;
					}
				}catch (SL.WrongTypeException wte) {
					wte.printStackTrace();
				}
				return true;
			}
		});

	} // End of SingleNumValueDefinition

} // End of SingleNumValueDefinition