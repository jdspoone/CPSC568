package casa.agentCom;

 /**
 * A simple "infinite" byte buffer designed to be for a writer to write byte[]s to and
 * a read to read what the writer has written.
 * <p><b>Title:</b> CASA Agent Infrastructure</p>
 * <p><b>Copyright: Copyright (c) 2003-2014, Knowledge Science Group, University of Calgary.</b> 
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  
 * The  Knowledge Science Group makes no representations about the suitability
 * of  this software for any purpose.  It is provided "as is" without express
 * or implied warranty.</p>
 * <p><b>Company:</b> Knowledge Science Group, Department of Computer Science, University of Calgary</p>
 * @version 0.9
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public interface InfiniteReadWriteByteBufferInterface {

	/**
	 * @param size
	 * @return The first <em>size</em> elements of the buffer.
	 */
	public abstract byte[] peakBuffer(int size);

	/**
	 * @return The number of bytes available for read in the buffer.
	 */
	public abstract int bytesAvailableInBuffer();

	/**
	 * Returns the first <em>size</em> elements from the buffer and removes them
	 * from the buffer.
	 * @param size Specifies the number or elements to read from the buffer.
	 * @return the first <em>size</em> bytes from the buffer.
	 */
	public abstract byte[] readBuffer(int size);

	/**
	 * Adds the <em>data</em> to the end of the buffer.  This data may later be
	 * read by {@link #readBuffer(int)}.
	 * @param data The data to be written into the end of the buffer.
	 */
	public abstract void writeBuffer(final byte[] data);
	
	/**
	 * @return The number last written by {@link #putBytesExpected(int)}.
	 */
	public abstract int getBytesExpected();
	
	/**
	 * Updates the number that will be returned by {@link #getBytesExpected()}.
	 * @param n The number to be returned by subsequent calls to {@link #getBytesExpected()}.
	 */
	public abstract void putBytesExpected(int n);

}