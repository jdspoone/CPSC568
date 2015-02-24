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

import casa.exceptions.IllegalOperationException;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public abstract class SimpleAction extends Action {

	private String name;
	
	protected Param[] params = null;
	
	private Object[] paramValues = null;
	protected Object getParamValue(int index) {
		if (paramValues==null || paramValues.length<index) {
			if (params==null || params.length<index) {
				throw new IndexOutOfBoundsException();
			}
			return params[index];
		}
		return paramValues[index];
	}
	
	/**
	 * @param name
	 * @param params
	 */
	public SimpleAction(String name, Param... params) {
		super();
		this.name = name;
		if (params!=null)
			this.params = params.clone();
	}
	
	/**
	 * Utility method to be used by subclasses to return a list of parameters that are guaranteed to 
	 * conform to the cardinality and type constraints of the parameter list.  If the constraints
	 * are violated by the type list, this method will throw an exception.  <p>
	 * If you are considering using this method, you might consider using {@link #bindParamValues(Object...)}
	 * instead, as it has the same functionality, but also fills in the default values for you
	 * automatically.
	 * @param values The values corresponding to the type definition with the same ordinality.
	 * @return The values from the parameter list if this method didn't through
	 * @throws IllegalOperationException if the list of values is longer than expected (short is OK)
	 * @throws ClassCastException if one or more of the parameters do not conform to the type in specification.
	 * @see #bindParamValues(Object...)
	 */
	protected Object[] setParamValues(Object...values) throws IllegalOperationException, ClassCastException {
		if (values==null) {
			return null;
		}
		if (values.length>params.length) {
			throw new IllegalOperationException("Action "+getName()+" requires at most "+params.length+" parameters, but called with "+values.length+" parameter values");
		}
		int i = 0;
		Object[] paramValues = new Object[values.length];
		try {
			for (Object val: values) {
				paramValues[i] = params[i].type.cast(val); // will throw a ClassCastException if type clash
				i++;
			}
		} catch (ClassCastException e) {
			throw new ClassCastException("Cannot cast parameter "+i+" from type "+values[i].getClass()+" to type "+params[i].type+".");
		}
		this.paramValues = paramValues;
		return paramValues;
	}
	
	/**
	 * Computes the values of all parameters to this action taking into account the default values if
	 * the specified values are missing (null) or the list of specified values is shorter than the list
	 * of defined parameters. This method uses {@link #setParamValues(Object...)} to check the type
	 * constraints of the parameters.
	 * @param values
	 * @return
	 * @throws IllegalOperationException
	 * @throws ClassCastException
	 * @see #setParamValues(Object...)
	 */
	protected Object[] bindParamValues(Object... values) throws IllegalOperationException, ClassCastException {
		Object[] specifiedValues = setParamValues(values);
		Object[] paramValues = new Object[params.length];
		if (specifiedValues==null) { // params are all defaulted
			for (int i=params.length-1; i>=0; i--) {
				paramValues[i] = params[i].defValue;
			}
		}
		else {
			for (int i=params.length-1; i>=0; i--) {
				paramValues[i] = specifiedValues.length>i ? specifiedValues[i] : params[i].defValue;
			}
		}
		this.paramValues = paramValues;
		return paramValues;
	}

	@Override
	public String getName() {
		return name;
	}

}
