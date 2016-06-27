// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.rules.builtins;

import openllet.core.ABoxImpl;
import openllet.core.Literal;

/**
 * <p>
 * Title: Numeric Adapter
 * </p>
 * <p>
 * Description: Adapter from Numeric Functions to built-in Function.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 * 
 * @author Ron Alford
 */

public class NumericAdapter implements Function
{

	private final NumericFunction _function;

	public NumericAdapter(final NumericFunction function)
	{
		this._function = function;
	}

	@Override
	public Literal apply(final ABoxImpl abox, final Literal expected, final Literal... args)
	{
		Number expectedNum = null;
		Number result = null;
		Literal resultLit = null;

		if (expected != null)
		{
			if (!(expected.getValue() instanceof Number))
			{
				ABoxImpl._logger.info("Testing non-numeric against the result of a numeric _function '" + _function + "': " + expected);
				return null;
			}
			expectedNum = (Number) expected.getValue();
		}

		final Number[] numArgs = new Number[args.length];
		for (int i = 0; i < args.length; i++)
			if (args[i].getValue() instanceof Number)
				numArgs[i] = (Number) args[i].getValue();
			else
			{
				ABoxImpl._logger.info("Non numeric arguments to numeric _function '" + _function + "': " + args[i]);
				return null;
			}

		final NumericPromotion promoter = new NumericPromotion();
		promoter.promote(numArgs);
		final FunctionApplicationVisitor visitor = new FunctionApplicationVisitor(_function, expectedNum);
		promoter.accept(visitor);

		result = visitor.getResult();
		if (result != null)
			if (expected != null)
				resultLit = expected;
			else
			{
				final NumberToLiteralVisitor converter = new NumberToLiteralVisitor(abox);
				promoter.promote(result);
				promoter.accept(converter);
				resultLit = converter.getLiteral();
			}

		return resultLit;
	}

}
