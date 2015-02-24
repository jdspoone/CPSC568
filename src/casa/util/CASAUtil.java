package casa.util;

import casa.AbstractProcess;
import casa.AbstractProcess.Subthread;
import casa.AgentThreadGroup;
import casa.CasaPersistent;
import casa.MLMessage;
import casa.Status;
import casa.TokenParser;
import casa.TransientAgent;
import casa.abcl.ParamsMap;
import casa.io.CASAFilePropertiesMap;
import casa.ontology.Ontology;
import casa.ontology.owl2.OWLOntology;
import casa.ui.AgentUI;
import casa.ui.BufferedAgentUI;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.armedbear.lisp.Environment;
import org.armedbear.lisp.LispObject;
import org.armedbear.lisp.SimpleString;
import org.junit.Test;
import org.ksg.casa.CASA;

/**
 * Meant to be a place to store useful utility functions for CASA
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary.
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  The
 * Knowledge Science Group makes no representations about the suitability of
 * this software for any purpose.  It is provided "as is" without express or
 * implied warranty.</p>
 *
 * @author <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public class CASAUtil {
	//public casaUtil() {}
	private static final String ESCAPED_CHARACTERS = "\",)]}=";

	/**
	 * Given an Exception, return the stack trace as a String object
	 * @param ex an Exception object; may be null, in which case the stack trace will the generated.
	 * @return A String object version of the stack trace of the Exception object
	 */
	public static String getStack(Throwable ex) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(out);
		boolean synthetic = false;
		
		if (ex==null) {
			try {
				int i = 1/0;
			}
			catch (Throwable e) {
				ex = e;
				synthetic = true;
			}
		}
		
		ex.printStackTrace(pw);
		pw.flush();
		String ret = out.toString(); 
		
		if (synthetic) {
			int mark = ret.indexOf("casa.util.CASAUtil.getStack(");
			ret = ret.substring(mark);
			mark = ret.indexOf('\n')+1;
			ret = ret.substring(mark);
		}
		
		return ret;
	}
	
	/**
	 * Returns a new String that is <em>in</em> encoded with backslashes in
	 * front of every instance of any character that occurs in <em>escChars</em>.
	 * For example,
	 * <code>
	 *  casautil.escape("(())","()")  ==>  "\(\(\)\)"
	 * </code>
	 * escape() always escapes the backslash character, so <code>unescape(x,"")</code>
	 * is exactly the same as <code>escape(x,"\\")</code>.
	 * <br>Note that escape() and unescape() reverse each others actions (if you start
	 * with escape).  However escape applied a second time will re-encode the
	 * string (if there were some escaped charaters).  This can be undone by two
	 * calls to unescape.  Unescape applied to a string that has not been escaped
	 * may have unpredictable results.
	 * <pre>
	 * <b>code</b>                      <b>value of s</b>
	 * s = "(())";               (())
	 * s =   escape(s,"()");     \(\(\)\)
	 * s = unescape(s,"()");     (())
	 * s =   escape(s,"()");     \(\(\)\)
	 * s =   escape(s,"()");     \\(\\(\\)\\)
	 * s = unescape(s,"()");     \(\(\)\)
	 * s = unescape(s,"()");     (())
	 * </pre>
	 * @param in the input String to operate on
	 * @param escChars2 a String containing the characters to escape (prepend with a backslash)
	 * @return always returns a new String, independent of <em>in</em>
	 * @see #unescape(String, String)
	 * @see #scanFor(String, int, String)
	 */
	public static String escape(String in, String escChars2) {
		if (in == null || escChars2 == null) return in==null?null:new String(in);
		String escChars = escChars2.concat("\\");
		String out = "";
		char c;
		int end = in.length();
		for (int i=0; i<end; i++) {
			c = in.charAt(i);
			switch(c) {
			case '\n':
				out += "\\n";
				break;
			case '\t':
				out += "\\t";
				break;
			default:
				if (escChars.indexOf(c) >= 0)
					out += '\\';
				out += c;
				break;
			}
		}
		return out;
	}

	/**
	 * Removes any backslash characters that had been added to <em>in</em> by a
	 * previous call to {@link #escape(String,String)} with the same
	 * <em>escChars</em>. For example, <code>
	 *  casautil.unescape("\(\(\)\)","()")  ==>  "(())"
	 * </code> unescape() always
	 * unescapes the backslash character, so <code>unescape(x,"")</code> is
	 * exactly the same as <code>unescape(x,"\\")</code>. <br>
	 * Note that escape() and unescape() reverse each others actions (if you
	 * start with escape). However escape applied a second time will re-encode
	 * the string (if there were some escaped characters). This can be undone by
	 * two calls to unescape. Unescape applied to a string that has not been
	 * escaped may have unpredictable results.
	 * 
	 * <pre>
	 * <b>code</b>                      <b>value of s</b>
	 * s = "(())";               (())
	 * s =   escape(s,"()");     \(\(\)\)
	 * s = unescape(s,"()");     (())
	 * s =   escape(s,"()");     \(\(\)\)
	 * s =   escape(s,"()");     \\(\\(\\)\\)
	 * s = unescape(s,"()");     \(\(\)\)
	 * s = unescape(s,"()");     (())
	 * </pre>
	 * 
	 * @param in
	 *            the string to unescape
	 * @param escChars2
	 *            the escaped characters to recognize
	 * @return the unescaped string
	 * @see #escape(String,String)
	 * @see #scanFor(String,int,String)
	 */
	public static String unescape(String in, String escChars2) {
		if (in == null) return null;
		if (in.length() == 0) return ""; //can't be any escapes in 1-char String, and we need to guarantee one pass through the loop
		String escChars = escChars2.concat("\\");
		StringBuffer out = new StringBuffer(in.length());
		char c=' ', next=in.charAt(0); //fix it so the last line will do the right thing if we don't enter the loop
		int end = in.length()-1;
		int i;
		for (i=0; i<end; i++) {
			c = in.charAt(i);
			if (c == '\\') {
				next = in.charAt(i+1);
				if      (next=='n')                   {
					out.append('\n'); 
					i++;}
				else if (next=='t')                   {
					out.append('\t');
					i++;}
				else if (escChars.indexOf(next) >= 0) {
					out.append(next);
					i++;}
				else 
					out.append(c);
			}
			else {
				out.append(c);
			}
		}
		/*
    if (c == '\\') {
      if (escChars.indexOf(next) < 0) out.append(next);
    }
    else out.append(in.charAt(i));
		 */
		if (i==end)
			out.append(in.charAt(i));
		return out.toString().trim();
	}

	/**
	 * Scans for the first occurrence of any of the characters in <em>chars</em>
	 * starting at <em>startAt</em>, and returns the index (from the beginning
	 * of string, not <em>startAt</em>).  It will <b>not</b> pay attention to
	 * the any target character that is preceded by a backslash.  Thus, {@link #scanFor(String, int, String)}
	 * can be used in conjunction with {#link escape(String,String)} and {@link #unescape(String, String)} to guarantee that
	 * an unknown string can be parsed.  For example, you need to delineate an
	 * arbitrary string for future reading, but there may be further text after
	 * it.  You can't just surround your string with {} because your string may
	 * contain } characters.  Save your string, s, as <code>s="{"+escape(s,"{}")+"}"
	 * </code> you can safely use scanFor(s,1,"{}") to find the original terminating
	 * } delimiter.  The expression:
	 * <pre>
	 * unescape(s.substring(scanFor(s,0,"{")+1,scanFor(s,scanFor(s,0,"{"),"}")),"{}")
	 * </pre>
	 * successfully retrieves the original string, no matter what was in the
	 * original string.
	 * @param in
	 * @param startAt
	 * @param chars
	 * @return the index of the first occurrence of a char in <em>chars</em> or
	 *         -1 if none of the chars in <em>chars</em> are in <em>in</em>
	 * @see #unescape(String, String)
	 * @see #escape(String, String)
	 */
	public static int scanFor(String in, int startAt, String chars) {
		int pos = startAt, end = in.length();
		if (pos<0 || pos>=end) return -1;
		for (pos=startAt; pos<end; pos++) {
			char c = in.charAt(pos);
			if (c=='\\') pos++;
			else {
				if (chars.indexOf(c) >= 0) break;
				if (c=='"') {
					pos = scanFor(in, pos+1, "\"");
					if (pos<0) pos = end;
				}
				else if (c=='(' && chars.indexOf('"')<0) { //if we are not in a quotation, match ()'s.
					pos = scanFor(in, pos+1, ")");
					if (pos<0) pos = end;
				}
			}

			//TODO should we also skip across (), [], and {}? --rck
		}
		if (pos>=end) return -1;
		return pos;
	}

	/**
	 * Scans for the first occurrence of any charcter less or equal to than ascii 32 (blank)
	 * starting at <em>startAt</em>, and returns the index (from the beginning
	 * of string, not <em>startAt</em>).  It will <b>not</b> pay attention to
	 * the any charater that is preceded by a backslash.  Thus, scanForWhiteSpace()
	 * can be used in conjunction with escape() and unescape() to guarentee that
	 * an unknown string can be parsed.
	 * @param in
	 * @param startAt
	 * @return the index of the first occurance of a char in <em>chars</em> or
	 *         -1 if none of the chars in <em>chars</em> are in <em>in</em>
	 */
	public static int scanForWhiteSpace(String in, int startAt) {
		int pos = startAt, end = in.length();
		for (pos = startAt; pos < end; pos++) {
			char c = in.charAt(pos);
			if (c == '\\') pos++;
			else if (c <= 32) break;
		}
		if (pos == end)
			return -1;
		return pos;
	}

	/**
	 * Returns the <em>s</em> surrounded by double-quotes and with any double-quotes
	 * originally in <em>s</em> escaped.
	 * @param s
	 * @return Returns the <em>s</em> surrounded by double-quotes and with any double-quotes
	 * originally in <em>s</em> escaped.
	 * @see #escape(String, String)
	 */
	public static String toQuotedString(String s) {
		return "\""+escape(s,"\"")+"\"";
	}

	/**
	 * Returns exactly the same results as {@link #fromQuotedString(String, int) fromQuotedString(<em>s</em>,0)}.
	 * @param s the quoted string to de-quote
	 * @return an unquoted, unescaped string
	 * @throws ParseException if <em>s</em> can't be interpreted as a quoted string (eg: first non-white-space isn't '"') starting at <em>startAt</em>
	 */
	public static String fromQuotedString(String s) throws ParseException {
		return fromQuotedString(s,0);
	}

	/**
	 * Given a String (as generated from {@link #toQuotedString(String)})
	 * returns a String without the enclosing quotes and with an escaped double-quotes
	 * restored (unescaped).  Extra chars after the closing quote are ignored.
	 * @param s the quoted string to de-quote
	 * @param startAt the position is <em>s</em> to start at
	 * @return an unquoted, unescaped string
	 * @throws ParseException if <em>s</em> can't be interpreted as a quoted string (eg: first non-white-space isn't '"') starting at <em>startAt</em>
	 * @see #unescape(String, String)
	 */
	public static String fromQuotedString(String s, int startAt) throws ParseException {
		int pos = startAt, mark;
		while (Character.isWhitespace(s.charAt(pos))) pos++; //read whitespace
		if (s.charAt(pos) != '\"') throw new ParseException("Expected '\"'",pos);
		mark = ++pos;
		pos = scanFor(s,pos,"\"");
		if (pos < 0) throw new ParseException("Expected '\"'",s.length());
		return unescape(s.substring(mark,pos),ESCAPED_CHARACTERS);
	}

	/**
	 * Returns the result <em>x.toString()</em> but surrounded by double-quotes
	 * and with any double-quotes originally in <em>x.toString()</em> escaped.
	 * @param x the object to turn into a quoted string
	 * @return as above
	 * @see #escape(String, String)
	 */
	public static String toQuotedString(Object x) {
		return toQuotedString(x.toString());
	}

	/**
	 * Returns the serialized Object x in the form:
	 * <pre>
	 * '(' class-name ')' serialized-object
	 * </pre>
	 * where <code>serialized-object</code> is obtained by calling the object's
	 * <em>toString()</em> method and applying {@link #toQuotedString(String)}
	 * to it.  This will surround it is double-quotes and escape any quotes
	 * that happen to be in the string.
	 * @param x the object o serialize
	 * @return a String representing the object
	 * @see #unserialize(String, String)
	 */
	public static String serialize(Object x) {
		if (x==null) return "null";
		if (x==org.armedbear.lisp.Lisp.NIL) return "NIL";
		if (x instanceof org.armedbear.lisp.Fixnum)
			return Long.toString(((org.armedbear.lisp.Fixnum)x).longValue()); 
		if (x instanceof String) {
			if (scanForWhiteSpace((String)x, 0)==-1)
				return (String)x;
			else
				return toQuotedString((String)x);
		}
		if (x instanceof Collection<?>)
			return serialize ((Collection<?>) x);
		if (x instanceof Map<?, ?>)
			return serialize ((Map<?, ?>) x);
		if (x instanceof Object[])
			return serialize((Object[])x);
		else
			if (isSerializable(x))
				return "("
						+ x.getClass().getName()
						+ ")"
						+ toQuotedString(x.toString());
			else {
				Trace.log("error", "Cannot serialize object '"+x.getClass().getName()+"', returning null.");
				return null;
			}
	}

	private static String innerSerialize(Object x) {
		return toQuotedString(serialize(x));
	}

	/**
	 * Calls {@link #unserialize(String, int, String) unserialize(s,0)} to return an
	 * object instantiated from s.
	 * @param s the string containing the serialized object
	 * @param className TODO
	 * @return an object instantiated from s
	 * @throws ParseException if a new object cannot be instantiated from s
	 */
	public static Object unserialize(String s, String className) throws ParseException {
		return unserialize(s, 0, className);
	}

	/**
	 * Attempts to construct an object from a serialized string.  Serialized
	 * objects may be of the form:
	 * <pre>
	 * serial-obj    ::= null | hashtable | array | quoted-string | integer |
	 *                   collection | simple-object
	 * null          ::= "null"
	 * hashtable     ::= "{" { serial-obj "=" serial-obj } { "," serial-obj "=" serial-obj }* "}"
	 * array         ::= "[" { serial-obj } { "," serial-obj }* "]"
	 * quoted-string ::= '"' text-with-escaped-quotes '"'
	 * integer       ::= {digit}+
	 * collection    ::= "(" class-name ")" array
	 * simple-object ::= "(" class-name ")" quoted-string
	 * </pre>
	 * <code>simple-object</code>s are all instantiated by trying several means in
	 * the following order:
	 * <ol>
	 * <li>A String constructor
	 * <li>A null constructor and calling fromString(String) method on the object
	 * <li>A null constructor and calling fromString({@link casa.TokenParser}) method on the object
	 * </ol>
	 * A collection is instantiated by its null constructor and then calling the
	 * <em>add(Object)</em> method for each serialized object.
	 * <br>
	 * Extra characters after the close of the object are ignored.
	 * <p>
	 * If all the above fails, further attempts are made:
	 * <ol>
	 * <li>if <em>defaultClassName</em> is not null, {@link #interpretString(String, String)} is called
	 * to interpret raw characters as per the "several means" above.
	 * <li>if the first non-whitespace character in <em>s</em> is "(", then the s is returned (as it 
	 * may be either a Lisp expression or a FIPA-SL expression.
	 * </ol>
	 * @param s the String containing the serialized object(s)
	 * @param startAt the position in the String s to start at
	 * @param className TODO
	 * @return An object instantiated from s
	 * @throws ParseException if an object cannot be instantiated
	 */
	public static Object unserialize(String s, int startAt, String defaultClassName) throws ParseException {
		int pos = startAt, mark;
		String className, serializedObj;
		Class<?> _class;
		if (s.length()==0) return (Object)"";
		while (Character.isWhitespace(s.charAt(pos)))
			pos++;
		char c = s.charAt(pos);

		try { //do normal processing as a regular serializable string, the if we fail, try it interpreting using InterpretString(String,String)
			if (c != '(') {
				if (c == 'n' && s.length()>=pos+4 && s.substring(pos,pos+4).equalsIgnoreCase("null")) return null;
				if (c == 'N' && s.length()>=pos+3 && s.substring(pos,pos+3).equalsIgnoreCase("NIL")) return org.armedbear.lisp.Lisp.NIL;
				if (c == '{') return unserializeHashtable (s, pos, defaultClassName);
				if (c == '[') return unserializeArray (s, pos, defaultClassName);
				if (c == '\"') return fromQuotedString (s, pos);
				if (Character.isDigit (c)||c=='-') {
					int e = pos+1;
					while (e<s.length() && Character.isDigit(s.charAt(e))) e++ ;
					try {
						Integer i = new Integer (s.substring(pos,e));
						return i;
					} catch (NumberFormatException ex3) {
						throw new ParseException (
								"Expected '(' or '{' or '\"' or a decimal number", pos);
					}
				} else { // assume it's a string
					//throw new ParseException ("Expected '(' or '{' or '\"' or a digit", pos);
					return s;
				}
			}
			mark = ++pos;
			pos = scanFor(s, pos, ")");
			if (pos < 0)
				throw new ParseException("Expected ')'", s.length());
			className = s.substring(mark, pos);
			try {
				_class = Class.forName(className);
			}
			catch (ClassNotFoundException ex) {
				throw new ParseException("Expected a class name", mark);
			}
			// if it is a collection, use a different approach
			if (Collection.class.isAssignableFrom(_class)) {
				Object[] tempArray = unserializeArray(s, ++pos, className);
				// create new object of type _class
				Constructor<? extends Collection> con = null;
				Collection<Object> newCollection = null;
				try {
					con = _class.asSubclass(Collection.class).getConstructor (new Class[0]);
					newCollection = (Collection<Object>) con.newInstance (new Object [0]);
				} catch (SecurityException ex5) {
					throw new ParseException (className +
							"() constructor is not accessable: " +
							ex5.toString (), mark);
				} catch (NoSuchMethodException ex5) {
					throw new ParseException ("Class '" + className + "' (a subclass of Collection) must have a constructor taking a no arguments",
							mark);
				} catch (InvocationTargetException ex5) {
					throw new ParseException ("new " + className +
							"() constructor call failed (InvocationTargetException): " +
							ex5.getCause().toString (), mark);
				} catch (Exception ex5) {
					throw new ParseException ("new " + className +
							"() constructor call failed: " +
							ex5.toString (), mark);
				}

				// loop through array and add elements
				int length = tempArray.length;
				for (int i = 0; i < length; i++) {
					newCollection.add (tempArray[i]);
				}

				// return new object
				return newCollection;
			} else { // a non-collection object....
				serializedObj = fromQuotedString (s, ++pos);
				Class<?>[] paramsType = {String.class};
				Constructor<?> con;
				Object[] constructorParameters = null;
				try { // try to get get a String constructor
					con = _class.getConstructor (paramsType);
					constructorParameters = new Object[] {serializedObj};
				} catch (Exception ex1) {
					Object[] fromStringParameters;
					Method fromStringMethod;
					try { //try to use the .fromString(String) method
						fromStringMethod = _class.getMethod ("fromString", paramsType);
						fromStringParameters = new Object[] {serializedObj};
					} catch (Exception ex4) {
						try { //try to use the .fromString(casa.TokenParser) method
							Class<?>[] paramsType2 = {TokenParser.class};
							fromStringMethod = _class.getMethod ("fromString", paramsType2);
							fromStringParameters = new Object[] {new casa.TokenParser(serializedObj)};
						} catch (Exception ex5) {
							throw new ParseException ("Class '" + className + "' must have a public constructor taking a single String argument or a public method fromString(String) or fromString(casa.TokenParser) and a constructor with no parameters",
									mark);
						}
					}

					try {
						Modifier m;
						if ((fromStringMethod.getModifiers()&Modifier.STATIC) != 0) { // static fromString() case
							Object object = fromStringMethod.invoke(null, fromStringParameters);
							return object;
						}
						else { // non-static fromString() case
							con = _class.getConstructor(new Class<?>[]{});
							Object o = con.newInstance(new Object[]{});
							Object object = fromStringMethod.invoke(o, fromStringParameters);
						}
					} catch (Exception e) {
						ParseException e2 = new ParseException(serializedObj,0);
						e2.initCause(e);
						throw e2;
					}
				}

				try {
					Object object = con.newInstance (constructorParameters);
					return object;
				} catch (InvocationTargetException ex5) {
					throw new ParseException ("new " + className +
							"() constructor call failed (InvocationTargetException): " +
							ex5.getCause().toString (), mark);
				} catch (Exception ex2) {
					throw new ParseException ("new " + className +
							"(String) constructor call failed: " +
							ex2.toString (), mark);
				}
			}
		} catch (ParseException ex) { // Failed to parse the normal casa.* syntax, try using the defaultClassName
			if (defaultClassName!=null) {
				Object ret = interpretString(s.substring(startAt), defaultClassName);
				if (ret != null)
					return ret;
			}
			if (c=='(' && startAt==0) {
				try {
					Class.forName(defaultClassName);
				} catch (Throwable e) {
					return s;
				}
			}
			throw ex;
		}
	}

	/**
	 * Attempt to interpret the string <em>content</em> as an object of type <em>className</em>.
	 * This "attempt" is made by trying to find constructors or methods in class <em>className</em>
	 * as follows (in this order):
	 * <ol>
	 * <li> public constructor <em>className</em>(String)
	 * <li> public static <em>className</em>.toString(String)
	 * <li> public static <em>className</em>.toString({@link casa.TokenParser})
	 * </ol>
	 * @param content The String to interpret as above
	 * @param className The name of the class the <em>content</em> is expected to be an object of
	 * @return An object of the type <em>className</em>, or null if <em>content</em> cannot be interpreted as an object of class <em>className</em>.
	 */
	public static Object interpretString(String content, String className) {
		Object ret = null;
		try {
			Class<?> cls = Class.forName(className);
			try { // String constructor
				Class<?>[] strParamType = { Class.forName("java.lang.String") };
				Constructor<?> constr = cls.getConstructor(strParamType);
				String[] strParam = { content };
				ret = constr.newInstance((Object[]) strParam);
			} catch (Exception ex1) {}
			if (ret == null) { // empty constructor
				String[] strParam = { content };
				try { // obj.fromString(String)
					Class<?>[] strParamType = { Class.forName("java.lang.String") };
					Method m = cls.getMethod("fromString", strParamType);
					if ((m.getModifiers()&Modifier.STATIC)==0) {
						Constructor<?> constr = cls.getConstructor((Class[]) null);
						Object obj = constr.newInstance((Object[]) null);
						ret = m.invoke(obj, (Object[]) strParam);
					}
					else {
						ret = m.invoke(null, (Object[])strParam);
					}
				} catch (NoSuchMethodException ex) { // obj.fromString(TokenParser)
					Class<?>[] strParamType = { Class.forName("casa.TokenParser") };
					Method m = cls.getMethod("fromString", strParamType);
					Object[] params = { new TokenParser(content) };
					ret = m.invoke(null, (Object[])params);
				}
			}
		} catch (Exception ex) {}
		return ret;
	}

	/**
	 * Determines if the object is serializable by calling {@link #isSerializable(Class)} with the class of the object.
	 * @see CASAUtil#isSerializable(Class)
	 * @param o The object to inspect
	 * @return True if the object is serializable
	 */
	public static boolean isSerializable(Object obj) {
		Class<?> _class = obj.getClass();
		return isSerializable(_class);
	}

	/**
	 * Determines if a class is serializable by testing to see if at least one of the following conditions holds:
	 * <ul>
	 * <li> it has a constructor with a single String parameter
	 * <li> it has a fromString({@link java.lang.String}) method and a constructor with no parameters
	 * <li> it has a fromString({@link casa.TokenParser}) method and a constructor with no parameters
	 * </ul>
	 * @param _class The Class to inspect.
	 * @return true if the class is serializatable (the above conditions hold).
	 */
	public static boolean isSerializable(Class<?> _class) {
		Class<?>[] paramsType = {String.class};
		try { // try to get get a String constructor
			_class.getConstructor (paramsType);
			return true;
		} catch (Exception ex1) {
			try { //try to use the .fromString(String) method
				_class.getMethod ("fromString", paramsType);
				return true;
			} catch (Exception ex4) {
				try { //try to use the .fromString(casa.TokenParser) method
					Class<?>[] paramsType2 = {TokenParser.class};
					_class.getMethod ("fromString", paramsType2);
					return true;
				} catch (Exception ex5) {
					// fromString() is not accessible
					return false;
				}
			}

//			try {
//				_class.getConstructor (new Class[0]);
//				return true;
//			} catch (SecurityException ex2) {
//				return false;
//			} catch (NoSuchMethodException ex2) {
//				return false;
//			}
		}
	}

	//  /**
	//   * Given an {@link java.lang.Map} object, returns a String of the form:
	//   * <pre>
	//   *   "{" { serial-obj "=" serial-obj } { "," serial-obj "=" serial-obj }* "}"
	//   * </pre>
	//   * (See {@link #unserialize(String, int)} for the definition of <code>serial-obj</code>.)
	//   * @param map the data structure to serialize
	//   * @return a string representing <em>map</em>
	//   */
	//  public static String serialize(Map<String, ?> map) {
	//    if (map==null) return "null";
	//    String out = "{";
	//    Set<String> keys = map.keySet();
	//    Object key;
	//    for (Iterator<String> i = keys.iterator(); i.hasNext();) {
	//      key = i.next();
	//      out += innerSerialize(key)
	//           + "="
	//           + innerSerialize(map.get(key))
	//           + (i.hasNext()?", ":"");
	//    }
	//    out += "}";
	//    return out;
	//  }

	/**
	 * Given an {@link java.lang.Map} object, returns a String of the form:
	 * <pre>
	 *   "{" { serial-obj "=" serial-obj } { "," serial-obj "=" serial-obj }* "}"
	 * </pre>
	 * (See {@link #unserialize(String, int, String)} for the definition of <code>serial-obj</code>.)
	 * @param map the data structure to serialize
	 * @return a string representing <em>map</em>
	 */
	public static String serialize(Map<?, ?> map) {
		if (map==null) return "null";
		String out = "{";
		Set<?> keys = map.keySet();
		Object key;
		for (Iterator<?> i = keys.iterator(); i.hasNext();) {
			key = i.next();
			out += innerSerialize(key)
					+ "="
					+ innerSerialize(map.get(key))
					+ (i.hasNext()?", ":"");
		}
		out += "}";
		return out;
	}

	/**
	 * Given an array of Objects, returns an String of the form:
	 * <pre>
	 *   "[" { serial-obj } { "," serial-obj }* "]"
	 * </pre>
	 * (See {@link #unserialize(String, int, String)} for the definition of <code>serial-obj</code>.)
	 * @param array the array to serialize
	 * @return a string representing <em>array</em>
	 */
	public static String serialize(Object... array) {
		if (array==null) return "null";
		StringBuffer out = new StringBuffer ("[");
		for (int i = 0, end=array.length-1; i<=end; i++) {
			out.append (innerSerialize(array[i]));
			if (i < end) {
				out.append (", ");
			}
		}
		out.append (']');
		return out.toString ();
	}

	/**
	 * Given a collection of Objects, returns an String of the form:
	 * <pre>
	 *   "(" class-name ")" array
	 * </pre>
	 * (See {@link #serialize(Object[])} for the definition of <code>array</code>.)
	 * @param collection the Collection to serialize
	 * @return a string representing <em>array</em>
	 */
	public static String serialize(Collection<?> collection) {
		return "("
				+ collection.getClass().toString().substring(6)
				+ ")" + serialize (collection.toArray());
	}

	/**
	 * Behaves exactly the same as
	 * {@link #unserializeHashtable(String, int, String) unserializeHashtable(<em>s</em>,0)}.
	 * @param s the String to interpret a {@link java.util.Collection} from.
	 * @param className TODO
	 * @return an object interpreted from <em>s</em>
	 * @throws ParseException if an object cannot be instantiated from <em>s</em>
	 */
	public static Hashtable<Object, Object> unserializeHashtable(String s, String className) throws ParseException {
		return unserializeHashtable(s,0, className);
	}

	/**
	 * Returns a {@link java.util.HashTable} object instantiated from <em>s</em>.
	 * <em>s</em> is expected to be of the form:
	 * <pre>
	 *   "(" class-name ")" array
	 * </pre>
	 * See {@link #unserializeArray(String, int, String)} for the definition of
	 * <code>array</code>.
	 * Extra characters after the end of the object are ignored.
	 * @param s the String to interpret a {@link java.util.Collection} from.
	 * @param startAt the position in <em>s</em> to start parsing
	 * @param className TODO
	 * @return an object interpreted from <em>s</em>
	 * @throws ParseException if an object cannot be instantiated from <em>s</em>
	 */
	public static Hashtable<Object, Object> unserializeHashtable(String s, int startAt, String className) throws ParseException {
		int pos = startAt, mark;
		Object key, value;
		Hashtable<Object, Object> table = new Hashtable<Object, Object>();
		while (Character.isWhitespace(s.charAt(pos)))
			pos++;
		if (s.charAt(pos) == 'n' && s.length()>=pos+4 && s.substring(pos,pos+4).equals("null")) return null;
		if (s.charAt(pos) != '{')
			throw new ParseException("Expected '{'", pos);
		while (s.charAt(pos) != '}') {
			mark = ++pos;
			pos = scanFor(s, pos, "=}");
			if (s.charAt(pos)=='}') break;
			try {
				key = unserializeQuotedString(s, mark, className);
			}
			catch (ParseException ex) {
				try {
					key = unserialize (s, mark, null);
				}
				catch (ParseException ex2) {
					throw new ParseException(ex2.toString(),mark+ex2.getErrorOffset()); //fix up the offset
				}
			}
			mark = ++pos;
			pos = scanFor(s, pos, ",}");
			try {
				value = unserializeQuotedString(s, mark, className);
			}
			catch (ParseException ex) {
				try {
					value = unserialize (s, mark, null);
				}
				catch (ParseException ex2) {
					throw new ParseException(ex2.toString(),mark+ex2.getErrorOffset()); //fix up the offset
				}
			}
			table.put(key, value);
		}
		return table;
	}

	static public Object unserializeQuotedString(String input, int mark, String className) throws ParseException {
		String s;
		try {
			s = fromQuotedString(input, mark);
		} catch (ParseException e) {
			throw new ParseException (e.toString (), mark + e.getErrorOffset ()); //fix up the offset;
		}
		Object value;
		try {
			value = unserialize (s, className);
		} catch (ParseException ex) {
			return s;
		}
		return value;
	}

	/**
	 * Behaves exactly the same as
	 * {@link #unserializeArray(String, int, String) unserializeArray(<em>s</em>,0)}.
	 * @param s the String to interpret an array of objects from.
	 * @param className TODO
	 * @return an object interpreted from <em>s</em>
	 * @throws ParseException if an object cannot be instantiated from <em>s</em>
	 */
	public static Object[] unserializeArray(String s, String className) throws ParseException {
		return unserializeArray(s,0, className);
	}

	/**
	 * Returns an array of Objects instantiated from <em>s</em>.
	 * <em>s</em> is expected to be of the form:
	 * <pre>
	 *   "[" { serial-obj } { "," serial-obj }* "]"
	 * </pre>
	 * See {@link #unserialize(String, int, String)} for the definition of
	 * <code>serial-obj</code>.
	 * Extra characters after the end of the object are ignored.
	 * @param s the String to interpret an array of Objects from.
	 * @param startAt the position in <em>s</em> to start parsing
	 * @param className TODO
	 * @return an array of Objects interpreted from <em>s</em>
	 * @throws ParseException if an object cannot be instantiated from <em>s</em>
	 */
	public static Object[] unserializeArray(String s, int startAt, String className) throws ParseException {
		int pos = startAt, mark;
		Object value;
		ArrayList<Object> v = new ArrayList<Object>();
		while (Character.isWhitespace(s.charAt(pos)))
			pos++;
		if (s.charAt(pos) == 'n' && s.length()>=pos+4 && s.substring(pos,pos+4).equals("null")) return null;
		if (s.charAt(pos) != '[')
			throw new ParseException("Expected '['", pos);

		mark = ++pos;
		pos = scanFor(s, pos, ",]"); 
		// if the end character isn't a ']', it isn't empty, and if the next
		// section has non-whitespace characters, it isn't empty.
		if (s.charAt (pos) != ']' || s.substring (mark, pos).trim ().length () != 0) {
			value = unserializeQuotedString(s, mark, className);
			//    	try {
			//    		value = unserialize (fromQuotedString(s, mark));
			//    	} catch (ParseException ex) {
			//    		//      throw new ParseException (ex.toString (), mark + ex.getErrorOffset ()); //fix up the offset
			//    		try {
			//    			value = fromQuotedString(s, mark);
			//    		} catch (ParseException ex2) {
			//    			throw new ParseException (ex.toString ()+" or "+ex2.toString (), mark + ex.getErrorOffset ()); //fix up the offset
			//    		}
			//    	}
			v.add (value);
		}

		while (s.charAt (pos) != ']') {
			mark = ++pos;
			pos = scanFor(s, pos, ",]");
			value = unserializeQuotedString(s, mark, className);
			//      try {
			//        value = unserialize (fromQuotedString(s, mark));
			//      } catch (ParseException ex) {
			////        throw new ParseException (ex.toString (), mark + ex.getErrorOffset ()); //fix up the offset
			//        try {
			//          value = fromQuotedString(s, mark);
			//        } catch (ParseException ex2) {
			//          throw new ParseException (ex.toString ()+" or "+ex2.toString (), mark + ex.getErrorOffset ()); //fix up the offset
			//        }
			//      }
			v.add (value);
		}
		return v.toArray();
	}

	public static void main(String[] args) {
		try {
			InetAddress addr1 = InetAddress.getByName("10.0.1.6");
			InetAddress addr4 = InetAddress.getByName("10.0.1.7");
			InetAddress addr2 = InetAddress.getByName("68.147.227.36");
			InetAddress addr3 = InetAddress.getByName("136.159.7.26");
//			System.out.println("Addr1: "+getNetwork(addr1, null));
//			System.out.println("Addr2: "+getNetwork(addr2, null));
//			System.out.println("Addr3: "+getNetwork(addr3, null));

//			System.out.println("my: ");
//			for (InetAddress a: getMyInetAddresses()) System.out.println("  "+a); 
			System.out.println(addr1+": ");
			for (InetAddress a: getInetAddressesFor(addr1)) System.out.println("  "+a); 
			System.out.println(addr4+": ");
			for (InetAddress a: getInetAddressesFor(addr4)) System.out.println("  "+a); 
			System.out.println(addr2+": ");
			for (InetAddress a: getInetAddressesFor(addr2)) System.out.println("  "+a); 
			System.out.println(addr3+": ");
			for (InetAddress a: getInetAddressesFor(addr3)) System.out.println("  "+a); 
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
//		DEBUG.setDEBUGGING(true);
//		String s;
//		System.out.println((s = "(())")+".");
//		System.out.println((s =   escape(s,"()"))+".");
//		System.out.println((s = unescape(s,"()"))+".");
//		System.out.println((s =   escape(s,"()"))+".");
//		System.out.println((s =   escape(s,"()"))+".");
//		System.out.println((s = unescape(s,"()"))+".");
//		System.out.println((s = unescape(s,"()"))+".");
//		s = "  xhou  ("+s+")";
//		System.out.println(Integer.toString(scanFor(s,0,"("))+" "+Integer.toString(scanFor(s,0,")"))+" "+Integer.toString(scanFor(s,scanFor(s,0,"()")+1,"()"))+" " );
//		System.out.println(s = "{}{}");
//		System.out.println(s ="{"+escape(s,"{}")+"}");
//		System.out.println(unescape(s.substring(scanFor(s,0,"{")+1,scanFor(s,scanFor(s,0,"{"),"}")),"{}"));
//
//		try {
//			URLDescriptor url = URLDescriptor.make("kremer", "136.159.4.4", "100",
//					"/path/file");
//			System.out.println(url.toString(null));
//			System.out.println(s = serialize(url));
//			URLDescriptor url2 = (URLDescriptor) unserialize(s, null);
//			System.out.println("port="+Integer.toString(url2.getPort())+"  user="+url2.getUser());
//			System.out.println(url2.toString(null));
//		}
//		catch (ParseException ex1) {
//			CASAUtil.log("error", ex1.toString()+" at position "+Integer.toString(ex1.getErrorOffset()), ex1, true);
//		}
//		catch (URLDescriptorException ex1) {
//			CASAUtil.log("error", "CASAUtil.main", ex1, true);
//		}
//
//
//		Hashtable<String, String> table = new Hashtable<String, String>(), table2 = new Hashtable<String, String>();
//		table.put("key1","\"value\"1\"");
//		table.put("key2","\\value\\2\\");
//		table.put("key3","value3");
//		System.out.println(table.toString());
//		System.out.println(s = serialize(table));
//		try {
//			table2 = (Hashtable<String, String>) unserialize(s, null);
//		}
//		catch (ParseException ex) {
//			CASAUtil.log("error", ex.toString()+" at position "+Integer.toString(ex.getErrorOffset()), ex, true);
//		}
//		System.out.println(table2.toString());
	}

	/**
	 * Given a string with "binary" data (ASCII codes less than 32), returns a string
	 * with each of the non-printable characters (less than ASCII 32) substituted
	 * with "%hh", where "h" is a hex digit.
	 * @param input the string to encode
	 * @param escapeAmp 
	 * @return the encoded string
	 */
	public static String encode(String input, boolean escapeAmp) {
		char percent = '%';
		char amp = '&';
		String encodedAmp = "&amp;";

		// Do the encoding
		int inputLength = input.length();
		StringBuffer output = new StringBuffer();
		char tempChar;

		for (int inputPosition = 0; inputPosition < inputLength; inputPosition++) {
			tempChar = input.charAt(inputPosition);

			if (tempChar <= 0x20) {
				output.append(percent);
				if (tempChar < 0x10) {
					output.append('0');
				}
				output.append(Integer.toHexString(tempChar));
			} else if (escapeAmp & tempChar == amp) {
				output.append (encodedAmp);
			} else {
				output.append(tempChar);
			}
		}
		return output.toString();
	}

	public static StringBuilder pad(StringBuilder buf, int n_spaces) {
		for (int i=n_spaces; i>0; i--) buf.append(' ');
		return buf;
	}

	public static String padLeftTo(String s, char c, int n_spaces) {
		int len = s.length();
		StringBuilder b = new StringBuilder();
		for (int i=n_spaces-len; i>0; i--)
			b.append(c);
		b.append(s);
		return b.toString();
	}

	public static String padRightTo(String s, char c, int n_spaces) {
		int len = s.length();
		StringBuilder b = new StringBuilder();
		b.append(s);
		for (int i=n_spaces-len; i>0; i--)
			b.append(c);
		return b.toString();
	}

	/**
	 * Given a string with encoded "binary" data ("%hh" for ASCII codes less
	 * than 32), returns a string with each of the encoded characters replaced
	 * by the approriate "binary" (non-printable) character.
	 * @param input the string to encode
	 * @param decodeAmp
	 * @return the encoded string
	 */
	public static String decode(String input, boolean decodeAmp) {
		char percent = '%';
		char amp = '&';
		String encodedAmp = "&amp;";

		// Do the decoding
		int inputLength = input.length();
		StringBuffer output = new StringBuffer();
		char tempChar;

		for (int inputPosition = 0; inputPosition < inputLength; inputPosition++) {
			tempChar = input.charAt(inputPosition);

			if (tempChar == percent) {
				char testChar1 = input.charAt(inputPosition + 1);
				char testChar2 = input.charAt(inputPosition + 2);

				// If first and second character after percent are hexadecimal
				if (isHexDigit(testChar1) && isHexDigit(testChar2)) {
					// It is an encoded character
					output.append( (char) Integer.parseInt(input.substring(inputPosition +
							1, inputPosition + 3), 16));
					inputPosition += 2;
				} else {
					output.append(tempChar);
				}
			} else if (decodeAmp && tempChar == amp) {
				if (input.startsWith (encodedAmp, inputPosition)) {
					output.append (amp);
					inputPosition += 4;
				}
			} else {
				output.append(tempChar);
			}
		}

		return output.toString();
	}

	private static boolean isHexDigit(char input) {
		return (input >= '0' && input <= '9') ||
				(input >= 'a' && input <= 'f') ||
				(input >= 'A' && input <= 'F');
	}

	//
	// New concrete method for getLocalHost
	//

	static private InetAddress cachedLocalHost=null;

	/**
	 * This method should be used by CASA in place of
	 * {@link java.net.InetAddress#getLocalHost()}.  Since the firewalls etc. may
	 * cause casa to have to use a different local host IP address, an local
	 * host ip address may be stored in the file CASA.casa (a CASA-format file)
	 * under the property "router".  This method will look in this file (if it
	 * exists) before using the InetAddress.getLocalHost() method to get the
	 * local host.
	 * @return the InetAddress for the local host
	 */
	public static InetAddress getLocalHost() {
		if (cachedLocalHost!=null) return cachedLocalHost;

		//look for pre-specified host address in CASA.casa (in the current def dir)
		try {
			//      CASAFile dataFile = new CASAFile ("CASA.casa");
			//      CASAFilePropertiesMap properties = new CASAFilePropertiesMap (dataFile);
			//      String routerString = new String (properties.getString ("router"));
			String routerString = CASA.getPreference("router", (String)null, 0);
			if (routerString!=null && routerString.length()>0) {
				routerString += ".";
				byte ip[] = {0, 0, 0, 0};
				for (int i = 0, j = -1; i < 4; i++) {
					ip[i] = (byte) Integer.parseInt (routerString.substring (j + 1,
							(j = routerString.indexOf ('.', j+1))));
				}
				cachedLocalHost = InetAddress.getByAddress (ip);
				//DEBUG.PRINT("CASAUtil.getLocalHost: Set LocalHost to '"+cachedLocalHost.toString()+"' from properties in CASA.casa file");
				return cachedLocalHost;
			}
		}
		catch (UnknownHostException ex1) {}
		catch (NumberFormatException ex1) {}
		catch (Exception ex1) {}

		//failed the above.  Get the local host from InetAddress
		try {
			cachedLocalHost = InetAddress.getLocalHost ();
			//DEBUG.PRINT("CASAUtil.getLocalHost: Set LocalHost to '"+cachedLocalHost.toString()+"' from InetAddress class");
			return cachedLocalHost;
		} catch (UnknownHostException ex) {
			return null;
		}
	}

	public static String byteArrayAsHexString (byte[] buffer) {
		int i;
		StringBuffer hexData = new StringBuffer ();
		int hexOnLine = 0;
		String tempString;
		char tempChar;
		for (i = 0; i < buffer.length; i++) {
			tempString = Integer.toHexString (buffer[i]);
			int tempLength = tempString.length ();
			if (tempLength == 0) {
				hexData.append ("00");
			} else if (tempLength == 1) {
				hexData.append ('0');
				hexData.append (tempString);
			} else if (tempLength == 2) {
				hexData.append (tempString);
			} else {
				hexData.append (tempString.charAt(tempLength - 2));
				hexData.append (tempString.charAt(tempLength - 1));
			}
			hexData.append (' ');
			hexOnLine++;
			if (hexOnLine == 8) {
				hexData.append (' ');
			}
			if (hexOnLine >= 16) {
				hexData.append (' ');
				for (int j = i - 15; j <= i; j++) {
					tempChar = (char) buffer[j];
					if (tempChar >= 32 && tempChar <= 126) {
						hexData.append (tempChar);
					} else {
						hexData.append ('.');
					}
				}
				hexData.append ('\n');
				hexOnLine = 0;
			}
		}

		if (hexOnLine != 0) {
			for (int j = 0; j < (16 - hexOnLine); j++) {
				hexData.append ("   ");
			}
			if (hexOnLine < 9) {
				hexData.append (" ");
			}
			hexData.append (" ");
			for (int j = i - hexOnLine; j < buffer.length; j++) {
				tempChar = (char) buffer[j];
				if (tempChar >= 32 && tempChar <= 126) {
					hexData.append (tempChar);
				} else {
					hexData.append (".");
				}
			}
		}

		return hexData.toString ();
	}

	/**
	 * Given an array of objects, return an array of Class corresponding to the
	 * classes of the objects
	 * @param o the array of Objects
	 * @return an array of Class objects with each element being the class of the
	 *   object in the corresponding element of <em>o</em>.
	 */
	public static Class<?>[] objectsToClasses(Object[] o) {
		if (o==null) return null;
		Class<?>[] c = new Class[o.length];
		for (int i=0; i<o.length; i++) {
			c[i] = o[i].getClass();
		}
		return c;
	}

	public static String formatWidth(String text, int lineWidth, String autoIndent) {
		String out = "";
		String prefix = "";
		boolean newline = true;
		int targetWidth = lineWidth-5;
		int lineCount = 0;

		if (autoIndent==null) autoIndent = "";
		int autoIndentLength = autoIndent.length();

		for (int i=0, end=text.length(); i<end; i++) {
			char c = text.charAt(i);
			out += c;
			lineCount++;
			if (newline && Character.isWhitespace(c)) prefix+=c;
			else newline = false;
			if (c=='\n') {
				newline = true;
				prefix="";
				lineCount = 0;
			}
			if (lineCount>targetWidth && Character.isWhitespace(c)) {
				out += '\n' + prefix + (prefix.length()>0?autoIndent:"");
				lineCount = prefix.length() + (prefix.length()>0?autoIndentLength:0);
			}
		}
		return out;
	}

	/**
	 * Returns the current time as a formated date string in the default format
	 * @return The formated date string
	 */
	public static String getDateAsString() {
		return getDateAsString(System.currentTimeMillis(), null);
	}

	/**
	 * Returns the current time as a formated date string
	 * @param format The format of the date string according to {@link SimpleDateFormat#format(Date)}.
	 * @return The formated date string
	 */
	public static String getDateAsString(String format) {
		return getDateAsString(System.currentTimeMillis(), format);
	}

	/**
	 * Converts the long millisecond time to a formated date string in the default format
	 * @param timeSpec the time in milliseconds to convert to a date string
	 * @return The formated date string
	 */
	public static String getDateAsString(Long timeSpec) {
		return getDateAsString(timeSpec,null);
	}

	/**
	 * Converts the long millisecond time to a formated date string
	 * @param timeSpec the time in milliseconds to convert to a date string
	 * @param format The format of the date string according to {@link SimpleDateFormat#format(Date)}.
	 * @return The formated date string
	 */
	public static String getDateAsString(Long timeSpec, String format) {
		String date;
		try {
			Date _date = new Date(timeSpec);
			if (format==null)
				date = _date.toString();
			else
				date = new SimpleDateFormat(format).format(_date);
		} catch (Exception e) {
			date = Long.toString(timeSpec);
		}
		return date;
	}

//	/**
//	 * Returns the first available port number.  There is no lock placed on the port number 
//	 * returned.  It may be stolen by a competing process.
//	 * @return open
//	 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
//	 */
//	public static int getOpenPort(){
//		ServerSocket srv = null;
//		int open = -1;
//		try {
//			srv = new ServerSocket(0);
//			open = srv.getLocalPort();
//			srv.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return open;
//	}

	/**
	 * Attempts opening the requested port.  If that port is not available, it attempts to open
	 * the next one.  There is no lock placed on the port number returned.  It may be stolen
	 * by a competing process.
	 * @param start
	 * @return open
	 * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
	 */
	public static int getOpenPort(int start){
		int open = -1;
		boolean portFound = false;

		//Ports 0-1024 are reserved
		if (start < 1025 || start > 65536)
			start = 1025;

		ServerSocket srv = null;
		while(!portFound && start <= 65536){
			try {
				srv = new ServerSocket(start);
				open = srv.getLocalPort();
				try {
					srv.close();
					portFound = true;
				} catch (IOException e) {
					e.printStackTrace();
					start++;
				}
			} catch (IOException e1) {
				start++;
			}  
		}
		return open;
	}
	/**
	 * Private recursive helper method for the public {@link #writePersistent()} method.
	 * @param prefix A prefix to add to this 
	 * @param obj The object to write out.
	 * @see #writePersistent()
	 */
	public static Status writePersistentToProperties(String prefix, Object obj, CASAFilePropertiesMap properties) {
		if (properties==null) return new Status(-1, "properties parameter is null.");
		if (prefix==null) prefix = "";
		int i = 0;
		StringBuilder unread = new StringBuilder();
		for (Field f: AnnotationUtil.getAnnotatedFields(obj.getClass(), CasaPersistent.class)) {
			Class<?> t = f.getType();
			f.setAccessible(true);
			try {
				if (t.isPrimitive()) {
					if (t.equals(boolean.class)) {
						properties.setBoolean(prefix+f.getName(), f.getBoolean(obj));
					} else if (t.equals(char.class)) {
						properties.setChar(prefix+f.getName(), f.getChar(obj));
					} else if (t.equals(byte.class)) {
						properties.setByte(prefix+f.getName(), f.getByte(obj));
					} else if (t.equals(short.class)) {
						properties.setShort(prefix+f.getName(), f.getShort(obj));
					} else if (t.equals(int.class)) {
						properties.setInteger(prefix+f.getName(), f.getInt(obj));
					} else if (t.equals(long.class)) {
						properties.setLong(prefix+f.getName(), f.getLong(obj));
					} else if (t.equals(float.class)) {
						properties.setFloat(prefix+f.getName(), f.getFloat(obj));
					} else if (t.equals(double.class)) {
						properties.setDouble(prefix+f.getName(), f.getDouble(obj));
					}
				}
				else { // non-primitive
					boolean internal = false;
					for (Field f2: f.getType().getDeclaredFields()) {
						if (f2.isAnnotationPresent(CasaPersistent.class)) {
							internal = true;
							break;
						}
					}
					if (internal) {
						writePersistentToProperties(f.getName()+".", f.get(obj), properties);
					}
					else {
						properties.setString(prefix+f.getName(), serialize(f.get(obj)));
					}
				}
			} catch (Throwable e) {
				if (unread.length()>0) unread.append(", ");
				unread.append(f.getName());
			} 
			i++;
		}
		if (unread.length()>0)
			return new Status(1,"writePersistentToProperties(): Failed to write the following fields: "+unread);
		else
			return new Status(0);

	}
	/**
	 * Reads the persistent data from the data structures marked with the CasaPersistent annotation.
	 * This method is static because this allows it to be used by the LAC class as well.
	 * @param prefix
	 * @param obj
	 * @param properties
	 */
	public static Status readPersistentFromProperties(String prefix, Object obj, CASAFilePropertiesMap properties) {
		if (prefix==null) prefix = "";
		int i = 0;
		StringBuilder unread = new StringBuilder();
		for (Field f: AnnotationUtil.getAnnotatedFields(obj.getClass(), CasaPersistent.class)) {
			Class<?> t = f.getType();
			f.setAccessible(true);
			try {
				if (t.isPrimitive()) {
					if (t.equals(boolean.class)) {
						f.setBoolean(obj, properties.getBoolean(prefix+f.getName()));
					} else if (t.equals(char.class)) {
						f.setChar(obj, properties.getChar(prefix+f.getName()));
					} else if (t.equals(byte.class)) {
						f.setByte(obj, properties.getByte(prefix+f.getName()));
					} else if (t.equals(short.class)) {
						f.setShort(obj, properties.getShort(prefix+f.getName()));
					} else if (t.equals(int.class)) {
						f.setInt(obj, properties.getInteger(prefix+f.getName()));
					} else if (t.equals(long.class)) {
						f.setLong(obj, properties.getLong(prefix+f.getName()));
					} else if (t.equals(float.class)) {
						f.setFloat(obj, properties.getFloat(prefix+f.getName()));
					} else if (t.equals(double.class)) {
						f.setDouble(obj, properties.getDouble(prefix+f.getName()));
					}
				}
				else { // non-primitive
					boolean internal = false;
					for (Field f2: f.getType().getDeclaredFields()) {
						if (f2.isAnnotationPresent(CasaPersistent.class)) {
							internal = true;
							break;
						}
					}
					if (internal) {
						readPersistentFromProperties(f.getName()+".", f.get(obj), properties);
					}
					else {
						String data = properties.getString(prefix+f.getName());
						Object obj2 = unserialize(data, null);
						f.set(obj, obj2);
					}
				}
			} catch (Exception e) {
				if (unread.length()>0) unread.append(", ");
				unread.append(f.getName());
			} 
			i++;
		}
		if (unread.length()>0)
			return new Status(1,"readPersistentFromProperties(): Failed to read the following fields: "+unread);
		else
			return new Status(0);
	}

	/**
	 * Returns a list of all the proper subclasses of the parameter class. <br>
	 * Caution 1: This can take a very long time -- in the order of seconds.<br>
	 * Caution 2: This will ONLY return the classes that are LOADED: if the class just hasn't been loaded yet,
	 * it will not be counted.
	 * @param superclass The class to return all the subclasses of.
	 * @return All the proper subclasses of the parameter class.
	 */
	public static List<Class<?>> findSubclassesOf(Class<?> superclass) {
		List<Class<?>> ret = new LinkedList<Class<?>>();
		for (Package pack: Package.getPackages()) {
			ret.addAll(findSubClassesOf(pack.getName(), superclass));
		}
		return ret;
	}

	/**
	 * Returns a list of all the proper subclasses of the parameter class with the parameter package. <br>
	 * Caution 1: This can take a very long time -- in the order of seconds.<br>
	 * Caution 2: This will ONLY return the classes that are LOADED: if the class just hasn't been loaded yet,
	 * it will not be counted.
	 * @param pckgname The name of package to search
	 * @param superclass The class to return all the subclasses of.
	 * @return All the proper subclasses of the parameter class with the package.
	 */
	public static List<Class<?>> findSubClassesOf(String pckgname, Class<?> superclass) {
		List<Class<?>> ret = new LinkedList<Class<?>>();
		String name = new String(pckgname);
		if (!name.startsWith("/")) {
			name = "/" + name;
		}        
		name = name.replace('.','/');

		// Get a File object for the package
		URL url = superclass.getResource(name);
		File directory = url==null?null:new File(url.getFile());
		if (directory!=null && directory.exists()) {
			// Get the list of the files contained in the package
			for (String filename: directory.list()) { //int i=0;i<files.length;i++) {
				// we are only interested in .class files
				if (filename.endsWith(".class")) {
					// removes the .class extension
					String classname = filename.substring(0,filename.length()-6);
					try {
						Class<?> clas = Class.forName(pckgname+"."+classname);
						if (clas!=superclass && superclass.isAssignableFrom(clas)) {
							ret.add(clas);
						}
					} catch (ClassNotFoundException cnfex) {
						System.err.println(cnfex);
					}
				}
			}
		}
		return ret;
	}

	@Test
	public void test() {
		System.out.println(findSubclassesOf(MLMessage.class));
	}

	/**
	 * @param bb
	 * @param encoding The encoding (eg: "UTF-8").
	 * @return The String version of <em>bb</em> encoded in <em>encoding</em> or (if that encoding fails or <em>encoding</em> is null) the system default encoding.
	 */
	public static String bytesToString(byte[] bb, String encoding) {
		try {
			return encoding==null ? new String(bb) : new String(bb, encoding);
		} catch (UnsupportedEncodingException e) {
			return new String(bb);
		}
	}

	/**
		 * Utility method to display a string with all escape characters visible.  Useful
		 * to show the buffer, which could have binary data in it.
		 * @param s The string that may contain non-printable characters.
		 * @return A version of <em>s</em> with all the non-printables shown as escape sequences.
		 */
		public static String makeUnprintablesVisible(String s){
	    StringBuilder sb = new StringBuilder();
	    for (int i=0; i<s.length(); i++) {
	    	char c = s.charAt(i);
	    	if (c>=0)
	        switch (c){
	            case '\n': sb.append("\\n"); break;
	            case '\t': sb.append("\\t"); break;
	            case '\r': sb.append("\\r"); break;
	            case '\b': sb.append("\\b"); break;
	            case '\f': sb.append("\\f"); break;
	            case '\\': sb.append("\\\\"); break;
	            case 0:
	            case 1: 
	            case 2: 
	            case 3: 
	            case 4: 
	            case 5: 
	            case 6: 
	            case 7: 
	            case 11: 
	            case 14: 
	            case 15: 
	            case 16: 
	            case 17: 
	            case 18: 
	            case 19: 
	            case 20:
	            case 21: 
	            case 22: 
	            case 23: 
	            case 24: 
	            case 25: 
	            case 26: 
	            case 27: 
	            case 28: 
	            case 29:
	            case 30:
	            case 31:
	            case 127:
	            	String hex = Integer.toString(c,16);
	            	hex = padLeftTo(hex, '0', 5-hex.length());
	            	sb.append("\\u").append(hex);
	            default: sb.append(c);
	        }
	    	else {
	    		int x = (int)(-c);
	      	String hex = Integer.toString(x,16);
	      	hex = padLeftTo(hex, '0', 5-hex.length());
	      	sb.append("\\u").append(hex);
	    	}
	    }
	    return sb.toString();
	}

	public static Ontology findOntology(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
		Ontology ont = null;
		try {
		  if (params.containsKey("ONTOLOGY")) 
		  	//ont = getOntology((String)params.getJavaObject("ONTOLOGY"));
	  		ont = (Ontology)callMethod(agent==null?OWLOntology.class:Class.forName(agent.getOntologyEngine()), "getOntology", null, new Class<?>[]{String.class}, new Object[]{(String)params.getJavaObject("ONTOLOGY")});
		  if (ont == null) 
		  	ont = (Ontology)casa.abcl.Lisp.lookupAsJavaObject(env, "ontology");
			if (ont==null) {
				if (agent==null) agent = TransientAgent.getAgentForThread();
				if (agent!=null) ont = (Ontology)agent.getOntology();
			}
			if (ont == null) printui("Can't find ontology to apply operation to", ui, params, agent);
			return ont;						
		} catch (Throwable e1) {
			printui("Can't find ontology to apply operation to: "+e1+": "+e1.getMessage(), ui, params, agent);
			return null;
		}
	
	}

	public static String printui(String s, AgentUI ui, @SuppressWarnings("unused") ParamsMap params, TransientAgent agent) {
		if (ui!=null && agent != null)
			ui.println("; warning/error: "+agent.println("error",s));
		else if (agent != null)
			System.out.println("; warning/error: "+agent.println("error",s));
		else 
			System.out.println("; warning/error: "+s);
		return s;
	}

	static TreeMap<String,InetAddress[]> cachedAddresses = new TreeMap<String,InetAddress[]>();

	private static InetAddress[] getInetAddressesForPrimitive(InetAddress addr) {
		try {
			String name = addr.getHostName();
			InetAddress[] addresses = InetAddress.getAllByName(name);
			Vector<InetAddress> v = new Vector<InetAddress>();
			for (InetAddress a: addresses) {
				if (a instanceof Inet4Address) 
					v.add(a);
			}
			InetAddress ret[] = new InetAddress[v.size()];
			int i = 0;
			for (InetAddress s: v) {
				ret[i++] = s;
			}
			return ret;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return new InetAddress[0];
		}
	}

	public static InetAddress[] getInetAddressesFor(InetAddress addr) {
		String name = addr.getHostName();

		// get info from cache if possible
		InetAddress ret[] = cachedAddresses.get(name);
		if (ret!=null)
			return ret;

		//check to see if this is one of the local addresses
		if (isMyInetAddress(addr))
			return getMyInetAddresses();

		ret = getInetAddressesForPrimitive(addr);

		if (isMyHostName(name)) {
			for (String n: getMyHostNames()) 
				cachedAddresses.put(n, ret);
		}
		else
			cachedAddresses.put(name, ret);

		return ret;
	}

	/**
	 * Used to cache the return value from {@link #getMyInetAddresses()}.  Set to null to
	 * get the addresses recaluclated.
	 */
	private static InetAddress[] myInetAddresses = null;
	private static String[] myHostNames = null;

	/**
	 * Resets the caches for {@link #getMyAddresses()} and {@link #getMyInetAddresses()}.  Call this 
	 * method to cause the recalculation of the InetAddresses on the next call to these methods. 
	 */
	public static void resetInetAddresses() {
		myInetAddresses = null;
		myAddresses = null;
		myHostNames = null;
	}
	
	private static void updateHostNames(InetAddress[] addresses) {
		TreeSet<String> set = new TreeSet<String>();
		for (InetAddress a: addresses) {
			set.add(a.getHostName());
		}
		myHostNames = new String[set.size()];
		int i = 0;
		for (String n: set) {
			myHostNames[i++] = n;
		}
	}
	
	public static boolean isMyHostName(String hostName) {
		if (myHostNames==null) {
			getMyInetAddresses();
		}
		for (String h:myHostNames) {
			if (h.equals(hostName))
				return true;
		}
		return false;
	}

	public static String[] getMyHostNames() {
		if (myHostNames==null) {
			getMyInetAddresses();
		}
		return myHostNames;
	}

	/**
	 * 
	 * @param addr
	 * @return true iff the parameter addr is one of local hosts addresses.
	 */
	public static boolean isMyInetAddress(InetAddress addr) {
		for (InetAddress a: getMyInetAddresses()) {
			if (a.equals(addr))
				return true;
		}
		return false;
	}
	/** 
	 * @return all the computer's Inet4 addresses, do this using BOTH
	 * {@link InetAddress#getLocalHost() and the local {@link #getLocalHost()}.
	 */
	public static InetAddress[] getMyInetAddresses() {
		if (myInetAddresses!=null)
			return myInetAddresses;

		try {
			InetAddress systemHost = InetAddress.getLocalHost();
			InetAddress[] system = CASAUtil.getInetAddressesForPrimitive(systemHost);
			InetAddress casaHost = getLocalHost();
			if (casaHost==null || systemHost.equals(casaHost)) {
				myInetAddresses = system;
				updateHostNames(myInetAddresses);
				return system;
			}
			InetAddress[] casa = CASAUtil.getInetAddressesForPrimitive(casaHost);
			Vector<InetAddress> v = new Vector<InetAddress>();

			// merge the two arrays
			for (InetAddress a: system)
				v.add(a);
			for (InetAddress a: casa)
				if (!v.contains(a))
					v.add(a);

			//copy the merged set into an array
			InetAddress[] ret = new InetAddress[v.size()];
			int i = 0;
			for (InetAddress a: v) {
				ret[i++] = a;
			}
			myInetAddresses = ret;
			updateHostNames(myInetAddresses);
			return ret;
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new InetAddress[0];
	}


	public static String[] getAddressesFor(InetAddress addr) {
		InetAddress[] addresses = CASAUtil.getInetAddressesFor(addr);
		String ret[] = new String[addresses.length];
		int i = 0;
		for (InetAddress s: addresses) {
			ret[i++] = s.getHostAddress();
		}
		return ret;
	}

	/**
	 * Used to cache the return value from {@link #getMyAddresses()}.  Set to null to
	 * get the addresses recaluclated.
	 */
	private static String[] myAddresses = null;

	/** 
	 * @return all the computer's Inet4 addresses as a string array.
	 */
	public static String[] getMyAddresses() {
		if (myAddresses!=null)
			return myAddresses;
		InetAddress[] addresses = CASAUtil.getMyInetAddresses();
		String ret[] = new String[addresses.length];
		int i = 0;
		for (InetAddress s: addresses) {
			ret[i++] = s.getHostAddress();
		}
		myAddresses = ret;
		return ret;
	}

  /**
   * Returns the correct outgoing IP address for network associated with <em>remoteAddr</em>. 
   * The local machine may have several networkInterfaces, and this will determine which
   * one of them to use. 
   * @param remoteAddr
   * @param defaultLocal the default to return if none is found.
   * @return
   * @throws Exception
   */
	public static InetAddress getNetwork(InetAddress remoteAddr, InetAddress defaultLocal) {
		byte[] remote = remoteAddr.getAddress(); //get the remote address as bytes
		InetAddress[] myAddresses = CASAUtil.getMyInetAddresses();
		top:
			for (InetAddress myAddr: myAddresses) { //for all local addresses
				byte[] local = myAddr.getAddress(); //get the local address as bytes

				NetworkInterface networkInterface = null;
				try {
					networkInterface = NetworkInterface.getByInetAddress(myAddr);
				} catch (SocketException e) {
				}
				if (networkInterface!=null) {
					network:
					for (InterfaceAddress address: networkInterface.getInterfaceAddresses()) {
						//build the mask
						short len = address.getNetworkPrefixLength();
						if (len>24)
							continue network;
						byte[] mask = new byte[]{0,0,0,0};
						for (int i=0; len>0; i++, len--) {
							mask[i/8] >>= 1;
							mask[i/8] |= 0x80;
						}

						for (int i = 0; i < remote.length; i++)
							if ((remote[i] & mask[i]) != (local[i] & mask[i]))
								continue top;

						return myAddr;
					}
				}
			}
		return defaultLocal;
	}

	/**
	 * Search for a class named target+suffix that is a subclass of superclass.  Regardless of weather this class is found, return
	 * a map of all prefixes-to-suffixes of classes that are subclasses of superclass.  In the event that a match is found in map that isn't
	 * a subtype, it is eliminated and the search is performed as it it wasn't there.
	 * @param map If there is already a map, this should be filled, which acts as a cache -- eliminating the search time.  If this null the preferences and possibly run-time sub
	 * classes will be searched.
	 * @param superclass The class to look for subclasses of.  This may NOT be null;
	 * @param target The prefix part of the class name we are looking for, which is also used as the index to this class in the return Map. If
	 * this is null, an extensive search will always be made for all subclasses matching the suffix part. 
	 * @param suffix The suffice pat the class name (the class name we looking for is prefix+suffix).  If this is null,
	 * it will be taken as the name of the superclass.
	 * @return A Map from prefix to classes for all the subclasses of the superclass.  This may or may not contain the target.
	 */
	public static Map<String, Class<?>> fillSubclasses(Map<String, Class<?>> map, Class<?> superclass, String target, String suffix) {
		assert superclass!=null;
		Class<?> cls = target==null?null:(map==null?null:map.get(target));
		if (cls!=null) {
			if (superclass.isAssignableFrom(cls)) return map; // if we already have the target in the map, there's nothing to do.
			else map.remove(target); // we map the target, but it isn't a subclass, so get rid of it so we can search again.
		}
		if (suffix==null) suffix = superclass.getCanonicalName();
		String preferenceName = superclass.getCanonicalName();
		//  	preferenceName = preferenceName.substring(preferenceName.lastIndexOf('.'+1),preferenceName.length());
		preferenceName += "-subclasses";
		if (map == null) {
			map = new TreeMap<String, Class<?>>();	
			Map<String,String> prefs = CASA.getPreference(preferenceName, (Map<String,String>)null, 0);
			if (prefs!=null) { // we have preferences cached in our preferences
				for (String key: prefs.keySet()) {
					if (key.length()>0) {
						try {
							map.put(key, Class.forName(prefs.get(key)));
						} catch (ClassNotFoundException e) {
							// don't worry about it.
						}
					}
				}
			}
		}
		if (target==null || !map.containsKey(target)) {
			List<Class<?>> subclasses = findSubclassesOf(superclass);
			Map<String,String> prefs = new TreeMap<String,String>();
			for (Class<?> c: subclasses) {
				String name = c.getCanonicalName();
				name = name.substring(name.lastIndexOf('.')+1,name.length());
				if (name.length()>0) {
					String key = name.substring(0,name.indexOf(suffix));
					map.put(key,(Class<?>)c);
					prefs.put(key, c.getCanonicalName());
				}
			}
			CASA.putPreference(preferenceName, prefs, 0);
		}
		return map;
	}

	/**
	 * Wait <em>time</em> milliseconds ignoring interrupts.
	 * @param time The time to wait.
	 * @param code If an intterrupt happens during the sleep,
	 *  {@link Runnable1#run(Object)} on <em>code</em> is called.  If the run() returns
	 *  true, this method aborts the sleep and returns false.  This parameter may be null,
	 *  in which case, this method sleeps the entire time. 
	 * @return True if this method waited the full <em>time</em>; false if run() is executed
	 *  and returns true.
	 */
	public static boolean sleepIgnoringInterrupts(long time, Runnable1<InterruptedException, Boolean> code) {
		long doneTime = System.currentTimeMillis()+time;
		while (System.currentTimeMillis()<doneTime) {
			try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
				if (code!=null) {
					if (code.run(e)) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * Attempt to find an in-scope agent. 
	 * <ol>
	 * <li> If the current thread is an agent, return this thread (casted to an {@link AbstractProcess}
	 * <li> Else if the current thread is a {@link Subthread} created with {@link AbstractProcess#makeSubthread(Runnable)}
	 * or {@link AbstractProcess#makeSubthread(Runnable, String)} then return the agent recorded in the SubThread.
	 * <li> Else if the current thread is in a threadGroup with an agent, then try to find the agent who created the threadGroup and return that agent.
	 * <li> Else if the current thread has a field "agent" (typically created using {@link ThreadLocal}) of type
	 * {@link AbstractProcess} then return this agent.
	 * <li> Else return null.
	 * </ol>
	 * @return as above.
	 */
	public static AbstractProcess getAbstractProcessInScopeSilent() {
		Thread thread = Thread.currentThread();
		if (thread instanceof AbstractProcess) // if we're in the agent's thread directly 
			return (AbstractProcess)thread;
		if (thread instanceof AbstractProcess.Subthread)  // if we're in a thread created by AbstractProcess.makeSubthread() 
			return ((AbstractProcess.Subthread)thread).getAgent();
		ThreadGroup threadGroup = thread.getThreadGroup();
		AbstractProcess agent;
		if (threadGroup instanceof AgentThreadGroup && (agent=((AgentThreadGroup)threadGroup).getAgent())!=null) {
			return agent;
		}
		try { // see if we have a ThreadLocal variable for the agent
			Field feild = thread.getClass().getField("agent");
			Object o = feild.get(thread);
			if (o!=null && o instanceof AbstractProcess)
				return (AbstractProcess) o;
		}
		catch (Throwable e) { 
			//we don't bother if this failed.
		}
		return null;
	}

	/**
	 * Attempt to find an in-scope agent. If it's not found, logs a warning message to Sysout.
	 * @return The found agent.
	 * @see CASAUtil#getAbstractProcessInScopeSilent()
	 */
	public static AbstractProcess getAbstractProcessInScope() {
		AbstractProcess ret = getAbstractProcessInScopeSilent();
		if (ret==null)
			Trace.logToSysout("warning", "CASAUtil.getAbstractProcessInScope() returned null", null, Trace.OPT_FORCE_STACK_TRACE);
		return ret;
	}

	/**
	 * Logs the message if tag is in effect to the in-scope agent's log files, windows, etc.
	 * If no agent can be found in scope, does the best-effort to log to System output.
	 * @param tag A trace tag: if an agent can't be found in scope this is ignored
	 * @param message The message to log
	 * @param ex If ex is not null, the stack dump is appended to the message.
	 * @param printStackTrace set to true to print a stack trace (note that if <em>ex</em> is not null, the trace will be printed anyway).
	 * @return The String (minus the generated log header) printed to the log
	 * @see Trace#log(String, String)
	 * @see #getAbstractProcessInScope()
	 * @deprecated Use {@link Trace#log(String,String,Throwable,int)} instead
	 */
	@Deprecated
	public static String log(String tag, String message, Throwable ex, boolean printStackTrace) {
		return Trace.log(tag, message, ex, printStackTrace?0:Trace.OPT_SUPRESS_STACK_TRACE);
	}

	@Deprecated
	static String makeLogHeader(String tag) {
		return Trace.getHeader(tag, "unknown-agent");
	}

  /**
   * Put the argument strings into a cons list.
   * @param strings the strings that will form the atoms in the cons list.
   * @return The cons list.
   */
	public static LispObject toCons(String... strings) { 
	LispObject list = org.armedbear.lisp.Lisp.NIL;
	org.armedbear.lisp.Cons ptr=null, last;
	for (String o: strings) {
		if (ptr==null) list = ptr = new org.armedbear.lisp.Cons(new SimpleString(o));
		else {
		  last = ptr;
			ptr = new org.armedbear.lisp.Cons(new SimpleString(o));
			last.cdr = ptr;
		}
	}
	return list;
  }
  
	/**
   * Calls a method by name alone, without requiring the compiler to know about it.  
   * This is particularly useful for platform-specific calls such as those of the apple com.apple.mrj.MRJFileUtils class.
   * For example:
   * <pre>
   *   try {
	 *     CASAUtil.callMethod(Class.forName("com.apple.mrj.MRJFileUtils"), 
	 *                         "openURL", 
	 *                         null, 
	 *                         new Class[]{String.class}, 
	 *                         new Object[]{url});
	 *   }
	 *   catch(Throwable ex) {}
   * </pre>
	 * @param cls The class of target object.  If <em>cls</em> is null, but <em>target</em> is specified, then <em>cls</em> is assumed to be the runtime type of <em>target</em>.
	 * @param methodName The name of the method to invoke.
	 * @param target The object on which to call this method. May be null if this is a <em>methodName</em> is the name of a static method.
	 * @param paramTypes The types of the arguments.
	 * @param params The arguments
	 * @return The result of invoking the method with the specified parameters.
   * @throws NoSuchMethodException
   * @throws InvocationTargetException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws NullPointerException
   * @throws SecurityException
   * @throws ExceptionInInitializerError
	 */
	public static Object callMethod(Class<?> cls, String methodName, Object target, Class<?>[] paramTypes, Object[] params) 
  		throws NoSuchMethodException, InvocationTargetException, IllegalArgumentException, IllegalAccessException, NullPointerException, SecurityException, ExceptionInInitializerError {
		if (cls==null && target!=null)
			cls = target.getClass();
		Method method = cls.getMethod(methodName, paramTypes);
		Object ret = method.invoke(target, params);
		return ret;
	}
	
	/**
	 * Run an agent of type <em>theClass</em> named <em>agentName</em> on port <em>port</em>, optionally
	 * executing <em>code</em> once the agent is started up.  This can be really useful for testing new
	 * agents.  
	 * @param theClass The class of the agent to start.
	 * @param agentName The name of the new agent.
	 * @param port The port for the new agent.
	 * @param code The code to execute once the agent starts up -- it will be passed the actual agent.
	 * @param argPairs key/value pairs of arguments to pass in the agent's constructor (must be even).
	 * @return the started agent, or null if the agent couldn't be started.
	 * when it is called; may be null.
	 */
	public static TransientAgent startAnAgent(Class<?> theClass, String agentName, int port, Runnable1<TransientAgent,Void> code, String... argPairs) {
		BufferedAgentUI buf = new BufferedAgentUI();
		if (argPairs==null || argPairs.length<1) {
			argPairs = new String[] {"PROCESS", "CURRENT", "TRACE", "10", "TRACETAGS", "info5,warning,msg,iRobot,-boundSymbols,-policies9,-commitments,-eventqueue,-conversations"};
		}
		TransientAgent agent = casa.CASA.startAgent(buf, theClass, agentName, port, argPairs);
		if (agent==null) {
			Trace.log("error",buf.result());
			return null;
		}
		while (!agent.isInitialized()) { //wait for the agent to startup
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		try { // give the agent some time to get started
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}
		if (code!=null) {
				code.run(agent);
		}
		return agent;
	}
	
	/**
	 * @return the runtime PID as a string, or best attempt.  If we can't get to the actual PID, then return the name of the JVM.
	 */
	public static String getPID() {
		java.lang.management.RuntimeMXBean runtime;
		java.lang.reflect.Field jvm;
		runtime = java.lang.management.ManagementFactory.getRuntimeMXBean();
		try {
			jvm = runtime.getClass().getDeclaredField("jvm");
			jvm.setAccessible(true);
			sun.management.VMManagement mgmt =  
					(sun.management.VMManagement) jvm.get(runtime);
			java.lang.reflect.Method pid_method =  
					mgmt.getClass().getDeclaredMethod("getProcessId");
			pid_method.setAccessible(true);

			int pid = (Integer) pid_method.invoke(mgmt);
			return Integer.toString(pid);
		} catch (Throwable e) {
			return runtime.getName();
		}
	}

	
}
