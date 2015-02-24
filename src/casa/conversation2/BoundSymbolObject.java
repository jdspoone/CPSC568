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
package casa.conversation2;

import casa.TransientAgent;
import casa.abcl.BoundSymbols;
import casa.exceptions.IllegalOperationException;
import casa.util.CASAUtil;
import casa.util.InstanceCounter;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.armedbear.lisp.Environment;
import org.armedbear.lisp.LispObject;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class BoundSymbolObject implements Cloneable {
	/**
	 * The name of the conversation
	 */
	protected String name;
	
	/**
	 * The parent conversation
	 */
	private BoundSymbolObject parent = null;
	/**
	 * Set the parent
	 * @param parent
	 */
	protected void setParent(BoundSymbolObject parent) {this.parent = parent;}
	protected BoundSymbolObject getParent() {return parent;}

	/**
	 * The child conversations
	 */
	protected TreeMap<String, BoundSymbolObject> children = new TreeMap<String, BoundSymbolObject>();
	/**
	 * @return The list of children of this BoundSymbolObject
	 */
	public Collection<BoundSymbolObject> getChildren() {
		return children.values();
	}
	/**
	 * Add a child to the child conversation list
	 * @param child
	 */
	protected void addChild(BoundSymbolObject child) {
		try {
			children.put(child.name, child);
			child.parent = this;
		} catch (Throwable e) {
			CASAUtil.log("error", "BoundSymbolObject.addChild("+child+") called after destroyed.", e, true);
		}
	}
	
	public void setName(String name) {
		assert false: "BoundSymbolObject: setting nam enot allowed";
	}

	/** 
	 * @param childName
	 * @return The child with the name <em>childName</em> or null if there is no such child
	 */
	protected BoundSymbolObject getChild(String childName) {
		try {
			return children.get(childName);
		} catch (Throwable e) {
			CASAUtil.log("error", "BoundSymbolObject.getChild("+childName+") called after destroyed.", e, true);
			return null;
		}
	}
	
	protected void clearChildren() {children = new TreeMap<String, BoundSymbolObject>();}
	
  /**
   * The symbols in this conversation, which may be local bound or may be bound to one of 
   * the parent's symbols (not necessarily of the same name).
   */
  protected Symbols symbols = new Symbols(this);
  
  class Symbols extends BoundSymbols {
  	BoundSymbolObject owner;
  	
  	Symbols(BoundSymbolObject owner) {this.owner = owner;};
  	Symbols(BoundSymbolObject owner, Symbols copy) {
  		super(copy); 
  		this.owner = owner;
  		
  	}
		@Override
		protected BoundSymbols getChild(String name) {
			BoundSymbolObject childConv = owner.children.get(name); 
			return childConv==null?null:childConv.symbols;
		}
		@Override
		protected BoundSymbols getParent() {
			return owner.parent==null?null:owner.parent.symbols;
		}
		@Override
		protected Collection<String> getChildren() {
			return owner.children.keySet();
		}
  };
  
  /**
   * Create a local binding for this symbol <em>sym</em>.
   * @param sym
   * @param val
   */
  public void bindVar(String sym, Object val) {
  	try {
  		symbols.bind(sym, val);
  	} catch (Throwable e) {
  		CASAUtil.log("error", "BoundSymbolObject.bindVar("+sym+", "+val+") called after destroyed.", e, true);
  	}
  }
  
  /**
   * Create a local binding for this symbols <em>sym</em>.
   * @param sym
   * @param val
   */
  public void bindVarExpression(String sym, LispObject exp) {
  	try {
  		symbols.bindExp(sym, exp);
  	} catch (Throwable e) {
  		CASAUtil.log("error", "BoundSymbolObject.bindVarExpression("+sym+", "+exp+") called after destroyed.", e, true);
  	}
  }

  /**
   * Resolve the value of symbols <em>sym</em>.  It may be locally bound or 
   * may be bound to a symbol in the parent's symbols (not necessarily of the
   * same name).
   * @param sym
   * @throws IllegalOperationException if the sym is not evaluated
   */
  public Object getVar(String sym) throws IllegalOperationException {
  	try {
  		return symbols.get(sym);
  	} catch (IllegalOperationException e) {
  		throw e;
  	} catch (Throwable e) {
  		CASAUtil.log("error", "BoundSymbolObject.getVar("+sym+") called after destroyed.", e, true);
  		throw new IllegalOperationException(e);
  	}
  }

  /**
   * Bind the value of <em>childSym</em> in child conversation <em>childName</em> to the local
   * symbol <em>sym</em>. 
   * @param sym
   * @param childName
   * @param childSym
   * @throws IllegalOperationException if <em>childName</em> isn't the name of a child of this conversation
   */
  public void bindVarTo(String sym, String childName, String childSym) throws IllegalOperationException {
  	try {
  		symbols.bindTo(sym, childName, childSym);
  	} catch (IllegalOperationException e) {
  		throw e;
  	} catch (Throwable e) {
  		CASAUtil.log("error", "BoundSymbolObject.bindVarTo("+sym+", "+childName+", "+childSym+") called after destroyed.", e, true);
  		throw new IllegalOperationException(e);
  	}
  }
  
  /**
   * Bind the value of <em>sym</em> to the parent's local
   * symbol <em>sym</em>. 
   * @param sym
   * @param parentSym
   * @throws IllegalOperationException if <em>childName</em> isn't the name of a child of this conversation
   */
  public void bindVarTo(String sym, String parentSym) throws IllegalOperationException {
  	try {
  		symbols.bindTo(sym, parentSym);
  	} catch (Throwable e) {
  		CASAUtil.log("error", "BoundSymbolObject.bindVarTo("+sym+", "+parentSym+") called after destroyed.", e, true);
  		throw new IllegalOperationException(e);
  	}
  }
  
  /**
   * @param env
   * @return a new environment based on <em>env</em> extended with local symbols.
   * @throws IllegalOperationException if any of the symbols are not evaluated.
   */
  protected Environment extendEnv(Environment env) throws IllegalOperationException {
  	try {
  		return symbols.extendEnv(env);
  	} catch (IllegalOperationException e) {
  		throw e;
  	} catch (Throwable e) {
  		CASAUtil.log("error", "BoundSymbolObject.extendEnv("+env+") called after destroyed.", e, true);
  		throw new IllegalOperationException(e);
  	}
  }
  
  /**
   * @return a symbol map compatible with {@link casa.abcl.Lisp#abclEval(TransientAgent, Environment, Map, String, casa.ui.AgentUI)}'s Map parameter that represents
   * the bindings for the BoundSymbols object.
   * @throws IllegalOperationException if any of the symbols are not evaluated.
   */
  protected Map<String, LispObject> getMap() throws IllegalOperationException {
  	try {
  		return symbols.getMap();
  	} catch (IllegalOperationException e) {
  		throw e;
  	} catch (Throwable e) {
  		CASAUtil.log("error", "BoundSymbolObject.getMap() called after destroyed.", e, true);
  		throw new IllegalOperationException(e);
  	}
  }
  
  /**
   * @param name
   */
  public BoundSymbolObject(String name) {
  	this.name = name;
  	InstanceCounter.add(this);
  }
  
  /**
   * @param name
   * @param parent
   */
  public BoundSymbolObject(String name, BoundSymbolObject parent) {
  	this(name);
  	setParent(parent);
  }
  
  /**
   * @return the name of this conversation.
   */
  public String getName() {
  	return name;
  }

//  @Override
//	protected BoundSymbolObject clone() throws CloneNotSupportedException {
//		if (parent!=null) throw new CloneNotSupportedException();
//		return clone(parent);
//	}
  
	@Override
	public BoundSymbolObject clone() throws CloneNotSupportedException {
		if (parent!=null)
			throw new CloneNotSupportedException("Cannot clone a non-top level BoundSymbolObject");
		BoundSymbolObject ret = (BoundSymbolObject) super.clone();
		copyTo(ret);
		InstanceCounter.add(ret);
		return ret;
	}
	
	protected BoundSymbolObject copyTo(BoundSymbolObject x) throws CloneNotSupportedException {
		x.parent = null;
		x.symbols = new Symbols(x, symbols);
		x.children = new TreeMap<String, BoundSymbolObject>();
		for (String key: children.keySet()) {
			BoundSymbolObject val = children.get(key).clone(x);
			//val.parent = ret;
			x.children.put(key, val);
		}
		return x;
	}
	
	private BoundSymbolObject clone(BoundSymbolObject parent) throws CloneNotSupportedException {
		assert parent!=null;
		BoundSymbolObject ret = (BoundSymbolObject) super.clone();
		ret.parent = parent;
		ret.symbols = new Symbols(ret, symbols);
		ret.children = new TreeMap<String, BoundSymbolObject>();
		for (String key: children.keySet()) {
			BoundSymbolObject val = children.get(key).clone(ret);
			//val.parent = ret;
			InstanceCounter.add(val);
			ret.children.put(key, val);
		}
		InstanceCounter.add(ret);
		return ret;
	}
	
	/**
	 * For the SYMBOL sym, bind the VALUE fromVal to be treated as being equivalent to the VALUE toVal.
	 * Note that this is recursively defined.
	 * @param var The name of a symbol already registered through {@link #bind(String, Object)} 
	 * or {@link #bindExp(String, LispObject)} or {@link #bindTo(String, String, String)}.
	 * @param fromVal The value to bind.
	 * @param toVal The target value.
	 * @throws IllegalOperationException if the symbol is not found.
	 */
	public void bindValue(String var, String fromVal, String toVal) throws IllegalOperationException {
		try {
		symbols.bindValue(var, fromVal, toVal);
	} catch (IllegalOperationException e) {
		throw e;
	} catch (Throwable e) {
		CASAUtil.log("error", "BoundSymbolObject.bindValue("+var+", "+fromVal+", "+toVal+") called after destroyed.", e, true);
		throw new IllegalOperationException(e);
	}
	}
	
//	public boolean equalVal(String sym, Object obj, Object other) throws IllegalOperationException {
//		return symbols.equalVal(sym, obj, other);
//	}
  
	/**
	 * Desperate attempt to get the gc to clear objects when we think this one is dead.
	 */
	public void destroy() {
		symbols = null;
		if (children!=null) {
			for (BoundSymbolObject b: children.values()) {
				b.destroy();
			}
			children = null;
		}
		parent = null;
	}
}
