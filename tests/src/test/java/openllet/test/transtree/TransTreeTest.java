package openllet.test.transtree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.search.EntitySearcher;

import openllet.OpenlletTransTree;
import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.taxonomy.PartialOrderTaxonomyBuilder;
import openllet.core.taxonomy.SubsumptionComparator;
import openllet.core.taxonomy.Taxonomy;
import openllet.core.taxonomy.printer.ClassTreePrinter;
import openllet.core.utils.ATermUtils;
import openllet.owlapi.OWLAPILoader;
import openllet.owlapi.OntologyUtils;

public class TransTreeTest
{

	@Test
	public void testDiscoveryOntology()
	{
		testProperty("test/data/trans-tree-tests/discovery.owl", //
				"http://purl.org/vocab/relationship/ancestorOf");
	}

	private static void testProperty(final String ontologyURI, final String propertyURI)
	{
		final OWLAPILoader loader = new OWLAPILoader();
		final KnowledgeBase kb = loader.createKB(ontologyURI);

		final OWLEntity entity = OntologyUtils.findEntity(propertyURI, loader.allOntologies());

		if (entity == null)
			throw new IllegalArgumentException("Property not found: " + propertyURI);

		if (!(entity instanceof OWLObjectProperty))
			throw new IllegalArgumentException("Not an object property: " + propertyURI);

		if (!EntitySearcher.isTransitive((OWLObjectProperty) entity, loader.allOntologies()))
			throw new IllegalArgumentException("Not a transitive property: " + propertyURI);

		final ATermAppl p = ATermUtils.makeTermAppl(entity.getIRI().toString());

		testDeprecatedTaxonomy(kb, p);
	}

	private static void testDeprecatedTaxonomy(final KnowledgeBase kb, final ATermAppl p)
	{
		final PartialOrderTaxonomyBuilder builder = new PartialOrderTaxonomyBuilder(kb, new PartClassesComparator(kb, p));
		builder.classify();

		final Taxonomy<ATermAppl> taxonomy = builder.getTaxonomy();
		final ClassTreePrinter printer = new ClassTreePrinter();
		printer.print(taxonomy);
	}

	private static class PartClassesComparator extends SubsumptionComparator
	{

		private final ATermAppl _p;

		public PartClassesComparator(final KnowledgeBase kb, final ATermAppl p)
		{
			super(kb);
			_p = p;
		}

		@Override
		protected boolean isSubsumedBy(final ATermAppl a, final ATermAppl b)
		{
			final ATermAppl someB = ATermUtils.makeSomeValues(_p, b);

			return _kb.isSubClassOf(a, someB);
		}
	}

	@SuppressWarnings("unused")
	private static class PartIndividualsComparator extends SubsumptionComparator
	{

		private final ATermAppl _p;

		public PartIndividualsComparator(final KnowledgeBase kb, final ATermAppl p)
		{
			super(kb);
			_p = p;
		}

		@Override
		protected boolean isSubsumedBy(final ATermAppl a, final ATermAppl b)
		{
			return _kb.hasPropertyValue(a, _p, b);
		}
	}

	@Test
	public void filter1()
	{
		final OpenlletTransTree cli = new OpenlletTransTree();

		cli.parseArgs(new String[] { "trans-tree", "-p", "http://clarkparsia.com/pellet/tutorial/pops#subProjectOf", "-f", "http://clarkparsia.com/pellet/tutorial/pops#Employee", "test/data/trans-tree-tests/ontology-010.ttl" });
		cli.run();

		final Taxonomy<ATermAppl> taxonomy = cli._publicTaxonomy;

		assertEquals(5, taxonomy.getClasses().size()); //TOP, not(TOP), Employee, CivilServant, Contractor

		final Set<Set<ATermAppl>> subclasses = taxonomy.getSubs(ATermUtils.TOP);

		assertEquals(4, subclasses.size()); //not(TOP), Employee, CivilServant, Contractor

		final Iterator<Set<ATermAppl>> iterator = subclasses.iterator();

		final Set<ATermAppl> elements = new HashSet<>(4);

		while (iterator.hasNext())
		{
			final Set<ATermAppl> subclass = iterator.next();
			assertEquals(1, subclass.size());
			elements.add(subclass.iterator().next());
		}

		assertTrue(elements.contains(ATermUtils.makeNot(ATermUtils.TOP)));
		assertTrue(elements.contains(ATermUtils.makeTermAppl("http://clarkparsia.com/pellet/tutorial/pops#Employee")));
		assertTrue(elements.contains(ATermUtils.makeTermAppl("http://clarkparsia.com/pellet/tutorial/pops#CivilServant")));
		assertTrue(elements.contains(ATermUtils.makeTermAppl("http://clarkparsia.com/pellet/tutorial/pops#Contractor")));

	}

	@Test
	public void filter2()
	{
		final OpenlletTransTree cli = new OpenlletTransTree();

		cli.parseArgs(new String[] { "trans-tree", "-p", "http://clarkparsia.com/pellet/tutorial/pops#subProjectOf", "-f", "http://clarkparsia.com/pellet/tutorial/pops#Employee", "--individuals", "test/data/trans-tree-tests/ontology-010.ttl" });
		cli.run();

		final Taxonomy<ATermAppl> taxonomy = cli._publicTaxonomy;

		final Set<ATermAppl> classes = taxonomy.getClasses();
		assertEquals(3, classes.size()); //TOP, not(TOP), 1 Employee
		assertTrue(classes.contains(ATermUtils.makeTermAppl("http://clarkparsia.com/pellet/tutorial/pops#Employee1")));
	}

	@Test
	public void filter3()
	{
		final OpenlletTransTree cli = new OpenlletTransTree();

		cli.parseArgs(new String[] { "trans-tree", "-p", "http://clarkparsia.com/pellet/tutorial/pops#subProjectOf", "-f", "http://clarkparsia.com/pellet/tutorial/pops#Contractor", "--individuals", "test/data/trans-tree-tests/ontology-010.ttl" });
		cli.run();

		final Taxonomy<ATermAppl> taxonomy = cli._publicTaxonomy;
		assertEquals(2, taxonomy.getClasses().size()); //TOP, not(TOP) (no Contractors)
	}
}
