
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


//-----------------------------------------------------
// This file has been automatically produced by a tool.
//-----------------------------------------------------

package jade.semantics.lang.sl.grammar;

import java.util.HashMap;
//#PJAVA_EXCLUDE_END
/*#PJAVA_INCLUDE_BEGIN
import jade.util.leap.Collection;
import jade.util.leap.Comparable;
import jade.util.leap.LinkedList;
import jade.util.leap.List;
import jade.util.leap.HashMap;
#PJAVA_INCLUDE_END*/

public abstract class Formula extends Node
{
        public interface Operations extends Node.Operations
        {
            public Formula getSimplifiedFormula(Formula node);
            public void simplify(Formula node);
            public boolean isMentalAttitude(Formula node, Term term);
            public boolean isInstitutionalFact(Formula node, Term term);
            public boolean isSubsumedBy(Formula node, Formula formula);
            public boolean isConsistentWith(Formula node, Formula formula);
            public Formula getDoubleMirror(Formula node, Term i, Term j, boolean default_result_is_true);
            public boolean isAFreeVariable(Formula node, Variable x);
            public Formula getVariablesSubstitution(Formula node, ListOfVariable vars);
            public Formula getVariablesSubstitutionAsIn(Formula node, Formula formula);
            public Formula getVariablesSubstitution(Formula node, Variable x, Variable y);
            public Formula isBeliefFrom(Formula node, Term agent);
            public Formula isInstitutionalFactFrom(Formula node, Term institution);
            public Formula isExistsOn(Formula node, Term variable);
            public jade.semantics.lang.sl.tools.MatchResult match(Formula node, Node expression);
            public Formula instantiate(Formula node, String varname, Node expression);
        }
        public Formula getSimplifiedFormula()
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            return((Formula.Operations)_thisoperations).getSimplifiedFormula(this );
        }
        public void simplify()
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            ((Formula.Operations)_thisoperations).simplify(this );
        }
        public boolean isMentalAttitude(Term term)
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            return((Formula.Operations)_thisoperations).isMentalAttitude(this , term);
        }
        public boolean isInstitutionalFact(Term term)
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            return((Formula.Operations)_thisoperations).isInstitutionalFact(this , term);
        }
        public boolean isSubsumedBy(Formula formula)
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            return((Formula.Operations)_thisoperations).isSubsumedBy(this , formula);
        }
        public boolean isConsistentWith(Formula formula)
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            return((Formula.Operations)_thisoperations).isConsistentWith(this , formula);
        }
        public Formula getDoubleMirror(Term i, Term j, boolean default_result_is_true)
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            return((Formula.Operations)_thisoperations).getDoubleMirror(this , i, j, default_result_is_true);
        }
        public boolean isAFreeVariable(Variable x)
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            return((Formula.Operations)_thisoperations).isAFreeVariable(this , x);
        }
        public Formula getVariablesSubstitution(ListOfVariable vars)
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            return((Formula.Operations)_thisoperations).getVariablesSubstitution(this , vars);
        }
        public Formula getVariablesSubstitutionAsIn(Formula formula)
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            return((Formula.Operations)_thisoperations).getVariablesSubstitutionAsIn(this , formula);
        }
        public Formula getVariablesSubstitution(Variable x, Variable y)
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            return((Formula.Operations)_thisoperations).getVariablesSubstitution(this , x, y);
        }
        public Formula isBeliefFrom(Term agent)
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            return((Formula.Operations)_thisoperations).isBeliefFrom(this , agent);
        }
        public Formula isInstitutionalFactFrom(Term institution)
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            return((Formula.Operations)_thisoperations).isInstitutionalFactFrom(this , institution);
        }
        public Formula isExistsOn(Term variable)
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            return((Formula.Operations)_thisoperations).isExistsOn(this , variable);
        }
        public jade.semantics.lang.sl.tools.MatchResult match(Node expression)
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            return((Formula.Operations)_thisoperations).match(this , expression);
        }
        public Formula instantiate(String varname, Node expression)
        {
            
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            return((Formula.Operations)_thisoperations).instantiate(this , varname, expression);
        }
    public static Integer ID = new Integer(10005);
    @Override
	public int getClassID() {return ID.intValue();}
    Formula _sm_simplified_formula;

    public Formula(int capacity)  {
      super (capacity);
    }

    @Override
	public void copyValueOf(Node n, HashMap clones) {
        if (n instanceof Formula) {
            super.copyValueOf(n, clones);
            Formula tn = (Formula)n;
            sm_simplified_formula( tn._sm_simplified_formula);
        }
    }

    @Override
	public Node.Operations getOperations() {
        Node.Operations result = (Node.Operations)_operations.get(ID);
        if ( result == null ) {result = super.getOperations();}
        return result;
    }
    static public int sm_simplified_formula_ID = new String("sm_simplified_formula").hashCode();
    public Formula sm_simplified_formula() {return _sm_simplified_formula;}
    public void sm_simplified_formula(Formula o) {_sm_simplified_formula = o;}

    @Override
	public boolean hasAttribute(int attrname) {
        if ( attrname == sm_simplified_formula_ID) return true;
        return super.hasAttribute(attrname);
    }

    @Override
	public Object getAttribute(int attrname) {
        if ( attrname == sm_simplified_formula_ID) return sm_simplified_formula();
        return super.getAttribute(attrname);
    }

    @Override
	public void setAttribute(int attrname, Object attrvalue) {
        if ( attrname == sm_simplified_formula_ID) {sm_simplified_formula((Formula)attrvalue);return;}
        super.setAttribute(attrname, attrvalue);
    }
}