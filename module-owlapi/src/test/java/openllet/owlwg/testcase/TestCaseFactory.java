package openllet.owlwg.testcase;

import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

public interface TestCaseFactory<O>
{

	ConsistencyTest<O> getConsistencyTestCase(OWLOntology o, OWLNamedIndividual i);

	InconsistencyTest<O> getInconsistencyTestCase(OWLOntology o, OWLNamedIndividual i);

	PositiveEntailmentTest<O> getPositiveEntailmentTestCase(OWLOntology o, OWLNamedIndividual i);

	NegativeEntailmentTest<O> getNegativeEntailmentTestCase(OWLOntology o, OWLNamedIndividual i);
}
