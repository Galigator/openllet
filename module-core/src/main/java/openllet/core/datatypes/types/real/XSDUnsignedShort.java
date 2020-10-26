package openllet.core.datatypes.types.real;

import javax.xml.bind.DatatypeConverter;

import openllet.core.datatypes.exceptions.InvalidLiteralException;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.Namespaces;

/**
 * <p>
 * Title: <code>xsd:unsignedShort</code>
 * </p>
 * <p>
 * Description: Singleton implementation of <code>xsd:unsignedShort</code> datatype
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
public class XSDUnsignedShort extends AbstractDerivedIntegerType
{

	private static final XSDUnsignedShort instance = new XSDUnsignedShort();
	private static final int MAX_VALUE = 65535;

	public static XSDUnsignedShort getInstance()
	{
		return instance;
	}

	private XSDUnsignedShort()
	{
		super(ATermUtils.makeTermAppl(Namespaces.XSD + "unsignedShort"), 0, MAX_VALUE);
	}

	@Override
	protected Number fromLexicalForm(final String lexicalForm) throws InvalidLiteralException
	{
		try
		{
			final int i = DatatypeConverter.parseInt(lexicalForm);
			if (i < 0)
				throw new InvalidLiteralException(getName(), lexicalForm);
			if (i > MAX_VALUE)
				throw new InvalidLiteralException(getName(), lexicalForm);
			return i;
		}
		catch (final NumberFormatException e)
		{
			throw new InvalidLiteralException(getName(), lexicalForm, e);
		}
	}
}
