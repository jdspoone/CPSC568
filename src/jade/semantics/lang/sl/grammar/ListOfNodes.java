
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

//#PJAVA_EXCLUDE_BEGIN
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;


//import java.util.*;
/**
This abstract class is the base class of all list of nodes.
 It provides all basic mechanisms needed to manipulate a list of nodes.
*/
public class ListOfNodes extends Node {
    protected LinkedList _nodesList = new LinkedList();
    /**
    Constructor. It builds a empty list of nodes. 
    */
    public ListOfNodes() {super(0);}
    /**
    Constructor. It builds a list of nodes, containing all the given nodes. 
    @param nodes an array of nodes to insert in the list.
    */
    public ListOfNodes(Node[] nodes) {super(0); addAll(nodes);}
    /**
    Constructor. It builds a list of nodes, containing all the given nodes. 
    @param nodes a list of nodes to insert in the list.
    */
    public ListOfNodes(ListOfNodes nodes) {super(0); addAll(nodes);}
    /**
    This field represent the static ID of the class Node.
    */
    public static Integer ID = new Integer(10000);
    /**
    This method return the dynamic class ID of the node. It is the same
    for all nodes of the same type. This method which is automatically redefined 
    according the declaration rank of the corresponding operator in the grammar.
    */
    @Override
	public int getClassID(){
        return ID.intValue();
    }
    /**
    This method add a node within the list. 
    @param node node to be added.
    */
    public void add(Node node) {_nodesList.add(node);}
    /**
    This method add several nodes within the list. 
    @param nodes array of nodes to be added.
    */
    public void addAll(Node[] nodes) {for (int i=0; i<nodes.length; i++) {_nodesList.add(nodes[i]);}}
    /**
    This method add several nodes within the list. 
    @param nodes list of nodes to be added.
    */
    public void addAll(ListOfNodes nodes) {for (int i=0; i<nodes.size(); i++) {_nodesList.add(nodes.get(i));}}
    /**
    This method add several nodes within the list, starting at the index position. 
    @param index the index of the start position.
    @param nodes list of nodes to be added.
    */
    public void addAll(int index, ListOfNodes nodes)  {for (int i=0; i<nodes.size(); i++) {_nodesList.add(index+i, nodes.get(i));}}
    /**
    This method add a node within the list at a particular position. 
    @param index position where tu add the node.
    @param node the node to be added.
    */
    public void add(int index, Node node) {_nodesList.add(index, node);}
    /**
    This method remove a node from the list. 
    @param node the node to be removed.
    */
    public void remove(Node node) {_nodesList.remove(node);}
    /**
    This method remove a node from the list. 
    @param index the index of the node to be removed.
    @return the removed node.
    */
    public Node remove(int index) {return (Node)_nodesList.remove(index);}
    /**
    This method remove all nodes from the list. 
    */
    public void removeAll() {_nodesList.clear();}
    /**
    This method replace the node at the index position by a nea one. 
    @param index the index of the node to replace.
    @param node the new node.
    */
    @Override
	public void replace(int index, Node node) {_nodesList.remove(index); _nodesList.add(index, node);}
    /**
    This method replace all the nodes contained by the list by new nodes. 
    @param nodes an array containing the new nodes to be added.
    */
    public void replaceAll(Node[] nodes) {removeAll();addAll(nodes);}
    /**
    This method replace all the nodes contained by the list by new nodes. 
    @param nodes a list containing the new nodes to be added.
    */
    public void replaceAll(ListOfNodes nodes) {removeAll();addAll(nodes);}
    /**
    This method returns true if the node is already in the list. 
    @param node the node to check if it is present in the list.
    @return true if the node is already in the list.
    */
    public boolean contains(Node node) {return _nodesList.contains(node);}
    /**
    This method returns the node at a particular position. 
    @param index the index of the node to be returned.
    @return the node at the index position, or null.
    */
    public Node get(int index) {return (Node) _nodesList.get(index);}
    /**
    This method returns the last node of the list. 
    @return the last node of the list, or null if empty.
    */
    public Node getLast() {return (Node) _nodesList.get(_nodesList.size()-1);}
    /**
    This method returns the first node of the list. 
    @return the first node of the list, or null if empty.
    */
    public Node getFirst() {return (Node) _nodesList.get(0);}
    /**
    This method returns the size of the list. 
    @return the size of the list.
    */
    public int size() {return _nodesList.size();}
    /**
    This method returns if the list is empty. 
    @return true if the list is empty.
    */
    public boolean isEmpty() {return _nodesList.isEmpty();}
    /**
    This method sort the list with respect to a comparator, 
    or to the typographical order of the strings representing the nodes.
    One should notice the list is not duplicated.
    @param comparator the comparator to use to sort the list.
    */
    public void sort(Comparator comparator) {
        if ( comparator == null ) {
            Collections.sort(_nodesList, new Comparator() {
                public int compare(Object o1, Object o2) {return (o1.toString().compareTo(o2.toString()));}});
        }
        else {
            Collections.sort(_nodesList, comparator);
        }
    }
    /**
    This method returns the list as a collection. 
    @return the list as a collection.
    */
    public Collection asACollection() {return _nodesList;}
    /**
    This method returns the list of nodes as a list. 
    @return the list of nodes as a list.
    */
    public List asAList() {return _nodesList;}
    /**
    This method returns the nodes contained within the list. 
    @return the nodes of the list.
    */
    @Override
	public Node[] children() {
        Node[] result = new Node[_nodesList.size()];
        for(int i=0;i<result.length;i++) {
            result[i] = (Node)_nodesList.get(i);
        }
        return result;
    }
    /**
    This method returns a vector containing nodes ok for the given finder. 
    @param finder the finder to operate on each node of the list.
    @return the vector containing the found nodes.
    */
    public Vector find(NodeFinder finder) {
        Vector result = new Vector();
        for (int i=0; i<_nodesList.size(); i++) {
            if ( finder.isOk((Node)_nodesList.get(i)) ) {
                result.addElement(_nodesList.get(i));
            }
        }
        return result;
    }
    /**
    This method dumps the graph this node of which is the root. 
    @param tab a prefix string used to display each node of the graph.
    */
    @Override
	public void dump(String tab) {
        System.out.println(tab+getClass().getName()+"<"+hashCode()+">");
        for (int i=0; i<_nodesList.size(); i++) {
            ((Node)_nodesList.get(i)).dump(tab+"  ");
        }
    }
    /**
    This method is part of the implementation of the visitor design pattern. 
    @param visitor the visitor to apply on the list.
    */
    @Override
	public void accept(Visitor visitor) {
        childrenAccept(visitor);
    }
    /**
    This method is part of the implementation of the visitor design pattern. 
    @param v the visitor to apply on each node of the list.
    */
    @Override
	public void childrenAccept(Visitor v) {
        for (int i=0; i<_nodesList.size(); i++) {
            ((Node)_nodesList.get(i)).accept(v);
        }
    }
    /**
    This method return a clone of the list.
    @return a new recreated graph.
    */
    @Override
	public Node getClone(HashMap clones)
    {
        Node clone = new ListOfNodes();
        clone.copyValueOf(this, clones);
        return clone;
    }
    /**
    This method replace the list entirely with the other list.
    @param n the new list of nodes.
    */
    @Override
	public void copyValueOf(Node n, HashMap clones) {
        if (n instanceof ListOfNodes) {
            _nodesList = new LinkedList();
            for (int i=0; i<((ListOfNodes)n)._nodesList.size(); i++) {
                Node original = (Node)((ListOfNodes)n)._nodesList.get(i);
                if ( original != null ) {
                    Node clone = (Node)clones.get(original);
                    if ( clone == null ) {
                        clone = original.getClone(clones);
                        clones.put(original, clone);
                    }
                add(clone);
                }
            }
        }
    }
    /**
    */
    @Override
	protected void dofind(Class[] nodeClasses, int attribut, Object value, ListOfNodes result, boolean all) {
        for (int i=0; i<_nodesList.size(); i++) {
            ((Node)_nodesList.get(i)).dofind(nodeClasses, attribut, value, result, all);
        }
    }
    /**
    This method fills the result list with all nodes of the graph  
    instance of the <b>nodeClass</b>.  
    @param nodeClass the class the return nodes must be instance of.
    @param result the result list.
    @return true if the returned list is not empty.
    */
    @Override
	public boolean childrenOfKind(Class[] nodeClasses, ListOfNodes result) {
        for (int i=0; i<_nodesList.size(); i++) {
            ((Node)_nodesList.get(i)).childrenOfKind(nodeClasses, result);
        }
        return (result.size() != 0);
    }
    /**
    This method return a string representing the list. Each 
    node of the list is represented, separated from the previous and next by a ','.
    @return the image string of the list.
    */
    @Override
	public String toString() {
        String result = "";
        for (int i=0; i<_nodesList.size(); i++) {
            if ( i != 0 ) {
                result += ", ";
            }
            result += _nodesList.get(i).toString();
        }
        return result;
    }
    
    /**
    This method returns an iterator object to iterate on the list nodes.
    @return an iterator object
    */
   public Iterator iterator() {
   	return _nodesList.iterator();
   }
}
