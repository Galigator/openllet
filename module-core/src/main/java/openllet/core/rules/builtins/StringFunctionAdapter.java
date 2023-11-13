// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.rules.builtins;

import static openllet.core.rules.builtins.ComparisonTesters.expectedIfEquals;

import openllet.aterm.ATermAppl;
import openllet.core.boxes.abox.ABoxForRule;
import openllet.core.boxes.abox.Literal;
import openllet.core.utils.ATermUtils;

/**
 * <p>
 * Title: String-to-String Function Adapter
 * </p>
 * <p>
 * Description: Adapter from StringToStringFunction to Function
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
public class StringFunctionAdapter implements Function
{
	public final String _datatypeURI;
	public final StringToStringFunction _function;

	public StringFunctionAdapter(final StringToStringFunction function)
	{
		this(function, null);
	}

	public StringFunctionAdapter(final StringToStringFunction function, final String datatypeURI)
	{
		_datatypeURI = datatypeURI;
		_function = function;
	}

	@Override
	public Literal apply(final ABoxForRule abox, final Literal expected, final Literal... litArgs)
	{
		final String[] args = new String[litArgs.length];
		for (int i = 0; i < litArgs.length; i++)
			args[i] = ATermUtils.getLiteralValue(litArgs[i].getTerm());

		final String result = _function.apply(args);
		if (result == null)
			return null;

		ATermAppl resultTerm;
		if (_datatypeURI == null)
			resultTerm = ATermUtils.makePlainLiteral(result);
		else
			resultTerm = ATermUtils.makeTypedLiteral(result, _datatypeURI);

		final Literal resultLit = abox.addLiteral(resultTerm);

		return expectedIfEquals(expected, resultLit);
	}

}
