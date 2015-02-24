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

import jade.semantics.lang.sl.grammar.FalseNode;
import jade.semantics.lang.sl.grammar.FeasibleNode;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.Term;

public class FeasibleNodeOperations extends FormulaNodeOperations {
	
	@Override
	public void simplify(Formula node) {
        Term action = ((FeasibleNode) node).as_action().sm_simplified_term();
        Formula formula = ((FeasibleNode) node).as_formula().sm_simplified_formula();
        if (formula instanceof FalseNode) {
			node.sm_simplified_formula(formula);
        }
        else {
			FeasibleNode feasibleNode = new FeasibleNode(action, formula);
			feasibleNode.sm_simplified_formula(feasibleNode);
			node.sm_simplified_formula(feasibleNode);
        }
    }

    @Override
	public boolean isSubsumedBy(Formula node, Formula formula) {
        if (super.isSubsumedBy(node, formula)) {
            return true;
        }
        // tests if (feasible ??A ??PHI) is subsumed by (feasible ??A ??PSI)
        else if (formula instanceof FeasibleNode) {
            return ((FeasibleNode) node).as_action().equals(((FeasibleNode) formula).as_action())
                    && ((FeasibleNode) node).as_formula().isSubsumedBy(((FeasibleNode) formula).as_formula());
        }
        return false;
    }
}
