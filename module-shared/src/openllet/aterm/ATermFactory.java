/*
 * Copyright (c) 2002-2007, CWI and INRIA
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of California, Berkeley nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package openllet.aterm;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import openllet.aterm.pure.owl.FunAll;
import openllet.aterm.pure.owl.FunAllDifferent;
import openllet.aterm.pure.owl.FunAnd;
import openllet.aterm.pure.owl.FunAsymmetric;
import openllet.aterm.pure.owl.FunBottomDataProperty;
import openllet.aterm.pure.owl.FunBottomObjectProperty;
import openllet.aterm.pure.owl.FunBuiltin;
import openllet.aterm.pure.owl.FunCard;
import openllet.aterm.pure.owl.FunComplementOf;
import openllet.aterm.pure.owl.FunDatatypeDefinition;
import openllet.aterm.pure.owl.FunDifferent;
import openllet.aterm.pure.owl.FunDisjointClasses;
import openllet.aterm.pure.owl.FunDisjointProperties;
import openllet.aterm.pure.owl.FunDisjointPropertyWith;
import openllet.aterm.pure.owl.FunDisjointWith;
import openllet.aterm.pure.owl.FunDomain;
import openllet.aterm.pure.owl.FunEmpty;
import openllet.aterm.pure.owl.FunEquivalentClasses;
import openllet.aterm.pure.owl.FunEquivalentProperty;
import openllet.aterm.pure.owl.FunFacet;
import openllet.aterm.pure.owl.FunFunctionnal;
import openllet.aterm.pure.owl.FunInv;
import openllet.aterm.pure.owl.FunInverseFunctional;
import openllet.aterm.pure.owl.FunInverseProperty;
import openllet.aterm.pure.owl.FunIrreflexive;
import openllet.aterm.pure.owl.FunLiteral;
import openllet.aterm.pure.owl.FunMax;
import openllet.aterm.pure.owl.FunMin;
import openllet.aterm.pure.owl.FunNot;
import openllet.aterm.pure.owl.FunOr;
import openllet.aterm.pure.owl.FunProp;
import openllet.aterm.pure.owl.FunRange;
import openllet.aterm.pure.owl.FunReflexive;
import openllet.aterm.pure.owl.FunRestrictedDatatype;
import openllet.aterm.pure.owl.FunRule;
import openllet.aterm.pure.owl.FunSameAs;
import openllet.aterm.pure.owl.FunSelf;
import openllet.aterm.pure.owl.FunSome;
import openllet.aterm.pure.owl.FunSubClassOf;
import openllet.aterm.pure.owl.FunSubProperty;
import openllet.aterm.pure.owl.FunSymmetric;
import openllet.aterm.pure.owl.FunTop;
import openllet.aterm.pure.owl.FunTopDataProperty;
import openllet.aterm.pure.owl.FunTopObjectProperty;
import openllet.aterm.pure.owl.FunTransitive;
import openllet.aterm.pure.owl.FunType;
import openllet.aterm.pure.owl.FunValue;
import openllet.aterm.pure.owl.FunVar;

/**
 * An ATermFactory is responsible for making new ATerms.
 * A factory can create a new ATerm by parsing a String, by making
 * it via one of the many "make" methods, or by reading it from an
 * InputStream.
 *
 * @author Hayco de Jong (jong@cwi.nl)
 * @author Pieter Olivier (olivierp@cwi.nl)
 */
public interface ATermFactory
{
	byte START_OF_SHARED_TEXT_FILE = (byte) '!';

	/**
	 * Creates a new ATerm by parsing a string.
	 *
	 * @param  trm the string representation of the term
	 *
	 * @return     the parsed term.
	 *
	 * @see        #make(String)
	 */
	ATerm parse(String trm);

	/**
	 * Equivalent of parse.
	 *
	 * @param  trm the string representation of the term
	 *
	 * @return     the parsed term.
	 *
	 * @see        #parse(String)
	 */
	ATerm make(String trm);

	/**
	 * Creates a new ATerm given a string pattern and a list of arguments.
	 * First the string pattern is parsed into an ATerm.
	 * Then the holes in the pattern are filled with arguments taken from
	 * the supplied list of arguments.
	 *
	 * @param  pattern the string pattern containing a placeholder for each
	 *                 argument.
	 * @param  args    the list of arguments to be filled into the placeholders.
	 *
	 * @return         the constructed term.
	 */
	ATerm make(String pattern, List<Object> args);

	/**
	 * Creates a new ATerm given a pattern and a list of arguments.
	 * The holes in the pattern are filled with arguments taken from
	 * the supplied list of arguments.
	 *
	 * @param  pattern the pattern containing a placeholder for each argument.
	 * @param  args    the list of arguments to be filled into the placeholders.
	 *
	 * @return         the constructed term.
	 */
	ATerm make(ATerm pattern, List<Object> args);

	/**
	 * Creates a new ATerm given a pattern and a single argument.
	 * This convenience method creates an ATerm from a pattern and one
	 * argument.
	 *
	 * @param  pattern the pattern containing a placeholder for the argument.
	 * @param  arg1    the argument to be filled into the hole.
	 *
	 * @return         the constructed term.
	 */
	ATerm make(String pattern, Object arg1);

	/**
	 * Creates a new ATerm given a pattern and a fixed number of arguments.
	 * This convenience method creates an ATerm from a pattern and two
	 * arguments.
	 *
	 * @param  pattern the pattern containing a placeholder for the arguments.
	 * @param  arg1    the argument to be filled into the first hole.
	 * @param  arg2    the argument to be filled into the second hole.
	 *
	 * @return         the constructed term.
	 */
	ATerm make(String pattern, Object arg1, Object arg2);

	/**
	 * Creates a new ATerm given a pattern and a fixed number of arguments.
	 * This convenience method creates an ATerm from a pattern and three
	 * arguments.
	 *
	 * @param  pattern the pattern containing a placeholder for the arguments.
	 * @param  arg1    the argument to be filled into the first hole.
	 * @param  arg2    the argument to be filled into the second hole.
	 * @param  arg3    the argument to be filled into the third hole.
	 *
	 * @return         the constructed term.
	 */
	ATerm make(String pattern, Object arg1, Object arg2, Object arg3);

	/**
	 * Creates a new ATermInt object
	 *
	 * @param  val the integer value to be stored.
	 *
	 * @return     the constructed ATermInt object.
	 */
	ATermInt makeInt(int val);

	/**
	 * Creates a new ATermLong object
	 *
	 * @param  val the long value to be stored.
	 *
	 * @return     the constructed ATermLong object.
	 */
	ATermLong makeLong(long val);

	/**
	 * Creates a new ATermReal object
	 *
	 * @param  val the double value to be stored.
	 *
	 * @return     the constructed ATermReal object.
	 */
	ATermReal makeReal(double val);

	/**
	 * Creates an empty ATermList object
	 *
	 * @return the (empty) ATermList.
	 */
	ATermList makeList();

	/**
	 * Creates a singleton ATermList object.
	 *
	 * @param  single the element to be placed in the list.
	 *
	 * @return        the singleton ATermList object.
	 */
	ATermList makeList(ATerm single);

	/**
	 * Creates a head-tail style ATermList.
	 *
	 * @param  head the head of the list.
	 * @param  tail the tail of the list.
	 *
	 * @return      the constructed ATermList.
	 */
	ATermList makeList(ATerm head, ATermList tail);

	/**
	 * Creates an ATermPlaceholder object.
	 *
	 * @param  type the type of the hole in the placeholder.
	 *
	 * @return      the constructed ATermPlaceholder.
	 */
	ATermPlaceholder makePlaceholder(ATerm type);

	/**
	 * Creates an ATermBlob (Binary Large OBject).
	 *
	 * @param  data the data to be stored in the blob.
	 *
	 * @return      the constructed ATermBlob.
	 */
	ATermBlob makeBlob(byte[] data);

	/**
	 * Creates an AFun object
	 *
	 * @param  name     the name of the function symbol.
	 * @param  arity    the arity of the function symbol.
	 * @param  isQuoted whether the function symbol is quoted ("foo") or not (foo).
	 *
	 * @return          the constructed AFun.
	 */
	AFun makeAFun(String name, int arity, boolean isQuoted);

	/**
	 * Creates a function application.
	 *
	 * @param  fun the function symbol of the application.
	 *
	 * @return     the constructed function application.
	 */
	ATermAppl makeAppl(AFun fun);

	/**
	 * Creates a function application.
	 *
	 * @param  fun the function symbol of the application.
	 * @param  arg the argument of the application.
	 *
	 * @return     the constructed function application.
	 */
	ATermAppl makeAppl(AFun fun, ATerm arg);

	/**
	 * Creates a function application.
	 *
	 * @param  fun  the function symbol of the application.
	 * @param  arg1 the first argument of the application.
	 * @param  arg2 the second argument of the application.
	 *
	 * @return      the constructed function application.
	 */
	ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2);

	/**
	 * Creates a function application.
	 *
	 * @param  fun  the function symbol of the application.
	 * @param  arg1 the first argument of the application.
	 * @param  arg2 the second argument of the application.
	 * @param  arg3 the third argument of the application.
	 *
	 * @return      the constructed function application.
	 */
	ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2, ATerm arg3);

	/**
	 * Creates a function application.
	 *
	 * @param  fun  the function symbol of the application.
	 * @param  arg1 the first argument of the application.
	 * @param  arg2 the second argument of the application.
	 * @param  arg3 the third argument of the application.
	 * @param  arg4 the fourth argument of the application.
	 *
	 * @return      the constructed function application.
	 */
	ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2, ATerm arg3, ATerm arg4);

	/**
	 * Creates a function application.
	 *
	 * @param  fun  the function symbol of the application.
	 * @param  arg1 the first argument of the application.
	 * @param  arg2 the second argument of the application.
	 * @param  arg3 the third argument of the application.
	 * @param  arg4 the fourth argument of the application.
	 * @param  arg5 the fifth argument of the application.
	 *
	 * @return      the constructed function application.
	 */
	ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2, ATerm arg3, ATerm arg4, ATerm arg5);

	/**
	 * Creates a function application.
	 *
	 * @param  fun  the function symbol of the application.
	 * @param  arg1 the first argument of the application.
	 * @param  arg2 the second argument of the application.
	 * @param  arg3 the third argument of the application.
	 * @param  arg4 the fourth argument of the application.
	 * @param  arg5 the fifth argument of the application.
	 * @param  arg6 the sixth argument of the application.
	 *
	 * @return      the constructed function application.
	 */
	ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2, ATerm arg3, ATerm arg4, ATerm arg5, ATerm arg6);

	/**
	 * Creates a function application.
	 *
	 * @param  fun  the function symbol of the application.
	 * @param  args an array containing the arguments.
	 *
	 * @return      the constructed function application.
	 */
	ATermAppl makeAppl(AFun fun, ATerm[] args);

	/**
	 * Creates a function application.
	 *
	 * @param  fun  the function symbol of the application.
	 * @param  args an ATermList containing the arguments.
	 *
	 * @return      the constructed function application.
	 */
	ATermAppl makeApplList(AFun fun, ATermList args);

	FunLiteral literal();

	FunAnd and();

	FunOr or();

	FunSome some();

	FunAll all();

	FunNot not();

	FunMax max();

	FunMin min();

	FunValue value();

	FunSelf self();

	FunCard card();

	FunInv inv();

	FunSubClassOf subClassOf();

	FunEquivalentClasses equivalentClasses();

	FunSameAs sameAs();

	FunDisjointWith disjointWith();

	FunDisjointClasses disjointClasses();

	FunDisjointPropertyWith disjointPropertyWith();

	FunDisjointProperties disjointProperties();

	FunComplementOf complementOf();

	FunVar var();

	FunType type();

	FunProp prop();

	FunDifferent different();

	FunAllDifferent allDifferent();

	FunAsymmetric asymmetric();

	FunFunctionnal functional();

	FunInverseFunctional inverseFunctional();

	FunIrreflexive irreflexive();

	FunReflexive reflexive();

	FunSymmetric symmetric();

	FunTransitive transitive();

	FunSubProperty subProperty();

	FunEquivalentProperty equivalentProperty();

	FunInverseProperty inverseProperty();

	FunDomain domain();

	FunRange range();

	FunRule rule();

	FunBuiltin builtin();

	FunDatatypeDefinition datatypeDefinition();

	FunRestrictedDatatype restrictedDatatype();

	FunFacet facet();

	FunEmpty empty();

	FunTop top();

	FunTopObjectProperty topObjectProperty();

	FunTopDataProperty topDataProperty();

	FunBottomObjectProperty bottomObjectProperty();

	FunBottomDataProperty bottomDataProperty();

	/**
	 * Creates an ATerm from a text stream.
	 *
	 * @param  stream      the inputstream to read the ATerm from.
	 *
	 * @return             the parsed ATerm.
	 * @throws IOException
	 */
	ATerm readFromTextFile(InputStream stream) throws IOException;

	/**
	 * Creates an ATerm from a openllet.shared.hash text stream.
	 *
	 * @param  stream      the inputstream to read the ATerm from.
	 *
	 * @return             the parsed ATerm.
	 * @throws IOException
	 */
	ATerm readFromSharedTextFile(InputStream stream) throws IOException;

	/**
	 * Creates an ATerm from a binary stream.
	 *
	 * @param  stream      the inputstream to read the ATerm from.
	 *
	 * @return             the parsed ATerm.
	 * @throws IOException
	 */
	ATerm readFromBinaryFile(InputStream stream) throws IOException;

	/**
	 * Creates an ATerm from a stream.
	 * This function determines the type of stream (text, openllet.shared.hash, binary)
	 * and parses the ATerm accordingly.
	 *
	 * @param  stream      the inputstream to read the ATerm from.
	 *
	 * @return             the parsed ATerm.
	 * @throws IOException
	 */
	ATerm readFromFile(InputStream stream) throws IOException;

	/**
	 * Creates an ATerm from a given filename.
	 *
	 * @param  file        the filename to read the ATerm from.
	 *
	 * @return             the parsed ATerm.
	 * @throws IOException
	 */
	ATerm readFromFile(String file) throws IOException;

	/**
	 * Creates an ATerm by importing it from another ATermFactory.
	 *
	 * @param  term the term (possibly from another ATermFactory) to rebuild in
	 *              this factory.
	 *
	 * @return      the imported ATerm.
	 */
	ATerm importTerm(ATerm term);
}
