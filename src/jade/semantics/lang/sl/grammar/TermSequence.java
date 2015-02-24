
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

public abstract class TermSequence extends Term
{
        public interface Operations extends Term.Operations
        {
            public void addTerm(TermSequence node, Term term);
            public void removeTerm(TermSequence node, Term term);
            public Term getTerm(TermSequence node, int i);
            public int size(TermSequence node);
        }
        public void addTerm(Term term)
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            ((TermSequence.Operations)_thisoperations).addTerm(this , term);
        }
        public void removeTerm(Term term)
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            ((TermSequence.Operations)_thisoperations).removeTerm(this , term);
        }
        public Term getTerm(int i)
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            return((TermSequence.Operations)_thisoperations).getTerm(this , i);
        }
        public int size()
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            return((TermSequence.Operations)_thisoperations).size(this );
        }
    public static Integer ID = new Integer(10018);
    @Override
	public int getClassID() {return ID.intValue();}
    static int _as_terms = 0;

    public TermSequence(int capacity, ListOfTerm as_terms)  {
      super (capacity);
        as_terms(as_terms);
    }

    @Override
	public void copyValueOf(Node n, HashMap clones) {
        if (n instanceof TermSequence) {
            super.copyValueOf(n, clones);
            TermSequence tn = (TermSequence)n;
        }
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