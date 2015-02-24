package casa.util;

import casa.agentCom.URLDescriptor;
import casa.interfaces.TransientAgentInterface;

import java.util.Collection;
import java.util.Set;
import java.util.TreeMap;


/**
 * A singleton class in order to have only one agent look-up table in the system.
 * Each entry in the table will consist of the agent's URL and its handle.   The handle
 * will be the agent's unique ID provided by the VM.
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
 * @version 0.9
 * @author <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @author Gabriel Becerra
 */
public class AgentLookUpTable {
  /**
   * Serves as an Agent Look-up table.
   */
  private static TreeMap<URLDescriptor, TransientAgentInterface> lookUpTable = new TreeMap<URLDescriptor, TransientAgentInterface> ();

  /**
   * Private constructor in order disallow any instance of this class.
   */
  private AgentLookUpTable() {}

  public static synchronized Set<URLDescriptor> keys() {
    return lookUpTable.keySet();
  }
  public static synchronized Set<URLDescriptor> keySet() {
    return lookUpTable.keySet();
  }
  public static boolean containsValue(TransientAgentInterface value) {
    return lookUpTable.containsValue(value);
  }
  public static synchronized TransientAgentInterface remove(URLDescriptor key) {
    return lookUpTable.remove(key);
  }
  public static synchronized boolean containsKey(URLDescriptor key) {
    return lookUpTable.containsKey(key);
  }
  public static synchronized TransientAgentInterface put(URLDescriptor key, TransientAgentInterface value) {
    return lookUpTable.put(key, value);
  }
  public static synchronized TransientAgentInterface get(URLDescriptor key) {
    return lookUpTable.get(key);
  }
  public static Collection<TransientAgentInterface> values() {
    return lookUpTable.values();
  }
  public static synchronized int size () {
    return lookUpTable.size();
  }
  public static URLDescriptor findByName(String name) {
  	for (URLDescriptor url: keySet()) {
  		if (name.equals(url.getFile()))
  			return url;
  	}
  	return null;
  }
}
