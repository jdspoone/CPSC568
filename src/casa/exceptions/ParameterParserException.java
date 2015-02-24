package casa.exceptions;

/**
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
public class ParameterParserException extends Exception {
    public static final int UNKNOWN = 0;
    public static final int URL = 1;
    public static final int PORT = 2;

    public static final String UNKNOWN_MSG = "unknown";
    public static final String URL_MSG = "error parsing URL parameter";
    public static final String PORT_MSG = "error parsing port parameter";

    private int error;

    private static String getMsg(int err){
	String msg = null;
	switch(err){
	case URL: msg = URL_MSG; break;
	case PORT: msg = PORT_MSG; break;
	case UNKNOWN:
	default: msg = UNKNOWN_MSG;
	}
	return msg;
    }

    public ParameterParserException() {
	super(UNKNOWN_MSG);
	this.error = UNKNOWN;
    }

    public ParameterParserException(String msg) {
	super(msg);
	this.error = UNKNOWN;
    }

    public ParameterParserException( int error ) {
	super(ParameterParserException.getMsg(error));
	this.error = error;
    }

    /**
		 * @return
		 */
    public int getError() {
	return error;
    }
}
