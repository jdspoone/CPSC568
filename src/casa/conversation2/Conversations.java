package casa.conversation2;

import casa.util.Trace;

import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

class Conversations extends ConcurrentSkipListMap<String,Conversation> {
	
	private static final long serialVersionUID = -6293959966584006173L;

	@Override
	synchronized public Conversation put(String name, Conversation conv) {
		assert conv.isTemplate();
		return super.put(name, conv);
	}

	@Override
	synchronized public Conversation get(Object key) {
		assert key instanceof String;
		return super.get(key);
	}

	/**
	 * Renames a Conversation instance by removing it an putting it back.  This is necessary because it if
	 * conversation's internal name changes, it's index in the container must also change to match.
	 * This method works with {@link Conversation#setName(String)} so that you can call either this 
	 * method or {@link Conversation#setName(String)} to accomplish the same objective.
	 * @param oldName The original name of the conversation.
	 * @param newName The new name of the conversation.
	 * @return
	 */
	synchronized public Conversation rename(String oldName, String newName) {
		Conversation old = remove(oldName);
		if (old==null) {
			Trace.log("error", "Conversation.knownConversations.rename: Cannot rename missing conversation named "+oldName);
			return null;
		}
		if (!old.getName().equals(newName))
			old.setName(newName);
		return put(newName, old);
	}
}