
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
This class implements the <b>Visitor</b> interface.
It can be derived to implement a particular visitor for this abstract syntax.
*/
public class VisitorBase implements Visitor {

    public void visitListOfContentExpression(ListOfContentExpression node) {
        node.childrenAccept(this);
    }
    public void visitListOfFormula(ListOfFormula node) {
        node.childrenAccept(this);
    }
    public void visitListOfTerm(ListOfTerm node) {
        node.childrenAccept(this);
    }
    public void visitListOfVariable(ListOfVariable node) {
        node.childrenAccept(this);
    }
    public void visitListOfParameter(ListOfParameter node) {
        node.childrenAccept(this);
    }
    public void visitContentNode(ContentNode node) {}
    public void visitActionContentExpressionNode(ActionContentExpressionNode node) {}
    public void visitIdentifyingContentExpressionNode(IdentifyingContentExpressionNode node) {}
    public void visitFormulaContentExpressionNode(FormulaContentExpressionNode node) {}
    public void visitMetaContentExpressionReferenceNode(MetaContentExpressionReferenceNode node) {}
    public void visitMetaFormulaReferenceNode(MetaFormulaReferenceNode node) {}
    public void visitNotNode(NotNode node) {}
    public void visitPropositionSymbolNode(PropositionSymbolNode node) {}
    public void visitResultNode(ResultNode node) {}
    public void visitPredicateNode(PredicateNode node) {}
    public void visitTrueNode(TrueNode node) {}
    public void visitFalseNode(FalseNode node) {}
    public void visitEqualsNode(EqualsNode node) {}
    public void visitObligationNode(ObligationNode node) {}
    public void visitCountAsNode(CountAsNode node) {}
    public void visitInstitutionalFactNode(InstitutionalFactNode node) {}
    public void visitBelieveNode(BelieveNode node) {}
    public void visitUncertaintyNode(UncertaintyNode node) {}
    public void visitIntentionNode(IntentionNode node) {}
    public void visitPersistentGoalNode(PersistentGoalNode node) {}
    public void visitDoneNode(DoneNode node) {}
    public void visitFeasibleNode(FeasibleNode node) {}
    public void visitExistsNode(ExistsNode node) {}
    public void visitForallNode(ForallNode node) {}
    public void visitImpliesNode(ImpliesNode node) {}
    public void visitEquivNode(EquivNode node) {}
    public void visitOrNode(OrNode node) {}
    public void visitAndNode(AndNode node) {}
    public void visitMetaTermReferenceNode(MetaTermReferenceNode node) {}
    public void visitFactNode(FactNode node) {}
    public void visitAnyNode(AnyNode node) {}
    public void visitIotaNode(IotaNode node) {}
    public void visitAllNode(AllNode node) {}
    public void visitSomeNode(SomeNode node) {}
    public void visitVariableNode(VariableNode node) {}
    public void visitMetaVariableReferenceNode(MetaVariableReferenceNode node) {}
    public void visitRealConstantNode(RealConstantNode node) {}
    public void visitDateTimeConstantNode(DateTimeConstantNode node) {}
    public void visitStringConstantNode(StringConstantNode node) {}
    public void visitWordConstantNode(WordConstantNode node) {}
    public void visitByteConstantNode(ByteConstantNode node) {}
    public void visitIntegerConstantNode(IntegerConstantNode node) {}
    public void visitRelativeTimeConstantNode(RelativeTimeConstantNode node) {}
    public void visitTermSetNode(TermSetNode node) {}
    public void visitTermSequenceNode(TermSequenceNode node) {}
    public void visitActionExpressionNode(ActionExpressionNode node) {}
    public void visitAlternativeActionExpressionNode(AlternativeActionExpressionNode node) {}
    public void visitSequenceActionExpressionNode(SequenceActionExpressionNode node) {}
    public void visitFunctionalTermNode(FunctionalTermNode node) {}
    public void visitFunctionalTermParamNode(FunctionalTermParamNode node) {}
    public void visitParameterNode(ParameterNode node) {}
    public void visitSymbolNode(SymbolNode node) {}
    public void visitMetaSymbolReferenceNode(MetaSymbolReferenceNode node) {}

};
