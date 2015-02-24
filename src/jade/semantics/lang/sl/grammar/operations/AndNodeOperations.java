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
import jade.semantics.lang.sl.grammar.FalseNode;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.ListOfFormula;
import jade.semantics.lang.sl.grammar.MetaTermReferenceNode;
import jade.semantics.lang.sl.grammar.NotNode;
import jade.semantics.lang.sl.grammar.Term;


public class AndNodeOperations extends FormulaNodeOperations implements AndNode.Operations {
	
    @Override
	public void simplify(Formula node) {
		
        Formula left = ((AndNode) node).as_left_formula().sm_simplified_formula();
        Formula right = ((AndNode) node).as_right_formula().sm_simplified_formula();

//    	System.err.println("------- SIMP. AND " + left + " / " + right);

///*********************        
//        //#PJAVA_EXCLUDE_BEGIN
        if (left.isSubsumedBy(right)) {
            node.sm_simplified_formula(right);
//        	System.err.println("------- CAS 1 = " + node.sm_simplified_formula());
        }
        else if (right.isSubsumedBy(left)) {
			node.sm_simplified_formula(left);
//        	System.err.println("------- CAS 2 = " + node.sm_simplified_formula());
        }
        else if ((new NotNode(right)).isSubsumedBy(left) || (new NotNode(left)).isSubsumedBy(right)) {
			FalseNode falseNode = new FalseNode();
			falseNode.sm_simplified_formula(falseNode);
			node.sm_simplified_formula(falseNode);
//        	System.err.println("------- CAS 3 = " + node.sm_simplified_formula());
        }
        else if (left instanceof AndNode) {
			Formula leftLeft = ((AndNode) left).as_left_formula().sm_simplified_formula();
			Formula leftRight = ((AndNode) left).as_right_formula().sm_simplified_formula();
			node.sm_simplified_formula(new AndNode(leftLeft, new AndNode(leftRight, right)).getSimplifiedFormula());
//        	System.err.println("------- CAS 4 = " + node.sm_simplified_formula());
        }
        else {
			node.sm_simplified_formula(orderAndLeaves(left, right));
//        	System.err.println("------- CAS 5 = " + node.sm_simplified_formula());
        }

//        //#PJAVA_EXCLUDE_END
//        /*#PJAVA_INCLUDE_BEGIN
// **********************/
//        AndNode result = new AndNode(left, right);
//        result.sm_simplified_formula(result);
//        node.sm_simplified_formula(result);
//        System.err.println("?????????? AND");
//        #PJAVA_INCLUDE_END*/
    }

    private AndNode orderAndLeaves(Formula left, Formula right) {
        // left is supposed to be a leaf (not a AndNode)
        // right is supposed to be either a leaf (terminal case) or an already
        // ordered AndNode
		AndNode andNode;
		if (right instanceof AndNode) {
			Formula middle = ((AndNode) right).as_left_formula();
			if (left.compare(middle) <= 0) {
				andNode = new AndNode(left, right);
				andNode.sm_simplified_formula(andNode);
			}
			else {
				andNode = (AndNode)new AndNode(middle, orderAndLeaves(left, ((AndNode) right).as_right_formula())).getSimplifiedFormula();
			}
		}
		else if (left.compare(right) <= 0) {
			andNode =  new AndNode(left, right);
			andNode.sm_simplified_formula(andNode);			
		}
		else {
			andNode =  new AndNode(right, left);
			andNode.sm_simplified_formula(andNode);
		}
		return andNode;
    }
    
    @Override
	public boolean isMentalAttitude(Formula node, Term term) {
        return (((AndNode) node).as_left_formula().isMentalAttitude(term) && ((AndNode) node).as_right_formula()
                .isMentalAttitude(term));
    }
    
    @Override
	public boolean isInstitutionalFact(Formula node, Term term) {
        return (((AndNode) node).as_left_formula().isInstitutionalFact(term) && 
        		((AndNode) node).as_right_formula().isInstitutionalFact(term));
    }

    @Override
	public Formula isInstitutionalFactFrom(Formula node, Term institution) {
        Formula leftFact = ((AndNode) node).as_left_formula().isInstitutionalFactFrom(institution);
        if (leftFact != null) {
            Term instantiatedInstitution;
            if (institution instanceof MetaTermReferenceNode) {
                instantiatedInstitution = ((MetaTermReferenceNode)institution).sm_value();
            } else {
                instantiatedInstitution = institution;
            }
            Formula rightFact = ((AndNode) node).as_right_formula().isInstitutionalFactFrom(instantiatedInstitution);
            if (rightFact != null) {
                return new AndNode(leftFact, rightFact);
            }
        }
        return null;
    }
    
    @Override
	public Formula isBeliefFrom(Formula node, Term agent) {
        Formula leftBelief = ((AndNode) node).as_left_formula().isBeliefFrom(agent);
        if (leftBelief != null) {
            Term instantiatedAgent;
            if (agent instanceof MetaTermReferenceNode) {
                instantiatedAgent = ((MetaTermReferenceNode)agent).sm_value();
            } else {
                instantiatedAgent = agent;
            }
            Formula rightBelief = ((AndNode) node).as_right_formula().isBeliefFrom(instantiatedAgent);
            if (rightBelief != null) {
                return new AndNode(leftBelief, rightBelief);
            }
        }
        return null;
    }

    @Override
	public boolean isSubsumedBy(Formula node, Formula formula) {
        if (super.isSubsumedBy(node, formula)) {
            return true;
        }
        else {
            Formula left = ((AndNode) node).as_left_formula().getSimplifiedFormula();
            Formula right = ((AndNode) node).as_right_formula().getSimplifiedFormula();
            if (left.isSubsumedBy(formula) && right.isSubsumedBy(formula)) {
                return true;
            }
            else if (formula instanceof AndNode) {
                Formula left_form = ((AndNode) formula).as_left_formula().getSimplifiedFormula();
                Formula right_form = ((AndNode) formula).as_right_formula().getSimplifiedFormula();
                return (left.isSubsumedBy(left_form) || left.isSubsumedBy(right_form))
                        && (right.isSubsumedBy(left_form) || right.isSubsumedBy(right_form));
            }
            else {
                return false;
            }
        }
    }

    @Override
	public Formula getDoubleMirror(Formula node, Term i, Term j, boolean default_result_is_true) {
        return (new AndNode(((AndNode) node).as_left_formula().getDoubleMirror(i, j, true), ((AndNode) node)
                .as_right_formula().getDoubleMirror(i, j, true))).getSimplifiedFormula();
    }
	
    public ListOfFormula getLeaves(AndNode node)
    {
    	ListOfFormula result = new ListOfFormula();
    	getLeaves(node, result);
    	return result;
    }
    
    private void getLeaves(AndNode node, ListOfFormula leaves) {
    	if (node.as_left_formula() instanceof AndNode) {
    		getLeaves((AndNode)node.as_left_formula(), leaves);
    	}
    	else {
    		leaves.append(node.as_left_formula());
    	}
    	if (node.as_right_formula() instanceof AndNode) {
    		getLeaves((AndNode)node.as_right_formula(), leaves);
    	}
    	else {
    		leaves.append(node.as_right_formula());
    	}
    }
}
