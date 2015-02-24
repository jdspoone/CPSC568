package casa.jade;

import casa.conversation2.FormulaFilter;
import casa.util.Runnable1;

import java.text.ParseException;

import jade.semantics.kbase.observers.Observer;
import jade.semantics.lang.sl.grammar.BelieveNode;
import jade.semantics.lang.sl.grammar.Constant;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.IntegerConstant;
import jade.semantics.lang.sl.grammar.ListOfTerm;
import jade.semantics.lang.sl.grammar.Node;
import jade.semantics.lang.sl.grammar.PredicateNode;
import jade.semantics.lang.sl.grammar.RealConstantNode;
import jade.semantics.lang.sl.grammar.StringConstant;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.parser.SLParser;
import jade.semantics.lang.sl.tools.SL;

public class BeliefObserver extends Observer {
	CasaKB kbase;

	FormulaFilter filter = null;
	
	Runnable1<Formula, Object> runnable = null;
	
	/**
	 * Constructs and new BeliefObserver to monitor the knowledge base for changes
	 * to one or more formulas and execute <em>runnable</em> when its value changes,
	 * subject to <em>filter</em>. 
	 * @param kbase The KB to monitor.
	 * @param formula The formula (perhaps first of many) to monitor.
	 * @param filter A filter that can suppress the calling of <em>runnable.run(Formula)</em>
	 * @param runnable A type parameterized runnable to run when the formula changes (the return value is ignored). 
	 */
	public BeliefObserver(CasaKB kbase, Formula formula, FormulaFilter filter, Runnable1<Formula, Object> runnable) {
		super(kbase, formula);
		assert kbase!=null;
		assert formula!=null;
		assert runnable!=null;
		this.kbase = kbase;
		this.filter = filter;
		this.runnable = runnable;
	}

	/**
	 * Called whenever the formula changes. This implementation uses filter.shouldNotify() to 
	 * determine if the Runnable1.run() should be called. 
	 */
	@Override
	public boolean update(Formula formula) {
		if (formula==null) {
			return super.update(formula);
		}

		Term myself = kbase.getAgentName();
		formula = formula.instantiate("myself", myself);
		Formula f = new BelieveNode(myself, formula).getSimplifiedFormula();
		boolean ret = super.update(f);

		if (ret && (filter==null || filter.shouldNotify(formula))) {
			runnable.run(formula);
		}
		return ret;
	}

	public void setFilter(FormulaFilter f) {
		filter = f;
	}
	
	/**
	 * Creates a BeliefObserver and adds it to the <em>kbase</em> so that <em>runnable</em> is executed
	 * every time the <em>var</em> changes, subject to the <em>filter</em>.<p>
	 * To stop observing this change, call <em>kbase</em>.removeObserver() with the parameter being the returned BeliefObject from this method call.
	 * @param kbase The KB to monitor.
	 * @param var The variable to monitor; what's actually monitored is a unique predicate in the kb of the form "(<em>var value</em>)".
	 * @param filter A filter that can suppress the calling of <em>runnable.run(Formula)</em>
	 * @param runnable A type parameterized runnable to run when the formula changes (the return value is ignored, and the parameter of the run() method is the VALUE of the variable in KB). 
	 * @return a BeliefObserver object.
	 * @throws ParseException
	 */
	public static BeliefObserver onValueChange(CasaKB kbase, String var, FormulaFilter filter, final Runnable1<Object, Object> runnable) throws ParseException {
		Runnable1<Formula, Object> enclosingRunnable = new Runnable1<Formula,Object>(){
			@Override
			public Object run(Formula f) {
				if (f instanceof PredicateNode) {
					PredicateNode p = (PredicateNode)f;
					ListOfTerm terms = p.as_terms();
					Node t = terms.get(0);
					if (t instanceof Constant) {
						Constant c = (Constant)t;
						if (c instanceof IntegerConstant) {
							return runnable.run(((IntegerConstant)c).intValue());
						}
						if (c instanceof RealConstantNode) {
							return runnable.run(((IntegerConstant)c).realValue());
						}
						if (c instanceof StringConstant) {
							return runnable.run(((IntegerConstant)c).stringValue());
						}
					}
				}
				return true;
			}};
		Formula formula = SL.formula("("+var+" ??x)");
		BeliefObserver bo = new BeliefObserver(kbase, formula, filter, enclosingRunnable);
		kbase.addObserver(bo);
		bo.update(null); // necessary to initialize the observer
		return bo;
	}
	
}