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
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.IntentionNode;
import jade.semantics.lang.sl.grammar.MetaTermReferenceNode;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.grammar.TrueNode;
 
public class IntentionNodeOperations
    extends FormulaNodeOperations
{
	@Override
	public void simplify(Formula node) {
    
		Term agent = ((IntentionNode)node).as_agent().sm_simplified_term();
		Formula formula = ((IntentionNode)node).as_formula().sm_simplified_formula();
		if ( formula instanceof TrueNode || formula instanceof FalseNode ) {
			FalseNode falseNode = new FalseNode();
			node.sm_simplified_formula(falseNode);
			falseNode.sm_simplified_formula(falseNode);
		}
		else {
			IntentionNode intentionNode = new IntentionNode(agent, formula);
			intentionNode.sm_simplified_formula(intentionNode);
			node.sm_simplified_formula(intentionNode);
		}
    }

    @Override
	public boolean isMentalAttitude(Formula node, Term term)
    {
    	Term agent = term;
    	if (term instanceof MetaTermReferenceNode) {
    		agent = ((MetaTermReferenceNode)term).sm_value();
    		if (agent == null) {
    			((MetaTermReferenceNode)term).sm_value(((IntentionNode) node).as_agent());
    			return true;
    		}
    	}
        return ((IntentionNode) node).as_agent().equals(agent);
    }
}
