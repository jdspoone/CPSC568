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
 * FiltersDefinition.java
 * Created on 15 d�c. 2004
 * Author : louisvi
 */
package jade.semantics.kbase.filters;

import jade.util.leap.ArrayList;

/**
 * Sorted list of <code>FilterDefinition</code>.
 * @author Vincent Louis - France Telecom
 * @version 0.9
 */
public class FiltersDefinition extends ArrayList {
    
    
    /**
     * Creates a new FilterDefinition.
     */
    public FiltersDefinition() {}
    
    /**
     * Creates a new FilterDefinition witha list of filters.
     * @param list list of <code>KBFilter</code>
     */
    public FiltersDefinition(ArrayList list) {
        super();
        for (int i=0 ; i<list.size() ; i++) {
            add(new FilterDefinition(-1, (KBFilter)list.get(i)));
        }
    } 
    
    /**
     * Adds a new filter in the list.
     * @param index the specified index to add the filter
     * @param filter a <code>KBFilter</code>
     */
    public void defineFilter(int index, KBFilter filter) {
        add(new FilterDefinition(index, filter));
    } 
    
    /**
     * Adds a new filter at the begining of the list
     * @param filter a <code>KBFilter</code>
     */
    public void defineFilter(KBFilter filter) {
        defineFilter(-1, filter);
    } 
    
    /**
     * Returns the <code>FilterDefinition</code> at the specified index
     * @return the <code>FilterDefinition</code> at the specified index
     * @param i index if the <code>FilterDefinition</code> 
     */
    public FilterDefinition getFilterDefinition(int i) {
        return (FilterDefinition)super.get(i);
    } 
    
} 
