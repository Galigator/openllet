// Copyright (c) 2006 - 2010, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.taxonomy;

import static openllet.core.utils.TermFactory.BOTTOM;
import static openllet.core.utils.TermFactory.TOP;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.utils.CollectionUtils;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.KosarajuStrongConnectivityInspector;
import org.jgrapht.alg.interfaces.StrongConnectivityAlgorithm;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * @author Evren Sirin
 */
public class JGraphBasedDefinitionOrder extends AbstractDefinitionOrder
{
	private Map<ATermAppl, Set<ATermAppl>> _equivalents;

	private DirectedGraph<ATermAppl, DefaultEdge> _graph;

	public JGraphBasedDefinitionOrder(final KnowledgeBase kb, final Comparator<ATerm> comparator)
	{
		super(kb, comparator);
	}

	private Set<ATermAppl> createSet()
	{
		return _comparator != null ? new TreeSet<>(_comparator) : CollectionUtils.<ATermAppl> makeIdentitySet();
	}

	private Queue<ATermAppl> createQueue()
	{
		return _comparator != null ? new PriorityQueue<>(10, _comparator) : new LinkedList<>();
	}

	private boolean addEquivalent(final ATermAppl key, final ATermAppl value)
	{
		Set<ATermAppl> values = _equivalents.get(key);
		if (values == null)
		{
			values = createSet();
			_equivalents.put(key, values);
		}

		return values.add(value);
	}

	private Set<ATermAppl> getAllEquivalents(final ATermAppl key)
	{
		Set<ATermAppl> values = _equivalents.get(key);

		if (values != null)
			values.add(key);
		else
			values = Collections.singleton(key);

		return values;
	}

	private Set<ATermAppl> getEquivalents(final ATermAppl key)
	{
		final Set<ATermAppl> values = _equivalents.get(key);
		return values != null ? values : Collections.<ATermAppl> emptySet();
	}

	@Override
	protected void initialize()
	{
		_equivalents = CollectionUtils.makeIdentityMap();

		_graph = new DefaultDirectedGraph<>(DefaultEdge.class);

		_graph.addVertex(TOP);
		for (final ATermAppl c : _kb.getClasses())
			_graph.addVertex(c);
	}

	@Override
	protected void addUses(final ATermAppl c, final ATermAppl usedByC)
	{
		if (c.equals(TOP))
			addEquivalent(TOP, usedByC);
		else
			if (!c.equals(usedByC))
				_graph.addEdge(c, usedByC);
	}

	@Override
	protected Set<ATermAppl> computeCycles()
	{
		final Set<ATermAppl> cyclicConcepts = CollectionUtils.makeIdentitySet();

		cyclicConcepts.addAll(getEquivalents(TOP));

		final StrongConnectivityAlgorithm<ATermAppl, DefaultEdge> scInspector = new KosarajuStrongConnectivityInspector<>(_graph);
		final List<Set<ATermAppl>> sccList = scInspector.stronglyConnectedSets();
		for (final Set<ATermAppl> scc : sccList)
		{
			if (scc.size() == 1)
				continue;

			cyclicConcepts.addAll(scc);

			collapseCycle(scc);
		}

		return cyclicConcepts;
	}

	private void collapseCycle(final Set<ATermAppl> scc)
	{
		final Iterator<ATermAppl> i = scc.iterator();
		final ATermAppl rep = i.next();

		while (i.hasNext())
		{
			final ATermAppl node = i.next();

			addEquivalent(rep, node);

			for (final DefaultEdge edge : _graph.incomingEdgesOf(node))
			{
				final ATermAppl incoming = _graph.getEdgeSource(edge);
				if (!incoming.equals(rep))
					_graph.addEdge(incoming, rep);
			}

			for (final DefaultEdge edge : _graph.outgoingEdgesOf(node))
			{
				final ATermAppl outgoing = _graph.getEdgeTarget(edge);
				if (!outgoing.equals(rep))
					_graph.addEdge(rep, outgoing);
			}

			_graph.removeVertex(node);
		}
	}

	@Override
	protected List<ATermAppl> computeDefinitionOrder()
	{
		final List<ATermAppl> definitionOrder = CollectionUtils.makeList();

		definitionOrder.add(TOP);
		definitionOrder.addAll(getEquivalents(TOP));

		_graph.removeVertex(TOP);

		destructiveTopologocialSort(definitionOrder);

		definitionOrder.add(BOTTOM);

		return definitionOrder;
	}

	public void destructiveTopologocialSort(final List<ATermAppl> nodesSorted)
	{
		final Queue<ATermAppl> nodesPending = createQueue();

		for (final ATermAppl node : _graph.vertexSet())
			if (_graph.outDegreeOf(node) == 0)
				nodesPending.add(node);

		while (!nodesPending.isEmpty())
		{
			final ATermAppl node = nodesPending.remove();

			assert _graph.outDegreeOf(node) == 0;

			nodesSorted.addAll(getAllEquivalents(node));

			for (final DefaultEdge edge : _graph.incomingEdgesOf(node))
			{
				final ATermAppl source = _graph.getEdgeSource(edge);
				if (_graph.outDegreeOf(source) == 1)
					nodesPending.add(source);
			}

			_graph.removeVertex(node);
		}

		assert _graph.vertexSet().isEmpty() : "Failed to sort elements: " + _graph.vertexSet();
	}
}
