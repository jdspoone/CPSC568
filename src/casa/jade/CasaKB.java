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
package casa.jade;

import casa.TransientAgent;
import casa.util.CASAUtil;

import jade.semantics.interpreter.Finder;
import jade.semantics.kbase.ArrayListKBaseImpl;
import jade.semantics.kbase.KBase;
import jade.semantics.kbase.QueryResult;
import jade.semantics.kbase.filters.FilterKBaseImpl;
import jade.semantics.kbase.filters.FiltersDefinition;
import jade.semantics.kbase.filters.KBAssertFilter;
import jade.semantics.kbase.filters.KBQueryFilter;
import jade.semantics.kbase.filters.std.DefaultFilterKBaseLoader;
import jade.semantics.kbase.observers.Observer;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.IdentifyingExpression;
import jade.semantics.lang.sl.grammar.ListOfTerm;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.parser.SLParser;
import jade.semantics.lang.sl.tools.MatchResult;
import jade.semantics.lang.sl.tools.SL;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.Set;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.TreeMap;
//import com.apple.crypto.provider.Debug;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class CasaKB extends FilterKBaseImpl implements Comparable<CasaKB>{

	TreeMap<String, CasaKB> OtherAgents = new TreeMap<String,CasaKB>();
	public TreeMap<String, CasaKB> getOtherAgentKBs() {
		return OtherAgents;
	}
	
	String name;
	TransientAgent agent;
	

	//	/**
	//	 * @param decorated
	//	 */
	//	public CasaKB(KBase decorated) {
	//		super(decorated);
	//		addFiltersDefinition(new CASAkbFiltersDefinition("agent-name"));
	//	}

	/**
	 * @param agent TODO
	 * @param decorated
	 * @param loader
	 */
	public CasaKB(String agentName, TransientAgent agent) {
		super(getDecorated(agentName), new DefaultFilterKBaseLoader());
		
		name = agentName;
		this.agent = agent;
		
		//add decorator to handle a unary predicate that can only have a single value explicitly.  This can
		//be used in a "iota" and "all" expressions.
		addFiltersDefinition(new SingleValueDefinition("agent-name"));

		//assert that we know all about the "agent-name" predicate (it's iota).
		String formula = "(= (iota ?x (agent-name ?x)) "+agentName+")";
		try {
			Formula form = SLParser.getParser().parseFormula(formula, true);//SL.formula(formula);
			assertFormula(form);
		}
		catch (Throwable e) {
			ParseException ex = new ParseException("TransientAgent.assert_(): malformed term '"+formula+"'", 0);
			ex.initCause(e);
			CASAUtil.log("error", "CasaKB.constructor", e, true);
		}
		
		//Add a filter to interpret formualas like "(B Alice ??X)" and put Alice's beliefs into her
		//local belief base copy.
		addFiltersDefinition(new CASAkbFiltersDefinition(name, this));
		
		//Add the ontology filter
		addKBQueryFilter(new OntologyFilter(),END);
	}
	
	private static KBase getDecorated(String agent) {
		Term result = (Term)jade.semantics.interpreter.Tools.AGENT_IDENTIFIER_PATTERN.getClone();            
		try {
			SL.set(result, "name", SL.word(agent));
			//    if (addresses != null) SL.set(result, "addresses", addresses);
			//    if (resolvers != null) SL.set(result, "resolvers", resolvers);
			SL.substituteMetaReferences(result);
			SL.removeOptionalParameter(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayListKBaseImpl(result);
	}

	@Override
	public int compareTo(CasaKB o) {
		return this.getAgentName().toString().compareTo(o.getAgentName().toString());
	}

	public synchronized void agentAssert(String agentName, Formula formula) {
		CasaKB kb = OtherAgents.get(agentName);
		if (kb==null) {
			kb = new CasaKB(agentName, agent);
			OtherAgents.put(agentName, kb);
		}
		kb.assertFormula(formula);
	}

	public synchronized QueryResult agentQuery(String agentName, Formula formula) {
		CasaKB kb = OtherAgents.get(agentName);
		if (kb==null) {
			kb = new CasaKB(agentName, agent);
			OtherAgents.put(agentName, kb);
		}
		QueryResult qr = kb.query(formula);
		return qr;
//		if (qr==null)
//			return null;
//		MatchResult mr = new MatchResult();
//		ArrayList results = qr.getResults();
//		if (results==null)
//			return null;
//		for (int n=results.size(), index=0; index<n; index++) {
//			mr.add(qr.getResult(index));
//		}
//	  return mr;
	}

	public synchronized MatchResult agentQueryRef(String agentName, IdentifyingExpression exp) {
		CasaKB kb = OtherAgents.get(agentName);
		if (kb==null) {
			kb = new CasaKB(agentName, agent);
			OtherAgents.put(agentName, kb);
		}
		ListOfTerm terms = kb.queryRef(exp);

		MatchResult mr = new MatchResult();
		for (int n=terms.size(), index=0; index<n; index++) {
			mr.add(terms.get(index));
		}
	  return mr;
	}

//	public synchronized String toString(int indent) {
//		StringBuilder b = new StringBuilder();
//		CASAUtil.pad(b, indent).append("KB ").append(name).append(": ").append(super.toStrings()).append('\n');
//		for (CasaKB kb: OtherAgents.values()) 
//			b.append(kb.toString(indent+2));
//		return b.toString();
//	}

	@Override
	public synchronized String toString() {
		return toString(0);
	}

	public synchronized String toString(int indent) {
		StringBuilder b = new StringBuilder();
		CASAUtil.pad(b, indent).append("KB ").append(name).append(": ").append('\n');
		CASAUtil.pad(b, indent).append("QUERY FILTERS:\n");
		b.append(toStringQueryFilters(indent));
		CASAUtil.pad(b, indent).append("ASSERT FILTERS:\n");
		b.append(toStringAssertFilters(indent));
		CASAUtil.pad(b, indent).append("FACTS:\n");
		b.append(toStringFacts(indent));
		for (CasaKB kb: OtherAgents.values()) 
			b.append(kb.toString(indent+2));
		return b.toString();
	}
	
	public synchronized String toStringFacts() {return toStringFacts(0);}

	protected synchronized String toStringFacts(int indent) {
		StringBuilder b = new StringBuilder();
		for (Iterator i = decorated.toStrings().iterator(); i.hasNext();) {
			CASAUtil.pad(b, indent).append(i.next()).append('\n');
		}
		return b.toString();
	}
	
	public synchronized String toStringQueryFilters() {return toStringQueryFilters(0);}

	protected synchronized String toStringQueryFilters(int indent) {
		return toStringHelper(queryFilterList.iterator(),indent);
	}
	
	public synchronized String toStringAssertFilters() {return toStringAssertFilters(0);}

	protected synchronized String toStringAssertFilters(int indent) {
		return toStringHelper(assertFilterList.iterator(),indent);
	}
	
	private String toStringHelper(Iterator i, int indent) {
		StringBuilder b = new StringBuilder();
		for (; i.hasNext();) {
			Object o = i.next();
			if (o!=null) {
				Class<?> cls = o.getClass();
				String name = cls.getName();
				CASAUtil.pad(b, indent).append(name).append('\n');
				for (; cls!=null; cls=cls.getEnclosingClass()) { // many of the decorators are defined as inner classes so search outside the classes
					toStringHelperFields(b, cls, o, indent+2);
					try {
		        Field field = cls.getDeclaredField("this$0");
		        field.setAccessible(true);
		        o = field.get(o);
					} catch (NoSuchFieldException e) {
					} catch (Throwable e) {
						CASAUtil.log("error", "CasaKB.toStringHelper()", e, true);
					}
				}
			}
		}
		return b.toString();
	}
	
	
	private void toStringHelperFields(StringBuilder b, Class<?> cls, Object o, int indent) {
		Field[] fields = cls.getDeclaredFields();
		for (Field f: fields) {
			if (Formula.class.isAssignableFrom(f.getType())) {
				try {
					boolean accessible = f.isAccessible();
					if (!accessible) f.setAccessible(true);
					Object obj = f.get(o);
					if (obj!=null)
						CASAUtil.pad(b, indent).append(obj).append('\n');
					if (!accessible) f.setAccessible(false);
				} catch (Throwable e) {
					CASAUtil.log("error", "CasaKB.toStringHelperFields()", e, true);
				}
			}
		}
	}


	/* ***************************************************************/
	/* All the rest of these are just here to synchronize the class */
	/* ***************************************************************/

	@Override
	public synchronized void assertFormula(Formula formula) {
		super.assertFormula(formula);
	}

	@Override
	public synchronized QueryResult query(Formula formula, ArrayList reasons) {
		return super.query(formula, reasons);
	}

	@Override
	public synchronized QueryResult query(Formula formula) {
		return super.query(formula);
	}

	@Override
	public synchronized void addFiltersDefinition(FiltersDefinition filtersDefinition) {
		super.addFiltersDefinition(filtersDefinition);
	}

	@Override
	public synchronized void addKBAssertFilter(KBAssertFilter filter) {
		super.addKBAssertFilter(filter);
	}

	@Override
	public synchronized void addKBAssertFilter(KBAssertFilter filter, int index) {
		super.addKBAssertFilter(filter, index);
	}

	@Override
	public synchronized void addKBQueryFilter(KBQueryFilter filter) {
		super.addKBQueryFilter(filter);
	}

	@Override
	public synchronized void addKBQueryFilter(KBQueryFilter filter, int index) {
		super.addKBQueryFilter(filter, index);
	}

	@Override
	public synchronized void removeKBAssertFilter(Finder filterIdentifier) {
		super.removeKBAssertFilter(filterIdentifier);
	}

	@Override
	public synchronized void removeKBQueryFilter(Finder filterIdentifier) {
		super.removeKBQueryFilter(filterIdentifier);
	}

	@Override
	public synchronized void addObserver(Observer obs) {
		super.addObserver(obs);
	}

	@Override
	public synchronized void getObserverTriggerPatterns(Formula formula, Set result) {
		super.getObserverTriggerPatterns(formula, result);
	}

	@Override
	public synchronized ArrayList toStrings() {
		return super.toStrings();
	}

	@Override
	public synchronized void viewFilterQuery() {
		super.viewFilterQuery();
	}

	@Override
	public synchronized KBase getDecorated() {
		return super.getDecorated();
	}

	@Override
	public synchronized void addClosedPredicate(Formula pattern) {
		super.addClosedPredicate(pattern);
	}

	@Override
	public synchronized Term getAgentName() {
		return super.getAgentName();
	}

	@Override
	public synchronized KBase getWrappingKBase() {
		return super.getWrappingKBase();
	}

	@Override
	public synchronized boolean isClosed(Formula pattern, QueryResult values) {
		return super.isClosed(pattern, values);
	}

	@Override
	public synchronized ListOfTerm queryRef(IdentifyingExpression expression) {
		return super.queryRef(expression);
	}

	@Override
	public synchronized Term queryRef(IdentifyingExpression expression,
			ArrayList falsityReasons) {
		return super.queryRef(expression, falsityReasons);
	}

	@Override
	public synchronized Term queryRefSingleTerm(IdentifyingExpression expression) {
		return super.queryRefSingleTerm(expression);
	}

	@Override
	public synchronized Term eval(Term expression) {
		return super.eval(expression);
	}

	@Override
	public synchronized void removeClosedPredicate(Finder finder) {
		super.removeClosedPredicate(finder);
	}

	@Override
	public synchronized void removeFormula(Finder finder) {
		super.removeFormula(finder);
	}

	@Override
	public synchronized void removeObserver(Finder finder) {
		super.removeObserver(finder);
	}

	@Override
	public synchronized void removeObserver(Observer obs) {
		super.removeObserver(obs);
	}

	@Override
	public synchronized void retractFormula(Formula formula) {
		super.retractFormula(formula);
	}

	@Override
	public synchronized void setAgentName(Term agent) {
		super.setAgentName(agent);
	}

	@Override
	public synchronized void setWrappingKBase(KBase kbase) {
		super.setWrappingKBase(kbase);
	}

	@Override
	public synchronized void updateObservers(Formula formula) {
		super.updateObservers(formula);
	}
}
