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

import jade.semantics.lang.sl.grammar.AndNode;
import jade.semantics.lang.sl.grammar.FalseNode;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.ObligationNode;
import jade.semantics.lang.sl.grammar.TrueNode;

public class ObligationNodeOperation extends FormulaNodeOperations {

	@Override
	public void simplify(Formula node)
	{	
		Formula fact = ((ObligationNode) node).as_formula().sm_simplified_formula();
		if (fact instanceof TrueNode || fact instanceof FalseNode) {
			node.sm_simplified_formula(fact);
		}
		// normal operator : O(p and q) <-> (Op and Oq)
		else if (fact instanceof AndNode) {
			Formula formulaLeft = ((AndNode) fact).as_left_formula().sm_simplified_formula();
			Formula formulaRight = ((AndNode) fact).as_right_formula().sm_simplified_formula();
			node.sm_simplified_formula((new AndNode(new ObligationNode(formulaLeft), 
													new ObligationNode(formulaRight))
										).getSimplifiedFormula());
		}
		// TODO : ForallNode, see BelieveNodeOperations (why is it into comments?)
//		else if (fact.isInstitutionalFact(institution)) {
//			node.sm_simplified_formula(fact);
//		}
		else {
			ObligationNode obligationNode = new ObligationNode(fact);
			obligationNode.sm_simplified_formula(obligationNode);
			node.sm_simplified_formula(obligationNode);
		}//end if
	}// end simplify
	
	
	// FIXME
	@Override
	public boolean isSubsumedBy(Formula node, Formula formula) {
		if (super.isSubsumedBy(node, formula)) {
			return true;
		}
		else if (formula instanceof ObligationNode) {
			return ((ObligationNode) node).as_formula().isSubsumedBy(((ObligationNode) formula).as_formula());
		}
		// TODO existsNode, see BelieveNodeOperations.isSubsumedBy()
		return false;
	}
	
	
}
