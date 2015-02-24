package casa.conversation2;

import casa.Status;
import casa.event.Event;
import casa.util.Runnable1;

public class Runnable1Action implements Action<Runnable1<Event, Status>>{
	Runnable1<Event, Status> runnable;

	@Override
	public void setAction(Runnable1<Event, Status> action) {
		runnable = action;
	}

	@Override
	public Runnable1<Event, Status> getAction() {
		return runnable;
	}

	@Override
	public Status execute(Event event) {
		// TODO Auto-generated method stub
		return runnable.run(event);
	}

}
