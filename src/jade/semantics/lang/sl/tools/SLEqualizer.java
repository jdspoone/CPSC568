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

//#PJAVA_EXCLUDE_BEGIN
import jade.semantics.lang.sl.grammar.ByteConstantNode;
import jade.semantics.lang.sl.grammar.DateTimeConstantNode;
import jade.semantics.lang.sl.grammar.FunctionalTermParamNode;
import jade.semantics.lang.sl.grammar.IntegerConstantNode;
import jade.semantics.lang.sl.grammar.MetaContentExpressionReferenceNode;
import jade.semantics.lang.sl.grammar.MetaFormulaReferenceNode;
import jade.semantics.lang.sl.grammar.MetaSymbolReferenceNode;
import jade.semantics.lang.sl.grammar.MetaTermReferenceNode;
import jade.semantics.lang.sl.grammar.Node;
import jade.semantics.lang.sl.grammar.ParameterNode;
import jade.semantics.lang.sl.grammar.RealConstantNode;
import jade.semantics.lang.sl.grammar.StringConstantNode;
import jade.semantics.lang.sl.grammar.SymbolNode;
import jade.semantics.lang.sl.grammar.VariableNode;
import jade.semantics.lang.sl.grammar.WordConstantNode;

import java.util.Arrays;
import java.util.HashMap;


/**
 * The SL class provides a simple mechanism to check if 2 SL
 * expressions considered as patterns match. If true, it becomes possible to
 * retrieve the value of meta variables that has been unified during the match
 * checking.
 * 
 * @author Thierry Martinez - France T�l�com
 * @version 0.9
 */
	public class SLEqualizer {
		
		
		HashMap _variablesMap = new HashMap();
		
		public boolean equals(Node expression1, Node expression2) {
			_variablesMap.clear();
			return doEquals(expression1, expression2);
		}
		
		public boolean doEquals(Node expression1, Node expression2) {
			boolean equal = expression1.getClass() == expression2.getClass();
			if ( equal ) {
				Node[] children1 = expression1.children();
				Node[] children2 = expression2.children();
				equal = (children1.length == children2.length);
				if ( equal ) {
					if ( expression1 instanceof FunctionalTermParamNode &&
						((FunctionalTermParamNode)expression1).as_symbol() instanceof SymbolNode &&
						((SymbolNode)((FunctionalTermParamNode)expression1).as_symbol()).toString().equals("agent-identifier") &&
						((FunctionalTermParamNode)expression1).as_symbol().equals(((FunctionalTermParamNode)expression2).as_symbol())) {
//						System.out.println("!!!!!!! SLequalizer, expression1 = "+expression1);
//						System.out.println("!!!!!!! SLequalizer, expression2 = "+expression2);
//						System.out.println("!!! name1="+((FunctionalTermParamNode)expression1).getParameter("name")+" and name2="+((FunctionalTermParamNode)expression2).getParameter("name"));
//						boolean ret = ((FunctionalTermParamNode)expression1).getParameter("name").equals(((FunctionalTermParamNode)expression2).getParameter("name"));
//						System.out.println("!!! comparison returns "+ret);
//						return ret;
						return ((FunctionalTermParamNode)expression1).getParameter("name").equals(((FunctionalTermParamNode)expression2).getParameter("name"));
					}
					else if ( expression1 instanceof MetaContentExpressionReferenceNode ) {
						equal = ((MetaContentExpressionReferenceNode)expression1).lx_name()
						.equals(((MetaContentExpressionReferenceNode)expression2).lx_name());
					}
					else if ( expression1 instanceof MetaFormulaReferenceNode ) {
						equal = ((MetaFormulaReferenceNode)expression1).lx_name()
						.equals(((MetaFormulaReferenceNode)expression2).lx_name());
					}
					else if ( expression1 instanceof MetaSymbolReferenceNode ) {
						equal = ((MetaSymbolReferenceNode)expression1).lx_name()
						.equals(((MetaSymbolReferenceNode)expression2).lx_name());
					}
					else if ( expression1 instanceof MetaTermReferenceNode ) {
						equal = ((MetaTermReferenceNode)expression1).lx_name()
						.equals(((MetaTermReferenceNode)expression2).lx_name());
					}
					else if ( expression1 instanceof IntegerConstantNode ) {
						equal = ((IntegerConstantNode)expression1).lx_value()
						.equals(((IntegerConstantNode)expression2).lx_value());
					}
					else if ( expression1 instanceof RealConstantNode ) {
						equal = ((RealConstantNode)expression1).lx_value()
						.equals(((RealConstantNode)expression2).lx_value());
					}
					else if ( expression1 instanceof StringConstantNode ) {
						equal = (((StringConstantNode)expression1).lx_value()
						 == ((StringConstantNode)expression2).lx_value());
					}
					else if ( expression1 instanceof WordConstantNode ) {
						equal = (((WordConstantNode)expression1).lx_value()
						== ((WordConstantNode)expression2).lx_value());
					}
					else if ( expression1 instanceof ByteConstantNode ) {
						//FIXME This shouldn't work in pJava
						equal = Arrays.equals(((ByteConstantNode)expression1).lx_value(),
								((ByteConstantNode)expression2).lx_value());
					}
					else if ( expression1 instanceof DateTimeConstantNode ) {
						equal = ((DateTimeConstantNode)expression1).lx_value()
						.equals(((DateTimeConstantNode)expression2).lx_value());
					}
                    else if ( expression1 instanceof ParameterNode ) {
                        equal = ((ParameterNode)expression1).lx_name()
                        .equals(((ParameterNode)expression2).lx_name()) &&
                        ((ParameterNode)expression1).as_value()
                        .equals(((ParameterNode)expression2).as_value());
                    }
					else if ( expression1 instanceof SymbolNode ) {
						equal = (((SymbolNode)expression1).lx_value()
						== ((SymbolNode)expression2).lx_value());
					}
					else if ( expression1 instanceof VariableNode ) {
						String varName = (String)_variablesMap.get(((VariableNode)expression1).lx_name());
						if ( varName == null ) { 
							varName = (String)_variablesMap.get(((VariableNode)expression2).lx_name());
							if ( varName == null ) {
								_variablesMap.put(((VariableNode)expression1).lx_name(), ((VariableNode)expression2).lx_name());
								equal = true;
							}
							else {
								equal = varName.equals(((VariableNode)expression1).lx_name());
							}
						}
						else {
							equal = varName.equals(((VariableNode)expression2).lx_name());							
						}
					}
				}
				
				for(int i=0; equal && i<children1.length; i++) {
					equal = equal && doEquals(children1[i], children2[i]);
				}
			}			
			return equal;
		}
}
