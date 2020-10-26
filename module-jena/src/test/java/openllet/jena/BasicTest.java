package openllet.jena;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

/**
 * @since 2.6.4
 */
public class BasicTest
{
	private static void test(final String file)
	{
		final OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		m.read(file);

		final InfModel model = ModelFactory.createInfModel(PelletReasonerFactory.theInstance().create(), m);
		model.listStatements();
	}

	@Test
	public void testCrashA()
	{
		//		test("src/test/resources/bigtu-wapa-model.instances.ttl"); // stackOverFlow
	}

	@Test
	public void testB()
	{
		test("src/test/resources/decimal-int.owl");
	}

	@Test
	public void testCrashC()
	{
		//		test("src/test/resources/jena-datatypes.owl"); // [line: 1, col: 1] Content not allow in prolog
	}

	@Test
	public void testD()
	{
		test("src/test/resources/ontology-plus-data.ttl");
	}

	@Test
	public void testE()
	{
		test("src/test/resources/universal-property.owl");
	}

	@Test
	public void testCrashF()
	{
		// test("src/test/resources/vicodi.ttl"); // disable because 1793s to run
	}

}
