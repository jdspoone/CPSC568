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
import jade.semantics.lang.sl.grammar.IntegerConstantNode;
import jade.semantics.lang.sl.grammar.ListOfNodes;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.grammar.TermSequence;
import jade.semantics.lang.sl.tools.SL;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * (nth ??n ??elem ??sequence)
 * (n=0 pour le 1er �l�ment de la sequence)
 * @author Vincent Louis - France Telecom
 *
 */
public class NthPredicate extends TernaryPredicate {

	/**
	 * (nth ??n ??elem ??sequence)
	 */
	
	public static final String NTH_ID = "nth";
	/**
	 * @param id
	 */
	public NthPredicate() {
		super(NTH_ID);
	}

	@Override
	protected boolean checkDomainV1(Term i1) {
		return (i1 instanceof IntegerConstantNode && ((IntegerConstantNode)i1).intValue().longValue() >= 0);
	}

	@Override
	protected boolean checkDomainV2(Term i2) {
		return true;
	}
	
	@Override
	protected boolean checkDomainV3(Term i3) {
		return i3 instanceof TermSequence;
	}

	@Override
	protected boolean doQuery(Term i1, Term i2, Term i3, KBase kb) {
		return i2.equals(((TermSequence)i3).getTerm(((IntegerConstantNode)i1).intValue().intValue()));
	}
	
	@Override
	protected Set<Term> doQueryV1Values(Term i2, Term i3, KBase kb) {
		ListOfNodes set = ((TermSequence)i3).as_terms();
		for (int i=0; i<set.size(); i++) {
			if (set.get(i).equals(i2)) {
				Set<Term> result = new HashSet<Term>(1);
				result.add(SL.integer(new Long(i)));
				return result;
			}
		}
		return null;
	}
	
	@Override
	protected Set<Term> doQueryV2Values(Term i1, Term i3, KBase kb) {
		ListOfNodes set = ((TermSequence)i3).as_terms();
		int i = ((IntegerConstantNode)i1).intValue().intValue();
		if (i < set.size()) {
			Set<Term> result = new HashSet<Term>(1);
			result.add((Term)set.get(i));
			return result;
		}
		return null;
	}

	@Override
	protected Set<Vector<Term>> doQueryV1V2Values(Term i3, KBase kb) {
		ListOfNodes set = ((TermSequence)i3).as_terms();
		if (set.size() > 0) {
			Set<Vector<Term>> result = new HashSet<Vector<Term>>(set.size());
			for (int i=0; i<set.size(); i++) {
				Vector<Term> pair = new Vector<Term>(2);
				pair.add(SL.integer(new Long(i)));
				pair.add((Term)set.get(i));
				result.add(pair);
			}
			return result;
		}
		return null;
	}	
}
