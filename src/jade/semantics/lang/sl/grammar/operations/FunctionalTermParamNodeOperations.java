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

package jade.semantics.lang.sl.grammar.operations;

import jade.semantics.lang.sl.grammar.FunctionalTermParamNode;
import jade.semantics.lang.sl.grammar.ListOfParameter;
import jade.semantics.lang.sl.grammar.Parameter;
import jade.semantics.lang.sl.grammar.ParameterNode;
import jade.semantics.lang.sl.grammar.Term;

import java.util.Comparator;

public class FunctionalTermParamNodeOperations 
	extends TermNodeOperations 
	implements FunctionalTermParamNode.Operations {
	
    public Term getParameter(FunctionalTermParamNode node, String name) {
		ListOfParameter result = new ListOfParameter();
		if ( node.as_parameters().find(ParameterNode.class, Parameter.lx_name_ID, name, result, false)) {
			return result.element(0).as_value();
		}
		else {
			return null;
		}
    }
	
    public void setParameter(FunctionalTermParamNode node, String name, Term term) {
		ListOfParameter result = new ListOfParameter();
		if ( node.as_parameters().find(ParameterNode.class, Parameter.lx_name_ID, name, result, false)) {
			result.element(0).as_value(term);
		}
		else {
			node.as_parameters().add(new ParameterNode(term, name, new Boolean(false)));
		}
	}
	
	
	@Override
	public void simplify(Term node)
	{
		if ( ((FunctionalTermParamNode)node).as_parameters().size() == 0 ) {
			super.simplify(node);
		}
		else {
			FunctionalTermParamNode original = (FunctionalTermParamNode)node;
			FunctionalTermParamNode simplified = (FunctionalTermParamNode)original.getClone();
			for (int i=0; i<simplified.as_parameters().size(); i++) {
				Parameter p = simplified.as_parameters().element(i);
				Term value = original.as_parameters().element(i).as_value().sm_simplified_term();
				p.as_value(value);
			}
			node.sm_simplified_term(simplified);
			simplified.sm_simplified_term(simplified);
			simplified.as_parameters().sort(new Comparator() {
				public int compare(Object o1, Object o2) {
					return ((Parameter)o1).compare((Parameter)o2);
				}
				@Override
				public boolean equals(Object obj) {
					return super.equals(obj);
				}
			});
		}
	}
}
