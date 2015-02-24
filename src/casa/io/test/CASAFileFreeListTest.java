package casa.io.test;

import casa.io.CASAFile;
import casa.io.CASAFileFreeList;
import casa.io.CASAFileIndexEntry;

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
public class CASAFileFreeListTest {

    public static void main(String[] args){

	if(args.length < 1){
	    System.out.println("usage: java casa.io.test.CASAFileFreeListTest <file> [<node to delete>]");
	    System.exit(0);
	}

	CASAFile file = null;

	try {
	    file = new CASAFile(args[0]);

	    printFreeList(file);

	    if(args.length > 1){
		System.out.println("attempting to delete node '" + args[1] + "' from file: '" + args[0] + "'");
		System.out.println();
		System.out.println("Checking node existence...");
		CASAFileIndexEntry entry = file.getIndex().getEntry(args[1]);
		if(entry != null) System.out.println("node exists in file");
		else System.out.println("node does not exist in file");

		System.out.println();
		System.out.println("attempting node removal...");
		if(file.deleteNode(args[1])) System.out.println("node deleted");
		else System.out.println("node not deleted");

		printFreeList(file);
	    }
	} catch(Exception e){
	    System.err.println("unexpected exception: " + e.getMessage());
	}

	System.out.println();
	System.out.println("CASAFileFreeListTest done");

	System.exit(0);
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
