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
import jade.semantics.lang.sl.grammar.Term;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Vincent Louis - France Telecom
 *
 */
public abstract class UnaryFunction extends BinaryPredicate {

	/**
	 * @param id
	 */
	public UnaryFunction(String id) {
		super(id);
	}
	
	/* (non-Javadoc)
	 * @see jade.semantics.kbase.filters.std.builtins.BinaryPredicate#doQuery(jade.semantics.lang.sl.grammar.Term, jade.semantics.lang.sl.grammar.Term, jade.semantics.kbase.KBase)
	 */
	@Override
	protected boolean doQuery(Term i1, Term i2, KBase kb) {
		return i2.equals(eval(i1, kb));
	}
	
	/* (non-Javadoc)
	 * @see jade.semantics.kbase.filters.std.builtins.BinaryPredicate#doQueryV1Values(jade.semantics.lang.sl.grammar.Term, jade.semantics.kbase.KBase)
	 */
	@Override
	protected Set<Term> doQueryV2Values(Term i1, KBase kb) {
		Set<Term> result = new HashSet<Term>(1);
		result.add(eval(i1, kb));
		return result;
	}
		
	abstract protected Term eval(Term i1, KBase kb);

}
