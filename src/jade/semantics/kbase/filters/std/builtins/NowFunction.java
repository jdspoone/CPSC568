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
 * created on 11 d�c. 07 by Vincent Louis
 */

/**
 * 
 */
package jade.semantics.kbase.filters.std.builtins;

import java.util.Date;

import jade.semantics.kbase.KBase;
import jade.semantics.lang.sl.grammar.DateTimeConstantNode;
import jade.semantics.lang.sl.grammar.Term;

/**
 * @author Vincent Louis - France Telecom
 *
 */
public class NowFunction extends ConstantFunction {

	public static final String NOW_ID = "now";

	/**
	 * 
	 */
	public NowFunction() {
		super(NOW_ID);
	}
	
	/* (non-Javadoc)
	 * @see jade.semantics.kbase.filters.std.builtins.UnaryPredicate#checkDomainV1(jade.semantics.lang.sl.grammar.Term)
	 */
	@Override
	protected boolean checkDomainV1(Term i1) {
		return i1 instanceof DateTimeConstantNode;
	}
	
	/* (non-Javadoc)
	 * @see jade.semantics.kbase.filters.std.builtins.ConstantFunction#eval(jade.semantics.kbase.KBase)
	 */
	@Override
	protected Term eval(KBase kb) {
		return new DateTimeConstantNode(new Date(System.currentTimeMillis()));
	}
}

