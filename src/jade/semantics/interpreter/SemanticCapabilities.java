/**
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. 
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  
 * The  Knowledge Science Group makes no representations about the suitability
 * of  this software for any purpose.  It is provided "as is" without express
 * or implied warranty.</p>
 * <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
package jade.semantics.interpreter;

import jade.semantics.actions.SemanticAction;
import jade.semantics.lang.sl.grammar.ActionExpression;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 * Dummy class for jade support.
 */
public class SemanticCapabilities {
	
	public class SemanticActionTable {
		public SemanticAction getSemanticActionInstance(ActionExpression actionExp) throws SemanticInterpretationException {
			return null;
		}
	}

	public SemanticActionTable getMySemanticActionTable() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getAgentName() {
		// TODO Auto-generated method stub
		return null;
	}

}
