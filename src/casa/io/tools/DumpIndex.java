package casa.io.tools;

import casa.io.CASAFile;
import casa.io.CASAFileIndex;

/**
 * DumpIndex prints out the contents of a CASA file index.
 * <p>usage: java casa.io.tools.DumpIndex [<file name>]
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
public class DumpIndex {
    public static void main(String[] args){
	String fileName = (args.length > 0) ? args[0] : CASAFile.DEFAULT_FILE_NAME;
	try {
	    CASAFile file = new CASAFile(fileName);
	    if(file.isCASAFile()){
		CASAFileIndex index = file.getIndex();
		java.util.Vector entries = index.getEntries();
		if((entries != null) && (entries.size() > 0)){
		    System.out.println("entries for file " + fileName + ":");
		    java.util.Iterator iter = entries.iterator();
		    while(iter.hasNext()) System.out.println(iter.next().toString());
		} else System.out.println("no entries! should be at least one (the index itself)");
	    } else System.out.println(fileName + " is not a CASA file");
	} catch(Exception e){
	    System.err.println("unexpected exception: " + e.getMessage());
	}
	System.exit(0);
    }
}
