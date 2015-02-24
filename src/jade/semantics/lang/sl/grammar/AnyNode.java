
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

public class AnyNode extends IdentifyingExpression
{
    public static Integer ID = new Integer(31);
    @Override
	public final int getClassID() {return ID.intValue();}

    public AnyNode(Term as_term, Formula as_formula)  {
        super(2, as_term, as_formula);
        initNode();
    }

    public AnyNode() {
        super(2, null, null);
        initNode();
    }

    @Override
	public void accept(Visitor visitor) {visitor.visitAnyNode(this);}

    @Override
	public Node getClone(HashMap clones) {
        Node clone = new AnyNode(null, null);
        clone.copyValueOf(this, clones);
        return clone;
    }

    @Override
	public void copyValueOf(Node n, HashMap clones) {
        if (n instanceof AnyNode) {
            super.copyValueOf(n, clones);
            AnyNode tn = (AnyNode)n;
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