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
* created on 12 mars 07 by Thierry Martinez
*/

package jade.semantics.lang.sl.grammar.operations;

import jade.semantics.lang.sl.grammar.ActionExpressionNode;
import jade.semantics.lang.sl.grammar.AlternativeActionExpressionNode;
import jade.semantics.lang.sl.grammar.AndNode;
import jade.semantics.lang.sl.grammar.BelieveNode;
import jade.semantics.lang.sl.grammar.ByteConstantNode;
import jade.semantics.lang.sl.grammar.ContentExpression;
import jade.semantics.lang.sl.grammar.ContentNode;
import jade.semantics.lang.sl.grammar.DateTimeConstantNode;
import jade.semantics.lang.sl.grammar.DoneNode;
import jade.semantics.lang.sl.grammar.EqualsNode;
import jade.semantics.lang.sl.grammar.EquivNode;
import jade.semantics.lang.sl.grammar.ExistsNode;
import jade.semantics.lang.sl.grammar.FeasibleNode;
import jade.semantics.lang.sl.grammar.ForallNode;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.FunctionalTermNode;
import jade.semantics.lang.sl.grammar.FunctionalTermParamNode;
import jade.semantics.lang.sl.grammar.IdentifyingExpression;
import jade.semantics.lang.sl.grammar.ImpliesNode;
import jade.semantics.lang.sl.grammar.IntegerConstant;
import jade.semantics.lang.sl.grammar.IntentionNode;
import jade.semantics.lang.sl.grammar.Node;
import jade.semantics.lang.sl.grammar.NotNode;
import jade.semantics.lang.sl.grammar.ObligationNode;
import jade.semantics.lang.sl.grammar.OrNode;
import jade.semantics.lang.sl.grammar.ParameterNode;
import jade.semantics.lang.sl.grammar.PersistentGoalNode;
import jade.semantics.lang.sl.grammar.PredicateNode;
import jade.semantics.lang.sl.grammar.RealConstantNode;
import jade.semantics.lang.sl.grammar.SequenceActionExpressionNode;
import jade.semantics.lang.sl.grammar.StringConstantNode;
import jade.semantics.lang.sl.grammar.SymbolNode;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.grammar.TermSequence;
import jade.semantics.lang.sl.grammar.TermSet;
import jade.semantics.lang.sl.grammar.TrueNode;
import jade.semantics.lang.sl.grammar.UncertaintyNode;
import jade.semantics.lang.sl.grammar.WordConstantNode;

public class SLSyntax {
	
	static public void installOperations() 
	{
		Node.installOperations(new Object[] {
				Node.ID,                          		new DefaultNodeOperations(),
				ContentNode.ID,                         new ContentNodeOperations(),
				ContentExpression.ID,          			new ContentExpressionNodeOperations(),
				Formula.ID,             				new FormulaNodeOperations(),
				Term.ID,                				new TermNodeOperations(),
				TermSequence.ID,          				new TermSequenceNodeOperations(),
				TermSet.ID,          					new TermSetNodeOperations(),
				BelieveNode.ID,                         new BelieveNodeOperations(),
				IntentionNode.ID,                       new IntentionNodeOperations(),
				UncertaintyNode.ID,                     new UncertaintyNodeOperations(),
				PersistentGoalNode.ID,                  new PersistentGoalNodeOperations(),
				FeasibleNode.ID,                        new FeasibleNodeOperations(),
				DoneNode.ID,                            new DoneNodeOperations(),
				EqualsNode.ID,                          new EqualsNodeOperations(),
				TrueNode.ID,                            new TrueNodeOperations(),
				ForallNode.ID,                          new ForallNodeOperations(),
				ExistsNode.ID,                          new ExistsNodeOperations(),
				NotNode.ID,                             new NotNodeOperations(),
				AndNode.ID,                             new AndNodeOperations(),
				OrNode.ID,                              new OrNodeOperations(),
				EquivNode.ID,                           new EquivNodeOperations(),
				ImpliesNode.ID,                         new ImpliesNodeOperations(),
				PredicateNode.ID,                       new PredicateNodeOperations(),
				ActionExpressionNode.ID,                new ActionExpressionNodeOperations(),
				AlternativeActionExpressionNode.ID, 	new AlternativeActionExpressionNodeOperations(),
				SequenceActionExpressionNode.ID, 		new SequenceActionExpressionNodeOperations(),
				FunctionalTermNode.ID,                  new FunctionalTermNodeOperations(),
				FunctionalTermParamNode.ID,             new FunctionalTermParamNodeOperations(),
				SymbolNode.ID,                          new SymbolNodeOperations(),
				IntegerConstant.ID,						new IntegerConstantOperations(),
				RealConstantNode.ID, 					new RealConstantNodeOperations(),
				StringConstantNode.ID,					new StringConstantNodeOperations(),
				WordConstantNode.ID,					new WordConstantNodeOperations(),
				ByteConstantNode.ID,					new ByteConstantNodeOperations(),
				DateTimeConstantNode.ID,				new DateTimeConstantNodeOperations(),
				ParameterNode.ID,	                    new ParameterNodeOperations(),
				IdentifyingExpression.ID,               new IdentifyingExpressionNodeOperations(),
				ObligationNode.ID,						new ObligationNodeOperation()
		});
	}
}

