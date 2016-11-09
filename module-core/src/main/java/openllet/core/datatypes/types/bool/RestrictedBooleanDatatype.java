package openllet.core.datatypes.types.bool;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import openllet.aterm.ATermAppl;
import openllet.core.datatypes.Datatype;
import openllet.core.datatypes.EmptyIterator;
import openllet.core.datatypes.RestrictedDatatype;
import openllet.core.datatypes.exceptions.InvalidConstrainingFacetException;

/**
 * <p>
 * Title: Restricted Boolean Datatype
 * </p>
 * <p>
 * Description: A subset of the value space of xsd:boolean.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Mike Smith
 */

public class RestrictedBooleanDatatype implements RestrictedDatatype<Boolean>
{

	private final Datatype<Boolean> _dt;
	private final boolean _permitFalse;
	private final boolean _permitTrue;

	public RestrictedBooleanDatatype(final Datatype<Boolean> dt)
	{
		_dt = dt;
		_permitTrue = true;
		_permitFalse = true;
	}

	private RestrictedBooleanDatatype(final RestrictedBooleanDatatype other, final boolean permitTrue, final boolean permitFalse)
	{
		_dt = other._dt;
		_permitFalse = permitFalse;
		_permitTrue = permitTrue;
	}

	@Override
	public RestrictedDatatype<Boolean> applyConstrainingFacet(final ATermAppl facet, final Object value) throws InvalidConstrainingFacetException
	{
		throw new InvalidConstrainingFacetException(facet, value);
	}

	@Override
	public boolean contains(final Object value)
	{
		if (value instanceof Boolean)
			return ((Boolean) value).booleanValue() ? _permitTrue : _permitFalse;
		return false;
	}

	@Override
	public boolean containsAtLeast(final int n)
	{
		if (n <= 0)
			return true;
		if (n == 1)
			return _permitTrue || _permitFalse;
		if (n == 2)
			return _permitTrue && _permitFalse;
		return false;
	}

	@Override
	public RestrictedDatatype<Boolean> exclude(final Collection<?> values)
	{
		boolean permitTrue = _permitTrue;
		boolean permitFalse = _permitFalse;
		for (final Object o : values)
			if (o instanceof Boolean)
			{
				final Boolean b = (Boolean) o;
				if (b.booleanValue())
					permitTrue = false;
				else
					permitFalse = false;
			}
		if (permitTrue == _permitTrue && permitFalse == _permitFalse)
			return this;

		return new RestrictedBooleanDatatype(this, permitTrue, permitFalse);
	}

	@Override
	public Datatype<? extends Boolean> getDatatype()
	{
		return _dt;
	}

	@Override
	public RestrictedDatatype<Boolean> intersect(final RestrictedDatatype<?> other, final boolean negated)
	{
		if (other instanceof RestrictedBooleanDatatype)
		{
			final RestrictedBooleanDatatype otherRBD = (RestrictedBooleanDatatype) other;
			final boolean permitTrue = _permitTrue && otherRBD._permitTrue;
			final boolean permitFalse = _permitFalse && otherRBD._permitFalse;

			if (permitTrue == _permitTrue && permitFalse == _permitFalse)
				return this;
			if (permitTrue == otherRBD._permitTrue && permitFalse == otherRBD._permitFalse)
				return otherRBD;
			return new RestrictedBooleanDatatype(this, permitTrue, permitFalse);
		}
		else
			throw new IllegalArgumentException();
	}

	@Override
	public boolean isEmpty()
	{
		return !_permitTrue && !_permitFalse;
	}

	@Override
	public boolean isEnumerable()
	{
		return true;
	}

	@Override
	public boolean isFinite()
	{
		return true;
	}

	@Deprecated
	@Override
	public int size()
	{
		return (_permitTrue ? 1 : 0) + (_permitFalse ? 1 : 0);
	}

	@Override
	public RestrictedDatatype<Boolean> union(final RestrictedDatatype<?> other)
	{
		if (other instanceof RestrictedBooleanDatatype)
		{
			final RestrictedBooleanDatatype otherRBD = (RestrictedBooleanDatatype) other;
			final boolean permitTrue = _permitTrue || otherRBD._permitTrue;
			final boolean permitFalse = _permitFalse || otherRBD._permitFalse;

			if (permitTrue == _permitTrue && permitFalse == _permitFalse)
				return this;
			if (permitTrue == otherRBD._permitTrue && permitFalse == otherRBD._permitFalse)
				return otherRBD;
			return new RestrictedBooleanDatatype(this, permitTrue, permitFalse);
		}
		else
			throw new IllegalArgumentException();
	}

	@Override
	public Iterator<Boolean> valueIterator()
	{
		if (_permitTrue)
			if (_permitFalse)
				return Arrays.asList(Boolean.TRUE, Boolean.FALSE).iterator();
			else
				return Arrays.asList(Boolean.TRUE).iterator();
		else
			if (_permitFalse)
				return Arrays.asList(Boolean.FALSE).iterator();
			else
				return new EmptyIterator<>();
	}

}
