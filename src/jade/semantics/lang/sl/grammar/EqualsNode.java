
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

public class EqualsNode extends AtomicFormula
{
    public static Integer ID = new Integer(13);
    @Override
	public final int getClassID() {return ID.intValue();}
    static int _as_left_term = 0;
    static int _as_right_term = 1;

    public EqualsNode(Term as_left_term, Term as_right_term)  {
        super(2);
        as_left_term(as_left_term);
        as_right_term(as_right_term);
        initNode();
    }

    public EqualsNode() {
        super(2);
        as_left_term(null);
        as_right_term(null);
        initNode();
    }

    @Override
	public void accept(Visitor visitor) {visitor.visitEqualsNode(this);}

    @Override
	public Node getClone(HashMap clones) {
        Node clone = new EqualsNode(null, null);
        clone.copyValueOf(this, clones);
        return clone;
    }

    @Override
	public void copyValueOf(Node n, HashMap clones) {
        if (n instanceof EqualsNode) {
            super.copyValueOf(n, clones);
            EqualsNode tn = (EqualsNode)n;
        }
        initNode();
    }


    @Override
	public Node.Operations getOperations() {
        Node.Operations result = (Node.Operations)_operations.get(ID);
        if ( result == null ) {result = super.getOperations();}
        return result;
    }
    static public int as_left_term_ID = new String("as_left_term").hashCode();
    public Term as_left_term() {return (Term)_nodes[_as_left_term];}
    public void as_left_term(Term s) {_nodes[_as_left_term] = s;}
    static public int as_right_term_ID = new String("as_right_term").hashCode();
    public Term as_right_term() {return (Term)_nodes[_as_right_term];}
    public void as_right_term(Term s) {_nodes[_as_right_term] = s;}

    @Override
	public boolean hasAttribute(int attrname) {
        if ( attrname == as_left_term_ID) return true;
        if ( attrname == as_right_term_ID) return true;
        return super.hasAttribute(attrname);
    }

    @Override
	public Object getAttribute(int attrname) {
        if ( attrname == as_left_term_ID) return as_left_term();
        if ( attrname == as_right_term_ID) return as_right_term();
        return super.getAttribute(attrname);
    }

    @Override
	public void setAttribute(int attrname, Object attrvalue) {
        if ( attrname == as_left_term_ID) {as_left_term((Term)attrvalue);return;}
        if ( attrname == as_right_term_ID) {as_right_term((Term)attrvalue);return;}
        super.setAttribute(attrname, attrvalue);
    }
}