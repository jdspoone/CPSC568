package casa;

import casa.agentCom.URLDescriptor;
import casa.exceptions.ServiceDescriptorException;
import casa.exceptions.URLDescriptorException;

    /*
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

/**
 * @author  <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 */
public class ServiceDescriptor
{
  /**
	 */
  private URLDescriptor url     = null;
  /**
	 */
  private String        service = null;

  public ServiceDescriptor() {
  }
  public ServiceDescriptor( String        service,
                            URLDescriptor url ) {
    this.service = service;
    this.url     = url;
  }

  /**
	 * @return
	 */
  public String getService() {
    return service;
  }
  /**
	 * @param service
	 */
  public void setService( String service ) {
    this.service = service;
  }

  public URLDescriptor getURL() {
    return url;
  }
  public void setURL( URLDescriptor url ) {
    this.url = url;
  }
  /**
   * other methods
   *
   * @returns <li> the buffer with either 'null' appended whenever the service or the
   *          url are null </li><br>
   *          <li> the buffer with the service and/or the URL </li>
   *
   */
  public String toString() {
    StringBuffer          buffer = new StringBuffer();

    if( service == null ) buffer.append( ML.BLANK + ML.NULL );
    else                  buffer.append( ML.BLANK + TokenParser.makeFit( service ));

    if( url     == null ) buffer.append( ML.BLANK + ML.NULL );
    else                  buffer.append( ML.BLANK + url.toString(null) );

    return                buffer.toString();
  }

  public static void fromString( TokenParser       parser,
                                 ServiceDescriptor descriptor ) throws ServiceDescriptorException {
    try {
      String str = parser.getNextToken();
      if(   !str.equals( ML.NULL ))
             descriptor.setService( str  );

      URLDescriptor      url = URLDescriptor.fromString( parser );
      descriptor.setURL( url );
    }
    catch( URLDescriptorException e ) {
      throw new ServiceDescriptorException();
    }
  }

  public static ServiceDescriptor fromString( TokenParser parser ) throws ServiceDescriptorException {
    ServiceDescriptor   descriptor = new ServiceDescriptor();
    fromString( parser, descriptor );
                 return descriptor;
  }
}
