// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.jena.graph.query;

import static openllet.core.utils.iterator.IteratorUtils.flatten;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.jena.JenaUtils;
import openllet.jena.PelletInfGraph;
import openllet.jena.graph.loader.GraphLoader;

public abstract class TripleQueryHandler
{

	public abstract boolean contains(KnowledgeBase kb, GraphLoader loader, Node subj, Node pred, Node obj);

	public abstract ExtendedIterator<Triple> find(KnowledgeBase kb, PelletInfGraph openllet, Node subj, Node pred, Node obj);

	protected ExtendedIterator<Triple> objectFiller(final Node s, final Node p, final Collection<ATermAppl> objects)
	{
		return objectFiller(s, p, objects.iterator());
	}

	@SuppressWarnings("static-method")
	protected ExtendedIterator<Triple> objectFiller(final Node s, final Node p, final Iterator<ATermAppl> objects)
	{
		return WrappedIterator.create(objects)//
				.filterKeep(JenaUtils._isGrapheNode)//
				.mapWith(JenaUtils::makeGraphNode)//
				.filterKeep(Optional::isPresent)//
				.mapWith(node -> Triple.create(s, p, node.get()));
	}

	protected ExtendedIterator<Triple> objectSetFiller(final Node s, final Node p, final Set<Set<ATermAppl>> objectSets)
	{
		return objectFiller(s, p, flatten(objectSets.iterator()));
	}

	protected ExtendedIterator<Triple> propertyFiller(final Node s, final Collection<ATermAppl> properties, final Node o)
	{
		return propertyFiller(s, properties.iterator(), o);
	}

	@SuppressWarnings("static-method")
	protected ExtendedIterator<Triple> propertyFiller(final Node s, final Iterator<ATermAppl> properties, final Node o)
	{
		return WrappedIterator.create(properties)//
				.filterKeep(JenaUtils._isGrapheNode)//
				.mapWith(JenaUtils::makeGraphNode)//
				.filterKeep(Optional::isPresent)//
				.mapWith(node -> Triple.create(s, node.get(), o));
	}

	protected ExtendedIterator<Triple> subjectFiller(final Collection<ATermAppl> subjects, final Node p, final Node o)
	{
		return subjectFiller(subjects.iterator(), p, o);
	}

	@SuppressWarnings("static-method")
	protected ExtendedIterator<Triple> subjectFiller(final Iterator<ATermAppl> subjects, final Node p, final Node o)
	{
		return WrappedIterator.create(subjects)//
				.filterKeep(JenaUtils._isGrapheNode)//
				.mapWith(JenaUtils::makeGraphNode)//
				.filterKeep(Optional::isPresent)//
				.mapWith(node -> Triple.create(node.get(), p, o));
	}

	protected ExtendedIterator<Triple> subjectSetFiller(final Set<Set<ATermAppl>> subjectSets, final Node p, final Node o)
	{
		return subjectFiller(flatten(subjectSets.iterator()), p, o);
	}
}
