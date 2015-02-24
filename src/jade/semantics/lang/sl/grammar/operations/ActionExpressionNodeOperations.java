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

import jade.semantics.lang.sl.grammar.ActionExpression;
import jade.semantics.lang.sl.grammar.ActionExpressionNode;
import jade.semantics.lang.sl.grammar.AlternativeActionExpressionNode;
import jade.semantics.lang.sl.grammar.ListOfTerm;
import jade.semantics.lang.sl.grammar.SequenceActionExpressionNode;
import jade.semantics.lang.sl.grammar.Term;


public class ActionExpressionNodeOperations
extends TermNodeOperations
implements ActionExpression.Operations 
{
	@Override
	public void simplify(Term node)
	{
		ActionExpressionNode action = (ActionExpressionNode)node;
		Term agent = action.as_agent().sm_simplified_term();
		Term term = action.as_term().sm_simplified_term();
		ActionExpressionNode simplified = new ActionExpressionNode(agent, term);
		simplified.sm_simplified_term(simplified);
		node.sm_simplified_term(simplified);
	}
	
	public ActionExpression getFirstStep(ActionExpression node) {
		if (node instanceof ActionExpressionNode) {
			return node;
		}
		else if (node instanceof AlternativeActionExpressionNode) {
			return new AlternativeActionExpressionNode(
					((ActionExpression)((AlternativeActionExpressionNode)node).as_left_action()).getFirstStep(),
					((ActionExpression)((AlternativeActionExpressionNode)node).as_right_action()).getFirstStep());
		}
		else if (node instanceof SequenceActionExpressionNode) {
			return ((ActionExpression)((SequenceActionExpressionNode)node).as_left_action()).getFirstStep();
		}
		else {
			return null;
		}
	}
	
	private void addAgents2Result(ListOfTerm result, ListOfTerm agents)
	{
		for (int i=0; i<agents.size(); i++) {
			if ( !result.contains(agents.element(i)) ) {
				result.add(agents.element(i));
			}
		}
	}
	
	public ListOfTerm getActors(ActionExpression node)
	{
		ListOfTerm result = new ListOfTerm();
		if ( node instanceof ActionExpressionNode ) {
			result.add(((ActionExpressionNode)node).as_agent());
		}
		else if ( node instanceof AlternativeActionExpressionNode ) {
			addAgents2Result(result,((ActionExpression)((AlternativeActionExpressionNode)node).as_left_action()).getActors());
			addAgents2Result(result,((ActionExpression)((AlternativeActionExpressionNode)node).as_right_action()).getActors());
		}
		else if ( node instanceof SequenceActionExpressionNode ) {
			addAgents2Result(result,((ActionExpression)((SequenceActionExpressionNode)node).as_left_action()).getActors());
			addAgents2Result(result,((ActionExpression)((SequenceActionExpressionNode)node).as_right_action()).getActors());
		}
		return result;
	}
	
	public Term getActor(ActionExpression node)
	{
//		return getActor(node, null);
		ListOfTerm firstStepActors = node.getFirstStep().getActors();
		if (firstStepActors.size() == 1) {
			return firstStepActors.first();
		}
		else {
			return null;
		}
	}
	
//	private Term getActor(Term node, Term actor) {
//		if (node instanceof ActionExpressionNode) {
//			if (actor == null) {
//				return ((ActionExpressionNode)node).as_agent();
//			}
//			else {
//				return (actor.equals(((ActionExpressionNode)node).as_agent()) ? actor : null);
//			}
//		}
//		else if (node instanceof AlternativeActionExpressionNode) {
//			Term result = getActor(((AlternativeActionExpressionNode)node).as_left_action(), actor);
//			return (result==null ?
//				null :
//				getActor(((AlternativeActionExpressionNode)node).as_right_action(), result));
//		}
//		else if (node instanceof SequenceActionExpressionNode) {
//			Term result = getActor(((SequenceActionExpressionNode)node).as_left_action(), actor);
//			return (result==null ?
//				null :
//				getActor(((SequenceActionExpressionNode)node).as_right_action(), result));
//		}
//		else {
//			return null;
//		}
//	}
}
