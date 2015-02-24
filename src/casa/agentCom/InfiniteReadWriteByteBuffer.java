package casa.agentCom;

import java.util.Arrays;

/**
 * A simple "infinite" byte buffer designed to be for a writer to write byte[]s to and
 * a read to read what the writer has written.
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003-2014, Knowledge Science Group, University of Calgary. 
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
public class InfiniteReadWriteByteBuffer implements InfiniteReadWriteByteBufferInterface {

	private byte[] buffer = new byte[0];
	private int bytesExpected;
	
	@Override
	public void writeBuffer(final byte[] data) {
		synchronized (buffer) {
			byte[] combined = new byte[buffer.length + data.length];
			System.arraycopy(buffer,0,combined,0            ,buffer.length);
			System.arraycopy(data  ,0,combined,buffer.length,data.length);
			buffer = combined;
		}
	}
	
	@Override
	public byte[] readBuffer(int size) {
		synchronized (buffer) {
			assert size<=buffer.length;
			byte[] ret = new byte[size];
			ret = Arrays.copyOf(buffer, size);
			buffer = Arrays.copyOfRange(buffer, size, buffer.length);
			return ret;
		}
	}
	
	@Override
	public int bytesAvailableInBuffer() {
		synchronized (buffer) {
			return buffer.length;
		}
	}
	
	@Override
	public byte[] peakBuffer(int size) {
		synchronized (buffer) {
			assert size<=buffer.length;
			byte[] ret = new byte[size];
			ret = Arrays.copyOf(buffer, size);
			return ret;
		}
	}

	@Override
	public int getBytesExpected() {
		return bytesExpected;
	}

	@Override
	public void putBytesExpected(int n) {
		bytesExpected = n;;
	}
}
