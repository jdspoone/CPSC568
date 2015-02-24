package casa.util;

/**
 * <code>Tristate</code> is a class that is essentially an extension of the
 * <code>Boolean</code> class that incorporates a third state. This third
 * state is called undefined and can be used in any case where the truthfulness
 * of something is undefined. This class is immutable and does not have any
 * public constructors. To get a <code>Tristate</code>, use one of the
 * constants, or the <code>forState()</code> function.
 * 
 * <p>
 * Copyright: Copyright 2003-2014, Knowledge Science Group, University of
 * Calgary. Permission to use, copy, modify, distribute and sell this software
 * and its documentation for any purpose is hereby granted without fee, provided
 * that the above copyright notice appear in all copies and that both that
 * copyright notice and this permission notice appear in supporting
 * documentation. The Knowledge Science Group makes no representations about the
 * suitability of this software for any purpose. It is provided "as is" without
 * express or implied warranty.
 * </p>
 * 
 * @author Jason Heard
 * @author <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public class Tristate {
	/**
	 * The <code>int</code> that represents the state of this object. It must
	 * always be one of <code>FALSE_INT</code>, <code>TRUE_INT</code>, or
	 * <code>UNDEFINED_INT</code>.
	 */
	private int state;

	/**
	 * The <code>int</code> representing the false state.
	 */
	private static final int FALSE_INT = 0;

	/**
	 * A static <code>Tristate</code> that is in the false state.
	 */
	public static final Tristate FALSE = new Tristate (FALSE_INT);

	/**
	 * The <code>int</code> representing the truthful state.
	 */
	private static final int TRUE_INT = 1;

	/**
	 * A static <code>Tristate</code> that is in the truthful state.
	 */
	public static final Tristate TRUE = new Tristate (TRUE_INT);

	/**
	 * The <code>int</code> representing the undefined state.
	 */
	private static final int UNDEFINED_INT = 2;

	/**
	 * A static <code>Tristate</code> that is in the undefined state.
	 */
	public static final Tristate UNDEFINED = new Tristate (UNDEFINED_INT);

	/**
	 * Creates a new <code>Tristate</code> object with the given
	 * <code>int</code> state. If state does not equal <code>FALSE_INT</code>
	 * or <code>TRUE_INT</code>, the state is set to
	 * <code>UNDEFINED_INT</code>.
	 * 
	 * @param state The initial state of the new <code>Tristate</code> object
	 *            as an <code>int</code>.
	 */
	private Tristate (int state) {
		if (state == FALSE_INT) {
			this.state = FALSE_INT;
		} else if (state == TRUE_INT) {
			this.state = TRUE_INT;
		} else {
			this.state = UNDEFINED_INT;
		}
	}
	
	/**
	 * Creates a Tristate based on the String parameter
	 * @param val "true" for a true state, "false" for a false state, otherwise an undefined state.
	 */
	public Tristate(String val) {
		if ("true".equals(val.toLowerCase())) state = TRUE_INT;
		else if ("false".equals(val.toLowerCase())) state = FALSE_INT;
		else state = UNDEFINED_INT;
	}

	/**
	 * Returns the <code>Tristate</code> object with the given boolean state.
	 * This method will never return a <code>Tristate</code> object that is
	 * undefined.
	 * 
	 * @param state The state of the <code>Tristate</code> object that should
	 *            be returned as a <code>boolean</code>.
	 * @return A <code>Tristate</code> object with the given truth value.
	 */
	public static Tristate forState (boolean state) {
		if (state) {
			return TRUE;
		} else {
			return FALSE;
		}
	}

	/**
	 * Returns the <code>Tristate</code> object with the negation of this
	 * object's boolean state. This method will return a <code>Tristate</code>
	 * object that is undefined if this object is undefined.
	 * 
	 * @return A <code>Tristate</code> object with the negation of this
	 *         object's truth value.
	 */
	public Tristate negation () {
		if (state == FALSE_INT) {
			return TRUE;
		} else if (state == TRUE_INT) {
			return FALSE;
		} else {
			return UNDEFINED;
		}
	}

	/**
	 * Sets the state of the object to the given state. If state does not equal
	 * <code>FALSE</code> or <code>TRUE</code>, the state is set to
	 * <code>UNDEFINED</code>.
	 * 
	 * @param state The state that the object should be set to.
	 */
	private void setState (int state) {
		if (state == FALSE_INT) {
			this.state = FALSE_INT;
		} else if (state == TRUE_INT) {
			this.state = TRUE_INT;
		} else {
			this.state = UNDEFINED_INT;
		}
	}

	/**
	 * Retrieves the current state of the object as an <code>int</code>. This
	 * will return <code>FALSE_INT</code> if the state is false,
	 * <code>TRUE_INT</code> if the state is true, or
	 * <code>UNDEFINED_INT</code> if the state is undefined.
	 * 
	 * @return <code>FALSE_INT</code> if the state is false,
	 *         <code>TRUE_INT</code> if the state is true, or
	 *         <code>UNDEFINED_INT</code> if the state is undefined.
	 */
	private int getState () {
		return state;
	}

	/**
	 * Determines whether the object has a defined state. Returns
	 * <code>true</code> if the state is not undefined; <code>false</code>
	 * otherwise.
	 * 
	 * @return <code>true</code> if the state is not undefined;
	 *         <code>false</code> otherwise.
	 */
	public boolean isDefined () {
		return state != UNDEFINED_INT;
	}

	/**
	 * Determines the objects boolean value. Returns <code>true</code> if the
	 * state is true; <code>false</code> if the state is false. It will throw
	 * an exception if the state is undefined.
	 * 
	 * @return <code>true</code> if the state is true; <code>false</code> if
	 *         it is false.
	 * @throws RuntimeException if the state is undefined.
	 */
	public boolean booleanValue () throws RuntimeException {
		if (state == FALSE_INT) {
			return false;
		} else if (state == TRUE_INT) {
			return true;
		} else {
			throw new RuntimeException ("Boolean value is currently undefined.");
		}
	}

	/**
	 * Determines the objects boolean value, returning a default value if it is
	 * undefined. Returns <code>true</code> if the state is true,
	 * <code>false</code> if the state is false, or the given default value if
	 * the state is undefined.
	 * 
	 * @param defaultValue The <code>boolean</code> value that should be
	 *            returned if the object's state is undefined.
	 * @return <code>true</code> if the state is true, <code>false</code> if
	 *         it is false, or the defaultValue if it is undefined.
	 */
	public boolean booleanValue (boolean defaultValue) {
		if (state == FALSE_INT) {
			return false;
		} else if (state == TRUE_INT) {
			return true;
		} else {
			return defaultValue;
		}
	}

	/**
	 * Determines if the current <code>Tristate</code> object is equal to the
	 * given object. This returns <code>true</code> if the object is a
	 * <code>Tristate</code> or a <code>Boolean</code> object in the same
	 * state; <code>false</code> otherwise.
	 * 
	 * @param object The object to compare to the current <code>Tristate</code>
	 *            object.
	 * @return <code>true</code> if the object is a <code>Tristate</code> or
	 *         a <code>Boolean</code> object in the same state;
	 *         <code>false</code> otherwise.
	 */
	public boolean equals (Object object) {
		if (this == object) {
			return true;
		} else if (object != null) {
			if (object instanceof Tristate) {
				Tristate tempState = (Tristate) object;
				return tempState.state == this.state;
			} else if (object instanceof Boolean) {
				Boolean tempBool = (Boolean) object;
				if (tempBool.booleanValue ()) {
					return TRUE_INT == this.state;
				} else {
					return FALSE_INT == this.state;
				}
			}
		}

		return false;
	}

	/**
	 * Retrieves the current state of the object as an <code>String</code>.
	 * This will return <code>"false"</code> if the state is false,
	 * <code>"true"</code> if the state is true, or <code>"undefined"</code>
	 * if the state is undefined.
	 * 
	 * @return <code>"false"</code> if the state is false, <code>"true"</code>
	 *         if the state is true, or <code>"undefined"</code> if the state
	 *         is undefined.
	 */
	public String toString () {
		if (state == FALSE_INT) {
			return "false";
		} else if (state == TRUE_INT) {
			return "true";
		} else {
			return "undefined";
		}
	}

	/**
	 * Returns the hash code for this <code>Tristate</code> object. Returns
	 * the hash code for <code>Boolean.FALSE</code> if this object's state is
	 * false, the hash code for <code>Boolean.TRUE</code> if this object's
	 * state is true, or the <code>int</code> 1225 if this object's state is
	 * undefined.
	 * 
	 * @return The hash code for <code>Boolean.FALSE</code> if this object's
	 *         state is false, the hash code for <code>Boolean.TRUE</code> if
	 *         this object's state is true, or the <code>int</code> 1225 if
	 *         this object's state is undefined.
	 */
	public int hashCode () {
		if (state == FALSE_INT) {
			return Boolean.FALSE.hashCode ();
		} else if (state == TRUE_INT) {
			return Boolean.TRUE.hashCode ();
		} else {
			return 1225;
		}
	}
}