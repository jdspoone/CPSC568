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

import jade.semantics.lang.sl.grammar.Node;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.tools.SL;

public class TermNodeOperations extends DefaultNodeOperations implements Term.Operations {
	
	@Override
	public void initNode(Node node)
	{		
		((Term)node).sm_simplified_term(null);
	}

	public void simplify(Term node)
	{
		node.sm_simplified_term(node);
	}
	
    public Term getSimplifiedTerm(Term node) {
		 doSimplifyNode(node);
		 return node.sm_simplified_term();
    }
	
	public jade.semantics.lang.sl.tools.MatchResult match(Term node, Node expression)
	{
		return SL.match(node, expression);
	}
	
	public Term instantiate(Term node, String varname, Node expression)
	{
		return (Term)SL.instantiate(node, varname, expression);
	}
}
