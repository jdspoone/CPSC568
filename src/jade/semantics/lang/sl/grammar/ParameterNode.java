
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


//-----------------------------------------------------
// This file has been automatically produced by a tool.
//-----------------------------------------------------

package jade.semantics.lang.sl.grammar;

import java.util.HashMap;
//#PJAVA_EXCLUDE_END
/*#PJAVA_INCLUDE_BEGIN
import jade.util.leap.Collection;
import jade.util.leap.Comparable;
import jade.util.leap.LinkedList;
import jade.util.leap.List;
import jade.util.leap.HashMap;
#PJAVA_INCLUDE_END*/

public class ParameterNode extends Parameter
{
    public static Integer ID = new Integer(51);
    @Override
	public final int getClassID() {return ID.intValue();}

    public ParameterNode(Term as_value, java.lang.String lx_name, java.lang.Boolean lx_optional)  {
        super(1, as_value, lx_name, lx_optional);
    }

    public ParameterNode(Term as_value)  {
        super(1, as_value, null, null);
        initNode();
    }

    public ParameterNode() {
        super(1, null, null, null);
        initNode();
    }

    @Override
	public void accept(Visitor visitor) {visitor.visitParameterNode(this);}

    @Override
	public Node getClone(HashMap clones) {
        Node clone = new ParameterNode(null);
        clone.copyValueOf(this, clones);
        return clone;
    }

    @Override
	public void copyValueOf(Node n, HashMap clones) {
        if (n instanceof ParameterNode) {
            super.copyValueOf(n, clones);
            ParameterNode tn = (ParameterNode)n;
        }
        initNode();
    }


    @Override
	public Node.Operations getOperations() {
        Node.Operations result = (Node.Operations)_operations.get(ID);
        if ( result == null ) {result = super.getOperations();}
        return result;
    }
}