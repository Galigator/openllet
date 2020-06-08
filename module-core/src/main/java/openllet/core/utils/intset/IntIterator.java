// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.utils.intset;

import java.util.Objects;
import java.util.function.Consumer;

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
public interface IntIterator
{
	boolean hasNext();

	int next();

	default void remove()
	{
		throw new UnsupportedOperationException("remove");
	}

	default void forEachRemaining(final Consumer<Integer> action)
	{
		Objects.requireNonNull(action);
		while (hasNext())
			action.accept(next());
	}
}
