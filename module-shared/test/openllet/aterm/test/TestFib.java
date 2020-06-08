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

package openllet.aterm.test;

import openllet.aterm.AFun;
import openllet.aterm.ATermAppl;
import openllet.aterm.ATermFactory;
import openllet.aterm.pure.PureFactory;
import org.junit.Before;
import org.junit.Test;

public class TestFib
{
	private ATermFactory	_factory;

	private AFun			zero, suc, plus, fib;
	private ATermAppl		tzero;

	public final static void main(final String[] args)
	{
		final TestFib t = newTestFib(new PureFactory());

		t.test1();
		t.test2();
		t.test3(15);
	}

	public TestFib()
	{

	}

	@Before
	public void setUp()
	{
		_factory = new PureFactory();

		zero = _factory.makeAFun("zero", 0, false);
		suc = _factory.makeAFun("suc", 1, false);
		plus = _factory.makeAFun("plus", 2, false);
		fib = _factory.makeAFun("fib", 1, false);
		tzero = _factory.makeAppl(zero);
	}

	public final static TestFib newTestFib(final ATermFactory factory)
	{
		final TestFib t = new TestFib();

		t._factory = factory;

		t.zero = factory.makeAFun("zero", 0, false);
		t.suc = factory.makeAFun("suc", 1, false);
		t.plus = factory.makeAFun("plus", 2, false);
		t.fib = factory.makeAFun("fib", 1, false);
		t.tzero = factory.makeAppl(t.zero);

		return t;
	}

	@Test
	public void test1()
	{
		normalizePlus(_factory.makeAppl(plus, _factory.makeAppl(suc, _factory.makeAppl(suc, tzero)), _factory.makeAppl(suc, _factory.makeAppl(suc, tzero))));
	}

	@Test
	public void test2()
	{
		// System.out.println("test 2");
		normalizeFib(_factory.makeAppl(fib, _factory.makeAppl(suc, _factory.makeAppl(suc, _factory.makeAppl(suc, _factory.makeAppl(suc, tzero))))));

		// System.out.println("res = fib(4) = " + res);
	}

	public void test3(final int n)
	{
		ATermAppl N = tzero;
		for (int i = 0; i < n; i++)
			N = _factory.makeAppl(suc, N);
		normalizeFib(_factory.makeAppl(fib, N));

		System.out.println(_factory);
	}

	public ATermAppl normalizePlus(final ATermAppl t)
	{
		ATermAppl res = t;
		while (true)
		{
			final ATermAppl v0 = (ATermAppl) res.getArgument(0);

			// plus(s(s(s(s(s(x))))),y) => plus(x,s(s(s(s(s(y))))))
			if (v0.getAFun() == suc)
			{
				final ATermAppl v1 = (ATermAppl) v0.getArgument(0);
				if (v1.getAFun() == suc)
				{
					final ATermAppl v2 = (ATermAppl) v1.getArgument(0);
					if (v2.getAFun() == suc)
					{
						final ATermAppl v3 = (ATermAppl) v2.getArgument(0);
						if (v3.getAFun() == suc)
						{
							final ATermAppl v4 = (ATermAppl) v3.getArgument(0);
							if (v4.getAFun() == suc)
							{
								res = _factory.makeAppl(plus, v4.getArgument(0),
										_factory.makeAppl(suc, _factory.makeAppl(suc, _factory.makeAppl(suc, _factory.makeAppl(suc, _factory.makeAppl(suc, res.getArgument(1)))))));
								continue;
							}
						}
					}
				}
			}

			// plus(0,x) = x
			if (v0.getAFun() == zero)
			{
				res = (ATermAppl) res.getArgument(1);
				break;
			}

			// plus(s(x),y) => plus(x,s(y))
			if (v0.getAFun() == suc)
			{
				res = _factory.makeAppl(plus, v0.getArgument(0), _factory.makeAppl(suc, res.getArgument(1)));
				continue;
			}
			break;
		}
		return res;
	}

	public ATermAppl normalizeFib(final ATermAppl t)
	{

		ATermAppl res = t;
		while (true)
		{
			// fib(0) = suc(0)
			final ATermAppl v0 = (ATermAppl) res.getArgument(0);
			if (v0.getAFun() == zero)
			{
				res = _factory.makeAppl(suc, v0);
				break;
			}
			// fib(suc(0)) => suc(0)
			if (v0.getAFun() == suc)
			{
				final ATermAppl v1 = (ATermAppl) v0.getArgument(0);
				if (v1.getAFun() == zero)
				{
					res = v0;
					break;
				}
			}
			//  fib(s(s(x))) => plus(fib(x),fib(s(x)))
			//     v0 v1
			if (v0.getAFun() == suc)
			{
				final ATermAppl v1 = (ATermAppl) v0.getArgument(0);
				if (v1.getAFun() == suc)
				{
					final ATermAppl v2 = (ATermAppl) v1.getArgument(0);
					final ATermAppl fib1 = normalizeFib(_factory.makeAppl(fib, v2));
					final ATermAppl fib2 = normalizeFib(_factory.makeAppl(fib, v1));
					//System.out.println("adding");
					res = normalizePlus(_factory.makeAppl(plus, fib1, fib2));
					break;
				}
			}
			break;
		}
		return res;
	}

	public void setFactory(final ATermFactory factory2)
	{
		_factory = factory2;
	}
}
