package casa;
import casa.abcl.CasaLispOperator;
import casa.abcl.LispException;
import casa.abcl.ParamsMap;
import casa.ontology.v3.CASAOntology;
import casa.ui.AgentUI;
import casa.util.Trace;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.armedbear.lisp.Cons;
import org.armedbear.lisp.ControlTransfer;
import org.armedbear.lisp.Environment;
import org.armedbear.lisp.JavaObject;
import org.armedbear.lisp.LispObject;
import org.armedbear.lisp.SimpleString;
import org.armedbear.lisp.Symbol;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright 2003-2014, University of Calgary</p>
 * @author Rob Kremer
 * @version 0.9
 */

public class Act extends Vector<String> implements Comparable<Act> {

//	static {
//		casa.abcl.Lisp.loadClass("org.armedbear.lisp.Lisp");
//	}

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
   * Constructs an Act list using "x|y|z" syntax. A missing identifier between the vertical
   * bars will be interpreted as BOTTOM (does not match anything in the ontology).
   * @param name a string containing legal Act names separated by "|"s
   */
  public Act(String name) {
    fromString(name);
  	//assert validate() : "Bad Act specificaiton: '"+name+"'.";
    fixNulls();
  }

	/**
   * Constructs an Act list using from the list of identifiers. A missing null identifier
   * (or one with the value "null" (not case sensitive))
   * will be interpreted as BOTTOM (does not match anything in the ontology).
   * @param name a string containing legal Act names separated by "|"s
   */
  public Act(List<String> p0) {
    super(p0==null?new Vector<String>():p0);
  	//assert validate() : "Bad Act specificaiton: '"+p0+"'.";
    fixNulls();
  }

  protected boolean validate() {
  	for (String element: this) {
  		if (element==null || "null".equalsIgnoreCase(element)) return false;
  	}
  	return true;
  }

  protected void fixNulls() {
  	for (int i = size()-1; i>=0; i--) {
  		if (get(i)==null || get(i).length()==0 || "null".equalsIgnoreCase(get(i))) {
  			add(i,CASAOntology.BOTTOM);
  		}
  	}
  }

  /**
   * rebuilds this Act list using "x|y|z" syntax.
   * @param name a string containing legal Act names separated by "|"s
   */
  public void fromString(String name) {
    removeAllElements();
    if (!(name==null || name.trim().length()==0)) {
      String[] names = name.split("\\|");
      for (int i = 0, end = names.length; i < end; i++) {
        add(names[i]);
      }
    }
  }
  
  
  
  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer();
    for (int i=0,end=size(); i<end; i++) {
      buf.append(elementAt(i));
      if (i<(end-1)) buf.append('|');
    }
    return buf.toString();
  }

  /**
   * Returns a String-formatted version of the Act stack. dots are skipped and 
   * the next letter is capitalised, '-'s are turned to '_'s.
   * @param numberToIgnore number of prefix elements to skip, -ve number returns the last element only 
   * @return string representing the Act formatted for legal variable names
   */
  public String toStringSimplify(int numberToIgnore) {
    StringBuffer buf = new StringBuffer();
    if (numberToIgnore<0) {
    	if (isEmpty()) buf.append("null");
    	else prettifyAndAppend(buf,lastElement());
    }
    else {
		  for (int i=numberToIgnore,end=size(); i<end; i++) {
		    prettifyAndAppend(buf, elementAt(i));
		    if (i<(end-1)) buf.append('_');
      }
    }
    return buf.toString();
  }

  private void prettifyAndAppend (StringBuffer buf, String string) {
  	boolean capitolizeNext = false;
  	for (char c : string.toCharArray()) {
  		if (c == '.') {
  			capitolizeNext = true;
  		} else {
  			if (capitolizeNext) {
  				capitolizeNext = false;
  				buf.append (Character.toUpperCase(c));
  			} else {
  				buf.append ((c=='-')?'_':c);
  			}
  		}
  	}
	}

	/**
	 * Pushes the parameter String onto the stack
	 * @param token
	 * @return this
	 */
	public Act push(String token) {
    insertElementAt(token,0);
    return this;
  }

  /**
   * Pops the top element off the stack
   * @return this
   */
  public Act pop() {
    if (size()>0) {
    	try {
        removeElementAt(0);
    	}
    	catch (ArrayIndexOutOfBoundsException ex) {
    	}
    }
    return this;
  }

  /**
   * Returns the top element of the stack
   * @return the top element
   */
  public String peek() {
    return size()>0?elementAt(0):null;
  }
  
  /**
   * Creates a new Act out of <em>act</em> and pushes <em>tokens</em> onto it.
   * @param act
   * @param tokens
   * @return a new Act with the tokens pushed
   */
  static public Act DuplicateAndPush(Act act, String... tokens) {
  	Act ret = new Act(act);
  	if (tokens!=null) {
  		for (String t: tokens) ret.push(t);
  	}
  	return ret;
  }

  /**
   * Creates a new Act out of <em>act</em> and pops <em>n</em> takens off the top.
   * <em>n</em> may be less than or equal to 0, which does nothing. 
   * If <em>n</em> is greater than the height of the stack, the stack is emptied. 
   * @param act
   * @param n the number of tokens to pop.
   * @return A new Act, popped.
   */
  static public Act DuplicateAndPop(Act act, int n) {
  	Act ret = new Act(act);
  	for (int i=n; i>0; i--) ret.pop();
  	return ret;
  }

  /**
   * Creates a new Act out of <em>act</em> and pops the top element off the top.
   * @param act
   * @return A new Act, popped.
   * @see #DuplicateAndPop(Act, int)
   */
  static public Act DuplicateAndPop(Act act) {
  	return DuplicateAndPop(act,1);
  }

  public static void main(String[] args) {
    String test = "1|2|3";
    Act x =  new Act(test);
    if (x.size()!=3) Trace.log("error", "Failed " + test);
    else if (!x.toString().equals(test))Trace.log("error", "Failed toString() for "+test);
    test = "x";
    x =  new Act(test);
    if (x.size()!=1) Trace.log("error", "Failed " + test);
    else if (!x.toString().equals(test)) Trace.log("error", "Failed toString() for "+test);
  }
  
  /**
   * Lisp operator: (ACT string ...)<br>
   * Create a MessageEventDescriptor object.
   */
  @SuppressWarnings("unused")
	private static final CasaLispOperator ACT =
    new CasaLispOperator("ACT", "\"!Return a new Act object.\" "+
    						"&REST ACTIONS \"!one or more action types as strings.\" ",
    						     TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
    {
      @Override
			public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) throws ControlTransfer {
				LispObject obj = params.getLispObject("ACTIONS");
				if (obj == null || obj==org.armedbear.lisp.Lisp.NIL) {
					return new Status(-7,printui("(ACT ...): found no types to build an Act from.",ui));
				} 
				else if (obj instanceof org.armedbear.lisp.Cons) {
					StringBuilder buf = new StringBuilder();
					for (; obj!=null && obj!=org.armedbear.lisp.Lisp.NIL; obj = obj.cdr()) {
						int marker = 0;
						LispObject car = obj.car();
						try {
							while (car instanceof org.armedbear.lisp.Cons) { // a list can be either a function (which could return a list) or a cons representation of an act.
								try {
									marker = 5;
									car = org.armedbear.lisp.Lisp.eval(car);
								} catch (ControlTransfer e) {
									marker = 10;
									ParamsMap pm = new ParamsMap();
									pm.put("ACTIONS", car, car, false);
									Status stat = execute(agent,pm,ui, env);
									marker = 15;
									if (stat instanceof StatusObject<?>) {
										marker = 20;
										Object act = ((StatusObject<?>)stat).getObject();
										if (act instanceof Act) {
											marker = 25;
											car = new JavaObject(act);
										}
										else throw new LispException(toString(params)+": Can't interpret cons list a either an Act or a function call, got "+act);
									}
									else throw new LispException(toString(params)+": Can't interpret cons list a either an Act or a function call, got "+stat);
								}
							}
							if (car instanceof JavaObject) {
								marker = 30;
								Object javaObj = ((JavaObject)car).getObject();
								if (javaObj==null || (javaObj instanceof String && "-".equals(javaObj))) // skip a missing item or item "-"
									continue;
							  if (javaObj instanceof Act) {
									marker = 35;
							  	car = new org.armedbear.lisp.SimpleString(javaObj.toString());
								}
							}
							if (car instanceof SimpleString) {
								marker = 37;
								String s = ((SimpleString)car).getStringValue();
								if ("-".equals(s)) { // skip a missing item or item "-"
									continue; //agent.println("error","Operator Act: Unexpected NIL in expression "+this);
								}
							}
							if (car instanceof Symbol) {
								marker = 40;
								LispObject car2 = ((Symbol)car).getSymbolValue();
								String s = (car2!=null) ? car2.getStringValue() : null;
								if (car2==org.armedbear.lisp.Lisp.NIL || "-".equals(s)) { // skip a missing item or item "-"
									continue; //agent.println("error","Operator Act: Unexpected NIL in expression "+this);
								}
								car = car2;
								marker = 42;
							}
							marker = 43;
							String action = (car==null?null:car.getStringValue());//.toUpperCase();
							marker = 45;
							if (action!=null && action.length()>1) {
								if (action.charAt(0)=='"' && action.charAt(action.length()-1)=='"') {
									marker = 50;
									action = action.substring(1,action.length()-1);
								}
							}
							if (action==null || action.length()==0 || "null".equalsIgnoreCase(action.toString())) {
								assert true :"Trying to instantiate an ACT with a null member";
							}
							if (buf.length()>0) buf.append('|');
							buf.append(action);
							//if (obj!=null && obj!=org.armedbear.lisp.Lisp.NIL) buf.append('|');
						} catch (ControlTransfer e) {
							agent.println("error",toString(params)+": failed execution (car type="+car.classOf()+") after marker "+marker,new Exception(e));
							assert false;
							throw(e);
						}
					}
					Act act = new Act(buf.toString());
					return new StatusObject<Act>(0,act);
				} 
      
				return null;
      }
      
      private String printui(String s, AgentUI ui) {
      	ui.println("; warning/error: "+s);
      	return s;
      }

    };
    
    /**
     * Lisp operator: (ACT2LIST ACT)<br>
     * Attempt to join the cooperation domain specified by the parameter URL.
     */
    @SuppressWarnings("unused")
		private static final CasaLispOperator ACT2LIST =
      new CasaLispOperator("ACT2LIST", "\"!Convert an Act JavaObject to a Cons list.\" "+
      						"ACT \"@casa.Act\" \"!The Act object to return as a Cons list.\" ", TransientAgent.class, new Object() { }.getClass().getEnclosingClass(), "A2L")
      {
        @Override
  			public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
        	Act act;
					try {
						act = (Act)params.getJavaObject("ACT",Act.class);
					} catch (Exception e) {
						return new Status(-1,"(ACT2LIST <act>): Invalid argument",e);
					}
        	Cons cons = null;
      		Cons last = null;
        	for (String t: act) {
        		if (t==null || t.length()==0)
        			continue;
        		SimpleString item = new SimpleString(t);
        		if (cons==null) {
        		  cons = last = new Cons(item);
        		} else {
        			last.cdr = new Cons(item);
        			last = (Cons)last.cdr;
        		}
        	}
        	return new StatusObject<Cons>(0,cons);
      }
    };

    /**
     * Lisp operator: (ACT.ACTION_AT ACT INDEX)<br>
     * Attempt to join the cooperation domain specified by the parameter URL.
     */
    @SuppressWarnings("unused")
		private static final CasaLispOperator ACT__ACTION_AT =
      new CasaLispOperator("ACT.ACTION-AT", "\"!Return the ACTION at index INDEX, where the first is index 0.\" "
      						+ "ACT \"!The Act object or String to index into.\" "
      						+ "INDEX \"@java.lang.Integer\" \"!The index, beginning with 0\" ", TransientAgent.class, new Object() { }.getClass().getEnclosingClass(), "A2L")
      {
        @Override
  			public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
        	Act act;
					try {
						act = (Act)params.getJavaObject("ACT",Act.class);
					} catch (Throwable e) {
						try {
							String s = (String)params.getJavaObject("ACT",String.class);
							act = new Act(s);
						}
						catch (Throwable e1) {
						  return new Status(-1,"(ACT.ACTION-AT <act> <index>): Invalid <act> argument, must be an Act object or a String representation of an Act object.",e);
						}
					}
					int index = (Integer)params.getJavaObject("INDEX");
					String ret;
					try {
						ret = act.get(index);
					} catch (Throwable e) {
					  return new Status(-2,"(ACT.ACTION-AT <act> <index>): <index> out of range.",e);
					}
        	return new StatusObject<String>(0,"success",ret);
      }
    };

    /**
     * Lisp operator: (ACT.SIZE ACT INDEX)<br>
     * Attempt to join the cooperation domain specified by the parameter URL.
     */
    @SuppressWarnings("unused")
		private static final CasaLispOperator ACT__SIZE =
      new CasaLispOperator("ACT.SIZE", "\"!Return the number of ACTIONS in the ACT.\" "
      						+ "ACT \"!The Act object or String reprsentation of the Act object.\" ", TransientAgent.class, new Object() { }.getClass().getEnclosingClass(), "A2L")
      {
        @Override
  			public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
        	Act act;
					try {
						act = (Act)params.getJavaObject("ACT",Act.class);
					} catch (Throwable e) {
						try {
							String s = (String)params.getJavaObject("ACT",String.class);
							act = new Act(s);
						}
						catch (Throwable e1) {
						  return new Status(-1,"(ACT.ACTION-AT <act> <index>): Invalid <act> argument, must be an Act object or a String representation of an Act object.",e);
						}
					}
					int ret = act.size();
	      	return new StatusObject<Integer>(0,ret);
      }
    };

    /**
     * Lisp operator: (ACT2STRING ACT)<br>
     * Attempt to join the cooperation domain specified by the parameter URL.
     */
    public static final CasaLispOperator ACT2STRING =
      new CasaLispOperator("ACT2STRING", "\"!Convert an Act JavaObject to its string representation.\" "+
      						"ACT \"@casa.Act\" \"!The Act object to return as a Cons list.\" ", TransientAgent.class, new Object() { }.getClass().getEnclosingClass(), "A2L")
      {
        @Override
  			public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
        	Act act;
					try {
						act = (Act)params.getJavaObject("ACT",Act.class);
					} catch (Exception e) {
						return new Status(-1,"(ACT2LIST <act>): Invalid argument",e);
					}
					String s = act.toString();
					SimpleString ret = new SimpleString(s);
        	return new StatusObject<SimpleString>(0,ret);
      }
    };

		@Override
		public int compareTo(Act o) {
			Iterator<String> oit = o.iterator();
			for (String s: this) {
				int c;
				if (oit.hasNext()) {
					if ((c = s.compareTo(oit.next())) != 0) 
					  return c; // matching indices differ
				}
				else
					return 1; // other is sorter
			}
			if (oit.hasNext())
				return -1; // other is longer
			else
				return 0; // same length and all compare
		}

//    /**
//     * Lisp operator: (contains act1 act2)<br>
//     * You can't perform a subtypep on lists returned by act2list for some reason.
//     * This function determines if the performatives in act2 can also be found in
//     * act1.
//     * 
//     * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
//     */
//    private static final CasaLispOperator CONTAINS =
//      new CasaLispOperator("CONTAINS", "\"!Compare to two Act object .\" " +
//      						"ACT1 \"@casa.Act\" \"!The act to which act2 will be compared.\" " + 
//      						"ACT2 \"@casa.Act\" \"!The performatives in this act are compared to those in act1.\" ",
//      						TransientAgent.class)
//      {
//        @Override
//  			public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
//        	Act act1, act2;
//        	
//        	try {
//        		act1 = (Act)params.getJavaObject("ACT1", Act.class);
//        		act2 = (Act)params.getJavaObject("ACT2", Act.class);
//					} catch (Exception e) {
//						return new Status(-1,"(CONTAINS <act1> <act2>): Invalid argument",e);
//					}
//
//					boolean matched;
//					for (String a2: act2){
//						matched = false;
//						for (String a1: act1)
//							if (a2.equals(a1))
//								matched = true;
//						if (!matched)
//							return new StatusObject<LispObject>(0, org.armedbear.lisp.Lisp.NIL);
//					}
//        	return new StatusObject<LispObject>(0, org.armedbear.lisp.Lisp.T);
//      }
//    };
    

}