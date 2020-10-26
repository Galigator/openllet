// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.tracker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import openllet.aterm.ATermAppl;

/**
 * <p>
 * Title: Simple Branch Effect Tracker
 * </p>
 * <p>
 * Description: Basic ArrayList<HashSet> implementation of BranchEffectTracker
 * </p>
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Mike Smith
 */
public class SimpleBranchEffectTracker implements BranchEffectTracker
{

	private final ArrayList<Set<ATermAppl>> _effects;

	public SimpleBranchEffectTracker()
	{
		_effects = new ArrayList<>();
	}

	private SimpleBranchEffectTracker(final SimpleBranchEffectTracker other)
	{
		final int n = other._effects.size();

		_effects = new ArrayList<>(n);
		for (int i = 0; i < n; i++)
		{
			final Set<ATermAppl> s = other._effects.get(i);
			_effects.add(s == null ? null : new HashSet<>(s));
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see openllet.core.BranchEffectTracker#add(int, openllet.aterm.ATermAppl)
	 */
	@Override
	public boolean add(final int branch, final ATermAppl a)
	{
		if (branch <= 0)
			return false;

		final int diff = branch - _effects.size();
		if (diff > 0)
		{
			@SuppressWarnings("unchecked")
			final Set<ATermAppl> nulls[] = new Set[diff];
			_effects.addAll(Arrays.asList(nulls));
		}

		Set<ATermAppl> existing = _effects.get(branch - 1);
		if (existing == null)
		{
			existing = new HashSet<>();
			_effects.set(branch - 1, existing);
		}

		return existing.add(a);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see openllet.core.BranchEffectTracker#copy()
	 */
	@Override
	public SimpleBranchEffectTracker copy()
	{
		return new SimpleBranchEffectTracker(this);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see openllet.core.BranchEffectTracker#getAll(int)
	 */
	@Override
	public Set<ATermAppl> getAll(final int branch)
	{

		if (branch < 1)
			throw new IllegalArgumentException();

		if (branch > _effects.size())
			return Collections.emptySet();

		final Set<ATermAppl> ret = new HashSet<>();
		for (int i = branch - 1; i < _effects.size(); i++)
		{
			final Set<ATermAppl> s = _effects.get(i);
			if (s != null)
				ret.addAll(s);
		}

		return ret;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see openllet.core.BranchEffectTracker#remove(int)
	 */
	@Override
	public Set<ATermAppl> remove(final int branch)
	{
		if (branch < 1)
			throw new IllegalArgumentException();

		if (branch > _effects.size())
			return Collections.emptySet();

		final Set<ATermAppl> ret = _effects.remove(branch - 1);
		if (ret == null)
			return Collections.emptySet();

		return ret;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see openllet.core.BranchEffectTracker#removeAll(int)
	 */
	@Override
	public Set<ATermAppl> removeAll(final int branch)
	{

		if (branch < 1)
			throw new IllegalArgumentException();

		if (branch > _effects.size())
			return Collections.emptySet();

		final Set<ATermAppl> ret = new HashSet<>();
		for (int i = _effects.size() - 1; i >= branch - 1; i--)
		{
			final Set<ATermAppl> s = _effects.remove(i);
			if (s != null)
				ret.addAll(s);
		}

		return ret;
	}
}
