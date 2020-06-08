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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import openllet.core.utils.SetUtils;

/**
 * @author     Evren Sirin
 * @param  <T> type of the node name.
 */
public class TaxonomyNode<T>
{

	private final Map<Object, Object>	_dataMap	= new ConcurrentHashMap<>();

	private final Set<TaxonomyNode<T>>	_subs		= SetUtils.create(2);
	private final Set<TaxonomyNode<T>>	_supers		= SetUtils.create();

	private volatile Set<T>				_equivalents;							// Most time it won't change. But it happen some time.
	private volatile boolean			_hidden;

	private volatile boolean			_checkMark;
	private volatile boolean			_seen		= false;					// Is the checkMark meaningfull

	private volatile T					_name;									// Can be null :-(

	protected volatile short			_depth		= 0;

	public TaxonomyNode(final T name, final boolean hidden)
	{
		_name = name;
		_hidden = hidden;

		if (name == null)
			_equivalents = Collections.emptySet();
		else
			_equivalents = Collections.singleton(name);
	}

	public TaxonomyNode(final Collection<T> equivalents, final boolean hidden)
	{
		if (equivalents == null || equivalents.isEmpty())
		{
			_name = null;
			_equivalents = Collections.emptySet();
		}
		else
		{
			_name = equivalents.iterator().next();
			_equivalents = SetUtils.create(equivalents);
		}

		_hidden = hidden;
	}

	public void setMark(final boolean b)
	{
		_checkMark = b;
		_seen = true;
	}

	public boolean markIsDefined()
	{
		return _seen;
	}

	public boolean getMark()
	{
		return _checkMark;
	}

	public void resetMark()
	{
		_seen = false;
	}

	public void addEquivalent(final T t)
	{
		if (_equivalents.size() < 2)// Equivalents of size [0, 1] are immutable collections.
			_equivalents = SetUtils.create(_equivalents);
		_equivalents.add(t);
	}

	public void addSub(final TaxonomyNode<T> other)
	{
		if (equals(other) || _subs.contains(other)) return;

		_subs.add(other);
		if (!_hidden) other._supers.add(this);
	}

	public void addSubs(final Collection<TaxonomyNode<T>> others)
	{
		others.forEach(t -> addSub(t));
	}

	public void addSupers(final Collection<TaxonomyNode<T>> others)
	{
		_supers.addAll(others);
		if (!_hidden) for (final TaxonomyNode<T> other : others)
			other._subs.add(this);
	}

	public void clearData()
	{
		_dataMap.clear();
	}

	public void disconnect()
	{
		for (final Iterator<TaxonomyNode<T>> j = _subs.iterator(); j.hasNext();)
		{
			final TaxonomyNode<T> sub = j.next();
			j.remove();
			sub._supers.remove(this);
		}

		for (final Iterator<TaxonomyNode<T>> j = _supers.iterator(); j.hasNext();)
		{
			final TaxonomyNode<T> sup = j.next();
			j.remove();
			sup._subs.remove(this);
		}
	}

	public Object getDatum(final Object key)
	{
		return _dataMap.get(key);
	}

	public Set<T> getEquivalents()
	{
		return _equivalents;
	}

	public T getName()
	{
		return _name;
	}

	public Collection<TaxonomyNode<T>> getSubs()
	{
		return _subs;
	}

	public Collection<TaxonomyNode<T>> getSupers()
	{
		return _supers;
	}

	public boolean isBottom()
	{
		return _subs.isEmpty();
	}

	public boolean isHidden()
	{
		return _hidden;
	}

	public boolean isLeaf()
	{
		return _subs.size() == 1 && _subs.iterator().next().isBottom();
	}

	public boolean isTop()
	{
		return _supers.isEmpty();
	}

	public void print()
	{
		print("");
	}

	public void print(final String indentLvl)
	{
		if (_subs.isEmpty()) return;

		System.out.print(indentLvl);
		final Iterator<T> i = _equivalents.iterator();
		while (i.hasNext())
		{
			System.out.print(i.next());
			if (i.hasNext()) System.out.print(" = ");
		}
		System.out.println();

		final String indent = indentLvl + "  ";
		for (final TaxonomyNode<T> sub : _subs)
			sub.print(indent);
	}

	public Object putDatum(final Object key, final Object value)
	{
		return _dataMap.put(key, value);
	}

	public Object removeDatum(final Object key)
	{
		return _dataMap.remove(key);
	}

	public void removeMultiplePaths()
	{
		if (!_hidden) for (final TaxonomyNode<T> sup : _supers)
			for (final TaxonomyNode<T> sub : _subs)
				sup.removeSub(sub);
	}

	public void removeEquivalent(final T t)
	{
		_equivalents.remove(t);

		if (_name != null && _name.equals(t)) _name = _equivalents.iterator().next();
	}

	public void removeSub(final TaxonomyNode<T> other)
	{
		_subs.remove(other);
		other._supers.remove(this);
	}

	public void setHidden(final boolean hidden)
	{
		this._hidden = hidden;
	}

	@Override
	public String toString()
	{
		return _name.toString();// + " = " + _equivalents;
	}
}
