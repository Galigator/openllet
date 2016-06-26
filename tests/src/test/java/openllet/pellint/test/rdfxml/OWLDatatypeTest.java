package openllet.pellint.test.rdfxml;

import static org.junit.Assert.assertTrue;

import openllet.pellint.rdfxml.OWLSyntaxChecker;
import openllet.pellint.rdfxml.RDFLints;
import openllet.pellint.rdfxml.RDFModel;
import openllet.pellint.rdfxml.RDFModelReader;
import openllet.test.PelletTestSuite;
import org.junit.Test;

/**
 * Tests for datatypes in lint
 *
 * @author Blazej Bulka <blazej@clarkparsia.com>
 */
public class OWLDatatypeTest
{
	private static final String TEST_438_DATA = PelletTestSuite.base + "/misc/ticket-438.ttl";

	/**
	 * Test for ticket 438. (Lint reported user-defined datatypes as "untyped classes" because they used owl:equivalentClass to connect a named datatype with an
	 * anonymous datatype; in the implementation of lint at that time, it was _expected (incorrectly) that both arguments to equivalentClasses are types).
	 */
	@Test
	public void testDatatypeEquivalentClass()
	{
		final RDFModelReader modelReader = new RDFModelReader();
		final RDFModel rdfModel = modelReader.read(TEST_438_DATA, false /* loadImports */);

		final OWLSyntaxChecker checker = new OWLSyntaxChecker();
		final RDFLints lints = checker.validate(rdfModel);

		assertTrue(lints.isEmpty());
	}
}
