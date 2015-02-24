package casa.io;

/*
 * a simple class to hold a CASAFile reference, a block offset, and a block data offset
 * to represent a specific position within a CASA file
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
class FilePosition {

    /**
		 */
    private CASAFile file = null;
    private long blockOffset = -1L;
    private long dataOffset = 0L;

    protected FilePosition(CASAFile file){ this(file, -1L, 0L);}

    protected FilePosition(CASAFile file, long blockOffset){ this(file, blockOffset, 0L);}

    protected FilePosition(CASAFile file, long blockOffset, long dataOffset){
        this.file = file;
        this.blockOffset = blockOffset;
        this.dataOffset = dataOffset;
    }

    protected FilePosition(FilePosition pos){
        this.file = pos.getFile();
        this.blockOffset = pos.getBlockOffset();
        this.dataOffset = pos.getDataOffset();
    }

    /**
		 * @return
		 */
    public CASAFile getFile(){ return file;}
    /**
		 * @return
		 */
    public long getBlockOffset(){ return blockOffset;}
    /**
		 * @return
		 */
    public long getDataOffset(){ return dataOffset;}

    /**
		 * @param value
		 */
    protected void setBlockOffset(long value){ blockOffset = value;}
    /**
		 * @param value
		 */
    protected void setDataOffset(long value){ dataOffset = value;}
    protected void setOffsets(long blockOffset, long dataOffset){
        this.blockOffset = blockOffset;
        this.dataOffset = dataOffset;
    }

    public String toString(){
        StringBuffer buff = new StringBuffer();
        buff.append(blockOffset);
        buff.append(":");
        buff.append(dataOffset);
        return buff.toString();
    }

}
