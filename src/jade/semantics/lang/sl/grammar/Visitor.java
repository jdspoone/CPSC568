
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


//-----------------------------------------------------
// This file has been automatically produced by a tool.
//-----------------------------------------------------

package jade.semantics.lang.sl.grammar;

/**
This interface defines all the operations that have to be performed by a particular 
visitor, when visiting a particular abstract syntax tree. It is implemented by the 
<b>VisitorBase</b> class.
*/
public interface Visitor {

    public abstract void visitListOfContentExpression(ListOfContentExpression node);
    public abstract void visitListOfFormula(ListOfFormula node);
    public abstract void visitListOfTerm(ListOfTerm node);
    public abstract void visitListOfVariable(ListOfVariable node);
    public abstract void visitListOfParameter(ListOfParameter node);
    public abstract void visitContentNode(ContentNode node);
    public abstract void visitActionContentExpressionNode(ActionContentExpressionNode node);
    public abstract void visitIdentifyingContentExpressionNode(IdentifyingContentExpressionNode node);
    public abstract void visitFormulaContentExpressionNode(FormulaContentExpressionNode node);
    public abstract void visitMetaContentExpressionReferenceNode(MetaContentExpressionReferenceNode node);
    public abstract void visitMetaFormulaReferenceNode(MetaFormulaReferenceNode node);
    public abstract void visitNotNode(NotNode node);
    public abstract void visitPropositionSymbolNode(PropositionSymbolNode node);
    public abstract void visitResultNode(ResultNode node);
    public abstract void visitPredicateNode(PredicateNode node);
    public abstract void visitTrueNode(TrueNode node);
    public abstract void visitFalseNode(FalseNode node);
    public abstract void visitEqualsNode(EqualsNode node);
    public abstract void visitObligationNode(ObligationNode node);
    public abstract void visitCountAsNode(CountAsNode node);
    public abstract void visitInstitutionalFactNode(InstitutionalFactNode node);
    public abstract void visitBelieveNode(BelieveNode node);
    public abstract void visitUncertaintyNode(UncertaintyNode node);
    public abstract void visitIntentionNode(IntentionNode node);
    public abstract void visitPersistentGoalNode(PersistentGoalNode node);
    public abstract void visitDoneNode(DoneNode node);
    public abstract void visitFeasibleNode(FeasibleNode node);
    public abstract void visitExistsNode(ExistsNode node);
    public abstract void visitForallNode(ForallNode node);
    public abstract void visitImpliesNode(ImpliesNode node);
    public abstract void visitEquivNode(EquivNode node);
    public abstract void visitOrNode(OrNode node);
    public abstract void visitAndNode(AndNode node);
    public abstract void visitMetaTermReferenceNode(MetaTermReferenceNode node);
    public abstract void visitFactNode(FactNode node);
    public abstract void visitAnyNode(AnyNode node);
    public abstract void visitIotaNode(IotaNode node);
    public abstract void visitAllNode(AllNode node);
    public abstract void visitSomeNode(SomeNode node);
    public abstract void visitVariableNode(VariableNode node);
    public abstract void visitMetaVariableReferenceNode(MetaVariableReferenceNode node);
    public abstract void visitRealConstantNode(RealConstantNode node);
    public abstract void visitDateTimeConstantNode(DateTimeConstantNode node);
    public abstract void visitStringConstantNode(StringConstantNode node);
    public abstract void visitWordConstantNode(WordConstantNode node);
    public abstract void visitByteConstantNode(ByteConstantNode node);
    public abstract void visitIntegerConstantNode(IntegerConstantNode node);
    public abstract void visitRelativeTimeConstantNode(RelativeTimeConstantNode node);
    public abstract void visitTermSetNode(TermSetNode node);
    public abstract void visitTermSequenceNode(TermSequenceNode node);
    public abstract void visitActionExpressionNode(ActionExpressionNode node);
    public abstract void visitAlternativeActionExpressionNode(AlternativeActionExpressionNode node);
    public abstract void visitSequenceActionExpressionNode(SequenceActionExpressionNode node);
    public abstract void visitFunctionalTermNode(FunctionalTermNode node);
    public abstract void visitFunctionalTermParamNode(FunctionalTermParamNode node);
    public abstract void visitParameterNode(ParameterNode node);
    public abstract void visitSymbolNode(SymbolNode node);
    public abstract void visitMetaSymbolReferenceNode(MetaSymbolReferenceNode node);

};
