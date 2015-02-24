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

import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.MetaTermReferenceNode;
import jade.semantics.lang.sl.grammar.PersistentGoalNode;
import jade.semantics.lang.sl.grammar.Term;
 
public class PersistentGoalNodeOperations
    extends FormulaNodeOperations
{
	@Override
	public void simplify(Formula node) {
		Term agent = ((PersistentGoalNode)node).as_agent().sm_simplified_term();
		Formula formula = ((PersistentGoalNode)node).as_formula().sm_simplified_formula();
		PersistentGoalNode pgGoal = new PersistentGoalNode(agent, formula);
		pgGoal.sm_simplified_formula(pgGoal);
		node.sm_simplified_formula(pgGoal);
    }

    @Override
	public boolean isMentalAttitude(Formula node, Term term)
    {
    	Term agent = term;
    	if (term instanceof MetaTermReferenceNode) {
    		agent = ((MetaTermReferenceNode)term).sm_value();
    		if (agent == null) {
    			((MetaTermReferenceNode)term).sm_value(((PersistentGoalNode) node).as_agent());
    			return true;
    		}
    	}
        return ((PersistentGoalNode) node).as_agent().equals(agent);
    }
}
