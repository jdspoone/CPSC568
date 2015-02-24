package casa.io.tools;

import java.io.IOException;

import casa.io.CASAFile;
import casa.io.CASAIOException;

/**
 * Defrag is a special utility class used to defrag a CASAFile.
 * It provides command line access to CASAFile.defragment().
 * This puts all nodes into one block apiece, removes all free blocks (and,
 * incidently the free block list) and compacts the index (ie removes deleted
 * index entries).
 * Note that this is a VERY sensitive operation, as the file offsets of the
 * nodes could possibly change, and the index entry offsets for each node could
 * change.  As a result, it is not recommended that this be used on files
 * currently in use by a live system.  ie there should not be any FilePosition
 * instances or CASAFileIndexEntry instances floating around, as they will
 * likely be invalid after defrag.
 * @see casa.io.CASAFile
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

public class Defrag {
    public static void main(String[] argv){
	int retCode = 0;

	CASAFile file = (argv.length > 0) ? new CASAFile(argv[0]) : new CASAFile();

	System.out.println("defragmenting file: '" + file.getName() + "' (direct call to CASAFile.defragment())");

	try { file.defragment(System.out);}
	catch(CASAIOException cioe) {
	    System.err.println("Unexpected CASA exception during defrag: " + cioe.getMessage());
	    retCode = -1;
	} catch(IOException ioe) {
	    System.err.println("Unexpected IO exception during defrag: " + ioe.getMessage());
	    retCode = -2;
	}

	System.exit(retCode);
    }
}
