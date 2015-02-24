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
package jade.semantics.kbase.filters.std;

import jade.semantics.kbase.QueryResult;
import jade.semantics.kbase.QueryResult.BoolWrapper;
import jade.semantics.kbase.filters.KBAssertFilter;
import jade.semantics.kbase.filters.KBQueryFilter;
import jade.semantics.lang.sl.grammar.AndNode;
import jade.semantics.lang.sl.grammar.BelieveNode;
import jade.semantics.lang.sl.grammar.ExistsNode;
import jade.semantics.lang.sl.grammar.ForallNode;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.ListOfFormula;
import jade.semantics.lang.sl.grammar.ListOfNodes;
import jade.semantics.lang.sl.grammar.MetaTermReferenceNode;
import jade.semantics.lang.sl.grammar.NotNode;
import jade.semantics.lang.sl.grammar.OrNode;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.tools.MatchResult;
import jade.semantics.lang.sl.tools.SL;
import jade.util.leap.ArrayList;
import jade.util.leap.Set;

/**
 * The {@link HornClauseFilter} makes it possible to process the assertion of
 * Horn clauses (like in Prolog). Horn clauses are disjunction of exactly one
 * positive literal (the "head") and one or more negative literals (the "body").
 * In other words, they are material implications, the right hand-side of which
 * (i.e. its consequent) is a single positive literal (and not a disjunction).
 * For example:
 * <pre>
 * (B ??myself (implies (and p1 p2 ... pn)
 *                      q))
 * </pre>
 * Which is logically equivalent to:
 * <pre>
 * (B ??myself (or (not p1) (not p2) ... (not pn))
 *                 q)
 * </pre>
 * <p>
 * The application of this filter consists in installing a proper query filter
 * for each asserted Horn clause. The installed query filter simply replaces
 * queries on the head of the asserted Horn clause (i.e. the only positive
 * literal of the clause) with queries on the conjunction of its body literals
 * (i.e. the negative literals of the clause).
 * </p>
 * <p>
 * The {@link HornClauseFilter} is automatically loaded by the
 * {@link DefaultFilterKBaseLoader}, so that default semantics agents benefit
 * from it.
 * </p>
 * 
 * @author Thierry Martinez - France Telecom
 * @author Vincent Louis - France Telecom
 * @since JSA 2
 */
public class HornClauseFilter extends KBAssertFilter {
		
	static int count=0;

	/**
	 * The {@link #apply(Formula)} method applies on asserted Horn clauses. It
	 * consists in intalling dynamically a query filter that deals with queries
	 * on the head of the asserted Horn clause.
	 *  
	 * {@inheritDoc}
	 */
	@Override
	public Formula apply(Formula formula) {
		if ((formula instanceof BelieveNode)
				&& ((BelieveNode) formula).as_agent().equals(myKBase.getAgentName())
				&& ((BelieveNode) formula).as_formula() instanceof OrNode) {
			OrNode or = (OrNode) ((BelieveNode) formula).as_formula();
			ListOfFormula literals = or.getLeaves();
			// Look for the head of the Horn clause (only positive literal)
			// and its body (all other necessarily negative literals)
			Formula head = null;
			for (int i=literals.size()-1; i>=0; i--) {
				Formula literal = literals.element(i);
				if (literal instanceof NotNode) {
					literals.replace(i, ((NotNode)literal).as_formula());
				}
				else if (literal instanceof ForallNode) {
					literals.replace(i, new ExistsNode(
							((ForallNode)literal).as_variable(),
							new NotNode(((ForallNode)literal).as_formula())));
				}
				else if (head == null) {
					head = literal;
					literals.remove(i);
				}
				else {
					head = null;
					break;
				}
			}
			// If a regular Horn clause was found, install a proper query filter
			if (head != null) {
				myKBase.addKBQueryFilter(new HornClauseQueryFilter(myKBase.getAgentName(), head, SL.and(literals)));
				return SL.TRUE;
			}
		}
		return formula;
	}
	
	/**
	 * Private class, which implements the query filter installed for each
	 * asserted Horn clause.
	 * 
	 * @author Vincent Louis - France Telecom
	 *
	 */
	class HornClauseQueryFilter extends KBQueryFilter {
		
		Formula head;
		Formula body;
		String stringRepresentation;
		
		HornClauseQueryFilter(Term agentName, Formula head, Formula body) {
			this.head = new BelieveNode(agentName, head).getSimplifiedFormula();
			this.body = body.getSimplifiedFormula();
			stringRepresentation = head.toString() + " <- " + this.body.toString();
			ListOfNodes metaRefs = new ListOfNodes();
			this.body.childrenOfKind(SL.META_REFERENCE_CLASSES, metaRefs);
			for (int i=0; i<metaRefs.size(); i++) {
				SL.setMetaReferenceName(metaRefs.get(i),
						"#HORN" + count++);// + SL.getMetaReferenceName(metaRefs.get(i)));
			}
		}
		
		@Override
		public QueryResult apply(Formula formula, ArrayList falsityReasons, BoolWrapper goOn) {
			MatchResult match = head.match(formula);
			if (match != null) {
				QueryResult result = myKBase.query((Formula)SL.instantiate(body, match), falsityReasons);
				if (result != null) {
					for (int i=result.size()-1; i>=0; i--) {
						MatchResult solution = result.getResult(i);
						for (int j=solution.size()-1 ; j>=0 ; j--) {
							if (((String)solution.get(j).getAttribute(MetaTermReferenceNode.lx_name_ID))
									.startsWith("#HORN")) {
								solution.remove(j);
							}
						}
						if (solution.isEmpty()) {
							result.remove(i);
						}
					}
				}
				return result;
			}
			return QueryResult.UNKNOWN;
		}
		
		@Override
		public ArrayList toStrings() {
			ArrayList result = new ArrayList(1);
			result.add(stringRepresentation);
			return result;
		}
		
		@Override
		public boolean getObserverTriggerPatterns(Formula formula, Set set) {
			MatchResult match = head.match(formula);
			if ( match != null ) {
				if (body instanceof AndNode) {
					ListOfFormula literals = ((AndNode)body).getLeaves();
					for (int i=0; i<literals.size(); i++) {
						set.add(SL.instantiate(literals.element(i), match));
					}
				}
				else {
					set.add(SL.instantiate(body, match));
				}
				return false;
	        }
	        return true;
		}
	}
}
