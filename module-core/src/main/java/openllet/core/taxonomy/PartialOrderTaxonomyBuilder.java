// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.taxonomy;

import java.util.Map;
import java.util.Set;
import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.PartialOrderBuilder;
import openllet.core.utils.PartialOrderComparator;
import openllet.core.utils.progress.ProgressMonitor;

/**
 * Build partially order taxonomy (property or class). Used only by CLI.
 */
public class PartialOrderTaxonomyBuilder implements TaxonomyBuilder
{

	private final PartialOrderBuilder<ATermAppl> _builder;
	private final KnowledgeBase _kb;
	private final TaxonomyImpl<ATermAppl> _tax;

	public PartialOrderTaxonomyBuilder(final KnowledgeBase kb)
	{
		this(kb, new SubsumptionComparator(kb));
	}

	public PartialOrderTaxonomyBuilder(final KnowledgeBase kb, final PartialOrderComparator<ATermAppl> comparator)
	{
		_kb = kb;
		_tax = new TaxonomyImpl<>(null, ATermUtils.TOP, ATermUtils.BOTTOM);
		_builder = new PartialOrderBuilder<>(_tax, comparator);
	}

	@Override
	public boolean classify()
	{
		_builder.addAll(_kb.getClasses());

		return true;
	}

	@Override
	public void classify(final ATermAppl c)
	{
		_builder.add(c);
	}

	@Override
	public boolean realize()
	{
		throw new UnsupportedOperationException();
		/*
		CDOptimizedTaxonomyBuilder b = new CDOptimizedTaxonomyBuilder();
		b.setKB( _kb );
		b.classify();
		return b.realize();
		 */
	}

	@Override
	public void realize(final ATermAppl x)
	{
		throw new UnsupportedOperationException();
	}

	//	@Override
	//	public void setKB(final KnowledgeBase kb)
	//	{
	//		_kb = kb;
	//	}

	public PartialOrderComparator<ATermAppl> getComparator()
	{
		return _builder.getComparator();
	}

	@Override
	public void setProgressMonitor(final ProgressMonitor monitor)
	{
		//
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<ATermAppl, Set<ATermAppl>> getToldDisjoints()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TaxonomyImpl<ATermAppl> getToldTaxonomy()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public TaxonomyImpl<ATermAppl> getTaxonomy()
	{
		return _tax;
	}
}
