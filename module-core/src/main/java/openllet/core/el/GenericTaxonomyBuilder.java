// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.el;

import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;
import openllet.aterm.ATermAppl;
import openllet.core.taxonomy.Taxonomy;
import openllet.core.taxonomy.TaxonomyImpl;
import openllet.core.taxonomy.TaxonomyNode;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.MultiValueMap;
import openllet.core.utils.SetUtils;

/**
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Harris Lin
 */
public class GenericTaxonomyBuilder
{
	private Taxonomy<ATermAppl> _taxonomyImpl;

	private MultiValueMap<ATermAppl, ATermAppl> _subsumers;

	public Taxonomy<ATermAppl> build(final MultiValueMap<ATermAppl, ATermAppl> subsumers)
	{
		_subsumers = subsumers;
		_taxonomyImpl = new TaxonomyImpl<>(null, ATermUtils.TOP, ATermUtils.BOTTOM);

		for (final ATermAppl subsumer : subsumers.get(ATermUtils.TOP))
			if (ATermUtils.isPrimitive(subsumer))
				_taxonomyImpl.addEquivalentNode(subsumer, _taxonomyImpl.getTop());

		for (final Entry<ATermAppl, Set<ATermAppl>> entry : subsumers.entrySet())
		{
			final ATermAppl c = entry.getKey();
			if (ATermUtils.isPrimitive(c))
				if (entry.getValue().contains(ATermUtils.BOTTOM))
					_taxonomyImpl.addEquivalentNode(c, _taxonomyImpl.getBottomNode());
				else
					add(c);
		}

		return _taxonomyImpl;
	}

	private TaxonomyNode<ATermAppl> add(final ATermAppl c)
	{
		TaxonomyNode<ATermAppl> node = _taxonomyImpl.getNode(c);

		if (node == null)
		{
			final Set<ATermAppl> equivalents = SetUtils.create();
			final Set<TaxonomyNode<ATermAppl>> subsumerNodes = SetUtils.create();

			for (final ATermAppl subsumer : _subsumers.get(c))
			{
				if (c.equals(subsumer) || !ATermUtils.isPrimitive(subsumer))
					continue;

				if (_subsumers.get(subsumer).contains(c))
					equivalents.add(subsumer);
				else
				{
					final TaxonomyNode<ATermAppl> supNode = add(subsumer);
					subsumerNodes.add(supNode);
				}
			}

			node = add(c, subsumerNodes);

			for (final ATermAppl eq : equivalents)
				_taxonomyImpl.addEquivalentNode(eq, node);
		}

		return node;
	}

	private TaxonomyNode<ATermAppl> add(final ATermAppl c, final Set<TaxonomyNode<ATermAppl>> subsumers)
	{
		final Set<TaxonomyNode<ATermAppl>> parents = SetUtils.create(subsumers);
		final Set<ATermAppl> supers = SetUtils.create();
		final Set<ATermAppl> subs = Collections.singleton(ATermUtils.BOTTOM);

		for (final TaxonomyNode<ATermAppl> subsumer : subsumers)
			parents.removeAll(subsumer.getSupers());

		for (final TaxonomyNode<ATermAppl> parent : parents)
		{
			supers.add(parent.getName());
			parent.removeSub(_taxonomyImpl.getBottomNode());
		}

		return _taxonomyImpl.addNode(Collections.singleton(c), supers, subs, false);
	}
}
