package casa.io;

/**
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
class CASAFileBlock {

    public static final long DEFAULT_BLOCK_SIZE = 1024L;

    /**
		 */
    protected CASAFileBlockHeader header;
    protected byte[] data;

    /*
     * public constructors
     */

    /**
     * construct a file block that contains a data area of size >= dataSize
     */
    public CASAFileBlock(int dataSize) throws CASAIOException {
        int blockSize = (int)DEFAULT_BLOCK_SIZE;
        int blockSizeRequired = CASAFileBlockHeader.HEADER_SIZE + ((dataSize < 10) ?  10 : dataSize);
        while(blockSize < blockSizeRequired) blockSize += DEFAULT_BLOCK_SIZE;
        header = new CASAFileBlockHeader(0L, blockSize);
        data = new byte[(int)header.getDataSize()];
        java.util.Arrays.fill(data, 0, data.length, (byte)0);
    }

    public CASAFileBlock(byte[] dataBytes) throws CASAIOException { this(-1L, dataBytes, 0, dataBytes.length);}

    /*
     * protected constructors
     */

    protected CASAFileBlock(){ this(new CASAFileBlockHeader());}

    protected CASAFileBlock(CASAFileBlockHeader aHeader){
        header = aHeader;
        data = new byte[(int)header.getDataSize()];
        java.util.Arrays.fill(data, 0, (int)header.getDataSize(), (byte)0);
    }

    protected CASAFileBlock(CASAFileBlockHeader aHeader, int[] theData){
        header = aHeader;
        // block size is set by header in this case
        data = new byte[(int)header.getDataSize()];

        // copy as much data as possible
        int i;
        for(i = 0; (i < data.length) && (i < theData.length); i++){
            data[i] = (byte)(CASAFileUtilities.INT_BYTE_MASK & theData[i]);
        }

        // clear remaining block data
        if(i < data.length) java.util.Arrays.fill(data, i, data.length, (byte)0);
    }

    protected CASAFileBlock(long indexOffset, byte[] dataBytes) throws CASAIOException {
        this(indexOffset, dataBytes, 0, dataBytes.length);
    }

    protected CASAFileBlock(long indexOffset, byte[] dataBytes, int start, int dataCount) throws CASAIOException {
        if((start < 0) || (start >= dataBytes.length))
            throw new CASAIOException("CASAFileBlock(long, byte[], int, int) - invalid array indexes");

        long blockSize = DEFAULT_BLOCK_SIZE;
        int end = ((start + dataCount) > dataBytes.length) ? dataBytes.length : start + dataCount;

        long dataUsed = CASAFileBlockHeader.HEADER_SIZE + (long)(end - start);

        // get required block size (in multiples of defualt size)
        while(blockSize < dataUsed) blockSize += DEFAULT_BLOCK_SIZE;

        // create header
        header = new CASAFileBlockHeader(indexOffset, blockSize, blockSize - dataUsed, 0L);

        // create data array
        data = new byte[(int)header.getDataSize()];

        // copy bytes
        int index = 0;
        for(int i = start; i < end; i++, index++) data[index] = dataBytes[i];

        // clear remaining bytes
        if(index < data.length) java.util.Arrays.fill(data, index, data.length, (byte)0);
    }

    /*
     * public methods
     */
    /**
		 * @return
		 */
    public CASAFileBlockHeader getHeader(){ return header;}
    public long length(){ return header.length() + data.length;}
    public long dataLength(){ return data.length;}

    /*
     * returns the actual data byte array (not a copy)
     */
    public byte[] getDataBytes(){ return data;}

    /*
     * returns a copy of both header bytes and data bytes
     */
    public byte[] getBytes(){
        byte[] bytes = new byte[(int)header.getBlockSize()];

        // copy header bytes
        byte[] dataBytes = header.getBytes();
        int index;
        for(index = 0; index < dataBytes.length; index++) bytes[index] = dataBytes[index];

        // copy data bytes
        for(int i = 0; i < data.length; i++, index++) bytes[index] = data[i];

        return bytes;
    }
}
