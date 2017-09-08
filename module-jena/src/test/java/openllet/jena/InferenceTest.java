package openllet.jena;

import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.logging.Logger;
import openllet.core.KnowledgeBaseImpl;
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

public class InferenceTest
{
	private static final Logger LOG = Log.getLogger(InferenceTest.class);

	private static final String fname = "/ontology-plus-data.ttl";
	private static final String indiv = "http://ontology.tno.nl/2017/7/untitled-ontology-103/indiv";

	@Test
	public void testRemovalWithInference()
	{
		final Model model = ModelFactory.createDefaultModel();
		final InputStream is = InferenceTest.class.getResourceAsStream(fname);
		assert is != null;

		assert model.size() == 0;

		model.read(is, null, "TURTLE");

		final PelletReasoner reasoner = PelletReasonerFactory.theInstance().create(null);
		final InfModel inf = ModelFactory.createInfModel(reasoner, model);
		//reasoner.bindSchema(inf.getGraph());

		// printAllStatements(inf, 0);

		assertTrue(0 == printIndividualStatements(inf));

		//		{
		//			LOG.info("---- add Individual Statement ----");
		//			final Resource s = model.createResource(indiv);
		//			final Property p = model.createProperty("http://www.w3.org/2002/07/owl#sameAs");
		//			final Resource o = model.createResource(indiv);
		//
		//			final Statement stmt = ResourceFactory.createStatement(s, p, o);
		//
		//			model.add(stmt);
		//		}

		final PelletInfGraph graph = (PelletInfGraph) inf.getGraph();
		final KnowledgeBaseImpl kb = (KnowledgeBaseImpl) graph.getKB();

		assertTrue(1 == printIndividualStatements(inf));

		addIndividualStatementToModel(model);

		// printAllStatements(inf, 1);

		assertTrue(4 == printIndividualStatements(inf));

		removeIndividualStatementToModel(model);

		inf.rebind();
		inf.reset();

		assertTrue(0 == printIndividualStatements(inf));

		// printAllStatements(inf, 2);
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

		//		{
		//			final Resource s = m.createResource(indiv);
		//			final Property p = m.createProperty("http://www.w3.org/2002/07/owl#sameAs");
		//			final Resource o = m.createResource(indiv);
		//
		//			final Statement stmt = ResourceFactory.createStatement(s, p, o);
		//
		//			m.remove(stmt);
		//		}
	}

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
