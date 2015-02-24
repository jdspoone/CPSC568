package casa.agentCom;

import casa.AbstractProcess;
import casa.MLMessage;
import casa.exceptions.IPSocketException;
import casa.exceptions.MLMessageFormatException;
import casa.exceptions.URLDescriptorException;
import casa.util.CASAUtil;
import casa.util.LimitedSet;
import casa.util.Pair;
import casa.util.Trace;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class is designed to listen on a socket on behalf of one or more agents. For each connection it receives,
 * it creates a {@link TCPChannel} object representing the channel.  The constructor automatically
 * runs itself as a thread.  Objects of this class will Accept, Read, and Write {@link MLMessage} objects, and
 * maintain the socket connections until they are broken at the far end.<p>
 * Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @author  <a href="http://http://www.pcs.cnu.edu/~flores/">Roberto Flores</a>
 */
public class SocketServerTCPIP extends Thread implements SocketServerInterface
{
	//**************************** "constants" that could be set *********************************
	
	/** When the IP port given in negative, it means to try ports n++ until an open port is found, this limits the number of tries */
	private static final int numberOfPortsToTry = 100;
	
	/** If we fail to get a local host address, setting this to true will use the loopback address instead.  Otherwise we will fail.*/
  private static final boolean useLoopbackAsFallback = true;

	/** 
	 * Used in {@link #readAMessage(SocketChannel)} and {@link #writeAMessage(SelectionKey)}
	 * to decode/encode the incoming message buffer. 
	 */
	private static final Charset ENCODING = Charset.forName( "UTF-8"/*System.getProperty( "file.encoding" )*/);
	
	/** This is a 4-byte signature to be prepended to a message send/receive to identify the start of a message. 
	 * @see #writeAMessage(SelectionKey)
	 * @see #readABlockHeader(SocketChannel) 
	 * */
	private static final int SIGNATURE = 0xA6B45521; //0x31323334 ;
	
	//**************************** statics *******************************************************
	
  /** All the socket servers in the process */
  private static Vector<SocketServerTCPIP> all = new Vector<SocketServerTCPIP>();
  
  /** 
   * The size of header.
   * @see #readABlockHeader(byte[])
   */
	static final int HEADER_SIZE = 8;
	

  
  //***************************** dynamic object values *****************************************
  
	/** The server socket listing on port {@link #IPport} */
  private ServerSocket      socket;
  
  /** the local IP address of {@link #socket} */
  private InetAddress       IPaddress;
  
  /** The port number the {@link #socket} is listening on */
  private int               IPport;
  
  /** The selector object used in the {@link #run()} method */
	private Selector          selector;
	
	/**
	 * True if the we are lost in the channel and scanning for a valid header.
	 * @see #recover(InfiniteReadWriteByteBufferInterface)
	 */
	private boolean recoveryMode = false;

	/**
	 * Used by {@link #registerChannel(SocketChannel, TCPChannel)} and {@link #run()}
	 * to safely do new registrations without generating an interrupt on the socket listener thread.
	 */
	private ConcurrentLinkedQueue<Pair<SocketChannel, TCPChannel>> registerQueue = new ConcurrentLinkedQueue<Pair<SocketChannel, TCPChannel>>();
	
	/**
	 * A buffer to remember recently received messages to detect possible endless loops of resending messages.
	 * @see #queueMessage(MLMessage)
	 */
	private LimitedSet<MLMessage> recentMessages = new LimitedSet<MLMessage>(6);
	
  /**
   * Constructs a new SocketServer listening on the specified port.  The port number is negative, this constructor
   * will look for a empty port between <em>IPport</em> and <em>IPport</em>+{@link #numberOfPortsToTry}.  When using this
   * constructor the thread is started.
   * @param IPport  the port to listen on.  A negative number indicates to try other ports.
   * @throws IPSocketException if (<em>IPport</em> is negative) a valid port in the range <em>IPport</em> -> <em>IPport</em>+{@link #numberOfPortsToTry} is not
   *         found, or if (<em>IPport</em> is positive) <em>IPport</em> is not available.
   */
  public SocketServerTCPIP(int IPport) throws IPSocketException{
  	super("Global Socket Server");
  	all.add(this);

  	int port_to_try = Math.abs(IPport);
  	int maxPort = port_to_try + (IPport<0?numberOfPortsToTry:0);

  	while( port_to_try <= maxPort ) {
  		try {
  			this.setPort  ( port_to_try );
  			IPport = port_to_try;
  			this.setName("Global Socket Server at "+Integer.toString(port_to_try)+(IPport<0?" orSo":""));
  			this.start();
  			return;
  		}
  		catch( IOException e ) {
  			Trace.log("warning", "Port " +
  					String.valueOf(port_to_try) +
  					" already used." +
  					(IPport<0
  							? " Trying port "+String.valueOf(port_to_try+1)
  									: "") +
  									" (" + e.toString() + ")");
  			port_to_try += 1;
  			continue;
  		}
  	}
  	throw new IPSocketException( "Valid IPSocket not found"
  			+ ((IPport!=maxPort)
  					? " in range: "
  					+ Integer.toString(IPport) + "-"
  					+ Integer.toString(maxPort)
  					: ": "+ Integer.toString(IPport)));
  }

  /** 
   * @return All the SocketServers in this process.
   */
  public static SocketServerTCPIP[] get() {
  	SocketServerTCPIP ret[] = new SocketServerTCPIP[all.size()];
  	int i = 0;
  	for (SocketServerTCPIP s: all)
  		ret[i++] = s;
  	return ret;
  }

  /**
   * Set the port to listen on.
   *
   * @param IPport  the port to listen on
   *
   * @throws IOException when either 'e' is an instance of IOException or
   *         when getLocalHost() fails.  <-- In any of these cases, the port
   *         will fail to listen.
   *
   */
  private void setPort( int IPport ) throws IOException {
  	try{
  		this.IPaddress = CASAUtil.getLocalHost();
  	} catch(Exception e){
  		this.IPaddress = null;
  		if(!useLoopbackAsFallback){
  			Trace.log( "error",  "SocketServerGlobal.setPort()", e, 0);
  			if(e instanceof IOException) throw (IOException)e;
  			else throw new IOException("InetAddress.getLocalHost() failed: "+e.getMessage());
  		}
  	}

  	if ((this.IPaddress == null) && useLoopbackAsFallback){
  		Trace.log( "warning",  "SocketServerGlobal.setPort(): unable to get localHost, trying loopback address.");
  		try {
  			this.IPaddress = InetAddress.getByName(null);
  		} catch (Exception e){
  			this.IPaddress = null;
  			Trace.log( "error",  "SocketServerGlobal.setPort()", e, 0);
  			if(e instanceof IOException) throw (IOException)e;
  			else throw new IOException("InetAddress.getByName(null) failed: "+e.getMessage());
  		}
  	}

  	if(this.IPaddress == null){
  		System.out.println( "failed!!" );
  		throw new IOException("could not get local host address");
  	}

  	// addr found
  	this.IPport = IPport;

  	InetSocketAddress   address  = new InetSocketAddress( IPport );

  	selector = Selector.open();
  	ServerSocketChannel channel  = ServerSocketChannel.open();

  	channel.configureBlocking( false );
  	channel.register( selector, SelectionKey.OP_ACCEPT );

  	socket = channel.socket();
  	socket.bind( address );
  }

  /**
   * Retrieves the address of the listening socket
   * @returns address of the listening socket
   */
  @Override
	public String getHostAddress() {
    return IPaddress.getHostAddress();
  }

  /**
   * Retrieves the port of the listening socket
   * @returns port of the listening socket
   */
  @Override
	public String getLocalPortAsString() {
    return String.valueOf( IPport );
  }

  /**
   * Retrieves the port of the listening socket
   * @returns port of the listening socket
   */
  @Override
	public int getLocalPort() {
    return IPport;
  }
  
	/**
	 * The runnable obligation for this thread.  This method uses {@link #selector} to ACCEPT
	 * connections to channels, as well as to READ and WRITE through the selected channels. 
	 */
	@Override
	public void run() { // based on code from Roberto Flores 
		
		// An orphanedChannel is a channel that cannot yet be associated with a URL because we don't
		// know the URL yet.  This is because we have to OP_ACCEPT the channel before we get a message
		// from it in the next OP_READ operation which will contain the URL.
		Vector<SocketChannel> orphanedChannels = new Vector<SocketChannel>();

		// start server & register for incoming clients
		while (!socket.isClosed() && socket.isBound()) {
			SelectionKey key = null;
			try {
				// do any pending registrations.  Done here because we need to do it when selector.select() is not holding the lock on the internal structure in Selector.
				for (Pair<SocketChannel, TCPChannel> p: registerQueue) {
					SocketChannel ch = p.getFirst();
					TCPChannel tcp = p.getSecond();
					try {
						tcp.key = ch.register(selector, SelectionKey.OP_READ, tcp);
					} catch (ClosedChannelException e) {
						Trace.log("error", "SocketServer.run()", e, 0);
					}
					registerQueue.remove();
				}

				if (selector.select(0) > 0) {

					Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
					while (iterator.hasNext()) {
						key = iterator.next();
						iterator.remove();

						// [OP_ACCEPT] client received: register for future reads (but we use orphanedChanels to finish the job).
						if (key.isAcceptable()) {
							try {
								SocketChannel channel = ((ServerSocketChannel)key.channel()).accept();
								Trace.log("sockets", "SocketServerGlobal OP_ACCEPT on key: "+key+" (ops="+readyOpsToString(key.readyOps())+", channel="+key.attachment()+", accepted channel="+channel+")");
								channel.configureBlocking( false );
								channel.register( selector, SelectionKey.OP_READ );
								orphanedChannels.add(channel);
							}
							catch (IOException e) {
								Trace.log("error", "SocketServer.run()", e, 0);
							} 
						}
						// [OP_CONNECT] do nothing
						if (key.isConnectable()) {
							Trace.log("sockets", "SocketServerGlobal OP_CONNECT on key: "+key+" (ops="+readyOpsToString(key.readyOps())+", channel="+key.attachment()+")");
						}
						// [OP_READ] data received: read it and queue out to the appropriate channel (hopefully a local one).  (Also check for orphanedChannels and finish the job.)
						if (key.isReadable()) {
							SocketChannel channel = (SocketChannel)key.channel();
							Trace.log("sockets", "SocketServerGlobal OP_READ on key: "+key+" (ops="+readyOpsToString(key.readyOps())+", channel="+key.attachment()+", key.channel="+channel+")");
							try {
								readMessages(key, orphanedChannels);
							} catch (ClosedChannelException e1) {
								Trace.log("warning", "SocketServer.run() [OP_READ]: Socket closed, cancelling key: "+key+" (ops="+readyOpsToString(key.readyOps())+", channel="+key.attachment()+", key.channel="+channel+")");
								removeConnection(key); // The channel is closed, cancel the key (etc).
							}
						}
						// [OP_WRITE] data to send: send it
						if (key.isWritable()) {
							Trace.log("sockets", "SocketServerGlobal OP_WRITE on key: "+key+" (ops="+readyOpsToString(key.readyOps())+", channel="+key.attachment()+")");
							try {
								writeAMessage(key);
							} catch (ClosedChannelException e) {
								Trace.log("warning", "SocketServer.run() [OP_WRITE]: Socket closed, cancelling key: "+key+" (ops="+readyOpsToString(key.readyOps())+", channel="+key.attachment()+", key.channel="+key.channel()+")");
								removeConnection(key); // The channel is closed, cancel the key (etc).
							}
						}
					}
				}
			}
			catch (CancelledKeyException e) {} // Do nothing on CancelledKey -- we just canceled it in a previous if-statement.
			catch (Throwable e) {
				Trace.log("error", "Unexpected exception in SocketServerGlobal.run()"+(key==null?" during initialization":(", ops="+readyOpsToString(key.readyOps()))), e, 0);
			}
		}
		try {
			selector.close();
		} catch (IOException e) {
			Trace.log("error", "SocketServerGlobal.run(): Unexpected exception closing selector", e, 0);
		}
		Trace.log("sockets", "SocketServerGlobal closed");
	}
	
	/**
	 * External threads may call this method to register the <em>channel</em>.  Note that the 
	 * channel is NOT necessarily registered during this method's invocation, but is queued for 
	 * registration when it's safe to interrupt the socket listener thread.
	 * This method is synchronized with the i/o methods because it generates an interrupt in
	 * the socket listener thread, which would cause the channel to be closed if it happens
	 * during i/o operations.  
	 * @param channel
	 * @param tcpChannel
	 */
	synchronized void registerChannel(SocketChannel channel, TCPChannel tcpChannel) {
		Trace.log("sockets", "SocketServerGlobal registering channel "+channel);
		registerQueue.add(new Pair<SocketChannel, TCPChannel>(channel, tcpChannel));
		selector.wakeup();
		yield();
	}
	
	/**
	 * External threads may call this method to queue a write on the channel of <em>key</em>.  Note that the 
	 * write is NOT necessarily done during this method's invocation, but is queued for 
	 * when it's safe to interrupt the socket listener thread.
	 * This method is synchronized with the i/o methods because it generates an interrupt in
	 * the socket listener thread, which would cause the channel to be closed if it happens
	 * during i/o operations.  
	 * @param key
	 */
	public synchronized void queueAWrite(SelectionKey key) {
		Trace.log("sockets", "SocketServerGlobal queued a write on key: "+key+" (ops="+readyOpsToString(key.readyOps())+", channel="+key.attachment()+", key.channel="+key.channel()+")");
		key.interestOps(SelectionKey.OP_WRITE);
		selector.wakeup();
		yield();
	}
	
	/**
	 * Called when the key.channel is ready for writing, but there may be nothing there to write.
	 * All the message queued for writing in the key's {@link TCPChannel} are written, and
	 * the key's interest is set back to OP_READ.
	 * The message actually send on the channel is in the following format:
	 * <ol> 
	 * <li> a 4-byte "signature" equal to the {@link #SIGNATURE} constant of value {@value #SIGNATURE}.
	 * <li> a 4-byte length indicating the number of bytes in the body message
	 * <li> the actual message data encoded according to {@link #ENCODING} of {@value #ENCODING}
	 * </ol>
	 * This method is synchronized with to avoid interrupts (which would close the channel) from 
	 * {@link #registerChannel(SocketChannel, TCPChannel)} and {@link #queueAWrite(SelectionKey)}.
	 * @param key
	 * @return The message written (from a {@link TCPChannel}'s write queue) or null if there is 
	 * no message in the queue.
	 */
	private synchronized void writeAMessage(SelectionKey key) throws ClosedChannelException {
		TCPChannel tcpChannel = (TCPChannel)key.attachment();
		if (tcpChannel==null) // method may get called with a null tcpChannel during initialization.
			return;
		
		key.interestOps(SelectionKey.OP_READ);
		for (Pair<AbstractProcess, MLMessage> pair=tcpChannel.getNextOutMessage(); pair!=null; pair=tcpChannel.getNextOutMessage()) {
			MLMessage msg = pair.getSecond();
			AbstractProcess agent = pair.getFirst();
			ByteBuffer encodedMsg = ENCODING.encode(msg.toString());
			int msgSize = encodedMsg.limit();
			ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE+msgSize);
			buffer.putInt(SIGNATURE);
			buffer.putInt(msgSize);
			buffer.put(encodedMsg);
			buffer.flip();
			while (buffer.remaining()>0) {
				try {
					((SocketChannel)key.channel()).write(buffer);
				} catch (ClosedChannelException e) {
					String m = Trace.log("error", "SocketServerGlobal.writeAMessage(): channel closed, so key canceled (key="+key+", key.channel="+key.channel()+", key.attachment="+key.attachment()+")"+"\nmessage:\n"+msg.toString(true), e, 0);
					if (agent != null)
						agent.println("error", m, e);
					removeConnection(key);
					throw e;
				} catch (IOException e) {
					String m = Trace.log("error", "SocketServerGlobal.writeAMessage(): IOException sending message:\n"+msg.toString(true), e, 0);
					if (agent != null)
						agent.println("error", m, e);
				}
			}
			String m = Trace.log("sockets", "Sent message (TCP/IP, "+key.attachment()+")\n"+msg.toString(true));
			if (agent != null)
				agent.println("msg", m);
		}
	}

	/**
	 * Removed the URL as an attachment from the <em>key</em>, cancels the <em>key</em>,
	 * and removes the key/channel from the URL.
	 * This method is synchronized with to avoid interrupts (which would close the channel) from 
	 * {@link #registerChannel(SocketChannel, TCPChannel)} and {@link #queueAWrite(SelectionKey)}.
	 * @param key
	 */
	private synchronized void removeConnection(SelectionKey key) {
		assert key!=null;
		key.cancel();
		InfiniteReadWriteByteBufferInterface buf = (InfiniteReadWriteByteBufferInterface)key.attachment();
		key.attach(null);
		try {
			key.channel().close();
		} catch (IOException e) {}
		TCPChannel tcpChannel = (buf instanceof TCPChannel) ? (TCPChannel)buf : null;
		URLDescriptor url = (tcpChannel!=null) ? url = tcpChannel.getURL() : null;
		if (url!=null)
			url.setChannel(null);
		Trace.log("sockets", "Removed TCPChannel: "+key);
	}

	/**
	 * Attempt to recover from corrupt data by scanning for a legal header in the 
	 * byte buffer. When this method terminates, the <em>buffer</em> will either 
	 * begin with a valid header or there will be less than {@link #HEADER_SIZE} 
	 * ({@value #HEADER_SIZE}) bytes left in the buffer.  This behaviour allows
	 * a recovery to span multiple channel reads.<p>
	 * This is a VERY inefficient implementation of recover(), but fairly robust.
	 * @param buffer
	 */
	private void recover(InfiniteReadWriteByteBufferInterface buffer) {
		while (buffer.bytesAvailableInBuffer()>=HEADER_SIZE) {
			byte[] buf = buffer.peakBuffer(HEADER_SIZE);
			try {
				buffer.putBytesExpected(readABlockHeader(buf)); // this call (readABlockHeader()) will throw if we aren't looking at a legitimate block header.
				return;
			} catch (SignatureException e) {}
			buffer.readBuffer(1); // throw away the prefix of the buffer and try again.
		}
	}
	
	/**
	 * Does a read from the <em>key</em>'s TCP/IP channel and tries to interpret messages
	 * and queues them to the relevant agent's event queue.  Actually, if the message is NOT
	 * directed at a local agent, the message is forwarded to the agent the information is
	 * available in its {@link URLDescriptor}.
	 * @param key
	 * @param orphanedChannels
	 * @throws ClosedChannelException
	 */
	private synchronized void readMessages(SelectionKey key, Vector<SocketChannel> orphanedChannels) throws ClosedChannelException {
		SocketChannel channel = (SocketChannel)key.channel();
		InfiniteReadWriteByteBufferInterface inputBuffer = (InfiniteReadWriteByteBufferInterface)key.attachment();
		if (inputBuffer==null) {
			inputBuffer = new InfiniteReadWriteByteBuffer();
			key.attach(inputBuffer);
		}
		assert channel!=null;
		assert inputBuffer!=null;

		//read the data from the channel into tcp's buffer
		int totalBytesRead = 0;
		try {
			totalBytesRead = readData(key);
			Trace.log("sockets5", "SocketServerTCPIP.readMessages(): after raw read of "+totalBytesRead+" bytes, the current buffer is:\n  \""
					+CASAUtil.makeUnprintablesVisible(CASAUtil.bytesToString(inputBuffer.peakBuffer(inputBuffer.bytesAvailableInBuffer()), "UTF-8"))+'"');
			if (totalBytesRead==0) { //if we read nothing, the channel is closed.
				throw new ClosedChannelException();
			}
		} catch (ClosedChannelException e) {
			throw e;
		} catch (IOException e) {
			Trace.log("error", "SocketServerTCPIP.readMesssages(): Unexpected exception reading data from channel, closing channel.", e, 0);
			throw new ClosedChannelException();
		}
    
		if (recoveryMode) {
			recover(inputBuffer);
		}
		
		//use tcp's buffer to try to construct a MLMessage and forward it to an agent
		int available;
		while (inputBuffer.getBytesExpected() <= (available=inputBuffer.bytesAvailableInBuffer()) ) {
			
			//read a header?
			if (inputBuffer.getBytesExpected() == 0) {
				if (available>=HEADER_SIZE) {
				  try {
				  	int sizeOfBody = readABlockHeader(inputBuffer.readBuffer(HEADER_SIZE));
						inputBuffer.putBytesExpected(sizeOfBody);
					} catch (SignatureException e) {
						Trace.log("error", "SocketServerTCPIP.readMessages(): found bad block header (block signature not prefixing data block) . Discarding data and entering recovery mode.");
						inputBuffer.putBytesExpected(0);
						recoveryMode = true;
						recover(inputBuffer);
					}
				}
				else {
					return; // if we don't have at least a header, we just wait for the next read.
				}
			}
			
			available=inputBuffer.bytesAvailableInBuffer(); // "available" needs to be reset here because it may have changed in the above block
			if (inputBuffer.getBytesExpected() <= available) { //we have a message in tcp's buffer
				byte[] incomingMsg = inputBuffer.readBuffer(inputBuffer.getBytesExpected());
				inputBuffer.putBytesExpected(0);
				String data = ENCODING.decode(ByteBuffer.wrap(incomingMsg)).toString();
				MLMessage msg;
				try {
					msg = MLMessage.fromString(data);
				} catch (MLMessageFormatException e) {
					Trace.log("error", "SocketServerTCPIP.readMessages(): incomming message can't be parsed. Raw message:\n\""+CASAUtil.bytesToString(incomingMsg, "UTF-8")+'"', e, 0);
					continue;
				}
				synchronized (orphanedChannels) {
					if (orphanedChannels.contains(channel)) {
						try {
							URLDescriptor senderURL = msg.getSender();
							if (senderURL.getChannel() == null) { // it should be null, but its possible another thread has initialized it.
								assert senderURL.getChannel()==null; //if we are initializing this channel, there should not be a channel in the sender's URL.
								assert inputBuffer instanceof InfiniteReadWriteByteBuffer;; // similar to the above line
								addConnection(senderURL, key, (InfiniteReadWriteByteBuffer)inputBuffer);
							}
						} catch (URLDescriptorException e) {
							Trace.log("error", "SocketServerTCPIP.readMessages(): incomming message has unparsable URL field. Dropping message\n"+msg.toString(true), e, 0);
							removeConnection(key);
							orphanedChannels.remove(channel);
							break;
						} catch (IOException e) {
							Trace.log("error", "SocketServerTCPIP.readMessages(): Cound not register new connection for new message\n"+msg.toString(true), e, 0);
						}
					}
					orphanedChannels.remove(channel);
				}
				queueMessage(msg);
			}
		}
		if (inputBuffer.getBytesExpected()>0)
			Trace.log("sockets", "SocketServerTCPIP.readMessages(): read complete, waiting on "+(inputBuffer.getBytesExpected()-inputBuffer.bytesAvailableInBuffer())
					+" bytes after reading "+totalBytesRead+" bytes in the invokation.  Current buffer:\n  \""
					+CASAUtil.bytesToString(inputBuffer.peakBuffer(inputBuffer.bytesAvailableInBuffer()), "UTF-8")+'"');
	}
	
	/**
	 * Attempt to read an arbitrary number of bytes from <em>key</em>.channel(), and places it
	 * in <em>key</em>.attachment().buffer, where <em>key</em>.attachment() is a {@link TCPChannel} object 
	 * or a {@link InfiniteReadWriteByteBuffer}. 
	 * @param the key containing the channel and attachment objects.
	 * @return the number of bytes read.
	 * @throws IOException
	 */
	private int readData(SelectionKey key) throws IOException  {
		SocketChannel channel = (SocketChannel)key.channel();
		InfiniteReadWriteByteBufferInterface rwBuffer = (InfiniteReadWriteByteBufferInterface)key.attachment();
		assert channel!=null;
		assert rwBuffer!=null;
		int len=0, total=0;
		ByteBuffer buffer = ByteBuffer.allocate( 1024 ); //A buffer for reading
		rwBuffer.bytesAvailableInBuffer();
		while ((len=channel.read(buffer))>0) {
			rwBuffer.writeBuffer(Arrays.copyOf(buffer.array(), len));
			buffer.clear();
			total +=len;
		}
		return total;
	}
	
	/**
	 * Read a header, checking the signature (first 4bytes) and returning the size (in bytes)
	 * of the body that follows the header.
	 * @param buffer The bytes to read; must be {@link #HEADER_SIZE} ({@value #HEADER_SIZE}).
	 * @return the size of the data in the body of this block (excluding the header).
	 * @throws SignatureException if the signature in the header doesn't match.
	 */
	private int readABlockHeader(byte[] buffer) throws SignatureException  {
		assert buffer.length==HEADER_SIZE;
		ByteBuffer bb = ByteBuffer.wrap(buffer);
		if (bb.getInt()!=SIGNATURE) {
			String m = "SocketServerGlobal.run(): Expected casa socket signature at the beginning of an incomming message, but didn't get it..";
			Trace.log("warning", m);
			throw new SignatureException(m);
		}
		recoveryMode = false;
		return bb.getInt();
		
	}
	
	/**
	 * Queue a message that has been read to a (probably) local agent.  The "(probably)" is in the previous
	 * sentence because it's possible that we might be forwarding on the message, in which case this
	 * event will be logging as a <em>warning</em> tag.
	 * @param toURL
	 * @param message
	 * @return true if the message could be sent (the <em>toURL</em> had a channel); otherwise false.
	 * @throws ClosedChannelException 
	 */
	private boolean queueMessage(MLMessage message) throws ClosedChannelException {
		try {
			URLDescriptor toURL = message.getReceiver();
			Channel ch = toURL.getChannel();
			if (ch!=null) {
				if (ch instanceof DirectChannel) {
					Trace.log("channel", "SocketServerGlobal.run(): received message for local URL, forwarding. Message:\n"+message.toString(true));
				}
				else {
					if (recentMessages.contains(message)) {
						Trace.log("error", "SocketServerGlobal.run(): received message for non-local URL, forwarding wrapped (recursed).  Message dumped.  Message:\n"+message.toString(true));
						return false;
					}
					recentMessages.push(message);
					Trace.log("warning", "SocketServerGlobal.run(): received message for non-local URL, attempting forwarding. Message:\n"+message.toString(true));
				}
				ch.sendMessage(null, message);
				return true;
			}
			else {
				Trace.log("warning", "SocketServerGlobal.run(): incomming message can't be forwarded as channel (from URL) is null. Message:\n"+message.toString(true));
			}
		} catch (URLDescriptorException e) {
			Trace.log("error", "SocketServerGlobal.run(): incomming message receiver field can't be parsed. Message:\n"+message.toString(true), e, 0);
		}
		return false;
	}
	
	/**
	 * Creates a new {@link TCPChannel} object, and links it to <em>url</em>.  
	 * Also logs the creation through {@link Trace#log(String, String)} using the "msg" tag.
	 * This method is synchronized with to avoid interrupts (which would close the channel) from 
	 * {@link #registerChannel(SocketChannel, TCPChannel)} and {@link #queueAWrite(SelectionKey)}.
	 * @param url The URL under which to register the new connection. (The URL holds the connection object, since
	 * it is unique to the agent, whether local or remote.)
	 * @param channel The channel.
	 * @return the newly-created {@link TCPChannel} object.
	 * @throws IOException if {@link TCPChannel#TCPChannel(URLDescriptor, SelectionKey, SocketServerInterface)} throws an exception.
	 */
	private synchronized TCPChannel addConnection(URLDescriptor url, SelectionKey key, InfiniteReadWriteByteBuffer initBuffer) throws IOException {
		TCPChannel ch = new TCPChannel(url, key, this, initBuffer); 
		url.setChannel(ch);
		Trace.log("sockets", "Added new TCPChannelGlobal channel (prompted by receiving a message) "+ch.toString()+" to URL "+url);
		return ch;
	}

  /**
   * Method to close the socket server's open port and return it to the system.
   */
  @Override
	public void closePort () {
    try {
      socket.close ();
      socket = null;
    } catch (Exception e) {
    	Trace.log("error", "SocketServer.closePort()", e, 0);
    }
  }

  /**
   * This method is ignored.  This socket server is meant to be global to the process, so it will continue until the process shuts down.
   * @see SocketServerInterface#exit()
   */
	@Override
	public void exit() {
		//ignore this method call.
	}
	
	/**
   * Utility method to print out the OP codes in a more legible manner in the log messages.
   * @param code The OP code
   * @return A string version of the OP code set ("OP_ACCEPT ...").
   */
	private static String readyOpsToString(int code) {
		StringBuilder b = new StringBuilder();
		if ((code&SelectionKey.OP_ACCEPT)>0) {
			b.append(" OP_ACCEPT");
			code &= ~SelectionKey.OP_ACCEPT;
		}
		if ((code&SelectionKey.OP_CONNECT)>0) {
			b.append(" OP_CONNECT");
			code &= ~SelectionKey.OP_CONNECT;
		}
		if ((code&SelectionKey.OP_READ)>0) {
			b.append(" OP_READ");
			code &= ~SelectionKey.OP_READ;
		}
		if ((code&SelectionKey.OP_WRITE)>0) {
			b.append(" OP_WRITE");
			code &= ~SelectionKey.OP_WRITE;
		}
		if (code!=0)
			b.append(" UNKNOWN: ").append(code);
		return b.toString();
	}

}
