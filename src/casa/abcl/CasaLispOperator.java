package casa.abcl;

import casa.LispAccessible;
import casa.PerformDescriptor;
import casa.Status;
import casa.StatusObject;
import casa.TransientAgent;
import casa.ui.AgentUI;
import casa.util.AnnotationUtil;
import casa.util.CASAUtil;
import casa.util.Pair;
import casa.util.Trace;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;

import org.armedbear.lisp.Condition;
import org.armedbear.lisp.Cons;
import org.armedbear.lisp.ControlTransfer;
import org.armedbear.lisp.Environment;
import org.armedbear.lisp.JavaObject;
import org.armedbear.lisp.Lisp;
import org.armedbear.lisp.LispObject;
import org.armedbear.lisp.LispThread;
import org.armedbear.lisp.Pathname;
import org.armedbear.lisp.SimpleString;
import org.armedbear.lisp.SpecialOperator;
import org.armedbear.lisp.Stream;
import org.armedbear.lisp.StringInputStream;
import org.armedbear.lisp.Symbol;

/**
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: </p>
 * Use this class to implement ABCL (lisp) operators that work in the context of an agent.  Use it by subclassing this class, overriding the  {@link #execute(TransientAgent,ParamsMap,AgentUI, Environment)}  method.
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
public abstract class CasaLispOperator extends /*Function*/SpecialOperator implements Comparable<CasaLispOperator> {

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(CasaLispOperator o) {
		return name.toUpperCase().compareTo(o.getName().toUpperCase());
	}

	/** the name of the operator */
	private String name;
	
	/** the class (or subclass thereof) that this operator is restricted to */
	private Class<?> classRestriction = null;

	/** the defining Class for this operator (set in the contructor, used in help) */
	private Class<?> definingClass;

	private ArrayList<Entry> ordered = new ArrayList<Entry>() {
		private static final long	serialVersionUID	= 3078505568248846295L;

		@Override
		public boolean add(Entry o) {
			if (o.svar!=null) keyed.put(o.svar, new Entry(o.svar,org.armedbear.lisp.Lisp.NIL,null));
			return super.add(o);
		}
	};

	private boolean allowOtherKeys = false;
	private int required;

	private TreeMap<String,Entry> keyed = new TreeMap<String,Entry>(){
		private static final long	serialVersionUID	= -8816114511766206922L;

		@Override
		public Entry put(String key, Entry value) {
			if (value.svar!=null) super.put(value.svar, new Entry(value.svar,org.armedbear.lisp.Lisp.NIL,null));
			return super.put(key, value);
		}
	};

	private String doc = null;
	//private Class<? extends TransientAgent> agentClass;

//	private static ConcurrentSkipListMap<Class<? extends TransientAgent>,TreeSet<CasaLispOperator>> all = 
//			new ConcurrentSkipListMap <Class<? extends TransientAgent>,TreeSet<CasaLispOperator>>(new casa.util.ClassComparator());
	private static ConcurrentSkipListSet<CasaLispOperator> all = new ConcurrentSkipListSet<CasaLispOperator>();

	private String printName(LispObject obj) {
		if (obj instanceof SimpleString) return "\""+((SimpleString)obj).getStringValue()+"\"";
		else if (obj==org.armedbear.lisp.Lisp.T) return "T";
		else if (obj==org.armedbear.lisp.Lisp.NIL) return "NIL";
		else return obj.toString();
	}

	private LinkedList<String> synonyms = null;

	/**
	 * Create a synonym for this lisp command in the lisp environment.
	 * @param name The name of the new synonym for this lisp command.
	 */
	public void makeSynonym(String name) {
		if (name==null) return;
		Symbol symbol = org.armedbear.lisp.Lisp.PACKAGE_CL_USER.internAndExport(name.toUpperCase());
		symbol.setSymbolFunction(this);
		if (doc!=null) symbol.setDocumentation(Symbol.FUNCTION, new SimpleString(doc));
		if (synonyms==null) synonyms = new LinkedList<String>();
		synonyms.add(name);
	}

	/**
	 * Creates a new {@link casa.abcl.CasaLispOperator} given a method and a transient agent.
	 * The method must be annotated with a {@link casa.LispAccessible} otherwise it will
	 * be ignored.
	 * @param agentClass the class that has the method
	 * @param aMethod the method to create a {@link casa.abcl.CasaLispOperator} for 
	 * @return the created {@link casa.abcl.CasaLispOperator}
	 */
	public static CasaLispOperator makeCasaLispOperator(Class<? extends TransientAgent> agentClass, final Method aMethod){
		LispAccessible annotation = AnnotationUtil.getAnnotation(aMethod, LispAccessible.class);
		if (annotation == null)
			return null;

		String commandName = annotation.name().equals("") ? aMethod.getName() : annotation.name();
		final Class<?>[] methodParams = aMethod.getParameterTypes();
		final LispAccessible.Argument[] parameterSpec;
		if(methodParams.length == annotation.arguments().length)
			parameterSpec = annotation.arguments();
		else{
			parameterSpec = new LispAccessible.Argument[methodParams.length];
			for(int i=0; i<methodParams.length; i++){
				parameterSpec[i] = makeArgument(String.format("ARG%d", i),"Argument's help is undefined.");
			}
		}

		StringBuffer arglist = new StringBuffer();
		arglist.append(String.format("\"!%s\" ",annotation.help()));
		for(int i = 0; i<methodParams.length; i++){
			arglist.append(String.format("%s \"@%s\" \"!%s\" ", parameterSpec[i].name(), methodParams[i].getName(), parameterSpec[i].help()));
		}

		CasaLispOperator op = new CasaLispOperator(commandName.toUpperCase(), arglist.toString(), agentClass, aMethod.getClass()) {

			@Override
			public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) throws ControlTransfer {
				Object[] args = new Object[parameterSpec.length];
				for(int i=0; i<parameterSpec.length; i++)
					args[i] = params.getJavaObject(parameterSpec[i].name());
				try {
					Object ret = aMethod.invoke(agent, args);
					if(aMethod.getReturnType() != void.class)
						return new StatusObject<Object>(0, ret);
					else
						return new Status(0);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					return new Status(-1, String.format("Failed to execute generated operator; %s", e.getMessage()));
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					return new Status(-1, String.format("Failed to execute generated operator; %s", e.getMessage()));
				} catch (InvocationTargetException e) {
					e.printStackTrace();
					return new Status(-1, String.format("Failed to execute generated operator; %s", e.getMessage()));
				}
			}
		};

		return op;
	}

	/**
	 * Constructs an argument pair (describes parameter for {@link casa.abcl.CasaLispOperator}).
	 * @param argName the argument name
	 * @param help a description of the argument
	 * @return a {@link casa.LispAccessible} pair representation
	 */
	private final static LispAccessible.Argument makeArgument(final String argName, final String help){
		return new LispAccessible.Argument() {
			public Class<? extends Annotation> annotationType() {
				return getClass();
			}

			public String name() {
				return argName;
			}

			public String help() {
				return help;
			}
		};
	}

	//	/**
	//	 * Constructs a new casaLispOperator.  You must implement the 
	//	 * {@link #execute(TransientAgent, ParamsMap, AgentUI, Environment)} method.  The format of arglist is:
	//	 * <pre>
	//	 * {doc-string}*
	//	 * {var [type-spec] [doc-string]}*
	//	 * [&OPTIONAL {{var | ( var [initform [svar]])} [type-spec] [doc-string] }*]
	//	 * [&REST var [doc-string]]
	//	 * [&KEY {var | ({var | (keyword var)} [initform [svar]]) [type-spec] [doc-string]}*
	//	 *       [&ALLOW-OTHER-KEYS]]
	//	 * [&AUX {var | (var [initform])}*])
	//	 * </pre>
	//	 * where doc-string := "!{characters}*" (a quoted string that begins with
	//	 * an exclamation mark) and type-spec := "@type-name" (a quoted string specifying
	//	 * a lisp type with an "@" prepended).
	//	 * @param name The name of the operator
	//	 * @param arglist The arglist in doc-string augmented Lambda-list format.
	//	 * @param agentClass The class of the agent for which this function is to be registered against.
	//	 * @param synonyms a ... list of synonyms to be used for this same casaLispOperator.
	//	 * @throws ControlTransfer for any number of reasons
	//	 * @deprecated Use {@link #CasaLispOperator(String,String,Class<? extends TransientAgent>,Class,String...)} instead
	//	 */
	//	public CasaLispOperator(String name, String arglist, Class<? extends TransientAgent> agentClass, String... synonyms) {
	//		this(name, arglist, agentClass, new Object() { }.getClass().getEnclosingClass(), synonyms);
	//	}

	/**
	 * Constructs a new casaLispOperator.  You must implement the 
	 * {@link #execute(TransientAgent, ParamsMap, AgentUI, Environment)} method.  The format of arglist is:
	 * <pre>
	 * {doc-string}*
	 * {var [type-spec] [doc-string]}*
	 * [&OPTIONAL {{var | ( var [initform [svar]])} [type-spec] [doc-string] }*]
	 * [&REST var [doc-string]]
	 * [&KEY {var | ({var | (keyword var)} [initform [svar]]) [type-spec] [doc-string]}*
	 *       [&ALLOW-OTHER-KEYS]]
	 * [&AUX {var | (var [initform])}*])
	 * </pre>
	 * where doc-string := "!{characters}*" (a quoted string that begins with
	 * an exclamation mark) and type-spec := "@type-name" (a quoted string specifying
	 * a lisp type with an "@" prepended).
	 * @param name The name of the operator
	 * @param arglist The arglist in doc-string augmented Lambda-list format.
	 * @param agentClass The class of the agent for which this function is to be registered against (may be null).
	 * @param definingClass The class that defines this operator (for help functions)
	 * @param synonyms a ... list of synonyms to be used for this same casaLispOperator.
	 * @throws ControlTransfer for any number of reasons
	 */
	public CasaLispOperator(String name, String arglist, Class<? extends TransientAgent> agentClass, Class<?> definingClass, String... synonyms) {
		super(name.toUpperCase(), org.armedbear.lisp.Lisp.PACKAGE_CL_USER, true, arglist);

		this.name = name.toUpperCase();
		this.definingClass = definingClass;
		this.classRestriction = agentClass; //OK if this is null;

		// record this entry
		all.add(this);

		String lambdaList;
		// fix the lambda list by chopping out the doc-string and type-spec syntax parts
		{
			StringBuilder strippedArglist = new StringBuilder();
			int len = arglist.length();
			int i = 0, start = 0;
			for (int          end=CASAUtil.scanFor(arglist, ((i=CASAUtil.scanFor(arglist, start, "\""))+1), "\"");
					i>=0; 
					start=end+1, end=CASAUtil.scanFor(arglist, ((i=CASAUtil.scanFor(arglist, start, "\""))+1), "\"")) {
				strippedArglist.append(arglist.substring(start,arglist.charAt(i+1)=='!'?i:end+1));
			}
			strippedArglist.append(arglist.substring(start,len));
			lambdaList = strippedArglist.toString();

			strippedArglist = new StringBuilder();
			len = lambdaList.length();
			i = 0; start = 0;
			for (int          end=CASAUtil.scanFor(lambdaList, ((i=CASAUtil.scanFor(lambdaList, start, "\""))+1), "\"");
					i>=0; 
					start=end+1, end=CASAUtil.scanFor(lambdaList, ((i=CASAUtil.scanFor(lambdaList, start, "\""))+1), "\"")) {
				strippedArglist.append(lambdaList.substring(start,lambdaList.charAt(i+1)=='@'?i:end+1));
			}
			strippedArglist.append(lambdaList.substring(start,len));
			lambdaList = strippedArglist.toString();

			setLambdaList(new SimpleString(lambdaList));
		}

		try {
			LispObject def;

			StringInputStream in = new StringInputStream(arglist);
			LispThread thread = LispThread.currentThread();
			def = org.armedbear.lisp.Lisp.NIL;
			while (true) {
				LispObject obj = in.read(false, org.armedbear.lisp.Lisp.EOF, false, thread, Stream.currentReadtable);
				if (obj == org.armedbear.lisp.Lisp.EOF) break;
				def = new Cons(obj,def);
			}
			def = def.nreverse();

			// doc strings
			StringBuilder docSB = new StringBuilder();
			while (def!=null && def!=org.armedbear.lisp.Lisp.NIL && def.car() instanceof SimpleString && def.car().getStringValue().charAt(0)=='!') {
				docSB.append(((String)def.car().getStringValue()).substring(1)).append(docSB.toString().endsWith(".")?"":".").append('\n');
				def = def.cdr();
			}
			if (definingClass!=null)
				docSB.append("  Defined in class ").append(definingClass.toString()).append(".\n");

			// ordinary vars
			// {var}*
			required = 0;
			while (def!=null && def!=org.armedbear.lisp.Lisp.NIL && def.car() instanceof Symbol && ((Symbol)def.car()).getName().charAt(0)!='&') {
				Entry entry = new Entry(((Symbol)def.car()).getName(),null,null); 
				ordered.add(entry);
				def = def.cdr();
				required++;
				if (def !=null && def!=org.armedbear.lisp.Lisp.NIL && def.car() instanceof SimpleString && def.car().getStringValue().charAt(0)=='@') {
					try {
						String className = primName2ClassName(def.car().getStringValue().substring(1));
						entry.type = Class.forName(className);
					} catch (Exception e) {
						Trace.log("error","casaLispOperator.contructor: Can't find class "+def.car().getStringValue().substring(1), e);
					}
					def = def.cdr();
				}
				if (def !=null && def!=org.armedbear.lisp.Lisp.NIL && def.car() instanceof SimpleString && def.car().getStringValue().charAt(0)=='!') {
					entry.doc = def.car().getStringValue().substring(1);
					def = def.cdr();
				}
				docSB.append("    ").append(pad(entry.name,15));
				if (entry.type!=null) docSB.append(" (").append(entry.type.getCanonicalName()).append(')');
				if (entry.value!=null) docSB.append(" [def=").append(printName(entry.value)).append("]");
				if (entry.doc!=null) docSB.append(' ').append(entry.doc);
				docSB.append('\n');
			}

			// &OPTIONAL keyword
			// [&OPTIONAL {var | ( var [initform [svar]])}*]
			if (def!=null && def!=org.armedbear.lisp.Lisp.NIL && def.car() instanceof Symbol && "&OPTIONAL".equalsIgnoreCase(((Symbol)def.car()).getName())) { 
				def = def.cdr();
				while (def!=null && def!=org.armedbear.lisp.Lisp.NIL && (def.car() instanceof Cons || (def.car() instanceof Symbol && ((Symbol)def.car()).getName().charAt(0)!='&'))) { // optional vars
					Entry entry = readVar(def);
					ordered.add(entry);
					def = def.cdr();
					if (def !=null && def!=org.armedbear.lisp.Lisp.NIL && def.car() instanceof SimpleString && def.car().getStringValue().charAt(0)=='@') {
						entry.type = Class.forName(primName2ClassName(def.car().getStringValue().substring(1)));
						def = def.cdr();
					}
					if (def !=null && def!=org.armedbear.lisp.Lisp.NIL && def.car() instanceof SimpleString && def.car().getStringValue().charAt(0)=='!') {
						entry.doc = def.car().getStringValue().substring(1);
						def = def.cdr();
					}
					docSB.append("    ").append(pad(entry.name,15)).append(" [optional");
					if (entry.value!=null) docSB.append(" def=").append(printName(entry.value));
					docSB.append("]");
					if (entry.type!=null) docSB.append(" (").append(entry.type.getCanonicalName()).append(')');
					if (entry.doc!=null) docSB.append(' ').append(entry.doc);
					docSB.append('\n');
				}
			}

			// &REST keyword
			// [&REST var]
			if (def!=null  && def!=org.armedbear.lisp.Lisp.NIL && def.car() instanceof Symbol && "&REST".equalsIgnoreCase(((Symbol)def.car()).getName())) { 
				def = def.cdr();
				String key = def.car().writeToString();
				def = def.cdr();
				String docString = null;
				if (def !=null  && def!=org.armedbear.lisp.Lisp.NIL && def.car() instanceof SimpleString && def.car().getStringValue().charAt(0)=='!') {
					docString = def.car().getStringValue().substring(1);
					def = def.cdr();
				}
				Entry entry = new Entry(key,new SimpleString("&REST"),null,null,docString);
				ordered.add(entry);
				docSB.append("    ").append(pad(entry.name,15));
				if (entry.value!=null) docSB.append(" [def=").append(printName(entry.value)).append("]");
				if (entry.doc!=null) docSB.append(' ').append(entry.doc);
				docSB.append('\n');
			}

			// &KEY keyword
			// [&KEY {var | ({var | (keyword var)} [initform [svar]])}*
			if (def!=null && def!=org.armedbear.lisp.Lisp.NIL && def.car() instanceof Symbol && "&KEY".equalsIgnoreCase(((Symbol)def.car()).getName())) { 
				def = def.cdr();
				while (def!=null && def!=org.armedbear.lisp.Lisp.NIL && (def.car() instanceof Cons || (def.car() instanceof Symbol && ((Symbol)def.car()).getName().charAt(0)!='&'))) { // optional vars
					Entry entry = readVar(def);
					keyed.put(entry.name, entry);
					def = def.cdr();
					if (def !=null && def!=org.armedbear.lisp.Lisp.NIL && def.car() instanceof SimpleString && def.car().getStringValue().charAt(0)=='@') {
						entry.type = Class.forName(primName2ClassName(def.car().getStringValue().substring(1)));
						def = def.cdr();
					}
					if (def !=null && def!=org.armedbear.lisp.Lisp.NIL && def.car() instanceof SimpleString && def.car().getStringValue().charAt(0)=='!') {
						entry.doc = def.car().getStringValue().substring(1);
						def = def.cdr();
					}
					docSB.append("    :").append(pad(entry.name,14));
					if (entry.value!=null) docSB.append(" [def=").append(printName(entry.value)).append("]");
					if (entry.type!=null) docSB.append(" (").append(entry.type.getCanonicalName()).append(')');
					if (entry.doc!=null) docSB.append(' ').append(entry.doc);
					docSB.append('\n');
				}
			}

			// &ALLOW-OTHER-KEYS keyword
			// [&ALLOW-OTHER-KEYS]
			if (def!=null && def!=org.armedbear.lisp.Lisp.NIL && def.car() instanceof Symbol && "&ALLOW-OTHER-KEYS".equalsIgnoreCase(((Symbol)def.car()).getName())) { 
				def = def.cdr();
				allowOtherKeys = true;
			}

			if (docSB.length()>0) {
				doc = docSB.toString();
				try {
					Symbol opSym = org.armedbear.lisp.Lisp.intern(this.name, org.armedbear.lisp.Lisp.PACKAGE_CL_USER);
					opSym.setDocumentation(Symbol.FUNCTION, new SimpleString(doc));
				} catch (ControlTransfer e) {
					Trace.log("error", "Lisp error", e);
				}
			}

			//make symbols for any synonyms
			if (synonyms!=null) {
				for (String syn: synonyms) {
					makeSynonym(syn);
				}
			}

		} catch (Throwable e) {
			Trace.log("error", "CasaLispOperator", e);
		}
	}

	static public String primName2ClassName(String className) {
		if ("boolean".equals(className)) return "java.lang.Boolean";
		if ("byte".equals(className)) return "java.lang.Byte";
		if ("char".equals(className)) return "java.lang.Character";
		if ("double".equals(className)) return "java.lang.Double";
		if ("float".equals(className)) return "java.lang.Float";
		if ("int".equals(className)) return "java.lang.Integer";
		if ("long".equals(className)) return "java.lang.Long";
		if ("short".equals(className)) return "java.lang.sShort";
		return className;
	}

	/**
	 * Return s padded out with space characters so that the length of return
	 * string is n or more.
	 * @param s The string to pad out.
	 * @param n The minimum length of the return string
	 * @return A string that start with s, and has a length of at least n
	 */
	private String pad(String s, int n) {
		StringBuilder sb = new StringBuilder();
		sb.append(s);
		for (int i=(n-s.length()); i>0; i--) sb.append(' ');
		return sb.toString();
	}

	/**
	 * Read a variable from an atom that's either a simple "var" form
	 * or the "(var default)" form (the "(var default svar)" form is not
	 * yet implemented).  
	 * @param def The definition in Cons form.
	 * @return A new Entry describing the var definition
	 * @throws ControlTransfer 
	 */
	private Entry readVar(LispObject def) throws ControlTransfer {
		if (def.car() instanceof Cons) {
			LispObject varList = def.car(); 
			LispObject var = varList.car();
			if (!(var!=null && var!=org.armedbear.lisp.Lisp.NIL && var instanceof Symbol)) 
				throw new LispException("Expected a Symbol but got '"+var.toString()+"'");
			varList = varList.cdr();
			LispObject val = null;
			if (varList!=null && varList!=org.armedbear.lisp.Lisp.NIL) {
				val = varList.car();
				varList = varList.cdr();
			}
			String svar = null;
			if (varList!=null && varList!=org.armedbear.lisp.Lisp.NIL) {
				svar = varList.car().writeToString();
			}
			return new Entry(((Symbol)var).getName(),val,null,svar);
			// TODO need to account for the svar part!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

		} else {
			LispObject var = def.car();
			if (!(var!=null && var!=org.armedbear.lisp.Lisp.NIL && var instanceof Symbol)) 
				throw new LispException("Expected a Symbol but got '"+var.toString()+"'");
			return new Entry(((Symbol)var).getName(),null,null);
		}

	}

	/** 
	 * Interprets the lisp command according specification in the constructor and 
	 * calls {@link #execute(TransientAgent, ParamsMap, AgentUI, Environment)} for the actual
	 * implementation of the command with the appropriate parameters filled
	 * out.
	 * @see org.armedbear.lisp.LispObject#execute(org.armedbear.lisp.LispObject, org.armedbear.lisp.Environment)
	 */
	@Override
	public LispObject execute(LispObject args, Environment env) throws ControlTransfer {
		TransientAgent agent = (TransientAgent)casa.abcl.Lisp.lookupAsJavaObject(env, "agent");

		AgentUI ui = (AgentUI)casa.abcl.Lisp.lookupAsJavaObject(env, "ui");

		ParamsMap params = new ParamsMap();

		LispObject sbFileName = LispThread.currentThread().lookupSpecial(Lisp._SOURCE_);
		String source = sbFileName==null?null:((Pathname)sbFileName).getNamestring();
		params.put("__SOURCE_", new Pair<Object,LispObject>(source,sbFileName), false);

		// non-key arguments
		int c = 0;
		int len = ordered.size();
		LispObject marker = null;
		boolean hasRest = false;
		while (c<len && args!=org.armedbear.lisp.Lisp.NIL && args.car().writeToString().charAt(0)!=':') {
			Entry entry = ordered.get(c++);
			if (entry.value instanceof SimpleString && "&REST".equalsIgnoreCase(entry.value.getStringValue())) {
				StringBuilder rest = new StringBuilder();
				marker = args;
				LispObject restCons = null, restConsP = null;
				hasRest = true;
				while(args!=org.armedbear.lisp.Lisp.NIL) {
					rest.append(args.car().writeToString()).append(' ');
					if (restCons==null) restCons = restConsP = new Cons(evalDefault(args.car(),env));
					else {
						restConsP = ((Cons)restConsP).cdr = new Cons(evalDefault(args.car(), env));
					}
					args = args.cdr();
				}
				params.put(entry.name, new Pair<Object,LispObject>(rest.toString().trim(),restCons), false);
				break;
			}
			else {
				LispObject lispObj = args.car();
				LispObject evaled = evalDefault(lispObj, env);
				Object javaObj = checkType(entry,evaled,env, params); // will throw if the type doesn't match
				params.put(entry.name, new Pair<Object,LispObject>(javaObj, evaled), false);		
				if (entry.svar!=null) {
					params.put(entry.svar, true, org.armedbear.lisp.Lisp.T, true);
				}
			}
			args = args.cdr();
		}

		//check cardinality of non-key args
		if (c<required) throw new LispException("operator \""+name+"\" requires at least "+required+" arguments");
		if (!(args==org.armedbear.lisp.Lisp.NIL || args.car().writeToString().charAt(0)==':'))
			throw new LispException("Operator \""+name+"\" requires at most "+len+" non-key arguments");

		//fill in any defaults in the &optional parameters
		while (c<len) {
			Entry entry = ordered.get(c++);
			if (!(entry.value instanceof SimpleString && "&REST".equalsIgnoreCase(entry.value.getStringValue()))) {
				if (entry.value!=null) {
					LispObject evaled = evalDefault(entry.value, env);
					Object javaObj = checkType(entry,evaled,env, params); // will throw if the type doesn't match
					params.put(entry.name, new Pair<Object,LispObject>(javaObj, evaled), true);						
				}
			}
		}
		if (marker!=null && marker!=org.armedbear.lisp.Lisp.NIL) args = marker;

		// keyed arguments
		while (args!=org.armedbear.lisp.Lisp.NIL) {
			String key = args.car().writeToString();
			if (key.charAt(0)!=':') {
				if (hasRest) {
					args = args.cdr();
					continue;
				} else {
					throw new LispException("Operator \""+name+"\": Expecting keyword starting with ':', found \""+key+"\"");
				}
			}
			key = key.substring(1);
			if ((!allowOtherKeys && !keyed.containsKey(key)) /*|| "allow-other-keys".equals(key)*/) {
				throw new LispException("Operator \""+name+"\": key :"+key+" not allowed");
			}
			args = args.cdr();
			Entry entry = keyed.get(key);
			if (entry == null) { //it's an unrecognized key with ALLOW-OTHER-KEYS specified
				entry = new Entry(key,null,null);
			}
			if (args!=org.armedbear.lisp.Lisp.NIL && args.car().writeToString().charAt(0)!=':') {//there is a value
				LispObject evaled = evalDefault(args.car(), env);
				Object javaObj = checkType(entry,evaled,env, params); // will throw if the type doesn't match
				params.put(key, javaObj, evaled, false);						
				args = args.cdr();
			}
			else { //value is missing
				if (entry.value == null) {
					//in the case of a Boolean-typed key, we return T if the value is missing
					params.put(key, (entry.type==Boolean.class)?true:null, (entry.type==Boolean.class)?org.armedbear.lisp.Lisp.T:org.armedbear.lisp.Lisp.NIL, true);
				}
				else {
					LispObject evaled = evalDefault(entry.value, env);
					Object javaObj = checkType(entry,evaled,env, params); // will throw if the type doesn't match
					params.put(key, javaObj, evaled, true);
				}
			}
			//fill in the svar if necessary
			if (entry!=null) {
				if (entry.svar!=null) params.put(entry.svar, true, org.armedbear.lisp.Lisp.T, false);
			}
		}

		//fill in any defaults for keys
		for (String key: keyed.keySet()) {
			if (!params.containsKey(key)) {
				Entry entry = keyed.get(key);
				LispObject v = entry.value;
				if (v!=null) {
					LispObject evaled = evalDefault(v, env);
					Object javaObj = checkType(entry,evaled,env, params); // will throw if the type doesn't match
					params.put(key, javaObj, evaled, true);
				}
			}
		}

		// TODO need to implement the aux vars

		Status stat;
		try {
			stat = execute(agent, params, ui, env);
		} catch (ControlTransfer e) {
			String msg = e.getMessage();
			if (msg==null) {
				Condition cond = (Condition)e.getCondition();
				if (cond!=null) {
					String report = cond.getConditionReport();
					if (report!=null) msg = report;
				}
			}
			ControlTransfer e1 = new LispException((msg==null?"[no message]":msg)+"\n"+toString(params));
			throw e1;
		} catch (Throwable e) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			PrintWriter pw = new PrintWriter(out);
			for (Throwable th = e; th!=null; th = th.getCause()) {
				if (th!=e) pw.println("Caused by:");
				e.printStackTrace(pw);
			}
			pw.flush();
			ControlTransfer e1 = new LispException("Unexpected non-ControlTransfer exception during execution of lisp operator "+name+":\n"+out);
			throw e1;
		}

		//If the first condition returns a LispError instead of a JavaObject, agent.reply
		//throws an exception and the receiving agent will not be notified of the perform's
		//failure/refuse/etc... - dsb
		if (stat==null) return org.armedbear.lisp.Lisp.NIL;
		if (stat.getStatusValue()<0)	return new JavaObject(stat);//return new org.armedbear.lisp.LispError(stat.getExplanation());
		if (stat.getStatusValue()>0) ui.println("/* Warning: "+stat.getExplanation()+" */");
		if (stat instanceof StatusObject<?>) {
			return casa.abcl.Lisp.javaObj2LispObj(((StatusObject<?>)stat).getObject());
		} else if (stat instanceof PerformDescriptor) 
			return new JavaObject(stat);
		else { // simple status
			return org.armedbear.lisp.Lisp.NIL;
		}
	}

	/**
	 * Look for a symbol named <em>name</em> in the lisp environment and return it's value.
	 * @param <Ty> The type to return (this will be checked)
	 * @param name The name of the lisp Symbol
	 * @param env The lisp environment, which will be used to check for symbol values
	 * @return The object of type Ty that is the value of the symbol, if the symbol or its value doesn't exist,
	 * or the value isn't of type Ty, returns null.
	 * @throws ClassCastException if the type doesn't conform to type Ty
	 */
	@SuppressWarnings("unchecked")
	public <Ty> Ty getSymbolValue(String name, Environment env) throws ClassCastException {
		Symbol ontSymbol = (Symbol) Lisp.PACKAGE_CL_USER.findSymbol(name);
		if (ontSymbol!=null) {
			LispObject lo = env.lookup(ontSymbol);
			if (lo!=null && lo instanceof JavaObject) {
				Object jo = ((JavaObject)lo).getObject();
				return (Ty)jo;
			}
		}
		return null;
	}

	/**
	 * @param entry The entry
	 * @param javaObj The java object version of the value
	 * @param lispObj The lisp object version of the value
	 * @param env The lisp environment
	 * @param params The params map
	 * @throws ControlTransfer if there's a parameter mismatch with the entry
	 */
	private Object checkType(Entry entry, LispObject lispObj, Environment env, ParamsMap params) throws ControlTransfer {
		//LispObject evaled = evalDefault(unEvaled, env);
		if (!Boolean.class.equals(entry.type) && lispObj==org.armedbear.lisp.Lisp.NIL)
			return null;
		Object javaObj = casa.abcl.Lisp.lispObj2JavaObj(lispObj);
		if (entry != null) {
			Class<?> cls = entry.type;
			if (cls != null) { // just return if the target class is null
				if (!(/*Boolean.FALSE.equals(javaObj) ||*/ cls.isInstance(javaObj))) { // just return if the object is an instance of the target class or NIL
					if (javaObj instanceof String) { // if the object is a string, we might be able to coerce...
						try {
							if (cls.equals(java.lang.Integer.class)) {
								Integer.parseInt((String)javaObj); // will throw NumberFormatException if it can't be coerced
								return javaObj;
							}
							if (cls.equals(java.lang.Float.class)) {
								Float.parseFloat((String)javaObj); // will throw NumberFormatException if it can't be coerced
								return javaObj;
							}
							if (cls.equals(java.lang.Boolean.class)) {
								Boolean.parseBoolean((String)javaObj); // will throw NumberFormatException if it can't be coerced
								return javaObj;
							}
						} catch (NumberFormatException e) {
							// do nothing, and we'll fall through to throwing an error.
						}
					}

					//			  	else if (lispObj instanceof Cons) {
					//			  		evalDefault(lispObj, env);
					//			  	}

					throw new LispException(toString(params)+": Type mismatch on parameter '"+entry.name
							+"', expected type "+entry.type.getCanonicalName()+" but got '"
							+lispObj.writeToString()+"' of type "+javaObj.getClass()+".");
				}
			}
		}
		return javaObj;
	}

	/**
	 * Interpret the argument o in the context of the lisp environment.
	 * If the argument object o is a Symbol, look it up in the environment.
	 * If the argument object o is a Cons list, interpret it as lisp would, possibly
	 * by evaluating it as a function.
	 * Otherwise, just return o. 
	 * @param o The value to interpret.
	 * @param env The lisp Environment in which to interpret the value.
	 * @return A lisp object that is the interpretation of the value.
	 * @throws ControlTransfer
	 */
	private LispObject evalDefault(LispObject o, Environment env) throws ControlTransfer {
		if (o==null) return null;
		if (o instanceof Symbol) {
			Symbol s = (Symbol)o;
			LispObject interpreted = env.lookup(s);
			if (interpreted!=null) return interpreted;
			if (s.getSymbolValue()!=null) o = s.getSymbolValue();
		}
		else if (o instanceof org.armedbear.lisp.Cons)
			o = org.armedbear.lisp.Lisp.eval(o, env, LispThread.currentThread());
		return o;
	}

	/**
	 * Subclasses need to override the method to implement the actual operator.
	 * This method should return a Status:
	 * <table>
	 * <tr><th>Return</td><td>Indicates...</td></tr>
	 * <tr><td>Status(0)</td><td>the method was successful, but there is no return value (possibly it printed to <code>ui</code>); will return NIL</td></tr>
	 * <tr><td>Status(-ve,"message")</td><td>the method failed and the return will be a lisp error object with "message" as its message</td></tr>
	 * <tr><td>Status(+ve,"warning")</td><td>the method was successful with warnings, but there is no return value (possibly it printed to <code>ui</code>); will return NIL and print the warning to <code>ui</code></td></tr>
	 * <tr><td>StatusObject(0,LispObject)</td><td>the method was successful, and will directly return object</td></tr>
	 * <tr><td>Status(+ve,"warning")</td><td>the method was successful with warnings, and will return object and print the warning to <code>ui</code></td></tr>
	 * </table>
	 * @param agent The calling agent
	 * @param params The dictionary of all the actual parameter name/value pairs that appear in function call. 
	 * @param ui The AgentUI object that this method may use for input and output
	 * @param env The Lisp environment to use for this execution -- you can use this to instantiate symbols, etc in the context of Lisp execution.
	 * @return A suitable Status (see above).
	 * @throws ControlTransfer
	 */
	public abstract Status execute (TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) throws ControlTransfer;

	/**
	 * @return  The name of the operator
	 */
	public String getName() {return name;}

	/**
	 * @return  The full doc string for the operator
	 */
	public String getDoc() {return doc;}

	/**
	 * @return The shorted doc string for the operator.  Truncated at either
	 * the first "." or the the first newline.
	 */
	public String getDocShort() {
		String ret = doc.substring(0, casa.util.CASAUtil.scanFor(doc,0,".\n")+1).trim();
		return ret;
	}

	/**
	 * 
	 * @param params the parameter object.
	 * @return the ParamsMap object interpreted as a String
	 */
	public String toString(ParamsMap params) {
		StringBuilder buf = new StringBuilder();
		buf.append('(').append(name).append(' ');
		for (Entry e: ordered) {
			buf.append(params.getJavaObject(e.name)).append(' ');
		}
		for (String key: keyed.keySet()) {
			Object val = params.getJavaObject(key);
			if (val!=null) {
				buf.append(':').append(key).append(' ').append(val).append(' ');
			}
		}
		buf.append(')');
		return buf.toString();
	}

	/**
	 * Will return an iterator over all the casaLispOperators that were defined
	 * for (by?) agents of the parameter class.
	 * @param agentClass A class object that will be used to filter the next()'s of the iterator.  Only operators compatible with the class are returned. 
	 * @return An iterator
	 */
	public static Iterator<CasaLispOperator> iteratorFor(final Class<? extends TransientAgent> agentClass) {
		return new Iterator<CasaLispOperator>() {
			Iterator<CasaLispOperator> it = null;
			void init() {
				TreeSet<CasaLispOperator> list = new TreeSet<CasaLispOperator>();
						for (CasaLispOperator o: all) {
							if (o.classRestriction==null || agentClass==null || o.classRestriction.isAssignableFrom(agentClass))
								list.add(o);
						}
				it = list.iterator();
			}
			public boolean hasNext() {
				if (it==null) init();
				return it.hasNext();
			}

			public CasaLispOperator next() {
				if (it==null) init();
				return it.next();
			}

			public void remove() {
			}
		};
	}

	/**
	 * A simple class describing an entry in {@link #ordered} or {@link #keyed}.
	 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
	 */
	class Entry {
		public String name;
		public LispObject value;
		public Class<?> type;
		public String svar;
		public String doc;
		public Entry(String name, LispObject lispObject, Class<?> type) {
			this(name, lispObject, type, null, null);
		}
		public Entry(String name, LispObject lispObject, Class<?> type, String svar) {
			this(name, lispObject, type, svar, null);
		}
		public Entry(String name, LispObject lispValue, Class<?> type, String svar, String doc) {
			this.name = name.toUpperCase();
			this.value = lispValue;
			this.type = type;
			this.svar = svar;
			this.doc = doc;
		}
		@Override
		public String toString() {
			return "("+name+" "+value+" "+type+" \"!"+doc+"\")\n";
		}

	}

	/**
	 * Lisp operator: (?)<br>
	 * Displays the list of all casa lisp operators with the first sentence of documentation.
	 * Returns NIL.
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator HELP =
	new CasaLispOperator("HELP", "\"!Displays list of casa functions.\" "
			+"&OPTIONAL APROPOSE \"@java.lang.String\" \"!A string to match to select a subset of casa functions.\" "
			+"&KEY (LATEX NIL) \"@java.lang.Boolean\" \"!Print the output in LaTex format (not a full document, just a set of \\subsection's).\" "
			, TransientAgent.class, new Object() { }.getClass().getEnclosingClass(), "?")
	{
		Map<String, String> helpEntries = new TreeMap<String,String>() {
			@Override
			public String put(String key, String value) {
				return super.put(key.toUpperCase(), value);
			}
		};
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
			StringBuilder s = new StringBuilder();
			String match = (String)params.getJavaObject("APROPOSE");
			boolean latex = (Boolean)params.getJavaObject("LATEX");
			if (latex) {
				s.append("%*********************************************************************\n%***begin casa-lisp generated text (use (? :latex T))*****************\n%*********************************************************************\n")
				.append("\\section{Lisp Commands}\n")
				.append("\\label{sec:LispCommands}\n\n");
			}
			for (Iterator<CasaLispOperator> i=CasaLispOperator.iteratorFor(latex?null:agent.getClass()); i.hasNext();) {
				CasaLispOperator o = i.next();
				if (match==null 
						|| o.getName().toUpperCase().indexOf(match.toUpperCase())>=0 
						|| o.getDocShort().toUpperCase().indexOf(match.toUpperCase())>=0 
						|| o.getDoc().toUpperCase().indexOf(match.toUpperCase())>=0 ) {
					if (latex)
						appendLatex(o, agent);
					else
						append(o);
				}
			}
			for (String key: helpEntries.keySet()) {
				s.append(helpEntries.get(key));
			}
			if (latex) {
				s.append("%*********************************************************************\n%***end casa-lisp generated text (use (? :latex T))*******************\n%*********************************************************************\n\n");
			}
			else {
				s.append("Use (describe '<topic>) for more detail.\n");
			}
			ui.println(s.toString());
			return new Status(0);
		}
		private void append(CasaLispOperator o) {
			StringBuilder s = new StringBuilder();
			s.append(o.getName());
			int padding = o.getName().length();
			if (o.synonyms != null) {
				for (String syn: o.synonyms) {
					s.append(',').append(syn);
					padding += (syn.length()+1);
				}
			}
			for (int j=Math.max(1,(15-padding)); j>0; j--) s.append(' ');
			s.append(o.getDocShort()).append('\n');
			helpEntries.put(o.getName(), s.toString());
		}
		private void appendLatexSynonym(String name, String refName) {
			StringBuilder s = new StringBuilder();
			s.append("%***Casa-lisp generated text. (use (? [command-name] :latex T))\n\\subsection{")
			.append(latexFix(name)).append("}\n\\label{LispCommand:").append(latexFix(name)).append("}\n")
			.append(latexFix(name)).append(" is a synonym for ").append(latexFix(refName)).append(" (Section \\ref{LispCommand:").append(latexFix(refName)).append("}).\n");
			helpEntries.put(name, s.toString());
		}
		private void appendLatex(CasaLispOperator o, TransientAgent agent) {
			StringBuilder s = new StringBuilder();
			Status stat = agent.abclEval("(describe '"+o.getName()+")", null);
			s.append("%***Casa-lisp generated text. (use (? [command-name] :latex T))\n\\subsection{")
			.append(o.getName()).append("}\n\\label{LispCommand:").append(latexFix(o.getName())).append("}\n");
			//get the output from DESCRIBE
			String buf = ((StatusObject<?>)stat).getExplanation();
			//trim the preamble from the output
			int index = buf.indexOf(':'); 
			if (index>0) {
				buf = buf.substring(index+1);
			}
			int state = 0;
			boolean tabHeaderPrinted = false;
			for (String line: buf.split("\n")) {
				line = latexFix(line);
				switch (state) {
				case 0: // The binding info
					if ("The function's lambda list is:".equals(line)) {
						if (o.synonyms!=null) {
							s.append("Its synonyms are");
							String sep = "";
							for (String syn: o.synonyms) {
								s.append(sep).append(" \\emph{").append(latexFix(syn)).append("}");
								sep = ",";
								appendLatexSynonym(syn, o.getName());
							}
							s.append(".\n");
						}
						s.append("\\subsubsection{Lambda list}\n");
						state = 1;
					}
					else {
						s.append(line).append('\n');
					}
					break;
				case 1: // The lambda list
					if ("Function documentation:".equals(line)) {
						s.append("\\subsubsection{Function documentation}\n");
						state = 2;
					}
					else {
						s.append(line).append('\n');
					}
					break;
				case 2: // The main doc lines
					if (line.startsWith("    ")) {
						state = 3;
					}
					else {
						s.append(line).append('\n');
						break;
					}
				case 3: // parameters' doc
					String def = "";
					String type = "";
					line = line.trim();
					index = line.indexOf(' ');
					String param = line;
					if (index>0) {
						param = line.substring(0,index);
						line = line.substring(index).trim();
						if (line.startsWith("[")) {
							index = line.indexOf(']');
							def = line.substring(1, index);
							line = line.substring(index+1).trim();
						}
						if (line.startsWith("(")) {
							index = line.indexOf(')');
							type = line.substring(1, index);
							line = line.substring(index+1).trim();
						}
					}
					if (!tabHeaderPrinted) {
						tabHeaderPrinted = true;
						s.append("\n\\noindent\n\\footnotesize\n\\begin{tabular*}\n{\\textwidth}{@{\\extracolsep{\\fill}} | @{ } p{0.20\\textwidth} @{ } | @{ } p{0.15\\textwidth} @{ } | @{ } p{0.15\\textwidth} @{ } | @{ } p{0.46\\textwidth} @{ } | }")
						.append("\\hline\n\\textbf{Parameter} & \\textbf{Default} & \\textbf{Type} & \\textbf{Notes} \\\\ \\hline \\hline\n");
					}
					s.append(latexCellFix(param)).append(" & ")
					.append(latexCellFix(def)).append(" & ")
					.append(latexCellFix(type)).append(" & ")
					.append(latexCellFix(line)).append(" \\\\ \\hline\n");
					break;
				}
			}
			if (tabHeaderPrinted)
				s.append("\\end{tabular*}\n\\normalsize\n");
			helpEntries.put(o.getName(), s.toString());
		}
		private String latexFix(String line) {
			String ret = line.replaceAll("#", "\\\\#");
			ret = ret.replaceAll("&", "\\\\&");
			ret = ret.replaceAll("_", "\\\\_");
			ret = ret.replaceAll("\\$", java.util.regex.Matcher.quoteReplacement("\\$"));
			ret = ret.replaceAll("<",  java.util.regex.Matcher.quoteReplacement("$<$"));
			ret = ret.replaceAll(">", java.util.regex.Matcher.quoteReplacement("$>$"));
			return ret;
		}
		private String latexCellFix(String line) {
			String ret = line.replaceAll("-", "-\\\\-");
			ret = ret.replaceAll("/", "/\\\\-");
			ret = ret.replaceAll("=", "=\\\\-");
			ret = ret.replaceAll("\\.", ".\\\\-");
			return ret;
		}
	};

	/**
	 * Lisp operator: (echo)<br>
	 * This is here for debugging and experimentation purposes only.  It prints the
	 * parameters that are received by the {@link CasaLispOperator#execute(TransientAgent, Map, AgentUI, Environment)} method.
	 * <pre>
	 *   Lambda List:  first  &optional (second 2 second_default)  third  &rest rest  &key named bool &allow-other-keys 
	 *    FIRST first param
	 *    SECOND [optional def=2.0] second optional param with default
	 *    THIRD [optional] third optional param
	 *    REST [def=&rest] the rest of the line, including keys
	 *    :NAMED1 named1 doc
	 *    :BOOL bool doc
	 * </pre> 
	 */
	@SuppressWarnings("unused")
	private static final CasaLispOperator ECHO =
	new CasaLispOperator("ECHO", "\"!Merely echos the parameters for casa operators.\" "+
			"first \"!first param\" "+
			"&OPTIONAL "+
			"(second 2 second_default) \"!second optional param with default\" "+
			"third \"!third optional param\" "+
			"&REST rest \"!the rest of the line, including keys\" "+
			"&KEY "+
			"NAMED \"!named doc\" "+
			"BOOL \"@java.lang.Boolean\" \"!bool doc\""+
			"&ALLOW-OTHER-KEYS", TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
	{
		@Override
		public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
			StringBuilder b = new StringBuilder("\n{ ; form is '(params.getJavaObject(key) [type], params.getLispObject(key) [type])'\n");
			for (String key: params.keySet()) {
				Object javaVal = params.getJavaObject(key);
				LispObject lispVal= params.getLispObject(key);
				b.append(key)
				.append(" = (");

				if (javaVal==null) {
					b.append("null");
				}
				else {
					if (javaVal instanceof LispObject)
						b.append(((LispObject)javaVal).writeToString());
					else
						b.append(javaVal.toString());
					b.append(" [type=")
					.append(javaVal.getClass().toString())
					.append("]");
				}

				b.append(", ");

				if (lispVal==null) {
					b.append("null");
				}
				else {
					b.append(lispVal.writeToString());
					b.append(" [type=")
					.append(lispVal.getClass().toString())
					.append("]");
				}

				b.append(")\n");
			}
			b.append("}");
			if (ui!=null) {
				ui.println(b.toString());
			}
			return new Status(0);
		}
	};


}
