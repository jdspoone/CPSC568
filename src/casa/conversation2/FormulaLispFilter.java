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

import casa.Status;
import casa.StatusObject;
import casa.abcl.Lisp;
import casa.util.Trace;

import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.ListOfTerm;
import jade.semantics.lang.sl.grammar.PredicateNode;
import jade.semantics.lang.sl.grammar.Term;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class FormulaLispFilter extends FormulaFilter {
	
	private String lispCode;
	private String prevTerm = "0", prevTerm2 = "0";

	/**
	 * 
	 */
	public FormulaLispFilter(String lispCode) {
		this.lispCode = lispCode;
	}

	/* (non-Javadoc)
	 * @see casa.conversation2.FormulaFilter#shouldNotify(jade.semantics.lang.sl.grammar.Formula)
	 */
	@Override
	public boolean shouldNotify(Formula formula) {
		Term term = null;
		if (formula instanceof PredicateNode) {
			PredicateNode pn = (PredicateNode)formula;
			ListOfTerm terms = pn.as_terms();
			term = (Term)terms.get(0);
		}
		if (term!=null || !lispCode.contains("??term")) {
			String thisTerm = term.toString();
		  String code = lispCode.replaceAll("\\?\\?term", thisTerm)
		  											.replaceAll("\\?\\?prev", prevTerm)
														.replaceAll("\\?\\?prev2", prevTerm2);
		  prevTerm2 = prevTerm;
		  prevTerm = thisTerm;
		  Status stat = Lisp.abclEval(null, null, null, code, null);
		  if (stat instanceof StatusObject) {
		  	Object o = ((StatusObject<?>)stat).getObject();
		  	if (o==null || o==org.armedbear.lisp.Lisp.NIL) {
		  		return false;
		  	}
		  	else {
		  		return true;
		  	}
		  }
		}
		else {
			Trace.log("error", "FormulaLispFilter.shouldNotify(): Can't find term from formula "+formula);
		}
		return true;
	}

}
