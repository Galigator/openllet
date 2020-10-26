// Portions Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// Clark & Parsia, LLC parts of this source code are available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.utils;

import java.util.Iterator;
import java.util.Set;

import openllet.core.utils.iterator.PairIterator;

/**
 * @author Evren Sirin
 * @param <T> kind of element
 */
public class CandidateSet<T>
{
	private final Set<T> _knowns = SetUtils.create();
	private final Set<T> _unknowns = SetUtils.create();

	public CandidateSet()
	{
		// knowns and unknowns empty
	}

	public CandidateSet(final Set<T> knowns)
	{
		_knowns.addAll(knowns);
	}

	public CandidateSet(final Set<T> knowns, final Set<T> unknowns)
	{
		_knowns.addAll(knowns);
		_unknowns.addAll(unknowns);
	}

	public Set<T> getKnowns()
	{
		return _knowns;
	}

	public Set<T> getUnknowns()
	{
		return _unknowns;
	}

	public void add(final T obj, final Bool isKnown)
	{
		if (isKnown.isTrue())
			_knowns.add(obj);
		else
			if (isKnown.isUnknown())
				_unknowns.add(obj);
	}

	public void update(final T obj, final Bool isCandidate)
	{
		if (isCandidate.isTrue())
		{
			// do nothing
		}
		else
			if (isCandidate.isFalse())
				remove(obj);
			else
				if (_knowns.contains(obj))
				{
					_knowns.remove(obj);
					_unknowns.add(obj);
				}
	}

	public boolean remove(final Object obj)
	{
		return _knowns.remove(obj) || _unknowns.remove(obj);
	}

	public boolean contains(final Object obj)
	{
		return _knowns.contains(obj) || _unknowns.contains(obj);
	}

	public int size()
	{
		return _knowns.size() + _unknowns.size();
	}

	public Iterator<T> iterator()
	{
		return new PairIterator<>(_knowns.iterator(), _unknowns.iterator());
	}

	@Override
	public String toString()
	{
		return "Knowns: " + _knowns.size() + " Unknowns: " + _unknowns.size();
	}
}
