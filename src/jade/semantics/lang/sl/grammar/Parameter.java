
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

public abstract class Parameter extends Node
{
    public static Integer ID = new Integer(10027);
    @Override
	public int getClassID() {return ID.intValue();}
    java.lang.String _lx_name;
    java.lang.Boolean _lx_optional;
    static int _as_value = 0;

    public Parameter(int capacity, Term as_value, java.lang.String lx_name, java.lang.Boolean lx_optional)  {
      super (capacity);
        as_value(as_value);
        lx_name(lx_name);
        lx_optional(lx_optional);
    }

    @Override
	public void copyValueOf(Node n, HashMap clones) {
        if (n instanceof Parameter) {
            super.copyValueOf(n, clones);
            Parameter tn = (Parameter)n;
            lx_name( tn._lx_name);
            lx_optional( tn._lx_optional);
        }
    }

    @Override
	public Node.Operations getOperations() {
        Node.Operations result = (Node.Operations)_operations.get(ID);
        if ( result == null ) {result = super.getOperations();}
        return result;
    }
    static public int lx_name_ID = new String("lx_name").hashCode();
    public java.lang.String lx_name() {return _lx_name;}
    public void lx_name(java.lang.String o) {_lx_name = o;}
    static public int lx_optional_ID = new String("lx_optional").hashCode();
    public java.lang.Boolean lx_optional() {return _lx_optional;}
    public void lx_optional(java.lang.Boolean o) {_lx_optional = o;}
    static public int as_value_ID = new String("as_value").hashCode();
    public Term as_value() {return (Term)_nodes[_as_value];}
    public void as_value(Term s) {_nodes[_as_value] = s;}

    @Override
	public boolean hasAttribute(int attrname) {
        if ( attrname == lx_name_ID) return true;
        if ( attrname == lx_optional_ID) return true;
        if ( attrname == as_value_ID) return true;
        return super.hasAttribute(attrname);
    }

    @Override
	public Object getAttribute(int attrname) {
        if ( attrname == lx_name_ID) return lx_name();
        if ( attrname == lx_optional_ID) return lx_optional();
        if ( attrname == as_value_ID) return as_value();
        return super.getAttribute(attrname);
    }

    @Override
	public void setAttribute(int attrname, Object attrvalue) {
        if ( attrname == lx_name_ID) {lx_name((java.lang.String)attrvalue);return;}
        if ( attrname == lx_optional_ID) {lx_optional((java.lang.Boolean)attrvalue);return;}
        if ( attrname == as_value_ID) {as_value((Term)attrvalue);return;}
        super.setAttribute(attrname, attrvalue);
    }
}