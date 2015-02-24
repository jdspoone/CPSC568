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
package jade.semantics.kbase.filters.std;

import jade.semantics.lang.sl.grammar.FunctionalTermParamNode;

import java.io.IOException;
import java.io.Reader;

/**
 * @author Thierry Martinez
 * This kind of table aims at storing functional terms, so that they can be queried
 * with special implicit predicates. 
 * Functional terms that can be stored in this table must be instances of
 * FunctionalTermParamNode, which means they have 
 * the following form (name :param1 value1 :param2 value2 :param3 value3 ...).
 * To issue query about this fact, the implicit predicates to use look like
 * (name:param1&param2&param3 gid value1 value2 value3) where
 * gid is a global identifier, value1 to value3 can be both a constant or a variable.
 * 
 * To use such a table, one just has to create a table instance as
 * a field member of his agent class, then to retrieve and add the associated
 * query filter to the agent's knowledge base, as shown in the example hereunder :
 * 
 * public class MyAgent extends SemanticAgent {
 *
 *	FunctionalTermTable storage;
 *	
 *	class MainSemanticCapabilities extends DefaultCapabilities {
 *		protected KBase setupKbase() {
 *			FilterKBase kb = (FilterKBase) super.setupKbase();		
 *			storage = new FunctionalTermTableImpl(kb);						
 *			return kb;
 *		}
 *	}
 *	
 *	public Main() {
 *		semanticCapabilities = new MainSemanticCapabilities();
 *	}
 *	
 *	public void setup() {
 *		super.setup();
 *		storage.load((String)getArguments()[0]);
 *	}
 *}
 */
public interface FunctionalTermTable {
	
	/** 
	 * Adds a functional term in the table
	 * @param term the term to add
	 */
	public String add(FunctionalTermParamNode term);
	
	/**
	 * Loads functional terms from a given reader
	 * @param r the reader where to read the functional terms
	 * @throws IOException
	 * @see public void load(String file)
	 */
	public void load(Reader r) throws IOException;
	
	/**
	 * Loads functional terms from a given file
	 * @param file the name of the file where to read the terms
	 */
	public void load(String file);
}