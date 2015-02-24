package casa.io;

/**
 * A representation of a 'data pointer' to a position within a data node <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */


public class NodePosition {

    private String node = null;
    private long offset = 0L;

    public NodePosition(String nodeName){
        node = nodeName;
        offset = 0L;
    }

    public NodePosition(String nodeName, long dataOffset){
        node = nodeName;
        offset = dataOffset;
    }

    public String getNodeName(){ return node;}
    /**
		 * @return
		 */
    public long getOffset(){ return offset;}
    /**
		 * @param value
		 */
    public void setOffset(long value){ offset = value;}

    public String toString(){
        StringBuffer buff = new StringBuffer(node);
        buff.append(":").append(offset);
        return buff.toString();
    }

}
