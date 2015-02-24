package casa.io;

import java.io.PrintStream;

/**
 * A collection of functions and constants used to make life simpler when working with the CASA I/O.
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

public class CASAFileUtilities {

    /** The maximum unsigned 32 bit value. */
    public static final long MAX_UNSIGNED_LONG = 0x00000000FFFFFFFFL;

    /** The maximum unsigned 16 bit value. */
    public static final int MAX_UNSIGNED_INT = 0x0000FFFF;

    /** The maximum unsigned 8 bit value. */
    public static final short MAX_UNSIGNED_SHORT = 0x00FF;

    /** A mask for the lower 8 bits of a Java long. */
    protected static final long LONG_BYTE_MASK = 0x00000000000000FFL;

    /** A mask for the lower 8 bits of a Java int. */
    protected static final int INT_BYTE_MASK = 0x000000FF;

    /**
     * Convert a long (64 bit value in Java) to an unsigned 32 bit representation  in 4 bytes.
     * @return 4 byte array (big endian)
     */
    public static byte[] toBytes(long aValue){
	byte[] bytes = new byte[4];
	java.util.Arrays.fill(bytes, (byte)0x00); // clear array

	if((aValue >= 0) && (aValue <= MAX_UNSIGNED_LONG)){
	    // only handle if 0 <= value <= MAX_UNSIGNED_LONG
	    long byteValue, value;
	    int index = 3;
	    value = aValue;
	    while((index >= 0) && (value > 0)){
		byteValue = LONG_BYTE_MASK & value;
		bytes[index--] = (byte)byteValue;
		value >>= 8;
	    }
	}

	return bytes;
    }

    /**
     * Convert an int (32 bit value in Java) to an unsigned 16 bit representation in 2 bytes.
     * @return 2 byte array (big endian)
     */
    public static byte[] toBytes(int aValue){
	byte[] bytes = new byte[2];
	java.util.Arrays.fill(bytes, (byte)0x00); // clear array
	if((aValue >= 0) && (aValue <= MAX_UNSIGNED_INT)){
	    // only handle if 0 <= value <= MAX_UNSIGNED_LONG
	    int value = aValue;
	    bytes[1] = (byte)(INT_BYTE_MASK & value);
	    value >>= 8;
	    bytes[0] = (byte)(INT_BYTE_MASK & value);
	}

	return bytes;
    }

    /**
     * Convert a short (16 bit value in Java) to an unsigned 8 bit representation in 1 byte.
     */
    public static byte[] toBytes(short aValue){
	byte[] bytes = new byte[1];
	bytes[0] = toByte(aValue);
	return bytes;
    }

    /**
     * Convert a short (16 bit value in Java) to an unsigned 8 bit representation in 1 byte.
     */
    public static byte toByte(short aValue){
	return (byte)(INT_BYTE_MASK & (int)aValue);
    }

    /**
     * Convert 4 bytes to an int (interpret 4 bytes as an UNSIGNED long value).
     * @throws ArrayIndexOutOfBoundsException if not 4 bytes in array
     */
    public static long toLong(byte[] bytes){ return CASAFileUtilities.toLong(bytes, 0);}

    /**
     * Convert 4 bytes to an int (interpret 4 bytes as an UNSIGNED long value).
     * @throws ArrayIndexOutOfBoundsException if not 4 bytes in array
     */
    public static long toLong(byte[] bytes, int start){
	long value = (LONG_BYTE_MASK & (int)bytes[start]);
	value <<= 8;
	value += (LONG_BYTE_MASK & (int)bytes[start + 1]);
	value <<= 8;
	value += (LONG_BYTE_MASK & (int)bytes[start + 2]);
	value <<= 8;
	value += (LONG_BYTE_MASK & (int)bytes[start + 3]);
	return value;
    }

    /**
     * Convert 2 bytes to an int (interpret 2 bytes as an UNSIGNED integer value).
     * @throws ArrayIndexOutOfBoundsException if not 2 bytes in array
     */
    public static int toInt(byte[] bytes){ return CASAFileUtilities.toInt(bytes, 0);}

    /**
     * Convert 2 bytes to an int (interpret 2 bytes as an UNSIGNED integer value).
     * @throws ArrayIndexOutOfBoundsException if not 2 bytes in array
     */
    public static int toInt(byte[] bytes, int start){
	int value = INT_BYTE_MASK & (int)bytes[start];
	value <<= 8;
	value += (INT_BYTE_MASK & (int)bytes[start + 1]);
	return value;
    }

    /**
     * Convert 1 byte to a short (interpret 1 byte as an UNSIGNED short value).
     * @throws ArrayIndexOutOfBoundsException if empty array
     */
    public static short toShort(byte[] bytes){ return CASAFileUtilities.toShort(bytes[0]);}

    /**
     * Convert 1 byte to a short (interpret 1 byte as an UNSIGNED short value).
     * @throws ArrayIndexOutOfBoundsException if empty array
     */
    public static short toShort(byte[] bytes, int start){ return CASAFileUtilities.toShort(bytes[start]);}
    /**
     * Convert 1 byte to a short (interpret 1 byte as an UNSIGNED short value).
     */
    public static short toShort(byte aByte){ return (short)(INT_BYTE_MASK & (int)aByte);}

    /**
     * Dump out bytes in a nicely formatted hex value format to the supplied stream.
     */
    public static void toStream(PrintStream out, byte[] bytes){ CASAFileUtilities.toStream(out, bytes, 0, bytes.length);}

    /**
     * Dump out bytes in a nicely formatted hex value format to the supplied stream.
     */
    public static void toStream(PrintStream out, byte[] bytes, int startIndex, int endIndex){
	int index, end, count, byteVal;
	index = startIndex;
	end = (endIndex > bytes.length) ? bytes.length : endIndex;
	while(index < end){
	    // write lines of 16 bytes
	    for(count = 0; (count < 16) && (index < end); count++, index++){
		byteVal = INT_BYTE_MASK & (int)bytes[index];
		out.print(" ");
		if(byteVal < 16) out.print("0");
		out.print(Integer.toHexString(byteVal));
	    }
	    out.println();
	}
    }


    /**
     * Convert an int array to a byte array.  This uses only the lower byte of the
     * int as the byte value.  The top 24 bits of the int values are ignored.
     */
    public static byte[] toByteArray(int[] data){ return toByteArray(data, 0, data.length);}

    /**
     * Convert an int array to a byte array.  This uses only the lower byte of the
     * int as the byte value.  The top 24 bits of the int values are ignored.
     */
    public static byte[] toByteArray(int[] data, int start, int length){
	byte[] bytes = new byte[length];
	int index = start;
	for(int i = 0; i < length; i++, index++) bytes[i] = (byte)(INT_BYTE_MASK & data[index]);
	return bytes;
    }

    /**
     * Convert a byte array to an int array.  This uses only the lower byte of the int
     * as the byte value.  The top 24 bits of the int values should be ignored
     */
    public static int[] toIntArray(byte[] data){ return toIntArray(data, 0, data.length);}

    /**
     * Convert a byte array to an int array.  This uses only the lower byte of the int
     * as the byte value.  The top 24 bits of the int values should be ignored
     */
    public static int[] toIntArray(byte[] data, int start, int length){
	int[] ints = new int[length];
	int index = start;
	for(int i = 0; i < length; i++, index++) ints[i] = INT_BYTE_MASK & (int)data[index];
	return ints;
    }
}
