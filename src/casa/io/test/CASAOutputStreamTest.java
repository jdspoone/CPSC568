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
public class CASAOutputStreamTest {
    public static void main(String[] args){

	if(args.length < 3){
	    System.out.println("usage: java casa.io.test.CASAOutputStreamTest <file> <node> <data> [<data> [...] ]");
	    System.exit(0);
	}

	CASAFile file = null;
	CASAOutputStream out = null;

	try {
	    file = new CASAFile(args[0]);

	    System.out.println("creating stream...");
	    out = new CASAOutputStream(args[1], CASAFile.MODE_APPEND, file);
	    System.out.println("stream created writing data...");

	    PrintWriter pw = new PrintWriter(out);
	    for(int i = 2; i < args.length; i++) pw.println(args[i]);

	    pw.flush();
	    out.flush();

	    System.out.println("done");

	} catch(Exception e){
	    System.err.println("unexpected exception: " + e.getMessage());
	}

	System.exit(0);
    }

}
