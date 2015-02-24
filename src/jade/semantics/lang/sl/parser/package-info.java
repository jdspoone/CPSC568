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

/**
 * Provides the classes used by the JSA engine to parse and unparse FIPA-SL
 * expressions.
 * 
 * The format of parsed expressions relies on the hierarchy of
 * {@link jade.semantics.lang.sl.grammar.Node} classes (see the
 * {@link jade.semantics.lang.sl.grammar} package). Note that this hierarchy
 * actually implements an extension of FIPA-SL, which includes meta-references
 * and makes it possible to match and instantiate patterns of expressions
 * against regular expressions (see the {@link jade.semantics.lang.sl.tools.SL}
 * class).
 */
package jade.semantics.lang.sl.parser;