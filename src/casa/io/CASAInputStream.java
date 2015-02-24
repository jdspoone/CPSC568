package casa.io;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * A subclass of java.io.InputStream intended as a basis for stream input from a CASA file. <p> Note that CASAInputStream does not have the reserved node restriction that CASAOutputStream has.  That is, all nodes within a file can be read using the CASAInputStream. <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public class CASAInputStream extends java.io.InputStream {

    static private int BUFFER_LENGTH = 128;

    private int available;
    private int[] buffer;
    private int bufferIndex = 0;

    private String node = null;

    private boolean validStream = false;

    /**
		 */
    private FilePosition lastRead = null;
    /**
		 */
    private FilePosition initialPosition = null;

    public CASAInputStream(String nodeName) throws CASAIOException, IOException {
        this(new NodePosition(nodeName, 0L), new CASAFile());
    }

    public CASAInputStream(String nodeName, CASAFile file) throws CASAIOException, IOException {
        this(new NodePosition(nodeName, 0L), file);
    }

    public CASAInputStream(NodePosition pos, CASAFile file) throws CASAIOException, IOException {
        super();

        if(pos == null) throw new NullPointerException("an initial node position is required for CASAInputStream");
        if(file == null) throw new NullPointerException("a CASAFile is required for CASAInputStream");
        if(!file.isCASAFile()) throw new CASAIOException("file " + file.getCanonicalPath() + " for CASAInputStream is not a CASA file ("+file.validityCheckExplanation+")");

	// get corresponding file position (returns null if node not in file)
        initialPosition = CoreIO.getFilePosition(file, pos);
        if(initialPosition == null) throw new NodeNotFoundException(pos.getNodeName());

        lastRead = new FilePosition(initialPosition);

        buffer = new int[BUFFER_LENGTH];
        bufferIndex = 0;
        available = 0;
	node = pos.getNodeName();
        validStream = true;
    }

    /*
     * protected constructor for internal use (assumes positions are valid etc.)
     */
    protected CASAInputStream(FilePosition pos, String nodeName) throws CASAIOException, IOException {
        super();
        initialPosition = new FilePosition(pos);
        lastRead = new FilePosition(pos);

        buffer = new int[BUFFER_LENGTH];
        bufferIndex = 0;
        available = 0;
	node = nodeName;
        validStream = true;
    }


    /*
     * fill the internal buffer with bytes, essentially a low-level buffered read...
     * returns actual number of bytes read, 0 if EOF reached
     */
    private int fillBuffer() throws IOException {
        // need a last read position to start from
        if(lastRead == null) throw new CASAIOException("CASAInputStream.fillBuffer() - lastRead position == null");

        int bytesRead = 0;
        RandomAccessFile aFile = null;

        try {
            aFile = new RandomAccessFile(lastRead.getFile(), "r");

            CASAFileBlockHeader header = CoreIO.readBlockHeader(lastRead.getBlockOffset(), aFile);

            int blockAvailable = (int)(header.getDataOffset() - lastRead.getDataOffset());

            byte[] bytes = new byte[BUFFER_LENGTH];

            int toRead = (blockAvailable >= BUFFER_LENGTH) ? BUFFER_LENGTH : blockAvailable;

            bytesRead = 0;

            if(toRead > 0){
                // can read some bytes from current block so do so and set values as appropriate
                aFile.seek(lastRead.getBlockOffset() + CASAFileBlockHeader.HEADER_SIZE + lastRead.getDataOffset());
                aFile.readFully(bytes, 0, toRead);
                bytesRead = toRead;
            }

            if((bytesRead == BUFFER_LENGTH) || (header.getNextOffset() <= 0)){
                // either complete read or partial read with no next
                lastRead.setDataOffset(lastRead.getDataOffset() + bytesRead);
            } else {
                // read from next block (won't need to recursively read, as block data sizes are > buffer size)
                int index = bytesRead;
                long next = header.getNextOffset();
                toRead = BUFFER_LENGTH - bytesRead;
                // move to next block and finish reading from there (minimum block size is > buffer size)
                header = CoreIO.readBlockHeader(next, aFile);
                blockAvailable = (int)header.getDataOffset();

                if(blockAvailable < toRead) toRead = blockAvailable;

                if(toRead > 0){
                    // can read some bytes from current block so do so and set values as appropriate
                    aFile.seek(next + CASAFileBlockHeader.HEADER_SIZE); // seek to start of data section in next block
                    aFile.readFully(bytes, index, toRead);              // read bytes
                    bytesRead += toRead;
                }

                // set file position values
                lastRead.setOffsets(next, toRead);
            }

            // copy byte buffer to internal int buffer if bytes were read
            for(int i = 0; i < bytesRead; i++)
                buffer[i] = CASAFileUtilities.INT_BYTE_MASK & (int)bytes[i];

        } catch(IOException ioe){
            throw ioe;
        } finally {
            // ensure no open file handles!
            if(aFile != null) aFile.close();
        }

        return bytesRead;
    }


    /////////////////////////////////////////////////
    // implementation of inherited abstract methods
    /////////////////////////////////////////////////

    /**
     * Reads a byte from this input stream. The general contract for read is that one
     * byte is read from the input stream. The byte read is the eight low-order bits
     * of the int returned. The 24 high-order bits of the return value should be ignored.
     * @return -1 on EOF
     */

    public int read() throws IOException {
        if(!validStream) throw new CASAIOException("CASAInputStream.read() - stream no longer valid");

        int retVal = -1;
        if(available >= 0){
            if(bufferIndex < available) retVal = buffer[bufferIndex++];
            else {
                bufferIndex = 0;           // reset buffer index
                available = fillBuffer();  // fill buffer (returns # bytes read)
                retVal = (available > 0) ? buffer[bufferIndex++] : -1;
            }
        }

        return retVal;
    }

    /////////////////////////////////////////////////
    // implementation of overridden methods
    /////////////////////////////////////////////////

    public int available() throws IOException {
        if(!validStream) throw new CASAIOException("CASAInputStream.available() - stream no longer valid");
        return available;
    }

    public void reset() throws IOException {
        lastRead.setOffsets(initialPosition.getBlockOffset(), initialPosition.getDataOffset());
        available = 0;  // force refill of buffer on next read
    }

    public void close() throws IOException {
	if(validStream) validStream = false;
	super.close();
    }

}
