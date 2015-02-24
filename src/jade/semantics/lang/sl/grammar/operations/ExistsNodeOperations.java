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

import jade.semantics.lang.sl.grammar.BelieveNode;
import jade.semantics.lang.sl.grammar.DoneNode;
import jade.semantics.lang.sl.grammar.ExistsNode;
import jade.semantics.lang.sl.grammar.FeasibleNode;
import jade.semantics.lang.sl.grammar.ForallNode;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.MetaFormulaReferenceNode;
import jade.semantics.lang.sl.grammar.MetaVariableReferenceNode;
import jade.semantics.lang.sl.grammar.NotNode;
import jade.semantics.lang.sl.grammar.OrNode;
import jade.semantics.lang.sl.grammar.QuantifiedFormula;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.grammar.Variable;
import jade.semantics.lang.sl.tools.SL;

public class ExistsNodeOperations extends FormulaNodeOperations {
	
	@Override
	public void simplify(Formula node) {	
        Variable x = ((ExistsNode) node).as_variable();
        Formula formula = ((ExistsNode) node).as_formula().sm_simplified_formula();
        if (formula instanceof OrNode) {
            node.sm_simplified_formula((new OrNode(new ExistsNode(x, ((OrNode) formula).as_left_formula()), 
					                               new ExistsNode(x, ((OrNode) formula).as_right_formula())))
					                               .getSimplifiedFormula());
        }
        else if (!formula.isAFreeVariable(x)) {
			node.sm_simplified_formula(formula);
        }
        else if (formula instanceof NotNode && ((NotNode)formula).as_formula() instanceof BelieveNode) {
        	Formula beliefFormula = ((BelieveNode)((NotNode)formula).as_formula()).as_formula();
        	Term beliefAgent = ((BelieveNode)((NotNode)formula).as_formula()).as_agent();
        	node.sm_simplified_formula((new NotNode(new BelieveNode(beliefAgent,
        															new ForallNode(x,
        																		   beliefFormula)))).getSimplifiedFormula());
        }
        else if (formula instanceof DoneNode && ((DoneNode)formula).as_formula().isAFreeVariable(x)) {
			node.sm_simplified_formula((new DoneNode(((DoneNode) formula).as_action(), 
												     new ExistsNode(x, ((DoneNode) formula).as_formula())))
												     .getSimplifiedFormula());
        }
        else if (formula instanceof FeasibleNode && ((FeasibleNode)formula).as_formula().isAFreeVariable(x)) {
			node.sm_simplified_formula((new FeasibleNode(((FeasibleNode) formula).as_action(), 
					                                     new ExistsNode(x,((FeasibleNode) formula).as_formula())))
					                                     .getSimplifiedFormula());
        }
        else {
			ExistsNode existsNode = new ExistsNode(x, formula);
			existsNode.sm_simplified_formula(existsNode);
			node.sm_simplified_formula(existsNode);
        }
    }

    @Override
	public boolean isMentalAttitude(Formula node, Term term) {
        return ((ExistsNode) node).as_formula().isMentalAttitude(term);
    }

    @Override
	public boolean isInstitutionalFact(Formula node, Term term) {
        return ((ExistsNode) node).as_formula().isInstitutionalFact(term);
    }
    
    @Override
	public Formula isExistsOn(Formula node, Term variable) {
        if (variable instanceof MetaVariableReferenceNode) {
            ((MetaVariableReferenceNode)variable).sm_value(((ExistsNode) node).as_variable());
            return ((ExistsNode) node).as_formula();
        } else if (((ExistsNode) node).as_variable().equals(variable)) {
            return ((ExistsNode) node).as_formula();
        }
        return null;
    }
    
    @Override
	public boolean isSubsumedBy(Formula node, Formula formula) {
        if (super.isSubsumedBy(node, formula)) {
            return true;
        }
        // tests if (exists ??X ??PHI) is subsumed by (exists ??Y ??PSI)
        else if (formula instanceof QuantifiedFormula) {
            QuantifiedFormula n = (QuantifiedFormula) node;
            QuantifiedFormula f = (QuantifiedFormula) formula;
            QuantifiedFormula f2 = (QuantifiedFormula) f.getVariablesSubstitution(f.as_variable(), n.as_variable());
            return n.as_formula().isSubsumedBy(f2.as_formula());
        }
        // tests if (exists ??X ??PHI) is subsumed by PSI[??X/o]
//        else if (!formula.find(SL.META_REFERENCE_CLASSES, MetaTermReferenceNode.lx_name_ID, null, new ListOfNodes(), false)) {
        else if (!(formula instanceof MetaFormulaReferenceNode)) {
            Formula f = (Formula) SL.toPattern(((ExistsNode) node).as_formula(), ((ExistsNode) node)
                    .as_variable());
            return SL.match(f, formula) != null;
        }
        else {
        	return false;
        }
    }

    @Override
	public Formula getDoubleMirror(Formula node, Term i, Term j, boolean default_result_is_true) {
        return (new ExistsNode(((ExistsNode) node).as_variable(), ((ExistsNode) node).as_formula().getDoubleMirror(i,
                j, default_result_is_true))).getSimplifiedFormula();
    }
    

}
