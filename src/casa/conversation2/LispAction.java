package casa.conversation2;

import casa.Status;
import casa.abcl.Lisp;
import casa.event.Event;

import java.util.TreeMap;

import org.armedbear.lisp.JavaObject;
import org.armedbear.lisp.LispObject;

public class LispAction implements Action<String> {
	Conversation conversation;
  String cmd;
  
  public LispAction(Conversation converstation, String cmd) {
  	this.conversation = converstation;
  	this.cmd = cmd;
  }
  
	@Override
  public Status execute(Event event) {
		TreeMap<String, LispObject> map = new TreeMap<String, LispObject>();
    map.put("conversation", new JavaObject(conversation));
    map.put("event", new JavaObject(event));
    map.put("agent", new JavaObject(conversation.agent));
		return Lisp.abclEval(conversation.agent, null, map, cmd, null);
	}
	
	@Override
	public void setAction(String action) {
		cmd = action;
	}
	
	@Override
	public String getAction() {
		return cmd;
	}
}
