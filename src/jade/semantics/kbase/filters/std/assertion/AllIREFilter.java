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
 * AllIREFilter.java
 * Created on 1 juil. 2005
 * Author : Vincent Pautret
 */
package jade.semantics.kbase.filters.std.assertion;

import jade.semantics.interpreter.Finder;
import jade.semantics.kbase.filters.KBAssertFilter;
import jade.semantics.lang.sl.grammar.AllNode;
import jade.semantics.lang.sl.grammar.BelieveNode;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.IdentifyingExpression;
import jade.semantics.lang.sl.grammar.IotaNode;
import jade.semantics.lang.sl.grammar.ListOfNodes;
import jade.semantics.lang.sl.grammar.ListOfTerm;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.grammar.TermSetNode;
import jade.semantics.lang.sl.grammar.VariableNode;
import jade.semantics.lang.sl.tools.MatchResult;
import jade.semantics.lang.sl.tools.SL;

/**
 * Filter for the identifying expression of the form <i>(= (all ??X ??formula) ??set)</i>
 * or <i>(= (iota ??X ??formula) ??set)</i>.
 * Asserts in the belief base each element which appears in the set for the 
 * first pattern and the single value for the second pattern.
 * @author Vincent Pautret - France Telecom
 * @version 0.9
 */
public class AllIREFilter extends KBAssertFilter {
    
    /**
     * Pattern used to assert formula in the belief base
     */
   // private Formula bPattern;
    /**
     * Pattern that must match to apply the filter
     */
    protected Formula generalPattern;
    
    protected Formula generalNotPattern;
    
    protected Formula formulaPattern;
    
    protected Formula closedPattern;
    
    protected Term termPattern;
    
    private ListOfNodes listOfNodes;
    
    private Finder finder;
    /**
     * Constructor of the filter. Instantiates the patterns.
     */
    public AllIREFilter() {
        generalPattern = SL.formula("(B ??agent (= ??ide ??phi))");
        generalNotPattern = SL.formula("(B ??agent (not (= ??ide ??phi)))");
     //   bPattern = SL.formula("(B ??agent ??formula)" ); 
        finder = new Finder() {
            @Override
			public boolean identify(Object object) {
                 if (object instanceof Formula) {
                     return SL.match(formulaPattern, (Formula)object) != null;
                 }
                 return false;
            }
        };
    } // End of AllIREFilter/0
    
    /**
     * If the filter is applicable, asserts in the belief base each element 
     * which appears in the set, and returns a <code>TrueNode</code>.
     * @param formula a formula to assert
     * @return TrueNode if the filter is applicable, the given formula in the 
     * other cases.
     */
    @Override
	public final Formula apply(Formula formula) {
        try {
            MatchResult applyResult = SL.match(generalPattern, formula);
            if (applyResult != null && applyResult.getTerm("ide") instanceof IdentifyingExpression && 
                    ((IdentifyingExpression)applyResult.getTerm("ide") instanceof AllNode ||
                     (IdentifyingExpression)applyResult.getTerm("ide") instanceof IotaNode)) {
                getPatterns(applyResult);
                // modif CA 3 April 08 : removed 1st parameter formula
                return generalPatternProcess(applyResult);
            } 
            //else {            
                applyResult = SL.match(generalNotPattern, formula);
                if (applyResult != null && applyResult.getTerm("ide") instanceof IdentifyingExpression &&
                        ((IdentifyingExpression)applyResult.getTerm("ide") instanceof AllNode ||
                         (IdentifyingExpression)applyResult.getTerm("ide") instanceof IotaNode)) {
                    getPatterns(applyResult);
                    return generalNotPatternProcess(formula, applyResult);
                }
            //}
        } catch (Exception e) {
            e.printStackTrace();
        }
        return formula;
    } // End of beforeAssert/1
    
    // modif CA 3 April 2008 - removed first parameter Formula formula, never used
    private Formula generalPatternProcess(MatchResult applyResult) {
        try {
            myKBase.retractFormula(formulaPattern);
			myKBase.addClosedPredicate(closedPattern); 
            if (((IdentifyingExpression)applyResult.getTerm("ide")) instanceof AllNode &&
                    applyResult.getTerm("phi") instanceof TermSetNode) {
                ListOfTerm list = ((TermSetNode)applyResult.getTerm("phi")).as_terms();
                for(int i = 0; i < list.size(); i++) {
                    Formula toBelieve = formulaPattern;
                    MatchResult termMatchResult = SL.match(termPattern, list.get(i));
                    for (int j = 0; j < listOfNodes.size(); j++) {
                        toBelieve = (Formula)SL.instantiate(toBelieve, 
                                ((VariableNode)listOfNodes.get(j)).lx_name(), termMatchResult.getTerm(((VariableNode)listOfNodes.get(j)).lx_name()));
                    }
                    myKBase.assertFormula(new BelieveNode(applyResult.getTerm("agent"), toBelieve));
                    //return new BelieveNode(applyResult.getTerm("agent"), toBelieve);
                }
                return SL.TRUE;
            } 
            //else {
                Formula toBelieve = formulaPattern;
                MatchResult termMatchResult = SL.match(termPattern, applyResult.getTerm("phi"));
                for (int j = 0; j < listOfNodes.size(); j++) {
                    toBelieve = (Formula)SL.instantiate(toBelieve, 
                            ((VariableNode)listOfNodes.get(j)).lx_name(), termMatchResult.getTerm(((VariableNode)listOfNodes.get(j)).lx_name()));
                }
                myKBase.assertFormula(new BelieveNode(applyResult.getTerm("agent"), toBelieve));
                //return new BelieveNode(applyResult.getTerm("agent"), toBelieve);
                
                return SL.TRUE;
            //}
        } catch (Exception e) {
            e.printStackTrace();
            return SL.TRUE;
        }
        
    }
    
    private Formula generalNotPatternProcess(Formula formula, MatchResult applyResult) {
        try {
            ListOfTerm solutionsInKB = myKBase.queryRef((IdentifyingExpression)applyResult.getTerm("ide"));
            if (solutionsInKB != null) {
                if ((IdentifyingExpression)applyResult.getTerm("ide") instanceof IotaNode) {
                    if (solutionsInKB.get(0).equals(applyResult.getTerm("phi"))) {
						myKBase.removeClosedPredicate(finder);
                    }
                } else if (applyResult.getTerm("phi") instanceof TermSetNode && ((TermSetNode)applyResult.getTerm("phi")).as_terms().size() == solutionsInKB.size()) {
                    ListOfTerm list = ((TermSetNode)applyResult.getTerm("phi")).as_terms();
                    for(int i = 0; i < list.size(); i++) {
                        if (!solutionsInKB.contains(list.get(i))) {
                            return formula;
                        }
                    }
					myKBase.removeClosedPredicate(finder);
                }
                return SL.TRUE;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return formula;
    }
    
    private void getPatterns(MatchResult applyResult) {
        try {
            listOfNodes = new ListOfNodes();
            if (((IdentifyingExpression)applyResult.getTerm("ide")).as_term().childrenOfKind(VariableNode.class, listOfNodes)) {
                formulaPattern = (Formula)SL.toPattern(((IdentifyingExpression)applyResult.getTerm("ide")).as_formula(), listOfNodes, null);
                closedPattern = (Formula)SL.toPattern(((IdentifyingExpression)applyResult.getTerm("ide")).as_formula(), listOfNodes, "_");
                termPattern = (Term)SL.toPattern(((IdentifyingExpression)applyResult.getTerm("ide")).as_term(), listOfNodes, null);
            }             
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} // End of class AllIREFilter
