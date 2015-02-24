package casa.ui;

import java.io.OutputStream;
import java.util.Observer;

/**
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
 * @author <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public interface AgentUI extends Observer {
	public static final int	TYPE_STRING		= 0;
	public static final int	TYPE_ANY		= 0;
	public static final int	TYPE_INT		= 1;
	public static final int	TYPE_FLOAT		= 2;
	public static final int	TYPE_BOOLEAN	= 3;

	public void print(String txt);

	public void println(String txt);

	/**
	 * The general contract is that the UI will return a string from the user 
	 * after prompting the use, that conforms to the the type specified by the
	 * type parameter.  If the use just hits [return] the default parameter
	 * value is returned.  The input prompt is repeated until an acceptable
	 * input is typed.
	 * @param prompt The prompt the use will see
	 * @param help An extended prompt if the user enters something inappropriate 
	 * @param type The type which may be 
	 * <ul>
	 * <li><b>AgentUI.TYPE_STRING</b>: any String
	 * <li><b>AgentUI.TYPE_ANY</b>: any String
	 * <li><b>AgentUI.TYPE_INT</b>: an integer that can be parsed by {@link java.lang.Integer#parseInt(String)}
	 * <li><b>AgentUI.TYPE_FLOAT</b>: any float number that can be parsed by {@link java.lang.Float#parseFloat(String)}
	 * <li><b>AgentUI.TYPE_BOOLEAN</b>: any of "true", "yes", "on" or "1" for a return of "true"; 
	 * or any of "false", "no", "off", or "0" for a return of "false" (case insensitive)
   * <ul>
	 * @param _default The value to return (unchecked) if the user just hits [return]
	 * @return A string conforming to the type parameter as listed above.
	 */
	public String ask(String prompt, String help, int type, String _default);

	/**
	 * Lets the UI know that it should "start" displaying itself.
	 */
	public void start();

	/**
	 * Returns an output stream that can be used to write to the interface
	 */
	public OutputStream getOutStream();

	public boolean takesHTML();
}