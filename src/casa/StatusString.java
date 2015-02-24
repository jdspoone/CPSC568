package casa;

import casa.util.CASAUtil;

/**
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose is hereby granted without fee, provided that the above copyright notice appear in all copies and that both that copyright notice and this permission notice appear in supporting documentation.  The Knowledge Science Group makes no representations about the suitability of this software for any purpose.  It is provided "as is" without express or implied warranty.</p>
 * @author  <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public class StatusString extends Status {
  /**
	 */
  private String data;

  public StatusString() {
    super();

    this.data = "";
  }

  public StatusString(int status, String data) {
    super(status);

    this.data = data;
  }

  public StatusString(int status, String explanation, String data) {
    super(status, explanation);

    this.data = data;
  }

  public StatusString(String data) throws Exception {
    this();

    this.data = data;
  }

  public StatusString(TokenParser p) throws Exception {
    this();
    fromString(p);
  }

  /**
	 * @return
	 */
  public String getData () {
    return data;
  }

  /**
	 * @param data
	 */
  public void setData (String data) {
    this.data = data;
  }

  @Override
public String toString_extension () {
    return CASAUtil.toQuotedString (data);
  }

  @Override
public void fromString_extension (TokenParser parser) throws Exception {
    data = parser.getNextToken ();
  }
}