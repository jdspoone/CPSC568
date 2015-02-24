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

package jade.semantics.lang.sl.tools;

import jade.semantics.lang.sl.grammar.ActionContentExpressionNode;
import jade.semantics.lang.sl.grammar.ActionExpression;
import jade.semantics.lang.sl.grammar.AndNode;
import jade.semantics.lang.sl.grammar.ByteConstantNode;
import jade.semantics.lang.sl.grammar.Constant;
import jade.semantics.lang.sl.grammar.Content;
import jade.semantics.lang.sl.grammar.ContentExpression;
import jade.semantics.lang.sl.grammar.DateTimeConstantNode;
import jade.semantics.lang.sl.grammar.FalseNode;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.FormulaContentExpressionNode;
import jade.semantics.lang.sl.grammar.FunctionalTermParamNode;
import jade.semantics.lang.sl.grammar.IdentifyingContentExpressionNode;
import jade.semantics.lang.sl.grammar.IdentifyingExpression;
import jade.semantics.lang.sl.grammar.IntegerConstantNode;
import jade.semantics.lang.sl.grammar.ListOfFormula;
import jade.semantics.lang.sl.grammar.ListOfNodes;
import jade.semantics.lang.sl.grammar.MetaContentExpressionReferenceNode;
import jade.semantics.lang.sl.grammar.MetaFormulaReferenceNode;
import jade.semantics.lang.sl.grammar.MetaSymbolReferenceNode;
import jade.semantics.lang.sl.grammar.MetaTermReferenceNode;
import jade.semantics.lang.sl.grammar.MetaVariableReferenceNode;
import jade.semantics.lang.sl.grammar.Node;
import jade.semantics.lang.sl.grammar.OrNode;
import jade.semantics.lang.sl.grammar.Parameter;
import jade.semantics.lang.sl.grammar.RealConstantNode;
import jade.semantics.lang.sl.grammar.RelativeTimeConstantNode;
import jade.semantics.lang.sl.grammar.Symbol;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.grammar.TrueNode;
import jade.semantics.lang.sl.grammar.Variable;
import jade.semantics.lang.sl.grammar.VariableNode;
import jade.semantics.lang.sl.grammar.operations.SLSyntax;
import jade.semantics.lang.sl.parser.ParseException;
import jade.semantics.lang.sl.parser.SLParser;
import jade.util.leap.ArrayList;

import java.util.Date;
import java.util.Iterator;

/**
 * The SL class provides a simple mechanism to check if 2 SL
 * expressions considered as patterns match. If true, it becomes possible to
 * retrieve the value of meta variables that has been unified during the match
 * checking.
 * 
 * @author Thierry Martinez - France T�l�com
 * @version 0.9
 */
public class SL {

	/**
	 * The FIPA-SL date format
	 */
	static public final java.text.SimpleDateFormat DATE_FORMAT = new java.text.SimpleDateFormat("yyyyMMdd'T'HHmmssSSS");
	static public long NULL_TIME_OFFSET = 0;

	/**
	 * SL true boolean constant. Using this static constant avoids creating
	 * useless {@link TrueNode} instances.
	 */
	public static final TrueNode TRUE = new TrueNode();//.getSimplifiedFormula();

	/**
	 * SL false boolean constant. Using this static constant avoids creating
	 * useless {@link FalseNode} instances.
	 */
	public static final FalseNode FALSE = new FalseNode();//.getSimplifiedFormula();


	public static final Class[] META_REFERENCE_CLASSES = new Class[] {
		MetaContentExpressionReferenceNode.class,
		MetaFormulaReferenceNode.class,
		MetaSymbolReferenceNode.class,
		MetaTermReferenceNode.class,
		MetaVariableReferenceNode.class
	};

	/**
	 * This exception is thrown when trying to get the value of a meta variable
	 * with the wrong type. For example, when trying to get a formula while the
	 * meta variable value is a term.
	 * 
	 * @see jade.semantics.lang.sl.tools.MatchResult#getFormula(String name)
	 * @see jade.semantics.lang.sl.tools.MatchResult#getTerm(String name)
	 * @see jade.semantics.lang.sl.tools.MatchResult#getVariable(String name)
	 * @see jade.semantics.lang.sl.tools.MatchResult#getSymbol(String name)
	 * @see jade.semantics.lang.sl.tools.MatchResult#getActionExpression(String name)
	 * @see jade.semantics.lang.sl.tools.MatchResult#getContentExpression(String name)
	 */
	public static class WrongTypeException extends Exception {
		private static final long serialVersionUID = 1L;
	}

	public static class LoopingInstantiationException extends Exception {
		private static final long serialVersionUID = 1L;

		public LoopingInstantiationException(Node meta, Node value) {
			super("trying to instantiate ??" + meta.getAttribute(MetaTermReferenceNode.lx_name_ID) + ", with another metareference ??"
					+ value.getAttribute(MetaTermReferenceNode.lx_name_ID) );
		}
	}

	/**
	 * This method return a new expression corresponding to the given one in
	 * which all variables equals to <b><code>x</code></b> are replaced by a
	 * meta variable named <b><code>??X</code></b>.
	 * 
	 * @param expression
	 *            the expression to transform as a pattern.
	 * @param x
	 *            the variable to be replaced by a meta variable.
	 * @return the pattern node.
	 */
	public static Node toPattern(Node expression, Variable x) {
		if ( expression instanceof Variable )  {	
			return new MetaTermReferenceNode("X");
		}
		Node result = expression.getClone();
		variable2MetaVariable(result, x, "X");
		return result;
	}

	/**
	 * This method return a new expression corresponding to the given one in
	 * which all variables equals to <b><code>x</code></b> are replaced by a
	 * meta variable named <b><code>??\<metaname\></code></b>.
	 * 
	 * @param expression
	 *            the expression to transform as a pattern.
	 * @param x
	 *            the variable to be replaced by a meta variable.
	 * @param metaname
	 *            the name of the introduced meta variable.
	 * @return the pattern node.
	 */
	public static Node toPattern(Node expression, Variable x, String metaname) {
		if ( expression instanceof Variable ) {
			return new MetaTermReferenceNode(metaname);
		}
		Node result = expression.getClone();
		variable2MetaVariable(result, x, metaname);
		return result;
	}

	/**
	 * This method returns a new expression corresponding to the given one in
	 * which all variables of the given list  are replaced by a
	 * meta variable named <b><code>?? + given prefix + \<metaname\></code></b>.<br>
	 * The metaname is the same as the variable name.
	 * @param expression
	 *            the expression to transform as a pattern.
	 * @param l
	 *            the list of variables to be replaced by meta variables.
	 * @param prefix
	 *            the prefix of the name of the introduced meta variable.
	 * @return the pattern node.
	 */
	public static Node toPattern(Node expression, ListOfNodes l, String prefix) {
		if ( expression instanceof Variable ) {
			if (prefix != null) {
				return new MetaTermReferenceNode(prefix + ((Variable)expression).lx_name());
			}
			return new MetaTermReferenceNode(((Variable)expression).lx_name());
		}
		Node result = expression.getClone(); 
		for (int i = 0; i < l.size(); i++) {
			//node = toPattern(node, (VariableNode)l.get(i), ((VariableNode)l.get(i)).lx_name());
			if (prefix != null) {
				variable2MetaVariable(result, (VariableNode)l.get(i), prefix + ((VariableNode)l.get(i)).lx_name());    
			} else {
				variable2MetaVariable(result, (VariableNode)l.get(i), ((VariableNode)l.get(i)).lx_name());
			}
		}
		return result;
	}


	/**
	 * Assigned the value of the named meta variable.
	 * 
	 * @param name
	 *            the name of the meta variable to assign its value.
	 * @param value
	 *            the value to be assigned to this meta variable.
	 * @return the manipulator itself.
	 * @exception WrongTypeException
	 *                Thrown if the value of the variable is not f the good
	 *                type.
	 */
	public static void set(Node expression, String name, Node value)
	throws WrongTypeException {
		ListOfNodes metaReferences = new ListOfNodes();
		// FIXME assume MetaTermReferenceNode.lx_name_ID equals any XXX.lx_name_ID
		if (expression.find(META_REFERENCE_CLASSES,
				MetaTermReferenceNode.lx_name_ID, nameOf(name), metaReferences, true)) {
//			for (int i = 0; i < metaReferences.size(); i++) {
//			setMetaReferenceValue(metaReferences.get(i), value);
//			}
			if ( value == null ) {
				for (Iterator it = metaReferences.iterator(); it.hasNext();) {
					((Node)it.next()).setAttribute(MetaTermReferenceNode.sm_value_ID, value);
				}
			}
			else {
				for (Iterator it = metaReferences.iterator(); it.hasNext();) {
					setMetaReferenceValue((Node)it.next(), value);
				}
			}
		}
	}

	/**
	 * Instantiates a formula by replacing all metavariables 
	 * contained in m by their values. It also clones the input formula.
	 * @param f the Formula to instantiate
	 * @param m the MatchResult containinf the metavariable to instantiate
	 * @return the same formula with the metavariable replaced by their values
	 */
	public static Node instantiate(Node node, MatchResult mr) {
		try {
			Node result = node.getClone();
			for (int i=0; i<mr.size(); i++) {
				set(result,
						SL.getMetaReferenceName(mr.get(i)),
						SL.getMetaReferenceValue(mr.get(i)));
			}
			substituteMetaReferences(result);
			return result;
		} catch (Exception e) {
			System.err.println("Exception occurs when trying to instantiate "+ node +  " with " + mr);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Return a tree representing the instantiated pattern, meaning all meta
	 * variables have been replaced by their value.
	 * 
	 * @param expression the expression to instantiate.
	 * @return the instantiated expression.
	 */
	public static Node instantiate(Node expression) {
		try {
			Node result = expression.getClone();
			substituteMetaReferences(result);
			return result;
		} catch (LoopingInstantiationException lie) {
			lie.printStackTrace();
			return null;
		}
	}

	/**
	 * @param expression
	 * @param varname
	 * @param value
	 * @return
	 * @throws WrongTypeException
	 */
	public static Node instantiate(Node expression, String varname, Node value) {
		try {
			Node result = expression.getClone();
			set(result, varname, value);
			substituteMetaReferences(result);
			return result;
		} catch (Exception e) {
			System.err.println("Exception occurs when trying to instantiate "+ expression);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param expression
	 * @param varname1
	 * @param value1
	 * @param varname2
	 * @param value2
	 * @return
	 * @throws WrongTypeException
	 */
	public static Node instantiate(Node expression, String varname1,
			Node value1, String varname2, Node value2) {
		try {
			Node result = expression.getClone();
			set(result, varname1, value1);
			set(result, varname2, value2);
			substituteMetaReferences(result);
			return result;
		} catch (Exception e) {
			System.err.println("Exception occurs when trying to instantiate "+ expression);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param expression
	 * @param varname1
	 * @param value1
	 * @param varname2
	 * @param value2
	 * @param varname3
	 * @param value3
	 * @return
	 * @throws WrongTypeException
	 */
	public static Node instantiate(Node expression, String varname1,
			Node value1, String varname2, Node value2, String varname3,
			Node value3) {
		try {
			Node result = expression.getClone();
			set(result, varname1, value1);
			set(result, varname2, value2);
			set(result, varname3, value3);
			substituteMetaReferences(result);
			return result;
		} catch (Exception e) {
			System.err.println("Exception occurs when trying to instantiate "+ expression);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param expression
	 * @param varname1
	 * @param value1
	 * @param varname2
	 * @param value2
	 * @param varname3
	 * @param value3
	 * @param varname4
	 * @param value4
	 * @return
	 * @throws WrongTypeException
	 */
	public static Node instantiate(Node expression, String varname1,
			Node value1, String varname2, Node value2, String varname3,
			Node value3, String varname4, Node value4) {
		try {
			Node result = expression.getClone();
			set(result, varname1, value1);
			set(result, varname2, value2);
			set(result, varname3, value3);
			set(result, varname4, value4);
			substituteMetaReferences(result);
			return result;
		} catch (Exception e) {
			System.err.println("Exception occurs when trying to instantiate "+ expression);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param expression
	 * @param arguments an array containing alternatively the var name and the value to be assigned.
	 *                  var names are Strings while values are of type Node.
	 * @return a node that is the result of the instantiation
	 * @throws WrongTypeException
	 */
	public static Node instantiate(Node expression, Object[] varNamesAndValues) {
		try {
			Node result = expression.getClone();
			for (int i = 0; i<varNamesAndValues.length; i+=2) {
				set(result, (String)varNamesAndValues[i], (Node)varNamesAndValues[i+1]);
			}
			substituteMetaReferences(result);
			return result;
		} catch (Exception e) {
			System.err.println("Exception occurs when trying to instantiate "+ expression);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param value the value of that SL string constant
	 * @return a StringConstantNode holding the given string
	 */
	public static Constant string(String value)
	{
		return SLSharedNodeTable.getInstance().getStringConstantNode(value);
	}

	/**
	 * @param value the value of that SL word constant
	 * @return a WordConstantNode holding the given string
	 */
	public static Constant word(String value)
	{
		return SLSharedNodeTable.getInstance().getWordConstantNode(value);
	}

	/**
	 * @param value the value of that SL integer constant
	 * @return a IntegerConstantNode holding the given value
	 */
	public static Constant integer(long value)
	{
		return new IntegerConstantNode(new Long(value));
	}

	/**
	 * @param value the value of that SL integer constant
	 * @return a IntegerConstantNode holding the given value
	 */
	public static Constant integer(String value)
	{
		return new IntegerConstantNode(new Long(value));
	}

	/**
	 * @param value the value of that SL real constant
	 * @return a RealConstantNode holding the given value
	 */
	public static Constant real(double value)
	{
		return new RealConstantNode(new Double(value));
	}

	/**
	 * @param value the value of that SL real constant
	 * @return a RealConstantNode holding the given value
	 */
	public static Constant real(String value)
	{
		return new RealConstantNode(new Double(value));
	}

	/**
	 * @param value the value of that SL date constant
	 * @return a DateTimeConstantNode holding the given value
	 */
	public static Constant date(Date value)
	{
		return new DateTimeConstantNode(value);
	}

	/**
	 * @param value the value of that SL date constant
	 * @return a DateTimeConstantNode holding the given value
	 */
	public static Constant date(String value)
	{
		try {
			if (value.startsWith("+")) {
				return new RelativeTimeConstantNode(new Long(
						relativeTime(value.substring(1))));
			}
			else if (value.startsWith("-")) {
				return new RelativeTimeConstantNode(new Long(
						-relativeTime(value.substring(1))));
			}
			else {
				return new DateTimeConstantNode(SL.DATE_FORMAT.parse(value));
			}
		}
		catch (Exception e) {e.printStackTrace();}
		return null;
	}
	
	private static long relativeTime(String value) throws java.text.ParseException {
		if (NULL_TIME_OFFSET == 0)
			NULL_TIME_OFFSET = DATE_FORMAT.parse("00000000T000000000").getTime();
		return SL.DATE_FORMAT.parse(value).getTime() - NULL_TIME_OFFSET;
	}
	
	public static String relativeTime(long time) {
		return null;
	}

	/**
	 * @param value the value of that SL date constant
	 * @return a DateTimeConstantNode holding the given value
	 */
	public static Constant bytes(byte[] value)
	{
		return new ByteConstantNode(value);
	}

	/**
	 * @param value the value of that symbol
	 * @return a SL SymbolNode with the given name
	 */
	public static Symbol symbol(String name)
	{
		return SLSharedNodeTable.getInstance().getSymbolNode(name);
	}

	/**
	 * @deprecated use formula instead
	 */
	@Deprecated
	public static Formula fromFormula(String expression) {
		return formula(expression);
	}

	/**
	 * @param expression
	 * @return
	 */
	public static Formula formula(String expression) {
		try {
			return SLParser.getParser().parseFormula(expression, true);
		} catch (ParseException pe) {
			pe.printStackTrace();
			return null;
		}
	}

	/**
	 * @deprecated use term instead
	 */
	@Deprecated
	public static Term fromTerm(String expression) {
		return term(expression);
	}

	/**
	 * @param expression
	 * @return
	 */
	public static Term term(String expression) {
		try {
//			System.err.println("expression="+expression);
			return SLParser.getParser().parseTerm(expression, true);
		} catch (ParseException pe) {
			pe.printStackTrace();
			return null;
		}
	}

	/**
	 * @deprecated use content instead
	 */
	@Deprecated
	public static Content fromContent(String expression) {
		return content(expression);
	}

	/**
	 * @param expression
	 * @return
	 */
	public static Content content(String expression) {
		try {
			return SLParser.getParser().parseContent(expression, true);
		} catch (ParseException pe) {
			pe.printStackTrace();
			return null;
		}
	}

	/**
	 * Builds the conjunction of a list of formulas. Returns <code>false</code>
	 * if the list is empty.
	 * @param literals list of formulas that are to be linked with the
	 *                 <code>and</code> logical connector.
	 * @return the conjunction of the formulas in literals.
	 */
	public static Formula and(ListOfFormula literals) {
		if (literals.size() == 0) {
			return SL.FALSE;
		}
		else if (literals.size() == 1) {
			return literals.element(0);
		}
		else {
			return new AndNode((Formula)literals.remove(0), and(literals));
		}
	}

	/**
	 * Builds the disjunction of a list of formulas. Returns <code>true</code>
	 * if the list is empty.
	 * @param literals list of formulas that are to be linked with the
	 *                 <code>or</code> logical connector.
	 * @return the disjunction of the formulas in literals.
	 */
	public static Formula or(ListOfFormula literals) {
		if (literals.size() == 0) {
			return SL.TRUE;
		}
		else if (literals.size() == 1) {
			return literals.element(0);
		}
		else {
			return new OrNode((Formula)literals.remove(0), or(literals));
		}
	}

	/**
	 * Check if the expressions 1 and 2 match. If true, all meta variables that
	 * has been unified can be accessed using one of the <b><code>getFormula, getTerm, ... </code></b>
	 * methods of the matching result. This method is equivalent to <b><code>SL.newMatcher().match(expression1, expression2);</code></b>
	 * 
	 * @param expression1
	 *            the first expression.
	 * @param expression2
	 *            the second expression.
	 * @return A matching result if the matching complete, null otherwise.
	 * @see jade.semantics.lang.sl.tools.MatchResult
	 */

	/** TODO 
	 * @author CA
	 * il faudrait que cette fonction match fasse appel � getSimplifiedTerm
	 * avant de comparer les 2 termes, sinon tous les patterns �crits � la
	 * main sont potentiellement dans le d�sordre ce qui provoque des
	 * exceptions difficiles � localiser...
	 */

	public static MatchResult match(Node expression1, Node expression2) {
//		return SLMatcher2.match(expression1, expression2);
		//System.err.println("match "+expression1+" with "+expression2);
		return new SLMatcher().match(expression1, expression2);
	}

	/**
	 * @param metaReference
	 * @return
	 */
	public static String getMetaReferenceName(Node metaReference)
	{
		return (String) metaReference.getAttribute("lx_name");
	}

	/**
	 * @param metaReference
	 * @return
	 */
	public static Node getMetaReferenceValue(Node metaReference)
	{
		return (Node)metaReference.getAttribute("sm_value");
	}

	/**
	 * Return true if the given node is a metareference node
	 * @param node
	 * @return true if the given node is a metareference node
	 */
	public static boolean isMetaReference(Node node)
	{
		return  node instanceof MetaFormulaReferenceNode ||
		node instanceof MetaTermReferenceNode||
		node instanceof MetaVariableReferenceNode ||
		node instanceof MetaSymbolReferenceNode ||
		node instanceof MetaContentExpressionReferenceNode;
	}


	/**
	 * @param metaReference
	 * @param value
	 * @throws WrongTypeException
	 */
	public static void setMetaReferenceValue(Node metaReference, Node value)
	throws WrongTypeException
	{
		try {
			if (metaReference instanceof MetaFormulaReferenceNode) {
				MetaFormulaReferenceNode mRef = (MetaFormulaReferenceNode)metaReference;
				if (value == null || value instanceof Formula) {
					mRef.sm_value((Formula) value);
				} else {
					mRef.sm_value(((FormulaContentExpressionNode) value).as_formula());
				}
			} else if (metaReference instanceof MetaTermReferenceNode) {
				((MetaTermReferenceNode) metaReference).sm_value((Term) value);
			} else if (metaReference instanceof MetaVariableReferenceNode) {
				((MetaVariableReferenceNode) metaReference).sm_value((Variable) value);
			} else if (metaReference instanceof MetaSymbolReferenceNode) {
				((MetaSymbolReferenceNode) metaReference).sm_value((Symbol) value);
			} else if (metaReference instanceof MetaContentExpressionReferenceNode) {
				MetaContentExpressionReferenceNode mRef = (MetaContentExpressionReferenceNode)metaReference;
				if (value == null || value instanceof ContentExpression) {
					mRef.sm_value((ContentExpression) value);
				} else if (value instanceof Formula) {
					mRef.sm_value(new FormulaContentExpressionNode((Formula) value));
				} else if (value instanceof ActionExpression) {
					mRef.sm_value(new ActionContentExpressionNode((ActionExpression) value));
				} else {
					mRef.sm_value(new IdentifyingContentExpressionNode((IdentifyingExpression) value));
				}
			} else {
				throw new WrongTypeException();
			}
		} catch (ClassCastException cce) {
			throw new WrongTypeException();
		}
	}


	public static void setMetaReferenceName(Node metaReference, String name) {
		metaReference.setAttribute(MetaTermReferenceNode.lx_name_ID, name);
	}


	/**
	 * @param expression
	 */
	public static void clearMetaReferences(Node expression)
	// -----------------------------------------------
	{
		ListOfNodes metaReferences = new ListOfNodes();
		expression.childrenOfKind(META_REFERENCE_CLASSES, metaReferences);
		for (int i = 0; i < metaReferences.size(); i++) {
			try {
				setMetaReferenceValue(metaReferences.get(i), null);
			} catch (WrongTypeException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param node
	 * @throws LoopingInstantiationException
	 */
	public static void substituteMetaReferences(Node node)
	throws LoopingInstantiationException
	{
		Node[] children = node.children();

		for (int i = 0; i < children.length; i++) {
			Node childNode = children[i];
			Node valueNode = null;

			while (isMetaReference(childNode)
					&& ((valueNode = (Node)childNode.getAttribute(MetaTermReferenceNode.sm_value_ID)) != null)) {
				if (childNode == valueNode) {
					childNode.setAttribute(MetaTermReferenceNode.sm_value_ID, null);
					break;
//					throw new LoopingInstantiationException(childNode, valueNode);
				}
				childNode = valueNode;
			}
			// childNode is not a meta-reference or its value is null
			substituteMetaReferences(childNode);

			if (childNode != children[i]) {
				node.replace(i, childNode);
			}
		}
	}

	/**
	 * @param node
	 */
	public static void removeOptionalParameter(Node node)
	{
		ListOfNodes terms = new ListOfNodes();
		if ( node.childrenOfKind(FunctionalTermParamNode.class, terms) ) {
			for (int i=0; i<terms.size(); i++) {
				FunctionalTermParamNode term = (FunctionalTermParamNode)terms.get(i);
				for (int j=term.as_parameters().size()-1; j>=0; j--) {
					Parameter p = term.as_parameters().element(j);
					if ( p.lx_optional().booleanValue() &&
							((p.as_value() instanceof MetaTermReferenceNode && ((MetaTermReferenceNode)p.as_value()).sm_value() == null)
									|| (p.as_value() instanceof MetaVariableReferenceNode && ((MetaVariableReferenceNode)p.as_value()).sm_value() == null)) ) {
						term.as_parameters().remove(p);
					}
				}

			}
		}
	}

	/**
	 * This method transforms an SL expression so that all contained metareferences 
	 * with same name and same type are refered to by the same java reference.
	 * @param expr
	 * @param mrefs
	 * @return the expr argument
	 */
	public static Node linkSameMetaReferences(Node expr) {
		linkSameMetaReferences(expr, new ArrayList());
		return expr;
	}

	public static void linkSameMetaReferences(Node expr, ArrayList mrefs) {
		if ( expr instanceof ListOfNodes ) {
			for (int i=0; i<((ListOfNodes)expr).size(); i++) {
				linkSameMetaReferences(expr, ((ListOfNodes)expr).get(i), i, mrefs);
			}
		}
		else {
			Node nodes[] = expr.children();
			for (int i=0; i<nodes.length; i++) {
				linkSameMetaReferences(expr, nodes[i], i, mrefs);
			}	
		}
	}

	// ===============================================
	// Protected - Private
	// ===============================================
	// -----------------------------------------------
	private static String nameOf(String name)
	// -----------------------------------------------
	{
		return name != null && name.startsWith("??") ?
				name.substring(2) :
					name;
	}

	// -----------------------------------------------
	private static void variable2MetaVariable(Node node, Variable x, String metavarname)
	// -----------------------------------------------
	{
		Node[] children = node.children();
		for (int i = 0; i < children.length; i++) {
			if (children[i] instanceof VariableNode
					&& ((VariableNode) children[i]).lx_name().equals(
							x.lx_name())) {
				node.replace(i, new MetaTermReferenceNode(metavarname));
			} else {
				variable2MetaVariable(children[i], x, metavarname);
			}
		}
	}

	// -----------------------------------------------
	private static Node get(ArrayList mrefs, Node mref) {
		// -----------------------------------------------
		if ( mrefs.contains(mref) ) {
			for (int i=0; i<mrefs.size(); i++) {
				if ( mrefs.get(i).equals(mref) ) {
					return (Node)mrefs.get(i);
				}
			}
		}
		return null;
	}

	// -----------------------------------------------
	private static void linkSameMetaReferences(Node parent, Node current, int index, ArrayList mrefs) {
		// -----------------------------------------------
		if ( SL.isMetaReference(current) ) {
			Node other = get(mrefs, current);
			if ( other != null ) {
				parent.replace(index, other);
			}
			else {
				mrefs.add(current);
			}
		}
		else {
			linkSameMetaReferences(current, mrefs);
		}
	}


	// ==========================================================
	// Static needed statement to initialise the node operations.
	// ==========================================================
	static {
		SLSyntax.installOperations();
	}

}
