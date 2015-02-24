package casa.io;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * CASAFileIO - a pure Java implementation of various functions used for working with CASAFiles
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

class CoreIO {

    //////////////////////////////////////////////////////////////////////////////////////////
    //
    //  generic write functions
    //
    //////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Writes the specified data to the file for the specified node.  As with the stream methods
     * the data to be written is actually only the eight low-order bits of each integer in the data array.
     * The 24 high-order bits of each int are ignored.
     * If the node does not exist, it will be created with the given data.
     * only the data from data[offset] to data[length - 1] is written.
     * The data is written from the dataOffset parameter.
     * If append == true then the data is appended to the node, otherwise the nodes data will be overwritten
     * by the new data
     */

    protected static FilePosition write(FilePosition pos, byte[] bytes) throws IOException, CASAIOException {
	return write(pos, bytes, 0, bytes.length);
    }

    protected static FilePosition write(FilePosition pos, byte[] bytes, int startIndex, int dataCount) throws IOException, CASAIOException {

	RandomAccessFile file = null;
	FilePosition newPos = null;

	try {
	    // open file for writing and perform write
	    file = new RandomAccessFile(pos.getFile(), "rw");
	    newPos = write(pos, bytes, startIndex, dataCount, file);
	} catch(IOException ioe){
	    throw ioe;
	} finally {
	    if(file != null) file.close();
	}

	return newPos;
    }

    protected static FilePosition write(FilePosition pos, byte[] bytes, int startIndex, int dataCount, RandomAccessFile aFile) throws IOException, CASAIOException {

	// if file offset <= 0 then we have an invalid offset
	if(pos.getBlockOffset() <= 0)
	    throw new CASAIOException("CASAFileWriter.write() - invalid file position ("+pos.getBlockOffset() + " <= 0)");

	// get block header at offset
	CASAFileBlockHeader header = readBlockHeader(pos.getBlockOffset(), aFile);

	// write as much as possible to this block
	long dataSize = header.getDataSize();
	long free = dataSize - pos.getDataOffset(); // ignore header free size
	int bytesToWrite = (free < dataCount) ? (int)free : dataCount;
	long offset;

	if(bytesToWrite > 0){
	    // write bytes to current block
	    offset = pos.getBlockOffset() + CASAFileBlockHeader.HEADER_SIZE + pos.getDataOffset();
	    aFile.seek(offset);
	    aFile.write(bytes, startIndex, bytesToWrite);
	}

	// adjust free size in header
	free -= bytesToWrite;
	header.setFreeDataSize(free);

	// create new file position
	FilePosition newPos;

	if(bytesToWrite >= dataCount){
	    // full write to the current block so just adjust new data offset position
	    newPos = new FilePosition(pos);
	    newPos.setDataOffset(header.getDataOffset());
	} else {
	    // need to continue writing in another block
	    int newStart = startIndex + bytesToWrite;
	    int newCount = dataCount - bytesToWrite;
	    long next = header.getNextOffset();
	    if(next > 0){
		// header already has a next block so simply perform a recursive write
		FilePosition aPos = new FilePosition(pos.getFile(), next, 0L);
		newPos = write(aPos, bytes, newStart, newCount, aFile);
	    } else {
		// header has no next so check free block list
		next = pos.getFile().getFreeList().getBlock();
		if(next > 0){
		    // found a free block, so set header properties and perform a recursive write
		    FilePosition aPos = new FilePosition(pos.getFile(), next, 0L);
		    CASAFileBlockHeader newHeader = readBlockHeader(next, aFile);
		    newHeader.setIndexOffset(header.getIndexOffset());
		    writeBlockHeader(newHeader, next, aFile);
		    // perform recursive write
		    newPos = write(aPos, bytes, newStart, newCount, aFile);
		} else {
		    // no free blocks, so add a new one to end of file
		    CASAFileBlock block = new CASAFileBlock(header.getIndexOffset(), bytes, newStart, newCount);
		    next = appendBlock(block, aFile);
		    newPos = new FilePosition(pos.getFile(), next, block.header.getDataOffset());
		}
		// set next pointer in header
		header.setNextOffset(next);
	    }
	}

	// write modified original header
	aFile.seek(pos.getBlockOffset());
	aFile.write(header.getBytes());

	// all done!
	return newPos;
    }

    /**
     * go through all blocks (including first) and set free data size == total data area
     */
    protected static void clearBlocks(FilePosition pos) throws IOException, CASAIOException {

	RandomAccessFile raFile = null;
	try {
	    raFile = new RandomAccessFile(pos.getFile(), "rw");
	    CASAFileBlockHeader header = null;
	    long offset = pos.getBlockOffset();

	    while(offset > 0){
		header = readBlockHeader(offset, raFile);
		header.setFreeDataSize(header.getDataSize());
		writeBlockHeader(header, offset, raFile);
		offset = header.getNextOffset();
	    }

	} catch(CASAIOException cioe){
	    throw cioe;
	} catch(IOException ioe){
	    throw ioe;
	} finally {
	    if(raFile != null) raFile.close();
	}
    }

    /**
     * convert the supplied node position into a file position
     * returns null if node not found
     */

    protected static FilePosition getFilePosition(CASAFile file, NodePosition nodePos) throws CASAIOException, IOException {
	FilePosition pos = null;
	CASAFileIndexEntry entry = file.getIndex().getEntry(nodePos.getNodeName());

	if(entry != null){
	    // node exists so find nodePos in file as a file position
	    RandomAccessFile aFile = null;
	    try {
		long offset = 0L;
		long dataOffset = -1L;
		long blockOffset = entry.getOffset();
		aFile = new RandomAccessFile(file, "r");
		CASAFileBlockHeader header = readBlockHeader(blockOffset, aFile);

		while(dataOffset < 0L){
		    if(offset + header.getDataSize() >= nodePos.getOffset()){
			// nodePos is in this block
			dataOffset = nodePos.getOffset() - offset;
			// ensure offset is not past end of node
			if(dataOffset > header.getDataOffset()) dataOffset = header.getDataOffset();
		    } else if((header.getDataOffset() < header.getDataSize()) || (header.getNextOffset() <= 0)){
			// reached end of node
			dataOffset = header.getDataOffset();
		    } else {
			// advance to next block
			offset += header.getDataSize();
			blockOffset = header.getNextOffset();
			header = readBlockHeader(blockOffset, aFile);
		    }
		}

		pos = new FilePosition(file, blockOffset, dataOffset);
	    } catch(IOException ioe){
		throw ioe;
	    } finally {
		if(aFile != null) aFile.close();
	    }
	}

	return pos;
    }

    protected static FilePosition getFirstWritePosition(CASAFile file, long offset, int mode) throws CASAIOException, IOException {

	FilePosition pos = new FilePosition(file, offset, 0L);

	switch(mode){
	case CASAFile.MODE_OVERWRITE:
	    // start at beginning of node
	    pos.setDataOffset(0L);
	    // need to ensure that any linked nodes are reset (or freed)
	    clearBlocks(pos);
	    break;
	case CASAFile.MODE_APPEND:
	default:
	    // check header for freeDataOffset and advance to first writable position
	    long next = pos.getBlockOffset();
	    CASAFileBlockHeader header = null;
	    RandomAccessFile raFile = null;
	    try {
		raFile = new RandomAccessFile(pos.getFile(), "r");
		do {
		    header = readBlockHeader(next, raFile);
		    pos.setOffsets(next, header.getDataOffset());
		    next = header.getNextOffset();
		} while((header.getDataOffset() >= header.getDataSize()) && (next > 0));

	    } catch(CASAIOException cioe){
		throw cioe;
	    } catch(IOException ioe){
		throw ioe;
	    } finally {
		if(raFile != null) raFile.close();
	    }

	}

	return pos;
    }

    protected static FilePosition writeBlock(CASAFileBlock block, FilePosition pos) throws IOException {

	RandomAccessFile file = null;
	FilePosition newPos = null;

	try {
	    // open file for writing and write block
	    file = new RandomAccessFile(pos.getFile(), "rw");
	    newPos = writeBlock(block, pos, file);
	} catch(IOException ioe){
	    throw ioe;
	} finally {
	    if(file != null) file.close();
	}

	return newPos;
    }

    protected static FilePosition writeBlock(CASAFileBlock block, FilePosition pos, RandomAccessFile aFile) throws IOException {
	FilePosition newPos = new FilePosition(pos);

	// if block offset > 0 assume it is correct otherwise append block to end of file
	if(newPos.getBlockOffset() <= 0) newPos.setBlockOffset(aFile.length());

	aFile.seek(newPos.getBlockOffset());
	aFile.write(block.getBytes());

	newPos.setDataOffset(block.getHeader().getDataOffset());

	return newPos;
    }

    protected static void writeBlockHeader(CASAFileBlockHeader header, FilePosition pos) throws IOException {
	RandomAccessFile file = null;
	try {
	    // open file for writing and write block
	    file = new RandomAccessFile(pos.getFile(), "rw");
	    writeBlockHeader(header, pos.getBlockOffset(), file);
	} catch(IOException ioe){
	    throw ioe;
	} finally {
	    if(file != null) file.close();
	}
    }

    protected static void writeBlockHeader(CASAFileBlockHeader header, long offset, RandomAccessFile file) throws IOException {
	// seek to offset and write header data
	file.seek(offset);
	file.write(header.getBytes());
    }

    protected static FilePosition appendBlock(CASAFileBlock block, CASAFile file) throws IOException, CASAIOException {
	RandomAccessFile raFile = null;
	long offset = 0L;

	try {
	    raFile = new RandomAccessFile(file, "rw");
	    offset = appendBlock(block, raFile);
	} catch(IOException ioe){
	    throw ioe;
	} finally {
	    if(raFile != null) raFile.close();
	}

	FilePosition pos = new FilePosition(file, offset, block.header.getDataOffset());

	return pos;
    }

    protected static long appendBlock(CASAFileBlock block, RandomAccessFile file) throws IOException {
 	long offset = file.length();

	file.seek(offset);
	file.write(block.getBytes());

	return offset;
    }


    //////////////////////////////////////////////////////////////////////////////////////////
    //
    //  generic read functions
    //
    //////////////////////////////////////////////////////////////////////////////////////////

    protected static CASAFileHeader readFileHeader(CASAFile file) throws IOException, CASAIOException {

	RandomAccessFile raFile = null;
	CASAFileHeader header = null;

	try {
	    raFile = new RandomAccessFile(file, "r");
	    header = readFileHeader(raFile);
	} catch(IOException ioe){
	    throw ioe;
	} finally {
	    if(raFile != null) raFile.close();
	}

	return header;
    }

    protected static CASAFileHeader readFileHeader(RandomAccessFile file) throws IOException, CASAIOException {

	byte[] headerBytes = new byte[CASAFileHeader.HEADER_SIZE];

	// read data
	file.seek(0L);
	file.readFully(headerBytes);

	// create and return header
	CASAFileHeader header = new CASAFileHeader(headerBytes);

	return header;
    }

    protected static CASAFileBlockHeader readBlockHeader(CASAFile file, long offset) throws IOException, CASAIOException {
	return readBlockHeader(new FilePosition(file, offset, 0L));
    }

    protected static CASAFileBlockHeader readBlockHeader(FilePosition pos) throws IOException, CASAIOException {
	// open file for reading
	RandomAccessFile aFile = new RandomAccessFile(pos.getFile(), "r");
	// read block header
	CASAFileBlockHeader header = readBlockHeader(pos.getBlockOffset(), aFile);
	// close file
	aFile.close();

	return header;
    }

    protected static CASAFileBlockHeader readBlockHeader(FilePosition pos, RandomAccessFile aFile) throws IOException, CASAIOException {
	return readBlockHeader(pos.getBlockOffset(), aFile);
    }

    protected static CASAFileBlockHeader readBlockHeader(long offset, RandomAccessFile aFile) throws IOException, CASAIOException {
	byte[] headerBytes = new byte[CASAFileBlockHeader.HEADER_SIZE];
	aFile.seek(offset);
	aFile.readFully(headerBytes);
	CASAFileBlockHeader header = new CASAFileBlockHeader(headerBytes);
	return header;
    }

    protected static CASAFileBlock readBlock(CASAFile file, long offset) throws IOException, CASAIOException {
	return readBlock(new FilePosition(file, offset, 0L));
    }

    protected static CASAFileBlock readBlock(FilePosition pos) throws IOException, CASAIOException {
	RandomAccessFile file = null;
	CASAFileBlock block = null;
	try {
	    file = new RandomAccessFile(pos.getFile(), "r");
	    block = readBlock(pos, file);
	} catch(IOException ioe){
	    throw ioe;
	} finally {
	    if(file != null) file.close();
	}

	return block;
    }

    protected static CASAFileBlock readBlock(FilePosition pos, RandomAccessFile aFile) throws IOException, CASAIOException {

	// get header
	CASAFileBlockHeader header = readBlockHeader(pos, aFile);
	CASAFileBlock block = new CASAFileBlock(header);

	// seek to offset (not really neccessary as filepointer ahould be at end of header...) and read data
	aFile.seek(pos.getBlockOffset() + CASAFileBlockHeader.HEADER_SIZE);
	aFile.readFully(block.data);

	return block;
    }

    protected static int read(FilePosition pos, byte[] bytes, int start, int byteCount) throws IOException, CASAIOException {
	RandomAccessFile file = null;
	int bytesRead = 0;
	try {
	    file = new RandomAccessFile(pos.getFile(), "r");
	    bytesRead = read(pos, bytes, start, byteCount, file);
	} catch(IOException ioe){
	    throw ioe;
	} finally {
	    if(file != null) file.close();
	}

	return bytesRead;
    }

    /**
     * read an arbitrary number of bytes from file
     */
    protected static int read(FilePosition pos, byte[] bytes, int start, int byteCount, RandomAccessFile file) throws IOException, CASAIOException {

	int bytesRead = 0;
	int toRead = 0;
	CASAFileBlockHeader header = CoreIO.readBlockHeader(pos.getBlockOffset(), file);
	int available = (int)(header.getDataOffset() - pos.getDataOffset());
	int byteIndex = start;

	// quick check of available as it might initially be <= 0 so need to automatically advance to next block, if any
	if(available <= 0){
	    if((header.getDataOffset() < header.getDataSize()) || (header.getNextOffset() <= 0)){
		// reached end of node
		available = 0;
		pos.setDataOffset(header.getDataOffset());
	    } else {
		// continue reading from next block
		pos.setDataOffset(0L);
		pos.setBlockOffset(header.getNextOffset());
		header = CoreIO.readBlockHeader(pos.getBlockOffset(), file);
		available = (int)header.getDataOffset(); // whole of blocks data is available for read
	    }
	}

	while((bytesRead < byteCount) && (available > 0)){

	    // read from current block - get number of bytes to actually read (minimum of all available or  bytes needed
	    toRead = java.lang.Math.min((byteCount - bytesRead), available);
	    file.seek(pos.getBlockOffset() + CASAFileBlockHeader.HEADER_SIZE + pos.getDataOffset());
	    file.readFully(bytes, byteIndex, toRead);
	    bytesRead += toRead;
	    byteIndex += toRead;

	    if(bytesRead < byteCount){ 	// need more to read
		if((header.getDataOffset() < header.getDataSize()) || (header.getNextOffset() <= 0)){
		    // reached end of node
		    available = 0;
		    pos.setDataOffset(header.getDataOffset());
		} else {
		    // continue reading from next block
		    pos.setDataOffset(0L);
		    pos.setBlockOffset(header.getNextOffset());
		    header = CoreIO.readBlockHeader(pos.getBlockOffset(), file);
		    available = (int)header.getDataOffset(); // whole of block is available for read
		}
	    } else { // done reading so just adjust positions data offset
		pos.setDataOffset(pos.getDataOffset() + toRead);
	    }
	}

	return bytesRead;
    }

    protected static long getNodeLength(CASAFile file, String nodeName) throws CASAIOException, IOException {
	if(!file.exists()) throw new CASAIOException("CASAFileWriter.getNodeLength() - must have an existing file");
	if(!file.isCASAFile()) throw new CASAIOException("CASAFileWriter.getNodeLength() - file is not a CASA file");
	// find node in index
	CASAFileIndex fileIndex = new CASAFileIndex(file);
	CASAFileIndexEntry entry = fileIndex.getEntry(nodeName);
	long length = (entry == null) ? 0L : getNodeLength(new FilePosition(file, entry.getOffset(), 0L));
	return length;
    }

    protected static long getNodeLength(FilePosition pos) throws CASAIOException, IOException {

	long length = 0;
	RandomAccessFile file = null;

	try {
	    long dataUsed = 0;
	    long next = pos.getBlockOffset();
	    CASAFileBlockHeader header = null;

	    file = new RandomAccessFile(pos.getFile(), "r");

	    while(next > 0){
		header = readBlockHeader(next, file);
		dataUsed = header.getDataOffset();
		length += dataUsed;
		// if used all of this blocks data, check the next...
		next = (dataUsed == header.getDataSize()) ? header.getNextOffset() : 0L;
	    }

	} catch(CASAIOException cioe){
	    throw cioe;
	} catch(IOException ioe){
	    throw ioe;
	} finally {
	    if(file != null) file.close();
	}

	return length;
    }

    /**
     * convenience method to set the index offset in one (or more) block headers
     * this method follows all next links and ensures that the index offset is
     * consistent accross all blocks containing a node
     */
    protected static void setIndexOffset(FilePosition pos, long indexOffset) throws CASAIOException, IOException {

	long length = 0;
	RandomAccessFile file = null;

	try {
	    CASAFileBlockHeader header = null;
	    long next = pos.getBlockOffset();
	    file = new RandomAccessFile(pos.getFile(), "rw");

	    do {
		header = readBlockHeader(next, file);
		header.setIndexOffset(indexOffset);
		writeBlockHeader(header, next, file);
		next = header.getNextOffset();
	    } while(next > 0L);

	} catch(CASAIOException cioe){
	    throw cioe;
	} catch(IOException ioe){
	    throw ioe;
	} finally {
	    if(file != null) file.close();
	}
    }

}
