package casa.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This data structure allows multiple, independent output iterators to be held.
 * 
 * @author Jason Heard
 */
public class AsynchronousMultiOutQueue<T> {
	private Node head;
	
	public AsynchronousMultiOutQueue() {
		head = new Node ();
	}

	public synchronized void enqueue (T newItem) {
		head.data = newItem;
		Node tempNode = head;
		head = new Node ();
		tempNode.next = head;
	}
	
	public Iterator<T> getIterator () {
		return new NotificationIterator ();
	}
	
	private class NotificationIterator implements Iterator<T> {
		private Node current;
		
		public NotificationIterator () {
			current = head;
		}

		public boolean hasNext() {
			return current.next != null;
		}

		public synchronized T next() {
			if (current.next != null) {
				T temp = current.data;
				current = current.next;
				return temp;				
			} else {
				throw new NoSuchElementException ();
			}
		}

		public void remove() {
			throw new UnsupportedOperationException ();
		}
	}
	
	private class Node {
		public Node next = null;
		public T data = null;
	}
}