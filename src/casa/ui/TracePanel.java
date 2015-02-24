package casa.ui;

//import javax.swing.JTextArea;
import casa.AbstractProcess;
import casa.ObserverNotification;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * <p>Title: </p> <p>Description: </p> <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */


public class TracePanel extends JEditorPane/*JTextArea*/ implements Observer {
	private static final long	serialVersionUID	= -1999741966837440086L;
//	private Set<ProcessInterface> traces = new java.util.HashSet<ProcessInterface>();
  private StringBuilder buffer = new StringBuilder(MAX_BUFFER+1000);
  private Timer timer;
  public   String prompt = new String("$-");
  private boolean executeFlag = false;
  protected boolean showEvents=false;
  protected boolean showMessages=false;
  protected boolean showTrace=false;
  protected AbstractProcess agent;
  /**
   * The maximum size the buffer will grow (to save memory).  When it reaches this
   * limit, the buffer will be truncated (at the beginning) to 2/3 this size.
   */
  protected static int MAX_BUFFER = 20000;
  
  public TracePanel(AbstractProcess agent) throws HeadlessException {
  	this.agent = agent;
  	assert agent!=null;
    setContentType("text/html");
    ActionListener actionListener = new ActionListener() {
      @Override
			public void actionPerformed(ActionEvent evt) {
        flush();
      }  
    };
    makePopupMenu(actionListener,this);
    timer = new Timer(500,actionListener);
    timer.stop();
    //addTrace();
  }

  public synchronized void flush() {
    String content = getText();
    try {
      int p = content.lastIndexOf("</body>");
      if (p>0) content = content.substring(0,p);
      p = content.lastIndexOf("<a name=\"endPoint\"></a>");
      if (p>0) content = content.substring(0,p);

      content = content+buffer.append("<a name=\"endPoint\"></a>\n</body>\n</html>\n").toString();
      if (content.length()>MAX_BUFFER) {
      	content = "..."+content.substring(content.length()-MAX_BUFFER*2/3);
      }
      setText(content);
      buffer.setLength(0);
      timer.stop();
    }
    catch (Throwable e) { //reduce memory requirements
    	content = getText();
    	setText(content.substring(content.length()/2));
    	flush();
    }
    /*try {
      scrollToReference("endPoint");
    }
    catch (Exception ex) {
    }*/
  }

  private synchronized void putBuffer(String s) {
    if (buffer.length() == 0)
        timer.start();
    buffer.append(s);
  }

  @Override
	public void update(Observable o, Object arg){
  	String s;
  	String type = null;
  	String color = "black";
  	if (arg instanceof ObserverNotification) {
  		Object object = ((ObserverNotification)arg).getObject();
  		type = ((ObserverNotification)arg).getType();
  		if ("event_trace".equals(type)) {
  			if (!showTrace)
  			  return;
  			else {
    	    s = (object == null) ? "" : object.toString();
    	    color = "#DF6C00";
  			}
  		}
  		else if (agent.isA(type, "event_messageEvent")) {
  			if (!showMessages)
  				return;
  			else {
    	    s = (object == null) ? "" : ("Message Event ("+type+"):\n"+object.toString()+"\n");
    	    color = "#AF4C00";
  			}
  		}
  		else {
  			if (!showEvents)
  				return;
  			else {
  	      s = (object == null) ? "" : ("Event:\n"+object.toString()+"\n");
    	    color = "#7F2C00";
  			}
  		}
  	}
  	else {
  	  s = arg.toString();
  	}
  	
    int length = s.length();
    
    //remove any extra returns at the end of the message
    if (length>3 && s.substring(length-3, length).equals("\r\r\n")) {
      s = s.substring(0, length-3) + "\r\n";
      --length;
    }

    if (length>6 && s.substring(0,6).equalsIgnoreCase("<html>")) {
      s = s.replaceAll("</?html>|</?HTML>",""); //remove all the <html>'s and </html>'s.
      printHTML(s);
    }
    else {
//    	if ("event_trace".equals(type)) { 
    		  printHTML("<FONT color=\""+color+"\">" + HTML2plain(s) + "</FONT>");
//    	}
//    	else
//        printPlain(s);
    }
  }

  static private String HTML2plain(String arg) {
  	String s = arg;
    s = s.replaceAll("&","&amp;");
    s = s.replaceAll("<","&lt;");
    s = s.replaceAll(">","&gt;");
    s = s.replaceAll("\n","<br>");
    s = s.replaceAll("  ","&nbsp;&nbsp;");
    s = s.replaceAll("!!!(.+)!!!","<font color=green>$1</font>");
    return s;
  }

  public void printPlain(String s) {
   putBuffer(HTML2plain(s));
  }

  public void printHTML(String s) {
    putBuffer(s);
  }

  public void addTrace(){
      if(agent != null){
//        traces.addLocalObserver(trace);
          agent.addObserver(this,"event_trace");
      }
  }

  public void removeTrace(){
      if(agent != null){
//          traces.remove(trace);
          agent.deleteObserver(this);
      }
  }
  
  @Override
	public void paste(){
	    Toolkit myToolkit = this.getToolkit();
	    Clipboard myClipboard = myToolkit.getSystemClipboard();
	    String  s = null;
	    try{ 
	    	s = (String)myClipboard.getContents(myToolkit.getSystemClipboard()).getTransferData(DataFlavor.stringFlavor); 
	    	int pos = this.getCaretPosition();
	    	this.getDocument().insertString(pos,s,null);
	    }catch(UnsupportedFlavorException u){}
	    catch(IOException e ){}
	  	catch(BadLocationException b){} 
	  }
 
  @Override
	public void setText(String s){
	  String part = "";
	  Document doc = this.getDocument();
	  //System.out.println(doc.getLength());
	  
	  try{
		  String t = doc.getText(0,doc.getLength());
		  int x = t.lastIndexOf(prompt);
		  if(x>1){
			  if (!executeFlag)
				  part = t.substring(x+prompt.length());
			  executeFlag = false;
		  }
		  String promptHTML = "<br><font color=green>"+prompt+part+" </font>";
		  String begin = s.substring(0,s.lastIndexOf("</body>"));
		  String end = s.substring(s.lastIndexOf("</body>"));
	  
	  
		  begin =  begin+promptHTML + end;
	  
	  
	  super.setText(begin);
	  }catch(Throwable b){}
  }
  /**
	 * @param b
	 */
  public void setExecuteFlag(boolean b){
	  this.executeFlag = b;
  }
  
  /************** popup menu ***********************************************/
  protected JPopupMenu popup;
  protected JCheckBoxMenuItem traceMenuItem;
  protected JCheckBoxMenuItem eventsMenuItem;
  protected JCheckBoxMenuItem messagesMenuItem;
  protected JMenu bufferMenu;
  protected JMenuItem incBufferMenuItem;
  protected JMenuItem decBufferMenuItem;
  

  private void makePopupMenu(ActionListener listener, JEditorPane pane) {
      //Add listener to components that can bring up popup menus.
      PopupListener popupListener = new PopupListener();
      pane.addMouseListener(popupListener);
      //menuBar.addMouseListener(popupListener);

      //Create the popup menu.
      popup = new JPopupMenu();
      traceMenuItem = new JCheckBoxMenuItem("show trace", false);
      traceMenuItem.addItemListener(popupListener);
      popup.add(traceMenuItem);
      messagesMenuItem = new JCheckBoxMenuItem("show messages", false);
      messagesMenuItem.addItemListener(popupListener);
      popup.add(messagesMenuItem);
      eventsMenuItem = new JCheckBoxMenuItem("show all events", false);
      eventsMenuItem.addItemListener(popupListener);
      popup.add(eventsMenuItem);
      
      popup.addSeparator();

      bufferMenu = new JMenu("buffer size", true);
      bufferMenu.addItemListener(popupListener);
      popup.add(bufferMenu);
      incBufferMenuItem = new JMenuItem("increase");
      incBufferMenuItem.addActionListener(popupListener);
      bufferMenu.add(incBufferMenuItem);
      decBufferMenuItem = new JMenuItem("decrease");
      decBufferMenuItem.addActionListener(popupListener);
      bufferMenu.add(decBufferMenuItem);
      
}

  class PopupListener extends MouseAdapter implements ItemListener, ActionListener {
      @Override
			public void mousePressed(MouseEvent e) {
          maybeShowPopup(e);
      }

      @Override
			public void mouseReleased(MouseEvent e) {
          maybeShowPopup(e);
      }

      private void maybeShowPopup(MouseEvent e) {
          if (e.isPopupTrigger()) {
              popup.show(e.getComponent(),
                         e.getX(), e.getY());
          }
      }
      
			@Override
			public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();
        if (source == traceMenuItem) {
        	showTrace = !(e.getStateChange()==ItemEvent.DESELECTED);
          printPlain("Show trace: "+(showTrace?"on; ":"off; "));
        } 
        else if (source == eventsMenuItem) {
        	showEvents = !(e.getStateChange()==ItemEvent.DESELECTED);
          printPlain("Show all events: "+(showEvents?"on; ":"off; "));
        } 
        else if (source == messagesMenuItem) {
        	showMessages = !(e.getStateChange()==ItemEvent.DESELECTED);
          printPlain("Show message events: "+(showMessages?"on; ":"off; "));
        } 
        if (showEvents) {
        	messagesMenuItem.setSelected(true);
        }
        
      }

			@Override
			public void actionPerformed(ActionEvent e) {
				Object source = e.getSource();
        if (source == incBufferMenuItem) {
          printPlain("MAX_BUFFER increased from "+MAX_BUFFER+" to "+MAX_BUFFER*2);
        	MAX_BUFFER *= 2;
        } 
        else if (source == decBufferMenuItem) {
          printPlain("MAX_BUFFER decreased from "+MAX_BUFFER+" to "+MAX_BUFFER/2);
        	MAX_BUFFER /= 2;
        } 
				
			}

  }
  
}
