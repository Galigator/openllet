package openllet.core.datatypes.types.floating;

import javax.xml.bind.DatatypeConverter;

import openllet.aterm.ATermAppl;
import openllet.core.datatypes.AbstractBaseDatatype;
import openllet.core.datatypes.Datatype;
import openllet.core.datatypes.RestrictedDatatype;
import openllet.core.datatypes.exceptions.InvalidLiteralException;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.Namespaces;

/**
 * <p>
 * Title: <code>xsd:double</code>
 * </p>
 * <p>
 * Description: Singleton implementation of <code>xsd:double</code> datatype
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
public class XSDDouble extends AbstractBaseDatatype<Double>
{

	private static final XSDDouble instance;

	static
	{
		instance = new XSDDouble();
	}

	public static XSDDouble getInstance()
	{
		return instance;
	}

	private final RestrictedFloatingPointDatatype<Double> dataRange;

	/**
	 * Private constructor forces use of {@link #getInstance()}
	 */
	private XSDDouble()
	{
		super(ATermUtils.makeTermAppl(Namespaces.XSD + "double"));
		dataRange = new RestrictedFloatingPointDatatype<>(this, IEEEDoubleType.getInstance());
	}

	@Override
	public RestrictedDatatype<Double> asDataRange()
	{
		return dataRange;
	}

	@Override
	public ATermAppl getCanonicalRepresentation(final ATermAppl input) throws InvalidLiteralException
	{
		final Double f = getValue(input);
		final String canonicalForm = DatatypeConverter.printDouble(f);
		if (canonicalForm.equals(ATermUtils.getLiteralValue(input)))
			return input;
		else
			return ATermUtils.makeTypedLiteral(canonicalForm, getName());
	}

	@Override
	public ATermAppl getLiteral(final Object value)
	{
		if (value instanceof Double)
			return ATermUtils.makeTypedLiteral(DatatypeConverter.printDouble((Double) value), getName());
		else
			throw new IllegalArgumentException();
	}

	@Override
	public Datatype<?> getPrimitiveDatatype()
	{
		return this;
	}

	@Override
	public Double getValue(final ATermAppl literal) throws InvalidLiteralException
	{
		final String lexicalForm = getLexicalForm(literal);
		try
		{
			return DatatypeConverter.parseDouble(lexicalForm);
		}
		catch (final NumberFormatException e)
		{
			throw new InvalidLiteralException(getName(), lexicalForm, e);
		}
	}

	@Override
	public boolean isPrimitive()
	{
		return true;
	}

}
