package openllet.core.datatypes.types.duration;

import static java.lang.String.format;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.logging.Logger;
import javax.xml.datatype.Duration;
import openllet.aterm.ATermAppl;
import openllet.core.datatypes.Datatype;
import openllet.core.datatypes.Facet;
import openllet.core.datatypes.RestrictedDatatype;
import openllet.core.datatypes.exceptions.InvalidConstrainingFacetException;
import openllet.shared.tools.Log;

/**
 * <p>
 * Title: Restricted Duration Datatype
 * </p>
 * <p>
 * Description: A subset of the value space of xsd:duration
 * </p>
 * <p>
 * Copyright: Copyright (c) 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Evren Sirin
 */
public class RestrictedDurationDatatype implements RestrictedDatatype<Duration>
{
	private final static Logger			_logger	= Log.getLogger(RestrictedDurationDatatype.class);

	private final Datatype<Duration>	_dt;
	protected final Predicate<Duration>	_check;

	public RestrictedDurationDatatype(final Datatype<Duration> dt, final Predicate<Duration> check)
	{
		_dt = dt;
		_check = check;
	}

	public RestrictedDurationDatatype(final Datatype<Duration> dt)
	{
		this(dt, x -> true);
	}

	@Override
	public RestrictedDatatype<Duration> applyConstrainingFacet(final ATermAppl facet, final Object value) throws InvalidConstrainingFacetException
	{
		if (!(value instanceof Duration))
		{
			final String msg = format(UNSUPPORTED_FORMAT, getDatatype(), facet, value);
			_logger.severe(msg);
			throw new IllegalArgumentException(msg);
		}

		final Facet f = Facet.Registry.get(facet);
		if (f == null)
		{
			final String msg = format(UNSUPPORTED_FORMAT, getDatatype(), facet, value);
			_logger.severe(msg);
			throw new IllegalArgumentException(msg);
		}

		final Facet.XSD xsd = (Facet.XSD) f;
		final Duration duration = (Duration) value;

		switch (xsd)
		{
			case LENGTH:
			case MAX_LENGTH:
			case MAX_EXCLUSIVE:
				return new RestrictedDurationDatatype(_dt, anotherDuration -> duration.compare(anotherDuration) > 0);
			case MAX_INCLUSIVE:
				return new RestrictedDurationDatatype(_dt, anotherDuration -> duration.compare(anotherDuration) >= 0);
			case MIN_LENGTH:
			case MIN_EXCLUSIVE:
				return new RestrictedDurationDatatype(_dt, anotherDuration -> duration.compare(anotherDuration) < 0);
			case MIN_INCLUSIVE:
				return new RestrictedDurationDatatype(_dt, anotherDuration -> duration.compare(anotherDuration) <= 0);

			case PATTERN: // TODO allow duration that are regexp.
				_logger.severe("Duration as regexp pattern will come in future developpement.");
				//$FALL-THROUGH$
			default:
				throw new UnsupportedOperationException();
		}
	}

	@Override
	public boolean contains(final Object duration)
	{
		if (!(duration instanceof Duration)) return false;

		return _check.test((Duration) duration);
	}

	@Override
	public boolean containsAtLeast(final int n)
	{
		return true;
	}

	@Override
	public RestrictedDatatype<Duration> exclude(final Collection<?> values)
	{
		return new RestrictedDurationDatatype(_dt, _check.and(duration -> !values.contains(duration)));
	}

	@Override
	public RestrictedDatatype<Duration> intersect(final RestrictedDatatype<?> other, final boolean negated)
	{
		if (other instanceof RestrictedDurationDatatype)
			return new RestrictedDurationDatatype(_dt, _check.and(((RestrictedDurationDatatype) other)._check));
		else
			throw new IllegalArgumentException();
	}

	@Override
	public RestrictedDatatype<Duration> union(final RestrictedDatatype<?> other)
	{
		if (other instanceof RestrictedDurationDatatype)
			return new RestrictedDurationDatatype(_dt, _check.or(((RestrictedDurationDatatype) other)._check));
		else
			throw new IllegalArgumentException();
	}

	@Override
	public Datatype<? extends Duration> getDatatype()
	{
		return _dt;
	}

	@Override
	public boolean isEmpty()
	{
		return false;
	}

	@Override
	public boolean isEnumerable()
	{
		return false;
	}

	@Override
	public boolean isFinite()
	{
		return false;
	}

	@Override
	public Iterator<Duration> valueIterator()
	{
		throw new IllegalStateException();
	}
}
