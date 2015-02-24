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
package casa.jade;

import jade.semantics.kbase.filters.KBQueryFilter;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.Node;
import jade.semantics.lang.sl.grammar.Symbol;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.grammar.Variable;
import jade.semantics.lang.sl.tools.MatchResult;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public abstract class CasaQueryFilter extends KBQueryFilter {

	/**
	 * 
	 */
	public CasaQueryFilter() {
		// TODO Auto-generated constructor stub
	}

	protected Node getNode(MatchResult applyResult, String var) {
		Node ret = null;
		Formula form = getFormula(applyResult, var);
		if (form!=null) 
		{
			ret= form;
		}
		else 
		{
			Term term = getTerm(applyResult, var);
			if (term!=null) 
			{
				ret= term;
			}
			else 
			{
				Symbol sym = getSymbol(applyResult, var);
				if (sym!=null) 
				{
					ret=sym;
				}
				else
				{
					Variable vari = getVariable(applyResult, var);
					if (vari!=null) 
					{
						ret=vari;
					}
				}
			}
		}
		return ret;

	}

	protected Formula getFormula(MatchResult applyResult, String var) {
		try {
			return applyResult.getFormula(var);
		}
		catch(Throwable e) {
			return null;
		}
	}

	protected Term getTerm(MatchResult applyResult, String var) {
		try {
			return applyResult.getTerm(var);
		}
		catch(Throwable e) {
			return null;
		}
	}

	protected Symbol getSymbol(MatchResult applyResult, String var) {
		try {
			return applyResult.getSymbol(var);
		}
		catch(Throwable e) {
			return null;
		}
	}

	protected Variable getVariable(MatchResult applyResult, String var) {
		try {
			return applyResult.getVariable(var);
		}
		catch(Throwable e) {
			return null;
		}
	}

}
