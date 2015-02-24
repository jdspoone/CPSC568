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
 * AndFilter.java
 * Created on 18 nov. 2005
 * Author : Vincent Pautret
 */
package jade.semantics.kbase.filters.std.assertion;

import jade.semantics.kbase.filters.KBAssertFilter;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.tools.MatchResult;
import jade.semantics.lang.sl.tools.SL;

/**
 * This filter asserts in the Kbase the two parts of an And formula.
 * @author Vincent Pautret - France Telecom
 * @version 0.9
 */
public class AndFilter extends KBAssertFilter {
    
    protected Formula pattern;
    
    /**
     * Constructor of the filter. Instantiates the patterns.
     */
    public AndFilter() {
        pattern = SL.formula("(and ??phi ??psi)");
    } // End of AndFilter/0
    
    /**
     * If the filter is applicable, asserts in the belief base each element 
     * which appears in the set, and returns a <code>TrueNode</code>.
     * @param formula a formula to assert
     * @return TrueNode if the filter is applicable, the given formula in the 
     * other cases.
     */
    @Override
	public final Formula apply(Formula formula) {
        try {
            MatchResult applyResult = SL.match(pattern, formula);
            if (applyResult != null) {
                myKBase.assertFormula(applyResult.getFormula("phi"));
                myKBase.assertFormula(applyResult.getFormula("psi"));
                return SL.TRUE;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return formula;
    } // End of beforeAssert/1

}
