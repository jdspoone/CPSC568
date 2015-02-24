package casa.interfaces;

import casa.AdvertisementDescriptor;
import casa.Status;
import casa.StatusAdvertisementDescriptorList;

/**
 * <code>YellowPagesAgentInterface</code> is an extension of
 * <code>AgentInterface</code> that acts as an advertisement service for other
 * agents.  It adds methods for the incoming requests that a yellow pages agent
 * must handle as well as constants used in comunication with a yellow pages
 * agent.
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
 * @author Jason Heard
 */

public interface YellowPagesAgentInterface extends AgentInterface {

  /**
   * Adds the given advertisement to the list of current advertisements.
   *
   * @param advertisement The advertisement that should be added to the list of
   * current advertisements.
   * @return The <code>Status</code> of the advertise action:
   * <li>0 if it was successful with no warnings or errors, or</li>
   * <li>1 if the advertisement was previously in the list of current
   * advertisements.</li>
   */
  public Status advertise (AdvertisementDescriptor advertisement);

  /**
   * Removes the given advertisement from the list of current advertisements.
   *
   * @param advertisement The advertisement that should be removed from the
   * list of current advertisements.
   * @return The <code>Status</code> of the remove advertisement action:
   * <li>0 if it was successful with no warnings or errors, or</li>
   * <li>1 if the advertisement was not previously in the list of current
   * advertisements.</li>
   */
  public Status removeAdvertisement (AdvertisementDescriptor advertisement);

  /**
   * Retreives a list of advertisements that match the specified search
   * parameters.  The search parameters are encapsulated in a
   * <code>AdvertisementSearchInterface</code> object.  Returns a list of
   * <code>AdvertisementDescriptor</code>s (encapsulated in a
   * <code>StatusAdvertisementDescriptorList</code>) that match the specified
   * search parameters.
   *
   * @param searchParameters An object that will determine which of the
   * advertisements is a match.
   * @return A <code>Vector</code> of <code>AdvertisementDescriptor</code>s
   * (encapsulated in a <code>StatusAdvertisementDescriptorList</code>) that
   * match the specified search parameters.  The status will be:
   * <li>0 indicating the operation was successful.</li>
   */
  public StatusAdvertisementDescriptorList search (AdvertisementSearchInterface searchParameters);
}
