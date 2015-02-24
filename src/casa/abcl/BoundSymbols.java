package casa.abcl;

import casa.Status;
import casa.StatusObject;
import casa.TransientAgent;
import casa.conversation2.Conversation;
import casa.exceptions.IllegalOperationException;
import casa.util.CASAUtil;
import casa.util.Trace;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.armedbear.lisp.Cons;
import org.armedbear.lisp.Environment;
import org.armedbear.lisp.JavaObject;
import org.armedbear.lisp.LispObject;

/**
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description:</p><p>
 * Supports a set of symbols and values who's value can be later looked up from both Java and Lisp 
 * (defined in an Environment object).  To support {@link Conversation}s, where symbols are 
 * declared with expressions and later resolved at instantiation time by evaluating
 * those expressions, the method {@link #bindExp(String, LispObject)} can be used to record the 
 * expression at declaration time, and the method {@link #evaluate(TransientAgent, Environment, Map)} 
 * can be used at evaluate the expressions at instantiation time.</p>
 * <p>
 * In addition, this class supports a hierarchy (not a lattice) of symbols through 
 * {@link #getParent()}, where BOTH a variable NAMES may be mapped to different names in the
 * parent, and VALUES may be mapped to different values in the parent.  Thus, 
 * {@link #getPrimitiveRaw(String)} may return the parent's
 * (or parent's parent's...) {@link Value} structure if it is referenced through 
 * {@link Value#reference}.  In addition, {@link #get(String)} and {@link #bind(String, Object)}
 * use translate VALUES through the local {@link Value#valueMap} when returning or 
 * putting values.</p>
 * <p>
 * The following diagram shows a slightly-simplified diagram of how a 2-level nested {@link #get(String)} operation
 * work for a referenced ({@link #bindTo(String, String, String)}) symbol that also has a valueMap 
 * ({@link #bindValue(String, Object, Object)}), showing two {@link Value} objects (one "local", and the other one in the parent:<br>
 * <img src="doc-files/BoundSymbols.gif" height=150 width=300></img><br>
 * The diagram shows how {@link #get(String)} looks up the the symbol, and uses the reference to map to the value in
 * the parent's Value, then uses this parent's value and to translate the result back to a local value using the local
 * valueMap.  The operation is recursive.
 * </p>
 *  
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
public abstract class BoundSymbols /*implements Cloneable*/ {
	
	/**
	 * Data associated with each named symbol
	 */
	class Value implements Cloneable {
		/** The explicit value of the object.  Ignored if {@link #reference} is not null. */
		Object value = null;
		
		/** The name of the Value object in the parent that this object defers to. */
		String reference = null;
		
		/** The expression that is evaluated by 
		 * {@link BoundSymbols#evaluate(TransientAgent, Environment, Map)}.
		 * Ignored if {@link #reference} is not null. */
		LispObject expression = null;
		
		/** Used to indicate that {@link #expression} has already been evaluated by
		 * {@link BoundSymbols#evaluate(TransientAgent, Environment, Map)}. */
		boolean evaluated = false;
		
		/** The map of values to values in the parent Value structure this is
		 * to be used to translate when getting and putting values. */
	  Map<Object,Object> valueMap = null;

	  @Override
		protected Value clone() throws CloneNotSupportedException {
			Value ret = (Value)super.clone();
      if (valueMap!=null) ret.valueMap = new TreeMap<Object,Object>(valueMap); 
			return ret;
		}

	  /** 
	   * Constructor to make a simple local-value Value.
	   * @param value The value to be associated with the Value
	   */
	  Value(Object value) {this.value=value; evaluated=true;}
	  
	  /** Constructor to make a simple reference to a parent Value.
	   * @param reference The name of the reference value in the parent,
	   * where the name of this value need not be the same of the name
	   * of this {@link BoundSymobls} object. 
	   */
		Value(String reference) {this.reference = reference;}
		
		/** 
		 * Constructor to make a simple local-expression Value, where the 
		 * expression's result is placed in value by 
		 * {@link BoundSymbols#evaluate(TransientAgent, Environment, Map)}.
		 * The expression must be evaluated before the value my 
		 * {@link BoundSymbols#evaluate(TransientAgent, Environment, Map)}
		 * before {@link BoundSymbols#get(String)} will safely return a
		 * value.
		 * @param exp The expression; normally a Lisp Cons object.
		 */
		Value(LispObject exp) {expression = exp;}
		
	  /**
	   * Prints out this object in the form:
	   * <pre>
	   * [value=? reference=? expression=? evaluated=?, valueMap={?=?, ...}]
	   * </pre>
	   * Defined for debugging purposes.
	   */
		@Override
		public String toString() { // defined for debugging purposes
	  	StringBuilder b = new StringBuilder();
	  	b.append("[value=").append(value==null?"null":value)
	  	.append(" reference=").append(reference==null?"null":reference)
	  	.append(" expression=").append(expression==null?"null":expression.writeToString())
	  	.append(" evaluated=").append(evaluated)
	  	.append(" valueMap=").append(valueMap==null?"null":valueMap.toString())
	  	.append("]\n");
	  	return b.toString();
	  }
	} //class Value
	
  /**
   * The symbol/{@link Value} dictionary
   */
  private TreeMap<String,Value> vars = new TreeMap<String,Value>();
  
  /**
   * Default constructor
   */
  public BoundSymbols() {};
  
  /**
   * Copy constructor
   * @param copy Creates a deep copy of this Object
   */
  public BoundSymbols(BoundSymbols copy) {
  	vars = new TreeMap<String, Value>();
  	for (String key: copy.vars.keySet()) {
  		Value val = copy.vars.get(key);
  		try {
				vars.put(key, val.clone());
			} catch (CloneNotSupportedException e) {
				Trace.log("error", "BoundSymbols.<init>()", e);
			}
  	}
  }
  
//	@Override
//	public BoundSymbols clone() throws CloneNotSupportedException {
//		BoundSymbols ret = (BoundSymbols)super.clone();
//		ret.vars = new TreeMap<String,Value>();
//		for (String key: vars.keySet()) {
//			ret.vars.put(key, vars.get(key).clone());
//		}
//		return ret;
//	}

  /**
   * Bind a symbol to an expression.  Note that a {@link #get(String)} will throw an exception if 
   * the {@link #evaluate(TransientAgent, Environment, Map)} method is not called beforehand.
   * Note also that the expression will be ignored if this is a reference.
   * @param sym the symbol to bind
   * @param exp the expression to be bound to the symbol
   */
  public void bindExp(String sym, LispObject exp) {
  	assert !exp.writeToString().startsWith("(("); 
  	if (vars.get(sym)==null)
  	  vars.put(sym, new Value(exp));
  	else {
  		Value v = getPrimitiveRaw(sym);
  		v.expression = exp;
  		v.evaluated = false;
  	}
  }
  
  /**
   * Bind a symbol to a value.  If a valueMap is associated with
   * the symbol, the mapped value is entered.  If {@link #bindExp(String, LispObject)}
   * or {@link #bindTo(String, String)} is called subsequently, this
   * value will be ignored.
   * Note that the expression will be ignored if this is a reference.
   * @param sym the symbol to bind
   * @param val the value of to be bound to the symbol
   */
  public void bind(String sym, Object val) {
  	if (vars.get(sym)==null)
  		vars.put(sym,new Value(val));
  	else {
			Value v = getPrimitiveRaw(sym); // this *may* be there parent Value structure
	  	Value localValue = vars.get(sym); // his definitely isn't the parent Value structure
	  	//if we have a value mapping, translate before committing the change
	  	if (v!=localValue && localValue.valueMap!=null && localValue.valueMap.containsKey(val)) {
	  		val = localValue.valueMap.get(val);
	  	}
			v.value = val;
  	}
  }
  
  /**
   * Used to resolve all symbols created with the {@link #bindExp(String, LispObject)} method by evaluating the expression given and
   * assigning the result to the value of the symbol.
   * @param agent The agent in who's context to evaluate the expression in.
   * @param env The environment.
   * @param newBindings A map of bindings that will be applied to <em>env</em>.
   */
  public void evaluate(TransientAgent agent, Environment env, Map<String,LispObject> newBindings) {
  	for (String key: vars.keySet()) {
  		Value v = getPrimitiveRaw(key);
  		Status stat = null;
  		if (v.expression != null)
  			stat = casa.abcl.Lisp.abclEval(agent, env, newBindings, v.expression.writeToString(), null);
  		if (stat instanceof StatusObject<?>) {
  			v.value = ((StatusObject<?>)stat).getObject();
  		}
  		v.evaluated = true;
  	}
  }
  
  /**
   * bind localSym to the parent's symbol globalSym
   * @param localSym
   * @param globalSym
   */
  public void bindTo(String localSym, String globalSym) {
  	vars.put(localSym, new Value(globalSym));
  }
  
  /**
   * bind the childSym in the childName child to the local symbol, localSym
   * @param localSym
   * @param childName
   * @param childSym
   * @throws IllegalOperationException if childName cannot be found
   */
  public void bindTo(String localSym, String childName, String childSym) throws IllegalOperationException {
  	BoundSymbols child = getChild(childName);
  	if (child==null) 
  		throw new IllegalOperationException("BoundSymbols: no child node "+childName);
  	child.bindTo(childSym, localSym);
  }
  
  /**
   * Return the value associated with symbol <em>sym</em>.  If the {@link Value#reference} 
   * for this <em>sym</em> is null, then the local value is returned (
   * If there is valueMap for this symbol, a reverse lookup is performed, and 
   * the first match is returned.  If there is an expression for this symbol
   * it must have already been evaluated by {@link #evaluate(TransientAgent, Environment, Map)}
   * or an Exception is thrown.
   * @param sym the symbol for which to return the value for
   * @return The value associated with this symbol
   * @throws IllegalOperationException if this symbol was created with {@link #bindExp(String, LispObject)} and {@link #evaluate(TransientAgent, Environment, Map)} has not been executed since.
   */
  public Object get(String sym) throws IllegalOperationException {
  	Value localValue = vars.get(sym);
  	if (localValue==null) 
  		return null;
  	if (localValue.reference!=null) { // if this is a reference...
  		Object ret = getParent().get(localValue.reference);
  		if (localValue.valueMap!=null && localValue.valueMap.containsValue(ret)) {
  		  for (Object key: localValue.valueMap.keySet()) {
  			  if (localValue.valueMap.get(key).equals(ret))
  				  return key;
  		  }
  		}
  		return ret;
  	}
  	else if (localValue.expression!=null && !localValue.evaluated) // if this is an expression
  		throw new IllegalOperationException("BoundSymbols.get(String sym): Symbol '"+sym+"' has not been evaluated.");
  	return localValue.value;
  }
  
  /**
   * @param sym The symbol to report on
   * @return a report of the assignment with respect to both this object the parent objects.
   */
  public String reportSymbol(String sym) {
  	StringBuilder b = new StringBuilder();
  	b.append("BoundSymbol \"").append(sym).append("\" ");
  	Value localValue = vars.get(sym);
  	if (localValue==null) 
  		b.append("not defined");
  	else if (localValue.reference!=null) { // is a reference...
  		b.append("evaulates to '"+toStringValue(getSymbolTrans(sym))+"' from referenced parent symbol \"").append(localValue.reference).append("\"; ")
  		 .append(getParent().reportSymbol(localValue.reference));
  	}
  	else if (localValue.expression!=null) { // is an epression
			b.append("expression \"").append(localValue.expression.writeToString()).append("\" ");
  		if (!localValue.evaluated) // if this is an expression
  		  b.append("has not been evaluated");
  		else {
  			b.append("evaluated to '").append(toStringValue(localValue.value)).append("'");
  		}
  	}
  	else { // is a simple value
  		b.append("has value '").append(toStringValue(localValue.value)).append("'");
  	}
  	return b.toString();
  }
  
//  private Value getPrimitive(String sym) throws IllegalOperationException {
//  	Value val = vars.get(sym);
//  	if (val==null) return null;
//  	if (val.reference!=null) {
//  		BoundSymbols parent = getParent();
//			try {
//				return parent.getPrimitive(val.reference);
//			} catch (Throwable e) {
//				throw new IllegalOperationException("BoundSymbols.getPrimitive("+sym+"): Unexpected error in getting reference value from parent",e);
//			}
//  	}
//  	else {
//  		if (!val.evaluated) throw new IllegalOperationException("Symbol "+sym+" not evaluated.");
//  		return val;
//  	}
//  }
  
  /**
   * @param sym A Symbol to return the {@link Value} for.
   * @return The associated {@link Value} object, whether an ancestor's or local, or null if there is no associated value.
   */
  private Value getPrimitiveRaw(String sym) {
  	Value val = vars.get(sym);
  	if (val==null) return null;
  	if (val.reference!=null) {
  		BoundSymbols parent = getParent();
			return parent.getPrimitiveRaw(val.reference);
  	}
  	else {
  		return val;
  	}
  }
  
  /**
   * Return a new environment based on <em>env</em> with the symbols in <em>vars</em> bound.
   * @param env
   * @return the extended Environment
   * @throws IllegalOperationException if any of the symbols have not yet been evaluated.
   */
  public Environment extendEnv(Environment env) throws IllegalOperationException {
  	Environment newEnv = new Environment(env);
  	for (String sym: vars.keySet()) {
  		newEnv.bind(new org.armedbear.lisp.Symbol(sym), new JavaObject(get(sym)));
  	}
  	return newEnv;
  }
  
  /**
   * Return symbol map compatible with {@link casa.abcl.Lisp#abclEval(TransientAgent, Environment, Map, String, casa.ui.AgentUI)}'s Map parameter that represents
   * the bindings for this BoundSymbols object.
   * @return a symbol map of the symbols bound here.
   * @throws IllegalOperationException if any of the symbols have not yet been evaluated.
   */
  public Map<String, LispObject> getMap() throws IllegalOperationException {
  	Map<String, LispObject> map = new TreeMap<String, LispObject>();
  	for (String sym: vars.keySet()) {
  		Object obj = get(sym);
  		LispObject lobj;
  		if (obj instanceof LispObject) 
  			lobj = (LispObject)obj;
  		else 
  			lobj = new JavaObject(obj);
  		map.put(sym, lobj);
  	}
  	return map;
  }
  
  /**
   * @return the parent BoundSymbols object
   */
  protected abstract BoundSymbols getParent();
  
  /**
   * @param name The name of the child to return.
   * @return the child BoundSymbols object with the name <em>name</em>.
   */
  protected abstract BoundSymbols getChild(String name);
  
  /**
   * @return the names of all the child BoundSymbols objects.
   */
  protected abstract Collection<String> getChildren();
  
  /**
   * @param indent the number of character spaces to pad at the beginning of each line.
   * @return a String in the form of "([symbol] . [expression])\n ..." for each of the directly (non-bound-to) symbols contained.
   */
  public String toStringBound(int indent) {
  	StringBuilder b = new StringBuilder();
  	String comment;
  	for (String sym: vars.keySet()) {
  		comment = "";
  		Value val = vars.get(sym);
  		if (val.reference==null) {
  			CASAUtil.pad(b, indent)
  			.append("(\"")
  			.append(sym)
  			.append("\" ");
  			if (val.expression!=null) {
  				b.append(val.expression.writeToString());
  				comment = "; expression, resolves to = "+toStringValue(getSymbolTrans(sym)); //val.value);
  			}
  			else {
  				b.append(toStringValue(getSymbolTrans(sym)));//val.value));
  				comment = "; value";
  			}
  			b.append(") ")
  			.append(comment)
  			.append("\n");
  		}
  		else {
  			CASAUtil.pad(b, indent)
  			.append(";symbol \"")
  			.append(sym)
  			.append("\" references parent symbol \"")
  			.append(val.reference)
  			.append("\", resolves to '")
  			.append(toStringValue(getSymbolTrans(sym)))
  			.append("'\n");
  		}
  	}
  	return b.toString();
  }
  
  private String toStringValue(Object o) {
  	if (o==null)
  		return "NIL";
  	if (o instanceof String)
  		return "\""+o+"\"";
  	if (o instanceof JavaObject)
  		return toStringValue(((JavaObject)o).getObject());
  	if (o instanceof Conversation)
  		return toStringValue(((Conversation)o).getName()+"@id="+((Conversation)o).getId());
  	if (o instanceof Cons)
  		return ((Cons)o).writeToString();
  	if (o instanceof JavaObject)
  		return toStringValue(((JavaObject)o).getObject());
  	else 
  		return o.toString();
  }
  
  private Object getSymbolTrans(String sym) {
  	try {
			return get(sym);
		} catch (IllegalOperationException e) {
			return "<unevaluated>";
		}
  }
    
  /**
   * Usually the PARENT makes use of the returned text!!!
   * @param indent the number of character spaces to pad at the beginning of each line.
   * @return a String in the form of "([parentValue] [myName] [myValue])\n ..." for each of the value bindings.
   */
  public String toStringBoundValues(int indent, String sym, String myName) {
		Value val = vars.get(sym); //specifically NOT getPrimitive() because that might return a parent Value
		if (val==null) 
			return "";
		if (val.valueMap==null) 
			return "";

  	StringBuilder b = new StringBuilder();
  	for (Object key: val.valueMap.keySet()) {
  		Object value = val.valueMap.get(key);
			CASAUtil.pad(b, indent)
			.append("(\"").append(value).append("\" \"").append(myName).append("\" \"").append(key).append("\")\n");
  	}
  	return b.toString();
  }
  
  /**
   * @param indent the number of character spaces to pad at the beginning of each line.
   * @return a String in the form of "([symbol] [child-name] [child-symbol])\n ..." for each symbol bound to a symbol of some child symbol.
   */
  public String toStringBoundTo(int indent) {
  	StringBuilder b = new StringBuilder();
  	for (String childName: getChildren()) {
  		BoundSymbols child = getChild(childName);
  		for (String sym: child.vars.keySet()) {
  			Value val = child.vars.get(sym);
  			if (val.reference!=null) {
  				CASAUtil.pad(b, indent)
  				.append("(\"")
  				.append(child.vars.get(sym).reference)
  				.append("\" \"")
  				.append(childName)
  				.append("\" \"")
  				.append(sym)
  				.append("\")\n");
  			}
  		}
  	}
  	return b.toString();
  }
  
  /**
   * @param indent the number of character spaces to pad at the beginning of each line.
   * @return a String in the form of "([symbol] [child-name] [child-symbol])\n ..." for each symbol bound to a symbol of some child symbol.
   */
  public String toStringBoundValue(int indent, String forSym) {
  	StringBuilder b = new StringBuilder();
  	for (String childName: getChildren()) {
  		BoundSymbols child = getChild(childName);
  		for (String sym: child.vars.keySet()) {
  			if (forSym.equalsIgnoreCase(sym)) {
  				Value val = child.vars.get(sym);
  				if (val.valueMap==null) break;
  				for (Object key: val.valueMap.keySet()) {
  					CASAUtil.pad(b, indent)
  					.append("(\"")
  					.append(val.valueMap.get(key))
  					.append("\" \"")
  					.append(childName)
  					.append("\" \"")
  					.append(key)
  					.append("\")\n");
  				}
  				
  			}
  		}
  	}
  	return b.toString();
  }
  
	/**
	 * For the SYMBOL sym, bind the VALUE fromVal to be treated as being equivalent to the VALUE toVal.
	 * Note that this is recursively defined.
	 * @param sym The name of a symbol already registered through {@link #bind(String, Object)} 
	 * or {@link #bindExp(String, LispObject)} or {@link #bindTo(String, String, String)}.
	 * @param fromVal The value to bind.
	 * @param toVal The target value.
	 * @throws IllegalOperationException if the symbol is not found.
	 */
	public void bindValue(String sym, Object fromVal, Object toVal) throws IllegalOperationException {
		Value val = vars.get(sym);
		if (val==null) 
			throw new IllegalOperationException("BoundSymbols.bindValue(): no symbol '"+sym+"' found.");
		if (val.valueMap==null) 
			val.valueMap = new TreeMap<Object,Object>();
		val.valueMap.put(fromVal, toVal);
		if (val.reference == null) // make sure we reference an object in the parent
			val.reference = sym;
	}
	
//	/**
//	 * Evaluate the equivalence of the value <em>obj</em> in symbol <em>sym</em> with the object <em>other</em>.
//	 * The comparison is done in terms 
//	 * of {@link String#equalsIgnoreCase(String)} in the case of both <em>obj</em> and <em>other</em> being
//	 * Strings, and {@link Object#equals(Object)} otherwise.
//	 * @param sym The symbol in whose context we should evaluate the comparison.  
//	 * @param obj The value to compare
//	 * @param other The other value to compare
//	 * @return true iff <em>obj</em> and <em>other</em> are equivalent.
//	 * @throws IllegalOperationException
//	 */
//	public boolean equalVal(String sym, Object obj, Object other) throws IllegalOperationException {
//		Value val = getPrimitive(sym);
//		if (val==null) throw new IllegalOperationException("BoundSymbols.bindValue(): no symbol '"+sym+"' found.");
//		if (val.valueMap==null) return equalValPrimitive(obj,other);		
//		Object trans = val.valueMap.get(obj);
//		if (trans==null) return equalValPrimitive(obj,other);
//		return equalVal(sym,trans,other);
//	}

//  private boolean equalValPrimitive(Object obj, Object other) {
//  	if (obj instanceof String && other instanceof String) 
//  		return ((String)obj).equalsIgnoreCase((String)other);
//  	return obj.equals(other);
//  }
}
