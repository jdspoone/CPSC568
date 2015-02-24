/**
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. 
 * Permission to use, copy, modify, distribute and sell this software and its 
 * documentation for any purpose is hereby granted without fee, provided that the 
 * above copyright notice appear in all copies and that both that copyright notice 
 * and this permission notice appear in supporting documentation.  The  Knowledge 
 * Science Group makes no representations about the suitability of  this software 
 * for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
package casa;

//import org.armedbear.lisp.LispObject;
import casa.abcl.CasaLispOperator;
import casa.abcl.ParamsMap;
import casa.ui.AgentUI;
import casa.util.Trace;

import org.armedbear.lisp.JavaObject;
import org.armedbear.lisp.SimpleString;

/**
 * This class holds the main() command-line entry point for CASA.   It simply runs an simple  {@link TransientAgent}  using it's text interface. If there are arguments on the command line, it passes them to the agent through it's  {@link TransientAgent#executeCommand(String,AgentUI)  executeCommand()}  method and requests the agent exit.  If there are no command line arguments, it allows the agent to wait for command prompts and merely waits for the agent to exit. <p> The agent does not register with the LAC, so no LAC need be started to use the command line (but the command line is typically used to start a LAC). <p> The most common use of this is to use "read file <filename>" as the arguments on the command line. 
 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @deprecated {@link CASAProcess} takes over the functionality of this class, particularly {@link CASAProcess#main(String[])}.
 */
@Deprecated
public class CASACommandLine2 {
	
	static {
		casa.abcl.Lisp.loadClass("casa.abcl.Lisp");
	}
  static CasaLispOperator loadActPlease = Act.ACT2STRING;
	
//	static {casa.abcl.Lisp.loadClass("org.armedbear.lisp.LispObject");} //attempt to load this before anything else...
	/**
	 */
	static TransientAgent agent = null;
	static boolean trace = false;
	static boolean text = false;

	public static void executeCommandLineAgent(String s) {
		String[] args = s.split("\\s+");
		main(args);
	}
	
	/**
	 * @param args
	 * @deprecated Use {@link CASAProcess#main(String[])} instead
	 */
	@Deprecated
	public static void main(String[] args) {
		CASAProcess.main(args);
		return;
//		System.setProperty("apple.laf.useSceenMenuBar", "true");
//		
//		boolean autoStart=false, suppressAutoStart=false, help=false;
//		int lacPort = 9000;
//
//		//if (args.length>0 && args[0].length()>1 && args[0].charAt(0)=='-') {
//		int i;
//		for (i=0; args.length>i && args[i].length()>1 && args[i].charAt(0)=='-'; i++) {
//			String p = args[i].substring(1);
//			if ("help".equalsIgnoreCase(p)) {
//				help = true;
//				continue;
//			}
//			if ("lac".equalsIgnoreCase(p)) {
//				autoStart = true;
//				try {
//					autoStart = true;
//					if (i+1 < args.length)
//						lacPort = Integer.parseInt(args[++i]);
//				} catch (Throwable e) {
//					--i; // set i back to it's original position, and accept default LAC port
//				}
//				continue;
//			}
//			for (int j=p.length()-1; j>=0; j--) {
//				char c = p.charAt(j);
//				switch (c) {
//				case 't': //trace
//					trace = true;
//					break;
//				case 'T': //text
//					text = true;
//					break;
//				case 'L':
//					autoStart = true;
//					break;
//				case 'l':
//					suppressAutoStart = true;
//					break;
//				case '?':
//					help = true;;
//					break;
//				default:
//					fatalError("Unrecognized qualifer '"+c+"' in command line", true);
//				}
//			}
//		}
//		if (autoStart && suppressAutoStart)
//			fatalError("Qualifiers (L or LAC) and l cannot be used together",true);
//		if (help) 
//			fatalError(null, args.length==1);
//		
//		String temp[] = new String[args.length-i]; 
//		for (int j=i; j<args.length; j++) temp[j-i] = args[j];
//		args = temp;
//
//		final StringBuffer buf = new StringBuffer();
//		for (String s: args) buf.append(s).append(" ");
//		BufferedAgentUI uiTemp = new BufferedAgentUI();
//		Status stat = startAgent(uiTemp);
//		if (stat.getStatusValue()!=0) DEBUG.PRINT("startAgent() failed: "+stat+", "+uiTemp.result());
//		assert agent!=null;
//		boolean dialogMode = (buf.length()==0);
//		
//		// Create the UI object to either interact with the user, or to execute the command line and exit
//		final AgentUI ui;
//		if (dialogMode) {
//			if (text)
//			  ui = new casa.ui.TextInterface(agent,null,true);
//			else {
//				TransientAgentInternalFrame f = new casa.ui.TransientAgentInternalFrame(agent, "casa", new JFrame());
//    		//f.setSize(160, 250);
//    		//f.setName (getName ());
//    		//f.setOpaque (true);
//    		f.pack ();
//    		//f.setIcon (true);
//    		f.show ();
//    		ui = f;
//			}
//		}
//		else
//			ui = new casa.ui.TextInterface(agent,null,Thread.currentThread());
//		assert ui!=null;
//		
//		Socket socket;
//		try {
//			socket = new Socket((String)null, lacPort);
//		} catch (Throwable e1) {
//			socket = null;
//		}
//		if (socket==null) {
//			boolean startLAC;
//			if (autoStart) startLAC = true;
//			else if (suppressAutoStart) startLAC=false;
//			else {
//			  String ret = ui.ask("No LAC detected on port "+lacPort+", would you like to start one?", "No LAC detected on port "+lacPort+".  Most CASA applications require one.", AgentUI.TYPE_BOOLEAN, "true");
//			  startLAC = (ret==null)?false:ret.equals("true");
//			}
//			if (startLAC) { // start a LAC
//			  try {
//			  	ParamsMap p = new ParamsMap();
//			  	p.putJavaObject("TYPE", "casa.LAC", true);
//			  	p.putJavaObject("NAME", "LAC", true);
//			  	p.putJavaObject("PORT", Integer.toString(lacPort), true);
//			  	p.putJavaObject("PROCESS", "CURRENT", true);
//			  	p.putJavaObject("MARKUP", "KQML", true);
//			  	TransientAgent.NewAgentLispCommand com = new TransientAgent.NewAgentLispCommand("NEW-AGENT");
//			  	Status st = com.execute(null,p,ui,null);
//			  	LAC lac = null;
//			  	if (st instanceof StatusObject<?>) {
//			  		Object obj = ((StatusObject<?>)st).getObject();
//			  		if (obj instanceof TransientAgent) {
//			  			lac = (LAC)obj;
//			  		}
//			  	}
////					LAC lac = new LAC(p, ui);
////					lac.start();
//			  	if (lac==null)
//			  		ui.println("LAC startup FAILED: "+st);
//			  	else {
//			  		while (!lac.isInitialized()) {
//			  			ui.println("Waiting for LAC to start...");
//			  			Thread.sleep(100);
//			  		}
//						ui.println("LAC started on port "+lacPort);
//			  	}
//				} catch (Exception e) {
//					ui.println("Exception starting LAC: "+e.toString());
//				}
//			}
//		} else {
//			try {
//				socket.close();
//			} catch (IOException e1) {
//			}
//		}
//
//    //execute the command line if we had one
//    if (!dialogMode) {
////    	new Thread("casa command line execution") {
////    		public void run() {
//    			agent.executeCommand(buf.toString(), ui);
//    			agent.exit();    			
////    		}
////    	}.start();
//		}
//    
//    // wait around for the thread do die
//    ExitChecker.startExitCheck();
//    //Runtime rt = Runtime.getRuntime();
//    Thread rt = Thread.currentThread();
//		while (agent.isAlive()) {
//			synchronized (rt) {
//				try {
//					rt.wait(500);
//				} catch (InterruptedException e) {
//				}
//			}
//		}
	}
	
	private static void fatalError(String s, boolean fatal) {
		Trace.log("error", (s!=null?(s+". "):"")+"Usage:\n"+
				"  casa [-lLtT?] [-LAC [<port>]] [-help] [<lisp-command>]\n"+
				"    where:\n"+
				"      l: Suppress automatically stating a LAC if there isn't one\n"+
				"      L: Automatically start a LAC at port 9000 if there isn't one\n"+
				"      LAC [<port>]: Automatically start a LAC at port <port> if there isn't one (defaults to 9000)\n"+
				"      t: turn tracing-to-file on\n"+
				"      T: Use a text interface if there's no command on the command line\n"+
				"      ?, help: Prints this help text\n"+
				"      <lisp-command>: any legal agent run-time command (runs dialogue mode if this is missing)"
				, Trace.OPT_SUPPRESS_AGENT_LOG);
	  if (fatal)
	  	System.exit(-1);
	}

	private static Status startAgent(AgentUI ui) {
		try {
			ParamsMap params = new ParamsMap();
			params.put("PORT", new Integer(-1000), new JavaObject(new Integer(-1000)), false);
			params.put("NAME", "casa", new SimpleString("casa"), false);
			params.put("LACPORT", new Integer(-1), new JavaObject(new Integer(-1)), false);
			int traceSpec = 10; //AbstractProcess.TRACE_TO_FILE|(trace?AbstractProcess.TRACE_ON:AbstractProcess.TRACE_OFF);
			params.put("TRACE", new Integer(traceSpec), new JavaObject(new Integer(traceSpec)), false);
			params.put("TRACETAGS", "error,warning,info,msg", new SimpleString("error,warning,info,msg"), false);
			params.put("STRATEGY", "sc3", new SimpleString("sc3"), false);
			agent = new TransientAgent (params, ui);
			agent.start();
			for (int i=20; i!=0 && !agent.isInitialized(); i--) {
				try {
					Thread.sleep(500);
				}
				catch (InterruptedException ex2) {
				}
			}
		}
		catch (Exception ex) {
			return new Status( -2,
			"CASACommandLine.startAgent: "+ex);
		}
		if (!agent.isInitialized()) {
			return new Status( -5,
			"CASACommandLine.startAgent: Failed initialize the 'casa' sub-agent");
		}
		else { //we have a sub-agent
			return new Status(0);
		}
	}
	
//	private static class generateCommandScript extends Command {
//		static String getSpec() {
//			return 
//			   "generate command script | filename(type=string; required; valuerequired; default=casa; help=\"name of the command file [default=casa]\") " +
//           "?(help=\"write out a CASA executable command script file based on the current configuation\")";
//		}
//		
//    @Override public Status execute(String line, Map<String, String> params, AgentUI ui) {
//    	String filename = params.get("filename");
//     	String filesep = System.getProperty("file.separator");
//     	String pathsep = System.getProperty("path.separator");
//     	String os = System.getProperty("os.name");
//    	if (filesep.equals("\\")) { // must be windows
//    		if (filename.indexOf('\\')==-1) filename = filename+".bat";
//    	}
//    	String classpath = System.getProperty("java.class.path");
//    	if (classpath.indexOf(pathsep)==-1) { //there is only one path given in classpath: assume this is a jar
//    		File f = new File(classpath);
//    		classpath = f.getAbsolutePath(); //expand the classpath the the full absolute path
//    	}
//    	try {
//				FileWriter f = new FileWriter(filename);
//				f.write(System.getProperty("java.home")
//						+filesep+"bin"+filesep+"java -classpath "
//						+classpath
//						+" casa.CASACommandLine2 $1 $2 $3 $4 $5 $6 $7 $8 $9\n");
//				f.close();
//      	if (filesep.equals("\\")) { // must be windows
//      	}
//      	else { // not windows, let's assume UNIX (Mac, Linux, etc)
//					Runtime.getRuntime().exec("chmod ugo+x "+filename);
//      	}
//				ui.print("Created file "+filename+(char)-1);
//			} catch (IOException e) {
//				ui.print(e.toString()+"\n"+(char)-1);
//			}
//      return new Status(0);
//    }		
//	}
	
	
}
