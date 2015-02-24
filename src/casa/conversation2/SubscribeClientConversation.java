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
package casa.conversation2;

import casa.KQMLMessage;
import casa.ML;
import casa.MLMessage;
import casa.PerformDescriptor;
import casa.Status;
import casa.StatusObject;
import casa.TransientAgent;
import casa.abcl.CasaLispOperator;
import casa.abcl.LispException;
import casa.abcl.ParamsMap;
import casa.agentCom.URLDescriptor;
import casa.exceptions.IllegalOperationException;
import casa.ui.AgentUI;
import casa.util.Pair;

import jade.semantics.lang.sl.grammar.Content;
import jade.semantics.lang.sl.grammar.ContentExpression;
import jade.semantics.lang.sl.grammar.ListOfContentExpression;
import jade.semantics.lang.sl.grammar.ListOfTerm;
import jade.semantics.lang.sl.grammar.Node;
import jade.semantics.lang.sl.grammar.Term;

import java.util.Collection;

import org.armedbear.lisp.Environment;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class SubscribeClientConversation extends Conversation {
	
	String expression = null;
	
	/**
	 * @param name
	 */
	public SubscribeClientConversation(String name) {
		super(name);
	}

	/**
	 * @param name
	 * @param parent
	 */
	public SubscribeClientConversation(String name, Conversation parent) {
		super(name, parent);
	}
	
	/**tells the server that we are interested in events represented by the lisp expression
	 * 
	 * @param name													the name of the lisp function defining a conversation to execute (Ex. "--subscription-request")
	 * @param ownerAgent										the agent that is interested in the event
	 * @param to														the server that will let ownerAgent know about the event
	 * @param expression										the lisp expression for the event
	 * @param filter												
	 * @throws IllegalOperationException		
	 */
	@SuppressWarnings("unchecked")
	public SubscribeClientConversation(String name, TransientAgent ownerAgent, URLDescriptor to, String expression, String filter) throws IllegalOperationException {
		super(name, ownerAgent, 
				new KQMLMessage(
						ML.PERFORMATIVE, ML.SUBSCRIBE,
						ML.SENDER, ownerAgent.getURL().toString(),
						ML.RECEIVER, to.toString(),
						ML.LANGUAGE, "FIPA-SL",
						ML.CONTENT, expression,
						"x-filter", (filter==null||filter.length()==0)?"()":filter
						),
				new Pair<String,Object>("server",to),
				new Pair<String,Object>("client",ownerAgent.getURL())
		); 
		this.expression = expression;
	}
		
	public PerformDescriptor update_subscribe(MLMessage msg) {
		try {
			Content cont = (Content)msg.getContent();
			ListOfContentExpression contentList = cont.as_expressions();
			ContentExpression cexp = contentList.first();
			Node[] children = cexp.children();
			Node[] grandchildren = children[0].children();
			ListOfTerm set = (ListOfTerm)grandchildren[1];
			@SuppressWarnings("unchecked")
			Collection<Term> terms = (Collection<Term>)set.asACollection();
			
			for (Term exp: terms) {
				update(null, exp);
			}
		} catch (Throwable e) {
    	PerformDescriptor ret = new PerformDescriptor(-1,"Expected :language FIPA-SL and :content of type ListOfContentExpression:"+e.toString());
    	ret.put(ML.PERFORMATIVE, ML.NOT_UNDERSTOOD);
    	ret.put(ML.ACT, msg.getParameter(ML.PERFORMATIVE)+(msg.getParameter(ML.ACT)==null?"":("|"+msg.getParameter(ML.ACT))));
    	ret.put("X-stack-trace", agent.println("error","",e));
    	return ret;
		}
		return null;
	}
	
	protected void update(URLDescriptor agentB, Term exp) {
		agent.println("warning", "Received subscribed Term '"+exp.toString()+"' for subscription '"+expression+"'");
		//agent.kBase.assertFormula(new BelieveNode(null, exp));
	}

  @SuppressWarnings("unused")
	private static final CasaLispOperator SUBSCRIBE_CONVERSATION =
    new CasaLispOperator("SUBSCRIBE-CONVERSATION", "\"!Declares and instantiates a subscribe conversation.\" "
								+"NAME \"@java.lang.String\" \"!The name of the conversation.\" "
    						+"TO \"@java.lang.String\" \"!A Cons list of Lisp functions describing sub-conversations or policies.\" "
    						+"EXPRESSION \"@java.lang.String\" \"!A Cons list of pairs of symbol/values pairs (themselves Cons lists) that will be bound in the context of the conversation.  The expressions are evaluated at the time the conversation is created.\" "
    						+"&KEY SAY \"@java.lang.String\" \"!Message to print as a warning message when the event occurs.\" "
    						, TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
  {
  	@Override
  	public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
  		Conversation conv;
			try {
				URLDescriptor server = URLDescriptor.make((String)params.getJavaObject("TO")); 
				final String say = (String)params.getJavaObject("SAY"); 
				conv = new SubscribeClientConversation(
						(String)params.getJavaObject("NAME"), 
						agent, server, 
						(String)params.getJavaObject("EXPRESSION"), null)
								{
									@Override
									public void update(URLDescriptor agentB, Term term) {
										agent.println("warning", (say==null?"Yeah! it works! Got term: ":(say+": "))+term);
									}
								};
			} catch (Throwable e) {
				throw new LispException(agent.println("error", toString()+": Cannot instantatiate a SubscribeConversation", e));
			}
  		return new StatusObject<Conversation>(0,conv);
  	}
  };

}
