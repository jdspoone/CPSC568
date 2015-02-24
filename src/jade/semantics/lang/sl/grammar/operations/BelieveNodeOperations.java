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

package jade.semantics.lang.sl.grammar.operations;

import jade.semantics.lang.sl.grammar.AndNode;
import jade.semantics.lang.sl.grammar.BelieveNode;
import jade.semantics.lang.sl.grammar.ExistsNode;
import jade.semantics.lang.sl.grammar.FalseNode;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.MetaTermReferenceNode;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.grammar.TrueNode;

public class BelieveNodeOperations extends FormulaNodeOperations {
	
	@Override
	public void simplify(Formula node) {
        Term agent = ((BelieveNode) node).as_agent().sm_simplified_term();
        Formula formula = ((BelieveNode) node).as_formula().sm_simplified_formula();

//    	System.err.println("####### SIMPLIFY " + agent + " / " + formula);

        MetaTermReferenceNode agentVar = new MetaTermReferenceNode(Integer.toString(agent.hashCode()));
        
        if (formula instanceof TrueNode || formula instanceof FalseNode) {
            node.sm_simplified_formula(formula);
//        	System.err.println("####### CAS 1 = " + node.sm_simplified_formula());
        }
        else if (formula instanceof AndNode) {
			Formula formulaLeft = ((AndNode) formula).as_left_formula().sm_simplified_formula();
			Formula formulaRight = ((AndNode) formula).as_right_formula().sm_simplified_formula();
			node.sm_simplified_formula((new AndNode(new BelieveNode(agent, formulaLeft), new BelieveNode(agent,
                    formulaRight))).getSimplifiedFormula());
//        	System.err.println("####### CAS 2 = " + node.sm_simplified_formula());
        }
//        else if (formula instanceof ForallNode) {
//        	Variable var = ((ForallNode)formula).as_variable();
//        	Formula quantifiedFormula = ((ForallNode)formula).as_formula();
//        	node.sm_simplified_formula((new ForallNode(var, new BelieveNode(agent, quantifiedFormula))).getSimplifiedFormula());
//        }
        else if (formula.isMentalAttitude(agentVar) &&
        		agent.equals(agentVar.sm_value())) {
//        	System.err.println("####### " + agent + " / " + agentVar + " / " + formula);
			node.sm_simplified_formula(formula);
//        	System.err.println("####### CAS 3 = " + node.sm_simplified_formula());
        }
        else {
			BelieveNode believeNode = new BelieveNode(agent, formula);
			believeNode.sm_simplified_formula(believeNode);
			node.sm_simplified_formula(believeNode);
//        	System.err.println("####### CAS 4 = " + node.sm_simplified_formula());
        }
    }

    @Override
	public boolean isMentalAttitude(Formula node, Term term) {
    	Term agent = term;
    	if (term instanceof MetaTermReferenceNode) {
    		agent = ((MetaTermReferenceNode)term).sm_value();
    		if (agent == null) {
    			((MetaTermReferenceNode)term).sm_value(((BelieveNode) node).as_agent());
    			return true;
    		}
    	}
        return ((BelieveNode) node).as_agent().equals(agent);
    }

    @Override
	public Formula isBeliefFrom(Formula node, Term agent) {
        if (agent instanceof MetaTermReferenceNode) {
            ((MetaTermReferenceNode)agent).sm_value(((BelieveNode) node).as_agent());
            return ((BelieveNode) node).as_formula();
        } else if (((BelieveNode) node).as_agent().equals(agent)) {
            return ((BelieveNode) node).as_formula();
        }
        return null;
    }

    @Override
	public boolean isSubsumedBy(Formula node, Formula formula) {
        if (super.isSubsumedBy(node, formula)) {
            return true;
        }
        else if (formula instanceof BelieveNode) {
            return ((BelieveNode) node).as_agent().equals(((BelieveNode) formula).as_agent())
                    && ((BelieveNode) node).as_formula().isSubsumedBy(((BelieveNode) formula).as_formula());
        }
//        else if (formula instanceof IntentionNode) {
//            return ((BelieveNode) node).as_agent().equals(((IntentionNode) formula).as_agent())
//                    && ((BelieveNode) node).isSubsumedBy(
//                            new BelieveNode(((IntentionNode) formula).as_agent(), (new NotNode(((IntentionNode) formula).as_formula())).getSimplifiedFormula()));
//        }
        // tests if (B ??A (exists ??X ??PSI)) is subsumed by (exists ??Y (B ??A
        // ??PHI))
        else if (((BelieveNode) node).as_formula() instanceof ExistsNode && formula instanceof ExistsNode
                && ((ExistsNode) formula).as_formula() instanceof BelieveNode) {
            return ((BelieveNode) node).as_agent().equals(
                    ((BelieveNode) ((ExistsNode) formula).as_formula()).as_agent())
                    && ((ExistsNode) ((BelieveNode) node).as_formula()).as_formula().isSubsumedBy(
                            ((BelieveNode) ((ExistsNode) formula).as_formula()).as_formula());
        }
        return false;
    }

    @Override
	public Formula getDoubleMirror(Formula node, Term i, Term j, boolean default_result_is_true) {
        if (((BelieveNode) node).as_agent().equals(j) && ((BelieveNode) node).as_formula().isMentalAttitude(i)) {
            return ((BelieveNode) node).as_formula();
        }
        else {
            return super.getDoubleMirror(node, i, j, default_result_is_true);
        }
    }
}
