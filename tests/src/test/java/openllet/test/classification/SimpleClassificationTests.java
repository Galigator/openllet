// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.test.classification;

import static openllet.core.utils.TermFactory.all;
import static openllet.core.utils.TermFactory.inv;
import static openllet.core.utils.TermFactory.list;
import static openllet.core.utils.TermFactory.some;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;
import openllet.core.OpenlletOptions;
import openllet.jena.PelletReasonerFactory;
import openllet.test.AbstractKBTests;
import openllet.test.PelletTestSuite;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
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
public class SimpleClassificationTests extends AbstractKBTests
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter(SimpleClassificationTests.class);
	}

	@Test
	public void cdClassificationWithInverses()
	{
		classes(_C, _D, _E);
		objectProperties(_p);

		_kb.addSubClass(_C, some(_p, _D));
		_kb.addSubClass(_D, all(inv(_p), _E));

		assertTrue(_kb.isConsistent());
		assertTrue(_kb.isSubClassOf(_C, _E));

		_kb.classify();

		assertTrue(_kb.isSubClassOf(_C, _E));
	}

	@Test
	public void cdClassificationWithCyclicInverses()
	{
		classes(_C, _D, _E);
		objectProperties(_p, _q);

		_kb.addSubClass(_E, some(_p, _C));
		_kb.addSubClass(_C, all(inv(_p), _D));
		_kb.addSubClass(_D, some(_q, _E));

		assertTrue(_kb.isConsistent());
		assertTrue(_kb.isSubClassOf(_E, _D));

		_kb.classify();

		assertTrue(_kb.isSubClassOf(_E, _D));
	}

	@Test
	public void cdClassificationWithPropChain()
	{
		classes(_C, _D, _E);
		objectProperties(_p, _q, _r);

		_kb.addSubProperty(list(_p, _q), _r);
		_kb.addSubClass(_C, some(_p, some(_q, _D)));
		_kb.addSubClass(_D, all(inv(_r), _E));

		assertTrue(_kb.isConsistent());
		assertTrue(_kb.isSubClassOf(_C, _E));

		_kb.classify();

		_kb.printClassTree();

		assertTrue(_kb.isSubClassOf(_C, _E));
	}

	@Test
	public void unique_name_assumption()
	{
		try
		{
			final var data = ModelFactory.createDefaultModel();
			OpenlletOptions.USE_UNIQUE_NAME_ASSUMPTION = true;
			data.read(PelletTestSuite.base + "misc/two_individuals.ttl", "TTL");

			final var reasoner = PelletReasonerFactory.theInstance().create();
			final var infModel = ModelFactory.createInfModel(reasoner, data);

			final var l = infModel.listStatements().toList();
			for (final Statement s : l)
			{
				System.out.println(s);
				if ("differentFrom".equals(s.getPredicate().getLocalName()))
					assertFalse(s.toString(), s.getObject().equals(s.getSubject()));
			}
		}
		finally
		{
			OpenlletOptions.USE_UNIQUE_NAME_ASSUMPTION = false;
		}
	}
}
