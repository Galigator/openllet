// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.sparqlowl.parser.test;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import openllet.core.KnowledgeBase;
import openllet.core.utils.FileUtils;
import openllet.core.utils.SetUtils;
import openllet.jena.JenaLoader;
import openllet.query.sparqldl.parser.ARQParser;
import openllet.query.sparqlowl.parser.arq.ARQTerpParser;
import openllet.query.sparqlowl.parser.arq.TerpSyntax;
import openllet.test.PelletTestSuite;

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
@RunWith(Parameterized.class)
public class ParserTest
{
	public static final String base = PelletTestSuite.base + "/sparqldl-tests/simple/";

	@Parameters
	public static Collection<Object[]> getParameters()
	{
		final Collection<Object[]> parameters = new ArrayList<>();

		addParameter(parameters, "simple", 1, 8, 6);
		addParameter(parameters, "parent", 1, 11);

		return parameters;
	}

	private static void addParameter(final Collection<Object[]> parameters, final String prefix, final int minIndex, final int maxIndex, final Integer... ignoreIndices)
	{
		final Set<Integer> ignoreSet = SetUtils.create(ignoreIndices);
		for (int i = minIndex; i <= maxIndex; i++)
			if (!ignoreSet.contains(i))
				parameters.add(new Object[] { prefix + ".ttl", prefix + i + ".rq", prefix + i + ".terp" });
	}

	private static ARQParser _parser;

	private final String _kbFile;
	private KnowledgeBase _kb;
	private final String _sparqlFile;
	private final String _sparqlOWLFile;

	public ParserTest(final String kbFile, final String sparqlFile, final String sparqlOWLFile)
	{
		_kbFile = kbFile;
		_sparqlFile = sparqlFile;
		_sparqlOWLFile = sparqlOWLFile;
	}

	@BeforeClass
	public static void beforeClass()
	{
		ARQTerpParser.registerFactory();
	}

	@AfterClass
	public static void afterClass()
	{
		ARQTerpParser.unregisterFactory();
	}

	@Before
	public void before()
	{
		_kb = new JenaLoader().createKB(base + _kbFile);
		_parser = new ARQParser();
	}

	@After
	public void after()
	{
		_kb = null;
		_parser = null;
	}

	// https://www.slideshare.net/candp/owled-2010-terp
	@Test
	@Ignore
	public void compareQuery() throws FileNotFoundException, IOException
	{
		System.out.println(_kb.getIndividuals()); //  ./tests/src/test/resources/test/data/sparqldl-tests/simple/parent.ttl
		System.out.println(_kb.getObjectProperties());
		System.out.println(_kb.getAllClasses());
		System.out.println(_sparqlFile + "\t" + _sparqlOWLFile);
		//  ./tests/src/test/resources/test/data/sparqldl-tests/simple/parent1.rq
		//  ./tests/src/test/resources/test/data/sparqldl-tests/simple/parent1.terp
		final Query sparql = QueryFactory.create(FileUtils.readFile(base + _sparqlFile), Syntax.syntaxSPARQL);
		final openllet.query.sparqldl.model.Query expected = _parser.parse(sparql, _kb);

		final Query sparqlOWL = QueryFactory.create(FileUtils.readFile(base + _sparqlOWLFile), TerpSyntax.getInstance());
		final openllet.query.sparqldl.model.Query actual = _parser.parse(sparqlOWL, _kb);

		assertEquals(expected.getAtoms(), actual.getAtoms());
	}
}
