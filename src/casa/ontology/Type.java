package casa.ontology;

public interface Type extends OntologyEntity { //,Comparable<Type>{

	/**
	 * return true iff this object is marked as an individual
	 */
	public abstract boolean isIndividual();
	
	/**
	 * Traverses the hierarchy setting each node's <em>mark</em> element to the
	 * number of parents of the node.
	 */
	public void resetMark();
	
}