// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.tableau.completion.incremental;

import openllet.aterm.ATermAppl;
import openllet.core.boxes.abox.Clash;

/**
 * A _clash dependency.
 *
 * @author Christian Halaschek-Wiener
 */
public class ClashDependency implements Dependency
{

	/**
	 * The _assertion
	 */
	private final ATermAppl _assertion;

	/**
	 * The _clash
	 */
	private final Clash _clash;

	/**
	 * Constructor
	 *
	 * @param assertion
	 * @param clash
	 */
	public ClashDependency(final ATermAppl assertion, final Clash clash)
	{
		_assertion = assertion;
		_clash = clash;
	}

	/**
	 * ToString method
	 */
	@Override
	public String toString()
	{
		return "Clash [" + _assertion + "]  - [" + _clash + "]";
	}

	/**
	 * Equals method
	 */
	@Override
	public boolean equals(final Object other)
	{
		if (other instanceof ClashDependency)
			return _assertion.equals(((ClashDependency) other)._assertion) && _clash.getNode().equals(((ClashDependency) other)._clash.getNode()) && _clash.getType() == ((ClashDependency) other)._clash.getType() && _clash.getDepends().equals(((ClashDependency) other)._clash.getDepends());
		else
			return false;
	}

	/**
	 * Hashcode method TODO: this may not be sufficient
	 */
	@Override
	public int hashCode()
	{
		return _clash.getType().hashCode() + _clash.getDepends().hashCode() + _clash.getNode().hashCode() + _assertion.hashCode();
	}

	/**
	 * @return the _assertion
	 */
	protected ATermAppl getAssertion()
	{
		return _assertion;
	}

	/**
	 * @return the _clash
	 */
	public Clash getClash()
	{
		return _clash;
	}

}
