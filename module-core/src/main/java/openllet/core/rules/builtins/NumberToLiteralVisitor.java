// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.rules.builtins;

import java.math.BigDecimal;
import java.math.BigInteger;

import openllet.core.boxes.abox.ABoxForRule;
import openllet.core.boxes.abox.Literal;
import openllet.core.exceptions.InternalReasonerException;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.Namespaces;

/**
 * <p>
 * Title: Number To Literal Visitor
 * </p>
 * <p>
 * Description: Convert from a Number object to a pellet Literal.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Ron Alford
 */
public class NumberToLiteralVisitor implements NumericVisitor
{

	private final ABoxForRule _abox;
	private Literal _result;

	public NumberToLiteralVisitor(final ABoxForRule abox)
	{
		_abox = abox;
	}

	private static void argCheck(final Number[] args)
	{
		if (args.length != 1)
			throw new InternalReasonerException("Wrong number of arguments to visitor.");
	}

	public Literal getLiteral()
	{
		return _result;
	}

	private void setLiteral(final Number arg, final String typeURI)
	{
		_result = _abox.addLiteral(ATermUtils.makeTypedLiteral(arg.toString(), typeURI));
	}

	@Override
	public void visit(final BigDecimal[] args)
	{
		argCheck(args);
		setLiteral(args[0], Namespaces.XSD + "decimal");
	}

	@Override
	public void visit(final BigInteger[] args)
	{
		argCheck(args);
		setLiteral(args[0], Namespaces.XSD + "integer");
	}

	@Override
	public void visit(final Double[] args)
	{
		argCheck(args);
		setLiteral(args[0], Namespaces.XSD + "double");
	}

	@Override
	public void visit(final Float[] args)
	{
		argCheck(args);
		setLiteral(args[0], Namespaces.XSD + "float");
	}

}
