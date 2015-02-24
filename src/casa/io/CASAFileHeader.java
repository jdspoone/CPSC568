package casa.io;


/*
 * a CASA file header is currently composed of the following:
 * [4 bytes magicNumber] - unsigned == 0xCA2ABABE
 * [4 bytes version info] - major:minor:patch:platform
 * total header size = 8 bytes
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
class CASAFileHeader {

    public static final long MAGIC_NUMBER = 0xCA2ABABEL;
    public static final byte[] MAGIC_NUMBER_BYTES = {-54, 42, -70, -66}; // == 0xCA2ABABE;
    public static final int[] CURRENT_VERSION = { 1, 0, 0, 0};           // current version
    public static final int HEADER_SIZE = 8;                             // header size (in bytes)

    public static final int MAJOR = 0; // indexes to read the version array
    public static final int MINOR = 1;
    public static final int PATCH = 2;
    public static final int PLATFORM = 3;

    private int[] version = null;
    private long versionEncoded = 0L;
    private String versionString = null;

    /*
     * Construct a new file header.
     */

    protected CASAFileHeader(){
        version = new int[4];
        for(int i = 0; i< 4; i++){
            version[i] = CURRENT_VERSION[i];
            versionEncoded <<= 8;
            versionEncoded += version[i];
        }
        StringBuffer buff = new StringBuffer();
        buff.append(version[MAJOR]);
        buff.append(".");
        buff.append(version[MINOR]);
        buff.append(".");
        buff.append(version[PATCH]);
        versionString = buff.toString();
    }

    /*
     * Construct a file header from a block of raw bytes, interpreted as:
     * [4 bytes magicNumber] - unsigned == 0xCA2A
     * [4 bytes version data] - coded version information
     *
     * throws an exception if the magic number is incorrect or if the byte array is not equal
     * to HEADER_SIZE
     */

    protected CASAFileHeader(byte[] bytes) throws CASAIOException {

        // check data length
        if(bytes.length != HEADER_SIZE) throw new CASAIOException("CASAFileHeader(byte[]) - invalid byte array size");

        // check magic number
        long magic = CASAFileUtilities.toLong(bytes);
        if(magic != MAGIC_NUMBER) throw new CASAIOException("CASAFileHeader(byte[]) - invalid magic number");

        // we have the correct header size and the magic number matches, so assume correct header data
        int index = 4;
        version = new int[4];
        versionEncoded = 0L;

        for(int i = 0; (i < 4) && (index < bytes.length); i++, index++){
            version[i] = CASAFileUtilities.INT_BYTE_MASK & (int)bytes[index];
            versionEncoded <<= 8;
            versionEncoded += version[i];
        }

        StringBuffer buff = new StringBuffer();
        buff.append(version[MAJOR]);
        buff.append(".");
        buff.append(version[MINOR]);
        buff.append(".");
        buff.append(version[PATCH]);
        versionString = buff.toString();
    }

    /**
		 * @return
		 */
    public String getVersion(){ return versionString;}
    /**
		 * @return
		 */
    public long getVersionEncoded(){ return versionEncoded;}
    public int getMajorVersion(){ return version[MAJOR];}
    public int getMinorVersion(){ return version[MINOR];}
    public int getPatchVersion(){ return version[PATCH];}
    /**
		 * @return
		 */
    public int getPlatform(){ return version[PLATFORM];}
    public int getVersion(int code){
        int val;
        switch(code){
        case MAJOR:
        case MINOR:
        case PATCH:
        case PLATFORM:
            val = version[code];
            break;
        default:
            val = -1;
        }
        return val;
    }

    /*
     * return this header as an array of bytes
     * the array.length is guaranteed == CASAFileHeader.HEADER_SIZE
     */
    public byte[] getBytes(){
        // create header bytes
        byte[] bytes = new byte[HEADER_SIZE];

        int i, index;

        // add magic number
        index = 0;
        for(i = 0; i < MAGIC_NUMBER_BYTES.length; i++, index++) bytes[index] = MAGIC_NUMBER_BYTES[i];

        // add version numbers
        for(i = 0; i < 4; i++, index++)
            bytes[index] = (byte)(CASAFileUtilities.INT_BYTE_MASK & version[i]);

        return bytes;
    }

}
