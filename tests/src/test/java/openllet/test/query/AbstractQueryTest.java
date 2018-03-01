// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.test.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import openllet.aterm.ATermAppl;
import openllet.core.utils.ATermUtils;
import openllet.query.sparqldl.engine.QueryEngine;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.Query.VarType;
import openllet.query.sparqldl.model.QueryAtom;
import openllet.query.sparqldl.model.QueryImpl;
import openllet.query.sparqldl.model.QueryResult;
import openllet.query.sparqldl.model.ResultBinding;
import openllet.test.AbstractKBTests;
import org.junit.Assert;

/**
 * <p>
 * Copyright: Copyright (c) 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Evren Sirin
 */
public abstract class AbstractQueryTest extends AbstractKBTests
{
	protected static final ATermAppl x = ATermUtils.makeVar("x");
	protected static final ATermAppl y = ATermUtils.makeVar("y");
	protected static final ATermAppl z = ATermUtils.makeVar("z");

	protected ATermAppl[] select(final ATermAppl... vars)
	{
		return vars;
	}

	protected QueryAtom[] where(final QueryAtom... atoms)
	{
		return atoms;
	}

	protected Query ask(final QueryAtom... atoms)
	{
		return query(new ATermAppl[0], atoms);
	}

	protected Query query(final ATermAppl[] vars, final QueryAtom[] atoms)
	{
		final Query q = new QueryImpl(_kb, true);
		for (final ATermAppl var : vars)
			q.addResultVar(var);

		for (final QueryAtom atom : atoms)
			q.add(atom);

		for (final ATermAppl var : q.getUndistVars())
			q.addDistVar(var, VarType.INDIVIDUAL);

		return q;
	}

	protected void testQuery(final Query query, final boolean expected)
	{
		final QueryResult result = QueryEngine.exec(query);

		assertEquals(expected, !result.isEmpty());
	}

	protected void testQuery(final Query query, final ATermAppl[]... values)
	{
		final List<ATermAppl> resultVars = query.getResultVars();

		final Map<List<ATermAppl>, Integer> answers = new HashMap<>();
		for (final ATermAppl[] value : values)
		{
			final List<ATermAppl> answer = Arrays.asList(value);
			final Integer count = answers.get(answer);
			if (count == null)
				answers.put(answer, 1);
			else
				answers.put(answer, count + 1);

		}

		final QueryResult result = QueryEngine.exec(query);
		for (final ResultBinding binding : result)
		{
			final List<ATermAppl> list = new ArrayList<>(resultVars.size());
			for (final ATermAppl var : resultVars)
				list.add(binding.getValue(var));

			final Integer count = answers.get(list);
			if (count == null)
				Assert.fail("Unexpected binding in the result: " + list);
			else
				if (count == 1)
					answers.remove(list);
				else
					answers.put(list, count - 1);
		}

		assertTrue("Unfound bindings: " + answers.keySet(), answers.isEmpty());
	}

}
