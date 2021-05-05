// Copyright (c) 2006 - 2010, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.rules.rete;

import openllet.core.rules.rete.NodeProvider.TokenNodeProvider;
import openllet.core.rules.rete.NodeProvider.WMENodeProvider;

import java.util.Objects;

public class JoinCondition implements FilterCondition
{
	private final WMENodeProvider _wmeProvider;
	private final TokenNodeProvider _tokenProvider;

	public JoinCondition(final WMENodeProvider wme, final TokenNodeProvider token)
	{
		_wmeProvider = wme;
		_tokenProvider = token;
	}

	@Override
	public boolean test(final WME wme, final Token token)
	{
		return Objects.equals(_wmeProvider.getNode(wme, token).getTerm(), _tokenProvider.getNode(wme, token).getTerm());
	}

	public WMENodeProvider getWME()
	{
		return _wmeProvider;
	}

	public TokenNodeProvider getToken()
	{
		return _tokenProvider;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + _wmeProvider.hashCode();
		result = prime * result + _tokenProvider.hashCode();
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof JoinCondition))
			return false;
		final JoinCondition other = (JoinCondition) obj;
		return _wmeProvider.equals(other._wmeProvider) && _tokenProvider.equals(other._tokenProvider);
	}

	@Override
	public String toString()
	{
		return _wmeProvider + "=" + _tokenProvider;
	}
}
