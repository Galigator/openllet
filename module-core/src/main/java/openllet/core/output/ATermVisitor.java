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

package openllet.core.output;

import openllet.aterm.ATermAppl;
import openllet.aterm.ATermList;

/**
 * A visitor interface specifically designed for structures in Pellet. Since the class descriptions in Pellet are all represented as ATermAppl's with different
 * functors any output (or conversion) function will need to check functors in _order to create a result. This interface defines the functions for each
 * different construct to make this process easier. A better implementation would actually integrate this functionality with the jjtraveler.Visitable interface.
 *
 * @author Evren Sirin
 */
public interface ATermVisitor
{
	/**
	 * Visit a generic term which may be a class expression, _individual or a literal.
	 *
	 * @param term
	 */
	void visit(ATermAppl term);

	/**
	 * Visit a primitive term (with no arguments) that stands for a URI. This URI may belong to a class, a property, an _individual or a datatype.
	 *
	 * @param term
	 */
	void visitTerm(ATermAppl term);

	/**
	 * Visit the 'and' (intersectionOf) term.
	 *
	 * @param term
	 */
	void visitAnd(ATermAppl term);

	/**
	 * Visit the 'or' (unionOf) term.
	 *
	 * @param term
	 */
	void visitOr(ATermAppl term);

	/**
	 * Visit the 'not' (complementOf) term.
	 *
	 * @param term
	 */
	void visitNot(ATermAppl term);

	/**
	 * Visit the 'some' (someValuesFrom restriction) term.
	 *
	 * @param term
	 */
	default void visitSome(final ATermAppl term)
	{
		//
	}

	/**
	 * Visit the 'all' (allValuesFrom restriction) term.
	 *
	 * @param term
	 */
	default void visitAll(final ATermAppl term)
	{
		// empty
	}

	/**
	 * Visit the 'min' (minCardinality restriction) term.
	 *
	 * @param term
	 */
	default void visitMin(final ATermAppl term)
	{
		// empty
	}

	/**
	 * Visit the 'card' (minCardinality restriction) term. This is not a standard term that ise used inside the reasoner but sometimes used for display
	 * purposes. Normally, cardinality restrictions would be stored as a conjunction of min and max restrictions.
	 *
	 * @param term
	 */
	default void visitCard(final ATermAppl term)
	{
		// empty
	}

	/**
	 * Visit the 'max' (maxCardinality restriction) term.
	 *
	 * @param term
	 */
	default void visitMax(final ATermAppl term)
	{
		// empty
	}

	/**
	 * Visit the hasValue restriction term. This term is in the form some(property,value(_individual)) or some(property,value(literal))
	 *
	 * @param term
	 */
	default void visitHasValue(final ATermAppl term)
	{
		// empty
	}

	/**
	 * Visit the nominal term. This term is in the form some(property,value(_individual))
	 *
	 * @param term
	 */
	void visitValue(ATermAppl term);

	/**
	 * Visit the 'oneOf' term. This term is in the form or([value(i1),value(i2),...,value(i3)] where i's are individuals or literal constants
	 *
	 * @param term
	 */
	void visitOneOf(ATermAppl term);

	/**
	 * Visit the literal term. The literals are in the form literal(lexicalValue, language, datatypeURI)
	 *
	 * @param term
	 */
	default void visitLiteral(final ATermAppl term)
	{
		// empty
	}

	/**
	 * Visit the list structure. Lists are found in 'and' and 'or' terms.
	 *
	 * @param term
	 */
	void visitList(ATermList term);

	/**
	 * Visit the self restriction term. This is in the form self(p).
	 *
	 * @param term
	 */
	default void visitSelf(final ATermAppl term)
	{
		// empty
	}

	/**
	 * @param p inverse
	 */
	default void visitInverse(final ATermAppl p)
	{
		// empty
	}

	void visitRestrictedDatatype(ATermAppl dt);
}
