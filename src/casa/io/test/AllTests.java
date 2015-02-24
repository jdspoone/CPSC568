package casa.io.test;

import casa.io.CASAFile;
import casa.io.CASAFileFreeList;
import casa.io.CASAFileIndex;
import casa.io.CASAInputStream;
import casa.io.CASAOutputStream;

/**
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
public class AllTests {
    public static void main(String[] args){

	byte[] outDataSmall = new byte[512];
	byte[] outDataLarge = new byte[1024];
	byte[] inDataSmall = new byte[512];
	byte[] inDataLarge = new byte[1024];

	java.util.Random rnd = new java.util.Random();
	rnd.nextBytes(outDataSmall);
	rnd.nextBytes(outDataLarge);

	System.out.println("performing CASA File I/O test harness");

	String fileName = (args.length > 0) ? args[0] : CASAFile.DEFAULT_FILE_NAME;

	CASAFile file = new CASAFile(fileName);
	int testsPassed = 0;
	int testsConducted = 1;

	// if file test fails, no point continuing!
	if(testCASAFile(file)){
	    testsPassed++;

	    boolean result;

	    // stream writes and reads
	    testsConducted++;
	    String nodeName = "small node data test";
	    result = streamIOTest(file, nodeName, outDataSmall);
	    if(!result) System.out.println("test failed");
	    else testsPassed++;
	    nodeName = "large node data test";
	    testsConducted++;
	    result = streamIOTest(file, nodeName, outDataLarge);
	    if(!result) System.out.println("test failed");
	    else testsPassed++;

	    // test node deletion
	    testsConducted++;
	    result = nodeDeletionTest(file, outDataLarge);
	    if(!result) System.out.println("test failed");
	    else testsPassed++;

	    // testing arbitrary reads and writes
	    testsConducted++;
	    nodeName = "arbitrary small node data test";
	    result = arbitraryIOTest(file, nodeName, outDataSmall);
	    if(!result) System.out.println("test failed");
	    else testsPassed++;
	    nodeName = "arbitrary large node data test";
	    testsConducted++;
	    result = arbitraryIOTest(file, nodeName, outDataLarge);
	    if(!result) System.out.println("test failed");
	    else testsPassed++;
	}

	System.out.println("finished CASA File I/O test harness, tests passed: "+
			   testsPassed + " of " + testsConducted);

	System.exit(0);
    }

    public static boolean nodeDeletionTest(CASAFile file, byte[] bytes){
	boolean testPassed = false;

	System.out.println("node deletion test started");

	try {
	    // print lists
	    System.out.println("node deletion test - current index and free list");
	    printIndex(file);
	    printFreeList(file);

	    System.out.println("node deletion test - adding node to delete");
	    String node = "node to delete";
	    CASAOutputStream out = new CASAOutputStream(node, CASAFile.MODE_OVERWRITE, file);
	    out.write(bytes);
	    out.flush();

	    if(file.deleteNode(node)){
		System.out.println("node " + node + " deleted");
		testPassed = true;
	    } else System.out.println("node " + node + " not deleted");

	    System.out.println("node deletion test - current index and free list");
	    printIndex(file);
	    printFreeList(file);
	} catch(Exception e){
	    System.err.println("nodeDeletionTest() - unexpected exception: " + e.getMessage());
	}

	return testPassed;
    }

    public static boolean arbitraryIOTest(CASAFile file, String node, byte[] bytes){
	boolean testPassed = false;

	System.out.println("arbitrary I/O test using node '" + node + "'");

	try {
	    int bytesWritten = file.write(bytes, node);
	    if(bytesWritten != bytes.length)
		System.out.println("bytes written do not equal bytes supplied for writing!");
	    else {
		byte[] inbytes = new byte[bytes.length];
		int bytesRead = file.read(inbytes, node);
		if(bytesRead == bytes.length){
		    // check that the byte arrays are the same
		    int i = 0;
		    while((i < bytes.length) && (bytes[i] == inbytes[i])) i++;
		    if(i >= bytes.length) testPassed = true;
		    else System.out.println("bytes read do not equal bytes written!");
		} else System.out.println("# bytes read (" + bytesRead +
					  ") != # bytes written (" + bytesWritten + ")");
	    }
	} catch(Exception e){
	    System.err.println("arbitraryIOTest() - unexpected exception: " + e.getMessage());
	}

	return testPassed;
    }

    public static boolean streamIOTest(CASAFile file, String node, byte[] bytes){
	boolean testPassed = false;

	System.out.println("stream I/O test using node '" + node + "'");

	try {
	    CASAOutputStream out = new CASAOutputStream(node, CASAFile.MODE_OVERWRITE, file);
	    out.write(bytes);
	    out.flush();
	    byte[] inbytes = new byte[bytes.length];
	    CASAInputStream in = new CASAInputStream(node, file);
	    int bytesRead = in.read(inbytes);
	    if(bytesRead == bytes.length){
		// check that the byte arrays are the same
		int i = 0;
		while((i < bytes.length) && (bytes[i] == inbytes[i])) i++;
		if(i >= bytes.length) testPassed = true;
		else System.out.println("bytes read do not equal bytes written!");

	    } else System.out.println("# bytes read ("+bytesRead+
				      ") != # bytes written ("+bytes.length+")");
	} catch(Exception e){
	    System.err.println("streamIOTest() - unexpected exception: " + e.getMessage());
	}

	return testPassed;
    }

    public static boolean testCASAFile(CASAFile file){
	boolean testPassed = false;

	try {
	    String path = file.getCanonicalPath();
	    if(file.exists()){
		if(file.isCASAFile()){
		    System.out.println("file '" + path + "' is a CASA File, v" +
				       file.getVersion() + " (" + file.getVersionEncoded() + ")");
		    testPassed = true;
		} else System.out.println("file '" + path + "' is not a CASA File");
	    } else {
		System.out.println("file '" + path + "' does not exist, attempting to create it...");
		if(!file.createNewFile()) System.out.println("file '" + path + "' not created.");
		else {
		    System.out.println("file '" + path + "' created.");
		    testPassed = true;
		}
	    }
	} catch(Exception e){
	    System.err.println("testCASAFile() - unexpected exception: " + e.getMessage());
	}

	return testPassed;
    }

    public static void printIndex(CASAFile file){
	try {
	    CASAFileIndex index = file.getIndex();

	    java.util.Vector entries = index.getEntries();

	    if((entries == null) || (entries.size() <= 0)){
		System.out.println("no entries in index! should be at least one (the index itself)");
	    } else {
		System.out.println(" index length: " + index.getLength());
		System.out.println("index entries:");
		java.util.Iterator iter = entries.iterator();
		while(iter.hasNext()) System.out.println("  " + iter.next().toString());
	    }
	} catch(Exception e){
	    System.err.println("printIndex() - unexpected exception: " + e.getMessage());
	}
    }

    public static void printFreeList(CASAFile file){
	try {
	    CASAFileFreeList freeList = file.getFreeList();

	    java.util.Vector entries = freeList.getFreeBlocks();

	    if(entries == null) System.out.println("no free list block in file");
	    else if(entries.size() <= 0) System.out.println("no free blocks in file");
	    else {
		System.out.println("free blocks:");
		java.util.Iterator iter = entries.iterator();
		long val;
		while(iter.hasNext()){
		    val = new Long(iter.next().toString()).longValue();
		    System.out.println("  0x" + Long.toHexString(val));
		}
	    }
	} catch(Exception e){
	    System.err.println("printIndex() - unexpected exception: " + e.getMessage());
	}
    }

}
