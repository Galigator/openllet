// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public
// License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.test.query;

import static openllet.core.utils.TermFactory.TOP;
import static openllet.core.utils.TermFactory.not;
import static openllet.query.sparqldl.model.QueryAtomFactory.NotKnownAtom;
import static openllet.query.sparqldl.model.QueryAtomFactory.PropertyValueAtom;
import static openllet.query.sparqldl.model.QueryAtomFactory.SubClassOfAtom;
import static openllet.query.sparqldl.model.QueryAtomFactory.TypeAtom;
import static openllet.test.PelletTestCase.assertIteratorValues;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import openllet.aterm.ATermAppl;
import openllet.core.utils.ATermUtils;
import openllet.query.sparqldl.engine.QueryEngine;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.QueryAtom;
import openllet.query.sparqldl.model.QueryImpl;
import openllet.query.sparqldl.model.QueryResult;
import openllet.query.sparqldl.model.ResultBinding;
import openllet.query.sparqldl.model.Query.VarType;
import openllet.test.AbstractKBTests;
import org.junit.Test;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Evren Sirin
 */
public class TestBooleanQueries extends AbstractKBTests
{
	private static final ATermAppl x = ATermUtils.makeVar("x");
	private static final ATermAppl y = ATermUtils.makeVar("y");

	private Query query(final QueryAtom... atoms)
	{
		final Query q = new QueryImpl(_kb, true);
		for (final QueryAtom atom : atoms)
			q.add(atom);
		return q;
	}

	private void testQuery(final boolean expected, final Query query)
	{
		assertEquals(expected, !QueryEngine.exec(query).isEmpty());
	}

	private void testABoxQuery(final boolean expected, final Query query)
	{
		assertEquals(expected, QueryEngine.execBooleanABoxQuery(query));
	}

	@Test
	public void testBooleanQueries()
	{
		classes(_A, _B);
		objectProperties(_p);
		individuals(_a, _b);

		_kb.addType(_a, _A);
		_kb.addType(_b, _B);

		_kb.addPropertyValue(_p, _a, _b);

		final Query q1 = query(TypeAtom(x, _A));
		final Query q2 = query(TypeAtom(x, _B));
		final Query q3 = query(PropertyValueAtom(x, _p, y), TypeAtom(y, _B));
		final Query q4 = query(TypeAtom(x, _A), PropertyValueAtom(x, _p, y), TypeAtom(y, _B));
		final Query q5 = query(TypeAtom(x, _C));
		final Query q6 = query(TypeAtom(x, _A), TypeAtom(x, _C));

		testABoxQuery(true, q1);
		testABoxQuery(true, q2);
		testABoxQuery(true, q3);
		testABoxQuery(true, q4);
		testABoxQuery(false, q5);
		testABoxQuery(false, q6);

		_kb.removePropertyValue(_p, _a, _b);

		testABoxQuery(true, q1);
		testABoxQuery(true, q2);
		testABoxQuery(false, q3);
		testABoxQuery(false, q4);
		testABoxQuery(false, q5);
		testABoxQuery(false, q6);

		_kb.addSubClass(TOP, _C);

		testABoxQuery(true, q1);
		testABoxQuery(true, q2);
		testABoxQuery(false, q3);
		testABoxQuery(false, q4);
		testABoxQuery(true, q5);
		testABoxQuery(true, q6);
	}

	@Test
	public void testMixedQuery()
	{
		classes(_A, _B, _C);
		individuals(_a);

		_kb.addSubClass(_A, _C);
		_kb.addSubClass(_B, _C);

		_kb.addType(_a, _A);

		final Query q1 = query(SubClassOfAtom(x, _C), TypeAtom(y, x));
		q1.addDistVar(x, VarType.CLASS);
		q1.addResultVar(x);

		final QueryResult qr = QueryEngine.exec(q1);

		final List<ATermAppl> results = new ArrayList<>();
		for (final ResultBinding result : qr)
		{
			System.out.println(result);
			results.add(result.getValue(x));
		}

		assertIteratorValues(results.iterator(), new ATermAppl[] { _A, _C });
	}

	@Test
	public void testNegatedBooleanQueries1()
	{
		classes(_A, _B);
		individuals(_a);

		_kb.addType(_a, _A);

		final Query q1 = query(NotKnownAtom(TypeAtom(_a, _A)));
		final Query q2 = query(NotKnownAtom(TypeAtom(_a, _B)));
		final Query q3 = query(NotKnownAtom(TypeAtom(_a, not(_A))));
		final Query q4 = query(NotKnownAtom(TypeAtom(_a, not(_B))));

		testQuery(false, q1);
		testQuery(true, q2);
		testQuery(true, q3);
		testQuery(true, q4);

		_kb.addDisjointClass(_A, _B);

		testQuery(false, q1);
		testQuery(true, q2);
		testQuery(true, q3);
		testQuery(false, q4);
	}
}
