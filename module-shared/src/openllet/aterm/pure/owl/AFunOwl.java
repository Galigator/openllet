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
			setHashCode(hashFunction());
	}

	protected AFunOwl(final PureFactory factory)
	{
		super(factory);
		setHashCode(hashFunction());
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

	@SuppressWarnings("fallthrough")
	private int hashFunction()
	{
		int a = GOLDEN_RATIO;
		int b = GOLDEN_RATIO;
		final String name = getName();
		final int len = name.length();
		if (len >= 12)
			return hashFunction2();

		int c = isQuoted() ? 7 * getArity() + 1 : getArity() + 1; // Check if the quoted case exist.
		c += len;
		switch (len)
		{
			case 11:
				c += name.charAt(10) << 24;
			case 10:
				c += name.charAt(9) << 16;
			case 9:
				c += name.charAt(8) << 8;
				/* the first byte of c is reserved for the length */
			case 8:
				b += name.charAt(7) << 24;
			case 7:
				b += name.charAt(6) << 16;
			case 6:
				b += name.charAt(5) << 8;
			case 5:
				b += name.charAt(4);
			case 4:
				a += name.charAt(3) << 24;
			case 3:
				a += name.charAt(2) << 16;
			case 2:
				a += name.charAt(1) << 8;
			case 1:
				a += name.charAt(0);
			default:
				return HashFunctions.mix(a, b, c);
		}
	}

	/**
	 * Handle the last 11 bytes
	 */
	@SuppressWarnings("fallthrough")
	private int hashFunction2()
	{
		final String name = getName();
		final int count = name.length();
		final char[] source = new char[count];

		name.getChars(0, count, source, 0);
		int len = count;
		int a = GOLDEN_RATIO;
		int b = GOLDEN_RATIO;

		int c = isQuoted() ? 7 * (getArity() + 1) : getArity() + 1; // to avoid collison
		int k = 0;

		while (len >= 12)
		{
			a += source[k + 0] + (source[k + 1] << 8) + (source[k + 2] << 16) + (source[k + 3] << 24);
			b += source[k + 4] + (source[k + 5] << 8) + (source[k + 6] << 16) + (source[k + 7] << 24);
			c += source[k + 8] + (source[k + 9] << 8) + (source[k + 10] << 16) + (source[k + 11] << 24);
			// mix(a,b,c);
			a -= b;
			a -= c;
			a ^= c >> 13;
			b -= c;
			b -= a;
			b ^= a << 8;
			c -= a;
			c -= b;
			c ^= b >> 13;
			a -= b;
			a -= c;
			a ^= c >> 12;
			b -= c;
			b -= a;
			b ^= a << 16;
			c -= a;
			c -= b;
			c ^= b >> 5;
			a -= b;
			a -= c;
			a ^= c >> 3;
			b -= c;
			b -= a;
			b ^= a << 10;
			c -= a;
			c -= b;
			c ^= b >> 15;

			k += 12;
			len -= 12;
		}

		// Handle most of the key
		c += count;
		switch (len)
		{
			case 11:
				c += source[k + 10] << 24;
			case 10:
				c += source[k + 9] << 16;
			case 9:
				c += source[k + 8] << 8;
				/* the first byte of c is reserved for the length */
			case 8:
				b += source[k + 7] << 24;
			case 7:
				b += source[k + 6] << 16;
			case 6:
				b += source[k + 5] << 8;
			case 5:
				b += source[k + 4];
			case 4:
				a += source[k + 3] << 24;
			case 3:
				a += source[k + 2] << 16;
			case 2:
				a += source[k + 1] << 8;
			case 1:
				a += source[k + 0];
			default:
				return HashFunctions.mix(a, b, c);
		}
	}
}
