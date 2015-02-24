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

package jade.semantics.kbase.filters.std.query;

/*
 * IsdoingFilter.java
 * Created on 12 nov. 2007
 * Author : Carole Adam
 */

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
 * This filter applies when an "not (is_doing myself plan)" 
 * formula is queried to the KBase.
 * @author Carole Adam - France Telecom
 * @version 0.9
 */
public class IsdoingQueryFilter extends KBQueryFilter {
    
    /**
     * Pattern that must match to apply the filter
     */
    private Formula pattern;
    
    /*********************************************************************/
    /**				 			CONSTRUCTOR								**/
    /*********************************************************************/
    
    /**
     * Creates a new Filter on the pattern 
     * (B ??agent (not (is_doing ??myself ??plan)))
     */
    public IsdoingQueryFilter() {
        pattern = SL.formula("(B ??myself (not (is_doing ??myself ??plan)))");
    }
    
    /*********************************************************************/
    /**				 			METHODS									**/
    /*********************************************************************/
    
    /**
     * If the formula matches the pattern 
     * this method returns the result of the query of 
     * (not (B myself (is_doing myself plan))) instead
     * @param formula a formula on which the filter is tested
     * @param agent a term that represents the agent trying to apply the filter
     * @return a QueryResult object as described above. 
     */
    @Override
	public QueryResult apply(Formula formula, ArrayList falsityReasons, QueryResult.BoolWrapper goOn) {
        try {
            MatchResult applyResult = SL.match(pattern,formula);
            /** TODO: check getTerm("myself") !!! **/
            if (applyResult != null && applyResult.getTerm("myself").equals(myKBase.getAgentName())) {
            	goOn.setBool(false); // further filters must not be applied
            	Formula not_believe_isdoing_PLAN = 
            		new NotNode(
            			new BelieveNode(myKBase.getAgentName(),
            				((NotNode)((BelieveNode)pattern).as_formula()).as_formula())
            			);
                // query the fully instantiated formula
            	// the result is an empty set if found, null otherwise
            	return myKBase.query(not_believe_isdoing_PLAN.instantiate("plan",applyResult.getTerm("plan")), falsityReasons);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return QueryResult.UNKNOWN;
    } 

    /**
     * Adds nothing in the set, returns false.
     * @param formula an observed formula
     * @param set set of patterns. Each pattern corresponds to a kind a formula
     * which, if it is asserted in the base, triggers the observer that
     * observes the formula given in parameter.
     */
    @Override
	public boolean getObserverTriggerPatterns(Formula formula, Set set) {
    	return false;
    }
    
} // End of class Filter