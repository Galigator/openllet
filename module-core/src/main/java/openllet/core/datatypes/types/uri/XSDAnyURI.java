package openllet.core.datatypes.types.uri;

import java.net.URI;
import java.net.URISyntaxException;
import openllet.aterm.ATermAppl;
import openllet.core.datatypes.AbstractBaseDatatype;
import openllet.core.datatypes.Datatype;
import openllet.core.datatypes.RestrictedDatatype;
import openllet.core.datatypes.exceptions.InvalidLiteralException;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.Namespaces;

/**
 * <p>
 * Title: <code>xsd:string</code>
 * </p>
 * <p>
 * Description: Singleton implementation of <code>xsd:anyURI</code> datatype
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
public class XSDAnyURI extends AbstractBaseDatatype<ATermAppl>
{

	private static final XSDAnyURI instance;
	public static final ATermAppl NAME;

	static
	{
		NAME = ATermUtils.makeTermAppl(Namespaces.XSD + "anyURI");
		instance = new XSDAnyURI();
	}

	public static XSDAnyURI getInstance()
	{
		return instance;
	}

	private final RestrictedDatatype<ATermAppl> dataRange;

	private XSDAnyURI()
	{
		super(NAME);
		dataRange = new RestrictedURIDatatype(this);
	}

	@Override
	public RestrictedDatatype<ATermAppl> asDataRange()
	{
		return dataRange;
	}

	@Override
	public ATermAppl getCanonicalRepresentation(final ATermAppl input) throws InvalidLiteralException
	{
		return getValue(input);
	}

	@Override
	public ATermAppl getLiteral(final Object value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Datatype<?> getPrimitiveDatatype()
	{
		return this;
	}

	@Override
	public ATermAppl getValue(final ATermAppl literal) throws InvalidLiteralException
	{
		final String lexicalForm = getLexicalForm(literal).trim();

		try
		{
			return ATermUtils.makeTypedLiteral(new URI(lexicalForm).normalize().toString(), NAME);
		}
		catch (final URISyntaxException e)
		{
			throw new InvalidLiteralException(NAME, lexicalForm, e);
		}
	}

	@Override
	public boolean isPrimitive()
	{
		return false;
	}
}
