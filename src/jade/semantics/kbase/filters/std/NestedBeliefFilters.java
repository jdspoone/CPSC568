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
 * created on 19 janv. 2007 by Vincent Louis
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
import jade.semantics.lang.sl.grammar.AndNode;
import jade.semantics.lang.sl.grammar.BelieveNode;
import jade.semantics.lang.sl.grammar.ExistsNode;
import jade.semantics.lang.sl.grammar.ForallNode;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.IntentionNode;
import jade.semantics.lang.sl.grammar.MetaTermReferenceNode;
import jade.semantics.lang.sl.grammar.NotNode;
import jade.semantics.lang.sl.grammar.OrNode;
import jade.semantics.lang.sl.grammar.PersistentGoalNode;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.grammar.UncertaintyNode;
import jade.semantics.lang.sl.tools.MatchResult;
import jade.semantics.lang.sl.tools.SL;
import jade.util.leap.ArrayList;
import jade.util.leap.HashMap;
import jade.util.leap.Iterator;
import jade.util.leap.Set;

/**
 * @author Vincent Louis - France Telecom
 *
 * TODO process the reasons in the apply method
 */
public abstract class NestedBeliefFilters extends FiltersDefinition {

	private HashMap nestedKBases;
	private Term agentPattern;

	private Formula bPattern = SL.formula("(B ??myself ??phi)");
	private Formula notBPattern = SL.formula("(not (B ??myself ??phi))");
	
	public NestedBeliefFilters() {
		this(new MetaTermReferenceNode("agent"));
	}
	
	/**
	 * 
	 */
	public NestedBeliefFilters(Term agentPattern) {
		this.agentPattern = agentPattern;
		this.nestedKBases = new HashMap();
		
		defineFilter(new KBQueryFilter() {
			/* (non-Javadoc)
			 * @see jade.semantics.kbase.filter.KBQueryFilter#apply(jade.semantics.lang.sl.grammar.Formula, jade.semantics.lang.sl.grammar.Term)
			 */
			@Override
			public QueryResult apply(Formula formula, ArrayList falsityReasons, QueryResult.BoolWrapper goOn) {
				MatchResult applyResult = bPattern.match(formula); 
				if (applyResult!=null) {
					Formula nestedFormula = applyResult.formula("phi");
					MetaTermReferenceNode beliefAgent = new MetaTermReferenceNode("agent");
					Formula nestedBelief = nestedFormula.isBeliefFrom(beliefAgent);
					if (nestedBelief != null && handleNestedBeliefFrom(beliefAgent.sm_value())) {
						KBase nestedKBase = (KBase)nestedKBases.get(beliefAgent.sm_value().toString()); //FIXME
						if (nestedKBase != null) {
							return nestedKBase.query(nestedFormula);
						}
					}
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
			
			@Override
			public ArrayList toStrings() {
				ArrayList result = new ArrayList(nestedKBases.size() * 15);
				for (Iterator it = nestedKBases.values().iterator(); it.hasNext(); ) {
					KBase nestedKBase = (KBase)it.next();
					ArrayList stringKBase = nestedKBase.toStrings();
					result.add("******* Beliefs of agent "+nestedKBase.getAgentName()+" *******");
					for (Iterator jt = stringKBase.iterator(); jt.hasNext(); ) {
						String line = jt.next().toString();
						if (!line.startsWith("***")) {
							// to avoid adding the encapsulated title line "******* KBase *******" (see FilterKBaseImpl.toStrings() )
							result.add("(B " + nestedKBase.getAgentName() + " " + line + ")");
						}
					}
					result.add("***");
				}
				return result;
			}
		});
		
		defineFilter(new KBAssertFilterAdapter(bPattern) {
			/* (non-Javadoc)
			 * @see jade.semantics.kbase.filter.KBAssertFilterAdapter#doApply(jade.semantics.lang.sl.grammar.Formula, jade.semantics.lang.sl.tools.MatchResult)
			 */
			@Override
			public Formula doApply(Formula formula, MatchResult match) {
				Formula nestedFormula = match.formula("phi");
				MetaTermReferenceNode beliefAgent = new MetaTermReferenceNode("agent");
				boolean isNestedMentalAttitude = nestedFormula.isMentalAttitude(beliefAgent);
				Term nestedAgent = beliefAgent.sm_value();
				if (isNestedMentalAttitude && !(nestedAgent instanceof MetaTermReferenceNode)
						&& handleNestedBeliefFrom(nestedAgent)) {
					KBase nestedKBase = (KBase)nestedKBases.get(nestedAgent.toString()); // FIXME
					if (nestedKBase == null) {
						nestedKBase = newInstance(nestedAgent);
						nestedKBases.put(nestedAgent.toString(), nestedKBase); // FIXME
					}
					nestedKBase.assertFormula(nestedFormula);
					/* update Observers (since this formula is locally stored in the filter
					 * it is never asserted in the agent's KBase so observers are never 
					 * updated... (see ArrayListKBaseImpl.assertFormula)
					 * 
					 * useless to encapsulate the nestedFormula in a belief of the agent owning
					 * the FilterKBase since this operation is made by Observer.update(Formula)
					 */
					myKBase.updateObservers(nestedFormula);
					return SL.TRUE;
				}
				return super.doApply(formula, match);
			}
		});

		defineFilter(new KBAssertFilterAdapter(notBPattern) {
			/* (non-Javadoc)
			 * @see jade.semantics.kbase.filter.KBAssertFilterAdapter#doApply(jade.semantics.lang.sl.grammar.Formula, jade.semantics.lang.sl.tools.MatchResult)
			 */
			@Override
			public Formula doApply(Formula formula, MatchResult match) {
				Formula nestedFormula = match.formula("phi");
				MetaTermReferenceNode beliefAgent = new MetaTermReferenceNode("agent");
				boolean isNestedMentalAttitude = isPositiveMentalAttitude(nestedFormula, beliefAgent);
				Term nestedAgent = beliefAgent.sm_value();
				if (isNestedMentalAttitude && !(nestedAgent instanceof MetaTermReferenceNode)
						&& handleNestedBeliefFrom(nestedAgent)) {
					KBase nestedKBase = (KBase)nestedKBases.get(nestedAgent.toString()); // FIXME
					if (nestedKBase != null) {
						nestedKBase.assertFormula(new NotNode(nestedFormula));
						return SL.TRUE;
					}
				}
				return super.doApply(formula, match);
			}
		});
	}
	
	private boolean isPositiveMentalAttitude(Formula formula, Term term) {
    	if (formula instanceof BelieveNode) {
    		return checkOrInstantiate(term, ((BelieveNode)formula).as_agent());
		}
    	if (formula instanceof IntentionNode) {
    		return checkOrInstantiate(term, ((IntentionNode)formula).as_agent());    		
    	}
    	if (formula instanceof UncertaintyNode) {
    		return checkOrInstantiate(term, ((UncertaintyNode)formula).as_agent());    		
    	}
    	if (formula instanceof PersistentGoalNode) {
    		return checkOrInstantiate(term, ((PersistentGoalNode)formula).as_agent());    		
    	}
    	if (formula instanceof ExistsNode) {
    		return isPositiveMentalAttitude(((ExistsNode)formula).as_formula(), term);
    	}
    	if (formula instanceof ForallNode) {
    		return isPositiveMentalAttitude(((ForallNode)formula).as_formula(), term);
    	}
    	if (formula instanceof AndNode) {
    		return isPositiveMentalAttitude(((AndNode)formula).as_left_formula(), term)
    		&& isPositiveMentalAttitude(((AndNode)formula).as_right_formula(), term);
    	}
    	if (formula instanceof OrNode) {
    		return isPositiveMentalAttitude(((OrNode)formula).as_left_formula(), term)
    		&& isPositiveMentalAttitude(((OrNode)formula).as_right_formula(), term);
    	}
    	return false;
	}
	
	private boolean checkOrInstantiate(Term term, Term value) {
    	Term agent = term;
    	if (term instanceof MetaTermReferenceNode) {
    		agent = ((MetaTermReferenceNode)term).sm_value();
    		if (agent == null) {
    			((MetaTermReferenceNode)term).sm_value(value);
    			return true;
    		}
    	}
    	return value.equals(agent);
	}
	
	public boolean handleNestedBeliefFrom(Term agent) {
		return agentPattern.match(agent)!=null;
	}
		
	/**
	 * Creates a new instance of KBase. The new instance should be set exactly
	 * as the KBase instance that runs the invoked newInstance() method.
	 * For example, for FilterKBaseImpl instances, newly created instances
	 * should be set with the same filters as the original instance.
	 * 
	 * @param agent the agent the beliefs of the new instance of KBase belongs to
	 * @return a new instance of KBase
	 */
	public abstract KBase newInstance(Term agent);

}
