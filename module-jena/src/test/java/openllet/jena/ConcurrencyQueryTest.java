/**
 *
 */
package openllet.jena;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.junit.Assert;
import org.junit.Test;

/**
 * Verifies that Pellet doesn't throw any exceptions when doing concurrent ABox queries provided that classification and realization are down synchronously
 * before.
 *
 * @author Pavel Klinov
 */
public class ConcurrencyQueryTest extends SequentialTestsContraintInitializer
{
	private static final String	ONTOLOGY_PATH	= "/vicodi.ttl";
	private static final Random	_random			= new Random();
	private static final float	RATIO			= 0.9f;			// 0.9% of the total number of tests samples. You can go far over 1 for performances test.

	@Test
	public void concurrentDLQueries() throws Exception
	{
		final long a = System.currentTimeMillis();

		System.err.println("Loading the ontology");
		final OntModel ontModel = loadOntologyModel(ONTOLOGY_PATH); // create Pellet reasoner

		System.err.println("Realizing the ontology");
		((PelletInfGraph) ontModel.getGraph()).realize(); // force classification and realization

		final long b = System.currentTimeMillis();
		System.err.println("Realizing in " + (b - a) + "ms");

		final List<Individual> buffer = new ArrayList<>(30_000);
		final Vector<Individual> streamSource = new Vector<>((int) (30_000 * RATIO));
		final Iterator<Individual> individuals = ontModel.listIndividuals(); // adding the individuals to the processing queue for the threads to process concurrently
		while (individuals.hasNext())
			buffer.add(individuals.next());

		for (int i = 0, l = (int) (buffer.size() * RATIO); i < l; i++) // Take random individual in random order to create the source.
			streamSource.add(buffer.get(_random.nextInt(buffer.size())));

		final long c = System.currentTimeMillis();
		System.err.println("Prepare source in " + (c - b) + "ms");

		streamSource.parallelStream()// It take as mush CPU core as available.
				.forEach(individual ->
				{
					final String indName = Thread.currentThread().getName() + ": " + individual.getLocalName() + " -- ";

					{
						final Iterator<? extends Property> propertyIter = ontModel.listObjectProperties(); // querying for all object property values for each individual

						while (propertyIter.hasNext())
						{
							final Property property = propertyIter.next();
							printIterator(individual.listPropertyValues(property), indName + property.getLocalName() + " --> ");
						}
					}
					{
						final Iterator<? extends Property> propertyIter = ontModel.listDatatypeProperties(); // querying for all _data property values for each individual

						while (propertyIter.hasNext())
						{
							final Property property = propertyIter.next();
							printIterator(individual.listPropertyValues(property), indName + property.getLocalName() + " --> ");
						}
					}
				});
		final long d = System.currentTimeMillis();
		System.err.println("Parallel queries in  " + (d - c) + "ms");
	}

	@SuppressWarnings("unused")
	private static void printIterator(final Iterator<?> iterator, final String threadId)
	{
		while (iterator.hasNext())
			Assert.assertTrue(null != iterator.next().toString()); //System.err.println(threadId + ": " + iterator.next());
	}

	private static OntModel loadOntologyModel(final String ontologyPath) throws IOException
	{
		final OntModel model = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);

		// read the file
		try (final InputStream ontStream = ConcurrencyQueryTest.class.getResourceAsStream(ontologyPath))
		{
			model.read(ontStream, null, "TTL");
		}
		return model;
	}
}
