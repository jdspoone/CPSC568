package casa.event;

/**
 * The EventObserver interface describes the method needed to inform an object
 * of the occurrence of an event. An event with which this observer is
 * registered will call {@link #notifyEventOccurred(String, Event, Object)} when the event
 * occurs.
 * 
 * @author Jason Heard
 * @version 0.9
 */
public interface EventObserver {
  /**
   * Indicates that an event of the given type has occurred.
   * 
   * @param type A string representing the type of event that has occurred.
   * @param event The event that {@link AbstractEvent#fireEvent() fired}.
   * @param instanceInfo An object that details the event occurrence.
   */
  public void notifyEventOccurred (String type, Event event, Object instanceInfo);
}
