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
 * ExistsFilter.java
 * Created on 24 oct. 2005
 * Author : Vincent Pautret
 */
package jade.semantics.kbase.filters.std.query;

import jade.semantics.kbase.QueryResult;
import jade.semantics.kbase.filters.KBQueryFilter;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.MetaTermReferenceNode;
import jade.semantics.lang.sl.tools.MatchResult;
import jade.semantics.lang.sl.tools.SL;
import jade.util.leap.ArrayList;
import jade.util.leap.Set;

/**
 * This filter applies when an "exists formula" is asserted in the belief Base.
 * @author Vincent Pautret - France Telecom
 * @version 0.9
 */
public class ExistsFilter extends KBQueryFilter {
    
    int count = 0;
    
    /**
     * Pattern that must match to apply the filter
     */
    private Formula pattern;
    private Formula pattern2;
    /*********************************************************************/
    /**                         CONSTRUCTOR                             **/
    /*********************************************************************/
    
    /**
     * Creates a new Filter on the pattern (B ??agt (exists ??var ??phi))
     */
    public ExistsFilter() {
        pattern = SL.formula("(B ??agt (exists ??var ??phi))");
        pattern2 = SL.formula("(exists ??var (B ??agt ??phi))");
    } 
    
    /*********************************************************************/
    /**                         METHODS                                 **/
    /*********************************************************************/
    
    /** 
     * If the formula matches the pattern (B ??agent (exists ??var ??phi))
     * and ??agent equals the given agent term, this method returns a 
     * {@link QueryResult} that corresponds to the answer to the query on 
     * the incoming formula. In all the MatchResults of this QueryResult, 
     * the meta variable corresponding to the variable "??var" of the pattern
     * are deleted. Else, it returns {@link QueryResult#UNKNOWN}
     * @param formula a formula on which the filter is tested
     * @param agent a term that represents the agent is trying to apply the filter
     * @return a QueryResult object as described above.
     */
    @Override
	public QueryResult apply(Formula formula, ArrayList falsityReasons, QueryResult.BoolWrapper goOn) {
        QueryResult queryResult = QueryResult.UNKNOWN;
        try {
            MatchResult applyResult = SL.match(pattern,formula);
            if (applyResult == null) {
                applyResult = SL.match(pattern2,formula);
            }
            if (applyResult != null && applyResult.getTerm("agt").equals(myKBase.getAgentName())) {
            	goOn.setBool(false); // Further filters must not be applied
            	String metaName = "#EXISTS" + count++ + applyResult.getVariable("var").lx_name();
                Formula form = (Formula)SL.toPattern(applyResult.getFormula("phi"), 
                        applyResult.getVariable("var"), metaName);
                queryResult = myKBase.query(form, falsityReasons);
                cleanMatches(queryResult, metaName);
            }
         } catch (Exception e) {
            e.printStackTrace();
        }
        return queryResult;
    } 
    
    /**
     * Deletes the variable with the name given as argument from all 
     * MatchResult of the given QueryResult. 
     * @param list a list of MatchResult 
     * @param varName the name of the variable that should be deletes in all the
     * MatchResults of the given QueryResult.
     */
    private void cleanMatches(QueryResult results, String varName) {
        if (results != null) {
            for (int i = 0; i < results.size(); i++) {
                for (int j = 0; j< results.getResult(i).size(); j++) {
                    if (results.getResult(i).get(j).getAttribute(MetaTermReferenceNode.lx_name_ID).equals(varName)) {
                    	results.getResult(i).remove(j); // j--, if we would continue the loop
                        if (results.getResult(i).size() == 0) results.remove(i); // i--, if we would continue the loop
                        break;
                    }
                }
            }
        }
    }

    /**
     * Adds the formula corresponding to phi (see the patterns) where the variable
     * is transform into a MetaVariable. 
     * @param formula an observed formula
     * @param set set of patterns. Each pattern corresponds to a kind a formula
     * which, if it is asserted in the base, triggers the observer that
     * observes the formula given in parameter.
     */
    @Override
	public boolean getObserverTriggerPatterns(Formula formula, Set set) {
        MatchResult applyResult = SL.match(pattern,formula);
        if (applyResult == null) {
            applyResult = SL.match(pattern2,formula);
        }
        try {
            if (applyResult != null) {
				myKBase.getObserverTriggerPatterns((Formula)SL.toPattern(
            			applyResult.getFormula("phi"),
            			applyResult.getVariable("var"),
            			applyResult.getVariable("var").lx_name()), set);
            	return false;
                //set.add(SL.toPattern(applyResult.getFormula("phi"), applyResult.getVariable("var"), applyResult.getVariable("var").lx_name()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
} // End of class ExistsFilter
