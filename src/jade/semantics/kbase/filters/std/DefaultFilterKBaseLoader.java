/******************************************************************
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
* created on 12 mars 07 by Thierry Martinez
* modified by Carole Adam, november 2007
*/

package jade.semantics.kbase.filters.std;

import jade.semantics.kbase.filters.FilterKBase;
import jade.semantics.kbase.filters.FilterKBaseLoader;
import jade.semantics.kbase.filters.std.assertion.AllIREFilter;
import jade.semantics.kbase.filters.std.assertion.AndFilter;
import jade.semantics.kbase.filters.std.assertion.IsdoingAssertFilter;
import jade.semantics.kbase.filters.std.builtins.CardFunction;
import jade.semantics.kbase.filters.std.builtins.ConcatFunction;
import jade.semantics.kbase.filters.std.builtins.DifferenceFunction;
import jade.semantics.kbase.filters.std.builtins.DivisionFunction;
import jade.semantics.kbase.filters.std.builtins.LesserThanOrEqualPredicate;
import jade.semantics.kbase.filters.std.builtins.LesserThanPredicate;
import jade.semantics.kbase.filters.std.builtins.MemberPredicate;
import jade.semantics.kbase.filters.std.builtins.NowFunction;
import jade.semantics.kbase.filters.std.builtins.ProductFunction;
import jade.semantics.kbase.filters.std.builtins.SumFunction;
import jade.semantics.kbase.filters.std.builtins.YearFunction;
import jade.semantics.kbase.filters.std.query.ExistsFilter;
import jade.semantics.kbase.filters.std.query.ForallFilter;
import jade.semantics.kbase.filters.std.query.IREFilter;
import jade.semantics.kbase.filters.std.query.IsdoingQueryFilter;
import jade.semantics.kbase.filters.std.query.OrFilter;

public class DefaultFilterKBaseLoader implements FilterKBaseLoader {
	
	public void load(FilterKBase kbase) {
		// Add several filters to handle event memory beliefs
		kbase.addFiltersDefinition(new EventMemoryFilters());
	       
		// Add other filters
		kbase.addFiltersDefinition(new CFPFilters(kbase));

		kbase.addKBQueryFilter(new IREFilter());
		kbase.addKBQueryFilter(new jade.semantics.kbase.filters.std.query.AndFilter());
		kbase.addKBQueryFilter(new OrFilter());
		kbase.addKBQueryFilter(new ExistsFilter());
		kbase.addKBQueryFilter(new ForallFilter());
		kbase.addKBAssertFilter(new AllIREFilter());
		kbase.addKBAssertFilter(new AndFilter());
		kbase.addKBAssertFilter(new jade.semantics.kbase.filters.std.assertion.ForallFilter());
		
		kbase.addKBAssertFilter(new IsdoingAssertFilter());
		kbase.addKBQueryFilter(new IsdoingQueryFilter());
		
		kbase.addKBAssertFilter(new HornClauseFilter());
		
		// Add built-in predicates
		BuiltInPredicateFilters builtInPredicates = BuiltInPredicateFilters.getInstance(kbase);		
		builtInPredicates.addPredicate(new MemberPredicate());
		builtInPredicates.addPredicate(new LesserThanPredicate());
		builtInPredicates.addPredicate(new LesserThanOrEqualPredicate());
		builtInPredicates.addPredicate(new CardFunction());
		builtInPredicates.addPredicate(new ConcatFunction());
		builtInPredicates.addPredicate(new SumFunction());
		builtInPredicates.addPredicate(new DifferenceFunction());
		builtInPredicates.addPredicate(new ProductFunction());
		builtInPredicates.addPredicate(new DivisionFunction());
		builtInPredicates.addPredicate(new NowFunction());
		builtInPredicates.addPredicate(new YearFunction());

		// Add filters to handle nested beliefs
		
		// FIXME filters for nested beliefs are not added
		
		// institutional filters transferred to InstitutionalCapabilities
		// Carole Adam, 28 November 2007
		
//        kbase.addFiltersDefinition(new NestedBeliefFilters() {
//			/* (non-Javadoc)
//        	 * @see jade.semantics.kbase.filters.NestedBeliefFilters#newInstance()
//        	 */
//        	public KBase newInstance(Term agent) {
//				return new FilterKBaseImpl(new ArrayListKBaseImpl(agent));
//        	} 
//        });
	}

}

