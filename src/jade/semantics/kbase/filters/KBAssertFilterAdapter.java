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
 * KBAssertFilterAdapter.java
 * Created on 16 nov. 2004
 * Author : Vincent Pautret
 */

package jade.semantics.kbase.filters;

import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.tools.MatchResult;
import jade.semantics.lang.sl.tools.SL;

/**
 * Adapter of a <code>KBassertFilter</code>.
 * @author Vincent Pautret - France Telecom
 * @version 0.9
 */
public abstract class KBAssertFilterAdapter extends KBAssertFilter {
        
    /**
     * Pattern that must match to apply the filter adapter
     */
    protected Formula pattern;
    
    /**
     * Creates a new filter.
     * @param pttrn a pattern. The pattern must contain a variable named "??agent" 
     * representing the semantic agent itself.
     */
    public KBAssertFilterAdapter(String pttrn) {
        this(SL.formula(pttrn));
    }
    
	/**
     * Creates a new filter.
     * @param formula a pattern. The pattern must contain a variable named "??agent" 
     * representing the semantic agent itself.
     */
    public KBAssertFilterAdapter(Formula formula) {
        super();
        pattern = formula;
    }
     
	/**
     * Returns true if the pattern of the adapter matches the formula,
     * false if not.
     * @param formula a formula
     * @return true if the pattern of the adapter matches the formula,
     * false if not. 
     * @see KBAssertFilter#apply(Formula)
     */
    @Override
	public final Formula apply(Formula formula) {
        MatchResult match = SL.match(pattern, formula);
//        System.err.println("### pattern = "+pattern);
//        System.err.println("### formula = "+formula);
//        System.err.println("### match = "+match);
        
        if (match != null) {
        	return doApply(formula, match);  
        }
        return formula;
    } // End of isApplicable/2
    
    /**
     * Returns the string representing the pattern.
     * @return the string representing the pattern.
     */
    @Override
	public String toString() {
        return pattern.toString();
    } // End of toString/0
    
    /**
     * Returns the given formula.
     * @param formula the incoming formula
     * @return a formula after the application of the method 
     */
    public Formula doApply(Formula formula, MatchResult match) {
        return formula;
    }
} // End of KBAssertFilterAdapter
