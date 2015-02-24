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

import jade.semantics.lang.sl.grammar.AllNode;
import jade.semantics.lang.sl.grammar.AnyNode;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.IdentifyingExpression;
import jade.semantics.lang.sl.grammar.IotaNode;
import jade.semantics.lang.sl.grammar.SomeNode;
import jade.semantics.lang.sl.grammar.Term;

public class IdentifyingExpressionNodeOperations 
	extends TermNodeOperations {
		
	@Override
	public void simplify(Term node)
	{
		Term term = ((IdentifyingExpression)node).as_term().sm_simplified_term();
		Formula formula = ((IdentifyingExpression)node).as_formula().sm_simplified_formula();
		IdentifyingExpression ire = null;
		if ( node instanceof IotaNode ) {
			ire = new IotaNode(term, formula);
		}
		else if ( node instanceof AllNode ) {
			ire = new AllNode(term, formula);
		}
		else if ( node instanceof SomeNode ) {
			ire = new SomeNode(term, formula);
		}
		else {
			ire = new AnyNode(term, formula);			
		}
		ire.sm_simplified_term(ire);
		node.sm_simplified_term(ire);
	}
}
