// Portions Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// Clark & Parsia, LLC parts of this source code are available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com
//
// ---
// Portions Copyright (c) 2003 Ron Alford, Mike Grove, Bijan Parsia, Evren Sirin
// Alford, Grove, Parsia, Sirin parts of this source code are available under the terms of the MIT License.
//
// The MIT License
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to
// deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.

package openllet.core.taxonomy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;
import openllet.core.exceptions.InternalReasonerException;
import openllet.core.taxonomy.TaxonomyUtils.TaxonomyKey;
import openllet.core.utils.CollectionUtils;
import openllet.shared.tools.Log;

/**
 * @param <T> kind of Node
 * @since 2.6.0
 */
public class TaxonomyImpl<T> implements Taxonomy<T>
{
	private class DatumEquivalentsPairIterator<U> implements Iterator<Map.Entry<Set<U>, Object>>
	{

		private final Iterator<TaxonomyNode<U>> _i;
		private final Object _key;

		public DatumEquivalentsPairIterator(final TaxonomyImpl<U> t, final Object key)
		{
			this._key = key;
			_i = t.getNodes().values().iterator();
		}

		@Override
		public boolean hasNext()
		{
			return _i.hasNext();
		}

		@Override
		public Entry<Set<U>, Object> next()
		{
			final TaxonomyNode<U> current = _i.next();
			return new SimpleImmutableEntry<>(Collections.unmodifiableSet(current.getEquivalents()), current.getDatum(_key));
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}

	private class DepthFirstDatumOnlyIterator<U> implements Iterator<Object>
	{

		private final Object _key;
		private final List<TaxonomyNode<U>> _pending;
		private final Set<TaxonomyNode<U>> _visited;

		public DepthFirstDatumOnlyIterator(final TaxonomyImpl<U> t, final U u, final Object key)
		{
			this._key = key;
			_visited = new HashSet<>();
			_pending = new ArrayList<>();
			final TaxonomyNode<U> node = t.getNode(u);
			if (node != null)
				_pending.add(node);
		}

		@Override
		public boolean hasNext()
		{
			return !_pending.isEmpty();
		}

		@Override
		public Object next()
		{
			if (_pending.isEmpty())
				throw new NoSuchElementException();

			final TaxonomyNode<U> current = _pending.remove(_pending.size() - 1);
			_visited.add(current);
			for (final TaxonomyNode<U> sub : current.getSubs())
				if (!_visited.contains(sub))
					_pending.add(sub);

			return current.getDatum(_key);
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}

	private class SimpleImmutableEntry<K, V> implements Map.Entry<K, V>
	{

		private final K _key;
		private final V _value;

		public SimpleImmutableEntry(final K key, final V value)
		{
			super();
			this._key = key;
			this._value = value;
		}

		@Override
		public K getKey()
		{
			return _key;
		}

		@Override
		public V getValue()
		{
			return _value;
		}

		@Override
		public V setValue(final V value)
		{
			throw new UnsupportedOperationException();
		}
	}

	public static final Logger _logger = Log.getLogger(TaxonomyImpl.class);

	private static final boolean SUB = true;

	private static final boolean SUPER = false;

	public static final boolean TOP_DOWN = true;

	protected volatile TaxonomyNode<T> _bottomNode;
	protected volatile Map<T, TaxonomyNode<T>> _nodes;
	protected volatile TaxonomyNode<T> _topNode;

	protected volatile short _depth = 0;
	protected volatile int _totalBranching = 0;

	@Override
	public Logger getLogger()
	{
		return _logger;
	}

	@Override
	public TaxonomyNode<T> getBottomNode()
	{
		return _bottomNode;
	}

	@Override
	public void setBottomNode(final TaxonomyNode<T> bottomNode)
	{
		_bottomNode = bottomNode;
	}

	@Override
	public TaxonomyNode<T> getTopNode()
	{
		return _topNode;
	}

	@Override
	public void setTopNode(final TaxonomyNode<T> topNode)
	{
		_topNode = topNode;
	}

	@Override
	public short getDepth()
	{
		return _depth;
	}

	@Override
	public void setDepth(final short depth)
	{
		_depth = depth;
	}

	@Override
	public int getTotalBranching()
	{
		return _totalBranching;
	}

	@Override
	public void setTotalBranching(final int totalBranching)
	{
		this._totalBranching = totalBranching;
	}

	@Override
	public Map<T, TaxonomyNode<T>> getNodes()
	{
		return _nodes;
	}

	@Override
	public void setNodes(final Map<T, TaxonomyNode<T>> nodes)
	{
		_nodes = nodes;
	}

	public TaxonomyImpl()
	{
		this(null, null, null);
	}

	public TaxonomyImpl(final Collection<T> elements, final T top, final T bottom)
	{
		_nodes = CollectionUtils.makeMap();

		if (top == null)
			_topNode = new TaxonomyNode<>((T) null, /* hidden = */true);
		else
		{
			_topNode = new TaxonomyNode<>(top, /* hidden = */false);
			_nodes.put(top, _topNode);
		}

		if (bottom == null)
			_bottomNode = new TaxonomyNode<>((T) null, /* hidden = */true);
		else
		{
			_bottomNode = new TaxonomyNode<>(bottom, /* hidden = */false);
			_nodes.put(bottom, _bottomNode);
		}

		if (elements == null || elements.isEmpty())
			_topNode.addSub(_bottomNode);
		else
			for (final T t : elements)
				addNode(t, /* hidden = */false);

		// precaution to avoid creating an invalid taxonomy is now done by
		// calling assertValid function because the taxonomy might be invalid
		// during the merge operation but it is guaranteed to be valid after
		// the merge is completed. so we check for validity at the very _end
		// TOP_NODE.setSupers( Collections.EMPTY_LIST );
		// BOTTOM_NODE.setSubs( Collections.EMPTY_LIST );
	}

	/**
	 * Iterate over nodes in taxonomy (no specific order)returning pair of equivalence set and datum associated with {@code key} for each. Useful, e.g., to
	 * collect equivalence sets matching some condition on the datum (as in all classes which have a particular instances)
	 *
	 * @param key key associated with datum returned
	 * @return iterator over equivalence set, datum pairs
	 */
	@Override
	public Iterator<Map.Entry<Set<T>, Object>> datumEquivalentsPair(final TaxonomyKey key)
	{
		return new DatumEquivalentsPairIterator<>(this, key);
	}

	/**
	 * Iterate down taxonomy in a _depth first traversal, beginning with class {@code c}, returning only datum associated with {@code _key} for each. Useful,
	 * e.g., to collect datum values in a transitive closure (as in all instances of a class).
	 *
	 * @param t starting location in taxonomy
	 * @param key _key associated with datum returned
	 * @return datum iterator
	 */
	@Override
	public Iterator<Object> depthFirstDatumOnly(final T t, final TaxonomyKey key)
	{
		return new DepthFirstDatumOnlyIterator<>(this, t, key);
	}

	/**
	 * As in {@link #getSubs(Object, boolean)} except the return _value is the union of nested sets
	 */
	@Override
	public Set<T> getFlattenedSubs(final T t, final boolean direct)
	{
		return getFlattenedSubSupers(t, direct, SUB);
	}

	/**
	 * Use {@link #getFlattenedSubs(Object, boolean)} or {@link #getFlattenedSupers(Object, boolean)} this method will become private
	 */
	private Set<T> getFlattenedSubSupers(final T t, final boolean direct, final boolean subOrSuper)
	{
		TaxonomyNode<T> node = _nodes.get(t);

		final Set<T> result = new HashSet<>();

		final List<TaxonomyNode<T>> visit = new ArrayList<>();
		visit.addAll(subOrSuper == SUB ? node.getSubs() : node.getSupers());

		for (int i = 0; i < visit.size(); i++)
		{
			node = visit.get(i);

			if (node.isHidden())
				continue;

			final Set<T> add = node.getEquivalents();
			result.addAll(add);

			if (!direct)
				visit.addAll(subOrSuper == SUB ? node.getSubs() : node.getSupers());
		}

		return result;
	}

	/**
	 * As in {@link #getSupers(Object, boolean)} except the return _value is the union of nested sets
	 */
	@Override
	public Set<T> getFlattenedSupers(final T t, final boolean direct)
	{
		return getFlattenedSubSupers(t, direct, SUPER);
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
	@Override
	public Set<Set<T>> getSubs(final T t, final boolean direct)
	{
		return getSubSupers(t, direct, SUB);
	}

	/**
	 * Use {@link #getSubs(Object, boolean)} or {@link #getSupers(Object, boolean)}.
	 */
	private Set<Set<T>> getSubSupers(final T t, final boolean direct, final boolean subOrSuper)
	{
		TaxonomyNode<T> node = _nodes.get(t);

		if (node == null)
			return Collections.emptySet();

		final Set<Set<T>> result = new HashSet<>();

		final List<TaxonomyNode<T>> visit = new ArrayList<>();
		visit.addAll(subOrSuper == SUB ? node.getSubs() : node.getSupers());

		for (int i = 0; i < visit.size(); i++)
		{
			node = visit.get(i);

			if (node.isHidden())
				continue;

			final Set<T> add = new HashSet<>(node.getEquivalents());
			if (!add.isEmpty())
				result.add(add);

			if (!direct)
				visit.addAll(subOrSuper == SUB ? node.getSubs() : node.getSupers());
		}

		return result;
	}

	/**
	 * Returns all the superclasses (implicitly or explicitly defined) of class c. The class c itself is not included in the list. but all the other classes
	 * that are sameAs c are put into the list. Also note that the returned list will always have at least one element, that is TOP concept. By definition TOP
	 * concept is superclass of every concept. This function is equivalent to calling getSuperClasses(c, true).
	 *
	 * @param t class whose superclasses are returned
	 * @return A set of sets, where each set in the collection represents an equivalence class. The elements of the inner class are ATermAppl objects.
	 */
	@Override
	public Set<Set<T>> getSupers(final T t)
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
	@Override
	public Set<Set<T>> getSupers(final T t, final boolean direct)
	{
		return getSubSupers(t, direct, SUPER);
	}

	@Override
	public Stream<Set<T>> supers(final T t, final boolean direct)
	{
		return getSubSupers(t, direct, SUPER).stream();
	}

	@Override
	public TaxonomyNode<T> getTop()
	{
		return _topNode;
	}

	@Override
	public void merge(final TaxonomyNode<T> node1, final TaxonomyNode<T> node2)
	{
		final List<TaxonomyNode<T>> mergeList = new ArrayList<>(2);
		mergeList.add(node1);
		mergeList.add(node2);

		final TaxonomyNode<T> node = mergeNodes(mergeList);

		removeCycles(node);
	}

	private TaxonomyNode<T> mergeNodes(final List<TaxonomyNode<T>> mergeList)
	{

		assert mergeList.size() > 1 : "Attempt to merge less than two _nodes";

		_logger.finer(() -> "Merge " + mergeList);

		TaxonomyNode<T> node = null;
		if (mergeList.contains(_topNode))
			node = _topNode;
		else
			if (mergeList.contains(_bottomNode))
				node = _bottomNode;
			else
				node = mergeList.get(0);

		final Set<TaxonomyNode<T>> merged = new HashSet<>();
		merged.add(node);

		for (final TaxonomyNode<T> other : mergeList)
		{

			if (merged.contains(other))
				continue;
			else
				merged.add(other);

			for (final TaxonomyNode<T> sub : other.getSubs())
				if (sub != _bottomNode && !mergeList.contains(sub))
				{
					if (node.getSubs().size() == 1 && node.getSubs().iterator().next() == _bottomNode)
						node.removeSub(_bottomNode);
					node.addSub(sub);
				}

			for (final TaxonomyNode<T> sup : other.getSupers())
				if (sup != _topNode && !mergeList.contains(sup))
				{
					if (node.getSupers().size() == 1 && node.getSupers().iterator().next() == _topNode)
						_topNode.removeSub(node);
					sup.addSub(node);
				}

			other.disconnect();

			for (final T t : other.getEquivalents())
				addEquivalentNode(t, node);

		}

		node.clearData();

		if (node != _topNode && node.getSupers().isEmpty())
			_topNode.addSub(node);

		if (node != _bottomNode && node.getSubs().isEmpty())
			node.addSub(_bottomNode);

		return node;
	}

	/**
	 * Walk through the super _nodes of the given _node and when a cycle is detected merge all the _nodes in that path
	 */
	@Override
	public void removeCycles(final TaxonomyNode<T> node)
	{
		if (!_nodes.get(node.getName()).equals(node))
			throw new InternalReasonerException("This _node does not exist in the taxonomy: " + node.getName());
		removeCycles(node, new ArrayList<TaxonomyNode<T>>());
	}

	/**
	 * Given a node and (a possibly empty) path of sub nodes, remove cycles by merging all the _nodes in the path.
	 */
	private boolean removeCycles(final TaxonomyNode<T> node, final List<TaxonomyNode<T>> path)
	{
		// cycle detected
		if (path.contains(node))
		{
			mergeNodes(path);
			return true;
		}
		else
		{
			// no cycle yet, add this _node to the path and continue
			path.add(node);

			final List<TaxonomyNode<T>> supers = new ArrayList<>(node.getSupers());
			for (int i = 0; i < supers.size();)
			{
				final TaxonomyNode<T> sup = supers.get(i);
				// remove cycles involving super _node
				removeCycles(sup, path);
				// if the super has been removed then no need
				// to increment the _index
				if (i < supers.size() && supers.get(i).equals(sup))
					i++;
			}

			// remove the _node from the path
			path.remove(path.size() - 1);

			return false;
		}
	}

	@Override
	public Object removeDatum(final T t, final TaxonomyKey key)
	{
		return getNode(t).removeDatum(key);
	}
}
