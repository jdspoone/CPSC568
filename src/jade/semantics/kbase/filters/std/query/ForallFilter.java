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
 * Created on 24 oct. 2005
 * Author : Vincent Pautret
 */
package jade.semantics.kbase.filters.std.query;

import jade.semantics.kbase.QueryResult;
import jade.semantics.kbase.filters.KBQueryFilter;
import jade.semantics.lang.sl.grammar.BelieveNode;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.NotNode;
import jade.semantics.lang.sl.tools.MatchResult;
import jade.semantics.lang.sl.tools.SL;
import jade.util.leap.ArrayList;
import jade.util.leap.Set;

/**
 * This filter applies when an "forall formula" is asserted in the belief base.
 * @author Vincent Pautret - France Telecom
 * @version 0.9
 */
public class ForallFilter extends KBQueryFilter {

    /**
     * Pattern that must match to apply the filter
     */
    private Formula pattern1;
    private Formula pattern2;
    /*********************************************************************/
    /**                         CONSTRUCTOR                             **/
    /*********************************************************************/
    
    /**
     * Creates a new Filter on the pattern (forall ??var ??phi)
     */
    public ForallFilter() {
        pattern1 = SL.formula("(B ??agent (forall ??var ??phi))");
        pattern2 = SL.formula("(forall ??var ??phi)");
    } 
    
    /*********************************************************************/
    /**                         METHODS                                 **/
    /*********************************************************************/
    
    /** 
     * If the formula matches the pattern (B ??agent (forall ??var ??phi))
     * and ??agent equals the given agent and ??phi is a mental attitude of this
     * agent, the method returns {@link QueryResult#KNOWN} if the answer to the 
     * query on the opposite of incoming formula (not (phi)) is null and 
     * (not (phi)) is a closed formula. It return {@link QueryResult#UNKNOWN}
     * otherwise.
     * @param formula a formula on which the filter is tested
     * @param agent a term that represents the agent is trying to apply the filter
     * @return a QueryResult as described above.
     */
    @Override
	public QueryResult apply(Formula formula, ArrayList falsityReasons, QueryResult.BoolWrapper goOn) {
        QueryResult queryResult = QueryResult.UNKNOWN;
        try {
            MatchResult applyResult = SL.match(pattern1,formula);
            if (applyResult == null || !myKBase.getAgentName().equals(applyResult.getTerm("agent"))) {
                applyResult = SL.match(pattern2,formula);
            }

            if (applyResult != null) {
                goOn.setBool(false); // Further filters must not be applied
                Formula form = (Formula)SL.toPattern(new NotNode(applyResult.getFormula("phi")).getSimplifiedFormula(), 
                        applyResult.getVariable("var"), applyResult.getVariable("var").lx_name());
                QueryResult formResult = myKBase.query(form, new ArrayList());
                if (formResult == QueryResult.UNKNOWN) {
                	if (myKBase.isClosed(form, null)) {
                		queryResult = QueryResult.KNOWN;
                	}
                	else {
                		falsityReasons.add(new NotNode(new BelieveNode(myKBase.getAgentName(), formula)).getSimplifiedFormula());
                	}
                }
                else {
                	// TODO add each instantiation of form with bindings within formResult
                	for (int i=0; i<formResult.size(); i++) {
                		falsityReasons.add(SL.instantiate(form, formResult.getResult(i)));
                	}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return queryResult;
    } 
    
    /**
     * By default, this method does nothing. 
     * @param formula an observed formula
     * @param set set of patterns. Each pattern corresponds to a kind a formula
     * which, if it is asserted in the base, triggers the observer that
     * observes the formula given in parameter.
     */
    @Override
	public boolean getObserverTriggerPatterns(Formula formula, Set set) {
    	return true;
    }
}
