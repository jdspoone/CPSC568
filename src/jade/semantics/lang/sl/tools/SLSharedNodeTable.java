package jade.semantics.lang.sl.tools;

import jade.semantics.lang.sl.grammar.Node;
import jade.semantics.lang.sl.grammar.StringConstantNode;
import jade.semantics.lang.sl.grammar.SymbolNode;
import jade.semantics.lang.sl.grammar.WordConstantNode;
import jade.util.leap.HashMap;

public class SLSharedNodeTable {
	
	private HashMap stringConstantMap = new HashMap();
	private HashMap wordConstantMap = new HashMap();
	private HashMap symbolMap = new HashMap();
	
	synchronized public int hashCode(HashMap map, Object obj) {
		int hashCode = obj.hashCode();
		Node node = (Node)map.get(hashCode);
		while ( node != null && !node.getAttribute(StringConstantNode.lx_value_ID).equals(obj) ) {
			hashCode ++;
			node = (Node)map.get(hashCode);
		}
		return hashCode;
	}

	
	synchronized public StringConstantNode getStringConstantNode(String value) {
		int hc = hashCode(stringConstantMap, value);
		StringConstantNode node = (StringConstantNode)stringConstantMap.get(hc);
		if ( node == null ) {
			stringConstantMap.put(hc, node = new StringConstantNode(value, hc));
		}
		return node;
	}

	synchronized public WordConstantNode getWordConstantNode(String value) {
		int hc = hashCode(wordConstantMap, value);
		WordConstantNode node = (WordConstantNode)wordConstantMap.get(hc);
		if ( node == null ) {
			wordConstantMap.put(hc, node = new WordConstantNode(value, hc));
		}
		return node;
	}

	synchronized public SymbolNode getSymbolNode(String value) {
		int hc = hashCode(symbolMap, value);
		SymbolNode node = (SymbolNode)symbolMap.get(hc);
		if ( node == null ) {
			symbolMap.put(hc, node = new SymbolNode(value, hc));
		}
		return node;
	}

	// -------------------------------------------------------------
	//                  SINGLETON PATTERN DEFINITIONS
	// -------------------------------------------------------------
	static private final SLSharedNodeTable instance = new SLSharedNodeTable();
	
	static public final SLSharedNodeTable getInstance() {
		return instance;
	}

}
