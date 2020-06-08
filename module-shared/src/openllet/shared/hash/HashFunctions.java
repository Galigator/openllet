/*
 * Copyright (c) 2003-2007, CWI and INRIA
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * - Neither the name of the CWI, INRIA nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package openllet.shared.hash;

public class HashFunctions
{
	/** The golden ratio; an arbitrary value */
	public static final int GOLDEN_RATIO = 0x9e3779b9;

	static public int oneAtATime(final Object[] o)
	{
		int hash = 0;
		for (final Object element : o)
		{
			hash += element.hashCode();
			hash += hash << 10;
			hash ^= hash >> 6;
		}
		hash += hash << 3;
		hash ^= hash >> 11;
		hash += hash << 15;
		return hash;
	}

	static public int simple(final Object[] o)
	{
		int hash = o[o.length - 1].hashCode();
		for (int i = 0; i < o.length - 1; i++)
			hash = 16 * hash + o[i].hashCode();
		return hash;
	}

	static public int cwi(final Object[] o)
	{
		int hash = 0;
		for (final Object element : o)
			hash = hash << 1 ^ hash >> 1 ^ element.hashCode();
		return hash;
	}

	static public int doobs(final Object[] o)
	{
		int alpha = GOLDEN_RATIO;
		alpha += /*(o[1].hashCode() << 8) +  was annonations part */ o[0].hashCode();
		return mix(alpha, GOLDEN_RATIO, o.length/* the previous hash value */);
	}

	public static int mix(final int aBit, final int bBit, final int cBit)
	{
		int a = aBit, b = bBit, c = cBit;

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

		return c;
	}

	@SuppressWarnings("fallthrough")
	public static int hashTerm(final String name, final boolean isQuoted, final int arity)
	{
		int a = GOLDEN_RATIO;
		int b = GOLDEN_RATIO;
		final int len = name.length();
		if (len >= 12) return hashLongNames(name, isQuoted, arity);

		int c = isQuoted ? 7 * arity + 1 : arity + 1; // Check if the quoted case exist.
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

	@SuppressWarnings("fallthrough")
	public static int hashLongNames(final String name, final boolean isQuoted, final int arity)
	{
		final int count = name.length();
		final char[] source = new char[count];

		name.getChars(0, count, source, 0);
		int len = count;
		int a = GOLDEN_RATIO;
		int b = GOLDEN_RATIO;

		int c = isQuoted ? 7 * (arity + 1) : arity + 1; // to avoid collison
		int k = 0;

		while (len >= 12)
		{
			a += source[k + 0] + (source[k + 1] << 8) + (source[k + 2] << 16) + (source[k + 3] << 24);
			b += source[k + 4] + (source[k + 5] << 8) + (source[k + 6] << 16) + (source[k + 7] << 24);
			c += source[k + 8] + (source[k + 9] << 8) + (source[k + 10] << 16) + (source[k + 11] << 24);

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
