// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.tracker;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import openllet.aterm.ATermAppl;
import openllet.core.boxes.abox.ABoxImpl;
import openllet.core.boxes.abox.DefaultEdge;
import openllet.core.boxes.abox.Edge;
import openllet.core.boxes.abox.Individual;
import openllet.core.boxes.abox.Node;

/**
 * <p>
 * Title: Simple incremental change tracker
 * </p>
 * <p>
 * Description: Basic implementation of {@link IncrementalChangeTracker} interface
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
public class SimpleIncrementalChangeTracker implements IncrementalChangeTracker
{
	private final HashSet<Edge> _deletedEdges;
	private final HashMap<Node, Set<ATermAppl>> _deletedTypes;
	private final HashSet<Edge> _newEdges;
	private final HashSet<Individual> _newIndividuals;
	private final HashSet<Node> _unprunedNodes;
	private final HashSet<Individual> _updatedIndividuals;

	public SimpleIncrementalChangeTracker()
	{
		_deletedEdges = new HashSet<>();
		_deletedTypes = new HashMap<>();
		_newEdges = new HashSet<>();
		_newIndividuals = new HashSet<>();
		_unprunedNodes = new HashSet<>();
		_updatedIndividuals = new HashSet<>();
	}

	private SimpleIncrementalChangeTracker(final SimpleIncrementalChangeTracker src, final ABoxImpl target)
	{

		_deletedEdges = new HashSet<>(src._deletedEdges.size());
		_newEdges = new HashSet<>(src._newEdges.size());

		for (final Edge se : src._deletedEdges)
		{
			final Individual s = target.getIndividual(se.getFrom().getName());
			if (s == null)
				throw new NullPointerException();
			final Node o = target.getNode(se.getTo().getName());
			if (o == null)
				throw new NullPointerException();

			_newEdges.add(new DefaultEdge(se.getRole(), s, o, se.getDepends()));
		}

		_deletedTypes = new HashMap<>(src._deletedTypes.size());

		for (final Map.Entry<Node, Set<ATermAppl>> e : src._deletedTypes.entrySet())
		{
			final Node n = target.getNode(e.getKey().getName());
			if (n == null)
				throw new NullPointerException();
			_deletedTypes.put(n, new HashSet<>(e.getValue()));
		}

		for (final Edge se : src._newEdges)
		{
			final Individual s = target.getIndividual(se.getFrom().getName());
			if (s == null)
				throw new NullPointerException();
			final Node o = target.getNode(se.getTo().getName());
			if (o == null)
				throw new NullPointerException();

			_newEdges.add(new DefaultEdge(se.getRole(), s, o, se.getDepends()));
		}

		_newIndividuals = new HashSet<>(src._newIndividuals.size());

		for (final Individual si : src._newIndividuals)
		{
			final Individual ti = target.getIndividual(si.getName());
			if (ti == null)
				throw new NullPointerException();

			_newIndividuals.add(ti);
		}

		_unprunedNodes = new HashSet<>(src._unprunedNodes.size());

		for (final Node sn : src._unprunedNodes)
		{
			final Node tn = target.getNode(sn.getName());
			if (tn == null)
				throw new NullPointerException();

			_unprunedNodes.add(tn);
		}

		_updatedIndividuals = new HashSet<>(src._updatedIndividuals.size());

		for (final Individual si : src._updatedIndividuals)
		{
			final Individual ti = target.getIndividual(si.getName());
			if (ti == null)
				throw new NullPointerException();

			_updatedIndividuals.add(ti);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see openllet.core.IncrementalChangeTracker#addDeletedEdge(org.mindswap.pellet.Edge)
	 */
	@Override
	public boolean addDeletedEdge(final Edge e)
	{
		if (e == null)
			throw new NullPointerException();

		return _deletedEdges.add(e);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see openllet.core.IncrementalChangeTracker#addDeletedType(org.mindswap.pellet.Node,
	 *      openllet.aterm.ATermAppl)
	 */
	@Override
	public boolean addDeletedType(final Node n, final ATermAppl type)
	{
		if (n == null)
			throw new NullPointerException();
		if (type == null)
			throw new NullPointerException();

		Set<ATermAppl> existing = _deletedTypes.get(n);
		if (existing == null)
		{
			existing = new HashSet<>();
			_deletedTypes.put(n, existing);
		}

		return existing.add(type);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see openllet.core.IncrementalChangeTracker#addNewEdge(org.mindswap.pellet.Edge)
	 */
	@Override
	public boolean addNewEdge(final Edge e)
	{
		if (e == null)
			throw new NullPointerException();

		return _newEdges.add(e);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see openllet.core.IncrementalChangeTracker#addNewIndividual(org.mindswap.pellet.Individual)
	 */
	@Override
	public boolean addNewIndividual(final Individual i)
	{
		if (i == null)
			throw new NullPointerException();

		return _newIndividuals.add(i);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see openllet.core.IncrementalChangeTracker#addUnprunedNode(org.mindswap.pellet.Node)
	 */
	@Override
	public boolean addUnprunedNode(final Node n)
	{
		if (n == null)
			throw new NullPointerException();

		return _unprunedNodes.add(n);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see openllet.core.IncrementalChangeTracker#addUpdatedIndividual(org.mindswap.pellet.Individual)
	 */
	@Override
	public boolean addUpdatedIndividual(final Individual i)
	{
		if (i == null)
			throw new NullPointerException();

		return _updatedIndividuals.add(i);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see openllet.core.IncrementalChangeTracker#clear()
	 */
	@Override
	public void clear()
	{
		_deletedEdges.clear();
		_deletedTypes.clear();
		_newEdges.clear();
		_newIndividuals.clear();
		_unprunedNodes.clear();
		_updatedIndividuals.clear();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see openllet.core.IncrementalChangeTracker#copy(org.mindswap.pellet.ABox)
	 */
	@Override
	public SimpleIncrementalChangeTracker copy(final ABoxImpl target)
	{
		return new SimpleIncrementalChangeTracker(this, target);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see openllet.core.IncrementalChangeTracker#deletedEdges()
	 */
	@Override
	public Iterator<Edge> deletedEdges()
	{
		return Collections.unmodifiableSet(_deletedEdges).iterator();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see openllet.core.IncrementalChangeTracker#deletedTypes()
	 */
	@Override
	public Iterator<Entry<Node, Set<ATermAppl>>> deletedTypes()
	{
		return Collections.unmodifiableMap(_deletedTypes).entrySet().iterator();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see openllet.core.IncrementalChangeTracker#newEdges()
	 */
	@Override
	public Iterator<Edge> newEdges()
	{
		return Collections.unmodifiableSet(_newEdges).iterator();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see openllet.core.IncrementalChangeTracker#newIndividuals()
	 */
	@Override
	public Iterator<Individual> newIndividuals()
	{
		return Collections.unmodifiableSet(_newIndividuals).iterator();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see openllet.core.IncrementalChangeTracker#unprunedNodes()
	 */
	@Override
	public Iterator<Node> unprunedNodes()
	{
		return Collections.unmodifiableSet(_unprunedNodes).iterator();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see openllet.core.IncrementalChangeTracker#updatedIndividuals()
	 */
	@Override
	public Iterator<Individual> updatedIndividuals()
	{
		return Collections.unmodifiableSet(_updatedIndividuals).iterator();
	}
}
