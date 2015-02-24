package casa.ui;

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
 * @author <a href="mailto:ayala@cpsc.ucalgary.ca">Gabriel Becerra</a>
 * @deprecated
 */

@Deprecated
public class CasaInactiveAgentData {
  //
  // Objects
  //
  protected Object casaData;

  public CasaInactiveAgentData(Object casaData) {
    this.casaData = casaData;
  }

  public Object getObject() {
    return casaData;
  }

  @Override
  public String toString() {
    return casaData.toString();
  }
}