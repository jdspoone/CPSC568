package casa.agentCom;

import casa.AbstractProcess;
import casa.MLMessage;
import casa.Status;
import casa.util.Pair;
import casa.util.Trace;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class is the TCP/IP specialization of the {@link Channel} class.  It is
 * responsible for maintaining information related to a {@link SelectionKey}, and
 * it's main interface to agents is its {@link #sendMessage(AbstractProcess, MLMessage)}
 * method (obligated by the {@link Channel} interface), which is responsible for 
 * actually sending a message through a TCP/IP channel. Objects of this class also
 * store a buffer and index for use by the owner socket server.
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
 * 
 * @version 0.9
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @author  <a href="http://http://www.pcs.cnu.edu/~flores/">Roberto Flores</a>
 */
public class TCPChannel extends Thread implements Channel, InfiniteReadWriteByteBufferInterface {
	
	/** The address of the remote side of the connection */
	private SocketAddress address;
	
	/** The URL of the remote side of the connection */
	private URLDescriptor remoteURL;
	
	
	/** 
	 * The Selection associated with this channel.  The protection is package because
	 * the {@link SocketServerTCPIP} object must update it asynchronously to the 
	 * constructor.  The key will have this TCPChannelGlobal object associated with it as
	 * its {@link SelectionKey#attachment()} object.
	 */
	SelectionKey key;
	
	/**
	 * The socket server that "owns" this object.
	 */
	private SocketServerTCPIP owner;
	
	/**
	 * Create a new channel by connecting to a remote server socket.  
	 * ie: start the channel by SENDING a message to the server.
	 * NOTE: {@link #key} is not necessarily set by the end of this constructor, but it
	 * will be set by the next cycle of {@link SocketServerTCPIP} by SocketServerGlobal.
	 * @param creator The agent creating this new object. Used for logging.
	 * @param url The URL of the agent to connect to.
	 * @param owner The socket server that "owns" this object.
	 * @throws IOException if something goes wrong.
	 */
	public TCPChannel(AbstractProcess creator, URLDescriptor url, SocketServerInterface owner) throws IOException {
		creator.println("sockets5", "Creating TCPChannelGlobal to url "+url+" due to sending a message.");
		this.owner = (SocketServerTCPIP)owner;
		assert creator!=null;
		assert url!=null;
		this.remoteURL = url;
		int port   = url.getPort();
		String host   = url.getHostString();
		address = new InetSocketAddress( host, port );
		key     = getConnection( url );

		if (key != null) {
			throw new IOException(creator.println("error", "TCPChannel.init(): SelectionKey already exists: "+toString()));
		}
		else {
			try {
				SocketChannel channel;
				channel = SocketChannel.open();
				channel.configureBlocking( false );

				synchronized (this) {
					channel.connect( address );
					long timeout = System.currentTimeMillis()+2000;
					while (channel.isConnectionPending() && System.currentTimeMillis()<timeout) {
						if (!channel.finishConnect())
							try {
								sleep(200);
							} catch (InterruptedException e) {}
					}
					if   (!channel.finishConnect()) {
						throw new SocketTimeoutException("TCPChannelGlobal.<init>(): Connection timed out (2000 ms), url="+url+", attempted by agent "+creator.getName());
					}
				}

//				channel.socket().setKeepAlive(true);
				this.owner.registerChannel(channel, this);
				waitForKey();
				url.setChannel(this);
				key.attach(this);
			} catch (IOException e) {
				if (key!=null) key.cancel();
				creator.println("error", "TCPChannel.init(): Unexpected exception: "+toString(), e);
				throw e;
			}
		}
		creator.println("sockets", "Created TCPChannelGlobal to url "+url+" due to sending a message: "+toString());
	}

	/**
	 * Create a new Channel from a incoming message on a server socket.
	 * ie: start the channel by RECEIVING a message (as a server).
	 * NOTE: {@link #key} is not necessarily set by the end of this constructor, but it
	 * will be set by the next cycle of {@link SocketServerTCPIP} by SocketServerGlobal.
	 * @param url The url of the agent making the connection.
	 * @param key The key for the channel just accepted by the socket server.
	 * @param owner The socker server that "owns" this object.
	 * @param initBuffer A buffer to be used as this object's channel input buffer.
	 * @throws IOException if {@link SocketChannel#configureBlocking(boolean)} throws an exception.
	 */
	TCPChannel(URLDescriptor url, SelectionKey key, SocketServerInterface owner, InfiniteReadWriteByteBuffer initBuffer) throws IOException {
		Trace.log("sockets5", "Creating TCPChannelGlobal to url "+url+" due to receiving a message.");
		this.owner = (SocketServerTCPIP) owner;
		assert url!=null;
		assert key!=null;
		this.key = key;
		this.remoteURL = url;
		url.setChannel(this);
		SocketChannel channel = (SocketChannel)key.channel();
		channel.configureBlocking( false );
//		channel.socket().setKeepAlive(true);
		this.owner.registerChannel(channel, this);
		if (initBuffer!=null)
			buffer = initBuffer;
		Trace.log("sockets", "Created TCPChannelGlobal to url "+url+" due to recieving a message: "+toString());
	}

	/** Used to store queued outgoing messages for this connection until the socket is ready to write. */
	private ConcurrentLinkedQueue<Pair<AbstractProcess, MLMessage>> writeQueue = new ConcurrentLinkedQueue<Pair<AbstractProcess, MLMessage>>();
	
	/**
	 * @return If there is any queued outgoing messages, return one of them (and remove it from the queue), otherwise returns null.
	 */
	public Pair<AbstractProcess, MLMessage> getNextOutMessage() {
		if (writeQueue.isEmpty())
			return null;
		return writeQueue.poll();
	}
	
	/**
	 * This method doesn't actually immediately send a message right away, but queues the message for 
	 * sending once the socket is write-ready.
	 * @param sender The sender of the message. Primarily used for logging.
	 * @param message The message to send.  The destination agent is specified in the RECEIVER field.
	 */
	@Override
	public Status sendMessage(AbstractProcess sender, MLMessage message) throws ClosedChannelException {
		assert message!=null;
		writeQueue.add(new Pair<AbstractProcess, MLMessage>(sender, message));
		
		//It's possible that key has not yet been instantiated by SocketServerGlobal, so we must wait for it.
		waitForKey();
		
		owner.queueAWrite(key); // tells the SocketServerGlobal to pick up the queued messages when it can.
		if (sender!=null)
			sender.println("sockets", "TCPChannelGlobal queued an outgoing message: "+toString());
		return new Status(0);
	}
	
	/**
	 * The key is instantiated by {@link SocketServerTCPIP} asynchronously, so this utility method
	 * waits around for that to happen.
	 */
	private void waitForKey() {
		while (key==null) {
			try {
				sleep(200);
 	 		} catch (InterruptedException e) {
 	 			continue;
			}
			Trace.log("warning", "TCPChannelGlobal: Waiting on key to be instantiated.");
		}
	}
	
	/**
	 * @return The URL of the agent at the remote end of the connection.
	 */
	public URLDescriptor getURL() {
		return remoteURL;
	}

	/**
	 * Utility method to return any TCPChannelGlobal currently associated with the <em>url</em>.
	 * @param url
	 * @return If there is a TCPChannelGlobal associated with the <em>url</em>, then return it, otherwise return null;
	 */
	private synchronized SelectionKey getConnection(URLDescriptor url) {
		Channel channel =  url.getChannel();
		if (channel != null && channel instanceof TCPChannel) {
			TCPChannel tcpc = (TCPChannel)channel;
			return tcpc.key;
		}
		return null;
	}
	
	/**
	 * Return a string form of the object in the form:
	 * <pre>
	 * localAddress -> remoteAddress {[ {closed} {unbound} ]}
	 * </pre>
	 * @return the string form of this object.
	 */
	@Override
	public String toString() {
		if (key==null)
			return "uninitialized TCPChannelGlobal";
		StringBuilder b = new StringBuilder();
		SocketChannel channel = (SocketChannel)key.channel();
		Socket socket = (channel!=null?channel.socket():null);
		String msg = "";
		if (socket==null)
			msg = "noSocket";
		else {
			if (socket.isClosed())
				msg = " closed";
			if (!socket.isBound())
				msg += " unbound";
		}
		if (msg.length()>0)
			msg = " ["+msg+" ]";
		if (socket==null) 
			b.append("<none>");
		else {
			b.append(socket.getLocalAddress())
			.append(':')
			.append(socket.getLocalPort());
		}
		b.append(" -> ");
		if (socket==null)
			b.append("<none>");
		else {
			b.append(socket.getInetAddress())
			.append(':')
			.append(socket.getPort());
		}
		b.append(msg);
		return b.toString();
	}

	/**
	 * A buffer for use by the owner socker server.
	 */
	private InfiniteReadWriteByteBuffer buffer = new InfiniteReadWriteByteBuffer();
	
	@Override
	public byte[] peakBuffer(int size) {
		return buffer.peakBuffer(size);
	}

	@Override
	public int bytesAvailableInBuffer() {
		return buffer.bytesAvailableInBuffer();
	}

	@Override
	public byte[] readBuffer(int size) {
		return buffer.readBuffer(size);
	}

	@Override
	public void writeBuffer(byte[] data) {
		buffer.writeBuffer(data);
	}

	@Override
	public int getBytesExpected() {
		return buffer.getBytesExpected();
	}

	@Override
	public void putBytesExpected(int n) {
		buffer.putBytesExpected(n);
		
	}
}
