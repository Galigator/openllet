// Portions Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// Clark & Parsia, LLC parts of this source code are available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com
//
// ---
// Portions Copyright (c) 2003 Ron Alford, Mike Grove, Bijan Parsia, Evren Sirin
// Alford, Grove, Parsia, Sirin parts of this source code are available under the terms of the MIT License.
//
// The MIT License
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to
// deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.

package openllet.core.expressivity;

import java.util.HashSet;
import java.util.Set;
import openllet.aterm.ATermAppl;

/**
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Evren Sirin, Harris Lin
 */
public class Expressivity
{
	/**
	 * not (owl:complementOf) is used directly or indirectly
	 */
	private boolean _hasNegation = false;
	private boolean _hasAllValues = false;
	private boolean _hasDisjointClasses = false;

	/**
	 * An inverse property has been defined or a property has been defined as InverseFunctional
	 */
	private boolean _hasInverse = false;
	private boolean _hasFunctionality = false;
	private boolean _hasCardinality = false;
	private boolean _hasCardinalityQ = false;
	private boolean _hasFunctionalityD = false;
	private boolean _hasCardinalityD = false;
	private boolean _hasTransitivity = false;
	private boolean _hasRoleHierarchy = false;
	private boolean _hasReflexivity = false;
	private boolean _hasIrreflexivity = false;
	private boolean _hasDisjointRoles = false;
	private boolean _hasAsymmetry = false;
	private boolean _hasComplexSubRoles = false;
	private boolean _hasDatatype = false;
	private boolean _hasUserDefinedDatatype = false;

	private boolean _hasKeys = false;

	private boolean _hasDomain = false;
	private boolean _hasRange = false;

	private boolean _hasIndividual = false;
	/**
	 * The set of individuals in the ABox that have been used as nominals, i.e. in an owl:oneOf enumeration or target of owl:hasValue restriction
	 */
	private Set<ATermAppl> _nominals = new HashSet<>();

	private Set<ATermAppl> _anonInverses = new HashSet<>();

	public Expressivity()
	{
		// Nothing to do.
	}

	public Expressivity(final Expressivity other)
	{
		_hasNegation = other._hasNegation;
		_hasAllValues = other._hasAllValues;
		_hasDisjointClasses = other._hasDisjointClasses;
		_hasInverse = other._hasInverse;
		_hasFunctionality = other._hasFunctionality;
		_hasCardinality = other._hasCardinality;
		_hasCardinalityQ = other._hasCardinalityQ;
		_hasFunctionalityD = other._hasFunctionalityD;
		_hasCardinalityD = other._hasCardinalityD;
		_hasTransitivity = other._hasTransitivity;
		_hasRoleHierarchy = other._hasRoleHierarchy;
		_hasReflexivity = other._hasReflexivity;
		_hasIrreflexivity = other._hasIrreflexivity;
		_hasDisjointRoles = other._hasDisjointRoles;
		_hasAsymmetry = other._hasAsymmetry;
		_hasComplexSubRoles = other._hasComplexSubRoles;
		_hasDatatype = other._hasDatatype;
		_hasKeys = other._hasKeys;
		_hasDomain = other._hasDomain;
		_hasRange = other._hasRange;
		_hasIndividual = other._hasIndividual;
		_nominals = new HashSet<>(other._nominals);
		_anonInverses = new HashSet<>(other._anonInverses);
	}

	public boolean isEL()
	{
		return !_hasNegation && !_hasAllValues && !_hasInverse && !_hasFunctionality && !_hasCardinality//
				&& !_hasCardinalityQ && !_hasFunctionalityD && !_hasCardinalityD && !_hasIrreflexivity//
				&& !_hasDisjointRoles && !_hasAsymmetry && !_hasDatatype && !_hasKeys && !_hasIndividual//
				&& _nominals.isEmpty();
	}

	@Override
	public String toString()
	{
		String dl = "";

		if (isEL())
		{
			dl = "EL";

			if (_hasComplexSubRoles || _hasReflexivity || _hasDomain || _hasRange || _hasDisjointClasses)
				dl += "+";
			else
				if (_hasRoleHierarchy)
					dl += "H";
		}
		else
		{
			dl = "AL";

			if (_hasNegation)
				dl = "ALC";

			if (_hasTransitivity)
				dl += "R+";

			if ("ALCR+".equals(dl))
				dl = "S";

			if (_hasComplexSubRoles)
				dl = "SR";
			else
				if (_hasRoleHierarchy)
					dl += "H";

			if (hasNominal())
				dl += "O";

			if (_hasInverse)
				dl += "I";

			if (_hasCardinalityQ)
				dl += "Q";
			else
				if (_hasCardinality)
					dl += "N";
				else
					if (_hasFunctionality)
						dl += "F";

			if (_hasDatatype)
				if (_hasKeys)
					dl += "(Dk)";
				else
					dl += "(D)";
		}

		return dl;
	}

	public void setHasNegation(final boolean v)
	{
		_hasNegation = v;
	}

	public void setHasAllValues(final boolean v)
	{
		_hasAllValues = v;
	}

	public void setHasDisjointClasses(final boolean v)
	{
		_hasDisjointClasses = v;
	}

	/**
	 * @return Returns the hasInverse.
	 */
	public boolean hasInverse()
	{
		return _hasInverse;
	}

	public void setHasInverse(final boolean v)
	{
		_hasInverse = v;
	}

	/**
	 * @return Returns the hasFunctionality.
	 */
	public boolean hasFunctionality()
	{
		return _hasFunctionality;
	}

	public void setHasFunctionality(final boolean v)
	{
		_hasFunctionality = v;
	}

	/**
	 * @return Returns the hasCardinality.
	 */
	public boolean hasCardinality()
	{
		return _hasCardinality;
	}

	public void setHasCardinality(final boolean v)
	{
		_hasCardinality = v;
	}

	/**
	 * @return Returns the hasCardinality.
	 */
	public boolean hasCardinalityQ()
	{
		return _hasCardinalityQ;
	}

	public void setHasCardinalityQ(final boolean v)
	{
		_hasCardinalityQ = v;
	}

	/**
	 * @return true if a cardinality restriction (less than or equal to 1) is defined on any datatype property
	 */
	public boolean hasFunctionalityD()
	{
		return _hasFunctionalityD;
	}

	public void setHasFunctionalityD(final boolean v)
	{
		_hasFunctionalityD = v;
	}

	/**
	 * @return true if a cardinality restriction (greater than 1) is defined on any datatype property
	 */
	public boolean hasCardinalityD()
	{
		return _hasCardinalityD;
	}

	public void setHasCardinalityD(final boolean v)
	{
		_hasCardinalityD = v;
	}

	/**
	 * @return Returns the hasTransitivity.
	 */
	public boolean hasTransitivity()
	{
		return _hasTransitivity;
	}

	public void setHasTransitivity(final boolean v)
	{
		_hasTransitivity = v;
	}

	public void setHasRoleHierarchy(final boolean v)
	{
		_hasRoleHierarchy = v;
	}

	public void setHasReflexivity(final boolean v)
	{
		_hasReflexivity = v;
	}

	public void setHasIrreflexivity(final boolean v)
	{
		_hasIrreflexivity = v;
	}

	public boolean hasDisjointRoles()
	{
		return _hasDisjointRoles;
	}

	public void setHasDisjointRoles(final boolean v)
	{
		_hasDisjointRoles = v;
	}

	public boolean hasAsymmmetry()
	{
		return _hasAsymmetry;
	}

	public void setHasAsymmetry(final boolean v)
	{
		_hasAsymmetry = v;
	}

	public boolean hasComplexSubRoles()
	{
		return _hasComplexSubRoles;
	}

	public void setHasComplexSubRoles(final boolean v)
	{
		_hasComplexSubRoles = v;
	}

	public void setHasDatatype(final boolean v)
	{
		_hasDatatype = v;
	}

	public boolean hasUserDefinedDatatype()
	{
		return _hasUserDefinedDatatype;
	}

	public void setHasUserDefinedDatatype(final boolean v)
	{
		if (v)
			setHasDatatype(true);
		_hasUserDefinedDatatype = v;
	}

	public boolean hasKeys()
	{
		return _hasKeys;
	}

	public void setHasKeys(final boolean v)
	{
		_hasKeys = v;
	}

	public void setHasDomain(final boolean v)
	{
		_hasDomain = v;
	}

	public void setHasRange(final boolean v)
	{
		_hasRange = v;
	}

	public void setHasIndividual(final boolean v)
	{
		_hasIndividual = v;
	}

	public boolean hasNominal()
	{
		return !_nominals.isEmpty();
	}

	public Set<ATermAppl> getNominals()
	{
		return _nominals;
	}

	public void addNominal(final ATermAppl n)
	{
		_nominals.add(n);
	}

	/**
	 * Returns every property p such that inv(p) is used in an axiom in the KB. The named inverses are not considered.
	 *
	 * @return the set of properties whose anonymous inverse is used
	 */
	public Set<ATermAppl> getAnonInverses()
	{
		return _anonInverses;
	}

	public void addAnonInverse(final ATermAppl p)
	{
		_anonInverses.add(p);
	}
}
