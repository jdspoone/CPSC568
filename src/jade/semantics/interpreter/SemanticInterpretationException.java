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
 * SemanticInterpretationException.java
 * Created on 13 mai 2005
 * Author : Vincent Pautret
 */
package jade.semantics.interpreter;

import jade.semantics.lang.sl.grammar.Term;

/**
 * Exception raised during the interpretation process.
 * @author Vincent Pautret - France Telecom
 * @version 0.9
 */
public class SemanticInterpretationException extends Exception {
	
    /**
     * Reason of the exception
     */
    private String reason;
    
    /**
     * Object of the exception
     */
    private Term object;
    
    /**
     * Creates a new SemanticInterpretationException
     * @param reason the reason of the exception
     */
    public SemanticInterpretationException(String reason) {
        this(reason, null);
    } // End of SemanticInterpretationException/1
    
    /**
     * Creates a new SemanticInterpretationException
     * @param reason the reason of the exception
     * @param object the object of the exception
     */
    public SemanticInterpretationException(String reason, Term object) {
        super();
        this.reason = reason;
        setObject(object);
    } // End of SemanticInterpretationException/2
    
    /**
     * Returns the object of the exception
     * @return the object of the exception
     */ 
    public Term getObject() {
        return object;
    } // End of getObject/0
    
    /**
     * Returns the reason of the exception
     * @return the reason of the exception
     */
    public String getReason() {
        return reason;
    } // End of getReason/0
    
    /**
     * Sets the object of the exception
     * @param object the object of the exception
     */
    protected void setObject(Term object) {
        this.object = object;
    } // End of setObject/1
} // End of class
