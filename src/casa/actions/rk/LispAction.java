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
package casa.actions.rk;

import casa.Status;
import casa.abcl.Lisp;
import casa.exceptions.IllegalOperationException;
import casa.ui.BufferedAgentUI;

import java.util.TreeMap;

import org.armedbear.lisp.LispObject;

/**
 * Implements an action build out of lisp code. The lisp code is passed as a string
 * into the constructor and when it is executed (as a result of a call to {@link #execute()}),
 * the action's parameters will be available in the Lisp execution environment as the
 * values for the symbols corresponding to the parameter names.<p>
 * Creating a new Action is as simple as this:
 * <pre>
 * Action a = new LispAction("add2", "(+ p1 p2)"
 *                                 , new Parameter("p1", Long.class, new Long(0)
 *                                 , new Parameter("p2", Long.class, new Long(0));
 * </pre>
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class LispAction extends SimpleAction {
	
	String code = null;

	/**
	 * 
	 * @param name
	 * @param implCode
	 * @param params
	 */
	public LispAction(String name, String implCode, Param... params) {
		super(name, params);
		code = implCode;
	}
	
	/**
	 * The lisp code that was passed as a constructor parameter is executed.
	 * The action's parameters will be available in the Lisp execution environment as the
   * values for the symbols corresponding to the parameter names.
   * These values are populated with the values from the last call to {@link #setParamValues(Object...)}
   * If there are more parameters than the values supplied, the excess parameter values are populated 
   * by the default values from the parameter definitions.
	 */
	@Override
	public Status execute(Object... args) throws IllegalOperationException, ClassCastException {
		BufferedAgentUI ui = new BufferedAgentUI();
		Object[] paramValues = bindParamValues(args);
		TreeMap<String,LispObject> bindings = new TreeMap<String,LispObject>();
			for (int i=params.length-1; i>=0; i--) {
				LispObject lispObj = casa.abcl.Lisp.javaObj2LispObj(paramValues[i]);
				bindings.put(params[i].name, lispObj);
			}
		Status ret = Lisp.abclEval(null, null, bindings, code, ui);
		return ret;
	}
	
	public static void main(String[] args) {
	}
	


}
