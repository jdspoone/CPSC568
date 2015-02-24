package casa;

import casa.io.CASAFilePropertiesMap;
import casa.ui.ObjectFieldCache;
import casa.util.PropertyException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
/**
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public class ProcessOptions {
  /**
	 */
  private AbstractProcess agent;
  public ProcessOptions(AbstractProcess agent) {this.agent=agent;}
  /**
	 * @return
	 */
  public AbstractProcess getAgent() {return agent;}
  public void write(CASAFilePropertiesMap m) {
  	if (m!=null) {
  		m.setLong   ("options.defaultTimeout"  ,defaultTimeout);
  		m.setInteger("options.resendThreshhold",resendThreshhold);
  		m.setLong   ("options.minTimeout"      ,minTimeout);
  		m.setBoolean("options.useAckProtocol"  ,useAckProtocol);
  		m.setBoolean("options.tracing"         ,tracing);
  		m.setBoolean("options.traceMonitor"    ,traceMonitor);
  		m.setBoolean("options.traceToFile"     ,traceToFile);
  		m.setString ("options.traceTags"       ,traceTags);
  		m.setBoolean("options.recordHistory"   ,recordHistory);
  		m.setBoolean("options.usePriority"     ,usePriority);
  		m.setBoolean("options.observeMessages" ,observeMessages);
  		m.setBoolean("options.processLocalMessageShortcutting",processLocalMessageShortcutting);
  		//    m.setString ("options.strategy"        ,strategy);
  		m.setString ("options.security.package",security_package);
  		m.setInteger("options.security.level"  ,security_level);
  		m.setString ("options.security.defSignatureAlgorithm",security_defSignatureAlgorithm);
  		m.setBoolean("options.security.requireIncommingSigning",security_requireIncommingSigning);
  	}
  }
  public void read(CASAFilePropertiesMap m) {
    try {
      defaultTimeout = m.getLong ("options.defaultTimeout");
    } catch (PropertyException ex) {
    }
    try {
      resendThreshhold = m.getInteger ("options.resendThreshhold");
    } catch (PropertyException ex1) {
    }
    try {
      minTimeout = Math.min(m.getLong ("options.minTimeout"), 3000);
    } catch (PropertyException ex2) {
    }
    try {
      useAckProtocol = m.getBoolean ("options.useAckProtocol");
    } catch (PropertyException ex3) {
    }
    try {
      /*if (!tracing)*/ tracing = m.getBoolean ("options.tracing");
    } catch (PropertyException ex4) {
    }
    try {
      /*if (!traceMonitor)*/ traceMonitor = m.getBoolean ("options.traceMonitor");
    } catch (PropertyException ex4) {
    }
    try {
      /*if (!traceToFile)*/ traceToFile = m.getBoolean ("options.traceToFile");
    } catch (PropertyException ex4) {
    }
    try {
      /*if (traceTags==null || traceTags.length()==0)*/ traceTags = m.getString ("options.traceTags");
    } catch (PropertyException ex4) {
    }
    try {
      recordHistory = m.getBoolean ("options.recordHistory");
    } catch (PropertyException ex4) {
    }
    try {
      usePriority = m.getBoolean ("options.usePriority");
    } catch (PropertyException ex4) {
    }
    try {
      observeMessages = m.getBoolean ("options.observeMessages");
    } catch (PropertyException ex4) {
    }
    try {
      processLocalMessageShortcutting = m.getBoolean ("options.processLocalMessageShortcutting");
    } catch (PropertyException ex4) {
    }
//    try {
//      strategy = m.getString ("options.strategy");
//    } catch (PropertyException ex4) {
//    }
    try {
    	security_package = m.getString ("options.security.package");
        if (security_package.equals ("")) security_package = null;
    } catch (PropertyException ex4) {
    }
    try {
    	security_level = m.getInteger("options.security.level");
    } catch (PropertyException ex4) {
    }
    try {
    	security_defSignatureAlgorithm = m.getString ("options.security.defSignatureAlgorithm");
    } catch (PropertyException ex4) {
    }
    try {
    	security_requireIncommingSigning = m.getBoolean("options.security.requireIncommingSigning");
    } catch (PropertyException ex4) {
    }
  }

	@CasaPersistent
	@CasaOption (
			labelText = "Default Timeout",
			helpText = "the milliseconds to (asychronously) wait for a message to be replied to or acknowledge before generating a timeout event (must be greater than \'Minimum Send Timeout\')")
	public long		defaultTimeout						= 10000;
	@CasaPersistent
	@CasaOption(
			labelText = "Resend Threshold",
			helpText = "The number of times to retry sending a message if it is not acknowledged within it\'s reply-by period.",
			group = "Acknowledge Protocol")
	public int		resendThreshhold					= 3;
	@CasaPersistent
	@CasaOption(
			labelText = "Minimum Send Timeout (msec)",
			helpText = "The minimum number of milliseconds to wait before generating a timeout event.  Any messages with less than this timeout value will have their timeout value increased to this amount.")
	public long		minTimeout							= 3000;
	@CasaPersistent
	@CasaOption (
			labelText = "Use ACK Protocol",
			helpText = "Use the Acknowledge protocol: all incoming messages with be either replied to or acknowledge.  (Caution: using the ACK protocol increases network traffic, but guarentees message delivery.)",
			group = "Acknowledge Protocol")
	public boolean	useAckProtocol						= false;
	@CasaPersistent
	@CasaOption(
			labelText = "Tracing",
			helpText = "The agent should maintain a record of trace events (you can see these using the \'start trace monitor window\' menu item)",
			group = "Tracing",
			postSaveMethod = "resetTraceOptions",
			groupOrder = 0,
			actionListenerMethod = "getTracingActionListener")
	public boolean	tracing								= false;
	@CasaPersistent
	@CasaOption(
			enabledMethod="isTracing",
			labelText = "Start trace monitor window",
			helpText = "the trace monitor window will be activated on agent startup",
			group = "Tracing",
			postSaveMethod = "resetTraceOptions",
			groupOrder = 4)
	public boolean	traceMonitor						= false;
	@CasaPersistent
	@CasaOption(
			enabledMethod="isTracing",
			labelText = "Trace to file",
			helpText = "trace to the agent log file",
			group = "Tracing",
			postSaveMethod = "resetTraceOptions",
			groupOrder = 3)
	public boolean	traceToFile							= false;
	@CasaPersistent
	@CasaOption(
			enabledMethod="isTracing",
			labelText = "Trace history max size",
			helpText = "set the memory trace history max size; set to 0 to turn trace history off.  Only applies if TRACING is on",
			group = "Tracing",
			postSaveMethod = "resetTraceOptions",
			groupOrder = 2)
	public long	traceHistoryMaxSize					= 0;
	@CasaPersistent
	@CasaOption(
			enabledMethod="isTracing",
			labelText = "Trace tags",
//			helpText = "a comma-delimited list; (valid tags are: calls, warning, info, msg, msgHandling, commitments, policies, lisploadverbose, lisploadprint\n [append a single digit (1-9) to increase detail])",
			// The __ will be substituted for learned tags
			helpText = "a comma-delimited list; (valid tags are: __\n [append a single digit (1-9) to increase detail])",
			group = "Tracing",
			groupOrder = 1)
	public String	traceTags							= null;
//	/**
//	 * does NOT persist: only used to convey the command line settings
//	 */
//	public int		traceMask							= 0;
	@CasaPersistent
	@CasaOption(
			labelText = "Threaded events",
			helpText = "If set, the agent processes events asychronously in a separate thread; otherwise events are processed in the agent's main thread.")
  public boolean threadedEvents = false;
	@CasaPersistent
	@CasaOption(
			labelText = "Record history",
			helpText = "the agent should keep a record of all messages sent and received (caution, this may use a lot of memory)")
	public boolean	recordHistory						= false;
	@CasaPersistent
	@CasaOption(
			labelText = "Message queue is a prority queue",
			helpText = "if unchecked, queue is FIFO")
	public boolean	usePriority							= true;
	@CasaPersistent
	@CasaOption(
			labelText = "Observe messages",
			helpText = "the agent should process messages not addressed to it (broadcast messages are always processed)")
	public boolean	observeMessages						= false;
	@CasaPersistent
	@CasaOption(
			labelText = "Process local message shortcutting")
	public boolean	processLocalMessageShortcutting		= true;
//	@CasaPersistent
//	@CasaOption
//	public String	strategy							= null;//TODO: write strategy help
	@CasaPersistent
	@CasaOption(
			group = "Security")
	public String	security_package					= null;//TODO: write security package help
	@CasaPersistent
	@CasaOption(
			group = "Security")
	public int		security_level						= 1;//TODO: write security level help
	@CasaPersistent
	@CasaOption(
			group = "Security")
	public String	security_defSignatureAlgorithm		= null;//TODO: write security def sig algorithm help
	@CasaPersistent
	@CasaOption(
			group = "Security")
	public boolean	security_requireIncommingSigning	= false;//TODO: write security require incomming signing help

  @CasaPersistent 
  @CasaOption(labelText="trusting", group="Do Execute Requests")
  public boolean ExecuteRequest_trusting = true;

  @CasaPersistent 
  @CasaOption(labelText="paranoid", group="Do Execute Requests")
  public boolean ExecuteRequest_paranoid = false;

  @CasaPersistent 
  @CasaOption(labelText="URLs in FIPA style", helpText="true: (agent-idenifier :name \"fred\" ...); false: casa://10.0.1.12/casa/TransientAgent/fred:5400.")
  public boolean fipa_urls = false;

  ///////////////////////////////////////////////////////////////////////////////////
  // The following sets() are used to add special processing when changing variables.
  // These are are automatically called when setting the values through TransientAgent.setField().
  ///////////////////////////////////////////////////////////////////////////////////
  public void setTracing(boolean tracing) {
		this.tracing = tracing;
		agent.setTracing(tracing);
	}
  public void setTraceMonitor(boolean traceMonitor) {
		this.traceMonitor = traceMonitor;
 	  agent.resetTraceOptions ();
	}
  public void setTraceToFile(boolean traceToFile) {
		this.traceToFile = traceToFile;
 	  agent.resetTraceOptions ();
	}
  public void setTraceTags(String traceTags) { //TODO fix this
		this.traceTags =  this.traceTags+","+traceTags;
		agent.resetTraceOptions ();
	}
  public void setSecurity_package(String securityPackage) {
		this.security_package = securityPackage;
		agent.resetSecurityPackage(security_package);
	}
  public void setSecurity_level(int securityLevel) {
		this.security_level = securityLevel;
		if (agent.securityFilter!=null) agent.securityFilter.setSecurityLevel(security_level);
	}
  public void setSecurity_defSignatureAlgorithm(String securityDefSignatureAlgorithm) {
		this.security_defSignatureAlgorithm = securityDefSignatureAlgorithm;
		if (agent.securityFilter!=null) agent.securityFilter.setDefSignatureAlgorithm(security_defSignatureAlgorithm);
	}
  public void setSecurity_requireIncommingSigning(boolean securityRequireIncommingSigning) {
		security_requireIncommingSigning = securityRequireIncommingSigning;
		if (agent.securityFilter!=null) agent.securityFilter.requireIncomingSigning(security_requireIncommingSigning);
	}
  public void resetTraceOptions() {
  	agent.setTracing(tracing);
  	agent.resetTraceOptions();
  }
  
  public boolean isTracing(){
	  return tracing;
  }

  public ActionListener getTracingActionListener(final Collection<ObjectFieldCache> cache){
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean enabled = false;
				for(ObjectFieldCache cash : cache){
					if(cash.getFieldName().equals("tracing"))
						enabled = (Boolean) cash.getGuiValueAsNative();
				}
				for(ObjectFieldCache cash : cache){
					if(cash.getFieldName().equals("traceHistoryMaxSize") || 
							cash.getFieldName().equals("traceMonitor") ||
							cash.getFieldName().equals("traceTags") ||
							cash.getFieldName().equals("traceToFile")){
						cash.getGuiLabel().setEnabled(enabled);
						cash.getGuiValue().setEnabled(enabled);
					}
				}				
			}
		};
	}
}