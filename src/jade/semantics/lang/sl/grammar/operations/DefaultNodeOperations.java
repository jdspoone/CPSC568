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
import jade.semantics.lang.sl.grammar.ListOfNodes;
import jade.semantics.lang.sl.grammar.Node;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.parser.SLUnparser;
import jade.semantics.lang.sl.tools.SLEqualizer;

public class DefaultNodeOperations implements Node.Operations 
{
	public void initNode(Node node)
	{		
	}
	
	protected ListOfNodes nodesToSimplify(Node node, ListOfNodes nodes)
	// Should only be called on nodes holding sm_simplified_node operations.
	{
		boolean simplify = (node instanceof Formula && ((Formula)node).sm_simplified_formula() == null ) 
		|| (node instanceof Term && ((Term)node).sm_simplified_term() == null );
		
		int currentSize = nodes.size();
		
		Node[] children = node.children();
		for (int i=0; i<children.length; i++) {
			Node child = children[i];
			if ( child != null ) {
				nodesToSimplify(child, nodes);	
			}
		}
		
		if ( simplify || nodes.size() != currentSize ) {
			if ( node instanceof Formula ) {
				((Formula)node).sm_simplified_formula(null);
				nodes.add(node);
			}
			else if ( node instanceof Term ){
				((Term)node).sm_simplified_term(null);
				nodes.add(node);
			}
		} 
		
		return nodes;
	}
	
	protected void doSimplifyNode(Node node) {
		ListOfNodes nodes = nodesToSimplify(node, new ListOfNodes());
		for (int i=0; i<nodes.size(); i++) {
			if ( nodes.get(i) instanceof Formula ) {
				((Formula)nodes.get(i)).simplify();
			}
			else {
				((Term)nodes.get(i)).simplify();
			}
		}
	}
	
	public String toString(Node node)
	{
		java.io.StringWriter writer = new java.io.StringWriter();
		node.accept(new SLUnparser(writer));
		return writer.toString();
	}
	
	public boolean equals(Node node1, Node node2)
	{
		if ( node1 == node2) {
			return true;
		}
		else if (node2 == null) { // node1 cannot be null because this method was called through a "node1.equals(node2)"
			return false;
		}
		else {
			return new SLEqualizer().equals(node1, node2);
		}
	}
	
	public int compare(Node node1, Node node2)
	{
		return 0;
	}
}
