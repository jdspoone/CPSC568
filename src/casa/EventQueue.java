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

import casa.event.Event;
import casa.event.MessageEvent;

import java.util.Iterator;

/**
 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
public class EventQueue extends ItemBuffer<Event> implements MessageReceiver{

  private boolean usePriorities = true;
//  @Deprecated
//  private Thread waitingThread = null;
//  @Deprecated
//  private String waitingReplyWith = null;
  /**
	 */
  private AbstractProcess agent = null;

  /**
   * Constructs and empty EventQueue associating it with an agent that will
   * be interrupted whenever an event is queued.
   * @param agent
   */
  protected EventQueue(AbstractProcess agent) {
  	this.agent = agent;
  }
  
  public synchronized MLMessage getMessageInReplyTo(MLMessage msg) {
    String r = msg.getParameter(ML.REPLY_WITH);
    for (Iterator<Event> i=iterator(); i.hasNext(); ) {
      MLMessage m = (MLMessage)i.next();
      String inReplyTo = m.getParameter(ML.IN_REPLY_TO);
      if (inReplyTo!=null && inReplyTo.equals(r)) {
        if (remove(m))
          return m;
      }
    }
    return null;
  }

//  /**
//   * Sets up to have the putMessage() notify the thread if a message with
//   * a in-reply-to field matching replyWithString.  There can be only one
//   * thread waiting at a time, otherwise this method fails and returns false.
//   * @param thread
//   * @param replyWithString
//   * @return True if it's set up, false otherwise
//   */
//  @Deprecated
//  public synchronized boolean setWaitThread(Thread thread, String replyWithString) {
//    if (!(waitingThread==null && waitingReplyWith==null)) {
//      DEBUG.PRINT("MessageBuffer.setWaitThread: called without clearing previous thread");
//      return false;
//    }
//    waitingThread = thread;
//    waitingReplyWith = replyWithString;
//    return true;
//  }

//  /**
//   * Clears the notify the waiting thread thread.
//   */
//  @Deprecated
//  public synchronized void clearWaitThread() {
//    waitingThread = null;
//    waitingReplyWith = null;
//  }

  /**
   * Inserts an Event at the end of the buffer and checks to see if there
   * is a thread waiting on the message (in which case, it notifies the thread).
   *
   * @param event message to insert
   */
  @Override
  public synchronized void putItem( Event event ) {
  	event.setQueueTime(System.currentTimeMillis());
  	agent.println("eventqueue", "EventQueue.putItem(): Queuing event: "+event);
  	super.putItem( event );
//  		if (waitingThread!=null && event instanceof MessageEvent) {
//  			if (waitingReplyWith.equals(((MessageEvent)event).getMessage().getParameter(ML.IN_REPLY_TO))) {
//  				waitingThread.interrupt();
//  				clearWaitThread();
//  			}
//  		}
  }

	@Override
	protected synchronized Event getItem() {
		if (agent.isLoggingTag("eventqueue9")) {
			StringBuilder b = new StringBuilder("Event queue contents:");
			int n = 0;
	    for (Iterator<Event> i = iterator(); i.hasNext();) {
	    	Event e = i.next();
	    	b.append("\n    ").append(++n).append(". ").append(e.toString());
	    }
	    agent.println("eventqueue9", b.toString());
		}
		Event event = super.getItem();
		if (event==null) {
	  	agent.println("eventqueue3", "EventQueue.getItem(): Attempt to Dequeue an event, but none available");
		}
		else {
  	  agent.println("eventqueue", "EventQueue.getItem(): Dequeuing event: "+event);
		}
		return event;
	}
	
  
  @Override
  protected void notifyNewItem(Event e) {
  	//agent.interrupt();
  	if (agent!=null) {
  		java.lang.Thread.State threadState = agent.getState();
  		//if (threadState.equals(java.lang.Thread.State.WAITING)||threadState.equals(java.lang.Thread.State.TIMED_WAITING)) {
  		//  	  synchronized (agent) {
  		agent.bump();
  		//  	  }
  	}
  }

  /**
   * Override of the template method change the behaviour of
   * method putItem().  This implementation returns a position
   * at the end of any other elements with a equal-or-less-than
   * priority, making it a order-preserving priority queueu.  
   * You could, for example, always return 0, which would make
   * it behave like a stack.
   * @param item the item to decide where to put in the buffer
   * @return the placement for an order-preserving priority queue.
   */
  @Override
	protected synchronized int insertPos(Event item) {
    if (!usePriorities) return super.insertPos(item);
    int thisPriority = item.getPriority();
    int pos = 0;
    for (Iterator<Event> i = iterator(); i.hasNext(); pos++) {
      int priority = i.next().getPriority();
      if (thisPriority>priority) break;
    }
    return pos;
  }

  /**
   * Set to true to use priority fields in messages to control the order that
   * messages are dequeued from the buffer.  If set to false, the queue will be
   * simple queue.
   * @param usePriority set to true to make this a priority queue, false for a simple FIFO queue.
   * @return the old value of usePriorities
   */
  public boolean setUsePriority(boolean usePriority) {
    boolean temp = this.usePriorities;
    this.usePriorities = usePriority;
    return temp;
  }

  /**
   *
   * @return true if using priority messages, false if this is set to a simple FIFO queue.
   */
  public boolean getUsePriority() {
    return usePriorities;
  }

	public void messageReceived(MLMessage message) {
		MessageEvent event = new MessageEvent(ML.EVENT_MESSAGE_RECEIVED,agent,message);
		event.start();
	}

}
