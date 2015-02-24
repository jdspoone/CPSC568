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
package casa.abcl;

import casa.MLMessage;
import casa.Status;
import casa.StatusObject;
import casa.TransientAgent;
import casa.ui.AgentUI;
import casa.ui.BufferedAgentUI;
import casa.util.Trace;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import org.armedbear.lisp.Condition;
import org.armedbear.lisp.ControlTransfer;
import org.armedbear.lisp.Environment;
import org.armedbear.lisp.Interpreter;
import org.armedbear.lisp.JavaObject;
import org.armedbear.lisp.LispError;
import org.armedbear.lisp.LispObject;
import org.armedbear.lisp.LispThread;
import org.armedbear.lisp.Primitive;
import org.armedbear.lisp.SimpleString;
import org.armedbear.lisp.Stream;
import org.armedbear.lisp.StringInputStream;
import org.armedbear.lisp.Symbol;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class Lisp {

	static {
//		casa.util.SysoutToBitBucket bitBucket = new casa.util.SysoutToBitBucket();
		loadClass("org.armedbear.lisp.LispObject");
		loadClass("casa.conversation2.Conversation");
		loadClass("casa.event.EventDescriptor");
		loadClass("casa.event.MessageEventDescriptor");
		loadClass("casa.Act");
		loadClass("casa.policy.Policy");
		loadClass("casa.event.Event");
		loadClass("casa.ontology.v3.CASAOntology");
		loadClass("casa.socialcommitments.SocialCommitmentDescriptor");
		loadClass("casa.PerformDescriptor");
		loadClass("casa.PerfActTransformation");
		loadClass("casa.PerformDescriptor");
		loadClass("casa.ontology.Constraint");
		loadClass("casa.ontology.v3.ConstraintSimple");
		loadClass("casa.ontology.Ontology");
		loadClass("casa.util.JarLoader");
//		bitBucket.finish();
	}
	
  public static final void loadClass(String className)
  {
    try
      {
        Class.forName(className);
      }
    catch (ClassNotFoundException e)
      {
        e.printStackTrace();
      }
  }

  private static Primitive LISP_ERROR_HANDLER = null;

	/**
	 * 
	 */
	private Lisp() {
	}
	
	public static Interpreter getInterpreter() {
		Interpreter lisp = Interpreter.getInstance();
		if (lisp==null) {
			lisp = Interpreter.createInstance();

      LISP_ERROR_HANDLER =
		    new Primitive("%LISP-ERROR-HANDLER", org.armedbear.lisp.Lisp.PACKAGE_CL_USER, false,"cond &OPTIONAL command")
		    {
		    @Override
		    public LispObject execute(LispObject cond, LispObject line) throws ControlTransfer
		    {
		        final Condition condition = (Condition) cond;
		        //String report = ("\""+line.getStringValue()+"\":\n  "+condition.writeToString()).replaceAll("<", "&lt;");
		        throw new LispException(condition/*report*/);
		    }
		    @Override
		    public LispObject execute(LispObject cond) throws ControlTransfer
		    {
		        final Condition condition = (Condition) cond;
		        //String report = (condition.writeToString()).replaceAll("<", "&lt;");
		        throw new LispException(condition/*report*/);
		    }
		    
		    };

			//lisp.debug = false;
			
			// custom lisp initialization code...
			try {
//			  lisp.eval("(defun invoke-debugger (condition) (%lisp-error-handler condition))");
			  lisp.eval("(setq *debugger-hook* (defun new-debugger-hook (condition old-hook) (%lisp-error-handler condition)))");
			  lisp.eval("(setq *AUTOLOAD-VERBOSE* NIL)");
			  lisp.eval("(defun arg-count-error (error-kind name arg lambda-list minimum maximum)"+
                  "  (declare (ignore error-kind))"+ //arg lambda-list minimum maximum
                  "  (error 'program-error"+
                  "         :format-control \"Wrong number of arguments for ~S, arg=~S, lambda-list=~S, minimum=~S, maximum=~S.\""+
                  "         :format-arguments (list name arg lambda-list minimum maximum)))");

			}
			catch (Throwable t) {
				System.out.println("abcl exception!");
				t.printStackTrace();
			}
			
			loadClass("org.armedbear.lisp.LispObject");
			loadClass("casa.event.MessageEventDescriptor");
			loadClass("casa.Act");
			loadClass("casa.policy.Policy");
			
		}

	return lisp;
}
	
	public static final ThreadLocal<Map<String,Stack<LispObject>>> envForThread = new ThreadLocal<Map<String,Stack<LispObject>>>();
	
	static public void bind(Environment env, String name, LispObject value) {
		name = name.toUpperCase();
		Symbol sym = org.armedbear.lisp.Lisp.PACKAGE_CL_USER.intern(new SimpleString(name), LispThread.currentThread());
		env.bind(sym, value);
		assert lookup(env,name)!=null;
		
		Map<String,Stack<LispObject>> map = envForThread.get();
		if (map==null) {
			map = new TreeMap<String,Stack<LispObject>>();
			map.put("casa-env-recovery-marker", new Stack<LispObject>());
			map.get("casa-env-recovery-marker").push(org.armedbear.lisp.Lisp.T);
			envForThread.set(map);
		}
		Stack<LispObject> s = map.get(name);
		if (s==null) { 
			s = new Stack<LispObject>();
			map.put(name, s);
		}
		s.push(value);
	}
	
	static public void unbind(String name) { //no need to unbind the env itself
		name = name.toUpperCase();
		Map<String,Stack<LispObject>> map = envForThread.get();
		if (map==null) 
			return;
		Stack<LispObject> 
		s = map.get(name);
		if (s==null) 
			return;
		s.pop();
		if (s.isEmpty())
			map.remove(name);
	}
	
	static private boolean needsRecovering(Environment env) {
		if (env==null || env.isEmpty()) return true;
		Symbol sym = org.armedbear.lisp.Lisp.PACKAGE_CL_USER.intern("casa-env-recovery-marker");
		return env.lookup(sym)==null;
	}
	
	static public LispObject lookup(Environment env, String name) {
		name = name.toUpperCase();
		Symbol sym = org.armedbear.lisp.Lisp.PACKAGE_CL_USER.intern(name);
		LispObject ret = env.lookup(sym);
		if (ret!=null) return ret;
		if (needsRecovering(env)) {
			Map<String,Stack<LispObject>> map = envForThread.get();
			if (map==null) 
				return null;
			for (String key: map.keySet()) {
				Symbol sym2 = org.armedbear.lisp.Lisp.PACKAGE_CL_USER.intern(key);
				Stack<LispObject> s = map.get(key);
				if (!s.isEmpty()) env.bind(sym2, s.peek());
			}
			return lookup(env,name); //recurse
		}
		return null;
	}
	
  static public Object lookupAsJavaObject(Environment env, String name) throws ClassCastException {
		LispObject lo = lookup(env, name);
		JavaObject jo = (JavaObject)lo;
		if (jo==null)
			return null;
		else 
			return jo.getObject(); 
	}
	
  static public Status abclEval(TransientAgent agent, Environment env, Map<String,LispObject> newEnvBindings, String c, AgentUI ui) {
  	Status ret;
  	
  	if (c==null || "".equals(c.trim()))
  		return null;
  	
  	org.armedbear.lisp.LispObject val;
  	//create a temporary environment based on the current agent's environment
  	env = new Environment(env);
		
  	if (newEnvBindings!=null && newEnvBindings.size()>0) {
  		for (String key: newEnvBindings.keySet()) {
  			bind(env, key, newEnvBindings.get(key));
  		}
  	}

  	if (agent!=null) {
  	  bind(env, "agent", new JavaObject(agent)); // done in the parent environment
  	}
  	
  	if (ui==null) {
  		ui = new BufferedAgentUI();
  	}
  	bind(env, "ui", new JavaObject(ui));
  	
  	if (agent!=null) {
  		String packageName = agent.getAgentName();
  		if (packageName!= null) {

  			org.armedbear.lisp.Package packag = org.armedbear.lisp.Packages.findPackage(packageName); 
  			if (packag==null) {
  				packag = org.armedbear.lisp.Packages.makePackage(packageName);
  				Symbol sym = org.armedbear.lisp.Lisp.PACKAGE_CL_USER.intern(packageName);
  				sym.setSymbolValue(new SimpleString(packageName));
  			}
  			bind(env, "*package*", packag);
  			Symbol.PACKAGE.setSymbolValue(packag);
  			org.armedbear.lisp.LispThread.currentThread().setSpecialVariable(Symbol.PACKAGE, packag);
  		}
  	}


  	  	
  	try {
  			if (agent!=null) {
  				Symbol.LOAD_VERBOSE.setSymbolValue(agent.isLoggingTag("lisp")/*&&(agent.options==null||agent.options.tracing)*/?org.armedbear.lisp.Lisp.T:org.armedbear.lisp.Lisp.NIL);
  				Symbol.LOAD_PRINT.setSymbolValue(agent.isLoggingTag("lisp")/*&&(agent.options==null||agent.options.tracing)*/?org.armedbear.lisp.Lisp.T:org.armedbear.lisp.Lisp.NIL);
  			}
  			Symbol.STANDARD_OUTPUT.setSymbolValue(new Stream(Symbol.SYSTEM_STREAM,ui.getOutStream(),Symbol.CHARACTER));

  			// TODO - temporary hack until I figure out where all my double opening parentheses come from - JPH
  			if (c.startsWith ("((")) {
  				if (agent!=null && agent.isLoggingTag("lisp")) agent.println("lisp","Lisp execution: "+c.substring (1,c.length () - 1));
  				val = org.armedbear.lisp.Lisp.eval(new StringInputStream(c.substring (1,c.length () - 1)).read(true, org.armedbear.lisp.Lisp.NIL, false,LispThread.currentThread(), Stream.currentReadtable),
  						env, LispThread.currentThread());
  				if (agent!=null && agent.isLoggingTag("lisp") && ui instanceof BufferedAgentUI) agent.println("lisp","  -- Lisp execution result: "+val.writeToString());
  			}
  			else {
  				if (agent!=null && agent.isLoggingTag("lisp")) agent.println("lisp","Lisp execution: "+c);
  				val = org.armedbear.lisp.Lisp.eval(new StringInputStream(c).read(true, org.armedbear.lisp.Lisp.NIL, false, LispThread.currentThread(), Stream.currentReadtable),
  						env, LispThread.currentThread());
  				if (agent!=null && agent.isLoggingTag("lisp") && ui instanceof BufferedAgentUI) agent.println("lisp","  -- Lisp execution result: "+val.writeToString());
  			}
  		
  		if (val instanceof JavaObject) {
  			Object obj = ((JavaObject)val).getObject();
  			if (obj instanceof Status) 
  				ret = (Status)obj;
  			else {
  				ret = new StatusObject<Object>(0,obj);
  			}
  		}
  		else {
  	    String retval = val.writeToString();
  		  int statusVal = 0;
  		  if (val instanceof LispError) {
  			  ui.println("*** LispError! Lisp failed command \""+c+"\":\n"+((LispError)val).getConditionReport());
  			  statusVal = -1;
  		  }
  		  ret = new StatusObject<Object>(statusVal,retval,val);
  		}
  	} catch (Throwable e) {
  		try {
				String report = null;
				String msg = e.getMessage();
				Condition cond = ((e instanceof ControlTransfer)?(Condition)((ControlTransfer)e).getCondition():null);
				if (cond!=null) report = cond.writeToString()+": "+cond.getConditionReport();
		    ByteArrayOutputStream out = new ByteArrayOutputStream();
		    PrintWriter pw = new PrintWriter(out);
		    for (Throwable th = e; th!=null; th = th.getCause()) {
		    	if (th!=e) pw.println("Caused by:");
		      e.printStackTrace(pw);
		    }
		    pw.flush();
				String err = "Lisp.abclEval: \n"
					+c
					+(report==null?"":(":  \n"+report))
					+(msg==null?"":("\n"+msg)
					+(out.size()>0?("\n"+out.toString()):""));
				ret = new Status(-1010,signalError(agent,"error",err,e));
			} catch (ControlTransfer e1) {
				String err = "Lisp.abclEval: (no condition report)\n"+/*bigC*/c;
				ret = new Status(-1011,signalError(agent,"error",err,e1));
			}
  	}
  	
  	unbind("agent");
  	unbind("ui");
  	if (newEnvBindings!=null && newEnvBindings.size()>0) {
  		for (String key: newEnvBindings.keySet()) {
  			unbind(key);
  		}
  	}
  	
  	return ret;
  }
  
  private static String signalError(TransientAgent agent, String tag, String msg, Throwable e) {
  	String ret = msg;
  	if (agent == null) {
  		if (e==null) {
  			Trace.log("error", msg);
  		}
  		else {
  			Trace.log("error", msg, e);
  		}
  	}
  	else {
  		if (e==null) {
  		  ret = agent.println(tag, msg);
  		}
  		else {
  			ret = agent.println(tag,msg,e);
  		}
  	}
  	return ret;
  }
  
  /**
   * Lisp operator: (?)<br>
   * Displays the list of all casa lisp operators with the first sentence of documenation.
   * Returns NIL.
   */
  @SuppressWarnings("unused")
  private static final CasaLispOperator SLEEP_IGNORING_INTERRUPTS =
    new CasaLispOperator("SLEEP-IGNORING-INTERRUPTS", "\"!Similar to the lisp (sleep [seconds]) operator, but will ingore itnerrupts.\" "+
    		"&optional SECONDS \"@java.lang.Integer\" \"!The number of seconds to wait.\"", TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
    {
      @Override
			public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
    		Integer secondsInteger = (Integer)params.getJavaObject("SECONDS");
    		int secs = 0;
    		if (secondsInteger!=null) {
    			secs = secondsInteger;
    		}
    		long endTime = System.currentTimeMillis()+(secs*1000);
    		while (true){
    			long sleepfor = endTime-System.currentTimeMillis();
    			if (sleepfor<=0) break;
    			try {
						Thread.sleep(sleepfor);
					} catch (InterruptedException e) {}
    		}
        return new Status(0);
    }
  };

  /**
   * Lisp operator: (tostring)<br>
   * Displays the list of all casa lisp operators with the first sentence of documenation.
   * Returns NIL.
   */
  @SuppressWarnings("unused")
  private static final CasaLispOperator TOSTRING =
    new CasaLispOperator("TOSTRING", "\"!prints a java object through it's toString() method.\" "+
    		"OBJECT \"!The object to convert to a String.\" "+
    		"&KEY PRETTY \"@java.lang.Boolean\" \"!Call toString(true) instead of toString() for MLMessage's.\"", TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
    {
      @Override
			public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
    		Object obj = params.getJavaObject("OBJECT");
    		if (obj==null) obj = "NIL";
    		if ((Boolean)params.getJavaObject("PRETTY") && obj instanceof MLMessage) 
          return new StatusObject<String>(0, ((MLMessage)obj).toString(true), ((MLMessage)obj).toString(true));
        return new StatusObject<String>(0, obj.toString(), obj.toString());
    }
  };

	/**
	 * Given a LispObject, return the appropriate Java object:
	 * <table>
	 * <tr><td>Symbol</td><td>String</td></tr>
	 * <tr><td>Cons</td><td>The evaluation of the Cons (recursively interpreted once)</td></tr>
	 * <tr><td>floatp() is true</td><td>Float</td></tr>
	 * <tr><td>integerp() is true</td><td>Integer</td></tr>
	 * <tr><td>stringp() is true</td><td>String</td></tr>
	 * <tr><td>javaInstance() is true</td><td>Object</td></tr>
	 * </table>
	 * @param o The LispObject to interpret
	 * @param env The current environment (for interpreting a Cons)
	 * @return Some appropriate Java object
	 * @throws ControlTransfer
	 */
	static public	Object lispObj2JavaObj(LispObject o) throws ControlTransfer {
		if (o==null) return null;
		if (o==org.armedbear.lisp.Lisp.T)											return true;
		if (o==org.armedbear.lisp.Lisp.NIL)										return false;
		if (o.floatp())       				return new Float(o.floatValue());
		if (o.integerp())             return new Integer(o.intValue());
		if (o.stringp())			        return o.getStringValue();
		if (o instanceof Symbol)		  return ((Symbol)o).getName();
		if (o.javaInstance()!=null)	  return o.javaInstance();
		throw new LispException("Unknown type: "+o);
	}

	static public LispObject javaObj2LispObj(Object obj) {
		if (obj==null) return org.armedbear.lisp.Lisp.NIL;
		if (obj instanceof LispObject) return (LispObject)obj;
		if (obj instanceof String) return new SimpleString((String)obj);
		if (obj instanceof Integer) return org.armedbear.lisp.LispInteger.getInstance(((Integer)obj).intValue());
		if (obj instanceof Long) return org.armedbear.lisp.LispInteger.getInstance(((Long)obj).longValue());
		if (obj instanceof Boolean) return ((Boolean)obj).booleanValue()?org.armedbear.lisp.Lisp.T:org.armedbear.lisp.Lisp.NIL;
		JavaObject jObj = new org.armedbear.lisp.JavaObject(obj);
		return jObj;
	}



}

