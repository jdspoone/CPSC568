package casa.interfaces;

import casa.Status;
import casa.util.Trace;

public interface AgentPrintInterface {
  /**
   * Debugging or error method: Uses the {@link Trace} object to log the
   * string if appropriate (ie: the traceTAg matches a tag that's turned on
   * in the Trace object.
   * @param traceTag The <em>txt</em> String will be logged if the traceTag maches a tag that's turned on in the Trace object.
   * @param txt The String to be logged
   * @return TODO
   */
  public String println(String traceTag, String txt);

  /**
       * Same as {@link #println(String, String)} but appends ex.toString() and prints
   * a stack trace after.
   * @param traceTag The <em>txt</em> String will be logged if the traceTag maches a tag that's turned on in the Trace object.
   * @param txt The String to be logged
   * @param ex An Exception object
   * @return TODO
   */
  public String println(String traceTag, String txt, Throwable ex);

  /**
   * Same as {@link #println(String, String)} but appends tempStatus.getExplanation().
   * @param traceTag The <em>txt</em> String will be logged if the traceTag maches a tag that's turned on in the Trace object.
   * @param txt The String to be logged
   * @param tempStatus A Status object
   * @return TODO
   */
  public String println(String traceTag, String txt, Status tempStatus);

  public boolean isLoggingTag (String tag);
}
