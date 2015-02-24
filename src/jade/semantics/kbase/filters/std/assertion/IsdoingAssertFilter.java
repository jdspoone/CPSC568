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

package jade.semantics.kbase.filters.std.assertion;

/*
 * IsdoingFilter.java
 * Created on 12 nov. 2007
 * Author : Carole Adam
 */

import jade.semantics.kbase.filters.KBAssertFilter;
import jade.semantics.kbase.filters.std.DefaultFilterKBaseLoader;
import jade.semantics.lang.sl.grammar.BelieveNode;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.ListOfTerm;
import jade.semantics.lang.sl.grammar.MetaTermReferenceNode;
import jade.semantics.lang.sl.grammar.NotNode;
import jade.semantics.lang.sl.grammar.PredicateNode;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.tools.MatchResult;
import jade.semantics.lang.sl.tools.SL;

/**
 * This filter prevents from asserting (B myself (not (is_doing myself p)))
 * and only asserts (not (B myself (is_doing myself p))) 
 * 
 * Filter added to all kinds of agents, see {@link DefaultFilterKBaseLoader}
 * 
 * TODO: this filter is never used the pattern is false (should be encapsulated in a belief) FIXME
 * 
 * @author Carole Adam - France Telecom
 * @version 0.9
 */
public class IsdoingAssertFilter extends KBAssertFilter {
    
    protected Formula pattern;
    
    /**
     * Constructor of the filter. Instantiates the patterns.
     */
    public IsdoingAssertFilter() {
        pattern = SL.formula("(is_doing ??myself ??phi)");
    } 
    
    /**
     * If the filter is applicable, asserts in the belief base the
     * weaker version, and returns a <code>TrueNode</code>.
     * @param formula a formula to assert
     * @return TrueNode if the filter is applicable, the given formula otherwise.
     */
    @Override
	public final Formula apply(Formula formula) {
        try {
            MatchResult applyResult = SL.match(pattern, formula);
            if (applyResult != null) {
                myKBase.assertFormula(
                		new NotNode(
                				new BelieveNode(
                						new MetaTermReferenceNode("myself"),
                						new PredicateNode(
                								SL.symbol("is_doing"),
                								new ListOfTerm(new Term[] {new MetaTermReferenceNode("myself"),applyResult.getTerm("phi")})
                						)
                				)
                		)
                );
                return SL.TRUE;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return formula;
    }

}
