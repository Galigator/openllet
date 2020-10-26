package openllet.core.datatypes.types.datetime;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import openllet.aterm.ATermAppl;
import openllet.core.datatypes.Datatype;
import openllet.core.datatypes.RestrictedDatatype;
import openllet.core.datatypes.exceptions.InvalidLiteralException;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.Namespaces;

/**
 * <p>
 * Title: <code>xsd:dateTimeStamp</code>
 * </p>
 * <p>
 * Description: Singleton implementation of <code>xsd:dateTimeStamp</code> datatype
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
public class XSDDateTimeStamp extends AbstractTimelineDatatype
{

	private static final XSDDateTimeStamp instance;

	static
	{
		instance = new XSDDateTimeStamp();
	}

	public static XSDDateTimeStamp getInstance()
	{
		return instance;
	}

	private final RestrictedTimelineDatatype dataRange;

	private XSDDateTimeStamp()
	{
		super(ATermUtils.makeTermAppl(Namespaces.XSD + "dateTimeStamp"), DatatypeConstants.DATETIME);

		dataRange = new RestrictedTimelineDatatype(this, DatatypeConstants.DATETIME, false);
	}

	@Override
	public RestrictedDatatype<XMLGregorianCalendar> asDataRange()
	{
		return dataRange;
	}

	@Override
	public ATermAppl getCanonicalRepresentation(final ATermAppl input) throws InvalidLiteralException
	{
		return XSDDateTime.getInstance().getLiteral(getValue(input));
	}

	@Override
	public Datatype<?> getPrimitiveDatatype()
	{
		return XSDDateTime.getInstance();
	}

	@Override
	public XMLGregorianCalendar getValue(final ATermAppl literal) throws InvalidLiteralException
	{
		final XMLGregorianCalendar c = super.getValue(literal);
		if (c.getTimezone() == DatatypeConstants.FIELD_UNDEFINED)
			throw new InvalidLiteralException(getName(), ATermUtils.getLiteralValue(literal));
		return c;
	}

	@Override
	public boolean isPrimitive()
	{
		return false;
	}
}
