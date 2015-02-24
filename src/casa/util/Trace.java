package casa.util;

import casa.AbstractProcess;
import casa.CasaObservable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.ksg.casa.CASA;

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
 * This class is responsible for CASA's trace mechanism which uses String identifiers called 
 * "trace tags" to control whether a particular "topic" is recorded.  Trace messages on 
 * particular tags (topics) are divided into 10 levels by appending a trace tag identifier with
 * a digit (0 being the most basic, and 9 being the highest level of detail).  Trace messages
 * are normally logged to a file as (potentially multi-line) records.  For example:
 * <pre>
 * >>>>>>>>>>>>>>>>>>>>>>>>> casa.abcl.Lisp.signalError(Lisp.java:349)
 * [*14.39.12.788:CASAProcess1403296740779:error:main*] Lisp.abclEval: 
 * (load "scripts/sc2.lisp") :  
 * #<CONDITION {7D504E7B}>: Unexpected non-ControlTransfer exception during execution of lisp operator AGENT.TELL:
 * java.lang.NullPointerException
 * 	at casa.TransientAgent$14.execute(TransientAgent.java:8412)
 * 	at casa.abcl.CasaLispOperator.execute(CasaLispOperator.java:603)
 * 	at org.armedbear.lisp.Lisp.eval(Lisp.java:495)
 * 	at org.armedbear.lisp.Lisp.progn(Lisp.java:674)
 * ...
 * :
 * casa.abcl.LispException
 * <<<<<<<<<<<<<<<<<<<<<<<<<
 * </pre>
 * In addition, a memory-resident trace record may be kept, and agent observers will be notified (if the trace
 * is on behalf of an agent).  <p>
 * Most often, users will use the println() family o methods.  However if there is no agent in scope or otherwise
 * no Trace object in scope, users may equivalently use the log() family of methods (with the same parameters as
 * the println() methods) to access the process-wide trace object.
 * <p> For added control, several flags may be used with the println() an log() methods:
 * <ul>
 * <li> {@link #OPT_COPY_TO_SYSERR}
 * <li> {@link #OPT_COPY_TO_SYSOUT}
 * <li> {@link #OPT_FORCE_STACK_TRACE}
 * <li> {@link #OPT_INCLUDE_CODE_LINE_NUMBER}
 * <li> {@link #OPT_SUPPRESS_AGENT_LOG}
 * <li> {@link #OPT_SUPPRESS_HEADER_ON_SYSOUT}
 * <li> {@link #OPT_SUPRESS_STACK_TRACE}
 * </ul>
 *
 * @author <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
public class Trace implements TraceInterface { 

	/** Default for print methods that do not include an Exception parameter or method calls that have a null Exception parameter. */
	public  static final int    OPT_SUPRESS_STACK_TRACE       = 0x1; 
	/** Default for print methods that include an Exception parameter where the Exception is non-null. Allowable, but expensive for other operations. */
	public  static final int    OPT_FORCE_STACK_TRACE         = 0x2; 
	/** Default for error and warning tags. And expensive operation. */
	public  static final int    OPT_INCLUDE_CODE_LINE_NUMBER  = 0x4; 
	/** Only applicable to the process-wide Trace; forces output to sysout rather than trying to find an agent. */
	public  static final int    OPT_SUPPRESS_AGENT_LOG        = 0x8; 
	/** Copies output to sysout. */
	public  static final int    OPT_COPY_TO_SYSOUT		        = 0x10; 
	/** Copies output to syserr. */
	public  static final int    OPT_COPY_TO_SYSERR		        = 0x20; 
	/** Suppresses the header block and &gt;&gt;&gt;&gt; flagging when printing to sysout or syserr */
	public  static final int    OPT_SUPPRESS_HEADER_ON_SYSOUT = 0x40; 
	
	/** 
	 * WARNING: the timestamp format should NOT contain any colons (":"), as that would screw up any code that parses the header
	 * @see #getHeader(String);
	 */
	private static final String LONG_TIMESTAMP_FORMAT   = "HH.mm.ss.SSS";
	/**
	 * The maximum file size (in bytes) for log files. If this threshold is exceeded, the file is zeroed and started over.
	 */
	public  static final int    MAX_FILE_SIZE           = 3000000; //forces delete-and-reset of trace files when it grows beyond this many bytes.
	

	/** For platform-independence it's recommended that line.separator be used */
	private static String LINE_SEPARATOR = System.getProperty("line.separator","\n");

	/** 
	 * WARNING: the timestamp format should NOT contain any colons (":"), as that would screw up any code that parses the header
	 * @see #getHeader(String);
	 */
	private static String dateFormat = LONG_TIMESTAMP_FORMAT;
	
	/** The name of this trace; this is used to name the trace file. */
	private String traceName = "";
	
	/** The internal history buffer; if null, no history will be saved. */
	private StringBuffer history = null;
	
	/** The default maximum size (in bytes) of the internal history to be saved. */
	public static final long maxHistoryDefault = 80000;
	
	/** The maximum size (in bytes) of the internal history to be saved. */
	private long maxHistory = 0; // 0 means there is no history in memory
	
	/** 
	 * Flag to indicate we are logging to a file.
	 * @see #traceName
	 */
	private boolean logToFile = false;
	
	/** The prefix to go after the header and before the actual message in trace records. */
	private String prefix = null;
	
	/** The structure used to save trace tags for quick lookup. */
	private TreeMap<String,Integer> traceTags = new TreeMap<String,Integer>();
	
	/** The agent that "owns" this Trace object.  May be null. */
	private CasaObservable agent;
	
	/** 
	 * The file stream we will be writing to.
	 * @see #logToFile
	 * @see #logFile 
	 */
	private FileWriter fileWriter = null;

	/** The accumulated number of errors seen. */
	private static int errors = 0;

	/** The accumulated number of warnings seen. */
	private static int warnings = 0;
	
	/**
	 * An Trace object used by the static methods to track messages not associated with any particular agent.
	 */
	private static final Trace processWideTrace = new Trace("casaOut"+System.currentTimeMillis()); 
	
	/**
	 * Equivalent to {@link #Trace(CasaObservable, String, long) Trace(null, <em>name</em>, 0)}
	 * @param name The name of this trace; used to specify the trace file name.
	 * If null, will default to the name of the current process.
	 */
	private Trace(String name) {
		this(null, name, 0);
	}

	/**
	 * Equivalent to {@link #Trace(CasaObservable, String, long) Trace(<em>agent</em>, null, 0)}
	 * @param agent The agent that "owns" this Trace object.
	 */
	public Trace(CasaObservable agent) {
		this(agent, null, 0);
		}

	/**
	 * Equivalent to {@link #Trace(CasaObservable, String, long) Trace(<em>agent</em>, <em>name</em>, 0)}
	 * @param agent The agent that "owns" this Trace object.
	 * @param name The name of this trace; used to specify the trace file name.
	 * If null, will default to the agent name or the name of the current process if agent is null.
	 */
	public Trace(CasaObservable agent, String name){ 
		this(agent, name, 0);
		}

	/**
	 * Equivalent to {@link #Trace(CasaObservable, String, long, String) Trace(<em>agent</em>, <em>name</em>, 0, LONG_TIMESTAMP_FORMAT)}
	 * @param agent The agent that "owns" this Trace object.
	 * @param name The name of this trace; used to specify the trace file name.
	 * If null, will default to the agent name or the name of the current process if agent is null.
	 * @param maxTraceHistoryBytes The maximum number of bytes to save in the internal history.
	 * @see #LONG_TIMESTAMP_FORMAT
	 */
	public Trace(CasaObservable agent, String name, long maxTraceHistoryBytes){ 
		this(agent, name, maxTraceHistoryBytes, LONG_TIMESTAMP_FORMAT);
		}

	/**
	 * Creates a new Trace object.  The time stamp and agent are checked for validity (assert).
	 * @param agent The agent that "owns" this Trace object.
	 * @param name The name of this trace; used to specify the trace file name.  
	 * If null, will default to the agent name or the name of the current process if agent is null.
	 * @param maxTraceHistoryBytes The maximum number of bytes to save in the internal history.
	 * @param timeStampFormat The timeStampFormat to use; must not contain any colons (":") as that will mess up header parsing.
	 * Defaults to {@link #LONG_TIMESTAMP_FORMAT} ({@value #LONG_TIMESTAMP_FORMAT}) if this is null.  
	 */
	private Trace(CasaObservable agent, String name, long maxTraceHistoryBytes, String timeStampFormat) {
		assert timeStampFormat.indexOf(':')<0;
		assert name!=null || (agent!=null && (agent instanceof AbstractProcess));
		if((timeStampFormat == null) || (timeStampFormat.length() <= 0))
			dateFormat = LONG_TIMESTAMP_FORMAT;
		else
			dateFormat = timeStampFormat;
		this.agent = agent;
		if (name != null) 
			traceName = name;
		else {
			traceName = ((AbstractProcess)agent).getAgentName(); 
		}
		setHistoryMaxBytes(maxTraceHistoryBytes);
	}
	
	/**
	 * Assures {@link #fileWriter} is closed.
	 * @see java.lang.Object#finalize()
	 */
	@Override
		protected void finalize() throws Throwable {
			if(fileWriter != null)
				fileWriter.close();
			fileWriter = null;
			super.finalize();
		}

	/**
	 * The file we are logging to. 
	 */
	File logFile;
	
	/**
	 * The number of file resets we have done.
	 * @see #MAX_FILE_SIZE
	 */
	int restarts = 0;
	
	/** Holds the startDate as a convenience for file resets.
	 * @see #resetLogFile()
	 */
	String startDate;
	
	/**
	 * Makes this Trace object start/stop logging to a File.  
	 * Will NOT reset the file if this is called with b=true when we are already logging to a file.
	 * @param b True if we want to start logging to a file, false to stop.
	 * @return returns true if we were previously logging to a file, false if we weren't.
	 * @throws IOException
	 */
	public boolean setLogToFile(boolean b) throws IOException {
		boolean ret = logToFile;
		logToFile = b;
		if (!ret && b) { // nolog to log
			resetLogFile();
		}
		else if (ret && !b) {
			if (fileWriter!=null)
				fileWriter.close();
			fileWriter = null;
		}
		return ret;
	}
	
	/**
	 * Starts or restarts (by wiping and re-initializing) the log file.  The file will be named
	 * with the value returned by {@link #getName()} with the extension ".log".  A "standard" header
	 * will be written to the new file.
	 * @throws IOException
	 */
	public void resetLogFile() throws IOException {
		if (fileWriter!=null)
			fileWriter.close();
		logFile = new File(getName() + ".log");
		fileWriter = new FileWriter(logFile, false);
		if (restarts==0)
			startDate = new Date().toString();
		fileWriter.write("*** Started log at " + new Date().toString() + " ***"+LINE_SEPARATOR +
		                 "*** header format: [* <timestamp> : <traceName> : <tagNameAndDigit> : <TreadName> *]"+LINE_SEPARATOR +
		                 "***   where <tracename> is usually the agent name."+LINE_SEPARATOR);
	}

	/**
	 * @return True iff this Trace object is logging to a file.
	 */
	public boolean getLogToFile() {
		return logToFile;
	}

	/**
	 * @return The prefix being used.
	 * @see #setPrefix(String)
	 * @see #print(String, String, Throwable, int)
	 */
	public String getPrefix(){ return prefix;}

	/**
	 * Sets the prefix to be used in records. 
	 * @param newPrefix The new prefix to use.
	 * @return The old prefix.
	 * @see #getPrefix()
	 * @see #print(String, String, Throwable, int)
	 */
	public String setPrefix(String newPrefix){
		String oldPrefix = prefix;
		prefix = newPrefix;
		return oldPrefix;
	}

	/**
	 * Adds a comma-delimited list of trace tags to a string.  If any of tags are
	 * immediately preceded with a '-', those tags are removed instead.  Eg:
	 * "a,-b,c,-d" will add a and c, and remove b and d.  Whitespace before and
	 * after each tag is ignored.  A tag can be any identifier-like string.
	 * You can also append a single digit (0-9) to indicate a higher level
	 * of detail (default = 0). 
	 * @param tags a comma-delimited list of trace tags
	 * @return the number of tags processed (would be 4 in the above example)
	 * @see casa.util.TraceInterface#addTraceTags(java.lang.String)
	 */
	@Override
	public int addTraceTags(String tags) {
		int ret = 0;
		if (tags!=null) {
			String t[] = tags.split(",");
			for (int i = 0; i < t.length; i++) {
				String s = t[i].trim();
				if (s.length()>0) {
					String s2 = getTagString(s);
					if (s2.length()>0) {
						if (s2.charAt(0) == '-')
							traceTags.remove(s2.substring(1));
						else {
							int detail = getTagDetail(s);
							traceTags.put(s2,detail>0?detail:0);
						}
					}
				}
			}
			ret = t.length;
		}
		return ret;
	}

	/**
	 * Removes the tags in the comma-delimited streams specified in <em>tags</em>.
	 * Note that removeTraceTags("x,y") is equivalent to addTraceTags("-x,-y").  Unlike addTraceTags(String),
	 * the tag identifiers must appear alone, without the prefix "-" or the digit suffix.
	 * @param tags The tags to be removed.
	 * @see casa.util.TraceInterface#removeTraceTags(java.lang.String)
	 * @see #setTraceTags(String)
	 */
	@Override
	public int removeTraceTags(String tags) {
		int ret = 0;
		if (tags!=null) {
			String t[] = tags.split(",");
			for (int i = t.length - 1; i >= 0; i--) {
				traceTags.remove(t[i]);
			}
			ret = t.length;
		}
		return ret;
	}

	/**
	 * Works the same as addTraceTags, but removes all previous tags beforehand.
	 * @param tags A command-delimited list of tags. White space is ignored.
	 * @return The number of tags inserted
	 * @see casa.util.TraceInterface#setTraceTags(java.lang.String)
	 */
	@Override
	public int setTraceTags(String tags) {
		traceTags = new TreeMap<String,Integer>();
		return addTraceTags(tags);
	}

	/**
	 * Turns all ALL the trace tags.
	 * @see casa.util.TraceInterface#setAllTraceTags(java.lang.String)
	 */
	@Override
	public void setAllTraceTags() {
		traceTags = new TreeMap<String,Integer>();
	}

	/**
	 * Turns off ALL the trace tags.
	 * @see casa.util.TraceInterface#clearAllTraceTags(java.lang.String)
	 */
	@Override
	public void clearAllTraceTags() {
		traceTags = new TreeMap<String,Integer>();
		setTraceTags("error");
	}

	/** 
	 * @return The current set of active trace tags as a comma-delimited String.  Note that the "-" prefix
	 * does not appear, but the digit suffix may.
	 * @see casa.util.TraceInterface#getTraceTags()
	 */
	@Override
	public String getTraceTags() {
		StringBuffer buf = new StringBuffer();
		for (String key :  traceTags.keySet()) {
			buf.append(key);
			int detail = traceTags.get(key);
			if (detail>0) buf.append(detail);
			buf.append(",");
		}
		if (buf.length()>0) buf.setLength(buf.length()-1);
		return buf.toString();
	}

	/**
	 * Determine if the specified trace tag spec will print, taking
	 * into account the last digit appended on the tag if it's there.
	 * @param tag
	 * @return true if traceTags contains tag, false otherwise.
	 * @see casa.util.TraceInterface#isLoggingTag(java.lang.String)
	 */
	@Override
	public boolean isLoggingTag(String tag) {
		if (tag == null || tag.length() == 0 || tag.equals("error") || traceTags.size()==0)
			return true;
		String tagName = getTagString(tag);
		updateTags(tagName);
		if (traceTags.containsKey(tagName)) {
			if (getTagDetail(tag)<=traceTags.get(tagName))
				return true;
		}
		return false;
	}

	/**
	 * @param tag
	 * @return the numeric value of the last character in <em>tag</em> or -1 if
	 *         the last character is not a digit or -2 if <em>tag</em> is null
	 *         or empty
	 */
	protected int getTagDetail(String tag) {
		if (tag == null || tag.length() == 0)
			return -2;
		char lastChar = tag.charAt(tag.length() - 1);
		return Character.isDigit(lastChar) ?
				Character.getNumericValue(lastChar) :
				-1;
	}

	/**
	 * @param tag the tag string to parse
	 * @return the tag name given a tag string (removing digits as necessary).
	 */
	protected String getTagString(String tag) {
		if (tag == null || tag.length() == 0)
			return null;
		return Character.isDigit(tag.charAt(tag.length() - 1)) ?
				tag.substring(0, tag.length() - 1) :
				tag;
	}

	/**
	 * @return the current time as a String in {@link #dateFormat} format.
	 */
	protected static String getTimeStamp(){
		return CASAUtil.getDateAsString(dateFormat);
	}

	/**
	 * Sets the name of this Trace object.  Note that this does NOT affect any open log file's name, but will
	 * affect any resets to the file.
	 * @param value The new name.
	 */
	public void setName(String value){ traceName = value;}
	
	/**
	 * @return the name of this Trace object.
	 */
	public String getName(){ return traceName;}

	/**
	 * @return True iff this Trace object is saving history.
	 */
	public boolean getSaveHistory(){ return maxHistory>0;}

	/**
	 * Sets whether or not this Trace object will save history information.
	 * @param value True if history is to be saved.
	 */
	public void setSaveHistory(boolean value){
		if((maxHistory>0) != value){
			setHistoryMaxBytes(value?maxHistoryDefault:0);
		}
	}

	/**
	 * Sets the maximum bytes of history information to save.
	 * @param max The maximum bytes of history to be saved.
	 */
	public void setHistoryMaxBytes(long max) {
    if (max <= 100) 
    	history=null;
    else if (history==null) 
    	history = new StringBuffer();
		maxHistory = Math.max(max,100);
	}

	/**
	 * @return The history for this TraceObject, mitigated by the saveHistory setting and the historyMaxBytes setting.
	 * @see #setSaveHistory(boolean)
	 * @see #setHistoryMaxBytes(long)
	 */
	public String getHistory(){ return history!=null ? history.toString() : "";}

	/**
	 * The set of knowTags, initialized by {@link #getKnownTagsFromPersistentStore()}.
	 */
	private static TreeSet<String> knownTags = getKnownTagsFromPersistentStore();
	
	/**
	 * @return the set of all known tags, as specified by {@link CASA#getPreference(String, boolean, int) CASA.getPreference("knownTraceTags", "", 0)}
	 * @see #saveKnownTagsToPersistentStore()
	 * @see #updateTags(String)
	 */
	private static synchronized TreeSet<String> getKnownTagsFromPersistentStore() {
		String tags = CASA.getPreference("knownTraceTags", "", 0);
		String[] tagArray = tags.split(",");
		TreeSet<String> set = new TreeSet<String>();
		for (String t: tagArray) set.add(t);
		return set;
	}
	
	/**
	 * Saves the known tags back to persistent store by calling {@link CASA#putPreference(String, java.util.Map, int) CASA.putPreference("knownTraceTags", tags, CASA.USER|CASA.SYSTEM)}
	 * @see #getKnownTagsFromPersistentStore()
	 * @see #updateTags(String)
	 */
	private static synchronized void saveKnownTagsToPersistentStore() {
		String tags = getKnownTagsString();
		CASA.putPreference("knownTraceTags", tags, CASA.USER|CASA.SYSTEM);
	}
	
	/**
	 * Records the tag to persistent tags if it's not already there.
	 * @param tag The tag NAME (without trailing digit).
	 * @see #getKnownTagsFromPersistentStore()
	 * @see #saveKnownTagsToPersistentStore()
	 */
	private static synchronized void updateTags(String tag) {
		if (tag!=null && tag.length()>0 && !knownTags.contains(tag)) {
			knownTags.add(tag);
			saveKnownTagsToPersistentStore();
		}
	}

	/**
	 * @return The known tags as a comma-delimited string; this method only uses the locally-cached known tags.
	 * @see #saveKnownTagsToPersistentStore()
	 */
	public static synchronized String getKnownTagsString() {
		if (knownTags.size()==0) return ""; 
		StringBuilder s = new StringBuilder();
		for (String tag: knownTags) s.append(',').append(tag);
		return s.substring(1);
	}
	
	/**
	 * Sets the set of known tags to the empty set, and stores it persistently (and globally to CASA).
	 */
	public static synchronized void clearKnownTags() {
		knownTags.clear();
		saveKnownTagsToPersistentStore();
	}
	
	/**
	 * @param t
	 * @return True iff the trace element t is part of the printing methods at top of stack; that is, the method name begins with "print", "display", or "log". 
	 */
	private static boolean isPrintMethodElement(StackTraceElement t) {
		String methodName = t.getMethodName().toLowerCase(); 
		return methodName.startsWith("print") || methodName.startsWith("display")|| methodName.startsWith("log");
	}
	
	private static StackTraceElement[] getTrimmedStackTrace() {
		return getTrimmedStackTrace(3);
	}

	/**
	 * @param skip The number of top-level element to skip
	 * @return The current thread's stack with all the print-type methods trimmed off the top.
	 * @see Trace#isPrintMethodElement(StackTraceElement)
	 */
	private static StackTraceElement[] getTrimmedStackTrace(int skip) {
		Vector<StackTraceElement> ret = new Vector<StackTraceElement>();
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		boolean trimming = true;
		if (trace!=null) { // we can't be certain how deep the "real" calling method is, but let's assume it's the first one that doesn't start with "print" :)
			for (StackTraceElement t: trace) {
				if (skip-- > 0) // skip the first elements, according to "skip"
					continue;
				if (trimming && isPrintMethodElement(t))
					continue;
				trimming = false;
				ret.add(t);
			}
		}
		return ret.toArray(new StackTraceElement[ret.size()]);
	}
	
	/**
	 * Returns the stack with the best guess at trimming the the Trace methods off the top.
	 * @return The trimmed stack formated as a String with tabs before each line.
	 * @see Trace#getTrimmedStackTrace()
	 * @see Trace#isPrintMethodElement(StackTraceElement)
	 */
	public static String getTrimmedStackTraceString() {
		StringBuilder buf = new StringBuilder();
		for (StackTraceElement t: getTrimmedStackTrace(3)) {
			buf.append("\n\t").append(t.toString());
		}
		String ret = buf.toString();
		if (ret.length()>0)
			ret = ret.substring(1);
		return ret;
	}
	
	/**
	 * This is the print method that all the other println methods eventually call.
	 * The following is printed/logged:
	 * <ol>
	 * <li> if the tag is error the message will be "bracketed" by a long string of ">"'s, 
	 * if warning a medium string of ">"'s, 
	 * if the {@link #OPT_INCLUDE_CODE_LINE_NUMBER} is in <em>flags</em> then so short string of ">"'s.
	 * In any of these cases, the ">"'s is followed by the guess of the calling source file and line number.
	 * <li> the record header.
	 * <li> the prefix, if it is set.
	 * <li> a stack trace if <em>e</em> is non-null and {@link #OPT_SUPRESS_STACK_TRACE} is not set, or {@link #OPT_FORCE_STACK_TRACE} is set.
	 * <li> a matching closing string of "<"'s if we are bracketing with ">"'s.
	 * </ol>
	 * @param tag This tag MUST be present in set of traceTags for anything to be printed/logged.
	 * @param string The message to be printed/logged.
	 * @param e If this is non-null, the stack trace will be appended to the message
	 * @param flags The options flags; use bitwise disjunct ("|") to combine options - {@link #OPT_SUPPRESS_AGENT_LOG}, {@link #OPT_COPY_TO_SYSERR}, {@link #OPT_COPY_TO_SYSOUT}, {@link #OPT_FORCE_STACK_TRACE}, {@link #OPT_INCLUDE_CODE_LINE_NUMBER}, {@link #OPT_SUPPRESS_HEADER_ON_SYSOUT}, {@link #OPT_SUPRESS_STACK_TRACE}.
	 * @return The parameter <em>string</em>
	 */
	private String print(String tag, String string, Throwable e, int flags) {
		if (string==null || string.trim().length()==0)
			return "";
		if (isLoggingTag(tag) ) {
			String stackTrace = null;
			if (e!=null && (OPT_SUPRESS_STACK_TRACE&flags)==0)
				stackTrace = getStackTraceString(e);
			else if (e==null && (OPT_FORCE_STACK_TRACE&flags)!=0)
				stackTrace = getTrimmedStackTraceString();
				
			//set up flagging (>>>>>  <<<<< surrounds)
			boolean flagging=false;
			String inflagString=null, outflagString=null;
			if (tag!=null) {
				if (tag.length()>=5 && tag.substring(0,5).equals("error")) {
					errors++;
					flagging = true;
					inflagString  = ">>>>>>>>>>>>>>>>>>>>>>>>>";
					outflagString = "<<<<<<<<<<<<<<<<<<<<<<<<<";
					if (string.length()==0 || string.charAt(string.length()-1)!='\n') outflagString = "\n"+outflagString;
				}
				else if (tag.length()>=7 && tag.substring(0,7).equals("warning")) {
					warnings++;
					flagging = true;
					inflagString  = ">>>>>>>>>>>>>";
					outflagString = "<<<<<<<<<<<<<";
					if (string.length()==0 || string.charAt(string.length()-1)!='\n') outflagString = "\n"+outflagString;
				}
				else if ((flags&OPT_INCLUDE_CODE_LINE_NUMBER)!=0) {
					flagging = true;
					inflagString  = ">>>>>>>";
					outflagString = "<<<<<<<";
					if (string.length()==0 || string.charAt(string.length()-1)!='\n') outflagString = "\n"+outflagString;
				}
			}
			if (flagging) {
				String methodDescription = "unknown method";
				StackTraceElement[] trace = getTrimmedStackTrace();
				if (trace!=null && trace.length>0) { // we can't be certain how deep the "real" calling method is, but let's assume it's the first one that doesn't start with "print" :)
					methodDescription = trace[0].toString();
				}
				inflagString += (" "+methodDescription+LINE_SEPARATOR);
			}
			
			StringBuilder buff = new StringBuilder();
			
			if (flagging) 
				buff.append(inflagString).append(LINE_SEPARATOR);
			buff.append(getHeader(tag));
			if (prefix != null) 
				buff.append(prefix);
			buff.append(string.replaceAll("\n",LINE_SEPARATOR));
			if (stackTrace!=null)
				buff.append(LINE_SEPARATOR).append(stackTrace);
			if (flagging) 
				buff.append(LINE_SEPARATOR).append(outflagString);
			buff.append(LINE_SEPARATOR);
			String msg = buff.toString();
			
			if (history!=null){ 
				if(history.length()>maxHistory) {			//maximum amount of characters in the variable
					history.delete(0,(int)(maxHistory*.2));			//deleting the first 20% of characters
					history.replace(0, 3, "...");
				}
				history.append(msg);
			}
			
			boolean printedToSysout = false;
			if (logToFile) 
				printToFile(msg);
			else {
				if (this==processWideTrace && originalSysErr==null) { // we are the process-wide trace and we are not capturing system output or logging to a file.
					System.out.println((flags&OPT_SUPPRESS_HEADER_ON_SYSOUT)!=0?(string+(stackTrace==null?"":(LINE_SEPARATOR+stackTrace))):msg);
					printedToSysout = true;
				}
			}
			
			if ((flags&OPT_COPY_TO_SYSERR)!=0 && tag!="syserr")
				printToOriginalSysErr((flags&OPT_SUPPRESS_HEADER_ON_SYSOUT)!=0?(string+(stackTrace==null?"":(LINE_SEPARATOR+stackTrace))):msg);
			
			if (!printedToSysout && (flags&OPT_COPY_TO_SYSOUT)!=0 && tag!="sysout")
				printToOriginalSysOut((flags&OPT_SUPPRESS_HEADER_ON_SYSOUT)!=0?(string+(stackTrace==null?"":(LINE_SEPARATOR+stackTrace))):msg);
			
			if (agent!=null)
				agent.notifyObservers("event_trace",msg);
		}
		return string;
	}

	/*
	 * @see casa.util.TraceInterface#println(java.lang.String, java.lang.String)
	 */
	@Override
	public String println(String tag, String string) {
		return print(tag, string, null, 0);
	}
	
	/**
	 * @see casa.util.TraceInterface#println(java.lang.String, java.lang.String, int)
	 */
	@Override
	public String println(String tag, String string, int flags) {
		return print(tag, string, null, flags);
	}

  /*
	 * @see casa.util.TraceInterface#println(java.lang.String, java.lang.String, java.lang.Throwable)
	 */
	@Override
	public String println(String traceTag, String txt, Throwable ex) {
	  if (isLoggingTag(traceTag)) {
	    return print(traceTag, txt, ex, 0);
	  }
	  return txt;
	}

	/*
	 * @see casa.util.TraceInterface#println(java.lang.String, java.lang.String, java.lang.Throwable, int)
	 */
	@Override
	public String println(String traceTag, String txt, Throwable ex, int flags) {
	  if (isLoggingTag(traceTag)) {
	    return print(traceTag, txt, ex, flags);
	  }
	  return txt;
	}

	/**
	 * Converts the parameter Throwable to a string, including the stack trace and the "caused by" trace.
	 * @param ex
	 * @return The string version of the Throwable with the stack trace.
	 */
  public static String getStackTraceString(Throwable ex) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintWriter pw = new PrintWriter(out);
    for (Throwable th = ex; th!=null; th = th.getCause()) {
    	if (th!=ex) pw.println("Caused by:");
      th.printStackTrace(pw);
    }
    pw.println();
    pw.flush();
    return out.toString();
  }

  private void printToFile(String msg) {
  	if (fileWriter!=null) {
  		try {
  			if (logFile.length()>MAX_FILE_SIZE) { // file has grown to over its limit: wipe it and reset.
  				resetLogFile();
  				println("warning", "!Log file has grown to greater than "+(MAX_FILE_SIZE/1000000)+"MB.  It has been wiped and restarted "+ ++restarts +" time"+(restarts>1?"s":"")+" since starting at "+startDate+".");
  			}
  			fileWriter.write(msg);
  			fileWriter.flush();
  		}
  		catch (IOException ex) {
  			printToOriginalSysErr("Trace ERROR: Exception occured when trying to print message to "+logFile+":\n"+msg+"\n", ex);
  		}
  	}
  	else {
  		printToOriginalSysErr(msg);
  	}
  }

	private String getHeader(String tag){
		return getHeader(tag, getName());
	}
	
	/**
	 * Return a formatted a header.  The header is of format (with no spaces):
	 * <pre>
	 * [* <b>timestamp</b> : <em>agentName</em> : <em>tag</em> : <b>thread-name</b> *] 
	 * </pre>
	 * @param tag the tag used in the header.
	 * @param agentName the agentName used in the header.
	 * @return The formatted header as above.
	 */
	public static String getHeader(String tag, String agentName) {
		StringBuffer buff = new StringBuffer("[*"+getTimeStamp());
		buff.append(":");
		buff.append(agentName);
		buff.append(":");
		buff.append(tag==null?"":tag);
		buff.append(":");
		buff.append(Thread.currentThread().getName());
		buff.append("*] ");
		return buff.toString();
	}

	/**
	 * @return the total number of error-tagged messages seen.
	 */
	public static int getErrors(){ return errors; }

	/**
	 * @return the total number of warning-tagged messages seen.
	 */
	public static int getWarnings(){ return warnings; }

	/**
	 * @return the File object onto which we should write trace messages.
	 */
	public File getTraceFile() {
		return new File(getName() + ".log");
	}

	/**
	 * Log a message to System output in the format of the agents' log files.
	 * @param tag Used in the output message
	 * @param message The message to log
	 * @param ex If ex is not null, the stack dump is appended to the message.
	 * @param printStackTrace set to true to print a stack trace (note that if <em>ex</em> is not null, the trace will be printed anyway).
	 * @return The String (minus the generated log header) printed to the log
	 * @deprecated use {@link #log(String, String, Throwable, int) log(String, String, Throwable, options|#OPT_COPY_TO_SYSOUT|#OPT_SUPPRESS_AGENT_LOG)} instead
	 */
	@Deprecated
	public static String logToSysout(String tag, String message, Throwable ex, int options) {
		return log(tag, message, ex, options|OPT_COPY_TO_SYSOUT|OPT_SUPPRESS_AGENT_LOG);
//		String ret = message;
//	
//		if (ex!=null) {
//			if (options==0 || (options&OPT_SUPRESS_STACK_TRACE)==0) {
//				ByteArrayOutputStream trailer = new ByteArrayOutputStream();
//				PrintWriter pw = new PrintWriter(trailer);
//				for (Throwable th = ex; th!=null; th = th.getCause()) {
//					if (th!=ex) pw.println("Caused by:");
//					ex.printStackTrace(pw);
//				}
//				pw.flush();
//				ret += ":\n" + trailer.toString();
//			}
//			else {
//				ret += ": "+ex.toString();
//			}
//		}
//		else if ((options&OPT_FORCE_STACK_TRACE)!=0) {
//			ret += (":" + getTrimmedStackTraceString());
//		}
//		System.out.println(CASAUtil.makeLogHeader(tag)+ret);
//		return ret;
	}

	/**
	 * Logs the message to the TraceInterface object in the first argument.  If the first parameter
	 * is null, the log message will go to the process-wide Trace object.
	 * @param agent
	 * @param tag
	 * @param message
	 * @param ex
	 * @param options
	 * @return
	 */
	public static String log(TraceInterface agent, String tag, String message, Throwable ex, int options) {
		if (agent!=null) {
			return agent.println(tag, message, ex, options);
		}
		return processWideTrace.println(tag, message, ex, options);
	}

	/**
	 * Logs the message if tag is in effect to the in-scope agent's log files, windows, etc.
	 * If no agent can be found in scope, uses the process-wide Trace object to perform the logging.
	 * @param tag A trace tag: if an agent can't be found in scope this is ignored
	 * @param message The message to log
	 * @param ex If ex is not null, the stack dump is appended to the message.
	 * @return The String (minus the generated log header) printed to the log
	 * @see Trace#log(String, String)
	 * @see CASAUtil#getAbstractProcessInScope()
	 */
	public static String log(String tag, String message, Throwable ex) {
		return Trace.log(null, tag, message, ex, 0);
	}
	
	/**
	 * Logs the message if tag is in effect to the in-scope agent's log files, windows, etc.
	 * If no agent can be found in scope, uses the process-wide Trace object to perform the logging.
	 * @param tag A trace tag: if an agent can't be found in scope this is ignored
	 * @param message The message to log
	 * @param ex If ex is not null, the stack dump is appended to the message.
	 * @return The String (minus the generated log header) printed to the log
	 * @see Trace#log(String, String)
	 * @see CASAUtil#getAbstractProcessInScope()
	 */
	public static String log(String tag, String message, Throwable ex, int options) {
		AbstractProcess agent = (OPT_SUPPRESS_AGENT_LOG&options)!=0 ? null : CASAUtil.getAbstractProcessInScopeSilent();
		return Trace.log(agent, tag, message, ex, options);
	}
	
	/**
	 * Logs the message if tag is in effect to the in-scope agent's log files, windows, etc.
	 * If no agent can be found in scope, does the best-effort to log to System output.
	 * @param tag A trace tag: if an agent can't be found in scope this is ignored
	 * @param message The message to log
	 * @return The String (minus the generated log header) printed to the log
	 * @see log
	 * @see CASAUtil#getAbstractProcessInScope()
	 */
	public static String log(String tag, String message) {
		AbstractProcess agent = CASAUtil.getAbstractProcessInScopeSilent();
		if (agent!=null)
		  return agent.println(tag, message);
		return log(agent, tag, message, null, 0);
	}

	/**
	 * Logs the message if tag is in effect to the in-scope agent's log files, windows, etc.
	 * If no agent can be found in scope, does the best-effort to log to System output.
	 * @param tag A trace tag: if an agent can't be found in scope this is ignored
	 * @param message The message to log
	 * @return The String (minus the generated log header) printed to the log
	 * @see log
	 * @see CASAUtil#getAbstractProcessInScope()
	 */
	public static String log(String tag, String message, int options) {
		AbstractProcess agent = (OPT_SUPPRESS_AGENT_LOG&options)!=0 ? null : CASAUtil.getAbstractProcessInScopeSilent();
		if (agent!=null)
		  return agent.println(tag, message);
		return log(agent, tag, message, null, options);
	}
	
	/**
	 * Replaces {@link System#out}out and {@link System#err} to log any program output to the process-wide trace
	 * file.  Prints messages to out and err to indicate these have been re-routed.  The output is captured, but
	 * is then copied to the original out and err streams, so there is no apparent effect (other than the output
	 * is captured and logged).
	 */
	public static void captureSysout() {
		try {
			setProcessLogToFile(true);
			final PrintStream sysout = System.out;
			final PrintStream syserr = System.err;
			final LogStream logOutStream = new LogStream("sysout", processWideTrace, sysout);
			final LogStream logErrStream = new LogStream("syserr", processWideTrace, syserr);

			if (originalSysErr==null) //only do this on the FIRST call...
				originalSysErr = System.err;
			if (originalSysOut==null) //only do this on the FIRST call...
				originalSysOut = System.out;
			System.setOut(logOutStream);
			System.setErr(logErrStream);
			System.out.println("Standard output will be copied to "+getProcessTraceFile());
			System.err.println("Standard error will be copied to "+getProcessTraceFile());
		} catch (Throwable e2) {
			System.out.println("Cannot redirect standard out and standard err.");
			e2.printStackTrace();
		}
	}
	
	private void printToOriginalSysErr(String msg) {
		if (originalSysErr==null)
			System.err.println(msg);
		else
			originalSysErr.println(msg);
	}

	private void printToOriginalSysOut(String msg) {
		if (originalSysOut==null)
			System.out.println(msg);
		else
			originalSysOut.println(msg);
	}

	private void printToOriginalSysErr(String msg, Throwable e) {
		printToOriginalSysErr(msg+":\n"+getStackTraceString(e));
	}

	/**
	 * Turns on or off logging to the process-wide trace file.
	 * @param b true to set tracing on.
	 * @throws IOException
	 */
	static void setProcessLogToFile(boolean b) throws IOException {
		processWideTrace.setLogToFile(b);
	}
	
	/**
	 * @return True iff we are recording in the trace file.
	 */
	static File getProcessTraceFile() {
		return processWideTrace.getTraceFile();
	}
	
	/**
	 * Sets the process-wide trace tags.  This effects only the static log() family of methods.
	 * @param tags
	 * @see #setTraceTags(String)
	 */
	static public void setTags(String tags) {
		processWideTrace.setTraceTags(tags);
	}
	
	/**
	 * @return the process-wide trace tags as used by the static log() family of methods.
	 */
	static public String getTags() {
		return processWideTrace.getTraceTags();
	}
	
	private static PrintStream originalSysErr = null;
	private static PrintStream originalSysOut = null;
	
	static private class LogStream extends PrintStream {
		private TraceInterface trace;
		private String tag;

		public LogStream(String tag, TraceInterface trace, PrintStream sysout) throws FileNotFoundException {
			super(sysout);
			assert trace!=null;
			assert sysout!=null;
			this.trace = trace;
			this.tag = tag;
		}
		
		@Override
		public void write(byte[] buf, int off, int len) {
			super.write(buf, off, len);
			flush();
			String msg = CASAUtil.bytesToString(Arrays.copyOfRange(buf, off, off+len), null);
			trace.println(tag, msg);
		}

	}

}