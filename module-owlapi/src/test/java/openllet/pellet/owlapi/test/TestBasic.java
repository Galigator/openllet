package openllet.pellet.owlapi.test;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import openllet.owlapi.OWL;
import openllet.owlapi.OWLGenericTools;
import openllet.owlapi.OWLHelper;
import openllet.owlapi.OWLManagerGroup;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;

/**
 * Test basic of openllet-owlapi
 * 
 * @since 2.6.0
 */
public class TestBasic
{
	static
	{
		System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "WARN");
	}

	@Test
	public void rule() throws OWLOntologyCreationException
	{
		try (final OWLManagerGroup group = new OWLManagerGroup())
		{
			final OWLOntologyID ontId = OWLHelper.getVersion(IRI.create("#owlapi.tests"), 1.0);

			final OWLHelper owl = new OWLGenericTools(group, ontId, true);

			final OWLClass ClsA = OWL.Class("#ClsA");
			final OWLNamedIndividual Ind1 = OWL.Individual("#Ind1");

			owl.declareIndividual(ClsA, Ind1);

			final List<OWLClass> entities = owl.getReasoner().getTypes(Ind1).entities().collect(Collectors.toList());
			assertTrue(entities.size() == 2);
			assertTrue(entities.contains(ClsA));
			assertTrue(entities.contains(OWL.Thing));
		}
	}
}
