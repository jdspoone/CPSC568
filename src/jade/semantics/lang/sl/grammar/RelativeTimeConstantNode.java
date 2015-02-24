
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

//#PJAVA_EXCLUDE_BEGIN
import java.util.HashMap;

public class RelativeTimeConstantNode extends IntegerConstant
{
    public static Integer ID = new Integer(43);
    @Override
	public final int getClassID() {return ID.intValue();}

    public RelativeTimeConstantNode(java.lang.Long lx_value)  {
        super(0, lx_value);
    }

    public RelativeTimeConstantNode() {
        super(0, null);
        initNode();
    }

    @Override
	public void accept(Visitor visitor) {visitor.visitRelativeTimeConstantNode(this);}

    @Override
	public Node getClone(HashMap clones) {
        Node clone = new RelativeTimeConstantNode();
        clone.copyValueOf(this, clones);
        return clone;
    }

    @Override
	public void copyValueOf(Node n, HashMap clones) {
        if (n instanceof RelativeTimeConstantNode) {
            super.copyValueOf(n, clones);
            RelativeTimeConstantNode tn = (RelativeTimeConstantNode)n;
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