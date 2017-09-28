package openllet.jena;

import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.logging.Logger;
import openllet.shared.tools.Log;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.junit.Test;

public class AddDelTripleInferenceTest extends SequentialTestsContraintInitializer
{
	private static final Logger LOG = Log.getLogger(AddDelTripleInferenceTest.class);

	private static final String fname = "/ontology-plus-data.ttl";
	private static final String indiv = "http://ontology.tno.nl/2017/7/untitled-ontology-103/indiv";

	@Test
	public void testRemovalWithInference()
	{
		final Model model = ModelFactory.createDefaultModel();
		final InputStream is = AddDelTripleInferenceTest.class.getResourceAsStream(fname);
		assert is != null;

		assert model.size() == 0;

		model.read(is, null, "TURTLE");

		final PelletReasoner reasoner = PelletReasonerFactory.theInstance().create(null);
		final InfModel inf = ModelFactory.createInfModel(reasoner, model);

		{
			final long count = printIndividualStatements(inf);
			assertTrue("expected 0 but get " + count, 0 == count);
		}

		addIndividualStatementToModel(model);

		{
			final long count = printIndividualStatements(inf);
			assertTrue("expected 4 but get " + count, 4 == count);
		}

		removeIndividualStatementToModel(model);

		inf.rebind();
		inf.reset();

		{
			final long count = printIndividualStatements(inf);
			assertTrue("expected 0 but get " + count, 0 == count);
		}
	}

	private static long printIndividualStatements(final Model m)
	{
		LOG.info("---- print Individual Statements ----");
		final Resource a = m.getResource(indiv);

		long count = 0;
		final StmtIterator si = a.listProperties();
		while (si.hasNext())
		{
			final Statement aStmt = si.next();
			LOG.info(() -> "Stmt: " + aStmt);
			count++;
		}
		return count;
	}

	private static void addIndividualStatementToModel(final Model m)
	{
		LOG.info("---- add Individual Statement ----");
		final Resource s = m.createResource(indiv);
		final Property p = m.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		final Resource o = m.createResource("http://ontology.tno.nl/2017/7/untitled-ontology-103/TestClass");

		final Statement stmt = ResourceFactory.createStatement(s, p, o);

		m.add(stmt);
	}

	private static void removeIndividualStatementToModel(final Model m)
	{
		LOG.info("---- remove Individual Statements ----");
		{
			final Resource s = m.createResource(indiv);
			final Property p = m.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
			final Resource o = m.createResource("http://ontology.tno.nl/2017/7/untitled-ontology-103/TestClass");

			final Statement stmt = ResourceFactory.createStatement(s, p, o);

			m.remove(stmt);
		}
	}

	@SuppressWarnings("unused")
	private static void printAllStatements(final Model m, final int n)
	{
		LOG.info("\\/---- print all statements ---\\/" + n);
		final StmtIterator iter = m.listStatements();
		while (iter.hasNext())
		{
			final Statement s = iter.next();

			LOG.info(() -> "Stmt: " + s);
		}
		LOG.info("/\\---- print all statements ---/\\" + n);
	}

}
