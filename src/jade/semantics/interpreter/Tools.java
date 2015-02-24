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
 * Tools.java
 * Created on 23 juin 2005
 * Author : Vincent Pautret
 */
package jade.semantics.interpreter;

import jade.core.AID;
import jade.semantics.actions.CommunicativeAction;
import jade.semantics.actions.SemanticAction;
import jade.semantics.lang.sl.grammar.ActionExpression;
import jade.semantics.lang.sl.grammar.ListOfTerm;
import jade.semantics.lang.sl.grammar.MetaTermReferenceNode;
import jade.semantics.lang.sl.grammar.StringConstantNode;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.grammar.TermSequenceNode;
import jade.semantics.lang.sl.grammar.WordConstantNode;
import jade.semantics.lang.sl.tools.MatchResult;
import jade.semantics.lang.sl.tools.SL;
import jade.util.leap.Iterator;

/**
 * Utility class.
 * @author Vincent Pautret - France Telecom
 * @version 0.9
 */
public class Tools {
    
    /**
     * General pattern used to recognize functional terms denoting
     * agent descriptors
     */
    static final public Term AGENT_IDENTIFIER_PATTERN = SL
    .term("(agent-identifier " +
                "(::? :addresses ??addresses) " +
                ":name ??name " +
                "(::? :resolvers ??resolvers))");
    
    /***************************************************************************
     * STATIC METHODS /
     **************************************************************************/
    
    /**
     * Returns the AID corresponding to the term representing an agent. Returns 
     * null if the term is not an agent or if an exception occurs.
     * @param agentTerm a term representing an agent 
     * @return the AID corresponding to the term representing an agent 
     */
    public static AID term2AID(Term agentTerm) {
        AID result = null;
        ////////////////////////////////////////////////////////////////////
        // added specific management of MetaTermReferenceNode having a value
        // modification by VL, 31January08 - see SLMatcher for debug
		while (agentTerm instanceof MetaTermReferenceNode) {
//			System.out.println("loop");
			agentTerm = ((MetaTermReferenceNode)agentTerm).sm_value();
		}
		// end modification VL
		////////////////////////////////////////////////////////////////////
        MatchResult matchResult = SL.match(AGENT_IDENTIFIER_PATTERN, agentTerm);
        if (matchResult != null) {
            try {
                result = new AID(((WordConstantNode)matchResult.getTerm("name")).stringValue(), true);
                Term addresses = matchResult.getTerm("addresses");
	            if (addresses != null && addresses instanceof TermSequenceNode) {
                    for (int i = 0 ; i < ((TermSequenceNode)addresses).as_terms().size() ; i++) {
                        result.addAddresses(((StringConstantNode)((TermSequenceNode)addresses).as_terms().get(i)).stringValue());
                    }
                }
                Term resolvers = matchResult.getTerm("resolvers");
                if (resolvers != null && resolvers instanceof TermSequenceNode) {
                    for (int i = 0 ; i < ((TermSequenceNode)resolvers).as_terms().size() ; i++) {
                        result.addResolvers(term2AID((Term)((TermSequenceNode)resolvers).as_terms().get(i)));
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                result = null;
            }
        }
        return result;
    } // End of term2AID/1
    
    /**
     * Returns the term representing an agent to the corresponding AID. Returns 
     * null if an exception occurs.
     * @param aid an AID
     * @return a term representing an agent, or null.
     */
    public static Term AID2Term(AID aid) {
        TermSequenceNode addresses = null;
        TermSequenceNode resolvers = null;
        Iterator aidIterator = aid.getAllAddresses();
        if (aidIterator.hasNext()) {
            addresses = new TermSequenceNode(new ListOfTerm());
            while (aidIterator.hasNext()) {
                addresses.as_terms().add(SL.string((String)aidIterator.next()));
            }
        }
        aidIterator = aid.getAllResolvers();
        if (aidIterator.hasNext()) {
            resolvers = new TermSequenceNode(new ListOfTerm());
            while (aidIterator.hasNext()) {
                resolvers.as_terms().add(AID2Term((AID)aidIterator.next()));
            }
        }
        try {
            Term result = (Term)AGENT_IDENTIFIER_PATTERN.getClone();            
            SL.set(result, "name", SL.word(aid.getName()));
            if (addresses != null) SL.set(result, "addresses", addresses);
            if (resolvers != null) SL.set(result, "resolvers", resolvers);
            SL.substituteMetaReferences(result);
            SL.removeOptionalParameter(result);
            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    } // End of AID2Term/1
    
    
    /**
     * Tests if the action expression given in parameter is a communicative 
     * action from the semantic agent (me) to the specified receiver.
     * @param actionExp an action expression
     * @param receiver the receiver
     * @param me the current semantic agent 
     * @return true if the the action expression given in parameter is a 
     * communicative action from the semantic agent (me) to the specified 
     * receiver, false if not.
     */
    public static boolean isCommunicativeActionFromMeToReceiver(ActionExpression actionExp, Term receiver, SemanticCapabilities capabilities) {
    	SemanticAction action = null;
    	try {
    		action = capabilities.getMySemanticActionTable().getSemanticActionInstance(actionExp);
    	} catch (SemanticInterpretationException sie) {
        	// Action does not exist within the agent table. FIXME
    		return false;
    	}
    	if (action instanceof CommunicativeAction) {
    		return (action.getActor().equals(capabilities.getAgentName()) &&
    				((CommunicativeAction)action).getReceiverList().contains(receiver));
    	}
    	//else {
    		return false;
    	//}
    } 
    
    
    
	/*****************
	 * DEBUG METHODS *
	 *****************/
    // added by CA 16 June 2008 (copied from InstitutionTools)
    // to be used in classes external to the institutions extension
    // in order to keep them independent from the extension
    
	public static void printTraceMessage(String message, boolean DEBUG) {
		if (DEBUG) {
			System.err.println("JIA! "+message);
		}
	}
} // End of class Tools
