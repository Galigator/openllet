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

package openllet.core.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility functions for {#link java.util.Set Set}s.
 *
 * @author Evren Sirin
 */
public class SetUtils
{
	/**
	 * Adds the given object to the set but saves memory space by allocating only the required amount for small sets. The idea is to use the specialized empty
	 * set and singleton set implementations (which are immutable) for the sets of size 0 and 1. If the set is empty a new singleton set is created, if set has
	 * one element we create a new set with two elements, otherwise we simply add the element to the given set.This technique is most useful if the expected set
	 * size is 0 or 1.
	 *
	 * @param o
	 * @param set
	 * @return merge of set
	 */
	public static <T> Set<T> add(final T o, final Set<T> set)
	{
		switch (set.size())
		{
			case 0:
				return Collections.singleton(o);
			case 1:
			{
				final T existing = set.iterator().next();
				if (existing.equals(o))
					return set;
				return binary(existing, o);
			}
			default:
			{
				set.add(o);
				return set;
			}
		}
	}

	public static <T> Set<T> remove(final Object o, final Set<T> set)
	{
		switch (set.size())
		{
			case 0:
				return set;
			case 1:
			{
				if (set.contains(o))
					return Collections.emptySet();
				return set;
			}
			default:
			{
				set.remove(o);
				return set;
			}
		}
	}

	public final static <T> Set<T> singleton(final T o)
	{
		return Collections.singleton(o);
	}

	public final static <T> Set<T> binary(final T o1, final T o2)
	{
		final Set<T> set = create();
		set.add(o1);
		set.add(o2);

		return set;
	}

	/**
	 * @param coll A Collection of sets
	 * @return the union of all the sets given in a collection.
	 */
	public static <T> Set<T> union(final Collection<? extends Collection<? extends T>> coll)
	{
		final Set<T> set = create();

		for (final Collection<? extends T> innerColl : coll)
			set.addAll(innerColl);

		return set;
	}

	/**
	 * @param c1 A Collection of sets
	 * @param c2 A Collection of sets
	 * @return the union of two collections
	 */
	public static <T> Set<T> union(final Collection<? extends T> c1, final Collection<? extends T> c2)
	{
		final Set<T> set = create();
		set.addAll(c1);
		set.addAll(c2);

		return set;
	}

	/**
	 * @param coll A Collection of sets
	 * @return the intersection of all the collections given in a collection.
	 */
	public static <T> Set<T> intersection(final Collection<? extends Collection<? extends T>> coll)
	{
		final Iterator<? extends Collection<? extends T>> i = coll.iterator();

		if (!i.hasNext())
			return create();

		final Set<T> set = create();
		set.addAll(i.next());
		while (i.hasNext())
		{
			final Collection<? extends T> innerColl = i.next();
			set.retainAll(innerColl);
		}

		return set;
	}

	/**
	 * @return the intersection of two collections
	 * @param c1 A Collection of sets
	 * @param c2 A Collection of sets
	 */
	public static <T> Set<T> intersection(final Collection<? extends T> c1, final Collection<? extends T> c2)
	{
		final Set<T> set = create();
		set.addAll(c1);
		set.retainAll(c2);

		return set;
	}

	/**
	 * @param c1
	 * @param c2
	 * @return true if two collections have any elements in common
	 */
	public static boolean intersects(final Collection<?> c1, final Collection<?> c2)
	{
		for (final Object name : c1)
			if (c2.contains(name))
				return true;

		return false;
	}

	/**
	 * @param sub
	 * @param sup
	 * @return true if one set is subset of another one
	 */
	public static boolean subset(final Set<?> sub, final Set<?> sup)
	{
		return sub.size() <= sup.size() && sup.containsAll(sub);
	}

	/**
	 * @param s1
	 * @param s2
	 * @return true if one set is equal of another one
	 */
	public static <T> boolean equals(final Set<T> s1, final Set<T> s2)
	{
		return s1.size() == s2.size() && s1.containsAll(s2);
	}

	/**
	 * @param c1
	 * @param c2
	 * @return the difference of two sets. All the elements of second set is removed from the first set
	 */
	public static <T> Set<T> difference(final Collection<T> c1, final Collection<? extends Object> c2)
	{
		final Set<T> set = create();
		set.addAll(c1);
		if (c2 instanceof Set)
			set.removeAll(c2);
		else
			for (final Object e : c2)
				set.remove(e);

		return set;
	}

	/**
	 * @param elems
	 * @return a new set containing all the elements in the array
	 */
	@SafeVarargs
	public static <T> Set<T> create(final T... elems)
	{
		final Set<T> set = create(elems.length);
		for (final T elem : elems)
			set.add(elem);

		return set;
	}

	/**
	 * Creates a set containing all the elements in the collection
	 *
	 * @param initialSize is the initial size of the set.
	 * @return a fresh set resilient to concurrency.
	 * @since 2.6.0
	 */
	public static <T> Set<T> create(final int initialSize)
	{
		return Collections.newSetFromMap(new ConcurrentHashMap<>(initialSize));
	}

	/**
	 * Creates a set containing all the elements in the collection
	 *
	 * @return a fresh set resilient to concurrency.
	 * @since 2.6.0
	 */
	public static <T> Set<T> create()
	{
		return Collections.newSetFromMap(new ConcurrentHashMap<>());
	}

	/**
	 * @param elements
	 * @return a new set containing all the elements in the collection
	 */
	public static <T> Set<T> create(final Collection<T> elements)
	{
		final Set<T> result = create();
		result.addAll(elements);
		return result;
	}
}
