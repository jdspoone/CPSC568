
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

public class OrNode extends BinaryLogicalFormula
{
    public static Integer ID = new Integer(27);
    @Override
	public final int getClassID() {return ID.intValue();}
        public interface Operations extends Formula.Operations
        {
            public ListOfFormula getLeaves(OrNode node);
        }
        public ListOfFormula getLeaves()
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            return((OrNode.Operations)_thisoperations).getLeaves(this );
        }

    public OrNode(Formula as_left_formula, Formula as_right_formula)  {
        super(2, as_left_formula, as_right_formula);
        initNode();
    }

    public OrNode() {
        super(2, null, null);
        initNode();
    }

    @Override
	public void accept(Visitor visitor) {visitor.visitOrNode(this);}

    @Override
	public Node getClone(HashMap clones) {
        Node clone = new OrNode(null, null);
        clone.copyValueOf(this, clones);
        return clone;
    }

    @Override
	public void copyValueOf(Node n, HashMap clones) {
        if (n instanceof OrNode) {
            super.copyValueOf(n, clones);
            OrNode tn = (OrNode)n;
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