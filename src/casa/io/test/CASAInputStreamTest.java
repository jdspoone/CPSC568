package casa.io.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import casa.io.CASAFile;
import casa.io.CASAInputStream;

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
public class CASAInputStreamTest {
    public static void main(String[] args){

	if(args.length < 2){
	    System.out.println("usage: java casa.io.test.CASAOutputStreamTest <file> <node>");
	    System.exit(0);
	}

	CASAFile file = null;
	CASAInputStream in = null;

	try {
	    file = new CASAFile(args[0]);

	    System.out.println("creating stream...");
	    in = new CASAInputStream(args[1], file);
	    System.out.println("stream created reading data...");

	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
	    String line = br.readLine();

	    while(line != null){
		System.out.println(line);
		line = br.readLine();
	    }

	    System.out.println("done");

	} catch(Exception e){
	    System.err.println("unexpected exception: " + e.getMessage());
	}

	System.exit(0);
    }

}
