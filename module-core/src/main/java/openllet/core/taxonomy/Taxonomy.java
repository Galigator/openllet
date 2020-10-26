package openllet.core.taxonomy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import openllet.atom.OpenError;
import openllet.core.exceptions.InternalReasonerException;
import openllet.core.taxonomy.TaxonomyUtils.TaxonomyKey;
import openllet.core.utils.Bool;
import openllet.shared.tools.Logging;

/**
 * Definition of taxonomies
 *
 * @param <T> type of taxon
 * @since 2.6.0
 */
public interface Taxonomy<T> extends Logging
{
	TaxonomyNode<T> getBottomNode();

	void setBottomNode(final TaxonomyNode<T> bottomNode);

	TaxonomyNode<T> getTopNode();

	void setTopNode(final TaxonomyNode<T> topNode);

	short getDepth();

	void setDepth(final short depth);

	int getTotalBranching();

	void setTotalBranching(int totalBranching);

	Map<T, TaxonomyNode<T>> getNodes();

	void setNodes(Map<T, TaxonomyNode<T>> nodes);

	default void addEquivalentNode(final T t, final TaxonomyNode<T> node)
	{
		node.addEquivalent(t);
		getNodes().put(t, node);
	}

	/**
	 * Add a collection of elements equivalent to an element already in the taxonomy.
	 *
	 * @param t
	 * @param eqs
	 */
	default void addEquivalents(final T t, final Collection<T> eqs)
	{

		assert getNodes().containsKey(t) : "Element " + t.toString() + " not in taxonomy";

		final TaxonomyNode<T> node = getNodes().get(t);
		for (final T eq : eqs)
		{
			assert !getNodes().containsKey(eq) : "Element " + eq.toString() + " alread in taxonomy";
			node.addEquivalent(eq);
			getNodes().put(eq, node);
		}
	}

	/**
	 * Add a node with known supers and subs. Any direct relations between subs and supers are removed.
	 *
	 * @param equivalents a non-empty set of equivalent elements defining the node (one of which becomes the label)
	 * @param sups collection of supers, all of which must already exist in the taxonomy
	 * @param subs collection of subs, all of which must already exist in the taxonomy
	 * @param hidden indicates hidden or not
	 * @return the new node
	 */
	default TaxonomyNode<T> addNode(final Collection<T> equivalents, final Collection<T> sups, final Collection<T> subs, final boolean hidden)
	{

		assert !equivalents.isEmpty() : "Taxonomy getNodes() must have at least one element";
		assert getNodes().keySet().containsAll(sups) : "At least one super element not in taxonomy";
		assert getNodes().keySet().containsAll(subs) : "At least one sub element not in taxonomy";

		final TaxonomyNode<T> node = new TaxonomyNode<>(equivalents, hidden);
		for (final T t : equivalents)
			getNodes().put(t, node);

		short depth = 1;

		// Super handling
		{
			/*
			 * Note the special case when no supers are provided and top is
			 * hidden. Top points to the new _node, but not the reverse
			 */
			if (sups.isEmpty())
			{
				if (getTopNode().isHidden())
				{
					getTopNode().addSub(node);
					if (getTopNode().getSubs().size() == 2)
						getTopNode().removeSub(getBottomNode());
				}
				else
					node.addSupers(Collections.singleton(getTopNode()));

				setTotalBranching(getTotalBranching() + 1);
			}
			else
			{
				final Set<TaxonomyNode<T>> supNodes = new HashSet<>();
				for (final T sup : sups)
				{
					final TaxonomyNode<T> supNode = getNodes().get(sup);
					if (supNode._depth >= depth)
						depth = (short) (supNode._depth + 1);
					supNodes.add(supNode);
				}
				node._depth = depth;
				if (depth > getDepth())
					setDepth(depth);
				node.addSupers(supNodes);

				setTotalBranching(getTotalBranching() + supNodes.size());
			}
		}

		// Sub handling
		{
			Set<TaxonomyNode<T>> subNodes;
			if (subs.isEmpty())
			{
				if (getBottomNode().isHidden())
				{
					getBottomNode().addSupers(Collections.singleton(node));
					getBottomNode().getSupers().removeAll(node.getSupers());
				}
				else
					node.addSub(getBottomNode());

				setTotalBranching(getTotalBranching() + 1);
			}
			else
			{
				subNodes = new HashSet<>();
				for (final T sub : subs)
					subNodes.add(getNodes().get(sub));
				node.addSubs(subNodes);

				setTotalBranching(getTotalBranching() + subNodes.size());
			}
		}

		node.removeMultiplePaths();

		return node;
	}

	default TaxonomyNode<T> addNode(final T t, final boolean hidden)
	{
		final TaxonomyNode<T> node = new TaxonomyNode<>(t, hidden);
		getTopNode().addSub(node);
		node.addSub(getBottomNode());
		getNodes().put(t, node);
		return node;
	}

	/**
	 * Add a sub/super relation
	 *
	 * @param sub
	 * @param sup
	 */
	default void addSuper(final T sub, final T sup)
	{

		assert getNodes().containsKey(sub) : "Sub element " + sub.toString() + " not in taxonomy";
		assert getNodes().containsKey(sup) : "Super element " + sup.toString() + " not in taxonomy";

		final TaxonomyNode<T> subNode = getNodes().get(sub);
		final TaxonomyNode<T> supNode = getNodes().get(sup);
		if (subNode.equals(supNode))
			throw new InternalReasonerException("Equivalent elements cannot have sub/super relationship");

		if (subNode.getSupers().size() == 1 && subNode.getSupers().iterator().next() == getTopNode())
			getTopNode().removeSub(subNode);

		if (supNode.getSubs().size() == 1 && supNode.getSubs().iterator().next() == getBottomNode())
			supNode.removeSub(getBottomNode());

		supNode.addSub(subNode);
	}

	/**
	 * Add a collection of supers to an element
	 *
	 * @param sub
	 * @param sups
	 */
	default void addSupers(final T sub, final Collection<T> sups)
	{

		assert getNodes().containsKey(sub) : "Sub element " + sub.toString() + " not in taxonomy";
		assert getNodes().keySet().containsAll(sups) : "At least one super element not in taxonomy";

		final TaxonomyNode<T> subNode = getNodes().get(sub);
		final Set<TaxonomyNode<T>> supNodes = new HashSet<>();
		for (final T sup : sups)
			supNodes.add(getNodes().get(sup));

		if (subNode.getSupers().size() == 1 && subNode.getSupers().contains(getTopNode()))
			getTopNode().removeSub(subNode);

		for (final TaxonomyNode<T> supNode : supNodes)
			if (supNode.getSubs().size() == 1 && supNode.getSubs().contains(getBottomNode()))
				supNode.removeSub(getBottomNode());

		subNode.addSupers(supNodes);
	}

	default void assertValid()
	{
		assert getTopNode().getSupers().isEmpty() : "Top _node in the taxonomy has parents";
		assert getBottomNode().getSubs().isEmpty() : "Bottom _node in the taxonomy has children";
	}

	/**
	 * Given a list of concepts, find all the Least Common Ancestors (LCA). Note that a taxonomy is DAG not a tree so we do not have a unique LCA but a set of
	 * LCA.
	 * <p>
	 * FIXME : does not work when one of the elements is an ancestor of the rest
	 * </p>
	 * <p>
	 * TODO : <code>what to do with equivalent classes?</code>
	 * </p>
	 * <p>
	 * TODO : <code>improve efficiency</code>
	 * </p>
	 *
	 * @param list
	 * @return the Least Common Ancestors
	 */
	default List<T> computeLCA(final List<T> list)
	{

		if (list.isEmpty())
			return null;

		// get the first concept
		T t = list.get(0);

		// add all its ancestor as possible LCA candidates
		final List<T> ancestors = new ArrayList<>(getFlattenedSupers(t, /* direct = */false));

		for (int i = 1; i < list.size() && ancestors.size() > 0; i++)
		{
			t = list.get(i);

			// take the intersection of possible candidates to get rid of
			// uncommon ancestors
			ancestors.retainAll(getFlattenedSupers(t, /* direct = */false));
		}

		final Set<T> toBeRemoved = new HashSet<>();

		// we have all common ancestors now remove the ones that have
		// descendants in the list
		for (final T a : ancestors)
		{

			if (toBeRemoved.contains(a))
				continue;

			final Set<T> supers = getFlattenedSupers(a, /* direct = */false);
			toBeRemoved.addAll(supers);
		}

		ancestors.removeAll(toBeRemoved);

		return ancestors;
	}

	default boolean contains(final T t)
	{
		return getNodes().containsKey(t);
	}

	/**
	 * Iterate over getNodes() in taxonomy (no specific order)returning pair of equivalence set and datum associated with {@code key} for each. Useful, e.g., to
	 * collect equivalence sets matching some condition on the datum (as in all classes which have a particular instances)
	 *
	 * @param key key associated with datum returned
	 * @return iterator over equivalence set, datum pairs
	 */
	Iterator<Map.Entry<Set<T>, Object>> datumEquivalentsPair(final TaxonomyKey key);

	/**
	 * Iterate down taxonomy in a _depth first traversal, beginning with class {@code c}, returning only datum associated with {@code _key} for each. Useful,
	 * e.g., to collect datum values in a transitive closure (as in all instances of a class).
	 *
	 * @param t starting location in taxonomy
	 * @param key _key associated with datum returned
	 * @return datum iterator
	 */
	Iterator<Object> depthFirstDatumOnly(final T t, final TaxonomyKey key);

	/**
	 * Returns all the classes that are equivalent to class c. Class c itself is included in the result.
	 *
	 * @param t class whose equivalent classes are found
	 * @return A set of ATerm objects
	 */
	default Set<T> getAllEquivalents(final T t)
	{
		final TaxonomyNode<T> node = getNodes().get(t);

		if (node == null)
			return new HashSet<>();

		return new HashSet<>(node.getEquivalents());
	}

	default Set<T> getClasses()
	{
		return getNodes().keySet();
	}

	/**
	 * Get datum on taxonomy elements associated with {@code _key}
	 *
	 * @param t identifies the taxonomy element
	 * @param key identifies the specific datum
	 * @return the datum (or {@code null} if none is associated with {@code _key})
	 */
	default Object getDatum(final T t, final TaxonomyKey key)
	{
		final TaxonomyNode<T> node = getNodes().get(t);
		return node == null ? null : node.getDatum(key);
	}

	/**
	 * Returns all the classes that are equivalent to class c. Class c itself is NOT included in the result.
	 *
	 * @param t class whose equivalent classes are found
	 * @return A set of ATerm objects
	 */
	default Set<T> getEquivalents(final T t)
	{
		final Set<T> result = getAllEquivalents(t);
		result.remove(t);

		return result;
	}

	/**
	 * As in {@link #getSubs(Object, boolean)} except the return value is the union of nested sets
	 *
	 * @param t
	 * @param direct
	 * @return a union of subs
	 */
	Set<T> getFlattenedSubs(final T t, final boolean direct);

	/**
	 * As in {@link #getSupers(Object, boolean)} except the return value is the union of nested sets
	 *
	 * @param t
	 * @param direct
	 * @return a union of supers
	 */
	Set<T> getFlattenedSupers(final T t, final boolean direct);

	default TaxonomyNode<T> getNode(final T t)
	{
		return getNodes().get(t);
	}

	/**
	 * Returns all the (named) subclasses of class c. The class c itself is not included in the list but all the other classes that are equivalent to c are put
	 * into the list. Also note that the returned list will always have at least one element, that is the BOTTOM concept. By definition BOTTOM concept is
	 * subclass of every concept. This function is equivalent to calling getSubClasses(c, true).
	 *
	 * @param t class whose subclasses are returned
	 * @return A set of sets, where each set in the collection represents an equivalence class. The elements of the inner class are ATermAppl objects.
	 */
	default Set<Set<T>> getSubs(final T t)
	{
		return getSubs(t, false);
	}

	/**
	 * Returns the (named) subclasses of class c. Depending on the second parameter the resulting list will include either all subclasses or only the direct
	 * subclasses. A class d is a direct subclass of c iff
	 * <ol>
	 * <li>d is subclass of c</li>
	 * <li>there is no other class x different from c and d such that x is subclass of c and d is subclass of x</li>
	 * </ol>
	 * The class c itself is not included in the list but all the other classes that are sameAs c are put into the list. Also note that the returned list will
	 * always have at least one element. The list will either include one other concept from the hierarchy or the BOTTOM concept if no other class is subsumed
	 * by c. By definition BOTTOM concept is subclass of every concept.
	 *
	 * @param t Class whose subclasses are found
	 * @param direct If true return only direct subclasses elese return all the subclasses
	 * @return A set of sets, where each set in the collection represents an equivalence class. The elements of the inner class are ATermAppl objects.
	 */
	Set<Set<T>> getSubs(final T t, final boolean direct);

	/**
	 * Returns all the superclasses (implicitly or explicitly defined) of class c. The class c itself is not included in the list. but all the other classes
	 * that are sameAs c are put into the list. Also note that the returned list will always have at least one element, that is TOP concept. By definition TOP
	 * concept is superclass of every concept. This function is equivalent to calling getSuperClasses(c, true).
	 *
	 * @param t class whose superclasses are returned
	 * @return A set of sets, where each set in the collection represents an equivalence class. The elements of the inner class are ATermAppl objects.
	 */
	default Set<Set<T>> getSupers(final T t)
	{
		return getSupers(t, false);
	}

	/**
	 * Returns the (named) superclasses of class c. Depending on the second parameter the resulting list will include either all or only the direct
	 * superclasses. A class d is a direct superclass of c iff
	 * <ol>
	 * <li>d is superclass of c</li>
	 * <li>there is no other class x such that x is superclass of c and d is superclass of x</li>
	 * </ol>
	 * The class c itself is not included in the list but all the other classes that are sameAs c are put into the list. Also note that the returned list will
	 * always have at least one element. The list will either include one other concept from the hierarchy or the TOP concept if no other class subsumes c. By
	 * definition TOP concept is superclass of every concept.
	 *
	 * @param t Class whose subclasses are found
	 * @param direct If true return all the superclasses else return only direct superclasses
	 * @return A set of sets, where each set in the collection represents an equivalence class. The elements of the inner class are ATermAppl objects.
	 */
	Set<Set<T>> getSupers(final T t, final boolean direct);

	Stream<Set<T>> supers(final T t, final boolean direct);

	default TaxonomyNode<T> getTop()
	{
		return getTopNode();
	}

	/**
	 * Checks if x is equivalent to y
	 *
	 * @param x Name of the first class
	 * @param y Name of the second class
	 * @return true if x is equivalent to y
	 */
	default Bool isEquivalent(final T x, final T y)
	{
		final TaxonomyNode<T> nodeX = getNodes().get(x);
		final TaxonomyNode<T> nodeY = getNodes().get(y);

		if (nodeX == null || nodeY == null)
			return Bool.UNKNOWN;
		else
			if (nodeX.equals(nodeY))
				return Bool.TRUE;
			else
				return Bool.FALSE;
	}

	/**
	 * Checks if x has an ancestor y.
	 *
	 * @param x Name of the _node
	 * @param y Name of the ancestor ode
	 * @return true if x has an ancestor y
	 */
	default Bool isSubNodeOf(final T x, final T y)
	{
		final TaxonomyNode<T> nodeX = getNodes().get(x);
		final TaxonomyNode<T> nodeY = getNodes().get(y);

		if (nodeX == null || nodeY == null)
			return Bool.UNKNOWN;
		else
			if (nodeX.equals(nodeY))
				return Bool.TRUE;

		if (nodeX.isHidden())
		{
			if (nodeY.isHidden())
				return Bool.UNKNOWN;
			else
				return getFlattenedSupers(x, /* direct = */false).contains(y) ? Bool.TRUE : Bool.FALSE;
		}
		else
			return getFlattenedSubs(y, false).contains(x) ? Bool.TRUE : Bool.FALSE;
	}

	void merge(final TaxonomyNode<T> node1, final TaxonomyNode<T> node2);

	/**
	 * Set a datum value associated with {@code key} on a taxonomy element
	 *
	 * @param t identifies the taxonomy element
	 * @param key identifies the datum
	 * @param value the datum
	 * @return previous _value of datum or {@code null} if not set
	 */
	default Object putDatum(final T t, final TaxonomyKey key, final Object value)
	{
		final TaxonomyNode<T> node = getNodes().get(t);
		if (node == null)
			throw new OpenError(t + " is an unknown class!");

		return node.putDatum(key, value);
	}

	/**
	 * Remove an element from the taxonomy.
	 *
	 * @param t
	 */
	default void remove(final T t)
	{
		assert getNodes().containsKey(t) : "Element not contained in taxonomy";

		final TaxonomyNode<T> node = getNodes().remove(t);
		if (node.getEquivalents().size() == 1)
		{
			final Collection<TaxonomyNode<T>> subs = node.getSubs();
			final Collection<TaxonomyNode<T>> supers = node.getSupers();
			node.disconnect();
			for (final TaxonomyNode<T> sup : supers)
				sup.addSubs(subs);
		}
		else
			node.removeEquivalent(t);
	}

	/**
	 * Walk through the super getNodes() of the given _node and when a cycle is detected merge all the getNodes() in that path
	 *
	 * @param node
	 */
	void removeCycles(final TaxonomyNode<T> node);

	default Object removeDatum(final T t, final TaxonomyKey key)
	{
		return getNode(t).removeDatum(key);
	}

	/**
	 * Clear existing supers for an element and set to a new collection
	 *
	 * @param t
	 * @param supers
	 */
	default void resetSupers(final T t, final Collection<T> supers)
	{

		assert getNodes().containsKey(t) : "Element " + t.toString() + " not in taxonomy";
		assert getNodes().keySet().containsAll(supers) : "Supers not all contained in taxonomy";

		final TaxonomyNode<T> node = getNodes().get(t);

		final List<TaxonomyNode<T>> initial = new ArrayList<>(node.getSupers());
		for (final TaxonomyNode<T> n : initial)
			n.removeSub(node);

		if (supers.isEmpty())
			getTopNode().addSub(node);
		else
		{
			final Set<TaxonomyNode<T>> added = new HashSet<>();
			for (final T sup : supers)
			{
				final TaxonomyNode<T> n = getNodes().get(sup);
				if (added.add(n))
					n.addSub(node);
			}
		}
	}

	/**
	 * Sort the getNodes() in the taxonomy using topological ordering starting from top to bottom.
	 *
	 * @param includeEquivalents If false the equivalents in a _node will be ignored and only the name of the _node will be added to the result
	 * @return List of _node names sorted in topological ordering
	 */
	default List<T> topologocialSort(final boolean includeEquivalents)
	{
		return topologocialSort(includeEquivalents, null);
	}

	/**
	 * Sort the getNodes() in the taxonomy using topological ordering starting from top to bottom.
	 *
	 * @param includeEquivalents If false the equivalents in a node will be ignored and only the name of the _node will be added to the result
	 * @param comparator comparator to use sort the getNodes() at same level, <code>null</code> if no special ordering is needed
	 * @return List of node names sorted in topological ordering
	 */
	default List<T> topologocialSort(final boolean includeEquivalents, final Comparator<? super T> comparator)
	{
		final Map<TaxonomyNode<T>, Integer> degrees = new HashMap<>();
		final Map<T, TaxonomyNode<T>> nodesPending = comparator == null ? new HashMap<>() : new TreeMap<>(comparator);
		final Set<TaxonomyNode<T>> nodesLeft = new HashSet<>();
		final List<T> nodesSorted = new ArrayList<>();

		getLogger().fine("Topological sort...");

		for (final TaxonomyNode<T> node : getNodes().values())
		{
			if (node.isHidden())
				continue;

			nodesLeft.add(node);
			final int degree = node.getSupers().size();
			if (degree == 0)
			{
				nodesPending.put(node.getName(), node);
				degrees.put(node, 0);
			}
			else
				degrees.put(node, degree);
		}

		for (int i = 0, size = nodesLeft.size(); i < size; i++)
		{
			if (nodesPending.isEmpty())
				throw new InternalReasonerException("Cycle detected in the taxonomy!");

			final TaxonomyNode<T> node = nodesPending.values().iterator().next();

			final int deg = degrees.get(node);
			if (deg != 0)
				throw new InternalReasonerException("Cycle detected in the taxonomy " + node + " " + deg + " " + nodesSorted.size() + " " + getNodes().size());

			nodesPending.remove(node.getName());
			nodesLeft.remove(node);
			if (includeEquivalents)
				nodesSorted.addAll(node.getEquivalents());
			else
				nodesSorted.add(node.getName());

			for (final TaxonomyNode<T> sub : node.getSubs())
			{
				final int degree = degrees.get(sub);
				if (degree == 1)
				{
					nodesPending.put(sub.getName(), sub);
					degrees.put(sub, 0);
				}
				else
					degrees.put(sub, degree - 1);
			}
		}

		if (!nodesLeft.isEmpty())
			throw new InternalReasonerException("Failed to sort elements: " + nodesLeft);

		getLogger().fine("done");

		return nodesSorted;
	}
}
