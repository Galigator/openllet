package openllet.pellet.owlapi.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import openllet.owlapi.OWL;
import openllet.owlapi.OWLGenericTools;
import openllet.owlapi.OWLHelper;
import openllet.owlapi.OWLManagerGroup;
import openllet.owlapi.SWRL;
import openllet.shared.tools.Log;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;

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
		Log.setLevel(Level.WARNING, OWLGenericTools.class);
	}

	@Test
	public void rule() throws OWLOntologyCreationException
	{
		try (final OWLManagerGroup group = new OWLManagerGroup())
		{
			final OWLOntologyID ontId = OWLHelper.getVersion(IRI.create("#owlapi.tests"), 1.0);

			final OWLHelper owl = new OWLGenericTools(group, ontId, true);
			final OWLClass ClsA = OWL.Class("#ClsA");
			final OWLClass ClsB = OWL.Class("#ClsB");
			final OWLClass ClsC = OWL.Class("#ClsC");
			final OWLClass ClsD = OWL.Class("#ClsD");
			final OWLClass ClsE = OWL.Class("#ClsE");
			final OWLClass ClsF = OWL.Class("#ClsF");
			final OWLClass ClsG = OWL.Class("#ClsG");
			final OWLNamedIndividual Ind1 = OWL.Individual("#Ind1");
			final SWRLVariable varA = SWRL.variable(IRI.create("#a"));

			owl.declareIndividual(ClsA, Ind1);

			{
				final List<OWLClass> entities = owl.getReasoner().getTypes(Ind1).entities().collect(Collectors.toList());
				assertTrue(entities.size() == 2);
				assertTrue(entities.contains(ClsA));
				assertTrue(entities.contains(OWL.Thing));
			}

			owl.addAxiom(SWRL.rule(//
					SWRL.antecedent(SWRL.classAtom(ClsA, varA)), //
					SWRL.consequent(SWRL.classAtom(ClsB, varA))//
			));

			{
				final List<OWLClass> entities = owl.getReasoner().getTypes(Ind1).entities().collect(Collectors.toList());
				assertTrue(entities.size() == 3);
				assertTrue(entities.contains(ClsA));
				assertTrue(entities.contains(ClsB));
				assertTrue(entities.contains(OWL.Thing));
			}

			owl.addAxiom(SWRL.rule(//
					SWRL.antecedent(SWRL.classAtom(ClsB, varA)), //
					SWRL.consequent(SWRL.classAtom(ClsC, varA))//
			));

			{
				final List<OWLClass> entities = owl.getReasoner().getTypes(Ind1).entities().collect(Collectors.toList());
				assertTrue(entities.size() == 4);
				assertTrue(entities.contains(ClsA));
				assertTrue(entities.contains(ClsB));
				assertTrue(entities.contains(ClsC));
				assertTrue(entities.contains(OWL.Thing));
			}

			owl.addClass(Ind1, ClsD);

			owl.addAxiom(SWRL.rule(//
					SWRL.antecedent(SWRL.classAtom(OWL.and(ClsD, ClsC), varA)), //
					SWRL.consequent(SWRL.classAtom(ClsE, varA))//
			));

			{
				final List<OWLClass> entities = owl.getReasoner().getTypes(Ind1).entities().collect(Collectors.toList());
				assertTrue(entities.size() == 6);
				assertTrue(entities.contains(ClsA));
				assertTrue(entities.contains(ClsB));
				assertTrue(entities.contains(ClsC));
				assertTrue(entities.contains(ClsD));
				assertTrue(entities.contains(ClsE));
				assertTrue(entities.contains(OWL.Thing));
			}

			owl.addClass(Ind1, OWL.not(ClsF)); // Mark the negation to enforce the open world assertion.

			final SWRLRule D_and_NotF = SWRL.rule(//
					SWRL.antecedent(SWRL.classAtom(OWL.and(ClsD, OWL.not(ClsF)), varA)), //
					SWRL.consequent(SWRL.classAtom(ClsG, varA))//
			);

			{
				owl.addAxiom(D_and_NotF);
				final List<OWLClass> entities = owl.getReasoner().getTypes(Ind1).entities().collect(Collectors.toList());
				assertTrue(entities.contains(ClsG));
				owl.removeAxiom(D_and_NotF);
			}

			final SWRLRule D_and_F = SWRL.rule(//
					SWRL.antecedent(SWRL.classAtom(OWL.and(ClsD, ClsF), varA)), //
					SWRL.consequent(SWRL.classAtom(ClsG, varA))//
			);

			{
				owl.addAxiom(D_and_F);
				final List<OWLClass> entities = owl.getReasoner().getTypes(Ind1).entities().collect(Collectors.toList());
				assertFalse(entities.contains(ClsG));
				owl.removeAxiom(D_and_F);
			}
		}
	}
}
