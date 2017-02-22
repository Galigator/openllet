// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.tableau.completion.rule;

import java.util.logging.Logger;
import openllet.core.IndividualIterator;
import openllet.core.OpenlletOptions;
import openllet.core.boxes.abox.Individual;
import openllet.core.boxes.abox.Node;
import openllet.core.tableau.completion.CompletionStrategy;
import openllet.core.tableau.completion.queue.NodeSelector;
import openllet.core.tableau.completion.queue.QueueElement;
import openllet.shared.tools.Log;

/**
 * <p>
 * Copyright: Copyright (c) 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Evren Sirin
 */
public abstract class AbstractTableauRule implements TableauRule
{
	public final static Logger _logger = Log.getLogger(AbstractTableauRule.class);

	protected enum BlockingType
	{
		NONE, DIRECT, INDIRECT, COMPLETE
	}

	protected final CompletionStrategy _strategy;
	protected final NodeSelector _nodeSelector;
	protected final BlockingType _blockingType;

	public AbstractTableauRule(final CompletionStrategy strategy, final NodeSelector nodeSelector, final BlockingType blockingType)
	{
		_strategy = strategy;
		_nodeSelector = nodeSelector;
		_blockingType = blockingType;
	}

	@Override
	public void apply(final IndividualIterator i)
	{
		i.reset(_nodeSelector);
		while (i.hasNext())
		{
			final Individual node = i.next();

			if (_strategy.getBlocking().isBlocked(node))
			{
				if (OpenlletOptions.USE_COMPLETION_QUEUE)
					addQueueElement(node);
			}
			else
			{
				apply(node);

				if (_strategy.getABox().isClosed())
					return;
			}
		}
	}

	protected boolean isBlocked(final Individual node)
	{
		switch (_blockingType)
		{
			case NONE:
				return false;
			case DIRECT:
				return _strategy.getBlocking().isDirectlyBlocked(node);
			case INDIRECT:
				return _strategy.getBlocking().isIndirectlyBlocked(node);
			case COMPLETE:
				return _strategy.getBlocking().isBlocked(node);
			default:
				throw new AssertionError();
		}
	}

	protected void addQueueElement(final Node node)
	{
		_strategy.getABox().getCompletionQueue().add(new QueueElement(node), _nodeSelector);
	}
}
