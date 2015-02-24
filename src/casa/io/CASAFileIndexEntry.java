package casa.io;

import java.io.IOException;

/**
 * Simple class to encapsulate the information in an index entry, namely the entry name and the file offset to the start of the entries data block. <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public class CASAFileIndexEntry {

    private String name;
    private long offset;

    protected CASAFileIndexEntry(String name, long offset){
        this.offset = offset;
        this.name = name;
    }

    /**
		 * @return
		 */
    public String getName(){ return name;}

    public String toString(){ return name;}

    /**
		 * @return
		 */
    public long getOffset(){ return offset;}

    protected byte[] getBytes() throws IOException {

        byte[] bytes;
        byte[] nameBytes = name.getBytes(CASAFileIndex.ENTRY_ENCODING);
        byte[] entryBytes = new byte[nameBytes.length + 6];

        int index;

        // set index file offset (essentially just after file header)
        bytes = CASAFileUtilities.toBytes(offset);
        for(index = 0; index < 4; index++) entryBytes[index] = bytes[index];

        // set name length
        int length = nameBytes.length;
        bytes = CASAFileUtilities.toBytes(length);
        entryBytes[index++] = bytes[0];
        entryBytes[index++] = bytes[1];

        // now copy name data
        for(int i = 0; i < nameBytes.length; i++, index++) entryBytes[index] = nameBytes[i];

        return entryBytes;
    }

}
