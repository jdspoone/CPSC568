package casa;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method in an Agent for <em>Lisp Accessible</em>. A method is
 * <em>lisp accessible</em> if there is a corresponding <em>Lisp operator</em>.
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
 * @author <a href="mailto:rkyee@ucalgary.ca">Ryan Yee</a>
 * @see TransientAgent#createCasaLispOperators
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LispAccessible {
	/**
	 * @return the Lisp name to bind this method to
	 */
	String name() default "";
	String help() default "Function's help is undefined.";
	Argument[] arguments() default {};
		
	/**
	 * An {@link Argument} represents arguments to a Lisp function 
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
	 * @author <a href="mailto:rkyee@ucalgary.ca">Ryan Yee</a>
	 *
	 */
	public @interface Argument{
		/**
		 * @return the argument name
		 */
		String name();
		String help() default "Help undefined";
	}
}
