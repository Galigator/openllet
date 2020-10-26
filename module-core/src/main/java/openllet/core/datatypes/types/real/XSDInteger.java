package openllet.core.datatypes.types.real;

import javax.xml.bind.DatatypeConverter;

import openllet.core.datatypes.exceptions.InvalidLiteralException;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.Namespaces;

/**
 * <p>
 * Title: <code>xsd:integer</code>
 * </p>
 * <p>
 * Description: Singleton implementation of <code>xsd:integer</code> datatype
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
public class XSDInteger extends AbstractDerivedIntegerType
{

	private static final XSDInteger instance = new XSDInteger();

	public static XSDInteger getInstance()
	{
		return instance;
	}

	private XSDInteger()
	{
		super(ATermUtils.makeTermAppl(Namespaces.XSD + "integer"), null, null);
	}

	@Override
	protected Number fromLexicalForm(final String lexicalForm) throws InvalidLiteralException
	{
		try
		{
			return DatatypeConverter.parseInteger(lexicalForm);
		}
		catch (final NumberFormatException e)
		{
			throw new InvalidLiteralException(getName(), lexicalForm, e);
		}
	}
}
