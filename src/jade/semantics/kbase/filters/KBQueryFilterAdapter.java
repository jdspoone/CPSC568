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
 * KBQueryFilterAdapter.java
 * Created on 16 nov. 2004
 * Author : Vincent Pautret
 */

package jade.semantics.kbase.filters;

import jade.semantics.kbase.QueryResult;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.tools.MatchResult;
import jade.semantics.lang.sl.tools.SL;
import jade.util.leap.ArrayList;
import jade.util.leap.Set;

/**
 * Adapter of <code>KBQueryFilter</code>
 * @author Vincent Pautret - France Telecom
 * @version 0.9
 * 
 * TODO process the reasons in the apply method
 * 
 */
public abstract class KBQueryFilterAdapter extends KBQueryFilter {
    
    /**
     * Pattern that must match to apply the filter adapter
     */
    
    protected Formula pattern;
    
    /**
     * Creates a new filter.
     * @param pttrn a pattern. The pattern must contain a variable named "??agent" 
     * representing the semantic agent itself.
     */
    public KBQueryFilterAdapter(String pttrn) {
        this(SL.formula(pttrn));
    }
    
	/**
	 * Creates a new Filter
     * @param formula a pattern. The pattern must contain a variable named "??agent" 
     * representing the semantic agent itself.
     */
    public KBQueryFilterAdapter(Formula formula) {
        pattern = formula;
    }
    
    /**
     * TO BE REWRITTEN TO BE REWRITTEN TO BE REWRITTEN
     * By default, this method returns false as first element in the array,
     * meaning the filter is not applicable, and null as second element.
     * The result is the one of the doApply/2 method. 
     * @param formula the formula on which the filter is applied
     * @param agent a term that represents the agent is trying to apply the filter
     * @return false as first element, and the result of doApply/2 as second element.
     */
    @Override
	final public QueryResult apply(Formula formula, ArrayList falsityReasons, QueryResult.BoolWrapper goOn) {
        MatchResult match = SL.match(pattern, formula);
        if (match != null) {
            MatchResult result = doApply(formula, match);
            if (result != null) {
            	goOn.setBool(false); // TODO to remove 
            	return new QueryResult(result);
            } 
            //else {
                return null;
            //}
        }
        return null; // Assume a filter that doesn't match provide no solution
    } 
    
    /**
     * Performs the filter on the formula <code>formula</code>. By default, 
     * returns <code>null</code>.
     * @param formula a formula
     * @param match the MatchResult corresponding to the match between
     * the incoming formula and the pattern of the filter.
     * @return a list of Bind if the filter is applicable and succeeds, 
     * <code>null</code> if not.
     */
    public MatchResult doApply(Formula formula, MatchResult match) {
        return null;
    } // End of apply/2
    
    /**
     * By default, this method does nothing. 
     * @param formula an observed formula
     * @param set set of patterns. Each pattern corresponds to a kind a formula
     * which, if it is asserted in the base, triggers the observer that
     * observes the formula given in parameter.
     */
    @Override
	public boolean getObserverTriggerPatterns(Formula formula, Set set) {
        MatchResult match = SL.match(formula, pattern);
        if (match != null) {
        	return doGetObserverTriggerPatterns(match, set);
        }
        return true;
    }
    
    public boolean doGetObserverTriggerPatterns(MatchResult match, Set set) {
    	return true;
    }
    
    /**
     * Returns the string representing the pattern
     * @return the string representing the pattern
     */
    @Override
	public String toString() {
        return pattern.toString();
    }
} // End of KBQueryFilterAdapter
