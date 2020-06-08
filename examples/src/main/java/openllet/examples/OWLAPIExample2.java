package openllet.examples;

import java.util.UUID;
import java.util.stream.Stream;
import openllet.owlapi.OWL;
import openllet.owlapi.OWLHelper;
import openllet.owlapi.OpenlletReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * Another example with owl-api and reasoning.
 *
 * @since 2.6.1
 */
public class OWLAPIExample2
{
	public final static void main(final String[] args) throws Exception
	{
		// Declare an ontology.

		final OWLOntology ontology = //
				OWLManager.createConcurrentOWLOntologyManager()//
						.createOntology(IRI.create("http://com.github.galigator.openllet/ontologies#demo2"));

		// Create the reasoner bind to the ontology and the helper I like.

		final OWLHelper h = OWLHelper.createLightHelper(OpenlletReasonerFactory.getInstance().createReasoner(ontology));

		// Define constants use to share information and build interpreter.

		final OWLNamedIndividual i = OWL.Individual("ind_i");
		final OWLNamedIndividual j = OWL.Individual("ind_j");
		final OWLNamedIndividual k = OWL.Individual("ind_k");
		final OWLNamedIndividual l = OWL.Individual("ind_l");

		final OWLClass a = OWL.Class("cls_a");
		final OWLClass b = OWL.Class("cls_b");
		final OWLClass c = OWL.Class("cls_c");
		final OWLClass d = OWL.Class("cls_d");
		final OWLClass e = OWL.Class("cls_e");
		final OWLClass f = OWL.Class("cls_f");
		final OWLClass g = OWL.Class("cls_g");

		final OWLObjectProperty u = OWL.ObjectProperty("prop_u");
		final OWLObjectProperty v = OWL.ObjectProperty("prop_v");
		final OWLObjectProperty w = OWL.ObjectProperty("prop_w");

		// Add a set of axioms into the ontology.

		h.addAxioms(//
				Stream.of(//
						OWL.classAssertion(i, a), //
						OWL.classAssertion(j, b), //
						OWL.classAssertion(k, c), //
						OWL.propertyAssertion(i, u, j), //
						OWL.propertyAssertion(j, v, k), //
						OWL.range(u, d), //
						OWL.range(u, e), //
						OWL.domain(w, g), //
						OWL.domain(v, e), //
						OWL.domain(v, f), //
						OWL.subClassOf(OWL.and(d, f), g)//
				));

		// Get the reasoner and flush the changes we just made.

		final OWLReasoner r = h.getReasoner();

		// This ontology is consistent, lets give a try of what can be deduce.

		System.out.println("\n Classes of " + i);
		r.getTypes(i).entities().forEach(System.out::println);
		// owl:Thing <cls_f> <cls_d> <cls_a>

		System.out.println("\n Classes of " + j);
		r.getTypes(j).entities().forEach(System.out::println);
		// owl:Thing <cls_f> <cls_d> <cls_e> <cls_b> <cls_a>

		System.out.println("\n Classes of " + k);
		r.getTypes(k).entities().forEach(System.out::println);
		// owl:Thing <cls_c>

		System.out.println("\n Value of i-u->?");
		r.getObjectPropertyValues(i, u).entities().forEach(System.out::println);
		// <ind_j>

		System.out.println("\n Individuals of a simple Class expression (negation of cls_e)");
		r.getInstances(OWL.not(e)).entities().forEach(System.out::println);

		// Optimize reasoning by reapplying infered types on actual set of axiom.
		h.addAxioms(//
				r.getTypes(j).entities()//
						.filter(cls -> !cls.equals(OWL.Thing))//
						.map(cls -> OWL.classAssertion(j, cls))//
		);

		System.out.println("\n Classes of " + i);
		h.getReasoner().getTypes(i).entities().forEach(System.out::println);

		System.out.println("\n The axioms are now. ");
		h.getOntology().axioms().map(OWLAxiom::toString).distinct().sorted().forEach(System.out::println);

		System.out.println("\n Playing with subClassing ");
		h.addAxiom(OWL.classAssertion(l, OWL.Thing)); // As a test you can comment this line to see how the result change.
		h.addAxiom(OWL.propertyAssertion(l, w, OWL.Individual(UUID.randomUUID().toString())));
		h.getReasoner().getTypes(l).entities().forEach(System.out::println);

		// Another simplification.
		h.addAxioms(//
				r.getTypes(l).entities()//
						.filter(cls -> !cls.equals(OWL.Thing))//
						.map(cls -> OWL.classAssertion(l, cls))//
		);

		System.out.println("\n The axioms are now. ");
		h.getOntology().axioms().map(OWLAxiom::toString).distinct().sorted().forEach(System.out::println);
	}
}
