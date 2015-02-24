package casa;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * This is a thin wrapper for java.util.LinkedList.
 *
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
 *
 */
public abstract class ItemBuffer<T> implements Iterable<T> {

    /** refers to the actual underlying data structure being used */
    private LinkedList<T> items;

    /** Constructs an empty ItemBuffer   */
    protected ItemBuffer() {
        this.items = new LinkedList<T>();
    }

    /**
     * Returns the first object in the list
     *
     * @return first object in the list
     */
    protected synchronized T getItem() {
        return (items.size() > 0) ? items.removeFirst() : null;
    }

    protected synchronized boolean remove(Object o) {
      return items.remove(o);
    }
    
    protected synchronized T peek() {
    	return items.peek();
    }

    /**
     * Inserts the <em>item<em> into the buffer at the position specified by
     * the insertPos(<em>item</em>) method.  By default insertPos() returns
     * the size of the buffer, so the default behaviour to append <em>item</em>.
     * Subclasses should override insertPos() to change this behaviour.
     *
     * @param item object to insert into the list
     */
    protected void putItem( T item ) {
        if(item != null) {
        	synchronized (this) {
        		int pos = insertPos(item);
        	  items.add( pos, item );
        	}
        }
        notifyNewItem(item);
    }
    
    /**
     * This method is called whenever {@link #putItem(Object)} is called.  Subclasses
     * should override to provide the actual behaviour.  This method does nothing.
     * @param item the item that was queued
     */
    protected void notifyNewItem (T item) {
    }

    /**
     * Returns true iff the buffer is empty
     * @return true iff the buffer is empty
     */
    public synchronized boolean isEmpty() { return items.isEmpty(); }

    /**
     * Template method to be overridden to change the behaviour of
     * method putItem().  This implementation returns the size of the buffer,
     * causing putItem() to append new items to the buffer.  You could, for example,
     * always return 0, which would make it behave like a stack.
     * @param item the item to decide where to put in the buffer
     * @return the size of the buffer.
     */
    protected synchronized int insertPos(T item) {
      return items.size();
    }

    /**
     * Warning: the returned iterator is NOT synchronized.
     * @return an iterator for the buffer
     */
    public Iterator<T> iterator() {
      return items.iterator();
    }

    /**
     * Returns the number of items in the buffer
     * @return the number of items in the buffer
     */
    public synchronized int size() {return items.size();}

  	@Override
		public synchronized String toString() {
  		StringBuffer b = new StringBuffer();
  		for (T e: items) 
  			b.append(casa.util.CASAUtil.serialize(e)).append('\n');
  		return b.toString();
  	}
  	
  	public synchronized boolean contains(T item) {
  		return items.contains(item);
  	}
}
