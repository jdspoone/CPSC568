package casa.util;

/**
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary.
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  The
 * Knowledge Science Group makes no representations about the suitability of
 * this software for any purpose.  It is provided "as is" without express or
 * implied warranty.</p>
 *
 * Defines the basic functionality for the {@link Trace} class.
 * @author <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
public interface TraceInterface {

	/**
	 * Adds a comma-delimited list of trace tags to a string.  If any of tags are
	 * immediately preceded with a '-', those tags are removed instead.  Eg:
	 * "a,-b,c,-d" will add a and c, and remove b and d.  Whitespace before and
	 * after each tag is ignored.  A tag can be any identifier-like string.
	 * You can also append a single digit (0-9) to indicate a higher level
	 * of detail (default = 0). 
	 * @param tags a comma-delimited list of trace tags
	 * @return the number of tags processed (would be 4 in the above example)
	 */
	public abstract int addTraceTags(String tags);

	/**
	 * Removes the tags in the comma-delimited streams specified in <em>tags</em>.
	 * Note that removeTraceTags("x,y") is equivalent to addTraceTags("-x,-y").  Unlike addTraceTags(String),
	 * the tag identifiers must appear alone, without the prefix "-" or the digit suffix.
	 * @param tags The tags to be removed.
	 * @see #setTraceTags(String)
	 */
	public abstract int removeTraceTags(String tags);

	/**
	 * Works the same as addTraceTags, but removes all previous tags beforehand.
	 * @param tags A command-delimited list of tags. White space is ignored.
	 * @return The number of tags inserted
	 */
	public abstract int setTraceTags(String tags);

	/** 
	 * @return The current set of active trace tags as a comma-delimited String.  Note that the "-" prefix
	 * does not appear, but the digit suffix may.
	 */
	public abstract String getTraceTags();

	/**
	 * Determine if the specified trace tag spec will print, taking
	 * into account the last digit appended on the tag if it's there.
	 * @param tag
	 * @return true if traceTags contains tag, false otherwise.
	 */
	public abstract boolean isLoggingTag(String tag);

	/**
	 * Equivalent to {@link #println(String, String, Throwable, int) println(tag, string, null, 0)}
	 * @param traceTag This tag MUST be present in set of traceTags for anything to be printed/logged.
	 * @param txt The message to be printed/logged.
	 * @return The parameter <em>tag</em>
	 * @see #println(String, String, Throwable, int)
	 */
	public abstract String println(String tag, String string);

	/**
	 * Equivalent to {@link #println(String, String, Throwable, int) println(tag, string, null, flag)}
	 * @param traceTag This tag MUST be present in set of traceTags for anything to be printed/logged.
	 * @param txt The message to be printed/logged.
	 * @param flags The options flags; use bitwise disjunct ("|") to combine options - {@link #OPT_SUPPRESS_AGENT_LOG}, {@link #OPT_COPY_TO_SYSERR}, {@link #OPT_COPY_TO_SYSOUT}, {@link #OPT_FORCE_STACK_TRACE}, {@link #OPT_INCLUDE_CODE_LINE_NUMBER}, {@link #OPT_SUPPRESS_HEADER_ON_SYSOUT}, {@link #OPT_SUPRESS_STACK_TRACE}.
	 * @return The parameter <em>tag</em>
	 * @see #println(String, String, Throwable, int)
	 */
	public abstract String println(String tag, String string, int flags);

	/**
	 * Equivalent to {@link #println(String, String, Throwable, int) println(tag, string, ex, 0)}
	 * @param traceTag This tag MUST be present in set of traceTags for anything to be printed/logged.
	 * @param txt The message to be printed/logged.
	 * @param ex If this is non-null, the stack trace will be appended to the message
	 * @return The parameter <em>tag</em>
	 * @see #println(String, String, Throwable, int)
	 */
	public abstract String println(String traceTag, String txt, Throwable ex);

	/**
	 * This is the println method that defines all the other println methods.
	 * The following is printed/logged:
	 * <ol>
	 * <li> if the tag is error the message will be "bracketed" by a long string of ">"'s, 
	 * if warning a medium string of ">"'s, 
	 * if the {@link #OPT_INCLUDE_CODE_LINE_NUMBER} is in <em>flags</em> then so short string of ">"'s.
	 * In any of these cases, the ">"'s is followed by the guess of the calling source file and line number.
	 * <li> the record header.
	 * <li> the prefix, if it is set.
	 * <li> a stack trace if <em>ex</em> is non-null and {@link #OPT_SUPRESS_STACK_TRACE} is not set, or {@link #OPT_FORCE_STACK_TRACE} is set.
	 * <li> a matching closing string of "<"'s if we are bracketing with ">"'s.
	 * </ol>
	 * The header is in the following format with no spaces:
	 * <pre>
	 * [* <b>timestamp</b> : <em>agentName</em> : <em>tag</em> : <b>thread-name</b> *] 
	 * </pre>
	 * @param traceTag This tag MUST be present in set of traceTags for anything to be printed/logged.
	 * @param txt The message to be printed/logged.
	 * @param ex If this is non-null, the stack trace will be appended to the message
	 * @param flags The options flags; use bitwise disjunct ("|") to combine options - {@link #OPT_SUPPRESS_AGENT_LOG}, {@link #OPT_COPY_TO_SYSERR}, {@link #OPT_COPY_TO_SYSOUT}, {@link #OPT_FORCE_STACK_TRACE}, {@link #OPT_INCLUDE_CODE_LINE_NUMBER}, {@link #OPT_SUPPRESS_HEADER_ON_SYSOUT}, {@link #OPT_SUPRESS_STACK_TRACE}.
	 * @return The parameter <em>txt</em>
	 */
	public abstract String println(String traceTag, String txt, Throwable ex, int flags);

	/**
	 * Turns all ALL the trace tags.
	 */
	public void setAllTraceTags();

	/**
	 * Turns off ALL the trace tags.
	 */
	public void clearAllTraceTags();

}