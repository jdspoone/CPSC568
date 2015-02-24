/**
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. 
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  
 * The  Knowledge Science Group makes no representations about the suitability
 * of  this software for any purpose.  It is provided "as is" without express
 * or implied warranty.</p>
 * <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
package casa.actions.rk;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import casa.Status;
import casa.StatusObject;
import casa.abcl.Lisp;
import casa.exceptions.IllegalOperationException;

import org.armedbear.lisp.Fixnum;
import org.junit.Test;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class TestActions {

	/**
	 * 
	 */
	public TestActions() {
	}

	@Test
	public final void LispAction1() {
		//test a setq
		Action a = new LispAction("assignSumToX", 
				"(setq x (+ p1 p2))",
				new Param("p1", Integer.class, new Integer(0)),
				new Param("p2", Integer.class, new Integer(0)));
		assertTrue(executeAndReturnInt(a, 1, 2) == 3);
		
		//test that the above setq actually affected the Lisp environment
		Action b = new LispAction("X", 
				"x");
		assertTrue(executeAndReturnInt(b) == 3);

	}

	//helper method: the actions should return a integer value (Fixnum) as a result of a Lisp expression
	protected int executeAndReturnInt(Action a, Object...params) {
		Status stat = null;
		try {
			stat = a.execute(params);
		} catch (ClassCastException e) {
			e.printStackTrace();
		} catch (IllegalOperationException e) {
			e.printStackTrace();
		}
		return getIntFromStatus(stat);
	}
	
	protected int getIntFromStatus(Status stat) {
		if (stat instanceof StatusObject) {
			Object obj = ((StatusObject<?>)stat).getObject();
			if (obj instanceof Fixnum) {
				return ((Fixnum)obj).value;
			}
		}
		return Integer.MIN_VALUE;
	}
	
	@Test
	public final void Seq1() {
		//test a sequence of dependent computations
		Action a = new Seq(
				new LispAction("assignSumToX", 
						"(setq x (+ 2 3))"),
				new LispAction("assignXplusP1", 
						"(setq y (+ x 4))")
				);
		assertTrue(executeAndReturnInt(a) == 9);
	}

	@Test
	public final void Seq2() {
		//test a sequence of dependent computations
		Action a = new Seq(
				new LispAction("assignSumToX", 
						"(setq x (+ 2 3))"),
				new LispAction("assignXplusP1", 
						"(setq y (+ x 4))")
				);
		String persist = a.toString();
		System.out.println(persist);
		assertTrue(getIntFromStatus(Lisp.abclEval(null, null, null, persist, null)) == 9);
	}

	@Test
	public final void Choice_One1() {
		//test a sequence of dependent computations
		Action a = new ActionChoice_One(
				new LispAction("zero", 
						"0"),
				new LispAction("one", 
						"1"),
				new LispAction("two", 
						"2")
				);
		boolean res[] = {false, false, false};
	  final int tries = 10;
		for (int i = tries; i>0 ;i--) 
			res[executeAndReturnInt(a)] = true;
		for (int i = 2; i>=0; i--)
			if (!res[i])
				fail("In "+tries+" tries, we didn't get a "+i);
	}

}
