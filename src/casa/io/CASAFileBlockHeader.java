package casa.io;


/*
 * a CASA file block header is currently composed of the following:
 * [2 bytes magicNumber] - unsigned == 0xCA2A
 * [4 bytes index offset] - positive offset from beginning of index data segment
 * [4 bytes block size (bytes)] - unsigned total bytes for block, including header
 * [4 bytes free (bytes)] - unsigned total bytes free in block
 * [4 bytes next block offset] - unsigned file offset to next block (== 0 if none)
 * [1 byte flags] - various flags (free, child, etc)
 * [5 bytes expansion] - reserved for future use
 * total header size = 24 bytes
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
 */

/**
 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
class CASAFileBlockHeader {

    public static final long MAGIC_NUMBER = 0xCA2AL;
    public static final byte[] MAGIC_NUMBER_BYTES = {-54, 42}; // == 0xCA2A;
    public static final int HEADER_SIZE = 24;                  // header size (in bytes)

    public static final short FREE       = 0x00000001;
    public static final short CHILD      = 0x00000002;
    public static final short FREE_MASK  = 0xFFFFFFFE;
    public static final short CHILD_MASK = 0xFFFFFFFD;

    private long indexOffset;   //  4 byte index data offset (use lower 4 bytes as unsigned value)
    private long blockSize;     //  4 byte size (use lower 4 bytes as unsigned value)
    private long freeSize;      //  4 byte free (use lower 4 bytes as unsigned value)
    private long nextOffset;    //  4 byte next (use lower 4 bytes as unsigned value)
    private boolean freeFlag;   // is block free?
    private boolean childFlag;  // is block a child?

    protected CASAFileBlockHeader(){
        indexOffset = 0;
        blockSize = CASAFileBlock.DEFAULT_BLOCK_SIZE;
        freeSize = blockSize - HEADER_SIZE;
        nextOffset = 0L;
        freeFlag = true;
        childFlag = false;
    }

    protected CASAFileBlockHeader(long indexOffset, long blockSize) throws CASAIOException{
        this(indexOffset, blockSize, blockSize - HEADER_SIZE, 0L);
    }

    protected CASAFileBlockHeader(long indexOffset, long blockSize,  long freeSize, long nextOffset) throws CASAIOException{
        this.indexOffset = indexOffset;
        this.blockSize = blockSize;
        this.freeSize = freeSize;
        this.nextOffset = nextOffset;

        // check for positive values
        if(indexOffset < 0)
            throw new CASAIOException("CASAFileBlockHeader(long, long, long, long) - invalid argument (indexOffset < 0)");
        if(blockSize < 0)
            throw new CASAIOException("CASAFileBlockHeader(long, long, long, long) - invalid argument (blockSize < 0)");
        if(freeSize < 0)
            throw new CASAIOException("CASAFileBlockHeader(long, long, long, long) - invalid argument (freeSize < 0)");
        if(nextOffset < 0)
            throw new CASAIOException("CASAFileBlockHeader(long, long, long, long) - invalid argument (nextOffset < 0)");

        // other checks
        if(blockSize < HEADER_SIZE)               // block must be large enough to hold header
            throw new CASAIOException("CASAFileBlockHeader(long, long, long, long) - invalid argument (blockSize < HEADER_SIZE)");
        if(freeSize > (blockSize - HEADER_SIZE))  // free size cannot be larger than data size
            throw new CASAIOException("CASAFileBlockHeader(long, long, long, long) - invalid argument (freeSize > data size)");

        // set default flag values
        freeFlag = false;
        childFlag = false;
    }

    /*
     * Construct a block header from a block of raw bytes, interpreted as:
     * [2 bytes magicNumber] - unsigned == 0xCA2A
     * [4 bytes index offset] - positive offset from beginning of index data segment
     * [4 bytes block size (bytes)] - unsigned total bytes for block, including header
     * [4 bytes free (bytes)] - unsigned total bytes free in block
     * [4 bytes next block offset] - unsigned file offset to next block (== 0 if none)
     * [1 byte flags] - various flags (free, child, etc)
     * [5 bytes expansion] - reserved for future use
     *
     * throws an exception if the magic number is incorrect or if the byte array is not equal
     * to HEADER_SIZE
     */

    protected CASAFileBlockHeader(byte[] bytes) throws CASAIOException {

        if(bytes.length != HEADER_SIZE)
            throw new CASAIOException("CASAFileBlockHeader(byte[]) - invalid byte array size");

        if((bytes[0] != MAGIC_NUMBER_BYTES[0]) || (bytes[1] != MAGIC_NUMBER_BYTES[1]))
            throw new CASAIOException("CASAFileBlockHeader(byte[]) - invalid magic number");

        // we have the correct header size and the magic number matches, so assume correct header data
        int index = 2;
        indexOffset = CASAFileUtilities.toLong(bytes, index);
        index += 4;
        blockSize = CASAFileUtilities.toLong(bytes, index);
        index += 4;
        freeSize = CASAFileUtilities.toLong(bytes, index);
        index += 4;
        nextOffset = CASAFileUtilities.toLong(bytes, index);
        index += 4;
        freeFlag = ((FREE & (short)bytes[index]) > 0);
        childFlag = ((CHILD & (short)bytes[index]) > 0);
    }

    public long length(){ return HEADER_SIZE;}
    /**
		 * @return
		 */
    public long getIndexOffset(){ return indexOffset;}
    /**
		 * @return
		 */
    public long getBlockSize(){ return blockSize;}
    public long getDataSize(){ return blockSize - HEADER_SIZE;}
    public long getFreeDataSize(){ return freeSize;}
    /**
		 * @return
		 */
    public long getNextOffset(){ return nextOffset;}
    public long getDataOffset(){ return getDataSize() - getFreeDataSize();}
    public boolean hasNext(){ return (nextOffset > 0);}
    public boolean isFree(){ return freeFlag;}
    public boolean isChild(){ return childFlag;}

    /*
     * return this header as an array of bytes
     * the array.length is guaranteed == CASAFileBlockHeader.HEADER_SIZE
     */
    public byte[] getBytes(){

        int i, index;
        byte[] convBytes;
        byte[] bytes;

        // create header bytes
        bytes = new byte[HEADER_SIZE];

        // add magic number
        index = 0;
        for(i = 0; i < MAGIC_NUMBER_BYTES.length; i++, index++) bytes[index] = MAGIC_NUMBER_BYTES[index];

        // convert and add indexOffset
        convBytes = CASAFileUtilities.toBytes(indexOffset);
        for(i = 0; i < convBytes.length; i++, index++) bytes[index] = convBytes[i];

        // convert and add blockSize
        convBytes = CASAFileUtilities.toBytes(blockSize);
        for(i = 0; i < convBytes.length; i++, index++) bytes[index] = convBytes[i];

        // convert and add freeSize
        convBytes = CASAFileUtilities.toBytes(freeSize);
        for(i = 0; i < convBytes.length; i++, index++) bytes[index] = convBytes[i];

        // convert and add nextOffset
        convBytes = CASAFileUtilities.toBytes(nextOffset);
        for(i = 0; i < convBytes.length; i++, index++) bytes[index] = convBytes[i];

        // clear last bytes (including flag byte)
        java.util.Arrays.fill(bytes, index, HEADER_SIZE, (byte)0);

        // set flag values
        if(freeFlag) bytes[index] |= FREE;
        if(childFlag) bytes[index] |= CHILD;

        return bytes;
    }

    /**
		 * @param value
		 * @throws CASAIOException
		 */
    protected void setIndexOffset(long value) throws CASAIOException {
        if(value < 0)
            throw new CASAIOException("CASAFileBlockHeader.setIndexOffset(long) - invalid value ( < 0)");
        indexOffset = value;
    }

    protected void setFreeDataSize(long value) throws CASAIOException {
        if((value < 0) || (value > getDataSize()))
            throw new CASAIOException("CASAFileBlockHeader.setFreeDataSize(long) - invalid free value ( < 0 || > block data size)");
        freeSize = value;
    }

    /**
		 * @param value
		 * @throws CASAIOException
		 */
    protected void setNextOffset(long value) throws CASAIOException {
        if(value < 0)
            throw new CASAIOException("CASAFileBlockHeader.setNextOffset(long) - invalid value ( < 0)");
        nextOffset = value;
    }

    /**
		 * @param value
		 */
    protected void setFreeFlag(boolean value){ freeFlag = value;}
    /**
		 * @param value
		 */
    protected void setChildFlag(boolean value){ childFlag = value;}

    /*
     * convenience method for resetting values in header
     */
    protected void reset(){
        indexOffset = 0L;
        freeSize = blockSize - HEADER_SIZE;
        nextOffset = 0L;
        freeFlag = true;
        childFlag = false;
    }
}
