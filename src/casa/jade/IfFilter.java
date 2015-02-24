package casa.jade;

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

import jade.semantics.kbase.QueryResult;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.Node;
import jade.semantics.lang.sl.tools.MatchResult;
import jade.semantics.lang.sl.tools.SL;
import jade.util.leap.ArrayList;
import jade.util.leap.Set;


/**
 * This filter applies when a "not formula" is queried from the belief Base.
 */
public class IfFilter extends CasaQueryFilter {


	/**
	 * Pattern that must match to apply the filter
	 */
	private Formula pattern;
	private Formula pattern1;

	/*********************************************************************/
	/**                          CONSTRUCTOR                             **/
	/*********************************************************************/

	/**
	 * Creates a new Filter on the pattern (and ??phi ??psi)
	 */
	public IfFilter() {
		pattern = SL.formula("(B ??agent (if ??cond ??a ??b))");
		pattern1 = SL.formula("(if ??cond ??a ??b)");
	} // End of AndFilter/1

	/*********************************************************************/
	/**                          METHODS                                 **/
	/*********************************************************************/

	/** 
	 * If the formula matches the pattern (B ??agent (not ??phi)) and
	 * ??agent equals the given agent, this method returns a {@link QueryResult} holding
	 * the MatchResults of ??phi, that is the MatchResults
	 * that satisfy ??phi. Else it returns {@link QueryResult.UNKNOWN}
	 * @param formula a formula on which the filter is tested
	 * @param agent a term that represents the agent is trying to apply the filter
	 * @return a QueryResult as described above. 
	 */
	@Override
	public QueryResult apply(Formula formula, ArrayList falsityReasons, QueryResult.BoolWrapper goOn) {
		MatchResult applyResult = SL.match(pattern,formula);
		if (applyResult==null)
			applyResult = SL.match(pattern1,formula);
		Node condition = null;
		Node a = null;
		Node b = null;
		if (applyResult != null) 
		{
			goOn.setBool(false); // Further filters must not be applied

			condition = getNode(applyResult, "cond");
			a = getNode(applyResult,"a");  
			b = getNode(applyResult,"b");  

			String cond = condition.toString();
			
			Formula condFor = SL.formula(cond);//new BelieveNode(myKBase.getAgentName(), SL.formula(cond)).getSimplifiedFormula();
			QueryResult result = myKBase.getWrappingKBase().query(condFor);
			
			if (result==null)
				return myKBase.getWrappingKBase().query(SL.formula(b.toString()));
			else
				return myKBase.getWrappingKBase().query(SL.formula(a.toString()));
		}
		return null;
	} 

	/**
	 * Adds in the set, the patterns for the formula phi and for the formula
	 * psi.
	 * @param formula an observed formula
	 * @param set set of patterns. Each pattern corresponds to a kind a formula
	 * which, if it is asserted in the base, triggers the observer that
	 * observes the formula given in parameter.
	 */
	@Override
	public boolean getObserverTriggerPatterns(Formula formula, Set set) {
		MatchResult match = SL.match(pattern,formula);
		if (match != null) {
			try {
				myKBase.getObserverTriggerPatterns(match.getFormula("phi"), set);
				return false;
			} catch (SL.WrongTypeException wte) {
				wte.printStackTrace();
			}
		}
		return true;
	}

}
