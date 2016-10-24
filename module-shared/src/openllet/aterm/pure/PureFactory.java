/*
 * Copyright (c) 2002-2007, CWI and INRIA
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of California, Berkeley nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

package openllet.aterm.pure;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import openllet.aterm.AFun;
import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.aterm.ATermBlob;
import openllet.aterm.ATermFactory;
import openllet.aterm.ATermInt;
import openllet.aterm.ATermList;
import openllet.aterm.ATermLong;
import openllet.aterm.ATermPlaceholder;
import openllet.aterm.ATermReal;
import openllet.aterm.ParseError;
import openllet.aterm.pure.binary.BAFReader;
import openllet.aterm.pure.binary.BinaryReader;
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
import openllet.atom.OpenError;
import openllet.shared.hash.SharedObject;
import openllet.shared.hash.SharedObjectFactory;

public class PureFactory extends SharedObjectFactory implements ATermFactory
{
	/**
	 * This is a fix-point hash-code such that
	 * empty.hashcode = empty.getAnnotations().hashCode
	 * this magic value can be found using: findEmptyHashCode()
	 */
	public final static int FIX_POINT = 240146486;

	private final ATermList _empty;

	private static boolean isBase64(final int c)
	{
		return Character.isLetterOrDigit(c) || c == '+' || c == '/';
	}

	public static int abbrevSize(final int strTerm)
	{
		int abbrev = strTerm;
		int size = 1;

		if (abbrev == 0)
			return 2;

		while (abbrev > 0)
		{
			size++;
			abbrev /= 64;
		}

		return size;
	}

	public PureFactory()
	{
		super();
		final ATermListImpl protoList = new ATermListImpl(this);

		protoList.init(FIX_POINT, null, null);
		_empty = (ATermList) build(protoList);
		((ATermListImpl) _empty).init(FIX_POINT, null, null);
	}

	@Override
	public ATermList makeList()
	{
		return _empty;
	}

	@Override
	public ATermList makeList(final ATerm singleton)
	{
		return makeList(singleton, _empty);
	}

	@Override
	public AFun makeAFun(final String name, final int arity, final boolean isQuoted)
	{
		return (AFun) build(new AFunImpl(this, name, arity, isQuoted));
	}

	@Override
	public ATermInt makeInt(final int value)
	{
		return (ATermInt) build(new ATermIntImpl(this, value));
	}

	@Override
	public ATermLong makeLong(final long value)
	{
		return (ATermLong) build(new ATermLongImpl(this, value));
	}

	@Override
	public ATermReal makeReal(final double value)
	{
		return (ATermReal) build(new ATermRealImpl(this, value));
	}

	@Override
	public ATermPlaceholder makePlaceholder(final ATerm type)
	{
		return (ATermPlaceholder) build(new ATermPlaceholderImpl(this, type));
	}

	@Override
	public ATermBlob makeBlob(final byte[] data)
	{
		return (ATermBlob) build(new ATermBlobImpl(this, data));
	}

	@Override
	public ATermList makeList(final ATerm first, final ATermList next)
	{
		return (ATermList) build(new ATermListImpl(this, first, next));
	}

	private static ATerm[] array0 = new ATerm[0];

	@Override
	public ATermAppl makeAppl(final AFun fun, final ATerm[] args)
	{
		return (ATermAppl) build(new ATermApplImpl(this, fun, args));
	}

	@Override
	public ATermAppl makeApplList(final AFun fun, final ATermList list)
	{
		final ATerm[] arg_array = new ATerm[list.getLength()];

		int i = 0;
		for (final ATerm term : list)
			arg_array[i++] = term;

		return makeAppl(fun, arg_array);
	}

	@Override
	public ATermAppl makeAppl(final AFun fun)
	{
		return makeAppl(fun, array0);
	}

	@Override
	public ATermAppl makeAppl(final AFun fun, final ATerm arg)
	{
		final ATerm[] argarray1 = new ATerm[] { arg };
		return makeAppl(fun, argarray1);
	}

	@Override
	public ATermAppl makeAppl(final AFun fun, final ATerm arg1, final ATerm arg2)
	{
		final ATerm[] argarray2 = new ATerm[] { arg1, arg2 };
		return makeAppl(fun, argarray2);
	}

	@Override
	public ATermAppl makeAppl(final AFun fun, final ATerm arg1, final ATerm arg2, final ATerm arg3)
	{
		final ATerm[] argarray3 = new ATerm[] { arg1, arg2, arg3 };
		return makeAppl(fun, argarray3);
	}

	@Override
	public ATermAppl makeAppl(final AFun fun, final ATerm arg1, final ATerm arg2, final ATerm arg3, final ATerm arg4)
	{
		final ATerm[] argarray4 = new ATerm[] { arg1, arg2, arg3, arg4 };
		return makeAppl(fun, argarray4);
	}

	@Override
	public ATermAppl makeAppl(final AFun fun, final ATerm arg1, final ATerm arg2, final ATerm arg3, final ATerm arg4, final ATerm arg5)
	{
		final ATerm[] argarray5 = new ATerm[] { arg1, arg2, arg3, arg4, arg5 };
		return makeAppl(fun, argarray5);
	}

	@Override
	public ATermAppl makeAppl(final AFun fun, final ATerm arg1, final ATerm arg2, final ATerm arg3, final ATerm arg4, final ATerm arg5, final ATerm arg6)
	{
		final ATerm[] args = { arg1, arg2, arg3, arg4, arg5, arg6 };
		return makeAppl(fun, args);
	}

	public ATermAppl makeAppl(final AFun fun, final ATerm arg1, final ATerm arg2, final ATerm arg3, final ATerm arg4, final ATerm arg5, final ATerm arg6, final ATerm arg7)
	{
		final ATerm[] args = { arg1, arg2, arg3, arg4, arg5, arg6, arg7 };
		return makeAppl(fun, args);
	}

	public ATermList getEmpty()
	{
		return _empty;
	}

	private static ATerm parseAbbrev(final ATermReader reader) throws IOException
	{
		ATerm result;
		int abbrev;

		int c = reader.read();

		abbrev = 0;
		while (isBase64(c))
		{
			abbrev *= 64;
			if (c >= 'A' && c <= 'Z')
				abbrev += c - 'A';
			else
				if (c >= 'a' && c <= 'z')
					abbrev += c - 'a' + 26;
				else
					if (c >= '0' && c <= '9')
						abbrev += c - '0' + 52;
					else
						if (c == '+')
							abbrev += 62;
						else
							if (c == '/')
								abbrev += 63;
							else
								throw new OpenError("not a base-64 digit: " + c);

			c = reader.read();
		}

		result = reader.getTerm(abbrev);

		return result;
	}

	private ATerm parseNumber(final ATermReader reader) throws IOException
	{
		final StringBuilder str = new StringBuilder();
		ATerm result;

		do
			str.append((char) reader.getLastChar());
		while (Character.isDigit(reader.read()));

		if (reader.getLastChar() != '.' && reader.getLastChar() != 'e' && reader.getLastChar() != 'E' && reader.getLastChar() != 'l' && reader.getLastChar() != 'L')
		{
			int val;
			try
			{
				val = Integer.parseInt(str.toString());
			}
			catch (final NumberFormatException e)
			{
				throw new ParseError("malformed int", e);
			}
			result = makeInt(val);
		}
		else
			if (reader.getLastChar() == 'l' || reader.getLastChar() == 'L')
			{
				reader.read();
				long val;
				try
				{
					val = Long.parseLong(str.toString());
				}
				catch (final NumberFormatException e)
				{
					throw new ParseError("malformed long", e);
				}
				result = makeLong(val);
			}
			else
			{
				if (reader.getLastChar() == '.')
				{
					str.append('.');
					reader.read();
					if (!Character.isDigit(reader.getLastChar()))
						throw new ParseError("digit expected");
					do
						str.append((char) reader.getLastChar());
					while (Character.isDigit(reader.read()));
				}
				if (reader.getLastChar() == 'e' || reader.getLastChar() == 'E')
				{
					str.append((char) reader.getLastChar());
					reader.read();
					if (reader.getLastChar() == '-' || reader.getLastChar() == '+')
					{
						str.append((char) reader.getLastChar());
						reader.read();
					}
					if (!Character.isDigit(reader.getLastChar()))
						throw new ParseError("digit expected!");
					do
						str.append((char) reader.getLastChar());
					while (Character.isDigit(reader.read()));
				}
				double val;
				try
				{
					val = Double.valueOf(str.toString()).doubleValue();
				}
				catch (final NumberFormatException e)
				{
					throw new ParseError("malformed real", e);
				}
				result = makeReal(val);
			}
		return result;
	}

	private static String parseId(final ATermReader reader) throws IOException
	{
		int c = reader.getLastChar();
		final StringBuilder buf = new StringBuilder(32);

		do
		{
			buf.append((char) c);
			c = reader.read();
		} while (Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '+' || c == '*' || c == '$');

		return buf.toString();
	}

	@SuppressWarnings("static-method")
	private String parseString(final ATermReader reader) throws IOException
	{
		boolean escaped;
		final StringBuilder str = new StringBuilder();

		do
		{
			escaped = false;
			if (reader.read() == '\\')
			{
				reader.read();
				escaped = true;
			}

			final int lastChar = reader.getLastChar();
			if (lastChar == -1)
				throw new ParseError("Unterminated quoted function symbol: " + str);

			if (escaped)
				switch (lastChar)
				{
					case 'n':
						str.append('\n');
						break;
					case 't':
						str.append('\t');
						break;
					case 'b':
						str.append('\b');
						break;
					case 'r':
						str.append('\r');
						break;
					case 'f':
						str.append('\f');
						break;
					case '\\':
						str.append('\\');
						break;
					case '\'':
						str.append('\'');
						break;
					case '\"':
						str.append('\"');
						break;
					case '0':
					case '1':
					case '2':
					case '3':
					case '4':
					case '5':
					case '6':
					case '7':
						str.append(reader.readOct());
						break;
					default:
						str.append('\\').append((char) reader.getLastChar());
				}
			else
				if (lastChar != '\"')
					str.append((char) lastChar);
		} while (escaped || reader.getLastChar() != '"');

		return str.toString();
	}

	private ATermList parseATerms(final ATermReader reader) throws IOException
	{
		final ATerm[] terms = parseATermsArray(reader);
		ATermList result = _empty;
		for (int i = terms.length - 1; i >= 0; i--)
			result = makeList(terms[i], result);

		return result;
	}

	private ATerm[] parseATermsArray(final ATermReader reader) throws IOException
	{
		final List<ATerm> list = new ArrayList<>();

		ATerm term = parseFromReader(reader);
		list.add(term);
		while (reader.getLastChar() == ',')
		{
			reader.readSkippingWS();
			term = parseFromReader(reader);
			list.add(term);
		}

		final ATerm[] array = new ATerm[list.size()];
		final ListIterator<ATerm> iter = list.listIterator();
		int index = 0;
		while (iter.hasNext())
			array[index++] = iter.next();
		return array;
	}

	private ATerm parseFromReader(final ATermReader reader) throws IOException
	{
		ATerm result;
		int c, start, end;
		String funname;

		start = reader.getPosition();
		switch (reader.getLastChar())
		{
			case -1:
				throw new ParseError("premature EOF encountered.");

			case '#':
				return parseAbbrev(reader);

			case '[':
				c = reader.readSkippingWS();
				if (c == -1)
					throw new ParseError("premature EOF encountered.");

				if (c == ']')
				{
					c = reader.readSkippingWS();
					result = _empty;
				}
				else
				{
					result = parseATerms(reader);
					if (reader.getLastChar() != ']')
						throw new ParseError("expected ']' but got '" + (char) reader.getLastChar() + "'");
					c = reader.readSkippingWS();
				}

				break;

			case '<':
				c = reader.readSkippingWS();
				final ATerm ph = parseFromReader(reader);

				if (reader.getLastChar() != '>')
					throw new ParseError("expected '>' but got '" + (char) reader.getLastChar() + "'");

				c = reader.readSkippingWS();

				result = makePlaceholder(ph);

				break;

			case '"':
				funname = parseString(reader);

				c = reader.readSkippingWS();
				if (reader.getLastChar() == '(')
				{
					c = reader.readSkippingWS();
					if (c == -1)
						throw new ParseError("premature EOF encountered.");
					if (reader.getLastChar() == ')')
						result = makeAppl(makeAFun(funname, 0, true));
					else
					{
						final ATerm[] list = parseATermsArray(reader);

						if (reader.getLastChar() != ')')
							throw new ParseError("_expected ')' but got '" + reader.getLastChar() + "'");
						result = makeAppl(makeAFun(funname, list.length, true), list);
					}
					c = reader.readSkippingWS();
				}
				else
					result = makeAppl(makeAFun(funname, 0, true));

				break;

			case '(':
				c = reader.readSkippingWS();
				if (c == -1)
					throw new ParseError("premature EOF encountered.");
				if (reader.getLastChar() == ')')
					result = makeAppl(makeAFun("", 0, false));
				else
				{
					final ATerm[] list = parseATermsArray(reader);

					if (reader.getLastChar() != ')')
						throw new ParseError("_expected ')' but got '" + (char) reader.getLastChar() + "'");
					result = makeAppl(makeAFun("", list.length, false), list);
				}
				c = reader.readSkippingWS();

				break;

			case '-':
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				result = parseNumber(reader);
				c = reader.skipWS();
				break;

			default:
				c = reader.getLastChar();
				if (Character.isLetter(c))
				{
					funname = parseId(reader);
					c = reader.skipWS();
					if (reader.getLastChar() == '(')
					{
						c = reader.readSkippingWS();
						if (c == -1)
							throw new ParseError("premature EOF encountered.");
						if (reader.getLastChar() == ')')
							result = makeAppl(makeAFun(funname, 0, false));
						else
						{
							final ATerm[] list = parseATermsArray(reader);

							if (reader.getLastChar() != ')')
								throw new ParseError("_expected ')' but got '" + (char) reader.getLastChar() + "'");
							result = makeAppl(makeAFun(funname, list.length, false), list);
						}
						c = reader.readSkippingWS();
					}
					else
						result = makeAppl(makeAFun(funname, 0, false));
				}
				else
					throw new ParseError("illegal character: '" + (char) reader.getLastChar() + "'");
		}

		if (reader.getLastChar() == '{')
			if (reader.readSkippingWS() == '}')
				reader.readSkippingWS();
			else
			{
				if (reader.getLastChar() != '}')
					throw new ParseError("'}' _expected '" + (char) reader.getLastChar() + "'");
				reader.readSkippingWS();
			}

		/* Parse some ToolBus anomalies for backwards compatibility */
		if (reader.getLastChar() == ':')
			reader.read();

		if (reader.getLastChar() == '?')
			reader.readSkippingWS();

		end = reader.getPosition();
		reader.storeNextTerm(result, end - start);

		return result;
	}

	@Override
	public ATerm parse(final String trm)
	{
		try
		{
			final ATermReader reader = new ATermReader(new StringReader(trm), trm.length());
			reader.readSkippingWS();
			final ATerm result = parseFromReader(reader);
			return result;
		}
		catch (final IOException e)
		{
			throw new ParseError("premature end of string", e);
		}
	}

	@Override
	public ATerm make(final String trm)
	{
		return parse(trm);
	}

	@Override
	public ATerm make(final String pattern, final List<Object> args)
	{
		return make(parse(pattern), args);
	}

	@Override
	public ATerm make(final String pattern, final Object arg1)
	{
		final List<Object> args = new LinkedList<>();
		args.add(arg1);
		return make(pattern, args);
	}

	@Override
	public ATerm make(final String pattern, final Object arg1, final Object arg2)
	{
		final List<Object> args = new LinkedList<>();
		args.add(arg1);
		args.add(arg2);
		return make(pattern, args);
	}

	@Override
	public ATerm make(final String pattern, final Object arg1, final Object arg2, final Object arg3)
	{
		final List<Object> args = new LinkedList<>();
		args.add(arg1);
		args.add(arg2);
		args.add(arg3);
		return make(pattern, args);
	}

	@Override
	public ATerm make(final ATerm pattern, final List<Object> args)
	{
		return pattern.make(args);
	}

	public ATerm parsePattern(final String pattern) throws ParseError
	{
		return parse(pattern);
	}

	protected boolean isDeepEqual(final ATerm t1, final ATerm t2)
	{
		if (t1.getType() != t2.getType())
			return false;

		// TODO : Need an implemention of Comparable<XTerm> for each type of ATerm.
		throw new UnsupportedOperationException("Not yet implemented! " + t1 + ", " + t2);
	}

	private ATerm readFromSharedTextFile(final ATermReader reader) throws IOException
	{
		reader.initializeSharing();
		return parseFromReader(reader);
	}

	private ATerm readFromTextFile(final ATermReader reader) throws IOException
	{
		return parseFromReader(reader);
	}

	@Override
	public ATerm readFromTextFile(final InputStream stream) throws IOException
	{
		final ATermReader reader = new ATermReader(new BufferedReader(new InputStreamReader(stream)));
		reader.readSkippingWS();

		return readFromTextFile(reader);
	}

	@Override
	public ATerm readFromSharedTextFile(final InputStream stream) throws IOException
	{
		final ATermReader reader = new ATermReader(new BufferedReader(new InputStreamReader(stream)));
		reader.readSkippingWS();

		if (reader.getLastChar() != '!')
			throw new IOException("not a openllet.shared.hash text file!");

		reader.readSkippingWS();

		return readFromSharedTextFile(reader);
	}

	@Override
	public ATerm readFromBinaryFile(final InputStream stream) throws IOException
	{
		return readFromBinaryFile(stream, false);
	}

	private ATerm readFromBinaryFile(final InputStream stream, final boolean headerRead) throws ParseError, IOException
	{
		final BAFReader r = new BAFReader(this, stream);
		return r.readFromBinaryFile(headerRead);
	}

	private ATerm readSAFFromOldStyleStream(final InputStream stream) throws IOException
	{
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final byte[] buffer = new byte[4096];
		int nrOfBytesRead;
		while ((nrOfBytesRead = stream.read(buffer, 0, buffer.length)) != -1)
			baos.write(buffer, 0, nrOfBytesRead);
		return BinaryReader.readTermFromSAFString(this, baos.toByteArray());
	}

	@Override
	public ATerm readFromFile(final InputStream stream) throws IOException
	{
		int firstToken;
		do
		{
			firstToken = stream.read();
			if (firstToken == -1)
				throw new IOException("Premature EOF.");
		} while (Character.isWhitespace((char) firstToken));

		final char typeByte = (char) firstToken;

		switch (typeByte)
		{
			case '!':
			{
				final ATermReader reader = new ATermReader(new BufferedReader(new InputStreamReader(stream)));
				reader.readSkippingWS();
				return readFromSharedTextFile(reader);
			}
			case '?':
			{
				return readSAFFromOldStyleStream(stream);
			}
			case '[':
			case '_':
			case '-':
				return readShiftFromFile(stream, typeByte);
			default:
			{
				if (Character.isLetterOrDigit(typeByte))
					return readShiftFromFile(stream, typeByte);
				else
					if (firstToken == 0)
						try (final BufferedInputStream bis = new BufferedInputStream(stream))
						{
							if (BAFReader.isBinaryATerm(bis))
								return readFromBinaryFile(bis, true);
						}
			}
		}

		throw new OpenError("Unsupported file type : " + typeByte);
	}

	private ATerm readShiftFromFile(final InputStream stream, final char typeByte) throws IOException
	{
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(stream)))
		{
			final ATermReader reader = new ATermReader(buffer);
			reader.last_char = typeByte; // Reinsert the type into the stream (since in this case it wasn't a type byte).
			return readFromTextFile(reader);
		}
	}

	@Override
	public ATerm readFromFile(final String filename) throws IOException
	{
		try (final FileInputStream fis = new FileInputStream(filename))
		{
			return readFromFile(fis);
		}
	}

	/**
	 * @see ATermFactory#importTerm(ATerm)
	 */
	@Override
	public ATerm importTerm(final ATerm term)
	{
		final SharedObject object = (SharedObject) term;
		if (contains(object))
			return term;

		ATerm result;

		switch (term.getType())
		{
			case ATerm.APPL:
				final ATermAppl appl = (ATermAppl) term;

				final AFun fun = (AFun) importTerm(appl.getAFun());

				final int nrOfArguments = appl.getArity();
				final ATerm[] newArguments = new ATerm[nrOfArguments];
				for (int i = nrOfArguments - 1; i >= 0; i--)
					newArguments[i] = importTerm(appl.getArgument(i));

				result = makeAppl(fun, newArguments);
				break;
			case ATerm.LIST:
				final ATermList list = (ATermList) term;
				if (list.isEmpty())
				{
					result = _empty;
					break;
				}
				final ATerm first = importTerm(list.getFirst());
				final ATermList next = (ATermList) importTerm(list.getNext());

				result = makeList(first, next);
				break;
			case ATerm.INT:
				final ATermInt integer = (ATermInt) term;

				result = makeInt(integer.getInt());
				break;
			case ATerm.LONG:
				final ATermLong elongatedType = (ATermLong) term;

				result = makeLong(elongatedType.getLong());
				break;
			case ATerm.REAL:
				final ATermReal real = (ATermReal) term;

				result = makeReal(real.getReal());
				break;
			case ATerm.PLACEHOLDER:
				final ATermPlaceholder placeHolder = (ATermPlaceholder) term;

				result = makePlaceholder(importTerm(placeHolder.getPlaceholder()));
				break;
			case ATerm.AFUN:
				final AFun afun = (AFun) term;

				return makeAFun(afun.getName(), afun.getArity(), afun.isQuoted());
			default:
				throw new OpenError("Unknown term type id: " + term.getType());
		}

		return result;
	}

	@Override
	public FunAnd and()
	{
		return (FunAnd) build(new FunAnd(this));
	}

	@Override
	public FunLiteral literal()
	{
		return (FunLiteral) build(new FunLiteral(this));
	}

	@Override
	public FunOr or()
	{
		return (FunOr) build(new FunOr(this));
	}

	@Override
	public FunSome some()
	{
		return (FunSome) build(new FunSome(this));
	}

	@Override
	public FunAll all()
	{
		return (FunAll) build(new FunAll(this));
	}

	@Override
	public FunNot not()
	{
		return (FunNot) build(new FunNot(this));
	}

	@Override
	public FunMax max()
	{
		return (FunMax) build(new FunMax(this));
	}

	@Override
	public FunMin min()
	{
		return (FunMin) build(new FunMin(this));
	}

	@Override
	public FunValue value()
	{
		return (FunValue) build(new FunValue(this));
	}

	@Override
	public FunSelf self()
	{
		return (FunSelf) build(new FunSelf(this));
	}

	@Override
	public FunCard card()
	{
		return (FunCard) build(new FunCard(this));
	}

	@Override
	public FunInv inv()
	{
		return (FunInv) build(new FunInv(this));
	}

	@Override
	public FunSubClassOf subClassOf()
	{
		return (FunSubClassOf) build(new FunSubClassOf(this));
	}

	@Override
	public FunEquivalentClasses equivalentClasses()
	{
		return (FunEquivalentClasses) build(new FunEquivalentClasses(this));
	}

	@Override
	public FunSameAs sameAs()
	{
		return (FunSameAs) build(new FunSameAs(this));
	}

	@Override
	public FunDisjointWith disjointWith()
	{
		return (FunDisjointWith) build(new FunDisjointWith(this));
	}

	@Override
	public FunDisjointClasses disjointClasses()
	{
		return (FunDisjointClasses) build(new FunDisjointClasses(this));
	}

	@Override
	public FunDisjointPropertyWith disjointPropertyWith()
	{
		return (FunDisjointPropertyWith) build(new FunDisjointPropertyWith(this));
	}

	@Override
	public FunDisjointProperties disjointProperties()
	{
		return (FunDisjointProperties) build(new FunDisjointProperties(this));
	}

	@Override
	public FunComplementOf complementOf()
	{
		return (FunComplementOf) build(new FunComplementOf(this));
	}

	@Override
	public FunVar var()
	{
		return (FunVar) build(new FunVar(this));
	}

	@Override
	public FunType type()
	{
		return (FunType) build(new FunType(this));
	}

	@Override
	public FunProp prop()
	{
		return (FunProp) build(new FunProp(this));
	}

	@Override
	public FunDifferent different()
	{
		return (FunDifferent) build(new FunDifferent(this));
	}

	@Override
	public FunAllDifferent allDifferent()
	{
		return (FunAllDifferent) build(new FunAllDifferent(this));
	}

	@Override
	public FunAsymmetric asymmetric()
	{
		return (FunAsymmetric) build(new FunAsymmetric(this));
	}

	@Override
	public FunFunctionnal functional()
	{
		return (FunFunctionnal) build(new FunFunctionnal(this));
	}

	@Override
	public FunInverseFunctional inverseFunctional()
	{
		return (FunInverseFunctional) build(new FunInverseFunctional(this));
	}

	@Override
	public FunIrreflexive irreflexive()
	{
		return (FunIrreflexive) build(new FunIrreflexive(this));
	}

	@Override
	public FunReflexive reflexive()
	{
		return (FunReflexive) build(new FunReflexive(this));
	}

	@Override
	public FunSymmetric symmetric()
	{
		return (FunSymmetric) build(new FunSymmetric(this));
	}

	@Override
	public FunTransitive transitive()
	{
		return (FunTransitive) build(new FunTransitive(this));
	}

	@Override
	public FunSubProperty subProperty()
	{
		return (FunSubProperty) build(new FunSubProperty(this));
	}

	@Override
	public FunEquivalentProperty equivalentProperty()
	{
		return (FunEquivalentProperty) build(new FunEquivalentProperty(this));
	}

	@Override
	public FunInverseProperty inverseProperty()
	{
		return (FunInverseProperty) build(new FunInverseProperty(this));
	}

	@Override
	public FunDomain domain()
	{
		return (FunDomain) build(new FunDomain(this));
	}

	@Override
	public FunRange range()
	{
		return (FunRange) build(new FunRange(this));
	}

	@Override
	public FunRule rule()
	{
		return (FunRule) build(new FunRule(this));
	}

	@Override
	public FunBuiltin builtin()
	{
		return (FunBuiltin) build(new FunBuiltin(this));
	}

	@Override
	public FunDatatypeDefinition datatypeDefinition()
	{
		return (FunDatatypeDefinition) build(new FunDatatypeDefinition(this));
	}

	@Override
	public FunRestrictedDatatype restrictedDatatype()
	{
		return (FunRestrictedDatatype) build(new FunRestrictedDatatype(this));
	}

	@Override
	public FunFacet facet()
	{
		return (FunFacet) build(new FunFacet(this));
	}

	@Override
	public FunEmpty empty()
	{
		return (FunEmpty) build(new FunEmpty(this));
	}

	@Override
	public FunTop top()
	{
		return (FunTop) build(new FunTop(this));
	}

	@Override
	public FunTopObjectProperty topObjectProperty()
	{
		return (FunTopObjectProperty) build(new FunTopObjectProperty(this));
	}

	@Override
	public FunTopDataProperty topDataProperty()
	{
		return (FunTopDataProperty) build(new FunTopDataProperty(this));
	}

	@Override
	public FunBottomObjectProperty bottomObjectProperty()
	{
		return (FunBottomObjectProperty) build(new FunBottomObjectProperty(this));
	}

	@Override
	public FunBottomDataProperty bottomDataProperty()
	{
		return (FunBottomDataProperty) build(new FunBottomDataProperty(this));
	}
}
