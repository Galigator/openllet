package openllet.aterm.pure.owl;

import java.io.IOException;

import openllet.aterm.AFun;
import openllet.aterm.ATerm;
import openllet.aterm.Visitor;
import openllet.aterm.pure.ATermImpl;
import openllet.aterm.pure.PureFactory;
import openllet.aterm.stream.BufferedOutputStreamWriter;
import openllet.shared.hash.HashFunctions;
import openllet.shared.hash.SharedObject;

public abstract class AFunOwl extends ATermImpl implements AFun
{
	protected AFunOwl(final PureFactory factory, final boolean lateHashComputation)
	{
		super(factory);
		if (!lateHashComputation)
			setHashCode(HashFunctions.hashTerm(getName(), isQuoted(), getArity()));
	}

	protected AFunOwl(final PureFactory factory)
	{
		super(factory);
		setHashCode(HashFunctions.hashTerm(getName(), isQuoted(), getArity()));
	}

	@Override
	public SharedObject duplicate()
	{
		return this;
	}

	@Override
	public boolean equivalent(final SharedObject obj)
	{
		if (obj instanceof AFun)
		{
			final AFun peer = (AFun) obj;
			return peer.getName().equals(getName()) && peer.getArity() == getArity() && peer.isQuoted() == isQuoted();
		}
		return false;
	}

	@Override
	public boolean isQuoted()
	{
		return false;
	}

	@Override
	public String getName()
	{
		return this.getClass().getSimpleName();
	}

	@Override
	public int getType()
	{
		return ATerm.AFUN;
	}

	@Override
	public int serialize(final BufferedOutputStreamWriter writer) throws IOException
	{
		final String name = getName();
		int bytesWritten = 0;
		if (isQuoted())
		{
			writer.write('"');
			bytesWritten++;
		}

		final int numberOfCharacters = name.length();
		bytesWritten += numberOfCharacters;
		for (int i = 0; i < numberOfCharacters; i++)
		{
			char c = name.charAt(i);
			switch (c)
			{
				case '\n':
					writer.write('\\');
					writer.write('n');
					bytesWritten++;
					break;
				case '\t':
					writer.write('\\');
					writer.write('t');
					bytesWritten++;
					break;
				case '\b':
					writer.write('\\');
					writer.write('b');
					bytesWritten++;
					break;
				case '\r':
					writer.write('\\');
					writer.write('r');
					bytesWritten++;
					break;
				case '\f':
					writer.write('\\');
					writer.write('f');
					bytesWritten++;
					break;
				case '\\':
					writer.write('\\');
					writer.write('\\');
					bytesWritten++;
					break;
				case '\'':
					writer.write('\\');
					writer.write('\'');
					bytesWritten++;
					break;
				case '\"':
					writer.write('\\');
					writer.write('\"');
					bytesWritten++;
					break;

				case '!':
				case '@':
				case '#':
				case '$':
				case '%':
				case '^':
				case '&':
				case '*':
				case '(':
				case ')':
				case '-':
				case '_':
				case '+':
				case '=':
				case '|':
				case '~':
				case '{':
				case '}':
				case '[':
				case ']':
				case ';':
				case ':':
				case '<':
				case '>':
				case ',':
				case '.':
				case '?':
				case ' ':
				case '/':
					writer.write(c);
					break;

				default:
					if (Character.isLetterOrDigit(c))
						writer.write(c);
					else
					{
						writer.write('\\');
						writer.write('0' + c / 64);
						c = (char) (c % 64);
						writer.write('0' + c / 8);
						c = (char) (c % 8);
						writer.write('0' + c);

						bytesWritten += 3;
					}
			}
		}

		if (isQuoted())
		{
			writer.write('"');
			bytesWritten++;
		}

		return bytesWritten;
	}

	@Override
	public String toString()
	{
		final String name = getName();
		final StringBuilder result = new StringBuilder(name.length());

		if (isQuoted())
			result.append('"');

		for (int i = 0; i < name.length(); i++)
		{
			char c = name.charAt(i);
			switch (c)
			{
				case '\n':
					result.append('\\');
					result.append('n');
					break;
				case '\t':
					result.append('\\');
					result.append('t');
					break;
				case '\b':
					result.append('\\');
					result.append('b');
					break;
				case '\r':
					result.append('\\');
					result.append('r');
					break;
				case '\f':
					result.append('\\');
					result.append('f');
					break;
				case '\\':
					result.append('\\');
					result.append('\\');
					break;
				case '\'':
					result.append('\\');
					result.append('\'');
					break;
				case '\"':
					result.append('\\');
					result.append('\"');
					break;

				case '!':
				case '@':
				case '#':
				case '$':
				case '%':
				case '^':
				case '&':
				case '*':
				case '(':
				case ')':
				case '-':
				case '_':
				case '+':
				case '=':
				case '|':
				case '~':
				case '{':
				case '}':
				case '[':
				case ']':
				case ';':
				case ':':
				case '<':
				case '>':
				case ',':
				case '.':
				case '?':
				case ' ':
				case '/':
					result.append(c);
					break;

				default:
					if (Character.isLetterOrDigit(c))
						result.append(c);
					else
					{
						result.append('\\');
						result.append((char) ('0' + c / 64));
						c = (char) (c % 64);
						result.append((char) ('0' + c / 8));
						c = (char) (c % 8);
						result.append((char) ('0' + c));
					}
			}
		}

		if (isQuoted())
			result.append('"');

		return result.toString();
	}

	@Override
	public ATerm accept(final Visitor<ATerm> v)
	{
		return v.visitAFun(this);
	}
}
