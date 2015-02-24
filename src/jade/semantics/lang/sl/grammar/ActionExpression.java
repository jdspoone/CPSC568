
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

public abstract class ActionExpression extends Term
{
        public interface Operations extends Term.Operations
        {
            public ListOfTerm getActors(ActionExpression node);
            public Term getActor(ActionExpression node);
            public ActionExpression getFirstStep(ActionExpression node);
        }
        public ListOfTerm getActors()
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            return((ActionExpression.Operations)_thisoperations).getActors(this );
        }
        public Term getActor()
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            return((ActionExpression.Operations)_thisoperations).getActor(this );
        }
        public ActionExpression getFirstStep()
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            return((ActionExpression.Operations)_thisoperations).getFirstStep(this );
        }
    public static Integer ID = new Integer(10020);
    @Override
	public int getClassID() {return ID.intValue();}
    jade.semantics.actions.SemanticAction _sm_action;

    public ActionExpression(int capacity)  {
      super (capacity);
    }

    @Override
	public void copyValueOf(Node n, HashMap clones) {
        if (n instanceof ActionExpression) {
            super.copyValueOf(n, clones);
            ActionExpression tn = (ActionExpression)n;
            sm_action( tn._sm_action);
        }
    }

    @Override
	public Node.Operations getOperations() {
        Node.Operations result = (Node.Operations)_operations.get(ID);
        if ( result == null ) {result = super.getOperations();}
        return result;
    }
    static public int sm_action_ID = new String("sm_action").hashCode();
    public jade.semantics.actions.SemanticAction sm_action() {return _sm_action;}
    public void sm_action(jade.semantics.actions.SemanticAction o) {_sm_action = o;}

    @Override
	public boolean hasAttribute(int attrname) {
        if ( attrname == sm_action_ID) return true;
        return super.hasAttribute(attrname);
    }

    @Override
	public Object getAttribute(int attrname) {
        if ( attrname == sm_action_ID) return sm_action();
        return super.getAttribute(attrname);
    }

    @Override
	public void setAttribute(int attrname, Object attrvalue) {
        if ( attrname == sm_action_ID) {sm_action((jade.semantics.actions.SemanticAction)attrvalue);return;}
        super.setAttribute(attrname, attrvalue);
    }
}