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

package jade.semantics.kbase.filters.std;

import jade.semantics.kbase.KBase;
import jade.semantics.kbase.QueryResult;
import jade.semantics.kbase.filters.FilterKBase;
import jade.semantics.kbase.filters.FiltersDefinition;
import jade.semantics.kbase.filters.KBAssertFilterAdapter;
import jade.semantics.kbase.filters.KBQueryFilter;
import jade.semantics.kbase.filters.std.builtins.BinaryPredicate;
import jade.semantics.kbase.filters.std.builtins.TernaryPredicate;
import jade.semantics.kbase.filters.std.builtins.UnaryPredicate;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.MetaTermReferenceNode;
import jade.semantics.lang.sl.grammar.SymbolNode;
import jade.semantics.lang.sl.tools.MatchResult;
import jade.semantics.lang.sl.tools.SL;
import jade.util.leap.ArrayList;
import jade.util.leap.Set;

import java.util.HashMap;

/**
 * @author Vincent Louis - France Telecom
 *
 * TODO process the reasons in the apply method
 * 
 */
public class BuiltInPredicateFilters extends FiltersDefinition {
	
	
	HashMap<String,UnaryPredicate> unaryPredicates = new HashMap<String,UnaryPredicate>();
	HashMap<String,BinaryPredicate> binaryPredicates = new HashMap<String,BinaryPredicate>();
	HashMap<String,TernaryPredicate> ternaryPredicates = new HashMap<String,TernaryPredicate>();

	KBase kbase;

	Formula PREDICATE_V1 = SL.formula("(??predicate ??v1)");
	Formula PREDICATE_V1_V2 = SL.formula("(??predicate ??v1 ??v2)");
	Formula PREDICATE_V1_V2_V3 = SL.formula("(??predicate ??v1 ??v2 ??v3)");
	
	/***************************************************************************
	 * PRIVATE METHODS
	 **************************************************************************/

	private UnaryPredicate handleUnaryPredicate(
			MatchResult match, MetaTermReferenceNode v1,
			KBase kb) { 
		UnaryPredicate result = unaryPredicates.get(((SymbolNode)match.symbol("predicate")).toString());
		if (result != null) {
			v1.sm_value(kb.eval(match.term("v1")));
			if (v1.sm_value() != null) {
				return result;
			}
		}
		return null;
	}

	private BinaryPredicate handleBinaryPredicate(
			MatchResult match, MetaTermReferenceNode v1, MetaTermReferenceNode v2,
			KBase kb) { 
		BinaryPredicate result = binaryPredicates.get(((SymbolNode)match.symbol("predicate")).toString());
		if (result != null) {
			v1.sm_value(kb.eval(match.term("v1")));
			if (v1.sm_value() != null) {
				v2.sm_value(kb.eval(match.term("v2")));
				if (v2.sm_value() != null) {
					return result;
				}
			}
		}
		return null;
	}
	
	private TernaryPredicate handleTernaryPredicate(
			MatchResult match, MetaTermReferenceNode v1, MetaTermReferenceNode v2, MetaTermReferenceNode v3,
			KBase kb) { 
		TernaryPredicate result = ternaryPredicates.get(((SymbolNode)match.symbol("predicate")).toString());
		if (result != null) {
			v1.sm_value(kb.eval(match.term("v1")));
			if (v1.sm_value() != null) {
				v2.sm_value(kb.eval(match.term("v2")));
				if (v2.sm_value() != null) {
					v3.sm_value(kb.eval(match.term("v3")));
					if (v3.sm_value() != null) {
						return result;
					}
				}
			}
		}
		return null;
	}

//	private Term getValue(Term value, KBase kb) {
//		// Get the most "deep" Constant or MetaTermReferenceNode refered to by the term
//		while ((value instanceof MetaTermReferenceNode)
//				&& (((MetaTermReferenceNode)value).sm_value() != null)) {
//			value = ((MetaTermReferenceNode)value).sm_value();
//		}
//
//		if (!(value instanceof MetaTermReferenceNode)) {
//			// If there are unbound meta-references, then return null
//			if (value.find(new Class[] {MetaContentExpressionReferenceNode.class,
//					MetaFormulaReferenceNode.class, 
//					MetaSymbolReferenceNode.class, 
//					MetaTermReferenceNode.class, 
//					MetaVariableReferenceNode.class}, "sm_value", null, new ListOfNodes(), false)) {
//				value = null;
//			}
//			// If value is an IRE, resolves it and assign the actual value
//			else if (value instanceof IdentifyingExpression) {
//				ListOfTerm queryResult = kb.queryRef((IdentifyingExpression)value);
//				if (queryResult == null) {
//					value = null;
//				}
//				else if (value instanceof AnyNode || value instanceof IotaNode) {
//					value = queryResult.first();
//				}
//				else {
//					value = new TermSetNode(queryResult);
//				}
//			}
//		}
//		return value;
//	}

	/***************************************************************************
	 * CONSTRUCTOR: definition of all filters to access predefined ("built-in") predicates
	 **************************************************************************/
	public BuiltInPredicateFilters(KBase kb) {
		this.kbase = kb;

		/***********************************************************************
		 * ASSERTING (??predicate ??v1)
		 **********************************************************************/
		defineFilter(new KBAssertFilterAdapter("(B ??myself " + PREDICATE_V1 + ")") {
			/* (non-Javadoc)
			 * @see jade.semantics.kbase.filter.KBAssertFilterAdapter#doApply(jade.semantics.lang.sl.grammar.Formula, jade.semantics.lang.sl.tools.MatchResult)
			 */
			@Override
			public Formula doApply(Formula formula, MatchResult match) {
				MetaTermReferenceNode v1 = new MetaTermReferenceNode();
				UnaryPredicate predicate = handleUnaryPredicate(match, v1, kbase);
				if (predicate != null) {
					Formula result = predicate.assertTrue(v1.sm_value(), kbase);
					return (result == null ? formula : result);
				}
				return formula;
			}
		});
		
		defineFilter(new KBAssertFilterAdapter("(B ??myself (not " + PREDICATE_V1 + "))") {
			/* (non-Javadoc)
			 * @see jade.semantics.kbase.filter.KBAssertFilterAdapter#doApply(jade.semantics.lang.sl.grammar.Formula, jade.semantics.lang.sl.tools.MatchResult)
			 */
			@Override
			public Formula doApply(Formula formula, MatchResult match) {
				MetaTermReferenceNode v1 = new MetaTermReferenceNode();
				UnaryPredicate predicate = handleUnaryPredicate(match, v1, kbase);
				if (predicate != null) {
					Formula result = predicate.assertFalse(v1.sm_value(), kbase);
					return (result == null ? formula : result);
				}
				return formula;
			}
		});

		defineFilter(new KBAssertFilterAdapter("(not (B ??myself " + PREDICATE_V1 + "))") {
			/* (non-Javadoc)
			 * @see jade.semantics.kbase.filter.KBAssertFilterAdapter#doApply(jade.semantics.lang.sl.grammar.Formula, jade.semantics.lang.sl.tools.MatchResult)
			 */
			@Override
			public Formula doApply(Formula formula, MatchResult match) {
				MetaTermReferenceNode v1 = new MetaTermReferenceNode();
				UnaryPredicate predicate = handleUnaryPredicate(match, v1, kbase);
				if (predicate != null) {
					Formula result = predicate.retract(v1.sm_value(), kbase);
					return (result == null ? formula : result);
				}
				return formula;
			}
		});

		/***********************************************************************
		 * ASSERTING (??predicate ??v1 ??v2)
		 **********************************************************************/
		defineFilter(new KBAssertFilterAdapter("(B ??myself " + PREDICATE_V1_V2 + ")") {
			/* (non-Javadoc)
			 * @see jade.semantics.kbase.filter.KBAssertFilterAdapter#doApply(jade.semantics.lang.sl.grammar.Formula, jade.semantics.lang.sl.tools.MatchResult)
			 */
			@Override
			public Formula doApply(Formula formula, MatchResult match) {
				MetaTermReferenceNode v1 = new MetaTermReferenceNode();
				MetaTermReferenceNode v2 = new MetaTermReferenceNode();
				BinaryPredicate predicate = handleBinaryPredicate(match, v1, v2, kbase);
				if (predicate != null) {
					Formula result = predicate.assertTrue(v1.sm_value(), v2.sm_value(), kbase);
					return (result == null ? formula : result);
				}
				return formula;
			}
		});
		
		defineFilter(new KBAssertFilterAdapter("(B ??myself (not " + PREDICATE_V1_V2 + "))") {
			/* (non-Javadoc)
			 * @see jade.semantics.kbase.filter.KBAssertFilterAdapter#doApply(jade.semantics.lang.sl.grammar.Formula, jade.semantics.lang.sl.tools.MatchResult)
			 */
			@Override
			public Formula doApply(Formula formula, MatchResult match) {
				MetaTermReferenceNode v1 = new MetaTermReferenceNode();
				MetaTermReferenceNode v2 = new MetaTermReferenceNode();
				BinaryPredicate predicate = handleBinaryPredicate(match, v1, v2, kbase);
				if (predicate != null) {
					Formula result = predicate.assertFalse(v1.sm_value(), v2.sm_value(), kbase);
					return (result == null ? formula : result);
				}
				return formula;
			}
		});

		defineFilter(new KBAssertFilterAdapter("(not (B ??myself " + PREDICATE_V1_V2 + "))") {
			/* (non-Javadoc)
			 * @see jade.semantics.kbase.filter.KBAssertFilterAdapter#doApply(jade.semantics.lang.sl.grammar.Formula, jade.semantics.lang.sl.tools.MatchResult)
			 */
			@Override
			public Formula doApply(Formula formula, MatchResult match) {
				MetaTermReferenceNode v1 = new MetaTermReferenceNode();
				MetaTermReferenceNode v2 = new MetaTermReferenceNode();
				BinaryPredicate predicate = handleBinaryPredicate(match, v1, v2, kbase);
				if (predicate != null) {
					Formula result = predicate.retract(v1.sm_value(), v2.sm_value(), kbase);
					return (result == null ? formula : result);
				}
				return formula;
			}
		});

		/***********************************************************************
		 * ASSERTING (??predicate ??v1 ??v2 ??v3)
		 **********************************************************************/
		
		defineFilter(new KBAssertFilterAdapter("(B ??myself " + PREDICATE_V1_V2_V3 + ")") {
			/* (non-Javadoc)
			 * @see jade.semantics.kbase.filter.KBAssertFilterAdapter#doApply(jade.semantics.lang.sl.grammar.Formula, jade.semantics.lang.sl.tools.MatchResult)
			 */
			@Override
			public Formula doApply(Formula formula, MatchResult match) {
				MetaTermReferenceNode v1 = new MetaTermReferenceNode();
				MetaTermReferenceNode v2 = new MetaTermReferenceNode();
				MetaTermReferenceNode v3 = new MetaTermReferenceNode();
				TernaryPredicate predicate = handleTernaryPredicate(match, v1, v2, v3, kbase);
				if (predicate != null) {
					Formula result = predicate.assertTrue(v1.sm_value(), v2.sm_value(), v3.sm_value(), kbase);
					return (result == null ? formula : result);
				}
				return formula;
			}
		});
		
		defineFilter(new KBAssertFilterAdapter("(B ??myself (not " + PREDICATE_V1_V2_V3 + "))") {
			/* (non-Javadoc)
			 * @see jade.semantics.kbase.filter.KBAssertFilterAdapter#doApply(jade.semantics.lang.sl.grammar.Formula, jade.semantics.lang.sl.tools.MatchResult)
			 */
			@Override
			public Formula doApply(Formula formula, MatchResult match) {
				MetaTermReferenceNode v1 = new MetaTermReferenceNode();
				MetaTermReferenceNode v2 = new MetaTermReferenceNode();
				MetaTermReferenceNode v3 = new MetaTermReferenceNode();
				TernaryPredicate predicate = handleTernaryPredicate(match, v1, v2, v3, kbase);
				if (predicate != null) {
					Formula result = predicate.assertFalse(v1.sm_value(), v2.sm_value(), v3.sm_value(), kbase);
					return (result == null ? formula : result);
				}
				return formula;
			}
		});

		defineFilter(new KBAssertFilterAdapter("(not (B ??myself " + PREDICATE_V1_V2_V3 + "))") {
			/* (non-Javadoc)
			 * @see jade.semantics.kbase.filter.KBAssertFilterAdapter#doApply(jade.semantics.lang.sl.grammar.Formula, jade.semantics.lang.sl.tools.MatchResult)
			 */
			@Override
			public Formula doApply(Formula formula, MatchResult match) {
				MetaTermReferenceNode v1 = new MetaTermReferenceNode();
				MetaTermReferenceNode v2 = new MetaTermReferenceNode();
				MetaTermReferenceNode v3 = new MetaTermReferenceNode();
				TernaryPredicate predicate = handleTernaryPredicate(match, v1, v2, v3, kbase);
				if (predicate != null) {
					Formula result = predicate.retract(v1.sm_value(), v2.sm_value(), v3.sm_value(), kbase);
					return (result == null ? formula : result);
				}
				return formula;
			}
		});

		/***********************************************************************
		 * QUERYING (??predicate ??v1)
		 **********************************************************************/
		defineFilter(new KBQueryFilter() {
			Formula pattern = SL.formula("(B ??myself " + PREDICATE_V1 + ")");
			
			/* (non-Javadoc)
			 * @see jade.semantics.kbase.filter.KBQueryFilter#apply(jade.semantics.lang.sl.grammar.Formula, jade.semantics.lang.sl.grammar.Term)
			 */
			@Override
			public QueryResult apply(Formula formula, ArrayList falsityReasons, QueryResult.BoolWrapper goOn) {
				MatchResult match = pattern.match(formula);
				if (match != null) {
					MetaTermReferenceNode v1 = new MetaTermReferenceNode();
					UnaryPredicate predicate = handleUnaryPredicate(match, v1, kbase);
					if (predicate != null) {
						return predicate.query(v1.sm_value(), kbase);
					}
				}
				return QueryResult.UNKNOWN;
			}
			
			/* (non-Javadoc)
			 * @see jade.semantics.kbase.filter.KBQueryFilter#getObserverTriggerPatterns(jade.semantics.lang.sl.grammar.Formula, jade.util.leap.Set)
			 */
			@Override
			public boolean getObserverTriggerPatterns(Formula formula, Set set) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		
		/***********************************************************************
		 * QUERYING (??predicate ??v1 ??v2)
		 **********************************************************************/		
		defineFilter(new KBQueryFilter() {
			Formula pattern = SL.formula("(B ??myself " + PREDICATE_V1_V2 + ")");
			
			/* (non-Javadoc)
			 * @see jade.semantics.kbase.filter.KBQueryFilter#apply(jade.semantics.lang.sl.grammar.Formula, jade.semantics.lang.sl.grammar.Term)
			 */
			@Override
			public QueryResult apply(Formula formula, ArrayList falsityReasons, QueryResult.BoolWrapper goOn) {
				MatchResult match = pattern.match(formula);
				if (match != null) {
					MetaTermReferenceNode v1 = new MetaTermReferenceNode();
					MetaTermReferenceNode v2 = new MetaTermReferenceNode();
					BinaryPredicate predicate = handleBinaryPredicate(match, v1, v2, kbase);
					if (predicate != null) {
						return predicate.query(v1.sm_value(), v2.sm_value(), kbase);
					}
				}
				return QueryResult.UNKNOWN;
			}
			
			/* (non-Javadoc)
			 * @see jade.semantics.kbase.filter.KBQueryFilter#getObserverTriggerPatterns(jade.semantics.lang.sl.grammar.Formula, jade.util.leap.Set)
			 */
			@Override
			public boolean getObserverTriggerPatterns(Formula formula, Set set) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		
		
		/***********************************************************************
		 * QUERYING (??predicate ??v1 ??v2 ??v3)
		 **********************************************************************/		
		defineFilter(new KBQueryFilter() {
			Formula pattern = SL.formula("(B ??myself " + PREDICATE_V1_V2_V3 + ")");
			
			/* (non-Javadoc)
			 * @see jade.semantics.kbase.filter.KBQueryFilter#apply(jade.semantics.lang.sl.grammar.Formula, jade.semantics.lang.sl.grammar.Term)
			 */
			@Override
			public QueryResult apply(Formula formula, ArrayList falsityReasons, QueryResult.BoolWrapper goOn) {
				MatchResult match = pattern.match(formula);
				if (match != null) {
					MetaTermReferenceNode v1 = new MetaTermReferenceNode();
					MetaTermReferenceNode v2 = new MetaTermReferenceNode();
					MetaTermReferenceNode v3 = new MetaTermReferenceNode();
					TernaryPredicate predicate = handleTernaryPredicate(match, v1, v2, v3, kbase);
					if (predicate != null) {
						return predicate.query(v1.sm_value(), v2.sm_value(), v3.sm_value(), kbase);
					}
				}
				return QueryResult.UNKNOWN;
			}
			
			/* (non-Javadoc)
			 * @see jade.semantics.kbase.filter.KBQueryFilter#getObserverTriggerPatterns(jade.semantics.lang.sl.grammar.Formula, jade.util.leap.Set)
			 */
			@Override
			public boolean getObserverTriggerPatterns(Formula formula, Set set) {
				// TODO Auto-generated method stub
				return false;
			}
		});
	}
	
	/***************************************************************************
	 * PUBLIC METHODS
	 **************************************************************************/
	public void addPredicate(UnaryPredicate predicate) {
		unaryPredicates.put(predicate.getId(), predicate);
	}
	
	public void addPredicate(BinaryPredicate predicate) {
		binaryPredicates.put(predicate.getId(), predicate);
	}

	public void addPredicate(TernaryPredicate predicate) {
		ternaryPredicates.put(predicate.getId(), predicate);
	}
	
	static HashMap<KBase, BuiltInPredicateFilters> builtInPredicateFilters = new HashMap<KBase, BuiltInPredicateFilters>();
	
	static public BuiltInPredicateFilters getInstance(FilterKBase kbase) {
		BuiltInPredicateFilters result = builtInPredicateFilters.get(kbase);
		if (result == null) {
			result = new BuiltInPredicateFilters(kbase);
			builtInPredicateFilters.put(kbase, result);
			kbase.addFiltersDefinition(result);
		}
		return result;
	}
}
