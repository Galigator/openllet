// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.tableau.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.utils.ATermUtils;

/**
 * <p>
 * Description: Least Recently Used implementation of ConceptCache. Primitive concepts and their negation are always kept in the cache. The least recently used
 * complex concept will be removed from the cache if the max size is reached.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Ron Alford
 */
public class ConceptCacheLRU extends AbstractConceptCache
{
	private Map<ATermAppl, CachedNode> _primitive;
	private Map<ATermAppl, CachedNode> _nonPrimitive;

	private CacheSafety _cacheSafety;

	/**
	 * Creates an empty ConceptCacheImpl with no size restrictions Using this constructor is equivalent to break the auto-flush LRU policy of this cache.
	 *
	 * @param kb
	 */
	public ConceptCacheLRU(final KnowledgeBase kb)
	{
		this(kb, Integer.MAX_VALUE);
	}

	/**
	 * Creates an empty _cache with at most <code>maxSize</code> elements which are neither named or negations of names.
	 *
	 * @param kb
	 * @param maxSize
	 */
	public ConceptCacheLRU(final KnowledgeBase kb, final int maxSize)
	{
		super(maxSize);

		_cacheSafety = CacheSafetyFactory.createCacheSafety(kb.getExpressivity());

		_primitive = new ConcurrentHashMap<>();

		_nonPrimitive = Integer.MAX_VALUE == maxSize ? //
				new ConcurrentHashMap<>() : // as "size" is an integer and so size() can be greater than max-int, then the predicate removeEldestEntry will always be false. So we use a simpler data structure.
				Collections.synchronizedMap(new LinkedHashMap<ATermAppl, CachedNode>(16, 0.75f, true)
				{
					private static final long serialVersionUID = 3701638684292370398L;

					@Override
					protected boolean removeEldestEntry(final Map.Entry<ATermAppl, CachedNode> eldest)
					{
						return _nonPrimitive.size() > getMaxSize();
					}
				});
	}

	@Override
	public CacheSafety getSafety()
	{
		return _cacheSafety;
	}

	@Override
	public void clear()
	{
		_primitive.clear();
		_nonPrimitive.clear();
	}

	@Override
	public boolean containsKey(final Object key)
	{
		return _primitive.containsKey(key) || _nonPrimitive.containsKey(key);
	}

	@Override
	public boolean containsValue(final Object value)
	{
		return _primitive.containsValue(value) || _nonPrimitive.containsValue(value);
	}

	@Override
	public Set<java.util.Map.Entry<ATermAppl, CachedNode>> entrySet()
	{
		final Set<java.util.Map.Entry<ATermAppl, CachedNode>> returnSet = new HashSet<>(_primitive.entrySet());
		returnSet.addAll(_nonPrimitive.entrySet());
		return returnSet;
	}

	@Override
	public CachedNode get(final Object key)
	{
		final CachedNode node = _primitive.get(key);
		if (node != null)
			return node;
		return _nonPrimitive.get(key);
	}

	@Override
	public boolean isEmpty()
	{
		return _primitive.isEmpty() && _nonPrimitive.isEmpty();
	}

	@Override
	public Set<ATermAppl> keySet()
	{
		final Set<ATermAppl> keys = new HashSet<>(_primitive.keySet());
		keys.addAll(_nonPrimitive.keySet());
		return keys;
	}

	/**
	 * key can't be null, value can't be null.
	 */
	@Override
	public CachedNode put(final ATermAppl key, final CachedNode value)
	{
		if (ATermUtils.isPrimitiveOrNegated(key))
		{
			final CachedNode prev = _primitive.put(key, value);
			if (isFull())
				_nonPrimitive.entrySet();
			return prev;
		}

		return _nonPrimitive.put(key, value);
	}

	@Override
	public void putAll(final Map<? extends ATermAppl, ? extends CachedNode> t)
	{
		for (final java.util.Map.Entry<? extends ATermAppl, ? extends CachedNode> entry : t.entrySet())
			put(entry.getKey(), entry.getValue());
	}

	@Override
	public CachedNode remove(final Object key)
	{
		if (_primitive.containsKey(key))
			return _primitive.remove(key);
		return _nonPrimitive.remove(key);
	}

	@Override
	public int size()
	{
		return _primitive.size() + _nonPrimitive.size();
	}

	@Override
	public Collection<CachedNode> values()
	{
		final Set<CachedNode> valueSet = new HashSet<>(_primitive.values());
		valueSet.addAll(_nonPrimitive.values());
		return valueSet;
	}

	@Override
	public String toString()
	{
		return "[Cache size: " + _primitive.size() + "," + _nonPrimitive.size() + "]";
	}

}
