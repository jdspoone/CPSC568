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
 * Created by Carole Adam - 19 November 2007
 */


package jade.semantics.lang.sl.grammar.operations;

import jade.semantics.lang.sl.grammar.AndNode;
import jade.semantics.lang.sl.grammar.FalseNode;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.InstitutionalFactNode;
import jade.semantics.lang.sl.grammar.MetaTermReferenceNode;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.grammar.TrueNode;

public class InstitutionalFactNodeOperations 
extends FormulaNodeOperations {

	@Override
	public void simplify(Formula node)
	{	
		Term institution = ((InstitutionalFactNode) node).as_institution().sm_simplified_term();
		Formula fact = ((InstitutionalFactNode) node).as_fact().sm_simplified_formula();
		if (fact instanceof TrueNode || fact instanceof FalseNode) {
			node.sm_simplified_formula(fact);
		}
//		else if (fact instanceof CountAsNode) {
//			// axiom (DP) NON : reciproque de DP ... FAUX
//			node.sm_simplified_formula(fact);
//		}
		else if (fact instanceof AndNode) {
			// axiome C pour D normal + r�ciproque par RM : D(p and q) <-> (D p and D q)
			Formula formulaLeft = ((AndNode) fact).as_left_formula().sm_simplified_formula();
			Formula formulaRight = ((AndNode) fact).as_right_formula().sm_simplified_formula();
			node.sm_simplified_formula((new AndNode(new InstitutionalFactNode(institution, formulaLeft), 
													new InstitutionalFactNode(institution, formulaRight))
			).getSimplifiedFormula());
		}
//		else if (fact instanceof ImpliesNode) {
//			// axiom (SD)
//			Formula leftFormula = ((ImpliesNode) fact).as_left_formula();
//			Formula rightFormula = ((ImpliesNode) fact).as_right_formula();
//			node.sm_simplified_formula(new CountAsNode(institution,leftFormula,rightFormula));
//		}
		// TODO : ForallNode, see BelieveNodeOperations (why is it into comments?)
		
		// axiom DD, r�ciproque vraie seulement si (D p countas p) : vrai ? FIXME
//		else if (fact.isInstitutionalFact(institution)) {
//			node.sm_simplified_formula(fact);
//		}
		else {
			InstitutionalFactNode institutionalFactNode = new InstitutionalFactNode(institution, fact);
			institutionalFactNode.sm_simplified_formula(institutionalFactNode);
			node.sm_simplified_formula(institutionalFactNode);
		}//end if
	}// end simplify

	@Override
	public boolean isInstitutionalFact(Formula node, Term term) {
    	Term institution = term;
    	if (term instanceof MetaTermReferenceNode) {
    		institution = ((MetaTermReferenceNode)term).sm_value();
    		if (institution == null) {
    			((MetaTermReferenceNode)term).sm_value(((InstitutionalFactNode) node).as_institution());
    			return true;
    		}
    	}
        return ((InstitutionalFactNode) node).as_institution().equals(institution);
    }
	
	@Override
	public Formula isInstitutionalFactFrom(Formula node, Term institution) {
		if (institution instanceof MetaTermReferenceNode) {
			((MetaTermReferenceNode)institution).sm_value(((InstitutionalFactNode) node).as_institution());
			return ((InstitutionalFactNode) node).as_fact();
		} else if (((InstitutionalFactNode) node).as_institution().equals(institution)) {
			return ((InstitutionalFactNode) node).as_fact();
		}
		return null;
	}


//	public boolean isSubsumedBy(Formula node, Formula formula) {
//		if (super.isSubsumedBy(node, formula)) {
//			return true;
//		}
//		else if (formula instanceof InstitutionalFactNode) {
//			return ((InstitutionalFactNode) node).as_institution().equals(((InstitutionalFactNode) formula).as_institution())
//			&& ((InstitutionalFactNode) node).as_fact().isSubsumedBy(((InstitutionalFactNode) formula).as_fact());
//		}
//		// TODO existsNode, see BelieveNodeOperations.isSubsumedBy()
//		return false;
//	}

}

