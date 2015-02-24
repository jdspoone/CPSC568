package casa.ui;

import casa.ML;
import casa.ObserverNotification;
import casa.Status;
import casa.TransientAgent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Observable;

/**
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The  Knowledge Science Group makes no representations about the suitability of  this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public class TextInterface extends ObservingAgentUI {

  protected String prompt = "\nAgent> ";
  Thread thread = null;
  boolean exit=false;
  
  /**
   * Create a new {@link AgentUI} that reads from System.in and prints to System.out.
   * @param a The agent for which this interface is created
   * @param args the arguments vector (as per the standard main(String[])) containing the command name and arguments to execute 
   * @param thread sends an interrupt to this thread whenever an EOF character is encountered at the end of a print() call.
   */
  public TextInterface(TransientAgent a, String[] args, Thread thread) {
  	this(a,args,false);
  	this.thread = thread;
  }
  
  /**
   * Create a new {@link AgentUI} that reads from System.in and prints to System.out.
   * @param a The agent for which this interface is created
   * @param args the arguments vector (as per the standard main(String[])) containing the command name and arguments to execute 
   * @param useListener if true, creates a listener thread that prompts for new commands from System.in
   */
  public TextInterface(TransientAgent a, String[] args, boolean useListener) {
  	super(a);
    agent = a;
    prompt = "\n"+agent.getURL().getFile()+"> ";
    if (useListener) {
      InputListener inputListener  = new InputListener(this);
      thread = agent.makeSubthread(inputListener,"CASAagentTextInterfaceListener");
      thread.start();
    }
//    agent.getCommandInterpreter().setStyle(RTCommandInterpreter.STYLE_PLAIN);
//    agent.getCommandInterpreter().setAsk(true);
  }
  
  /**
   * <p>Description: adapted from the previous update(). The structure and most comments
   * belong to the author of the previous version.  Update() now uses {@link ObserverNotification}</p>
   * @author <a href="mailto:dsbidulo@ucalgary.ca">Daniel Bidulock</a>
   * @version 0.9
   */
  @Override
	public void update(Observable o, Object arg) {
  	super.update(o, arg);
  	
	  String event = (arg instanceof ObserverNotification) ? ((ObserverNotification)arg).getType() : null;
	  if (event!=null && event.equals(ML.EVENT_EXITED) || event.equals(ML.EVENT_CLOSE_PORT)){
	      exit=true;
	  }

    if (thread!=null) thread.interrupt();  	
  }
  
  public boolean takesHTML() {return false;}

  public void print(String txt) {
  	boolean interrupt = false;
  	if (txt.length()>0) {
  		if (txt.charAt(txt.length()-1)==(char)-1) {
  		  txt = txt.substring(0,txt.length()-1);
  		  interrupt = true;
  	  } else if (txt.length()>1 && txt.charAt(txt.length()-1)==(char)-2) { //accounts for println()
  		  txt = txt.substring(0,txt.length()-2)+txt.charAt(txt.length()-1);
  		  interrupt = true;
  	  }
  	}
    System.out.print(txt);
    if (interrupt && thread!=null) 
    	thread.interrupt();
  }

  public void println(String txt) {
    print(txt+'\n');
  }

  public String ask(String prompt, String help, int type, String _default) {
    String p = "\n  "+
               prompt+
               (type<0?(help!=null?" ("+help+")":""+
                        _default!=null?" [default="+_default+"]":"")
                      :"")+
               "> ";
    type = type<0?-type:type;
    print(p);
    String buf;
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    try {
      buf = in.readLine();
      if (buf==null)
        return null; //EOF
      buf = buf.trim();
      if (buf.length()==0)
        return _default;
      switch(type) {
        case TYPE_INT:
          try {
            Integer.parseInt(buf);
          }
          catch (NumberFormatException ex1) {
          	println("*** Parameter must be of type int");
            return ask(prompt, help, -type, _default);
          }
          break;
        case TYPE_FLOAT:
          try {
            Float.parseFloat(buf);
          }
          catch (NumberFormatException ex1) {
          	println("*** Parameter must be of type float");
            return ask(prompt, help, -type, _default);
          }
          break;
        case TYPE_BOOLEAN:
          try {
            if ("|true|yes|t|y|on|1|".indexOf("|"+buf.toLowerCase()+"|")>=0) buf = "true";
            else if ("|false|no|f|n|off|0|".indexOf("|"+buf.toLowerCase()+"|")>=0) buf = "false";
            else return ask(prompt, help, -type, _default);
          }
          catch (NumberFormatException ex1) {
          	println("*** Parameter must be of type boolean");
            return ask(prompt, help, -type, _default);
          }
          break;
        default:
          break;
      }
      return buf;
    }
    catch (IOException ex) {
      return ask(prompt, help, -type, _default);
    }
  }

  public class InputListener implements Runnable {
    AgentUI ui;

    public InputListener(AgentUI ui) {
      this.ui = ui;
    }

    public void run() {
    	String buf;
    	BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
//  	InputStream/*Reader*/ in = /*new InputStreamReader(*/System.in/*)*/;
//  	BufferedInputStream in = new BufferedInputStream(System.in,1);
    	top:
    	while (!exit) {
    		Status stat=null;
    		try {
    			ui.print(prompt);
    			//if (Thread.interrupted()) continue;
    			buf = in.readLine();
    			stat = evalWithHistory(buf);
    			println((stat==null || stat.getStatusValue()>=0)
    					? ((stat==null)?"":stat.getExplanation())
    					:"****'"+buf+
    					"' returned error status "+String.valueOf(stat.getStatusValue())+":\n"+
    					stat.getExplanation()+"\n****");
    					if (stat!=null && stat.getStatusValue()!=0)
    						println("**** '"+buf+
    								"' returned error status "+String.valueOf(stat.getStatusValue())+":\n"+
    								stat.getExplanation()+"\n****");
    		}
    		catch (IOException ex) {
    			ui.println("********\nException: "+ex.toString());
    			ex.printStackTrace();
    			ui.println("********");
    		}
//    		synchronized (Thread.currentThread()) { 
//    			try {Thread.sleep(5000);
//    			} 
//    			catch (InterruptedException e) {
////  				ui.println("Interrupted.");
//    			}
//    		}
    	}
    	ui.println("");
    }
  }

  /**
	* Doesn't do anything in text UI
	*/
	public void start() {
	}

	public OutputStream getOutStream() {
		return System.out;
	}
}