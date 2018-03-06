package openllet.core.datatypes.types.text;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import openllet.aterm.ATermAppl;
import openllet.core.datatypes.Datatype;
import openllet.core.datatypes.RestrictedDatatype;
import openllet.core.datatypes.exceptions.InvalidConstrainingFacetException;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.SetUtils;
import openllet.core.vocabulary.BuiltinNamespace;

/**
 * <p>
 * Title: Restricted Text Datatype
 * </p>
 * <p>
 * Description: A subset of the value space of rdf:plainLiteral
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
public class RestrictedTextDatatype implements RestrictedDatatype<ATermAppl>
{
	private static final String NCNAMESTARTCHAR = "[A-Z]|_|[a-z]|[\u00C0-\u00D6]|[\u00D8-\u00F6]|[\u00F8-\u02FF]|[\u0370-\u037D]|[\u037F-\u1FFF]|[\u200C-\u200D]|[\u2070-\u218F]|[\u2C00-\u2FEF]|[\u3001-\uD7FF]|[\uF900-\uFDCF]|[\uFDF0-\uFFFD]";
	private static final String NCNAMECHAR = NCNAMESTARTCHAR + "|-|\\.|[0-9]|\u00B7|[\u0300-\u036F]|[\u203F-\u2040]";
	protected static final String NCNAME = "(" + NCNAMESTARTCHAR + ")(" + NCNAMECHAR + ")*";

	private static final String NAMESTARTCHAR = ":|" + NCNAMESTARTCHAR;
	private static final String NAMECHAR = NAMESTARTCHAR + "|-|\\.|[0-9]|\u00B7|[\u0300-\u036F]|[\u203F-\u2040]";
	protected static final String NAME = "(" + NAMESTARTCHAR + ")(" + NAMECHAR + ")*";

	protected static final String NMTOKEN = "(" + NAMECHAR + ")+";

	protected static final String TOKEN = "([^\\s])(\\s([^\\s])|([^\\s]))*";

	protected static final String LANGUAGE = "[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*";

	protected static final String NORMALIZED_STRING = "([^\\r\\n\\t])*";

	private static final Set<ATermAppl> permittedDts = new HashSet<>(Arrays.asList(ATermUtils.EMPTY));

	/*
	 * XXX: This is awkward.
	 */
	public static boolean addPermittedDatatype(final ATermAppl dt)
	{
		return permittedDts.add(dt);
	}

	protected final Set<Object> _excludedValues;
	protected final Predicate<String> _check;
	protected final boolean _allowLang;
	protected final Datatype<ATermAppl> _dt;

	protected RestrictedTextDatatype(final Datatype<ATermAppl> dt, final Predicate<String> check, final boolean allowLang, final Set<Object> excludedValues)
	{
		_dt = dt;
		_check = check;
		_allowLang = allowLang;
		_excludedValues = excludedValues;
	}

	protected RestrictedTextDatatype(final RestrictedTextDatatype rtd, final Predicate<String> check)
	{
		_dt = rtd._dt;
		_check = check;
		_allowLang = rtd._allowLang;
		_excludedValues = rtd._excludedValues;
	}

	public RestrictedTextDatatype(final Datatype<ATermAppl> dt, final boolean allowLang)
	{
		this(dt, x -> true, allowLang, Collections.emptySet());
	}

	private RestrictedTextDatatype(final Datatype<ATermAppl> dt, final Pattern pattern)
	{
		this(dt, x -> pattern.matcher(x).matches(), false, Collections.emptySet());
	}

	public RestrictedTextDatatype(final Datatype<ATermAppl> dt, final String pattern)
	{
		this(dt, Pattern.compile(pattern));
	}

	@Override
	public boolean contains(final Object value)
	{
		if (value instanceof ATermAppl)
		{
			final ATermAppl a = (ATermAppl) value;

			if (_excludedValues.contains(a))
				return false;

			if (ATermUtils.isLiteral(a) && permittedDts.contains(a.getArgument(ATermUtils.LIT_URI_INDEX)))
			{
				if (!_allowLang && !ATermUtils.EMPTY.equals(a.getArgument(ATermUtils.LIT_LANG_INDEX)))
					return false;

				final String litValue = ((ATermAppl) a.getArgument(ATermUtils.LIT_VAL_INDEX)).getName();
				return _check.test(litValue);
			}
		}
		return false;
	}

	@Override
	public boolean containsAtLeast(final int n)
	{
		return true;
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
	public Iterator<ATermAppl> valueIterator()
	{
		throw new IllegalStateException();
	}

	@Override
	public Datatype<? extends ATermAppl> getDatatype()
	{
		return _dt;
	}

	@Override
	public RestrictedDatatype<ATermAppl> applyConstrainingFacet(final ATermAppl facet, final Object value) throws InvalidConstrainingFacetException
	{
		final String name = facet.getName().substring(BuiltinNamespace.XSD.getURI().length());
		// final Facet x = Facet.Registry.get(facet) // Facet.XSD;
		switch (name)
		{
			case "pattern":
			{
				if (!(value instanceof ATermAppl))
					throw new UnsupportedOperationException("Don't know how to eval " + value + " in a regexp-pattern assertion");

				final ATermAppl term = (ATermAppl) value;
				final ATermAppl payload = (ATermAppl) term.getChildAt(0); // The 'pattern'.
				final Pattern pattern = Pattern.compile(payload.getName());
				return new RestrictedTextDatatype(_dt, _check.and(str -> pattern.matcher(str).matches()), _allowLang, _excludedValues);
			}
			case "lang_range":
				throw new UnsupportedOperationException("TODO : support lang_range facets"); // TODO: support lang_range facets
			case "length":
				throw new UnsupportedOperationException("TODO : support length facets"); // TODO: support length facets
			case "min_length": // Look-like there two syntax.
			case "minLength":
			{
				if (!(value instanceof Number))
					throw new UnsupportedOperationException("Don't know how to eval " + value + " in a max_length assertion : " + value.getClass().getName() + " a java 'long/Long' was expected");

				final Long minLength = ((Number) value).longValue();
				return new RestrictedTextDatatype(_dt, _check.and(str -> str.length() > minLength), _allowLang, _excludedValues);
			}

			case "max_length": // Look-like there two syntax.
			case "maxLength":
			{
				if (!(value instanceof Number))
					throw new UnsupportedOperationException("Don't know how to eval " + value + " in a max_length assertion : " + value.getClass().getName() + " a java 'long/Long' was expected");

				final Long maxLength = ((Number) value).longValue();
				return new RestrictedTextDatatype(_dt, _check.and(str -> str.length() < maxLength), _allowLang, _excludedValues);
			}
			default:
				throw new UnsupportedOperationException(facet.getName() + " is an unknow restriction");
		}
		//return this;
	}

	@Override
	public RestrictedDatatype<ATermAppl> intersect(final RestrictedDatatype<?> other, final boolean negated)
	{
		if (other instanceof RestrictedTextDatatype)
		{
			final RestrictedTextDatatype that = (RestrictedTextDatatype) other;

			return new RestrictedTextDatatype(_dt, //
					_check.and(that._check), //
					_allowLang && that._allowLang, //
					SetUtils.union(_excludedValues, that._excludedValues));
		}
		else
			throw new IllegalArgumentException();
	}

	@Override
	public RestrictedDatatype<ATermAppl> exclude(final Collection<?> values)
	{
		final Set<Object> newExcludedValues = new HashSet<>(values);
		newExcludedValues.addAll(_excludedValues);// TODO : replace by _check.and(value -> !values.contains(value))
		return new RestrictedTextDatatype(_dt, _check, _allowLang, newExcludedValues);
	}

	@Override
	public RestrictedDatatype<ATermAppl> union(final RestrictedDatatype<?> other) // XXX This look like buggy.
	{
		if (other instanceof RestrictedTextDatatype)
		{
			if (_allowLang)
				return this; // XXX Strange

			final RestrictedTextDatatype that = (RestrictedTextDatatype) other;

			// TODO : take care of the too possible errors, when values aren't the sames !!!!
			// _allowLang = rtd._allowLang;
			// _excludedValues = rtd._excludedValues;

			return new RestrictedTextDatatype(that, _check.or(that._check));
		}
		else
			throw new IllegalArgumentException();
	}

}
