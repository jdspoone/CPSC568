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
 
public class MetaReferenceNodeOperations 
    extends DefaultNodeOperations
{
    @Override
	public boolean equals(Node node1, Node node2)
    {
	return (node1.getClass().equals(node2.getClass())) 
	    && (node1.toString() == node2.toString());
// 	return ((node1 instanceof MetaFormulaReferenceNode &&
// 		 node2 instanceof MetaFormulaReferenceNode &&
// 		 ((MetaFormulaReferenceNode)node1).lx_name().equals(((MetaFormulaReferenceNode)node2).lx_name())) ||
// 		(node1 instanceof MetaVariableReferenceNode &&
// 		 node2 instanceof MetaVariableReferenceNode &&
// 		 ((MetaVariableReferenceNode)node1).lx_name().equals(((MetaVariableReferenceNode)node2).lx_name())) ||
// 		(node1 instanceof MetaSymbolReferenceNode &&
// 		 node2 instanceof MetaSymbolReferenceNode &&
// 		 ((MetaSymbolReferenceNode)node1).lx_name().equals(((MetaSymbolReferenceNode)node2).lx_name())) ||
// 		(node1 instanceof MetaActionExpressionReferenceNode &&
// 		 node2 instanceof MetaActionExpressionReferenceNode &&
// 		 ((MetaActionExpressionReferenceNode)node1).lx_name().equals(((MetaActionExpressionReferenceNode)node2).lx_name())) ||
// 		(node1 instanceof MetaContentExpressionReferenceNode &&
// 		 node2 instanceof MetaContentExpressionReferenceNode &&
// 		 ((MetaContentExpressionReferenceNode)node1).lx_name().equals(((MetaContentExpressionReferenceNode)node2).lx_name())) ||
// 		(node1 instanceof MetaTermReferenceNode &&
// 		 node2 instanceof MetaTermReferenceNode &&
// 		 ((MetaTermReferenceNode)node1).lx_name().equals(((MetaTermReferenceNode)node2).lx_name())));
    }
}
