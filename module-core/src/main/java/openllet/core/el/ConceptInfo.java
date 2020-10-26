// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.el;

import java.util.Set;

import openllet.aterm.ATermAppl;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.MultiValueMap;
import openllet.core.utils.SetUtils;

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
class ConceptInfo
{
	private final ATermAppl _concept;
	private final Set<Trigger> _triggers;

	private final MultiValueMap<ATermAppl, ConceptInfo> _successors;
	private final MultiValueMap<ATermAppl, ConceptInfo> _predecessors = new MultiValueMap<>();

	private final Set<ConceptInfo> superClasses = SetUtils.create();

	public ConceptInfo(final ATermAppl c, final boolean storeSuccessors, final boolean noTriggers)
	{
		_concept = c;

		_successors = storeSuccessors ? new MultiValueMap<>() : null;
		_predecessors.clear();

		_triggers = noTriggers ? null : SetUtils.create();
	}

	public boolean addSuccessor(final ATermAppl p, final ConceptInfo ci)
	{
		if (ci._predecessors.add(p, this))
		{
			if (_successors != null)
				_successors.add(p, ci);

			return true;
		}

		return false;
	}

	public boolean addSuperClass(final ConceptInfo sup)
	{
		return superClasses.add(sup);
	}

	public boolean addTrigger(final Trigger trigger)
	{
		return _triggers.add(trigger);
	}

	@Override
	public boolean equals(final Object obj)
	{
		return obj instanceof ConceptInfo && ((ConceptInfo) obj)._concept == _concept;
	}

	public ATermAppl getConcept()
	{
		return _concept;
	}

	public MultiValueMap<ATermAppl, ConceptInfo> getSuccessors()
	{
		return _successors;
	}

	public MultiValueMap<ATermAppl, ConceptInfo> getPredecessors()
	{
		return _predecessors;
	}

	public Set<ConceptInfo> getSuperClasses()
	{
		return superClasses;
	}

	public Set<Trigger> getTriggers()
	{
		return _triggers;
	}

	public boolean hasSuccessor(final ATermAppl p, final ConceptInfo ci)
	{
		return ci._predecessors.contains(p, this);
	}

	@Override
	public int hashCode()
	{
		return _concept.hashCode();
	}

	public boolean hasSuperClass(final ConceptInfo ci)
	{
		return superClasses.contains(ci);
	}

	@Override
	public String toString()
	{
		return ATermUtils.toString(_concept);
	}
}
