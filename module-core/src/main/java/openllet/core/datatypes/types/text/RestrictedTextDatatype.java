package openllet.core.datatypes.types.text;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
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
	protected final Set<Pattern> _patterns;
	protected final Optional<Long> _maxLength;
	protected final boolean _allowLang;
	protected final Datatype<ATermAppl> _dt;

	protected RestrictedTextDatatype(final Datatype<ATermAppl> dt, final Set<Pattern> patterns, final boolean allowLang, final Set<Object> excludedValues)
	{
		_dt = dt;
		_patterns = patterns;
		_allowLang = allowLang;
		_excludedValues = excludedValues;
		_maxLength = Optional.empty();
	}

	protected RestrictedTextDatatype(final Datatype<ATermAppl> dt, final Long maxLength, final boolean allowLang, final Set<Object> excludedValues)
	{
		_dt = dt;
		_maxLength = Optional.of(maxLength);
		_patterns = Collections.emptySet();
		_allowLang = allowLang;
		_excludedValues = excludedValues;
	}

	public RestrictedTextDatatype(final Datatype<ATermAppl> dt, final boolean allowLang)
	{
		this(dt, Collections.emptySet(), allowLang, Collections.emptySet());
	}

	public RestrictedTextDatatype(final Datatype<ATermAppl> dt, final String pattern)
	{
		this(dt, Collections.singleton(Pattern.compile(pattern)), false, Collections.emptySet());
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

				if (!_patterns.isEmpty())
				{
					final String litValue = ((ATermAppl) a.getArgument(ATermUtils.LIT_VAL_INDEX)).getName();
					for (final Pattern pattern : _patterns)
						if (!pattern.matcher(litValue).matches())
							return false;
				}

				if (_maxLength.isPresent())
				{
					final String litValue = ((ATermAppl) a.getArgument(ATermUtils.LIT_VAL_INDEX)).getName();
					if (litValue.length() > _maxLength.get())
						return false;
				}

				return true;
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
		// System.out.println("RestrictedTextDatatype.applyConstrainingFacet(" + facet + "\t" + value + ")");
		final String name = facet.getName().substring(BuiltinNamespace.XSD.getURI().length());
		switch (name)
		{
			case "pattern":
			{
				if (!(value instanceof ATermAppl))
					throw new UnsupportedOperationException("Don't know how to eval " + value + " in a regexp-pattern assertion");

				final ATermAppl term = (ATermAppl) value;
				final ATermAppl payload = (ATermAppl) term.getChildAt(0); // The 'pattern'.
				final Set<Pattern> patterns = SetUtils.union(_patterns, Collections.singleton(Pattern.compile(payload.getName())));
				return new RestrictedTextDatatype(_dt, patterns, _allowLang, _excludedValues);
			}
			case "lang_range":
				throw new UnsupportedOperationException("TODO : support lang_range facets"); // TODO: support lang_range facets
			case "length":
				throw new UnsupportedOperationException("TODO : support length facets"); // TODO: support length facets
			case "min_length": // Look-like there two syntax.
			case "minLength":
				throw new UnsupportedOperationException("TODO : support minLength facets"); // TODO: support minLength facets

			case "max_length": // Look-like there two syntax.
			case "maxLength":
			{
				if (!(value instanceof Number))
					throw new UnsupportedOperationException("Don't know how to eval " + value + " in a max_length assertion : " + value.getClass().getName() + " a java 'long/Long' was expected");

				final Long maxLength = ((Number) value).longValue();

				return new RestrictedTextDatatype(_dt, _maxLength.map(ml -> ml < maxLength ? ml : maxLength).orElse(maxLength), _allowLang, _excludedValues);
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

			return new RestrictedTextDatatype(_dt, SetUtils.union(_patterns, that._patterns), _allowLang && that._allowLang, SetUtils.union(_excludedValues, that._excludedValues));
		}
		else
			throw new IllegalArgumentException();
	}

	@Override
	public RestrictedDatatype<ATermAppl> exclude(final Collection<?> values)
	{
		final Set<Object> newExcludedValues = new HashSet<>(values);
		newExcludedValues.addAll(_excludedValues);
		return new RestrictedTextDatatype(_dt, _patterns, _allowLang, newExcludedValues);
	}

	@Override
	public RestrictedDatatype<ATermAppl> union(final RestrictedDatatype<?> other) // XXX This look like buggy.
	{
		if (other instanceof RestrictedTextDatatype)
		{
			if (!_patterns.isEmpty() || !((RestrictedTextDatatype) other)._patterns.isEmpty())
				throw new UnsupportedOperationException();

			if (_allowLang)
				return this;

			final RestrictedTextDatatype rtd = (RestrictedTextDatatype) other;

			if (_maxLength.isPresent() && !rtd._maxLength.isPresent())
				return this;
			if (!_maxLength.isPresent() && rtd._maxLength.isPresent())
				return rtd;
			if (_maxLength.isPresent() && rtd._maxLength.isPresent() && _maxLength.get().longValue() != rtd._maxLength.get().longValue())
				throw new UnsupportedOperationException("Two different max length " + _maxLength.get() + " && " + rtd._maxLength.get());

			return rtd;
		}
		else
			throw new IllegalArgumentException();
	}

}
