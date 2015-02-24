
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

public abstract class Term extends Node
{
        public interface Operations extends Node.Operations
        {
            public Term getSimplifiedTerm(Term node);
            public void simplify(Term node);
            public jade.semantics.lang.sl.tools.MatchResult match(Term node, Node expression);
            public Term instantiate(Term node, String varname, Node expression);
        }
        public Term getSimplifiedTerm()
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            return((Term.Operations)_thisoperations).getSimplifiedTerm(this );
        }
        public void simplify()
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            ((Term.Operations)_thisoperations).simplify(this );
        }
        public jade.semantics.lang.sl.tools.MatchResult match(Node expression)
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            return((Term.Operations)_thisoperations).match(this , expression);
        }
        public Term instantiate(String varname, Node expression)
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            return((Term.Operations)_thisoperations).instantiate(this , varname, expression);
        }
    public static Integer ID = new Integer(10014);
    @Override
	public int getClassID() {return ID.intValue();}
    Term _sm_simplified_term;

    public Term(int capacity)  {
      super (capacity);
    }

    @Override
	public void copyValueOf(Node n, HashMap clones) {
        if (n instanceof Term) {
            super.copyValueOf(n, clones);
            Term tn = (Term)n;
            sm_simplified_term( tn._sm_simplified_term);
        }
    }

    @Override
	public Node.Operations getOperations() {
        Node.Operations result = (Node.Operations)_operations.get(ID);
        if ( result == null ) {result = super.getOperations();}
        return result;
    }
    static public int sm_simplified_term_ID = new String("sm_simplified_term").hashCode();
    public Term sm_simplified_term() {return _sm_simplified_term;}
    public void sm_simplified_term(Term o) {_sm_simplified_term = o;}

    @Override
	public boolean hasAttribute(int attrname) {
        if ( attrname == sm_simplified_term_ID) return true;
        return super.hasAttribute(attrname);
    }

    @Override
	public Object getAttribute(int attrname) {
        if ( attrname == sm_simplified_term_ID) return sm_simplified_term();
        return super.getAttribute(attrname);
    }

    @Override
	public void setAttribute(int attrname, Object attrvalue) {
        if ( attrname == sm_simplified_term_ID) {sm_simplified_term((Term)attrvalue);return;}
        super.setAttribute(attrname, attrvalue);
    }
}