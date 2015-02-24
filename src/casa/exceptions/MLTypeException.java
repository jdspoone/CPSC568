package casa.exceptions;


/**
 * Title:         CASA
 * Description:   This exception is thrown when an attempt is made to set the markup language (a field
 *                within IPKnowledgeModule) to anything other than ML.XML or ML.KQML.
 *
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
 *                @see IPKnowledgeModule
 * @author <a href="laf@cpsc.ucalgary.ca">Chad La Fournie</a>
 *
 */
public class MLTypeException extends Exception {

  public MLTypeException(){
    super();
  }

  public MLTypeException( String s ){
    super( s );
  }
}