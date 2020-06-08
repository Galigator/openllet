package openllet.test.annotations;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;
import junit.framework.JUnit4TestAdapter;
import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.KnowledgeBaseImpl;
import openllet.core.OpenlletOptions;
import openllet.core.utils.ATermUtils;
import openllet.jena.PelletInfGraph;
import openllet.owlapi.OpenlletReasoner;
import openllet.owlapi.OpenlletReasonerFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class TestReasoningWithAnnotationAxioms
{

	private KnowledgeBase				kb										= new KnowledgeBaseImpl();

	private final ATermAppl				i										= ATermUtils.makeTermAppl("i");
	private final ATermAppl				p1										= ATermUtils.makeTermAppl("p1");
	private final ATermAppl				o1										= ATermUtils.makePlainLiteral("o1");
	private final ATermAppl				o2										= ATermUtils.makePlainLiteral("o2");
	private final ATermAppl				p2										= ATermUtils.makeTermAppl("p2");
	private final ATermAppl				p3										= ATermUtils.makeTermAppl("p3");

	private final OWLOntologyManager	manager									= OWLManager.createOWLOntologyManager();
	private final OWLNamedIndividual	oi										= manager.getOWLDataFactory().getOWLNamedIndividual(IRI.create("i"));
	private final OWLAnnotationProperty	op1										= manager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create("p1"));
	private final OWLAnnotationProperty	op2										= manager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create("p2"));
	private final OWLAnnotationProperty	op3										= manager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create("p3"));
	private final OWLAnnotationValue	oo1										= manager.getOWLDataFactory().getOWLLiteral("o1");

	private final OntModel				model									= ModelFactory.createOntologyModel(openllet.jena.PelletReasonerFactory.THE_SPEC);
	private final Resource				ji										= ResourceFactory.createResource("http://example.org#i");
	private final Property				jp1										= ResourceFactory.createProperty("http://example.org#p1");
	private final Property				jp2										= ResourceFactory.createProperty("http://example.org#p2");
	private final Literal				jo1										= ResourceFactory.createPlainLiteral("o1");

	private final boolean				USE_ANNOTATION_SUPPORT_DEFAULT_VALUE	= OpenlletOptions.USE_ANNOTATION_SUPPORT;

	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter(TestReasoningWithAnnotationAxioms.class);
	}

	@Before
	public void setUp()
	{
		OpenlletOptions.USE_ANNOTATION_SUPPORT = true;
	}

	@After
	public void cleanUp()
	{
		OpenlletOptions.USE_ANNOTATION_SUPPORT = USE_ANNOTATION_SUPPORT_DEFAULT_VALUE;
	}

	@Test
	/**
	 * Tests if the value of a given annotation property is propagated to its superproperty
	 */
	public void testSubAnnotationPropertyOfAxiom1()
	{
		kb.addIndividual(i);
		kb.addAnnotationProperty(p1);
		kb.addAnnotationProperty(p2);
		kb.addSubProperty(p1, p2);
		kb.addAnnotation(i, p1, o1);

		assertEquals(kb.getPropertyValues(p1), kb.getPropertyValues(p2));
	}

	@Test
	/**
	 * Tests if the value of a given annotation property is propagated to its superproperties
	 */
	public void testSubAnnotationPropertyOfAxiom2()
	{
		kb.addIndividual(i);
		kb.addAnnotationProperty(p1);
		kb.addAnnotationProperty(p2);
		kb.addAnnotationProperty(p3);
		kb.addSubProperty(p1, p2);
		kb.addSubProperty(p2, p3);
		kb.addAnnotation(i, p1, o1);

		assertEquals(kb.getPropertyValues(p1), kb.getPropertyValues(p3));
	}

	@Test
	/**
	 * Tests if the value of a given annotation property is propagated to its superproperties
	 */
	public void testSubAnnotationPropertyOfAxiom3()
	{

		kb.addIndividual(i);
		kb.addAnnotationProperty(p1);
		kb.addAnnotationProperty(p2);
		kb.addAnnotationProperty(p3);
		kb.addSubProperty(p1, p2);
		kb.addSubProperty(p2, p3);
		kb.addAnnotation(i, p1, o1);
		kb.addAnnotation(i, p2, o2);

		assertEquals(kb.getPropertyValues(p2), kb.getPropertyValues(p3));
	}

	@Test
	public void testOWLAPILoader1() throws OWLOntologyCreationException, OWLOntologyChangeException
	{
		final Set<OWLAxiom> axioms = new HashSet<>();
		axioms.add(manager.getOWLDataFactory().getOWLSubAnnotationPropertyOfAxiom(op1, op2));
		axioms.add(manager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(op1, oi.getIRI(), oo1));

		final OWLOntology ontology = manager.createOntology(axioms);
		final OpenlletReasoner reasoner = OpenlletReasonerFactory.getInstance().createReasoner(ontology);

		assertEquals(reasoner.getAnnotationPropertyValues(oi, op1), reasoner.getAnnotationPropertyValues(oi, op2));
	}

	@Test
	public void testOWLAPILoader2() throws OWLOntologyCreationException, OWLOntologyChangeException
	{

		final Set<OWLAxiom> axioms = new HashSet<>();
		axioms.add(manager.getOWLDataFactory().getOWLSubAnnotationPropertyOfAxiom(op1, op2));
		axioms.add(manager.getOWLDataFactory().getOWLSubAnnotationPropertyOfAxiom(op2, op3));
		axioms.add(manager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(op1, oi.getIRI(), oo1));

		final OWLOntology ontology = manager.createOntology(axioms);
		final OpenlletReasoner reasoner = OpenlletReasonerFactory.getInstance().createReasoner(ontology);

		assertEquals(reasoner.getAnnotationPropertyValues(oi, op1), reasoner.getAnnotationPropertyValues(oi, op3));
	}

	@Test
	public void testJenaLoader1()
	{

		model.add(jp1, RDF.type, OWL.AnnotationProperty);
		model.add(ji, jp1, jo1);
		model.add(jp1, RDFS.subPropertyOf, RDFS.label);
		model.prepare();

		kb = ((PelletInfGraph) model.getGraph()).getKB();

		final ATermAppl st = ATermUtils.makeTermAppl("http://example.org#i");
		final ATermAppl pt = ATermUtils.makeTermAppl(RDFS.label.getURI());
		final ATermAppl ot = ATermUtils.makeStringLiteral("o1");

		final Set<ATermAppl> actual = kb.getAnnotations(st, pt);

		final Set<ATermAppl> expected = new HashSet<>();
		expected.add(ot);

		assertEquals(expected, actual);
	}

	@Test
	public void testJenaLoader2()
	{

		model.add(jp1, RDF.type, OWL.AnnotationProperty);
		model.add(ji, jp1, jo1);
		model.add(jp2, RDF.type, OWL.AnnotationProperty);
		model.add(jp1, RDFS.subPropertyOf, jp2);
		model.prepare();

		final ATermAppl st = ATermUtils.makeTermAppl("http://example.org#i");
		final ATermAppl pt = ATermUtils.makeTermAppl(jp2.getURI());
		final ATermAppl ot = ATermUtils.makeStringLiteral("o1");

		kb = ((PelletInfGraph) model.getGraph()).getKB();
		final Set<ATermAppl> actual = kb.getAnnotations(st, pt);

		final Set<ATermAppl> expected = new HashSet<>();
		expected.add(ot);

		assertEquals(expected, actual);
	}
}
