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
 * SemanticAction.java
 * Created on 28 oct. 2004
 * Author : Vincent Pautret
 */
package jade.semantics.actions;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.semantics.interpreter.SemanticCapabilities;
import jade.semantics.interpreter.SemanticInterpretationException;
import jade.semantics.lang.sl.grammar.ActionExpression;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.Term;


/**
 * SemanticAction is the interface of all semantic actions that a Semantic Agent
 * may perform. Each semantic action must provide the following pieces of 
 * information, to be usable by the semantic agent interpreter:
 * <ul>
 * <li> Feasibility precondition (an SL formula): represents a condition that 
 * must hold for an agent to be able to perform the action.
 * <li> Persistent feasibility precondition (an SL formula): represents the
 *  subset of the feasibility precondition that necessarily persists just after
 *  the performance of the action. 
 * <li>Rational effect (an SL formula): represents a state intented by the agent
 *  performing the action.
 * <li>Postcondition (an SL formula): represents the effect that the performing 
 * agent considers to be true just after the performance of the action.
 * <li>A behaviour implementing the performance of this action by the agent.
 * </ul>
 * @author Vincent Pautret - France Telecom
 * @version 0.9
 */
public interface SemanticAction {
    
    /*********************************************************************/
    /**				 			CONSTANTS								**/
    /*********************************************************************/
	
	/**
	 * Standard name for metaVariables refering to the actor of actions
	 */
	public static final String ACTOR = "actor";

    /*********************************************************************/
    /**				 			ACCESSOR AND MUTATOR					**/
    /*********************************************************************/
    
	/**
     * Returns the semantic capabilities of agent holding this action
     * @return Returns the semantic capabilities of agent holding this action
     */
    public SemanticCapabilities getSemanticCapabilities();

	/**
     * Returns a Term that represents the author of the action.
     * @return a Term that represents the author of the action (could be null)
     * @deprecated Use {@link #getActor()} instead
     */
    @Deprecated
	public Term getAuthor();
    
	/**
     * Returns a Term that represents the actor of the action.
     * @return a Term that represents the actor of the action (could be null)
     */
    public Term getActor();

    /**
     * Returns the feasibility precondition.
     * @return the feasibility precondition.
     **/
    public Formula getFeasibilityPrecondition();
    
    /**
     * Returns the rational effect of the action
     * @return the rational effect of the action
     **/
    public Formula getRationalEffect();
    
    /**
     * Returns the persitentFeasibilityPrecondition.
     * @return the persitentFeasibilityPrecondition.
     */
    public Formula getPersistentFeasibilityPrecondition();
    
    /**
     * Returns the postcondition of the action.
     * @return the postcondition of the action
     **/
    public Formula getPostCondition();
    
    /**
     * Returns the behaviour of the action.
     * @return the behaviour of the action
     */
    public Behaviour getBehaviour();
    
    /********************************************************************/
    /** 			PUBLIC METHODS
     */
    /********************************************************************/
    
    /**
     * Creates a new instance of this prototype of semantic action from
     * the specified action expression.
     * @param actionExpression
     *          an expression of action that specifies the instance to create
     * @return a new instance of the semantic action, the action expression of
     * which is specified, or null if no instance of the semantic action with
     * the specified action expression can be created
     * @throws SemanticInterpretationException if any exception occurs
     */
    public SemanticAction newAction(ActionExpression actionExpression) throws SemanticInterpretationException;
    
    /**
     * Creates a new instance of this prototype of semantic action from
     * the specified rational effect.
     * Should be overridden when using the rational effect of the action
     * (returns null by default).
     * @param rationalEffect a formula that specifies the rational effet of the 
     * instance to create
     * @param inReplyTo an ACL message the message to answer
     * @return a new instance of the semantic action, the rational effect of
     * which is specified, or null if no instance of the semantic action with
     * the specified rational effect can be created
     */
    public SemanticAction newAction(Formula rationalEffect, ACLMessage inReplyTo);
    
    /**
     * Returns the action expression representation of this action.
     * @return an action expression representing the semantic action
     * @throws SemanticInterpretationException if any exception occurs
     */
    public ActionExpression toActionExpression() throws SemanticInterpretationException;
} // End of interface SemanticAction
