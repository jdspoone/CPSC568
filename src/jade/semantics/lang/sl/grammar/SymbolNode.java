
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

public class SymbolNode extends Symbol
{
    public static Integer ID = new Integer(52);
    @Override
	public final int getClassID() {return ID.intValue();}
    java.lang.String _lx_value;
    int _lx_hashcode;

    public SymbolNode(java.lang.String lx_value, int lx_hashcode)  {
        super(0);
        lx_value(lx_value);
        lx_hashcode(lx_hashcode);
    }

    public SymbolNode() {
        super(0);
        lx_value(null);
        lx_hashcode(0);
        initNode();
    }

    @Override
	public void accept(Visitor visitor) {visitor.visitSymbolNode(this);}

    @Override
	public Node getClone(HashMap clones) {
        Node clone = new SymbolNode();
        clone.copyValueOf(this, clones);
        return clone;
    }

    @Override
	public void copyValueOf(Node n, HashMap clones) {
        if (n instanceof SymbolNode) {
            super.copyValueOf(n, clones);
            SymbolNode tn = (SymbolNode)n;
            lx_value( tn._lx_value);
            lx_hashcode( tn._lx_hashcode);
        }
        initNode();
    }


    @Override
	public Node.Operations getOperations() {
        Node.Operations result = (Node.Operations)_operations.get(ID);
        if ( result == null ) {result = super.getOperations();}
        return result;
    }
    static public int lx_value_ID = new String("lx_value").hashCode();
    public java.lang.String lx_value() {return _lx_value;}
    public void lx_value(java.lang.String o) {_lx_value = o;}
    static public int lx_hashcode_ID = new String("lx_hashcode").hashCode();
    public int lx_hashcode() {return _lx_hashcode;}
    public void lx_hashcode(int o) {_lx_hashcode = o;}

    @Override
	public boolean hasAttribute(int attrname) {
        if ( attrname == lx_value_ID) return true;
        if ( attrname == lx_hashcode_ID) return true;
        return super.hasAttribute(attrname);
    }

    @Override
	public Object getAttribute(int attrname) {
        if ( attrname == lx_value_ID) return lx_value();
        if ( attrname == lx_hashcode_ID) return new Integer(lx_hashcode());
        return super.getAttribute(attrname);
    }

    @Override
	public void setAttribute(int attrname, Object attrvalue) {
        if ( attrname == lx_value_ID) {lx_value((java.lang.String)attrvalue);return;}
        if ( attrname == lx_hashcode_ID) {lx_hashcode(((Integer)attrvalue).intValue()); return;}
        super.setAttribute(attrname, attrvalue);
    }
}