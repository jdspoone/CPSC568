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

package jade.semantics.lang.sl.grammar.operations;

import jade.semantics.lang.sl.grammar.ListOfTerm;
import jade.semantics.lang.sl.grammar.Node;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.grammar.TermSequence;

public class TermSequenceNodeOperations 
	extends TermNodeOperations 
	implements TermSequence.Operations {
	
    @Override
	public void initNode(Node node)
	{
		super.initNode(node);
		if ( ((TermSequence)node).as_terms() == null ) {
			((TermSequence)node).as_terms(new ListOfTerm());
		}
	}

	public void addTerm(TermSequence node, Term term)
	{
		node.as_terms().add(term);
	}
	
    public void removeTerm(TermSequence node, Term term)
	{
		node.as_terms().remove(term);
	}
	
    public Term getTerm(TermSequence node, int i)
	{
		return node.as_terms().element(i);
	}
	
	public int size(TermSequence node) {
		return node.as_terms().size();
	}
}
