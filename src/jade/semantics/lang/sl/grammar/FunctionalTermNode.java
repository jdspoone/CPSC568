
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

public class FunctionalTermNode extends FunctionalTerm
{
    public static Integer ID = new Integer(49);
    @Override
	public final int getClassID() {return ID.intValue();}
    static int _as_terms = 1;

    public FunctionalTermNode(Symbol as_symbol, ListOfTerm as_terms)  {
        super(2, as_symbol);
        as_terms(as_terms);
        initNode();
    }

    public FunctionalTermNode() {
        super(2, null);
        as_terms(null);
        initNode();
    }

    @Override
	public void accept(Visitor visitor) {visitor.visitFunctionalTermNode(this);}

    @Override
	public Node getClone(HashMap clones) {
        Node clone = new FunctionalTermNode(null, null);
        clone.copyValueOf(this, clones);
        return clone;
    }

    @Override
	public void copyValueOf(Node n, HashMap clones) {
        if (n instanceof FunctionalTermNode) {
            super.copyValueOf(n, clones);
            FunctionalTermNode tn = (FunctionalTermNode)n;
        }
        initNode();
    }


    @Override
	public Node.Operations getOperations() {
        Node.Operations result = (Node.Operations)_operations.get(ID);
        if ( result == null ) {result = super.getOperations();}
        return result;
    }
    static public int as_terms_ID = new String("as_terms").hashCode();
    public ListOfTerm as_terms() {return (ListOfTerm)_nodes[_as_terms];}
    public void as_terms(ListOfTerm s) {_nodes[_as_terms] = s;}

    @Override
	public boolean hasAttribute(int attrname) {
        if ( attrname == as_terms_ID) return true;
        return super.hasAttribute(attrname);
    }

    @Override
	public Object getAttribute(int attrname) {
        if ( attrname == as_terms_ID) return as_terms();
        return super.getAttribute(attrname);
    }

    @Override
	public void setAttribute(int attrname, Object attrvalue) {
        if ( attrname == as_terms_ID) {as_terms((ListOfTerm)attrvalue);return;}
        super.setAttribute(attrname, attrvalue);
    }
}