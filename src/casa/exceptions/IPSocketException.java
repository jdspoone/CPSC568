package casa.exceptions;

/**
 * <p>Title: IPSocketException</p>
 * <p>Description: Thrown when an agent attempts to bind to an IPSocket (port) that
 *                 is invalid or in use.</p>
 *
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
 *
 * <p>Company: University of Calgary</p>
 *
 * @see IPSocketServer
 * @author Chad La Fournie
 *
 */

public class IPSocketException extends Exception {

  public IPSocketException() {
  }

  public IPSocketException( String s ){
    super( s );
  }
}