package casa.io;

import java.io.IOException;

/**
 * A representation of the free blocks list within a CASAFile.  This class is of limited utility outside of the casa.io.package. <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public class CASAFileFreeList {

    /** The reserved index name for the free list */
    public static final String FREE_LIST_ENTRY_NAME = "_*free block list";

    /**
		 */
    private CASAFile file;

    protected CASAFileFreeList(CASAFile aFile){ file = aFile;}

    /** simple method to determine if the free list is empty or not */
    public boolean hasBlocks() throws CASAIOException, IOException {
	// the free list has a block if it is not empty
	return (CoreIO.getNodeLength(file, FREE_LIST_ENTRY_NAME) > 0);
    }

    /**
     * Remove a block from the free list and returns the file offset to it.
     * @return The offset to the free block or 0 if no free blocks. (0 is an invalid block offset)
     */
    protected long getBlock() throws IOException, CASAIOException {

	long freeBlock = 0L;

	CASAFileIndexEntry entry = file.getIndex().getEntry(FREE_LIST_ENTRY_NAME);

	if(entry == null) return freeBlock; // no free blocks available

	long offset = entry.getOffset();
	long prevOffset = offset;

	// find last block of freeList data
	CASAFileBlockHeader header = CoreIO.readBlockHeader(file, offset);
	while((header.getFreeDataSize() < 4L) && (header.getNextOffset() > 0L)){
	    prevOffset = offset;
	    offset = header.getNextOffset();
	    header = CoreIO.readBlockHeader(file, offset);
	}

	// offset is offset to last block of free block list
	// header is header of that block (might be first and only block)

	if(header.getDataOffset() > 0L){
	    // we have at least one free block in the data, so return last added block as free block
	    CASAFileBlock block = CoreIO.readBlock(file, offset);
	    int dataOffset = (int)block.header.getDataOffset() - 4; // beginning of free block entry
	    freeBlock = CASAFileUtilities.toLong(block.data, dataOffset);
	    // clear block entry, adjust freeDataSize and write block
	    for(int i = dataOffset; i < dataOffset + 4; i++) block.data[i] = (byte)0;
	    block.header.setFreeDataSize(block.header.getFreeDataSize() + 4L);
	    CoreIO.writeBlock(block, new FilePosition(file, offset, 0L));
	} else if(prevOffset != offset){
	    // special case of current free list block being empty but has a previous block
	    // that means the current block is actually a free block, so just return it.
	    // need only to change the next offset pointer in the previous block to 0L

	    freeBlock = offset;
	    header.reset(); // reset header (as it's free now)

	    CASAFileBlockHeader prevHeader = CoreIO.readBlockHeader(file, prevOffset);
	    prevHeader.setNextOffset(0L); // clear previous blocks next pointer

	    // write block headers
	    FilePosition pos = new FilePosition(file, prevOffset, 0L);
	    CoreIO.writeBlockHeader(prevHeader, pos);
	    pos.setBlockOffset(offset);
	    CoreIO.writeBlockHeader(header, pos);
	} else freeBlock = 0L; // no free blocks

	return freeBlock;
    }

    /**
     * Add a block to the free list.  The blocks' headers are reset.
     */
    protected void addBlock(long freeBlock) throws CASAIOException, IOException {

	if(freeBlock <= 0) throw new CASAIOException("CASAFileFreeList.addBlock() - invalid free block offset");

	byte[] offsetBytes = CASAFileUtilities.toBytes(freeBlock);

	CASAFileIndexEntry entry = file.getIndex().getEntry(FREE_LIST_ENTRY_NAME);

	if(entry == null){
	    // add a completely new block and add entry into index
	    CASAFileBlock block = new CASAFileBlock(0L, offsetBytes);
	    FilePosition pos = CoreIO.appendBlock(block, file);
	    long indexOffset = file.getIndex().addEntry(FREE_LIST_ENTRY_NAME, pos.getBlockOffset());
	    block.header.setIndexOffset(indexOffset);
	    CoreIO.writeBlockHeader(block.header, pos);
	} else {
	    // get a free block
	    long offset = entry.getOffset();
	    // find last block of freeList data
	    CASAFileBlockHeader header = CoreIO.readBlockHeader(file, offset);

	    while((header.getFreeDataSize() < 4L) && (header.getNextOffset() > 0L)){
		offset = header.getNextOffset();
		header = CoreIO.readBlockHeader(file, offset);
	    }

	    if(header.getFreeDataSize() >= 4L){
		// we have room to store free block
		CASAFileBlock block = CoreIO.readBlock(file, offset);
		int dataOffset = (int)block.header.getDataOffset();
		// append block entry, adjust freeDataSize and write block
		for(int i = 0; i < 4; i++) block.data[dataOffset + i] = offsetBytes[i];
		block.header.setFreeDataSize(block.header.getFreeDataSize() - 4L);
		FilePosition pos = new FilePosition(file, offset, 0L);
		CoreIO.writeBlock(block, pos);
	    } else {
		// need to start writing in a free block, perhaps the one just being freed?! 8-)
		header.setNextOffset(freeBlock);

		CASAFileBlockHeader nextHeader = CoreIO.readBlockHeader(file, freeBlock);
		nextHeader.setFreeDataSize(header.getDataSize());
		nextHeader.setNextOffset(0L);
		nextHeader.setIndexOffset(header.getIndexOffset());

		// write block headers
		FilePosition pos = new FilePosition(file, freeBlock, 0L);
		CoreIO.writeBlockHeader(nextHeader, pos);
		pos.setBlockOffset(offset);
		CoreIO.writeBlockHeader(header, pos);
	    }
	}
    }

    /**
     * Get a list of the file offsets to any free blocks.
     * @return null if file does not have a free block list, otherwise a Vector of Long offsets
     */
    public java.util.Vector getFreeBlocks() throws IOException, CASAIOException {
	java.util.Vector blocks = null;

	CASAFileIndexEntry entry = file.getIndex().getEntry(FREE_LIST_ENTRY_NAME);

	if(entry != null){
	    blocks = new java.util.Vector();
	    long blockOffset = entry.getOffset();
	    int index, dataOffset;
	    CASAFileBlock block;
	    do {
		block = CoreIO.readBlock(file, blockOffset);
		dataOffset = (int)block.header.getDataOffset();
		for(index = 0; index < dataOffset; index += 4){
		    blocks.add(new Long(CASAFileUtilities.toLong(block.data, index)));
		}
		blockOffset = block.header.getNextOffset();
	    } while(blockOffset > 0L);
	}

	return blocks;
    }

}
