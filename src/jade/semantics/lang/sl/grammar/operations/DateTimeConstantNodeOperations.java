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
import jade.semantics.lang.sl.grammar.DateTimeConstantNode;
import jade.semantics.lang.sl.grammar.Node;

public class DateTimeConstantNodeOperations 
	extends TermNodeOperations 
	implements Constant.Operations 
{
    public Long intValue(Constant node)
	{
		return null;
	}
	
    public Double realValue(Constant node)
	{
		return null;
	}
	
    public String stringValue(Constant node)
	{
		return null;
	}
	
    public byte[] byteValue(Constant node)
	{
		return ((DateTimeConstantNode)node).lx_value().toString().getBytes();
	}

	@Override
	public int compare(Node node1, Node node2)
	{
	    //#DOTNET_EXCLUDE_BEGIN
	    //#PJAVA_EXCLUDE_BEGIN
		if ( node1 == node2 ) {
			return 0;
		}
		else {
			return ((DateTimeConstantNode)node1).lx_value().compareTo(((DateTimeConstantNode)node2).lx_value());
		}
		//#PJAVA_EXCLUDE_END
        //#DOTNET_EXCLUDE_END
        
        /*#DOTNET_INCLUDE_BEGIN
        java.util.Date date1 = ((DateTimeConstantNode)node1).lx_value();
        java.util.Date date2 = ((DateTimeConstantNode)node2).lx_value();
        System.DateTime dt1 = new System.DateTime(date1.getYear(), date1.getMonth(), date1.getDay(), date1.getHours(), date1.getMinutes(), date1.getSeconds
(), 0);
        System.DateTime dt2 = new System.DateTime(date1.getYear(), date1.getMonth(), date1.getDay(), date1.getHours(), date1.getMinutes(), date1.getSeconds
(), 0);
        return System.DateTime.Compare(dt1, dt2);
        #DOTNET_INCLUDE_END*/
        
        /*#PJAVA_INCLUDE_BEGIN
               if ( ((DateTimeConstantNode)node1).lx_value().getTime() < ((DateTimeConstantNode)node2).lx_value().getTime() ) {
                   return -1;
               }
               else if ( ((DateTimeConstantNode)node1).lx_value().getTime() == ((DateTimeConstantNode)node2).lx_value().getTime() )
               {
                   return 0;
               }
               else {
                   return 1;
               }
        #PJAVA_INCLUDE_END*/
	}
}
