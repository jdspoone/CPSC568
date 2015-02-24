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

import jade.semantics.kbase.QueryResult;
import jade.semantics.lang.sl.grammar.MetaTermReferenceNode;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.tools.MatchResult;

import java.util.Set;
import java.util.Vector;

/**
 * @author Vincent Louis - France Telecom
 *
 */
public class BuiltInPredicate {
	
	private String id;
	
	/**
	 * Creates a built-in predicate with a given
	 * name (which is considered as the predicate unique identifier)
	 * 
	 * @param id name of the predicate to create
	 */
	public BuiltInPredicate(String id) {
		this.id = id;
	}
	
	/**
	 * Returns the predicate unique identifier (i.e. its name)
	 * 
	 * @return the predicate unique identifier
	 * @see #BuiltInPredicate(String)
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object arg0) {
		return (arg0 instanceof BuiltInPredicate
				&& id.equals(((BuiltInPredicate)arg0).id));
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	
	protected QueryResult querySingleValues(MetaTermReferenceNode v1, Set<Term> values) {
		if (values != null) {
			QueryResult result = new QueryResult();
			for (Term value : values) {
				MatchResult bindings = new MatchResult();
				MetaTermReferenceNode binding = (MetaTermReferenceNode)v1.getClone();
				binding.sm_value(value);
				bindings.add(binding);
				result.add(bindings);
			}
			return result;
		}
		return null;
	}

	protected QueryResult queryTupleValues(MetaTermReferenceNode[] metaRefs, Set<Vector<Term>> values) {
		if (values != null && values.size() > 0) {
			QueryResult result = new QueryResult();
			for (Vector<Term> value : values) {
				MatchResult bindings = new MatchResult();
				for (int i=0 ; i<metaRefs.length ; i++) {
					MetaTermReferenceNode binding = (MetaTermReferenceNode)metaRefs[i].getClone();
					binding.sm_value(value.get(i));
					bindings.add(binding);
				}
				result.add(bindings);
			}
			return result;
		}
		return null;
	}
}
