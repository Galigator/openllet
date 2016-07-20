// Portions Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// Clark & Parsia, LLC parts of this source code are available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com
//
// ---
// Portions Copyright (c) 2003 Ron Alford, Mike Grove, Bijan Parsia, Evren Sirin
// Alford, Grove, Parsia, Sirin parts of this source code are available under the terms of the MIT License.
//
// The MIT License
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to
// deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.

package openllet.core.boxes.rbox;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.aterm.ATermList;
import openllet.core.DependencySet;
import openllet.core.FSMBuilder;
import openllet.core.OpenlletOptions;
import openllet.core.PropertyType;
import openllet.core.RoleTaxonomyBuilder;
import openllet.core.taxonomy.Taxonomy;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.iterator.FilterIterator;
import openllet.core.utils.iterator.IteratorUtils;
import openllet.core.utils.iterator.MapIterator;
import openllet.shared.tools.Log;

/**
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Evren Sirin
 */
public class RBoxImpl implements RBox
{
	public static Logger _logger = Log.getLogger(RBoxImpl.class);

	@Override
	public Logger getLogger()
	{
		return _logger;
	}

	private final Map<ATermAppl, Role> _roles = new HashMap<>();

	private final Set<Role> _reflexiveRoles = new HashSet<>();
	private final Map<Role, Map<ATermAppl, Set<Set<ATermAppl>>>> _domainAssertions;
	private final Map<Role, Map<ATermAppl, Set<Set<ATermAppl>>>> _rangeAssertions;
	private final FSMBuilder _fsmBuilder;

	private Taxonomy<ATermAppl> _objectTaxonomy;
	private Taxonomy<ATermAppl> _dataTaxonomy;
	private Taxonomy<ATermAppl> _annotationTaxonomy;

	@Override
	public Taxonomy<ATermAppl> getObjectTaxonomy()
	{
		if (_objectTaxonomy == null)
		{
			final RoleTaxonomyBuilder builder = new RoleTaxonomyBuilder(this, PropertyType.OBJECT);
			_objectTaxonomy = builder.classify();
		}
		return _objectTaxonomy;
	}

	@Override
	public void setObjectTaxonomy(final Taxonomy<ATermAppl> objectTaxonomy)
	{
		_objectTaxonomy = objectTaxonomy;
	}

	@Override
	public Taxonomy<ATermAppl> getDataTaxonomy()
	{
		if (_dataTaxonomy == null)
		{
			final RoleTaxonomyBuilder builder = new RoleTaxonomyBuilder(this, PropertyType.DATATYPE);
			_dataTaxonomy = builder.classify();
		}
		return _dataTaxonomy;
	}

	@Override
	public void setDataTaxonomy(final Taxonomy<ATermAppl> dataTaxonomy)
	{
		_dataTaxonomy = dataTaxonomy;
	}

	@Override
	public Taxonomy<ATermAppl> getAnnotationTaxonomy()
	{
		if (_annotationTaxonomy == null)
		{
			final RoleTaxonomyBuilder builder = new RoleTaxonomyBuilder(this, PropertyType.ANNOTATION);
			if (OpenlletOptions.USE_ANNOTATION_SUPPORT)
				_annotationTaxonomy = builder.classify();
		}
		return _annotationTaxonomy;
	}

	@Override
	public void setAnnotationTaxonomy(final Taxonomy<ATermAppl> annotationTaxonomy)
	{
		_annotationTaxonomy = annotationTaxonomy;
	}

	@Override
	public Map<ATermAppl, Role> getRoles()
	{
		return _roles;
	}

	@Override
	public boolean isObjectTaxonomyPrepared()
	{
		return _objectTaxonomy != null;
	}

	@Override
	public boolean isDataTaxonomyPrepared()
	{
		return _dataTaxonomy != null;
	}

	@Override
	public boolean isAnnotationTaxonomyPrepared()
	{
		return _annotationTaxonomy != null;
	}

	@Override
	public Set<Role> getReflexiveRoles()
	{
		return _reflexiveRoles;
	}

	@Override
	public Map<Role, Map<ATermAppl, Set<Set<ATermAppl>>>> getDomainAssertions()
	{
		return _domainAssertions;
	}

	@Override
	public Map<Role, Map<ATermAppl, Set<Set<ATermAppl>>>> getRangeAssertions()
	{
		return _rangeAssertions;
	}

	@Override
	public FSMBuilder getFsmBuilder()
	{
		return _fsmBuilder;
	}

	private static class ValueIterator extends MapIterator<Map.Entry<ATermAppl, Set<Set<ATermAppl>>>, ATermAppl>
	{
		public ValueIterator(final Iterator<Entry<ATermAppl, Set<Set<ATermAppl>>>> iterator)
		{
			super(iterator);
		}

		@Override
		public ATermAppl map(final Entry<ATermAppl, Set<Set<ATermAppl>>> e)
		{
			return e.getKey();
		}

	}

	@Override
	public Iterator<ATermAppl> getAssertedDomains(final Role r)
	{
		final Map<ATermAppl, Set<Set<ATermAppl>>> domains = getDomainAssertions().get(r);
		return domains == null ? IteratorUtils.<ATermAppl> emptyIterator() : new ValueIterator(new DomainRangeIterator(domains, r, true));
	}

	private static class DomainRangeIterator extends FilterIterator<Map.Entry<ATermAppl, Set<Set<ATermAppl>>>>
	{
		final ATermAppl _p;
		final boolean _isDomain;

		public DomainRangeIterator(final Map<ATermAppl, Set<Set<ATermAppl>>> map, final Role role, final boolean isDomain)
		{
			super(map.entrySet().iterator());
			_p = role.getName();
			_isDomain = isDomain;
		}

		@Override
		public boolean filter(final Map.Entry<ATermAppl, Set<Set<ATermAppl>>> entry)
		{
			final Set<Set<ATermAppl>> allExplanations = entry.getValue();

			final Set<ATermAppl> explanation = Collections.singleton(_isDomain ? ATermUtils.makeDomain(_p, entry.getKey()) : ATermUtils.makeRange(_p, entry.getKey()));
			return !allExplanations.contains(explanation);
		}
	}

	@Override
	public Iterator<ATermAppl> getAssertedRanges(final Role r)
	{
		final Map<ATermAppl, Set<Set<ATermAppl>>> ranges = getRangeAssertions().get(r);
		return ranges == null ? IteratorUtils.<ATermAppl> emptyIterator() : new ValueIterator(new DomainRangeIterator(ranges, r, false));
	}

	public RBoxImpl()
	{
		_domainAssertions = new HashMap<>();
		_rangeAssertions = new HashMap<>();

		_fsmBuilder = new FSMBuilder(this);

		addDatatypeRole(ATermUtils.TOP_DATA_PROPERTY);
		addDatatypeRole(ATermUtils.BOTTOM_DATA_PROPERTY);
		final Role topObjProp = addObjectRole(ATermUtils.TOP_OBJECT_PROPERTY);
		final Role bottomObjProp = addObjectRole(ATermUtils.BOTTOM_OBJECT_PROPERTY);

		topObjProp.setTransitive(true, DependencySet.INDEPENDENT);
		topObjProp.setReflexive(true, DependencySet.INDEPENDENT);

		bottomObjProp.setIrreflexive(true, DependencySet.INDEPENDENT);
		bottomObjProp.setAsymmetric(true, DependencySet.INDEPENDENT);

		addEquivalentRole(topObjProp.getName(), topObjProp.getInverse().getName(), DependencySet.INDEPENDENT);
		addEquivalentRole(bottomObjProp.getName(), bottomObjProp.getInverse().getName(), DependencySet.INDEPENDENT);

	}

	@Override
	public void propogateDomain(final Role role, final Map<ATermAppl, Set<Set<ATermAppl>>> domains)
	{
		if (domains == null || domains.isEmpty())
			return;
		for (final Map.Entry<ATermAppl, Set<Set<ATermAppl>>> e : domains.entrySet())
		{
			final Set<ATermAppl> explanation = e.getValue().iterator().next();
			final ATermAppl domain = e.getKey();
			final ATermAppl normalized = ATermUtils.normalize(domain);

			for (final Role s : role.getSubRoles())
			{
				final DependencySet explainSub = role.getExplainSub(s.getName());
				final DependencySet ds = explainSub.union(explanation, true);

				s.addDomain(normalized, ds);
			}
		}
	}

	@Override
	public void propogateRange(final Role role, final Map<ATermAppl, Set<Set<ATermAppl>>> ranges)
	{
		if (ranges == null || ranges.isEmpty())
			return;
		for (final Map.Entry<ATermAppl, Set<Set<ATermAppl>>> e : ranges.entrySet())
		{
			final Set<ATermAppl> explanation = e.getValue().iterator().next();
			final ATermAppl range = e.getKey();
			final ATermAppl normalized = ATermUtils.normalize(range);

			for (final Role s : role.getSubRoles())
			{
				final DependencySet explainSub = role.getExplainSub(s.getName());
				final DependencySet ds = explainSub.union(explanation, true);

				s.addRange(normalized, ds);
			}
		}
	}

	@Override
	public void computeImmediateSubRoles(final Role r, final Map<ATerm, DependencySet> subs)
	{

		final Role invR = r.getInverse();
		if (invR != null && invR != r)
		{

			for (final Role invSubR : invR.getSubRoles())
			{
				final Role subR = invSubR.getInverse();
				if (subR == null)
				{
					if (_logger.isLoggable(Level.FINE))
						_logger.fine("Property " + invSubR + " was supposed to be an ObjectProperty but it is not!");
				}
				else
					if (subR != r)
					{
						// System.out.println("expsub:
						// "+invR.getExplainSub(invSubR.getName()));
						// System.out.println("expinv:
						// "+invSubR.getExplainInverse());
						final DependencySet subDS = invR.getExplainSub(invSubR.getName());
						subs.put(subR.getName(), subDS);
					}
			}
			for (final ATermList roleChain : invR.getSubRoleChains())
			{
				final DependencySet subDS = invR.getExplainSub(roleChain);

				final ATermList subChain = inverse(roleChain);
				subs.put(subChain, subDS);
			}
		}

		for (final Role sub : r.getSubRoles())
		{
			final DependencySet subDS = r.getExplainSub(sub.getName());

			subs.put(sub.getName(), subDS);
		}

		for (final ATermList subChain : r.getSubRoleChains())
		{
			final DependencySet subDS = r.getExplainSub(subChain);

			subs.put(subChain, subDS);
		}

	}

	@Override
	public void computeSubRoles(final Role r, final Set<Role> subRoles, final Set<ATermList> subRoleChains, final Map<ATerm, DependencySet> dependencies, final DependencySet ds)
	{
		// check for loops
		if (subRoles.contains(r))
			return;

		// reflexive
		subRoles.add(r);
		dependencies.put(r.getName(), ds);

		// transitive closure
		final Map<ATerm, DependencySet> immSubs = new HashMap<>();
		computeImmediateSubRoles(r, immSubs);
		for (final Entry<ATerm, DependencySet> entry : immSubs.entrySet())
		{
			final ATerm sub = entry.getKey();
			final DependencySet subDS = OpenlletOptions.USE_TRACING ? ds.union(entry.getValue(), true) : DependencySet.INDEPENDENT;
			if (sub instanceof ATermAppl)
			{
				final Role subRole = getRole(sub);

				computeSubRoles(subRole, subRoles, subRoleChains, dependencies, subDS);
			}
			else
			{
				subRoleChains.add((ATermList) sub);
				dependencies.put(sub, subDS);
			}
		}
	}

	/**
	 * Returns a string representation of the RBox where for each role subroles, superroles, and isTransitive information is given
	 */
	@Override
	public String toString()
	{
		return "[RBox " + _roles.values() + "]";
	}

}
