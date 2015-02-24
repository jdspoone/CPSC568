
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

public class ListOfParameter extends ListOfNodes
{
    public static Integer ID = new Integer(10026);
    @Override
	public int getClassID() {return ID.intValue();}
    public ListOfParameter(Parameter[] nodes) {
        super(nodes);
    }

    public ListOfParameter() {
        super();
    }

    public Parameter element(int i) {return (Parameter)super.get(i);}
    public void append(Parameter e) {add(e);}
    public void prepend(Parameter e) {add(0,e);}
    public Parameter last() {return (Parameter)super.getLast();}
    public Parameter first() {return (Parameter)super.getFirst();}
    @Override
	public void accept(Visitor visitor) {visitor.visitListOfParameter(this);}
    @Override
	public Node getClone(HashMap clones) {
        Node clone = new ListOfParameter();
        clone.copyValueOf(this, clones);
        return clone;
    }

    @Override
	public Node.Operations getOperations() {
        Node.Operations result = (Node.Operations)_operations.get(ID);
        if ( result == null ) {result = super.getOperations();}
        return result;
    }
}