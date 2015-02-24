
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

public class ResultNode extends AtomicFormula
{
    public static Integer ID = new Integer(9);
    @Override
	public final int getClassID() {return ID.intValue();}
    static int _as_term1 = 0;
    static int _as_term2 = 1;

    public ResultNode(Term as_term1, Term as_term2)  {
        super(2);
        as_term1(as_term1);
        as_term2(as_term2);
        initNode();
    }

    public ResultNode() {
        super(2);
        as_term1(null);
        as_term2(null);
        initNode();
    }

    @Override
	public void accept(Visitor visitor) {visitor.visitResultNode(this);}

    @Override
	public Node getClone(HashMap clones) {
        Node clone = new ResultNode(null, null);
        clone.copyValueOf(this, clones);
        return clone;
    }

    @Override
	public void copyValueOf(Node n, HashMap clones) {
        if (n instanceof ResultNode) {
            super.copyValueOf(n, clones);
            ResultNode tn = (ResultNode)n;
        }
        initNode();
    }


    @Override
	public Node.Operations getOperations() {
        Node.Operations result = (Node.Operations)_operations.get(ID);
        if ( result == null ) {result = super.getOperations();}
        return result;
    }
    static public int as_term1_ID = new String("as_term1").hashCode();
    public Term as_term1() {return (Term)_nodes[_as_term1];}
    public void as_term1(Term s) {_nodes[_as_term1] = s;}
    static public int as_term2_ID = new String("as_term2").hashCode();
    public Term as_term2() {return (Term)_nodes[_as_term2];}
    public void as_term2(Term s) {_nodes[_as_term2] = s;}

    @Override
	public boolean hasAttribute(int attrname) {
        if ( attrname == as_term1_ID) return true;
        if ( attrname == as_term2_ID) return true;
        return super.hasAttribute(attrname);
    }

    @Override
	public Object getAttribute(int attrname) {
        if ( attrname == as_term1_ID) return as_term1();
        if ( attrname == as_term2_ID) return as_term2();
        return super.getAttribute(attrname);
    }

    @Override
	public void setAttribute(int attrname, Object attrvalue) {
        if ( attrname == as_term1_ID) {as_term1((Term)attrvalue);return;}
        if ( attrname == as_term2_ID) {as_term2((Term)attrvalue);return;}
        super.setAttribute(attrname, attrvalue);
    }
}