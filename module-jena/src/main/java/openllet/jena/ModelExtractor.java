// Portions Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// Clark & Parsia, LLC parts of this source code are available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.jena;

import static openllet.jena.JenaUtils.makeGraphNode;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.ReasonerVocabulary;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.boxes.rbox.Role;
import openllet.core.utils.iterator.IteratorUtils;
import openllet.jena.vocabulary.OWL2;

/**
 * Extract a Jena model that contains the information Pellet inferred. Models can be generated about classes, properties or individuals. Note that _individual
 * models do not contain any information about property assertions, it just contains type assertions about individuals.
 *
 * @author Evren Sirin
 */
public class ModelExtractor
{

	/**
	 * Enumeration of types of statements that can be retrieved
	 */
	public enum StatementType
	{
		/**
		 * for individuals, rdf:type statements (includes super-classes)
		 */
		ALL_INSTANCE,

		/**
		 * for classes, rdfs:subClassOf statements (includes all super-classes)
		 */
		ALL_SUBCLASS,

		/**
		 * for properties, rdfs:subPropertyOf statements (includes all super-properties)
		 */
		ALL_SUBPROPERTY,

		/**
		 * for classes, owl:complementOf statements
		 */
		COMPLEMENT_CLASS,

		/**
		 * for individuals, _data property value statements
		 */
		DATA_PROPERTY_VALUE,

		/**
		 * for individuals, owl:differentFrom statements
		 */
		DIFFERENT_FROM,

		/**
		 * for individuals, rdf:type statements (only the most specific classes)
		 */
		DIRECT_INSTANCE,

		/**
		 * for classes, rdfs:subClassOf statements (includes only direct super-classes)
		 */
		DIRECT_SUBCLASS,

		/**
		 * for properties, rdfs:subPropertyOf statements (includes only direct super-properties)
		 */
		DIRECT_SUBPROPERTY,

		/**
		 * for classes, owl:disjointWith statements
		 */
		DISJOINT_CLASS,

		/**
		 * for classes, owl:propertyDisjointWith statements
		 */
		DISJOINT_PROPERTY,

		/**
		 * for classes, owl:equivalentClass statements
		 */
		EQUIVALENT_CLASS,

		/**
		 * for properties, owl:equivalentProperty statements
		 */
		EQUIVALENT_PROPERTY,

		/**
		 * for properties, owl:inverseOf statements
		 */
		INVERSE_PROPERTY,

		/**
		 * for individuals, jena reasoner vocabulary direct rdf:type statements
		 */
		JENA_DIRECT_INSTANCE,

		/**
		 * for classes, jena reasoner vocabulary direct rdfs:subClassOf statements
		 */
		JENA_DIRECT_SUBCLASS,

		/**
		 * for properties, jena reasoner vocabulary direct rdfs:subPropertyOf statements
		 */
		JENA_DIRECT_SUBPROPERTY,

		/**
		 * for individuals, object property value statements
		 */
		OBJECT_PROPERTY_VALUE,

		/**
		 * for individuals, owl:sameAs statements
		 */
		SAME_AS;

		/**
		 * All statements about classes
		 */
		public static final EnumSet<StatementType> ALL_CLASS_STATEMENTS;

		/**
		 * All statements about individuals
		 */
		public static final EnumSet<StatementType> ALL_INDIVIDUAL_STATEMENTS;

		/**
		 * All statements about properties
		 */
		public static final EnumSet<StatementType> ALL_PROPERTY_STATEMENTS;

		/**
		 * All statements (without Jena predicates for direct relations)
		 */
		public static final EnumSet<StatementType> ALL_STATEMENTS;

		/**
		 * All statements (including Jena predicates for direct relations)
		 */
		public static final EnumSet<StatementType> ALL_STATEMENTS_INCLUDING_JENA;

		/**
		 * All property values (both object and _data)
		 */
		public static final EnumSet<StatementType> PROPERTY_VALUE;

		/**
		 * Default statements
		 */
		public static final EnumSet<StatementType> DEFAULT_STATEMENTS;

		static
		{
			ALL_CLASS_STATEMENTS = EnumSet.of(ALL_SUBCLASS, COMPLEMENT_CLASS, DIRECT_SUBCLASS, DISJOINT_CLASS, EQUIVALENT_CLASS);

			ALL_INDIVIDUAL_STATEMENTS = EnumSet.of(ALL_INSTANCE, DATA_PROPERTY_VALUE, DIFFERENT_FROM, DIRECT_INSTANCE, OBJECT_PROPERTY_VALUE, SAME_AS);

			ALL_PROPERTY_STATEMENTS = EnumSet.of(ALL_SUBPROPERTY, DIRECT_SUBPROPERTY, EQUIVALENT_PROPERTY, INVERSE_PROPERTY, DISJOINT_PROPERTY);

			ALL_STATEMENTS = EnumSet.complementOf(EnumSet.of(JENA_DIRECT_INSTANCE, JENA_DIRECT_SUBCLASS, JENA_DIRECT_SUBPROPERTY));

			ALL_STATEMENTS_INCLUDING_JENA = EnumSet.allOf(StatementType.class);

			DEFAULT_STATEMENTS = EnumSet.of(StatementType.DIRECT_SUBCLASS, StatementType.EQUIVALENT_CLASS, StatementType.DIRECT_INSTANCE, StatementType.OBJECT_PROPERTY_VALUE, StatementType.DATA_PROPERTY_VALUE, StatementType.DIRECT_SUBPROPERTY, StatementType.EQUIVALENT_PROPERTY, StatementType.INVERSE_PROPERTY);

			PROPERTY_VALUE = EnumSet.of(StatementType.DATA_PROPERTY_VALUE, StatementType.OBJECT_PROPERTY_VALUE);
		}
	}

	/**
	 * A _filter that does not accept anything.
	 */
	public static final Predicate<Triple> FILTER_NONE = t -> false;

	/**
	 * Associated KB
	 */
	private KnowledgeBase _kb;

	/**
	 * Filter that will be used to drop inferences
	 */
	private Predicate<Triple> _filter = FILTER_NONE;

	/**
	 * Controls the selected statements for methods where no _selector is passed (initial setup intended to be backwards compatible)
	 */
	private EnumSet<StatementType> _selector = StatementType.DEFAULT_STATEMENTS;

	/**
	 * Initialize an empty extractor
	 */
	public ModelExtractor()
	{
	}

	/**
	 * Initialize the extractor with a Jena model that is backed by PelletInfGraph.
	 *
	 * @param model is a Jena model that is backed by PelletInfGraph.
	 * @throws ClassCastException if the model.getGraph() does not return an instance of PelletInfGraph
	 */
	public ModelExtractor(final Model model) throws ClassCastException
	{
		this((PelletInfGraph) model.getGraph());
	}

	/**
	 * Initialize the extractor with a PelletInfGraph
	 *
	 * @param graph is a PelletInfGraph
	 */
	public ModelExtractor(final PelletInfGraph graph)
	{
		this(graph.getPreparedKB());
	}

	/**
	 * Initialize the extractor with a reasoner
	 *
	 * @param kb is a reasoner
	 */
	public ModelExtractor(final KnowledgeBase kb)
	{
		setKB(kb);
	}

	/**
	 * Creates and adds the triple to the given list if the triple passes the _filter.
	 *
	 * @param triples List to be added
	 * @param s subject of the triple
	 * @param p predicate of the triple
	 * @param o object of the triple
	 */
	private void addTriple(final List<Triple> triples, final Node s, final Node p, final Node o)
	{
		final Triple triple = Triple.create(s, p, o);
		if (!_filter.test(triple))
			triples.add(triple);
	}

	public Model extractClassModel()
	{
		return extractClassModel(ModelFactory.createDefaultModel());
	}

	public Model extractClassModel(final Model model)
	{
		final boolean allSubs = _selector.contains(StatementType.ALL_SUBCLASS);
		final boolean jenaDirectSubs = _selector.contains(StatementType.JENA_DIRECT_SUBCLASS);
		final boolean subs = allSubs || jenaDirectSubs || _selector.contains(StatementType.DIRECT_SUBCLASS);
		final boolean equivs = _selector.contains(StatementType.EQUIVALENT_CLASS);
		final boolean disjs = _selector.contains(StatementType.DISJOINT_CLASS);
		final boolean comps = _selector.contains(StatementType.COMPLEMENT_CLASS);

		if (subs || equivs || disjs || comps)
			_kb.classify();

		final List<Triple> triples = new ArrayList<>();

		final Set<ATermAppl> classes = _kb.getAllClasses();

		for (final ATermAppl c : classes)
		{
			triples.clear();

			final Optional<Node> sOpt = makeGraphNode(c);
			if (!sOpt.isPresent())
				continue;
			final Node s = sOpt.get();
			addTriple(triples, s, RDF.type.asNode(), OWL.Class.asNode());

			final Node p = RDFS.subClassOf.asNode();

			if (subs)
			{
				if (allSubs)
				{
					final Set<ATermAppl> eqs = _kb.getAllEquivalentClasses(c);
					for (final ATermAppl eq : eqs)
						makeGraphNode(eq).ifPresent(o -> addTriple(triples, s, p, o));
				}

				final Set<Set<ATermAppl>> supers = allSubs ? _kb.getSuperClasses(c, false) : _kb.getSuperClasses(c, true);

				Iterator<ATermAppl> i = IteratorUtils.flatten(supers.iterator());
				while (i.hasNext())
					makeGraphNode(i.next()).ifPresent(o -> addTriple(triples, s, p, o));

				if (jenaDirectSubs)
				{
					final Node pX = ReasonerVocabulary.directSubClassOf.asNode();
					final Set<Set<ATermAppl>> direct = allSubs ? _kb.getSuperClasses(c, true) : supers;

					i = IteratorUtils.flatten(direct.iterator());
					while (i.hasNext())
						makeGraphNode(i.next()).ifPresent(o -> addTriple(triples, s, pX, o));
				}
			}

			if (equivs)
			{
				final Node pX = OWL.equivalentClass.asNode();

				final Set<ATermAppl> eqs = _kb.getAllEquivalentClasses(c);
				for (final ATermAppl a : eqs)
					makeGraphNode(a).ifPresent(o -> addTriple(triples, s, pX, o));
			}

			if (disjs)
			{
				final Set<Set<ATermAppl>> disj = _kb.getDisjointClasses(c);
				if (!disj.isEmpty())
				{
					final Node pX = OWL.disjointWith.asNode();

					final Iterator<ATermAppl> i = IteratorUtils.flatten(disj.iterator());
					while (i.hasNext())
					{
						final ATermAppl a = i.next();
						if (classes.contains(a))
							makeGraphNode(a).ifPresent(o -> addTriple(triples, s, pX, o));
					}
				}
			}

			if (comps)
			{
				final Set<ATermAppl> comp = _kb.getComplements(c);
				if (!comp.isEmpty())
				{
					final Node pX = OWL.complementOf.asNode();
					for (final ATermAppl a : comp)
						if (classes.contains(a))
							makeGraphNode(a).ifPresent(o -> addTriple(triples, s, pX, o));
				}
			}
			for (final Triple t : triples)
				model.getGraph().add(t);
		}

		return model;
	}

	/**
	 * Extract statements about individuals
	 *
	 * @return a statements container
	 */
	public Model extractIndividualModel()
	{
		return extractIndividualModel(ModelFactory.createDefaultModel());
	}

	/**
	 * Extract statements about individuals
	 *
	 * @param model
	 * @return a statements container
	 */
	public Model extractIndividualModel(final Model model)
	{

		/*
		 * Initialize booleans that reflect the _selector parameter - this avoids
		 * doing set contains evaluations for each pass of the loop.
		 */
		final boolean allClasses = _selector.contains(StatementType.ALL_INSTANCE);
		final boolean jenaDirectClasses = _selector.contains(StatementType.JENA_DIRECT_INSTANCE);
		final boolean classes = allClasses || jenaDirectClasses || _selector.contains(StatementType.DIRECT_INSTANCE);
		final boolean sames = _selector.contains(StatementType.SAME_AS);
		final boolean diffs = _selector.contains(StatementType.DIFFERENT_FROM);
		final boolean objValues = _selector.contains(StatementType.OBJECT_PROPERTY_VALUE);
		final boolean dataValues = _selector.contains(StatementType.DATA_PROPERTY_VALUE);

		if (classes)
			_kb.realize();

		final List<Triple> triples = new ArrayList<>();

		for (final ATermAppl ind : _kb.getIndividuals())
		{

			triples.clear();

			makeGraphNode(ind).ifPresent(s ->
			{
				if (classes)
				{
					final Set<Set<ATermAppl>> types = _kb.getTypes(ind, !allClasses);

					Iterator<ATermAppl> i = IteratorUtils.flatten(types.iterator());
					{
						final Node pX = RDF.type.asNode();
						while (i.hasNext())
							makeGraphNode(i.next()).ifPresent(o -> addTriple(triples, s, pX, o));
					}

					if (jenaDirectClasses)
					{

						final Node pX = ReasonerVocabulary.directRDFType.asNode();

						final Set<Set<ATermAppl>> directTypes = allClasses ? _kb.getTypes(ind, true) : types;

						i = IteratorUtils.flatten(directTypes.iterator());
						while (i.hasNext())
							makeGraphNode(i.next()).ifPresent(o -> addTriple(triples, s, pX, o));
					}
				}

				if (sames)
				{
					final Node pX = OWL.sameAs.asNode();
					addTriple(triples, s, pX, s);
					for (final ATermAppl a : _kb.getSames(ind))
						makeGraphNode(a).ifPresent(node -> addTriple(triples, s, pX, node));
				}

				if (diffs)
				{
					final Node pX = OWL.differentFrom.asNode();
					for (final ATermAppl a : _kb.getDifferents(ind))
						makeGraphNode(a).ifPresent(node -> addTriple(triples, s, pX, node));
				}

				if (dataValues || objValues)
					for (final Role role : _kb.getRBox().getRoles().values())
					{

						if (role.isAnon())
							continue;

						List<ATermAppl> values;
						final ATermAppl name = role.getName();
						if (role.isDatatypeRole())
						{
							if (dataValues)
								values = _kb.getDataPropertyValues(name, ind);
							else
								continue;
						}
						else
							if (role.isObjectRole())
							{
								if (objValues)
									values = _kb.getObjectPropertyValues(name, ind);
								else
									continue;
							}
							else
								continue;

						if (values.isEmpty())
							continue;

						makeGraphNode(name).ifPresent(p ->
						{
							for (final ATermAppl value : values)
								makeGraphNode(value).ifPresent(node -> addTriple(triples, s, p, node));
						});
					}
				for (final Triple t : triples)
					model.getGraph().add(t);
			});
		}

		return model;
	}

	public Model extractModel()
	{
		return extractModel(ModelFactory.createDefaultModel());
	}

	public Model extractModel(final Model model)
	{
		extractClassModel(model);
		extractPropertyModel(model);
		extractIndividualModel(model);

		return model;

	}

	public Model extractPropertyModel()
	{
		return extractPropertyModel(ModelFactory.createDefaultModel());
	}

	public Model extractPropertyModel(final Model model)
	{

		final boolean allSubs = _selector.contains(StatementType.ALL_SUBPROPERTY);
		final boolean jenaDirectSubs = _selector.contains(StatementType.JENA_DIRECT_SUBPROPERTY);
		final boolean subs = allSubs || jenaDirectSubs || _selector.contains(StatementType.DIRECT_SUBPROPERTY);
		final boolean equivs = _selector.contains(StatementType.EQUIVALENT_PROPERTY);
		final boolean invs = _selector.contains(StatementType.INVERSE_PROPERTY);
		final boolean disjs = _selector.contains(StatementType.DISJOINT_PROPERTY);

		_kb.prepare();

		final List<Triple> triples = new ArrayList<>();

		for (final Role role : _kb.getRBox().getRoles().values())
		{
			triples.clear();

			if (role.isAnon())
				continue;

			final ATermAppl name = role.getName();
			final Node s = makeGraphNode(name).orElse(null);
			if (null == s)
				continue;

			{
				final Node pX = RDF.type.asNode();

				if (role.isDatatypeRole())
					addTriple(triples, s, pX, OWL.DatatypeProperty.asNode());
				else
					if (role.isObjectRole())
						addTriple(triples, s, pX, OWL.ObjectProperty.asNode());
					else
						continue;

				if (role.isFunctional())
					addTriple(triples, s, pX, OWL.FunctionalProperty.asNode());
				if (role.isInverseFunctional())
					addTriple(triples, s, pX, OWL.InverseFunctionalProperty.asNode());
				if (role.isTransitive())
					addTriple(triples, s, pX, OWL.TransitiveProperty.asNode());
				if (role.isSymmetric())
					addTriple(triples, s, pX, OWL.SymmetricProperty.asNode());
			}

			if (equivs)
			{
				final Node pX = OWL.equivalentProperty.asNode();
				for (final ATermAppl eq : _kb.getAllEquivalentProperties(name))
					if (JenaUtils._isGrapheNode.test(eq))
						makeGraphNode(eq).ifPresent(node ->
						{
							addTriple(triples, s, pX, node);
							if (allSubs)
								addTriple(triples, s, RDFS.subPropertyOf.asNode(), node);
						});
			}

			if (invs)
			{
				final Set<ATermAppl> inverses = _kb.getInverses(name);
				if (!inverses.isEmpty())
				{
					final Node pX = OWL.inverseOf.asNode();
					for (final ATermAppl inverse : inverses)
						if (JenaUtils._isGrapheNode.test(inverse))
							makeGraphNode(inverse).ifPresent(node -> addTriple(triples, s, pX, node));
				}
			}

			if (disjs)
			{
				final Set<Set<ATermAppl>> disjoints = _kb.getDisjointProperties(name);
				if (!disjoints.isEmpty())
				{
					final Node pX = OWL2.propertyDisjointWith.asNode();

					final Iterator<ATermAppl> i = IteratorUtils.flatten(disjoints.iterator());
					while (i.hasNext())
						makeGraphNode(i.next()).ifPresent(o -> addTriple(triples, s, pX, o));
				}
			}

			if (subs)
			{
				final Node pN = RDFS.subPropertyOf.asNode();

				if (allSubs)
				{
					final Set<ATermAppl> eqs = _kb.getAllEquivalentProperties(name);
					for (final ATermAppl eq : eqs)
						if (JenaUtils._isGrapheNode.test(eq))
							makeGraphNode(eq).ifPresent(o -> addTriple(triples, s, pN, o));
				}

				final Set<Set<ATermAppl>> supers = _kb.getSuperProperties(name, !allSubs);

				if (!supers.isEmpty())
				{
					Iterator<ATermAppl> i = IteratorUtils.flatten(supers.iterator());
					while (i.hasNext())
						makeGraphNode(i.next()).ifPresent(node -> addTriple(triples, s, pN, node));

					if (jenaDirectSubs)
					{
						final Node pX = ReasonerVocabulary.directSubPropertyOf.asNode();

						final Set<Set<ATermAppl>> direct = allSubs ? _kb.getSuperProperties(name, true) : supers;
						i = IteratorUtils.flatten(direct.iterator());
						while (i.hasNext())
							makeGraphNode(i.next()).ifPresent(node -> addTriple(triples, s, pX, node));
					}
				}
			}

			// FIXME: Add domain statements

			// FIXME: Add range statements

			for (final Triple t : triples)
				model.getGraph().add(t);
		}

		return model;
	}

	/**
	 * @return the selector
	 */
	public EnumSet<StatementType> getSelector()
	{
		return _selector;
	}

	/**
	 * @param selector to set
	 */
	public void setSelector(final EnumSet<StatementType> selector)
	{
		_selector = selector;
	}

	/**
	 * @return Returns the reasoner.
	 */
	public KnowledgeBase getKB()
	{
		return _kb;
	}

	/**
	 * @param kb as reasoner to set.
	 */
	public void setKB(final KnowledgeBase kb)
	{
		_kb = kb;
	}

	/**
	 * @return the filter used to filter out any unwanted inferences from the result.
	 */
	public Predicate<Triple> getFilter()
	{
		return _filter;
	}

	/**
	 * Sets the filter that will filter out any unwanted inferences from the result. The filter should process {@link Triple} objects and return
	 * <code>true</code> for any triple that should not be included in the result. Use {@link #FILTER_NONE} to disable filtering.
	 *
	 * @param filter see bellow
	 */
	public void setFilter(final Predicate<Triple> filter)
	{
		if (filter == null)
			throw new NullPointerException("Filter cannot be null");

		_filter = filter;
	}
}
