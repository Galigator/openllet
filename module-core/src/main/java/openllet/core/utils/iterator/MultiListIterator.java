// Portions Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// Clark & Parsia, LLC parts of this source code are available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.utils.iterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import openllet.aterm.ATermAppl;
import openllet.aterm.ATermList;

public class MultiListIterator implements Iterator<ATermAppl>
{
	private final List<ATermList> _list = new ArrayList<>(2);

	private int _index = 0;

	private volatile ATermList _curr;

	public MultiListIterator(final ATermList first)
	{
		_curr = first;
	}

	@Override
	public boolean hasNext()
	{
		while (_curr.isEmpty() && _index < _list.size())
			_curr = _list.get(_index++);

		return !_curr.isEmpty();
	}

	@Override
	public ATermAppl next()
	{
		if (!hasNext())
			throw new NoSuchElementException();

		final ATermAppl next = (ATermAppl) _curr.getFirst();

		_curr = _curr.getNext();

		return next;
	}

	public void append(final ATermList other)
	{
		_list.add(other);
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}
}
