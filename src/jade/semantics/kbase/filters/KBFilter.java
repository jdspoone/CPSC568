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
 * KBFilter.java
 * Created on 15 d�c. 2004
 * Author : louisvi
 */
package jade.semantics.kbase.filters;

import jade.semantics.kbase.KBase;

/**
 * General object that represents a belief base filter.
 * @author Vincent Louis - France Telecom
 * @version 0.9
 */
public class KBFilter {
    
    /**
     * Belief base onto apply this filter
     */
    protected FilterKBase myKBase;
    
    /**
     * Returns the belief base
     * @return Returns the myKBase.
     */
    public KBase getMyKBase() {
        return myKBase;
    } 
    
    /**
     * Sets the belief base
     * @param myKBase The myKBase to set.
     */
    public void setMyKBase(FilterKBase myKBase) {
        this.myKBase = myKBase;
    }   
} 
