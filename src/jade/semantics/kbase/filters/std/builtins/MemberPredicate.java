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
import jade.semantics.lang.sl.grammar.ListOfTerm;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.grammar.TermSequence;
import jade.semantics.lang.sl.grammar.TermSet;

import java.util.HashSet;
import java.util.Set;

/**
 * (member ??x ??set_or_sequence)
 * @author Vincent Louis - France Telecom
 *
 */
public class MemberPredicate extends BinaryPredicate {

	public static final String MEMBER_ID = "member";
	/**
	 * @param id
	 */
	public MemberPredicate() {
		super(MEMBER_ID);
	}
	
	@Override
	protected boolean checkDomainV1(Term i1) {
		return true;
	}
	
	@Override
	protected boolean checkDomainV2(Term i2) {
		return (i2 instanceof TermSet) || (i2 instanceof TermSequence);
	}

	@Override
	protected boolean doQuery(Term i1, Term i2, KBase kb) {
		if (i2 instanceof TermSet) {
			return ((TermSet)i2).as_terms().contains(i1);
		}
		//else {
			return ((TermSequence)i2).as_terms().contains(i1);
		//}
	}
	
	@Override
	protected Set<Term> doQueryV1Values(Term i2, KBase kb) {
		ListOfTerm set = (i2 instanceof TermSet ? ((TermSet)i2).as_terms() : ((TermSequence)i2).as_terms());
		if (set != null && set.size() > 0) {
			Set<Term> result = new HashSet<Term>(set.size());
			for (Object element : set.asAList()) {
				result.add((Term)element);
			}
			return result;
		}
		return null;
	}
}
