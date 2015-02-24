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

/*
 * created on 19 nov. 07 by Vincent Louis
 */

/**
 * 
 */
package jade.semantics.kbase.filters.std.builtins;

import jade.semantics.kbase.KBase;
import jade.semantics.lang.sl.grammar.Constant;
import jade.semantics.lang.sl.grammar.IntegerConstantNode;
import jade.semantics.lang.sl.grammar.RealConstantNode;
import jade.semantics.lang.sl.grammar.Term;

/**
 * (+ ??n1 ??n2) 
 * @author Vincent Louis - France Telecom
 *
 */
public class SumFunction extends BinaryFunction {


	public static String SUM_ID = "+";
	/**
	 * @param id
	 */
	public SumFunction() {
		super(SUM_ID);
	}
	
	/* (non-Javadoc)
	 * @see jade.semantics.kbase.filters.std.builtins.TernaryPredicate#checkDomainV1(jade.semantics.lang.sl.grammar.Term)
	 */
	@Override
	protected boolean checkDomainV1(Term i1) {
		return i1 instanceof IntegerConstantNode || i1 instanceof RealConstantNode;
	}
	
	/* (non-Javadoc)
	 * @see jade.semantics.kbase.filters.std.builtins.TernaryPredicate#checkDomainV2(jade.semantics.lang.sl.grammar.Term)
	 */
	@Override
	protected boolean checkDomainV2(Term i2) {
		return i2 instanceof IntegerConstantNode || i2 instanceof RealConstantNode;
	}
	
	/* (non-Javadoc)
	 * @see jade.semantics.kbase.filters.std.builtins.TernaryPredicate#checkDomainV3(jade.semantics.lang.sl.grammar.Term)
	 */
	@Override
	protected boolean checkDomainV3(Term i3) {
		return i3 instanceof IntegerConstantNode || i3 instanceof RealConstantNode;
	}

	/* (non-Javadoc)
	 * @see jade.semantics.kbase.filters.std.builtins.BinaryFunction#eval(jade.semantics.lang.sl.grammar.Term, jade.semantics.lang.sl.grammar.Term, jade.semantics.kbase.KBase)
	 */
	@Override
	protected Term eval(Term i1, Term i2, KBase kb) {
		if (i1 instanceof RealConstantNode || i2 instanceof RealConstantNode) {
			return new RealConstantNode(new Double(
					((Constant)i1).realValue().doubleValue()
					+ ((Constant)i2).realValue().doubleValue()));
		}
		return new IntegerConstantNode(new Long(
				((Constant)i1).intValue().longValue()
				+ ((Constant)i2).intValue().longValue()));
	}
}