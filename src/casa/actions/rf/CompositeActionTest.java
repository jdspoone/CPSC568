package casa.actions.rf;
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
		List<TestAction> actions = new ArrayList<TestAction>();
		TestAction       a       = new TestAction();
		TestAction       b       = new TestAction();
		actions.add( a );
		actions.add( b );

		CompositeAction<TestAction> composite = new CompositeAction<TestAction>( actions );
		Iterator<TestAction>        iterator  = composite.iterator();

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
		List<TestAction> actions = new ArrayList<TestAction>();
		for (int i = 0; i < COLLECTION_SIZE; i++) {
			actions.add( new TestAction() );
		}
		CompositeAction<TestAction> composite = new CompositeAction<TestAction>( actions );
		composite.execute();

		for (int i = 0; i < actions.size(); i++) {
			TestAction action = actions.get( i );
			assertEquals( "", i, action.getTag()      ); // it was executed in list order
			assertEquals( "", 1, action.getExecuted() ); // it was executed 1 time
		}
	}
	@Test
	public void testHashSetRunsAll() {
		Set<TestAction> actions = new HashSet<TestAction>();
		for (int i = 0; i < COLLECTION_SIZE; i++) {
			actions.add( new TestAction() );
		}
		CompositeAction<TestAction> composite = new CompositeAction<TestAction>( actions );
		composite.execute();
		
		for (TestAction action : actions) {
			assertEquals( "", 1, action.getExecuted() ); // it was executed 1 time
		}
	}
	@Test
	public void testChoiceOne() {
		List<TestAction> actions = new ArrayList<TestAction>();
		for (int i = 0; i < COLLECTION_SIZE; i++) {
			actions.add( new TestAction() );
		}
		CompositeAction<TestAction> composite = new ActionChoice_One<TestAction>( actions );
		composite.execute();

		int counter = 0;
		for (TestAction action : actions) {
			if (action.wasExecuted()) {
				assertEquals( "", 1, action.getExecuted() ); // it was executed 1 time
				counter++;
			}
		}
		assertEquals( "", 1, counter ); // only 1 was executed
	}
	@Test
	public void testChoiceOneOrMore() {
		Queue<TestAction> actions = new LinkedList<TestAction>();
		for (int i = 0; i < COLLECTION_SIZE; i++) {
			actions.add( new TestAction() );
		}
		CompositeAction<TestAction> composite = new ActionChoice_OneOrMore<TestAction>( actions );
		composite.execute();

		int counter = 0;
		for (TestAction action : actions) {
			if (action.wasExecuted()) {
				assertEquals( "", 1, action.getExecuted() ); // it was executed 1 time
				counter++;
			}
		}
		assertTrue( "", counter > 0 ); // 1+ were executed
	}
	@Test
	public void testChoiceZeroOrMore() {
		Set<TestAction> actions = new HashSet<TestAction>();
		for (int i = 0; i < COLLECTION_SIZE; i++) {
			actions.add( new TestAction() );
		}
		CompositeAction<TestAction> composite = new ActionChoice_ZeroOrMore<TestAction>( actions );
		composite.execute();

		int counter = 0;
		for (TestAction action : actions) {
			if (action.wasExecuted()) {
				assertEquals( "", 1, action.getExecuted() ); // it was executed 1 time
				counter++;
			}
		}
		assertTrue( "", counter > -1 ); // 0+ were executed
	}
	@Test
	public void testChoiceZeroOrOne() {
		List<TestAction> actions = new LinkedList<TestAction>();
		for (int i = 0; i < COLLECTION_SIZE; i++) {
			actions.add( new TestAction() );
		}
		CompositeAction<TestAction> composite = new ActionChoice_ZeroOrOne<TestAction>( actions );
		composite.execute();

		int counter = 0;
		for (TestAction action : actions) {
			if (action.wasExecuted()) {
				assertEquals( "", 1, action.getExecuted() ); // it was executed 1 time
				counter++;
			}
		}
		assertTrue( "", counter == 0 || counter == 1 ); // one or none were executed
	}
	// testing on empty
	// are any exceptions thrown?
	@Test
	public void testArrayList_WhenEmpty() {
		List<TestAction>            actions   = new ArrayList<TestAction>();
		CompositeAction<TestAction> composite = new CompositeAction<TestAction>( actions );
		composite.execute();
	}
	@Test
	public void testHashSet_WhenEmpty() {
		Set<TestAction>             actions   = new HashSet<TestAction>();
		CompositeAction<TestAction> composite = new CompositeAction<TestAction>( actions );
		composite.execute();
	}
	@Test
	public void testChoiceOne_WhenEmpty() {
		List<TestAction>            actions   = new ArrayList<TestAction>();
		CompositeAction<TestAction> composite = new ActionChoice_One<TestAction>( actions );
		composite.execute();
	}
	@Test
	public void testChoiceOneOrMore_WhenEmpty() {
		Queue<TestAction>           actions   = new LinkedList<TestAction>();
		CompositeAction<TestAction> composite = new ActionChoice_OneOrMore<TestAction>( actions );
		composite.execute();
	}
	@Test
	public void testChoiceZeroOrMore_WhenEmpty() {
		Set<TestAction>             actions   = new HashSet<TestAction>();
		CompositeAction<TestAction> composite = new ActionChoice_ZeroOrMore<TestAction>( actions );
		composite.execute();
	}
	@Test
	public void testChoiceZeroOrOne_WhenEmpty() {
		List<TestAction>            actions   = new LinkedList<TestAction>();
		CompositeAction<TestAction> composite = new ActionChoice_ZeroOrOne<TestAction>( actions );
		composite.execute();
	}	
	// frequency tests:
	// what are the odds that an action in a choice is never executed?
	@Test
	public void testChoiceOne_RunSeveralTimes() {
		List<TestAction> actions = new ArrayList<TestAction>();
		for (int i = 0; i < COLLECTION_SIZE; i++) {
			actions.add( new TestAction() );
		}
		CompositeAction<TestAction> composite = new ActionChoice_One<TestAction>( actions );
		for (int i = 0; i < FREQUENCY_RUNS; i++) {
			composite.execute();
		}
		// were all actions executed?
		for (TestAction action : actions) {
			assertTrue( "action not executed", action.wasExecuted() ); // executed at least 1 time
		}
	}
	@Test
	public void testChoiceOneOrMore_RunSeveralTimes() {
		Queue<TestAction> actions = new LinkedList<TestAction>();
		for (int i = 0; i < COLLECTION_SIZE; i++) {
			actions.add( new TestAction() );
		}
		CompositeAction<TestAction> composite = new ActionChoice_OneOrMore<TestAction>( actions );
		for (int i = 0; i < FREQUENCY_RUNS; i++) {
			composite.execute();
		}
		// were all actions executed?
		for (TestAction action : actions) {
			assertTrue( "action not executed", action.wasExecuted() ); // executed at least 1 time
		}
	}
	@Test
	public void testChoiceZeroOrMore_RunSeveralTimes() {
		Set<TestAction> actions = new HashSet<TestAction>();
		for (int i = 0; i < COLLECTION_SIZE; i++) {
			actions.add( new TestAction() );
		}
		CompositeAction<TestAction> composite = new ActionChoice_ZeroOrMore<TestAction>( actions );
		for (int i = 0; i < FREQUENCY_RUNS; i++) {
			composite.execute();
		}
		// were all actions executed?
		for (TestAction action : actions) {
			assertTrue( "action not executed", action.wasExecuted() ); // executed at least 1 time
		}
	}
	@Test
	public void testChoiceZeroOrOne_RunSeveralTimes() {
		List<TestAction> actions = new LinkedList<TestAction>();
		for (int i = 0; i < COLLECTION_SIZE; i++) {
			actions.add( new TestAction() );
		}
		CompositeAction<TestAction> composite = new ActionChoice_ZeroOrOne<TestAction>( actions );
		for (int i = 0; i < FREQUENCY_RUNS; i++) {
			composite.execute();
		}
		// were all actions executed?
		for (TestAction action : actions) {
			assertTrue( "action not executed", action.wasExecuted() ); // executed at least 1 time
		}
	}
}
