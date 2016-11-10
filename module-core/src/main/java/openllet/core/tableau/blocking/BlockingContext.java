// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.tableau.blocking;

import java.util.Set;
import openllet.core.boxes.abox.Edge;
import openllet.core.boxes.abox.Individual;
import openllet.core.boxes.abox.Node;
import openllet.core.boxes.rbox.Role;
import openllet.core.utils.SetUtils;

/**
 * A class to keep track of the _current _individual being tested for blocking conditions. Current context stores the _blocker candidate and caches the incoming
 * edges to the (possibly) blocked individual since multiple blocking conditions need to access that information.
 *
 * @author Evren Sirin
 */
public final class BlockingContext
{
	public final Individual _blocked;
	public volatile Individual _blocker;
	private Set<Role> _rolesToBlocked;

	public BlockingContext(final Individual blocked)
	{
		_blocked = blocked;
		_blocker = blocked;
	}

	/**
	 * Sets the _blocker to the parent of _current _blocker and checks if if the new _blocker candidate is allowed to block. Root _nodes are not allowed to
	 * block.
	 *
	 * @return <code>true</code> if the new _blocker candidate is allowed to block
	 */
	public boolean moveBlockerUp()
	{
		_blocker = _blocker.getParent();
		_rolesToBlocked = null;

		return !_blocker.isRoot();
	}

	/**
	 * Sets the _blocker to the specified child of the _current _blocker and returns if the new _blocker candidate is allowed to block. The child is not allowed
	 * to block if it is a literal, or a root, or pruned/merged, or is _blocked itself.
	 *
	 * @param child child of the _current _blocker
	 * @return <code>true</code> if the new _blocker candidate is allowed to block
	 */
	public boolean moveBlockerDown(final Node child)
	{
		if (child.isLiteral() || child.isRoot() || child.isPruned() || child.isMerged() || ((Individual) child).isBlocked() || child.equals(_blocker))
			return false;

		_blocker = (Individual) child;
		_rolesToBlocked = null;

		return true;
	}

	/**
	 * Returns if the _blocked _node is an r-successor of its parent.
	 *
	 * @param r the property to check for r-successor relation
	 * @return <true> if the _blocked _node is an r-successor of its parent.
	 */
	public boolean isRSuccessor(final Role r)
	{
		return getIncomingRoles().contains(r);
	}

	/**
	 * Returns if the role from the parent of _blocked candidate has any inverse super properties.
	 *
	 * @return if the role from the parent of _blocked candidate has any inverse super properties
	 */
	public boolean isInvSuccessor()
	{
		for (final Role role : getIncomingRoles())
			if (role.isAnon())
				return true;

		return false;
	}

	/**
	 * Returns the roles that points to the _blocked candidate from its parent and _cache the result for future use.
	 *
	 * @return the roles that points to the _blocked candidate from its parent
	 */
	protected Set<Role> getIncomingRoles()
	{
		if (_rolesToBlocked == null)
		{
			_rolesToBlocked = getIncomingRoles(_blocked);

			assert _rolesToBlocked != null;
		}

		return _rolesToBlocked;
	}

	/**
	 * Returns the roles that points to the given _individual from its parent.
	 *
	 * @param ind _individual to check
	 * @return the roles that points to the given _individual from its parent
	 */
	public static Set<Role> getIncomingRoles(final Individual ind)
	{
		Set<Role> rolesToBlocked = null;
		for (final Edge e : ind.getInEdges())
			if (e.getFrom().equals(ind.getParent()))
				if (rolesToBlocked == null)
					rolesToBlocked = e.getRole().getSuperRoles();
				else
					if (!rolesToBlocked.contains(e.getRole()))
					{
						rolesToBlocked = SetUtils.create(rolesToBlocked);
						rolesToBlocked.addAll(e.getRole().getSuperRoles());
					}
		return rolesToBlocked;
	}

	@Override
	public String toString()
	{
		return _blocked + " blocked by " + _blocker;
	}
}
