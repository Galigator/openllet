// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * Title: Collection Utilities
 * </p>
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Harris Lin
 * @author Evren Sirin
 */
public class CollectionUtils
{
	public static <K, V> Map<K, V> makeIdentityMap()
	{
		return new IdentityHashMap<>();
	}

	public static <K, V> Map<K, V> makeIdentityMap(final int size)
	{
		return new IdentityHashMap<>(size);
	}

	public static <K, V> Map<K, V> makeIdentityMap(final Map<? extends K, ? extends V> map)
	{
		return new IdentityHashMap<>(map);
	}

	public static <T> Set<T> makeIdentitySet()
	{
		return new IdentityHashSet<>();
	}

	public static <T> Set<T> makeIdentitySet(final int size)
	{
		return new IdentityHashSet<>(size);
	}

	public static <T> Set<T> makeIdentitySet(final Collection<? extends T> a)
	{
		return new IdentityHashSet<>(a);
	}

	public static <T> List<T> makeList()
	{
		return new ArrayList<>();
	}

	public static <T> List<T> makeList(final int size)
	{
		return new ArrayList<>(size);
	}

	public static <T> List<T> makeList(final Collection<? extends T> a)
	{
		return new ArrayList<>(a);
	}

	public static <K, V> Map<K, V> makeMap()
	{
		return new ConcurrentHashMap<>();
	}

	public static <K, V> Map<K, V> makeMap(final int size)
	{
		return new ConcurrentHashMap<>(size);
	}

	public static <K, V> Map<K, V> makeMap(final Map<? extends K, ? extends V> map)
	{
		return new ConcurrentHashMap<>(map);
	}

	@Deprecated
	public static <T> Set<T> makeSet()
	{
		return SetUtils.create();
	}

	@Deprecated
	public static <T> Set<T> makeSet(final int size)
	{
		return SetUtils.create(size);
	}

	@Deprecated
	public static <T> Set<T> makeSet(final Collection<? extends T> a)
	{
		return SetUtils.create(a);
	}
}
