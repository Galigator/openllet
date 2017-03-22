package openllet.core.datatypes.types.text;

import openllet.aterm.ATermAppl;
import openllet.core.datatypes.AbstractBaseDatatype;
import openllet.core.datatypes.Datatype;
import openllet.core.datatypes.RestrictedDatatype;
import openllet.core.datatypes.exceptions.InvalidLiteralException;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.Namespaces;

/**
 * <p>
 * Title: <code>rdf:plainLiteral</code>
 * </p>
 * <p>
 * Description: Singleton implementation of <code>rdf:plainLiteral</code> datatype
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
public class RDFPlainLiteral extends AbstractBaseDatatype<ATermAppl>
{

	private static final RDFPlainLiteral instance = new RDFPlainLiteral();

	static
	{
		RestrictedTextDatatype.addPermittedDatatype(instance.getName());
	}

	public static RDFPlainLiteral getInstance()
	{
		return instance;
	}

	private final RestrictedTextDatatype _dataRange;

	private RDFPlainLiteral()
	{
		super(ATermUtils.makeTermAppl(Namespaces.RDF + "PlainLiteral"));
		_dataRange = new RestrictedTextDatatype(this, true);
	}

	@Override
	public RestrictedDatatype<ATermAppl> asDataRange()
	{
		return _dataRange;
	}

	@Override
	public ATermAppl getCanonicalRepresentation(final ATermAppl input) throws InvalidLiteralException
	{
		return getValue(input);
	}

	@Override
	public ATermAppl getLiteral(final Object value)
	{
		if (value instanceof ATermAppl)
		{
			final ATermAppl literal = (ATermAppl) value;
			try
			{
				return getCanonicalRepresentation(literal);
			}
			catch (final InvalidLiteralException e)
			{
				throw new IllegalStateException(e);
			}
		}
		else
			throw new IllegalArgumentException();
	}

	@Override
	public Datatype<?> getPrimitiveDatatype()
	{
		return this;
	}

	@Override
	public ATermAppl getValue(final ATermAppl literal) throws InvalidLiteralException
	{
		/*
		 * This call checks that the input is a literal and the datatype name
		 * matches. The return value is not needed because plain literal values
		 * cannot be canonicalized.
		 */
		getLexicalForm(literal);

		return literal;
	}

	@Override
	public boolean isPrimitive()
	{
		return true;
	}
}
