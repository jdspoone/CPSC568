
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

import java.util.Vector;
import java.util.HashMap;
//#PJAVA_EXCLUDE_END
/*#PJAVA_INCLUDE_BEGIN
import jade.util.leap.Collection;
import jade.util.leap.Comparable;
import jade.util.leap.LinkedList;
import jade.util.leap.List;
import jade.util.leap.HashMap;
#PJAVA_INCLUDE_END*/


//import java.util.*;
/**
This abstract class is the base class of all nodes belonging to a directed graph
 representing a particular abstract syntax tree. It provides all basic mechanisms
needed to manipulate a node.
*/
public abstract class Node implements Comparable {
    /**
    This interface defines node operations that can be redefined
     using the <b><code>addOperations</code></b> method.
    Notice the <b><code>addOperations</code></b> is a class method that 
    must be called for a particular class before creating any node of this class.
    */
    public interface Operations {
        public boolean equals(Node node1,Node node2);
        public int compare(Node node1,Node node2);
        public String toString(Node node);
        public void initNode(Node node);
    }
    static HashMap _operations = new HashMap();
    Node.Operations _thisoperations = null;
    /**
    This field represent the static ID of the class Node.
    */
    public static Integer ID = new Integer(0);
    /**
    This method return the dynamic class ID of the node. It is the same
    for all nodes of the same type. This method which is automatically redefined 
    according the declaration rank of the corresponding operator in the grammar.
    */
    public int getClassID(){
        return ID.intValue();
    }
    /**
    This method allows to redefine some operations for a particular class of nodes.
    @param classID the id of the class the operations of which we want to redefine.
    @param operations the new operations definition.
    */
    public static void addOperations(Integer classID, Node.Operations operations){
        _operations.put(classID, operations);
    }
    /**
    This method returns the object implementing the operations associated to this node.
    @return the object implementing the operations of this node.
    */
    public Node.Operations getOperations(){
            return (Node.Operations)_operations.get(ID);
    }
    /**
    This method allows to redefine operations on several node classes.
    @param class_operations_array an array containing alternatively a node class ID
     and a Node.Operations class.
    @see Node#addOperations(Integer, Node.Operations)
    */
    public static void installOperations(Object[] class_operations_array){
        // class_operations_array is an array alterning Integer and Node.Operations objects.
        _operations.clear();
        for (int i=0; i<class_operations_array.length; i++) {
            _operations.put(class_operations_array[i++], class_operations_array[i]);
        }
    }
    protected Node[] _nodes = new Node[0];
    protected Node(int capacity) {
        _nodes = new Node[capacity];
        _thisoperations = getOperations();
    }
    /**
    This method returns the children of a the node.
    @return chidlren of the node 
    */
    public Node[] children() {return _nodes;}
    /**
    This method allow to replace a child of the node. Notice
     this method performs no check on the type of the new node.
    For this reason, the use of a <b>as_</b> method should be preferred each
     time it is possible.
    @param index the index of the child to replace.
    @param node the new node.
    */
    public void replace(int index, Node node) {_nodes[index] = node;}
    /**
    This method dumps the graph this node of which is the root. 
    @param tab a prefix string used to display each node of the graph.
    */
    public void dump(String tab) {
        System.out.println(tab+getClass().getName()+"<"+hashCode()+">");
        for (int i=0; i<_nodes.length; i++) {
            if (_nodes[i] != null ) _nodes[i].dump(tab+"  ");
        }
    }
    /**
    This method is part of the implementation of the visitor design pattern. 
    @param v the visitor to apply on chidlren.
    */
    public void childrenAccept(Visitor v) {
        for (int i=0; i<_nodes.length; i++) {
            if (_nodes[i] != null ) _nodes[i].accept(v);
        }
    }
    /**
    This method return a clone of the graph this node of which is the root.
    @return a new recreated graph.
    */
    public Node getClone() {return getClone(new HashMap());}
    
    public abstract Node getClone(HashMap clones);
    /**
    This method replace the graph entirely with the other graph the root is n.
    @param n the root node of the new graph.
    */
    public void copyValueOf(Node n) {copyValueOf(n, new HashMap());}
    
    public void copyValueOf(Node n, HashMap clones) {
        for (int i=0; i<_nodes.length; i++) {
            Node original = n._nodes[i];
            if ( original != null ) {
                Node clone = (Node)clones.get(original);
                if ( clone == null ) {
                    clone = original.getClone(clones);
                    clones.put(original,clone);
                }
            _nodes[i]= clone;
            }
        }
    }
    Vector _observers = null;
    /**
    This method call the <b>nodeChanged</b> method on each observer 
    attached to this node.
    */
    public void notifyChanges() {
        if ( _observers != null ) {
            for (int i = 0;i<_observers.size();i++) {
                ((NodeObserver)_observers.elementAt(i)).nodeChanged(this);
            }
        }
    }
    /**
    This method allows to add an observer for this node.
    @param observer the observer to add.
    */
    public void addObserver(NodeObserver observer) {
        if ( _observers == null ) {
            _observers = new Vector();
        }
        _observers.addElement(observer);
    }
    /**
    This method allows to remove an observer of this node.
    @param observer the observer to remove.
    */
    public void removeObserver(NodeObserver observer) {
        if ( _observers != null ) {
            _observers.removeElement(observer);
        }
    }
    /**
    This method returns true is the node holds the attribute the name of which is given as parameter. 
    @param attrname the name of the attribute (or its hashCode) we are looking for.
    @return true if the node holds this attribute.
    */
    public boolean hasAttribute(String attrname) {return hasAttribute(attrname.hashCode());}
    public boolean hasAttribute(int attrname) {return false;}
    /**
    This method returns an object representing the value of the attribute if exists. 
    @param attrname the name of the attribute (or its hashCode) we are looking for.
    @return the object representing the value of the attribute, or null if the attribute doesn't exist.
    */
    public Object getAttribute(String attrname) {return getAttribute(attrname.hashCode());}
    public Object getAttribute(int attrname) {return null;}
    /**
    This method set the value of the attribute if exists. 
    @param attrname the name of the attribute (or its hashCode) we are looking for.
    @param attrvalue the new value of this attribute.
    */
    public void setAttribute(String attrname, Object attrvalue) {setAttribute(attrname.hashCode(), attrvalue);}
    public void setAttribute(int attrname, Object attrvalue) {}
    /**
    This method is part of the implementation of the visitor design pattern. 
    @param visitor the visitor to apply on this node.
    */
    public abstract void accept(Visitor visitor);
    /**
    This method fills the result list with all nodes of the graph  
    instance of the <b>nodeClass</b>.  
    @param nodeClass the name of the class the return nodes must be instance of.
    @param result the result list.
    @return true if the list is not empty.
    */
    public boolean childrenOfKind(String nodeClass, ListOfNodes result) {
        try {
            return childrenOfKind(Class.forName(nodeClass), result);
        }
        catch (ClassNotFoundException cnf) {return false;}
    }
    /**
    This method fills the result list with all nodes of the graph  
    instance of the <b>nodeClass</b>.  
    @param nodeClass the class the return nodes must be instance of.
    @param result the result list.
    @return true if the returned list is not empty.
    */
    public boolean childrenOfKind(Class nodeClass, ListOfNodes result) {
        return childrenOfKind(new Class[] {nodeClass}, result);
    }
    /**
    This method fills the result list with all nodes of the graph  
    instance of one of the <b>nodeClasses</b>.  
    @param nodeClasses the return nodes must be instance of one of these classes.
    @param result the result list.
    @return true if the returned list is not empty.
    */
    public boolean childrenOfKind(Class[] nodeClasses, ListOfNodes result) {
        for(int i=0; i<nodeClasses.length; i++) {
            if ( nodeClasses[i].isInstance(this) ) {
                result.add(this);
                break;
            }
        }
        for (int i=0; i<_nodes.length; i++) {
            if (_nodes[i] != null ) _nodes[i].childrenOfKind(nodeClasses, result);
        }
        return (result.size() != 0);
    }
    /**
    This method fills the result list with all nodes of the graph  
    instance of the <b>nodeClass</b>, holding an attribute the value 
    of which is the <b>value</b> parameter. 
    @param nodeClass the class the return nodes must be instance of.
    @param attribut the name of the attribute (or its hashcode).
    @param value the expected value of this attribute for the return nodes.
    @param result the result list.
    @param all if true, this method looks for all nodes satisfaying the constraints,
    otherwise it return the first found node.
    @return true if the returned list is not empty.
    */
    public boolean find(Class nodeClass, String attribut, Object value, ListOfNodes result, boolean all) {
        return find(new Class[] {nodeClass}, attribut, value, result, all);
    }
    public boolean find(Class nodeClass, int attribut, Object value, ListOfNodes result, boolean all) {
        return find(new Class[] {nodeClass}, attribut, value, result, all);
    }
    /**
    This method fills the result list with all nodes of the graph  
    instance of one of the <b>nodeClasses</b>, holding an attribute named  <b>attribut</b> the value 
    of which is <b>value</b> parameter. 
    @param nodeClasses the return nodes must be instance of one of these classes.
    @param attribut the name of the attribute.
    @param value the expected value of this attribute for the return nodes.
    @param result the result list.
    @param all if true, this method looks for all nodes satisfaying the constraints,
    otherwise it return the first found node.
    @return true if the returned list is not empty.
    */
    public boolean find(Class[] nodeClasses, String attribut, Object value, ListOfNodes result, boolean all) {
        dofind(nodeClasses, attribut.hashCode(), value, result, all);
        return (result.size() != 0);
    }
    public boolean find(Class[] nodeClasses, int attribut, Object value, ListOfNodes result, boolean all) {
        dofind(nodeClasses, attribut, value, result, all);
        return (result.size() != 0);
    }
    protected void dofind(Class[] nodeClasses, int attribut, Object value, ListOfNodes result, boolean all) {
        for (int i=0; i<nodeClasses.length; i++) {
            if ( nodeClasses[i].isInstance(this) ) {
                if ( hasAttribute(attribut) ) {
                    Object attributValue = getAttribute(attribut);
                    if ( attributValue == value || (attributValue != null &&  attributValue.equals(value)) ) {
                        result.add(this);
                    }
                }
                break;
            }
        }
        for (int i=0; i<_nodes.length && (all || result.size()==0); i++) {
            if (_nodes[i] != null ) _nodes[i].dofind(nodeClasses, attribut, value, result, all);
        }
    }
    /**
    This method return if the node equals another node.  
    @param object the node to compare with.
    @return true if the 2 nodes are equal.
    */
    @Override
	public boolean equals(Object object) {
        if ( _thisoperations == null ) {
            _thisoperations = getOperations();
        }
        if ( _thisoperations != null ) {
            return _thisoperations.equals(this, (Node)object);
        }
        else {
            return super.equals(object);
        }
    }
    /**
    This method return if the node equals another node.  
    @param object the node to compare with.
    @return true if the 2 nodes are equal.
    */
    public int compareTo(Object object) {
        if ( object instanceof Node ) {
            return compare((Node)object);
        }
        else {
        throw new ClassCastException();
        }
    }
    /**
    This method compares the node with the one given as an argument.  
    @param other the other node to compare the node with.
    @return -1 if the node is less than the other node, 
    0 if the two nodes are equals and 1 if the node is greater than the other.
    */
    public int compare(Node other) {
        int result = 0;
        if ( getClassID() < other.getClassID() ) {
            result = -1;
        } else if (getClassID() > other.getClassID() ) {
            result = 1;
        } else { // Same classes
            if ( _thisoperations == null ) {
                _thisoperations = getOperations();
            }
            if ( _thisoperations != null ) {
                result = _thisoperations.compare(this, other);
            }
            if ( result == 0 ) {
                Node[] children = children();
                Node[] otherchildren = other.children();
                if ( children.length < otherchildren.length ) {
                    result = -1;
                } else if ( children.length > otherchildren.length ) {
                    result = 1;
                } else {
                    for (int i=0; i<children.length; i++) {
                        result = children[i].compare(otherchildren[i]);
                        if ( result != 0 ) {
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }
    /**
    This method return a string representing the node.  
    @return the image string of the node.
    */
    @Override
	public String toString() {
        if ( _thisoperations == null ) {
            _thisoperations = getOperations();
        }
        if ( _thisoperations != null ) {
            return _thisoperations.toString(this);
        }
        else {
            return super.toString();
        }
    }
    /**
    This method is called to initialize the current node if needed.  
    In particular, it should be redefined to perform correct initialization of the semantic attributes. 
    */
    public void initNode() {
        if ( _thisoperations == null ) {
            _thisoperations = getOperations();
        }
        if ( _thisoperations != null ) {
            _thisoperations.initNode(this);
        }
    }
}
