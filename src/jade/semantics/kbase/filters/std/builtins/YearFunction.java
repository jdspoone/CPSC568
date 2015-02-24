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
import jade.semantics.lang.sl.grammar.DateTimeConstantNode;
import jade.semantics.lang.sl.grammar.IntegerConstantNode;
import jade.semantics.lang.sl.grammar.Term;

/**
 * (card ??set ??card)
 * @author Vincent Louis - France Telecom
 *
 */
public class YearFunction extends UnaryFunction {

	public static final String YEAR_ID = "year";
	/**
	 * @param id
	 */
	public YearFunction() {
		super(YEAR_ID);
	}
	
	@Override
	protected boolean checkDomainV2(Term i2) {
		return i2 instanceof IntegerConstantNode;
	}
	
	@Override
	protected boolean checkDomainV1(Term i1) {
		return (i1 instanceof DateTimeConstantNode);
	}
		
	/* (non-Javadoc)
	 * @see jade.semantics.kbase.filters.std.builtins.UnaryFunction#eval(jade.semantics.lang.sl.grammar.Term, jade.semantics.kbase.KBase)
	 */
	@Override
	protected Term eval(Term i1, KBase kb) {
		return new IntegerConstantNode(new Long(
				((DateTimeConstantNode)i1).lx_value().getYear() + 1900)
		);
	}
}
