package casa.util;


/**
 * Simple trace monitor.  This can be used to dump traces to System.out, or to a GUI window.
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary.
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  The
 * Knowledge Science Group makes no representations about the suitability of
 * this software for any purpose.  It is provided "as is" without express or
 * implied warranty.</p>
 *
 * @author <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
import casa.ObserverNotification;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class TraceMonitor extends JFrame implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static String LINE_SEPARATOR = System.getProperty("line.separator");

	private String title = null;
	private LogWindow window = null;

	public TraceMonitor(){ this(null, false, false);}
	public TraceMonitor(String title){ this(title, false, false);}
	public TraceMonitor(String title, boolean showWindow){ this(title, showWindow, false);}

	public TraceMonitor(String title, boolean showWindow, boolean exitOnClose){
		if((title == null) || (title.length() <= 0))
			this.title = "Trace Monitor";
		else this.title = "Trace Monitor '" + title + "'";
		window = (showWindow ? new LogWindow(exitOnClose) : null);
	}

//	public void addTrace(Trace trace){
//		if(trace != null){
//			traces.add(trace);
//			trace.addLocalObserver(this);
//		}
//	}
//
//	public void removeTrace(Trace trace){
//		if(trace != null){
//			traces.remove(trace);
//			trace.deleteLocalObserver(this);
//		}
//	}

	public void showWindow(){
		if(window == null) window = new LogWindow(false);
		window.setVisible(true);
	}

	public void closeWindow(){
		if(window != null) window.closeWindow();
	}

	public void append(String msg){
		if(window == null) System.out.print(msg);
		else window.appendLog(msg);
	}

	public void appendLine(String msg){ append(msg + LINE_SEPARATOR);}

	@Override
	public void update(Observable o, Object arg){
  	String s;
  	if (arg instanceof ObserverNotification) {
  		Object object = ((ObserverNotification)arg).getObject();
  	  s = (object == null) ? "" : object.toString();
  	}
  	else {
  	  s = arg.toString();
  	}
		this.append(s);
	}

	private class LogWindow extends JFrame implements ActionListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private JTextArea log;

		public LogWindow(boolean exitOnClose){
			super(title);
			log = new JTextArea();
			log.setEditable(false);
			getContentPane().setLayout(new BorderLayout());
			getContentPane().add(new JScrollPane(log), BorderLayout.CENTER);
			//addWindowListener(this);
			JMenu menu = new JMenu("File");
			JMenuItem item = new JMenuItem("Save...");
			item.setActionCommand("save");
			item.addActionListener(this);
			menu.add(item);
			menu.addSeparator();
			item = new JMenuItem((exitOnClose ? "Exit" : "Close"));
			item.setActionCommand("close");
			item.addActionListener(this);
			menu.add(item);
			JMenuBar menubar = new JMenuBar();
			menubar.add(menu);
			setJMenuBar(menubar);
			pack();
			setSize(320,240);
			if(exitOnClose) setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setVisible(true);
		}

		public void saveLog(){
			JFileChooser chooser = new JFileChooser();
			File file = null;
			int option;
			boolean cancelled = false;
			do {
				if(chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) cancelled = true;
				else {
					file = chooser.getSelectedFile();
					if(file == null) JOptionPane.showMessageDialog(this, "Please select a file.",
							"No File", JOptionPane.ERROR_MESSAGE);
					else if(file.exists()){
						if(!file.isFile()) JOptionPane.showMessageDialog(this, "The selected file is not a regular file.",
								"Not A File", JOptionPane.ERROR_MESSAGE);
						else {
							option = JOptionPane.showConfirmDialog(this,"Overwrite file '"+file.getName()+"'?", "File Exists",JOptionPane.YES_NO_CANCEL_OPTION);
							if(option == JOptionPane.CANCEL_OPTION) cancelled = true;
							else if(option != JOptionPane.YES_OPTION) file = null;
						}
					}
				}
			} while(!cancelled && (file == null));

			if(!cancelled && (file != null)){
				PrintWriter out = null;
				try {
					out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
					out.println(log.getText());
					out.flush();
					out.close();
				} catch(Exception e){
					JOptionPane.showMessageDialog(this, "An unexpected exception occurred: "+e.getMessage(),
							"Error Saving Log", JOptionPane.ERROR_MESSAGE);
				} finally {
					if(out != null) try { out.close();} catch(Exception ee){}
				}
			}
		}

		public void appendLog(String msg){ log.append(msg);}
		@SuppressWarnings("unused")
		public void clearLog(){ log.setText("");}

		public void closeWindow(){
			window = null;
			this.dispose();
			if(this.getDefaultCloseOperation() == JFrame.EXIT_ON_CLOSE) System.exit(0);
		}

		////////////////////////////////////////////////////
		// Implementation of ActionListener interface
		////////////////////////////////////////////////////

		@Override
		public void actionPerformed(ActionEvent e){
			String cmd = e.getActionCommand();
			if(cmd.equals("close")) closeWindow();
			else if(cmd.equals("save")) saveLog();
		}
	}
}
