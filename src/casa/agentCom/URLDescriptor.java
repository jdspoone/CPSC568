package casa.agentCom;

import casa.AbstractProcess;
import casa.ML;
import casa.MLMessage;
import casa.Status;
import casa.StatusObject;
import casa.TokenParser;
import casa.TransientAgent;
import casa.abcl.CasaLispOperator;
import casa.abcl.Lisp;
import casa.abcl.LispException;
import casa.abcl.ParamsMap;
import casa.exceptions.AmbiguousURLException;
import casa.exceptions.URLDescriptorException;
import casa.ui.AgentUI;
import casa.util.CASAUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.armedbear.lisp.Cons;
import org.armedbear.lisp.Environment;
import org.armedbear.lisp.JavaObject;
import org.armedbear.lisp.LispObject;
import org.armedbear.lisp.SimpleString;

/**
 * URLDescriptor strings are of the form: <pre>
 * casa:// [ user @ ] [ host ] [ : port ] [ / [ [   dir  / ]* [ file ] ] [# fragment] [? key [ = value ] [ & key [ = value ] ]* ] ]
 *                                          ----directory----                         -------------------data--------------------
 *                                          ---------path---------------
 * </pre> 
 * where <ul> 
 * <li> <em>user</em> is an alphanumeric local identifier for an individual user. 
 * <li> <em>host</em> is an IP address or host name.  Default="localhost". 
 * <li> <em>port</em> is a numeric value. 
 * <li> <em>path</em> specifies a domain-specific description of an agent type. 
 * <li> <em>directory</em> is part of the path. 
 * <li> <em>dir</em> is an alphanumeric string. 
 * <li> <em>file</em> is an alphanumeric string that is part of a path. 
 * <li> <em>fragment</em> is an alphanumeric string designating a part of the 
 *      agent (currently not used by CASA) 
 * <li> <em>data</em> specifies various data elements that might be used by the agent. 
 * <li> <em>key</em> is a key string (case sensitive) to retrieve the associated value. 
 * <li> <em>value</em> is an alphanumeric string. 
 * </ul>
 * The following are directly supported key/value data pairs: 
 * <ul> 
 * <li> <em><b>lac=</b>port</em> is the port number of the agent's LAC.  Note 
 *      that the host of the LAC and the agent itself must be identical.</li> 
 * <li> <em><b>indirect</b></em> (no value part) indicates that messages should 
 *      be routed trough the lac (which will forward it) instead of directly to the agent.</li> 
 * <li> <em><b>via=</b>URLDescriptor ...</em> indicates a list of urls that 
 *      all messages to this url should be proxied through.  This is done by 
 *      packing messages in the content of a proxy message.  Typically, this 
 *      is used to send a message through a CD instead of directly to another agent.</li> 
 * <li> <em><b>directed</b></em> (used in a CD url in a via datum only only, 
 *      ignored otherwise) indicates that the message to be forwarded should be 
 *      fowarded in <em>directed</em> mode ("+ syntax" -- tagged for the recipient, 
 *      but broadcaste to all CD members).</li> 
 * <li> <em><b>alias=</b>URLDescriptor</em> is the url to send any messages through. 
 *      This is used for situations such as tunneling messages trough a firewall.  
 *      Note that this alias URL should not contain any "&" characters (ie: it cannot 
 *      contain multiple value parts (otherwise the parser or the outer URL may 
 *      take the non-first values of the alias url as the outer ones).
 * </li> 
 * </ul> 
 * for example: 
 * <blockquote> 
 * <b>agent12@www.cpsc.ucalgary.ca:8000</b> describes an agent called agent12 
 *      that is currently running and listening at port 8000<br>
 * <b>136.159.2.4/canada/alberta/calgary?lac=9000</b> describes an agent instance
 *      ("calgary") of type "canada/alberta" that may or may not be running on 
 *      an unknown port, but it can be started or its port number obtained from 
 *      the LAC running on port 9000. 
 * </blockquote> 
 * URL strings are of two types: 
 * <ul> 
 * <li><b>resolved</b> where the current port of the agent is known.  In this case, 
 *     the path is usually ignored by the system. 
 * <li><b>unresoved</b> where the current port of the agent is not known, but the 
 *     url may become resolved if the the path and lac ("?lac=") are known.  This 
 *     resolution may be done by sending a resolve request message to the LAC 
 *     (which must have the same host as the agent), asking it to resolve the URL. 
 *     If the agent is running, the LAC may simply return a resolved URL.  If the 
 *     agent is not currently running, the LAC may activate and run the agent on 
 *     some port, then return the a resolved URL. 
 * </ul>
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. 
 * Permission to use, copy, modify, distribute and sell this software and its 
 * documentation for any purpose is hereby granted without fee, provided that 
 * the above copyright notice appear in all copies and that both that copyright 
 * notice and this permission notice appear in supporting documentation.  The 
 * Knowledge Science Group makes no representations about the suitability of 
 * this software for any purpose.  It is provided "as is" without express or 
 * implied warranty.</p>
 * @author   <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @author   <a href="mailto:sedachv@cpsc.ucalgary.ca">Vladimir Sedach</a>
 * @version 0.9
 */
public class URLDescriptor implements Comparable<URLDescriptor> {

	static private List<URLDescriptor> knownURLs = new LinkedList<URLDescriptor>();
		
	private static URLDescriptor resolve(URLDescriptor url) throws AmbiguousURLException {
//		try {
		assert url!=null;
		String name = url.getFile();
		String dir = url.getDirectory();
		InetAddress host = url.host;
		int port = url.getPort();
		boolean hasName = name!=null && name.length()>0;
		boolean hasDirectory = dir!=null && dir.length()>0;
		boolean hasHost = host!=null;
		boolean hasPort = port>0;
		assert name!=null || dir!=null || host!=null || port>0;
		//TODO the Following assumption that if the url doesn't have a name and it's port is 9000 then it's name is LAC is a bit of a hack...
		if (!hasName && port==9000) {
			name = "LAC";
			hasName = true;
		}
		synchronized(knownURLs) {
			URLDescriptor candidate = null;
			for (URLDescriptor kurl: knownURLs) {
				boolean kurlName = kurl.getFile()!=null && kurl.getFile().length()>0;
				boolean kurlDirectory = kurl.getDirectory()!=null && kurl.getDirectory().length()>0;
				boolean kurlHost = kurl.host!=null;
				boolean kurlPort = kurl.getPort()>0;
				if (hasName && kurlName && !name.equals(kurl.getFile()))
					continue;
				if (hasDirectory && kurlDirectory &&!dir.equals(kurl.getDirectory()))
					continue;
				if (hasHost && kurlHost && !host.equals(kurl.getHost()))
					continue;
				if (hasPort && kurlPort && port!=kurl.getPort())
					continue;
				if (!hasName &&  kurlName &&  hasPort && !kurlPort)
					continue;
				if ( hasName && !kurlName && !hasPort &&  kurlPort)
					continue;
				if (candidate==null)
					candidate = kurl;
				else {
					candidate = disambiguate(url,candidate,kurl);
				}
			}
			if (candidate==null) {
				knownURLs.add(url);
				return url;
			}
			else {
				candidate.mergeWith(url);
				return candidate;
			}
		}
	}
	
	private void mergeWith(URLDescriptor url) {
		if (getData()==null || getData().length()==0)
			setData(url.getData());
		if (getDirectory()==null || getDirectory().length()==0)
			setDirectory(url.getDirectory());
		if (getFile()==null || getFile().length()==0)
			setFile(url.getFile());
		if (getPort()==0)
			setPort(url.getPort());
		if (host==null)
			setHost(url.host);
		if (getFragment()==null || getFragment().length()==0)
			setFragment(url.getFragment());
		if (getUser()==null || getUser().length()==0)
			setUser(url.getUser());
	}
	
	/**
	 * If true, any URL which is ambiguous between a local and remote URL will silently resolve
	 * to the local URL.  If false, the ambiguity will cause a {@link URLDescriptorException} to
	 * be thrown.  
	 */
	static private boolean AMBIGUOUS_TAKEN_AS_LOCALHOST = true;
	
	/**
	 * If b is true, any URL which is ambiguous between a local and remote URL will silently resolve
	 * to the local URL.  If false, the ambiguity will cause a {@link AmbiguousURLException} to
	 * be thrown.  
	 * @param b
	 */
	static public void setDefaultAmbiguousURLtoLocal(boolean b) {
		AMBIGUOUS_TAKEN_AS_LOCALHOST = b;
	}
	
	/**
	 * @return if any URL which is ambiguous between a local and remote URL will silently resolve
	 * to the local URL, returns true.  If the ambiguity will cause a {@link URLDescriptorException} to
	 * be thrown, returns false.
	 */
	static public boolean getDefaultAmbiguousURLtoLocal() {
		return AMBIGUOUS_TAKEN_AS_LOCALHOST;
	}
	
	private static URLDescriptor disambiguate(URLDescriptor target, URLDescriptor x, URLDescriptor y) throws AmbiguousURLException {
		if (AMBIGUOUS_TAKEN_AS_LOCALHOST) {
			InetAddress loc = CASAUtil.getLocalHost();
			if (target.host==null) {
				boolean xl = loc.equals(x.getHost()); 
				boolean yl = loc.equals(y.getHost()); 
				if (xl && !yl)
					return x;
				if (!xl & yl) {
					return y;
				}
			}
		}
		//		System.out.println(CASAUtil.log("error", "URLDescriptor: Ambiguous URL match:\n"+target+"\n"+x+"\n"+y));
		throw new AmbiguousURLException(target, x, y);
	}
	public static URLDescriptor[] getKnownURLs() {
		URLDescriptor[] ret;
		synchronized (knownURLs) {
			ret = knownURLs.toArray(new URLDescriptor[knownURLs.size()]);
		}
		return ret;
	}

  private String user; // name of the physical user this agent is associated with
  /**
	 */
  private InetAddress host; // host machine url
  /**
	 */
  private int port; // port number on host machine
  /**
	 */
  private String path; // agent-specific path.
  /**
	 */
  private String fragment; // the part after the #
  /**
	 */
  private String data; // all the stuff after the ?, including the LAC port

  /**
   *
   * @param user is an alphanumeric string (e.g., agent1, 007) which is the name of the agent
       * @param host is an IP address or name, e.g., 136.159.2.4, www.cpsc.ucalgary.ca
   * @param port is a numeric value, e.g., 9000, 8080
   * @param path is a slash-divided sequence of alphanumeric strings, e.g., canada/alberta/calgary
   */
  private URLDescriptor (String user, InetAddress host, int port, String path) {
    initialize (user, host, port, path, "", null);
  }

  private URLDescriptor (String user, InetAddress host, int port, String path,
                        String data) {
    initialize (user, host, port, path, data, null);
  }

  private URLDescriptor (String user, InetAddress host, int port, String path,
                        String data, String fragment) {
    initialize (user, host, port, path, data, fragment);
  }

  private URLDescriptor (URLDescriptor url) {
    this (url.getUser (), url.getHost (), url.getPort (), url.getPath (),
          url.getData ());
  }

  private URLDescriptor (InetAddress host, int port) {
    initialize (null, host, port, null, null, null);
  }

  /**
   * Create a URL for the local machine on <em>port</em>
   * @param port The port for the url (the trailing : part)
   * @throws UnknownHostException If we can't get the IP address of the local machine for some reason
   */
  private URLDescriptor (int port) {
    //    initialize (null, InetAddress.getLocalHost (), port, null, null);
    initialize (null, CASAUtil.getLocalHost (), port, null, null, null);
  }

  private URLDescriptor (String host, String port) throws URLDescriptorException {
    initialize (null, null, 0, null, null, null);
    this.setHost (host);
    this.setPort (port);
  }

  private URLDescriptor (String host, String port, String user) throws URLDescriptorException {
    initialize (user, null, 0, null, null, null);
    this.setHost (host);
    this.setPort (port);
  }

  private URLDescriptor (String url_string) throws URLDescriptorException {
    if (url_string == null) throw new URLDescriptorException (URLDescriptorException.NULL_ERROR,
                                        "URL is null");
    String s = new String(url_string);
    s = s.trim ();

    if (s.startsWith("(")) { // handle a FIPA-type address...
    	Status stat = Lisp.abclEval(null, null, null, s, null);
    	if (stat.getStatusValue()>=0) {
    		if (stat instanceof StatusObject) {
    			Object obj = ((StatusObject<?>)stat).getObject();
    			if (obj instanceof URLDescriptor) {
    				URLDescriptor u = (URLDescriptor)obj;
    				initialize(u.user, u.host, u.port, u.path, u.data, u.fragment);
    				return;
    			}
    		}
    	}
    }
    
    // Not a FIPA address, try to interpret as CASA-URL.
    try {
    	// if we have a "casa://" prefix, remove it.
      if (s.length()>7) {
        if (s.substring(0, 7).equalsIgnoreCase("casa://")) {
          s = s.substring(7);
        }
      }

      int start = 0;
      int posQMARK = s.indexOf("?", start); //allow stuff after the "?" to contain URL special characters by truncating temporarily
      String dataString=null;
      if (posQMARK>0) {
        dataString = s.substring(posQMARK);
        s = s.substring(0, posQMARK);
      }
      int posAT = s.indexOf ("@");
      if (posAT>0) start = posAT;
      int posCOLON = s.indexOf (":", start);
      if (posCOLON>0) start = posCOLON;
      int posSLASH = s.indexOf ("/", start);
      if (posSLASH>0) start = posSLASH;
      int posHASH = s.indexOf ("#", start);
      if (posHASH>0) start = posHASH;
      if (dataString!=null) s += dataString;

      String USER, HOST, PATH, FRAGMENT, DATA;

      if (posAT != -1) {
        USER = s.substring (0, posAT);
      } else {
        USER = "";
      }

      if (posQMARK != -1) {
        DATA = s.substring (posQMARK + 1);
        s = s.substring (0, posQMARK);
      } else {
        DATA = "";
      }

      if (posHASH != -1) {
        FRAGMENT = s.substring (posHASH + 1);
        s = s.substring (0, posHASH);
      } else {
        FRAGMENT = "";
      }

      if (posSLASH != -1) {
        PATH = s.substring (posSLASH + 1, s.length ());
      } else {
        PATH = "";
        posSLASH = s.length ();
      }

      if (posCOLON != -1) {
        HOST = s.substring (posAT + 1, posCOLON);
        if (HOST.length()==0) HOST = null; //"localhost";
        this.setPort(s.substring (posCOLON + 1, posSLASH));
      } else {
      	String temp = s.substring (posAT + 1, posSLASH);
      	try {
      	  this.setPort(Integer.parseInt(temp));
	      	HOST = null; //"localhost";
      	} 
      	catch (NumberFormatException ex) {
          HOST = temp;
          this.setPort(0);
      	}
      }

      this.setUser (USER);
      this.setHost (HOST);
      this.setPath (PATH);
      this.setFragment (FRAGMENT);
      this.setData (DATA);
    } catch (StringIndexOutOfBoundsException e) {
      throw new URLDescriptorException (URLDescriptorException.HOST_ERROR,
                                        "malformed URL: '" + url_string + "'");
    }
  }

  private URLDescriptor (String user, String host, String port, String path) throws
      URLDescriptorException {
    initialize (user, null, 0, path, null, null);
    this.setHost (host);
    this.setPort (port);
  }

  public static URLDescriptor make(String user, InetAddress host, int port, String path) throws URLDescriptorException {
  	return resolve(new URLDescriptor(user, host, port, path));
  }

  public static URLDescriptor make(String user, InetAddress host, int port, String path, String data) throws URLDescriptorException {
  	return resolve(new URLDescriptor(user, host, port, path, data));
  }

  public static URLDescriptor make(String user, InetAddress host, int port, String path,	String data, String fragment) throws URLDescriptorException {
  	return resolve(new URLDescriptor(user, host, port, path, fragment));
  }

  public static URLDescriptor make(URLDescriptor url) throws URLDescriptorException {
  	return resolve(new URLDescriptor(url));
  }

  public static URLDescriptor make(InetAddress host, int port) throws URLDescriptorException {
  	return resolve(new URLDescriptor(host, port));
  }

  /**
   * Create a URL for the local machine on <em>port</em>
   * @param port The port for the url (the trailing : part)
   * @throws URLDescriptorException 
   * @throws UnknownHostException If we can't get the IP address of the local machine for some reason
   */
  public static URLDescriptor make(int port) throws URLDescriptorException {
  	return resolve(new URLDescriptor(port));
  }

  public static URLDescriptor make(String host, String port) throws URLDescriptorException {
  	return resolve(new URLDescriptor(host, port));
  }

  public static URLDescriptor make(String host, String port, String user) throws URLDescriptorException {
  	return resolve(new URLDescriptor(host, port, user));
  }

  public static URLDescriptor make(String url_string) throws URLDescriptorException {
  	return resolve(new URLDescriptor(url_string));
  }

  public static URLDescriptor fromString(String url_string) throws URLDescriptorException {
  	return make(url_string);
  }

  public static URLDescriptor make(String user, String host, String port, String path) throws URLDescriptorException {
  	return resolve(new URLDescriptor(user, host, port, path));
  }

  private void initialize (String user, InetAddress host, int port, String path,
                           String data, String fragment) {
    setUser (user);
//    this.host = (host == null ? CASAUtil.getLocalHost () : host);
    this.host = host;
        //    try {
        //      this.host = host == null ? InetAddress.getLocalHost () : host;
        //    } catch (UnknownHostException ex) {
        //      throw new URLDescriptorException (URLDescriptorException.HOST_ERROR,
        //                                        "Unknown host: " + host);
        //    }
    if (this.host!=null && this.host.isLoopbackAddress()) 
    	this.host = CASAUtil.getLocalHost();

    setPort (port);
    setPath (path);
    setFragment (fragment);
    setData (data);
  }

  /** 
   * Copy the contents of the param url to this one.
   * @param url
   */
  public void copy (URLDescriptor url) {
    setUser (url.getUser ());
    setHost (url.getHost ());
    setPort (url.getPort ());
    setPath (url.getPath ());
    setData (url.getData ());
  }
  
  /****************
   *     HAS/IS
   ****************/

  /**
       * @return TRUE (HAS) iff the Agent is identified under any alphanumeric String
   */
  public boolean hasUser () {
    return (user != null) && (user.length () > 0);
  }

  /**
   * @return TRUE (HAS) iff the Agent contains an alphanumeric String as its
   *                        path (see def. of path above)
   */
  public boolean hasPath () {
    return (path != null) && (path.length () > 0);
  }

  public boolean hasPort () {
    return port > 0;
  }

  public boolean hasLACport () {
    String lac = getDataValue ("lac");
    if (lac == null) {
      return false;
    }
    int port = 0;
    try {
      port = Integer.parseInt (lac);
    } catch (NumberFormatException ex) {
      return false;
    }
    return port>0;
  }

  public boolean hasFragment () {
    return (fragment != null) && (fragment.length () > 0);
  }

  public boolean hasData () {
    return (data != null) && (data.length () > 0);
  }

  public boolean isResolved () {
    return hasPort ();
  }

  public boolean isResolvable () {
    return hasPath () && hasLACport ();
  }

  /****************
   *     SET
   ****************/

  public void unresolve () {
    setPort (0);
  }

  /**
	 * @param user  is passed in order to set the Agent's name, i.e. Agent1
	 */
  public void setUser (String user) {
    if (user == null) {
      this.user = user;
    } else {
      this.user = CASAUtil.encode (user, false);
    }
  }

  /**
	 * @param host
	 */
  private void setHost (InetAddress host) {
//    if (host == null) {
//      this.host = host;
//    } else {
      this.host = host;
//    }
  }

  private void setHost (String host) throws URLDescriptorException {
    try {
      if (host == null || host.length ()==0)
      	this.host = null;
      else if ("localhost".equalsIgnoreCase(host))
      	this.host=CASAUtil.getLocalHost();
      else
        this.host = InetAddress.getByName(host);
    } catch (UnknownHostException e) {
      throw new URLDescriptorException (URLDescriptorException.HOST_ERROR,
                                        "Unknown host: " + host);
    }
  }

  /**
   * @param port
   * @throws URLDescriptorException
   */
  public void setPort (String port) throws URLDescriptorException {
    try {
      int portInt = Integer.parseInt (port);
      setPort (portInt);
    } catch (NumberFormatException e) {
      throw new URLDescriptorException (URLDescriptorException.PORT_ERROR,
                                        "ports must be integer values");
    }
  }

  /**
	 * @param port
	 */
  public void setPort (int port) {
    this.port = port < 0 ? 0 : port;
  }

  /**
	 * @param path
	 */
  private void setPath (String path) {
  	assert (!"null".equals(path));
    if (path == null) {
      this.path = "";
      return;
    }
    this.path = CASAUtil.encode (path, false);
  }

  public void setLACport (int lac_port) {
    if (lac_port <= 0) {
      deleteDataValue ("lac");
      return;
    }
    setDataValue ("lac", Integer.toString (lac_port));
  }

  public void setIndirect (boolean val) {
    if (!val) {
      deleteDataValue ("indirect");
      return;
    }
    setDataValue ("indirect", null);
  }

  public void setDirectory (String dir) {
    String oldFile = getFile ();
  	if (dir==null) {
  		setPath(null);
  		if (oldFile!=null) 
  			setFile(oldFile);
  		return;
  	}
    setPath (dir + (dir.charAt (dir.length()-1) == '/' ? "" : "/") +
             (oldFile == null ? "" : oldFile));
  }

  private void setFile (String file) {
    String oldDir = getDirectory ();
    setPath ((oldDir == null ? "" : oldDir) + (file==null ? "" : file));
  }

  /**
	 * Replace the entire data section in the URL.  It should be syntactically correct as it isn't checked (except for encoding).  If you only want to add, delete, or replace a key see  {@link #setDataValue(String,String)} or  {@link #deleteDataValue(String)} .
	 * @param  data
	 */
  public void setData (String data) {
    if (data == null) {
      this.data = "";
      return;
    }
    this.data = CASAUtil.encode (data, false);
  }

  /**
	 * @param frag
	 */
  public void setFragment (String frag) {
    if (frag == null) {
      this.fragment = "";
      return;
    }
    this.fragment = CASAUtil.encode (frag, false);
  }

  /**
   * Inserts a data key (and potentially a value) in to the data part 
   * of the url (after the "?", separated by "&"s).  If a key is
   * already present it will be replaced.
   * @param key
   * @param value may be null, indicating no value (just the key) 
   */
  public void setDataValue (String key, String value) {
    if (key == null) {
      throw new RuntimeException ("null data keys aren't allowed");
    }
    if (getDataValue (key) != null) { //there already is a value
      deleteDataValue (key);
    }
    data += ((data.length () > 0) ? "&" : "") + CASAUtil.encode (key, false) +
        (value == null ? "" : "=" + CASAUtil.encode (value, true));
  }

  /**
   * Removes a data key (and potentially a value) from the data part 
   * of the url (after the "?", separated by "&"s).
   * @param key
   * @param guiValue may be null, indicating no value (just the key) 
   */
  public String deleteDataValue (String key) {
    key = CASAUtil.encode (key, false);
    String oldValue = getDataValue (key);
    if (oldValue == null) {
      return null;
    }
    String temp = "&" + data + "&";
    int start = temp.indexOf ("&" + key + "=");
    if (start == -1) {
      start = temp.indexOf ("&" + key + "&"); //the "no value" case
    }
    int end = temp.indexOf ('&', start + 1);
    while (temp.startsWith ("&amp;", end)) {
      end = temp.indexOf ('&', end + 1);
    }
    temp = temp.substring (0, start) + temp.substring (end);
    data = temp.length () < 2 ? "" : temp.substring (1, temp.length () - 1);
    return oldValue;
  }

  /****************
   *     GET
   ****************/

  /**
	 * GET
	 */
  public String getUser () {
    return user;
  }

  /**
	 * @return
	 */
  public InetAddress getHost () {
    return (host == null ? CASAUtil.getLocalHost () : host);
  }

  public String getHostString () {
    return getHost().getHostAddress ();
  }

  /**
	 * @return
	 */
  public int getPort () {
    return Math.abs(port);
  }

  /**
	 * @return
	 */
  public String getPath () {
    return path;
  }

  public String getHostAndPort () {
    return getHostString() + ":" + String.valueOf (port);
  }

  public int getLACport () {
    try {
      return Integer.parseInt (getDataValue ("lac"));
    } catch (NumberFormatException ex) {
      return 0;
    }
  }

  public boolean getIndirect () {
    return getDataValue ("indirect") != null;
  }

  /**
   * @return The path (not including the File or Name) with the leading / and training /.
   */
  public String getDirectory () {
    if (path == null || path.length()==0) {
      return null;
    }
    int lastSlash = path.lastIndexOf ('/');
    if (lastSlash == -1) {
      return null; // -1 means there is not slash
    }
    return path.substring (0, lastSlash + 1);
  }

  /**
   * @return The File or AgentName not including any slashes.
   */
  public String getFile () {
    if (path == null || path.length()==0) {
      return null;
    }
    int lastSlash = path.lastIndexOf ('/');
    if (lastSlash == -1) {
      return path; // -1 means there is not slash: there is no directory, just a file.
    }
    return path.substring (lastSlash + 1);
  }

  /**
	 * @return
	 */
  public String getData () {
    return data;
  }

  /**
	 * @return
	 */
  public String getFragment () {
    return fragment;
  }

  public String getShortestName() {
    String ret = getFile();
    if (ret==null || ret.length()==0) ret = getHostAndPort();
    return ret;
  }

  /**
   * Check the data (after the "?") for the key and return the value
   * @param key the key string, eg: ...?key=value&key2=value2
   * @return
   * <li><b>the value</b>, if the key is there and there is a value
   * <li><b>""</b>, if the key is there, but no value (eg: ...?key&...)
   * <li><b>null</b>, if the key is not found in the data
   */
  public String getDataValue (String key) {
    key = CASAUtil.encode (key, false);
    String temp = "&" + data + "&";
    int start = temp.indexOf ("&" + key + "=");
    if (start == -1) { //try the "no value" case
      start = temp.indexOf ("&" + key + "&");
      if (start == -1) {
        return null; // not found
      } else {
        return ""; // no value, but it's there
      }
    }
    start += 2 + key.length ();
    int end = temp.indexOf ('&', start);
    while (temp.startsWith ("&amp;", end)) {
      end = temp.indexOf ('&', end + 1);
    }
    String ret = temp.substring (start, end);
    return CASAUtil.decode (ret, true);
  }

  /**
   * Check the data (after the "?") for the existence of the key.
   * 
   * @param key the key string, eg: ...?key=value&key2=value2
   * @return <code>true</code> if the key is found in the data; <code>false</code> otherwise.
   */
  public boolean hasDataValue (String key) {
    key = CASAUtil.encode (key, false);
    String temp = "&" + data + "&";
    int start = temp.indexOf ("&" + key + "=");
    if (start == -1) { //try the "no value" case
      start = temp.indexOf ("&" + key + "&");
      if (start == -1) {
        return false; // not found
      }
    }
    return true; // it's there, value or no
  }

  /**
   * Determine if this address is local relative to 'relativeTo'.
   * @param relativeTo
   * @return True iff 
   */
  public boolean local(URLDescriptor relativeTo) {
    if (relativeTo==null) return true;
    byte[] o = relativeTo.getHost().getAddress();
    byte[] t = getHost().getAddress();
    for (int i = 0, end = o.length - 1; i < end; i++)
      if (o[i] != t[i]) return false;
    return true;
  }

  /**
   *
   * @return String containing the host, port, '@'user and '/'path
   */
  public String getFullAddress (URLDescriptor relativeTo) {

    //protocol
    StringBuilder b = new StringBuilder("casa://");

    //user
    if (hasUser ()) {
      b.append(getUser()).append("@");
    }

    //IP address
    b.append(getHostString());

    //port
    if (hasPort ()) {
    	b.append(":").append(Integer.toString (getPort()));
    }

    //path
    if (hasPath ()) {
    	b.append("/").append(getPath());
    }

    //fagment (#)
    if (hasFragment ()) {
    	b.append("#").append(getFragment());
    }

    //data (?)
    if (hasData ()) {
    	b.append("?").append(getData ());
    }

    return b.toString();
  }

  /**
   * other methods
   **/

  /**
   * Simply calls #getFullAddress(<em>relativeTo</em>)
   * @return the entire address that corresponds to each Agent, YP, CD, or LAC
   *         For example, some of the prodefined Full addresses are:
   *         User: Agent1 --> 136.159.14.232:9005   ... it will also contain
   *                          136.159.14.232:13361  in order to show the IPSocket(port)
   *                                                that is using in order to
   *                                                connect with other Agents
   */
  public String toString (URLDescriptor relativeTo) {
    return getFullAddress (relativeTo);
  }

  /**
   * Simply calls #getFullAddress(null)
   * @return the entire address that corresponds to each Agent, YP, CD, or LAC
   */
  @Override
	public String toString () {
  	AbstractProcess agent = CASAUtil.getAbstractProcessInScopeSilent();
  	if (agent!=null && agent.options.fipa_urls) {
  		return toStringAgentIdentifier(false);
  	}
    return getFullAddress (null);
  }
  
  public String toStringAgentIdentifier(boolean shortForm) {
  	StringBuilder b = new StringBuilder("(agent-identifier");
  	if (getFile()!=null && getFile().length()>0) 
  		b.append(" :name ").append(getFile());
  	if (!shortForm) {
  		b.append(" :url ").append(toString());
//  		if (getDirectory()!=null && getDirectory().length()>0) b.append(" :type \"").append(getDirectory()).append('"');
//  		if (host!=null) b.append(" :host \"").append(host.getHostAddress()).append('"');
//  		if (port!=0) b.append(" :port ").append(port);
//  		if (user!=null && user.length()>0) b.append(" :user \"").append(user).append('"');
//  		if (data!=null && data.length()>0) b.append(" :data \"").append(data).append('"');
//  		if (fragment!=null && fragment.length()>0) b.append(" :fragment \"").append(fragment).append('"');
  	}
  	b.append(')');
  	return b.toString();
  }

  /**
   * Constructs a URLDescriptor using the TokenParser input
   * @param parser
   * @return
   * @throws URLDescriptorException
   */
  public static URLDescriptor fromString (TokenParser parser) throws URLDescriptorException {
    String str = parser.getNextToken ();
    if (str == null || str.equals (ML.NULL)) {
      return null;
    } else {
      return URLDescriptor.make(str);
    }
  }

  /* *****************************************************************************
   *************** STATIC UTILITY METHODS TO DEAL WITH LISTS OF URLS**************
   *******************************************************************************/
  static public URLset peakURL(String s) throws URLDescriptorException {
    URLAndInt u = null;
    URLset ret = null;

    if (s==null) return null;
    s = s.trim();
    if (s.length()==0) return null;
    char c = s.charAt(0);

    switch (c) {
      case '+':
        ret = peakURL(s.substring(1));
        ret.setMarked(true);
        break;
      case '{':
        for (u=readFirstURL(s,1); u!=null; u=readFirstURL(s,u.i)) {
          if (ret==null) ret = new URLset();
          ret.add(u.url);
        }
        break;
      case '*':
        ret = new URLset();
        break;
      default:
        u = readFirstURL(s,0);
        if (u!=null) {
          ret = new URLset();
          ret.add(u.url);
        }
    }
    return ret;
  }

  static public String popURL(String s) /*throws URLDescriptorException*/ {
    String ret = null;

    if (s==null) return null;
    s = s.trim();
    if (s.length()==0) return null;
    char c = s.charAt(0);

    switch (c) {
      case '+':
        ret = popURL(s.substring(1));
        break;
      case '{':
        int close = CASAUtil.scanFor(s, 0, "}");
        if (close==-1) return null;
        ret = (s.length()<(close+1))?null:s.substring(close+1);
        break;
      case '*':
        ret = (s.length()>1)?s.substring(1):null;
        break;
      default:
        int end = CASAUtil.scanForWhiteSpace(s, 0);
        end = (end==-1?s.length():end);
        ret = (end>0)?s.substring(end):null;
    }
    if (ret!=null) ret = ret.trim();
    if (ret!=null && ret.length()==0) ret = null;
    return ret;
  }

  static public URLAndInt readFirstURL(String s, int startAt) throws URLDescriptorException {
    URLAndInt ret = null;
    if (s==null) return null;
    s = s.trim();
    if (s.length()==0) return null;
    char c = s.charAt(0);
    if (Character.isLetterOrDigit(c)) {
      int end = CASAUtil.scanForWhiteSpace(s, 0);
      int close = CASAUtil.scanFor(s, 0, "}");
      if (close!=-1 && close<end) end = close;
      end = (end==-1?s.length():end);
      if (end>0) {
        ret = new URLAndInt(new URLDescriptor(s.substring(0, end)), end);
      }
    }
    return ret;
  }

  static public String pushURL(String s, String url) {
    String ret = url + (s==null ? "" : (" " + s));
    return ret;
  }

  static public String pushURL(String s, String[] url) {
    String ret = "{";
    for (int i=0, end=url.length; i<end; i++) {
      ret = ret + url[i] + " ";
    }
    ret = ret + "}" + (s==null ? "" : (" " + s));
    return ret;
  }

  /*******************************************************************************/

  public static void main (String[] args) {
		

    try {
  		URLDescriptor url1 = new URLDescriptor(8000);
  		URLDescriptor url2 = new URLDescriptor(":8000");
  		URLDescriptor url3 = new URLDescriptor("198.166.10.1:8000");
  		System.out.println(url1.compareTo(url2));
  		System.out.println(url2.compareTo(url3));
  		System.out.println(url3.toStringAgentIdentifier(false));
  		URLDescriptor url4 = new URLDescriptor("(agent-identifier :name \"fred\")");
  		System.out.println(url4.toStringAgentIdentifier(false));
  		System.out.println(url4.toString());
  		
//    	URLDescriptor url1 = new URLDescriptor("169.254.62.135", "9010");
//    	URLDescriptor url2 = new URLDescriptor("192.168.20.108", "9010");
//    	System.out.println(url1.equals(url2));
//     	System.out.println(url2.equals(url1));
//     	System.out.println(url1.equals(url1));
//    	URLDescriptor url3 = new URLDescriptor("193.168.20.108", "9010");
//     	System.out.println(url1.equals(url3));
//    	
//    	
//    	
//      URLDescriptor url = new URLDescriptor ("casa://kremer@136.159.10.10:8000/top/bottom.txt/file.txt?lac=9000&data=somed&amp;ata");
//      System.out.println (url.getUser () + "@" + url.getHostString () + ":" +
//                          url.getPort () + "/" + url.getPath () + "#" +
//                          url.getData ());
//      System.out.println (url.toString (null));
//      System.out.println ("lac=" + url.getLACport () + "  data=" +
//          url.getDataValue ("data"));
//      url.deleteDataValue ("data");
//      System.out.println ("lac=" + url.getLACport () + "  data=" +
//          url.getDataValue ("data"));
//      url.setDataValue ("data", "some&amped&up&thing&amp;");
//      System.out.println ("lac=" + url.getLACport () + "  data=" +
//                          url.getDataValue ("data"));
//      System.out.println (url.toString (null));
//      url.deleteDataValue ("lac");
//      System.out.println ("lac=" + url.getLACport () + "  data=" +
//                          url.getDataValue ("data"));
//      System.out.println (url.toString (null));
//      System.out.println ("directory=" + url.getDirectory () + "   file=" +
//                          url.getFile ());
//      url = new URLDescriptor (
//          "casa://kremer@136.159.10.10/top/bottom.txt/file.txt");
//      System.out.println (url.toString (null));
//
//      System.out.println ("\nTesting encoding/decoding...");
//      String string = "c r ap%p ol&a";
//      System.out.println ("Str\n" + string);
//      System.out.println ("Str -> ENC");
//      System.out.println (CASAUtil.encode (string, true));
//      System.out.println ("Str -> ENC -> DEC");
//      System.out.println (CASAUtil.decode (CASAUtil.encode (string, true), true));
//      System.out.println ("Str -> ENC -> ENC");
//      System.out.println (CASAUtil.encode (CASAUtil.encode (string, true), true));
//      System.out.println ("Str -> ENC -> ENC -> DEC -> DEC");
//      System.out.println (CASAUtil.decode (CASAUtil.decode (CASAUtil.encode (
//          CASAUtil.encode (string, true), true), true), true));
//      System.out.println ("Str -> ENC -> DEC -> ENC -> ENC");
//      System.out.println (CASAUtil.encode (CASAUtil.encode (CASAUtil.decode (
//          CASAUtil.encode (string, true), true), true), true));
//
    } catch (URLDescriptorException ex) {
    	System.out.println("EXCEPTION: " + ex.toString());
    	CASAUtil.log("error", "URLDescription.main()", ex, true);
    }
  }

  private int compareHosts(InetAddress otherAddr) {
  	InetAddress thisHost = this.getHost();
  	if (thisHost.isLoopbackAddress()) thisHost = CASAUtil.getLocalHost();
  	InetAddress[] thisHosts=CASAUtil.getInetAddressesFor(thisHost);
  	InetAddress[] thoseHosts=CASAUtil.getInetAddressesFor(otherAddr);
  	//InetAddress thishost=this.getHost(), thathost=otherURL.getHost();
  		for (InetAddress thishost: thisHosts) {
  			for (InetAddress thathost: thoseHosts) {
//  				if (thishost.isLoopbackAddress()) thishost = CASAUtil.getLocalHost();
//  				if (thathost.isLoopbackAddress()) thathost = CASAUtil.getLocalHost();
  				if (thishost.equals(thathost)) {
  					return 0;
  				}
  			}
  		}
  	return thisHost.toString().compareTo(otherAddr.toString());
  }
  
  /**
   * Determines whether the given object is a <code>URLDescriptor</code> that
   * describes that same agent as this <code>URLDescriptor</code>.  The argument
   * object may be a string, in which case, an attempt is made to parse it,
   * and if it is a legitimate URLDescriptor, then it is compared as such.
   *
   * @param object The object to compare to this <code>URLDescriptor</code>.
   * @return <code>true</code> if the object is a <code>URLDescriptor</code>
   * and has the same hosts and ports, OR if either of the ports are 0 (undefined)
   * then the hosts and paths are compare instead;
   * <code>false</code> otherwise.
   */
  @Override
	public boolean equals (Object object) {
  	URLDescriptor otherURL;
    if (object == this) {
      return true;
    } else if (object instanceof URLDescriptor) {
    	  return compareTo((URLDescriptor)object)==0;
    } else if (object instanceof String) {
      try {
        // Try to convert the String into a URLDescriptor to pass into equals
        // anew.
        otherURL = make((String) object);
        return compareTo(otherURL)==0;
      } catch (URLDescriptorException ex) {
        return false;
      }
    } else {
      return false;
    }
    
//    if (compareHosts(otherURL.getHost())!=0) 
//    	return false;
//
//  	// hosts are the same, compare ports or paths
//  	int thisPort = getPort(), thatPort = otherURL.getPort();
//  	// both ports are defined: use the port equiv.
//  	if (thisPort != 0 && thatPort != 0) { 
//  		if (thisPort!=thatPort)
//  			return false; // -ve, 0, or +ve
//  	} 
//  	// at this point, we're down to comparing paths...
//  	String thisDir = getDirectory(), thatDir = otherURL.getDirectory();
//  	String thisName = getFile(), thatName = otherURL.getFile();
//  	if (thisDir==null || thatDir == null) { // one or both of directories is not defined
//  		if (thisName!=null && thisName.length()>0 && thatName!=null && thatName.length()>0) 
//  			if (!thisName.equals(thatName))
//  				return false;
//  	}
//  	else {
//  		if (!getPath().equals(otherURL.getPath())) // compare whole paths
//  			return false;
//  	}
//  	return true;
  }
  
  @Override
	public int compareTo(URLDescriptor o) {
  	if (o==this) 
  		return 0;
  	InetAddress thishost=this.getHost(), thathost=o.getHost();
  	if (thishost.isLoopbackAddress()) thishost = CASAUtil.getLocalHost();
  	if (thathost.isLoopbackAddress()) thathost = CASAUtil.getLocalHost();

  	int temp = compareHosts(thathost);
  	if (temp!=0) 
  		return temp;

  	// hosts are the same, compare ports
  	int ret = 0;
  	int thisPort = getPort(), thatPort = o.getPort();
  	// both ports are defined: use the port equiv.
  	if (thisPort != 0 && thatPort != 0) { 
  		ret = thisPort-thatPort; // -ve, 0, or +ve
  	} 
  	// if we can distinguish based on just port, return
  	if (ret != 0) 
  		return ret;
  	
  	// at this point, we're down to comparing paths...
  	String thisDir = getDirectory(), thatDir = o.getDirectory();
  	String thisName = getFile(), thatName = o.getFile();
  	if (thisDir==null || thatDir == null) { // one or both of directories is not defined
  		if (thisName!=null && thisName.length()>0 && thatName!=null && thatName.length()>0) 
  			return thisName.compareTo(thatName);
  	}
  	else
  		return getPath().compareTo(o.getPath()); // compare whole paths
  	return 0;
  }

  @Override
	public int hashCode () {
    return toString().hashCode(); //using port if iffy because a URL can be unresolved and then become resolved
                 //this is compensated for in SocialCommitmentsStore.getInnerMap() and .getVector()
    //return this.getPath().hashCode (); //and sometimes we don't have a path
  }
  
  // Freshness section
  // The following code implements the 3-time-ping-fail-strikeout rule in CooperationDomain
  // The code was moved here from a wrapper class in CooperationDomain because it's needed inside the
  // URLDescriptor for easy pretty-color-fontifying
  // Note that the protocol for "freshness" is defined in CooperationDomain, this is just the
  // implementation for the URLDescriptor, BUT the CooperationDomainListModel also depends on this protocol
   
  // This keeps track of the URLDescriptor ping strike-outs
  /**
	 */
  private int mark = 0;
  
  /*
   * Call a strikeout on this URLDescriptor
   * @return The number of strike-outs
   */
  public int mark() {
    return ++mark;
  }

  /*
   * Returns the number of strike-outs of this URLDescriptor
   */
  /**
	 * @return
	 */
  public int getMark() {
    return mark;
  }

  /*
  * Resets the number of strike-outs for this URLDescriptor to 0
  */
  public void resetMark() {
  	if (!isWithdrawn())
  		mark = 0;
  }
  
  //This switch says whether the agent represented by this URLDescriptor is withdrawn from a CooperationDomain
  //If it's true, then this URLDescriptor is no longer valid and should be removed
  //Currently used by CooperationDomainListModel for fontifying.
  /**
	 */
  private boolean withdrawn = false;
  
  // This is the local time, in milliseconds, at which the URLDescriptor became invalidated
  private long timeWithdrawn;

  /**
	 * @return
	 */
  public boolean isWithdrawn() {
  	return withdrawn;
  }
  
  public long timeOfWithdrawal() {
  	return timeWithdrawn;
  }
  
  public void withdraw() {
  	timeWithdrawn = System.currentTimeMillis();
  	withdrawn = true;
  }

  /**
   * Builds a stack of {@link URLDescriptor}s from the <em>via</em>
   * data item in this url
   * @return a stack of {@link URLDescriptor}s.
   */
  public Stack<URLDescriptor> getViaStack() {
		URLDescriptor temp = null;
		Stack<URLDescriptor> stack = new Stack<URLDescriptor> ();
		try {
			temp = new URLDescriptor (getDataValue ("via"));
			do {
				stack.push (temp);
				try {
					temp = new URLDescriptor (temp.getDataValue ("via"));
					temp.deleteDataValue ("via");
				} catch (URLDescriptorException e) {}
			} while (temp.hasDataValue ("via"));
		} catch (URLDescriptorException e) {}
    return stack;  	
  }
  
  /**
   * Replaces the <em>via</em> data item with one constructed from the
   * <em>stack</em> parameter.
   * @param stack the stack from which to construct the new <em>via</em> data item.
   */
  public void putViaStack(Stack<URLDescriptor> stack) {
		URLDescriptor top = null, temp=null;
		while (! stack.empty ()) {
			temp = stack.pop ();
			if (top!=null) temp.setDataValue ("via", top.toString ());
			top = temp;
		}
		if (top==null) deleteDataValue("via"); 
		else           setDataValue ("via", top.toString ());
  }
  
  /**
   * Appends the <em>endURL<em> url to the end of the <em>via</em> data item in the
   * url ("?via=").
   * @param endURL the url to append to the <em>via</em> data item.
   */
  public void pushViaAtEnd (URLDescriptor endURL) {
  	if (endURL==null) return;
		Stack<URLDescriptor> stack = getViaStack();
	  stack.push(endURL);
	  putViaStack(stack);
	}
	
	/**
	 * Removes the last url from the <em>via</em> data item in the url ("?via=").
	 * @return the url removed, or null if there was no vi<em>via</em>a item.
	 */
	public URLDescriptor popViaAtEnd () {
		Stack<URLDescriptor> stack = getViaStack();	
		URLDescriptor ret = stack.empty()?null:stack.pop();
		putViaStack(stack);
		return ret;
	}
	
  /**
   * Checks if the <em>via</em> data item contains the parameter <em>url</em>.
   * @param url the url to search for
   * @return true iff the <em>via</em> contains the parameter url.
   */
  public boolean containsVia (URLDescriptor url) {
		Stack<URLDescriptor> stack = getViaStack();
	  for (URLDescriptor u: stack) {
	  	if (u.equals(url)) return true;
	  }
	  return false;
	}
  
  /**
   * Lisp operator: (NEW-URL STRING)<br>
   */
  @SuppressWarnings("unused")
	private static final CasaLispOperator NEW_URL =
    new CasaLispOperator("NEW-URL", "\"!Attempt to construct a URL from a string representation.\" "+
    						"STRING \"!The URL of the cooperation domain to join.\" ", TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
    {
      @Override
			public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
        try {
					URLDescriptor url = new URLDescriptor((String)params.getJavaObject("STRING"));
					return new StatusObject<URLDescriptor>(0,url);
				} catch (URLDescriptorException e) {
					return new Status(-6,"Bad URL: "+e.toString());
				}
    }
  };
  
  /**
   * Lisp operator: (AGENT-IDENTIFIER)<br>
   */
  @SuppressWarnings("unused")
	private static final CasaLispOperator AGENT_IDENTIFIER =
    new CasaLispOperator("AGENT-IDENTIFIER", "\"!return a URL based on the FIPA-like agent-identifier expression.\" "+
    						"&KEY NAME \"@java.lang.String\" \"!The name (file) of the agent (NOT including the path or type).\" "
    						+"TYPE  \"@java.lang.String\" \"!The type (path) of the agent.\" "
    						+"PORT  \"@java.lang.Integer\" \"!The port.\" "
    						+"HOST  \"@java.lang.String\" \"!The host.\" "
    						+"USER  \"@java.lang.String\" \"!The user.\" "
    						+"DATA  \"@java.lang.String\" \"!The data.\" "
    						+"FRAGMENT  \"@java.lang.String\" \"!The fragment.\" "
    						, TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
    {
      @Override
			public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
      	String name = params.getJavaObject("NAME", String.class);
      	String type = params.getJavaObject("TYPE", String.class);
      	if (type!=null && !type.endsWith("/"))
      		type += "/";
      	Integer port = params.getJavaObject("PORT", Integer.class);
      	String host = params.getJavaObject("HOST", String.class);
      	String user = params.getJavaObject("USER", String.class);
      	String data = params.getJavaObject("DATA", String.class);
      	String fragment = params.getJavaObject("FRAGMENT", String.class);
      	
      	String path = (type==null?"":type)+(name==null?"":name);
      	if (path.length()==0)
      		path = null;
      	URLDescriptor url = new URLDescriptor(user, null, port==null?0:port, path, data, fragment);
      	if (host!=null) {
      		try {
						url.setHost(host);
					} catch (URLDescriptorException e) {
						throw new LispException("Bad host in agent-identifier expression", e);
					}
      	}
      	return new StatusObject<URLDescriptor>(url);
      }
    };

    /**
     * Lisp operator: (URLS.GET)<br>
     * Attempt to join the cooperation domain specified by the parameter URL.
     */
    @SuppressWarnings("unused")
  	private static final CasaLispOperator URLS__GET =
    		new CasaLispOperator("URLS.GET", "\"!return a Cons list of all known URLs as Java URLDescriptors.\" "
    				+"&KEY STRING \"!If this is non-NIL, the returned Cons list will be URL Strings.\" "
    				+"FORMAT \"!If this is non-null, the return will be a string containing the String urls separated by newlines.\" "
    				, TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
    {
    	@Override
    	public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
    		URLDescriptor[] urls = getKnownURLs();
    		boolean asString = params.getJavaObject("STRING")!=null || params.getJavaObject("STRING")!=null;
    		boolean asFormat = params.getJavaObject("FORMAT")!=null || params.getJavaObject("FORMAT")!=null;
    		
    		if (asFormat) {
    			StringBuilder b = new StringBuilder();
      		for (URLDescriptor url: urls) {
      			b.append(url.toString()).append("?channel=").append(url.getChannel()).append('\n');
       		}
      		return new StatusObject<LispObject>(0, new SimpleString(b.toString()));
    		}
    		else {
      		Cons cons = null;
      		LispObject beginning = org.armedbear.lisp.Lisp.NIL;
    		for (URLDescriptor url: urls) {
    			Cons c = new Cons(asString?new SimpleString(url.toString()):new JavaObject(url), org.armedbear.lisp.Lisp.NIL);
    			if (cons==null) {
    				cons = c;
    				beginning = c;
    			}
    			else {
    				cons.cdr = c;
    				cons = c;
    			}
    		}
    		return new StatusObject<LispObject>(0, beginning);
    		}
    	}
    };
    
  /**
   * Lisp operator: (NEW-URL STRING)<br>
   * Attempt to join the cooperation domain specified by the parameter URL.
   */
  @SuppressWarnings("unused")
	private static final CasaLispOperator URL__GET =
  		new CasaLispOperator("URL.GET", "\"!return the url as a string, or extract a component; up to ONE key is allowed (result is undefined if more than one key)\" "
  				+"URL \"!The URL as a URL object or a string.\" "
  				+"&KEY FILE \"!The FILE or NAME component.\" "
  				+"NAME \"!The FILE or NAME component.\" "
  				+"HOST \"!The DATA component.\" "
  				+"DATA \"!The DATA component.\" "
  				+"DIRECTORY \"!The DIRECTORY component.\" "
  				+"FRAGMENT \"!The FRAGMENT component.\" "
  				+"HOSTANDPORT \"!The HOSTANDPORT component.\" "
  				+"LACPORT \"!The LACPORT component.\" "
  				+"PATH \"!The PATH component.\" "
  				+"PORT \"!The PORT component.\" "
  				+"SHORTESTNAME \"!The SHORTESTNAME component.\" "
  				, TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
  {
  	@Override
  	public Status execute(TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
  		URLDescriptor url;
  		Object obj;
  		try {
  			obj = params.getJavaObject("URL");
  			if (obj instanceof URLDescriptor) {
  				url = (URLDescriptor)obj;
  			}
  			else {
  				url = make((String)obj);	
  			}
  		} catch (Throwable e) {
  			return new Status(-6,"Bad URL: "+e.toString());
  		}
  		if (params.getJavaObject("FILE")!=null || params.getJavaObject("NAME")!=null) {
  			return new StatusObject<String>(0,"success",url.getFile());
  		}
   		if (params.getJavaObject("HOST")!=null) {
  			return new StatusObject<String>(0,"success",url.getHostString());
  		}
   		if (params.getJavaObject("DATA")!=null) {
  			return new StatusObject<String>(0,"success",url.getData());
  		}
   		if (params.getJavaObject("DIRECTORY")!=null) {
  			return new StatusObject<String>(0,"success",url.getDirectory());
  		}
   		if (params.getJavaObject("FRAGMENT")!=null) {
  			return new StatusObject<String>(0,"success",url.getFragment());
  		}
   		if (params.getJavaObject("HOSTANDPORT")!=null) {
  			return new StatusObject<String>(0,"success",url.getHostAndPort());
  		}
   		if (params.getJavaObject("LACPORT")!=null) {
  			return new StatusObject<String>(0,"success",":"+url.getLACport());
  		}
   		if (params.getJavaObject("PATH")!=null) {
  			return new StatusObject<String>(0,"success",url.getPath());
  		}
   		if (params.getJavaObject("PORT")!=null) {
  			return new StatusObject<String>(0,"success",":"+url.getPort());
  		}
   		if (params.getJavaObject("SHORTESTNAME")!=null) {
  			return new StatusObject<String>(0,"success",url.getShortestName());
  		}
   		return new StatusObject<String>(0,"success",url.toString());
  	}
  };
  
  private Channel channel = null;
  
  public void setChannel(Channel channel) {
  	this.channel = channel;
  }
  
  Channel getChannel() {
  	return channel;
  }
  
  public boolean hasChannel() {
  	return channel!=null;
  }
  
  public Status sendMessage(final AbstractProcess sender, MLMessage message) throws IOException, URLDescriptorException {
  	assert sender!=null;
  	assert message!=null;
  	assert message.getReceiver().equals(this);
  	
    if (channel == null) {
    	final URLDescriptor ThisToURL = this;
    	class MyRunnable implements Runnable {
    		public boolean done = false;
    		public IOException exception = null;
				@Override
				public void run() {
					try {
						Channel channel2 = new TCPChannel(sender, ThisToURL, sender.getSocketServer());
			    	ThisToURL.setChannel(channel2);
					} catch (IOException e) {
						sender.println("error", "AbstractProcess.sendMessage_primitive(): Unexpected exception creating new TCPChannel", e);
						exception = e;
					}
					done = true;
				}
    	};
    	// we need to run the code in MyRunnable.run() in a thread that isn't going to be interrupted 
    	// (it will fail if interrupted). But we also need to sync with it when it's done.
    	MyRunnable r = new MyRunnable();
    	Thread t = sender.makeSubthread(r);
    	t.start();
    	while (!r.done) {
    		try {
    			t.join();
    		} catch (InterruptedException e) {}
    	}
    	if (r.exception != null)
    		throw(r.exception);
    }
    
    channel.sendMessage(sender, message);
  	return new Status(0);
  }
  
  
		
}

/**
 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
class URLAndInt {
  /**
	 */
  URLDescriptor url;
  int i;
  URLAndInt(URLDescriptor url, int i) {this.url=url; this.i=i;}
}

