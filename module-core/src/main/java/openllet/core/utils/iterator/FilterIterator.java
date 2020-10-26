package openllet.core.utils.iterator;

import java.util.Iterator;
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
 * @param <T> kind of element
 */
public abstract class FilterIterator<T> implements Iterator<T>
{
	private final Iterator<T> _iterator;

	/**
	 * Next element to be returned, or null if _iterator is consumed
	 */
	private T _next;

	public FilterIterator(final Iterator<T> iterator)
	{
		_iterator = iterator;
	}

	private void findNext()
	{
		if (_next != null)
			return;

		while (_iterator.hasNext())
		{
			_next = _iterator.next();

			// if this element is filter
			if (!filter(_next))
				return;
		}

		_next = null;
	}

	@Override
	public boolean hasNext()
	{
		findNext();

		return _next != null;
	}

	@Override
	public T next()
	{
		if (!hasNext())
			throw new NoSuchElementException();

		final T result = _next;
		_next = null;

		return result;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

	public abstract boolean filter(T obj);
}
