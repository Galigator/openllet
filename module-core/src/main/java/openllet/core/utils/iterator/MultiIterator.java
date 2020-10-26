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

/**
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Evren Sirin
 * @param <T> kind of elements
 */
public class MultiIterator<T> implements Iterator<T>
{
	private final List<Iterator<? extends T>> _list = new ArrayList<>(2);

	private volatile int _index = 0;

	private volatile Iterator<? extends T> _curr;

	public MultiIterator(final Iterator<? extends T> first)
	{
		_curr = first;
	}

	public MultiIterator(final Iterator<? extends T> first, final Iterator<? extends T> second)
	{
		_curr = first;
		_list.add(second);
	}

	@Override
	public boolean hasNext()
	{
		while (!_curr.hasNext() && _index < _list.size())
			_curr = _list.get(_index++);

		return _curr.hasNext();
	}

	@Override
	public T next()
	{
		if (!hasNext())
			throw new NoSuchElementException("multi iterator");

		return _curr.next();
	}

	public void append(final Iterator<? extends T> other)
	{
		if (other.hasNext())
			if (other instanceof MultiIterator)
				_list.addAll(((MultiIterator<? extends T>) other)._list);
			else
				_list.add(other);
	}

	@Override
	public void remove()
	{
		_curr.remove();
	}
}
