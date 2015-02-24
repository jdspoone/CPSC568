/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
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

package jade.content.onto.annotations;

//#J2ME_EXCLUDE_FILE

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Identifies the result slot of an <code>AgentAction</code>.<br>
 * The annotation is to be applied to the getter method.
 *
 * @see jade.content.AgentAction
 * @author Paolo Cancedda
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Result {

	Class type();
}
