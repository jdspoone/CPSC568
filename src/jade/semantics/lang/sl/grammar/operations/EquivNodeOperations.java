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
import jade.semantics.lang.sl.grammar.EquivNode;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.ImpliesNode;
 
public class EquivNodeOperations
    extends FormulaNodeOperations
{
	@Override
	public void simplify(Formula node) {	
		Formula left = ((EquivNode)node).as_left_formula().sm_simplified_formula();
		Formula right = ((EquivNode)node).as_right_formula().sm_simplified_formula();
		node.sm_simplified_formula((new AndNode(new ImpliesNode(left, right), 
											    new ImpliesNode(right, left))).getSimplifiedFormula());
    }
}
