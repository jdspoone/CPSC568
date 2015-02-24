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
 * CommunicativeAction.java
 * Created on 28 juin 2005
 * Author : Vincent Pautret
 */
package jade.semantics.actions;

import jade.lang.acl.ACLMessage;
import jade.semantics.interpreter.SemanticInterpretationException;
import jade.semantics.lang.sl.content.UnparseContentException;
import jade.semantics.lang.sl.grammar.ActionExpression;
import jade.semantics.lang.sl.grammar.Content;
import jade.semantics.lang.sl.grammar.DateTimeConstantNode;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.ListOfTerm;
import jade.semantics.lang.sl.grammar.Node;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.grammar.TermSetNode;
import jade.semantics.lang.sl.grammar.WordConstantNode;
import jade.semantics.lang.sl.tools.SL;

import java.util.Date;

/**
 * CommunicativeAction is the interface of all communicative semantic actions 
 * that a Semantic Agent may perform.
 * @author Vincent Pautret - France Telecom
 * @version 0.9
 */
public interface CommunicativeAction extends SemanticAction {
    
    /**
     * General pattern used to recognize action expressions denoting
     * a communicative action
     */
    static final public Term COMMUNICATIVE_ACTION_PATTERN = SL
    .term("(action ??sender (??performative " +
            ":content ??content " +
            "(::? :conversation-id ??conversation-id) " +
            "(::? :encoding ??encoding) " +
            "(::? :in-reply-to ??in-reply-to) " +
            "(::? :language ??language) " +
            "(::? :ontology ??ontology) " +
            "(::? :protocol ??protocol) " +
            ":receiver ??receiver " +
            "(::? :reply-by ??reply-by) " +
            "(::? :reply-to ??reply-to)" +
            "(::? :reply-with ??reply-with) " +
            ":sender ??sender)) ");
    
    
    /***************************************************************************
     * PUBLIC METHODS /
     **************************************************************************/
    
    /**
     * Creates a new instance of this prototype of semantic action from
     * the specified action expression.
     * 
     * @param actionExpression
     *          an expression of action that specifies the instance to create
     * @return a new instance of the semantic action, the action expression of
     * which is specified, or null if no instance of the semantic action with
     * the specified action expression can be created
     * @throws SemanticInterpretationException if any exception occurs
     */
    public SemanticAction newAction(ActionExpression actionExpression) throws SemanticInterpretationException;
    
    
    /**
     * Creates a new instance of this prototype of semantic action from an 
     * ACLMessage.
     * @param aclMessage an ACL Message
     * @throws SemanticInterpretationException if any exception occurs
     * @return a new instance of the semantic action, the ACL message of
     * which is specified, or null if no instance of the semantic action with
     * the specified ACL message can be created
     */
    public SemanticAction newAction(ACLMessage aclMessage) throws SemanticInterpretationException;
    
    /**
     * Creates an instance of Semantic Action from a sender, receivers and content,
     * such that it is a consistent reply to another communicative action
     * @param author author of the action
     * @param receivers list of receivers
     * @param content content of the action
     * @param inReplyTo communicative action to reply to 
     * @throws SemanticInterpretationException if any exception occurs
     * @return a new instance of the semantic action, the parameters of
     * which are specified, or null if no instance of the semantic action with
     * the specified parameters can be created
     */
    public SemanticAction newAction(Term author, ListOfTerm receivers, Content content, CommunicativeAction inReplyTo) throws SemanticInterpretationException;
    
    /**
     * Creates an instance of the Semantic Action from a communicative action and a specific content
     * @param content content of the action
     * @param body communicative action, model to build the new action 
     * @return a new instance of the semantic action, the parameters of
     * which are specified, or null if no instance of the semantic action with
     * the specified parameters can be created
     * @throws SemanticInterpretationException if any exception occurs
     */
    public SemanticAction newAction(Content content, CommunicativeAction body) throws SemanticInterpretationException;
    
    
    /**
     * Creates a new instance of this prototype of semantic action from
     * the specified rational effect.
     * Should be overridden when using the rational effect of the action
     * (returns null by default).
     * 
     * @param rationalEffect
     *              a formula that specifies the rational effet of the instance to create
     * @param inReplyTo an ACL message the message to answer
     * @return a new instance of the semantic action, the rational effect of
     * which is specified, or null if no instance of the semantic action with
     * the specified rational effect can be created
     */
    public SemanticAction newAction(Formula rationalEffect, ACLMessage inReplyTo);
    
    
    
    /***********************************************************************
     * PUBLIC GETTERS AND SETTERS
     ***********************************************************************/
    
    
    /**
     * Sets the author of the action.
     * @param author the author of the action
     */
    public void setAuthor(Term author);
    
    /**
     * Returns the performative.
     * @return the performative.
     */
    public int getPerformative();
    
    
    /**
     * Sets the performative.
     * @param performative The performative to set.
     */
    public void setPerformative(int performative);
    
    
    /**
     * Returns the surface performative.
     * @return the surface performative.
     */
    public int getSurfacePerformative();
    
    
    /**
     * Sets the surface performative.
     * @param surfacePerformative The surfacePerformative to set.
     */
    public void setSurfacePerformative(int surfacePerformative);
    
    /**
     * Returns the receiver list.
     * @return the receiver list.
     */
    public ListOfTerm getReceiverList();
    
    
    /**
     * Returns the first receiver of the receiver list.
     * @return the first receiver of the receiver list.
     */
    public Term getReceiver();
    
    
    /**
     * Sets a unique receiver to this communicative action
     * @param receiver a receiver
     */
    public void setReceiver(Term receiver);
    
    
    /**
     * Sets a list of receiver
     * @param receiverList The receiverList to set.
     */
    public void setReceiverList(ListOfTerm receiverList);
    
    /**
     * Sets a list of receiver 
     * @param receiverList a list of receivers
     */
    public void setReceiverList(TermSetNode receiverList);
    
    
    
    /**
     * Sets the content with the given content
     * @param content The content to set.
     */
    public void setContent(Content content);
    
    /**
     * Sets the content with a new content
     */
    public void setContent();
    
    /**
     * Sets the content with a new content of the given size 
     * @param size size of the new content
     */
    public void setContent(int size); 
    
    /**
     * Sets the element i of the content
     * @param i an index
     * @param element the element to set
     */
    public void setContentElement(int i, Node element);
    
    
    /**
     * Returns the content.
     * @return the content.
     */
    public Content getContent();
    
    /**
     * Returns the element of the content at the specified index.
     * @param i an index 
     * @return the element of the content at the specified index
     */
    public Node getContentElement(int i);
    
    /**
     * Returns the number of elements in the content.
     * @return the number of elements in the content
     */
    public int getContentElementNumber();
    
    /**
     * Sets the surface content size.
     * @param size the size to set
     */
    public void setSurfaceContent(int size);
    
    /**
     * Sets the surface content with the given content
     * @param content the content to set
     */
    public void setSurfaceContent(Content content);
    
    /**
     * Sets the element i of the surface content.
     * @param i an index
     * @param element the element to set
     */
    public void setSurfaceContentElement(int i, Node element);
    
    /**
     * Returns the surface content.
     * @return the surface content
     */
    public Content getSurfaceContent();
    
    /**
     * Returns the element of the surface content at the specified index.
     * @param i an index
     * @return the element at the specified index
     */
    public Node getSurfaceContentElement(int i);
    
    /**
     * Returns the number of element in the surface content.
     * @return the number of element in the surface content
     */
    public int getSurfaceContentElementNumber();
    
    /**
     * Returns the conversationId parameter.
     * @return the conversationId.
     */
    public String getConversationId();
    
    /**
     * Sets the conversation-id.
     * @param conversationId The conversationId to set.
     */
    public void setConversationId(String conversationId);
    
    /**
     * Sets the conversation-id parameter.
     * @param conversationId value of conversationId
     */
    public void setConversationId(WordConstantNode conversationId);
    
    /**
     * Returns the encoding parameter.
     * @return the encoding parameter.
     */
    public String getEncoding();
    
    /**
     * Sets the encoding parameter.
     * @param encoding value of the encoding parameter.
     */
    public void setEncoding(String encoding);
    
    /**
     * Sets the encoding parameter.
     * @param encoding value of the encoding parameter
     */
    public void setEncoding(WordConstantNode encoding);
    
    /**
     * Returns the inReplyTo parameter.
     * @return the inReplyTo parameter.
     */
    public String getInReplyTo();
    
    /**
     * Sets in-reply-to parameter
     * @param inReplyTo inReplyTo value to set.
     */
    public void setInReplyTo(String inReplyTo);
    
    /**
     * Sets in-reply-to parameter
     * @param inReplyTo inReplyTo value
     */
    public void setInReplyTo(WordConstantNode inReplyTo);
    
    /**
     * Returns the language parameter.
     * @return the language.
     */
    public String getLanguage();
    
    /**
     * Sets the language parameter.
     * @param language The language to set.
     */
    public void setLanguage(String language);
    
    /**
     * Sets the language parameter.
     * @param language the language to set.
     */
    public void setLanguage(WordConstantNode language);
    
    /**
     * Returns the ontology parameter.
     * @return the ontology.
     */
    public String getOntology();
    
    /**
     * Sets the ontology parameter.
     * @param ontology The ontology to set.
     */
    public void setOntology(String ontology);
    
    /**
     * Sets the ontology parameter.
     * @param ontology The ontology to set.
     */
    public void setOntology(WordConstantNode ontology);
    
    /**
     * Returns the protocol parameter. 
     * @return the protocol.
     */
    public String getProtocol();
    
    /**
     * Sets the protocol parameter.
     * @param protocol The protocol to set.
     */
    public void setProtocol(String protocol);
    
    /**
     * Sets the protocol parameter.
     * @param protocol The protocol to set.
     */
    public void setProtocol(WordConstantNode protocol);
    
    /**
     * Returns the replyBy parameter.
     * @return the replyBy parameter.
     */
    public Date getReplyBy();
    
    /**
     * Sets reply-by parameter.
     * @param replyBy The replyBy to set.
     */
    public void setReplyBy(Date replyBy);
    
    /**
     * Sets reply-by parameter.
     * @param replyBy value of the replyBy parameter
     */
    public void setReplyBy(DateTimeConstantNode replyBy);
    
    /**
     * Returns the replyToList parameter.
     * @return the replyToList parameter.
     */
    public ListOfTerm getReplyToList();
    
    /**
     * Sets reply-to parameter.
     * @param replyToList The replyToList to set.
     */
    public void setReplyToList(ListOfTerm replyToList);
    
    /**
     * Sets reply-to parameter.
     * @param replyToList The replyToList to set.
     */
    public void setReplyToList(TermSetNode replyToList);
    
    /**
     * Returns the replyWith parameter.
     * @return the replyWith parameter.
     */
    public String getReplyWith();
    
    /**
     * Sets reply-with parameter.
     * @param replyWith The replyWith to set.
     */
    public void setReplyWith(String replyWith);
    
    /**
     * Sets reply-with parameter.
     * @param replyWith The replyWith to set.
     */
    public void setReplyWith(WordConstantNode replyWith);
    
    /**
     * Returns the ACL Message corresponding to the action.
     * @throws UnparseContentException if an error of parsing occurs
     * @return an acl message
     */
    public ACLMessage toAclMessage() throws UnparseContentException;
    
} // End of interface CommunicativeAction
