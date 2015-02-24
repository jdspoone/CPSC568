
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

public class SequenceActionExpressionNode extends ActionExpression
{
    public static Integer ID = new Integer(48);
    @Override
	public final int getClassID() {return ID.intValue();}
    static int _as_left_action = 0;
    static int _as_right_action = 1;

    public SequenceActionExpressionNode(Term as_left_action, Term as_right_action)  {
        super(2);
        as_left_action(as_left_action);
        as_right_action(as_right_action);
        initNode();
    }

    public SequenceActionExpressionNode() {
        super(2);
        as_left_action(null);
        as_right_action(null);
        initNode();
    }

    @Override
	public void accept(Visitor visitor) {visitor.visitSequenceActionExpressionNode(this);}

    @Override
	public Node getClone(HashMap clones) {
        Node clone = new SequenceActionExpressionNode(null, null);
        clone.copyValueOf(this, clones);
        return clone;
    }

    @Override
	public void copyValueOf(Node n, HashMap clones) {
        if (n instanceof SequenceActionExpressionNode) {
            super.copyValueOf(n, clones);
            SequenceActionExpressionNode tn = (SequenceActionExpressionNode)n;
        }
        initNode();
    }


    @Override
	public Node.Operations getOperations() {
        Node.Operations result = (Node.Operations)_operations.get(ID);
        if ( result == null ) {result = super.getOperations();}
        return result;
    }
    static public int as_left_action_ID = new String("as_left_action").hashCode();
    public Term as_left_action() {return (Term)_nodes[_as_left_action];}
    public void as_left_action(Term s) {_nodes[_as_left_action] = s;}
    static public int as_right_action_ID = new String("as_right_action").hashCode();
    public Term as_right_action() {return (Term)_nodes[_as_right_action];}
    public void as_right_action(Term s) {_nodes[_as_right_action] = s;}

    @Override
	public boolean hasAttribute(int attrname) {
        if ( attrname == as_left_action_ID) return true;
        if ( attrname == as_right_action_ID) return true;
        return super.hasAttribute(attrname);
    }

    @Override
	public Object getAttribute(int attrname) {
        if ( attrname == as_left_action_ID) return as_left_action();
        if ( attrname == as_right_action_ID) return as_right_action();
        return super.getAttribute(attrname);
    }

    @Override
	public void setAttribute(int attrname, Object attrvalue) {
        if ( attrname == as_left_action_ID) {as_left_action((Term)attrvalue);return;}
        if ( attrname == as_right_action_ID) {as_right_action((Term)attrvalue);return;}
        super.setAttribute(attrname, attrvalue);
    }
}