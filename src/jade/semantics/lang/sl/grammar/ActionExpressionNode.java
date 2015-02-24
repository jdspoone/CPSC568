
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

public class ActionExpressionNode extends ActionExpression
{
    public static Integer ID = new Integer(46);
    @Override
	public final int getClassID() {return ID.intValue();}
    static int _as_agent = 0;
    static int _as_term = 1;

    public ActionExpressionNode(Term as_agent, Term as_term)  {
        super(2);
        as_agent(as_agent);
        as_term(as_term);
        initNode();
    }

    public ActionExpressionNode() {
        super(2);
        as_agent(null);
        as_term(null);
        initNode();
    }

    @Override
	public void accept(Visitor visitor) {visitor.visitActionExpressionNode(this);}

    @Override
	public Node getClone(HashMap clones) {
        Node clone = new ActionExpressionNode(null, null);
        clone.copyValueOf(this, clones);
        return clone;
    }

    @Override
	public void copyValueOf(Node n, HashMap clones) {
        if (n instanceof ActionExpressionNode) {
            super.copyValueOf(n, clones);
            ActionExpressionNode tn = (ActionExpressionNode)n;
        }
        initNode();
    }


    @Override
	public Node.Operations getOperations() {
        Node.Operations result = (Node.Operations)_operations.get(ID);
        if ( result == null ) {result = super.getOperations();}
        return result;
    }
    static public int as_agent_ID = new String("as_agent").hashCode();
    public Term as_agent() {return (Term)_nodes[_as_agent];}
    public void as_agent(Term s) {_nodes[_as_agent] = s;}
    static public int as_term_ID = new String("as_term").hashCode();
    public Term as_term() {return (Term)_nodes[_as_term];}
    public void as_term(Term s) {_nodes[_as_term] = s;}

    @Override
	public boolean hasAttribute(int attrname) {
        if ( attrname == as_agent_ID) return true;
        if ( attrname == as_term_ID) return true;
        return super.hasAttribute(attrname);
    }

    @Override
	public Object getAttribute(int attrname) {
        if ( attrname == as_agent_ID) return as_agent();
        if ( attrname == as_term_ID) return as_term();
        return super.getAttribute(attrname);
    }

    @Override
	public void setAttribute(int attrname, Object attrvalue) {
        if ( attrname == as_agent_ID) {as_agent((Term)attrvalue);return;}
        if ( attrname == as_term_ID) {as_term((Term)attrvalue);return;}
        super.setAttribute(attrname, attrvalue);
    }
}