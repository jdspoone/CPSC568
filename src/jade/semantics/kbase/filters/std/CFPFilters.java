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
 * created on 28 mars 2007 by Vincent Louis
 */

/**
 * 
 */
package jade.semantics.kbase.filters.std;

import jade.semantics.kbase.KBase;
import jade.semantics.kbase.QueryResult;
import jade.semantics.kbase.filters.FiltersDefinition;
import jade.semantics.kbase.filters.KBAssertFilterAdapter;
import jade.semantics.kbase.filters.KBQueryFilter;
import jade.semantics.lang.sl.grammar.ActionExpressionNode;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.Node;
import jade.semantics.lang.sl.grammar.NotNode;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.tools.MatchResult;
import jade.semantics.lang.sl.tools.SL;
import jade.util.leap.ArrayList;
import jade.util.leap.Set;

/**
 * @author Vincent Louis - France Telecom
 *
 * TODO process the reasons in the apply method
 */
public class CFPFilters extends FiltersDefinition {
	
	
	Formula proposalVectorPattern = SL.formula("(B (sequence ??__agent ??__act) ??__condition)");
	Formula b_MYSELF_propose_AGENT_ACT_CONDITION =
		SL.formula("(B ??myself (or (not (I ??__agent (done ??__act ??__condition))) (I ??myself (done ??__act ??__condition))))");
	Formula not_b_MYSELF_propose_AGENT_ACT_CONDITION = new NotNode(b_MYSELF_propose_AGENT_ACT_CONDITION);
	
	private ArrayList proposals;
	private KBase myKBase;
	
	/**
	 * 
	 */
	public CFPFilters(KBase kbase) {
		proposals = new ArrayList();
		myKBase = kbase;
		
		/***********************************************************************
		 * ASSERTING A PROPOSAL
		 **********************************************************************/
		defineFilter(//0,
				new KBAssertFilterAdapter(b_MYSELF_propose_AGENT_ACT_CONDITION) {
			/* (non-Javadoc)
			 * @see jade.semantics.kbase.filter.KBAssertFilterAdapter#doApply(jade.semantics.lang.sl.grammar.Formula, jade.semantics.lang.sl.tools.MatchResult)
			 */
			@Override
			public Formula doApply(Formula formula, MatchResult match) {
				if (checkProposalPattern(match)) {
					Node proposalVector = buildProposalVector(match);
					if (proposalVector != null && !proposals.contains(proposalVector)) {
						proposals.add(proposalVector);
					}
					return SL.TRUE;
				}
				return formula;
			}
		});
		
		/***********************************************************************
		 * RETRACTING A PATTERN OF PROPOSALS
		 **********************************************************************/
		defineFilter(//0,
				new KBAssertFilterAdapter(not_b_MYSELF_propose_AGENT_ACT_CONDITION) {
			/* (non-Javadoc)
			 * @see jade.semantics.kbase.filter.KBAssertFilterAdapter#doApply(jade.semantics.lang.sl.grammar.Formula, jade.semantics.lang.sl.tools.MatchResult)
			 */
			@Override
			public Formula doApply(Formula formula, MatchResult match) {
//				System.err.println("###### f = " + formula); // DEBUG
//				System.err.println("@@@@@@ m = " + match); // DEBUG
				for (int i=proposals.size()-1; i>=0; i--) {
					if (SL.match((Node)proposals.get(i), buildProposalVector(match)) != null) {
						proposals.remove(i);
					}
				}
				return SL.TRUE;
			}	
		});
		
		/***********************************************************************
		 * QUERYING FOR A PROPOSAL
		 **********************************************************************/
		defineFilter(//0,
				new KBQueryFilter() {
			/* (non-Javadoc)
			 * @see jade.semantics.kbase.filter.KBQueryFilter#apply(jade.semantics.lang.sl.grammar.Formula, jade.semantics.lang.sl.grammar.Term)
			 */
			@Override
			public QueryResult apply(Formula formula, ArrayList falsityReasons, QueryResult.BoolWrapper goOn) {
				MatchResult match = b_MYSELF_propose_AGENT_ACT_CONDITION.match(formula);
				if (checkProposalPattern(match)) {
					QueryResult result = new QueryResult();
					Node proposalVector = buildProposalVector(match);
					for (int i=0; i<proposals.size(); i++) {
						MatchResult matchVector = SL.match(proposalVector, (Node)proposals.get(i));
						if (matchVector != null) {
							result.add(matchVector);
						}
					}
					return result;
				}
				return null;
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
	
	private boolean checkProposalPattern(MatchResult match) {
		if (match != null) {
			Term act = match.term("__act");
			Term agent = match.term("__agent");
			return (//myKBase.getAgentName().equals(match.term("myself")) && // should be implicit
					!myKBase.getAgentName().equals(agent) &&
					act instanceof ActionExpressionNode &&
					myKBase.getAgentName().equals(((ActionExpressionNode)act).getActor()));
		}
		return false;
	}
	
	private Node buildProposalVector(MatchResult match) {
		return SL.instantiate(proposalVectorPattern,
				"__agent", match.term("__agent"),
				"__act", match.term("__act"),
				"__condition", match.formula("__condition"));
	}
}

