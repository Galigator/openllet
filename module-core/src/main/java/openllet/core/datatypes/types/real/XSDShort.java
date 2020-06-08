package openllet.core.datatypes.types.real;

import javax.xml.bind.DatatypeConverter;
import openllet.core.datatypes.exceptions.InvalidLiteralException;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.Namespaces;

/**
 * <p>
 * Title: <code>xsd:short</code>
 * </p>
 * <p>
 * Description: Singleton implementation of <code>xsd:short</code> datatype
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
public class XSDShort extends AbstractDerivedIntegerType
{

	private static final XSDShort instance = new XSDShort();

	public static XSDShort getInstance()
	{
		return instance;
	}

	private XSDShort()
	{
		super(ATermUtils.makeTermAppl(Namespaces.XSD + "short"), Short.MIN_VALUE, Short.MAX_VALUE);
	}

	@Override
	protected Number fromLexicalForm(final String lexicalForm) throws InvalidLiteralException
	{
		try
		{
			final int n = DatatypeConverter.parseInt(lexicalForm);
			if (n < Short.MIN_VALUE || n > Short.MAX_VALUE) throw new InvalidLiteralException(getName(), lexicalForm);
			return Short.valueOf((short) n);
		}
		catch (final NumberFormatException e)
		{
			throw new InvalidLiteralException(getName(), lexicalForm, e);
		}
	}
}
