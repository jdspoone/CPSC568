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
package casa;

import casa.abcl.CasaLispOperator;
import casa.abcl.ParamsMap;
import casa.interfaces.Describable;
import casa.interfaces.Transformation;
import casa.ui.AgentUI;

import java.util.Vector;
import java.util.regex.Matcher;

import org.armedbear.lisp.Environment;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class PerfActTransformation implements Transformation {

	@SuppressWarnings("serial")
	private class Trans extends Vector<String> {
		Trans(Vector<String> v) {
			super(v);
		}
		Trans() {
			super();
		}
		@Override
		public String toString() {
			if (this.size()==0) 
				return "";
			StringBuilder b = new StringBuilder();
			for (String s: this)
				b.append(s).append('|');
		  String s = b.toString();
		  return s.substring(0,s.length()-1);
		}
	}
	Trans fromV, toV;
	
	public PerfActTransformation(Vector<String> from, Vector<String> to) {
		fromV = new Trans(from);
		toV = new Trans(to);
	}
	
	public PerfActTransformation(String from, String to) {
		String[] f = from.split("\\|");
		String[] t = to.split("\\|");
		fromV = new Trans();
		for (String s: f)
			fromV.add(s);
		toV = new Trans();
		for (String s: t)
			toV.add(s);
	}
	
	public boolean isApplicable(Describable e) {
		return transform(e)!=e;
	}
	
	private Describable trans(Describable e, Vector<String> from, Vector<String> to) {
		String performative = e.getParameter(ML.PERFORMATIVE);
		if (performative==null)
			return e;
		String actString = e.getParameter(ML.ACT);
		Vector<String> in = (actString==null||actString.length()==0) ? new Vector<String>() : new Act(actString);
		in.insertElementAt(performative, 0);
		int n = from.size();
		for (int i=0; i<=in.size()-n; i++) {
			if (in.subList(i, i+n).equals(from)) {
				Vector<String> out = new Vector<String>(in.subList(0, i));
				for (int j=0; j<to.size(); j++) 
					out.add(to.elementAt(j));
				for (int j=i+n; j<in.size(); j++) 
					out.add(in.elementAt(j));
				Describable ret = e.clone();
				ret.setParameter(ML.PERFORMATIVE, out.elementAt(0));
				StringBuilder actb = new StringBuilder();
				int j=0;
				for (String s: out) {
					if (j++!=0)
						actb.append(s).append('|');
				}
				String act = actb.toString();
				act = act.substring(0, act.length()-1);
				assert !act.equals(e.getParameter(ML.ACT));
				ret.setParameter(ML.ACT, act);
				return ret;
			}
		}
		return e;
	}

	/* (non-Javadoc)
	 * @see casa.Transformation#transform(casa.interfaces.Describable)
	 */
	public Describable transform(Describable e) {
		return trans(e, fromV, toV);
	}
	
	/* (non-Javadoc)
	 * @see casa.Transformation#revTransform(casa.interfaces.Describable)
	 */
	public Describable revTransform(Describable e) {
		return trans(e, toV, fromV);
	}
	
	private String trans(String s, Vector<String> from, Vector<String> to) {
		String ret = s.replaceFirst(Matcher.quoteReplacement(from()), to());
		return ret;
	}

	/* (non-Javadoc)
	 * @see casa.Transformation#transform(casa.interfaces.Describable)
	 */
	public String transform(String e) {
		return trans(e, fromV, toV);
	}
	
	/* (non-Javadoc)
	 * @see casa.Transformation#revTransform(casa.interfaces.Describable)
	 */
	public String revTransform(String e) {
		return trans(e, toV, fromV);
	}
	
	/* (non-Javadoc)
	 * @see casa.Transformation#from()
	 */
	public String from() {
		StringBuilder b = new StringBuilder();
		for (String s: fromV)
			b.append(s).append('|');
		String ret = b.toString();
		ret = ret.substring(0,ret.length()-1);
		return ret;
	}

	/* (non-Javadoc)
	 * @see casa.Transformation#to()
	 */
	public String to() {
		StringBuilder b = new StringBuilder();
		for (String s: toV)
			b.append(s).append('|');
		String ret = b.toString();
		ret = ret.substring(0,ret.length()-1);
		return ret;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder("(TRANSFORMATION \"");
		b.append(fromV.toString())
		.append("\" \"")
		.append(toV.toString())
		.append("\")");
		return b.toString();
	}
	
	/**
	 * Lisp operator: (TRANSFORMATION)<br>
	 * Return the class name of the agent.
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator TRANSFORMATION =
		new CasaLispOperator("TRANSFORMATION", "\"!Instantiates a transformation object that translates over the PERFORMATIVE/ACT of any Describable object.\" "
				+"FROM \"@java.lang.String\" \"!The from part in id|id... form.\" "
				+"TO   \"@java.lang.String\" \"!The to part in id|id... form.\" "
				, TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
	{
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
			PerfActTransformation trans = new PerfActTransformation((String)params.getJavaObject("FROM"), (String)params.getJavaObject("TO"));
			agent.addTransformation(trans);
			return new StatusObject<PerfActTransformation>(trans);
		}
	};

	/**
	 * Lisp operator: (TRANSFORMATION.GET_FROM)<br>
	 * Return the class name of the agent.
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator TRANSFORMATION__GET_FROM =
		new CasaLispOperator("TRANSFORMATION.GET-FROM", "\"!returns the FROM part of the argument TRANSFORMATION in id|id|... format.\" "
				+"TRANSFORMATION \"@casa.PerfActTransformation\" \"!The Transformation.\" "
				, TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
	{
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
			PerfActTransformation tran = (PerfActTransformation)params.getJavaObject("TRANSFORMATION");
			return new StatusObject<String>(0,null,tran.from());
		}
	};
	
	/**
	 * Lisp operator: (TRANSFORMATION.GET_TO)<br>
	 * Return the class name of the agent.
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator TRANSFORMATION__GET_TO =
		new CasaLispOperator("TRANSFORMATION.GET-TO", "\"!returns the TO part of the argument TRANSFORMATION in id|id|... format.\" "
				+"TRANSFORMATION \"@casa.PerfActTransformation\" \"!The Transformation.\" "
				, TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
	{
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
			PerfActTransformation tran = (PerfActTransformation)params.getJavaObject("TRANSFORMATION");
			return new StatusObject<String>(0,null,tran.to());
		}
	};
	
	/**
	 * Lisp operator: (TRANSFORM-STRING)<br>
	 * Return the class name of the agent.
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator TRANSFORM_STRING =
		new CasaLispOperator("TRANSFORM-STRING", "\"!returns the argument string transformed in id|id|... format.\" "
				+"TRANSFORMATION \"@casa.PerfActTransformation\" \"!The Transformation o perform.\" "
				+"STRING \"@java.lang.String\" \"!The string to translate in id|id|... format.\" "
				, TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
	{
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
			String s = ((String)params.getJavaObject("STRING"));
			PerfActTransformation tran = ((PerfActTransformation)params.getJavaObject("TRANSFORMATION"));
			return new StatusObject<String>(0,null,agent.transform(s));
		}
	};

	@Override
	public int compareTo(Transformation o) {
		int ret = from().compareTo(o.from());
		if (ret!=0) return ret;
		assert to().compareTo(o.to())==0;
		return 0;
	}
}
