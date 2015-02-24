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
 * Created by Carole Adam - 20 November 2007
 */

package jade.semantics.lang.sl.grammar.operations;

import jade.semantics.lang.sl.grammar.CountAsNode;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.Term;

public class CountAsNodeOperations extends FormulaNodeOperations {

	@Override
	public void simplify(Formula node)
	{	
		Term institution = ((CountAsNode) node).as_institution().sm_simplified_term();
		Formula left_fact = ((CountAsNode) node).as_left_formula().sm_simplified_formula();
		Formula right_fact = ((CountAsNode) node).as_right_formula().sm_simplified_formula();
		// TODO case of TrueNode and FalseNode ??
//		if (right_fact instanceof AndNode) {
//			// axiom (CC) : a l'envers ...
//			Formula formulaLeft = ((AndNode) right_fact).as_left_formula().sm_simplified_formula();
//			Formula formulaRight = ((AndNode) right_fact).as_right_formula().sm_simplified_formula();
//			node.sm_simplified_formula((new AndNode(new CountAsNode(institution, left_fact,formulaLeft), 
//					new CountAsNode(institution, left_fact, formulaRight))
//			).getSimplifiedFormula());
//		}
//		else if (left_fact instanceof OrNode) {
//			// axiom (CA) : a l'envers ... 
//			Formula formulaLeft = ((OrNode) left_fact).as_left_formula().sm_simplified_formula();
//			Formula formulaRight = ((OrNode) left_fact).as_right_formula().sm_simplified_formula();
//			node.sm_simplified_formula((new AndNode(new CountAsNode(institution, formulaLeft, right_fact), 
//													new CountAsNode(institution, formulaRight, right_fact))
//										).getSimplifiedFormula());
//		}
		// TODO : ForallNode, see BelieveNodeOperations (why is it into comments?)
//		else {
			CountAsNode countAsNode = new CountAsNode(institution, left_fact, right_fact);
			countAsNode.sm_simplified_formula(countAsNode);
			node.sm_simplified_formula(countAsNode);
//		}//end if
	}// end simplify
	
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
