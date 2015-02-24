
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

public class InstitutionalFactNode extends InstitutionalLogicFormula
{
    public static Integer ID = new Integer(16);
    @Override
	public final int getClassID() {return ID.intValue();}
    static int _as_institution = 0;
    static int _as_fact = 1;

    public InstitutionalFactNode(Term as_institution, Formula as_fact)  {
        super(2);
        as_institution(as_institution);
        as_fact(as_fact);
        initNode();
    }

    public InstitutionalFactNode() {
        super(2);
        as_institution(null);
        as_fact(null);
        initNode();
    }

    @Override
	public void accept(Visitor visitor) {visitor.visitInstitutionalFactNode(this);}

    @Override
	public Node getClone(HashMap clones) {
        Node clone = new InstitutionalFactNode(null, null);
        clone.copyValueOf(this, clones);
        return clone;
    }

    @Override
	public void copyValueOf(Node n, HashMap clones) {
        if (n instanceof InstitutionalFactNode) {
            super.copyValueOf(n, clones);
            InstitutionalFactNode tn = (InstitutionalFactNode)n;
        }
        initNode();
    }


    @Override
	public Node.Operations getOperations() {
        Node.Operations result = (Node.Operations)_operations.get(ID);
        if ( result == null ) {result = super.getOperations();}
        return result;
    }
    static public int as_institution_ID = new String("as_institution").hashCode();
    public Term as_institution() {return (Term)_nodes[_as_institution];}
    public void as_institution(Term s) {_nodes[_as_institution] = s;}
    static public int as_fact_ID = new String("as_fact").hashCode();
    public Formula as_fact() {return (Formula)_nodes[_as_fact];}
    public void as_fact(Formula s) {_nodes[_as_fact] = s;}

    @Override
	public boolean hasAttribute(int attrname) {
        if ( attrname == as_institution_ID) return true;
        if ( attrname == as_fact_ID) return true;
        return super.hasAttribute(attrname);
    }

    @Override
	public Object getAttribute(int attrname) {
        if ( attrname == as_institution_ID) return as_institution();
        if ( attrname == as_fact_ID) return as_fact();
        return super.getAttribute(attrname);
    }

    @Override
	public void setAttribute(int attrname, Object attrvalue) {
        if ( attrname == as_institution_ID) {as_institution((Term)attrvalue);return;}
        if ( attrname == as_fact_ID) {as_fact((Formula)attrvalue);return;}
        super.setAttribute(attrname, attrvalue);
    }
}