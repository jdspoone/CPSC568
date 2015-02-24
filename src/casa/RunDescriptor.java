package casa;

import casa.util.CASAUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

/**
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public class RunDescriptor {
  public final static int None        = 0;
  public final static int CommandLine = 1;
  public final static int JavaClass   = 2;
  /**
	 */
  public final static int Internal    = 3;
  private static int Last        = 3;

  private String command = null;
  /**
	 */
  private int type = 0;
  /**
	 */
  private boolean authorized = false;

  public RunDescriptor() {
    command = null;
    type = None;
  }

  /**
   * Constructor to be used in conjunction with the toString() method; builds
   * a new RunDescriptor from a String with the syntax of toString().  The string
   * may contain more data than is necessary for the object (ie: there may be
   * another object after RunDescriptor object).
   * @param text input String
   * @throws ParseException if the input text is malformed
   */
  public RunDescriptor(String text) throws ParseException {
    parse(text,0);
  }

  /**
   * Same as the RunDescriptor(String) constructor, but takes a starting point
   * in the string, and updates <em>lastPosition</em> to indicate the point in
   * the string where it finished parsing
   * @param text String to parse
   * @param startAt the position in the string to start parsing
   * @param lastPosition (update) the position in the string there parsing finished
   * @throws ParseException if the input text is malformed
   */
  public RunDescriptor(String text, int startAt) throws ParseException {
    parse(text,startAt);
  }

  /**
   * Copy constructor
   * @param r the RunDescriptor to be copied
   */
  public RunDescriptor(RunDescriptor r) {
    command = new String(r.command);
    type = r.type;
  }

  public int parse(String text, int startAt) throws ParseException {
    String s = text;
    int pos = startAt, mark;
    //skip white space
    while (Character.isWhitespace(s.charAt(pos))) pos++;
    //verify the beginning of the object
    if (!s.substring(pos,pos+2).equals("(&")) throw new ParseException("Expected '(&'",pos);
    else pos+=2;
    //find the end of the object
    int end = s.indexOf("&)",pos);
    if (end<0) throw new ParseException("Expected '&)'",s.length());
    //read in the type
    mark = pos;
    while (Character.isDigit(s.charAt(pos))) pos++;
    int type = 0;
    try { type = Integer.parseInt(s.substring(mark, pos)); }
    catch (NumberFormatException ex) { throw new ParseException("expected an integer value for type", mark); }
    if (type>Last || type <0) throw new ParseException("expected an integer value between 1 and "+Integer.toString(Last),mark);
    this.type = type;
    //skip white space
    while (Character.isWhitespace(s.charAt(pos))) pos++;
    //read in the string part
    if (s.charAt(pos)=='"') {
      command = CASAUtil.fromQuotedString(s, pos);
      pos = CASAUtil.scanFor(s,pos,"\"");
      pos++;
      authorized = Boolean.getBoolean(s.substring(pos,end));
    }
    else {
      command = s.substring(pos, end);
      authorized = true;
    }
    return end+2;
  }

  /**
   * Make this RunDescriptor a <em>command line</em> run descriptor and specify
   * the command line to execute to run the agent.  Several special tokens will
   * be substituted just before the command is run:
   * <table>
   * <tr><td>%port%   </td><td>The port given in the run time command for the agent to listen at</td></tr>
   * <tr><td>%lacPort%</td><td>The LAC port given in teh run time command for the agent to register to</td></tr>
   * <tr><td>%path%   </td><td>The path of the the agent.  This is the concatonations of the %dir% and %file%</td></tr>
   * <tr><td>%file%   </td><td>The filename for the agent.  This represents an individual agent name.  Same as %name%.</td></tr>
   * <tr><td>%name%   </td><td>The name for the agent.  This represents an individual agent name.  Same as %file%.</td></tr>
   * <tr><td>%dir%    </td><td>The directory path, relative to the LAC's root, for the agent.  Same as %type%.</td></tr>
   * <tr><td>%type%   </td><td>The type path specifying the agent type.  Same as %dir%.</td></tr>
   * </table>
   * @param commandLine The command line, possibly containing replacement tokens as specified above.
   */
  public void setCommandLine(String commandLine) {
    type = CommandLine;
    this.command = commandLine;
  }

  /**
   * see setJavaClass().
   * @param internalClass The class specification (see setJavaClass()).
   */
  public void setInternal(String internalClass) {
    type = Internal;
    this.command = internalClass;
  }

  /**
	 * Set the value of <em>authorized</em>
	 * @param newVal  the new value of <em>authorized</em>
	 * @return  the value of <em>authorized</em> before this operation
	 */
  public boolean setAuthorized(boolean newVal) {
    boolean ret = authorized;
    authorized = newVal;
    return ret;
  }

  /**
	 * Determines whether the command in this RunDescriptor has been authorized or not. A RunDescriptor registered by an agent over network is generally not authorized, and will not be executed until it is authorized by a priviledged local user.
	 * @return  true if this RunDescriptor is authorized, false otherwise.
	 */
  public boolean isAuthorized() {
    return authorized;
  }

  /**
   * Set this RunDescriptor to be a <em> Java class run descriptor</em>.  The
   * syntax for the parameter is:
   * <pre>
   * fully-qualified-class-name { param-value:fully-qualified-class-name }*
   * </pre>
   * This specifies the values and types used to find and call the constructor
   * for the new object.
   * In the param-value position, you may use the following special tokens which
   * will be substituted just before creating the object:
   * <table>
   * <tr><td>%port%   </td><td>The port given in the run time command for the agent to listen at</td></tr>
   * <tr><td>%lacPort%</td><td>The LAC port given in teh run time command for the agent to register to</td></tr>
   * <tr><td>%path%   </td><td>The path of the the agent.  This is the concatonations of the %dir% and %file%</td></tr>
   * <tr><td>%file%   </td><td>The filename for the agent.  This represents an individual agent name.  Same as %name%.</td></tr>
   * <tr><td>%name%   </td><td>The name for the agent.  This represents an individual agent name.  Same as %file%.</td></tr>
   * <tr><td>%dir%    </td><td>The directory path, relative to the LAC's root, for the agent.  Same as %type%.</td></tr>
   * <tr><td>%type%   </td><td>The type path specifying the agent type.  Same as %dir%.</td></tr>
   * </table>
   * @param javaClass the specification of the class as above
   */
  public void setJavaClass(String javaClass) {
    type = JavaClass;
    this.command = javaClass;
  }

  /**
	 * Returns the type of this RunDescriptor.  Possible return types are: <ul> <li>RunDescriptor.None <li>RunDescriptor.CommandLine <li>RunDescriptor.JavaClass <li>RunDescriptor.Internal </ul>
	 * @return  the type of this RunDescriptor
	 */
  public int getType() {
    return type;
  }

  /**
   * Returns the command line if this RunDescriptor is of the <em>command line</em>
   * type, null otherwise.
   * @return a string or null.
   */
  public String getCommandLine() {
    return type==CommandLine ? command : null;
  }

  /**
	 * Returns the specification as a string if this RunDescriptor is of the <em>internal</em> type, null otherwise.
	 * @return  a string or null.
	 */
  public String getInternal() {
    return type==Internal ? command : null;
  }

  /**
   * Returns the specification as a string if this RunDescriptor is of the <em>Java class</em>
   * type, null otherwise.
   * @return a string or null.
   */
  public String getJavaClass() {
    return type==JavaClass ? command : null;
  }

  public String toString() {
    String ret = "(&"
               + Integer.toString(type) + " "
               + CASAUtil.toQuotedString(command) + " "
               + Boolean.toString(authorized)
               + "&)";
    return ret;
  }

  /**
   * Attempts to start a new agent based on the type of RunDescriptor this is.
   * @param port The requested port
   * @param lac The requested LAC port to register the agent to
   * @param path The commplete path, including type and agent name, of the agent
   * @return tries to return a port number for the agent.  May return 1 if the
   * agent was run, but was unable to determine the port the agent is listening at.
   * Returns 0 for failure.
   * @throws IOException
   * @throws ParseException
   * @throws ClassNotFoundException
   * @throws NoSuchMethodException
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   * @throws InstantiationException
   */
  public int run(int port, int lac, String path, TransientAgent agent)
      throws IOException,
             ParseException,
             ClassNotFoundException,
             NoSuchMethodException,
             InvocationTargetException,
             IllegalAccessException,
             InstantiationException {
    String com = getReplacedCommand(port, lac, path);
//    switch (type) {
//        case 0: // None
//          return 0;
//        case 1: // CommandLine
//          try {Runtime.getRuntime().exec(com);}
//          catch (IOException ex) {
//            DEBUG.PRINT("RunDescriptor.run: "+ex);
//            throw (ex);
//          }
//          return 1; /** @todo s/b the port of the agent. */
//        case 2: // JavaClass
//        case 3: // Internal
          try {
//            CASACommandLine2.main(com.split("\\s"));
          	agent.abclEval(com, null);
          }
          catch (Throwable ex1) {
        	  agent.println("error", "RunDescriptor.run: \"+com+\" yeilded exception: ", ex1);
        	  return 0;
          }
          return 1;
//          /*
//          TokenParser parser = new TokenParser(command);
//          Class cls = getclass(parser.getNextToken());
//          String s, typeName, paramName;
//          Vector types = new Vector(), params = new Vector();
//          for (int i=0; ((s=parser.getNextTokenQuotesIn())!=null); i++) {
//            String[] split = s.split(":");
//            if (split.length!=2) throw new ParseException("Bad expression in parameter '"+s+"'. Parameter spec format is <param-value>:<class>",0);
//            types.add(getclass(split[1]));
//            Object o = translateObjValue(split[0],port,lac,path);
//            params.add(o);
//          }
//          Class[] typesArray = new Class[types.size()];
//          for (int i=types.size()-1; i>=0; i--) typesArray[i] = (Class)types.elementAt(i);
//          Constructor constructor = cls.getConstructor(typesArray);
//          Object o = constructor.newInstance(params.toArray());
//          if (o instanceof AbstractProcess) return ((AbstractProcess)o).getPort();
//          return port;
//          */
//        default:
//          DEBUG.PRINT_ANYWAY("RunDescriptor.run: got an unexpected type: "+Integer.toString(type));
//          return 0;
//    }
  }

  protected String getReplacedCommand(int port, int lac, String path) {
    String com = new String(command);
    com = replace(com, "%port%", Integer.toString(port));
    com = replace(com, "%lacPort%", Integer.toString(lac));
    String name = null;
    if (path != null) {
      path = path.replace('.','/');
      com = replace(com, "%path%", path);
      int pos = path.lastIndexOf('/');
      if (pos < 0)             name = path;
      else                     name = path.substring(pos + 1);
    }
    if (name!=null) com = replace(com, "%file%", name);
    if (name!=null) com = replace(com, "%name%", new String(name));
    String _dir  = null;
    if (path != null) {
      int pos = path.lastIndexOf('/');
      if (pos >= 0) _dir = path.substring(0,pos);
    }
    if (_dir!=null) com = replace(com, "%dir%" , _dir);
    String _type = null;
    if (_dir!=null) _type = _dir.replaceAll("/",".");
    if (_type!=null) com = replace(com, "%type%", _type);
    return com;
  }

  private String replace(String in, String replace, String with) {
    //char c[] = in.toCharArray();
    String out = "";
    int i=0,j=1;
    while (j>0) {
      j = in.indexOf(replace, i);
      if (j > i) {
        out += in.substring(i, j) + with;
        i = j + replace.length();
      }
    }
    out += in.substring(i);
    return out;
  }

  protected Class<?> getclass(String s) throws ClassNotFoundException {
    if (s.equals("int")) return int.class;
    return Class.forName(s);
  }

  protected Object translateObjValue(String obj, int port, int lacPort, String path) throws ParseException {
    if (obj.equals("%port%"))    return new Integer(port);
    if (obj.equals("%lacPort%")) return new Integer(lacPort);
    if (obj.equals("%path%"))    return path;
    if (obj.equals("%file%") || obj.equals("%name%")) {
      if (path != null) {
        int pos = path.lastIndexOf('/');
        if (pos < 0)             return path;
        else                     return path.substring(pos + 1);
      } else                     return null;
    }
    if (obj.equals("%dir%") || obj.equals("%type%")) {
      if (path != null) {
        int pos = path.lastIndexOf('/');
        if (pos < 0)             return null;
        else                     return path.substring(0,pos + 1);
      } else                     return null;
    }
    return CASAUtil.unserialize(obj, null);
  }
}