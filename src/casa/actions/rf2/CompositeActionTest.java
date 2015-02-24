package casa.actions.rf2;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class CompositeActionTest {
	private static final int FREQUENCY_RUNS  = 5000; 
	private static final int COLLECTION_SIZE = 50; 
	private static       int TAGGED          = 0;
	private class TestAction implements Action {
		private int tag      = -1;
		private int executed = 0;
		@Override
		public void execute() {
			executed++;
			tag = TAGGED++;
		}
		public boolean wasExecuted() {
			return executed > 0;
		}
		public int getExecuted() {
			return executed;
		}
		public int getTag() {
			return tag;
		}
	}
	@Before
	public void initTest() {
		TAGGED = 0;
	}
	@Test(expected=NoSuchElementException.class)
	public void testCompositeIteratorHasNext() {
		List<Action> actions = new ArrayList<Action>();
		TestAction       a       = new TestAction();
		TestAction       b       = new TestAction();
		actions.add( a );
		actions.add( b );

		CompositeAction composite = new CompositeAction( actions );
		Iterator<Action>        iterator  = composite.iterator();

		assertTrue  ( "",    iterator.hasNext() ); // has a
		assertEquals( "", a, iterator.next() );
		assertTrue  ( "",    iterator.hasNext() ); // has b
		assertEquals( "", b, iterator.next() );
		assertFalse ( "",    iterator.hasNext() ); // has none
		iterator.next();
		fail( "where is my exception?" );
	}
	@Test
	public void testArrayListRunsAllSequentially() {
		List<Action> actions = new ArrayList<Action>();
		for (int i = 0; i < COLLECTION_SIZE; i++) {
			actions.add( new TestAction() );
		}
		CompositeAction composite = new CompositeAction( actions );
		composite.execute();

		for (int i = 0; i < actions.size(); i++) {
			TestAction action = (TestAction)actions.get( i );
			assertEquals( "", i, action.getTag()      ); // it was executed in list order
			assertEquals( "", 1, action.getExecuted() ); // it was executed 1 time
		}
	}
	@Test
	public void testHashSetRunsAll() {
		Set<Action> actions = new HashSet<Action>();
		for (int i = 0; i < COLLECTION_SIZE; i++) {
			actions.add( new TestAction() );
		}
		CompositeAction composite = new CompositeAction( actions );
		composite.execute();
		
		for (Action action : actions) {
			assertEquals( "", 1, ((TestAction)action).getExecuted() ); // it was executed 1 time
		}
	}
	@Test
	public void testChoiceOne() {
		List<Action> actions = new ArrayList<Action>();
		for (int i = 0; i < COLLECTION_SIZE; i++) {
			actions.add( new TestAction() );
		}
		CompositeAction composite = new ActionChoice_One( actions );
		composite.execute();

		int counter = 0;
		for (Action action : actions) {
			if (((TestAction)action).wasExecuted()) {
				assertEquals( "", 1, ((TestAction)action).getExecuted() ); // it was executed 1 time
				counter++;
			}
		}
		assertEquals( "", 1, counter ); // only 1 was executed
	}
	@Test
	public void testChoiceOneOrMore() {
		Queue<Action> actions = new LinkedList<Action>();
		for (int i = 0; i < COLLECTION_SIZE; i++) {
			actions.add( new TestAction() );
		}
		CompositeAction composite = new ActionChoice_OneOrMore( actions );
		composite.execute();

		int counter = 0;
		for (Action action : actions) {
			if (((TestAction)action).wasExecuted()) {
				assertEquals( "", 1, ((TestAction)action).getExecuted() ); // it was executed 1 time
				counter++;
			}
		}
		assertTrue( "", counter > 0 ); // 1+ were executed
	}
	@Test
	public void testChoiceZeroOrMore() {
		Set<Action> actions = new HashSet<Action>();
		for (int i = 0; i < COLLECTION_SIZE; i++) {
			actions.add( new TestAction() );
		}
		CompositeAction composite = new ActionChoice_ZeroOrMore( actions );
		composite.execute();

		int counter = 0;
		for (Action action : actions) {
			if (((TestAction)action).wasExecuted()) {
				assertEquals( "", 1, ((TestAction)action).getExecuted() ); // it was executed 1 time
				counter++;
			}
		}
		assertTrue( "", counter > -1 ); // 0+ were executed
	}
	@Test
	public void testChoiceZeroOrOne() {
		List<Action> actions = new LinkedList<Action>();
		for (int i = 0; i < COLLECTION_SIZE; i++) {
			actions.add( new TestAction() );
		}
		CompositeAction composite = new ActionChoice_ZeroOrOne( actions );
		composite.execute();

		int counter = 0;
		for (Action action : actions) {
			if (((TestAction)action).wasExecuted()) {
				assertEquals( "", 1, ((TestAction)action).getExecuted() ); // it was executed 1 time
				counter++;
			}
		}
		assertTrue( "", counter == 0 || counter == 1 ); // one or none were executed
	}
	// testing on empty
	// are any exceptions thrown?
//	@Test
//	public void testArrayList_WhenEmpty() {
//		List<Action>            actions   = new Array<TestAction>();
//		CompositeAction composite = new CompositeAction( actions );
//		composite.execute();
//	}
	@Test
	public void testHashSet_WhenEmpty() {
		Set<Action>             actions   = new HashSet<Action>();
		CompositeAction composite = new CompositeAction( actions );
		composite.execute();
	}
	@Test
	public void testChoiceOne_WhenEmpty() {
		List<Action>            actions   = new ArrayList<Action>();
		CompositeAction composite = new ActionChoice_One( actions );
		composite.execute();
	}
	@Test
	public void testChoiceOneOrMore_WhenEmpty() {
		Queue<Action>           actions   = new LinkedList<Action>();
		CompositeAction composite = new ActionChoice_OneOrMore( actions );
		composite.execute();
	}
	@Test
	public void testChoiceZeroOrMore_WhenEmpty() {
		Set<Action>             actions   = new HashSet<Action>();
		CompositeAction composite = new ActionChoice_ZeroOrMore( actions );
		composite.execute();
	}
	@Test
	public void testChoiceZeroOrOne_WhenEmpty() {
		List<Action>            actions   = new LinkedList<Action>();
		CompositeAction composite = new ActionChoice_ZeroOrOne( actions );
		composite.execute();
	}	
	// frequency tests:
	// what are the odds that an action in a choice is never executed?
	@Test
	public void testChoiceOne_RunSeveralTimes() {
		List<Action> actions = new ArrayList<Action>();
		for (int i = 0; i < COLLECTION_SIZE; i++) {
			actions.add( new TestAction() );
		}
		CompositeAction composite = new ActionChoice_One( actions );
		for (int i = 0; i < FREQUENCY_RUNS; i++) {
			composite.execute();
		}
		// were all actions executed?
		for (Action action : actions) {
			assertTrue( "action not executed", ((TestAction)action).wasExecuted() ); // executed at least 1 time
		}
	}
	@Test
	public void testChoiceOneOrMore_RunSeveralTimes() {
		Queue<Action> actions = new LinkedList<Action>();
		for (int i = 0; i < COLLECTION_SIZE; i++) {
			actions.add( new TestAction() );
		}
		CompositeAction composite = new ActionChoice_OneOrMore( actions );
		for (int i = 0; i < FREQUENCY_RUNS; i++) {
			composite.execute();
		}
		// were all actions executed?
		for (Action action : actions) {
			assertTrue( "action not executed", ((TestAction)action).wasExecuted() ); // executed at least 1 time
		}
	}
	@Test
	public void testChoiceZeroOrMore_RunSeveralTimes() {
		Set<Action> actions = new HashSet<Action>();
		for (int i = 0; i < COLLECTION_SIZE; i++) {
			actions.add( new TestAction() );
		}
		CompositeAction composite = new ActionChoice_ZeroOrMore( actions );
		for (int i = 0; i < FREQUENCY_RUNS; i++) {
			composite.execute();
		}
		// were all actions executed?
		for (Action action : actions) {
			assertTrue( "action not executed", ((TestAction)action).wasExecuted() ); // executed at least 1 time
		}
	}
	@Test
	public void testChoiceZeroOrOne_RunSeveralTimes() {
		List<Action> actions = new LinkedList<Action>();
		for (int i = 0; i < COLLECTION_SIZE; i++) {
			actions.add( new TestAction() );
		}
		CompositeAction composite = new ActionChoice_ZeroOrOne( actions );
		for (int i = 0; i < FREQUENCY_RUNS; i++) {
			composite.execute();
		}
		// were all actions executed?
		for (Action action : actions) {
			assertTrue( "action not executed", ((TestAction)action).wasExecuted() ); // executed at least 1 time
		}
	}
}
