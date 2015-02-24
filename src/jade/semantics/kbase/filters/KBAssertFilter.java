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
 * KBAssertFilter.java
 * Created on 16 nov. 2004
 * Author : Vincent Pautret
 */
package jade.semantics.kbase.filters;

import jade.semantics.lang.sl.grammar.Formula;

/**
 * This class provides methods the developer has to subclass to create
 * a new filter for asserting belief into the belief base 
 * @author Vincent Pautret - France Telecom
 * @version 0.9
 */
public class KBAssertFilter extends KBFilter {
    
    /**
     * Applies the filter before asserting the formula into the KBase.
     * This method may modify the formula to assert by returning another formula.
     * By default, it returns the same formula. The boolean <code>mustApplyAfter</code>
     * is set to true.<br> 
     * Should be overridden.
     * @param formula the formula to assert
     * @return the formula to assert into the KBase
     */
    public Formula apply(Formula formula) {
        return formula;
    } 
    
    /**
     * This method is deprecated and does nothing.
     * Use the new <code>apply</code> method instead.
     * @param formula
     * @return
     * @deprecated
     */
    @Deprecated
	final public Formula beforeAssert(Formula formula) {
    	return null;
    }
    
    /**
     * This method is deprecated and does nothing. 
     * Do not use any more: it is no more invoked by the JSA framework!
     * @param formula
     * @deprecated 
     */
    @Deprecated
	final public void afterAssert(Formula formula) {
    } 
    
} 
