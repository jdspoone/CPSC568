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
 * @author Baljeet Singh
 * @author <a href="http://kremer.cpsc.ucalgary.ca/">Rob Kremer</a>
 */
package casa.jade;

import casa.TransientAgent;
import casa.exceptions.IllegalOperationException;
import casa.ontology.owl2.OWLOntology;

import jade.semantics.kbase.QueryResult;
import jade.semantics.kbase.QueryResult.BoolWrapper;
import jade.semantics.kbase.filters.FilterKBaseImpl;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.Node;
import jade.semantics.lang.sl.tools.MatchResult;
import jade.semantics.lang.sl.tools.SL;
import jade.util.leap.ArrayList;
import jade.util.leap.Set;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 *This filter applies when a "formula" is queried from the belief Base.
 */
public class OntologyFilter extends CasaQueryFilter {

  /**
   * Pattern that must match to apply the filter
   */
	private Formula pattern0;

	private Formula pattern1;

	private Formula pattern2;

	private Formula pattern3;

	private Formula patternB0;

	private Formula patternB1;

	private Formula patternB2;

	private Formula patternB3;

	/**
	 * A simple enum to identify the various types of predicates that we handle.
	 */
	enum PredType {
		SIMPLE_ID, ZEROARY_PRED, UNARY_PRED, BINARY_PRED
	};

	PredType predType;

	/**
	 * A simple class to store the data about a predicate
	 */
	class Predicate {
		PredType type;

		Node function, first, second;

		public Predicate(PredType type, Node function, Node first, Node second) {
			assert type != null && function != null;
			this.type = type;
			this.function = function;
			switch (type) {
			case SIMPLE_ID:
			case ZEROARY_PRED:
				assert first == null && second == null;
				break;
			case UNARY_PRED:
				assert first != null && second == null;
				this.first = first;
				break;
			case BINARY_PRED:
				assert first != null && second != null;
				this.first = first;
				this.second = second;
			}
		}
	}

	/**
	 * Creates a new OntologyFilter on the different patterns 
	 */
	public OntologyFilter() {
		super();
		patternB3 = SL.formula("(B ??agent (??fun ??phi ??psi))");
		patternB2 = SL.formula("(B ??agent (??fun ??phi))");
		patternB1 = SL.formula("(B ??agent (??fun))");
		patternB0 = SL.formula("(B ??agent ??fun)");
		pattern3 = SL.formula("(??fun ??phi ??psi)");
		pattern2 = SL.formula("(??fun ??phi)");
		pattern1 = SL.formula("(??fun)");
		pattern0 = SL.formula("??fun");
	}

	/**
	 * @param formula
	 * @param falsityReasons
	 * @param goOn
	 * @see jade.semantics.kbase.filters.KBQueryFilter#apply(jade.semantics.lang.sl.grammar.Formula,
	 *      jade.util.leap.ArrayList,
	 *      jade.semantics.kbase.QueryResult.BoolWrapper)
	 *  The formula is matched against the patterns and then an appropriate predicate is generated 
	 *  to apply the searching against.    
	 */
	@Override
	public QueryResult apply(Formula formula, ArrayList falsityReasons,
			BoolWrapper goOn) {
		QueryResult queryResult = ((FilterKBaseImpl) myKBase).getDecorated().query(
				formula, falsityReasons);
		if (queryResult != null)
			return queryResult;

		Predicate predicate = null;

		MatchResult applyResult = SL.match(patternB3, formula);
		if (applyResult != null) {
			predicate = new Predicate(PredType.BINARY_PRED, getNode(applyResult,
					"fun"), getNode(applyResult, "phi"), getNode(applyResult, "psi"));
		} else {
			applyResult = SL.match(patternB2, formula);
			if (applyResult != null) {
				predicate = new Predicate(PredType.UNARY_PRED, getNode(applyResult,
						"fun"), getNode(applyResult, "phi"), null);
			} else {
				applyResult = SL.match(patternB1, formula);
				if (applyResult != null) {
					predicate = new Predicate(PredType.ZEROARY_PRED, getNode(applyResult,
							"fun"), null, null);
				} else {
					applyResult = SL.match(patternB0, formula);
					if (applyResult != null) {
						predicate = new Predicate(PredType.SIMPLE_ID, getNode(applyResult,
								"fun"), null, null);
					} else {
						applyResult = SL.match(pattern3, formula);
						if (applyResult != null) {
							predicate = new Predicate(PredType.BINARY_PRED, getNode(
									applyResult, "fun"), getNode(applyResult, "phi"), getNode(
									applyResult, "psi"));
						} else {
							applyResult = SL.match(pattern2, formula);
							if (applyResult != null) {
								predicate = new Predicate(PredType.UNARY_PRED, getNode(
										applyResult, "fun"), getNode(applyResult, "phi"), null);
							} else {
								applyResult = SL.match(pattern1, formula);
								if (applyResult == null) {
									predicate = new Predicate(PredType.ZEROARY_PRED, getNode(
											applyResult, "fun"), null, null);
								} else {
									applyResult = SL.match(pattern0, formula);
									if (applyResult != null) {
										predicate = new Predicate(PredType.SIMPLE_ID, getNode(
												applyResult, "fun"), null, null);
									}
								}
							}
						}
					}
				}
			}
		}
		if (predicate != null) {
			/*If predicate is generated meaning formula matched against an pattern
			 * then predicate is passed to the searchOntology method for further searching.
			*/
			queryResult = searchOntology(predicate);
		}
		return queryResult;
	}

	/* (non-Javadoc)
	 * @see
	 * jade.semantics.kbase.filters.KBQueryFilter#getObserverTriggerPatterns(jade
	 * .semantics.lang.sl.grammar.Formula, jade.util.leap.Set)
	 */
	@Override
	public boolean getObserverTriggerPatterns(Formula formula, Set set) {
		return false;
	}

	/**
	 * @param agent
	 * @param x
	 * @return an set containing all the children of x and x itself
	 */
	public java.util.Set<String> recursiveChild(TransientAgent agent, String x) {
		java.util.Set<String> SI = new HashSet<String>();
		SI.add(x);
		java.util.Set<String> child;
		try {
			child = agent.getOntology().isChild(x);
			for (String c : child) {
				//Add until the bottom of ontology is reached
				if (!c.equals("Nothing")) {
					SI.add(c);
				}
			}
		} catch (IllegalOperationException e) {
			agent.println("error", "OntologyFilter.recursiveChild:", e);
		}
		return SI;
	}

	/**
	 * @param agent
	 * @param x
	 * @return an set containing all the parents of x and x itself
	 */
	public java.util.Set<String> recursiveParent(TransientAgent agent, String x) {
		java.util.Set<String> SI= new HashSet<String>();
		SI.add(x);
		java.util.Set<String> parents;
		try {
			parents = agent.getOntology().isParent(x);
			for (String p : parents) {
				//Add until top of the ontology is reached
				if (!p.equals("Thing")) {
					SI.add(p);
				}
			}
		} catch (IllegalOperationException e) {
			agent.println("error", "OntologyFilter.recursiveParent:", e);
		}
		return SI;
	}

	/**
	 * This function generated all possible queries form an query using 
	 * agent's ontology and outputs the QueryResult
	 * @param predicate
	 */
	public QueryResult searchOntology(Predicate predicate) {
		assert predicate != null;
		TransientAgent agent = ((CasaKB) myKBase).agent;
		assert agent != null;
    
		List<java.util.Set<String>> s = new java.util.ArrayList<java.util.Set<String>>();
		String expr = "";
		String[] identifiers = null;
		switch (predicate.type) {
		case BINARY_PRED:
			identifiers = new String[3];
			identifiers[0] = predicate.function.toString();
			identifiers[1] = predicate.first.toString();
			identifiers[2] = predicate.second.toString();
			expr = identifiers[0] + " " + identifiers[1] + " " + identifiers[2];
			break;
		case UNARY_PRED:
			identifiers = new String[2];
			identifiers[0] = predicate.function.toString();
			identifiers[1] = predicate.first.toString();
			expr = identifiers[0] + " " + identifiers[1];
			break;
		case ZEROARY_PRED:
		case SIMPLE_ID:
			identifiers = new String[1];
			identifiers[0] = predicate.function.toString();
			expr = identifiers[0];
		}

		if (predicate.type == PredType.SIMPLE_ID) {
			OWLOntology ont = (OWLOntology) agent.getOntology();
			for (String i : identifiers) {
				try {
					if (ont.isType(i)) {
						java.util.Set<String> A = new HashSet<String>();
						A = recursiveParent(agent, i);
						s.add(A);
					}
				} catch (IllegalOperationException e) {
					agent.println("error", "OntologyFilter.searchOntology:", e);
				}
			}
		} else {
			int arg = 0;
			OWLOntology ont = (OWLOntology) agent.getOntology();
			for (String i : identifiers) {
				try {
					if (ont.isType(i)) {
						java.util.Set<String> A = new HashSet<String>();
						if (arg == 0) {
							//Reverse subsumption
							A = recursiveChild(agent, i);
						} else {
							//Forward subsumption
							A = recursiveParent(agent, i);
						}
						arg++;
						s.add(A);
					}
				} catch (IllegalOperationException e) {
					agent.println("error", "OntologyFilter.searchOntology:", e);
				}
			}
		}
		if (s.size() != 0) {
			//Generate all  possible queries
			java.util.Set<String> result1 = recursivePermutations(s);
			//remove original query
			result1.remove(expr);
			Iterator<String> iterator = result1.iterator();
			while (iterator.hasNext()) {
				String val = "";
				if (predicate.type == PredType.SIMPLE_ID) {
					val = (String) iterator.next();
				} else {
					val = "(" + (String) iterator.next() + ")";
				}
				try {
					QueryResult queryResult = agent.query(val);
					if (queryResult != null) {
						return queryResult;
					}
				} catch (ParseException e) {
					agent.println("error", "OntologyFilter.searchOntology:", e);
				}
			}
		}
		return null;
	}

	/**
	 * @param sets
	 * @return The set containing all the queries for the identifiers passed
	 */
	public java.util.Set<String> recursivePermutations(
			List<java.util.Set<String>> sets) {
		if (sets.size() == 1) {
			java.util.Set<String> si = sets.get(0);
			return si;
		}
		if (sets.size() == 2) {
			return getPermutations(sets.get(0), sets.get(1));
		} else {
			return getPermutations(sets.get(0),
					recursivePermutations(sets.subList(1, sets.size())));
		}
	}

	/**
	 * @param a
	 * @param b
	 * @return an set containing all possible queries for two sets of identifiers
	 */
	private java.util.Set<String> getPermutations(java.util.Set<String> a,
			java.util.Set<String> b) {
		java.util.Set<String> si = new HashSet<String>();
		for (String i : a) {
			for (String j : b) {
				String x = i + " " + j;
				si.add(x);
			}
		}
		return si;
	}

}
