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

package jade.semantics.kbase.filters.std.builtins;

import jade.semantics.kbase.KBase;
import jade.semantics.lang.sl.grammar.Constant;
import jade.semantics.lang.sl.grammar.DateTimeConstantNode;
import jade.semantics.lang.sl.grammar.IntegerConstantNode;
import jade.semantics.lang.sl.grammar.RealConstantNode;
import jade.semantics.lang.sl.grammar.StringConstant;
import jade.semantics.lang.sl.grammar.Term;

/**
 * (< ??x1 ??x2)
 * @author Vincent Louis - France Telecom
 *
 */
public class LesserThanPredicate extends BinaryPredicate {

	public static final String LESSER_THAN_ID = "<";
	/**
	 * @param id
	 */
	public LesserThanPredicate() {
		super(LESSER_THAN_ID);
	}
	
	protected LesserThanPredicate(String id) {
		super(id);
	}

	@Override
	protected boolean checkDomainV1V2(Term i1, Term i2) {
		return ((i1 instanceof Constant) && i1.getClass().equals(i2.getClass()));
	}

	@Override
	protected boolean doQuery(Term i1, Term i2, KBase kb) {
		if (i1 instanceof DateTimeConstantNode) {
			return ((DateTimeConstantNode)i1).lx_value().before(((DateTimeConstantNode)i2).lx_value());
		}
		else if (i1 instanceof IntegerConstantNode) {
			return ((IntegerConstantNode)i1).lx_value().compareTo(((IntegerConstantNode)i2).lx_value()) < 0;
		}
		else if (i1 instanceof RealConstantNode) {
			return ((RealConstantNode)i1).lx_value().compareTo(((RealConstantNode)i2).lx_value()) < 0;
		}
		else {
			return ((StringConstant)i1).stringValue().compareTo(((StringConstant)i2).stringValue()) < 0;
		}
	}
}
