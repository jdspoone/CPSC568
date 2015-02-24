package casa.ontology.v3;

import java.util.Comparator;

class StringComp implements Comparator<String> {

	public int compare(String o1, String o2) {
		return o1.compareToIgnoreCase(o2);
	}
} // class NameComp