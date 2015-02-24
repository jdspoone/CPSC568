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
 * KBQueryFilter.java
 * Created on 16 nov. 2004
 * Author : Vincent Pautret
 */
package jade.semantics.kbase.filters;

import jade.semantics.kbase.KBase;
import jade.semantics.kbase.QueryResult;
import jade.semantics.lang.sl.grammar.Formula;
import jade.util.leap.ArrayList;
import jade.util.leap.Set;

/**
 * This abstact class provides methods the developer has to override to create
 * a new filter for querying the belief base about a formula.
 * @author Vincent Pautret - France Telecom
 * @version 0.9
 */
public abstract class KBQueryFilter extends KBFilter {
    
    /**
     * Returns a {@link QueryResult} object, similar to the one returned by the query
     * operation of the {@link KBase} interface. Such an objet can be
     * - {@link QueryResult#UNKNOWN} if the filter is not applicable or
     * returns no solution, 
     * - {@link QueryResult#KNOWN} if the filter returns
     * a solution and the formula holds no meta-variable, 
     * - a not empty {@link QueryResult} object if the filter returns
     * solutions and the formula holds meta-variables.
     * @param formula a formula on which the filter is tested
     * @param falsityReasons a list of believed formulas, which must be filled to explain
     *                a null result of the method. If the filter is actually not
     *                applicable, this list must not be modified.
     *                Warning: when calling the method, this parameter should not be null
     * @return a QueryResult as previously described. 
     * @see KBase#query(Formula)
     */
    public abstract QueryResult apply(Formula formula, ArrayList falsityReasons, QueryResult.BoolWrapper goOn);
    
    /**
     * Fills the set given in parameter with the patterns manipulated by this 
     * filter and likely to trigger the observers which observe the formula given
     * in parameter.
     * @param formula an observed formula
     * @param set set of patterns. Each pattern corresponds to a kind a formula
     * which, if it is asserted in the base, triggers the observer that
     * observes the formula given in parameter.
     * 
     * @return : FIXME missing javadoc, explain what does the returned boolean mean 
     */
    public abstract boolean getObserverTriggerPatterns(Formula formula, Set set);
    
    public ArrayList toStrings() {
    	ArrayList ret = new ArrayList(1);
    	ret.add(getClass().getCanonicalName());
    	return ret;
//    	return null;
    }
} 
