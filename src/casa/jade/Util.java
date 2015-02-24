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
package casa.jade;

import jade.semantics.lang.sl.grammar.Node;
import jade.semantics.lang.sl.parser.SLParser;

import java.text.ParseException;

/**
 * Utility class holding various utility funcitons for jade utility handeling.  Primarily for
 * the semantic extension -- KBs.
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class Util {

	/**
	 * 
	 */
	public Util() {
	}
	
	static public Node parseExpression(String expression) throws ParseException {
		SLParser parser = SLParser.getParser();
		try {
			return parser.parseFormula(expression, true);//SL.formula(content);
		} catch (Throwable e) {
			try {
				return parser.parseTerm(expression, true);
			} catch (Throwable e2) {
				try {
					return parser.parseContent(expression, true);
				} catch (Throwable e3) {
				  ParseException e1 = new ParseException("MLMessage.getContent(): Could not parse expression '"+expression+"'",0);
			 	  e1.initCause(e);
				  throw e1;
				}
			}
		}

	}

}
