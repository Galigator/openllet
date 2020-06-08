package openllet.core.datatypes.exceptions;

import static java.lang.String.format;

import openllet.aterm.ATermAppl;

/**
 * <p>
 * Title: Invalid Constraining Facet Exception
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
public class InvalidConstrainingFacetException extends DatatypeReasonerException
{

	private static final long	serialVersionUID	= 2L;

	private final ATermAppl		_facet;
	private final Object		_value;

	public InvalidConstrainingFacetException(final ATermAppl facet, final Object value)
	{
		this(format("Invalid constraining _facet ('%s','%s')", facet.getName(), value), facet, value);
	}

	public InvalidConstrainingFacetException(final ATermAppl facet, final Object value, final Throwable cause)
	{
		this(facet, value);
		initCause(cause);
	}

	public InvalidConstrainingFacetException(final String msg, final ATermAppl facet, final Object value)
	{
		super(msg);
		_facet = facet;
		_value = value;
	}

	public ATermAppl getFacet()
	{
		return _facet;
	}

	public Object getValue()
	{
		return _value;
	}
}
