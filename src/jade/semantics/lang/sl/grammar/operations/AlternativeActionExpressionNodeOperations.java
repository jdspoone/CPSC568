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

import jade.semantics.lang.sl.grammar.AlternativeActionExpressionNode;
import jade.semantics.lang.sl.grammar.Term;

 
public class AlternativeActionExpressionNodeOperations
    extends ActionExpressionNodeOperations
{
	@Override
	public void simplify(Term node)
	{
		AlternativeActionExpressionNode action = (AlternativeActionExpressionNode)node;
		Term left = action.as_left_action().sm_simplified_term();
		Term right = action.as_right_action().sm_simplified_term();
		
        if (left instanceof AlternativeActionExpressionNode) {
			Term leftLeft = ((AlternativeActionExpressionNode) left).as_left_action().sm_simplified_term();
			Term leftRight = ((AlternativeActionExpressionNode) left).as_right_action().sm_simplified_term();
			node.sm_simplified_term(new AlternativeActionExpressionNode(leftLeft, 
							        new AlternativeActionExpressionNode(leftRight, right)).getSimplifiedTerm());
        }
        else {
			node.sm_simplified_term(orderAlternativeLeaves(left, right));
        }
    }

    private AlternativeActionExpressionNode orderAlternativeLeaves(Term left, Term right) {
        // left is supposed to be a leaf (not a AlternativeActionExpressionNode)
        // right is supposed to be either a leaf (terminal case) or an already
        // ordered AlternativeActionExpressionNode
		AlternativeActionExpressionNode altNode;
		if (right instanceof AlternativeActionExpressionNode) {
			Term middle = ((AlternativeActionExpressionNode) right).as_left_action();
			if (left.compare(middle) <= 0) {
				altNode = new AlternativeActionExpressionNode(left, right);
				altNode.sm_simplified_term(altNode);
			}
			else {
				altNode = (AlternativeActionExpressionNode)new AlternativeActionExpressionNode(middle, 
						orderAlternativeLeaves(left, 
										       ((AlternativeActionExpressionNode) right).as_right_action())).getSimplifiedTerm();
			}
		}
		else if (left.compare(right) <= 0) {
			altNode =  new AlternativeActionExpressionNode(left, right);
			altNode.sm_simplified_term(altNode);			
		}
		else {
			altNode =  new AlternativeActionExpressionNode(right, left);
			altNode.sm_simplified_term(altNode);
		}
		return altNode;
    }
 }
