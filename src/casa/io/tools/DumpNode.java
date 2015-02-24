package casa.io.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import casa.io.CASAFile;
import casa.io.CASAIOException;

/**
 * DumpNode is a special utility class used to dump the contents of a node
 * from a CASA file to a file or standard out.
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

public class DumpNode {

    public static void main(String[] argv){
	int retCode = 0;

	if(argv.length < 2){
	    System.out.println("usage: java casa.io.DumpNode <node name> <src file> [<dest file>]");
	    System.out.println("the src file is required to be a CASA file.");
	    System.out.println("if dest file exists, or is not specified, then stdout is assumed");
	    System.exit(0);
	}

	String nodeName = argv[0];
	CASAFile file = new CASAFile(argv[1]);
	File outFile = null;
	OutputStream out = null;

	try {
	    if(argv.length > 2){
		String filePath = file.getCanonicalPath();
		int suffixCount = 1;
		outFile = new File(argv[2]);
		out = new FileOutputStream(outFile);
	    } else out = System.out;

	    int nodeLength = (int)file.getNodeLength(nodeName);
	    byte[] bytes = new byte[nodeLength];

	    // read all node data
	    int bytesRead = file.read(bytes, nodeName, 0L, 0, nodeLength);

	    if(bytesRead != nodeLength)
		throw new CASAIOException("error reading node '"+nodeName+"' - bytes read != node length");

	    // node data read successfully, so write bytes
	    out.write(bytes);

	} catch(CASAIOException cioe) {
	    System.err.println("Unexpected CASA exception: " + cioe.getMessage());
	    retCode = -1;
	} catch(IOException ioe) {
	    System.err.println("Unexpected IO exception: " + ioe.getMessage());
	    retCode = -2;
	} finally {
	    if(outFile != null) try{out.close();}catch(IOException ioe2){}
	}

	System.exit(retCode);
    }
}
