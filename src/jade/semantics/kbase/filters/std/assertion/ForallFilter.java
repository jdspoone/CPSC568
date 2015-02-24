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
 * ForallFilter.java
 * Created on 18 nov. 2005
 * Author : Vincent Pautret
 */
package jade.semantics.kbase.filters.std.assertion;

import jade.semantics.kbase.filters.KBAssertFilter;
import jade.semantics.lang.sl.grammar.AtomicFormula;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.NotNode;
import jade.semantics.lang.sl.grammar.TrueNode;
import jade.semantics.lang.sl.tools.MatchResult;
import jade.semantics.lang.sl.tools.SL;

/**
 * @author Vincent Pautret - France Telecom
 * @version 0.9
 */
public class ForallFilter extends KBAssertFilter {

    protected Formula pattern;
    protected Formula pattern2;
    /**
     * Constructor of the filter. Instantiates the patterns.
     */
    public ForallFilter() {
        pattern = SL.formula("(B ??agt (forall ??var ??phi))");
        pattern2 = SL.formula("(= (all ??var (not ??phi)) (set))");
    } // End of ForallFilter/0
    
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
                Formula phi = applyResult.getFormula("phi");
                if ( ((phi instanceof AtomicFormula) && !(phi instanceof TrueNode)) ||
                        ((phi instanceof NotNode) && (((NotNode)phi).as_formula() instanceof AtomicFormula))) {
                myKBase.assertFormula((Formula)SL.instantiate(pattern2, "??var", applyResult.getVariable("var"),
                        "phi", applyResult.getFormula("phi")));
                return SL.TRUE;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return formula;
    } // End of beforeAssert/1
}
