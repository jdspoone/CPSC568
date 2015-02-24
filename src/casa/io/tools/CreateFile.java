package casa.io.tools;

import casa.io.CASAFile;

/**
 * CreateFile creates a new CASA file.
 * <p>usage: java casa.io.tools.CreateFile <file name>
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

public class CreateFile {
    public static void main(String[] argv){
	if(argv.length < 1) System.out.println("usage: java casa.io.tools.CreateFile <file name>");
	else {
	    CASAFile file = new CASAFile(argv[0]);
	    boolean created = false;
	    if(!file.exists()){
		try {
		    if(file.createNewFile()) System.out.println("CASA file '" + argv[0] + "' created");
		    else System.out.println("CASA file '" + argv[0] + "' creation failed");
		} catch(Exception e){
		    System.out.println("unexpected exception: "+e.getMessage());
		}
	    } else if(file.isCASAFile()) System.out.println("file '" + argv[0] + "' already exists as a CASA file");
	    else System.out.println("file '" + argv[0] + "' already exists but is not a CASA file.");
	}
	System.exit(0);
    }
}
