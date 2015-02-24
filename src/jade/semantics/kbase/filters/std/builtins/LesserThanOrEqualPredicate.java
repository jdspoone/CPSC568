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
import jade.semantics.lang.sl.grammar.Term;

/**
 * (<= ??x1 ??x2)
 * @author Vincent Louis - France Telecom
 *
 */
public class LesserThanOrEqualPredicate extends LesserThanPredicate {

	public static final String LESSER_THAN_OR_EQUAL_ID = "<=";
	/**
	 * @param id
	 */
	public LesserThanOrEqualPredicate() {
		super(LESSER_THAN_OR_EQUAL_ID);
	}

	@Override
	protected boolean doQuery(Term i1, Term i2, KBase kb) {
		return i1.equals(i2) || super.doQuery(i1, i2, kb);
	}
}
