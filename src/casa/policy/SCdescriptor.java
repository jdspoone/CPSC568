/**
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. 
 * Permission to use, copy, modify, distribute and sell this software and its 
 * documentation for any purpose is hereby granted without fee, provided that the 
 * above copyright notice appear in all copies and that both that copyright notice 
 * and this permission notice appear in supporting documentation.  The  Knowledge 
 * Science Group makes no representations about the suitability of  this software 
 * for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
package casa.policy;

import casa.Act;
import casa.ML;
import casa.Status;
import casa.StatusObject;
import casa.TransientAgent;
import casa.abcl.LispException;
import casa.abcl.ParamsMap;
import casa.abcl.CasaLispOperator;
import casa.policy.Policy.SCOpData;
import casa.socialcommitments.operators.CancelSocialCommitment;
import casa.socialcommitments.operators.SocialCommitmentOperator;
import casa.ui.AgentUI;

import org.armedbear.lisp.ControlTransfer;
import org.armedbear.lisp.Environment;
import org.armedbear.lisp.Function;
import org.armedbear.lisp.SpecialOperator;

/**
 * A simple "record" class used to describe a SC that this one is  dependent on.  Used in the :DependsOn clause.
 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
@Deprecated
public class SCdescriptor {
	public SCdescriptor(String performative, Act act) {
		this.performative = performative;
		this.act = act;
	}
	public String performative;
	/**
	 */
	public Act act;
	
//	/**
//	 * Lisp operator: (CANCEL :DEBTOR d :Creditor c :PERFORMATIVE p :ACT a)<br>
//	 * Construct a social commitment CANCEL operator.
//	 */
//	private static final CasaLispOperator SCDESCRIPTOR =
//		new CasaLispOperator("SCDESCRIPTOR", "\"!Return a new SCdescriptor object.\" "+
//				"&KEY PERFORMATIVE \"!The performative.\" "+
//				"ACT \"@casa.Act\" \"!The act.\" "
//				, TransientAgent.class)
//	{
//		@Override
//		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) throws ControlTransfer {
//			try {
//				String performative = (String)params.getJavaObject("PERFORMATIVE",String.class);
//				if (!agent.isA(performative,ML.ACTION)) throw new Exception(/*"(SCDESCRIPTOR :Performative <id> :ACT <act>)*/toString(params)+" must have a :Performative parameter that evaluates to a ACTION type, got '"+performative+"'");
//				Act act = (Act)params.getJavaObject("ACT",Act.class);
//				return new StatusObject<SCdescriptor>(new SCdescriptor(performative, act));
//			} catch (Exception e) {
//				throw new LispException(agent.println("error","SCDescriptor error",e));
//			}
//		}
//	};


}

