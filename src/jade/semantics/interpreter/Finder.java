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
 * Finder.java
 * Created on 9 d�c. 2004
 * Author: louisvi
 */
package jade.semantics.interpreter;

import jade.util.leap.ArrayList;

/**
 * Class that represents general objet that permits object identification.
 * This class should be extended.
 * @author Vincent Louis - France Telecom
 * @version 0.9
 */
public class Finder {
    
    /**
     * Returns true if the object passed in parameter is identified by this 
     * identifier, false in the other case.
     * By default, this methods returns false.
     * @param object the object to be identified
     * @return true if the object passed in parameter is identified by this 
     * identifier, false in the other case.
     */
    public boolean identify(Object object) {
        System.out.println("IDENTIFY GENERIC-OBJECT : " + object);
        return false;
    } // End of identify
    
    /**
     * Removes an object of the list if it is identified.
     * @param list an object list
     */
    public void removeFromList(ArrayList list) {
        for (int i = list.size()-1 ; i >= 0 ; i--) {
            if (identify(list.get(i))) {
                list.remove(i);
            }
        }
    } // End of removeFromList/1
} // End of Identifier
