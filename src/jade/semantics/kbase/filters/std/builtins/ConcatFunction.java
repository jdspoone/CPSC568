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
 * created on 22 nov. 07 by Vincent Louis
 */

/**
 * 
 */
package jade.semantics.kbase.filters.std.builtins;

import jade.semantics.kbase.KBase;
import jade.semantics.lang.sl.grammar.StringConstantNode;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.tools.SL;

/**
 * @author Vincent Louis - France Telecom
 *
 */
public class ConcatFunction extends BinaryFunction {

	public static final String CONCAT_ID = "concat";
	
	/**
	 * 
	 */
	public ConcatFunction() {
		super(CONCAT_ID);
	}
	
	
	/* (non-Javadoc)
	 * @see jade.semantics.kbase.filters.std.builtins.TernaryPredicate#checkDomainV1(jade.semantics.lang.sl.grammar.Term)
	 */
	@Override
	protected boolean checkDomainV1(Term i1) {
		return i1 instanceof StringConstantNode;
	}
	
	/* (non-Javadoc)
	 * @see jade.semantics.kbase.filters.std.builtins.TernaryPredicate#checkDomainV2(jade.semantics.lang.sl.grammar.Term)
	 */
	@Override
	protected boolean checkDomainV2(Term i2) {
		return i2 instanceof StringConstantNode;
	}
	
	/* (non-Javadoc)
	 * @see jade.semantics.kbase.filters.std.builtins.TernaryPredicate#checkDomainV3(jade.semantics.lang.sl.grammar.Term)
	 */
	@Override
	protected boolean checkDomainV3(Term i3) {
		return i3 instanceof StringConstantNode;
	}
	
	/* (non-Javadoc)
	 * @see jade.semantics.kbase.filters.std.builtins.BinaryFunction#eval(jade.semantics.lang.sl.grammar.Term, jade.semantics.lang.sl.grammar.Term, jade.semantics.kbase.KBase)
	 */
	@Override
	protected Term eval(Term i1, Term i2, KBase kb) {
		return SL.string(
				((StringConstantNode)i1).stringValue() + ((StringConstantNode)i2).stringValue());
	}

}

