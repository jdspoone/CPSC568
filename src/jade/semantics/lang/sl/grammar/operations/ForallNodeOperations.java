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
import jade.semantics.lang.sl.grammar.DoneNode;
import jade.semantics.lang.sl.grammar.FeasibleNode;
import jade.semantics.lang.sl.grammar.ForallNode;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.InstitutionalFactNode;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.grammar.Variable;
 
public class ForallNodeOperations
    extends FormulaNodeOperations
{
	@Override
	public void simplify(Formula node) {
    
		Variable x = ((ForallNode)node).as_variable();
		Formula formula = ((ForallNode)node).as_formula().sm_simplified_formula();
		if ( formula instanceof AndNode ) {
			node.sm_simplified_formula((new AndNode(new ForallNode(x, ((AndNode)formula).as_left_formula()),
							new ForallNode(x, ((AndNode)formula).as_right_formula()))).getSimplifiedFormula());
		}
		else if ( !formula.isAFreeVariable(x) ) {
			node.sm_simplified_formula(formula);
		}
		else if ( formula instanceof BelieveNode ) {
			node.sm_simplified_formula((new BelieveNode(((BelieveNode)formula).as_agent(), 
					new ForallNode(x, ((BelieveNode)formula).as_formula()))).getSimplifiedFormula());
		}
		else if ( formula instanceof InstitutionalFactNode ) {
			node.sm_simplified_formula((new InstitutionalFactNode(((InstitutionalFactNode)formula).as_institution(), 
					new ForallNode(x, ((InstitutionalFactNode)formula).as_fact()))).getSimplifiedFormula());
		}
		else if ( formula instanceof DoneNode ) {
			node.sm_simplified_formula((new DoneNode(((DoneNode)formula).as_action(),
					new ForallNode(x, ((DoneNode)formula).as_formula()))).getSimplifiedFormula());
		}
		else if ( formula instanceof FeasibleNode ) {
			node.sm_simplified_formula((new FeasibleNode(((FeasibleNode)formula).as_action(),
					new ForallNode(x, ((FeasibleNode)formula).as_formula()))).getSimplifiedFormula());
		}
		else {
			ForallNode forallNode = new ForallNode(x, formula);
			forallNode.sm_simplified_formula(forallNode);
			node.sm_simplified_formula(forallNode);
		}
    }

    @Override
	public boolean isMentalAttitude(Formula node, Term term)
    {
	return ((ForallNode)node).as_formula().isMentalAttitude(term);
    }

    @Override
	public boolean isInstitutionalFact(Formula node, Term term)
    {
	return ((ForallNode)node).as_formula().isInstitutionalFact(term);
    }

    @Override
	public Formula isBeliefFrom(Formula node, Term agent) {
        Formula forallBelief = ((ForallNode) node).as_formula().isBeliefFrom(agent);
        if (forallBelief != null) {
            return new ForallNode(((ForallNode) node).as_variable(), forallBelief);
        }
        return null;
    }

    @Override
	public Formula isInstitutionalFactFrom(Formula node, Term agent) {
        Formula forallInstfact = ((ForallNode) node).as_formula().isInstitutionalFactFrom(agent);
        if (forallInstfact != null) {
            return new ForallNode(((ForallNode) node).as_variable(), forallInstfact);
        }
        return null;
    }
    
    @Override
	public Formula getDoubleMirror(Formula node, Term i, Term j, boolean default_result_is_true)
    {
	return (new ForallNode(((ForallNode)node).as_variable(), 
			       ((ForallNode)node).as_formula().getDoubleMirror(i,j,default_result_is_true))).getSimplifiedFormula();
    }

    @Override
	public boolean isSubsumedBy(Formula node, Formula formula)
    {
	// tests if (forall ??A ??PHI) is subsumed by (forall ??A ??PSI)
	if ( formula instanceof ForallNode ) {
	    ForallNode n = (ForallNode)node;
	    ForallNode f = (ForallNode)formula;
	    ForallNode f2 = (ForallNode)f.getVariablesSubstitution(f.as_variable(), n.as_variable());
	    return n.as_formula().isSubsumedBy(f2.as_formula());
	}
	else {
	    return super.isSubsumedBy(node, formula);
	}
    }
}
