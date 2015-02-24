package casa.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.Vector;

/**
 * CASAFile is a special type of File used for data I/O within the CASA Framework. Data in a CASAFile is referenced as <i>nodes</i> of data, with the <i>nodes</i> being listed within the files <i>index</i>. See CASAFileIndex for more information about the index. I/O can be performed using two approaches. Arbitrary I/O is supported in CASAFile using the read and write methods. <p> The "shape" of the file is as follows: <pre> +---------------------------+ | file header               | +---------------------------+ | block header "_*index"    | |--------                   | | node record---------------------------+ | node record----------------------+    | | ...                       |      |    | +---------------------------+ &#60;----+    | | block header -------------------------------+ +--------                   |           |     | | ...                       |           |     | +---------------------------+ &#60;---------+     | | block header              |                 |node +--------                   |                 |continuation | ...                       |                 |block +---------------------------+ &#60;---------------+ | block header              | +--------                   | | ...                       | +---------------------------+ </pre> Stream based I/O is supported with CASAInputStream and CASAOutputStream. <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 * @see casa.io.CASAFileIndex
 * @see casa.io.CASAInputStream
 * @see  casa.io.CASAOutputStream
 */

public class CASAFile extends File {

  /** write mode flag used to indicate that data be appended to a node */
  public static final int MODE_APPEND = 0;

  /** write mode flag used to indicate that data be written from the start of a node */
  public static final int MODE_OVERWRITE = 1;

  /** the file name used with the default constructor */
  public static String DEFAULT_FILE_NAME = "data.casa";

  /**
	 */
  private CASAFileHeader header = null;
  private boolean validityChecked = false;

  public CASAFile () {
    super (DEFAULT_FILE_NAME);
  }

  /**
   * Creates a new CASAFile class using the specified pathname.
   *
   * @param pathname Pathname of the file to create or open.
   */
  public CASAFile (String pathname) {
    super (pathname);
  }

  public static String checkAndFixPathname (String pathname) {
    String amp = "&";
    char illegalChars[] = {'&', '\\', '/', ':', '*', '?', '"', '<', '>', '|'};
    String replacementStrings[] = {"&amp;", "&bs;", "&fs;", "&colon;", "&ast;",
                                  "&ques;", "&quot;", "&lt;", "&gt;", "&pipe;"};

    String tempPath;

    int i = 0;
    int nextPosition = pathname.indexOf (illegalChars[i]);

    while (i < illegalChars.length || nextPosition != -1) {
      if (nextPosition == -1) {
        i++;
        if (i < illegalChars.length) {
          nextPosition = pathname.indexOf (illegalChars[i]);
        }
        continue;
      }

      tempPath = pathname.substring (0, nextPosition) + replacementStrings[i];
      pathname = tempPath + pathname.substring (nextPosition + 1);

      nextPosition = pathname.indexOf (illegalChars[i], nextPosition + 1);
    }

    return pathname;
  }
  
  String validityCheckExplanation = null;

  /**
   * A file is a CASA file if the file exists, it is a file, and
   * the files magic number matches the CASAFile magic number.
   */
  public boolean isCASAFile () {

    boolean checkPassed = false; // be pessimistic

    if (validityChecked) {
      checkPassed = (header != null);
    } else if (exists () && isFile ()) {
      try {
        header = CoreIO.readFileHeader (this);
        checkPassed = true; // an exception would have been thrown otherwise
      } catch (Exception e) {
      	validityCheckExplanation ="CASAFile.isCASAFile() - exception reading file header: " + e.getMessage ();
      }
      validityChecked = true;
    } else {
    	header = null;
      checkPassed = false; // not a file or doesn't exist
      if (exists()) validityCheckExplanation = "CASAFile is not a file";
      else validityCheckExplanation = "CASAFile does not exist";
      validityChecked = true;
    }

    return checkPassed;
  }

  /**
       * returns the encoded version number.  This number encoding is compatible with
   * other CASA utilities version number encoding.
   */
  public long getVersionEncoded () {
    return (isCASAFile () ? header.getVersionEncoded () : -1);
  }

  /** return the version number of this file */
  public String getVersion () {
    return (isCASAFile () ? header.getVersion () : "not a CASA file");
  }

  /**
   * Overrides createNewFile() from File.  Ensures that the file created is a
   * genuine CASA file.
   * @return true on successful creation of file, false otherwise
   * @see java.io.File#createNewFile()
   */
  public boolean createNewFile () throws IOException {

    boolean created = false; // be pessimistic

    if (!exists ()) {
      // file doesn't exist so ok to create
      RandomAccessFile aFile = new RandomAccessFile (this, "rw");
      CASAFileHeader header = new CASAFileHeader ();
      CASAFileBlock indexBlock = CASAFileIndex.createDefaultIndexBlock ();

      // write file header and index block
      aFile.write (header.getBytes ());
      aFile.write (indexBlock.getBytes ());
      aFile.close ();

      // created successfully...
      created = true;
      validityChecked = false;
    }

    return created;
  }

  /**
   * Delete the given node from the file.
   * returns true if node deleted, false otherwise (ie if node not in file).
   * @throws casa.io.CASAIOException if nodeName is a reserved name or some
   * other error occurs during node deletion
   * @throws java.io.IOException if some I/O error occurs during node deletion
   */
  public boolean deleteNode (String nodeName) throws CASAIOException,
      IOException {

    boolean deleted = false;

    if (CASAFileIndex.isReservedName (nodeName)) {
      throw new CASAIOException ("Node '" + nodeName +
                                 "' is reserved, it may not be deleted.");
    }

    CASAFileIndex index = getIndex ();
    CASAFileIndexEntry entry = index.removeEntry (nodeName);

    if (entry != null) {
      // node is no longer in index so add all blocks it used to the free block list
      long nextOffset;
      long blockOffset = entry.getOffset ();
      FilePosition pos = new FilePosition (this, blockOffset, 0L);
      CASAFileBlockHeader header = null;
      CASAFileFreeList freeList = getFreeList ();

      do {
        // reset header values and write it
        pos.setBlockOffset (blockOffset);
        header = CoreIO.readBlockHeader (pos);
        nextOffset = header.getNextOffset ();
        header.reset ();
        CoreIO.writeBlockHeader (header, pos);
        freeList.addBlock (blockOffset);
        blockOffset = nextOffset;
      } while (blockOffset > 0L);

      deleted = true;
    }

    return deleted;
  }

  /**
       * Deletes all nodes in the file except those with reserved names (index, etc).
   *
   * @return boolean true if all nodes successfully deleted, false otherwise
   */
  public boolean deleteAllNodes () {
    boolean deleted = false;
    Vector nodeList = null;

    // get the list of nodes
    try {
      nodeList = getIndex ().getEntries ();
    } catch (CASAIOException ex) {
      ex.printStackTrace ();
    } catch (IOException ex) {
      ex.printStackTrace ();
    }

    // delete one node at a time
    for (Enumeration nodeNames = nodeList.elements ();
                                 nodeNames.hasMoreElements (); ) {
      try {
        String deleteMe = nodeNames.nextElement ().toString ();
        if (CASAFileIndex.isReservedName (deleteMe)) {
          continue;
        }
        deleted = deleteNode (deleteMe);
        if (false == deleted) {
          return deleted; // error
        }
      } catch (IOException ex) {
        ex.printStackTrace ();
      }
    }

    return deleted;
  }

  //////////////////////////////////////////////////////////////////////////////
  //  methods for arbitrary length reads and writes to a node at an arbitrary
  //  position (generalized reads and writes)
  //////////////////////////////////////////////////////////////////////////////

  /** convenience method for writing bytes to a node */
  public int write (byte[] bytes, String node) throws CASAIOException,
      IOException {
    NodePosition pos = new NodePosition (node, getNodeLength (node)); // start at end of node data
    return write (bytes, pos, 0, bytes.length);
  }

  /** convenience method for writing bytes to a node */
  public int write (byte[] bytes, String node, int mode, int start, int length) throws
      CASAIOException, IOException {
    NodePosition pos = null;
    // set initial node position for write
    if (mode == MODE_OVERWRITE) {
      pos = new NodePosition (node); // beginning of node data (offset == 0)
    } else {
      pos = new NodePosition (node, getNodeLength (node)); // start at end of node data
    }
    return write (bytes, pos, start, length);
  }

  /** convenience method for writing bytes to a node */
  public int write (byte[] bytes, NodePosition pos) throws CASAIOException,
      IOException {
    return write (bytes, pos, 0, bytes.length);
  }

  /**
       * write bytes to the given node position within the file.  Writes all bytes from
       * bytes[start] to bytes[start + length - 1] inclusive to the file.  If the node
   * does not exist, it is created.  The supplied node position is incremented
       * with the number of bytes written.  However, this value will not be more than
   * the total length of the node data.
   * @return returns the number of bytes written
   * @throws casa.io.CASAIOException if node is a reserved name or some
   * other error occurs during writing
       * @throws java.io.IOException if some underlying I/O error occurs during writing
   */
  public int write (byte[] bytes, NodePosition nodePos, int start, int length) throws
      CASAIOException, IOException {
    // perform various quick fail checks
    if ((bytes == null) || (bytes.length <= 0) || (length <= 0)) {
      return 0;
    }
    if ((start < 0) || (start >= bytes.length)) {
      throw new ArrayIndexOutOfBoundsException (
          "CASAFile.write() - invalid start index");
    }
    if (!isCASAFile ()) {
      throw new CASAIOException ("file '" + getCanonicalPath () +
                                 "' is not a CASA file");
    }
    if (CASAFileIndex.isReservedName (nodePos.getNodeName ())) {
      throw new CASAIOException ("node '" + nodePos.getNodeName () +
                                 "' is a reserved name, writing not allowed");
    }

    // checks passed so do write
    int bytesToWrite = java.lang.Math.min (length, bytes.length - start); // prevent errors...
    FilePosition writePos = null;
    FilePosition nodeFilePos = CoreIO.getFilePosition (this, nodePos);

    if (nodeFilePos != null) {
      writePos = CoreIO.write (nodeFilePos, bytes, start, bytesToWrite);
    } else { // need to create a new node
      long blockOffset = getFreeList ().getBlock ();
      if (blockOffset > 0) { // write to free block
        nodeFilePos = new FilePosition (this, blockOffset, 0L);
        writePos = CoreIO.write (nodeFilePos, bytes, start, bytesToWrite);
      } else { // no free blocks so create a whole new block and append it to the file
        CASAFileBlock block = new CASAFileBlock (0, bytes, start, bytesToWrite);
        writePos = CoreIO.appendBlock (block, this);
        blockOffset = writePos.getBlockOffset ();
        nodeFilePos = new FilePosition (this, blockOffset, 0L);
      }

      // add entry to index and set index offset in block headers
      long indexOffset = getIndex ().addEntry (nodePos.getNodeName (),
                                               blockOffset);
      CoreIO.setIndexOffset (nodeFilePos, indexOffset);
    }

    // everything succeeded, so bytesToWrite == bytes written...
    nodePos.setOffset (nodePos.getOffset () + bytesToWrite);

    return bytesToWrite;
  }

  /** convenience method for reading bytes from a node */
  public int read (byte[] bytes, String node) throws CASAIOException,
      IOException {
    NodePosition pos = new NodePosition (node, 0L); // start at beginning of node data
    return read (bytes, pos, 0, bytes.length);
  }

  /** convenience method for reading bytes from a node */
  public int read (byte[] bytes, String node, long dataOffset, int start,
                   int length) throws CASAIOException, IOException {
    NodePosition pos = new NodePosition (node, dataOffset); // start at beginning of node data
    return read (bytes, pos, start, length);
  }

  /** convenience method for reading bytes from a node */
  public int read (byte[] bytes, NodePosition pos) throws CASAIOException,
      IOException {
    return read (bytes, pos, 0, bytes.length);
  }

  /**
       * Read length bytes from the given node position within the file and store them
       * in the supplied byte array.  The supplied node position is incremented by the
   * number of bytes read.
   * @return returns the number of bytes read
   * @throws casa.io.CASAIOException if some CASA error occurs during reading
       * @throws java.io.IOException if some underlying I/O error occurs during reading
   */
  public int read (byte[] bytes, NodePosition nodePos, int start, int length) throws
      CASAIOException, IOException {

    // perform various quick fail checks
    if ((bytes == null) || (bytes.length <= 0) || (length <= 0)) {
      return 0;
    }
    if ((start < 0) || (start >= bytes.length)) {
      throw new ArrayIndexOutOfBoundsException (
          "CASAFile.read() - invalid start index");
    }
    if (!isCASAFile ()) {
      throw new CASAIOException ("file '" + getCanonicalPath () +
                                 "' is not a CASA file");
    }

    FilePosition nodeFilePos = CoreIO.getFilePosition (this, nodePos);
    //if(nodeFilePos == null) throw new NodeNotFoundException(nodePos.getNodeName());
    if (nodeFilePos == null) {
      return 0;
    }

    // checks passed so do read
    int bytesToRead = java.lang.Math.min (length, bytes.length - start); // prevent errors...

    int bytesRead = CoreIO.read (nodeFilePos, bytes, start, bytesToRead);

    nodePos.setOffset (nodePos.getOffset () + bytesRead);

    return bytesRead;
  }

  /**
   * Calculate the size of a node within the file.
   * @return length of node (in bytes), 0 if the node is not found in the file
   */
  public long getNodeLength (String nodeName) throws CASAIOException,
      IOException {
    CASAFileIndexEntry entry = getIndex ().getEntry (nodeName);
    long length = (entry == null) ? 0L :
                  CoreIO.getNodeLength (new FilePosition (this, entry.getOffset (),
        0L));
    return length;
  }

  /**
   * Get this files index.
   * @return returns an instance of CASAFileIndex represnting this files index
   * @see casa.io.CASAFileIndex
   */
  public CASAFileIndex getIndex () throws CASAIOException, IOException {
    if (!isCASAFile ()) {
      throw new CASAIOException ("file '" + getCanonicalPath () +
                                 "' is not a CASA file");
    }
    return new CASAFileIndex (this);
  }

  /**
   * Get this files free block list.
   * @return Returns an instance of CASAFileFreeList represnting this files free block list.
   * @see casa.io.CASAFileFreeList
   */
  public CASAFileFreeList getFreeList () throws CASAIOException, IOException {
    if (!isCASAFile ()) {
      throw new CASAIOException ("file '" + getCanonicalPath () +
                                 "' is not a CASA file");
    }
    return new CASAFileFreeList (this);
  }

  /** defrag this file */
  public void defragment () throws CASAIOException, IOException {
    defragment (null);
  }

  /**
   * Defrag this file, writing progress and summary to out (if != null).
   * This puts all nodes into one block apiece, removes all free blocks (and,
   * incidently the free block list) and compacts the index (ie removes deleted
   * index entries).
   * Note that this is a VERY sensitive operation, as the file offsets of the
       * nodes could possibly change, and the index entry offsets for each node could
   * change.  As a result, it is not recommended that this be used on files
       * currently in use by a live system.  ie there should not be any FilePosition
   * instances or CASAFileIndexEntry instances floating around, as they will
   * likely be invalid after defrag.
   */
  public void defragment (PrintStream log) throws CASAIOException, IOException {

    if (!isCASAFile ()) {
      throw new CASAIOException ("file '" + this.getName () +
                                 "' is not a CASA file");
    }

    boolean doLog = (log != null);

    // ensure newFile is a unique (ie non-existent) file
    String filePath = this.getCanonicalPath ();
    int suffixCount = 1;
    CASAFile newFile = new CASAFile (filePath + "." + suffixCount);
    while (newFile.exists ()) {
      suffixCount++;
      newFile = new CASAFile (filePath + "." + suffixCount);
    }

    CASAFileBlock block = null;
    CASAFileIndex fileIndex = this.getIndex ();
    CASAFileIndexEntry entry = null;
    int nodeLength;
    byte[] bytes = null;

    ///////////////////////////////////////////////////////////////////////////
    // create new file with index block and add the index as an entry itself
    ///////////////////////////////////////////////////////////////////////////

    if (doLog) {
      log.println ("creating temporary new file: " + newFile.getCanonicalPath ());

    }
    nodeLength = (int) fileIndex.getLength ();
    block = new CASAFileBlock (nodeLength);
    entry = new CASAFileIndexEntry (CASAFileIndex.INDEX_ENTRY_NAME,
                                    CASAFileHeader.HEADER_SIZE);
    bytes = entry.getBytes ();
    for (int i = 0; i < bytes.length; i++) {
      block.data[i] = bytes[i];
    }
    block.getHeader ().setFreeDataSize (block.getHeader ().getFreeDataSize () -
                                        bytes.length);
    block.getHeader ().setFreeFlag (false); // block is definitely not free!
    RandomAccessFile raFile = null;

    try {
      raFile = new RandomAccessFile (newFile, "rw");
      CASAFileHeader fileHeader = new CASAFileHeader ();
      raFile.write (fileHeader.getBytes ());
      raFile.write (block.getBytes ());
    } finally {
      if (raFile != null) {
        raFile.close ();
      }
    }

    ///////////////////////////////////////////////////////////////////////////
    // copy each node accross, since no free blocks, each node will be created
    // in a new block, and the index will be compacted.
    ///////////////////////////////////////////////////////////////////////////

    if (doLog) {
      log.println ("copying node data...");

      // now get entries and for each node in file
      // a) get node bytes
      // b) write entire node data to new file
      // note, this skips any reserved node entries (ie. the index and the free list)

    }
    java.util.Vector entries = fileIndex.getEntries ();

    if (doLog) {
      log.println ("node count: " + entries.size ());

    }
    java.util.Iterator iteration = entries.iterator ();
    String nodeName;
    NodePosition pos;
    int bytesRead, bytesWritten;

    while (iteration.hasNext ()) {
      entry = (CASAFileIndexEntry) iteration.next ();
      nodeName = entry.getName ();
      if (!CASAFileIndex.isReservedName (nodeName)) {

        if (doLog) {
          log.println ("copying node: " + nodeName);

        }
        nodeLength = (int)this.getNodeLength (nodeName);
        if (bytes.length < nodeLength) {
          bytes = new byte[nodeLength]; // ensure space in bytes array

          // read all node data
        }
        bytesRead = this.read (bytes, nodeName, 0L, 0, nodeLength);
        if (bytesRead != nodeLength) {
          throw new CASAIOException ("error reading node '" + nodeName +
                                     "' - bytes read != node length");
        }

        // node data read successfully, so write to new file
        pos = new NodePosition (nodeName);
        bytesWritten = newFile.write (bytes, pos, 0, nodeLength);
        if (bytesWritten != nodeLength) {
          throw new CASAIOException ("error writing node '" + nodeName +
                                     "' - bytes written != node length");
        }

      } else if (doLog) {
        log.println ("skipped node '" + nodeName + "' (it's a reserved node)");
      }
    }

    ///////////////////////////////////////////////////////////////////////////
    // rename old file, and rename new file
    ///////////////////////////////////////////////////////////////////////////

    if (doLog) {
      log.println ("node copy complete, renaming files...");

    }
    suffixCount = 0;
    File origFile = this.getCanonicalFile ();
    File bakFile = new File (filePath + ".bak");
    while (bakFile.exists ()) {
      suffixCount++;
      bakFile = new CASAFile (filePath + ".bak" + suffixCount);
    }

    if (doLog) {
      log.println (this.getCanonicalPath () + " --> " +
                   bakFile.getCanonicalPath ());
    }
    this.renameTo (bakFile);
    if (doLog) {
      log.println (newFile.getCanonicalPath () + " --> " +
                   origFile.getCanonicalPath ());
    }
    newFile.renameTo (origFile);

    if (doLog) {
      log.println ("defragment complete, original file copied to: " +
                   bakFile.getCanonicalPath ());
      log.println ("this.getCanonicalPath(): " + this.getCanonicalPath ());
      // to do - add defragment statistics...
    }
  }

  /**
   * Writes a byte array to a given node, overwriting any data currently stored
   * in that node.
   *
   * @param nodeName The name of the node to store the data to.
   * @param nodeData The data to be stored in the given node.
   * @throws IOException If there was an error writing to the node for any
   * reason.
   */
  public void putNode (String nodeName, byte[] nodeData) throws IOException {
    CASAOutputStream outputStream = null;

    try {
      // open the file stream to write to, just one write, so no buffer
      outputStream = new CASAOutputStream (nodeName, CASAFile.MODE_OVERWRITE, this);
      outputStream.write (nodeData);
    } catch (IOException ex) {
      throw ex;
    } finally {
      if (outputStream != null) {
        try {
          outputStream.close ();
        } catch (IOException e) {
        }
      }
    }
  }

  /**
   * Writes a byte array to a given node, appending it to any data currently
   * stored in that node.
   *
   * @param nodeName The name of the node to store the data to.
   * @param nodeData The data to be stored in the given node.
   * @throws IOException If there was an error writing to the node for any
   * reason.
   */
  public void appendNode (String nodeName, byte[] nodeData) throws IOException {
    CASAOutputStream outputStream = null;

    try {
      // open the file stream to write to, just one write, so no buffer
      outputStream = new CASAOutputStream (nodeName, CASAFile.MODE_APPEND, this);
      outputStream.write (nodeData);
    } catch (IOException ex) {
      throw ex;
    } finally {
      if (outputStream != null) {
        try {
          outputStream.close ();
        } catch (IOException e) {
        }
      }
    }
  }

  /**
   * Reads and returns the byte array stored in the given node.
   *
   * @param nodeName The name of the node to store the data to.
   * @return The data stored in the given node as a byte array.
   * @throws IOException If there was an error writing to the node for any
   * reason.
   */
  public byte[] getNode (String nodeName) throws IOException {
    CASAInputStream fileInputStream = null;
    BufferedInputStream inputStream = null;
    byte[] byteBuffer = new byte[0];

    try {
      // open the file stream to read, and buffer it
      fileInputStream = new CASAInputStream (nodeName, this);
      inputStream = new BufferedInputStream (fileInputStream);

      // read file to byte buffer
      int readLength = -1;
      String temp = new String ();
      byte[] readBuffer = new byte[16 * 1024]; // 256k buffer
      byte[] tempBuffer;
      int oldLength;

      readLength = inputStream.read (readBuffer);

      while (readLength != -1) {
        oldLength = byteBuffer.length;
        tempBuffer = new byte[oldLength + readLength];
        System.arraycopy (byteBuffer, 0, tempBuffer, 0, oldLength);
        byteBuffer = tempBuffer;
        System.arraycopy (readBuffer, 0, byteBuffer, oldLength, readLength);

        readLength = inputStream.read (readBuffer);
      }
    } catch (IOException ex) {
      throw ex;
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close ();
        } catch (IOException ex1) {
        }
      }
      if (fileInputStream != null) {
        try {
          fileInputStream.close ();
        } catch (IOException ex2) {
        }
      }
    }

    return byteBuffer;
  }
}
