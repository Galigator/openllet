// Portions Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// Clark & Parsia, LLC parts of this source code are available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DisjointSet _data structure. Uses path compression and union by rank.
 *
 * @author     Evren Sirin
 * @param  <T> kind of element
 */
public class DisjointSet<T>
{
	private class Node<U>
	{
		private final U	_object;
		private Node<U>	_parent	= this;
		private int		_rank	= 0;

		public Node(final U o)
		{
			_object = o;
		}
	}

	private final Map<T, Node<T>> _elements = new ConcurrentHashMap<>();

	public void add(final T o)
	{
		if (_elements.containsKey(o)) return;

		_elements.put(o, new Node<>(o));
	}

	public boolean contains(final T o)
	{
		return _elements.containsKey(o);
	}

	public Collection<T> elements()
	{
		return Collections.unmodifiableSet(_elements.keySet());
	}

	public T find(final T o)
	{
		return findRoot(o)._object;
	}

	private Node<T> findRoot(final T o)
	{
		Node<T> node = _elements.get(o);
		while (node._parent._parent != node._parent)
		{
			node._parent = node._parent._parent;
			node = node._parent;
		}

		return node._parent;
	}

	public Collection<Set<T>> getEquivalanceSets()
	{

		final Map<T, Set<T>> equivalanceSets = new HashMap<>();

		for (final T x : _elements.keySet())
		{
			final T representative = find(x);

			Set<T> equivalanceSet = equivalanceSets.get(representative);
			if (equivalanceSet == null)
			{
				equivalanceSet = new HashSet<>();
				equivalanceSets.put(representative, equivalanceSet);
			}
			equivalanceSet.add(x);
		}

		return equivalanceSets.values();
	}

	public boolean isSame(final T x, final T y)
	{
		return find(x).equals(find(y));
	}

	@Override
	public String toString()
	{
		final StringBuffer buffer = new StringBuffer();

		buffer.append("{");
		for (final Iterator<Node<T>> i = _elements.values().iterator(); i.hasNext();)
		{
			final Node<T> node = i.next();
			buffer.append(node._object);
			buffer.append(" -> ");
			buffer.append(node._parent._object);
			if (i.hasNext()) buffer.append(", ");
		}
		buffer.append("}");

		return buffer.toString();
	}

	public Node<T> union(final T x, final T y)
	{
		Node<T> rootX = findRoot(x);
		Node<T> rootY = findRoot(y);

		if (rootX._rank > rootY._rank)
		{
			final Node<T> node = rootX;
			rootX = rootY;
			rootY = node;
		}
		else if (rootX._rank == rootY._rank) ++rootY._rank;

		rootX._parent = rootY;

		return rootY;
	}

}
