
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

public class FunctionalTermParamNode extends FunctionalTerm
{
    public static Integer ID = new Integer(50);
    @Override
	public final int getClassID() {return ID.intValue();}
        public interface Operations extends Term.Operations
        {
            public Term getParameter(FunctionalTermParamNode node, String name);
            public void setParameter(FunctionalTermParamNode node, String name, Term term);
        }
        public Term getParameter(String name)
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            return((FunctionalTermParamNode.Operations)_thisoperations).getParameter(this , name);
        }
        public void setParameter(String name, Term term)
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            ((FunctionalTermParamNode.Operations)_thisoperations).setParameter(this , name, term);
        }
    static int _as_parameters = 1;

    public FunctionalTermParamNode(Symbol as_symbol, ListOfParameter as_parameters)  {
        super(2, as_symbol);
        as_parameters(as_parameters);
        initNode();
    }

    public FunctionalTermParamNode() {
        super(2, null);
        as_parameters(null);
        initNode();
    }

    @Override
	public void accept(Visitor visitor) {visitor.visitFunctionalTermParamNode(this);}

    @Override
	public Node getClone(HashMap clones) {
        Node clone = new FunctionalTermParamNode(null, null);
        clone.copyValueOf(this, clones);
        return clone;
    }

    @Override
	public void copyValueOf(Node n, HashMap clones) {
        if (n instanceof FunctionalTermParamNode) {
            super.copyValueOf(n, clones);
            FunctionalTermParamNode tn = (FunctionalTermParamNode)n;
        }
        initNode();
    }


    @Override
	public Node.Operations getOperations() {
        Node.Operations result = (Node.Operations)_operations.get(ID);
        if ( result == null ) {result = super.getOperations();}
        return result;
    }
    static public int as_parameters_ID = new String("as_parameters").hashCode();
    public ListOfParameter as_parameters() {return (ListOfParameter)_nodes[_as_parameters];}
    public void as_parameters(ListOfParameter s) {_nodes[_as_parameters] = s;}

    @Override
	public boolean hasAttribute(int attrname) {
        if ( attrname == as_parameters_ID) return true;
        return super.hasAttribute(attrname);
    }

    @Override
	public Object getAttribute(int attrname) {
        if ( attrname == as_parameters_ID) return as_parameters();
        return super.getAttribute(attrname);
    }

    @Override
	public void setAttribute(int attrname, Object attrvalue) {
        if ( attrname == as_parameters_ID) {as_parameters((ListOfParameter)attrvalue);return;}
        super.setAttribute(attrname, attrvalue);
    }
}