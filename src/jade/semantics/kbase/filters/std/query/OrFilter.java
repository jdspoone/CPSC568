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
 * OrFilter.java
 * Created on 23 nov. 2004
 * Author : Vincent Pautret
 */
package jade.semantics.kbase.filters.std.query;

import jade.semantics.kbase.QueryResult;
import jade.semantics.kbase.filters.KBQueryFilter;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.tools.MatchResult;
import jade.semantics.lang.sl.tools.SL;
import jade.util.leap.ArrayList;
import jade.util.leap.Set;

/**
 * This filter applies when an "or formula" is asserted in the KBase.
 * @author Vincent Pautret - France Telecom
 * @version 0.9
 */
public class OrFilter extends KBQueryFilter {
    
    /**
     * Pattern that must match to apply the filter
     */
    private Formula pattern;
    private Formula pattern2;
    
    /*********************************************************************/
    /**				 			CONSTRUCTOR								**/
    /*********************************************************************/
    
    /**
     * Creates a new Filter on the pattern (B ??agent (or ??phi ??psi))
     */
    public OrFilter() {
        pattern = SL.formula("(B ??agent (or ??phi ??psi))");
        pattern2 = SL.formula("(or ??phi ??psi)");
    } // End of OrFilter/1
    
    /*********************************************************************/
    /**				 			METHODS									**/
    /*********************************************************************/
    
    /**
     * If the formula matches the pattern (B ??agent (or ??phi ??psi)) and
     * ??agent equals the given agent term, this method returns the union
     * of the whole of solutions of the first part of the formula and 
     * the whole of solutions of the second part of the formula. Else it 
     * returns QueryResult.UNKNOWN.
     * @param formula a formula on which the filter is tested
     * @param agent a term that represents the agent is trying to apply the filter
     * @return a QueryResult object as described above. 
     */
    @Override
	public QueryResult apply(Formula formula, ArrayList falsityReasons, QueryResult.BoolWrapper goOn) {
        try {
            MatchResult applyResult = SL.match(pattern,formula);
            if (applyResult == null || !applyResult.getTerm("agent").equals(myKBase.getAgentName())) {
            	applyResult = SL.match(pattern2,formula);
            }
            
            if (applyResult != null) {
            	goOn.setBool(false); // Further filters must not be applied
            	ArrayList phiReasons = new ArrayList();
                QueryResult phiList = myKBase.query(applyResult.getFormula("phi"), phiReasons);
                if (phiList == QueryResult.UNKNOWN) {
                	QueryResult psiList = myKBase.query(applyResult.getFormula("psi"), falsityReasons);
                	if (psiList == QueryResult.UNKNOWN) {
                		QueryResult.addReasons(falsityReasons, phiReasons);
                	}
                	return psiList;
                }
                //else {
                	QueryResult psiList = myKBase.query(applyResult.getFormula("psi"), new ArrayList());
                	if (psiList == QueryResult.UNKNOWN) {
                		return phiList;
                	}
                	//else {
                		return phiList.union(psiList);
                	//}
                //}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return QueryResult.UNKNOWN;
    } 

    /**
     * Adds in the set, the patterns for the formula phi and for the formula
     * psi.
     * @param formula an observed formula
     * @param set set of patterns. Each pattern corresponds to a kind a formula
     * which, if it is asserted in the base, triggers the observer that
     * observes the formula given in parameter.
     */
    @Override
	public boolean getObserverTriggerPatterns(Formula formula, Set set) {
 	   MatchResult match = SL.match(pattern, formula);  // modif by CA, 26 June 08: inverse order of parameters
 	   if (match != null) {
 		   try {
// 			   System.err.println("or filter, pattern="+pattern+" ; formula="+formula);
// 			   Formula phi = match.getFormula("phi");
// 			   Formula phi2 = match.formula("phi");
// 			   Formula psi = match.getFormula("psi");
// 			   Formula psi2 = match.formula("psi");
// 			   System.err.println("!!! ### !!! match="+match);
// 			   System.err.println("!!!!!!! phi="+phi+" ; psi="+psi);
// 			   System.err.println("!!!!!!! phi2="+phi2+" ; psi2="+psi2);
			   
			   myKBase.getObserverTriggerPatterns(match.getFormula("phi"), set);
			   myKBase.getObserverTriggerPatterns(match.getFormula("psi"), set);
//			   System.err.println("phi="+match.formula("phi")+" ; psi="+match.formula("psi"));
 			   return false;
 		   } catch (SL.WrongTypeException wte) {
 			   wte.printStackTrace();
 		   }
 	   }
 	   return true;
    }
} // End of class OrFilter
