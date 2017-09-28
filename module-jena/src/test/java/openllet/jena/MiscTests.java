/**
 *
 */
package openllet.jena;

import java.util.logging.Logger;
import openllet.core.OpenlletOptions;
import openllet.core.exceptions.InconsistentOntologyException;
import openllet.shared.tools.Log;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.OWL2;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Pavel Klinov
 */
public class MiscTests
{
	private static final Logger _logger = Log.getLogger(MiscTests.class);

	public static void configurePelletOptions()
	{
		OpenlletOptions.PROCESS_JENA_UPDATES_INCREMENTALLY = false;
		OpenlletOptions.ALWAYS_REBUILD_RETE = false;
		OpenlletOptions.USE_UNIQUE_NAME_ASSUMPTION = true;
		OpenlletOptions.USE_COMPLETION_QUEUE = false;
		OpenlletOptions.AUTO_REALIZE = false;
	}

	private static final String NAMESPACE = "http://www.inmindcomputing.com/example";
	private OntModel _model;

	@Test
	public void dataAssertionTest()
	{
		_model.read(MiscTests.class.getResourceAsStream("/decimal-int.owl"), null);
		final Individual entity = _model.getIndividual(NAMESPACE + "/dataAssertion.owl#ENTITY");
		final DatatypeProperty value = _model.getDatatypeProperty(NAMESPACE + "/dataAssertion.owl#dataAssertionValue");
		Assert.assertTrue(value.isFunctionalProperty());
		Assert.assertEquals(1, entity.listPropertyValues(value).toSet().size());
	}

	@Test
	public void incrementalDeletionTest()
	{
		final Individual entity = _model.createIndividual(NAMESPACE + "/dataAssertion.owl#ENTITY", null);
		final DatatypeProperty property = _model.createDatatypeProperty(NAMESPACE + "/dataAssertion.owl#ENTITY", true);

		final Statement firstValue = _model.createLiteralStatement(entity, property, "1");
		final Statement secondValue = _model.createLiteralStatement(entity, property, "2");

		// TODO: prepare registers PelletGraphListener. This implies a different behaviour of the whole graph before and
		// after the first read operation. Please, delete comment if this is as designed.
		_model.prepare();
		_model.add(firstValue);
		_model.remove(firstValue);
		_model.add(secondValue);

		try
		{
			_model.listObjectsOfProperty(property).toSet();
		}
		catch (final InconsistentOntologyException e)
		{
			Log.error(_logger, e);
			Assert.fail("Both values are contained in the knowledge base.");
		}
	}

	@Before
	public void setUp()
	{
		_model = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
		configurePelletOptions();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown()
	{
		_model.close();
	}

	@Test
	public void universalTest()
	{
		_model.read(MiscTests.class.getResourceAsStream("/universal-property.owl"), null);
		final ObjectProperty universal = _model.getObjectProperty(NAMESPACE + "/universal.owl#universalProperty");
		final ObjectProperty abstracT = _model.getObjectProperty(NAMESPACE + "/universal.owl#abstractProperty");
		final ObjectProperty concrete = _model.getObjectProperty(NAMESPACE + "/universal.owl#concreteProperty");
		Assert.assertTrue(universal.getEquivalentProperty().equals(OWL2.topObjectProperty));
		Assert.assertTrue(universal.listSubProperties().toSet().contains(abstracT));
		Assert.assertTrue(universal.listSubProperties().toSet().contains(concrete));
	}

}
