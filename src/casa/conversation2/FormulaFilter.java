package casa.conversation2;

import jade.semantics.lang.sl.grammar.Formula;

public abstract class FormulaFilter {
	public abstract boolean shouldNotify(Formula formula); 
}