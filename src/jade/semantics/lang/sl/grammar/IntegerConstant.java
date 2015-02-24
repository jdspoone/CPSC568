
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

public abstract class IntegerConstant extends Constant
{
    public static Integer ID = new Integer(10024);
    @Override
	public int getClassID() {return ID.intValue();}
    java.lang.Long _lx_value;

    public IntegerConstant(int capacity, java.lang.Long lx_value)  {
      super (capacity);
        lx_value(lx_value);
    }

    @Override
	public void copyValueOf(Node n, HashMap clones) {
        if (n instanceof IntegerConstant) {
            super.copyValueOf(n, clones);
            IntegerConstant tn = (IntegerConstant)n;
            lx_value( tn._lx_value);
        }
    }

    @Override
	public Node.Operations getOperations() {
        Node.Operations result = (Node.Operations)_operations.get(ID);
        if ( result == null ) {result = super.getOperations();}
        return result;
    }
    static public int lx_value_ID = new String("lx_value").hashCode();
    public java.lang.Long lx_value() {return _lx_value;}
    public void lx_value(java.lang.Long o) {_lx_value = o;}

    @Override
	public boolean hasAttribute(int attrname) {
        if ( attrname == lx_value_ID) return true;
        return super.hasAttribute(attrname);
    }

    @Override
	public Object getAttribute(int attrname) {
        if ( attrname == lx_value_ID) return lx_value();
        return super.getAttribute(attrname);
    }

    @Override
	public void setAttribute(int attrname, Object attrvalue) {
        if ( attrname == lx_value_ID) {lx_value((java.lang.Long)attrvalue);return;}
        super.setAttribute(attrname, attrvalue);
    }
}