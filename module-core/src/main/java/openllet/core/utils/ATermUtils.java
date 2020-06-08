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

package openllet.core.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import openllet.aterm.AFun;
import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.aterm.ATermFactory;
import openllet.aterm.ATermInt;
import openllet.aterm.ATermList;
import openllet.aterm.pure.PureFactory;
import openllet.atom.OpenError;
import openllet.core.boxes.rbox.Role;
import openllet.core.exceptions.InternalReasonerException;
import openllet.core.output.ATermManchesterSyntaxRenderer;
import openllet.core.utils.iterator.MultiListIterator;
import openllet.core.utils.iterator.PairIterator;

/**
 * This class provides the functions ATerm related functions. Creating terms for URI's and complex class descriptions is done here. There are also functions for
 * normalization, simplification and conversion to NNF (Normal Negation Form).
 *
 * @TODO move into aterm package.
 * @author Evren Sirin
 */
public class ATermUtils
{
	private static final ATermFactory factory = new PureFactory();

	public static final AFun LITFUN = factory.makeAFun("literal", 3, false);
	public static final int LIT_VAL_INDEX = 0;
	public static final int LIT_LANG_INDEX = 1;
	public static final int LIT_URI_INDEX = 2;

	public static final AFun ANDFUN = factory.and();
	public static final AFun ORFUN = factory.or();
	public static final AFun SOMEFUN = factory.some();
	public static final AFun ALLFUN = factory.all();
	public static final AFun NOTFUN = factory.not();
	public static final AFun MAXFUN = factory.max();
	public static final AFun MINFUN = factory.min();
	public static final AFun VALUEFUN = factory.value();
	public static final AFun SELFFUN = factory.self();
	/**
	 * This is not used in the reasoner but kept here to be used for display
	 */
	public static final AFun CARDFUN = factory.card();

	public static Set<AFun> CLASS_FUN = SetUtils.create(new AFun[] { ALLFUN, SOMEFUN, MAXFUN, MINFUN, CARDFUN, ANDFUN, ORFUN, NOTFUN, VALUEFUN, SELFFUN });

	public static final AFun INVFUN = factory.inv();

	public static final AFun SUBFUN = factory.subClassOf();
	public static final AFun EQCLASSFUN = factory.equivalentClasses();

	public static final AFun SAMEASFUN = factory.sameAs();

	public static final AFun DISJOINTFUN = factory.disjointWith();
	public static final AFun DISJOINTSFUN = factory.disjointClasses();

	public static final AFun DISJOINTPROPFUN = factory.disjointPropertyWith();
	public static final AFun DISJOINTPROPSFUN = factory.disjointProperties();

	public static final AFun COMPLEMENTFUN = factory.complementOf();

	/**
	 * This is used to represent variables in queries
	 */
	public static final AFun VARFUN = factory.var();

	public static final AFun TYPEFUN = factory.type();

	public static final AFun PROPFUN = factory.prop();

	/**
	 * Added for explanations
	 */
	public static final AFun DIFFERENTFUN = factory.different();
	public static final AFun ALLDIFFERENTFUN = factory.allDifferent();
	public static final AFun ASYMMETRICFUN = factory.asymmetric();
	/**
	 * @deprecated Use {@link #ASYMMETRICFUN}
	 */
	@Deprecated
	public static final AFun ANTISYMMETRICFUN = ASYMMETRICFUN;
	public static final AFun FUNCTIONALFUN = factory.functional();
	public static final AFun INVFUNCTIONALFUN = factory.inverseFunctional();
	public static final AFun IRREFLEXIVEFUN = factory.irreflexive();
	public static final AFun REFLEXIVEFUN = factory.reflexive();
	public static final AFun SYMMETRICFUN = factory.symmetric();
	public static final AFun TRANSITIVEFUN = factory.transitive();
	public static final AFun SUBPROPFUN = factory.subProperty();
	public static final AFun EQPROPFUN = factory.equivalentProperty();
	public static final AFun INVPROPFUN = factory.inverseProperty();

	public static final AFun DOMAINFUN = factory.domain();
	public static final AFun RANGEFUN = factory.range();

	public static final AFun RULEFUN = factory.rule();

	public static final AFun BUILTINFUN = factory.builtin();

	public static final AFun DATATYPEDEFFUN = factory.datatypeDefinition();

	public static final AFun RESTRDATATYPEFUN = factory.restrictedDatatype();

	public static final AFun FACET = factory.facet();

	public static final ATermAppl EMPTY = makeTermAppl("");

	public static final ATermList EMPTY_LIST = factory.makeList();

	/**
	 * Set of all axiom functors used in explanations
	 */
	public static Set<AFun> AXIOM_FUN = SetUtils.create(new AFun[] { TYPEFUN, PROPFUN, SAMEASFUN, DIFFERENTFUN, ALLDIFFERENTFUN, SUBFUN, EQCLASSFUN, DISJOINTFUN, DISJOINTSFUN, COMPLEMENTFUN, SUBPROPFUN, EQPROPFUN, INVPROPFUN, DOMAINFUN, RANGEFUN, FUNCTIONALFUN, INVFUNCTIONALFUN, TRANSITIVEFUN, SYMMETRICFUN, REFLEXIVEFUN, IRREFLEXIVEFUN, ANTISYMMETRICFUN, });

	// TOP and BOTTOM concepts. TOP is not defined as T or not(T) any
	// more but added to each _node manually. Defining TOP as a primitive
	// concept reduces number of GCIs and makes other reasoning tasks
	// faster
	public static final ATermAppl TOP = makeTermAppl("_TOP_");
	public static final ATermAppl BOTTOM = makeNot(TOP);

	public static final ATermAppl TOP_OBJECT_PROPERTY = makeTermAppl("_TOP_OBJECT_PROPERTY_");
	public static final ATermAppl TOP_DATA_PROPERTY = makeTermAppl("_TOP_DATA_PROPERTY_");
	public static final ATermAppl BOTTOM_OBJECT_PROPERTY = makeTermAppl("_BOTTOM_OBJECT_PROPERTY_");
	public static final ATermAppl BOTTOM_DATA_PROPERTY = makeTermAppl("_BOTTOM_DATA_PROPERTY_");

	public static final ATermAppl TOP_LIT = makeTermAppl(Namespaces.RDFS + "Literal");
	public static final ATermAppl BOTTOM_LIT = makeNot(TOP_LIT);

	public static final ATermAppl CONCEPT_SAT_IND = makeTermAppl("_C_");

	public static final ATermInt ONE = factory.makeInt(1);

	public static final ATermAppl LITERAL_STRING = makeTermAppl("http://www.w3.org/2001/XMLSchema#string");
	public static final ATermAppl PLAIN_LITERAL_DATATYPE = makeTermAppl(Namespaces.RDF + "PlainLiteral");

	public static QNameProvider qnames = new QNameProvider();

	static public ATermFactory getFactory()
	{
		return factory;
	}

	final static public ATermAppl makeTypeAtom(final ATermAppl ind, final ATermAppl c)
	{
		return factory.makeAppl(TYPEFUN, ind, c);
	}

	final static public ATermAppl makePropAtom(final ATermAppl p, final ATermAppl s, final ATermAppl o)
	{
		return factory.makeAppl(PROPFUN, p, s, o);
	}

	static public ATermAppl makeStringLiteral(final String value)
	{
		return factory.makeAppl(LITFUN, makeTermAppl(value), EMPTY, LITERAL_STRING);
	}

	static public ATermAppl makePlainLiteral(final String value)
	{
		return factory.makeAppl(LITFUN, makeTermAppl(value), EMPTY, PLAIN_LITERAL_DATATYPE);
	}

	static public ATermAppl makePlainLiteral(final String value, final String lang)
	{
		return factory.makeAppl(LITFUN, makeTermAppl(value), makeTermAppl(lang), PLAIN_LITERAL_DATATYPE);
	}

	static public ATermAppl makeTypedLiteral(final String value, final ATermAppl dt)
	{
		return factory.makeAppl(LITFUN, makeTermAppl(value), EMPTY, dt);
	}

	static public ATermAppl makeTypedLiteral(final String value, final String dt)
	{
		return factory.makeAppl(LITFUN, makeTermAppl(value), EMPTY, makeTermAppl(dt));
	}

	static public ATermAppl makeTypedPlainLangLiteral(final String value, final String lang)
	{
		return factory.makeAppl(LITFUN, makeTermAppl(value), makeTermAppl(lang), PLAIN_LITERAL_DATATYPE);
	}

	static public ATermAppl NO_DATATYPE = makeTermAppl("NO_DATATYPE");

	static public ATermAppl makeLiteral(final ATermAppl name)
	{
		return factory.makeAppl(LITFUN, name, EMPTY, NO_DATATYPE);
	}

	static public String getLiteralValue(final ATermAppl literal)
	{
		return ((ATermAppl) literal.getArgument(LIT_VAL_INDEX)).getName();
	}

	static public String getLiteralLang(final ATermAppl literal)
	{
		return ((ATermAppl) literal.getArgument(LIT_LANG_INDEX)).getName();
	}

	static public String getLiteralDatatype(final ATermAppl literal)
	{
		return ((ATermAppl) literal.getArgument(LIT_URI_INDEX)).getName();
	}

	static public ATermAppl makeTermAppl(final String name)
	{
		return factory.makeAppl(factory.makeAFun(name, 0, false));
	}

	static public ATermAppl makeTermAppl(final AFun fun, final ATerm[] args)
	{
		return factory.makeAppl(fun, args);
	}

	static public ATermAppl makeNot(final ATerm c)
	{
		return factory.makeAppl(NOTFUN, c);
	}

	static public ATerm term(final String str)
	{
		return factory.parse(str);
	}

	// negate all the elements in the list and return the new list
	static public ATermList negate(final ATermList list)
	{
		if (list.isEmpty())
			return list;

		ATermAppl a = (ATermAppl) list.getFirst();
		a = isNot(a) ? (ATermAppl) a.getArgument(0) : makeNot(a);
		final ATermList result = makeList(a, negate(list.getNext()));

		return result;
	}

	final static public ATermAppl negate(final ATermAppl a)
	{
		return isNot(a) ? (ATermAppl) a.getArgument(0) : makeNot(a);
	}

	public static final AFun BNODE_FUN = factory.makeAFun("bnode", 1, false);
	public static final AFun ANON_FUN = factory.makeAFun("anon", 1, false);
	public static final AFun ANON_NOMINAL_FUN = factory.makeAFun("anon_nominal", 1, false);

	private static final ATermAppl[] anonCache = new ATermAppl[1000];
	static
	{
		for (int i = 0; i < anonCache.length; i++)
			anonCache[i] = factory.makeAppl(ANON_FUN, factory.makeInt(i));
	}

	final static public boolean isAnonNominal(final ATermAppl term)
	{
		return term.getAFun().equals(ANON_NOMINAL_FUN);
	}

	final static public ATermAppl makeAnonNominal(final int id)
	{
		return factory.makeAppl(ANON_NOMINAL_FUN, factory.makeInt(id));
	}

	final static public ATermAppl makeAnon(final int id)
	{
		if (id < anonCache.length)
			return anonCache[id];
		return factory.makeAppl(ANON_FUN, factory.makeInt(id));
	}

	final static public ATermAppl makeBnode(final String id)
	{
		return factory.makeAppl(BNODE_FUN, makeTermAppl(id));
	}

	final static public ATermAppl makeVar(final String name)
	{
		return factory.makeAppl(VARFUN, makeTermAppl(name));
	}

	final static public ATermAppl makeVar(final ATermAppl name)
	{
		return factory.makeAppl(VARFUN, name);
	}

	final static public ATermAppl makeValue(final ATerm c)
	{
		return factory.makeAppl(VALUEFUN, c);
	}

	final static public ATermAppl makeInv(final ATermAppl r)
	{
		if (isInv(r))
			return (ATermAppl) r.getArgument(0);

		return factory.makeAppl(INVFUN, r);
	}

	final static public ATermAppl makeInvProp(final ATerm r, final ATerm s)
	{
		return factory.makeAppl(INVPROPFUN, r, s);
	}

	final static public ATermAppl makeSub(final ATerm a, final ATerm b)
	{
		return factory.makeAppl(SUBFUN, a, b);
	}

	final static public ATermAppl makeEqClasses(final ATerm a, final ATerm b)
	{
		return factory.makeAppl(EQCLASSFUN, a, b);
	}

	final static public ATermAppl makeSameAs(final ATerm a, final ATerm b)
	{
		return factory.makeAppl(SAMEASFUN, a, b);
	}

	final static public ATermAppl makeSubProp(final ATerm r, final ATerm s)
	{
		return factory.makeAppl(SUBPROPFUN, r, s);
	}

	final static public ATermAppl makeEqProp(final ATerm r, final ATerm s)
	{
		return factory.makeAppl(EQPROPFUN, r, s);
	}

	final static public ATermAppl makeDomain(final ATerm r, final ATerm c)
	{
		return factory.makeAppl(DOMAINFUN, r, c);
	}

	final static public ATermAppl makeRange(final ATerm r, final ATerm c)
	{
		return factory.makeAppl(RANGEFUN, r, c);
	}

	final static public ATermAppl makeComplement(final ATerm a, final ATerm b)
	{
		return factory.makeAppl(COMPLEMENTFUN, a, b);
	}

	final static public ATermAppl makeDisjoint(final ATerm a, final ATerm b)
	{
		return factory.makeAppl(DISJOINTFUN, a, b);
	}

	final static public ATermAppl makeDisjoints(final ATermList list)
	{
		return factory.makeAppl(DISJOINTSFUN, list);
	}

	final static public ATermAppl makeDisjointProperty(final ATerm a, final ATerm b)
	{
		return factory.makeAppl(DISJOINTPROPFUN, a, b);
	}

	final static public ATermAppl makeDisjointProperties(final ATermList list)
	{
		return factory.makeAppl(DISJOINTPROPSFUN, list);
	}

	final static public ATermAppl makeDifferent(final ATerm a, final ATerm b)
	{
		return factory.makeAppl(DIFFERENTFUN, a, b);
	}

	final static public ATermAppl makeAllDifferent(final ATermList list)
	{
		return factory.makeAppl(ALLDIFFERENTFUN, list);
	}

	final static public ATermAppl makeAsymmetric(final ATerm r)
	{
		return factory.makeAppl(ASYMMETRICFUN, r);
	}

	/**
	 * @param r
	 * @return DO NOT USE
	 * @deprecated Use {@link #makeAsymmetric(ATerm)}
	 */
	@Deprecated
	final static public ATermAppl makeAntisymmetric(final ATerm r)
	{
		return makeAsymmetric(r);
	}

	final static public ATermAppl makeFunctional(final ATerm a)
	{
		return factory.makeAppl(FUNCTIONALFUN, a);
	}

	final static public ATermAppl makeInverseFunctional(final ATerm a)
	{
		return factory.makeAppl(INVFUNCTIONALFUN, a);
	}

	final static public ATermAppl makeIrreflexive(final ATerm r)
	{
		return factory.makeAppl(IRREFLEXIVEFUN, r);
	}

	final static public ATermAppl makeReflexive(final ATerm r)
	{
		return factory.makeAppl(REFLEXIVEFUN, r);
	}

	final static public ATermAppl makeSymmetric(final ATerm r)
	{
		return factory.makeAppl(SYMMETRICFUN, r);
	}

	final static public ATermAppl makeTransitive(final ATerm r)
	{
		return factory.makeAppl(TRANSITIVEFUN, r);
	}

	final static public ATermAppl makeAnd(final ATerm c1, final ATerm c2)
	{
		return makeAnd(makeList(c2).insert(c1));
	}

	static public ATermAppl makeAnd(final ATermList list)
	{
		if (list == null)
			throw new NullPointerException();
		else
			if (list.isEmpty())
				return TOP;
			else
				if (list.getNext().isEmpty())
					return (ATermAppl) list.getFirst();

		return factory.makeAppl(ANDFUN, list);
	}

	final static public ATermAppl makeOr(final ATermAppl c1, final ATermAppl c2)
	{
		return makeOr(makeList(c2).insert(c1));
	}

	static public ATermAppl makeOr(final ATermList list)
	{
		if (list == null)
			throw new NullPointerException();
		else
			if (list.isEmpty())
				return BOTTOM;
			else
				if (list.getNext().isEmpty())
					return (ATermAppl) list.getFirst();

		return factory.makeAppl(ORFUN, list);
	}

	final static public ATermAppl makeAllValues(final ATerm r, final ATerm c)
	{
		final ATerm arg1;
		if (r.getType() == ATerm.LIST)
		{
			final ATermList list = (ATermList) r;
			arg1 = list.getLength() == 1 ? list.getFirst() : r;
		}
		else
			arg1 = r;

		return factory.makeAppl(ALLFUN, arg1, c);
	}

	final static public ATermAppl makeSomeValues(final ATerm r, final ATerm c)
	{
		assertTrue(c instanceof ATermAppl);

		return factory.makeAppl(SOMEFUN, r, c);
	}

	final static public ATermAppl makeSelf(final ATermAppl r)
	{
		return factory.makeAppl(SELFFUN, r);
	}

	final static public ATermAppl makeHasValue(final ATerm r, final ATerm ind)
	{
		final ATermAppl c = makeValue(ind);
		return factory.makeAppl(SOMEFUN, r, c);
	}

	final static public ATermAppl makeNormalizedMax(final ATermAppl r, final int n, final ATermAppl c)
	{
		assertTrue(n >= 0);

		return makeNot(makeMin(r, n + 1, c));
	}

	final static public ATermAppl makeMax(final ATerm r, final int n, final ATerm c)
	{
		// assertTrue( n >= 0 );

		// This was causing nnf to come out wrong
		// return makeNot(makeMin(r, n + 1));

		return makeMax(r, factory.makeInt(n), c);
	}

	final static public ATermAppl makeMax(final ATerm r, final ATermInt n, final ATerm c)
	{
		assertTrue(n.getInt() >= 0);

		return factory.makeAppl(MAXFUN, r, n, c);
	}

	final static public ATermAppl makeMin(final ATerm r, final int n, final ATerm c)
	{
		// comment out built-in simplification so that clashExplanation
		// axioms will come out right
		// if( n == 0 )
		// return TOP;

		return makeMin(r, factory.makeInt(n), c);
	}

	final static public ATermAppl makeMin(final ATerm r, final ATermInt n, final ATerm c)
	{
		assertTrue(n.getInt() >= 0);

		return factory.makeAppl(MINFUN, r, n, c);
	}

	final static public ATermAppl makeDisplayCard(final ATerm r, final int n, final ATerm c)
	{
		assertTrue(n >= 0);

		return factory.makeAppl(CARDFUN, r, factory.makeInt(n), c);
	}

	final static public ATermAppl makeDisplayMax(final ATerm r, final int n, final ATerm c)
	{
		assertTrue(n >= 0);

		return factory.makeAppl(MAXFUN, r, factory.makeInt(n), c);
	}

	final static public ATermAppl makeDisplayMin(final ATerm r, final int n, final ATerm c)
	{
		assertTrue(n >= 0);

		return factory.makeAppl(MINFUN, r, factory.makeInt(n), c);
	}

	final static public ATermAppl makeCard(final ATerm r, final int n, final ATerm c)
	{
		return makeDisplayCard(r, n, c);
		// ATermAppl max = makeMax( r, n, c );
		// if( n == 0 )
		// return max;
		//
		// ATermAppl min = makeMin( r, n, c );
		// return makeAnd( min, max );
	}

	final static public ATermAppl makeExactCard(final ATerm r, final int n, final ATerm c)
	{
		return makeExactCard(r, factory.makeInt(n), c);
	}

	final static public ATermAppl makeExactCard(final ATerm r, final ATermInt n, final ATerm c)
	{
		final ATermAppl max = makeMax(r, n, c);

		if (n.getInt() == 0)
			return max;

		final ATermAppl min = makeMin(r, n, c);
		return makeAnd(min, max);
	}

	final static public ATermAppl makeFacetRestriction(final ATermAppl facetName, final ATermAppl facetValue)
	{
		return factory.makeAppl(FACET, facetName, facetValue);
	}

	final static public ATermAppl makeRestrictedDatatype(final ATermAppl baseDatatype, final ATermAppl[] restrictions)
	{
		return factory.makeAppl(RESTRDATATYPEFUN, baseDatatype, makeList(restrictions));
	}

	final static public ATermAppl makeDatatypeDefinition(final ATermAppl datatype, final ATermAppl definition)
	{
		return factory.makeAppl(DATATYPEDEFFUN, datatype, definition);
	}

	final static public boolean isRestrictedDatatype(final ATermAppl term)
	{
		return term.getAFun().equals(RESTRDATATYPEFUN);
	}

	final static public ATermList makeList(final ATerm singleton)
	{
		return factory.makeList(singleton, EMPTY_LIST);
	}

	final static public ATermList makeList(final ATerm first, final ATermList rest)
	{
		return factory.makeList(first, rest);
	}

	public static ATermList makeList(final Collection<ATermAppl> set)
	{
		ATermList list = EMPTY_LIST;

		for (final ATerm term : set)
			list = list.insert(term);
		return list;
	}

	final static public ATermList makeList(final ATerm[] aTerms)
	{
		return makeList(aTerms, 0);
	}

	static private ATermList makeList(final ATerm[] aTerms, final int index)
	{
		if (index >= aTerms.length)
			return EMPTY_LIST;
		else
			if (index == aTerms.length - 1)
				return makeList(aTerms[index]);

		return makeList(aTerms[index], makeList(aTerms, index + 1));
	}

	final static public boolean member(final ATerm a, final ATermList list)
	{
		return list.indexOf(a, 0) != -1;
	}

	static public boolean isSet(final ATermList list)
	{
		if (list.isEmpty())
			return true;

		ATerm curr = list.getFirst();
		for (final ATerm term : list)
		{
			if (Comparators.termComparator.compare(curr, term) >= 0)
				return false;
			curr = term;
		}

		return true;
	}

	static public ATermList toSet(final ATermList list)
	{
		if (isSet(list))
			return list;

		final int size = list.getLength();

		ATerm[] a = toArray(list);
		if (a == null || a.length < size)
			a = new ATerm[Math.max(100, size)];

		Arrays.sort(a, 0, size, Comparators.termComparator);

		ATermList set = makeList(a[size - 1]);
		for (int i = size - 2; i >= 0; i--)
		{
			final ATerm s = set.getFirst();
			if (!s.equals(a[i]))
				set = set.insert(a[i]);
		}

		return set;
	}

	static public ATermList toSet(final ATerm[] a, final int size)
	{
		Arrays.sort(a, 0, size, Comparators.termComparator);

		ATermList set = makeList(a[size - 1]);
		for (int i = size - 2; i >= 0; i--)
		{
			final ATerm s = set.getFirst();
			if (!s.equals(a[i]))
				set = set.insert(a[i]);
		}

		return set;
	}

	static public ATermList toSet(final Collection<ATermAppl> terms)
	{
		final int size = terms.size();

		final ATermAppl[] a = new ATermAppl[size];
		terms.toArray(a);

		return toSet(a, size);
	}

	/**
	 * Return the string representations of the terms in a collection. For each element of the collection {@link #toString(ATermAppl)} function will be called
	 * to create the string representation.
	 *
	 * @param terms a collection of terms
	 * @return string representation of the terms
	 */
	public static String toString(final Collection<ATermAppl> terms)
	{
		if (terms.isEmpty())
			return "[]";

		final StringBuilder sb = new StringBuilder();
		sb.append("[");

		final Iterator<ATermAppl> i = terms.iterator();
		sb.append(toString(i.next()));
		while (i.hasNext())
		{
			sb.append(", ");
			sb.append(toString(i.next()));
		}

		sb.append("]");

		return sb.toString();
	}

	/**
	 * Return a string representation of the term which might be representing a named term, literal, variable or a complex concept expression. The URIs used in
	 * the term will be shortened into local names. The concept expressions are printed in NNF format.
	 *
	 * @param term term whose string representation we are creating
	 * @return string representation of the term
	 */
	public static String toString(final ATermAppl term)
	{
		return toString(term, true, true);
	}

	/**
	 * Return a string representation of the term which might be representing a named term, literal, variable or a complex concept expression. The URIs used in
	 * the term can be shortened into local names. The concept expressions can be printed in NNF format.
	 *
	 * @param term whose string representation we are creating
	 * @param printLocalName the name of the term
	 * @param printNNF true to use the NNF Format
	 * @return string representation of the term
	 */
	public static String toString(final ATermAppl term, final boolean printLocalName, final boolean printNNF)
	{
		if (term == null)
			return "<null>";
		else
		{
			final StringBuilder sb = new StringBuilder();
			toString(term, sb, printNNF ? Bool.FALSE : Bool.UNKNOWN, printLocalName);
			return sb.toString();
		}
	}

	/**
	 * Helper for toString function.
	 *
	 * @param term term
	 * @param sb the builder we are
	 * @param negated
	 * @param useLocalName
	 */
	private static void toString(final ATermAppl term, final StringBuilder sb, final Bool negated, final boolean printLocalName)
	{
		if (TOP.equals(term))
			sb.append(negated.isTrue() ? "owl:Nothing" : "owl:Thing");
		else
			if (BOTTOM.equals(term))
				sb.append(negated.isTrue() ? "owl:Thing" : "owl:Nothing");
			else
				if (isVar(term))
				{
					String name = ((ATermAppl) term.getArgument(0)).getName();
					if (printLocalName)
						name = URIUtils.getLocalName(name);
					sb.append("?").append(name);
				}
				else
					if (isLiteral(term))
					{
						final String value = ((ATermAppl) term.getArgument(0)).toString();
						final String lang = ((ATermAppl) term.getArgument(1)).getName();
						final ATermAppl datatypeURI = (ATermAppl) term.getArgument(2);

						sb.append('"').append(value).append('"');
						if (!"".equals(lang))
							sb.append('@').append(lang);
						else
							if (!datatypeURI.equals(NO_DATATYPE) && !datatypeURI.equals(PLAIN_LITERAL_DATATYPE))
							{
								sb.append("^^");
								toString(datatypeURI, sb, Bool.FALSE, printLocalName);
							}
					}
					else
						if (isPrimitive(term))
						{
							if (negated.isTrue())
								sb.append("not(");
							final String name = term.getName();
							sb.append(URIUtils.getLocalName(name));
							if (negated.isTrue())
								sb.append(")");
						}
						else
							if (isRestrictedDatatype(term))
							{
								if (negated.isTrue())
									sb.append("not(");
								toString((ATermAppl) term.getArgument(0), sb, Bool.FALSE, printLocalName);
								sb.append("[");
								ATermList list = (ATermList) term.getArgument(1);
								while (!list.isEmpty())
								{
									final ATermAppl facet = (ATermAppl) list.getFirst();
									sb.append(ATermManchesterSyntaxRenderer.FACETS.get(facet.getArgument(0)));
									sb.append(" ");
									toString((ATermAppl) facet.getArgument(1), sb, Bool.FALSE, printLocalName);
									list = list.getNext();
									if (!list.isEmpty())
										sb.append(", ");
								}
								sb.append("]");
								if (negated.isTrue())
									sb.append(")");
							}
							else
								if (negated.isKnown() && isNot(term))
									toString((ATermAppl) term.getArgument(0), sb, negated.not(), printLocalName);
								else
								{
									final AFun fun = term.getAFun();
									if (negated.isTrue())
									{
										if (ANDFUN.equals(fun))
											sb.append(ORFUN.getName());
										else
											if (ORFUN.equals(fun))
												sb.append(ANDFUN.getName());
											else
												if (SOMEFUN.equals(fun))
													sb.append(ALLFUN.getName());
												else
													if (ALLFUN.equals(fun))
														sb.append(SOMEFUN.getName());
													else
														if (MINFUN.equals(fun))
															sb.append(MAXFUN.getName());
														else
															if (MAXFUN.equals(fun))
																sb.append(MINFUN.getName());
															else
																if (!NOTFUN.equals(fun))
																{
																	if (VALUEFUN.equals(fun) || RESTRDATATYPEFUN.equals(fun))
																		sb.append("not(");
																	sb.append(fun.getName());
																}
									}
									else
										sb.append(fun.getName());

									Bool negatedRecurse = negated;
									if (negated.isKnown() && MINFUN.equals(fun) || MAXFUN.equals(fun))
										negatedRecurse = Bool.FALSE;
									else
										if (NOTFUN.equals(fun))
											negatedRecurse = negated.not();

									sb.append("(");
									for (int i = 0, n = term.getArity(); i < n; i++)
									{
										if (i > 0)
											sb.append(", ");
										final ATerm arg = term.getArgument(i);
										if (arg instanceof ATermAppl)
											toString((ATermAppl) arg, sb, i > 0 ? negatedRecurse : Bool.FALSE, printLocalName);
										else
											if (arg instanceof ATermList)
											{
												sb.append("[");
												ATermList list = (ATermList) arg;
												while (!list.isEmpty())
												{
													toString((ATermAppl) list.getFirst(), sb, negatedRecurse, printLocalName);
													list = list.getNext();
													if (!list.isEmpty())
														sb.append(", ");
												}
												sb.append("]");
											}
											else
											{
												int value = ((ATermInt) arg).getInt();
												if (negated.isTrue())
													if (MINFUN.equals(fun))
														value--;
													else
														if (MAXFUN.equals(fun))
															value++;
												sb.append(value);
											}
									}
									sb.append(")");
									if ((VALUEFUN.equals(fun) || RESTRDATATYPEFUN.equals(fun)) && negated.isTrue())
										sb.append(")");
								}
	}

	static public ATermAppl[] toArray(final ATermList list)
	{
		final ATermAppl[] a = new ATermAppl[list.getLength()];

		int i = 0;
		for (final ATerm term : list)
		{
			a[i] = (ATermAppl) term;
			i++;
		}

		return a;
	}

	public final static void assertTrue(final boolean condition)
	{
		if (!condition)
			throw new OpenError("assertion failed.");
	}

	public static final boolean isPrimitive(final ATermAppl c)
	{
		return c.getArity() == 0;
	}

	public static final boolean isNegatedPrimitive(final ATermAppl c)
	{
		return isNot(c) && isPrimitive((ATermAppl) c.getArgument(0));
	}

	public static final boolean isPrimitiveOrNegated(final ATermAppl c)
	{
		return isPrimitive(c) || isNegatedPrimitive(c);
	}

	public static final boolean isBnode(final ATermAppl name)
	{
		return BNODE_FUN.equals(name.getAFun());
	}

	public static final boolean isAnon(final ATermAppl term)
	{
		return ANON_FUN.equals(term.getAFun());
	}

	public static final boolean isBuiltinProperty(final ATermAppl name)
	{
		return TOP_OBJECT_PROPERTY.equals(name)//
				|| BOTTOM_OBJECT_PROPERTY.equals(name)//
				|| makeInv(TOP_OBJECT_PROPERTY).equals(name)//
				|| makeInv(BOTTOM_OBJECT_PROPERTY).equals(name)//
				|| TOP_DATA_PROPERTY.equals(name)//
				|| BOTTOM_DATA_PROPERTY.equals(name);
	}

	public static Set<ATermAppl> listToSet(final ATermList list)
	{
		final Set<ATermAppl> set = new HashSet<>();
		for (final ATerm term : list)
			set.add((ATermAppl) term);
		return set;
	}

	public static Set<ATermAppl> getPrimitives(final ATermList list)
	{
		final Set<ATermAppl> set = new HashSet<>();
		for (final ATerm term : list)
		{
			final ATermAppl appl = (ATermAppl) term;
			if (isPrimitive(appl))
				set.add(appl);
		}

		return set;
	}

	public final static ATermAppl getTop(final Role r)
	{
		return r.isDatatypeRole() ? TOP_LIT : TOP;
	}

	public final static boolean isTop(final ATermAppl a)
	{
		return TOP.equals(a) || TOP_LIT.equals(a);
	}

	public final static boolean isBottom(final ATermAppl a)
	{
		return BOTTOM.equals(a) || BOTTOM_LIT.equals(a);
	}

	final static public boolean isInv(final ATermAppl r)
	{
		return INVFUN.equals(r.getAFun());
	}

	public final static boolean isAnd(final ATermAppl a)
	{
		return ANDFUN.equals(a.getAFun());
	}

	public final static boolean isOr(final ATermAppl a)
	{
		return ORFUN.equals(a.getAFun());
	}

	public final static boolean isAllValues(final ATermAppl a)
	{
		return ALLFUN.equals(a.getAFun());
	}

	public final static boolean isSomeValues(final ATermAppl a)
	{
		return SOMEFUN.equals(a.getAFun());
	}

	public final static boolean isSelf(final ATermAppl a)
	{
		return SELFFUN.equals(a.getAFun());
	}

	public final static boolean isHasValue(final ATermAppl a)
	{
		return SOMEFUN.equals(a.getAFun()) && VALUEFUN.equals(((ATermAppl) a.getArgument(1)).getAFun());
	}

	public final static boolean isNominal(final ATermAppl a)
	{
		return VALUEFUN.equals(a.getAFun());
	}

	public final static boolean isOneOf(final ATermAppl a)
	{
		if (!ORFUN.equals(a.getAFun()))
			return false;

		ATermList list = (ATermList) a.getArgument(0);
		while (!list.isEmpty())
		{
			if (!isNominal((ATermAppl) list.getFirst()))
				return false;
			list = list.getNext();
		}
		return true;
	}

	public final static boolean isDataRange(final ATermAppl a)
	{
		if (!ORFUN.equals(a.getAFun()))
			return false;

		ATermList list = (ATermList) a.getArgument(0);
		while (!list.isEmpty())
		{
			final ATermAppl term = (ATermAppl) list.getFirst();
			if (!isNominal(term) || !isLiteral((ATermAppl) term.getArgument(0)))
				return false;
			list = list.getNext();
		}
		return true;
	}

	public final static boolean isNot(final ATermAppl a)
	{
		return NOTFUN.equals(a.getAFun());
	}

	public final static boolean isMax(final ATermAppl a)
	{
		return MAXFUN.equals(a.getAFun());
	}

	public final static boolean isMin(final ATermAppl a)
	{
		return MINFUN.equals(a.getAFun());
	}

	public final static boolean isCard(final ATermAppl a)
	{
		if (isMin(a) || isMax(a))
			return true;
		else
			if (isAnd(a))
			{
				final ATermAppl arg0 = (ATermAppl) a.getArgument(0);
				return isMin(arg0) || isMax(arg0);
			}

		return false;
	}

	public final static boolean isLiteral(final ATermAppl a)
	{
		return LITFUN.equals(a.getAFun());
	}

	final static public boolean isVar(final ATermAppl a)
	{
		return VARFUN.equals(a.getAFun());
	}

	final static public boolean isTransitiveChain(final ATermList chain, final ATerm r)
	{
		return chain.getLength() == 2 && chain.getFirst().equals(r) && chain.getLast().equals(r);
	}

	public static boolean isComplexClass(final ATerm c)
	{
		if (c instanceof ATermAppl)
		{
			final ATermAppl a = (ATermAppl) c;
			final AFun f = a.getAFun();
			return CLASS_FUN.contains(f);
		}
		return false;
	}

	public final static boolean isPropertyAssertion(final ATermAppl a)
	{
		return PROPFUN.equals(a.getAFun());
	}

	public final static boolean isTypeAssertion(final ATermAppl a)
	{
		return TYPEFUN.equals(a.getAFun());
	}

	public static ATerm nnf(final ATerm term)
	{
		if (term instanceof ATermList)
			return nnf((ATermList) term);
		if (term instanceof ATermAppl)
			return nnf((ATermAppl) term);

		return null;
	}

	public static ATermList nnf(final ATermList list)
	{
		ATermList newList = factory.makeList();

		for (final ATerm term : list)
			newList = newList.append(nnf((ATermAppl) term));

		return newList;
	}

	/*
	 * return the term in NNF form, i.e. negation only occurs in front of atomic
	 * concepts
	 */
	public static ATermAppl nnf(final ATermAppl term)
	{
		ATermAppl newterm = null;

		AFun af = term.getAFun();

		if (af.equals(NOTFUN))
		{ // Function is a NOT
			// Take the first argument to the NOT, then check
			// the type of that argument to determine what needs to be done.
			assertTrue(af.getArity() == 1);
			final ATermAppl arg = (ATermAppl) term.getArgument(0);
			af = arg.getAFun();

			if (arg.getArity() == 0)
				newterm = term; // Negation is in as far as it can go
			else
				if (NOTFUN.equals(af))
					newterm = nnf((ATermAppl) arg.getArgument(0));
				else
					if (VALUEFUN.equals(af) || SELFFUN.equals(af) || RESTRDATATYPEFUN.equals(af))
						newterm = term;
					else
						if (MAXFUN.equals(af))
						{
							final ATermInt n = (ATermInt) arg.getArgument(1);
							newterm = makeMin(arg.getArgument(0), n.getInt() + 1, nnf(arg.getArgument(2)));
						}
						else
							if (MINFUN.equals(af))
							{
								final ATermInt n = (ATermInt) arg.getArgument(1);
								if (n.getInt() == 0)
									newterm = BOTTOM;
								else
									newterm = makeMax(arg.getArgument(0), n.getInt() - 1, nnf(arg.getArgument(2)));
							}
							else
								if (CARDFUN.equals(af))
									newterm = nnf(makeNot(makeExactCard(arg.getArgument(0), (ATermInt) arg.getArgument(1), arg.getArgument(2))));
								else
									if (ANDFUN.equals(af))
										newterm = makeOr(nnf(negate((ATermList) arg.getArgument(0))));
									else
										if (ORFUN.equals(af))
											newterm = makeAnd(nnf(negate((ATermList) arg.getArgument(0))));
										else
											if (SOMEFUN.equals(af))
											{
												final ATerm p = arg.getArgument(0);
												final ATerm c = arg.getArgument(1);
												newterm = makeAllValues(p, nnf(makeNot(c)));
											}
											else
												if (ALLFUN.equals(af))
												{
													final ATerm p = arg.getArgument(0);
													final ATerm c = arg.getArgument(1);
													newterm = makeSomeValues(p, nnf(makeNot(c)));
												}
												else
													throw new InternalReasonerException("Unknown term type: " + term);
		}
		else
			if (MINFUN.equals(af) || MAXFUN.equals(af) || SELFFUN.equals(af))
				newterm = term;
			else
				if (CARDFUN.equals(af))
					newterm = nnf(makeExactCard(term.getArgument(0), (ATermInt) term.getArgument(1), term.getArgument(2)));
				else
				{
					// Return the term with all of its arguments in nnf
					final ATerm args[] = new ATerm[term.getArity()];
					for (int i = 0; i < term.getArity(); i++)
						args[i] = nnf(term.getArgument(i));
					newterm = factory.makeAppl(af, args);
				}

		assertTrue(newterm != null);

		return newterm;
	}

	public static Collection<ATermAppl> normalize(final Collection<ATermAppl> terms)
	{
		final List<ATermAppl> list = new ArrayList<>();
		for (final ATermAppl term : terms)
			list.add(normalize(term));

		return list;
	}

	public static ATermList normalize(final ATermList list)
	{
		final int size = list.getLength();
		final ATerm[] terms = new ATerm[size];
		int i = 0;
		for (final ATerm term : list)
		{
			terms[i] = normalize((ATermAppl) term);
			i++;
		}

		return toSet(terms, size);
	}

	/**
	 * Normalize the term by making following changes:
	 * <ul>
	 * <li>or([a1, a2,..., an]) -> not(and[not(a1), not(a2), ..., not(an)]])</li>
	 * <li>some(p, c) -> all(p, not(c))</li>
	 * <li>max(p, n) -> not(min(p, n+1))</li>
	 * </ul>
	 *
	 * @param term
	 * @return the term normalized
	 */
	public static ATermAppl normalize(final ATermAppl term)
	{
		ATermAppl norm = term;
		final AFun fun = term.getAFun();
		final ATerm arg1 = term.getArity() > 0 ? term.getArgument(0) : null;
		final ATerm arg2 = term.getArity() > 1 ? term.getArgument(1) : null;
		final ATerm arg3 = term.getArity() > 2 ? term.getArgument(2) : null;

		if (arg1 == null || SELFFUN.equals(fun) || VALUEFUN.equals(fun) || INVFUN.equals(fun) || RESTRDATATYPEFUN.equals(fun))
		{
			// do nothing because these terms cannot be decomposed any further
		}
		else
			if (NOTFUN.equals(fun))
			{
				if (!isPrimitive((ATermAppl) arg1))
					norm = simplify(makeNot(normalize((ATermAppl) arg1)));
			}
			else
				if (ANDFUN.equals(fun))
					norm = simplify(makeAnd(normalize((ATermList) arg1)));
				else
					if (ORFUN.equals(fun))
					{
						final ATermList neg = negate((ATermList) arg1);
						final ATermAppl and = makeAnd(neg);
						final ATermAppl notAnd = makeNot(and);
						norm = normalize(notAnd);
					}
					else
						if (ALLFUN.equals(fun))
							norm = simplify(makeAllValues(arg1, normalize((ATermAppl) arg2)));
						else
							if (SOMEFUN.equals(fun))
								norm = normalize(makeNot(makeAllValues(arg1, makeNot(arg2))));
							else
								if (MAXFUN.equals(fun) && arg2 != null)
									norm = normalize(makeNot(makeMin(arg1, ((ATermInt) arg2).getInt() + 1, arg3)));
								else
									if (MINFUN.equals(fun))
										norm = simplify(makeMin(arg1, (ATermInt) arg2, normalize((ATermAppl) arg3)));
									else
										if (CARDFUN.equals(fun) && arg2 != null)
										{
											final ATermAppl normMin = simplify(makeMin(arg1, ((ATermInt) arg2).getInt(), normalize((ATermAppl) arg3)));
											final ATermAppl normMax = normalize(makeMax(arg1, ((ATermInt) arg2).getInt(), arg3));
											norm = simplify(makeAnd(normMin, normMax));
										}
										else
											throw new InternalReasonerException("Unknown concept type: " + term);

		return norm;
	}

	/**
	 * Simplify the term by making following changes:
	 * <ul>
	 * <li>and([]) -> TOP</li>
	 * <li>all(p, TOP) -> TOP</li>
	 * <li>min(p, 0) -> TOP</li>
	 * <li>and([a1, and([a2,...,an])]) -> and([a1, a2, ..., an]))</li>
	 * <li>and([a, not(a), ...]) -> BOTTOM</li>
	 * <li>not(C) -> not(simplify(C))</li>
	 * </ul>
	 *
	 * @param term
	 * @return the term simplified
	 */
	public static ATermAppl simplify(final ATermAppl term)
	{
		ATermAppl simp = term;
		final AFun fun = term.getAFun();
		final ATerm arg1 = term.getArity() > 0 ? term.getArgument(0) : null;
		final ATerm arg2 = term.getArity() > 1 ? term.getArgument(1) : null;
		final ATerm arg3 = term.getArity() > 2 ? term.getArgument(2) : null;

		if (arg1 == null || SELFFUN.equals(fun) || VALUEFUN.equals(fun) || RESTRDATATYPEFUN.equals(fun))
		{
			// do nothing because term is primitive or self restriction
		}
		else
			if (NOTFUN.equals(fun))
			{
				final ATermAppl arg = (ATermAppl) arg1;
				if (isNot(arg))
					simp = simplify((ATermAppl) arg.getArgument(0));
				else
					if (isMin(arg))
					{
						final ATermInt n = (ATermInt) arg.getArgument(1);
						if (n.getInt() == 0)
							simp = BOTTOM;
					}
			}
			else
				if (ANDFUN.equals(fun))
				{
					final ATermList conjuncts = (ATermList) arg1;
					if (conjuncts.isEmpty())
						simp = TOP;
					else
					{
						final Set<ATermAppl> set = new HashSet<>();
						final List<ATermAppl> negations = new ArrayList<>();
						final MultiListIterator i = new MultiListIterator(conjuncts);
						while (i.hasNext())
						{
							final ATermAppl c = i.next();
							if (TOP.equals(c))
								continue;
							else
								if (BOTTOM.equals(c))
									return BOTTOM;
								else
									if (isAnd(c))
										i.append((ATermList) c.getArgument(0));
									else
										if (isNot(c))
											negations.add(c);
										else
											set.add(c);
						}

						for (final ATermAppl notC : negations)
						{
							final ATermAppl c = (ATermAppl) notC.getArgument(0);
							if (set.contains(c))
								return BOTTOM;
						}

						if (set.isEmpty())
						{
							if (negations.isEmpty())
								return TOP;
							else
								if (negations.size() == 1)
									return negations.get(0);
						}
						else
							if (set.size() == 1 && negations.isEmpty())
								return set.iterator().next();

						negations.addAll(set);
						final int size = negations.size();
						final ATermAppl[] terms = new ATermAppl[size];
						negations.toArray(terms);
						simp = makeAnd(toSet(terms, size));
					}
				}
				else
					if (ALLFUN.equals(fun))
					{
						if (arg2 != null && arg2.equals(TOP))
							simp = TOP;
					}
					else
						if (MINFUN.equals(fun))
						{
							final ATermInt n = (ATermInt) arg2;
							if (n != null && n.getInt() == 0)
								simp = TOP;

							if (arg3 != null && arg3.equals(BOTTOM))
								simp = BOTTOM;
						}
						else
							if (MAXFUN.equals(fun))
							{
								final ATermInt n = (ATermInt) arg2;
								if (n != null && arg3 != null && n.getInt() > 0 && arg3.equals(BOTTOM))
									simp = TOP;
							}
							else
								throw new InternalReasonerException("Unknown term type: " + term);

		return simp;
	}

	/**
	 * @param conjuncts
	 * @return a simplified and assuming that all the elements have already been normalized.
	 */
	public static ATermAppl makeSimplifiedAnd(final Collection<ATermAppl> conjuncts)
	{
		final Set<ATermAppl> set = new HashSet<>();
		final List<ATermAppl> negations = new ArrayList<>();
		final MultiListIterator listIt = new MultiListIterator(EMPTY_LIST);
		final Iterator<ATermAppl> i = new PairIterator<>(conjuncts.iterator(), listIt);
		while (i.hasNext())
		{
			final ATermAppl c = i.next();
			if (TOP.equals(c))
				continue;
			else
				if (BOTTOM.equals(c))
					return BOTTOM;
				else
					if (isAnd(c))
						listIt.append((ATermList) c.getArgument(0));
					else
						if (isNot(c))
							negations.add(c);
						else
							set.add(c);
		}

		for (final ATermAppl notC : negations)
		{
			final ATermAppl c = (ATermAppl) notC.getArgument(0);
			if (set.contains(c))
				return BOTTOM;
		}

		if (set.isEmpty())
		{
			if (negations.isEmpty())
				return TOP;
			else
				if (negations.size() == 1)
					return negations.get(0);
		}
		else
			if (set.size() == 1 && negations.isEmpty())
				return set.iterator().next();

		negations.addAll(set);
		final int size = negations.size();
		final ATermAppl[] terms = new ATermAppl[size];
		negations.toArray(terms);
		return makeAnd(toSet(terms, size));
	}

	public static Set<ATermAppl> findPrimitives(final ATermAppl term)
	{
		final Set<ATermAppl> primitives = new HashSet<>();

		findPrimitives(term, primitives, false, false);
		return primitives;
	}

	public static Set<ATermAppl> findPrimitives(final ATermAppl term, final boolean skipRestrictions, final boolean skipTopLevel)
	{
		final Set<ATermAppl> primitives = new HashSet<>();

		findPrimitives(term, primitives, skipRestrictions, skipTopLevel);

		return primitives;
	}

	public static void findPrimitives(final ATermAppl term, final Set<ATermAppl> primitives)
	{
		findPrimitives(term, primitives, false, false);
	}

	public static void findPrimitives(final ATermAppl term, final Set<ATermAppl> primitives, final boolean skipRestrictions, final boolean skipTopLevel)
	{
		final AFun fun = term.getAFun();

		if (isPrimitive(term))
			primitives.add(term);
		else
			if (SELFFUN.equals(fun) || VALUEFUN.equals(fun) || RESTRDATATYPEFUN.equals(fun))
			{
				// do nothing because there is no atomic concept here
			}
			else
				if (NOTFUN.equals(fun))
				{
					final ATermAppl arg = (ATermAppl) term.getArgument(0);
					if (!isPrimitive(arg) || !skipTopLevel)
						findPrimitives(arg, primitives, skipRestrictions, false);
				}
				else
					if (ANDFUN.equals(fun) || ORFUN.equals(fun))
					{
						ATermList list = (ATermList) term.getArgument(0);
						while (!list.isEmpty())
						{
							final ATermAppl arg = (ATermAppl) list.getFirst();
							if (!isNegatedPrimitive(arg) || !skipTopLevel)
								findPrimitives(arg, primitives, skipRestrictions, false);
							list = list.getNext();
						}
					}
					else
						if (!skipRestrictions)
							if (ALLFUN.equals(fun) || SOMEFUN.equals(fun))
							{
								final ATermAppl arg = (ATermAppl) term.getArgument(1);
								findPrimitives(arg, primitives, skipRestrictions, false);
							}
							else
								if (MAXFUN.equals(fun) || MINFUN.equals(fun) || CARDFUN.equals(fun))
								{
									final ATermAppl arg = (ATermAppl) term.getArgument(2);
									findPrimitives(arg, primitives, skipRestrictions, false);
								}
								else
									throw new InternalReasonerException("Unknown concept type: " + term);
	}

	public static Collection<ATermAppl> primitiveOrBottom(final Collection<ATermAppl> collection)
	{
		return collection.stream().filter(a -> isPrimitive(a) || a == BOTTOM).collect(Collectors.toList());
	}

	public static Set<ATermAppl> primitiveOrBottom(final Set<ATermAppl> collection)
	{
		return collection.stream().filter(a -> isPrimitive(a) || a == BOTTOM).collect(Collectors.toSet());
	}

	public static ATermAppl makeRule(final ATermAppl[] head, final ATermAppl[] body)
	{
		return makeRule(null, head, body);
	}

	public static ATermAppl makeRule(final ATermAppl name, final ATermAppl[] head, final ATermAppl[] body)
	{
		return factory.makeAppl(RULEFUN, name == null ? EMPTY : name, makeList(head), makeList(body));
	}

	public static ATermAppl makeBuiltinAtom(final ATermAppl[] args)
	{
		return factory.makeAppl(BUILTINFUN, makeList(args));
	}
}
