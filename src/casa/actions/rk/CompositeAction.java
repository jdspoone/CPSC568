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
import casa.StatusObject;
import casa.TransientAgent;
import casa.abcl.CasaLispOperator;
import casa.abcl.LispException;
import casa.abcl.ParamsMap;
import casa.exceptions.IllegalOperationException;
import casa.ui.AgentUI;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

import org.armedbear.lisp.Cons;
import org.armedbear.lisp.ControlTransfer;
import org.armedbear.lisp.Environment;
import org.armedbear.lisp.JavaObject;
import org.armedbear.lisp.Lisp;
import org.armedbear.lisp.LispObject;

/**
 * An abstract composite action; basically a container for actions.  Subclasses
 * must implement the {@link #execute()} method.
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
public abstract class CompositeAction extends Action implements java.util.Collection<Action> {
	
	static {
		makeLispCommand(Seq.class);
		makeLispCommand(ActionChoice_One.class);
	}
	
	LinkedList<Action> actions = null;

	/**
	 * @param actions The actions of this composite action; depending on the subclass this
	 * list may be interpreted as a sequence or a set.
	 */
	public CompositeAction(Action...actions) {
		if (actions!=null && actions.length>0) {
			this.actions = new LinkedList<Action>();
			if (actions!=null) {
				for (Action action: actions) {
					this.actions.add(action);
				}
			}
		}
	}
	
	/**
	 * Execute all or some subset of the actions in the order specified by the iterator.
	 * We are assuming the subclass has overridden the iterator to do the appropriate
	 * action.
	 * This method stops when any actions fails and that status is returned; 
	 * if it succeeds (warning, +ve, are ignored) then the LAST status result 
	 * executed is returned. 
	 */
	@Override
	public Status execute(Object... paramValues) throws IllegalOperationException, ClassCastException {
		if (paramValues!=null && paramValues.length>0)
			throw new IllegalOperationException("Action seq cannot take any arguments");
		Status stat = new Status(0);
		for (Action action: this) {
			stat = action.execute();
			if (stat.getStatusValue()<0) {
				return stat;
			}
		}
		return stat;
	}
	
	/* (non-Javadoc)
	 * @see casa.actions.Action#getName()
	 */
	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	/* ******************************************************************************
	 * Collection contract
	 ********************************************************************************/
	/* (non-Javadoc)
	 * @see java.util.Collection#add(java.lang.Object)
	 */
	@Override
	public boolean add(Action e) {
		return actions.add(e);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(Collection<? extends Action> c) {
		return actions.addAll(c);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#clear()
	 */
	@Override
	public void clear() {
		actions.clear();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object o) {
		return actions.contains(o);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(Collection<?> c) {
		return actions.containsAll(c);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return actions.isEmpty();
	}

//	/* (non-Javadoc)
//	 * @see java.util.Collection#iterator()
//	 */
//	@Override
//	public Iterator<Action> iterator() {
//		return null;
//	}

	/* (non-Javadoc)
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(Object o) {
		return actions.remove(o);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(Collection<?> c) {
		return actions.removeAll(c);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		return actions.retainAll(c);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#size()
	 */
	@Override
	public int size() {
		return actions.size();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#toArray()
	 */
	@Override
	public Object[] toArray() {
		return actions.toArray();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#toArray(T[])
	 */
	@Override
	public <T> T[] toArray(T[] a) {
		return actions.toArray(a);
	}
	
	/* ******************************************************************************
	 * end Collection contract
	 ********************************************************************************/
	
	protected static void makeLispCommand
	(final Class<? extends CompositeAction> theClass) {
		new CasaLispOperator(theClass.getSimpleName(), "\"!Creates an instance of a composite Action, "+theClass.getSimpleName()+"\" "
  		+"&REST REST"
  		, TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
		{
			@Override
			public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) throws ControlTransfer {
				LispObject obj = params.getLispObject("REST");
				Vector<Action> v = new Vector<Action>();
				if (obj instanceof Cons) {
					Cons args = (Cons)obj;
					for (; args!=Lisp.NIL; args=(Cons)args.cdr) {
						if (args.car instanceof JavaObject) {
							Object o = ((JavaObject)args.car).getObject(); 
							if (o instanceof Action) {
								v.add((Action)o);
							}
							else {
								throw new LispException("("+getName()+" "+params.getJavaObject("REST")+"): Expected a cons of Action.");
							}
						}
						else {
							throw new LispException("("+getName()+" "+params.getJavaObject("REST")+"): Expected a cons of Action.");
						}
					}
				}
				else {
					throw new LispException("("+getName()+" "+params.getJavaObject("REST")+"): Expected a cons of Action.");
				}
				CompositeAction action;
				try {
					Constructor<? extends CompositeAction> constructor = theClass.getConstructor(Action[].class);
					action = (CompositeAction)constructor.newInstance(v.toArray(/*new Action[v.size()]*/));
				} catch (Throwable e) {
					throw new LispException("("+getName()+" "+params.getJavaObject("REST")+"): Failed to construct new object.", e);
				}
				return new StatusObject<Action>(0, action);
			}
		};
	}

}
