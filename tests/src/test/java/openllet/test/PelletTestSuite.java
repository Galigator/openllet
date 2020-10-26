// Portions Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// Clark & Parsia, LLC parts of this source code are available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.test;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import openllet.datatypes.test.DatatypesSuite;
import openllet.explanation.test.ExplanationTestSuite;
import openllet.modularity.test.ModularityTestSuite;
import openllet.pellint.test.PellintTestSuite;
import openllet.test.annotations.AnnotationsTestSuite;
import openllet.test.classification.ClassificationTestSuite;
import openllet.test.el.ELTests;
import openllet.test.inctest.IncConsistencyTests;
import openllet.test.inctest.IncJenaConsistencyTests;
import openllet.test.owlapi.OWLAPITests;
import openllet.test.owlapi.OWLPrimerTests;
import openllet.test.query.QueryTestSuite;
import openllet.test.rbox.RBoxTestSuite;
import openllet.test.rules.RulesTestSuite;
import openllet.test.tbox.TBoxTests;
import openllet.test.transtree.TransTreeTestSuite;

@RunWith(Suite.class)
@SuiteClasses({ PellintTestSuite.class, //
		TracingTests.class, //
		MiscTests.class, //
		MergeTests.class, //
		RBoxTestSuite.class, //
		BlockingTests.class, //
		CacheSafetyTests.class, //
		JenaTests.class, //
		OWLAPITests.class, //
		OWLPrimerTests.class, //
		OWLAPIObjectConversionTests.class, //
		OWLAPIAxiomConversionTests.class, //
		IncConsistencyTests.class, //
		IncJenaConsistencyTests.class, //
		RulesTestSuite.class, //
		TBoxTests.class, //
		DatatypesSuite.class, //
		ELTests.class, //
		ExplanationTestSuite.class, //
		TestIsClass.class, //
		TestKnowledgeBase.class, //
		AnnotationsTestSuite.class, //
		TransTreeTestSuite.class, //
		LiebigTestSuite.class, //
		QueryTestSuite.class, //
		WebOntTestSuite.class, //
		DLTestSuite.class, //
		ClassificationTestSuite.class, //
		ModularityTestSuite.class, //
		LiteralComparisonTest.class, //
		// CLI Tests must go last, since some of them muck with PelletOptions!
		openllet.test.CLITests.class })
public class PelletTestSuite
{
	public static String base = new File("test/data/").exists() ? "test/data/" : "src/test/resources/test/data/";

	static
	{
		System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "WARN");
	}

	@Rule
	public TestName _currentMethodName = new TestName();

	@Before
	public void setUp()
	{
		System.out.println("====================BEGIN========================= [" + this.getClass().getSimpleName() + "." + _currentMethodName.getMethodName() + "] ");
	}
}
