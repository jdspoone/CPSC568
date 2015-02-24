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
import jade.semantics.lang.sl.grammar.ForallNode;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.IntentionNode;
import jade.semantics.lang.sl.grammar.NotNode;
import jade.semantics.lang.sl.grammar.OrNode;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.grammar.TrueNode;
import jade.semantics.lang.sl.grammar.UncertaintyNode;
import jade.semantics.lang.sl.tools.SL;

public class NotNodeOperations extends FormulaNodeOperations {

	@Override
	public void simplify(Formula node) {
		Formula formula = ((NotNode) node).as_formula().sm_simplified_formula();
		if (formula instanceof TrueNode) {
			node.sm_simplified_formula(new FalseNode().getSimplifiedFormula());
		}
		else if (formula instanceof FalseNode) {
			node.sm_simplified_formula(SL.TRUE.getSimplifiedFormula());
		}
		else if (formula instanceof NotNode) {
			node.sm_simplified_formula(((NotNode) formula).as_formula().sm_simplified_formula());
		}
		else if (formula instanceof AndNode) {
			Formula formulaLeft = ((AndNode) formula).as_left_formula().sm_simplified_formula();
			Formula formulaRight = ((AndNode) formula).as_right_formula().sm_simplified_formula();
			node.sm_simplified_formula((new OrNode(new NotNode(formulaLeft), new NotNode(formulaRight))).getSimplifiedFormula());
		}
		else if (formula instanceof OrNode) {
			Formula formulaLeft = ((OrNode) formula).as_left_formula().sm_simplified_formula();
			Formula formulaRight = ((OrNode) formula).as_right_formula().sm_simplified_formula();
			node.sm_simplified_formula((new AndNode(new NotNode(formulaLeft), new NotNode(formulaRight))).getSimplifiedFormula());
		}
		else if (formula instanceof ForallNode) {			
			node.sm_simplified_formula((new ExistsNode(((ForallNode) formula).as_variable(), new NotNode(((ForallNode) formula)
					.as_formula().sm_simplified_formula()))).getSimplifiedFormula());
		}
		else if (formula instanceof ExistsNode) {
			node.sm_simplified_formula((new ForallNode(((ExistsNode) formula).as_variable(), new NotNode(((ExistsNode) formula)
					.as_formula().sm_simplified_formula()))).getSimplifiedFormula());
		}
		else {
			NotNode notNode = new NotNode(formula);
			notNode.sm_simplified_formula(notNode);
			node.sm_simplified_formula(notNode);
		}
	}

	@Override
	public boolean isMentalAttitude(Formula node, Term term) {
		return ((NotNode) node).as_formula().isMentalAttitude(term);
	}

	@Override
	public boolean isInstitutionalFact(Formula node, Term term) {
		return ((NotNode) node).as_formula().isInstitutionalFact(term);
	}
	
	@Override
	public boolean isSubsumedBy(Formula node, Formula formula) {
		if (super.isSubsumedBy(node, formula)) {
			return true;
		}
		else if (formula instanceof NotNode) {
			return ((NotNode) formula).as_formula().isSubsumedBy(((NotNode) node).as_formula());
		}
		else if (((NotNode) node).as_formula() instanceof BelieveNode) {
			BelieveNode b_node = (BelieveNode) ((NotNode) node).as_formula();
			if (formula instanceof BelieveNode) {
				BelieveNode b_form = (BelieveNode) formula;
				return b_node.as_agent().equals(b_form.as_agent())
				&& (new NotNode(b_node.as_formula())).getSimplifiedFormula().isSubsumedBy(b_form.as_formula());
			}
			// else if (formula instanceof IntentionNode) {
				// IntentionNode i_form = (IntentionNode) formula;
				// return b_node.as_agent().equals(i_form.as_agent())
				// && i_form.as_formula().isSubsumedBy(b_node.as_formula());
				// }
			else if (formula instanceof UncertaintyNode) {
				UncertaintyNode u_form = (UncertaintyNode) formula;
				return b_node.as_agent().equals(u_form.as_agent())
				&& (u_form.as_formula().isSubsumedBy(b_node.as_formula()) || new NotNode(b_node.as_formula())
				.getSimplifiedFormula().isSubsumedBy(u_form.as_formula()));
			}
		}
		// TODO : case of InstitutionalFactNode ???
		else if (((NotNode) node).as_formula() instanceof IntentionNode) {
			IntentionNode i_node = (IntentionNode) ((NotNode) node).as_formula();
			return new NotNode(new BelieveNode(i_node.as_agent(), new NotNode(i_node.as_formula())))
			.getSimplifiedFormula().isSubsumedBy(formula);
		}
		else if (((NotNode) node).as_formula() instanceof UncertaintyNode) {
			UncertaintyNode u_node = (UncertaintyNode) ((NotNode) node).as_formula();
			if (formula instanceof BelieveNode) {
				BelieveNode b_form = (BelieveNode) formula;
				return u_node.as_agent().equals(b_form.as_agent())
				&& (u_node.as_formula().isSubsumedBy(b_form.as_formula()) || new NotNode(u_node.as_formula())
				.getSimplifiedFormula().isSubsumedBy(b_form.as_formula()));
			}
			// else if (formula instanceof IntentionNode) {
				// IntentionNode i_form = (IntentionNode) formula;
				// return u_node.as_agent().equals(i_form.as_agent())
			// && ((u_node.as_formula().isSubsumedBy(new
			// NotNode(i_form.as_formula()).getSimplifiedFormula()))
			// || u_node.as_formula().isSubsumedBy(u_node.as_formula()));
			// }
			else if (formula instanceof UncertaintyNode) {
				UncertaintyNode u_form = (UncertaintyNode) formula;
				return u_node.as_agent().equals(u_form.as_agent())
				&& new NotNode(u_node.as_formula()).getSimplifiedFormula().isSubsumedBy(u_form.as_formula());
			}
		}
		// return (formula instanceof NotNode ?
		// ((NotNode) formula).as_formula() :
		// new NotNode(formula)).isSubsumedBy(((NotNode) node).as_formula());
		return false;
	}

	@Override
	public Formula getDoubleMirror(Formula node, Term i, Term j, boolean default_result_is_true) {
		return (new NotNode(((NotNode) node).as_formula().getDoubleMirror(i, j, !default_result_is_true)))
		.getSimplifiedFormula();
	}
}
