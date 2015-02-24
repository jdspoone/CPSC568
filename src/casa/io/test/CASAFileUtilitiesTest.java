package casa.io.test;

import casa.io.CASAFileUtilities;

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
public class CASAFileUtilitiesTest {

    public static final int LOW_VERBOSITY = 0;
    public static final int MED_VERBOSITY = 1;
    public static final int HIGH_VERBOSITY = 2;

    public static void main(String[] args){

	if(args.length <= 0){
	    CASAFileUtilitiesTest.testConversion(-1L);
	    CASAFileUtilitiesTest.testConversion(0L);
	    CASAFileUtilitiesTest.testConversion(1L);
	    CASAFileUtilitiesTest.testConversion(CASAFileUtilities.MAX_UNSIGNED_LONG - 1L);
	    CASAFileUtilitiesTest.testConversion(CASAFileUtilities.MAX_UNSIGNED_LONG);
	    CASAFileUtilitiesTest.testConversion(CASAFileUtilities.MAX_UNSIGNED_LONG + 1L);

	    CASAFileUtilitiesTest.testConversion(-1);
	    CASAFileUtilitiesTest.testConversion(0);
	    CASAFileUtilitiesTest.testConversion(1);
	    CASAFileUtilitiesTest.testConversion(CASAFileUtilities.MAX_UNSIGNED_INT - 1);
	    CASAFileUtilitiesTest.testConversion(CASAFileUtilities.MAX_UNSIGNED_INT);
	    CASAFileUtilitiesTest.testConversion(CASAFileUtilities.MAX_UNSIGNED_INT + 1);

	    short value = -1;
	    CASAFileUtilitiesTest.testConversion(value);
	    value = 0;
	    CASAFileUtilitiesTest.testConversion(value);
	    value = 1;
	    CASAFileUtilitiesTest.testConversion(value);
	    value = CASAFileUtilities.MAX_UNSIGNED_SHORT - 1;
	    CASAFileUtilitiesTest.testConversion(value);
	    value = CASAFileUtilities.MAX_UNSIGNED_SHORT;
	    CASAFileUtilitiesTest.testConversion(value);
	    value = CASAFileUtilities.MAX_UNSIGNED_SHORT + 1;
	    CASAFileUtilitiesTest.testConversion(value);

	} else {
	    long value;
	    for(int i = 0; i < args.length; i++){
		try {
		    value = Long.valueOf(args[i]).longValue();
		    CASAFileUtilitiesTest.testConversion(value);
		} catch(Exception e){
		    System.out.println("argument '" + args[i] + "' not parsable as a long value, skipping...");
		}
	    }
	}

	System.exit(0);
    }

    public static boolean testConversion(long testValue){
	return CASAFileUtilitiesTest.testConversion(testValue, HIGH_VERBOSITY);
    }

    public static boolean testConversion(long testValue, int verbosity){

	byte[] testBytes = CASAFileUtilities.toBytes(testValue);
	long retValue = CASAFileUtilities.toLong(testBytes);
	boolean passed = (retValue == testValue);
	String resultMessage;

	if(passed) resultMessage = "conversion test long -> bytes -> long passed";
	else resultMessage = "conversion test long -> bytes -> long failed";

	switch(verbosity){
	case LOW_VERBOSITY: break;
	case MED_VERBOSITY: System.out.println(resultMessage); break;
	case HIGH_VERBOSITY:
	default:
	    System.out.println(resultMessage);
	    System.out.print("test value: " + testValue + " converted value: " + retValue + " bytes:");
	    CASAFileUtilities.toStream(System.out, testBytes);
	}

	return passed;
    }

    public static boolean testConversion(int testValue){
	return CASAFileUtilitiesTest.testConversion(testValue, HIGH_VERBOSITY);
    }

    public static boolean testConversion(int testValue, int verbosity){

	byte[] testBytes = CASAFileUtilities.toBytes(testValue);
	int retValue = CASAFileUtilities.toInt(testBytes);
	boolean passed = (retValue == testValue);
	String resultMessage;

	if(passed) resultMessage = "conversion test int -> bytes -> int passed";
	else resultMessage = "conversion test int -> bytes -> int failed";

	switch(verbosity){
	case LOW_VERBOSITY: break;
	case MED_VERBOSITY: System.out.println(resultMessage); break;
	case HIGH_VERBOSITY:
	default:
	    System.out.println(resultMessage);
	    System.out.print("test value: " + testValue + " converted value: " + retValue + " bytes:");
	    CASAFileUtilities.toStream(System.out, testBytes);
	}

	return passed;
    }

    public static boolean testConversion(short testValue){
	return CASAFileUtilitiesTest.testConversion(testValue, HIGH_VERBOSITY);
    }

    public static boolean testConversion(short testValue, int verbosity){

	byte[] testBytes = CASAFileUtilities.toBytes(testValue);
	short retValue = CASAFileUtilities.toShort(testBytes);
	boolean passed = (retValue == testValue);
	String resultMessage;

	if(passed) resultMessage = "conversion test short -> bytes -> short passed";
	else resultMessage = "conversion test short -> bytes -> short failed";

	switch(verbosity){
	case LOW_VERBOSITY: break;
	case MED_VERBOSITY: System.out.println(resultMessage); break;
	case HIGH_VERBOSITY:
	default:
	    System.out.println(resultMessage);
	    System.out.print("test value: " + testValue + " converted value: " + retValue + " bytes:");
	    CASAFileUtilities.toStream(System.out, testBytes);
	}

	return passed;
    }

}
