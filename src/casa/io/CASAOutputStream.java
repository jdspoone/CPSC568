package casa.io;

import java.io.IOException;

/**
 * A subclass of java.io.OutputStream intended as a basis for stream output to a CASA file. <p> Note that a CASAOutputStream is not allowed to write to any reserved nodes. <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
public class CASAOutputStream extends java.io.OutputStream {

    private static int BUFFER_LENGTH = 64;

    private int[] buffer;
    private int bufferIndex = 0;

    private String node = null;

    /**
		 */
    private FilePosition lastWrite = null;
    /**
		 */
    private FilePosition initialPosition = null;

    private boolean validStream = false;

    /*
     * public constructors are restricted from writing to the index node
     */
    public CASAOutputStream(String nodeName) throws CASAIOException, IOException {
        this(nodeName, CASAFile.MODE_APPEND, new CASAFile());
    }

    public CASAOutputStream(String nodeName, int writeMode, CASAFile file) throws CASAIOException, IOException {
        super();

        if(nodeName == null) throw new NullPointerException("Node name required for CASAOutputStream");
        if(CASAFileIndex.isReservedName(nodeName)) throw new CASAIOException("Node '" + nodeName + "' is reserved, writing not allowed");
        if(file == null) throw new NullPointerException("A CASAFile is required for CASAOutputStream");
        if(!file.isCASAFile()) {
          if (!file.exists()) file.createNewFile();
          else {
          	file.delete();
          	throw new CASAIOException("File " + file.getCanonicalPath() + " for CASAOutputStream is not a CASA file.  File deleted.");
          }
        }
        if(!file.isCASAFile()) throw new CASAIOException("File " + file.getCanonicalPath() + " for CASAOutputStream is not a CASA file, and couldn't create it");

        buffer = new int[BUFFER_LENGTH];
        bufferIndex = 0;
        node = nodeName;

        // find first write position
        CASAFileIndexEntry entry = file.getIndex().getEntry(node);

        if(entry == null){ // it's a new node
            initialPosition = new FilePosition(file, 0L, 0L);
            lastWrite = null;
        } else { // node exists in file, so save file offset and set first write position
            initialPosition = new FilePosition(file, entry.getOffset(), 0L);
            lastWrite = CoreIO.getFirstWritePosition(file, entry.getOffset(), writeMode);
        }

        validStream = true;
    }

    /**
     * protected constructors - allows writing to reserved nodes
     * assumes parameters etc are valid
     */
    protected CASAOutputStream(FilePosition pos, int writeMode) throws CASAIOException, IOException {
        super();
        buffer = new int[BUFFER_LENGTH];
        bufferIndex = 0;
        node = null;
        initialPosition = new FilePosition(pos);
        lastWrite = CoreIO.getFirstWritePosition(pos.getFile(), pos.getBlockOffset(), writeMode);
        validStream = true;
    }

    /////////////////////////////////////////////////
    // implementation of inherited abstract methods
    /////////////////////////////////////////////////

    /**
     * Writes the specified byte to this output stream. The general contract for
     * write is that one byte is written to the output stream. The byte to be written
     * is the eight low-order bits of the argument b. The 24 high-order bits of b are
     * ignored.
     */

    public void write(int b) throws IOException {
        if(!validStream) throw new CASAIOException("CASAOutputStream.write() - stream no longer valid");
        buffer[bufferIndex++] = b;
        if(bufferIndex >= BUFFER_LENGTH) flush();
    }

    /////////////////////////////////////////////////
    // implementation of overridden methods
    /////////////////////////////////////////////////

    /** Flush the internal buffer to the file. */
    public void flush() throws IOException {
        if(!validStream) throw new CASAIOException("CASAOutputStream.flush() - stream no longer valid");

        if(bufferIndex > 0){
            byte[] bytes = CASAFileUtilities.toByteArray(buffer, 0, bufferIndex);

            try {
                // if already a write performed continue where left off, otherwise it's a new node
                if(lastWrite != null) lastWrite = CoreIO.write(lastWrite, bytes, 0, bytes.length);
                else {
                    // free block available?
                    long blockOffset = initialPosition.getFile().getFreeList().getBlock();
                    if(blockOffset > 0){ // write to free block
                        initialPosition.setOffsets(blockOffset, 0L);
                        lastWrite = CoreIO.write(initialPosition, bytes, 0, bytes.length);
                    } else { // need to create a new node
                        // create block with fake index offset (set below) assume quicker to write header twice than scan entire index twice
                        CASAFileBlock block = new CASAFileBlock(0, bytes);
                        // write block
                        lastWrite = CoreIO.appendBlock(block, initialPosition.getFile());
                        blockOffset = lastWrite.getBlockOffset();
                        initialPosition.setOffsets(blockOffset, 0L);
                    }

                    // add entry to index
                    long indexOffset = initialPosition.getFile().getIndex().addEntry(node, blockOffset);

                    // set index offset in node blocks
                    CoreIO.setIndexOffset(initialPosition, indexOffset);
                }
            } catch(IOException ioe){
                // log errors, as not all exception propagate up through the writers
                System.err.println("CASAOutputStream.flush() - exception " + ioe.getMessage());
                validStream = false;
                throw ioe;
            }

            // done flush, so reset buffer index
            bufferIndex = 0;
        }
    }

    /** Close the stream.  This will perform a final flush of the stream before closing. */
    public void close() throws IOException {
        if(validStream){
            flush();              // ensure stream is flushed
            validStream = false;  // invalidate stream
        }
	super.close();
    }

}
