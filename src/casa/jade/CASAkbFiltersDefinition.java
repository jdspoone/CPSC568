/**
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. 
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  
 * The  Knowledge Science Group makes no representations about the suitability
 * of  this software for any purpose.  It is provided "as is" without express
 * or implied warranty.</p>
 * <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
package casa.jade;

import casa.util.CASAUtil;

import jade.semantics.kbase.QueryResult;
import jade.semantics.kbase.QueryResult.BoolWrapper;
import jade.semantics.kbase.filters.FiltersDefinition;
import jade.semantics.kbase.filters.KBAssertFilterAdapter;
import jade.semantics.kbase.filters.KBQueryFilter;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.ListOfParameter;
import jade.semantics.lang.sl.grammar.Node;
import jade.semantics.lang.sl.grammar.ParameterNode;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.grammar.TrueNode;
import jade.semantics.lang.sl.grammar.WordConstantNode;
import jade.semantics.lang.sl.tools.MatchResult;
import jade.semantics.lang.sl.tools.SL;
import jade.semantics.lang.sl.tools.SL.WrongTypeException;
import jade.util.leap.ArrayList;
import jade.util.leap.Set;

/**
	 * Filter definitions to match patterns like (B Alice exp) and put or get the
	 * expression in Alice's own (sub) belief base  
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class CASAkbFiltersDefinition extends FiltersDefinition {
	/**
	 * Patterns used to manage the defined predicate
	 */
	private Formula AGENT_BELIEVE_X_PATTERN;
	private Formula NOT_AGENT_BELIEVE_X_PATTERN;
	private Formula AGENT_BELIEVE_IOTA_X_PATTERN;
	private Formula AGENT_BELIEVE_ALL_X_PATTERN ;
	private Formula AGENT_BELIEVE_ANY_X_PATTERN ;
	private Formula AGENT_BELIEVE_SOME_X_PATTERN;

	//	   /**
	//	    * Removes from the base all the belief about this kind of predicat
	//	    * @param kbase the base to clean
	//	    */
	//	   protected void cleanKBase(FilterKBase kbase) {
	//	       kbase.retractFormula(NOT_VALUE_X_PATTERN);
	//	       kbase.retractFormula(VALUE_X_PATTERN);
	//	       kbase.retractFormula(VALUE_GT_X_PATTERN);
	//	       kbase.retractFormula(NOT_VALUE_GT_X_PATTERN);
	//	   } // End of cleanKBase/1

	public String getAgentFromMatch(MatchResult match) throws WrongTypeException {
		try {
			Term agentTerm = match.getTerm("A");
			//								 String agentName = agentTerm.toString();
			Node[] children = agentTerm.children();
			ListOfParameter param = (ListOfParameter)children[1];
			ParameterNode pNode = (ParameterNode)param.getFirst();
			children = pNode.children();
			WordConstantNode wcn = (WordConstantNode)children[0];
			return wcn.lx_value();
		} catch (WrongTypeException e) {
			throw e;
		} catch (Throwable e) {
			CASAUtil.log("error", "CASAKB.getAgentFromMatch("+match+")", e, true);
			WrongTypeException ex = new WrongTypeException();
			ex.initCause(e);
			throw ex;
		}
	}
	
	String name;
	CasaKB kb;
	
	
	/**
	 * A filter to stuff beliefs of other agents into their own sub-belief bases.  That is, 
	 * each agent that we know about has a belief base of it's own that contains it's beliefs.
	 * If we see an exression (B Alice (??X)), we will automatically put ??X into Alice's 
	 * local belief base, creating it if necessary.
	 * @param name name of the predicat 
	 */
	public CASAkbFiltersDefinition(String name_, CasaKB kb_) 
	{
		name = name_;
		kb = kb_;
		
		AGENT_BELIEVE_X_PATTERN = SL.formula("(B ??A ??X)");

		NOT_AGENT_BELIEVE_X_PATTERN = SL.formula("(not (B ??A ??X))");

		//			AGENT_BELIEVE_IOTA_X_PATTERN = SL.formula("(B ??A (iota ?y ??X))");
		//			AGENT_BELIEVE_ALL_X_PATTERN  = SL.formula("(B ??A (all  ?y ??X))");
		//			AGENT_BELIEVE_ANY_X_PATTERN  = SL.formula("(B ??A (any  ?y ??X))");
		//			AGENT_BELIEVE_SOME_X_PATTERN = SL.formula("(some ?y (B ??A ??X))");

		// ASSERT FILTERS
		// --------------
		defineFilter(new KBAssertFilterAdapter("(B ??agent " + AGENT_BELIEVE_X_PATTERN + ")") {
			
			Formula BBPattern = SL.formula("(B ??agent " + AGENT_BELIEVE_X_PATTERN + ")");
			
			//If the predicat is already in the base, does nothing, otherwise
			//cleans the base of all knowledge related to this predicate
			@Override
			public Formula doApply(Formula formula, MatchResult match) {
				try {
					Formula x = match.getFormula("X");
					String agentName = getAgentFromMatch(match);
					if (agentName.equals(name)) 
						return formula;
					kb.agentAssert(agentName,x);
					return new TrueNode();
				} catch (WrongTypeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return formula;
			}
		});


		// QUERY FILTERS
		// -------------

		defineFilter(new KBQueryFilter() {
			
			Formula BBPattern = SL.formula("(B ??agent " + AGENT_BELIEVE_X_PATTERN + ")");
			
			@Override
			public QueryResult apply(Formula formula, ArrayList falsityReasons,
					BoolWrapper goOn) {
//				formula = SL.formula("(B ??agent " + AGENT_BELIEVE_X_PATTERN + ")");
				MatchResult match = SL.match(BBPattern, formula);
				if (match != null) {
					//MatchResult result = doApply(formula, match);
					QueryResult result;
					try {
						String agentName = getAgentFromMatch(match);
						Formula x = match.getFormula("X");
						if (agentName.equals(name)) 
							return null; //new MatchResult();
						Node node = SL.instantiate(AGENT_BELIEVE_X_PATTERN, "X", x);
						node = SL.instantiate(node, "A", match.getTerm("A"));
						result = kb.agentQuery(agentName,(Formula)node);
						if (result != null) {
							goOn.setBool(false); // TODO to remove 
							return result;
						} 
						return result;
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}
				return null; // Assume a filter that doesn't match provide no solution
			}

			@Override
			public boolean getObserverTriggerPatterns(Formula formula, Set set) {
				return true;
			}

		}); 

		
		defineFilter(new NotFilter());
		defineFilter(new IfFilter());
	} 

}
