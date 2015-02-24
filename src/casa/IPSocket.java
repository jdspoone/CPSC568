package casa;

//import casa.cp.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * This class is an extremely thin (and trasparent) holder/wrapper for the
 * java.net.Socket class.
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
 */
public abstract class IPSocket
{
  /**
   * Refers to the socket that this object is responsible for holding.
   */
  protected Socket socket;

  /**
   * Default constructor. Do NOT use the default constructor and then use the
   * methods that this class offers without first assigning the
   * <CODE>socket</CODE> field.
   */
  public IPSocket() {
  }


  /**
   * Constructs an object with <CODE>this.socket</CODE> already refering to the
   * socket specified.
   *
   * @param socket  socket to hold with this object
   */
  public IPSocket(Socket socket) {
    this.socket = socket;
  }

  /**
   * @returnd the address corresponding to the socket held by this object.
   *
   * @see java.net.Socket#getInetAddress()
   */
  public InetAddress getInetAddress() {
    return socket.getInetAddress();
  }

  /**
   * @returns the port corresponding to the socket held by this object.
   *
   * @see java.net.Socket#getLocalPort()
   */
  public int getIPport() {
    return socket.getLocalPort();
  }
  
  /**
   * Closes the socket.
   */
  protected void close() {
    try {socket.close();}
    catch (IOException ex) {}
  }
}

