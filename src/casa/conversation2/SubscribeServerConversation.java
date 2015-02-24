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

import casa.ML;
import casa.MLMessage;
import casa.PerformDescriptor;
import casa.agentCom.URLDescriptor;
import casa.jade.BeliefObserver;
import casa.util.Runnable1;

import jade.semantics.lang.sl.grammar.ContentNode;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.IdentifyingContentExpressionNode;
import jade.semantics.lang.sl.grammar.IdentifyingExpression;
import jade.semantics.lang.sl.tools.SL;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class SubscribeServerConversation extends Conversation {
	
	protected BeliefObserver observer = null;
	
	protected ConcurrentSkipListSet<Formula> formulas = new ConcurrentSkipListSet<Formula>();
	
	/**constructs a conversation using the lisp function defined by the given name
	 * 
	 * @param name
	 */
	public SubscribeServerConversation(String name) {
		super(name);
	}

//	/**
//	 * @param name
//	 */
//	public SubscribeServerConversation(String name, FormulaFilter filter) {
//		super(name);
//		this.filter = filter;
//	}

	/**
	 * @param name
	 * @param parent
	 */
	public SubscribeServerConversation(String name, Conversation parent) {
		super(name, parent);
	}
	
	protected void observeFormula(Formula formula, FormulaFilter filter) {
		Runnable1<Formula, Object> runnable = new Runnable1<Formula, Object>() {
			@Override
			public Boolean run(Formula f) {
				try {
					URLDescriptor client = (URLDescriptor)getVar("client");
					agent.sendMessage(ML.INFORM_REF, null, client, ML.LANGUAGE, ML.FIPA_SL, ML.CONTENT, "("+f.toString()+")", ML.CONVERSATION_ID, getId());
				} catch (Throwable e) {
					agent.println("error", "Failed to send a inform-ref to a subscribe ("+formulas+")",e);
				}
				return true;
			}
		};
		observer = new BeliefObserver(agent.kBase, formula, filter, runnable);
		agent.kBase.addObserver(observer);
		observer.update(null);
	}
	
	protected boolean okToObserve(IdentifyingExpression exp) {
		return true;
	}

  @SuppressWarnings("unchecked")
	public PerformDescriptor evaluate_subscribe(MLMessage msg) {
    Object contentObj;
		try {
			contentObj = msg.getContent();
	    if (contentObj==null || !((contentObj instanceof IdentifyingExpression) || contentObj instanceof ContentNode))
	    	throw new ParseException("Expected :language FIPA-SL and :content of type IdentifyingExpression or ContentNode",0);
		} catch (Throwable e) {
    	PerformDescriptor ret = new PerformDescriptor(-1,e.toString());
    	ret.put(ML.PERFORMATIVE, ML.NOT_UNDERSTOOD);
    	ret.put(ML.ACT, msg.getParameter(ML.PERFORMATIVE)+(msg.getParameter(ML.ACT)==null?"":("|"+msg.getParameter(ML.ACT))));
    	ret.put("X-stack-trace", agent.println("error","",e));
    	return ret;
		}
		List<IdentifyingExpression> list = new LinkedList<IdentifyingExpression>();
		if (contentObj instanceof ContentNode) {
			List<IdentifyingContentExpressionNode> contentList = ((ContentNode)contentObj).as_expressions().asAList();
			for (IdentifyingContentExpressionNode f: contentList) {
				list.add(f.as_identifying_expression());
			}
		}
		else {
			list.add((IdentifyingExpression)contentObj);
		}
		for (IdentifyingExpression exp: list) {
	    if (okToObserve(exp)) {
	    	Formula formula = exp.as_formula();
	    	formula = subVarsForMetavars(formula);
	    	if (observer==null) {
	    		String filterString = msg.getParameter("x-filter");
	    		FormulaFilter filter = null;
	    		if (filterString!=null && filterString.length()>2) {
	    			filter = new FormulaLispFilter(filterString);
	    		}
	        observeFormula(formula, filter);
	    	}
	    	else {
	    		observer.addFormula(formula);
	    	}
        formulas.add(formula);
	    }
	    else {
	    	if (observer!=null)
	    	  agent.kBase.removeObserver(observer);
	    	PerformDescriptor ret = new PerformDescriptor(-2);
	    	ret.put(ML.PERFORMATIVE, ML.REFUSE);
	    	ret.put(ML.ACT, msg.getParameter(ML.PERFORMATIVE)+(msg.getParameter(ML.ACT)==null?"":("|"+msg.getParameter(ML.ACT))));
	    	return ret;
	    }
		}
		
		agent.println("conversations", "SubscribeConversation.evaluate_subscribe(): observing formulas: "+formulas);
		
		//if we got here, we're returning success
		return null;
  }
  
  private Formula subVarsForMetavars(Formula f) {
  	String s = f.toString();
  	s = s.replaceAll("\\s\\?([^?])", " ??$1");
  	return SL.formula(s);
  }
  
	@Override
	protected void deleteSelf() {
	  if (observer!=null)
	  	agent.kBase.removeObserver(observer);
		super.deleteSelf();
	}
	
}
