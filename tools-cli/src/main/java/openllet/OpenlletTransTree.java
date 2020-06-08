// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public
// License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet;

import static openllet.OpenlletCmdOptionArg.NONE;
import static openllet.OpenlletCmdOptionArg.REQUIRED;

import java.util.HashSet;
import java.util.Set;
import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.taxonomy.PartialOrderTaxonomyBuilder;
import openllet.core.taxonomy.SubsumptionComparator;
import openllet.core.taxonomy.Taxonomy;
import openllet.core.taxonomy.TaxonomyBuilder;
import openllet.core.taxonomy.printer.ClassTreePrinter;
import openllet.core.utils.ATermUtils;
import openllet.owlapi.OWLAPILoader;
import openllet.owlapi.OntologyUtils;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.search.EntitySearcher;

/**
 * <p>
 * Description: Compute the hierarchy for part-of classes (or individuals) given a (transitive) property.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Markus Stocker
 */
public class OpenlletTransTree extends OpenlletCmdApp
{

	private String	_propertyName;
	private boolean	_showClasses;
	private boolean	_showIndividuals;

	public OpenlletTransTree()
	{
		super();
	}

	@Override
	public String getAppId()
	{
		return "OpenlletTransTree: Compute a transitive-tree closure";
	}

	@Override
	public String getAppCmd()
	{
		return "openllet trans-tree " + getMandatoryOptions() + "[options] <file URI>...";
	}

	@Override
	public OpenlletCmdOptions getOptions()
	{
		_showClasses = true;
		_showIndividuals = false;

		final OpenlletCmdOptions options = getGlobalOptions();

		OpenlletCmdOption option = new OpenlletCmdOption("property");
		option.setShortOption("p");
		option.setType("<URI>");
		option.setDescription("The part-of (transitive) property");
		option.setIsMandatory(true);
		option.setArg(REQUIRED);
		options.add(option);

		option = new OpenlletCmdOption("classes");
		option.setShortOption("c");
		option.setDescription("Show parts hierarchy for classes");
		option.setDefaultValue(_showClasses);
		option.setIsMandatory(false);
		option.setArg(NONE);
		options.add(option);

		option = new OpenlletCmdOption("individuals");
		option.setShortOption("i");
		option.setDescription("Show parts hierarchy for individuals");
		option.setDefaultValue(_showIndividuals);
		option.setIsMandatory(false);
		option.setArg(NONE);
		options.add(option);

		option = new OpenlletCmdOption("filter");
		option.setShortOption("f");
		option.setType("<URI>");
		option.setDescription("The class to filter");
		option.setIsMandatory(false);
		option.setArg(REQUIRED);
		options.add(option);

		return options;
	}

	@Override
	public void run()
	{
		_propertyName = _options.getOption("property").getValueAsString();

		final OWLAPILoader loader = new OWLAPILoader();
		final KnowledgeBase kb = loader.createKB(getInputFiles());

		final OWLEntity entity = OntologyUtils.findEntity(_propertyName, loader.allOntologies());

		if (entity == null) throw new OpenlletCmdException("Property not found: " + _propertyName);

		if (!(entity instanceof OWLObjectProperty)) throw new OpenlletCmdException("Not an object property: " + _propertyName);

		if (!EntitySearcher.isTransitive((OWLObjectProperty) entity, loader.allOntologies())) throw new OpenlletCmdException("Not a transitive property: " + _propertyName);

		final ATermAppl p = ATermUtils.makeTermAppl(entity.getIRI().toString());

		ATermAppl c = null;
		boolean filter = false;

		if (_options.getOption("filter").exists())
		{
			final String filterName = _options.getOption("filter").getValueAsString();
			final OWLEntity filterClass = OntologyUtils.findEntity(filterName, loader.allOntologies());
			if (filterClass == null) throw new OpenlletCmdException("Filter class not found: " + filterName);
			if (!(filterClass instanceof OWLClass)) throw new OpenlletCmdException("Not a class: " + filterName);

			c = ATermUtils.makeTermAppl(filterClass.getIRI().toString());

			filter = true;
		}

		// Test first the individuals parameter, as per default the --classes option is true
		final boolean indParam = _options.getOption("individuals").getValueAsBoolean();

		final TaxonomyBuilder builder = new PartialOrderTaxonomyBuilder(kb, indParam ? new PartIndividualsComparator(kb, p) : new PartClassesComparator(kb, p));

		if (indParam)
		{// Parts for individuals

			Set<ATermAppl> individuals;
			if (filter)
				individuals = kb.getInstances(c);
			else
				individuals = kb.getIndividuals(); // Note: this is not an optimal solution

			for (final ATermAppl individual : individuals)
				if (!ATermUtils.isBnode(individual)) builder.classify(individual);
		}
		else if (filter)
			for (final ATermAppl cl : getDistinctSubclasses(kb, c))
				builder.classify(cl);
		else
			builder.classify();

		final Taxonomy<ATermAppl> taxonomy = builder.getTaxonomy();

		final ClassTreePrinter printer = new ClassTreePrinter();
		printer.print(taxonomy);

		_publicTaxonomy = taxonomy;
	}

	/** Unit testing access only */
	public Taxonomy<ATermAppl> _publicTaxonomy;

	private Set<ATermAppl> getDistinctSubclasses(final KnowledgeBase kb, final ATermAppl c)
	{
		final Set<ATermAppl> filteredClasses = new HashSet<>();
		final Set<Set<ATermAppl>> subclasses = kb.getSubClasses(c);
		for (final Set<ATermAppl> s : subclasses)
			filteredClasses.addAll(s);
		filteredClasses.add(c);

		//Remove not(TOP), since taxonomy builder complains otherwise...
		filteredClasses.remove(ATermUtils.negate(ATermUtils.TOP));

		return filteredClasses;
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
}
