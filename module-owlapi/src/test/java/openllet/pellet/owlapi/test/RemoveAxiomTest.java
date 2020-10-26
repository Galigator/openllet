package openllet.pellet.owlapi.test;

import org.junit.Assert;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import openllet.owlapi.OWL;
import openllet.owlapi.OpenlletReasonerFactory;

public class RemoveAxiomTest
{
	@Test
	public void test() throws Exception
	{
		final OWLOntology ontology = OWLManager.createOWLOntologyManager().createOntology();

		final OWLClass theClass = OWL.Class("Cls");

		final OWLObjectProperty r = OWL.ObjectProperty("R");
		final OWLObjectProperty inverseR = OWL.ObjectProperty("inverseR");
		final OWLNamedIndividual a = OWL.Individual("A");
		final OWLNamedIndividual b = OWL.Individual("B");

		ontology.addAxioms(//
				OWL.inverseProperties(r, inverseR), //
				OWL.classAssertion(a, theClass), //
				OWL.classAssertion(b, theClass), //
				OWL.propertyAssertion(a, r, b));

		final OWLReasoner reasoner = OpenlletReasonerFactory.getInstance().createReasoner(ontology);
		Assert.assertTrue(reasoner.getObjectPropertyValues(a, r).containsEntity(b));
		Assert.assertTrue(reasoner.getObjectPropertyValues(b, inverseR).containsEntity(a));

		ontology.remove(OWL.propertyAssertion(a, inverseR, b));
		ontology.remove(//
				reasoner.inverseObjectProperties(inverseR)// Removing every way of getting the inverse.
						.map(inverse -> OWL.propertyAssertion(a, inverse, b))//
		);

		reasoner.flush();

		Assert.assertFalse(reasoner.getObjectPropertyValues(a, r).containsEntity(b));
		Assert.assertFalse(reasoner.getObjectPropertyValues(b, inverseR).containsEntity(a));

		reasoner.equivalentObjectProperties(inverseR).forEach(System.out::println);
		Assert.assertTrue(reasoner.equivalentObjectProperties(inverseR)//
				.anyMatch(OWLObjectInverseOf.class::isInstance));
	}
}
