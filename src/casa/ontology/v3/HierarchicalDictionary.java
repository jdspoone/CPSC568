package casa.ontology.v3;

import casa.exceptions.IllegalOperationException;
import casa.ontology.Ontology;
import casa.ontology.OntologyEntity;
import casa.ontology.Relation;
//import casa.ontology.v3.CASAOntology.HierarchicalDictionary;
//import casa.ontology.v3.CASAOntology.HierarchicalDictionary.Applier;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

/**
	 * A dictionary that uses it container class as an owner and uses the
	 * owner to access objects from dictionaries or its parents' dictionaries.
	 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
	 * @param <T> The type of info to store in this dictionary
	 */
	abstract class HierarchicalDictionary<T extends OntologyEntity> implements Iterable<T> {
		/**
		 * 
		 */
		private final CASAOntology casaOntology;

		protected Ontology owner;
		
		/**
		 * Remembers the owner so that it can use the
	   * owner to access objects from dictionaries or its parents' dictionaries.
		 * @param owner The containing "this"
		 * @param casaOntology TODO
		 */
		public HierarchicalDictionary(CASAOntology casaOntology, Ontology owner) {
			this.casaOntology = casaOntology;
			this.owner = owner;
			invalidateSearchPathCache();
		}
		
		/**
		 * This map uses keys that are the UNQUALIFIED names (not the qualified "ontology:name" syntax).
		 */
		TreeMap<String,T> map = new TreeMap<String,T>(new StringComp());
		
		public Iterator<T> iterator() {
			Iterator<T> it = new Iterator<T>(){
				Iterator<T> curIter = map.values().iterator();
				
				Iterator<Iterator<T>> iterators = null;
		
				private void init() {
					if (cachedSearchPath==null) resetSearchPathCache();
					Vector<HierarchicalDictionary<T>> dictPath = cachedSearchPath;
					Vector<Iterator<T>> its = new Vector<Iterator<T>>();
					for (HierarchicalDictionary<T> dict: dictPath) {
						its.add(dict.map.values().iterator());
					}
					iterators = its.iterator();
				}
				
				private void pumpIter() {
					if (iterators == null) {
						init();
					}
					if (iterators!=null && iterators.hasNext() && !curIter.hasNext()) {
						curIter = iterators.next();
					}
					else
						curIter = null;
				}

				@Override
				public boolean hasNext() {
					if (curIter== null) return false;
					if (curIter.hasNext()) return true;
					pumpIter();
					return hasNext();
				}
				
				@Override
				public T next() {
					if (curIter== null) throw new NoSuchElementException();
					if (curIter.hasNext()) return curIter.next();
					pumpIter();
					return next();
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
			return it;
		}
    
		/**
		 * Defers  to {@link #put(Name, Object)} by constructing a {@link Name} out of the <em>key</em> String.
		 * @param key
		 * @param value
		 * @return the previous value associated with key, or null if there was no mapping for key. (A null return can also indicate that the map previously associated null with key.)
		 * @throws IllegalOperationException if the qualified syntax explicitly specifies a dictionary that is not the owner.
		 * @see {@link #put(Name, Object)}
		 */
		public T put(String key, T value) throws IllegalOperationException {
			return put(new Name(key), value);
		}
			
		/**
		 * Adds the specified key and value to the dictionary.  If the Name contains an ontology that refers to another ontology, 
		 * the parent ontologies are searched. 
		 * It's OK for the name to contain a null (or absent) ontology: it's just assumed to be this ontology.
		 * @param key
		 * @param value
		 * @return the previous value associated with key, or null if there was no mapping for key. (A null return can also indicate that the map previously associated null with key.)
		 * @throws IllegalOperationException if the qualified syntax explicitly specifies a dictionary that is not the owner.
		 */
		public T put(Name key, T value) throws IllegalOperationException {
			if (!consistencyCheck(key, value)) 
				throw new IllegalOperationException("key '"+key+"' and value '"+value+"' are inconsistent in ontology "+owner.getName()+" put operation");
			
			class MyApplier extends Applier<T,T,Name> {// applier = new Applier<T,T,Name>(key) {
				T value;
				MyApplier(Name name, T value) {super(name); param=name; this.value = value;}
				@Override
				public boolean apply(HierarchicalDictionary<T> dict) {
					if (param.getOntology()==null || param.getOntology()==dict.owner) {
						ret = map.put(param.getName(), value);
						return true;
					}
					return false; }};
					
		  MyApplier myApplier = new MyApplier(key,value);
		  applyAllCachedSupers(myApplier);
		  return (T)myApplier.getReturnValue();
		}
		
		/**
		 * Finds the object associated with this <em>key</em> in the dictionary.  If the Name contains an ontology that refers 
		 * to another ontology, and that is an ancestor ontology, the appropriate value is returned. 
		 * It's OK for the name to contain a null (or absent) ontology: it's just assumed to be either this ontology or one of the parent ontologies; in either
		 * case it is the value is returned according to the search rules specified in {@link #applyAllCachedSupers(Applier)}
		 * and {@link #applyAllSupers(Applier)}.
		 * @param key
		 * @return the value to which the specified key is mapped, or null if this map contains no mapping for the key
		 * @throws IllegalOperationException if the qualified syntax explicitly specifies a dictionary that is not the owner.
		 */
		public T get(Name key) {
			class MyApplier extends Applier<T,T,Name> {// applier = new Applier<T,T,Name>(key) {
				MyApplier(Name name) {super(name); param=name;}
				@Override
				public boolean apply(HierarchicalDictionary<T> dict) {
					if (param.getOntology()==null) {
						return (ret = dict.map.get(param.getName()))!=null;
					}
					if (param.getOntology()==dict.owner) {
						ret = dict.map.get(param.getName());
						return true;
					}
					return false; }};
					
		  MyApplier myApplier = new MyApplier(key);
		  applyAllCachedSupers(myApplier);
		  return (T)myApplier.getReturnValue();
		}
		
		/**
		 * Used by {@link #applyAllSupers(Applier)} to traverse the lattice of ontologies.
		 * Note that since ontologies are shared, caution must be taken not to run
		 * into concurrency issues when using marks.
		 */
		private int mark;
		
		/**
		 * Increments the value of {@link #mark} and recursively those of all its
		 * super dictionaries.
		 */
		private void incAllMarks() { //this can't use applyAllSupers because we WANT to travese all possible paths completely
			mark++;
		  for (CASAOntology so: this.casaOntology.superOntologies) {
		    HierarchicalDictionary<T> sd = getDictionary(so);
		    sd.incAllMarks();
		  }
		}

		/**
		 * Sets the value of {@link #mark} to zero and recursively those of all its
		 * super dictionaries.
		 */
		private void clearAllMarks() {
			mark = 0;
		  for (CASAOntology so: this.casaOntology.superOntologies) {
		    HierarchicalDictionary<T> sd = getDictionary(so);
		    sd.clearAllMarks();
		  }
		}
		
		/**
		 * Prepares for a search by {@link #applyAllSupers(Applier)} by recursively
		 * clearing all marks and then incrementing them.
		 */
		private void initSuperSearch() {
			clearAllMarks();
			incAllMarks();
			}
		
		/**
		 * Calls <em>applier</em>.apply() recursively to in all super dictionary in left-right
		 * order but with the constraint that no super dictionary is applied before
		 * all of it's sub dictionaries are applied.
		 * @param applier
		 */
		private boolean applyAllSupers(Applier applier) {
			T ret = null;
		  if (applier.apply(this)) return true;
		  for (CASAOntology so: this.casaOntology.superOntologies) {
		    HierarchicalDictionary<T> sd = getDictionary(so);
		    sd.mark--;
		    if (sd.mark<=0) { //defer searching a parent if some other branch shares it and hasn't reached it yet.
		    	if (sd.applyAllSupers(applier)) return true;
		    }
		  }
		  return false;
		}
		
		/**
		 * A linear recording of a correct search order of all the
		 * ancestor dictionaries.
		 */
		Vector<HierarchicalDictionary<T>> cachedSearchPath = null;

		/**
		 * Used to record the time of the last invalidation for comparison
		 * during a search using the cached search path -- 
		 * {@link #applyAllCachedSupers(Applier)}
		 */
		long invalidationTimeStamp = 0;
		
		/**
		 * Rebuilds the search path cache, {@link #cachedSearchPath}, by
		 * climbing the hierarchy and recording an equivalent linear
		 * version of the search path.  Because this must mark nodes
		 * as they are visited, this behaviour must synchronize on
		 * the CLASS of this dictionary, locking out all other similar
		 * dictionaries in the process.  Because we need to assure that
		 * multiple reset's don't happen at once, this method is synchronized
		 * to the instance, and will just return if the cachedSearchPath is
		 * NOT null.  Therefore, you need to set cachedSearchPath to null
		 * FIRST if you want to rebuild and exist cachedSearchPath.
		 */
		private synchronized void resetSearchPathCache() { // the method is synchronized with the instance
																											// because we don't want to re-search
			if (cachedSearchPath!=null) return;
			cachedSearchPath = new Vector<HierarchicalDictionary<T>>();
			Applier<T,T,T> myApplier = new Applier<T,T,T>(null) {
				@Override
				public boolean apply(HierarchicalDictionary<T> dict) {
					cachedSearchPath.add(dict);
					return false;}};
			synchronized (this.getClass()) {
			  initSuperSearch();
			  applyAllSupers(myApplier);
			}
		}
		
		/**
		 * Invalidate the search path by setting {@link #cachedSearchPath} to null
		 * and resetting {@link #invalidationTimeStamp} to the current time.
		 * @see #applyAllCachedSupers(Applier) 
		 */
		void invalidateSearchPathCache() {
			cachedSearchPath = null;
		  invalidationTimeStamp = System.currentTimeMillis();
		}
		
		/**
		 * Call {@link Applier#apply(HierarchicalDictionary)} to all the dictionaries
		 * in the cached path {@link #cachedSearchPath}. Note that if one the
		 * super-dictionaries has had it's search path invalidated after
		 * this search path has been invalidated, then we must invalidate
		 * ours, rebuild it and recursively start again.
		 * @param applier
		 */
		private synchronized void applyAllCachedSupers(Applier applier) {
			if (cachedSearchPath==null) resetSearchPathCache();
			if (applier.apply(this)) return;
			for (HierarchicalDictionary<T> sd: cachedSearchPath) {
				if (sd.invalidationTimeStamp>invalidationTimeStamp) {
					invalidateSearchPathCache();
					applyAllCachedSupers(applier);
					return;
				}
				if (applier.apply(sd)) return;
			}
		}
		
		/**
		 * A class used by {@link HierarchicalDictionary#applyAllCachedSupers(Applier)}
		 * and {@link HierarchicalDictionary#applyAllSupers(Applier)} to run
		 * an algorithm on every dictionary in the "upward" part of the lattice
		 * from this one.
		 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
		 *
		 * @param <T> The same type param as used in the enclosing {@link HierarchicalDictionary}
		 * @param <aRet> the type to be returned by the {@link #getReturnValue()} method
		 * @param <aParam> the type to be passed to the constructor.
		 */
		abstract class Applier<T extends OntologyEntity, aRet, aParam> {
			aRet ret;
			aParam param;
			public Applier(aParam param) {this.param = param;}
			public abstract boolean apply(HierarchicalDictionary<T> dict);
			public aRet getReturnValue() {return ret;}
		}

		/**
		 * Defers  to {@link #get(Name)} by constructing a {@link Name} out of the <em>key</em> String.
		 * @param key
		 * @return the value to which the specified key is mapped, or null if this map contains no mapping for the key
		 * @throws IllegalOperationException 
		 * @throws IllegalOperationException if the qualified syntax explicitly specifies a dictionary that is not the owner.
		 * @see {@link #get(Name)}
		 */
		public T get(String key) throws IllegalOperationException {
			return get(new Name(key/*.toUpperCase()*/));
		}

		/**
		 * Defers  to {@link #containsKey(Name)} by constructing a {@link Name} out of the <em>key</em> String.
		 * @param key
		 * @return true iff the specified key is mapped, or null if this map contains no mapping for the key
		 * @throws IllegalOperationException if the qualified syntax explicitly specifies a dictionary that is not the owner.
		 * @see {@link #containsKey(Name)}
		 */
		public boolean containsKey(String key) throws IllegalOperationException {
			return containsKey(new Name(key));
		}

		/**
		 * Determines if the key associated with this <em>key</em> in the dictionary.  If the Name contains an ontology that refers to another ontology, an exception is thrown. 
		 * It's OK for the name to contain a null (or absent) ontology: it's just assumed to be either this ontology or one of the parent ontologies; in either
		 * case the search is performed.
		 * @param key
		 * @return true iff the specified key is mapped, or null if this map contains no mapping for the key
		 * @throws IllegalOperationException if the qualified syntax explicitly specifies a dictionary that is not the owner.
		 */
		public boolean containsKey(Name key) {
			Ontology keyOnt = key.getOntology();
			String keyName = key.getName();
			if (keyOnt==null) {
			  boolean ret = map.containsKey(keyName);
			  if (ret) return ret;
			  for (CASAOntology so: this.casaOntology.superOntologies) {
			    HierarchicalDictionary<T> sd = getDictionary(so);
				  ret = sd.containsKey(key);
				  if (ret) return ret;
			  }
			  return false;
			}
			if (keyOnt==owner) return map.containsKey(keyName);
		  for (CASAOntology so: this.casaOntology.superOntologies) {
		    HierarchicalDictionary<T> sd = getDictionary(so);
			  boolean ret = sd.containsKey(key);
			  if (ret) return ret;
		  }	
		  return false;
		}


		/**
		 * @return the set of fully-qualified ("ontology:name" syntax) of keys contained in this dictionary (but not it's parent dictionaries). 
		 */
		public Set<String> keySet() {
			Set<String> ret = new TreeSet<String>();
			String ontName = owner.getName();
			for (String key: map.keySet()) {
				ret.add(ontName+Name.SEPARATOR_CHAR+key);
			}
			return ret;
		}
		
		/**
		 * @return the set of values (not keys) contained in this dictionary (but not it's parent dictionaries). 
		 */
		public Collection<T> values() {
			return map.values();
		}
		
		/**
		 * Subclasses should override this method to specify a consistency check between keys and values
		 * @param key
		 * @param value
		 * @return true is the key and value are consistent with one another
		 */
		protected boolean consistencyCheck(Name key, T value) {
			boolean ret = (owner.getName()+Name.SEPARATOR_CHAR+key.getName()).equals(value.getName());
			return ret;
			}

		/**
		 * Subclasses should override this method to specify how to retrieve super-dictionaries
		 * from the containing ontology.
		 * @param superOntology
		 * @return
		 */
		protected abstract HierarchicalDictionary<T> getDictionary(CASAOntology ontology);

	} //class HierarchicalDictionary