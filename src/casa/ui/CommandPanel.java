/**
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. 
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  
 * The  Knowledge Science Group makes no representations about the suitability
 * of  this software for any purpose.  It is provided "as is" without express
 * or implied warranty.</p>
 * <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
package casa.ui;

import casa.Status;
import casa.StatusObject;
import casa.TransientAgent;
import casa.util.CASAUtil;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.OutputStream;
import java.util.Observable;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Keymap;

import org.omg.PortableInterceptor.SUCCESSFUL;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
@SuppressWarnings("serial")
public class CommandPanel extends JPanel implements AgentUI {
	
	TransientAgent agent;
	Container frame;
	protected TracePanel tracePanel;
  protected Vector <String> inputLine =  new Vector<String>();
  protected int prevIndex = -1;
  protected int currentIndex;
  protected OutputStream outStream = new OutputStream(){
  	@Override
		public void write(int b){
  		print(new String(new byte[]{(byte)b}),true,"black");
  		}
  	@Override
		public void write(byte[] b, int off, int len) {
  		print(new String(b,off,len),true,"black");
  		}
  	@Override
		public void write(byte[] b) {
  		print(new String(b),true,"black");
  		}
  	};


	public CommandPanel(TransientAgent agent, Container frame) {
		tracePanel = new TracePanel(agent);
		this.agent = agent;
		this.frame = frame;
		setName("Command");
		
		setLayout(new BorderLayout());
		// final String prompt = new String("$-");

		//final TracePanel tracePanel = new TracePanel();
		tracePanel.setMinimumSize(new Dimension(10, 4));
		tracePanel.setAutoscrolls(true);
		tracePanel.setFocusable(true);
		//tracePanel.addTrace(agent);

		JScrollPane msgScrolltemp = new JScrollPane(tracePanel);

		add( msgScrolltemp, BorderLayout.CENTER);
		setMinimumSize(new Dimension(20, 24));
		// add(makeCommandPanel(i), BorderLayout.SOUTH);


		class promptFilter extends DocumentFilter{

			Document doc = tracePanel.getDocument();

			@Override
			public void insertString(DocumentFilter.FilterBypass fb, int offset,
					String text, AttributeSet attr) throws BadLocationException {	 
				try{
					int x = doc.getText(0,doc.getLength()).lastIndexOf(tracePanel.prompt);
					if(tracePanel.getCaret().getDot()>=x+tracePanel.prompt.length())
						fb.insertString(offset, text, attr);	 
				} catch(BadLocationException b){}

			}

			@Override
			public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attr)
			throws BadLocationException {

				try{
					int x = doc.getText(0,doc.getLength()).lastIndexOf(tracePanel.prompt);
					if(tracePanel.getCaret().getDot()>=x+tracePanel.prompt.length())
						fb.replace(offset, length, string, attr);
				} catch(BadLocationException b){}
			}

			@Override
			public void remove(FilterBypass fb, int offset, int length) 
			throws BadLocationException {	 
				try{
					int x = doc.getText(0,doc.getLength()).lastIndexOf(tracePanel.prompt);
					if(tracePanel.getCaret().getDot()>x+tracePanel.prompt.length())
						fb.remove(offset,length); 
				} catch(BadLocationException b){}    		 
			}
		}

		((AbstractDocument)(tracePanel.getDocument())).setDocumentFilter(new promptFilter());  
		
		final int SUPRESS_PRINT = 6163;
		
		class Executer {
			boolean inString = false;
			int parenCount = 0;
			String buf = "";
			public boolean isPending() {
				return buf.length()>0;
			}
			public Status execute(String line) {
				if (line==null || line.length()==0)
					return new Status(SUPRESS_PRINT);
				buf += (buf.length()==0?"":"\n")+line;
				for (int i=0, end=line.length(); i<end; i++) {
					switch (line.charAt(i)) {
					case '"':
						inString = !inString;
						break;
					case '\\':
						i++;
						break;
					case '(':
						if (!inString)
							parenCount++;
						break;
					case ')':
						if (!inString)
							parenCount--;
						break;
					}
				}
				if (parenCount==0 && !inString) {
						inputLine.addElement(buf.substring(1));
						currentIndex = ++prevIndex;
					Status ret = new Status(executeButton(buf));
					buf = "";
					return ret;
				}
				return new Status(SUPRESS_PRINT);
			}
		}

    final Executer exec = new Executer();

    class CommandLine{
			protected String getLine(){
				String line = null;
				Document doc = tracePanel.getDocument();
				try{
					String text = doc.getText(0,doc.getLength());
					int x = text.lastIndexOf(tracePanel.prompt)+tracePanel.prompt.length();
					if (x<tracePanel.prompt.length()) x=0;
					line = text.substring(x,doc.getLength()); 
				}catch(BadLocationException e){}
				return line;
			}
			public void putCaret(){
				tracePanel.requestFocus();
				Document doc = tracePanel.getDocument();
				try{
					String text = doc.getText(0,doc.getLength());
					int x = text.lastIndexOf(tracePanel.prompt);
					tracePanel.getCaret().setDot(x+tracePanel.prompt.length()+1);
				}catch(BadLocationException e){}
			}

			public void executeLine(){
				String line1  = getLine();
				tracePanel.setExecuteFlag(true);
				boolean print = true;
				if(line1!=null && line1.length()>0){
					Status status = exec.execute(line1); //executeButton(line1);
					if (status.getStatusValue()==SUPRESS_PRINT)
						print = false;
					if (print) {
						println("");
						printStatus(status, line1);
					}
					else
						print("");
				}
				if (print) 
					putCaret();    
			}
		}
		
		Action action = new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent e){
				CommandLine info = new CommandLine(); 
//				println("");
				info.executeLine();
			}
		};

		Action prevLine = new AbstractAction(){
			@Override
			public void actionPerformed (ActionEvent e){
				Document doc = tracePanel.getDocument();
				try{
					if(prevIndex>=0){
						int x = doc.getText(0, doc.getLength()).lastIndexOf(tracePanel.prompt)+tracePanel.prompt.length()+1;
						tracePanel.getDocument().remove(x, doc.getLength()-x);	 
						tracePanel.getDocument().insertString(tracePanel.getDocument().getLength(), inputLine.elementAt(currentIndex),null);
						if (currentIndex>0)
							currentIndex--;
					}
				}catch(BadLocationException b){}
			}
		};

		Action nextLine = new AbstractAction(){
			@Override
			public void actionPerformed (ActionEvent e){
				Document doc = tracePanel.getDocument();
				try{
					if(prevIndex>=0){
						int x = doc.getText(0, doc.getLength()).lastIndexOf(tracePanel.prompt)+tracePanel.prompt.length()+1;
						tracePanel.getDocument().remove(x, doc.getLength()-x);	 
						tracePanel.getDocument().insertString(tracePanel.getDocument().getLength(), inputLine.elementAt(currentIndex),null);
						if (currentIndex< inputLine.size()-1)
							currentIndex++;

					}
				}catch(BadLocationException b){}
			}
		};

		KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0);
		KeyStroke up = KeyStroke.getKeyStroke(KeyEvent.VK_UP,0);
		KeyStroke down = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,0);

		Keymap map = TracePanel.addKeymap("newKeymap",tracePanel.getKeymap());
		map.addActionForKeyStroke(enter, action);
		map.addActionForKeyStroke(up, prevLine);
		map.addActionForKeyStroke(down, nextLine);
		tracePanel.setKeymap(map);
		println("ready");
	}
	
	public void print (String tempTxt, boolean tempNormal, final String color) {
		if (tracePanel!=null) {
			String txt;
			txt = tempTxt;
			boolean stripHTML = true;
			if (txt.trim().startsWith("<")) {
				int s = txt.indexOf('<');
				if (txt.length()>(s+5) && txt.substring(s, s+5).equalsIgnoreCase("<html")) {
					stripHTML = false;
				}
			}
			if (stripHTML) {
				txt = "<small><pre width=\"79\" wrap>"+txt.replaceAll("<", "&lt;")+"</pre></small>";
//				txt = txt.replaceAll("<", "&lt;");
//				txt = txt.replaceAll(" ", "&nbsp;");
//				txt = txt.replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
			}
			txt = txt.replaceAll("\\n", "<br>");

			//			  tracePanel.printHTML("<font color="+color+">");
			//			  tracePanel.update(null,txt);
			//			  tracePanel.printHTML("</font>");
			tracePanel.update(null,"<html><font color="+color+">"+txt+"</font></html>");
		}
	}

  @Override
	public void print(String txt){
    print(txt, true, "purple");
  }


  @Override
	public void println (String txt) {
    print (txt + "\n");
  }

  public void printStatus (Status status, String commandString) {
    if (status==null || status.getStatusValue () >= 0) {
//      print ("<html><br><b>" + commandString + ":</b></html>");
//      print ("<html><br></html>");
      print(CASAUtil.formatWidth (status==null?"null":status.getExplanation (), 80, "        "), false,"blue");
			if (status instanceof StatusObject<?>) {
				Object obj = ((StatusObject<?>)status).getObject();
  			print (CASAUtil.formatWidth ("\n"+(obj==null?"null":obj.toString()), 80, "        "));
			}
    } else {
      print ("<html><br><b>"
          + commandString
          + "</b><font color=red> returned bad status "
          + String.valueOf (status.getStatusValue ())
          + " "
          + CASAUtil.formatWidth (status.getExplanation ()
              + "</font></html>", 80, ""));
    }
    //scrollMsgsToBottom ();
  }

  /**
   * this is a work-around for anonymous classes
   */
  protected Status executeButton( String c ){
	 return agent.executeCommand(c, this);
  }

	@Override
	public String ask(String prompt, String help, int type, String _default) {
    String ans = (String) JOptionPane.showInputDialog (frame, (help != null
        ? help + "\n-------------------------------\n" : "")
        + prompt
        + (_default != null ? " [default=" + _default + "]" : "")
        + ":", agent.getAgentName (), JOptionPane.INFORMATION_MESSAGE, null,
        null, _default);
    if (ans == null)
      return null; //cancelled
    ans = ans.trim ();
    if (ans.length () == 0)
      return _default;
    switch (type < 0 ? -type : type) {
      case TYPE_INT:
        try {
          Integer.parseInt (ans);
        } catch (NumberFormatException ex1) {
          return ask (prompt, help, -type, _default);
        }
      break;
      case TYPE_FLOAT:
        try {
          Float.parseFloat (ans);
        } catch (NumberFormatException ex1) {
          return ask (prompt, help, -type, _default);
        }
      break;
      case TYPE_BOOLEAN:
        try {
          if ("|true|yes|t|y|on|1|".indexOf ("|" + ans.toLowerCase () + "|") >= 0)
            ans = "true";
          else if ("|false|no|f|n|off|0|".indexOf ("|" + ans.toLowerCase() + "|") >= 0)
            ans = "false";
          else
            return ask (prompt, help, -type, _default);
        } catch (NumberFormatException ex1) {
          return ask (prompt, help, -type, _default);
        }
      break;
      default:
      break;
    }
    return ans;
	}

	@Override
	public OutputStream getOutStream() {
		return outStream;
	}

	@Override
	public void start() {
	}

	@Override
	public boolean takesHTML() {
		return true;
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub
		tracePanel.update(arg0, arg1);
	}
  

}
