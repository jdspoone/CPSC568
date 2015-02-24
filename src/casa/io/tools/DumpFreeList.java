package casa.io.tools;

import casa.io.CASAFile;
import casa.io.CASAFileFreeList;

/**
 * DumpFreeList prints out the contents of a CASA file free block list.
 * <p>usage: java casa.io.tools.DumpFreeList [<file name>]
 * <br>  if file name is not included, then the default file name is used.
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
public class DumpFreeList {
    public static void main(String[] args){
	String fileName = (args.length > 0) ? args[0] : CASAFile.DEFAULT_FILE_NAME;
	try {
	    CASAFile file = new CASAFile(fileName);
	    if(file.isCASAFile()){
		CASAFileFreeList freeList = file.getFreeList();
		java.util.Vector entries = freeList.getFreeBlocks();
		if(entries == null) System.out.println("no free list in file " + fileName);
		else if(entries.size() <= 0) System.out.println("no free blocks in file " + fileName);
		else {
		    System.out.println("free blocks for file " + fileName + ":");
		    java.util.Iterator iter = entries.iterator();
		    long val;
		    while(iter.hasNext()){
			val = new Long(iter.next().toString()).longValue();
			System.out.println("  0x" + Long.toHexString(val));
		    }
		}
	    } else System.out.println(fileName + " is not a CASA file");
	} catch(Exception e){
	    System.err.println("unexpected exception: " + e.getMessage());
	}
	System.exit(0);
    }
}
