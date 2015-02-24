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
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.ListOfFormula;
import jade.semantics.lang.sl.grammar.ListOfNodes;
import jade.semantics.lang.sl.grammar.MetaVariableReferenceNode;
import jade.semantics.lang.sl.grammar.NotNode;
import jade.semantics.lang.sl.grammar.OrNode;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.grammar.TrueNode;
import jade.semantics.lang.sl.grammar.Variable;
import jade.semantics.lang.sl.tools.SL;

public class OrNodeOperations extends FormulaNodeOperations implements OrNode.Operations {
	
	@Override
	public void simplify(Formula node) {
        Formula left = ((OrNode) node).as_left_formula().sm_simplified_formula();
        Formula right = ((OrNode) node).as_right_formula().sm_simplified_formula();

///**************************
        if (left.isSubsumedBy(right)) {
            node.sm_simplified_formula(left);
        }
        else if (right.isSubsumedBy(left)) {
			node.sm_simplified_formula(right);
        }
        else if ((right.isSubsumedBy(new NotNode(left))) || (left.isSubsumedBy(new NotNode(right)))) {
			TrueNode trueNode = SL.TRUE;
			trueNode.sm_simplified_formula(trueNode);
			node.sm_simplified_formula(trueNode);
        }
        else if (left instanceof AndNode) {
			Formula leftLeft = ((AndNode) left).as_left_formula().sm_simplified_formula();
			Formula leftRight = ((AndNode) left).as_right_formula().sm_simplified_formula();
			node.sm_simplified_formula((new AndNode(new OrNode(leftLeft, right), new OrNode(leftRight, right))).getSimplifiedFormula());
        }
        else if (right instanceof AndNode) {
			Formula rightLeft = ((AndNode) right).as_left_formula().sm_simplified_formula();
			Formula rightRight = ((AndNode) right).as_right_formula().sm_simplified_formula();
			node.sm_simplified_formula((new AndNode(new OrNode(rightLeft, left), new OrNode(rightRight, left))).getSimplifiedFormula());
        }
        else if (left instanceof OrNode) {
			Formula leftLeft = ((OrNode) left).as_left_formula().sm_simplified_formula();
			Formula leftRight = ((OrNode) left).as_right_formula().sm_simplified_formula();
			node.sm_simplified_formula(new OrNode(leftLeft, new OrNode(leftRight, right)).getSimplifiedFormula());
        }
        else {
			node.sm_simplified_formula(orderOrLeaves(left, right));
        }
//***************************/
//        OrNode result = new OrNode(left, right);
//        result.sm_simplified_formula(result);
//        node.sm_simplified_formula(result);
//        System.err.println("?????????? OR");

    }

    private OrNode orderOrLeaves(Formula left, Formula right) {
		OrNode orNode;
        // left is supposed to be a leaf (not a OrNode)
        // right is supposed to be either a leaf (terminal case) or an already
        // ordered OrNode
        if (right instanceof OrNode) {
            Formula middle = ((OrNode) right).as_left_formula();
            if (left.compare(middle) <= 0) {
                orNode = new OrNode(left, right);
				orNode.sm_simplified_formula(orNode);
            }
            else {
                orNode = (OrNode) new OrNode(middle, orderOrLeaves(left, ((OrNode) right).as_right_formula())).getSimplifiedFormula();
            }
        }
        else if (left.compare(right) <= 0) {
			orNode = new OrNode(left, right);
			orNode.sm_simplified_formula(orNode);
		}
        else {
			orNode = new OrNode(right, left);
			orNode.sm_simplified_formula(orNode);
		}
		return orNode;
    }

    @Override
	public boolean isMentalAttitude(Formula node, Term term) {
        return (((OrNode) node).as_left_formula().isMentalAttitude(term) && ((OrNode) node).as_right_formula()
                .isMentalAttitude(term));
    }
    
    @Override
	public boolean isInstitutionalFact(Formula node, Term term) {
        return (((OrNode) node).as_left_formula().isInstitutionalFact(term) && 
        		((OrNode) node).as_right_formula().isMentalAttitude(term));
    }

    @Override
	public Formula isBeliefFrom(Formula node, Term agent) {
    	Formula leftBelief = ((OrNode)node).as_left_formula().isBeliefFrom(agent);
    	Formula rightFormula = ((OrNode)node).as_right_formula();
    	if (leftBelief == null) {
    		leftBelief = rightFormula.isBeliefFrom(agent);
    		rightFormula = ((OrNode)node).as_left_formula();
    	}
    	if (leftBelief != null && rightFormula.isMentalAttitude(agent)) {
    		return new OrNode(leftBelief, rightFormula);
    	}
    	else {
    		return null;
    	}
    }
    
    @Override
	public Formula isInstitutionalFactFrom(Formula node, Term institution) {
    	Formula leftInstfact = ((OrNode)node).as_left_formula().isInstitutionalFactFrom(institution);
    	Formula rightFormula = ((OrNode)node).as_right_formula();
    	if (leftInstfact == null) {
    		leftInstfact = rightFormula.isBeliefFrom(institution);
    		rightFormula = ((OrNode)node).as_left_formula();
    	}
    	if (leftInstfact != null && rightFormula.isInstitutionalFact(institution)) {
    		return new OrNode(leftInstfact, rightFormula);
    	}
    	else {
    		return null;
    	}
    }
    
    
    @Override
	public Formula isExistsOn(Formula node, Term variable) {
    	Formula leftExists = ((OrNode)node).as_left_formula().isExistsOn(variable);
    	Variable leftVar = null;
    	if (variable instanceof Variable) {
    		leftVar = (Variable)variable;
    	}
    	else if (variable instanceof MetaVariableReferenceNode) {
    		leftVar = ((MetaVariableReferenceNode)variable).sm_value();
    	}
		if (leftExists != null && variable != null) {
    		MetaVariableReferenceNode var = new MetaVariableReferenceNode("var");
    		Formula rightExists = ((OrNode)node).as_right_formula().isExistsOn(var);
    		if (rightExists != null && var.sm_value() != null) {
    			rightExists = (Formula)rightExists.getClone();
    			ListOfNodes varOccurrences = new ListOfNodes();
    			rightExists.find(Variable.class, Variable.lx_name_ID, var.sm_value().lx_name(), varOccurrences, true);
    			for (int i=0; i<varOccurrences.size(); i++) {
    				((Variable)varOccurrences.get(i)).lx_name(leftVar.lx_name());
    			}
    			return new OrNode(leftExists, rightExists);
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
            Formula left = ((OrNode) node).as_left_formula().getSimplifiedFormula();
            Formula right = ((OrNode) node).as_right_formula().getSimplifiedFormula();
            if (left.isSubsumedBy(formula) || right.isSubsumedBy(formula)) {
                return true;
            }
            else if (formula instanceof OrNode) {
                Formula left_form = ((OrNode) formula).as_left_formula().getSimplifiedFormula();
                Formula right_form = ((OrNode) formula).as_right_formula().getSimplifiedFormula();
                return (left.isSubsumedBy(left_form) && left.isSubsumedBy(right_form))
                        || (right.isSubsumedBy(left_form) && right.isSubsumedBy(right_form));
            }
            else {
                return false;
            }
        }
    }

    @Override
	public Formula getDoubleMirror(Formula node, Term i, Term j, boolean default_result_is_true) {
        return (new OrNode(((OrNode) node).as_left_formula().getDoubleMirror(i, j, true), ((OrNode) node)
                .as_right_formula().getDoubleMirror(i, j, true))).getSimplifiedFormula();
    }
	
    public ListOfFormula getLeaves(OrNode node)
    {
    	ListOfFormula result = new ListOfFormula();
    	getLeaves(node, result);
    	return result;
    }
    
    private void getLeaves(OrNode node, ListOfFormula leaves) {
    	if (node.as_left_formula() instanceof OrNode) {
    		getLeaves((OrNode)node.as_left_formula(), leaves);
    	}
    	else {
    		leaves.append(node.as_left_formula());
    	}
    	if (node.as_right_formula() instanceof OrNode) {
    		getLeaves((OrNode)node.as_right_formula(), leaves);
    	}
    	else {
    		leaves.append(node.as_right_formula());
    	}
    }
}
