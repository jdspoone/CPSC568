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

import jade.semantics.lang.sl.grammar.Constant;
import jade.semantics.lang.sl.grammar.Node;
import jade.semantics.lang.sl.grammar.RealConstantNode;

public class RealConstantNodeOperations 
	extends TermNodeOperations 
	implements Constant.Operations 
{
    public Long intValue(Constant node)
	{
		return new Long (((RealConstantNode)node).lx_value().longValue());
	}
	
    public Double realValue(Constant node)
	{
		return ((RealConstantNode)node).lx_value();
	}
	
    public String stringValue(Constant node)
	{
		return ((RealConstantNode)node).lx_value().toString();
	}
	
    public byte[] byteValue(Constant node)
	{
		return new byte[] {((RealConstantNode)node).lx_value().byteValue()};
	}

	@Override
	public int compare(Node node1, Node node2)
	{
	    //#PJAVA_EXCLUDE_BEGIN
		if ( node1 == node2 ) {
			return 0;
		}
		else {
			return ((RealConstantNode)node1).lx_value().compareTo(((RealConstantNode)node2).lx_value());
		}
		//#PJAVA_EXCLUDE_END
        /*#PJAVA_INCLUDE_BEGIN
               if ( ((RealConstantNode)node1).lx_value().longValue() < ((RealConstantNode)node2).lx_value().longValue() ) {
                   return -1;
               }
               else if ( ((RealConstantNode)node1).lx_value().longValue() == ((RealConstantNode)node2).lx_value().longValue() ) {
                   return 0;
               }
               else {
                   return 1;
               }
        #PJAVA_INCLUDE_END*/
         
	}
}
