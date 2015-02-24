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
package casa.ui;

import casa.ML;
import casa.MLMessage;
import casa.ObserverNotification;
import casa.Status;
import casa.TransientAgent;
import casa.abcl.CasaLispOperator;
import casa.abcl.ParamsMap;

import java.util.LinkedList;
import java.util.Observable;

import org.armedbear.lisp.Environment;

/**
 * This class is meant to be a handy superclass for UIs that want to observer an owner agent
 * for trace messages, or other events. Control of logging of these events is via the {@link #UI_MONITOR "(ui.monitor ...)"}
 * lisp command.<br>
 * It also handles command history memory through the {@link #UI_HISTORY "(ui.history...)"}
 * lisp command, which is supported via the {@link #evalWithHistory(String)} method.
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public abstract class ObservingAgentUI implements AgentUI {

  /**
   * The agent that "owns" this interface.
   */
  protected TransientAgent agent = null;

  protected boolean showEvents=true;
  protected boolean showMessages=true;
  protected boolean showUnknown=true;
  protected boolean showInfo=true;
  protected boolean showTrace=true;

  private LinkedList<String> history = new LinkedList<String>();
  private int MAX_HISTORY = 10;
  private String historyAccess = null;
  
  /**
   * @param agent The agent that "owns" this interface
   */
  protected ObservingAgentUI(TransientAgent agent) {
  	if(agent!=null){
  		this.agent = agent;
    	agent.addObserver(this);  	
  	}
  }

  /**
   * Handle a non-event notification
   * @param arg The object passed to the notification.
   */
  protected void handleOther(Object arg) {
    if (showUnknown && arg!=null) println(arg.toString());
  }
  
  /**
   * Handle an event notification, by printing it to the ui, depending on the values set in the 
   * {@link #UI_MONITOR "(ui.monitor ...)"} lisp command.
   * @param event the event type string
   * @param obj the object passed with the event
   */
  protected void handleEvent(String event, Object obj) {
	  if (event == null) {
		  if (showEvents) {
	        	println("received notification >>>> " + event + "\n" + obj);//ObservableEvent.state2String(event)+"\n   "+obj);
	      }
	  }
	  else if (event.equals(ML.EVENT_POST_STRING)){
		  println(obj.toString());
	  }
	  else if (event.equals(ML.EVENT_EXITING)){
		  //Pass... needed to avoid catch-all 'else'
	  }
	  else if (event.equals(ML.EVENT_EXITED)){
		  println("Agent "+agent.getURL().getFile()+" exited.");
	  }
	  else if (event.equals(ML.EVENT_CLOSE_PORT)){
		  //Pass... needed to avoid catch-all 'else'
	  }
	  else if (event.equals(ML.EVENT_MESSAGE_RECEIVED)){
		  if (showMessages) {
	      		println("Received message: ");
	      		printlnObject(obj);
	      }
	  }
	  else if (event.equals(ML.EVENT_MESSAGE_SENT)){
		  if (showMessages) {
	      		println("Sent message:");
	      		printlnObject(obj);
	      }
	  }
	  else if (event.equals(ML.EVENT_MESSAGE_SEND_FAILED)){
		  if (showMessages) {
	      		println("Message send *F*A*I*L*E*D*: ");
	      		printlnObject(obj);
		  }
	  }
	  else if (event.equals("event_trace")){
	  	if (showTrace) {
  		  printlnObject(obj);
	  	}
	  }
//	  else {
//  		agent.println(null,"ObservingAgentUI.handleEvent: Unknown event: \""+event+"\" and object \""+obj+"\"");
//	  }
	  
}

  /**
   * A utility method to safely print out an object.  If the object is a method, use it's "pretty print"
   * toString() method.  Otherwise, just call it's toString() method (checking first that it isn't null).
   * @param obj The object to print to the ui.
   */
  protected void printlnObject(Object obj) {
  	if (obj instanceof MLMessage) {
			obj = ((MLMessage) obj).toString(true);
		}
  	if (obj!=null) println(obj.toString());
  }

  /**
   * Defers to {@link #handleEvent(String, Object)} if <em>arg</em> is an 
   * {@link ObserverNotification}, or {@link #handleOther(Object)} otherwise.
   */
  public void update(Observable o, Object arg) {
	  String event = (arg instanceof ObserverNotification) ? ((ObserverNotification)arg).getType() : null;
	  if (event != null)
		  handleEvent(event, ((ObserverNotification)arg).getObject());
	  else
		  handleOther(arg);
  }

  /**
   * Evaluate a command string.  You should probably consider using {@link #evalWithHistory(String)
   * instead to properly support the {@link #UI_HISTORY "(ui.history ...)"} lisp command.
   * @param buf The string to evaluate in the command language (Lisp).
   * @return A status returned from the evaluation (may be null).
   */
  protected Status eval(String buf) {
		return (agent==null)?(casa.abcl.Lisp.abclEval(null, null, null, buf, new BufferedAgentUI())):agent.abclEval(buf, null, this);
  }
  /**
   * Evaluate a command string, taking into account the {@link #UI_HISTORY "(ui.history...)"} lisp command. 
   * @param buf The String to evaluate in the command language (Lisp).
   * @return A status returned from the evaluation (may be null).
   */
  protected Status evalWithHistory(String buf) {
  	Status stat;
		do {
			if (historyAccess!=null) {
				buf = historyAccess;
				println(/*prompt*/">> "+buf);
				historyAccess = null;
			}
			//if (Thread.interrupted()) continue;
			if (buf==null) {
				println("bye");
				return null; //EOF
			}
			buf = buf.trim();
			if (buf.length()==0) return null;
			history.addFirst(buf);
			if (history.size()>MAX_HISTORY) history.removeLast();
      stat = eval(buf);
		} while (historyAccess!=null);
		return stat;
  }
  
  /**
   * Lisp operator: (UI_MONITOR :EVENTS [bool] :MESSAGES [bool] :INFO [bool] :UNKNOWN [bool] :TRACE [bool])<br>
   * Turn on or off display of various notification message types from the agent.
   */
  @SuppressWarnings("unused")
	private static final CasaLispOperator UI_MONITOR =
    new CasaLispOperator("ui.monitor", "\"!display various notification messages as they come in from the agent\" "
    		+"&KEY EVENTS \"@java.lang.Boolean\" \"!show agent events as they come in.\" "
    		+"MESSAGES \"@java.lang.Boolean\" \"!show agent CASA messages as they come in.\" "
    		+"INFO \"@java.lang.Boolean\" \"!show information messages as they come in.\" "
    		+"UNKNOWN \"@java.lang.Boolean\" \"!show unknown notifications as they come in.\" "
    		+"TRACE \"@java.lang.Boolean\" \"!show trace messages as they come in.\" "
    		, TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
  	{
  	  @Override 
  	  public Status execute (TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
        StringBuffer buf = new StringBuffer();
        boolean all = params.size() == 0;
        
        ObservingAgentUI textInterface;
				try {
					textInterface = (ObservingAgentUI)ui;
				} catch (Exception e) {
					return new Status(-6, agent.println("error","(UI.MONITOR ...) can only be called on a subclass of casa.ui.ObservingAgentUI",e));
				}

        if (params.containsKey("EVENTS")) textInterface.showEvents = (Boolean) params.getJavaObject("EVENTS");
        if (all || params.containsKey("events")) buf.append("  events: ").append(String.valueOf(textInterface.showEvents)).append("\n");

        if (params.containsKey("MESSAGES")) textInterface.showEvents = (Boolean) params.getJavaObject("MESSAGES");;
        if (all || params.containsKey("MESSAGES")) buf.append("  messages: ").append(String.valueOf(textInterface.showEvents)).append("\n");

        if (params.containsKey("INFO")) textInterface.showEvents = (Boolean) params.getJavaObject("INFO");;
        if (all || params.containsKey("INFO")) buf.append("  info: ").append(String.valueOf(textInterface.showInfo)).append("\n");

        if (params.containsKey("TRACE")) textInterface.showEvents = (Boolean) params.getJavaObject("TRACE");;
        if (all || params.containsKey("TRACE")) buf.append("  trace: ").append(String.valueOf(textInterface.showTrace)).append("\n");

        if (params.containsKey("UNKNOWN")) textInterface.showEvents = (Boolean) params.getJavaObject("UNKNOWN");;
        if (all || params.containsKey("UNKNOWN")) buf.append("  unknown: ").append(String.valueOf(textInterface.showUnknown)).append("\n");

        String ret = buf.toString();
        ui.print(ret);
        return new Status(0,ret);
      }
  	};

    /**
     * Lisp operator: (UI_HISTORY )<br>
     * Print or access the agent history
     */
    @SuppressWarnings("unused")
  	private static final CasaLispOperator UI_HISTORY =
      new CasaLispOperator("ui.history", "\"!no param: show a command history list; param: access an element\" "
      		+"&OPTIONAL INDEX \"@java.lang.Integer\" \"!The element of the history list to execute.\" "
      		+"&KEY MAX \"@java.lang.Integer\" \"!The the maximum number of history elements to save.\" "
      		, TransientAgent.class, new Object() { }.getClass().getEnclosingClass())
    	{
  	  @Override 
  	  public Status execute (TransientAgent agent, ParamsMap params, AgentUI ui, Environment env) {
        ObservingAgentUI textInterface;
				try {
					textInterface = (ObservingAgentUI)ui;
				} catch (Exception e) {
					return new Status(-6, agent.println("error","(UI.HISTORY ...) can only be called on a subclass of casa.ui.ObservingAgentUI",e));
				}
        textInterface.history.removeFirst();
        if (params.containsKey("MAX")) {
        	textInterface.MAX_HISTORY = (Integer)params.getJavaObject("MAX");
        }
      	if (params.containsKey("INDEX")) {
      		int index = (Integer)params.getJavaObject("INDEX");
      		if (index < textInterface.history.size()) {
      			textInterface.historyAccess = textInterface.history.get(index);
      		}
      		else {
      			textInterface.print(""+(char)-1);
        		return new Status(-1,"index out of range");
      		}
      	} else {
      		int i = 0;
      		for (String buf : textInterface.history) {
      			textInterface.println(""+i++ +". "+buf);
      		}
      		textInterface.println("  (max history elements to save: "+textInterface.MAX_HISTORY+")");
      	}
      	textInterface.print(""+(char)-1);
        return new Status(0);
      }
  	};

}
