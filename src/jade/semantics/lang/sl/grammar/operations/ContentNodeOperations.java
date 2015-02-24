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

import jade.semantics.lang.sl.grammar.ActionContentExpressionNode;
import jade.semantics.lang.sl.grammar.ActionExpression;
import jade.semantics.lang.sl.grammar.Content;
import jade.semantics.lang.sl.grammar.ContentExpression;
import jade.semantics.lang.sl.grammar.ContentNode;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.FormulaContentExpressionNode;
import jade.semantics.lang.sl.grammar.IdentifyingContentExpressionNode;
import jade.semantics.lang.sl.grammar.IdentifyingExpression;
import jade.semantics.lang.sl.grammar.ListOfContentExpression;
import jade.semantics.lang.sl.grammar.MetaContentExpressionReferenceNode;
import jade.semantics.lang.sl.grammar.Node;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.grammar.Content.Operations;
import jade.semantics.lang.sl.parser.SLUnparser;
import jade.semantics.lang.sl.tools.SL;

public class ContentNodeOperations 
	extends TermNodeOperations
	implements Operations {

	@Override
	public void simplify(Term node)
	{
		if ( ((ContentNode)node).as_expressions().size() == 0 ) {
			super.simplify(node);
		}
		else {
			ContentNode original = (ContentNode)node;
			ContentNode simplified = new ContentNode(new ListOfContentExpression());
			for (int i=0; i<original.as_expressions().size(); i++) {
				ContentExpression exp = original.as_expressions().element(i);
				if (exp instanceof FormulaContentExpressionNode) {
					simplified.as_expressions().add(new FormulaContentExpressionNode(
							((FormulaContentExpressionNode)exp).as_formula().sm_simplified_formula()));
				}
				else if (exp instanceof IdentifyingContentExpressionNode){
					simplified.as_expressions().add(new IdentifyingContentExpressionNode(
							(IdentifyingExpression)((IdentifyingContentExpressionNode)exp).as_identifying_expression().sm_simplified_term()));
				}
				else if (exp instanceof ActionContentExpressionNode){
					simplified.as_expressions().add(new ActionContentExpressionNode(
							(ActionExpression)((ActionContentExpressionNode)exp).as_action_expression().sm_simplified_term()));
				}
				else if (exp instanceof MetaContentExpressionReferenceNode){
					simplified.as_expressions().add(exp);
				}
			}
			node.sm_simplified_term(simplified);
			simplified.sm_simplified_term(simplified);
		}
	}

	public String toSLString(Content node) {
		String result = null;
		try {
			java.io.StringWriter writer = new java.io.StringWriter();
			(new SLUnparser(writer)).unparseTrueSL(node);
			result = writer.toString();
		}
		catch (Exception e) {}
		return result;
	}
	
    public Node getContentElement(Content node, int i) {
        return node.as_expressions().element(i).getElement();		
	}
	
    public void setContentElement(Content node, int i, Node element) {
        if (element instanceof ActionExpression) {
			node.as_expressions().replace(i, new ActionContentExpressionNode((ActionExpression)element));
        }
        else if (element instanceof Formula) {
			node.as_expressions().replace(i, new FormulaContentExpressionNode((Formula)element));            
        }
        else if (element instanceof IdentifyingExpression) {
			node.as_expressions().replace(i, new IdentifyingContentExpressionNode((IdentifyingExpression)element));            
        }		
	}
	
    public void addContentElement(Content node, Node element) {
		if ( node.as_expressions() == null ) {
			node.as_expressions(new ListOfContentExpression());			
		}
        if (element instanceof ActionExpression) {
			node.as_expressions().append(new ActionContentExpressionNode((ActionExpression)element));
        }
        else if (element instanceof Formula) {
			node.as_expressions().append(new FormulaContentExpressionNode((Formula)element));            
        }
        else if (element instanceof IdentifyingExpression) {
			node.as_expressions().append(new IdentifyingContentExpressionNode((IdentifyingExpression)element));            
        }		
	}
	
    public void setContentElements(Content node, int number){
		if ( node.as_expressions() != null ) {
			node.as_expressions().removeAll();
		}
		else {
			node.as_expressions(new ListOfContentExpression());
		}
        for (int i = 0 ; i < number ; i++) {
			node.as_expressions().add(null); // prepare a dummy content element
        }		
    }
	
	public int contentElementNumber(Content node) {
		return node.as_expressions().size();
	}
	
	public jade.semantics.lang.sl.tools.MatchResult match(Content node, Node expression)
	{
		return SL.match(node, expression);
	}
	
	@Override
	public Term instantiate(Term node, String varname, Node expression)
	{
		return (Term)SL.instantiate(node, varname, expression);
	}
}
