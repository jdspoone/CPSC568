package casa.io;

import java.io.IOException;

/**
 * A representation of the index node within a CASA file. The index is stored as a node itself, and can be treated as such.  That is, it can be publicly read through the CASAInputStream.  However, it cannot be written to publicly. In any case, this class provides more convenient methods for reading the entries within the index for a file. <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public class CASAFileIndex {

    /** The reserved name for the index within the index */
    public static final String INDEX_ENTRY_NAME = "_*index";

    /** The encoding used for the index entry names. */
    public static final String ENTRY_ENCODING = "UTF-16BE";

    protected static final long DEFAULT_INDEX_BLOCK_SIZE = 2048L;

    /** Determine if a node name is actually a reserved name */
    public static boolean isReservedName(String name){
	return (CASAFileIndex.INDEX_ENTRY_NAME.equals(name) ||
		CASAFileFreeList.FREE_LIST_ENTRY_NAME.equals(name));
    }

    /**
		 */
    private FilePosition filePos = null;

    public CASAFileIndex(CASAFile file){
	// index is always first block
	filePos = new FilePosition(file, CASAFileHeader.HEADER_SIZE, 0L);
    }

    /**
     * Get the given nodes index entry.
     * @return The nodes' index entry if name exists in the file, otherwise null.
     */
    public CASAFileIndexEntry getEntry(String name) throws IOException, CASAIOException {
	CASAFileIndexEntry entry = null;
	CASAInputStream in = new CASAInputStream(filePos, INDEX_ENTRY_NAME);

	do {
	    entry = nextEntry(in);
	} while((entry != null) && (!entry.getName().equals(name)));

	return entry;
    }

    /**
     * Get a list of all entries within the file.
     * @return A Vector of CASAFileIndexEntry objects.
     */
    public java.util.Vector getEntries() throws IOException, CASAIOException {
	java.util.Vector entries = new java.util.Vector();
	CASAFileIndexEntry entry = null;
	CASAInputStream in = new CASAInputStream(filePos, INDEX_ENTRY_NAME);

	do {
	    entry = nextEntry(in);
	    if(entry != null) entries.add(entry);
	} while(entry != null);

	return entries;
    }

    private CASAFileIndexEntry nextEntry(CASAInputStream in) throws IOException, CASAIOException {
	byte[] offsetBytes = new byte[4];
	byte[] lengthBytes = new byte[2];
	byte[] nameBytes;

	CASAFileIndexEntry entry = null;

	long offset;
	int length;
	String name;

	int bytesRead;

	while(entry == null){
	    bytesRead = in.read(offsetBytes);
	    if(bytesRead != 4) break; //EOF
	    offset = CASAFileUtilities.toLong(offsetBytes);

	    bytesRead = in.read(lengthBytes);
	    if(bytesRead != 2) break; //EOF
	    length = CASAFileUtilities.toInt(lengthBytes);

	    nameBytes = new byte[length];
	    bytesRead = in.read(nameBytes);
	    if(bytesRead != length) break; //EOF
	    name = new String(nameBytes, ENTRY_ENCODING);

	    // read in an entries data
	    // if offset > 0 then we have a valid entry, otherwise continue reading
	    if(offset > 0) entry = new CASAFileIndexEntry(name, offset);
	}

	return entry;
    }

    /** return the length (in bytes) of the index */
    public long getLength() throws CASAIOException, IOException {
	return CoreIO.getNodeLength(filePos);
    }

    /**
     * returns the index offset to this entry (exceptions if unsuccessful)
     */
    protected long addEntry(String name, long offset) throws CASAIOException, IOException {
	long indexOffset = getLength();
	CASAFileIndexEntry entry = new CASAFileIndexEntry(name, offset);
	CASAOutputStream out = new CASAOutputStream(filePos, CASAFile.MODE_APPEND);
	out.write(entry.getBytes());
	out.flush();
	return indexOffset;
    }

    /**
     * returns the entry removed if entry existed in index, otherwise null
     */
    protected CASAFileIndexEntry removeEntry(String name) throws CASAIOException, IOException {
	if(isReservedName(name)) throw new CASAIOException("node '" + name + "' is a reserved node, deletion is not allowed");

	CASAFileIndexEntry entry = getEntry(name);

	if(entry != null){
	    // get index offset of entry
	    FilePosition pos = new FilePosition(filePos.getFile(), entry.getOffset(), 0L);
	    CASAFileBlockHeader header = CoreIO.readBlockHeader(pos);
	    long indexOffset = header.getIndexOffset();

	    // get block containing index entry
	    //System.out.println("node position (index offset): " + indexOffset);
	    pos = CoreIO.getFilePosition(filePos.getFile(), new NodePosition(INDEX_ENTRY_NAME, indexOffset));
	    //System.out.println("file position data offset: " + pos.getDataOffset());

	    CASAFileBlock block = CoreIO.readBlock(pos);
	    // set entries block offset to 0 and write block back to file
	    int dataOffset = (int)pos.getDataOffset();
	    for(int i = dataOffset; i < dataOffset + 4; i++) block.data[i] = (byte)0;
	    CoreIO.writeBlock(block, pos);
	}

	return entry;
    }

    public int getNumberOfEntries(){
      int numberOfEntries = 0;
      CASAFileIndexEntry entry = null;

      try {
        CASAInputStream in = new CASAInputStream (filePos, INDEX_ENTRY_NAME);

        do {
          entry = nextEntry (in);
          if (entry != null) {
            numberOfEntries++;
          }
        } while (entry != null);
      } catch (IOException ex) {
        return 0;
      }

      return numberOfEntries;
    }

    /**
     * Utility method to create a default block containing an initial index block.
     * The index block is itself added as an entry.
     * Note, however, that it is assumed that the first block in a CASA file
     * will be the index block.  As a result, the block offset for the index
     * is assumed to be the file header size.
     */

    protected static CASAFileBlock createDefaultIndexBlock() throws IOException, CASAIOException {
        // create block and add the index as an entry itself
        CASAFileBlock block = new CASAFileBlock(new CASAFileBlockHeader(0L, DEFAULT_INDEX_BLOCK_SIZE));

	CASAFileIndexEntry entry = new CASAFileIndexEntry(INDEX_ENTRY_NAME, CASAFileHeader.HEADER_SIZE);
	byte[] bytes = entry.getBytes();

        for(int i = 0; i < bytes.length; i++) block.data[i] = bytes[i];

        // adjust header free size
        block.getHeader().setFreeDataSize(block.getHeader().getFreeDataSize() - bytes.length);

        return block;
    }

}
