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
import jade.semantics.lang.sl.grammar.FalseNode;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.IntentionNode;
import jade.semantics.lang.sl.grammar.ListOfNodes;
import jade.semantics.lang.sl.grammar.ListOfVariable;
import jade.semantics.lang.sl.grammar.MetaTermReferenceNode;
import jade.semantics.lang.sl.grammar.MetaVariableReferenceNode;
import jade.semantics.lang.sl.grammar.Node;
import jade.semantics.lang.sl.grammar.NotNode;
import jade.semantics.lang.sl.grammar.OrNode;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.grammar.Variable;
import jade.semantics.lang.sl.grammar.VariableNode;
import jade.semantics.lang.sl.tools.SL;

public class FormulaNodeOperations extends DefaultNodeOperations implements Formula.Operations {
	
    @Override
	public void initNode(Node node)
	{		
		((Formula)node).sm_simplified_formula(null);
	}
	 
	public void simplify(Formula node)
	{
		node.sm_simplified_formula(node);
	}
	
    public Formula getSimplifiedFormula(Formula node) {
		doSimplifyNode(node);
		return node.sm_simplified_formula();
    }

    public boolean isMentalAttitude(Formula node, Term term) {
        return false;
    }
    
    public boolean isInstitutionalFact(Formula node, Term term) {
    	return false;
    }
    
    public Formula isInstitutionalFactFrom(Formula node, Term institution) {
        return null;
    }
    
    public Formula isBeliefFrom(Formula node, Term agent) {
        return null;
    }

    public Formula isExistsOn(Formula node, Term variable) {
        return null;
    }

    public boolean isSubsumedBy(Formula node, Formula formula) {
        if (formula instanceof FalseNode) {
            return true;
        }
        else if (formula instanceof IntentionNode) {
            Term i_agent = ((IntentionNode) formula).as_agent();
            Formula i_form = ((IntentionNode) formula).as_formula();
            return node.isSubsumedBy(new BelieveNode(i_agent, new NotNode(i_form)).getSimplifiedFormula());
        }
        else if (formula instanceof AndNode) {
            return (node.isSubsumedBy(((AndNode) formula).as_left_formula()) || (node.isSubsumedBy(((AndNode) formula)
                    .as_right_formula())));
        }
        else if (formula instanceof OrNode) {
            return (node.isSubsumedBy(((OrNode) formula).as_left_formula()) && (node.isSubsumedBy(((OrNode) formula)
                    .as_right_formula())));
        }
        else {
 //           return node.equals(formula.getVariablesSubstitutionAsIn(node));
            return node.equals(formula);
		}
    }

    public boolean isConsistentWith(Formula node, Formula formula) {
        return !((new AndNode(node, formula)).getSimplifiedFormula().equals(new FalseNode()));
    }

    public Formula getDoubleMirror(Formula node, Term i, Term j, boolean default_result_is_true) {
        if (default_result_is_true) {
            return SL.TRUE;
        }
        else {
            return new FalseNode();
        }
    }

    public boolean isAFreeVariable(Formula node, Variable x) {
        ListOfNodes variables = new ListOfNodes();
        while (x instanceof MetaVariableReferenceNode) {
        	if (((MetaVariableReferenceNode)x).sm_value() == null) {
         		return node.find(new Class[] {
         				MetaVariableReferenceNode.class, MetaTermReferenceNode.class},
         				Variable.lx_name_ID, x.lx_name(), variables, false);
        	}
        	else {
        		x = ((MetaVariableReferenceNode)x).sm_value();
        	}
        }
        return node.find(VariableNode.class, Variable.lx_name_ID, x.lx_name(), variables, false);
        
//        if (node.find(Variable.class, Variable.lx_name_ID, x.lx_name(), variables, true)) {
//        	for (Iterator iterator = variables.iterator() ; iterator.hasNext() ;) {
//        		if (iterator.next() == x) return true;
//        	}
//        }
//        return false;
    }

    public Formula getVariablesSubstitution(Formula node, Variable x, Variable y) {
        Formula result = (Formula) node.getClone();
        if (x instanceof VariableNode && y instanceof VariableNode) {
            ListOfVariable rvars = new ListOfVariable();
            result.find(VariableNode.class, Variable.lx_name_ID, x.lx_name(), rvars, true);
            for (int i = 0; i < rvars.size(); i++) {
                if (rvars.get(i) instanceof VariableNode) {
                    rvars.element(i).lx_name(y.lx_name());
                }
            }
        }
        return result;
    }

    public Formula getVariablesSubstitution(Formula node, ListOfVariable vars) {
        Formula result = (Formula) node.getClone();
        ListOfVariable rvars = new ListOfVariable();
        result.childrenOfKind(VariableNode.class, rvars);
        for (int i = 0; i < rvars.size() && i < vars.size(); i++) {
            if (rvars.get(i) instanceof VariableNode && vars.get(i) instanceof VariableNode) {
                rvars.element(i).lx_name(vars.element(i).lx_name());
            }
        }
        return result;
    }

    public Formula getVariablesSubstitutionAsIn(Formula node, Formula formula) {
        ListOfVariable vars = new ListOfVariable();
        formula.childrenOfKind(Variable.class, vars);
        return node.getVariablesSubstitution(vars);
    }
	
	public jade.semantics.lang.sl.tools.MatchResult match(Formula node, Node expression)
	{
		return SL.match(node, expression);
	}
	
	public Formula instantiate(Formula node, String varname, Node expression)
	{
		return (Formula)SL.instantiate(node, varname, expression);
	}

}
