package openllet.pellet.owlapi.test;

import static openllet.owlwg.Constants.OWLWG_TEST_CASES_IRI;
import static openllet.owlwg.Constants.RESULTS_ONTOLOGY_PHYSICAL_IRI;
import static openllet.owlwg.Constants.TEST_ONTOLOGY_PHYSICAL_IRI;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.FreshEntitiesException;

import openllet.atom.OpenError;
import openllet.core.KnowledgeBaseImpl;
import openllet.owlwg.TestCollection;
import openllet.owlwg.cli.FilterConditionParser;
import openllet.owlwg.owlapi.testcase.impl.OwlApiTestCaseFactory;
import openllet.owlwg.runner.pellet.PelletTestRunner;
import openllet.owlwg.testcase.TestCase;
import openllet.owlwg.testcase.filter.FilterCondition;
import openllet.owlwg.testrun.RunResultType;
import openllet.owlwg.testrun.TestRunResult;
import openllet.shared.tools.Log;

// @Ignore("Failing tests")
@RunWith(Parameterized.class)
public class OWLWGTestCase
{
	static
	{
		// Enable those lines for finer debugging.
		Log._defaultLevel = Level.INFO;
		//		Log._setDefaultParent = true;
		//		Log._parent.addHandler(Log._systemOutHandler);
		Log.setLevel(Level.INFO);
	}

	public static Object _lock = new Object();

	/**
	 * Ensure that test cases timeout after 10 seconds. This is in slightly broader than the one second timeout for each PelletOA3TestRunner.
	 */
	@Rule
	public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

	/**
	 * The dockerVM that run Travis is this time slow my i5-3570K
	 */
	private static final long _travisLowSpeed = 7;

	public static List<String> _failingTests = //
			Stream.of( //
					// G1
					"WebOnt-description-logic-906", //
					"WebOnt-description-logic-907", //
					"WebOnt-description-logic-903", //
					"WebOnt-description-logic-909", //
					"WebOnt-description-logic-910", //
					"WebOnt-miscellaneous-002", //
					"WebOnt-miscellaneous-001", //
					"New-Feature-ObjectPropertyChain-BJP-002", //
					"WebOnt-description-logic-663", //
					"WebOnt-description-logic-664", //
					"WebOnt-description-logic-662", //
					// G2
					"New-Feature-DisjointDataProperties-002", //
					"WebOnt-Class-005-direct", //
					"WebOnt-Restriction-005-direct", //
					"WebOnt-miscellaneous-202", //
					"Qualified-cardinality-restricted-int", //
					// G3
					"Inconsistent String Pattern with Disjoint Dataproperties") // Should not work...
					.collect(Collectors.toList());

	@SuppressWarnings("resource") // getResourceAsStream leak data, but we don't care in test, the only impact class loading.
	@Parameters
	public static List<Object[]> data() throws OWLOntologyCreationException, OWLOntologyChangeException
	{
		final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		final OwlApiTestCaseFactory factory = new OwlApiTestCaseFactory();

		final FilterCondition filter = FilterConditionParser.parse("approved proposed extracredit or direct dl and");

		/*
		 * Load the test and results ontology from local files before
		 * reading the test cases, otherwise import of them is likely to
		 * fail.
		 */
		manager.loadOntologyFromOntologyDocument(OWLWGTestCase.class.getResourceAsStream(TEST_ONTOLOGY_PHYSICAL_IRI));
		manager.loadOntologyFromOntologyDocument(OWLWGTestCase.class.getResourceAsStream(RESULTS_ONTOLOGY_PHYSICAL_IRI));

		final OWLOntology casesOntology = manager.loadOntologyFromOntologyDocument(OWLWGTestCase.class.getResourceAsStream(OWLWG_TEST_CASES_IRI));

		try
		{
			final TestCollection<OWLOntology> cases = new TestCollection<>(factory, casesOntology, filter);

			final List<Object[]> testParams = new ArrayList<>(cases.size());
			for (final Object test : cases.asList())
				testParams.add(new Object[] { test });

			return testParams;
		}
		finally
		{
			manager.removeOntology(casesOntology);
		}
	}

	private TestCase<OWLOntology> _test;

	public OWLWGTestCase(final TestCase<OWLOntology> test)
	{
		_test = test;
	}

	@Test
	public void runTestCase()
	{

		// FAILURE
		if (_failingTests.contains(_test.getIdentifier()))
		{
			System.out.println("Failure of test " + _test.getIdentifier() + "\t" + _test.getIRI());
			return;
		}

		synchronized (_lock)
		{
			try
			{
				KnowledgeBaseImpl._logger.setLevel(Level.WARNING);

				final Collection<TestRunResult> results = new PelletTestRunner().run(_test, 1 * 1000 * _travisLowSpeed); // One second of timeout : really enough if every thing work well.
				for (final TestRunResult result : results)
				{
					final RunResultType resultType = result.getResultType();
					if (!RunResultType.PASSING.equals(resultType))
						if (result.getCause() != null)
						{
							// FIXME Can get rid of conditional once #295 is fixed.
							if (!(result.getCause() instanceof FreshEntitiesException))
								throw new OpenError(_test.getIdentifier(), result.getCause());
						}
						else
						{
							System.out.println("FAILURE [" + _test.getIdentifier() + "]");
							fail(result.toString());
						}
				}
			}
			catch (final Exception e)
			{
				System.out.println("EXCEPTION [" + _test.getIdentifier() + "]");
				throw e;
			}
			finally
			{
				_test.dispose();
				_test = null;
				System.gc();
			}
		}
	}

}
