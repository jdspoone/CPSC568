package plistreader.test;

import java.io.File;
import java.util.Properties;

import plistreader.AbstractReader;
import plistreader.AbstractWriter;
import plistreader.PlistFactory;
import plistreader.PlistProperties;
import plistreader.PlistReaderException;

/**
 * <p>Title: PlistReader</p>
 *
 * <p>Description: Package to read and write PLIST files on OsX</p>
 *
 * <p>Copyright: 2007, Gie Spaepen</p>
 *
 * <p>University of Antwerp</p>
 *
 * <p>Use this class to start a testapplication in the console.  This app only
 * needs <b>one</b> argument which must be a valid path to a file as a
 * <code>String</code> wrapped in a <code>String[]</code>.  This path may
 * be absolute or relative (to the path of this package).</p>
 *
 * <p>The results of this testapplication are printed to the console you're
 * working in.</p>
 *
 *
 * @author Gie Spaepen
 * @version 1.2
 */
public class testReader {

    /**
     * Main function to start the testapplication.
     * @param args String[] - This array must have a length of 1: 1 filepath
     */
    public static void main(String[] args){
    //Print header
    System.out.println("***********************************************");
    System.out.println("*         PlistReader Testapplication         *");
    System.out.println("*                                             *");
    System.out.println("* (c)2007 Gie Spaepen - University of Antwerp *");
    System.out.println("***********************************************");
    System.out.println("");
    if(args.length == 1){
      //Create a reader with the PlistFactory
      AbstractReader reader = (new PlistFactory()).createReader();
      try {
        //Get the file
        File readFile = new File(args[0]);
        File writeFile = new File("writeTest.plist");
        //Print the read test header
        System.out.println("Read test: "+readFile.getPath());
        System.out.println("Structured Content:");

        //Parse the content and store it into a PlistProperties object
        PlistProperties props = reader.parse(readFile);

        //List it and use the System.out printstream
        props.list(System.out);

        //Conversion test
        System.out.println("");
        System.out.println("***********************************************");
        System.out.println("Now performing 2 conversion tests...");
        System.out.println("Firstly convert PlistProperties to Properties and then the way round!");
        Properties oldprops = PlistProperties.convertToProperties(props);
        oldprops.list(System.out);
        System.out.println("");System.out.println("Step 2...");System.out.println("");
        PlistProperties.convertToPlistProperties(oldprops).list(System.out);

        //Write content
        System.out.println("");
        System.out.println("***********************************************");
        System.out.println("Write test: "+writeFile.getPath());
        System.out.println("Write content");
        AbstractWriter writer = (new PlistFactory()).createWriter();
        writer.write(writeFile,props);
        System.out.println("Done");


      }
      catch (Exception ex) {
        //Catch exceptions from the reader object
        ex.printStackTrace();
        System.out.println("Test not succesfully passed... check errors.");
      }
      System.out.println("***********************************************");
    }
    else{
      PlistReaderException.NO_ARGUMENTS.printStackTrace();
    }
  }

}
