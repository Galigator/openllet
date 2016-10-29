// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.modularity.test;

import static openllet.modularity.test.TestUtils.assertClassificationEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import openllet.modularity.AxiomBasedModuleExtractor;
import openllet.modularity.IncrementalClassifier;
import openllet.modularity.ModuleExtractor;
import openllet.modularity.PelletIncremantalReasonerFactory;
import openllet.modularity.io.IncrementalClassifierPersistence;
import openllet.owlapi.OWL;
import openllet.owlapi.OntologyUtils;
import openllet.owlapi.OpenlletReasoner;
import openllet.owlapi.OpenlletReasonerFactory;
import openllet.shared.tools.Log;
import openllet.test.PelletTestSuite;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * <p>
 * Copyright: Copyright (c) 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Blazej Bulka
 */
public class PersistenceUpdatesTest
{
	private static final Logger _logger = Log.getLogger(PersistenceUpdatesTest.class);

	public static final String base = PelletTestSuite.base + "modularity/";

	private static final String TEST_FILE = "test-persistence-classification.zip";

	public ModuleExtractor createModuleExtractor()
	{
		return new AxiomBasedModuleExtractor();
	}

	public void performPersistenceRemoves(final String fileName) throws IOException
	{
		final String common = "file:" + base + fileName;
		testPersistenceRemoves(common + ".owl");
	}

	public void performPersistenceAdds(final String fileName) throws IOException
	{
		final String common = "file:" + base + fileName;
		testPersistenceAdds(common + ".owl");
	}

	public void performPersistenceAllowedUpdates(final String fileName) throws IOException
	{
		final String common = "file:" + base + fileName;
		testAllowedUpdates(common + ".owl");
	}

	public void performUpdatesAfterPersistence(final String fileName) throws IOException
	{
		final String common = "file:" + base + fileName;
		testUpdatesAfterPersistence(common + ".owl");
	}

	public void performUpdatesAfterPersistence2(final String fileName) throws IOException
	{
		final String common = "file:" + base + fileName;
		testUpdatesAfterPersistence2(common + ".owl");
	}

	public void performUpdatesWhenPersisted(final String fileName) throws IOException
	{
		final String common = "file:" + base + fileName;
		testUpdatesWhenPersisted(common + ".owl");
	}

	public void testPersistenceRemoves(final String inputOnt) throws IOException
	{
		final File testFile = new File(TEST_FILE);
		final OWLOntology ontology = OntologyUtils.loadOntology(inputOnt);

		try
		{
			final ModuleExtractor moduleExtractor = createModuleExtractor();

			final IncrementalClassifier modular = PelletIncremantalReasonerFactory.getInstance().createReasoner(ontology, moduleExtractor);
			modular.classify();

			OWL._manager.removeAxioms(ontology, TestUtils.selectRandomAxioms(ontology, 1).stream());

			// at this point there should be a change to the ontology that is not applied yet to the classifier
			// this should cause the save operation to fail

			try (FileOutputStream fos = new FileOutputStream(testFile))
			{
				IncrementalClassifierPersistence.save(modular, fos);
				fail("The incremental classifer must not allow itself to be persisted if there are any unapplied changes to the ontology");
			}
			catch (final IllegalStateException e)
			{
				_logger.log(Level.FINE, "", e);
				assertTrue(testFile.delete());
				// correct behavior
			}

		}
		finally
		{
			if (ontology != null)
				OWL._manager.removeOntology(ontology);
		}
	}

	public void testPersistenceAdds(final String inputOnt) throws IOException
	{
		final File testFile = new File(TEST_FILE);
		final OWLOntology ontology = OntologyUtils.loadOntology(inputOnt);

		try
		{
			final ModuleExtractor moduleExtractor = createModuleExtractor();

			final IncrementalClassifier modular = PelletIncremantalReasonerFactory.getInstance().createReasoner(ontology, moduleExtractor);

			// first remove a random axiom
			final Set<OWLAxiom> axiomsToRemove = TestUtils.selectRandomAxioms(ontology, 1);

			OWL._manager.removeAxioms(ontology, axiomsToRemove.stream());

			// classify (i.e., update)
			modular.classify();

			// add the axiom back but do not classify (do not cause an update)
			OWL._manager.addAxioms(ontology, axiomsToRemove.stream());

			// at this point there should be a change to the ontology that is not applied yet to the classifier
			// this should cause the save operation to fail

			try (final FileOutputStream fos = new FileOutputStream(testFile))
			{
				IncrementalClassifierPersistence.save(modular, fos);
				fail("The incremental classifer must not allow itself to be persisted if there are any unapplied changes to the ontology");
			}
			catch (final IllegalStateException e)
			{
				_logger.log(Level.FINE, "", e);
				assertTrue(testFile.delete());
				// correct behavior
			}

			modular.dispose();
		}
		finally
		{
			if (ontology != null)
				OWL._manager.removeOntology(ontology);
		}
	}

	public void testAllowedUpdates(final String inputOnt) throws IOException
	{
		final File testFile = new File(TEST_FILE);
		final OWLOntology ontology = OntologyUtils.loadOntology(inputOnt);

		try
		{
			final ModuleExtractor moduleExtractor = createModuleExtractor();

			final IncrementalClassifier modular = PelletIncremantalReasonerFactory.getInstance().createReasoner(ontology, moduleExtractor);
			modular.classify();

			// first remove a random axiom
			final Set<OWLAxiom> axiomsToRemove = TestUtils.selectRandomAxioms(ontology, 1);

			OWL._manager.removeAxioms(ontology, axiomsToRemove.stream());
			// add the axiom back but do not classify
			OWL._manager.addAxioms(ontology, axiomsToRemove.stream());

			// remove another random axiom
			OWL._manager.removeAxioms(ontology, TestUtils.selectRandomAxioms(ontology, 1).stream());

			// classify (i.e., update)
			modular.classify();

			// at this point, the ontology should be updated (despite the changes), and the save should succeed.
			try (final FileOutputStream fos = new FileOutputStream(testFile))
			{
				IncrementalClassifierPersistence.save(modular, fos);
			}

			assertTrue(testFile.delete());
		}
		finally
		{
			if (null != ontology)
				OWL._manager.removeOntology(ontology);
		}
	}

	/**
	 * Tests whether the restored classifier can be updated. The test creates one original classifier (modular), persists it, reads it back as another
	 * classifier (modular2). Then it performs the same modifications of the ontology on them, and checks whether their behavior is identical.
	 *
	 * @param inputOnt
	 * @throws IOException
	 */
	public void testUpdatesAfterPersistence(final String inputOnt) throws IOException
	{
		final File testFile = new File(TEST_FILE);
		final OWLOntology ontology = OntologyUtils.loadOntology(inputOnt);

		try
		{
			final ModuleExtractor moduleExtractor = createModuleExtractor();

			IncrementalClassifier modular = PelletIncremantalReasonerFactory.getInstance().createReasoner(ontology, moduleExtractor);
			modular.classify();

			try (final FileOutputStream fos = new FileOutputStream(testFile))
			{
				IncrementalClassifierPersistence.save(modular, fos);
			}

			try (final FileInputStream fis = new FileInputStream(testFile))
			{
				modular = IncrementalClassifierPersistence.load(fis);
			}

			// first remove a random axiom
			OWL._manager.removeAxioms(ontology, TestUtils.selectRandomAxioms(ontology, 1).stream());

			modular.classify();

			final OpenlletReasoner expected = OpenlletReasonerFactory.getInstance().createReasoner(modular.getRootOntology());

			assertClassificationEquals(expected, modular);
		}
		finally
		{
			if (null != ontology)
				OWL._manager.removeOntology(ontology);
		}
	}

	/**
	 * Tests whether the restored classifier can be updated. The test creates one original classifier (modular), persists it, reads it back as another
	 * classifier (modular2). Then it performs the same modifications of the ontology on them, and checks whether their behavior is identical.
	 *
	 * @param inputOnt
	 * @throws IOException
	 */
	public void testUpdatesAfterPersistence2(final String inputOnt) throws IOException
	{
		final File testFile = new File(TEST_FILE);
		final OWLOntology ontology = OntologyUtils.loadOntology(inputOnt);

		try
		{
			IncrementalClassifier modular = PelletIncremantalReasonerFactory.getInstance().createReasoner(ontology, createModuleExtractor());
			modular.classify();

			try (final FileOutputStream fos = new FileOutputStream(testFile))
			{
				IncrementalClassifierPersistence.save(modular, fos);
			}

			try (final FileInputStream fis = new FileInputStream(testFile))
			{
				modular = IncrementalClassifierPersistence.load(fis, ontology);
			}

			// first remove a random axiom
			OWL._manager.removeAxioms(ontology, TestUtils.selectRandomAxioms(ontology, 1).stream());

			modular.classify();

			final OpenlletReasoner expected = OpenlletReasonerFactory.getInstance().createReasoner(ontology);

			assertClassificationEquals(expected, modular);
		}
		finally
		{
			if (null != ontology)
				OWL._manager.removeOntology(ontology);
		}
	}

	public void testUpdatesWhenPersisted(final String inputOnt) throws IOException
	{
		final File testFile = new File(TEST_FILE);
		final OWLOntology ontology = OntologyUtils.loadOntology(inputOnt);

		try
		{
			IncrementalClassifier modular = PelletIncremantalReasonerFactory.getInstance().createReasoner(ontology, createModuleExtractor());
			modular.classify();

			try (final FileOutputStream fos = new FileOutputStream(testFile))
			{
				IncrementalClassifierPersistence.save(modular, fos);
			}

			// perform changes while the classifier is stored on disk; first remove a random axiom
			OWL._manager.removeAxioms(ontology, TestUtils.selectRandomAxioms(ontology, 1).stream());

			try (final FileInputStream fis = new FileInputStream(testFile))
			{
				modular = IncrementalClassifierPersistence.load(fis, ontology);
			}

			final OpenlletReasoner expected = OpenlletReasonerFactory.getInstance().createReasoner(ontology);

			assertClassificationEquals(expected, modular);
		}
		finally
		{
			if (null != ontology)
				OWL._manager.removeOntology(ontology);
		}
	}

	@Test
	public void miniTambisPersistenceAddsTest() throws IOException
	{
		performPersistenceAdds("miniTambis");
	}

	@Test
	public void miniTambisPersistenceRemovesTest() throws IOException
	{
		performPersistenceRemoves("miniTambis");
	}

	@Test
	public void miniTambisPersistenceAllowedUpdatesTest() throws IOException
	{
		performPersistenceAllowedUpdates("miniTambis");
	}

	@Test
	public void miniTambisUpdatesAfterPersistenceTest() throws IOException
	{
		performUpdatesAfterPersistence("miniTambis");
	}

	@Test
	public void miniTambisUpdatesAfterPersistence2Test() throws IOException
	{
		performUpdatesAfterPersistence2("miniTambis");
	}

	@Test
	public void miniTambisUpdatesWhenPersistedTest() throws IOException
	{
		performUpdatesWhenPersisted("miniTambis");
	}
}
