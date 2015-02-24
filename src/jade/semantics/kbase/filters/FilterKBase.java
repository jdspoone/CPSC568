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
 * FilterKBase.java
 * Created on 21 mars 2005
 * Author : Vincent Pautret
 */
package jade.semantics.kbase.filters;

import jade.semantics.interpreter.Finder;
import jade.semantics.kbase.KBase;
import jade.semantics.lang.sl.grammar.Formula;
import jade.util.leap.Set;

/**
 * Interface that defines a belief base based upon filters. These filters are
 * used to access the base.
 * @author Vincent Pautret - France Telecom
 * @version 0.9
 */
public interface FilterKBase extends KBase {
    
    /**
     * Value to add a filter at the beginning of the list of filters
     */
    public int FRONT = 0;
    
    /**
     * Value to add a filter at the end of the list of filters
     */
    public int END = Integer.MAX_VALUE; 
    
    /**
     * Adds an assert filter to the belief base
     * @param assertFilter an assert filter
     */
    public void addKBAssertFilter(KBAssertFilter assertFilter);

    /**
     * Adds an assert filter to the belief base at the specified index. It is 
     * possible the <code>FRONT</code> or <code>END</code> constants to put
     * the filter at the beginning of the lits or at the end of the list. 
     * @param assertFilter an assert filter
     * @param index the index in the list of filters to add a new filter 
     */
    public void addKBAssertFilter(KBAssertFilter assertFilter, int index);
    
    /**
     * Removes the assert filters that are identified by 
     * the specified finder. 
     * @param finder a finder
     */
    public void removeKBAssertFilter(Finder finder);
    
    /**
     * Adds a query filter to the belief base
     * @param queryFilter a queryFilter
     */
    public void addKBQueryFilter(KBQueryFilter queryFilter);
    
    /**
     * Adds a query filter to the belief base at the specified index. It is 
     * possible the <code>FRONT</code> or <code>END</code> constants to put
     * the filter at the beginning of the lits or at the end of the list.
     * @param queryFilter a queryFilter
     * @param index the index in the list of filters to add a new filter
     */
    public void addKBQueryFilter(KBQueryFilter queryFilter, int index);

    /**
     * Removes the query filters that are identified by 
     * the specified finder. 
     * @param finder a finder
     */
    public void removeKBQueryFilter(Finder finder);
    
    /**
     * Adds a list of filters to the KBase (useful for defining specific predicate management)
     * @param filtersDefinition the list of filters
     */
    public void addFiltersDefinition(FiltersDefinition filtersDefinition);
    
    /**
     * Calls the getObserverTriggerPatterns method of each query filters.
     * At least, the pattern corresponding to the observed formula
     * is returned.
     * @param formula an observed formula
     * @return result set of patterns. Each pattern corresponds to a kind a formula
     * which, if it is asserted in the base, triggers the observer that
     * observes the formula given in parameter.
     */
    public void getObserverTriggerPatterns(Formula formula, Set result);

} 
