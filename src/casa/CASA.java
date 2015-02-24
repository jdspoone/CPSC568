/* <p>Title: CASA Agent Infrastructure</p>
 * <p>Copyright: Copyright (c) 2003-2014, Knowledge Science Group, University of Calgary. 
 */
package casa;

import casa.CASAProcess.ProcessInfo;
import casa.abcl.ParamsMap;
import casa.ui.AbstractInternalFrame;
import casa.ui.AgentUI;
import casa.ui.BufferedAgentUI;
import casa.util.Pair;
import casa.util.Trace;

import java.awt.Container;
import java.io.IOException;
import java.net.Socket;
import java.util.Vector;

import javax.swing.JTabbedPane;

import org.armedbear.lisp.JavaObject;
import org.armedbear.lisp.SimpleString;

/**
 *
 * <p><b>Title:</b> CASA Agent Infrastructure</p>
 * <p><b>Copyright: Copyright (c) 2003-2014, Knowledge Science Group, University of Calgary.</b> 
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  
 * The  Knowledge Science Group makes no representations about the suitability
 * of  this software for any purpose.  It is provided "as is" without express
 * or implied warranty.</p>
 * <p><b>Company:</b> Knowledge Science Group, Department of Computer Science, University of Calgary</p>
 * @version 0.9
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class CASA {

	/**
	 * 
	 */
	private CASA() {
	}

	static {
		// Apple: set the system property to use a top-of-screen menu bar and the about menu to be "CASA"
		System.setProperty("apple.laf.useSceenMenuBar", "true");
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "CASA");

		casa.abcl.Lisp.loadClass("casa.abcl.Lisp");
	}


	/**
	 * Usage:<br>
	 * <pre>
	 * casa [-lLtT?] [-LAC [<em>port</em>]] [-NOLAC [<em>port</em>]] [-PROCESS [<em>port</em>]] [-NOPROCESS] [-HELP] [<em>lisp-command</em>]<br>
	 *   where:
	 *    l: Suppress automatically stating a LAC if there isn't one (same as -NOLAC).
	 *    L: Automatically start a LAC at port 9000 if there isn't one (same as -LAC).
	 *    t: turn tracing-to-file on.
	 *    T: Use a text interface if there's no command on the command line.
	 *    N: supress starting an interface if there's no command on the command line.
	 *    ?, HELP: Prints this help text.
	 *    LAC [<em>port</em>]: Automatically start a LAC at port <port> if there isn't one (defaults to 9000).
	 *    NOLAC [<em>port</em>]: Do not start a LAC, but expect a LAC at port <port> (defaults to 9000).
	 *    PROCESS [<em>port</em>]: Automatically start a CASAProcess at port <port> (defaults to 9010).
	 *    NOPROCESS [<em>port</em>]: Do not start a PROCESS, but run a simple agent that executes the <lisp-command>.
	 *    TAGS [<em>tag-specifier</em>]: Specify the tags for non-agent (process-global) logging.
	 *    DAEMON: Mark this process such that no agent should start a GUI. Implies N.
	 *    KILLONLOWMEMORY: If the process runs low on memory, kill the process. Useful for daemons.
	 *    <em>lisp-command</em>: any legal agent run-time command (runs dialogue mode if this is missing).
	 * </pre>
	 *  -L, -LAC, -PROCESS, and -NOPROCESS are mutually exclusive.
	 *  If none of -L, -LAC, -PROCESS, and -NOPROCESS are present then if a LAC exists at -NOLAC (or 9000),
	 *   then a CASAProcess is started at 9010 or above, otherwise a LAC is started.
	 *  Qualifiers can be abbreviated to the shortest unique truncation.
	 * @param args Standard command-line args as described above
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		commandArgs = args;
		
		Trace.log(null, "CASA build: "+AbstractInternalFrame.getBuildTime(), Trace.OPT_COPY_TO_SYSOUT|Trace.OPT_SUPPRESS_HEADER_ON_SYSOUT);
		Trace.captureSysout();
		
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		boolean startLAC = false, noStartLAC = false, help = false;
		int lacPort = 9000;
		boolean startProcess = false, noStartProcess = false;
		int processPort = -9010;
		
		// Process command-line qualifiers
		int i;
		for (i = 0; args.length > i && args[i].length() > 1
				&& args[i].charAt(0) == '-'; i++) {
			String p = args[i].substring(1);
			int length = p.length();
			if (length <= 4
					&& "help".substring(0, Math.max(length, 1)).equalsIgnoreCase(p)) {
				help = true;
				continue;
			}
			if (length <= 4
					&& "tags".substring(0, Math.max(length, 2)).equalsIgnoreCase(p)) {
					if (i + 1 < args.length)
						Trace.setTags(args[++i]);
				continue;
			}
			if (length <= 3
					&& "lac".substring(0, Math.max(length, 2)).equalsIgnoreCase(p)) {
				startLAC = true;
				try {
					if (i + 1 < args.length)
						lacPort = Integer.parseInt(args[++i]);
				} catch (Throwable e) {
					--i; // set i back to it's original position, and accept default LAC
					// port
				}
				continue;
			}
			if (length <= 5
					&& "nolac".substring(0, Math.max(length, 3)).equalsIgnoreCase(p)) {
				noStartLAC = true;
				try {
					if (i + 1 < args.length)
						lacPort = Integer.parseInt(args[++i]);
				} catch (Throwable e) {
					--i; // set i back to it's original position, and accept default LAC
					// port
				}
				continue;
			}
			if (length <= 6
					&& "daemon".substring(0, Math.max(length, 2)).equalsIgnoreCase(p)) {
				ProcessInfo.daemon = true;
				continue;
			}
			if (length <= 7
					&& "process".substring(0, Math.max(length, 1)).equalsIgnoreCase(p)) {
				startProcess = true;
				try {
					if (i + 1 < args.length)
						processPort = Integer.parseInt(args[++i]);
				} catch (Throwable e) {
					--i; // set i back to it's original position, and accept default LAC
					// port
				}
				continue;
			}
			
			if (length <= 9
					&& "noprocess".substring(0, Math.max(length, 3)).equalsIgnoreCase(p)) {
				noStartProcess = true;
				continue;
			}
			if (length <= 15
					&& "killonlowmemory".substring(0, Math.max(length, 4)).equalsIgnoreCase(p)) {
				startKillOnOutOfMemory();
				continue;
			}
			for (int j = p.length() - 1; j >= 0; j--) {
				char c = p.charAt(j);
				switch (c) {
				case 't': // trace
					trace = true;
					break;
				case 'T': // text
					text = true;
					break;
				case 'N': // text
					noInterface = true;
					break;
				case 'L':
					startLAC = true;
					break;
				case 'l':
					noStartLAC = true;
					break;
				case '?':
					help = true;
					;
					break;
				default:
					fatalError("Unrecognized qualifer '" + c + "' in position "+j+" in arg "+i+"("+args[i]+") in command line", true);
				}
			}
		}
		
		if (ProcessInfo.daemon)
			noInterface = true;
			
		if (startLAC && noStartLAC)
			fatalError("Qualifiers -LAC and -NOLAC cannot be used together", true);
		if (startLAC && startProcess)
			fatalError("Qualifiers -LAC and -PROCESS cannot be used together", true);
		if (startLAC && noStartProcess)
			fatalError("Qualifiers -LAC and -NOPROCESS cannot be used together", true);
		if (startProcess && noStartProcess)
			fatalError("Qualifiers -PROCESS and -NOPROCESS cannot be used together", true);
		if (help)
			fatalError(null, args.length == 1);

		//collect the non-dashed parameters in command[]
		String command[] = new String[args.length - i];
		for (int j = i; j < args.length; j++)
			command[j - i] = args[j];
		args = command;

		// finished processing command-line qualifiers.
		// get the agent command from the command-line.
		final StringBuffer commandArgs = new StringBuffer();
		for (String s : command) {
			commandArgs.append(s).append(" ");
		}
		boolean dialogMode = (commandArgs.length() == 0);
		
		// if nothing specified, figure out appropriate default behaviour
		if (!noStartLAC && !startLAC && !startProcess && !noStartProcess && dialogMode) {
			if (isPortTaken(lacPort)) {
				startProcess = true;
				Trace.log(null, "Neither -[NO]LAC nor -[NO]PROCESS specified on command line and a LAC on port "+lacPort+" detected, so starting a regular process.", Trace.OPT_COPY_TO_SYSOUT|Trace.OPT_SUPPRESS_HEADER_ON_SYSOUT);
			}
			else {
				startLAC = true;
				Trace.log(null, "Neither -[NO]LAC nor -[NO]PROCESS specified on command line and no LAC on port "+lacPort+" detected, so starting a LAC.", Trace.OPT_COPY_TO_SYSOUT|Trace.OPT_SUPPRESS_HEADER_ON_SYSOUT);
			}
		}


		BufferedAgentUI uiTemp = new BufferedAgentUI();
		

		LAC lac = null;
		CASAProcess process = null;
		TransientAgent agent=null;
		if (startLAC) {
			Pair<String, String> params[] = text?new Pair[]{new Pair<String,String>("interface","text")}:new Pair[]{};
			lac = startLAC(uiTemp, lacPort, params);
			if (lac != null) {
				agent = lac;
				CASAProcess.instance = lac;
				ProcessInfo.lac = lac;
				ProcessInfo.lacPort = lac.getPort();
//				ProcessInfo.process = lac;
//				ProcessInfo.processPort = ProcessInfo.lacPort;
			} else {
				Trace.log("error", "CASAProcess.main(): Failed to start a LAC agent, attempting to start a process agent instead...", Trace.OPT_COPY_TO_SYSERR|Trace.OPT_SUPPRESS_HEADER_ON_SYSOUT);
				process = startProcess(uiTemp, processPort, lacPort);
				if (process!=null) {
					agent = process;
					CASAProcess.instance = process;
//					ProcessInfo.process = process;
//					ProcessInfo.processPort = process.getPort(); 
				}
				else {
					Trace.log("error", "CASAProcess.main(): Failed to start a process agent.", Trace.OPT_COPY_TO_SYSERR|Trace.OPT_SUPPRESS_HEADER_ON_SYSOUT);
				}
			}
		} else if (startProcess) {
			process = startProcess(uiTemp, processPort, lacPort);
			if (process!=null) {
				agent = process;
				CASAProcess.instance = process;
//				ProcessInfo.process = process;
//				ProcessInfo.processPort = process.getPort();
			}
			else {
				Trace.log("error", "CASAProcess.main(): Failed to start a process agent.", Trace.OPT_COPY_TO_SYSERR|Trace.OPT_SUPPRESS_HEADER_ON_SYSOUT);
			}
		} else { // noStartProcess
			Pair<?,?> v[] = new Pair<?,?>[]{}; 
			if (text)
				v = new Pair<?,?>[]{new Pair<String, String>("INTERFACE", "TEXT")};
			agent = startAgent(uiTemp, (Pair<String,String>[])v);
		}
		if (agent==null) {
			Trace.log("error", "CASAProcess.main(): Failed to start agent: "+(uiTemp!=null?uiTemp.result():"reason unknown")+".", Trace.OPT_COPY_TO_SYSERR|Trace.OPT_SUPPRESS_HEADER_ON_SYSOUT);
		}
		assert agent != null;

//		if (!noInterface && (dialogMode || startLAC || startProcess)) { // dialogMode: Give the agent the appropriate UI to carry on a dialog (text or windowed)
//			if (text) {
//				agent.putUI(agent.makeDefaultTextInterface(args));
//			}
//			else {
//				AgentUI ui = null;
//				long timeout = System.currentTimeMillis()+3000;
//				while (ui==null && System.currentTimeMillis()<timeout) 
//					ui = agent.getUI();
//				final TransientAgent finalAgent = agent;
//				AbstractInternalFrame.runInEventDispatchThread(new Runnable() {
//					@Override
//					public void run() {
//						AgentUI ui = finalAgent.getUI();
//						if (ui!=null) {
//							if (ui instanceof AbstractInternalFrame) {
//								setFocusToCommandPanel((AbstractInternalFrame)ui);
//							}
//						}
//						else {
//						TransientAgentInternalFrame f = new casa.ui.TransientAgentInternalFrame(
//								finalAgent, finalAgent.getURL().getFile(), new JFrame());
//						f.pack();
//						f.show();
//						finalAgent.putUI(f);
//						setFocusToCommandPanel(f);
//						}}}
//				, true);
//			}
//		} 
		if (!dialogMode) { // !dialogMode: have the agent execute the command line and exit
//			AgentUI ui = agent.getUI();
//			if (ui==null) { 
//				ui = new casa.ui.TextInterface(agent, null, Thread.currentThread());
//				agent.putUI(ui);
//			}
//			assert ui != null;
			long sleeptime = 500;
			while (!agent.isInitialized()) {
				agent.println(null, "Waiting for agent "+agent.getURL()+" to finish initializing...");
				try {
					Thread.sleep(sleeptime+=1000);
				} catch (InterruptedException e) {}
			}
			agent.println(null, "Executing command: "+commandArgs.toString());
			BufferedAgentUI bui = new BufferedAgentUI();
			agent.executeCommand(commandArgs.toString(), bui);
			agent.println(null, "Result:\n"+bui.result());
			if (!startProcess && !startLAC)
				agent.exit();
		}
		else {
			if (noStartProcess && noStartLAC)
			  agent.println(null, "Entering dialog mode...", Trace.OPT_COPY_TO_SYSOUT);
		}

		// wait around for the thread do die
		ExitChecker.startExitCheck();
		while (agent.isAlive()) {
			try {
				agent.join();
			} catch (InterruptedException e1) {
			}
		}
	}
	
	// ***************************************************************/
	// FROM CASACommandLine2
	// ***************************************************************/
	static {
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		casa.abcl.Lisp.loadClass("casa.abcl.Lisp");
	}

	/**
	 */
	static boolean trace = false;

	static boolean text = false;
	
	static boolean noInterface = false;

	public static void executeCommandLineAgent(String s) {
		String[] args = s.split("\\s+");
		main(args);
	}
	
	static String[] commandArgs = null;
	
	private static void setFocusToCommandPanel(AbstractInternalFrame frame) {
		Container container = frame.getCommandPanel().getParent();
		if (container instanceof JTabbedPane) {
			JTabbedPane tabbedPane = (JTabbedPane)container;
			int i;
			for (i=0; i<tabbedPane.getTabCount(); i++) {
				if ("command".equalsIgnoreCase(tabbedPane.getTitleAt(i)))
					break;
			}
			tabbedPane.setSelectedIndex(i);
		}
		frame.getCommandPanel().println("Ready...");
	}

	static private LAC startLAC(AgentUI ui, int lacPort, Pair<String,String>... keys) {
		Vector<Pair<String,String>> v = new Vector<Pair<String,String>>();
		v.add(new Pair<String,String>("PROCESS", "CURRENT"));
		v.add(new Pair<String,String>("TRACE", "10"));
		for (Pair<String,String> p: keys)
			v.add(p);
		return (LAC)startAgent(ui, LAC.class, "LAC", 9000, v.toArray(new Pair[2+keys.length]));
	}
	
	static Pair<String,String>[] stringsToPairs(String... strings) {
		assert strings.length % 2 == 0;
		Vector<Pair<String,String>> v = new Vector<Pair<String,String>>(); 
		for (int i=0; i<strings.length;) {
			v.add(new Pair<String,String>(strings[i++],strings[i++]));
		}
		return v.toArray(new Pair[strings.length/2]);
	}
	
	static public TransientAgent startAgent(AgentUI ui, Class<?> cls, String name, int port, String... keyValuePairs) {
		return startAgent(ui, cls, name, port, stringsToPairs(keyValuePairs));
	}

	static public TransientAgent startAgent(AgentUI ui, Class<?> cls, String name, int port, Pair<String,String>... keyValuePairs) {
		TransientAgent agent;
		if (isPortTaken(port)) {
			ui.println("CASAProcess.startAgent(ui, "+cls+", \""+name+"\", "+port+" ...): Port already taken.");
			agent = null;
		}
		else {
			try {
				ParamsMap p = new ParamsMap();
				p.putJavaObject("TYPE", cls.getName(), true);
				p.putJavaObject("NAME", name, true);
				p.putJavaObject("PORT", Integer.toString(port), true);
				if (keyValuePairs!=null) {
					int len = keyValuePairs.length - 1;
					for (Pair<String,String> k: keyValuePairs)
						p.putJavaObject(k.getFirst(), k.getSecond(), true);
				}
				String process = p.getJavaObject("PROCESS", String.class);
				boolean remoteLaunch = !(process==null || "CURRENT".equalsIgnoreCase(process));
				TransientAgent.NewAgentLispCommand com = new TransientAgent.NewAgentLispCommand("NEW-AGENT");
				Status st = com.execute(null, p, ui, null);
				if (st instanceof StatusObject<?>) {
					Object obj = ((StatusObject<?>) st).getObject();
					if (obj instanceof TransientAgent) {
						agent = (TransientAgent) obj;
					}
					else {
						agent = null;
						ui.println("CASAProcess.startAgent(ui, "+cls+", \""+name+"\", "+port+" ...): Agent must be subtype of TransientAgent, but it is "+obj.getClass()+".");
					}
				}
				else {
					agent = null;
					if (!remoteLaunch)
						ui.println("CASAProcess.startAgent(ui, "+cls+", \""+name+"\", "+port+" ...): Expected a return of StatusObject<?>, but got "+st+".");
				}
				if (!remoteLaunch) {
					if (agent == null)
						ui.println(name+" startup FAILED: " + st);
					else {
						for (int i = 20; i != 0 && !agent.isInitialized(); i--) {
							try {
								ui.println("Waiting for "+name+" to start...");
								Thread.sleep(500);
							} catch (InterruptedException ex2) {
							}
						}
						if (!agent.isInitialized()) {
							ui.println("CASAProcess.startAgent(): Failed initialize "+name);
						} else {
							ui.println(name+" started on port " + agent.getURL().getPort());
						}
					}
				}
			} catch (Throwable e) {
				agent = null;
				ui.println("CASAProcess.startAgent(): " + e.toString());
				e.printStackTrace();
			}
			// }
		}
		return agent;
	}

	private static boolean isPortTaken(int lacPort) {
		Socket socket = null;
		try {
			socket = new Socket((String) null, lacPort);
		} catch (Throwable e1) {
			return false;
		}
		try {
			socket.close();
		} catch (IOException e1) {
		}
		return true;
	}

	private static CASAProcess startProcess(AgentUI ui, int port, int lacPort) {
		CASAProcess agent = null;
		try {
			ParamsMap params = new ParamsMap();
			params.putJavaObject("TYPE", "casa.CASAProcess", false);
			params.putJavaObject("PROCESS", "CURRENT", false);
//			params.putJavaObject("MARKUP", "KQML", false);
			params.put("PORT", new Integer(port), new JavaObject(new Integer(port)),
					false);
			String name = "CASAProcess" + System.currentTimeMillis();
			params.put("NAME", name, new SimpleString(name), false);
			params.put("LACPORT", new Integer(lacPort), new JavaObject(new Integer(
					lacPort)), false);
			int traceSpec = 10; // AbstractProcess.TRACE_TO_FILE|(trace?AbstractProcess.TRACE_ON:AbstractProcess.TRACE_OFF);
			params.put("TRACE", new Integer(traceSpec), new JavaObject(new Integer(
					traceSpec)), false);
			params.put("TRACETAGS", "error,warning,info,msg", new SimpleString(
					"error,warning,info,msg"), false);
			// agent = new CASAProcess (params, ui);
			// agent.start();
			TransientAgent.NewAgentLispCommand com = new TransientAgent.NewAgentLispCommand(
					"NEW-AGENT");
			Status st = com.execute(null, params, ui, null);
			if (st instanceof StatusObject<?>) {
				Object obj = ((StatusObject<?>) st).getObject();
				if (obj instanceof CASAProcess) {
					agent = (CASAProcess) obj;
				}
			}
			if (agent == null)
				ui.println("CASAProcess startup FAILED: " + st);
			else {
				for (int i = 20; i != 0 && !agent.isInitialized(); i--) {
					try {
						ui.println("Waiting for CASAProcess to start...");
						Thread.sleep(500);
					} catch (InterruptedException ex2) {
					}
				}
				if (!agent.isInitialized()) {
					ui.println("CASAProcess.startProcess(): Failed initialize the CASAProcess");
				} else {
					ui.println("CASAProcess started on port " + agent.getURL().getPort());
				}
			}
		} catch (Exception ex) {
			ui.println("CASAProcess.startProcess(): " + ex);
		}
		return agent;
	}

	@SafeVarargs
	static public TransientAgent startAgent(AgentUI ui, Pair<String,String>... keys) {
		TransientAgent agent = null;
		try {
			ParamsMap params = new ParamsMap();
			params.put("PORT", new Integer(-1024),
					new JavaObject(new Integer(-1024)), false);
			params.put("NAME", "casa", new SimpleString("casa"), false);
			params.put("LACPORT", new Integer(-1), new JavaObject(new Integer(-1)),
					false);
			int traceSpec = 10; // AbstractProcess.TRACE_TO_FILE|(trace?AbstractProcess.TRACE_ON:AbstractProcess.TRACE_OFF);
			params.put("TRACE", new Integer(traceSpec), new JavaObject(new Integer(
					traceSpec)), false);
			params.put("TRACETAGS", "error,warning,info,msg", new SimpleString(
					"error,warning,info,msg"), false);
			params.put("STRATEGY", "sc3", new SimpleString("sc3"), false);
			if (keys!=null)
				for (Pair<String,String> pair: keys) {
					params.put(pair.getFirst(), pair.getSecond(), new SimpleString(pair.getSecond()), false);
				}
			agent = new TransientAgent(params, ui);
			agent.start();
			for (int i = 20; i != 0 && !agent.isInitialized(); i--) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException ex2) {
				}
			}
		} catch (Exception ex) {
			ui.println("CASACommandLine.startAgent: " + ex);
		}
		if (!agent.isInitialized()) {
			ui.println("CASACommandLine.startAgent: Failed to initialize the 'casa' sub-agent");
		}
		return agent;
	}

	/**
	 * Prints the error string, followed by a command-line "usage" paragraph.
	 * @param s error message.  Can be null.
	 * @param fatal Terminates the program if true by calling {@link System#exit(int) System.exit(-1)}.
	 */
	private static void fatalError(String s, boolean fatal) {
		StringBuilder cl = new StringBuilder();
		if (commandArgs!=null) {
			for (String a:commandArgs) {
				cl.append(" ").append(a);
			}
		}
		Trace.log("error", (s != null ? (s + ". ") : "")
						+ "Usage:\n"
						+ "  casa [-lLtT?] [-LAC [<port>]] [-NOLAC [<port>]] [-PROCESS [<port>]] [-NOPROCESS] [-HELP]\n"
						+ "       [-TAGS tag-spec] [-DAEMON] [-KILLONLOWMEMORY] [<lisp-command>]\n"
						+ "    where:\n"
						+ "      l: Suppress automatically stating a LAC if there isn't one (same as -NOLAC).\n"
						+ "      L: Automatically start a LAC at port 9000 if there isn't one (same as -LAC).\n"
						+ "      t: turn tracing-to-file on.\n"
						+ "      T: Use a text interface instead of a GUI if there's no command on the command line.\n"
						+ "      N: supress starting an interface if there's no command on the command line.\n"
						+ "      ?, HELP: Prints this help text.\n"
						+ "      LAC [<port>]: Automatically start a LAC at port <port> if there isn't one (defaults to 9000).\n"
						+ "      NOLAC [<port>]: Do not start a LAC, but expect a LAC at port <port> (defaults to 9000).\n"
						+ "      PROCESS [<port>]: Automatically start a CASAProcess at port <port> (defaults to 9010).\n"
						+ "      NOPROCESS [<port>]: Do not start a PROCESS, but run a simple agent that executes the <lisp-command>.\n"
						+ "      TAGS [<tag-specifier>]: Specify the trace tags for non-agent (process-global) logging.\n"
						+ "      DAEMON: Mark this process such that no agent should start a GUI. Implies N.\n"
						+ "      KILLONLOWMEMORY: If the process runs low on memory, kill the process. Useful for daemons.\n"
						+ "      <lisp-command>: any legal agent run-time command (runs dialogue mode if this is missing).\n\n"
						+ "      -L, -LAC, -PROCESS, and -NOPROCESS are mutually exclusive.\n"
						+ "        If none of -L, -LAC, -PROCESS, and -NOPROCESS are present then if a LAC exists at -NOLAC (or 9000),\n"
						+ "      then a CASAProcess is started at 9010 or above, otherwise a LAC is started.\n"
						+ "        Tag-spec is a comma-separated list of trace tag identifiers, which my be prefixed with '-' to\n"
						+ "      indicate turning the tag off, and suffixed with a severity digit"
						+ "        Qualifiers may be abbreviated to the shortest unique truncation.\n"
						+ "Command line arguments were:\n  "+cl.toString()
						, Trace.OPT_SUPPRESS_AGENT_LOG);
		if (fatal)
			System.exit(-1);
	}

	// ***************************************************************/
	// END: FROM CASACommandLine2
	// ***************************************************************/
	
	static void startKillOnOutOfMemory() {
		new Thread() {
			@Override
			public void run() {
				long max = Runtime.getRuntime().maxMemory();
				long threshhold = Math.round(max*.03);
				while (true) {
					//sleep for a minute
					try {
						sleep(60000);
					} catch (InterruptedException e) {}
					if (Runtime.getRuntime().freeMemory()<threshhold) {
						System.out.println("Free memory getting dangerously low.");
						Runtime.getRuntime().gc();
						if (Runtime.getRuntime().freeMemory()<threshhold) {
							System.out.println("Free memory not recoverable by gc. Terminating process.");
							Runtime.getRuntime().exit(-3);
						}
					}
				}
			}

		}.start();
	}


}
