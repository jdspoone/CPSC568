/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
JSA - JADE Semantics Add-on is a framework to develop cognitive
agents in compliance with the FIPA-ACL formal specifications.

Copyright 2003-2014, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/
package jade.semantics.kbase.filters.std;

import jade.semantics.interpreter.Tools;
import jade.semantics.kbase.QueryResult;
import jade.semantics.kbase.filters.FilterKBase;
import jade.semantics.kbase.filters.KBQueryFilter;
import jade.semantics.lang.sl.grammar.BelieveNode;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.FunctionalTermParamNode;
import jade.semantics.lang.sl.grammar.ListOfParameter;
import jade.semantics.lang.sl.grammar.ListOfTerm;
import jade.semantics.lang.sl.grammar.MetaTermReferenceNode;
import jade.semantics.lang.sl.grammar.Node;
import jade.semantics.lang.sl.grammar.Parameter;
import jade.semantics.lang.sl.grammar.ParameterNode;
import jade.semantics.lang.sl.grammar.PredicateNode;
import jade.semantics.lang.sl.grammar.SymbolNode;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.tools.MatchResult;
import jade.semantics.lang.sl.tools.SL;
import jade.util.leap.ArrayList;
import jade.util.leap.Set;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Vector;

/**
 * 
 * @author Thierry Martinez - France Telecom
 *
 * TODO process the reasons in the apply method
 */
public class FunctionalTermTableImpl implements FunctionalTermTable {
	
	//---------------------------------------------------------------
	//                 PRIVATE FIELDS
	//---------------------------------------------------------------
	FilterKBase kbase;
	
	HashMap terms;
	long nlid;
	
	//---------------------------------------------------------------
	//                 PRIVATE METHODS
	//---------------------------------------------------------------
	
	/**
	 * @param id
	 * @return
	 */
	String localID(String id) {
		String[] idparts = id.split(":");
		if ( idparts.length == 2 ) {
			return idparts[1];
		}
		return id;
	}
	
	/**
	 *  Looks for terms matching the class name, the required fields and the local id, 
	 *  in the current table. If the lid is null, the method returns either 
	 *  a list that contains the lid found and for each lid the current values
	 *  of the fields or a null list if no term match the request. This list
	 *  is organised as follow : lid value1 value2 ... lid value1 value2 ...
	 *  If the lid is not null, the method does the same but only with the 
	 *  corresponding term if exists. The result is either a empty list, or 
	 *  a list containing all the required fields but not the lid.
	 *  
	 * @param classname the name of the functional term
	 * @param fields a array holding the name of all required fields
	 * @param lid the local id or null
	 * @return a list "lid v1 v2 ... lid v1 v2 ..." if the lid is unspecified
	 *         or "v1 v2 ..." if the lid is specified or null if the request
	 *         doesn't match any term stored in the table.
	 */
	ListOfTerm lookupFields(String classname, String[] fields, String lid)
	//--------------------------------------------
	{
		ListOfTerm result = null;
		if ( lid != null ) {
			// id is specified -> return a list which contains the only possible values or null list
			FT ft = (FT)terms.get(lid);
			if ( ft != null && ft.getClassName().equals(classname)) {
				for (int i=0; i<fields.length; i++) {
					Term fvalue = ft.getField(fields[i]);
					if ( fvalue != null ) {
						if ( result == null ) {
							result = new ListOfTerm();
						}
						result.add(fvalue);
					}
				}
				if ( result.size() != fields.length ) {
					result = null;
				}
			}	
		}
		else {
			// no id specified -> return a list which contains alternatively id and values or null list
			Object[] fts = terms.values().toArray();
			for (int i=0; i<fts.length; i++) {
				if ( ((FT)fts[i]).getClassName().equals(classname) ) {
					Vector vals = new Vector();
					for (int j=0; j<fields.length; j++) {
						Term fvalue = ((FT)fts[i]).getField(fields[j]);
						if ( fvalue != null ) {
							vals.add(fvalue);
						}
					}
					if ( vals.size() == fields.length ) {
						if ( result == null ) {
							result = new ListOfTerm();
						}
						result.add(SL.word(((FT)fts[i]).getID()));
						for (int k=0; k<vals.size(); k++) {
							result.add((Term)vals.get(k));
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * @param s
	 * @param terms
	 * @return
	 */
	
	String complete(String term, HashMap named_terms)
	{
		String result = "";
		int i = 0, j, k;
		do {
			j = term.indexOf('<', i);
			if ( j != -1 ) {
				k = term.indexOf('>', j);
				result += term.substring(i, j);
				String lid = (String)named_terms.get(term.substring(j+1, k).trim());
				if ( lid == null ) {
					result += "NULL";
				}
				else {
					result += ((FT)terms.get(lid)).getID();
				}
				i = k+1;
			}
			else {
				result += term.substring(i);
				i = j;
			}
		} while ( i != -1 );
		return result;
	}
	
	//---------------------------------------------------------------
	//                        INNER CLASSES
	//---------------------------------------------------------------
	class FT {
		String lid;
		String classname;
		
		HashMap fields;
		
		
		FT(String classname) {
			this.lid = null;
			this.classname = classname;
			this.fields = new HashMap();
		}
		
		
		FT(FunctionalTermParamNode node) {
			this.lid = null;
			this.classname = node.as_symbol().toString();
			this.fields = new HashMap();
			for (int i=0;i<node.as_parameters().size(); i++) {
				Parameter param = node.as_parameters().element(i);
				fields.put(param.lx_name(), param.as_value());
			}
		}
		
		void setLID(String lid) {
			this.lid = lid;
		}

		String getLID() {
			return lid;
		}
		
		String getID() {
			return Tools.term2AID(kbase.getAgentName()).getLocalName()+":"+getLID();
		}
		
		String getClassName() {
			return classname;
		}
		
		Term getField(String field) {
			return (Term)fields.get(field);
		}

		FunctionalTermParamNode toTerm()
		{
			FunctionalTermParamNode term = new FunctionalTermParamNode(SL.symbol(classname),
					                                                   new ListOfParameter());
			Object[] thiskeys = fields.keySet().toArray();
			for ( int i=0; i<thiskeys.length; i++ ) {
				term.as_parameters().add(new ParameterNode((Term)fields.get(thiskeys[i]),(String)thiskeys[i],Boolean.FALSE));
			}	
			return term;
		}
		
		@Override
		public boolean equals(Object object) {
			FT other = (FT)object;
			if ( this.classname.equals(other.classname) ) {
				Object[] thiskeys = fields.keySet().toArray();
				Object[] otherkeys = other.fields.keySet().toArray();
				if ( thiskeys.length == otherkeys.length ) {
					boolean equals = true;
					for ( int i=0; i<thiskeys.length && equals; i++ ) {
						equals = (thiskeys[i].equals(otherkeys[i])
						  	   && ((fields.get(thiskeys[i])==null && other.fields.get(otherkeys[i])==null)
						           || fields.get(thiskeys[i])==other.fields.get(otherkeys[i])));
					}
					return equals;
				}
			}
			return false;
		}
		
		@Override
		public String toString() {
			return toTerm().toString();
		}
	}
	
	//---------------------------------------------------------------
	//                        CONSTRUCTORS
	//---------------------------------------------------------------
	/**
	 * Constructor
	 * @param kbase the kbase used to add the proper filters. It should not be null.
	 */
	
	public FunctionalTermTableImpl(FilterKBase kbase) {
		this.kbase = kbase;
		this.nlid = 0;
		this.terms = new HashMap();
		this.kbase.addKBQueryFilter(getKBQueryFilter());
	}
	
	//---------------------------------------------------------------
	//                        PUBLIC METHODS
	//---------------------------------------------------------------
	/* (non-Javadoc)
	 * @see test.functermobjects.FunctionalTermTable#add(jade.semantics.lang.sl.grammar.FunctionalTermParamNode)
	 */
	public String add(FunctionalTermParamNode term) {
		FT ft = new FT(term);
		if ( !terms.containsValue(ft) ) {
			ft.setLID(Long.toString(nlid++));
			terms.put(ft.getLID(), ft);
			kbase.updateObservers(null);
		}
		return ft.getLID();
	}
	
	/* (non-Javadoc)
	 * @see test.functermobjects.FunctionalTermTable#load(java.io.Reader)
	 */
	
	public void load(Reader r) throws IOException {
		BufferedReader br = new BufferedReader(r);
		HashMap named_terms = new HashMap();
		FunctionalTermParamNode term = null;
		String line = null;
		String myName = Tools.term2AID(kbase.getAgentName()).getLocalName();

		while (br.ready()) {
			line = br.readLine().trim();
			if ( line.startsWith("(") ) {
				// Functional term without a name
				term = (FunctionalTermParamNode)SL.term(complete(line.trim(), named_terms));
				term = (FunctionalTermParamNode)term.instantiate("myself", SL.string(myName));
				add(term);
				System.err.println("adding term ["+term+"]");
			}
			else if ( line.indexOf('=') != -1 ) {
				// Functional term without a name
				String split[] = line.split("=");
				if ( split.length == 2 ) {
					term = (FunctionalTermParamNode)SL.term(complete(split[1].trim(), named_terms));
					term = (FunctionalTermParamNode)term.instantiate("myself", SL.string(myName));
					String lid = add(term);		
					named_terms.put(split[0].trim(), lid);
					System.err.println("adding term ["+term+"]");
				}
			}
		}
	}	
	
	/* (non-Javadoc)
	 * @see test.functermobjects.FunctionalTermTable#load(java.lang.String)
	 */
	public void load(String file) {
		try {
			load(new FileReader(file));
		}
		catch(Exception e) {e.printStackTrace();}
	}
	
	
	/* (non-Javadoc)
	 * @see test.functermobjects.FunctionalTermTable#getKBQueryFilter(jade.semantics.interpreter.SemanticCapabilities)
	 */
	public KBQueryFilter getKBQueryFilter() {
		return new KBQueryFilter() {
			
			@Override
			public QueryResult apply(Formula formula, ArrayList falsityReasons, QueryResult.BoolWrapper goOn) {
				QueryResult queryResult = QueryResult.UNKNOWN;
				ArrayList list = new ArrayList();
				MatchResult matchres = null, matchval[];
				
				try {
					if ( (formula instanceof BelieveNode) && 
						 ((BelieveNode)formula).as_agent().equals(kbase.getAgentName()) &&
						 ((BelieveNode)formula).as_formula() instanceof PredicateNode ) {

						PredicateNode predicate = (PredicateNode)((BelieveNode)formula).as_formula();
						SymbolNode symbolNode = (SymbolNode)predicate.as_symbol();
						String[] symbol = symbolNode.toString().split(":");

						if ( symbol.length == 2 ) {
							String classname = symbol[0];
							String[] fields = symbol[1].split("&");
							Node[] terms = predicate.as_terms().children();
							int nbvals = terms.length-1; // The first term is the global id.

							if ( (nbvals >= 1) && (terms[0] != null) && (nbvals == fields.length) ) {
								Term id = (Term)terms[0];
								matchval = new MatchResult[nbvals];
								ListOfTerm results = null;
								if ( id instanceof MetaTermReferenceNode ) {
									results = lookupFields(classname, fields, null);
									if ( results != null ) {
										for ( int i=0; i<results.size(); i+=(nbvals+1) ) {
											matchres = id.match(results.get(i));
											for ( int k=0; k<nbvals; k++) {
												matchval[k] = ((Term)terms[k+1]).match(results.get(i+k+1));
												if ( matchval[k] == null ) {
													matchres = null;
													break;
												}
												matchres = matchres.join(matchval[k]);
											}
											if ( matchres != null ) {list.add(matchres);}
										}
									}
								}
								else {
									results = lookupFields(classname, fields, localID(id.toString()));
									if ( results != null ) {
										for ( int i=0; i<results.size(); i+=nbvals+1) {
											for ( int k=0; k<nbvals; k++) {
												matchval[k] = ((Term)terms[k+1]).match(results.get(i+k));
												if ( matchval[k] == null ) {
													matchres = null;
													break;
												}
												//else {
													matchres = (matchres==null?
															matchval[k]:matchres.join(matchval[k]));
												//}
											}
											if ( matchres != null ) {list.add(matchres);}
										}
									}
								}
								if ( list.size() != 0 ) {return new QueryResult(list);}
							}
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					return QueryResult.UNKNOWN;
				}
				
				return queryResult;
			}
			
		    @Override
			public boolean getObserverTriggerPatterns(Formula formula, Set set) {return true;}
		};
	}
}
