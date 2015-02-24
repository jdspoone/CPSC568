package casa.io.tools;

import casa.io.CASAFile;

/**
 * CheckFile check to see if a file is a CASA file.
 * <p>usage: java casa.io.tools.CheckFile [<file name>]
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

public class CheckFile {
    public static void main(String[] argv){
	CASAFile file = (argv.length > 0) ? new CASAFile(argv[0]) : new CASAFile();
	if(file.isCASAFile()) System.out.println("file '" + file.getName() + "' is a CASA file");
	else System.out.println("file '" + file.getName() + "' is not a CASA file");
	System.exit(0);
    }
}
