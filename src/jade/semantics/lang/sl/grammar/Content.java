
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

public abstract class Content extends Term
{
        public interface Operations extends Term.Operations
        {
            public String toSLString(Content node);
            public Node getContentElement(Content node, int i);
            public void setContentElement(Content node, int i, Node element);
            public void addContentElement(Content node, Node element);
            public void setContentElements(Content node, int number);
            public int contentElementNumber(Content node);
            public jade.semantics.lang.sl.tools.MatchResult match(Content node, Node expression);
        }
        public String toSLString()
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            return((Content.Operations)_thisoperations).toSLString(this );
        }
        public Node getContentElement(int i)
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            return((Content.Operations)_thisoperations).getContentElement(this , i);
        }
        public void setContentElement(int i, Node element)
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            ((Content.Operations)_thisoperations).setContentElement(this , i, element);
        }
        public void addContentElement(Node element)
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            ((Content.Operations)_thisoperations).addContentElement(this , element);
        }
        public void setContentElements(int number)
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            ((Content.Operations)_thisoperations).setContentElements(this , number);
        }
        public int contentElementNumber()
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            return((Content.Operations)_thisoperations).contentElementNumber(this );
        }
        @Override
		public jade.semantics.lang.sl.tools.MatchResult match(Node expression)
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            return((Content.Operations)_thisoperations).match(this , expression);
        }
    public static Integer ID = new Integer(10001);
    @Override
	public int getClassID() {return ID.intValue();}
    static int _as_expressions = 0;

    public Content(int capacity, ListOfContentExpression as_expressions)  {
      super (capacity);
        as_expressions(as_expressions);
    }

    @Override
	public void copyValueOf(Node n, HashMap clones) {
        if (n instanceof Content) {
            super.copyValueOf(n, clones);
            Content tn = (Content)n;
        }
    }

    @Override
	public Node.Operations getOperations() {
        Node.Operations result = (Node.Operations)_operations.get(ID);
        if ( result == null ) {result = super.getOperations();}
        return result;
    }
    static public int as_expressions_ID = new String("as_expressions").hashCode();
    public ListOfContentExpression as_expressions() {return (ListOfContentExpression)_nodes[_as_expressions];}
    public void as_expressions(ListOfContentExpression s) {_nodes[_as_expressions] = s;}

    @Override
	public boolean hasAttribute(int attrname) {
        if ( attrname == as_expressions_ID) return true;
        return super.hasAttribute(attrname);
    }

    @Override
	public Object getAttribute(int attrname) {
        if ( attrname == as_expressions_ID) return as_expressions();
        return super.getAttribute(attrname);
    }

    @Override
	public void setAttribute(int attrname, Object attrvalue) {
        if ( attrname == as_expressions_ID) {as_expressions((ListOfContentExpression)attrvalue);return;}
        super.setAttribute(attrname, attrvalue);
    }
}