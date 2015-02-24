package casa.io.test;

import java.io.PrintWriter;

import casa.io.CASAFile;
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
public class CASAFileWriteTest {
    public static void main(String[] args){

	if(args.length < 3){
	    CASAFileWriteTest.printUsage();
	    System.exit(0);
	}

	String fileName = args[0];
	String nodeName = args[1];
	CASAFile file;
	CASAOutputStream stream;
	PrintWriter writer;

	try {

	    System.out.println("initializing file...");
	    file = new CASAFile(fileName);
	    file.createNewFile();

	    //writing bytes
	    System.out.println("writing bytes...");

	    System.out.println("initializing stream...");
	    stream = new CASAOutputStream(nodeName, CASAFile.MODE_APPEND, file);

	    System.out.println("initializing writer...");
	    writer = new PrintWriter(stream);

	    System.out.println("writing data...");

	    for(int i = 2; i < args.length; i++){
		System.out.println("writing '" + args[i] + "'");
		writer.println(args[i]);
		writer.flush();
	    }

	    System.out.println("closing stream...");
	    stream.close();

	    System.out.println("test finished...");

	} catch(Exception e){
	    System.err.println("unexpected exception: " + e.getMessage());
	}

	System.exit(0);
    }

    public static void printUsage(){
	System.out.println();
	System.out.println("usage: java casa.io.test.CASAFileWriteTest <casa file name> <node name> <node data> [<node data> ...]");
	System.out.println();
    }
}
